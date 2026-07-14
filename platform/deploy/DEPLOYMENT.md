# UPrep — Live Deployment Record

Source of truth for **what is currently running in production** and how to
redeploy it. Keep this file in sync whenever the live deployment changes.

_Last verified: 2026-07-12._

## Where it runs

| Item | Value |
|------|-------|
| Public URL | https://65.2.108.70.sslip.io (HTTPS via Caddy + sslip.io) |
| Cloud | AWS EC2, region `ap-south-1` (Mumbai) |
| Host | Ubuntu, x86_64, 2 vCPU / 7.6 GB RAM + 4 GB swap, 78 GB disk |
| Deploy dir on host | `/home/ubuntu/uprep` |
| Orchestration | Docker Compose (`docker-compose.yml` in this folder) |

> Exact instance/EIP/security-group IDs and the SSH key are **not** in git.
> They live in the git-ignored files in this folder: `instance.env`, `sg.env`,
> `uprep-key.pem`. SSH in with:
> `ssh -i platform/deploy/uprep-key.pem ubuntu@<EIP from instance.env>`

## What runs (containers)

| Container | Image | Role |
|-----------|-------|------|
| `uprep-caddy-1` | `caddy:2` | TLS + reverse proxy, `:80/:443` → `ui:3000` |
| `uprep-ui-1` | `node:20-bullseye` | Next.js app (this repo's `platform/web`); builds + serves on `:3000` |
| `uprep-lmsbe-1` | `uprep-lmsbe:aws` | **Legacy Play backend** (from `legacy/lms-master`), API services on `:19011+` |
| `uprep-socat-1` | `alpine/socat` | Forwards `127.0.0.1:27017` → `mongo` for the legacy services |
| `uprep-mongo-1` | `mongo:3.4` | Database (`localvedantu`), volume `mongo-data` |

**Note:** the live backend is the **legacy `lmsbe`**, not the nextgen
`platform/backend`. The nextgen backend is not deployed on this host.

## Source ↔ deployment mapping (all tracked in git)

| On the host (`~/uprep/…`) | In this repo |
|---------------------------|--------------|
| `uprep-ui/` (app source) | `platform/web/` |
| `lms-master/` (legacy backend source) | `legacy/lms-master/` |
| `docker-compose.yml`, `Caddyfile`, `entrypoint.sh`, `qa-chain.sh`, `folder-test.sh` | `platform/deploy/` (identical) |

## NOT in git (rebuild or copy separately)

These are intentionally excluded (secrets or large binaries) and live only on
the host / locally in `platform/deploy/`:

- `uprep-key.pem`, `instance.env`, `sg.env` — secrets / infra IDs
- `images.tar.gz` (~378 MB) — saved Docker images (`uprep-lmsbe:aws`, etc.)
- `lms-master.tgz` (~194 MB), `uprep-ui.tgz` (~54 MB) — shipped source snapshots
- `localvedantu.archive` — Mongo dump (seed/restore)
- `ui-*.tgz`, `uprep-payload.tgz` — incremental patch bundles

We track **source + config**, and rebuild artifacts from source — we do not
commit the large binaries (they exceed sensible git limits).

## Redeploy the web app (`ui`)

From a machine with the deploy key:

```bash
# 1) ship current web source to the host (no --delete; skips deps/uploads)
rsync -az --exclude node_modules --exclude .next --exclude 'public/uploads' \
  -e "ssh -i platform/deploy/uprep-key.pem" \
  platform/web/ ubuntu@<EIP>:~/uprep/uprep-ui/

# 2) recreate the ui container (re-runs npm install + next build + start)
ssh -i platform/deploy/uprep-key.pem ubuntu@<EIP> \
  'cd ~/uprep && docker compose up -d --force-recreate ui && docker compose logs -f ui'
```

Tip: before recreating, snapshot the current build for rollback:
`sudo cp -a ~/uprep/uprep-ui/.next ~/uprep/uprep-ui/.next.bak`

## Verify after deploy

```bash
BASE=https://65.2.108.70.sslip.io
curl -sL -o /dev/null -w "%{http_code}\n" $BASE/            # expect 200
bash platform/deploy/qa-chain.sh                            # CMDS pipeline (author→publish→test)
```

## Common operations (on the host, in `~/uprep`)

```bash
docker compose ps                       # what's running
docker compose logs -f ui               # tail the web app
docker compose restart lmsbe            # restart legacy backend
docker compose up -d --force-recreate ui  # redeploy web after rsync
```
