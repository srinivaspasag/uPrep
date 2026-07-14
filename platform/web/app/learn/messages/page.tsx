"use client";

import { useEffect, useRef, useState } from "react";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Msg = { id: string; userId: string; userName: string; text: string; at: number };

export default function MessagesPage() {
  const [messages, setMessages] = useState<Msg[]>([]);
  const [text, setText] = useState("");
  const lastAt = useRef(0);
  const bottomRef = useRef<HTMLDivElement>(null);
  const myId = getSession()?.id || "";

  async function poll() {
    const res = await fetch(`/api/learn/messages?since=${lastAt.current}`);
    const d = await res.json();
    const incoming: Msg[] = d.messages || [];
    if (incoming.length) {
      lastAt.current = incoming[incoming.length - 1].at;
      setMessages((prev) => {
        const seen = new Set(prev.map((m) => m.id));
        return [...prev, ...incoming.filter((m) => !seen.has(m.id))];
      });
    }
  }

  useEffect(() => {
    poll();
    const t = setInterval(poll, 4000);
    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function send(e: React.FormEvent) {
    e.preventDefault();
    const t = text.trim();
    if (!t) return;
    setText("");
    const s = getSession();
    await fetch("/api/learn/messages", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: s?.id,
        userName: [s?.firstName, s?.lastName].filter(Boolean).join(" ") || "Student",
        text: t,
      }),
    });
    poll();
  }

  return (
    <LmsShell active="messages">
      <h1 className="text-lg font-semibold text-slate-800">Class Messages</h1>
      <p className="mt-1 text-xs text-slate-400"># general — visible to your batch</p>

      <div className="mt-4 flex h-[60vh] flex-col rounded-lg border border-slate-200">
        <div className="flex-1 space-y-3 overflow-y-auto p-4">
          {messages.length === 0 ? (
            <div className="py-16 text-center text-sm text-slate-400">
              No messages yet. Say hello 👋
            </div>
          ) : (
            messages.map((m) => {
              const mine = m.userId === myId;
              return (
                <div key={m.id} className={`flex ${mine ? "justify-end" : "justify-start"}`}>
                  <div
                    className={`max-w-[75%] rounded-lg px-3 py-2 text-sm ${
                      mine ? "bg-emerald-500 text-white" : "bg-slate-100 text-slate-700"
                    }`}
                  >
                    {!mine && <div className="text-xs font-semibold text-slate-500">{m.userName}</div>}
                    <div>{m.text}</div>
                    <div className={`mt-0.5 text-[10px] ${mine ? "text-emerald-100" : "text-slate-400"}`}>
                      {new Date(m.at).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                    </div>
                  </div>
                </div>
              );
            })
          )}
          <div ref={bottomRef} />
        </div>
        <form onSubmit={send} className="flex gap-2 border-t border-slate-100 p-3">
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Type a message…"
            className="flex-1 rounded-full border border-slate-300 px-4 py-2 text-sm outline-none focus:border-emerald-400"
          />
          <button className="rounded-full bg-[#e8443b] px-5 py-2 text-sm font-semibold text-white hover:bg-[#d33c34]">
            Send
          </button>
        </form>
      </div>
    </LmsShell>
  );
}
