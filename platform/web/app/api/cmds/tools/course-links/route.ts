import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { planCourseLinks, applyCourseLinkPlans, type LinkPlan } from "@/lib/course-links";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Link Shared Subjects (SUPER ADMIN only — touches every subscriber org).
// For every active course grant, gives the subscriber ONE subject linked to
// the provider's, merging the subscriber's own same-named duplicates into it
// — see lib/course-links.ts.
//   GET  -> dry-run plan, changes nothing
//   POST -> re-plans server-side (never trusts a client list) and applies
async function requireSuperAdmin(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s || !isSuperAdmin(s.profile, s.isSuperAdmin)) return null;
  return s;
}

async function withOrgNames(plans: LinkPlan[]) {
  const db = await getDb();
  const ids = Array.from(new Set(plans.flatMap((p) => [p.subscriberOrgId, p.providerOrgId]))).filter(ObjectId.isValid);
  const docs = (await db
    .collection("organizations")
    .find({ _id: { $in: ids.map((id) => new ObjectId(id)) } })
    .project({ name: 1 })
    .toArray()) as any[];
  const names = new Map(docs.map((o) => [String(o._id), o.name || "(unnamed)"]));
  return plans.map((p) => ({
    ...p,
    subscriberOrgName: names.get(p.subscriberOrgId) || p.subscriberOrgId,
    providerOrgName: names.get(p.providerOrgId) || p.providerOrgId,
  }));
}

export async function GET(req: NextRequest) {
  if (!(await requireSuperAdmin(req)))
    return NextResponse.json({ error: "Super admin access required" }, { status: 403 });
  try {
    const db = await getDb();
    const subscriberOrgId = req.nextUrl.searchParams.get("subscriberOrgId") || undefined;
    const plans = await planCourseLinks(db, { subscriberOrgId, merge: true });
    return NextResponse.json({ plans: await withOrgNames(plans) });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Scan failed" }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  if (!(await requireSuperAdmin(req)))
    return NextResponse.json({ error: "Super admin access required" }, { status: 403 });
  try {
    const db = await getDb();
    const subscriberOrgId = req.nextUrl.searchParams.get("subscriberOrgId") || undefined;
    const plans = await planCourseLinks(db, { subscriberOrgId, merge: true });
    const result = await applyCourseLinkPlans(db, plans);
    return NextResponse.json({ ok: true, subjects: plans.length, ...result });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Apply failed" }, { status: 500 });
  }
}
