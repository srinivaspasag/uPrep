#! /bin/bash
T_SCRIPT_DIR=`dirname $0`;
T_LOCAL_REPO=$T_SCRIPT_DIR"/../../local-repo";
T_DEBUG_MODE=false;

if [ "clean" == "$3" ]
then
	`rm -R "$1/"o.*."$2"`;
	`rm -R "$1/*/"o.*."$2"`;
   	echo "BUILD FILES CLEANED";
	exit 0;
fi

if [ "debug" == "$3" ]
then
   T_DEBUG_MODE=true;
   echo "Build is running in DEBUG MODE for "$2;
else
   echo "Build is running in DEPLOYMENT MODE for "$2;
fi

if [ ! -d "$T_LOCAL_REPO" ]
then
  echo "local-repo path incorrect : "$T_LOCAL_REPO;
  exit 1;
fi;

yuiCompressor=$T_LOCAL_REPO"/yuicompressor-2.4.7.jar"

if [ ! -f "$yuiCompressor" ]
then
  echo "could not find : $T_LOCAL_REPO/yuicompressor-2.4.7.jar";
  exit 1;
fi;

filePrefix=$1'/';
input='';
output='';
filer=$1'/filer.txt';

if [ ! -f "$filer" ]
then
  echo "could not find filer : $filer";
  exit 1;
fi;

if [ $T_DEBUG_MODE == true ] 
then
	rm -R "$1/"o.*."$2";
	rm -R "$1/*/"o.*."$2";
fi

for line in $(cat $filer)
do
	if [ "$line" == "execute" ]; then
		if [ $T_DEBUG_MODE == false ] 
		then
			tmpFile=`mktemp --suffix="$2"`;
			cat $input > $tmpFile;
			java -jar "$yuiCompressor" --type $2 $tmpFile -o $output --charset utf-8;
			exitStatus="$?";
			if [ "$exitStatus" -ne "0" ]
			then
				echo "could not complete successfully [exitStatus=$exitStatus]";
				exit $exitStatus;
			fi;
			rm $tmpFile;
		else
			echo "File Creating = "$output" ; STATUS = "$line;
			cat $input > $output;
		fi;
		echo "FILE Created : "$output;
		output='';
		input='';
	elif [ "$line" == "combine" ]; then
		echo "File Creating = "$output" ; From = "$input" STATUS = "$line;
		cat $input > $output;
		echo "FILE Created : "$output;
		output='';
		input='';
	else
		param="$(echo $line | cut -d: -f1)";
		if [ "$param" == "Output" ]; then
			outputFileName="$(echo $line | cut -d: -f2)";
			if [[ "`basename $outputFileName`" !=  "o."* ]]
			then
				echo "incorrect output filename [$outputFileName]; should start with \"o.\"";
				exit 1;
			fi;
			output=$filePrefix$outputFileName;
		else
			inputFileName=$filePrefix"$(echo $line | cut -d: -f2)";
			if [ ! -f "$inputFileName" ]
			then
				echo "File not found! filer.txt = [$inputFileName];";
				exit 1;
			fi;
			input=$input$inputFileName" ";
		fi
	fi
done
echo "Build DONE";
