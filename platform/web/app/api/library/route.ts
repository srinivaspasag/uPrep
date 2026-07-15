import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

function collForType(type: string): string {
  const m: Record<string, string> = {
    TEST: "tests",
    MODULE: "modules",
    DOCUMENT: "documents",
    VIDEO: "videos",
  };
  return m[type] || "tests";
}

function defaultTypeForColl(coll: string): string {
  const m: Record<string, string> = {
    tests: "TEST",
    modules: "MODULE",
    documents: "DOCUMENT",
    videos: "VIDEO",
  };
  return m[coll] || "TEST";
}

type LibraryItem = {
  id: string;
  name: string;
  type: string;
  questionCount: number;
  durationMin: number;
  totalMarks: number;
  difficulty: string | null;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
  linkType?: string | null;
};

// Lists browsable content (tests + modules) for an org directly from MongoDB.
// The legacy browse endpoints are ElasticSearch-backed and aren't indexed on
// this stack, so we read Mongo directly for the demo. Detail/take-test still
// use the real content service.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const typeParam = req.nextUrl.searchParams.get("type"); // optional: TEST | MODULE

  try {
    const db = await getDb();
    const filter: Record<string, unknown> = {
      "contentSrc.id": orgId,
      recordState: "ACTIVE",
    };

    const allCollections = ["tests", "modules", "documents", "videos"];
    const collections = typeParam
      ? allCollections.filter((c) => collForType(typeParam) === c)
      : allCollections;
    const items: LibraryItem[] = [];

    for (const coll of collections) {
      const docs = await db
        .collection(coll)
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();

      for (const d of docs as any[]) {
        items.push({
          id: String(d._id),
          name: d.name || d.title || "(untitled)",
          type: d.type || defaultTypeForColl(coll),
          questionCount: d.actualQusCount ?? d.qusCount ?? 0,
          durationMin: d.duration ? Math.round(d.duration / 60000) : 0,
          totalMarks: d.totalMarks ?? 0,
          difficulty: d.difficulty ?? null,
          url: d.url ?? null,
          embedUrl: d.embedUrl ?? null,
          provider: d.provider ?? null,
          linkType: d.linkType ?? null,
        });
      }
    }

    return NextResponse.json({ items, orgId });
  } catch (e: any) {
    return NextResponse.json(
      { items: [], error: e?.message || "Failed to load library" },
      { status: 500 }
    );
  }
}
