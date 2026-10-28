// Folds an institute's separate per-program subject copies back into its ONE
// common subject (the one linked to the master's), e.g. Scholar Spectra's
// 5× "Chemistry XI". Undoes scripts/restore-own-content-programs.cjs (removed),
// which had given each "Scholar Regular/Repeater" program its own copy to keep
// it at zero — that broke the institute's single common subject tree. Zero
// content for those programs is now enforced at read time instead (programs
// keep `ownContent: true`; see lib/enrollment.ts accessibleFolderIds).
//
// For each ACTIVE top-level linked subject, every ACTIVE top-level, unlinked
// folder of the same name in the same org is merged into it: programs,
// sections and students pointing at the copy are repointed to the common
// subject and the copy is archived (INACTIVE, mergedInto). A copy that has any
// chapter or item in it is NOT touched — it's reported for a manual decision.
//
// Dry run by default. Run inside the ui container:
//   docker compose exec ui node scripts/merge-into-common-subjects.cjs --org <orgId>
//   … --apply     (writes; saves a before-image JSON next to the script first)
const fs = require("fs");
const path = require("path");
const { MongoClient } = require("mongodb");

const arg = (k) => {
  const i = process.argv.indexOf(k);
  return i > 0 ? process.argv[i + 1] : undefined;
};
const ORG = arg("--org");
const APPLY = process.argv.includes("--apply");
if (!ORG) {
  console.error("usage: node scripts/merge-into-common-subjects.cjs --org <orgId> [--apply]");
  process.exit(1);
}
const CONTENT_COLLS = ["videos", "documents", "books", "tests", "modules", "questionsets"];
const norm = (s) => String(s || "").trim().replace(/\s+/g, " ").toLowerCase();

(async () => {
  const c = await MongoClient.connect(process.env.MONGO_URI || "mongodb://localhost:27117", { useUnifiedTopology: true });
  const db = c.db(process.env.MONGO_DB || "localvedantu");
  const now = Date.now();

  const top = await db
    .collection("folders")
    .find({ "contentSrc.id": ORG, recordState: "ACTIVE", $or: [{ parentId: null }, { parentId: { $exists: false } }] })
    .toArray();
  const linked = top.filter((f) => f.linkedCourseId);
  const copies = top.filter((f) => !f.linkedCourseId);

  const plan = []; // { keeper, copy }
  const blocked = [];
  for (const k of linked) {
    for (const f of copies.filter((x) => norm(x.name) === norm(k.name))) {
      const id = String(f._id);
      const chapters = await db.collection("folders").countDocuments({ parentId: id, recordState: "ACTIVE" });
      let items = 0;
      for (const coll of CONTENT_COLLS) items += await db.collection(coll).countDocuments({ folderId: id, recordState: "ACTIVE" });
      if (chapters || items) blocked.push(`"${f.name}" ${id}: ${chapters} chapter(s), ${items} item(s) — left as is`);
      else plan.push({ keeper: k, copy: f });
    }
  }

  const copyIds = plan.map((p) => String(p.copy._id));
  const keeperOf = new Map(plan.map((p) => [String(p.copy._id), String(p.keeper._id)]));
  const progs = await db.collection("orgprograms").find({ orgId: ORG, courseIds: { $in: copyIds } }).toArray();
  const secs = await db.collection("orgsections").find({ orgId: ORG, courseIds: { $in: copyIds } }).toArray();
  const members = await db.collection("orgmembers").find({ orgId: ORG, enrolledCourseIds: { $in: copyIds } }).toArray();

  console.log(`Org ${ORG} — ${APPLY ? "APPLYING" : "dry run"}`);
  for (const k of linked) {
    const mine = plan.filter((p) => p.keeper === k);
    if (mine.length) console.log(`  "${k.name}" (common, ${k._id}) ← ${mine.length} empty cop${mine.length === 1 ? "y" : "ies"}`);
  }
  console.log(`  repoint: ${progs.length} program(s), ${secs.length} section(s), ${members.length} student(s)`);
  if (blocked.length) console.log("NOT merged (have content):\n  " + blocked.join("\n  "));

  if (!APPLY) {
    console.log("\nDry run — nothing changed. Re-run with --apply.");
    return c.close();
  }

  const file = path.join(__dirname, `merge-common-subjects-backup-${now}.json`);
  fs.writeFileSync(
    file,
    JSON.stringify(
      {
        at: new Date(now).toISOString(),
        copies: plan.map((p) => p.copy),
        programs: progs.map((p) => ({ _id: p._id, courseIds: p.courseIds })),
        sections: secs.map((s) => ({ _id: s._id, courseIds: s.courseIds })),
        members: members.map((m) => ({ _id: m._id, enrolledCourseIds: m.enrolledCourseIds })),
      },
      null,
      1
    )
  );
  console.log(`Before-image saved: ${file}`);

  const swap = (ids) => Array.from(new Set((ids || []).map(String).map((x) => keeperOf.get(x) || x)));
  for (const p of progs) await db.collection("orgprograms").updateOne({ _id: p._id }, { $set: { courseIds: swap(p.courseIds), lastUpdated: now } });
  for (const s of secs) await db.collection("orgsections").updateOne({ _id: s._id }, { $set: { courseIds: swap(s.courseIds), lastUpdated: now } });
  for (const m of members)
    await db.collection("orgmembers").updateOne({ _id: m._id }, { $set: { enrolledCourseIds: swap(m.enrolledCourseIds), lastUpdated: now } });
  for (const p of plan)
    await db
      .collection("folders")
      .updateOne({ _id: p.copy._id }, { $set: { recordState: "INACTIVE", mergedInto: String(p.keeper._id), lastUpdated: now } });
  console.log(`Merged ${plan.length} cop${plan.length === 1 ? "y" : "ies"}.`);
  await c.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
