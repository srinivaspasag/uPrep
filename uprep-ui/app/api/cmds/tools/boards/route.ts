import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Boards & Course Management — a subject → chapter → topic tree stored in a
// `topics` collection ({name, parentId, orgId}). Returns the full flat list;
// the UI assembles the tree.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("topics")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ name: 1 })
      .limit(1000)
      .toArray();
    return NextResponse.json({
      nodes: (docs as any[]).map((t) => ({
        id: String(t._id),
        name: t.name,
        parentId: t.parentId || null,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ nodes: [], error: e?.message }, { status: 500 });
  }
}

type Body = { orgId?: string; name?: string; parentId?: string | null };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("topics").insertOne({
      _id,
      orgId: b.orgId || DEFAULT_ORG_ID,
      name,
      parentId: b.parentId || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name, parentId: b.parentId || null });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to add" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    // Soft-delete the node and its descendants.
    const toRemove = [id];
    const all: any[] = await db.collection("topics").find({ recordState: "ACTIVE" }).toArray();
    const childrenOf = (pid: string) => all.filter((n) => String(n.parentId) === pid).map((n) => String(n._id));
    for (let i = 0; i < toRemove.length; i++) toRemove.push(...childrenOf(toRemove[i]));
    const oids = Array.from(new Set(toRemove)).map((x) => new ObjectId(x));
    await db
      .collection("topics")
      .updateMany({ _id: { $in: oids } }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true, removed: oids.length });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
