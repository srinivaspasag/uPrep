"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import MathText from "@/components/MathText";
import { slugifyCode } from "@/lib/testCode";

// Single "Create Test" entry point, matching legacy's real structure
// (QrTests.createTest() vs createTestAuto() — ONE Setup screen shared by
// both, forking only on an "auto-generate" flag) instead of the two fully
// separate pages this rebuild had split it into.
//
// Manual and auto-generate share the SAME Subjects & Types (grid of Number
// of Questions / Marks / Negative Marks per type) and Chapters steps — the
// grid's "Number of Questions" means "how many to auto-pick" in auto mode
// and "the cap you may manually pick" in manual mode.
//
// A subject can hold MULTIPLE admin-defined Sections (default "Section 1",
// "+ Add Section" for more) — matching a real test doc's shape
// (metadata[].details[]: one section CAN mix SCQ/MCQ/Numeric, each with its
// own marks) rather than forcing one section per question type. Each
// section gets its own type/count/marks grid; the saved test has one
// physical section per admin-defined Section, with per-type marks inside.

type Node = { id: string; name: string; type: string; parentId: string | null };
type TypeRow = { type: string; count: number; positive: number; negative: number };
type TestSection = { id: string; name: string; typeRows: TypeRow[] };
type DifficultyRow = { level: string; count: number };
type PublishedFilter = "PUBLISHED" | "UNPUBLISHED" | "BOTH";
type LibQuestion = {
  id: string;
  text: string;
  type: string;
  options: number;
  difficulty: string | null;
  hasKey: boolean;
};

type SubjectConfig = {
  subjectId: string;
  subjectName: string;
  chapters: Node[];
  selectedChapterIds: string[];
  selectedConceptIds: Record<string, string[]>;
  sections: TestSection[];
  publishedFilter: PublishedFilter;
  // Keyed by question type (not by section — a type belongs to exactly one
  // section within a subject, so this stays unambiguous either way).
  difficultyByType: Record<string, DifficultyRow[]>;
};

function effectiveChapterBoardIds(s: SubjectConfig): string[] {
  return s.selectedChapterIds.flatMap((cid) => {
    const concepts = s.selectedConceptIds[cid];
    return concepts && concepts.length > 0 ? concepts : [cid];
  });
}

type GeneratedQuestion = {
  id: string;
  text: string;
  type: string;
  difficulty: string | null;
  published: boolean;
  sectionId: string;
  chapter?: string | null;
};
type GeneratedSubject = { subjectBoardId: string; subjectName: string; questions: GeneratedQuestion[]; requested: number };

const QUESTION_TYPES = ["SCQ", "MCQ", "NUMERIC", "SUBJECTIVE", "MATRIX", "PARA"];
// Legacy's real "Test Format Details" modal always shows exactly these 3
// rows as a fixed grid, typed directly (no add-a-row step). The other types
// this app supports are kept available behind "+ more question types"
// rather than dropped.
const CORE_TYPES = ["SCQ", "MCQ", "NUMERIC"];
const EXTRA_TYPES = QUESTION_TYPES.filter((t) => !CORE_TYPES.includes(t));
const DIFFICULTIES = ["EASY", "MODERATE", "TOUGH"];

function activeTypeRows(sec: TestSection): TypeRow[] {
  return sec.typeRows.filter((r) => r.count > 0);
}
function allActiveRows(subject: SubjectConfig): TypeRow[] {
  return subject.sections.flatMap(activeTypeRows);
}
function makeId(): string {
  return Math.random().toString(36).slice(2, 10);
}
function defaultSection(name: string): TestSection {
  return { id: makeId(), name, typeRows: CORE_TYPES.map((type) => ({ type, count: 0, positive: 0, negative: 0 })) };
}

type Mode = "manual" | "auto";
type Step = "setup" | "types" | "chapters" | "difficulty" | "review" | "pick";
const STEP_LABEL: Record<Step, string> = {
  setup: "Setup",
  types: "Subjects & Types",
  chapters: "Chapters",
  difficulty: "Difficulty",
  review: "Generate & Review",
  pick: "Pick Questions",
};

