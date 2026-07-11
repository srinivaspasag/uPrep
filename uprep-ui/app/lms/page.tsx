import Link from "next/link";
import { MarketingShell } from "@/components/MarketingShell";

export const metadata = {
  title: "UPrep LMS for Institutions",
  description:
    "Launch your own branded learning app. Host courses, run tests, and grow revenue with zero IT investment.",
};

const WHY: { icon: string; title: string }[] = [
  { icon: "🛠️", title: "No IT Investment" },
  { icon: "📡", title: "Increase your Reach" },
  { icon: "💰", title: "Monetize & Sell More" },
  { icon: "🚀", title: "Next-Gen Brand" },
  { icon: "🪙", title: "Minimal Cost" },
];

const CAPABILITIES: { title: string; points: string[] }[] = [
  {
    title: "Create Content",
    points: [
      "Upload PDFs, e-books, videos, tests and more.",
      "Organise content into sub-heads in sequence.",
      "Build quizzes to assess student learning.",
    ],
  },
  {
    title: "Analytics",
    points: [
      "Instant analytics on tests and usage.",
      "Track individual progress and engagement.",
      "Remotely monitor application usage.",
    ],
  },
  {
    title: "Manage Resources",
    points: [
      "Set rules to guide a desired learning path.",
      "Place quiz checkpoints to assess learning.",
      "Share and tag content in a controlled way.",
    ],
  },
  {
    title: "Test Generator",
    points: [
      "Choose test conditions and structure.",
      "Pick difficulty level and marking scheme.",
      "Auto-generate a test within minutes.",
    ],
  },
  {
    title: "Doubts Forum",
    points: [
      "Identify critical topics for discussion.",
      "View and answer doubts asked by students.",
      "Enable peer-to-peer learning.",
    ],
  },
  {
    title: "Content Support",
    points: [
      "Content support as a value-added service.",
      "Over 1200 hours of video content.",
      "Over 2000 tests and 8000 pages of e-books.",
    ],
  },
];

const BENEFITS: { title: string; points: string[] }[] = [
  {
    title: "Create a Marketplace of Your Courses",
    points: [
      "Sell courses from your own app.",
      "Increase revenue and visibility.",
      "Admin tools to create & manage courses.",
    ],
  },
  {
    title: "Run Distance Learning Programs",
    points: [
      "Increase your reach.",
      "Interact with distance-program students.",
      "Real-time tests and feedback on mobile.",
    ],
  },
  {
    title: "Run Tests & Test Series on Mobile",
    points: [
      "Make and sell your own tests.",
      "Use diagnostic tests to understand your base.",
      "Let students see their learning curve.",
    ],
  },
  {
    title: "Enable 24×7 Collaboration",
    points: [
      "Track progress and increase engagement.",
      "Doubts forum for peer-to-peer collaboration.",
      "Higher student engagement.",
    ],
  },
];

const MARKETING: { icon: string; title: string; desc: string }[] = [
  { icon: "✉️", title: "E-mail Marketing", desc: "Run campaigns with Gmail or G-Suite integrated mail." },
  { icon: "🔔", title: "Push Notification", desc: "Text & image on-click app campaigns." },
  { icon: "💬", title: "SMS Marketing", desc: "Reach prospects via our SMS integration." },
];

const STATS = [
  { value: "120+", label: "Organizations" },
  { value: "140K+", label: "Total Students" },
  { value: "16K+", label: "Tests Created" },
  { value: "10M+", label: "Minutes Consumed" },
];

