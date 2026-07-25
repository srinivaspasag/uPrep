"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";

type Course = { id: string; name: string; chapterCount: number; folderCount: number };
type Sub = { id: string; name: string; type: "FOLDER" };
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

  const openFolder = useCallback(async (crumbs: Crumb[]) => {
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

  const atRoot = path.length === 0;

  return (
    <LmsShell active="courses">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">My Courses</h1>
        {staff && (
          <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs text-indigo-600">
            Staff preview — showing all courses
          </span>
        )}
      </div>

      {atRoot && !staff && (
        <div className="mt-4 flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 p-3">
          <span className="text-sm text-slate-600">Have an access code?</span>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            onKeyDown={(e) => e.key === "Enter" && joinWithCode()}
            placeholder="ENTER CODE"
            className="w-40 rounded border border-slate-300 px-3 py-1.5 font-mono text-sm tracking-widest outline-none focus:border-emerald-500"
          />
          <button
            onClick={joinWithCode}
            disabled={joining}
            className="rounded bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {joining ? "Joining…" : "Join with code"}
          </button>
          {joinMsg && <span className="text-xs text-slate-500">{joinMsg}</span>}
        </div>
      )}

      {/* Breadcrumbs */}
      {!atRoot && (
        <div className="mt-3 flex flex-wrap items-center gap-1 text-sm text-slate-500">
          <button onClick={() => openFolder([])} className="hover:text-emerald-700">
            My Courses
          </button>
          {path.map((c, i) => (
            <span key={c.id} className="flex items-center gap-1">
              <span className="text-slate-300">/</span>
              <button
                onClick={() => openFolder(path.slice(0, i + 1))}
                className={i === path.length - 1 ? "font-medium text-slate-700" : "hover:text-emerald-700"}
              >
                {c.name}
              </button>
            </span>
          ))}
        </div>
      )}

      <div className="mt-6">
        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : atRoot ? (
          courses.length === 0 ? (
            <ZeroState img="/legacy/zero/general-no-content.jpg">
              You have no courses yet. Ask your institute to assign one.
            </ZeroState>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {courses.map((c) => (
                <button
                  key={c.id}
                  onClick={() => openFolder([{ id: c.id, name: c.name }])}
                  className="rounded-lg border border-slate-200 bg-white p-5 text-left transition hover:border-emerald-300 hover:shadow-md"
                >
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-50 text-lg">
                    📘
                  </div>
                  <div className="mt-3 font-semibold text-slate-800">{c.name}</div>
                  <div className="mt-1 text-xs text-slate-400">{c.chapterCount} chapters</div>
                </button>
              ))}
            </div>
          )
        ) : browsing ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : subfolders.length === 0 && items.length === 0 ? (
          <ZeroState img="/legacy/zero/general-no-content.jpg">This folder is empty.</ZeroState>
        ) : (
          <div className="space-y-6">
            {subfolders.length > 0 && (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {subfolders.map((f) => (
                  <button
                    key={f.id}
                    onClick={() => openFolder([...path, { id: f.id, name: f.name }])}
                    className="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 text-left transition hover:border-emerald-300 hover:shadow-sm"
                  >
                    <span className="text-xl">📁</span>
                    <span className="font-medium text-slate-700">{f.name}</span>
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

function CourseItemCard({ item }: { item: Item }) {
  const inner = (
    <div className="rounded-lg border border-slate-200 bg-white p-4 transition hover:shadow-md">
      <span
        className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
          item.type === "VIDEO"
            ? "bg-rose-100 text-rose-700"
            : item.type === "DOCUMENT"
            ? "bg-amber-100 text-amber-700"
            : "bg-emerald-100 text-emerald-700"
        }`}
      >
        {item.type}
      </span>
      <div className="mt-2 font-medium text-slate-800">{item.name}</div>
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
        <video src={item.url} controls className="mt-2 w-full rounded" />
      )}
      {item.type === "DOCUMENT" && <div className="mt-2 text-xs text-blue-600">Open document ↗</div>}
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
