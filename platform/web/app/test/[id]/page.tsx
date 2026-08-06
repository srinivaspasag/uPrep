"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import MathText from "@/components/MathText";

type Question = {
  id: string;
  content: string;
  type: string;
  options: string[];
  board: string; // subject/section name, from the backend's per-board question grouping
};
type TestMeta = {
  id: string;
  name: string;
  durationMin: number;
  totalMarks: number;
  code: string | null;
  resultVisibility: string;
};
type Section = { name: string; questions: Question[] };

// Post-submission review — see app/api/tests/[id]/review/route.ts.
type ReviewQuestion = {
  id: string;
  content: string;
  type: string;
  options: string[];
  chosenIndexes: string[];
  correctIndexes: string[];
  verdict: "CORRECT" | "INCORRECT" | "PARTIAL" | "UNANSWERED";
  score: number;
};
type ReviewSection = { name: string; questions: ReviewQuestion[] };
type Review = { hidden: boolean; totalScore?: number; totalMarks?: number; sections?: ReviewSection[] };

const RESULT_TABS = [
  { key: "details", label: "Test Details" },
  { key: "performance", label: "Your Performance" },
  { key: "answers", label: "Your Answers" },
  { key: "sheet", label: "Result Sheet" },
] as const;
type ResultTab = (typeof RESULT_TABS)[number]["key"];

type Phase = "landing" | "instructions" | "attempt" | "confirm" | "result";
type SubmitResult = {
  graded: boolean;
  total: number;
  judgeable: number;
  correct: number;
  ungraded: number;
  error?: string;
  failedQIds?: string[];
};

// The 5 states a question can be in, matching the real exam palette:
// silver (never opened) / red (opened, no answer) / green (answered) /
// purple (marked for review, no answer) / purple+dot (answered AND marked).
// Only NOT_VISITED/NOT_ANSWERED/MARKED are excluded from grading (no
// answerGiven is sent for them) — ANSWERED_MARKED is graded normally.
type QState = "NOT_VISITED" | "NOT_ANSWERED" | "ANSWERED" | "MARKED" | "ANSWERED_MARKED";

