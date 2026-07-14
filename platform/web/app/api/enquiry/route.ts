import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Public "Get in Touch" enquiries from the marketing site. Stored in Mongo so
// admins can follow up (the legacy PHP site emailed info@uprep.in instead).
export async function POST(req: NextRequest) {
  const b = await req.json().catch(() => ({}));
  const name = (b.name || "").trim();
  const email = (b.email || "").trim();
  const phone = (b.phone || "").trim();
  const classComp = (b.classComp || "").trim();

  if (!name || (!email && !phone)) {
    return NextResponse.json(
      { error: "Please provide your name and a way to reach you." },
      { status: 400 }
    );
  }

  try {
    const db = await getDb();
    const now = Date.now();
    const doc = {
      _id: new ObjectId(),
      orgId: b.orgId || DEFAULT_ORG_ID,
      name,
      email,
      phone,
      classComp,
      source: "marketing-site",
      status: "NEW",
      recordState: "ACTIVE",
      timeCreated: now,
    };
    await db.collection("enquiries").insertOne(doc);
    return NextResponse.json({ ok: true, id: String(doc._id) });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Could not submit enquiry" },
      { status: 500 }
    );
  }
}
