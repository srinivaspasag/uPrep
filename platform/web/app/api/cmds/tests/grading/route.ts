import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const ATTEMPTS_COLL = "userquestionattempts";

// Subjective grading queue: questions the auto-grader couldn't judge
// (isJudgeable = false) and that a teacher hasn't graded yet. Staff assign a
// verdict/marks, which the analytics read back.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(ATTEMPTS_COLL)
      .find({ orgId, isJudgeable: false, manualGraded: { $ne: true } } as any)
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();

    // Enrich with question text + student name where available.
    const qIds = Array.from(new Set((docs as any[]).map((d) => d.qId).filter(Boolean)));
    const questions = qIds.length
      ? await db.collection("questions").find({ _id: { $in: qIds } } as any).toArray().catch(() => [])
      : [];
    const qById = new Map((questions as any[]).map((q) => [String(q._id), q]));

    const userIds = Array.from(new Set((docs as any[]).map((d) => d.userId).filter(Boolean)));
    const members = userIds.length
      ? await db.collection("orgmembers").find({ userId: { $in: userIds } } as any).toArray().catch(() => [])
      : [];
    const nameByUser = new Map(
      (members as any[]).map((m) => [String(m.userId), `${m.firstName || ""} ${m.lastName || ""}`.trim()])
    );

    const items = (docs as any[]).map((d) => ({
      id: String(d._id),
      attemptId: d.attemptId || null,
      qId: d.qId,
      userId: d.userId,
      studentName: nameByUser.get(String(d.userId)) || d.userId || "Student",
      answerGiven: Array.isArray(d.answerGiven) ? d.answerGiven : d.answerGiven ? [d.answerGiven] : [],
      question: qById.get(String(d.qId))?.content || null,
      maxMarks: d.maxMarks ?? qById.get(String(d.qId))?.marks ?? null,
    }));

    return NextResponse.json({ items, orgId });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type GradeBody = { id?: string; verdict?: string; marks?: number };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as GradeBody;
  const id = String(b.id || "");
  const verdict = (b.verdict || "").toUpperCase();
  if (!id) return NextResponse.json({ error: "Missing attempt id" }, { status: 400 });
  if (!["CORRECT", "INCORRECT", "PARTIAL"].includes(verdict))
    return NextResponse.json({ error: "Invalid verdict" }, { status: 400 });

  try {
    const { ObjectId } = await import("mongodb");
    if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
    const session = await sessionFromReq(req);
    const db = await getDb();
    const res = await db.collection(ATTEMPTS_COLL).updateOne(
      { _id: new ObjectId(id) } as any,
      {
        $set: {
          isJudgeable: true,
          isCorrect: verdict,
          marksObtained: typeof b.marks === "number" ? b.marks : verdict === "CORRECT" ? undefined : 0,
          manualGraded: true,
          gradedBy: session?.id || null,
          gradedAt: Date.now(),
        },
      }
    );
    if (!res.matchedCount) return NextResponse.json({ error: "Attempt not found" }, { status: 404 });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Grade failed" }, { status: 500 });
  }
}
