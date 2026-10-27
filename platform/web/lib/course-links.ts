import type { Db } from "mongodb";
import { ObjectId } from "mongodb";
import { loadOrgFolders, collectSubtreeIds } from "@/lib/courses";
import { GRANTS_COLL } from "@/lib/grants";
import { PRODUCTS_COLL } from "@/lib/commerce";

// Turns each granted provider subject into ONE linked subject in the
// subscriber org (see lib/courses.ts "Linked subjects"), and optionally folds
// the subscriber's own same-named duplicates into it.
//
// Why merge: before links existed, a subscriber that wanted a granted subject
// in its own programs created a fresh, empty folder with the same name — once
// per program. Found live: Scholar Spectra had 4x "Chemistry XI" of its own
// next to the master's shared one, its sections pointed at the empty copies,
// and students saw 0 chapters. Merging keeps the best of those folders,
// links it to the master subject, moves anything filed in the others into it,
// repoints every program/section/student/product reference (including ones
// that pointed straight at the master subject) and archives the rest.

const CONTENT_COLLS = ["videos", "documents", "books", "tests", "modules", "questionsets"];

export type LinkPlan = {
  subscriberOrgId: string;
  providerOrgId: string;
  providerCourseId: string;
  name: string;
  // null = no usable folder in the subscriber org; one will be created.
  keeper: { id: string; name: string; alreadyLinked: boolean; itemCount: number } | null;
  duplicates: { id: string; name: string; itemCount: number; chapterCount: number }[];
  refs: { programs: number; sections: number; members: number; products: number };
};

const norm = (s: string) => s.trim().replace(/\s+/g, " ").toLowerCase();

async function countItems(db: Db, folderIds: string[]): Promise<number> {
  const counts: number[] = await Promise.all(
    CONTENT_COLLS.map(async (c) =>
      Number(await db.collection(c).countDocuments({ folderId: { $in: folderIds }, recordState: "ACTIVE" } as any))
    )
  );
  return counts.reduce((a, b) => a + b, 0) + Math.max(0, folderIds.length - 1);
}

// Plans the link for every ACTIVE grant (optionally just one subscriber org,
// or just some provider subjects). Read-only.
export async function planCourseLinks(
  db: Db,
  opts: { subscriberOrgId?: string; courseIds?: string[]; merge: boolean }
): Promise<LinkPlan[]> {
  const grantQuery: any = { recordState: "ACTIVE" };
  if (opts.subscriberOrgId) grantQuery.subscriberOrgId = opts.subscriberOrgId;
  if (opts.courseIds) grantQuery.courseId = { $in: opts.courseIds };
  const grants = (await db.collection(GRANTS_COLL).find(grantQuery).toArray()) as any[];
  if (grants.length === 0) return [];

  const providerIds = Array.from(new Set(grants.map((g) => String(g.courseId)))).filter(ObjectId.isValid);
  const providerDocs = (await db
    .collection("folders")
    .find({ _id: { $in: providerIds.map((id) => new ObjectId(id)) }, recordState: "ACTIVE" } as any)
    .toArray()) as any[];
  const providerById = new Map(providerDocs.map((f) => [String(f._id), f]));

  const plans: LinkPlan[] = [];
  const bySubscriber = new Map<string, any[]>();
  for (const g of grants) {
    const list = bySubscriber.get(String(g.subscriberOrgId)) || [];
    list.push(g);
    bySubscriber.set(String(g.subscriberOrgId), list);
  }

  for (const [subscriberOrgId, subGrants] of Array.from(bySubscriber.entries())) {
    const folders = await loadOrgFolders(db, subscriberOrgId);
    const topDocs = (await db
      .collection("folders")
      .find({
        "contentSrc.id": subscriberOrgId,
        recordState: "ACTIVE",
        $or: [{ parentId: null }, { parentId: { $exists: false } }],
      } as any)
      .toArray()) as any[];
    const claimed = new Set<string>(); // a folder can back only one grant

    for (const g of subGrants) {
      const provider = providerById.get(String(g.courseId));
      if (!provider) continue; // provider subject deleted — nothing to link
      const providerCourseId = String(provider._id);
      const name = provider.name || "Folder";
      const candidates = topDocs.filter(
        (f) =>
          !claimed.has(String(f._id)) &&
          (String(f.linkedCourseId || "") === providerCourseId ||
            (!f.linkedCourseId && norm(f.name || "") === norm(name)))
      );
      const scored = await Promise.all(
        candidates.map(async (f) => {
          const id = String(f._id);
          const subtree = Array.from(collectSubtreeIds(folders, [id]));
          return {
            id,
            name: f.name || "Folder",
            alreadyLinked: String(f.linkedCourseId || "") === providerCourseId,
            itemCount: await countItems(db, subtree),
            chapterCount: folders.filter((x) => x.parentId === id).length,
            created: Number(f.timeCreated) || 0,
          };
        })
      );
      // Keep: already-linked first, then the one with the most in it, then oldest.
      scored.sort(
        (a, b) =>
          Number(b.alreadyLinked) - Number(a.alreadyLinked) || b.itemCount - a.itemCount || a.created - b.created
      );
      const keeper = scored[0] || null;
      const duplicates = opts.merge ? scored.slice(1) : [];
      for (const c of [keeper, ...duplicates]) if (c) claimed.add(c.id);

      const oldIds = [providerCourseId, ...duplicates.map((d) => d.id)];
      const count = async (coll: string, q: any) => Number(await db.collection(coll).countDocuments(q));
      const [programs, sections, members, products] = await Promise.all([
        count("orgprograms", { orgId: subscriberOrgId, courseIds: { $in: oldIds } }),
        count("orgsections", { orgId: subscriberOrgId, courseIds: { $in: oldIds } }),
        count("orgmembers", { orgId: subscriberOrgId, enrolledCourseIds: { $in: oldIds } }),
        count(PRODUCTS_COLL, { orgId: subscriberOrgId, courseId: { $in: oldIds } }),
      ]);

      plans.push({
        subscriberOrgId,
        providerOrgId: String(g.providerOrgId),
        providerCourseId,
        name,
        keeper: keeper
          ? { id: keeper.id, name: keeper.name, alreadyLinked: keeper.alreadyLinked, itemCount: keeper.itemCount }
          : null,
        duplicates: duplicates.map((d) => ({ id: d.id, name: d.name, itemCount: d.itemCount, chapterCount: d.chapterCount })),
        refs: { programs, sections, members, products },
      });
    }
  }
  return plans;
}

