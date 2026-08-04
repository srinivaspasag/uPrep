// Per-subject accent used across the student app's course cards — purely a
// scan aid (Physics blue, Chemistry teal, Maths violet, Biology green), kept
// separate from the app's own brand/accent color. Matched by name substring
// since course names are admin-entered free text ("Physics XI", "Physics
// XII", ...), not a fixed enum.
export type SubjectAccent = { border: string; text: string; chip: string; dot: string; header: string };

const PALETTE: Record<string, SubjectAccent> = {
  physics: { border: "border-blue-500", text: "text-blue-700", chip: "bg-blue-50", dot: "bg-blue-500", header: "bg-blue-600" },
  chemistry: { border: "border-teal-500", text: "text-teal-700", chip: "bg-teal-50", dot: "bg-teal-500", header: "bg-teal-600" },
  math: { border: "border-violet-500", text: "text-violet-700", chip: "bg-violet-50", dot: "bg-violet-500", header: "bg-violet-600" },
  bio: { border: "border-emerald-500", text: "text-emerald-700", chip: "bg-emerald-50", dot: "bg-emerald-500", header: "bg-emerald-600" },
};

const DEFAULT_ACCENT: SubjectAccent = {
  border: "border-amber-500",
  text: "text-amber-700",
  chip: "bg-amber-50",
  dot: "bg-amber-500",
  header: "bg-amber-600",
};

export function subjectAccent(name: string): SubjectAccent {
  const n = name.toLowerCase();
  if (n.includes("physic")) return PALETTE.physics;
  if (n.includes("chem")) return PALETTE.chemistry;
  if (n.includes("math")) return PALETTE.math;
  if (n.includes("bio") || n.includes("botany") || n.includes("zoolog")) return PALETTE.bio;
  return DEFAULT_ACCENT;
}
