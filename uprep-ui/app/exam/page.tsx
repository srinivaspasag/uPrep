import Link from "next/link";
import { MarketingShell } from "@/components/MarketingShell";

export const metadata = {
  title: "Exams — UPrep",
  description:
    "Prepare for JEE, NEET, Olympiads, NTSE and more with UPrep's integrated learning platform.",
};

const EXAMS: {
  code: string;
  name: string;
  desc: string;
  tags: string[];
  color: string;
}[] = [
  {
    code: "JEE",
    name: "JEE Mains & Advanced",
    desc: "Complete preparation for engineering entrance with concept videos, tests and mentoring.",
    tags: ["Physics", "Chemistry", "Maths"],
    color: "from-blue-500 to-indigo-500",
  },
  {
    code: "NEET",
    name: "NEET",
    desc: "Medical entrance prep with full syllabus coverage, practice tests and analytics.",
    tags: ["Physics", "Chemistry", "Biology"],
    color: "from-emerald-500 to-teal-500",
  },
  {
    code: "FND",
    name: "Foundation (Class 6–10)",
    desc: "Build strong fundamentals in Math and Science with live classes and weekend tests.",
    tags: ["Math", "Science"],
    color: "from-amber-500 to-orange-500",
  },
  {
    code: "OLY",
    name: "Olympiad Ace",
    desc: "Targeted preparation for IMO, NSO and other olympiads with curated problem sets.",
    tags: ["IMO", "NSO", "IJSO"],
    color: "from-fuchsia-500 to-purple-500",
  },
  {
    code: "NTSE",
    name: "NTSE / NSTSE",
    desc: "Scholarship exam preparation with diagnostic tests and detailed performance reports.",
    tags: ["MAT", "SAT"],
    color: "from-rose-500 to-pink-500",
  },
  {
    code: "KVPY",
    name: "KVPY",
    desc: "Aptitude and science preparation for the Kishore Vaigyanik Protsahan Yojana.",
    tags: ["Aptitude", "Science"],
    color: "from-cyan-500 to-blue-500",
  },
];

export default function ExamPage() {
  return (
    <MarketingShell>
      <section className="bg-gradient-to-r from-slate-900 to-indigo-900 py-20 text-white">
        <div className="mx-auto max-w-4xl px-4 text-center">
          <h1 className="text-4xl font-bold sm:text-5xl">Exams we prepare you for</h1>
          <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-200">
            From foundation to competitive entrances — structured programs, live
            classes, and a learning app that tracks your every step.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16">
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {EXAMS.map((e) => (
            <div
              key={e.code}
              className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <div
                className={`flex h-24 items-center justify-center bg-gradient-to-r ${e.color} text-2xl font-bold text-white`}
              >
                {e.code}
              </div>
              <div className="p-6">
                <h3 className="text-lg font-semibold text-slate-800">{e.name}</h3>
                <p className="mt-2 text-sm text-slate-500">{e.desc}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {e.tags.map((t) => (
                    <span
                      key={t}
                      className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600"
                    >
                      {t}
                    </span>
                  ))}
                </div>
                <Link
                  href="/signup"
                  className="mt-5 inline-block text-sm font-medium text-blue-600 hover:underline"
                >
                  Start preparing →
                </Link>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-blue-600 py-14 text-center text-white">
        <div className="mx-auto max-w-3xl px-4">
          <h2 className="text-2xl font-bold sm:text-3xl">
            Not sure which program fits?
          </h2>
          <p className="mt-3 text-blue-100">
            Sign up and our mentors will help you build the right preparation
            plan.
          </p>
          <div className="mt-6 flex flex-wrap justify-center gap-4">
            <Link
              href="/signup"
              className="rounded-md bg-white px-8 py-3 font-semibold text-blue-700 hover:bg-blue-50"
            >
              Sign Up
            </Link>
            <Link
              href="/login"
              className="rounded-md bg-blue-500 px-8 py-3 font-semibold text-white ring-1 ring-white/40 hover:bg-blue-400"
            >
              Login
            </Link>
          </div>
        </div>
      </section>
    </MarketingShell>
  );
}
