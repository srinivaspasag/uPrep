"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";

type ScheduledTest = {
  id: string;
  testId: string;
  testName: string;
  startAt: number | null;
  endAt: number | null;
  durationMin: number | null;
  status: "UPCOMING" | "LIVE" | "ENDED";
  startsInMs: number | null;
  endsInMs: number | null;
  canStart: boolean;
};

function fmtCountdown(ms: number): string {
  let s = Math.max(0, Math.floor(ms / 1000));
  const d = Math.floor(s / 86400);
  s -= d * 86400;
  const h = Math.floor(s / 3600);
  s -= h * 3600;
  const m = Math.floor(s / 60);
  s -= m * 60;
  const parts: string[] = [];
  if (d) parts.push(`${d}d`);
  if (h || d) parts.push(`${h}h`);
  parts.push(`${m}m`);
  if (!d && !h) parts.push(`${s}s`);
  return parts.join(" ");
}

export default function ScheduledTestsPage() {
  const [items, setItems] = useState<ScheduledTest[]>([]);
  const [loading, setLoading] = useState(true);
  const [, setTick] = useState(0);

  useEffect(() => {
    fetch("/api/learn/tests")
      .then((r) => r.json())
      .then((d) => setItems(d.items || []))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const t = setInterval(() => setTick((n) => n + 1), 1000);
    return () => clearInterval(t);
  }, []);

  return (
    <LmsShell active="scheduled">
      <h1 className="text-xl font-semibold text-slate-800">Scheduled Tests</h1>
      <p className="mt-1 text-sm text-slate-500">
        Tests your institute has scheduled. You can start a test only during its window.
      </p>

      <div className="mt-6">
        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : items.length === 0 ? (
          <ZeroState img="/legacy/zero/general-no-content.jpg">
            No tests scheduled for you right now.
          </ZeroState>
        ) : (
          <div className="space-y-3">
            {items.map((t) => {
              const now = Date.now();
              const startsIn = t.startAt ? t.startAt - now : null;
              const endsIn = t.endAt ? t.endAt - now : null;
              return (
                <div
                  key={t.id}
                  className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-800">{t.testName}</span>
                      {t.status === "LIVE" && (
                        <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-bold uppercase text-red-600">
                          ● Live now
                        </span>
                      )}
                      {t.status === "UPCOMING" && (
                        <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold uppercase text-amber-700">
                          Upcoming
                        </span>
                      )}
                      {t.status === "ENDED" && (
                        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-bold uppercase text-slate-500">
                          Ended
                        </span>
                      )}
                    </div>
                    <div className="mt-1 text-xs text-slate-400">
                      {t.startAt ? new Date(t.startAt).toLocaleString() : "Time TBA"}
                      {t.durationMin ? ` · ${t.durationMin} min` : ""}
                      {t.status === "UPCOMING" && startsIn != null && (
                        <span className="ml-2 font-medium text-amber-600">
                          starts in {fmtCountdown(startsIn)}
                        </span>
                      )}
                      {t.status === "LIVE" && endsIn != null && (
                        <span className="ml-2 font-medium text-red-600">
                          ends in {fmtCountdown(endsIn)}
                        </span>
                      )}
                    </div>
                  </div>
                  {t.canStart ? (
                    <Link
                      href={`/test/${t.testId}`}
                      className="rounded-md bg-emerald-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-emerald-700"
                    >
                      Start test
                    </Link>
                  ) : (
                    <span className="rounded-md bg-slate-100 px-4 py-1.5 text-sm font-medium text-slate-400">
                      {t.status === "ENDED" ? "Closed" : "Locked"}
                    </span>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </LmsShell>
  );
}
