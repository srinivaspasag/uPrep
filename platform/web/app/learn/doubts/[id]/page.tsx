"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "next/navigation";
import AnswerText from "@/components/AnswerText";
import GuidedAnswer, { type AnswerStep } from "@/components/GuidedAnswer";
import RelatedContent, { AiraRecommendedVideo, type RelatedItem } from "@/components/RelatedContent";
import { AiraAvatar } from "../layout";

type Answer = {
  id: string;
  content: string;
  userName: string;
  timeCreated: number;
  isAi?: boolean;
  steps?: AnswerStep[] | null;
};
type Doubt = {
  id: string;
  name: string;
  content: string;
  userName: string;
  subject: string | null;
  views: number;
  state: string;
  timeCreated: number;
};

function fullDate(ts: number): string {
  if (!ts) return "";
  return new Date(ts).toLocaleDateString(undefined, { day: "numeric", month: "long", year: "numeric" });
}

function timeAgo(ts: number): string {
  if (!ts) return "";
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

export default function DoubtConversationPage() {
  const params = useParams();
  const id = String(params.id);
  const [doubt, setDoubt] = useState<Doubt | null>(null);
  const [answers, setAnswers] = useState<Answer[]>([]);
  const [relatedContent, setRelatedContent] = useState<RelatedItem[]>([]);
  const [aiPending, setAiPending] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recommendedPlaying, setRecommendedPlaying] = useState(false);
  const [retrying, setRetrying] = useState(false);
  const [retryError, setRetryError] = useState<string | null>(null);
  const [reply, setReply] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    const res = await fetch(`/api/learn/doubts/${id}`);
    const data = await res.json();
    setDoubt(data.doubt || null);
    setAnswers(data.answers || []);
    setRelatedContent(data.relatedContent || []);
    setAiPending(!!data.aiPending);
    setLoading(false);
  }, [id]);

  async function retryAira() {
    setRetrying(true);
    setRetryError(null);
    const res = await fetch(`/api/learn/doubts/${id}/ai-answer`, { method: "POST" });
    const data = await res.json().catch(() => ({}));
    setRetrying(false);
    if (!res.ok) {
      setRetryError(data.error || "Aira still couldn't answer this one.");
      return;
    }
    if (data.pending) {
      setAiPending(true);
    } else {
      setAnswers((prev) =>
        prev.some((a) => a.id === data.id)
          ? prev
          : [...prev, { id: data.id, content: data.content, userName: data.userName, timeCreated: data.timeCreated, isAi: true, steps: data.steps || null }]
      );
    }
  }

  useEffect(() => {
    load();
  }, [load]);

  // Voice input for the reply box. Uses the browser's built-in speech
  // recognition (Chrome/Edge only) — no external API, no extra dependency.
  const [isListening, setIsListening] = useState(false);
  const [voiceSupported, setVoiceSupported] = useState(false);
  const recognitionRef = useRef<any>(null);

  useEffect(() => {
    const SpeechRecognitionCtor =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognitionCtor) return; // Firefox/Safari: mic button just won't render.

    const recognition = new SpeechRecognitionCtor();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = "en-IN";

    recognition.onresult = (event: any) => {
      let transcript = "";
      for (let i = 0; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      setReply(transcript);
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
      setReply("");
      recognition.start();
      setIsListening(true);
    }
  }

  // Aira's answer, computed early (before the loading/not-found guards
  // below) so the read-aloud effect has a stable hook order every render.
  const aiAnswer = answers.find((a) => a.isAi);
  const isGuided = !!aiAnswer?.steps && aiAnswer.steps.length > 1;

  const [ttsSupported, setTtsSupported] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const spokenIdRef = useRef<string | null>(null);

  useEffect(() => {
    if (typeof window !== "undefined" && "speechSynthesis" in window) setTtsSupported(true);
  }, []);

  // Without this, Aira keeps talking even after you've navigated away from
  // this doubt (Back button, opening a different doubt, etc.) — the browser
  // has no idea the page changed, since speechSynthesis runs independently
  // of React. This stops it the moment this page unmounts.
  useEffect(() => {
    return () => {
      if (typeof window !== "undefined" && "speechSynthesis" in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  // AnswerText.tsx strips markdown and renders LaTeX into visuals for the
  // screen, but speech synthesis only ever sees the raw source string — so
  // without this, it reads "$", "**", and LaTeX commands aloud literally.
  // This converts the same syntax into natural spoken words instead of
  // just deleting the symbols, so math actually sounds like math ("a over
  // b" instead of "a b" or "backslash frac a b").
  function sanitizeForSpeech(raw: string): string {
    let s = raw;

    // Common LaTeX commands → words. Must run before the generic
    // brace/backslash cleanup below, or \frac{a}{b} would be mangled
    // before we get a chance to read it properly.
    s = s.replace(/\\frac\{([^{}]+)\}\{([^{}]+)\}/g, "$1 over $2");
    s = s.replace(/\\sqrt\{([^{}]+)\}/g, "square root of $1");
    s = s.replace(/\\left|\\right/g, "");
    s = s.replace(/\\times/g, " times ");
    s = s.replace(/\\cdot/g, " times ");
    s = s.replace(/\\pm/g, " plus or minus ");
    s = s.replace(/\\pi/g, "pi");
    s = s.replace(/\\circ/g, " degrees ");
    s = s.replace(/\\[Dd]elta/g, "delta ");

    // Remaining LaTeX delimiters and any leftover \command we didn't name
    // above — drop the syntax, keep whatever text is left.
    s = s.replace(/\$\$([\s\S]*?)\$\$/g, "$1");
    s = s.replace(/\$([^$]*?)\$/g, "$1");
    s = s.replace(/\\[a-zA-Z]+/g, "");
    s = s.replace(/[{}]/g, "");

    // Markdown: bold markers, headers, list bullets/numbers.
    s = s.replace(/\*\*([^*]+)\*\*/g, "$1");
    s = s.replace(/^\s{0,3}#{1,6}\s+/gm, "");
    s = s.replace(/^\s*[-*]\s+/gm, "");
    s = s.replace(/^\s*\d+[.)]\s+/gm, "");

    // Superscript/subscript — "x^2" reads as "x to the power of 2"
    // instead of "x caret 2"; subscript carets just get dropped.
    s = s.replace(/\^/g, " to the power of ");
    s = s.replace(/_/g, " ");

    return s.replace(/\s+/g, " ").trim();
  }

  function speakAnswer() {
    if (!aiAnswer || typeof window === "undefined" || !("speechSynthesis" in window)) return;
    const rawText = isGuided
      ? (aiAnswer.steps || []).map((s) => `${s.title}. ${s.body}`).join(" ")
      : aiAnswer.steps?.[0]?.body || aiAnswer.content;
    const text = sanitizeForSpeech(rawText);
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "en-IN";
    utterance.onstart = () => setIsSpeaking(true);
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);
    window.speechSynthesis.speak(utterance);
  }

  function toggleSpeak() {
    if (isSpeaking) {
      window.speechSynthesis.cancel();
      setIsSpeaking(false);
    } else {
      speakAnswer();
    }
  }

  // Auto-read Aira's answer the first time it appears for this doubt — this
  // is what completes the "ask by voice, hear the answer back" loop. Follow-up
  // replies below don't get a fresh AI answer (see postAnswer), so this only
  // fires once per doubt, not on every reply.
  useEffect(() => {
    if (!aiAnswer || !ttsSupported || spokenIdRef.current === aiAnswer.id) return;
    spokenIdRef.current = aiAnswer.id;
    speakAnswer();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aiAnswer?.id, ttsSupported]);

  async function postAnswer() {
    if (!reply.trim()) return;
    setSaving(true);
    setError(null);
    const res = await fetch(`/api/learn/doubts/${id}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: reply.trim() }),
    });
    setSaving(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Could not post your reply.");
      return;
    }
    const created = await res.json();
    setAnswers((prev) => [...prev, created]);
    setReply("");
  }

  if (loading) {
    return <div className="flex h-full items-center justify-center text-sm text-[#8890A1]">Loading…</div>;
  }
  if (!doubt) {
    return <div className="flex h-full items-center justify-center text-sm text-[#8890A1]">This doubt could not be found.</div>;
  }

  const humanAnswers = answers.filter((a) => !a.isAi);
  // Only Guided answers get the explicit "Aira recommends" callout — a
  // single real video pulled from the same grounded relatedContentFor()
  // lookup as the strip below, not generated by the model. Pulled out of
  // the strip so it isn't shown twice.
  const recommendedVideo = isGuided ? relatedContent.find((i) => i.type === "VIDEO") : null;
  const remainingContent = recommendedVideo ? relatedContent.filter((i) => i.id !== recommendedVideo.id) : relatedContent;

  return (
    <div className="flex h-full flex-col">
      <div className="flex-1 overflow-y-auto px-6 py-6 sm:px-10">
        <div className="mx-auto max-w-2xl">
          <div className="text-center text-xs text-[#8890A1]">{fullDate(doubt.timeCreated)}</div>

          {/* Question, as a chat bubble on the right — this is "your message". */}
          <div className="mt-4 flex justify-end">
            <div className="max-w-[85%] rounded-2xl rounded-tr-sm bg-[#16233D] px-4 py-3 text-sm text-white">
              <div className="font-medium">{doubt.name}</div>
              {doubt.content && <div className="mt-1 whitespace-pre-wrap text-white/80">{doubt.content}</div>}
            </div>
          </div>
          {doubt.subject && <div className="mt-1.5 text-right text-[11px] text-[#8890A1]">{doubt.subject}</div>}

          {/* Aira's reply. */}
          <div className="mt-5 flex items-start gap-2.5">
            <AiraAvatar />
            <div className="min-w-0 flex-1 rounded-2xl rounded-tl-sm bg-white p-4 shadow-sm">
              <div className="flex items-center gap-2">
                <span className="text-sm font-semibold text-[#16233D]">Aira</span>
                <span className="rounded-full bg-blue-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-blue-700">
                  AI · verify with your teacher
                </span>
                {aiAnswer && ttsSupported && (
                  <button
                    type="button"
                    onClick={toggleSpeak}
                    title={isSpeaking ? "Stop reading" : "Read answer aloud"}
                    className={`ml-auto rounded-full p-1.5 transition ${
                      isSpeaking ? "bg-blue-100 text-blue-700" : "text-[#8890A1] hover:bg-[#EDEEE9] hover:text-[#3E4A63]"
                    }`}
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                      <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                      <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
                    </svg>
                  </button>
                )}
              </div>
              {aiAnswer ? (
                isGuided ? (
                  <div className="mt-2">
                    <GuidedAnswer steps={aiAnswer.steps!} />
                    {recommendedVideo && (
                      <AiraRecommendedVideo
                        item={recommendedVideo}
                        playing={recommendedPlaying}
                        onPlay={() => setRecommendedPlaying(true)}
                      />
                    )}
                  </div>
                ) : (
                  <AnswerText className="mt-2 text-sm leading-relaxed text-[#3E4A63]">
                    {aiAnswer.steps?.[0]?.body || aiAnswer.content}
                  </AnswerText>
                )
              ) : aiPending ? (
                <p className="mt-2 text-sm text-[#8890A1]">
                  Aira wasn't confident enough to answer this one directly — it's been sent to a teacher to take a
                  look. Check back soon, or a teacher may reply below in the meantime.
                </p>
              ) : (
                <div className="mt-2">
                  <p className="text-sm text-[#8890A1]">Aira had trouble answering this one — worth trying again.</p>
                  <button
                    onClick={retryAira}
                    disabled={retrying}
                    className="mt-2 rounded-lg border border-blue-300 bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-800 transition hover:bg-blue-100 disabled:opacity-60"
                  >
                    {retrying ? "Asking Aira…" : "Try again"}
                  </button>
                  {retryError && <p className="mt-1.5 text-xs text-red-500">{retryError}</p>}
                </div>
              )}
              <RelatedContent items={remainingContent} />
            </div>
          </div>

          {/* Human/teacher replies, if any. */}
          {humanAnswers.length > 0 && (
            <div className="mt-5 space-y-3">
              {humanAnswers.map((a) => (
                <div key={a.id} className="flex items-start gap-2.5">
                  <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-[#EDEEE9] text-xs font-semibold text-[#3E4A63]">
                    {(a.userName || "U").charAt(0).toUpperCase()}
                  </span>
                  <div className="min-w-0 flex-1 rounded-2xl rounded-tl-sm bg-white p-4 shadow-sm">
                    <div className="flex items-center gap-2 text-sm">
                      <span className="font-semibold text-[#16233D]">{a.userName}</span>
                      <span className="text-xs text-[#8890A1]">{timeAgo(a.timeCreated)}</span>
                    </div>
                    <AnswerText className="mt-1 text-sm leading-relaxed text-[#3E4A63]">{a.content}</AnswerText>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Follow-up composer, pinned to the bottom of the pane. */}
      <div className="border-t border-[#D9D6C9] bg-white px-6 py-4 sm:px-10">
        <div className="mx-auto flex max-w-2xl items-end gap-2">
          <textarea
            value={reply}
            onChange={(e) => setReply(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                postAnswer();
              }
            }}
            rows={1}
            placeholder={isListening ? "Listening…" : "Ask a follow-up or add a reply…"}
            className="max-h-32 flex-1 resize-none rounded-2xl border border-[#D9D6C9] px-4 py-2.5 text-sm text-[#16233D] outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
          />
          {voiceSupported && (
            <button
              type="button"
              onClick={toggleListening}
              title={isListening ? "Stop listening" : "Ask by voice"}
              className={`shrink-0 rounded-full p-2.5 transition ${
                isListening
                  ? "animate-pulse bg-red-500 text-white"
                  : "bg-[#EDEEE9] text-[#3E4A63] hover:bg-[#e2e0d5]"
              }`}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
                <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                <line x1="12" y1="19" x2="12" y2="23" />
                <line x1="8" y1="23" x2="16" y2="23" />
              </svg>
            </button>
          )}
          <button
            onClick={postAnswer}
            disabled={saving || !reply.trim()}
            className="shrink-0 rounded-full bg-[#e8443b] px-5 py-2.5 text-sm font-semibold text-white shadow-sm shadow-red-900/10 transition hover:bg-[#d33c34] disabled:opacity-60"
          >
            {saving ? "…" : "Send"}
          </button>
        </div>
        {error && <div className="mx-auto mt-2 max-w-2xl text-sm text-red-500">{error}</div>}
      </div>
    </div>
  );
}
