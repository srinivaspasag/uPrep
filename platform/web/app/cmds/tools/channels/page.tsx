"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Channel = { id: string; name: string; contentCount: number };

export default function ChannelsPage() {
  const [session, setSession] = useState<UprepSession | null>(null);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setSession(getSession());
  }, []);

  async function load() {
    setLoading(true);
    try {
      const d = await (await fetch("/api/cmds/tools/channels")).json();
      setChannels(d.channels || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);

  async function create() {
    setError("");
    if (!name.trim()) return setError("Title is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/channels", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: name.trim(), userId: session?.id }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Create failed");
        return;
      }
      setName("");
      setOpen(false);
      load();
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[900px] px-8 py-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-light text-slate-700">Channels</h1>
          <button
            onClick={() => setOpen(true)}
            className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
          >
            + Create Channel
          </button>
        </div>

        <div className="mt-5 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Name</th>
                <th className="px-4 py-2 font-medium">No. of challenges</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={2} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : channels.length === 0 ? (
                <tr>
                  <td colSpan={2} className="px-4 py-10 text-center text-slate-400">
                    No Channels found.
                  </td>
                </tr>
              ) : (
                channels.map((c) => (
                  <tr key={c.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-700">{c.name}</td>
                    <td className="px-4 py-3 text-slate-500">{c.contentCount}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-[380px] rounded-lg bg-white p-6 shadow-xl">
            <h3 className="text-lg font-semibold text-slate-800">Create Channel</h3>
            <label className="mt-4 block text-sm">
              <span className="mb-1 block text-slate-600">Title*</span>
              <input
                autoFocus
                maxLength={256}
                value={name}
                onChange={(e) => setName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && create()}
                className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              />
            </label>
            {error && <div className="mt-2 text-sm text-red-600">{error}</div>}
            <div className="mt-5 flex justify-end gap-2">
              <button
                onClick={() => setOpen(false)}
                className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                onClick={create}
                disabled={saving}
                className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {saving ? "Creating…" : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}
    </CmdsShell>
  );
}
