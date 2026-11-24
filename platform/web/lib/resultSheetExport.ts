// Downloadable result sheet (Excel + PDF) in the institute's printed format:
//
//   <logo>
//   BATCH: <program>
//   <test name>
//   S.NO | NAME | ROLL NO. | PHYSICS(180): Score Rank Correct Wrong Left | … | TOTAL(720) | Rank
//
// Built in the browser from the same result rows the Result Sheet tab shows,
// so the files always match the screen. The libraries are loaded only when a
// download is clicked.

export type SheetSubject = { name: string; totalMarks: number };
export type SheetRow = {
  name: string;
  memberId: string;
  score: number;
  rank: number;
  subjects: { name: string; score: number; rank: number | null; correct: number; wrong: number; left: number }[];
};
export type SheetInput = {
  testName: string;
  totalMarks: number;
  orgName: string;
  logoUrl: string;
  batch: string;
  subjects: SheetSubject[];
  rows: SheetRow[];
};

const SUB_COLS = ["Score", "Rank", "Correct", "Wrong", "Left"] as const;
const RED = "C00000";
const TOTAL_FILL = "FFF2CC";

function fileBase(testName: string): string {
  return `${testName.replace(/[\\/:*?"<>|]+/g, " ").replace(/\s+/g, " ").trim() || "Test"} - Result Sheet`;
}

function save(blob: Blob, filename: string) {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 10000);
}

// Logo as a PNG data URL (any format the browser can show — jpeg, webp…).
async function loadLogo(url: string): Promise<{ dataUrl: string; w: number; h: number } | null> {
  if (!url) return null;
  try {
    const blob = await (await fetch(url)).blob();
    const src = URL.createObjectURL(blob);
    const img = await new Promise<HTMLImageElement>((ok, bad) => {
      const i = new Image();
      i.onload = () => ok(i);
      i.onerror = bad;
      i.src = src;
    });
    const c = document.createElement("canvas");
    c.width = img.naturalWidth;
    c.height = img.naturalHeight;
    c.getContext("2d")!.drawImage(img, 0, 0);
    URL.revokeObjectURL(src);
    return { dataUrl: c.toDataURL("image/png"), w: img.naturalWidth, h: img.naturalHeight };
  } catch {
    return null;
  }
}

function subjectHeader(s: SheetSubject): string {
  return s.totalMarks ? `${s.name.toUpperCase()}(${s.totalMarks})` : s.name.toUpperCase();
}

function cells(row: SheetRow, subjects: SheetSubject[]): (string | number)[] {
  const out: (string | number)[] = [];
  for (const s of subjects) {
    const v = row.subjects.find((x) => x.name === s.name);
    out.push(v?.score ?? 0, v?.rank ?? "", v?.correct ?? 0, v?.wrong ?? 0, v?.left ?? 0);
  }
  return out;
}

