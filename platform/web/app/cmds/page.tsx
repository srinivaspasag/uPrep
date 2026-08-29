"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell, { CmdsSubjectsRail } from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";
import { canManageContent } from "@/lib/roles";
import { naturalCompare } from "@/lib/courses";

type Resource = {
  id: string;
  title: string;
  type: string;
  subject: string | null;
  addedBy: string | null;
  addedAt: number;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
  count?: number;
  hidden?: boolean;
  downloadEnabled?: boolean;
  sectionIds?: string[];
  order?: number | null;
  visibleUserIds?: string[];
  hiddenUserIds?: string[];
};

type Crumb = { id: string; name: string };

const TYPE_ICON: Record<string, string> = {
  FOLDER: "📁",
  DOCUMENT: "📄",
  VIDEO: "🎬",
  TEST: "📕",
  MODULE: "🟩",
  QUESTION_SET: "🟦",
  BOOK: "📖",
};

const ADD_MENU: { icon: string; label: string; action: string }[] = [
  { icon: "📁", label: "Add a Folder", action: "folder" },
  { icon: "🗂️", label: "Add a Session", action: "session" },
  { icon: "📄", label: "Add a Document", action: "/cmds/documents/new" },
  { icon: "📖", label: "Add a Book", action: "/cmds/books/new" },
  { icon: "🟦", label: "Add a Question Set", action: "/cmds/questions/set/new" },
  { icon: "📕", label: "Add a Test", action: "/cmds/tests/new" },
  { icon: "🎬", label: "Add a Video", action: "/cmds/videos/new" },
  { icon: "🟩", label: "Create a Module", action: "/cmds/modules/new" },
];

const FILTERS = ["All Resources", "FOLDER", "DOCUMENT", "VIDEO", "TEST", "MODULE", "QUESTION_SET", "BOOK"];

