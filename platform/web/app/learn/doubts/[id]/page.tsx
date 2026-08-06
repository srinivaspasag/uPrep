"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession, type UprepSession } from "@/lib/session";
import { subjectAccent } from "@/lib/subjectColors";

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

function Avatar({ name, chip, text }: { name: string; chip: string; text: string }) {
  return (
    <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${chip} ${text}`}>
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

  const accent = subjectAccent(doubt?.subject || "");

  return (
    <LmsShell active="doubts">
      <Link href="/learn/doubts" className="inline-flex items-center gap-1 text-sm text-[#8890A1] hover:text-amber-700">
        ← Back to Doubts Forum
      </Link>

      {loading ? (
        <div className="py-16 text-center text-sm text-[#8890A1]">Loading…</div>
      ) : !doubt ? (
        <div className="py-16 text-center text-sm text-[#8890A1]">This doubt could not be found.</div>
      ) : (
        <>
          <div className="relative mt-4 overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white p-5 pl-6 sm:p-6 sm:pl-7">
            <span className={`absolute left-0 top-0 h-full w-1.5 ${accent.dot}`} />
            <div className="flex items-start gap-3">
              <Avatar name={doubt.userName} chip={accent.chip} text={accent.text} />
              <div className="min-w-0 flex-1">
                <h1 className="font-serif text-xl font-semibold text-[#16233D]">{doubt.name}</h1>
                {doubt.content && (
                  <p className="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-[#3E4A63]">{doubt.content}</p>
                )}
                <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-[#8890A1]">
                  <span className="font-medium text-[#3E4A63]">Asked by {doubt.userName}</span>
                  <span>·</span>
                  <span>{timeAgo(doubt.timeCreated)}</span>
                  <span>·</span>
                  <span>{doubt.views} views</span>
                  {doubt.subject && (
                    <span className={`rounded-full px-2 py-0.5 font-medium ${accent.chip} ${accent.text}`}>
                      {doubt.subject}
                    </span>
                  )}
                  <span
                    className={`rounded-full px-2 py-0.5 font-medium ${
                      doubt.state === "ANSWERED" ? "bg-emerald-50 text-emerald-700" : "bg-[#EDEEE9] text-[#8890A1]"
                    }`}
                  >
                    {doubt.state === "ANSWERED" ? "Answered" : "Open"}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <h2 className="mt-7 font-serif text-base font-semibold text-[#16233D]">
            {answers.length} {answers.length === 1 ? "Answer" : "Answers"}
          </h2>

          <ul className="mt-3 space-y-3">
            {answers.map((a) => (
              <li key={a.id} className="flex items-start gap-3 rounded-xl border border-[#D9D6C9] bg-white p-4">
                <Avatar name={a.userName} chip="bg-[#EDEEE9]" text="text-[#3E4A63]" />
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 text-sm">
                    <span className="font-medium text-[#16233D]">{a.userName}</span>
                    <span className="text-xs text-[#8890A1]">{timeAgo(a.timeCreated)}</span>
                  </div>
                  <p className="mt-1 whitespace-pre-wrap text-sm leading-relaxed text-[#3E4A63]">{a.content}</p>
                </div>
              </li>
            ))}
            {answers.length === 0 && (
              <li className="rounded-xl border border-dashed border-[#D9D6C9] bg-white py-8 text-center text-sm text-[#8890A1]">
                No answers yet. Be the first to help!
              </li>
            )}
          </ul>

          <div className="mt-6 rounded-2xl border border-[#D9D6C9] bg-white p-5">
            <label className="text-sm font-medium text-[#3E4A63]">Your answer</label>
            <textarea
              value={reply}
              onChange={(e) => setReply(e.target.value)}
              rows={3}
              placeholder="Write a helpful answer…"
              className="mt-1.5 w-full rounded-lg border border-[#D9D6C9] px-3 py-2.5 text-sm text-[#16233D] outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
            />
            {error && <div className="mt-2 text-sm text-red-500">{error}</div>}
            <div className="mt-3 flex justify-end">
              <button
                onClick={postAnswer}
                disabled={saving || !reply.trim()}
                className="rounded-lg bg-[#e8443b] px-5 py-2.5 text-sm font-semibold text-white shadow-sm shadow-red-900/10 transition hover:bg-[#d33c34] disabled:opacity-60"
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
