"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession } from "@/lib/session";

type Program = { id: string; name: string; code: string | null; description: string; isOffline: boolean };
type Center = { id: string; name: string };
type Section = { id: string; name: string; centerId: string | null };
type Counts = { teachers: number; students: number; content: number };

type TabKey = "content" | "members" | "students" | "organizations" | "marksheets";
const TABS: { key: TabKey; label: string }[] = [
  { key: "content", label: "Content" },
  { key: "members", label: "Members" },
  { key: "students", label: "Students" },
  { key: "organizations", label: "Organizations" },
  { key: "marksheets", label: "Upload Mark Sheets" },
];

export default function ProgramDetailPage() {
  const params = useParams();
  const id = String(params.id);
  const [program, setProgram] = useState<Program | null>(null);
  const [centers, setCenters] = useState<Center[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [counts, setCounts] = useState<Counts>({ teachers: 0, students: 0, content: 0 });
  const [tab, setTab] = useState<TabKey>("content");
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");

  useEffect(() => {
    fetch(`/api/cmds/programs/${id}`)
      .then((r) => r.json())
      .then((d) => {
        setProgram(d.program || null);
        setCenters(d.centers || []);
        setSections(d.sections || []);
        setCounts(d.counts || { teachers: 0, students: 0, content: 0 });
      })
      .finally(() => setLoading(false));
  }, [id]);

  const searchPlaceholder =
    tab === "students" || tab === "members"
      ? "Search Students"
      : tab === "marksheets"
      ? "Search Mark Sheets"
      : "Search Content";

  return (
    <CmdsShell active="programs">
      {/* Program header */}
      <div className="border-b border-slate-200 bg-slate-50 px-8 py-4">
        <div className="mx-auto flex max-w-[1100px] items-center justify-between">
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-xl font-semibold text-slate-800">
                {loading ? "Program" : program?.name || "Program"}
              </h1>
              <Link href="/cmds/programs" className="text-xs text-blue-600 hover:underline">
                Change Program
              </Link>
            </div>
            <div className="mt-0.5 text-sm text-slate-500">
              {centers[0]?.name || "All Centers"} · {sections[0]?.name || "All Sections"}
            </div>
          </div>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={searchPlaceholder}
            className="w-56 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
        </div>
      </div>

      <div className="mx-auto flex max-w-[1100px]">
        {/* Left rail */}
        <aside className="w-[200px] shrink-0 border-r border-slate-100 py-6">
          <nav className="flex flex-col">
            {TABS.map((t) => {
              const badge =
                t.key === "members"
                  ? counts.teachers
                  : t.key === "students"
                  ? counts.students
                  : t.key === "content"
                  ? counts.content
                  : 0;
              return (
                <button
                  key={t.key}
                  onClick={() => setTab(t.key)}
                  className={`flex items-center justify-between border-l-[3px] px-5 py-3 text-left text-[13px] ${
                    tab === t.key
                      ? "border-emerald-500 font-semibold text-slate-900"
                      : "border-transparent text-slate-500 hover:text-slate-700"
                  }`}
                >
                  {t.label}
                  {badge > 0 && <span className="text-xs text-slate-400">{badge}</span>}
                </button>
              );
            })}
          </nav>
        </aside>

        {/* Main */}
        <main className="flex-1 px-8 py-6">
          {tab === "content" && <ContentTab query={query} />}
          {tab === "members" && <PeopleTab profile="TEACHER" query={query} label="teachers" />}
          {tab === "students" && <PeopleTab profile="STUDENT" query={query} label="students" />}
          {tab === "organizations" && (
            <EmptyPanel
              title="Organizations"
              text="No organizations are sharing this program yet."
            />
          )}
          {tab === "marksheets" && <MarkSheetsTab programId={id} />}
        </main>
      </div>
    </CmdsShell>
  );
}

function ContentTab({ query }: { query: string }) {
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    fetch("/api/cmds/content")
      .then((r) => r.json())
      .then((d) => setRows(d.resources || []))
      .finally(() => setLoading(false));
  }, []);
  const visible = useMemo(
    () =>
      rows.filter((r) => (r.title || "").toLowerCase().includes(query.toLowerCase())),
    [rows, query]
  );

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-600">Content</h2>
        <Link
          href="/cmds"
          className="rounded bg-[#e8443b] px-3 py-1.5 text-xs font-medium text-white hover:bg-[#d13a32]"
        >
          + Add Content
        </Link>
      </div>
      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Title</th>
              <th className="px-4 py-2 font-medium">Type</th>
              <th className="px-4 py-2 font-medium">Visibility Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={3} className="px-4 py-10 text-center text-slate-400">
                  Loading…
                </td>
              </tr>
            ) : visible.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-4 py-10 text-center text-slate-400">
                  No content
                </td>
              </tr>
            ) : (
              visible.map((r) => (
                <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-700">{r.title}</td>
                  <td className="px-4 py-3 text-slate-500">{r.type}</td>
                  <td className="px-4 py-3">
                    <span className="text-emerald-600">● Published</span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function PeopleTab({ profile, query, label }: { profile: string; query: string; label: string }) {
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    fetch(`/api/cmds/tools/people?profile=${profile}`)
      .then((r) => r.json())
      .then((d) => setRows(d.members || []))
      .finally(() => setLoading(false));
  }, [profile]);
  const visible = useMemo(
    () =>
      rows.filter((m) =>
        `${m.firstName} ${m.lastName} ${m.memberId}`.toLowerCase().includes(query.toLowerCase())
      ),
    [rows, query]
  );

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold capitalize text-slate-600">{label}</h2>
        <Link
          href="/cmds/tools/people"
          className="rounded bg-[#e8443b] px-3 py-1.5 text-xs font-medium text-white hover:bg-[#d13a32]"
        >
          + Add {profile === "TEACHER" ? "Teachers" : "Students"}
        </Link>
      </div>
      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Name</th>
              <th className="px-4 py-2 font-medium">{profile === "STUDENT" ? "Enrollment ID" : "ID"}</th>
              <th className="px-4 py-2 font-medium">Email</th>
              <th className="px-4 py-2 font-medium">Role</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="px-4 py-10 text-center text-slate-400">
                  Loading…
                </td>
              </tr>
            ) : visible.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-10 text-center text-slate-400">
                  No {label} found
                </td>
              </tr>
            ) : (
              visible.map((m) => (
                <tr key={m.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-700">
                    {m.firstName} {m.lastName}
                  </td>
                  <td className="px-4 py-3 text-slate-500">{m.memberId}</td>
                  <td className="px-4 py-3 text-slate-500">{m.email || "—"}</td>
                  <td className="px-4 py-3 text-slate-500">{m.profile}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

type MarkSheet = {
  id: string;
  name: string;
  url: string | null;
  fileSize: number;
  uploadedAt: number;
  status: string;
};

function MarkSheetsTab({ programId }: { programId: string }) {
  const [items, setItems] = useState<MarkSheet[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    try {
      const res = await fetch(`/api/cmds/programs/${programId}/marksheets`);
      const d = await res.json();
      setItems(d.items || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [programId]);

  async function onFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    setUploading(true);
    setError(null);
    const fd = new FormData();
    fd.append("file", file);
    fd.append("userId", getSession()?.id || "");
    const res = await fetch(`/api/cmds/programs/${programId}/marksheets`, {
      method: "POST",
      body: fd,
    });
    setUploading(false);
    if (!res.ok) {
      const d = await res.json().catch(() => ({}));
      setError(d.error || "Upload failed");
      return;
    }
    load();
  }

  const uploadBtn = (
    <label className="cursor-pointer rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700">
      {uploading ? "Uploading…" : "Upload .xls / .xlsx"}
      <input type="file" accept=".xls,.xlsx,.csv" className="hidden" onChange={onFile} disabled={uploading} />
    </label>
  );

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-600">List of Offline Tests Uploaded</h2>
        {items.length > 0 && uploadBtn}
      </div>

      {error && <div className="mt-3 text-sm text-red-500">{error}</div>}

      {loading ? (
        <div className="mt-4 py-14 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-4 flex flex-col items-center justify-center rounded border border-dashed border-slate-200 py-14 text-center">
          <div className="text-3xl">📄</div>
          <div className="mt-2 text-sm text-slate-500">No mark sheets uploaded yet</div>
          <div className="mt-3">{uploadBtn}</div>
        </div>
      ) : (
        <div className="mt-4 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">File</th>
                <th className="px-4 py-2 font-medium">Size</th>
                <th className="px-4 py-2 font-medium">Uploaded</th>
                <th className="px-4 py-2 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {items.map((m) => (
                <tr key={m.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <a href={m.url || "#"} target="_blank" className="text-slate-700 hover:text-blue-600">
                      📄 {m.name}
                    </a>
                  </td>
                  <td className="px-4 py-3 text-slate-500">
                    {m.fileSize ? `${Math.round(m.fileSize / 1024)} KB` : "—"}
                  </td>
                  <td className="px-4 py-3 text-slate-500">
                    {m.uploadedAt ? new Date(m.uploadedAt).toLocaleString() : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-600">
                      {m.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function EmptyPanel({ title, text }: { title: string; text: string }) {
  return (
    <div>
      <h2 className="text-sm font-semibold text-slate-600">{title}</h2>
      <div className="mt-4 rounded border border-dashed border-slate-200 py-14 text-center text-sm text-slate-400">
        {text}
      </div>
    </div>
  );
}
