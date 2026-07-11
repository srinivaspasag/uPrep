import { NextRequest, NextResponse } from "next/server";
import { API, DEFAULT_ORG_ID } from "@/lib/config";
import { getDb } from "@/lib/mongo";
import { verifyPassword } from "@/lib/password";

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
      return NextResponse.json({
        result: {
          id: String(member._id),
          firstName: member.firstName || "",
          lastName: member.lastName || "",
          memberId: member.memberId || null,
          authType: "LOCAL",
        },
      });
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
    let data: unknown;
    try {
      data = JSON.parse(text);
    } catch {
      data = { errorCode: "UPSTREAM_NON_JSON", raw: text.slice(0, 500) };
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
