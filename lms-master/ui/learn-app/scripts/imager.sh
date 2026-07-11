#! /bin/bash
T_SCRIPT_DIR=`dirname $0`;
T_OUTPUT_FOLDER=$T_SCRIPT_DIR"/../app/views/dataImages";

if [ ! -d "$T_OUTPUT_FOLDER" ]
then
  echo "local-repo path incorrect : "$T_OUTPUT_FOLDER;
  exit 1;
fi;

filePrefix=$1'/';
input='';
output='';
imager=$1'/imager.txt';

if [ ! -f "$imager" ]
then
  echo "could not find filer : $imager";
  exit 1;
fi;

if [ $T_DEBUG_MODE == true ] 
then
	rm -R "$1/"o.*."$2";
	rm -R "$1/*/"o.*."$2";
fi

for line in $(cat $imager)
do
done

echo " Images Build DONE ";
