// Seed a dummy super user (org admin) that the web app accepts for login + /cmds.
//
// A "super user" here is the org admin: an ACTIVE `orgmembers` doc with
// profile=MANAGER, memberId=SUPER_ADMIN and isSuperAdmin=true, plus an
// `organizations` doc whose adminUserId points at that member (so the Java
// checkIfSuperAdmin also returns true). Passwords use the same scrypt scheme as
// platform/web/lib/password.ts so local Mongo login works with no legacy backend.
//
// Usage (from platform/web):
//   node scripts/seed-superuser.js
//   SEED_PASSWORD='MyPass123' SEED_EMAIL=admin@demo.local node scripts/seed-superuser.js
//   npm run seed:superuser
//
// Env (all optional):
//   MONGO_URI     default mongodb://localhost:27117   (same default as lib/config.ts)
//   MONGO_DB      default localvedantu
//   SEED_ORG_ID   default 5874a52bc92ed65e3defc7e5    (NEXT_PUBLIC_DEFAULT_ORG_ID)
//   SEED_MEMBER_ID default SUPER_ADMIN
//   SEED_EMAIL    default superadmin@uprep.local
//   SEED_PASSWORD default Uprep@12345
//   SEED_ORG_NAME default Demo Org
//   SEED_CREATE_ORG default true   (set false to only add the member to an existing org)

const { MongoClient, ObjectId } = require("mongodb");
const { scryptSync, randomBytes } = require("crypto");

const MONGO_URI = process.env.MONGO_URI || "mongodb://localhost:27117";
const MONGO_DB = process.env.MONGO_DB || "localvedantu";
const ORG_ID = process.env.SEED_ORG_ID || "5874a52bc92ed65e3defc7e5";
const MEMBER_ID = process.env.SEED_MEMBER_ID || "SUPER_ADMIN";
const EMAIL = (process.env.SEED_EMAIL || "superadmin@uprep.local").trim().toLowerCase();
const PASSWORD = process.env.SEED_PASSWORD || "Uprep@12345";
const ORG_NAME = process.env.SEED_ORG_NAME || "Demo Org";
const CREATE_ORG = (process.env.SEED_CREATE_ORG || "true").toLowerCase() !== "false";

// Mirror of platform/web/lib/password.ts -> hashPassword().
function hashPassword(password) {
  const salt = randomBytes(16);
  const hash = scryptSync(password, salt, 64);
  return `scrypt$${salt.toString("hex")}$${hash.toString("hex")}`;
}

function toObjId(id) {
  try {
    return new ObjectId(id);
  } catch {
    return id;
  }
}

async function main() {
  const client = await MongoClient.connect(MONGO_URI, {
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000,
  });
  try {
    const db = client.db(MONGO_DB);
    const orgmembers = db.collection("orgmembers");
    const organizations = db.collection("organizations");
    const now = Date.now();

    // 1) Upsert the super-admin member (idempotent on orgId + memberId).
    const existing = await orgmembers.findOne({ orgId: ORG_ID, memberId: MEMBER_ID });
    const memberObjId = existing ? existing._id : new ObjectId();
    const userId = existing?.userId || `LOCAL_${memberObjId.toHexString()}`;

    await orgmembers.updateOne(
      { orgId: ORG_ID, memberId: MEMBER_ID },
      {
        $set: {
          orgId: ORG_ID,
          memberId: MEMBER_ID,
          userId,
          firstName: "Super",
          lastName: "Admin",
          email: EMAIL,
          profile: "MANAGER",
          isSuperAdmin: true,
          authType: "LOCAL",
          passwordHash: hashPassword(PASSWORD),
          recordState: "ACTIVE",
          lastUpdated: now,
        },
        $setOnInsert: { _id: memberObjId, timeCreated: now },
      },
      { upsert: true }
    );

    // 2) Ensure the org exists and points its admin at this member (so the Java
    //    checkIfSuperAdmin path also resolves; the web path only needs the flag).
    if (CREATE_ORG) {
      await organizations.updateOne(
        { _id: toObjId(ORG_ID) },
        {
          $set: {
            adminUserId: userId,
            adminOrgMemberId: memberObjId.toHexString(),
            status: "APPROVED",
            studentPageStatus: "APPROVED",
            recordState: "ACTIVE",
            lastUpdated: now,
          },
          $setOnInsert: {
            name: ORG_NAME,
            fullName: ORG_NAME,
            type: "COLLEGE",
            authType: "VEDANTU",
            timeCreated: now,
          },
        },
        { upsert: true }
      );
    }

    console.log("Dummy super user ready.");
    console.log("  DB           :", `${MONGO_URI} / ${MONGO_DB}`);
    console.log("  Org ID       :", ORG_ID);
    console.log("  Login ID     :", `${ORG_ID}:${MEMBER_ID}`, `(or just "${MEMBER_ID}" if it is the default org)`);
    console.log("  Email        :", EMAIL);
    console.log("  Password     :", PASSWORD);
    console.log("  Profile      : MANAGER (isSuperAdmin=true)  ->  /cmds access granted");
  } finally {
    await client.close();
  }
}

main().catch((e) => {
  console.error("Seed failed:", e.message || e);
  process.exit(1);
});
