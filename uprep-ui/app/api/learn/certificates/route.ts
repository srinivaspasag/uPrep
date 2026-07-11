import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Certificates are earned by completing program activity. We compute a simple
// eligibility signal: a student who has finished at least one test earns a
// participation certificate per active program.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const userId = req.nextUrl.searchParams.get("userId") || "";
  try {
    const db = await getDb();
    // Program data lives in `orgprograms` (same source the Programs pages use),
    // keyed by orgId — not the empty `programs` collection.
    const programs: any[] = await db
      .collection("orgprograms")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ lastUpdated: -1 })
      .limit(100)
      .toArray();

    const finished = userId
      ? await db.collection("userentityattempts").countDocuments({ userId, entityType: "TEST" })
      : 0;

    const items = programs.map((p) => ({
      id: String(p._id),
      name: p.name || p.cName || "Program",
      code: p.code || null,
      eligible: finished > 0,
      testsCompleted: finished,
    }));

    return NextResponse.json({ items, testsCompleted: finished });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}
