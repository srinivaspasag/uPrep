import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveCourseCatalog } from "@/lib/grants";
import { resolvePackCatalog, getPackById } from "@/lib/packs";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Pack assignment (staff-only — gated by middleware on /api/cmds/**).
//
//   GET                     -> { packs:[{id,name,granted,courseIds,courseCount}] }
//   POST { memberId, packId } -> add all of the pack's (allowed) courses to a
//                               student's enrolledCourseIds (union, non-destructive)
export async function GET(req: NextRequest) {
  try {
    const db = await getDb();
    const session = await sessionFromReq(req);
    const orgOverride = req.nextUrl.searchParams.get("orgId");
    const orgId =
      session && isSuperAdmin(session.profile, session.isSuperAdmin) && orgOverride
        ? orgOverride
        : session?.orgId || orgOverride || DEFAULT_ORG_ID;

    const packs = (await resolvePackCatalog(db, orgId)).map((p) => ({
      id: p.id,
      name: p.name,
      granted: p.granted,
      courseIds: p.courseIds,
      courseCount: p.courseIds.length,
    }));
    return NextResponse.json({ packs, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type Body = { memberId?: string; packId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const memberId = String(b.memberId || "");
  const packId = String(b.packId || "");
  if (!ObjectId.isValid(memberId))
    return NextResponse.json({ error: "Valid memberId is required" }, { status: 400 });
  if (!ObjectId.isValid(packId))
    return NextResponse.json({ error: "Valid packId is required" }, { status: 400 });

  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    const orgId = member.orgId || DEFAULT_ORG_ID;
    const pack = await getPackById(db, packId);
    if (!pack) return NextResponse.json({ error: "Pack not found" }, { status: 404 });

    // The student may only receive courses inside their org's catalog
    // (own + granted, which already includes pack-granted courses).
    const catalog = await resolveCourseCatalog(db, orgId);
    const allowed = new Set(catalog.map((c) => c.id));
    const packCourses = pack.courseIds.filter((id) => allowed.has(id));

    const current: string[] = Array.isArray(member.enrolledCourseIds)
      ? member.enrolledCourseIds.map(String)
      : [];
    const merged = Array.from(new Set([...current, ...packCourses]));

    await db
      .collection("orgmembers")
      .updateOne(
        { _id: new ObjectId(memberId) },
        { $set: { enrolledCourseIds: merged, lastUpdated: Date.now() } }
      );
    return NextResponse.json({
      ok: true,
      added: packCourses.length,
      enrolledCourseIds: merged,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Assign failed" }, { status: 500 });
  }
}
