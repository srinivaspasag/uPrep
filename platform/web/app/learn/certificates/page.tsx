"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Cert = {
  id: string;
  name: string;
  code: string | null;
  eligible: boolean;
  testsCompleted: number;
  issued: boolean;
  certificateId: string | null;
  serial: string | null;
};

export default function CertificatesPage() {
  const router = useRouter();
  const [items, setItems] = useState<Cert[]>([]);
  const [loading, setLoading] = useState(true);
  const [claiming, setClaiming] = useState<string | null>(null);

  function load() {
    const uid = getSession()?.id || "";
    return fetch(`/api/learn/certificates?userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => setItems(d.items || []))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function claim(programId: string) {
    setClaiming(programId);
    try {
      const res = await fetch("/api/learn/certificates", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ programId }),
      });
      const d = await res.json().catch(() => ({}));
      if (res.ok && d.certificate?.id) {
        router.push(`/learn/certificates/${d.certificate.id}`);
      } else {
        alert(d.error || "Could not issue certificate");
      }
    } finally {
      setClaiming(null);
    }
  }

  return (
    <LmsShell active="certificates">
      <h1 className="text-lg font-semibold text-slate-800">Certificates</h1>
      <p className="mt-1 text-sm text-slate-500">Earn certificates by completing tests in your programs.</p>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No programs available.
        </div>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          {items.map((c) => (
            <div key={c.id} className="rounded-lg border border-slate-200 p-5">
              <div className="text-2xl">🎓</div>
              <div className="mt-2 font-medium text-slate-800">{c.name}</div>
              <div className="mt-1 text-xs text-slate-400">{c.testsCompleted} tests completed</div>
              {c.serial && (
                <div className="mt-1 font-mono text-[11px] text-slate-400">{c.serial}</div>
              )}
              {c.issued && c.certificateId ? (
                <Link
                  href={`/learn/certificates/${c.certificateId}`}
                  className="mt-3 inline-block rounded-md bg-emerald-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-emerald-700"
                >
                  View certificate
                </Link>
              ) : c.eligible ? (
                <button
                  onClick={() => claim(c.id)}
                  disabled={claiming === c.id}
                  className="mt-3 inline-block rounded-md bg-blue-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {claiming === c.id ? "Issuing…" : "Claim certificate"}
                </button>
              ) : (
                <div className="mt-3 rounded-md bg-slate-100 px-3 py-1.5 text-xs text-slate-400">
                  🔒 Complete a test to unlock
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </LmsShell>
  );
}
