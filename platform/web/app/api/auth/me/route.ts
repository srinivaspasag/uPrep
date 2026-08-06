import { NextRequest, NextResponse } from "next/server";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Returns the current server-trusted session identity. Used by the client to
// rehydrate its session snapshot (e.g. after stopping impersonation).
export async function GET(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s) return NextResponse.json({ result: null }, { status: 401 });
  return NextResponse.json({
    result: {
      id: s.id,
      orgId: s.orgId,
      firstName: s.firstName || "",
      lastName: s.lastName || "",
      memberId: s.memberId || null,
      profile: s.profile,
      isSuperAdmin: s.isSuperAdmin,
    },
  });
}
