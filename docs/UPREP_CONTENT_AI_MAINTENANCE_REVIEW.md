# UPrep LMS — Content, AI/LLM & Maintenance Review

**Audience:** planning / stakeholders   **Status:** for review   **Owner:** _TBD_
Covers three questions: (1) how to get content, (2) LLM/AI integration + monthly cost,
(3) what it takes to maintain the platform ongoing.

---

## Part 1 — Content: why it's empty and how to fill it

### Current state
- The platform has **no real content** — only a handful of seeded demo records (one test, a few questions).
- **There is no content database dump in the workspace to restore.** The `mongo-scripts` folder holds one-off ops/migration scripts, not content.
- In the legacy stack the content **publishing pipeline is buggy** (content authored in CMDS never reached the student library index). The modern `nextgen` stack has the content/CMDS **APIs** but no authoring UI yet.

> **Implication:** content must be **created or imported** — it cannot simply be "switched on."

### How to get content — five paths

| # | Path | What it means | Effort | Best when |
|---|---|---|---|---|
| 1 | **Recover original data** | Obtain a MongoDB dump/backup from the old UPrep production infra or a partner who ran it, and restore it | Low (if a dump exists) | **Do this first** — check if any backup survives |
| 2 | **Manual authoring** | Content team creates tests/questions/modules and uploads docs/videos via CMDS | High, ongoing | Long-term, quality-controlled catalog |
| 3 | **Bulk import / migration** | Write importers to load question banks from Excel/CSV/Word/QTI into the content APIs | Medium (one-time build) | You already have banks in spreadsheets/another LMS |
| 4 | **License / partner content** | Buy or partner for ready question banks & courseware | $$ | Fast catalog, exam-prep verticals |
| 5 | **AI-assisted generation** | Use an LLM to draft questions/explanations from syllabus & documents, humans review | Medium | Accelerate #2 (see Part 2) |

### Recommendation
1. **Immediately:** find out whether an **original production backup exists** (fastest, richest option). Ask whoever operated the old system.
2. If not, run **#3 + #5 together**: build a bulk importer and use **AI to draft** questions/explanations, with a **human review** step for quality.
3. Stand up a working **authoring UI** (part of the broader UI decision) so the content team can maintain the catalog going forward.

> Content is a **program, not a one-time task** — budget for a content team + tooling on an ongoing basis.

---

## Part 2 — LLM / AI integration

### Where it fits (highest-value use cases for this product)
The product already has the right hooks (Doubts Forum, questions, analytics), so AI slots in naturally:

| Use case | Value | Notes |
|---|---|---|
| **Question generation** | Fills the content gap fast | From syllabus + uploaded docs; human-reviewed |
| **Doubt-solving tutor / chatbot** | Direct fit for the Doubts Forum | RAG over your content so answers stay on-syllabus |
| **Auto content tagging** | Fixes manual board/topic tagging (a known pain) | Classify questions/docs to the taxonomy automatically |
| **Auto-grading & feedback** | Scales subjective assessment | Short-answer / descriptive grading with rubric |
| **Semantic search** | Better content discovery | Embeddings + vector search |
| **Explanations / hints / summaries** | Learning quality | On-demand step-by-step help |
| **Analytics insights** | Teacher/ops productivity | "At-risk student" detection, natural-language reports |

### How to integrate (architecture)
- Add a small **AI service** (new microservice, or extend `content`/`cmds`) that calls a **hosted LLM API** — keep the provider behind an interface so it's swappable.
- For grounded answers (tutor, search), add **RAG**: embed your content into a **vector store** (e.g. OpenSearch/pgvector/managed) and retrieve before prompting.
- Enforce **usage caps, caching, and per-tenant limits** to control cost; log prompts/outputs for quality.

### Provider options
| Option | Examples | Trade-off |
|---|---|---|
| **Hosted API (recommended)** | AWS **Bedrock** (Claude/Llama/Titan), OpenAI, Anthropic, Google Gemini | No infra; pay per token; fastest to ship. Bedrock keeps it in-AWS |
| **Self-hosted open model** | Llama 3.x / Mistral on GPU (EC2 `g5`) | High fixed cost (~$700–4,000+/mo per GPU); only worth it at large, steady volume |

