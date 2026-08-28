import { MongoClient, Db } from "mongodb";
import { MONGO_URI, MONGO_DB } from "./config";

// Reuse a single client across hot-reloads / requests (Next.js dev re-imports).
// mongodb v3 driver — required for the legacy Mongo 3.4 server (wire v5).
let clientPromise: Promise<MongoClient> | null = null;

function getClient(): Promise<MongoClient> {
  if (!clientPromise) {
    clientPromise = MongoClient.connect(MONGO_URI, {
      useUnifiedTopology: true,
      serverSelectionTimeoutMS: 5000,
    });
  }
  return clientPromise;
}

export async function getDb(): Promise<Db> {
  const client = await getClient();
  return client.db(MONGO_DB);
}
