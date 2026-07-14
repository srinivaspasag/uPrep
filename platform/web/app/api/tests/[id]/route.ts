import { NextRequest, NextResponse } from "next/server";
import { API, DEFAULT_ORG_ID, CALLING_APP, CALLING_APP_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Fetches a test's info + questions from the REAL legacy content service
// (getTestInfo / getTestQuestions). Answer keys are intentionally NOT sent to
// the client. userId/orgId come from the caller (logged-in session).
export async function GET(
  req: NextRequest,
  { params }: { params: { id: string } }
) {
  const id = params.id;
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;

  const form = () =>
    new URLSearchParams({
      callingApp: CALLING_APP,
      callingAppId: CALLING_APP_ID,
      callingUserId: userId,
      userId,
      orgId,
      id,
    });

  try {
    const [infoResp, qResp] = await Promise.all([
      fetch(`${API.content}/tests/getTestInfo`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form(),
      }),
      fetch(`${API.content}/tests/getTestQuestions`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form(),
      }),
    ]);

    const info = await infoResp.json();
    const q = await qResp.json();

    if (info.errorCode || q.errorCode) {
      return NextResponse.json(
        { error: info.errorMessage || q.errorMessage || info.errorCode || q.errorCode },
        { status: 400 }
      );
    }

    const ir = info.result || {};
    const qr = q.result || {};

    // Flatten board -> questions into a clean, key-free question list.
    const questions: any[] = [];
    for (const b of qr.boards || []) {
      for (const qq of b.questions || []) {
        questions.push({
          id: qq.id,
          content: qq.content,
          type: qq.type,
          options: qq.options || [],
          board: b.name,
        });
      }
    }

    return NextResponse.json({
      test: {
        id: ir.id || id,
        name: ir.name || qr.name || "Test",
        durationMin: (qr.totalTestTime || ir.duration || 0)
          ? Math.round((qr.totalTestTime || ir.duration) / 60000)
          : 0,
        totalMarks: ir.totalMarks ?? 0,
        code: qr.code || ir.code || null,
      },
      questions,
    });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message || "Failed to load test" },
      { status: 500 }
    );
  }
}
