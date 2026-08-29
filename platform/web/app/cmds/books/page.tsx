"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";

type Book = {
  id: string;
  name: string;
  url: string | null;
  fileSize: number | null;
  subject: string | null;
  chapter: string | null;
  boardIds: string[];
  lastUpdated: number;
};
type BoardNode = { id: string; name: string };

function fmtSize(bytes: number | null): string {
  if (!bytes) return "";
  const mb = bytes / (1024 * 1024);
  return mb >= 1 ? `${mb.toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB`;
}

export default function BooksPage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [subjects, setSubjects] = useState<BoardNode[]>([]);
  const [expandedSubject, setExpandedSubject] = useState<string | null>(null);
  const [chaptersBySubject, setChaptersBySubject] = useState<Record<string, BoardNode[]>>({});
  const [loadingChapters, setLoadingChapters] = useState(false);
  const [filterBoardId, setFilterBoardId] = useState<string | null>(null);
  const [filterLabel, setFilterLabel] = useState("All Subjects");

  useEffect(() => {
    fetch("/api/cmds/tools/boards")
      .then((r) => r.json())
      .then((d) => setSubjects(d.nodes || []))
      .catch(() => {});
  }, []);

  async function toggleSubject(s: BoardNode) {
    if (expandedSubject === s.id) {
      setExpandedSubject(null);
      return;
    }
    setExpandedSubject(s.id);
    if (!chaptersBySubject[s.id]) {
      setLoadingChapters(true);
      try {
        const d = await fetch(`/api/cmds/tools/boards?parentId=${s.id}`).then((r) => r.json());
        setChaptersBySubject((prev) => ({ ...prev, [s.id]: d.nodes || [] }));
      } finally {
        setLoadingChapters(false);
      }
    }
  }

  function filterByChapter(c: BoardNode) {
    setFilterLabel(c.name);
    setFilterBoardId(c.id);
  }
  function clearFilter() {
    setFilterBoardId(null);
    setFilterLabel("All Subjects");
  }

  async function load() {
    setLoading(true);
    setError("");
    try {
      const qs = filterBoardId ? `?boardId=${filterBoardId}` : "";
      const r = await fetch(`/api/cmds/books${qs}`);
      const d = await r.json();
      if (d.error) setError(d.error);
      setBooks(d.items || []);
    } catch {
      setError("Failed to load books");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterBoardId]);

  async function deleteBook(b: Book) {
    if (!window.confirm(`Delete "${b.name}"? This can't be undone from the UI.`)) return;
    setError("");
    const r = await fetch(`/api/cmds/books?id=${encodeURIComponent(b.id)}`, { method: "DELETE" });
    const d = await r.json();
    if (!r.ok || d.error) setError(d.error || "Delete failed");
    else await load();
  }

  return (
    <CmdsShell active="resources">
      <main className="mx-auto max-w-6xl px-4 py-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-slate-800">Books</h1>
            <p className="mt-1 text-slate-500">
              Upload textbooks and reference books, tagged to a chapter so students can find them in the Digital
              Library.
            </p>
          </div>
          <div className="flex gap-2">
            <Link
              href="/cmds/books/new"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              + Add Book
            </Link>
            <button
              onClick={load}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
            >
              Refresh
            </button>
          </div>
        </div>

        {error && !loading && (
          <div className="mt-6 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">{error}</div>
        )}

        <div className="mt-6 flex gap-6">
          <aside className="w-56 shrink-0">
            <div className="rounded-xl bg-white p-4 ring-1 ring-black/5">
              <h3 className="text-sm font-semibold text-slate-700">Board Tree</h3>
              <button
                onClick={clearFilter}
                className={`mt-2 block w-full rounded px-2 py-1 text-left text-sm ${
                  !filterBoardId ? "bg-blue-50 font-medium text-blue-700" : "text-slate-600 hover:bg-slate-50"
                }`}
              >
                All Subjects
              </button>
              <div className="mt-1 space-y-0.5">
                {subjects.map((s) => (
                  <div key={s.id}>
                    <div className="flex items-center">
                      <button
                        onClick={() => toggleSubject(s)}
                        className="px-1 text-xs text-slate-400 hover:text-slate-600"
                      >
                        {expandedSubject === s.id ? "▾" : "▸"}
                      </button>
                      <button
                        onClick={() => toggleSubject(s)}
                        title={s.name}
                        className="flex-1 truncate rounded px-1 py-1 text-left text-sm text-slate-700 hover:bg-slate-50"
                      >
                        {s.name}
                      </button>
                    </div>
                    {expandedSubject === s.id && (
                      <div className="ml-4 space-y-0.5">
                        {loadingChapters && !chaptersBySubject[s.id] && (
                          <div className="px-2 py-1 text-xs text-slate-400">Loading…</div>
                        )}
                        {(chaptersBySubject[s.id] || []).map((c) => (
                          <button
                            key={c.id}
                            onClick={() => filterByChapter(c)}
                            title={c.name}
                            className={`block w-full truncate rounded px-2 py-1 text-left text-xs ${
                              filterLabel === c.name
                                ? "bg-blue-50 font-medium text-blue-700"
                                : "text-slate-500 hover:bg-slate-50"
                            }`}
                          >
                            {c.name}
                          </button>
                        ))}
                        {chaptersBySubject[s.id] && chaptersBySubject[s.id].length === 0 && (
                          <div className="px-2 py-1 text-xs text-slate-300">No chapters</div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </aside>

          <div className="min-w-0 flex-1">
            {filterBoardId && (
              <div className="mb-4 flex items-center justify-between rounded-md bg-slate-100 px-3 py-2 text-sm text-slate-600">
                <span>
                  Filtered by <span className="font-medium text-slate-800">{filterLabel}</span> — {books.length}{" "}
                  book{books.length === 1 ? "" : "s"}
                </span>
                <button onClick={clearFilter} className="text-blue-600 hover:underline">
                  Clear
                </button>
              </div>
            )}

            {loading ? (
              <div className="py-16 text-center text-slate-400">Loading…</div>
            ) : books.length === 0 ? (
              <div className="rounded-xl border border-dashed border-slate-200 py-16 text-center text-slate-400">
                No books yet.{" "}
                <Link href="/cmds/books/new" className="text-blue-600 hover:underline">
                  Add one
                </Link>
                .
              </div>
            ) : (
              <ul className="space-y-2">
                {books.map((b) => (
                  <li
                    key={b.id}
                    className="flex items-center justify-between gap-3 rounded-lg bg-white p-3 ring-1 ring-black/5"
                  >
                    <div className="flex min-w-0 items-center gap-3">
                      <span className="text-2xl">📖</span>
                      <div className="min-w-0">
                        <div className="truncate font-medium text-slate-800">{b.name}</div>
                        <div className="text-xs text-slate-400">
                          {b.chapter || "Untagged"}
                          {b.fileSize ? ` · ${fmtSize(b.fileSize)}` : ""}
                        </div>
                      </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-3">
                      {b.url && (
                        <a
                          href={b.url}
                          target="_blank"
                          rel="noreferrer"
                          className="text-sm text-blue-600 hover:underline"
                        >
                          View
                        </a>
                      )}
                      <button
                        onClick={() => deleteBook(b)}
                        className="text-sm text-red-500 hover:underline"
                      >
                        Delete
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </main>
    </CmdsShell>
  );
}
