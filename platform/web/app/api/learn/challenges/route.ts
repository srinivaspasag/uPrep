import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

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
  const b = (await req.json().catch(() => ({}))) as Body;
  try {
    const db = await getDb();
    if (b.action === "join") {
      if (!b.id || !ObjectId.isValid(b.id) || !b.userId)
        return NextResponse.json({ error: "Missing fields" }, { status: 400 });
      await db
        .collection("challenges")
        .updateOne({ _id: new ObjectId(b.id) }, { $addToSet: { participants: b.userId } });
      return NextResponse.json({ ok: true });
    }
    // create
    const name = (b.name || "").trim();
    if (!name) return NextResponse.json({ error: "Challenge name is required" }, { status: 400 });
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("challenges").insertOne({
      _id,
      orgId: b.orgId || DEFAULT_ORG_ID,
      name,
      description: (b.description || "").trim(),
      testId: b.testId || null,
      endAt: b.endAt || null,
      participants: [],
      createdBy: b.userId || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
