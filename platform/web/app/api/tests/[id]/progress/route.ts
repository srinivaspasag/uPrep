import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const PROGRESS_COLL = "testprogress";

// In-progress attempt state so a student can pause a test and resume later (the
// legacy app persisted attempt state server-side; the new take-test flow kept
// it only in memory). Keyed by (userId, testId).
async function userId(req: NextRequest): Promise<string | null> {
  const s = await sessionFromReq(req);
  return s?.id || null;
}

export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const uid = await userId(req);
  if (!uid) return NextResponse.json({ progress: null }, { status: 401 });
  try {
    const db = await getDb();
    const doc: any = await db
      .collection(PROGRESS_COLL)
      .findOne({ userId: uid, testId: params.id });
    if (!doc) return NextResponse.json({ progress: null });
    return NextResponse.json({
      progress: {
        answers: doc.answers || {},
        remaining: doc.remaining ?? null,
        updatedAt: doc.updatedAt,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ progress: null, error: e?.message }, { status: 500 });
  }
}

type SaveBody = { answers?: Record<string, number>; remaining?: number };

export async function PUT(req: NextRequest, { params }: { params: { id: string } }) {
  const uid = await userId(req);
  if (!uid) return NextResponse.json({ error: "Not signed in" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as SaveBody;
  try {
    const db = await getDb();
    await db.collection(PROGRESS_COLL).updateOne(
      { userId: uid, testId: params.id },
      {
        $set: {
          answers: b.answers || {},
          remaining: typeof b.remaining === "number" ? b.remaining : null,
          updatedAt: Date.now(),
        },
      },
      { upsert: true }
    );
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest, { params }: { params: { id: string } }) {
  const uid = await userId(req);
  if (!uid) return NextResponse.json({ error: "Not signed in" }, { status: 401 });
  try {
    const db = await getDb();
    await db.collection(PROGRESS_COLL).deleteOne({ userId: uid, testId: params.id });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Clear failed" }, { status: 500 });
  }
}
