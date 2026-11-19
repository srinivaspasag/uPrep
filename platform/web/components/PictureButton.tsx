"use client";

import { useRef, useState, type ClipboardEvent, type RefObject } from "react";

// "Add picture" for question text, options and solutions. Uploads to
// /api/cmds/questions/image and inserts `![picture](/uploads/…)` at the
// cursor (components/MathText renders it). Pasting a screenshot straight into
// the field does the same via pastePicture().

type Field = HTMLInputElement | HTMLTextAreaElement;

export async function uploadPicture(file: File): Promise<string> {
  const form = new FormData();
  form.append("file", file);
  const r = await fetch("/api/cmds/questions/image", { method: "POST", body: form });
  const d = await r.json().catch(() => ({}));
  if (!r.ok || !d.token) throw new Error(d.error || "Upload failed");
  return d.token as string;
}

function insertAt(el: Field | null, value: string, snippet: string): string {
  let start = el?.selectionStart ?? value.length;
  let end = el?.selectionEnd ?? value.length;
  // Never clicked into the box (cursor still at 0) — add after the text,
  // which is almost always where a picture belongs.
  if (start === 0 && end === 0) start = end = value.length;
  const before = value.slice(0, start);
  const after = value.slice(end);
  const lead = before && !/\s$/.test(before) ? " " : "";
  const trail = after && !/^\s/.test(after) ? " " : "";
  return before + lead + snippet + trail + after;
}

// onPaste handler: if the clipboard holds a picture, upload it and insert it.
export function pastePicture(
  e: ClipboardEvent<Field>,
  value: string,
  onChange: (v: string) => void,
  onError?: (msg: string) => void
) {
  const file = Array.from(e.clipboardData?.files || []).find((f) => f.type.startsWith("image/"));
  if (!file) return;
  e.preventDefault();
  const el = e.currentTarget;
  uploadPicture(file)
    .then((token) => onChange(insertAt(el, value, token)))
    .catch((err) => onError?.(err.message));
}

export default function PictureButton({
  fieldRef,
  value,
  onChange,
  compact = false,
}: {
  fieldRef: RefObject<Field | null>;
  value: string;
  onChange: (v: string) => void;
  compact?: boolean;
}) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  async function pick(file: File) {
    setBusy(true);
    setErr("");
    try {
      const token = await uploadPicture(file);
      onChange(insertAt(fieldRef.current, value, token));
    } catch (e: any) {
      setErr(e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <span className="inline-flex items-center gap-2">
      <button
        type="button"
        onClick={() => fileRef.current?.click()}
        disabled={busy}
        title="Add a picture (or paste a screenshot straight into the box)"
        className="rounded-md border border-slate-300 px-2 py-1 text-xs text-slate-600 hover:bg-slate-100 disabled:opacity-60"
      >
        {busy ? "Uploading…" : compact ? "🖼" : "🖼 Add picture"}
      </button>
      <input
        ref={fileRef}
        type="file"
        accept="image/png,image/jpeg,image/gif,image/webp"
        className="hidden"
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) pick(f);
          e.target.value = "";
        }}
      />
      {err && <span className="text-xs text-red-600">{err}</span>}
    </span>
  );
}
