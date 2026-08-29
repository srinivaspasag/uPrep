"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, setSession, setImpersonating, type UprepSession } from "@/lib/session";
import { isStaff } from "@/lib/roles";

type Mapping = { programId: string; centerId: string; sectionId: string };

type Member = {
  id: string;
  memberId: string;
  firstName: string;
  lastName: string;
  email: string;
  profile: string;
  contactNumber: string;
  status: string;
  programMemberships?: Mapping[];
  enrolledCourseIds?: string[];
};

type AcadProgram = { id: string; name: string; centerIds?: string[] };
type AcadCenter = { id: string; name: string };
type AcadSection = { id: string; name: string; programId?: string | null; centerId?: string | null };

const PROFILES = ["STUDENT", "OFFLINE_USER", "TEACHER", "MANAGER", "EDITOR", "SALESPERSON"];

export default function PeoplePage() {
  const router = useRouter();
  const [session, setSessionState] = useState<UprepSession | null>(null);
  const [profile, setProfile] = useState("STUDENT");
  const [query, setQuery] = useState("");
  const [members, setMembers] = useState<Member[]>([]);
  const [counts, setCounts] = useState<Record<string, number>>({});
  const [programCounts, setProgramCounts] = useState<Record<string, number>>({});
  const [unassignedStudents, setUnassignedStudents] = useState(0);
  const [courseNames, setCourseNames] = useState<Record<string, string>>({});
  const [programFilter, setProgramFilter] = useState<string>(""); // "" | "UNASSIGNED" | a programId
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [editing, setEditing] = useState<Member | null>(null);
  const [resetting, setResetting] = useState<Member | null>(null);
  const [emailOpen, setEmailOpen] = useState(false);
  const [acadPrograms, setAcadPrograms] = useState<AcadProgram[]>([]);
  const [acadCenters, setAcadCenters] = useState<AcadCenter[]>([]);
  const [acadSections, setAcadSections] = useState<AcadSection[]>([]);
  // Carries a Program's id through from its "+ Add Students" link (see
  // app/cmds/programs/[id]/page.tsx) so a student added from within a
  // specific program actually lands assigned to it, instead of landing here
  // with no program context and needing a separate, easy-to-miss step.
  const [prefillProgramId, setPrefillProgramId] = useState<string | null>(null);
  const isAdmin = (session?.profile || "").trim().toUpperCase() === "MANAGER";

  async function deactivate(m: Member) {
    if (!confirm(`Deactivate ${m.firstName} ${m.lastName}?`)) return;
    await fetch(`/api/cmds/tools/people?id=${m.id}`, { method: "DELETE" });
    load();
  }

  useEffect(() => {
    setSessionState(getSession());
    fetch("/api/cmds/tools/academic")
      .then((r) => r.json())
      .then((d) => {
        setAcadPrograms(d.programs || []);
        setAcadCenters(d.centers || []);
        setAcadSections(d.sections || []);
      })
      .catch(() => {});
    const sp = new URLSearchParams(window.location.search);
    const pid = sp.get("programId");
    const prof = sp.get("profile");
    if (prof && PROFILES.includes(prof)) setProfile(prof);
    if (pid) {
      setPrefillProgramId(pid);
      setAddOpen(true);
    }
  }, []);

  async function impersonate(m: Member) {
    if (!confirm(`Sign in as ${m.firstName} ${m.lastName}? You can return to your admin account anytime.`))
      return;
    const res = await fetch("/api/auth/impersonate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memberId: m.id }),
    });
    const d = await res.json().catch(() => ({}));
    if (!res.ok) {
      alert(d.error || "Could not start impersonation");
      return;
    }
    const result = d.result;
    setSession(result);
    setImpersonating(`${m.firstName} ${m.lastName}`.trim() || m.memberId || "user");
    router.push(isStaff(result?.profile) ? "/cmds" : "/learn/library");
  }

  async function load() {
    setLoading(true);
    try {
      const programParam =
        programFilter && programFilter !== "UNASSIGNED" ? `&programId=${encodeURIComponent(programFilter)}` : "";
      const res = await fetch(
        `/api/cmds/tools/people?profile=${encodeURIComponent(profile)}&query=${encodeURIComponent(query)}${programParam}`
      );
      const d = await res.json();
      let list: Member[] = d.members || [];
      // "Unassigned" isn't a real programId the backend can filter on —
      // narrow it down here instead.
      if (programFilter === "UNASSIGNED") {
        list = list.filter((m) => !m.programMemberships || m.programMemberships.length === 0);
      }
      setMembers(list);
      setCounts(d.counts || {});
      setProgramCounts(d.programCounts || {});
      setUnassignedStudents(d.unassignedStudents || 0);
      setCourseNames((prev) => ({ ...prev, ...(d.courseNames || {}) }));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [profile, programFilter]);

  function toggleProgramFilter(id: string) {
    setProgramFilter((prev) => (prev === id ? "" : id));
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-light text-slate-700">People Management</h1>
          <div className="flex items-center gap-2">
            {isAdmin && (
              <button
                onClick={() => setEmailOpen(true)}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
              >
                Send Email
              </button>
            )}
            {profile === "STUDENT" && (
              <Link
                href="/cmds/tools/people/bulk"
                className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
              >
                Bulk Upload
              </Link>
            )}
            <button
              onClick={() => setAddOpen(true)}
              className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
            >
              + Add {profile === "STUDENT" ? "Student" : "Member"}
            </button>
          </div>
        </div>

        {/* Profile selector */}
        <div className="mt-5 flex flex-wrap gap-2">
          {PROFILES.map((p) => (
            <button
              key={p}
              onClick={() => {
                setProfile(p);
                setProgramFilter("");
              }}
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                profile === p
                  ? "bg-slate-800 text-white"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              {p.replace("_", " ")}
              {counts[p] ? <span className="ml-1 opacity-70">({counts[p]})</span> : null}
            </button>
          ))}
        </div>

        {/* Students by Program */}
        {profile === "STUDENT" && (acadPrograms.length > 0 || unassignedStudents > 0) && (
          <div className="mt-4 rounded border border-slate-200 bg-slate-50 p-3">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Students by Program
              </span>
              {programFilter && (
                <button
                  onClick={() => setProgramFilter("")}
                  className="text-xs text-blue-600 hover:underline"
                >
                  Clear filter
                </button>
              )}
            </div>
            <div className="flex flex-wrap gap-2">
              {acadPrograms.map((p) => (
                <button
                  key={p.id}
                  onClick={() => toggleProgramFilter(p.id)}
                  className={`rounded-full border px-3 py-1 text-xs ${
                    programFilter === p.id
                      ? "border-slate-800 bg-slate-800 text-white"
                      : "border-slate-200 bg-white text-slate-600 hover:border-slate-400"
                  }`}
                >
                  {p.name}{" "}
                  <span className={`font-semibold ${programFilter === p.id ? "" : "text-slate-800"}`}>
                    {programCounts[p.id] || 0}
                  </span>
                </button>
              ))}
              {unassignedStudents > 0 && (
                <button
                  onClick={() => toggleProgramFilter("UNASSIGNED")}
                  className={`rounded-full border px-3 py-1 text-xs ${
                    programFilter === "UNASSIGNED"
                      ? "border-amber-600 bg-amber-600 text-white"
                      : "border-amber-200 bg-amber-50 text-amber-700 hover:border-amber-400"
                  }`}
                >
                  Unassigned <span className="font-semibold">{unassignedStudents}</span>
                </button>
              )}
            </div>
          </div>
        )}

        {/* Search */}
        <div className="mt-4 flex items-center gap-2">
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && load()}
            placeholder="Search by name / ID / email"
            className="w-72 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
          <button
            onClick={load}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
          >
            Search
          </button>
        </div>

        {/* Table */}
        <div className="mt-4 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Name</th>
                <th className="px-4 py-2 font-medium">Institute ID</th>
                <th className="px-4 py-2 font-medium">Email</th>
                <th className="px-4 py-2 font-medium">Contact</th>
                <th className="px-4 py-2 font-medium">Role</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : members.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                    No {profile.toLowerCase().replace("_", " ")}s found
                    {programFilter === "UNASSIGNED"
                      ? " with no program assigned"
                      : programFilter
                      ? ` in ${acadPrograms.find((p) => p.id === programFilter)?.name || "this program"}`
                      : ""}
                  </td>
                </tr>
              ) : (
                members.map((m) => (
                  <tr key={m.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <span className="flex h-7 w-7 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-600">
                          {(m.firstName || "?").charAt(0).toUpperCase()}
                        </span>
                        {m.firstName} {m.lastName}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-slate-500">{m.memberId}</td>
                    <td className="px-4 py-3 text-slate-500">{m.email || "—"}</td>
                    <td className="px-4 py-3 text-slate-500">{m.contactNumber || "—"}</td>
                    <td className="px-4 py-3">
                      <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-xs text-indigo-600">
                        {m.profile}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-emerald-600">● {m.status}</span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 text-xs">
                        <button
                          onClick={() => setEditing(m)}
                          className="text-blue-600 hover:underline"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => setResetting(m)}
                          className="text-amber-600 hover:underline"
                        >
                          Reset password
                        </button>
                        <button
                          onClick={() => impersonate(m)}
                          className="text-purple-600 hover:underline"
                        >
                          Login as
                        </button>
                        {isAdmin && (
                          <button
                            onClick={() => deactivate(m)}
                            className="text-red-500 hover:underline"
                          >
                            Deactivate
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {addOpen && (
        <AddMemberModal
          profile={profile}
          orgId={undefined}
          userId={session?.id}
          programs={acadPrograms}
          centers={acadCenters}
          sections={acadSections}
          initialProgramId={prefillProgramId}
          onClose={() => {
            setAddOpen(false);
            setPrefillProgramId(null);
          }}
          onDone={() => {
            setAddOpen(false);
            setPrefillProgramId(null);
            load();
          }}
        />
      )}

      {editing && (
        <EditMemberModal
          member={editing}
          programs={acadPrograms}
          centers={acadCenters}
          sections={acadSections}
          courseNames={courseNames}
          onClose={() => setEditing(null)}
          onDone={() => {
            setEditing(null);
            load();
          }}
        />
      )}

      {resetting && (
        <ResetPasswordModal member={resetting} onClose={() => setResetting(null)} />
      )}

      {emailOpen && <SendEmailModal onClose={() => setEmailOpen(false)} />}
    </CmdsShell>
  );
}

function ResetPasswordModal({ member, onClose }: { member: Member; onClose: () => void }) {
  const [newPassword, setNewPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState<{ loginId: string; password: string } | null>(null);
  // `loginId` here holds the bare member id (org auto-resolved at login).

  async function submit() {
    setError("");
    if (newPassword && newPassword.length < 6) return setError("Password must be at least 6 characters.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/people/password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: member.id, newPassword }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Reset failed");
        return;
      }
      setDone({ loginId: d.memberId || d.loginId, password: d.password });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">
          Reset password — {member.firstName} {member.lastName}
        </h3>
        {done ? (
          <>
            <p className="mt-2 text-sm text-slate-500">
              Password updated. Share the new login below.
            </p>
            <div className="mt-4 space-y-2 rounded border border-slate-200 bg-slate-50 p-3 text-sm">
              <div>
                <span className="text-slate-500">Login ID:</span>{" "}
                <span className="font-mono font-medium text-slate-800">{done.loginId}</span>
              </div>
              <div>
                <span className="text-slate-500">Password:</span>{" "}
                <span className="font-mono font-medium text-slate-800">{done.password}</span>
              </div>
            </div>
            <div className="mt-5 flex justify-end gap-2">
              <button
                onClick={() =>
                  navigator.clipboard?.writeText(`Login ID: ${done.loginId}\nPassword: ${done.password}`)
                }
                className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
              >
                Copy
              </button>
              <button
                onClick={onClose}
                className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
              >
                Done
              </button>
            </div>
          </>
        ) : (
          <>
            <p className="mt-2 text-sm text-slate-500">
              Leave blank to auto-generate a temporary password.
            </p>
            <div className="mt-4">
              <Field
                label="New password (optional)"
                value={newPassword}
                onChange={setNewPassword}
              />
            </div>
            {error && <div className="mt-3 text-sm text-red-600">{error}</div>}
            <div className="mt-5 flex justify-end gap-2">
              <button
                onClick={onClose}
                className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                onClick={submit}
                disabled={saving}
                className="rounded bg-amber-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-amber-700 disabled:opacity-50"
              >
                {saving ? "Resetting…" : "Reset password"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

// Program -> Center -> Section picker, shared by Add and Edit — legacy does
// this as "Step 2/2: Assign Courses and Sections" on the same form, adding
// mappings one at a time to a list rather than a single fixed selection (a
// student can belong to more than one program/section). Mirrors that: pick
// a cascading Program/Center/Section and it's added to the list
// automatically as soon as all three are chosen — each entry removable. The
// parent owns `mappings` and submits the whole list on save.
//
// Bug found live: this used to require an extra "+ Add mapping" click below
// the dropdowns before the selection actually joined `mappings` — easy to
// miss since the modal's own primary "Add"/"Save" button sits right below
// and looks like the action that finishes the job. An admin who picked
// Program/Center/Section and went straight for that button created the
// student with an empty programMemberships (no course access at all),
// while the same picker on the post-creation Assign Courses page has no such
// trap (one Program/Center/Section, one Assign button — no intermediate
// list). Auto-adding as soon as the third dropdown is chosen removes the
// missable step entirely.
function MappingPicker({
  programs,
  centers,
  sections,
  mappings,
  onChange,
  initialProgramId,
}: {
  programs: AcadProgram[];
  centers: AcadCenter[];
  sections: AcadSection[];
  mappings: (Mapping & { programName: string; centerName: string; sectionName: string })[];
  onChange: (next: (Mapping & { programName: string; centerName: string; sectionName: string })[]) => void;
  initialProgramId?: string | null;
}) {
  const [programId, setProgramId] = useState(initialProgramId || "");
  const [centerId, setCenterId] = useState("");
  const [sectionId, setSectionId] = useState("");

  const centersForProgram = useMemo(() => {
    const prog = programs.find((p) => p.id === programId);
    const ids = new Set(prog?.centerIds || []);
    return centers.filter((c) => ids.has(c.id));
  }, [programs, centers, programId]);
  const sectionsForProgramCenter = useMemo(
    () => sections.filter((s) => s.programId === programId && s.centerId === centerId),
    [sections, programId, centerId]
  );

  function addMapping(pProgramId: string, pCenterId: string, pSectionId: string) {
    if (!pProgramId || !pCenterId || !pSectionId) return;
    if (mappings.some((m) => m.programId === pProgramId && m.centerId === pCenterId && m.sectionId === pSectionId))
      return;
    const programName = programs.find((p) => p.id === pProgramId)?.name || pProgramId;
    const centerName = centers.find((c) => c.id === pCenterId)?.name || pCenterId;
    const sectionName = sections.find((s) => s.id === pSectionId)?.name || pSectionId;
    onChange([...mappings, { programId: pProgramId, centerId: pCenterId, sectionId: pSectionId, programName, centerName, sectionName }]);
    setProgramId("");
    setCenterId("");
    setSectionId("");
  }

  // Fires the moment all three levels are picked — no separate click needed.
  useEffect(() => {
    if (programId && centerId && sectionId) addMapping(programId, centerId, sectionId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [programId, centerId, sectionId]);

  function removeMapping(i: number) {
    onChange(mappings.filter((_, idx) => idx !== i));
  }

  return (
    <div>
      <span className="mb-1 block text-sm text-slate-600">Assign to Program / Center / Section</span>
      {mappings.length > 0 && (
        <div className="mb-2 space-y-1">
          {mappings.map((m, i) => (
            <div
              key={`${m.programId}-${m.centerId}-${m.sectionId}`}
              className="flex items-center justify-between rounded border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs text-emerald-800"
            >
              <span>
                {m.programName} → {m.centerName} → {m.sectionName}
              </span>
              <button type="button" onClick={() => removeMapping(i)} className="text-emerald-700 hover:text-red-600">
                ✕
              </button>
            </div>
          ))}
        </div>
      )}
      <div className="grid grid-cols-3 gap-2">
        <select
          value={programId}
          onChange={(e) => {
            setProgramId(e.target.value);
            setCenterId("");
            setSectionId("");
          }}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs outline-none focus:border-slate-500"
        >
          <option value="">Program…</option>
          {programs.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>
        <select
          value={centerId}
          onChange={(e) => {
            setCenterId(e.target.value);
            setSectionId("");
          }}
          disabled={!programId}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs outline-none focus:border-slate-500 disabled:bg-slate-50"
        >
          <option value="">Center…</option>
          {centersForProgram.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <select
          value={sectionId}
          onChange={(e) => setSectionId(e.target.value)}
          disabled={!centerId}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs outline-none focus:border-slate-500 disabled:bg-slate-50"
        >
          <option value="">Section…</option>
          {sectionsForProgramCenter.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
      </div>
      <p className="mt-1.5 text-[11px] text-slate-400">Picking a Section adds it to the list above automatically.</p>
    </div>
  );
}

function resolveMappingNames(
  raw: Mapping[],
  programs: AcadProgram[],
  centers: AcadCenter[],
  sections: AcadSection[]
): (Mapping & { programName: string; centerName: string; sectionName: string })[] {
  return raw.map((m) => ({
    ...m,
    programName: programs.find((p) => p.id === m.programId)?.name || "(unknown program)",
    centerName: centers.find((c) => c.id === m.centerId)?.name || "(unknown center)",
    sectionName: sections.find((s) => s.id === m.sectionId)?.name || "(unknown section)",
  }));
}

function EditMemberModal({
  member,
  programs,
  centers,
  sections,
  courseNames,
  onClose,
  onDone,
}: {
  member: Member;
  programs: AcadProgram[];
  centers: AcadCenter[];
  sections: AcadSection[];
  courseNames: Record<string, string>;
  onClose: () => void;
  onDone: () => void;
}) {
  const [firstName, setFirstName] = useState(member.firstName);
  const [lastName, setLastName] = useState(member.lastName);
  const [email, setEmail] = useState(member.email);
  const [contactNumber, setContactNumber] = useState(member.contactNumber);
  const [memberProfile, setMemberProfile] = useState(member.profile);
  const [mappings, setMappings] = useState(() =>
    resolveMappingNames(member.programMemberships || [], programs, centers, sections)
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  async function submit() {
    setError("");
    if (!firstName.trim()) return setError("First name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/people", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          id: member.id,
          firstName,
          lastName,
          email,
          contactNumber,
          profile: memberProfile,
          programMemberships: mappings.map(({ programId, centerId, sectionId }) => ({
            programId,
            centerId,
            sectionId,
          })),
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Failed to update member");
        return;
      }
      onDone();
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Edit member</h3>
        <div className="mt-4 grid grid-cols-2 gap-3">
          <Field label="First name*" value={firstName} onChange={setFirstName} />
          <Field label="Last name" value={lastName} onChange={setLastName} />
          <Field label="Contact" value={contactNumber} onChange={setContactNumber} />
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Role</span>
            <select
              value={memberProfile}
              onChange={(e) => setMemberProfile(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
            >
              {PROFILES.map((p) => (
                <option key={p} value={p}>
                  {p.replace("_", " ")}
                </option>
              ))}
            </select>
          </label>
          <div className="col-span-2">
            <Field label="Email" value={email} onChange={setEmail} />
          </div>
        </div>
        {/* Legacy's real OrgMember.mappings field (Program/Center/Section) is
            generic to every profile — its own model comment lists exactly
            which fields are STUDENT-only (father/mother/guardian/etc.) and
            mappings isn't one of them; QrPeople.java's mapping-edit actions
            operate on whatever member is being viewed, not gated to
            STUDENT/TEACHER. This used to be restricted to those two
            profiles here, which didn't match legacy — an admin (MANAGER)
            or any other staff profile can be assigned a Program too. */}
        <div className="mt-3">
          <MappingPicker
            programs={programs}
            centers={centers}
            sections={sections}
            mappings={mappings}
            onChange={setMappings}
          />
        </div>
        {memberProfile === "STUDENT" && (member.enrolledCourseIds || []).length > 0 && (
          <div className="mt-3 rounded border border-amber-200 bg-amber-50 p-3">
            <span className="text-sm font-medium text-amber-800">
              Directly enrolled courses ({member.enrolledCourseIds!.length})
            </span>
            <p className="mt-1 text-xs text-amber-700">
              Granted outside of any Program (via the Enroll tool, a coupon, or checkout) — this is why this
              student can see courses even with no Program mapping above. Not editable here.
            </p>
            <div className="mt-2 flex flex-wrap gap-1.5">
              {member.enrolledCourseIds!.map((cid) => (
                <span key={cid} className="rounded-full bg-white px-2 py-0.5 text-xs text-amber-800 ring-1 ring-amber-200">
                  {courseNames[cid] || cid}
                </span>
              ))}
            </div>
            <Link href="/cmds/tools/enroll" className="mt-2 inline-block text-xs text-blue-600 hover:underline">
              Manage in Enroll tool →
            </Link>
          </div>
        )}
        {error && <div className="mt-3 text-sm text-red-600">{error}</div>}
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={saving}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? "Saving…" : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}

function AddMemberModal({
  profile,
  programs,
  centers,
  sections,
  initialProgramId,
  onClose,
  onDone,
}: {
  profile: string;
  orgId?: string;
  userId?: string;
  programs: AcadProgram[];
  centers: AcadCenter[];
  sections: AcadSection[];
  initialProgramId?: string | null;
  onClose: () => void;
  onDone: () => void;
}) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [memberId, setMemberId] = useState("");
  const [email, setEmail] = useState("");
  const [contactNumber, setContactNumber] = useState("");
  const [password, setPassword] = useState("");
  const [mappings, setMappings] = useState<
    (Mapping & { programName: string; centerName: string; sectionName: string })[]
  >([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [created, setCreated] = useState<{ loginId: string; password: string } | null>(null);

  async function submit() {
    setError("");
    if (!firstName.trim()) return setError("First name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/people", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          firstName,
          lastName,
          memberId,
          email,
          contactNumber,
          profile,
          password,
          programMemberships: mappings.map(({ programId, centerId, sectionId }) => ({
            programId,
            centerId,
            sectionId,
          })),
        }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Failed to add member");
        return;
      }
      // Show the login credentials once so the admin can share them. No email
      // verification is needed — the account can log in immediately. We show the
      // bare member id (the org is auto-resolved at login).
      setCreated({ loginId: d.memberId || d.loginId, password: d.password });
    } finally {
      setSaving(false);
    }
  }

  if (created) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
        <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
          <h3 className="text-lg font-semibold text-slate-800">
            {profile.replace("_", " ")} created
          </h3>
          <p className="mt-2 text-sm text-slate-500">
            Share these login details. They can sign in right away — no email verification required.
          </p>
          <div className="mt-4 space-y-2 rounded border border-slate-200 bg-slate-50 p-3 text-sm">
            <div>
              <span className="text-slate-500">Login ID:</span>{" "}
              <span className="font-mono font-medium text-slate-800">{created.loginId}</span>
            </div>
            <div>
              <span className="text-slate-500">Password:</span>{" "}
              <span className="font-mono font-medium text-slate-800">{created.password}</span>
            </div>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            Tip: the student can also log in with just the Institute ID (the org is assumed).
          </p>
          <div className="mt-5 flex justify-end gap-2">
            <button
              onClick={() => {
                navigator.clipboard?.writeText(`Login ID: ${created.loginId}\nPassword: ${created.password}`);
              }}
              className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
            >
              Copy
            </button>
            <button
              onClick={onDone}
              className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
            >
              Done
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Add {profile.replace("_", " ")}</h3>
        <div className="mt-4 grid grid-cols-2 gap-3">
          <Field label="First name*" value={firstName} onChange={setFirstName} />
          <Field label="Last name" value={lastName} onChange={setLastName} />
          <Field label="Institute ID (auto from name if blank)" value={memberId} onChange={setMemberId} />
          <Field label="Contact" value={contactNumber} onChange={setContactNumber} />
          <div className="col-span-2">
            <Field label="Email" value={email} onChange={setEmail} />
          </div>
          <div className="col-span-2">
            <Field
              label="Password (optional — auto-generated if blank)"
              value={password}
              onChange={setPassword}
            />
          </div>
        </div>
        {/* See the Edit modal's matching comment — legacy's real Program/
            Center/Section mapping applies to any profile, not just
            STUDENT/TEACHER. */}
        <div className="mt-3">
          <MappingPicker
            programs={programs}
            centers={centers}
            sections={sections}
            mappings={mappings}
            onChange={setMappings}
            initialProgramId={initialProgramId}
          />
        </div>
        {error && <div className="mt-3 text-sm text-red-600">{error}</div>}
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={saving}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? "Adding…" : "Add"}
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-slate-600">{label}</span>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
      />
    </label>
  );
}

// "Send Emails" — legacy's QrPeople.sendEmailsPopup: recipients are the
// students of a specific Program+Center+Section, MANAGER-only (gated by
// the toolbar button that opens this, not re-checked here client-side).
function SendEmailModal({ onClose }: { onClose: () => void }) {
  const [programs, setPrograms] = useState<{ id: string; name: string }[]>([]);
  const [programId, setProgramId] = useState("");
  const [centers, setCenters] = useState<{ id: string; name: string }[]>([]);
  const [sections, setSections] = useState<{ id: string; name: string; centerId: string | null }[]>([]);
  const [centerId, setCenterId] = useState("");
  const [sectionId, setSectionId] = useState("");
  const [loadingProgram, setLoadingProgram] = useState(false);
  const [subject, setSubject] = useState("");
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState("");

  useEffect(() => {
    fetch("/api/cmds/programs")
      .then((r) => r.json())
      .then((d) => setPrograms(d.programs || []))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!programId) {
      setCenters([]);
      setSections([]);
      setCenterId("");
      setSectionId("");
      return;
    }
    setLoadingProgram(true);
    fetch(`/api/cmds/programs/${programId}`)
      .then((r) => r.json())
      .then((d) => {
        setCenters(d.centers || []);
        setSections(d.sections || []);
      })
      .finally(() => setLoadingProgram(false));
  }, [programId]);

  useEffect(() => {
    setCenterId((prev) => (centers.some((c) => c.id === prev) ? prev : centers[0]?.id || ""));
  }, [centers]);

  const centerSections = useMemo(
    () => sections.filter((s) => !centerId || s.centerId === centerId),
    [sections, centerId]
  );

  useEffect(() => {
    setSectionId((prev) => (centerSections.some((s) => s.id === prev) ? prev : centerSections[0]?.id || ""));
  }, [centerSections]);

  async function send() {
    if (!sectionId || !subject.trim() || !text.trim()) return;
    setSending(true);
    setResult("");
    try {
      const res = await fetch("/api/cmds/tools/people/email", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sectionId, subject: subject.trim(), text: text.trim() }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setResult(d.error || "Send failed");
      } else {
        setResult(
          `Sent — attempted for ${d.notified ?? 0} student(s), ${d.delivered ?? 0} delivered${
            !d.delivered ? " (no email provider configured yet)" : ""
          }.`
        );
      }
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[460px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Send Email</h3>
        <p className="mt-1 text-sm text-slate-500">
          Recipients: students of the selected Program, Section, and Center.
        </p>

        <label className="mt-4 block text-sm font-medium text-slate-600">Program</label>
        <select
          value={programId}
          onChange={(e) => setProgramId(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        >
          <option value="">Select a program…</option>
          {programs.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>

        {programId && (
          <>
            <label className="mt-4 block text-sm font-medium text-slate-600">Center</label>
            <select
              value={centerId}
              onChange={(e) => setCenterId(e.target.value)}
              disabled={loadingProgram || centers.length === 0}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
            >
              {centers.length === 0 && <option value="">No centers</option>}
              {centers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            <label className="mt-4 block text-sm font-medium text-slate-600">Section</label>
            <select
              value={sectionId}
              onChange={(e) => setSectionId(e.target.value)}
              disabled={loadingProgram || centerSections.length === 0}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:bg-slate-50"
            >
              {centerSections.length === 0 && <option value="">No sections</option>}
              {centerSections.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </>
        )}

        <label className="mt-4 block text-sm font-medium text-slate-600">Subject</label>
        <input
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        />

        <label className="mt-4 block text-sm font-medium text-slate-600">Message</label>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          className="mt-1 h-28 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        />

        {result && <p className="mt-3 text-sm text-slate-600">{result}</p>}

        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            {result ? "Close" : "Cancel"}
          </button>
          <button
            onClick={send}
            disabled={sending || !sectionId || !subject.trim() || !text.trim()}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {sending ? "Sending…" : "Send"}
          </button>
        </div>
      </div>
    </div>
  );
}
