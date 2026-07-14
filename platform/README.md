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

## Build & deploy

The live deployment details (host, containers, source↔host mapping) are in
[`deploy/DEPLOYMENT.md`](deploy/DEPLOYMENT.md). Run these from the repo root.

### A) Redeploy the web app (most common — what's live)

The `ui` container builds itself from mounted source on start, so you ship
source and recreate it (no image build):

```bash
# 1. optional local sanity check
cd platform/web && npm install && npm run build && cd ../..

# 2. ship source to the box (no --delete; skips deps/uploads)
rsync -az --exclude node_modules --exclude .next --exclude 'public/uploads' \
  -e "ssh -i platform/deploy/uprep-key.pem" \
  platform/web/ ubuntu@<EIP>:~/uprep/uprep-ui/

# 3. recreate the container (re-runs npm install + next build + start)
ssh -i platform/deploy/uprep-key.pem ubuntu@<EIP> \
  'cd ~/uprep && docker compose up -d --force-recreate ui && docker compose logs -f ui'
```

`<EIP>` is the host IP in `platform/deploy/instance.env` (git-ignored).

### B) Build a Docker image + save/load onto the server

No registry needed — `docker save` locally, `docker load` on the box:

```bash
cd platform

# 1. build (nextgen backend -> uprep-lms:latest)
docker compose build            # or: docker build -t uprep-lms:latest ./backend

# 2. save -> copy -> load -> run
docker save uprep-lms:latest | gzip > uprep-lms.tar.gz
scp -i deploy/uprep-key.pem uprep-lms.tar.gz ubuntu@<EIP>:~/uprep/
ssh -i deploy/uprep-key.pem ubuntu@<EIP> \
  'cd ~/uprep && gunzip -c uprep-lms.tar.gz | docker load && docker compose up -d'
```

Bundle all images at once (like the existing `images.tar.gz` on the box):

```bash
docker save uprep-lmsbe:aws mongo:3.4 caddy:2 node:20-bullseye alpine/socat \
  | gzip > images.tar.gz            # on box: gunzip -c images.tar.gz | docker load
```

### C) Full stack locally (dev)

```bash
cd platform
cp .env.example .env
docker compose up -d --build                       # backend + mongo + website
# lite subset: docker compose -f docker-compose.lite.yml up -d --build
cd web && npm install && npm run dev               # http://localhost:3000
```

### Verify after any deploy

```bash
BASE=https://<EIP>.sslip.io
curl -sL -o /dev/null -w "%{http_code}\n" $BASE/    # expect 200
bash platform/deploy/qa-chain.sh                     # CMDS pipeline end-to-end
```

## Notes

- Secrets (`.env`, keystores, `*.pem`) are git-ignored — never commit them.
  Credentials previously hardcoded in legacy config have been redacted to
  `__REDACTED__`; supply real values via environment/secret configuration.
- Legacy projects (`lms-master`, `lms-local`, and supporting legacy services)
  live under `../legacy/` at the repo root; they are intentionally outside this
  platform folder and are not needed to build or run it.
