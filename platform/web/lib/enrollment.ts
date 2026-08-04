import type { Db } from "mongodb";
import { ObjectId } from "mongodb";

// A student's course access is the union of three independent grants:
//   1. enrolledCourseIds  — direct manual override on the member doc (Assign
//      Courses tool, checkout/coupon/enroll-code flows).
//   2. Program.courseIds  — the Program-wide grant.
//   3. Section.courseIds  — a finer-grained override on top of the Program
//      (real production data showed this is actually the PRIMARY path: a
//      Program's own courseIds can be empty while its Section carries the
//      real list).
// This was previously duplicated (and had drifted out of sync) between
// /api/learn/courses and /api/library — factored out so every student-facing
// surface computes the exact same access set.
export type StudentEnrollment = {
  enrolledRoots: string[];
  studentProgramIds: string[];
  studentSectionIds: string[];
  // centerName/sectionName mirror legacy's real "Program, Center, Batch"
  // identity for a Program card (see Institute.getMySections /
  // categorySections.html) — legacy shows no progress/chapter data on this
  // card at all, only that triple, so that's what we surface too.
  programGroups: {
    id: string;
    name: string;
    courseIds: string[];
    centerName: string | null;
    sectionName: string | null;
  }[];
};

export async function resolveStudentEnrollment(
  db: Db,
  memberId: string,
  allCourseRoots: string[]
): Promise<StudentEnrollment> {
  const m: any = ObjectId.isValid(memberId)
    ? await db.collection("orgmembers").findOne({ _id: new ObjectId(memberId) }).catch(() => null)
    : null;

  const directIds: string[] = Array.isArray(m?.enrolledCourseIds) ? m.enrolledCourseIds : [];
  const memberships: Array<{ programId: string; centerId?: string; sectionId?: string }> = Array.isArray(
    m?.programMemberships
  )
    ? m.programMemberships
    : [];
  const studentProgramIds = memberships.map((mm) => mm.programId).filter(ObjectId.isValid);
  const studentSectionIds = memberships
    .map((mm) => mm.sectionId)
    .filter((id): id is string => !!id && ObjectId.isValid(id));
  const studentCenterIds = memberships
    .map((mm) => mm.centerId)
    .filter((id): id is string => !!id && ObjectId.isValid(id));
  // First membership entry that names a given program wins for display — a
  // student assigned to the same program at two centers is an edge case we
  // don't need to represent as two cards.
  const membershipByProgramId = new Map<string, { centerId?: string; sectionId?: string }>();
  for (const mm of memberships) {
    if (mm.programId && !membershipByProgramId.has(mm.programId)) membershipByProgramId.set(mm.programId, mm);
  }

  const programDocs: any[] =
    studentProgramIds.length > 0
      ? await db
          .collection("orgprograms")
          .find({ _id: { $in: studentProgramIds.map((id) => new ObjectId(id)) } })
          .toArray()
      : [];
  const sectionDocs: any[] =
    studentSectionIds.length > 0
      ? await db
          .collection("orgsections")
          .find({ _id: { $in: studentSectionIds.map((id) => new ObjectId(id)) } })
          .toArray()
      : [];
  const centerDocs: any[] =
    studentCenterIds.length > 0
      ? await db
          .collection("orgcenters")
          .find({ _id: { $in: studentCenterIds.map((id) => new ObjectId(id)) } })
          .toArray()
      : [];
  const sectionNameById = new Map(sectionDocs.map((s) => [String(s._id), s.name || null]));
  const centerNameById = new Map(centerDocs.map((c) => [String(c._id), c.name || null]));

  const programCourseIdSets = new Map<string, Set<string>>();
  for (const d of programDocs) {
    programCourseIdSets.set(String(d._id), new Set(Array.isArray(d.courseIds) ? d.courseIds : []));
  }
  for (const s of sectionDocs) {
    const pid = s.programId ? String(s.programId) : null;
    if (!pid || !programCourseIdSets.has(pid)) continue;
    for (const cid of Array.isArray(s.courseIds) ? s.courseIds : []) programCourseIdSets.get(pid)!.add(cid);
  }
  const programGroups = programDocs
    .map((d) => {
      const pid = String(d._id);
      const mm = membershipByProgramId.get(pid);
      return {
        id: pid,
        name: d.name || "(untitled program)",
        courseIds: Array.from(programCourseIdSets.get(pid) || []).filter((id) => allCourseRoots.includes(id)),
        centerName: (mm?.centerId && centerNameById.get(mm.centerId)) || null,
        sectionName: (mm?.sectionId && sectionNameById.get(mm.sectionId)) || null,
      };
    })
    .filter((g) => g.courseIds.length > 0);

  const programCourseIds = programDocs.flatMap((d) => (Array.isArray(d.courseIds) ? d.courseIds : []));
  const sectionCourseIds = sectionDocs.flatMap((d) => (Array.isArray(d.courseIds) ? d.courseIds : []));
  const ids = Array.from(new Set([...directIds, ...programCourseIds, ...sectionCourseIds]));
  const enrolledRoots = ids.filter((id) => allCourseRoots.includes(id));

  return { enrolledRoots, studentProgramIds, studentSectionIds, programGroups };
}
