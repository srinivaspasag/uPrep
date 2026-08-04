import { NextRequest, NextResponse } from "next/server";
import { resolveOrgId } from "@/lib/org-scope";
import { callBoardService } from "@/lib/legacyBoard";

export const dynamic = "force-dynamic";

// Board Tree, student-facing copy of app/api/cmds/tools/boards/route.ts —
// deliberately outside /api/cmds/** since middleware.ts staff-gates that
// whole path and students need to browse the tree to tag a doubt (same
// reasoning as /api/seller/verify living outside the staff-gated path).
// Same live board-services proxy, same response shape.
export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const parentId = req.nextUrl.searchParams.get("parentId") || undefined;
  const typeOverride = req.nextUrl.searchParams.get("type") || undefined;
  const type = typeOverride || (parentId ? "TOPIC" : "COURSE");

  try {
    const res = await callBoardService<{ list?: any[] }>("getChildren", {
      orgId,
      userId: "student",
      callingUserId: "student",
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
