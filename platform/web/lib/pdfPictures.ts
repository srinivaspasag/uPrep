// Exact-copy question import: every question (and each of its options) is cut
// out of the rendered PDF page as a picture, so equations, vector arrows,
// stacked fractions, chemical structures, tables and diagrams arrive exactly
// as printed. No AI service, no OCR: pdf.js draws the page (the same engine
// Firefox uses) and the text layer is only used to *locate* things — the
// question numbers ("12.") and the option labels ("1)" … "4)").
//
// Why not text: many papers store their maths as drawn shapes, not
// characters (the DT-PHY paper's text layer has "1)" "2)" and nothing after
// them), so no amount of text parsing can recover it.
//
// The flow, per document:
//   1. Render every page at 150 dpi — drawn at 300 dpi and shrunk, so strokes
//      thinner than a pixel (the minus in "10⁻²") keep their full weight.
//      Pages are cached a few at a time (a 33-page A3 file would otherwise
//      hold ~600 MB of pixels).
//   2. Blank out page furniture that is not part of any question: the page
//      border box, the rule between two columns, running headers/footers
//      and page numbers.
//   3. Single- or two-column? (In two-column papers almost no printed text
//      crosses the middle of the page.) Each column of each page is a
//      "frame"; questions flow frame to frame in reading order.
//   4. Find the question numbers: line-leading "N." that run 1, 2, 3 … at the
//      same left margin (statements numbered inside a question sit further
//      right, so they never break the sequence).
//   5. A question is everything from just above its number to just above the
//      next one (into the next column/page if needed), stopping at a section
//      heading ("CHEMISTRY - SECTION A").
//   6. Inside it, the last complete run of option labels 1)…4) / (a)…(d)
//      splits it into the question picture and four option pictures.
//   7. Self-check: every printed pixel of the question must be inside one of
//      its pictures and no text may be cut between two pictures. If the
//      labels can't be found or the check fails, the whole question —
//      options included — is kept as one picture, so nothing is ever lost.
//
// Checked against 16 real papers/solution sheets (physics, chemistry,
// botany, zoology, JEE; single- and two-column): every question found,
// every picture passes the self-check, and the renders match poppler's.
//
// Pixel coordinates (top-left origin, at RENDER_DPI) are used throughout.

import fs from "fs";
import path from "path";

export const RENDER_DPI = 150;
const S = RENDER_DPI / 72;
// A pixel darker than this is ink. The diagonal watermark is light grey
// (~225), so it never counts as content.
const INK = 170;
// A run of blank pixel rows at least this tall separates two lines of print
// (≈2.4 pt). Inside one line — fraction bars, exponents, arrows over
// vectors — the gaps are smaller.
const LINE_GAP = 5;
const PAD = 6;
const PAGE_CACHE = 3;
const SUPERSAMPLE = 2;

export type CutQuestion = {
  num: number; // the number printed on the paper
  stem: Buffer[]; // PNG(s) of the question (several when it runs over a page)
  options: Buffer[] | null; // 4 PNGs, or null when kept inside the stem
  text: string; // the text layer of the region (for answer keys / checks)
  note?: string; // why options were kept inside the question picture
};
export type CutResult = {
  questions: CutQuestion[];
  missing: number[]; // numbers skipped in the 1…N run
  pages: number;
  warnings: string[];
};

type Item = { s: string; x: number; y: number; w: number; h: number; base: number }; // y = top, base = baseline
type Line = { items: Item[]; text: string; x: number; top: number; bottom: number; base: number };
type Page = { n: number; W: number; H: number; items: Item[]; lines: Line[] };
type Raster = { W: number; H: number; rgba: Uint8ClampedArray; ink: Uint8Array };
type Box = { x0: number; y0: number; x1: number; y1: number };
type Piece = { fi: number; y0: number; y1: number }; // a slice of one frame
// A frame is one text column of one page; questions flow frame to frame in
// reading order (left column, right column, next page …).
type Frame = { page: number; x0: number; x1: number; top: number; lines: Line[] };

const PDFJS_DIR = path.join(process.cwd(), "node_modules/pdfjs-dist");

async function loadPdfjs(): Promise<any> {
  // Imported at runtime from node_modules (an .mjs build); kept out of the
  // webpack bundle via next.config's serverComponentsExternalPackages.
  // eslint-disable-next-line no-new-func
  const dynamicImport = new Function("p", "return import(p)") as (p: string) => Promise<any>;
  return dynamicImport(path.join(PDFJS_DIR, "legacy/build/pdf.mjs"));
}

