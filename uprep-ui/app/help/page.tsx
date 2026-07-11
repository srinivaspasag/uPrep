"use client";

import { useMemo, useState } from "react";
import Link from "next/link";

type Topic = { q: string; a: string; cat: string };

const TOPICS: Topic[] = [
  { cat: "Getting started", q: "How do I log in?", a: "Use your registered email or member ID and password on the login screen. Forgot it? Use 'Forgot Password' to receive a reset link." },
  { cat: "Getting started", q: "How do I change my profile or password?", a: "Open the avatar menu (top right) → Profile & Settings. Edit your details, or use the Change Password form." },
  { cat: "Library & content", q: "How do I find content?", a: "Use the Digital Library tab, or the search box in the header to search across tests, videos, documents and doubts." },
  { cat: "Library & content", q: "How do I bookmark content?", a: "Tap the ☆ star on any content card. Find everything you saved under the ☆ Bookmarks icon in the header." },
  { cat: "Tests", q: "How do I take a test?", a: "Open a test from the library and press Start Test. Answer the questions and submit before the timer ends. Auto-graded tests show your score instantly." },
  { cat: "Tests", q: "Where do I see my performance?", a: "The Analytics tab shows your attempts and accuracy. The Leaderboard ranks you against your batch." },
  { cat: "Doubts & community", q: "How do I ask a doubt?", a: "Go to the Doubts Forum and press 'Ask a Doubt'. Peers and teachers can answer. You'll find your questions under the 'My Doubts' tab." },
  { cat: "Doubts & community", q: "What are Challenges?", a: "Challenges are time-boxed test competitions. Join one from the Challenges tab and compete for the top of the leaderboard." },
  { cat: "Assignments", q: "How do I submit an assignment?", a: "Open the Assignments tab, pick an assignment and press 'Submit answer'. You can edit your submission until it's graded." },
  { cat: "Certificates", q: "How do I get a certificate?", a: "Complete at least one test in a program, then open the Certificates tab and download your certificate as a PDF." },
];

export default function HelpCenter() {
  const [q, setQ] = useState("");
  const [open, setOpen] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const term = q.trim().toLowerCase();
    if (!term) return TOPICS;
    return TOPICS.filter((t) => (t.q + " " + t.a + " " + t.cat).toLowerCase().includes(term));
  }, [q]);

  const cats = useMemo(() => Array.from(new Set(filtered.map((t) => t.cat))), [filtered]);

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex h-14 max-w-3xl items-center justify-between px-4">
          <span className="font-semibold text-slate-800">UPrep · Help Center</span>
          <Link href="/learn/library" className="text-sm text-emerald-600 hover:underline">
            ← Back to app
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-slate-800">How can we help?</h1>
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search help topics…"
          className="mt-4 w-full rounded-lg border border-slate-300 px-4 py-3 text-sm outline-none focus:border-emerald-400"
        />

        {filtered.length === 0 ? (
          <div className="py-16 text-center text-sm text-slate-400">
            No topics match “{q}”. Try different keywords or{" "}
            <a href="mailto:support@uprep.example" className="text-emerald-600 hover:underline">
              contact support
            </a>
            .
          </div>
        ) : (
          cats.map((cat) => (
            <section key={cat} className="mt-8">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">{cat}</h2>
              <div className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
                {filtered
                  .filter((t) => t.cat === cat)
                  .map((t) => (
                    <div key={t.q}>
                      <button
                        onClick={() => setOpen(open === t.q ? null : t.q)}
                        className="flex w-full items-center justify-between px-4 py-3 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                      >
                        {t.q}
                        <span className="text-slate-400">{open === t.q ? "−" : "+"}</span>
                      </button>
                      {open === t.q && <div className="px-4 pb-4 text-sm text-slate-600">{t.a}</div>}
                    </div>
                  ))}
              </div>
            </section>
          ))
        )}

        <div className="mt-10 rounded-lg bg-emerald-50 p-5 text-center">
          <div className="text-sm font-medium text-emerald-800">Still need help?</div>
          <a
            href="mailto:support@uprep.example"
            className="mt-2 inline-block rounded-md bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700"
          >
            Contact Support
          </a>
        </div>
      </main>
    </div>
  );
}
