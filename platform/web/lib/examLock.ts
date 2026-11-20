import { ObjectId } from "mongodb";
import { NextResponse } from "next/server";
import { getDb } from "./mongo";

// Aira (AI doubts) is closed to a student while they have a test in
// progress — otherwise "Pause & exit", ask Aira, resume answers the paper.
//
// A test is "in progress" when the test page has saved progress for it
// (testprogress — written every 15 s while attempting and on Pause & exit,
// deleted on submit), it still has time left, and the student has no
// finished attempt for it. Progress untouched for more than a day is treated
// as abandoned, so a test left unfinished can't lock Aira forever.

const STALE_MS = 24 * 60 * 60 * 1000;

export type ActiveExam = { testId: string; testName: string };

export async function activeExam(userId: string): Promise<ActiveExam | null> {
  if (!userId) return null;
  const db = await getDb();
  const open: any[] = await db
    .collection("testprogress")
    .find({ userId, updatedAt: { $gt: Date.now() - STALE_MS } })
    .sort({ updatedAt: -1 })
    .limit(5)
    .toArray();
  for (const p of open) {
    if (typeof p.remaining === "number" && p.remaining <= 0) continue;
    const testId = String(p.testId);
    const finished = await db
      .collection("userentityattempts")
      .findOne({ userId, "entity.id": testId, finished: true }, { projection: { _id: 1 } });
    if (finished) continue;
    let testName = "your test";
    if (ObjectId.isValid(testId)) {
      const t: any = await db.collection("tests").findOne({ _id: new ObjectId(testId) }, { projection: { name: 1 } });
      if (t?.name) testName = t.name;
    }
    return { testId, testName };
  }
  return null;
}

export function examLockMessage(exam: ActiveExam): string {
  return `Aira is closed while your test "${exam.testName}" is in progress. Finish and submit the test, then Aira will be available again.`;
}

// For API routes: a 423 response when the student is mid-exam, else null.
export async function examLockResponse(userId: string): Promise<NextResponse | null> {
  try {
    const exam = await activeExam(userId);
    if (!exam) return null;
    return NextResponse.json({ error: examLockMessage(exam), examLock: true, testId: exam.testId }, { status: 423 });
  } catch {
    return null; // never take Aira down because the check itself failed
  }
}
