#!/bin/bash -i
echo "App Management Console";
echo "=======================";

T_SCRIPT="$0";
T_SCRIPT_DIR=`dirname $(readlink -f "$T_SCRIPT")`;
T_SCRIPT_DIR_NAME=`basename $T_SCRIPT_DIR`;
T_PROPERTY_FILE_PREFIX=$T_SCRIPT_DIR_NAME;

if [ -z "$T_PROPERTY_FILE_PREFIX" ]
then
  echo "error: could not find basename for qualifying property files"
  exit 1;
fi;

T_VEDANTU_FWKID_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"FwkId";
T_VEDANTU_ALLOWEDAPPS_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"AllowedApps";
T_VEDANTU_CLUSTEREDAPPS_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"ClusteredApps";
T_VEDANTU_SENDMAIL="$HOME/."$T_PROPERTY_FILE_PREFIX"SendMail";
T_VEDANTU_PLAYPATH_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"PlayPath";
T_VEDANTU_APPS_PLAYPATH_FILE="$HOME/."$T_PROPERTY_FILE_PREFIX"AppsPlayPath";

if [ ! -e "$T_SCRIPT_DIR/logs" ]
then
  mkdir "$T_SCRIPT_DIR/logs";
fi;


#----------------------------------------------------------------
function __exit__ {
  echo "Bye";
  exit 0;
}


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


function __send_mail__ {
  if [ ! -f "$T_VEDANTU_SENDMAIL" ]
  then
    return;
  fi;
  if [ -z "`which sendmail`" ]
  then
    echo "sendmail not found.. please install..";
    return;
  fi;
  echo "sending notification...";
  _sendmail_subject="$1";
  _sendmail_to="$2";
  _sendmail_from="$3";
  _sendmail_msg_body="$4";
  _sm_tmp_file=`__create_tmp_file__`;
  __fappend__ $_sm_tmp_file "From: $_sendmail_from";
  __fappend__ $_sm_tmp_file "To: $_sendmail_to";
  __fappend__ $_sm_tmp_file "Reply-To: $_sendmail_from";
  __fappend__ $_sm_tmp_file "Subject: $_sendmail_subject";
  __fappend__ $_sm_tmp_file "";
  __fappend__ $_sm_tmp_file "$_sendmail_msg_body";
  __fappend__ $_sm_tmp_file "";
  __fappend__ $_sm_tmp_file "";
  cat $_sm_tmp_file|sendmail -t;
  #__remove_tmp_file__ "$_sm_tmp_file";
  echo "completed sending notification";
  #sendmail -f "$_sendmail_from" -s "$_sendmail_subject" "$_sendmail_to" < "$_sendmail_msg_file";
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
    "default")  __help_action__ "start" "fwkId";
                __help_action__ "stop";
                __help_action__ "status";
                __help_action__ "clean";
                __help_action__ "deps" "--sync";
                __help_show__;
                __help_switch__;
                __help_idlist__;
      ;;
    "show")     __help_show__;
      ;;
    "status")   __help_action__ "status";
      ;;
    "start")    __help_action__ "start";
      ;;
    "stop")     __help_action__ "stop";
      ;;
    "clean")    __help_action__ "clean";
      ;;
    "deps")     __help_action__ "deps" "--sync";
      ;;
    "switch")   __help_switch__;
      ;;
    "idlist")   __help_idlist__;
      ;;
    *)          echo "unknown option: $P_TYPE";
      ;;
  esac;
  echo;
}


function __help_action__ {
  if [ -n "$2" ]
  then
    echo "$1 <appId[,appId[,appId...]] [$2]>";
    echo "$1 <id:clusterId [$2]>";
  else
    echo "$1 <appId[,appId[,appId...]]>";
    echo "$1 <id:clusterId>";
  fi;
  echo "  -note: there should be no space in appIds separated by commas";
}


function __help_switch__ {
  echo "switch </path/to/project>";
}


function __help_show__ {
  echo "show [appId[,appId[,appId...]]]";
  echo "show <id:clusterId>";
  echo "  -note: there should be no space in appIds separated by commas";
}


