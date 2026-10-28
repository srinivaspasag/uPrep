import type { Db } from "mongodb";
import { collectSubtreeIds, type FolderNode } from "@/lib/courses";
import { resolveBoardNames } from "@/lib/legacyBoard";

// Tagged-only content. An institute can't file its own content into a shared
// (provider) chapter — those are read-only and shared with every other org
// (see app/api/cmds/upload) — so a book made for "Chemistry XI › Atomic
// Structure" is uploaded at the CMDS root, board-tagged with that subject and
// chapter, and published to a section. Found live: such a Spectra e-book,
// published to Scholar Repeater JEE, reached its students only as a loose
// "Shared with your class" card — never under the program's Chemistry XI,
// whose card kept showing Books 0.
//
// So it is PLACED at read time instead of filed: under the student's subject
// with the same name as its tagged subject, inside the chapter whose name
// matches a tagged chapter (else at the subject itself). Only the org's own
// content, and only for students it is explicitly published to (section /
// program / per-student grant) — staff preview sees whatever is published.

export const CONTENT_COLLS = ["videos", "documents", "books", "tests"] as const;

export type PlacedItem = { coll: string; doc: any; folderId: string; rootId: string };

// "1.Some Basic Concepts" / "Ch 2 - Atomic Structure" → "somebasicconcepts" /
// "atomicstructure" — folder chapters are numbered, board chapters aren't.
function norm(s: unknown): string {
  return String(s || "")
    .toLowerCase()
    .replace(/^\s*(chapter|ch)\b\.?/, "")
    .replace(/^[\s\d.\-–:)]+/, "")
    .replace(/[^a-z0-9]+/g, "");
}
function sameName(a: string, b: string): boolean {
  if (!a || !b) return false;
  if (a === b) return true;
  return Math.min(a.length, b.length) >= 6 && (a.includes(b) || b.includes(a));
}

// Grant clause for a student: hidden-ness plus an EXPLICIT publish to them —
// unlike content inside a subject folder, unscoped own content (no
// visibleProgramIds) does not count, or every untouched upload at the CMDS
// root would show up in every subject with the same name.
export function placedGrantFor(userId: string, programIds: string[], sectionIds: string[]) {
  return {
    hidden: { $ne: true },
    hiddenUserIds: { $nin: [userId] },
    $or: [
      { visibleUserIds: { $in: [userId] } },
      { visibleSectionIds: { $in: sectionIds } },
      { visibleProgramIds: { $in: programIds } },
    ],
  };
}

// Staff preview: anything published to someone, not drafts still being set up.
export const PLACED_PUBLISHED = {
  $or: [
    { "visibleSectionIds.0": { $exists: true } },
    { "visibleProgramIds.0": { $exists: true } },
    { "visibleUserIds.0": { $exists: true } },
  ],
};

const namesKey = (ids: string[]) => ids.join(",");

// Board ids → names, cached on the doc (boardNames + boardNamesKey, so a
// re-tag refreshes it). The board service is owner-scoped, so each owner org
// in the catalog is asked for whatever is still unresolved.
async function ensureBoardNames(db: Db, placed: PlacedItem[], ownerOrgIds: string[]) {
  const stale = placed.filter((p) => {
    const ids: string[] = Array.isArray(p.doc.boardIds) ? p.doc.boardIds.map(String) : [];
    return ids.length > 0 && p.doc.boardNamesKey !== namesKey(ids);
  });
  if (!stale.length) return;
  const wanted = new Set(stale.flatMap((p) => p.doc.boardIds.map(String)));
  const names: Record<string, string> = {};
  for (const owner of ownerOrgIds) {
    const missing = Array.from(wanted).filter((id) => !names[id]);
    if (!missing.length) break;
    Object.assign(names, await resolveBoardNames(owner, missing));
  }
  for (const p of stale) {
    const ids: string[] = p.doc.boardIds.map(String);
    const mine: Record<string, string> = {};
    for (const id of ids) if (names[id]) mine[id] = names[id];
    // Nothing resolved (board service down) → leave uncached, retry next time.
    if (!Object.keys(mine).length) continue;
    p.doc.boardNames = mine;
    await db
      .collection(p.coll)
      .updateOne({ _id: p.doc._id }, { $set: { boardNames: mine, boardNamesKey: namesKey(ids) } });
  }
}

export async function loadPlacedContent(
  db: Db,
  opts: { orgId: string; folders: FolderNode[]; rootIds: string[]; grant: Record<string, unknown>; ownerOrgIds: string[] }
): Promise<PlacedItem[]> {
  const roots = opts.rootIds
    .map((id) => opts.folders.find((f) => f.id === id))
    .filter((f): f is FolderNode => !!f);
  if (!roots.length) return [];
  const inTree = collectSubtreeIds(opts.folders, opts.rootIds);

  const placed: PlacedItem[] = [];
  for (const coll of CONTENT_COLLS) {
    const docs = await db
      .collection(coll)
      .find({ "contentSrc.id": opts.orgId, recordState: "ACTIVE", subject: { $nin: [null, ""] }, ...opts.grant } as any)
      .limit(500)
      .toArray();
    for (const d of docs as any[]) {
      // Already filed inside a subject — the folder tree shows it as is.
      if (d.folderId && inTree.has(String(d.folderId))) continue;
      const root = roots.find((r) => norm(r.name) === norm(d.subject));
      if (root) placed.push({ coll, doc: d, folderId: root.id, rootId: root.id });
    }
  }
  if (!placed.length) return placed;

  await ensureBoardNames(db, placed, opts.ownerOrgIds);
  for (const p of placed) {
    const tagged = Object.values((p.doc.boardNames || {}) as Record<string, string>).map(norm);
    const chapter = opts.folders.find(
      (f) => f.parentId === p.rootId && tagged.some((t) => sameName(norm(f.name), t))
    );
    if (chapter) p.folderId = chapter.id;
  }
  return placed;
}
