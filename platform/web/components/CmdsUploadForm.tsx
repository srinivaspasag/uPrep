"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";
import { parseVideoUrl } from "@/lib/video";

export default function CmdsUploadForm({
  kind,
  title,
  accept,
  hint,
}: {
  kind: "document" | "video";
  title: string;
  accept: string;
  hint: string;
}) {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string>("");
  // Videos can be added either by file upload or by external URL (YouTube/Vimeo).
  const [mode, setMode] = useState<"file" | "url">("file");
  const [videoUrl, setVideoUrl] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState(0);
  const [folderId, setFolderId] = useState<string | null>(null);
  const [folderName, setFolderName] = useState<string>("");
  // Subjects from the curriculum (Boards) tree — auto-populates the Subject
  // field like the legacy CMDS tagging widget did.
  const [subjects, setSubjects] = useState<string[]>([]);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setSession(getSession());
    // The current CMDS folder is passed via the URL (?folder=<id>&folderName=<name>)
    // so uploads land inside the folder the user was browsing.
    const sp = new URLSearchParams(window.location.search);
    setFolderId(sp.get("folder"));
    setFolderName(sp.get("folderName") || "");

    // Top-level nodes (no parent) are the subjects.
    fetch("/api/cmds/tools/boards")
      .then((r) => r.json())
      .then((d) => {
        const roots = (d.nodes || [])
          .filter((n: { parentId: string | null }) => !n.parentId)
          .map((n: { name: string }) => n.name)
          .filter(Boolean);
        setSubjects(Array.from(new Set<string>(roots)).sort());
      })
      .catch(() => setSubjects([]));
  }, []);

  // Where to return after upload/cancel — back into the folder if we came from one.
  const backHref = folderId
    ? `/cmds?folder=${encodeURIComponent(folderId)}&folderName=${encodeURIComponent(folderName)}`
    : "/cmds";

  function pick(f: File | null) {
    setFile(f);
    setError("");
    if (f && !name) setName(f.name.replace(/\.[^.]+$/, ""));
    if (f && kind === "video") setPreview(URL.createObjectURL(f));
    else setPreview("");
  }

  const parsedVideo = kind === "video" && mode === "url" ? parseVideoUrl(videoUrl) : null;

  async function submitUrl() {
    setError("");
    if (!name.trim()) return setError("Please enter a title.");
    if (!videoUrl.trim()) return setError("Please paste a video URL.");
    if (!parsedVideo)
      return setError("Unsupported URL. Paste a YouTube or Vimeo link.");

    setSaving(true);
    try {
      const res = await fetch("/api/cmds/videos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          subject: subject.trim(),
          userId: session?.id || "",
          folderId: folderId || undefined,
          url: videoUrl.trim(),
        }),
      });
      if (res.ok) {
        router.push(backHref);
      } else {
        const data = await res.json().catch(() => ({}));
        setError(data.error || "Failed to add video");
      }
    } catch {
      setError("Network error while adding video");
    } finally {
      setSaving(false);
    }
  }

  function submit() {
    if (kind === "video" && mode === "url") return submitUrl();
    setError("");
    if (!name.trim()) return setError("Please enter a title.");
    if (!file) return setError("Please choose a file.");

    setSaving(true);
    setProgress(0);
    const form = new FormData();
    form.append("kind", kind);
    form.append("name", name.trim());
    form.append("subject", subject.trim());
    form.append("userId", session?.id || "");
    if (folderId) form.append("folderId", folderId);
    form.append("file", file);

    // XHR for upload progress (fetch has no progress event).
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/api/cmds/upload");
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) setProgress(Math.round((e.loaded / e.total) * 100));
    };
    xhr.onload = () => {
      setSaving(false);
      if (xhr.status >= 200 && xhr.status < 300) {
        router.push(backHref);
      } else {
        try {
          setError(JSON.parse(xhr.responseText).error || "Upload failed");
        } catch {
          setError("Upload failed");
        }
      }
    };
    xhr.onerror = () => {
      setSaving(false);
      setError("Network error during upload");
    };
    xhr.send(form);
  }

  return (
    <CmdsShell active="resources">
      <div className="mx-auto max-w-[640px] px-6 py-8">
        <div className="mb-4 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          / <span className="text-slate-600">{title}</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">{title}</h1>

        <div className="mt-6 space-y-5">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Title</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder={`${title} title`}
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600">Subject</label>
            <input
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              list="cmds-subjects"
              className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder={subjects.length ? "Select or type a subject" : "e.g. Physics (optional)"}
            />
            <datalist id="cmds-subjects">
              {subjects.map((s) => (
                <option key={s} value={s} />
              ))}
            </datalist>
          </div>

          {kind === "video" && (
            <div className="flex gap-1 rounded-md bg-slate-100 p-1 text-sm">
              {(
                [
                  ["file", "Upload file"],
                  ["url", "Add by URL (YouTube / Vimeo)"],
                ] as const
              ).map(([m, label]) => (
                <button
                  key={m}
                  type="button"
                  onClick={() => {
                    setMode(m);
                    setError("");
                  }}
                  className={`flex-1 rounded px-3 py-1.5 font-medium transition ${
                    mode === m
                      ? "bg-white text-slate-800 shadow-sm"
                      : "text-slate-500 hover:text-slate-700"
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>
          )}

          {kind !== "video" || mode === "file" ? (
            <>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-600">File</label>
                <div
                  onClick={() => inputRef.current?.click()}
                  onDragOver={(e) => e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault();
                    pick(e.dataTransfer.files?.[0] || null);
                  }}
                  className="flex cursor-pointer flex-col items-center justify-center rounded-md border-2 border-dashed border-slate-300 py-10 text-center hover:border-emerald-400"
                >
                  <div className="text-3xl">{kind === "video" ? "🎬" : "📄"}</div>
                  <div className="mt-2 text-sm text-slate-600">
                    {file ? file.name : "Click or drag a file here"}
                  </div>
                  <div className="mt-1 text-xs text-slate-400">{hint}</div>
                  <input
                    ref={inputRef}
                    type="file"
                    accept={accept}
                    className="hidden"
                    onChange={(e) => pick(e.target.files?.[0] || null)}
                  />
                </div>
              </div>

              {preview && (
                <video
                  src={preview}
                  controls
                  className="w-full rounded-md border border-slate-200"
                />
              )}
            </>
          ) : (
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-600">Video URL</label>
              <input
                value={videoUrl}
                onChange={(e) => {
                  setVideoUrl(e.target.value);
                  setError("");
                }}
                className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
                placeholder="https://www.youtube.com/watch?v=… or https://vimeo.com/…"
              />
              <div className="mt-1 text-xs text-slate-400">
                Paste a YouTube or Vimeo link — it plays inline in the learn app.
              </div>
              {videoUrl.trim() && !parsedVideo && (
                <div className="mt-2 text-xs text-amber-600">
                  Not a recognized YouTube or Vimeo URL yet.
                </div>
              )}
              {parsedVideo && (
                <div className="mt-3 aspect-video w-full overflow-hidden rounded-md border border-slate-200">
                  <iframe
                    src={parsedVideo.embedUrl}
                    className="h-full w-full"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    title="Video preview"
                  />
                </div>
              )}
            </div>
          )}

          {saving && !(kind === "video" && mode === "url") && (
            <div className="h-2 w-full overflow-hidden rounded bg-slate-100">
              <div
                className="h-full bg-emerald-500 transition-all"
                style={{ width: `${progress}%` }}
              />
            </div>
          )}

          {error && <div className="text-sm text-red-600">{error}</div>}

          <div className="flex gap-3 pt-2">
            <button
              onClick={submit}
              disabled={saving}
              className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              {saving
              ? kind === "video" && mode === "url"
                ? "Saving…"
                : `Uploading ${progress}%`
              : `Save ${title.replace("Add a ", "")}`}
            </button>
            <Link
              href={backHref}
              className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100"
            >
              Cancel
            </Link>
          </div>
        </div>
      </div>
    </CmdsShell>
  );
}
