import zlib from "zlib";
import { pageText, type PlacedImage } from "./pdfColumns";

// Text + pictures from an exam-paper PDF, without any AI service.
//
// Uses the pdf.js 2.0 build that ships inside pdf-parse (already a
// dependency) because it can decode every embedded picture — including
// JPEGs — to raw pixels on the server (nativeImageDecoderSupport "none");
// pdf-parse's default 1.10 build needs a browser for JPEGs. Each picture is
// returned as a PNG and marked in the text with a placeholder token
// `![image](pdfimg:N)` at the spot it sits on the page, so the question
// splitter keeps it inside the right question/option. The caller saves the
// PNGs and swaps the placeholders for real /uploads/… URLs.

// eslint-disable-next-line @typescript-eslint/no-var-requires
const PDFJS = require("pdf-parse/lib/pdf.js/v2.0.550/build/pdf.js");

export const IMG_PLACEHOLDER = (n: number) => `![image](pdfimg:${n})`;

// Smaller than this (in PDF points) is decoration — bullets, rules, icons.
const MIN_SIDE_PT = 14;

// --- PNG encoding (no dependency) -------------------------------------------
const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
function crc32(buf: Buffer): number {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(type: string, data: Buffer): Buffer {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type, "ascii"), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}

// pdf.js ImageKind: 1 = 1-bit grayscale (packed rows), 2 = RGB, 3 = RGBA.
export function rawToPng(width: number, height: number, kind: number, data: Uint8Array | Uint8ClampedArray): Buffer {
  const channels = kind === 3 ? 4 : 3;
  const rowLen = width * channels;
  const raw = Buffer.alloc((rowLen + 1) * height);
  for (let y = 0; y < height; y++) {
    const o = y * (rowLen + 1);
    raw[o] = 0; // filter: none
    if (kind === 1) {
      const packed = Math.ceil(width / 8);
      for (let x = 0; x < width; x++) {
        const bit = (data[y * packed + (x >> 3)] >> (7 - (x & 7))) & 1;
        const v = bit ? 255 : 0;
        raw[o + 1 + x * 3] = v;
        raw[o + 2 + x * 3] = v;
        raw[o + 3 + x * 3] = v;
      }
    } else {
      raw.set(data.subarray(y * rowLen, y * rowLen + rowLen), o + 1);
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; // bit depth
  ihdr[9] = channels === 4 ? 6 : 2; // RGBA : RGB
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk("IHDR", ihdr),
    chunk("IDAT", zlib.deflateSync(raw)),
    chunk("IEND", Buffer.alloc(0)),
  ]);
}

// --- extraction ---------------------------------------------------------------
type M = [number, number, number, number, number, number];
const mul = (m: M, n: number[]): M => [
  m[0] * n[0] + m[2] * n[1],
  m[1] * n[0] + m[3] * n[1],
  m[0] * n[2] + m[2] * n[3],
  m[1] * n[2] + m[3] * n[3],
  m[0] * n[4] + m[2] * n[5] + m[4],
  m[1] * n[4] + m[3] * n[5] + m[5],
];

export async function extractPdf(buf: Buffer): Promise<{ text: string; images: Buffer[] }> {
  const doc = await PDFJS.getDocument({
    data: new Uint8Array(buf),
    nativeImageDecoderSupport: "none",
    disableFontFace: true,
  }).promise;
  const OPS = PDFJS.OPS;
  const images: Buffer[] = [];
  const seen = new Map<string, number>(); // same picture reused (e.g. logo on every page)
  const pageData: { items: any[]; width: number; placed: PlacedImage[] }[] = [];
  try {
    for (let n = 1; n <= doc.numPages; n++) {
      const page = await doc.getPage(n);
      const viewport = page.getViewport(1);
      const [tc, ol] = await Promise.all([page.getTextContent(), page.getOperatorList()]);
      const placed: PlacedImage[] = [];
      let ctm: M = [1, 0, 0, 1, 0, 0];
      const stack: M[] = [];
      for (let i = 0; i < ol.fnArray.length; i++) {
        const fn = ol.fnArray[i];
        const args = ol.argsArray[i];
        if (fn === OPS.save) stack.push(ctm);
        else if (fn === OPS.restore) ctm = stack.pop() || ctm;
        else if (fn === OPS.transform) ctm = mul(ctm, args);
        else if (fn === OPS.paintImageXObject || fn === OPS.paintJpegXObject || fn === OPS.paintInlineImageXObject) {
          // Unit square mapped by the CTM: x, y = bottom-left; a, d = size.
          const w = Math.abs(ctm[0]);
          const h = Math.abs(ctm[3]);
          if (w < MIN_SIDE_PT || h < MIN_SIDE_PT) continue;
          let img: any = null;
          try {
            img = typeof args[0] === "string" ? page.objs.get(args[0]) : args[0];
          } catch {
            img = null;
          }
          if (!img?.data || !img.width || !img.height) continue;
          const key = `${img.width}x${img.height}:${img.data.length}:${img.data[0]},${img.data[img.data.length >> 1]}`;
          let idx = seen.get(key);
          if (idx === undefined) {
            try {
              images.push(rawToPng(img.width, img.height, img.kind, img.data));
            } catch {
              continue;
            }
            idx = images.length - 1;
            seen.set(key, idx);
          }
          const x = Math.min(ctm[4], ctm[4] + ctm[0]);
          const yTop = Math.max(ctm[5], ctm[5] + ctm[3]);
          placed.push({ x, yTop, w, token: IMG_PLACEHOLDER(idx) });
        }
      }
      pageData.push({ items: tc.items || [], width: viewport.width, placed });
      page.cleanup();
    }
  } finally {
    doc.destroy();
  }
  // A picture that appears on more than one page is a logo/letterhead, not
  // part of a question.
  const pagesWith = new Map<string, number>();
  for (const p of pageData) for (const t of Array.from(new Set(p.placed.map((x) => x.token)))) pagesWith.set(t, (pagesWith.get(t) || 0) + 1);
  const text = pageData
    .map((p) => pageText(p.items, p.width, p.placed.filter((x) => (pagesWith.get(x.token) || 0) < 2)))
    .join("\n\n");
  // Pictures above the first question are the paper's letterhead/logo.
  const firstQ = text.search(/(^|\n)\s*1\s*[.\]]\s/);
  const cleaned =
    firstQ > 0 ? text.slice(0, firstQ).replace(/!\[image\]\(pdfimg:\d+\)\n?/g, "") + text.slice(firstQ) : text;
  return { text: cleaned, images };
}
