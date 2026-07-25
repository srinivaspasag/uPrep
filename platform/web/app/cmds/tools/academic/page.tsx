"use client";

import { useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Entity = {
  id: string;
  name: string;
  code: string;
  departmentId?: string | null;
  programId?: string | null;
  centerId?: string | null;
  centerIds?: string[];
  courseIds?: string[];
};

type Data = {
  departments: Entity[];
  programs: Entity[];
  centers: Entity[];
  sections: Entity[];
};

type Course = { id: string; name: string; granted?: boolean };

export default function AcademicStructurePage() {
  const [data, setData] = useState<Data>({ departments: [], programs: [], centers: [], sections: [] });
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<"structure" | "courses">("structure");

  const [dept, setDept] = useState<string | null>(null);
  const [program, setProgram] = useState<string | null>(null);
  const [center, setCenter] = useState<string | null>(null);
  const [centerPicker, setCenterPicker] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [struct, crs] = await Promise.all([
        fetch("/api/cmds/tools/academic").then((r) => r.json()),
        fetch("/api/cmds/enroll?courses=1").then((r) => (r.ok ? r.json() : {})).catch(() => ({})),
      ]);
      setData({
        departments: struct.departments || [],
        programs: struct.programs || [],
        centers: struct.centers || [],
        sections: struct.sections || [],
      });
      setCourses(crs.courses || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);

  async function add(kind: string, name: string, extra: Record<string, unknown> = {}) {
    await fetch("/api/cmds/tools/academic", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ kind, name, ...extra }),
    });
    load();
  }
  async function rename(kind: string, id: string, name: string) {
    await fetch("/api/cmds/tools/academic", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ kind, id, name }),
    });
    load();
  }
  async function remove(kind: string, id: string) {
    if (!confirm("Remove this item?")) return;
    await fetch(`/api/cmds/tools/academic?kind=${kind}&id=${id}`, { method: "DELETE" });
    load();
  }
  async function assignCenter(programId: string, centerId: string) {
    await fetch("/api/cmds/tools/academic", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ kind: "assign-center", programId, centerId }),
    });
    load();
  }
  async function unassignCenter(programId: string, centerId: string) {
    await fetch(
      `/api/cmds/tools/academic?kind=assign-center&programId=${programId}&centerId=${centerId}`,
      { method: "DELETE" }
    );
    if (center === centerId) setCenter(null);
    load();
  }
  async function assignCourses(programId: string, courseIds: string[]) {
    await fetch("/api/cmds/tools/academic", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ kind: "assign-courses", programId, courseIds }),
    });
    load();
  }

  const programsForDept = dept ? data.programs.filter((p) => p.departmentId === dept) : data.programs;
  const selectedProgram = useMemo(
    () => data.programs.find((p) => p.id === program) || null,
    [data.programs, program]
  );
  // Centers where the selected program runs (resolved from program.centerIds).
  const centersForProgram = useMemo(() => {
    if (!selectedProgram) return [] as Entity[];
    const ids = new Set(selectedProgram.centerIds || []);
    return data.centers.filter((c) => ids.has(c.id));
  }, [selectedProgram, data.centers]);
  const unassignedCenters = useMemo(() => {
    const ids = new Set(selectedProgram?.centerIds || []);
    return data.centers.filter((c) => !ids.has(c.id));
  }, [selectedProgram, data.centers]);
  // Sections for the selected program AND center.
  const sectionsForProgramCenter = useMemo(
    () =>
      data.sections.filter(
        (s) => (!program || s.programId === program) && (!center || s.centerId === center)
      ),
    [data.sections, program, center]
  );

  return (
    <CmdsShell>
      <div className="flex">
        {/* Left rail: Classroom Centers (master list) */}
        <CentersRail
          centers={data.centers}
          onAdd={(name) => add("center", name)}
          onRename={(id, name) => rename("center", id, name)}
          onRemove={(id) => remove("center", id)}
        />

        <main className="flex-1 px-8 py-6">
          <h1 className="text-2xl font-light text-slate-700">Edit Academic Structure</h1>
          <div className="mt-1 flex gap-6 border-b border-slate-200 pb-0 text-sm">
            <button
              onClick={() => setTab("structure")}
              className={`-mb-px border-b-2 pb-2 ${
                tab === "structure"
                  ? "border-emerald-500 font-medium text-slate-800"
                  : "border-transparent text-slate-400 hover:text-slate-600"
              }`}
            >
              Edit Academic Structure
            </button>
            <button
              onClick={() => setTab("courses")}
              className={`-mb-px border-b-2 pb-2 ${
                tab === "courses"
                  ? "border-emerald-500 font-medium text-slate-800"
                  : "border-transparent text-slate-400 hover:text-slate-600"
              }`}
            >
              Assign Courses
            </button>
          </div>

          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading structure…</div>
          ) : tab === "structure" ? (
            <div className="mt-6 grid grid-cols-4 gap-3">
              <Column
                title="Departments"
                searchLabel="Departments"
                kind="department"
                items={data.departments}
                selected={dept}
                onSelect={(id) => {
                  setDept(id === dept ? null : id);
                  setProgram(null);
                  setCenter(null);
                }}
                footerLabel="Add New Department"
                onAdd={(name) => add("department", name)}
                onRename={rename}
                onRemove={remove}
              />
              <Column
                title="Runs Programs"
                searchLabel="Runs Programs"
                kind="program"
                items={programsForDept}
                selected={program}
                onSelect={(id) => {
                  setProgram(id === program ? null : id);
                  setCenter(null);
                }}
                footerLabel="Add New Program"
                onAdd={(name) => add("program", name, dept ? { departmentId: dept } : {})}
                onRename={rename}
                onRemove={remove}
                disabled={data.departments.length > 0 && !dept}
                disabledHint="Select a department"
              />
              <Column
                title="in Centers"
                searchLabel="in Centers"
                kind="center"
                items={centersForProgram}
                selected={center}
                onSelect={(id) => setCenter(id === center ? null : id)}
                footerLabel="Assign a Center"
                onFooterClick={() => setCenterPicker(true)}
                onRename={rename}
                onRemove={(_k, id) => program && unassignCenter(program, id)}
                removeLabel="unassign"
                disabled={!program}
                disabledHint="Select a program"
                emptyHint="No centers assigned — use “Assign a Center”."
              />
              <Column
                title="Has Sections"
                searchLabel="Has Sections"
                kind="section"
                items={sectionsForProgramCenter}
                selected={null}
                onSelect={() => {}}
                footerLabel="Add New Section"
                onAdd={(name) =>
                  add("section", name, {
                    programId: program || undefined,
                    centerId: center || undefined,
                  })
                }
                onRename={rename}
                onRemove={remove}
                disabled={!center}
                disabledHint="Select a center"
              />
            </div>
          ) : (
            <AssignCoursesTab programs={data.programs} courses={courses} onSave={assignCourses} />
          )}
        </main>
      </div>

      {centerPicker && program && (
        <CenterPickerModal
          centers={unassignedCenters}
          onClose={() => setCenterPicker(false)}
          onPick={async (centerId) => {
            await assignCenter(program, centerId);
            setCenterPicker(false);
          }}
          onCreate={async (name) => {
            await add("center", name);
          }}
        />
      )}
    </CmdsShell>
  );
}

