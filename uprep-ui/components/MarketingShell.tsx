"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

const NAV: { label: string; href: string; match: string }[] = [
  { label: "Home", href: "/", match: "home" },
  { label: "About", href: "/about", match: "about" },
  { label: "LMS", href: "/lms", match: "lms" },
  { label: "Exams", href: "/exam", match: "exam" },
];

export function MarketingShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  const isActive = (href: string) =>
    href === "/" ? pathname === "/" : pathname?.startsWith(href);

  return (
    <div className="min-h-screen bg-white text-slate-800">
      {/* Nav */}
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
          <Link href="/" className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-blue-600 to-indigo-600 font-bold text-white">
              U
            </div>
            <span className="text-lg font-bold tracking-tight text-slate-800">
              UPrep
            </span>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden items-center gap-7 md:flex">
            {NAV.map((n) => (
              <Link
                key={n.href}
                href={n.href}
                className={`text-sm font-medium transition ${
                  isActive(n.href)
                    ? "text-blue-600"
                    : "text-slate-600 hover:text-blue-600"
                }`}
              >
                {n.label}
              </Link>
            ))}
            <a
              href="#get-in-touch"
              className="text-sm font-medium text-slate-600 transition hover:text-blue-600"
            >
              Contact
            </a>
          </nav>

          <div className="hidden items-center gap-3 md:flex">
            <Link
              href="/login"
              className="rounded-md px-4 py-2 text-sm font-semibold text-blue-600 transition hover:bg-blue-50"
            >
              Login
            </Link>
            <Link
              href="/signup"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
            >
              Sign Up
            </Link>
          </div>

          {/* Mobile toggle */}
          <button
            onClick={() => setOpen((v) => !v)}
            className="flex h-9 w-9 items-center justify-center rounded-md text-slate-600 md:hidden"
            aria-label="Menu"
          >
            <div className="space-y-1.5">
              <span className="block h-0.5 w-5 bg-slate-700" />
              <span className="block h-0.5 w-5 bg-slate-700" />
              <span className="block h-0.5 w-5 bg-slate-700" />
            </div>
          </button>
        </div>

        {open && (
          <div className="border-t border-slate-100 bg-white px-4 py-3 md:hidden">
            <div className="flex flex-col gap-1">
              {NAV.map((n) => (
                <Link
                  key={n.href}
                  href={n.href}
                  onClick={() => setOpen(false)}
                  className="rounded-md px-2 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                  {n.label}
                </Link>
              ))}
              <div className="mt-2 flex gap-2">
                <Link
                  href="/login"
                  className="flex-1 rounded-md border border-blue-200 px-3 py-2 text-center text-sm font-semibold text-blue-600"
                >
                  Login
                </Link>
                <Link
                  href="/signup"
                  className="flex-1 rounded-md bg-blue-600 px-3 py-2 text-center text-sm font-semibold text-white"
                >
                  Sign Up
                </Link>
              </div>
            </div>
          </div>
        )}
      </header>

      <main>{children}</main>

      <MarketingFooter />
    </div>
  );
}

function MarketingFooter() {
  const cols: { title: string; links: { label: string; href: string }[] }[] = [
    {
      title: "Resources",
      links: [
        { label: "LMS", href: "/lms" },
        { label: "Students", href: "/signup" },
        { label: "Digital Library", href: "/login" },
        { label: "Videos", href: "/login" },
        { label: "Download Mobile App", href: "#get-in-touch" },
      ],
    },
    {
      title: "Programs",
      links: [
        { label: "JEE", href: "/exam" },
        { label: "NEET", href: "/exam" },
        { label: "Test Series", href: "/exam" },
        { label: "Olympiad Ace", href: "/exam" },
        { label: "Foundation", href: "/exam" },
      ],
    },
    {
      title: "Exams",
      links: [
        { label: "JEE Mains & Advanced", href: "/exam" },
        { label: "NEET", href: "/exam" },
        { label: "IMO", href: "/exam" },
        { label: "KVPY", href: "/exam" },
        { label: "NSO / NSTSE / NTSE", href: "/exam" },
      ],
    },
    {
      title: "Company",
      links: [
        { label: "About", href: "/about" },
        { label: "Contact Us", href: "#get-in-touch" },
        { label: "Privacy Policy", href: "/about" },
      ],
    },
  ];

  return (
    <footer className="border-t border-slate-200 bg-slate-900 text-slate-300">
      <div className="mx-auto max-w-6xl px-4 py-12">
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-5">
          <div className="lg:col-span-1">
            <div className="flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 font-bold text-white">
                U
              </div>
              <span className="text-lg font-bold text-white">UPrep</span>
            </div>
            <p className="mt-3 text-sm text-slate-400">
              India&rsquo;s first integrated learning platform for students.
            </p>
            <div className="mt-4 text-sm text-slate-400">
              <div>040-48215405</div>
              <div>info@uprep.in</div>
            </div>
          </div>
          {cols.map((c) => (
            <div key={c.title}>
              <h4 className="text-sm font-semibold uppercase tracking-wide text-white">
                {c.title}
              </h4>
              <ul className="mt-3 space-y-2 text-sm">
                {c.links.map((l) => (
                  <li key={l.label}>
                    <Link href={l.href} className="hover:text-white">
                      {l.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="mt-10 border-t border-slate-800 pt-6 text-center text-xs text-slate-500">
          © {new Date().getFullYear()} UPrep Learning India Pvt Ltd. All Rights
          Reserved.
        </div>
      </div>
    </footer>
  );
}
