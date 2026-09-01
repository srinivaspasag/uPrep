import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { promises as fs } from "fs";
import path from "path";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { buildZip, type ZipEntry } from "@/lib/zip";
import { getOrCreateGroupKey, encryptBuffer, ENCRYPTION_INFO } from "@/lib/group-crypto";
import { loadOrgFolders, orderThenNatural, naturalCompare, type FolderNode } from "@/lib/courses";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Packages a distribution group into a downloadable .zip — the actual
// "copy onto a pendrive/SD card" step, which nothing else in the system
// automates (see plan: groups/access-codes only ever moved content IDs and
// licensing, never bytes). Pulls each content item's real file straight
// out of public/uploads (see lib/storage.ts — that's the only place files
// live; there's no S3 in this deployment) and zips them together with a
// manifest so staff know what's missing and why.
const GROUPS_COLL = "sellergroups";
const UPLOAD_DIR = path.join(process.cwd(), "public", "uploads");

async function requireManager(req: NextRequest) {
  const s = await sessionFromReq(req);
  return (s?.profile || "").trim().toUpperCase() === "MANAGER";
}

type ResolvedItem = {
  id: string;
  name: string;
  type: string;
  url: string | null;
  folderId: string | null;
  order: number | null;
};

async function resolveItems(db: any, ids: string[]): Promise<ResolvedItem[]> {
  const oids = ids.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
  if (oids.length === 0) return [];
  const [documents, videos, books, tests, modules, questionsets] = await Promise.all([
    db.collection("documents").find({ _id: { $in: oids } }).toArray(),
    db.collection("videos").find({ _id: { $in: oids } }).toArray(),
    db.collection("books").find({ _id: { $in: oids } }).toArray(),
    db.collection("tests").find({ _id: { $in: oids } }).toArray(),
    db.collection("modules").find({ _id: { $in: oids } }).toArray(),
    db.collection("questionsets").find({ _id: { $in: oids } }).toArray(),
  ]);
  const folderMeta = (d: any) => ({
    folderId: d.folderId ?? null,
    order: typeof d.order === "number" ? d.order : null,
  });
  const out: ResolvedItem[] = [];
  for (const d of documents as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "DOCUMENT", url: d.url ?? null, ...folderMeta(d) });
  for (const d of videos as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "VIDEO", url: d.url ?? null, ...folderMeta(d) });
  for (const d of books as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "BOOK", url: d.url ?? null, ...folderMeta(d) });
  for (const d of tests as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "TEST", url: null, folderId: null, order: null });
  for (const d of modules as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "MODULE", url: null, folderId: null, order: null });
  for (const d of questionsets as any[]) out.push({ id: String(d._id), name: d.name || d.title || "(untitled)", type: "QUESTION_SET", url: null, folderId: null, order: null });
  return out;
}

// Mode "plain" ships real, directly-openable files (like legacy did) plus a
// browsable index.html — usable with any file manager, no app required.
// Mode "encrypted" ships AES-256-GCM ciphertext (see group-crypto.ts),
// decrypted only by the Android app's native SD-card reader after it binds
// to this group's access code via /api/seller/verify.
function buildIndexHtml(groupName: string, items: { fileName: string; name: string; type: string }[]): string {
  const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  const byType = (t: string) => items.filter((i) => i.type === t);
  const section = (title: string, list: typeof items) =>
    list.length === 0
      ? ""
      : `<h2>${esc(title)}</h2><ul>${list
          .map((i) => `<li><a href="content/${esc(i.fileName)}">${esc(i.name)}</a></li>`)
          .join("")}</ul>`;
  return `<!doctype html>
<html><head><meta charset="utf-8"><title>${esc(groupName)}</title>
<style>
body{font-family:sans-serif;max-width:640px;margin:24px auto;padding:0 16px;color:#16233D}
h1{font-size:20px}h2{font-size:15px;margin-top:28px;color:#3E4A63}
ul{list-style:none;padding:0}li{margin:6px 0}
a{display:block;padding:12px 14px;background:#f4f3ee;border-radius:8px;color:#16233D;text-decoration:none}
a:active{background:#e8e6dc}
</style></head>
<body>
<h1>${esc(groupName)}</h1>
<p>Tap a title below to open it. If nothing happens, use your tablet's Files app, open the "content" folder next to this page, and tap the file directly.</p>
${section("Videos", byType("VIDEO"))}
${section("Books", byType("BOOK"))}
${section("Documents", byType("DOCUMENT"))}
</body></html>`;
}

