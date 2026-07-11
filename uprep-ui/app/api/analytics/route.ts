import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// A student's test results: one row per finished TEST attempt. Score is derived
// from the per-question verdicts (userquestionattempts) and the test's marks
// map (metadata[].marks: qId -> {positive, negative}). Mirrors the legacy
// "Result Analytics" screen.
export async function GET(req: NextRequest) {
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  if (!userId) return NextResponse.json({ results: [] });

  try {
    const db = await getDb();

    const attempts: any[] = await db
      .collection("userentityattempts")
      .find({ userId, orgId, "entity.type": "TEST", finished: true })
      .sort({ endTime: -1 })
      .toArray();

    if (attempts.length === 0) return NextResponse.json({ results: [] });

    // Load referenced tests (name, totalMarks, per-qId marks map).
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

    const tests: any[] = await db
      .collection("tests")
      .find({ _id: { $in: testOids } })
      .toArray();
    const testById = new Map(tests.map((t) => [String(t._id), t]));

    const marksMap = (test: any): Record<string, { positive: number; negative: number }> => {
      const m: Record<string, { positive: number; negative: number }> = {};
      for (const md of test?.metadata || []) {
        if (md.marks) Object.assign(m, md.marks);
      }
      return m;
    };

    const results = [];
    for (const at of attempts) {
      const attemptId = String(at._id);
      const test = testById.get(String(at.entity?.id));
      if (!test) continue;
      const marks = marksMap(test);

      const qAttempts: any[] = await db
        .collection("userquestionattempts")
        .find({ attemptId })
        .toArray();

      let score = 0;
      for (const q of qAttempts) {
        if (!q.isJudgeable) continue;
        const verdict = String(q.isCorrect || "").toUpperCase();
        const mk = marks[String(q.qId)] || { positive: 0, negative: 0 };
        if (verdict === "CORRECT") score += mk.positive || 0;
        else if (verdict === "INCORRECT") score -= mk.negative || 0;
      }
      if (score < 0) score = 0;

      results.push({
        entityId: String(at.entity?.id),
        name: test.name || "Test",
        score,
        totalMarks: test.totalMarks ?? 0,
        timeTaken: Number(at.timeLeft ? 0 : 0),
        attemptedAt: Number(at.endTime) || Number(at.timeCreated) || 0,
      });
    }

    return NextResponse.json({ results, orgId });
  } catch (e: any) {
    return NextResponse.json({ results: [], error: e?.message }, { status: 500 });
  }
}
