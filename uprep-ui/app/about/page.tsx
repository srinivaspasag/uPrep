import Link from "next/link";
import { MarketingShell } from "@/components/MarketingShell";

export const metadata = {
  title: "About UPrep",
  description:
    "UPrep is India's first integrated learning platform, combining live classes with a personalised learning app.",
};

const FOUNDERS: { name: string; role: string; bio: string }[] = [
  {
    name: "Krishna Rao Akula",
    role: "Co-Founder",
    bio: "Drives UPrep's vision of accessible, high-quality integrated learning for every student.",
  },
  {
    name: "Mohan Krishna Punyamurtula",
    role: "Co-Founder",
    bio: "Leads product and pedagogy, blending classroom rigour with technology.",
  },
];

const VALUES: { icon: string; title: string; desc: string }[] = [
  { icon: "🎯", title: "Student First", desc: "Every decision is measured by student outcomes." },
  { icon: "🧑‍🏫", title: "Great Teaching", desc: "Renowned educators, small batches, real mentoring." },
  { icon: "📈", title: "Measurable Progress", desc: "Analytics and regular tests keep learning on track." },
  { icon: "🌍", title: "Reach", desc: "A platform that scales quality learning across India." },
];

export default function AboutPage() {
  return (
    <MarketingShell>
      <section className="bg-gradient-to-r from-slate-900 to-indigo-900 py-20 text-white">
        <div className="mx-auto max-w-4xl px-4 text-center">
          <h1 className="text-4xl font-bold sm:text-5xl">About UPrep</h1>
          <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-200">
            UPrep is India&rsquo;s first integrated learning platform for
            students — pairing live online classes with a personalised learning
            app so every learner gets the attention, practice, and insight they
            need to excel.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16">
        <div className="grid gap-10 md:grid-cols-2">
          <div>
            <h2 className="text-2xl font-bold text-slate-800">Our Mission</h2>
            <p className="mt-4 text-slate-600">
              We redefine the classroom and equip it with standard pedagogy and
              a stress-free environment. Our learning app gives students concept
              videos, a 24×7 doubts forum, a recorded-class library, weekend
              tests, and detailed analytics — everything they need in one place.
            </p>
            <p className="mt-4 text-slate-600">
              For institutions, UPrep provides a complete content management and
              distribution system to launch their own branded learning platform
              with zero IT investment.
            </p>
            <Link
              href="/lms"
              className="mt-6 inline-block rounded-md bg-blue-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-blue-700"
            >
              Explore UPrep for institutions →
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4">
            {VALUES.map((v) => (
              <div
                key={v.title}
                className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
              >
                <div className="text-2xl">{v.icon}</div>
                <h3 className="mt-2 font-semibold text-slate-800">{v.title}</h3>
                <p className="mt-1 text-sm text-slate-500">{v.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="bg-slate-50 py-16">
        <div className="mx-auto max-w-5xl px-4">
          <h2 className="text-center text-2xl font-bold text-slate-800">
            Leadership
          </h2>
          <div className="mx-auto mt-10 grid max-w-3xl gap-6 sm:grid-cols-2">
            {FOUNDERS.map((f) => (
              <div
                key={f.name}
                className="rounded-xl bg-white p-6 text-center shadow-sm ring-1 ring-slate-100"
              >
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-blue-600 to-indigo-600 text-xl font-bold text-white">
                  {f.name
                    .split(" ")
                    .map((n) => n[0])
                    .slice(0, 2)
                    .join("")}
                </div>
                <h3 className="mt-3 font-semibold text-slate-800">{f.name}</h3>
                <div className="text-sm text-blue-600">{f.role}</div>
                <p className="mt-2 text-sm text-slate-500">{f.bio}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </MarketingShell>
  );
}
