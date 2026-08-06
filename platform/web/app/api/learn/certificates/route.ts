import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { randomBytes } from "crypto";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { resolveStudentEnrollment } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const CERTS_COLL = "certificates";

// Identity comes from the server-trusted session; query params are a fallback
// so existing client calls keep working.
async function whoami(req: NextRequest) {
  const session = await sessionFromReq(req);
  const userId = session?.id || req.nextUrl.searchParams.get("userId") || "";
  const orgId = session?.orgId || req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  const studentName =
    [session?.firstName, session?.lastName].filter(Boolean).join(" ") || "Student";
  const memberId = session?.memberId || null;
  return { userId, orgId, studentName, memberId };
}

function serial(): string {
  return `UP-${randomBytes(4).toString("hex").toUpperCase()}-${randomBytes(2)
    .toString("hex")
    .toUpperCase()}`;
}

function mapCert(c: any) {
  return {
    id: String(c._id),
    serial: c.serial,
    refType: c.refType,
    refId: c.refId,
    name: c.refName,
    studentName: c.studentName,
    issuedAt: c.issuedAt,
  };
}

export async function GET(req: NextRequest) {
  const { userId, orgId, studentName } = await whoami(req);
  const certId = req.nextUrl.searchParams.get("certId");

  try {
    const db = await getDb();

    // Single issued certificate by id (certificate detail/verify view).
    if (certId && ObjectId.isValid(certId)) {
      const c: any = await db.collection(CERTS_COLL).findOne({ _id: new ObjectId(certId) });
      if (!c) return NextResponse.json({ error: "Certificate not found" }, { status: 404 });
      return NextResponse.json({ certificate: mapCert(c) });
    }

    // Certificates are only ever meaningful for programs the student is
    // actually assigned to — bug found live: this queried every ACTIVE
    // program in the org regardless of enrollment, so a student saw a
    // certificate slot for every program in the institute, not just theirs.
    const { studentProgramIds } = await resolveStudentEnrollment(db, userId, []);
    const programs: any[] = studentProgramIds.length
      ? await db
          .collection("orgprograms")
          .find({ orgId, recordState: "ACTIVE", _id: { $in: studentProgramIds.map((id) => new ObjectId(id)) } })
          .sort({ lastUpdated: -1 })
          .toArray()
      : [];

    const finished = userId
      ? await db.collection("userentityattempts").countDocuments({ userId, entityType: "TEST" })
      : 0;

    const issued: any[] = userId
      ? await db
          .collection(CERTS_COLL)
          .find({ userId, recordState: "ACTIVE" })
          .sort({ issuedAt: -1 })
          .toArray()
      : [];
    const issuedByRef = new Map(issued.map((c) => [`${c.refType}:${c.refId}`, c]));

    const items = programs.map((p) => {
      const key = `PROGRAM:${String(p._id)}`;
      const cert = issuedByRef.get(key);
      return {
        id: String(p._id),
        name: p.name || p.cName || "Program",
        code: p.code || null,
        eligible: finished > 0,
        testsCompleted: finished,
        issued: !!cert,
        certificateId: cert ? String(cert._id) : null,
        serial: cert ? cert.serial : null,
      };
    });

    return NextResponse.json(
      {
        items,
        testsCompleted: finished,
        studentName,
        issued: issued.map(mapCert),
      },
      { headers: { "Cache-Control": "no-store, private", Vary: "Cookie" } }
    );
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type IssueBody = { programId?: string; refType?: string };

// Claim/issue a certificate. Idempotent per (userId, refType, refId): calling
// twice returns the same record. Requires eligibility (>=1 completed test).
export async function POST(req: NextRequest) {
  const { userId, orgId, studentName, memberId } = await whoami(req);
  if (!userId) return NextResponse.json({ error: "Sign in to claim a certificate" }, { status: 401 });

  const b = (await req.json().catch(() => ({}))) as IssueBody;
  const refId = String(b.programId || "");
  const refType = (b.refType || "PROGRAM").toUpperCase();
  if (!refId) return NextResponse.json({ error: "Program is required" }, { status: 400 });

  try {
    const db = await getDb();

    const finished = await db
      .collection("userentityattempts")
      .countDocuments({ userId, entityType: "TEST" });
    if (finished < 1)
      return NextResponse.json(
        { error: "Complete at least one test in this program first." },
        { status: 403 }
      );

    const existing: any = await db
      .collection(CERTS_COLL)
      .findOne({ userId, refType, refId, recordState: "ACTIVE" });
    if (existing) return NextResponse.json({ certificate: mapCert(existing), reused: true });

    let refName = "Program";
    if (refType === "PROGRAM" && ObjectId.isValid(refId)) {
      const p: any = await db.collection("orgprograms").findOne({ _id: new ObjectId(refId) });
      refName = p?.name || p?.cName || "Program";
    }

    const now = Date.now();
    const doc = {
      _id: new ObjectId(),
      orgId,
      userId,
      memberId,
      refType,
      refId,
      refName,
      studentName,
      serial: serial(),
      issuedAt: now,
      recordState: "ACTIVE",
      timeCreated: now,
    };
    await db.collection(CERTS_COLL).insertOne(doc);
    return NextResponse.json({ certificate: mapCert(doc) });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Could not issue certificate" }, { status: 500 });
  }
}