export async function downloadResultExcel(input: SheetInput) {
  const ExcelJS: any = (await import("exceljs")).default;
  const wb = new ExcelJS.Workbook();
  const ws = wb.addWorksheet("Result Sheet", {
    pageSetup: { orientation: "landscape", paperSize: 9, fitToPage: true, fitToWidth: 1, fitToHeight: 0 },
    views: [{ state: "frozen", ySplit: 5, xSplit: 3 }],
  });
  const nSub = input.subjects.length;
  const nCols = 3 + nSub * 5 + 2;
  const thin = { style: "thin", color: { argb: "FF000000" } };
  const border = { top: thin, left: thin, bottom: thin, right: thin };

  ws.columns = [
    { width: 6 },
    { width: 30 },
    { width: 12 },
    ...Array.from({ length: nSub * 5 }, (_, i) => ({ width: i % 5 === 0 ? 7 : i % 5 === 2 ? 8 : 7 })),
    { width: 12 },
    { width: 7 },
  ];

  // Row 1: logo (or the institute name), rows 2-3: batch + test name.
  const logo = await loadLogo(input.logoUrl);
  ws.mergeCells(1, 1, 1, nCols);
  ws.getRow(1).height = 60;
  if (logo) {
    const id = wb.addImage({ base64: logo.dataUrl, extension: "png" });
    const h = 70;
    const w = Math.round((logo.w / logo.h) * h);
    // centre the picture over the sheet (column widths are in characters ≈ 7px)
    const totalPx = (ws.columns as any[]).reduce((s, c) => s + (c.width || 8) * 7, 0);
    let left = Math.max(0, (totalPx - w) / 2), col = 0;
    for (const c of ws.columns as any[]) {
      const cw = (c.width || 8) * 7;
      if (left < cw) break;
      left -= cw;
      col++;
    }
    ws.addImage(id, { tl: { col: col + left / ((ws.columns as any[])[col]?.width * 7 || 56), row: 0.1 }, ext: { width: w, height: h } });
  } else {
    const c = ws.getCell(1, 1);
    c.value = input.orgName;
    c.font = { bold: true, size: 20, color: { argb: "FF" + RED } };
    c.alignment = { horizontal: "center", vertical: "middle" };
  }
  ws.mergeCells(2, 1, 2, nCols);
  const b = ws.getCell(2, 1);
  b.value = input.batch ? `BATCH: ${input.batch}` : "";
  b.font = { bold: true, size: 12 };
  b.alignment = { horizontal: "center" };
  b.border = border;
  ws.mergeCells(3, 1, 3, nCols);
  const t = ws.getCell(3, 1);
  t.value = input.testName;
  t.font = { bold: true, size: 14, color: { argb: "FF" + RED } };
  t.alignment = { horizontal: "center" };
  t.border = border;

  // Rows 4-5: two-level header.
  const h1 = 4, h2 = 5;
  const head = (r: number, c: number, v: string, extra: any = {}) => {
    const cell = ws.getCell(r, c);
    cell.value = v;
    cell.font = { bold: true, ...(extra.font || {}) };
    cell.alignment = { horizontal: "center", vertical: "middle", wrapText: true };
    cell.border = border;
    if (extra.fill) cell.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FF" + extra.fill } };
  };
  [["S.NO", 1], ["NAME", 2], ["ROLL NO.", 3]].forEach(([v, c]) => {
    ws.mergeCells(h1, c as number, h2, c as number);
    head(h1, c as number, v as string);
  });
  input.subjects.forEach((s, i) => {
    const c0 = 4 + i * 5;
    ws.mergeCells(h1, c0, h1, c0 + 4);
    head(h1, c0, subjectHeader(s), { font: { size: 12 } });
    SUB_COLS.forEach((name, k) => head(h2, c0 + k, name));
  });
  const totalCol = 4 + nSub * 5, rankCol = totalCol + 1;
  ws.mergeCells(h1, totalCol, h2, totalCol);
  head(h1, totalCol, input.totalMarks ? `TOTAL(${input.totalMarks})` : "TOTAL", { fill: TOTAL_FILL, font: { color: { argb: "FF" + RED } } });
  ws.mergeCells(h1, rankCol, h2, rankCol);
  head(h1, rankCol, "Rank", { font: { color: { argb: "FF" + RED } } });
  ws.getRow(h2).height = 18;

  input.rows.forEach((r, i) => {
    const row = ws.getRow(6 + i);
    row.values = [i + 1, r.name, r.memberId, ...cells(r, input.subjects), r.score, r.rank];
    row.eachCell({ includeEmpty: true }, (cell: any, col: number) => {
      cell.border = border;
      cell.alignment = { horizontal: col === 2 ? "left" : "center", vertical: "middle" };
      const isSubRank = col >= 4 && col < totalCol && (col - 4) % 5 === 1;
      if (isSubRank || col === rankCol) cell.font = { bold: true, color: { argb: "FF" + RED } };
      else if (col === totalCol) {
        cell.font = { bold: true };
        cell.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FF" + TOTAL_FILL } };
      } else if ((col >= 4 && (col - 4) % 5 === 0) || col === 3) cell.font = { bold: true };
    });
  });

  const buf = await wb.xlsx.writeBuffer();
  save(new Blob([buf], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }), `${fileBase(input.testName)}.xlsx`);
}

