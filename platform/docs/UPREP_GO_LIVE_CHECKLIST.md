# UPrep LMS — End-to-End Go-Live Checklist

**Target:** `backend` backend on **AWS (Mumbai `ap-south-1`)** + MongoDB Atlas.
**Owner:** _TBD_   **Status:** _in planning_

Track each item with `[ ]` → `[x]`. Items marked **P0** block a public launch.

---

## Critical path (order of execution)
**Decide stack + UI → AWS + Atlas → deploy backend → rotate secrets → load content → build UI → go live.**

| Milestone | Time | Gate |
|---|---|---|
| Backend live (internal) | ~1 day | services boot |
| Production-safe backend | ~2–4 weeks | secrets + TLS + backups |
| Full public launch | gated by UI + content | the two big unknowns |

---

## 1. Decisions (do first — they gate everything)
- [ ] Confirm **nextgen backend on AWS** as the production target (legacy = demo/reference only)
- [ ] **Decide the UI**: port legacy screens · build new SPA on the APIs · website-only  ← *biggest open item*
- [ ] Confirm whether an **original UPrep content backup exists** (changes the content plan)
- [ ] Assign an **owner** and approve the starter budget (~$330–400/mo infra)

## 2. Accounts & services to set up
- [ ] **AWS** account (PAYG), region **Mumbai `ap-south-1`**, IAM admin user (not root)
- [ ] **Domain** registered / Route 53 hosted zone
- [ ] **MongoDB Atlas** account (cluster created in step 4)
- [ ] **AWS Bedrock** access enabled (for AI, later)

## 3. Infrastructure (~$330–400/mo)
- [ ] EC2 `m7g.2xlarge` (8 vCPU / 32 GB, ARM) + 200 GB gp3 + Elastic IP
- [ ] Security group: 22 (my IP), 80/443 open; service ports 8081–8088 / 20000 **closed**
- [ ] S3 bucket (`uprep-content-prod`) + IAM role attached to EC2
- [ ] ElastiCache (Redis) or Redis container
- [ ] ALB + ACM certificate (or Nginx + Let's Encrypt)

## 4. Deploy backend (~1 day)  — see `UPREP_AWS_DEPLOY_RUNBOOK.md`
- [ ] MongoDB **Atlas M10** cluster in AWS Mumbai; DB user; network access locked to EC2
- [ ] Install Docker; `git clone`; set `SPRING_DATA_MONGODB_URI` (db `prodlp`) in `.env`
- [ ] Edit `docker-compose.yml`: use Atlas URI, remove local mongo, S3 via IAM role
- [ ] `docker compose up -d --build`
- [ ] **Verify all 8 services** return `200` on `/v2/api-docs`; website + virtual-classroom up
- [ ] Reverse proxy + **HTTPS** working; DNS A record → Elastic IP

## 5. Security (P0 — before anything is public)
- [ ] **P0** Rotate the committed **AWS access key** + **SMTP password** (deactivate old in IAM/SES)
- [ ] **P0** Move secrets to **AWS Secrets Manager** (out of properties/`.env`)
- [ ] Scrub old credentials from git history
- [ ] Atlas: auth on, network access restricted, backups enabled

## 6. Content
- [ ] Confirm/obtain **original data backup**; if found → restore to Atlas
- [ ] If not: build **bulk importer** (Excel/CSV/QTI) for question banks
- [ ] **AI-assisted generation** of questions/explanations with **human review**
- [ ] Stand up authoring path (part of the UI decision)
- [ ] Content QA pass before launch

## 7. UI (largest build)
- [ ] Implement chosen frontend against nextgen APIs
- [ ] Wire authentication / session end-to-end
- [ ] **End-to-end journey test:** login → browse content → take a test → results

## 8. AI / LLM (optional pilot — ~$100–350/mo)
- [ ] AI service calling **Bedrock**; provider behind an interface
- [ ] **RAG**: embed content into a vector store
- [ ] Ship **question generation** + **doubt tutor** first
- [ ] Usage caps + caching + prompt/output logging

## 9. Maintenance & ops (ongoing)
- [ ] Monitoring + alerting (CloudWatch + Actuator health/readiness)
- [ ] Backups + **restore drill** verified
- [ ] **CI/CD** pipeline + smoke/integration tests
- [ ] Plan **Spring Boot 2.3 → 3.x (Java 17)** upgrade (EOL)
- [ ] Dependency/vuln patching cadence
- [ ] **DPDP Act** (India) data-privacy handling for student data
- [ ] Team assigned: ~0.5–1 backend/DevOps + 0.5–1 frontend + content team + QA/support

---

## The 3 things that will actually block launch
1. **No UI** in the modern stack — decide and build it.
2. **Content** — must be sourced/created (no dump to restore in-repo).
3. **Committed secrets** — rotate before exposing anything.

---

## Cost snapshot
| Bucket | Est. /mo |
|---|---|
| Cloud infra (EC2 + Atlas + S3 + ALB) | ~$330–400 |
| AI/LLM (pilot → moderate) | ~$100–2,200 |
| Monitoring / tooling / backups | ~$50–150 |
| **Platform subtotal (excl. salaries)** | **~$500–2,800** |
| People (content + engineering) | dominant long-run cost — separate |

---
_Companion docs: `UPREP_FINAL_BRIEF.md`, `UPREP_AWS_DEPLOY_RUNBOOK.md`, `UPREP_CONTENT_AI_MAINTENANCE_REVIEW.md`; architecture in the `uprep-cloud-deployment-review` canvas._
