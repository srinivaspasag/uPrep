"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import MathText from "@/components/MathText";

type QType = "SCQ" | "MCQ" | "NUMERIC" | "SUBJECTIVE" | "MATRIX" | "PARA";

const TYPES: { k: QType; label: string; hint: string }[] = [
  { k: "SCQ", label: "Single correct", hint: "One right option" },
  { k: "MCQ", label: "Multiple correct", hint: "One or more right options" },
  { k: "NUMERIC", label: "Numeric", hint: "Answer is a number" },
  { k: "SUBJECTIVE", label: "Subjective", hint: "Free text, graded manually" },
  { k: "MATRIX", label: "Matrix match", hint: "Match List A to List B" },
  { k: "PARA", label: "Comprehension", hint: "Passage + single-correct" },
];

const hasOptions = (t: QType) => t === "SCQ" || t === "MCQ" || t === "PARA";

export default function NewQuestionPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [content, setContent] = useState("");
  const [type, setType] = useState<QType>("SCQ");
  const [options, setOptions] = useState<string[]>(["", "", "", ""]);
  const [correct, setCorrect] = useState<number[]>([]);
  const [numeric, setNumeric] = useState("");
  const [paragraph, setParagraph] = useState("");
  const [matrixPairs, setMatrixPairs] = useState("");
  const [solution, setSolution] = useState("");
  const [difficulty, setDifficulty] = useState("EASY");
  const [subject, setSubject] = useState("");
  const [tags, setTags] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
  }, [router]);

  function setOption(i: number, val: string) {
    setOptions((prev) => prev.map((o, idx) => (idx === i ? val : o)));
  }
  function addOption() {
    setOptions((prev) => [...prev, ""]);
  }
  function removeOption(i: number) {
    setOptions((prev) => prev.filter((_, idx) => idx !== i));
    setCorrect((prev) => prev.filter((c) => c !== i).map((c) => (c > i ? c - 1 : c)));
  }
  function toggleCorrect(i: number) {
    if (type === "MCQ") setCorrect((prev) => (prev.includes(i) ? prev.filter((c) => c !== i) : [...prev, i]));
    else setCorrect([i]);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!content.trim()) return setError("Enter the question text.");

    const payload: any = {
      userId: session?.id,
      content: content.trim(),
      type,
      difficulty,
      subject: subject.trim(),
      tags: tags.split(",").map((t) => t.trim()).filter(Boolean),
      solution: solution.trim(),
    };

    if (hasOptions(type)) {
      const clean = options.map((o) => o.trim());
      if (clean.filter(Boolean).length < 2) return setError("Add at least 2 options.");
      if (clean.some((o) => !o)) return setError("Fill in every option (or remove blanks).");
      if (correct.length < 1) return setError("Mark the correct answer.");
      payload.options = clean;
      payload.correct = correct;
      if (type === "PARA") payload.paragraph = paragraph.trim();
    } else if (type === "NUMERIC") {
      const nums = numeric.split(",").map((n) => n.trim()).filter(Boolean);
      if (nums.length === 0) return setError("Enter at least one numeric answer.");
      payload.numericAnswers = nums;
    } else if (type === "MATRIX") {
      const clean = options.map((o) => o.trim()).filter(Boolean);
      if (clean.length < 2) return setError("Add the match items.");
      payload.options = clean;
      payload.matrixPairs = matrixPairs.split(",").map((p) => p.trim()).filter(Boolean);
    }

    setSaving(true);
    try {
      const r = await fetch("/api/cmds/questions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const d = await r.json();
      if (!r.ok || d.error) {
        setError(d.message || d.error || "Failed to save question.");
        setSaving(false);
        return;
      }
      router.push("/cmds/questions");
    } catch {
      setError("Failed to save question.");
      setSaving(false);
    }
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex h-14 max-w-3xl items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-800 font-bold text-white">
              C
            </div>
            <span className="font-semibold text-slate-800">UPrep CMDS</span>
          </div>
          <Link href="/cmds/questions" className="text-sm text-blue-600 hover:underline">
            ← Questions
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-slate-800">Add Question</h1>
        <p className="mt-1 text-slate-500">
          Supports LaTeX — wrap math in <code className="rounded bg-slate-100 px-1">$...$</code> for
          inline or <code className="rounded bg-slate-100 px-1">$$...$$</code> for block.
        </p>

        <form onSubmit={submit} className="mt-6 space-y-6">
          {/* Type selector */}
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Question type</label>
            <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3">
              {TYPES.map((t) => (
                <button
                  key={t.k}
                  type="button"
                  onClick={() => {
                    setType(t.k);
                    setCorrect([]);
                  }}
                  className={`rounded-md border px-3 py-2 text-left text-sm ${
                    type === t.k
                      ? "border-blue-500 bg-blue-50 text-blue-700"
                      : "border-slate-200 text-slate-600 hover:border-slate-300"
                  }`}
                >
                  <div className="font-medium">{t.label}</div>
                  <div className="text-xs text-slate-400">{t.hint}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Passage for comprehension */}
          {type === "PARA" && (
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">Comprehension passage</label>
              <textarea
                value={paragraph}
                onChange={(e) => setParagraph(e.target.value)}
                rows={4}
                placeholder="Enter the passage students read before answering…"
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          )}

          {/* Question text + preview */}
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Question</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={3}
              placeholder="e.g. Find $\int_0^1 x^2\,dx$"
              className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            {content.trim() && (
              <div className="mt-2 rounded-md bg-slate-50 p-3 text-sm text-slate-700">
                <span className="text-xs text-slate-400">Preview: </span>
                <MathText>{content}</MathText>
              </div>
            )}

            <div className="mt-4 flex flex-wrap gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700">Difficulty</label>
                <select
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
                  className="mt-2 rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700"
                >
                  <option value="EASY">Easy</option>
                  <option value="MODERATE">Moderate</option>
                  <option value="TOUGH">Tough</option>
                </select>
              </div>
              <div className="flex-1">
                <label className="block text-sm font-medium text-slate-700">Subject</label>
                <input
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  placeholder="e.g. Mathematics"
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700"
                />
              </div>
              <div className="flex-1">
                <label className="block text-sm font-medium text-slate-700">Tags (comma-separated)</label>
                <input
                  value={tags}
                  onChange={(e) => setTags(e.target.value)}
                  placeholder="calculus, integration"
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700"
                />
              </div>
            </div>
          </div>

          {/* Options (SCQ/MCQ/PARA) */}
          {hasOptions(type) && (
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-medium text-slate-700">
                  Options{" "}
                  <span className="text-slate-400">
                    (mark the correct {type === "MCQ" ? "answers" : "answer"})
                  </span>
                </label>
                <button type="button" onClick={addOption} className="text-sm text-blue-600 hover:underline">
                  + Add option
                </button>
              </div>
              <div className="mt-3 space-y-2">
                {options.map((opt, i) => (
                  <div key={i} className="flex items-center gap-3">
                    <input
                      type={type === "MCQ" ? "checkbox" : "radio"}
                      name="correct"
                      checked={correct.includes(i)}
                      onChange={() => toggleCorrect(i)}
                      className="h-4 w-4 accent-emerald-600"
                    />
                    <input
                      value={opt}
                      onChange={(e) => setOption(i, e.target.value)}
                      placeholder={`Option ${i + 1}`}
                      className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    />
                    {options.length > 2 && (
                      <button
                        type="button"
                        onClick={() => removeOption(i)}
                        className="text-slate-400 hover:text-red-500"
                      >
                        ✕
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Numeric */}
          {type === "NUMERIC" && (
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">
                Accepted answers <span className="text-slate-400">(comma-separated)</span>
              </label>
              <input
                value={numeric}
                onChange={(e) => setNumeric(e.target.value)}
                placeholder="e.g. 3.14, 3.142"
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          )}

          {/* Matrix */}
          {type === "MATRIX" && (
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">Match items (one per line)</label>
              <textarea
                value={options.join("\n")}
                onChange={(e) => setOptions(e.target.value.split("\n"))}
                rows={4}
                placeholder={"A. Newton\nB. Pascal\n1. Force\n2. Pressure"}
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <label className="mt-3 block text-sm font-medium text-slate-700">
                Correct pairs <span className="text-slate-400">(comma-separated, e.g. A-1, B-2)</span>
              </label>
              <input
                value={matrixPairs}
                onChange={(e) => setMatrixPairs(e.target.value)}
                placeholder="A-1, B-2"
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          )}

          {type === "SUBJECTIVE" && (
            <div className="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-700 ring-1 ring-amber-200">
              Subjective questions are stored for <span className="font-medium">manual grading</span> —
              students submit free text and a teacher scores it.
            </div>
          )}

          {/* Solution editor */}
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">
              Solution / explanation <span className="text-slate-400">(shown after attempt)</span>
            </label>
            <textarea
              value={solution}
              onChange={(e) => setSolution(e.target.value)}
              rows={3}
              placeholder="Explain the working. LaTeX supported."
              className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            {solution.trim() && (
              <div className="mt-2 rounded-md bg-slate-50 p-3 text-sm text-slate-700">
                <span className="text-xs text-slate-400">Preview: </span>
                <MathText>{solution}</MathText>
              </div>
            )}
          </div>

          {error && (
            <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {saving ? "Saving…" : "Save Question"}
            </button>
            <Link
              href="/cmds/questions"
              className="rounded-md border border-slate-300 px-5 py-2.5 text-sm text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </Link>
          </div>
        </form>
      </main>
    </div>
  );
}
