import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Send Notification — the legacy screen fires Google FCM (prod only). Here we
// record the notification to Mongo (orgnotifications) and return success, so the
// full authoring flow works in the demo without external push infra. Wire a real
// FCM/APNs/web-push provider here when going live.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("orgnotifications")
      .find({ orgId })
      .sort({ timeCreated: -1 })
      .limit(50)
      .toArray();
    const history = (docs as any[]).map((n) => ({
      id: String(n._id),
      title: n.title,
      message: n.message,
      resourceType: n.resourceType || null,
      sectionId: n.sectionId || null,
      sentAt: n.timeCreated,
    }));
    return NextResponse.json({ history, orgId });
  } catch (e: any) {
    return NextResponse.json({ history: [], error: e?.message }, { status: 500 });
  }
}

type SendBody = {
  title?: string;
  message?: string;
  summary?: string;
  imageUrl?: string;
  sectionId?: string;
  resourceType?: string;
  resourceId?: string;
  orgId?: string;
  userId?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as SendBody;
  const title = (b.title || "").trim();
  const message = (b.message || "").trim();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!title) return NextResponse.json({ error: "Notification title is required" }, { status: 400 });
  if (!message) return NextResponse.json({ error: "Notification message is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("orgnotifications").insertOne({
      _id,
      orgId,
      title,
      message,
      summary: (b.summary || "").trim() || null,
      imageUrl: (b.imageUrl || "").trim() || null,
      sectionId: b.sectionId || null,
      resourceType: b.resourceType || null,
      resourceId: b.resourceId || null,
      sentBy: b.userId || null,
      status: "SENT",
      timeCreated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), status: "SENT" });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Send failed" }, { status: 500 });
  }
}
