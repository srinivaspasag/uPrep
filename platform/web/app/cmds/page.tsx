"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell, { CmdsSubjectsRail } from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Resource = {
  id: string;
  title: string;
  type: string;
  subject: string | null;
  addedBy: string | null;
  addedAt: number;
  url?: string | null;
  count?: number;
};

type Crumb = { id: string; name: string };

const TYPE_ICON: Record<string, string> = {
  FOLDER: "📁",
  DOCUMENT: "📄",
  VIDEO: "🎬",
  TEST: "📕",
  MODULE: "🟩",
  QUESTION_SET: "🟦",
};

const ADD_MENU: { icon: string; label: string; action: string }[] = [
  { icon: "📁", label: "Add a Folder", action: "folder" },
  { icon: "📄", label: "Add a Document", action: "/cmds/documents/new" },
  { icon: "🟦", label: "Add a Question Set", action: "/cmds/questions/set/new" },
  { icon: "📕", label: "Add a Test", action: "/cmds/tests/new" },
  { icon: "🎬", label: "Add a Video", action: "/cmds/videos/new" },
  { icon: "🟩", label: "Create a Module", action: "/cmds/modules/new" },
  { icon: "⚙️", label: "Auto Generate Test", action: "/cmds/tests/auto" },
];

const FILTERS = ["All Resources", "FOLDER", "DOCUMENT", "VIDEO", "TEST", "MODULE", "QUESTION_SET"];

