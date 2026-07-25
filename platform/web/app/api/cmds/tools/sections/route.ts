import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { SECTIONS_COLL, uniqueCode } from "@/lib/sections";
import { resolveCourseCatalog } from "@/lib/grants";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// GET: list an org's sections (+ its course catalog for the create form).
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(SECTIONS_COLL)
      .find({ orgId, recordState: "ACTIVE" } as any)
      .sort({ timeCreated: -1 })
      .toArray();

    const memberCounts = await db
      .collection("orgmembers")
      .aggregate([
        { $match: { orgId, recordState: "ACTIVE", "mappings.sectionId": { $exists: true } } },
        { $unwind: "$mappings" },
        { $group: { _id: "$mappings.sectionId", n: { $sum: 1 } } },
      ])
      .toArray();
    const counts = new Map(memberCounts.map((c: any) => [String(c._id), c.n]));

    const catalog = await resolveCourseCatalog(db, orgId);
    const sections = (docs as any[]).map((s) => ({
      id: String(s._id),
      name: s.name,
      code: s.code,
      courseIds: Array.isArray(s.courseIds) ? s.courseIds : [],
      memberCount: counts.get(String(s._id)) || 0,
    }));
    return NextResponse.json({
      sections,
      courses: catalog.map((c) => ({ id: c.id, name: c.name, granted: c.granted })),
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ sections: [], courses: [], error: e?.message }, { status: 500 });
  }
}

type CreateBody = { name?: string; courseIds?: string[]; orgId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  const orgId = await resolveOrgId(req, b.orgId);
  if (!name) return NextResponse.json({ error: "Section name is required" }, { status: 400 });

  try {
    const db = await getDb();
    // Only allow courses from this org's catalog (own + granted).
    const catalog = await resolveCourseCatalog(db, orgId);
    const allowed = new Set(catalog.map((c) => c.id));
    const courseIds = Array.from(
      new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String))
    ).filter((id) => allowed.has(id));

    const code = await uniqueCode(db, orgId);
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection(SECTIONS_COLL).insertOne({
      _id,
      orgId,
      name,
      code,
      courseIds,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name, code, courseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const res = await db
      .collection(SECTIONS_COLL)
      .updateOne(
        { _id: new ObjectId(id), orgId } as any,
        { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } }
      );
    if (!res.matchedCount)
      return NextResponse.json({ error: "Section not found in your institute" }, { status: 404 });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
