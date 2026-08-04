import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";

export const dynamic = "force-dynamic";

// Test edit — mirrors legacy's real rule (CMDSTestManager.modifyTestQuestions,
// which throws ALREADY_PUBLISHED the moment a test has been published/shared):
// metadata (name, duration, password, result visibility, rules) can always be
// changed, but the question set itself is locked the moment the test has a
// real student attempt on it — editing questions on a live test would leave
// its own analytics mixing results from different question sets. There's no
// separate CMDS-draft/live Test split in this rebuild (unlike legacy's dual
// CMDSTest/Test model), so "has attempts" is the equivalent gate here.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid test id" }, { status: 400 });

  try {
    const db = await getDb();
    const test: any = await db.collection("tests").findOne({ _id: new ObjectId(id) });
    if (!test) return NextResponse.json({ error: "Test not found" }, { status: 404 });

    const attemptCount = await db
      .collection("userentityattempts")
      .countDocuments({ "entity.type": "TEST", "entity.id": id, finished: true });

    const md = Array.isArray(test.metadata) ? test.metadata : [];
    // Flatten every section's qIds/marks into the single-section shape the
    // manual create form (and this edit form) uses — a test authored via the
    // multi-subject Auto Generate wizard has >1 section; editing questions on
    // those is unsupported here (name/settings still editable), same
    // real-world scope legacy's own edit form has.
    const sections = md.map((m: any) => ({
      name: m.name || "Section",
      questionIds: Array.isArray(m.qIds) ? m.qIds : [],
      positive: Object.values(m.marks || {})[0] ? (Object.values(m.marks || {})[0] as any).positive : 4,
      negative: Object.values(m.marks || {})[0] ? (Object.values(m.marks || {})[0] as any).negative : 1,
    }));

    return NextResponse.json({
      test: {
        id: String(test._id),
        name: test.name || "",
        durationMin: test.duration ? Math.round(Number(test.duration) / 60000) : 30,
        password: test.password || "",
        resultVisibility: test.resultVisibility || "VISIBLE",
        enablePartialMarks: !!test.enablePartialMarks,
        enableSectionLocking: !!test.enableSectionLocking,
        autoResumeTest: !!test.autoResumeTest,
        folderId: test.folderId || null,
      },
      sections,
      singleSection: sections.length <= 1,
      attemptCount,
      questionsLocked: attemptCount > 0,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load test" }, { status: 500 });
  }
}

type EditTestBody = {
  name?: string;
  durationMin?: number;
  password?: string;
  resultVisibility?: string;
  enablePartialMarks?: boolean;
  enableSectionLocking?: boolean;
  autoResumeTest?: boolean;
  // Question-set edits — only honored if the test has zero finished attempts.
  sectionName?: string;
  positive?: number;
  negative?: number;
  questionIds?: string[];
  orgId?: string;
};

export async function PATCH(req: NextRequest, { params }: { params: { id: string } }) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid test id" }, { status: 400 });

  const body = (await req.json().catch(() => ({}))) as EditTestBody;
  const orgId = await resolveOrgId(req, body.orgId);

  try {
    const db = await getDb();
    const tests = db.collection("tests");
    const existing: any = await tests.findOne({ _id: new ObjectId(id) });
    if (!existing) return NextResponse.json({ error: "Test not found" }, { status: 404 });

    const attemptCount = await db
      .collection("userentityattempts")
      .countDocuments({ "entity.type": "TEST", "entity.id": id, finished: true });

    const set: Record<string, unknown> = { lastUpdated: Date.now() };

    // Metadata — always editable.
    if (body.name !== undefined) {
      const name = body.name.trim();
      if (!name) return NextResponse.json({ error: "Test name is required" }, { status: 400 });
      set.name = name;
    }
    if (body.durationMin !== undefined) set.duration = Math.max(1, Math.round(Number(body.durationMin) || 30)) * 60 * 1000;
    if (body.password !== undefined) set.password = body.password.trim() || null;
    if (body.resultVisibility !== undefined) set.resultVisibility = body.resultVisibility.toUpperCase();
    if (body.enablePartialMarks !== undefined) set.enablePartialMarks = !!body.enablePartialMarks;
    if (body.enableSectionLocking !== undefined) set.enableSectionLocking = !!body.enableSectionLocking;
    if (body.autoResumeTest !== undefined) set.autoResumeTest = !!body.autoResumeTest;

    // Question set — locked once the test has a real finished attempt, same
    // rule legacy enforces for a published/shared test.
    if (body.questionIds !== undefined) {
      if (attemptCount > 0) {
        return NextResponse.json(
          { error: "This test already has student attempts — its question set can't be changed anymore." },
          { status: 409 }
        );
      }
      const questionIds = Array.isArray(body.questionIds) ? body.questionIds : [];
      if (questionIds.length === 0)
        return NextResponse.json({ error: "Select at least one question" }, { status: 400 });

      let oids: ObjectId[];
      try {
        oids = questionIds.map((qid) => new ObjectId(qid));
      } catch {
        return NextResponse.json({ error: "Invalid question id" }, { status: 400 });
      }

      const found = await db
        .collection("questions")
        .find({ _id: { $in: oids }, "contentSrc.id": orgId, recordState: "ACTIVE" })
        .toArray();
      const byId = new Map(found.map((q: any) => [q._id.toString(), q]));
      const missing = questionIds.filter((qid) => !byId.has(qid));
      if (missing.length)
        return NextResponse.json(
          { error: `Some questions are not published/available: ${missing.join(", ")}` },
          { status: 400 }
        );

      const positive = Math.max(1, Math.round(Number(body.positive) || 4));
      const negative = Math.max(0, Math.round(Number(body.negative) || 1));
      const ordered = questionIds.map((qid) => byId.get(qid)).filter(Boolean) as any[];
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
      const marks: Record<string, { positive: number; negative: number }> = {};
      for (const qid of questionIds) marks[qid] = { positive, negative };
      const totalMarks = questionIds.length * positive;
      const existingSection = Array.isArray(existing.metadata) && existing.metadata[0] ? existing.metadata[0] : {};

      set.metadata = [
        {
          id: existingSection.id || new ObjectId().toHexString(),
          name: (body.sectionName || existingSection.name || "General").trim() || "General",
          qusCount: questionIds.length,
          currentQuesCount: 0,
          maxQuestionsToBeAttemptedForBoard: 0,
          details,
          totalMarks,
          qIds: questionIds,
          marks,
        },
      ];
      set.qusCount = questionIds.length;
      set.totalMarks = totalMarks;
    }

    await tests.updateOne({ _id: new ObjectId(id) }, { $set: set });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to update test" }, { status: 500 });
  }
}
