import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Schedule / Classroom Connect — a `schedules` collection ({title, startAt,
// durationMin, teacher, center, joinUrl}).
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("schedules")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ startAt: 1 })
      .limit(300)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((s) => ({
        id: String(s._id),
        title: s.title,
        startAt: s.startAt || null,
        durationMin: s.durationMin || 60,
        teacher: s.teacher || "",
        center: s.center || "",
        joinUrl: s.joinUrl || "",
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  orgId?: string;
  title?: string;
  startAt?: number | null;
  durationMin?: number;
  teacher?: string;
  center?: string;
  joinUrl?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const title = (b.title || "").trim();
  if (!title) return NextResponse.json({ error: "Class title is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("schedules").insertOne({
      _id,
      orgId: b.orgId || DEFAULT_ORG_ID,
      title,
      startAt: b.startAt || null,
      durationMin: Math.max(15, Math.round(Number(b.durationMin) || 60)),
      teacher: (b.teacher || "").trim(),
      center: (b.center || "").trim(),
      joinUrl: (b.joinUrl || "").trim(),
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), title });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to schedule" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection("schedules")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
