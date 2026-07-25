import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Student-facing live classes: upcoming (and recently-started) scheduled classes
// for the signed-in student's org, with the teacher's VC join link.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.orgId) return NextResponse.json({ items: [] });
  try {
    const db = await getDb();
    const cutoff = Date.now() - 2 * 60 * 60 * 1000; // keep classes visible up to 2h after start
    const docs = await db
      .collection("schedules")
      .find({ orgId: session.orgId, recordState: "ACTIVE" } as any)
      .sort({ startAt: 1 })
      .limit(200)
      .toArray();

    const now = Date.now();
    const items = (docs as any[])
      .filter((s) => !s.startAt || s.startAt >= cutoff)
      .map((s) => {
        const start = s.startAt || null;
        const endsAt = start ? start + (s.durationMin || 60) * 60000 : null;
        const live = start != null && now >= start && (endsAt == null || now <= endsAt);
        return {
          id: String(s._id),
          title: s.title,
          startAt: start,
          durationMin: s.durationMin || 60,
          teacher: s.teacher || "",
          joinUrl: s.joinUrl || "",
          live,
        };
      });
    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}
