"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

// Bulk-upload students from a pasted/uploaded CSV — same shape as the
// existing "Bulk Import Questions" tool (app/cmds/questions/import/page.tsx):
// parse client-side, show an editable review step, then import row-by-row
// through the exact same POST /api/cmds/tools/people the single "+ Add
// Student" form uses, so validation/seat-limits/login-creation stay
// identical either way.
//
// Legacy's real bulk upload (organization-mgmt StudentsXLParser /
// OrgMemberManager.uploadOrgStudents) scopes an entire upload batch to ONE
// Program chosen up front, with the spreadsheet supplying Center + Section
// per row (validated against that program's actual centers/sections) — this
// mirrors that rather than inventing a different shape. Legacy's XL format
// also carries gender/DOB/parent-contact columns our simplified Member model
// doesn't track; those are dropped here, keeping only the fields this app
// actually stores.

type AcadProgram = { id: string; name: string; centerIds?: string[] };
type AcadCenter = { id: string; name: string };
type AcadSection = { id: string; name: string; programId?: string | null; centerId?: string | null };

type ParsedRow = {
  key: number;
  memberId: string;
  firstName: string;
  lastName: string;
  email: string;
  contactNumber: string;
  centerName: string;
  sectionName: string;
};

type RowResult = {
  key: number;
  ok: boolean;
  name?: string;
  memberId?: string;
  password?: string;
  error?: string;
};

const TEMPLATE_HEADERS = ["MemberId", "First Name", "Last Name", "Email", "Contact No", "Center", "Section"];

function normalizeHeader(h: string) {
  return h.toLowerCase().replace(/[^a-z]/g, "");
}

const HEADER_ALIASES: Record<string, string> = {
  memberid: "memberId",
  institueid: "memberId",
  instituteid: "memberId",
  id: "memberId",
  firstname: "firstName",
  memberfirstname: "firstName",
  lastname: "lastName",
  memberlastname: "lastName",
  email: "email",
  contactno: "contactNumber",
  contact: "contactNumber",
  contactnumber: "contactNumber",
  phone: "contactNumber",
  mobile: "contactNumber",
  center: "centerName",
  centre: "centerName",
  section: "sectionName",
};

function parseDelimited(text: string): string[][] {
  const firstLine = (text.split(/\r?\n/)[0] || "");
  const delim = firstLine.includes("\t") ? "\t" : ",";
  const lines = text.split(/\r?\n/).filter((l) => l.trim().length > 0);
  return lines.map((line) => {
    if (delim === "\t") return line.split("\t").map((c) => c.trim());
    const cells: string[] = [];
    let cur = "";
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (inQuotes) {
        if (ch === '"') {
          if (line[i + 1] === '"') {
            cur += '"';
            i++;
          } else inQuotes = false;
        } else cur += ch;
      } else if (ch === '"') {
        inQuotes = true;
      } else if (ch === ",") {
        cells.push(cur.trim());
        cur = "";
      } else {
        cur += ch;
      }
    }
    cells.push(cur.trim());
    return cells;
  });
}

