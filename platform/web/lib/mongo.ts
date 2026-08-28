import { MongoClient, Db } from "mongodb";
import { MONGO_URI, MONGO_DB } from "./config";
import { mockDb } from "./mongo-mock";

// Reuse a single client across hot-reloads / requests (Next.js dev re-imports).
// mongodb v3 driver — required for the legacy Mongo 3.4 server (wire v5).
let clientPromise: Promise<MongoClient> | null = null;
let useMock = false;

async function getClient(): Promise<MongoClient | null> {
  if (useMock) return null;
  if (!clientPromise) {
    clientPromise = MongoClient.connect(MONGO_URI, {
      useUnifiedTopology: true,
      serverSelectionTimeoutMS: 1500,
    }).catch((err) => {
      console.warn("MongoDB not reachable at", MONGO_URI, "- using local mock DB fallback:", err?.message);
      useMock = true;
      clientPromise = null;
      return null as any;
    });
  }
  return clientPromise;
}

export async function getDb(): Promise<Db> {
  try {
    const client = await getClient();
    if (client) {
      return client.db(MONGO_DB);
    }
  } catch {
    useMock = true;
  }
  return mockDb as unknown as Db;
}