export default function LmsPage() {
  return (
    <MarketingShell>
      {/* Hero */}
      <section className="bg-gradient-to-r from-blue-700 to-indigo-700 py-20 text-white">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-4 md:grid-cols-2">
          <div>
            <h1 className="text-4xl font-bold leading-tight sm:text-5xl">
              Launch your own learning app
            </h1>
            <p className="mt-4 max-w-md text-lg text-blue-100">
              Host courses, increase your market reach, and earn more revenue —
              on a fully branded platform with zero IT investment.
            </p>
            <a
              href="#get-in-touch"
              className="mt-8 inline-block rounded-md bg-white px-8 py-3 font-semibold text-blue-700 shadow-lg transition hover:bg-blue-50"
            >
              Get in touch
            </a>
          </div>
          <div className="hidden justify-end md:flex">
            <img
              src="/legacy/test_screen.png"
              alt="UPrep content management"
              className="w-[420px] max-w-none rounded-xl shadow-2xl"
            />
          </div>
        </div>
      </section>

      {/* Why */}
      <section className="mx-auto max-w-6xl px-4 py-16">
        <h2 className="text-center text-2xl font-bold text-slate-800 sm:text-3xl">
          Why UPrep?
        </h2>
        <div className="mt-10 grid gap-5 sm:grid-cols-3 lg:grid-cols-5">
          {WHY.map((w) => (
            <div
              key={w.title}
              className="rounded-xl border border-slate-200 bg-white p-5 text-center shadow-sm"
            >
              <div className="text-3xl">{w.icon}</div>
              <h3 className="mt-2 text-sm font-semibold text-slate-800">
                {w.title}
              </h3>
            </div>
          ))}
        </div>
      </section>

      {/* Capabilities */}
      <section className="bg-slate-50 py-16">
        <div className="mx-auto max-w-6xl px-4">
          <h2 className="text-center text-2xl font-bold text-slate-800">
            A complete content &amp; delivery system
          </h2>
          <div className="mt-10 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {CAPABILITIES.map((c) => (
              <div
                key={c.title}
                className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-100"
              >
                <h3 className="font-semibold text-blue-700">{c.title}</h3>
                <ul className="mt-3 space-y-2 text-sm text-slate-600">
                  {c.points.map((p) => (
                    <li key={p} className="flex gap-2">
                      <span className="text-blue-500">✓</span>
                      <span>{p}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
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

      {/* Benefits */}
      <section className="mx-auto max-w-6xl px-4 py-16">
        <h2 className="text-center text-2xl font-bold text-slate-800">
          Benefits of your own mobile / web app
        </h2>
        <div className="mt-10 grid gap-6 md:grid-cols-2">
          {BENEFITS.map((b) => (
            <div
              key={b.title}
              className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
            >
              <h3 className="font-semibold text-slate-800">{b.title}</h3>
              <ul className="mt-3 space-y-2 text-sm text-slate-600">
                {b.points.map((p) => (
                  <li key={p} className="flex gap-2">
                    <span className="text-emerald-500">✓</span>
                    <span>{p}</span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </section>

      {/* Marketing tools */}
      <section className="bg-slate-50 py-16">
        <div className="mx-auto max-w-5xl px-4">
          <h2 className="text-center text-2xl font-bold text-slate-800">
            Reach your students on the right channel
          </h2>
          <div className="mt-10 grid gap-6 sm:grid-cols-3">
            {MARKETING.map((m) => (
              <div
                key={m.title}
                className="rounded-xl bg-white p-6 text-center shadow-sm ring-1 ring-slate-100"
              >
                <div className="text-3xl">{m.icon}</div>
                <h3 className="mt-2 font-semibold text-slate-800">{m.title}</h3>
                <p className="mt-1 text-sm text-slate-500">{m.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section id="get-in-touch" className="mx-auto max-w-4xl px-4 py-16 text-center">
        <h2 className="text-2xl font-bold text-slate-800 sm:text-3xl">
          Ready to transform your students&rsquo; learning experience?
        </h2>
        <p className="mt-3 text-slate-500">
          Talk to us about launching your branded UPrep platform.
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-4">
          <Link
            href="/signup"
            className="rounded-md bg-blue-600 px-8 py-3 font-semibold text-white hover:bg-blue-700"
          >
            Get Started
          </Link>
          <Link
            href="/"
            className="rounded-md border border-slate-300 px-8 py-3 font-semibold text-slate-700 hover:bg-slate-50"
          >
            Contact us
          </Link>
        </div>
        <div className="mt-6 text-sm text-slate-500">
          📞 040-48215405 &nbsp;·&nbsp; ✉️ info@uprep.in
        </div>
      </section>
    </MarketingShell>
  );
}
