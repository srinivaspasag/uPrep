// Parsing for CMDS Bulk Import (app/cmds/questions/import). Pure functions,
// no React, so they can be exercised directly against real papers.
//
// Supported question-paper shapes:
//   "1] text a) .. b) .. c) .. d) .."           (original format)
//   "1. text (1) .. (2) .. (3) .. (4) .."       (AIIMS-style)
//   "1. text 1) .. 2) .. 3) .. 4) .."           (e.g. Scholar Spectra DT papers)
// In the last shape "1)" is an OPTION marker, not a question number, so the
// question splitter has to work out which numbering style marks questions.

export const LETTERS = ["a", "b", "c", "d"];

// Line-start "N)" markers whose numbers never go past 4 and keep restarting
// at 1 are options, not questions — only when questions have their own
// "N." / "N]" numbering to fall back on.
function parenIsOptionStyle(text: string): boolean {
  const paren = Array.from(text.matchAll(/(?:^|\n)\s*(\d{1,3})\s*\)\s+/g)).map((m) => parseInt(m[1], 10));
  const other = Array.from(text.matchAll(/(?:^|\n)\s*(\d{1,3})\s*[.\]]\s+/g)).length;
  if (paren.length < 4 || other < 2) return false;
  const small = paren.filter((n) => n >= 1 && n <= 4).length;
  const restarts = paren.filter((n) => n === 1).length;
  return small / paren.length >= 0.9 && restarts >= 2;
}

export function splitQuestionBlocks(text: string): { num: number; text: string }[] {
  // "." / ")" delimited numbers require a line break before them (running
  // prose has plenty of "2. " and "b) " that aren't question boundaries).
  // "]" is a much rarer marker in this content, so it only needs to be
  // preceded by whitespace — important because OCR'd/copy-pasted text (e.g.
  // from Google Docs) often loses hard line breaks and runs questions
  // together as one paragraph, e.g. "...E of S 21] A vehicle...".
  const re = parenIsOptionStyle(text)
    ? /(?:^|\n)\s*(\d{1,3})\s*[.]\s+|(?:^|\s)(\d{1,3})\]\s+/g
    : /(?:^|\n)\s*(\d{1,3})\s*[.)]\s+|(?:^|\s)(\d{1,3})\]\s+/g;
  const raw = Array.from(text.matchAll(re)).map((m) => ({
    num: parseInt((m[1] ?? m[2]) as string, 10),
    index: m.index ?? 0,
    length: m[0].length,
  }));
  // Keep only strictly increasing question numbers — guards against a stray
  // bracketed number (e.g. a "[21]" source citation) being mistaken for a
  // new question boundary and splitting mid-question.
  const filtered: typeof raw = [];
  let last = 0;
  for (const m of raw) {
    if (m.num > last) {
      filtered.push(m);
      last = m.num;
    }
  }
  const blocks: { num: number; text: string }[] = [];
  for (let i = 0; i < filtered.length; i++) {
    const start = filtered[i].index + filtered[i].length;
    const end = i + 1 < filtered.length ? filtered[i + 1].index : text.length;
    blocks.push({ num: filtered[i].num, text: text.slice(start, end).trim() });
  }
  return blocks;
}

// Option marker sets, all mapped positionally onto a/b/c/d. Bare "a)" / "1)"
// must not be part of "(a)" / "(1)" or a longer word/number, so statement
// labels like "(a) Darwin's finches" inside the question are not mistaken for
// options.
type MarkerSet = { markers: [string, string, string, string]; bare: boolean };
const OPTION_MARKER_SETS: MarkerSet[] = [
  { markers: ["a)", "b)", "c)", "d)"], bare: true },
  { markers: ["(1)", "(2)", "(3)", "(4)"], bare: false },
  { markers: ["1)", "2)", "3)", "4)"], bare: true },
];

