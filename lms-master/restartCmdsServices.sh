#!/bin/bash

# set -x

source /etc/profile.d/java.sh
source /etc/profile.d/play.sh

# echo "Env PATH:$PATH";
T_SCRIPT="$0";
T_SCRIPT_DIR=`dirname $(readlink -f "$T_SCRIPT")`;
T_SCRIPT_DIR_NAME=`basename $T_SCRIPT_DIR`;
T_PROPERTY_FILE_PREFIX=$T_SCRIPT_DIR_NAME;

T_FWK_ID="prod";
T_APP_PORT="19014";
T_APP_START_MODE="prod";
T_APP_NAME="cmds/cmds-services";

if [ -z "$T_PROPERTY_FILE_PREFIX" ]
then
  echo "error: could not find basename for qualifying property files"
  exit 1;
fi;

T_VEDANTU_FWKID_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"FwkId";
T_VEDANTU_ALLOWEDAPPS_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"AllowedApps";
T_VEDANTU_CLUSTEREDAPPS_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"ClusteredApps";
T_VEDANTU_PLAYPATH_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"PlayPath";

if [ ! -e "$T_SCRIPT_DIR/logs" ]
then
  mkdir "$T_SCRIPT_DIR/logs";
fi;

function __create_tmp_file__ {
  _tmp_file="/tmp/vPlay_"$RANDOM".tmp";
  touch "$_tmp_file";
  echo "$_tmp_file";
}


function __remove_tmp_file__ {
  rm -f "$1";
}


