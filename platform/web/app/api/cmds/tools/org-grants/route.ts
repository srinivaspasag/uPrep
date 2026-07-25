import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { loadOrgFolders, topLevelCourses } from "@/lib/courses";
import { GRANTS_COLL, getGrantedCourseIds } from "@/lib/grants";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Course sharing (SUPER ADMIN only) — grant courses owned by the provider org
// (the super admin's current org) to a subscriber org. Mirrors the legacy
// "share program to org" flow (granteeorgprograms).
//
//   GET  ?subscriberOrgId=<id> -> { courses:[{id,name}], grantedCourseIds:[] }
//   POST { subscriberOrgId, courseIds } -> replace the grant set for that org
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
    return NextResponse.json({ providerOrgId: s.orgId, courses, grantedCourseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { subscriberOrgId?: string; courseIds?: string[] };

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
          recordState: "ACTIVE",
          timeCreated: now,
          lastUpdated: now,
        }))
      );
    }
    return NextResponse.json({ ok: true, grantedCourseIds: courseIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}
