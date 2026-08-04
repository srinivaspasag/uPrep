// Per-subject accent used across the student app's course cards — purely a
// scan aid (Physics blue, Chemistry teal, Maths violet, Botany green,
// Zoology amber), kept separate from the app's own brand/accent color.
// Matched by name substring since course names are admin-entered free text
// ("Physics XI", "Physics XII", ...), not a fixed enum.
//
// Redesigned for more visual energy on the student-facing card grid
// (/learn/courses, /learn/programs): each subject now gets a two-stop
// gradient (`gradient`) for card headers instead of a flat fill — reads as
// livelier without tipping into neon, since the stops stay in the same
// hue family (e.g. blue -> indigo, not blue -> pink). Botany and Zoology
// used to share the exact same green and were indistinguishable at a
// glance in the grid; Zoology now gets its own warm amber/orange lane.
export type SubjectAccent = {
  border: string;
  text: string;
  chip: string;
  dot: string;
  header: string;
  gradient: string;
};

const PALETTE: Record<string, SubjectAccent> = {
  physics: {
    border: "border-blue-500",
    text: "text-blue-700",
    chip: "bg-blue-50",
    dot: "bg-blue-500",
    header: "bg-blue-600",
    gradient: "bg-gradient-to-br from-blue-500 to-indigo-600",
  },
  chemistry: {
    border: "border-teal-500",
    text: "text-teal-700",
    chip: "bg-teal-50",
    dot: "bg-teal-500",
    header: "bg-teal-600",
    gradient: "bg-gradient-to-br from-cyan-500 to-teal-600",
  },
  math: {
    border: "border-violet-500",
    text: "text-violet-700",
    chip: "bg-violet-50",
    dot: "bg-violet-500",
    header: "bg-violet-600",
    gradient: "bg-gradient-to-br from-violet-500 to-purple-600",
  },
  botany: {
    border: "border-emerald-500",
    text: "text-emerald-700",
    chip: "bg-emerald-50",
    dot: "bg-emerald-500",
    header: "bg-emerald-600",
    gradient: "bg-gradient-to-br from-lime-500 to-emerald-600",
  },
  zoology: {
    border: "border-orange-500",
    text: "text-orange-700",
    chip: "bg-orange-50",
    dot: "bg-orange-500",
    header: "bg-orange-600",
    gradient: "bg-gradient-to-br from-amber-500 to-orange-600",
  },
  bio: {
    border: "border-emerald-500",
    text: "text-emerald-700",
    chip: "bg-emerald-50",
    dot: "bg-emerald-500",
    header: "bg-emerald-600",
    gradient: "bg-gradient-to-br from-lime-500 to-emerald-600",
  },
};

const DEFAULT_ACCENT: SubjectAccent = {
  border: "border-rose-500",
  text: "text-rose-700",
  chip: "bg-rose-50",
  dot: "bg-rose-500",
  header: "bg-rose-600",
  gradient: "bg-gradient-to-br from-rose-500 to-pink-600",
};

export function subjectAccent(name: string): SubjectAccent {
  const n = name.toLowerCase();
  if (n.includes("physic")) return PALETTE.physics;
  if (n.includes("chem")) return PALETTE.chemistry;
  if (n.includes("math")) return PALETTE.math;
  if (n.includes("botany")) return PALETTE.botany;
  if (n.includes("zoolog")) return PALETTE.zoology;
  if (n.includes("bio")) return PALETTE.bio;
  return DEFAULT_ACCENT;
}
