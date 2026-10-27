import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const JOBS_COLL = "packagejobs";

async function requireManager(req: NextRequest) {
  const s = await sessionFromReq(req);
  return (s?.profile || "").trim().toUpperCase() === "MANAGER";
}

// Polled by the CMDS UI after GET .../package returns a jobId — the package
// route itself only enqueues and returns immediately (see its comment for
// why); this is how the UI finds out when the background job is actually
// done and where to download it from.
//
//   GET ?jobId=<id>        -> that specific job
//   GET (no jobId)         -> most recent job for this group
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can package distribution groups." }, { status: 403 });

  const groupId = params.id;
  const jobId = req.nextUrl.searchParams.get("jobId");
  const orgId = await resolveOrgId(req, null);

  try {
    const db = await getDb();
    const job = jobId
      ? await db.collection(JOBS_COLL).findOne({ _id: jobId, groupId, orgId } as any)
      : await db
          .collection(JOBS_COLL)
          .find({ groupId, orgId } as any)
          .sort({ timeCreated: -1 })
          .limit(1)
          .next();

    if (!job) return NextResponse.json({ error: "Job not found" }, { status: 404 });

    return NextResponse.json({
      jobId: job._id,
      status: job.status,
      processed: job.processed || 0,
      total: job.total || 0,
      url: job.url || null,
      fileName: job.fileName || null,
      sizeBytes: job.sizeBytes || null,
      error: job.error || null,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}
