"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Item = { id: string; title: string; body: string; imageUrl: string | null; at: number };

export default function NewsAdminPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [imageUrl, setImageUrl] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/news").then((r) => r.json());
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function post() {
    if (!title.trim()) return;
    await fetch("/api/cmds/tools/news", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title, body, imageUrl }),
    });
    setTitle("");
    setBody("");
    setImageUrl("");
    load();
  }

  async function remove(id: string) {
    await fetch(`/api/cmds/tools/news?id=${id}`, { method: "DELETE" });
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[800px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">News Feed</h1>
        <p className="mt-1 text-sm text-slate-500">Publish announcements to the student news feed.</p>

        <div className="mt-5 rounded border border-slate-200 p-4">
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Headline" className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
          <textarea value={body} onChange={(e) => setBody(e.target.value)} rows={3} placeholder="Story…" className="mt-2 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
          <input value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="Image URL (optional)" className="mt-2 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500" />
          <button onClick={post} className="mt-3 rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700">
            Publish
          </button>
        </div>

        {loading ? (
          <div className="py-10 text-center text-slate-400">Loading…</div>
        ) : (
          <div className="mt-6 space-y-3">
            {items.map((n) => (
              <div key={n.id} className="flex items-start justify-between rounded border border-slate-200 p-4">
                <div>
                  <div className="font-medium text-slate-800">{n.title}</div>
                  {n.body && <div className="mt-1 text-sm text-slate-500">{n.body}</div>}
                  <div className="mt-1 text-xs text-slate-400">{new Date(n.at).toLocaleString()}</div>
                </div>
                <button onClick={() => remove(n.id)} className="text-xs text-red-500 hover:underline">Delete</button>
              </div>
            ))}
          </div>
        )}
      </div>
    </CmdsShell>
  );
}
