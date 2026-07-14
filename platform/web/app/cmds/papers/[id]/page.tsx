"use client";

import { useEffect, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import MathText from "@/components/MathText";

type Q = {
  id: string;
  content: string;
  type: string;
  options: string[];
  answerIndices: string[];
  answerText: string[];
  solution: string | null;
  difficulty: string | null;
};
type Section = { name: string; questions: Q[] };
type Paper = { test: { name: string; code: string | null; totalMarks: number }; sections: Section[] };

const LETTERS = ["A", "B", "C", "D", "E", "F", "G", "H"];

function PaperInner({ id }: { id: string }) {
  const params = useSearchParams();
  const mode = params.get("mode") === "solution" ? "solution" : "question";
  const [paper, setPaper] = useState<Paper | null>(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    fetch(`/api/cmds/papers/${id}`)
      .then((r) => r.json())
      .then((d) => (d.error ? setErr(d.error) : setPaper(d)))
      .catch(() => setErr("Failed to load paper"));
  }, [id]);

  if (err) return <div className="p-10 text-center text-red-600">{err}</div>;
  if (!paper) return <div className="p-10 text-center text-slate-400">Loading…</div>;

  let counter = 0;
  return (
    <div className="mx-auto max-w-3xl bg-white p-10 text-slate-800">
      {/* Print controls — hidden when printing */}
      <div className="mb-6 flex items-center justify-between print:hidden">
        <div className="flex gap-2 text-sm">
          <a
            href={`?mode=question`}
            className={`rounded-md border px-3 py-1.5 ${
              mode === "question" ? "border-blue-500 bg-blue-50 text-blue-700" : "border-slate-200 text-slate-600"
            }`}
          >
            Question paper
          </a>
          <a
            href={`?mode=solution`}
            className={`rounded-md border px-3 py-1.5 ${
              mode === "solution" ? "border-blue-500 bg-blue-50 text-blue-700" : "border-slate-200 text-slate-600"
            }`}
          >
            Solution paper
          </a>
        </div>
        <button
          onClick={() => window.print()}
          className="rounded-md bg-slate-800 px-4 py-1.5 text-sm font-medium text-white hover:bg-slate-700"
        >
          Print / Save PDF
        </button>
      </div>

      <div className="border-b-2 border-slate-800 pb-3 text-center">
        <h1 className="text-xl font-bold">{paper.test.name}</h1>
        <div className="mt-1 text-sm text-slate-500">
          {paper.test.code ? `Code: ${paper.test.code} · ` : ""}Total Marks: {paper.test.totalMarks}
          {mode === "solution" ? " · SOLUTIONS" : ""}
        </div>
      </div>

      {paper.sections.map((s, si) => (
        <div key={si} className="mt-6">
          <h2 className="mb-3 text-sm font-bold uppercase tracking-wide text-slate-600">{s.name}</h2>
          <ol className="space-y-5">
            {s.questions.map((q) => {
              counter += 1;
              return (
                <li key={q.id} className="text-[15px] leading-relaxed">
                  <div className="flex gap-2">
                    <span className="font-semibold">{counter}.</span>
                    <div className="flex-1">
                      <MathText>{q.content}</MathText>
                      {q.options.length > 0 && (
                        <div className="mt-2 grid grid-cols-2 gap-x-6 gap-y-1 pl-1">
                          {q.options.map((o, oi) => {
                            const isKey = mode === "solution" && q.answerIndices.includes(String(oi));
                            return (
                              <div
                                key={oi}
                                className={isKey ? "font-semibold text-emerald-700" : ""}
                              >
                                ({LETTERS[oi] || oi + 1}) <MathText>{o}</MathText>
                                {isKey ? " ✓" : ""}
                              </div>
                            );
                          })}
                        </div>
                      )}
                      {mode === "solution" && (
                        <div className="mt-2 rounded bg-slate-50 p-2 text-sm">
                          <span className="font-semibold text-emerald-700">Answer: </span>
                          {q.answerText.length ? (
                            <MathText>{q.answerText.join(", ")}</MathText>
                          ) : (
                            <span className="text-slate-400">Manually graded</span>
                          )}
                          {q.solution && (
                            <div className="mt-1 text-slate-600">
                              <span className="font-semibold">Solution: </span>
                              <MathText>{q.solution}</MathText>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </li>
              );
            })}
          </ol>
        </div>
      ))}
    </div>
  );
}

export default function PaperPage({ params }: { params: { id: string } }) {
  return (
    <div className="min-h-screen bg-slate-100 py-8 print:bg-white print:py-0">
      <Suspense fallback={<div className="p-10 text-center text-slate-400">Loading…</div>}>
        <PaperInner id={params.id} />
      </Suspense>
    </div>
  );
}
