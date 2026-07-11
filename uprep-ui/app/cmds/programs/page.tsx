"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Program = {
  id: string;
  name: string;
  code: string | null;
  description: string;
  isOffline: boolean;
  sectionCount: number;
};

export default function CmdsProgramsPage() {
  const [session, setSession] = useState<UprepSession | null>(null);
  const [programs, setPrograms] = useState<Program[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setSession(getSession());
  }, []);

  async function load() {
    setLoading(true);
    try {
      const d = await (await fetch("/api/cmds/programs")).json();
      setPrograms(d.programs || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);

  async function create() {
    setError("");
    if (!name.trim()) return setError("Program name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/programs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: name.trim(), description, userId: session?.id }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Create failed");
        return;
      }
      setName("");
      setDescription("");
      setOpen(false);
      load();
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell active="programs">
      <div className="mx-auto max-w-[1000px] px-8 py-8">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-light text-slate-700">Programs</h1>
          <button
            onClick={() => setOpen(true)}
            className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
          >
            + Create Program
          </button>
        </div>

        <div className="mt-6">
          {loading ? (
            <div className="py-12 text-center text-slate-400">Loading programs…</div>
          ) : programs.length === 0 ? (
            <div className="rounded border border-dashed border-slate-200 py-16 text-center">
              <div className="text-4xl">📚</div>
              <div className="mt-3 text-slate-500">No programs yet</div>
              <div className="mt-1 text-sm text-slate-400">
                Create a program to organize content and enroll students.
              </div>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {programs.map((p) => (
                <Link
                  key={p.id}
                  href={`/cmds/programs/${p.id}`}
                  className="block rounded-lg border border-slate-200 p-4 transition hover:shadow-md"
                >
                  <div className="flex items-start justify-between">
                    <div className="text-base font-medium text-slate-800">{p.name}</div>
                    {p.isOffline && (
                      <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-600">
                        Offline
                      </span>
                    )}
                  </div>
                  {p.description && (
                    <div className="mt-1 line-clamp-2 text-sm text-slate-500">{p.description}</div>
                  )}
                  <div className="mt-3 flex items-center gap-3 text-xs text-slate-400">
                    <span>🏫 {p.sectionCount} section(s)</span>
                    {p.code && <span>#{p.code}</span>}
                  </div>
                  <div className="mt-3 text-xs font-medium text-blue-600">Manage program →</div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>

      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-[420px] rounded-lg bg-white p-6 shadow-xl">
            <h3 className="text-lg font-semibold text-slate-800">Create Program</h3>
            <label className="mt-4 block text-sm">
              <span className="mb-1 block text-slate-600">Program name*</span>
              <input
                autoFocus
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
                placeholder="e.g. NEET 2027"
              />
            </label>
            <label className="mt-3 block text-sm">
              <span className="mb-1 block text-slate-600">Description</span>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="h-20 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
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
