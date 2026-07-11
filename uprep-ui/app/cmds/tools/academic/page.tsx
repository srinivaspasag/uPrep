"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Entity = {
  id: string;
  name: string;
  code: string;
  departmentId?: string | null;
  programId?: string | null;
  centerId?: string | null;
};

type Data = {
  departments: Entity[];
  programs: Entity[];
  centers: Entity[];
  sections: Entity[];
};

export default function AcademicStructurePage() {
  const [data, setData] = useState<Data>({ departments: [], programs: [], centers: [], sections: [] });
  const [loading, setLoading] = useState(true);
  const [dept, setDept] = useState<string | null>(null);
  const [program, setProgram] = useState<string | null>(null);
  const [center, setCenter] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    try {
      const d = await (await fetch("/api/cmds/tools/academic")).json();
      setData({
        departments: d.departments || [],
        programs: d.programs || [],
        centers: d.centers || [],
        sections: d.sections || [],
      });
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

  const programsForDept = dept
    ? data.programs.filter((p) => p.departmentId === dept)
    : data.programs;
  const sectionsForCenter = center
    ? data.sections.filter((s) => s.centerId === center)
    : data.sections;

  return (
    <CmdsShell>
      <div className="flex">
        {/* Left rail */}
        <aside className="w-[170px] shrink-0 border-r border-slate-100 px-4 py-6">
          <div className="rounded border border-slate-200 px-3 py-1.5 text-sm text-slate-700">
            Classroom Centers
          </div>
        </aside>

        <main className="flex-1 px-8 py-6">
          <h1 className="text-2xl font-light text-slate-700">Edit Academic Structure</h1>
          <div className="mt-1 flex gap-6 border-b border-slate-200 pb-2 text-sm">
            <span className="border-b-2 border-emerald-500 pb-2 font-medium text-slate-800">
              Edit Academic Structure
            </span>
            <span className="text-slate-400">Assign Courses</span>
          </div>

          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading structure…</div>
          ) : (
            <div className="mt-6 grid grid-cols-4 gap-3">
              <Column
                title="Departments"
                kind="department"
                items={data.departments}
                selected={dept}
                onSelect={(id) => {
                  setDept(id);
                  setProgram(null);
                  setCenter(null);
                }}
                onAdd={(name) => add("department", name)}
                onRename={rename}
                onRemove={remove}
              />
              <Column
                title="Programs"
                kind="program"
                items={programsForDept}
                selected={program}
                onSelect={(id) => {
                  setProgram(id);
                  setCenter(null);
                }}
                onAdd={(name) => add("program", name, dept ? { departmentId: dept } : {})}
                onRename={rename}
                onRemove={remove}
                disabled={data.departments.length > 0 && !dept}
                disabledHint="Select a department"
              />
              <Column
                title="Centers"
                kind="center"
                items={data.centers}
                selected={center}
                onSelect={(id) => setCenter(id)}
                onAdd={(name) => add("center", name)}
                onRename={rename}
                onRemove={remove}
              />
              <Column
                title="Sections"
                kind="section"
                items={sectionsForCenter}
                selected={null}
                onSelect={() => {}}
                onAdd={(name) =>
                  add("section", name, {
                    programId: program || undefined,
                    centerId: center || undefined,
                  })
                }
                onRename={rename}
                onRemove={remove}
                disabled={data.centers.length > 0 && !center}
                disabledHint="Select a center"
              />
            </div>
          )}
        </main>
      </div>
    </CmdsShell>
  );
}

function Column({
  title,
  kind,
  items,
  selected,
  onSelect,
  onAdd,
  onRename,
  onRemove,
  disabled,
  disabledHint,
}: {
  title: string;
  kind: string;
  items: Entity[];
  selected: string | null;
  onSelect: (id: string) => void;
  onAdd: (name: string) => void;
  onRename: (kind: string, id: string, name: string) => void;
  onRemove: (kind: string, id: string) => void;
  disabled?: boolean;
  disabledHint?: string;
}) {
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");

  function submit() {
    if (!name.trim()) return;
    onAdd(name.trim());
    setName("");
    setAdding(false);
  }

  return (
    <div className="rounded border border-slate-200">
      <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-3 py-2">
        <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{title}</span>
        <button
          onClick={() => !disabled && setAdding((o) => !o)}
          disabled={disabled}
          className="text-lg leading-none text-emerald-600 hover:text-emerald-700 disabled:text-slate-300"
          title={disabled ? disabledHint : `Add ${title}`}
        >
          +
        </button>
      </div>
      {adding && (
        <div className="border-b border-slate-100 p-2">
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && submit()}
            placeholder={`New ${title.slice(0, -1)}`}
            className="w-full rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
          />
        </div>
      )}
      <div className="max-h-[420px] overflow-y-auto">
        {disabled ? (
          <div className="px-3 py-6 text-center text-xs text-slate-400">{disabledHint}</div>
        ) : items.length === 0 ? (
          <div className="px-3 py-6 text-center text-xs text-slate-400">None</div>
        ) : (
          items.map((it) =>
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
                <button onClick={() => onSelect(it.id)} className="flex-1 text-left">
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
                    ✕
                  </button>
                </div>
              </div>
            )
          )
        )}
      </div>
    </div>
  );
}
