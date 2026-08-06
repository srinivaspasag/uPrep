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

type Course = { id: string; name: string; granted?: boolean; boardMatched?: boolean };

// Board Tree subjects (Physics XI, Chemistry XI, ...) and the content-folder
// catalog ("courses" as far as Assign Courses is concerned) are two
// separately-maintained lists that happen to mostly share names. Normalizing
// this way — strip a leading "1." ordinal, fold "Mathematics" to "Maths" —
// matched 85-100% of real chapters when checked against production data;
// the one true mismatch found was Maths XI/XII using the short form while
// the Board Tree used the long form, which this specifically accounts for.
function normSubjectName(s: string): string {
  return (s || "")
    .toLowerCase()
    .replace(/^[0-9]+[.)]?\s*/, "")
    .replace(/mathematics/g, "maths")
    .replace(/[^a-z0-9]/g, "");
}

export default function AcademicStructurePage() {
  const [data, setData] = useState<Data>({ departments: [], programs: [], centers: [], sections: [] });
  const [courses, setCourses] = useState<Course[]>([]);
  // Resolved names for assigned courseIds at any depth (subject or a
  // specific chapter) — the top-level `courses` catalog only ever lists
  // subject roots, so a chapter-level assignment needs this to display at
  // all. See app/api/cmds/tools/academic/route.ts GET.
  const [courseNames, setCourseNames] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<"structure" | "courses">("structure");

  const [dept, setDept] = useState<string | null>(null);
  const [program, setProgram] = useState<string | null>(null);
  const [center, setCenter] = useState<string | null>(null);
  const [section, setSection] = useState<string | null>(null);
  const [centerPicker, setCenterPicker] = useState(false);
  const [error, setError] = useState("");

  async function load() {
    setLoading(true);
    try {
      const [struct, crs, boards] = await Promise.all([
        fetch("/api/cmds/tools/academic").then((r) => r.json()),
        fetch("/api/cmds/enroll?courses=1").then((r) => (r.ok ? r.json() : {})).catch(() => ({})),
        fetch("/api/cmds/tools/boards").then((r) => (r.ok ? r.json() : {})).catch(() => ({})),
      ]);
      setData({
        departments: struct.departments || [],
        programs: struct.programs || [],
        centers: struct.centers || [],
        sections: struct.sections || [],
      });
      setCourseNames(struct.courseNames || {});
      // "Assign Courses" grants access via content-folder ids under the
      // hood (unchanged — that's the mechanism My Courses/Digital
      // Library/Certificates already rely on), but the picker itself now
      // shows and matches against real Board Tree subject names instead of
      // raw folder names, so it reads as "assign this Board Tree subject"
      // rather than "assign this uploaded content folder".
      const subjectNameByNorm = new Map<string, string>(
        (boards.nodes || []).map((s: { name: string }) => [normSubjectName(s.name), s.name])
      );
      const merged: Course[] = (crs.courses || []).map((c: any) => {
        const canon = subjectNameByNorm.get(normSubjectName(c.name));
        return { id: c.id, name: canon || c.name, granted: c.granted, boardMatched: !!canon };
      });
      setCourses(merged);
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
    setError("");
    const res = await fetch(`/api/cmds/tools/academic?kind=${kind}&id=${id}`, { method: "DELETE" });
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Failed to remove item");
      return;
    }
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
    if (center === centerId) {
      setCenter(null);
      setSection(null);
    }
    load();
  }
  async function assignSectionCourses(sectionId: string, courseIds: string[], notify: boolean) {
    const res = await fetch("/api/cmds/tools/academic", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ kind: "assign-section-courses", sectionId, courseIds, notify }),
    });
    const d = await res.json().catch(() => ({}));
    await load();
    return d as { ok?: boolean; error?: string; notified?: number; delivered?: number };
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
  const selectedSection = useMemo(
    () => data.sections.find((s) => s.id === section) || null,
    [data.sections, section]
  );
  // Courses already assigned to the selected section — shown as the 5th
  // column, read-only (assigning/removing happens in the Assign Courses tab
  // now; this used to have its own separate add/remove flow too, which was
  // genuinely duplicated functionality doing the same mutation two ways).
  const assignedCourses = useMemo(() => {
    const byId = new Map(courses.map((c) => [c.id, c.name]));
    return (selectedSection?.courseIds || []).map((id) => ({
      id,
      name: byId.get(id) || courseNames[id] || "(unknown course)",
      code: "",
    }));
  }, [selectedSection, courses, courseNames]);

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

          {error && (
            <div className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {error}
            </div>
          )}

          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading structure…</div>
          ) : tab === "structure" ? (
            <div className="mt-6 grid grid-cols-5 gap-3">
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
                  setSection(null);
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
                  setSection(null);
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
                onSelect={(id) => {
                  setCenter(id === center ? null : id);
                  setSection(null);
                }}
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
                selected={section}
                onSelect={(id) => setSection(id === section ? null : id)}
                footerLabel="Add New Section"
                onAdd={(name) =>
                  add("section", name, {
                    programId: program || undefined,
                    centerId: center || undefined,
                  })
                }
                onRename={rename}
                onRemove={(k, id) => {
                  if (section === id) setSection(null);
                  remove(k, id);
                }}
                disabled={!center}
                disabledHint="Select a center"
              />
              <Column
                title="Has Courses"
                searchLabel="Has Courses"
                kind="section-course"
                items={assignedCourses}
                selected={null}
                onSelect={() => {}}
                footerLabel="Manage in Assign Courses →"
                onFooterClick={() => setTab("courses")}
                onRename={() => {}}
                onRemove={() => {}}
                disabled={!section}
                disabledHint="Select a section"
                emptyHint="No courses assigned — use “Manage in Assign Courses”."
                showEdit={false}
                showRemove={false}
              />
            </div>
          ) : (
            <AssignCoursesTab
              programs={data.programs}
              centers={data.centers}
              sections={data.sections}
              courses={courses}
              onSave={assignSectionCourses}
              initialProgramId={program}
              initialCenterId={center}
              initialSectionId={section}
            />
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
  showEdit = true,
  showRemove = true,
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
  showEdit?: boolean;
  showRemove?: boolean;
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
                  {kind === "section" && it.code && (
                    <span className="ml-2 font-mono text-[11px] font-normal text-slate-400">{it.code}</span>
                  )}
                </button>
                {/* Bug found live: Edit/Remove were opacity-0 until hover — easy
                    to miss entirely (especially on touch/trackpad), which is
                    what made "delete" look like it was missing rather than
                    just invisible. Always visible now. */}
                <div className="flex items-center gap-1.5">
                  {showEdit && (
                    <button
                      onClick={() => {
                        setEditingId(it.id);
                        setEditName(it.name);
                      }}
                      className="text-xs text-blue-600 hover:underline"
                    >
                      Edit
                    </button>
                  )}
                  {showRemove && (
                    <button
                      onClick={() => onRemove(kind, it.id)}
                      className="text-xs text-red-500 hover:underline"
                    >
                      {removeLabel || "✕"}
                    </button>
                  )}
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

// Picker for the "Has Courses" column's "+ Add Courses" action — only offers
// courses not already assigned to the selected section (already-assigned
// ones are shown directly in the column itself, removed via its "remove").
type FolderChild = { id: string; name: string; hasChildren: boolean };

// Chapters (and deeper) under a course root, fetched lazily per-node on
// first expand rather than eagerly walking the whole tree up front — a
// subject can have 15-20 chapters, each potentially with their own
// sub-folders, and most staff only ever expand one or two subjects per visit.
async function fetchChildren(parentId: string): Promise<FolderChild[]> {
  const d = await fetch(`/api/cmds/content?parentId=${encodeURIComponent(parentId)}`)
    .then((r) => r.json())
    .catch(() => ({ resources: [] }));
  return (d.resources || [])
    .filter((r: any) => r.type === "FOLDER")
    .map((r: any) => ({ id: r.id, name: r.title, hasChildren: true }));
}

// Used by AssignCoursesTab, the sole course-assignment UI (the Edit Academic
// Structure tab's "Has Courses" column used to have its own separate
// add/remove flow doing the same thing — genuinely duplicated functionality
// — so that column is now a read-only view of what's assigned, linking here
// to actually change it). Recursive so a chapter can itself expand into its own
// sub-folders (concepts). Checking a node grants exactly that folder's
// subtree, not everything above or below it.
function CourseTree({
  courses,
  picked,
  onToggle,
}: {
  courses: Course[];
  picked: Set<string>;
  onToggle: (id: string) => void;
}) {
  // Per-node expansion state: undefined = not expanded, "loading", or the
  // fetched children array.
  const [expanded, setExpanded] = useState<Record<string, FolderChild[] | "loading" | undefined>>({});

  async function toggleExpand(id: string) {
    if (expanded[id] !== undefined) {
      setExpanded((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });
      return;
    }
    setExpanded((prev) => ({ ...prev, [id]: "loading" }));
    const children = await fetchChildren(id);
    setExpanded((prev) => ({ ...prev, [id]: children }));
  }

  const boardCourses = courses.filter((c) => c.boardMatched);
  const otherCourses = courses.filter((c) => !c.boardMatched);

  function TreeRow({
    id,
    name,
    granted,
    depth,
  }: {
    id: string;
    name: string;
    granted?: boolean;
    depth: number;
  }) {
    const kids = expanded[id];
    return (
      <div>
        <div
          className="flex items-center gap-2 border-b border-slate-50 py-2.5 pr-4 text-sm text-slate-700 hover:bg-slate-50"
          style={{ paddingLeft: 16 + depth * 20 }}
        >
          <button
            onClick={() => toggleExpand(id)}
            className="w-4 shrink-0 text-slate-400 hover:text-slate-600"
            title="Show chapters"
          >
            {kids === "loading" ? "⋯" : kids ? "▾" : "▸"}
          </button>
          <label className="flex flex-1 cursor-pointer items-center gap-2">
            <input
              type="checkbox"
              checked={picked.has(id)}
              onChange={() => onToggle(id)}
              className="accent-emerald-600"
            />
            <span className="flex-1">{name}</span>
            {granted && (
              <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-medium text-indigo-600">
                shared
              </span>
            )}
          </label>
        </div>
        {Array.isArray(kids) &&
          (kids.length === 0 ? (
            <div className="py-2 text-xs text-slate-400" style={{ paddingLeft: 40 + depth * 20 }}>
              No sub-chapters.
            </div>
          ) : (
            kids.map((k) => <TreeRow key={k.id} id={k.id} name={k.name} depth={depth + 1} />)
          ))}
      </div>
    );
  }

  if (courses.length === 0) return null;
  return (
    <>
      {boardCourses.map((c) => (
        <TreeRow key={c.id} id={c.id} name={c.name} granted={c.granted} depth={0} />
      ))}
      {otherCourses.length > 0 && (
        <>
          <div className="border-b border-t border-slate-100 bg-slate-50 px-4 py-1.5 text-[11px] font-medium uppercase tracking-wide text-slate-400">
            Other content folders (no matching Board Tree subject)
          </div>
          {otherCourses.map((c) => (
            <TreeRow key={c.id} id={c.id} name={c.name} granted={c.granted} depth={0} />
          ))}
        </>
      )}
    </>
  );
}

// Courses are assigned per Section (Program -> Center -> Section -> Course),
// on top of (not replacing) the org-wide program.courseIds list — see the
// "Section-level course assignment" plan. A cascading picker narrows down to
// one section, whose course list this tab reads/writes.
function AssignCoursesTab({
  programs,
  centers,
  sections,
  courses,
  onSave,
  initialProgramId,
  initialCenterId,
  initialSectionId,
}: {
  programs: Entity[];
  centers: Entity[];
  sections: Entity[];
  courses: Course[];
  onSave: (
    sectionId: string,
    courseIds: string[],
    notify: boolean
  ) => Promise<{ ok?: boolean; error?: string; notified?: number; delivered?: number }>;
  initialProgramId?: string | null;
  initialCenterId?: string | null;
  initialSectionId?: string | null;
}) {
  // Seeded once from whatever was selected in the Edit Academic Structure
  // tab (the "Manage in Assign Courses →" link on Has Courses) so switching
  // tabs doesn't dump you back to three empty dropdowns.
  const [programId, setProgramId] = useState(initialProgramId || "");
  const [centerId, setCenterId] = useState(initialCenterId || "");
  const [sectionId, setSectionId] = useState(initialSectionId || "");
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const [notify, setNotify] = useState(false);
  const [saving, setSaving] = useState(false);
  const [savedNote, setSavedNote] = useState("");

  const selectedProgram = useMemo(() => programs.find((p) => p.id === programId) || null, [programs, programId]);
  const centersForProgram = useMemo(() => {
    if (!selectedProgram) return [] as Entity[];
    const ids = new Set(selectedProgram.centerIds || []);
    return centers.filter((c) => ids.has(c.id));
  }, [selectedProgram, centers]);
  const sectionsForProgramCenter = useMemo(
    () => sections.filter((s) => s.programId === programId && s.centerId === centerId),
    [sections, programId, centerId]
  );
  const selectedSection = useMemo(() => sections.find((s) => s.id === sectionId) || null, [sections, sectionId]);

  useEffect(() => {
    setCenterId("");
    setSectionId("");
  }, [programId]);
  useEffect(() => {
    setSectionId("");
  }, [centerId]);
  useEffect(() => {
    setPicked(new Set(selectedSection?.courseIds || []));
    setSavedNote("");
  }, [selectedSection]);

  function toggle(id: string) {
    setPicked((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function save() {
    if (!sectionId) return;
    // Guard against the exact accident that hit production twice already:
    // this save is a full replace, not a merge, so an empty selection wipes
    // every course this section's students currently have — silently, if
    // the picker happened to render before it was pre-seeded, or a click
    // just missed. Confirm before actually removing an existing grant to
    // nothing.
    const hadExisting = (selectedSection?.courseIds || []).length > 0;
    if (picked.size === 0 && hadExisting) {
      const ok = window.confirm(
        "This will remove ALL course access currently granted to this section's students — nothing is selected. Continue?"
      );
      if (!ok) return;
    }
    setSaving(true);
    const res = await onSave(sectionId, Array.from(picked), notify);
    setSaving(false);
    if (res.error) {
      setSavedNote(res.error);
    } else if (notify) {
      setSavedNote(
        `Saved. Notify attempted for ${res.notified ?? 0} student(s) — ${res.delivered ?? 0} delivered${
          !res.delivered ? " (no email provider configured yet)" : ""
        }.`
      );
    } else {
      setSavedNote("Saved.");
    }
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

      {programId && (
        <label className="mt-4 block">
          <span className="text-xs font-medium text-slate-500">Center</span>
          <select
            value={centerId}
            onChange={(e) => setCenterId(e.target.value)}
            disabled={centersForProgram.length === 0}
            className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
          >
            <option value="">{centersForProgram.length === 0 ? "No centers assigned" : "Select a center…"}</option>
            {centersForProgram.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
      )}

      {centerId && (
        <label className="mt-4 block">
          <span className="text-xs font-medium text-slate-500">Section</span>
          <select
            value={sectionId}
            onChange={(e) => setSectionId(e.target.value)}
            disabled={sectionsForProgramCenter.length === 0}
            className="mt-1 w-full rounded border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
          >
            <option value="">
              {sectionsForProgramCenter.length === 0 ? "No sections in this center" : "Select a section…"}
            </option>
            {sectionsForProgramCenter.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>
      )}

      {!sectionId ? (
        <div className="mt-6 rounded border border-dashed border-slate-200 py-12 text-center text-sm text-slate-400">
          Pick a Program, Center and Section to choose the courses assigned to it.
        </div>
      ) : (
        <>
          <div className="mt-5">
            <div className="text-xs font-semibold uppercase tracking-wide text-slate-400">
              Courses assigned to this section
            </div>
            <p className="mt-1 text-xs text-slate-400">
              Pick a whole Board Tree subject, or expand ▸ it to assign individual chapters instead.
            </p>
            {courses.length === 0 ? (
              <div className="mt-2 text-sm text-slate-400">
                No courses in your catalog yet — create courses in Resources first.
              </div>
            ) : (
              <div className="mt-2 max-h-96 overflow-y-auto rounded border border-slate-200">
                <CourseTree courses={courses} picked={picked} onToggle={toggle} />
              </div>
            )}
          </div>

          <label className="mt-4 flex cursor-pointer items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={notify}
              onChange={(e) => setNotify(e.target.checked)}
              className="accent-emerald-600"
            />
            Notify students in this section by email
          </label>

          <div className="mt-3 flex items-center gap-3">
            <button
              onClick={save}
              disabled={saving}
              className="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Saving…" : "Save courses"}
            </button>
            {savedNote && <span className="text-sm text-slate-600">{savedNote}</span>}
          </div>
        </>
      )}
    </div>
  );
}
