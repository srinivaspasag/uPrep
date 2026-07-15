import { NextResponse } from "next/server";
import { SESSION_COOKIE } from "@/lib/auth-session";

export const runtime = "nodejs";

// Clears the server-trusted session cookie. Called by clearSession() so logout
// invalidates CMDS access, not just the client-side sessionStorage copy.
export async function POST() {
  const res = NextResponse.json({ ok: true });
  res.cookies.set(SESSION_COOKIE, "", { path: "/", maxAge: 0 });
  return res;
}
