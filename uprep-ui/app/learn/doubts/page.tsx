"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession, type UprepSession } from "@/lib/session";

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
      <div className="flex items-stretch gap-4 rounded-md bg-slate-50 p-3">
        <button
          onClick={() => setShowAsk(true)}
          className="rounded-md bg-[#e8443b] px-8 py-3 font-semibold text-white hover:bg-[#d33c34]"
        >
          Ask a Doubt
        </button>
        <div className="flex flex-col justify-center">
          <div className="font-semibold text-slate-700">Have a doubt</div>
          <div className="text-sm text-slate-500">
            Resolve your doubts by asking them to your peers and teachers. The more specific the
            question, the better.
          </div>
        </div>
      </div>

      <div className="mt-6 flex gap-4 border-b border-slate-200 pb-2 text-sm">
        {TABS.map((t) => (
          <button
            key={t.k}
            onClick={() => setTab(t.k)}
            className={`-mb-[9px] border-b-2 pb-2 ${
              tab === t.k
                ? "border-emerald-500 font-semibold text-slate-800"
                : "border-transparent text-slate-500 hover:text-slate-700"
            }`}
          >
            {t.l}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading doubts…</div>
      ) : items.length === 0 ? (
        <ZeroState img="/legacy/zero/2doubts-zero.jpg">
          {tab === "asked" ? "You haven't asked any doubts yet." : "No doubts here yet — be the first to ask!"}
        </ZeroState>
      ) : (
        <ul className="mt-4 divide-y divide-slate-100">
          {items.map((d) => (
            <li key={d.id}>
              <Link href={`/learn/doubts/${d.id}`} className="block py-4 hover:bg-slate-50">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0">
                    <div className="truncate font-semibold text-slate-800">{d.name}</div>
                    {d.content && (
                      <div className="mt-1 line-clamp-2 text-sm text-slate-500">{d.content}</div>
                    )}
                    <div className="mt-2 flex items-center gap-3 text-xs text-slate-400">
                      <span>{d.userName}</span>
                      <span>·</span>
                      <span>{timeAgo(d.timeCreated)}</span>
                      {d.subject && (
                        <>
                          <span>·</span>
                          <span className="rounded bg-slate-100 px-1.5 py-0.5 text-slate-500">
                            {d.subject}
                          </span>
                        </>
                      )}
                    </div>
                  </div>
                  <div className="flex shrink-0 items-center gap-4 text-center text-xs text-slate-500">
                    <div>
                      <div className="text-base font-semibold text-slate-700">{d.answerCount}</div>
                      <div>answers</div>
                    </div>
                    {d.state === "ANSWERED" && (
                      <span className="rounded-full bg-emerald-50 px-2 py-1 font-medium text-emerald-600">
                        Answered
                      </span>
                    )}
                  </div>
                </div>
              </Link>
            </li>
          ))}
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
        <div className="text-lg font-semibold text-slate-800">Ask a Doubt</div>
        <p className="mt-1 text-sm text-slate-500">
          Be specific so peers and teachers can help you faster.
        </p>

        <label className="mt-4 block text-sm font-medium text-slate-600">Your question</label>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. How do I find the derivative of sin(x²)?"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
          autoFocus
        />

        <label className="mt-4 block text-sm font-medium text-slate-600">Details (optional)</label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          placeholder="Add any context, what you've tried, etc."
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
        />

        <label className="mt-4 block text-sm font-medium text-slate-600">Subject (optional)</label>
        <input
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          placeholder="e.g. Mathematics"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
        />

        {error && <div className="mt-3 text-sm text-red-500">{error}</div>}

        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-md px-4 py-2 text-sm text-slate-500 hover:text-slate-700"
          >
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={saving}
            className="rounded-md bg-[#e8443b] px-5 py-2 text-sm font-semibold text-white hover:bg-[#d33c34] disabled:opacity-60"
          >
            {saving ? "Posting…" : "Post Doubt"}
          </button>
        </div>
      </div>
    </div>
  );
}
