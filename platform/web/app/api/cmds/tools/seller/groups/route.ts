import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { resolveProgramContentIds, type CourseGroup } from "@/lib/seller-program";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Seller Dashboard "distribution groups" — legacy's SDCardGroup, simplified
// to pure content bookkeeping (see plan: legacy's own SDCardManager never
// copies bytes either, it only links content to a virtual card/group; the
// actual physical media prep happens outside this system).
const GROUPS_COLL = "sellergroups";

function requireManager(req: NextRequest) {
  return sessionFromReq(req).then((s) => (s?.profile || "").trim().toUpperCase() === "MANAGER");
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(GROUPS_COLL)
      .find({ orgId } as any)
      .sort({ createdAt: -1 })
      .toArray();
    return NextResponse.json({
      groups: (docs as any[]).map((g) => ({
        id: String(g._id),
        name: g.name,
        itemCount: Array.isArray(g.contentIds) ? g.contentIds.length : 0,
        createdAt: g.createdAt,
        programId: g.programId || null,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ groups: [], error: e?.message }, { status: 500 });
  }
}

type CreateBody = { name?: string; contentIds?: string[]; programId?: string };

export async function POST(req: NextRequest) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can create distribution groups." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  const orgId = await resolveOrgId(req, null);
  const session = await sessionFromReq(req);

  try {
    const db = await getDb();

    let contentIds: string[];
    let programId: string | null = null;
    let programName: string | null = null;
    let courseGroups: CourseGroup[] = [];
    if (b.programId && ObjectId.isValid(b.programId)) {
      programId = b.programId;
      const resolved = await resolveProgramContentIds(db, orgId, programId);
      contentIds = resolved.contentIds;
      programName = resolved.programName;
      courseGroups = resolved.courseGroups;
      if (contentIds.length === 0)
        return NextResponse.json({ error: "This program has no content in any of its sections yet." }, { status: 400 });
    } else {
      contentIds = Array.from(new Set((Array.isArray(b.contentIds) ? b.contentIds : []).map(String)));
      if (contentIds.length === 0) return NextResponse.json({ error: "Pick at least one content item" }, { status: 400 });
    }

    const _id = new ObjectId();
    await db.collection(GROUPS_COLL).insertOne({
      _id,
      orgId,
      name,
      contentIds,
      programId,
      programName,
      courseGroups,
      createdBy: session?.id || null,
      createdAt: Date.now(),
    });
    return NextResponse.json({ id: _id.toHexString(), name, itemCount: contentIds.length });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}
