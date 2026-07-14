#!/bin/bash
B=http://localhost:3000/api
U=6a397a810cf26ac35629e387
jqid(){ sed -n 's/.*"id":"\([a-f0-9]*\)".*/\1/p'; }

echo '--- 1. create folder ---'
FRES=$(curl -s -m 20 -X POST "$B/cmds/content" -H 'Content-Type: application/json' \
  -d '{"kind":"folder","name":"QA Folder","userId":"'"$U"'"}')
echo "$FRES"; FID=$(echo "$FRES" | jqid); echo "FID=$FID"

echo '--- 2. upload document WITH folderId ---'
printf 'PDFDATA-QA' > /tmp/qa.pdf
URES=$(curl -s -m 30 -X POST "$B/cmds/upload" -F kind=document -F name="Filed Doc" \
  -F userId="$U" -F folderId="$FID" -F file=@/tmp/qa.pdf)
echo "$URES"
FILE=$(echo "$URES" | sed -n 's#.*"url":"/uploads/\([^"]*\)".*#\1#p'); echo "FILE=$FILE"

echo '--- 3. content INSIDE folder (should list the doc) ---'
curl -s -m 20 "$B/cmds/content?parentId=$FID" | head -c 400; echo
echo '--- 4. content at ROOT (doc should NOT be here) ---'
curl -s -m 20 "$B/cmds/content" | grep -o '"title":"Filed Doc"' | head -1 && echo "BUG: found at root" || echo "OK: not at root"
