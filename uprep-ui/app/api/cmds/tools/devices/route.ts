import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Device Management — mirrors org-services :19012 /activityLogger/getUsers.
// Lists members with their web/mobile availability, derived from activity
// records where present (legacy stack has little seeded activity).
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const profile = (req.nextUrl.searchParams.get("profile") || "STUDENT").toUpperCase();
  const query = (req.nextUrl.searchParams.get("query") || "").trim().toLowerCase();

  try {
    const db = await getDb();
    const members = await db
      .collection("orgmembers")
      .find({ orgId, recordState: "ACTIVE", ...(profile !== "ALL" ? { profile } : {}) })
      .toArray();

    // Build a set of userIds seen in activityrecords (treated as "logged in on web").
    let activeUserIds = new Set<string>();
    try {
      const acts = await db
        .collection("activityrecords")
        .find({ orgId })
        .project({ userId: 1 })
        .limit(1000)
        .toArray();
      activeUserIds = new Set((acts as any[]).map((a) => String(a.userId)));
    } catch {
      /* activity collection may be absent — treat all as offline */
    }

    let rows = (members as any[]).map((m) => {
      const web = activeUserIds.has(String(m.userId));
      return {
        id: String(m._id),
        memberId: m.memberId || "",
        name: `${m.firstName || ""} ${m.lastName || ""}`.trim(),
        profile: m.profile || "",
        web: web ? "LOGGED_IN" : "LOGGED_OUT",
        mobile: "LOGGED_OUT",
      };
    });

    if (query) rows = rows.filter((r) => `${r.name} ${r.memberId}`.toLowerCase().includes(query));

    return NextResponse.json({ devices: rows, orgId });
  } catch (e: any) {
    return NextResponse.json({ devices: [], error: e?.message }, { status: 500 });
  }
}
