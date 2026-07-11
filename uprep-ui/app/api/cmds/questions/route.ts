import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

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
  const orgId = b.orgId || DEFAULT_ORG_ID;
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
