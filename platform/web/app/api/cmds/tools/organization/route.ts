import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
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

    // logoUrl is Mongo-direct (no faithful legacy field — see route header
    // note in tools/organizations/route.ts's `plan` field for precedent):
    // legacy's orgThumbnail is write-only via a separate multipart action
    // (uploadOrgPic) that isn't part of updateOrganization at all.
    const orgDoc = ObjectId.isValid(orgId)
      ? await (await getDb()).collection("organizations").findOne({ _id: new ObjectId(orgId) })
      : null;

    const appInfos: Array<{ type?: string; url?: string }> = Array.isArray(o.appInfos) ? o.appInfos : [];
    const playStoreLink = appInfos.find((a) => a?.type === "GOOGLE_PLAY")?.url || "";

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
        // Legacy's DoubtsForumMode is a Java enum (PUBLIC/PRIVATE/HIDDEN,
        // uppercase) — any other casing crashes Morphia on every subsequent
        // getOrganization call for this org.
        doubtsForumMode: (o.doubtsForumMode || "PUBLIC").toUpperCase(),
        locations: Array.isArray(o.locations) ? o.locations : [],
        socialMedia: o.socialMedia || {},
        playStoreLink,
        logoUrl: (orgDoc as any)?.logoUrl || "",
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
  // socialMedia and appInfos are sub-objects, not simple strings — legacy's
  // own admin JS (submitEditInstituteInfo) always lists both regardless of
  // whether their sub-fields changed, so we match that rather than only
  // include them conditionally.
  const fields: Record<string, unknown> = {
    name,
    fullName: (b.fullName || "").trim(),
    website: (b.website || "").trim(),
    contactNumber: (b.contactNumber || "").trim(),
    type: b.type || "COLLEGE",
    address: (b.address || "").trim(),
    description: (b.description || "").trim(),
  };
  // authType/doubtsForumMode are set outside `fields` above (radio inputs,
  // not the plain text fields) but still need to be in updateList or legacy
  // silently drops them on save — same class of bug as socialMedia/appInfos.
  const updateList = [...Object.keys(fields), "authType", "doubtsForumMode", "socialMedia", "appInfos"];

  const socialMedia = b.socialMedia || {};
  const playStoreLink = (b.playStoreLink || "").trim();
  const appInfos = playStoreLink ? [{ type: "GOOGLE_PLAY", url: playStoreLink }] : [];
  // Legacy's DoubtsForumMode is a Java enum (PUBLIC/PRIVATE/HIDDEN) — any
  // other casing crashes Morphia on every subsequent read of this org.
  const doubtsForumMode = (b.doubtsForumMode || "PUBLIC").toUpperCase();

  try {
    await callOrgService("updateOrganization", {
      ...fields,
      orgId,
      userId,
      callingUserId: userId,
      authType: b.authType || "VEDANTU",
      doubtsForumMode,
      socialMedia,
      appInfos,
      updateList,
    });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}
