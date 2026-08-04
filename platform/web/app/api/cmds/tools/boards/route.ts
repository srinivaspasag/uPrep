import { NextRequest, NextResponse } from "next/server";
import { resolveOrgId } from "@/lib/org-scope";
import { callBoardService } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";

// Board Tree — proxied to the LIVE legacy board-services backend (real
// subject/chapter/topic data, confirmed working this session), replacing
// the old disconnected `topics`-collection stub. Legacy has no independent
// "list all subjects"/"list all chapters" action — the tree is walked one
// parentId hop at a time via getChildren, same as legacy's own admin UI
// (QrBoards.java's topicTree/subTopics). Root call (no parentId) returns
// this org's top-level Subject nodes (type=COURSE); passing a parentId
// returns that Subject's Chapters (type=TOPIC) by default, or that
// Chapter's Concepts (type=SUBTOPIC) when ?type=SUBTOPIC is given — mirrors
// legacy's real 3-level ORG tree (Subject -> Chapter -> Concept, confirmed
// via BoardXLParser's maxAllowedColumns=3 and the "Add SubTopic" control in
// tagging.js) rather than the 2-level cap we originally shipped.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const parentId = req.nextUrl.searchParams.get("parentId") || undefined;
  const typeOverride = req.nextUrl.searchParams.get("type") || undefined;
  const type = typeOverride || (parentId ? "TOPIC" : "COURSE");

  try {
    const res = await callBoardService<{ list?: any[] }>("getChildren", {
      orgId,
      userId: "admin",
      callingUserId: "admin",
      context: "ORG",
      type,
      ownerId: orgId,
      parentId,
    });
    const nodes = (res.list || []).map((n) => ({
      id: n.id,
      name: n.name,
      type: n.type,
      parentId: Array.isArray(n.parentIds) && n.parentIds.length > 0 ? n.parentIds[0] : null,
    }));
    return NextResponse.json({ nodes });
  } catch (e: any) {
    return NextResponse.json({ nodes: [], error: e?.message }, { status: 500 });
  }
}
