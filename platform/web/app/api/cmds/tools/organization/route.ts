import { NextRequest, NextResponse } from "next/server";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { callOrgService } from "@/lib/legacyOrg";

export const dynamic = "force-dynamic";

// Organization Info — backed by the live legacy org-services API
// (Organizations.getOrganization / updateOrganization), not Mongo directly.
// See legacy/lms-master/organization/organization-services/app/controllers/
// Organizations.java:172-193,599-622.

async function actingUserId(req: NextRequest): Promise<string> {
  const session = await sessionFromReq(req);
  return session?.id || "admin";
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const userId = await actingUserId(req);
  try {
    const o = await callOrgService<any>("getOrganization", { orgId, userId });
    if (!o?.id) return NextResponse.json({ error: "Organization not found" }, { status: 404 });

    return NextResponse.json({
      org: {
        id: o.id,
        name: o.name || "",
        fullName: o.fullName || "",
        website: o.website || "",
        contactNumber: o.contactNumber || "",
        type: o.type || "COLLEGE",
        address: o.address || "",
        description: o.description || "",
        authType: o.authType || "VEDANTU",
        doubtsForumMode: o.doubtsForumMode || "public",
        locations: Array.isArray(o.locations) ? o.locations : [],
        socialMedia: o.socialMedia || {},
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load organization" }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as any;
  const orgId = await resolveOrgId(req, b.orgId);
  const userId = await actingUserId(req);
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  // Legacy's updateOrganization only writes fields named in `updateList`.
  const fields: Record<string, unknown> = {
    name,
    fullName: (b.fullName || "").trim(),
    website: (b.website || "").trim(),
    contactNumber: (b.contactNumber || "").trim(),
    type: b.type || "COLLEGE",
    address: (b.address || "").trim(),
    description: (b.description || "").trim(),
  };
  const updateList = Object.keys(fields);

  try {
    await callOrgService("updateOrganization", {
      ...fields,
      orgId,
      userId,
      callingUserId: userId,
      authType: b.authType || "VEDANTU",
      doubtsForumMode: b.doubtsForumMode || "public",
      updateList,
    });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
