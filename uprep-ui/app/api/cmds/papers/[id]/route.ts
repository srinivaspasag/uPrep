import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

function stripHtml(s: unknown): string {
  if (typeof s !== "string") return "";
  return s.replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(/\s+/g, " ").trim();
}

// Returns a test's full paper (questions + answer keys + solutions) for the
// printable question/solution paper. Reads Mongo directly (content service
// deliberately strips keys).
export async function GET(_req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid test id" }, { status: 400 });
  try {
    const db = await getDb();
    const test: any = await db.collection("tests").findOne({ _id: new ObjectId(id) });
    if (!test) return NextResponse.json({ error: "Test not found" }, { status: 404 });

    const sections: any[] = [];
    for (const section of test.metadata || []) {
      const qIds: string[] = section.qIds || [];
      const oids = qIds.filter((q) => ObjectId.isValid(q)).map((q) => new ObjectId(q));
      const qDocs: any[] = await db.collection("questions").find({ _id: { $in: oids } }).toArray();
      const byId = new Map(qDocs.map((q) => [q._id.toString(), q]));
      const answers: any[] = await db.collection("answers").find({ qId: { $in: qIds } }).toArray();
      const ansByQ = new Map(answers.map((a) => [a.qId, a.answer || []]));

      const questions = qIds
        .map((qid) => byId.get(qid))
        .filter(Boolean)
        .map((q: any) => {
          const key: string[] = (ansByQ.get(q._id.toString()) || []).map((x: any) => String(x));
          const options: string[] = (q.options || []).map((o: string) => stripHtml(o));
          const answerText = key
            .map((k) => {
              const n = Number(k);
              return Number.isInteger(n) && options[n] !== undefined ? options[n] : k;
            })
            .filter(Boolean);
          return {
            id: q._id.toString(),
            content: stripHtml(q.content),
            type: q.type || "SCQ",
            options,
            answerIndices: key,
            answerText,
            solution: q.solutionInfo?.explanation || null,
            difficulty: q.difficulty || null,
          };
        });

      sections.push({ name: section.name || "Section", questions });
    }

    return NextResponse.json({
      test: { id, name: test.name || "Test", code: test.code || null, totalMarks: test.totalMarks ?? 0 },
      sections,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load paper" }, { status: 500 });
  }
}
