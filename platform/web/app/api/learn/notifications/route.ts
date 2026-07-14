import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Student notifications inbox — reads org-wide broadcasts from `orgnotifications`
// (the same collection CMDS "Send Notification" writes to). Closes the loop
// between the admin composer and the student.
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
    const items = (docs as any[]).map((n) => ({
      id: String(n._id),
      title: n.title || "Notification",
      message: n.message || "",
      imageUrl: n.imageUrl || null,
      resourceType: n.resourceType || null,
      sentAt: n.timeCreated || 0,
    }));
    return NextResponse.json({ items, orgId });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}
