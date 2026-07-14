# UPrep LMS — Production Deployment Brief

**Audience:** planning / stakeholders   **Status:** for review   **Owner:** _TBD_

---

## Page 1 — Executive Summary

### The situation
UPrep exists as **two parallel codebases**, and they are not interchangeable:

- **Modern backend** (`backend`) — Spring Boot / Java 17, **8 services, ~400 APIs**. Container-ready and cloud-deployable. **But it has no user interface — APIs only.**
- **Legacy full app** (`lms-master`) — the Play 1.2.4 / Java 6–7 stack we demo locally. It contains the **actual LMS and CMDS screens**, but every layer is ~10+ years end-of-life and x86-only. **Not cloud-deployable — reference only.**

**Headline:** The backend can go live on the cloud in ~1 day. The **user-facing UI is the real gap** and the biggest planning decision.

### What is deployable
| Deploy now (pilot, $0) | Decision / build needed | Do not deploy |
|---|---|---|
| Backend (8 services) | **The UI** (biggest item) | Legacy `lms-master` |
| Public website (PHP) | Rotate committed secrets | — |
| Virtual Classroom svc | Managed DB + storage + TLS | — |

### Three blockers before a public launch (P0)
1. **Leaked secrets** — live AWS + SMTP credentials are committed in source; must rotate + vault.
2. **No UI** — decide: port legacy UI, build a new SPA on the APIs, or launch website-only.
3. **Unverified boot** — confirm all 8 services start cleanly against a real database (one showed a wiring error).

### Recommended path & cost
| Stage | Setup | Cost/mo | Time |
|---|---|---|---|
| **Pilot** | 1 Oracle Ampere VM + Docker Compose | **$0** | ~1 day |
| **Early production** | + managed MongoDB, object storage, TLS | ~$60–150 | ~2–4 wks |
| **Full production** | Kubernetes + DB replica set + LB + autoscale | ~$300–800+ | ~4–8 wks |

> **Timeline reality:** ~1 day to a live $0 backend; **~2–4 weeks** to production-safe *excluding* the UI build.

### The ask
- Approve **nextgen backend** as the deployment target (legacy = reference only).
- Assign an owner + decision on the **UI direction**.
- Approve the **pilot → early production** path on Oracle Cloud.

---

## Page 2 — Detail for Planning

### Component deployability
| Component | Stack | Cloud-deployable? | Note |
|---|---|---|---|
| Backend: user, organization, content, **cmds**, board, social, comm, event | Spring Boot / Java 17 | **Yes** | Compose-ready; verify boot |
| Website (`website`) | PHP / Apache | **Yes** | Container or Vercel |
| Virtual Classroom | Spring Boot / Java 17 | **Yes** | Needs external BBB host |
| **LMS + CMDS UI** (`lms-master`) | Play 1.2.4 / Java 6–7 | **No** | EOL, x86-only → reference/data |
| MongoDB / Redis / Object storage | Infra | **Yes** | Provision + harden |
| Android apps, proxy-server, score-reader, mongo-scripts, sales-app, … | Mixed | Out of scope | Not part of server deploy |

> **Key nuance:** "LMS" and "CMDS" exist twice — as **deployable backend APIs** (nextgen) and as **non-deployable legacy screens** (`lms-master`). The backend is ready; the screens are the gap.

### What production needs (beyond "it runs")
- **Data:** managed MongoDB **replica set** (or Atlas) with auth + automated backups. *(Today: single node, no backup — top data-loss risk.)*
- **Storage:** migrate files to **OCI Object Storage** with IAM. *(Today: hardcoded AWS bucket + keys.)*
- **Edge:** **HTTPS/TLS** via load balancer; private service ports; **auth + rate limiting** at gateway.
- **Ops:** health/readiness probes, metrics, centralized logs, and **CI/CD** (all absent today).
- **Cache:** provision Redis (code present, not run).

### Architecture notes (for technical planning)
- **Distributed monolith:** 8 services share **one MongoDB**; the "event bus" is a **Mongo-polling worker**, not Kafka.
- **Comfortable scale:** mid-size multi-tenant LMS — **~100k–500k users, low-thousands concurrent**.
- **First scaling ceiling:** the shared MongoDB (add read replicas → shard hot collections → split DB per domain).
- No API gateway / service discovery today — services use fixed host:port + shared DB.

### Roadmap
| Phase | Goal | Output |
|---|---|---|
| 0 | Decide & prepare | Confirm nextgen; pick UI path; OCI account + domain |
| 1 | Stand up ($0) | Ampere VM, `docker compose up`, seed data, smoke-test APIs |
| 2 | Harden | Rotate/vault secrets, OCI storage, TLS, DB auth + backups |
| 3 | UI | Build/port UI against nextgen APIs; end-to-end journey test |
| 4 | Production | Managed DB + Redis, Kubernetes + LB + autoscale, monitoring, CI/CD |

### Top risks
- Legacy stack is a dead end (EOL, x86-only, known bugs) — do not host.
- Committed credentials (Critical) — rotate immediately.
- No UI (Critical) — product cannot launch to end users without it.
- Single DB, no backups (High) — provision HA + backups early.

---
_Full interactive detail (topology, gap table, risk register, cost) available in the `uprep-cloud-deployment-review` canvas._
