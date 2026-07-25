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

// Impersonation: while an admin is browsing as another user we keep a small
// marker (label of the impersonated user) so the app can show a "return to
// admin" banner. The server holds the admin's real session in a cookie.
const IMP_KEY = "uprep_impersonating";

export function setImpersonating(label: string) {
  try {
    sessionStorage.setItem(IMP_KEY, label);
  } catch {}
}

export function getImpersonating(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return sessionStorage.getItem(IMP_KEY);
  } catch {
    return null;
  }
}

export function clearImpersonating() {
  try {
    sessionStorage.removeItem(IMP_KEY);
  } catch {}
}
