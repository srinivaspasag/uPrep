import type { Db } from "mongodb";

// Course model for the new stack: a "course" is a top-level content folder
// (parentId null) in an org's library — e.g. "Physics XI". Enrolling a student
// in a course grants access to that folder and everything nested under it
// (chapters → sessions → videos). Enrollment is stored on the student's
// orgmembers doc as `enrolledCourseIds: string[]` (a simplified analogue of the
// legacy `orgmembers.mappings`).

export type FolderNode = {
  id: string;
  name: string;
  parentId: string | null;
  ownerOrgId: string | null;
  order: number | null;
  // Set on a subscriber org's own top-level subject that is a live link to a
  // provider org's subject (see "Linked subjects" below) — the provider
  // folder id it mirrors. Only honoured while the grant is still active.
  linkedCourseId?: string | null;
};

function mapFolder(f: any): FolderNode {
  return {
    id: String(f._id),
    name: f.name || "Folder",
    parentId: f.parentId || null,
    ownerOrgId: f?.contentSrc?.id || null,
    order: typeof f.order === "number" ? f.order : null,
    linkedCourseId: f.linkedCourseId ? String(f.linkedCourseId) : null,
  };
}

// Linked subjects. Sharing a provider subject (e.g. master "Chemistry XI")
// with another org creates ONE "Chemistry XI" folder in that org carrying
// `linkedCourseId` = the provider folder — instead of the subscriber seeing a
// separate read-only "Shared" copy next to its own same-named, empty
// folders (found live: Scholar Spectra had 4x "Chemistry XI" + 1 Shared, and
// students saw the empty local ones). The link is live: in any folder set
// that contains both ends, the provider subject's direct children are
// re-parented onto the subscriber's folder, so chapter listing, courseRootOf
// and subtree walks all treat the provider chapters as the subscriber
// subject's own (read-only — writes are still ownership-checked per doc).
// Content filed directly in the provider subject root is picked up by
// collectSubtreeIds, which includes `linkedCourseId` alongside the link.
//
// A link only counts while an ACTIVE grant (subscriber, provider subject)
// exists, so revoking the grant cuts off access even though the folder
// itself (and anything the subscriber filed in it) stays.
export async function applyCourseLinks(db: Db, folders: FolderNode[]): Promise<FolderNode[]> {
  const linked = folders.filter((f) => f.linkedCourseId && !f.parentId);
  if (linked.length === 0) return folders;
  const grants = await db
    .collection("orgcoursegrants")
    .find({
      recordState: "ACTIVE",
      courseId: { $in: linked.map((f) => f.linkedCourseId!) },
      subscriberOrgId: { $in: Array.from(new Set(linked.map((f) => f.ownerOrgId).filter(Boolean))) },
    } as any)
    .project({ courseId: 1, subscriberOrgId: 1 })
    .toArray();
  const active = new Set((grants as any[]).map((g) => `${g.subscriberOrgId}:${g.courseId}`));
  const present = new Set(folders.map((f) => f.id));
  const aliasOf = new Map<string, string>(); // provider subject id -> subscriber folder id
  for (const f of linked) {
    if (!present.has(f.linkedCourseId!) || aliasOf.has(f.linkedCourseId!)) continue;
    if (active.has(`${f.ownerOrgId}:${f.linkedCourseId}`)) aliasOf.set(f.linkedCourseId!, f.id);
  }
  return folders.map((f) => {
    if (f.linkedCourseId && !f.parentId && aliasOf.get(f.linkedCourseId) !== f.id)
      return { ...f, linkedCourseId: null }; // grant gone: plain local folder again
    if (f.parentId && aliasOf.has(f.parentId)) return { ...f, parentId: aliasOf.get(f.parentId)! };
    return f;
  });
}

// Plain alphabetical sort breaks on real chapter/session names — e.g.
// "SESSION 10 11 & 12" sorts before "SESSION 2" because "1" < "2" as the
// first character. Compares run-of-digits chunks numerically and the rest
// alphabetically, so "SESSION 2" < "SESSION 3 4 & 5" < "SESSION 10 11 & 12".
export function naturalCompare(a: string, b: string): number {
  const chunks = /(\d+)|(\D+)/g;
  const aParts = a.match(chunks) || [];
  const bParts = b.match(chunks) || [];
  const len = Math.max(aParts.length, bParts.length);
  for (let i = 0; i < len; i++) {
    const ap = aParts[i] ?? "";
    const bp = bParts[i] ?? "";
    if (ap === bp) continue;
    const an = Number(ap);
    const bn = Number(bp);
    if (ap !== "" && bp !== "" && !isNaN(an) && !isNaN(bn) && an !== bn) return an - bn;
    return ap.localeCompare(bp);
  }
  return 0;
}