function __help_idlist__ {
  echo "idlist [clusterid]";
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


function __status__ {
  _status_app=`__app_path__ "$1"`;
  if [ -z "$_status_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  echo "app: $_status_app";
  _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
  if [ -z "$_play_version" ]
  then
    echo "Not supported for play! version 2.x. Please use 'show' instead";
    exit 1;
  fi;
  _play_path=`__get_play_path__ "$1"`;
  _status_response=`$_play_path status $_status_app`;
  _status_exit_code="$?";
  echo "--------------------------------------";
  echo "$_status_response";
  echo "exit code: "$_status_exit_code;
  echo "--------------------------------------";
  if [ "$_status_exit_code" -ne "0" ]
  then
    _status_failed_reason=`echo "$_status_response" | grep -i "Cannot retrieve the application status" | grep "\(503\)"`;
    if [ -n "$_status_failed_reason" ]
    then
      _status_app_conf="$_status_app/conf";
      _status_using_ps=`ps aux | grep "$_status_app_conf" | grep -v grep | wc -l`;
      if [ "$_status_using_ps" -eq "0" ]
      then
        echo "app is not running!";
      else
        echo "app is running.. it seems to be waiting for its first request";
      fi;
    else
      _status_failed_reason=`echo "$_status_response" | grep -i "Cannot contact the application"`;
      if [ -n "$_status_failed_reason" ]
      then
        echo "app is not running!";
      else
        echo "not able to determine why the app is not running.. please contact administrator!";
      fi;
    fi;
  fi;
}


function __is_allowed__ {
  isAllowed="true";
  if [ -f "$T_VEDANTU_ALLOWEDAPPS_FILE" ]
  then
    isAllowed="false";
    t_app_from_allowed=`grep -e "$1\(:\|$\)" "$T_VEDANTU_ALLOWEDAPPS_FILE" | cut -d":" -f1`;
    if [ "$t_app_from_allowed" == "$1" ]
    then
      isAllowed="true";
    fi;
  fi;
  echo "$isAllowed";
}


function __get_fwk_id__ {
  _get_fwkId_app="$1";
  _fwkId="$2";
  if [ -z "$_fwkId" ]
  then
    if [ -f "$T_VEDANTU_ALLOWEDAPPS_FILE" ]
    then
      _tFwkIdWithApp=`grep -e "$_get_fwkId_app\(:\|$\)" "$T_VEDANTU_ALLOWEDAPPS_FILE"`;
      _fwkId=`echo "$_tFwkIdWithApp" | cut -d":" -f2`;
      if [ "$_fwkId" == "$_tFwkIdWithApp" ]
      then
        _fwkId="";
      fi;
    fi;
    if [ -z "$_fwkId" ]
    then
      if [ -f "$T_VEDANTU_FWKID_FILE" ]
      then
        _fwkId=`head -1 "$T_VEDANTU_FWKID_FILE"`;
      fi;
    fi;
  fi;
  if [ -n "$_fwkId" ]
  then
    _fwkId=`echo "$_fwkId" | sed "s#--%\(.*\)#\1#g"`;
  fi;
  echo "$_fwkId";
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


function __get_clustered_apps__ {
  _get_clustered_apps="$1";
  _lower_get_clustered_apps=`echo "$1" | tr A-Z a-z`;
  if [[ "$_lower_get_clustered_apps" == "id:"* ]]
  then
    if [ -f "$T_VEDANTU_CLUSTEREDAPPS_FILE" ]
    then
      _get_clustered_apps_id=`echo "$1" | cut -d":" -f2`;
      _get_clustered_apps=`grep "$_get_clustered_apps_id" "$T_VEDANTU_CLUSTEREDAPPS_FILE" | cut -d"=" -f2`;
    fi;
  fi;
  echo "$_get_clustered_apps";
}


function __id_list__ {
  _id="$1";
  if [ -f "$T_VEDANTU_CLUSTEREDAPPS_FILE" ]
  then
    _t_found_id="false";
    while read line
    do
      _t_clustered_apps_id=`echo "$line" | cut -d"=" -f1`;
      if [ -z "$_id" ] || [ "$_id" == "$_t_clustered_apps_id" ]
      then
        _t_clustered_apps=`echo "$line" | cut -d"=" -f2 | tr -d " " | sed "s#,#, #g"`;
        printf "  %-25s%-20s\n" "$_t_clustered_apps_id" "$_t_clustered_apps";
        _t_found_id="true";
      fi;
    done < "$T_VEDANTU_CLUSTEREDAPPS_FILE";
    if [ "$_t_found_id" == "false" ]
    then
      echo "id ["$_id"] not found";
    fi;
  else
    echo "no id file found : $T_VEDANTU_CLUSTEREDAPPS_FILE";
  fi;
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

#  _generate_start_app_name=`basename "$_generate_start_app_dir"`;
#  _generate_start_app_version=`grep -e "val[[:space:]]*appVersion" "$_generate_start_app_dir/project/Build.scala" | awk '{print $NF}' | tr -d \"`;
#  _generate_start_app_jar_name=$_generate_start_app_name"_"$_generate_start_play_version"-"$_generate_start_app_version".jar";
#
#  _generate_start_commons_dir="$T_SCRIPT_DIR/commons";
#  _generate_start_commons_name=`basename "$_generate_start_commons_dir"`;
#  _generate_start_commons_version=`grep -e "val[[:space:]]*appVersion" "$_generate_start_commons_dir/project/Build.scala" | awk '{print $NF}' | tr -d \"`;
#  _generate_start_commons_jar_name=$_generate_start_commons_name"_"$_generate_start_play_version"-"$_generate_start_commons_version".jar";
#
#  cat $T_SCRIPT_DIR"/vStart" | sed "s#{COMMONS_JAR}#"$_generate_start_commons_jar_name"#g" | sed "s#{APP_JAR}#"$_generate_start_app_jar_name"#g" > $_generate_start_target_dir"/vStart";
#
#  chmod +x $_generate_start_target_dir"/vStart";

}


function __start__ {
	_arg="$1";
	if [ "${_arg: -1}" = "/" ]; then
		_arg=${_arg::-1};
	fi
  _start_app=`__app_path__ "$_arg"`;
  if [ -z "$_start_app" ]
  then
    echo "invalid app : $_arg";
    return;
  fi;
  echo "app: $_start_app";
  isAllowed=`__is_allowed__ "$_arg"`;
  if [ "$isAllowed" != "true" ]
  then
    echo "not allowed: $_arg";
    return;
  fi;
  fwkId=`__get_fwk_id__ "$_arg" "$2"`;
  echo "fwkId : $fwkId";
  _play_path=`__get_play_path__ "$_arg"`;
  _start_app_name=`basename $_start_app`;
  _commons_log_conf=$T_SCRIPT_DIR"/commons/conf/logger.xml";
  _target_log_conf=$_start_app"/conf/logger.xml";
  #ln -sfn $_commons_log_conf $_target_log_conf;
  _pre_start_script=$_start_app"/scripts/pre_start.sh";
  if [ -f "$_pre_start_script" ]
  then
    echo "pre-start script found ["$_pre_start_script"]";
    _pre_start_script_response=`$_pre_start_script`;
    _pre_start_script_exit_code="$?";
    echo "--------------------------------------";
    echo "$_pre_start_script_response";
    echo "pre-start exitCode: $_pre_start_script_exit_code";
    echo "--------------------------------------";

    if [ "$_pre_start_script_exit_code" -ne "0" ]
    then
      echo "pre-start script failed [exitCode=$_pre_start_script_exit_code]";
      return;
    fi;
  fi;
  if [ -n "$fwkId" ]
  then
    _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
    if [ -n "$_play_version" ]
    then
      _start_response=`$_play_path start $_start_app --%$fwkId -DVEDANTU_APP_NAME=$_start_app_name`;
      _start_exit_code="$?"
    else
      _start_port=`__get_app_port__ "$_play_path" "$_arg" "$fwkId"`;
      _start_mode=`__get_app_start_mode__ "$_play_path" "$_arg" "$fwkId"`;
      _start_memory=`__get_app_memory__ "$_play_path" "$_arg" "$fwkId"`;
      cd $_start_app;
      ln -sfn $T_SCRIPT_DIR/logs;
      _start_command="start";
      if [ "dev" == "$_start_mode" ]
      then
        _start_command="run";
      fi;
      if ([ "content-services" == "$_start_app_name" ] || [ "cmds-services" == "$_start_app_name" ] || [ "viewer-services" == "$_start_app_name" ]) && [ "prod" == "$fwkId" ]
      then
        _start_memory="$_start_memory -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:PermSize=128M -XX:MaxPermSize=256M";
      fi;
      echo "configuration: $_start_command $_start_memory -Dhttp.port=$_start_port -Dconfig.resource=$fwkId.conf -DVEDANTU_APP_NAME=$_start_app_name -DLOG_FILE_NAME=$_start_app_name";
      #echo "Please press CTRL+D after few seconds";
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
    __send_mail__ "[`hostname`] app start - $_arg" "tech@vedantu.com" "apps@vedantu.com" "$_start_msg_body";
  fi;
}


function __stop__ {
	_arg="$1";
	if [ "${_arg: -1}" = "/" ]; then
		_arg=${_arg::-1};
	fi
  _stop_app=`__app_path__ "$_arg"`;
  if [ -z "$_stop_app" ]
  then
    echo "invalid app : $_arg";
    return;
  fi;
  _play_path=`__get_play_path__ "$_arg"`;
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
    __send_mail__ "[`hostname`] app stop - $_arg" "tech@vedantu.com" "apps@vedantu.com" "$_stop_msg_body";
  fi;
}


function __clean__ {
  _clean_app=`__app_path__ "$1"`;
  if [ -z "$_clean_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  echo "app: $_clean_app";
  _play_path=`__get_play_path__ "$1"`;
  _clean_response=`$_play_path clean $_clean_app`;
  _clean_exit_code="$?";
  echo "--------------------------------------";
  echo "$_clean_response";
  echo "exit code: "$_clean_exit_code;
  echo "--------------------------------------";
  if [ "$_clean_exit_code" -ne "0" ]
  then
    echo "could not clean application!";
  else
    _clean_msg_body="
--------------------------------------
$_clean_response
exit code: $_clean_exit_code
--------------------------------------
";
    __send_mail__ "[`hostname`] app clean - $1" "tech@vedantu.com" "apps@vedantu.com" "$_clean_msg_body";
  fi;
}


function __deps__ {
  _deps_app=`__app_path__ "$1"`;
  if [ -z "$_deps_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  echo "app: $_deps_app";
  _play_path=`__get_play_path__ "$1"`;
  _deps_response=`$_play_path deps $_deps_app "$2"`;
  _deps_exit_code="$?";
  echo "--------------------------------------";
  echo "$_deps_response";
  echo "exit code: "$_deps_exit_code;
  echo "--------------------------------------";
  if [ "$_deps_exit_code" -ne "0" ]
  then
    if [ -n "$2" ]
    then
      echo "could not deps $2 application!";
    else
      echo "could not deps application!";
    fi;
  else
    _deps_msg_body="
--------------------------------------
$_deps_response
exit code: $_deps_exit_code
--------------------------------------
";
    __send_mail__ "[`hostname`] deps clean - $1" "tech@vedantu.com" "apps@vedantu.com" "$_deps_msg_body";
  fi;
}


function __get_app_port__ {
  _play_path="$1";
  _app_path="$2";
  _app_fwkId="$3";
  _app_port="";
  if [ -n "$_app_fwkId" ]
  then
    _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
    if [ -n "$_play_version" ]
    then
      _app_port=`grep "\([\s]*[#]*[\s]*\)%"$_app_fwkId"\.http\.port=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
    else
      if [ -f "$_app_path/conf/$_app_fwkId.conf" ]
      then
        _app_port=`cat $_app_path/conf/$_app_fwkId.conf | grep "http\.port" | awk -F= '{print $2}'`;
      fi;
    fi;
  fi;
  if [ -z "$_app_port" ]
  then
    _app_port=`grep "\([\s]*[#]*[\s]*\)http\.port=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
  fi;
  echo "$_app_port";
}


function __get_app_memory__ {
  _play_path="$1";
  _app_path="$2";
  _app_fwkId="$3";
  _app_memory="";
  if [ -n "$_app_fwkId" ]
  then
    _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
    if [ -n "$_play_version" ]
    then
      _app_memory=`grep "\([\s]*[#]*[\s]*\)%"$_app_fwkId"\.jvm\.memory=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
    else
      if [ -f "$_app_path/conf/$_app_fwkId.conf" ]
      then
        _app_memory=`cat $_app_path/conf/$_app_fwkId.conf | grep "jvm\.memory" | awk -F= '{print $2}'`;
      fi;
    fi;
  fi;
  if [ -z "$_app_memory" ]
  then
    _app_memory=`grep "\([\s]*[#]*[\s]*\)jvm\.memory=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
  fi;
  echo $_app_memory | tr -d \";
}


function __get_app_start_mode__ {
  _play_path="$1";
  _app_path="$2";
  _app_fwkId="$3";
  _app_mode="";
  if [ -n "$_app_fwkId" ]
  then
    _play_version=`$_play_path help version | grep "play\!" | grep " 1.2"`;
    if [ -n "$_play_version" ]
    then
      _app_mode=`grep "\([\s]*[#]*[\s]*\)%"$_app_fwkId"\.mode=" $_app_path/conf/application.conf | cut -d"=" -f2 | tr -d [[:space:]]`;
    else
      if [ -f "$_app_path/conf/$_app_fwkId.conf" ]
      then
        _app_mode=`cat $_app_path/conf/$_app_fwkId.conf | grep "mode=" | awk -F= '{print $2}'`;
      fi;
    fi;
  fi;
  if [ -z "$_app_mode" ]
  then
    _app_mode="prod";
  fi;
  echo "$_app_mode";
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


function __show_app__ {
  _show_app=`__app_path__ "$1"`;
  if [ -z "$_show_app" ]
  then
    echo "invalid app : $1";
    return;
  fi;
  _show_heading="$2";
  if [ -z "$_show_heading" ]
  then
    _show_heading="false";
  fi;

  _status_ps="running";
  _status_pid="";
#  _show_app_conf="$_show_app/conf";
#  _show_app_using_ps=`ps aux | grep "$_show_app_conf" | grep -v grep`;
  _show_app_using_ps=`__get_app_ps_id__ "$_show_app"`;
  if [ -z "$_show_app_using_ps" ]
  then
    _status_ps="stopped";
  else
#    _status_pid=`echo "$_show_app_using_ps" | awk '{print $2}'`;
    _status_pid="$_show_app_using_ps";
    _status_ps="running["$_status_pid"]";
  fi;

  _status_server_pid="-----";
  _show_app_server_pid_file="$_show_app/server.pid";
  if [ ! -e "$_show_app_server_pid_file" ]
  then
    _show_app_server_pid_file="$_show_app/RUNNING_PID";
  fi;
  if [ -f "$_show_app_server_pid_file" ]
  then
    _status_server_pid=`cat "$_show_app_server_pid_file"`;
    if [ -z "$_status_server_pid" ]
    then
      _status_server_pid="nopid";
    fi;
  fi;
  _status_is_allowed="";
  t_is_allowed=`__is_allowed__ "$1"`;
  if [ "$t_is_allowed" == "true" ]
  then
    _status_is_allowed="allowed"
  fi;
  _status_fwkId=`__get_fwk_id__ "$1" `;
  _play_path=`__get_play_path__ "$1"`;
  _status_port=`__get_app_port__ "$_play_path" "$1" "$_status_fwkId"`;
  if [ "$_show_heading" == "true" ]
  then
    printf "  %-40s%-20s%-20s%-20s%-15s-%10s\n" "app" "ps.pid" "server.pid" "is-allowed" "fwkId" "port";
    printf "  %-40s%-20s%-20s%-20s%-15s-%10s\n" "---" "------" "----------" "----------" "-----" "----";
  fi;
  printf "  %-40s%-20s%-20s%-20s%-15s-%10s\n" "$1" "ps:$_status_ps" "server.pid:$_status_server_pid" "$_status_is_allowed" "$_status_fwkId" "$_status_port";
}


function __show__ {
  if [ -n "$1" ]
  then
    apps=`__get_clustered_apps__ "$1" | tr , " "`;
  else
#    apps=`ls $PRJ_PATH`;
    apps=`find $PRJ_PATH -name "application.conf" | awk -F/conf/ '{print $1}' | xargs -I X sh -c 'y=X;echo ${y##*'$PRJ_PATH'/}'`;
  fi;
  _heading="true";
  for app in $apps
  do
    _show_app=`__app_path__ "$app"`;
    if [ -n "$_show_app" ]
    then
      __show_app__ "$app" "$_heading";
      _heading="false";
    fi;
  done;
}


function __do_action_multi__ {
  _t_cmd="__"$1"__";
  if [[ "$2" == *","* ]]
  then
    for app in `echo $2 | tr "," " "`
    do
      _t_app=`__app_path__ "$app"`;
      if [ -n "$_t_app" ]
      then
        #_multi_res=`"$_t_cmd" "$app" "$3"`;
        #echo "$_multi_res";
        "$_t_cmd" "$app" "$3"
      fi;
    done;
  else
    #_multi_res=`$_t_cmd "$2" "$3"`;
    #echo "$_multi_res";
    "$_t_cmd" "$2" "$3"
  fi;
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
  P_APP=`__get_clustered_apps__ "$P_APP"`;

  case "$P_ACTION" in
    status) __do_action_multi__ "status" "$P_APP";
      ;;
    start)  __do_action_multi__ "start"  "$P_APP" "$3";
      ;;
    stop)   __do_action_multi__ "stop"   "$P_APP";
      ;;
    clean)  __do_action_multi__ "clean"  "$P_APP";
      ;;
    deps)   __do_action_multi__ "deps"   "$P_APP" "$3";
      ;;
    *)      echo "unsupported action ["$P_ACTION"]";
            echo;
      ;;
  esac;
}


