import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { PRODUCTS_COLL } from "@/lib/commerce";
import { resolveCourseCatalog } from "@/lib/grants";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Products = a course offered for sale at a price. Lists products + the org's
// course catalog for the create form.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(PRODUCTS_COLL)
      .find({ orgId, recordState: "ACTIVE" } as any)
      .sort({ timeCreated: -1 })
      .toArray();
    const catalog = await resolveCourseCatalog(db, orgId);
    const products = (docs as any[]).map((p) => ({
      id: String(p._id),
      courseId: p.courseId,
      name: p.name,
      priceCents: p.priceCents,
      currency: p.currency || "INR",
    }));
    return NextResponse.json({
      products,
      courses: catalog.map((c) => ({ id: c.id, name: c.name })),
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ products: [], courses: [], error: e?.message }, { status: 500 });
  }
}

type Body = { courseId?: string; name?: string; price?: number; currency?: string; orgId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const orgId = await resolveOrgId(req, b.orgId);
  const courseId = String(b.courseId || "");
  const name = (b.name || "").trim();
  const priceCents = Math.max(0, Math.round(Number(b.price) * 100 || 0));
  if (!courseId) return NextResponse.json({ error: "Choose a course" }, { status: 400 });
  if (!name) return NextResponse.json({ error: "Product name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const catalog = await resolveCourseCatalog(db, orgId);
    if (!catalog.some((c) => c.id === courseId))
      return NextResponse.json({ error: "That course isn't in your catalog" }, { status: 400 });

    const now = Date.now();
    const _id = new ObjectId();
    await db.collection(PRODUCTS_COLL).insertOne({
      _id,
      orgId,
      courseId,
      name,
      priceCents,
      currency: (b.currency || "INR").toUpperCase(),
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name, priceCents });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    await db
      .collection(PRODUCTS_COLL)
      .updateOne({ _id: new ObjectId(id), orgId } as any, {
        $set: { recordState: "INACTIVE", lastUpdated: Date.now() },
      });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
