import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { aiTutorConfigured, ensureAiAnswer } from "@/lib/aiTutor";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

function toId(id: string): ObjectId | string {
  return ObjectId.isValid(id) ? new ObjectId(id) : id;
}

// Aira now answers automatically when a doubt is posted (see
// POST /api/learn/doubts). This endpoint is kept for a manual re-ask —
// ensureAiAnswer() is idempotent, so calling it here just returns whatever
// already exists unless there's genuinely no answer yet.
export async function POST(req: NextRequest, { params }: { params: { id: string } }) {
  if (!aiTutorConfigured()) {
    return NextResponse.json({ error: "AI answers aren't configured on this server" }, { status: 501 });
  }
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });

  const id = params.id;
  try {
    const db = await getDb();
    const doubt = await db.collection("discussions").findOne({ _id: toId(id) as any });
    if (!doubt) return NextResponse.json({ error: "Doubt not found" }, { status: 404 });

    await ensureAiAnswer(db, id);

    const answer: any = await db
      .collection("comments")
      .findOne({ entityId: id, entityType: "DISCUSSION", userId: "ai-tutor", recordState: "ACTIVE" });
    if (!answer) return NextResponse.json({ error: "Aira couldn't answer this one" }, { status: 500 });

    return NextResponse.json({
      id: String(answer._id),
      content: answer.content,
      steps: answer.aiMeta?.steps || null,
      userName: "Aira",
      timeCreated: answer.timeCreated,
      confidence: answer.aiMeta?.confidence || null,
      pending: answer.status === "pending_review",
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to generate an AI answer" }, { status: 500 });
  }
}
