import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Public slug -> org resolver for /i/[slug] (an institute's own branded
// entry point). Only the handful of fields needed to brand a pre-login page
// and redirect into the existing deep link — never anything sensitive
// (address, contact number, etc. stay behind the authenticated org routes).
//
//   GET /api/orgs/by-slug?slug=scholar-spectra -> { id, name, logoUrl }
export async function GET(req: NextRequest) {
  const slug = (req.nextUrl.searchParams.get("slug") || "").trim().toLowerCase();
  if (!slug) return NextResponse.json({ error: "slug is required" }, { status: 400 });

  try {
    const db = await getDb();
    const org = await db.collection("organizations").findOne({ slug, recordState: "ACTIVE" } as any);
    if (!org) return NextResponse.json({ error: "Institute not found" }, { status: 404 });
    return NextResponse.json({
      id: String(org._id),
      name: org.name || org.fullName || "",
      logoUrl: org.logoUrl || "",
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
