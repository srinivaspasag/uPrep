import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { API, CALLING_APP, CALLING_APP_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { hashPassword, verifyPassword } from "@/lib/password";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = {
  userId?: string;
  email?: string;
  oldPassword?: string;
  newPassword?: string;
};

// Change the signed-in user's password.
//
// Local accounts (created via CMDS / self-signup, carrying a scrypt passwordHash)
// are updated directly in Mongo against the server-trusted session — no legacy
// user-services round-trip. Legacy accounts still proxy to
// `changeUserPassword` (per-user salt + SYSTEM_SALT hashing).
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const oldPassword = b.oldPassword || "";
  const newPassword = b.newPassword || "";

  if (!oldPassword || !newPassword)
    return NextResponse.json({ error: "Both current and new passwords are required" }, { status: 400 });
  if (newPassword.length < 6)
    return NextResponse.json({ error: "New password must be at least 6 characters" }, { status: 400 });

  // Local-account path: identify the caller from the signed session cookie.
  try {
    const session = await sessionFromReq(req);
    if (session?.id && ObjectId.isValid(session.id)) {
      const db = await getDb();
      const member: any = await db
        .collection("orgmembers")
        .findOne({ _id: new ObjectId(session.id), recordState: "ACTIVE" });
      if (member?.passwordHash) {
        if (!verifyPassword(oldPassword, member.passwordHash))
          return NextResponse.json({ error: "Current password is incorrect" }, { status: 400 });
        await db
          .collection("orgmembers")
          .updateOne(
            { _id: member._id },
            { $set: { passwordHash: hashPassword(newPassword), lastUpdated: Date.now() } }
          );
        return NextResponse.json({ ok: true });
      }
    }
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Could not change password" }, { status: 500 });
  }

  // Legacy account fallback.
  const userId = (b.userId || "").trim();
  const email = (b.email || "").trim();
  if (!userId || !email)
    return NextResponse.json({ error: "Missing account details" }, { status: 400 });

  const form = new URLSearchParams({
    callingApp: CALLING_APP,
    callingAppId: CALLING_APP_ID,
    callingUserId: userId,
    userId,
    email,
    targetUserId: userId,
    oldPassword,
    newPassword,
  });

  try {
    const res = await fetch(`${API.user}/users/changeUserPassword`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form.toString(),
    });
    const data = await res.json().catch(() => ({}));
    if (data?.errorCode || data?.errorMessage) {
      return NextResponse.json(
        { error: data.errorMessage || "Could not change password" },
        { status: 400 }
      );
    }
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Password service is unavailable" },
      { status: 502 }
    );
  }
}
