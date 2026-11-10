// Where a student goes back to after leaving a test (submit → analysis →
// "Back", Pause & exit, the logo). Found live: every exit was hard-coded to
// /learn/library, which redirects to the Digital Library root, so a student
// who opened a test from inside a subject/program folder, Tests, Search or
// Bookmarks was dumped on the subject list ("program files") instead of where
// they started. Links into a test now carry ?from=<page they were on>.

export const DEFAULT_RETURN = "/learn/courses";

// Only same-app student pages — never an absolute/protocol-relative URL, so
// ?from= can't be used as an open redirect.
export function safeReturnPath(raw: string | null | undefined): string {
  if (!raw) return DEFAULT_RETURN;
  if (!raw.startsWith("/learn/") || raw.startsWith("//") || raw.includes("\\")) return DEFAULT_RETURN;
  return raw;
}

// /test/<id> + ?from=<current page>. Called at click time so it captures
// in-page state kept in the URL (e.g. the Digital Library's ?path= folder).
export function withReturn(href: string, from: string): string {
  return `${href}${href.includes("?") ? "&" : "?"}from=${encodeURIComponent(from)}`;
}

export function returnLabel(path: string): string {
  const p = path.split("?")[0];
  if (p.startsWith("/learn/courses")) return path.includes("path=") ? "Back to Folder" : "Back to Library";
  if (p.startsWith("/learn/module")) return "Back to Module";
  if (p.startsWith("/learn/programs")) return "Back to Programs";
  if (p.startsWith("/learn/tests")) return "Back to Tests";
  if (p.startsWith("/learn/challenges")) return "Back to Challenges";
  if (p.startsWith("/learn/search")) return "Back to Search";
  if (p.startsWith("/learn/bookmarks")) return "Back to Bookmarks";
  if (p.startsWith("/learn/analytics")) return "Back to Analytics";
  if (p.startsWith("/learn/activity")) return "Back to Recent Activity";
  return "Back";
}
