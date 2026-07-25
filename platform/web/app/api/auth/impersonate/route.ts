import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import {
  SESSION_COOKIE,
  createSessionToken,
  sessionCookieOptions,
} from "@/lib/auth-session";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff, isSuperAdmin } from "@/lib/roles";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// The admin's real identity is parked here while they browse as another user,
// so "Return to admin" can restore it.
const RETURN_COOKIE = "uprep_return";

type Body = { memberId?: string };

// Start impersonation: issue the target member's session while stashing the
// admin's own token. Org admins may only impersonate members in their own org;
// super admins may impersonate anyone. Cannot impersonate another super admin.
//
// Lives under /api/auth (not /api/cmds) so the DELETE (stop) call still works
// once the active session is a non-staff student.
export async function POST(req: NextRequest) {
  const admin = await sessionFromReq(req);
  if (!admin || !isStaff(admin.profile))
    return NextResponse.json({ error: "Staff access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as Body;
  const id = String(b.memberId || "");
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid member id" }, { status: 400 });

  try {
    const db = await getDb();
    const target: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(id) });
    if (!target) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    const superAdmin = isSuperAdmin(admin.profile, admin.isSuperAdmin);
    if (!superAdmin && target.orgId !== admin.orgId)
      return NextResponse.json({ error: "That member belongs to another institute" }, { status: 403 });

    const targetSuper = target?.isSuperAdmin === true || target?.extraInfo?.isSuperAdmin === true;
    if (targetSuper && !superAdmin)
      return NextResponse.json({ error: "Cannot impersonate an administrator" }, { status: 403 });

    const profile = (target.profile || "STUDENT").toUpperCase();
    const result = {
      id: String(target._id),
      firstName: target.firstName || "",
      lastName: target.lastName || "",
      memberId: target.memberId || null,
      profile,
      isSuperAdmin: targetSuper,
      impersonated: true,
    };

    const res = NextResponse.json({ result });

    const currentToken = req.cookies.get(SESSION_COOKIE)?.value;
    if (currentToken) res.cookies.set(RETURN_COOKIE, currentToken, sessionCookieOptions());

    const token = await createSessionToken({
      id: String(target._id),
      orgId: target.orgId || DEFAULT_ORG_ID,
      memberId: target.memberId || null,
      firstName: target.firstName || "",
      lastName: target.lastName || "",
      profile,
      isSuperAdmin: targetSuper,
    });
    res.cookies.set(SESSION_COOKIE, token, sessionCookieOptions());
    return res;
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Impersonation failed" }, { status: 500 });
  }
}

// Stop impersonation: restore the admin's parked token.
export async function DELETE(req: NextRequest) {
  const returnToken = req.cookies.get(RETURN_COOKIE)?.value;
  if (!returnToken) return NextResponse.json({ error: "Not impersonating" }, { status: 400 });

  const res = NextResponse.json({ ok: true });
  res.cookies.set(SESSION_COOKIE, returnToken, sessionCookieOptions());
  res.cookies.set(RETURN_COOKIE, "", { ...sessionCookieOptions(), maxAge: 0 });
  return res;
}