// Explicit manual `order` wins when set (lets an admin fix chapters that have
// no numbering at all, e.g. "Essential Mathematics for Physics" vs
// "Kinematics") — unordered items fall back to natural sort and always sort
// after any explicitly-ordered ones.
export function orderThenNatural<T extends { name: string; order?: number | null }>(items: T[]): T[] {
  return [...items].sort((a, b) => {
    const ao = typeof a.order === "number" ? a.order : null;
    const bo = typeof b.order === "number" ? b.order : null;
    if (ao !== null && bo !== null) return ao - bo;
    if (ao !== null) return -1;
    if (bo !== null) return 1;
    return naturalCompare(a.name, b.name);
  });
}

// Loads every ACTIVE folder for an org in one query so callers can walk the
// tree in memory (avoids N recursive DB round-trips).
export async function loadOrgFolders(
  db: Db,
  orgId: string
): Promise<FolderNode[]> {
  const docs = await db
    .collection("folders")
    .find({ "contentSrc.id": orgId, recordState: "ACTIVE" } as any)
    .toArray();
  // Provider folders aren't in this set, so every link resolves to "not
  // present" and is cleared — a single-org walk never reaches provider content.
  return applyCourseLinks(db, (docs as any[]).map(mapFolder));
}

// Raw `linkedCourseId` of each of an org's top-level folders, unvalidated —
// for the catalog, which checks it against the org's active grants itself.
export async function loadOrgCourseLinks(db: Db, orgId: string): Promise<Map<string, string>> {
  const docs = await db
    .collection("folders")
    .find({ "contentSrc.id": orgId, recordState: "ACTIVE", linkedCourseId: { $exists: true, $nin: [null, ""] } } as any)
    .project({ linkedCourseId: 1, parentId: 1 })
    .toArray();
  const out = new Map<string, string>();
  for (const d of docs as any[]) if (!d.parentId) out.set(String(d._id), String(d.linkedCourseId));
  return out;
}

// Loads ACTIVE folders across several owner orgs at once — needed when a course
// is granted to an org by another (provider) org, so its subtree lives under a
// different contentSrc.id than the viewing org.
export async function loadFoldersForOrgs(
  db: Db,
  orgIds: string[]
): Promise<FolderNode[]> {
  const uniq = Array.from(new Set(orgIds.filter(Boolean)));
  if (uniq.length === 0) return [];
  const docs = await db
    .collection("folders")
    .find({ "contentSrc.id": { $in: uniq }, recordState: "ACTIVE" } as any)
    .toArray();
  return applyCourseLinks(db, (docs as any[]).map(mapFolder));
}

// Top-level folders = the course catalog.
export function topLevelCourses(folders: FolderNode[]): FolderNode[] {
  return orderThenNatural(folders.filter((f) => !f.parentId));
}

// All folder ids in the subtree(s) rooted at `rootIds` (roots included).
export function collectSubtreeIds(
  folders: FolderNode[],
  rootIds: string[]
): Set<string> {
  const byId = new Map(folders.map((f) => [f.id, f]));
  const childrenOf = new Map<string, FolderNode[]>();
  for (const f of folders) {
    if (!f.parentId) continue;
    if (!childrenOf.has(f.parentId)) childrenOf.set(f.parentId, []);
    childrenOf.get(f.parentId)!.push(f);
  }
  const out = new Set<string>();
  const stack = [...rootIds];
  while (stack.length) {
    const id = stack.pop()!;
    if (out.has(id)) continue;
    out.add(id);
    // A linked subject also owns whatever is filed directly in the provider
    // subject root (its child folders are already re-parented onto it).
    const linkedTo = byId.get(id)?.linkedCourseId;
    if (linkedTo) out.add(linkedTo);
    for (const c of childrenOf.get(id) || []) stack.push(c.id);
  }
  return out;
}

// Walks a folder up to its top-level ancestor (the course root it belongs to).
export function courseRootOf(
  folders: FolderNode[],
  folderId: string
): string | null {
  const byId = new Map(folders.map((f) => [f.id, f]));
  let cur = byId.get(folderId);
  if (!cur) return null;
  while (cur.parentId && byId.has(cur.parentId)) {
    cur = byId.get(cur.parentId)!;
  }
  return cur.id;
}
