// Student-facing marks for one finished attempt.
//
// The legacy grading service stores a verdict (isCorrect) on every
// userquestionattempts row but never fills in `score` — it is 0 on every
// row in production. Marks therefore have to be derived from the test's
// marks map (metadata[].marks: qId -> {positive, negative}), exactly the way
// the admin Test Analytics page does it (app/api/cmds/tests/analytics), so a
// student and their teacher always see the same number:
//   CORRECT  -> +positive
//   INCORRECT -> -negative
//   PARTIAL / ungraded -> 0
//   total is floored at 0
// Skipped questions have no row at all, so "left" = questions in the test
// minus questions with a row.

export type Marks = { positive: number; negative: number };

export type SectionScore = {
  name: string;
  score: number;
  totalMarks: number;
  totalQuestions: number;
  correct: number;
  wrong: number; // includes partly-correct, like admin Analytics' CWL columns
  left: number;
};

export type ScoreSummary = {
  sections: SectionScore[];
  score: number;
  totalMarks: number;
  totalQuestions: number;
  correct: number;
  wrong: number;
  partial: number;
  ungraded: number;
  left: number;
};

export function marksMap(test: any): Record<string, Marks> {
  const m: Record<string, Marks> = {};
  for (const md of test?.metadata || []) {
    if (md?.marks) Object.assign(m, md.marks);
  }
  return m;
}

export function testQIds(test: any): string[] {
  return (test?.metadata || []).flatMap((m: any) => (Array.isArray(m?.qIds) ? m.qIds.map(String) : []));
}

function num(v: any): number {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

// A row with no chosen option is a skipped question, whatever verdict the
// grader stamped on it (the live grader doesn't store these, but a blank
// must never cost negative marks if one does show up).
export function isBlank(row: any): boolean {
  return !Array.isArray(row?.answerGiven) || row.answerGiven.filter((v: any) => String(v ?? "").trim() !== "").length === 0;
}

// Marks one question row earns (before the whole-test floor at 0).
export function questionMarks(row: any, mk: Marks | undefined): number {
  if (!row || !row.isJudgeable || isBlank(row)) return 0;
  const verdict = String(row.isCorrect || "").toUpperCase();
  if (verdict === "CORRECT") return num(mk?.positive);
  if (verdict === "PARTIAL") return 0;
  return -num(mk?.negative);
}

export function summarize(test: any, rows: any[]): ScoreSummary {
  const marks = marksMap(test);
  const qIds = testQIds(test);
  const inTest = new Set(qIds);
  let score = 0;
  let correct = 0;
  let wrong = 0;
  let partial = 0;
  let ungraded = 0;
  const seen = new Set<string>();
  for (const r of rows) {
    const id = String(r.qId);
    if (seen.has(id) || isBlank(r)) continue;
    seen.add(id);
    if (!r.isJudgeable) {
      ungraded++;
      continue;
    }
    const verdict = String(r.isCorrect || "").toUpperCase();
    if (verdict === "CORRECT") correct++;
    else if (verdict === "PARTIAL") partial++;
    else wrong++;
    score += questionMarks(r, marks[id]);
  }
  const totalQuestions = qIds.length || seen.size;
  const answeredInTest = qIds.length ? Array.from(seen).filter((id) => inTest.has(id)).length : seen.size;
  const metaTotal = (test?.metadata || []).reduce((s: number, m: any) => s + num(m?.totalMarks), 0);
  const totalMarks = metaTotal || Object.values(marks).reduce((s, mk) => s + num(mk.positive), 0);
  // Per subject (test section), same rules as the admin Analytics
  // "Subject-wise" breakdown: not floored at 0, partial counted as wrong.
  const rowByQ = new Map<string, any>();
  for (const r of rows) if (!isBlank(r) && !rowByQ.has(String(r.qId))) rowByQ.set(String(r.qId), r);
  const sections: SectionScore[] = (test?.metadata || []).map((m: any) => {
    const ids: string[] = Array.isArray(m?.qIds) ? m.qIds.map(String) : [];
    let sc = 0, c = 0, w = 0, answered = 0;
    for (const id of ids) {
      const r = rowByQ.get(id);
      if (!r) continue;
      answered++;
      if (!r.isJudgeable) continue;
      const v = String(r.isCorrect || "").toUpperCase();
      if (v === "CORRECT") c++;
      else w++;
      sc += questionMarks(r, marks[id]);
    }
    const tm = num(m?.totalMarks) || ids.reduce((t, id) => t + num(marks[id]?.positive), 0);
    return {
      name: m?.name || "Section",
      score: Math.round(sc * 100) / 100,
      totalMarks: tm,
      totalQuestions: ids.length,
      correct: c,
      wrong: w,
      left: Math.max(0, ids.length - answered),
    };
  });
  return {
    sections,
    score: Math.max(0, Math.round(score * 100) / 100),
    totalMarks,
    totalQuestions,
    correct,
    wrong,
    partial,
    ungraded,
    left: Math.max(0, totalQuestions - answeredInTest),
  };
}
