filename="$1"
TMP_FILE="/tmp/s3output.txt"
while read -r line
do
    uuid="$line"
    count=`s3cmd ls s3://video-prod-learnpedialive/$uuid | wc -l`
    if [ "$count" -lt 3 ]
    then
        echo
        s3cmd ls s3://video-prod-learnpedialive/$uuid
    else
    	echo "$line" > $TMP_FILE
    fi;
done < "$filename"