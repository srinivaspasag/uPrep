import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { hashPassword, generatePassword } from "@/lib/password";

export const dynamic = "force-dynamic";

// People Management — reads org members directly from Mongo (orgmembers), the
// same collection org-services :19012 /members/getMembers serves.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const profile = (req.nextUrl.searchParams.get("profile") || "").toUpperCase();
  const query = (req.nextUrl.searchParams.get("query") || "").trim().toLowerCase();

  try {
    const db = await getDb();
    const filter: Record<string, unknown> = { orgId, recordState: "ACTIVE" };
    if (profile && profile !== "ALL") filter.profile = profile;

    const docs = await db
      .collection("orgmembers")
      .find(filter)
      .sort({ lastUpdated: -1 })
      .limit(500)
      .toArray();

    let members = (docs as any[]).map((m) => ({
      id: String(m._id),
      userId: m.userId || null,
      memberId: m.memberId || "",
      firstName: m.firstName || "",
      lastName: m.lastName || "",
      email: m.email || "",
      profile: m.profile || "",
      contactNumber: m.contactNumber || "",
      status: m.recordState === "ACTIVE" ? "Active" : "Inactive",
    }));

    if (query)
      members = members.filter((m) =>
        `${m.firstName} ${m.lastName} ${m.memberId} ${m.email}`.toLowerCase().includes(query)
      );

    // Counts per profile for the selector badges.
    const counts: Record<string, number> = {};
    const all = await db.collection("orgmembers").find({ orgId, recordState: "ACTIVE" }).toArray();
    for (const m of all as any[]) counts[m.profile || "UNKNOWN"] = (counts[m.profile || "UNKNOWN"] || 0) + 1;

    return NextResponse.json({ members, counts, orgId });
  } catch (e: any) {
    return NextResponse.json({ members: [], counts: {}, error: e?.message }, { status: 500 });
  }
}

type AddBody = {
  firstName?: string;
  lastName?: string;
  memberId?: string;
  email?: string;
  profile?: string;
  contactNumber?: string;
  orgId?: string;
  password?: string;
};

// Add a member (STUDENT/TEACHER/etc). Writes to orgmembers AND makes the account
// login-capable immediately (no email verification): a password is hashed and
// stored locally, so /api/auth/login can authenticate it directly against Mongo.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as AddBody;
  const firstName = (b.firstName || "").trim();
  const profile = (b.profile || "STUDENT").toUpperCase();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!firstName) return NextResponse.json({ error: "First name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    const memberId = (b.memberId || "").trim() || `${profile.slice(0, 3)}_${now.toString().slice(-5)}`;

    // memberId is the login username (orgId:memberId) — must be unique per org.
    const clash = await db.collection("orgmembers").findOne({ orgId, memberId, recordState: "ACTIVE" });
    if (clash) return NextResponse.json({ error: `Institute ID "${memberId}" is already in use.` }, { status: 409 });

    const plainPassword = (b.password || "").trim() || generatePassword();

    await db.collection("orgmembers").insertOne({
      _id,
      // Unique placeholder — orgmembers has a unique index on (orgId, userId),
      // so we can't leave this null (multiple would collide).
      userId: `LOCAL_${_id.toHexString()}`,
      orgId,
      memberId,
      firstName,
      lastName: (b.lastName || "").trim(),
      email: (b.email || "").trim().toLowerCase(),
      profile,
      contactNumber: (b.contactNumber || "").trim(),
      authType: "LOCAL",
      passwordHash: hashPassword(plainPassword),
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });

    // Return the plaintext password once so the admin can share the login.
    return NextResponse.json({
      id: _id.toHexString(),
      memberId,
      profile,
      loginId: `${orgId}:${memberId}`,
      password: plainPassword,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Add failed" }, { status: 500 });
  }
}

type EditBody = {
  id?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  profile?: string;
  contactNumber?: string;
};

// Edit a member's details.
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as EditBody;
  if (!b.id || !ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  if (!(b.firstName || "").trim()) return NextResponse.json({ error: "First name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const set: Record<string, unknown> = {
      firstName: (b.firstName || "").trim(),
      lastName: (b.lastName || "").trim(),
      email: (b.email || "").trim(),
      contactNumber: (b.contactNumber || "").trim(),
      lastUpdated: Date.now(),
    };
    if (b.profile) set.profile = b.profile.toUpperCase();
    await db.collection("orgmembers").updateOne({ _id: new ObjectId(b.id) }, { $set: set });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

// Deactivate a member (soft delete).
export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection("orgmembers")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Deactivate failed" }, { status: 500 });
  }
}
