import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// List programs (with per-program section counts) and create new ones.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("orgprograms")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ lastUpdated: -1 })
      .toArray();
    const sections = await db.collection("orgsections").find({ orgId, recordState: "ACTIVE" }).toArray();

    const programs = (docs as any[]).map((d) => ({
      id: String(d._id),
      name: d.name || d.cName || "(untitled)",
      code: d.code || null,
      description: d.description || "",
      isOffline: !!d.isOffline,
      sectionCount: (sections as any[]).filter((s) => s.programId === String(d._id)).length,
    }));
    return NextResponse.json({ programs, orgId });
  } catch (e: any) {
    return NextResponse.json({ programs: [], error: e?.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as {
    name?: string;
    description?: string;
    departmentId?: string;
    orgId?: string;
  };
  const name = (b.name || "").trim();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!name) return NextResponse.json({ error: "Program name is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("orgprograms").insertOne({
      _id,
      orgId,
      name,
      cName: name.toLowerCase(),
      code: name.replace(/\s+/g, "_").toUpperCase().slice(0, 20),
      description: (b.description || "").trim(),
      departmentId: b.departmentId || null,
      centersSections: [],
      isOffline: false,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}
