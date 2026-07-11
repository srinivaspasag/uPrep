"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { MarketingShell } from "@/components/MarketingShell";

type Program = {
  id: string;
  name: string;
  code: string | null;
  description: string;
  isOffline: boolean;
};

const FEATURES: { icon: string; title: string; desc: string }[] = [
  { icon: "🎓", title: "Renowned Teachers", desc: "Learn from India's most experienced educators." },
  { icon: "👥", title: "Max 20 per batch", desc: "Small batches for personal attention." },
  { icon: "🧭", title: "Personal Mentoring", desc: "One-on-one guidance to stay on track." },
  { icon: "🎬", title: "In-App Concept Videos", desc: "Bite-sized videos for every concept." },
  { icon: "💬", title: "24×7 Doubts Forum", desc: "Ask anytime, get answers fast." },
  { icon: "📚", title: "Recorded Library", desc: "Revisit every class, whenever you want." },
  { icon: "📝", title: "Weekend Tests", desc: "Regular assessment to measure progress." },
  { icon: "📊", title: "Detailed Analytics", desc: "Know exactly where to improve." },
];

const STATS: { value: string; label: string }[] = [
  { value: "120+", label: "Organizations" },
  { value: "140K+", label: "Students" },
  { value: "16K+", label: "Tests Created" },
  { value: "10M+", label: "Minutes Learned" },
];

