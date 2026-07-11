import { NextRequest, NextResponse } from "next/server";
import { ObjectId, Long } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

// Publishes authored CMDS questions into the gradable content library.
//
// NOTE: This mirrors the backend `QuestionPublisher` (creates the library
// `Question` + canonical `Answer` docs and flips the CMDS question to
// published/COMPLETE). We do it server-side here because the legacy
// `cmdsResources/publish` endpoint can't bind its `List<SrcEntity>` payload
// under Play 2.1 (public fields, no setters). When the backend is rebuilt for
// AWS with proper accessors, this route can delegate to that endpoint instead.

function toAnswerArray(solutionInfo: any): string[] {
  const a = solutionInfo?.answer;
  if (a == null) return [];
  if (Array.isArray(a)) return a.map((x: any) => String(x));
  return [String(a)];
}

export async function POST(req: NextRequest) {
  const body = (await req.json().catch(() => ({}))) as { ids?: string[]; userId?: string };
  const ids = Array.isArray(body.ids) ? body.ids : [];
  const actorId = body.userId || "";
  if (ids.length === 0) return NextResponse.json({ error: "No question ids provided" }, { status: 400 });

  const db = await getDb();
  const cmdsQ = db.collection("cmdsquestions");
  const libQ = db.collection("questions");
  const answers = db.collection("answers");

  const results: { id: string; status: string; message?: string }[] = [];

  for (const id of ids) {
    try {
      const oid = new ObjectId(id);
      const cq: any = await cmdsQ.findOne({ _id: oid });
      if (!cq) {
        results.push({ id, status: "NOT_FOUND" });
        continue;
      }
      const type: string = cq.type || "SCQ";
      const options: string[] = cq.solutionInfo?.optionBody?.newOptions || [];
      const answerArr = toAnswerArray(cq.solutionInfo);
      const needsOptions = type === "SCQ" || type === "MCQ" || type === "PARA" || type === "MATRIX";
      const needsAnswer = type !== "SUBJECTIVE"; // subjective is manually graded
      if (needsOptions && options.length === 0) {
        results.push({ id, status: "SKIPPED", message: "Question has no options" });
        continue;
      }
      if (needsAnswer && answerArr.length === 0) {
        results.push({ id, status: "SKIPPED", message: "Question has no answer key" });
        continue;
      }

      const now = Date.now();
      const hexId = oid.toHexString();

      // 1) canonical Answer doc (unique per qId) — replace if re-publishing.
      //    Subjective questions have no auto-answer key (manually graded).
      await answers.deleteMany({ qId: hexId });
      let ansId: ObjectId | null = null;
      if (needsAnswer) {
        ansId = new ObjectId();
        await answers.insertOne({
          _id: ansId,
          qId: hexId,
          userId: cq.userId || actorId,
          answer: answerArr,
          qType: type,
          recordState: "ACTIVE",
          creationTime: now,
          lastUpdated: now,
        });
      }

      // 2) library Question doc (reuses the CMDS _id, as the backend does)
      await libQ.updateOne(
        { _id: oid },
        {
          $set: {
            content: cq.questionBody?.newText || "",
            type,
            options,
            answerId: ansId ? ansId.toHexString() : null,
            hasAns: !!ansId,
            difficulty: cq.difficulty || "EASY",
            boardIds: cq.boardIds || [],
            tags: cq.tags || [],
            contentSrc: cq.contentSrc || null,
            scope: cq.scope || "PUBLIC",
            userId: cq.userId || actorId,
            recordState: "ACTIVE",
            lastUpdated: now,
            "solutionInfo.explanation": cq.solutionInfo?.explanation || null,
            "solutionInfo.globalAnsId": ansId ? ansId.toHexString() : null,
          },
          $setOnInsert: { timeCreated: Long.fromNumber(now), code: hexId.slice(-8) },
        },
        { upsert: true }
      );

      // 3) flip the CMDS question to published/COMPLETE
      await cmdsQ.updateOne(
        { _id: oid },
        {
          $set: {
            published: true,
            status: "COMPLETE",
            globalQid: hexId,
            publishedOn: now,
            lastUpdated: now,
          },
        }
      );

      results.push({ id, status: "PUBLISHED" });
    } catch (e: any) {
      results.push({ id, status: "ERROR", message: e?.message });
    }
  }

  const published = results.filter((r) => r.status === "PUBLISHED").length;
  return NextResponse.json({ published, total: ids.length, results });
}
