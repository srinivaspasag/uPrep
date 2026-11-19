// Column-aware text for exam-paper PDFs (used by the CMDS Bulk Import
// extractor when no AI transcription key is configured).
//
// pdf-parse's default rendering emits text in content-stream order, which for
// two-column papers interleaves the columns — e.g. DT-60's Q1 options came out
// mixed with Q5's ("1) Marsupial rat… 1) Tendrils of Cucurbita…"). Here each
// page's text items are grouped into lines by y, every line is cut at the
// page's middle gutter, and the left column is read top-to-bottom before the
// right one. A page is only treated as two-column if its right half contains
// a question number ("12."), so single-column pages with a 2x2 option grid are
// left alone. Lines above the first question (title block) stay full-width.
//
// Pictures (tables pasted as images, diagrams) are placed into the same flow
// as their own line at their top edge, so they land inside the question or
// option they belong to. They are passed in already turned into text tokens
// (see lib/pdfExtract) — this file only decides where they go.

type Item = { x: number; y: number; w: number; s: string };
export type PlacedImage = { x: number; yTop: number; w: number; token: string };

function joinItems(items: Item[]): string {
  let out = "";
  let end = -Infinity;
  for (const it of items) {
    if (out && it.x - end > 1.5 && !/\s$/.test(out) && !/^\s/.test(it.s)) out += " ";
    out += it.s;
    end = it.x + it.w;
  }
  return out.replace(/\s+/g, " ").trim();
}

const QNUM = /^\d{1,3}\.(\s|$)/;

export function pageText(rawItems: any[], pageWidth: number, images: PlacedImage[] = []): string {
  const items: Item[] = rawItems
    .filter((i) => typeof i?.str === "string" && i.str.length)
    // Rotated text is a watermark (e.g. the diagonal institute name on
    // solution sheets), never question content — its letters would otherwise
    // land one by one in the middle of answers.
    .filter((i) => Math.abs(i.transform?.[1] || 0) < 0.01 && Math.abs(i.transform?.[2] || 0) < 0.01)
    .map((i) => ({ x: i.transform[4], y: i.transform[5], w: i.width || 0, s: i.str }));
  // Group into lines (same baseline within 3pt), top of page first.
  items.sort((a, b) => b.y - a.y || a.x - b.x);
  const lines: { y: number; items: Item[]; image?: boolean }[] = [];
  for (const it of items) {
    const line = lines.find((l) => !l.image && Math.abs(l.y - it.y) <= 3);
    if (line) line.items.push(it);
    else lines.push({ y: it.y, items: [it] });
  }
  // Each picture is a line of its own, never merged with text beside it.
  for (const im of images) lines.push({ y: im.yTop, items: [{ x: im.x, y: im.yTop, w: im.w, s: im.token }], image: true });
  lines.sort((a, b) => b.y - a.y);
  for (const l of lines) l.items.sort((a, b) => a.x - b.x);

  const gutter = pageWidth * 0.49;

  // Picture options captioned underneath: a row of pictures followed by a
  // row holding only the labels ("1)   2)"), e.g. DT-59 Q10's four line
  // diagrams. Re-pair each label with the picture above it (nearest by x),
  // so the text reads "1) <pic> 2) <pic>" like any other options.
  const LABEL = /^\(?([1-4a-dA-D])\)$/;
  const sideOf = (x: number) => (x >= gutter - 6 ? 1 : 0);
  const dropped = new Set<number>();
  for (let li = 0; li < lines.length; li++) {
    const L = lines[li];
    if (L.image) continue;
    for (const side of [0, 1]) {
      const mine = L.items.filter((it) => sideOf(it.x) === side);
      const marks = mine.filter((it) => it.s.trim());
      if (!marks.length || !marks.every((it) => LABEL.test(it.s.trim()))) continue;
      const pics: number[] = [];
      for (let k = li - 1; k >= 0; k--) {
        const P = lines[k];
        if (dropped.has(k)) continue;
        const onSide = P.items.some((it) => sideOf(it.x) === side);
        if (!onSide) continue;
        if (!P.image || P.y - L.y > 260) break;
        pics.push(k);
      }
      if (pics.length !== marks.length) continue;
      const byX = pics.map((k) => lines[k].items[0]).sort((a, b) => a.x - b.x);
      const sortedMarks = [...marks].sort((a, b) => a.x - b.x);
      const merged: Item[] = [];
      sortedMarks.forEach((m, i) => {
        merged.push(m);
        merged.push({ x: m.x + m.w + 3, y: m.y, w: 1, s: byX[i].s });
      });
      L.items = [...L.items.filter((it) => sideOf(it.x) !== side), ...merged].sort((a, b) => a.x - b.x);
      pics.forEach((k) => dropped.add(k));
    }
  }
  for (const k of Array.from(dropped).sort((a, b) => b - a)) lines.splice(k, 1);
  const split = lines.map((l) => {
    if (l.image) {
      const it = l.items[0];
      const right = it.x >= gutter - 6;
      return { y: l.y, left: right ? "" : it.s, right: right ? it.s : "", all: it.s, image: true };
    }
    // Cut at the first item that starts past the gutter after a visible gap.
    let k = l.items.findIndex((it, i) => it.x >= gutter && (i === 0 || l.items[i - 1].x + l.items[i - 1].w <= it.x - 2));
    if (k === -1) k = l.items.length;
    return { y: l.y, left: joinItems(l.items.slice(0, k)), right: joinItems(l.items.slice(k)), all: joinItems(l.items), image: false };
  });

  const twoColumn = split.some((l) => !l.image && QNUM.test(l.right));
  if (!twoColumn) return split.map((l) => l.all).join("\n");

  // Title block: everything above the first line that starts a question in
  // either column is read as whole lines.
  const firstQ = split.findIndex((l) => !l.image && (QNUM.test(l.left) || QNUM.test(l.right)));
  const head = firstQ > 0 ? split.slice(0, firstQ) : [];
  const body = firstQ > 0 ? split.slice(firstQ) : split;
  return [
    ...head.map((l) => l.all),
    ...body.map((l) => l.left).filter(Boolean),
    ...body.map((l) => l.right).filter(Boolean),
  ].join("\n");
}

// pdf-parse `pagerender` hook (text only).
export async function renderPageColumns(pageData: any): Promise<string> {
  const viewport = pageData.getViewport(1);
  const tc = await pageData.getTextContent({ normalizeWhitespace: false, disableCombineTextItems: false });
  return pageText(tc.items || [], viewport.width);
}
