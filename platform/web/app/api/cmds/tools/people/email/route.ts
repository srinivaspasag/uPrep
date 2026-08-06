import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { sendEmail } from "@/lib/messaging";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// "Send Emails" — legacy's QrPeople.sendEmailsPopup/sendEmail
// (/members/sendEmailsToStudents): bulk-email the students of a specific
// Program+Center+Section. MANAGER-only, matching legacy's gate.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if ((session?.profile || "").trim().toUpperCase() !== "MANAGER")
    return NextResponse.json({ error: "Only institute admins can send emails." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as { sectionId?: string; subject?: string; text?: string };
  const sectionId = b.sectionId || "";
  const subject = (b.subject || "").trim();
  const text = (b.text || "").trim();
  if (!sectionId || !subject || !text)
    return NextResponse.json({ error: "sectionId, subject and text are required" }, { status: 400 });

  const orgId = await resolveOrgId(req, null);

  try {
    const db = await getDb();
    const members = await db
      .collection("orgmembers")
      .find({ orgId, profile: "STUDENT", "programMemberships.sectionId": sectionId } as any)
      .toArray();

    let notified = 0;
    let delivered = 0;
    for (const m of members as any[]) {
      if (!m.email) continue;
      notified++;
      const res = await sendEmail(m.email, subject, text, { sectionId });
      if (res.delivered) delivered++;
    }

    return NextResponse.json({ ok: true, notified, delivered });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Send failed" }, { status: 500 });
  }
}
