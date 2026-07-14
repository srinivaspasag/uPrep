"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";

type Program = { id: string; name: string; code: string | null };

export default function ProgramsPage() {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/programs")
      .then((r) => r.json())
      .then((d) => setPrograms(d.programs || []))
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="programs">
      <h1 className="text-xl font-semibold tracking-wide text-slate-700">MY PROGRAMS</h1>
      <div className="mt-5">
        {loading ? (
          <div className="text-slate-400">Loading…</div>
        ) : programs.length === 0 ? (
          <div className="text-slate-400">You are not enrolled in any program yet.</div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {programs.map((p) => (
              <div
                key={p.id}
                className="flex h-[150px] w-[240px] items-start rounded-md border border-slate-200 bg-[repeating-linear-gradient(0deg,#fafafa,#fafafa_23px,#f0f0f0_24px)] p-4"
              >
                <span className="text-[15px] font-bold uppercase leading-snug text-[#f0a020]">
                  {p.name}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="mt-12 border-t border-slate-100 pt-6">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold tracking-wide text-slate-700">
            AVAILABLE PROGRAMS
          </h2>
          <span className="text-sm text-slate-400">CATEGORY</span>
        </div>
        <div className="mt-6 flex flex-col items-center justify-center rounded-md border border-dashed border-slate-200 py-16 text-center">
          <div className="text-5xl">🙁</div>
          <div className="mt-3 text-lg font-medium text-[#f0a020]">SORRY!!</div>
          <div className="text-slate-400">No programs added yet!</div>
        </div>
      </div>
    </LmsShell>
  );
}
