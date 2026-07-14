"use client";

import { useEffect, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";

type Result = { id: string; name: string; type: string; subject: string | null; url: string | null };

const TYPE_BADGE: Record<string, string> = {
  TEST: "bg-emerald-100 text-emerald-700",
  MODULE: "bg-indigo-100 text-indigo-700",
  DOCUMENT: "bg-amber-100 text-amber-700",
  VIDEO: "bg-rose-100 text-rose-700",
  QUESTION_SET: "bg-sky-100 text-sky-700",
  DISCUSSION: "bg-slate-100 text-slate-600",
};

function hrefFor(r: Result): string {
  if (r.type === "TEST") return `/test/${r.id}`;
  if (r.type === "DISCUSSION") return `/learn/doubts/${r.id}`;
  if (r.url) return r.url;
  return "/learn/library";
}

function SearchInner() {
  const params = useSearchParams();
  const initial = params.get("q") || "";
  const [q, setQ] = useState(initial);
  const [results, setResults] = useState<Result[]>([]);
  const [loading, setLoading] = useState(false);

  async function run(term: string) {
    if (!term.trim()) {
      setResults([]);
      return;
    }
    setLoading(true);
    const res = await fetch(`/api/learn/search?q=${encodeURIComponent(term)}`);
    const d = await res.json();
    setResults(d.results || []);
    setLoading(false);
  }

  useEffect(() => {
    if (initial) run(initial);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initial]);

  return (
    <>
      <h1 className="text-lg font-semibold text-slate-800">Search</h1>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          run(q);
        }}
        className="mt-4 flex gap-2"
      >
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search tests, videos, documents, doubts…"
          className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
          autoFocus
        />
        <button className="rounded-md bg-[#e8443b] px-5 py-2 text-sm font-semibold text-white hover:bg-[#d33c34]">
          Search
        </button>
      </form>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Searching…</div>
      ) : results.length === 0 ? (
        <div className="py-16 text-center text-sm text-slate-400">
          {q ? "No matches found." : "Type a query to search the library."}
        </div>
      ) : (
        <ul className="mt-6 divide-y divide-slate-100">
          {results.map((r) => (
            <li key={`${r.type}-${r.id}`}>
              <Link
                href={hrefFor(r)}
                target={r.url && r.type !== "TEST" ? "_blank" : undefined}
                className="flex items-center gap-3 py-3 hover:bg-slate-50"
              >
                <span
                  className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                    TYPE_BADGE[r.type] || "bg-slate-100 text-slate-600"
                  }`}
                >
                  {r.type}
                </span>
                <span className="flex-1 text-slate-700">{r.name}</span>
                {r.subject && <span className="text-xs text-slate-400">{r.subject}</span>}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

export default function SearchPage() {
  return (
    <LmsShell active="library">
      <Suspense fallback={<div className="py-16 text-center text-sm text-slate-400">Loading…</div>}>
        <SearchInner />
      </Suspense>
    </LmsShell>
  );
}
