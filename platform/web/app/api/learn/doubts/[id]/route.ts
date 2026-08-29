import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";

export const dynamic = "force-dynamic";

function toId(id: string): ObjectId | string {
  return ObjectId.isValid(id) ? new ObjectId(id) : id;
}

// Content already on the platform tagged to the same chapter(s) as the
// doubt — shown as "matching solutions" alongside Aira's answer, the same
// way a student would find it browsing the Digital Library, just surfaced
// proactively instead of requiring a separate search.
async function relatedContentFor(db: any, orgId: string, boardIds: string[]) {
  if (!boardIds.length) return [];
  const filter = { boardIds: { $in: boardIds }, "contentSrc.id": orgId, recordState: "ACTIVE" };
  const [videos, books, documents] = await Promise.all([
    db.collection("videos").find(filter).sort({ lastUpdated: -1 }).limit(4).toArray(),
    db.collection("books").find(filter).sort({ lastUpdated: -1 }).limit(2).toArray(),
    db.collection("documents").find(filter).sort({ lastUpdated: -1 }).limit(2).toArray(),
  ]);
  const items = [
    ...(videos as any[]).map((v) => ({
      id: String(v._id),
      type: "VIDEO" as const,
      name: v.name || "(untitled video)",
      url: v.url || null,
      embedUrl: v.embedUrl || null,
      provider: v.provider || null,
    })),
    ...(books as any[]).map((b) => ({
      id: String(b._id),
      type: "BOOK" as const,
      name: b.name || "(untitled book)",
      url: b.url || null,
      embedUrl: null,
      provider: null,
    })),
    ...(documents as any[]).map((d) => ({
      id: String(d._id),
      type: "DOCUMENT" as const,
      name: d.name || "(untitled document)",
      url: d.url || null,
      embedUrl: null,
      provider: null,
    })),
  ];
  return items.slice(0, 6);
}

// GET one doubt + its answers (from `comments`). Also bumps the view count.
// Doubts are private (see app/api/learn/doubts/route.ts) — only the asker
// or staff can read one, not just anyone who has/guesses the id.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  try {
    const db = await getDb();
    const doc: any = await db.collection("discussions").findOne({ _id: toId(id) as any });
    if (!doc) return NextResponse.json({ error: "Doubt not found" }, { status: 404 });
    if (doc.userId !== session.id && !isStaff(session.profile)) {
      return NextResponse.json({ error: "Not authorized" }, { status: 403 });
    }

    await db
      .collection("discussions")
      .updateOne({ _id: toId(id) as any }, { $inc: { views: 1 } });

    // AI Tutor answers awaiting teacher review (status: "pending_review",
    // see lib/aiTutor.ts) are held back from this public view until
    // approved — but the client needs to tell "genuinely pending review"
    // apart from "Aira never answered at all" (a Groq failure), since those
    // look identical if all we return is the filtered answers list.
    const [answers, aiPending] = await Promise.all([
      db
        .collection("comments")
        .find({ entityId: id, entityType: "DISCUSSION", recordState: "ACTIVE", status: { $ne: "pending_review" } })
        .sort({ timeCreated: 1 })
        .limit(200)
        .toArray(),
      db
        .collection("comments")
        .findOne({ entityId: id, entityType: "DISCUSSION", recordState: "ACTIVE", userId: "ai-tutor", status: "pending_review" }),
    ]);

    const boardIds: string[] = Array.isArray(doc.boardIds) ? doc.boardIds.filter(Boolean) : [];
    const orgId = doc.contentSrc?.id || DEFAULT_ORG_ID;
    const relatedContent = await relatedContentFor(db, orgId, boardIds);

    return NextResponse.json({
      relatedContent,
      aiPending: !!aiPending,
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
        isAi: a.userId === "ai-tutor",
        steps: a.aiMeta?.steps || null,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load doubt" }, { status: 500 });
  }
}

type AnswerBody = { content?: string };

// POST an answer to this doubt.
// Security fix: userId/userName used to come straight from the request
// body, so anyone could post an answer that displays as authored by
// another user. Author identity now comes only from the session.
export async function POST(req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userName = [session.firstName, session.lastName].filter(Boolean).join(" ") || "Member";
  const b = (await req.json().catch(() => ({}))) as AnswerBody;
  const content = (b.content || "").trim();
  if (!content) return NextResponse.json({ error: "Answer cannot be empty" }, { status: 400 });

  try {
    const db = await getDb();
    const doubt: any = await db.collection("discussions").findOne({ _id: toId(id) as any });
    if (!doubt) return NextResponse.json({ error: "Doubt not found" }, { status: 404 });
    if (doubt.userId !== session.id && !isStaff(session.profile)) {
      return NextResponse.json({ error: "Not authorized" }, { status: 403 });
    }

    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("comments").insertOne({
      _id,
      entityId: id,
      entityType: "DISCUSSION",
      content,
      userId: session.id,
      userName,
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
      userName,
      timeCreated: now,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to post answer" }, { status: 500 });
  }
}
