"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Resource = {
  id: string;
  title: string;
  type: string;
  subject: string | null;
};

const TYPE_ICON: Record<string, string> = {
  DOCUMENT: "📄",
  VIDEO: "🎬",
  TEST: "📕",
  QUESTION_SET: "🟦",
};

export default function NewModulePage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [pool, setPool] = useState<Resource[]>([]);
  const [picked, setPicked] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const s = getSession();
    setSession(s);
    if (!s) return;
    fetch(`/api/cmds/content`)
      .then((r) => r.json())
      .then((d) => {
        // Modules can contain documents, videos, tests, question sets.
        const items: Resource[] = (d.resources || []).filter((r: Resource) =>
          ["DOCUMENT", "VIDEO", "TEST", "QUESTION_SET"].includes(r.type)
        );
        setPool(items);
      })
      .finally(() => setLoading(false));
  }, []);

  function toggle(id: string) {
    setPicked((p) => (p.includes(id) ? p.filter((x) => x !== id) : [...p, id]));
  }

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Please enter a module name.");
    if (picked.length === 0) return setError("Pick at least one item for the module.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/content", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          kind: "module",
          name: name.trim(),
          subject: subject.trim(),
          contentIds: picked,
          userId: session?.id,
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Failed to create module");
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
          / <span className="text-slate-600">Create a Module</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">Create a Module</h1>

        <div className="mt-6 grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Module name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="e.g. Kinematics Basics"
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
              Content ({picked.length} selected)
            </h2>
          </div>
          {loading ? (
            <div className="py-8 text-center text-slate-400">Loading content…</div>
          ) : pool.length === 0 ? (
            <div className="rounded border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
              No content yet. Add documents, videos or tests first.
            </div>
          ) : (
            <div className="max-h-[340px] space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
              {pool.map((r) => (
                <label
                  key={r.id}
                  className="flex cursor-pointer items-center gap-3 rounded px-2 py-2 text-sm hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={picked.includes(r.id)}
                    onChange={() => toggle(r.id)}
                    className="accent-emerald-600"
                  />
                  <span>{TYPE_ICON[r.type] || "📄"}</span>
                  <span className="flex-1 text-slate-700">{r.title}</span>
                  <span className="text-xs text-slate-400">{r.type}</span>
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
            {saving ? "Creating…" : "Create Module"}
          </button>
          <Link href="/cmds" className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </Link>
        </div>
      </div>
    </CmdsShell>
  );
}
