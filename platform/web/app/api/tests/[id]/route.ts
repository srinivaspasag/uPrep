import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { fetchTestPayload } from "@/lib/test-payload";
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

  try {
    const { test, questions } = await fetchTestPayload(id, orgId, userId);

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
      test: { ...test, resultVisibility },
      questions,
      alreadyAttempted,
    });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Failed to load test" },
      { status: e?.legacy ? 400 : 500 }
    );
  }
}
