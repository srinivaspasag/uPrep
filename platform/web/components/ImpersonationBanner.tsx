"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getImpersonating, clearImpersonating, setSession } from "@/lib/session";

// Shown across the app while an admin is browsing as another user. Lets them
// jump back to their own admin account in one click.
export default function ImpersonationBanner() {
  const router = useRouter();
  const [label, setLabel] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    setLabel(getImpersonating());
  }, []);

  if (!label) return null;

  async function stop() {
    setBusy(true);
    try {
      await fetch("/api/auth/impersonate", { method: "DELETE" });
      clearImpersonating();
      // Refresh the client session snapshot from the restored cookie.
      try {
        const r = await fetch("/api/auth/me");
        if (r.ok) {
          const d = await r.json();
          if (d?.result) setSession(d.result);
        }
      } catch {}
      router.push("/cmds");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="sticky top-0 z-50 flex items-center justify-center gap-3 bg-amber-500 px-4 py-1.5 text-sm text-white">
      <span>
        Viewing as <span className="font-semibold">{label}</span>
      </span>
      <button
        onClick={stop}
        disabled={busy}
        className="rounded bg-white/20 px-2.5 py-0.5 text-xs font-medium hover:bg-white/30 disabled:opacity-60"
      >
        {busy ? "Returning…" : "Return to admin"}
      </button>
    </div>
  );
}
