import { ObjectId } from "mongodb";
import { hashPassword } from "./password";
import { DEFAULT_ORG_ID } from "./config";

// In-memory collection fallback for local dev when MongoDB server is offline
class MockCollection {
  name: string;
  docs: any[];

  constructor(name: string, initialDocs: any[] = []) {
    this.name = name;
    this.docs = initialDocs;
  }

  private matchFilter(doc: any, filter: any): boolean {
    if (!filter || Object.keys(filter).length === 0) return true;
    for (const key of Object.keys(filter)) {
      if (key === "$or" && Array.isArray(filter.$or)) {
        const anyMatch = filter.$or.some((sub: any) => this.matchFilter(doc, sub));
        if (!anyMatch) return false;
        continue;
      }
      const val = filter[key];
      if (val && typeof val === "object" && "$exists" in val) {
        const exists = doc[key] !== undefined;
        if (exists !== val.$exists) return false;
        if (val.$ne !== undefined && doc[key] === val.$ne) return false;
        continue;
      }
      if (key === "_id") {
        const docId = String(doc._id);
        const filterId = String(val);
        if (docId !== filterId) return false;
        continue;
      }
      if (doc[key] !== val) return false;
    }
    return true;
  }

  find(filter: any = {}) {
    let result = this.docs.filter((d) => this.matchFilter(d, filter));
    const cursor = {
      sort: (sortObj: any) => {
        const key = Object.keys(sortObj || {})[0];
        if (key) {
          const dir = sortObj[key];
          result.sort((a, b) => {
            if (a[key] < b[key]) return dir === -1 ? 1 : -1;
            if (a[key] > b[key]) return dir === -1 ? -1 : 1;
            return 0;
          });
        }
        return cursor;
      },
      limit: (n: number) => {
        result = result.slice(0, n);
        return cursor;
      },
      toArray: async () => [...result],
    };
    return cursor;
  }

  async findOne(filter: any = {}) {
    return this.docs.find((d) => this.matchFilter(d, filter)) || null;
  }

  async insertOne(doc: any) {
    const inserted = { ...doc };
    if (!inserted._id) inserted._id = new ObjectId();
    this.docs.push(inserted);
    return { insertedId: inserted._id };
  }

  async updateOne(filter: any, update: any, options: any = {}) {
    const idx = this.docs.findIndex((d) => this.matchFilter(d, filter));
    if (idx !== -1) {
      if (update.$set) Object.assign(this.docs[idx], update.$set);
      if (update.$inc) {
        for (const k of Object.keys(update.$inc)) {
          this.docs[idx][k] = (this.docs[idx][k] || 0) + update.$inc[k];
        }
      }
      return { modifiedCount: 1 };
    } else if (options.upsert) {
      const newDoc = { ...(update.$setOnInsert || {}), ...(update.$set || {}), ...filter };
      if (!newDoc._id) newDoc._id = new ObjectId();
      this.docs.push(newDoc);
      return { upsertedCount: 1, upsertedId: newDoc._id };
    }
    return { modifiedCount: 0 };
  }

  async deleteOne(filter: any) {
    const idx = this.docs.findIndex((d) => this.matchFilter(d, filter));
    if (idx !== -1) {
      this.docs.splice(idx, 1);
      return { deletedCount: 1 };
    }
    return { deletedCount: 0 };
  }

  async countDocuments(filter: any = {}) {
    return this.docs.filter((d) => this.matchFilter(d, filter)).length;
  }
}

class MockDb {
  collections: Map<string, MockCollection> = new Map();

  constructor() {
    this.initDefaultData();
  }

  private initDefaultData() {
    const defaultOrg = {
      _id: new ObjectId(DEFAULT_ORG_ID),
      name: "UPrep Demo Institute",
      fullName: "UPrep Demo Institute",
      recordState: "ACTIVE",
    };

    const superAdminMember = {
      _id: new ObjectId("5874a52bc92ed65e3defc7e6"),
      orgId: DEFAULT_ORG_ID,
      memberId: "superadmin",
      firstName: "Super",
      lastName: "Admin",
      email: "superadmin@uprep.local",
      profile: "MANAGER",
      isSuperAdmin: true,
      authType: "LOCAL",
      passwordHash: hashPassword("Uprep@12345"),
      recordState: "ACTIVE",
    };

    const superAdminMember2 = {
      _id: new ObjectId("5874a52bc92ed65e3defc7e7"),
      orgId: DEFAULT_ORG_ID,
      memberId: "SUPER_ADMIN",
      firstName: "Super",
      lastName: "Admin",
      email: "admin@uprep.local",
      profile: "MANAGER",
      isSuperAdmin: true,
      authType: "LOCAL",
      passwordHash: hashPassword("Uprep@12345"),
      recordState: "ACTIVE",
    };

    const demoCoupons = [
      {
        _id: new ObjectId(),
        orgId: DEFAULT_ORG_ID,
        code: "WELCOME20",
        percentOff: 20,
        amountOffCents: null,
        active: true,
        validUntil: new Date(Date.now() + 86400000 * 30).toISOString(),
        redeemed: 5,
        timeCreated: Date.now() - 86400000 * 2,
      },
      {
        _id: new ObjectId(),
        orgId: DEFAULT_ORG_ID,
        code: "EXPIRED50",
        percentOff: 50,
        amountOffCents: null,
        active: true,
        validUntil: new Date(Date.now() - 86400000 * 5).toISOString(),
        redeemed: 12,
        timeCreated: Date.now() - 86400000 * 10,
      },
    ];

    const demoProducts = [
      {
        _id: new ObjectId("5874a52bc92ed65e3defc7e8"),
        orgId: DEFAULT_ORG_ID,
        courseId: "course-101",
        name: "Complete Physics & Mathematics Masterclass",
        priceCents: 499900,
        currency: "INR",
        recordState: "ACTIVE",
        timeCreated: Date.now(),
      },
    ];

    this.collections.set("organizations", new MockCollection("organizations", [defaultOrg]));
    this.collections.set("orgmembers", new MockCollection("orgmembers", [superAdminMember, superAdminMember2]));
    this.collections.set("coupons", new MockCollection("coupons", demoCoupons));
    this.collections.set("products", new MockCollection("products", demoProducts));
    this.collections.set("invoices", new MockCollection("invoices", []));
  }

  collection(name: string): any {
    if (!this.collections.has(name)) {
      this.collections.set(name, new MockCollection(name));
    }
    return this.collections.get(name);
  }
}

// Global singleton mock database instance across hot reloads
const globalForMock = global as unknown as { mockDbInstance?: MockDb };
export const mockDb = globalForMock.mockDbInstance || new MockDb();
if (process.env.NODE_ENV !== "production") globalForMock.mockDbInstance = mockDb;
