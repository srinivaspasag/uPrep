import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Resolve the student's orgmembers record by userId (falls back to _id).
async function findMember(db: any, userId: string, orgId: string) {
  const or: any[] = [{ userId }];
  if (ObjectId.isValid(userId)) or.push({ _id: new ObjectId(userId) });
  return db.collection("orgmembers").findOne({ orgId, $or: or });
}

export async function GET(req: NextRequest) {
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  if (!userId) return NextResponse.json({ error: "Missing userId" }, { status: 400 });

  try {
    const db = await getDb();
    const m: any = await findMember(db, userId, orgId);
    if (!m) return NextResponse.json({ profile: null });
    return NextResponse.json({
      profile: {
        id: String(m._id),
        memberId: m.memberId || "",
        firstName: m.firstName || "",
        lastName: m.lastName || "",
        email: m.email || "",
        contactNumber: m.contactNumber || "",
        profile: m.profile || "STUDENT",
        thumbnail: m.thumbnail || m.profilePicUrl || null,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ profile: null, error: e?.message }, { status: 500 });
  }
}

type UpdateBody = {
  userId?: string;
  orgId?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  contactNumber?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as UpdateBody;
  const userId = b.userId || "";
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!userId) return NextResponse.json({ error: "Missing userId" }, { status: 400 });
  if (!(b.firstName || "").trim())
    return NextResponse.json({ error: "First name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const m: any = await findMember(db, userId, orgId);
    if (!m) return NextResponse.json({ error: "Profile not found" }, { status: 404 });

    const set: Record<string, unknown> = {
      firstName: (b.firstName || "").trim(),
      lastName: (b.lastName || "").trim(),
      email: (b.email || "").trim(),
      contactNumber: (b.contactNumber || "").trim(),
      lastUpdated: Date.now(),
    };
    await db.collection("orgmembers").updateOne({ _id: m._id }, { $set: set });

    return NextResponse.json({ ok: true, firstName: set.firstName, lastName: set.lastName });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
