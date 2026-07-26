import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Program membership — assigns a student to a Program + Center + Section
// (the primary assignment path; direct course assignment via /api/cmds/enroll
// stays available as a manual/secondary override, see app/cmds/tools/enroll).
//
//   GET ?memberId=<orgmember _id>                       -> { memberships }
//   POST { memberId, programId, centerId, sectionId }   -> upsert one entry
//   DELETE ?memberId=&programId=                        -> remove one entry
type Membership = {
  programId: string;
  centerId: string;
  sectionId: string;
  assignedAt: number;
};

export async function GET(req: NextRequest) {
  const memberId = req.nextUrl.searchParams.get("memberId") || "";
  if (!ObjectId.isValid(memberId))
    return NextResponse.json({ error: "Invalid memberId" }, { status: 400 });

  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });
    const memberships: Membership[] = Array.isArray(member.programMemberships)
      ? member.programMemberships
      : [];
    return NextResponse.json({ memberships });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { memberId?: string; programId?: string; centerId?: string; sectionId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as SaveBody;
  const memberId = String(b.memberId || "");
  const programId = String(b.programId || "");
  const centerId = String(b.centerId || "");
  const sectionId = String(b.sectionId || "");
  if (![memberId, programId, centerId, sectionId].every(ObjectId.isValid))
    return NextResponse.json(
      { error: "memberId, programId, centerId and sectionId are all required" },
      { status: 400 }
    );

  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    // Program/center/section must exist and belong to the member's own org.
    const [program, center, section] = await Promise.all([
      db.collection("orgprograms").findOne({ _id: new ObjectId(programId), orgId: member.orgId }),
      db.collection("orgcenters").findOne({ _id: new ObjectId(centerId), orgId: member.orgId }),
      db.collection("orgsections").findOne({ _id: new ObjectId(sectionId), orgId: member.orgId }),
    ]);
    if (!program || !center || !section)
      return NextResponse.json({ error: "Program, center or section not found" }, { status: 404 });

    const existing: Membership[] = Array.isArray(member.programMemberships)
      ? member.programMemberships
      : [];
    const next = [
      ...existing.filter((m) => m.programId !== programId),
      { programId, centerId, sectionId, assignedAt: Date.now() },
    ];

    await db
      .collection("orgmembers")
      .updateOne({ _id: member._id }, { $set: { programMemberships: next, lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true, memberships: next });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const memberId = req.nextUrl.searchParams.get("memberId") || "";
  const programId = req.nextUrl.searchParams.get("programId") || "";
  if (!ObjectId.isValid(memberId) || !programId)
    return NextResponse.json({ error: "memberId and programId required" }, { status: 400 });

  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });
    const existing: Membership[] = Array.isArray(member.programMemberships)
      ? member.programMemberships
      : [];
    const next = existing.filter((m) => m.programId !== programId);
    await db
      .collection("orgmembers")
      .updateOne({ _id: member._id }, { $set: { programMemberships: next, lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true, memberships: next });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Remove failed" }, { status: 500 });
  }
}
