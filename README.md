# UPrep

Learning-management platform (LMS + CMDS) — a modern Next.js UI on top of the
UPrep/Learnpedia backend services, with native mobile wrappers.

For the full architecture and the legacy-to-cloud migration story, see
[`docs/UPREP_ARCHITECTURE_AND_MIGRATION.md`](docs/UPREP_ARCHITECTURE_AND_MIGRATION.md).

## Repository layout

| Path | What it is |
|------|------------|
| `uprep-ui/` | **Main app** — Next.js + React + TypeScript. Student LMS (`/learn/*`) and admin CMDS (`/cmds/*`), with ~47 API routes talking to MongoDB. |
| `uprep-android/` | Native Android wrapper (Kotlin WebView) around the deployed web app. |
| `uprep-ios/` | Native iOS wrapper (Swift `WKWebView`). Generated with XcodeGen. |
| `lms_nextgen-master/` | Next-gen backend — Spring Boot API services (the long-term backend). Built into one image; each service selected via `SERVICE` env var. |
| `lms-master/` | Legacy Play Framework system (Play 2.1 backend services + Play 1.2.4 UI). Still containerized for the current deploy. |
| `virtual-classroom-service-main/` | Virtual classroom service. |
| `uprep-website-master/` | Public marketing site (PHP). |
| `lms-local/` | Dockerfiles + seed scripts for running the legacy stack locally. |
| `mongo-scripts-master/`, `scripts-master/` | Database and ops scripts. |
| `proxy-server-master/`, `secure-api-master/`, `logs-server-master/`, `sdcard-server-master/`, `organisation-logos-master/` | Supporting legacy microservices. |
| `deploy/` | Deployment scripts, Caddyfile, compose for the host. |
| `docs/` | Architecture and migration documentation. |

## Quick start (local)

Prerequisites: Docker + Docker Compose, Node.js 18+.

```bash
cp .env.example .env        # fill in values

# Backend services + Mongo (full stack)
docker compose up -d --build
# ...or the lite subset for a small box:
# docker compose -f docker-compose.lite.yml up -d --build

# The web app
cd uprep-ui
npm install
npm run dev                 # http://localhost:3000
```

Mobile apps have their own build instructions in `uprep-android/README.md` and
`uprep-ios/README.md`.

## Notes

- Secrets (`.env`, keystores, `*.pem`) are git-ignored — never commit them.
- Large legacy/peripheral projects (the old Windows "Score" desktop app, the
  offline-assessment PHP app, and the legacy Android apps) are intentionally not
  in this repo; they live in the separate archive backups.
