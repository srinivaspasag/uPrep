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

type Item = { x: number; y: number; w: number; s: string; size?: number; script?: "sup" | "sub"; onBase?: boolean };
export type PlacedImage = { x: number; yTop: number; w: number; token: string };

// Superscripts / subscripts (10⁻¹⁰, A₃, Fe³⁺) are printed in a smaller font
// slightly above or below the line. They are written with the Unicode
// superscript/subscript characters so they read right everywhere a question
// is shown — CMDS editor, web test page and the Android app, which has no
// math renderer. Letters Unicode has no small form for (the c of Kc) stay
// plain letters, as before; any other power is written ^(…) — K₂^(1/2), e^(-αt)
// — which reads the same on every screen.
const SUP: Record<string, string> = {
  "0": "⁰", "1": "¹", "2": "²", "3": "³", "4": "⁴", "5": "⁵", "6": "⁶", "7": "⁷", "8": "⁸", "9": "⁹",
  "+": "⁺", "-": "⁻", "−": "⁻", "–": "⁻", "=": "⁼", "(": "⁽", ")": "⁾", n: "ⁿ", i: "ⁱ", x: "ˣ", y: "ʸ",
  "′": "′", "'": "′", "″": "″", "*": "*", "∗": "*", " ": "",
};
const SUB: Record<string, string> = {
  "0": "₀", "1": "₁", "2": "₂", "3": "₃", "4": "₄", "5": "₅", "6": "₆", "7": "₇", "8": "₈", "9": "₉",
  "+": "₊", "-": "₋", "−": "₋", "–": "₋", "=": "₌", "(": "₍", ")": "₎",
  a: "ₐ", e: "ₑ", o: "ₒ", x: "ₓ", h: "ₕ", k: "ₖ", l: "ₗ", m: "ₘ", n: "ₙ", p: "ₚ", s: "ₛ", t: "ₜ", " ": "",
};
const FRAC: Record<string, string> = {
  "1/2": "½", "1/3": "⅓", "2/3": "⅔", "1/4": "¼", "3/4": "¾", "1/5": "⅕", "1/6": "⅙", "1/8": "⅛", "3/8": "⅜",
};
function scriptText(kind: "sup" | "sub", s: string): string {
  // A raised small o / ring after a number is the degree sign (25°C).
  if (kind === "sup" && /^\s*[o∘°]\s*$/.test(s)) return "°";
  const map = kind === "sup" ? SUP : SUB;
  const chars = Array.from(s);
  if (chars.every((c) => c in map)) return chars.map((c) => map[c]).join("");
  // A subscript Unicode can't show stays plain text, as it always was (μ0ε0).
  if (kind === "sub" || /^[A-Za-zÀ-ÖØ-öø-ɏͰ-Ͽℓ¹²³⁰⁴-⁹₀-₉\s]+$/.test(s)) return s.trim();
  // A real power Unicode can't show (e^(-αt), r^(3/2)); anything longer is
  // the top of a display fraction and stays plain text.
  if (/^[-−]?[A-Za-zÀ-ÖØ-öø-ɏͰ-Ͽℓ¹²³⁰⁴-⁹₀-₉0-9]{1,3}(\/[0-9])?$/.test(s.trim())) return `^(${s.trim().replace(/[−–]/g, "-")})`;
  return s.trim();
}

function joinItems(items: Item[]): string {
  let out = "";
  let end = -Infinity;
  let afterPower = false; // the last thing written was a plain superscript
  for (let i = 0; i < items.length; i++) {
    const it = items[i];
    if (it.script) {
      // The whole cluster of small pieces ("−", "1", "0") belongs to the
      // character before it — no space.
      let j = i;
      while (j + 1 < items.length && items[j + 1].script) j++;
      const cluster = items.slice(i, j + 1);
      const sup = cluster.filter((x) => x.script === "sup");
      const sub = cluster.filter((x) => x.script === "sub");
      const text = (xs: Item[]) => xs.map((x) => x.s).join("");
      const lo = (xs: Item[]) => Math.min(...xs.map((x) => x.x));
      const hi = (xs: Item[]) => Math.max(...xs.map((x) => x.x + x.w));
      const stacked = sup.length > 0 && sub.length > 0 && lo(sup) < hi(sub) - 0.5 && lo(sub) < hi(sup) - 0.5;
      const gap = it.x - end;
      if (stacked && !(gap <= 1.2 && /[A-Za-zÀ-ÖØ-öø-ɏͰ-Ͽℓ¹²³⁰⁴-⁹₀-₉0-9)\]]$/.test(out))) {
        // Number over number, not touching a symbol before it: a stacked
        // fraction (the ½ of "½N₂ + 3/2H₂ ⇌ NH₃").
        // A ring over a small A is the ångström sign (3.4Å).
        const f = `${text(sup).trim()}/${text(sub).trim()}`.replace(/A\s*[∘˚°]|[∘˚°]\s*A/g, "Å");
        if (out && gap > 1.5 && !/\s$/.test(out)) out += " ";
        out += FRAC[f] || f;
      } else if (stacked) {
        // Both on one symbol (AB₂⁻, K₁²): subscript first, as chemistry writes it.
        out += scriptText("sub", text(sub)) + scriptText("sup", text(sup));
      } else {
        for (let k = 0; k < cluster.length; ) {
          let m = k;
          while (m + 1 < cluster.length && cluster[m + 1].script === cluster[k].script) m++;
          out += scriptText(cluster[k].script!, text(cluster.slice(k, m + 1)));
          k = m + 1;
        }
      }
      end = hi(cluster);
      // A fraction or degree sign is followed directly by what it qualifies
      // (½N₂, 25°C); a power or ion charge often by a word (AB₂⁻ are).
      afterPower = /[⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁾ⁿⁱˣʸ′″)]$/.test(out) && (!stacked || /[⁺⁻]$/.test(out));
      i = j;
      continue;
    }
    // A real word gap is about a quarter of the font size; smaller gaps are
    // kerning inside a word the PDF stored in pieces ("L ytic", "Or ganic").
    // Right after a power the gap is measured from its small raised glyphs,
    // so a smaller one already separates a following word ("B⁻ and", 0.7pt).
    // After a subscript a small gap is just math spacing ("H₂O", "SO₂Cl₂").
    const minGap = afterPower && /^[A-Za-zÀ-ÖØ-öø-ɏͰ-Ͽℓ¹²³⁰⁴-⁹₀-₉0-9]/.test(it.s) ? 0.5 : Math.max(1.5, (it.size || 0) * 0.2);
    afterPower = false;
    if (out && it.x - end > minGap && !/\s$/.test(out) && !/^\s/.test(it.s)) out += " ";
    out += it.s;
    end = it.x + it.w;
  }
  return out.replace(/\s+/g, " ").trim();
}

