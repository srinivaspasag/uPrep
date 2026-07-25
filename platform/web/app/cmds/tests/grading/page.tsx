"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import MathText from "@/components/MathText";

type Item = {
  id: string;
  qId: string;
  studentName: string;
  answerGiven: string[];
  question: string | null;
  maxMarks: number | null;
};

export default function GradingPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [marks, setMarks] = useState<Record<string, string>>({});

  async function load() {
    setLoading(true);
    try {
      const d = await fetch("/api/cmds/tests/grading").then((r) => r.json());
      setItems(d.items || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);

  async function grade(item: Item, verdict: string) {
    const m = marks[item.id];
    await fetch("/api/cmds/tests/grading", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: item.id, verdict, marks: m ? Number(m) : undefined }),
    });
    setItems((prev) => prev.filter((x) => x.id !== item.id));
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[900px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Subjective Grading</h1>
        <p className="mt-1 text-sm text-slate-500">
          Answers the auto-grader couldn&apos;t judge. Assign a verdict (and optional marks) to release
          the student&apos;s score.
        </p>

        <div className="mt-6 space-y-4">
          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading…</div>
          ) : items.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
              Nothing awaiting grading. 🎉
            </div>
          ) : (
            items.map((it) => (
              <div key={it.id} className="rounded-lg border border-slate-200 bg-white p-5">
                <div className="text-xs uppercase tracking-wide text-slate-400">{it.studentName}</div>
                {it.question && (
                  <div className="mt-2">
                    <MathText className="text-slate-800">{it.question}</MathText>
                  </div>
                )}
                <div className="mt-3 rounded bg-slate-50 p-3 text-sm text-slate-700">
                  <div className="mb-1 text-xs font-medium text-slate-500">Student answer</div>
                  {it.answerGiven.length ? it.answerGiven.join(", ") : <span className="text-slate-400">— (blank)</span>}
                </div>
                <div className="mt-3 flex flex-wrap items-center gap-2">
                  {it.maxMarks != null && (
                    <input
                      type="number"
                      placeholder={`marks / ${it.maxMarks}`}
                      value={marks[it.id] || ""}
                      onChange={(e) => setMarks((m) => ({ ...m, [it.id]: e.target.value }))}
                      className="w-28 rounded border border-slate-300 px-2 py-1 text-sm"
                    />
                  )}
                  <button
                    onClick={() => grade(it, "CORRECT")}
                    className="rounded bg-emerald-600 px-3 py-1 text-sm font-medium text-white hover:bg-emerald-700"
                  >
                    Correct
                  </button>
                  <button
                    onClick={() => grade(it, "PARTIAL")}
                    className="rounded bg-amber-500 px-3 py-1 text-sm font-medium text-white hover:bg-amber-600"
                  >
                    Partial
                  </button>
                  <button
                    onClick={() => grade(it, "INCORRECT")}
                    className="rounded bg-red-500 px-3 py-1 text-sm font-medium text-white hover:bg-red-600"
                  >
                    Incorrect
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </CmdsShell>
  );
}
