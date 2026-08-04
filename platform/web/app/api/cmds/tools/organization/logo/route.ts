import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { saveUpload } from "@/lib/storage";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Org logo — Mongo-direct (see the GET handler in ../route.ts for why:
// legacy's orgThumbnail isn't reachable through updateOrganization, only a
// separate multipart action legacy itself uses). Reuses the same local-disk
// upload helper the mark sheets uploader already uses.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
    return NextResponse.json({ error: "Only institute admins can change the logo." }, { status: 403 });

  const orgId = await resolveOrgId(req, null);
  if (!ObjectId.isValid(orgId)) return NextResponse.json({ error: "Invalid org" }, { status: 400 });

  const form = await req.formData().catch(() => null);
  const file = form?.get("file");
  if (!(file instanceof File)) return NextResponse.json({ error: "file is required" }, { status: 400 });

  try {
    const stored = await saveUpload(file);
    const db = await getDb();
    await db
      .collection("organizations")
      .updateOne({ _id: new ObjectId(orgId) }, { $set: { logoUrl: stored.url, lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true, url: stored.url });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Upload failed" }, { status: 500 });
  }
}
