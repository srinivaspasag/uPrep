import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Reads back the score for a test the caller has ALREADY finished — no new
// attempt is started or submitted. Backs the "already attempted" redirect
// in app/test/[id]/page.tsx (see app/api/tests/[id]/route.ts's
// `alreadyAttempted` flag): legacy never lets a student re-attempt a
// finished test, it just shows their existing result again, so this powers
// that same "come back and see your score" path without re-grading
// anything. Verdict-reading logic mirrors the read-back half of
// app/api/tests/[id]/submit/route.ts.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const entityId = params.id;
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userId = session.id;

  try {
    const db = await getDb();
    const attempt: any = await db
      .collection("userentityattempts")
      .find({ userId, "entity.id": entityId, "entity.type": "TEST", finished: true })
      .sort({ endTime: -1 })
      .limit(1)
      .next();
    if (!attempt) return NextResponse.json({ error: "No completed attempt found" }, { status: 404 });

    const attemptId = String(attempt._id);
    const qAttempts = await db.collection("userquestionattempts").find({ attemptId }).toArray();

    let correct = 0;
    let judgeable = 0;
    let ungraded = 0;
    const perQuestion: { qId: string; result: string }[] = [];
    for (const at of qAttempts as any[]) {
      if (at.isJudgeable) {
        judgeable++;
        const verdict = String(at.isCorrect || "").toUpperCase();
        const isCorrect = verdict === "CORRECT";
        if (isCorrect) correct++;
        perQuestion.push({ qId: at.qId, result: verdict === "PARTIAL" ? "PARTIAL" : isCorrect ? "CORRECT" : "INCORRECT" });
      } else {
        ungraded++;
        perQuestion.push({ qId: at.qId, result: "PENDING_REVIEW" });
      }
    }

    return NextResponse.json({
      graded: judgeable > 0,
      attemptId,
      total: qAttempts.length,
      answered: qAttempts.length,
      judgeable,
      correct,
      ungraded,
      perQuestion,
      failedQIds: [],
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load result" }, { status: 500 });
  }
}