function CentersRail({
  centers,
  onAdd,
  onRename,
  onRemove,
}: {
  centers: Entity[];
  onAdd: (name: string) => void;
  onRename: (id: string, name: string) => void;
  onRemove: (id: string) => void;
}) {
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState("");
  return (
    <aside className="w-[190px] shrink-0 border-r border-slate-100 px-4 py-6">
      <div className="rounded border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-700">
        Classroom Centers
      </div>
      <div className="mt-3 space-y-1">
        {centers.length === 0 ? (
          <div className="px-1 py-2 text-xs text-slate-400">No centers yet</div>
        ) : (
          centers.map((c) => (
            <div
              key={c.id}
              className="group flex items-center justify-between rounded px-2 py-1 text-sm text-slate-600 hover:bg-slate-50"
            >
              <span className="truncate">{c.name}</span>
              <button
                onClick={() => onRemove(c.id)}
                className="text-xs text-red-400 opacity-0 hover:underline group-hover:opacity-100"
                title="Remove center"
              >
                ✕
              </button>
            </div>
          ))
        )}
      </div>
      {adding ? (
        <div className="mt-2">
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && name.trim()) {
                onAdd(name.trim());
                setName("");
                setAdding(false);
              }
              if (e.key === "Escape") setAdding(false);
            }}
            placeholder="Center name"
            className="w-full rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
          />
        </div>
      ) : (
        <button
          onClick={() => setAdding(true)}
          className="mt-4 w-full rounded border border-dashed border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-500 hover:border-emerald-400 hover:text-emerald-600"
        >
          + Add New Center
        </button>
      )}
    </aside>
  );
}

