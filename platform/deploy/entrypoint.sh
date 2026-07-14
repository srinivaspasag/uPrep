#!/bin/bash
# Boot the legacy Play services inside the committed lmsbe toolchain image.
# - JVM bytecode verification is disabled (legacy Play 2.1 on Zulu7 hits VerifyError).
# - Mongo is reached at localhost:27017 via the socat sidecar (see docker-compose.yml).
export JAVA_TOOL_OPTIONS="-Xverify:none"
export SBT_OPTS="-Xmx768m -Xms256m -Dsbt.override.build.repos=true"

ZW=/root/lms/vedantu/zowie

echo ">>> waiting for mongo bridge on 127.0.0.1:27017 ..."
for i in $(seq 1 90); do
  if (exec 3<>/dev/tcp/127.0.0.1/27017) 2>/dev/null; then echo ">>> mongo reachable"; break; fi
  sleep 2
done

start_svc() {
  local rel="$1" port="$2"
  local dir="$ZW/$rel"
  if [ ! -d "$dir" ]; then echo ">>> skip $rel (missing)"; return; fi
  cd "$dir"
  rm -f RUNNING_PID target/universal/stage/RUNNING_PID 2>/dev/null
  local log="/var/log/uprep-$(echo "$rel" | tr '/' '-').log"
  echo ">>> starting $rel on :$port  (log: $log)"
  nohup sbt -Dconfig.resource=local.conf -Dhttp.port="$port" "start $port" > "$log" 2>&1 &
  sleep 8
}

start_svc user/user-services 19011
start_svc content/content-services 19013
start_svc organization/organization-services 19012
start_svc board/board-services 19016

echo ">>> all services launched; tailing to keep container alive"
tail -f /dev/null
