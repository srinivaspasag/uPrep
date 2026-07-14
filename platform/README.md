# UPrep Platform

The modern, maintained UPrep stack — backend, web, mobile, and supporting
services — self-contained in one folder. Legacy code is **not** here; it stays
at the repository root under its original project folders.

## Layout

| Path | What it is |
|------|------------|
| `backend/` | Spring Boot API backend (Java 17). 8 domain services built from one image, each selected via the `SERVICE` env var. |
| `web/` | Next.js + React + TypeScript app — student LMS (`/learn/*`) and admin CMDS (`/cmds/*`). |
| `website/` | Public marketing site (PHP). |
| `mobile/android/` | Native Android wrapper (Kotlin WebView). |
| `mobile/ios/` | Native iOS wrapper (Swift `WKWebView`, generated with XcodeGen). |
| `services/virtual-classroom/` | Virtual classroom service (Spring Boot). |
| `observability/logs-server/` | Log collection service. |
| `mongo-scripts/` | Database seed/maintenance scripts. |
| `deploy/` | Deployment scripts, Caddyfile, host compose. |
| `docs/` | Architecture, deploy runbook, go-live checklist, and other docs. |
| `docker-compose.yml` / `docker-compose.lite.yml` | Full stack / small-box subset. |

## Quick start

Prerequisites: Docker + Docker Compose, Node.js 18+.

```bash
cp .env.example .env        # fill in values (never commit .env)

# Backend services + Mongo (run from this folder)
docker compose up -d --build
# ...or the lite subset for a ~1 GB box:
# docker compose -f docker-compose.lite.yml up -d --build

# Web app
cd web
npm install
npm run dev                 # http://localhost:3000
```

Mobile build steps: `mobile/android/README.md` and `mobile/ios/README.md`.

## Notes

- Secrets (`.env`, keystores, `*.pem`) are git-ignored — never commit them.
  Credentials previously hardcoded in legacy config have been redacted to
  `__REDACTED__`; supply real values via environment/secret configuration.
- Legacy projects (`lms-master`, `lms-local`, and supporting legacy services)
  live under `../legacy/` at the repo root; they are intentionally outside this
  platform folder and are not needed to build or run it.
