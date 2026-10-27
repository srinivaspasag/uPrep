// Pre-deploy smoke checks — the minimum every change must not break. Runs
// against a local production build on a throwaway database seeded by
// seed.cjs; see run.sh. Exits non-zero if anything fails.
//
// Add a check here whenever a fix lands for something that broke live, so it
// stays fixed.
import crypto from "node:crypto";
import fs from "node:fs";

const BASE = process.env.SMOKE_BASE || "http://localhost:3457";
const SECRET = process.env.SESSION_SECRET;
const ids = JSON.parse(fs.readFileSync(process.argv[2], "utf8"));

// Same HMAC cookie format as lib/auth-session.ts.
const b64 = (b) => Buffer.from(b).toString("base64").replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
const token = (p, secret = SECRET) => {
  const body = b64(JSON.stringify({ memberId: null, isSuperAdmin: false, exp: Date.now() + 3.6e6, ...p }));
  return `${body}.${b64(crypto.createHmac("sha256", secret).update(body).digest())}`;
};
const as = {
  master: token({ id: "u-master", orgId: ids.P, profile: "MANAGER", isSuperAdmin: true }),
  spectra: token({ id: "u-spectra", orgId: ids.S, profile: "MANAGER" }),
  fresh: token({ id: "u-fresh", orgId: ids.S2, profile: "MANAGER" }),
  st1: token({ id: ids.ST1, memberId: ids.ST1, orgId: ids.S, profile: "STUDENT" }),
  st2: token({ id: ids.ST2, memberId: ids.ST2, orgId: ids.S, profile: "STUDENT" }),
  forged: token({ id: "x", orgId: ids.S, profile: "MANAGER" }, "wrong-secret"),
  none: null,
};

async function call(who, path, init = {}) {
  const headers = { ...(init.headers || {}) };
  if (as[who]) headers.cookie = `uprep_auth=${as[who]}`;
  if (init.body && typeof init.body === "string") headers["content-type"] = "application/json";
  const r = await fetch(BASE + path, { redirect: "manual", ...init, headers });
  let j = null;
  try {
    j = await r.json();
  } catch {}
  return { status: r.status, j, location: r.headers.get("location") || "" };
}
const json = (method, body) => ({ method, body: JSON.stringify(body) });
const upload = (who, name, folderId, kind = "book") => {
  const f = new FormData();
  f.set("kind", kind);
  f.set("name", name);
  if (folderId) f.set("folderId", folderId);
  f.set("file", new Blob(["%PDF-1.4 smoke"], { type: "application/pdf" }), "smoke.pdf");
  return call(who, "/api/cmds/upload", { method: "POST", body: f });
};
const titles = (rows) => (rows || []).map((r) => r.title || r.name).sort();
const has = (rows, name) => titles(rows).includes(name);
const adminList = async (who, qs) => (await call(who, `/api/cmds/content?${qs}`)).j?.resources || [];
const studentFolder = async (who, folderId) => (await call(who, `/api/learn/courses?folderId=${folderId}`)).j || {};
const rootSubjects = async (who) =>
  (await adminList(who, "type=FOLDER")).map((r) => `${r.title}${r.granted ? " [Shared]" : ""}${r.linked ? " [Linked]" : ""}`).sort();

let pass = 0;
const failures = [];
function check(label, ok, detail = "") {
  if (ok) pass++;
  else failures.push(label);
  console.log(`  ${ok ? "PASS" : "FAIL"}  ${label}${!ok && detail ? `  — ${detail}` : ""}`);
}
async function group(name, fn) {
  console.log(`\n${name}`);
  try {
    await fn();
  } catch (e) {
    check(`${name}: crashed`, false, e?.stack || String(e));
  }
}

await group("1. Pages & access control", async () => {
  for (const p of ["/", "/login"]) check(`GET ${p} → 200`, (await call("none", p)).status === 200);
  const cmds = await call("none", "/cmds");
  check("signed-out /cmds → redirected to login", cmds.status === 307 && cmds.location.includes("/login"));
  check("signed-out CMDS API → 401", (await call("none", "/api/cmds/content")).status === 401);
  check("forged session cookie → 401", (await call("forged", "/api/cmds/content")).status === 401);
  check("student on CMDS API → 403", (await call("st1", "/api/cmds/content")).status === 403);
  check("student course list loads", (await call("st1", "/api/learn/courses")).status === 200);
  check("student library loads", (await call("st1", "/api/library")).status === 200);
});

