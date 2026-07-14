# UPrep LMS — Hosting & Production Brief (Final)

**Audience:** planning / stakeholders   **Status:** for review   **Owner:** _TBD_
**Target cloud:** AWS (Mumbai `ap-south-1`)   **Scale:** small production (1k–10k users)

---

## Highlights (quick overview)

- **Two stacks:** a modern API backend (`backend`, deployable) and a legacy full app that holds the actual UI (`lms-master`, demo-only).
- **Backend:** 8 Spring Boot services, ~400 APIs — cloud-ready.
- **Biggest gap:** the modern stack has **no UI**; the working screens live only in the legacy stack.
- **Best cloud fit: AWS** — the code already uses **AWS S3** (files) and **AWS SES** (email), so it's the least-migration home; MongoDB Atlas runs **natively in AWS**.
- **Starter cost:** **~$330–400/mo** (1 EC2 box + Atlas M10 + S3 + ALB).
- **Must-fix first:** rotate committed AWS/SMTP secrets; decide the UI direction; verify all 8 services boot cleanly.
- **Scale:** comfortably supports ~**100k–500k users**; the shared MongoDB is the first ceiling.
- **Timeline:** live backend in **~1 day**; production-safe in **~2–4 weeks** (excluding the UI build).

---

## Recommendation — host everything on AWS

For this stack AWS is the natural home, because:

1. **The code already targets AWS** — AWS S3 SDK for file storage and AWS SES SMTP for email. Least migration of any cloud.
2. **MongoDB Atlas runs natively inside AWS** (Mumbai) — fully managed DB, no cross-cloud hop.
3. **Single vendor for everything** — compute, DB, storage, cache, load balancer, secrets, CI/CD, monitoring.
4. **India regions** — Mumbai (`ap-south-1`), Hyderabad (`ap-south-2`).

Deploy mechanics stay simple: `docker compose up` on one EC2 VM to start; move to ECS/EKS later when you need autoscaling.

### What to buy — AWS, small production

| Item | Spec | ~Cost/mo |
|---|---|---|
| Account / region | AWS PAYG, **Mumbai `ap-south-1`** | — |
| **Compute** | 1 × EC2 `m7g.2xlarge` — **8 vCPU / 32 GB** (Graviton/ARM; nextgen is ARM-ready) running Docker Compose | ~$235 |
| **Storage (disk)** | 200 GB EBS gp3 | ~$16 |
| **Database** | MongoDB **Atlas M10** (replica set + backups) in AWS Mumbai | ~$57 |
| **Object storage** | **S3** (already coded for it) | ~$5–15 |
| **Cache** | ElastiCache (small) or Redis container on the VM | ~$0–30 |
| **Load balancer + TLS** | ALB + ACM certificate (cert free) | ~$18 |
| **Domain** | Route 53 hosted zone + registration | ~$1 + reg |
| **Total** | | **~$330–400/mo** |

Grow path: resize EC2 to `m7g.4xlarge` (16/64) and Atlas to **M20/M30** as you approach 10k users — a resize, not a rebuild.

### Why not OCI (the earlier suggestion)
OCI is ~20–35% cheaper on compute, but Atlas can't run inside OCI (cross-cloud DB), and none of the code targets it. AWS trades a little more monthly cost for **less migration and one-vendor simplicity**. Chosen: **AWS**.

---

## Executive summary

UPrep exists as **two parallel codebases**, and they are not interchangeable:

- **Modern backend** (`backend`) — Spring Boot / Java 17, **8 services, ~400 APIs**. Container-ready and cloud-deployable. **But it has no user interface — APIs only.**
- **Legacy full app** (`lms-master`) — Play 1.2.4 / Java 6–7. Contains the **actual LMS and CMDS screens**, but every layer is ~10+ years end-of-life. **Reference/demo only, not a production target.**

**Headline:** the backend can go live quickly on AWS; the **user-facing UI is the real gap** and the biggest planning decision.

### Three blockers before a public production launch (P0)
1. **Leaked secrets** — live AWS + SMTP credentials are committed in source; rotate + move to AWS Secrets Manager.
2. **No UI** — decide: port legacy UI, build a new SPA on the APIs, or launch website-only.
3. **Unverified boot** — confirm all 8 services start cleanly against Atlas (one showed a wiring error).

