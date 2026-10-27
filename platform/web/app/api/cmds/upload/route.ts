import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { saveUpload } from "@/lib/storage";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Handles CMDS file uploads (Add a Document, Add a Video). Saves the file to
// local storage and records a content doc in Mongo (documents/videos), which
// the learn-app Library reads. Swap saveUpload() for S3 on AWS.
export async function POST(req: NextRequest) {
  let form: FormData;
  try {
    form = await req.formData();
  } catch {
    return NextResponse.json({ error: "Expected multipart form data" }, { status: 400 });
  }

  const kind = String(form.get("kind") || "document").toLowerCase(); // document | video | book
  const name = String(form.get("name") || "").trim();
  const subject = String(form.get("subject") || "").trim();
  const userId = String(form.get("userId") || "");
  const orgId = await resolveOrgId(req, String(form.get("orgId") || ""));
  const folderId = String(form.get("folderId") || "").trim() || null;
  let boardIds: string[] = [];
  try {
    const parsed = JSON.parse(String(form.get("boardIds") || "[]"));
    if (Array.isArray(parsed)) boardIds = parsed.filter(Boolean).map(String);
  } catch {
    /* ignore malformed boardIds, leave empty */
  }
  const file = form.get("file");

  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  if (!(file instanceof File) || file.size === 0)
    return NextResponse.json({ error: "A file is required" }, { status: 400 });

  const collection = kind === "video" ? "videos" : kind === "book" ? "books" : "documents";
  const contentType = kind === "video" ? "VIDEO" : kind === "book" ? "BOOK" : "DOCUMENT";

  try {
    const db = await getDb();
    // Only file into a folder this org owns. Inside a linked subject the
    // provider's chapters are listed alongside the org's own (browse-only) —
    // a book uploaded into one would show up for the provider and every
    // other org that chapter is shared with.
    if (folderId) {
      const folder: any = ObjectId.isValid(folderId)
        ? await db.collection("folders").findOne({ _id: new ObjectId(folderId), recordState: "ACTIVE" } as any)
        : null;
      if (!folder || folder?.contentSrc?.id !== orgId)
        return NextResponse.json(
          { error: "You can only add content to your own folders — shared chapters are read-only." },
          { status: 403 }
        );
    }
    const stored = await saveUpload(file);
    const now = Date.now();
    const _id = new ObjectId();

    await db.collection(collection).insertOne({
      _id,
      name,
      type: contentType,
      url: stored.url,
      fileName: stored.fileName,
      fileSize: stored.size,
      mimeType: stored.contentType,
      subject: subject || null,
      boardIds,
      folderId,
      contentSrc: { type: "ORGANIZATION", id: orgId },
      scope: "ORG",
      userId,
      recordState: "ACTIVE",
      // Videos are never downloadable — no toggle, no exception. Documents
      // keep the existing default (enabled unless explicitly turned off).
      ...(kind === "video" ? { downloadEnabled: false } : {}),
      timeCreated: now,
      lastUpdated: now,
    });

    return NextResponse.json({
      id: _id.toHexString(),
      name,
      type: contentType,
      url: stored.url,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Upload failed" }, { status: 500 });
  }
}
