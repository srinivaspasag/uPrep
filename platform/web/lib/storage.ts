import { promises as fs } from "fs";
import path from "path";
import crypto from "crypto";

// Local-disk storage for CMDS uploads (documents, videos, images).
// Files land in public/uploads and are served statically at /uploads/<name>.
// This is intentionally behind a small interface so it can be swapped for S3
// (signed uploads + CloudFront) when deploying to AWS without touching callers.

const UPLOAD_DIR = path.join(process.cwd(), "public", "uploads");

export type StoredFile = {
  url: string; // public URL path, e.g. /uploads/ab12.mp4
  fileName: string; // original name
  storedName: string; // on-disk name
  size: number;
  contentType: string;
};

export async function saveUpload(file: File): Promise<StoredFile> {
  await fs.mkdir(UPLOAD_DIR, { recursive: true });

  const buf = Buffer.from(await file.arrayBuffer());
  const ext = path.extname(file.name || "") || guessExt(file.type);
  const storedName = `${Date.now()}-${crypto.randomBytes(6).toString("hex")}${ext}`;
  await fs.writeFile(path.join(UPLOAD_DIR, storedName), buf);

  return {
    url: `/uploads/${storedName}`,
    fileName: file.name || storedName,
    storedName,
    size: buf.length,
    contentType: file.type || "application/octet-stream",
  };
}

// Save raw content (e.g. a generated CSV export) to the uploads dir.
export async function saveBuffer(
  content: string | Buffer,
  fileName: string,
  contentType = "application/octet-stream"
): Promise<StoredFile> {
  await fs.mkdir(UPLOAD_DIR, { recursive: true });
  const buf = typeof content === "string" ? Buffer.from(content, "utf8") : content;
  const ext = path.extname(fileName) || "";
  const storedName = `${Date.now()}-${crypto.randomBytes(6).toString("hex")}${ext}`;
  await fs.writeFile(path.join(UPLOAD_DIR, storedName), buf);
  return {
    url: `/uploads/${storedName}`,
    fileName,
    storedName,
    size: buf.length,
    contentType,
  };
}

function guessExt(mime: string): string {
  const map: Record<string, string> = {
    "video/mp4": ".mp4",
    "video/webm": ".webm",
    "application/pdf": ".pdf",
    "image/png": ".png",
    "image/jpeg": ".jpg",
  };
  return map[mime] || "";
}
