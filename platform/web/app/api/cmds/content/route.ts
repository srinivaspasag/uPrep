import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

type ResourceRow = {
  id: string;
  title: string;
  type: string; // FOLDER | DOCUMENT | VIDEO | TEST | MODULE | QUESTION_SET
  subject: string | null;
  addedBy: string | null;
  addedAt: number;
  url?: string | null;
  count?: number;
};

// GET: aggregate all Institute Resources across content collections. When
// `parentId` is supplied, only content inside that folder is returned (folders
// via `parentId`, other content via `folderId`); at root, only unfiled content.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const subject = req.nextUrl.searchParams.get("subject"); // optional filter
  const typeFilter = req.nextUrl.searchParams.get("type"); // optional: FOLDER etc.
  const parentId = req.nextUrl.searchParams.get("parentId"); // folder id or null=root

  try {
    const db = await getDb();
    const base = { "contentSrc.id": orgId, recordState: "ACTIVE" };

    // Lightweight folder list for the "move to folder" picker.
    if (req.nextUrl.searchParams.get("allFolders") === "1") {
      const fdocs = await db.collection("folders").find(base as any).sort({ name: 1 }).toArray();
      return NextResponse.json({
        folders: (fdocs as any[]).map((f) => ({ id: String(f._id), name: f.name || "Folder" })),
      });
    }

    const rows: ResourceRow[] = [];

    // Folder scoping: folders use `parentId`, content uses `folderId`.
    const inFolder = (field: string) =>
      parentId
        ? { [field]: parentId }
        : { $or: [{ [field]: null }, { [field]: { $exists: false } }] };

    const pull = async (
      coll: string,
      type: string,
      map: (d: any) => Partial<ResourceRow>
    ) => {
      const field = type === "FOLDER" ? "parentId" : "folderId";
      const docs = await db
        .collection(coll)
        .find({ ...base, ...inFolder(field) } as any)
        .sort({ lastUpdated: -1 })
        .limit(200)
        .toArray();
      for (const d of docs as any[]) {
        rows.push({
          id: String(d._id),
          title: d.name || d.title || "(untitled)",
          type,
          subject: d.subject ?? null,
          addedBy: d.userId ?? null,
          addedAt: Number(d.timeCreated) || Number(d.lastUpdated) || 0,
          url: d.url ?? null,
          ...map(d),
        });
      }
    };

    await pull("folders", "FOLDER", () => ({}));
    await pull("documents", "DOCUMENT", () => ({}));
    await pull("videos", "VIDEO", () => ({}));
    await pull("tests", "TEST", (d) => ({ count: d.qusCount ?? 0 }));
    await pull("modules", "MODULE", (d) => ({
      count: Array.isArray(d.contentIds) ? d.contentIds.length : 0,
    }));
    await pull("questionsets", "QUESTION_SET", (d) => ({
      count: Array.isArray(d.qIds) ? d.qIds.length : 0,
    }));

    let out = rows;
    if (subject && subject !== "All Subjects")
      out = out.filter((r) => (r.subject || "").toLowerCase() === subject.toLowerCase());
    if (typeFilter && typeFilter !== "All Resources")
      out = out.filter((r) => r.type === typeFilter);

    out.sort((a, b) => b.addedAt - a.addedAt);

    // Resolve current folder name for breadcrumbs.
    let folder: { id: string; name: string; parentId: string | null } | null = null;
    if (parentId && ObjectId.isValid(parentId)) {
      const f: any = await db.collection("folders").findOne({ _id: new ObjectId(parentId) });
      if (f) folder = { id: String(f._id), name: f.name || "Folder", parentId: f.parentId || null };
    }

    return NextResponse.json({ resources: out, orgId, folder });
  } catch (e: any) {
    return NextResponse.json({ resources: [], error: e?.message }, { status: 500 });
  }
}

const COLL_FOR_TYPE: Record<string, string> = {
  FOLDER: "folders",
  DOCUMENT: "documents",
  VIDEO: "videos",
  TEST: "tests",
  MODULE: "modules",
  QUESTION_SET: "questionsets",
};

type MutateBody = {
  id?: string;
  type?: string;
  action?: "rename" | "move";
  name?: string;
  folderId?: string | null;
};

// PATCH: rename a resource, or move it into a folder (folderId null = root).
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as MutateBody;
  const coll = COLL_FOR_TYPE[String(b.type || "").toUpperCase()];
  if (!coll || !b.id) return NextResponse.json({ error: "id and valid type required" }, { status: 400 });
  if (!ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  const set: Record<string, unknown> = { lastUpdated: Date.now() };
  if (b.action === "rename") {
    const name = (b.name || "").trim();
    if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
    set.name = name;
  } else if (b.action === "move") {
    // Folders track parentId; other content tracks folderId.
    const field = String(b.type).toUpperCase() === "FOLDER" ? "parentId" : "folderId";
    set[field] = b.folderId || null;
  } else {
    return NextResponse.json({ error: "Unsupported action" }, { status: 400 });
  }

  try {
    const db = await getDb();
    await db.collection(coll).updateOne({ _id: new ObjectId(b.id) }, { $set: set });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

// DELETE: soft-delete a resource (recordState = INACTIVE).
export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  const type = (req.nextUrl.searchParams.get("type") || "").toUpperCase();
  const coll = COLL_FOR_TYPE[type];
  if (!coll || !id) return NextResponse.json({ error: "id and valid type required" }, { status: 400 });
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const db = await getDb();
    await db
      .collection(coll)
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}

type CreateBody = {
  kind?: string; // folder | module | questionset | autotest
  name?: string;
  subject?: string;
  userId?: string;
  orgId?: string;
  parentId?: string;
  contentIds?: string[]; // module
  qIds?: string[]; // questionset
};

// POST: create non-file content (folder, module, question set).
export async function POST(req: NextRequest) {
  const body = (await req.json().catch(() => ({}))) as CreateBody;
  const kind = (body.kind || "").toLowerCase();
  const name = (body.name || "").trim();
  const orgId = body.orgId || DEFAULT_ORG_ID;
  const userId = body.userId || "";
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  const now = Date.now();
  const _id = new ObjectId();
  const common = {
    _id,
    name,
    subject: (body.subject || "").trim() || null,
    contentSrc: { type: "ORGANIZATION", id: orgId },
    scope: "ORG",
    userId,
    recordState: "ACTIVE",
    timeCreated: now,
    lastUpdated: now,
  };

  try {
    const db = await getDb();
    if (kind === "folder") {
      await db.collection("folders").insertOne({
        ...common,
        type: "FOLDER",
        parentId: body.parentId || null,
      });
    } else if (kind === "module") {
      await db.collection("modules").insertOne({
        ...common,
        type: "MODULE",
        contentIds: Array.isArray(body.contentIds) ? body.contentIds : [],
      });
    } else if (kind === "questionset") {
      await db.collection("questionsets").insertOne({
        ...common,
        type: "QUESTION_SET",
        qIds: Array.isArray(body.qIds) ? body.qIds : [],
      });
    } else {
      return NextResponse.json({ error: `Unsupported kind: ${kind}` }, { status: 400 });
    }
    return NextResponse.json({ id: _id.toHexString(), name, kind });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}
