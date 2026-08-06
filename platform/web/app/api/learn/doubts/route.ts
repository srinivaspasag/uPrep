import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Doubts Forum — backed by the legacy `discussions` collection (contentType
// DISCUSSION). Answers live in `comments` keyed by the discussion id. This
// mirrors the legacy DiscussionsService data model closely enough to interop.
//
// Security fix: the "asked by me" tab used to filter by a client-supplied
// ?userId=, letting anyone list another student's doubts by id. The general
// browse list is intentionally public (it's a forum); only the "asked"
// filter is identity-sensitive, so that alone is now derived from the
// session (silently empty if not logged in, matching prior no-userId
// behavior) rather than gating the whole read.
export async function GET(req: NextRequest) {
  const sp = req.nextUrl.searchParams;
  const orgId = sp.get("orgId") || DEFAULT_ORG_ID;
  const tab = (sp.get("tab") || "recent").toLowerCase();
  const session = await sessionFromReq(req);
  const userId = session?.id || "";
  const q = (sp.get("q") || "").trim();

  try {
    const db = await getDb();
    const filter: Record<string, unknown> = {
      contentType: "DISCUSSION",
      recordState: "ACTIVE",
      "contentSrc.id": orgId,
    };
    if (tab === "asked" && userId) filter.userId = userId;
    if (q) filter.name = { $regex: q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), $options: "i" };

    const sort: Record<string, 1 | -1> =
      tab === "popular" ? { upVotes: -1, comments: -1, timeCreated: -1 } : { timeCreated: -1 };

    const docs = await db
      .collection("discussions")
      .find(filter)
      .sort(sort)
      .limit(100)
      .toArray();

    const items = (docs as any[]).map((d) => ({
      id: String(d._id),
      name: d.name || "(untitled doubt)",
      content: d.content || "",
      userId: d.userId || null,
      userName: d.userName || "Student",
      subject: d.subject || null,
      answerCount: d.comments ?? 0,
      upVotes: d.upVotes ?? 0,
      views: d.views ?? 0,
      state: d.state || "UNASSIGNED",
      timeCreated: d.timeCreated ?? 0,
    }));

    return NextResponse.json({ items, tab, orgId });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message || "Failed to load doubts" }, { status: 500 });
  }
}

type CreateBody = {
  name?: string;
  content?: string;
  subject?: string;
  boardIds?: string[];
};

// Security fix: userId/userName/orgId used to come straight from the
// request body, so anyone could post a doubt that displays as authored by
// another user. Author identity now comes only from the session.
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
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to post doubt" }, { status: 500 });
  }
}
