"use client";

import { useState } from "react";
import AnswerText from "@/components/AnswerText";

export type AnswerStep = { title: string; body: string };

// Click-through walkthrough for an AI Tutor answer — one step revealed at a
// time instead of the whole explanation at once, per the "guided, step by
// step" product ask. Falls back to nothing special if a caller has no
// steps (older answers stored before this existed); AnswerText on the raw
// content still renders those fine, just as one block.
export default function GuidedAnswer({ steps }: { steps: AnswerStep[] }) {
  const [i, setI] = useState(0);
  const [furthest, setFurthest] = useState(0);
  if (!steps.length) return null;
  const step = steps[i];
  const isLast = i === steps.length - 1;

  function go(next: number) {
    setI(next);
    setFurthest((f) => Math.max(f, next));
  }

  return (
    <div>
      <div className="flex items-center gap-1.5">
        {steps.map((_, idx) => (
          <button
            key={idx}
            onClick={() => idx <= furthest && go(idx)}
            disabled={idx > furthest}
            aria-label={`Step ${idx + 1}`}
            className={`h-1.5 flex-1 rounded-full transition ${
              idx === i ? "bg-blue-500" : idx <= furthest ? "bg-blue-300" : "bg-blue-100"
            } ${idx <= furthest ? "cursor-pointer" : "cursor-default"}`}
          />
        ))}
      </div>
      <div className="mt-3 flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-blue-700">
        <span>
          Step {i + 1} of {steps.length}
        </span>
        <span className="text-blue-900">— {step.title}</span>
      </div>
      <AnswerText className="mt-1.5 text-sm leading-relaxed text-[#3E4A63]">{step.body}</AnswerText>
      <div className="mt-3 flex items-center justify-between">
        <button
          onClick={() => go(i - 1)}
          disabled={i === 0}
          className="rounded-lg px-3 py-1.5 text-xs font-semibold text-blue-800 hover:bg-blue-100 disabled:cursor-default disabled:opacity-0"
        >
          ← Previous
        </button>
        {!isLast ? (
          <button
            onClick={() => go(i + 1)}
            className="rounded-lg bg-blue-500 px-4 py-1.5 text-xs font-semibold text-white hover:bg-blue-600"
          >
            Next step →
          </button>
        ) : (
          <span className="text-xs font-medium text-emerald-700">✓ That's the full walkthrough</span>
        )}
      </div>
    </div>
  );
}