export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can package distribution groups." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid group id" }, { status: 400 });

  const plain = req.nextUrl.searchParams.get("mode") !== "encrypted";
  const orgId = await resolveOrgId(req, null);

  try {
    const db = await getDb();
    const group: any = await db.collection(GROUPS_COLL).findOne({ _id: new ObjectId(id), orgId } as any);
    if (!group) return NextResponse.json({ error: "Group not found" }, { status: 404 });

    const topIds: string[] = Array.isArray(group.contentIds) ? group.contentIds : [];
    const topItems = await resolveItems(db, topIds);

    // One level of MODULE expansion — a module is a container, never a real
    // file, so its own children (which can point at documents/videos/tests)
    // are what actually needs to end up on the card.
    const moduleIds = topItems.filter((i) => i.type === "MODULE").map((i) => i.id);
    let childItems: ResolvedItem[] = [];
    if (moduleIds.length) {
      const moduleDocs = await db.collection("modules").find({ _id: { $in: moduleIds.map((mid) => new ObjectId(mid)) } }).toArray();
      const childIds = Array.from(
        new Set((moduleDocs as any[]).flatMap((m) => (Array.isArray(m.contentIds) ? m.contentIds : [])))
      );
      childItems = await resolveItems(db, childIds);
    }

    // Flat, deduped set of everything that ends up in the manifest — top-level
    // items minus MODULE containers themselves (superseded by their children)
    // plus the resolved children.
    const allItems = [...topItems.filter((i) => i.type !== "MODULE"), ...childItems];
    const seen = new Set<string>();
    const items = allItems
      .filter((i) => (seen.has(i.id) ? false : (seen.add(i.id), true)))
      // Same order-then-natural-name sequencing the online browse route uses
      // (app/api/learn/courses/route.ts) — the SD-card reader trusts this
      // array order instead of re-sorting client-side.
      .sort((a, b) => {
        if (a.order !== null && b.order !== null) return a.order - b.order;
        if (a.order !== null) return -1;
        if (b.order !== null) return 1;
        return naturalCompare(a.name, b.name);
      });

    // Real chapter/session tree these items actually live in — derived by
    // walking each item's own `folderId` up to its root, not from
    // `group.courseGroups` (empty for manually-picked groups, and documented
    // to omit sectionIds-tagged items even for program-based ones — walking
    // up from the item's real folderId works uniformly for every group type).
    const allFolders = await loadOrgFolders(db, orgId);
    const folderById = new Map(allFolders.map((f) => [f.id, f]));
    const neededFolderIds = new Set<string>();
    for (const item of items) {
      if (!item.folderId) continue;
      let cur: FolderNode | undefined = folderById.get(item.folderId);
      while (cur && !neededFolderIds.has(cur.id)) {
        neededFolderIds.add(cur.id);
        cur = cur.parentId ? folderById.get(cur.parentId) : undefined;
      }
    }
    const relevantFolders = orderThenNatural(allFolders.filter((f) => neededFolderIds.has(f.id)));

    const zipEntries: ZipEntry[] = [];
    const manifest: {
      id: string;
      name: string;
      type: string;
      includedAsFile: boolean;
      folderId: string | null;
      reason?: string;
      fileName?: string;
      encryptedFileName?: string;
      originalExt?: string;
    }[] = [];
    const usedNames = new Set<string>();
    const indexItems: { fileName: string; name: string; type: string }[] = [];

    // Generated/looked up once per package request — the same key encrypts
    // every file in this group, and is only ever handed out separately by
    // /api/seller/verify to a device that's already bound to an access code.
    // Only needed for the encrypted mode; plain mode never touches this.
    const groupKey = plain ? null : await getOrCreateGroupKey(db, id);

    for (const item of items) {
      const isLocalFile =
        (item.type === "DOCUMENT" || item.type === "VIDEO" || item.type === "BOOK") &&
        item.url &&
        item.url.startsWith("/uploads/");
      if (!isLocalFile) {
        manifest.push({
          id: item.id,
          name: item.name,
          type: item.type,
          includedAsFile: false,
          folderId: item.folderId,
          reason:
            item.type === "TEST" || item.type === "QUESTION_SET"
              ? "Question banks aren't files — the device needs a one-time online sync to cache these."
              : item.url
              ? "External URL, not hosted here — the device needs network access to reach it."
              : "No file attached to this item.",
        });
        continue;
      }
      const fileName = path.basename(item.url as string);
      const filePath = path.join(UPLOAD_DIR, fileName);
      try {
        const buf = await fs.readFile(filePath);
        const ext = path.extname(fileName);
        let safeName = `${item.name.replace(/[\\/:*?"<>|]/g, "_").slice(0, 120)}${ext}`;
        let n = 2;
        while (usedNames.has(safeName)) {
          safeName = `${item.name.replace(/[\\/:*?"<>|]/g, "_").slice(0, 110)} (${n})${ext}`;
          n++;
        }
        usedNames.add(safeName);
        if (plain) {
          zipEntries.push({ name: `content/${safeName}`, data: buf });
          manifest.push({ id: item.id, name: item.name, type: item.type, includedAsFile: true, folderId: item.folderId, fileName: safeName, originalExt: ext });
          indexItems.push({ fileName: safeName, name: item.name, type: item.type });
        } else {
          const encryptedName = `${safeName}.enc`;
          zipEntries.push({ name: `content/${encryptedName}`, data: encryptBuffer(buf, groupKey as Buffer) });
          manifest.push({ id: item.id, name: item.name, type: item.type, includedAsFile: true, folderId: item.folderId, encryptedFileName: encryptedName, originalExt: ext });
        }
      } catch {
        manifest.push({
          id: item.id,
          name: item.name,
          type: item.type,
          includedAsFile: false,
          folderId: item.folderId,
          reason: "File missing on the server — could not be read.",
        });
      }
    }

    if (plain && indexItems.length > 0) {
      zipEntries.push({ name: "index.html", data: Buffer.from(buildIndexHtml(group.name || "Content", indexItems), "utf8") });
    }

    zipEntries.push({
      name: "manifest.json",
      data: Buffer.from(
        JSON.stringify(
          {
            groupId: id,
            groupName: group.name,
            packagedAt: Date.now(),
            packagingMode: plain ? "plain" : "encrypted",
            // Program → Course grouping, when this group came from "Pack a
            // whole Program" — lets the reader show Program/Courses/Content
            // instead of one flat file list, matching the online Learn app's
            // navigation. Absent (undefined) for manually-picked groups or
            // groups created before this field existed — readers must treat
            // that as "no hierarchy, show flat" rather than an error.
            programName: group.programName || null,
            courseGroups: Array.isArray(group.courseGroups) && group.courseGroups.length > 0 ? group.courseGroups : null,
            // Real chapter/session tree below the Course level — every item's
            // `folderId` (in `items[]` below) points at a node in here, so the
            // reader can drill Course → Chapter → ... → session-wise content
            // instead of dumping a course's entire item list flat. Derived
            // fresh from each item's own folderId (see the ancestor walk
            // above), not stored on the group — always matches what's really
            // in items[] even for manually-picked groups.
            folders: relevantFolders.length > 0
              ? relevantFolders.map((f) => ({ id: f.id, name: f.name, parentId: f.parentId, order: f.order }))
              : null,
            totalItems: items.length,
            includedAsFiles: manifest.filter((m) => m.includedAsFile).length,
            // Plain mode: files in content/ are the real, directly-openable
            // files — open index.html, or browse content/ with a file
            // manager. Encrypted mode: files are ciphertext, not playable
            // as-is — the decryption key is never in this zip, it's released
            // separately, over the network, by /api/seller/verify to a
            // device that binds itself to this group's access code. See
            // ENCRYPTION_INFO.layout for the per-file byte format — the
            // Android app's SdCardCrypto implements this handshake.
            encryption: plain ? null : ENCRYPTION_INFO,
            items: manifest,
          },
          null,
          2
        ),
        "utf8"
      ),
    });

    const zipBuffer = buildZip(zipEntries);

    const safeGroupName = (group.name || "distribution-group").replace(/[\\/:*?"<>|]/g, "_");
    return new NextResponse(new Uint8Array(zipBuffer), {
      status: 200,
      headers: {
        "Content-Type": "application/zip",
        "Content-Disposition": `attachment; filename="${safeGroupName}.zip"`,
        "Content-Length": String(zipBuffer.length),
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Package failed" }, { status: 500 });
  }
}
