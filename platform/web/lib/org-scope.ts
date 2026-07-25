import type { NextRequest } from "next/server";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";

// Resolves which org a staff (CMDS) request operates on. This is the multi-tenant
// boundary: a regular org admin/teacher is PINNED to the org in their signed
// session cookie and cannot act on another org's data. Only a super admin may
// target a different org via an explicit override (?orgId= / body.orgId), which
// is how legacy Widgets org-switching worked.
//
// Falls back to DEFAULT_ORG_ID only when there's no session at all (shouldn't
// happen behind the /api/cmds middleware gate, but keeps callers safe).
export async function resolveOrgId(
  req: NextRequest,
  override?: string | null
): Promise<string> {
  const session = await sessionFromReq(req);
  if (session && isSuperAdmin(session.profile, session.isSuperAdmin) && override) {
    return override;
  }
  return session?.orgId || override || DEFAULT_ORG_ID;
}
