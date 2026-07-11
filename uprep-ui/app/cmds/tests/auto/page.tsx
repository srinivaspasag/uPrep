"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type LibQuestion = { id: string; text: string; type: string };

export default function AutoGenerateTestPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [count, setCount] = useState(5);
  const [durationMin, setDurationMin] = useState(30);
  const [positive, setPositive] = useState(4);
  const [negative, setNegative] = useState(1);
  const [available, setAvailable] = useState<LibQuestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const s = getSession();
    setSession(s);
    if (!s) return;
    fetch(`/api/cmds/tests`)
      .then((r) => r.json())
      .then((d) => setAvailable(d.questions || []))
      .finally(() => setLoading(false));
  }, []);

  async function generate() {
    setError("");
    if (!name.trim()) return setError("Please enter a test name.");
    if (available.length === 0) return setError("No published questions to draw from.");
    const n = Math.min(count, available.length);

    // Random sample of n questions.
    const pool = [...available];
    for (let i = pool.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [pool[i], pool[j]] = [pool[j], pool[i]];
    }
    const questionIds = pool.slice(0, n).map((q) => q.id);

    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tests", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          sectionName: "Auto Generated",
          durationMin,
          positive,
          negative,
          questionIds,
          userId: session?.id,
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Failed to generate test");
        return;
      }
      router.push("/cmds");
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell active="resources">
      <div className="mx-auto max-w-[560px] px-6 py-8">
        <div className="mb-4 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          / <span className="text-slate-600">Auto Generate Test</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">Auto Generate Test</h1>
        <p className="mt-1 text-sm text-slate-400">
          {loading
            ? "Loading question bank…"
            : `${available.length} published question(s) available.`}
        </p>

        <div className="mt-6 space-y-5">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Test name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="e.g. Physics Auto Test 1"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <NumField label="No. of questions" value={count} onChange={setCount} min={1} />
            <NumField label="Duration (min)" value={durationMin} onChange={setDurationMin} min={1} />
            <NumField label="Marks (correct)" value={positive} onChange={setPositive} min={1} />
            <NumField label="Negative marks" value={negative} onChange={setNegative} min={0} />
          </div>

          {error && <div className="text-sm text-red-600">{error}</div>}

          <div className="flex gap-3 pt-2">
            <button
              onClick={generate}
              disabled={saving || loading}
              className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Generating…" : "Generate Test"}
            </button>
            <Link href="/cmds" className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100">
              Cancel
            </Link>
          </div>
        </div>
      </div>
    </CmdsShell>
  );
}

function NumField({
  label,
  value,
  onChange,
  min,
}: {
  label: string;
  value: number;
  onChange: (n: number) => void;
  min: number;
}) {
  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-slate-600">{label}</label>
      <input
        type="number"
        min={min}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
      />
    </div>
  );
}
