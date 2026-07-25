import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { SECTIONS_COLL } from "@/lib/sections";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { code?: string };

// Redeem a section access code: joins the section and enrolls the student in
// the section's courses. Scoped to the student's own org.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.id || !ObjectId.isValid(session.id))
    return NextResponse.json({ error: "Please sign in first" }, { status: 401 });

  const b = (await req.json().catch(() => ({}))) as Body;
  const code = (b.code || "").trim().toUpperCase();
  if (!code) return NextResponse.json({ error: "Enter an access code" }, { status: 400 });

  try {
    const db = await getDb();
    const section: any = await db
      .collection(SECTIONS_COLL)
      .findOne({ code, recordState: "ACTIVE" } as any);
    if (!section) return NextResponse.json({ error: "Invalid or expired code" }, { status: 404 });

    const member: any = await db
      .collection("orgmembers")
      .findOne({ _id: new ObjectId(session.id) });
    if (!member) return NextResponse.json({ error: "Account not found" }, { status: 404 });

    if (member.orgId !== section.orgId)
      return NextResponse.json(
        { error: "This code belongs to a different institute." },
        { status: 403 }
      );

    const sectionId = String(section._id);
    const mappings = Array.isArray(member.mappings) ? member.mappings : [];
    const already = mappings.some((m: any) => String(m.sectionId) === sectionId);
    const newMappings = already ? mappings : [...mappings, { sectionId, joinedAt: Date.now() }];

    const enrolled = new Set(
      (Array.isArray(member.enrolledCourseIds) ? member.enrolledCourseIds : []).map(String)
    );
    for (const c of section.courseIds || []) enrolled.add(String(c));

    await db.collection("orgmembers").updateOne(
      { _id: member._id },
      {
        $set: {
          mappings: newMappings,
          enrolledCourseIds: Array.from(enrolled),
          lastUpdated: Date.now(),
        },
      }
    );

    return NextResponse.json({
      ok: true,
      section: { id: sectionId, name: section.name },
      addedCourses: (section.courseIds || []).length,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Could not redeem code" }, { status: 500 });
  }
}
