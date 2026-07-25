import type { NextRequest } from "next/server";
import {
  SESSION_COOKIE,
  verifySessionToken,
  type SessionPayload,
} from "@/lib/auth-session";

// Reads the server-trusted session from a route handler's request cookie.
// Routes under /api/cmds/** are already gated to staff by middleware.ts; this is
// for routes that need the caller's identity/org/role (enrollment filtering,
// superadmin checks) without trusting client-held sessionStorage.
export async function sessionFromReq(
  req: NextRequest
): Promise<SessionPayload | null> {
  return verifySessionToken(req.cookies.get(SESSION_COOKIE)?.value);
}
