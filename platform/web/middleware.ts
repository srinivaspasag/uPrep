import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { SESSION_COOKIE, verifySessionToken } from "@/lib/auth-session";
import { isStaff } from "@/lib/roles";

// Access gate for the institute console — the direct analogue of the legacy
// cmds-app Security.checkAccess() @Before interceptor. Runs on every /cmds page
// and /api/cmds request and admits only authenticated staff profiles.
export const config = {
  matcher: ["/cmds", "/cmds/:path*", "/api/cmds/:path*"],
};

// Announcements are shared with students (the learn app News page reads them),
// so the news GET is exempt from the staff gate.
function isPublicRead(req: NextRequest): boolean {
  return req.method === "GET" && req.nextUrl.pathname === "/api/cmds/tools/news";
}

export async function middleware(req: NextRequest) {
  if (isPublicRead(req)) return NextResponse.next();

  const isApi = req.nextUrl.pathname.startsWith("/api/");
  const session = await verifySessionToken(req.cookies.get(SESSION_COOKIE)?.value);

  if (!session) {
    if (isApi)
      return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    url.searchParams.set("next", req.nextUrl.pathname);
    return NextResponse.redirect(url);
  }

  if (!isStaff(session.profile)) {
    // Legacy sent non-staff to "/noteligible"; here the learn app is home.
    if (isApi)
      return NextResponse.json({ error: "CMDS access requires a staff account" }, { status: 403 });
    const url = req.nextUrl.clone();
    url.pathname = "/learn/library";
    url.search = "";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}
