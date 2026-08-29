import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { resolveAdminProgramScope } from "@/lib/enrollment";

export const dynamic = "force-dynamic";

// List programs (with per-program section counts) and create new ones.
//
// Program-scoped admins: an admin with a Program assigned to them (see
// People Management's Program/Center/Section picker, now available for any
// staff profile) only sees that program here — everyone else (no
// assignment, or a super admin) sees the whole org's programs, unchanged.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const session = await sessionFromReq(req);
    const scopeIds = session
      ? await resolveAdminProgramScope(db, session.id, !!session.isSuperAdmin)
      : null;

    const filter: Record<string, unknown> = { orgId, recordState: "ACTIVE" };
    if (scopeIds) filter._id = { $in: scopeIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id)) };

    const docs = await db
      .collection("orgprograms")
      .find(filter)
      .sort({ lastUpdated: -1 })
      .toArray();
    const sections = await db.collection("orgsections").find({ orgId, recordState: "ACTIVE" }).toArray();

    const programs = (docs as any[]).map((d) => ({
      id: String(d._id),
      name: d.name || d.cName || "(untitled)",
      code: d.code || null,
      description: d.description || "",
      isOffline: !!d.isOffline,
      sectionCount: (sections as any[]).filter((s) => s.programId === String(d._id)).length,
    }));
    return NextResponse.json({ programs, orgId });
  } catch (e: any) {
    return NextResponse.json({ programs: [], error: e?.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as {
    name?: string;
    description?: string;
    departmentId?: string;
    orgId?: string;
  };
  const name = (b.name || "").trim();
  const orgId = await resolveOrgId(req, b.orgId);
  if (!name) return NextResponse.json({ error: "Program name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("orgprograms").insertOne({
      _id,
      orgId,
      name,
      cName: name.toLowerCase(),
      code: name.replace(/\s+/g, "_").toUpperCase().slice(0, 20),
      description: (b.description || "").trim(),
      departmentId: b.departmentId || null,
      centersSections: [],
      isOffline: false,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}
