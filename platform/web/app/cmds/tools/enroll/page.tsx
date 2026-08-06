"use client";

import { useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Student = { id: string; memberId: string; firstName: string; lastName: string; email: string };
type Entity = { id: string; name: string; departmentId?: string | null; programId?: string | null; centerId?: string | null; centerIds?: string[] };
type Membership = { programId: string; centerId: string; sectionId: string; assignedAt: number };

// Course assignment is Program/Center/Section membership only — a student
// sees whatever courses their Program grants (see Academic Structure). The
// old "assign individual courses directly" checklist + Course Packs
// quick-assign were a duplicate, ad-hoc path around that same structure and
// have been removed; course-level access changes now happen in Academic
// Structure's own Courses tab instead.
export default function AssignCoursesPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [studentId, setStudentId] = useState("");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);

  const [programs, setPrograms] = useState<Entity[]>([]);
  const [centers, setCenters] = useState<Entity[]>([]);
  const [sections, setSections] = useState<Entity[]>([]);
  const [memberships, setMemberships] = useState<Membership[]>([]);
  const [pickProgram, setPickProgram] = useState("");
  const [pickCenter, setPickCenter] = useState("");
  const [pickSection, setPickSection] = useState("");
  const [assigningProgram, setAssigningProgram] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    // Load each source independently so one failing/stale request can't blank
    // the whole page (e.g. an expired session on one endpoint).
    const safe = (url: string) =>
      fetch(url)
        .then((r) => (r.ok ? r.json() : {}))
        .catch(() => ({}));
    Promise.allSettled([safe("/api/cmds/tools/people?profile=STUDENT"), safe("/api/cmds/tools/academic")])
      .then((results) => {
        const [ppl, acad] = results.map((r) => (r.status === "fulfilled" ? (r.value as any) : {}));
        setStudents(ppl.members || []);
        setPrograms(acad.programs || []);
        setCenters(acad.centers || []);
        setSections(acad.sections || []);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!studentId) {
      setMemberships([]);
      return;
    }
    setMsg("");
    setPickProgram("");
    setPickCenter("");
    setPickSection("");
    fetch(`/api/cmds/enroll/program?memberId=${encodeURIComponent(studentId)}`)
      .then((r) => r.json())
      .then((d) => setMemberships(d.memberships || []));
  }, [studentId]);

  const centersForPickProgram = useMemo(() => {
    const prog = programs.find((p) => p.id === pickProgram);
    const ids = new Set(prog?.centerIds || []);
    return centers.filter((c) => ids.has(c.id));
  }, [programs, centers, pickProgram]);
  const sectionsForPick = useMemo(
    () => sections.filter((s) => s.programId === pickProgram && s.centerId === pickCenter),
    [sections, pickProgram, pickCenter]
  );

  async function assignProgram() {
    if (!studentId || !pickProgram || !pickCenter || !pickSection) return;
    setAssigningProgram(true);
    setMsg("");
    try {
      const res = await fetch("/api/cmds/enroll/program", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          memberId: studentId,
          programId: pickProgram,
          centerId: pickCenter,
          sectionId: pickSection,
        }),
      });
      const d = await res.json().catch(() => ({}));
      if (res.ok) {
        setMemberships(d.memberships || []);
        setPickProgram("");
        setPickCenter("");
        setPickSection("");
        setMsg("Program assigned. The student will see it in their Learning Network.");
      } else {
        setMsg(d.error || "Assign failed");
      }
    } finally {
      setAssigningProgram(false);
    }
  }

  async function removeMembership(programId: string) {
    if (!studentId) return;
    const res = await fetch(
      `/api/cmds/enroll/program?memberId=${encodeURIComponent(studentId)}&programId=${encodeURIComponent(programId)}`,
      { method: "DELETE" }
    );
    const d = await res.json().catch(() => ({}));
    if (res.ok) setMemberships(d.memberships || []);
  }

  const filteredStudents = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return students;
    return students.filter((s) =>
      `${s.firstName} ${s.lastName} ${s.memberId} ${s.email}`.toLowerCase().includes(q)
    );
  }, [students, query]);

  const selectedStudent = students.find((s) => s.id === studentId);

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Assign Courses</h1>
        <p className="mt-1 text-sm text-slate-500">
          Assign a student to a Program, Center and Section — they&apos;ll automatically see that
          program&apos;s courses in their Learning Network.
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

            {/* Program assignment */}
            <div className="rounded border border-slate-200 p-4">
              {!studentId ? (
                <div className="py-6 text-center text-sm text-slate-400">
                  {students.length === 0
                    ? "No students yet — add one in Tools → People Management, then assign a program here."
                    : "Select a student on the left to assign a program."}
                </div>
              ) : (
                <div className="rounded-lg border border-emerald-100 bg-emerald-50/40 p-4">
                  <h2 className="font-semibold text-slate-700">
                    Assign Program — {selectedStudent?.firstName} {selectedStudent?.lastName}
                  </h2>

                  {memberships.length > 0 && (
                    <div className="mt-3 space-y-1.5">
                      {memberships.map((m) => {
                        const prog = programs.find((p) => p.id === m.programId);
                        const ctr = centers.find((c) => c.id === m.centerId);
                        const sec = sections.find((s) => s.id === m.sectionId);
                        return (
                          <div
                            key={m.programId}
                            className="flex items-center justify-between rounded border border-slate-200 bg-white px-3 py-1.5 text-sm"
                          >
                            <span>
                              <span className="font-medium text-slate-700">{prog?.name || "(program)"}</span>
                              <span className="text-slate-400"> @ {ctr?.name || "?"} — {sec?.name || "?"}</span>
                            </span>
                            <button
                              onClick={() => removeMembership(m.programId)}
                              className="text-xs text-red-500 hover:underline"
                            >
                              Remove
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  <div className="mt-3 flex flex-wrap items-end gap-2">
                    <label className="block">
                      <span className="text-xs text-slate-500">Program</span>
                      <select
                        value={pickProgram}
                        onChange={(e) => {
                          setPickProgram(e.target.value);
                          setPickCenter("");
                          setPickSection("");
                        }}
                        className="mt-1 block w-44 rounded border border-slate-300 bg-white px-2 py-1.5 text-sm outline-none focus:border-slate-500"
                      >
                        <option value="">Select…</option>
                        {programs.map((p) => (
                          <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                      </select>
                    </label>
                    <label className="block">
                      <span className="text-xs text-slate-500">Center</span>
                      <select
                        value={pickCenter}
                        onChange={(e) => {
                          setPickCenter(e.target.value);
                          setPickSection("");
                        }}
                        disabled={!pickProgram}
                        className="mt-1 block w-40 rounded border border-slate-300 bg-white px-2 py-1.5 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50 disabled:text-slate-400"
                      >
                        <option value="">Select…</option>
                        {centersForPickProgram.map((c) => (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                      </select>
                    </label>
                    <label className="block">
                      <span className="text-xs text-slate-500">Section</span>
                      <select
                        value={pickSection}
                        onChange={(e) => setPickSection(e.target.value)}
                        disabled={!pickCenter}
                        className="mt-1 block w-40 rounded border border-slate-300 bg-white px-2 py-1.5 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50 disabled:text-slate-400"
                      >
                        <option value="">Select…</option>
                        {sectionsForPick.map((s) => (
                          <option key={s.id} value={s.id}>{s.name}</option>
                        ))}
                      </select>
                    </label>
                    <button
                      onClick={assignProgram}
                      disabled={assigningProgram || !pickProgram || !pickCenter || !pickSection}
                      className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                    >
                      {assigningProgram ? "Assigning…" : "Assign"}
                    </button>
                  </div>
                  {msg && <p className="mt-2 text-sm text-slate-500">{msg}</p>}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}
