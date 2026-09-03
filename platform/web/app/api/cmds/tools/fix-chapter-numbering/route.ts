import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";
import { loadOrgFolders } from "@/lib/courses";

export const dynamic = "force-dynamic";

// Matches folder names with a zero-padded leading number, e.g. "02.Economic
// Botany" or "05 Magnetism" — but NOT "1.Sexual Reproduction" (no leading
// zero, already fine) and NOT "10.Environmental Issues" (two real digits,
// not zero-padded). Handles both "N." and "N " separators, since real
// folder names in this org use both inconsistently — only the leading
// zero digit is stripped; whatever separator was already there (period or
// space) is preserved exactly as-is.
const ZERO_PADDED = /^0(\d+)([.\s])(.*)$/;

type Change = { id: string; oldName: string; newName: string };

async function scan(orgId: string): Promise<Change[]> {
  const db = await getDb();
  const folders = await loadOrgFolders(db, orgId);
  const changes: Change[] = [];
  for (const f of folders) {
    const m = f.name.match(ZERO_PADDED);
    if (!m) continue;
    const newName = `${m[1]}${m[2]}${m[3]}`;
    if (newName !== f.name) changes.push({ id: f.id, oldName: f.name, newName });
  }
  return changes;
}

// GET: preview only — scans every subject/chapter/session folder in this
// org and reports what WOULD change, without touching the database. Safe
// to call as many times as you like.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const changes = await scan(orgId);
  return NextResponse.json({ changes, count: changes.length });
}

// POST: re-scans (never trusts a client-supplied list) and actually
// applies the renames — strips the leading zero from every mismatched
// folder name, across every subject in this org, in one pass.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const changes = await scan(orgId);

  const db = await getDb();
  const folders = db.collection("folders");
  for (const c of changes) {
    await folders.updateOne({ _id: new ObjectId(c.id) }, { $set: { name: c.newName } });
  }

  return NextResponse.json({ applied: changes.length, changes });
}
