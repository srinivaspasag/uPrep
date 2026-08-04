"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession } from "@/lib/session";
import { canManageContent } from "@/lib/roles";

type Program = { id: string; name: string; code: string | null; description: string; isOffline: boolean };
type Center = { id: string; name: string };
type Section = {
  id: string;
  name: string;
  centerId: string | null;
  courses?: { id: string; name: string }[];
};
type Counts = { teachers: number; students: number; content: number };

type TabKey = "content" | "members" | "students" | "organizations" | "marksheets" | "analytics";
const TABS: { key: TabKey; label: string }[] = [
  { key: "content", label: "Content" },
  { key: "members", label: "Members" },
  { key: "students", label: "Students" },
  { key: "organizations", label: "Organizations" },
  { key: "marksheets", label: "Upload Mark Sheets" },
  { key: "analytics", label: "Analytics" },
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
          {tab === "content" && (
            <ContentTab query={query} programId={id} centers={centers} sections={sections} />
          )}
          {tab === "members" && <PeopleTab profile="TEACHER" query={query} label="teachers" programId={id} />}
          {tab === "students" && <PeopleTab profile="STUDENT" query={query} label="students" programId={id} />}
          {tab === "organizations" && (
            <EmptyPanel
              title="Organizations"
              text="No organizations are sharing this program yet."
            />
          )}
          {tab === "analytics" && <ProgramAnalyticsTab programId={id} />}
          {tab === "marksheets" && <MarkSheetsTab programId={id} />}
        </main>
      </div>
    </CmdsShell>
  );
}

