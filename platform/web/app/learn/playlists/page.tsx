"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Item = { entityId: string; name: string; type: string; url?: string | null };
type Playlist = { id: string; name: string; description: string; items: Item[]; userName: string };
type LibItem = { id: string; name: string; type: string; url?: string | null };

export default function PlaylistsPage() {
  const [lists, setLists] = useState<Playlist[]>([]);
  const [library, setLibrary] = useState<LibItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [addTo, setAddTo] = useState<string | null>(null);

  async function load() {
    const [pl, lib] = await Promise.all([
      fetch("/api/learn/playlists").then((r) => r.json()),
      fetch("/api/library").then((r) => r.json()),
    ]);
    setLists(pl.items || []);
    setLibrary((lib.items || []).filter((i: any) => i.type === "VIDEO" || i.type === "DOCUMENT"));
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function create() {
    if (!name.trim()) return;
    const s = getSession();
    await fetch("/api/learn/playlists", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: s?.id,
        userName: [s?.firstName, s?.lastName].filter(Boolean).join(" ") || "Student",
        name: name.trim(),
        description: description.trim(),
      }),
    });
    setCreating(false);
    setName("");
    setDescription("");
    load();
  }

  async function addItem(playlistId: string, item: LibItem) {
    await fetch("/api/learn/playlists", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: playlistId,
        addItem: { entityId: item.id, name: item.name, type: item.type, url: item.url ?? null },
      }),
    });
    setAddTo(null);
    load();
  }

  return (
    <LmsShell active="playlists">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">Playlists</h1>
        <button
          onClick={() => setCreating((c) => !c)}
          className="rounded-md bg-[#e8443b] px-4 py-1.5 text-sm font-semibold text-white hover:bg-[#d33c34]"
        >
          + New Playlist
        </button>
      </div>

      {creating && (
        <div className="mt-4 rounded-lg border border-slate-200 p-4">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Playlist name"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
          />
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description (optional)"
            className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
          />
          <button
            onClick={create}
            className="mt-2 rounded-md bg-emerald-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-emerald-700"
          >
            Create
          </button>
        </div>
      )}

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : lists.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No playlists yet. Create one to organize videos and documents.
        </div>
      ) : (
        <div className="mt-4 space-y-4">
          {lists.map((p) => (
            <div key={p.id} className="rounded-lg border border-slate-200 p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium text-slate-800">{p.name}</div>
                  {p.description && <div className="text-sm text-slate-500">{p.description}</div>}
                </div>
                <button
                  onClick={() => setAddTo(addTo === p.id ? null : p.id)}
                  className="text-sm font-medium text-emerald-600 hover:underline"
                >
                  + Add content
                </button>
              </div>

              {addTo === p.id && (
                <div className="mt-3 max-h-48 overflow-y-auto rounded-md border border-slate-100 bg-slate-50 p-2">
                  {library.length === 0 ? (
                    <div className="p-2 text-sm text-slate-400">No videos/documents available.</div>
                  ) : (
                    library.map((l) => (
                      <button
                        key={l.id}
                        onClick={() => addItem(p.id, l)}
                        className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-left text-sm text-slate-600 hover:bg-white"
                      >
                        <span className="text-xs text-slate-400">{l.type}</span>
                        {l.name}
                      </button>
                    ))
                  )}
                </div>
              )}

              <ul className="mt-3 space-y-1">
                {(p.items || []).length === 0 ? (
                  <li className="text-sm text-slate-400">Empty — add some content.</li>
                ) : (
                  p.items.map((it) => (
                    <li key={it.entityId} className="flex items-center gap-2 text-sm text-slate-700">
                      <span className="text-xs text-slate-400">{it.type}</span>
                      {it.url ? (
                        <a href={it.url} target="_blank" rel="noreferrer" className="hover:text-blue-600">
                          {it.name}
                        </a>
                      ) : (
                        it.name
                      )}
                    </li>
                  ))
                )}
              </ul>
            </div>
          ))}
        </div>
      )}
    </LmsShell>
  );
}
