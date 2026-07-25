import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const PROGRESS_COLL = "testprogress";

// Admin control over student test attempts (legacy "Reset / End" + in-progress
// monitor). In-progress state lives in `testprogress` (userId, testId); finished
// attempts live in `userentityattempts` + `userquestionattempts`.
//
//   GET  ?testId=  -> students currently in progress on this test (monitor)
//   POST { action: "reset" | "end", testId, userId }
//     reset -> delete finished attempts + in-progress state so they can retake
//     end   -> clear the in-progress state (stops/locks the current attempt)

async function requireAdmin(req: NextRequest) {
  const session = await sessionFromReq(req);
  const isAdmin = (session?.profile || "").trim().toUpperCase() === "MANAGER";
  return { session, isAdmin };
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const testId = req.nextUrl.searchParams.get("testId") || "";
  if (!testId) return NextResponse.json({ inProgress: [] });
  try {
    const db = await getDb();
    const docs = await db
      .collection(PROGRESS_COLL)
      .find({ testId } as any)
      .sort({ updatedAt: -1 })
      .toArray();

    // Only surface students who belong to this org.
    const userIds = Array.from(new Set((docs as any[]).map((d) => String(d.userId))));
    const oids = userIds
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const members = await db
      .collection("orgmembers")
      .find({ _id: { $in: oids }, orgId } as any)
      .toArray();
    const memberById = new Map(
      (members as any[]).map((m) => [
        String(m._id),
        { name: `${m.firstName || ""} ${m.lastName || ""}`.trim() || m.memberId || "Student", memberId: m.memberId || "" },
      ])
    );

    const inProgress = (docs as any[])
      .filter((d) => memberById.has(String(d.userId)))
      .map((d) => {
        const info = memberById.get(String(d.userId))!;
        return {
          userId: String(d.userId),
          name: info.name,
          memberId: info.memberId,
          answered: d.answers ? Object.keys(d.answers).length : 0,
          remaining: typeof d.remaining === "number" ? d.remaining : null,
          updatedAt: Number(d.updatedAt) || 0,
        };
      });

    return NextResponse.json({ inProgress, orgId });
  } catch (e: any) {
    return NextResponse.json({ inProgress: [], error: e?.message }, { status: 500 });
  }
}

type Body = { action?: string; testId?: string; userId?: string; orgId?: string };

export async function POST(req: NextRequest) {
  const { isAdmin } = await requireAdmin(req);
  if (!isAdmin)
    return NextResponse.json({ error: "Only institute admins can manage attempts." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as Body;
  const action = (b.action || "").toLowerCase();
  const testId = (b.testId || "").trim();
  const targetUserId = (b.userId || "").trim();
  const orgId = await resolveOrgId(req, b.orgId);
  if (!testId || !targetUserId)
    return NextResponse.json({ error: "testId and userId are required" }, { status: 400 });

  try {
    const db = await getDb();

    // Verify the target student belongs to the admin's org (multi-tenant guard).
    if (ObjectId.isValid(targetUserId)) {
      const member = await db
        .collection("orgmembers")
        .findOne({ _id: new ObjectId(targetUserId), orgId } as any);
      if (!member)
        return NextResponse.json({ error: "Student not found in your institute" }, { status: 404 });
    }

    if (action === "end") {
      // Clear the in-progress state — stops the current (paused/resumable) attempt.
      await db.collection(PROGRESS_COLL).deleteOne({ userId: targetUserId, testId });
      return NextResponse.json({ ok: true, action: "end" });
    }

    if (action === "reset") {
      // Remove finished attempts (+ their per-question rows) and any in-progress
      // state so the student can retake the test from scratch.
      const attempts = await db
        .collection("userentityattempts")
        .find({ orgId, userId: targetUserId, "entity.type": "TEST", "entity.id": testId } as any)
        .toArray();
      const attemptIds = (attempts as any[]).map((a) => String(a._id));
      if (attemptIds.length) {
        await db.collection("userquestionattempts").deleteMany({ attemptId: { $in: attemptIds } } as any);
        await db
          .collection("userentityattempts")
          .deleteMany({ _id: { $in: (attempts as any[]).map((a) => a._id) } } as any);
      }
      await db.collection(PROGRESS_COLL).deleteOne({ userId: targetUserId, testId });
      return NextResponse.json({ ok: true, action: "reset", cleared: attemptIds.length });
    }

    return NextResponse.json({ error: "Unsupported action" }, { status: 400 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Action failed" }, { status: 500 });
  }
}