await group("2. Institute Resources: folder + book lifecycle", async () => {
  check("admin resource list loads", (await call("spectra", "/api/cmds/content")).status === 200);
  const f = await call("spectra", "/api/cmds/content", json("POST", { kind: "folder", name: "Smoke Chapter", parentId: ids.SPHY }));
  check("create chapter folder", f.status === 200, JSON.stringify(f.j));
  const chap = f.j?.id;
  const up = await upload("spectra", "Smoke Book", chap);
  check("upload book into it", up.status === 200, JSON.stringify(up.j));
  const book = up.j?.id;
  check("admin sees the book in the folder", has(await adminList("spectra", `parentId=${chap}`), "Smoke Book"));
  check("enrolled student sees the book", has((await studentFolder("st1", chap)).items, "Smoke Book"));
  check("rename book", (await call("spectra", "/api/cmds/content", json("PATCH", { id: book, type: "BOOK", action: "rename", name: "Smoke Book v2" }))).status === 200);
  check("renamed title shows", has(await adminList("spectra", `parentId=${chap}`), "Smoke Book v2"));
  check("hide book", (await call("spectra", "/api/cmds/content", json("PATCH", { id: book, type: "BOOK", action: "visibility", hidden: true }))).status === 200);
  check("hidden book not shown to student", !has((await studentFolder("st1", chap)).items, "Smoke Book v2"));
  await call("spectra", "/api/cmds/content", json("PATCH", { id: book, type: "BOOK", action: "visibility", hidden: false }));
  check("unhidden book back for student", has((await studentFolder("st1", chap)).items, "Smoke Book v2"));
  check("delete book", (await call("spectra", `/api/cmds/content?id=${book}&type=BOOK`, { method: "DELETE" })).status === 200);
  check("deleted book gone for admin + student",
    !has(await adminList("spectra", `parentId=${chap}`), "Smoke Book v2") && !has((await studentFolder("st1", chap)).items, "Smoke Book v2"));
  check("student NOT in a section with this subject can't open it", (await call("st2", `/api/learn/courses?folderId=${chap}`)).status === 403);
  check("another org's admin can't edit this org's folder",
    (await call("fresh", "/api/cmds/content", json("PATCH", { id: chap, type: "FOLDER", action: "rename", name: "hijack" }))).status >= 400);
});

await group("3. Program page + section publishing", async () => {
  const pr = await call("spectra", `/api/cmds/programs/${ids.PROG}`);
  check("program page data loads", pr.status === 200 && pr.j?.program?.name === "Scholar Regular JEE XI");
  // The Content tab filters centers/sections client-side by programId — the
  // API must keep tagging each section with it (bug: "No sections" shown).
  const mine = (pr.j?.sections || []).filter((s) => s.programId === ids.PROG);
  check("program's sections carry programId + centerId", mine.length === 2 && mine.every((s) => s.centerId === ids.CENTER));
  check("section's subjects resolve to names", mine.every((s) => s.courses.every((c) => c.name !== "(unknown course)")));
  const up = await upload("spectra", "Section Sample Book", null);
  const bulk = (action) => call("spectra", "/api/cmds/content/bulk", json("POST", { items: [{ id: up.j?.id, type: "BOOK" }], action, sectionId: ids.SEC1 }));
  check("add loose book to Sec A", (await bulk("addToSection")).status === 200);
  check("Content tab lists it for Sec A", has(await adminList("spectra", `sectionId=${ids.SEC1}`), "Section Sample Book"));
  const inLib = async (who) => JSON.stringify((await call(who, "/api/library")).j).includes("Section Sample Book");
  check("hidden from Sec A student until Make Visible", !(await inLib("st1")));
  check("Make Visible", (await bulk("visible")).status === 200);
  check("Sec A student sees it", await inLib("st1"));
  check("Sec B student does not", !(await inLib("st2")));
});

await group("4. Shared subjects: Link Shared Subjects tool", async () => {
  check("before: duplicates + Shared copy (the bug)", (await rootSubjects("spectra")).filter((s) => /chemistry/i.test(s)).length === 4);
  check("tool refused for non-super-admin", (await call("spectra", "/api/cmds/tools/course-links")).status === 403);
  const plan = (await call("master", "/api/cmds/tools/course-links")).j?.plans?.find((p) => p.subscriberOrgId === ids.S);
  check("preview keeps the duplicate that has content", plan?.keeper?.id === ids.D2);
  check("preview merges the others (case/space-insensitive)", plan?.duplicates.map((d) => d.id).sort().join() === [ids.D1, ids.D3].sort().join());
  check("preview changes nothing", (await rootSubjects("spectra")).filter((s) => /chemistry/i.test(s)).length === 4);
  check("apply", (await call("master", "/api/cmds/tools/course-links", { method: "POST" })).status === 200);
  check("after: exactly one Chemistry XI, Linked",
    (await rootSubjects("spectra")).filter((s) => /chemistry/i.test(s)).join() === "Chemistry XI [Linked]");
  const again = (await call("master", "/api/cmds/tools/course-links")).j?.plans || [];
  check("re-run preview: nothing left to do",
    again.every((p) => p.keeper?.alreadyLinked && !p.duplicates.length && Object.values(p.refs).every((n) => n === 0)));
});

