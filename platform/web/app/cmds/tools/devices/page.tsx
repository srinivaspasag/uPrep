"use client";

import { useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Device = {
  id: string;
  memberId: string;
  name: string;
  profile: string;
  web: string;
  mobile: string;
  programCount: number;
  program: string | null;
  center: string | null;
  section: string | null;
};

const PROFILES = ["STUDENT", "TEACHER", "MANAGER", "EDITOR"];

// Mirrors legacy's real second filter on this page (QrDevices/home.html's
// "vChooseUserDeviceStatus" dropdown) — it defaults to "All Members" and
// otherwise filters by web/device availability, not role. Our rebuild had no
// equivalent at all; this was the actual gap the user meant by "filter by
// all members."
const STATUS_FILTERS = [
  { value: "", label: "All Members" },
  { value: "WEB_ON", label: "Available on Web" },
  { value: "WEB_OFF", label: "Un-Available on Web" },
  { value: "MOBILE_ON", label: "Available on Device" },
  { value: "MOBILE_OFF", label: "Un-Available on Device" },
];

export default function DevicesPage() {
  const [profile, setProfile] = useState("STUDENT");
  const [statusFilter, setStatusFilter] = useState("");
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [detailFor, setDetailFor] = useState<Device | null>(null);

  const [programs, setPrograms] = useState<{ id: string; name: string }[]>([]);
  const [programId, setProgramId] = useState("");
  const [centers, setCenters] = useState<{ id: string; name: string }[]>([]);
  const [sections, setSections] = useState<{ id: string; name: string; centerId: string | null }[]>([]);
  const [centerId, setCenterId] = useState("");
  const [sectionId, setSectionId] = useState("");

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
    fetch(`/api/cmds/programs/${programId}`)
      .then((r) => r.json())
      .then((d) => {
        setCenters(d.centers || []);
        setSections(d.sections || []);
        setCenterId("");
        setSectionId("");
      })
      .catch(() => {});
  }, [programId]);

  const centerSections = useMemo(
    () => sections.filter((s) => !centerId || s.centerId === centerId),
    [sections, centerId]
  );

  async function load() {
    setLoading(true);
    try {
      const params = new URLSearchParams({ profile, query });
      if (programId) params.set("programId", programId);
      if (centerId) params.set("centerId", centerId);
      if (sectionId) params.set("sectionId", sectionId);
      const d = await (await fetch(`/api/cmds/tools/devices?${params.toString()}`)).json();
      setRows(d.devices || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [profile, programId, centerId, sectionId]);

  const filteredRows = useMemo(() => {
    if (!statusFilter) return rows;
    return rows.filter((r) => {
      if (statusFilter === "WEB_ON") return r.web === "LOGGED_IN";
      if (statusFilter === "WEB_OFF") return r.web !== "LOGGED_IN";
      if (statusFilter === "MOBILE_ON") return r.mobile === "LOGGED_IN";
      if (statusFilter === "MOBILE_OFF") return r.mobile !== "LOGGED_IN";
      return true;
    });
  }, [rows, statusFilter]);

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1100px] px-8 py-6">
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

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="rounded border border-slate-300 px-2 py-1.5 text-sm text-slate-700 outline-none"
          >
            {STATUS_FILTERS.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
          <select
            value={programId}
            onChange={(e) => setProgramId(e.target.value)}
            className="rounded border border-slate-300 px-2 py-1.5 text-sm text-slate-700 outline-none"
          >
            <option value="">All Programs</option>
            {programs.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          {programId && (
            <select
              value={centerId}
              onChange={(e) => setCenterId(e.target.value)}
              className="rounded border border-slate-300 px-2 py-1.5 text-sm text-slate-700 outline-none"
            >
              <option value="">All Centers</option>
              {centers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          )}
          {programId && centerId && (
            <select
              value={sectionId}
              onChange={(e) => setSectionId(e.target.value)}
              className="rounded border border-slate-300 px-2 py-1.5 text-sm text-slate-700 outline-none"
            >
              <option value="">All Sections</option>
              {centerSections.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          )}
        </div>

        <div className="mt-3 flex items-center gap-2">
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
                <th className="px-4 py-2 font-medium">Program / Center / Section</th>
                <th className="px-4 py-2 font-medium">Web</th>
                <th className="px-4 py-2 font-medium">Device</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 font-medium" />
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                    Loading…
                  </td>
                </tr>
              ) : filteredRows.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-10 text-center text-slate-400">
                    No Devices Found Active
                  </td>
                </tr>
              ) : (
                filteredRows.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-700">{r.name}</td>
                    <td className="px-4 py-3 text-slate-500">{r.memberId}</td>
                    <td className="px-4 py-3 text-slate-500">
                      {r.programCount === 0
                        ? "—"
                        : r.programCount > 1
                        ? `${r.programCount} Programs`
                        : [r.program, r.center, r.section].filter(Boolean).join(" / ") || "—"}
                    </td>
                    <td className="px-4 py-3">
                      <Dot on={r.web === "LOGGED_IN"} label="Web" />
                    </td>
                    <td className="px-4 py-3">
                      <Dot on={r.mobile === "LOGGED_IN"} label="Mobile" />
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {r.web === "LOGGED_IN" || r.mobile === "LOGGED_IN" ? "Available" : "Un-Available"}
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => setDetailFor(r)}
                        className="text-xs text-blue-600 hover:underline"
                      >
                        View Details
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {detailFor && <DeviceDetailModal device={detailFor} onClose={() => setDetailFor(null)} />}
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

type LoginRecord = { device: string; at: number | null; ip: string | null };

// Login history drill-down — legacy shows a richer per-page activity feed
// (page + user action) we have no data source for; this shows what we
// actually track (see lib/login-log.ts): device, time, IP per login.
function DeviceDetailModal({ device, onClose }: { device: Device; onClose: () => void }) {
  const [history, setHistory] = useState<LoginRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    fetch(`/api/cmds/tools/devices?memberId=${device.id}`)
      .then((r) => r.json())
      .then((d) => {
        if (cancelled) return;
        if (d.error) setError(d.error);
        else setHistory(d.history || []);
      })
      .catch(() => {
        if (!cancelled) setError("Could not load login history.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [device.id]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">{device.name}</h3>
        <p className="mt-1 text-sm text-slate-500">Login History</p>

        <div className="mt-4 max-h-72 overflow-y-auto rounded border border-slate-200">
          {loading ? (
            <div className="px-4 py-8 text-center text-sm text-slate-400">Loading…</div>
          ) : error ? (
            <div className="px-4 py-8 text-center text-sm text-red-500">{error}</div>
          ) : history.length === 0 ? (
            <div className="px-4 py-8 text-center text-sm text-slate-400">No login history recorded</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="px-3 py-2 font-medium">Device</th>
                  <th className="px-3 py-2 font-medium">Time</th>
                  <th className="px-3 py-2 font-medium">IP</th>
                </tr>
              </thead>
              <tbody>
                {history.map((h, i) => (
                  <tr key={i} className="border-b border-slate-100">
                    <td className="px-3 py-2 text-slate-600">{h.device}</td>
                    <td className="px-3 py-2 text-slate-500">
                      {h.at ? new Date(h.at).toLocaleString() : "—"}
                    </td>
                    <td className="px-3 py-2 text-slate-400">{h.ip || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="mt-5 flex justify-end">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