export default function BulkUploadStudentsPage() {
  const [session, setSession] = useState<UprepSession | null>(null);
  const [programs, setPrograms] = useState<AcadProgram[]>([]);
  const [centers, setCenters] = useState<AcadCenter[]>([]);
  const [sections, setSections] = useState<AcadSection[]>([]);
  const [programIds, setProgramIds] = useState<string[]>([]);
  const [raw, setRaw] = useState("");
  const [rows, setRows] = useState<ParsedRow[] | null>(null);
  const [step, setStep] = useState<"input" | "review" | "done">("input");
  const [uploading, setUploading] = useState(false);
  const [results, setResults] = useState<RowResult[]>([]);
  const [parseError, setParseError] = useState("");

  useEffect(() => {
    setSession(getSession());
    fetch("/api/cmds/tools/academic")
      .then((r) => r.json())
      .then((d) => {
        setPrograms(d.programs || []);
        setCenters(d.centers || []);
        setSections(d.sections || []);
      })
      .catch(() => {});
  }, []);

  // Every helper below is scoped to ONE program id — with multiple programs
  // selected, the same Center/Section text in the CSV has to resolve
  // independently against each program's own centers/sections (center and
  // section names aren't shared across programs, just coincidentally reused).
  function centersForProgram(pid: string): AcadCenter[] {
    const prog = programs.find((p) => p.id === pid);
    const ids = new Set(prog?.centerIds || []);
    return centers.filter((c) => ids.has(c.id));
  }
  function sectionsForProgramCenter(pid: string, centerId: string) {
    return sections.filter((s) => s.programId === pid && s.centerId === centerId);
  }
  function resolveCenterForProgram(pid: string, name: string): string {
    if (!name.trim()) return "";
    const hit = centersForProgram(pid).find((c) => c.name.toLowerCase() === name.trim().toLowerCase());
    return hit?.id || "";
  }
  function resolveSectionForProgram(pid: string, centerId: string, name: string): string {
    if (!name.trim() || !centerId) return "";
    const hit = sectionsForProgramCenter(pid, centerId).find((s) => s.name.toLowerCase() === name.trim().toLowerCase());
    return hit?.id || "";
  }
  // Resolved {centerId, sectionId} per selected program for a given row's
  // raw Center/Section text — null entries mean that program couldn't
  // resolve the name (surfaced in rowError, not silently dropped).
  function resolveRowForPrograms(r: ParsedRow) {
    return programIds.map((pid) => {
      const centerId = resolveCenterForProgram(pid, r.centerName);
      const sectionId = resolveSectionForProgram(pid, centerId, r.sectionName);
      const prog = programs.find((p) => p.id === pid);
      return { programId: pid, programName: prog?.name || "(program)", centerId, sectionId };
    });
  }

  function downloadTemplate() {
    const firstCenterName = programIds.length ? centersForProgram(programIds[0])[0]?.name : "";
    const example = ["", "Riya", "Sharma", "riya@example.com", "9876543210", firstCenterName || "Center A", "Section A"];
    const csv = [TEMPLATE_HEADERS.join(","), example.join(",")].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "students_template.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  function parse() {
    setParseError("");
    if (programIds.length === 0) {
      setParseError("Pick at least one Program first — every uploaded student is assigned into all of them.");
      return;
    }
    const table = parseDelimited(raw);
    if (table.length < 2) {
      setParseError("Paste (or upload) at least a header row and one student row.");
      return;
    }
    const header = table[0].map(normalizeHeader);
    const fieldAt = header.map((h) => HEADER_ALIASES[h] || "");
    if (!fieldAt.includes("firstName")) {
      setParseError('Could not find a "First Name" column in the header row.');
      return;
    }
    const parsed: ParsedRow[] = table.slice(1).map((cells, idx) => {
      const get = (field: string) => {
        const i = fieldAt.indexOf(field);
        return i === -1 ? "" : (cells[i] || "").trim();
      };
      return {
        key: idx,
        memberId: get("memberId"),
        firstName: get("firstName"),
        lastName: get("lastName"),
        email: get("email"),
        contactNumber: get("contactNumber"),
        centerName: get("centerName"),
        sectionName: get("sectionName"),
      };
    });
    setRows(parsed);
    setStep("review");
  }

  function updateRow(key: number, patch: Partial<ParsedRow>) {
    setRows((prev) => (prev ? prev.map((r) => (r.key === key ? { ...r, ...patch } : r)) : prev));
  }
  function removeRow(key: number) {
    setRows((prev) => (prev ? prev.filter((r) => r.key !== key) : prev));
  }

  function rowError(r: ParsedRow, allRows: ParsedRow[]): string {
    if (!r.firstName.trim()) return "Missing first name";
    if (r.memberId.trim()) {
      const dupe = allRows.filter(
        (x) => x.key !== r.key && x.memberId.trim().toLowerCase() === r.memberId.trim().toLowerCase()
      );
      if (dupe.length) return "Duplicate Institute ID in this batch";
    }
    if (!r.centerName.trim() && !r.sectionName.trim()) return ""; // no section placement, account-only — fine
    if ((r.centerName.trim() && !r.sectionName.trim()) || (!r.centerName.trim() && r.sectionName.trim()))
      return "Pick both Center and Section, or leave both blank";
    // Center/Section given — must resolve for EVERY selected program, since
    // each one gets its own membership. Partial resolution would otherwise
    // silently enroll a student into some programs but not others with no
    // visible signal why.
    for (const res of resolveRowForPrograms(r)) {
      if (!res.centerId) return `Unknown center "${r.centerName}" for ${res.programName}`;
      if (!res.sectionId) return `Unknown section "${r.sectionName}" for ${res.programName}`;
    }
    return "";
  }

  async function uploadAll() {
    if (!rows) return;
    setUploading(true);
    const out: RowResult[] = [];
    for (const r of rows) {
      const err = rowError(r, rows);
      if (err) {
        out.push({ key: r.key, ok: false, error: err });
        continue;
      }
      try {
        const res = await fetch("/api/cmds/tools/people", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            firstName: r.firstName,
            lastName: r.lastName,
            memberId: r.memberId,
            email: r.email,
            contactNumber: r.contactNumber,
            profile: "STUDENT",
            // Bulk uploads: each student's password is their Login ID (MemberId).
            passwordIsMemberId: true,
            programMemberships:
              r.centerName.trim() && r.sectionName.trim()
                ? resolveRowForPrograms(r)
                    .filter((res) => res.centerId && res.sectionId)
                    .map((res) => ({ programId: res.programId, centerId: res.centerId, sectionId: res.sectionId }))
                : [],
          }),
        });
        const d = await res.json().catch(() => ({}));
        const name = [r.firstName, r.lastName].filter(Boolean).join(" ");
        if (!res.ok || d.error) out.push({ key: r.key, ok: false, name, error: d.error || "Failed" });
        else out.push({ key: r.key, ok: true, name, memberId: d.memberId, password: d.password });
      } catch {
        out.push({ key: r.key, ok: false, error: "Network error" });
      }
    }
    setResults(out);
    setUploading(false);
    setStep("done");
  }

  function downloadCredentials() {
    const created = results.filter((r) => r.ok);
    const csv = [
      "Name,Login ID,Password",
      ...created.map((r) => `${(r.name || "").replace(/,/g, " ")},${r.memberId},${r.password}`),
    ].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "student_logins.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  if (!session) return null;

  const okCount = rows ? rows.filter((r) => !rowError(r, rows)).length : 0;
  const errCount = rows ? rows.length - okCount : 0;

  return (
    <CmdsShell active="resources">
      <main className="mx-auto max-w-5xl px-4 py-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-slate-800">Bulk Upload Students</h1>
            <p className="mt-1 text-slate-500">
              Upload a CSV of students into one or more Programs at once — review before anything is created.
            </p>
          </div>
          <Link href="/cmds/tools/people" className="text-sm text-blue-600 hover:underline">
            ← People Management
          </Link>
        </div>

        {step === "input" && (
          <div className="mt-6 space-y-5">
            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <label className="block text-sm font-medium text-slate-700">Programs</label>
              <p className="mt-1 text-xs text-slate-400">
                Every student in this batch is assigned into all programs checked below — the same Center/Section
                names from the CSV are resolved separately against each one.
              </p>
              <div className="mt-2 flex flex-wrap gap-2">
                {programs.map((p) => {
                  const checked = programIds.includes(p.id);
                  return (
                    <label
                      key={p.id}
                      className={`cursor-pointer rounded-md border px-3 py-1.5 text-sm transition ${
                        checked
                          ? "border-blue-400 bg-blue-50 text-blue-800"
                          : "border-slate-300 text-slate-600 hover:bg-slate-50"
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={(e) =>
                          setProgramIds((prev) =>
                            e.target.checked ? [...prev, p.id] : prev.filter((id) => id !== p.id)
                          )
                        }
                        className="mr-1.5 accent-blue-600"
                      />
                      {p.name}
                    </label>
                  );
                })}
                {programs.length === 0 && <span className="text-sm text-slate-400">Loading programs…</span>}
              </div>
            </div>

            <div className="rounded-xl bg-white p-5 ring-1 ring-black/5">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-medium text-slate-700">Student CSV</label>
                <div className="flex items-center gap-2">
                  <button
                    onClick={downloadTemplate}
                    className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-100"
                  >
                    Download template
                  </button>
                  <label className="cursor-pointer rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-100">
                    Upload .csv
                    <input
                      type="file"
                      accept=".csv,.txt"
                      className="hidden"
                      onChange={(e) => {
                        const f = e.target.files?.[0];
                        if (f) f.text().then(setRaw);
                        e.target.value = "";
                      }}
                    />
                  </label>
                </div>
              </div>
              <p className="mt-1 text-xs text-slate-400">
                Columns: {TEMPLATE_HEADERS.join(", ")}. MemberId is optional (auto-generated if blank); Center/Section
                are optional but must both be given, or both left blank. Each student logs in with their MemberId as
                both username and password; download the logins after the upload.
              </p>
              <textarea
                value={raw}
                onChange={(e) => setRaw(e.target.value)}
                rows={14}
                className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-xs text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder={"MemberId,First Name,Last Name,Email,Contact No,Center,Section\n,Riya,Sharma,riya@example.com,9876543210,Center A,Section A"}
              />
            </div>

            {parseError && (
              <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
                {parseError}
              </div>
            )}

            <button
              onClick={parse}
              disabled={!raw.trim()}
              className="rounded-md bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
            >
              Parse & Review
            </button>
          </div>
        )}

        {step === "review" && rows && (
          <div className="mt-6 space-y-4">
            <div className="flex items-center justify-between rounded-md bg-blue-50 px-4 py-3 text-sm text-blue-800 ring-1 ring-blue-200">
              <span>
                Parsed {rows.length} row{rows.length === 1 ? "" : "s"} — {okCount} ready
                {errCount > 0 && <span className="font-medium text-amber-700">, {errCount} need fixing</span>}.
              </span>
              <button onClick={() => setStep("input")} className="text-blue-700 hover:underline">
                ← Back
              </button>
            </div>

            <div className="overflow-x-auto rounded-xl bg-white ring-1 ring-black/5">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                    <th className="px-3 py-2 font-medium">Institute ID</th>
                    <th className="px-3 py-2 font-medium">First name</th>
                    <th className="px-3 py-2 font-medium">Last name</th>
                    <th className="px-3 py-2 font-medium">Email</th>
                    <th className="px-3 py-2 font-medium">Contact</th>
                    <th className="px-3 py-2 font-medium">Center</th>
                    <th className="px-3 py-2 font-medium">Section</th>
                    <th className="px-3 py-2 font-medium"></th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((r) => {
                    const err = rowError(r, rows);
                    return (
                      <tr key={r.key} className={`border-b border-slate-100 ${err ? "bg-amber-50" : ""}`}>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.memberId}
                            onChange={(e) => updateRow(r.key, { memberId: e.target.value })}
                            className="w-28 rounded border border-slate-200 px-2 py-1 text-xs"
                            placeholder="auto"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.firstName}
                            onChange={(e) => updateRow(r.key, { firstName: e.target.value })}
                            className="w-28 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.lastName}
                            onChange={(e) => updateRow(r.key, { lastName: e.target.value })}
                            className="w-28 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.email}
                            onChange={(e) => updateRow(r.key, { email: e.target.value })}
                            className="w-40 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.contactNumber}
                            onChange={(e) => updateRow(r.key, { contactNumber: e.target.value })}
                            className="w-24 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.centerName}
                            onChange={(e) => updateRow(r.key, { centerName: e.target.value })}
                            placeholder="—"
                            className="w-28 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5">
                          <input
                            value={r.sectionName}
                            onChange={(e) => updateRow(r.key, { sectionName: e.target.value })}
                            placeholder="—"
                            className="w-28 rounded border border-slate-200 px-2 py-1 text-xs"
                          />
                        </td>
                        <td className="px-3 py-1.5 text-xs">
                          {err ? (
                            <span className="text-amber-700" title={err}>
                              ⚠
                            </span>
                          ) : programIds.length > 1 && r.centerName.trim() ? (
                            <span
                              className="text-emerald-600"
                              title={`Resolved for: ${resolveRowForPrograms(r)
                                .map((res) => res.programName)
                                .join(", ")}`}
                            >
                              ✓ ({programIds.length})
                            </span>
                          ) : (
                            <span className="text-emerald-600">✓</span>
                          )}
                          <button
                            onClick={() => removeRow(r.key)}
                            className="ml-2 text-slate-400 hover:text-red-500"
                          >
                            ✕
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <button
              onClick={uploadAll}
              disabled={uploading || okCount === 0}
              className="rounded-md bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
            >
              {uploading ? "Uploading…" : `Upload ${okCount} student${okCount === 1 ? "" : "s"}`}
            </button>
            {errCount > 0 && (
              <p className="text-xs text-slate-400">
                Rows marked ⚠ will be skipped — fix them above or remove them before uploading.
              </p>
            )}
          </div>
        )}

        {step === "done" && (
          <div className="mt-6 rounded-xl bg-white p-8 ring-1 ring-black/5">
            <div className="text-center">
              <div className="text-4xl">{results.every((r) => r.ok) ? "✅" : "⚠️"}</div>
              <h2 className="mt-3 text-xl font-semibold text-slate-800">
                Created {results.filter((r) => r.ok).length} of {results.length}
              </h2>
            </div>

            {results.some((r) => r.ok) && (
              <div className="mt-6">
                <div className="mb-2 flex items-center justify-between">
                  <h3 className="text-sm font-medium text-slate-700">Login credentials — share with students</h3>
                  <button
                    onClick={downloadCredentials}
                    className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-600 hover:bg-slate-100"
                  >
                    Download CSV
                  </button>
                </div>
                <div className="overflow-x-auto rounded-lg ring-1 ring-black/5">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                        <th className="px-3 py-2">Name</th>
                        <th className="px-3 py-2">Login ID</th>
                        <th className="px-3 py-2">Password</th>
                      </tr>
                    </thead>
                    <tbody>
                      {results
                        .filter((r) => r.ok)
                        .map((r) => (
                          <tr key={r.key} className="border-t border-slate-100">
                            <td className="px-3 py-1.5 text-slate-700">{r.name}</td>
                            <td className="px-3 py-1.5 font-mono text-xs">{r.memberId}</td>
                            <td className="px-3 py-1.5 font-mono text-xs">{r.password}</td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {results.some((r) => !r.ok) && (
              <div className="mt-6">
                <h3 className="mb-2 text-sm font-medium text-slate-700">Failed rows</h3>
                <div className="rounded-md bg-red-50 p-3 text-xs text-red-700 ring-1 ring-red-200">
                  {results
                    .filter((r) => !r.ok)
                    .map((r) => (
                      <div key={r.key}>
                        Row {r.key + 2}{r.name ? ` (${r.name})` : ""}: {r.error}
                      </div>
                    ))}
                </div>
              </div>
            )}

            <div className="mt-6 flex justify-center gap-3">
              <Link
                href="/cmds/tools/people"
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
              >
                Go to People Management
              </Link>
              <button
                onClick={() => {
                  setStep("input");
                  setRows(null);
                  setResults([]);
                  setRaw("");
                }}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
              >
                Upload more
              </button>
            </div>
          </div>
        )}
      </main>
    </CmdsShell>
  );
}
