import { createWriteStream, type WriteStream } from "fs";
import { deflateRawSync } from "zlib";

// Minimal ZIP writer (store + deflate), built on Node core only. Pulled in
// after `archiver` broke production: its dependency tree has a package.json
// `exports` map with "default" listed before other conditions, which this
// Next.js/webpack version (14.2.35) refuses to resolve at all — "Module not
// found: Default condition should be last one" killed the whole build. No
// third-party zip library here is worth risking that again for something
// this small; the ZIP format itself is simple enough to write directly
// against the spec (PKWARE APPNOTE.TXT / Wikipedia's ZIP article).

// CRC-32 (ISO 3309 / ITU-T V.42), same table-driven algorithm all zip tools use.
const CRC_TABLE = (() => {
  const table = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    table[n] = c >>> 0;
  }
  return table;
})();

function crc32(buf: Buffer): number {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

// MS-DOS date/time packed format ZIP headers require.
function dosDateTime(d: Date): { time: number; date: number } {
  const time = ((d.getHours() & 0x1f) << 11) | ((d.getMinutes() & 0x3f) << 5) | ((d.getSeconds() >> 1) & 0x1f);
  const date = (((d.getFullYear() - 1980) & 0x7f) << 9) | (((d.getMonth() + 1) & 0xf) << 5) | (d.getDate() & 0x1f);
  return { time, date };
}

export type ZipEntry = { name: string; data: Buffer };

export function buildZip(entries: ZipEntry[]): Buffer {
  const { time, date } = dosDateTime(new Date());
  const localParts: Buffer[] = [];
  const centralParts: Buffer[] = [];
  let offset = 0;

  for (const entry of entries) {
    const nameBuf = Buffer.from(entry.name, "utf8");
    const crc = crc32(entry.data);
    const compressed = deflateRawSync(entry.data);
    // Deflate can occasionally grow tiny/incompressible input — fall back to
    // "stored" (method 0) rather than ship a compressed blob larger than the original.
    const useStore = compressed.length >= entry.data.length;
    const method = useStore ? 0 : 8;
    const payload = useStore ? entry.data : compressed;

    const localHeader = Buffer.alloc(30);
    localHeader.writeUInt32LE(0x04034b50, 0);
    localHeader.writeUInt16LE(20, 4); // version needed
    localHeader.writeUInt16LE(0, 6); // flags
    localHeader.writeUInt16LE(method, 8);
    localHeader.writeUInt16LE(time, 10);
    localHeader.writeUInt16LE(date, 12);
    localHeader.writeUInt32LE(crc, 14);
    localHeader.writeUInt32LE(payload.length, 18);
    localHeader.writeUInt32LE(entry.data.length, 22);
    localHeader.writeUInt16LE(nameBuf.length, 26);
    localHeader.writeUInt16LE(0, 28); // extra length
    localParts.push(localHeader, nameBuf, payload);

    const centralHeader = Buffer.alloc(46);
    centralHeader.writeUInt32LE(0x02014b50, 0);
    centralHeader.writeUInt16LE(20, 4); // version made by
    centralHeader.writeUInt16LE(20, 6); // version needed
    centralHeader.writeUInt16LE(0, 8); // flags
    centralHeader.writeUInt16LE(method, 10);
    centralHeader.writeUInt16LE(time, 12);
    centralHeader.writeUInt16LE(date, 14);
    centralHeader.writeUInt32LE(crc, 16);
    centralHeader.writeUInt32LE(payload.length, 20);
    centralHeader.writeUInt32LE(entry.data.length, 24);
    centralHeader.writeUInt16LE(nameBuf.length, 28);
    centralHeader.writeUInt16LE(0, 30); // extra length
    centralHeader.writeUInt16LE(0, 32); // comment length
    centralHeader.writeUInt16LE(0, 34); // disk number start
    centralHeader.writeUInt16LE(0, 36); // internal attrs
    centralHeader.writeUInt32LE(0, 38); // external attrs
    centralHeader.writeUInt32LE(offset, 42); // offset of local header
    centralParts.push(centralHeader, nameBuf);

    offset += localHeader.length + nameBuf.length + payload.length;
  }

  const centralDirStart = offset;
  const centralDir = Buffer.concat(centralParts);

  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(0, 4); // disk number
  end.writeUInt16LE(0, 6); // disk with central dir
  end.writeUInt16LE(entries.length, 8);
  end.writeUInt16LE(entries.length, 10);
  end.writeUInt32LE(centralDir.length, 12);
  end.writeUInt32LE(centralDirStart, 16);
  end.writeUInt16LE(0, 20); // comment length

  return Buffer.concat([...localParts, centralDir, end]);
}

// Streaming, Zip64-capable writer for archives too large to hold in memory
// (the packaging route's JEE/NEET-scale groups run 50-70GB across thousands
// of files — buildZip() above would need the whole thing, plus every
// compressed payload, resident in RAM at once on a 7.6GB box). Writes each
// entry straight to disk as it arrives; only the small central-directory
// metadata (not file bytes) is held in memory between entries.
//
// Always emits Zip64 extra fields and the Zip64 end-of-central-directory
// record/locator for every entry, regardless of whether this particular
// archive actually exceeds the 32-bit limits — avoids a dual code path, and
// every reader that matters here (Python's zipfile, Info-ZIP, Android's
// java.util.zip) already handles Zip64 unconditionally. This is the same
// 4GB ceiling that forced Python's zipfile (not BSD unzip) to be used to
// extract the hand-built JEE/NEET packages earlier — this writer now
// produces that same Zip64 layout natively instead of relying on a
// one-off script.
const ZIP64_EXTRA_ID = 0x0001;
const VERSION_ZIP64 = 45; // 4.5 — minimum version needed to extract Zip64

type CentralRecord = {
  nameBuf: Buffer;
  crc: number;
  compSize: number;
  uncompSize: number;
  method: number;
  time: number;
  date: number;
  offset: number;
};

export class ZipStreamWriter {
  private readonly stream: WriteStream;
  private offset = 0;
  private readonly central: CentralRecord[] = [];
  private finished = false;

  constructor(destPath: string) {
    this.stream = createWriteStream(destPath);
  }

  private write(buf: Buffer): Promise<void> {
    return new Promise((resolve, reject) => {
      this.stream.write(buf, (err) => (err ? reject(err) : resolve()));
    });
  }

  async addFile(name: string, data: Buffer): Promise<void> {
    if (this.finished) throw new Error("ZipStreamWriter already finished");
    const { time, date } = dosDateTime(new Date());
    const nameBuf = Buffer.from(name, "utf8");
    const crc = crc32(data);
    const compressed = deflateRawSync(data);
    const useStore = compressed.length >= data.length;
    const method = useStore ? 0 : 8;
    const payload = useStore ? data : compressed;

    // Zip64 extended info extra field for the local header: just the two
    // 8-byte sizes (id 0x0001, size 16) — no offset field here, that only
    // belongs in the central-directory copy.
    const extra = Buffer.alloc(20);
    extra.writeUInt16LE(ZIP64_EXTRA_ID, 0);
    extra.writeUInt16LE(16, 2);
    extra.writeBigUInt64LE(BigInt(data.length), 4);
    extra.writeBigUInt64LE(BigInt(payload.length), 12);

    const header = Buffer.alloc(30);
    header.writeUInt32LE(0x04034b50, 0);
    header.writeUInt16LE(VERSION_ZIP64, 4); // version needed
    header.writeUInt16LE(0, 6); // flags
    header.writeUInt16LE(method, 8);
    header.writeUInt16LE(time, 10);
    header.writeUInt16LE(date, 12);
    header.writeUInt32LE(crc, 14);
    header.writeUInt32LE(0xffffffff, 18); // compressed size -> see Zip64 extra
    header.writeUInt32LE(0xffffffff, 22); // uncompressed size -> see Zip64 extra
    header.writeUInt16LE(nameBuf.length, 26);
    header.writeUInt16LE(extra.length, 28);

    await this.write(header);
    await this.write(nameBuf);
    await this.write(extra);
    await this.write(payload);

    this.central.push({
      nameBuf,
      crc,
      compSize: payload.length,
      uncompSize: data.length,
      method,
      time,
      date,
      offset: this.offset,
    });
    this.offset += header.length + nameBuf.length + extra.length + payload.length;
  }

  async finish(): Promise<{ sizeBytes: number }> {
    if (this.finished) throw new Error("ZipStreamWriter already finished");
    this.finished = true;

    const cdStart = this.offset;
    for (const rec of this.central) {
      // Zip64 extended info extra field for the central directory: both
      // sizes plus the local-header offset (id 0x0001, size 24).
      const extra = Buffer.alloc(28);
      extra.writeUInt16LE(ZIP64_EXTRA_ID, 0);
      extra.writeUInt16LE(24, 2);
      extra.writeBigUInt64LE(BigInt(rec.uncompSize), 4);
      extra.writeBigUInt64LE(BigInt(rec.compSize), 12);
      extra.writeBigUInt64LE(BigInt(rec.offset), 20);

      const header = Buffer.alloc(46);
      header.writeUInt32LE(0x02014b50, 0);
      header.writeUInt16LE(VERSION_ZIP64, 4); // version made by
      header.writeUInt16LE(VERSION_ZIP64, 6); // version needed
      header.writeUInt16LE(0, 8); // flags
      header.writeUInt16LE(rec.method, 10);
      header.writeUInt16LE(rec.time, 12);
      header.writeUInt16LE(rec.date, 14);
      header.writeUInt32LE(rec.crc, 16);
      header.writeUInt32LE(0xffffffff, 20); // compressed size -> see Zip64 extra
      header.writeUInt32LE(0xffffffff, 24); // uncompressed size -> see Zip64 extra
      header.writeUInt16LE(rec.nameBuf.length, 28);
      header.writeUInt16LE(extra.length, 30);
      header.writeUInt16LE(0, 32); // comment length
      header.writeUInt16LE(0, 34); // disk number start (never multi-disk)
      header.writeUInt16LE(0, 36); // internal attrs
      header.writeUInt32LE(0, 38); // external attrs
      header.writeUInt32LE(0xffffffff, 42); // local header offset -> see Zip64 extra

      await this.write(header);
      await this.write(rec.nameBuf);
      await this.write(extra);
      this.offset += header.length + rec.nameBuf.length + extra.length;
    }
    const cdSize = this.offset - cdStart;
    const zip64EocdOffset = this.offset;

    const zip64Eocd = Buffer.alloc(56);
    zip64Eocd.writeUInt32LE(0x06064b50, 0);
    zip64Eocd.writeBigUInt64LE(BigInt(44), 4); // size of remaining record
    zip64Eocd.writeUInt16LE(VERSION_ZIP64, 12); // version made by
    zip64Eocd.writeUInt16LE(VERSION_ZIP64, 14); // version needed
    zip64Eocd.writeUInt32LE(0, 16); // disk number
    zip64Eocd.writeUInt32LE(0, 20); // disk with start of central dir
    zip64Eocd.writeBigUInt64LE(BigInt(this.central.length), 24); // entries on this disk
    zip64Eocd.writeBigUInt64LE(BigInt(this.central.length), 32); // total entries
    zip64Eocd.writeBigUInt64LE(BigInt(cdSize), 40);
    zip64Eocd.writeBigUInt64LE(BigInt(cdStart), 48);

    const zip64Locator = Buffer.alloc(20);
    zip64Locator.writeUInt32LE(0x07064b50, 0);
    zip64Locator.writeUInt32LE(0, 4); // disk with Zip64 EOCD
    zip64Locator.writeBigUInt64LE(BigInt(zip64EocdOffset), 8);
    zip64Locator.writeUInt32LE(1, 16); // total number of disks

    // Classic EOCD still required (older-but-still-live readers look for it
    // first) — counts/sizes/offset all sentineled to 0xFFFF/0xFFFFFFFF so
    // readers know to go consult the Zip64 record above instead.
    const eocd = Buffer.alloc(22);
    eocd.writeUInt32LE(0x06054b50, 0);
    eocd.writeUInt16LE(0, 4); // disk number
    eocd.writeUInt16LE(0, 6); // disk with central dir
    eocd.writeUInt16LE(0xffff, 8); // entries on this disk -> see Zip64 EOCD
    eocd.writeUInt16LE(0xffff, 10); // total entries -> see Zip64 EOCD
    eocd.writeUInt32LE(0xffffffff, 12); // central dir size -> see Zip64 EOCD
    eocd.writeUInt32LE(0xffffffff, 16); // central dir offset -> see Zip64 EOCD
    eocd.writeUInt16LE(0, 20); // comment length

    await this.write(zip64Eocd);
    await this.write(zip64Locator);
    await this.write(eocd);
    this.offset += zip64Eocd.length + zip64Locator.length + eocd.length;

    const sizeBytes = this.offset;
    await new Promise<void>((resolve, reject) => {
      this.stream.end((err: unknown) => (err ? reject(err) : resolve()));
    });
    return { sizeBytes };
  }

  // Cleans up a partially-written file on failure — called from the job
  // runner's catch block so a crashed/failed job doesn't leave a truncated
  // .zip sitting at the final `url` path looking like a finished package.
  destroy(err?: Error): void {
    this.stream.destroy(err);
  }
}
