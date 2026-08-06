import type { Db } from "mongodb";
import { ObjectId } from "mongodb";
import { loadOrgFolders, topLevelCourses } from "@/lib/courses";

// Cross-org course sharing — the new-stack analogue of the legacy
// `granteeorgprograms` collection (GranteeOrgProgram: provider → subscriber →
// program). Here the shared unit is a *course* (a top-level content folder).
// A super admin grants a course owned by a provider org to a subscriber org;
// that org's admin can then assign it to their students, and students can view
// it — even though the content's contentSrc.id stays the provider org.
export const GRANTS_COLL = "orgcoursegrants";

export type CourseGrant = {
  providerOrgId: string;
  subscriberOrgId: string;
  courseId: string; // top-level folder id owned by providerOrgId
};

// Course ids granted TO an org (from any provider) — orgcoursegrants only.
// Course Packs (a bundled-grant mechanism layered on top of this) has been
// removed as a duplicate of Academic Structure's own Program/Section course
// assignment; this no longer folds in pack grants.
export async function getGrantedCourseIds(
  db: Db,
  subscriberOrgId: string
): Promise<string[]> {
  const docs = await db
    .collection(GRANTS_COLL)
    .find({ subscriberOrgId, recordState: "ACTIVE" } as any)
    .toArray();
  return Array.from(new Set((docs as any[]).map((d) => String(d.courseId))));
}

export type CatalogCourse = {
  id: string;
  name: string;
  ownerOrgId: string | null;
  granted: boolean;
};

// The full course catalog available to an org: its OWN top-level courses plus
// any courses granted to it by other orgs. Used both by the org admin (to pick
// what to assign) and by the enrollment/access checks.
export async function resolveCourseCatalog(
  db: Db,
  orgId: string
): Promise<CatalogCourse[]> {
  const ownFolders = await loadOrgFolders(db, orgId);
  const own: CatalogCourse[] = topLevelCourses(ownFolders).map((f) => ({
    id: f.id,
    name: f.name,
    ownerOrgId: f.ownerOrgId,
    granted: false,
  }));

  const grantedIds = await getGrantedCourseIds(db, orgId);
  let granted: CatalogCourse[] = [];
  if (grantedIds.length) {
    const objIds = grantedIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
    const gdocs = await db
      .collection("folders")
      .find({ _id: { $in: objIds }, recordState: "ACTIVE" } as any)
      .toArray();
    granted = (gdocs as any[]).map((f) => ({
      id: String(f._id),
      name: f.name || "Folder",
      ownerOrgId: f?.contentSrc?.id || null,
      granted: true,
    }));
  }

  // Own courses first, then granted (dedupe by id in case of overlap).
  const seen = new Set(own.map((c) => c.id));
  return [...own, ...granted.filter((c) => !seen.has(c.id))];
}

// Owner orgs whose folder trees back an org's catalog — the set to load folders
// for when resolving subtrees (own org + every provider org that granted a course).
export function catalogOwnerOrgs(orgId: string, catalog: CatalogCourse[]): string[] {
  return Array.from(new Set([orgId, ...catalog.map((c) => c.ownerOrgId || orgId)]));
}
