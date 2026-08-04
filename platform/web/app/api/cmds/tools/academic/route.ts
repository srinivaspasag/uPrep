import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { callOrgService } from "@/lib/legacyOrg";
import { sendEmail } from "@/lib/messaging";

export const dynamic = "force-dynamic";

// Edit Academic Structure — Departments -> Programs -> Centers -> Sections.
// Backed by the LIVE legacy org-services API (Organizations.java), not Mongo
// directly — see legacy/lms-master/organization/organization-services/app/
// controllers/Organizations.java:736-1255. This is the same production data
// legacy's own admin UI manages, so recordState/validation/business rules
// match legacy exactly (fixes the old INACTIVE-vs-DELETED bug for free).
//
// Exception: program.courseIds ("Assign Courses" tab) stays Mongo-direct.
// Legacy's programCourses concept is actually Board entities (resolved via
// board-services), a different domain than this app's content-folder course
// catalog — there's no faithful mapping, so that one piece is left as-is.

const COLL: Record<string, string> = {
  department: "orgdepartments",
  program: "orgprograms",
  center: "orgcenters",
  section: "orgsections",
};

function codeFor(name: string) {
  return name.replace(/\s+/g, "_").toUpperCase().slice(0, 20);
}

// Bug found live: codeFor() derives a code purely from the name with no
// collision check — two similarly-named entities (e.g. two "Class XI"
// sections under different programs) could silently end up with the same
// code. Append -2, -3, ... until it's unique within this org + collection.
async function uniqueCode(
  db: Awaited<ReturnType<typeof getDb>>,
  coll: string,
  orgId: string,
  baseCode: string
): Promise<string> {
  let code = baseCode;
  let n = 2;
  while (await db.collection(coll).findOne({ orgId, code, recordState: "ACTIVE" } as any)) {
    const suffix = `-${n}`;
    code = baseCode.slice(0, 20 - suffix.length) + suffix;
    n++;
  }
  return code;
}

async function actingUserId(req: NextRequest): Promise<string> {
  const session = await sessionFromReq(req);
  return session?.id || "admin";
}

