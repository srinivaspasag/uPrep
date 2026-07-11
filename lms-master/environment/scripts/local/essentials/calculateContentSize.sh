if [ -n "$1" ]; then
params="type=$1&callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp";
echo $params 
 curl -d  "$params"  http://localhost:19014/sizer/calculate;
else
eTypes=(CMDSDOCUMENT CMDSVIDEO CMDSTEST CMDSASSIGNMENT CMDSQUESTION CMDSQUESTIONSET CMDSMODULE CMDSFILE);
for i in "${eTypes[@]}"
do
 params="type=$i&callingUserId=51a6d3fbe4b0f9f31d6d6a31&userId=51a6d3fbe4b0f9f31d6d6a31&callingAppId=WebApp&callingApp=WebApp";
echo $params 
 curl -d  "$params"  http://localhost:19014/sizer/calculate;
done;
fi;
