import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";

// Leaderboard from finished test attempts (`userentityattempts`), joined to
// `orgmembers` for display names. range=week|all.
export async function GET(req: NextRequest) {
  const range = req.nextUrl.searchParams.get("range") === "all" ? "all" : "week";
  try {
    const db = await getDb();
    const since = range === "week" ? Date.now() - 7 * 24 * 60 * 60 * 1000 : 0;
    const attempts: any[] = await db
      .collection("userentityattempts")
      .find({ entityType: "TEST" })
      .toArray();

    const agg = new Map<string, { tests: number; score: number }>();
    for (const a of attempts) {
      const t = Number(a.lastUpdated || a.timeCreated || 0);
      if (since && t < since) continue;
      const uid = String(a.userId || "");
      if (!uid) continue;
      const cur = agg.get(uid) || { tests: 0, score: 0 };
      cur.tests += 1;
      cur.score += Number(a.score ?? a.marksObtained ?? 0);
      agg.set(uid, cur);
    }

    const ranked = Array.from(agg.entries())
      .map(([userId, v]) => ({ userId, ...v }))
      .sort((a, b) => b.score - a.score || b.tests - a.tests)
      .slice(0, 25);

    const idStrings = ranked.map((r) => r.userId);
    const idOids = idStrings
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const members: any[] = idStrings.length
      ? await db
          .collection("orgmembers")
          .find({ $or: [{ userId: { $in: idStrings } }, { _id: { $in: idOids } }] })
          .toArray()
      : [];
    const nameById = new Map<string, string>();
    for (const m of members) {
      const label = [m.firstName, m.lastName].filter(Boolean).join(" ") || m.name || "Student";
      if (m.userId) nameById.set(String(m.userId), label);
      nameById.set(String(m._id), label);
    }

    const rows = ranked.map((r, i) => ({
      rank: i + 1,
      userId: r.userId,
      name: nameById.get(r.userId) || "Student",
      tests: r.tests,
      score: Math.round(r.score),
    }));

    return NextResponse.json({ range, rows });
  } catch (e: any) {
    return NextResponse.json({ rows: [], error: e?.message }, { status: 500 });
  }
}
