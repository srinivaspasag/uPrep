"use client";

import { useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Plan = {
  subscriberOrgId: string;
  subscriberOrgName: string;
  providerOrgName: string;
  providerCourseId: string;
  name: string;
  keeper: { id: string; name: string; alreadyLinked: boolean; itemCount: number } | null;
  duplicates: { id: string; name: string; itemCount: number; chapterCount: number }[];
  refs: { programs: number; sections: number; members: number; products: number };
};

// A plan needs no work once its subject is linked, has no duplicates to fold
// in and nothing still points at the provider subject directly.
const isNoop = (p: Plan) =>
  !!p.keeper?.alreadyLinked &&
  p.duplicates.length === 0 &&
  p.refs.programs + p.refs.sections + p.refs.members + p.refs.products === 0;

export default function CourseLinksPage() {
  const [scanning, setScanning] = useState(false);
  const [applying, setApplying] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [plans, setPlans] = useState<Plan[] | null>(null);
  const [result, setResult] = useState<{ created: number; linked: number; archived: number } | null>(null);
  const [error, setError] = useState("");

  const pending = (plans || []).filter((p) => !isNoop(p));

  async function scan() {
    setScanning(true);
    setError("");
    setResult(null);
    setConfirming(false);
    try {
      const res = await fetch("/api/cmds/tools/course-links");
      const d = await res.json();
      if (!res.ok) {
        setError(d.error || "Could not scan shared subjects.");
        return;
      }
      setPlans(d.plans || []);
    } finally {
      setScanning(false);
    }
  }

  async function apply() {
    setApplying(true);
    setError("");
    try {
      const res = await fetch("/api/cmds/tools/course-links", { method: "POST" });
      const d = await res.json();
      if (!res.ok) {
        setError(d.error || "Could not apply.");
        return;
      }
      setResult({ created: d.created ?? 0, linked: d.linked ?? 0, archived: d.archived ?? 0 });
      setPlans(null);
    } finally {
      setApplying(false);
      setConfirming(false);
    }
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-5xl p-6">
        <h1 className="font-serif text-2xl font-semibold text-slate-800">Link Shared Subjects</h1>
        <p className="mt-2 text-sm text-slate-500">
          For every subject shared with another institute, keeps exactly one subject of that name in the
          receiving institute and links it to the master subject, so its chapters and content stay in sync. Any
          duplicate folders with the same name are merged into it (their contents move over and they are archived,
          not deleted), and programs, sections, students and products that pointed at a duplicate or at the master
          copy are switched to the linked subject.
        </p>

        <div className="mt-5 flex flex-wrap gap-3">
          <button
            onClick={scan}
            disabled={scanning}
            className="rounded bg-slate-800 px-4 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-60"
          >
            {scanning ? "Scanning…" : "1. Preview changes"}
          </button>
          {pending.length > 0 && !confirming && (
            <button
              onClick={() => setConfirming(true)}
              className="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
            >
              2. Apply to {pending.length} subject{pending.length === 1 ? "" : "s"}
            </button>
          )}
          {confirming && (
            <span className="flex items-center gap-2 rounded bg-amber-50 px-3 py-1.5 text-sm text-amber-800">
              This changes live data for every institute listed below.
              <button
                onClick={apply}
                disabled={applying}
                className="rounded bg-emerald-600 px-3 py-1 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
              >
                {applying ? "Applying…" : "Yes, apply"}
              </button>
              <button onClick={() => setConfirming(false)} className="text-xs text-amber-700 hover:underline">
                Cancel
              </button>
            </span>
          )}
        </div>

        {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

        {result && (
          <p className="mt-4 rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            Done — {result.linked} existing subject(s) linked, {result.created} created, {result.archived} duplicate
            folder(s) merged and archived.
          </p>
        )}

        {plans && pending.length === 0 && !scanning && (
          <p className="mt-4 text-sm text-slate-500">Nothing to do — every shared subject is already linked once.</p>
        )}

        {pending.length > 0 && (
          <div className="mt-5 overflow-x-auto rounded-lg border border-slate-200">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-2">Institute</th>
                  <th className="px-4 py-2">Subject</th>
                  <th className="px-4 py-2">Keep &amp; link</th>
                  <th className="px-4 py-2">Merge &amp; archive</th>
                  <th className="px-4 py-2">References switched</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {pending.map((p) => (
                  <tr key={`${p.subscriberOrgId}:${p.providerCourseId}`} className="align-top">
                    <td className="px-4 py-2 text-slate-700">
                      {p.subscriberOrgName}
                      <div className="text-xs text-slate-400">from {p.providerOrgName}</div>
                    </td>
                    <td className="px-4 py-2 font-medium text-slate-700">{p.name}</td>
                    <td className="px-4 py-2 text-slate-600">
                      {p.keeper ? (
                        <>
                          existing folder{p.keeper.alreadyLinked ? " (already linked)" : ""}
                          <div className="text-xs text-slate-400">{p.keeper.itemCount} item(s) inside</div>
                        </>
                      ) : (
                        <span className="text-emerald-700">create new</span>
                      )}
                    </td>
                    <td className="px-4 py-2 text-slate-600">
                      {p.duplicates.length === 0
                        ? "—"
                        : p.duplicates.map((d) => (
                            <div key={d.id}>
                              {d.name}{" "}
                              <span className="text-xs text-slate-400">
                                ({d.chapterCount} chapter(s), {d.itemCount} item(s) move over)
                              </span>
                            </div>
                          ))}
                    </td>
                    <td className="px-4 py-2 text-xs text-slate-500">
                      {p.refs.programs} program(s), {p.refs.sections} section(s), {p.refs.members} student(s),{" "}
                      {p.refs.products} product(s)
                    </td>
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
