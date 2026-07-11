import { NextRequest, NextResponse } from "next/server";
import { API, CALLING_APP, CALLING_APP_ID } from "@/lib/config";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = {
  userId?: string;
  email?: string;
  oldPassword?: string;
  newPassword?: string;
};

// Change the logged-in student's password via the legacy user-services
// `changeUserPassword` endpoint (handles per-user salt + SYSTEM_SALT hashing).
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const userId = (b.userId || "").trim();
  const email = (b.email || "").trim();
  const oldPassword = b.oldPassword || "";
  const newPassword = b.newPassword || "";

  if (!userId || !email) return NextResponse.json({ error: "Missing account details" }, { status: 400 });
  if (!oldPassword || !newPassword)
    return NextResponse.json({ error: "Both current and new passwords are required" }, { status: 400 });
  if (newPassword.length < 6)
    return NextResponse.json({ error: "New password must be at least 6 characters" }, { status: 400 });

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
    // Legacy wraps errors in a body even on HTTP 200.
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
