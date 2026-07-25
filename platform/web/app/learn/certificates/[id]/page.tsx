"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

type Cert = {
  id: string;
  serial: string;
  name: string;
  studentName: string;
  issuedAt: number;
};

export default function CertificateView({ params }: { params: { id: string } }) {
  const [cert, setCert] = useState<Cert | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`/api/learn/certificates?certId=${encodeURIComponent(params.id)}`)
      .then((r) => r.json())
      .then((d) => setCert(d.certificate || null))
      .finally(() => setLoading(false));
  }, [params.id]);

  const issuedDate = cert
    ? new Date(cert.issuedAt).toLocaleDateString(undefined, {
        year: "numeric",
        month: "long",
        day: "numeric",
      })
    : "";

  return (
    <div className="min-h-screen bg-slate-100 py-8 print:bg-white print:py-0">
      <div className="mx-auto max-w-3xl px-4">
        <div className="mb-4 flex items-center justify-between print:hidden">
          <Link href="/learn/certificates" className="text-sm text-slate-500 hover:text-slate-800">
            ← Back
          </Link>
          <button
            onClick={() => window.print()}
            disabled={!cert}
            className="rounded-md bg-slate-800 px-4 py-1.5 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
          >
            Print / Save PDF
          </button>
        </div>

        {loading ? (
          <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
        ) : !cert ? (
          <div className="rounded-md bg-amber-50 px-4 py-3 text-center text-sm text-amber-700">
            This certificate could not be found. It may not have been issued yet.
          </div>
        ) : (
          <div className="relative overflow-hidden rounded-lg bg-white p-12 text-center shadow-lg ring-8 ring-emerald-600/10">
            <div className="pointer-events-none absolute inset-4 rounded border-2 border-emerald-600/30" />
            <div className="relative">
              <div className="text-5xl">🎓</div>
              <div className="mt-4 text-sm font-semibold uppercase tracking-[0.3em] text-emerald-700">
                Certificate of Completion
              </div>
              <div className="mt-8 text-sm text-slate-500">This certifies that</div>
              <div className="mt-2 text-3xl font-bold text-slate-800">{cert.studentName}</div>
              <div className="mt-6 text-sm text-slate-500">has successfully participated in</div>
              <div className="mt-2 text-xl font-semibold text-slate-700">{cert.name}</div>
              <div className="mt-10 flex items-center justify-between px-6 text-xs text-slate-400">
                <div>
                  <div className="border-t border-slate-300 pt-1 font-medium text-slate-600">
                    UPrep Academy
                  </div>
                  Issued {issuedDate}
                </div>
                <div className="text-right">
                  <div className="border-t border-slate-300 pt-1 font-medium text-slate-600">
                    Authorized Signature
                  </div>
                  Director of Learning
                </div>
              </div>
              <div className="mt-6 font-mono text-[11px] text-slate-400">
                Certificate ID: {cert.serial}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
