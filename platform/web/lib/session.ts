"use client";

export type UprepSession = {
  id: string;
  firstName?: string;
  lastName?: string;
  memberId?: string | null;
  thumbnail?: string;
  authType?: string;
  profile?: string;
  isSuperAdmin?: boolean;
};

const KEY = "uprep_session";

export function getSession(): UprepSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = sessionStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as UprepSession) : null;
  } catch {
    return null;
  }
}

export function setSession(s: unknown) {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(s));
  } catch {}
}

export function clearSession() {
  try {
    sessionStorage.removeItem(KEY);
  } catch {}
  // Also drop the server-trusted session cookie so CMDS access is revoked.
  try {
    fetch("/api/auth/logout", { method: "POST", keepalive: true });
  } catch {}
}
