"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Device = {
  id: string;
  memberId: string;
  name: string;
  profile: string;
  web: string;
  mobile: string;
};

const PROFILES = ["STUDENT", "TEACHER", "MANAGER", "EDITOR"];

export default function DevicesPage() {
  const [profile, setProfile] = useState("STUDENT");
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const d = await (
        await fetch(
          `/api/cmds/tools/devices?profile=${encodeURIComponent(profile)}&query=${encodeURIComponent(query)}`
        )
      ).json();
      setRows(d.devices || []);
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
          <h1 className="text-2xl font-light text-slate-700">Device Management</h1>
          <select
            value={profile}
            onChange={(e) => setProfile(e.target.value)}
            className="rounded border border-slate-300 px-2 py-1 text-sm text-slate-700 outline-none"
          >
            {PROFILES.map((p) => (
              <option key={p} value={p}>
                {p.charAt(0) + p.slice(1).toLowerCase()}s
              </option>
            ))}
          </select>
        </div>

        <div className="mt-4 flex items-center gap-2">
          <span className="text-xs text-slate-400">Filter By</span>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && load()}
            placeholder="search by Name"
            className="w-64 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
          <button
            onClick={load}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50"
          >
            Search
          </button>
        </div>

        <div className="mt-4 overflow-hidden rounded border border-slate-200">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2 font-medium">Name</th>
                <th className="px-4 py-2 font-medium">Institute ID</th>
                <th className="px-4 py-2 font-medium">Web</th>
                <th className="px-4 py-2 font-medium">Device</th>
                <th className="px-4 py-2 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                    No Devices Found Active
                  </td>
                </tr>
              ) : (
                rows.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-700">{r.name}</td>
                    <td className="px-4 py-3 text-slate-500">{r.memberId}</td>
                    <td className="px-4 py-3">
                      <Dot on={r.web === "LOGGED_IN"} label="Web" />
                    </td>
                    <td className="px-4 py-3">
                      <Dot on={r.mobile === "LOGGED_IN"} label="Mobile" />
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {r.web === "LOGGED_IN" || r.mobile === "LOGGED_IN" ? "Available" : "Un-Available"}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </CmdsShell>
  );
}

function Dot({ on, label }: { on: boolean; label: string }) {
  return (
    <span className={`inline-flex items-center gap-1 text-xs ${on ? "text-emerald-600" : "text-slate-400"}`}>
      <span className={`h-2 w-2 rounded-full ${on ? "bg-emerald-500" : "bg-slate-300"}`} />
      {label}
    </span>
  );
}