export default function CmdsResourcesPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [subject, setSubject] = useState("All Subjects");
  const [typeFilter, setTypeFilter] = useState("All Resources");
  const [sortBy, setSortBy] = useState<"date" | "title">("date");
  const [rows, setRows] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [folderOpen, setFolderOpen] = useState(false);
  const [folderName, setFolderName] = useState("");
  const [path, setPath] = useState<Crumb[]>([]);
  const [moveTarget, setMoveTarget] = useState<Resource | null>(null);
  const addRef = useRef<HTMLDivElement>(null);

  const currentFolderId = path.length ? path[path.length - 1].id : null;

  useEffect(() => {
    setSession(getSession());
    // Restore the folder view when returning from the upload pages
    // (?folder=<id>&folderName=<name>).
    const sp = new URLSearchParams(window.location.search);
    const fid = sp.get("folder");
    const fname = sp.get("folderName");
    if (fid) setPath([{ id: fid, name: fname || "Folder" }]);
  }, []);

  async function load() {
    setLoading(true);
    try {
      const params = new URLSearchParams({ subject, type: typeFilter });
      if (currentFolderId) params.set("parentId", currentFolderId);
      const res = await fetch(`/api/cmds/content?${params.toString()}`);
      const data = await res.json();
      setRows(data.resources || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (session) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session, subject, typeFilter, currentFolderId]);

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (addRef.current && !addRef.current.contains(e.target as Node)) setAddOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const sorted = useMemo(() => {
    const copy = [...rows];
    if (sortBy === "title") copy.sort((a, b) => a.title.localeCompare(b.title));
    else copy.sort((a, b) => b.addedAt - a.addedAt);
    // Folders always float to the top for easy navigation.
    copy.sort((a, b) => (a.type === "FOLDER" ? -1 : 0) - (b.type === "FOLDER" ? -1 : 0));
    return copy;
  }, [rows, sortBy]);

  function onAddClick(action: string) {
    setAddOpen(false);
    if (action === "folder") {
      setFolderOpen(true);
      return;
    }
    // Carry the current folder into the upload page so the file is filed here.
    if (currentFolderId) {
      const name = path[path.length - 1]?.name || "Folder";
      router.push(
        `${action}?folder=${encodeURIComponent(currentFolderId)}&folderName=${encodeURIComponent(name)}`
      );
    } else {
      router.push(action);
    }
  }

  async function createFolder() {
    if (!folderName.trim()) return;
    await fetch("/api/cmds/content", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        kind: "folder",
        name: folderName.trim(),
        subject: subject === "All Subjects" ? "" : subject,
        userId: session?.id,
        parentId: currentFolderId,
      }),
    });
    setFolderName("");
    setFolderOpen(false);
    load();
  }

  function openFolder(r: Resource) {
    setPath((p) => [...p, { id: r.id, name: r.title }]);
  }

  function goToCrumb(index: number) {
    // index -1 = root
    setPath((p) => (index < 0 ? [] : p.slice(0, index + 1)));
  }

  async function renameResource(r: Resource) {
    const name = window.prompt("Rename to:", r.title);
    if (name == null || !name.trim() || name.trim() === r.title) return;
    await fetch("/api/cmds/content", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: r.id, type: r.type, action: "rename", name: name.trim() }),
    });
    load();
  }

  async function deleteResource(r: Resource) {
    if (!window.confirm(`Delete "${r.title}"? This can't be undone from the UI.`)) return;
    await fetch(`/api/cmds/content?id=${encodeURIComponent(r.id)}&type=${encodeURIComponent(r.type)}`, {
      method: "DELETE",
    });
    load();
  }

  return (
    <CmdsShell active="resources">
      <div className="flex">
        <CmdsSubjectsRail subject={subject} onSubject={setSubject} />

        <main className="flex-1 px-8 py-6">
          <h1 className="text-2xl font-light text-slate-700">Institute Resources</h1>

          {/* Breadcrumbs */}
          <div className="mt-2 flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <button onClick={() => goToCrumb(-1)} className="hover:text-slate-800">
              All Resources
            </button>
            {path.map((c, i) => (
              <span key={c.id} className="flex items-center gap-1">
                <span className="text-slate-300">›</span>
                <button
                  onClick={() => goToCrumb(i)}
                  className={i === path.length - 1 ? "font-medium text-slate-800" : "hover:text-slate-800"}
                >
                  {c.name}
                </button>
              </span>
            ))}
          </div>

          {/* Toolbar */}
          <div className="mt-4 flex items-center justify-end gap-6">
            <div className="relative" ref={addRef}>
              <button
                onClick={() => setAddOpen((o) => !o)}
                className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
              >
                + Add Content ▾
              </button>
              {addOpen && (
                <div className="absolute right-0 z-30 mt-1 w-56 rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg">
                  {ADD_MENU.map((m) => (
                    <button
                      key={m.label}
                      onClick={() => onAddClick(m.action)}
                      className="flex w-full items-center gap-3 px-4 py-2 text-left text-slate-700 hover:bg-slate-50"
                    >
                      <span>{m.icon}</span>
                      {m.label}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <label className="flex flex-col text-[11px] text-slate-400">
              Filter By
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="mt-0.5 border-b border-slate-300 pb-0.5 text-sm text-slate-700 outline-none"
              >
                {FILTERS.map((f) => (
                  <option key={f} value={f}>
                    {f === "All Resources" ? "All Resources" : label(f)}
                  </option>
                ))}
              </select>
            </label>

            <label className="flex flex-col text-[11px] text-slate-400">
              Sort By
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as "date" | "title")}
                className="mt-0.5 border-b border-slate-300 pb-0.5 text-sm text-slate-700 outline-none"
              >
                <option value="date">Date Added</option>
                <option value="title">Title</option>
              </select>
            </label>
          </div>

          {/* Table */}
          <div className="mt-4 overflow-hidden rounded border border-slate-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="px-4 py-2 font-medium">Title</th>
                  <th className="px-4 py-2 font-medium">Added By</th>
                  <th className="px-4 py-2 font-medium">Date Added</th>
                  <th className="px-4 py-2 font-medium">Type</th>
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
                ) : sorted.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                      {path.length ? "This folder is empty" : "No resources"}
                    </td>
                  </tr>
                ) : (
                  sorted.map((r) => (
                    <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <span>{TYPE_ICON[r.type] || "📄"}</span>
                          <RowTitle r={r} onOpenFolder={openFolder} />
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-500">{r.addedBy || "—"}</td>
                      <td className="px-4 py-3 text-slate-500">
                        {r.addedAt ? new Date(r.addedAt).toLocaleDateString() : "—"}
                      </td>
                      <td className="px-4 py-3 text-slate-400">{label(r.type)}</td>
                      <td className="px-4 py-3">
                        <RowActions
                          onRename={() => renameResource(r)}
                          onMove={() => setMoveTarget(r)}
                          onDelete={() => deleteResource(r)}
                        />
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </main>
      </div>

      {/* Add Folder modal */}
      {folderOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-[380px] rounded-lg bg-white p-6 shadow-xl">
            <h3 className="text-lg font-semibold text-slate-800">Add a Folder</h3>
            {path.length > 0 && (
              <p className="mt-1 text-sm text-slate-500">Inside {path[path.length - 1].name}</p>
            )}
            <input
              autoFocus
              value={folderName}
              onChange={(e) => setFolderName(e.target.value)}
              placeholder="Folder name"
              className="mt-4 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              onKeyDown={(e) => e.key === "Enter" && createFolder()}
            />
            <div className="mt-5 flex justify-end gap-2">
              <button
                onClick={() => setFolderOpen(false)}
                className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                onClick={createFolder}
                className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}

      {moveTarget && (
        <MoveModal
          resource={moveTarget}
          onClose={() => setMoveTarget(null)}
          onMoved={() => {
            setMoveTarget(null);
            load();
          }}
        />
      )}
    </CmdsShell>
  );
}

function RowTitle({ r, onOpenFolder }: { r: Resource; onOpenFolder: (r: Resource) => void }) {
  if (r.type === "FOLDER")
    return (
      <button onClick={() => onOpenFolder(r)} className="font-medium text-slate-700 hover:text-blue-600">
        {r.title}
      </button>
    );
  if (r.type === "VIDEO" || r.type === "DOCUMENT")
    return (
      <a href={r.url || "#"} target="_blank" className="text-slate-700 hover:text-blue-600">
        {r.title}
      </a>
    );
  if (r.type === "TEST")
    return (
      <Link href={`/test/${r.id}`} className="text-slate-700 hover:text-blue-600">
        {r.title}
      </Link>
    );
  return <span className="text-slate-700">{r.title}</span>;
}

function RowActions({
  onRename,
  onMove,
  onDelete,
}: {
  onRename: () => void;
  onMove: () => void;
  onDelete: () => void;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);
  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen((o) => !o)}
        className="rounded px-2 py-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
      >
        ⋯
      </button>
      {open && (
        <div className="absolute right-0 z-20 mt-1 w-36 rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg">
          <button
            onClick={() => {
              setOpen(false);
              onRename();
            }}
            className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
          >
            Rename
          </button>
          <button
            onClick={() => {
              setOpen(false);
              onMove();
            }}
            className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
          >
            Move to folder
          </button>
          <button
            onClick={() => {
              setOpen(false);
              onDelete();
            }}
            className="block w-full px-4 py-2 text-left text-red-500 hover:bg-red-50"
          >
            Delete
          </button>
        </div>
      )}
    </div>
  );
}

function MoveModal({
  resource,
  onClose,
  onMoved,
}: {
  resource: Resource;
  onClose: () => void;
  onMoved: () => void;
}) {
  const [folders, setFolders] = useState<{ id: string; name: string }[]>([]);
  const [target, setTarget] = useState<string>("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch("/api/cmds/content?allFolders=1")
      .then((r) => r.json())
      .then((d) => setFolders((d.folders || []).filter((f: any) => f.id !== resource.id)))
      .catch(() => {});
  }, [resource.id]);

  async function move() {
    setSaving(true);
    await fetch("/api/cmds/content", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: resource.id,
        type: resource.type,
        action: "move",
        folderId: target || null,
      }),
    });
    setSaving(false);
    onMoved();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[400px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Move “{resource.title}”</h3>
        <label className="mt-4 block text-sm font-medium text-slate-600">Destination folder</label>
        <select
          value={target}
          onChange={(e) => setTarget(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        >
          <option value="">All Resources (root)</option>
          {folders.map((f) => (
            <option key={f.id} value={f.id}>
              {f.name}
            </option>
          ))}
        </select>
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={move}
            disabled={saving}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {saving ? "Moving…" : "Move"}
          </button>
        </div>
      </div>
    </div>
  );
}

function label(type: string): string {
  const m: Record<string, string> = {
    FOLDER: "Folder",
    DOCUMENT: "Document",
    VIDEO: "Video",
    TEST: "Test",
    MODULE: "Module",
    QUESTION_SET: "Question Set",
  };
  return m[type] || type;
}
