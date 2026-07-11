"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Bookmark = { id: string; entityId: string; entityType: string; name: string; url: string | null };

const BADGE: Record<string, string> = {
  TEST: "bg-emerald-100 text-emerald-700",
  MODULE: "bg-indigo-100 text-indigo-700",
  DOCUMENT: "bg-amber-100 text-amber-700",
  VIDEO: "bg-rose-100 text-rose-700",
};

function hrefFor(b: Bookmark): string {
  if (b.entityType === "TEST") return `/test/${b.entityId}`;
  if (b.url) return b.url;
  return "/learn/library";
}

export default function BookmarksPage() {
  const [items, setItems] = useState<Bookmark[]>([]);
  const [loading, setLoading] = useState(true);

  async function load() {
    const uid = getSession()?.id || "";
    const res = await fetch(`/api/learn/bookmarks?userId=${encodeURIComponent(uid)}`);
    const d = await res.json();
    setItems(d.items || []);
    setLoading(false);
  }

  useEffect(() => {
    load();
  }, []);

  async function remove(b: Bookmark) {
    await fetch("/api/learn/bookmarks", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: getSession()?.id, entityId: b.entityId }),
    });
    load();
  }

  return (
    <LmsShell active="library">
      <h1 className="text-lg font-semibold text-slate-800">Bookmarks</h1>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No bookmarks yet. Tap the ☆ on any content to save it here.
        </div>
      ) : (
        <ul className="mt-4 divide-y divide-slate-100">
          {items.map((b) => (
            <li key={b.id} className="flex items-center gap-3 py-3">
              <span
                className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                  BADGE[b.entityType] || "bg-slate-100 text-slate-600"
                }`}
              >
                {b.entityType}
              </span>
              <Link
                href={hrefFor(b)}
                target={b.url && b.entityType !== "TEST" ? "_blank" : undefined}
                className="flex-1 text-slate-700 hover:text-blue-600"
              >
                {b.name}
              </Link>
              <button
                onClick={() => remove(b)}
                className="text-sm text-slate-400 hover:text-red-500"
                title="Remove bookmark"
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
    </LmsShell>
  );
}
