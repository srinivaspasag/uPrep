import { NextRequest, NextResponse } from "next/server";
import mammoth from "mammoth";
// @ts-ignore -- no bundled types. Import the internal module directly rather
// than the package root: pdf-parse's index.js has a `!module.parent` debug
// branch that (mis)fires under Next's build-time module collection and
// tries to read a nonexistent test fixture, crashing the build.
import pdfParse from "pdf-parse/lib/pdf-parse.js";
import Anthropic from "@anthropic-ai/sdk";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || "";
const ANTHROPIC_MODEL = process.env.ANTHROPIC_MODEL || "claude-sonnet-5";

// Plain text-layer PDF extraction (pdf-parse) reads whatever character codes
// are embedded in the file. Many exam-paper PDFs render their equations with
// a custom/subset math font that has no working glyph->Unicode map, so
// text-layer extraction comes back as garbage (confirmed live: fractions and
// inequalities from a real uploaded question bank came out as Arabic
// presentation-form characters, not the original symbols) — no text-layer
// parser can recover that, the mapping data simply isn't in the file. Claude
// reading the PDF as a document (vision, not text-layer) transcribes the
// same content correctly, so that's the primary path when a key is
// configured; pdf-parse remains the free fallback when it isn't.
async function extractPdfViaVision(buf: Buffer): Promise<string> {
  const client = new Anthropic({ apiKey: ANTHROPIC_API_KEY });
  const msg = await client.messages.create({
    model: ANTHROPIC_MODEL,
    max_tokens: 8000,
    messages: [
      {
        role: "user",
        content: [
          {
            type: "document",
            source: { type: "base64", media_type: "application/pdf", data: buf.toString("base64") },
          },
          {
            type: "text",
            text:
              "Transcribe this document's text exactly, in reading order. If it is a numbered exam question bank, " +
              "keep each question's number (e.g. \"1]\") and its a)/b)/c)/d) option markers exactly as in the " +
              "source. Write any mathematical notation (fractions, exponents, roots, Greek letters, inequalities, " +
              "etc.) as LaTeX between $ signs, e.g. $\\frac{a}{b}$, $x^2$, $\\sqrt{x}$ — never leave equations as " +
              "garbled symbols. Output only the transcribed text, no commentary, no markdown code fences.",
          },
        ],
      },
    ],
  });
  const block = msg.content.find((b) => b.type === "text");
  return block && block.type === "text" ? block.text : "";
}

export async function POST(req: NextRequest) {
  try {
    const form = await req.formData();
    const file = form.get("file");
    if (!(file instanceof Blob)) return NextResponse.json({ error: "No file provided" }, { status: 400 });
    const name = (file as File).name || "";
    const buf = Buffer.from(await file.arrayBuffer());

    if (/\.docx$/i.test(name)) {
      const { value } = await mammoth.extractRawText({ buffer: buf });
      return NextResponse.json({ text: value, method: "docx" });
    }

    if (/\.pdf$/i.test(name)) {
      if (ANTHROPIC_API_KEY) {
        try {
          const text = await extractPdfViaVision(buf);
          if (text.trim()) return NextResponse.json({ text, method: "vision" });
        } catch {
          // Fall through to the plain-text fallback below.
        }
      }
      const data = await pdfParse(buf);
      return NextResponse.json({
        text: data.text,
        method: "text-layer",
        warning: ANTHROPIC_API_KEY
          ? "AI transcription failed — fell back to plain text extraction, which can garble equations."
          : "AI transcription isn't configured on this server — using plain text extraction, which can garble equations.",
      });
    }

    // .txt or unrecognized extension — treat as plain text.
    return NextResponse.json({ text: buf.toString("utf-8"), method: "text" });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to extract text from file" }, { status: 500 });
  }
}
