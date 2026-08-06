import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { resolveBoardNames } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Multi-subject Instant Test Generator — the real Mongo-query substitute for
// legacy's Elasticsearch-backed generator (see the plan doc). Given a flat
// list of "selections" (subject -> chapters -> type -> count, optionally
// split by difficulty, optionally including unpublished drafts), picks
// random matching questions per selection and returns them grouped by
// subject for the review step — does NOT create the test yet.

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s.replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(/\s+/g, " ").trim();
}

function shuffle<T>(arr: T[]): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

type PublishedFilter = "PUBLISHED" | "UNPUBLISHED" | "BOTH";

type DiffCount = { level: string; count: number };

type Selection = {
  subjectBoardId: string;
  subjectName: string;
  // Which admin-defined Section (see app/cmds/tests/new/page.tsx) this
  // selection's picks belong to. Two sections of the same subject can both
  // want, say, SCQ — without this, there'd be no way to tell the resulting
  // questions apart, and no way to stop both selections drawing the exact
  // same question (each was excluding only its OWN excludeIds, blind to
  // what a sibling selection for the same subject had just picked).
  sectionId: string;
  chapterBoardIds: string[];
  type: string;
  count: number;
  positive: number;
  negative: number;
  publishedFilter?: PublishedFilter;
  difficulty?: DiffCount[];
  excludeIds?: string[]; // used by the "replace one question" mode
};

type Pool = {
  id: string;
  text: string;
  type: string;
  difficulty: string | null;
  published: boolean;
  sectionId?: string;
  boardId?: string | null;
  chapter?: string | null;
};

async function fetchPool(
  db: any,
  orgId: string,
  s: Selection,
  difficulty?: string
): Promise<Pool[]> {
  const filter = s.publishedFilter || "PUBLISHED";
  const exclude = (s.excludeIds || []).filter(ObjectId.isValid).map((id) => new ObjectId(id));
  const baseMatch: any = {
    boardIds: { $in: s.chapterBoardIds },
    type: s.type,
    ...(difficulty ? { difficulty } : {}),
    ...(exclude.length ? { _id: { $nin: exclude } } : {}),
  };

  const pools: Pool[] = [];

  if (filter === "PUBLISHED" || filter === "BOTH") {
    const docs = await db
      .collection("questions")
      .find({ ...baseMatch, "contentSrc.id": orgId, recordState: "ACTIVE" })
      .limit(500)
      .toArray();
    for (const q of docs as any[]) {
      pools.push({
        id: String(q._id),
        text: stripHtml(q.content),
        type: q.type || "SCQ",
        difficulty: q.difficulty || null,
        published: true,
        boardId: Array.isArray(q.boardIds) ? q.boardIds[q.boardIds.length - 1] : null,
      });
    }
  }
  if (filter === "UNPUBLISHED" || filter === "BOTH") {
    const docs = await db
      .collection("cmdsquestions")
      .find({ ...baseMatch, "contentSrc.id": orgId, recordState: "ACTIVE", published: false })
      .limit(500)
      .toArray();
    for (const q of docs as any[]) {
      pools.push({
        id: String(q._id),
        text: stripHtml(q.questionBody?.newText),
        type: q.type || "SCQ",
        difficulty: q.difficulty || null,
        published: false,
        boardId: Array.isArray(q.boardIds) ? q.boardIds[q.boardIds.length - 1] : null,
      });
    }
  }
  return pools;
}

async function pickForSelection(db: any, orgId: string, s: Selection) {
  const picked: Pool[] = [];
  const usedIds = new Set<string>(s.excludeIds || []);

  // Difficulty buckets are a SPLIT of the type's requested count (see
  // app/cmds/tests/new/page.tsx's Difficulty step), never an addition to
  // it — the UI is expected to clamp each bucket so the split can't exceed
  // s.count, but that's client-side only, so re-clamp here too: walk the
  // buckets in order and stop handing out budget once s.count is spent.
  let budget = s.count;
  const buckets: DiffCount[] =
    Array.isArray(s.difficulty) && s.difficulty.length > 0
      ? s.difficulty
          .filter((d) => d.count > 0)
          .map((d) => {
            const take = Math.min(d.count, budget);
            budget -= take;
            return { level: d.level, count: take };
          })
          .filter((d) => d.count > 0)
      : [{ level: "", count: s.count }];

  for (const bucket of buckets) {
    const pool = await fetchPool(
      db,
      orgId,
      { ...s, excludeIds: Array.from(usedIds) },
      bucket.level || undefined
    );
    const shuffled = shuffle(pool.filter((p) => !usedIds.has(p.id)));
    const take = shuffled.slice(0, bucket.count);
    for (const q of take) usedIds.add(q.id);
    picked.push(...take);
  }
  return picked;
}

export async function POST(req: NextRequest) {
  const orgId = await resolveOrgId(req, null);
  const body = (await req.json().catch(() => ({}))) as { selections?: Selection[]; replace?: Selection };

  try {
    const db = await getDb();

    // Replace mode — one selection, return one fresh matching question.
    if (body.replace) {
      const picked = await pickForSelection(db, orgId, { ...body.replace, count: 1 });
      if (picked.length === 0)
        return NextResponse.json({ error: "No matching replacement question found" }, { status: 404 });
      const q = picked[0];
      const names = q.boardId ? await resolveBoardNames(orgId, [q.boardId]) : {};
      return NextResponse.json({ question: { ...q, chapter: q.boardId ? names[q.boardId] || null : null } });
    }

    const selections = Array.isArray(body.selections) ? body.selections : [];
    if (selections.length === 0)
      return NextResponse.json({ error: "No selections provided" }, { status: 400 });

    const subjects: {
      subjectBoardId: string;
      subjectName: string;
      questions: Pool[];
      requested: number;
    }[] = [];
    const bySubject = new Map<string, (typeof subjects)[number]>();
    // Grows as selections for the SAME subject are processed, so a second
    // section requesting the same type never draws a question the first
    // section already claimed.
    const usedIdsBySubject = new Map<string, Set<string>>();

    for (const s of selections) {
      const usedForSubject = usedIdsBySubject.get(s.subjectBoardId) || new Set<string>();
      const picked = await pickForSelection(db, orgId, {
        ...s,
        excludeIds: [...(s.excludeIds || []), ...usedForSubject],
      });
      for (const q of picked) usedForSubject.add(q.id);
      usedIdsBySubject.set(s.subjectBoardId, usedForSubject);

      let entry = bySubject.get(s.subjectBoardId);
      if (!entry) {
        entry = { subjectBoardId: s.subjectBoardId, subjectName: s.subjectName, questions: [], requested: 0 };
        bySubject.set(s.subjectBoardId, entry);
        subjects.push(entry);
      }
      entry.questions.push(...picked.map((q) => ({ ...q, sectionId: s.sectionId })));
      entry.requested += s.count;
    }

    // One batched name lookup across every picked question (see
    // lib/legacyBoard.ts's resolveBoardNames) rather than one call per
    // question — powers the chapter label shown in the Generate & Review
    // step.
    const allBoardIds = subjects.flatMap((s) => s.questions.map((q) => q.boardId)).filter(Boolean) as string[];
    const chapterNames = await resolveBoardNames(orgId, allBoardIds);
    for (const s of subjects) {
      for (const q of s.questions) {
        q.chapter = q.boardId ? chapterNames[q.boardId] || null : null;
      }
    }

    return NextResponse.json({ subjects });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Generate failed" }, { status: 500 });
  }
}
