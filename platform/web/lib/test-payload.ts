import { API, CALLING_APP, CALLING_APP_ID } from "@/lib/config";

// A test's info + questions as the student app sees them — GET /api/tests/[id]
// and the offline packager (tests on SD cards) both build it here, so a test
// read off a card is byte-for-byte the shape the Android app already caches
// from a live download (TestsApi.TestDetailResponse). Question content and
// options only: correct answers live in the separate `answers` collection and
// are never part of this, which is what makes it safe to put on a card.
export type TestPayload = {
  test: {
    id: string;
    name: string;
    durationMin: number;
    totalMarks: number;
    code: string | null;
  };
  questions: { id: string; content: string; type: string; options: string[]; board: string }[];
};

export async function fetchTestPayload(id: string, orgId: string, userId: string): Promise<TestPayload> {
  const form = () =>
    new URLSearchParams({ callingApp: CALLING_APP, callingAppId: CALLING_APP_ID, callingUserId: userId, userId, orgId, id });
  // The legacy service answers a crash with an HTML error page (Play's
  // "Oops, an error occured … logged with id @xxxx"). Parsing that as JSON
  // used to surface "Unexpected token '<' … is not valid JSON" to students —
  // log what the service actually said and show something readable instead.
  const post = async (p: string) => {
    // no-store: Next caches fetch() results — POSTs included — when called
    // from a GET route, so without this a question edited in CMDS kept
    // being served from .next/cache in its old form.
    const r = await fetch(`${API.content}/tests/${p}`, {
      method: "POST",
      cache: "no-store",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form(),
    });
    const text = await r.text();
    try {
      return JSON.parse(text);
    } catch {
      const logId = text.match(/logged with id\s+@?([0-9a-z]+)/i)?.[1] || "";
      console.error(`[tests] legacy ${p} for test ${id} returned HTTP ${r.status} non-JSON${logId ? ` (legacy log id ${logId})` : ""}`);
      throw Object.assign(new Error("This test couldn't be loaded right now. Please tell your teacher."), {
        legacy: true,
      });
    }
  };
  const [info, q] = await Promise.all([post("getTestInfo"), post("getTestQuestions")]);
  if (info.errorCode || q.errorCode)
    throw Object.assign(new Error(info.errorMessage || q.errorMessage || info.errorCode || q.errorCode), { legacy: true });

  const ir = info.result || {};
  const qr = q.result || {};
  const questions: TestPayload["questions"] = [];
  for (const b of qr.boards || []) {
    for (const qq of b.questions || []) {
      questions.push({ id: qq.id, content: qq.content, type: qq.type, options: qq.options || [], board: b.name });
    }
  }
  return {
    test: {
      id: ir.id || id,
      name: ir.name || qr.name || "Test",
      durationMin: qr.totalTestTime || ir.duration ? Math.round((qr.totalTestTime || ir.duration) / 60000) : 0,
      totalMarks: ir.totalMarks ?? 0,
      code: qr.code || ir.code || null,
    },
    questions,
  };
}
