import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

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
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
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
  kind?: string; // department | program | center | section
  name?: string;
  departmentId?: string;
  programId?: string;
  centerId?: string;
  orgId?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as AddBody;
  const kind = (b.kind || "").toLowerCase();
  const name = (b.name || "").trim();
  const orgId = b.orgId || DEFAULT_ORG_ID;
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
