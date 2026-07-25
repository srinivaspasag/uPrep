# UPrep — Legacy vs New-Stack Gap Analysis

> Source of truth: **Learnpedia User Manual** (58 pages) cross-checked against the current
> `platform/web` (Next.js) implementation on AWS, as of **2026-07-22**.
> Read like a designer: every documented capability in the manual is treated as a requirement,
> then matched to what actually exists in the new stack.
>
> **Traceability:** `[man p.X]` = the manual's own printed footer page number the item is drawn from
> (the page numbers shown as "Page … N" at the bottom of each manual page, not the PDF sheet number).
> Lines with no `[man p.X]` are net-new features not described in the manual.

## How to read this

Each line is a checklist item. **Unchecked (`[ ]`) = needs work.**

| Tag | Meaning |
|-----|---------|
| ✅ **Working** | Implemented and functionally equivalent to legacy |
| 🟡 **Partial** | Exists but missing sub-features / depth / correctness |
| ❌ **Missing** | Not implemented in the new stack |
| ⚪ **Out of scope (web)** | Legacy native/offline product; not part of the web app |

Severity: **P0** = blocks core teaching workflow · **P1** = important parity gap · **P2** = nice-to-have / polish.

---

## Executive summary

The new stack has **strong coverage of content authoring, the student Learn app, people management, and commerce basics**, and even **exceeds** legacy in a few places (impersonation, certificates, live classes, playlists, leaderboard/challenges, auto-generated tests).

The biggest parity gaps are concentrated in **three workflows**:

1. **Program content curation & visibility** — the legacy "tick content → add to program → make visible/invisible on learn/device → remove" loop, plus **Schedule a Test** per section. Today a Program is essentially a label; it does not actually gate/curate what a section sees. `[man p.12–15]`
2. **Admin test analytics ("Learning Network")** — top performers, %-vs-marks distribution, per-question correct/incorrect with time-taken, printable result sheet, score-range detailed analytics, and admin **Reset / Pause / Resume / End** controls. `[man p.30–34]`
3. **Commerce depth & offline** — Seller Dashboard (access codes, shipments, order states), package pricing per program (days + price, open/close), org email/SMTP config, and the entire offline family (Pendrive, SD tablet, Web Offline Test). `[man p.25–27, 50–57]`

**Top P0/P1 backlog** is collected at the bottom.

---

## 1. CMDS

### 1a. Content Management Portal

#### Institute Resources
- [x] ✅ **Working** `[man p.4]` — Resources browser with folders, breadcrumb navigation, type filter, sort by date/title (`app/cmds/page.tsx`).
- [x] ✅ **Working** `[man p.4]` — Search resources (client-side filter + `learn/search` for students).
- [x] ✅ **Working** `[man p.4, 10]` — Subjects rail on the left.
- [ ] 🟡 **Partial (P2)** `[man p.4, 10]` — Subjects rail is **hard-coded** to `["All Subjects", "Physics"]` in `CmdsShell.CmdsSubjectsRail`; it does not enumerate the org's real subjects.

#### Add Content
- [x] ✅ **Working** `[man p.5]` — Add a Folder (nested, subject-tagged).
- [x] ✅ **Working** `[man p.5]` — Add a Document (PDF ebook upload) `cmds/documents/new`.
- [x] ✅ **Working** `[man p.6]` — Add a Test `cmds/tests/new`; plus **Auto-Generate Test** (bonus, not in manual).
- [x] ✅ **Working** — Add a Video `cmds/videos/new`.
- [x] ✅ **Working** — Create a Module `cmds/modules/new`.
- [ ] 🟡 **Partial (P1)** `[man p.6]` — "Add a Question Set" picks from **already-published questions** (`cmds/questions/set/new`); the manual's **"Upload Questions" via predefined template** (bulk CSV/XLSX import with same Topic/Type/Difficulty) is **not** implemented.
- [ ] ❌ **Missing (P2)** `[man p.5]` — Document upload subject **+ topic** tagging enforced at upload (manual requires topic tags; current mainly captures subject).

