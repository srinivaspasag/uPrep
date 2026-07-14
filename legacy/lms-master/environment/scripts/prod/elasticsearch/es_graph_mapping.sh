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

curl -XPUT $host:$port/questions/following/_mapping -d '{
  "following":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/added/_mapping -d '{
  "added":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "question"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/questions/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/questions/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "question"}, "_routing": {"required": true}
  }
}'


## video related mapping

curl -XPUT $host:$port/videos/following/_mapping -d '{
  "following":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/added/_mapping -d '{
  "added":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "video"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/videos/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/videos/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "video"}, "_routing": {"required": true}
  }
}'



## challenge related mapping

curl -XPUT $host:$port/challenges/following/_mapping -d '{
  "following":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/added/_mapping -d '{
  "added":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "challenge"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/challenges/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/challenges/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "challenge"}, "_routing": {"required": true}
  }
}'


## test related mapping

curl -XPUT $host:$port/tests/following/_mapping -d '{
  "following":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/added/_mapping -d '{
  "added":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "test"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/tests/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/tests/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "test"}, "_routing": {"required": true}
  }
}'


## assignment related mapping
curl -XPUT $host:$port/assignments/following/_mapping -d '{
  "following":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/added/_mapping -d '{
  "added":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "assignment"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/assignments/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/assignments/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "assignment"}, "_routing": {"required": true}
  }
}'



## discussion related mapping
curl -XPUT $host:$port/discussions/following/_mapping -d '{
  "following":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/discussions/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/discussions/added/_mapping -d '{
  "added":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/discussions/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/discussions/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/discussions/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/discussions/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "discussion"}, "_routing": {"required": true}
  }
}'



## document related mapping
curl -XPUT $host:$port/documents/following/_mapping -d '{
  "following":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/added/_mapping -d '{
  "added":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/documents/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/documents/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'



## common content related mapping
curl -XPUT $host:$port/contents/following/_mapping -d '{
  "following":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/added/_mapping -d '{
  "added":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "content"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/contents/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/contents/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "content"}, "_routing": {"required": true}
  }
}'



## user related mapping
curl -XPUT $host:$port/users/following/_mapping -d '{
  "following":{
    "_parent": {"type": "user"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/users/organization/_mapping -d '{
  "organization":{   
	"_parent": {"type": "user"},   "_routing": {"required": true}
    }
}'




## board related mapping
curl -XPUT $host:$port/boards/following/_mapping -d '{
  "following":{
    "_parent": {"type": "board"},"_routing": {"required": true}
  }
}'

## cmds resource related mapping
curl -XPUT $host:$port/cmdsresources/added/_mapping -d '{
  "added":{
    "_parent": {"type": "resource"},"_routing": {"required": true}
  }
}'

## document related mapping

curl -XPUT $host:$port/documents/following/_mapping -d '{
  "following":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/added/_mapping -d '{
  "added":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "document"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/documents/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/documents/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "document"}, "_routing": {"required": true}
  }
}'

## file related mapping

curl -XPUT $host:$port/files/following/_mapping -d '{
  "following":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/added/_mapping -d '{
  "added":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/attempted/_mapping -d '{
  "attempted":{
    "_parent": {"type": "file"},"_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/files/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/files/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "file"}, "_routing": {"required": true}
  }
}'


## module related mapping

curl -XPUT $host:$port/modules/following/_mapping -d '{
  "following":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/modules/voted/_mapping -d '{
  "voted":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/modules/added/_mapping -d '{
  "added":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/modules/rated/_mapping -d '{
  "rated":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'
  
curl -XPUT $host:$port/modules/commented/_mapping -d '{
  "commented":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'

curl -XPUT $host:$port/modules/viewed/_mapping -d '{
  "viewed":{
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'


curl -XPUT $host:$port/modules/shared/_mapping -d '{
  "shared": {
    "_parent": {"type": "module"}, "_routing": {"required": true}
  }
}'


