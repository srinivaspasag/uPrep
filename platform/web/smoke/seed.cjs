// Seeds the throwaway smoke-test database (see smoke/run.sh) and prints the
// fixture ids as JSON. Never point this at a real database — it drops it first.
//
// Shape mirrors real usage, including the bug that motivated linked subjects:
//   Master Org  — "Chemistry XI" (chapter + book + video), "Physics XI"
//   Scholar Spectra — receives a grant for Master's Chemistry XI, but also has
//     3 duplicate "Chemistry XI" folders of its own (one holding a chapter with
//     a worksheet) and its own unshared "Physics XI".
//     Program "Scholar Regular JEE XI": Sec A -> [dup Chemistry XI, Physics XI],
//     Sec B -> [Master's Chemistry XI directly]. One student in each.
//   Fresh Institute — no folders at all; gets grants during the run.
const { MongoClient, ObjectId } = require("mongodb");

const uri = process.env.MONGO_URI;
const dbName = process.env.MONGO_DB;
if (!uri || !dbName || !/127\.0\.0\.1|localhost/.test(uri)) {
  console.error("seed.cjs only runs against a local throwaway MONGO_URI");
  process.exit(1);
}

(async () => {
  const c = await MongoClient.connect(uri, { useUnifiedTopology: true });
  const db = c.db(dbName);
  await db.dropDatabase();
  const id = () => new ObjectId();
  const now = Date.now();
  const P = id(), S = id(), S2 = id();
  const owned = (org) => ({ contentSrc: { type: "ORGANIZATION", id: String(org) }, recordState: "ACTIVE", lastUpdated: now });
  const folder = (_id, name, org, parentId = null, extra = {}) =>
    ({ _id, name, type: "FOLDER", parentId, timeCreated: now, ...owned(org), ...extra });
  const item = (name, org, folderId, type) =>
    ({ _id: id(), name, type, folderId: String(folderId), timeCreated: now, ...owned(org) });

  const M = id(), C1 = id(), PHY = id(), D1 = id(), D2 = id(), D3 = id(), EXTRA = id(), SPHY = id();
  await db.collection("organizations").insertMany([
    { _id: P, name: "Master Org" }, { _id: S, name: "Scholar Spectra" }, { _id: S2, name: "Fresh Institute" },
  ]);
  await db.collection("folders").insertMany([
    folder(M, "Chemistry XI", P), folder(C1, "1.Some Basic Concepts", P, String(M)), folder(PHY, "Physics XI", P),
    folder(D1, "Chemistry XI", S, null, { timeCreated: now - 3000 }),
    folder(D2, "Chemistry XI", S, null, { timeCreated: now - 2000 }),
    folder(D3, "chemistry  xi", S, null, { timeCreated: now - 1000 }),
    folder(EXTRA, "Extra Practice", S, String(D2)), folder(SPHY, "Physics XI", S),
  ]);
  await db.collection("books").insertOne(item("Master NCERT Book", P, C1, "BOOK"));
  await db.collection("videos").insertOne(item("Master Lecture 1", P, C1, "VIDEO"));
  await db.collection("documents").insertOne(item("Spectra Worksheet", S, EXTRA, "DOCUMENT"));
  await db.collection("orgcoursegrants").insertOne({
    providerOrgId: String(P), subscriberOrgId: String(S), courseId: String(M), programNames: ["NEET Online"], recordState: "ACTIVE",
  });

  const CENTER = id(), PROG = id(), OTHER_PROG = id(), SEC1 = id(), SEC2 = id(), OTHER_SEC = id();
  await db.collection("orgcenters").insertOne({ _id: CENTER, orgId: String(S), name: "Puducherry", recordState: "ACTIVE" });
  await db.collection("orgprograms").insertMany([
    { _id: PROG, orgId: String(S), name: "Scholar Regular JEE XI", courseIds: [], recordState: "ACTIVE" },
    { _id: OTHER_PROG, orgId: String(S), name: "Unrelated Program", courseIds: [], recordState: "ACTIVE" },
  ]);
  await db.collection("orgsections").insertMany([
    { _id: SEC1, orgId: String(S), programId: String(PROG), centerId: String(CENTER), name: "Sec A", courseIds: [String(D1), String(SPHY)], recordState: "ACTIVE" },
    { _id: SEC2, orgId: String(S), programId: String(PROG), centerId: String(CENTER), name: "Sec B", courseIds: [String(M)], recordState: "ACTIVE" },
    { _id: OTHER_SEC, orgId: String(S), programId: String(OTHER_PROG), centerId: String(CENTER), name: "Other Sec", courseIds: [], recordState: "ACTIVE" },
  ]);
  const ST1 = id(), ST2 = id();
  await db.collection("orgmembers").insertMany([
    { _id: ST1, orgId: String(S), profile: "STUDENT", recordState: "ACTIVE", programMemberships: [{ programId: String(PROG), centerId: String(CENTER), sectionId: String(SEC1) }] },
    { _id: ST2, orgId: String(S), profile: "STUDENT", recordState: "ACTIVE", programMemberships: [{ programId: String(PROG), centerId: String(CENTER), sectionId: String(SEC2) }] },
  ]);

  const q = (org, text) => ({ _id: id(), content: text, type: "SCQ", options: ["a", "b"], hasAns: true, boardIds: [], ...owned(org) });
  const QM = q(P, "Master Q: mole concept"), QS = q(S, "Spectra Q: stoichiometry"), QX = q(id(), "Unrelated org Q");
  await db.collection("questions").insertMany([QM, QS, QX]);
  await db.collection("answers").insertMany([QM, QS, QX].map((x) => ({ qId: String(x._id) })));

  const out = { P, S, S2, M, C1, PHY, D1, D2, D3, EXTRA, SPHY, CENTER, PROG, OTHER_PROG, SEC1, SEC2, OTHER_SEC, ST1, ST2, QM: QM._id, QS: QS._id, QX: QX._id };
  console.log(JSON.stringify(Object.fromEntries(Object.entries(out).map(([k, v]) => [k, String(v)]))));
  await c.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
