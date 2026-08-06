import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Challenges — time-boxed test competitions. A `challenges` collection
// ({name, description, testId, endAt, participants[]}).
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("challenges")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((c) => ({
        id: String(c._id),
        name: c.name,
        description: c.description || "",
        testId: c.testId || null,
        endAt: c.endAt || null,
        participants: (c.participants || []).length,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  action?: "create" | "join";
  id?: string;
  userId?: string;
  orgId?: string;
  name?: string;
  description?: string;
  testId?: string;
  endAt?: number | null;
};

export async function POST(req: NextRequest) {
  // "join" adds the caller to the participant list — must be the caller's
  // own id from the verified session, not a client-supplied userId (was
  // previously trusted as-is, so any request could add an arbitrary user).
  const session = await sessionFromReq(req);
  const b = (await req.json().catch(() => ({}))) as Body;
  try {
    const db = await getDb();
    if (b.action === "join") {
      if (!session?.id) return NextResponse.json({ error: "Sign in to join" }, { status: 401 });
      if (!b.id || !ObjectId.isValid(b.id))
        return NextResponse.json({ error: "Missing fields" }, { status: 400 });
      await db
        .collection("challenges")
        .updateOne({ _id: new ObjectId(b.id) }, { $addToSet: { participants: session.id } });
      return NextResponse.json({ ok: true });
    }
    // create — orgId/createdBy also now come from the session rather than
    // the client, for the same reason as "join" above.
    if (!session?.id) return NextResponse.json({ error: "Sign in to create a challenge" }, { status: 401 });
    const name = (b.name || "").trim();
    if (!name) return NextResponse.json({ error: "Challenge name is required" }, { status: 400 });
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("challenges").insertOne({
      _id,
      orgId: session.orgId || DEFAULT_ORG_ID,
      name,
      description: (b.description || "").trim(),
      testId: b.testId || null,
      endAt: b.endAt || null,
      participants: [],
      createdBy: session.id,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
