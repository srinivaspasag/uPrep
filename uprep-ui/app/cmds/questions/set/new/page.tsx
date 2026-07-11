"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type LibQuestion = {
  id: string;
  text: string;
  type: string;
  options: number;
  hasKey: boolean;
};

export default function NewQuestionSetPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [pool, setPool] = useState<LibQuestion[]>([]);
  const [picked, setPicked] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const s = getSession();
    setSession(s);
    if (!s) return;
    fetch(`/api/cmds/tests`)
      .then((r) => r.json())
      .then((d) => setPool(d.questions || []))
      .finally(() => setLoading(false));
  }, []);

  function toggle(id: string) {
    setPicked((p) => (p.includes(id) ? p.filter((x) => x !== id) : [...p, id]));
  }

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Please enter a set name.");
    if (picked.length === 0) return setError("Pick at least one question.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/content", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          kind: "questionset",
          name: name.trim(),
          subject: subject.trim(),
          qIds: picked,
          userId: session?.id,
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Failed to create question set");
        return;
      }
      router.push("/cmds");
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell active="resources">
      <div className="mx-auto max-w-[760px] px-6 py-8">
        <div className="mb-4 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          / <span className="text-slate-600">Add a Question Set</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">Add a Question Set</h1>

        <div className="mt-6 grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Set name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="e.g. Newton's Laws – Practice"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Subject</label>
            <input
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="Physics (optional)"
            />
          </div>
        </div>

        <div className="mt-6">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-600">
              Published questions ({picked.length} selected)
            </h2>
            <Link href="/cmds/questions" className="text-xs text-blue-600 hover:underline">
              Manage / publish questions →
            </Link>
          </div>
          {loading ? (
            <div className="py-8 text-center text-slate-400">Loading questions…</div>
          ) : pool.length === 0 ? (
            <div className="rounded border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
              No published questions yet. Author and publish questions first.
            </div>
          ) : (
            <div className="max-h-[360px] space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
              {pool.map((q) => (
                <label
                  key={q.id}
                  className="flex cursor-pointer items-start gap-3 rounded px-2 py-2 text-sm hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={picked.includes(q.id)}
                    onChange={() => toggle(q.id)}
                    className="mt-0.5 accent-emerald-600"
                  />
                  <span className="flex-1 text-slate-700">{q.text || "(no text)"}</span>
                  <span className="text-xs text-slate-400">{q.type}</span>
                </label>
              ))}
            </div>
          )}
        </div>

        {error && <div className="mt-4 text-sm text-red-600">{error}</div>}

        <div className="mt-6 flex gap-3">
          <button
            onClick={submit}
            disabled={saving}
            className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? "Creating…" : "Create Question Set"}
          </button>
          <Link href="/cmds" className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </Link>
        </div>
      </div>
    </CmdsShell>
  );
}
