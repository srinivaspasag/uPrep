import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Public institute type-ahead for the login page — the analogue of the legacy
// institute-name autocomplete (UIComRegister orgList). Returns a handful of
// orgs whose name matches the query so the user picks an institute by NAME and
// we carry its id internally (users never see/type the raw org id).
//
//   GET /api/orgs/suggest?q=demo  -> { orgs: [{ id, name, fullName }] }
export async function GET(req: NextRequest) {
  const q = (req.nextUrl.searchParams.get("q") || "").trim();
  if (q.length < 1) return NextResponse.json({ orgs: [] });

  // Escape regex metacharacters so a user's input is matched literally.
  const safe = q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const rx = new RegExp(safe, "i");

  try {
    const db = await getDb();
    const docs = await db
      .collection("organizations")
      .find({ recordState: "ACTIVE", $or: [{ name: rx }, { fullName: rx }] } as any)
      .limit(8)
      .toArray();

    const orgs = (docs as any[]).map((o) => ({
      id: String(o._id),
      name: o.name || o.fullName || "(unnamed)",
      fullName: o.fullName || "",
    }));
    return NextResponse.json({ orgs });
  } catch (e: any) {
    return NextResponse.json({ orgs: [], error: e?.message || "Failed" }, { status: 500 });
  }
}