---

## Component deployability

| Component | Stack | Cloud-deployable? | Note |
|---|---|---|---|
| Backend: user, organization, content, **cmds**, board, social, comm, event | Spring Boot / Java 17 | **Yes** | Compose-ready; verify boot |
| Website (`website`) | PHP / Apache | **Yes** | Container or Amplify/S3+CloudFront |
| Virtual Classroom | Spring Boot / Java 17 | **Yes** | Needs external BBB host |
| **LMS + CMDS UI** (`lms-master`) | Play 1.2.4 / Java 6–7 | **Demo only** | Runs on an x86 VM; not a production stack |
| MongoDB / Redis / Object storage | Atlas / ElastiCache / S3 | **Yes** | Managed on AWS |
| Android apps, proxy-server, score-reader, mongo-scripts, sales-app, … | Mixed | Out of scope | Not part of server deploy |

> **Key nuance:** "LMS" and "CMDS" exist twice — as **deployable backend APIs** (nextgen) and as **legacy screens** (`lms-master`). The backend is production-ready; the screens are demo-grade.

---

## What production needs (beyond "it runs")

- **Data:** **MongoDB Atlas** replica set with auth + automated backups. *(Today: single node, no backup — top data-loss risk.)*
- **Storage:** dedicated **S3 bucket** + IAM role (drop the committed keys). *(Today: hardcoded AWS bucket + keys.)*
- **Edge:** **HTTPS/TLS** via ALB + ACM; private service ports; **auth + rate limiting** (API Gateway / filter).
- **Ops:** health/readiness probes, **CloudWatch** metrics + logs, and **CI/CD** (CodePipeline / GitHub Actions).
- **Cache:** provision **ElastiCache** (Redis code present, not run).

---

## Architecture notes (for technical planning)

- **Distributed monolith:** 8 services share **one MongoDB**; the "event bus" is a **Mongo-polling worker**, not Kafka.
- **Comfortable scale:** mid-size multi-tenant LMS — **~100k–500k users, low-thousands concurrent**.
- **First scaling ceiling:** the shared MongoDB (add read replicas → shard hot collections → split DB per domain).
- **No API gateway / service discovery** today — services use fixed host:port + shared DB.

---

## Cost & roadmap

| Stage | Setup | Cost/mo | Time |
|---|---|---|---|
| **Backend live** | 1 EC2 + Docker Compose + Atlas M10 | ~$330–400 | ~1 day |
| **Production-safe** | + Secrets Manager, dedicated S3/IAM, ALB TLS, backups | ~$350–450 | ~2–4 wks |
| **Scale-out** | ECS/EKS + Atlas M30 + ElastiCache + autoscale | ~$700–1,200+ | ~4–8 wks |

| Phase | Goal | Output |
|---|---|---|
| 0 | Decide & prepare | Confirm nextgen; pick UI path; AWS account + domain |
| 1 | Stand up | EC2 + Atlas, `docker compose up`, seed data, smoke-test |
| 2 | Harden | Rotate secrets → Secrets Manager, S3/IAM, ALB TLS, backups |
| 3 | UI | Build/port UI against nextgen APIs; end-to-end journey test |
| 4 | Scale-out | ECS/EKS + ElastiCache + autoscale, monitoring, CI/CD |

---

## Top risks

- **Legacy stack is a dead end** (EOL, x86-only, known bugs) — fine for a demo, not for production.
- **Committed credentials** (Critical) — rotate immediately, move to Secrets Manager.
- **No UI** (Critical) — cannot launch to end users without it.
- **Single DB, no backups** (High) — Atlas fixes this on day one.

---

## The ask

1. Approve **nextgen backend on AWS** as the production target (legacy = demo/reference only).
2. Assign an owner + decide the **UI direction**.
3. Approve the AWS starter spend (**~$330–400/mo**) and the phased path.

---
_Companion: step-by-step setup in `UPREP_AWS_DEPLOY_RUNBOOK.md`; interactive detail in the `uprep-cloud-deployment-review` canvas._
