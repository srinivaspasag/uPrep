"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Item = {
  id: string;
  title: string;
  startAt: number | null;
  durationMin: number;
  teacher: string;
  center: string;
  joinUrl: string;
};

export default function SchedulePage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [when, setWhen] = useState("");
  const [durationMin, setDurationMin] = useState(60);
  const [teacher, setTeacher] = useState("");
  const [center, setCenter] = useState("");
  const [joinUrl, setJoinUrl] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/schedule").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function add() {
    setError("");
    if (!title.trim()) return setError("Class title is required.");
    const res = await fetch("/api/cmds/tools/schedule", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title,
        startAt: when ? new Date(when).getTime() : null,
        durationMin,
        teacher,
        center,
        joinUrl,
      }),
    });
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Failed to schedule.");
      return;
    }
    setTitle("");
    setWhen("");
    setTeacher("");
    setCenter("");
    setJoinUrl("");
    load();
  }

  async function remove(id: string) {
    await fetch(`/api/cmds/tools/schedule?id=${id}`, { method: "DELETE" });
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Schedule / Classroom Connect</h1>

        <div className="mt-5 rounded border border-slate-200 p-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Class title" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input type="datetime-local" value={when} onChange={(e) => setWhen(e.target.value)} className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input type="number" min={15} value={durationMin} onChange={(e) => setDurationMin(Number(e.target.value))} placeholder="Duration (min)" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input value={teacher} onChange={(e) => setTeacher(e.target.value)} placeholder="Teacher" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input value={center} onChange={(e) => setCenter(e.target.value)} placeholder="Center / batch" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
            <input value={joinUrl} onChange={(e) => setJoinUrl(e.target.value)} placeholder="Join link (Zoom/Meet)" className="rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
          </div>
          {error && <div className="mt-2 text-sm text-red-600">{error}</div>}
          <button onClick={add} className="mt-3 rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700">
            Schedule class
          </button>
        </div>

        <div className="mt-6 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Class</th>
                <th className="px-4 py-2 font-medium">When</th>
                <th className="px-4 py-2 font-medium">Teacher</th>
                <th className="px-4 py-2 font-medium">Center</th>
                <th className="px-4 py-2 font-medium">Join</th>
                <th className="px-4 py-2 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="px-4 py-10 text-center text-slate-400">Loading…</td></tr>
              ) : items.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-10 text-center text-slate-400">No classes scheduled.</td></tr>
              ) : (
                items.map((s) => (
                  <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-700">{s.title}</td>
                    <td className="px-4 py-3 text-slate-500">
                      {s.startAt ? new Date(s.startAt).toLocaleString() : "—"} · {s.durationMin}m
                    </td>
                    <td className="px-4 py-3 text-slate-500">{s.teacher || "—"}</td>
                    <td className="px-4 py-3 text-slate-500">{s.center || "—"}</td>
                    <td className="px-4 py-3">
                      {s.joinUrl ? (
                        <a href={s.joinUrl} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline">
                          Join
                        </a>
                      ) : (
                        "—"
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <button onClick={() => remove(s.id)} className="text-xs text-red-500 hover:underline">
                        Cancel
                      </button>
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
