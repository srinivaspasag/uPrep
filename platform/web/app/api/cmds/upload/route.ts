import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { saveUpload } from "@/lib/storage";
import { DEFAULT_ORG_ID } from "@/lib/config";

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

  const kind = String(form.get("kind") || "document").toLowerCase(); // document | video
  const name = String(form.get("name") || "").trim();
  const subject = String(form.get("subject") || "").trim();
  const userId = String(form.get("userId") || "");
  const orgId = String(form.get("orgId") || DEFAULT_ORG_ID);
  const folderId = String(form.get("folderId") || "").trim() || null;
  const file = form.get("file");

  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  if (!(file instanceof File) || file.size === 0)
    return NextResponse.json({ error: "A file is required" }, { status: 400 });

  const collection = kind === "video" ? "videos" : "documents";
  const contentType = kind === "video" ? "VIDEO" : "DOCUMENT";

  try {
    const stored = await saveUpload(file);
    const db = await getDb();
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
      folderId,
      contentSrc: { type: "ORGANIZATION", id: orgId },
      scope: "ORG",
      userId,
      recordState: "ACTIVE",
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
