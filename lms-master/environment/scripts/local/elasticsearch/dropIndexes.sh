

COMMENT following two lines to drop indexes as this script will drop indices
SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS


host=$1;
port=$2;
if [ -z $host ]
then
echo "please provide listining host for elasticsearch service";
 exit;
fi;

if [ -z $port ]
then
echo "no prot is provided, using default port:9200";
port="9200";
fi;

## question related mapping

for i in   questions tests  videos assignments files modules documents challenges discussions contents users  boards organizations cmdsresources 
do
echo -e "Removing index " $i 
curl -XDELETE $host:$port/$i

echo -e "Removed index " $i 
done

for i in   cmdsquestions cmdstests  cmdsvideos cmdsassignments cmdsfiles cmdsmodules cmdsdocuments
do
echo -e "Removing index " $i 
curl -XDELETE $host:$port/$i

echo -e "Removed index " $i 
done