export async function downloadResultPdf(input: SheetInput) {
  const { jsPDF } = await import("jspdf");
  const autoTable: any = (await import("jspdf-autotable")).default;
  const doc = new jsPDF({ orientation: "landscape", unit: "pt", format: "a4" });
  const pageW = doc.internal.pageSize.getWidth();
  const margin = 20;
  let y = 18;

  const logo = await loadLogo(input.logoUrl);
  if (logo) {
    const h = 46;
    const w = Math.min(pageW - 2 * margin, (logo.w / logo.h) * h);
    doc.addImage(logo.dataUrl, "PNG", (pageW - w) / 2, y, w, (w / logo.w) * logo.h);
    y += (w / logo.w) * logo.h + 8;
  } else if (input.orgName) {
    doc.setFont("helvetica", "bold");
    doc.setFontSize(20);
    doc.setTextColor(192, 0, 0);
    doc.text(input.orgName, pageW / 2, y + 18, { align: "center" });
    y += 30;
  }
  doc.setDrawColor(0);
  doc.setLineWidth(1);
  doc.line(margin, y, pageW - margin, y);
  y += 15;
  if (input.batch) {
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(0, 0, 0);
    doc.text(`BATCH: ${input.batch}`, pageW / 2, y, { align: "center" });
    y += 17;
  }
  doc.setFont("helvetica", "bold");
  doc.setFontSize(14);
  doc.setTextColor(192, 0, 0);
  doc.text(input.testName, pageW / 2, y, { align: "center" });
  y += 8;

  const nSub = input.subjects.length;
  const span = nSub ? 2 : 1;
  const head = [
    [
      { content: "S.NO", rowSpan: span },
      { content: "NAME", rowSpan: span },
      { content: "ROLL NO.", rowSpan: span },
      ...input.subjects.map((s) => ({ content: subjectHeader(s), colSpan: 5 })),
      { content: input.totalMarks ? `TOTAL(${input.totalMarks})` : "TOTAL", rowSpan: span },
      { content: "Rank", rowSpan: span },
    ],
    ...(nSub ? [input.subjects.flatMap(() => SUB_COLS.map((c) => c))] : []),
  ];
  const body = input.rows.map((r, i) => [i + 1, r.name, r.memberId, ...cells(r, input.subjects), r.score, r.rank]);
  const totalCol = 3 + nSub * 5, rankCol = totalCol + 1;
  const isSubRank = (c: number) => c >= 3 && c < totalCol && (c - 3) % 5 === 1;

  autoTable(doc, {
    startY: y,
    head,
    body,
    theme: "grid",
    margin: { left: margin, right: margin },
    styles: { fontSize: nSub > 4 ? 6 : 7.5, cellPadding: 2, halign: "center", valign: "middle", lineColor: 0, lineWidth: 0.5, textColor: 0 },
    headStyles: { fillColor: [255, 255, 255], textColor: 0, fontStyle: "bold", lineColor: 0, lineWidth: 0.5 },
    columnStyles: { 1: { halign: "left", cellWidth: 110 }, 2: { cellWidth: 48 } },
    didParseCell: (d: any) => {
      const c = d.column.index;
      if (d.section === "head") {
        if (d.row.index === 0 && (c === totalCol || c === rankCol)) d.cell.styles.textColor = [192, 0, 0];
        if (d.row.index === 0 && c === totalCol) d.cell.styles.fillColor = [255, 242, 204];
        return;
      }
      if (isSubRank(c) || c === rankCol) {
        d.cell.styles.textColor = [192, 0, 0];
        d.cell.styles.fontStyle = "bold";
      } else if (c === totalCol) {
        d.cell.styles.fillColor = [255, 242, 204];
        d.cell.styles.fontStyle = "bold";
      } else if (c === 2 || (c >= 3 && (c - 3) % 5 === 0)) d.cell.styles.fontStyle = "bold";
    },
  });

  save(doc.output("blob"), `${fileBase(input.testName)}.pdf`);
}
