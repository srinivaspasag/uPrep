import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Student notifications inbox — reads org-wide broadcasts from `orgnotifications`
// (the same collection CMDS "Send Notification" writes to). Closes the loop
// between the admin composer and the student.
//
// orgId comes from the session, never a client-supplied param — this used to
// read `?orgId=` straight off the query string with no auth check at all,
// so any institute's broadcast notifications were readable by anyone who
// knew (or guessed) another institute's orgId.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ items: [] }, { status: 401 });
  const orgId = session.orgId;
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
