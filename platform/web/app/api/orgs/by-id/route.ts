import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Public id -> org branding lookup — used by /login when deep-linked via
// ?org=<id> (either directly, or via /i/[slug] redirecting into it) to show
// that institute's own logo instead of the default. Same minimal, public-safe
// field set as by-slug/route.ts.
//
//   GET /api/orgs/by-id?id=<orgId> -> { id, name, logoUrl }
export async function GET(req: NextRequest) {
  const id = (req.nextUrl.searchParams.get("id") || "").trim();
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const db = await getDb();
    const org = await db
      .collection("organizations")
      .findOne({ _id: new ObjectId(id), recordState: "ACTIVE" } as any);
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
