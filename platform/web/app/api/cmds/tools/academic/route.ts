import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";

// Edit Academic Structure — Departments → Programs → Centers → Sections.
// Reads/writes the org* collections directly (orgdepartments, orgprograms,
// orgcenters, orgsections), mirroring org-services :19012 /organizations/*.
const COLL: Record<string, string> = {
  department: "orgdepartments",
  program: "orgprograms",
  center: "orgcenters",
  section: "orgsections",
};

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const list = async (coll: string) => {
      const docs = await db
        .collection(coll)
        .find({ orgId, recordState: "ACTIVE" })
        .sort({ name: 1 })
        .toArray();
      return (docs as any[]).map((d) => ({
        id: String(d._id),
        name: d.name || "",
        code: d.code || "",
        departmentId: d.departmentId || null,
        programId: d.programId || null,
        centerId: d.centerId || null,
        // Program↔center links + assigned courses (only meaningful on programs).
        centerIds: Array.isArray(d.centerIds) ? d.centerIds.map(String) : [],
        courseIds: Array.isArray(d.courseIds) ? d.courseIds.map(String) : [],
      }));
    };
    const [departments, programs, centers, sections] = await Promise.all([
      list("orgdepartments"),
      list("orgprograms"),
      list("orgcenters"),
      list("orgsections"),
    ]);
    return NextResponse.json({ departments, programs, centers, sections, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message }, { status: 500 });
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

  // Relationship operations (no new node created) ---------------------------
  // Assign an existing center to a program ("this program runs in that center").
  if (kind === "assign-center") {
    if (!b.programId || !ObjectId.isValid(b.programId) || !b.centerId)
      return NextResponse.json({ error: "programId and centerId are required" }, { status: 400 });
    try {
      const db = await getDb();
      await db
        .collection("orgprograms")
        .updateOne(
          { _id: new ObjectId(b.programId), orgId } as any,
          { $addToSet: { centerIds: String(b.centerId) }, $set: { lastUpdated: Date.now() } }
        );
      return NextResponse.json({ ok: true });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Assign failed" }, { status: 500 });
    }
  }

  // Set the courses assigned to a program (Assign Courses tab).
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

  const coll = COLL[kind];
  if (!coll) return NextResponse.json({ error: `Unknown kind: ${kind}` }, { status: 400 });
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    const doc: Record<string, unknown> = {
      _id,
      orgId,
      code: name.replace(/\s+/g, "_").toUpperCase().slice(0, 20),
      name,
      cName: name.toLowerCase(),
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    };
    if (kind === "program" && b.departmentId) doc.departmentId = b.departmentId;
    if (kind === "section") {
      doc.programId = b.programId || null;
      doc.centerId = b.centerId || null;
    }
    await db.collection(coll).insertOne(doc);
    return NextResponse.json({ id: _id.toHexString(), name, kind });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Add failed" }, { status: 500 });
  }
}

type EditBody = { kind?: string; id?: string; name?: string };

// Rename an academic node.
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as EditBody;
  const coll = COLL[(b.kind || "").toLowerCase()];
  if (!coll || !b.id || !ObjectId.isValid(b.id))
    return NextResponse.json({ error: "id and valid kind required" }, { status: 400 });
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection(coll)
      .updateOne(
        { _id: new ObjectId(b.id) },
        { $set: { name, cName: name.toLowerCase(), lastUpdated: Date.now() } }
      );
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

// Remove an academic node (soft delete).
export async function DELETE(req: NextRequest) {
  const kind = (req.nextUrl.searchParams.get("kind") || "").toLowerCase();
  const id = req.nextUrl.searchParams.get("id") || "";

  // Unassign a center from a program (remove the link, keep the center).
  if (kind === "assign-center") {
    const programId = req.nextUrl.searchParams.get("programId") || "";
    const centerId = req.nextUrl.searchParams.get("centerId") || "";
    if (!ObjectId.isValid(programId) || !centerId)
      return NextResponse.json({ error: "programId and centerId required" }, { status: 400 });
    const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
    try {
      const db = await getDb();
      await db
        .collection("orgprograms")
        .updateOne(
          { _id: new ObjectId(programId), orgId } as any,
          { $pull: { centerIds: centerId }, $set: { lastUpdated: Date.now() } } as any
        );
      return NextResponse.json({ ok: true });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Unassign failed" }, { status: 500 });
    }
  }

  const coll = COLL[kind];
  if (!coll || !ObjectId.isValid(id))
    return NextResponse.json({ error: "id and valid kind required" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection(coll)
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Remove failed" }, { status: 500 });
  }
}
