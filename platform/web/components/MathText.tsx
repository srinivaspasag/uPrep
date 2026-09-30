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

function renderToHtml(input: string): string {
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
