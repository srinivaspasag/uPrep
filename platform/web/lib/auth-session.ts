// Server-trusted session token — the piece the legacy stack had (a Play server
// session) but the new app lacked. A compact HMAC-signed cookie carries the
// caller's identity + org profile so edge middleware and API routes can enforce
// CMDS access without trusting client-held sessionStorage.
//
// Uses Web Crypto (globalThis.crypto.subtle) so the exact same code runs in both
// the Node.js route runtime and the edge middleware runtime.

import { SESSION_COOKIE } from "@/lib/roles";

const SECRET =
  process.env.SESSION_SECRET || "uprep-dev-session-secret-change-in-prod";
export const SESSION_MAX_AGE_SEC = 7 * 24 * 60 * 60; // 7 days

export type SessionPayload = {
  id: string;
  orgId: string;
  memberId: string | null;
  firstName?: string;
  lastName?: string;
  profile: string;
  isSuperAdmin: boolean;
  exp: number; // ms epoch
};

function bytesToB64url(bytes: Uint8Array): string {
  let bin = "";
  for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function b64urlToBytes(s: string): Uint8Array {
  let t = s.replace(/-/g, "+").replace(/_/g, "/");
  while (t.length % 4) t += "=";
  const bin = atob(t);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

function encodeStr(s: string): string {
  return bytesToB64url(new TextEncoder().encode(s));
}

function decodeStr(s: string): string {
  return new TextDecoder().decode(b64urlToBytes(s));
}

async function hmac(data: string): Promise<string> {
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(SECRET),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(data));
  return bytesToB64url(new Uint8Array(sig));
}

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}

export async function createSessionToken(
  input: Omit<SessionPayload, "exp"> & { exp?: number }
): Promise<string> {
  const payload: SessionPayload = {
    ...input,
    exp: input.exp ?? Date.now() + SESSION_MAX_AGE_SEC * 1000,
  };
  const body = encodeStr(JSON.stringify(payload));
  const sig = await hmac(body);
  return `${body}.${sig}`;
}

export async function verifySessionToken(
  token: string | null | undefined
): Promise<SessionPayload | null> {
  if (!token) return null;
  const dot = token.lastIndexOf(".");
  if (dot <= 0) return null;
  const body = token.slice(0, dot);
  const sig = token.slice(dot + 1);
  const expected = await hmac(body);
  if (!timingSafeEqual(sig, expected)) return null;
  try {
    const payload = JSON.parse(decodeStr(body)) as SessionPayload;
    if (!payload || typeof payload.exp !== "number" || payload.exp < Date.now())
      return null;
    return payload;
  } catch {
    return null;
  }
}

export function sessionCookieOptions() {
  return {
    httpOnly: true,
    sameSite: "lax" as const,
    path: "/",
    secure: process.env.NODE_ENV === "production",
    maxAge: SESSION_MAX_AGE_SEC,
  };
}

export { SESSION_COOKIE };
