import { NextRequest, NextResponse } from "next/server";
import { ObjectId, Long } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Create a gradable Test from already-published library questions.
//
// This mirrors the shape the legacy content-service persists (see a real
// `tests` doc: metadata[].qIds + metadata[].marks map, qusCount == total qIds).
// We write it directly to Mongo — same approach as /api/cmds/publish — because
// the legacy Play 2.1 test-authoring endpoints can't bind their nested
// request payloads. The learn-app reads the result through the REAL content
// service (getTestInfo/getTestQuestions) and grades it through analytics.

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s.replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(/\s+/g, " ").trim();
}

// GET: list published library questions available to compose into a test.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("questions")
      .find({ "contentSrc.id": orgId, recordState: "ACTIVE" })
      .sort({ lastUpdated: -1 })
      .toArray();

    const questions = docs.map((q: any) => ({
      id: q._id.toString(),
      text: stripHtml(q.content),
      type: q.type || "SCQ",
      options: Array.isArray(q.options) ? q.options.length : 0,
      difficulty: q.difficulty || null,
      hasKey: !!q.hasAns,
    }));

    return NextResponse.json({ questions, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load questions" }, { status: 500 });
  }
}

type CreateTestBody = {
  name?: string;
  sectionName?: string;
  durationMin?: number;
  positive?: number;
  negative?: number;
  questionIds?: string[];
  orgId?: string;
  userId?: string;
  // Rules
  password?: string;
  enablePartialMarks?: boolean;
  enableSectionLocking?: boolean;
  autoResumeTest?: boolean;
  resultVisibility?: string;
};

export async function POST(req: NextRequest) {
  const body = (await req.json().catch(() => ({}))) as CreateTestBody;

  const name = (body.name || "").trim();
  const sectionName = (body.sectionName || "General").trim() || "General";
  const durationMin = Math.max(1, Math.round(Number(body.durationMin) || 30));
  const positive = Math.max(1, Math.round(Number(body.positive) || 4));
  const negative = Math.max(0, Math.round(Number(body.negative) || 1));
  const orgId = body.orgId || DEFAULT_ORG_ID;
  const actorId = body.userId || "";
  const questionIds = Array.isArray(body.questionIds) ? body.questionIds : [];

  if (!name) return NextResponse.json({ error: "Test name is required" }, { status: 400 });
  if (questionIds.length === 0)
    return NextResponse.json({ error: "Select at least one question" }, { status: 400 });

  try {
    const db = await getDb();
    const libQ = db.collection("questions");
    const answers = db.collection("answers");
    const tests = db.collection("tests");

    // Validate the picked questions: they must be published + ACTIVE in this org
    // and have a canonical Answer doc (otherwise they can't be graded).
    let oids: ObjectId[];
    try {
      oids = questionIds.map((id) => new ObjectId(id));
    } catch {
      return NextResponse.json({ error: "Invalid question id" }, { status: 400 });
    }

    const found: any[] = await libQ
      .find({ _id: { $in: oids }, "contentSrc.id": orgId, recordState: "ACTIVE" })
      .toArray();
    const byId = new Map(found.map((q) => [q._id.toString(), q]));

    const missing = questionIds.filter((id) => !byId.has(id));
    if (missing.length) {
      return NextResponse.json(
        { error: `Some questions are not published/available: ${missing.join(", ")}` },
        { status: 400 }
      );
    }

    const noKey: string[] = [];
    for (const id of questionIds) {
      const cnt = await answers.countDocuments({ qId: id });
      if (cnt === 0) noKey.push(id);
    }
    if (noKey.length) {
      return NextResponse.json(
        { error: `These questions have no answer key: ${noKey.join(", ")}` },
        { status: 400 }
      );
    }

    // Preserve the caller's ordering.
    const ordered = questionIds.map((id) => byId.get(id));
    const n = ordered.length;

    const sectionId = new ObjectId().toHexString();

    // Group qIds by question type for the `details` array (legacy shape).
    const byType = new Map<string, string[]>();
    for (const q of ordered) {
      const t = q.type || "SCQ";
      if (!byType.has(t)) byType.set(t, []);
      byType.get(t)!.push(q._id.toString());
    }
    const details = Array.from(byType.entries()).map(([type, qIds]) => ({
      type,
      qusCount: qIds.length,
      currentQuesCount: 0,
      marks: { positive, negative },
      qIds,
      maxQuestionsTobeAttempted: 0,
    }));

    // qId -> Marks map (the grading pipeline reads this).
    const marks: Record<string, { positive: number; negative: number }> = {};
    const allQIds: string[] = [];
    for (const q of ordered) {
      const hex = q._id.toString();
      allQIds.push(hex);
      marks[hex] = { positive, negative };
    }

    const totalMarks = n * positive;
    const now = Date.now();
    const testId = new ObjectId();
    const code = "TST-" + testId.toHexString().slice(-6).toUpperCase();

    const doc = {
      _id: testId,
      qusCount: n,
      actualQusCount: 0,
      duration: Long.fromNumber(durationMin * 60 * 1000),
      totalMarks,
      metadata: [
        {
          id: sectionId,
          name: sectionName,
          qusCount: n,
          currentQuesCount: 0,
          maxQuestionsToBeAttemptedForBoard: 0,
          details,
          totalMarks,
          qIds: allQIds,
          marks,
        },
      ],
      type: "TEST",
      mode: "ONLINE",
      code,
      attempts: Long.fromNumber(0),
      published: true,
      password: (body.password || "").trim() || null,
      enablePartialMarks: !!body.enablePartialMarks,
      autoResumeTest: !!body.autoResumeTest,
      oneOrMoreMarksQTypes: ["MCQ", "PARA", "MATRIX"],
      enableSectionLocking: !!body.enableSectionLocking,
      showAIR: false,
      subjectiveTest: false,
      isNTAPattern: false,
      resultVisibility: (body.resultVisibility || "VISIBLE").toUpperCase(),
      upVotes: 0,
      views: 0,
      followers: 0,
      comments: 0,
      shares: 0,
      good: 0,
      average: 0,
      bad: 0,
      boardIds: [sectionId],
      difficulty: "UNKNOWN",
      contentSrc: { type: "ORGANIZATION", id: orgId },
      completed: true,
      userId: actorId,
      scope: "ORG",
      name,
      size: {
        initialized: false,
        original: Long.fromNumber(0),
        thumbnail: Long.fromNumber(0),
        encrypted: Long.fromNumber(0),
        converted: Long.fromNumber(0),
        totalSize: Long.fromNumber(0),
        finalized: false,
      },
      timeCreated: Long.fromNumber(now),
      lastUpdated: Long.fromNumber(now),
      recordState: "ACTIVE",
    };

    await tests.insertOne(doc as any);

    return NextResponse.json({
      id: testId.toHexString(),
      name,
      code,
      qusCount: n,
      totalMarks,
      durationMin,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to create test" }, { status: 500 });
  }
}
