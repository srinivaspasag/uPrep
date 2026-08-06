import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { API, CALLING_APP, CALLING_APP_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Fetches a test's info + questions from the REAL legacy content service
// (getTestInfo / getTestQuestions). Answer keys are intentionally NOT sent to
// the client. userId/orgId come from the signed session cookie, never from
// the caller — this used to trust ?userId=/?orgId= directly, which let
// anyone read another student's alreadyAttempted status (and forwarded an
// arbitrary identity to the legacy service as callingUserId).
export async function GET(
  req: NextRequest,
  { params }: { params: { id: string } }
) {
  const id = params.id;
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userId = session.id;
  const orgId = session.orgId;

  const form = () =>
    new URLSearchParams({
      callingApp: CALLING_APP,
      callingAppId: CALLING_APP_ID,
      callingUserId: userId,
      userId,
      orgId,
      id,
    });

  try {
    const [infoResp, qResp] = await Promise.all([
      fetch(`${API.content}/tests/getTestInfo`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form(),
      }),
      fetch(`${API.content}/tests/getTestQuestions`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form(),
      }),
    ]);

    const info = await infoResp.json();
    const q = await qResp.json();

    if (info.errorCode || q.errorCode) {
      return NextResponse.json(
        { error: info.errorMessage || q.errorMessage || info.errorCode || q.errorCode },
        { status: 400 }
      );
    }

    const ir = info.result || {};
    const qr = q.result || {};

    // Flatten board -> questions into a clean, key-free question list.
    const questions: any[] = [];
    for (const b of qr.boards || []) {
      for (const qq of b.questions || []) {
        questions.push({
          id: qq.id,
          content: qq.content,
          type: qq.type,
          options: qq.options || [],
          board: b.name,
        });
      }
    }

    // resultVisibility isn't exposed by the real content-service's
    // getTestInfo response, so it's read straight from the same Mongo the
    // backend itself writes to — same established pattern as the submit
    // route reading userquestionattempts directly. Defaults to VISIBLE,
    // matching the create route's own default.
    let resultVisibility = "VISIBLE";
    // Legacy allows exactly ONE attempt per test, ever — confirmed in the
    // real backend source (AnalyticsManager.isMultiAttemptAllowed() is
    // hardcoded `return false`). Retaking an already-finished test makes
    // the real startAttempt call throw MULTI_ATTEMPTS_NOT_ALLOWED; legacy's
    // UI never lets a student reach that error because it checks attempt
    // status up front and routes straight to the existing result instead
    // (Tests.java testPageDirect). Surfaced here so the client can do the
    // same, instead of letting a student retake the whole test only to have
    // the submission silently rejected at the very end.
    let alreadyAttempted = false;
    if (ObjectId.isValid(id)) {
      try {
        const db = await getDb();
        const testDoc = await db.collection("tests").findOne({ _id: new ObjectId(id) }, { projection: { resultVisibility: 1 } });
        if (testDoc?.resultVisibility) resultVisibility = String(testDoc.resultVisibility).toUpperCase();
        if (userId) {
          const finished = await db
            .collection("userentityattempts")
            .findOne({ userId, "entity.id": id, "entity.type": "TEST", finished: true });
          alreadyAttempted = !!finished;
        }
      } catch {
        /* fall back to VISIBLE / not-attempted if the direct read fails */
      }
    }

    return NextResponse.json({
      test: {
        id: ir.id || id,
        name: ir.name || qr.name || "Test",
        durationMin: (qr.totalTestTime || ir.duration || 0)
          ? Math.round((qr.totalTestTime || ir.duration) / 60000)
          : 0,
        totalMarks: ir.totalMarks ?? 0,
        code: qr.code || ir.code || null,
        resultVisibility,
      },
      questions,
      alreadyAttempted,
    });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Failed to load test" },
      { status: 500 }
    );
  }
}
