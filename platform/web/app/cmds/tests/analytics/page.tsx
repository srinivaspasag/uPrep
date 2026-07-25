"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession } from "@/lib/session";

type TestRow = { testId: string; name: string; attempts: number; students: number; lastAt: number };
type Overall = {
  attempts: number;
  students: number;
  avgScore: number;
  avgPercent: number;
  highScore: number;
  lowScore: number;
};
type PerQuestion = {
  qId: string;
  label: string;
  attempts: number;
  correct: number;
  incorrect: number;
  partial: number;
  ungraded: number;
  correctPercent: number;
};
type ResultRow = {
  userId: string;
  name: string;
  memberId: string;
  score: number;
  percent: number;
  attempts: number;
  lastAt: number;
};
type TopPerformer = { name: string; memberId: string; score: number; percent: number };
type DistBucket = { label: string; count: number; percentOfStudents: number };
type Detail = {
  test: { id: string; name: string; totalMarks: number };
  overall: Overall;
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
  const [tab, setTab] = useState<"overview" | "questions" | "results" | "monitor">("overview");
  const [inProgress, setInProgress] = useState<InProgress[]>([]);
  const [minPct, setMinPct] = useState("");
  const [maxPct, setMaxPct] = useState("");
  const [busy, setBusy] = useState(false);
  const isAdmin = (getSession()?.profile || "").trim().toUpperCase() === "MANAGER";

  useEffect(() => {
    fetch("/api/cmds/tests/analytics")
      .then((r) => r.json())
      .then((d) => setTests(d.tests || []))
      .finally(() => setLoading(false));
  }, []);

  async function loadMonitor(testId: string) {
    const d = await (await fetch(`/api/cmds/tests/attempts?testId=${encodeURIComponent(testId)}`)).json();
    setInProgress(d.inProgress || []);
  }

  async function open(testId: string) {
    setSelected(testId);
    setDetail(null);
    setDetailLoading(true);
    setTab("overview");
    setMinPct("");
    setMaxPct("");
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
                <h2 className="text-lg font-semibold text-slate-800">{detail.test.name}</h2>
                <div className="mt-4 flex gap-6 border-b border-slate-200 text-sm">
                  {(["overview", "questions", "results", "monitor"] as const).map((k) => (
                    <button
                      key={k}
                      onClick={() => setTab(k)}
                      className={`-mb-px border-b-2 pb-2 capitalize ${
                        tab === k
                          ? "border-emerald-500 font-semibold text-slate-900"
                          : "border-transparent text-slate-500 hover:text-slate-700"
                      }`}
                    >
                      {k === "questions"
                        ? "Per-question"
                        : k === "results"
                        ? "Result sheet"
                        : k === "monitor"
                        ? `In progress${inProgress.length ? ` (${inProgress.length})` : ""}`
                        : "Overview"}
                    </button>
                  ))}
                </div>

                {tab === "overview" && (
                  <div className="mt-5 space-y-6">
                    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                      <Stat label="Attempts" value={detail.overall.attempts} />
                      <Stat label="Students" value={detail.overall.students} />
                      <Stat label="Total marks" value={detail.test.totalMarks} />
                      <Stat label="Average score" value={detail.overall.avgScore} />
                      <Stat label="Average %" value={`${detail.overall.avgPercent}%`} />
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
                  <div className="mt-5 overflow-hidden rounded border border-slate-200">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                          <th className="px-4 py-2 font-medium">Q</th>
                          <th className="px-4 py-2 font-medium">Attempts</th>
                          <th className="px-4 py-2 font-medium">Correct</th>
                          <th className="px-4 py-2 font-medium">Incorrect</th>
                          <th className="px-4 py-2 font-medium">Partial</th>
                          <th className="px-4 py-2 font-medium">Ungraded</th>
                          <th className="px-4 py-2 font-medium">% Correct</th>
                        </tr>
                      </thead>
                      <tbody>
                        {detail.perQuestion.length === 0 ? (
                          <tr>
                            <td colSpan={7} className="px-4 py-8 text-center text-slate-400">
                              No question data.
                            </td>
                          </tr>
                        ) : (
                          detail.perQuestion.map((q) => (
                            <tr key={q.qId} className="border-b border-slate-100">
                              <td className="px-4 py-2 font-medium text-slate-700">{q.label}</td>
                              <td className="px-4 py-2 text-slate-500">{q.attempts}</td>
                              <td className="px-4 py-2 text-emerald-600">{q.correct}</td>
                              <td className="px-4 py-2 text-red-500">{q.incorrect}</td>
                              <td className="px-4 py-2 text-amber-600">{q.partial}</td>
                              <td className="px-4 py-2 text-slate-400">{q.ungraded}</td>
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
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                )}

                {tab === "results" && (
                  <div className="mt-5">
                    <div className="flex flex-wrap items-end justify-between gap-3">
                      <div className="flex items-end gap-2">
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
                    </div>

                    <div className="mt-3 overflow-hidden rounded border border-slate-200">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                            <th className="px-4 py-2 font-medium">#</th>
                            <th className="px-4 py-2 font-medium">Student</th>
                            <th className="px-4 py-2 font-medium">ID</th>
                            <th className="px-4 py-2 font-medium">Best score</th>
                            <th className="px-4 py-2 font-medium">%</th>
                            <th className="px-4 py-2 font-medium">Attempts</th>
                            <th className="px-4 py-2 font-medium">Last attempt</th>
                            {isAdmin && <th className="px-4 py-2 font-medium" />}
                          </tr>
                        </thead>
                        <tbody>
                          {(() => {
                            const lo = minPct === "" ? -Infinity : Number(minPct);
                            const hi = maxPct === "" ? Infinity : Number(maxPct);
                            const rows = detail.resultSheet.filter(
                              (r) => r.percent >= lo && r.percent <= hi
                            );
                            if (rows.length === 0)
                              return (
                                <tr>
                                  <td colSpan={isAdmin ? 8 : 7} className="px-4 py-8 text-center text-slate-400">
                                    No students in this range.
                                  </td>
                                </tr>
                              );
                            return rows.map((r, i) => (
                              <tr key={r.userId} className="border-b border-slate-100">
                                <td className="px-4 py-2 text-slate-400">{i + 1}</td>
                                <td className="px-4 py-2 font-medium text-slate-700">{r.name}</td>
                                <td className="px-4 py-2 text-slate-500">{r.memberId || "—"}</td>
                                <td className="px-4 py-2 text-slate-700">
                                  {r.score}
                                  <span className="text-slate-400"> / {detail.test.totalMarks}</span>
                                </td>
                                <td className="px-4 py-2 text-slate-500">{r.percent}%</td>
                                <td className="px-4 py-2 text-slate-500">{r.attempts}</td>
                                <td className="px-4 py-2 text-slate-400">
                                  {r.lastAt ? new Date(r.lastAt).toLocaleString() : "—"}
                                </td>
                                {isAdmin && (
                                  <td className="px-4 py-2">
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
                            ));
                          })()}
                        </tbody>
                      </table>
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
    </CmdsShell>
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
