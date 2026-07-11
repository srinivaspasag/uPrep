"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Notification = {
  id: string;
  title: string;
  message: string;
  resourceType: string | null;
  sentAt: number;
};

const RESOURCE_TYPES = [
  { v: "MODULE", label: "Module" },
  { v: "VIDEO", label: "Video" },
  { v: "TEST", label: "Test" },
  { v: "DOCUMENT", label: "E-Book" },
];

export default function NotificationsPage() {
  const [session, setSession] = useState<UprepSession | null>(null);
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  const [summary, setSummary] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [resourceOn, setResourceOn] = useState(false);
  const [resourceType, setResourceType] = useState("MODULE");
  const [resourceId, setResourceId] = useState("");
  const [sending, setSending] = useState(false);
  const [msg, setMsg] = useState("");
  const [history, setHistory] = useState<Notification[]>([]);

  useEffect(() => {
    setSession(getSession());
  }, []);

  async function loadHistory() {
    const d = await (await fetch("/api/cmds/tools/notifications")).json();
    setHistory(d.history || []);
  }
  useEffect(() => {
    loadHistory();
  }, []);

  async function send() {
    setMsg("");
    if (!title.trim()) return setMsg("Notification title is required.");
    if (!message.trim()) return setMsg("Notification message is required.");
    setSending(true);
    try {
      const res = await fetch("/api/cmds/tools/notifications", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          message,
          summary,
          imageUrl,
          resourceType: resourceOn ? resourceType : undefined,
          resourceId: resourceOn ? resourceId : undefined,
          userId: session?.id,
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setMsg(d.error || "Send failed");
        return;
      }
      setMsg("Notification sent.");
      setTitle("");
      setMessage("");
      setSummary("");
      setImageUrl("");
      setResourceId("");
      setResourceOn(false);
      loadHistory();
    } finally {
      setSending(false);
    }
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[720px] px-8 py-6">
        <h1 className="text-2xl font-light uppercase tracking-wide text-slate-700">Push Notification</h1>

        <div className="mt-6 space-y-4">
          <Field label="Notification Title*" value={title} onChange={setTitle} maxLength={40} />
          <Field label="Notification Message*" value={message} onChange={setMessage} />
          <Field label="Notification summary" value={summary} onChange={setSummary} />
          <Field label="Url of Image" value={imageUrl} onChange={setImageUrl} />

          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={resourceOn}
              onChange={(e) => setResourceOn(e.target.checked)}
              className="accent-emerald-600"
            />
            Send Resource Specific Message
          </label>

          {resourceOn && (
            <div className="rounded border border-slate-200 p-4">
              <div className="mb-2 text-sm font-medium text-slate-600">Resource Type*</div>
              <div className="flex flex-wrap gap-4 text-sm text-slate-600">
                {RESOURCE_TYPES.map((r) => (
                  <label key={r.v} className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="resourceType"
                      checked={resourceType === r.v}
                      onChange={() => setResourceType(r.v)}
                      className="accent-emerald-600"
                    />
                    {r.label}
                  </label>
                ))}
              </div>
              <div className="mt-3">
                <Field label="Resource ID*" value={resourceId} onChange={setResourceId} />
              </div>
            </div>
          )}

          <div className="flex items-center gap-3 pt-1">
            <div className="text-xs text-slate-400">* Fields are Mandatory</div>
            <div className="flex-1" />
            <button
              onClick={send}
              disabled={sending}
              className="rounded bg-emerald-600 px-6 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {sending ? "Sending…" : "Send"}
            </button>
          </div>
          {msg && (
            <div className={msg.includes("sent") ? "text-sm text-emerald-600" : "text-sm text-red-600"}>
              {msg}
            </div>
          )}
        </div>

        {history.length > 0 && (
          <div className="mt-10">
            <h2 className="text-sm font-semibold text-slate-600">Recently Sent</h2>
            <div className="mt-3 divide-y divide-slate-100 rounded border border-slate-200">
              {history.map((n) => (
                <div key={n.id} className="px-4 py-3">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-slate-800">{n.title}</span>
                    <span className="text-xs text-slate-400">
                      {n.sentAt ? new Date(n.sentAt).toLocaleString() : ""}
                    </span>
                  </div>
                  <div className="text-sm text-slate-500">{n.message}</div>
                  {n.resourceType && (
                    <span className="mt-1 inline-block rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500">
                      {n.resourceType}
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}

function Field({
  label,
  value,
  onChange,
  maxLength,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  maxLength?: number;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-slate-600">Enter the {label}</span>
      <input
        value={value}
        maxLength={maxLength}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
      />
    </label>
  );
}
