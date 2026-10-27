#!/bin/bash
# Pre-deploy smoke suite: builds the app, runs it against a throwaway Mongo
# (docker) seeded with fake orgs/students, runs smoke/checks.mjs, tears down.
# Never touches production. Usage (from platform/web): bash smoke/run.sh
# Pass --no-build to reuse an existing .next build.
set -u
cd "$(dirname "$0")/.."
PORT=3457; MPORT=27999; NAME=uprep-smoke-mongo
TMP=$(mktemp -d)
export MONGO_URI="mongodb://127.0.0.1:$MPORT" MONGO_DB=smoke SESSION_SECRET=smoke-secret UPLOAD_DIR="$TMP/uploads" SMOKE_BASE="http://localhost:$PORT"

cleanup() { [ -n "${SRV:-}" ] && kill "$SRV" 2>/dev/null; docker rm -f $NAME >/dev/null 2>&1; rm -rf "$TMP"; }
trap cleanup EXIT

if [ "${1:-}" != "--no-build" ]; then
  echo "== build"; npx next build > "$TMP/build.log" 2>&1 || { tail -30 "$TMP/build.log"; echo "BUILD FAILED"; exit 1; }
fi
echo "== database"
docker rm -f $NAME >/dev/null 2>&1
docker run -d --rm --name $NAME -p 127.0.0.1:$MPORT:27017 mongo:4.4 >/dev/null || exit 1
for i in $(seq 1 30); do node -e "require('mongodb').MongoClient.connect(process.env.MONGO_URI,{useUnifiedTopology:true,serverSelectionTimeoutMS:1000}).then(c=>{c.close();process.exit(0)}).catch(()=>process.exit(1))" && break; sleep 1; done
node smoke/seed.cjs > "$TMP/ids.json" || exit 1
echo "== server"
npx next start -p $PORT > "$TMP/server.log" 2>&1 & SRV=$!
for i in $(seq 1 60); do curl -s -o /dev/null "http://localhost:$PORT/login" && break; sleep 1; done
echo "== checks"
node smoke/checks.mjs "$TMP/ids.json"; RC=$?
[ $RC -ne 0 ] && { echo "--- server log tail"; tail -20 "$TMP/server.log"; }
exit $RC
