"use client";

import { useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Student = { id: string; memberId: string; firstName: string; lastName: string; email: string };
type Course = { id: string; name: string; chapterCount: number; granted?: boolean };
type Pack = { id: string; name: string; granted: boolean; courseIds: string[]; courseCount: number };

export default function AssignCoursesPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [packs, setPacks] = useState<Pack[]>([]);
  const [studentId, setStudentId] = useState("");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    // Load each source independently so one failing/stale request can't blank
    // the whole page (e.g. an expired session on one endpoint).
    const safe = (url: string) =>
      fetch(url)
        .then((r) => (r.ok ? r.json() : {}))
        .catch(() => ({}));
    Promise.allSettled([
      safe("/api/cmds/tools/people?profile=STUDENT"),
      safe("/api/cmds/enroll?courses=1"),
      safe("/api/cmds/enroll/pack"),
    ])
      .then((results) => {
        const [ppl, crs, pks] = results.map((r) =>
          r.status === "fulfilled" ? (r.value as any) : {}
        );
        setStudents(ppl.members || []);
        setCourses(crs.courses || []);
        setPacks(pks.packs || []);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!studentId) {
      setSelected(new Set());
      return;
    }
    setMsg("");
    fetch(`/api/cmds/enroll?memberId=${encodeURIComponent(studentId)}`)
      .then((r) => r.json())
      .then((d) => setSelected(new Set<string>(d.enrolledCourseIds || [])));
  }, [studentId]);

  const filteredStudents = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return students;
    return students.filter((s) =>
      `${s.firstName} ${s.lastName} ${s.memberId} ${s.email}`.toLowerCase().includes(q)
    );
  }, [students, query]);

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function save() {
    if (!studentId) return;
    setSaving(true);
    setMsg("");
    try {
      const res = await fetch("/api/cmds/enroll", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ memberId: studentId, courseIds: Array.from(selected) }),
      });
      const d = await res.json().catch(() => ({}));
      setMsg(res.ok ? "Saved. The student will see these courses in their learn app." : d.error || "Save failed");
    } finally {
      setSaving(false);
    }
  }

  // Adds a whole pack's courses to the current selection (student saved immediately).
  async function assignPack(packId: string) {
    if (!studentId || !packId) return;
    setSaving(true);
    setMsg("");
    try {
      const res = await fetch("/api/cmds/enroll/pack", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ memberId: studentId, packId }),
      });
      const d = await res.json().catch(() => ({}));
      if (res.ok) {
        setSelected(new Set<string>(d.enrolledCourseIds || []));
        setMsg(`Pack applied — added ${d.added} course(s).`);
      } else {
        setMsg(d.error || "Assign failed");
      }
    } finally {
      setSaving(false);
    }
  }

  const selectedStudent = students.find((s) => s.id === studentId);

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Assign Courses</h1>
        <p className="mt-1 text-sm text-slate-500">
          Give a student access to specific courses. They&apos;ll only see the courses assigned here
          in their Digital Library and “My Courses”.
        </p>

        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : (
          <div className="mt-6 grid grid-cols-[320px_1fr] gap-6">
            {/* Student picker */}
            <div className="rounded border border-slate-200">
              <div className="border-b border-slate-100 p-3">
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Search students…"
                  className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
                />
              </div>
              <div className="max-h-[440px] overflow-auto">
                {filteredStudents.length === 0 ? (
                  <div className="p-4 text-sm text-slate-400">No students found.</div>
                ) : (
                  filteredStudents.map((s) => (
                    <button
                      key={s.id}
                      onClick={() => setStudentId(s.id)}
                      className={`flex w-full items-center gap-2 border-b border-slate-50 px-3 py-2 text-left text-sm ${
                        studentId === s.id ? "bg-emerald-50 text-emerald-800" : "hover:bg-slate-50"
                      }`}
                    >
                      <span className="flex h-7 w-7 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-600">
                        {(s.firstName || "?").charAt(0).toUpperCase()}
                      </span>
                      <span>
                        <span className="block">{s.firstName} {s.lastName}</span>
                        <span className="block text-xs text-slate-400">{s.memberId}</span>
                      </span>
                    </button>
                  ))
                )}
              </div>
            </div>

            {/* Course checklist */}
            <div className="rounded border border-slate-200 p-4">
              {!studentId ? (
                <div className="py-6">
                  <div className="text-center text-sm text-slate-400">
                    {students.length === 0
                      ? "No students yet — add one in Tools → People Management, then assign courses here."
                      : "Select a student on the left to assign courses."}
                  </div>
                  {(courses.length > 0 || packs.length > 0) && (
                    <div className="mt-6 rounded-lg border border-slate-100 bg-slate-50/60 p-4">
                      <div className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                        Available to this institute
                      </div>
                      {packs.length > 0 && (
                        <div className="mt-2 flex flex-wrap gap-2">
                          {packs.map((p) => (
                            <span
                              key={p.id}
                              className="rounded-full border border-indigo-200 bg-white px-3 py-1 text-xs font-medium text-indigo-700"
                            >
                              {p.name} ({p.courseCount})
                            </span>
                          ))}
                        </div>
                      )}
                      <div className="mt-3 grid grid-cols-2 gap-2">
                        {courses.map((c) => (
                          <div
                            key={c.id}
                            className="flex items-center gap-2 rounded border border-slate-100 bg-white px-3 py-2 text-sm text-slate-600"
                          >
                            <span className="flex-1">{c.name}</span>
                            {c.granted && (
                              <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-medium text-indigo-600">
                                shared
                              </span>
                            )}
                          </div>
                        ))}
                      </div>
                      <p className="mt-3 text-xs text-slate-400">
                        Pick a student to actually assign any of these.
                      </p>
                    </div>
                  )}
                </div>
              ) : (
                <>
                  <div className="flex items-center justify-between">
                    <h2 className="font-semibold text-slate-700">
                      Courses for {selectedStudent?.firstName} {selectedStudent?.lastName}
                    </h2>
                    <span className="text-xs text-slate-400">{selected.size} selected</span>
                  </div>

                  {packs.length > 0 && (
                    <div className="mt-3 flex flex-wrap items-center gap-2 rounded border border-indigo-100 bg-indigo-50/50 p-3">
                      <span className="text-xs font-medium text-indigo-700">Quick-assign a pack:</span>
                      {packs.map((p) => (
                        <button
                          key={p.id}
                          onClick={() => assignPack(p.id)}
                          disabled={saving}
                          title={`Adds ${p.courseCount} course(s)`}
                          className="rounded-full border border-indigo-200 bg-white px-3 py-1 text-xs font-medium text-indigo-700 hover:bg-indigo-100 disabled:opacity-50"
                        >
                          + {p.name}
                          <span className="ml-1 text-indigo-400">({p.courseCount})</span>
                        </button>
                      ))}
                    </div>
                  )}
                  <div className="mt-3 grid max-h-[400px] grid-cols-2 gap-2 overflow-auto">
                    {courses.length === 0 ? (
                      <div className="col-span-2 text-sm text-slate-400">
                        No courses yet. Create top-level folders in Resources first.
                      </div>
                    ) : (
                      courses.map((c) => (
                        <label
                          key={c.id}
                          className="flex cursor-pointer items-center gap-2 rounded border border-slate-100 px-3 py-2 text-sm hover:bg-slate-50"
                        >
                          <input
                            type="checkbox"
                            checked={selected.has(c.id)}
                            onChange={() => toggle(c.id)}
                            className="accent-emerald-600"
                          />
                          <span className="flex-1">{c.name}</span>
                          {c.granted && (
                            <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-medium text-indigo-600">
                              shared
                            </span>
                          )}
                          <span className="text-xs text-slate-400">{c.chapterCount} ch</span>
                        </label>
                      ))
                    )}
                  </div>
                  <div className="mt-4 flex items-center gap-3">
                    <button
                      onClick={save}
                      disabled={saving}
                      className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                    >
                      {saving ? "Saving…" : "Save assignment"}
                    </button>
                    {msg && <span className="text-sm text-slate-500">{msg}</span>}
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}
