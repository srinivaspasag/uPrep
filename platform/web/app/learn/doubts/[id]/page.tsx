"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Answer = { id: string; content: string; userName: string; timeCreated: number };
type Doubt = {
  id: string;
  name: string;
  content: string;
  userName: string;
  subject: string | null;
  upVotes: number;
  views: number;
  state: string;
  timeCreated: number;
};

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

function Avatar({ name }: { name: string }) {
  return (
    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-600">
      {(name || "U").charAt(0).toUpperCase()}
    </span>
  );
}

export default function DoubtDetailPage() {
  const params = useParams();
  const id = String(params.id);
  const [session, setSession] = useState<UprepSession | null>(null);
  const [doubt, setDoubt] = useState<Doubt | null>(null);
  const [answers, setAnswers] = useState<Answer[]>([]);
  const [loading, setLoading] = useState(true);
  const [reply, setReply] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setSession(getSession());
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    const res = await fetch(`/api/learn/doubts/${id}`);
    const data = await res.json();
    setDoubt(data.doubt || null);
    setAnswers(data.answers || []);
    setLoading(false);
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  async function postAnswer() {
    if (!reply.trim()) return;
    setSaving(true);
    setError(null);
    const res = await fetch(`/api/learn/doubts/${id}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        content: reply.trim(),
        userId: session?.id,
        userName: [session?.firstName, session?.lastName].filter(Boolean).join(" ") || "Member",
      }),
    });
    setSaving(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Could not post your answer.");
      return;
    }
    const created = await res.json();
    setAnswers((prev) => [...prev, created]);
    setReply("");
  }

  return (
    <LmsShell active="doubts">
      <Link href="/learn/doubts" className="text-sm text-emerald-600 hover:underline">
        ← Back to Doubts Forum
      </Link>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : !doubt ? (
        <div className="py-16 text-center text-sm text-slate-400">This doubt could not be found.</div>
      ) : (
        <>
          <div className="mt-4 rounded-lg border border-slate-200 p-5">
            <div className="flex items-start gap-3">
              <Avatar name={doubt.userName} />
              <div className="min-w-0 flex-1">
                <h1 className="text-lg font-semibold text-slate-800">{doubt.name}</h1>
                {doubt.content && (
                  <p className="mt-2 whitespace-pre-wrap text-sm text-slate-600">{doubt.content}</p>
                )}
                <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-slate-400">
                  <span>Asked by {doubt.userName}</span>
                  <span>·</span>
                  <span>{timeAgo(doubt.timeCreated)}</span>
                  <span>·</span>
                  <span>{doubt.views} views</span>
                  {doubt.subject && (
                    <span className="rounded bg-slate-100 px-1.5 py-0.5 text-slate-500">
                      {doubt.subject}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

          <h2 className="mt-6 text-sm font-semibold uppercase tracking-wide text-slate-500">
            {answers.length} {answers.length === 1 ? "Answer" : "Answers"}
          </h2>

          <ul className="mt-3 space-y-3">
            {answers.map((a) => (
              <li key={a.id} className="flex items-start gap-3 rounded-lg bg-slate-50 p-4">
                <Avatar name={a.userName} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 text-sm">
                    <span className="font-medium text-slate-700">{a.userName}</span>
                    <span className="text-xs text-slate-400">{timeAgo(a.timeCreated)}</span>
                  </div>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-slate-600">{a.content}</p>
                </div>
              </li>
            ))}
            {answers.length === 0 && (
              <li className="rounded-lg border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
                No answers yet. Be the first to help!
              </li>
            )}
          </ul>

          <div className="mt-6 rounded-lg border border-slate-200 p-4">
            <label className="text-sm font-medium text-slate-600">Your answer</label>
            <textarea
              value={reply}
              onChange={(e) => setReply(e.target.value)}
              rows={3}
              placeholder="Write a helpful answer…"
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
            />
            {error && <div className="mt-2 text-sm text-red-500">{error}</div>}
            <div className="mt-3 flex justify-end">
              <button
                onClick={postAnswer}
                disabled={saving || !reply.trim()}
                className="rounded-md bg-[#e8443b] px-5 py-2 text-sm font-semibold text-white hover:bg-[#d33c34] disabled:opacity-60"
              >
                {saving ? "Posting…" : "Post Answer"}
              </button>
            </div>
          </div>
        </>
      )}
    </LmsShell>
  );
}
