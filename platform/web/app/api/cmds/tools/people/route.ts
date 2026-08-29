import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { hashPassword, generatePassword } from "@/lib/password";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { resolveAdminProgramScope } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Resolves which org a staff request operates on: an org admin is pinned to
// their session org; only a super admin may target another org via an explicit
// override (?orgId / body.orgId).
async function resolveOrgId(req: NextRequest, override?: string | null): Promise<string> {
  const session = await sessionFromReq(req);
  if (session && isSuperAdmin(session.profile, session.isSuperAdmin) && override) return override;
  return session?.orgId || override || DEFAULT_ORG_ID;
}

// Build a friendly, human-readable login id from the member's name, e.g.
// "Ravi Kumar" -> "ravi.kumar" (then ravi.kumar2, ravi.kumar3, … on collision).
// Falls back to a profile prefix when the name has no usable characters.
async function generateMemberId(
  db: any,
  orgId: string,
  firstName: string,
  lastName: string,
  profile: string
): Promise<string> {
  let base = `${firstName} ${lastName}`
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ".")
    .replace(/^\.+|\.+$/g, "");
  if (!base) base = (profile.slice(0, 3).toLowerCase() || "user");

  let candidate = base;
  let n = 1;
  // Check every record (not just ACTIVE) so we never reuse a deactivated id.
  while (await db.collection("orgmembers").findOne({ orgId, memberId: candidate })) {
    n += 1;
    candidate = `${base}${n}`;
  }
  return candidate;
}

// People Management — reads org members directly from Mongo (orgmembers), the
// same collection org-services :19012 /members/getMembers serves.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const profile = (req.nextUrl.searchParams.get("profile") || "").toUpperCase();
  const query = (req.nextUrl.searchParams.get("query") || "").trim().toLowerCase();
  const programId = req.nextUrl.searchParams.get("programId") || "";

  try {
    const db = await getDb();
    const session = await sessionFromReq(req);
    const adminScope = session
      ? await resolveAdminProgramScope(db, session.id, !!session.isSuperAdmin)
      : null;

    const filter: Record<string, unknown> = { orgId, recordState: "ACTIVE" };
    if (profile && profile !== "ALL") filter.profile = profile;
    // Scoped to one Program's roster (e.g. the Program detail page's
    // Members/Students tabs) — without this every org member showed up
    // under every program regardless of actual assignment.
    if (programId) filter["programMemberships.programId"] = programId;
    // A program-scoped admin (one with a Program assigned to their own
    // account — see People Management's mapping picker) only ever sees
    // members within their own assigned program(s), regardless of what
    // ?programId= a client request asks for. An unscoped admin (no
    // assignment, or a super admin) is unaffected.
    if (adminScope) {
      filter["programMemberships.programId"] = programId && adminScope.includes(programId)
        ? programId
        : { $in: adminScope };
    }

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
      // Bug found live: there was no UI anywhere to assign a student/teacher
      // to a Program/Center/Section — Add and Edit forms had name/email/role
      // fields only, never touched this. Returned raw here; the client
      // resolves names against the same program/center/section data the
      // Academic Structure page already fetches.
      programMemberships: Array.isArray(m.programMemberships) ? m.programMemberships : [],
      // Bug found live: a student can ALSO have courses granted directly
      // (independent of any Program mapping) via the Enroll tool / coupon /
      // checkout flow — see lib/enrollment.ts's `directIds`. That grant was
      // completely invisible in People Management: a student with zero
      // Program mappings could still see a full course list in the learn
      // app, and there was no way here to tell why. Returned raw; the
      // client resolves names against `folders`.
      enrolledCourseIds: Array.isArray(m.enrolledCourseIds) ? m.enrolledCourseIds : [],
    }));

    if (query)
      members = members.filter((m) =>
        `${m.firstName} ${m.lastName} ${m.memberId} ${m.email}`.toLowerCase().includes(query)
      );

    // Counts per profile for the selector badges — scoped the same way as
    // `members` above, so a program-scoped admin's badge counts and
    // "Students by Program" pills only ever reflect their own program(s),
    // not the whole org.
    const counts: Record<string, number> = {};
    const countsFilter: Record<string, unknown> = { orgId, recordState: "ACTIVE" };
    if (adminScope) countsFilter["programMemberships.programId"] = { $in: adminScope };
    const all = await db.collection("orgmembers").find(countsFilter).toArray();
    for (const m of all as any[]) counts[m.profile || "UNKNOWN"] = (counts[m.profile || "UNKNOWN"] || 0) + 1;

    // Students per program (independent of the search box / result cap above,
    // so it reflects the whole org, not just the current page of results). A
    // student counts once per program even if mapped to multiple sections
    // within it.
    const programCounts: Record<string, number> = {};
    let unassignedStudents = 0;
    for (const m of all as any[]) {
      if ((m.profile || "").toUpperCase() !== "STUDENT") continue;
      const ms: Mapping[] = Array.isArray(m.programMemberships) ? m.programMemberships : [];
      if (ms.length === 0) {
        unassignedStudents++;
        continue;
      }
      const programIds = new Set(ms.map((x) => x.programId).filter(Boolean));
      for (const pid of programIds) programCounts[pid] = (programCounts[pid] || 0) + 1;
    }

    // Resolve names for any directly-enrolled course ids on the returned
    // page of members (not the whole org — same scoping as `members` itself).
    const courseIds = Array.from(new Set(members.flatMap((m) => m.enrolledCourseIds))).filter(ObjectId.isValid);
    const courseNames: Record<string, string> = {};
    if (courseIds.length) {
      const courseDocs = await db
        .collection("folders")
        .find({ _id: { $in: courseIds.map((id) => new ObjectId(id)) } })
        .toArray();
      for (const c of courseDocs as any[]) courseNames[String(c._id)] = c.name || "(untitled course)";
    }

    return NextResponse.json({ members, counts, programCounts, unassignedStudents, courseNames, orgId });
  } catch (e: any) {
    return NextResponse.json({ members: [], counts: {}, error: e?.message }, { status: 500 });
  }
}

