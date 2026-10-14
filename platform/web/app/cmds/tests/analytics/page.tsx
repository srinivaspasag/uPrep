"use client";

import { Fragment, useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession } from "@/lib/session";
import MathText from "@/components/MathText";

type TestRow = { testId: string; name: string; attempts: number; students: number; lastAt: number };
type Subject = { name: string; questionCount: number; totalMarks: number; topics: string[] };
type Overall = {
  attempts: number;
  students: number;
  avgScore: number;
  avgPercent: number;
  avgTimeSec: number;
  highScore: number;
  lowScore: number;
};
type QuestionStudent = { userId: string; name: string; memberId: string; timeSec: number };
type PerQuestion = {
  qId: string;
  label: string;
  section: string | null;
  content: string;
  type: string;
  options: string[];
  correctIndexes: string[];
  attempts: number;
  correct: number;
  incorrect: number;
  partial: number;
  ungraded: number;
  notAttempted: number;
  correctPercent: number;
  avgTimeSec: number;
  correctStudents: QuestionStudent[];
  wrongStudents: QuestionStudent[];
};
type SubjectCWL = { name: string; correct: number; wrong: number; left: number; total: number; score: number };
type ResultRow = {
  userId: string;
  name: string;
  memberId: string;
  score: number;
  percent: number;
  attempts: number;
  lastAt: number;
  timeSec: number;
  subjects: SubjectCWL[];
  rank: number;
  totalStudents: number;
};
type TopPerformer = { name: string; memberId: string; score: number; percent: number };
type DistBucket = { label: string; count: number; percentOfStudents: number };
type StudentQuestion = {
  qId: string;
  label: string;
  verdict: string;
  marks: { positive: number; negative: number } | null;
  timeSec: number;
};
type StudentDetail = ResultRow & { questions: StudentQuestion[] };

// mm:ss for sub-minute precision where it matters (per-question), plain
// "Xm" for test-level averages where seconds are just noise.
function fmtTime(sec: number): string {
  const s = Math.round(sec);
  if (s < 60) return `${s}s`;
  if (s < 3600) {
    const m = Math.floor(s / 60);
    const rem = s % 60;
    return rem > 0 ? `${m}m ${rem}s` : `${m}m`;
  }
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  return `${h}:${String(m).padStart(2, "0")} hrs`;
}

const LETTERS = ["A", "B", "C", "D", "E", "F", "G", "H"];
type Detail = {
  test: { id: string; name: string; totalMarks: number; durationMin: number; totalQuestions: number };
  overall: Overall;
  subjects: Subject[];
  topPerformers: TopPerformer[];
  distribution: DistBucket[];
  perQuestion: PerQuestion[];
  resultSheet: ResultRow[];
};
type InProgress = {
  userId: string;
  name: string;
  memberId: string;
  answered: number;
  remaining: number | null;
  updatedAt: number;
};

