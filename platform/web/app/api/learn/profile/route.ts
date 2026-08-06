import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Resolve the student's orgmembers record by userId (falls back to _id).
async function findMember(db: any, userId: string, orgId: string) {
  const or: any[] = [{ userId }];
  if (ObjectId.isValid(userId)) or.push({ _id: new ObjectId(userId) });
  return db.collection("orgmembers").findOne({ orgId, $or: or });
}

// Security fix: this used to trust a client-supplied ?userId= directly, so
// anyone could read (and, on POST, overwrite) any other user's name/email/
// phone by guessing their id. Identity now comes only from the signed
// session cookie — a caller can only ever see/edit their own profile.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userId = session.id;
  const orgId = session.orgId;

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
  firstName?: string;
  lastName?: string;
  email?: string;
  contactNumber?: string;
};

export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const userId = session.id;
  const orgId = session.orgId;

  const b = (await req.json().catch(() => ({}))) as UpdateBody;
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
