"use client";

import katex from "katex";
import { useMemo } from "react";

// Renders chat-style answer text (AI Tutor + human doubt answers): light
// markdown — **bold**, numbered/bulleted lists, paragraph breaks — plus
// LaTeX math via KaTeX. MathText.tsx (used for question banks) intentionally
// stays math-only and untouched; question content is never markdown, so
// giving it list/bold parsing would risk misreading a literal "1." or "*"
// inside a question as a list marker. This is a separate component so that
// risk never applies to the existing question-rendering call sites.
function escapeHtml(s: string): string {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// Some models write LaTeX with \( \)/\[ \] delimiters despite being told to
// use $ $/$$ $$ (the only delimiters KaTeX-in-this-app recognizes) — normalize
// defensively here too, not just at generation time, so it degrades gracefully
// for any answer that slipped through already.
function normalizeDelimiters(s: string): string {
  return s.replace(/\\\[/g, "$$").replace(/\\\]/g, "$$").replace(/\\\(/g, "$").replace(/\\\)/g, "$");
}

// Guards against a model leaving a $ open across an English aside instead of
// closing it around just the symbols (confirmed live: "$\frac{b}{a}(because
// the coefficient of x is -b)...$" — valid enough LaTeX that KaTeX renders
// it without error, as a wall of italic letters with the spaces silently
// eaten, since math mode ignores whitespace between tokens). A real math
// span is mostly symbols/short variables; several space-separated plain-
// English words inside one is the tell that prose leaked into math mode —
// render that span as plain text (spacing intact) instead of feeding KaTeX.
function looksLikeStrayProse(inner: string): boolean {
  const tokens = inner.trim().split(/\s+/);
  if (tokens.length < 4) return false;
  const wordy = tokens.filter((t) => /^[A-Za-z]{2,}$/.test(t)).length;
  return wordy / tokens.length > 0.6;
}

function renderMath(inner: string, displayMode: boolean): string {
  if (looksLikeStrayProse(inner)) return escapeHtml(inner);
  try {
    return katex.renderToString(inner, { displayMode, throwOnError: false });
  } catch {
    return escapeHtml(inner);
  }
}

// Placeholder marker uses Private Use Area characters — guaranteed not to
// collide with anything a model or student could type, and immune to
// escapeHtml (which only touches & < >) and the bold-markdown split below,
// so the rendered math HTML can be substituted back in after everything
// else has been escaped/structured.
const MARK = "\u0001AIRAMATH\u0001";

// Pulled out to its own pass over the FULL text, before any line-splitting.
// Bug found live: a model wrapped a display equation as "\(\n...\n\)" (the
// delimiters each on their own line) — line-by-line processing put the "\("
// and "\)" (normalized to "$") on different lines than the formula between
// them, so the pairing never matched and the raw LaTeX source rendered as
// literal text instead of math. Matching across the whole string first,
// before any line splitting happens, means a math span can never be
// severed by a line break inside it, regardless of how a model wraps it.
function extractMath(input: string): { text: string; lookup: string[] } {
  const lookup: string[] = [];
  const text = input.replace(/\$\$([\s\S]+?)\$\$|\$([^$]+?)\$/g, (_m, block, inline) => {
    const html = block !== undefined ? renderMath(block.trim(), true) : renderMath(inline, false);
    lookup.push(html);
    return `${MARK}${lookup.length - 1}${MARK}`;
  });
  return { text, lookup };
}

// Renders **...** (bold) plus placeholder substitution within one line;
// everything else is escaped plain text.
function renderInline(line: string, lookup: string[]): string {
  const out: string[] = [];
  const markerRe = new RegExp(`${MARK}(\\d+)${MARK}`, "g");
  const parts = line.split(markerRe);
  // split() with a capturing group alternates [text, index, text, index, ...text]
  parts.forEach((part, i) => {
    if (i % 2 === 1) {
      out.push(lookup[Number(part)] ?? "");
      return;
    }
    const boldParts = part.split(/(\*\*[^*]+\*\*)/g);
    for (const b of boldParts) {
      if (b.startsWith("**") && b.endsWith("**") && b.length >= 4) {
        out.push(`<strong>${escapeHtml(b.slice(2, -2))}</strong>`);
      } else {
        out.push(escapeHtml(b));
      }
    }
  });
  return out.join("");
}

function renderToHtml(input: string): string {
  if (!input) return "";
  const { text: withPlaceholders, lookup } = extractMath(normalizeDelimiters(input));
  const lines = withPlaceholders.split("\n");
  const html: string[] = [];
  let listBuffer: { type: "ol" | "ul"; items: string[] } | null = null;

  const flushList = () => {
    if (!listBuffer) return;
    const tag = listBuffer.type;
    html.push(`<${tag} class="ml-5 list-${tag === "ol" ? "decimal" : "disc"} space-y-1">`);
    for (const item of listBuffer.items) html.push(`<li>${renderInline(item, lookup)}</li>`);
    html.push(`</${tag}>`);
    listBuffer = null;
  };

  for (const raw of lines) {
    const line = raw.trim();
    const ordered = line.match(/^\d+[.)]\s+(.*)$/);
    const bulleted = !ordered && line.match(/^[-*]\s+(.*)$/);
    if (ordered) {
      if (!listBuffer || listBuffer.type !== "ol") {
        flushList();
        listBuffer = { type: "ol", items: [] };
      }
      listBuffer.items.push(ordered[1]);
      continue;
    }
    if (bulleted) {
      if (!listBuffer || listBuffer.type !== "ul") {
        flushList();
        listBuffer = { type: "ul", items: [] };
      }
      listBuffer.items.push(bulleted[1]);
      continue;
    }
    flushList();
    if (!line) continue;
    html.push(`<p>${renderInline(line, lookup)}</p>`);
  }
  flushList();
  return html.join("");
}

export default function AnswerText({ children, className }: { children: string; className?: string }) {
  const html = useMemo(() => renderToHtml(children || ""), [children]);
  return (
    <div
      className={`space-y-2 ${className || ""}`}
      // eslint-disable-next-line react/no-danger
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
