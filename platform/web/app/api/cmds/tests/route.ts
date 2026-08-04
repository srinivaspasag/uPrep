import { NextRequest, NextResponse } from "next/server";
import { ObjectId, Long } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { resolveOrgId } from "@/lib/org-scope";

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
// Optional ?boardIds=<id>,<id>&difficulty=&type= narrows the pool to
// questions tagged to specific Board Tree chapters — what the Auto Generate
// Test page uses instead of drawing from the entire org question bank.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const boardIds = (req.nextUrl.searchParams.get("boardIds") || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  const difficulty = req.nextUrl.searchParams.get("difficulty") || "";
  const type = req.nextUrl.searchParams.get("type") || "";

  try {
    const db = await getDb();
    const docs = await db
      .collection("questions")
      .find({
        "contentSrc.id": orgId,
        recordState: "ACTIVE",
        ...(boardIds.length ? { boardIds: { $in: boardIds } } : {}),
        ...(difficulty ? { difficulty } : {}),
        ...(type ? { type } : {}),
      } as any)
      .sort({ lastUpdated: -1 })
      .toArray();

    const questions = docs.map((q: any) => ({
      id: q._id.toString(),
      text: stripHtml(q.content),
      type: q.type || "SCQ",
      options: Array.isArray(q.options) ? q.options.length : 0,
      difficulty: q.difficulty || null,
      hasKey: !!q.hasAns,
      boardIds: Array.isArray(q.boardIds) ? q.boardIds : [],
    }));

    return NextResponse.json({ questions, orgId });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load questions" }, { status: 500 });
  }
}

type TypeMarks = Record<string, { positive?: number; negative?: number }>;
type SectionInput = {
  name?: string;
  positive?: number;
  negative?: number;
  questionIds?: string[];
  // Real legacy sections aren't one-type-each — one section's details[] can
  // mix SCQ/MCQ/Numeric, each with its OWN marks (see a real test doc's
  // metadata[].details[]). Optional per-type override on top of the
  // section-level positive/negative fallback, so an admin-defined "Section
  // 1" containing multiple question types can carry different marks per
  // type instead of forcing one uniform mark across the whole section.
  typeMarks?: TypeMarks;
};

type CreateTestBody = {
  name?: string;
  code?: string;
  sectionName?: string;
  durationMin?: number;
  positive?: number;
  negative?: number;
  questionIds?: string[];
  // Multi-subject Instant Test Generator: one metadata section per subject,
  // each with its own marks — see plan doc Part B. When present, this
  // replaces the flat sectionName/positive/negative/questionIds shape above
  // (still supported as-is for the manual single-section test creator).
  sections?: SectionInput[];
  orgId?: string;
  userId?: string;
  folderId?: string;
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
  const durationMin = Math.max(1, Math.round(Number(body.durationMin) || 30));
  const orgId = await resolveOrgId(req, body.orgId);
  const actorId = body.userId || "";

  // Normalize into a canonical multi-section shape whether the caller sent
  // the legacy flat single-section body or the new multi-subject sections[].
  const sectionInputs: { name: string; positive: number; negative: number; questionIds: string[]; typeMarks: TypeMarks }[] =
    Array.isArray(body.sections) && body.sections.length > 0
      ? body.sections.map((s) => ({
          name: (s.name || "Section").trim() || "Section",
          positive: Math.max(1, Math.round(Number(s.positive) || 4)),
          negative: Math.max(0, Math.round(Number(s.negative) || 1)),
          questionIds: Array.isArray(s.questionIds) ? s.questionIds : [],
          typeMarks: s.typeMarks && typeof s.typeMarks === "object" ? s.typeMarks : {},
        }))
      : [
          {
            name: (body.sectionName || "General").trim() || "General",
            positive: Math.max(1, Math.round(Number(body.positive) || 4)),
            negative: Math.max(0, Math.round(Number(body.negative) || 1)),
            questionIds: Array.isArray(body.questionIds) ? body.questionIds : [],
            typeMarks: {},
          },
        ];

  const questionIds = sectionInputs.flatMap((s) => s.questionIds);

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

    const n = questionIds.length;

    // Build one metadata entry per section/subject (legacy shape — a real
    // multi-subject test has one metadata[] entry per subject, each with its
    // own details[]/qIds/marks; single-section callers just get an array of
    // length 1, unchanged from before).
    const metadata = sectionInputs
      .filter((s) => s.questionIds.length > 0)
      .map((s) => {
        const ordered = s.questionIds.map((id) => byId.get(id)).filter(Boolean);
        const byType = new Map<string, string[]>();
        for (const q of ordered) {
          const t = q.type || "SCQ";
          if (!byType.has(t)) byType.set(t, []);
          byType.get(t)!.push(q._id.toString());
        }
        // A type's own marks (typeMarks[type]) win when given; otherwise
        // fall back to the section-level positive/negative — keeps the
        // single-type/single-section manual creator (which never sends
        // typeMarks) working unchanged.
        const marksFor = (type: string) => ({
          positive: Math.max(0, Math.round(Number(s.typeMarks[type]?.positive ?? s.positive))),
          negative: Math.max(0, Math.round(Number(s.typeMarks[type]?.negative ?? s.negative))),
        });
        const details = Array.from(byType.entries()).map(([type, qIds]) => ({
          type,
          qusCount: qIds.length,
          currentQuesCount: 0,
          marks: marksFor(type),
          qIds,
          maxQuestionsTobeAttempted: 0,
        }));
        const marks: Record<string, { positive: number; negative: number }> = {};
        const qIds = ordered.map((q) => q._id.toString());
        for (const q of ordered) marks[q._id.toString()] = marksFor(q.type || "SCQ");
        const sectionTotal = ordered.reduce((sum, q) => sum + marksFor(q.type || "SCQ").positive, 0);
        return {
          id: new ObjectId().toHexString(),
          name: s.name,
          qusCount: qIds.length,
          currentQuesCount: 0,
          maxQuestionsToBeAttemptedForBoard: 0,
          details,
          totalMarks: sectionTotal,
          qIds,
          marks,
        };
      });

    const allQIds = metadata.flatMap((m) => m.qIds);
    const totalMarks = metadata.reduce((sum, m) => sum + m.totalMarks, 0);
    const now = Date.now();
    const testId = new ObjectId();

    // Legacy's real Create Test form has a required "Code*" field alongside
    // Name — an admin-chosen short identifier, distinct from the auto id.
    // Honor one if given (must be unique per org among live tests); fall
    // back to the auto-generated code otherwise so the manual single-section
    // creator (which doesn't collect one) keeps working unchanged.
    const requestedCode = (body.code || "").trim();
    let code: string;
    if (requestedCode) {
      const clash = await tests.findOne({
        "contentSrc.id": orgId,
        recordState: "ACTIVE",
        code: { $regex: `^${requestedCode.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}$`, $options: "i" },
      } as any);
      if (clash) return NextResponse.json({ error: `Test code "${requestedCode}" is already in use.` }, { status: 409 });
      code = requestedCode;
    } else {
      code = "TST-" + testId.toHexString().slice(-6).toUpperCase();
    }

    const doc = {
      _id: testId,
      qusCount: n,
      actualQusCount: 0,
      duration: Long.fromNumber(durationMin * 60 * 1000),
      totalMarks,
      metadata,
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
      boardIds: metadata.map((m) => m.id),
      difficulty: "UNKNOWN",
      folderId: body.folderId || null,
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
