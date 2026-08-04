"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { getSession } from "@/lib/session";
import { ZeroState } from "@/components/LmsShell";

type LibraryItem = {
  id: string;
  name: string;
  type: string;
  questionCount: number;
  durationMin: number;
  totalMarks: number;
  difficulty: string | null;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
};

type TabKey = "recent" | "modules" | "tests" | "documents" | "videos";

const TABS: { key: TabKey; label: string }[] = [
  { key: "recent", label: "Recently Added" },
  { key: "modules", label: "Modules" },
  { key: "tests", label: "Tests" },
  { key: "documents", label: "Documents" },
  { key: "videos", label: "Videos" },
];

// The flat, cross-subject, by-type content browser shown at the bottom of
// /learn/courses (the real "Digital Library" page — see that file). This is
// NOT legacy's Library screen (that's the subject/chapter card tree above
// it); it's a rebuild-only fallback so content with no subject/chapter tag
// at all — e.g. added straight to a section via Programs > Content > Make
// Visible — still has somewhere to surface, since the tag-driven tree can
// never reach it.
export default function LibrarySection() {
  const [items, setItems] = useState<LibraryItem[]>([]);
  const [tab, setTab] = useState<TabKey>("recent");
  const [loading, setLoading] = useState(true);
  const [bookmarks, setBookmarks] = useState<Set<string>>(new Set());
  // Bug found live: every video card used to render a real <video controls>
  // element inline, all at once — with ~100 videos that's ~100 simultaneous
  // players loading metadata, which is exactly what made this slow and
  // "tough to scroll". Cards are now lightweight thumbnails; clicking one
  // opens the real player here instead.
  const [playing, setPlaying] = useState<LibraryItem | null>(null);

  useEffect(() => {
    const uid = getSession()?.id || "";
    Promise.all([
      fetch("/api/library?onlyLoose=1").then((r) => r.json()),
      fetch(`/api/learn/bookmarks?userId=${encodeURIComponent(uid)}`).then((r) => r.json()),
    ])
      .then(([lib, bm]) => {
        setItems(lib.items || []);
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
    return items; // recent
  }, [tab, tests, modules, documents, videos, items]);

  if (loading) return <div className="py-10 text-center text-[#8890A1]">Loading…</div>;
  if (items.length === 0) return null; // nothing to show — don't render an empty section at all

  return (
    <div>
      <div className="flex flex-wrap gap-x-5 gap-y-2 border-b border-[#D9D6C9] pb-2 text-sm">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`-mb-[9px] border-b-2 pb-2 ${
              tab === t.key
                ? "border-amber-600 font-semibold text-[#16233D]"
                : "border-transparent text-[#8890A1] hover:text-[#16233D]"
            }`}
          >
            {t.label}
            {t.key === "tests" && tests.length > 0 && <span className="ml-1 text-[#8890A1]">({tests.length})</span>}
            {t.key === "modules" && modules.length > 0 && (
              <span className="ml-1 text-[#8890A1]">({modules.length})</span>
            )}
            {t.key === "documents" && documents.length > 0 && (
              <span className="ml-1 text-[#8890A1]">({documents.length})</span>
            )}
            {t.key === "videos" && videos.length > 0 && (
              <span className="ml-1 text-[#8890A1]">({videos.length})</span>
            )}
          </button>
        ))}
      </div>

      <div className="mt-5">
        {visible.length === 0 ? (
          <ZeroState img="/legacy/zero/general-no-content.jpg" />
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {visible.map((it) => (
              <ContentCard
                key={it.id}
                item={it}
                bookmarked={bookmarks.has(it.id)}
                onToggleBookmark={() => toggleBookmark(it)}
                onPlay={() => setPlaying(it)}
              />
            ))}
          </div>
        )}
      </div>

      {playing && <VideoLightbox item={playing} onClose={() => setPlaying(null)} />}
    </div>
  );
}

