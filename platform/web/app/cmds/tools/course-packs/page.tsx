"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Course = { id: string; name: string };
type Pack = {
  id: string;
  name: string;
  courseIds: string[];
  courseCount: number;
  courseNames: string[];
};

export default function CoursePacksPage() {
  const [packs, setPacks] = useState<Pack[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [forbidden, setForbidden] = useState(false);
  const [editing, setEditing] = useState<Pack | "new" | null>(null);

  async function load() {
    setLoading(true);
    try {
      const res = await fetch("/api/cmds/tools/course-packs");
      if (res.status === 403) {
        setForbidden(true);
        return;
      }
      const d = await res.json();
      setPacks(d.packs || []);
      setCourses(d.courses || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function remove(p: Pack) {
    if (!confirm(`Delete pack "${p.name}"? Orgs granted this pack will lose it.`)) return;
    await fetch(`/api/cmds/tools/course-packs?id=${encodeURIComponent(p.id)}`, { method: "DELETE" });
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-light text-slate-700">Course Packs</h1>
            <p className="mt-1 text-sm text-slate-500">
              Super-admin only. Bundle courses into a named pack, then grant it to an institute
              from <span className="font-medium">Organizations → Grant courses</span>.
            </p>
          </div>
          {!forbidden && (
            <button
              onClick={() => setEditing("new")}
              className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
            >
              + Create Pack
            </button>
          )}
        </div>

        {forbidden ? (
          <div className="mt-10 rounded border border-amber-200 bg-amber-50 p-6 text-center text-sm text-amber-700">
            This section is restricted to super admins.
          </div>
        ) : loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : packs.length === 0 ? (
          <div className="mt-10 rounded border border-slate-200 bg-slate-50 p-6 text-center text-sm text-slate-500">
            No packs yet. Create one to bundle courses together.
          </div>
        ) : (
          <div className="mt-6 space-y-3">
            {packs.map((p) => (
              <div key={p.id} className="rounded-lg border border-slate-200 bg-white p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <div className="font-semibold text-slate-800">{p.name}</div>
                    <div className="mt-1 text-xs text-slate-500">
                      {p.courseCount} course{p.courseCount === 1 ? "" : "s"}
                      {p.courseNames.length > 0 && (
                        <>: {p.courseNames.join(", ")}</>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => setEditing(p)}
                      className="rounded border border-slate-200 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-50"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => remove(p)}
                      className="rounded border border-red-200 px-2.5 py-1 text-xs text-red-600 hover:bg-red-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {editing && (
        <PackModal
          pack={editing === "new" ? null : editing}
          courses={courses}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </CmdsShell>
  );
}

function PackModal({
  pack,
  courses,
  onClose,
  onSaved,
}: {
  pack: Pack | null;
  courses: Course[];
  onClose: () => void;
  onSaved: () => void;
}) {
  const [name, setName] = useState(pack?.name || "");
  const [selected, setSelected] = useState<Set<string>>(new Set(pack?.courseIds || []));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  async function save() {
    setError("");
    if (!name.trim()) return setError("Pack name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/course-packs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          id: pack?.id,
          name: name.trim(),
          courseIds: Array.from(selected),
        }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Save failed");
        return;
      }
      onSaved();
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="flex max-h-[80vh] w-[480px] flex-col rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">
          {pack ? "Edit pack" : "Create pack"}
        </h3>
        <input
          autoFocus
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Pack name (e.g. NEET 2027 Complete)"
          className="mt-4 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        />
        <div className="mt-4 text-sm font-medium text-slate-600">Courses in this pack</div>
        <div className="mt-2 flex-1 overflow-y-auto rounded border border-slate-200">
          {courses.length === 0 ? (
            <div className="p-4 text-sm text-slate-400">
              This org has no courses to bundle yet.
            </div>
          ) : (
            courses.map((c) => (
              <label
                key={c.id}
                className="flex cursor-pointer items-center gap-2 border-b border-slate-100 px-3 py-2 text-sm text-slate-700 last:border-0 hover:bg-slate-50"
              >
                <input
                  type="checkbox"
                  checked={selected.has(c.id)}
                  onChange={() => toggle(c.id)}
                  className="accent-emerald-600"
                />
                {c.name}
              </label>
            ))
          )}
        </div>
        {error && <div className="mt-3 text-sm text-red-600">{error}</div>}
        <div className="mt-5 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            onClick={save}
            disabled={saving}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {saving ? "Saving…" : "Save pack"}
          </button>
        </div>
      </div>
    </div>
  );
}
