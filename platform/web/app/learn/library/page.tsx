"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type LibraryItem = {
  id: string;
  name: string;
  type: string;
  questionCount: number;
  durationMin: number;
  totalMarks: number;
  difficulty: string | null;
  url?: string | null;
};
type Program = { id: string; name: string; code: string | null };

type TabKey =
  | "recent"
  | "modules"
  | "tests"
  | "documents"
  | "videos"
  | "assignments"
  | "files"
  | "questions";

const TABS: { key: TabKey; label: string }[] = [
  { key: "recent", label: "Recently Added" },
  { key: "modules", label: "Modules" },
  { key: "tests", label: "Tests" },
  { key: "documents", label: "Documents" },
  { key: "videos", label: "Videos" },
  { key: "assignments", label: "Assignments" },
  { key: "files", label: "Files" },
  { key: "questions", label: "Questions" },
];

export default function LibraryPage() {
  const [items, setItems] = useState<LibraryItem[]>([]);
  const [programs, setPrograms] = useState<Program[]>([]);
  const [tab, setTab] = useState<TabKey>("recent");
  const [loading, setLoading] = useState(true);
  const [bookmarks, setBookmarks] = useState<Set<string>>(new Set());

  useEffect(() => {
    const uid = getSession()?.id || "";
    Promise.all([
      fetch("/api/library").then((r) => r.json()),
      fetch("/api/programs").then((r) => r.json()),
      fetch(`/api/learn/bookmarks?userId=${encodeURIComponent(uid)}`).then((r) => r.json()),
    ])
      .then(([lib, prog, bm]) => {
        setItems(lib.items || []);
        setPrograms(prog.programs || []);
        setBookmarks(new Set((bm.items || []).map((b: any) => b.entityId)));
      })
      .finally(() => setLoading(false));
  }, []);

  async function toggleBookmark(it: LibraryItem) {
    const uid = getSession()?.id || "";
    if (!uid) return;
    setBookmarks((prev) => {
      const next = new Set(prev);
      if (next.has(it.id)) next.delete(it.id);
      else next.add(it.id);
      return next;
    });
    await fetch("/api/learn/bookmarks", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: uid,
        entityId: it.id,
        entityType: it.type,
        name: it.name,
        url: it.url ?? null,
      }),
    });
  }

  const tests = useMemo(() => items.filter((i) => i.type === "TEST"), [items]);
  const modules = useMemo(() => items.filter((i) => i.type === "MODULE"), [items]);
  const documents = useMemo(() => items.filter((i) => i.type === "DOCUMENT"), [items]);
  const videos = useMemo(() => items.filter((i) => i.type === "VIDEO"), [items]);

  const visible = useMemo(() => {
    if (tab === "tests") return tests;
    if (tab === "modules") return modules;
    if (tab === "documents") return documents;
    if (tab === "videos") return videos;
    if (tab === "recent") return items;
    return [] as LibraryItem[]; // assignments/files/questions: not seeded
  }, [tab, tests, modules, documents, videos, items]);

  const activeProgram = programs[0];

  return (
    <LmsShell active="library">
      {/* Program selector — mirrors legacy JEE Main / Center / Batch rows */}
      <div className="mb-6 max-w-[560px]">
        <div className="flex items-center justify-between rounded-sm bg-[#e8443b] px-4 py-1.5 text-sm font-semibold text-white">
          <span>{activeProgram?.name || "JEE Main 2026"}</span>
          <span>▾</span>
        </div>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-1.5 text-sm text-slate-600">
          <span>Main Center</span>
          <span className="text-slate-400">▾</span>
        </div>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-1.5 text-sm text-slate-600">
          <span>Batch A</span>
          <span className="text-slate-400">▾</span>
        </div>
      </div>

      <div className="flex gap-8">
        {/* Subjects panel */}
        <div className="w-[150px] shrink-0">
          <h2 className="text-lg font-semibold text-slate-800">Subjects</h2>
          <p className="mt-1 text-xs text-slate-400">Select a subject to view contents</p>
          <div className="mt-3 space-y-1 text-sm">
            <div className="cursor-pointer rounded bg-emerald-50 px-2 py-1 font-medium text-emerald-700">
              Recently Added
            </div>
            <div className="cursor-pointer rounded px-2 py-1 text-slate-500 hover:bg-slate-50">
              Physics
            </div>
          </div>
        </div>

        {/* Content area */}
        <div className="flex-1">
          <div className="flex flex-wrap gap-x-5 gap-y-2 border-b border-slate-200 pb-2 text-sm">
            {TABS.map((t) => (
              <button
                key={t.key}
                onClick={() => setTab(t.key)}
                className={`-mb-[9px] border-b-2 pb-2 ${
                  tab === t.key
                    ? "border-emerald-500 font-semibold text-slate-800"
                    : "border-transparent text-slate-500 hover:text-slate-700"
                }`}
              >
                {t.label}
                {t.key === "tests" && tests.length > 0 && (
                  <span className="ml-1 text-slate-400">({tests.length})</span>
                )}
                {t.key === "modules" && modules.length > 0 && (
                  <span className="ml-1 text-slate-400">({modules.length})</span>
                )}
                {t.key === "documents" && documents.length > 0 && (
                  <span className="ml-1 text-slate-400">({documents.length})</span>
                )}
                {t.key === "videos" && videos.length > 0 && (
                  <span className="ml-1 text-slate-400">({videos.length})</span>
                )}
              </button>
            ))}
          </div>

          <div className="mt-6">
            {loading ? (
              <div className="py-16 text-center text-slate-400">Loading…</div>
            ) : visible.length === 0 ? (
              <ZeroState img="/legacy/zero/general-no-content.jpg" />
            ) : (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {visible.map((it) => (
                  <ContentCard
                    key={it.id}
                    item={it}
                    bookmarked={bookmarks.has(it.id)}
                    onToggleBookmark={() => toggleBookmark(it)}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </LmsShell>
  );
}

const BADGE: Record<string, string> = {
  TEST: "bg-emerald-100 text-emerald-700",
  MODULE: "bg-indigo-100 text-indigo-700",
  DOCUMENT: "bg-amber-100 text-amber-700",
  VIDEO: "bg-rose-100 text-rose-700",
};

function ContentCard({
  item,
  bookmarked,
  onToggleBookmark,
}: {
  item: LibraryItem;
  bookmarked: boolean;
  onToggleBookmark: () => void;
}) {
  const inner = (
    <div className="rounded-lg border border-slate-200 bg-white p-4 transition hover:shadow-md">
      <div className="flex items-start justify-between">
        <span
          className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
            BADGE[item.type] || "bg-slate-100 text-slate-600"
          }`}
        >
          {item.type}
        </span>
        <button
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onToggleBookmark();
          }}
          className={`text-lg leading-none ${
            bookmarked ? "text-amber-500" : "text-slate-300 hover:text-amber-400"
          }`}
          title={bookmarked ? "Remove bookmark" : "Bookmark"}
        >
          {bookmarked ? "★" : "☆"}
        </button>
      </div>
      <div className="mt-2 font-medium text-slate-800">{item.name}</div>

      {item.type === "TEST" && (
        <div className="mt-2 flex gap-3 text-xs text-slate-500">
          <span>❓ {item.questionCount} q</span>
          <span>⏱ {item.durationMin} min</span>
          <span>🎯 {item.totalMarks}</span>
        </div>
      )}
      {item.type === "MODULE" && <div className="mt-2 text-xs text-slate-500">Module</div>}
      {item.type === "VIDEO" && item.url && (
        <video src={item.url} controls className="mt-2 w-full rounded" />
      )}
      {item.type === "DOCUMENT" && (
        <div className="mt-2 text-xs text-blue-600">Open document ↗</div>
      )}
    </div>
  );

  if (item.type === "TEST") return <Link href={`/test/${item.id}`}>{inner}</Link>;
  if (item.type === "DOCUMENT" && item.url)
    return (
      <a href={item.url} target="_blank" rel="noreferrer">
        {inner}
      </a>
    );
  return inner;
}
