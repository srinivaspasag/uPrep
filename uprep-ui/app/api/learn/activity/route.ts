import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

type FeedItem = {
  type: "test" | "doubt" | "answer";
  title: string;
  detail?: string;
  at: number;
};

// Recent Activity feed for a student: finished test attempts + doubts they
// asked + answers they posted, merged and sorted by time. Also returns a
// weekly leaderboard (most finished tests) for the org.
export async function GET(req: NextRequest) {
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;

  try {
    const db = await getDb();
    const feed: FeedItem[] = [];

    if (userId) {
      const attempts: any[] = await db
        .collection("userentityattempts")
        .find({ userId, orgId, "entity.type": "TEST", finished: true })
        .sort({ endTime: -1 })
        .limit(20)
        .toArray();

      const testIds = Array.from(new Set(attempts.map((a) => a.entity?.id).filter(Boolean)));
      const testOids = testIds
        .map((id) => {
          try {
            return new ObjectId(id);
          } catch {
            return null;
          }
        })
        .filter(Boolean) as any[];
      const tests: any[] = testOids.length
        ? await db.collection("tests").find({ _id: { $in: testOids } }).toArray()
        : [];
      const testName = new Map(tests.map((t) => [String(t._id), t.name || "Test"]));

      for (const a of attempts) {
        feed.push({
          type: "test",
          title: `Attempted ${testName.get(String(a.entity?.id)) || "a test"}`,
          at: Number(a.endTime) || Number(a.timeCreated) || 0,
        });
      }

      const myDoubts: any[] = await db
        .collection("discussions")
        .find({ userId, contentType: "DISCUSSION", recordState: "ACTIVE" })
        .sort({ timeCreated: -1 })
        .limit(20)
        .toArray();
      for (const d of myDoubts) {
        feed.push({ type: "doubt", title: `Asked a doubt`, detail: d.name, at: Number(d.timeCreated) || 0 });
      }

      const myAnswers: any[] = await db
        .collection("comments")
        .find({ userId, entityType: "DISCUSSION", recordState: "ACTIVE" })
        .sort({ timeCreated: -1 })
        .limit(20)
        .toArray();
      for (const c of myAnswers) {
        feed.push({ type: "answer", title: `Answered a doubt`, detail: c.content, at: Number(c.timeCreated) || 0 });
      }
    }

    feed.sort((a, b) => b.at - a.at);

    // Weekly leaderboard: most finished tests in the last 7 days across the org.
    const weekAgo = Date.now() - 7 * 24 * 3600 * 1000;
    const weekAttempts: any[] = await db
      .collection("userentityattempts")
      .find({ orgId, "entity.type": "TEST", finished: true, endTime: { $gte: weekAgo } })
      .limit(2000)
      .toArray();

    const counts = new Map<string, number>();
    for (const a of weekAttempts) {
      if (!a.userId) continue;
      counts.set(a.userId, (counts.get(a.userId) || 0) + 1);
    }
    const topIds = Array.from(counts.entries()).sort((a, b) => b[1] - a[1]).slice(0, 5);

    // attempts.userId maps to orgmembers.userId (fall back to matching _id).
    const idStrings = topIds.map(([id]) => id);
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

    const leaderboard = topIds.map(([id, points], i) => ({
      rank: i + 1,
      name: nameById.get(id) || "Student",
      points,
    }));

    return NextResponse.json({ feed: feed.slice(0, 30), leaderboard, orgId });
  } catch (e: any) {
    return NextResponse.json({ feed: [], leaderboard: [], error: e?.message }, { status: 500 });
  }
}
