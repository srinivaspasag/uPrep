// Resolves external video links (YouTube / Vimeo) into an embeddable form.
// Mirrors the legacy CMDS "Add by URL" feature (ExternalContentSrc + data
// collectors), but derives the embed URL directly — no backend call needed,
// since YouTube/Vimeo embed URLs are deterministic from the video id.

export type VideoProvider = "YOUTUBE" | "VIMEO";

export type ParsedVideo = {
  provider: VideoProvider;
  videoId: string;
  embedUrl: string; // iframe src (inline player)
  externalUrl: string; // canonical watch page (open in new tab)
};

const YOUTUBE_PATTERNS: RegExp[] = [
  /(?:youtube\.com\/watch\?[^#]*\bv=)([A-Za-z0-9_-]{6,})/i,
  /(?:youtu\.be\/)([A-Za-z0-9_-]{6,})/i,
  /(?:youtube\.com\/embed\/)([A-Za-z0-9_-]{6,})/i,
  /(?:youtube\.com\/shorts\/)([A-Za-z0-9_-]{6,})/i,
  /(?:youtube\.com\/v\/)([A-Za-z0-9_-]{6,})/i,
];

const VIMEO_PATTERNS: RegExp[] = [
  /(?:player\.vimeo\.com\/video\/)(\d+)/i,
  /(?:vimeo\.com\/)(?:channels\/[^/]+\/)?(\d+)/i,
];

function firstMatch(url: string, patterns: RegExp[]): string | null {
  for (const re of patterns) {
    const m = url.match(re);
    if (m && m[1]) return m[1];
  }
  return null;
}

// Parses a YouTube or Vimeo URL. Returns null if the URL isn't a recognized
// video link (callers should surface a "use YouTube or Vimeo" message).
export function parseVideoUrl(input: string): ParsedVideo | null {
  const url = (input || "").trim();
  if (!url) return null;

  const ytId = firstMatch(url, YOUTUBE_PATTERNS);
  if (ytId) {
    return {
      provider: "YOUTUBE",
      videoId: ytId,
      embedUrl: `https://www.youtube.com/embed/${ytId}`,
      externalUrl: `https://www.youtube.com/watch?v=${ytId}`,
    };
  }

  const vimeoId = firstMatch(url, VIMEO_PATTERNS);
  if (vimeoId) {
    return {
      provider: "VIMEO",
      videoId: vimeoId,
      embedUrl: `https://player.vimeo.com/video/${vimeoId}`,
      externalUrl: `https://vimeo.com/${vimeoId}`,
    };
  }

  return null;
}