// One page as RGBA pixels at RENDER_DPI.
async function renderPage(doc: any, n: number): Promise<{ W: number; H: number; rgba: Uint8ClampedArray }> {
  // Drawn at twice the resolution, then each 2×2 block averaged: pdf.js
  // draws strokes thinner than a pixel too faintly (DT-PHY Q16's
  // superscript minus signs in "10⁻²" all but vanished at 150 dpi, which
  // turns 10⁻² into 10²). Supersampling gives them their true weight.
  const pg = await doc.getPage(n);
  const vp = pg.getViewport({ scale: S });
  const W = Math.ceil(vp.width),
    H = Math.ceil(vp.height);
  const big = pg.getViewport({ scale: S * SUPERSAMPLE });
  const BW = W * SUPERSAMPLE,
    BH = H * SUPERSAMPLE;
  const { canvas, context } = doc.canvasFactory.create(BW, BH);
  context.fillStyle = "#ffffff";
  context.fillRect(0, 0, BW, BH);
  await pg.render({ canvasContext: context, viewport: big }).promise;
  const src = context.getImageData(0, 0, BW, BH).data as Uint8ClampedArray;
  doc.canvasFactory.destroy({ canvas, context });
  const rgba = new Uint8ClampedArray(W * H * 4);
  const k = SUPERSAMPLE,
    kk = k * k;
  for (let y = 0; y < H; y++)
    for (let x = 0; x < W; x++)
      for (let c = 0; c < 3; c++) {
        // half the block's average + half its darkest sample: a stroke
        // thinner than one output pixel still prints solid (as poppler or a
        // printer would draw it); ordinary text barely changes
        let sum = 0,
          min = 255;
        for (let dy = 0; dy < k; dy++)
          for (let dx = 0; dx < k; dx++) {
            const v = src[((y * k + dy) * BW + x * k + dx) * 4 + c];
            sum += v;
            if (v < min) min = v;
          }
        rgba[(y * W + x) * 4 + c] = (sum / kk + min) / 2;
      }
  for (let i = 3; i < rgba.length; i += 4) rgba[i] = 255;
  return { W, H, rgba };
}

async function openPdf(buf: Buffer): Promise<any> {
  const pdfjs = await loadPdfjs();
  return pdfjs.getDocument({
    data: new Uint8Array(buf),
    verbosity: 0,
    standardFontDataUrl: path.join(PDFJS_DIR, "standard_fonts") + "/",
    cMapUrl: path.join(PDFJS_DIR, "cmaps") + "/",
    cMapPacked: true,
    isEvalSupported: false,
  }).promise;
}

// For checks against another renderer: page n as RGBA at RENDER_DPI.
export async function renderPdfPage(buf: Buffer, n: number) {
  const doc = await openPdf(buf);
  try {
    return await renderPage(doc, n);
  } finally {
    await doc.destroy();
  }
}

function groupLines(items: Item[]): Line[] {
  const sorted = [...items].sort((a, b) => a.base - b.base || a.x - b.x);
  const lines: Line[] = [];
  for (const it of sorted) {
    const l = lines.find((l) => Math.abs(l.base - it.base) <= 2.5 * S);
    if (l) l.items.push(it);
    else lines.push({ items: [it], text: "", x: 0, top: 0, bottom: 0, base: it.base });
  }
  for (const l of lines) {
    l.items.sort((a, b) => a.x - b.x);
    l.text = l.items.map((i) => i.s).join(" ").replace(/\s+/g, " ").trim();
    l.x = l.items[0].x;
    l.top = Math.min(...l.items.map((i) => i.y));
    l.bottom = Math.max(...l.items.map((i) => i.y + i.h));
  }
  return lines.sort((a, b) => a.base - b.base);
}

