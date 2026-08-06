"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import BoardPicker from "@/components/BoardPicker";
import MathText from "@/components/MathText";

type LibQuestion = {
  id: string;
  text: string;
  type: string;
  options: number;
  difficulty: string | null;
  hasKey: boolean;
};

// Used by /cmds/tests/[id]/edit only — test CREATION lives at
// /cmds/tests/new (a single merged Setup + manual-picker/Instant-Test-
// Generator flow, matching legacy's real single-entry-point structure —
// see that file's header comment). This mirrors legacy's real edit rule
// (CMDSTestManager.modifyTestQuestions refuses once a test is
// published/shared): metadata (name/duration/password/rules) is always
// editable, but the question set locks the moment the test has a real
// student attempt — see app/api/cmds/tests/[id]/route.ts for the
// server-side enforcement (the client-side disabling here is just UX, not
// the guard).
export default function TestForm({ testId }: { testId: string }) {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);

  const [name, setName] = useState("");
  const [sectionName, setSectionName] = useState("General");
  const [durationMin, setDurationMin] = useState(30);
  const [positive, setPositive] = useState(4);
  const [negative, setNegative] = useState(1);

  const [password, setPassword] = useState("");
  const [enablePartialMarks, setEnablePartialMarks] = useState(false);
  const [enableSectionLocking, setEnableSectionLocking] = useState(false);
  const [autoResumeTest, setAutoResumeTest] = useState(false);
  const [resultVisibility, setResultVisibility] = useState("VISIBLE");

  const [pool, setPool] = useState<LibQuestion[]>([]);
  const [picked, setPicked] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [folderId, setFolderId] = useState<string | null>(null);
  const [folderName, setFolderName] = useState<string>("");
  const [boardIds, setBoardIds] = useState<string[]>([]);

  const [questionsLocked, setQuestionsLocked] = useState(false);
  const [attemptCount, setAttemptCount] = useState(0);
  const [multiSection, setMultiSection] = useState(false);

  const backHref = folderId
    ? `/cmds?folder=${encodeURIComponent(folderId)}&folderName=${encodeURIComponent(folderName)}`
    : "/cmds";

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);

    async function loadExisting() {
      const d = await fetch(`/api/cmds/tests/${testId}`).then((r) => r.json());
      if (d.error) {
        setError(d.error);
        return;
      }
      setName(d.test.name || "");
      setDurationMin(d.test.durationMin || 30);
      setPassword(d.test.password || "");
      setResultVisibility(d.test.resultVisibility || "VISIBLE");
      setEnablePartialMarks(!!d.test.enablePartialMarks);
      setEnableSectionLocking(!!d.test.enableSectionLocking);
      setAutoResumeTest(!!d.test.autoResumeTest);
      setFolderId(d.test.folderId || null);
      setQuestionsLocked(!!d.questionsLocked);
      setAttemptCount(d.attemptCount || 0);
      setMultiSection(!d.singleSection);
      if (d.singleSection && d.sections?.[0]) {
        setSectionName(d.sections[0].name || "General");
        setPositive(d.sections[0].positive || 4);
        setNegative(d.sections[0].negative || 1);
        setPicked(d.sections[0].questionIds || []);
      }
    }

    loadExisting();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [router, testId]);

  // Board Tree filter — narrows the published-question pool to a subject or
  // chapter instead of listing the whole org's question bank.
  useEffect(() => {
    if (!session) return;
    setLoading(true);
    const qs = boardIds.length ? `?boardIds=${boardIds.join(",")}` : "";
    fetch(`/api/cmds/tests${qs}`)
      .then((r) => r.json())
      .then((d) => {
        if (d.error) setError(d.error);
        setPool((d.questions || []).filter((q: LibQuestion) => q.hasKey));
      })
      .catch(() => setError("Failed to load published questions."))
      .finally(() => setLoading(false));
  }, [session, boardIds]);

  // "How many questions of each type" target + live counter as you pick —
  // set a target per question type, watch it fill up while selecting
  // instead of hand-counting against the pool below. Enforced: once a type
  // hits its target, picking another of that type is blocked (a target of
  // 0/unset stays unlimited). Unchecking is never blocked.
  const [typeTargets, setTypeTargets] = useState<Record<string, number>>({});
  const [pickError, setPickError] = useState("");
  const typesInPool = useMemo(() => Array.from(new Set(pool.map((q) => q.type))).sort(), [pool]);
  const pickedByType = useMemo(() => {
    const poolById = new Map(pool.map((q) => [q.id, q]));
    const counts: Record<string, number> = {};
    for (const id of picked) {
      const t = poolById.get(id)?.type;
      if (t) counts[t] = (counts[t] || 0) + 1;
    }
    return counts;
  }, [picked, pool]);

  function toggle(id: string) {
    if (questionsLocked) return;
    setPickError("");
    if (picked.includes(id)) {
      setPicked((prev) => prev.filter((x) => x !== id));
      return;
    }
    const type = pool.find((q) => q.id === id)?.type;
    const target = type ? typeTargets[type] || 0 : 0;
    if (type && target > 0 && (pickedByType[type] || 0) >= target) {
      setPickError(`${type} target is ${target} — already reached. Raise the target or remove one first.`);
      return;
    }
    setPicked((prev) => [...prev, id]);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!name.trim()) return setError("Enter a test name.");

    setSaving(true);
    try {
      const r = await fetch(`/api/cmds/tests/${testId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          durationMin,
          password: password.trim(),
          enablePartialMarks,
          enableSectionLocking,
          autoResumeTest,
          resultVisibility,
          // Only send question-set fields when they're actually editable —
          // sending them locked-but-unchanged would otherwise trip the
          // server's 409 for a test that already has attempts.
          ...(!questionsLocked && !multiSection
            ? { sectionName: sectionName.trim() || "General", positive, negative, questionIds: picked }
            : {}),
        }),
      });
      const d = await r.json();
      if (!r.ok || d.error) {
        setError(d.error || "Failed to save test.");
        setSaving(false);
        return;
      }
      router.push(backHref);
    } catch {
      setError("Failed to save test.");
      setSaving(false);
    }
  }

  const totalMarks = picked.length * positive;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="mx-auto max-w-3xl px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-slate-800 flex items-center justify-center text-white font-bold">
              C
            </div>
            <span className="font-semibold text-slate-800">UPrep CMDS</span>
          </div>
          <Link href={backHref} className="text-sm text-blue-600 hover:underline">
            ← Resources
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-slate-800">Edit Test</h1>
        <p className="mt-1 text-slate-500">
          Change this test's settings. It's attemptable and auto-graded in the learn app immediately.
        </p>
        {questionsLocked && (
          <div className="mt-4 rounded-md bg-amber-50 px-3 py-3 text-sm text-amber-700 ring-1 ring-amber-200">
            {attemptCount} student{attemptCount === 1 ? " has" : "s have"} already completed this test, so its
            question set is locked to keep results consistent. Name, duration, and other settings can still be
            changed.
          </div>
        )}
        {!questionsLocked && multiSection && (
          <div className="mt-4 rounded-md bg-slate-100 px-3 py-3 text-sm text-slate-600 ring-1 ring-slate-200">
            This test has multiple subject sections (from the Instant Test Generator) — its question set isn't
            editable here yet. Settings below still apply.
          </div>
        )}

        <form onSubmit={submit} className="mt-6 space-y-6">
          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Test name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Physics — Weekly Practice #2"
              className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />

            <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div>
                <label className="block text-sm font-medium text-slate-700">Section</label>
                <input
                  value={sectionName}
                  onChange={(e) => setSectionName(e.target.value)}
                  disabled={questionsLocked || multiSection}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800 disabled:bg-slate-50 disabled:text-slate-400"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Duration (min)</label>
                <input
                  type="number"
                  min={1}
                  value={durationMin}
                  onChange={(e) => setDurationMin(Number(e.target.value))}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Marks / correct</label>
                <input
                  type="number"
                  min={1}
                  value={positive}
                  onChange={(e) => setPositive(Number(e.target.value))}
                  disabled={questionsLocked || multiSection}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800 disabled:bg-slate-50 disabled:text-slate-400"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Negative</label>
                <input
                  type="number"
                  min={0}
                  value={negative}
                  onChange={(e) => setNegative(Number(e.target.value))}
                  disabled={questionsLocked || multiSection}
                  className="mt-2 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800 disabled:bg-slate-50 disabled:text-slate-400"
                />
              </div>
            </div>
          </div>

          <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
            <label className="block text-sm font-medium text-slate-700">Rules & settings</label>
            <div className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-medium text-slate-500">Access password (optional)</label>
                <input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Leave blank for open access"
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500">Result visibility</label>
                <select
                  value={resultVisibility}
                  onChange={(e) => setResultVisibility(e.target.value)}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700"
                >
                  <option value="VISIBLE">Show immediately</option>
                  <option value="HIDDEN">Hide from students</option>
                  <option value="AFTER_END">After test window ends</option>
                </select>
              </div>
            </div>
            <div className="mt-3 flex flex-wrap gap-4 text-sm text-slate-600">
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={enablePartialMarks} onChange={(e) => setEnablePartialMarks(e.target.checked)} />
                Partial marks (MCQ)
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={enableSectionLocking} onChange={(e) => setEnableSectionLocking(e.target.checked)} />
                Section locking
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" className="h-4 w-4 accent-blue-600" checked={autoResumeTest} onChange={(e) => setAutoResumeTest(e.target.checked)} />
                Auto-resume
              </label>
            </div>
          </div>

          {!questionsLocked && !multiSection && (
            <>
              <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
                <label className="block text-sm font-medium text-slate-700">
                  Filter by subject / chapter <span className="text-slate-400">(optional)</span>
                </label>
                <div className="mt-2">
                  <BoardPicker selected={boardIds} onChange={setBoardIds} />
                </div>
              </div>

              <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
                <div className="flex items-center justify-between">
                  <label className="block text-sm font-medium text-slate-700">
                    Questions{" "}
                    <span className="text-slate-400">
                      (published, with answer keys{boardIds.length ? " · filtered to selected chapters" : ""})
                    </span>
                  </label>
                  <span className="text-sm text-slate-500">
                    {picked.length} selected · {totalMarks} marks
                  </span>
                </div>

                {typesInPool.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-3 rounded-md bg-slate-50 p-3">
                    {typesInPool.map((t) => {
                      const target = typeTargets[t] || 0;
                      const got = pickedByType[t] || 0;
                      const met = target > 0 && got >= target;
                      return (
                        <label key={t} className="flex items-center gap-1.5 text-xs text-slate-600">
                          {t}
                          <input
                            type="number"
                            min={0}
                            value={target || ""}
                            onChange={(e) =>
                              setTypeTargets((prev) => ({ ...prev, [t]: Math.max(0, Number(e.target.value) || 0) }))
                            }
                            placeholder="0"
                            className="w-14 rounded border border-slate-300 px-1.5 py-0.5 text-xs outline-none focus:border-slate-500"
                          />
                          <span className={met ? "font-medium text-emerald-600" : "text-slate-400"}>
                            {got}/{target || "—"}
                          </span>
                        </label>
                      );
                    })}
                  </div>
                )}
                {pickError && (
                  <p className="mt-2 rounded-md bg-amber-50 px-3 py-1.5 text-xs text-amber-700 ring-1 ring-amber-200">
                    {pickError}
                  </p>
                )}

                {loading && <div className="mt-4 text-slate-500">Loading…</div>}
                {!loading && pool.length === 0 && (
                  <div className="mt-4 rounded-md bg-amber-50 px-3 py-3 text-sm text-amber-700 ring-1 ring-amber-200">
                    {boardIds.length
                      ? "No published questions tagged to these chapters yet. Try clearing the filter or publish some questions first."
                      : "No published questions yet. Publish some questions first, then create a test."}
                  </div>
                )}

                <div className="mt-3 space-y-2">
                  {pool.map((q, i) => (
                    <label
                      key={q.id}
                      className="flex cursor-pointer items-start gap-3 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50"
                    >
                      <input
                        type="checkbox"
                        className="mt-1 h-4 w-4 accent-blue-600"
                        checked={picked.includes(q.id)}
                        onChange={() => toggle(q.id)}
                      />
                      <span className="flex-1">
                        <span className="text-slate-800">
                          {picked.includes(q.id) && (
                            <span className="mr-1 text-xs font-semibold text-blue-600">
                              #{picked.indexOf(q.id) + 1}
                            </span>
                          )}
                          {q.text ? <MathText>{q.text}</MathText> : `Question ${i + 1}`}
                        </span>
                        <span className="mt-0.5 block text-xs text-slate-400">
                          {q.type} · {q.options} options · {q.difficulty || "—"}
                        </span>
                      </span>
                    </label>
                  ))}
                </div>
              </div>
            </>
          )}

          {error && (
            <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {saving ? "Saving…" : "Save Changes"}
            </button>
            <Link
              href={backHref}
              className="rounded-md border border-slate-300 px-5 py-2.5 text-sm text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </Link>
          </div>
        </form>
      </main>
    </div>
  );
}