### Monthly cost (estimates — usage-driven)
Assumes hosted API + a small managed vector store. Token costs vary by model (a mid-tier model ≈ $0.5–5 per million tokens; premium models more).

| Tier | Example usage | Est. LLM/mo | + Vector/infra | **Total/mo** |
|---|---|---|---|---|
| **Pilot** | Content generation + light chatbot, 100s of users | $50–300 | ~$50 | **~$100–350** |
| **Moderate** | Active tutor + grading, 1k–10k users | $500–2,000 | ~$100–200 | **~$600–2,200** |
| **Heavy** | Tutor as a core feature, high volume | $3,000–10,000+ | $300+ | **$3,300–10,000+** |

> These scale with **tokens consumed**, so caps + caching + choosing the cheapest adequate model per task are the main cost levers. Start on a hosted API at the pilot tier and measure before committing to self-hosting.

### Recommendation
Start with **AWS Bedrock** (keeps AI in the same cloud/region, one bill, data stays in AWS), implement **RAG over your content**, ship **question-generation + doubt tutor** first (highest ROI), and gate everything behind **usage limits**.

---

## Part 3 — What it takes to maintain the platform

Running it isn't just the server bill. Ongoing maintenance spans infra, security, data, people, and tech-debt.

### Operational (infrastructure)
- Patch OS/containers; renew TLS certs (auto via ACM/Let's Encrypt).
- **Monitoring & alerting** (CloudWatch + Actuator), uptime checks, on-call/incident response.
- **Backups + restore drills** (Atlas handles backups; test restores quarterly).
- Capacity: watch the shared MongoDB (first bottleneck); scale EC2/Atlas as usage grows.

### Security & compliance
- **Rotate secrets** (the committed AWS/SMTP keys — do now), move to Secrets Manager.
- **Dependency upgrades / vuln scanning** — the stack ships old libraries; keep them patched.
- **PII & data privacy** — student data implies India **DPDP Act** obligations (consent, retention, access) + secure handling.

### Data & content
- Content QA and refresh cycle; taxonomy upkeep.
- Data retention/cleanup; analytics hygiene.

### Technical debt to plan for
- **Spring Boot 2.3 is end-of-life** — plan an upgrade to a supported Spring Boot 3.x line (Java 17). Not urgent for a pilot, but required for a maintained production platform.
- **No CI/CD** today — add a pipeline so fixes/deploys are safe and repeatable.
- **No automated tests** visible — add smoke/integration tests to protect against regressions.
- **UI**: whatever UI is chosen becomes its own maintenance surface.

### People (typical minimum team)
| Role | Commitment | Purpose |
|---|---|---|
| Backend / DevOps engineer | 0.5–1 FTE | Deploys, upgrades, incidents, scaling |
| Frontend engineer | 0.5–1 FTE | UI build + upkeep (once UI direction is set) |
| Content team | Ongoing | Author/curate/QA the catalog |
| QA / support | Part-time | Testing + user support |
| AI/ML (optional) | Part-time | LLM features, prompt/RAG tuning |

### Rough monthly running cost (small production)
| Bucket | Est. /mo |
|---|---|
| Cloud infra (EC2 + Atlas + S3 + ALB) | ~$330–400 |
| AI/LLM (pilot → moderate) | ~$100–2,200 |
| Monitoring / tooling / backups | ~$50–150 |
| Domain / email / misc | ~$20–50 |
| **Platform subtotal (excl. salaries)** | **~$500–2,800** |
| People (varies greatly by geography/model) | separate line item |

> The dominant long-run cost is **people (content + engineering)**, not the servers. The AI bill is the most variable and should be capped/measured.

---

## Summary — the asks

1. **Content:** first confirm whether an **original data backup exists**; if not, fund a **bulk-import + AI-assisted authoring** effort and a content team.
2. **AI:** approve a **pilot on AWS Bedrock** (question generation + doubt tutor via RAG), with usage caps; measure before scaling.
3. **Maintenance:** staff a **minimum team** (backend/DevOps + content), budget **~$500–2,800/mo** platform cost (excl. salaries), and schedule the **Spring Boot upgrade + CI/CD** as required tech-debt work.

---
_Companion docs: `UPREP_FINAL_BRIEF.md` (hosting/production) and `UPREP_AWS_DEPLOY_RUNBOOK.md` (deploy steps); architecture overview in the `uprep-cloud-deployment-review` canvas._
