import JSZip from "jszip";

// First worksheet of an .xlsx as tab-separated lines — enough for answer-key
// spreadsheets like "Sl.No | Key | Answer" (see parseAnswerKey in
// lib/questionImport). Shared strings and inline strings are resolved;
// numbers are written as stored.

function decode(s: string): string {
  return s
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#(\d+);/g, (_, n) => String.fromCharCode(Number(n)))
    .replace(/&amp;/g, "&");
}

function colIndex(ref: string): number {
  const letters = (ref.match(/^[A-Z]+/) || ["A"])[0];
  let n = 0;
  for (const ch of letters) n = n * 26 + (ch.charCodeAt(0) - 64);
  return n - 1;
}

export async function xlsxToText(buf: Buffer): Promise<string> {
  const zip = await JSZip.loadAsync(buf);
  const shared: string[] = [];
  const sst = await zip.file("xl/sharedStrings.xml")?.async("string");
  if (sst) {
    for (const si of Array.from(sst.matchAll(/<si>([\s\S]*?)<\/si>/g))) {
      shared.push(decode(Array.from(si[1].matchAll(/<t[^>]*>([\s\S]*?)<\/t>/g)).map((t) => t[1]).join("")));
    }
  }
  const sheetName =
    Object.keys(zip.files)
      .filter((n) => /^xl\/worksheets\/sheet\d+\.xml$/.test(n))
      .sort((a, b) => parseInt(a.replace(/\D/g, ""), 10) - parseInt(b.replace(/\D/g, ""), 10))[0] || "";
  const sheet = sheetName ? await zip.file(sheetName)!.async("string") : "";
  const lines: string[] = [];
  for (const row of Array.from(sheet.matchAll(/<row[^>]*>([\s\S]*?)<\/row>/g))) {
    const cells: string[] = [];
    for (const c of Array.from(row[1].matchAll(/<c ([^>]*?)(?:\/>|>([\s\S]*?)<\/c>)/g))) {
      const attrs = c[1];
      const inner = c[2] || "";
      const ref = (attrs.match(/r="([A-Z]+\d+)"/) || [])[1] || "";
      const type = (attrs.match(/t="(\w+)"/) || [])[1] || "";
      const v = (inner.match(/<v>([\s\S]*?)<\/v>/) || [])[1];
      let val = "";
      if (type === "s" && v !== undefined) val = shared[parseInt(v, 10)] ?? "";
      else if (type === "inlineStr") val = decode(Array.from(inner.matchAll(/<t[^>]*>([\s\S]*?)<\/t>/g)).map((t) => t[1]).join(""));
      else if (v !== undefined) val = decode(v);
      const idx = ref ? colIndex(ref) : cells.length;
      while (cells.length < idx) cells.push("");
      cells[idx] = val.trim();
    }
    if (cells.some(Boolean)) lines.push(cells.join("\t"));
  }
  return lines.join("\n");
}
