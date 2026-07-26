import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";

export const dynamic = "force-dynamic";

// Lists an org's programs directly from MongoDB — mirrors the legacy
// web-app "PROGRAMS" section. Also used by the public marketing homepage
// (no session), so it stays open — but for a logged-in, non-staff (student)
// session, it narrows to only the programs they're actually assigned to
// (programMemberships), per the Program+Center+Section assignment model.
// Anonymous visitors and staff still see the full catalog.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const session = await sessionFromReq(req);
    const filter: Record<string, unknown> = { orgId, recordState: "ACTIVE" };

    if (session && !isStaff(session.profile) && ObjectId.isValid(session.id)) {
      const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(session.id) });
      const memberships: Array<{ programId: string }> = Array.isArray(member?.programMemberships)
        ? member.programMemberships
        : [];
      const assignedIds = memberships.map((m) => m.programId).filter(ObjectId.isValid);
      filter._id = { $in: assignedIds.map((id) => new ObjectId(id)) };
    }

    const docs = await db
      .collection("orgprograms")
      .find(filter)
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