function __do_git__ {
  _git_full_cmd=`echo $*`;
  _git_sub_cmd="$2";
  $_git_full_cmd;
  _git_exit_code="$?";
  _git_branch=`git branch | grep "*"`;
  if [ "$_git_sub_cmd" == "pull" ]
  then
    _git_changes=`git log @{1}.. --graph --no-merges --pretty=format:'%h %an -%d %s (%cr - %ai)' --abbrev-commit --date=relative | tee`;
    _git_msg_body="
--------------------------------------
branch: $_git_branch
full command: $_git_full_cmd
--------------------------------------
$_git_changes
--------------------------------------
exit code: $_git_exit_code
--------------------------------------
";
    echo "$_git_msg_body";
    __send_mail__ "[`hostname`] git pull" "tech@vedantu.com" "apps@vedantu.com" "$_git_msg_body";
  fi;
}


function __switch__ {
  PRJ_PATH="$1";
  if [ -z "$PRJ_PATH" ]
  then
    PRJ_PATH="`pwd`";
  fi;
}

#----------------------------------------------------------------


T_PLAY_PATH=`which play`;
if [ -z "$T_PLAY_PATH" ]
then
  echo "play not found... first install play!"
  exit 1;
fi;

#set defaults
__switch__ "$1";


P_CMD="x";
while [ "$P_CMD" != "exit" ]
do
  read -e -p "$PRJ_PATH > " -a C
  P_CMD=${C[0]};
  case "$P_CMD" in
    help)   __help__;
      ;;
    switch) __switch__  "${C[1]}";
      ;;
    show)   __show__  "${C[1]}";
      ;;
    idlist) __id_list__  "${C[1]}";
      ;;
    status) __do_action__ "status" "${C[1]}";
      ;;
    start)  __do_action__ "start" "${C[1]}" "${C[2]}";
      ;;
    stop)   __do_action__ "stop" "${C[1]}";
      ;;
    clean)  __do_action__ "clean" "${C[1]}";
      ;;
    deps)   __do_action__ "deps" "${C[1]}" "${C[2]}";
      ;;
    git)    __do_git__    ${C[@]};
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
