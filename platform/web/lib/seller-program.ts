import { ObjectId } from "mongodb";
import { loadOrgFolders, collectSubtreeIds } from "@/lib/courses";

// Every content collection a group can pull from — same set app/api/cmds/content
// aggregates across, minus FOLDER (never a real downloadable item).
const CONTENT_COLLECTIONS = ["documents", "videos", "books", "tests", "modules", "questionsets"];

// Resolve every content item belonging to a Program. Two independent paths,
// unioned together:
//
// Path 1 — explicit tagging (sectionIds / visibleProgramIds on the content
// doc itself). A small, deliberately-curated set some content may carry.
//
// Path 2 — course-folder access, which is what ACTUALLY gates what an
// enrolled student sees (app/api/learn/courses/route.ts, via
// lib/enrollment.ts's resolveStudentEnrollment): a Program's `courseIds`
// (unioned with its Sections' `courseIds` — same fold-in logic, since real
// data showed the Program's own list is often empty and the Section carries
// the real one) grants access to those folders' entire subtree, and content
// is filed there via `folderId` — a completely different field than
// `sectionIds`. Missing this path was a real bug found live: "Pack a whole
// Program" produced near-empty packages (2-9 items) for programs whose real
// content — thousands of videos/documents — lives here, the common case.
// `sectionIds`/`visibleProgramIds` tagging turned out to be the rare
// exception, not the norm.
//
// This is a point-in-time snapshot either way, matching how a manually-picked
// group already behaves — a card that's already been burned and shipped
// doesn't retroactively gain content added to the program afterward.
export type CourseGroup = { courseId: string; courseName: string; itemIds: string[] };
type ProgramResolution = { programName: string; contentIds: string[]; courseGroups: CourseGroup[] };

// Same two-path resolution as before, but now also tracks which top-level
// Course each item came from — so the SD-card manifest (and the Android
// reader's UI) can show Program → Courses → Content instead of one flat
// file list, matching what a student actually sees browsing Learn → Courses
// online. Items reached only via Path 1 (sectionIds/visibleProgramIds
// tagging, no folderId) have no course to attach to and are left out of
// courseGroups — the flat contentIds list (unchanged) still includes them,
// so nothing is silently dropped, they just don't get a course bucket.
export async function resolveProgramContentIds(db: any, orgId: string, programId: string): Promise<ProgramResolution> {
  const [program, sections] = await Promise.all([
    db.collection("orgprograms").findOne({ _id: new ObjectId(programId) } as any),
    db.collection("orgsections").find({ orgId, programId, recordState: "ACTIVE" } as any).toArray(),
  ]);
  const sectionIds = (sections as any[]).map((s) => String(s._id));
  const programName = program?.name || "(untitled program)";

  const ids = new Set<string>();

  for (const coll of CONTENT_COLLECTIONS) {
    const docs = await db
      .collection(coll)
      .find({
        "contentSrc.id": orgId,
        recordState: "ACTIVE",
        $or: [
          ...(sectionIds.length ? [{ sectionIds: { $in: sectionIds } }] : []),
          { visibleProgramIds: programId },
        ],
      } as any)
      .project({ _id: 1 })
      .toArray();
    for (const d of docs as any[]) ids.add(String(d._id));
  }

  const courseIds = new Set<string>(Array.isArray(program?.courseIds) ? program.courseIds : []);
  for (const s of sections as any[]) {
    for (const cid of Array.isArray(s.courseIds) ? s.courseIds : []) courseIds.add(cid);
  }

  const courseGroups: CourseGroup[] = [];
  if (courseIds.size > 0) {
    const folders = await loadOrgFolders(db, orgId);
    const folderNameById = new Map(folders.map((f) => [f.id, f.name]));
    for (const courseId of Array.from(courseIds)) {
      const subtree = Array.from(collectSubtreeIds(folders, [courseId]));
      if (subtree.length === 0) continue;
      const itemIds: string[] = [];
      for (const coll of ["documents", "videos", "books", "tests"]) {
        const docs = await db
          .collection(coll)
          .find({ folderId: { $in: subtree }, recordState: "ACTIVE", hidden: { $ne: true } } as any)
          .project({ _id: 1 })
          .toArray();
        for (const d of docs as any[]) {
          const sid = String(d._id);
          ids.add(sid);
          itemIds.push(sid);
        }
      }
      if (itemIds.length > 0) {
        courseGroups.push({ courseId, courseName: folderNameById.get(courseId) || "(untitled course)", itemIds });
      }
    }
  }

  return { programName, contentIds: Array.from(ids), courseGroups };
}
