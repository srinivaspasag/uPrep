"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import AnswerText from "@/components/AnswerText";

type Item = {
  id: string;
  doubtId: string;
  doubtName: string;
  content: string;
  confidence: string | null;
  reasoning: string | null;
  groundedOn: string | null;
  timeCreated: number;
};

export default function AiReviewQueuePage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    const d = await fetch("/api/cmds/doubts/ai-review").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function act(id: string, action: "approve" | "discard") {
    setBusyId(id);
    await fetch("/api/cmds/doubts/ai-review", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ commentId: id, action }),
    });
    setItems((prev) => prev.filter((i) => i.id !== id));
    setBusyId(null);
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[820px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">AI Tutor — Review Queue</h1>
        <p className="mt-1 text-sm text-slate-500">
          The AI Tutor flagged these doubt answers as low-confidence. Approve to show them to the student, or discard
          if the answer is wrong or unhelpful — nothing here is visible until you act on it.
        </p>

        {loading ? (
          <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
        ) : items.length === 0 ? (
          <div className="mt-6 rounded border border-dashed border-slate-200 py-10 text-center text-sm text-slate-400">
            Nothing waiting on review.
          </div>
        ) : (
          <ul className="mt-5 space-y-4">
            {items.map((i) => (
              <li key={i.id} className="rounded border border-amber-200 bg-amber-50/40 p-4">
                <div className="flex items-center justify-between gap-3">
                  <Link href={`/learn/doubts/${i.doubtId}`} className="text-sm font-medium text-slate-800 hover:underline">
                    {i.doubtName}
                  </Link>
                  {i.groundedOn && <span className="text-xs text-slate-400">{i.groundedOn}</span>}
                </div>
                <AnswerText className="mt-2 text-sm text-slate-700">{i.content}</AnswerText>
                {i.reasoning && (
                  <p className="mt-2 text-xs italic text-slate-500">Model's reasoning: {i.reasoning}</p>
                )}
                <div className="mt-3 flex items-center gap-2">
                  <button
                    onClick={() => act(i.id, "approve")}
                    disabled={busyId === i.id}
                    className="rounded bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
                  >
                    Approve & show student
                  </button>
                  <button
                    onClick={() => act(i.id, "discard")}
                    disabled={busyId === i.id}
                    className="rounded border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-50 disabled:opacity-60"
                  >
                    Discard
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </CmdsShell>
  );
}
