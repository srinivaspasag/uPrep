"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, clearSession, type UprepSession } from "@/lib/session";

type Question = {
  id: string;
  text: string;
  type: string;
  difficulty: string | null;
  published: boolean;
  completed: boolean;
  status: string;
  hasKey: boolean;
  options: number;
};
type Test = {
  id: string;
  name: string;
  type: string;
  qusCount: number;
  totalMarks: number;
  durationMin: number;
  published: boolean;
  completed: boolean;
};
type ModuleItem = {
  id: string;
  name: string;
  contentCount: number;
  published: boolean;
  completed: boolean;
};

type Tab = "questions" | "tests" | "modules";

function Pill({ ok, yes, no }: { ok: boolean; yes: string; no: string }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
        ok ? "bg-emerald-100 text-emerald-700" : "bg-amber-100 text-amber-700"
      }`}
    >
      {ok ? yes : no}
    </span>
  );
}

export default function CmdsPage() {
  const router = useRouter();
  const [session, setSessionState] = useState<UprepSession | null>(null);
  const [tab, setTab] = useState<Tab>("questions");
  const [questions, setQuestions] = useState<Question[]>([]);
  const [tests, setTests] = useState<Test[]>([]);
  const [modules, setModules] = useState<ModuleItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [publishing, setPublishing] = useState(false);
  const [notice, setNotice] = useState("");

  async function load() {
    setLoading(true);
    try {
      const r = await fetch("/api/cmds/resources");
      const d = await r.json();
      if (d.error) setError(d.error);
      setQuestions(d.questions || []);
      setTests(d.tests || []);
      setModules(d.modules || []);
      setSelected(new Set());
    } catch {
      setError("Failed to load CMDS resources");
    } finally {
      setLoading(false);
    }
  }

  function toggleSelect(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  const publishableIds = useMemo(
    () => questions.filter((q) => !q.published && q.hasKey).map((q) => q.id),
    [questions]
  );

  function toggleSelectAll() {
    setSelected((prev) =>
      prev.size >= publishableIds.length && publishableIds.length > 0
        ? new Set()
        : new Set(publishableIds)
    );
  }

  async function publishSelected() {
    if (selected.size === 0) return;
    setPublishing(true);
    setNotice("");
    setError("");
    try {
      const r = await fetch("/api/cmds/publish", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ids: Array.from(selected), userId: session?.id }),
      });
      const d = await r.json();
      if (!r.ok || d.error) {
        setError(d.error || "Publish failed");
      } else {
        setNotice(`Published ${d.published} of ${d.total} question(s). They're now gradable in the learn app.`);
        await load();
      }
    } catch {
      setError("Publish failed");
    } finally {
      setPublishing(false);
    }
  }

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSessionState(s);
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [router]);

  function logout() {
    clearSession();
    router.replace("/login");
  }

  const counts = useMemo(
    () => ({
      questions: questions.length,
      tests: tests.length,
      modules: modules.length,
    }),
    [questions, tests, modules]
  );

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="mx-auto max-w-6xl px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-slate-800 flex items-center justify-center text-white font-bold">
              C
            </div>
            <span className="font-semibold text-slate-800">UPrep CMDS</span>
            <span className="ml-2 rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-500">
              Content Management
            </span>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <Link href="/learn/library" className="text-blue-600 hover:underline">
              Learn app →
            </Link>
            <span className="text-slate-600">
              {session?.firstName} {session?.lastName}
            </span>
            <button
              onClick={logout}
              className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-600 hover:bg-slate-100"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-slate-800">Content Resources</h1>
            <p className="mt-1 text-slate-500">
              Author questions and tests, then publish them to the learning library.
            </p>
          </div>
          <div className="flex gap-2">
            <Link
              href="/cmds/questions/new"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              + Add Question
            </Link>
            <Link
              href="/cmds/tests/new"
              className="rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
            >
              + Create Test
            </Link>
            <Link
              href="/cmds/assignments/new"
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              + Assignment
            </Link>
            <button
              onClick={load}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
            >
              Refresh
            </button>
          </div>
        </div>

        <div className="mt-6 flex gap-1 border-b border-slate-200">
          {(["questions", "tests", "modules"] as Tab[]).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-4 py-2 text-sm font-medium capitalize -mb-px border-b-2 ${
                tab === t
                  ? "border-blue-600 text-blue-700"
                  : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            >
              {t} <span className="text-slate-400">({counts[t]})</span>
            </button>
          ))}
        </div>

        {loading && <div className="mt-8 text-slate-500">Loading…</div>}
        {error && !loading && (
          <div className="mt-6 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
            {error}
          </div>
        )}
        {notice && !loading && (
          <div className="mt-6 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 ring-1 ring-emerald-200">
            {notice}
          </div>
        )}

        {!loading && tab === "questions" && (
          <>
            {selected.size > 0 && (
              <div className="mt-6 flex items-center justify-between rounded-lg bg-blue-50 px-4 py-3 ring-1 ring-blue-200">
                <span className="text-sm text-blue-800">{selected.size} selected</span>
                <button
                  onClick={publishSelected}
                  disabled={publishing}
                  className="rounded-md bg-blue-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {publishing ? "Publishing…" : "Publish selected"}
                </button>
              </div>
            )}
            <div className="mt-6 overflow-hidden rounded-xl bg-white ring-1 ring-black/5">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-left text-slate-500">
                  <tr>
                    <th className="px-4 py-3 font-medium w-10">
                      <input
                        type="checkbox"
                        className="h-4 w-4 accent-blue-600"
                        checked={publishableIds.length > 0 && selected.size >= publishableIds.length}
                        disabled={publishableIds.length === 0}
                        onChange={toggleSelectAll}
                        title="Select all publishable"
                      />
                    </th>
                    <th className="px-4 py-3 font-medium">Question</th>
                    <th className="px-4 py-3 font-medium">Type</th>
                    <th className="px-4 py-3 font-medium">Answer key</th>
                    <th className="px-4 py-3 font-medium">Published</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {questions.map((q) => {
                    const publishable = !q.published && q.hasKey;
                    return (
                      <tr key={q.id} className="hover:bg-slate-50">
                        <td className="px-4 py-3">
                          <input
                            type="checkbox"
                            className="h-4 w-4 accent-blue-600 disabled:opacity-30"
                            checked={selected.has(q.id)}
                            disabled={!publishable}
                            onChange={() => toggleSelect(q.id)}
                            title={
                              q.published
                                ? "Already published"
                                : q.hasKey
                                ? "Select to publish"
                                : "Add an answer key first"
                            }
                          />
                        </td>
                        <td className="px-4 py-3 text-slate-800">
                          {q.text || <span className="text-slate-400">(no text)</span>}
                          <div className="text-xs text-slate-400">
                            {q.options} options · {q.difficulty || "—"}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-slate-600">{q.type}</td>
                        <td className="px-4 py-3">
                          <Pill ok={q.hasKey} yes="Set" no="Missing" />
                        </td>
                        <td className="px-4 py-3">
                          <Pill ok={q.published} yes="Published" no="Draft" />
                        </td>
                      </tr>
                    );
                  })}
                  {questions.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-4 py-8 text-center text-slate-400">
                        No questions yet.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}

        {!loading && tab === "tests" && (
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {tests.map((t) => (
              <div key={t.id} className="rounded-xl bg-white p-5 ring-1 ring-black/5">
                <div className="flex items-center justify-between">
                  <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
                    {t.type}
                  </span>
                  <Pill ok={t.published} yes="Published" no="Draft" />
                </div>
                <div className="mt-3 font-semibold text-slate-800">{t.name}</div>
                <div className="mt-3 flex gap-4 text-sm text-slate-500">
                  <span>❓ {t.qusCount} q</span>
                  <span>⏱ {t.durationMin} min</span>
                  <span>🎯 {t.totalMarks}</span>
                </div>
                <div className="mt-4 flex gap-2">
                  <Link
                    href={`/test/${t.id}`}
                    className="inline-block rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100"
                  >
                    Open →
                  </Link>
                  <Link
                    href={`/cmds/papers/${t.id}`}
                    className="inline-block rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100"
                  >
                    Paper / PDF
                  </Link>
                </div>
              </div>
            ))}
            {tests.length === 0 && (
              <div className="col-span-full rounded-xl bg-white p-8 text-center ring-1 ring-black/5">
                <div className="text-slate-400">No tests yet.</div>
                <Link
                  href="/cmds/tests/new"
                  className="mt-3 inline-block rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
                >
                  + Create your first test
                </Link>
              </div>
            )}
          </div>
        )}

        {!loading && tab === "modules" && (
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {modules.map((m) => (
              <div key={m.id} className="rounded-xl bg-white p-5 ring-1 ring-black/5">
                <div className="flex items-center justify-between">
                  <span className="rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                    MODULE
                  </span>
                  <Pill ok={m.published} yes="Published" no="Draft" />
                </div>
                <div className="mt-3 font-semibold text-slate-800">{m.name}</div>
                <div className="mt-3 text-sm text-slate-500">📚 {m.contentCount} items</div>
              </div>
            ))}
            {modules.length === 0 && (
              <div className="col-span-full rounded-xl bg-white p-8 text-center text-slate-400 ring-1 ring-black/5">
                No modules yet.
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
