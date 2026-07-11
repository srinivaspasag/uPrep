# UPrep — Legacy UI Status (Client Brief)

**Purpose:** clear, accurate status of the existing (legacy) user interface.
**Status:** for client discussion

---

## Verdict (one line)

**The legacy UI runs and can be demonstrated, but it is not fit for production** — it is
built on technology that is ~10+ years end-of-life, and some core content workflows are broken.

> In short: it's a good **proof that the product works**, not a platform you can safely launch on.

---

## What actually works today

We brought the legacy application up locally and confirmed the following **do** function:

- **The applications run and screens render** — the LMS portal, the CMDS content-management
  console, the tools console, and the learning app all load.
- **Login / authentication** works (student and admin logins verified).
- **Navigation and browsing** works — programs, centers, batches, subjects, question bank.
- **Content *authoring* screens** in CMDS work — you can create tests, add questions, upload docs.
- **Seeded/demo content is viewable** where it was placed directly.

So for a **walkthrough or demo**, the product's look, flow, and feature set can be shown end to end.

---

## What does NOT work / the real problems

### 1. Content publishing pipeline is broken
The most important workflow — **publishing authored content so students see it in their
library** — does not complete. Content created in CMDS does **not** appear in the student
library. Root causes (confirmed in code):
- The student library reads from a **search index that is never created** in this environment.
- The internal "publish to library" API has a **software defect** (a form-binding bug) that
  rejects the request.

**Impact:** a teacher can author content, but students won't receive it — a blocking defect for real use.

### 2. The technology stack is end-of-life
| Layer | Version | Age / status |
|---|---|---|
| Web framework | Play 1.2.4 | ~2011 · long unsupported |
| Language runtime | Java 6 / 7 | unsupported, security-EOL |
| Database | MongoDB 3.4 | end-of-life |
| Search | ElasticSearch 0.20.6 | ~2013 · end-of-life |

**Impact:** no security patches, no vendor support, very hard to hire for, and incompatible
with modern cloud/managed services.

### 3. Runtime & portability constraints
- Runs only on older **x86** infrastructure (needed slow emulation on modern Apple hardware).
- Fragile startup — several services must be hand-started in a specific order.

### 4. Operational gaps
- Credentials were found **hard-coded in source** (a security issue).
- No modern monitoring, CI/CD, automated tests, or supportability.

---

## Why we can't "just use it as-is"

The legacy UI is tightly coupled to the legacy backend and its end-of-life search/database
stack. Making it production-safe would mean **modernizing the very foundations it stands on**
(framework, language, database, search) — effectively a rewrite of the platform underneath it,
while still carrying the broken workflows. That is more costly and riskier than moving forward
on the already-modernized backend.

---

## The path forward

There are **two codebases**:
- **Legacy full app** (the UI you can see) — end-of-life; **good for demo, not production**.
- **Modern backend** (`nextgen`, Spring Boot / Java 17) — cloud-ready, but it is **API-only (no UI yet)**.

**Recommendation:**
1. **Use the legacy UI only for demonstration** of the product vision to the client.
2. **Build the production UI on the modern backend** — choose one of:
   - **Rebuild** a fresh, modern web UI on the existing APIs (recommended — clean, supportable), or
   - **Port** selected legacy screens onto the modern stack, or
   - launch **website-only** initially and add the app UI in a phase.
3. Fix the two P0 items regardless: **content pipeline** and **committed secrets**.

---

## Client talking points (summary)

- "The product **works and can be demonstrated today**."
- "The existing interface sits on **10+ year-old, unsupported technology** and has a **broken
  content-publishing step**, so it is **not safe to launch on**."
- "We already have a **modernized backend**; the remaining work is a **modern UI** on top of it,
  plus loading real content — that's the recommended, lower-risk path to production."

---
_Companion docs: `UPREP_FINAL_BRIEF.md`, `UPREP_GO_LIVE_CHECKLIST.md`, `UPREP_CONTENT_AI_MAINTENANCE_REVIEW.md`._
