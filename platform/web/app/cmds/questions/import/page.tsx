"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import MathText from "@/components/MathText";
import BoardPicker from "@/components/BoardPicker";
import CmdsShell from "@/components/CmdsShell";
import PictureButton, { pastePicture } from "@/components/PictureButton";
import { LETTERS, parseAnswerKey, splitOptions, splitQuestionBlocks } from "@/lib/questionImport";

// Bulk-import many single-correct MCQs at once from a pasted question bank +
// answer key (the common coaching-institute PDF/Word format: "1] question
// text a) opt b) opt c) opt d) opt" plus a separate answer key like
// "1]c" / "1] c"). Rather than inventing a new bulk-insert backend, this
// reuses the exact same POST /api/cmds/questions single-question endpoint
// the manual "+ Add Question" form uses — one call per parsed row — so
// validation/authorship/storage shape stays identical either way.
//
// PDF/Word text extraction is unreliable for symbols and layout (fractions,
// square roots etc. often come through garbled), so this only accepts
// pasted text, not raw file upload, and always shows an editable review
// step before anything is written.

type ParsedRow = {
  num: number;
  content: string;
  options: string[];
  correctLetter: string; // "a" | "b" | "c" | "d" | ""
  parseOk: boolean;
  raw: string;
};

export default function BulkImportQuestionsPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [questionsText, setQuestionsText] = useState("");
  const [answerKeyText, setAnswerKeyText] = useState("");
  const [boardIds, setBoardIds] = useState<string[]>([]);
  const [difficulty, setDifficulty] = useState("MODERATE");
  const [rows, setRows] = useState<ParsedRow[] | null>(null);
  const [step, setStep] = useState<"input" | "review" | "done">("input");
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<{ done: number; failed: number; errors: string[] } | null>(null);
  const [extracting, setExtracting] = useState<"questions" | "answers" | null>(null);
  const [extractError, setExtractError] = useState("");
  const [extractNote, setExtractNote] = useState<{ target: "questions" | "answers"; text: string; ok: boolean } | null>(
    null
  );

  async function extractFile(file: File, target: "questions" | "answers") {
    setExtracting(target);
    setExtractError("");
    setExtractNote(null);
    try {
      const form = new FormData();
      form.append("file", file);
      const r = await fetch("/api/cmds/questions/extract", { method: "POST", body: form });
      const d = await r.json();
      if (!r.ok || d.error) {
        setExtractError(d.error || "Failed to read file");
        return;
      }
      if (target === "questions") setQuestionsText(d.text || "");
      else setAnswerKeyText(d.text || "");
      if (target === "questions" && d.pictures > 0) {
        setExtractNote({
          target,
          text: `${d.pictures} picture${d.pictures === 1 ? "" : "s"} (tables/diagrams) found in the PDF and placed in their questions — check them in the review step.${d.warning ? " Equations may still need checking." : ""}`,
          ok: true,
        });
      } else if (d.method === "vision") {
        setExtractNote({ target, text: "Transcribed with AI vision — equations should read correctly.", ok: true });
      } else if (d.warning) {
        setExtractNote({ target, text: d.warning, ok: false });
      }
    } catch {
      setExtractError("Failed to read file");
    } finally {
      setExtracting(null);
    }
  }

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
  }, [router]);

  function runParse() {
    const blocks = splitQuestionBlocks(questionsText);
    const answers = parseAnswerKey(answerKeyText);
    const parsed: ParsedRow[] = blocks.map((b) => {
      const split = splitOptions(b.text);
      return {
        num: b.num,
        content: split?.question || b.text,
        options: split?.options || ["", "", "", ""],
        correctLetter: answers[b.num] || "",
        parseOk: !!split,
        raw: b.text,
      };
    });
    setRows(parsed);
    setStep("review");
  }

  function updateRow(i: number, patch: Partial<ParsedRow>) {
    setRows((prev) => (prev ? prev.map((r, idx) => (idx === i ? { ...r, ...patch } : r)) : prev));
  }
  function updateOption(i: number, oi: number, val: string) {
    setRows((prev) =>
      prev
        ? prev.map((r, idx) => (idx === i ? { ...r, options: r.options.map((o, k) => (k === oi ? val : o)) } : r))
        : prev
    );
  }
  function removeRow(i: number) {
    setRows((prev) => (prev ? prev.filter((_, idx) => idx !== i) : prev));
  }

  async function importAll() {
    if (!rows || !session) return;
    setImporting(true);
    let done = 0;
    const errors: string[] = [];
    for (const r of rows) {
      const correctIdx = LETTERS.indexOf(r.correctLetter.toLowerCase());
      if (!r.content.trim()) {
        errors.push(`Q${r.num}: empty question text — skipped`);
        continue;
      }
      if (r.options.some((o) => !o.trim())) {
        errors.push(`Q${r.num}: an option is empty — skipped`);
        continue;
      }
      if (correctIdx === -1) {
        errors.push(`Q${r.num}: no correct answer selected — skipped`);
        continue;
      }
      try {
        const res = await fetch("/api/cmds/questions", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            userId: session.id,
            content: r.content,
            type: "SCQ",
            options: r.options,
            correct: [correctIdx],
            difficulty,
            boardIds: boardIds.length ? boardIds : undefined,
          }),
        });
        const d = await res.json();
        if (!res.ok || d.error) errors.push(`Q${r.num}: ${d.error || "failed"}`);
        else done++;
      } catch {
        errors.push(`Q${r.num}: network error`);
      }
    }
    setResult({ done, failed: rows.length - done, errors });
    setImporting(false);
    setStep("done");
  }

  return (
    <CmdsShell active="resources">
    <main className="mx-auto max-w-5xl px-4 py-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Bulk Import Questions</h1>
          <p className="mt-1 text-slate-500">
            Upload or paste a numbered question paper (options a) b) c) d), (1)…(4) or 1)…4) — two-column PDFs are
            fine) and its answer key — review the parsed result, then import in one go.
          </p>
        </div>
        <Link href="/cmds/questions" className="text-sm text-blue-600 hover:underline">
          ← Question Bank
        </Link>
      </div>

      {step === "input" && (
        <div className="mt-6 space-y-5">
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">
              Tag all imported questions to a chapter (recommended)
            </label>
            <BoardPicker selected={boardIds} onChange={setBoardIds} />
          </div>

          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Difficulty (applied to the whole batch)</label>
            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              className="mt-2 rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-700"
            >
              <option value="EASY">Easy</option>
              <option value="MODERATE">Medium</option>
              <option value="TOUGH">Hard</option>
            </select>
          </div>

          {extractError && (
            <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {extractError}
            </div>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-medium text-slate-700">Questions</label>
                <label className="cursor-pointer rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-100">
                  {extracting === "questions" ? "Reading…" : "Upload file (.pdf/.docx/.txt)"}
                  <input
                    type="file"
                    accept=".pdf,.docx,.txt"
                    className="hidden"
                    onChange={(e) => {
                      const f = e.target.files?.[0];
                      if (f) extractFile(f, "questions");
                      e.target.value = "";
                    }}
                  />
                </label>
              </div>
              {extractNote && extractNote.target === "questions" ? (
                <p className={`mt-1 text-xs ${extractNote.ok ? "text-emerald-600" : "text-amber-600"}`}>
                  {extractNote.text}
                </p>
              ) : (
                <p className="mt-1 text-xs text-slate-400">
                  Upload a file or paste directly, e.g. "1] The displacement... a) ... b) ... c) ... d) ..." — math
                  symbols from PDFs can come through garbled, so double-check those in the review step.
                </p>
              )}
              <textarea
                value={questionsText}
                onChange={(e) => setQuestionsText(e.target.value)}
                rows={16}
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-xs text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder={"1] Question text here\na) option 1\nb) option 2\nc) option 3\nd) option 4\n\n2] ..."}
              />
            </div>
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-medium text-slate-700">Answer key / solutions</label>
                <label className="cursor-pointer rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-100">
                  {extracting === "answers" ? "Reading…" : "Upload file (.xlsx/.csv/.pdf/.docx/.txt)"}
                  <input
                    type="file"
                    accept=".xlsx,.csv,.pdf,.docx,.txt"
                    className="hidden"
                    onChange={(e) => {
                      const f = e.target.files?.[0];
                      if (f) extractFile(f, "answers");
                      e.target.value = "";
                    }}
                  />
                </label>
              </div>
              {extractNote && extractNote.target === "answers" ? (
                <p className={`mt-1 text-xs ${extractNote.ok ? "text-emerald-600" : "text-amber-600"}`}>
                  {extractNote.text}
                </p>
              ) : (
                <p className="mt-1 text-xs text-slate-400">
                  Upload a file or paste directly, e.g. "1] c Sol: ...", a bare list "1] c", or an Excel key (Q.No | 1-4 or
                  A-D).
                </p>
              )}
              <textarea
                value={answerKeyText}
                onChange={(e) => setAnswerKeyText(e.target.value)}
                rows={16}
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-xs text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder={"1] c\n2] d\n3] c ..."}
              />
            </div>
          </div>

          <button
            onClick={runParse}
            disabled={!questionsText.trim()}
            className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
          >
            Parse & Review
          </button>
        </div>
      )}

      {step === "review" && rows && (
        <div className="mt-6 space-y-4">
          <div className="flex items-center justify-between rounded-md bg-blue-50 px-4 py-3 text-sm text-blue-800 ring-1 ring-blue-200">
            <span>
              Parsed {rows.length} question{rows.length === 1 ? "" : "s"} —{" "}
              {rows.filter((r) => !r.parseOk).length > 0 && (
                <span className="font-medium text-amber-700">
                  {rows.filter((r) => !r.parseOk).length} need manual fixing (options not detected).
                </span>
              )}{" "}
              Review everything below before importing.
            </span>
            <button onClick={() => setStep("input")} className="text-blue-700 hover:underline">
              ← Back to paste
            </button>
          </div>

          {rows.map((r, i) => (
            <div
              key={i}
              className={`rounded-xl bg-white p-5 ring-1 ${r.parseOk ? "ring-black/5" : "ring-2 ring-amber-300"}`}
            >
              <div className="flex items-center justify-between">
                <span className="font-semibold text-blue-600">Q{r.num}</span>
                <div className="flex items-center gap-3">
                  {!r.parseOk && (
                    <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-medium text-amber-700">
                      Options not auto-detected — edit below
                    </span>
                  )}
                  <button onClick={() => removeRow(i)} className="text-xs text-slate-400 hover:text-red-500">
                    Remove
                  </button>
                </div>
              </div>

              <textarea
                value={r.content}
                onChange={(e) => updateRow(i, { content: e.target.value })}
                onPaste={(e) => pastePicture(e, r.content, (v) => updateRow(i, { content: v }), setExtractError)}
                rows={2}
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <div className="mt-1">
                <PictureButton fieldRef={{ current: null }} value={r.content} onChange={(v) => updateRow(i, { content: v })} />
              </div>
              {(r.content.includes("$") || r.content.includes("![")) && (
                <div className="mt-1 rounded bg-slate-50 px-2 py-1 text-sm text-slate-700">
                  <MathText>{r.content}</MathText>
                </div>
              )}

              <div className="mt-3 space-y-2">
                {r.options.map((opt, oi) => (
                  <div key={oi} className="flex items-center gap-2">
                    <input
                      type="radio"
                      name={`correct-${i}`}
                      checked={r.correctLetter === LETTERS[oi]}
                      onChange={() => updateRow(i, { correctLetter: LETTERS[oi] })}
                      className="h-4 w-4 accent-emerald-600"
                      title="Mark as correct answer"
                    />
                    <span className="w-5 text-xs font-medium text-slate-400">{LETTERS[oi]})</span>
                    <div className="flex-1">
                      <input
                        value={opt}
                        onChange={(e) => updateOption(i, oi, e.target.value)}
                        onPaste={(e) => pastePicture(e, opt, (v) => updateOption(i, oi, v), setExtractError)}
                        className="w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                      {(opt.includes("$") || opt.includes("![")) && (
                        <div className="mt-1 rounded bg-slate-50 px-2 py-1 text-sm text-slate-700">
                          <MathText>{opt}</MathText>
                        </div>
                      )}
                    </div>
                    <PictureButton compact fieldRef={{ current: null }} value={opt} onChange={(v) => updateOption(i, oi, v)} />
                  </div>
                ))}
              </div>
              {!r.correctLetter && (
                <p className="mt-2 text-xs text-amber-600">
                  No correct answer detected for this question — select one above before importing.
                </p>
              )}
            </div>
          ))}

          <div className="flex items-center gap-3">
            <button
              onClick={importAll}
              disabled={importing || rows.length === 0}
              className="rounded-md bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
            >
              {importing ? "Importing…" : `Import ${rows.length} question${rows.length === 1 ? "" : "s"}`}
            </button>
          </div>
        </div>
      )}

      {step === "done" && result && (
        <div className="mt-6 rounded-xl bg-white p-8 text-center ring-1 ring-black/5">
          <div className="text-4xl">{result.failed === 0 ? "✅" : "⚠️"}</div>
          <h2 className="mt-3 text-xl font-semibold text-slate-800">
            Imported {result.done} of {result.done + result.failed}
          </h2>
          {result.errors.length > 0 && (
            <div className="mx-auto mt-4 max-w-md rounded-md bg-red-50 p-3 text-left text-xs text-red-700 ring-1 ring-red-200">
              {result.errors.map((e, i) => (
                <div key={i}>{e}</div>
              ))}
            </div>
          )}
          <div className="mt-6 flex justify-center gap-3">
            <Link
              href="/cmds/questions"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
            >
              Go to Question Bank
            </Link>
            <button
              onClick={() => {
                setStep("input");
                setRows(null);
                setResult(null);
                setQuestionsText("");
                setAnswerKeyText("");
              }}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
            >
              Import more
            </button>
          </div>
        </div>
      )}
    </main>
    </CmdsShell>
  );
}
