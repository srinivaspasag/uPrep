import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Downloadable / printable Result Sheet (CSV) for a test — the manual's
// "printable result sheet". Recomputes the same ranked result sheet the
// analytics screen shows.
function marksMap(test: any): Record<string, { positive: number; negative: number }> {
  const m: Record<string, { positive: number; negative: number }> = {};
  for (const md of test?.metadata || []) if (md?.marks) Object.assign(m, md.marks);
  return m;
}

function csvCell(v: string | number): string {
  const s = String(v ?? "");
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const testId = req.nextUrl.searchParams.get("testId") || "";
  if (!testId) return NextResponse.json({ error: "testId required" }, { status: 400 });

  try {
    const db = await getDb();
    let testDoc: any = null;
    if (ObjectId.isValid(testId)) testDoc = await db.collection("tests").findOne({ _id: new ObjectId(testId) });
    const marks = marksMap(testDoc);
    const totalMarks =
      testDoc?.totalMarks ?? Object.values(marks).reduce((s, mk: any) => s + (mk.positive || 0), 0);
    const testName = testDoc?.name || testDoc?.title || "Test";

    const rawAttempts: any[] = await db
      .collection("userentityattempts")
      .find({ orgId, "entity.type": "TEST", "entity.id": testId, finished: true })
      .toArray();

    // Student-only — same fix as the analytics screen itself (a staff/QA
    // attempt shouldn't appear on an exported result sheet either).
    const rawUserIds = Array.from(new Set(rawAttempts.map((a) => String(a.userId))));
    const rawUserOids = rawUserIds.filter((id) => ObjectId.isValid(id)).map((id) => new ObjectId(id));
    const profileById = new Map(
      (
        await db.collection("orgmembers").find({ _id: { $in: rawUserOids } }).project({ profile: 1 }).toArray()
      ).map((m: any) => [String(m._id), (m.profile || "").toUpperCase()])
    );
    const attempts = rawAttempts.filter((a) => profileById.get(String(a.userId)) === "STUDENT");

    const attemptIds = attempts.map((a) => String(a._id));
    const qAttempts: any[] = attemptIds.length
      ? await db.collection("userquestionattempts").find({ attemptId: { $in: attemptIds } }).toArray()
      : [];
    const byAttempt = new Map<string, any[]>();
    for (const q of qAttempts) {
      const k = String(q.attemptId);
      if (!byAttempt.has(k)) byAttempt.set(k, []);
      byAttempt.get(k)!.push(q);
    }

    const scoreByUser = new Map<string, { best: number; attempts: number; lastAt: number }>();
    for (const at of attempts) {
      let score = 0;
      for (const q of byAttempt.get(String(at._id)) || []) {
        if (!q.isJudgeable) continue;
        const mk = marks[String(q.qId)] || { positive: 0, negative: 0 };
        const verdict = String(q.isCorrect || "").toUpperCase();
        if (verdict === "CORRECT") score += mk.positive || 0;
        else if (verdict === "INCORRECT") score -= mk.negative || 0;
      }
      if (score < 0) score = 0;
      const uid = String(at.userId);
      const endAt = Number(at.endTime) || Number(at.timeCreated) || 0;
      const prev = scoreByUser.get(uid);
      if (!prev) scoreByUser.set(uid, { best: score, attempts: 1, lastAt: endAt });
      else
        scoreByUser.set(uid, {
          best: Math.max(prev.best, score),
          attempts: prev.attempts + 1,
          lastAt: Math.max(prev.lastAt, endAt),
        });
    }

    const userIds = Array.from(scoreByUser.keys());
    const oids = userIds
      .map((id) => {
        try {
          return new ObjectId(id);
        } catch {
          return null;
        }
      })
      .filter(Boolean) as any[];
    const members = await db.collection("orgmembers").find({ _id: { $in: oids } }).toArray();
    const memberById = new Map(
      (members as any[]).map((m) => [
        String(m._id),
        { name: `${m.firstName || ""} ${m.lastName || ""}`.trim() || m.memberId || "Student", memberId: m.memberId || "" },
      ])
    );
    const pct = (s: number) => (totalMarks > 0 ? Math.round((s / totalMarks) * 1000) / 10 : 0);

    const rows = userIds
      .map((uid) => {
        const r = scoreByUser.get(uid)!;
        const info = memberById.get(uid) || { name: "Student", memberId: "" };
        return { name: info.name, memberId: info.memberId, score: r.best, percent: pct(r.best), attempts: r.attempts, lastAt: r.lastAt };
      })
      .sort((a, b) => b.score - a.score);

    const header = ["Rank", "Student", "ID", "Score", "Total", "Percent", "Attempts", "Last Attempt"];
    const lines = [header.map(csvCell).join(",")];
    rows.forEach((r, i) => {
      lines.push(
        [
          i + 1,
          r.name,
          r.memberId,
          r.score,
          totalMarks,
          `${r.percent}%`,
          r.attempts,
          r.lastAt ? new Date(r.lastAt).toISOString() : "",
        ]
          .map(csvCell)
          .join(",")
      );
    });

    const csv = lines.join("\n");
    const safeName = testName.replace(/[^a-z0-9]+/gi, "_").slice(0, 40);
    return new NextResponse(csv, {
      headers: {
        "Content-Type": "text/csv; charset=utf-8",
        "Content-Disposition": `attachment; filename="result-sheet-${safeName}.csv"`,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Export failed" }, { status: 500 });
  }
}
