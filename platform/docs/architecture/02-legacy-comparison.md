# uPrep — Legacy vs. Rebuild Comparison

| | |
|---|---|
| **Purpose** | State precisely what "legacy" refers to, what has been replaced, what still runs underneath the rebuild, and every place this project found the rebuild's assumed behavior didn't match legacy's actual behavior. |
| **"Legacy" precisely means** | The two Play Framework, server-rendered web applications `ui/cmds-app` (staff console) and `ui/learn-app` (student app) in `legacy/lms-master`, plus the domain services they and the rebuild both depend on. |
| **Confidence note** | Every row below is either (a) confirmed by reading the cited legacy source file, (b) confirmed by comparing behavior against live legacy service responses, or (c) explicitly marked as a project decision rather than a legacy-fidelity claim. Nothing here is asserted from general assumption about what a system like this "probably" does. |

---

## 1. The shape of "legacy" itself

The legacy monorepo is a real, if aging, microservices architecture — 12 top-level domains, each usually split into `-commons` (shared models), `-mgmt` (business logic library), `-services` (deployable Play HTTP API):

`billing`, `board`, `cmds`, `comm`, `commons`, `content`, `event`, `organization`, `social`, `static-resources`, `ui`, `user`, `viewer`.

Of these, the current production deployment starts **exactly four** backend services (`user-services`, `organization-services`, `content-services`, `board-services` — see `platform/deploy/entrypoint.sh`) and the rebuild calls **exactly those same four** (`lib/config.ts`). The two legacy **web** applications that used to be the actual front door — `ui/cmds-app` (controllers: `QrPeople`, `QrPrograms`, `QrModules`, `QrDevices`, `QrNotification`, `QrBilling`, `QrSchedule`, `QrChannels`, `QrPlans`, `QrProducts`, `QrInventory`, `QrSaleDetails`, `Widgets`, and more) and `ui/learn-app` (controllers: `MyContents`, `Tests`, `Institute`, `Profile`, `Payments`, `Invoices`, `Register`, `Security`, `UserMessages`, `Share`) — are **not running in production anymore**. The Next.js app has fully replaced them as the thing users' browsers talk to; it just still reaches into the four data services above for what it hasn't natively reimplemented.

`billing`, `comm`, `social`, `event`, `viewer`, and `cmds/cmds-services` exist in source but are dormant in this deployment — not proxied, not started. Any feature that lived only in those modules is, as far as this rebuild is concerned, not currently present (its data may still exist in Mongo, but nothing in the current stack reads or writes it through those services).

## 2. Architecture, side by side

```mermaid
flowchart TB
    subgraph Before["Legacy (original)"]
        direction TB
        B1["Browser"] --> B2["ui/cmds-app\n(Play, server-rendered HTML)"]
        B1 --> B3["ui/learn-app\n(Play, server-rendered HTML)"]
        B2 & B3 --> B4["user / org / content / board\n-services (Play)"]
        B4 --> B5[("MongoDB")]
        B2 & B3 -.->|search| B6["Elasticsearch"]
    end

    subgraph After["Rebuild (current)"]
        direction TB
        A1["Browser / Android"] --> A2["Next.js 14 App\n(replaces BOTH ui/cmds-app and ui/learn-app)"]
        A2 -->|direct driver, no search index| A3[("MongoDB")]
        A2 -->|still proxied for un-migrated logic| A4["user / org / content / board\n-services (same Play processes)"]
        A4 --> A3
    end
```

The single biggest structural change: **two separate legacy web apps collapsed into one Next.js app**, which now also absorbs everything Elasticsearch used to do for browse/listing (by querying Mongo directly instead — a deliberate trade-off, see §5).

## 3. Feature-area migration status

Status legend: **Replaced** = fully reimplemented, legacy web app no longer used for this · **Replaced+Enhanced** = reimplemented and then extended beyond legacy's original behavior · **New** = did not exist in legacy at all · **Removed** = existed in the rebuild at some point, deliberately taken back out.

