import { NextRequest, NextResponse } from "next/server";
import { API, DEFAULT_ORG_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";
import { verifyPassword } from "@/lib/password";
import {
  SESSION_COOKIE,
  createSessionToken,
  sessionCookieOptions,
  type SessionPayload,
} from "@/lib/auth-session";

export const runtime = "nodejs";

function readSuperAdmin(member: any): boolean {
  return member?.isSuperAdmin === true || member?.extraInfo?.isSuperAdmin === true;
}

// Builds the JSON response AND attaches the signed session cookie the CMDS gate
// (middleware) verifies on every /cmds request.
async function withSession(
  result: Record<string, unknown>,
  payload: Omit<SessionPayload, "exp">,
  status = 200
) {
  const res = NextResponse.json({ result }, { status });
  const token = await createSessionToken(payload);
  res.cookies.set(SESSION_COOKIE, token, sessionCookieOptions());
  return res;
}

// Mirrors the legacy web-app username login: user-services
// POST /users/authenticateUser with username = "orgId:memberId" (form-encoded).
export async function POST(req: NextRequest) {
  let identifier = "";
  let password = "";
  try {
    const body = await req.json();
    identifier = (body.identifier || "").trim();
    password = body.password || "";
  } catch {
    return NextResponse.json(
      { errorCode: "BAD_REQUEST", errorMessage: "Invalid request body" },
      { status: 400 }
    );
  }

  if (!identifier || !password) {
    return NextResponse.json(
      { errorCode: "MISSING_PARAMETERS", errorMessage: "Enter your ID and password" },
      { status: 400 }
    );
  }

  // Username is "orgId:memberId". If the user typed only a memberId, prefix the
  // default org so the backend can resolve it.
  const username = identifier.includes(":")
    ? identifier
    : `${DEFAULT_ORG_ID}:${identifier}`;

  // --- Local (Mongo) auth first ---------------------------------------------
  // Accounts created via CMDS / self-signup with a local passwordHash log in
  // directly against Mongo, with no dependency on the legacy user-services and
  // no email-verification step. Legacy accounts fall through to the service.
  const [orgId, ...rest] = username.split(":");
  const memberOrEmail = rest.join(":") || identifier;
  const lookup = memberOrEmail.trim().toLowerCase();
  try {
    const db = await getDb();
    const member: any = await db.collection("orgmembers").findOne({
      orgId,
      recordState: "ACTIVE",
      passwordHash: { $exists: true, $ne: null },
      $or: [{ memberId: memberOrEmail.trim() }, { email: lookup }],
    });
    if (member?.passwordHash) {
      if (!verifyPassword(password, member.passwordHash)) {
        return NextResponse.json(
          { errorCode: "INVALID_CREDENTIALS", errorMessage: "Incorrect ID or password" },
          { status: 401 }
        );
      }
      const profile = (member.profile || "STUDENT").toUpperCase();
      const isSuperAdmin = readSuperAdmin(member);
      return await withSession(
        {
          id: String(member._id),
          firstName: member.firstName || "",
          lastName: member.lastName || "",
          memberId: member.memberId || null,
          profile,
          isSuperAdmin,
          authType: "LOCAL",
        },
        {
          id: String(member._id),
          orgId,
          memberId: member.memberId || null,
          firstName: member.firstName || "",
          lastName: member.lastName || "",
          profile,
          isSuperAdmin,
        }
      );
    }
  } catch {
    // If Mongo is unavailable, fall through to the legacy service below.
  }

  const form = new URLSearchParams({
    username,
    password,
    callingApp: "web-app",
    callingAppId: "web-app",
  });

  try {
    const resp = await fetch(`${API.user}/users/authenticateUser`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form.toString(),
      cache: "no-store",
    });
    const text = await resp.text();
    let data: any;
    try {
      data = JSON.parse(text);
    } catch {
      data = { errorCode: "UPSTREAM_NON_JSON", raw: text.slice(0, 500) };
    }

    // On a successful legacy auth, enrich the caller's profile from Mongo (same
    // orgmembers source the People console uses) and issue the signed session
    // cookie so the CMDS gate can authorize this user.
    if (resp.ok && data && !data.errorCode) {
      try {
        const db = await getDb();
        const member: any = await db.collection("orgmembers").findOne({
          orgId,
          recordState: "ACTIVE",
          $or: [{ memberId: memberOrEmail.trim() }, { email: lookup }],
        });
        const profile = (member?.profile || data?.result?.profile || "STUDENT").toUpperCase();
        const isSuperAdmin = readSuperAdmin(member);
        const id = String(member?._id || data?.result?.id || data?.result?.userId || "");
        const memberId = member?.memberId || data?.result?.memberId || memberOrEmail.trim() || null;

        if (data.result && typeof data.result === "object") {
          data.result.profile = profile;
          data.result.isSuperAdmin = isSuperAdmin;
        }

        const res = NextResponse.json(data, { status: resp.status });
        const token = await createSessionToken({
          id,
          orgId,
          memberId,
          firstName: member?.firstName || data?.result?.firstName || "",
          lastName: member?.lastName || data?.result?.lastName || "",
          profile,
          isSuperAdmin,
        });
        res.cookies.set(SESSION_COOKIE, token, sessionCookieOptions());
        return res;
      } catch {
        // If enrichment fails, still return the upstream result (no cookie).
      }
    }

    return NextResponse.json(data, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      {
        errorCode: "UPSTREAM_UNREACHABLE",
        errorMessage: `Auth service unreachable at ${API.user}`,
        detail: String(e),
      },
      { status: 502 }
    );
  }
}
