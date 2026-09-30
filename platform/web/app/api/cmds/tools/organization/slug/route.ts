import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { slugify } from "@/lib/slug";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Org URL slug — Mongo-direct, same reasoning as logo/route.ts: this isn't a
// field the legacy org-service's updateOrganization knows about, so it's set
// directly here instead of round-tripping through callOrgService. Backs the
// institute's own branded entry point at /i/<slug> (see app/i/[slug]/page.tsx
// and /login's ?org=&inst= deep link, which /i/[slug] redirects into).
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
    return NextResponse.json({ error: "Only institute admins can change the URL slug." }, { status: 403 });

  const orgId = await resolveOrgId(req, null);
  if (!ObjectId.isValid(orgId)) return NextResponse.json({ error: "Invalid org" }, { status: 400 });

  const b = (await req.json().catch(() => ({}))) as { slug?: string };
  const slug = slugify(b.slug || "");
  if (!slug) return NextResponse.json({ error: "Enter a URL slug (letters, numbers, hyphens)." }, { status: 400 });

  try {
    const db = await getDb();
    const clash = await db.collection("organizations").findOne({ slug, _id: { $ne: new ObjectId(orgId) } });
    if (clash) return NextResponse.json({ error: `"${slug}" is already in use by another institute.` }, { status: 409 });

    await db
      .collection("organizations")
      .updateOne({ _id: new ObjectId(orgId) }, { $set: { slug, lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true, slug });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to save" }, { status: 500 });
  }
}
