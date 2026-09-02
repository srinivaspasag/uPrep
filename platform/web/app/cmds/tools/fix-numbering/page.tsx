"use client";

import { useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Change = { id: string; oldName: string; newName: string };

export default function FixChapterNumberingPage() {
  const [scanning, setScanning] = useState(false);
  const [applying, setApplying] = useState(false);
  const [changes, setChanges] = useState<Change[] | null>(null);
  const [appliedCount, setAppliedCount] = useState<number | null>(null);
  const [error, setError] = useState("");

  async function scan() {
    setScanning(true);
    setError("");
    setAppliedCount(null);
    try {
      const res = await fetch("/api/cmds/tools/fix-chapter-numbering");
      const d = await res.json();
      if (!res.ok) {
        setError(d.error || "Could not scan folders.");
        return;
      }
      setChanges(d.changes || []);
    } finally {
      setScanning(false);
    }
  }

  async function apply() {
    if (!changes || changes.length === 0) return;
    if (
      !window.confirm(
        `This will rename ${changes.length} folder(s) across every subject in your institute. This can't be undone from this page. Continue?`
      )
    )
      return;
    setApplying(true);
    setError("");
    try {
      const res = await fetch("/api/cmds/tools/fix-chapter-numbering", { method: "POST" });
      const d = await res.json();
      if (!res.ok) {
        setError(d.error || "Could not apply the fix.");
        return;
      }
      setAppliedCount(d.applied ?? 0);
      setChanges([]);
    } finally {
      setApplying(false);
    }
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-3xl p-6">
        <h1 className="font-serif text-2xl font-semibold text-slate-800">Fix Chapter Numbering</h1>
        <p className="mt-2 text-sm text-slate-500">
          Finds every folder (subject, chapter, or session) across your institute whose name has an
          inconsistent zero-padded number — e.g. <span className="font-mono">02.Economic Botany</span> — and
          normalizes it to a plain number — <span className="font-mono">2.Economic Botany</span>. Folders
          that are already correctly numbered (<span className="font-mono">1.</span>,{" "}
          <span className="font-mono">6.</span>, <span className="font-mono">10.</span>, etc.) are left
          untouched.
        </p>

        <div className="mt-5 flex gap-3">
          <button
            onClick={scan}
            disabled={scanning}
            className="rounded bg-slate-800 px-4 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-60"
          >
            {scanning ? "Scanning…" : "1. Scan for mismatches"}
          </button>
          {changes && changes.length > 0 && (
            <button
              onClick={apply}
              disabled={applying}
              className="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
            >
              {applying ? "Applying…" : `2. Apply fix to all ${changes.length}`}
            </button>
          )}
        </div>

        {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

        {appliedCount !== null && (
          <p className="mt-4 rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            Done — renamed {appliedCount} folder(s). Refresh the Digital Library page to see the change.
          </p>
        )}

        {changes && changes.length === 0 && appliedCount === null && !scanning && (
          <p className="mt-4 text-sm text-slate-500">No mismatches found — every folder is already numbered consistently.</p>
        )}

        {changes && changes.length > 0 && (
          <div className="mt-5 overflow-hidden rounded-lg border border-slate-200">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-2">Current name</th>
                  <th className="px-4 py-2">Will become</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {changes.map((c) => (
                  <tr key={c.id}>
                    <td className="px-4 py-2 font-mono text-slate-600">{c.oldName}</td>
                    <td className="px-4 py-2 font-mono text-emerald-700">{c.newName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}
