import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveBoardSubjects } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Advanced per-student analytics — the old /api/analytics only ever returned
// a flat list of test results (score, %, date). This adds the breakdown a
// student actually needs to know WHAT to study next: accuracy by subject
// (resolved via the board-service, same mechanism as the Question Bank's
// chapter display — see lib/legacyBoard.ts's resolveBoardSubjects) and by
// question type, plus a score trend and a couple of derived
// strengths/weaknesses. Still built entirely from userentityattempts +
// userquestionattempts, same as before — no new tracking.

function marksMap(test: any): Record<string, { positive: number; negative: number }> {
  const m: Record<string, { positive: number; negative: number }> = {};
  for (const md of test?.metadata || []) {
    if (md?.marks) Object.assign(m, md.marks);
  }
  return m;
}

export async function GET(req: NextRequest) {
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  if (!userId)
    return NextResponse.json({ results: [], trend: [], subjects: [], types: [], summary: null });

  try {
    const db = await getDb();

    const attempts: any[] = await db
      .collection("userentityattempts")
      .find({ userId, orgId, "entity.type": "TEST", finished: true })
      .sort({ endTime: 1 })
      .toArray();

    if (attempts.length === 0)
      return NextResponse.json({ results: [], trend: [], subjects: [], types: [], summary: null });

    const testIds = Array.from(new Set(attempts.map((a) => a.entity?.id).filter(Boolean)));
    const testOids = testIds
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const tests = await db.collection("tests").find({ _id: { $in: testOids } }).toArray();
    const testById = new Map((tests as any[]).map((t) => [String(t._id), t]));

    const attemptIds = attempts.map((a) => String(a._id));
    const allQAttempts = await db
      .collection("userquestionattempts")
      .find({ attemptId: { $in: attemptIds } })
      .toArray();
    const qAttemptsByAttempt = new Map<string, any[]>();
    for (const qa of allQAttempts as any[]) {
      const arr = qAttemptsByAttempt.get(qa.attemptId) || [];
      arr.push(qa);
      qAttemptsByAttempt.set(qa.attemptId, arr);
    }

    // Look up type/boardIds for every distinct question answered, across
    // both the published (questions) and draft (cmdsquestions) collections —
    // an attempted test's questions are always published, but this stays
    // robust if a question is later unpublished.
    const allQIds = Array.from(new Set((allQAttempts as any[]).map((qa) => String(qa.qId))));
    const qOids = allQIds
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const [pubQs, draftQs] = await Promise.all([
      db.collection("questions").find({ _id: { $in: qOids } }).project({ type: 1, boardIds: 1 }).toArray(),
      db.collection("cmdsquestions").find({ _id: { $in: qOids } }).project({ type: 1, boardIds: 1 }).toArray(),
    ]);
    const qInfoById = new Map<string, any>();
    for (const q of [...pubQs, ...draftQs] as any[]) qInfoById.set(String(q._id), q);

    const chapterIds = Array.from(
      new Set(
        Array.from(qInfoById.values())
          .map((q: any) => (Array.isArray(q.boardIds) ? q.boardIds[q.boardIds.length - 1] : null))
          .filter(Boolean)
      )
    ) as string[];
    const subjectByChapter = await resolveBoardSubjects(orgId, chapterIds);

    const results: any[] = [];
    const trend: { date: number; pct: number }[] = [];
    const subjectAgg = new Map<string, { correct: number; total: number }>();
    const typeAgg = new Map<string, { correct: number; total: number }>();
    let totalCorrect = 0;
    let totalJudgeable = 0;
    let totalAnswered = 0;

    for (const at of attempts) {
      const attemptId = String(at._id);
      const test = testById.get(String(at.entity?.id));
      if (!test) continue;
      const marks = marksMap(test);
      const qAttempts = qAttemptsByAttempt.get(attemptId) || [];

      let score = 0;
      for (const qa of qAttempts as any[]) {
        totalAnswered++;
        const qInfo = qInfoById.get(String(qa.qId));
        const chapterId = qInfo && Array.isArray(qInfo.boardIds) ? qInfo.boardIds[qInfo.boardIds.length - 1] : null;
        const subject = chapterId ? subjectByChapter[chapterId] : null;
        const type = qInfo?.type || "UNKNOWN";

        if (!qa.isJudgeable) continue;
        totalJudgeable++;
        const verdict = String(qa.isCorrect || "").toUpperCase();
        const isCorrect = verdict === "CORRECT";
        if (isCorrect) totalCorrect++;
        const mk = marks[String(qa.qId)] || { positive: 0, negative: 0 };
        score += isCorrect ? mk.positive || 0 : verdict === "INCORRECT" ? -(mk.negative || 0) : 0;

        if (subject) {
          const s = subjectAgg.get(subject) || { correct: 0, total: 0 };
          s.total++;
          if (isCorrect) s.correct++;
          subjectAgg.set(subject, s);
        }
        const t = typeAgg.get(type) || { correct: 0, total: 0 };
        t.total++;
        if (isCorrect) t.correct++;
        typeAgg.set(type, t);
      }
      if (score < 0) score = 0;

      const attemptedAt = Number(at.endTime) || Number(at.timeCreated) || 0;
      const pct = test.totalMarks ? Math.round((score / test.totalMarks) * 100) : 0;
      results.push({
        entityId: String(at.entity?.id),
        name: test.name || "Test",
        score,
        totalMarks: test.totalMarks ?? 0,
        attemptedAt,
      });
      trend.push({ date: attemptedAt, pct });
    }

    results.sort((a, b) => b.attemptedAt - a.attemptedAt);

    const subjects = Array.from(subjectAgg.entries())
      .map(([name, v]) => ({ name, accuracy: v.total ? Math.round((v.correct / v.total) * 100) : 0, total: v.total }))
      .sort((a, b) => b.total - a.total);

    const types = Array.from(typeAgg.entries())
      .map(([type, v]) => ({ type, accuracy: v.total ? Math.round((v.correct / v.total) * 100) : 0, total: v.total }))
      .sort((a, b) => b.total - a.total);

    const avgScore = results.length
      ? Math.round(
          (results.reduce((sum, r) => sum + (r.totalMarks ? (r.score / r.totalMarks) * 100 : 0), 0) /
            results.length) *
            10
        ) / 10
      : 0;
    const accuracy = totalJudgeable ? Math.round((totalCorrect / totalJudgeable) * 100) : 0;

    return NextResponse.json({
      results,
      trend,
      subjects,
      types,
      summary: {
        testsAttempted: results.length,
        avgScore,
        accuracy,
        questionsAnswered: totalAnswered,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load analytics" }, { status: 500 });
  }
}
