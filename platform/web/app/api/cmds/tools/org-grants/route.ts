import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { loadOrgFolders, topLevelCourses } from "@/lib/courses";
import { GRANTS_COLL, getGrantedCourseIds, resolveProviderPrograms } from "@/lib/grants";
import { planCourseLinks, applyCourseLinkPlans } from "@/lib/course-links";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Course sharing (SUPER ADMIN only) — grant courses owned by the provider org
// (the super admin's current org) to a subscriber org. Mirrors the legacy
// "share program to org" flow (granteeorgprograms).
//
//   GET  ?subscriberOrgId=<id> -> { courses:[{id,name}], grantedCourseIds:[], programs:[{id,name,courseIds}] }
//   POST { subscriberOrgId, courseIds, grantedProgramIds } -> replace the grant set for that org
async function requireSuperAdmin(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s || !isSuperAdmin(s.profile, s.isSuperAdmin)) return null;
  return s;
}

export async function GET(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const subscriberOrgId = req.nextUrl.searchParams.get("subscriberOrgId") || "";
  if (!subscriberOrgId)
    return NextResponse.json({ error: "subscriberOrgId is required" }, { status: 400 });

  try {
    const db = await getDb();
    // Grantable courses = the provider org's own top-level courses.
    const folders = await loadOrgFolders(db, s.orgId);
    const courses = topLevelCourses(folders).map((f) => ({ id: f.id, name: f.name }));
    const grantedCourseIds = await getGrantedCourseIds(db, subscriberOrgId);

    // Provider org's programs, each resolved to the union of courseIds across
    // its own sections — lets the UI offer "grant everything in this
    // program" as one click instead of checking each course individually
    // (a program itself has no courseIds of its own; that lives on its
    // sections — see lib/grants.ts's resolveProviderPrograms, shared with the
    // subscriber-side grouping display in /api/cmds/enroll).
    const programs = (await resolveProviderPrograms(db, s.orgId)).filter((p) => p.courseIds.length > 0);

    return NextResponse.json({ providerOrgId: s.orgId, courses, grantedCourseIds, programs });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { subscriberOrgId?: string; courseIds?: string[]; grantedProgramIds?: string[] };

export async function POST(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as SaveBody;
  const subscriberOrgId = String(b.subscriberOrgId || "");
  if (!subscriberOrgId)
    return NextResponse.json({ error: "subscriberOrgId is required" }, { status: 400 });
  if (subscriberOrgId === s.orgId)
    return NextResponse.json({ error: "An org already owns its own courses" }, { status: 400 });

  try {
    const db = await getDb();
    // Only allow granting courses the provider org actually owns.
    const folders = await loadOrgFolders(db, s.orgId);
    const ownIds = new Set(topLevelCourses(folders).map((f) => f.id));
    const courseIds = Array.from(
      new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String))
    ).filter((id) => ownIds.has(id));

    // Which program(s) each course is attributed to — FROZEN at grant time
    // from the programs the admin had fully checked via "Select all courses"
    // when they clicked Save, not re-derived later. Re-deriving against every
    // CURRENT provider program that happens to reuse the same subject folder
    // was the original (buggy) design — found live: granting "JEE Online" +
    // "NEET Online" showed up grouped under a dozen unrelated programs too
    // (Demo Class XI & XII, JEE Main 2026, JEE XI, ...) because they all
    // happen to share the same Physics/Chemistry/... course folders. A course
    // checked individually (not via a program button) gets no program name —
    // shown as "Other courses" by the subscriber-side display.
    const grantedProgramIds = Array.from(
      new Set((Array.isArray(b.grantedProgramIds) ? b.grantedProgramIds : []).map(String))
    );
    const relevantPrograms = grantedProgramIds.length
      ? (await resolveProviderPrograms(db, s.orgId)).filter((p) => grantedProgramIds.includes(p.id))
      : [];
    const programNamesByCourseId = new Map<string, string[]>();
    for (const cid of courseIds) {
      programNamesByCourseId.set(
        cid,
        relevantPrograms.filter((p) => p.courseIds.includes(cid)).map((p) => p.name)
      );
    }

    // Replace this provider's grants to this subscriber.
    await db
      .collection(GRANTS_COLL)
      .deleteMany({ providerOrgId: s.orgId, subscriberOrgId } as any);
    if (courseIds.length) {
      const now = Date.now();
      await db.collection(GRANTS_COLL).insertMany(
        courseIds.map((courseId) => ({
          providerOrgId: s.orgId,
          subscriberOrgId,
          courseId,
          programNames: programNamesByCourseId.get(courseId) || [],
          recordState: "ACTIVE",
          timeCreated: now,
          lastUpdated: now,
        }))
      );
      // Give the subscriber ONE linked "Chemistry XI" of its own for each
      // granted subject (reusing a same-named folder it already made, if any)
      // instead of a separate read-only "Shared" copy — see lib/course-links.ts.
      // No merging of duplicates here; that's the reviewed Link Shared
      // Subjects tool's job.
      await applyCourseLinkPlans(db, await planCourseLinks(db, { subscriberOrgId, courseIds, merge: false }));
    }
    return NextResponse.json({ ok: true, grantedCourseIds: courseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}