// Size of the page's body text: the font size most characters are set in.
function bodySize(items: Item[]): number {
  const count = new Map<number, number>();
  for (const it of items) {
    const k = Math.round((it.size || 0) * 2) / 2;
    if (k > 0) count.set(k, (count.get(k) || 0) + it.s.trim().length);
  }
  let best = 0;
  let n = -1;
  count.forEach((c, k) => {
    if (c > n) [best, n] = [k, c];
  });
  return best;
}

const QNUM = /^\d{1,3}\.(\s|$)/;
const LABEL_ONLY = /^\(?([1-4a-dA-D]|i{1,3}|iv)\)$/;

export function pageText(rawItems: any[], pageWidth: number, images: PlacedImage[] = []): string {
  const items: Item[] = rawItems
    .filter((i) => typeof i?.str === "string" && i.str.length)
    // Rotated text is a watermark (e.g. the diagonal institute name on
    // solution sheets), never question content — its letters would otherwise
    // land one by one in the middle of answers.
    .filter((i) => Math.abs(i.transform?.[1] || 0) < 0.01 && Math.abs(i.transform?.[2] || 0) < 0.01)
    .map((i) => ({ x: i.transform[4], y: i.transform[5], w: i.width || 0, s: i.str, size: Math.abs(i.transform[3] || 0) }));
  // Small text sitting off the baseline is a superscript/subscript. It is
  // held back while lines are formed — a 10⁻¹⁰ exponent is ~4.5pt above its
  // line, so it used to become a line of its own ("−10" printed above the
  // question, "2 × 10 and" left without its power) — then attached to the
  // line it touches.
  const bodyPt = bodySize(items);
  const small = (it: Item) => bodyPt > 0 && (it.size || 0) > 0 && (it.size || 0) <= bodyPt * 0.82;
  const held = items.filter(small);
  const main = items.filter((it) => !small(it));

  // Group into lines (same baseline within 3pt), top of page first.
  main.sort((a, b) => b.y - a.y || a.x - b.x);
  const lines: { y: number; items: Item[]; image?: boolean }[] = [];
  const addToLines = (it: Item) => {
    const line = lines.find((l) => !l.image && Math.abs(l.y - it.y) <= 3);
    if (line) line.items.push(it);
    else lines.push({ y: it.y, items: [it] });
  };
  for (const it of main) addToLines(it);

  // Attach each small piece (left to right, so "−", "1", "0" chain) to the
  // line whose text it touches horizontally and whose baseline is just
  // below (superscript) or just above (subscript) it. Small text touching
  // nothing — footnotes, a smaller header — is ordinary text.
  held.sort((a, b) => a.x - b.x || b.y - a.y);
  for (const it of held) {
    let best: { line: (typeof lines)[number]; dy: number } | null = null;
    for (const l of lines) {
      const dy = it.y - l.y;
      const base = Math.max(...l.items.map((x) => x.size || 0));
      if (dy > base * 0.6 || dy < -base * 0.45) continue;
      // The piece before it must be something a power/subscript can sit on
      // (a letter, digit, bracket or another small piece) — not an option
      // label "1)" or a minus sign, which is where the top of a physics
      // display fraction (2q over ε₀) sits; that is left as it always was.
      const touches = l.items.some((x) => {
        const xEnd = x.x + x.w;
        const base = !!x.script || (/[A-Za-zÀ-ÖØ-öø-ɏͰ-Ͽℓ¹²³⁰⁴-⁹₀-₉0-9)\]′]\s*$/.test(x.s) && !LABEL_ONLY.test(x.s.trim()));
        return (base && xEnd >= it.x - 2.5 && xEnd <= it.x + 1.5) || (x.x >= it.x + it.w - 1.5 && x.x <= it.x + it.w + 2.5);
      });
      if (touches && (!best || Math.abs(dy) < Math.abs(best.dy))) best = { line: l, dy };
    }
    if (best && Math.abs(best.dy) >= 0.8) best.line.items.push({ ...it, script: best.dy > 0 ? "sup" : "sub" });
    else if (best) best.line.items.push({ ...it, onBase: true }); // small but on the baseline
    else addToLines(it);
  }
  // A small piece on the baseline right under a raised one is the bottom of
  // a small stacked fraction (0.34nm over 3.4Å in an option).
  for (const l of lines) {
    const sups = l.items.filter((x) => x.script === "sup");
    for (const x of l.items)
      if (x.onBase && sups.some((s) => s.x < x.x + x.w - 0.5 && x.x < s.x + s.w - 0.5)) x.script = "sub";
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
