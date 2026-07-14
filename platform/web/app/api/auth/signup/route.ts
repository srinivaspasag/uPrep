import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { hashPassword } from "@/lib/password";

export const dynamic = "force-dynamic";

// Public self-registration, gated by the org's External Signup config. Creates a
// login-capable orgmembers record: the chosen password is hashed locally, so the
// user can sign in with their email + password (via /api/auth/login) without any
// email-verification step. If the org requires approval, the account is created
// PENDING and can't log in until an admin activates it.
type Body = {
  orgId?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  contactNumber?: string;
  password?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const orgId = b.orgId || DEFAULT_ORG_ID;
  const firstName = (b.firstName || "").trim();
  const email = (b.email || "").trim().toLowerCase();
  const password = (b.password || "").trim();

  if (!firstName) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  if (!email) return NextResponse.json({ error: "Email is required" }, { status: 400 });
  if (password.length < 6)
    return NextResponse.json({ error: "Password must be at least 6 characters" }, { status: 400 });

  try {
    const db = await getDb();
    const cfg: any = (await db.collection("signupconfigs").findOne({ orgId })) || {};
    if (!cfg.enabled) {
      return NextResponse.json(
        { error: "Self-signup is disabled for this institute. Contact your admin." },
        { status: 403 }
      );
    }

    if (cfg.allowedDomains) {
      const domains = String(cfg.allowedDomains)
        .split(",")
        .map((d: string) => d.trim().toLowerCase())
        .filter(Boolean);
      const emailDomain = email.split("@")[1] || "";
      if (domains.length && !domains.includes(emailDomain)) {
        return NextResponse.json(
          { error: `Email domain not allowed. Use one of: ${domains.join(", ")}` },
          { status: 400 }
        );
      }
    }

    const existing = await db.collection("orgmembers").findOne({ orgId, email });
    if (existing) return NextResponse.json({ error: "An account with this email already exists." }, { status: 409 });

    const requireApproval = cfg.requireApproval !== false;
    const profile = (cfg.defaultProfile || "STUDENT").toUpperCase();
    const now = Date.now();
    const _id = new ObjectId();
    const memberId = `${profile.slice(0, 3)}_${now.toString().slice(-6)}`;

    // Self-signups have no linked login account yet, but orgmembers has a unique
    // index on (orgId, userId), so we can't leave userId null (multiple pending
    // signups would collide). Use a unique placeholder until an admin/legacy
    // activation links the real user account.
    await db.collection("orgmembers").insertOne({
      _id,
      userId: `SELF_${_id.toHexString()}`,
      orgId,
      memberId,
      firstName,
      lastName: (b.lastName || "").trim(),
      email,
      profile,
      contactNumber: (b.contactNumber || "").trim(),
      authType: "LOCAL",
      passwordHash: hashPassword(password),
      signupSource: "SELF",
      approvalStatus: requireApproval ? "PENDING" : "APPROVED",
      recordState: requireApproval ? "PENDING" : "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });

    return NextResponse.json({
      ok: true,
      requireApproval,
      memberId,
      loginId: email,
      welcomeMessage: cfg.welcomeMessage || "",
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Signup failed" }, { status: 500 });
  }
}
