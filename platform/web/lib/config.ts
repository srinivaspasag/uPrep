// Backend service base URLs. Defaults target the LEGACY backend (which has real
// UPrep data + working auth today). Override via env to point at nextgen once the
// legacy->nextgen data migration (ObjectId vs String _id) is done.
export const API = {
  org: process.env.ORG_SERVICE_URL || "http://localhost:19012",
  user: process.env.USER_SERVICE_URL || "http://localhost:19011",
  content: process.env.CONTENT_SERVICE_URL || "http://localhost:19013",
  board: process.env.BOARD_SERVICE_URL || "http://localhost:19016",
};

// Default org context for the demo (UPrep). Lets users log in with just a memberId.
export const DEFAULT_ORG_ID =
  process.env.NEXT_PUBLIC_DEFAULT_ORG_ID || "5874a52bc92ed65e3defc7e5";

// Direct MongoDB access (browse/listing) — bypasses the ElasticSearch-backed
// search endpoints, which aren't indexed on the legacy stack. Detail + take-test
// still go through the real content service (see API.content).
export const MONGO_URI =
  process.env.MONGO_URI || "mongodb://localhost:27117";
export const MONGO_DB = process.env.MONGO_DB || "localvedantu";

// Identity every legacy backend request must carry (AbstractAppCheckReq).
export const CALLING_APP = "web-app";
export const CALLING_APP_ID = "web-app";
