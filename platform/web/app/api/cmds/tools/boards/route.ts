import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { callBoardService } from "@/lib/legacyBoard";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";

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
//
// getChildren is strictly ownerId-scoped, with no org-to-org sharing concept
// of its own — an org with no board tree of its own (e.g. a freshly created
// institute that only has GRANTED content) got "No subjects available" even
// though it can browse and tag content whose real subjects live on the
// provider's tree. Queried once per owner org (own + whoever granted this
// org a course — same set Institute Resources already browses) and merged,
// so tagging a locally-uploaded book can reach the same subjects the granted
// courses are actually organized under. A given parentId only ever belongs
// to ONE owner's tree, so only one of these calls returns anything for a
// chapter/concept-level lookup; the rest come back empty.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const parentId = req.nextUrl.searchParams.get("parentId") || undefined;
  const typeOverride = req.nextUrl.searchParams.get("type") || undefined;
  const type = typeOverride || (parentId ? "TOPIC" : "COURSE");

  try {
    const db = await getDb();
    const catalog = await resolveCourseCatalog(db, orgId);
    const ownerOrgIds = catalogOwnerOrgs(orgId, catalog);

    const results = await Promise.all(
      ownerOrgIds.map((ownerId) =>
        callBoardService<{ list?: any[] }>("getChildren", {
          orgId: ownerId,
          userId: "admin",
          callingUserId: "admin",
          context: "ORG",
          type,
          ownerId,
          parentId,
        }).catch(() => ({ list: [] as any[] }))
      )
    );

    const seen = new Set<string>();
    const nodes: { id: string; name: string; type: string; parentId: string | null }[] = [];
    for (const res of results) {
      for (const n of res.list || []) {
        if (seen.has(n.id)) continue;
        seen.add(n.id);
        nodes.push({
          id: n.id,
          name: n.name,
          type: n.type,
          parentId: Array.isArray(n.parentIds) && n.parentIds.length > 0 ? n.parentIds[0] : null,
        });
      }
    }
    return NextResponse.json({ nodes });
  } catch (e: any) {
    return NextResponse.json({ nodes: [], error: e?.message }, { status: 500 });
  }
}
