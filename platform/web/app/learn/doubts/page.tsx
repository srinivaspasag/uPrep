"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession, type UprepSession } from "@/lib/session";
import { subjectAccent } from "@/lib/subjectColors";
import BoardPicker from "@/components/BoardPicker";

type DoubtTab = "recent" | "popular" | "asked";

type Doubt = {
  id: string;
  name: string;
  content: string;
  userName: string;
  subject: string | null;
  answerCount: number;
  upVotes: number;
  views: number;
  state: string;
  timeCreated: number;
};

const TABS: { k: DoubtTab; l: string }[] = [
  { k: "recent", l: "Recent" },
  { k: "popular", l: "Popular" },
  { k: "asked", l: "Asked by me" },
];

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

export default function DoubtsPage() {
  const [tab, setTab] = useState<DoubtTab>("recent");
  const [session, setSession] = useState<UprepSession | null>(null);
  const [items, setItems] = useState<Doubt[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAsk, setShowAsk] = useState(false);

  useEffect(() => {
    setSession(getSession());
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    const uid = getSession()?.id || "";
    const res = await fetch(`/api/learn/doubts?tab=${tab}&userId=${encodeURIComponent(uid)}`);
    const data = await res.json();
    setItems(data.items || []);
    setLoading(false);
  }, [tab]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <LmsShell active="doubts">
      {/* Hero — a single bold moment (the red CTA, matching legacy's real
          brand color for this action) against the app's usual cream/navy
          palette, with a soft decorative wash instead of a flat bar. */}
      <div className="relative overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white p-6 sm:p-8">
        <div className="pointer-events-none absolute -right-10 -top-14 h-48 w-48 rounded-full bg-amber-50" />
        <div className="pointer-events-none absolute -right-2 top-10 h-20 w-20 rounded-full bg-red-50" />
        <div className="relative flex flex-col items-start gap-6 sm:flex-row sm:items-center sm:justify-between">
          <div className="max-w-lg">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-[#EDEEE9] px-3 py-1 text-xs font-medium uppercase tracking-wide text-[#8890A1]">
              💬 Doubts Forum
            </span>
            <h1 className="mt-3 font-serif text-2xl font-semibold text-[#16233D] sm:text-3xl">
              Stuck on something?
            </h1>
            <p className="mt-2 text-sm leading-relaxed text-[#3E4A63]">
              Ask your peers and teachers — the more specific the question, the faster you&apos;ll
              get help.
            </p>
          </div>
          <button
            onClick={() => setShowAsk(true)}
            className="group shrink-0 rounded-full bg-[#e8443b] px-7 py-3.5 text-sm font-semibold text-white shadow-sm shadow-red-900/10 transition hover:-translate-y-0.5 hover:bg-[#d33c34] hover:shadow-lg hover:shadow-red-900/15"
          >
            <span className="mr-1.5 inline-block transition group-hover:rotate-12">✍️</span>
            Ask a Doubt
          </button>
        </div>
      </div>

      {/* Tabs — same amber-underline pattern used elsewhere in the app
          (see components/LibrarySection.tsx) instead of a generic emerald
          underline, so this page reads as part of the same product. */}
      <div className="mt-8 flex flex-wrap gap-x-7 gap-y-2 border-b border-[#D9D6C9] text-sm">
        {TABS.map((t) => (
          <button
            key={t.k}
            onClick={() => setTab(t.k)}
            className={`-mb-px border-b-2 pb-2.5 font-medium transition ${
              tab === t.k
                ? "border-amber-600 text-[#16233D]"
                : "border-transparent text-[#8890A1] hover:text-[#16233D]"
            }`}
          >
            {t.l}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="py-16 text-center text-sm text-[#8890A1]">Loading doubts…</div>
      ) : items.length === 0 ? (
        <div className="mt-4 rounded-2xl border border-dashed border-[#D9D6C9] bg-white">
          <ZeroState img="/legacy/zero/2doubts-zero.jpg">
            {tab === "asked" ? "You haven't asked any doubts yet." : "No doubts here yet — be the first to ask!"}
          </ZeroState>
        </div>
      ) : (
        <ul className="mt-5 space-y-3">
          {items.map((d) => {
            const accent = subjectAccent(d.subject || "");
            const answered = d.state === "ANSWERED";
            return (
              <li key={d.id}>
                <Link
                  href={`/learn/doubts/${d.id}`}
                  className="group relative flex items-start gap-4 overflow-hidden rounded-xl border border-[#D9D6C9] bg-white p-4 pl-5 transition hover:-translate-y-0.5 hover:shadow-md sm:p-5 sm:pl-6"
                >
                  <span className={`absolute left-0 top-0 h-full w-1.5 ${accent.dot}`} />
                  <span
                    className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${accent.chip} ${accent.text}`}
                  >
                    {(d.userName || "U").charAt(0).toUpperCase()}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-serif text-[15px] font-semibold text-[#16233D] group-hover:text-amber-700">
                      {d.name}
                    </div>
                    {d.content && (
                      <div className="mt-1 line-clamp-2 text-sm text-[#3E4A63]">{d.content}</div>
                    )}
                    <div className="mt-2.5 flex flex-wrap items-center gap-2 text-xs text-[#8890A1]">
                      <span className="font-medium text-[#3E4A63]">{d.userName}</span>
                      <span>·</span>
                      <span>{timeAgo(d.timeCreated)}</span>
                      {d.subject && (
                        <span className={`rounded-full px-2 py-0.5 font-medium ${accent.chip} ${accent.text}`}>
                          {d.subject}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex shrink-0 flex-col items-end gap-2">
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                        answered ? "bg-emerald-50 text-emerald-700" : "bg-[#EDEEE9] text-[#8890A1]"
                      }`}
                    >
                      {answered ? "Answered" : "Open"}
                    </span>
                    <div className="text-center text-xs text-[#8890A1]">
                      <div className="font-serif text-base font-semibold text-[#16233D]">{d.answerCount}</div>
                      <div>{d.answerCount === 1 ? "answer" : "answers"}</div>
                    </div>
                  </div>
                </Link>
              </li>
            );
          })}
        </ul>
      )}

      {showAsk && (
        <AskDoubtModal
          session={session}
          onClose={() => setShowAsk(false)}
          onPosted={() => {
            setShowAsk(false);
            setTab("recent");
            load();
          }}
        />
      )}
    </LmsShell>
  );
}

function AskDoubtModal({
  session,
  onClose,
  onPosted,
}: {
  session: UprepSession | null;
  onClose: () => void;
  onPosted: () => void;
}) {
  const [name, setName] = useState("");
  const [content, setContent] = useState("");
  const [subject, setSubject] = useState("");
  const [boardIds, setBoardIds] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    if (!name.trim()) {
      setError("Please enter your question.");
      return;
    }
    setSaving(true);
    setError(null);
    const res = await fetch("/api/learn/doubts", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: name.trim(),
        content: content.trim(),
        subject: subject.trim(),
        boardIds,
        userId: session?.id,
        userName: [session?.firstName, session?.lastName].filter(Boolean).join(" ") || "Student",
      }),
    });
    setSaving(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Could not post your doubt.");
      return;
    }
    onPosted();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#16233D]/50 p-4 backdrop-blur-[2px]">
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl sm:p-7">
        <div className="flex items-start justify-between">
          <div>
            <div className="font-serif text-xl font-semibold text-[#16233D]">Ask a Doubt</div>
            <p className="mt-1 text-sm text-[#8890A1]">
              Be specific so peers and teachers can help you faster.
            </p>
          </div>
          <button
            onClick={onClose}
            className="shrink-0 text-xl leading-none text-[#8890A1] hover:text-[#16233D]"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <label className="mt-5 block text-sm font-medium text-[#3E4A63]">Your question</label>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. How do I find the derivative of sin(x²)?"
          className="mt-1.5 w-full rounded-lg border border-[#D9D6C9] px-3 py-2.5 text-sm text-[#16233D] outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
          autoFocus
        />

        <label className="mt-4 block text-sm font-medium text-[#3E4A63]">Details (optional)</label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          placeholder="Add any context, what you've tried, etc."
          className="mt-1.5 w-full rounded-lg border border-[#D9D6C9] px-3 py-2.5 text-sm text-[#16233D] outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
        />

        <div className="mt-4 rounded-lg border border-[#D9D6C9] bg-[#EDEEE9]/60 p-3">
          <BoardPicker selected={boardIds} onChange={setBoardIds} apiBase="/api/learn/boards" onSubjectChange={setSubject} />
          {subject && (
            <p className="mt-1.5 text-xs text-[#8890A1]">
              Subject: <span className="font-medium text-[#3E4A63]">{subject}</span> — derived from the chapter(s)
              tagged above.
            </p>
          )}
        </div>

        {error && <div className="mt-3 text-sm text-red-500">{error}</div>}

        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-lg px-4 py-2 text-sm text-[#8890A1] hover:text-[#16233D]"
          >
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={saving}
            className="rounded-lg bg-[#e8443b] px-5 py-2.5 text-sm font-semibold text-white shadow-sm shadow-red-900/10 transition hover:bg-[#d33c34] disabled:opacity-60"
          >
            {saving ? "Posting…" : "Post Doubt"}
          </button>
        </div>
      </div>
    </div>
  );
}
