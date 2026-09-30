// Shared slugify — used both when auto-generating a slug from an org's name
// on creation, and when an admin sets one explicitly. Keep in sync with
// whatever the org-facing URL (/i/[slug]) and the slug-edit form accept.
export function slugify(input: string): string {
  return input
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 60);
}
