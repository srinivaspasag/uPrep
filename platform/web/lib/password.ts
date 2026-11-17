import { scrypt, randomBytes, timingSafeEqual } from "crypto";

// Async scrypt runs on libuv's worker threads. The old scryptSync ran on the
// server's one JS thread (~50 ms per call), so when a whole class signed in
// at once every login queued behind the others AND every other request —
// opening tests, autosaving answers, submitting — froze until they were done.
// Same algorithm and default cost, so existing hashes verify unchanged.
function scryptAsync(password: string, salt: Buffer, keylen: number): Promise<Buffer> {
  return new Promise((resolve, reject) =>
    scrypt(password, salt, keylen, (err, key) => (err ? reject(err) : resolve(key)))
  );
}

// Self-contained password hashing for locally-created accounts (students/teachers
// added via CMDS, or self-signups). Lets them log in without the legacy
// user-services or any email-verification step. Stored as: scrypt$<saltHex>$<hashHex>
export async function hashPassword(password: string): Promise<string> {
  const salt = randomBytes(16);
  const hash = await scryptAsync(password, salt, 64);
  return `scrypt$${salt.toString("hex")}$${hash.toString("hex")}`;
}

export async function verifyPassword(password: string, stored: string): Promise<boolean> {
  try {
    const [scheme, saltHex, hashHex] = (stored || "").split("$");
    if (scheme !== "scrypt" || !saltHex || !hashHex) return false;
    const salt = Buffer.from(saltHex, "hex");
    const expected = Buffer.from(hashHex, "hex");
    const actual = await scryptAsync(password, salt, expected.length);
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
