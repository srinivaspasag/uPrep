"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Node = { id: string; name: string; type: string; parentId: string | null };

// Board Tree browser — proxied to the live legacy board-services backend
// (real subject/chapter data). Read-only for this pass: legacy's own
// "addBoards" action is unimplemented server-side (a TODO in the real
// source) — the real way legacy populates this tree is bulk Excel import,
// which for now is a one-time seed script rather than a UI action. This
// page mirrors legacy's own admin UX: drill down one level at a time,
// breadcrumb back up.
export default function BoardsPage() {
  const [parentId, setParentId] = useState<string | null>(null);
  const [breadcrumb, setBreadcrumb] = useState<{ id: string | null; name: string }[]>([
    { id: null, name: "Subjects" },
  ]);
  const [nodes, setNodes] = useState<Node[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const url = parentId ? `/api/cmds/tools/boards?parentId=${parentId}` : "/api/cmds/tools/boards";
    fetch(url)
      .then((r) => r.json())
      .then((d) => setNodes(d.nodes || []))
      .finally(() => setLoading(false));
  }, [parentId]);

  function enter(n: Node) {
    setBreadcrumb((prev) => [...prev, { id: n.id, name: n.name }]);
    setParentId(n.id);
  }
  function goTo(index: number) {
    setBreadcrumb((prev) => prev.slice(0, index + 1));
    setParentId(breadcrumb[index].id);
  }

  const atRoot = parentId === null;

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[820px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Boards & Course Management</h1>
        <p className="mt-1 text-sm text-slate-500">
          The Board Tree used to tag content and test questions — subjects at the top, chapters
          underneath. Backed by the same live service legacy used, so this is the real tree, not a
          separate copy.
        </p>

        <div className="mt-4 flex flex-wrap items-center gap-1 text-sm text-slate-500">
          {breadcrumb.map((b, i) => (
            <span key={i} className="flex items-center gap-1">
              {i > 0 && <span className="text-slate-300">›</span>}
              <button
                onClick={() => goTo(i)}
                className={i === breadcrumb.length - 1 ? "font-medium text-slate-800" : "hover:text-slate-800"}
              >
                {b.name}
              </button>
            </span>
          ))}
        </div>

        <div className="mt-3 overflow-hidden rounded border border-slate-200">
          {loading ? (
            <div className="py-16 text-center text-slate-400">Loading…</div>
          ) : nodes.length === 0 ? (
            <div className="py-16 text-center text-sm text-slate-400">
              {atRoot ? "No subjects in this org's tree yet." : "No chapters under this subject yet."}
            </div>
          ) : (
            <ul className="divide-y divide-slate-100">
              {nodes.map((n) => (
                <li key={n.id}>
                  {atRoot ? (
                    <button
                      onClick={() => enter(n)}
                      className="flex w-full items-center justify-between px-4 py-3 text-left text-sm text-slate-700 hover:bg-slate-50"
                    >
                      <span>{n.name}</span>
                      <span className="text-slate-300">›</span>
                    </button>
                  ) : (
                    <div className="px-4 py-3 text-sm text-slate-700">{n.name}</div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </CmdsShell>
  );
}
