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
  const countOnly = req.nextUrl.searchParams.get("count") === "1";

  try {
    const db = await getDb();
    const filter: Record<string, any> = { "contentSrc.id": orgId, recordState: "ACTIVE" };

    // Listing below caps at 200 for UI display; bulk-import tooling needs a
    // real total (a chapter can legitimately exceed 200 questions), so this
    // mode skips the listing/enrichment entirely and just counts.
    if (countOnly && kind === "question") {
      const qFilter = boardIds.length ? { ...filter, boardIds: { $in: boardIds } } : filter;
      const count = await db.collection("cmdsquestions").countDocuments(qFilter);
      return NextResponse.json({ count });
    }

    const dump = req.nextUrl.searchParams.get("dump") === "1";
    if (dump && kind === "question") {
      const qFilter = boardIds.length ? { ...filter, boardIds: { $in: boardIds } } : filter;
      const docs = await db
        .collection("cmdsquestions")
        .find(qFilter)
        .sort({ lastUpdated: 1 })
        .limit(2000)
        .toArray();
      return NextResponse.json({
        items: (docs as any[]).map((d) => ({
          id: String(d._id),
          text: stripHtml(d.questionBody?.newText),
          lastUpdated: d.lastUpdated,
        })),
      });
    }

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

// One-off restore for a soft-deleted (recordState: INACTIVE) unpublished
// question — undoes the same soft-delete the DELETE handler in
// app/api/cmds/questions/route.ts performs, for correcting a bad bulk
// dedupe run rather than day-to-day use.
export async function POST(req: NextRequest) {
  const { ObjectId } = await import("mongodb");
  const body = (await req.json().catch(() => ({}))) as { restoreId?: string };
  const id = body.restoreId || "";
  if (!id || !ObjectId.isValid(id)) {
    return NextResponse.json({ error: "A valid restoreId is required" }, { status: 400 });
  }
  try {
    const db = await getDb();
    const oid = new ObjectId(id);
    const cq = await db.collection("cmdsquestions").findOne({ _id: oid });
    if (!cq) return NextResponse.json({ error: "Question not found" }, { status: 404 });
    await db.collection("cmdsquestions").updateOne({ _id: oid }, { $set: { recordState: "ACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to restore" }, { status: 500 });
  }
}