await group("5. Linked subject: admin + student content", async () => {
  const inside = await adminList("spectra", `parentId=${ids.D2}`);
  check("admin: master chapter (read-only) + own chapter listed",
    titles(inside).join() === "1.Some Basic Concepts,Extra Practice" && inside.find((r) => r.title === "1.Some Basic Concepts")?.granted);
  const ch = await call("spectra", `/api/cmds/content?parentId=${ids.C1}`);
  check("admin: master chapter shows master's book + video", titles(ch.j?.resources).join() === "Master Lecture 1,Master NCERT Book");
  check("admin: breadcrumb goes back to own subject", ch.j?.folder?.parentId === ids.D2);
  check("book into own linked subject → OK", (await upload("spectra", "Spectra Subject Book", ids.D2)).status === 200);
  check("book into MASTER chapter → blocked", (await upload("spectra", "Sneaky Book", ids.C1)).status === 403);
  const loose = await upload("spectra", "Loose Book", null);
  check("move own book into MASTER chapter → blocked",
    (await call("spectra", "/api/cmds/content", json("PATCH", { id: loose.j?.id, type: "BOOK", action: "move", folderId: ids.C1 }))).status === 403);
  check("move own book into own chapter → OK",
    (await call("spectra", "/api/cmds/content", json("PATCH", { id: loose.j?.id, type: "BOOK", action: "move", folderId: ids.EXTRA }))).status === 200);
  for (const who of ["st1", "st2"]) {
    const chem = (await call(who, "/api/learn/courses")).j?.courses?.find((c) => c.id === ids.D2);
    check(`${who}: Chemistry XI shows master + own content (not 0)`,
      chem?.chapterCount === 2 && chem?.bookCount === 3 && chem?.videoCount === 1, JSON.stringify(chem));
  }
  const subj = await studentFolder("st1", ids.D2);
  check("student: both chapters listed", (subj.subfolders || []).map((f) => f.name).sort().join() === "1.Some Basic Concepts,Extra Practice");
  check("student: subject-level book listed", has(subj.items, "Spectra Subject Book"));
  check("student: master chapter opens with master content", titles((await studentFolder("st1", ids.C1)).items).join() === "Master Lecture 1,Master NCERT Book");
  check("master adds a book", (await upload("master", "Master New Book", ids.C1)).status === 200);
  check("student sees master's new book immediately", has((await studentFolder("st1", ids.C1)).items, "Master New Book"));
});

await group("6. Tests", async () => {
  const pick = (await call("spectra", "/api/cmds/tests")).j?.questions || [];
  check("question picker: own + shared, not unrelated orgs",
    pick.map((q) => q.id).sort().join() === [ids.QM, ids.QS].sort().join(), pick.map((q) => q.text).join(" | "));
  const mk = (who, name, folderId, qIds) => call(who, "/api/cmds/tests", json("POST", { name, durationMin: 10, questionIds: qIds, folderId }));
  check("master creates test in its chapter", (await mk("master", "Master Chapter Test", ids.C1, [ids.QM])).status === 200);
  const mixed = await mk("spectra", "Spectra Mixed Test", ids.D2, [ids.QM, ids.QS]);
  check("institute creates test from shared + own questions", mixed.status === 200, JSON.stringify(mixed.j));
  check("test into MASTER chapter → blocked", (await mk("spectra", "Sneaky Test", ids.C1, [ids.QS])).status === 403);
  check("question from unrelated org → rejected", (await mk("spectra", "Foreign Test", ids.D2, [ids.QX])).status === 400);
  const chem = (await call("st1", "/api/learn/courses")).j?.courses?.find((c) => c.id === ids.D2);
  check("student card counts both tests", chem?.testCount === 2, `testCount=${chem?.testCount}`);
  check("student sees master test in master chapter", has((await studentFolder("st1", ids.C1)).items, "Master Chapter Test"));
});

await group("7. Sharing lifecycle", async () => {
  check("revoke share", (await call("master", "/api/cmds/tools/org-grants", json("POST", { subscriberOrgId: ids.S, courseIds: [] }))).status === 200);
  check("after revoke: master chapter blocked for student", (await call("st1", `/api/learn/courses?folderId=${ids.C1}`)).status === 403);
  const subj = await studentFolder("st1", ids.D2);
  check("after revoke: institute's own items remain",
    (subj.subfolders || []).map((f) => f.name).join() === "Extra Practice" && has(subj.items, "Spectra Subject Book"));
  await call("master", "/api/cmds/tools/org-grants", json("POST", { subscriberOrgId: ids.S, courseIds: [ids.M] }));
  check("re-share reuses the same folder (no new duplicate)",
    (await rootSubjects("spectra")).filter((s) => /chemistry/i.test(s)).join() === "Chemistry XI [Linked]");
  check("after re-share: master chapter visible again", (await call("st1", `/api/learn/courses?folderId=${ids.C1}`)).status === 200);
  await call("master", "/api/cmds/tools/org-grants", json("POST", { subscriberOrgId: ids.S2, courseIds: [ids.M, ids.PHY] }));
  check("share to institute with no folders → linked subjects created",
    (await rootSubjects("fresh")).join() === "Chemistry XI [Linked],Physics XI [Linked]");
});

console.log(`\n${pass} passed, ${failures.length} failed`);
if (failures.length) {
  console.log("Failed:\n" + failures.map((f) => `  - ${f}`).join("\n"));
  process.exit(1);
}
