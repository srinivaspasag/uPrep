#!/bin/bash
# Deploys platform/web to the live host — but only after the smoke suite
# (platform/web/smoke/run.sh) passes with 0 failures. Run from the repo root:
#   bash platform/deploy/deploy-web.sh          # asks before shipping
#   bash platform/deploy/deploy-web.sh --yes    # no prompt (still smoke-gated)
# Host comes from EIP (env) or platform/deploy/instance.env, else the address
# in DEPLOYMENT.md.
set -u
cd "$(dirname "$0")/../.."
KEY=platform/deploy/uprep-key.pem
[ -f platform/deploy/instance.env ] && . platform/deploy/instance.env
HOST="${EIP:-65.2.108.70}"
SSH="ssh -i $KEY -o ConnectTimeout=15"
BASE="https://$HOST.sslip.io"
RSYNC_OPTS=(-az --exclude node_modules --exclude .next --exclude 'public/uploads' --exclude smoke -e "$SSH")

echo "== 1/4 what would ship to $HOST"
CHANGES=$(rsync "${RSYNC_OPTS[@]}" -n -i platform/web/ "ubuntu@$HOST:~/uprep/uprep-ui/" | grep -v '^\.d' | grep -v 'tsbuildinfo\|package-lock.json$')
[ -z "$CHANGES" ] && { echo "Nothing to deploy."; exit 0; }
echo "$CHANGES"
echo "(uncommitted in platform/web:)"; git status --short platform/web

echo "== 2/4 smoke suite"
(cd platform/web && bash smoke/run.sh) || { echo "SMOKE FAILED — not deploying."; exit 1; }

if [ "${1:-}" != "--yes" ]; then
  read -r -p "Smoke passed. Deploy the files above to PRODUCTION ($HOST)? [y/N] " ok
  [ "$ok" = "y" ] || { echo "Cancelled."; exit 1; }
fi

echo "== 3/4 ship + rebuild"
rsync "${RSYNC_OPTS[@]}" platform/web/ "ubuntu@$HOST:~/uprep/uprep-ui/" || exit 1
$SSH "ubuntu@$HOST" 'cd ~/uprep && docker compose up -d --force-recreate ui' || exit 1
for i in $(seq 1 60); do
  LOG=$($SSH "ubuntu@$HOST" 'cd ~/uprep && docker compose logs --since 15m ui 2>&1 | tail -40')
  echo "$LOG" | grep -q "Ready in" && break
  echo "$LOG" | grep -qiE "Failed to compile|Build error|npm ERR" && { echo "$LOG"; echo "LIVE BUILD FAILED"; exit 1; }
  sleep 15
done

echo "== 4/4 live checks (read-only)"
FAIL=0
chk() { local code; code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE$1"); [ "$code" = "$2" ] && echo "  PASS  $1 → $code" || { echo "  FAIL  $1 → $code (want $2)"; FAIL=1; }; }
chk / 200
chk /login 200
chk /cmds 307
chk /api/cmds/content 401
chk /api/learn/courses 401
[ $FAIL -eq 0 ] && echo "Deployed and live." || { echo "LIVE CHECKS FAILED — investigate now."; exit 1; }