// Replaces any of `oldIds` in an array field with `newId` (deduped), for every
// doc in `coll` matching `scope`.
async function repointArray(db: Db, coll: string, field: string, scope: any, oldIds: string[], newId: string) {
  const docs = (await db
    .collection(coll)
    .find({ ...scope, [field]: { $in: oldIds } } as any)
    .project({ [field]: 1 })
    .toArray()) as any[];
  const old = new Set(oldIds);
  for (const d of docs) {
    const next = Array.from(new Set((d[field] as string[]).map((id) => (old.has(String(id)) ? newId : String(id)))));
    await db.collection(coll).updateOne({ _id: d._id }, { $set: { [field]: next, lastUpdated: Date.now() } });
  }
}

export async function applyCourseLinkPlans(db: Db, plans: LinkPlan[]): Promise<{ created: number; linked: number; archived: number }> {
  let created = 0;
  let linked = 0;
  let archived = 0;
  const now = Date.now();
  for (const p of plans) {
    let keeperId = p.keeper?.id;
    if (!keeperId) {
      const _id = new ObjectId();
      await db.collection("folders").insertOne({
        _id,
        name: p.name,
        subject: null,
        boardIds: [],
        contentSrc: { type: "ORGANIZATION", id: p.subscriberOrgId },
        scope: "ORG",
        userId: "",
        recordState: "ACTIVE",
        timeCreated: now,
        lastUpdated: now,
        type: "FOLDER",
        parentId: null,
        linkedCourseId: p.providerCourseId,
        linkedOrgId: p.providerOrgId,
      });
      keeperId = _id.toHexString();
      created++;
    } else if (!p.keeper!.alreadyLinked) {
      await db
        .collection("folders")
        .updateOne(
          { _id: new ObjectId(keeperId) },
          { $set: { linkedCourseId: p.providerCourseId, linkedOrgId: p.providerOrgId, lastUpdated: now } }
        );
      linked++;
    }

    const dupIds = p.duplicates.map((d) => d.id);
    if (dupIds.length) {
      // Children move up one level into the keeper; deeper levels ride along.
      await db.collection("folders").updateMany({ parentId: { $in: dupIds } } as any, { $set: { parentId: keeperId, lastUpdated: now } });
      for (const c of CONTENT_COLLS)
        await db.collection(c).updateMany({ folderId: { $in: dupIds } } as any, { $set: { folderId: keeperId, lastUpdated: now } });
      const res = await db.collection("folders").updateMany(
        { _id: { $in: dupIds.map((id) => new ObjectId(id)) } },
        { $set: { recordState: "INACTIVE", mergedInto: keeperId, lastUpdated: now } }
      );
      archived += res.modifiedCount;
    }

    const oldIds = [p.providerCourseId, ...dupIds];
    const scope = { orgId: p.subscriberOrgId };
    await repointArray(db, "orgprograms", "courseIds", scope, oldIds, keeperId);
    await repointArray(db, "orgsections", "courseIds", scope, oldIds, keeperId);
    await repointArray(db, "orgmembers", "enrolledCourseIds", scope, oldIds, keeperId);
    await db
      .collection(PRODUCTS_COLL)
      .updateMany({ ...scope, courseId: { $in: oldIds } } as any, { $set: { courseId: keeperId, lastUpdated: now } });
  }
  return { created, linked, archived };
}
