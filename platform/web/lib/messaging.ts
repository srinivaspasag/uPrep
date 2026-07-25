import type { Db } from "mongodb";
import { getDb } from "@/lib/mongo";

// Pluggable outbound messaging (email + SMS) for the new stack. The legacy app
// leaned on external e-mail/SMS gateways; here we keep a provider-agnostic
// dispatcher so real delivery can be switched on with env vars, while dev/demo
// keeps working with no credentials (messages are persisted + logged).
//
// Providers (per channel), selected by env:
//   EMAIL_PROVIDER / SMS_PROVIDER = "log" (default) | "webhook"
//   NOTIFY_WEBHOOK_URL  -> POST { channel, to, subject, text } for real delivery
// A relay behind NOTIFY_WEBHOOK_URL (SendGrid/SES/Twilio/etc.) turns these into
// actual emails/texts without changing app code.

export type Channel = "email" | "sms";

export type OutboundMessage = {
  channel: Channel;
  to: string;
  subject?: string;
  text: string;
  meta?: Record<string, unknown>;
};

export type DispatchResult = {
  ok: boolean;
  delivered: boolean; // true only when a real provider accepted it
  provider: string;
  error?: string;
};

const MESSAGES_COLL = "messages";

function providerFor(channel: Channel): string {
  const v = channel === "email" ? process.env.EMAIL_PROVIDER : process.env.SMS_PROVIDER;
  return (v || "log").toLowerCase();
}

async function record(db: Db, msg: OutboundMessage, res: DispatchResult) {
  try {
    await db.collection(MESSAGES_COLL).insertOne({
      channel: msg.channel,
      to: msg.to,
      subject: msg.subject || null,
      text: msg.text,
      meta: msg.meta || null,
      provider: res.provider,
      delivered: res.delivered,
      error: res.error || null,
      timeCreated: Date.now(),
    });
  } catch {
    // Never let audit-write failure break the caller.
  }
}

async function viaWebhook(msg: OutboundMessage): Promise<DispatchResult> {
  const url = process.env.NOTIFY_WEBHOOK_URL;
  if (!url) return { ok: false, delivered: false, provider: "webhook", error: "NOTIFY_WEBHOOK_URL not set" };
  try {
    const r = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ channel: msg.channel, to: msg.to, subject: msg.subject, text: msg.text }),
    });
    if (!r.ok) return { ok: false, delivered: false, provider: "webhook", error: `HTTP ${r.status}` };
    return { ok: true, delivered: true, provider: "webhook" };
  } catch (e: any) {
    return { ok: false, delivered: false, provider: "webhook", error: e?.message || "webhook failed" };
  }
}

export async function dispatch(msg: OutboundMessage): Promise<DispatchResult> {
  const provider = providerFor(msg.channel);
  let res: DispatchResult;
  if (provider === "webhook") {
    res = await viaWebhook(msg);
  } else {
    // "log" provider: persisted + logged, not actually delivered. Keeps dev/demo
    // flows (password reset links, notifications) working with zero config.
    console.log(`[messaging:${msg.channel}] to=${msg.to} subject=${msg.subject || ""} :: ${msg.text}`);
    res = { ok: true, delivered: false, provider: "log" };
  }
  try {
    const db = await getDb();
    await record(db, msg, res);
  } catch {
    /* ignore */
  }
  return res;
}

export async function sendEmail(
  to: string,
  subject: string,
  text: string,
  meta?: Record<string, unknown>
): Promise<DispatchResult> {
  if (!to) return { ok: false, delivered: false, provider: "none", error: "no recipient" };
  return dispatch({ channel: "email", to, subject, text, meta });
}

export async function sendSms(to: string, text: string, meta?: Record<string, unknown>): Promise<DispatchResult> {
  if (!to) return { ok: false, delivered: false, provider: "none", error: "no recipient" };
  return dispatch({ channel: "sms", to, text, meta });
}
