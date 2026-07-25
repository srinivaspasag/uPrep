"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Plan = { name?: string; maxStudents?: number | null; maxCourses?: number | null };
type Org = {
  id: string;
  name: string;
  fullName: string;
  type: string;
  memberCount: number;
  plan?: Plan | null;
};

const ORG_TYPES = ["COLLEGE", "SCHOOL", "COACHING", "UNIVERSITY", "OTHER"];

export default function OrganizationsPage() {
  const [orgs, setOrgs] = useState<Org[]>([]);
  const [loading, setLoading] = useState(true);
  const [forbidden, setForbidden] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [grantOrg, setGrantOrg] = useState<Org | null>(null);

  async function load() {
    setLoading(true);
    try {
      const res = await fetch("/api/cmds/tools/organizations");
      if (res.status === 403) {
        setForbidden(true);
        return;
      }
      const d = await res.json();
      setOrgs(d.orgs || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function editPlan(o: Org) {
    const maxStudents = prompt(
      `Max students for "${o.name}" (blank = unlimited):`,
      o.plan?.maxStudents != null ? String(o.plan.maxStudents) : ""
    );
    if (maxStudents === null) return;
    const maxCourses = prompt(
      `Max courses for "${o.name}" (blank = unlimited):`,
      o.plan?.maxCourses != null ? String(o.plan.maxCourses) : ""
    );
    if (maxCourses === null) return;
    await fetch("/api/cmds/tools/organizations", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        orgId: o.id,
        plan: {
          name: "Custom",
          maxStudents: maxStudents.trim() ? Number(maxStudents) : null,
          maxCourses: maxCourses.trim() ? Number(maxCourses) : null,
        },
      }),
    });
    load();
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-light text-slate-700">Organizations</h1>
            <p className="mt-1 text-sm text-slate-500">
              Super-admin only. Create and manage institutes (organizations).
            </p>
          </div>
          {!forbidden && (
            <button
              onClick={() => setCreateOpen(true)}
              className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
            >
              + Create Organization
            </button>
          )}
        </div>

        {forbidden ? (
          <div className="mt-10 rounded border border-amber-200 bg-amber-50 p-6 text-center text-sm text-amber-700">
            This section is restricted to super admins.
          </div>
        ) : loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : (
          <div className="mt-6 overflow-hidden rounded border border-slate-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="px-4 py-2 font-medium">Name</th>
                  <th className="px-4 py-2 font-medium">Type</th>
                  <th className="px-4 py-2 font-medium">Members</th>
                  <th className="px-4 py-2 font-medium">Org ID</th>
                  <th className="px-4 py-2 font-medium">Courses</th>
                </tr>
              </thead>
              <tbody>
                {orgs.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                      No organizations yet.
                    </td>
                  </tr>
                ) : (
                  orgs.map((o) => (
                    <tr key={o.id} className="border-b border-slate-100 hover:bg-slate-50">
                      <td className="px-4 py-3">
                        <div className="font-medium text-slate-700">{o.name}</div>
                        {o.fullName && o.fullName !== o.name && (
                          <div className="text-xs text-slate-400">{o.fullName}</div>
                        )}
                      </td>
                      <td className="px-4 py-3 text-slate-500">{o.type}</td>
                      <td className="px-4 py-3 text-slate-500">{o.memberCount}</td>
                      <td className="px-4 py-3 font-mono text-xs text-slate-400">{o.id}</td>
                      <td className="px-4 py-3">
          <div className="flex gap-2">
            <button
              onClick={() => setGrantOrg(o)}
              className="rounded border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100"
            >
              Grant courses
            </button>
            <button
              onClick={() => editPlan(o)}
              className="rounded border border-sky-200 bg-sky-50 px-2.5 py-1 text-xs font-medium text-sky-700 hover:bg-sky-100"
            >
              {o.plan?.maxStudents != null || o.plan?.maxCourses != null
                ? `Plan: ${o.plan?.maxStudents ?? "∞"}s / ${o.plan?.maxCourses ?? "∞"}c`
                : "Set plan"}
            </button>
          </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {createOpen && (
        <CreateOrgModal
          onClose={() => setCreateOpen(false)}
          onDone={() => {
            setCreateOpen(false);
            load();
          }}
        />
      )}

      {grantOrg && (
        <GrantCoursesModal org={grantOrg} onClose={() => setGrantOrg(null)} />
      )}
    </CmdsShell>
  );
}

type GrantCourse = { id: string; name: string };

type GrantPack = { id: string; name: string; courseCount: number };

function GrantCoursesModal({ org, onClose }: { org: Org; onClose: () => void }) {
  const [courses, setCourses] = useState<GrantCourse[]>([]);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [packs, setPacks] = useState<GrantPack[]>([]);
  const [selectedPacks, setSelectedPacks] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      fetch(`/api/cmds/tools/org-grants?subscriberOrgId=${encodeURIComponent(org.id)}`).then((r) => r.json()),
      fetch(`/api/cmds/tools/org-pack-grants?subscriberOrgId=${encodeURIComponent(org.id)}`).then((r) => r.json()),
    ])
      .then(([g, p]) => {
        if (g.error) {
          setError(g.error);
          return;
        }
        setCourses(g.courses || []);
        setSelected(new Set<string>(g.grantedCourseIds || []));
        setPacks(p.packs || []);
        setSelectedPacks(new Set<string>(p.grantedPackIds || []));
      })
      .finally(() => setLoading(false));
  }, [org.id]);

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  function togglePack(id: string) {
    setSelectedPacks((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function save() {
    setSaving(true);
    setMsg("");
    setError("");
    try {
      const [gRes, pRes] = await Promise.all([
        fetch("/api/cmds/tools/org-grants", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ subscriberOrgId: org.id, courseIds: Array.from(selected) }),
        }),
        fetch("/api/cmds/tools/org-pack-grants", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ subscriberOrgId: org.id, packIds: Array.from(selectedPacks) }),
        }),
      ]);
      const gd = await gRes.json().catch(() => ({}));
      const pd = await pRes.json().catch(() => ({}));
      if (!gRes.ok || !pRes.ok) {
        setError(gd.error || pd.error || "Save failed");
        return;
      }
      setMsg(
        `Granted ${gd.grantedCourseIds?.length ?? 0} course(s) and ${pd.grantedPackIds?.length ?? 0} pack(s).`
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal>
      <h3 className="text-lg font-semibold text-slate-800">Grant content to {org.name}</h3>
      <p className="mt-1 text-sm text-slate-500">
        Grant whole <span className="font-medium">packs</span> and/or individual courses. The org
        admin then assigns them to students.
      </p>

      {loading ? (
        <div className="py-10 text-center text-slate-400">Loading…</div>
      ) : error ? (
        <div className="mt-4 rounded border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
          {error}
        </div>
      ) : (
        <div className="mt-4 max-h-[340px] space-y-4 overflow-auto rounded border border-slate-100 p-2">
          <div>
            <div className="px-2 pb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
              Course Packs
            </div>
            {packs.length === 0 ? (
              <div className="px-2 py-1 text-sm text-slate-400">
                No packs yet — create them under Course Packs.
              </div>
            ) : (
              packs.map((p) => (
                <label
                  key={p.id}
                  className="flex cursor-pointer items-center gap-2 rounded px-2 py-1.5 text-sm hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={selectedPacks.has(p.id)}
                    onChange={() => togglePack(p.id)}
                    className="accent-emerald-600"
                  />
                  <span className="font-medium text-slate-700">{p.name}</span>
                  <span className="text-xs text-slate-400">
                    ({p.courseCount} course{p.courseCount === 1 ? "" : "s"})
                  </span>
                </label>
              ))
            )}
          </div>

          <div>
            <div className="px-2 pb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
              Individual Courses
            </div>
            {courses.length === 0 ? (
              <div className="px-2 py-1 text-sm text-slate-400">
                No courses to grant. Create top-level folders in Resources first.
              </div>
            ) : (
              courses.map((c) => (
                <label
                  key={c.id}
                  className="flex cursor-pointer items-center gap-2 rounded px-2 py-1.5 text-sm hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={selected.has(c.id)}
                    onChange={() => toggle(c.id)}
                    className="accent-emerald-600"
                  />
                  <span>{c.name}</span>
                </label>
              ))
            )}
          </div>
        </div>
      )}

      <div className="mt-5 flex items-center justify-end gap-2">
        {msg && <span className="mr-auto text-sm text-emerald-600">{msg}</span>}
        <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
          Close
        </button>
        {!error && (
          <button
            onClick={save}
            disabled={saving || loading}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? "Saving…" : "Save grants"}
          </button>
        )}
      </div>
    </Modal>
  );
}