export default function CmdsResourcesPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [subject, setSubject] = useState("All Subjects");
  const [typeFilter, setTypeFilter] = useState("All Resources");
  const [sortBy, setSortBy] = useState<"date" | "title" | "sequence">("date");
  const [rows, setRows] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [folderOpen, setFolderOpen] = useState(false);
  const [folderName, setFolderName] = useState("");
  const [path, setPath] = useState<Crumb[]>([]);
  const [moveTarget, setMoveTarget] = useState<Resource[] | null>(null);
  const [addToSectionOpen, setAddToSectionOpen] = useState(false);
  const [previewVideo, setPreviewVideo] = useState<Resource | null>(null);
  const [studentVisTarget, setStudentVisTarget] = useState<Resource | null>(null);
  const [checked, setChecked] = useState<Set<string>>(new Set());
  const [sectionsMeta, setSectionsMeta] = useState<{ id: string; name: string; programId: string | null }[]>([]);
  const addRef = useRef<HTMLDivElement>(null);

  const currentFolderId = path.length ? path[path.length - 1].id : null;
  const isAdmin = (session?.profile || "").trim().toUpperCase() === "MANAGER";
  // Salesperson is excluded from content/resources, matching legacy
  // (QrResources/QrPrograms templates gate on orgUserProfile != "SALESPERSON").
  const canAccess = !session || canManageContent(session.profile);

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
      setChecked(new Set());
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (session) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session, subject, typeFilter, currentFolderId]);

  // Section -> Program lookup, so the "N Sections" cell can link straight
  // to the program a piece of content was assigned to — bug found live:
  // there was no way to click through from a content row to the program it
  // had just been assigned to.
  useEffect(() => {
    if (!session) return;
    fetch("/api/cmds/tools/academic")
      .then((r) => r.json())
      .then((d) => setSectionsMeta(d.sections || []))
      .catch(() => {});
  }, [session]);

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
    else if (sortBy === "sequence") {
      // Same rule students/mobile see (lib/courses.ts): explicit order wins,
      // otherwise natural sort so "Session 2" doesn't land after "Session 10".
      copy.sort((a, b) => {
        const ao = typeof a.order === "number" ? a.order : null;
        const bo = typeof b.order === "number" ? b.order : null;
        if (ao !== null && bo !== null) return ao - bo;
        if (ao !== null) return -1;
        if (bo !== null) return 1;
        return naturalCompare(a.title, b.title);
      });
    } else copy.sort((a, b) => b.addedAt - a.addedAt);
    // Bug found live: folders used to always float to the top regardless of
    // the chosen sort, so a test/video/document created straight at the
    // root (no folder picked) got buried below every subject folder even
    // though it was the single most-recently-added item — "Sort By: Date
    // Added" should mean what it says, mixed types included.
    return copy;
  }, [rows, sortBy]);

  async function moveInSequence(index: number, direction: -1 | 1) {
    const other = index + direction;
    if (other < 0 || other >= sorted.length) return;
    const a = sorted[index];
    const b = sorted[other];
    // Assign fresh, unambiguous order values for the whole visible list so
    // ties (both null, or duplicate numbers from earlier renames) don't
    // produce a no-op swap.
    const next = sorted.map((r, i) => ({ ...r, order: i }));
    const ai = next.findIndex((r) => r.id === a.id);
    const bi = next.findIndex((r) => r.id === b.id);
    [next[ai].order, next[bi].order] = [next[bi].order, next[ai].order];
    setRows((prev) => prev.map((r) => next.find((n) => n.id === r.id) || r));
    // Persist every row's order, not just the swapped pair — this render
    // just made every visible row's order explicit (via the map above), so
    // only saving the two that moved would leave the rest as order:null and
    // have them silently snap back to natural-sort position on next load.
    await Promise.all(
      next.map((r) =>
        fetch("/api/cmds/content", {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ id: r.id, type: r.type, action: "reorder", order: r.order }),
        })
      )
    );
  }

  function onAddClick(action: string) {
    setAddOpen(false);
    if (action === "folder") {
      setFolderOpen(true);
      return;
    }
    if (action === "session") {
      addSession();
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

  // One-click "Session N" folder creation — staff said they repeatedly type
  // Session 1, Session 2, etc. by hand via the generic Add a Folder modal.
  // Query FOLDER siblings directly (not `rows`, which is filtered by the
  // current type/subject filters and may not include folders at all) so the
  // next number is correct regardless of what the user is currently viewing.
  async function addSession() {
    setAddOpen(false);
    const params = new URLSearchParams({ subject: "All Subjects", type: "FOLDER" });
    if (currentFolderId) params.set("parentId", currentFolderId);
    const data = await fetch(`/api/cmds/content?${params.toString()}`).then((r) => r.json());
    const existing: Resource[] = data.resources || [];
    let max = 0;
    for (const r of existing) {
      const m = /^session\s+(\d+)$/i.exec(r.title.trim());
      if (m) max = Math.max(max, parseInt(m[1], 10));
    }
    await fetch("/api/cmds/content", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        kind: "folder",
        name: `Session ${max + 1}`,
        subject: subject === "All Subjects" ? "" : subject,
        userId: session?.id,
        parentId: currentFolderId,
      }),
    });
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

  async function toggleVisibility(r: Resource) {
    await fetch("/api/cmds/content", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: r.id, type: r.type, action: "visibility", hidden: !r.hidden }),
    });
    load();
  }

  async function toggleDownload(r: Resource) {
    await fetch("/api/cmds/content", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: r.id,
        type: r.type,
        action: "download",
        downloadEnabled: !(r.downloadEnabled !== false),
      }),
    });
    load();
  }

  function toggleChecked(id: string) {
    setChecked((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAllChecked() {
    setChecked((prev) => (prev.size === sorted.length ? new Set() : new Set(sorted.map((r) => r.id))));
  }

  const checkedResources = useMemo(() => rows.filter((r) => checked.has(r.id)), [rows, checked]);

  async function bulkDelete() {
    if (checkedResources.length === 0) return;
    if (!window.confirm(`Delete ${checkedResources.length} item(s)? This can't be undone from the UI.`)) return;
    await Promise.all(
      checkedResources.map((r) =>
        fetch(`/api/cmds/content?id=${encodeURIComponent(r.id)}&type=${encodeURIComponent(r.type)}`, {
          method: "DELETE",
        })
      )
    );
    load();
  }

  if (session && !canAccess) {
    return (
      <CmdsShell active="resources">
        <div className="px-8 py-16 text-center text-slate-400">
          You don&apos;t have access to Institute Resources.
        </div>
      </CmdsShell>
    );
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
            <Link
              href="/cmds/questions"
              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              📚 Question Bank
            </Link>
            <Link
              href="/cmds/books"
              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              📖 Books
            </Link>
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
                onChange={(e) => setSortBy(e.target.value as "date" | "title" | "sequence")}
                className="mt-0.5 border-b border-slate-300 pb-0.5 text-sm text-slate-700 outline-none"
              >
                <option value="date">Date Added</option>
                <option value="title">Title</option>
                <option value="sequence">Sequence (chapter order)</option>
              </select>
            </label>
          </div>

          {/* Bulk action bar — mirrors legacy's Move / Delete / Add To Programs */}
          {checked.size > 0 && (
            <div className="mt-3 flex items-center gap-2 rounded border border-slate-200 bg-slate-50 px-3 py-2">
              <span className="text-xs text-slate-500">{checked.size} selected</span>
              <button
                onClick={() => setMoveTarget(checkedResources)}
                className="rounded border border-slate-300 bg-white px-3 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
              >
                Move
              </button>
              <button
                onClick={bulkDelete}
                className="rounded border border-slate-300 bg-white px-3 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
              >
                Delete
              </button>
              {isAdmin && (
                <button
                  onClick={() => setAddToSectionOpen(true)}
                  className="rounded border border-slate-300 bg-white px-3 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                >
                  Add To Programs
                </button>
              )}
            </div>
          )}

          {/* Table */}
          <div className="mt-4 overflow-hidden rounded border border-slate-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="w-8 px-4 py-2">
                    {sorted.length > 0 && (
                      <input
                        type="checkbox"
                        checked={checked.size === sorted.length}
                        onChange={toggleAllChecked}
                      />
                    )}
                  </th>
                  <th className="px-4 py-2 font-medium">Title</th>
                  <th className="px-4 py-2 font-medium">Added By</th>
                  <th className="px-4 py-2 font-medium">Date Added</th>
                  <th className="px-4 py-2 font-medium">Type</th>
                  <th className="px-4 py-2 font-medium">Added To</th>
                  <th className="w-10 px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                      Loading…
                    </td>
                  </tr>
                ) : sorted.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                      {path.length ? "This folder is empty" : "No resources"}
                    </td>
                  </tr>
                ) : (
                  sorted.map((r, i) => (
                    <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-4 py-3">
                        <input
                          type="checkbox"
                          checked={checked.has(r.id)}
                          onChange={() => toggleChecked(r.id)}
                        />
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          {sortBy === "sequence" && (
                            <span className="flex shrink-0 flex-col">
                              <button
                                onClick={() => moveInSequence(i, -1)}
                                disabled={i === 0}
                                className="leading-none text-slate-400 hover:text-slate-700 disabled:opacity-20"
                                title="Move up"
                              >
                                ▲
                              </button>
                              <button
                                onClick={() => moveInSequence(i, 1)}
                                disabled={i === sorted.length - 1}
                                className="leading-none text-slate-400 hover:text-slate-700 disabled:opacity-20"
                                title="Move down"
                              >
                                ▼
                              </button>
                            </span>
                          )}
                          <span>{TYPE_ICON[r.type] || "📄"}</span>
                          <RowTitle r={r} onOpenFolder={openFolder} onPreviewVideo={setPreviewVideo} />
                          {r.hidden && (
                            <span className="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-medium text-amber-600">
                              Hidden
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-500">{r.addedBy || "—"}</td>
                      <td className="px-4 py-3 text-slate-500">
                        {r.addedAt ? new Date(r.addedAt).toLocaleDateString() : "—"}
                      </td>
                      <td className="px-4 py-3 text-slate-400">{label(r.type)}</td>
                      <td className="px-4 py-3 text-slate-400">
                        {r.type === "FOLDER" || !r.sectionIds?.length ? (
                          "—"
                        ) : (
                          <SectionsCell sectionIds={r.sectionIds} sectionsMeta={sectionsMeta} />
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <RowActions
                          hidden={!!r.hidden}
                          isFolder={r.type === "FOLDER"}
                          editHref={r.type === "TEST" ? `/cmds/tests/${r.id}/edit` : undefined}
                          canToggleVisibility={isAdmin}
                          downloadEnabled={r.downloadEnabled !== false}
                          canToggleDownload={isAdmin && r.type === "DOCUMENT"}
                          canSetStudentVisibility={isAdmin && r.type !== "FOLDER"}
                          onRename={() => renameResource(r)}
                          onMove={() => setMoveTarget([r])}
                          onToggleVisibility={() => toggleVisibility(r)}
                          onToggleDownload={() => toggleDownload(r)}
                          onSetStudentVisibility={() => setStudentVisTarget(r)}
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
          resources={moveTarget}
          onClose={() => setMoveTarget(null)}
          onMoved={() => {
            setMoveTarget(null);
            load();
          }}
        />
      )}

      {addToSectionOpen && (
        <AddToSectionModal
          resources={checkedResources}
          onClose={() => setAddToSectionOpen(false)}
          onAdded={() => {
            setAddToSectionOpen(false);
            load();
          }}
        />
      )}

      {previewVideo && <VideoPreviewModal resource={previewVideo} onClose={() => setPreviewVideo(null)} />}

      {studentVisTarget && (
        <StudentVisibilityModal
          resource={studentVisTarget}
          onClose={() => setStudentVisTarget(null)}
          onSaved={() => {
            setStudentVisTarget(null);
            load();
          }}
        />
      )}
    </CmdsShell>
  );
}

function RowTitle({
  r,
  onOpenFolder,
  onPreviewVideo,
}: {
  r: Resource;
  onOpenFolder: (r: Resource) => void;
  onPreviewVideo: (r: Resource) => void;
}) {
  if (r.type === "FOLDER")
    return (
      <button onClick={() => onOpenFolder(r)} className="font-medium text-slate-700 hover:text-blue-600">
        {r.title}
      </button>
    );
  if (r.type === "VIDEO")
    // Bug found live: this used to link straight to the stored `url`, which
    // for a YouTube/Vimeo-added video is the raw watch-page link — clicking
    // it took the admin out of CMDS to the external site instead of playing
    // inline. Play it here via embedUrl (or the direct file for uploads).
    return (
      <button onClick={() => onPreviewVideo(r)} className="text-slate-700 hover:text-blue-600">
        {r.title}
      </button>
    );
  if (r.type === "DOCUMENT")
    return (
      <a href={r.url || "#"} className="text-slate-700 hover:text-blue-600">
        {r.title}
      </a>
    );
  if (r.type === "TEST")
    return (
      <Link href={`/test/${r.id}`} className="text-slate-700 hover:text-blue-600">
        {r.title}
      </Link>
    );
  if (r.type === "MODULE")
    // Bug found live: module rows were plain, inert text — no way to see
    // what a module actually contained (ebook/test/video items).
    return (
      <Link href={`/cmds/modules/${r.id}`} className="text-slate-700 hover:text-blue-600">
        {r.title}
      </Link>
    );
  return <span className="text-slate-700">{r.title}</span>;
}

function VideoPreviewModal({ resource, onClose }: { resource: Resource; onClose: () => void }) {
  const src = resource.embedUrl || resource.url;
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-6"
      onClick={onClose}
    >
      <div
        className="w-full max-w-3xl rounded-lg bg-white p-4 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-3 flex items-center justify-between">
          <span className="font-medium text-slate-800">{resource.title}</span>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
            ✕
          </button>
        </div>
        <div className="aspect-video w-full overflow-hidden rounded bg-black">
          {resource.embedUrl ? (
            <iframe
              src={resource.embedUrl}
              className="h-full w-full"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
              title={resource.title}
            />
          ) : src ? (
            <video
              src={src}
              controls
              controlsList="nodownload"
              disablePictureInPicture
              onContextMenu={(e) => e.preventDefault()}
              className="h-full w-full"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-slate-400">
              No playable source for this video.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function RowActions({
  hidden,
  isFolder,
  editHref,
  canToggleVisibility,
  downloadEnabled,
  canToggleDownload,
  canSetStudentVisibility,
  onRename,
  onMove,
  onToggleVisibility,
  onToggleDownload,
  onSetStudentVisibility,
  onDelete,
}: {
  hidden: boolean;
  isFolder: boolean;
  editHref?: string;
  canToggleVisibility: boolean;
  downloadEnabled: boolean;
  canToggleDownload: boolean;
  canSetStudentVisibility: boolean;
  onRename: () => void;
  onMove: () => void;
  onToggleVisibility: () => void;
  onToggleDownload: () => void;
  onSetStudentVisibility: () => void;
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
  // Bug found live: the menu always opened downward with a fixed position,
  // so for any row in the bottom ~300px of the viewport (common — this list
  // can run long) it got clipped by the browser window, hiding Delete and
  // whatever else sat below the fold. Flip upward when there isn't enough
  // room below instead of assuming there always is.
  const [openUpward, setOpenUpward] = useState(false);
  function toggleOpen() {
    if (!open && ref.current) {
      const rect = ref.current.getBoundingClientRect();
      setOpenUpward(window.innerHeight - rect.bottom < 300);
    }
    setOpen((o) => !o);
  }
  return (
    <div className="relative" ref={ref}>
      <button
        onClick={toggleOpen}
        className="rounded px-2 py-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
      >
        ⋯
      </button>
      {open && (
        <div
          className={`absolute right-0 z-20 w-36 rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg ${
            openUpward ? "bottom-full mb-1" : "mt-1"
          }`}
        >
          {editHref && (
            <Link
              href={editHref}
              onClick={() => setOpen(false)}
              className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
            >
              ✎ Edit
            </Link>
          )}
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
          {!isFolder && canToggleVisibility && (
            <button
              onClick={() => {
                setOpen(false);
                onToggleVisibility();
              }}
              className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
            >
              {hidden ? "Make visible to students" : "Hide from students"}
            </button>
          )}
          {canToggleDownload && (
            <button
              onClick={() => {
                setOpen(false);
                onToggleDownload();
              }}
              className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
            >
              {downloadEnabled ? "Disable download" : "Enable download"}
            </button>
          )}
          {canSetStudentVisibility && (
            <button
              onClick={() => {
                setOpen(false);
                onSetStudentVisibility();
              }}
              className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
            >
              Publish/Unpublish to a student
            </button>
          )}
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

function SectionsCell({
  sectionIds,
  sectionsMeta,
}: {
  sectionIds: string[];
  sectionsMeta: { id: string; name: string; programId: string | null }[];
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

  const metaById = new Map(sectionsMeta.map((s) => [s.id, s]));
  const matched = sectionIds.map((id) => metaById.get(id)).filter((s): s is NonNullable<typeof s> => !!s);

  // A single section resolves straight to its program; several become a
  // small dropdown so the count itself stays clickable either way.
  if (matched.length === 1) {
    return matched[0].programId ? (
      <Link href={`/cmds/programs/${matched[0].programId}`} className="hover:text-blue-600 hover:underline">
        {matched[0].name}
      </Link>
    ) : (
      <span>{matched[0].name}</span>
    );
  }

  return (
    <div className="relative" ref={ref}>
      <button onClick={() => setOpen((o) => !o)} className="hover:text-blue-600 hover:underline">
        {sectionIds.length} Section{sectionIds.length === 1 ? "" : "s"}
      </button>
      {open && (
        <div className="absolute left-0 z-20 mt-1 w-44 rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg">
          {matched.length === 0 ? (
            <div className="px-4 py-2 text-slate-400">No section details found</div>
          ) : (
            matched.map((s) =>
              s.programId ? (
                <Link
                  key={s.id}
                  href={`/cmds/programs/${s.programId}`}
                  className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
                  onClick={() => setOpen(false)}
                >
                  {s.name}
                </Link>
              ) : (
                <div key={s.id} className="px-4 py-2 text-slate-600">
                  {s.name}
                </div>
              )
            )
          )}
        </div>
      )}
    </div>
  );
}

type VisState = "default" | "visible" | "hidden";

// Per-student override on top of program/section visibility — "publish to a
// student" grants access to just one student even if they wouldn't
// otherwise see it (e.g. early access), "unpublish from a student" excludes
// just one student even if everything else says visible (e.g. an
// accommodation). Doesn't touch the section/program-wide setting.
function StudentVisibilityModal({
  resource,
  onClose,
  onSaved,
}: {
  resource: Resource;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [students, setStudents] = useState<{ id: string; name: string; memberId: string }[]>([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [states, setStates] = useState<Record<string, VisState>>({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch("/api/cmds/tools/people?profile=STUDENT")
      .then((r) => r.json())
      .then((d) => {
        const list = (d.members || []).map((m: any) => ({
          id: m.id,
          name: [m.firstName, m.lastName].filter(Boolean).join(" ") || m.memberId || "Student",
          memberId: m.memberId || "",
        }));
        setStudents(list);
        const initial: Record<string, VisState> = {};
        for (const id of resource.visibleUserIds || []) initial[id] = "visible";
        for (const id of resource.hiddenUserIds || []) initial[id] = "hidden";
        setStates(initial);
      })
      .catch(() => setError("Failed to load students"))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function setState(id: string, s: VisState) {
    setStates((prev) => {
      const next = { ...prev };
      if (s === "default") delete next[id];
      else next[id] = s;
      return next;
    });
  }

  async function save() {
    setSaving(true);
    setError("");
    const visibleUserIds = Object.keys(states).filter((id) => states[id] === "visible");
    const hiddenUserIds = Object.keys(states).filter((id) => states[id] === "hidden");
    try {
      const res = await fetch("/api/cmds/content", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          id: resource.id,
          type: resource.type,
          action: "student-visibility",
          visibleUserIds,
          hiddenUserIds,
        }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok || d.error) {
        setError(d.error || "Failed to save");
        setSaving(false);
        return;
      }
      onSaved();
    } catch {
      setError("Failed to save");
      setSaving(false);
    }
  }

  const filtered = students.filter((s) =>
    query.trim() ? `${s.name} ${s.memberId}`.toLowerCase().includes(query.trim().toLowerCase()) : true
  );
  const overrideCount = Object.keys(states).length;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-6">
      <div className="w-[460px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Publish/Unpublish to a student</h3>
        <p className="mt-1 text-sm text-slate-500">
          Override visibility of <span className="font-medium text-slate-700">{resource.title}</span> for
          individual students, on top of its normal section/program visibility.
        </p>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search students…"
          className="mt-3 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"
        />
        <div className="mt-2 max-h-72 overflow-y-auto rounded border border-slate-200">
          {loading ? (
            <div className="px-3 py-6 text-center text-sm text-slate-400">Loading…</div>
          ) : filtered.length === 0 ? (
            <div className="px-3 py-6 text-center text-sm text-slate-400">No students found</div>
          ) : (
            filtered.map((s) => {
              const state = states[s.id] || "default";
              return (
                <div
                  key={s.id}
                  className="flex items-center justify-between gap-2 border-b border-slate-50 px-4 py-2 text-sm"
                >
                  <span className="text-slate-700">
                    {s.name} <span className="text-xs text-slate-400">{s.memberId}</span>
                  </span>
                  <select
                    value={state}
                    onChange={(e) => setState(s.id, e.target.value as VisState)}
                    className={`rounded border px-2 py-1 text-xs ${
                      state === "visible"
                        ? "border-emerald-300 bg-emerald-50 text-emerald-700"
                        : state === "hidden"
                        ? "border-red-300 bg-red-50 text-red-700"
                        : "border-slate-200 text-slate-500"
                    }`}
                  >
                    <option value="default">Default</option>
                    <option value="visible">Force visible</option>
                    <option value="hidden">Force hidden</option>
                  </select>
                </div>
              );
            })
          )}
        </div>
        {overrideCount > 0 && (
          <p className="mt-2 text-xs text-slate-400">
            {overrideCount} student override{overrideCount === 1 ? "" : "s"} set.
          </p>
        )}
        {error && <div className="mt-2 text-sm text-red-600">{error}</div>}
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={save}
            disabled={saving}
            className="rounded bg-blue-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
          >
            {saving ? "Saving…" : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}

type MoveFolder = { id: string; name: string };

// Bug found live: this used to dump EVERY folder in the org — hundreds of
// chapter folders across every subject, flat and unsorted by hierarchy —
// into one <select>, unusable at any real scale. Confirmed against legacy's
// actual "Move" UI (QrResources/moveToFolders.html): it's a lazy-loading
// tree, expand a folder to reveal its children on demand, not a flat list.
// This matches that: browse from root, drill into one folder at a time.
function MoveModal({
  resources,
  onClose,
  onMoved,
}: {
  resources: Resource[];
  onClose: () => void;
  onMoved: () => void;
}) {
  const movingIds = useMemo(() => new Set(resources.map((r) => r.id)), [resources]);
  const [path, setPath] = useState<MoveFolder[]>([]); // breadcrumb; [] = root
  const [children, setChildren] = useState<MoveFolder[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const currentFolderId = path.length ? path[path.length - 1].id : null;

  useEffect(() => {
    setLoading(true);
    const qs = currentFolderId
      ? `?type=FOLDER&parentId=${encodeURIComponent(currentFolderId)}`
      : "?type=FOLDER";
    fetch(`/api/cmds/content${qs}`)
      .then((r) => r.json())
      .then((d) => {
        const list: MoveFolder[] = (d.resources || [])
          .filter((r: any) => !movingIds.has(r.id))
          .map((r: any) => ({ id: r.id, name: r.title }));
        setChildren(list);
      })
      .catch(() => setChildren([]))
      .finally(() => setLoading(false));
  }, [currentFolderId, movingIds]);

  async function move() {
    setSaving(true);
    await Promise.all(
      resources.map((resource) =>
        fetch("/api/cmds/content", {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            id: resource.id,
            type: resource.type,
            action: "move",
            folderId: currentFolderId,
          }),
        })
      )
    );
    setSaving(false);
    onMoved();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">
          Move {resources.length === 1 ? `"${resources[0].title}"` : `${resources.length} items`}
        </h3>

        <div className="mt-3 flex flex-wrap items-center gap-1 text-xs text-slate-500">
          <button onClick={() => setPath([])} className="hover:text-blue-600">
            Institute Resources
          </button>
          {path.map((c, i) => (
            <span key={c.id} className="flex items-center gap-1">
              <span className="text-slate-300">›</span>
              <button onClick={() => setPath((p) => p.slice(0, i + 1))} className="hover:text-blue-600">
                {c.name}
              </button>
            </span>
          ))}
        </div>

        <div className="mt-2 max-h-64 overflow-y-auto rounded border border-slate-200">
          {loading ? (
            <div className="px-3 py-6 text-center text-xs text-slate-400">Loading…</div>
          ) : children.length === 0 ? (
            <div className="px-3 py-6 text-center text-xs text-slate-400">No sub-folders here.</div>
          ) : (
            children.map((f) => (
              <button
                key={f.id}
                onClick={() => setPath((p) => [...p, f])}
                className="flex w-full items-center justify-between border-b border-slate-50 px-3 py-2 text-left text-sm text-slate-700 last:border-0 hover:bg-slate-50"
              >
                📁 {f.name}
                <span className="text-slate-300">›</span>
              </button>
            ))
          )}
        </div>

        <p className="mt-2 text-xs text-slate-400">
          Moving into: <span className="font-medium text-slate-600">{path.length ? path[path.length - 1].name : "Institute Resources (root)"}</span>
        </p>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={move}
            disabled={saving}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {saving ? "Moving…" : "Move here"}
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
    BOOK: "Book",
  };
  return m[type] || type;
}

// "Add To Programs" bulk action — the legacy Institute Resources entry point
// into the Program+Center+Section assignment model. Cascading Program ->
// Center -> Section picker, then adds the selected resources to that section
// (folders are silently skipped — assignment only applies to content).
function AddToSectionModal({
  resources,
  onClose,
  onAdded,
}: {
  resources: Resource[];
  onClose: () => void;
  onAdded: () => void;
}) {
  const items = resources.filter((r) => r.type !== "FOLDER");
  const [programs, setPrograms] = useState<{ id: string; name: string }[]>([]);
  const [programId, setProgramId] = useState("");
  const [centers, setCenters] = useState<{ id: string; name: string }[]>([]);
  const [sections, setSections] = useState<{ id: string; name: string; centerId: string | null }[]>([]);
  const [centerId, setCenterId] = useState("");
  const [sectionId, setSectionId] = useState("");
  const [loadingProgram, setLoadingProgram] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch("/api/cmds/programs")
      .then((r) => r.json())
      .then((d) => setPrograms(d.programs || []))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!programId) {
      setCenters([]);
      setSections([]);
      setCenterId("");
      setSectionId("");
      return;
    }
    setLoadingProgram(true);
    fetch(`/api/cmds/programs/${programId}`)
      .then((r) => r.json())
      .then((d) => {
        setCenters(d.centers || []);
        setSections((d.sections || []).filter((s: any) => !s.programId || s.programId === programId));
      })
      .finally(() => setLoadingProgram(false));
  }, [programId]);

  useEffect(() => {
    setCenterId(centers[0]?.id || "");
  }, [centers]);

  const centerSections = useMemo(
    () => sections.filter((s) => !centerId || s.centerId === centerId),
    [sections, centerId]
  );

  useEffect(() => {
    setSectionId(centerSections[0]?.id || "");
  }, [centerSections]);

  async function confirm() {
    if (!sectionId || items.length === 0) return;
    setSaving(true);
    await fetch("/api/cmds/content/bulk", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        items: items.map((r) => ({ id: r.id, type: r.type })),
        action: "addToSection",
        sectionId,
      }),
    });
    setSaving(false);
    onAdded();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[420px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Add To Programs</h3>
        <p className="mt-1 text-sm text-slate-500">
          {items.length} item{items.length === 1 ? "" : "s"} selected
          {items.length < resources.length && " (folders skipped)"}
        </p>

        <label className="mt-4 block text-sm font-medium text-slate-600">Program</label>
        <select
          value={programId}
          onChange={(e) => setProgramId(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        >
          <option value="">Select a program…</option>
          {programs.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>

        {programId && (
          <>
            <label className="mt-4 block text-sm font-medium text-slate-600">Center</label>
            <select
              value={centerId}
              onChange={(e) => setCenterId(e.target.value)}
              disabled={loadingProgram || centers.length === 0}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
            >
              {centers.length === 0 && <option value="">No centers</option>}
              {centers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            <label className="mt-4 block text-sm font-medium text-slate-600">Section</label>
            <select
              value={sectionId}
              onChange={(e) => setSectionId(e.target.value)}
              disabled={loadingProgram || centerSections.length === 0}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
            >
              {centerSections.length === 0 && <option value="">No sections</option>}
              {centerSections.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </>
        )}

        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={confirm}
            disabled={saving || !sectionId || items.length === 0}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {saving ? "Adding…" : "Add"}
          </button>
        </div>
      </div>
    </div>
  );
}
