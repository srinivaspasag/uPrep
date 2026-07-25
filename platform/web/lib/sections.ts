import type { Db } from "mongodb";
import { randomBytes } from "crypto";

// Sections (a.k.a. batches) group students within an org and carry an access
// code students can redeem to self-enroll into the section's courses — the new
// stack's analogue of the legacy section/access-code enrolment.
export const SECTIONS_COLL = "sections";

const ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // no easily-confused chars

export function makeCode(len = 6): string {
  const bytes = randomBytes(len);
  let out = "";
  for (let i = 0; i < len; i++) out += ALPHABET[bytes[i] % ALPHABET.length];
  return out;
}

export async function uniqueCode(db: Db, orgId: string): Promise<string> {
  for (let i = 0; i < 8; i++) {
    const code = makeCode();
    const clash = await db
      .collection(SECTIONS_COLL)
      .findOne({ code, recordState: "ACTIVE" } as any);
    if (!clash) return code;
  }
  // Extremely unlikely; fall back to a longer code.
  return makeCode(8);
}