function CreateOrgModal({ onClose, onDone }: { onClose: () => void; onDone: () => void }) {
  const [name, setName] = useState("");
  const [fullName, setFullName] = useState("");
  const [type, setType] = useState("COLLEGE");
  const [adminFirstName, setAdminFirstName] = useState("");
  const [adminMemberId, setAdminMemberId] = useState("admin");
  const [adminPassword, setAdminPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [created, setCreated] = useState<{ id: string; admin: { loginId: string; password: string } | null } | null>(null);

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Organization name is required.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/organizations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, fullName, type, adminFirstName, adminMemberId, adminPassword }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Create failed");
        return;
      }
      setCreated({ id: d.id, admin: d.admin || null });
    } finally {
      setSaving(false);
    }
  }

  if (created) {
    return (
      <Modal>
        <h3 className="text-lg font-semibold text-slate-800">Organization created</h3>
        <div className="mt-3 space-y-2 rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div>
            <span className="text-slate-500">Org ID:</span>{" "}
            <span className="font-mono font-medium text-slate-800">{created.id}</span>
          </div>
          {created.admin && (
            <>
              <div>
                <span className="text-slate-500">Admin login:</span>{" "}
                <span className="font-mono font-medium text-slate-800">{created.admin.loginId}</span>
              </div>
              <div>
                <span className="text-slate-500">Admin password:</span>{" "}
                <span className="font-mono font-medium text-slate-800">{created.admin.password}</span>
              </div>
            </>
          )}
        </div>
        {created.admin && (
          <p className="mt-2 text-xs text-slate-400">
            Share these with the org admin — they can sign in immediately and add students / assign courses.
          </p>
        )}
        <div className="mt-5 flex justify-end">
          <button
            onClick={onDone}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
          >
            Done
          </button>
        </div>
      </Modal>
    );
  }

  return (
    <Modal>
      <h3 className="text-lg font-semibold text-slate-800">Create Organization</h3>
      <div className="mt-4 grid grid-cols-2 gap-3">
        <Field label="Name*" value={name} onChange={setName} />
        <label className="block text-sm">
          <span className="mb-1 block text-slate-600">Type</span>
          <select
            value={type}
            onChange={(e) => setType(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          >
            {ORG_TYPES.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </label>
        <div className="col-span-2">
          <Field label="Full name" value={fullName} onChange={setFullName} />
        </div>
      </div>

      <div className="mt-4 rounded border border-slate-100 bg-slate-50 p-3">
        <div className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          First admin (optional)
        </div>
        <div className="mt-2 grid grid-cols-2 gap-3">
          <Field label="Admin first name" value={adminFirstName} onChange={setAdminFirstName} />
          <Field label="Admin login ID" value={adminMemberId} onChange={setAdminMemberId} />
          <div className="col-span-2">
            <Field
              label="Admin password (optional — auto-generated if blank)"
              value={adminPassword}
              onChange={setAdminPassword}
            />
          </div>
        </div>
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
          {saving ? "Creating…" : "Create"}
        </button>
      </div>
    </Modal>
  );
}

function Modal({ children }: { children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[480px] rounded-lg bg-white p-6 shadow-xl">{children}</div>
    </div>
  );
}

function Field({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
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
