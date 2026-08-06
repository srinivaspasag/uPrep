"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

// The Instant Test Generator is now the "auto-generate" branch of the
// single merged Create Test flow at /cmds/tests/new (see that file's
// header comment) — this route stays only so old bookmarks/links don't
// 404, and forwards straight into that flow with folder context preserved.
export default function AutoGenerateTestRedirect() {
  const router = useRouter();
  useEffect(() => {
    const sp = new URLSearchParams(window.location.search);
    sp.set("mode", "auto");
    router.replace(`/cmds/tests/new?${sp.toString()}`);
  }, [router]);
  return null;
}
