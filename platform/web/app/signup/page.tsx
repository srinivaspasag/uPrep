"use client";

import { useState } from "react";
import Link from "next/link";

export default function SignupPage() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [contactNumber, setContactNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState<{ requireApproval: boolean; welcomeMessage: string; loginId: string } | null>(
    null
  );

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!firstName.trim()) return setError("Enter your name.");
    if (!email.trim()) return setError("Enter your email.");
    if (password.length < 6) return setError("Password must be at least 6 characters.");
    if (password !== confirm) return setError("Passwords do not match.");
    setSaving(true);
    try {
      const res = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ firstName, lastName, email, contactNumber, password }),
      });
      const d = await res.json();
      if (!res.ok || d.error) {
        setError(d.error || "Signup failed.");
        return;
      }
      setDone({ requireApproval: d.requireApproval, welcomeMessage: d.welcomeMessage, loginId: d.loginId || email });
    } catch {
      setError("Signup service is unavailable.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-black/5">
        <Link href="/" className="mb-6 flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 font-bold text-white">
            U
          </div>
          <span className="text-xl font-semibold text-slate-800">UPrep</span>
        </Link>

        {done ? (
          <div className="text-center">
            <div className="text-4xl">🎉</div>
            <h1 className="mt-3 text-lg font-semibold text-slate-800">Registration submitted</h1>
            <p className="mt-2 text-sm text-slate-500">
              {done.welcomeMessage || "Thanks for signing up!"}
            </p>
            {done.requireApproval ? (
              <p className="mt-2 text-sm text-slate-500">
                Your account is pending approval by your institute. Once an admin approves it, you
                can log in with <span className="font-medium text-slate-700">{done.loginId}</span>{" "}
                and the password you chose.
              </p>
            ) : (
              <p className="mt-2 text-sm text-slate-500">
                Your account is active — log in now with{" "}
                <span className="font-medium text-slate-700">{done.loginId}</span> and your password.
              </p>
            )}
            <Link
              href="/login"
              className="mt-5 inline-block rounded-md bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700"
            >
              Go to Login
            </Link>
          </div>
        ) : (
          <>
            <h1 className="text-lg font-semibold tracking-wide text-slate-700">CREATE YOUR ACCOUNT</h1>
            <form className="mt-6 space-y-4" onSubmit={submit}>
              <div className="grid grid-cols-2 gap-3">
                <Field label="First name" value={firstName} onChange={setFirstName} />
                <Field label="Last name" value={lastName} onChange={setLastName} />
              </div>
              <Field label="Email" type="email" value={email} onChange={setEmail} />
              <Field label="Contact number" value={contactNumber} onChange={setContactNumber} />
              <div className="grid grid-cols-2 gap-3">
                <Field label="Password" type="password" value={password} onChange={setPassword} />
                <Field label="Confirm password" type="password" value={confirm} onChange={setConfirm} />
              </div>

              {error && (
                <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={saving}
                className="w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
              >
                {saving ? "Submitting…" : "SIGN UP"}
              </button>
            </form>

            <p className="mt-4 text-center text-sm text-slate-500">
              Already a member?{" "}
              <Link href="/login" className="text-blue-600 hover:underline">
                Login
              </Link>
            </p>
          </>
        )}
      </div>
    </div>
  );
}

function Field({
  label,
  type = "text",
  value,
  onChange,
}: {
  label: string;
  type?: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-600">{label}</label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
      />
    </div>
  );
}
