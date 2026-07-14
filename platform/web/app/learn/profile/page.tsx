"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";
import { getSession, setSession as persistSession, type UprepSession } from "@/lib/session";

type Profile = {
  id: string;
  memberId: string;
  firstName: string;
  lastName: string;
  email: string;
  contactNumber: string;
  profile: string;
  thumbnail: string | null;
};

export default function ProfilePage() {
  const [session, setSess] = useState<UprepSession | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const s = getSession();
    setSess(s);
    if (!s) return;
    fetch(`/api/learn/profile?userId=${encodeURIComponent(s.id)}`)
      .then((r) => r.json())
      .then((d) => setProfile(d.profile))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="library">
      <h1 className="text-lg font-semibold text-slate-800">Profile & Settings</h1>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : !profile ? (
        <div className="mt-6 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
          We couldn&apos;t find your profile record. Your session may be limited — try logging in again.
        </div>
      ) : (
        <div className="mt-6 max-w-2xl space-y-8">
          <ProfileForm
            profile={profile}
            session={session}
            onSaved={(fn, ln) => {
              setProfile((p) => (p ? { ...p, firstName: fn, lastName: ln } : p));
              const s = getSession();
              if (s) {
                const next = { ...s, firstName: fn, lastName: ln };
                persistSession(next);
                setSess(next);
              }
            }}
          />
          <PasswordForm userId={session?.id || ""} email={profile.email} />
        </div>
      )}
    </LmsShell>
  );
}

function Field({
  label,
  value,
  onChange,
  type = "text",
  placeholder,
  readOnly,
}: {
  label: string;
  value: string;
  onChange?: (v: string) => void;
  type?: string;
  placeholder?: string;
  readOnly?: boolean;
}) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-600">{label}</label>
      <input
        type={type}
        value={value}
        placeholder={placeholder}
        readOnly={readOnly}
        onChange={(e) => onChange?.(e.target.value)}
        className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
          readOnly
            ? "border-slate-200 bg-slate-50 text-slate-500"
            : "border-slate-300 focus:border-emerald-400"
        }`}
      />
    </div>
  );
}

function ProfileForm({
  profile,
  session,
  onSaved,
}: {
  profile: Profile;
  session: UprepSession | null;
  onSaved: (firstName: string, lastName: string) => void;
}) {
  const [firstName, setFirstName] = useState(profile.firstName);
  const [lastName, setLastName] = useState(profile.lastName);
  const [email, setEmail] = useState(profile.email);
  const [contactNumber, setContactNumber] = useState(profile.contactNumber);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);

  async function save() {
    setSaving(true);
    setMsg(null);
    const res = await fetch("/api/learn/profile", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: session?.id, firstName, lastName, email, contactNumber }),
    });
    setSaving(false);
    const d = await res.json().catch(() => ({}));
    if (!res.ok) {
      setMsg({ ok: false, text: d.error || "Could not save changes." });
      return;
    }
    setMsg({ ok: true, text: "Profile updated." });
    onSaved(firstName.trim(), lastName.trim());
  }

  return (
    <section className="rounded-lg border border-slate-200 p-5">
      <div className="flex items-center gap-4">
        <span className="flex h-14 w-14 items-center justify-center rounded-full bg-slate-200 text-xl font-semibold text-slate-600">
          {(firstName || "U").charAt(0).toUpperCase()}
        </span>
        <div>
          <div className="font-semibold text-slate-800">
            {firstName} {lastName}
          </div>
          <div className="text-sm text-slate-500">
            {profile.profile} · ID {profile.memberId || "—"}
          </div>
        </div>
      </div>

      <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="First name" value={firstName} onChange={setFirstName} />
        <Field label="Last name" value={lastName} onChange={setLastName} />
        <Field label="Email" value={email} onChange={setEmail} type="email" />
        <Field label="Contact number" value={contactNumber} onChange={setContactNumber} />
      </div>

      {msg && (
        <div className={`mt-4 text-sm ${msg.ok ? "text-emerald-600" : "text-red-500"}`}>{msg.text}</div>
      )}

      <div className="mt-5">
        <button
          onClick={save}
          disabled={saving}
          className="rounded-md bg-[#e8443b] px-5 py-2 text-sm font-semibold text-white hover:bg-[#d33c34] disabled:opacity-60"
        >
          {saving ? "Saving…" : "Save changes"}
        </button>
      </div>
    </section>
  );
}

function PasswordForm({ userId, email }: { userId: string; email: string }) {
  const [oldPassword, setOld] = useState("");
  const [newPassword, setNew] = useState("");
  const [confirm, setConfirm] = useState("");
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);

  async function change() {
    setMsg(null);
    if (newPassword !== confirm) {
      setMsg({ ok: false, text: "New passwords do not match." });
      return;
    }
    setSaving(true);
    const res = await fetch("/api/learn/password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, email, oldPassword, newPassword }),
    });
    setSaving(false);
    const d = await res.json().catch(() => ({}));
    if (!res.ok) {
      setMsg({ ok: false, text: d.error || "Could not change password." });
      return;
    }
    setMsg({ ok: true, text: "Password changed." });
    setOld("");
    setNew("");
    setConfirm("");
  }

  return (
    <section className="rounded-lg border border-slate-200 p-5">
      <h2 className="font-semibold text-slate-800">Change password</h2>
      <p className="mt-1 text-sm text-slate-500">
        For your account {email ? <span className="font-medium">({email})</span> : null}.
      </p>
      <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Field label="Current password" value={oldPassword} onChange={setOld} type="password" />
        <Field label="New password" value={newPassword} onChange={setNew} type="password" />
        <Field label="Confirm new" value={confirm} onChange={setConfirm} type="password" />
      </div>
      {msg && (
        <div className={`mt-4 text-sm ${msg.ok ? "text-emerald-600" : "text-red-500"}`}>{msg.text}</div>
      )}
      <div className="mt-5">
        <button
          onClick={change}
          disabled={saving || !oldPassword || !newPassword}
          className="rounded-md border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-700 hover:border-slate-400 disabled:opacity-60"
        >
          {saving ? "Updating…" : "Update password"}
        </button>
      </div>
    </section>
  );
}
