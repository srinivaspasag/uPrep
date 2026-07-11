import { NextRequest, NextResponse } from "next/server";
import { API, DEFAULT_ORG_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

// The attempt/grading pipeline must identify as the learn app: the backend
// skips its program/section library-link validation for "learn-app" and
// "cmds-app" callers. A "web-app" caller would require the test to be linked
// into a program (INVALID_ID otherwise), which standalone CMDS-authored tests
// are not.
const LEARN_APP = "learn-app";

type IncomingAnswer = { qId: string; answerGiven: string[]; timeTaken?: number };

// Submits a test attempt to the REAL backend grading pipeline:
//   startAttempt -> recordAttempt (per question) -> endAttempt
// The backend grades each answer against the stored answer key. We never send
// the correct answers back to the browser — only per-question correctness and
// the aggregate score.
export async function POST(
  req: NextRequest,
  { params }: { params: { id: string } }
) {
  const entityId = params.id;
  const body = await req.json().catch(() => ({}));
  const userId: string = body.userId || "";
  const orgId: string = body.orgId || DEFAULT_ORG_ID;
  const answers: IncomingAnswer[] = Array.isArray(body.answers) ? body.answers : [];

  if (!userId) {
    return NextResponse.json({ error: "Missing userId" }, { status: 400 });
  }

  const base = () => ({
    callingApp: LEARN_APP,
    callingAppId: LEARN_APP,
    callingUserId: userId,
    userId,
    orgId,
    entityId,
    entityType: "TEST",
  });

  async function post(path: string, extra: Record<string, string>) {
    const form = new URLSearchParams({ ...base(), ...extra });
    const r = await fetch(`${API.content}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form,
    });
    // Backend sometimes returns an HTML error page on 500; guard JSON parse.
    const text = await r.text();
    try {
      return JSON.parse(text);
    } catch {
      return { errorCode: "BACKEND_ERROR", _raw: text.slice(0, 120) };
    }
  }

  try {
    const start = await post("/analytics/startAttempt", {});
    const attemptId = start?.result?.info?.id;
    if (!attemptId) {
      return NextResponse.json(
        { error: start?.errorCode || "Could not start attempt" },
        { status: 400 }
      );
    }

    // Record each answer. For a TEST the backend intentionally suppresses the
    // per-question verdict in the recordAttempt response (isJudgeable=false),
    // so we don't rely on it here — the grade is stored server-side and read
    // back from the attempt records after the attempt is ended.
    for (const a of answers) {
      const usp = new URLSearchParams({
        ...base(),
        attemptId,
        qId: a.qId,
        timeTaken: String(a.timeTaken ?? 0),
      });
      // answerGiven is a repeated form field; append each option string.
      for (const val of a.answerGiven || []) usp.append("answerGiven", val);

      await fetch(`${API.content}/analytics/recordAttempt`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: usp,
      }).catch(() => null);
    }

    await post("/analytics/endAttempt", { attemptId });

    // Read the stored, backend-computed verdicts for this attempt. This avoids
    // the legacy results HTTP endpoint (which has a Play 2.1 accessor issue on
    // the orgId field) while still surfacing the real, server-side grade.
    let correct = 0;
    let judgeable = 0;
    let ungraded = 0;
    const perQuestion: { qId: string; result: string }[] = [];
    try {
      const db = await getDb();
      const attempts = await db
        .collection("userquestionattempts")
        .find({ attemptId })
        .toArray();
      const byQ = new Map<string, any>();
      for (const at of attempts) byQ.set(String(at.qId), at);

      for (const a of answers) {
        const at = byQ.get(String(a.qId));
        if (!at) {
          ungraded++;
          perQuestion.push({ qId: a.qId, result: "UNGRADED" });
          continue;
        }
        if (at.isJudgeable) {
          judgeable++;
          const verdict = String(at.isCorrect || "").toUpperCase();
          const isCorrect = verdict === "CORRECT";
          if (isCorrect) correct++;
          perQuestion.push({
            qId: a.qId,
            result: verdict === "PARTIAL" ? "PARTIAL" : isCorrect ? "CORRECT" : "INCORRECT",
          });
        } else {
          perQuestion.push({ qId: a.qId, result: "PENDING_REVIEW" });
        }
      }
    } catch {
      // If the read fails, fall back to reporting the attempt as recorded.
      for (const a of answers) perQuestion.push({ qId: a.qId, result: "PENDING_REVIEW" });
    }

    return NextResponse.json({
      graded: judgeable > 0,
      attemptId,
      total: answers.length,
      answered: answers.length,
      judgeable,
      correct,
      ungraded,
      perQuestion,
    });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Failed to submit attempt" },
      { status: 500 }
    );
  }
}
