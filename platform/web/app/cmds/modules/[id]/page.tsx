"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";

type Item = {
  id: string;
  name: string;
  type: string;
  url?: string | null;
  embedUrl?: string | null;
  qusCount?: number;
};
type ModuleMeta = { id: string; name: string; subject: string | null };
type SessionGroup = { name: string; items: Item[] };

const TYPE_ICON: Record<string, string> = {
  DOCUMENT: "📄",
  VIDEO: "🎬",
  TEST: "📕",
  QUESTION_SET: "🟦",
};

export default function ModuleDetailPage() {
  const params = useParams();
  const id = String(params.id);
  const [mod, setMod] = useState<ModuleMeta | null>(null);
  const [items, setItems] = useState<Item[]>([]);
  const [groupedItems, setGroupedItems] = useState<SessionGroup[]>([]);
  const [missingCount, setMissingCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [preview, setPreview] = useState<Item | null>(null);
  const [nextUp, setNextUp] = useState<Item | null>(null);

  // Module order is mandatory (see ModuleForm) so the module can enforce a
  // sequence: closing a video — whether it just finished playing or was
  // manually closed — offers the next item in that order instead of just
  // dropping back to the plain list.
  function finishPreview() {
    const closedId = preview?.id;
    setPreview(null);
    if (!closedId) return;
    const idx = items.findIndex((it) => it.id === closedId);
    const next = idx >= 0 ? items[idx + 1] : undefined;
    if (next) setNextUp(next);
  }

  useEffect(() => {
    fetch(`/api/cmds/modules/${id}`)
      .then((r) => r.json())
      .then((d) => {
        if (d.error) setError(d.error);
        setMod(d.module || null);
        setItems(d.items || []);
        setGroupedItems(d.groupedItems || []);
        setMissingCount(d.missingCount || 0);
      })
      .finally(() => setLoading(false));
  }, [id]);

  return (
    <CmdsShell active="resources">
      <main className="mx-auto max-w-4xl px-4 py-8">
        <div className="mb-2 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          / <span className="text-slate-600">{mod?.name || "Module"}</span>
        </div>
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold text-slate-800">{mod?.name || "Module"}</h1>
          {mod && (
            <Link
              href={`/cmds/modules/${id}/edit`}
              className="rounded border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              ✎ Edit
            </Link>
          )}
        </div>
        {mod?.subject && <p className="mt-1 text-sm text-slate-500">{mod.subject}</p>}

        {error && (
          <div className="mt-6 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">{error}</div>
        )}

        <div className="mt-6">
          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading…</div>
          ) : items.length === 0 ? (
            <div className="rounded-xl bg-white p-8 text-center text-slate-400 ring-1 ring-black/5">
              No content in this module yet.
            </div>
          ) : groupedItems.length > 0 ? (
            <div className="space-y-6">
              {groupedItems.map((g, gi) => (
                <div key={`${g.name}-${gi}`}>
                  <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">{g.name}</h2>
                  <div className="space-y-2">
                    {g.items.map((it) => (
                      <ModuleItemRow key={it.id} item={it} onPreview={setPreview} />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="space-y-2">
              {items.map((it) => (
                <ModuleItemRow key={it.id} item={it} onPreview={setPreview} />
              ))}
            </div>
          )}
          {missingCount > 0 && (
            <p className="mt-3 text-xs text-slate-400">
              {missingCount} item{missingCount === 1 ? "" : "s"} in this module could not be found (likely deleted
              since being added).
            </p>
          )}
        </div>
      </main>
      {preview && <VideoModal item={preview} onClose={finishPreview} />}
      {nextUp && (
        <NextUpModal
          item={nextUp}
          onOpen={() => {
            const item = nextUp;
            setNextUp(null);
            if (item.type === "VIDEO") setPreview(item);
          }}
          onDismiss={() => setNextUp(null)}
        />
      )}
    </CmdsShell>
  );
}

function ModuleItemRow({ item, onPreview }: { item: Item; onPreview: (i: Item) => void }) {
  const inner = (
    <div className="flex items-center gap-3 rounded-lg bg-white p-4 ring-1 ring-black/5 transition hover:shadow-sm">
      <span className="text-xl">{TYPE_ICON[item.type] || "📄"}</span>
      <div className="flex-1">
        <div className="font-medium text-slate-800">{item.name}</div>
        <div className="text-xs text-slate-400">
          {item.type}
          {item.qusCount ? ` · ${item.qusCount} questions` : ""}
        </div>
      </div>
    </div>
  );
  if (item.type === "VIDEO")
    return (
      <button onClick={() => onPreview(item)} className="block w-full text-left">
        {inner}
      </button>
    );
  if (item.type === "TEST") return <Link href={`/test/${item.id}`}>{inner}</Link>;
  if (item.type === "QUESTION_SET") return <Link href="/cmds/questions">{inner}</Link>;
  if (item.type === "DOCUMENT" && item.url) return <a href={item.url}>{inner}</a>;
  return inner;
}

function VideoModal({ item, onClose }: { item: Item; onClose: () => void }) {
  const src = item.embedUrl || item.url;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-6" onClick={onClose}>
      <div className="w-full max-w-3xl rounded-lg bg-white p-4 shadow-xl" onClick={(e) => e.stopPropagation()}>
        <div className="mb-3 flex items-center justify-between">
          <span className="font-medium text-slate-800">{item.name}</span>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
            ✕
          </button>
        </div>
        <div className="aspect-video w-full overflow-hidden rounded bg-black">
          {item.embedUrl ? (
            <iframe
              src={item.embedUrl}
              className="h-full w-full"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
              title={item.name}
            />
          ) : src ? (
            <video
              src={src}
              controls
              controlsList="nodownload"
              disablePictureInPicture
              onContextMenu={(e) => e.preventDefault()}
              onEnded={onClose}
              className="h-full w-full"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-slate-400">
              No playable source for this video.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// Prompted after a video is closed (finished or manually) when the module has
// a next item in its ordered sequence — module order is mandatory (see
// ModuleForm's up/down reordering) specifically so this can be meaningful.
function NextUpModal({ item, onOpen, onDismiss }: { item: Item; onOpen: () => void; onDismiss: () => void }) {
  const href =
    item.type === "TEST"
      ? `/test/${item.id}`
      : item.type === "QUESTION_SET"
      ? "/cmds/questions"
      : item.type === "DOCUMENT" && item.url
      ? item.url
      : null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-sm rounded-lg bg-white p-6 shadow-xl">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Up next</p>
        <div className="mt-2 flex items-center gap-3">
          <span className="text-xl">{TYPE_ICON[item.type] || "📄"}</span>
          <div>
            <div className="font-medium text-slate-800">{item.name}</div>
            <div className="text-xs text-slate-400">{item.type}</div>
          </div>
        </div>
        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onDismiss} className="rounded px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100">
            Not now
          </button>
          {item.type === "VIDEO" ? (
            <button
              onClick={onOpen}
              className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
            >
              Play
            </button>
          ) : href ? (
            <a
              href={href}
              onClick={onDismiss}
              className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
            >
              Open
            </a>
          ) : null}
        </div>
      </div>
    </div>
  );
}
