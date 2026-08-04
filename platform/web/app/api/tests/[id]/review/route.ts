import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

// Post-submission answer review — legacy's real result page has "Your
// Answers" (full paper with correct answers revealed) and "Result Sheet"
// (per-question marks) tabs, gated behind the SAME "Show Results"
// visibility the test author set (QrTests createTest.html's
// "Show Results after the student takes the test" = SHOW/HIDE). Reads
// straight from Mongo (tests/userentityattempts/userquestionattempts/
// answers/questions) rather than the legacy content-service, same
// established pattern as the submit route — this data isn't safe to expose
// until the caller's OWN attempt is finished, so it's derived server-side
// from the session's userId, never trusted from the client.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const testId = params.id;
  const userId = req.nextUrl.searchParams.get("userId") || "";
  if (!userId) return NextResponse.json({ error: "Missing userId" }, { status: 400 });
  if (!ObjectId.isValid(testId)) return NextResponse.json({ error: "Invalid test id" }, { status: 400 });

  try {
    const db = await getDb();
    const test: any = await db.collection("tests").findOne({ _id: new ObjectId(testId) });
    if (!test) return NextResponse.json({ error: "Test not found" }, { status: 404 });

    const resultVisibility = String(test.resultVisibility || "VISIBLE").toUpperCase();
    if (resultVisibility !== "VISIBLE") {
      return NextResponse.json({ hidden: true });
    }

    const attempt: any = await db
      .collection("userentityattempts")
      .find({ "entity.id": testId, userId, finished: true })
      .sort({ endTime: -1 })
      .limit(1)
      .next();
    if (!attempt) return NextResponse.json({ error: "No completed attempt found" }, { status: 404 });

    const attemptId = attempt._id.toHexString();
    const qAttempts = await db.collection("userquestionattempts").find({ attemptId }).toArray();
    const attemptByQId = new Map(qAttempts.map((a: any) => [String(a.qId), a]));

    const allQIds: string[] = (test.metadata || []).flatMap((m: any) => (Array.isArray(m.qIds) ? m.qIds : []));
    const oids = allQIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
    const [questionDocs, answerDocs] = await Promise.all([
      db.collection("questions").find({ _id: { $in: oids } }).toArray(),
      db.collection("answers").find({ qId: { $in: allQIds } }).toArray(),
    ]);
    const questionById = new Map(questionDocs.map((q: any) => [String(q._id), q]));
    const answerByQId = new Map(answerDocs.map((a: any) => [String(a.qId), a]));

    let totalScore = 0;
    const sections = (test.metadata || []).map((m: any) => {
      const qIds: string[] = Array.isArray(m.qIds) ? m.qIds : [];
      const questions = qIds
        .map((qId) => {
          const q = questionById.get(qId);
          if (!q) return null;
          const at = attemptByQId.get(qId);
          const answerDoc = answerByQId.get(qId);
          const chosen: string[] = at?.answerGiven || [];
          const correct: string[] = answerDoc?.answer || [];
          const score = typeof at?.score === "number" ? at.score : 0;
          totalScore += score;
          const verdict = !at
            ? "UNANSWERED"
            : chosen.length === 0
            ? "UNANSWERED"
            : String(at.isCorrect || "").toUpperCase() === "CORRECT"
            ? "CORRECT"
            : String(at.isCorrect || "").toUpperCase() === "PARTIAL"
            ? "PARTIAL"
            : "INCORRECT";
          return {
            id: qId,
            content: q.content || "",
            type: q.type || "SCQ",
            options: q.options || [],
            chosenIndexes: chosen,
            correctIndexes: correct,
            verdict,
            score,
          };
        })
        .filter(Boolean);
      return { name: m.name || "Section", questions };
    });

    const totalMarks = (test.metadata || []).reduce((sum: number, m: any) => sum + (m.totalMarks || 0), 0);

    return NextResponse.json({ hidden: false, totalScore, totalMarks, sections });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load review" }, { status: 500 });
  }
}
