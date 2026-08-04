// Legacy's real Create Test form has a required "Code" field distinct from
// Name (see QrTests' createTest.html) — this derives a reasonable default
// from the test name so the admin isn't forced to fill in a second field by
// hand, while staying editable to match legacy's UI.
export function slugifyCode(name: string): string {
  return name
    .trim()
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}
