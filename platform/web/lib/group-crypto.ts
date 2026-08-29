import crypto from "crypto";
import { ObjectId } from "mongodb";

// Per-group content encryption — Node's built-in `crypto` only (the
// `archiver` incident is exactly why: no new dependency for something this
// size). One AES-256-GCM key per distribution group, generated once and
// persisted on the group document, never shipped inside the package itself.
// It's only ever released to a device over the network, via a successful
// /api/seller/verify call — see that route. The SD card/pendrive therefore
// only ever holds ciphertext; a device that never verifies never gets the
// key, regardless of how the files were obtained.
const GROUPS_COLL = "sellergroups";
const ALGORITHM = "aes-256-gcm";
const IV_LENGTH = 12; // bytes, GCM standard
const TAG_LENGTH = 16; // bytes

export async function getOrCreateGroupKey(db: any, groupId: string): Promise<Buffer> {
  const _id = new ObjectId(groupId);
  const group = await db.collection(GROUPS_COLL).findOne({ _id }, { projection: { encryptionKey: 1 } });
  if (group?.encryptionKey) return Buffer.from(group.encryptionKey, "base64");

  const key = crypto.randomBytes(32);
  await db
    .collection(GROUPS_COLL)
    .updateOne({ _id }, { $set: { encryptionKey: key.toString("base64") } });
  return key;
}

// Output layout: iv (12 bytes) || authTag (16 bytes) || ciphertext.
// A decryptor reads those first 28 bytes off the front, then decrypts the
// rest — no separate metadata file needed per encrypted file.
export function encryptBuffer(plain: Buffer, key: Buffer): Buffer {
  const iv = crypto.randomBytes(IV_LENGTH);
  const cipher = crypto.createCipheriv(ALGORITHM, key, iv);
  const ciphertext = Buffer.concat([cipher.update(plain), cipher.final()]);
  const authTag = cipher.getAuthTag();
  return Buffer.concat([iv, authTag, ciphertext]);
}

export function decryptBuffer(encrypted: Buffer, key: Buffer): Buffer {
  const iv = encrypted.subarray(0, IV_LENGTH);
  const authTag = encrypted.subarray(IV_LENGTH, IV_LENGTH + TAG_LENGTH);
  const ciphertext = encrypted.subarray(IV_LENGTH + TAG_LENGTH);
  const decipher = crypto.createDecipheriv(ALGORITHM, key, iv);
  decipher.setAuthTag(authTag);
  return Buffer.concat([decipher.update(ciphertext), decipher.final()]);
}

export const ENCRYPTION_INFO = {
  algorithm: ALGORITHM,
  ivLength: IV_LENGTH,
  tagLength: TAG_LENGTH,
  layout: "iv(12 bytes) + authTag(16 bytes) + ciphertext",
};
