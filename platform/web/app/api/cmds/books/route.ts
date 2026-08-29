import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";
import { resolveBoardNames } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";

// Books — a dedicated management section (parallel to the Question Bank at
// /cmds/questions) over the `books` collection created by
// POST /api/cmds/upload (kind=book). Same board-tagging as every other
// content type, but its own list/section rather than being buried inside
// the generic Institute Resources folder browser.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management.", items: [] }, { status: 403 });

  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const boardId = req.nextUrl.searchParams.get("boardId");

  try {
    const db = await getDb();
    const filter: Record<string, unknown> = { "contentSrc.id": orgId, recordState: "ACTIVE" };
    if (boardId) filter.boardIds = boardId;

    const docs = await db.collection("books").find(filter).sort({ lastUpdated: -1 }).limit(500).toArray();

    const chapterIds = (docs as any[])
      .map((d) => (Array.isArray(d.boardIds) ? d.boardIds[d.boardIds.length - 1] : null))
      .filter(Boolean) as string[];
    const chapterNames = await resolveBoardNames(orgId, chapterIds);

    const items = (docs as any[]).map((d) => {
      const chapterId = Array.isArray(d.boardIds) ? d.boardIds[d.boardIds.length - 1] : null;
      return {
        id: String(d._id),
        name: d.name || "(untitled book)",
        url: d.url || null,
        fileSize: d.fileSize ?? null,
        subject: d.subject || null,
        chapter: chapterId ? chapterNames[chapterId] || null : null,
        boardIds: Array.isArray(d.boardIds) ? d.boardIds : [],
        lastUpdated: d.lastUpdated ?? 0,
      };
    });

    return NextResponse.json({ items });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message || "Failed to load books" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = req.nextUrl.searchParams.get("id") || "";
  if (!id || !ObjectId.isValid(id)) return NextResponse.json({ error: "Valid id required" }, { status: 400 });

  try {
    const db = await getDb();
    await db
      .collection("books")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}
