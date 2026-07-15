import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { parseVideoUrl } from "@/lib/video";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Adds a video by external URL (YouTube / Vimeo) — the legacy CMDS "Add by URL"
// flow. Unlike /api/cmds/upload (which stores a file), this records an embed
// link (linkType: ADDED) that the learn app renders as an inline iframe.
export async function POST(req: NextRequest) {
  let body: any;
  try {
    body = await req.json();
  } catch {
    return NextResponse.json({ error: "Expected JSON body" }, { status: 400 });
  }

  const name = String(body?.name || "").trim();
  const subject = String(body?.subject || "").trim();
  const userId = String(body?.userId || "");
  const orgId = String(body?.orgId || DEFAULT_ORG_ID);
  const folderId = String(body?.folderId || "").trim() || null;
  const rawUrl = String(body?.url || "").trim();

  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  if (!rawUrl) return NextResponse.json({ error: "A video URL is required" }, { status: 400 });

  const parsed = parseVideoUrl(rawUrl);
  if (!parsed)
    return NextResponse.json(
      { error: "Unsupported video URL. Paste a YouTube or Vimeo link." },
      { status: 400 }
    );

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();

    await db.collection("videos").insertOne({
      _id,
      name,
      type: "VIDEO",
      linkType: "ADDED",
      provider: parsed.provider,
      videoId: parsed.videoId,
      embedUrl: parsed.embedUrl,
      externalUrl: parsed.externalUrl,
      // `url` is the canonical watch page so generic "open" links (CMDS list,
      // search, bookmarks, playlists) keep working.
      url: parsed.externalUrl,
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
      type: "VIDEO",
      linkType: "ADDED",
      provider: parsed.provider,
      embedUrl: parsed.embedUrl,
      url: parsed.externalUrl,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to add video" }, { status: 500 });
  }
}
