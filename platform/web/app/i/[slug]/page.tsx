"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";

// An institute's own branded entry point — resolves the slug to an org id
// and hands off to /login's existing ?org=&inst= deep link (which already
// pre-selects and freezes the institute, and now also fetches that org's own
// logo — see app/login/page.tsx). This page never talks to auth itself, it's
// purely a friendly-URL -> deep-link redirector.
export default function InstituteEntryPage() {
  const params = useParams<{ slug: string }>();
  const router = useRouter();
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    fetch(`/api/orgs/by-slug?slug=${encodeURIComponent(params.slug)}`)
      .then((r) => r.json())
      .then((d) => {
        if (cancelled) return;
        if (d.error || !d.id) {
          setError("We couldn't find that institute.");
          return;
        }
        router.replace(`/login?org=${encodeURIComponent(d.id)}&inst=${encodeURIComponent(d.name || "")}`);
      })
      .catch(() => {
        if (!cancelled) setError("Something went wrong. Please try again.");
      });
    return () => {
      cancelled = true;
    };
  }, [params.slug, router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 to-slate-200 px-4">
      <div className="text-center text-slate-500">
        {error ? (
          <>
            <p className="text-slate-700">{error}</p>
            <a href="/login" className="mt-3 inline-block text-sm text-blue-600 hover:underline">
              Go to the regular sign-in page
            </a>
          </>
        ) : (
          <p>Loading…</p>
        )}
      </div>
    </div>
  );
}
