import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Ratings & reviews — a `reviews` collection ({entityId, userId, rating 1-5,
// comment}). GET returns the aggregate + recent reviews for an entity (and the
// caller's own rating). POST upserts the caller's review.
//
// Security fix: "mine" used to be looked up by a client-supplied ?userId=,
// letting anyone learn what rating another user left. The aggregate/review
// list itself is intentionally public (like any product review page); only
// "mine" is identity-sensitive, so it's now derived from the session (null
// if not logged in) rather than gating the whole read.
export async function GET(req: NextRequest) {
  const entityId = req.nextUrl.searchParams.get("entityId") || "";
  const session = await sessionFromReq(req);
  const userId = session?.id || "";
  if (!entityId) return NextResponse.json({ average: 0, count: 0, reviews: [], mine: null });
  try {
    const db = await getDb();
    const docs = await db
      .collection("reviews")
      .find({ entityId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();
    const ratings = (docs as any[]).map((r) => r.rating || 0).filter((n) => n > 0);
    const average = ratings.length ? ratings.reduce((a, b) => a + b, 0) / ratings.length : 0;
    const mine = (docs as any[]).find((r) => r.userId === userId) || null;
    return NextResponse.json({
      average: Math.round(average * 10) / 10,
      count: ratings.length,
      mine: mine ? { rating: mine.rating, comment: mine.comment || "" } : null,
      reviews: (docs as any[])
        .filter((r) => r.comment)
        .slice(0, 20)
        .map((r) => ({
          id: String(r._id),
          rating: r.rating,
          comment: r.comment,
          userName: r.userName || "Student",
          at: r.timeCreated || 0,
        })),
    });
  } catch (e: any) {
    return NextResponse.json({ average: 0, count: 0, reviews: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  entityId?: string;
  entityType?: string;
  userName?: string;
  rating?: number;
  comment?: string;
};

export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userId = session.id;
  const b = (await req.json().catch(() => ({}))) as Body;
  const entityId = b.entityId || "";
  const rating = Number(b.rating || 0);
  if (!entityId) return NextResponse.json({ error: "Missing fields" }, { status: 400 });
  if (rating < 1 || rating > 5) return NextResponse.json({ error: "Rating must be 1–5" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    await db.collection("reviews").updateOne(
      { entityId, userId },
      {
        $set: {
          entityId,
          entityType: b.entityType || "CONTENT",
          userId,
          userName: b.userName || "Student",
          rating,
          comment: (b.comment || "").trim(),
          orgId: session.orgId,
          recordState: "ACTIVE",
          lastUpdated: now,
        },
        $setOnInsert: { _id: new ObjectId(), timeCreated: now },
      },
      { upsert: true }
    );
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