export default function TestPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [test, setTest] = useState<TestMeta | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [visited, setVisited] = useState<Record<string, boolean>>({});
  const [marked, setMarked] = useState<Record<string, boolean>>({});
  const [currentQId, setCurrentQId] = useState<string | null>(null);
  const [showOnlyMarked, setShowOnlyMarked] = useState(false);
  const [instructionsChecked, setInstructionsChecked] = useState(false);
  const [phase, setPhase] = useState<Phase>("landing");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [remaining, setRemaining] = useState(0); // seconds
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<SubmitResult | null>(null);
  const [resumed, setResumed] = useState(false);
  const [alreadyAttempted, setAlreadyAttempted] = useState(false);
  const [saving, setSaving] = useState(false);
  const [resultTab, setResultTab] = useState<ResultTab>("performance");
  const [review, setReview] = useState<Review | null>(null);
  const [reviewLoading, setReviewLoading] = useState(false);

  // Group the flat question list into subject/board sections, preserving the
  // order the backend returned them in (one metadata section per subject).
  const sections: Section[] = useMemo(() => {
    const order: string[] = [];
    const map = new Map<string, Question[]>();
    for (const q of questions) {
      const name = q.board || "General";
      if (!map.has(name)) {
        map.set(name, []);
        order.push(name);
      }
      map.get(name)!.push(q);
    }
    return order.map((name) => ({ name, questions: map.get(name)! }));
  }, [questions]);

  const qMeta = useMemo(() => {
    const m = new Map<string, { sectionIdx: number; indexInSection: number }>();
    sections.forEach((sec, si) => sec.questions.forEach((q, qi) => m.set(q.id, { sectionIdx: si, indexInSection: qi })));
    return m;
  }, [sections]);

  const currentMeta = currentQId ? qMeta.get(currentQId) : undefined;
  const activeSectionIdx = currentMeta?.sectionIdx ?? 0;
  const activeSection = sections[activeSectionIdx];
  const currentQuestion =
    currentMeta && activeSection ? activeSection.questions[currentMeta.indexInSection] : activeSection?.questions[0];

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
    Promise.all([
      fetch(`/api/tests/${params.id}?userId=${encodeURIComponent(s.id)}`).then((r) => r.json()),
      fetch(`/api/tests/${params.id}/progress`).then((r) => r.json()).catch(() => ({ progress: null })),
    ])
      .then(([d, p]) => {
        if (d.error) {
          setError(d.error);
          return;
        }
        setTest(d.test);
        setQuestions(d.questions || []);
        // Resume a paused attempt if one was saved.
        const prog = p?.progress;
        if (prog && prog.answers && Object.keys(prog.answers).length >= 0 && prog.remaining > 0) {
          setAnswers(prog.answers || {});
          setVisited(prog.visited || {});
          setMarked(prog.marked || {});
          setCurrentQId(prog.currentQId || (d.questions || [])[0]?.id || null);
          setRemaining(prog.remaining);
          setResumed(true);
          setPhase("attempt");
        } else if (d.alreadyAttempted) {
          // Legacy allows exactly ONE attempt per test, ever (confirmed in
          // the real backend: AnalyticsManager.isMultiAttemptAllowed() is
          // hardcoded false) — retaking silently fails at the real
          // startAttempt call. Legacy's UI never lets a student reach that:
          // it checks attempt status up front and shows the existing result
          // instead of "Start Test" (Tests.java testPageDirect). Matches
          // that here rather than letting a student redo the whole test
          // only to have the submission rejected at the very end.
          setLoading(false);
          setAlreadyAttempted(true);
          fetch(`/api/tests/${params.id}/my-result?userId=${encodeURIComponent(s.id)}`)
            .then((r) => r.json())
            .then((res) => {
              if (!res.error) setResult(res);
              setPhase("result");
            })
            .catch(() => setPhase("result"));
          return;
        }
      })
      .catch(() => setError("Failed to load test"))
      .finally(() => setLoading(false));
  }, [params.id, router]);

  async function saveProgress(showBusy = false) {
    if (!session || phase !== "attempt") return;
    if (showBusy) setSaving(true);
    try {
      await fetch(`/api/tests/${params.id}/progress`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ answers, remaining, visited, marked, currentQId }),
      });
    } finally {
      if (showBusy) setSaving(false);
    }
  }

  // Autosave progress periodically while attempting.
  useEffect(() => {
    if (phase !== "attempt") return;
    const t = setInterval(() => saveProgress(false), 15000);
    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [phase, answers, remaining, visited, marked, currentQId]);

  async function pauseAndExit() {
    await saveProgress(true);
    router.push("/learn/library");
  }

  useEffect(() => {
    if (phase !== "attempt" || remaining <= 0) return;
    const t = setInterval(() => setRemaining((r) => (r <= 1 ? 0 : r - 1)), 1000);
    return () => clearInterval(t);
  }, [phase, remaining]);

  const answeredCount = useMemo(() => Object.keys(answers).length, [answers]);

  function markVisited(qId: string) {
    setVisited((v) => (v[qId] ? v : { ...v, [qId]: true }));
  }

  function goTo(qId: string) {
    markVisited(qId);
    setCurrentQId(qId);
  }

  function switchSection(si: number) {
    const q = sections[si]?.questions[0];
    if (q) goTo(q.id);
  }

  function nextQuestionId(qId: string): string | null {
    const m = qMeta.get(qId);
    if (!m) return null;
    const sec = sections[m.sectionIdx];
    if (m.indexInSection + 1 < sec.questions.length) return sec.questions[m.indexInSection + 1].id;
    const nextSec = sections[m.sectionIdx + 1];
    return nextSec?.questions[0]?.id || null;
  }

  function saveAndNext() {
    if (!currentQId) return;
    const nxt = nextQuestionId(currentQId);
    if (nxt) goTo(nxt);
  }

  function markForReviewAndNext() {
    if (!currentQId) return;
    setMarked((m) => ({ ...m, [currentQId]: true }));
    const nxt = nextQuestionId(currentQId);
    if (nxt) goTo(nxt);
  }

  // Bug found live: once marked, a question had no way back — "marked"
  // could only ever be set to true, never cleared, anywhere in this file.
  // Stays on the current question rather than navigating, since unmarking
  // is a correction, not a "move on" action like the other three buttons.
  function unmarkForReview() {
    if (!currentQId) return;
    setMarked((m) => {
      const next = { ...m };
      delete next[currentQId];
      return next;
    });
  }

  function clearResponse() {
    if (!currentQId) return;
    setAnswers((a) => {
      const next = { ...a };
      delete next[currentQId];
      return next;
    });
  }

  function stateOf(qId: string): QState {
    const isAnswered = answers[qId] !== undefined;
    const isMarked = !!marked[qId];
    if (isMarked && isAnswered) return "ANSWERED_MARKED";
    if (isMarked) return "MARKED";
    if (isAnswered) return "ANSWERED";
    if (visited[qId]) return "NOT_ANSWERED";
    return "NOT_VISITED";
  }

  const sectionSummaries = useMemo(
    () =>
      sections.map((sec) => {
        let answeredN = 0,
          notAnswered = 0,
          markedN = 0,
          answeredMarked = 0,
          notVisited = 0;
        for (const q of sec.questions) {
          const st = stateOf(q.id);
          if (st === "ANSWERED") answeredN++;
          else if (st === "MARKED") markedN++;
          else if (st === "ANSWERED_MARKED") answeredMarked++;
          else if (st === "NOT_ANSWERED") notAnswered++;
          else notVisited++;
        }
        return {
          name: sec.name,
          total: sec.questions.length,
          answered: answeredN,
          notAnswered,
          marked: markedN,
          answeredMarked,
          notVisited,
        };
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [sections, answers, marked, visited]
  );

  function beginAttempt() {
    setRemaining((test?.durationMin || 0) * 60);
    const firstQ = currentQId || sections[0]?.questions[0]?.id || null;
    if (firstQ) {
      markVisited(firstQ);
      setCurrentQId(firstQ);
    }
    setPhase("attempt");
  }

  async function submit() {
    if (submitting || !session) return;
    setSubmitting(true);
    const payload = {
      userId: session.id,
      answers: questions.map((q) => ({
        qId: q.id,
        // The backend grades by option INDEX (answer keys are stored as
        // index strings, e.g. ["1"]), so send the selected option's index
        // — not its text. Questions that are only "Marked for Review" (no
        // answer chosen) naturally send an empty answerGiven, so the
        // backend won't grade them — matching the real exam rule that a
        // bare review-mark, unlike Answered & Marked for Review, is excluded.
        answerGiven: answers[q.id] !== undefined ? [String(answers[q.id])] : [],
        timeTaken: 0,
      })),
    };
    try {
      const r = await fetch(`/api/tests/${params.id}/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const d = await r.json();
      setResult(d);
      // Attempt finished — discard any saved pause state.
      fetch(`/api/tests/${params.id}/progress`, { method: "DELETE" }).catch(() => {});
    } catch {
      setResult({
        graded: false,
        total: questions.length,
        judgeable: 0,
        correct: 0,
        ungraded: questions.length,
        error: "Failed to submit",
      });
    } finally {
      setSubmitting(false);
      setPhase("result");
    }
  }

  useEffect(() => {
    if (phase === "attempt" && remaining === 0 && test) submit();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [remaining, phase, test]);

  // "Your Answers" / "Result Sheet" data — fetched once on reaching the
  // result screen; the backend itself enforces the resultVisibility gate
  // (returns { hidden: true } rather than the real answers) so this is safe
  // to call unconditionally.
  useEffect(() => {
    if (phase !== "result" || !session) return;
    setReviewLoading(true);
    fetch(`/api/tests/${params.id}/review?userId=${encodeURIComponent(session.id)}`)
      .then((r) => r.json())
      .then((d) => setReview(d))
      .catch(() => setReview({ hidden: true }))
      .finally(() => setReviewLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [phase]);

  const mmss = `${String(Math.floor(remaining / 60)).padStart(2, "0")}:${String(remaining % 60).padStart(2, "0")}`;

  if (loading) return <Centered>Loading test…</Centered>;
  if (error)
    return (
      <Centered>
        <div className="text-red-600">{error}</div>
        <Link href="/learn/library" className="mt-3 text-blue-600 hover:underline">
          ← Back to Library
        </Link>
      </Centered>
    );
  if (!test) return <Centered>Test not found.</Centered>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="mx-auto max-w-5xl px-4 h-14 flex items-center justify-between">
          <Link href="/learn/library" className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold">
              U
            </div>
            <span className="font-semibold text-slate-800">UPrep</span>
          </Link>
          {phase === "attempt" && (
            <div className="flex items-center gap-3">
              <span className="text-sm text-slate-500">
                {answeredCount}/{questions.length} answered
              </span>
              <span className="rounded-md bg-amber-100 px-3 py-1 text-sm font-medium text-amber-700">⏱ {mmss}</span>
              <button
                onClick={pauseAndExit}
                disabled={saving}
                className="rounded-md border border-slate-300 px-3 py-1 text-sm text-slate-600 hover:bg-slate-100 disabled:opacity-60"
              >
                {saving ? "Saving…" : "Pause & exit"}
              </button>
            </div>
          )}
        </div>
      </header>

      <main
        className={`mx-auto px-4 py-8 ${
          phase === "attempt" || phase === "confirm" || phase === "result" ? "max-w-5xl" : "max-w-3xl"
        }`}
      >
        {phase === "landing" && (
          <div className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-black/5">
            <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
              TEST
            </span>
            <h1 className="mt-3 text-2xl font-semibold text-slate-800">{test.name}</h1>
            {test.code && <p className="mt-1 text-sm text-slate-400">Code: {test.code}</p>}
            <div className="mt-6 grid grid-cols-3 gap-4 text-center">
              <Stat label="Questions" value={String(questions.length)} />
              <Stat label="Duration" value={`${test.durationMin} min`} />
              <Stat label="Total Marks" value={String(test.totalMarks)} />
            </div>
            {sections.length > 1 && (
              <div className="mt-5 flex flex-wrap gap-2">
                {sections.map((s) => (
                  <span
                    key={s.name}
                    className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600"
                  >
                    {s.name} · {s.questions.length}
                  </span>
                ))}
              </div>
            )}
            <button
              onClick={() => setPhase("instructions")}
              disabled={questions.length === 0}
              className="mt-8 w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
            >
              {questions.length === 0 ? "No questions available" : "Start Test"}
            </button>
            <Link href="/library" className="mt-4 block text-center text-sm text-slate-500 hover:text-blue-600">
              ← Back to Library
            </Link>
          </div>
        )}

        {phase === "instructions" && (
          <div className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-black/5">
            <h1 className="text-xl font-semibold text-slate-800">Instructions</h1>
            <ul className="mt-4 space-y-3 text-sm text-slate-600">
              <li>
                The clock has been set on the server and the countdown timer at the top will display the time
                remaining. The test will submit itself automatically when the time expires.
              </li>
              <li>
                Each question has a status shown in the question palette on the right, using the following colors:
              </li>
              <li>
                <PaletteLegend />
              </li>
              <li>
                <span className="font-medium text-slate-700">Save &amp; Next</span> saves your chosen option and
                moves to the next question. <span className="font-medium text-slate-700">Mark for Review &amp; Next</span>{" "}
                flags the question to revisit and moves on — it keeps any answer you already selected.
              </li>
              <li>
                Questions left as <span className="font-medium text-purple-600">Marked for Review</span> without an
                answer are <span className="font-medium">not evaluated</span>. Questions that are both{" "}
                <span className="font-medium text-purple-600">answered and marked for review</span> ARE evaluated
                normally.
              </li>
              <li>You can move freely between subjects/sections and any question at any time using the palette.</li>
            </ul>
            <label className="mt-6 flex items-center gap-2 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={instructionsChecked}
                onChange={(e) => setInstructionsChecked(e.target.checked)}
                className="h-4 w-4 accent-blue-600"
              />
              I have read and understood the instructions.
            </label>
            <button
              onClick={beginAttempt}
              disabled={!instructionsChecked}
              className="mt-6 w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
            >
              Proceed
            </button>
          </div>
        )}

        {phase === "attempt" && currentQuestion && (
          <div className="flex flex-col gap-5 lg:flex-row">
            <div className="flex-1 space-y-4">
              {resumed && (
                <div className="rounded-md bg-blue-50 px-3 py-2 text-sm text-blue-700 ring-1 ring-blue-100">
                  Resumed your paused attempt — answers and time were restored.
                </div>
              )}

              {sections.length > 1 && (
                <div className="flex flex-wrap gap-1 border-b border-slate-200">
                  {sections.map((s, si) => (
                    <button
                      key={s.name}
                      onClick={() => switchSection(si)}
                      className={`px-4 py-2 text-sm font-medium -mb-px border-b-2 ${
                        si === activeSectionIdx
                          ? "border-blue-600 text-blue-700"
                          : "border-transparent text-slate-500 hover:text-slate-700"
                      }`}
                    >
                      {s.name}
                    </button>
                  ))}
                </div>
              )}

              <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-black/5">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-blue-600">
                    Q{(currentMeta?.indexInSection ?? 0) + 1}
                    {sections.length > 1 ? ` · ${activeSection?.name}` : ""}
                  </span>
                  {marked[currentQuestion.id] && (
                    <button
                      onClick={unmarkForReview}
                      title="Remove the review mark from this question"
                      className="flex items-center gap-1.5 rounded-full bg-purple-100 px-2.5 py-0.5 text-xs font-medium text-purple-700 hover:bg-purple-200"
                    >
                      Marked for review
                      <span aria-hidden>✕</span>
                    </button>
                  )}
                </div>
                <div className="mt-2 flex gap-2">
                  <MathText className="text-slate-800">{currentQuestion.content}</MathText>
                </div>
                <div className="mt-4 space-y-2">
                  {currentQuestion.options.map((opt, oi) => (
                    <label
                      key={oi}
                      className={`flex items-center gap-3 rounded-md border px-3 py-2 cursor-pointer ${
                        answers[currentQuestion.id] === oi
                          ? "border-blue-500 bg-blue-50"
                          : "border-slate-200 hover:bg-slate-50"
                      }`}
                    >
                      <input
                        type="radio"
                        name={currentQuestion.id}
                        checked={answers[currentQuestion.id] === oi}
                        onChange={() => setAnswers((a) => ({ ...a, [currentQuestion.id]: oi }))}
                      />
                      <MathText className="text-slate-700">{opt}</MathText>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                {marked[currentQuestion.id] && (
                  <button
                    onClick={unmarkForReview}
                    className="rounded-md border border-purple-300 px-4 py-2 text-sm font-medium text-purple-700 hover:bg-purple-50"
                  >
                    Unmark for Review
                  </button>
                )}
                <button
                  onClick={markForReviewAndNext}
                  className="rounded-md bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700"
                >
                  Mark for Review &amp; Next
                </button>
                <button
                  onClick={clearResponse}
                  className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
                >
                  Clear Response
                </button>
                <button
                  onClick={saveAndNext}
                  className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
                >
                  Save &amp; Next
                </button>
                <button
                  onClick={() => setPhase("confirm")}
                  className="ml-auto rounded-md bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700"
                >
                  Submit Test
                </button>
              </div>
            </div>

            <aside className="w-full shrink-0 space-y-3 lg:w-72">
              <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5">
                <PaletteLegend />
                <label className="mt-3 flex items-center gap-2 text-xs text-slate-600">
                  <input
                    type="checkbox"
                    checked={showOnlyMarked}
                    onChange={(e) => setShowOnlyMarked(e.target.checked)}
                    className="h-3.5 w-3.5 accent-purple-600"
                  />
                  Show only marked for review questions
                </label>
                <div className="mt-3 grid grid-cols-6 gap-1.5 lg:grid-cols-5">
                  {(activeSection?.questions || [])
                    .filter((q) => {
                      if (!showOnlyMarked) return true;
                      const st = stateOf(q.id);
                      return st === "MARKED" || st === "ANSWERED_MARKED";
                    })
                    .map((q) => {
                      const idx = qMeta.get(q.id)!.indexInSection;
                      const st = stateOf(q.id);
                      const isCurrent = q.id === currentQuestion.id;
                      return (
                        <button
                          key={q.id}
                          onClick={() => goTo(q.id)}
                          title={STATE_LABEL[st]}
                          className={`relative h-8 w-8 rounded text-xs font-semibold ${
                            isCurrent ? "bg-amber-500 text-white ring-2 ring-amber-600" : STATE_CLASS[st]
                          }`}
                        >
                          {idx + 1}
                          {st === "ANSWERED_MARKED" && !isCurrent && (
                            <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-emerald-400 ring-1 ring-white" />
                          )}
                        </button>
                      );
                    })}
                </div>
              </div>
            </aside>
          </div>
        )}

        {phase === "confirm" && (
          <div className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-black/5">
            <h1 className="text-xl font-semibold text-slate-800">Confirm Submission</h1>
            <p className="mt-1 text-sm text-slate-500">
              Review your section-wise summary before submitting. Once submitted, you can't return to the test.
            </p>
            <div className="mt-5 overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="text-left text-slate-500">
                  <tr>
                    <th className="py-2 pr-3 font-medium">Section</th>
                    <th className="py-2 px-3 font-medium">No. of Questions</th>
                    <th className="py-2 px-3 font-medium">Answered</th>
                    <th className="py-2 px-3 font-medium">Not Answered</th>
                    <th className="py-2 px-3 font-medium">Marked for Review</th>
                    <th className="py-2 px-3 font-medium">Answered &amp; Marked for Review</th>
                    <th className="py-2 px-3 font-medium">Not Visited</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {sectionSummaries.map((s) => (
                    <tr key={s.name}>
                      <td className="py-2 pr-3 font-medium text-slate-700">{s.name}</td>
                      <td className="py-2 px-3">{s.total}</td>
                      <td className="py-2 px-3 text-emerald-700">{s.answered}</td>
                      <td className="py-2 px-3 text-red-600">{s.notAnswered}</td>
                      <td className="py-2 px-3 text-purple-700">{s.marked}</td>
                      <td className="py-2 px-3 text-purple-700">{s.answeredMarked}</td>
                      <td className="py-2 px-3 text-slate-500">{s.notVisited}</td>
                    </tr>
                  ))}
                  <tr className="font-semibold text-slate-800">
                    <td className="py-2 pr-3">Total</td>
                    <td className="py-2 px-3">{sectionSummaries.reduce((s, x) => s + x.total, 0)}</td>
                    <td className="py-2 px-3 text-emerald-700">{sectionSummaries.reduce((s, x) => s + x.answered, 0)}</td>
                    <td className="py-2 px-3 text-red-600">{sectionSummaries.reduce((s, x) => s + x.notAnswered, 0)}</td>
                    <td className="py-2 px-3 text-purple-700">{sectionSummaries.reduce((s, x) => s + x.marked, 0)}</td>
                    <td className="py-2 px-3 text-purple-700">
                      {sectionSummaries.reduce((s, x) => s + x.answeredMarked, 0)}
                    </td>
                    <td className="py-2 px-3 text-slate-500">{sectionSummaries.reduce((s, x) => s + x.notVisited, 0)}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p className="mt-4 text-xs text-slate-400">
              Questions left as "Marked for Review" with no answer selected will not be evaluated. "Answered &amp;
              Marked" questions are evaluated normally.
            </p>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setPhase("attempt")}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
              >
                No, go back
              </button>
              <button
                onClick={submit}
                disabled={submitting}
                className="rounded-md bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
              >
                {submitting ? "Submitting…" : "Yes, Submit"}
              </button>
            </div>
          </div>
        )}

        {phase === "result" && (
          <div className="rounded-xl bg-white shadow-sm ring-1 ring-black/5">
            {/* Matches legacy's real post-test page (Test Details / Your
                Performance / Your Answers / Result Sheet tabs) — this used
                to be a single flat score card with no tabs at all. */}
            <div className="flex flex-wrap border-b border-slate-200 px-4">
              {RESULT_TABS.map((t) => (
                <button
                  key={t.key}
                  onClick={() => setResultTab(t.key)}
                  className={`-mb-px border-b-2 px-4 py-3 text-sm font-medium ${
                    resultTab === t.key
                      ? "border-blue-600 text-blue-700"
                      : "border-transparent text-slate-500 hover:text-slate-700"
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>

            <div className="p-8">
              {resultTab === "details" && (
                <div>
                  <h1 className="text-xl font-semibold text-slate-800">{test.name}</h1>
                  {test.code && <p className="mt-1 text-sm text-slate-400">Code: {test.code}</p>}
                  <div className="mt-6 grid grid-cols-3 gap-4 text-center">
                    <Stat label="Total Questions" value={String(questions.length)} />
                    <Stat label="Time Alloted" value={`${test.durationMin} min`} />
                    <Stat label="Total Marks" value={String(test.totalMarks)} />
                  </div>
                  {sections.length > 1 && (
                    <div className="mt-6 space-y-2">
                      {sections.map((s) => (
                        <div
                          key={s.name}
                          className="flex items-center justify-between rounded-md border border-slate-200 px-4 py-2 text-sm"
                        >
                          <span className="font-medium text-slate-700">{s.name}</span>
                          <span className="text-slate-500">{s.questions.length} Questions</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {resultTab === "performance" && (
                <div className="text-center">
                  {alreadyAttempted && (
                    <p className="mx-auto mb-4 max-w-md rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-500 ring-1 ring-slate-200">
                      You've already completed this test — only one attempt is allowed, so this is showing your
                      existing result.
                    </p>
                  )}
                  {test.resultVisibility === "HIDDEN" ? (
                    <p className="py-10 font-semibold text-slate-600">Results will be displayed later</p>
                  ) : result?.graded ? (
                    <>
                      <div className="text-4xl">🎯</div>
                      <h1 className="mt-3 text-2xl font-semibold text-slate-800">Your Score</h1>
                      <div className="mt-4 text-5xl font-bold text-emerald-600">
                        {result.correct}
                        <span className="text-2xl text-slate-400">/{result.judgeable}</span>
                      </div>
                      <p className="mt-2 text-slate-500">
                        {result.correct} correct out of {result.judgeable} graded
                        {result.ungraded > 0 && ` · ${result.ungraded} pending review`}
                      </p>
                      {!!result.failedQIds?.length && (
                        <p className="mx-auto mt-3 max-w-md rounded-md bg-amber-50 px-3 py-2 text-sm text-amber-700 ring-1 ring-amber-200">
                          {result.failedQIds.length} of your answer{result.failedQIds.length === 1 ? "" : "s"} couldn't
                          be saved due to a connection issue and may be missing from this score. Please contact your
                          instructor if this looks wrong.
                        </p>
                      )}
                    </>
                  ) : (
                    <>
                      <div className="text-4xl">✅</div>
                      <h1 className="mt-3 text-2xl font-semibold text-slate-800">Test Submitted</h1>
                      <p className="mt-2 text-slate-500">
                        You answered {answeredCount} of {questions.length} questions.
                      </p>
                      <p className="mt-1 text-sm text-amber-600">
                        Automatic grading isn’t available for this test yet (answer keys not published).
                      </p>
                    </>
                  )}
                  <RatingWidget entityId={params.id} session={session} />
                </div>
              )}

              {resultTab === "answers" && (
                <ReviewPanel review={review} loading={reviewLoading} mode="answers" />
              )}

              {resultTab === "sheet" && (
                <ReviewPanel review={review} loading={reviewLoading} mode="sheet" />
              )}

              <div className="mt-8 flex justify-center gap-3 border-t border-slate-100 pt-6">
                <Link
                  href="/learn/library"
                  className="rounded-md border border-slate-300 px-4 py-2 text-slate-600 hover:bg-slate-100"
                >
                  Back to Library
                </Link>
                <Link
                  href="/learn/analytics"
                  className="rounded-md bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
                >
                  My Analytics
                </Link>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

const STATE_CLASS: Record<QState, string> = {
  NOT_VISITED: "bg-slate-300 text-slate-700 hover:bg-slate-400",
  NOT_ANSWERED: "bg-red-500 text-white hover:bg-red-600",
  ANSWERED: "bg-emerald-500 text-white hover:bg-emerald-600",
  MARKED: "bg-purple-500 text-white hover:bg-purple-600",
  ANSWERED_MARKED: "bg-purple-500 text-white hover:bg-purple-600",
};
const STATE_LABEL: Record<QState, string> = {
  NOT_VISITED: "Not Visited",
  NOT_ANSWERED: "Not Answered",
  ANSWERED: "Answered",
  MARKED: "Marked for Review",
  ANSWERED_MARKED: "Answered & Marked for Review",
};

function PaletteLegend() {
  const items: { swatch: string; label: string }[] = [
    { swatch: "bg-emerald-500", label: "Answered" },
    { swatch: "bg-amber-500", label: "Current question" },
    { swatch: "bg-purple-500", label: "Marked for review" },
    { swatch: "bg-slate-300", label: "Not visited" },
    { swatch: "bg-red-500", label: "Not answered" },
  ];
  return (
    <div className="grid grid-cols-1 gap-1 text-xs text-slate-600 sm:grid-cols-2">
      {items.map((it) => (
        <div key={it.label} className="flex items-center gap-1.5">
          <span className={`h-3 w-3 rounded-sm ${it.swatch}`} />
          {it.label}
        </div>
      ))}
    </div>
  );
}

function RatingWidget({ entityId, session }: { entityId: string; session: UprepSession | null }) {
  const [mine, setMine] = useState(0);
  const [average, setAverage] = useState(0);
  const [count, setCount] = useState(0);
  const [comment, setComment] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const uid = session?.id || "";
    fetch(`/api/learn/ratings?entityId=${entityId}&userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => {
        setAverage(d.average || 0);
        setCount(d.count || 0);
        if (d.mine) {
          setMine(d.mine.rating || 0);
          setComment(d.mine.comment || "");
        }
      })
      .catch(() => {});
  }, [entityId, session]);

  async function save(rating: number) {
    setMine(rating);
    await fetch("/api/learn/ratings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        entityId,
        entityType: "TEST",
        userId: session?.id,
        userName: [session?.firstName, session?.lastName].filter(Boolean).join(" ") || "Student",
        rating,
        comment,
      }),
    });
    setSaved(true);
  }

  return (
    <div className="mt-8 border-t border-slate-100 pt-6">
      <div className="text-sm font-medium text-slate-600">Rate this test</div>
      <div className="mt-2 flex items-center justify-center gap-1">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            onClick={() => save(n)}
            className={`text-2xl ${n <= mine ? "text-amber-400" : "text-slate-300 hover:text-amber-300"}`}
          >
            ★
          </button>
        ))}
      </div>
      {count > 0 && (
        <div className="mt-1 text-xs text-slate-400">
          Average {average} from {count} rating{count === 1 ? "" : "s"}
        </div>
      )}
      {saved && <div className="mt-2 text-xs text-emerald-600">Thanks for your feedback!</div>}
    </div>
  );
}

const VERDICT_LABEL: Record<ReviewQuestion["verdict"], string> = {
  CORRECT: "Correct",
  INCORRECT: "Incorrect",
  PARTIAL: "Partial",
  UNANSWERED: "Not Answered",
};
const VERDICT_CLASS: Record<ReviewQuestion["verdict"], string> = {
  CORRECT: "bg-emerald-100 text-emerald-700",
  INCORRECT: "bg-red-100 text-red-700",
  PARTIAL: "bg-amber-100 text-amber-700",
  UNANSWERED: "bg-slate-100 text-slate-500",
};

// Backs both the "Your Answers" (full paper, correct answer revealed) and
// "Result Sheet" (per-question marks table) tabs — same data, two legacy
// views of it.
function ReviewPanel({
  review,
  loading,
  mode,
}: {
  review: Review | null;
  loading: boolean;
  mode: "answers" | "sheet";
}) {
  if (loading) return <div className="py-10 text-center text-slate-400">Loading…</div>;
  if (!review || review.hidden)
    return <p className="py-10 text-center font-semibold text-slate-600">Results will be displayed later</p>;
  if (!review.sections?.length)
    return <div className="py-10 text-center text-slate-400">Nothing to show.</div>;

  if (mode === "sheet") {
    return (
      <div className="space-y-6">
        {review.sections.map((sec) => (
          <div key={sec.name}>
            <h3 className="mb-2 text-sm font-semibold text-slate-700">{sec.name}</h3>
            <table className="w-full text-sm">
              <thead className="text-left text-xs uppercase tracking-wide text-slate-500">
                <tr className="border-b border-slate-200">
                  <th className="py-2 pr-3 font-medium">Q#</th>
                  <th className="py-2 px-3 font-medium">Status</th>
                  <th className="py-2 px-3 font-medium text-right">Marks Obtained</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sec.questions.map((q, i) => (
                  <tr key={q.id}>
                    <td className="py-2 pr-3 text-slate-600">Q{i + 1}</td>
                    <td className="py-2 px-3">
                      <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${VERDICT_CLASS[q.verdict]}`}>
                        {VERDICT_LABEL[q.verdict]}
                      </span>
                    </td>
                    <td
                      className={`py-2 px-3 text-right font-medium ${
                        q.score > 0 ? "text-emerald-700" : q.score < 0 ? "text-red-600" : "text-slate-500"
                      }`}
                    >
                      {q.score > 0 ? `+${q.score}` : q.score}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
        <div className="flex justify-end border-t border-slate-200 pt-3 text-sm font-semibold text-slate-800">
          Total: {review.totalScore} / {review.totalMarks}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {review.sections.map((sec) => (
        <div key={sec.name}>
          <h3 className="mb-3 text-sm font-semibold text-slate-700">{sec.name}</h3>
          <div className="space-y-4">
            {sec.questions.map((q, i) => (
              <div key={q.id} className="rounded-lg border border-slate-200 p-4 text-left">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-blue-600">Q{i + 1}</span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${VERDICT_CLASS[q.verdict]}`}>
                    {VERDICT_LABEL[q.verdict]}
                  </span>
                </div>
                <div className="mt-2">
                  <MathText className="text-slate-800">{q.content}</MathText>
                </div>
                <div className="mt-3 space-y-1.5">
                  {q.options.map((opt, oi) => {
                    const idxStr = String(oi);
                    const isCorrect = q.correctIndexes.includes(idxStr);
                    const isChosen = q.chosenIndexes.includes(idxStr);
                    const cls = isCorrect
                      ? "border-emerald-400 bg-emerald-50"
                      : isChosen
                      ? "border-red-400 bg-red-50"
                      : "border-slate-200";
                    return (
                      <div key={oi} className={`flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm ${cls}`}>
                        {isCorrect && <span className="text-emerald-600">✓</span>}
                        {!isCorrect && isChosen && <span className="text-red-600">✕</span>}
                        <MathText className="text-slate-700">{opt}</MathText>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-slate-50 py-4">
      <div className="text-xl font-semibold text-slate-800">{value}</div>
      <div className="text-xs text-slate-500">{label}</div>
    </div>
  );
}

function Centered({ children }: { children: React.ReactNode }) {
  return <div className="min-h-screen flex flex-col items-center justify-center text-slate-500">{children}</div>;
}
