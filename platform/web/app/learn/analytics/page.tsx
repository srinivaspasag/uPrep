"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession } from "@/lib/session";
import { subjectAccent } from "@/lib/subjectColors";

type Result = {
  entityId: string;
  name: string;
  score: number;
  totalMarks: number;
  attemptedAt: number;
};
type TrendPoint = { date: number; pct: number };
type SubjectStat = { name: string; accuracy: number; total: number };
type TypeStat = { type: string; accuracy: number; total: number };
type Summary = { testsAttempted: number; avgScore: number; accuracy: number; questionsAnswered: number };

const TYPE_LABEL: Record<string, string> = {
  SCQ: "Single Choice",
  MCQ: "Multiple Choice",
  NUMERIC: "Numeric",
  SUBJECTIVE: "Subjective",
  MATRIX: "Matrix",
  PARA: "Paragraph",
};

export default function AnalyticsPage() {
  const [results, setResults] = useState<Result[]>([]);
  const [trend, setTrend] = useState<TrendPoint[]>([]);
  const [subjects, setSubjects] = useState<SubjectStat[]>([]);
  const [types, setTypes] = useState<TypeStat[]>([]);
  const [summary, setSummary] = useState<Summary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const s = getSession();
    if (!s) return;
    fetch(`/api/learn/analytics?userId=${encodeURIComponent(s.id)}`)
      .then((r) => r.json())
      .then((d) => {
        setResults(d.results || []);
        setTrend(d.trend || []);
        setSubjects(d.subjects || []);
        setTypes(d.types || []);
        setSummary(d.summary || null);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const bySubjectWeakestFirst = useMemo(
    () => [...subjects].filter((s) => s.total >= 1).sort((a, b) => a.accuracy - b.accuracy),
    [subjects]
  );
  const strengths = useMemo(
    () => [...subjects].filter((s) => s.total >= 3).sort((a, b) => b.accuracy - a.accuracy).slice(0, 3),
    [subjects]
  );
  const focusAreas = useMemo(
    () => [...subjects].filter((s) => s.total >= 3).sort((a, b) => a.accuracy - b.accuracy).slice(0, 3),
    [subjects]
  );

  return (
    <LmsShell active="analytics">
      <div className="relative overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white p-6 sm:p-8">
        <div className="pointer-events-none absolute -right-10 -top-16 h-48 w-48 rounded-full bg-gradient-to-br from-violet-100 to-blue-100 opacity-70" />
        <div className="pointer-events-none absolute right-20 bottom-2 h-14 w-14 rounded-full bg-emerald-50" />
        <span className="relative inline-flex items-center gap-1.5 rounded-full bg-[#EDEEE9] px-3 py-1 text-xs font-medium uppercase tracking-wide text-[#8890A1]">
          📊 Analytics
        </span>
        <h1 className="relative mt-3 font-serif text-2xl font-semibold text-[#16233D] sm:text-3xl">
          Your progress, at a glance
        </h1>
        <p className="relative mt-1.5 max-w-md text-sm text-[#3E4A63]">
          Every test you've taken, broken down by subject and question type — so you know exactly
          what to study next.
        </p>
      </div>

      {loading ? (
        <div className="py-16 text-center text-sm text-[#8890A1]">Loading…</div>
      ) : results.length === 0 ? (
        <div className="mt-4 rounded-2xl border border-dashed border-[#D9D6C9] bg-white">
          <ZeroState icon="📊" title="No analytics yet">
            <span>
              Attempt some{" "}
              <Link href="/learn/library" className="font-medium text-amber-700 underline underline-offset-2">
                tests
              </Link>{" "}
              to see your analytics.
            </span>
          </ZeroState>
        </div>
      ) : (
        <>
          {/* Summary stat cards */}
          <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <StatCard icon="🎯" gradient="from-blue-500 to-indigo-600" label="Avg. Score" value={`${summary?.avgScore ?? 0}%`} />
            <StatCard icon="✅" gradient="from-emerald-500 to-teal-600" label="Accuracy" value={`${summary?.accuracy ?? 0}%`} />
            <StatCard icon="📝" gradient="from-violet-500 to-purple-600" label="Tests Attempted" value={String(summary?.testsAttempted ?? 0)} />
            <StatCard icon="❓" gradient="from-amber-500 to-orange-600" label="Questions Answered" value={String(summary?.questionsAnswered ?? 0)} />
          </div>

          {/* Score trend */}
          {trend.length > 1 && (
            <div className="mt-6 rounded-2xl border border-[#D9D6C9] bg-white p-5">
              <h2 className="font-serif text-base font-semibold text-[#16233D]">Score trend</h2>
              <p className="mt-0.5 text-xs text-[#8890A1]">Test score % over time, oldest to most recent.</p>
              <TrendChart trend={trend} />
            </div>
          )}

          {/* Strengths & Focus areas */}
          {(strengths.length > 0 || focusAreas.length > 0) && (
            <div className="mt-6 grid gap-4 sm:grid-cols-2">
              <div className="rounded-2xl border border-emerald-100 bg-emerald-50/50 p-5">
                <h2 className="flex items-center gap-1.5 font-serif text-base font-semibold text-[#16233D]">
                  🌟 Strengths
                </h2>
                <p className="mt-0.5 text-xs text-[#8890A1]">Your strongest subjects right now.</p>
                <ul className="mt-3 space-y-2">
                  {strengths.length === 0 ? (
                    <li className="text-xs text-[#8890A1]">Attempt a few more tests to see this.</li>
                  ) : (
                    strengths.map((s) => (
                      <li key={s.name} className="flex items-center justify-between text-sm">
                        <span className="text-[#3E4A63]">{s.name}</span>
                        <span className="font-serif font-semibold text-emerald-700">{s.accuracy}%</span>
                      </li>
                    ))
                  )}
                </ul>
              </div>
              <div className="rounded-2xl border border-rose-100 bg-rose-50/50 p-5">
                <h2 className="flex items-center gap-1.5 font-serif text-base font-semibold text-[#16233D]">
                  🎯 Focus Areas
                </h2>
                <p className="mt-0.5 text-xs text-[#8890A1]">Subjects worth extra revision time.</p>
                <ul className="mt-3 space-y-2">
                  {focusAreas.length === 0 ? (
                    <li className="text-xs text-[#8890A1]">Attempt a few more tests to see this.</li>
                  ) : (
                    focusAreas.map((s) => (
                      <li key={s.name} className="flex items-center justify-between text-sm">
                        <span className="text-[#3E4A63]">{s.name}</span>
                        <span className="font-serif font-semibold text-rose-700">{s.accuracy}%</span>
                      </li>
                    ))
                  )}
                </ul>
              </div>
            </div>
          )}

          {/* Performance by subject */}
          {bySubjectWeakestFirst.length > 0 && (
            <div className="mt-6 rounded-2xl border border-[#D9D6C9] bg-white p-5">
              <h2 className="font-serif text-base font-semibold text-[#16233D]">Performance by subject</h2>
              <div className="mt-4 space-y-3">
                {bySubjectWeakestFirst.map((s) => {
                  const accent = subjectAccent(s.name);
                  return (
                    <div key={s.name}>
                      <div className="flex items-center justify-between text-sm">
                        <span className="font-medium text-[#16233D]">{s.name}</span>
                        <span className="text-xs text-[#8890A1]">
                          {s.accuracy}% <span className="text-[#D9D6C9]">·</span> {s.total} question{s.total === 1 ? "" : "s"}
                        </span>
                      </div>
                      <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-[#F0EFEA]">
                        <div
                          className={`h-full rounded-full ${accent.gradient}`}
                          style={{ width: `${Math.max(4, s.accuracy)}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Performance by question type */}
          {types.length > 0 && (
            <div className="mt-6 rounded-2xl border border-[#D9D6C9] bg-white p-5">
              <h2 className="font-serif text-base font-semibold text-[#16233D]">Performance by question type</h2>
              <div className="mt-3 flex flex-wrap gap-2.5">
                {types.map((t) => (
                  <div key={t.type} className="flex items-center gap-2 rounded-xl bg-[#F8F7F3] px-3.5 py-2">
                    <span
                      className={`h-2 w-2 rounded-full ${
                        t.accuracy >= 60 ? "bg-emerald-500" : t.accuracy >= 33 ? "bg-amber-500" : "bg-rose-500"
                      }`}
                    />
                    <span className="text-sm text-[#3E4A63]">{TYPE_LABEL[t.type] || t.type}</span>
                    <span className="font-serif text-sm font-semibold text-[#16233D]">{t.accuracy}%</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Recent results */}
          <div className="mt-6 overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white">
            <div className="border-b border-[#D9D6C9] px-5 py-3.5">
              <h2 className="font-serif text-base font-semibold text-[#16233D]">Recent results</h2>
            </div>
            <table className="w-full text-sm">
              <thead className="text-left text-[#8890A1]">
                <tr>
                  <th className="px-5 py-2.5 font-medium">Test</th>
                  <th className="px-5 py-2.5 font-medium">Score</th>
                  <th className="px-5 py-2.5 font-medium">%</th>
                  <th className="px-5 py-2.5 font-medium">Attempted</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#EDEEE9]">
                {results.map((r) => {
                  const pct = r.totalMarks ? Math.round((r.score / r.totalMarks) * 100) : 0;
                  return (
                    <tr key={r.entityId + r.attemptedAt} className="transition hover:bg-[#F8F7F3]">
                      <td className="px-5 py-3 font-medium text-[#16233D]">{r.name}</td>
                      <td className="px-5 py-3 text-[#3E4A63]">
                        {r.score}/{r.totalMarks}
                      </td>
                      <td className="px-5 py-3">
                        <span
                          className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                            pct >= 60
                              ? "bg-emerald-100 text-emerald-700"
                              : pct >= 33
                              ? "bg-amber-100 text-amber-700"
                              : "bg-rose-100 text-rose-700"
                          }`}
                        >
                          {pct}%
                        </span>
                      </td>
                      <td className="px-5 py-3 text-[#8890A1]">
                        {r.attemptedAt ? new Date(r.attemptedAt).toLocaleDateString() : "—"}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </LmsShell>
  );
}

function StatCard({ icon, gradient, label, value }: { icon: string; gradient: string; label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-[#D9D6C9] bg-white p-4">
      <span className={`flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br ${gradient} text-base shadow-sm`}>
        {icon}
      </span>
      <div className="mt-2.5 font-serif text-xl font-semibold text-[#16233D]">{value}</div>
      <div className="text-xs text-[#8890A1]">{label}</div>
    </div>
  );
}

// Lightweight inline SVG area chart — no charting library needed for a
// single trend line across a handful of test attempts.
function TrendChart({ trend }: { trend: TrendPoint[] }) {
  const width = 600;
  const height = 140;
  const pad = 8;
  const n = trend.length;
  const stepX = n > 1 ? (width - pad * 2) / (n - 1) : 0;
  const points = trend.map((t, i) => {
    const x = n > 1 ? pad + i * stepX : width / 2;
    const y = pad + (1 - Math.max(0, Math.min(100, t.pct)) / 100) * (height - pad * 2);
    return [x, y] as const;
  });
  const line = points.map((p, i) => `${i === 0 ? "M" : "L"} ${p[0].toFixed(1)} ${p[1].toFixed(1)}`).join(" ");
  const area = `${line} L ${points[points.length - 1][0].toFixed(1)} ${height - pad} L ${points[0][0].toFixed(1)} ${height - pad} Z`;

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="mt-4 w-full" preserveAspectRatio="none">
      <defs>
        <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.28" />
          <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill="url(#trendFill)" />
      <path d={line} fill="none" stroke="#8b5cf6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
      {points.map(([x, y], i) => (
        <circle key={i} cx={x} cy={y} r="4" fill="#fff" stroke="#8b5cf6" strokeWidth="2.5" />
      ))}
    </svg>
  );
}
