import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Lists an org's programs (courses) directly from MongoDB — mirrors the legacy
// web-app "PROGRAMS" section.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("orgprograms")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ lastUpdated: -1 })
      .limit(100)
      .toArray();

    const programs = (docs as any[]).map((d) => ({
      id: String(d._id),
      name: d.name || d.cName || "(untitled)",
      code: d.code || null,
      description: d.description || "",
      isOffline: !!d.isOffline,
    }));

    return NextResponse.json({ programs, orgId });
  } catch (e: any) {
    return NextResponse.json(
      { programs: [], error: e?.message || "Failed to load programs" },
      { status: 500 }
    );
  }
}
