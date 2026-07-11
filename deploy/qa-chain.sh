#!/bin/bash
# Exercises the CMDS authoring -> publish -> create-test -> fetch pipeline.
B=http://localhost:3000/api
U=6a397a810cf26ac35629e387
O=5874a52bc92ed65e3defc7e5

jqid() { sed -n 's/.*"id":"\([a-f0-9]*\)".*/\1/p'; }

echo '--- 1. author SCQ question ---'
QRES=$(curl -s -m 25 -X POST "$B/cmds/questions" -H 'Content-Type: application/json' \
  -d '{"userId":"'"$U"'","type":"SCQ","content":"What is 2 plus 2 (QA)","options":["3","4","5","6"],"correct":[1],"solution":"four","subject":"Math","difficulty":"EASY"}')
echo "$QRES"
QID=$(echo "$QRES" | jqid)
echo "QID=$QID"

echo '--- 2. publish ---'
curl -s -m 25 -X POST "$B/cmds/publish" -H 'Content-Type: application/json' \
  -d '{"ids":["'"$QID"'"],"userId":"'"$U"'"}'; echo

echo '--- 3. create test ---'
TRES=$(curl -s -m 25 -X POST "$B/cmds/tests" -H 'Content-Type: application/json' \
  -d '{"name":"Auto QA Test","userId":"'"$U"'","durationMin":10,"positive":4,"negative":1,"questionIds":["'"$QID"'"]}')
echo "$TRES"
TID=$(echo "$TRES" | jqid)
echo "TID=$TID"

echo '--- 4. fetch created test ---'
curl -s -m 25 -w '\nHTTP %{http_code}\n' "$B/tests/$TID?userId=$U&orgId=$O" | head -c 500; echo
echo '--- 5. papers (question + solution PDF data) ---'
curl -s -m 25 -o /dev/null -w 'papers HTTP %{http_code}\n' "$B/cmds/papers/$TID"
