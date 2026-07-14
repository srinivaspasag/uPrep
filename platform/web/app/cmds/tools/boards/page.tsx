"use client";

import { useEffect, useMemo, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Node = { id: string; name: string; parentId: string | null };

export default function BoardsPage() {
  const [nodes, setNodes] = useState<Node[]>([]);
  const [loading, setLoading] = useState(true);
  const [rootName, setRootName] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/boards").then((r) => r.json());
    setNodes(d.nodes || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function add(name: string, parentId: string | null) {
    if (!name.trim()) return;
    await fetch("/api/cmds/tools/boards", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: name.trim(), parentId }),
    });
    load();
  }
  async function remove(id: string) {
    if (!confirm("Remove this node and everything under it?")) return;
    await fetch(`/api/cmds/tools/boards?id=${id}`, { method: "DELETE" });
    load();
  }

  const roots = useMemo(() => nodes.filter((n) => !n.parentId), [nodes]);

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[820px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Boards & Course Management</h1>
        <p className="mt-1 text-sm text-slate-500">
          Build your subject → chapter → topic tree. Content and questions can be tagged to these.
        </p>

        <div className="mt-5 flex gap-2">
          <input
            value={rootName}
            onChange={(e) => setRootName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                add(rootName, null);
                setRootName("");
              }
            }}
            placeholder="Add a subject (top-level)…"
            className="w-72 rounded border border-slate-300 px-3 py-1.5 text-sm outline-none focus:border-slate-500"
          />
          <button
            onClick={() => {
              add(rootName, null);
              setRootName("");
            }}
            className="rounded bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
          >
            Add subject
          </button>
        </div>

        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading tree…</div>
        ) : roots.length === 0 ? (
          <div className="mt-6 rounded border border-dashed border-slate-200 py-12 text-center text-sm text-slate-400">
            No subjects yet. Add your first subject above.
          </div>
        ) : (
          <div className="mt-6 space-y-2">
            {roots.map((r) => (
              <TreeNode key={r.id} node={r} nodes={nodes} onAdd={add} onRemove={remove} depth={0} />
            ))}
          </div>
        )}
      </div>
    </CmdsShell>
  );
}

function TreeNode({
  node,
  nodes,
  onAdd,
  onRemove,
  depth,
}: {
  node: Node;
  nodes: Node[];
  onAdd: (name: string, parentId: string | null) => void;
  onRemove: (id: string) => void;
  depth: number;
}) {
  const [open, setOpen] = useState(depth < 1);
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState("");
  const children = nodes.filter((n) => n.parentId === node.id);

  return (
    <div style={{ marginLeft: depth * 18 }}>
      <div className="group flex items-center gap-2 rounded px-2 py-1.5 hover:bg-slate-50">
        <button onClick={() => setOpen((o) => !o)} className="w-4 text-slate-400">
          {children.length > 0 ? (open ? "▾" : "▸") : "•"}
        </button>
        <span className="flex-1 text-sm text-slate-700">{node.name}</span>
        <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100">
          <button onClick={() => setAdding((a) => !a)} className="text-xs text-emerald-600 hover:underline">
            + Sub-topic
          </button>
          <button onClick={() => onRemove(node.id)} className="text-xs text-red-500 hover:underline">
            ✕
          </button>
        </div>
      </div>

      {adding && (
        <div className="ml-6 mt-1 flex gap-2">
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                onAdd(name, node.id);
                setName("");
                setAdding(false);
              }
            }}
            placeholder="New sub-topic…"
            className="w-56 rounded border border-slate-300 px-2 py-1 text-sm outline-none focus:border-slate-500"
          />
          <button
            onClick={() => {
              onAdd(name, node.id);
              setName("");
              setAdding(false);
            }}
            className="text-xs text-emerald-600"
          >
            Add
          </button>
        </div>
      )}

      {open &&
        children.map((c) => (
          <TreeNode key={c.id} node={c} nodes={nodes} onAdd={onAdd} onRemove={onRemove} depth={depth + 1} />
        ))}
    </div>
  );
}
