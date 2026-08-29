import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";
import { naturalCompare } from "@/lib/courses";

export const dynamic = "force-dynamic";

type ResourceRow = {
  id: string;
  title: string;
  type: string; // FOLDER | DOCUMENT | VIDEO | TEST | MODULE | QUESTION_SET
  subject: string | null;
  addedBy: string | null;
  addedAt: number;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
  count?: number;
  hidden?: boolean; // true = invisible on learn/device (students can't see it), org-wide
  visibleProgramIds?: string[]; // non-empty = visible ONLY to these programs' students
  sectionIds?: string[]; // sections this item has been added to
  visibleSectionIds?: string[]; // subset of sectionIds currently published/visible
  downloadEnabled?: boolean; // false = download blocked on device, org-wide
  order?: number | null; // manual sequence override — see the "reorder" PATCH action
  visibleUserIds?: string[]; // per-student override: force-visible even if program/section rules wouldn't show it
  hiddenUserIds?: string[]; // per-student override: force-hidden even if otherwise visible
};

// GET: aggregate all Institute Resources across content collections. When
// `parentId` is supplied, only content inside that folder is returned (folders
// via `parentId`, other content via `folderId`); at root, only unfiled content.
// When `sectionId` is supplied instead, returns a FLAT list (any folder, any
// type) of items already added to that section — the legacy per-section
// content list, not a folder tree.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management.", resources: [] }, { status: 403 });

  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const subject = req.nextUrl.searchParams.get("subject"); // optional filter
  const typeFilter = req.nextUrl.searchParams.get("type"); // optional: FOLDER etc.
  const parentId = req.nextUrl.searchParams.get("parentId"); // folder id or null=root
  const sectionId = req.nextUrl.searchParams.get("sectionId"); // section-scoped flat list
  // Bug found live: Module create/edit's "Available content" picker only
  // ever showed items filed directly in the folder the module itself lives
  // in, so building a module from content spread across different course
  // folders was impossible. `all=1` drops folder scoping entirely — every
  // item in the org, any folder — same override precedence as sectionId.
  const allContent = req.nextUrl.searchParams.get("all") === "1";
  // Search, mainly for `all=1` — with 5000+ videos org-wide, a flat
  // "everything" list is only usable with a way to narrow it down; the
  // per-collection .limit(200) below would otherwise silently truncate to
  // whatever 200 happened to sort first, which for a large org is nowhere
  // near "all".
  const q = (req.nextUrl.searchParams.get("q") || "").trim();

  try {
    const db = await getDb();
    const base = { "contentSrc.id": orgId, recordState: "ACTIVE" };


    const rows: ResourceRow[] = [];

    // Folder scoping: folders use `parentId`, content uses `folderId`. A
    // sectionId overrides folder scoping entirely (flat list, content only);
    // so does `all=1`, which additionally skips FOLDER rows (a module's
    // content picker never lists folders, only leaf content).
    const inFolder = (field: string) =>
      allContent
        ? {}
        : sectionId
        ? { sectionIds: sectionId }
        : parentId
        ? { [field]: parentId }
        : { $or: [{ [field]: null }, { [field]: { $exists: false } }] };

    const pull = async (
      coll: string,
      type: string,
      map: (d: any) => Partial<ResourceRow>
    ) => {
      if ((sectionId || allContent) && type === "FOLDER") return; // flat lists never include folders
      const field = type === "FOLDER" ? "parentId" : "folderId";
      const searchMatch = q
        ? { $or: [{ name: { $regex: q, $options: "i" } }, { title: { $regex: q, $options: "i" } }] }
        : {};
      const docs = await db
        .collection(coll)
        .find({ ...base, ...inFolder(field), ...searchMatch } as any)
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
        embedUrl: d.embedUrl ?? null,
        provider: d.provider ?? null,
        hidden: !!d.hidden,
        visibleProgramIds: Array.isArray(d.visibleProgramIds) ? d.visibleProgramIds : [],
        sectionIds: Array.isArray(d.sectionIds) ? d.sectionIds : [],
        visibleSectionIds: Array.isArray(d.visibleSectionIds) ? d.visibleSectionIds : [],
        downloadEnabled: d.downloadEnabled !== false,
        order: typeof d.order === "number" ? d.order : null,
        visibleUserIds: Array.isArray(d.visibleUserIds) ? d.visibleUserIds : [],
        hiddenUserIds: Array.isArray(d.hiddenUserIds) ? d.hiddenUserIds : [],
        ...map(d),
      });
      }
    };

    await pull("folders", "FOLDER", () => ({}));
    await pull("documents", "DOCUMENT", () => ({}));
    await pull("books", "BOOK", () => ({}));
    await pull("videos", "VIDEO", () => ({}));
    await pull("tests", "TEST", (d) => ({ count: d.qusCount ?? 0 }));
    await pull("modules", "MODULE", (d) => ({
      count: Array.isArray(d.contentIds) ? d.contentIds.length : 0,
    }));
    await pull("questionsets", "QUESTION_SET", (d) => ({
      count: Array.isArray(d.qIds) ? d.qIds.length : 0,
    }));

    let out = rows;
    if (subject && subject !== "All Subjects") {
      // The Subjects filter now lists real Board Tree subject names (e.g.
      // "Chemistry XI") — match top-level subject folders by their own
      // name (that's literally what they're named), falling back to the
      // legacy `subject` tag field for any content that has it set.
      const wanted = subject.toLowerCase();
      out = out.filter(
        (r) =>
          (r.type === "FOLDER" && r.title.toLowerCase() === wanted) ||
          (r.subject || "").toLowerCase() === wanted
      );
    }
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
  BOOK: "books",
  VIDEO: "videos",
  TEST: "tests",
  MODULE: "modules",
  QUESTION_SET: "questionsets",
};

