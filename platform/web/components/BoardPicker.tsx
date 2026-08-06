"use client";

import { useEffect, useState } from "react";

type Node = { id: string; name: string; type: string; parentId: string | null };

// Board Tree tagging picker — Subject -> Chapter multi-select, with an
// optional per-chapter "Add Concepts" drill-down, backed by the same
// live-legacy-proxied /api/cmds/tools/boards endpoint the Boards & Course
// Management page uses. Reused across question creation, content upload,
// and the auto-test generator (see the plan doc: legacy's `boardIds` field
// is shared across all three via a common base model).
//
// Legacy's real ORG tree is 3 levels (Subject -> Chapter -> Concept, i.e.
// COURSE -> TOPIC -> SUBTOPIC), confirmed via BoardXLParser's
// maxAllowedColumns=3 and the "Add SubTopic" control in its tagging widget
// (uicomWidgets/tagging.js). Concepts are progressive disclosure, exactly
// like legacy — collapsed by default, expand a chapter to load and pick
// its concepts. All three levels write into the same flat `boardIds` list.
export default function BoardPicker({
  selected,
  onChange,
  apiBase = "/api/cmds/tools/boards",
  onSubjectChange,
}: {
  selected: string[];
  onChange: (boardIds: string[]) => void;
  apiBase?: string;
  // Reports the name of whichever Subject is currently picked here, so a
  // parent form can use it as the single source of truth for "subject"
  // instead of keeping a separate free-text field that can drift out of
  // sync with what's actually tagged in boardIds (see CmdsUploadForm).
  onSubjectChange?: (name: string) => void;
}) {
  const [subjects, setSubjects] = useState<Node[]>([]);
  const [subjectId, setSubjectId] = useState("");
  const [chapters, setChapters] = useState<Node[]>([]);
  const [loading, setLoading] = useState(false);
  const [openConceptsFor, setOpenConceptsFor] = useState<string | null>(null);
  const [concepts, setConcepts] = useState<Record<string, Node[]>>({});
  const [loadingConcepts, setLoadingConcepts] = useState(false);
  // Bug found live: the tagged summary only ever showed a bare count ("1
  // item(s) tagged"), never which item — accumulates a name for every node
  // seen while navigating (subjects, chapters, concepts) so the summary can
  // actually name what's tagged instead of just counting it.
  const [nameById, setNameById] = useState<Record<string, string>>({});

  function rememberNames(nodes: Node[]) {
    if (!nodes.length) return;
    setNameById((prev) => {
      const next = { ...prev };
      for (const n of nodes) next[n.id] = n.name;
      return next;
    });
  }

  useEffect(() => {
    fetch(apiBase)
      .then((r) => r.json())
      .then((d) => {
        setSubjects(d.nodes || []);
        rememberNames(d.nodes || []);
      })
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [apiBase]);

  useEffect(() => {
    if (!subjectId) {
      setChapters([]);
      return;
    }
    setLoading(true);
    fetch(`${apiBase}?parentId=${subjectId}`)
      .then((r) => r.json())
      .then((d) => {
        setChapters(d.nodes || []);
        rememberNames(d.nodes || []);
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subjectId, apiBase]);

  useEffect(() => {
    if (!onSubjectChange) return;
    const match = subjects.find((s) => s.id === subjectId);
    onSubjectChange(match?.name || "");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subjectId, subjects]);

  function toggle(id: string) {
    onChange(selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id]);
  }

  function toggleConcepts(chapterId: string) {
    if (openConceptsFor === chapterId) {
      setOpenConceptsFor(null);
      return;
    }
    setOpenConceptsFor(chapterId);
    if (concepts[chapterId]) return;
    setLoadingConcepts(true);
    fetch(`${apiBase}?parentId=${chapterId}&type=SUBTOPIC`)
      .then((r) => r.json())
      .then((d) => {
        setConcepts((prev) => ({ ...prev, [chapterId]: d.nodes || [] }));
        rememberNames(d.nodes || []);
      })
      .finally(() => setLoadingConcepts(false));
  }

  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-slate-600">Tag to Board Tree</label>
      <select
        value={subjectId}
        onChange={(e) => setSubjectId(e.target.value)}
        className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
      >
        <option value="">Select a subject…</option>
        {subjects.map((s) => (
          <option key={s.id} value={s.id}>
            {s.name}
          </option>
        ))}
      </select>

      {subjectId && (
        <div className="mt-2 max-h-72 overflow-y-auto rounded border border-slate-200">
          {loading ? (
            <div className="px-3 py-4 text-center text-xs text-slate-400">Loading chapters…</div>
          ) : chapters.length === 0 ? (
            <div className="px-3 py-4 text-center text-xs text-slate-400">No chapters under this subject</div>
          ) : (
            chapters.map((c) => (
              <div key={c.id} className="border-b border-slate-50 last:border-0">
                <div className="flex items-center gap-2 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50">
                  <label className="flex flex-1 cursor-pointer items-center gap-2">
                    <input
                      type="checkbox"
                      checked={selected.includes(c.id)}
                      onChange={() => toggle(c.id)}
                      className="accent-emerald-600"
                    />
                    {c.name}
                  </label>
                  <button
                    type="button"
                    onClick={() => toggleConcepts(c.id)}
                    className="shrink-0 text-xs text-blue-600 hover:underline"
                  >
                    {openConceptsFor === c.id ? "Hide concepts" : "+ Concepts"}
                  </button>
                </div>
                {openConceptsFor === c.id && (
                  <div className="ml-6 mb-1 rounded border border-slate-100 bg-slate-50">
                    {loadingConcepts && !concepts[c.id] ? (
                      <div className="px-3 py-2 text-xs text-slate-400">Loading concepts…</div>
                    ) : (concepts[c.id] || []).length === 0 ? (
                      <div className="px-3 py-2 text-xs text-slate-400">No concepts under this chapter</div>
                    ) : (
                      concepts[c.id].map((s) => (
                        <label
                          key={s.id}
                          className="flex cursor-pointer items-center gap-2 border-b border-slate-100 px-3 py-1 text-xs text-slate-600 last:border-0 hover:bg-white"
                        >
                          <input
                            type="checkbox"
                            checked={selected.includes(s.id)}
                            onChange={() => toggle(s.id)}
                            className="accent-emerald-600"
                          />
                          {s.name}
                        </label>
                      ))
                    )}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {selected.length > 0 && (
        <p className="mt-1 text-xs text-slate-400">
          {selected.length} item{selected.length === 1 ? "" : "s"} tagged:{" "}
          {selected.map((id) => nameById[id] || "(unnamed)").join(", ")}
        </p>
      )}
    </div>
  );
}
