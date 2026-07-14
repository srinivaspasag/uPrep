#!/bin/bash -i
echo "Event Bus Management Console";
echo "============================";


#----------------------------------------------------------------
function __exit__ {
  echo "Bye";
  exit 0;
}

function __default__ {
  P_V="$1";
  P_DF="$2";
  if [ -z "$P_V" ]
  then
    echo "$P_DF";
  else
    echo "$P_V";
  fi;
}

function __help__ {
  echo "help:";
  P_TYPE=`__default__ "${C[1]}" "default"`;
  case "$P_TYPE" in
    "default")  __help_action__ "start";
                __help_action__ "stop";
                __help_action__ "status";
                __help_switch__;
      ;;
    "status")   __help_action__ "status";
      ;;
    "start")    __help_action__ "start";
      ;;
    "stop")     __help_action__ "stop";
      ;;
    "switch")   __help_switch__;
      ;;
    *)          echo "unknown option: $P_TYPE";
      ;;
  esac;
  echo;
}

function __help_action__ {
  echo "$1 [eventType]";
}

function __help_switch__ {
  echo "switch <ip> <port>";
}


function __hit_url__ {
  P_ACTION="$1";
  P_EVENT_TYPE="$2";
  P_URL="http://$EB_IP:$EB_PORT/EventBusProcessors/$P_ACTION";
  if [ -n "$P_EVENT_TYPE" ]
  then
    P_URL=$P_URL"?eventType=$P_EVENT_TYPE";
  else
    P_URL=$P_URL"All";
  fi;
  $T_CURL_PATH $P_URL;
  echo;
  echo "....................................................";
  echo;
}

function __do_action__ {
  P_ACTION="$1";
  P_TYPE=`__default__ "${C[1]}" "default"`;
  case "$P_TYPE" in
    "default") __hit_url__ "$P_ACTION";
      ;;
    *)         __hit_url__ "$P_ACTION" "$P_TYPE";
      ;;
  esac;
}

function __switch__ {
  EB_IP="$1";
  if [ -z "$EB_IP" ]
  then
    EB_IP="127.0.0.1";
  fi;
  EB_PORT="$2";
  if [ -z "$EB_PORT" ]
  then
    EB_PORT=80;
  fi;
}

#----------------------------------------------------------------


T_CURL_PATH=`which curl`;
if [ -z "$T_CURL_PATH" ]
then
  echo "curl not found... first install curl!"
  exit 1;
fi;

#set defaults
__switch__ "$1" "$2";


P_CMD="x";
while [ "$P_CMD" != "exit" ]
do
  read -p "$EB_IP:$EB_PORT > " -a C
  P_CMD=${C[0]};
  case "$P_CMD" in
    help)   __help__;
      ;;
    switch) __switch__  "${C[1]}" "${C[2]}";
      ;;
    status) __do_action__ "getStatus";
      ;;
    start)  __do_action__ "start";
      ;;
    stop)   __do_action__ "stop";
      ;;
    exit)   __exit__;
      ;;
    *)      if [ -n "$P_CMD" ]
            then 
              echo "unknown command: $P_CMD";
            fi;
            echo;
    ;;
  esac;
done;

#----------------------------------------------------------------
