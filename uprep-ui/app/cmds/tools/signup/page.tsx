"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

export default function SignupConfigPage() {
  const [enabled, setEnabled] = useState(false);
  const [requireApproval, setRequireApproval] = useState(true);
  const [defaultProfile, setDefaultProfile] = useState("STUDENT");
  const [welcomeMessage, setWelcomeMessage] = useState("");
  const [collectPhone, setCollectPhone] = useState(true);
  const [allowedDomains, setAllowedDomains] = useState("");
  const [loading, setLoading] = useState(true);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    fetch("/api/cmds/tools/signup")
      .then((r) => r.json())
      .then((d) => {
        const c = d.config || {};
        setEnabled(!!c.enabled);
        setRequireApproval(!!c.requireApproval);
        setDefaultProfile(c.defaultProfile || "STUDENT");
        setWelcomeMessage(c.welcomeMessage || "");
        setCollectPhone(!!c.collectPhone);
        setAllowedDomains(c.allowedDomains || "");
      })
      .finally(() => setLoading(false));
  }, []);

  async function save() {
    await fetch("/api/cmds/tools/signup", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        enabled,
        requireApproval,
        defaultProfile,
        welcomeMessage,
        collectPhone,
        allowedDomains,
      }),
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[720px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">External Signup</h1>
        <p className="mt-1 text-sm text-slate-500">Control the public self-registration form for your institute.</p>

        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : (
          <div className="mt-5 space-y-4 rounded border border-slate-200 p-5">
            <label className="flex items-center gap-3 text-sm text-slate-700">
              <input type="checkbox" className="h-4 w-4 accent-emerald-600" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />
              Enable public signup
            </label>
            <label className="flex items-center gap-3 text-sm text-slate-700">
              <input type="checkbox" className="h-4 w-4 accent-emerald-600" checked={requireApproval} onChange={(e) => setRequireApproval(e.target.checked)} />
              Require admin approval before activation
            </label>
            <label className="flex items-center gap-3 text-sm text-slate-700">
              <input type="checkbox" className="h-4 w-4 accent-emerald-600" checked={collectPhone} onChange={(e) => setCollectPhone(e.target.checked)} />
              Collect phone number
            </label>

            <div>
              <label className="block text-sm font-medium text-slate-600">Default role for new signups</label>
              <select
                value={defaultProfile}
                onChange={(e) => setDefaultProfile(e.target.value)}
                className="mt-1 w-56 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
              >
                <option value="STUDENT">Student</option>
                <option value="OFFLINE_USER">Offline user</option>
                <option value="TEACHER">Teacher</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-600">Allowed email domains (comma-separated, optional)</label>
              <input
                value={allowedDomains}
                onChange={(e) => setAllowedDomains(e.target.value)}
                placeholder="e.g. school.edu, gmail.com"
                className="mt-1 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-600">Welcome message</label>
              <textarea
                value={welcomeMessage}
                onChange={(e) => setWelcomeMessage(e.target.value)}
                rows={3}
                className="mt-1 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
              />
            </div>

            <div className="flex items-center gap-3">
              <button onClick={save} className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700">
                Save configuration
              </button>
              {saved && <span className="text-sm text-emerald-600">Saved ✓</span>}
            </div>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}
