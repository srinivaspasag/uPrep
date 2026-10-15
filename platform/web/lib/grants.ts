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

export type ProviderProgram = { id: string; name: string; courseIds: string[] };

// An org's own programs, each resolved to the union of courseIds across its
// own sections — a program itself has no courseIds; that lives on its
// sections. Used both by the org-grants "by program" bulk-select (provider
// side) and by the subscriber-side display grouping granted courses back to
// the program they came from (see resolveGrantedProgramGroups below).
export async function resolveProviderPrograms(db: Db, providerOrgId: string): Promise<ProviderProgram[]> {
  const [programDocs, sectionDocs, ownFolders] = await Promise.all([
    db.collection("orgprograms").find({ orgId: providerOrgId, recordState: "ACTIVE" } as any).project({ name: 1 }).toArray(),
    db
      .collection("orgsections")
      .find({ orgId: providerOrgId, recordState: "ACTIVE" } as any)
      .project({ programId: 1, courseIds: 1 })
      .toArray(),
    loadOrgFolders(db, providerOrgId),
  ]);
  const ownCourseIds = new Set(topLevelCourses(ownFolders).map((f) => f.id));
  const courseIdsByProgram = new Map<string, Set<string>>();
  for (const sec of sectionDocs as any[]) {
    const pid = sec.programId ? String(sec.programId) : null;
    if (!pid) continue;
    const set = courseIdsByProgram.get(pid) || new Set<string>();
    for (const cid of Array.isArray(sec.courseIds) ? sec.courseIds : []) set.add(String(cid));
    courseIdsByProgram.set(pid, set);
  }
  return (programDocs as any[]).map((p) => ({
    id: String(p._id),
    name: p.name || "(untitled)",
    courseIds: Array.from(courseIdsByProgram.get(String(p._id)) || []).filter((cid) => ownCourseIds.has(cid)),
  }));
}

// For a subscriber org's granted courses, which provider program(s) each one
// belongs to right now — purely a display grouping (see lib/grants.ts's own
// header note: the grant itself only ever stores courseId, no program
// reference, so this is resolved fresh from the provider's CURRENT program
// structure every time, not frozen at grant time). A course can legitimately
// belong to more than one program (e.g. Physics XI shared by NEET + JEE), and
// won't match any program if it was granted individually or its source
// program was since restructured — callers should treat an empty list as
// "Other / individually granted," not an error.
export async function resolveGrantedProgramGroups(
  db: Db,
  grantedCourseIds: string[],
  catalog: CatalogCourse[]
): Promise<Record<string, string[]>> {
  const ownerOrgIds = Array.from(
    new Set(
      grantedCourseIds
        .map((cid) => catalog.find((c) => c.id === cid)?.ownerOrgId)
        .filter((id): id is string => !!id)
    )
  );
  if (ownerOrgIds.length === 0) return {};
  const programsByOwner = await Promise.all(ownerOrgIds.map((oid) => resolveProviderPrograms(db, oid)));
  const programs = programsByOwner.flat();
  const out: Record<string, string[]> = {};
  for (const cid of grantedCourseIds) {
    out[cid] = programs.filter((p) => p.courseIds.includes(cid)).map((p) => p.name);
  }
  return out;
}
