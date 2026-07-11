"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getSession, clearSession, type UprepSession } from "@/lib/session";
// Native CMDS Tools screens (rebuilt from the legacy cmds-app, wired to the same
// backend/Mongo data).
type ToolLink = { label: string; href: string };
const TOOL_LINKS: ToolLink[] = [
  { label: "Organization Info", href: "/cmds/tools/organization" },
  { label: "Edit Academic Structure", href: "/cmds/tools/academic" },
  { label: "People Management", href: "/cmds/tools/people" },
  { label: "Boards & Courses", href: "/cmds/tools/boards" },
  { label: "Schedule / Classroom", href: "/cmds/tools/schedule" },
  { label: "Device Management", href: "/cmds/tools/devices" },
  { label: "Challenge Channels", href: "/cmds/tools/channels" },
  { label: "Send Notification", href: "/cmds/tools/notifications" },
  { label: "News Feed", href: "/cmds/tools/news" },
  { label: "Referrals", href: "/cmds/tools/referrals" },
  { label: "External Signup", href: "/cmds/tools/signup" },
  { label: "Exports / SD Cards", href: "/cmds/tools/exports" },
];

// Exact replica of the legacy CMDS chrome: black title bar + secondary nav
// (Resources / Programs / Tools / Learning Network), with a left "Institute
// Resources" + Subjects rail rendered by the page.
export default function CmdsShell({
  children,
  active = "resources",
}: {
  children: React.ReactNode;
  active?: "resources" | "programs";
}) {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [userMenu, setUserMenu] = useState(false);
  const [toolsOpen, setToolsOpen] = useState(false);
  const toolsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
  }, [router]);

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (toolsRef.current && !toolsRef.current.contains(e.target as Node)) setToolsOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  function logout() {
    clearSession();
    router.replace("/login");
  }

  return (
    <div className="min-h-screen bg-white text-[#333]">
      {/* Black title bar */}
      <div className="flex h-9 items-center justify-center bg-[#1a1a1a] px-4 text-white">
        <div className="text-sm">
          <span className="font-semibold tracking-wide text-[#e8443b]">CMDS</span>
          <span className="text-slate-300"> - Content Management and Distribution System</span>
        </div>
        <div className="absolute right-4">
          <button
            onClick={() => setUserMenu((o) => !o)}
            className="flex items-center gap-2 text-xs text-slate-200 hover:text-white"
          >
            {session?.firstName || "UPrep"} {session?.lastName || "Admin"}
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-slate-600 text-[10px]">
              {(session?.firstName || "U").charAt(0)}
            </span>
            <span className="text-slate-400">▾</span>
          </button>
          {userMenu && (
            <div className="absolute right-0 z-40 mt-1 w-36 rounded-md border border-slate-200 bg-white py-1 text-xs text-slate-700 shadow-lg">
              <Link href="/learn/library" className="block px-3 py-2 hover:bg-slate-50">
                Learn app
              </Link>
              <button onClick={logout} className="block w-full px-3 py-2 text-left hover:bg-slate-50">
                Logout
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Secondary nav */}
      <header className="flex h-11 items-center border-b border-slate-200 bg-white px-4">
        <Link href="/cmds" className="flex items-center gap-2">
          <span className="flex h-6 w-6 items-center justify-center rounded bg-[#e8443b] text-xs font-bold text-white">
            U
          </span>
        </Link>

        <nav className="mx-auto flex items-center gap-8 text-sm">
          <Link
            href="/cmds"
            className={
              active === "resources"
                ? "font-medium text-slate-900"
                : "text-slate-500 hover:text-slate-800"
            }
          >
            Resources
          </Link>
          <Link
            href="/cmds/programs"
            className={
              active === "programs"
                ? "font-medium text-slate-900"
                : "text-slate-500 hover:text-slate-800"
            }
          >
            Programs ▾
          </Link>
        </nav>

        <div className="flex items-center gap-4 text-sm">
          <div className="relative" ref={toolsRef}>
            <button
              onClick={() => setToolsOpen((o) => !o)}
              className="flex items-center gap-1 text-slate-500 hover:text-slate-800"
            >
              🛠 Tools <span className="text-slate-400">▾</span>
            </button>
            {toolsOpen && (
              <div className="absolute right-0 z-40 mt-2 w-56 rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg">
                {TOOL_LINKS.map((t) => (
                  <Link
                    key={t.href}
                    href={t.href}
                    onClick={() => setToolsOpen(false)}
                    className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
                  >
                    {t.label}
                  </Link>
                ))}
              </div>
            )}
          </div>

          <a
            href="/learn/library"
            className="flex items-center gap-1 rounded bg-emerald-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-600"
            title="Open the student Learning Network"
          >
            Learning Network »
          </a>
        </div>
      </header>

      {children}
    </div>
  );
}

// Left rail used by the Institute Resources screen (Subjects filter).
export function CmdsSubjectsRail({
  subject,
  onSubject,
}: {
  subject: string;
  onSubject: (s: string) => void;
}) {
  const subjects = ["All Subjects", "Physics"];
  return (
    <aside className="w-[150px] shrink-0 border-r border-slate-100 px-4 py-6">
      <div className="rounded border border-slate-200 px-3 py-1.5 text-sm text-slate-700">
        Institute Resources
      </div>
      <h3 className="mt-6 font-semibold text-slate-700">Subjects</h3>
      <div className="mt-2 space-y-1 text-sm">
        {subjects.map((s) => (
          <label key={s} className="flex cursor-pointer items-center gap-2 text-slate-600">
            <input
              type="radio"
              name="subject"
              checked={subject === s}
              onChange={() => onSubject(s)}
              className="accent-emerald-600"
            />
            {s}
          </label>
        ))}
      </div>
    </aside>
  );
}