export default function TestAnalyticsPage() {
  const [tests, setTests] = useState<TestRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<string | null>(null);
  const [detail, setDetail] = useState<Detail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [tab, setTab] = useState<"details" | "overview" | "questions" | "results" | "monitor">("details");
  const [inProgress, setInProgress] = useState<InProgress[]>([]);
  const [minPct, setMinPct] = useState("");
  const [maxPct, setMaxPct] = useState("");
  const [busy, setBusy] = useState(false);
  const [studentsFor, setStudentsFor] = useState<{ question: PerQuestion; kind: "correct" | "wrong" } | null>(null);
  const [solutionFor, setSolutionFor] = useState<PerQuestion | null>(null);
  // Matches legacy's "Most/Least Attempted" and "Most/Least Correct" question
  // sort (AnalyticsManager.getQuestionSetOrderQuery — sorts by attempts or
  // correct count, asc/desc). We already compute both per question, so this
  // is a pure client-side resort, no new data needed.
  const [qSort, setQSort] = useState<"default" | "mostAttempted" | "leastAttempted" | "mostCorrect" | "leastCorrect">(
    "default"
  );
  const [resultView, setResultView] = useState<"overall" | "cwl">("overall");
  const [subjectFilter, setSubjectFilter] = useState("all");
  const [studentOpen, setStudentOpen] = useState(false);
  const [studentDetail, setStudentDetail] = useState<StudentDetail | null>(null);
  const [studentLoading, setStudentLoading] = useState(false);
  const isAdmin = (getSession()?.profile || "").trim().toUpperCase() === "MANAGER";

  async function openStudent(userId: string) {
    if (!selected) return;
    setStudentOpen(true);
    setStudentLoading(true);
    setStudentDetail(null);
    const d = await (
      await fetch(`/api/cmds/tests/analytics?testId=${encodeURIComponent(selected)}&userId=${encodeURIComponent(userId)}`)
    ).json();
    setStudentDetail(d.student || null);
    setStudentLoading(false);
  }

  const subjectNames = useMemo(() => {
    if (!detail) return [];
    return Array.from(new Set(detail.perQuestion.map((q) => q.section).filter(Boolean))) as string[];
  }, [detail]);

  const sortedQuestions = useMemo(() => {
    if (!detail) return [];
    const list = detail.perQuestion.filter((q) => subjectFilter === "all" || q.section === subjectFilter);
    switch (qSort) {
      case "mostAttempted":
        return list.sort((a, b) => b.attempts - a.attempts);
      case "leastAttempted":
        return list.sort((a, b) => a.attempts - b.attempts);
      case "mostCorrect":
        return list.sort((a, b) => b.correct - a.correct);
      case "leastCorrect":
        return list.sort((a, b) => a.correct - b.correct);
      default:
        return list;
    }
  }, [detail, qSort, subjectFilter]);

  useEffect(() => {
    fetch("/api/cmds/tests/analytics")
      .then((r) => r.json())
      .then((d) => {
        setTests(d.tests || []);
        // Deep link from the Program Analytics tab's "Open →" link.
        const sp = new URLSearchParams(window.location.search);
        const deepLinkId = sp.get("testId");
        if (deepLinkId) open(deepLinkId);
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadMonitor(testId: string) {
    const d = await (await fetch(`/api/cmds/tests/attempts?testId=${encodeURIComponent(testId)}`)).json();
    setInProgress(d.inProgress || []);
  }

  async function open(testId: string) {
    setSelected(testId);
    setDetail(null);
    setDetailLoading(true);
    setTab("details");
    setMinPct("");
    setMaxPct("");
    setSubjectFilter("all");
    setResultView("overall");
    const d = await (await fetch(`/api/cmds/tests/analytics?testId=${encodeURIComponent(testId)}`)).json();
    setDetail(d);
    setDetailLoading(false);
    loadMonitor(testId);
  }

  async function attemptAction(action: "reset" | "end", userId: string, name: string) {
    if (!selected) return;
    const verb = action === "reset" ? "reset (delete) all attempts for" : "end the in-progress test of";
    if (!window.confirm(`Are you sure you want to ${verb} ${name}?`)) return;
    setBusy(true);
    await fetch("/api/cmds/tests/attempts", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ action, testId: selected, userId }),
    });
    setBusy(false);
    // Refresh detail + monitor to reflect the change.
    const d = await (await fetch(`/api/cmds/tests/analytics?testId=${encodeURIComponent(selected)}`)).json();
    setDetail(d);
    loadMonitor(selected);
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1100px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Test Analytics</h1>
        <p className="mt-1 text-sm text-slate-500">
          Overall performance, per-question analysis, and the result sheet for every test your students
          have attempted.
        </p>

        <div className="mt-6 flex gap-6">
          {/* Test list */}
          <aside className="w-[280px] shrink-0">
            <div className="rounded border border-slate-200">
              <div className="border-b border-slate-200 bg-slate-50 px-4 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                Tests with attempts
              </div>
              {loading ? (
                <div className="px-4 py-8 text-center text-sm text-slate-400">Loading…</div>
              ) : tests.length === 0 ? (
                <div className="px-4 py-8 text-center text-sm text-slate-400">
                  No attempts recorded yet.
                </div>
              ) : (
                <ul className="max-h-[70vh] overflow-auto">
                  {tests.map((t) => (
                    <li key={t.testId}>
                      <button
                        onClick={() => open(t.testId)}
                        className={`block w-full border-b border-slate-100 px-4 py-3 text-left hover:bg-slate-50 ${
                          selected === t.testId ? "bg-indigo-50" : ""
                        }`}
                      >
                        <div className="text-sm font-medium text-slate-700">{t.name}</div>
                        <div className="mt-0.5 text-xs text-slate-400">
                          {t.attempts} attempt{t.attempts === 1 ? "" : "s"} · {t.students} student
                          {t.students === 1 ? "" : "s"}
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </aside>

          {/* Detail */}
          <main className="flex-1">
            {!selected ? (
              <div className="rounded border border-dashed border-slate-200 py-20 text-center text-sm text-slate-400">
                Pick a test on the left to see its analytics.
              </div>
            ) : detailLoading || !detail ? (
              <div className="py-20 text-center text-sm text-slate-400">Loading analytics…</div>
            ) : (
              <div>
                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-slate-800">{detail.test.name}</h2>
                  <button
                    onClick={() => selected && open(selected)}
                    title="Analytics here are computed fresh from attempt records on every load — this just re-fetches."
                    className="rounded border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-100"
                  >
                    ↻ Refresh
                  </button>
                </div>
                <div className="mt-4 flex gap-6 border-b border-slate-200 text-sm">
                  {(["details", "overview", "questions", "results", "monitor"] as const).map((k) => (
                    <button
                      key={k}
                      onClick={() => setTab(k)}
                      className={`-mb-px border-b-2 pb-2 capitalize ${
                        tab === k
                          ? "border-emerald-500 font-semibold text-slate-900"
                          : "border-transparent text-slate-500 hover:text-slate-700"
                      }`}
                    >
                      {k === "details"
                        ? "Test details"
                        : k === "questions"
                        ? "Per-question"
                        : k === "results"
                        ? "Result sheet"
                        : k === "monitor"
                        ? `In progress${inProgress.length ? ` (${inProgress.length})` : ""}`
                        : "Overall performance"}
                    </button>
                  ))}
                </div>

                {tab === "details" && (
                  <div className="mt-5 space-y-6">
                    <div className="grid grid-cols-3 gap-4">
                      <Stat label="Total marks" value={detail.test.totalMarks} />
                      <Stat label="Time alloted" value={`${detail.test.durationMin} min`} />
                      <Stat label="Total questions" value={detail.test.totalQuestions} />
                    </div>

                    {detail.subjects.length === 0 ? (
                      <p className="text-sm text-slate-400">No subject breakdown available for this test.</p>
                    ) : (
                      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {detail.subjects.map((s) => (
                          <div key={s.name} className="rounded-lg border border-slate-200 p-4">
                            <h3 className="text-sm font-semibold text-blue-700">{s.name}</h3>
                            <div className="mt-2 text-xs font-medium uppercase tracking-wide text-slate-400">
                              Format
                            </div>
                            <p className="mt-1 text-sm text-slate-600">
                              <span className="font-semibold text-slate-800">{s.questionCount}</span> Questions ·{" "}
                              <span className="font-semibold text-slate-800">{s.totalMarks}</span> Marks
                            </p>
                            <div className="mt-3 border-t border-slate-100 pt-3">
                              <div className="text-xs font-medium uppercase tracking-wide text-slate-400">
                                Curriculum
                              </div>
                              {s.topics.length === 0 ? (
                                <p className="mt-1 text-sm text-slate-400">Not tagged to a chapter.</p>
                              ) : (
                                <ul className="mt-1 space-y-0.5 text-sm text-slate-600">
                                  {s.topics.map((t) => (
                                    <li key={t}>{t}</li>
                                  ))}
                                </ul>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {tab === "overview" && (
                  <div className="mt-5 space-y-6">
                    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                      <Stat label="Attempts" value={detail.overall.attempts} />
                      <Stat label="Students" value={detail.overall.students} />
                      <Stat label="Total marks" value={detail.test.totalMarks} />
                      <Stat label="Average score" value={detail.overall.avgScore} />
                      <Stat label="Average %" value={`${detail.overall.avgPercent}%`} />
                      <Stat label="Average time" value={fmtTime(detail.overall.avgTimeSec)} />
                      <Stat
                        label="High / Low"
                        value={`${detail.overall.highScore} / ${detail.overall.lowScore}`}
                      />
                    </div>

                    <div className="grid gap-6 lg:grid-cols-2">
                      {/* Top performers */}
                      <div className="rounded-lg border border-slate-200 p-4">
                        <h3 className="text-sm font-semibold text-slate-600">Top performers</h3>
                        {detail.topPerformers.length === 0 ? (
                          <p className="mt-3 text-sm text-slate-400">No results yet.</p>
                        ) : (
                          <ol className="mt-3 space-y-2">
                            {detail.topPerformers.map((p, i) => (
                              <li key={i} className="flex items-center justify-between text-sm">
                                <span className="flex items-center gap-2">
                                  <span
                                    className={`flex h-5 w-5 items-center justify-center rounded-full text-[11px] font-bold ${
                                      i === 0
                                        ? "bg-amber-100 text-amber-700"
                                        : i === 1
                                        ? "bg-slate-200 text-slate-600"
                                        : i === 2
                                        ? "bg-orange-100 text-orange-700"
                                        : "bg-slate-100 text-slate-500"
                                    }`}
                                  >
                                    {i + 1}
                                  </span>
                                  <span className="font-medium text-slate-700">{p.name}</span>
                                  {p.memberId && <span className="text-xs text-slate-400">({p.memberId})</span>}
                                </span>
                                <span className="text-slate-600">
                                  {p.score}
                                  <span className="text-slate-400"> · {p.percent}%</span>
                                </span>
                              </li>
                            ))}
                          </ol>
                        )}
                      </div>

                      {/* % students vs marks distribution */}
                      <div className="rounded-lg border border-slate-200 p-4">
                        <h3 className="text-sm font-semibold text-slate-600">
                          Students vs marks distribution
                        </h3>
                        <div className="mt-4 space-y-2">
                          {detail.distribution.map((b) => (
                            <div key={b.label} className="flex items-center gap-3 text-xs">
                              <span className="w-16 shrink-0 text-slate-500">{b.label}</span>
                              <div className="h-3 flex-1 overflow-hidden rounded bg-slate-100">
                                <div
                                  className="h-full bg-indigo-500"
                                  style={{ width: `${b.percentOfStudents}%` }}
                                />
                              </div>
                              <span className="w-24 shrink-0 text-right text-slate-500">
                                {b.count} ({b.percentOfStudents}%)
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {tab === "questions" && (
                  <div className="mt-5">
                    <div className="flex items-center gap-4">
                      <label className="text-xs text-slate-500">
                        Filter by{" "}
                        <select
                          value={subjectFilter}
                          onChange={(e) => setSubjectFilter(e.target.value)}
                          className="ml-1 rounded border border-slate-300 px-2 py-1 text-xs text-slate-700 outline-none focus:border-slate-500"
                        >
                          <option value="all">All subjects</option>
                          {subjectNames.map((s) => (
                            <option key={s} value={s}>
                              {s}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label className="text-xs text-slate-500">
                        Sort by{" "}
                        <select
                          value={qSort}
                          onChange={(e) => setQSort(e.target.value as typeof qSort)}
                          className="ml-1 rounded border border-slate-300 px-2 py-1 text-xs text-slate-700 outline-none focus:border-slate-500"
                        >
                          <option value="default">Question order</option>
                          <option value="mostAttempted">Most attempted</option>
                          <option value="leastAttempted">Least attempted</option>
                          <option value="mostCorrect">Most correct</option>
                          <option value="leastCorrect">Least correct</option>
                        </select>
                      </label>
                    </div>
                    <div className="mt-2 overflow-hidden rounded border border-slate-200">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                          <th className="px-4 py-2 font-medium">Q</th>
                          <th className="px-4 py-2 font-medium">Attempts</th>
                          <th className="px-4 py-2 font-medium">Correct</th>
                          <th className="px-4 py-2 font-medium">Incorrect</th>
                          <th className="px-4 py-2 font-medium">Partial</th>
                          <th className="px-4 py-2 font-medium">Ungraded</th>
                          <th className="px-4 py-2 font-medium">Not attempted</th>
                          <th className="px-4 py-2 font-medium">Avg time</th>
                          <th className="px-4 py-2 font-medium">% Correct</th>
                          <th className="px-4 py-2 font-medium" />
                        </tr>
                      </thead>
                      <tbody>
                        {sortedQuestions.length === 0 ? (
                          <tr>
                            <td colSpan={10} className="px-4 py-8 text-center text-slate-400">
                              No question data.
                            </td>
                          </tr>
                        ) : (
                          sortedQuestions.map((q) => (
                            <tr key={q.qId} className="border-b border-slate-100">
                              <td className="px-4 py-2 font-medium text-slate-700">{q.label}</td>
                              <td className="px-4 py-2 text-slate-500">{q.attempts}</td>
                              <td className="px-4 py-2">
                                {q.correctStudents.length > 0 ? (
                                  <button
                                    onClick={() => setStudentsFor({ question: q, kind: "correct" })}
                                    className="font-medium text-emerald-600 hover:underline"
                                    title="See who answered correctly, with time taken"
                                  >
                                    {q.correct}
                                  </button>
                                ) : (
                                  <span className="text-emerald-600">{q.correct}</span>
                                )}
                              </td>
                              <td className="px-4 py-2">
                                {q.wrongStudents.length > 0 ? (
                                  <button
                                    onClick={() => setStudentsFor({ question: q, kind: "wrong" })}
                                    className="font-medium text-red-500 hover:underline"
                                    title="See who answered incorrectly, with time taken"
                                  >
                                    {q.incorrect}
                                  </button>
                                ) : (
                                  <span className="text-red-500">{q.incorrect}</span>
                                )}
                              </td>
                              <td className="px-4 py-2 text-amber-600">{q.partial}</td>
                              <td className="px-4 py-2 text-slate-400">{q.ungraded}</td>
                              <td className="px-4 py-2 text-slate-400">{q.notAttempted}</td>
                              <td className="px-4 py-2 text-slate-500">{fmtTime(q.avgTimeSec)}</td>
                              <td className="px-4 py-2">
                                <div className="flex items-center gap-2">
                                  <div className="h-1.5 w-24 overflow-hidden rounded bg-slate-100">
                                    <div
                                      className="h-full bg-emerald-500"
                                      style={{ width: `${q.correctPercent}%` }}
                                    />
                                  </div>
                                  <span className="text-xs text-slate-500">{q.correctPercent}%</span>
                                </div>
                              </td>
                              <td className="px-4 py-2">
                                <button
                                  onClick={() => setSolutionFor(q)}
                                  className="text-xs text-blue-600 hover:underline"
                                >
                                  View solution
                                </button>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                    </div>
                  </div>
                )}

                {tab === "results" && (
                  <div className="mt-5">
                    <div className="flex flex-wrap items-end justify-between gap-3">
                      <div className="flex items-end gap-3">
                        <label className="text-xs text-slate-500">
                          View
                          <select
                            value={resultView}
                            onChange={(e) => setResultView(e.target.value as typeof resultView)}
                            className="mt-1 block rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
                          >
                            <option value="overall">Overall Results</option>
                            <option value="cwl">CWL Analytics</option>
                          </select>
                        </label>
                        <label className="text-xs text-slate-500">
                          Min %
                          <input
                            type="number"
                            value={minPct}
                            onChange={(e) => setMinPct(e.target.value)}
                            placeholder="0"
                            className="mt-1 block w-20 rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
                          />
                        </label>
                        <label className="text-xs text-slate-500">
                          Max %
                          <input
                            type="number"
                            value={maxPct}
                            onChange={(e) => setMaxPct(e.target.value)}
                            placeholder="100"
                            className="mt-1 block w-20 rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
                          />
                        </label>
                        {(minPct || maxPct) && (
                          <button
                            onClick={() => {
                              setMinPct("");
                              setMaxPct("");
                            }}
                            className="pb-1 text-xs text-slate-400 hover:text-slate-600"
                          >
                            clear
                          </button>
                        )}
                      </div>
                      <a
                        href={`/api/cmds/tests/analytics/export?testId=${encodeURIComponent(detail.test.id)}`}
                        className="rounded border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-100"
                      >
                        Download CSV
                      </a>
                      <button
                        onClick={() => window.print()}
                        className="rounded border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-100"
                      >
                        Print Result Sheet
                      </button>
                    </div>

                    <style>{`
                      @media print {
                        body * { visibility: hidden; }
                        #result-sheet-print, #result-sheet-print * { visibility: visible; }
                        #result-sheet-print { position: absolute; top: 0; left: 0; width: 100%; }
                      }
                    `}</style>
                    <div id="result-sheet-print" className="mt-3 overflow-x-auto rounded border border-slate-200">
                      <h2 className="hidden bg-white px-4 py-3 text-lg font-semibold text-slate-800 print:block">
                        {detail.test.name} — Result Sheet
                      </h2>
                      {(() => {
                        const lo = minPct === "" ? -Infinity : Number(minPct);
                        const hi = maxPct === "" ? Infinity : Number(maxPct);
                        const rows = detail.resultSheet.filter((r) => r.percent >= lo && r.percent <= hi);
                        const subjectCols = detail.subjects.map((s) => s.name);

                        if (resultView === "cwl") {
                          return (
                            <table className="w-full min-w-[720px] text-sm">
                              <thead>
                                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                                  <th rowSpan={2} className="px-4 py-2 align-bottom font-medium">Rank</th>
                                  <th rowSpan={2} className="px-4 py-2 align-bottom font-medium">Student</th>
                                  <th rowSpan={2} className="px-4 py-2 align-bottom font-medium text-right">
                                    Total score
                                  </th>
                                  {subjectCols.map((s) => (
                                    <th key={s} colSpan={4} className="border-l border-slate-200 px-2 py-1 text-center font-medium text-blue-600">
                                      {s}
                                    </th>
                                  ))}
                                </tr>
                                <tr className="border-b border-slate-200 bg-slate-50 text-left text-[10px] uppercase tracking-wide text-slate-400">
                                  {subjectCols.map((s) => (
                                    <Fragment key={s}>
                                      <th className="border-l border-slate-200 px-2 py-1 font-medium">C</th>
                                      <th className="px-2 py-1 font-medium">W</th>
                                      <th className="px-2 py-1 font-medium">L</th>
                                      <th className="px-2 py-1 font-medium">T</th>
                                    </Fragment>
                                  ))}
                                </tr>
                              </thead>
                              <tbody>
                                {rows.length === 0 ? (
                                  <tr>
                                    <td colSpan={3 + subjectCols.length * 4} className="px-4 py-8 text-center text-slate-400">
                                      No students in this range.
                                    </td>
                                  </tr>
                                ) : (
                                  rows.map((r) => (
                                    <tr key={r.userId} className="border-b border-slate-100 hover:bg-slate-50">
                                      <td className="px-4 py-2 text-slate-400">{r.rank}</td>
                                      <td className="px-4 py-2 font-medium">
                                        <button
                                          onClick={() => openStudent(r.userId)}
                                          className="text-blue-600 hover:underline print:pointer-events-none print:text-slate-700"
                                        >
                                          {r.name}
                                        </button>
                                        {r.memberId && <span className="ml-1 text-xs text-slate-400">({r.memberId})</span>}
                                      </td>
                                      <td className="px-4 py-2 text-right font-medium text-slate-700">{r.score}</td>
                                      {subjectCols.map((s) => {
                                        const sub = r.subjects.find((x) => x.name === s);
                                        return (
                                          <Fragment key={s}>
                                            <td className="border-l border-slate-100 px-2 py-2 text-center text-emerald-600">
                                              {sub?.correct ?? 0}
                                            </td>
                                            <td className="px-2 py-2 text-center text-red-500">
                                              {sub?.wrong ?? 0}
                                            </td>
                                            <td className="px-2 py-2 text-center text-slate-400">
                                              {sub?.left ?? 0}
                                            </td>
                                            <td className="px-2 py-2 text-center text-slate-500">
                                              {sub?.total ?? 0}
                                            </td>
                                          </Fragment>
                                        );
                                      })}
                                    </tr>
                                  ))
                                )}
                              </tbody>
                            </table>
                          );
                        }

                        return (
                          <table className="w-full min-w-[720px] text-sm">
                            <thead>
                              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                                <th className="px-4 py-2 font-medium">Rank</th>
                                <th className="px-4 py-2 font-medium">Student</th>
                                <th className="px-4 py-2 font-medium">ID</th>
                                <th className="px-4 py-2 font-medium">Time taken</th>
                                {subjectCols.map((s) => (
                                  <th key={s} className="px-4 py-2 text-right font-medium text-blue-600">
                                    {s}
                                  </th>
                                ))}
                                <th className="px-4 py-2 text-right font-medium">Total score</th>
                                <th className="px-4 py-2 font-medium">%</th>
                                <th className="px-4 py-2 font-medium">Attempts</th>
                                <th className="px-4 py-2 font-medium">Last attempt</th>
                                {isAdmin && <th className="px-4 py-2 font-medium print:hidden" />}
                              </tr>
                            </thead>
                            <tbody>
                              {rows.length === 0 ? (
                                <tr>
                                  <td
                                    colSpan={8 + subjectCols.length + (isAdmin ? 1 : 0)}
                                    className="px-4 py-8 text-center text-slate-400"
                                  >
                                    No students in this range.
                                  </td>
                                </tr>
                              ) : (
                                rows.map((r) => (
                                  <tr key={r.userId} className="border-b border-slate-100 hover:bg-slate-50">
                                    <td className="px-4 py-2 text-slate-400">
                                      {r.rank} <span className="text-slate-300">/ {r.totalStudents}</span>
                                    </td>
                                    <td className="px-4 py-2 font-medium">
                                      <button
                                        onClick={() => openStudent(r.userId)}
                                        className="text-blue-600 hover:underline print:pointer-events-none print:text-slate-700"
                                      >
                                        {r.name}
                                      </button>
                                    </td>
                                    <td className="px-4 py-2 text-slate-500">{r.memberId || "—"}</td>
                                    <td className="px-4 py-2 text-slate-500">{r.timeSec ? fmtTime(r.timeSec) : "—"}</td>
                                    {subjectCols.map((s) => {
                                      const sub = r.subjects.find((x) => x.name === s);
                                      return (
                                        <td key={s} className="px-4 py-2 text-right text-slate-600">
                                          {sub ? sub.score : "—"}
                                        </td>
                                      );
                                    })}
                                    <td className="px-4 py-2 text-right text-slate-700">
                                      {r.score}
                                      <span className="text-slate-400"> / {detail.test.totalMarks}</span>
                                    </td>
                                    <td className="px-4 py-2 text-slate-500">{r.percent}%</td>
                                    <td className="px-4 py-2 text-slate-500">{r.attempts}</td>
                                    <td className="px-4 py-2 text-slate-400">
                                      {r.lastAt ? new Date(r.lastAt).toLocaleString() : "—"}
                                    </td>
                                    {isAdmin && (
                                      <td className="px-4 py-2 print:hidden">
                                        <button
                                          disabled={busy}
                                          onClick={() => attemptAction("reset", r.userId, r.name)}
                                          className="rounded px-2 py-1 text-xs text-red-500 hover:bg-red-50 disabled:opacity-50"
                                          title="Delete this student's attempts so they can retake"
                                        >
                                          Reset
                                        </button>
                                      </td>
                                    )}
                                  </tr>
                                ))
                              )}
                            </tbody>
                          </table>
                        );
                      })()}
                    </div>
                  </div>
                )}

                {tab === "monitor" && (
                  <div className="mt-5">
                    <div className="flex items-center justify-between">
                      <p className="text-sm text-slate-500">
                        Students with a paused / in-progress attempt on this test.
                      </p>
                      <button
                        onClick={() => selected && loadMonitor(selected)}
                        className="rounded border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-100"
                      >
                        Refresh
                      </button>
                    </div>
                    <div className="mt-3 overflow-hidden rounded border border-slate-200">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                            <th className="px-4 py-2 font-medium">Student</th>
                            <th className="px-4 py-2 font-medium">ID</th>
                            <th className="px-4 py-2 font-medium">Answered</th>
                            <th className="px-4 py-2 font-medium">Time left</th>
                            <th className="px-4 py-2 font-medium">Last activity</th>
                            {isAdmin && <th className="px-4 py-2 font-medium" />}
                          </tr>
                        </thead>
                        <tbody>
                          {inProgress.length === 0 ? (
                            <tr>
                              <td colSpan={isAdmin ? 6 : 5} className="px-4 py-8 text-center text-slate-400">
                                No students in progress.
                              </td>
                            </tr>
                          ) : (
                            inProgress.map((s) => (
                              <tr key={s.userId} className="border-b border-slate-100">
                                <td className="px-4 py-2 font-medium text-slate-700">{s.name}</td>
                                <td className="px-4 py-2 text-slate-500">{s.memberId || "—"}</td>
                                <td className="px-4 py-2 text-slate-500">{s.answered}</td>
                                <td className="px-4 py-2 text-slate-500">
                                  {s.remaining != null ? `${Math.round(s.remaining / 60)} min` : "—"}
                                </td>
                                <td className="px-4 py-2 text-slate-400">
                                  {s.updatedAt ? new Date(s.updatedAt).toLocaleString() : "—"}
                                </td>
                                {isAdmin && (
                                  <td className="px-4 py-2">
                                    <div className="flex gap-2">
                                      <button
                                        disabled={busy}
                                        onClick={() => attemptAction("end", s.userId, s.name)}
                                        className="rounded px-2 py-1 text-xs text-amber-600 hover:bg-amber-50 disabled:opacity-50"
                                        title="End / clear this in-progress attempt"
                                      >
                                        End
                                      </button>
                                      <button
                                        disabled={busy}
                                        onClick={() => attemptAction("reset", s.userId, s.name)}
                                        className="rounded px-2 py-1 text-xs text-red-500 hover:bg-red-50 disabled:opacity-50"
                                        title="Reset all attempts so they can retake"
                                      >
                                        Reset
                                      </button>
                                    </div>
                                  </td>
                                )}
                              </tr>
                            ))
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}
          </main>
        </div>
      </div>

      {studentsFor && (
        <QuestionStudentsModal
          question={studentsFor.question}
          kind={studentsFor.kind}
          onClose={() => setStudentsFor(null)}
        />
      )}
      {solutionFor && <SolutionModal question={solutionFor} onClose={() => setSolutionFor(null)} />}
      {studentOpen && (
        <StudentDetailModal
          loading={studentLoading}
          student={studentDetail}
          totalMarks={detail?.test.totalMarks || 0}
          onClose={() => setStudentOpen(false)}
        />
      )}
    </CmdsShell>
  );
}

// Per-student drill-down (legacy: score, right/wrong breakdown, question-by-
// question, rank) — rank here is institute-scoped, not "All India": legacy's
// real AIR is a genuine cross-institute aggregation only available for tests
// shared platform-wide via CMDS to multiple institutes, which doesn't apply
// to institute-authored tests. See app/api/cmds/tests/analytics/route.ts.
function StudentDetailModal({
  loading,
  student,
  totalMarks,
  onClose,
}: {
  loading: boolean;
  student: StudentDetail | null;
  totalMarks: number;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[85vh] w-full max-w-lg overflow-y-auto rounded-lg bg-white p-6 shadow-xl">
        {loading ? (
          <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
        ) : !student ? (
          <div className="py-16 text-center text-sm text-slate-400">Couldn't load this student's attempt.</div>
        ) : (
          <>
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-lg font-semibold text-slate-800">{student.name}</h3>
                <p className="text-xs text-slate-400">{student.memberId}</p>
              </div>
              <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
                ✕
              </button>
            </div>

            <div className="mt-4 grid grid-cols-3 gap-3">
              <div className="rounded border border-slate-200 p-3 text-center">
                <div className="text-xs text-slate-400">Score</div>
                <div className="mt-1 text-lg font-semibold text-slate-800">
                  {student.score}
                  <span className="text-sm font-normal text-slate-400">/{totalMarks}</span>
                </div>
              </div>
              <div className="rounded border border-slate-200 p-3 text-center">
                <div className="text-xs text-slate-400">Percent</div>
                <div className="mt-1 text-lg font-semibold text-slate-800">{student.percent}%</div>
              </div>
              <div className="rounded border border-slate-200 p-3 text-center">
                <div className="text-xs text-slate-400">Rank</div>
                <div className="mt-1 text-lg font-semibold text-slate-800">
                  {student.rank}
                  <span className="text-sm font-normal text-slate-400">/{student.totalStudents}</span>
                </div>
              </div>
            </div>
            <p className="mt-1 text-center text-[11px] text-slate-400">Rank within this institute's test-takers.</p>

            <div className="mt-4 grid grid-cols-3 gap-3 text-center text-xs">
              <div>
                <span className="font-semibold text-emerald-600">
                  {student.questions.filter((q) => q.verdict === "CORRECT").length}
                </span>{" "}
                <span className="text-slate-400">correct</span>
              </div>
              <div>
                <span className="font-semibold text-red-500">
                  {student.questions.filter((q) => q.verdict === "INCORRECT").length}
                </span>{" "}
                <span className="text-slate-400">incorrect</span>
              </div>
              <div>
                <span className="font-semibold text-amber-600">
                  {student.questions.filter((q) => q.verdict === "PARTIAL").length}
                </span>{" "}
                <span className="text-slate-400">partial</span>
              </div>
            </div>

            <div className="mt-4 overflow-hidden rounded border border-slate-200">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                    <th className="px-3 py-1.5 font-medium">Q</th>
                    <th className="px-3 py-1.5 font-medium">Result</th>
                    <th className="px-3 py-1.5 font-medium">Marks</th>
                    <th className="px-3 py-1.5 font-medium">Time</th>
                  </tr>
                </thead>
                <tbody>
                  {student.questions.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-3 py-6 text-center text-slate-400">
                        No question data for this attempt.
                      </td>
                    </tr>
                  ) : (
                    student.questions.map((q) => (
                      <tr key={q.qId} className="border-b border-slate-100 last:border-0">
                        <td className="px-3 py-1.5 font-medium text-slate-700">{q.label}</td>
                        <td className="px-3 py-1.5">
                          <span
                            className={
                              q.verdict === "CORRECT"
                                ? "text-emerald-600"
                                : q.verdict === "PARTIAL"
                                ? "text-amber-600"
                                : q.verdict === "UNGRADED"
                                ? "text-slate-400"
                                : "text-red-500"
                            }
                          >
                            {q.verdict.charAt(0) + q.verdict.slice(1).toLowerCase()}
                          </span>
                        </td>
                        <td className="px-3 py-1.5 text-slate-500">
                          {q.marks ? `+${q.marks.positive} / -${q.marks.negative}` : "—"}
                        </td>
                        <td className="px-3 py-1.5 text-slate-500">{fmtTime(q.timeSec)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="mt-5 flex justify-end">
              <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
                Close
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

// Who got a specific question right/wrong, with time taken on that question —
// legacy's "click the count, see the list + time taken" drill-down.
function QuestionStudentsModal({
  question,
  kind,
  onClose,
}: {
  question: PerQuestion;
  kind: "correct" | "wrong";
  onClose: () => void;
}) {
  const students = kind === "correct" ? question.correctStudents : question.wrongStudents;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-sm rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">
          {question.label} — {kind === "correct" ? "Answered correctly" : "Answered incorrectly"}
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          {students.length} student{students.length === 1 ? "" : "s"}
          {kind === "correct" ? "" : " answered incorrectly or partially"}.
        </p>
        <div className="mt-4 max-h-72 overflow-y-auto rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-3 py-1.5 font-medium">Student</th>
                <th className="px-3 py-1.5 font-medium text-right">Time taken</th>
              </tr>
            </thead>
            <tbody>
              {[...students]
                .sort((a, b) => a.timeSec - b.timeSec)
                .map((s) => (
                  <tr key={s.userId} className="border-b border-slate-50 last:border-0">
                    <td className="px-3 py-2">
                      <div className="text-slate-700">{s.name}</div>
                      {s.memberId && <div className="text-xs text-slate-400">{s.memberId}</div>}
                    </td>
                    <td className="px-3 py-2 text-right text-slate-500">{fmtTime(s.timeSec)}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
        <div className="mt-5 flex justify-end">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

// Admin-only solution view — question content + options with the correct
// one(s) highlighted, plus this question's own stats for context. Same data
// students see post-submission via /api/tests/[id]/review, just reachable
// from the analytics screen instead of requiring the admin to take the test.
function SolutionModal({ question: q, onClose }: { question: PerQuestion; onClose: () => void }) {
  const correctSet = new Set(q.correctIndexes.map(String));
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[85vh] w-full max-w-xl overflow-y-auto rounded-lg bg-white p-6 shadow-xl">
        <div className="flex items-start justify-between">
          <h3 className="text-lg font-semibold text-slate-800">{q.label} — Solution</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
            ✕
          </button>
        </div>

        <div className="mt-4 grid grid-cols-5 gap-2 text-center text-xs">
          <div>
            <div className="font-semibold text-slate-700">{q.attempts}</div>
            <div className="text-slate-400">attempted</div>
          </div>
          <div>
            <div className="font-semibold text-emerald-600">{q.correct}</div>
            <div className="text-slate-400">correct</div>
          </div>
          <div>
            <div className="font-semibold text-red-500">{q.incorrect}</div>
            <div className="text-slate-400">incorrect</div>
          </div>
          <div>
            <div className="font-semibold text-slate-400">{q.notAttempted}</div>
            <div className="text-slate-400">skipped</div>
          </div>
          <div>
            <div className="font-semibold text-slate-700">{fmtTime(q.avgTimeSec)}</div>
            <div className="text-slate-400">avg time</div>
          </div>
        </div>

        <div className="mt-4 text-sm text-slate-800">
          {q.content ? <MathText>{q.content}</MathText> : <span className="text-slate-400">No content found.</span>}
        </div>

        <div className="mt-4 space-y-2">
          {q.options.map((opt, i) => {
            const isCorrect = correctSet.has(String(i));
            return (
              <div
                key={i}
                className={`flex items-start gap-2 rounded-md border px-3 py-2 text-sm ${
                  isCorrect ? "border-emerald-300 bg-emerald-50" : "border-slate-200"
                }`}
              >
                <span
                  className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[11px] font-bold ${
                    isCorrect ? "bg-emerald-500 text-white" : "bg-slate-100 text-slate-500"
                  }`}
                >
                  {LETTERS[i] || i + 1}
                </span>
                <span className="text-slate-700">
                  <MathText>{opt}</MathText>
                </span>
                {isCorrect && <span className="ml-auto shrink-0 text-xs font-medium text-emerald-600">Correct</span>}
              </div>
            );
          })}
          {q.options.length === 0 && (
            <p className="text-sm text-slate-400">No options found for this question.</p>
          )}
        </div>

        <div className="mt-5 flex justify-end">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</div>
      <div className="mt-1 text-2xl font-light text-slate-800">{value}</div>
    </div>
  );
}
