import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";

// Device Management — mirrors org-services :19012 /activityLogger/getUsers.
// Lists members with their web/mobile availability, derived from activity
// records where present (legacy stack has little seeded activity).
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const memberId = req.nextUrl.searchParams.get("memberId"); // detail mode: login history for one member

  try {
    const db = await getDb();

    // Detail mode — one member's recent login history (device/time/ip),
    // for the "View Details" drill-down. Legacy shows a richer per-page
    // activity feed we have no data source for; this is what we actually
    // track (see lib/login-log.ts).
    if (memberId) {
      if (!ObjectId.isValid(memberId)) return NextResponse.json({ error: "Invalid memberId" }, { status: 400 });
      // logins.userId isn't always the orgmember's _id — some accounts have a
      // distinct prefixed userId (e.g. "LOCAL_<_id>"). Match either, same
      // fallback the list view already relies on.
      const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) });
      const candidateIds = Array.from(new Set([memberId, member?.userId].filter(Boolean)));
      const history = await db
        .collection("logins")
        .find({ orgId, userId: { $in: candidateIds } } as any)
        .sort({ at: -1 })
        .limit(20)
        .toArray();
      return NextResponse.json({
        history: (history as any[]).map((l) => ({
          device: l.device || "WEB",
          at: l.at || null,
          ip: l.ip || null,
        })),
      });
    }

    const profile = (req.nextUrl.searchParams.get("profile") || "STUDENT").toUpperCase();
    const query = (req.nextUrl.searchParams.get("query") || "").trim().toLowerCase();
    const programId = req.nextUrl.searchParams.get("programId") || "";
    const centerId = req.nextUrl.searchParams.get("centerId") || "";
    const sectionId = req.nextUrl.searchParams.get("sectionId") || "";

    const membershipFilter: Record<string, unknown> = {};
    if (sectionId) membershipFilter["programMemberships.sectionId"] = sectionId;
    else if (centerId) membershipFilter["programMemberships.centerId"] = centerId;
    else if (programId) membershipFilter["programMemberships.programId"] = programId;

    const members = await db
      .collection("orgmembers")
      .find({
        orgId,
        recordState: "ACTIVE",
        ...(profile !== "ALL" ? { profile } : {}),
        ...membershipFilter,
      } as any)
      .toArray();

    // Resolve Program/Center/Section names for student rows (batch lookup,
    // same pattern as academic/route.ts) — legacy shows these as columns.
    const membershipIds = new Set<string>();
    for (const m of members as any[]) {
      for (const mm of Array.isArray(m.programMemberships) ? m.programMemberships : []) {
        if (mm.programId) membershipIds.add(mm.programId);
        if (mm.centerId) membershipIds.add(mm.centerId);
        if (mm.sectionId) membershipIds.add(mm.sectionId);
      }
    }
    const idList = Array.from(membershipIds).filter(ObjectId.isValid).map((id) => new ObjectId(id));
    const [progDocs, centerDocs, sectionDocs] = idList.length
      ? await Promise.all([
          db.collection("orgprograms").find({ _id: { $in: idList } }).toArray(),
          db.collection("orgcenters").find({ _id: { $in: idList } }).toArray(),
          db.collection("orgsections").find({ _id: { $in: idList } }).toArray(),
        ])
      : [[], [], []];
    const nameOf = new Map<string, string>();
    for (const d of [...progDocs, ...centerDocs, ...sectionDocs] as any[]) nameOf.set(String(d._id), d.name || "");

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
      const memberships: Array<{ programId: string; centerId: string; sectionId: string }> = Array.isArray(
        m.programMemberships
      )
        ? m.programMemberships
        : [];
      return {
        id: String(m._id),
        memberId: m.memberId || "",
        name: `${m.firstName || ""} ${m.lastName || ""}`.trim(),
        profile: m.profile || "",
        web: recent && !onMobile ? "LOGGED_IN" : "LOGGED_OUT",
        mobile: recent && onMobile ? "LOGGED_IN" : "LOGGED_OUT",
        lastSeen: last?.at || null,
        lastDevice: last?.device || null,
        programCount: memberships.length,
        program: memberships[0] ? nameOf.get(memberships[0].programId) || null : null,
        center: memberships[0] ? nameOf.get(memberships[0].centerId) || null : null,
        section: memberships[0] ? nameOf.get(memberships[0].sectionId) || null : null,
      };
    });

    if (query) rows = rows.filter((r) => `${r.name} ${r.memberId}`.toLowerCase().includes(query));

    return NextResponse.json({ devices: rows, orgId });
  } catch (e: any) {
    return NextResponse.json({ devices: [], error: e?.message }, { status: 500 });
  }
}
