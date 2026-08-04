"use client";

import { useEffect, useState } from "react";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type FeedItem = { type: "test" | "doubt" | "answer"; title: string; detail?: string; at: number };

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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const s = getSession();
    const uid = s?.id || "";
    fetch(`/api/learn/activity?userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => {
        setFeed(d.feed || []);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="activity">
      <div className="max-w-2xl">
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
    </LmsShell>
  );
}
