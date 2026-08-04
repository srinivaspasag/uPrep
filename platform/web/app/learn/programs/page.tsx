"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LmsShell, { ZeroState } from "@/components/LmsShell";
import { subjectAccent } from "@/lib/subjectColors";

type Course = { id: string; name: string; chapterCount: number; folderCount: number };
type ProgramGroup = {
  id: string;
  name: string;
  courseIds: string[];
  centerName: string | null;
  sectionName: string | null;
};

// Mirrors legacy's real Program card (Institute.getMySections /
// categorySections.html): program name + center + batch — legacy shows no
// progress/chapter data on this screen either, so we don't fabricate any.
// The subject list here is the one addition, since it's real data we have
// and legacy's "Visit Library" popup led to a subject-organized library
// anyway (tags/institute/library/home.html's subjectBar).
export default function ProgramsPage() {
  const [programGroups, setProgramGroups] = useState<ProgramGroup[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/learn/courses")
      .then((r) => r.json())
      .then((d) => {
        setProgramGroups(d.programGroups || []);
        setCourses(d.courses || []);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <LmsShell active="programs">
      <div className="border-b-2 border-[#16233D] pb-3">
        <h1 className="font-serif text-2xl font-semibold text-[#16233D]">My Programs</h1>
      </div>

      <div className="mt-6">
        {loading ? (
          <div className="text-[#8890A1]">Loading…</div>
        ) : programGroups.length === 0 ? (
          <ZeroState img="/legacy/zero/general-no-content.jpg">
            You're not assigned to a program yet — check with your institute, or use an access code from{" "}
            <Link href="/learn/courses" className="text-amber-700 underline underline-offset-2">
              My Courses
            </Link>
            .
          </ZeroState>
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {programGroups.map((g) => (
              <ProgramCard key={g.id} group={g} courses={courses} />
            ))}
          </div>
        )}
      </div>
    </LmsShell>
  );
}

function ProgramCard({ group, courses }: { group: ProgramGroup; courses: Course[] }) {
  const groupCourses = courses.filter((c) => group.courseIds.includes(c.id));
  const totalChapters = groupCourses.reduce((sum, c) => sum + c.chapterCount, 0);

  return (
    <Link
      href="/learn/courses"
      className="block rounded-lg border border-[#D9D6C9] bg-white p-5 transition hover:-translate-y-0.5 hover:border-amber-400 hover:shadow-md"
    >
      <div className="font-serif text-lg font-semibold text-[#16233D]">{group.name}</div>
      {(group.centerName || group.sectionName) && (
        <div className="mt-1 text-xs text-[#8890A1]">
          {[group.centerName, group.sectionName].filter(Boolean).join(" · ")}
        </div>
      )}

      {groupCourses.length > 0 && (
        <div className="mt-4 flex flex-wrap gap-1.5">
          {groupCourses.map((c) => {
            const accent = subjectAccent(c.name);
            return (
              <span
                key={c.id}
                className={`rounded-full px-2.5 py-1 text-[11px] font-medium ${accent.chip} ${accent.text}`}
              >
                {c.name}
              </span>
            );
          })}
        </div>
      )}

      <div className="mt-4 flex items-center justify-between border-t border-[#EDEEE9] pt-3">
        <span className="text-xs text-[#8890A1]">
          {groupCourses.length} subject{groupCourses.length === 1 ? "" : "s"} · {totalChapters} chapters
        </span>
        <span className="text-xs font-semibold text-amber-700">View courses →</span>
      </div>
    </Link>
  );
}
