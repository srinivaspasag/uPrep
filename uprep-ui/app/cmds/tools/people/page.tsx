"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";

type Member = {
  id: string;
  memberId: string;
  firstName: string;
  lastName: string;
  email: string;
  profile: string;
  contactNumber: string;
  status: string;
};

const PROFILES = ["STUDENT", "OFFLINE_USER", "TEACHER", "MANAGER", "EDITOR", "SALESPERSON"];

export default function PeoplePage() {
  const [session, setSession] = useState<UprepSession | null>(null);
  const [profile, setProfile] = useState("STUDENT");
  const [query, setQuery] = useState("");
  const [members, setMembers] = useState<Member[]>([]);
  const [counts, setCounts] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [editing, setEditing] = useState<Member | null>(null);

  async function deactivate(m: Member) {
    if (!confirm(`Deactivate ${m.firstName} ${m.lastName}?`)) return;
    await fetch(`/api/cmds/tools/people?id=${m.id}`, { method: "DELETE" });
    load();
  }

  useEffect(() => {
    setSession(getSession());
  }, []);

  async function load() {
    setLoading(true);
    try {
      const res = await fetch(
        `/api/cmds/tools/people?profile=${encodeURIComponent(profile)}&query=${encodeURIComponent(query)}`
      );
      const d = await res.json();
      setMembers(d.members || []);
      setCounts(d.counts || {});
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [profile]);

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-light text-slate-700">People Management</h1>
          <button
            onClick={() => setAddOpen(true)}
            className="rounded bg-[#e8443b] px-3 py-1.5 text-sm font-medium text-white hover:bg-[#d13a32]"
          >
            + Add {profile === "STUDENT" ? "Student" : "Member"}
          </button>
        </div>

        {/* Profile selector */}
        <div className="mt-5 flex flex-wrap gap-2">
          {PROFILES.map((p) => (
            <button
              key={p}
              onClick={() => setProfile(p)}
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
                          onClick={() => deactivate(m)}
                          className="text-red-500 hover:underline"
                        >
                          Deactivate
                        </button>
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
          onClose={() => setAddOpen(false)}
          onDone={() => {
            setAddOpen(false);
            load();
          }}
        />
      )}

      {editing && (
        <EditMemberModal
          member={editing}
          onClose={() => setEditing(null)}
          onDone={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </CmdsShell>
  );
}

function EditMemberModal({
  member,
  onClose,
  onDone,
}: {
  member: Member;
  onClose: () => void;
  onDone: () => void;
}) {
  const [firstName, setFirstName] = useState(member.firstName);
  const [lastName, setLastName] = useState(member.lastName);
  const [email, setEmail] = useState(member.email);
  const [contactNumber, setContactNumber] = useState(member.contactNumber);
  const [memberProfile, setMemberProfile] = useState(member.profile);
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
  onClose,
  onDone,
}: {
  profile: string;
  orgId?: string;
  userId?: string;
  onClose: () => void;
  onDone: () => void;
}) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [memberId, setMemberId] = useState("");
  const [email, setEmail] = useState("");
  const [contactNumber, setContactNumber] = useState("");
  const [password, setPassword] = useState("");
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
        body: JSON.stringify({ firstName, lastName, memberId, email, contactNumber, profile, password }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Failed to add member");
        return;
      }
      // Show the login credentials once so the admin can share them. No email
      // verification is needed — the account can log in immediately.
      setCreated({ loginId: d.loginId, password: d.password });
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
          <Field label="Institute ID" value={memberId} onChange={setMemberId} />
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
