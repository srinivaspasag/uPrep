"use client";

import { useState } from "react";
import Link from "next/link";

export default function ForgotPasswordPage() {
  const [username, setUsername] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username }),
      });
      const data = await res.json();
      if (!res.ok || data.errorCode) {
        setError(data.error || data.errorMessage || "Could not send reset email");
        return;
      }
      setSent(true);
    } catch {
      setError("Network error — please try again");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-black/5">
        <div className="mb-6 flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 font-bold text-white">
            U
          </div>
          <span className="text-xl font-semibold text-slate-800">UPrep</span>
        </div>

        {sent ? (
          <>
            <h1 className="text-lg font-semibold tracking-wide text-slate-700">CHECK YOUR EMAIL</h1>
            <p className="mt-4 text-sm text-slate-600">
              If an account matches <span className="font-medium">{username}</span>, we&apos;ve sent a
              password-reset link. Follow the instructions in that email to set a new password.
            </p>
            <Link
              href="/login"
              className="mt-6 inline-block rounded-md bg-blue-600 px-5 py-2.5 font-semibold text-white hover:bg-blue-700"
            >
              Back to Login
            </Link>
          </>
        ) : (
          <>
            <h1 className="text-lg font-semibold tracking-wide text-slate-700">
              RESET YOUR PASSWORD
            </h1>
            <p className="mt-2 text-sm text-slate-500">
              Enter the email or member ID linked to your account and we&apos;ll email you a reset
              link.
            </p>

            <form onSubmit={onSubmit} className="mt-6 space-y-5">
              <div>
                <label htmlFor="username" className="block text-sm font-medium text-slate-600">
                  Email Id / Member Id <span className="text-red-500">*</span>
                </label>
                <input
                  id="username"
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />
              </div>

              {error && (
                <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
              >
                {loading ? "Sending…" : "SEND RESET LINK"}
              </button>

              <div className="text-center text-sm">
                <Link href="/login" className="text-slate-500 hover:text-blue-600">
                  Back to Login
                </Link>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
