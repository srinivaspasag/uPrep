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
// asked + answers they posted, merged and sorted by time.
//
// Used to also return a cross-student weekly "leaderboard" here — checked
// against legacy (MyContents.java's testPage()) and that's not real: legacy
// fetches toppers/leaderboard data for BOTH roles but only ever passes it
// into the TEACHER template (postTestTeacherPage.html); the STUDENT template
// (postTestPage.html) never receives it. Leaderboards are a staff-only test
// analytics view in the real app, not something a student sees about their
// peers — removed rather than "fixed", since showing it at all didn't match
// legacy to begin with.
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

    return NextResponse.json({ feed: feed.slice(0, 30), orgId });
  } catch (e: any) {
    return NextResponse.json({ feed: [], error: e?.message }, { status: 500 });
  }
}
