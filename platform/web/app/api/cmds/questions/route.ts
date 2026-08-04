import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";

export const dynamic = "force-dynamic";

const DEFAULT_FOLDER_ID = process.env.CMDS_DEFAULT_FOLDER_ID || "6a3a9ec80cf2d6b30f9e2fd8";
const DEFAULT_BOARD_ID = process.env.CMDS_DEFAULT_BOARD_ID || "6a3b7ab30cf2f6add23ae035";

type QType = "SCQ" | "MCQ" | "NUMERIC" | "SUBJECTIVE" | "MATRIX" | "PARA";

type AddQuestionBody = {
  userId?: string;
  orgId?: string;
  content: string;
  type: QType;
  options?: string[];
  correct?: number[]; // option indices for SCQ/MCQ/PARA
  numericAnswers?: string[]; // for NUMERIC
  matrixPairs?: string[]; // for MATRIX, e.g. "A-3"
  paragraph?: string; // for PARA
  solution?: string; // worked-solution explanation
  difficulty?: string;
  tags?: string[];
  subject?: string;
  boardIds?: string[];
  folderId?: string;
};

// Author a CMDS question directly into Mongo (`cmdsquestions`) in the shape the
// publish route consumes. Supports all legacy question types; SCQ/MCQ/NUMERIC
// are auto-gradable, SUBJECTIVE/MATRIX/PARA are stored for manual grading.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as AddQuestionBody;
  const userId = b.userId || "";
  const orgId = await resolveOrgId(req, b.orgId);
  const type = (b.type || "SCQ") as QType;
  const content = (b.content || "").trim();
  const options = (b.options || []).map((o) => o.trim());
  const correct = b.correct || [];

  if (!userId) return NextResponse.json({ error: "Missing userId" }, { status: 400 });
  if (!content) return NextResponse.json({ error: "Question text is required" }, { status: 400 });

  // Validate + derive the answer key per type.
  let answer: string[] = [];
  let storedOptions: string[] = [];
  if (type === "SCQ" || type === "MCQ" || type === "PARA") {
    if (options.filter(Boolean).length < 2)
      return NextResponse.json({ error: "At least 2 options required" }, { status: 400 });
    if (correct.length < 1)
      return NextResponse.json({ error: "Mark the correct answer" }, { status: 400 });
    if (type === "SCQ" && correct.length !== 1)
      return NextResponse.json({ error: "Single-correct must have exactly one answer" }, { status: 400 });
    storedOptions = options;
    answer = correct.map((i) => String(i));
  } else if (type === "NUMERIC") {
    const nums = (b.numericAnswers || []).map((n) => n.trim()).filter(Boolean);
    if (nums.length === 0)
      return NextResponse.json({ error: "Enter at least one numeric answer" }, { status: 400 });
    answer = nums;
  } else if (type === "MATRIX") {
    storedOptions = options;
    answer = (b.matrixPairs || []).map((p) => p.trim()).filter(Boolean);
  } else if (type === "SUBJECTIVE") {
    answer = []; // manual grading
  }

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("cmdsquestions").insertOne({
      _id,
      type,
      questionBody: { newText: content, paragraph: (b.paragraph || "").trim() || null },
      solutionInfo: {
        optionBody: { newOptions: storedOptions },
        answer,
        explanation: (b.solution || "").trim() || null,
        globalAnsId: null,
      },
      difficulty: (b.difficulty || "EASY").toUpperCase(),
      tags: Array.isArray(b.tags) ? b.tags.filter(Boolean) : [],
      subject: (b.subject || "").trim() || null,
      status: "DRAFT",
      published: false,
      boardIds: b.boardIds && b.boardIds.length ? b.boardIds : [DEFAULT_BOARD_ID],
      folderId: b.folderId || DEFAULT_FOLDER_ID,
      contentSrc: { type: "ORGANIZATION", id: orgId },
      scope: "ORG",
      userId,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), ok: true, type });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to add question" }, { status: 500 });
  }
}

// DELETE: soft-delete an authored question (recordState = INACTIVE). If it
// was already published, also retire it from the gradable library so it
// stops surfacing in new tests/assignments — existing tests that already
// reference it keep their stored qId/answer untouched (matches how the
// content DELETE route treats other resource types).
export async function DELETE(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = req.nextUrl.searchParams.get("id") || "";
  if (!id || !ObjectId.isValid(id))
    return NextResponse.json({ error: "Valid id required" }, { status: 400 });

  try {
    const db = await getDb();
    const oid = new ObjectId(id);
    const now = Date.now();
    const cq = await db.collection("cmdsquestions").findOne({ _id: oid });
    if (!cq) return NextResponse.json({ error: "Question not found" }, { status: 404 });

    // A published question may already be live in a test students have
    // attempted — deleting it would silently break that test's scoring and
    // analytics with no way back. Unpublished (draft) questions are always
    // safe to remove.
    if ((cq as any).published) {
      return NextResponse.json(
        { error: "This question is published and may already be in use — it can't be deleted." },
        { status: 409 }
      );
    }

    await db
      .collection("cmdsquestions")
      .updateOne({ _id: oid }, { $set: { recordState: "INACTIVE", lastUpdated: now } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
