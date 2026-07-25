import type { NextRequest } from "next/server";
import { getDb } from "@/lib/mongo";

// Records real login events so Device Management shows genuine last-seen /
// device info instead of a derived guess. Fire-and-forget — never blocks auth.
export const LOGINS_COLL = "logins";

function parseDevice(ua: string): "WEB" | "ANDROID" | "IOS" {
  const s = ua.toLowerCase();
  if (s.includes("android")) return "ANDROID";
  if (s.includes("iphone") || s.includes("ipad") || s.includes("ios")) return "IOS";
  return "WEB";
}

export async function recordLogin(
  req: NextRequest,
  info: { orgId: string; userId: string; memberId?: string | null; method: string }
): Promise<void> {
  try {
    const ua = req.headers.get("user-agent") || "";
    const ip =
      req.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ||
      req.headers.get("x-real-ip") ||
      "";
    const db = await getDb();
    await db.collection(LOGINS_COLL).insertOne({
      orgId: info.orgId,
      userId: info.userId,
      memberId: info.memberId || null,
      device: parseDevice(ua),
      userAgent: ua.slice(0, 300),
      ip,
      method: info.method,
      at: Date.now(),
    });
  } catch {
    // Best-effort only.
  }
}
