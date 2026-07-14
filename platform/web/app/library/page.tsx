import { redirect } from "next/navigation";

// First-pass library screen is retired — the real Digital Library lives in the
// LMS shell at /learn/library.
export default function LibraryRedirect() {
  redirect("/learn/library");
}
