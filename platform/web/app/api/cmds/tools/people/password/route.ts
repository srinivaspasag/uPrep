import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { hashPassword, generatePassword } from "@/lib/password";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { id?: string; newPassword?: string };

// Admin-initiated password reset for a member. Staff-only (middleware-gated to
// /api/cmds). An org admin may only reset members inside their own org; a super
// admin may reset anyone. Returns the new password once so it can be shared.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const id = String(b.id || "");
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid member id" }, { status: 400 });

  const custom = (b.newPassword || "").trim();
  if (custom && custom.length < 6)
    return NextResponse.json({ error: "Password must be at least 6 characters" }, { status: 400 });

  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(id) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    const session = await sessionFromReq(req);
    const superAdmin = !!session && isSuperAdmin(session.profile, session.isSuperAdmin);
    const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
    if (!superAdmin && member.orgId !== orgId)
      return NextResponse.json({ error: "That member belongs to another institute" }, { status: 403 });

    const password = custom || generatePassword();
    await db
      .collection("orgmembers")
      .updateOne(
        { _id: member._id },
        { $set: { passwordHash: hashPassword(password), authType: "LOCAL", lastUpdated: Date.now() } }
      );

    return NextResponse.json({
      ok: true,
      password,
      memberId: member.memberId || "",
      loginId: `${member.orgId}:${member.memberId || ""}`,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Reset failed" }, { status: 500 });
  }
}
