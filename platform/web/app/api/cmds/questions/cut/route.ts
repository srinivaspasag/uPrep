import { NextRequest, NextResponse } from "next/server";
import { cutPdf } from "@/lib/pdfPictures";
import { saveBuffer } from "@/lib/storage";
import { LETTERS } from "@/lib/questionImport";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const MAX_BYTES = 60 * 1024 * 1024;

// Exact-copy Bulk Import: cuts every question (and each option) out of the
// PDF as pictures — see lib/pdfPictures. Staff-only (middleware guards
// /api/cmds). Returns, per question:
//   questions: { num, content, options | null, layout, note }
//     layout "options"   — question picture + 4 option pictures
//            "whole"     — options are printed inside the question picture
//            "numerical" — no options printed (numerical-answer question)
//   solutions: { num, content, key } — key is the answer printed with the
//     solution ("12. [3] …"), as a/b/c/d, or "" when none is printed
export async function POST(req: NextRequest) {
  try {
    const form = await req.formData();
    const file = form.get("file");
    const kind = form.get("kind") === "solutions" ? "solutions" : "questions";
    if (!(file instanceof Blob)) return NextResponse.json({ error: "No file provided" }, { status: 400 });
    const name = (file as File).name || "";
    if (!/\.pdf$/i.test(name)) return NextResponse.json({ error: "Exact-picture import needs a PDF file" }, { status: 400 });
    if (file.size > MAX_BYTES) return NextResponse.json({ error: "PDF is too large (max 60 MB)" }, { status: 400 });
    const buf = Buffer.from(await file.arrayBuffer());

    const t0 = Date.now();
    const res = await cutPdf(buf, kind);
    const save = async (png: Buffer, label: string) => (await saveBuffer(png, `${kind}-${label}.png`, "image/png")).url;

    if (kind === "solutions") {
      const solutions = [];
      for (const q of res.questions) {
        const urls: string[] = [];
        for (let k = 0; k < q.stem.length; k++) urls.push(await save(q.stem[k], `s${q.num}-${k}`));
        // the answer printed right after the number: "12. [3]", "12. (c)", "12. Ans: 3"
        const m = q.text.match(
          /^\s*(?:Q\.?\s*)?\d{1,3}\s*[.):]?\s*(?:\[\s*([1-4a-dA-D])\s*\]|\(\s*([1-4a-dA-D])\s*\)|ans(?:wer)?\s*[:.\-]?\s*\(?([1-4a-dA-D])\)?)/i
        );
        const k = m ? (m[1] || m[2] || m[3]).toLowerCase() : "";
        solutions.push({
          num: q.num,
          content: urls.map((u) => `![solution](${u})`).join("\n\n"),
          key: /[1-4]/.test(k) ? LETTERS[parseInt(k, 10) - 1] : k,
        });
      }
      return NextResponse.json({ kind, pages: res.pages, ms: Date.now() - t0, missing: res.missing, warnings: res.warnings, solutions });
    }

    const questions = [];
    for (const q of res.questions) {
      const stem: string[] = [];
      for (let k = 0; k < q.stem.length; k++) stem.push(await save(q.stem[k], `q${q.num}-${k}`));
      let options: string[] | null = null;
      if (q.options) {
        options = [];
        for (let k = 0; k < q.options.length; k++) options.push(`![option ${k + 1}](${await save(q.options[k], `q${q.num}-o${k + 1}`)})`);
      }
      const numerical = !q.options && /numerical-answer/.test(q.note || "");
      questions.push({
        num: q.num,
        content: stem.map((u) => `![question](${u})`).join("\n\n"),
        options,
        layout: q.options ? "options" : numerical ? "numerical" : "whole",
        note: q.note || "",
      });
    }
    return NextResponse.json({ kind, pages: res.pages, ms: Date.now() - t0, missing: res.missing, warnings: res.warnings, questions });
  } catch (e: any) {
    console.error("[cut] failed:", e?.stack || e?.message);
    return NextResponse.json({ error: "Couldn't read this PDF: " + (e?.message || "unknown error") }, { status: 500 });
  }
}
