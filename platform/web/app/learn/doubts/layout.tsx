"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import LmsShell from "@/components/LmsShell";
import BoardPicker from "@/components/BoardPicker";

type StateFilter = "open" | "resolved";

type Doubt = {
  id: string;
  name: string;
  content: string;
  subject: string | null;
  answerCount: number;
  state: string;
  timeCreated: number;
};

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  if (s < 86400 * 7) return `${Math.floor(s / 86400)}d ago`;
  return new Date(ts).toLocaleDateString(undefined, { month: "short", day: "numeric" });
}

// Aira's avatar — a small gradient blob with a face, standing in for a real
// mascot illustration (none exists yet) without pulling in an image asset.
export function AiraAvatar({ size = "sm" }: { size?: "sm" | "md" }) {
  const dim = size === "md" ? "h-9 w-9 text-base" : "h-7 w-7 text-sm";
  return (
    <span
      className={`flex ${dim} shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-sky-300 via-blue-400 to-indigo-500 shadow-sm`}
    >
      ✨
    </span>
  );
}

export default function DoubtsLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [items, setItems] = useState<Doubt[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<StateFilter>("open");
  const [showAsk, setShowAsk] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    const res = await fetch(`/api/learn/doubts?state=${filter}`);
    const data = await res.json();
    setItems(data.items || []);
    setLoading(false);
  }, [filter]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <LmsShell active="doubts" fullWidth>
      <div className="flex h-[calc(100vh-52px)]">
        {/* My Doubts rail */}
        <aside className="flex w-[300px] shrink-0 flex-col border-r border-[#D9D6C9] bg-white">
          <div className="flex items-center gap-2 border-b border-[#D9D6C9] px-4 py-4">
            <AiraAvatar size="md" />
            <div>
              <div className="font-serif text-base font-semibold text-[#16233D]">Ask Aira</div>
              <div className="text-[11px] text-[#8890A1]">Your AI study assistant</div>
            </div>
          </div>

          <div className="px-4 pt-4">
            <button
              onClick={() => setShowAsk(true)}
              className="flex w-full items-center justify-center gap-1.5 rounded-full bg-amber-100 py-2.5 text-sm font-semibold text-amber-900 transition hover:bg-amber-200"
            >
              <span className="text-base leading-none">+</span> New Doubt
            </button>
          </div>

          <div className="px-4 pt-5 text-xs font-semibold uppercase tracking-wide text-[#8890A1]">My Doubts</div>
          <div className="flex gap-1.5 px-4 pt-2">
            {(["open", "resolved"] as StateFilter[]).map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`rounded-full px-3 py-1 text-xs font-medium capitalize transition ${
                  filter === f ? "bg-[#16233D] text-white" : "bg-[#EDEEE9] text-[#3E4A63] hover:bg-[#e2e0d8]"
                }`}
              >
                {f}
              </button>
            ))}
          </div>

          <div className="mt-3 flex-1 overflow-y-auto">
            {loading ? (
              <div className="px-4 py-8 text-center text-xs text-[#8890A1]">Loading…</div>
            ) : items.length === 0 ? (
              <div className="px-4 py-8 text-center text-xs text-[#8890A1]">
                {filter === "open" ? "No open doubts. Ask Aira something!" : "Nothing resolved yet."}
              </div>
            ) : (
              items.map((d) => {
                const isActive = pathname === `/learn/doubts/${d.id}`;
                return (
                  <Link
                    key={d.id}
                    href={`/learn/doubts/${d.id}`}
                    className={`block border-b border-[#F0EFE9] px-4 py-3 transition ${
                      isActive ? "bg-amber-50" : "hover:bg-[#F8F7F3]"
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      {d.subject && (
                        <span className="truncate text-[10px] font-semibold uppercase tracking-wide text-[#8890A1]">
                          {d.subject}
                        </span>
                      )}
                      <span className="shrink-0 text-[10px] text-[#8890A1]">{timeAgo(d.timeCreated)}</span>
                    </div>
                    <div className="mt-0.5 truncate text-sm font-medium text-[#16233D]">{d.name}</div>
                  </Link>
                );
              })
            )}
          </div>
        </aside>

        {/* Conversation pane */}
        <div className="min-w-0 flex-1 overflow-y-auto bg-[#F8F7F3]">{children}</div>
      </div>

      {showAsk && (
        <AskDoubtModal
          onClose={() => setShowAsk(false)}
          onPosted={(id) => {
            setShowAsk(false);
            setFilter("open");
            load();
            router.push(`/learn/doubts/${id}`);
          }}
        />
      )}
    </LmsShell>
  );
}

type AnswerMode = "detailed" | "short" | "guided";

const MODE_OPTIONS: { key: AnswerMode; icon: string; label: string; hint: string }[] = [
  { key: "detailed", icon: "📋", label: "Detailed", hint: "In-depth explanation" },
  { key: "short", icon: "📝", label: "Short", hint: "Key summary" },
  { key: "guided", icon: "🧭", label: "Guided", hint: "Step by step" },
];

function AskDoubtModal({
  onClose,
  onPosted,
}: {
  onClose: () => void;
  onPosted: (id: string) => void;
}) {
  const [name, setName] = useState("");
  const [content, setContent] = useState("");
  const [subject, setSubject] = useState("");
  const [boardIds, setBoardIds] = useState<string[]>([]);
  const [mode, setMode] = useState<AnswerMode>("guided");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Voice input for the question field — same pattern as the follow-up
  // composer in [id]/page.tsx.
  const [isListening, setIsListening] = useState(false);
  const [voiceSupported, setVoiceSupported] = useState(false);
  const recognitionRef = useRef<any>(null);

  useEffect(() => {
    const SpeechRecognitionCtor =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognitionCtor) return;

    const recognition = new SpeechRecognitionCtor();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = "en-IN";
    recognition.onresult = (event: any) => {
      let transcript = "";
      for (let i = 0; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      setName(transcript);
    };
    recognition.onend = () => setIsListening(false);
    recognition.onerror = () => setIsListening(false);

    recognitionRef.current = recognition;
    setVoiceSupported(true);

    return () => recognition.abort();
  }, []);

  function toggleListening() {
    const recognition = recognitionRef.current;
    if (!recognition) return;
    if (isListening) {
      recognition.stop();
    } else {
      setName("");
      recognition.start();
      setIsListening(true);
    }
  }

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
        mode,
      }),
    });
    setSaving(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Could not post your doubt.");
      return;
    }
    const created = await res.json();
    onPosted(created.id);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#16233D]/50 p-4 backdrop-blur-[2px]">
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl sm:p-7">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2.5">
            <AiraAvatar size="md" />
            <div>
              <div className="font-serif text-xl font-semibold text-[#16233D]">Ask Aira</div>
              <p className="text-sm text-[#8890A1]">Be specific — Aira answers right away.</p>
            </div>
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
        <div className="relative mt-1.5">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={isListening ? "Listening…" : "e.g. How do I find the derivative of sin(x²)?"}
            className="w-full rounded-lg border border-[#D9D6C9] px-3 py-2.5 pr-10 text-sm text-[#16233D] outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
            autoFocus
          />
          {voiceSupported && (
            <button
              type="button"
              onClick={toggleListening}
              title={isListening ? "Stop listening" : "Ask by voice"}
              className={`absolute right-1.5 top-1/2 -translate-y-1/2 rounded-full p-1.5 transition ${
                isListening
                  ? "animate-pulse bg-red-500 text-white"
                  : "text-[#8890A1] hover:bg-[#EDEEE9] hover:text-[#3E4A63]"
              }`}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
                <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                <line x1="12" y1="19" x2="12" y2="23" />
                <line x1="8" y1="23" x2="16" y2="23" />
              </svg>
            </button>
          )}
        </div>

        <label className="mt-4 block text-sm font-medium text-[#3E4A63]">Answer style</label>
        <div className="mt-1.5 grid grid-cols-3 gap-2">
          {MODE_OPTIONS.map((m) => (
            <button
              key={m.key}
              type="button"
              onClick={() => setMode(m.key)}
              className={`rounded-lg border px-3 py-2 text-left transition ${
                mode === m.key
                  ? "border-amber-400 bg-amber-50 ring-1 ring-amber-400"
                  : "border-[#D9D6C9] hover:border-amber-300 hover:bg-amber-50/50"
              }`}
            >
              <div className="flex items-center gap-1.5 text-sm font-semibold text-[#16233D]">
                <span>{m.icon}</span>
                {m.label}
              </div>
              <div className="mt-0.5 text-[11px] text-[#8890A1]">{m.hint}</div>
            </button>
          ))}
        </div>

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
              Subject: <span className="font-medium text-[#3E4A63]">{subject}</span> — helps Aira ground the
              answer in the right chapter.
            </p>
          )}
        </div>

        {error && <div className="mt-3 text-sm text-red-500">{error}</div>}

        <div className="mt-6 flex justify-end gap-3">
          <button onClick={onClose} className="rounded-lg px-4 py-2 text-sm text-[#8890A1] hover:text-[#16233D]">
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={saving}
            className="rounded-lg bg-[#e8443b] px-5 py-2.5 text-sm font-semibold text-white shadow-sm shadow-red-900/10 transition hover:bg-[#d33c34] disabled:opacity-60"
          >
            {saving ? "Asking Aira…" : "Ask Aira"}
          </button>
        </div>
      </div>
    </div>
  );
}
