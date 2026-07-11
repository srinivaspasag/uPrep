import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { saveUpload } from "@/lib/storage";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Mark sheets = offline test results uploaded per program. GET lists prior
// uploads; POST stores the spreadsheet and records a `marksheets` doc. On AWS,
// swap saveUpload() for S3 and add a parser to ingest scores into attempts.
export async function GET(_req: NextRequest, { params }: { params: { id: string } }) {
  try {
    const db = await getDb();
    const docs = await db
      .collection("marksheets")
      .find({ programId: params.id, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();
    const items = (docs as any[]).map((m) => ({
      id: String(m._id),
      name: m.name || m.fileName || "Mark sheet",
      url: m.url || null,
      fileSize: m.fileSize || 0,
      uploadedAt: m.timeCreated || 0,
      status: m.status || "UPLOADED",
    }));
    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest, { params }: { params: { id: string } }) {
  let form: FormData;
  try {
    form = await req.formData();
  } catch {
    return NextResponse.json({ error: "Expected multipart form data" }, { status: 400 });
  }

  const file = form.get("file");
  const userId = String(form.get("userId") || "");
  const orgId = String(form.get("orgId") || DEFAULT_ORG_ID);
  if (!(file instanceof File) || file.size === 0)
    return NextResponse.json({ error: "A .xls/.xlsx file is required" }, { status: 400 });

  try {
    const stored = await saveUpload(file);
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("marksheets").insertOne({
      _id,
      programId: params.id,
      orgId,
      name: stored.fileName,
      fileName: stored.fileName,
      url: stored.url,
      fileSize: stored.size,
      mimeType: stored.contentType,
      uploadedBy: userId || null,
      status: "UPLOADED",
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({
      id: _id.toHexString(),
      name: stored.fileName,
      url: stored.url,
      fileSize: stored.size,
      uploadedAt: now,
      status: "UPLOADED",
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Upload failed" }, { status: 500 });
  }
}
