import { scryptSync, randomBytes, timingSafeEqual } from "crypto";

// Self-contained password hashing for locally-created accounts (students/teachers
// added via CMDS, or self-signups). Lets them log in without the legacy
// user-services or any email-verification step. Stored as: scrypt$<saltHex>$<hashHex>
export function hashPassword(password: string): string {
  const salt = randomBytes(16);
  const hash = scryptSync(password, salt, 64);
  return `scrypt$${salt.toString("hex")}$${hash.toString("hex")}`;
}

export function verifyPassword(password: string, stored: string): boolean {
  try {
    const [scheme, saltHex, hashHex] = (stored || "").split("$");
    if (scheme !== "scrypt" || !saltHex || !hashHex) return false;
    const salt = Buffer.from(saltHex, "hex");
    const expected = Buffer.from(hashHex, "hex");
    const actual = scryptSync(password, salt, expected.length);
    return expected.length === actual.length && timingSafeEqual(expected, actual);
  } catch {
    return false;
  }
}

// Readable default password when an admin doesn't set one (shown once so it can
// be shared with the student/teacher).
export function generatePassword(): string {
  return `Uprep@${Math.floor(10000 + Math.random() * 90000)}`;
}
