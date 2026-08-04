"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Org = {
  id: string;
  name: string;
  fullName: string;
  website: string;
  contactNumber: string;
  type: string;
  address: string;
  description: string;
  authType: string;
  doubtsForumMode: string;
  socialMedia: Record<string, string>;
  logoUrl: string;
  playStoreLink: string;
};

const TYPES = ["COLLEGE", "SCHOOL", "UNIVERSITY", "INSTITUTE", "COMPANY", "OTHER"];

export default function OrganizationInfoPage() {
  const [org, setOrg] = useState<Org | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState("");
  const [logoUploading, setLogoUploading] = useState(false);

  useEffect(() => {
    fetch("/api/cmds/tools/organization")
      .then((r) => r.json())
      .then((d) => setOrg(d.org || null))
      .finally(() => setLoading(false));
  }, []);

  function set<K extends keyof Org>(k: K, v: Org[K]) {
    setOrg((o) => (o ? { ...o, [k]: v } : o));
  }
  function setSocial(k: string, v: string) {
    setOrg((o) => (o ? { ...o, socialMedia: { ...o.socialMedia, [k]: v } } : o));
  }

  async function onLogoFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    setLogoUploading(true);
    try {
      const fd = new FormData();
      fd.append("file", file);
      const res = await fetch("/api/cmds/tools/organization/logo", { method: "POST", body: fd });
      const d = await res.json().catch(() => ({}));
      if (res.ok && d.url) set("logoUrl", d.url);
    } finally {
      setLogoUploading(false);
    }
  }

  async function save() {
    if (!org) return;
    setMsg("");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/tools/organization", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(org),
      });
      setMsg(res.ok ? "Saved." : "Save failed.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[720px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Edit Organization Info</h1>

        {loading || !org ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : (
          <div className="mt-6 space-y-4">
            <Row label="Logo">
              <div className="flex items-center gap-4">
                {org.logoUrl ? (
                  <img
                    src={org.logoUrl}
                    alt="Institute logo"
                    className="h-16 w-16 rounded border border-slate-200 object-contain bg-white"
                  />
                ) : (
                  <div className="flex h-16 w-16 items-center justify-center rounded border border-dashed border-slate-300 text-[10px] text-slate-400">
                    No logo
                  </div>
                )}
                <label className="cursor-pointer rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50">
                  {logoUploading ? "Uploading…" : "Upload logo"}
                  <input
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={onLogoFile}
                    disabled={logoUploading}
                  />
                </label>
              </div>
            </Row>
            <Row label="Name*">
              <input className={inp} value={org.name} onChange={(e) => set("name", e.target.value)} />
            </Row>
            <Row label="Full Name*">
              <input className={inp} value={org.fullName} onChange={(e) => set("fullName", e.target.value)} />
            </Row>
            <Row label="Website*">
              <input
                className={inp}
                value={org.website}
                placeholder="eg. http://www.uprep.in"
                onChange={(e) => set("website", e.target.value)}
              />
            </Row>
            <Row label="Contact Number*">
              <input
                className={inp}
                value={org.contactNumber}
                onChange={(e) => set("contactNumber", e.target.value)}
              />
            </Row>
            <Row label="Type">
              <select className={inp} value={org.type} onChange={(e) => set("type", e.target.value)}>
                {TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t.charAt(0) + t.slice(1).toLowerCase()}
                  </option>
                ))}
              </select>
            </Row>
            <Row label="Address*">
              <textarea
                className={`${inp} h-20`}
                value={org.address}
                onChange={(e) => set("address", e.target.value)}
              />
            </Row>
            <Row label="Description">
              <textarea
                className={`${inp} h-20`}
                value={org.description}
                onChange={(e) => set("description", e.target.value)}
              />
            </Row>
            <Row label="Login Mechanism">
              <div className="flex gap-4 text-sm text-slate-600">
                {["VEDANTU", "EXT_AUTH_ORG"].map((a) => (
                  <label key={a} className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="authType"
                      checked={org.authType === a}
                      onChange={() => set("authType", a)}
                      className="accent-emerald-600"
                    />
                    {a === "VEDANTU" ? "UPrep System for Authentication" : "External System"}
                  </label>
                ))}
              </div>
            </Row>
            <Row label="Doubts Forum Mode">
              <div className="flex gap-4 text-sm text-slate-600">
                {["PUBLIC", "PRIVATE", "HIDDEN"].map((m) => (
                  <label key={m} className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="doubtsForumMode"
                      checked={org.doubtsForumMode === m}
                      onChange={() => set("doubtsForumMode", m)}
                      className="accent-emerald-600"
                    />
                    {m.charAt(0) + m.slice(1).toLowerCase()}
                  </label>
                ))}
              </div>
            </Row>

            <div className="pt-2">
              <div className="mb-2 text-sm font-semibold text-slate-600">Social Pages</div>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { key: "facebook", label: "Facebook" },
                  { key: "twitter", label: "Twitter" },
                  { key: "linkedin", label: "LinkedIn" },
                  { key: "blogger", label: "Blog" },
                  { key: "youtube", label: "YouTube" },
                ].map((s) => (
                  <input
                    key={s.key}
                    className={inp}
                    placeholder={s.label}
                    value={org.socialMedia?.[s.key] || ""}
                    onChange={(e) => setSocial(s.key, e.target.value)}
                  />
                ))}
              </div>
            </div>

            <div className="pt-2">
              <div className="mb-2 text-sm font-semibold text-slate-600">App Store Links</div>
              <input
                className={inp}
                placeholder="Android App Link (Play Store)"
                value={org.playStoreLink}
                onChange={(e) => set("playStoreLink", e.target.value)}
              />
            </div>

            <div className="flex items-center gap-3 pt-3">
              <button
                onClick={save}
                disabled={saving}
                className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {saving ? "Saving…" : "Save"}
              </button>
              {msg && (
                <span className={msg === "Saved." ? "text-sm text-emerald-600" : "text-sm text-red-600"}>
                  {msg}
                </span>
              )}
            </div>
          </div>
        )}
      </div>
    </CmdsShell>
  );
}

const inp =
  "w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500";

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="grid grid-cols-[180px_1fr] items-start gap-4">
      <label className="pt-2 text-sm font-medium text-slate-600">{label}</label>
      <div>{children}</div>
    </div>
  );
}
