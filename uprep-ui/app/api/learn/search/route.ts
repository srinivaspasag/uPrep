import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Global content search. The legacy browse is ElasticSearch-backed (not indexed
// on this stack), so we do a name regex across the content collections in Mongo.
export async function GET(req: NextRequest) {
  const q = (req.nextUrl.searchParams.get("q") || "").trim();
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  if (!q) return NextResponse.json({ results: [] });

  const rx = { $regex: q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), $options: "i" };
  const colls: { coll: string; type: string }[] = [
    { coll: "tests", type: "TEST" },
    { coll: "modules", type: "MODULE" },
    { coll: "documents", type: "DOCUMENT" },
    { coll: "videos", type: "VIDEO" },
    { coll: "questionsets", type: "QUESTION_SET" },
    { coll: "discussions", type: "DISCUSSION" },
  ];

  try {
    const db = await getDb();
    const results: any[] = [];
    for (const { coll, type } of colls) {
      const filter: any = { recordState: "ACTIVE", name: rx };
      if (coll !== "discussions") filter["contentSrc.id"] = orgId;
      else filter["contentSrc.id"] = orgId;
      const docs = await db.collection(coll).find(filter).limit(20).toArray();
      for (const d of docs as any[]) {
        results.push({
          id: String(d._id),
          name: d.name || "(untitled)",
          type,
          subject: d.subject || null,
          url: d.url || null,
        });
      }
    }
    return NextResponse.json({ results, query: q });
  } catch (e: any) {
    return NextResponse.json({ results: [], error: e?.message }, { status: 500 });
  }
}
