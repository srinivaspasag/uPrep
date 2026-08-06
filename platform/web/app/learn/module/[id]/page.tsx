"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import LmsShell from "@/components/LmsShell";

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

// Student-facing module viewer — matches legacy's real "modulePage.html"
// (a session-grouped timeline of clickable Test/Document/Video rows). This
// didn't exist at all before: a module could be assigned to a student and
// show up as a card in "Other Shared Content", but there was nowhere to
// actually open one — see app/api/learn/modules/[id]/route.ts.
export default function StudentModulePage() {
  const params = useParams();
  const id = String(params.id);
  const [mod, setMod] = useState<ModuleMeta | null>(null);
  const [groupedItems, setGroupedItems] = useState<SessionGroup[]>([]);
  const [flatItems, setFlatItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [playing, setPlaying] = useState<Item | null>(null);

  useEffect(() => {
    fetch(`/api/learn/modules/${id}`)
      .then((r) => r.json())
      .then((d) => {
        if (d.error) {
          setError(d.error);
          return;
        }
        setMod(d.module);
        setGroupedItems(d.groupedItems || []);
        setFlatItems(d.items || []);
      })
      .catch(() => setError("Failed to load module"))
      .finally(() => setLoading(false));
  }, [id]);

  const sections: SessionGroup[] = groupedItems.length > 0 ? groupedItems : [{ name: "", items: flatItems }];

  return (
    <LmsShell active="courses">
      <div className="mb-4 text-sm text-[#8890A1]">
        <Link href="/learn/courses" className="hover:text-amber-700">
          Digital Library
        </Link>
      </div>

      {loading ? (
        <div className="py-16 text-center text-[#8890A1]">Loading…</div>
      ) : error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      ) : (
        <>
          <h1 className="font-serif text-2xl font-semibold text-[#16233D]">{mod?.name}</h1>
          {mod?.subject && <p className="mt-1 text-sm text-[#8890A1]">{mod.subject}</p>}

          <div className="mt-6 space-y-8">
            {sections.map((sec, si) => (
              <div key={si}>
                {sec.name && (
                  <h2 className="mb-3 text-xs font-semibold uppercase tracking-wide text-[#8890A1]">{sec.name}</h2>
                )}
                <div className="overflow-hidden rounded-lg border border-[#D9D6C9] bg-white">
                  {sec.items.length === 0 ? (
                    <div className="px-4 py-6 text-center text-sm text-[#8890A1]">Nothing in this session.</div>
                  ) : (
                    sec.items.map((it, i) => <ModuleItemRow key={it.id} item={it} isLast={i === sec.items.length - 1} onPlay={() => setPlaying(it)} />)
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {playing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4" onClick={() => setPlaying(null)}>
          <div className="w-full max-w-3xl" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between pb-2">
              <span className="truncate pr-4 text-sm font-medium text-white">{playing.name}</span>
              <button onClick={() => setPlaying(null)} className="text-2xl leading-none text-white hover:text-slate-300">
                ×
              </button>
            </div>
            <div className="aspect-video w-full overflow-hidden rounded bg-black">
              {playing.embedUrl ? (
                <iframe
                  src={playing.embedUrl}
                  className="h-full w-full"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                  title={playing.name}
                />
              ) : playing.url ? (
                <video
                  src={playing.url}
                  controls
                  autoPlay
                  controlsList="nodownload"
                  disablePictureInPicture
                  onContextMenu={(e) => e.preventDefault()}
                  className="h-full w-full"
                />
              ) : null}
            </div>
          </div>
        </div>
      )}
    </LmsShell>
  );
}

function ModuleItemRow({ item, isLast, onPlay }: { item: Item; isLast: boolean; onPlay: () => void }) {
  const inner = (
    <div className={`flex items-center gap-3 px-4 py-3 hover:bg-[#F8F7F3] ${isLast ? "" : "border-b border-[#D9D6C9]"}`}>
      <span className="text-lg">{TYPE_ICON[item.type] || "📎"}</span>
      <span className="min-w-0 flex-1 truncate text-[#16233D]">{item.name}</span>
      {item.type === "TEST" && typeof item.qusCount === "number" && (
        <span className="shrink-0 text-xs text-[#8890A1]">{item.qusCount} questions</span>
      )}
    </div>
  );

  if (item.type === "TEST") return <Link href={`/test/${item.id}`}>{inner}</Link>;
  if (item.type === "VIDEO" && (item.url || item.embedUrl))
    return (
      <button onClick={onPlay} className="block w-full text-left">
        {inner}
      </button>
    );
  if (item.type === "DOCUMENT" && item.url)
    return (
      <a href={item.url} target="_blank" rel="noreferrer">
        {inner}
      </a>
    );
  return inner;
}
