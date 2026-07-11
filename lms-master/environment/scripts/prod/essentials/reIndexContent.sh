#curl -d "callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp" http://localhost:19014/indexer/reIndexLibraryLinks;

#curl -d "callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp" http://localhost:19014/indexer/reIndexUserActionMappings;

if [ -n "$1" ]; then
params="type=$1&callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp";
echo $params 
 curl -d  "$params"  http://localhost:19014/indexer/reIndex;
else
eTypes=(CMDSDOCUMENT CMDSVIDEO CMDSTEST CMDSASSIGNMENT CMDSQUESTION CMDSQUESTIONSET CMDSMODULE DOCUMENT MODULE VIDEO QUESTION TEST ASSIGNMENT CHALLENGE DISCUSSION FOLDER);
for i in "${eTypes[@]}"
do
 params="type=$i&callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp";
echo $params 
 curl -d  "$params"  http://localhost:19014/indexer/reIndex;
done;
fi;
