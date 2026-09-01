import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { getOrCreateGroupKey, ENCRYPTION_INFO } from "@/lib/group-crypto";
import { expiresAt as computeExpiresAt } from "@/lib/expiry";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Access-code verification — called by an offline device/client (NOT staff,
// no session — deliberately outside /api/cmds/** so middleware.ts's staff
// gate doesn't block it), mirroring legacy's real cmds-services
// AccessCodes.verifyAccessCode contract: {email, code, deviceId, userId}.
// This is the actual mechanism the user asked for: a code works only on
// one device, for one person.
//
// First verification binds deviceId+userId to the code. Every later
// verification must match both, or is rejected — exact legacy semantics
// (ITEM_ALREADY_VERIFIED_WITH_DIFFERENT_DEVICE / _FOR_DIFFERENT_USER).
const CODES_COLL = "accesscodes";
const GROUPS_COLL = "sellergroups";

type VerifyBody = { code?: string; email?: string; deviceId?: string; userId?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as VerifyBody;
  const code = (b.code || "").trim().toUpperCase();
  const email = (b.email || "").trim().toLowerCase();
  const deviceId = (b.deviceId || "").trim();
  const userId = (b.userId || "").trim();
  if (!code || !email || !deviceId || !userId)
    return NextResponse.json({ error: "code, email, deviceId and userId are all required" }, { status: 400 });

  try {
    const db = await getDb();
    const doc: any = await db.collection(CODES_COLL).findOne({ code, buyerEmail: email } as any);
    if (!doc) return NextResponse.json({ error: "INVALID_CODE" }, { status: 404 });

    // Access codes are valid for 1 year from generation — same policy as
    // student account expiry (lib/expiry.ts). This blocks both a fresh
    // activation attempt AND a re-verification of an already-bound
    // device/user past that date. The device also gets `expiresAt` back on
    // success (below) so it can enforce this fully offline afterward — the
    // whole point of this flow is zero connectivity after the first verify.
    const codeCreatedAt = typeof doc.timeCreated === "number" ? doc.timeCreated : 0;
    const codeExpiresAt = codeCreatedAt ? computeExpiresAt(codeCreatedAt) : null;
    if (codeExpiresAt && Date.now() > codeExpiresAt) {
      return NextResponse.json({ error: "ACCESS_CODE_EXPIRED" }, { status: 403 });
    }

    const boundDevices: string[] = Array.isArray(doc.deviceIds) ? doc.deviceIds : [];

    if (boundDevices.length === 0) {
      // First verification — bind this device and user.
      await db.collection(CODES_COLL).updateOne(
        { _id: doc._id },
        {
          $set: {
            deviceIds: [deviceId],
            userId,
            shipmentStatus: "RECEIVED",
            verified: true,
            verifiedAt: Date.now(),
            lastUpdated: Date.now(),
          },
        }
      );
    } else if (!boundDevices.includes(deviceId)) {
      return NextResponse.json({ error: "ITEM_ALREADY_VERIFIED_WITH_DIFFERENT_DEVICE" }, { status: 403 });
    } else if (doc.userId && doc.userId !== userId) {
      return NextResponse.json({ error: "ITEM_ALREADY_VERIFIED_FOR_DIFFERENT_USER" }, { status: 403 });
    }

    const group: any = await db.collection(GROUPS_COLL).findOne({ _id: new ObjectId(doc.groupId) } as any);

    // The decryption key for this group's packaged content — see
    // lib/group-crypto.ts. This is the ONLY place it's ever released, and
    // only after the device+user binding above has succeeded. A card that's
    // been copied or handed to someone else still needs a successful verify
    // with the right email+device to ever decrypt anything on it.
    const encryptionKey = group ? (await getOrCreateGroupKey(db, String(group._id))).toString("base64") : null;

    return NextResponse.json({
      ok: true,
      groupName: group?.name || doc.groupName || "",
      contentIds: Array.isArray(group?.contentIds) ? group.contentIds : [],
      encryptionKey,
      encryption: encryptionKey ? ENCRYPTION_INFO : null,
      expiresAt: codeExpiresAt,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Verification failed" }, { status: 500 });
  }
}
