"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";

type Item = { id: string; title: string; body: string; imageUrl: string | null; at: number };

export default function StudentNewsPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/cmds/tools/news")
      .then((r) => r.json())
      .then((d) => setItems(d.items || []))
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="library">
      <h1 className="text-lg font-semibold text-slate-800">News & Announcements</h1>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No news right now.
        </div>
      ) : (
        <div className="mt-4 space-y-4">
          {items.map((n) => (
            <article key={n.id} className="overflow-hidden rounded-lg border border-slate-200">
              {n.imageUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={n.imageUrl} alt={n.title} className="h-44 w-full object-cover" />
              )}
              <div className="p-4">
                <div className="font-semibold text-slate-800">{n.title}</div>
                {n.body && <p className="mt-1 text-sm text-slate-600">{n.body}</p>}
                <div className="mt-2 text-xs text-slate-400">{new Date(n.at).toLocaleDateString()}</div>
              </div>
            </article>
          ))}
        </div>
      )}
    </LmsShell>
  );
}