function Column({
  title,
  searchLabel,
  kind,
  items,
  selected,
  onSelect,
  onAdd,
  onFooterClick,
  footerLabel,
  onRename,
  onRemove,
  removeLabel,
  disabled,
  disabledHint,
  emptyHint,
}: {
  title: string;
  searchLabel: string;
  kind: string;
  items: Entity[];
  selected: string | null;
  onSelect: (id: string) => void;
  onAdd?: (name: string) => void;
  onFooterClick?: () => void;
  footerLabel: string;
  onRename: (kind: string, id: string, name: string) => void;
  onRemove: (kind: string, id: string) => void;
  removeLabel?: string;
  disabled?: boolean;
  disabledHint?: string;
  emptyHint?: string;
}) {
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState("");
  const [q, setQ] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");

  function submit() {
    if (!name.trim() || !onAdd) return;
    onAdd(name.trim());
    setName("");
    setAdding(false);
  }

  const visible = q
    ? items.filter((it) => it.name.toLowerCase().includes(q.toLowerCase()))
    : items;

  return (
    <div className="flex flex-col rounded border border-slate-200">
      {/* Search header */}
      <div className="border-b border-slate-100 bg-slate-50 px-2 py-1.5">
        <div className="flex items-center gap-1 rounded border border-slate-200 bg-white px-2 py-1">
          <span className="text-xs text-slate-400">🔍</span>
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder={searchLabel}
            disabled={disabled}
            className="w-full bg-transparent text-xs text-slate-600 outline-none disabled:cursor-not-allowed"
          />
        </div>
      </div>

      {adding && (
        <div className="border-b border-slate-100 p-2">
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && submit()}
            placeholder={`New ${title}`}
            className="w-full rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
          />
        </div>
      )}

      <div className="min-h-[220px] flex-1 overflow-y-auto">
        {disabled ? (
          <div className="px-3 py-8 text-center text-xs text-slate-400">{disabledHint}</div>
        ) : visible.length === 0 ? (
          <div className="px-3 py-8 text-center text-xs text-slate-400">{emptyHint || "None"}</div>
        ) : (
          visible.map((it) =>
            editingId === it.id ? (
              <div key={it.id} className="flex items-center gap-1 border-b border-slate-50 p-2">
                <input
                  autoFocus
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && editName.trim()) {
                      onRename(kind, it.id, editName.trim());
                      setEditingId(null);
                    }
                    if (e.key === "Escape") setEditingId(null);
                  }}
                  className="w-full rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
                />
                <button
                  onClick={() => {
                    if (editName.trim()) onRename(kind, it.id, editName.trim());
                    setEditingId(null);
                  }}
                  className="text-xs text-emerald-600"
                >
                  Save
                </button>
              </div>
            ) : (
              <div
                key={it.id}
                className={`group flex w-full items-center justify-between px-3 py-2 text-sm hover:bg-slate-50 ${
                  selected === it.id ? "bg-emerald-50 font-medium text-emerald-700" : "text-slate-700"
                }`}
              >
                <button onClick={() => onSelect(it.id)} className="flex-1 truncate text-left">
                  {it.name}
                </button>
                <div className="flex items-center gap-1.5 opacity-0 group-hover:opacity-100">
                  <button
                    onClick={() => {
                      setEditingId(it.id);
                      setEditName(it.name);
                    }}
                    className="text-xs text-blue-600 hover:underline"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => onRemove(kind, it.id)}
                    className="text-xs text-red-500 hover:underline"
                  >
                    {removeLabel || "✕"}
                  </button>
                </div>
              </div>
            )
          )
        )}
      </div>

      {/* Footer action */}
      <button
        onClick={() => {
          if (disabled) return;
          if (onFooterClick) onFooterClick();
          else setAdding((o) => !o);
        }}
        disabled={disabled}
        className="border-t border-slate-100 bg-emerald-50 py-2 text-center text-xs font-medium text-emerald-700 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-300"
      >
        + {footerLabel}
      </button>
    </div>
  );
}

