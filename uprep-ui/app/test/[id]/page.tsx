"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, type UprepSession } from "@/lib/session";
import MathText from "@/components/MathText";

type Question = {
  id: string;
  content: string;
  type: string;
  options: string[];
  board: string;
};
type TestMeta = {
  id: string;
  name: string;
  durationMin: number;
  totalMarks: number;
  code: string | null;
};

type Phase = "landing" | "attempt" | "result";
type SubmitResult = {
  graded: boolean;
  total: number;
  judgeable: number;
  correct: number;
  ungraded: number;
  error?: string;
};

export default function TestPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [test, setTest] = useState<TestMeta | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [phase, setPhase] = useState<Phase>("landing");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [remaining, setRemaining] = useState(0); // seconds
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<SubmitResult | null>(null);

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
    fetch(`/api/tests/${params.id}?userId=${encodeURIComponent(s.id)}`)
      .then((r) => r.json())
      .then((d) => {
        if (d.error) {
          setError(d.error);
          return;
        }
        setTest(d.test);
        setQuestions(d.questions || []);
      })
      .catch(() => setError("Failed to load test"))
      .finally(() => setLoading(false));
  }, [params.id, router]);

  useEffect(() => {
    if (phase !== "attempt" || remaining <= 0) return;
    const t = setInterval(() => setRemaining((r) => (r <= 1 ? 0 : r - 1)), 1000);
    return () => clearInterval(t);
  }, [phase, remaining]);

  const answeredCount = useMemo(
    () => Object.keys(answers).length,
    [answers]
  );

  function start() {
    setRemaining((test?.durationMin || 0) * 60);
    setPhase("attempt");
  }

  async function submit() {
    if (submitting || !session) return;
    setSubmitting(true);
    const payload = {
      userId: session.id,
      answers: questions.map((q) => ({
        qId: q.id,
        // The backend grades by option INDEX (answer keys are stored as
        // index strings, e.g. ["1"]), so send the selected option's index
        // — not its text.
        answerGiven: answers[q.id] !== undefined ? [String(answers[q.id])] : [],
        timeTaken: 0,
      })),
    };
    try {
      const r = await fetch(`/api/tests/${params.id}/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const d = await r.json();
      setResult(d);
    } catch {
      setResult({
        graded: false,
        total: questions.length,
        judgeable: 0,
        correct: 0,
        ungraded: questions.length,
        error: "Failed to submit",
      });
    } finally {
      setSubmitting(false);
      setPhase("result");
    }
  }

  useEffect(() => {
    if (phase === "attempt" && remaining === 0 && test) submit();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [remaining, phase, test]);

  const mmss = `${String(Math.floor(remaining / 60)).padStart(2, "0")}:${String(
    remaining % 60
  ).padStart(2, "0")}`;

  if (loading)
    return <Centered>Loading test…</Centered>;
  if (error)
    return (
      <Centered>
        <div className="text-red-600">{error}</div>
        <Link href="/learn/library" className="mt-3 text-blue-600 hover:underline">
          ← Back to Library
        </Link>
      </Centered>
    );
  if (!test) return <Centered>Test not found.</Centered>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="mx-auto max-w-3xl px-4 h-14 flex items-center justify-between">
          <Link href="/learn/library" className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold">
              U
            </div>
            <span className="font-semibold text-slate-800">UPrep</span>
          </Link>
          {phase === "attempt" && (
            <span className="rounded-md bg-amber-100 px-3 py-1 text-sm font-medium text-amber-700">
              ⏱ {mmss}
            </span>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        {phase === "landing" && (
          <div className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-black/5">
            <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
              TEST
            </span>
            <h1 className="mt-3 text-2xl font-semibold text-slate-800">
              {test.name}
            </h1>
            {test.code && (
              <p className="mt-1 text-sm text-slate-400">Code: {test.code}</p>
            )}
            <div className="mt-6 grid grid-cols-3 gap-4 text-center">
              <Stat label="Questions" value={String(questions.length)} />
              <Stat label="Duration" value={`${test.durationMin} min`} />
              <Stat label="Total Marks" value={String(test.totalMarks)} />
            </div>
            <button
              onClick={start}
              disabled={questions.length === 0}
              className="mt-8 w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
            >
              {questions.length === 0 ? "No questions available" : "Start Test"}
            </button>
            <Link
              href="/library"
              className="mt-4 block text-center text-sm text-slate-500 hover:text-blue-600"
            >
              ← Back to Library
            </Link>
          </div>
        )}

        {phase === "attempt" && (
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <h1 className="text-lg font-semibold text-slate-800">{test.name}</h1>
              <span className="text-sm text-slate-500">
                {answeredCount}/{questions.length} answered
              </span>
            </div>
            {questions.map((q, idx) => (
              <div
                key={q.id}
                className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-black/5"
              >
                <div className="flex gap-2">
                  <span className="font-semibold text-blue-600">Q{idx + 1}.</span>
                  <MathText className="text-slate-800">{q.content}</MathText>
                </div>
                <div className="mt-4 space-y-2">
                  {q.options.map((opt, oi) => (
                    <label
                      key={oi}
                      className={`flex items-center gap-3 rounded-md border px-3 py-2 cursor-pointer ${
                        answers[q.id] === oi
                          ? "border-blue-500 bg-blue-50"
                          : "border-slate-200 hover:bg-slate-50"
                      }`}
                    >
                      <input
                        type="radio"
                        name={q.id}
                        checked={answers[q.id] === oi}
                        onChange={() =>
                          setAnswers((a) => ({ ...a, [q.id]: oi }))
                        }
                      />
                      <MathText className="text-slate-700">{opt}</MathText>
                    </label>
                  ))}
                </div>
              </div>
            ))}
            <button
              onClick={submit}
              disabled={submitting}
              className="w-full rounded-md bg-emerald-600 py-3 font-semibold text-white transition hover:bg-emerald-700 disabled:opacity-60"
            >
              {submitting ? "Submitting…" : "Submit Test"}
            </button>
          </div>
        )}

        {phase === "result" && (
          <div className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-black/5 text-center">
            {result?.graded ? (
              <>
                <div className="text-4xl">🎯</div>
                <h1 className="mt-3 text-2xl font-semibold text-slate-800">
                  Your Score
                </h1>
                <div className="mt-4 text-5xl font-bold text-emerald-600">
                  {result.correct}
                  <span className="text-2xl text-slate-400">/{result.judgeable}</span>
                </div>
                <p className="mt-2 text-slate-500">
                  {result.correct} correct out of {result.judgeable} graded
                  {result.ungraded > 0 &&
                    ` · ${result.ungraded} pending review`}
                </p>
              </>
            ) : (
              <>
                <div className="text-4xl">✅</div>
                <h1 className="mt-3 text-2xl font-semibold text-slate-800">
                  Test Submitted
                </h1>
                <p className="mt-2 text-slate-500">
                  You answered {answeredCount} of {questions.length} questions.
                </p>
                <p className="mt-1 text-sm text-amber-600">
                  Automatic grading isn’t available for this test yet (answer keys
                  not published).
                </p>
              </>
            )}
            <RatingWidget entityId={params.id} session={session} />

            <div className="mt-6 flex justify-center gap-3">
              <Link
                href="/learn/library"
                className="rounded-md border border-slate-300 px-4 py-2 text-slate-600 hover:bg-slate-100"
              >
                Back to Library
              </Link>
              <Link
                href="/learn/analytics"
                className="rounded-md bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
              >
                My Analytics
              </Link>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

function RatingWidget({
  entityId,
  session,
}: {
  entityId: string;
  session: UprepSession | null;
}) {
  const [mine, setMine] = useState(0);
  const [average, setAverage] = useState(0);
  const [count, setCount] = useState(0);
  const [comment, setComment] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const uid = session?.id || "";
    fetch(`/api/learn/ratings?entityId=${entityId}&userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => {
        setAverage(d.average || 0);
        setCount(d.count || 0);
        if (d.mine) {
          setMine(d.mine.rating || 0);
          setComment(d.mine.comment || "");
        }
      })
      .catch(() => {});
  }, [entityId, session]);

  async function save(rating: number) {
    setMine(rating);
    await fetch("/api/learn/ratings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        entityId,
        entityType: "TEST",
        userId: session?.id,
        userName: [session?.firstName, session?.lastName].filter(Boolean).join(" ") || "Student",
        rating,
        comment,
      }),
    });
    setSaved(true);
  }

  return (
    <div className="mt-8 border-t border-slate-100 pt-6">
      <div className="text-sm font-medium text-slate-600">Rate this test</div>
      <div className="mt-2 flex items-center justify-center gap-1">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            onClick={() => save(n)}
            className={`text-2xl ${n <= mine ? "text-amber-400" : "text-slate-300 hover:text-amber-300"}`}
          >
            ★
          </button>
        ))}
      </div>
      {count > 0 && (
        <div className="mt-1 text-xs text-slate-400">
          Average {average} from {count} rating{count === 1 ? "" : "s"}
        </div>
      )}
      {saved && <div className="mt-2 text-xs text-emerald-600">Thanks for your feedback!</div>}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-slate-50 py-4">
      <div className="text-xl font-semibold text-slate-800">{value}</div>
      <div className="text-xs text-slate-500">{label}</div>
    </div>
  );
}

function Centered({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center text-slate-500">
      {children}
    </div>
  );
}
