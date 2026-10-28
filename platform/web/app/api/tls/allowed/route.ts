import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { APP_HOST, instituteSlugFromHost } from "@/lib/tenant-host";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Caddy's on-demand TLS "ask" endpoint (deploy/Caddyfile): before issuing a
// certificate for a *.edunora.ai name it calls GET ?domain=<host> and only
// proceeds on a 200. Without this anyone could point random subdomains at us
// and burn the Let's Encrypt rate limit (50 new certs/week per domain).
// Allowed: app.<domain>, and <slug>.<domain> for an ACTIVE institute's slug.
export async function GET(req: NextRequest) {
  const domain = (req.nextUrl.searchParams.get("domain") || "").toLowerCase();
  if (domain === APP_HOST) return new NextResponse("ok");
  const slug = instituteSlugFromHost(domain);
  if (!slug) return new NextResponse("no", { status: 404 });
  try {
    const db = await getDb();
    const org = await db
      .collection("organizations")
      .findOne({ slug, recordState: "ACTIVE" } as any, { projection: { _id: 1 } });
    return org ? new NextResponse("ok") : new NextResponse("no", { status: 404 });
  } catch {
    return new NextResponse("error", { status: 500 });
  }
}
