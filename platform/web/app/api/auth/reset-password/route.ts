import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { hashPassword } from "@/lib/password";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { token?: string; newPassword?: string };

const RESET_COLL = "passwordresets";

// Consume a single-use reset token (from forgot-password) and set a new local
// password. Token is invalidated on use and after its TTL.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const token = (b.token || "").trim();
  const newPassword = b.newPassword || "";
  if (!token) return NextResponse.json({ error: "Missing reset token" }, { status: 400 });
  if (newPassword.length < 6)
    return NextResponse.json({ error: "Password must be at least 6 characters" }, { status: 400 });

  try {
    const db = await getDb();
    const rec: any = await db.collection(RESET_COLL).findOne({ token });
    if (!rec || rec.used || (rec.exp && rec.exp < Date.now()))
      return NextResponse.json({ error: "This reset link is invalid or has expired." }, { status: 400 });

    await db
      .collection("orgmembers")
      .updateOne(
        { _id: rec.memberId },
        { $set: { passwordHash: hashPassword(newPassword), authType: "LOCAL", lastUpdated: Date.now() } }
      );
    await db.collection(RESET_COLL).updateOne({ _id: rec._id }, { $set: { used: true, usedAt: Date.now() } });

    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Reset failed" }, { status: 500 });
  }
}
