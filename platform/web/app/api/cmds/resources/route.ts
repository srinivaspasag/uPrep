import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";
import { resolveBoardNames } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";

// Lists authored CMDS resources (questions / tests / modules) for an org directly
// from MongoDB. These are the *authoring* collections (cmds*) that feed the
// publish pipeline; the published/library docs live in questions/tests/modules.
// Direct Mongo read mirrors the library route (legacy search endpoints unindexed).

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s
    .replace(/<[^>]*>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function hasAnswerKey(solutionInfo: any): boolean {
  const a = solutionInfo?.answer;
  if (a == null) return false;
  if (Array.isArray(a)) return a.length > 0;
  return String(a).length > 0;
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const kind = req.nextUrl.searchParams.get("kind") || "all"; // question | test | module | all
  const boardIds = (req.nextUrl.searchParams.get("boardIds") || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  try {
    const db = await getDb();
    const filter: Record<string, any> = { "contentSrc.id": orgId, recordState: "ACTIVE" };

    const out: {
      questions: any[];
      tests: any[];
      modules: any[];
    } = { questions: [], tests: [], modules: [] };

    if (kind === "question" || kind === "all") {
      const qFilter = boardIds.length ? { ...filter, boardIds: { $in: boardIds } } : filter;
      const docs = await db
        .collection("cmdsquestions")
        .find(qFilter)
        .sort({ lastUpdated: -1 })
        .limit(200)
        .toArray();
      // Each question is tagged with its deepest chapter/topic board id (see
      // lib/legacyBoard.ts's resolveBoardNames) — resolved in one batch call
      // rather than per-question, then attached below.
      const chapterIds = (docs as any[])
        .map((d) => (Array.isArray(d.boardIds) ? d.boardIds[d.boardIds.length - 1] : null))
        .filter(Boolean) as string[];
      const chapterNames = await resolveBoardNames(orgId, chapterIds);

      out.questions = (docs as any[]).map((d) => {
        const chapterId = Array.isArray(d.boardIds) ? d.boardIds[d.boardIds.length - 1] : null;
        return {
          id: String(d._id),
          text: stripHtml(d.questionBody?.newText).slice(0, 160),
          type: d.type || "UNKNOWN",
          difficulty: d.difficulty ?? null,
          published: !!d.published,
          completed: !!d.completed,
          status: d.status || "INCOMPLETE",
          hasKey: hasAnswerKey(d.solutionInfo),
          options: d.solutionInfo?.optionBody?.newOptions?.length ?? 0,
          chapter: chapterId ? chapterNames[chapterId] || null : null,
        };
      });
    }

    if (kind === "test" || kind === "all") {
      const docs = await db
        .collection("cmdstests")
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();
      out.tests = (docs as any[]).map((d) => ({
        id: String(d._id),
        name: d.name || "(untitled test)",
        type: d.type || "TEST",
        // See app/api/library/route.ts — actualQusCount is hardcoded to 0 at
        // creation and never updated, so it must never be preferred over
        // the real qusCount via `??` (0 isn't nullish).
        qusCount: d.qusCount ?? d.actualQusCount ?? 0,
        totalMarks: d.totalMarks ?? 0,
        durationMin: d.duration ? Math.round(d.duration / 60000) : 0,
        published: !!d.published,
        completed: !!d.completed,
      }));
    }

    if (kind === "module" || kind === "all") {
      const docs = await db
        .collection("cmdsmodules")
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();
      out.modules = (docs as any[]).map((d) => ({
        id: String(d._id),
        name: d.name || "(untitled module)",
        contentCount: d.totalContentCount ?? 0,
        published: !!d.published,
        completed: !!d.completed,
      }));
    }

    return NextResponse.json({ ...out, orgId });
  } catch (e: any) {
    return NextResponse.json(
      { questions: [], tests: [], modules: [], error: e?.message || "Failed to load CMDS resources" },
      { status: 500 }
    );
  }
}
