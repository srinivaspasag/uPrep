import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import {
  loadFoldersForOrgs,
  collectSubtreeIds,
  collectOwnSubtreeIds,
  courseRootOf,
  orderThenNatural,
  naturalCompare,
} from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";
import { resolveStudentEnrollment, resolveAllProgramGroups } from "@/lib/enrollment";
import { loadPlacedContent, placedGrantFor, PLACED_PUBLISHED, type PlacedItem } from "@/lib/tagged-content";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// This response depends entirely on the caller's auth cookie (a student and
// staff hitting the same URL get different course lists) — without an
// explicit no-store, a shared/browser cache has no signal that it must not
// reuse one caller's response for another.
const NO_STORE = { "Cache-Control": "no-store, private", Vary: "Cookie" };

// Student-facing course access. Enforces enrollment server-side:
//   GET                 -> the courses the caller is enrolled in
//   GET ?folderId=<id>  -> contents of a folder, ONLY if it lives inside a
//                          course the caller is enrolled in (staff see all).
//
// Access is derived from the server-trusted session cookie, not client input,
// so a student cannot read another course by guessing folder ids.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session)
    return NextResponse.json({ error: "Please log in", courses: [] }, { status: 401 });

  const orgId = session.orgId;
  const staff = isStaff(session.profile);
  const folderId = req.nextUrl.searchParams.get("folderId");

  try {
    const db = await getDb();
    // Catalog = the org's own courses + courses granted to it. Folders are
    // loaded across every owner org so granted subtrees resolve correctly.
    const catalog = await resolveCourseCatalog(db, orgId);
    const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
    const allCourseRoots = catalog.map((c) => c.id);

    // Enrolled course roots (staff preview everything). A student's access is
    // the union of direct course assignment, Program grant, and Section grant
    // — see resolveStudentEnrollment for the full union logic (shared with
    // /api/library so both surfaces agree on what a student can see).
    let enrolledRoots: string[];
    // Roots this caller may only see the institute's OWN content in (reached
    // solely via Programs flagged ownContent — see lib/enrollment.ts).
    let ownOnlyRoots: string[] = [];
    let studentProgramIds: string[] = []; // used below to scope content visibility too
    let studentSectionIds: string[] = []; // ditto, for section-level publishing
    let programGroups: {
      id: string;
      name: string;
      courseIds: string[];
      centerName: string | null;
      sectionName: string | null;
      ownContent: boolean;
    }[] = [];
    if (staff) {
      enrolledRoots = allCourseRoots;
      // Staff have no personal enrollment to group by — without this, the
      // Learning Network's Programs page showed nothing at all for a staff/
      // super-admin account. Show every real program in the org instead.
      programGroups = await resolveAllProgramGroups(db, orgId, allCourseRoots);
    } else {
      const enrollment = await resolveStudentEnrollment(db, session.id, allCourseRoots);
      enrolledRoots = enrollment.enrolledRoots;
      ownOnlyRoots = enrollment.ownOnlyRoots;
      studentProgramIds = enrollment.studentProgramIds;
      studentSectionIds = enrollment.studentSectionIds;
      programGroups = enrollment.programGroups;
    }
    // A program with zero courses attached (nothing locally created, nothing
    // granted+assigned yet) has nothing to show — found live: a freshly
    // created program showed up as an empty "0 subjects · 0 chapters" card,
    // which reads as broken rather than "not set up yet." Hide it from
    // Learning Network until at least one course is actually on it; it still
    // shows everywhere else (CMDS Programs list, Academic Structure) so
    // staff can keep configuring it.
    programGroups = programGroups.filter((g) => g.courseIds.length > 0);

    // The institute's own board-tagged content published to this student
    // but filed outside the subject tree — shown inside the subject/chapter
    // it's tagged with (lib/tagged-content.ts). Always own content, so it
    // shows in own-content views too.
    const placed = await loadPlacedContent(db, {
      orgId,
      folders,
      rootIds: enrolledRoots,
      grant: staff ? PLACED_PUBLISHED : placedGrantFor(session.id, studentProgramIds, studentSectionIds),
      ownerOrgIds: catalogOwnerOrgs(orgId, catalog),
    });
    const placedCounts = (items: PlacedItem[]) => ({
      videoCount: items.filter((p) => p.coll === "videos").length,
      documentCount: items.filter((p) => p.coll === "documents").length,
      bookCount: items.filter((p) => p.coll === "books").length,
      testCount: items.filter((p) => p.coll === "tests").length,
    });

    // Browse mode: list one folder's children, gated by enrollment.
    if (folderId) {
      const root = courseRootOf(folders, folderId);
      if (!root || !enrolledRoots.includes(root))
        return NextResponse.json({ error: "Not enrolled in this course" }, { status: 403 });

      // Own-content view: forced for a student who reaches this subject only
      // through own-content programs; requested (?own=1) when the subject was
      // opened from an own-content program's card (staff preview, or a student
      // who also has it via a normal program). Only the institute's own
      // folders/items, never the provider's.
      const ownView = ownOnlyRoots.includes(root) || req.nextUrl.searchParams.get("own") === "1";
      const ownFolderIds = ownView ? collectOwnSubtreeIds(folders, [root], orgId) : null;
      // A shared chapter holding the institute's tagged content is listed in
      // the own view too — showing only that content (see `ownItemsOnly`).
      const placedHere = placed.filter((p) => p.rootId === root);
      if (ownFolderIds) for (const p of placedHere) ownFolderIds.add(p.folderId);
      if (ownFolderIds && !ownFolderIds.has(folderId))
        return NextResponse.json({ error: "Not enrolled in this course" }, { status: 403 });
      const subtreeOf = (id: string) =>
        Array.from(ownFolderIds ? collectOwnSubtreeIds(folders, [id], orgId) : collectSubtreeIds(folders, [id]));
      const placedUnder = (id: string) => {
        const sub = collectSubtreeIds(folders, [id]);
        return placedHere.filter((p) => sub.has(p.folderId));
      };

      const rawSubfolders = orderThenNatural(
        folders.filter((f) => f.parentId === folderId && (!ownFolderIds || ownFolderIds.has(f.id)))
      );

      // Students only see content marked visible (hidden !== true). On top of
      // that, an item is visible if it has NO scoping at all (today's default),
      // OR the student's program is in its program allow-list, OR the student's
      // section is in its published-sections list (visibleSectionIds — the
      // section-level publish workflow: being added to a section does not
      // itself make content visible, only an explicit "Make Visible" does),
      // OR this specific student has an individual "publish to a student"
      // override (visibleUserIds) — and hiddenUserIds excludes this specific
      // student even if everything else says visible (an individual
      // "unpublish from a student" override). Staff see all.
      const visClause = staff
        ? {}
        : {
            hidden: { $ne: true },
            hiddenUserIds: { $nin: [session.id] },
            $or: [
              { visibleUserIds: { $in: [session.id] } },
              { visibleProgramIds: { $exists: false } },
              { visibleProgramIds: { $size: 0 } },
              { visibleProgramIds: { $in: studentProgramIds } },
              { visibleSectionIds: { $in: studentSectionIds } },
            ],
          };
      // Legacy's real chapter row (Library/tags/library/chapter.html) shows
      // Lectures/E-Books/Tests counts right on the row — ours used to be a
      // bare "📁 name" folder tile with no indication of what's inside until
      // you clicked in. Mirrors the same subtree-count approach already used
      // for the top-level subject cards.
      const subfolders = await Promise.all(
        rawSubfolders.map(async (f) => {
          const subtree = subtreeOf(f.id);
          const [videoCount, documentCount, bookCount, testCount] = await Promise.all([
            db.collection("videos").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("documents").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("books").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("tests").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
          ]);
          const extra = placedCounts(placedUnder(f.id));
          return {
            id: f.id,
            name: f.name,
            type: "FOLDER" as const,
            videoCount: Number(videoCount) + extra.videoCount,
            documentCount: Number(documentCount) + extra.documentCount,
            bookCount: Number(bookCount) + extra.bookCount,
            testCount: Number(testCount) + extra.testCount,
          };
        })
      );

      // A linked subject also shows what's filed directly in the provider
      // subject it mirrors (see lib/courses.ts "Linked subjects").
      const linkedTo = folders.find((f) => f.id === folderId)?.linkedCourseId;
      const folderIds = linkedTo && !ownView ? [folderId, linkedTo] : [folderId];
      // Own view of a shared chapter: only the institute's own items in it.
      const ownItemsOnly = ownView ? { "contentSrc.id": orgId } : {};
      const contentItems: any[] = [];
      for (const coll of ["videos", "documents", "books", "tests"]) {
        const filed = await db
          .collection(coll)
          .find({ folderId: { $in: folderIds }, recordState: "ACTIVE", ...visClause, ...ownItemsOnly } as any)
          .toArray();
        const docs = filed.concat(placedHere.filter((p) => p.coll === coll && p.folderId === folderId).map((p) => p.doc));
        // Same sequencing bug as folders (e.g. "Part 10" sorting before
        // "Part 2") — explicit `order` wins, otherwise natural sort by name.
        const sorted = (docs as any[]).sort((a, b) => {
          const ao = typeof a.order === "number" ? a.order : null;
          const bo = typeof b.order === "number" ? b.order : null;
          if (ao !== null && bo !== null) return ao - bo;
          if (ao !== null) return -1;
          if (bo !== null) return 1;
          return naturalCompare(String(a.name || a.title || ""), String(b.name || b.title || ""));
        });
        for (const d of sorted) {
          contentItems.push({
            id: String(d._id),
            name: d.name || d.title || "(untitled)",
            type:
              d.type ||
              (coll === "videos" ? "VIDEO" : coll === "documents" ? "DOCUMENT" : coll === "books" ? "BOOK" : "TEST"),
            url: d.url ?? null,
            embedUrl: d.embedUrl ?? null,
            provider: d.provider ?? null,
          });
        }
      }

      const here = folders.find((f) => f.id === folderId);
      return NextResponse.json(
        {
          folder: here ? { id: here.id, name: here.name, parentId: here.parentId } : null,
          courseRootId: root,
          subfolders,
          items: contentItems,
        },
        { headers: NO_STORE }
      );
    }

    // Course list mode. Legacy's real subject card (Library/subject.html)
    // shows a Chapters / E-Books / Tests count per subject — this mirrors
    // that instead of the bare chapter count the rebuild originally shipped.
    const visClause = staff
      ? {}
      : {
          hidden: { $ne: true },
          hiddenUserIds: { $nin: [session.id] },
          $or: [
            { visibleUserIds: { $in: [session.id] } },
            { visibleProgramIds: { $exists: false } },
            { visibleProgramIds: { $size: 0 } },
            { visibleProgramIds: { $in: studentProgramIds } },
            { visibleSectionIds: { $in: studentSectionIds } },
          ],
        };
    const countIn = async (rootId: string, subtree: string[]) => {
      const [videoCount, documentCount, bookCount, testCount] = await Promise.all(
        ["videos", "documents", "books", "tests"].map((c) =>
          db.collection(c).countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any)
        )
      );
      // Tagged content placed in this subject counts in both views, and the
      // shared chapter it sits in counts as a chapter of the own view too.
      const placedHere = placed.filter((p) => p.rootId === rootId);
      const extra = placedCounts(placedHere);
      const sub = new Set(subtree);
      for (const p of placedHere) if (p.folderId !== rootId) sub.add(p.folderId);
      return {
        chapterCount: folders.filter((x) => x.parentId === rootId && sub.has(x.id)).length,
        folderCount: sub.size,
        videoCount: Number(videoCount) + extra.videoCount,
        documentCount: Number(documentCount) + extra.documentCount,
        bookCount: Number(bookCount) + extra.bookCount,
        testCount: Number(testCount) + extra.testCount,
      };
    };
    // Subjects shown under an own-content program's heading also need the
    // own-content counts (staff preview, or a student who has the subject via
    // a normal program too) — the client uses `own` for those cards.
    const underOwnProgram = new Set(programGroups.filter((g) => g.ownContent).flatMap((g) => g.courseIds));
    const courses = await Promise.all(
      enrolledRoots
        .map((id) => folders.find((f) => f.id === id))
        .filter(Boolean)
        .map(async (f) => {
          const id = f!.id;
          const ownOnly = ownOnlyRoots.includes(id);
          const ownSubtree = () => Array.from(collectOwnSubtreeIds(folders, [id], orgId));
          const counts = await countIn(id, ownOnly ? ownSubtree() : Array.from(collectSubtreeIds(folders, [id])));
          const own = ownOnly ? counts : underOwnProgram.has(id) ? await countIn(id, ownSubtree()) : undefined;
          return { id, name: f!.name, ...counts, own };
        })
    );

    return NextResponse.json({ courses, staff, programGroups }, { headers: NO_STORE });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed", courses: [] }, { status: 500 });
  }
}
