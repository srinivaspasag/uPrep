"use client";

import katex from "katex";
import { useMemo } from "react";

// Renders text containing LaTeX. Supports $$...$$ (block) and $...$ (inline)
// delimiters; everything else is treated as plain text. Falls back to the raw
// source if KaTeX can't parse a segment.
function renderToHtml(input: string): string {
  if (!input) return "";
  const escapeHtml = (s: string) =>
    s
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");

  // Split on $$...$$ first, then $...$ inside plain segments.
  const out: string[] = [];
  const blockParts = input.split(/(\$\$[^$]*\$\$)/g);
  for (const part of blockParts) {
    if (part.startsWith("$$") && part.endsWith("$$") && part.length >= 4) {
      const tex = part.slice(2, -2);
      try {
        out.push(katex.renderToString(tex, { displayMode: true, throwOnError: false }));
      } catch {
        out.push(escapeHtml(part));
      }
      continue;
    }
    const inlineParts = part.split(/(\$[^$]+\$)/g);
    for (const seg of inlineParts) {
      if (seg.startsWith("$") && seg.endsWith("$") && seg.length >= 2) {
        const tex = seg.slice(1, -1);
        try {
          out.push(katex.renderToString(tex, { displayMode: false, throwOnError: false }));
        } catch {
          out.push(escapeHtml(seg));
        }
      } else {
        out.push(escapeHtml(seg));
      }
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
