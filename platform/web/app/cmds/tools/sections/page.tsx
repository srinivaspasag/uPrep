"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Course = { id: string; name: string; granted?: boolean };
type Section = { id: string; name: string; code: string; courseIds: string[]; memberCount: number };

export default function SectionsPage() {
  const [sections, setSections] = useState<Section[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const res = await fetch("/api/cmds/tools/sections");
      const d = await res.json();
      setSections(d.sections || []);
      setCourses(d.courses || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function remove(s: Section) {
    if (!confirm(`Delete section "${s.name}"? Students already enrolled keep their courses.`)) return;
    await fetch(`/api/cmds/tools/sections?id=${s.id}`, { method: "DELETE" });
    load();
  }

  const courseName = (id: string) => courses.find((c) => c.id === id)?.name || "course";

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-light text-slate-700">Sections & Batches</h1>
            <p className="mt-1 text-sm text-slate-500">
              Create a batch, share its access code, and students self-enroll into its courses.
            </p>
          </div>
          <button
            onClick={() => setAddOpen(true)}
            className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
          >
            + New Section
          </button>
        </div>

        <div className="mt-6 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Section</th>
                <th className="px-4 py-2 font-medium">Access Code</th>
                <th className="px-4 py-2 font-medium">Courses</th>
                <th className="px-4 py-2 font-medium">Students</th>
                <th className="px-4 py-2 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : sections.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    No sections yet.
                  </td>
                </tr>
              ) : (
                sections.map((s) => (
                  <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-700">{s.name}</td>
                    <td className="px-4 py-3">
                      <span className="rounded bg-slate-800 px-2 py-1 font-mono text-xs tracking-widest text-white">
                        {s.code}
                      </span>
                      <button
                        onClick={() => navigator.clipboard?.writeText(s.code)}
                        className="ml-2 text-xs text-blue-600 hover:underline"
                      >
                        Copy
                      </button>
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {s.courseIds.length === 0
                        ? "—"
                        : s.courseIds.map(courseName).join(", ")}
                    </td>
                    <td className="px-4 py-3 text-slate-500">{s.memberCount}</td>
                    <td className="px-4 py-3">
                      <button onClick={() => remove(s)} className="text-xs text-red-500 hover:underline">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {addOpen && (
        <AddSectionModal
          courses={courses}
          onClose={() => setAddOpen(false)}
          onDone={() => {
            setAddOpen(false);
            load();
          }}
        />
      )}
    </CmdsShell>
  );
}

function AddSectionModal({
  courses,
  onClose,
  onDone,
}: {
  courses: Course[];
  onClose: () => void;
  onDone: () => void;
}) {
  const [name, setName] = useState("");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [created, setCreated] = useState<{ name: string; code: string } | null>(null);

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Section name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/sections", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, courseIds: Array.from(selected) }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Create failed");
        return;
      }
      setCreated({ name: d.name, code: d.code });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[480px] rounded-lg bg-white p-6 shadow-xl">
        {created ? (
          <>
            <h3 className="text-lg font-semibold text-slate-800">Section created</h3>
            <p className="mt-2 text-sm text-slate-500">
              Share this access code with students in <span className="font-medium">{created.name}</span>. They
              can redeem it from “My Courses → Join with code”.
            </p>
            <div className="mt-4 rounded border border-slate-200 bg-slate-50 p-4 text-center">
              <div className="font-mono text-2xl tracking-[0.3em] text-slate-800">{created.code}</div>
            </div>
            <div className="mt-5 flex justify-end gap-2">
              <button
                onClick={() => navigator.clipboard?.writeText(created.code)}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
              >
                Copy code
              </button>
              <button
                onClick={onDone}
                className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
              >
                Done
              </button>
            </div>
          </>
        ) : (
          <>
            <h3 className="text-lg font-semibold text-slate-800">New section</h3>
            <label className="mt-4 block text-sm">
              <span className="mb-1 block text-slate-600">Section name*</span>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. JEE 2026 Morning Batch"
                className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
              />
            </label>

            <div className="mt-4">
              <div className="mb-1 text-sm text-slate-600">Courses in this section</div>
              <div className="max-h-52 overflow-auto rounded border border-slate-200">
                {courses.length === 0 ? (
                  <div className="px-3 py-6 text-center text-xs text-slate-400">No courses available.</div>
                ) : (
                  courses.map((c) => (
                    <label
                      key={c.id}
                      className="flex cursor-pointer items-center gap-2 border-b border-slate-100 px-3 py-2 text-sm last:border-0 hover:bg-slate-50"
                    >
                      <input
                        type="checkbox"
                        checked={selected.has(c.id)}
                        onChange={() => toggle(c.id)}
                      />
                      <span className="flex-1">{c.name}</span>
                      {c.granted && (
                        <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-medium text-indigo-600">
                          shared
                        </span>
                      )}
                    </label>
                  ))
                )}
              </div>
            </div>

            {error && <div className="mt-3 text-sm text-red-600">{error}</div>}
            <div className="mt-5 flex justify-end gap-2">
              <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
                Cancel
              </button>
              <button
                onClick={submit}
                disabled={saving}
                className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {saving ? "Creating…" : "Create section"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
