import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { SECTIONS_COLL } from "@/lib/sections";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Test scheduling — mirrors the legacy "Schedule a Test" flow: an admin picks a
// test, targets one or more sections (empty = whole institute), and sets an
// availability window [startAt, endAt]. Students only see/start the test inside
// that window; admins get a countdown. Stored in the `testschedules` collection.
const COLL = "testschedules";

// Attach computed status + countdown so the admin list can render live state.
function withStatus(s: any) {
  const now = Date.now();
  const startAt = Number(s.startAt) || 0;
  const endAt = Number(s.endAt) || 0;
  let status: "UPCOMING" | "LIVE" | "ENDED" = "UPCOMING";
  if (startAt && now >= startAt) status = endAt && now > endAt ? "ENDED" : "LIVE";
  return {
    id: String(s._id),
    testId: s.testId,
    testName: s.testName || "Test",
    sectionIds: Array.isArray(s.sectionIds) ? s.sectionIds : [],
    sectionNames: Array.isArray(s.sectionNames) ? s.sectionNames : [],
    startAt: startAt || null,
    endAt: endAt || null,
    durationMin: s.durationMin || null,
    status,
    startsInMs: startAt ? startAt - now : null,
    endsInMs: endAt ? endAt - now : null,
  };
}

// GET: schedules for the org (+ tests and sections for the create form).
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();

    const [schedules, tests, sections] = await Promise.all([
      db.collection(COLL).find({ orgId, recordState: "ACTIVE" } as any).sort({ startAt: 1 }).toArray(),
      db
        .collection("tests")
        .find({ "contentSrc.id": orgId, recordState: "ACTIVE" } as any)
        .sort({ lastUpdated: -1 })
        .limit(500)
        .toArray(),
      db.collection(SECTIONS_COLL).find({ orgId, recordState: "ACTIVE" } as any).sort({ name: 1 }).toArray(),
    ]);

    return NextResponse.json({
      schedules: (schedules as any[]).map(withStatus),
      tests: (tests as any[]).map((t) => ({
        id: String(t._id),
        name: t.name || t.title || "(untitled test)",
      })),
      sections: (sections as any[]).map((s) => ({ id: String(s._id), name: s.name })),
      orgId,
    });
  } catch (e: any) {
    return NextResponse.json({ schedules: [], tests: [], sections: [], error: e?.message }, { status: 500 });
  }
}

type CreateBody = {
  orgId?: string;
  testId?: string;
  sectionIds?: string[];
  startAt?: number;
  endAt?: number;
  durationMin?: number;
};

// POST: create a scheduled test.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const orgId = await resolveOrgId(req, b.orgId);
  const testId = (b.testId || "").trim();
  const startAt = Number(b.startAt) || 0;
  const endAt = Number(b.endAt) || 0;
  if (!testId) return NextResponse.json({ error: "Pick a test to schedule" }, { status: 400 });
  if (!startAt) return NextResponse.json({ error: "A start date/time is required" }, { status: 400 });
  if (endAt && endAt <= startAt)
    return NextResponse.json({ error: "The end time must be after the start time" }, { status: 400 });

  try {
    const db = await getDb();

    // Resolve the test name (denormalized for fast listing).
    let testName = "Test";
    if (ObjectId.isValid(testId)) {
      const t: any = await db.collection("tests").findOne({ _id: new ObjectId(testId) });
      if (t) testName = t.name || t.title || "Test";
    }

    // Keep only sections that belong to this org; empty = whole institute.
    const wantIds = (Array.isArray(b.sectionIds) ? b.sectionIds : []).map(String);
    let sectionIds: string[] = [];
    let sectionNames: string[] = [];
    if (wantIds.length) {
      const secs = await db
        .collection(SECTIONS_COLL)
        .find({ orgId, recordState: "ACTIVE" } as any)
        .toArray();
      const byId = new Map((secs as any[]).map((s) => [String(s._id), s.name]));
      for (const id of wantIds) {
        if (byId.has(id)) {
          sectionIds.push(id);
          sectionNames.push(byId.get(id));
        }
      }
    }

    const now = Date.now();
    const _id = new ObjectId();
    await db.collection(COLL).insertOne({
      _id,
      orgId,
      testId,
      testName,
      sectionIds,
      sectionNames,
      startAt,
      endAt: endAt || null,
      durationMin: Math.max(0, Math.round(Number(b.durationMin) || 0)) || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), testName });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to schedule" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const res = await db
      .collection(COLL)
      .updateOne(
        { _id: new ObjectId(id), orgId } as any,
        { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } }
      );
    if (!res.matchedCount)
      return NextResponse.json({ error: "Schedule not found in your institute" }, { status: 404 });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
