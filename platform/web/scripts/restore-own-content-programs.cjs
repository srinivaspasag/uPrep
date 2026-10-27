// Undoes "Link Shared Subjects" (lib/course-links.ts) for programs that build
// their OWN subjects — e.g. Scholar Spectra's "Scholar Repeater JEE/NEET
// (XI & XII)". The tool matched their same-named, empty folders ("Physics XI",
// …) to the master's shared subjects by name, linked/merged them and repointed
// the programs' sections at the linked subject, so they showed master content.
//
// For each such program this:
//   - gives it its own unlinked folder per affected subject — reactivating one
//     of the archived merge duplicates of that subject, else creating a new one
//     (each program gets its own; JEE and NEET don't share);
//   - repoints the program, its sections and its students (only students in
//     no other program) from the linked subject to that folder;
//   - flags the program `ownContent: true`, so the tool leaves it alone next time.
// Anything the institute had filed in the merged duplicates was moved into the
// linked subject and can't be attributed back automatically — it's listed, not moved.
//
// Dry run by default. Run inside the ui container:
//   docker compose exec ui node scripts/restore-own-content-programs.cjs --org <orgId> [--match "scholar repeater"]
//   … --apply     (writes; saves a before-image JSON next to the script first)
const fs = require("fs");
const path = require("path");
const { MongoClient, ObjectId } = require("mongodb");

const arg = (k) => {
  const i = process.argv.indexOf(k);
  return i > 0 ? process.argv[i + 1] : undefined;
};
const ORG = arg("--org");
const MATCH = new RegExp(arg("--match") || "scholar repeater", "i");
const APPLY = process.argv.includes("--apply");
if (!ORG) {
  console.error('usage: node scripts/restore-own-content-programs.cjs --org <orgId> [--match "scholar repeater"] [--apply]');
  process.exit(1);
}
const CONTENT_COLLS = ["videos", "documents", "books", "tests", "modules", "questionsets"];
const oid = (id) => (ObjectId.isValid(String(id)) ? new ObjectId(String(id)) : id);

