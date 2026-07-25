import { NextRequest, NextResponse } from "next/server";
import { randomBytes } from "crypto";
import { API, CALLING_APP, CALLING_APP_ID, DEFAULT_ORG_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";
import { sendEmail, sendSms } from "@/lib/messaging";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { username?: string; orgId?: string };

const RESET_COLL = "passwordresets";
const RESET_TTL_MS = 60 * 60 * 1000; // 1 hour

function baseUrl(req: NextRequest): string {
  const env = process.env.NEXT_PUBLIC_APP_URL;
  if (env) return env.replace(/\/$/, "");
  const origin = req.headers.get("origin");
  if (origin) return origin.replace(/\/$/, "");
  const host = req.headers.get("host");
  const proto = req.headers.get("x-forwarded-proto") || "https";
  return host ? `${proto}://${host}` : "";
}

// Start a password reset. For LOCAL accounts (scrypt passwordHash) we mint a
// single-use token and deliver a reset link via the pluggable messaging layer
// (email + SMS). Legacy accounts fall back to user-services
// `sendForgotPasswordMail`. Always responds ok so we never reveal whether an
// account exists.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const username = (b.username || "").trim();
  const lookup = username.toLowerCase();
  if (!username) return NextResponse.json({ error: "Enter your email or member ID" }, { status: 400 });

  try {
    const db = await getDb();
    const query: any = {
      recordState: "ACTIVE",
      passwordHash: { $exists: true, $ne: null },
      $or: [{ memberId: username }, { email: lookup }],
    };
    if (b.orgId) query.orgId = b.orgId;
    const member: any = await db.collection("orgmembers").findOne(query);

    if (member) {
      const token = randomBytes(24).toString("hex");
      const now = Date.now();
      await db.collection(RESET_COLL).insertOne({
        token,
        memberId: member._id,
        orgId: member.orgId,
        used: false,
        exp: now + RESET_TTL_MS,
        timeCreated: now,
      });

      const link = `${baseUrl(req)}/reset-password?token=${token}`;
      const name = member.firstName || "there";
      const text = `Hi ${name}, reset your UPrep password using this link (valid 1 hour): ${link}`;
      if (member.email) await sendEmail(member.email, "Reset your UPrep password", text, { memberId: String(member._id) });
      if (member.contactNumber) await sendSms(member.contactNumber, text, { memberId: String(member._id) });

      return NextResponse.json({ ok: true });
    }
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Reset failed" }, { status: 500 });
  }

  // Legacy fallback (non-local accounts).
  const form = new URLSearchParams({
    callingApp: CALLING_APP,
    callingAppId: CALLING_APP_ID,
    username: lookup,
    orgId: b.orgId || DEFAULT_ORG_ID,
  });
  try {
    const res = await fetch(`${API.user}/users/sendForgotPasswordMail`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form.toString(),
    });
    const data = await res.json().catch(() => ({}));
    if (data?.errorCode || data?.errorMessage) {
      // Still respond ok to avoid leaking account existence.
      return NextResponse.json({ ok: true });
    }
    return NextResponse.json({ ok: true });
  } catch {
    return NextResponse.json({ ok: true });
  }
}
