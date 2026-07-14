import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Assignments — a lightweight `assignments` collection ({name, description,
// dueDate, maxMarks}). Students submit text/file against these.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("assignments")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();
    const items = await Promise.all(
      (docs as any[]).map(async (a) => ({
        id: String(a._id),
        name: a.name,
        description: a.description || "",
        dueDate: a.dueDate || null,
        maxMarks: a.maxMarks || 0,
        submissions: await db.collection("submissions").countDocuments({ assignmentId: String(a._id) }),
      }))
    );
    return NextResponse.json({ items, orgId });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  userId?: string;
  orgId?: string;
  name?: string;
  description?: string;
  dueDate?: number | null;
  maxMarks?: number;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Assignment name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("assignments").insertOne({
      _id,
      name,
      description: (b.description || "").trim(),
      dueDate: b.dueDate || null,
      maxMarks: Math.max(0, Math.round(Number(b.maxMarks) || 0)),
      orgId: b.orgId || DEFAULT_ORG_ID,
      createdBy: b.userId || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to create assignment" }, { status: 500 });
  }
}
