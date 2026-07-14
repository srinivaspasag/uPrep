"use client";

import { useEffect, useState } from "react";
import LmsShell from "@/components/LmsShell";
import { getSession } from "@/lib/session";

type Assignment = {
  id: string;
  name: string;
  description: string;
  dueDate: number | null;
  maxMarks: number;
  submitted: boolean;
  submittedText: string;
  marks: number | null;
  status: string;
};

export default function AssignmentsPage() {
  const [items, setItems] = useState<Assignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [openId, setOpenId] = useState<string | null>(null);
  const [text, setText] = useState("");

  async function load() {
    const uid = getSession()?.id || "";
    const res = await fetch(`/api/learn/assignments?userId=${encodeURIComponent(uid)}`);
    const d = await res.json();
    setItems(d.items || []);
    setLoading(false);
  }
  useEffect(() => {
    load();
  }, []);

  async function submit(a: Assignment) {
    const s = getSession();
    await fetch("/api/learn/assignments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        assignmentId: a.id,
        userId: s?.id,
        userName: [s?.firstName, s?.lastName].filter(Boolean).join(" "),
        text,
      }),
    });
    setOpenId(null);
    setText("");
    load();
  }

  return (
    <LmsShell active="library">
      <h1 className="text-lg font-semibold text-slate-800">Assignments</h1>

      {loading ? (
        <div className="py-16 text-center text-sm text-slate-400">Loading…</div>
      ) : items.length === 0 ? (
        <div className="mt-6 rounded-lg border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400">
          No assignments yet.
        </div>
      ) : (
        <ul className="mt-4 space-y-3">
          {items.map((a) => (
            <li key={a.id} className="rounded-lg border border-slate-200 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="font-medium text-slate-800">{a.name}</div>
                  {a.description && <div className="mt-1 text-sm text-slate-500">{a.description}</div>}
                  <div className="mt-2 flex gap-3 text-xs text-slate-400">
                    {a.dueDate && <span>Due {new Date(a.dueDate).toLocaleDateString()}</span>}
                    {a.maxMarks > 0 && <span>Max {a.maxMarks} marks</span>}
                  </div>
                </div>
                <span
                  className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                    a.submitted ? "bg-emerald-100 text-emerald-700" : "bg-amber-100 text-amber-700"
                  }`}
                >
                  {a.marks != null ? `${a.marks}/${a.maxMarks}` : a.submitted ? "Submitted" : "Pending"}
                </span>
              </div>

              {openId === a.id ? (
                <div className="mt-3">
                  <textarea
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    rows={3}
                    placeholder="Type your answer…"
                    className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-400"
                  />
                  <div className="mt-2 flex gap-2">
                    <button
                      onClick={() => submit(a)}
                      className="rounded-md bg-[#e8443b] px-4 py-1.5 text-sm font-semibold text-white hover:bg-[#d33c34]"
                    >
                      Submit
                    </button>
                    <button
                      onClick={() => {
                        setOpenId(null);
                        setText("");
                      }}
                      className="rounded-md border border-slate-300 px-4 py-1.5 text-sm text-slate-600"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <button
                  onClick={() => {
                    setOpenId(a.id);
                    setText(a.submittedText || "");
                  }}
                  className="mt-3 text-sm font-medium text-emerald-600 hover:underline"
                >
                  {a.submitted ? "Edit submission" : "Submit answer"}
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </LmsShell>
  );
}
