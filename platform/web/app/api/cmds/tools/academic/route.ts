import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { callOrgService } from "@/lib/legacyOrg";

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

async function actingUserId(req: NextRequest): Promise<string> {
  const session = await sessionFromReq(req);
  return session?.id || "admin";
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const userId = await actingUserId(req);
  const auth = { orgId, userId, callingUserId: userId };

  try {
    const departmentsRes = await callOrgService<{ list?: any[] }>("getDepartments", auth);
    const departments = (departmentsRes.list || []).map((d) => ({
      id: d.id,
      name: d.name,
      code: d.code,
    }));

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

    const centersRes = await callOrgService<{ list?: any[] }>("getCenters", auth);
    const centers = (centersRes.list || []).map((c) => ({
      id: c.id,
      name: c.name,
      code: c.code,
    }));

    const db = await getDb(); // read-only: courseIds only (see note above)
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
    const sections = perProgram.flatMap((d) => d.sections);

    return NextResponse.json({ departments, programs, centers, sections, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load structure" }, { status: 500 });
  }
}

type AddBody = {
  kind?: string; // department | program | center | section | assign-center | assign-courses
  name?: string;
  departmentId?: string;
  programId?: string;
  centerId?: string;
  courseIds?: string[];
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
  const code = codeFor(name);

  try {
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
