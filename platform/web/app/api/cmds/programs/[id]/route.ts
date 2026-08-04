import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";

// Program detail — program doc + its centers/sections + tab counts. Mirrors the
// legacy program dashboard (org-services :19012 getPrograms/getProgramCenters).
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
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

    const sectionDocs = await db
      .collection("orgsections")
      .find({ orgId, recordState: "ACTIVE" })
      .toArray();

    // Resolve section.courseIds -> {id, name} so admins can see, right here,
    // which whole course folders (subjects) a section's students actually
    // get via "My Courses" — a real point of confusion found live: this
    // page's "Content" tab only ever showed loose files individually added
    // to the section, with no visibility at all into the separate, larger
    // course-folder grant that drives the student's actual course list.
    const allCourseIds = Array.from(
      new Set(sectionDocs.flatMap((s: any) => (Array.isArray(s.courseIds) ? s.courseIds : [])))
    ).filter((cid: string) => ObjectId.isValid(cid));
    const courseFolders = allCourseIds.length
      ? await db
          .collection("folders")
          .find({ _id: { $in: allCourseIds.map((cid) => new ObjectId(cid)) } })
          .project({ name: 1 })
          .toArray()
      : [];
    const courseNameById = new Map(courseFolders.map((f: any) => [String(f._id), f.name || "(untitled course)"]));

    const sections = sectionDocs.map((s: any) => ({
      id: String(s._id),
      name: s.name,
      centerId: s.centerId || null,
      programId: s.programId || null,
      courses: (Array.isArray(s.courseIds) ? s.courseIds : []).map((cid: string) => ({
        id: cid,
        name: courseNameById.get(cid) || "(unknown course)",
      })),
    }));

    // Tab counts — scoped to members actually assigned to THIS program
    // (orgmembers.programMemberships[].programId), not every org member.
    // Bug found live: this used to count/list every active org student
    // regardless of assignment, so a brand-new program showed the whole
    // org's roster under "Students" even though the student's own login
    // correctly showed no enrollment there.
    const programMemberFilter = { orgId, recordState: "ACTIVE", "programMemberships.programId": id };

    // Content count had the exact same bug, just never caught: it counted
    // every active document/video/test/module in the ORG, unscoped to this
    // program at all — a brand-new program with 2 items actually added to
    // its sections showed the whole org's library size (e.g. "5104") next
    // to "Content", wildly disagreeing with the table right below it, which
    // correctly scopes by sectionId (see ContentTab's `?sectionId=` fetch).
    // Match that: count content whose sectionIds intersects any section
    // that actually belongs to this program.
    const programSectionIds = sectionDocs
      .filter((s: any) => s.programId === id)
      .map((s: any) => String(s._id));
    const [teacherCount, studentCount, contentCount] = await Promise.all([
      db.collection("orgmembers").countDocuments({ ...programMemberFilter, profile: "TEACHER" }),
      db.collection("orgmembers").countDocuments({ ...programMemberFilter, profile: "STUDENT" }),
      programSectionIds.length === 0
        ? Promise.resolve(0)
        : Promise.all(
            ["tests", "modules", "documents", "videos"].map((c) =>
              db.collection(c).countDocuments({
                "contentSrc.id": orgId,
                recordState: "ACTIVE",
                sectionIds: { $in: programSectionIds },
              })
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
