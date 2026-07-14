"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Cert = { id: string; name: string; code: string | null; eligible: boolean; testsCompleted: number };

export default function CertificatesPage() {
  const [items, setItems] = useState<Cert[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const uid = getSession()?.id || "";
    fetch(`/api/learn/certificates?userId=${encodeURIComponent(uid)}`)
      .then((r) => r.json())
      .then((d) => setItems(d.items || []))
      .finally(() => setLoading(false));
  }, []);

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
              {c.eligible ? (
                <Link
                  href={`/learn/certificates/${c.id}`}
                  className="mt-3 inline-block rounded-md bg-emerald-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-emerald-700"
                >
                  View certificate
                </Link>
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
