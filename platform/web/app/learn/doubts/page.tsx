"use client";

import { AiraAvatar } from "./layout";

// Right-pane welcome state — shown at /learn/doubts with nothing selected
// yet. The doubts/layout.tsx sidebar (My Doubts list + New Doubt button)
// is always visible alongside this.
export default function DoubtsWelcomePage() {
  return (
    <div className="flex h-full flex-col items-center justify-center px-8 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-sky-100 via-blue-100 to-indigo-100">
        <span className="text-3xl">✨</span>
      </div>
      <h1 className="mt-5 font-serif text-xl font-semibold text-[#16233D]">Ask Aira anything</h1>
      <p className="mt-2 max-w-sm text-sm text-[#8890A1]">
        Pick a doubt from the list on the left, or click <span className="font-medium text-[#3E4A63]">+ New Doubt</span>{" "}
        to ask something new — Aira walks you through the answer step by step.
      </p>
      <div className="mt-6 flex items-center gap-2 text-xs text-[#8890A1]">
        <AiraAvatar />
        <span>Aira answers instantly, but always double-check with your teacher.</span>
      </div>
    </div>
  );
}
