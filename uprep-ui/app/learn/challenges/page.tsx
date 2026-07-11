"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Challenge = {
  id: string;
  name: string;
  description: string;
  testId: string | null;
  endAt: number | null;
  participants: number;
};

export default function ChallengesPage() {
  const [items, setItems] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);

  async function load() {
    const d = await fetch("/api/learn/challenges").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function join(c: Challenge) {
    await fetch("/api/learn/challenges", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ action: "join", id: c.id, userId: getSession()?.id }),
    });
    load();
  }

  return (
    <LmsShell active="challenges">
      <h1 className="text-lg font-semibold text-slate-800">Challenges</h1>
      <p className="mt-1 text-sm text-slate-500">Compete with your batch in time-boxed test challenges.</p>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No active challenges right now.
        </div>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          {items.map((c) => {
            const ended = c.endAt ? c.endAt < Date.now() : false;
            return (
              <div key={c.id} className="rounded-lg border border-slate-200 p-5">
                <div className="flex items-center justify-between">
                  <span className="text-2xl">🏆</span>
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      ended ? "bg-slate-100 text-slate-500" : "bg-emerald-100 text-emerald-700"
                    }`}
                  >
                    {ended ? "Ended" : "Live"}
                  </span>
                </div>
                <div className="mt-2 font-medium text-slate-800">{c.name}</div>
                {c.description && <div className="mt-1 text-sm text-slate-500">{c.description}</div>}
                <div className="mt-2 text-xs text-slate-400">
                  {c.participants} participant{c.participants === 1 ? "" : "s"}
                  {c.endAt && ` · ends ${new Date(c.endAt).toLocaleDateString()}`}
                </div>
                <div className="mt-3 flex gap-2">
                  <button
                    onClick={() => join(c)}
                    disabled={ended}
                    className="rounded-md border border-emerald-500 px-3 py-1.5 text-sm font-medium text-emerald-700 hover:bg-emerald-50 disabled:opacity-50"
                  >
                    Join
                  </button>
                  {c.testId && !ended && (
                    <Link
                      href={`/test/${c.testId}`}
                      className="rounded-md bg-[#e8443b] px-3 py-1.5 text-sm font-semibold text-white hover:bg-[#d33c34]"
                    >
                      Start
                    </Link>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </LmsShell>
  );
}
