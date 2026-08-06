import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Class chat — a `messages` collection ({channel, userId, userName, text}).
// Real-time in legacy uses websockets; here the client polls this endpoint.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const channel = req.nextUrl.searchParams.get("channel") || "general";
  const since = Number(req.nextUrl.searchParams.get("since") || 0);
  try {
    const db = await getDb();
    const filter: any = { orgId, channel };
    if (since) filter.timeCreated = { $gt: since };
    const docs = await db
      .collection("messages")
      .find(filter)
      .sort({ timeCreated: 1 })
      .limit(200)
      .toArray();
    return NextResponse.json({
      messages: (docs as any[]).map((m) => ({
        id: String(m._id),
        userId: m.userId,
        userName: m.userName || "Student",
        text: m.text,
        at: m.timeCreated || 0,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ messages: [], error: e?.message }, { status: 500 });
  }
}

type Body = { channel?: string; text?: string };

// Security fix: userId/userName used to come straight from the request
// body, so anyone could post a chat message that displays as sent by
// another user. Author identity now comes only from the session.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as Body;
  const text = (b.text || "").trim();
  if (!text) return NextResponse.json({ error: "Empty message" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("messages").insertOne({
      _id,
      orgId: session.orgId || DEFAULT_ORG_ID,
      channel: b.channel || "general",
      userId: session.id,
      userName: [session.firstName, session.lastName].filter(Boolean).join(" ") || "Student",
      text,
      timeCreated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), at: now });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to send" }, { status: 500 });
  }
}
