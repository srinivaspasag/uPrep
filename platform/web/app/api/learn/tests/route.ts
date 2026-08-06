import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import { resolveStudentEnrollment } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const COLL = "testschedules";

// Student-facing scheduled tests: the tests an admin scheduled for this student's
// institute (targeted to all sections, or to a section the student belongs to),
// with per-item status and whether it can be started right now. Ended tests are
// kept visible for a short grace period so students see what they missed.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.orgId) return NextResponse.json({ items: [] });

  try {
    const db = await getDb();
    const staff = isStaff(session.profile);

    // Which sections does this student belong to? (staff preview everything)
    // Bug found live: this used to read `orgmembers.mappings`, a field no
    // real student doc actually has (they carry `programMemberships`
    // instead) — so mySections was always empty, meaning a test scheduled
    // to a specific section was silently invisible to everyone in it; only
    // fully-global (sectionIds: []) tests ever showed. Same enrollment
    // source as every other student-facing surface now.
    let mySections = new Set<string>();
    if (!staff) {
      const { studentSectionIds } = await resolveStudentEnrollment(db, session.id, []);
      mySections = new Set(studentSectionIds);
    }

    const now = Date.now();
    const grace = 6 * 60 * 60 * 1000; // keep ended tests visible for 6h
    const docs = await db
      .collection(COLL)
      .find({ orgId: session.orgId, recordState: "ACTIVE" } as any)
      .sort({ startAt: 1 })
      .limit(300)
      .toArray();

    const items = (docs as any[])
      .filter((s) => {
        const secs = Array.isArray(s.sectionIds) ? s.sectionIds : [];
        // Targeted to all (empty) OR to one of the student's sections.
        return staff || secs.length === 0 || secs.some((id: string) => mySections.has(String(id)));
      })
      .map((s) => {
        const startAt = Number(s.startAt) || 0;
        const endAt = Number(s.endAt) || 0;
        let status: "UPCOMING" | "LIVE" | "ENDED" = "UPCOMING";
        if (startAt && now >= startAt) status = endAt && now > endAt ? "ENDED" : "LIVE";
        return {
          id: String(s._id),
          testId: s.testId,
          testName: s.testName || "Test",
          startAt: startAt || null,
          endAt: endAt || null,
          durationMin: s.durationMin || null,
          status,
          startsInMs: startAt ? startAt - now : null,
          endsInMs: endAt ? endAt - now : null,
          canStart: status === "LIVE",
        };
      })
      .filter((s) => s.status !== "ENDED" || (s.endAt && now - s.endAt < grace));

    return NextResponse.json({ items }, { headers: { "Cache-Control": "no-store, private", Vary: "Cookie" } });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}
