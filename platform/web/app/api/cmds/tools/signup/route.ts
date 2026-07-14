import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// External Signup config — a single `signupconfigs` doc per org controlling the
// public self-registration form.
const DEFAULTS = {
  enabled: false,
  requireApproval: true,
  defaultProfile: "STUDENT",
  welcomeMessage: "Welcome to UPrep! Complete your registration to get started.",
  collectPhone: true,
  allowedDomains: "",
};

export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const doc: any = await db.collection("signupconfigs").findOne({ orgId });
    return NextResponse.json({ config: { ...DEFAULTS, ...(doc || {}), orgId } });
  } catch (e: any) {
    return NextResponse.json({ config: { ...DEFAULTS, orgId }, error: e?.message }, { status: 500 });
  }
}

export async function PUT(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as any;
  const orgId = b.orgId || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const set = {
      enabled: !!b.enabled,
      requireApproval: !!b.requireApproval,
      defaultProfile: (b.defaultProfile || "STUDENT").toUpperCase(),
      welcomeMessage: (b.welcomeMessage || "").trim(),
      collectPhone: !!b.collectPhone,
      allowedDomains: (b.allowedDomains || "").trim(),
      lastUpdated: Date.now(),
    };
    await db.collection("signupconfigs").updateOne({ orgId }, { $set: { orgId, ...set } }, { upsert: true });
    return NextResponse.json({ ok: true, config: { ...set, orgId } });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Save failed" }, { status: 500 });
  }
}
