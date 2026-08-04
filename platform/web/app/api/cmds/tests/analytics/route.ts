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
  const studentUserId = req.nextUrl.searchParams.get("userId");

  try {
    const db = await getDb();

    // ---- LIST MODE: tests that have attempts ----
    if (!testId) {
      // Student-only, same reasoning as DETAIL mode below — a staff/QA
      // attempt shouldn't count toward "N students" for a test either.
      const studentIds = (
        await db
          .collection("orgmembers")
          .find({ orgId, recordState: "ACTIVE", profile: "STUDENT" })
          .project({ _id: 1 })
          .toArray()
      ).map((m: any) => String(m._id));

      const grouped = await db
        .collection("userentityattempts")
        .aggregate([
          { $match: { orgId, "entity.type": "TEST", finished: true, userId: { $in: studentIds } } },
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

    const rawAttempts: any[] = await db
      .collection("userentityattempts")
      .find({ orgId, "entity.type": "TEST", "entity.id": testId, finished: true })
      .sort({ endTime: -1 })
      .toArray();

    // Student-only — a staff/admin account attempting a test for QA purposes
    // (real example seen live: "Super Admin" showing up as a "Top performer"
    // next to actual students) would otherwise pollute the average, high/low,
    // and topper for an institute's own analytics. Every other student-facing
    // count in this app (teachers/students/content) already scopes by
    // profile; this route was the one surface that never did.
    const rawUserIds = Array.from(new Set(rawAttempts.map((a) => String(a.userId))));
    const rawUserOids = rawUserIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
    const profileById = new Map(
      (
        await db
          .collection("orgmembers")
          .find({ _id: { $in: rawUserOids } })
          .project({ profile: 1 })
          .toArray()
      ).map((m: any) => [String(m._id), (m.profile || "").toUpperCase()])
    );
    // Attempts from an id with no matching orgmember (e.g. an ad hoc QA
    // string userId, not a real ObjectId) are dropped too — they're never a
    // real enrolled student either.
    const attempts = rawAttempts.filter((a) => profileById.get(String(a.userId)) === "STUDENT");

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
      {
        attempts: number;
        correct: number;
        incorrect: number;
        partial: number;
        ungraded: number;
        wrongUserIds: Set<string>;
      }
    > = {};
    for (const id of qOrder)
      perQ[id] = { attempts: 0, correct: 0, incorrect: 0, partial: 0, ungraded: 0, wrongUserIds: new Set() };
    const labelByQId = new Map(qOrder.map((id, i) => [id, `Q${i + 1}`]));

    // Score each attempt + tally per-question outcomes.
    const scoreByUser = new Map<string, { best: number; attempts: number; lastAt: number }>();
    // Question-by-question verdicts for each user's BEST-scoring attempt —
    // powers the per-student drill-down (legacy's getUserEntityQuestionAttemptInfos,
    // AnalyticsManager.java:1635). Only kept for the attempt matching that
    // user's best score, same attempt the result sheet itself reports on.
    const bestAttemptDetail = new Map<
      string,
      { qId: string; label: string; verdict: string; marks: { positive: number; negative: number } | null }[]
    >();
    const scores: number[] = [];

    for (const at of attempts) {
      const qs = byAttempt.get(String(at._id)) || [];
      const uid = String(at.userId);
      let score = 0;
      const thisAttemptDetail: { qId: string; label: string; verdict: string; marks: { positive: number; negative: number } | null }[] = [];
      for (const q of qs) {
        const id = String(q.qId);
        const bucket =
          perQ[id] || (perQ[id] = { attempts: 0, correct: 0, incorrect: 0, partial: 0, ungraded: 0, wrongUserIds: new Set() });
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
          bucket.wrongUserIds.add(uid);
        } else {
          bucket.incorrect++;
          bucket.wrongUserIds.add(uid);
          score -= mk.negative || 0;
        }
        thisAttemptDetail.push({
          qId: id,
          label: labelByQId.get(id) || id,
          verdict: q.isJudgeable ? verdict : "UNGRADED",
          marks: marks[id] || null,
        });
      }
      if (score < 0) score = 0;
      scores.push(score);

      const endAt = Number(at.endTime) || Number(at.timeCreated) || 0;
      const prev = scoreByUser.get(uid);
      if (!prev) {
        scoreByUser.set(uid, { best: score, attempts: 1, lastAt: endAt });
        bestAttemptDetail.set(uid, thisAttemptDetail);
      } else {
        const newBest = Math.max(prev.best, score);
        // Keep the detail from whichever attempt is actually reported as the
        // best score — matches the result sheet, which also reports `best`.
        if (score >= prev.best) bestAttemptDetail.set(uid, thisAttemptDetail);
        scoreByUser.set(uid, { best: newBest, attempts: prev.attempts + 1, lastAt: Math.max(prev.lastAt, endAt) });
      }
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

    // Rank — legacy's real "All India Rank" is a genuine cross-institute
    // aggregation, only meaningful for tests distributed to multiple
    // institutes via CMDS (CMDSTestDAO.java showAIR flag) — not something
    // this single-institute rebuild can honestly compute. This is the
    // institute-scoped rank instead: real, computed, just labeled for what
    // it actually is (position among this institute's own test-takers).
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
      .sort((a, b) => b.score - a.score)
      .map((r, i) => ({ ...r, rank: i + 1, totalStudents: userIds.length }));

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
        // Who got it wrong (incorrect/partial) — drill-down for the admin.
        // Real per-question timing isn't tracked anywhere in the pipeline
        // today (deferred — see the plan), so no duration here.
        wrongStudents: Array.from(b.wrongUserIds).map((uid) => {
          const info = memberById.get(uid) || { name: "Student", memberId: "" };
          return { userId: uid, name: info.name, memberId: info.memberId };
        }),
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

    // Per-student drill-down — legacy's getUserEntityQuestionAttemptInfos +
    // getUserEntityAnalytics, single-test scoped (see the "All India Rank"
    // note on resultSheet above for why rank here is institute-scoped).
    let student: any = null;
    if (studentUserId) {
      const row = resultSheet.find((r) => r.userId === studentUserId);
      if (row) {
        student = {
          ...row,
          questions: bestAttemptDetail.get(studentUserId) || [],
        };
      }
    }

    return NextResponse.json({
      test: { id: testId, name: testDoc?.name || testDoc?.title || "Test", totalMarks },
      overall,
      topPerformers,
      distribution,
      perQuestion,
      resultSheet,
      student,
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load analytics" }, { status: 500 });
  }
}
