"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Row = { rank: number; userId: string; name: string; tests: number; score: number };

export default function LeaderboardPage() {
  const [range, setRange] = useState<"week" | "all">("week");
  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(true);
  const myId = getSession()?.id || "";

  useEffect(() => {
    setLoading(true);
    fetch(`/api/learn/leaderboard?range=${range}`)
      .then((r) => r.json())
      .then((d) => setRows(d.rows || []))
      .finally(() => setLoading(false));
  }, [range]);

  return (
    <LmsShell active="leaderboard">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">Leaderboard</h1>
        <div className="flex gap-1 rounded-md bg-slate-100 p-1 text-xs">
          {(["week", "all"] as const).map((r) => (
            <button
              key={r}
              onClick={() => setRange(r)}
              className={`rounded px-3 py-1 font-medium ${
                range === r ? "bg-white text-slate-800 shadow-sm" : "text-slate-500"
              }`}
            >
              {r === "week" ? "This week" : "All time"}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : rows.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No test attempts in this period yet.
        </div>
      ) : (
        <ol className="mt-4 space-y-1.5">
          {rows.map((r) => {
            const mine = r.userId === myId;
            const medal = r.rank === 1 ? "🥇" : r.rank === 2 ? "🥈" : r.rank === 3 ? "🥉" : null;
            return (
              <li
                key={r.userId}
                className={`flex items-center gap-4 rounded-lg border px-4 py-3 ${
                  mine ? "border-emerald-300 bg-emerald-50" : "border-slate-200"
                }`}
              >
                <span className="w-8 text-center text-sm font-semibold text-slate-500">
                  {medal || r.rank}
                </span>
                <span className="flex-1 font-medium text-slate-800">
                  {r.name}
                  {mine && <span className="ml-2 text-xs text-emerald-600">You</span>}
                </span>
                <span className="text-sm text-slate-400">{r.tests} tests</span>
                <span className="w-16 text-right font-semibold text-slate-800">{r.score}</span>
              </li>
            );
          })}
        </ol>
      )}
    </LmsShell>
  );
}