(async () => {
  const c = await MongoClient.connect(process.env.MONGO_URI || "mongodb://localhost:27117", { useUnifiedTopology: true });
  const db = c.db(process.env.MONGO_DB || "localvedantu");
  const now = Date.now();

  const programs = (await db.collection("orgprograms").find({ orgId: ORG, recordState: "ACTIVE" }).toArray()).filter((p) =>
    MATCH.test(p.name || "")
  );
  if (!programs.length) {
    console.log(`No ACTIVE programs in org ${ORG} matching ${MATCH}.`);
    return c.close();
  }
  const programIds = programs.map((p) => String(p._id));
  const sections = await db.collection("orgsections").find({ orgId: ORG, programId: { $in: programIds } }).toArray();

  const topLevel = await db
    .collection("folders")
    .find({ "contentSrc.id": ORG, $or: [{ parentId: null }, { parentId: { $exists: false } }] })
    .toArray();
  const linked = new Map(
    topLevel.filter((f) => f.recordState === "ACTIVE" && f.linkedCourseId).map((f) => [String(f._id), f])
  );
  const archivedInto = new Map(); // keeper id -> archived duplicates merged into it
  for (const f of topLevel)
    if (f.recordState === "INACTIVE" && f.mergedInto) {
      const list = archivedInto.get(String(f.mergedInto)) || [];
      list.push(f);
      archivedInto.set(String(f.mergedInto), list);
    }
  const ownedIds = new Set(topLevel.map((f) => String(f._id)));

  console.log(`Org ${ORG}: ${programs.length} program(s) matching ${MATCH} — ${APPLY ? "APPLYING" : "dry run"}\n`);

  const usedDup = new Set();
  const actions = []; // { program, keeper, restoreId, reuse: dupDoc | null }
  const warnings = [];
  for (const p of programs) {
    const pid = String(p._id);
    const mySections = sections.filter((s) => s.programId === pid);
    const refs = new Set([...(p.courseIds || []), ...mySections.flatMap((s) => s.courseIds || [])].map(String));
    console.log(`■ ${p.name} (${pid})${p.ownContent ? " — already ownContent" : ""}, ${mySections.length} section(s)`);
    for (const r of refs) {
      const k = linked.get(r);
      if (k) {
        const dup = (archivedInto.get(r) || []).find((d) => !usedDup.has(String(d._id)));
        if (dup) usedDup.add(String(dup._id));
        actions.push({ program: p, keeper: k, reuse: dup || null, restoreId: dup ? String(dup._id) : new ObjectId().toHexString() });
        console.log(
          `   ✗ "${k.name}" → linked subject ${r} (shows master content). Will point at ${
            dup ? `reactivated duplicate ${dup._id}` : "a NEW empty folder"
          }.`
        );
      } else if (!ownedIds.has(r)) {
        console.log(`   ! ${r} is not this org's folder (points straight at a provider subject) — left as is.`);
      } else {
        console.log(`   ✓ ${r} own folder`);
      }
    }
  }

  // What the merge moved into each affected linked subject (same timestamp as
  // the archived duplicates) — origin unknown, so reported only.
  for (const kid of new Set(actions.map((a) => String(a.keeper._id)))) {
    const stamps = (archivedInto.get(kid) || []).map((d) => d.lastUpdated).filter(Boolean);
    if (!stamps.length) continue;
    const moved = [];
    const chapters = await db.collection("folders").find({ parentId: kid, "contentSrc.id": ORG, lastUpdated: { $in: stamps } }).toArray();
    for (const f of chapters) moved.push(`chapter "${f.name}" (${f._id})`);
    for (const coll of CONTENT_COLLS) {
      const docs = await db.collection(coll).find({ folderId: kid, "contentSrc.id": ORG, lastUpdated: { $in: stamps } }).toArray();
      for (const d of docs) moved.push(`${coll} "${d.name || d.title}" (${d._id})`);
    }
    if (moved.length)
      warnings.push(`Linked subject ${kid} received these from merged duplicates — move by hand if they belong to the Repeater programs:\n     ${moved.join("\n     ")}`);
  }

  const memberFilter = { orgId: ORG, recordState: "ACTIVE", "programMemberships.programId": { $in: programIds } };
  const members = await db.collection("orgmembers").find(memberFilter).toArray();
  console.log(`\n${members.length} member(s) in these programs.`);
  if (warnings.length) console.log("\nWARNINGS\n  " + warnings.join("\n  "));

  if (!APPLY) {
    console.log("\nDry run — nothing changed. Re-run with --apply.");
    return c.close();
  }

  const backup = {
    at: new Date(now).toISOString(),
    programs,
    sections,
    members: members.map((m) => ({ _id: m._id, enrolledCourseIds: m.enrolledCourseIds })),
    reactivated: actions.filter((a) => a.reuse).map((a) => a.reuse),
    created: actions.filter((a) => !a.reuse).map((a) => a.restoreId),
  };
  const file = path.join(__dirname, `restore-own-content-backup-${now}.json`);
  fs.writeFileSync(file, JSON.stringify(backup, null, 1));
  console.log(`\nBefore-image saved: ${file}`);

  const swap = (ids, from, to) => Array.from(new Set((ids || []).map(String).map((x) => (x === from ? to : x))));
  for (const a of actions) {
    const pid = String(a.program._id);
    const kid = String(a.keeper._id);
    if (a.reuse)
      await db
        .collection("folders")
        .updateOne({ _id: a.reuse._id }, { $set: { recordState: "ACTIVE", lastUpdated: now }, $unset: { mergedInto: "", linkedCourseId: "", linkedOrgId: "" } });
    else
      await db.collection("folders").insertOne({
        _id: new ObjectId(a.restoreId),
        name: a.keeper.name,
        subject: a.keeper.subject || null,
        boardIds: a.keeper.boardIds || [],
        contentSrc: { type: "ORGANIZATION", id: ORG },
        scope: "ORG",
        userId: "",
        recordState: "ACTIVE",
        timeCreated: now,
        lastUpdated: now,
        type: "FOLDER",
        parentId: null,
      });

    const p = await db.collection("orgprograms").findOne({ _id: a.program._id });
    if ((p.courseIds || []).map(String).includes(kid))
      await db.collection("orgprograms").updateOne({ _id: p._id }, { $set: { courseIds: swap(p.courseIds, kid, a.restoreId), lastUpdated: now } });
    for (const s of await db.collection("orgsections").find({ orgId: ORG, programId: pid, courseIds: kid }).toArray())
      await db.collection("orgsections").updateOne({ _id: s._id }, { $set: { courseIds: swap(s.courseIds, kid, a.restoreId), lastUpdated: now } });
    for (const m of await db.collection("orgmembers").find({ ...memberFilter, "programMemberships.programId": pid, enrolledCourseIds: kid }).toArray()) {
      const onlyHere = (m.programMemberships || []).every((pm) => String(pm.programId) === pid);
      const next = onlyHere ? swap(m.enrolledCourseIds, kid, a.restoreId) : Array.from(new Set([...(m.enrolledCourseIds || []).map(String), a.restoreId]));
      await db.collection("orgmembers").updateOne({ _id: m._id }, { $set: { enrolledCourseIds: next, lastUpdated: now } });
    }
    console.log(`  ${a.program.name}: "${a.keeper.name}" ${kid} → ${a.restoreId}${a.reuse ? " (reactivated)" : " (new)"}`);
  }
  await db
    .collection("orgprograms")
    .updateMany({ _id: { $in: programs.map((p) => oid(p._id)) } }, { $set: { ownContent: true, lastUpdated: now } });
  console.log(`Flagged ${programs.length} program(s) ownContent.`);
  await c.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