// Index of the last occurrence of `marker` starting before `before`.
function lastMarker(low: string, marker: string, bare: boolean, before: number): number {
  let idx = low.lastIndexOf(marker, before);
  while (idx !== -1) {
    const prev = idx > 0 ? low[idx - 1] : "";
    if (!bare || !/[\w(]/.test(prev)) return idx;
    idx = idx > 0 ? low.lastIndexOf(marker, idx - 1) : -1;
  }
  return -1;
}

// Finds the options run at the end of a block: the last occurrence of each
// of the four markers. They don't have to appear in order — on 2x2 option
// grids the PDF baseline of "4)" can sit a hair above "3)", so the text comes
// out "… 4) X 3) Y". Each option is the text from its marker to the next
// marker. When more than one marker style fits (statements labelled a)-d)
// followed by options 1)-4), or the reverse), the run that starts LATEST is
// the options.
// Picture tokens ("![image](/uploads/x.png)") are masked out before looking
// for option labels, so a URL or placeholder ending in "1)" is never taken
// for an option marker. Same length, so positions still line up.
export const IMAGE_TOKEN = /!\[[^\]\n]*\]\([^)\s]*\)/g;
const maskImages = (s: string) => s.replace(IMAGE_TOKEN, (m) => "#".repeat(m.length));

export function splitOptions(block: string): { question: string; options: string[] } | null {
  const low = maskImages(block).toLowerCase();
  let best: { set: MarkerSet; pos: number[] } | null = null;
  for (const set of OPTION_MARKER_SETS) {
    const pos = set.markers.map((mk) => lastMarker(low, mk, set.bare, low.length));
    if (pos.some((p) => p === -1)) continue;
    if (new Set(pos).size !== 4) continue;
    if (!best || Math.min(...pos) > Math.min(...best.pos)) best = { set, pos };
  }
  if (!best) return null;
  const { set, pos } = best;
  const order = [0, 1, 2, 3].sort((a, b) => pos[a] - pos[b]);
  const clean = (s: string) => s.replace(/\s+/g, " ").trim();
  const options = ["", "", "", ""];
  order.forEach((k, i) => {
    const start = pos[k] + set.markers[k].length;
    const end = i + 1 < order.length ? pos[order[i + 1]] : block.length;
    options[k] = clean(block.slice(start, end));
  });
  return { question: clean(block.slice(0, pos[order[0]])), options };
}

// Answer key -> { questionNumber: "a" | "b" | "c" | "d" }. Accepts:
//   "1] c", "1)c", "1. (c)" anywhere (loose — solution paragraphs start the same way)
//   one answer per line with a number 1-4: "1) 3", "1 - (3)", "1. 3"
//   spreadsheet rows (uploaded .xlsx/.csv come through as tab-separated
//   lines), e.g. "1<TAB>2<TAB>B" — a letter cell wins over a number cell.
export function parseAnswerKey(text: string): Record<number, string> {
  const map: Record<number, string> = {};
  const toLetter = (v: string): string => {
    const t = v.trim().replace(/^\(|\)$/g, "").trim().toLowerCase();
    if (/^[a-d]$/.test(t)) return t;
    if (/^[1-4]$/.test(t)) return LETTERS[parseInt(t, 10) - 1];
    return "";
  };

  // Spreadsheet / table rows first: they are unambiguous.
  for (const line of text.split(/\r?\n/)) {
    if (!line.includes("\t") && !line.includes(",")) continue;
    const cells = line.split(/\t|,/).map((c) => c.trim()).filter(Boolean);
    if (cells.length < 2 || !/^\d{1,3}$/.test(cells[0])) continue;
    const rest = cells.slice(1);
    const letter = rest.map((c) => c.replace(/^\(|\)$/g, "").trim().toLowerCase()).find((c) => /^[a-d]$/.test(c));
    const ans = letter || toLetter(rest[0]);
    if (ans) map[parseInt(cells[0], 10)] = ans;
  }

  const re = /(\d{1,3})\s*[.)\]]\s*\(?([a-dA-D])\)?(?![a-zA-Z])/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text))) {
    const n = parseInt(m[1], 10);
    if (!map[n]) map[n] = m[2].toLowerCase();
  }

  for (const line of text.split(/\r?\n/)) {
    const nm = line.match(/^\s*(?:q\.?\s*)?(\d{1,3})\s*(?:[.)\]:\-]\s*|\s+)\(?([1-4])\)?\s*$/i);
    if (nm) {
      const n = parseInt(nm[1], 10);
      if (!map[n]) map[n] = LETTERS[parseInt(nm[2], 10) - 1];
    }
  }
  return map;
}
