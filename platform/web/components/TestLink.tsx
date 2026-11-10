"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import type { ComponentProps, MouseEvent } from "react";
import { withReturn } from "@/lib/returnTo";

// Drop-in for <Link> on anything that may open a test. For /test/ links it
// appends ?from=<current page> at click time, so the test page can send the
// student back where they came from. Other hrefs behave exactly like <Link>.
export default function TestLink(props: ComponentProps<typeof Link>) {
  const router = useRouter();
  const { href, onClick, target } = props;
  const isTest = typeof href === "string" && href.startsWith("/test/");

  function handleClick(e: MouseEvent<HTMLAnchorElement>) {
    onClick?.(e);
    if (!isTest || e.defaultPrevented || target === "_blank") return;
    // Let modified clicks (new tab/window) fall through to the plain href.
    if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    e.preventDefault();
    router.push(withReturn(href as string, window.location.pathname + window.location.search));
  }

  return <Link {...props} onClick={handleClick} />;
}
