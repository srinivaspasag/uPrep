import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Admin test analytics — mirrors the legacy "Result Analytics" screens:
//   GET                -> list of tests that have attempts (count + students)
//   GET ?testId=<id>   -> overall performance, per-question analysis, result sheet
//
// Scores are derived from per-question verdicts (userquestionattempts) against
// each test's marks map (metadata[].marks: qId -> {positive, negative}).

function marksMap(test: any): Record<string, { positive: number; negative: number }> {
  const m: Record<string, { positive: number; negative: number }> = {};
  for (const md of test?.metadata || []) {
    if (md?.marks) Object.assign(m, md.marks);
  }
  return m;
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const testId = req.nextUrl.searchParams.get("testId");

  try {
    const db = await getDb();

    // ---- LIST MODE: tests that have attempts ----
    if (!testId) {
      const grouped = await db
        .collection("userentityattempts")
        .aggregate([
          { $match: { orgId, "entity.type": "TEST", finished: true } },
          {
            $group: {
              _id: "$entity.id",
              attempts: { $sum: 1 },
              students: { $addToSet: "$userId" },
              lastAt: { $max: "$endTime" },
            },
          },
        ])
        .toArray();

      const testOids = grouped
        .map((g: any) => {
          try {
            return new ObjectId(String(g._id));
          } catch {
            return null;
          }
        })
        .filter(Boolean) as any[];
      const tests = await db.collection("tests").find({ _id: { $in: testOids } }).toArray();
      const nameById = new Map((tests as any[]).map((t) => [String(t._id), t.name || t.title || "Test"]));

      const rows = grouped
        .map((g: any) => ({
          testId: String(g._id),
          name: nameById.get(String(g._id)) || "Test",
          attempts: g.attempts,
          students: Array.isArray(g.students) ? g.students.length : 0,
          lastAt: Number(g.lastAt) || 0,
        }))
        .sort((a: any, b: any) => b.lastAt - a.lastAt);

      return NextResponse.json({ tests: rows, orgId });
    }

    // ---- DETAIL MODE: one test ----
    let testDoc: any = null;
    if (ObjectId.isValid(testId)) {
      testDoc = await db.collection("tests").findOne({ _id: new ObjectId(testId) });
    }
    const marks = marksMap(testDoc);
    const totalMarks =
      testDoc?.totalMarks ??
      Object.values(marks).reduce((sum, mk: any) => sum + (mk.positive || 0), 0);

    const attempts: any[] = await db
      .collection("userentityattempts")
      .find({ orgId, "entity.type": "TEST", "entity.id": testId, finished: true })
      .sort({ endTime: -1 })
      .toArray();

    if (attempts.length === 0) {
      return NextResponse.json({
        test: { id: testId, name: testDoc?.name || testDoc?.title || "Test", totalMarks },
        overall: { attempts: 0, students: 0, avgScore: 0, avgPercent: 0, highScore: 0, lowScore: 0 },
        perQuestion: [],
        resultSheet: [],
        orgId,
      });
    }

    // Pull every question attempt for these attempts in one query.
    const attemptIds = attempts.map((a) => String(a._id));
    const qAttempts: any[] = await db
      .collection("userquestionattempts")
      .find({ attemptId: { $in: attemptIds } })
      .toArray();
    const byAttempt = new Map<string, any[]>();
    for (const q of qAttempts) {
      const key = String(q.attemptId);
      if (!byAttempt.has(key)) byAttempt.set(key, []);
      byAttempt.get(key)!.push(q);
    }

    // Question universe: marks-map order first, then any extra attempted qIds.
    const qOrder: string[] = Object.keys(marks);
    const qSet = new Set(qOrder);
    for (const q of qAttempts) {
      const id = String(q.qId);
      if (!qSet.has(id)) {
        qSet.add(id);
        qOrder.push(id);
      }
    }
    const perQ: Record<
      string,
      { attempts: number; correct: number; incorrect: number; partial: number; ungraded: number }
    > = {};
    for (const id of qOrder)
      perQ[id] = { attempts: 0, correct: 0, incorrect: 0, partial: 0, ungraded: 0 };

    // Score each attempt + tally per-question outcomes.
    const scoreByUser = new Map<string, { best: number; attempts: number; lastAt: number }>();
    const scores: number[] = [];

    for (const at of attempts) {
      const qs = byAttempt.get(String(at._id)) || [];
      let score = 0;
      for (const q of qs) {
        const id = String(q.qId);
        const bucket = perQ[id] || (perQ[id] = { attempts: 0, correct: 0, incorrect: 0, partial: 0, ungraded: 0 });
        bucket.attempts++;
        const mk = marks[id] || { positive: 0, negative: 0 };
        if (!q.isJudgeable) {
          bucket.ungraded++;
          continue;
        }
        const verdict = String(q.isCorrect || "").toUpperCase();
        if (verdict === "CORRECT") {
          bucket.correct++;
          score += mk.positive || 0;
        } else if (verdict === "PARTIAL") {
          bucket.partial++;
        } else {
          bucket.incorrect++;
          score -= mk.negative || 0;
        }
      }
      if (score < 0) score = 0;
      scores.push(score);

      const uid = String(at.userId);
      const endAt = Number(at.endTime) || Number(at.timeCreated) || 0;
      const prev = scoreByUser.get(uid);
      if (!prev) scoreByUser.set(uid, { best: score, attempts: 1, lastAt: endAt });
      else
        scoreByUser.set(uid, {
          best: Math.max(prev.best, score),
          attempts: prev.attempts + 1,
          lastAt: Math.max(prev.lastAt, endAt),
        });
    }

    // Resolve student names.
    const userIds = Array.from(scoreByUser.keys());
    const userOids = userIds
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const members = await db
      .collection("orgmembers")
      .find({ _id: { $in: userOids } })
      .toArray();
    const memberById = new Map(
      (members as any[]).map((m) => [
        String(m._id),
        { name: `${m.firstName || ""} ${m.lastName || ""}`.trim() || m.memberId || "Student", memberId: m.memberId || "" },
      ])
    );

    const pct = (s: number) => (totalMarks > 0 ? Math.round((s / totalMarks) * 1000) / 10 : 0);

    const resultSheet = userIds
      .map((uid) => {
        const rec = scoreByUser.get(uid)!;
        const info = memberById.get(uid) || { name: "Student", memberId: "" };
        return {
          userId: uid,
          name: info.name,
          memberId: info.memberId,
          score: rec.best,
          percent: pct(rec.best),
          attempts: rec.attempts,
          lastAt: rec.lastAt,
        };
      })
      .sort((a, b) => b.score - a.score);

    const perQuestion = qOrder.map((id, i) => {
      const b = perQ[id];
      const graded = b.correct + b.incorrect + b.partial;
      return {
        qId: id,
        label: `Q${i + 1}`,
        attempts: b.attempts,
        correct: b.correct,
        incorrect: b.incorrect,
        partial: b.partial,
        ungraded: b.ungraded,
        correctPercent: graded > 0 ? Math.round((b.correct / graded) * 1000) / 10 : 0,
        marks: marks[id] || null,
      };
    });

    const sum = scores.reduce((a, b) => a + b, 0);
    const overall = {
      attempts: attempts.length,
      students: scoreByUser.size,
      avgScore: scores.length ? Math.round((sum / scores.length) * 10) / 10 : 0,
      avgPercent: scores.length ? pct(sum / scores.length) : 0,
      highScore: scores.length ? Math.max(...scores) : 0,
      lowScore: scores.length ? Math.min(...scores) : 0,
    };

    // Top performers = best-scoring students (result sheet is already sorted).
    const topPerformers = resultSheet.slice(0, 5).map((r) => ({
      name: r.name,
      memberId: r.memberId,
      score: r.score,
      percent: r.percent,
    }));

    // % students vs marks distribution — 5 buckets by percentage of total marks.
    const bucketLabels = ["0–20%", "20–40%", "40–60%", "60–80%", "80–100%"];
    const buckets = [0, 0, 0, 0, 0];
    for (const r of resultSheet) {
      let idx = Math.floor(r.percent / 20);
      if (idx > 4) idx = 4;
      if (idx < 0) idx = 0;
      buckets[idx]++;
    }
    const distribution = bucketLabels.map((label, i) => ({
      label,
      count: buckets[i],
      percentOfStudents:
        resultSheet.length > 0 ? Math.round((buckets[i] / resultSheet.length) * 1000) / 10 : 0,
    }));

    return NextResponse.json({
      test: { id: testId, name: testDoc?.name || testDoc?.title || "Test", totalMarks },
      overall,
      topPerformers,
      distribution,
      perQuestion,
      resultSheet,
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load analytics" }, { status: 500 });
  }
}
