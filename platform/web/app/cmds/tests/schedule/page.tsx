"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Schedule = {
  id: string;
  testId: string;
  testName: string;
  sectionIds: string[];
  sectionNames: string[];
  startAt: number | null;
  endAt: number | null;
  durationMin: number | null;
  status: "UPCOMING" | "LIVE" | "ENDED";
  startsInMs: number | null;
  endsInMs: number | null;
};
type Test = { id: string; name: string };
type Section = { id: string; name: string };

function fmtCountdown(ms: number | null): string {
  if (ms == null) return "";
  const neg = ms < 0;
  let s = Math.abs(Math.floor(ms / 1000));
  const d = Math.floor(s / 86400);
  s -= d * 86400;
  const h = Math.floor(s / 3600);
  s -= h * 3600;
  const m = Math.floor(s / 60);
  s -= m * 60;
  const parts = [] as string[];
  if (d) parts.push(`${d}d`);
  if (h || d) parts.push(`${h}h`);
  parts.push(`${m}m`);
  if (!d && !h) parts.push(`${s}s`);
  return (neg ? "-" : "") + parts.join(" ");
}

export default function ScheduleTestPage() {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [tests, setTests] = useState<Test[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [loading, setLoading] = useState(true);
  const [, setTick] = useState(0);

  // Form state
  const [testId, setTestId] = useState("");
  const [pickedSections, setPickedSections] = useState<Set<string>>(new Set());
  const [startLocal, setStartLocal] = useState("");
  const [endLocal, setEndLocal] = useState("");
  const [durationMin, setDurationMin] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  async function load() {
    const d = await (await fetch("/api/cmds/tests/schedule")).json();
    setSchedules(d.schedules || []);
    setTests(d.tests || []);
    setSections(d.sections || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  // Re-render every second so countdowns stay live.
  useEffect(() => {
    const t = setInterval(() => setTick((n) => n + 1), 1000);
    return () => clearInterval(t);
  }, []);

  function toggleSection(id: string) {
    setPickedSections((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function create() {
    setError("");
    if (!testId) return setError("Pick a test.");
    if (!startLocal) return setError("Set a start date & time.");
    const startAt = new Date(startLocal).getTime();
    const endAt = endLocal ? new Date(endLocal).getTime() : 0;
    if (endAt && endAt <= startAt) return setError("End time must be after start time.");
    setSaving(true);
    const res = await fetch("/api/cmds/tests/schedule", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        testId,
        sectionIds: Array.from(pickedSections),
        startAt,
        endAt,
        durationMin: durationMin ? Number(durationMin) : 0,
      }),
    });
    setSaving(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Failed to schedule");
      return;
    }
    setTestId("");
    setPickedSections(new Set());
    setStartLocal("");
    setEndLocal("");
    setDurationMin("");
    load();
  }

  async function remove(id: string) {
    if (!window.confirm("Remove this scheduled test?")) return;
    await fetch(`/api/cmds/tests/schedule?id=${encodeURIComponent(id)}`, { method: "DELETE" });
    load();
  }

  const badge = (s: Schedule) => {
    if (s.status === "LIVE")
      return <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-bold uppercase text-red-600">● Live</span>;
    if (s.status === "ENDED")
      return <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-bold uppercase text-slate-500">Ended</span>;
    return <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold uppercase text-amber-700">Upcoming</span>;
  };

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Schedule a Test</h1>
        <p className="mt-1 text-sm text-slate-500">
          Pick a test, target it to sections (or the whole institute), and set the window. Students can
          only take it during the window; you get a live countdown here.
        </p>

        {/* Create form */}
        <div className="mt-6 rounded-lg border border-slate-200 bg-slate-50/60 p-5">
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="text-xs font-medium text-slate-500">Test</span>
              <select
                value={testId}
                onChange={(e) => setTestId(e.target.value)}
                className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
              >
                <option value="">Select a test…</option>
                {tests.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name}
                  </option>
                ))}
              </select>
              {tests.length === 0 && (
                <span className="mt-1 block text-xs text-amber-600">
                  No tests found — create one in Resources → Add a Test first.
                </span>
              )}
            </label>

            <label className="block">
              <span className="text-xs font-medium text-slate-500">Duration (minutes, optional)</span>
              <input
                type="number"
                min={0}
                value={durationMin}
                onChange={(e) => setDurationMin(e.target.value)}
                placeholder="e.g. 60"
                className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
              />
            </label>

            <label className="block">
              <span className="text-xs font-medium text-slate-500">Starts at</span>
              <input
                type="datetime-local"
                value={startLocal}
                onChange={(e) => setStartLocal(e.target.value)}
                className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
              />
            </label>

            <label className="block">
              <span className="text-xs font-medium text-slate-500">Ends at (optional)</span>
              <input
                type="datetime-local"
                value={endLocal}
                onChange={(e) => setEndLocal(e.target.value)}
                className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
              />
            </label>
          </div>

          <div className="mt-4">
            <span className="text-xs font-medium text-slate-500">
              Target sections{" "}
              <span className="font-normal text-slate-400">(leave all unchecked = whole institute)</span>
            </span>
            {sections.length === 0 ? (
              <div className="mt-1 text-xs text-slate-400">
                No sections yet — the test will be available to the whole institute.
              </div>
            ) : (
              <div className="mt-2 flex flex-wrap gap-2">
                {sections.map((s) => (
                  <label
                    key={s.id}
                    className={`flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1 text-sm ${
                      pickedSections.has(s.id)
                        ? "border-indigo-300 bg-indigo-50 text-indigo-700"
                        : "border-slate-200 bg-white text-slate-600"
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={pickedSections.has(s.id)}
                      onChange={() => toggleSection(s.id)}
                      className="accent-indigo-600"
                    />
                    {s.name}
                  </label>
                ))}
              </div>
            )}
          </div>

          {error && <div className="mt-3 text-sm text-red-500">{error}</div>}

          <div className="mt-4">
            <button
              onClick={create}
              disabled={saving}
              className="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Scheduling…" : "Schedule test"}
            </button>
          </div>
        </div>

        {/* Existing schedules */}
        <h2 className="mt-8 text-sm font-semibold text-slate-600">Scheduled tests</h2>
        <div className="mt-3 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Test</th>
                <th className="px-4 py-2 font-medium">Target</th>
                <th className="px-4 py-2 font-medium">Window</th>
                <th className="px-4 py-2 font-medium">Status / Countdown</th>
                <th className="w-10 px-4 py-2" />
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : schedules.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    No tests scheduled yet.
                  </td>
                </tr>
              ) : (
                schedules.map((s) => {
                  const now = Date.now();
                  const startsIn = s.startAt ? s.startAt - now : null;
                  const endsIn = s.endAt ? s.endAt - now : null;
                  return (
                    <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-4 py-3 font-medium text-slate-700">{s.testName}</td>
                      <td className="px-4 py-3 text-slate-500">
                        {s.sectionNames.length ? s.sectionNames.join(", ") : "Whole institute"}
                      </td>
                      <td className="px-4 py-3 text-slate-500">
                        {s.startAt ? new Date(s.startAt).toLocaleString() : "—"}
                        {s.endAt ? ` → ${new Date(s.endAt).toLocaleString()}` : ""}
                        {s.durationMin ? ` · ${s.durationMin} min` : ""}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          {badge(s)}
                          {s.status === "UPCOMING" && startsIn != null && (
                            <span className="text-xs text-slate-500">starts in {fmtCountdown(startsIn)}</span>
                          )}
                          {s.status === "LIVE" && endsIn != null && (
                            <span className="text-xs text-slate-500">ends in {fmtCountdown(endsIn)}</span>
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => remove(s.id)}
                          className="rounded px-2 py-1 text-xs text-red-500 hover:bg-red-50"
                        >
                          Remove
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </CmdsShell>
  );
}