// Content is published per Section (Program+Center+Section), matching legacy:
// an admin picks a Center then a Section, adds items to it (not yet visible
// to students), then bulk-selects rows and runs "Choose Operation" to
// actually publish (Make Visible), toggle download, or remove from section.
function ContentTab({
  query,
  programId,
  centers,
  sections,
}: {
  query: string;
  programId: string;
  centers: { id: string; name: string }[];
  sections: {
    id: string;
    name: string;
    centerId: string | null;
    programId?: string | null;
    courses?: { id: string; name: string }[];
  }[];
}) {
  const isAdmin = (getSession()?.profile || "").trim().toUpperCase() === "MANAGER";
  // Salesperson is excluded from content, matching legacy — Members/Students/
  // Marksheets tabs on this same page stay open to them, only Content doesn't.
  const canAccess = canManageContent(getSession()?.profile);

  const programSections = useMemo(
    () => sections.filter((s) => !s.programId || s.programId === programId),
    [sections, programId]
  );
  const [centerId, setCenterId] = useState("");
  const [sectionId, setSectionId] = useState("");

  useEffect(() => {
    if (!centerId && centers[0]) setCenterId(centers[0].id);
  }, [centers, centerId]);

  const centerSections = useMemo(
    () => programSections.filter((s) => !centerId || s.centerId === centerId),
    [programSections, centerId]
  );

  useEffect(() => {
    if (centerSections.length && !centerSections.some((s) => s.id === sectionId)) {
      setSectionId(centerSections[0].id);
    } else if (!centerSections.length && sectionId) {
      setSectionId("");
    }
  }, [centerSections, sectionId]);

  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [checked, setChecked] = useState<Set<string>>(new Set());
  const [op, setOp] = useState("");
  const [showPicker, setShowPicker] = useState(false);

  async function load() {
    if (!sectionId) {
      setRows([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    const d = await (await fetch(`/api/cmds/content?sectionId=${sectionId}`)).json();
    setRows(d.resources || []);
    setChecked(new Set());
    setLoading(false);
  }
  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sectionId]);

  const visible = useMemo(
    () => rows.filter((r) => (r.title || "").toLowerCase().includes(query.toLowerCase())),
    [rows, query]
  );

  function toggleChecked(id: string) {
    setChecked((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAll() {
    setChecked((prev) => (prev.size === visible.length ? new Set() : new Set(visible.map((r) => r.id))));
  }

  async function applyOp() {
    if (!op || checked.size === 0 || !sectionId) return;
    setBusy(true);
    const items = visible.filter((r) => checked.has(r.id)).map((r) => ({ id: r.id, type: r.type }));
    await fetch("/api/cmds/content/bulk", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ items, action: op, sectionId }),
    });
    setOp("");
    setBusy(false);
    load();
  }

  if (!canAccess) {
    return <div className="py-16 text-center text-slate-400">You don&apos;t have access to Content.</div>;
  }

  const currentSection = centerSections.find((s) => s.id === sectionId);
  const grantedCourses = currentSection?.courses || [];

  return (
    <div>
      {sectionId && (
        <div className="mb-4 rounded-md bg-blue-50 px-4 py-3 text-sm ring-1 ring-blue-100">
          <div className="flex items-center justify-between">
            <span className="font-medium text-blue-900">
              Course subjects this section's students see in "My Courses"
            </span>
            <Link href="/cmds/tools/academic" className="text-xs text-blue-700 hover:underline">
              Manage in Academic Structure →
            </Link>
          </div>
          {grantedCourses.length === 0 ? (
            <p className="mt-1 text-blue-700">
              None assigned — this section grants no whole subjects yet, only the loose files listed below (if any).
            </p>
          ) : (
            <p className="mt-1 text-blue-700">
              {grantedCourses.map((c) => c.name).join(", ")}
            </p>
          )}
          <p className="mt-1 text-xs text-blue-500">
            This is separate from the table below — that's individual files added directly to this section; this is
            the whole-subject grant that drives the student's course list.
          </p>
        </div>
      )}
      <div className="flex flex-wrap items-center gap-3">
        <select
          value={centerId}
          onChange={(e) => setCenterId(e.target.value)}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs"
        >
          {centers.length === 0 && <option value="">No centers</option>}
          {centers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <select
          value={sectionId}
          onChange={(e) => setSectionId(e.target.value)}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs"
        >
          {centerSections.length === 0 && <option value="">No sections</option>}
          {centerSections.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>

        <div className="ml-auto flex items-center gap-2">
          {isAdmin && checked.size > 0 && (
            <>
              <select
                value={op}
                onChange={(e) => setOp(e.target.value)}
                className="rounded border border-slate-300 px-2 py-1.5 text-xs"
              >
                <option value="">Choose Operation ({checked.size})</option>
                <option value="visible">Make Visible</option>
                <option value="invisible">Make Invisible</option>
                <option value="enableDownload">Enable Download</option>
                <option value="disableDownload">Disable Download</option>
                <option value="removeFromSection">Remove From Section</option>
              </select>
              <button
                disabled={!op || busy}
                onClick={applyOp}
                className="rounded bg-slate-800 px-3 py-1.5 text-xs font-medium text-white hover:bg-slate-700 disabled:opacity-50"
              >
                Go
              </button>
            </>
          )}
          {isAdmin && sectionId && (
            <button
              onClick={() => setShowPicker(true)}
              className="rounded bg-[#e8443b] px-3 py-1.5 text-xs font-medium text-white hover:bg-[#d13a32]"
            >
              + Add Content
            </button>
          )}
        </div>
      </div>

      <p className="mt-2 text-xs text-slate-400">
        Content added to a section is not shown to students until it&apos;s explicitly made
        Visible. (Org-wide hide/show is still set from Resources.)
      </p>

      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="w-8 px-4 py-2">
                {isAdmin && visible.length > 0 && (
                  <input type="checkbox" checked={checked.size === visible.length} onChange={toggleAll} />
                )}
              </th>
              <th className="px-4 py-2 font-medium">Title</th>
              <th className="px-4 py-2 font-medium">Type</th>
              <th className="px-4 py-2 font-medium">Visibility</th>
              <th className="px-4 py-2 font-medium">Download</th>
            </tr>
          </thead>
          <tbody>
            {!sectionId ? (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                  {centers.length === 0
                    ? "No centers set up for this program yet."
                    : "No sections in this center yet — create one from Academic Structure."}
                </td>
              </tr>
            ) : loading ? (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                  Loading…
                </td>
              </tr>
            ) : visible.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                  No content added to this section yet
                </td>
              </tr>
            ) : (
              visible.map((r) => {
                const isVisible = Array.isArray(r.visibleSectionIds) && r.visibleSectionIds.includes(sectionId);
                return (
                  <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3">
                      {isAdmin && (
                        <input
                          type="checkbox"
                          checked={checked.has(r.id)}
                          onChange={() => toggleChecked(r.id)}
                        />
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-700">{r.title}</td>
                    <td className="px-4 py-3 text-slate-500">{r.type}</td>
                    <td className="px-4 py-3">
                      {r.hidden ? (
                        <span className="text-amber-600">● Hidden org-wide</span>
                      ) : isVisible ? (
                        <span className="text-emerald-600">● Visible</span>
                      ) : (
                        <span className="text-amber-600">● Not Visible</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {r.downloadEnabled === false ? "Disabled" : "Enabled"}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {showPicker && sectionId && (
        <AddContentModal
          sectionId={sectionId}
          onClose={() => setShowPicker(false)}
          onAdded={() => {
            setShowPicker(false);
            load();
          }}
        />
      )}
    </div>
  );
}

// Browse the general Resources tree and multi-select items to add to a
// section — the "select ebook/video from content" step of the legacy flow.
function AddContentModal({
  sectionId,
  onClose,
  onAdded,
}: {
  sectionId: string;
  onClose: () => void;
  onAdded: () => void;
}) {
  const [parentId, setParentId] = useState<string | null>(null);
  const [breadcrumb, setBreadcrumb] = useState<{ id: string | null; name: string }[]>([
    { id: null, name: "Resources" },
  ]);
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [picked, setPicked] = useState<Map<string, string>>(new Map());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setLoading(true);
    const url = parentId ? `/api/cmds/content?parentId=${parentId}` : "/api/cmds/content";
    fetch(url)
      .then((r) => r.json())
      .then((d) => setRows(d.resources || []))
      .finally(() => setLoading(false));
  }, [parentId]);

  function enterFolder(f: any) {
    setBreadcrumb((prev) => [...prev, { id: f.id, name: f.title }]);
    setParentId(f.id);
  }
  function goTo(index: number) {
    setBreadcrumb((prev) => prev.slice(0, index + 1));
    setParentId(breadcrumb[index].id);
  }

  function togglePick(r: any) {
    setPicked((prev) => {
      const next = new Map(prev);
      if (next.has(r.id)) next.delete(r.id);
      else next.set(r.id, r.type);
      return next;
    });
  }

  async function confirm() {
    if (picked.size === 0) return;
    setSaving(true);
    const items = Array.from(picked.entries()).map(([id, type]) => ({ id, type }));
    await fetch("/api/cmds/content/bulk", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ items, action: "addToSection", sectionId }),
    });
    setSaving(false);
    onAdded();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="flex max-h-[80vh] w-full max-w-2xl flex-col rounded-lg bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
          <h3 className="text-sm font-semibold text-slate-700">Add Content to Section</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            ✕
          </button>
        </div>
        <div className="flex flex-wrap items-center gap-1 border-b border-slate-100 px-5 py-2 text-xs text-slate-500">
          {breadcrumb.map((b, i) => (
            <span key={i}>
              {i > 0 && <span className="mx-1 text-slate-300">/</span>}
              <button onClick={() => goTo(i)} className="hover:text-blue-600 hover:underline">
                {b.name}
              </button>
            </span>
          ))}
        </div>
        <div className="flex-1 overflow-y-auto px-5 py-3">
          {loading ? (
            <div className="py-10 text-center text-sm text-slate-400">Loading…</div>
          ) : rows.length === 0 ? (
            <div className="py-10 text-center text-sm text-slate-400">Empty folder</div>
          ) : (
            <ul className="divide-y divide-slate-100">
              {rows.map((r) => (
                <li key={r.id} className="flex items-center gap-3 py-2">
                  {r.type === "FOLDER" ? (
                    <button
                      onClick={() => enterFolder(r)}
                      className="flex-1 text-left text-sm text-slate-700 hover:text-blue-600"
                    >
                      📁 {r.title}
                    </button>
                  ) : (
                    <>
                      <input type="checkbox" checked={picked.has(r.id)} onChange={() => togglePick(r)} />
                      <span className="flex-1 text-sm text-slate-700">{r.title}</span>
                      <span className="text-xs text-slate-400">{r.type}</span>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
        <div className="flex items-center justify-between border-t border-slate-200 px-5 py-3">
          <span className="text-xs text-slate-500">{picked.size} selected</span>
          <div className="flex gap-2">
            <button
              onClick={onClose}
              className="rounded border border-slate-200 px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              disabled={picked.size === 0 || saving}
              onClick={confirm}
              className="rounded bg-emerald-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Adding…" : `Add ${picked.size || ""} item${picked.size === 1 ? "" : "s"}`}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function PeopleTab({
  profile,
  query,
  label,
  programId,
}: {
  profile: string;
  query: string;
  label: string;
  programId: string;
}) {
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    fetch(`/api/cmds/tools/people?profile=${profile}&programId=${programId}`)
      .then((r) => r.json())
      .then((d) => setRows(d.members || []))
      .finally(() => setLoading(false));
  }, [profile, programId]);
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
          href={`/cmds/tools/people?programId=${encodeURIComponent(programId)}&profile=${profile}`}
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

type ProgramTest = {
  id: string;
  name: string;
  date: number;
  attempts: number;
  students: number;
  avgPercent: number;
  topperName: string | null;
  topperPercent: number | null;
};

// Legacy's program-level Analytics screen (Institute.java:1070 testAnalytics)
// — select a program, see a tests-over-time performance graph plus every
// test with its topper. Scoped here to tests attempted by this program's
// enrolled students (the join key programMemberships already uses
// everywhere else), since there's no direct test<->program link.
function ProgramAnalyticsTab({ programId }: { programId: string }) {
  const [tests, setTests] = useState<ProgramTest[]>([]);
  const [studentCount, setStudentCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    fetch(`/api/cmds/programs/${programId}/analytics`)
      .then((r) => r.json())
      .then((d) => {
        setTests(d.tests || []);
        setStudentCount(d.studentCount || 0);
      })
      .finally(() => setLoading(false));
  }, [programId]);

  if (loading) return <div className="py-16 text-center text-sm text-slate-400">Loading analytics…</div>;

  if (tests.length === 0) {
    return (
      <EmptyPanel
        title="Analytics"
        text={
          studentCount === 0
            ? "No students enrolled in this program yet."
            : "No completed test attempts from this program's students yet."
        }
      />
    );
  }

  const maxPercent = Math.max(100, ...tests.map((t) => t.avgPercent));
  const chartHeight = 160;
  const barWidth = Math.max(24, Math.min(56, Math.floor(640 / tests.length) - 8));

  return (
    <div>
      <h2 className="text-sm font-semibold text-slate-600">Analytics</h2>
      <p className="mt-1 text-xs text-slate-400">
        {tests.length} test{tests.length === 1 ? "" : "s"} attempted by {studentCount} enrolled student
        {studentCount === 1 ? "" : "s"}.
      </p>

      {/* Tests-over-time graph — average score % per test, in date order. */}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 p-4">
        <svg width={Math.max(640, tests.length * (barWidth + 8))} height={chartHeight + 40}>
          {tests.map((t, i) => {
            const h = (t.avgPercent / maxPercent) * chartHeight;
            const x = i * (barWidth + 8);
            return (
              <g key={t.id}>
                <title>
                  {t.name} — {t.avgPercent}% avg, {new Date(t.date).toLocaleDateString()}
                </title>
                <rect
                  x={x}
                  y={chartHeight - h}
                  width={barWidth}
                  height={h}
                  rx={3}
                  className="fill-indigo-400 hover:fill-indigo-500"
                />
                <text x={x + barWidth / 2} y={chartHeight - h - 6} textAnchor="middle" className="fill-slate-600 text-[11px]">
                  {t.avgPercent}%
                </text>
                <text
                  x={x + barWidth / 2}
                  y={chartHeight + 16}
                  textAnchor="middle"
                  className="fill-slate-400 text-[10px]"
                >
                  {new Date(t.date).toLocaleDateString(undefined, { month: "short", day: "numeric" })}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Every test with its topper. */}
      <div className="mt-6 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Test</th>
              <th className="px-4 py-2 font-medium">Date</th>
              <th className="px-4 py-2 font-medium">Attempts</th>
              <th className="px-4 py-2 font-medium">Avg %</th>
              <th className="px-4 py-2 font-medium">Topper</th>
              <th className="px-4 py-2 font-medium" />
            </tr>
          </thead>
          <tbody>
            {[...tests].reverse().map((t) => (
              <tr key={t.id} className="border-b border-slate-100">
                <td className="px-4 py-2 font-medium text-slate-700">{t.name}</td>
                <td className="px-4 py-2 text-slate-400">{new Date(t.date).toLocaleDateString()}</td>
                <td className="px-4 py-2 text-slate-500">
                  {t.attempts} ({t.students} student{t.students === 1 ? "" : "s"})
                </td>
                <td className="px-4 py-2 text-slate-700">{t.avgPercent}%</td>
                <td className="px-4 py-2 text-slate-600">
                  {t.topperName ? (
                    <>
                      {t.topperName} <span className="text-slate-400">({t.topperPercent}%)</span>
                    </>
                  ) : (
                    "—"
                  )}
                </td>
                <td className="px-4 py-2">
                  <Link
                    href={`/cmds/tests/analytics?testId=${t.id}`}
                    className="text-xs font-medium text-blue-600 hover:underline"
                  >
                    Open →
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
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
