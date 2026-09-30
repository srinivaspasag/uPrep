import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { hashPassword, generatePassword } from "@/lib/password";
import { slugify } from "@/lib/slug";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Multi-org administration — SUPER ADMIN only. The legacy stack restricted org
// creation to super admins (Widgets._amISuperAdmin); we enforce the same here.
// Middleware already guarantees a staff session on /api/cmds/**; we further
// require isSuperAdmin for this route.
async function requireSuperAdmin(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s || !isSuperAdmin(s.profile, s.isSuperAdmin)) return null;
  return s;
}

// GET -> { orgs: [{id,name,type,memberCount}] }
export async function GET(req: NextRequest) {
  if (!(await requireSuperAdmin(req)))
    return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  try {
    const db = await getDb();
    const docs = await db.collection("organizations").find({}).sort({ name: 1 }).toArray();
    const orgs = await Promise.all(
      (docs as any[]).map(async (o) => {
        const id = String(o._id);
        const memberCount = await db
          .collection("orgmembers")
          .countDocuments({ orgId: id, recordState: "ACTIVE" });
        return {
          id,
          name: o.name || "(unnamed)",
          fullName: o.fullName || "",
          type: o.type || "COLLEGE",
          memberCount,
          plan: o.plan || null,
          slug: o.slug || "",
        };
      })
    );
    return NextResponse.json({ orgs });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type CreateBody = {
  name?: string;
  fullName?: string;
  type?: string;
  website?: string;
  contactNumber?: string;
  address?: string;
  // Optional org admin created alongside the org.
  adminFirstName?: string;
  adminLastName?: string;
  adminMemberId?: string;
  adminPassword?: string;
};

// POST -> create an organization, and optionally its first admin (MANAGER).
export async function POST(req: NextRequest) {
  if (!(await requireSuperAdmin(req)))
    return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Organization name is required" }, { status: 400 });
  // The legacy org-service's updateOrganization hard-requires website,
  // contactNumber, AND address to be non-empty on every future save
  // ("missing <field>") — confirmed live against lmsbe by testing each one.
  // An org created without all three can log in fine but can never save
  // Organization Info afterward. Enforce them here too, not just in the UI,
  // so this can't recur via a direct API call either.
  const website = (b.website || "").trim();
  const contactNumber = (b.contactNumber || "").trim();
  const address = (b.address || "").trim();
  const missing = [
    !website && "Website",
    !contactNumber && "Contact Number",
    !address && "Address",
  ].filter(Boolean);
  if (missing.length)
    return NextResponse.json({ error: `Required: ${missing.join(", ")}` }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const orgObjId = new ObjectId();
    const orgId = orgObjId.toHexString();

    // `organizations` has a legacy UNIQUE index on slug/referer too — never
    // write empty strings for those, an empty value would collide with the
    // next org that also lacks one. website/contactNumber/address are all
    // required above so they're always safe to set here.
    const orgDoc: Record<string, any> = {
      _id: orgObjId,
      name,
      fullName: (b.fullName || "").trim() || name,
      website,
      type: b.type || "COLLEGE",
      contactNumber,
      address,
      authType: "VEDANTU",
      // Legacy's DoubtsForumMode is a Java enum (PUBLIC/PRIVATE/HIDDEN) —
      // Morphia throws IllegalArgumentException deserializing any other
      // casing, breaking every subsequent getOrganization call for this org.
      doubtsForumMode: "PUBLIC",
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    };

    // Auto-derive a URL slug from the name (/i/<slug> is that institute's own
    // branded login entry point — see app/i/[slug]/page.tsx). Collides with
    // an existing slug -> append -2, -3, ... until unique; never write an
    // empty slug (see the unique-index note above).
    const base = slugify(name);
    if (base) {
      let candidate = base;
      let n = 2;
      while (await db.collection("organizations").findOne({ slug: candidate })) {
        candidate = `${base}-${n}`;
        n++;
      }
      orgDoc.slug = candidate;
    }

    await db.collection("organizations").insertOne(orgDoc);

    // Optionally provision the org's first admin so it's usable immediately.
    let admin: { loginId: string; password: string } | null = null;
    const adminFirst = (b.adminFirstName || "").trim();
    if (adminFirst) {
      const memberId = (b.adminMemberId || "").trim() || "admin";
      const password = (b.adminPassword || "").trim() || generatePassword();
      const memObjId = new ObjectId();
      await db.collection("orgmembers").insertOne({
        _id: memObjId,
        userId: `LOCAL_${memObjId.toHexString()}`,
        orgId,
        memberId,
        firstName: adminFirst,
        lastName: (b.adminLastName || "").trim(),
        email: "",
        profile: "MANAGER",
        contactNumber: "",
        authType: "LOCAL",
        passwordHash: hashPassword(password),
        recordState: "ACTIVE",
        timeCreated: now,
        lastUpdated: now,
      });
      admin = { loginId: `${orgId}:${memberId}`, password };
    }

    return NextResponse.json({ id: orgId, name, admin });
  } catch (e: any) {
    if (e?.code === 11000) {
      const field = Object.keys(e?.keyPattern || {})[0] || "a unique field";
      return NextResponse.json(
        { error: `An organization with that ${field} already exists.` },
        { status: 409 }
      );
    }
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

type PlanBody = {
  orgId?: string;
  plan?: { name?: string; maxStudents?: number | null; maxCourses?: number | null };
};

// Set an org's licensing plan (seat/course limits). Super admin only.
export async function PATCH(req: NextRequest) {
  if (!(await requireSuperAdmin(req)))
    return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as PlanBody;
  const orgId = String(b.orgId || "");
  if (!ObjectId.isValid(orgId)) return NextResponse.json({ error: "Invalid org id" }, { status: 400 });

  const plan = {
    name: (b.plan?.name || "").trim() || "Custom",
    maxStudents:
      b.plan?.maxStudents == null || Number(b.plan.maxStudents) <= 0
        ? null
        : Math.round(Number(b.plan.maxStudents)),
    maxCourses:
      b.plan?.maxCourses == null || Number(b.plan.maxCourses) <= 0
        ? null
        : Math.round(Number(b.plan.maxCourses)),
  };

  try {
    const db = await getDb();
    const res = await db
      .collection("organizations")
      .updateOne({ _id: new ObjectId(orgId) }, { $set: { plan, lastUpdated: Date.now() } });
    if (!res.matchedCount) return NextResponse.json({ error: "Org not found" }, { status: 404 });
    return NextResponse.json({ ok: true, plan });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