type Mapping = { programId: string; centerId: string; sectionId: string };

type AddBody = {
  firstName?: string;
  lastName?: string;
  memberId?: string;
  email?: string;
  profile?: string;
  contactNumber?: string;
  orgId?: string;
  password?: string;
  programMemberships?: Mapping[];
};

// Add a member (STUDENT/TEACHER/etc). Writes to orgmembers AND makes the account
// login-capable immediately (no email verification): a password is hashed and
// stored locally, so /api/auth/login can authenticate it directly against Mongo.
export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as AddBody;
  const firstName = (b.firstName || "").trim();
  const profile = (b.profile || "STUDENT").toUpperCase();
  const orgId = await resolveOrgId(req, b.orgId);
  if (!firstName) return NextResponse.json({ error: "First name is required" }, { status: 400 });

  try {
    const db = await getDb();

    // Enforce the org's licensing seat limit for students.
    if (profile === "STUDENT") {
      const org: any = await db
        .collection("organizations")
        .findOne({ _id: ObjectId.isValid(orgId) ? new ObjectId(orgId) : (orgId as any) });
      const maxStudents = org?.plan?.maxStudents;
      if (maxStudents != null) {
        const current = await db
          .collection("orgmembers")
          .countDocuments({ orgId, profile: "STUDENT", recordState: "ACTIVE" });
        if (current >= maxStudents)
          return NextResponse.json(
            { error: `Seat limit reached (${maxStudents} students). Upgrade the plan to add more.` },
            { status: 403 }
          );
      }
    }

    const now = Date.now();
    const _id = new ObjectId();

    // memberId is the login username (unique per org). If the admin typed one,
    // honour it (and reject clashes); otherwise auto-generate a friendly, name-
    // based id.
    const customId = (b.memberId || "").trim();
    let memberId: string;
    if (customId) {
      memberId = customId;
      const clash = await db.collection("orgmembers").findOne({ orgId, memberId, recordState: "ACTIVE" });
      if (clash) return NextResponse.json({ error: `Institute ID "${memberId}" is already in use.` }, { status: 409 });
    } else {
      memberId = await generateMemberId(db, orgId, firstName, (b.lastName || "").trim(), profile);
    }

    const plainPassword = (b.password || "").trim() || generatePassword();

    // Program/center/section assignment, set at creation time when the admin
    // picked one — legacy does this as an immediate follow-up call after
    // creating the account (QrPeople Step 2, "Assign Courses and Sections"),
    // not a separate menu action; setting it inline here is equivalent and
    // avoids a create-succeeded-but-assignment-failed split state.
    const programMemberships = (Array.isArray(b.programMemberships) ? b.programMemberships : [])
      .filter((m) => m && m.programId && m.centerId && m.sectionId)
      .map((m) => ({ programId: m.programId, centerId: m.centerId, sectionId: m.sectionId, assignedAt: now }));

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
      programMemberships,
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
  programMemberships?: Mapping[];
};

// Confirms the member is inside the caller's org (super admin may reach any org).
async function assertSameOrg(req: NextRequest, db: any, id: string): Promise<string | null> {
  const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(id) });
  if (!member) return "Member not found";
  const session = await sessionFromReq(req);
  const superAdmin = !!session && isSuperAdmin(session.profile, session.isSuperAdmin);
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  if (!superAdmin && member.orgId !== orgId) return "That member belongs to another institute";
  return null;
}

// Edit a member's details.
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as EditBody;
  if (!b.id || !ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  if (!(b.firstName || "").trim()) return NextResponse.json({ error: "First name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const denied = await assertSameOrg(req, db, b.id);
    if (denied) return NextResponse.json({ error: denied }, { status: denied === "Member not found" ? 404 : 403 });
    const now = Date.now();
    const set: Record<string, unknown> = {
      firstName: (b.firstName || "").trim(),
      lastName: (b.lastName || "").trim(),
      email: (b.email || "").trim(),
      contactNumber: (b.contactNumber || "").trim(),
      lastUpdated: now,
    };
    if (b.profile) set.profile = b.profile.toUpperCase();
    // Full replace, not merge — matches how the rest of this app edits list
    // fields (e.g. module contentIds, section courseIds). The picker on the
    // client already shows every current mapping plus lets you add/remove,
    // so it always submits the complete intended set.
    if (b.programMemberships !== undefined) {
      set.programMemberships = (Array.isArray(b.programMemberships) ? b.programMemberships : [])
        .filter((m) => m && m.programId && m.centerId && m.sectionId)
        .map((m) => ({ programId: m.programId, centerId: m.centerId, sectionId: m.sectionId, assignedAt: now }));
    }
    await db.collection("orgmembers").updateOne({ _id: new ObjectId(b.id) }, { $set: set });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

// Deactivate a member (soft delete). Admin-only, matching legacy's
// QrPeople.showDeactivationPopup (MANAGER-gated).
export async function DELETE(req: NextRequest) {
  const session = await sessionFromReq(req);
  if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
    return NextResponse.json({ error: "Only institute admins can deactivate members." }, { status: 403 });

  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    const denied = await assertSameOrg(req, db, id);
    if (denied) return NextResponse.json({ error: denied }, { status: denied === "Member not found" ? 404 : 403 });
    await db
      .collection("orgmembers")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Deactivate failed" }, { status: 500 });
  }
}
