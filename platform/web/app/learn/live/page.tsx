"use client";

import { useEffect, useState } from "react";
import LmsShell, { ZeroState } from "@/components/LmsShell";

type LiveClass = {
  id: string;
  title: string;
  startAt: number | null;
  durationMin: number;
  teacher: string;
  joinUrl: string;
  live: boolean;
};

export default function LiveClassesPage() {
  const [items, setItems] = useState<LiveClass[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/learn/schedule")
      .then((r) => r.json())
      .then((d) => setItems(d.items || []))
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="live">
      <h1 className="text-xl font-semibold text-slate-800">Live Classes</h1>
      <p className="mt-1 text-sm text-slate-500">Join your scheduled online classes.</p>

      <div className="mt-6">
        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : items.length === 0 ? (
          <ZeroState icon="🎥" title="No live classes yet">
            No classes scheduled right now.
          </ZeroState>
        ) : (
          <div className="space-y-3">
            {items.map((c) => (
              <div
                key={c.id}
                className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-800">{c.title}</span>
                    {c.live && (
                      <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-bold uppercase text-red-600">
                        ● Live
                      </span>
                    )}
                  </div>
                  <div className="mt-1 text-xs text-slate-400">
                    {c.startAt ? new Date(c.startAt).toLocaleString() : "Time TBA"}
                    {c.teacher && ` · ${c.teacher}`} · {c.durationMin} min
                  </div>
                </div>
                {c.joinUrl ? (
                  <a
                    href={c.joinUrl}
                    target="_blank"
                    rel="noreferrer"
                    className={`rounded-md px-4 py-1.5 text-sm font-semibold text-white ${
                      c.live ? "bg-red-600 hover:bg-red-700" : "bg-blue-600 hover:bg-blue-700"
                    }`}
                  >
                    {c.live ? "Join now" : "Join link"}
                  </a>
                ) : (
                  <span className="text-xs text-slate-400">No link yet</span>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </LmsShell>
  );
}
