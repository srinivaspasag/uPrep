// Institute subdomains: https://<slug>.edunora.ai is that institute's own
// address (branded login, then the same app), https://app.edunora.ai is the
// platform's own (super admin, generic login), and edunora.ai/www stay the
// marketing site on Hostinger. One wildcard DNS record covers every
// institute; Caddy issues each subdomain's certificate on first visit after
// asking /api/tls/allowed, so only real institutes ever get one.
export const PLATFORM_DOMAIN = (process.env.NEXT_PUBLIC_PLATFORM_DOMAIN || "edunora.ai").toLowerCase();
export const APP_HOST = `app.${PLATFORM_DOMAIN}`;

// Names an institute may never take as its slug — they're (or will be) the
// platform's own subdomains.
export const RESERVED_SLUGS = new Set([
  "app", "www", "api", "admin", "mail", "email", "smtp", "cdn", "static", "assets", "docs",
  "help", "support", "status", "blog", "staging", "dev", "test", "demo", "login", "auth",
  "edunora", "dashboard", "cmds",
]);

// The institute slug a host names, or null for app./www./the bare domain,
// any other domain (sslip.io, localhost) or anything not one label deep.
export function instituteSlugFromHost(host: string | null | undefined): string | null {
  const h = (host || "").toLowerCase().split(":")[0];
  const suffix = `.${PLATFORM_DOMAIN}`;
  if (!h.endsWith(suffix)) return null;
  const sub = h.slice(0, -suffix.length);
  if (!sub || sub.includes(".") || RESERVED_SLUGS.has(sub)) return null;
  return sub;
}