function __fappend__ {
  echo "$2" >> "$1";
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

function __app_path__ {
  _t_app_path="$PRJ_PATH/$1";
  if [ -d "$_t_app_path" ]
  then
    _t_app_conf_path="$_t_app_path/conf/application.conf";
    if [ -f "$_t_app_conf_path" ]
    then
      echo "$_t_app_path";
    fi;
  fi;
}


function __get_play_path__ {
  _get_playPath_app="$1";
  _playPath="$2";
  if [ -z "$_playPath" ]
  then
    if [ -f "$T_VEDANTU_APPS_PLAYPATH_FILE" ]
    then
      _tPlayPathWithApp=`grep -e "$_get_playPath_app\(:\|$\)" "$T_VEDANTU_APPS_PLAYPATH_FILE"`;
      _playPath=`echo "$_tPlayPathWithApp" | cut -d":" -f2`;
      if [ "$_playPath" == "$_tPlayPathWithApp" ]
      then
        _playPath="";
      fi;
    fi;
    if [ -z "$_playPath" ]
    then
      if [ -f "$T_VEDANTU_PLAYPATH_FILE" ]
      then
        _playPath=`head -1 "$T_VEDANTU_PLAYPATH_FILE"`;
      fi;
    fi;
  fi;
  echo "$_playPath/play";
}


function __generate_start__ {
  _generate_start_app_dir="$1";
  _generate_start_play_version="$2";
  _generate_start_tmp_cp_file="$3";

  _generate_start_target_dir="$_generate_start_app_dir/target";
  _generate_start_cp_file="$_generate_start_target_dir/vClasspath";

  rm -f "$_generate_start_cp_file";
  touch "$_generate_start_cp_file";
  _generate_start_commence="false";
  while read _generate_start_line
  do
    _generate_start_line=`echo "$_generate_start_line" | tr -d [:space:]`;
    #echo "_generate_start_line: $_generate_start_line";
    #echo "_generate_start_commence: $_generate_start_commence";
    if [ -z "$_generate_start_line" ]
    then
      continue;
    fi;

    if [  "$_generate_start_commence" == "false" ]
    then
      _generate_start_commence_check=`echo "$_generate_start_line" | grep "computedclasspathof" | wc -l `;
      if [ "$_generate_start_commence_check" -eq "1" ]
      then
        _generate_start_commence="true";
      fi;
      continue;
    fi;

    #echo "$_generate_start_line";
    _generate_start_jar="";
    if [ "`echo $_generate_start_line | grep /target/scala-2.10/classes`" == "$_generate_start_line" ]
    then
      _generate_start_app_dir=`echo "$_generate_start_line" | sed "s#"/target/scala-2.10/classes"##g" | sed "s#\(.*\)"$T_SCRIPT_DIR"##g"`;
      _generate_start_app_dir="$T_SCRIPT_DIR/$_generate_start_app_dir";
      #echo "_generate_start_app_dir: $_generate_start_app_dir";
      _generate_start_app_name=`basename "$_generate_start_app_dir"`;
      _generate_start_app_version=`grep -e "val[[:space:]]*appVersion" "$_generate_start_app_dir/project/Build.scala" | awk '{print $NF}' | tr -d \"`;
      _generate_start_jar=$_generate_start_app_name"_"$_generate_start_play_version"-"$_generate_start_app_version".jar";
    else
      _generate_start_jar=`echo "$_generate_start_line" | awk -F/ '{print $NF}'`;
    fi;
    #echo "jar: $_generate_start_jar";
    if [ -n "$_generate_start_jar" ]
    then
      echo "$_generate_start_jar" >> "$_generate_start_cp_file";
      echo "jar: $_generate_start_jar";
    fi;

  done < "$_generate_start_tmp_cp_file";
  rm -f "$_generate_start_tmp_cp_file";

  cp $T_SCRIPT_DIR"/vStart" $_generate_start_target_dir"/vStart";
  chmod +x $_generate_start_target_dir"/vStart";
}


function __start__ {
  _start_app=`__app_path__ "$1"`;
  if [ -z "$_start_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  echo "app: $_start_app";
  fwkId=$T_FWK_ID;
  echo "fwkId : $fwkId";
  _play_path=`__get_play_path__ "$1"`;
  _start_app_name=`basename $_start_app`;
  _commons_log_conf=$T_SCRIPT_DIR"/commons/conf/logger.xml";
  _target_log_conf=$_start_app"/conf/logger.xml";
  #ln -sfn $_commons_log_conf $_target_log_conf;
  #removed pre_script code.
  if [ -n "$fwkId" ]
  then
    _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
    if [ -n "$_play_version" ]
    then
      _start_response=`$_play_path start $_start_app --%$fwkId -DVEDANTU_APP_NAME=$_start_app_name`;
      _start_exit_code="$?"
    else
      _start_port=$T_APP_PORT;
      _start_mode=$T_APP_START_MODE;
      _start_memory=`__get_app_memory__ "$_play_path" "$1" "$fwkId"`;
      cd $_start_app;
      ln -sfn $T_SCRIPT_DIR/logs;
      _start_command="start";
      if [ "dev" == "$_start_mode" ]
      then
        _start_command="run";
      fi;
      if [ "cmds-services" == "$_start_app_name" ] && [ "prod" == "$fwkId" ]
      then
        _start_memory="$_start_memory -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:PermSize=128M -XX:MaxPermSize=256M";
      fi;
      echo "configuration: $_start_command $_start_memory -Dhttp.port=$_start_port -Dconfig.resource=$fwkId.conf -DVEDANTU_APP_NAME=$_start_app_name -DLOG_FILE_NAME=$_start_app_name";
      #echo "Please press CTRL+D after few seconds";
      # export JAVA_OPTS="-Xms512M -Xmx1024M -XX:MaxPermSize=192M";
      $_play_path compile;
      sleep 2;
      $_play_path stage;
      sleep 2;
      _start_tmp_cp_file="`tempfile`";
      $_play_path classpath > $_start_tmp_cp_file;
      cd -;
      __generate_start__ "$_start_app" "2.10" "$_start_tmp_cp_file";
      export JAVA_OPTS=
      $_start_app/target/vStart -Dhttp.port=$_start_port -Dconfig.resource=$fwkId.conf -DVEDANTU_APP_NAME=$_start_app_name -DLOG_FILE_NAME=$_start_app_name $_start_memory > /dev/null &
      _start_proc_pid=$!;
      echo "started process with pid: $_start_proc_pid";
      _start_exit_code="$?"
      _start_response="not captured for apps running with play! version 2.x";
    fi;
  else
    _start_response=`$_play_path start $_start_app -DVEDANTU_APP_NAME=$_start_app_name`;
    _start_exit_code="$?"
  fi;
  echo "--------------------------------------";
  echo "$_start_response";
  echo "exit code: "$_start_exit_code;
  echo "--------------------------------------";
  if [ "$_start_exit_code" -ne "0" ]
  then
    _start_failed_reason=`echo "$_start_response" | grep -i "is already started!"`;
    if [ -n "$_start_failed_reason" ]
    then
      _start_app_conf="$_start_app/conf";
      _start_using_ps=`ps aux | grep "$_start_app_conf" | grep -v grep | wc -l`;
      if [ "$_start_using_ps" -eq "0" ]
      then
        echo "app is not running, but its not getting started.. please contact administrator";
      else
        echo "app is already running";
      fi;
    else
      echo "not able to determine why the app is not running.. please contact administrator!";
    fi;
  else
    _start_msg_body="
--------------------------------------
$_start_response
exit code: $_start_exit_code
--------------------------------------
";
    #__send_mail__ "[`hostname`] app start - $1" "tech@vedantu.com" "apps@vedantu.com" "$_start_msg_body";
  fi;
}


function __stop__ {
  _stop_app=`__app_path__ "$1"`;
  if [ -z "$_stop_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  _play_path=`__get_play_path__ "$1"`;
  echo "app: $_stop_app";
  _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
  if [ -n "$_play_version" ]
  then
    _stop_response=`$_play_path stop $_stop_app`;
    _stop_exit_code="$?";
  else
    cd $_stop_app;
    _stop_response=`$_play_path stop`;
    _stop_exit_code="$?";
    cd -;
  fi;
  echo "--------------------------------------";
  echo "$_stop_response";
  echo "exit code: "$_stop_exit_code;
  echo "--------------------------------------";
  if [ "$_stop_exit_code" -ne "0" ]
  then
    _stop_failed_reason=`echo "$_stop_response" | grep -i "is not started"`;
    if [ -n "$_stop_failed_reason" ]
    then
      _stop_app_conf="$_stop_app/conf";
      _stop_using_ps=`__get_app_ps_id__ "$_stop_app" | wc -l`;
      if [ "$_stop_using_ps" -eq "0" ]
      then
        echo "app is not running";
      else
        echo "app is already running but being reported as stopped.. please contact administrator";
      fi;
    else
      echo "not able to determine why the app is not stopping.. please contact administrator!";
    fi;
  else
    _stop_msg_body="
--------------------------------------
$_stop_response
exit code: $_stop_exit_code
--------------------------------------
";
  #  __send_mail__ "[`hostname`] app stop - $1" "tech@vedantu.com" "apps@vedantu.com" "$_stop_msg_body";
  fi;
}

function __get_app_memory__ {
  _play_path="$1";
  _app_path="$PRJ_PATH/$2";
  _app_fwkId="$3";
  _app_memory="";
  if [ -n "$_app_fwkId" ]
  then
    if [ -f "$_app_path/conf/$_app_fwkId.conf" ]
    then
      _app_memory=`cat $_app_path/conf/$_app_fwkId.conf | grep "jvm\.memory" | awk -F= '{print $2}'`;
    fi;
  fi;
  if [ -z "$_app_memory" ]
  then
    _app_memory=`grep "\([\s]*[#]*[\s]*\)jvm\.memory=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
  fi;
  echo $_app_memory | tr -d \";
}


function __get_app_ps_id__ {
  _app="$1";
  _ps_id="";
  _get_app_by_VEDANTU_APP_NAME="\-DVEDANTU_APP_NAME\=`basename $_app`";
  _get_app_using_ps=`ps aux | grep "$_get_app_by_VEDANTU_APP_NAME" | grep -v "grep "`;
  if [ -n "$_get_app_using_ps" ]
  then
    _ps_id=`echo "$_get_app_using_ps" | awk '{print $2}'`;
  fi;
  echo "$_ps_id";
}

function __do_action_multi__ {
  _t_cmd="__"$1"__";
  "$_t_cmd" "$2" "$3"
}


function __do_action__ {
  P_ACTION="$1";
  P_APP="$2";
  if [ -z "$P_APP" ]
  then
    echo "no app specified";
    echo;
    return;
  fi;

  case "$P_ACTION" in
    status) __do_action_multi__ "status" "$P_APP";
      ;;
    start)  __do_action_multi__ "start"  "$P_APP" "$3";
      ;;
    stop)   __do_action_multi__ "stop"   "$P_APP";
      ;;
    *)      echo "unsupported action ["$P_ACTION"]";
            echo;
      ;;
  esac;
}

function __switch__ {
  PRJ_PATH="$1";
  if [ -z "$PRJ_PATH" ]
  then
    echo "RScript Error: Could not find Project Path!"
    return
  fi;
}

#set defaults
__switch__ "$T_SCRIPT_DIR";

pid_before_stop=`__get_app_ps_id__ "$T_APP_NAME"`;
__do_action__ stop "$T_APP_NAME";
stop_action_exit_code="$?";

sleep 2
if [ "$stop_action_exit_code" -ne "0" ]
then
  echo "RScript: Stop action seems to have failed. PID: $pid_before_stop";
  kill -9 "$pid_before_stop";
fi;

pid_after_stop=`__get_app_ps_id__ "$T_APP_NAME"`;
if [ -n "$pid_after_stop" ]
then
  echo "RScript: Process with pid $pid_after_stop is still running";
  kill -9 "$pid_after_stop";
fi;

sleep 1
final_check_after_stop=`__get_app_ps_id__ "$T_APP_NAME"`;
if [ -n "$final_check_after_stop" ]
then
  echo "RScript Error: Stop process failed! Fatal Error!";
  return
else
  echo "RScript Stop Success!";
fi;

__do_action__ start "$T_APP_NAME"
start_action_exit_code="$?";
if [ "$start_action_exit_code" -ne "0" ]
then
  echo "RScript Error: Start action seems to have failed. Fatal Error!";
fi;

sleep 3
check_after_start=`__get_app_ps_id__ "$T_APP_NAME"`;
if [ -n "$check_after_start" ]
then
  echo "RScript Start Success!";
else
  echo "RScript Error: Start action failed";
fi;

# set +x