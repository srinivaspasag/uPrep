import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

// Teacher review queue for AI Tutor answers the model itself flagged as
// low-confidence (see app/api/learn/doubts/[id]/ai-answer/route.ts) —
// held out of the public doubt view until a staff member approves or
// discards them. Reached only via /api/cmds/**, already staff-gated by
// middleware.ts, so no extra role check is needed here.
function toId(id: string): ObjectId | string {
  return ObjectId.isValid(id) ? new ObjectId(id) : id;
}

export async function GET() {
  try {
    const db = await getDb();
    const pending = await db
      .collection("comments")
      .find({ userId: "ai-tutor", status: "pending_review", recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();

    const doubtIds = Array.from(new Set(pending.map((c: any) => c.entityId).filter(Boolean)));
    const doubts = doubtIds.length
      ? await db
          .collection("discussions")
          .find({ _id: { $in: doubtIds.map(toId) as any[] } })
          .toArray()
      : [];
    const doubtById = new Map(doubts.map((d: any) => [String(d._id), d]));

    const items = (pending as any[]).map((c) => {
      const doubt = doubtById.get(String(c.entityId));
      return {
        id: String(c._id),
        doubtId: c.entityId,
        doubtName: doubt?.name || "(doubt not found)",
        content: c.content,
        confidence: c.aiMeta?.confidence || null,
        reasoning: c.aiMeta?.reasoning || null,
        groundedOn: c.aiMeta?.groundedOn || null,
        timeCreated: c.timeCreated ?? 0,
      };
    });

    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message || "Failed to load the review queue" }, { status: 500 });
  }
}

type ReviewBody = { commentId?: string; action?: "approve" | "discard" };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as ReviewBody;
  const commentId = b.commentId || "";
  if (!commentId || !ObjectId.isValid(commentId)) {
    return NextResponse.json({ error: "A valid commentId is required" }, { status: 400 });
  }
  if (b.action !== "approve" && b.action !== "discard") {
    return NextResponse.json({ error: "action must be \"approve\" or \"discard\"" }, { status: 400 });
  }

  try {
    const db = await getDb();
    const comment: any = await db.collection("comments").findOne({ _id: new ObjectId(commentId) });
    if (!comment || comment.userId !== "ai-tutor") {
      return NextResponse.json({ error: "Pending AI answer not found" }, { status: 404 });
    }

    const now = Date.now();
    if (b.action === "approve") {
      await db
        .collection("comments")
        .updateOne({ _id: comment._id }, { $unset: { status: "" }, $set: { lastUpdated: now } });
      await db
        .collection("discussions")
        .updateOne({ _id: toId(comment.entityId) as any }, { $inc: { comments: 1 }, $set: { lastUpdated: now } });
    } else {
      await db
        .collection("comments")
        .updateOne({ _id: comment._id }, { $set: { recordState: "INACTIVE", lastUpdated: now } });
    }

    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to update the review queue" }, { status: 500 });
  }
}
