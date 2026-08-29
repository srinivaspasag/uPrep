"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";
import { getSession } from "@/lib/session";

type Group = { id: string; name: string; itemCount: number; createdAt: number };
type AccessCode = {
  id: string;
  code: string;
  groupId: string;
  groupName: string;
  buyerEmail: string;
  sellerInfo: { sellerReferenceNo?: string; pointOfSale?: string };
  userId: string | null;
  deviceIds: string[];
  shipmentStatus: "NOT_DISPATCHED" | "DISPATCHED" | "RECEIVED";
  invoiceId: string | null;
  verified: boolean;
  verifiedAt: number | null;
  timeCreated: number;
};

type Tab = "inventory" | "shipments" | "orders";

// Seller Dashboard — offline content distribution via device+email-locked
// access codes. Legacy parity: MANAGER-only (nav-gated in legacy, and
// explicitly excludes SALESPERSON despite Salesperson otherwise being a
// staff profile). See the plan doc for the legacy source trace.
export default function SellerDashboardPage() {
  const isAdmin = (getSession()?.profile || "").trim().toUpperCase() === "MANAGER";
  const [tab, setTab] = useState<Tab>("inventory");
  const [groups, setGroups] = useState<Group[]>([]);
  const [codes, setCodes] = useState<AccessCode[]>([]);
  const [loading, setLoading] = useState(true);
  const [newGroupOpen, setNewGroupOpen] = useState(false);
  const [generateFor, setGenerateFor] = useState<Group | null>(null);

  async function loadGroups() {
    const d = await (await fetch("/api/cmds/tools/seller/groups")).json();
    setGroups(d.groups || []);
  }
  async function loadCodes(shipmentStatus?: string) {
    const params = shipmentStatus ? `?shipmentStatus=${shipmentStatus}` : "";
    const d = await (await fetch(`/api/cmds/tools/seller/access-codes${params}`)).json();
    setCodes(d.codes || []);
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([loadGroups(), loadCodes()]).finally(() => setLoading(false));
  }, []);

  if (!isAdmin) {
    return (
      <CmdsShell>
        <div className="mx-auto max-w-[1000px] px-8 py-16 text-center text-slate-400">
          You don&apos;t have access to the Seller Dashboard.
        </div>
      </CmdsShell>
    );
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1100px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Seller Dashboard</h1>
        <p className="mt-1 text-sm text-slate-500">
          Distribute content offline: bundle it into a group, then generate access codes locked
          to one device and one email so a card can&apos;t be reused elsewhere.
        </p>

        <div className="mt-5 flex gap-6 border-b border-slate-200 text-sm">
          {(["inventory", "shipments", "orders"] as Tab[]).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`-mb-px border-b-2 pb-2 capitalize ${
                tab === t
                  ? "border-emerald-500 font-medium text-slate-800"
                  : "border-transparent text-slate-400 hover:text-slate-600"
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : tab === "inventory" ? (
          <InventoryTab
            groups={groups}
            onNewGroup={() => setNewGroupOpen(true)}
            onGenerate={(g) => setGenerateFor(g)}
          />
        ) : tab === "shipments" ? (
          <ShipmentsTab codes={codes} onReload={loadCodes} />
        ) : (
          <OrdersTab codes={codes} />
        )}
      </div>

      {newGroupOpen && (
        <NewGroupModal
          onClose={() => setNewGroupOpen(false)}
          onDone={() => {
            setNewGroupOpen(false);
            loadGroups();
          }}
        />
      )}

      {generateFor && (
        <GenerateCodesModal
          group={generateFor}
          onClose={() => setGenerateFor(null)}
          onDone={() => {
            setGenerateFor(null);
            loadCodes();
          }}
        />
      )}
    </CmdsShell>
  );
}

function InventoryTab({
  groups,
  onNewGroup,
  onGenerate,
}: {
  groups: Group[];
  onNewGroup: () => void;
  onGenerate: (g: Group) => void;
}) {
  return (
    <div className="mt-4">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-600">Distribution Groups</h2>
        <button
          onClick={onNewGroup}
          className="rounded bg-[#e8443b] px-3 py-1.5 text-xs font-medium text-white hover:bg-[#d13a32]"
        >
          + New Group
        </button>
      </div>
      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Name</th>
              <th className="px-4 py-2 font-medium">Items</th>
              <th className="px-4 py-2 font-medium">Created</th>
              <th className="w-64 px-4 py-2 font-medium" />
            </tr>
          </thead>
          <tbody>
            {groups.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-10 text-center text-slate-400">
                  No distribution groups yet
                </td>
              </tr>
            ) : (
              groups.map((g) => (
                <tr key={g.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-700">{g.name}</td>
                  <td className="px-4 py-3 text-slate-500">{g.itemCount}</td>
                  <td className="px-4 py-3 text-slate-500">
                    {g.createdAt ? new Date(g.createdAt).toLocaleDateString() : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <a
                        href={`/api/cmds/tools/seller/groups/${g.id}/package?mode=plain`}
                        title="Ready to copy straight onto an SD card — opens on any tablet, no app needed"
                        className="rounded border border-emerald-300 bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100"
                      >
                        Download .zip
                      </a>
                      <a
                        href={`/api/cmds/tools/seller/groups/${g.id}/package?mode=encrypted`}
                        title="Encrypted — no app exists yet that can open these files on a device"
                        className="rounded border border-slate-200 px-2.5 py-1 text-xs font-medium text-slate-400 hover:bg-slate-100"
                      >
                        Download .zip (encrypted)
                      </a>
                      <button
                        onClick={() => onGenerate(g)}
                        className="rounded border border-slate-200 px-2.5 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                      >
                        Generate Access Code(s)
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      <p className="mt-2 text-xs text-slate-400">
        <strong className="text-slate-500">Download .zip</strong> (green) gives real, ready-to-open
        files plus an index.html catalog — unzip it on a computer, copy everything straight onto the
        SD card&apos;s root folder, insert the card into the tablet, and open the files with its
        Files app (or open index.html in its browser). No app install needed, works fully offline.
        <strong className="ml-1 text-slate-500">Download .zip (encrypted)</strong> produces
        AES-256-GCM protected files for a future companion app that can call{" "}
        <code className="rounded bg-slate-100 px-1">/api/seller/verify</code> to unlock them — no
        such app exists yet, so those files won&apos;t open on any device today. Question banks and
        modules aren&apos;t files either way (see the zip&apos;s manifest.json for what needs an
        online sync instead).
      </p>
    </div>
  );
}

function ShipmentsTab({
  codes,
  onReload,
}: {
  codes: AccessCode[];
  onReload: (shipmentStatus?: string) => void;
}) {
  const [filter, setFilter] = useState("");
  const [busy, setBusy] = useState<string | null>(null);

  useEffect(() => {
    onReload(filter || undefined);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter]);

  async function setStatus(c: AccessCode, status: string) {
    setBusy(c.id);
    await fetch("/api/cmds/tools/seller/access-codes", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: c.id, action: "updateShipmentStatus", status }),
    });
    setBusy(null);
    onReload(filter || undefined);
  }

  async function resend(c: AccessCode) {
    setBusy(c.id);
    await fetch("/api/cmds/tools/seller/access-codes", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: c.id, action: "resendEmail" }),
    });
    setBusy(null);
  }

  return (
    <div className="mt-4">
      <div className="flex items-center gap-2">
        <span className="text-xs text-slate-400">Filter</span>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="rounded border border-slate-300 px-2 py-1.5 text-xs"
        >
          <option value="">All Statuses</option>
          <option value="NOT_DISPATCHED">Not Dispatched</option>
          <option value="DISPATCHED">Dispatched</option>
          <option value="RECEIVED">Received</option>
        </select>
      </div>
      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Code</th>
              <th className="px-4 py-2 font-medium">Group</th>
              <th className="px-4 py-2 font-medium">Buyer Email</th>
              <th className="px-4 py-2 font-medium">Locked To</th>
              <th className="px-4 py-2 font-medium">Status</th>
              <th className="w-52 px-4 py-2 font-medium" />
            </tr>
          </thead>
          <tbody>
            {codes.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-slate-400">
                  No access codes
                </td>
              </tr>
            ) : (
              codes.map((c) => (
                <tr key={c.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 font-mono text-slate-700">{c.code}</td>
                  <td className="px-4 py-3 text-slate-500">{c.groupName}</td>
                  <td className="px-4 py-3 text-slate-500">{c.buyerEmail}</td>
                  <td className="px-4 py-3 text-slate-500">
                    {c.deviceIds.length > 0 ? (
                      <span className="text-emerald-600">● 1 device locked</span>
                    ) : (
                      <span className="text-slate-400">Not yet verified</span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <select
                      value={c.shipmentStatus}
                      onChange={(e) => setStatus(c, e.target.value)}
                      disabled={busy === c.id}
                      className="rounded border border-slate-300 px-2 py-1 text-xs disabled:opacity-50"
                    >
                      <option value="NOT_DISPATCHED">Not Dispatched</option>
                      <option value="DISPATCHED">Dispatched</option>
                      <option value="RECEIVED">Received</option>
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => resend(c)}
                      disabled={busy === c.id}
                      className="rounded border border-slate-200 px-2.5 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100 disabled:opacity-50"
                    >
                      Resend Email
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function OrdersTab({ codes }: { codes: AccessCode[] }) {
  return (
    <div className="mt-4">
      <p className="text-xs text-slate-400">
        Access codes with a linked paid order. Codes generated without billing show no order.
      </p>
      <div className="mt-3 overflow-hidden rounded border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-2 font-medium">Code</th>
              <th className="px-4 py-2 font-medium">Buyer Email</th>
              <th className="px-4 py-2 font-medium">Seller Reference</th>
              <th className="px-4 py-2 font-medium">Point of Sale</th>
              <th className="px-4 py-2 font-medium">Order</th>
            </tr>
          </thead>
          <tbody>
            {codes.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-slate-400">
                  No access codes
                </td>
              </tr>
            ) : (
              codes.map((c) => (
                <tr key={c.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 font-mono text-slate-700">{c.code}</td>
                  <td className="px-4 py-3 text-slate-500">{c.buyerEmail}</td>
                  <td className="px-4 py-3 text-slate-500">{c.sellerInfo?.sellerReferenceNo || "—"}</td>
                  <td className="px-4 py-3 text-slate-500">{c.sellerInfo?.pointOfSale || "—"}</td>
                  <td className="px-4 py-3 text-slate-400">
                    {c.invoiceId ? (
                      <a href={`/cmds/tools/commerce?invoiceId=${c.invoiceId}`} className="text-blue-600 hover:underline">
                        View Invoice
                      </a>
                    ) : (
                      "No linked order"
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// Content picker for a new distribution group — same browse pattern as
// AddContentModal/AddToSectionModal built earlier this session.
type PickMode = "browse" | "program";
type ProgramOption = { id: string; name: string; sectionCount: number };

function NewGroupModal({ onClose, onDone }: { onClose: () => void; onDone: () => void }) {
  const [name, setName] = useState("");
  const [mode, setMode] = useState<PickMode>("browse");

  // Browse-content mode state
  const [parentId, setParentId] = useState<string | null>(null);
  const [breadcrumb, setBreadcrumb] = useState<{ id: string | null; name: string }[]>([
    { id: null, name: "Resources" },
  ]);
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [picked, setPicked] = useState<Set<string>>(new Set());

  // Pack-a-Program mode state
  const [programs, setPrograms] = useState<ProgramOption[]>([]);
  const [programsLoading, setProgramsLoading] = useState(true);
  const [programId, setProgramId] = useState<string | null>(null);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (mode !== "browse") return;
    setLoading(true);
    const url = parentId ? `/api/cmds/content?parentId=${parentId}` : "/api/cmds/content";
    fetch(url)
      .then((r) => r.json())
      .then((d) => setRows(d.resources || []))
      .finally(() => setLoading(false));
  }, [mode, parentId]);

  useEffect(() => {
    if (mode !== "program" || programs.length > 0) return;
    setProgramsLoading(true);
    fetch("/api/cmds/programs")
      .then((r) => r.json())
      .then((d) => setPrograms(d.programs || []))
      .finally(() => setProgramsLoading(false));
  }, [mode, programs.length]);

  function enterFolder(f: any) {
    setBreadcrumb((prev) => [...prev, { id: f.id, name: f.title }]);
    setParentId(f.id);
  }
  function goTo(index: number) {
    setBreadcrumb((prev) => prev.slice(0, index + 1));
    setParentId(breadcrumb[index].id);
  }
  function togglePick(r: any) {
    setPicked((prev) => {
      const next = new Set(prev);
      if (next.has(r.id)) next.delete(r.id);
      else next.add(r.id);
      return next;
    });
  }

  const canCreate = name.trim() && (mode === "browse" ? picked.size > 0 : !!programId);

  async function create() {
    if (!canCreate) return;
    setSaving(true);
    setError("");
    const body =
      mode === "browse"
        ? { name: name.trim(), contentIds: Array.from(picked) }
        : { name: name.trim(), programId };
    const res = await fetch("/api/cmds/tools/seller/groups", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const d = await res.json().catch(() => ({}));
    setSaving(false);
    if (!res.ok) {
      setError(d.error || "Create failed");
      return;
    }
    onDone();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="flex max-h-[85vh] w-full max-w-2xl flex-col rounded-lg bg-white shadow-xl">
        <div className="border-b border-slate-200 px-5 py-3">
          <h3 className="text-sm font-semibold text-slate-700">New Distribution Group</h3>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Group name (e.g. JEE 11th - Offline Pack)"
            className="mt-2 w-full rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
          <div className="mt-3 flex gap-4 text-xs">
            <button
              onClick={() => setMode("browse")}
              className={`border-b-2 pb-1 ${
                mode === "browse" ? "border-emerald-500 font-medium text-slate-800" : "border-transparent text-slate-400"
              }`}
            >
              Browse content
            </button>
            <button
              onClick={() => setMode("program")}
              className={`border-b-2 pb-1 ${
                mode === "program" ? "border-emerald-500 font-medium text-slate-800" : "border-transparent text-slate-400"
              }`}
            >
              Pack a whole Program
            </button>
          </div>
        </div>

        {mode === "browse" ? (
          <>
            <div className="flex flex-wrap items-center gap-1 border-b border-slate-100 px-5 py-2 text-xs text-slate-500">
              {breadcrumb.map((b, i) => (
                <span key={i}>
                  {i > 0 && <span className="mx-1 text-slate-300">/</span>}
                  <button onClick={() => goTo(i)} className="hover:text-blue-600 hover:underline">
                    {b.name}
                  </button>
                </span>
              ))}
            </div>
            <div className="flex-1 overflow-y-auto px-5 py-3">
              {loading ? (
                <div className="py-10 text-center text-sm text-slate-400">Loading…</div>
              ) : rows.length === 0 ? (
                <div className="py-10 text-center text-sm text-slate-400">Empty folder</div>
              ) : (
                <ul className="divide-y divide-slate-100">
                  {rows.map((r) => (
                    <li key={r.id} className="flex items-center gap-3 py-2">
                      {r.type === "FOLDER" ? (
                        <button
                          onClick={() => enterFolder(r)}
                          className="flex-1 text-left text-sm text-slate-700 hover:text-blue-600"
                        >
                          📁 {r.title}
                        </button>
                      ) : (
                        <>
                          <input type="checkbox" checked={picked.has(r.id)} onChange={() => togglePick(r)} />
                          <span className="flex-1 text-sm text-slate-700">{r.title}</span>
                          <span className="text-xs text-slate-400">{r.type}</span>
                        </>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </>
        ) : (
          <div className="flex-1 overflow-y-auto px-5 py-3">
            <p className="text-xs text-slate-500">
              Every item added to any of the program&apos;s sections (plus anything made visible to the
              whole program directly) is bundled in — a snapshot as of right now. Content added to the
              program later won&apos;t retroactively appear on cards already created.
            </p>
            {programsLoading ? (
              <div className="py-10 text-center text-sm text-slate-400">Loading programs…</div>
            ) : programs.length === 0 ? (
              <div className="py-10 text-center text-sm text-slate-400">No programs found</div>
            ) : (
              <ul className="mt-3 divide-y divide-slate-100">
                {programs.map((p) => (
                  <li key={p.id} className="flex items-center gap-3 py-2">
                    <input
                      type="radio"
                      name="program"
                      checked={programId === p.id}
                      onChange={() => setProgramId(p.id)}
                    />
                    <span className="flex-1 text-sm text-slate-700">{p.name}</span>
                    <span className="text-xs text-slate-400">
                      {p.sectionCount} section{p.sectionCount === 1 ? "" : "s"}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        {error && <div className="px-5 text-sm text-red-500">{error}</div>}
        <div className="flex items-center justify-between border-t border-slate-200 px-5 py-3">
          <span className="text-xs text-slate-500">
            {mode === "browse" ? `${picked.size} selected` : programId ? "1 program selected" : "No program selected"}
          </span>
          <div className="flex gap-2">
            <button
              onClick={onClose}
              className="rounded border border-slate-200 px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              disabled={!canCreate || saving}
              onClick={create}
              className="rounded bg-emerald-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving ? "Creating…" : "Create Group"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function GenerateCodesModal({
  group,
  onClose,
  onDone,
}: {
  group: Group;
  onClose: () => void;
  onDone: () => void;
}) {
  const [buyerEmail, setBuyerEmail] = useState("");
  const [sellerReferenceNo, setSellerReferenceNo] = useState("");
  const [pointOfSale, setPointOfSale] = useState("");
  const [count, setCount] = useState(1);
  const [notify, setNotify] = useState(true);
  const [saving, setSaving] = useState(false);
  const [result, setResult] = useState("");

  async function generate() {
    if (!buyerEmail.trim()) return;
    setSaving(true);
    setResult("");
    const res = await fetch("/api/cmds/tools/seller/access-codes", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        groupId: group.id,
        buyerEmail: buyerEmail.trim(),
        sellerReferenceNo: sellerReferenceNo.trim(),
        pointOfSale: pointOfSale.trim(),
        count,
        notify,
      }),
    });
    const d = await res.json().catch(() => ({}));
    setSaving(false);
    if (!res.ok) {
      setResult(d.error || "Generate failed");
      return;
    }
    setResult(
      `Generated ${d.codes.length} code(s): ${d.codes.join(", ")}${
        notify ? ` — email attempted for ${d.notified}, ${d.delivered} delivered${!d.delivered ? " (no email provider configured yet)" : ""}.` : ""
      }`
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-[440px] rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">Generate Access Code(s)</h3>
        <p className="mt-1 text-sm text-slate-500">For group: {group.name}</p>

        <label className="mt-4 block text-sm font-medium text-slate-600">Buyer Email</label>
        <input
          value={buyerEmail}
          onChange={(e) => setBuyerEmail(e.target.value)}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        />

        <div className="mt-4 grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-600">Seller Reference No.</label>
            <input
              value={sellerReferenceNo}
              onChange={(e) => setSellerReferenceNo(e.target.value)}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-600">Point of Sale</label>
            <input
              value={pointOfSale}
              onChange={(e) => setPointOfSale(e.target.value)}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
            />
          </div>
        </div>

        <label className="mt-4 block text-sm font-medium text-slate-600">Number of Codes</label>
        <input
          type="number"
          min={1}
          max={100}
          value={count}
          onChange={(e) => setCount(Math.min(Math.max(Number(e.target.value) || 1, 1), 100))}
          className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
        />

        <label className="mt-4 flex cursor-pointer items-center gap-2 text-sm text-slate-600">
          <input
            type="checkbox"
            checked={notify}
            onChange={(e) => setNotify(e.target.checked)}
            className="accent-emerald-600"
          />
          Email the code(s) to the buyer
        </label>

        {result && <p className="mt-3 text-sm text-slate-600">{result}</p>}

        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            {result ? "Close" : "Cancel"}
          </button>
          <button
            onClick={generate}
            disabled={saving || !buyerEmail.trim()}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {saving ? "Generating…" : "Generate"}
          </button>
        </div>
      </div>
    </div>
  );
}
