"use client";

import { useState } from "react";
import MathFieldInput from "./MathFieldInput";

// Visual equation builder — click fraction/root/exponent templates and fill
// in tab-able slots, see it rendered as you go, same interaction model as
// Word's Insert > Equation. Replaces the old approach of inserting a raw
// LaTeX snippet like "$\frac{a}{b}$" and asking the author to hand-edit the
// placeholder letters inside the text.
export default function EquationBuilder({
  onInsert,
  onClose,
}: {
  onInsert: (latex: string) => void;
  onClose: () => void;
}) {
  const [latex, setLatex] = useState("");

  function insert() {
    if (!latex.trim()) return;
    onInsert(`$${latex.trim()}$`);
    onClose();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Build an equation</h3>
        <p className="mt-1 text-sm text-slate-500">
          Type numbers/letters normally. Use the on-screen keyboard for fractions, roots, exponents —
          click a template, then tab between its boxes to fill them in.
        </p>
        <div className="mt-4">
          <MathFieldInput value={latex} onChange={setLatex} autoFocus />
        </div>
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={insert}
            disabled={!latex.trim()}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            Insert
          </button>
        </div>
      </div>
    </div>
  );
}
