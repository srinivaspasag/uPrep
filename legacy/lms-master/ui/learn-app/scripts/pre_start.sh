T_SCRIPT_DIR="`dirname $0`";

$T_SCRIPT_DIR/filer.sh $T_SCRIPT_DIR/../public/javascripts js $1
$T_SCRIPT_DIR/filer.sh $T_SCRIPT_DIR/../public/stylesheets css $1

#if [ "debug" == "$1" ]
#then
#	T_FROM="$T_SCRIPT_DIR"/../app/views/Institute/home_work.html
#	T_TO="$T_SCRIPT_DIR"/../app/views/Institute/home.html
#	cp "$T_FROM" "$T_TO"	
#else
#	T_FROM="$T_SCRIPT_DIR"/../app/views/Institute/static_home.html
#	T_TO="$T_SCRIPT_DIR"/../app/views/Institute/home.html
#	cp "$T_FROM" "$T_TO"	
#fi
