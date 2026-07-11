import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Organization Info — read/update the single `organizations` doc (same data
// org-services :19012 /organizations/getOrganization|updateOrganization uses).
function toObjId(id: string) {
  const { ObjectId } = require("mongodb");
  try {
    return new ObjectId(id);
  } catch {
    return id;
  }
}

export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const o: any =
      (await db.collection("organizations").findOne({ _id: toObjId(orgId) })) ||
      (await db.collection("organizations").findOne({}));
    if (!o) return NextResponse.json({ error: "Organization not found" }, { status: 404 });

    return NextResponse.json({
      org: {
        id: String(o._id),
        name: o.name || "",
        fullName: o.fullName || "",
        website: o.website || "",
        contactNumber: o.contactNumber || "",
        type: o.type || "COLLEGE",
        address: o.address || "",
        description: o.description || "",
        authType: o.authType || "VEDANTU",
        doubtsForumMode: o.doubtsForumMode || "public",
        locations: Array.isArray(o.locations) ? o.locations : [],
        socialMedia: o.socialMedia || {},
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as any;
  const orgId = b.orgId || DEFAULT_ORG_ID;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const set: Record<string, unknown> = {
      name,
      fullName: (b.fullName || "").trim(),
      website: (b.website || "").trim(),
      contactNumber: (b.contactNumber || "").trim(),
      type: b.type || "COLLEGE",
      address: (b.address || "").trim(),
      description: (b.description || "").trim(),
      authType: b.authType || "VEDANTU",
      doubtsForumMode: b.doubtsForumMode || "public",
      lastUpdated: Date.now(),
    };
    if (b.socialMedia && typeof b.socialMedia === "object") set.socialMedia = b.socialMedia;

    await db.collection("organizations").updateOne({ _id: toObjId(orgId) }, { $set: set });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
