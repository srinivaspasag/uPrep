"use client";

import { RefObject, useState } from "react";
import EquationBuilder from "./EquationBuilder";

// Single, complete tokens — no editing needed after insert, so a plain
// button row is genuinely the fastest way to add these.
const SYMBOLS: { label: string; insert: string }[] = [
  { label: "π", insert: "$\\pi$" },
  { label: "θ", insert: "$\\theta$" },
  { label: "α", insert: "$\\alpha$" },
  { label: "β", insert: "$\\beta$" },
  { label: "Δ", insert: "$\\Delta$" },
  { label: "±", insert: "$\\pm$" },
  { label: "×", insert: "$\\times$" },
  { label: "÷", insert: "$\\div$" },
  { label: "≤", insert: "$\\leq$" },
  { label: "≥", insert: "$\\geq$" },
  { label: "≠", insert: "$\\neq$" },
  { label: "→", insert: "$\\rightarrow$" },
  { label: "∞", insert: "$\\infty$" },
  { label: "°", insert: "$^{\\circ}$" },
];

// Fractions/roots/exponents/sums used to be quick-insert templates like
// "$\frac{a}{b}$" that left the author hand-editing placeholder letters
// inside raw LaTeX — exactly the friction that made this "not student/
// teacher-friendly" (real feedback: MS Word's equation editor is easier).
// Those now go through EquationBuilder instead: a visual, click-and-fill
// math input (MathLive) that outputs the same LaTeX this app already
// stores and renders (see MathText/KaTeX) — no format change, just a much
// friendlier way to produce it.
export default function EquationToolbar({
  textareaRef,
  value,
  onChange,
}: {
  textareaRef: RefObject<HTMLInputElement | HTMLTextAreaElement | null>;
  value: string;
  onChange: (next: string) => void;
}) {
  const [building, setBuilding] = useState(false);

  function insert(snippet: string) {
    const el = textareaRef.current;
    const start = el?.selectionStart ?? value.length;
    const end = el?.selectionEnd ?? value.length;
    const next = value.slice(0, start) + snippet + value.slice(end);
    onChange(next);
    requestAnimationFrame(() => {
      if (!el) return;
      el.focus();
      const pos = start + snippet.length;
      el.setSelectionRange(pos, pos);
    });
  }

  return (
    <>
      <div className="mt-1 flex flex-wrap items-center gap-1 rounded-md border border-slate-200 bg-slate-50 p-1.5">
        <button
          type="button"
          onClick={() => setBuilding(true)}
          className="rounded border border-emerald-300 bg-emerald-50 px-2.5 py-1 text-sm font-medium text-emerald-700 hover:bg-emerald-100"
        >
          🧮 Build equation
        </button>
        <span className="mx-1 h-5 w-px bg-slate-300" />
        {SYMBOLS.map((s) => (
          <button
            key={s.label}
            type="button"
            onClick={() => insert(s.insert)}
            title={`Insert ${s.insert}`}
            className="min-w-[28px] rounded border border-slate-300 bg-white px-2 py-1 text-sm text-slate-700 hover:bg-slate-100"
          >
            {s.label}
          </button>
        ))}
      </div>
      {building && <EquationBuilder onInsert={insert} onClose={() => setBuilding(false)} />}
    </>
  );
}
