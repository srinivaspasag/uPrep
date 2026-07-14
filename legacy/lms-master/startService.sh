
T_CONFIG="/usr/local/play-2.1.0;local;19013;/home/shankar/projects/git/vedantu/zowie/commons/conf/logger.xml"
T_PROJECT="content/content-services"

#./letsplay $T_PROJECT prepare "$T_CONFIG"
./letsplay $T_PROJECT stop 
./letsplay $T_PROJECT start 

## on successfull start notify master server

status=`./letsplay $T_PROJECT status | grep "not running"`

if [ -d "$status" ] 
 then
   exit 1;
else 
   exit 0;
fi

