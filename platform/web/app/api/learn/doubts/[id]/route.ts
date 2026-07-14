import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

function toId(id: string): ObjectId | string {
  return ObjectId.isValid(id) ? new ObjectId(id) : id;
}

// GET one doubt + its answers (from `comments`). Also bumps the view count.
export async function GET(_req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  try {
    const db = await getDb();
    const doc: any = await db.collection("discussions").findOne({ _id: toId(id) as any });
    if (!doc) return NextResponse.json({ error: "Doubt not found" }, { status: 404 });

    await db
      .collection("discussions")
      .updateOne({ _id: toId(id) as any }, { $inc: { views: 1 } });

    const answers = await db
      .collection("comments")
      .find({ entityId: id, entityType: "DISCUSSION", recordState: "ACTIVE" })
      .sort({ timeCreated: 1 })
      .limit(200)
      .toArray();

    return NextResponse.json({
      doubt: {
        id: String(doc._id),
        name: doc.name || "(untitled doubt)",
        content: doc.content || "",
        userId: doc.userId || null,
        userName: doc.userName || "Student",
        subject: doc.subject || null,
        upVotes: doc.upVotes ?? 0,
        views: (doc.views ?? 0) + 1,
        state: doc.state || "UNASSIGNED",
        timeCreated: doc.timeCreated ?? 0,
      },
      answers: (answers as any[]).map((a) => ({
        id: String(a._id),
        content: a.content || "",
        userId: a.userId || null,
        userName: a.userName || "Member",
        timeCreated: a.timeCreated ?? 0,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load doubt" }, { status: 500 });
  }
}

type AnswerBody = { content?: string; userId?: string; userName?: string };

// POST an answer to this doubt.
export async function POST(req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  const b = (await req.json().catch(() => ({}))) as AnswerBody;
  const content = (b.content || "").trim();
  if (!content) return NextResponse.json({ error: "Answer cannot be empty" }, { status: 400 });

  try {
    const db = await getDb();
    const doubt = await db.collection("discussions").findOne({ _id: toId(id) as any });
    if (!doubt) return NextResponse.json({ error: "Doubt not found" }, { status: 404 });

    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("comments").insertOne({
      _id,
      entityId: id,
      entityType: "DISCUSSION",
      content,
      userId: b.userId || null,
      userName: (b.userName || "").trim() || "Member",
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    await db.collection("discussions").updateOne(
      { _id: toId(id) as any },
      { $inc: { comments: 1 }, $set: { lastUpdated: now, state: "ANSWERED" } }
    );

    return NextResponse.json({
      id: _id.toHexString(),
      content,
      userName: (b.userName || "").trim() || "Member",
      timeCreated: now,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to post answer" }, { status: 500 });
  }
}