type MutateBody = {
  id?: string;
  type?: string;
  action?: "rename" | "move" | "visibility" | "download" | "student-visibility" | "edit-module";
  name?: string;
  folderId?: string | null;
  hidden?: boolean;
  visibleProgramIds?: string[];
  order?: number | null;
  downloadEnabled?: boolean;
  visibleUserIds?: string[];
  hiddenUserIds?: string[];
  subject?: string;
  boardIds?: string[];
  contentIds?: string[];
  sessions?: ModuleSession[];
};

// PATCH: rename a resource, move it into a folder (folderId null = root), or
// toggle its student visibility (Make Visible / Invisible on learn/device).
export async function PATCH(req: NextRequest) {
  const callerSession = await sessionFromReq(req);
  if (!canManageContent(callerSession?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

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
  } else if (b.action === "visibility") {
    // Visibility is an admin-only control (Institute/Super Admin), not teachers.
    const session = await sessionFromReq(req);
    if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
      return NextResponse.json({ error: "Only institute admins can change visibility." }, { status: 403 });
    // hidden=true removes it from student library/course browse (org-wide); staff still see it.
    if (b.hidden !== undefined) set.hidden = !!b.hidden;
    // visibleProgramIds: non-empty = visible ONLY to those programs' students
    // (set from a Program's Content tab); empty/omitted = visible to any
    // program with course access, i.e. today's default behavior.
    if (b.visibleProgramIds !== undefined)
      set.visibleProgramIds = Array.from(new Set((b.visibleProgramIds || []).map(String)));
  } else if (b.action === "download") {
    // Videos are never downloadable — no toggle, no exception, regardless of
    // what a caller sends. Only documents can still have this turned off.
    if (String(b.type).toUpperCase() === "VIDEO")
      return NextResponse.json({ error: "Videos can't be made downloadable." }, { status: 400 });
    set.downloadEnabled = b.downloadEnabled !== false;
  } else if (b.action === "student-visibility") {
    // Per-student exception on top of the program/section rules — publish
    // something early to one student, or unpublish it from just one, without
    // touching the section-wide visibility. hiddenUserIds wins over
    // visibleUserIds if a student somehow ends up in both (shouldn't happen
    // from the UI, but "hidden" being the stronger guarantee is the safer
    // default to preserve if it ever does).
    const session = await sessionFromReq(req);
    if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
      return NextResponse.json({ error: "Only institute admins can change visibility." }, { status: 403 });
    if (b.visibleUserIds !== undefined)
      set.visibleUserIds = Array.from(new Set((b.visibleUserIds || []).map(String)));
    if (b.hiddenUserIds !== undefined)
      set.hiddenUserIds = Array.from(new Set((b.hiddenUserIds || []).map(String)));
  } else if (b.action === "edit-module") {
    // Modules had a create form but no way to edit one afterward — same
    // fields the create form collects (name/subject/boardIds/contentIds),
    // just updated in place instead of inserted.
    if (String(b.type).toUpperCase() !== "MODULE")
      return NextResponse.json({ error: "edit-module only applies to modules" }, { status: 400 });
    const name = (b.name || "").trim();
    if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
    if (!Array.isArray(b.contentIds) || b.contentIds.length === 0)
      return NextResponse.json({ error: "Pick at least one item for the module" }, { status: 400 });
    set.name = name;
    set.subject = (b.subject || "").trim();
    set.boardIds = Array.isArray(b.boardIds) ? b.boardIds : [];
    set.contentIds = b.contentIds;
    if (b.sessions !== undefined) set.sessions = Array.isArray(b.sessions) ? b.sessions : [];
  } else if (b.action === "reorder") {
    // Explicit manual sequence — wins over the natural-name-sort fallback
    // students/mobile see (see lib/courses.ts). Needed for chapters/items
    // with no usable numbering in their name at all (e.g. "Vectors" vs
    // "Kinematics" — nothing to sort naturally by).
    set.order = typeof b.order === "number" ? b.order : null;
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
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

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

type ModuleSession = { name: string; contentIds: string[] };

type CreateBody = {
  kind?: string; // folder | module | questionset | autotest
  name?: string;
  subject?: string;
  userId?: string;
  orgId?: string;
  parentId?: string;
  contentIds?: string[]; // module
  sessions?: ModuleSession[]; // module — groups contentIds into named sessions (Session 1, 2, ...)
  qIds?: string[]; // questionset
  boardIds?: string[];
  folderId?: string;
};

// POST: create non-file content (folder, module, question set).
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const body = (await req.json().catch(() => ({}))) as CreateBody;
  const kind = (body.kind || "").toLowerCase();
  const name = (body.name || "").trim();
  const orgId = await resolveOrgId(req, body.orgId);
  const userId = body.userId || "";
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  const now = Date.now();
  const _id = new ObjectId();
  const common = {
    _id,
    name,
    subject: (body.subject || "").trim() || null,
    boardIds: Array.isArray(body.boardIds) ? body.boardIds : [],
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
        folderId: body.folderId || null,
        contentIds: Array.isArray(body.contentIds) ? body.contentIds : [],
        // Sessions group the same contentIds into named chunks (Session 1,
        // Session 2, ...) — additive on top of the flat, ordered contentIds
        // (which stays the concatenation of every session in order), so
        // anything reading contentIds directly is unaffected.
        sessions: Array.isArray(body.sessions) ? body.sessions : [],
      });
    } else if (kind === "questionset") {
      await db.collection("questionsets").insertOne({
        ...common,
        type: "QUESTION_SET",
        folderId: body.folderId || null,
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