function VideoLightbox({ item, onClose }: { item: LibraryItem; onClose: () => void }) {
  const mediaRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  // Double-click-to-fullscreen on a <video> is a browser default, not
  // something we control, and it's flaky on Windows/some browsers — an
  // explicit button using the standard Fullscreen API works everywhere.
  function enlarge() {
    const el = mediaRef.current as any;
    if (!el) return;
    const request = el.requestFullscreen || el.webkitRequestFullscreen || el.msRequestFullscreen;
    request?.call(el);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4" onClick={onClose}>
      <div className="w-full max-w-3xl" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between pb-2">
          <span className="truncate pr-4 text-sm font-medium text-white">{item.name}</span>
          <div className="flex shrink-0 items-center gap-3">
            <button
              onClick={enlarge}
              className="flex items-center gap-1 rounded border border-white/30 px-2 py-1 text-xs text-white hover:bg-white/10"
              title="Enlarge (fullscreen)"
            >
              ⛶ Enlarge
            </button>
            <button onClick={onClose} className="text-2xl leading-none text-white hover:text-slate-300">
              ×
            </button>
          </div>
        </div>
        <div ref={mediaRef} className="aspect-video w-full overflow-hidden rounded bg-black">
          {item.embedUrl ? (
            <iframe
              src={item.embedUrl}
              className="h-full w-full"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
              title={item.name}
            />
          ) : item.url ? (
            <video
              src={item.url}
              controls
              autoPlay
              controlsList="nodownload"
              disablePictureInPicture
              onContextMenu={(e) => e.preventDefault()}
              className="h-full w-full"
            />
          ) : null}
        </div>
      </div>
    </div>
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
  onPlay,
}: {
  item: LibraryItem;
  bookmarked: boolean;
  onToggleBookmark: () => void;
  onPlay: () => void;
}) {
  const isVideo = item.type === "VIDEO" && (item.embedUrl || item.url);

  const header = (
    <div className="flex items-start justify-between">
      <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${BADGE[item.type] || "bg-slate-100 text-slate-600"}`}>
        {item.type}
      </span>
      <button
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          onToggleBookmark();
        }}
        className={`text-lg leading-none ${bookmarked ? "text-amber-500" : "text-slate-300 hover:text-amber-400"}`}
        title={bookmarked ? "Remove bookmark" : "Bookmark"}
      >
        {bookmarked ? "★" : "☆"}
      </button>
    </div>
  );

  // Videos render as a lightweight clickable thumbnail (no media loaded
  // until opened) rather than a card with prose text — with 100+ videos in
  // this list, a plain play-button tile scans and scrolls far faster than a
  // full inline player per card. No "VIDEO" text badge here — the play
  // button already says that; it was pure redundant clutter.
  if (isVideo) {
    return (
      <button
        onClick={onPlay}
        className="overflow-hidden rounded-lg border border-[#D9D6C9] bg-white text-left transition hover:shadow-md"
      >
        <div className="relative aspect-video w-full overflow-hidden bg-[#16233D]">
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onToggleBookmark();
            }}
            className={`absolute right-2 top-2 text-lg leading-none ${bookmarked ? "text-amber-400" : "text-white/60 hover:text-amber-300"}`}
            title={bookmarked ? "Remove bookmark" : "Bookmark"}
          >
            {bookmarked ? "★" : "☆"}
          </button>
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="flex h-10 w-10 items-center justify-center rounded-full bg-white/90 text-[#16233D] shadow">▶</span>
          </div>
        </div>
        <div className="p-3 text-sm font-medium text-[#16233D]">{item.name}</div>
      </button>
    );
  }

  const inner = (
    <div className="rounded-lg border border-[#D9D6C9] bg-white p-4 transition hover:shadow-md">
      {header}
      <div className="mt-2 font-medium text-[#16233D]">{item.name}</div>

      {item.type === "TEST" && (
        <div className="mt-2 flex gap-3 text-xs text-[#8890A1]">
          <span>❓ {item.questionCount} q</span>
          <span>⏱ {item.durationMin} min</span>
          <span>🎯 {item.totalMarks}</span>
        </div>
      )}
      {item.type === "MODULE" && <div className="mt-2 text-xs text-blue-600">Open module ↗</div>}
      {item.type === "DOCUMENT" && <div className="mt-2 text-xs text-blue-600">Open document ↗</div>}
    </div>
  );

  // Bug found live: a module card here rendered with no link at all — the
  // ONLY module detail route in the whole app was staff-only
  // (/cmds/modules/[id], gated on canManageContent both on the page and its
  // API), so an assigned module was completely unopenable by a student. See
  // app/learn/module/[id]/page.tsx + app/api/learn/modules/[id]/route.ts.
  if (item.type === "MODULE") return <Link href={`/learn/module/${item.id}`}>{inner}</Link>;
  if (item.type === "TEST") return <Link href={`/test/${item.id}`}>{inner}</Link>;
  if (item.type === "DOCUMENT" && item.url)
    return (
      <a href={item.url} target="_blank" rel="noreferrer">
        {inner}
      </a>
    );
  return inner;
}
