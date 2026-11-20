import { NextRequest, NextResponse } from "next/server";
import { sessionFromReq } from "@/lib/server-session";
import { activeExam, examLockMessage } from "@/lib/examLock";

export const dynamic = "force-dynamic";

// Is Aira closed for this student right now (test in progress)? Used by the
// Ask Aira pages to show a lock screen instead of the chat.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ locked: false }, { status: 401 });
  try {
    const exam = await activeExam(session.id);
    return NextResponse.json(exam ? { locked: true, testId: exam.testId, testName: exam.testName, message: examLockMessage(exam) } : { locked: false });
  } catch {
    return NextResponse.json({ locked: false });
  }
}
