"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { subjectAccent } from "@/lib/subjectColors";

type Course = {
  id: string;
  name: string;
  chapterCount: number;
  folderCount: number;
  videoCount: number;
  documentCount: number;
  bookCount: number;
  testCount: number;
};
type ProgramGroup = {
  id: string;
  name: string;
  courseIds: string[];
  centerName: string | null;
  sectionName: string | null;
};
type Sub = {
  id: string;
  name: string;
  type: "FOLDER";
  videoCount: number;
  documentCount: number;
  bookCount: number;
  testCount: number;
};
type Item = {
  id: string;
  name: string;
  type: string;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
};
type Crumb = { id: string; name: string };

export default function MyCoursesPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [programGroups, setProgramGroups] = useState<ProgramGroup[]>([]);
  const [staff, setStaff] = useState(false);
  const [loading, setLoading] = useState(true);
  const [path, setPath] = useState<Crumb[]>([]); // empty = course list
  const [subfolders, setSubfolders] = useState<Sub[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [browsing, setBrowsing] = useState(false);

  const [code, setCode] = useState("");
  const [joining, setJoining] = useState(false);
  const [joinMsg, setJoinMsg] = useState("");

  const loadCourses = useCallback(() => {
    return fetch("/api/learn/courses")
      .then((r) => r.json())
      .then((d) => {
        setCourses(d.courses || []);
        setProgramGroups(d.programGroups || []);
        setStaff(!!d.staff);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadCourses();
  }, [loadCourses]);

  async function joinWithCode() {
    setJoinMsg("");
    if (!code.trim()) return;
    setJoining(true);
    try {
      const res = await fetch("/api/learn/enroll-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: code.trim() }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setJoinMsg(d.error || "Could not redeem code");
        return;
      }
      setJoinMsg(`Joined ${d.section?.name || "section"}. ${d.addedCourses || 0} course(s) added.`);
      setCode("");
      await loadCourses();
    } finally {
      setJoining(false);
    }
  }

  // Pure data-fetch for a given breadcrumb path — no history/URL side
  // effects, so it's safe to call both from clicks and from the
  // popstate/initial-load handler below without double-pushing history.
  const fetchFolder = useCallback(async (crumbs: Crumb[]) => {
    const target = crumbs[crumbs.length - 1];
    if (!target) {
      setPath([]);
      setSubfolders([]);
      setItems([]);
      return;
    }
    setBrowsing(true);
    try {
      const d = await fetch(`/api/learn/courses?folderId=${encodeURIComponent(target.id)}`).then((r) =>
        r.json()
      );
      setPath(crumbs);
      setSubfolders(d.subfolders || []);
      setItems(d.items || []);
    } finally {
      setBrowsing(false);
    }
  }, []);

  // Restore whichever folder the URL points to on first load (so a
  // refresh or bookmark lands in the right place too), and react to the
  // browser's Back/Forward buttons via popstate. This is what was
  // missing before — clicking through folders never touched browser
  // history, so Back had nothing to step back to and just reloaded the
  // page straight to the subject list instead of the previous folder.
  useEffect(() => {
    function crumbsFromLocation(): Crumb[] {
      const raw = new URLSearchParams(window.location.search).get("path");
      if (!raw) return [];
      try {
        return JSON.parse(raw);
      } catch {
        return [];
      }
    }

    fetchFolder(crumbsFromLocation());

    function onPopState(e: PopStateEvent) {
      fetchFolder((e.state && e.state.crumbs) || crumbsFromLocation());
    }
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, [fetchFolder]);

  // Every folder click pushes a real browser history entry (via the URL's
  // ?path= param), so the Back button now actually steps up one level
  // instead of leaving the page or resetting to the subject list.
  const openFolder = useCallback(
    (crumbs: Crumb[]) => {
      const url =
        crumbs.length === 0
          ? "/learn/courses"
          : `/learn/courses?path=${encodeURIComponent(JSON.stringify(crumbs))}`;
      window.history.pushState({ crumbs }, "", url);
      fetchFolder(crumbs);
    },
    [fetchFolder]
  );

  const atRoot = path.length === 0;

  return (
    <LmsShell active="courses">
      {/* Hero — a warm, multi-hue moment (small dots echoing the subject
          palette below) instead of a plain ruled header, so the page feels
          like the start of something rather than a document title. */}
      <div className="relative overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white p-6 sm:p-8">
        <div className="pointer-events-none absolute -right-10 -top-16 h-52 w-52 rounded-full bg-gradient-to-br from-blue-100 to-violet-100 opacity-70" />
        <div className="pointer-events-none absolute right-16 bottom-0 h-16 w-16 rounded-full bg-emerald-50" />
        <div className="pointer-events-none absolute right-0 top-1/2 h-10 w-10 -translate-y-1/2 rounded-full bg-orange-50" />
        <div className="relative flex flex-wrap items-end justify-between gap-3">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-[#EDEEE9] px-3 py-1 text-xs font-medium uppercase tracking-wide text-[#8890A1]">
              📚 Digital Library
            </span>
            <h1 className="mt-3 font-serif text-2xl font-semibold text-[#16233D] sm:text-3xl">
              Your subjects, ready to explore
            </h1>
            <p className="mt-1.5 max-w-md text-sm text-[#3E4A63]">
              Pick a subject to dive into chapters, videos, e-books and tests.
            </p>
          </div>
          {staff && (
            <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs text-indigo-600">
              Staff preview — showing all courses
            </span>
          )}
        </div>
      </div>

      {atRoot && !staff && (
        <div className="mt-5 flex flex-wrap items-center gap-2 rounded-lg border border-dashed border-[#D9D6C9] bg-white p-3">
          <span className="text-sm text-[#3E4A63]">Have an access code?</span>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            onKeyDown={(e) => e.key === "Enter" && joinWithCode()}
            placeholder="ENTER CODE"
            className="w-40 rounded border border-[#D9D6C9] px-3 py-1.5 font-mono text-sm tracking-widest outline-none focus:border-amber-500"
          />
          <button
            onClick={joinWithCode}
            disabled={joining}
            className="rounded bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {joining ? "Joining…" : "Join with code"}
          </button>
          {joinMsg && <span className="text-xs text-[#3E4A63]">{joinMsg}</span>}
        </div>
      )}

      {/* Breadcrumbs */}
      {!atRoot && (
        <div className="mt-4 flex flex-wrap items-center gap-1 text-sm text-[#3E4A63]">
          <button onClick={() => openFolder([])} className="hover:text-amber-700">
            Digital Library
          </button>
          {path.map((c, i) => (
            <span key={c.id} className="flex items-center gap-1">
              <span className="text-[#D9D6C9]">/</span>
              <button
                onClick={() => openFolder(path.slice(0, i + 1))}
                className={i === path.length - 1 ? "font-medium text-[#16233D]" : "hover:text-amber-700"}
              >
                {c.name}
              </button>
            </span>
          ))}
        </div>
      )}

      <div className="mt-6">
        {loading ? (
          <div className="py-16 text-center text-[#8890A1]">Loading…</div>
        ) : atRoot ? (
          programGroups.length === 0 ? (
            <ZeroState icon="📚" title="No subjects yet">
              You're not assigned to a program yet. Ask your institute to assign one.
            </ZeroState>
          ) : (
            // Courses only ever show grouped under the Program that granted
            // them — there's no "ungrouped"/"Other Courses" concept, a course
            // with no Program isn't shown here at all.
            <div className="space-y-10">
              {programGroups.map((g) => {
                const groupCourses = courses.filter((c) => g.courseIds.includes(c.id));
                if (groupCourses.length === 0) return null;
                return (
                  <div key={g.id}>
                    <div className="mb-4 flex items-baseline gap-2.5">
                      <span className="h-5 w-1.5 rounded-full bg-gradient-to-b from-blue-500 to-violet-500" />
                      <h2 className="font-serif text-lg font-semibold text-[#16233D]">{g.name}</h2>
                      {(g.centerName || g.sectionName) && (
                        <span className="text-xs text-[#8890A1]">
                          {[g.centerName, g.sectionName].filter(Boolean).join(" · ")}
                        </span>
                      )}
                    </div>
                    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                      {groupCourses.map((c) => (
                        <CourseCard key={c.id} course={c} onOpen={openFolder} />
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          )
        ) : browsing ? (
          <div className="py-16 text-center text-[#8890A1]">Loading…</div>
        ) : subfolders.length === 0 && items.length === 0 ? (
          <ZeroState icon="📁" title="Empty folder">This folder is empty.</ZeroState>
        ) : (
          <div className="space-y-6">
            {subfolders.length > 0 && (
              <div className="space-y-2">
                {subfolders.map((f) => (
                  <button
                    key={f.id}
                    onClick={() => openFolder([...path, { id: f.id, name: f.name }])}
                    className="flex w-full items-center justify-between gap-3 rounded-lg border border-[#D9D6C9] bg-white px-4 py-3 text-left transition hover:border-amber-400 hover:shadow-sm"
                  >
                    <span className="flex items-center gap-3">
                      <span className="text-xl">📁</span>
                      <span className="font-medium text-[#16233D]">{f.name}</span>
                    </span>
                    <span className="flex shrink-0 gap-4 text-xs text-[#8890A1]">
                      <span>
                        {f.videoCount} video{f.videoCount === 1 ? "" : "s"}
                      </span>
                      <span>
                        {f.documentCount} e-book{f.documentCount === 1 ? "" : "s"}
                      </span>
                      <span>
                        {f.bookCount} book{f.bookCount === 1 ? "" : "s"}
                      </span>
                      <span>
                        {f.testCount} test{f.testCount === 1 ? "" : "s"}
                      </span>
                    </span>
                  </button>
                ))}
              </div>
            )}

            {items.length > 0 && (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {items.map((it) => (
                  <CourseItemCard key={it.id} item={it} />
                ))}
              </div>
            )}
          </div>
        )}
      </div>

    </LmsShell>
  );
}

// Legacy's real subject card (ui/learn-app .../tags/library/subject.html) is
// a flat solid-color bar + a dry Chapters/E-Books/Tests table. Rebuilt with
// more visual energy for students — a gradient header (not flat), an icon
// medallion that overlaps the header/body seam like a badge, and the stats
// as small pill-chips instead of a table row — while keeping the exact same
// four numbers legacy shows, no fabricated progress bars.
function CourseCard({ course, onOpen }: { course: Course; onOpen: (crumbs: Crumb[]) => void }) {
  const accent = subjectAccent(course.name);
  const stats: { label: string; value: number; icon: string }[] = [
    { label: "Chapters", value: course.chapterCount, icon: "📖" },
    { label: "Videos", value: course.videoCount, icon: "▶️" },
    { label: "E-Books", value: course.documentCount, icon: "📄" },
    { label: "Books", value: course.bookCount, icon: "📚" },
    { label: "Tests", value: course.testCount, icon: "📝" },
  ];
  return (
    <button
      onClick={() => onOpen([{ id: course.id, name: course.name }])}
      className={`group overflow-hidden rounded-2xl border border-[#D9D6C9] bg-white text-left shadow-sm transition hover:-translate-y-1 hover:border-transparent hover:shadow-xl`}
    >
      <div className={`relative ${accent.gradient} px-4 pb-8 pt-3.5`}>
        <span className="font-serif text-[15px] font-semibold text-white drop-shadow-sm">{course.name}</span>
      </div>
      <div className="relative px-4 pb-4">
        <span
          className={`absolute -top-7 flex h-14 w-14 items-center justify-center rounded-2xl bg-white text-2xl shadow-md ring-4 ring-white transition group-hover:-translate-y-0.5 group-hover:rotate-3`}
        >
          <span className={`flex h-full w-full items-center justify-center rounded-2xl ${accent.chip}`}>
            {subjectEmoji(course.name)}
          </span>
        </span>
        <div className="grid grid-cols-2 gap-2 pt-9">
          {stats.map((s) => (
            <div key={s.label} className="flex items-center justify-between rounded-lg bg-[#F8F7F3] px-2.5 py-1.5">
              <span className="flex items-center gap-1.5 text-[11px] text-[#8890A1]">
                <span className="text-xs">{s.icon}</span>
                {s.label}
              </span>
              <span className="font-serif text-sm font-semibold text-[#16233D]">{s.value}</span>
            </div>
          ))}
        </div>
        <div
          className={`mt-3 flex items-center gap-1 text-xs font-semibold ${accent.text} opacity-0 transition group-hover:opacity-100`}
        >
          Explore subject
          <span className="transition group-hover:translate-x-0.5">→</span>
        </div>
      </div>
    </button>
  );
}

function subjectEmoji(name: string): string {
  const n = name.toLowerCase();
  if (n.includes("physic")) return "⚛️";
  if (n.includes("chem")) return "🧪";
  if (n.includes("math")) return "📐";
  if (n.includes("botany")) return "🌱";
  if (n.includes("zoolog")) return "🐾";
  if (n.includes("bio")) return "🧬";
  return "📘";
}

function CourseItemCard({ item }: { item: Item }) {
  const inner = (
    <div className="rounded-lg border border-[#D9D6C9] bg-white p-4 transition hover:shadow-md">
      <span
        className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
          item.type === "VIDEO"
            ? "bg-rose-100 text-rose-700"
            : item.type === "DOCUMENT"
            ? "bg-amber-100 text-amber-700"
            : item.type === "BOOK"
            ? "bg-indigo-100 text-indigo-700"
            : "bg-emerald-100 text-emerald-700"
        }`}
      >
        {item.type}
      </span>
      <div className="mt-2 font-medium text-[#16233D]">{item.name}</div>
      {item.type === "VIDEO" && item.embedUrl && (
        <div className="mt-2 aspect-video w-full overflow-hidden rounded">
          <iframe
            src={item.embedUrl}
            className="h-full w-full"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
            title={item.name}
          />
        </div>
      )}
      {item.type === "VIDEO" && !item.embedUrl && item.url && (
        <video
          src={item.url}
          controls
          controlsList="nodownload"
          disablePictureInPicture
          onContextMenu={(e) => e.preventDefault()}
          className="mt-2 w-full rounded"
        />
      )}
      {item.type === "DOCUMENT" && <div className="mt-2 text-xs text-blue-600">Open document ↗</div>}
      {item.type === "BOOK" && <div className="mt-2 text-xs text-indigo-600">Open book ↗</div>}
    </div>
  );

  if (item.type === "TEST") return <Link href={`/test/${item.id}`}>{inner}</Link>;
  if ((item.type === "DOCUMENT" || item.type === "BOOK") && item.url)
    return (
      <a href={item.url} target="_blank" rel="noreferrer">
        {inner}
      </a>
    );
  return inner;
}