import { NextRequest, NextResponse } from "next/server";
import { saveUpload } from "@/lib/storage";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Picture upload for question text / options / solutions ("Add picture" and
// paste-a-screenshot in the question editor and Bulk Import review). Staff
// only — /api/cmds is gated by middleware.ts. Returns the token to insert:
// `![picture](/uploads/<file>)`, which components/MathText renders.

const MAX_BYTES = 5 * 1024 * 1024;
const TYPES: Record<string, string> = {
  "image/png": ".png",
  "image/jpeg": ".jpg",
  "image/gif": ".gif",
  "image/webp": ".webp",
};

export async function POST(req: NextRequest) {
  const form = await req.formData().catch(() => null);
  const file = form?.get("file");
  if (!(file instanceof File)) return NextResponse.json({ error: "Choose a picture to upload" }, { status: 400 });
  const ext = TYPES[file.type];
  if (!ext) return NextResponse.json({ error: "Pictures must be PNG, JPG, GIF or WebP" }, { status: 400 });
  if (file.size > MAX_BYTES) return NextResponse.json({ error: "Picture is too large (max 5 MB)" }, { status: 400 });

  // Name by content type, not the client's file name, so the stored name
  // always matches the /uploads/<safe name> pattern MathText accepts.
  const named = new File([await file.arrayBuffer()], `picture${ext}`, { type: file.type });
  try {
    const stored = await saveUpload(named);
    return NextResponse.json({ ok: true, url: stored.url, token: `![picture](${stored.url})` });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Upload failed" }, { status: 500 });
  }
}
