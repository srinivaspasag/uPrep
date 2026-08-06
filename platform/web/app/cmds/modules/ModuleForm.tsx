"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";
import BoardPicker from "@/components/BoardPicker";

type Resource = {
  id: string;
  title: string;
  type: string;
  subject: string | null;
};

type SessionGroup = { id: string; name: string; contentIds: string[] };

let sessionCounter = 0;
function newSessionId() {
  sessionCounter += 1;
  return `s${Date.now()}${sessionCounter}`;
}

const TYPE_ICON: Record<string, string> = {
  DOCUMENT: "📄",
  VIDEO: "🎬",
  TEST: "📕",
  QUESTION_SET: "🟦",
};

// Shared by /cmds/modules/new and /cmds/modules/[id]/edit — modules had a
// create form but no way to edit one afterward (item #1 follow-up: staff
// asked for an edit option on created modules). Same shape as
// QuestionForm's create/edit split.
export default function ModuleForm({ moduleId }: { moduleId?: string }) {
  const isEdit = !!moduleId;
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [boardIds, setBoardIds] = useState<string[]>([]);
  const [pool, setPool] = useState<Resource[]>([]);
  // Content is grouped into named sessions (Session 1, Session 2, ...)
  // instead of one flat list — a module commonly covers several class
  // sessions' worth of material, and staff wanted to organize picked
  // content that way rather than a single undifferentiated sequence.
  const [sessions, setSessions] = useState<SessionGroup[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string>("");
  const picked = useMemo(() => sessions.flatMap((s) => s.contentIds), [sessions]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [folderId, setFolderId] = useState<string | null>(null);
  const [folderName, setFolderName] = useState<string>("");

  // Where to return after create/cancel — back into the folder we came from.
  const backHref = folderId
    ? `/cmds?folder=${encodeURIComponent(folderId)}&folderName=${encodeURIComponent(folderName)}`
    : "/cmds";

  useEffect(() => {
    const s = getSession();
    setSession(s);
    if (!s) return;

    async function load() {
      let fid: string | null = null;
      let fname = "";

      if (isEdit) {
        // Prefill from the existing module — including its own folder, so
        // Cancel/Save both return to where it actually lives.
        const d = await fetch(`/api/cmds/modules/${moduleId}`).then((r) => r.json());
        if (d.module) {
          setName(d.module.name || "");
          setSubject(d.module.subject || "");
          setBoardIds(d.module.boardIds || []);
          const rawSessions: { name: string; contentIds: string[] }[] = d.module.sessions || [];
          if (rawSessions.length > 0) {
            const withIds = rawSessions.map((s) => ({ ...s, id: newSessionId() }));
            setSessions(withIds);
            setActiveSessionId(withIds[0].id);
          } else if ((d.module.contentIds || []).length > 0) {
            // Module saved before sessions existed — migrate its flat
            // contentIds into a single "Session 1" so nothing already in it
            // disappears from view.
            const id = newSessionId();
            setSessions([{ id, name: "Session 1", contentIds: d.module.contentIds }]);
            setActiveSessionId(id);
          }
          fid = d.module.folderId || null;
        }
      } else {
        // The current CMDS folder is passed via the URL (?folder=<id>&folderName=<name>)
        // — same convention as CmdsUploadForm — so the module lands inside the
        // folder the user was browsing instead of silently going unfiled.
        const sp = new URLSearchParams(window.location.search);
        fid = sp.get("folder");
        fname = sp.get("folderName") || "";
      }
      setFolderId(fid);
      setFolderName(fname);
      setLoading(false);
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [moduleId]);

  // Bug found live (first pass): this was scoped to just the folder the
  // module itself lives in, so content filed elsewhere was unreachable.
  // Fixed by pulling from the whole org flat (`all=1` + search) — but that
  // traded one problem for another: with hundreds of videos, a flat
  // "everything" list with no structure is exactly what a user then has to
  // fight through. Real fix: browse it the same way Institute Resources
  // itself is browsed (Subject folder → Chapter folder → its content) —
  // same drill-down/breadcrumb pattern already used by the "Move" modal
  // (app/cmds/page.tsx) — with search as an escape hatch for when you
  // already know the title and don't want to click through folders.
  const [search, setSearch] = useState("");
  const [browsePath, setBrowsePath] = useState<{ id: string; name: string }[]>([]);
  const [browseFolders, setBrowseFolders] = useState<{ id: string; title: string }[]>([]);
  const [browseLoading, setBrowseLoading] = useState(false);
  const browsing = !search.trim();
  const currentFolderId = browsePath.length ? browsePath[browsePath.length - 1].id : null;

  useEffect(() => {
    if (!browsing) return;
    setBrowseLoading(true);
    const qs = currentFolderId ? `?parentId=${encodeURIComponent(currentFolderId)}` : "";
    fetch(`/api/cmds/content${qs}`)
      .then((r) => r.json())
      .then((d) => {
        const resources: Resource[] = d.resources || [];
        setBrowseFolders(resources.filter((r) => r.type === "FOLDER").map((r) => ({ id: r.id, title: r.title })));
        setPool(resources.filter((r) => ["DOCUMENT", "VIDEO", "TEST", "QUESTION_SET"].includes(r.type)));
      })
      .catch(() => {
        setBrowseFolders([]);
        setPool([]);
      })
      .finally(() => setBrowseLoading(false));
  }, [browsing, currentFolderId]);

  useEffect(() => {
    if (browsing || !search.trim()) return;
    const t = setTimeout(() => {
      fetch(`/api/cmds/content?all=1&q=${encodeURIComponent(search.trim())}`)
        .then((r) => r.json())
        .then((d) => {
          const items: Resource[] = (d.resources || []).filter((r: Resource) =>
            ["DOCUMENT", "VIDEO", "TEST", "QUESTION_SET"].includes(r.type)
          );
          setPool(items);
        });
    }, 250);
    return () => clearTimeout(t);
  }, [browsing, search]);

  const byId = useMemo(() => new Map(pool.map((r) => [r.id, r])), [pool]);

  function addSession() {
    const id = newSessionId();
    setSessions((prev) => [...prev, { id, name: `Session ${prev.length + 1}`, contentIds: [] }]);
    setActiveSessionId(id);
    return id;
  }

  function removeSession(id: string) {
    setSessions((prev) => prev.filter((s) => s.id !== id));
    if (activeSessionId === id) setActiveSessionId((prev) => (prev === id ? "" : prev));
  }

  function renameSession(id: string, name: string) {
    setSessions((prev) => prev.map((s) => (s.id === id ? { ...s, name } : s)));
  }

  // Picking an item from Available content adds it to whichever session is
  // currently active — "when I highlight [an item] I need to add it to
  // session1 or session2..." — auto-creating Session 1 on the fly if none
  // exists yet, so there's no separate required setup step first.
  function addToActiveSession(itemId: string) {
    const targetId = activeSessionId || addSession();
    setSessions((prev) =>
      prev.map((s) => (s.id === targetId ? { ...s, contentIds: [...s.contentIds, itemId] } : s))
    );
  }

  function removeFromSession(sessionId: string, itemId: string) {
    setSessions((prev) =>
      prev.map((s) => (s.id === sessionId ? { ...s, contentIds: s.contentIds.filter((x) => x !== itemId) } : s))
    );
  }

  // Order is mandatory, not incidental — a module plays back in this exact
  // sequence within each session (see the "Up next" prompt on the module
  // viewer), so authoring needs an explicit, visible way to set it rather
  // than relying on whatever order items happened to get checked in.
  function moveInSession(sessionId: string, index: number, direction: -1 | 1) {
    setSessions((prev) =>
      prev.map((s) => {
        if (s.id !== sessionId) return s;
        const other = index + direction;
        if (other < 0 || other >= s.contentIds.length) return s;
        const next = [...s.contentIds];
        [next[index], next[other]] = [next[other], next[index]];
        return { ...s, contentIds: next };
      })
    );
  }

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Please enter a module name.");
    if (picked.length === 0) return setError("Pick at least one item for the module.");
    setSaving(true);
    try {
      // contentIds stays the flat, ordered concatenation of every session in
      // order — the module viewer's overall "up next" sequencing reads that
      // directly; sessions is the same items grouped for display.
      const sessionsPayload = sessions
        .filter((s) => s.contentIds.length > 0)
        .map((s) => ({ name: s.name, contentIds: s.contentIds }));
      const res = isEdit
        ? await fetch("/api/cmds/content", {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              id: moduleId,
              type: "MODULE",
              action: "edit-module",
              name: name.trim(),
              subject: subject.trim(),
              boardIds,
              contentIds: picked,
              sessions: sessionsPayload,
            }),
          })
        : await fetch("/api/cmds/content", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              kind: "module",
              name: name.trim(),
              subject: subject.trim(),
              boardIds,
              contentIds: picked,
              sessions: sessionsPayload,
              userId: session?.id,
              folderId: folderId || undefined,
            }),
          });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || `Failed to ${isEdit ? "save" : "create"} module`);
        return;
      }
      router.push(isEdit ? `/cmds/modules/${moduleId}` : backHref);
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell active="resources">
      <div className="mx-auto max-w-[760px] px-6 py-8">
        <div className="mb-4 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          {folderName && (
            <>
              / <span className="text-slate-600">{folderName}</span>{" "}
            </>
          )}
          / <span className="text-slate-600">{isEdit ? "Edit Module" : "Create a Module"}</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">{isEdit ? "Edit Module" : "Create a Module"}</h1>
        {!isEdit && folderName && (
          <p className="mt-1 text-sm text-slate-400">
            This module will be added to <span className="font-medium text-slate-600">{folderName}</span>.
          </p>
        )}

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600">Module name</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
            placeholder="e.g. Kinematics Basics"
          />
        </div>

        <div className="mt-4">
          <BoardPicker selected={boardIds} onChange={setBoardIds} onSubjectChange={setSubject} />
          {subject && (
            <p className="mt-1 text-xs text-slate-400">
              Subject: <span className="font-medium text-slate-600">{subject}</span> — derived from the chapter(s)
              tagged above.
            </p>
          )}
        </div>

        <div className="mt-6">
          <h2 className="mb-2 text-sm font-semibold text-slate-600">
            Order ({picked.length} selected across {sessions.length} session{sessions.length === 1 ? "" : "s"})
          </h2>
          <p className="mb-2 text-xs text-slate-400">
            Group content into sessions (Session 1, Session 2, ...) — students go through each
            session in this exact order, then move to the next session. Use the arrows to set the
            sequence within a session.
          </p>
          {sessions.length === 0 ? (
            <div className="rounded border border-dashed border-slate-200 py-6 text-center text-sm text-slate-400">
              No sessions yet — pick an item below to start one, or add one explicitly.
            </div>
          ) : (
            <div className="space-y-4">
              {sessions.map((s) => (
                <div key={s.id} className="rounded border border-slate-200">
                  <div className="flex items-center gap-2 border-b border-slate-100 bg-slate-50 px-3 py-2">
                    <input
                      value={s.name}
                      onChange={(e) => renameSession(s.id, e.target.value)}
                      className="flex-1 rounded border border-transparent bg-transparent px-1.5 py-0.5 text-sm font-semibold text-slate-700 hover:border-slate-300 focus:border-slate-400 focus:bg-white focus:outline-none"
                    />
                    <span className="text-xs text-slate-400">{s.contentIds.length} item{s.contentIds.length === 1 ? "" : "s"}</span>
                    <button
                      type="button"
                      onClick={() => removeSession(s.id)}
                      className="rounded px-1.5 py-0.5 text-xs text-slate-400 hover:bg-white hover:text-red-500"
                      title="Remove this session"
                    >
                      Remove session
                    </button>
                  </div>
                  <div className="space-y-1 p-2">
                    {s.contentIds.length === 0 ? (
                      <p className="py-3 text-center text-xs text-slate-400">
                        Nothing here yet — pick content below while this session is active.
                      </p>
                    ) : (
                      s.contentIds.map((id, i) => {
                        const r = byId.get(id);
                        return (
                          <div
                            key={id}
                            className="flex items-center gap-3 rounded bg-emerald-50 px-2 py-2 text-sm ring-1 ring-inset ring-emerald-200"
                          >
                            <span className="w-5 shrink-0 text-center text-xs font-semibold text-emerald-700">
                              {i + 1}
                            </span>
                            <span>{TYPE_ICON[r?.type || ""] || "📄"}</span>
                            <span className="flex-1 font-medium text-emerald-800">{r?.title || "(unknown item)"}</span>
                            <span className="text-xs text-slate-400">{r?.type}</span>
                            <button
                              type="button"
                              onClick={() => moveInSession(s.id, i, -1)}
                              disabled={i === 0}
                              className="rounded px-1.5 py-0.5 text-slate-500 hover:bg-white disabled:opacity-30"
                              title="Move up"
                            >
                              ↑
                            </button>
                            <button
                              type="button"
                              onClick={() => moveInSession(s.id, i, 1)}
                              disabled={i === s.contentIds.length - 1}
                              className="rounded px-1.5 py-0.5 text-slate-500 hover:bg-white disabled:opacity-30"
                              title="Move down"
                            >
                              ↓
                            </button>
                            <button
                              type="button"
                              onClick={() => removeFromSession(s.id, id)}
                              className="rounded px-1.5 py-0.5 text-slate-400 hover:bg-white hover:text-red-500"
                              title="Remove"
                            >
                              ✕
                            </button>
                          </div>
                        );
                      })
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
          <button
            type="button"
            onClick={addSession}
            className="mt-2 rounded border border-slate-300 px-3 py-1 text-xs font-medium text-slate-600 hover:bg-slate-50"
          >
            + Add Session
          </button>

          <div className="mt-5 flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-slate-600">Available content</h2>
              <span className="text-xs text-slate-400">
                {browsing ? "Click into a folder to find content" : "Searching everywhere in Institute Resources"}
              </span>
            </div>
            {sessions.length > 0 && (
              <label className="flex items-center gap-1.5 text-xs text-slate-500">
                Adding to
                <select
                  value={activeSessionId}
                  onChange={(e) => setActiveSessionId(e.target.value)}
                  className="rounded border border-slate-300 px-2 py-1 text-xs text-slate-700 outline-none focus:border-slate-500"
                >
                  {sessions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </div>
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by title…"
            className="mt-2 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />

          {browsing && (
            <div className="mt-2 flex flex-wrap items-center gap-1 text-xs text-slate-500">
              <button
                type="button"
                onClick={() => setBrowsePath([])}
                className={currentFolderId ? "hover:text-slate-800 hover:underline" : "font-medium text-slate-800"}
              >
                Institute Resources
              </button>
              {browsePath.map((c, i) => (
                <span key={c.id} className="flex items-center gap-1">
                  <span className="text-slate-300">/</span>
                  <button
                    type="button"
                    onClick={() => setBrowsePath(browsePath.slice(0, i + 1))}
                    className={i === browsePath.length - 1 ? "font-medium text-slate-800" : "hover:text-slate-800 hover:underline"}
                  >
                    {c.name}
                  </button>
                </span>
              ))}
            </div>
          )}

          {loading || browseLoading ? (
            <div className="py-8 text-center text-slate-400">Loading content…</div>
          ) : browsing && browseFolders.length === 0 && pool.length === 0 ? (
            <div className="mt-2 rounded border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
              This folder is empty.
            </div>
          ) : !browsing && pool.length === 0 ? (
            <div className="mt-2 rounded border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
              No content matches that search.
            </div>
          ) : (
            <div className="mt-2 max-h-[280px] space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
              {browsing &&
                browseFolders.map((f) => (
                  <button
                    key={f.id}
                    type="button"
                    onClick={() => setBrowsePath([...browsePath, { id: f.id, name: f.title }])}
                    className="flex w-full items-center gap-3 rounded px-2 py-2 text-left text-sm hover:bg-slate-50"
                  >
                    <span>📁</span>
                    <span className="flex-1 font-medium text-slate-700">{f.title}</span>
                    <span className="text-slate-400">›</span>
                  </button>
                ))}
              {pool
                .filter((r) => !picked.includes(r.id))
                .map((r) => (
                  <label
                    key={r.id}
                    className="flex cursor-pointer items-center gap-3 rounded px-2 py-2 text-sm hover:bg-slate-50"
                  >
                    <input
                      type="checkbox"
                      checked={false}
                      onChange={() => addToActiveSession(r.id)}
                      className="accent-emerald-600"
                    />
                    <span>{TYPE_ICON[r.type] || "📄"}</span>
                    <span className="flex-1 text-slate-700">{r.title}</span>
                    <span className="text-xs text-slate-400">{r.type}</span>
                  </label>
                ))}
              {!browsing && pool.length > 0 && pool.every((r) => picked.includes(r.id)) && (
                <p className="py-4 text-center text-xs text-slate-400">Everything's been added.</p>
              )}
            </div>
          )}
        </div>

        {error && <div className="mt-4 text-sm text-red-600">{error}</div>}

        <div className="mt-6 flex gap-3">
          <button
            onClick={submit}
            disabled={saving}
            className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? (isEdit ? "Saving…" : "Creating…") : isEdit ? "Save Changes" : "Create Module"}
          </button>
          <Link
            href={isEdit ? `/cmds/modules/${moduleId}` : backHref}
            className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100"
          >
            Cancel
          </Link>
        </div>
      </div>
    </CmdsShell>
  );
}
