"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Item = { id: string; type: string; status: string; rows: number; url: string | null; at: number };

const TYPES = [
  { k: "members", label: "People / Members" },
  { k: "questions", label: "Question Bank" },
  { k: "tests", label: "Tests" },
];

export default function ExportsPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/exports").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function run(type: string) {
    setBusy(type);
    await fetch("/api/cmds/tools/exports", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type }),
    });
    setBusy("");
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Exports / SD Cards</h1>
        <p className="mt-1 text-sm text-slate-500">
          Generate CSV exports of your data. (In AWS these become offline SD-card burn jobs.)
        </p>

        <div className="mt-5 grid gap-3 sm:grid-cols-3">
          {TYPES.map((t) => (
            <div key={t.k} className="rounded border border-slate-200 p-4">
              <div className="font-medium text-slate-700">{t.label}</div>
              <button
                onClick={() => run(t.k)}
                disabled={busy === t.k}
                className="mt-3 rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {busy === t.k ? "Generating…" : "Generate export"}
              </button>
            </div>
          ))}
        </div>

        <h2 className="mt-8 text-sm font-semibold uppercase tracking-wide text-slate-500">Recent exports</h2>
        <div className="mt-3 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Type</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 font-medium">Rows</th>
                <th className="px-4 py-2 font-medium">Created</th>
                <th className="px-4 py-2 font-medium">Download</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="px-4 py-10 text-center text-slate-400">Loading…</td></tr>
              ) : items.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-10 text-center text-slate-400">No exports yet.</td></tr>
              ) : (
                items.map((e) => (
                  <tr key={e.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-700">{e.type}</td>
                    <td className="px-4 py-3"><span className="text-emerald-600">● {e.status}</span></td>
                    <td className="px-4 py-3 text-slate-500">{e.rows}</td>
                    <td className="px-4 py-3 text-slate-500">{new Date(e.at).toLocaleString()}</td>
                    <td className="px-4 py-3">
                      {e.url ? (
                        <a href={e.url} download className="text-blue-600 hover:underline">Download CSV</a>
                      ) : (
                        "—"
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </CmdsShell>
  );
}