// Legacy's getDepartments/getPrograms/getCenters/getSections list actions
// don't filter out DELETED records (confirmed live: deleting a department
// correctly flips its Mongo recordState to DELETED, but it kept showing up
// in the UI afterward because the list endpoint returns it regardless — the
// delete silently "not working" was actually this stale-list bug, not a
// failed delete). Cross-check every id against Mongo's recordState directly
// since legacy's own DTOs don't expose it.
async function activeIds(db: Awaited<ReturnType<typeof getDb>>, coll: string, ids: string[]): Promise<Set<string>> {
  const oids = ids.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
  if (oids.length === 0) return new Set();
  const docs = await db
    .collection(coll)
    .find({ _id: { $in: oids }, recordState: "ACTIVE" } as any)
    .project({ _id: 1 })
    .toArray();
  return new Set(docs.map((d: any) => String(d._id)));
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const userId = await actingUserId(req);
  const auth = { orgId, userId, callingUserId: userId };

  try {
    const db = await getDb();

    const departmentsRes = await callOrgService<{ list?: any[] }>("getDepartments", auth);
    const departmentsRaw = (departmentsRes.list || []).map((d) => ({
      id: d.id,
      name: d.name,
      code: d.code,
    }));
    const activeDeptIds = await activeIds(db, "orgdepartments", departmentsRaw.map((d) => d.id));
    const departments = departmentsRaw.filter((d) => activeDeptIds.has(d.id));

    let programsBase: any[];
    if (departments.length > 0) {
      const perDept = await Promise.all(
        departments.map((d) =>
          callOrgService<{ list?: any[] }>("getPrograms", { ...auth, departmentId: d.id }).then(
            (r) => (r.list || []).map((p) => ({ ...p, departmentId: d.id }))
          )
        )
      );
      programsBase = perDept.flat();
    } else {
      const r = await callOrgService<{ list?: any[] }>("getPrograms", auth);
      programsBase = (r.list || []).map((p) => ({ ...p, departmentId: null }));
    }
    const activeProgramIds = await activeIds(db, "orgprograms", programsBase.map((p) => p.id));
    programsBase = programsBase.filter((p) => activeProgramIds.has(p.id));

    const centersRes = await callOrgService<{ list?: any[] }>("getCenters", auth);
    const centersRaw = (centersRes.list || []).map((c) => ({
      id: c.id,
      name: c.name,
      code: c.code,
    }));
    const activeCenterIds = await activeIds(db, "orgcenters", centersRaw.map((c) => c.id));
    const centers = centersRaw.filter((c) => activeCenterIds.has(c.id));

    const perProgram = await Promise.all(
      programsBase.map(async (p) => {
        const linkedCenters = await callOrgService<{ list?: any[] }>("getProgramCenters", {
          ...auth,
          programId: p.id,
        }).then((r) => r.list || []);
        const centerIds = linkedCenters.map((c) => c.id);

        const sections = (
          await Promise.all(
            centerIds.map((centerId: string) =>
              callOrgService<{ list?: any[] }>("getSections", {
                ...auth,
                programId: p.id,
                centerId,
              }).then((r) =>
                (r.list || []).map((s) => ({
                  id: s.id,
                  name: s.name,
                  code: s.code,
                  programId: p.id,
                  centerId,
                }))
              )
            )
          )
        ).flat();
        await Promise.all(
          sections.map(async (s: any) => {
            const secDoc = ObjectId.isValid(s.id)
              ? await db.collection("orgsections").findOne({ _id: new ObjectId(s.id) })
              : null;
            s.courseIds = Array.isArray(secDoc?.courseIds) ? secDoc!.courseIds.map(String) : [];
            s.active = secDoc?.recordState === "ACTIVE";
          })
        );

        const progDoc = ObjectId.isValid(p.id)
          ? await db.collection("orgprograms").findOne({ _id: new ObjectId(p.id) })
          : null;
        const courseIds = Array.isArray(progDoc?.courseIds)
          ? progDoc!.courseIds.map(String)
          : [];

        return { centerIds, sections, courseIds };
      })
    );

    const programs = programsBase.map((p, i) => ({
      id: p.id,
      name: p.name,
      code: p.code,
      departmentId: p.departmentId,
      centerIds: perProgram[i].centerIds,
      courseIds: perProgram[i].courseIds,
    }));
    const sections = perProgram.flatMap((d) => d.sections).filter((s: any) => s.active);

    // Resolve every assigned courseId to a real name, at whatever depth it
    // lives at — the Assign Courses picker now lets staff grant a specific
    // chapter, not just a whole subject (see CourseTree), but the client's
    // top-level `courses` catalog only ever listed subject roots. Without
    // this, a chapter-level assignment saved correctly but silently
    // vanished from the "Has Courses" display, since nothing matched its id.
    const allCourseIds = new Set<string>();
    for (const s of sections) for (const id of s.courseIds || []) allCourseIds.add(id);
    for (const p of programs) for (const id of p.courseIds || []) allCourseIds.add(id);
    const courseOids = Array.from(allCourseIds)
      .filter((id) => ObjectId.isValid(id))
      .map((id) => new ObjectId(id));
    const courseFolders = courseOids.length
      ? await db.collection("folders").find({ _id: { $in: courseOids } }).toArray()
      : [];
    const courseNames: Record<string, string> = {};
    for (const f of courseFolders as any[]) courseNames[String(f._id)] = f.name || "(untitled)";

    return NextResponse.json({ departments, programs, centers, sections, courseNames, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load structure" }, { status: 500 });
  }
}

type AddBody = {
  kind?: string; // department | program | center | section | assign-center | assign-courses | assign-section-courses
  name?: string;
  departmentId?: string;
  programId?: string;
  centerId?: string;
  sectionId?: string;
  courseIds?: string[];
  notify?: boolean;
  orgId?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as AddBody;
  const kind = (b.kind || "").toLowerCase();
  const name = (b.name || "").trim();
  const orgId = await resolveOrgId(req, b.orgId);
  const userId = await actingUserId(req);
  const auth = { orgId, userId, callingUserId: userId };

  if (kind === "assign-center") {
    if (!b.programId || !b.centerId)
      return NextResponse.json({ error: "programId and centerId are required" }, { status: 400 });
    try {
      await callOrgService("addProgramCenters", {
        ...auth,
        programId: b.programId,
        centerIds: [b.centerId],
      });
      return NextResponse.json({ ok: true });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Assign failed" }, { status: 500 });
    }
  }

  // Section<->course assignment — new, on top of the program-wide list above
  // (see plan: "Section-level course assignment + email notification").
  // Mongo-direct for the same reason program.courseIds is (see file header).
  if (kind === "assign-section-courses") {
    const session = await sessionFromReq(req);
    if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
      return NextResponse.json({ error: "Only institute admins can assign courses." }, { status: 403 });
    if (!b.sectionId || !ObjectId.isValid(b.sectionId))
      return NextResponse.json({ error: "sectionId is required" }, { status: 400 });
    const courseIds = Array.from(new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String)));

    try {
      const db = await getDb();
      const section = await db
        .collection("orgsections")
        .findOne({ _id: new ObjectId(b.sectionId), orgId } as any);
      if (!section) return NextResponse.json({ error: "Section not found" }, { status: 404 });

      await db
        .collection("orgsections")
        .updateOne({ _id: new ObjectId(b.sectionId) }, { $set: { courseIds, lastUpdated: Date.now() } });

      let notified = 0;
      let delivered = 0;
      if (b.notify && courseIds.length > 0) {
        const [members, courseDocs] = await Promise.all([
          db
            .collection("orgmembers")
            .find({ orgId, profile: "STUDENT", "programMemberships.sectionId": b.sectionId } as any)
            .toArray(),
          db
            .collection("folders")
            .find({ _id: { $in: courseIds.filter(ObjectId.isValid).map((id) => new ObjectId(id)) } } as any)
            .toArray(),
        ]);
        const courseNames = (courseDocs as any[]).map((c) => c.name || "(untitled course)");
        const subject = "New courses assigned to your section";
        const text = `The following courses have been assigned to your section:\n\n${courseNames
          .map((n) => `- ${n}`)
          .join("\n")}`;
        for (const m of members as any[]) {
          if (!m.email) continue;
          notified++;
          const res = await sendEmail(m.email, subject, text, { sectionId: b.sectionId, courseIds });
          if (res.delivered) delivered++;
        }
      }

      return NextResponse.json({ ok: true, courseIds, notified, delivered });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Assign failed" }, { status: 500 });
    }
  }

  // Program<->course assignment stays Mongo-direct (see file header note).
  if (kind === "assign-courses") {
    if (!b.programId || !ObjectId.isValid(b.programId))
      return NextResponse.json({ error: "programId is required" }, { status: 400 });
    const courseIds = Array.from(new Set((Array.isArray(b.courseIds) ? b.courseIds : []).map(String)));
    try {
      const db = await getDb();
      await db
        .collection("orgprograms")
        .updateOne(
          { _id: new ObjectId(b.programId), orgId } as any,
          { $set: { courseIds, lastUpdated: Date.now() } }
        );
      return NextResponse.json({ ok: true, courseIds });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Assign failed" }, { status: 500 });
    }
  }

  if (!COLL[kind]) return NextResponse.json({ error: `Unknown kind: ${kind}` }, { status: 400 });
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const code = await uniqueCode(db, COLL[kind], orgId, codeFor(name));
    if (kind === "department") {
      const res = await callOrgService<{ id: string }>("addDepartment", { ...auth, name, code });
      return NextResponse.json({ id: res.id, name, kind });
    }
    if (kind === "program") {
      if (!b.departmentId)
        return NextResponse.json(
          { error: "departmentId is required — add a Department first" },
          { status: 400 }
        );
      const res = await callOrgService<{ id: string }>("addProgram", {
        ...auth,
        name,
        code,
        departmentId: b.departmentId,
      });
      return NextResponse.json({ id: res.id, name, kind });
    }
    if (kind === "center") {
      const res = await callOrgService<{ id: string }>("addCenter", { ...auth, name, code });
      return NextResponse.json({ id: res.id, name, kind });
    }
    if (kind === "section") {
      if (!b.programId || !b.centerId)
        return NextResponse.json(
          { error: "Select a program and center first" },
          { status: 400 }
        );
      const res = await callOrgService<{ id: string }>("addSection", {
        ...auth,
        name,
        code,
        programId: b.programId,
        centerId: b.centerId,
      });
      return NextResponse.json({ id: res.id, name, kind });
    }
    return NextResponse.json({ error: `Unknown kind: ${kind}` }, { status: 400 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Add failed" }, { status: 500 });
  }
}

type EditBody = { kind?: string; id?: string; name?: string; orgId?: string };

// Rename an academic node. Legacy's update actions require re-submitting the
// unchanged `code` (and departmentId/programId for program/section) — the
// frontend only sends the new name, so look the rest up from Mongo first
// (read-only; same doc legacy's own action then writes through).
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as EditBody;
  const kind = (b.kind || "").toLowerCase();
  const coll = COLL[kind];
  if (!coll || !b.id || !ObjectId.isValid(b.id))
    return NextResponse.json({ error: "id and valid kind required" }, { status: 400 });
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  const orgId = await resolveOrgId(req, b.orgId);
  const userId = await actingUserId(req);
  const auth = { orgId, userId, callingUserId: userId };
  const code = codeFor(name);

  try {
    const db = await getDb();
    const existing = await db.collection(coll).findOne({ _id: new ObjectId(b.id) });
    if (!existing) return NextResponse.json({ error: "Not found" }, { status: 404 });

    if (kind === "department") {
      await callOrgService("updateDepartment", { ...auth, departmentId: b.id, name, code });
    } else if (kind === "program") {
      if (!existing.departmentId)
        return NextResponse.json({ error: "Program has no department on record" }, { status: 400 });
      await callOrgService("updateProgram", {
        ...auth,
        programId: b.id,
        departmentId: existing.departmentId,
        name,
        code,
      });
    } else if (kind === "center") {
      await callOrgService("updateCenter", { ...auth, centerId: b.id, name, code });
    } else if (kind === "section") {
      if (!existing.programId)
        return NextResponse.json({ error: "Section has no program on record" }, { status: 400 });
      await callOrgService("updateSection", {
        ...auth,
        sectionId: b.id,
        programId: existing.programId,
        name,
        code,
      });
    }
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

// Remove an academic node (soft delete via legacy's remove* actions).
export async function DELETE(req: NextRequest) {
  const kind = (req.nextUrl.searchParams.get("kind") || "").toLowerCase();
  const id = req.nextUrl.searchParams.get("id") || "";
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const userId = await actingUserId(req);
  const auth = { orgId, userId, callingUserId: userId };

  if (kind === "assign-center") {
    const programId = req.nextUrl.searchParams.get("programId") || "";
    const centerId = req.nextUrl.searchParams.get("centerId") || "";
    if (!programId || !centerId)
      return NextResponse.json({ error: "programId and centerId required" }, { status: 400 });
    try {
      await callOrgService("removeProgramCenters", { ...auth, programId, centerIds: [centerId] });
      return NextResponse.json({ ok: true });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Unassign failed" }, { status: 500 });
    }
  }

  if (!COLL[kind] || !id)
    return NextResponse.json({ error: "id and valid kind required" }, { status: 400 });

  // Guard against orphaning enrolled students — legacy's own removeProgram
  // (OrgProgramManager.java:168, "// TODO: Remove from all corresponding
  // programs") never cleans up a student's programMemberships when the
  // program/section they point at gets deleted, and never blocks the delete
  // either. Left as-is, that's a real dangling reference: the student's
  // enrollment silently resolves to nothing. Block here instead, same
  // pattern as the published-question delete guard built earlier.
  if (kind === "program" || kind === "section") {
    try {
      const db = await getDb();
      const field = kind === "program" ? "programMemberships.programId" : "programMemberships.sectionId";
      const memberCount = await db.collection("orgmembers").countDocuments({
        orgId,
        recordState: "ACTIVE",
        [field]: id,
      } as any);
      if (memberCount > 0) {
        return NextResponse.json(
          {
            error: `${memberCount} student${memberCount === 1 ? " is" : "s are"} still enrolled in this ${kind} — remove them first, or this ${kind} would be deleted out from under them.`,
          },
          { status: 409 }
        );
      }
    } catch {
      // If the membership check itself fails, fall through to the normal
      // delete rather than silently blocking on an infra hiccup.
    }
  }

  try {
    if (kind === "department") await callOrgService("removeDepartment", { ...auth, departmentId: id });
    else if (kind === "program") await callOrgService("removeProgram", { ...auth, programId: id });
    else if (kind === "center") await callOrgService("removeCenter", { ...auth, centerId: id });
    else if (kind === "section") await callOrgService("removeSection", { ...auth, sectionId: id });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Remove failed" }, { status: 500 });
  }
}
