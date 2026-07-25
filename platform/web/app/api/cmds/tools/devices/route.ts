import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";

// Device Management — mirrors org-services :19012 /activityLogger/getUsers.
// Lists members with their web/mobile availability, derived from activity
// records where present (legacy stack has little seeded activity).
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const profile = (req.nextUrl.searchParams.get("profile") || "STUDENT").toUpperCase();
  const query = (req.nextUrl.searchParams.get("query") || "").trim().toLowerCase();

  try {
    const db = await getDb();
    const members = await db
      .collection("orgmembers")
      .find({ orgId, recordState: "ACTIVE", ...(profile !== "ALL" ? { profile } : {}) })
      .toArray();

    // Most-recent real login per user (from the `logins` audit written on auth).
    const lastLoginByUser = new Map<string, any>();
    try {
      const logins = await db
        .collection("logins")
        .find({ orgId })
        .sort({ at: -1 })
        .limit(5000)
        .toArray();
      for (const l of logins as any[]) {
        const key = String(l.userId);
        if (!lastLoginByUser.has(key)) lastLoginByUser.set(key, l);
      }
    } catch {
      /* logins collection may be absent — treat all as never-seen */
    }

    // A login within the last 30 minutes counts as "currently active".
    const ACTIVE_WINDOW = 30 * 60 * 1000;
    const now = Date.now();

    let rows = (members as any[]).map((m) => {
      const last = lastLoginByUser.get(String(m._id)) || lastLoginByUser.get(String(m.userId));
      const recent = last && now - (last.at || 0) < ACTIVE_WINDOW;
      const onMobile = last && (last.device === "ANDROID" || last.device === "IOS");
      return {
        id: String(m._id),
        memberId: m.memberId || "",
        name: `${m.firstName || ""} ${m.lastName || ""}`.trim(),
        profile: m.profile || "",
        web: recent && !onMobile ? "LOGGED_IN" : "LOGGED_OUT",
        mobile: recent && onMobile ? "LOGGED_IN" : "LOGGED_OUT",
        lastSeen: last?.at || null,
        lastDevice: last?.device || null,
      };
    });

    if (query) rows = rows.filter((r) => `${r.name} ${r.memberId}`.toLowerCase().includes(query));

    return NextResponse.json({ devices: rows, orgId });
  } catch (e: any) {
    return NextResponse.json({ devices: [], error: e?.message }, { status: 500 });
  }
}