export default function CreateTestPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [step, setStep] = useState<Step>("setup");
  const [mode, setMode] = useState<Mode>("manual");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const [folderId, setFolderId] = useState<string | null>(null);
  const [folderName, setFolderName] = useState<string>("");
  const backHref = folderId
    ? `/cmds?folder=${encodeURIComponent(folderId)}&folderName=${encodeURIComponent(folderName)}`
    : "/cmds";

  // ---- Setup (shared by both paths) ----
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [codeTouched, setCodeTouched] = useState(false);
  const [durationMin, setDurationMin] = useState(30);
  const [password, setPassword] = useState("");
  const [resultVisibility, setResultVisibility] = useState("VISIBLE");
  const [enablePartialMarks, setEnablePartialMarks] = useState(false);
  const [enableSectionLocking, setEnableSectionLocking] = useState(false);
  const [autoResumeTest, setAutoResumeTest] = useState(false);

  const setupBlockers = useMemo(() => {
    const msgs: string[] = [];
    if (!name.trim()) msgs.push("Enter a test name.");
    if (!code.trim()) msgs.push("Enter a test code.");
    return msgs;
  }, [name, code]);

  const sequence: Step[] = mode === "auto" ? ["types", "chapters", "difficulty", "review"] : ["types", "chapters", "pick"];

  // ---- Subjects (shared by both paths) ----
  const [allSubjects, setAllSubjects] = useState<Node[]>([]);
  const [subjects, setSubjects] = useState<SubjectConfig[]>([]);

  useEffect(() => {
    setSession(getSession());
    const sp = new URLSearchParams(window.location.search);
    setFolderId(sp.get("folder"));
    setFolderName(sp.get("folderName") || "");
    // Old bookmarks to the standalone Instant Test Generator page redirect
    // here with ?mode=auto — honor it so that shortcut still lands on the
    // right branch instead of Setup's default.
    if (sp.get("mode") === "auto") setMode("auto");
    fetch("/api/cmds/tools/boards")
      .then((r) => r.json())
      .then((d) => setAllSubjects(d.nodes || []))
      .catch(() => {});
  }, []);

  function addSubject(subjectId: string) {
    const subj = allSubjects.find((s) => s.id === subjectId);
    if (!subj || subjects.some((s) => s.subjectId === subjectId)) return;
    setSubjects((prev) => [
      ...prev,
      {
        subjectId,
        subjectName: subj.name,
        chapters: [],
        selectedChapterIds: [],
        selectedConceptIds: {},
        sections: [defaultSection("Section 1")],
        publishedFilter: "PUBLISHED",
        difficultyByType: {},
      },
    ]);
    fetch(`/api/cmds/tools/boards?parentId=${subjectId}`)
      .then((r) => r.json())
      .then((d) =>
        setSubjects((prev) =>
          prev.map((s) => (s.subjectId === subjectId ? { ...s, chapters: d.nodes || [] } : s))
        )
      )
      .catch(() => {});
  }
  function removeSubject(subjectId: string) {
    setSubjects((prev) => prev.filter((s) => s.subjectId !== subjectId));
  }
  function toggleSubject(subjectId: string) {
    if (subjects.some((s) => s.subjectId === subjectId)) removeSubject(subjectId);
    else addSubject(subjectId);
  }
  function updateSubject(subjectId: string, patch: Partial<SubjectConfig>) {
    setSubjects((prev) => prev.map((s) => (s.subjectId === subjectId ? { ...s, ...patch } : s)));
  }
  function copyFormatToOthers(fromId: string) {
    const from = subjects.find((s) => s.subjectId === fromId);
    if (!from) return;
    setSubjects((prev) =>
      prev.map((s) =>
        s.subjectId === fromId
          ? s
          : { ...s, sections: from.sections.map((sec) => ({ ...sec, id: makeId(), typeRows: sec.typeRows.map((r) => ({ ...r })) })) }
      )
    );
  }

  const typesBlockers = useMemo(
    () =>
      subjects.length === 0
        ? ["Add at least one subject."]
        : subjects.filter((s) => allActiveRows(s).length === 0).map((s) => `${s.subjectName}: enter a count for at least one question type.`),
    [subjects]
  );
  const chaptersBlockers = useMemo(
    () => subjects.filter((s) => s.selectedChapterIds.length === 0).map((s) => `${s.subjectName}: select at least one chapter.`),
    [subjects]
  );
  // A difficulty split must either be untouched (any difficulty) or add up
  // to exactly the type's requested count — a partial split (e.g. 1 of 3
  // allocated) would silently under-fill that type.
  const difficultyBlockers = useMemo(
    () =>
      subjects.flatMap((s) =>
        s.sections.flatMap((sec) =>
          activeTypeRows(sec)
            .filter((r) => {
              const allocated = (s.difficultyByType[r.type] || []).reduce((sum, d) => sum + d.count, 0);
              return allocated > 0 && allocated !== r.count;
            })
            .map((r) => `${s.subjectName} — ${sec.name} — ${r.type}: difficulty split must add up to ${r.count}.`)
        )
      ),
    [subjects]
  );
  const canGenerate = typesBlockers.length === 0 && chaptersBlockers.length === 0 && difficultyBlockers.length === 0;

  function goSetupNext() {
    if (setupBlockers.length > 0) return;
    setStep("types");
  }
  function goTypesNext() {
    if (typesBlockers.length > 0) return;
    setStep("chapters");
  }
  function goChaptersNext() {
    if (chaptersBlockers.length > 0) return;
    setStep(mode === "auto" ? "difficulty" : "pick");
  }

  // ---- Auto-generate path ----
  const [generated, setGenerated] = useState<GeneratedSubject[] | null>(null);
  const [generating, setGenerating] = useState(false);

  // One selection per (subject, SECTION, type) — not per (subject, type).
  // Two sections of the same subject can both want SCQ; keeping them as
  // distinct selections (each tagged with its real sectionId) lets the
  // server tell them apart and stop them drawing the same question twice
  // (see app/api/cmds/tests/auto/route.ts).
  function buildSelections() {
    return subjects.flatMap((s) =>
      s.sections.flatMap((sec) =>
        activeTypeRows(sec).map((t) => ({
          subjectBoardId: s.subjectId,
          subjectName: s.subjectName,
          sectionId: sec.id,
          chapterBoardIds: effectiveChapterBoardIds(s),
          type: t.type,
          count: t.count,
          positive: t.positive,
          negative: t.negative,
          publishedFilter: s.publishedFilter,
          difficulty: s.difficultyByType[t.type]?.filter((d) => d.count > 0),
        }))
      )
    );
  }

  async function generate() {
    setError("");
    setGenerating(true);
    try {
      const res = await fetch("/api/cmds/tests/auto", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ selections: buildSelections() }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Generate failed");
        return;
      }
      setGenerated(d.subjects || []);
      setStep("review");
    } finally {
      setGenerating(false);
    }
  }

  async function replaceQuestion(subjectBoardId: string, qId: string) {
    if (!generated) return;
    const subjIdx = generated.findIndex((s) => s.subjectBoardId === subjectBoardId);
    if (subjIdx === -1) return;
    const subj = generated[subjIdx];
    const q = subj.questions.find((x) => x.id === qId);
    if (!q) return;
    const cfg = subjects.find((s) => s.subjectId === subjectBoardId);
    if (!cfg) return;

    const excludeIds = generated.flatMap((s) => s.questions.map((x) => x.id));
    const res = await fetch("/api/cmds/tests/auto", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        replace: {
          subjectBoardId,
          subjectName: subj.subjectName,
          sectionId: q.sectionId,
          chapterBoardIds: effectiveChapterBoardIds(cfg),
          type: q.type,
          positive: 0,
          negative: 0,
          publishedFilter: cfg.publishedFilter,
          difficulty: q.difficulty ? [{ level: q.difficulty, count: 1 }] : undefined,
          excludeIds,
        },
      }),
    });
    const d = await res.json().catch(() => ({}));
    if (!res.ok || !d.question) {
      setError(d.error || "No replacement question available");
      return;
    }
    setGenerated((prev) =>
      prev
        ? prev.map((s, i) =>
            i === subjIdx
              ? { ...s, questions: s.questions.map((x) => (x.id === qId ? { ...d.question, sectionId: q.sectionId } : x)) }
              : s
          )
        : prev
    );
  }

  function removeGeneratedQuestion(subjectBoardId: string, qId: string) {
    setGenerated((prev) =>
      prev
        ? prev.map((s) =>
            s.subjectBoardId === subjectBoardId ? { ...s, questions: s.questions.filter((x) => x.id !== qId) } : s
          )
        : prev
    );
  }

  // One physical section per admin-defined Section (not per type) — a
  // section's typeRows become its typeMarks map, so a section mixing
  // SCQ+MCQ keeps each type's own marks (matches a real test doc's
  // metadata[].details[], see app/api/cmds/tests/route.ts). Question ->
  // section ownership is looked up EXACTLY via sectionId (never inferred
  // from type — two sections can share a type, e.g. both draw SCQ, and
  // type-based inference silently duplicated the same question into both).
  function sectionsForSave(idsBySectionId: (sectionId: string) => string[]) {
    return subjects.flatMap((s) =>
      s.sections
        .map((sec) => {
          const rows = activeTypeRows(sec);
          if (rows.length === 0) return null;
          const questionIds = idsBySectionId(sec.id);
          if (questionIds.length === 0) return null;
          const typeMarks = Object.fromEntries(rows.map((r) => [r.type, { positive: r.positive, negative: r.negative }]));
          const name = subjects.length > 1 || s.sections.length > 1 ? `${s.subjectName} - ${sec.name}` : sec.name;
          return { name, questionIds, typeMarks };
        })
        .filter((x): x is { name: string; questionIds: string[]; typeMarks: Record<string, { positive: number; negative: number }> } => !!x)
    );
  }

  async function saveAuto() {
    if (!generated) return;
    setError("");
    setSaving(true);
    try {
      const unpublishedIds = generated.flatMap((s) => s.questions.filter((q) => !q.published).map((q) => q.id));
      if (unpublishedIds.length > 0) {
        await fetch("/api/cmds/publish", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ids: unpublishedIds, userId: session?.id }),
        });
      }
      const allGenerated = generated.flatMap((g) => g.questions);
      const sections = sectionsForSave((sectionId) => allGenerated.filter((q) => q.sectionId === sectionId).map((q) => q.id));
      await createTest(sections);
    } finally {
      setSaving(false);
    }
  }

  const totalGenerated = useMemo(
    () => (generated ? generated.reduce((sum, s) => sum + s.questions.length, 0) : 0),
    [generated]
  );

  // ---- Manual-pick path ----
  const [poolBySubject, setPoolBySubject] = useState<Record<string, LibQuestion[]>>({});
  const [poolLoadingBySubject, setPoolLoadingBySubject] = useState<Record<string, boolean>>({});
  const [manualPicked, setManualPicked] = useState<Record<string, string[]>>({});
  const [pickError, setPickError] = useState("");

  useEffect(() => {
    if (step !== "pick") return;
    subjects.forEach((s) => {
      setPoolLoadingBySubject((prev) => ({ ...prev, [s.subjectId]: true }));
      const chapterIds = effectiveChapterBoardIds(s);
      const qs = chapterIds.length ? `?boardIds=${chapterIds.join(",")}` : "";
      fetch(`/api/cmds/tests${qs}`)
        .then((r) => r.json())
        .then((d) =>
          setPoolBySubject((prev) => ({
            ...prev,
            [s.subjectId]: (d.questions || []).filter((q: LibQuestion) => q.hasKey),
          }))
        )
        .catch(() => {})
        .finally(() => setPoolLoadingBySubject((prev) => ({ ...prev, [s.subjectId]: false })));
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step]);

  // Keyed by SECTION id, not subject id — a question can only ever belong
  // to one section of the test. Bug found live: this used to be keyed by
  // subject, so the exact same question checked under "Section 1" showed as
  // checked under "Section 2" too (both read/wrote the same flat array),
  // and saving silently put it in both sections at once.
  function toggleManualPick(subjectId: string, sectionId: string, qId: string, qType: string) {
    setPickError("");
    const subj = subjects.find((s) => s.subjectId === subjectId);
    const otherSectionIds = (subj?.sections || []).filter((s) => s.id !== sectionId).map((s) => s.id);
    const usedElsewhere = otherSectionIds.some((sid) => (manualPicked[sid] || []).includes(qId));
    if (usedElsewhere) {
      setPickError("This question is already used in another section of this subject.");
      return;
    }
    setManualPicked((prev) => {
      const current = prev[sectionId] || [];
      if (current.includes(qId)) return { ...prev, [sectionId]: current.filter((x) => x !== qId) };
      const cap = subj?.sections.find((s) => s.id === sectionId)?.typeRows.find((r) => r.type === qType)?.count || 0;
      const poolById = new Map((poolBySubject[subjectId] || []).map((q) => [q.id, q]));
      const gotForType = current.filter((id) => poolById.get(id)?.type === qType).length;
      if (cap > 0 && gotForType >= cap) {
        setPickError(`${qType} target is ${cap} — already reached. Raise it in Subjects & Types or remove one first.`);
        return prev;
      }
      return { ...prev, [sectionId]: [...current, qId] };
    });
  }

  const totalManualPicked = useMemo(() => Object.values(manualPicked).reduce((n, arr) => n + arr.length, 0), [manualPicked]);

  async function submitManualPick() {
    setError("");
    if (totalManualPicked === 0) return setError("Select at least one question.");
    setSaving(true);
    try {
      const sections = sectionsForSave((sectionId) => manualPicked[sectionId] || []);
      await createTest(sections);
    } finally {
      setSaving(false);
    }
  }

  // ---- Shared create call ----
  async function createTest(
    sections: { name: string; questionIds: string[]; typeMarks: Record<string, { positive: number; negative: number }> }[]
  ) {
    const res = await fetch("/api/cmds/tests", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: name.trim(),
        code: code.trim(),
        durationMin,
        sections,
        userId: session?.id,
        password: password.trim(),
        enablePartialMarks,
        enableSectionLocking,
        autoResumeTest,
        resultVisibility,
        folderId: folderId || undefined,
      }),
    });
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Failed to save test");
      return;
    }
    router.push(backHref);
  }

  if (!session) return null;

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
        <h1 className="text-2xl font-semibold text-slate-800">Create Test</h1>
        <p className="mt-1 text-slate-500">
          It becomes attemptable and auto-graded in the learn app immediately.
        </p>
        {folderName && (
          <p className="mt-1 text-sm text-slate-400">
            This test will be added to <span className="font-medium text-slate-600">{folderName}</span>.
          </p>
        )}

        {step !== "setup" && (
          <div className="mt-5 flex flex-wrap items-center gap-1 text-xs">
            <button onClick={() => setStep("setup")} className="rounded-full bg-emerald-100 px-3 py-1 text-emerald-700">
              Setup
            </button>
            {sequence.map((s) => (
              <button
                key={s}
                onClick={() => setStep(s)}
                className={`rounded-full px-3 py-1 ${
                  step === s
                    ? "bg-slate-800 text-white"
                    : sequence.indexOf(s) < sequence.indexOf(step)
                    ? "bg-emerald-100 text-emerald-700"
                    : "bg-slate-100 text-slate-400"
                }`}
              >
                {STEP_LABEL[s]}
              </button>
            ))}
          </div>
        )}

        {error && (
          <div className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">{error}</div>
        )}

        {step === "setup" && (
          <div className="mt-6 space-y-6">
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">Test name</label>
              <input
                value={name}
                onChange={(e) => {
                  const v = e.target.value;
                  setName(v);
                  if (!codeTouched) setCode(slugifyCode(v));
                }}
                placeholder="e.g. Physics — Weekly Practice #2"
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />

              <div className="mt-4">
                <label className="block text-sm font-medium text-slate-700">Code</label>
                <input
                  value={code}
                  onChange={(e) => {
                    setCodeTouched(true);
                    setCode(e.target.value.toUpperCase());
                  }}
                  placeholder="e.g. PHYSICS-WEEKLY-2"
                  className="mt-2 w-full max-w-xs rounded-md border border-slate-300 px-3 py-1.5 font-mono text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div className="mt-4">
                <label className="block text-sm font-medium text-slate-700">Duration (min)</label>
                <input
                  type="number"
                  min={1}
                  value={durationMin}
                  onChange={(e) => setDurationMin(Math.max(1, Number(e.target.value) || 1))}
                  className="mt-2 w-full max-w-[140px] rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-800"
                />
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

            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">How do you want to pick the questions?</label>
              <p className="mt-1 text-xs text-slate-400">
                Both ways use the same Subjects &amp; Types grid and Chapter selection next — this only decides the
                final step.
              </p>
              <div className="mt-3 grid gap-3 sm:grid-cols-2">
                <label
                  className={`flex cursor-pointer flex-col gap-2 rounded-lg border-2 p-4 transition ${
                    mode === "manual" ? "border-blue-500 bg-blue-50/60 ring-1 ring-blue-200" : "border-slate-200 hover:border-slate-300"
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-2xl">☑️</span>
                    <input type="radio" name="mode" checked={mode === "manual"} onChange={() => setMode("manual")} className="h-4 w-4 accent-blue-600" />
                  </div>
                  <span className="text-sm font-semibold text-slate-800">Pick questions manually</span>
                  <span className="text-xs text-slate-500">
                    After setting the Number of Questions / Marks grid per type, you check off the exact questions
                    yourself — the counts become a cap, not an auto-pick.
                  </span>
                </label>
                <label
                  className={`flex cursor-pointer flex-col gap-2 rounded-lg border-2 p-4 transition ${
                    mode === "auto" ? "border-blue-500 bg-blue-50/60 ring-1 ring-blue-200" : "border-slate-200 hover:border-slate-300"
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-2xl">⚙️</span>
                    <input type="radio" name="mode" checked={mode === "auto"} onChange={() => setMode("auto")} className="h-4 w-4 accent-blue-600" />
                  </div>
                  <span className="text-sm font-semibold text-slate-800">Auto-generate</span>
                  <span className="text-xs text-slate-500">
                    Same grid, but the system draws the questions for you (optionally by difficulty), then you can
                    Replace/Remove individual ones before saving (Instant Test Generator).
                  </span>
                </label>
              </div>
            </div>

            {setupBlockers.length > 0 && <p className="text-xs text-amber-600">{setupBlockers.join(" ")}</p>}

            <div className="flex gap-3">
              <button
                onClick={goSetupNext}
                disabled={setupBlockers.length > 0}
                className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
              >
                Next
              </button>
              <Link href={backHref} className="rounded-md border border-slate-300 px-5 py-2.5 text-sm text-slate-600 hover:bg-slate-100">
                Cancel
              </Link>
            </div>
          </div>
        )}

        {step === "types" && (
          <div className="mt-6 space-y-5">
            <SubjectChecklist allSubjects={allSubjects} chosen={subjects.map((s) => s.subjectId)} onToggle={toggleSubject} />
            <p className="text-xs text-slate-400">
              {mode === "manual"
                ? "Number of Questions here sets the cap you may pick per type in the last step. Add more Sections (e.g. Section 1, Section 2) to group different question types under different marking schemes."
                : "Number of Questions here sets how many the system draws per type. Add more Sections (e.g. Section 1, Section 2) to group different question types under different marking schemes."}
            </p>
            {subjects.length > 0 &&
              subjects.filter((s) => allActiveRows(s).length === 0).length > 0 && (
                <div className="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  Enter a count for at least one question type for:{" "}
                  {subjects.filter((s) => allActiveRows(s).length === 0).map((s) => s.subjectName).join(", ")}.
                </div>
              )}
            {subjects.map((s) => (
              <SubjectTypesPanel
                key={s.subjectId}
                subject={s}
                onChange={(patch) => updateSubject(s.subjectId, patch)}
                onRemove={() => removeSubject(s.subjectId)}
                onCopyToOthers={() => copyFormatToOthers(s.subjectId)}
                canCopy={subjects.length > 1}
              />
            ))}
            {subjects.length > 0 && (
              <div className="flex items-center justify-between rounded border border-slate-200 bg-slate-50 px-4 py-3">
                <span className="text-sm font-medium text-slate-700">Total Questions in the test</span>
                <span className="text-lg font-semibold text-slate-800">
                  {subjects.reduce((sum, s) => sum + allActiveRows(s).reduce((n, r) => n + r.count, 0), 0)}
                </span>
              </div>
            )}
            <StepNav
              onBack={() => setStep("setup")}
              onNext={goTypesNext}
              nextDisabled={typesBlockers.length > 0}
              nextTitle={typesBlockers.length > 0 ? typesBlockers.join(" ") : undefined}
            />
          </div>
        )}

        {step === "chapters" && (
          <div className="mt-6 space-y-5">
            {chaptersBlockers.length > 0 && (
              <div className="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                Select at least one chapter for: {chaptersBlockers.map((m) => m.split(":")[0]).join(", ")}.
              </div>
            )}
            {subjects.map((s) => (
              <SubjectChaptersPanel key={s.subjectId} subject={s} onChange={(patch) => updateSubject(s.subjectId, patch)} />
            ))}
            <StepNav
              onBack={() => setStep("types")}
              onNext={goChaptersNext}
              nextDisabled={chaptersBlockers.length > 0}
              nextTitle={chaptersBlockers.length > 0 ? chaptersBlockers.join(" ") : undefined}
            />
          </div>
        )}

        {step === "difficulty" && (
          <div className="mt-6 space-y-5">
            <p className="text-sm text-slate-500">
              Optionally split each question type's count by difficulty. Skip to draw from any difficulty.
            </p>
            {!canGenerate && (
              <div className="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                <div className="font-medium">Fix these before generating:</div>
                <ul className="mt-1 list-disc pl-5">
                  {[...typesBlockers, ...chaptersBlockers, ...difficultyBlockers].map((b) => (
                    <li key={b}>{b}</li>
                  ))}
                </ul>
              </div>
            )}
            {subjects.map((s) => (
              <SubjectDifficultyPanel key={s.subjectId} subject={s} onChange={(patch) => updateSubject(s.subjectId, patch)} />
            ))}
            <div className="mt-8 flex items-center justify-between border-t border-slate-100 pt-5">
              <button onClick={() => setStep("chapters")} className="rounded px-4 py-2 text-sm text-slate-500 hover:bg-slate-100">
                Back
              </button>
              <div className="flex gap-2">
                <button
                  onClick={generate}
                  disabled={!canGenerate || generating}
                  className="rounded border border-slate-300 px-5 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:opacity-40"
                >
                  Skip & Generate Now
                </button>
                <button
                  onClick={generate}
                  disabled={!canGenerate || generating}
                  className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-40"
                >
                  {generating ? "Generating…" : "Generate Test"}
                </button>
              </div>
            </div>
          </div>
        )}

        {step === "review" && (
          <div className="mt-6">
            {!generated ? (
              <div className="rounded border border-dashed border-slate-200 py-12 text-center text-sm text-slate-400">
                Click Generate to pull questions from the pool.
              </div>
            ) : (
              <div className="space-y-6">
                <p className="text-sm text-slate-500">
                  {totalGenerated} question(s) generated across {generated.length} subject(s).
                </p>
                {generated.map((g) => (
                  <div key={g.subjectBoardId} className="rounded border border-slate-200">
                    <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-4 py-2">
                      <span className="text-sm font-medium text-slate-700">{g.subjectName}</span>
                      <span className="text-xs text-slate-500">
                        {g.questions.length} / {g.requested}
                      </span>
                    </div>
                    <ul className="divide-y divide-slate-100">
                      {g.questions.map((q) => (
                        <li key={q.id} className="flex items-center justify-between gap-3 px-4 py-2">
                          <div className="min-w-0 flex-1">
                            <div className="truncate text-sm text-slate-700">
                              {q.text ? <MathText>{q.text}</MathText> : "(no text)"}
                            </div>
                            <div className="text-xs text-slate-400">
                              {q.type} {q.difficulty ? `· ${q.difficulty}` : ""} {q.chapter ? `· ${q.chapter}` : ""}{" "}
                              {!q.published && "· draft"}
                            </div>
                          </div>
                          <div className="flex shrink-0 gap-2 text-xs">
                            <button onClick={() => replaceQuestion(g.subjectBoardId, q.id)} className="text-blue-600 hover:underline">
                              Replace
                            </button>
                            <button onClick={() => removeGeneratedQuestion(g.subjectBoardId, q.id)} className="text-red-500 hover:underline">
                              Remove
                            </button>
                          </div>
                        </li>
                      ))}
                      {g.questions.length === 0 && (
                        <li className="px-4 py-6 text-center text-sm text-slate-400">No questions in this subject.</li>
                      )}
                    </ul>
                  </div>
                ))}
              </div>
            )}
            <div className="mt-8 flex items-center justify-between border-t border-slate-100 pt-5">
              <button onClick={() => setStep("difficulty")} className="rounded px-4 py-2 text-sm text-slate-500 hover:bg-slate-100">
                Back
              </button>
              {generated && (
                <button
                  onClick={saveAuto}
                  disabled={saving || totalGenerated === 0}
                  className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                >
                  {saving ? "Saving…" : "Save Test"}
                </button>
              )}
            </div>
          </div>
        )}

        {step === "pick" && (
          <div className="mt-6 space-y-6">
            <p className="text-sm text-slate-500">
              Only published questions with an answer key are shown, filtered to the chapters and types you chose.
            </p>
            {subjects.map((s) => {
              const pool = poolBySubject[s.subjectId] || [];
              const subjectPickedCount = s.sections.reduce((n, sec) => n + (manualPicked[sec.id] || []).length, 0);
              return (
                <div key={s.subjectId} className="rounded-xl bg-white p-5 ring-1 ring-black/5">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-slate-700">{s.subjectName}</span>
                    <span className="text-sm text-slate-500">{subjectPickedCount} selected</span>
                  </div>

                  <div className="mt-3 space-y-4">
                    {s.sections.map((sec) => {
                      const rows = activeTypeRows(sec);
                      if (rows.length === 0) return null;
                      const rowTypes = new Set(rows.map((r) => r.type));
                      const secPool = pool.filter((q) => rowTypes.has(q.type));
                      const pickedIds = manualPicked[sec.id] || [];
                      const otherSectionIds = s.sections.filter((x) => x.id !== sec.id).map((x) => x.id);
                      const usedElsewhere = new Set(otherSectionIds.flatMap((sid) => manualPicked[sid] || []));
                      return (
                        <div key={sec.id} className="rounded border border-slate-100 p-3">
                          <div className="text-sm font-semibold text-slate-700">{sec.name}</div>
                          <div className="mt-2 flex flex-wrap gap-3 rounded-md bg-slate-50 p-2 text-xs">
                            {rows.map((r) => {
                              const got = pickedIds.filter((id) => secPool.find((q) => q.id === id)?.type === r.type).length;
                              const met = got >= r.count;
                              return (
                                <span key={r.type} className={met ? "font-medium text-emerald-600" : "text-slate-600"}>
                                  {r.type}: {got}/{r.count}
                                </span>
                              );
                            })}
                          </div>

                          {poolLoadingBySubject[s.subjectId] && <div className="mt-2 text-xs text-slate-400">Loading…</div>}
                          {!poolLoadingBySubject[s.subjectId] && secPool.length === 0 && (
                            <div className="mt-2 rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-700 ring-1 ring-amber-200">
                              No published questions of these types in the chosen chapters.
                            </div>
                          )}

                          <div className="mt-2 space-y-1.5">
                            {secPool.map((q) => {
                              const disabled = usedElsewhere.has(q.id);
                              return (
                                <label
                                  key={q.id}
                                  title={disabled ? "Already used in another section of this subject" : undefined}
                                  className={`flex items-start gap-3 rounded-md border border-slate-200 px-3 py-2 ${
                                    disabled ? "cursor-not-allowed opacity-50" : "cursor-pointer hover:bg-slate-50"
                                  }`}
                                >
                                  <input
                                    type="checkbox"
                                    className="mt-1 h-4 w-4 accent-blue-600"
                                    checked={pickedIds.includes(q.id)}
                                    disabled={disabled}
                                    onChange={() => toggleManualPick(s.subjectId, sec.id, q.id, q.type)}
                                  />
                                  <span className="flex-1">
                                    <span className="text-slate-800">{q.text ? <MathText>{q.text}</MathText> : "(no text)"}</span>
                                    <span className="mt-0.5 block text-xs text-slate-400">
                                      {q.type} · {q.options} options · {q.difficulty || "—"}
                                      {disabled ? " · used in another section" : ""}
                                    </span>
                                  </span>
                                </label>
                              );
                            })}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
            {pickError && (
              <p className="rounded-md bg-amber-50 px-3 py-1.5 text-xs text-amber-700 ring-1 ring-amber-200">{pickError}</p>
            )}
            <div className="flex items-center justify-between border-t border-slate-100 pt-5">
              <button onClick={() => setStep("chapters")} className="rounded px-4 py-2 text-sm text-slate-500 hover:bg-slate-100">
                Back
              </button>
              <button
                onClick={submitManualPick}
                disabled={saving || totalManualPicked === 0}
                className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
              >
                {saving ? "Creating…" : `Create Test (${totalManualPicked} question${totalManualPicked === 1 ? "" : "s"})`}
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

function StepNav({
  onBack,
  onNext,
  nextDisabled,
  nextTitle,
}: {
  onBack: () => void;
  onNext: () => void;
  nextDisabled?: boolean;
  nextTitle?: string;
}) {
  return (
    <div className="mt-8 flex items-center justify-between border-t border-slate-100 pt-5">
      <button onClick={onBack} className="rounded px-4 py-2 text-sm text-slate-500 hover:bg-slate-100">
        Back
      </button>
      <button
        onClick={onNext}
        disabled={nextDisabled}
        title={nextTitle}
        className="rounded bg-slate-800 px-5 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-40"
      >
        Next
      </button>
    </div>
  );
}

function SubjectChecklist({
  allSubjects,
  chosen,
  onToggle,
}: {
  allSubjects: Node[];
  chosen: string[];
  onToggle: (id: string) => void;
}) {
  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-slate-600">
        Choose Subjects <span className="text-red-500">*</span>
      </label>
      <div className="grid max-h-72 grid-cols-2 gap-x-4 gap-y-1 overflow-y-auto rounded border border-slate-200 p-3 sm:grid-cols-3">
        {allSubjects.length === 0 && <p className="col-span-full text-sm text-slate-400">Loading subjects…</p>}
        {allSubjects.map((s) => (
          <label key={s.id} className="flex cursor-pointer items-center gap-2 rounded px-1 py-1 text-sm text-slate-600 hover:bg-slate-50">
            <input type="checkbox" checked={chosen.includes(s.id)} onChange={() => onToggle(s.id)} className="accent-emerald-600" />
            {s.name}
          </label>
        ))}
      </div>
      {chosen.length === 0 && <p className="mt-1 text-sm text-slate-400">Select at least one subject to continue.</p>}
    </div>
  );
}

function SubjectTypesPanel({
  subject,
  onChange,
  onRemove,
  onCopyToOthers,
  canCopy,
}: {
  subject: SubjectConfig;
  onChange: (patch: Partial<SubjectConfig>) => void;
  onRemove: () => void;
  onCopyToOthers: () => void;
  canCopy: boolean;
}) {
  function updateSection(sectionId: string, patch: Partial<TestSection>) {
    onChange({ sections: subject.sections.map((sec) => (sec.id === sectionId ? { ...sec, ...patch } : sec)) });
  }
  function setRow(sectionId: string, type: string, patch: Partial<TypeRow>) {
    const sec = subject.sections.find((s) => s.id === sectionId);
    if (!sec) return;
    const exists = sec.typeRows.some((r) => r.type === type);
    const nextRows = exists
      ? sec.typeRows.map((r) => (r.type === type ? { ...r, ...patch } : r))
      : [...sec.typeRows, { type, count: 0, positive: 0, negative: 0, ...patch }];
    updateSection(sectionId, { typeRows: nextRows });
  }
  function addExtraType(sectionId: string, type: string) {
    const sec = subject.sections.find((s) => s.id === sectionId);
    if (!sec || sec.typeRows.some((r) => r.type === type)) return;
    updateSection(sectionId, { typeRows: [...sec.typeRows, { type, count: 0, positive: 0, negative: 0 }] });
  }
  function removeExtraType(sectionId: string, type: string) {
    const sec = subject.sections.find((s) => s.id === sectionId);
    if (!sec) return;
    updateSection(sectionId, { typeRows: sec.typeRows.filter((r) => r.type !== type) });
  }
  function addSection() {
    onChange({ sections: [...subject.sections, defaultSection(`Section ${subject.sections.length + 1}`)] });
  }
  function removeSection(sectionId: string) {
    onChange({ sections: subject.sections.filter((s) => s.id !== sectionId) });
  }

  const totalCount = subject.sections.reduce((sum, sec) => sum + sec.typeRows.reduce((n, r) => n + r.count, 0), 0);

  return (
    <div className="rounded border border-slate-200 p-4">
      <div className="flex items-center justify-between">
        <span className="font-medium text-slate-700">{subject.subjectName}</span>
        <div className="flex items-center gap-3 text-xs">
          {canCopy && (
            <button onClick={onCopyToOthers} className="text-blue-600 hover:underline">
              Copy format to other subjects
            </button>
          )}
          <button onClick={onRemove} className="text-red-500 hover:underline">
            Remove
          </button>
        </div>
      </div>

      <div className="mt-3 space-y-4">
        {subject.sections.map((sec) => {
          const visibleTypes = [...CORE_TYPES, ...sec.typeRows.map((r) => r.type).filter((t) => EXTRA_TYPES.includes(t))];
          const hiddenExtras = EXTRA_TYPES.filter((t) => !visibleTypes.includes(t));
          const secTotal = sec.typeRows.reduce((n, r) => n + r.count, 0);
          const rowFor = (type: string): TypeRow => sec.typeRows.find((r) => r.type === type) || { type, count: 0, positive: 0, negative: 0 };
          return (
            <div key={sec.id} className="rounded border border-slate-100 bg-slate-50/60 p-3">
              <div className="flex items-center justify-between gap-2">
                <input
                  value={sec.name}
                  onChange={(e) => updateSection(sec.id, { name: e.target.value })}
                  className="rounded border border-slate-300 bg-white px-2 py-1 text-sm font-medium text-slate-700 focus:border-blue-500 focus:outline-none"
                />
                {subject.sections.length > 1 && (
                  <button onClick={() => removeSection(sec.id)} className="shrink-0 text-xs text-red-500 hover:underline">
                    Remove section
                  </button>
                )}
              </div>

              <table className="mt-2 w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-xs font-medium text-slate-500">
                    <th className="py-2 pr-3">Type of Question</th>
                    <th className="px-3 py-2">Number of Questions</th>
                    <th className="px-3 py-2">Marks per Question</th>
                    <th className="px-3 py-2">Negative Marks per Question</th>
                    <th className="w-6" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {visibleTypes.map((type) => {
                    const r = rowFor(type);
                    const isExtra = EXTRA_TYPES.includes(type);
                    return (
                      <tr key={type}>
                        <td className="py-2 pr-3 text-slate-700">{type}</td>
                        <td className="px-3 py-2">
                          <input
                            type="number"
                            min={0}
                            value={r.count}
                            onChange={(e) => setRow(sec.id, type, { count: Math.max(0, Number(e.target.value) || 0) })}
                            className="w-20 rounded border border-slate-300 bg-white px-2 py-1"
                          />
                        </td>
                        <td className="px-3 py-2">
                          <input
                            type="number"
                            min={0}
                            value={r.positive}
                            onChange={(e) => setRow(sec.id, type, { positive: Math.max(0, Number(e.target.value) || 0) })}
                            className="w-20 rounded border border-slate-300 bg-white px-2 py-1"
                          />
                        </td>
                        <td className="px-3 py-2">
                          <input
                            type="number"
                            min={0}
                            value={r.negative}
                            onChange={(e) => setRow(sec.id, type, { negative: Math.max(0, Number(e.target.value) || 0) })}
                            className="w-20 rounded border border-slate-300 bg-white px-2 py-1"
                          />
                        </td>
                        <td>
                          {isExtra && (
                            <button onClick={() => removeExtraType(sec.id, type)} title="Remove this question type" className="text-slate-400 hover:text-red-500">
                              ✕
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>

              {hiddenExtras.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-2 text-xs">
                  {hiddenExtras.map((t) => (
                    <button key={t} onClick={() => addExtraType(sec.id, t)} className="rounded border border-slate-300 bg-white px-2 py-1 text-slate-600 hover:bg-slate-50">
                      + {t}
                    </button>
                  ))}
                </div>
              )}

              <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
                <span>Section total</span>
                <span className="font-medium text-slate-700">{secTotal}</span>
              </div>
            </div>
          );
        })}
      </div>

      <button onClick={addSection} className="mt-3 rounded border border-dashed border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-50">
        + Add Section
      </button>

      <div className="mt-3 flex items-center justify-between border-t border-slate-100 pt-2 text-sm">
        <span className="font-medium text-slate-700">Total Questions</span>
        <span className="font-semibold text-slate-800">{totalCount}</span>
      </div>
    </div>
  );
}

function SubjectChaptersPanel({
  subject,
  onChange,
}: {
  subject: SubjectConfig;
  onChange: (patch: Partial<SubjectConfig>) => void;
}) {
  const [openConceptsFor, setOpenConceptsFor] = useState<string | null>(null);
  const [concepts, setConcepts] = useState<Record<string, Node[]>>({});
  const [loadingConcepts, setLoadingConcepts] = useState(false);

  function toggle(id: string) {
    const has = subject.selectedChapterIds.includes(id);
    if (has) {
      const nextConceptIds = { ...subject.selectedConceptIds };
      delete nextConceptIds[id];
      onChange({
        selectedChapterIds: subject.selectedChapterIds.filter((x) => x !== id),
        selectedConceptIds: nextConceptIds,
      });
    } else {
      onChange({ selectedChapterIds: [...subject.selectedChapterIds, id] });
    }
  }

  function toggleConceptsPanel(chapterId: string) {
    if (openConceptsFor === chapterId) {
      setOpenConceptsFor(null);
      return;
    }
    setOpenConceptsFor(chapterId);
    if (concepts[chapterId]) return;
    setLoadingConcepts(true);
    fetch(`/api/cmds/tools/boards?parentId=${chapterId}&type=SUBTOPIC`)
      .then((r) => r.json())
      .then((d) => setConcepts((prev) => ({ ...prev, [chapterId]: d.nodes || [] })))
      .finally(() => setLoadingConcepts(false));
  }

  function toggleConcept(chapterId: string, conceptId: string) {
    const current = subject.selectedConceptIds[chapterId] || [];
    const nextConcepts = current.includes(conceptId) ? current.filter((x) => x !== conceptId) : [...current, conceptId];
    const nextChapterIds = subject.selectedChapterIds.includes(chapterId)
      ? subject.selectedChapterIds
      : [...subject.selectedChapterIds, chapterId];
    onChange({
      selectedConceptIds: { ...subject.selectedConceptIds, [chapterId]: nextConcepts },
      selectedChapterIds: nextChapterIds,
    });
  }

  return (
    <div className="rounded border border-slate-200 p-4">
      <div className="flex items-center justify-between">
        <span className="font-medium text-slate-700">{subject.subjectName}</span>
        <select
          value={subject.publishedFilter}
          onChange={(e) => onChange({ publishedFilter: e.target.value as PublishedFilter })}
          className="rounded border border-slate-300 px-2 py-1 text-xs"
        >
          <option value="PUBLISHED">Published</option>
          <option value="UNPUBLISHED">Unpublished</option>
          <option value="BOTH">Both</option>
        </select>
      </div>
      <div className="mt-3 max-h-72 overflow-y-auto rounded border border-slate-100">
        {subject.chapters.length === 0 ? (
          <div className="px-3 py-4 text-center text-xs text-slate-400">No chapters under this subject</div>
        ) : (
          subject.chapters.map((c) => {
            const selectedConcepts = subject.selectedConceptIds[c.id] || [];
            return (
              <div key={c.id} className="border-b border-slate-50 last:border-0">
                <div className="flex items-center gap-2 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50">
                  <label className="flex flex-1 cursor-pointer items-center gap-2">
                    <input type="checkbox" checked={subject.selectedChapterIds.includes(c.id)} onChange={() => toggle(c.id)} className="accent-emerald-600" />
                    {c.name}
                    {selectedConcepts.length > 0 && (
                      <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-medium text-emerald-700">
                        {selectedConcepts.length} concept(s) only
                      </span>
                    )}
                  </label>
                  <button type="button" onClick={() => toggleConceptsPanel(c.id)} className="shrink-0 text-xs text-blue-600 hover:underline">
                    {openConceptsFor === c.id ? "Hide concepts" : "+ Concepts"}
                  </button>
                </div>
                {openConceptsFor === c.id && (
                  <div className="ml-6 mb-1 rounded border border-slate-100 bg-slate-50">
                    {loadingConcepts && !concepts[c.id] ? (
                      <div className="px-3 py-2 text-xs text-slate-400">Loading concepts…</div>
                    ) : (concepts[c.id] || []).length === 0 ? (
                      <div className="px-3 py-2 text-xs text-slate-400">
                        No concepts under this chapter — questions from anywhere in it will be used.
                      </div>
                    ) : (
                      concepts[c.id].map((s) => (
                        <label key={s.id} className="flex cursor-pointer items-center gap-2 border-b border-slate-100 px-3 py-1 text-xs text-slate-600 last:border-0 hover:bg-white">
                          <input type="checkbox" checked={selectedConcepts.includes(s.id)} onChange={() => toggleConcept(c.id, s.id)} className="accent-emerald-600" />
                          {s.name}
                        </label>
                      ))
                    )}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
      {Object.values(subject.selectedConceptIds).some((v) => v.length > 0) && (
        <p className="mt-2 text-xs text-slate-400">Chapters with concepts checked will only draw questions from those concepts.</p>
      )}
    </div>
  );
}

function SubjectDifficultyPanel({
  subject,
  onChange,
}: {
  subject: SubjectConfig;
  onChange: (patch: Partial<SubjectConfig>) => void;
}) {
  // Clamped so a type's difficulty split can never add up to more than the
  // count set on the Subjects & Types step — otherwise the split silently
  // overrides that count instead of dividing it (real bug found live: typing
  // 4 into Easy for a row that only asked for 1 question generated 4).
  function setLevelCount(type: string, level: string, count: number, cap: number) {
    const existing = subject.difficultyByType[type] || [];
    const otherAllocated = existing.filter((d) => d.level !== level).reduce((sum, d) => sum + d.count, 0);
    const clamped = Math.max(0, Math.min(count, cap - otherAllocated));
    const next = DIFFICULTIES.map((lvl) => {
      const prev = existing.find((d) => d.level === lvl)?.count || 0;
      return { level: lvl, count: lvl === level ? clamped : prev };
    });
    onChange({ difficultyByType: { ...subject.difficultyByType, [type]: next } });
  }

  return (
    <div className="rounded border border-slate-200 p-4">
      <span className="font-medium text-slate-700">{subject.subjectName}</span>
      <div className="mt-3 space-y-4">
        {subject.sections.map((sec) => {
          const rows = activeTypeRows(sec);
          if (rows.length === 0) return null;
          return (
            <div key={sec.id}>
              <div className="text-xs font-semibold text-slate-600">{sec.name}</div>
              <div className="mt-2 space-y-3">
                {rows.map((r) => {
                  const drows = subject.difficultyByType[r.type] || [];
                  const allocated = drows.reduce((sum, d) => sum + d.count, 0);
                  const mismatched = allocated > 0 && allocated !== r.count;
                  return (
                    <div key={r.type} className="rounded border border-slate-100 p-3">
                      <div className={`text-xs font-medium ${mismatched ? "text-amber-600" : "text-slate-500"}`}>
                        {r.type} — {r.count} question(s) ({allocated} allocated)
                        {mismatched && ` — add ${r.count - allocated} more or clear to skip`}
                      </div>
                      <div className="mt-2 flex gap-3">
                        {DIFFICULTIES.map((lvl) => (
                          <label key={lvl} className="text-xs text-slate-500">
                            {lvl.charAt(0) + lvl.slice(1).toLowerCase()}
                            <input
                              type="number"
                              min={0}
                              max={r.count}
                              value={drows.find((d) => d.level === lvl)?.count || 0}
                              onChange={(e) => setLevelCount(r.type, lvl, Math.max(0, Number(e.target.value) || 0), r.count)}
                              className="mt-1 block w-16 rounded border border-slate-300 px-2 py-1 text-sm"
                            />
                          </label>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