export default function Home() {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/programs")
      .then((r) => r.json())
      .then((d) => setPrograms(d.programs || []))
      .catch(() => setPrograms([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MarketingShell>
      {/* Hero */}
      <section
        className="relative bg-cover bg-center"
        style={{ backgroundImage: "url(/legacy/defaultBG.jpg)" }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-slate-900/85 to-indigo-900/70" />
        <div className="relative mx-auto grid max-w-6xl items-center gap-10 px-4 py-24 md:grid-cols-2">
          <div className="text-white">
            <span className="inline-block rounded-full bg-white/15 px-3 py-1 text-xs font-medium uppercase tracking-wide">
              Live Classes + Learning App
            </span>
            <h1 className="mt-4 text-4xl font-bold leading-tight drop-shadow sm:text-5xl">
              India&rsquo;s first integrated learning platform for students.
            </h1>
            <p className="mt-4 max-w-md text-lg text-slate-200">
              We redefine the classroom with standard pedagogy, a stress-free
              environment, and a personalised learning app to optimise every
              student&rsquo;s journey.
            </p>
            <div className="mt-8 flex flex-wrap items-center gap-4">
              <Link
                href="/signup"
                className="rounded-md bg-blue-600 px-8 py-3 font-semibold text-white shadow-lg transition hover:bg-blue-700"
              >
                Get Started
              </Link>
              <Link
                href="/lms"
                className="rounded-md bg-white/10 px-8 py-3 font-semibold text-white ring-1 ring-white/30 transition hover:bg-white/20"
              >
                Launch your own LMS
              </Link>
            </div>
          </div>
          <div className="relative hidden h-[340px] md:block">
            <img
              src="/legacy/laptop.png"
              alt="UPrep analytics on laptop"
              className="absolute right-0 top-6 w-[440px] max-w-none drop-shadow-2xl"
            />
            <img
              src="/legacy/phone.png"
              alt="UPrep on mobile"
              className="absolute bottom-0 left-2 w-[130px] drop-shadow-2xl"
            />
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="mx-auto max-w-6xl px-4 py-16">
        <h2 className="text-center text-2xl font-bold text-slate-800 sm:text-3xl">
          Live Online Classes + Personalised Learning App
        </h2>
        <p className="mx-auto mt-2 max-w-2xl text-center text-slate-500">
          Everything a student needs to learn, practice, and excel — in one place.
        </p>
        <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((f) => (
            <div
              key={f.title}
              className="rounded-xl border border-slate-200 bg-white p-5 text-center shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 text-2xl">
                {f.icon}
              </div>
              <h3 className="mt-3 font-semibold text-slate-800">{f.title}</h3>
              <p className="mt-1 text-sm text-slate-500">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Stats */}
      <section className="bg-gradient-to-r from-blue-600 to-indigo-600 py-14 text-white">
        <div className="mx-auto grid max-w-5xl grid-cols-2 gap-8 px-4 text-center sm:grid-cols-4">
          {STATS.map((s) => (
            <div key={s.label}>
              <div className="text-3xl font-bold sm:text-4xl">{s.value}</div>
              <div className="mt-1 text-sm text-blue-100">{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Programs (live from backend) */}
      <section className="mx-auto max-w-6xl px-4 py-16">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold text-slate-800">Our Programs</h2>
          <Link
            href="/exam"
            className="text-sm font-medium text-blue-600 hover:underline"
          >
            View all exams →
          </Link>
        </div>

        {loading ? (
          <div className="mt-10 text-center text-slate-400">Loading programs…</div>
        ) : programs.length === 0 ? (
          <div className="mt-10 rounded-lg border-2 border-dashed border-slate-200 py-16 text-center">
            <div className="text-4xl">📘</div>
            <p className="mt-2 font-medium text-slate-600">Programs coming soon</p>
            <p className="text-slate-400">
              New batches are being set up. Sign up to get notified.
            </p>
          </div>
        ) : (
          <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {programs.map((p) => (
              <Link
                key={p.id}
                href="/login"
                className="group rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md hover:ring-1 hover:ring-blue-200"
              >
                <div className="flex items-center justify-between">
                  <span className="rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                    {p.code || "PROGRAM"}
                  </span>
                  {p.isOffline && (
                    <span className="text-xs text-slate-400">Offline</span>
                  )}
                </div>
                <h3 className="mt-3 text-lg font-semibold text-slate-800 group-hover:text-blue-700">
                  {p.name}
                </h3>
                <p className="mt-1 line-clamp-2 text-sm text-slate-500">
                  {p.description || "Enroll to start learning."}
                </p>
                <span className="mt-4 inline-block text-sm font-medium text-blue-600">
                  View program →
                </span>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Testimonials */}
      <section className="bg-slate-50 py-16">
        <div className="mx-auto max-w-5xl px-4">
          <h2 className="text-center text-2xl font-bold text-slate-800">
            What students &amp; parents say
          </h2>
          <div className="mt-10 grid gap-6 md:grid-cols-3">
            {[
              {
                quote:
                  "The doubts forum and weekend tests kept me consistent. My scores jumped in just a term.",
                name: "Ananya, Class 10",
              },
              {
                quote:
                  "Small batches meant my son actually got attention. The analytics showed us exactly what to fix.",
                name: "R. Menon, Parent",
              },
              {
                quote:
                  "Recorded classes let me revise anytime. The concept videos are gold for revision.",
                name: "Karthik, JEE Aspirant",
              },
            ].map((t) => (
              <div
                key={t.name}
                className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-100"
              >
                <div className="text-3xl text-blue-200">&ldquo;</div>
                <p className="text-slate-600">{t.quote}</p>
                <div className="mt-4 text-sm font-semibold text-slate-800">
                  {t.name}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Get in touch */}
      <ContactSection />
    </MarketingShell>
  );
}

function ContactSection() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [classComp, setClassComp] = useState("");
  const [saving, setSaving] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!name.trim() || (!email.trim() && !phone.trim())) {
      setError("Please enter your name and a phone or email.");
      return;
    }
    setSaving(true);
    try {
      const res = await fetch("/api/enquiry", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, phone, classComp }),
      });
      const d = await res.json();
      if (!res.ok || d.error) {
        setError(d.error || "Something went wrong. Please try again.");
        return;
      }
      setDone(true);
    } catch {
      setError("Service unavailable. Please try again later.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section id="get-in-touch" className="mx-auto max-w-6xl px-4 py-16">
      <div className="grid items-center gap-10 md:grid-cols-2">
        <div>
          <h2 className="text-2xl font-bold text-slate-800 sm:text-3xl">
            Get in touch
          </h2>
          <p className="mt-3 max-w-md text-slate-500">
            Tell us a little about yourself and our team will reach out to help
            you find the right program.
          </p>
          <div className="mt-6 space-y-2 text-sm text-slate-600">
            <div>📞 040-48215405</div>
            <div>✉️ info@uprep.in</div>
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          {done ? (
            <div className="py-10 text-center">
              <div className="text-4xl">✅</div>
              <p className="mt-3 font-semibold text-slate-800">Thank you!</p>
              <p className="mt-1 text-sm text-slate-500">
                We&rsquo;ve received your details and will get back to you shortly.
              </p>
            </div>
          ) : (
            <form onSubmit={submit} className="space-y-4">
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your name"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-400"
              />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-400"
              />
              <input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="Phone number"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-400"
              />
              <select
                value={classComp}
                onChange={(e) => setClassComp(e.target.value)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-600 outline-none focus:border-blue-400"
              >
                <option value="">Choose class</option>
                {["Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "JEE", "NEET"].map(
                  (c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  )
                )}
              </select>
              {error && (
                <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
                  {error}
                </div>
              )}
              <button
                type="submit"
                disabled={saving}
                className="w-full rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
              >
                {saving ? "Submitting…" : "Submit"}
              </button>
            </form>
          )}
        </div>
      </div>
    </section>
  );
}
