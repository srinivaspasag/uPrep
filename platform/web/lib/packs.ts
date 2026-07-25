import type { Db } from "mongodb";
import { ObjectId } from "mongodb";

// Named "course packs" — a bundle of courses a provider org defines once and
// shares/assigns as a single unit. This is the new-stack analogue of the legacy
// `OrgProgram` (a program bundling many `courseIds`): the super admin grants a
// PACK to a subscriber org (orgpackgrants), and the org admin assigns the whole
// pack to a student in one click. Pack membership is dynamic — adding a course
// to a pack automatically flows to every org/student that has the pack.
export const PACKS_COLL = "coursepacks";
export const PACK_GRANTS_COLL = "orgpackgrants";

export type CoursePack = {
  id: string;
  orgId: string; // owner/provider org
  name: string;
  courseIds: string[]; // top-level folder ids owned by orgId
};

function mapPack(p: any): CoursePack {
  return {
    id: String(p._id),
    orgId: String(p.orgId || ""),
    name: p.name || "Untitled pack",
    courseIds: Array.isArray(p.courseIds) ? p.courseIds.map(String) : [],
  };
}

// Packs owned by an org (the provider's own packs).
export async function listPacks(db: Db, orgId: string): Promise<CoursePack[]> {
  const docs = await db
    .collection(PACKS_COLL)
    .find({ orgId, recordState: "ACTIVE" } as any)
    .sort({ name: 1 })
    .toArray();
  return (docs as any[]).map(mapPack);
}

export async function getPackById(db: Db, packId: string): Promise<CoursePack | null> {
  if (!ObjectId.isValid(packId)) return null;
  const p: any = await db.collection(PACKS_COLL).findOne({ _id: new ObjectId(packId) });
  return p ? mapPack(p) : null;
}

// Pack ids granted TO an org (from any provider).
export async function getGrantedPackIds(db: Db, subscriberOrgId: string): Promise<string[]> {
  const docs = await db
    .collection(PACK_GRANTS_COLL)
    .find({ subscriberOrgId, recordState: "ACTIVE" } as any)
    .toArray();
  return Array.from(new Set((docs as any[]).map((d) => String(d.packId))));
}

// Full pack docs granted to an org (for the assign UI).
export async function getGrantedPacks(db: Db, subscriberOrgId: string): Promise<CoursePack[]> {
  const ids = await getGrantedPackIds(db, subscriberOrgId);
  const objIds = ids.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
  if (!objIds.length) return [];
  const docs = await db
    .collection(PACKS_COLL)
    .find({ _id: { $in: objIds }, recordState: "ACTIVE" } as any)
    .toArray();
  return (docs as any[]).map(mapPack);
}

// Every course id reachable via packs granted to an org — folded into the org's
// granted-course set so existing access/enrollment checks work unchanged.
export async function getGrantedPackCourseIds(db: Db, subscriberOrgId: string): Promise<string[]> {
  const packs = await getGrantedPacks(db, subscriberOrgId);
  const out = new Set<string>();
  for (const p of packs) for (const c of p.courseIds) out.add(c);
  return Array.from(out);
}

// Packs available to an org for assignment: its OWN packs plus granted packs.
export async function resolvePackCatalog(db: Db, orgId: string): Promise<(CoursePack & { granted: boolean })[]> {
  const own = (await listPacks(db, orgId)).map((p) => ({ ...p, granted: false }));
  const granted = (await getGrantedPacks(db, orgId))
    .filter((p) => p.orgId !== orgId)
    .map((p) => ({ ...p, granted: true }));
  return [...own, ...granted];
}
