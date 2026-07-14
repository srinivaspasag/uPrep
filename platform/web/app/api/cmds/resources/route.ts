import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Lists authored CMDS resources (questions / tests / modules) for an org directly
// from MongoDB. These are the *authoring* collections (cmds*) that feed the
// publish pipeline; the published/library docs live in questions/tests/modules.
// Direct Mongo read mirrors the library route (legacy search endpoints unindexed).

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s
    .replace(/<[^>]*>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function hasAnswerKey(solutionInfo: any): boolean {
  const a = solutionInfo?.answer;
  if (a == null) return false;
  if (Array.isArray(a)) return a.length > 0;
  return String(a).length > 0;
}

export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const kind = req.nextUrl.searchParams.get("kind") || "all"; // question | test | module | all

  try {
    const db = await getDb();
    const filter = { "contentSrc.id": orgId, recordState: "ACTIVE" };

    const out: {
      questions: any[];
      tests: any[];
      modules: any[];
    } = { questions: [], tests: [], modules: [] };

    if (kind === "question" || kind === "all") {
      const docs = await db
        .collection("cmdsquestions")
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(200)
        .toArray();
      out.questions = (docs as any[]).map((d) => ({
        id: String(d._id),
        text: stripHtml(d.questionBody?.newText).slice(0, 160),
        type: d.type || "UNKNOWN",
        difficulty: d.difficulty ?? null,
        published: !!d.published,
        completed: !!d.completed,
        status: d.status || "INCOMPLETE",
        hasKey: hasAnswerKey(d.solutionInfo),
        options: d.solutionInfo?.optionBody?.newOptions?.length ?? 0,
      }));
    }

    if (kind === "test" || kind === "all") {
      const docs = await db
        .collection("cmdstests")
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();
      out.tests = (docs as any[]).map((d) => ({
        id: String(d._id),
        name: d.name || "(untitled test)",
        type: d.type || "TEST",
        qusCount: d.actualQusCount ?? d.qusCount ?? 0,
        totalMarks: d.totalMarks ?? 0,
        durationMin: d.duration ? Math.round(d.duration / 60000) : 0,
        published: !!d.published,
        completed: !!d.completed,
      }));
    }

    if (kind === "module" || kind === "all") {
      const docs = await db
        .collection("cmdsmodules")
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();
      out.modules = (docs as any[]).map((d) => ({
        id: String(d._id),
        name: d.name || "(untitled module)",
        contentCount: d.totalContentCount ?? 0,
        published: !!d.published,
        completed: !!d.completed,
      }));
    }

    return NextResponse.json({ ...out, orgId });
  } catch (e: any) {
    return NextResponse.json(
      { questions: [], tests: [], modules: [], error: e?.message || "Failed to load CMDS resources" },
      { status: 500 }
    );
  }
}
