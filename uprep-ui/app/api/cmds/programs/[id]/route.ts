import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Program detail — program doc + its centers/sections + tab counts. Mirrors the
// legacy program dashboard (org-services :19012 getPrograms/getProgramCenters).
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const id = params.id;

  try {
    const db = await getDb();
    let oid: any = id;
    try {
      oid = new ObjectId(id);
    } catch {
      /* keep string */
    }

    const program: any =
      (await db.collection("orgprograms").findOne({ _id: oid })) ||
      (await db.collection("orgprograms").findOne({ _id: id }));
    if (!program) return NextResponse.json({ error: "Program not found" }, { status: 404 });

    const centers = (
      await db.collection("orgcenters").find({ orgId, recordState: "ACTIVE" }).toArray()
    ).map((c: any) => ({ id: String(c._id), name: c.name }));

    const sections = (
      await db
        .collection("orgsections")
        .find({ orgId, recordState: "ACTIVE" })
        .toArray()
    ).map((s: any) => ({
      id: String(s._id),
      name: s.name,
      centerId: s.centerId || null,
      programId: s.programId || null,
    }));

    // Tab counts.
    const [teacherCount, studentCount, contentCount] = await Promise.all([
      db.collection("orgmembers").countDocuments({ orgId, profile: "TEACHER", recordState: "ACTIVE" }),
      db.collection("orgmembers").countDocuments({ orgId, profile: "STUDENT", recordState: "ACTIVE" }),
      Promise.all(
        ["tests", "modules", "documents", "videos"].map((c) =>
          db.collection(c).countDocuments({ "contentSrc.id": orgId, recordState: "ACTIVE" })
        )
      ).then((arr) => arr.reduce((a, b) => a + b, 0)),
    ]);

    return NextResponse.json({
      program: {
        id: String(program._id),
        name: program.name || "(untitled)",
        code: program.code || null,
        description: program.description || "",
        isOffline: !!program.isOffline,
      },
      centers,
      sections,
      counts: { teachers: teacherCount, students: studentCount, content: contentCount },
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message }, { status: 500 });
  }
}
