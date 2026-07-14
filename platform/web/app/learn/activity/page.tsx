"use client";

import { useEffect, useState } from "react";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type FeedItem = { type: "test" | "doubt" | "answer"; title: string; detail?: string; at: number };
type Leader = { rank: number; name: string; points: number };

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

const ICON: Record<FeedItem["type"], string> = { test: "📝", doubt: "❓", answer: "💬" };

export default function ActivityPage() {
  const [feed, setFeed] = useState<FeedItem[]>([]);
  const [leaderboard, setLeaderboard] = useState<Leader[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const s = getSession();
    const uid = s?.id || "";
    fetch(`/api/learn/activity?userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => {
        setFeed(d.feed || []);
        setLeaderboard(d.leaderboard || []);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="activity">
      <div className="flex gap-8">
        <div className="flex-1">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-3 text-sm text-blue-600">
            <span>✦ Recent Activity</span>
            <span className="ml-auto text-slate-400">All Programmes ▾</span>
          </div>

          {loading ? (
            <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
          ) : feed.length === 0 ? (
            <ZeroState img="/legacy/zero/1recent-activity-zero.jpg">
              Your recent tests, doubts and answers will show up here.
            </ZeroState>
          ) : (
            <ul className="mt-2 divide-y divide-slate-100">
              {feed.map((f, i) => (
                <li key={i} className="flex items-start gap-3 py-3">
                  <span className="mt-0.5 text-lg">{ICON[f.type]}</span>
                  <div className="min-w-0 flex-1">
                    <div className="text-sm text-slate-700">{f.title}</div>
                    {f.detail && (
                      <div className="mt-0.5 line-clamp-1 text-sm text-slate-400">{f.detail}</div>
                    )}
                  </div>
                  <span className="shrink-0 text-xs text-slate-400">{timeAgo(f.at)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <aside className="w-[220px] shrink-0">
          <div className="flex items-center justify-between text-sm">
            <span className="font-medium text-slate-700">Leaderboard</span>
            <span className="text-slate-400">This Week</span>
          </div>
          {leaderboard.length === 0 ? (
            <div className="mt-4 rounded-md border border-slate-100 py-10 text-center text-sm text-slate-400">
              No Leaders yet
            </div>
          ) : (
            <ul className="mt-4 space-y-2">
              {leaderboard.map((l) => (
                <li
                  key={l.rank}
                  className="flex items-center gap-3 rounded-md border border-slate-100 px-3 py-2"
                >
                  <span
                    className={`flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold ${
                      l.rank === 1
                        ? "bg-amber-100 text-amber-700"
                        : l.rank === 2
                        ? "bg-slate-200 text-slate-600"
                        : l.rank === 3
                        ? "bg-orange-100 text-orange-700"
                        : "bg-slate-50 text-slate-500"
                    }`}
                  >
                    {l.rank}
                  </span>
                  <span className="min-w-0 flex-1 truncate text-sm text-slate-700">{l.name}</span>
                  <span className="text-xs font-medium text-slate-500">{l.points}</span>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </LmsShell>
  );
}
