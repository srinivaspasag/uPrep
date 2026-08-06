import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import {
  SESSION_COOKIE,
  createSessionToken,
  sessionCookieOptions,
} from "@/lib/auth-session";
import { recordLogin } from "@/lib/login-log";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { phone?: string; code?: string; orgId?: string; firstName?: string };

const OTP_COLL = "otps";
const MAX_ATTEMPTS = 5;

function normPhone(p: string): string {
  return (p || "").replace(/[^\d+]/g, "");
}

// Verify an OTP and sign the user in. If no member exists for the phone, one is
// created on the fly (STUDENT self-signup) so phone-first onboarding works.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const phone = normPhone(b.phone || "");
  const code = (b.code || "").trim();
  if (!phone || !code) return NextResponse.json({ error: "Phone and code are required" }, { status: 400 });

  try {
    const db = await getDb();
    const rec: any = await db
      .collection(OTP_COLL)
      .find({ phone, used: false })
      .sort({ timeCreated: -1 })
      .limit(1)
      .next();

    if (!rec || (rec.exp && rec.exp < Date.now()))
      return NextResponse.json({ error: "Code expired — request a new one" }, { status: 400 });
    if ((rec.attempts || 0) >= MAX_ATTEMPTS)
      return NextResponse.json({ error: "Too many attempts — request a new code" }, { status: 429 });

    if (rec.code !== code) {
      await db.collection(OTP_COLL).updateOne({ _id: rec._id }, { $inc: { attempts: 1 } });
      return NextResponse.json({ error: "Incorrect code" }, { status: 401 });
    }

    await db.collection(OTP_COLL).updateOne({ _id: rec._id }, { $set: { used: true, usedAt: Date.now() } });

    const orgId = b.orgId || rec.orgId || DEFAULT_ORG_ID;
    let member: any = await db
      .collection("orgmembers")
      .findOne({ orgId, contactNumber: phone, recordState: "ACTIVE" });

    if (!member) {
      const now = Date.now();
      const _id = new ObjectId();
      const memberId = `OTP_${phone.slice(-6)}_${now.toString().slice(-4)}`;
      member = {
        _id,
        userId: `LOCAL_${_id.toHexString()}`,
        orgId,
        memberId,
        firstName: (b.firstName || "").trim() || "Student",
        lastName: "",
        email: "",
        profile: "STUDENT",
        contactNumber: phone,
        authType: "OTP",
        recordState: "ACTIVE",
        timeCreated: now,
        lastUpdated: now,
      };
      await db.collection("orgmembers").insertOne(member);
    }

    const profile = (member.profile || "STUDENT").toUpperCase();
    const isSuperAdmin = member?.isSuperAdmin === true || member?.extraInfo?.isSuperAdmin === true;
    await recordLogin(req, {
      orgId,
      userId: String(member._id),
      memberId: member.memberId || null,
      method: "OTP",
    });
    const res = NextResponse.json({
      result: {
        id: String(member._id),
        orgId,
        firstName: member.firstName || "",
        lastName: member.lastName || "",
        memberId: member.memberId || null,
        profile,
        isSuperAdmin,
        authType: "OTP",
      },
    });
    const token = await createSessionToken({
      id: String(member._id),
      orgId,
      memberId: member.memberId || null,
      firstName: member.firstName || "",
      lastName: member.lastName || "",
      profile,
      isSuperAdmin,
    });
    res.cookies.set(SESSION_COOKIE, token, sessionCookieOptions());
    return res;
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Verification failed" }, { status: 500 });
  }
}
