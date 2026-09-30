import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { ObjectId } from "mongodb";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Caller's OWN org branding — used by LmsShell (student app header) so a
// student sees THEIR institute's logo/name, not a hardcoded default. Same
// minimal, public-safe field set as by-id/by-slug — just resolved from the
// session's orgId instead of a query param, and requires a session (any
// role) since it's serving the logged-in student's own header.
//
//   GET /api/orgs/mine -> { id, name, logoUrl }
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.orgId || !ObjectId.isValid(session.orgId)) {
    return NextResponse.json({ error: "Not logged in" }, { status: 401 });
  }
  try {
    const db = await getDb();
    const org = await db
      .collection("organizations")
      .findOne({ _id: new ObjectId(session.orgId), recordState: "ACTIVE" } as any);
    if (!org) return NextResponse.json({ error: "Institute not found" }, { status: 404 });
    return NextResponse.json({
      id: String(org._id),
      name: org.name || org.fullName || "",
      logoUrl: org.logoUrl || "",
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
