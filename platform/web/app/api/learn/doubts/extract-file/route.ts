import { NextRequest, NextResponse } from "next/server";
import Anthropic from "@anthropic-ai/sdk";
import mammoth from "mammoth";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || "";
const ANTHROPIC_MODEL = process.env.ANTHROPIC_MODEL || "claude-sonnet-5";
const MAX_BYTES = 15 * 1024 * 1024; // comfortably covers a multi-page PDF or docx

const IMAGE_TYPES: Record<string, string> = {
  "image/jpeg": "image/jpeg",
  "image/png": "image/png",
  "image/webp": "image/webp",
  "image/gif": "image/gif",
};
type Kind = "image" | "pdf" | "docx";

function kindOf(file: File): Kind | null {
  if (IMAGE_TYPES[file.type]) return "image";
  if (file.type === "application/pdf" || /\.pdf$/i.test(file.name)) return "pdf";
  if (
    file.type === "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
    /\.docx$/i.test(file.name)
  )
    return "docx";
  return null;
}

const TOOL_NAME = "review_doubt_file";
const TOOL_SCHEMA = {
  name: TOOL_NAME,
  description: "Report whether this student-uploaded file is appropriate for a school study platform, and transcribe any academic question in it.",
  input_schema: {
    type: "object" as const,
    properties: {
      isAppropriate: {
        type: "boolean" as const,
        description:
          "false if the content contains sexual/adult content, nudity, graphic violence/gore, hate symbols, or anything else " +
          "inappropriate for a school platform used by students — regardless of whether it also contains a question. " +
          "true only if it's safe for a classroom setting.",
      },
      extractedText: {
        type: "string" as const,
        description:
          "Only when isAppropriate is true: the academic question/problem found, transcribed exactly as a student would type it. " +
          "Preserve all math as LaTeX between $ signs (e.g. $\\frac{a}{b}$, $x^2$, $\\sqrt{x}$) — never leave formulas as garbled " +
          "symbols. If appropriate but no legible academic question is present, return an empty string. Always empty when " +
          "isAppropriate is false.",
      },
      reason: {
        type: "string" as const,
        description: "One short sentence: why this verdict (internal only, not shown to the student).",
      },
    },
    required: ["isAppropriate", "extractedText", "reason"],
  },
};

// Turns an uploaded photo, PDF, or Word doc of a question into the same
// plain question text a student would have typed — the "Ask Aira" modal's
// upload button, same input surface as typing or voice, just a different
// way to fill in the question field.
//
// Moderation is built into the SAME call that reads the content, not a
// separate pass: the model is forced (tool_choice) to first judge whether
// the content is appropriate for a school platform before it's allowed to
// transcribe anything, so flagged content never produces usable text. One
// tool, one verdict shape, regardless of which of the three formats this
// was — keeps the moderation decision consistent across all of them rather
// than mixing models/rules per file type. Nothing is ever written to disk
// or stored — held in memory for this one request and discarded either way.
async function reviewContent(content: Anthropic.MessageParam["content"]): Promise<{ isAppropriate: boolean; text: string; reason: string }> {
  const client = new Anthropic({ apiKey: ANTHROPIC_API_KEY });
  const msg = await client.messages.create({
    model: ANTHROPIC_MODEL,
    max_tokens: 2000,
    tools: [TOOL_SCHEMA],
    tool_choice: { type: "tool", name: TOOL_NAME },
    messages: [{ role: "user", content }],
  });

  const call = msg.content.find((b) => b.type === "tool_use");
  // A forced tool_choice can still come back empty if the model's own safety
  // layer refuses the request outright (extreme content) — fail closed.
  if (!call || call.type !== "tool_use") return { isAppropriate: false, text: "", reason: "Model declined to process the file." };
  const input = call.input as { isAppropriate?: boolean; extractedText?: string; reason?: string };
  return {
    isAppropriate: input.isAppropriate === true,
    text: typeof input.extractedText === "string" ? input.extractedText : "",
    reason: input.reason || "",
  };
}

const ASK_TEXT =
  "A student uploaded this to a study-help chat to ask a question about it. First judge whether it's appropriate for a " +
  "school platform. Only if it is, transcribe the academic question in it.";

export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });

  if (!ANTHROPIC_API_KEY) {
    return NextResponse.json(
      { error: "File upload isn't set up on this server yet — please type your question instead." },
      { status: 503 }
    );
  }

  try {
    const form = await req.formData().catch(() => null);
    const file = form?.get("file");
    if (!(file instanceof File)) return NextResponse.json({ error: "No file provided" }, { status: 400 });

    const kind = kindOf(file);
    if (!kind) {
      return NextResponse.json({ error: "Please upload an image (JPEG/PNG/WebP/GIF), a PDF, or a Word (.docx) file." }, { status: 400 });
    }
    if (file.size > MAX_BYTES) {
      return NextResponse.json({ error: "That file is too large — please upload one under 15MB." }, { status: 400 });
    }

    const buf = Buffer.from(await file.arrayBuffer());

    let content: Anthropic.MessageParam["content"];
    if (kind === "image") {
      content = [
        { type: "image", source: { type: "base64", media_type: IMAGE_TYPES[file.type] as any, data: buf.toString("base64") } },
        { type: "text", text: ASK_TEXT },
      ];
    } else if (kind === "pdf") {
      content = [
        { type: "document", source: { type: "base64", media_type: "application/pdf", data: buf.toString("base64") } },
        { type: "text", text: ASK_TEXT },
      ];
    } else {
      // .docx — extract the raw text locally (same as Bulk Import Questions'
      // extraction route) and hand Claude the text itself, since Claude has
      // no native docx content type.
      const { value } = await mammoth.extractRawText({ buffer: buf });
      if (!value.trim()) return NextResponse.json({ error: "Couldn't find any text in that document." }, { status: 422 });
      content = `${ASK_TEXT}\n\nDocument content:\n${value}`;
    }

    const result = await reviewContent(content);

    if (!result.isAppropriate) {
      // Logged (not shown to the student) so staff can spot repeated abuse
      // via `docker logs uprep-ui-1` without building a dedicated review UI
      // for what should be a rare event.
      console.warn("[doubts/extract-file] rejected", { userId: session.id, kind, reason: result.reason });
      return NextResponse.json(
        { error: "This file couldn't be processed. Please upload a study question only." },
        { status: 422 }
      );
    }
    if (!result.text.trim()) {
      return NextResponse.json({ error: "Couldn't find a question in that file — try a clearer one." }, { status: 422 });
    }

    return NextResponse.json({ text: result.text.trim() });
  } catch (e: any) {
    console.error("[doubts/extract-file] failed", e?.message || e);
    return NextResponse.json({ error: "Couldn't read that file — please try again or type your question." }, { status: 500 });
  }
}
