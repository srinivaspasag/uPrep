import { NextRequest, NextResponse } from "next/server";
import { API, CALLING_APP, CALLING_APP_ID, DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { username?: string; orgId?: string };

// Trigger a password-reset email via legacy user-services
// `sendForgotPasswordMail`. Username can be an email or memberId.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const username = (b.username || "").trim().toLowerCase();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!username) return NextResponse.json({ error: "Enter your email or member ID" }, { status: 400 });

  const form = new URLSearchParams({
    callingApp: CALLING_APP,
    callingAppId: CALLING_APP_ID,
    username,
    orgId,
  });

  try {
    const res = await fetch(`${API.user}/users/sendForgotPasswordMail`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form.toString(),
    });
    const data = await res.json().catch(() => ({}));
    if (data?.errorCode || data?.errorMessage) {
      return NextResponse.json(
        { error: data.errorMessage || "Could not send reset email" },
        { status: 400 }
      );
    }
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Reset service is unavailable" },
      { status: 502 }
    );
  }
}
