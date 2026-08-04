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
};

function mapFolder(f: any): FolderNode {
  return {
    id: String(f._id),
    name: f.name || "Folder",
    parentId: f.parentId || null,
    ownerOrgId: f?.contentSrc?.id || null,
    order: typeof f.order === "number" ? f.order : null,
  };
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
  return (docs as any[]).map(mapFolder);
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
  return (docs as any[]).map(mapFolder);
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
