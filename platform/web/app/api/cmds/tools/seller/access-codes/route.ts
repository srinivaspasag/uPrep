import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import crypto from "crypto";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { sendEmail } from "@/lib/messaging";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Seller Dashboard access codes — legacy's AccessCode, simplified to one
// group per code (see plan). Backs BOTH the Inventory and Shipments tabs
// (same list, different filter) — matching legacy's real behavior; there
// is no separate physical stock/tracking-number system in legacy either.
const CODES_COLL = "accesscodes";
const GROUPS_COLL = "sellergroups";

async function requireManager(req: NextRequest) {
  const s = await sessionFromReq(req);
  return (s?.profile || "").trim().toUpperCase() === "MANAGER";
}

function genCode(): string {
  return crypto.randomBytes(5).toString("hex").toUpperCase();
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const shipmentStatus = req.nextUrl.searchParams.get("shipmentStatus") || "";
  try {
    const db = await getDb();
    const docs = await db
      .collection(CODES_COLL)
      .find({ orgId, ...(shipmentStatus ? { shipmentStatus } : {}) } as any)
      .sort({ timeCreated: -1 })
      .toArray();
    return NextResponse.json({
      codes: (docs as any[]).map((c) => ({
        id: String(c._id),
        code: c.code,
        groupId: c.groupId,
        groupName: c.groupName || "",
        buyerEmail: c.buyerEmail,
        sellerInfo: c.sellerInfo || {},
        userId: c.userId || null,
        deviceIds: Array.isArray(c.deviceIds) ? c.deviceIds : [],
        shipmentStatus: c.shipmentStatus,
        invoiceId: c.invoiceId || null,
        verified: !!c.verified,
        verifiedAt: c.verifiedAt || null,
        timeCreated: c.timeCreated,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ codes: [], error: e?.message }, { status: 500 });
  }
}

type GenerateBody = {
  groupId?: string;
  buyerEmail?: string;
  sellerReferenceNo?: string;
  pointOfSale?: string;
  count?: number;
  notify?: boolean;
};

// Generate one (or, with count>1, bulk-generate N independent) access
// code(s) for a distribution group — matches legacy's
// generateAccessCode/generateBulkAccessCodes. Not bound to a student yet;
// that happens at first verification (see ../access-codes/verify).
export async function POST(req: NextRequest) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can generate access codes." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as GenerateBody;
  const groupId = b.groupId || "";
  const buyerEmail = (b.buyerEmail || "").trim().toLowerCase();
  const count = Math.min(Math.max(Number(b.count) || 1, 1), 100);
  if (!ObjectId.isValid(groupId)) return NextResponse.json({ error: "Valid groupId is required" }, { status: 400 });
  if (!buyerEmail) return NextResponse.json({ error: "buyerEmail is required" }, { status: 400 });

  const orgId = await resolveOrgId(req, null);

  try {
    const db = await getDb();
    const group: any = await db.collection(GROUPS_COLL).findOne({ _id: new ObjectId(groupId), orgId } as any);
    if (!group) return NextResponse.json({ error: "Group not found" }, { status: 404 });

    const now = Date.now();
    const docs = Array.from({ length: count }, () => ({
      _id: new ObjectId(),
      orgId,
      code: genCode(),
      groupId,
      groupName: group.name,
      buyerEmail,
      sellerInfo: {
        sellerReferenceNo: (b.sellerReferenceNo || "").trim(),
        pointOfSale: (b.pointOfSale || "").trim(),
      },
      userId: null,
      deviceIds: [] as string[],
      shipmentStatus: "NOT_DISPATCHED",
      invoiceId: null,
      verified: false,
      verifiedAt: null,
      timeCreated: now,
      lastUpdated: now,
    }));
    await db.collection(CODES_COLL).insertMany(docs);

    let delivered = 0;
    if (b.notify) {
      for (const d of docs) {
        const res = await sendEmail(
          buyerEmail,
          "Your offline content access code",
          `Access code: ${d.code}\nContent: ${group.name}\n\nEnter this code along with this email address on your device to unlock the content.`,
          { accessCodeId: String(d._id) }
        );
        if (res.delivered) delivered++;
      }
    }

    return NextResponse.json({
      ok: true,
      codes: docs.map((d) => d.code),
      notified: b.notify ? docs.length : 0,
      delivered,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Generate failed" }, { status: 500 });
  }
}

type PatchBody = { id?: string; action?: "updateShipmentStatus" | "resendEmail"; status?: string };

export async function PATCH(req: NextRequest) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can update access codes." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as PatchBody;
  if (!b.id || !ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const db = await getDb();
    const code: any = await db.collection(CODES_COLL).findOne({ _id: new ObjectId(b.id) });
    if (!code) return NextResponse.json({ error: "Access code not found" }, { status: 404 });

    if (b.action === "updateShipmentStatus") {
      const status = ["NOT_DISPATCHED", "DISPATCHED", "RECEIVED"].includes(b.status || "")
        ? b.status
        : null;
      if (!status) return NextResponse.json({ error: "Invalid status" }, { status: 400 });
      await db
        .collection(CODES_COLL)
        .updateOne({ _id: code._id }, { $set: { shipmentStatus: status, lastUpdated: Date.now() } });
      return NextResponse.json({ ok: true });
    }

    if (b.action === "resendEmail") {
      const group: any = await db.collection(GROUPS_COLL).findOne({ _id: new ObjectId(code.groupId) } as any);
      const res = await sendEmail(
        code.buyerEmail,
        "Your offline content access code",
        `Access code: ${code.code}\nContent: ${group?.name || code.groupName || ""}\n\nEnter this code along with this email address on your device to unlock the content.`,
        { accessCodeId: String(code._id) }
      );
      return NextResponse.json({ ok: true, delivered: res.delivered });
    }

    return NextResponse.json({ error: "Unsupported action" }, { status: 400 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
