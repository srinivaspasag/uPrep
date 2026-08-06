"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { getSession, clearSession, type UprepSession } from "@/lib/session";
import ImpersonationBanner from "@/components/ImpersonationBanner";
import { isStaff } from "@/lib/roles";

export type LmsNavKey =
  | "courses"
  | "store"
  | "live"
  | "scheduled"
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

// Matches legacy's real sidebar exactly (ui/learn-app header.html's
// `<aside id="sidebar">` main-menu list + conf/messages TXT_* labels) — five
// items, no more: Library, Programs (student only), Doubts Forum (unless the
// org hides it), Analytics, Recent Activity. This rebuild had accumulated 14
// nav entries (Store, Live Classes, Scheduled Tests, Assignments,
// Leaderboard, Challenges, Messages, Playlists, Certificates) that don't
// exist in legacy's nav at all — their pages still exist and work, they're
// just not real top-level destinations, so they're removed from here rather
// than deleted outright.
// One accent color per section (same "gradient family" system as the
// subject/content cards — see lib/subjectColors.ts) so the sidebar itself
// gives a quick "which part of the app am I in" cue instead of a single flat
// amber indicator for everything.
const NAV: { key: LmsNavKey; label: string; href: string; icon: string; active: string; dot: string }[] = [
  { key: "courses", label: "Digital Library", href: "/learn/courses", icon: "📚", active: "bg-blue-50 text-blue-800", dot: "bg-gradient-to-b from-blue-500 to-violet-500" },
  { key: "programs", label: "Programs", href: "/learn/programs", icon: "🎯", active: "bg-emerald-50 text-emerald-800", dot: "bg-gradient-to-b from-emerald-500 to-teal-500" },
  { key: "doubts", label: "Doubts Forum", href: "/learn/doubts", icon: "💬", active: "bg-rose-50 text-rose-800", dot: "bg-gradient-to-b from-rose-500 to-red-500" },
  { key: "analytics", label: "Analytics", href: "/learn/analytics", icon: "📊", active: "bg-violet-50 text-violet-800", dot: "bg-gradient-to-b from-violet-500 to-purple-500" },
  { key: "activity", label: "Recent Activity", href: "/learn/activity", icon: "🕒", active: "bg-amber-50 text-amber-800", dot: "bg-gradient-to-b from-amber-500 to-orange-500" },
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
    <div className="min-h-screen bg-[#EDEEE9] text-[#16233D]">
      <ImpersonationBanner />
      {/* Top header — a thin multi-hue underline (echoing the subject
          palette) replaces the flat grey border, so even the chrome that's
          visible on every single page carries a little energy. */}
      <header className="sticky top-0 z-30 bg-white px-5 shadow-sm">
        <div className="flex h-[52px] items-center justify-between">
        <Link href="/learn/courses" className="flex items-center gap-2">
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
            <div className="relative">
              <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-[#8890A1]">🔍</span>
              <input
                name="q"
                placeholder="Search…"
                className="w-40 rounded-full border border-[#D9D6C9] bg-[#EDEEE9] py-1.5 pl-8 pr-3 text-xs text-[#3E4A63] outline-none transition focus:w-56 focus:border-amber-500 focus:bg-white"
              />
            </div>
          </form>

          <Link
            href="/learn/bookmarks"
            className="flex h-8 w-8 items-center justify-center rounded-full text-[#8890A1] transition hover:bg-amber-50 hover:text-amber-600"
            title="Bookmarks"
          >
            <span className="text-base">☆</span>
          </Link>

          {isStaff(session?.profile) && (
            <Link
              href="/cmds"
              className="flex items-center gap-1.5 rounded-full border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 transition hover:border-emerald-400 hover:text-emerald-700"
              title="Content Management & Distribution System"
            >
              <span className="text-[13px]">🛠</span> CMDS Console
            </Link>
          )}

          <Link
            href="/learn/notifications"
            className="relative flex h-8 w-8 items-center justify-center rounded-full text-[#8890A1] transition hover:bg-rose-50 hover:text-rose-600"
            title="Notifications"
          >
            <span className="text-base">🔔</span>
            {hasUnread && (
              <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-rose-500 ring-2 ring-white" />
            )}
          </Link>

        <div className="relative">
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 rounded-full py-1 pl-1 pr-2 text-sm text-[#3E4A63] transition hover:bg-[#F8F7F3] hover:text-[#16233D]"
          >
            <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 text-xs font-semibold text-white shadow-sm">
              {(session?.firstName || "U").charAt(0).toUpperCase()}
            </span>
            <span className="hidden sm:inline">
              {session?.firstName} {session?.lastName}
            </span>
            <span className="text-[#8890A1]">▾</span>
          </button>
          {menuOpen && (
            <div className="absolute right-0 mt-2 w-44 overflow-hidden rounded-xl border border-[#D9D6C9] bg-white py-1 text-sm shadow-lg">
              <Link
                href="/learn/profile"
                className="flex items-center gap-2 px-4 py-2 text-[#3E4A63] hover:bg-[#F8F7F3]"
              >
                👤 Profile & Settings
              </Link>
              {isStaff(session?.profile) && (
                <Link
                  href="/cmds"
                  className="flex items-center gap-2 px-4 py-2 text-[#3E4A63] hover:bg-[#F8F7F3]"
                >
                  🛠 CMDS Console
                </Link>
              )}
              <Link
                href="/learn/news"
                className="flex items-center gap-2 px-4 py-2 text-[#3E4A63] hover:bg-[#F8F7F3]"
              >
                📣 News & Announcements
              </Link>
              <Link
                href="/help"
                className="flex items-center gap-2 px-4 py-2 text-[#3E4A63] hover:bg-[#F8F7F3]"
              >
                ❓ Help Center
              </Link>
              <button
                onClick={logout}
                className="flex w-full items-center gap-2 px-4 py-2 text-left text-[#3E4A63] hover:bg-[#F8F7F3]"
              >
                🚪 Logout
              </button>
            </div>
          )}
          </div>
        </div>
        </div>
        <div className="h-[3px] bg-gradient-to-r from-blue-500 via-violet-500 to-amber-500" />
      </header>

      <div className="mx-auto flex max-w-[1100px]">
        {/* Left sidebar nav */}
        <aside className="w-[220px] shrink-0 border-r border-[#D9D6C9] bg-white py-5">
          {session && (
            <div className="mx-3 mb-4 rounded-xl bg-gradient-to-br from-[#16233D] to-[#2A3B5C] px-4 py-3.5 text-white">
              <div className="text-[11px] uppercase tracking-wide text-white/60">Welcome back</div>
              <div className="mt-0.5 truncate font-serif text-[15px] font-semibold">
                {session.firstName} 👋
              </div>
            </div>
          )}
          <nav className="flex flex-col gap-1 px-3">
            {NAV.map((item) => {
              const isActive = item.key === active;
              return (
                <Link
                  key={item.key}
                  href={item.href}
                  className={`group flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-[13px] font-medium tracking-wide transition ${
                    isActive
                      ? `${item.active} shadow-sm`
                      : "text-[#8890A1] hover:translate-x-0.5 hover:bg-[#F8F7F3] hover:text-[#16233D]"
                  }`}
                >
                  <span
                    className={`h-5 w-1 shrink-0 rounded-full transition ${isActive ? item.dot : "bg-transparent"}`}
                  />
                  <span className="text-base leading-none">{item.icon}</span>
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

// Empty-state block. Used to be legacy's dated hand-drawn "Sorry! No
// content to show.." doodle jpgs — replaced with an on-brand icon-in-a-
// gradient-circle treatment matching the rest of the redesign (same soft
// multi-hue wash as the page heroes) instead of a static illustration that
// reads as a broken/unfinished screen.
export function ZeroState({
  icon = "📭",
  title = "Nothing here yet",
  children,
}: {
  icon?: string;
  title?: string;
  children?: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-blue-100 via-violet-100 to-rose-100">
        <span className="text-3xl">{icon}</span>
      </div>
      <div className="mt-5 font-serif text-lg font-semibold text-[#16233D]">{title}</div>
      {children && <div className="mt-2 max-w-sm text-sm text-[#8890A1]">{children}</div>}
    </div>
  );
}
