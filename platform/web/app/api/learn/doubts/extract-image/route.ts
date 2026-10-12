import { NextRequest, NextResponse } from "next/server";
import Anthropic from "@anthropic-ai/sdk";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || "";
const ANTHROPIC_MODEL = process.env.ANTHROPIC_MODEL || "claude-sonnet-5";
const MAX_BYTES = 8 * 1024 * 1024; // 8MB — comfortably covers a phone photo
const ALLOWED_TYPES: Record<string, string> = {
  "image/jpeg": "image/jpeg",
  "image/png": "image/png",
  "image/webp": "image/webp",
  "image/gif": "image/gif",
};

// Turns a photo of a question (textbook page, handwritten notebook, etc.)
// into the same plain question text a student would have typed — the "Ask
// Aira" modal's image button, same input surface as typing or voice, just
// a different way to fill in the question field (see layout.tsx's existing
// SpeechRecognition handler for the pattern this mirrors).
//
// Moderation is built into the SAME vision call rather than a separate
// pass: the model is forced (tool_choice) to first judge whether the image
// is appropriate for a school platform before it's allowed to transcribe
// anything, so a flagged image never gets as far as producing usable text.
// The image itself is never written to disk or stored anywhere — it's
// base64'd in memory for this one request and discarded.
async function reviewImage(buf: Buffer, mediaType: string): Promise<{ isAppropriate: boolean; text: string; reason: string }> {
  const client = new Anthropic({ apiKey: ANTHROPIC_API_KEY });
  const msg = await client.messages.create({
    model: ANTHROPIC_MODEL,
    max_tokens: 2000,
    tools: [
      {
        name: "review_doubt_image",
        description: "Report whether this student-uploaded image is appropriate for a school study platform, and transcribe any academic question in it.",
        input_schema: {
          type: "object",
          properties: {
            isAppropriate: {
              type: "boolean",
              description:
                "false if the image contains sexual/adult content, nudity, graphic violence/gore, hate symbols, or anything else " +
                "inappropriate for a school platform used by students — regardless of whether it also contains a question. " +
                "true only if the image is safe for a classroom setting.",
            },
            extractedText: {
              type: "string",
              description:
                "Only when isAppropriate is true: the academic question/problem shown in the image (printed or handwritten), " +
                "transcribed exactly as a student would type it. Preserve all math as LaTeX between $ signs (e.g. $\\frac{a}{b}$, " +
                "$x^2$, $\\sqrt{x}$) — never leave formulas as garbled symbols. If the image is appropriate but has no legible " +
                "academic question in it, return an empty string. Always empty string when isAppropriate is false.",
            },
            reason: {
              type: "string",
              description: "One short sentence: why this verdict (internal only, not shown to the student).",
            },
          },
          required: ["isAppropriate", "extractedText", "reason"],
        },
      },
    ],
    tool_choice: { type: "tool", name: "review_doubt_image" },
    messages: [
      {
        role: "user",
        content: [
          { type: "image", source: { type: "base64", media_type: mediaType as any, data: buf.toString("base64") } },
          {
            type: "text",
            text:
              "A student uploaded this image to a study-help chat to ask a question about it. First judge whether it's " +
              "appropriate for a school platform. Only if it is, transcribe the academic question in it.",
          },
        ],
      },
    ],
  });

  const call = msg.content.find((b) => b.type === "tool_use");
  // A forced tool_choice can still come back empty if the model's own safety
  // layer refuses the request outright (extreme content) — fail closed,
  // not open, when that happens.
  if (!call || call.type !== "tool_use") return { isAppropriate: false, text: "", reason: "Model declined to process the image." };
  const input = call.input as { isAppropriate?: boolean; extractedText?: string; reason?: string };
  return {
    isAppropriate: input.isAppropriate === true,
    text: typeof input.extractedText === "string" ? input.extractedText : "",
    reason: input.reason || "",
  };
}

export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });

  if (!ANTHROPIC_API_KEY) {
    return NextResponse.json(
      { error: "Image upload isn't set up on this server yet — please type your question instead." },
      { status: 503 }
    );
  }

  try {
    const form = await req.formData().catch(() => null);
    const file = form?.get("file");
    if (!(file instanceof File)) return NextResponse.json({ error: "No image provided" }, { status: 400 });

    const mediaType = ALLOWED_TYPES[file.type];
    if (!mediaType) {
      return NextResponse.json({ error: "Please upload a JPEG, PNG, WebP, or GIF image." }, { status: 400 });
    }
    if (file.size > MAX_BYTES) {
      return NextResponse.json({ error: "That image is too large — please upload one under 8MB." }, { status: 400 });
    }

    const buf = Buffer.from(await file.arrayBuffer());
    const result = await reviewImage(buf, mediaType);

    if (!result.isAppropriate) {
      // Logged (not shown to the student) so staff can spot repeated abuse
      // via `docker logs uprep-ui-1` without building a dedicated review UI
      // for what should be a rare event.
      console.warn("[doubts/extract-image] rejected", { userId: session.id, reason: result.reason });
      return NextResponse.json(
        { error: "This image couldn't be processed. Please upload a clear photo of a study question only." },
        { status: 422 }
      );
    }
    if (!result.text.trim()) {
      return NextResponse.json({ error: "Couldn't find a question in that image — try a clearer photo." }, { status: 422 });
    }

    return NextResponse.json({ text: result.text.trim() });
  } catch (e: any) {
    console.error("[doubts/extract-image] failed", e?.message || e);
    return NextResponse.json({ error: "Couldn't read that image — please try again or type your question." }, { status: 500 });
  }
}
