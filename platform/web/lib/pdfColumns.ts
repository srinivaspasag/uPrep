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

type Item = { x: number; y: number; w: number; s: string };

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

export function pageText(rawItems: any[], pageWidth: number): string {
  const items: Item[] = rawItems
    .filter((i) => typeof i?.str === "string" && i.str.length)
    .map((i) => ({ x: i.transform[4], y: i.transform[5], w: i.width || 0, s: i.str }));
  // Group into lines (same baseline within 3pt), top of page first.
  items.sort((a, b) => b.y - a.y || a.x - b.x);
  const lines: { y: number; items: Item[] }[] = [];
  for (const it of items) {
    const line = lines.find((l) => Math.abs(l.y - it.y) <= 3);
    if (line) line.items.push(it);
    else lines.push({ y: it.y, items: [it] });
  }
  lines.sort((a, b) => b.y - a.y);
  for (const l of lines) l.items.sort((a, b) => a.x - b.x);

  const gutter = pageWidth * 0.49;
  const split = lines.map((l) => {
    // Cut at the first item that starts past the gutter after a visible gap.
    let k = l.items.findIndex((it, i) => it.x >= gutter && (i === 0 || l.items[i - 1].x + l.items[i - 1].w <= it.x - 2));
    if (k === -1) k = l.items.length;
    return { y: l.y, left: joinItems(l.items.slice(0, k)), right: joinItems(l.items.slice(k)), all: joinItems(l.items) };
  });

  const twoColumn = split.some((l) => QNUM.test(l.right));
  if (!twoColumn) return split.map((l) => l.all).join("\n");

  // Title block: everything above the first line that starts a question in
  // either column is read as whole lines.
  const firstQ = split.findIndex((l) => QNUM.test(l.left) || QNUM.test(l.right));
  const head = firstQ > 0 ? split.slice(0, firstQ) : [];
  const body = firstQ > 0 ? split.slice(firstQ) : split;
  return [
    ...head.map((l) => l.all),
    ...body.map((l) => l.left).filter(Boolean),
    ...body.map((l) => l.right).filter(Boolean),
  ].join("\n");
}

// pdf-parse `pagerender` hook.
export async function renderPageColumns(pageData: any): Promise<string> {
  const viewport = pageData.getViewport(1);
  const tc = await pageData.getTextContent({ normalizeWhitespace: false, disableCombineTextItems: false });
  return pageText(tc.items || [], viewport.width);
}
