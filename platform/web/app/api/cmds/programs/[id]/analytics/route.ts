import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";

// Program-level Analytics — legacy's Institute.java:1070 testAnalytics(), the
// "select a program, see overall analytics" screen (tests-over-time graph +
// per-test topper). Legacy scopes this to the institute (orgId); this
// rebuild has no direct test<->program link, so the equivalent scope is
// "tests attempted by students enrolled in this program" — the same join
// key programMemberships already establishes everywhere else in this app.
function marksMap(test: any): Record<string, { positive: number; negative: number }> {
  const m: Record<string, { positive: number; negative: number }> = {};
  for (const md of test?.metadata || []) {
    if (md?.marks) Object.assign(m, md.marks);
  }
  return m;
}

export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const programId = params.id;

  try {
    const db = await getDb();

    const students = await db
      .collection("orgmembers")
      .find({ orgId, recordState: "ACTIVE", profile: "STUDENT", "programMemberships.programId": programId })
      .project({ _id: 1 })
      .toArray();
    const studentIds = students.map((s: any) => String(s._id));

    if (studentIds.length === 0) {
      return NextResponse.json({ tests: [], studentCount: 0, orgId });
    }

    const attempts = await db
      .collection("userentityattempts")
      .find({ orgId, "entity.type": "TEST", finished: true, userId: { $in: studentIds } })
      .toArray();

    if (attempts.length === 0) {
      return NextResponse.json({ tests: [], studentCount: studentIds.length, orgId });
    }

    const byTest = new Map<string, any[]>();
    for (const a of attempts as any[]) {
      const tid = String(a.entity.id);
      if (!byTest.has(tid)) byTest.set(tid, []);
      byTest.get(tid)!.push(a);
    }

    const testOids = Array.from(byTest.keys())
      .filter((id) => ObjectId.isValid(id))
      .map((id) => new ObjectId(id));
    const testDocs = await db.collection("tests").find({ _id: { $in: testOids } }).toArray();
    const testById = new Map(testDocs.map((t: any) => [String(t._id), t]));

    const memberDocs = await db
      .collection("orgmembers")
      .find({ _id: { $in: studentIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id)) } })
      .toArray();
    const memberById = new Map(
      (memberDocs as any[]).map((m) => [
        String(m._id),
        { name: `${m.firstName || ""} ${m.lastName || ""}`.trim() || m.memberId || "Student", memberId: m.memberId || "" },
      ])
    );

    // One query for all question attempts across every test's attempts, same
    // batching approach as the per-test analytics route.
    const attemptIds = attempts.map((a: any) => String(a._id));
    const qAttempts = await db
      .collection("userquestionattempts")
      .find({ attemptId: { $in: attemptIds } })
      .toArray();
    const byAttempt = new Map<string, any[]>();
    for (const q of qAttempts as any[]) {
      const key = String(q.attemptId);
      if (!byAttempt.has(key)) byAttempt.set(key, []);
      byAttempt.get(key)!.push(q);
    }

    const tests = Array.from(byTest.entries())
      .map(([testId, testAttempts]) => {
        const testDoc = testById.get(testId);
        if (!testDoc) return null;
        const marks = marksMap(testDoc);
        const totalMarks =
          testDoc.totalMarks ?? Object.values(marks).reduce((sum, mk: any) => sum + (mk.positive || 0), 0);

        // Best score per student on this test (same "best attempt wins" rule
        // the per-test analytics route uses).
        const bestByUser = new Map<string, number>();
        for (const at of testAttempts) {
          const qs = byAttempt.get(String(at._id)) || [];
          let score = 0;
          for (const q of qs) {
            if (!q.isJudgeable) continue;
            const mk = marks[String(q.qId)] || { positive: 0, negative: 0 };
            const verdict = String(q.isCorrect || "").toUpperCase();
            if (verdict === "CORRECT") score += mk.positive || 0;
            else if (verdict === "INCORRECT") score -= mk.negative || 0;
          }
          if (score < 0) score = 0;
          const uid = String(at.userId);
          const prev = bestByUser.get(uid);
          if (prev === undefined || score > prev) bestByUser.set(uid, score);
        }

        const scores = Array.from(bestByUser.values());
        const avgScore = scores.length ? scores.reduce((a, b) => a + b, 0) / scores.length : 0;
        const avgPercent = totalMarks > 0 ? Math.round((avgScore / totalMarks) * 1000) / 10 : 0;

        let topperUid: string | null = null;
        let topperScore = -1;
        for (const [uid, score] of bestByUser.entries()) {
          if (score > topperScore) {
            topperScore = score;
            topperUid = uid;
          }
        }
        const topper = topperUid ? memberById.get(topperUid) : null;
        const topperPercent = totalMarks > 0 && topperScore >= 0 ? Math.round((topperScore / totalMarks) * 1000) / 10 : 0;

        const lastAt = Math.max(...testAttempts.map((a: any) => Number(a.endTime) || Number(a.timeCreated) || 0));

        return {
          id: testId,
          name: testDoc.name || testDoc.title || "Test",
          date: lastAt,
          attempts: testAttempts.length,
          students: bestByUser.size,
          avgPercent,
          topperName: topper?.name || null,
          topperPercent: topper ? topperPercent : null,
        };
      })
      .filter((t): t is NonNullable<typeof t> => t !== null)
      .sort((a, b) => a.date - b.date);

    return NextResponse.json({ tests, studentCount: studentIds.length, orgId });
  } catch (e: any) {
    return NextResponse.json({ tests: [], error: e?.message || "Failed to load program analytics" }, { status: 500 });
  }
}
