import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { saveBuffer } from "@/lib/storage";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Exports / SD Cards — generates a real CSV from Mongo and records the job in
// an `exports` collection. In AWS this would enqueue an SD-card burn job; here
// it produces a downloadable file immediately.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("exports")
      .find({ orgId })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((e) => ({
        id: String(e._id),
        type: e.type,
        status: e.status,
        rows: e.rows || 0,
        url: e.url || null,
        at: e.timeCreated || 0,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

function csvEscape(v: unknown): string {
  const s = v == null ? "" : String(v);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}
function toCsv(headers: string[], rows: any[][]): string {
  return [headers, ...rows].map((r) => r.map(csvEscape).join(",")).join("\n");
}

type Body = { type?: string; orgId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const type = (b.type || "members").toLowerCase();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    let headers: string[] = [];
    let rows: any[][] = [];

    if (type === "members") {
      const docs = await db.collection("orgmembers").find({ orgId, recordState: "ACTIVE" }).toArray();
      headers = ["Member ID", "First name", "Last name", "Email", "Role", "Contact"];
      rows = (docs as any[]).map((m) => [m.memberId, m.firstName, m.lastName, m.email, m.profile, m.contactNumber]);
    } else if (type === "questions") {
      const docs = await db.collection("questions").find({ "contentSrc.id": orgId, recordState: "ACTIVE" }).toArray();
      headers = ["ID", "Type", "Difficulty", "Content"];
      rows = (docs as any[]).map((q) => [String(q._id), q.type, q.difficulty, (q.content || "").replace(/<[^>]*>/g, " ")]);
    } else if (type === "tests") {
      const docs = await db.collection("tests").find({ "contentSrc.id": orgId, recordState: "ACTIVE" }).toArray();
      headers = ["ID", "Name", "Questions", "Total marks", "Code"];
      rows = (docs as any[]).map((t) => [String(t._id), t.name, t.qusCount, t.totalMarks, t.code]);
    } else {
      return NextResponse.json({ error: "Unknown export type" }, { status: 400 });
    }

    const csv = toCsv(headers, rows);
    const stored = await saveBuffer(csv, `${type}-export.csv`, "text/csv");
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("exports").insertOne({
      _id,
      orgId,
      type,
      status: "COMPLETE",
      rows: rows.length,
      url: stored.url,
      recordState: "ACTIVE",
      timeCreated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), type, rows: rows.length, url: stored.url });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Export failed" }, { status: 500 });
  }
}
