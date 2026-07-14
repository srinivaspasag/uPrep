"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Result = {
  entityId: string;
  name: string;
  score: number;
  totalMarks: number;
  timeTaken: number;
  attemptedAt: number;
};

export default function AnalyticsPage() {
  const [results, setResults] = useState<Result[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const s = getSession();
    if (!s) return;
    fetch(`/api/analytics?userId=${encodeURIComponent(s.id)}`)
      .then((r) => r.json())
      .then((d) => setResults(d.results || []))
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  }, []);

  const avg =
    results.length > 0
      ? Math.round(
          (results.reduce(
            (a, r) => a + (r.totalMarks ? (r.score / r.totalMarks) * 100 : 0),
            0
          ) /
            results.length) *
            10
        ) / 10
      : 0;

  return (
    <LmsShell active="analytics">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">Result Analytics</h1>
        <div className="flex items-center gap-2 text-sm text-slate-500">
          Avg. Score
          <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 font-semibold text-emerald-700">
            {avg}%
          </span>
        </div>
      </div>

      {loading ? (
        <div className="py-16 text-center text-slate-400">Loading…</div>
      ) : results.length === 0 ? (
        <ZeroState img="/legacy/zero/4analytics-zero.jpg">
          <span>
            Attempt some{" "}
            <Link href="/learn/library" className="text-blue-600 underline">
              tests
            </Link>{" "}
            to view your analytics.
          </span>
        </ZeroState>
      ) : (
        <div className="mt-6 overflow-hidden rounded-xl border border-slate-200">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium">Test</th>
                <th className="px-4 py-3 font-medium">Score</th>
                <th className="px-4 py-3 font-medium">%</th>
                <th className="px-4 py-3 font-medium">Attempted</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {results.map((r) => {
                const pct = r.totalMarks
                  ? Math.round((r.score / r.totalMarks) * 100)
                  : 0;
                return (
                  <tr key={r.entityId + r.attemptedAt} className="hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-800">{r.name}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {r.score}/{r.totalMarks}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                          pct >= 60
                            ? "bg-emerald-100 text-emerald-700"
                            : pct >= 33
                            ? "bg-amber-100 text-amber-700"
                            : "bg-red-100 text-red-700"
                        }`}
                      >
                        {pct}%
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {r.attemptedAt
                        ? new Date(r.attemptedAt).toLocaleDateString()
                        : "—"}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </LmsShell>
  );
}
