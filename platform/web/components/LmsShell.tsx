"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { getSession, clearSession, type UprepSession } from "@/lib/session";

export type LmsNavKey =
  | "library"
  | "programs"
  | "assignments"
  | "doubts"
  | "analytics"
  | "activity"
  | "leaderboard"
  | "challenges"
  | "messages"
  | "playlists"
  | "certificates";

const NAV: { key: LmsNavKey; label: string; href: string }[] = [
  { key: "library", label: "DIGITAL LIBRARY", href: "/learn/library" },
  { key: "programs", label: "PROGRAMS", href: "/learn/programs" },
  { key: "assignments", label: "ASSIGNMENTS", href: "/learn/assignments" },
  { key: "doubts", label: "DOUBTS FORUM", href: "/learn/doubts" },
  { key: "analytics", label: "ANALYTICS", href: "/learn/analytics" },
  { key: "activity", label: "RECENT ACTIVITY", href: "/learn/activity" },
  { key: "leaderboard", label: "LEADERBOARD", href: "/learn/leaderboard" },
  { key: "challenges", label: "CHALLENGES", href: "/learn/challenges" },
  { key: "messages", label: "MESSAGES", href: "/learn/messages" },
  { key: "playlists", label: "PLAYLISTS", href: "/learn/playlists" },
  { key: "certificates", label: "CERTIFICATES", href: "/learn/certificates" },
];

export default function LmsShell({
  active,
  children,
}: {
  active: LmsNavKey;
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [hasUnread, setHasUnread] = useState(false);

  useEffect(() => {
    const s = getSession();
    if (!s) {
      router.replace("/login");
      return;
    }
    setSession(s);
  }, [router, pathname]);

  useEffect(() => {
    let active = true;
    fetch("/api/learn/notifications")
      .then((r) => r.json())
      .then((d) => {
        if (!active) return;
        const newest = (d.items || [])[0]?.sentAt || 0;
        const seen = Number(sessionStorage.getItem("uprep_notif_seen") || 0);
        setHasUnread(newest > seen);
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, [pathname]);

  function logout() {
    clearSession();
    router.replace("/login");
  }

  return (
    <div className="min-h-screen bg-white text-[#333]">
      {/* Top header */}
      <header className="sticky top-0 z-30 flex h-[52px] items-center justify-between border-b border-slate-200 bg-white px-5 shadow-sm">
        <Link href="/learn/library" className="flex items-center gap-2">
          <Image
            src="/legacy/logo.png"
            alt="UPrep Learning"
            width={130}
            height={30}
            className="h-[30px] w-auto object-contain"
            priority
          />
        </Link>

        <div className="flex items-center gap-3">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              const q = (new FormData(e.currentTarget).get("q") as string) || "";
              if (q.trim()) router.push(`/learn/search?q=${encodeURIComponent(q.trim())}`);
            }}
            className="hidden md:block"
          >
            <input
              name="q"
              placeholder="Search…"
              className="w-40 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-600 outline-none focus:w-56 focus:border-emerald-400 focus:bg-white"
            />
          </form>

          <Link
            href="/learn/bookmarks"
            className="flex h-8 w-8 items-center justify-center rounded-full text-slate-500 hover:bg-slate-100 hover:text-slate-700"
            title="Bookmarks"
          >
            <span className="text-base">☆</span>
          </Link>

          <Link
            href="/cmds"
            className="flex items-center gap-1.5 rounded-md border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:border-emerald-400 hover:text-emerald-700"
            title="Content Management & Distribution System"
          >
            <span className="text-[13px]">🛠</span> CMDS Console
          </Link>

          <Link
            href="/learn/notifications"
            className="relative flex h-8 w-8 items-center justify-center rounded-full text-slate-500 hover:bg-slate-100 hover:text-slate-700"
            title="Notifications"
          >
            <span className="text-base">🔔</span>
            {hasUnread && (
              <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-emerald-500 ring-2 ring-white" />
            )}
          </Link>

        <div className="relative">
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 text-sm text-slate-600 hover:text-slate-900"
          >
            <span className="flex h-7 w-7 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-600">
              {(session?.firstName || "U").charAt(0).toUpperCase()}
            </span>
            <span className="hidden sm:inline">
              {session?.firstName} {session?.lastName}
            </span>
            <span className="text-slate-400">▾</span>
          </button>
          {menuOpen && (
            <div className="absolute right-0 mt-2 w-40 overflow-hidden rounded-md border border-slate-200 bg-white py-1 text-sm shadow-lg">
              <Link
                href="/learn/profile"
                className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
              >
                Profile & Settings
              </Link>
              <Link
                href="/cmds"
                className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
              >
                CMDS Console
              </Link>
              <Link
                href="/learn/news"
                className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
              >
                News & Announcements
              </Link>
              <Link
                href="/help"
                className="block px-4 py-2 text-slate-600 hover:bg-slate-50"
              >
                Help Center
              </Link>
              <button
                onClick={logout}
                className="block w-full px-4 py-2 text-left text-slate-600 hover:bg-slate-50"
              >
                Logout
              </button>
            </div>
          )}
          </div>
        </div>
      </header>

      <div className="mx-auto flex max-w-[1100px]">
        {/* Left sidebar nav */}
        <aside className="w-[210px] shrink-0 border-r border-slate-100 py-6">
          <nav className="flex flex-col">
            {NAV.map((item) => {
              const isActive = item.key === active;
              return (
                <Link
                  key={item.key}
                  href={item.href}
                  className={`border-l-[3px] px-5 py-3 text-[13px] tracking-wide transition ${
                    isActive
                      ? "border-emerald-500 font-semibold text-slate-900"
                      : "border-transparent text-slate-400 hover:text-slate-700"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </aside>

        {/* Main content */}
        <main className="min-h-[calc(100vh-52px)] flex-1 px-8 py-6">
          {children}
        </main>
      </div>
    </div>
  );
}

// Shared hand-drawn empty-state block matching the legacy "zero level" screens.
export function ZeroState({
  img,
  title,
  children,
}: {
  img: string;
  title?: string;
  children?: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img src={img} alt={title || "Nothing here yet"} className="max-w-[420px] opacity-90" />
      {children && <div className="mt-4 text-slate-500">{children}</div>}
    </div>
  );
}
