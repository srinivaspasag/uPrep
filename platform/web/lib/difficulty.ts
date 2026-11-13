// The legacy content service (Play/Morphia) maps a question's `difficulty`
// onto its Java enum Difficulty { UNKNOWN, EASY, MODERATE, TOUGH }
// (legacy/lms-master/content/content-commons/.../enums/Difficulty.java). Any
// other string makes getTestQuestions throw "No enum constant ...MEDIUM" and
// return an HTML 500 page — found live: Bulk Import saved "MEDIUM", and every
// student opening WT-17 got "Unexpected token '<' ... is not valid JSON".
// Every write of a question's difficulty goes through this.
export const DIFFICULTIES = ["EASY", "MODERATE", "TOUGH"] as const;

export function normalizeDifficulty(raw: unknown, fallback = "EASY"): string {
  const v = String(raw ?? "").trim().toUpperCase();
  if (!v) return fallback;
  if (v === "EASY") return "EASY";
  if (v === "MODERATE" || v === "MEDIUM") return "MODERATE";
  if (v === "TOUGH" || v === "HARD") return "TOUGH";
  return "UNKNOWN";
}
