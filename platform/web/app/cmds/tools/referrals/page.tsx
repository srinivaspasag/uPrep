"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Item = { id: string; code: string; description: string; reward: string; uses: number };

export default function ReferralsPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [reward, setReward] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/referrals").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function add() {
    await fetch("/api/cmds/tools/referrals", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, description, reward }),
    });
    setCode("");
    setDescription("");
    setReward("");
    load();
  }

  async function remove(id: string) {
    await fetch(`/api/cmds/tools/referrals?id=${id}`, { method: "DELETE" });
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[900px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Referrals</h1>
        <p className="mt-1 text-sm text-slate-500">Create referral codes and reward students for inviting friends.</p>

        <div className="mt-5 rounded border border-slate-200 p-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="Code (auto if blank)" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input value={reward} onChange={(e) => setReward(e.target.value)} placeholder="Reward (e.g. ₹500 credit)" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
          </div>
          <button onClick={add} className="mt-3 rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700">
            Create code
          </button>
        </div>

        <div className="mt-6 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Code</th>
                <th className="px-4 py-2 font-medium">Reward</th>
                <th className="px-4 py-2 font-medium">Description</th>
                <th className="px-4 py-2 font-medium">Uses</th>
                <th className="px-4 py-2 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="px-4 py-10 text-center text-slate-400">Loading…</td></tr>
              ) : items.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-10 text-center text-slate-400">No referral codes yet.</td></tr>
              ) : (
                items.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-medium text-slate-700">{r.code}</td>
                    <td className="px-4 py-3 text-slate-500">{r.reward || "—"}</td>
                    <td className="px-4 py-3 text-slate-500">{r.description || "—"}</td>
                    <td className="px-4 py-3 text-slate-500">{r.uses}</td>
                    <td className="px-4 py-3">
                      <button onClick={() => remove(r.id)} className="text-xs text-red-500 hover:underline">Delete</button>
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
