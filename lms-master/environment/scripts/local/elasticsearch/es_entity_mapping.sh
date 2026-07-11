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

curl -XPUT $host:$port/questions/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/questions/question/_mapping -d '{
  "question":{
  }
}'

# adding tests and mapping
curl -XPUT $host:$port/tests/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/tests/test/_mapping -d '{
  "test":{
  }
}'


# adding assignments and mapping
curl -XPUT $host:$port/assignments/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/assignments/assignment/_mapping -d '{
  "assignment":{
  }
}'


# adding videos and mapping
curl -XPUT $host:$port/videos/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/videos/video/_mapping -d '{
  "video":{
  }
}'

# adding discussions index and mapping
curl -XPUT $host:$port/discussions/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/discussions/discussion/_mapping -d '{
  "discussion":{
  }
}'

# adding challenges index and mapping
curl -XPUT $host:$port/challenges/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/challenges/challenge/_mapping -d '{
  "challenge":{
  }
}'

# adding common content index and mapping
curl -XPUT $host:$port/contents/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/contents/content/_mapping -d '{
  "content":{
  }
}'


# adding document mappings
curl -XPUT $host:$port/documents/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/documents/document/_mapping -d '{
  "document":{
  }
}'

# adding files and mapping
curl -XPUT $host:$port/files/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/files/file/_mapping -d '{
  "file":{
  }
}'

# adding modules and mapping
curl -XPUT $host:$port/modules/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/modules/module/_mapping -d '{
  "module":{
  }
}'


## cmds resource related mapping
curl -XPUT $host:$port/cmdsresources/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        },
        "analysis" : {
            "analyzer":{
                 "untouched_analyzer" : {
              	     "tokenizer" : "keyword",
                     "filter" : ["lowercase"]
             	 }
            }
        }
    }
}'

curl -XPUT $host:$port/cmdstests/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/cmdsassignments/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

## cmds resource related mapping
curl -XPUT $host:$port/cmdsquestions/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/cmdsvideos/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/cmdsdocuments/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/cmdsfiles/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

curl -XPUT $host:$port/cmdsmodules/ -d '{
    "settings" : {
        "index" : {
            "number_of_shards" : 5,
            "number_of_replicas" :1 
        }
    }
}'

## cmds resource related mapping
curl -XPUT $host:$port/cmdstests/cmdstest/_mapping -d '{
  "cmdstest":{
  }
}'

curl -XPUT $host:$port/cmdsassignments/cmdsassignment/_mapping -d '{
  "cmdsassignment":{
  }
}'


curl -XPUT $host:$port/cmdsquestions/cmdsquestion/_mapping -d '{
  "cmdsquestion":{
  }
}'

curl -XPUT $host:$port/cmdsvideos/cmdsvideo/_mapping -d '{
  "cmdsvideo":{
  }
}'

curl -XPUT $host:$port/cmdsdocuments/cmdsdocument/_mapping -d '{
  "cmdsdocument":{

  }
}'

curl -XPUT $host:$port/cmdsfiles/cmdsfile/_mapping -d '{
  "cmdsfile":{

  }
}'

curl -XPUT $host:$port/cmdsmodules/cmdsmodule/_mapping -d '{
  "cmdsmodule":{

  }
}'


curl -XPUT $host:$port/cmdsresources/resource/_mapping -d '{
  "resource":{
	"properties" : {
		"name": { 
			"type": "multi_field",
			"fields" :{"name" : {"type" : "string", "index" : "analyzed"},
                    		"untouched" : {"type" : "string", "index" : "analyzed","analyzer":"untouched_analyzer","include_in_all":"false"}
			}	
		}
	}
  }
}'
