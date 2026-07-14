"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";

type Notice = {
  id: string;
  title: string;
  message: string;
  imageUrl: string | null;
  resourceType: string | null;
  sentAt: number;
};

const SEEN_KEY = "uprep_notif_seen";

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

export default function NotificationsPage() {
  const [items, setItems] = useState<Notice[]>([]);
  const [loading, setLoading] = useState(true);
  const [lastSeen, setLastSeen] = useState(0);

  useEffect(() => {
    const prev = Number(sessionStorage.getItem(SEEN_KEY) || 0);
    setLastSeen(prev);
    fetch(`/api/learn/notifications`)
      .then((r) => r.json())
      .then((d) => {
        setItems(d.items || []);
        const newest = (d.items || [])[0]?.sentAt || 0;
        if (newest) sessionStorage.setItem(SEEN_KEY, String(Date.now()));
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="library">
      <h1 className="text-lg font-semibold text-slate-800">Notifications</h1>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          You&apos;re all caught up — no notifications yet.
        </div>
      ) : (
        <ul className="mt-4 divide-y divide-slate-100">
          {items.map((n) => {
            const unread = n.sentAt > lastSeen;
            return (
              <li key={n.id} className="flex items-start gap-3 py-4">
                <span
                  className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                    unread ? "bg-emerald-500" : "bg-transparent"
                  }`}
                />
                {n.imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={n.imageUrl}
                    alt=""
                    className="h-12 w-12 shrink-0 rounded-md object-cover"
                  />
                ) : (
                  <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-md bg-slate-100 text-lg">
                    🔔
                  </span>
                )}
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-800">{n.title}</span>
                    {n.resourceType && (
                      <span className="rounded bg-slate-100 px-1.5 py-0.5 text-xs text-slate-500">
                        {n.resourceType}
                      </span>
                    )}
                  </div>
                  <p className="mt-0.5 text-sm text-slate-600">{n.message}</p>
                </div>
                <span className="shrink-0 text-xs text-slate-400">{timeAgo(n.sentAt)}</span>
              </li>
            );
          })}
        </ul>
      )}
    </LmsShell>
  );
}
