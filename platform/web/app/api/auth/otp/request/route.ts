import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sendSms } from "@/lib/messaging";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { phone?: string; orgId?: string };

const OTP_COLL = "otps";
const OTP_TTL_MS = 5 * 60 * 1000; // 5 minutes

function normPhone(p: string): string {
  return (p || "").replace(/[^\d+]/g, "");
}

// Request a login/signup OTP for a phone number. Delivered via the pluggable
// SMS layer (real provider when SMS_PROVIDER=webhook, else logged). Set
// OTP_DEV_ECHO=1 to return the code in the response for local/demo testing.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const phone = normPhone(b.phone || "");
  if (phone.replace(/\D/g, "").length < 6)
    return NextResponse.json({ error: "Enter a valid phone number" }, { status: 400 });

  const code = String(Math.floor(100000 + Math.random() * 900000));
  const now = Date.now();
  try {
    const db = await getDb();
    // Invalidate any prior pending OTPs for this phone.
    await db.collection(OTP_COLL).updateMany({ phone, used: false }, { $set: { used: true } });
    await db.collection(OTP_COLL).insertOne({
      phone,
      orgId: b.orgId || null,
      code,
      attempts: 0,
      used: false,
      exp: now + OTP_TTL_MS,
      timeCreated: now,
    });

    await sendSms(phone, `Your UPrep verification code is ${code}. It expires in 5 minutes.`, { kind: "otp" });

    const devEcho = process.env.OTP_DEV_ECHO === "1";
    return NextResponse.json({ ok: true, ...(devEcho ? { devCode: code } : {}) });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Could not send code" }, { status: 500 });
  }
}
