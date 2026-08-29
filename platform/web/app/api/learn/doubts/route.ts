import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { ensureAiAnswer, type AnswerMode } from "@/lib/aiTutor";

export const dynamic = "force-dynamic";

// Doubts are private per-student ("My Doubts" + Aira, not a public forum) —
// backed by the same legacy `discussions`/`comments` collections as before,
// just always scoped to the caller's own session rather than browsable by
// tab. See app/learn/doubts/layout.tsx for the list UI this feeds.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated", items: [] }, { status: 401 });

  const sp = req.nextUrl.searchParams;
  const state = (sp.get("state") || "").toLowerCase(); // "open" | "resolved" | "" (all)
  const q = (sp.get("q") || "").trim();

  try {
    const db = await getDb();
    const filter: Record<string, unknown> = {
      contentType: "DISCUSSION",
      recordState: "ACTIVE",
      userId: session.id,
    };
    if (state === "open") filter.state = { $ne: "ANSWERED" };
    if (state === "resolved") filter.state = "ANSWERED";
    if (q) filter.name = { $regex: q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), $options: "i" };

    const docs = await db
      .collection("discussions")
      .find(filter)
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();

    const items = (docs as any[]).map((d) => ({
      id: String(d._id),
      name: d.name || "(untitled doubt)",
      content: d.content || "",
      subject: d.subject || null,
      answerCount: d.comments ?? 0,
      state: d.state || "UNASSIGNED",
      timeCreated: d.timeCreated ?? 0,
    }));

    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message || "Failed to load doubts" }, { status: 500 });
  }
}

type CreateBody = {
  name?: string;
  content?: string;
  subject?: string;
  boardIds?: string[];
  mode?: AnswerMode;
};

const VALID_MODES: AnswerMode[] = ["detailed", "short", "guided"];

// Security fix (kept from before this rewrite): userId/userName/orgId used
// to come straight from the request body, so anyone could post a doubt
// that displays as authored by another user. Author identity only ever
// comes from the session.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  const content = (b.content || "").trim();
  const orgId = session.orgId || DEFAULT_ORG_ID;
  if (!name) return NextResponse.json({ error: "A question title is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("discussions").insertOne({
      _id,
      name,
      content,
      contentType: "DISCUSSION",
      state: "UNASSIGNED",
      rating: 0,
      upVotes: 0,
      views: 0,
      followers: [],
      comments: 0,
      boardIds: Array.isArray(b.boardIds) ? b.boardIds.filter(Boolean).map(String) : [],
      targetIds: [],
      tags: [],
      subject: (b.subject || "").trim() || null,
      userId: session.id,
      userName: [session.firstName, session.lastName].filter(Boolean).join(" ") || "Student",
      contentSrc: { type: "ORGANIZATION", id: orgId },
      scope: "ORG",
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });

    // Aira answers every new doubt automatically — awaited so the doubt
    // already has (or has attempted) an answer by the time the client
    // navigates to it, rather than showing a blank thread that only fills
    // in on a later refetch. Never blocks/fails doubt creation itself —
    // ensureAiAnswer swallows its own errors.
    const mode = VALID_MODES.includes(b.mode as AnswerMode) ? (b.mode as AnswerMode) : "guided";
    await ensureAiAnswer(db, _id.toHexString(), mode);

    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to post doubt" }, { status: 500 });
  }
}
