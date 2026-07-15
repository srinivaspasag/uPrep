"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { isStaff } from "@/lib/roles";

export default function LoginPage() {
  const router = useRouter();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });
      const data = await res.json();
      if (!res.ok || data.errorCode) {
        setError(data.errorMessage || data.errorCode || "Login failed");
        return;
      }
      // Persist the returned session/member and continue into the app.
      const result = data.result ?? data;
      try {
        sessionStorage.setItem("uprep_session", JSON.stringify(result));
      } catch {}
      // Staff land in the CMDS console (honoring a ?next=/cmds/... redirect);
      // students go to the learn app. Mirrors the legacy app split.
      const staff = isStaff(result?.profile);
      const next = new URLSearchParams(window.location.search).get("next");
      if (staff) {
        router.push(next && next.startsWith("/cmds") ? next : "/cmds");
      } else {
        router.push("/learn/library");
      }
    } catch {
      setError("Network error — please try again");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl ring-1 ring-black/5 p-8">
        <div className="mb-6 flex items-center gap-2">
          <div className="h-9 w-9 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold">
            U
          </div>
          <span className="text-xl font-semibold text-slate-800">UPrep</span>
        </div>

        <h1 className="text-lg font-semibold text-slate-700 tracking-wide">
          ENTER YOUR DETAILS
        </h1>

        <form onSubmit={onSubmit} className="mt-6 space-y-5">
          <div>
            <label
              htmlFor="identifier"
              className="block text-sm font-medium text-slate-600"
            >
              Institute Id / Email Id <span className="text-red-500">*</span>
            </label>
            <input
              id="identifier"
              type="text"
              required
              autoComplete="username"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-600"
            >
              Password <span className="text-red-500">*</span>
            </label>
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              required
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </div>

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2 text-slate-600 select-none">
              <input
                type="checkbox"
                checked={showPassword}
                onChange={(e) => setShowPassword(e.target.checked)}
              />
              Show Password
            </label>
            <a href="/forgot-password" className="text-slate-500 hover:text-blue-600">
              Forgot Password?
            </a>
          </div>

          <p className="text-xs text-slate-400">* Required Fields</p>

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
            {loading ? "Signing in…" : "LOGIN"}
          </button>
        </form>
      </div>
    </div>
  );
}
