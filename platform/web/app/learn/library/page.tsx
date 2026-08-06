"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

// "Digital Library" was merged into My Courses — it showed the exact same
// content as My Courses' new "Library" section, just as its own nav item.
// This route stays alive (redirecting) so old links/bookmarks still land
// somewhere real instead of a dead page.
export default function LibraryRedirect() {
  const router = useRouter();
  useEffect(() => {
    router.replace("/learn/courses");
  }, [router]);
  return null;
}
