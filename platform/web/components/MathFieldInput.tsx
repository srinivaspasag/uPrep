"use client";

import { useEffect, useRef } from "react";

// Thin wrapper around MathLive's <math-field> custom element. Built with
// direct DOM manipulation rather than JSX — mathlive doesn't ship React JSX
// typings for the element, and custom-element properties/events don't
// reliably round-trip through React's synthetic event system anyway, so a
// ref + native listeners is the standard safe pattern for this kind of
// wrapper.
export default function MathFieldInput({
  value,
  onChange,
  autoFocus,
}: {
  value: string;
  onChange: (latex: string) => void;
  autoFocus?: boolean;
}) {
  const containerRef = useRef<HTMLDivElement>(null);
  const fieldRef = useRef<any>(null);

  useEffect(() => {
    let cancelled = false;
    import("mathlive").then(() => {
      if (cancelled || !containerRef.current) return;
      const el = document.createElement("math-field") as any;
      el.value = value;
      el.style.width = "100%";
      el.style.minHeight = "56px";
      el.style.fontSize = "1.25rem";
      el.style.padding = "10px";
      el.style.border = "1px solid #cbd5e1";
      el.style.borderRadius = "6px";
      const handleInput = () => onChange(el.value);
      el.addEventListener("input", handleInput);
      containerRef.current.appendChild(el);
      fieldRef.current = el;
      if (autoFocus) el.focus();
      return () => el.removeEventListener("input", handleInput);
      // eslint-disable-next-line react-hooks/exhaustive-deps
    });
    return () => {
      cancelled = true;
      if (fieldRef.current) fieldRef.current.remove();
      fieldRef.current = null;
    };
    // Mount once — value updates after mount are one-directional from the
    // field's own input event, not re-pushed from the `value` prop, so
    // typing doesn't fight the cursor position.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div ref={containerRef} />;
}
