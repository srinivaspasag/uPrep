import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { loadFoldersForOrgs } from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Course enrollment (staff-only — gated by middleware on /api/cmds/**).
//
//   GET ?courses=1[&orgId]        -> { courses: [{id,name,chapterCount}] }
//   GET ?memberId=<orgmember _id> -> { enrolledCourseIds: string[] }
//   POST { memberId, courseIds }  -> replace a student's enrolled courses
export async function GET(req: NextRequest) {
  const wantsCourses = req.nextUrl.searchParams.get("courses");
  const memberId = req.nextUrl.searchParams.get("memberId");

  try {
    const db = await getDb();

    // Scope to the caller's own org (own + granted courses). A super admin may
    // override via ?orgId to manage another org; regular org admins are pinned
    // to their session org and can't peek at another org's catalog.
    const session = await sessionFromReq(req);
    const orgOverride = req.nextUrl.searchParams.get("orgId");
    const orgId =
      session && isSuperAdmin(session.profile, session.isSuperAdmin) && orgOverride
        ? orgOverride
        : session?.orgId || orgOverride || DEFAULT_ORG_ID;

    if (wantsCourses) {
      // Own courses + courses granted to this org by a provider org.
      const catalog = await resolveCourseCatalog(db, orgId);
      const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
      const courses = catalog.map((c) => ({
        id: c.id,
        name: c.name,
        granted: c.granted,
        chapterCount: folders.filter((f) => f.parentId === c.id).length,
      }));
      return NextResponse.json({ courses, orgId });
    }

    if (memberId) {
      if (!ObjectId.isValid(memberId))
        return NextResponse.json({ error: "Invalid memberId" }, { status: 400 });
      const m: any = await db
        .collection("orgmembers")
        .findOne({ _id: new ObjectId(memberId) });
      if (!m) return NextResponse.json({ error: "Member not found" }, { status: 404 });
      return NextResponse.json({
        enrolledCourseIds: Array.isArray(m.enrolledCourseIds) ? m.enrolledCourseIds : [],
      });
    }

    return NextResponse.json({ error: "Pass ?courses=1 or ?memberId=" }, { status: 400 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { memberId?: string; courseIds?: string[] };

// Security fix: this never checked the ACTING staff member's own org at
// all (only that the target student's requested courses were within that
// student's org catalog) — a staff session from Org A could enroll/modify
// a student in Org B, blocked only by the blanket /api/cmds staff gate.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as SaveBody;
  const memberId = String(b.memberId || "");
  if (!ObjectId.isValid(memberId))
    return NextResponse.json({ error: "Valid memberId is required" }, { status: 400 });

  // Keep only well-formed folder ids.
  const requested = Array.from(
    new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String).filter(ObjectId.isValid))
  );

  try {
    const db = await getDb();
    const member: any = await db
      .collection("orgmembers")
      .findOne({ _id: new ObjectId(memberId) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });
    if (!isSuperAdmin(session.profile, session.isSuperAdmin) && member.orgId !== session.orgId)
      return NextResponse.json({ error: "That member belongs to another institute" }, { status: 403 });

    // A student can only be enrolled in courses in THEIR org's catalog
    // (own + granted). Silently drop anything outside it.
    const catalog = await resolveCourseCatalog(db, member.orgId || DEFAULT_ORG_ID);
    const allowed = new Set(catalog.map((c) => c.id));
    const courseIds = requested.filter((id) => allowed.has(id));

    await db
      .collection("orgmembers")
      .updateOne(
        { _id: new ObjectId(memberId) },
        { $set: { enrolledCourseIds: courseIds, lastUpdated: Date.now() } }
      );
    return NextResponse.json({ ok: true, enrolledCourseIds: courseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}
