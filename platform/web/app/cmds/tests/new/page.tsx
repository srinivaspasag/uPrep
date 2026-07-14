"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";

type LibQuestion = {
  id: string;
  text: string;
  type: string;
  options: number;
  difficulty: string | null;
  hasKey: boolean;
};

export default function NewTestPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);

  const [name, setName] = useState("");
  const [sectionName, setSectionName] = useState("General");
  const [durationMin, setDurationMin] = useState(30);
  const [positive, setPositive] = useState(4);
  const [negative, setNegative] = useState(1);

  const [password, setPassword] = useState("");
  const [enablePartialMarks, setEnablePartialMarks] = useState(false);
  const [enableSectionLocking, setEnableSectionLocking] = useState(false);
  const [autoResumeTest, setAutoResumeTest] = useState(false);
  const [resultVisibility, setResultVisibility] = useState("VISIBLE");

  const [pool, setPool] = useState<LibQuestion[]>([]);
  const [picked, setPicked] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
    (async () => {
      try {
        const r = await fetch("/api/cmds/tests");
        const d = await r.json();
        if (d.error) setError(d.error);
        setPool((d.questions || []).filter((q: LibQuestion) => q.hasKey));
      } catch {
        setError("Failed to load published questions.");
      } finally {
        setLoading(false);
      }
    })();
  }, [router]);

  function toggle(id: string) {
    setPicked((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!name.trim()) return setError("Enter a test name.");
    if (picked.length === 0) return setError("Select at least one question.");

    setSaving(true);
    try {
      const r = await fetch("/api/cmds/tests", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: session?.id,
          name: name.trim(),
          sectionName: sectionName.trim() || "General",
          durationMin,
          positive,
          negative,
          questionIds: picked,
          password: password.trim(),
          enablePartialMarks,
          enableSectionLocking,
          autoResumeTest,
          resultVisibility,
        }),
      });
      const d = await r.json();
      if (!r.ok || d.error) {
        setError(d.error || "Failed to create test.");
        setSaving(false);
        return;
      }
      router.push("/cmds");
    } catch {
      setError("Failed to create test.");
      setSaving(false);
    }
  }

  const totalMarks = picked.length * positive;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="mx-auto max-w-3xl px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-slate-800 flex items-center justify-center text-white font-bold">
              C
            </div>
            <span className="font-semibold text-slate-800">UPrep CMDS</span>
          </div>
          <Link href="/cmds" className="text-sm text-blue-600 hover:underline">
            ← Resources
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-slate-800">Create Test</h1>
        <p className="mt-1 text-slate-500">
          Compose a test from published questions. It becomes attemptable and auto-graded in the
          learn app immediately.
        </p>

        <form onSubmit={submit} className="mt-6 space-y-6">
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Test name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Physics — Weekly Practice #2"
              className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />

            <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div>
                <label className="block text-sm font-medium text-slate-700">Section</label>
                <input
                  value={sectionName}
                  onChange={(e) => setSectionName(e.target.value)}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Duration (min)</label>
                <input
                  type="number"
                  min={1}
                  value={durationMin}
                  onChange={(e) => setDurationMin(Number(e.target.value))}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Marks / correct</label>
                <input
                  type="number"
                  min={1}
                  value={positive}
                  onChange={(e) => setPositive(Number(e.target.value))}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Negative</label>
                <input
                  type="number"
                  min={0}
                  value={negative}
                  onChange={(e) => setNegative(Number(e.target.value))}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
            </div>
          </div>

          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Rules & settings</label>
            <div className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-medium text-slate-500">Access password (optional)</label>
                <input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Leave blank for open access"
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500">Result visibility</label>
                <select
                  value={resultVisibility}
                  onChange={(e) => setResultVisibility(e.target.value)}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700"
                >
                  <option value="VISIBLE">Show immediately</option>
                  <option value="HIDDEN">Hide from students</option>
                  <option value="AFTER_END">After test window ends</option>
                </select>
              </div>
            </div>
            <div className="mt-3 flex flex-wrap gap-4 text-sm text-slate-600">
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={enablePartialMarks} onChange={(e) => setEnablePartialMarks(e.target.checked)} />
                Partial marks (MCQ)
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={enableSectionLocking} onChange={(e) => setEnableSectionLocking(e.target.checked)} />
                Section locking
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={autoResumeTest} onChange={(e) => setAutoResumeTest(e.target.checked)} />
                Auto-resume
              </label>
            </div>
          </div>

          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <div className="flex items-center justify-between">
              <label className="block text-sm font-medium text-slate-700">
                Questions <span className="text-slate-400">(published, with answer keys)</span>
              </label>
              <span className="text-sm text-slate-500">
                {picked.length} selected · {totalMarks} marks
              </span>
            </div>

            {loading && <div className="mt-4 text-slate-500">Loading…</div>}
            {!loading && pool.length === 0 && (
              <div className="mt-4 rounded-md bg-amber-50 px-3 py-3 text-sm text-amber-700 ring-1 ring-amber-200">
                No published questions yet. Publish some questions first, then create a test.
              </div>
            )}

            <div className="mt-3 space-y-2">
              {pool.map((q, i) => (
                <label
                  key={q.id}
                  className="flex cursor-pointer items-start gap-3 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    className="mt-1 h-4 w-4 accent-blue-600"
                    checked={picked.includes(q.id)}
                    onChange={() => toggle(q.id)}
                  />
                  <span className="flex-1">
                    <span className="text-slate-800">
                      {picked.includes(q.id) && (
                        <span className="mr-1 text-xs font-semibold text-blue-600">
                          #{picked.indexOf(q.id) + 1}
                        </span>
                      )}
                      {q.text || `Question ${i + 1}`}
                    </span>
                    <span className="mt-0.5 block text-xs text-slate-400">
                      {q.type} · {q.options} options · {q.difficulty || "—"}
                    </span>
                  </span>
                </label>
              ))}
            </div>
          </div>

          {error && (
            <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving || pool.length === 0}
              className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {saving ? "Creating…" : "Create Test"}
            </button>
            <Link
              href="/cmds"
              className="rounded-md border border-slate-300 px-5 py-2.5 text-sm text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </Link>
          </div>
        </form>
      </main>
    </div>
  );
}