| Area | Legacy | Rebuild | Status |
|---|---|---|---|
| Staff login / access gate | `Security.checkAccess()` interceptor in `ui/cmds-app` | `middleware.ts` edge gate, same staff-profile rule (`MANAGER`/`TEACHER`/`EDITOR`/`SALESPERSON`) | Replaced |
| People Management | `QrPeople.java` | `/cmds/tools/people` + `/cmds/tools/people/bulk` | Replaced+Enhanced (CSV bulk import with auto-generated credentials is new) |
| Academic Structure | Implicit in `Institute.java` / org data model | `/cmds/tools/academic` dedicated screen | Replaced+Enhanced |
| Content Resources / Question Bank | `QrModules`, `QrDocuments`, general CMDS content screens | `/cmds` resources + `/cmds/questions` | Replaced+Enhanced (chapter/topic display resolved live via board-service, added this session) |
| Test creation | `QrTests.createTest()` (manual) / `createTestAuto()` (auto), **one shared Setup screen**, forking on an auto-generate flag | `/cmds/tests/new` — **now correctly mirrors this**: one Setup → Subjects & Types → Chapters flow with a mode toggle | Replaced — but see §4 for how this project initially got it wrong |
| Instant Test Generator (multi-subject, difficulty split, review & replace) | Not confirmed as existing in legacy in this form | `/cmds/tests/new` auto mode | New |
| Course Packs (bundle-grant courses across orgs) | Not part of legacy's model | Built, then explicitly removed by product decision ("no concept of Other Courses" / Academic Structure supersedes it) | Removed |
| Test-taking (student) | `Tests.java` (`testPage`, `testPageDirect`, `leaderBoard`) | `/test/[id]` | Replaced+Enhanced (see §4, one-attempt rule) |
| Digital Library (student content browsing) | `MyContents.java`, subject-card tree (`Library/subjects.html`) | `/learn/courses` | Replaced — rebuilt to match legacy's real structure after an earlier version incorrectly duplicated it with a second flat "Library" section (removed) |
| Leaderboard / toppers | `_getToppers()` / `toppersData`, **passed only into the teacher template**, never the student template | Rebuild initially showed peer leaderboard data to students (unverified assumption) | Corrected to match legacy: staff-only, removed from student surfaces |
| Doubts Forum | `social`/legacy doubt flow (module not actively proxied — see §1) | `/learn/doubts`, direct Mongo (`discussions`/`answers`) | Replaced (native data model, not a proxy to the dormant `social` service) |
| Student Analytics | Legacy has a basic "Result Analytics" list screen | `/learn/analytics` — score trend, per-subject accuracy (via board-tree resolution), per-question-type accuracy, strengths/focus-areas | New (far beyond legacy's flat table) |
| Sidebar navigation (student) | `header.html` + `conf/messages` — exactly 5 items: Digital Library, Programs, Doubts Forum, Analytics, Recent Activity | Rebuild had accumulated 14 nav items before being trimmed back to legacy's real 5 | Corrected to match legacy |
| Mobile app | No evidence of a legacy mobile app in this repository | Native Android app (Compose), evolving from WebView shell to native screens + offline downloads | New |

## 4. Business-rule fidelity findings

These are cases where this project's assumption about "what legacy does" was wrong, discovered by reading legacy source or live legacy data, and then corrected. Recorded here because they're exactly the kind of drift a "rebuild matches legacy" claim needs to survive scrutiny on.

1. **Test retake behavior.** Assumed: retaking a test should just work and analytics should reflect all attempts. Actual legacy rule, read directly from `AnalyticsManager.startAttempt()`: `isMultiAttemptAllowed()` is hardcoded `return false`, but the real behavior is more specific than "blocked, always" — a prior attempt with `endTime == 0` (abandoned mid-test) is silently **resumed** (same attempt id, status reset to ONGOING), and it's only a prior attempt that actually *finished* that triggers `MULTI_ATTEMPTS_NOT_ALLOWED`. `recordAttempt()` re-checks this on every single answer, not just at start (FINISHED/PAUSED/RESUMED states are each rejected explicitly). Legacy's UI never lets a student reach the rejection in the finished case — it checks status up front (`Tests.java testPageDirect` / `_isReAttemptTest()`) and routes straight to the existing result. The rebuild now matches this precisely (`alreadyAttempted` flag + a read-back-only result endpoint), not just the coarser "one attempt ever" version originally assumed.
2. **Leaderboard visibility.** Assumed (first pass): showing peers' names/scores to students matches legacy's "real gamification feature." Actual: `toppersData` is computed for every role in `MyContents.java testPage()`, but only ever passed into `postTestTeacherPage.html`'s render call — the student template never receives it. Corrected by removing leaderboard/peer data from student-facing surfaces entirely, not just filtering it.
3. **Test creation as two separate flows.** Assumed: manual test creation and auto-generate are two different products (matching how the rebuild had originally split them into two top-level pages). Actual: `QrTests.createTest()` and `createTestAuto()` **share one Setup screen**, forking only on an auto-generate flag. Corrected to a single unified flow with a mode toggle.
4. **Sidebar navigation scope.** Assumed: more nav destinations is more feature-complete. Actual, from `header.html` + `conf/messages` TXT_* constants: legacy's real sidebar has exactly 5 items. The rebuild's other 9 pages (Store, Live Classes, Scheduled Tests, Assignments, Challenges, Messages, Playlists, Certificates) still exist and function, they're just not real top-level nav destinations in legacy, so they were removed from the nav rather than deleted outright.
5. **Digital Library duplication.** The rebuild had added a second, flat "browse everything by type" section beneath the real subject-card tree, framed as covering a gap. On review, its "Other Shared Content" grant logic actually included the **entire** enrolled-course subtree (not just genuinely untagged content), so it was showing duplicates of what the subject tree already displayed. Per an explicit product decision this session ("there is no concept of Other Courses"), the whole section — and the component backing it — was removed rather than patched.
6. **Board Tree depth.** Assumed (early on): a 2-level Subject → Chapter tree. Actual, confirmed via `BoardXLParser`'s `maxAllowedColumns=3` and the "Add SubTopic" control in legacy's tagging UI: a real 3-level tree (Subject → Chapter → Concept). The rebuild's board-tree UI now matches.
7. **Bulk student upload format.** Modeled on legacy's real `StudentsXLParser` / `OrgMemberManager.uploadOrgStudents`: one Program chosen up front for the whole batch, with Center + Section supplied per row. Legacy's XL format also carries gender/DOB/parent-contact columns this rebuild's member model doesn't store — those are intentionally dropped, not silently lost (documented in the bulk-upload page's own header comment).

## 5. Deliberate trade-offs (not fidelity bugs — explicit decisions)

| Trade-off | Legacy | Rebuild | Why |
|---|---|---|---|
| Search / browse indexing | Elasticsearch-backed listing endpoints | Direct MongoDB queries | The legacy Elasticsearch index isn't populated/reachable in this environment; direct Mongo queries with in-app sorting/filtering substitute for it. This means full-text/fuzzy search is weaker than legacy's real ES-backed search wherever the rebuild relies on this substitution. |
| Session model | Play server-side session | Stateless HMAC-signed cookie (§ Architecture doc, §6) | Required by the Edge middleware architecture; not a legacy behavior being matched, a new constraint being solved. |
| Password storage for new accounts | Delegated to `user-services` | Self-issued `scrypt` hash for locally-created accounts, legacy proxy kept for legacy-issued accounts | Lets CMDS-created accounts work without a round trip to legacy for every login. |

## 6. A risk the new architecture introduced, then found and fixed

The session-model change in §5 is worth a second look specifically because it's where a real bug came from, not just an architectural swap. Legacy's Play server-side session meant a request's identity was never something a route had to think about separately — it was just *there*, tied to server-side state. The rebuild's stateless model means every route receives identity as data on the request, and it's up to that route to get it from the right place (the signed cookie, via `sessionFromReq()`) rather than the wrong one (a client-supplied `userId` field).

A systematic read of all 92 API routes (see the companion Architecture Deep Dive, §6.2, for the full route-by-route table) found that roughly a fifth of them had done exactly that — trusted a client-supplied `userId` instead of the session, in some cases going back to this project's earliest `/api/learn/**` routes. This wasn't present in legacy, and it wasn't an intended trade-off; it's the specific shape of bug this particular architecture change makes possible if a route is written carelessly. All instances found have been remediated (session-derived identity throughout, plus two staff-side cross-tenant org checks that were missing entirely); see the Deep Dive for the full finding, fix, and verification.

## 7. Net new capability inventory (does not exist in legacy at all)

- Advanced per-student analytics (subject/question-type accuracy resolved through the live board-tree hierarchy, score trend, strengths/focus-areas)
- CSV bulk student import with auto-generated Institute ID + password, downloadable as a credentials sheet
- Instant Test Generator (multi-subject, multi-chapter, difficulty-split generation with per-question review/replace)
- Admin-defined multiple Sections within a single test subject, each mixing question types under independent marking schemes
- Chapter/topic display on the Question Bank list, resolved live from the board-tree service
- Native Android app with offline content downloads (Room + WorkManager)
- The visual redesign itself (energetic, subject-color-coded UI) — a product decision, not a legacy-parity concern

## 8. What still fails if legacy goes down

Because four legacy Play services are proxied live rather than fully absorbed, the following break if `lmsbe` is unreachable: Board Tree browsing/tagging (question bank chapter filters, doubt tagging, student analytics' subject rollup), the actual test-taking screen's question content (`getTestInfo`/`getTestQuestions`), legacy-issued-account login, and whatever `organization-services` calls are still in `lib/legacyOrg.ts`. This is the direct cost of the Strangler Fig approach at its current stage of completion — it is not yet a fully independent system.
