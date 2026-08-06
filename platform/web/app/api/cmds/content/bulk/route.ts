import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

const COLL_FOR_TYPE: Record<string, string> = {
  DOCUMENT: "documents",
  VIDEO: "videos",
  TEST: "tests",
  MODULE: "modules",
  QUESTION_SET: "questionsets",
};

type BulkAction =
  | "addToSection"
  | "removeFromSection"
  | "visible"
  | "invisible"
  | "enableDownload"
  | "disableDownload";

type BulkBody = {
  items?: { id: string; type: string }[];
  action?: BulkAction;
  sectionId?: string;
};

// POST: bulk "Choose Operation" over multiple content rows within a Section
// — the legacy CMDS publish workflow (add to section, then Make Visible /
// Invisible, Enable / Disable Download, Remove From Section).
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
    return NextResponse.json({ error: "Only institute admins can publish content." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as BulkBody;
  const items = Array.isArray(b.items) ? b.items : [];
  if (!items.length) return NextResponse.json({ error: "No items selected" }, { status: 400 });

  const needsSection = b.action === "addToSection" || b.action === "removeFromSection" || b.action === "visible" || b.action === "invisible";
  if (needsSection && !b.sectionId)
    return NextResponse.json({ error: "sectionId is required for this action" }, { status: 400 });

  let update: Record<string, unknown> | null = null;
  switch (b.action) {
    case "addToSection":
      update = { $addToSet: { sectionIds: b.sectionId } };
      break;
    case "removeFromSection":
      update = { $pull: { sectionIds: b.sectionId, visibleSectionIds: b.sectionId } };
      break;
    case "visible":
      update = { $addToSet: { visibleSectionIds: b.sectionId } };
      break;
    case "invisible":
      update = { $pull: { visibleSectionIds: b.sectionId } };
      break;
    case "enableDownload":
      update = { $set: { downloadEnabled: true } };
      break;
    case "disableDownload":
      update = { $set: { downloadEnabled: false } };
      break;
    default:
      return NextResponse.json({ error: "Unsupported action" }, { status: 400 });
  }

  const byColl = new Map<string, ObjectId[]>();
  for (const item of items) {
    const type = String(item.type || "").toUpperCase();
    // Videos are never downloadable — no toggle, no exception. Silently drop
    // them from an "Enable Download" bulk op rather than erroring the whole
    // batch over a mixed-type selection.
    if (b.action === "enableDownload" && type === "VIDEO") continue;
    const coll = COLL_FOR_TYPE[type];
    if (!coll || !ObjectId.isValid(item.id)) continue;
    const ids = byColl.get(coll) || [];
    ids.push(new ObjectId(item.id));
    byColl.set(coll, ids);
  }
  if (!byColl.size) return NextResponse.json({ error: "No valid items" }, { status: 400 });

  try {
    const db = await getDb();
    await Promise.all(
      Array.from(byColl.entries()).map(([coll, ids]) =>
        db.collection(coll).updateMany({ _id: { $in: ids } }, update as any)
      )
    );
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Bulk update failed" }, { status: 500 });
  }
}
