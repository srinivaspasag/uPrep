import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";

export const dynamic = "force-dynamic";

const DEFAULT_BOARD_ID = process.env.CMDS_DEFAULT_BOARD_ID || "6a3b7ab30cf2f6add23ae035";

type QType = "SCQ" | "MCQ" | "NUMERIC" | "SUBJECTIVE" | "MATRIX" | "PARA";

// GET: load a question for editing — the "new question" form re-shaped for
// pre-fill (options/correct indices, numeric/matrix answers unpacked from
// the stored `answer` array back into their form-specific fields).
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const db = await getDb();
    const q: any = await db.collection("cmdsquestions").findOne({ _id: new ObjectId(id) });
    if (!q) return NextResponse.json({ error: "Question not found" }, { status: 404 });

    const type: QType = q.type || "SCQ";
    const options: string[] = q.solutionInfo?.optionBody?.newOptions || [];
    const answer: string[] = Array.isArray(q.solutionInfo?.answer)
      ? q.solutionInfo.answer
      : q.solutionInfo?.answer
      ? [String(q.solutionInfo.answer)]
      : [];

    return NextResponse.json({
      question: {
        id: String(q._id),
        type,
        content: q.questionBody?.newText || "",
        paragraph: q.questionBody?.paragraph || "",
        options,
        correct: type === "SCQ" || type === "MCQ" || type === "PARA" ? answer.map((a) => Number(a)) : [],
        numericAnswers: type === "NUMERIC" ? answer : [],
        matrixPairs: type === "MATRIX" ? answer : [],
        solution: q.solutionInfo?.explanation || "",
        difficulty: q.difficulty || "EASY",
        tags: Array.isArray(q.tags) ? q.tags : [],
        subject: q.subject || "",
        boardIds: Array.isArray(q.boardIds) ? q.boardIds : [],
        published: !!q.published,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load question" }, { status: 500 });
  }
}

type EditBody = {
  action?: "unpublish";
  content: string;
  type: QType;
  options?: string[];
  correct?: number[];
  numericAnswers?: string[];
  matrixPairs?: string[];
  paragraph?: string;
  solution?: string;
  difficulty?: string;
  tags?: string[];
  subject?: string;
  boardIds?: string[];
};

// PATCH: save edits. If the question is already published, also re-syncs
// the live gradable copy (the `questions` library doc + its `answers`
// entry) so a correction actually takes effect for future attempts —
// otherwise editing a published question's answer key would silently do
// nothing for students, since the library copy is a point-in-time snapshot
// taken at publish time (see /api/cmds/publish).
export async function PATCH(req: NextRequest, { params }: { params: { id: string } }) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  const b = (await req.json().catch(() => ({}))) as EditBody;

  if (b.action === "unpublish") {
    try {
      const db = await getDb();
      const oid = new ObjectId(id);
      const existing: any = await db.collection("cmdsquestions").findOne({ _id: oid });
      if (!existing) return NextResponse.json({ error: "Question not found" }, { status: 404 });
      if (!existing.published) return NextResponse.json({ ok: true }); // already a draft, nothing to do

      // Same reasoning as the delete guard this mirrors: pulling the live
      // gradable copy out from under a test that already includes it would
      // silently break that question for students mid-course. Block instead
      // of guessing — same as the earlier published-question delete guard.
      const usedIn = await db
        .collection("tests")
        .find({ "metadata.qIds": id, recordState: "ACTIVE" })
        .project({ name: 1, title: 1 })
        .toArray();
      if (usedIn.length > 0) {
        const names = (usedIn as any[]).map((t) => t.name || t.title || "Untitled test").join(", ");
        return NextResponse.json(
          { error: `Can't unpublish — still used in: ${names}. Remove it from those tests first.` },
          { status: 409 }
        );
      }

      await db.collection("cmdsquestions").updateOne({ _id: oid }, { $set: { published: false, lastUpdated: Date.now() } });
      await db.collection("questions").deleteOne({ _id: oid });
      await db.collection("answers").deleteMany({ qId: id });
      return NextResponse.json({ ok: true });
    } catch (e: any) {
      return NextResponse.json({ error: e?.message || "Failed to unpublish question" }, { status: 500 });
    }
  }

  const type = (b.type || "SCQ") as QType;
  const content = (b.content || "").trim();
  const options = (b.options || []).map((o) => o.trim());
  const correct = b.correct || [];

  if (!content) return NextResponse.json({ error: "Question text is required" }, { status: 400 });

  let answer: string[] = [];
  let storedOptions: string[] = [];
  if (type === "SCQ" || type === "MCQ" || type === "PARA") {
    if (options.filter(Boolean).length < 2)
      return NextResponse.json({ error: "At least 2 options required" }, { status: 400 });
    if (correct.length < 1) return NextResponse.json({ error: "Mark the correct answer" }, { status: 400 });
    if (type === "SCQ" && correct.length !== 1)
      return NextResponse.json({ error: "Single-correct must have exactly one answer" }, { status: 400 });
    storedOptions = options;
    answer = correct.map((i) => String(i));
  } else if (type === "NUMERIC") {
    const nums = (b.numericAnswers || []).map((n) => n.trim()).filter(Boolean);
    if (nums.length === 0) return NextResponse.json({ error: "Enter at least one numeric answer" }, { status: 400 });
    answer = nums;
  } else if (type === "MATRIX") {
    storedOptions = options;
    answer = (b.matrixPairs || []).map((p) => p.trim()).filter(Boolean);
  } else if (type === "SUBJECTIVE") {
    answer = [];
  }

  try {
    const db = await getDb();
    const oid = new ObjectId(id);
    const existing: any = await db.collection("cmdsquestions").findOne({ _id: oid });
    if (!existing) return NextResponse.json({ error: "Question not found" }, { status: 404 });

    const now = Date.now();
    const update = {
      type,
      questionBody: { newText: content, paragraph: (b.paragraph || "").trim() || null },
      solutionInfo: {
        optionBody: { newOptions: storedOptions },
        answer,
        explanation: (b.solution || "").trim() || null,
        globalAnsId: existing.solutionInfo?.globalAnsId ?? null,
      },
      difficulty: (b.difficulty || "EASY").toUpperCase(),
      tags: Array.isArray(b.tags) ? b.tags.filter(Boolean) : [],
      subject: (b.subject || "").trim() || null,
      boardIds: b.boardIds && b.boardIds.length ? b.boardIds : [DEFAULT_BOARD_ID],
      lastUpdated: now,
    };
    await db.collection("cmdsquestions").updateOne({ _id: oid }, { $set: update });

    if (existing.published) {
      const needsAnswer = type !== "SUBJECTIVE";
      const hexId = oid.toHexString();
      await db.collection("answers").deleteMany({ qId: hexId });
      let ansId: ObjectId | null = null;
      if (needsAnswer && answer.length > 0) {
        ansId = new ObjectId();
        await db.collection("answers").insertOne({
          _id: ansId,
          qId: hexId,
          userId: existing.userId || "",
          answer,
          qType: type,
          recordState: "ACTIVE",
          creationTime: now,
          lastUpdated: now,
        });
      }
      await db.collection("questions").updateOne(
        { _id: oid },
        {
          $set: {
            content,
            type,
            options: storedOptions,
            answerId: ansId ? ansId.toHexString() : null,
            hasAns: !!ansId,
            difficulty: update.difficulty,
            boardIds: update.boardIds,
            tags: update.tags,
            lastUpdated: now,
            "solutionInfo.explanation": update.solutionInfo.explanation,
            "solutionInfo.globalAnsId": ansId ? ansId.toHexString() : existing.solutionInfo?.globalAnsId ?? null,
          },
        }
      );
    }

    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to save question" }, { status: 500 });
  }
}
