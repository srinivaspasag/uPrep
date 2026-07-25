import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { loadOrgFolders, topLevelCourses } from "@/lib/courses";
import { PACKS_COLL, listPacks } from "@/lib/packs";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Course Packs — named bundles of courses a provider org defines and later
// shares/assigns as one unit (new-stack analogue of the legacy OrgProgram).
// SUPER ADMIN only; packs are owned by the super admin's current org.
async function requireSuperAdmin(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s || !isSuperAdmin(s.profile, s.isSuperAdmin)) return null;
  return s;
}

// GET -> { packs:[{id,name,courseIds,courseCount}], courses:[{id,name}] }
export async function GET(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });
  try {
    const db = await getDb();
    const folders = await loadOrgFolders(db, s.orgId);
    const courses = topLevelCourses(folders).map((f) => ({ id: f.id, name: f.name }));
    const nameById = new Map(courses.map((c) => [c.id, c.name]));
    const packs = (await listPacks(db, s.orgId)).map((p) => ({
      ...p,
      courseCount: p.courseIds.length,
      courseNames: p.courseIds.map((id) => nameById.get(id) || "(removed course)"),
    }));
    return NextResponse.json({ packs, courses });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { id?: string; name?: string; courseIds?: string[] };

// POST -> create a pack; if body.id is present, update it (name + courseIds).
export async function POST(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as SaveBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Pack name is required" }, { status: 400 });

  try {
    const db = await getDb();
    // Only allow courses the provider org actually owns.
    const folders = await loadOrgFolders(db, s.orgId);
    const ownIds = new Set(topLevelCourses(folders).map((f) => f.id));
    const courseIds = Array.from(
      new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String))
    ).filter((id) => ownIds.has(id));

    const now = Date.now();
    if (b.id) {
      if (!ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid pack id" }, { status: 400 });
      const res = await db
        .collection(PACKS_COLL)
        .updateOne(
          { _id: new ObjectId(b.id), orgId: s.orgId },
          { $set: { name, courseIds, lastUpdated: now } }
        );
      if (!res.matchedCount) return NextResponse.json({ error: "Pack not found" }, { status: 404 });
      return NextResponse.json({ ok: true, id: b.id, name, courseIds });
    }

    const _id = new ObjectId();
    await db.collection(PACKS_COLL).insertOne({
      _id,
      orgId: s.orgId,
      name,
      courseIds,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ ok: true, id: _id.toHexString(), name, courseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}

// DELETE ?id= -> soft-delete a pack (existing grants stop resolving to it).
export async function DELETE(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid pack id" }, { status: 400 });
  try {
    const db = await getDb();
    const res = await db
      .collection(PACKS_COLL)
      .updateOne(
        { _id: new ObjectId(id), orgId: s.orgId },
        { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } }
      );
    if (!res.matchedCount) return NextResponse.json({ error: "Pack not found" }, { status: 404 });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
