import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Student view of assignments, annotated with the caller's submission state.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const userId = req.nextUrl.searchParams.get("userId") || "";
  try {
    const db = await getDb();
    const docs = await db
      .collection("assignments")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();
    const subs = userId
      ? await db.collection("submissions").find({ userId }).toArray()
      : [];
    const subByAssignment = new Map((subs as any[]).map((s) => [s.assignmentId, s]));
    const items = (docs as any[]).map((a) => {
      const s = subByAssignment.get(String(a._id));
      return {
        id: String(a._id),
        name: a.name,
        description: a.description || "",
        dueDate: a.dueDate || null,
        maxMarks: a.maxMarks || 0,
        submitted: !!s,
        submittedText: s?.text || "",
        marks: s?.marks ?? null,
        status: s?.status || (s ? "SUBMITTED" : "PENDING"),
      };
    });
    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type SubmitBody = {
  assignmentId?: string;
  userId?: string;
  userName?: string;
  text?: string;
  fileUrl?: string | null;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as SubmitBody;
  if (!b.assignmentId || !b.userId)
    return NextResponse.json({ error: "Missing fields" }, { status: 400 });
  if (!(b.text || "").trim() && !b.fileUrl)
    return NextResponse.json({ error: "Enter your answer or attach a file" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    await db.collection("submissions").updateOne(
      { assignmentId: b.assignmentId, userId: b.userId },
      {
        $set: {
          assignmentId: b.assignmentId,
          userId: b.userId,
          userName: b.userName || "Student",
          text: (b.text || "").trim(),
          fileUrl: b.fileUrl || null,
          status: "SUBMITTED",
          lastUpdated: now,
        },
        $setOnInsert: { _id: new ObjectId(), timeCreated: now },
      },
      { upsert: true }
    );
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to submit" }, { status: 500 });
  }
}