export async function cutPdf(buf: Buffer, kind: "questions" | "solutions" = "questions"): Promise<CutResult> {
  const doc = await openPdf(buf);
  const warnings: string[] = [];

  try {
    // ---- 1. text layer of every page (cheap) ------------------------------
    const pages: Page[] = [];
    for (let n = 1; n <= doc.numPages; n++) {
      const pg = await doc.getPage(n);
      const vp = pg.getViewport({ scale: S });
      const tc = await pg.getTextContent();
      const items: Item[] = [];
      for (const it of tc.items as any[]) {
        if (!it.str || !it.str.trim()) continue;
        const [a, b, c, d, e, f] = it.transform;
        if (Math.abs(b) > 0.01 || Math.abs(c) > 0.01 || a <= 0) continue; // rotated (watermark) text
        const size = Math.abs(d) * S;
        const base = vp.height - f * S;
        items.push({ s: it.str, x: e * S, y: base - size * 0.8, w: it.width * S, h: size * 1.05, base });
      }
      pages.push({ n, W: Math.ceil(vp.width), H: Math.ceil(vp.height), items, lines: groupLines(items) });
    }

    // ---- 2. furniture: running headers / footers / page numbers ------------
    const norm = (t: string) => t.replace(/\d+/g, "#").replace(/\s+/g, " ").trim().toLowerCase();
    const seen = new Map<string, Set<number>>();
    for (const p of pages)
      for (const l of p.lines) {
        const k = norm(l.text) + "@" + Math.round(l.base / (6 * S));
        if (!seen.has(k)) seen.set(k, new Set());
        seen.get(k)!.add(p.n);
      }
    const furniture = new Map<number, Line[]>();
    for (const p of pages) {
      const f = p.lines.filter((l) => {
        const k = norm(l.text) + "@" + Math.round(l.base / (6 * S));
        const repeated = pages.length >= 2 && seen.get(k)!.size >= Math.max(2, Math.ceil(pages.length * 0.5));
        const pageNo = /^(page\s*)?#(\s*(of|\/)\s*#)?$/i.test(norm(l.text)) && (l.top < p.H * 0.08 || l.bottom > p.H * 0.92);
        return repeated || pageNo;
      });
      furniture.set(p.n, f);
    }

    // The text column (doc-wide): crops use this width so every question
    // picture shows at the same scale.
    const lefts = pages.flatMap((p) => p.items.map((i) => i.x)).sort((a, b) => a - b);
    const rights = pages.flatMap((p) => p.items.map((i) => i.x + i.w)).sort((a, b) => a - b);
    const pct = (arr: number[], q: number) => arr[Math.min(arr.length - 1, Math.max(0, Math.floor(arr.length * q)))] ?? 0;
    // Full extent of the printed text (not a percentile: the longest line
    // must never be clipped). Page furniture is excluded.
    const bodyItems = pages.flatMap((p) => p.items.filter((i) => !(furniture.get(p.n) || []).some((l) => l.items.includes(i))));
    const colX0 = bodyItems.length ? Math.min(...bodyItems.map((i) => i.x)) : pct(lefts, 0.01);
    const colX1 = bodyItems.length ? Math.max(...bodyItems.map((i) => i.x + i.w)) : pct(rights, 0.99);
    // the typical text column, for telling page rules from tables
    const typX0 = pct(lefts, 0.01),
      typX1 = pct(rights, 0.99);

    // Two-column papers (DT-59 BOTANY): almost no text run crosses the middle
    // of the page, and there is a clear gutter between the columns.
    const furnitureItems = new Set<Item>();
    furniture.forEach((ls) => ls.forEach((l) => l.items.forEach((i) => furnitureItems.add(i))));
    // Measured by text width: in single-column papers 25–60% of all printed
    // text runs across the middle; in two-column papers under 3%.
    let total = 0,
      totalW = 0,
      spanW = 0,
      rightSide = 0;
    const leftEnds: number[] = [],
      rightStarts: number[] = [];
    for (const p of pages)
      for (const it of p.items) {
        if (furnitureItems.has(it)) continue;
        total++;
        totalW += it.w;
        const m = p.W / 2;
        if (it.x < m - 5 * S && it.x + it.w > m + 5 * S) spanW += it.w;
        else if (it.x >= m - 10 * S) {
          rightSide++;
          rightStarts.push(it.x / p.W);
        } else leftEnds.push((it.x + it.w) / p.W);
      }
    leftEnds.sort((a, b) => a - b);
    rightStarts.sort((a, b) => a - b);
    const twoCol = total > 20 && rightSide > total * 0.25 && spanW < totalW * 0.1;
    let split = 0.5;
    if (twoCol) {
      const a = pct(leftEnds, 0.98),
        b = pct(rightStarts, 0.02);
      if (a < b) split = (a + b) / 2;
    }
    const frames: Frame[] = [];
    for (const p of pages) {
      if (!twoCol) {
        frames.push({ page: p.n, x0: Math.floor(colX0 - 10), x1: Math.ceil(colX1 + 10), top: 0, lines: p.lines });
        continue;
      }
      const sx = split * p.W;
      // the paper's title block runs across both columns at the top of page 1
      const across = p.items.filter((it) => it.x < sx - 5 * S && it.x + it.w > sx + 5 * S && it.y < p.H * 0.4);
      const top = across.length ? Math.ceil(Math.max(...across.map((it) => it.y + it.h))) + 4 : 0;
      const inLeft = p.items.filter((it) => it.x < sx && !across.includes(it));
      const inRight = p.items.filter((it) => it.x >= sx && !across.includes(it));
      // Each column reaches to its own text's real edge (a long line can run
      // a little past the middle — DT-60 ZOO Q23), never into the other's.
      const leftMax = inLeft.length ? Math.max(...inLeft.map((it) => it.x + it.w)) : sx;
      const rightMin = inRight.length ? Math.min(...inRight.map((it) => it.x)) : sx;
      const lx1 = Math.min(Math.max(sx - 3, leftMax + 3), rightMin - 3);
      const rx0 = Math.max(Math.min(sx + 3, rightMin - 3), lx1);
      frames.push({ page: p.n, x0: Math.floor(colX0 - 10), x1: Math.floor(lx1), top, lines: groupLines(inLeft) });
      frames.push({ page: p.n, x0: Math.ceil(rx0), x1: Math.ceil(colX1 + 10), top, lines: groupLines(inRight) });
    }

    // ---- page rasters, rendered on demand --------------------------------
    const cache = new Map<number, Raster>();
    const order: number[] = [];
    const bounds = new Map<number, { x0: number; x1: number }>();
    const raster = async (n: number): Promise<Raster> => {
      const hit = cache.get(n);
      if (hit) return hit;
      const { W, H, rgba } = await renderPage(doc, n);
      const ink = new Uint8Array(W * H);
      for (let i = 0, j = 0; i < ink.length; i++, j += 4)
        ink[i] = 0.299 * rgba[j] + 0.587 * rgba[j + 1] + 0.114 * rgba[j + 2] < INK ? 1 : 0;
      const r: Raster = { W, H, rgba, ink };
      const blank = (x0: number, y0: number, x1: number, y1: number) => {
        x0 = Math.max(0, Math.floor(x0));
        y0 = Math.max(0, Math.floor(y0));
        x1 = Math.min(W, Math.ceil(x1));
        y1 = Math.min(H, Math.ceil(y1));
        for (let y = y0; y < y1; y++)
          for (let x = x0; x < x1; x++) {
            ink[y * W + x] = 0;
            const j = (y * W + x) * 4;
            rgba[j] = rgba[j + 1] = rgba[j + 2] = 255;
          }
      };
      // Page border / header rules: long straight lines that reach outside
      // the text column on both sides (a table inside a question never does).
      for (let y = 0; y < H; y++) {
        let run = 0,
          best = 0,
          bs = 0,
          s = 0;
        for (let x = 0; x < W; x++) {
          if (ink[y * W + x]) {
            if (!run) s = x;
            run++;
            if (run > best) {
              best = run;
              bs = s;
            }
          } else run = 0;
        }
        if (best > W * 0.5 && bs < typX0 - 8 && bs + best > typX1 + 8) blank(bs, y, bs + best, y + 1);
      }
      let bx0 = 0,
        bx1 = W;
      for (let x = 0; x < W; x++) {
        let c = 0;
        for (let y = 0; y < H; y++) c += ink[y * W + x];
        if (c > H * 0.45 && (x < typX0 - 4 || x > typX1 + 4)) {
          blank(x, 0, x + 1, H);
          if (x < colX0) bx0 = Math.max(bx0, x + 1);
          else bx1 = Math.min(bx1, x);
        }
      }
      if (twoCol) {
        // the rule between the two columns
        const sx = split * W;
        for (let x = Math.max(0, Math.floor(sx - 25 * S)); x < Math.min(W, sx + 25 * S); x++) {
          let c = 0;
          for (let y = 0; y < H; y++) c += ink[y * W + x];
          if (c > H * 0.3) blank(x, 0, x + 1, H);
        }
      }
      bounds.set(n, { x0: bx0, x1: bx1 });
      for (const l of furniture.get(n) || []) blank(l.x - 2, l.top - 3, Math.max(...l.items.map((i) => i.x + i.w)) + 2, l.bottom + 3);
      cache.set(n, r);
      order.push(n);
      while (order.length > PAGE_CACHE) cache.delete(order.shift()!);
      return r;
    };

    // Horizontal crop window: the text column, widened for any print (a
    // diagram) that sticks out beyond it inside the page border.
    const cropX = (fi: number) => {
      const f = frames[fi];
      const b = bounds.get(f.page) || { x0: 0, x1: Infinity };
      return { x0: Math.max(b.x0 + 2, f.x0), x1: Math.min(b.x1 - 2, f.x1) };
    };
    const rowBlank = (r: Raster, y: number, x0: number, x1: number) => {
      const o = y * r.W;
      for (let x = x0; x < x1; x++) if (r.ink[o + x]) return false;
      return true;
    };
    // From row y, walk upward through ink until LINE_GAP blank rows: the
    // returned row is the middle of that gap (the cut line). It never goes
    // above `limit` — the baseline of the previous line of text — so tightly
    // spaced lines (DT-59 Q27's last option sits 13 pt above "28.") are cut
    // in the widest blank strip between them instead of being swallowed.
    const cutAbove = (r: Raster, y: number, x0: number, x1: number, limit: number) => {
      let gap = 0,
        best = 0,
        bestAt = Math.floor(y);
      for (let yy = Math.floor(y); yy > limit; yy--) {
        if (rowBlank(r, yy, x0, x1)) {
          gap++;
          if (gap > best) {
            best = gap;
            bestAt = yy + Math.floor(gap / 2);
          }
          if (gap >= LINE_GAP) return yy + Math.floor(gap / 2);
        } else gap = 0;
      }
      return best ? bestAt : Math.max(limit, Math.floor(y) - 1);
    };
    // Baseline of the nearest full-size text line above `top` in a frame
    // (smaller text — a fraction's numerator, an exponent — belongs to the
    // line it sits on, so it doesn't count).
    const prevBase = (lines: Line[], top: number, floor: number, size: number) => {
      let b = floor;
      for (const l of lines)
        if (l.base < top - 2 && l.base > b && Math.max(...l.items.map((i) => i.h)) >= size * 0.85) b = Math.ceil(l.base);
      return b;
    };
    // Tight ink box of a window, or null when empty.
    const inkBox = (r: Raster, x0: number, y0: number, x1: number, y1: number) => {
      let a = Infinity,
        b = Infinity,
        c = -1,
        d = -1;
      for (let y = Math.max(0, y0); y < Math.min(r.H, y1); y++) {
        const o = y * r.W;
        for (let x = Math.max(0, x0); x < Math.min(r.W, x1); x++)
          if (r.ink[o + x]) {
            if (x < a) a = x;
            if (x > c) c = x;
            if (y < b) b = y;
            if (y > d) d = y;
          }
      }
      return c < 0 ? null : { x0: a, y0: b, x1: c + 1, y1: d + 1 };
    };
    const inkBoxExcept = (r: Raster, x0: number, y0: number, x1: number, y1: number, erase: Box[]) => {
      if (!erase.length) return inkBox(r, x0, y0, x1, y1);
      const saved: [number, number][] = [];
      for (const e of erase)
        for (let y = Math.max(0, Math.floor(e.y0)); y < Math.min(r.H, Math.ceil(e.y1)); y++)
          for (let x = Math.max(0, Math.floor(e.x0)); x < Math.min(r.W, Math.ceil(e.x1)); x++)
            if (r.ink[y * r.W + x]) {
              saved.push([y * r.W + x, 1]);
              r.ink[y * r.W + x] = 0;
            }
      const b = inkBox(r, x0, y0, x1, y1);
      for (const [o] of saved) r.ink[o] = 1;
      return b;
    };
    const { createCanvas } = await import("@napi-rs/canvas");
    // Every picture cut for the current question, in page pixels — read back
    // by the self-check below.
    let cuts: { fi: number; box: Box; erased?: boolean }[] = [];
    const png = (r: Raster, fi: number, x0: number, y0: number, x1: number, y1: number, erase: Box[] = []) => {
      x0 = Math.max(0, Math.floor(x0));
      y0 = Math.max(0, Math.floor(y0));
      x1 = Math.min(r.W, Math.ceil(x1));
      y1 = Math.min(r.H, Math.ceil(y1));
      cuts.push({ fi, box: { x0, y0, x1, y1 } });
      // erased on purpose (option labels, the question number): accounted for
      for (const e of erase) cuts.push({ fi, box: e, erased: true });
      const w = x1 - x0,
        h = y1 - y0;
      const c = createCanvas(w, h);
      const ctx = c.getContext("2d");
      const img = ctx.createImageData(w, h);
      for (let y = 0; y < h; y++) img.data.set(r.rgba.subarray(((y0 + y) * r.W + x0) * 4, ((y0 + y) * r.W + x1) * 4), y * w * 4);
      ctx.putImageData(img, 0, 0);
      ctx.fillStyle = "#ffffff";
      for (const e of erase) ctx.fillRect(e.x0 - x0, e.y0 - y0, e.x1 - e.x0, e.y1 - e.y0);
      return c.toBuffer("image/png");
    };

    // ---- 3. question numbers ---------------------------------------------
    type Cand = { num: number; fi: number; item: Item; line: Line; rel: number };
    const cands: Cand[] = [];
    const NUM = /^(?:Q\.?\s*)?(\d{1,3})\s*[.):]?(?:\s|$)/i;
    // left edge of the text in each column (doc-wide), so a number's margin
    // compares across columns
    const sideOf = (fi: number) => (twoCol ? fi % 2 : 0);
    const sideLeft = [0, 1].map((side) =>
      pct(
        frames.flatMap((f, fi) => (sideOf(fi) === side ? f.lines.map((l) => l.x) : [])).sort((a, b) => a - b),
        0.01
      )
    );
    for (let fi = 0; fi < frames.length; fi++)
      for (const l of frames[fi].lines) {
        const it = l.items[0];
        const m = it.s.trim().match(NUM);
        if (!m) continue;
        // "12." / "Q12" / "12" alone — never an option label like "1)" or "(1)".
        if (/^\(/.test(it.s.trim()) || /^\d{1,3}\s*\)/.test(it.s.trim())) continue;
        cands.push({ num: parseInt(m[1], 10), fi, item: it, line: l, rel: it.x - sideLeft[sideOf(fi)] });
      }
    // Pick the left margin whose numbers form the longest 1, 2, 3 … run.
    // Each column picks its own margin (combinations tried for two-column
    // papers, where left and right margins can differ by a few points).
    const keysOf = (side: number) => Array.from(new Set(cands.filter((c) => sideOf(c.fi) === side).map((c) => Math.round(c.rel / (4 * S)))));
    const combos: number[][] = twoCol ? keysOf(0).flatMap((l) => keysOf(1).map((r) => [l, r])) : keysOf(0).map((k) => [k]);
    let anchors: Cand[] = [];
    for (const combo of combos) {
      // ±8 pt: two-digit numbers are often right-aligned a few points left of
      // one-digit ones; numbered statements inside a question sit ≥12 pt in.
      const col = cands.filter((c) => Math.abs(c.rel / (4 * S) - combo[sideOf(c.fi)]) <= 2);
      const run: Cand[] = [];
      let want = 1;
      for (const c of col)
        if (c.num === want) {
          run.push(c);
          want++;
        } else if (c.num > want && c.num <= want + 2 && run.length) {
          // tolerate one or two numbers the text layer lost
          run.push(c);
          want = c.num + 1;
        }
      if (run.length > anchors.length || (run.length === anchors.length && run.length && run[0].rel < anchors[0].rel))
        anchors = run;
    }
    if (process.env.PDFPIC_DEBUG)
      console.error("frames", frames.length, "twoCol", twoCol, "split", split.toFixed(3), "cands", cands.map((c) => `${c.num}@f${c.fi}:${(c.rel / S).toFixed(0)}`).join(" "));
    const missing: number[] = [];
    anchors.forEach((a, i) => {
      const prev = i ? anchors[i - 1].num : 0;
      for (let k = prev + 1; k < a.num; k++) missing.push(k);
    });
    if (!anchors.length) {
      warnings.push("No numbered questions (1., 2., 3. …) were found in this PDF.");
      return { questions: [], missing, pages: pages.length, warnings };
    }
    if (twoCol) warnings.push("Two-column paper: questions were read left column first, then right.");

    const HEADING = /\bSECTION\b|^(PART|PHYSICS|CHEMISTRY|MATHEMATICS|MATHS|BIOLOGY|BOTANY|ZOOLOGY)\b|^\W*END\b|ALL THE BEST/i;
    const isHeading = (l: Line) => HEADING.test(l.text) && l.text === l.text.toUpperCase() && !/^\(?\d/.test(l.text);

    // ---- 4. regions ---------------------------------------------------------
    const questions: CutQuestion[] = [];
    // Cut line above each anchor (computed once; it is also the previous
    // question's bottom).
    const startCut: number[] = [];
    for (const a of anchors) {
      const r = await raster(frames[a.fi].page);
      const { x0, x1 } = cropX(a.fi);
      const topOfLine = Math.min(a.item.y, ...a.line.items.map((i) => i.y));
      const limit = prevBase(frames[a.fi].lines, topOfLine, frames[a.fi].top, a.item.h);
      startCut.push(cutAbove(r, topOfLine, x0, x1, limit));
    }
    // Top of printable content in a frame a question continues into.
    const contentTop = async (fi: number) => {
      const r = await raster(frames[fi].page);
      const { x0, x1 } = cropX(fi);
      const b = inkBox(r, x0, frames[fi].top, x1, r.H);
      return b ? Math.max(frames[fi].top, b.y0 - PAD) : frames[fi].top;
    };

    // Option labels: "1)", "(1)", "a)", "(a)", "A)", "(A)" — at the start of a
    // text run, or inside one after a gap ("1) 0.58  2) 0.67").
    const splitOptions = async (
      pieces: Piece[],
      regionLines: { piece: number; line: Line }[]
    ): Promise<{ ok: true; piece: number; stemEnd: number; options: Buffer[] } | { ok: false; why: string }> => {
      type Lab = { k: number; style: string; piece: number; x: number; end: number; top: number; base: number; h: number; lineStart: boolean; merged: boolean };
      const labs: Lab[] = [];
      const LAB = /(^|\s)(\(?)([1-4]|[a-d]|[A-D])\)(?=\s|$)/g;
      for (const { piece, line } of regionLines)
        for (let j = 0; j < line.items.length; j++) {
          const it = line.items[j];
          const prev = j ? line.items[j - 1] : null;
          for (const m of Array.from(it.s.matchAll(LAB))) {
            const idx = (m.index ?? 0) + m[1].length;
            const lineStart = !prev && !it.s.slice(0, idx).trim();
            const len = m[2].length + 2;
            const ch = m[3];
            const k = /\d/.test(ch) ? +ch : ch.toLowerCase().charCodeAt(0) - 96;
            const style = (m[2] ? "(" : "") + (/\d/.test(ch) ? "1" : /[a-d]/.test(ch) ? "a" : "A");
            const per = it.w / Math.max(1, it.s.length);
            labs.push({ k, style, piece, x: it.x + per * idx, end: it.x + per * (idx + len), top: it.y, base: it.base, h: it.h, lineStart, merged: idx > 0 });
          }
        }
      // Visual rows: labels within ~0.7 of a line height of each other are on
      // one row even when tall maths shifts their baselines apart (DT-PHY
      // Q12: "3) 4)" sit higher than "1) 2)" next to square roots).
      labs.sort((p, q) => p.piece - q.piece || p.base - q.base);
      const rowOf: number[] = [];
      let rowId = -1,
        rowBase = -Infinity,
        rowPiece = -1;
      for (const l of labs) {
        if (l.piece !== rowPiece || l.base - rowBase > l.h * 0.7) {
          rowId++;
          rowBase = l.base;
          rowPiece = l.piece;
        }
        rowOf.push(rowId);
      }
      const ordered = labs.map((l, i) => ({ l, row: rowOf[i] })).sort((p, q) => p.row - q.row || p.l.x - q.l.x);
      // the last complete 1-2-3-4 run of one style, in reading order
      let run: { l: Lab; row: number }[] | null = null;
      for (const style of Array.from(new Set(labs.map((l) => l.style)))) {
        const ls = ordered.filter((o) => o.l.style === style);
        for (let s = 0; s < ls.length; s++) {
          if (ls[s].l.k !== 1) continue;
          const seq = [ls[s]];
          for (let t = s + 1; t < ls.length && seq.length < 4; t++) if (ls[t].l.k === seq.length + 1) seq.push(ls[t]);
          // Every row of options begins with a label, so the first label of
          // each row must be the first thing on its line — "(A)" inside
          // "1) Only (A) and (B)" is option text, not a label.
          const rowsOk =
            seq.length === 4 &&
            Array.from(new Set(seq.map((o) => o.row))).every((rid) => {
              const first = seq.filter((o) => o.row === rid).sort((p, q) => p.l.x - q.l.x)[0];
              return first.l.lineStart;
            });
          if (rowsOk && (!run || seq[0].row >= run[0].row)) run = seq;
        }
      }
      if (!labs.length) return { ok: false, why: "no options printed (numerical-answer question)" };
      if (!run) return { ok: false, why: "option labels 1)–4) not found" };
      if (new Set(run.map((o) => o.l.piece)).size !== 1) return { ok: false, why: "options continue on the next page/column" };
      const pc = pieces[run[0].l.piece];
      const r = await raster(frames[pc.fi].page);
      const { x0: cx0, x1: cx1 } = cropX(pc.fi);
      // rows of labels
      const rows: Lab[][] = [];
      for (const rid of Array.from(new Set(run.map((o) => o.row))).sort((p, q) => p - q))
        rows.push(run.filter((o) => o.row === rid).map((o) => o.l).sort((p, q) => p.x - q.x));
      // A label inside a merged text run ("2) 3.4 nm 3) 3.4 Å 4) 20 nm") has
      // only an estimated position (characters ≠ printed widths): snap it to
      // where the print really starts — the nearest point after a blank gap.
      for (const l of rows.flat()) {
        if (!l.merged) continue;
        const band = (xx: number) => {
          for (let y = Math.floor(l.top); y < Math.ceil(l.top + l.h); y++) if (r.ink[y * r.W + xx]) return true;
          return false;
        };
        const est = Math.round(l.x);
        let bestX = -1;
        for (let d = 0; d <= 25 * (S / 2); d++)
          for (const xx of [est - d, est + d]) {
            if (bestX < 0 && xx > 5 && xx < r.W && band(xx) && ![1, 2, 3, 4, 5].some((g) => band(xx - g))) bestX = xx;
          }
        if (bestX >= 0) {
          const w = l.end - l.x;
          l.x = bestX;
          l.end = bestX + w;
        }
      }
      const flat = rows.flat().map((l) => l.k);
      if (flat.join("") !== "1234") return { ok: false, why: "option labels are not in 1-2-3-4 order" };
      const rowTop: number[] = [];
      for (let ri = 0; ri < rows.length; ri++) {
        const top = Math.min(...rows[ri].map((l) => l.top));
        // never above the nearest text line over this row — for row 2+ that
        // may be the wrapped second line of the option above
        const limit = prevBase(
          frames[pc.fi].lines,
          top,
          ri === 0 ? pc.y0 : Math.floor(Math.max(...rows[ri - 1].map((l) => l.base))),
          rows[ri][0].h
        );
        rowTop.push(cutAbove(r, top, cx0, cx1, limit));
      }
      if (rowTop[0] <= pc.y0 + 1) return { ok: false, why: "no gap between question and options" };
      const options: Buffer[] = new Array(4);
      for (let ri = 0; ri < rows.length; ri++) {
        const y0 = rowTop[ri];
        const y1 = ri + 1 < rows.length ? rowTop[ri + 1] : pc.y1;
        for (let li = 0; li < rows[ri].length; li++) {
          const l = rows[ri][li];
          // start right after the label "1)" (its measured width), finishing
          // any glyph the estimate lands inside
          const colInk = (xx: number) => {
            for (let y = Math.floor(l.top); y < Math.ceil(l.top + l.h); y++) if (r.ink[y * r.W + xx]) return true;
            return false;
          };
          let x = Math.ceil(l.end);
          for (let g = 0; g < 6 && x < cx1 && colInk(x); g++) x++;
          const xEnd = li + 1 < rows[ri].length ? Math.floor(rows[ri][li + 1].x) - 3 : cx1;
          // The option's box starts at its label's left edge (a wrapped second
          // line of option text starts there, under the label) and the label
          // itself is erased from the picture.
          const xStart = Math.max(cx0, li ? Math.floor(rows[ri][li - 1].x) + 1 : cx0, Math.floor(l.x) - 3);
          const labelBox: Box = { x0: xStart, y0: Math.floor(l.top) - 1, x1: x - 1, y1: Math.ceil(l.top + l.h) };
          const b = inkBoxExcept(r, xStart, y0, xEnd, y1, [labelBox]);
          if (process.env.PDFPIC_DEBUG && (!b || xEnd - x < 4))
            console.error("empty option", l, { x, xEnd, y0, y1, cx0, cx1, rowTop, piece: pc });
          if (!b || xEnd - x < 4) return { ok: false, why: `option ${l.k} looks empty` };
          options[l.k - 1] = png(r, pc.fi, Math.max(xStart, b.x0 - PAD), b.y0 - PAD, Math.min(xEnd, b.x1 + PAD), b.y1 + PAD, [labelBox]);
        }
      }
      return { ok: true, piece: run[0].l.piece, stemEnd: rowTop[0], options };
    };

    // Self-check of one question's pictures (in `cuts`):
    //  1. nothing printed is lost — every ink pixel of the question's area
    //     lies inside one of its pictures;
    //  2. no text is cut through — every text run lies wholly inside one
    //     picture (option labels and the question number are erased on
    //     purpose and skipped).
    const selfCheck = async (pieces: Piece[], regionLines: { piece: number; line: Line }[], skip: Set<Item>): Promise<string> => {
      const inside = (fi: number, x: number, y: number) =>
        cuts.some((c) => c.fi === fi && x >= c.box.x0 && x < c.box.x1 && y >= c.box.y0 && y < c.box.y1);
      let lost = 0;
      for (const pc of pieces) {
        const r = await raster(frames[pc.fi].page);
        const { x0, x1 } = cropX(pc.fi);
        for (let y = Math.max(0, pc.y0); y < Math.min(r.H, pc.y1); y++)
          for (let x = x0; x < x1; x++) if (r.ink[y * r.W + x] && !inside(pc.fi, x, y)) lost++;
      }
      if (lost > 12 && process.env.PDFPIC_DEBUG_DIR) {
        // debug overlay: region with pictures outlined, lost print in red
        for (const pc of pieces) {
          const r = await raster(frames[pc.fi].page);
          const { x0, x1 } = cropX(pc.fi);
          const w = x1 - x0,
            h = pc.y1 - pc.y0;
          if (w <= 0 || h <= 0) continue;
          const c = createCanvas(w, h);
          const ctx = c.getContext("2d");
          const img = ctx.createImageData(w, h);
          for (let y = 0; y < h; y++)
            for (let x = 0; x < w; x++) {
              const o = ((pc.y0 + y) * r.W + x0 + x) * 4,
                q = (y * w + x) * 4;
              const lostPx = r.ink[(pc.y0 + y) * r.W + x0 + x] && !inside(pc.fi, x0 + x, pc.y0 + y);
              img.data[q] = lostPx ? 255 : r.rgba[o];
              img.data[q + 1] = lostPx ? 0 : r.rgba[o + 1];
              img.data[q + 2] = lostPx ? 0 : r.rgba[o + 2];
              img.data[q + 3] = 255;
            }
          ctx.putImageData(img, 0, 0);
          ctx.strokeStyle = "#0a0";
          for (const cc of cuts) if (cc.fi === pc.fi) ctx.strokeRect(cc.box.x0 - x0, cc.box.y0 - pc.y0, cc.box.x1 - cc.box.x0, cc.box.y1 - cc.box.y0);
          fs.mkdirSync(process.env.PDFPIC_DEBUG_DIR, { recursive: true });
          fs.writeFileSync(path.join(process.env.PDFPIC_DEBUG_DIR, `lost-f${pc.fi}-y${pc.y0}.png`), c.toBuffer("image/png"));
        }
      }
      if (lost > 12) return `${lost} printed pixels fell outside the pictures`;
      // a text run is cut through when no single picture holds all its print
      const LABEL = /\s\(?([1-4]|[a-dA-D])\)\s/;
      for (const { piece, line } of regionLines)
        for (const it of line.items) {
          if (skip.has(it) || LABEL.test(` ${it.s} `)) continue;
          const fi = pieces[piece].fi;
          const r = await raster(frames[fi].page);
          const size = it.h / 1.05;
          const x0 = Math.max(0, Math.ceil(it.x + 1)),
            x1 = Math.min(r.W, Math.floor(it.x + it.w - 1)),
            y0 = Math.max(0, Math.ceil(it.base - size * 0.75)),
            y1 = Math.min(r.H, Math.floor(it.base + size * 0.2));
          const holders = cuts.filter((c) => c.fi === fi && !c.erased && c.box.x1 > x0 && c.box.x0 < x1 && c.box.y1 > y0 && c.box.y0 < y1);
          let ok = true;
          const pix: number[] = [];
          for (let y = y0; y < y1; y++) for (let x = x0; x < x1; x++) if (r.ink[y * r.W + x]) pix.push(x, y);
          if (!pix.length) continue;
          ok = holders.some((c) => {
            for (let k = 0; k < pix.length; k += 2) {
              const x = pix[k],
                y = pix[k + 1];
              if (x < c.box.x0 || x >= c.box.x1 || y < c.box.y0 || y >= c.box.y1) return false;
            }
            return true;
          });
          if (!ok) return `text "${it.s.trim().slice(0, 30)}" would be cut between two pictures`;
        }
      return "";
    };

    for (let i = 0; i < anchors.length; i++) {
      const a = anchors[i];
      const next = anchors[i + 1];
      const pieces: Piece[] = [];
      const lastFrame = next ? next.fi : frames.length - 1;
      for (let fi = a.fi; fi <= lastFrame; fi++) {
        const r = await raster(frames[fi].page);
        const y0 = fi === a.fi ? startCut[i] : await contentTop(fi);
        let y1 = next && fi === next.fi ? startCut[i + 1] : r.H;
        // stop at a section heading between this question and the next
        const h = frames[fi].lines.find(
          (l) => l.top > (fi === a.fi ? y0 + 4 : y0 - 4) && l.bottom < y1 && isHeading(l) && !(fi === a.fi && l === a.line)
        );
        if (h) {
          const { x0, x1 } = cropX(fi);
          y1 = cutAbove(r, h.top, x0, x1, y0);
          pieces.push({ fi, y0, y1 });
          break;
        }
        if (y1 - y0 > 2) pieces.push({ fi, y0, y1 });
      }

      // text of the region, reading order
      const regionLines: { piece: number; line: Line }[] = [];
      pieces.forEach((pc, k) => {
        for (const l of frames[pc.fi].lines) if (l.base > pc.y0 && l.base <= pc.y1) regionLines.push({ piece: k, line: l });
      });
      const text = regionLines.map((x) => x.line.text).join("\n");

      // erase the printed question number ("12.") from the picture
      const numBox = { x0: a.item.x - 2, y0: a.item.y - 2, x1: a.item.x + a.item.w + 2, y1: a.item.y + a.item.h + 2 };
      if (!/^(?:Q\.?\s*)?\d{1,3}\s*[.):]?$/i.test(a.item.s.trim())) {
        // number merged with the first words in one text run: erase only the
        // number's share of the run's width
        const m = a.item.s.match(/^\s*(?:Q\.?\s*)?\d{1,3}\s*[.):]?/i)!;
        numBox.x1 = a.item.x + (a.item.w * m[0].trimEnd().length) / a.item.s.length + 1;
      }

      const stemPics = async (upto: { piece: number; y: number } | null) => {
        const out: Buffer[] = [];
        for (let k = 0; k < pieces.length; k++) {
          const pc = pieces[k];
          if (upto && k > upto.piece) break;
          const r = await raster(frames[pc.fi].page);
          const { x0, x1 } = cropX(pc.fi);
          const yEnd = upto && k === upto.piece ? upto.y : pc.y1;
          const erase = k === 0 && pc.fi === a.fi ? [numBox] : [];
          // find ink, ignoring the number we are about to erase
          const b = inkBoxExcept(r, x0, pc.y0, x1, yEnd, erase);
          if (!b) continue;
          out.push(png(r, pc.fi, x0, Math.max(pc.y0, b.y0 - PAD), x1, Math.min(yEnd, b.y1 + PAD), erase));
        }
        return out;
      };

      const skip = new Set<Item>([a.item]);
      const whole = async (note?: string) => {
        cuts = [];
        const stem = await stemPics(null);
        const bad = await selfCheck(pieces, regionLines, skip);
        if (bad) warnings.push(`Q${a.num}: ${bad} — check this question against the paper.`);
        questions.push({ num: a.num, stem, options: null, text, note });
      };

      if (kind === "solutions") {
        await whole();
        continue;
      }

      // ---- 5. options ---------------------------------------------------------
      cuts = [];
      const split = await splitOptions(pieces, regionLines);
      if (!split.ok) {
        await whole(split.why);
        continue;
      }
      const stem = await stemPics({ piece: split.piece, y: split.stemEnd });
      const bad = await selfCheck(pieces, regionLines, skip);
      // Anything doubtful: keep the whole question (options included) as one
      // picture rather than risk a wrong split.
      if (bad) await whole(`auto-check: ${bad}`);
      else questions.push({ num: a.num, stem, options: split.options, text });
    }

    return { questions, missing, pages: pages.length, warnings };
  } finally {
    await doc.destroy();
  }
}
