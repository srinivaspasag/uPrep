import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isSuperAdmin } from "@/lib/roles";
import { PACK_GRANTS_COLL, listPacks, getGrantedPackIds } from "@/lib/packs";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Grant PACKS owned by the provider org (the super admin's current org) to a
// subscriber org — mirrors org-grants but for named packs.
//   GET  ?subscriberOrgId= -> { packs:[{id,name,courseCount}], grantedPackIds:[] }
//   POST { subscriberOrgId, packIds } -> replace the pack-grant set for that org
async function requireSuperAdmin(req: NextRequest) {
  const s = await sessionFromReq(req);
  if (!s || !isSuperAdmin(s.profile, s.isSuperAdmin)) return null;
  return s;
}

export async function GET(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });
  const subscriberOrgId = req.nextUrl.searchParams.get("subscriberOrgId") || "";
  if (!subscriberOrgId)
    return NextResponse.json({ error: "subscriberOrgId is required" }, { status: 400 });
  try {
    const db = await getDb();
    const packs = (await listPacks(db, s.orgId)).map((p) => ({
      id: p.id,
      name: p.name,
      courseCount: p.courseIds.length,
    }));
    const grantedPackIds = await getGrantedPackIds(db, subscriberOrgId);
    return NextResponse.json({ providerOrgId: s.orgId, packs, grantedPackIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

type SaveBody = { subscriberOrgId?: string; packIds?: string[] };

export async function POST(req: NextRequest) {
  const s = await requireSuperAdmin(req);
  if (!s) return NextResponse.json({ error: "Super admin access required" }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as SaveBody;
  const subscriberOrgId = String(b.subscriberOrgId || "");
  if (!subscriberOrgId)
    return NextResponse.json({ error: "subscriberOrgId is required" }, { status: 400 });
  if (subscriberOrgId === s.orgId)
    return NextResponse.json({ error: "An org already owns its own packs" }, { status: 400 });

  try {
    const db = await getDb();
    // Only allow granting packs the provider org actually owns.
    const ownIds = new Set((await listPacks(db, s.orgId)).map((p) => p.id));
    const packIds = Array.from(
      new Set((Array.isArray(b.packIds) ? b.packIds : []).map(String))
    ).filter((id) => ownIds.has(id));

    await db.collection(PACK_GRANTS_COLL).deleteMany({ providerOrgId: s.orgId, subscriberOrgId } as any);
    if (packIds.length) {
      const now = Date.now();
      await db.collection(PACK_GRANTS_COLL).insertMany(
        packIds.map((packId) => ({
          providerOrgId: s.orgId,
          subscriberOrgId,
          packId,
          recordState: "ACTIVE",
          timeCreated: now,
          lastUpdated: now,
        }))
      );
    }
    return NextResponse.json({ ok: true, grantedPackIds: packIds });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}