function CenterPickerModal({
  centers,
  onClose,
  onPick,
  onCreate,
}: {
  centers: Entity[];
  onClose: () => void;
  onPick: (centerId: string) => void;
  onCreate: (name: string) => void;
}) {
  const [name, setName] = useState("");
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[420px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Assign a Center</h3>
        <p className="mt-1 text-sm text-slate-500">Pick a classroom center this program runs in.</p>
        <div className="mt-4 max-h-56 overflow-y-auto rounded border border-slate-200">
          {centers.length === 0 ? (
            <div className="px-3 py-6 text-center text-sm text-slate-400">
              All centers are already assigned. Create a new one below.
            </div>
          ) : (
            centers.map((c) => (
              <button
                key={c.id}
                onClick={() => onPick(c.id)}
                className="block w-full border-b border-slate-50 px-4 py-2.5 text-left text-sm text-slate-700 hover:bg-emerald-50 hover:text-emerald-700"
              >
                {c.name}
              </button>
            ))
          )}
        </div>
        <div className="mt-4 flex items-center gap-2">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && name.trim()) {
                onCreate(name.trim());
                setName("");
              }
            }}
            placeholder="Or create a new center…"
            className="flex-1 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
          <button
            onClick={() => {
              if (name.trim()) {
                onCreate(name.trim());
                setName("");
              }
            }}
            className="rounded bg-slate-100 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-200"
          >
            Create
          </button>
        </div>
        <div className="mt-5 flex justify-end">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Done
          </button>
        </div>
      </div>
    </div>
  );
}

function AssignCoursesTab({
  programs,
  courses,
  onSave,
}: {
  programs: Entity[];
  courses: Course[];
  onSave: (programId: string, courseIds: string[]) => void;
}) {
  const [programId, setProgramId] = useState<string>("");
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const [saving, setSaving] = useState(false);
  const [savedNote, setSavedNote] = useState("");

  useEffect(() => {
    const p = programs.find((x) => x.id === programId);
    setPicked(new Set(p?.courseIds || []));
    setSavedNote("");
  }, [programId, programs]);

  function toggle(id: string) {
    setPicked((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function save() {
    if (!programId) return;
    setSaving(true);
    await onSave(programId, Array.from(picked));
    setSaving(false);
    setSavedNote("Saved.");
  }

  return (
    <div className="mt-6 max-w-2xl">
      <label className="block">
        <span className="text-xs font-medium text-slate-500">Program</span>
        <select
          value={programId}
          onChange={(e) => setProgramId(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
        >
          <option value="">Select a program…</option>
          {programs.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>
      </label>

      {!programId ? (
        <div className="mt-6 rounded border border-dashed border-slate-200 py-12 text-center text-sm text-slate-400">
          Pick a program to choose the courses it includes.
        </div>
      ) : (
        <>
          <div className="mt-5">
            <div className="text-xs font-semibold uppercase tracking-wide text-slate-400">
              Courses in this program
            </div>
            {courses.length === 0 ? (
              <div className="mt-2 text-sm text-slate-400">
                No courses in your catalog yet — create courses in Resources first.
              </div>
            ) : (
              <div className="mt-2 grid gap-2 sm:grid-cols-2">
                {courses.map((c) => (
                  <label
                    key={c.id}
                    className={`flex cursor-pointer items-center gap-2 rounded border px-3 py-2 text-sm ${
                      picked.has(c.id)
                        ? "border-emerald-300 bg-emerald-50 text-emerald-800"
                        : "border-slate-200 bg-white text-slate-600"
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={picked.has(c.id)}
                      onChange={() => toggle(c.id)}
                      className="accent-emerald-600"
                    />
                    <span className="flex-1">{c.name}</span>
                    {c.granted && (
                      <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-medium text-indigo-600">
                        shared
                      </span>
                    )}
                  </label>
                ))}
              </div>
            )}
          </div>

          <div className="mt-5 flex items-center gap-3">
            <button
              onClick={save}
              disabled={saving}
              className="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Saving…" : "Save courses"}
            </button>
            {savedNote && <span className="text-sm text-emerald-600">{savedNote}</span>}
          </div>
        </>
      )}
    </div>
  );
}
