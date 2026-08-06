import { API, CALLING_APP, CALLING_APP_ID } from "./config";

// Calls the LIVE legacy board-services backend (Play, http://lmsbe:19016 in
// production) — the real Board Tree service: subject/chapter/topic nodes
// used to tag questions and content. See legacy/lms-master/board/
// board-services/app/controllers/Boards.java. Confirmed live and working
// this session (unlike org-services' getOrganization, which needed a data
// fix first) via a direct getChildren call against the deployed instance.
//
// Mirrors lib/legacyOrg.ts's callLegacy exactly (form-encoded POST,
// {result, errorCode, errorMessage} envelope) — duplicated rather than
// imported to keep each legacy-service client independently readable, same
// as callOrgService/callMembersService/callActivityLogger already are.
function setFormField(form: URLSearchParams, key: string, value: unknown) {
  if (value === undefined || value === null || value === "") return;
  if (Array.isArray(value)) {
    value.forEach((item, i) => {
      if (item !== null && typeof item === "object") {
        for (const [k, v] of Object.entries(item)) setFormField(form, `${key}[${i}].${k}`, v);
      } else {
        form.set(`${key}[${i}]`, String(item));
      }
    });
  } else if (typeof value === "object") {
    for (const [k, v] of Object.entries(value as Record<string, unknown>)) {
      setFormField(form, `${key}.${k}`, v);
    }
  } else {
    form.set(key, String(value));
  }
}

// Resolves board ids straight to their own node name — no parent-walk
// needed. getTreesOfBoards's treeRootIds can be ANY node id (not just
// subjects); with depth 0 it returns just that node's own BoardTree,
// wrapping the BoardBasicInfo we want. Confirmed via
// board-mgmt/.../BoardManager.getTreesOfBoards -> BoardDAO.collectHierarchy,
// which collects hierarchy DOWNWARD from the given ids. This is what powers
// showing a question's tagged chapter/topic name in the Question Bank list.
export async function resolveBoardNames(
  orgId: string,
  ids: string[]
): Promise<Record<string, string>> {
  const unique = Array.from(new Set(ids.filter(Boolean)));
  if (unique.length === 0) return {};
  try {
    const res = await callBoardService<{ list?: any[] }>("getTreesOfBoards", {
      orgId,
      userId: "admin",
      callingUserId: "admin",
      context: "ORG",
      ownerId: orgId,
      treeRootIds: unique,
      depth: 0,
    });
    const out: Record<string, string> = {};
    for (const t of res.list || []) {
      if (t?.board?.id && t.board.name) out[t.board.id] = t.board.name;
    }
    return out;
  } catch {
    return {};
  }
}

// Resolves a set of (usually chapter-level) board ids all the way up to
// their top-level Subject name — one extra batched getTreesOfBoards call on
// the union of parent ids, reusing the same BoardBasicInfo.parentIds field
// resolveBoardNames already receives but doesn't expose. Powers the
// per-subject rollup in student analytics: a question is tagged at chapter
// level (see resolveBoardNames's own comment), but "which subject is this
// student weak in" needs the subject, not the chapter.
export async function resolveBoardSubjects(
  orgId: string,
  chapterIds: string[]
): Promise<Record<string, string>> {
  const unique = Array.from(new Set(chapterIds.filter(Boolean)));
  if (unique.length === 0) return {};
  try {
    const res = await callBoardService<{ list?: any[] }>("getTreesOfBoards", {
      orgId,
      userId: "admin",
      callingUserId: "admin",
      context: "ORG",
      ownerId: orgId,
      treeRootIds: unique,
      depth: 0,
    });
    const parentOf: Record<string, string | null> = {};
    const nameOf: Record<string, string> = {};
    for (const t of res.list || []) {
      const id = t?.board?.id;
      if (!id) continue;
      nameOf[id] = t.board.name || "";
      const parentIds: string[] = Array.isArray(t.board.parentIds) ? t.board.parentIds : [];
      parentOf[id] = parentIds[0] || null;
    }
    const subjectIds = Array.from(new Set(Object.values(parentOf).filter(Boolean))) as string[];
    const subjectNames = subjectIds.length ? await resolveBoardNames(orgId, subjectIds) : {};
    const out: Record<string, string> = {};
    for (const id of unique) {
      const parentId = parentOf[id];
      out[id] = (parentId && subjectNames[parentId]) || nameOf[id] || "Other";
    }
    return out;
  } catch {
    return {};
  }
}

export async function callBoardService<T = any>(
  action: string,
  params: Record<string, unknown>
): Promise<T> {
  const form = new URLSearchParams({
    callingApp: CALLING_APP,
    callingAppId: CALLING_APP_ID,
  });
  for (const [key, value] of Object.entries(params)) {
    setFormField(form, key, value);
  }
  const resp = await fetch(`${API.board}/boards/${action}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form,
  });
  const json = await resp.json().catch(() => ({}));
  if (json.errorCode) {
    throw new Error(json.errorMessage || json.errorCode);
  }
  return (json.result ?? {}) as T;
}
