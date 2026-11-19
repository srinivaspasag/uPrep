"use client";

import katex from "katex";
import { useMemo } from "react";

// Renders text containing LaTeX. Supports both delimiter conventions found in
// the actual question bank: $$...$$ / $...$ (the ones this originally
// handled) AND \[...\] / \(...\) — the AI transcription pipeline used for
// equation-heavy PDFs (bulk import) outputs the backslash style almost
// exclusively, so questions using it were rendering as literal text with
// visible backslashes and parens instead of math. Falls back to the raw
// source if KaTeX can't parse a segment.
const MATH_SPLIT = /(\$\$[\s\S]*?\$\$|\\\[[\s\S]*?\\\]|\$[^$\n]+?\$|\\\([\s\S]*?\\\))/g;

// Pictures in questions/options/solutions: markdown-style `![alt](url)`
// (written by Bulk Import for pictures in PDFs, and by the question editor's
// "Add picture"). Only files uploaded to this site (/uploads/<name>) are
// shown — anything else stays as plain text, so question text can never
// load outside content.
const IMAGE_SPLIT = /(!\[[^\]\n]*\]\([^)\s]*\))/g;
const IMAGE_ONE = /^!\[([^\]\n]*)\]\((\/uploads\/[A-Za-z0-9._-]+)\)$/;

function imageHtml(token: string): string | null {
  const m = token.match(IMAGE_ONE);
  if (!m) return null;
  const alt = (m[1] || "picture").replace(/[<>"&]/g, "");
  return `<img src="${m[2]}" alt="${alt}" loading="lazy" style="display:block;max-width:100%;height:auto;margin:6px 0" />`;
}

function renderToHtml(input: string): string {
  if (!input) return "";
  if (input.includes("![")) {
    return input
      .split(IMAGE_SPLIT)
      .map((part) => (part.startsWith("![") ? imageHtml(part) ?? renderMath(part) : renderMath(part)))
      .join("");
  }
  return renderMath(input);
}

function renderMath(input: string): string {
  if (!input) return "";
  const escapeHtml = (s: string) =>
    s
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");

  const out: string[] = [];
  for (const part of input.split(MATH_SPLIT)) {
    if (!part) continue;
    let tex: string | null = null;
    let displayMode = false;
    if (part.startsWith("$$") && part.endsWith("$$") && part.length >= 4) {
      tex = part.slice(2, -2);
      displayMode = true;
    } else if (part.startsWith("\\[") && part.endsWith("\\]") && part.length >= 4) {
      tex = part.slice(2, -2);
      displayMode = true;
    } else if (part.startsWith("\\(") && part.endsWith("\\)") && part.length >= 4) {
      tex = part.slice(2, -2);
      displayMode = false;
    } else if (part.startsWith("$") && part.endsWith("$") && part.length >= 2) {
      tex = part.slice(1, -1);
      displayMode = false;
    }

    if (tex !== null) {
      try {
        out.push(katex.renderToString(tex, { displayMode, throwOnError: false }));
      } catch {
        out.push(escapeHtml(part));
      }
    } else {
      out.push(escapeHtml(part));
    }
  }
  return out.join("");
}

export default function MathText({
  children,
  className,
}: {
  children: string;
  className?: string;
}) {
  const html = useMemo(() => renderToHtml(children || ""), [children]);
  return (
    <span
      className={className}
      // eslint-disable-next-line react/no-danger
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
