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
  const post = (p: string) =>
    fetch(`${API.content}/tests/${p}`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form(),
    }).then((r) => r.json());
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
