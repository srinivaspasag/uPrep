"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CmdsShell from "@/components/CmdsShell";
import { getSession, type UprepSession } from "@/lib/session";
import BoardPicker from "@/components/BoardPicker";

type LibQuestion = {
  id: string;
  text: string;
  type: string;
  options: number;
  hasKey: boolean;
};

export default function NewQuestionSetPage() {
  const router = useRouter();
  const [session, setSession] = useState<UprepSession | null>(null);
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [boardIds, setBoardIds] = useState<string[]>([]);
  const [pool, setPool] = useState<LibQuestion[]>([]);
  const [picked, setPicked] = useState<string[]>([]);
  // Optional cap on how many questions this set should contain — once
  // reached, remaining checkboxes disable instead of silently letting you
  // over-select. Empty/0 = no limit.
  const [targetCount, setTargetCount] = useState<number | "">("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [folderId, setFolderId] = useState<string | null>(null);
  const [folderName, setFolderName] = useState<string>("");

  // Where to return after create/cancel — back into the folder we came from.
  const backHref = folderId
    ? `/cmds?folder=${encodeURIComponent(folderId)}&folderName=${encodeURIComponent(folderName)}`
    : "/cmds";

  useEffect(() => {
    const s = getSession();
    setSession(s);
    if (!s) return;
    const sp = new URLSearchParams(window.location.search);
    setFolderId(sp.get("folder"));
    setFolderName(sp.get("folderName") || "");

    fetch(`/api/cmds/tests`)
      .then((r) => r.json())
      .then((d) => setPool(d.questions || []))
      .finally(() => setLoading(false));
  }, []);

  const atLimit = typeof targetCount === "number" && targetCount > 0 && picked.length >= targetCount;

  function toggle(id: string) {
    setPicked((p) => {
      if (p.includes(id)) return p.filter((x) => x !== id);
      if (typeof targetCount === "number" && targetCount > 0 && p.length >= targetCount) return p; // limit reached
      return [...p, id];
    });
  }

  async function submit() {
    setError("");
    if (!name.trim()) return setError("Please enter a set name.");
    if (picked.length === 0) return setError("Pick at least one question.");
    setSaving(true);
    try {
      const res = await fetch("/api/cmds/content", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          kind: "questionset",
          name: name.trim(),
          subject: subject.trim(),
          boardIds,
          qIds: picked,
          userId: session?.id,
          folderId: folderId || undefined,
        }),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({}));
        setError(d.error || "Failed to create question set");
        return;
      }
      router.push(backHref);
    } finally {
      setSaving(false);
    }
  }

  return (
    <CmdsShell active="resources">
      <div className="mx-auto max-w-[760px] px-6 py-8">
        <div className="mb-4 text-sm text-slate-400">
          <Link href="/cmds" className="hover:text-slate-600">
            Institute Resources
          </Link>{" "}
          {folderName && (
            <>
              / <span className="text-slate-600">{folderName}</span>{" "}
            </>
          )}
          / <span className="text-slate-600">Add a Question Set</span>
        </div>
        <h1 className="text-2xl font-light text-slate-700">Add a Question Set</h1>
        {folderName && (
          <p className="mt-1 text-sm text-slate-400">
            This question set will be added to <span className="font-medium text-slate-600">{folderName}</span>.
          </p>
        )}

        <div className="mt-6">
          <label className="mb-1 block text-sm font-medium text-slate-600">Set name</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
            placeholder="e.g. Newton's Laws – Practice"
          />
        </div>

        <div className="mt-4">
          <BoardPicker selected={boardIds} onChange={setBoardIds} onSubjectChange={setSubject} />
          {subject && (
            <p className="mt-1 text-xs text-slate-400">
              Subject: <span className="font-medium text-slate-600">{subject}</span> — derived from the chapter(s)
              tagged above.
            </p>
          )}
        </div>

        <div className="mt-6">
          <label className="mb-1 block text-sm font-medium text-slate-600">
            Number of questions to add <span className="text-slate-400">(optional)</span>
          </label>
          <input
            type="number"
            min={1}
            value={targetCount}
            onChange={(e) => setTargetCount(e.target.value === "" ? "" : Math.max(1, Number(e.target.value)))}
            placeholder="Leave blank for no limit"
            className="w-48 rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
          />
        </div>

        <div className="mt-6">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-600">
              Published questions (
              {typeof targetCount === "number" && targetCount > 0
                ? `${picked.length} / ${targetCount} selected`
                : `${picked.length} selected`}
              )
            </h2>
            <Link href="/cmds/questions" className="text-xs text-blue-600 hover:underline">
              Manage / publish questions →
            </Link>
          </div>
          {atLimit && (
            <p className="mb-2 text-xs text-amber-600">
              Reached your limit of {targetCount}. Uncheck a question to swap in another.
            </p>
          )}
          {loading ? (
            <div className="py-8 text-center text-slate-400">Loading questions…</div>
          ) : pool.length === 0 ? (
            <div className="rounded border border-dashed border-slate-200 py-8 text-center text-sm text-slate-400">
              No published questions yet. Author and publish questions first.
            </div>
          ) : (
            <div className="max-h-[360px] space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
              {pool.map((q) => {
                const isPicked = picked.includes(q.id);
                const disabled = !isPicked && atLimit;
                return (
                  <label
                    key={q.id}
                    className={`flex items-start gap-3 rounded px-2 py-2 text-sm ${
                      disabled ? "cursor-not-allowed opacity-40" : "cursor-pointer hover:bg-slate-50"
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={isPicked}
                      disabled={disabled}
                      onChange={() => toggle(q.id)}
                      className="mt-0.5 accent-emerald-600"
                    />
                    <span className="flex-1 text-slate-700">{q.text || "(no text)"}</span>
                    <span className="text-xs text-slate-400">{q.type}</span>
                  </label>
                );
              })}
            </div>
          )}
        </div>

        {error && <div className="mt-4 text-sm text-red-600">{error}</div>}

        <div className="mt-6 flex gap-3">
          <button
            onClick={submit}
            disabled={saving}
            className="rounded bg-emerald-600 px-5 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {saving ? "Creating…" : "Create Question Set"}
          </button>
          <Link href={backHref} className="rounded px-5 py-2 text-sm text-slate-500 hover:bg-slate-100">
            Cancel
          </Link>
        </div>
      </div>
    </CmdsShell>
  );
}
