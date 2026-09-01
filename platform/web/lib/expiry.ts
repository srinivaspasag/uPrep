// Shared 1-year expiry policy for student accounts and SD-card access codes.
// Both clocks start at creation time (account creation / access-code
// generation), not first use — simple and predictable for whoever issues
// access.
export const ONE_YEAR_MS = 365 * 24 * 60 * 60 * 1000;

export function isExpired(createdAtMs: number | null | undefined): boolean {
  if (!createdAtMs) return false;
  return Date.now() > createdAtMs + ONE_YEAR_MS;
}

export function expiresAt(createdAtMs: number): number {
  return createdAtMs + ONE_YEAR_MS;
}