#### Add / Author a Question
- [x] ✅ **Working** `[man p.7]` — Question types **SCQ, MCQ, Numeric, Matrix, Para/Comprehension** (matches manual) **+ Subjective** (bonus).
- [x] ✅ **Working** `[man p.7]` — Difficulty **Easy / Moderate / Tough**.
- [x] ✅ **Working** `[man p.8]` — Options with correct-answer selection; numeric accepted answers; matrix pairs; solution/explanation.
- [x] ✅ **Working** `[man p.7]` — Formula support via **LaTeX** (`$...$` / `$$...$$`) with live preview (covers manual's "Add Formula").
- [ ] ❌ **Missing (P1)** `[man p.7–8]` — **"Add Image"** upload inside the question, options, and solution (manual explicitly supports images; current supports LaTeX text only).
- [ ] ❌ **Missing (P1)** `[man p.8]` — **Video Solution** attachment (link a CMDS video as the worked solution).
- [ ] 🟡 **Partial (P1)** `[man p.7]` — Structured **Subject → Topic** taxonomy on a question. Current uses a free-text `subject` + comma tags rather than selecting Subject then Topic(s) from the academic structure.

#### Question Bank
- [x] ✅ **Working** `[man p.9]` — Question list (`cmds/questions`), reverse-chronological, show details, edit.
- [ ] 🟡 **Partial (P2)** `[man p.10]` — Verify all documented filters exist: **Subject, Topic, Question Type, Level, Date Added**, and a **Paragraphs** view for comprehension questions. (Type/level likely present; Topic + Date + Paragraphs view need confirmation.)
- [x] ✅ **Working** `[man p.10]` — Edit propagates to tests; subject/topic/type/level locked after creation (design intent matches manual note).

#### Subjects
- [ ] 🟡 **Partial (P2)** `[man p.10–11]` — Viewing subjects **and their topics** as a browsable tree is not a first-class screen (topics live implicitly under academic structure / tags).

---

### 1b. Programs

- [x] ✅ **Working** `[man p.11–12]` — List of Programs, Create Program (`cmds/programs`).
- [x] ✅ **Working** `[man p.15–17]` — Program detail with Content / Members / Students / Organizations / Upload Mark Sheets tabs.
- [x] ✅ **Working** — Upload Mark Sheets for offline tests (`programs/[id]/marksheets`) — bonus vs manual.
- [x] ✅ **Working** — "Shared with your institute" surfacing of granted packs/courses on Programs (added 2026-07-16).
- [ ] ❌ **Missing (P0)** `[man p.12]` — **Add Content to a Program** by ticking items in Resources. The Content tab currently lists **all** org content and marks everything "● Published" (hard-coded); there is no per-program membership.
- [x] ✅ **Working (fixed 2026-07-23)** `[man p.12–13]` — **Make content Visible / Invisible on learn/device**. Content items carry a `hidden` flag; toggled from Resources (⋯ menu) and the Program → Content tab (real Visible/Invisible status instead of hard-coded "Published"). Student library (`/api/library`) and course browse (`/api/learn/courses`) exclude `hidden` items; staff still see them. *Follow-up: gate direct `/test/[id]` open + folder-level hide.*
- [x] ✅ **Working (fixed 2026-07-23)** `[man p.13]` — **Remove content from student view** via the Invisible toggle (soft, reversible); hard delete already existed in Resources.
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.13–15]` — **Schedule a Test** to a date/time window, targeted to one or more **sections** (empty = whole institute). Admin tool at **Tools → Schedule a Test** (`/cmds/tests/schedule`) with live countdown + status (Upcoming/Live/Ended); students see them under **Scheduled Tests** (`/learn/tests`) and can only start during the window (`testschedules` collection; APIs `/api/cmds/tests/schedule` + `/api/learn/tests`). *Follow-up: center targeting + hard server-side lock on `/test/[id]` outside the window.*
- [ ] 🟡 **Partial (P1)** `[man p.16]` — Manage **Members (Teachers/Editors)** *inside* a program (Add / Modify / Deactivate). Current Members tab is a **read-only** list that links out to People Management; no program-scoped assignment.
- [ ] 🟡 **Partial (P1)** `[man p.17–18]` — Manage **Students** inside a program incl. add-student-then-assign-program/center/section. Current Students tab is **read-only**.
- [ ] 🟡 **Partial (P1)** `[man p.16]` — **Switch center/section** to manage content/students per section. Header shows only the first center/section; no working switcher.
- [ ] ❌ **Missing (P2)** `[man p.15]` — Content type filter within a program (documents / tests / videos / modules).

---

### 1c. Tools

#### Organization Info
- [x] ✅ **Working** `[man p.19]` — Name, full name, website, contact, type, address, description, login mechanism, doubts-forum mode, social pages (FB/Twitter/LinkedIn/YouTube).
- [ ] ❌ **Missing (P1)** `[man p.4, 19]` — **Logo upload** (manual: CMDS shows the institute logo top-left; and org logo config).
- [ ] ❌ **Missing (P2)** `[man p.19]` — **App store links** (Play/App Store URLs) config.
- [ ] ❌ **Missing (P1)** `[man p.19]` — **Email account configuration** (Gmail / G-Suite / SendGrid SMTP) for content-upload notifications, verification emails, and batch emails. Messaging layer exists (`lib/messaging.ts`) but there is no org-facing SMTP setup screen.

#### Edit Academic Structure
- [x] ✅ **Working** `[man p.20]` — **Departments → Programs → Centers → Sections** CRUD (`cmds/tools/academic`).
- [x] ✅ **Working (fixed 2026-07-25)** `[man p.21]` — **"Assign Courses" to a program** is now a live tab: pick a program, tick courses from the org catalog (own + shared), saved to `orgprograms.courseIds` (`POST /api/cmds/tools/academic {kind:"assign-courses"}`). *Follow-up: auto-enroll a section's students from the program's course list.*
- [x] ✅ **Working (fixed 2026-07-25)** `[man p.20]` — Academic Structure rebuilt to match legacy: **Classroom Centers** left rail (add/remove real centers), cascading **Departments → Runs Programs → in Centers → Has Sections**, program↔center **"Assign a Center"** linkage (`orgprograms.centerIds`), sections scoped to program+center, and per-column search.

#### People Management
- [x] ✅ **Working** `[man p.21–22]` — Roles: Student, Offline User, Teacher, Manager, Editor, Salesperson; add, edit (name/email/contact/role), search, counts.
- [x] ✅ **Working** `[man p.22]` — **Change / Reset Password** with shareable credentials.
- [x] ✅ **Working** `[man p.23]` — **Deactivate** member.
- [x] ✅ **Working** — **Login as** (impersonation) — bonus vs manual.
- [ ] ❌ **Missing (P2)** `[man p.22]` — **Gender** field on add/edit (manual lists it).
- [ ] 🟡 **Partial (P1)** `[man p.22]` — **Assign Programs/Center/Section during add/edit** ("Step 2"). Course assignment lives in a separate "Assign Courses" tool, but program/section assignment on the member is not integrated.
- [ ] ❌ **Missing (P2)** `[man p.23]` — **Scheduled deactivation** (deactivate now **or** schedule a duration). Current is immediate only.
- [ ] ❌ **Missing (P1)** `[man p.23–24]` — **Send Emails** to a selected set of students (subject + message), gated on SMTP config.
- [ ] 🟡 **Partial (P2)** `[man p.23]` — **Export Students Data** (CSV, rolling 3-month window). A generic Exports tool exists (`cmds/tools/exports`); confirm it covers the documented student export + the 3-month constraint.

#### Device Management
- [x] ✅ **Working** `[man p.24]` — Per-member **web / mobile login status** with profile filter and name search (`cmds/tools/devices`).
- [ ] 🟡 **Partial (P2)** `[man p.24]` — Manual wants activity **class-wise and section-wise**; current is profile-wise + name search only (no section grouping).
- [ ] ❌ **Missing (P2)** `[man p.57]` — **Remove a device mapping** (relevant to offline/WoT seat control).

#### External Sign Up Management
- [ ] 🟡 **Partial (P1)** `[man p.25]` — Current `cmds/tools/signup` is a **self-registration config** (enable, approval, default role, allowed domains, welcome message) — a *different* feature from the manual.
- [ ] ❌ **Missing (P1)** `[man p.25]` — Manual's model: **show programs for purchase**, **Open/Close** a program on the student store, and **Create Package** = *(number of days + price)* per program/section. Commerce has flat course products but not day-based packages or per-program open/close.

#### Seller Dashboard
- [ ] ❌ **Missing (P1)** `[man p.26]` — **Access Codes** generation (for activating SD-card / offline access in the app).
- [ ] ❌ **Missing (P2)** `[man p.26]` — **Shipments** status tracking for access codes.
- [ ] 🟡 **Partial (P1)** `[man p.26–27]` — **Orders / purchase history** with legacy **order states** (FINALIZED, UNDER PROCESS, WAITING, CONFIRMED, CANCELLED, DRAFT). Commerce **Invoices** exist with PAID/PENDING/CANCELLED only — the richer state machine + retry/confirm flows are missing.

#### Coupons Dashboard
- [x] ✅ **Working** `[man p.27]` — Create coupons, **FLAT (₹ off)** and **PERCENTAGE (% off)**, disable, redeemed count.
- [ ] 🟡 **Partial (P2)** `[man p.27]` — Create form doesn't expose **max redemptions** or **expiry** (the data model has `maxRedemptions`, UI doesn't set it).

#### Send Push Notification
- [x] ✅ **Working** `[man p.28]` — Title, message, summary, image URL; **resource-specific** deep link (Module/Video/Test/Ebook + resource ID); recent-sent history.
- [ ] ❌ **Missing (P1)** `[man p.28]` — **Program / Center / Section-specific** targeting (manual's optional program-specific push).
- [ ] 🟡 **Partial (P1)** `[man p.28]` — **Actual delivery to Android** (FCM/push). No FCM/firebase integration found; notifications appear to be stored/logged and shown in-app only.

---

### 1d. Learning Network (admin view of the student portal)

- [x] ✅ **Working** `[man p.29]` — "Learning Network »" opens the student library view from CMDS.
- [x] ✅ **Working** `[man p.29–34]` — Digital Library, Modules, Ebooks, Videos are viewable.
- [x] ✅ **Working** — **Subjective grading** queue (`cmds/tests/grading`) — bonus vs manual.

#### Admin Test Analytics (the deep part of the manual)
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.31]` — **Overall Performance**: average score/%, high/low, **Top Performers** (top 5), and a **% students vs marks** distribution (5 buckets) on the Test Analytics → Overview tab.
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.31–32]` — **Per-question analytics**: attempts, Correct / Incorrect / Partial / Ungraded counts and **% correct** bar per question, at **Tools → Test Analytics** (`/cmds/tests/analytics`, API `/api/cmds/tests/analytics?testId=`). *Follow-up: per-question time-taken + Most/Least-attempted filters + question text.*
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.32]` — **Result Sheet**: ranked students with best score, %, attempt count, last-attempt time (Test Analytics → Result sheet tab) + **Download CSV** (printable/exportable via `/api/cmds/tests/analytics/export`). *Follow-up: subject-wise split.*
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.33]` — **Detailed Analytics by score range**: Min %/Max % filter on the Result sheet tab narrows to students scoring in a band.
- [x] ✅ **Working (fixed 2026-07-24)** `[man p.33]` — Admin **Reset Test**: per-student **Reset** (Result sheet + In-progress tabs) deletes their attempts (+ per-question rows + in-progress state) so they can retake (`POST /api/cmds/tests/attempts {action:"reset"}`, admin-only, org-scoped).
- [x] 🟡 **Mostly working (fixed 2026-07-24)** `[man p.34]` — Admin control over live attempts: new **In progress** monitor tab lists students with a paused/in-progress attempt (answered count, time left) with **End** (clears the attempt). *Follow-up: true real-time Pause/Resume requires the take-test page to poll a control flag — deferred.*
- [ ] 🟡 **Partial (P1)** `[man p.30]` — **Test Details** breakdown (subject marks, contributing chapters, topic-level mark distribution) for admins.

#### Doubts Forum (admin + student)
- [x] ✅ **Working** `[man p.35]` — Ask a Doubt, Answer a Doubt, Recent / Popular / Asked-by-me tabs, subject on items, up-votes/views (`learn/doubts`).
- [ ] 🟡 **Partial (P2)** `[man p.35]` — **"Following"** filter and an explicit **Subject-wise** filter tab from the manual are not present.

#### Institute Analytics
- [ ] ❌ **Missing (P1)** `[man p.35–36]` — **Program-wise institute analytics** with Program/Center/Section + Subject filters (aggregate across tests) — the one-stop analytics dashboard the manual describes.

---

## 2. E-Learning (student)

### 2a. Web portal (PC/Laptop)
- [x] ✅ **Working** `[man p.36–37, 43–44]` — Login via institute, Digital Library (Videos / Tests / Ebooks), Programs, Doubts Forum, Analytics, Recent Activity (`learn/*`).
- [x] ✅ **Working** `[man p.38–39]` — Take test: instructions → take → **exam summary** → analytics; pause/resume; submit.
- [x] ✅ **Working** `[man p.45, 49]` — **Study List** equivalent (Bookmarks + Playlists), Profile, Notifications, Store, Certificates, Live classes, Leaderboard, Challenges, Messages (several are bonus vs manual).
- [ ] 🟡 **Partial (P1)** `[man p.40–42]` — Student **test result depth**: manual shows **grid view + question view**, detailed per-question report, and printable **result sheet**. Current student analytics is a simple score/percentage table (`learn/analytics`); confirm the per-attempt grid/question review exists on `test/[id]` results.
- [ ] 🟡 **Partial (P2)** `[man p.44]` — Purchased-vs-available **Programs/Courses** store split matches manual ("available" + "purchased"); verify parity with `learn/store` + `learn/courses`.

### 2b. Mobile Android app
- [ ] 🟡 **Partial (P1)** `[man p.45–49]` — An Android wrapper exists (WebView build). Native tabs from the manual — **Courses, Doubts, Analytics, Profile, Study List** — ride on the web app; confirm deep-link/push/offline behave natively.
- [ ] ❌ **Missing (P2)** `[man p.49]` — Native **Study List** and offline SD-card content access on device.
- [ ] ❌ **Missing (P1)** `[man p.28]` — **Push notification delivery** to the Android app (see FCM gap above).

---

## 3. Offline products for JEE & NEET  ⚪ (largely out of web scope, but zero parity today)

- [ ] ⚪ **Out of scope / Missing** `[man p.50]` — **Score Pendrive Offline Solution** (Windows desktop app, offline video/test/ebook playback).
- [ ] ⚪ **Out of scope / Missing** `[man p.50]` — **Android Tablet + SD Card** offline solution (pre-loaded content, per-device user ID, offline viewing, online tests/sync).
- [ ] ⚪ **Out of scope / Missing** `[man p.51–56]` — **Web Based Offline Test (WoT)** — LAN/XAMPP local-server exam app: setup (program/test/password), sync users, per-computer device mapping, "NOT YOU" re-login, admin start/monitor/end, sync submissions to cloud.
- [ ] ❌ **Missing (P1, cloud side)** `[man p.26, 54]` — Even ignoring the native apps, the **cloud-side enablers** are absent: access-code generation/redemption for offline activation and device-seat mapping/removal.

---

## 4. FAQ-derived requirements (WoT operational rules)

- [ ] ⚪ **Missing** `[man p.57]` — Per-computer credential mapping, "NOT YOU" re-login, admin remove-mapping, end-test-for-cheating, LAN sync semantics. (Only relevant if WoT is brought forward.)

---

## Bonus — present in new stack, not in the legacy manual

These are net-new capabilities worth keeping/marketing (no manual reference by definition):

- ✅ Impersonation ("Login as") for support.
- ✅ Real completion **Certificates** (`learn/certificates`).
- ✅ **Live classes** with Zoom/Meet join links (`learn/live`, schedule tool).
- ✅ **Playlists / Bookmarks**, **Leaderboard**, **Challenges/Channels**, **Messages**.
- ✅ **Auto-generate Test** from the question bank.
- ✅ **Course Packs** (named bundles) + super-admin → org grants → student assignment.
- ✅ **OTP / phone login**, self-service password reset, HMAC session security, multi-tenant org scoping.
- ✅ Upload **Mark Sheets** for offline test results.

---

## Prioritized backlog (start here)

### P0 — restores core teaching workflow
1. Program content curation: tick-to-add content to a program + **Visible/Invisible on learn/device** + remove. `[man p.12–13]` (Makes Programs actually gate content.)
2. **Schedule a Test** to a center/section with date/time + countdown + timed student availability. `[man p.13–15]`
3. Admin **Test Analytics**: overall performance (avg, top performers, %-vs-marks), per-question correct/incorrect + time, printable **Result Sheet**. `[man p.31–32]`

### P1 — important parity
4. Admin live-test controls: **Reset / Pause / Resume / End** a student's attempt. `[man p.33–34]`
5. Program-scoped **Members/Students** management + **center/section switcher**. `[man p.16–18]`
6. Question authoring: **image upload** + **video-solution** attach + structured **Subject→Topic**. `[man p.7–8]`
7. Bulk **Upload Questions** via template. `[man p.6]`
8. Org **email/SMTP config** + **Send Emails to students** + **logo upload**. `[man p.19, 23–24]`
9. External signup **packages** (days + price, open/close) and richer **order states**; **access codes**. `[man p.25–27]`
10. **Push delivery to Android** (FCM) + program/section targeting. `[man p.28]`
11. **Program-wise institute analytics** dashboard. `[man p.35–36]`
12. Assign **courses to programs** ("Assign Courses" tab in Academic Structure). `[man p.21]`

### P2 — polish / completeness
13. Real subjects rail; Question Bank Topic/Date/Paragraph filters; Subjects→Topics browser. `[man p.4, 10]`
14. Gender field; scheduled deactivation; coupon max-redemptions/expiry UI. `[man p.22–23, 27]`
15. Device management section-wise grouping + device-mapping removal. `[man p.24]`
16. Doubts "Following" + subject-wise tabs. `[man p.35]`

---

## Verification notes (things I inferred and should be double-checked in a running org)

- Student per-attempt **grid/question review** and printable result sheet on `test/[id]` `[man p.40–42]` (marked Partial).
- Whether `cmds/tools/exports` covers the documented **student CSV export** + 3-month window `[man p.23]`.
- Question Bank filter completeness (**Topic / Date Added / Paragraphs** view) `[man p.10]`.
- Whether any push actually reaches devices vs in-app only `[man p.28]`.

---

## Caveats on sources

- The manual text I parsed was **text-only** — embedded screenshots/images in the PDF were not analyzed. If a screenshot documents behavior the text omitted, it may not be reflected here.
- Page numbers are the manual's **printed footer** numbers ("Page … N"), which run 1 behind the PDF sheet number (e.g. footer p.12 = PDF sheet 13).
