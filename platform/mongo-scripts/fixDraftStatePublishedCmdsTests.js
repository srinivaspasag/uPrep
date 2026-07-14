// var cmdsTestId = new ObjectId("5ad70b5ee00614d42da8e3a6");
var count = 0;
db.cmdstests.find({"published" : true,"completed":false}).forEach(function(doc){
    count++;
    function getMetadata() {
            for(i=0;i<doc.metadata.length;i++){
                var uniqueCMDSQIds = [];
                var cmdsQIds = [];
                var cmdsSCQIds = [];
                var cmdsMCQIds = [];
                var cmdsNumericIds = [];
                var cmdsParaIds = [];
                if(doc.metadata[i].qIds != null){
                    print("No QIds");
                    if(doc.metadata[i].qusCount == doc.metadata[i].qIds.length){
                        continue;
                    }
                }
                print("Implementing logic");
                for(j=0;j<doc.metadata[i].children.length;j++){
                    for(k=0;k<doc.metadata[i].children[j].qIds.length;k++){
                        cmdsQIds.push(doc.metadata[i].children[j].qIds[k]);
                    }
                }
                // Remove duplicates from cmdsQIds list
                var unique_array = [];
                for(j = 0;j < cmdsQIds.length; j++){
                    if(!inArray(cmdsQIds[j],unique_array)){
                        unique_array.push(cmdsQIds[j]);
                        uniqueCMDSQIds.push(new ObjectId(unique_array[j]));
                    }
                }
                print("Object with out duplicates "+uniqueCMDSQIds.toString());
                db.cmdsquestions.find({_id:{$in:uniqueCMDSQIds},type:"SCQ"}).forEach(function(cmdsdoc){
                    cmdsSCQIds.push(cmdsdoc._id.valueOf());
                })

                db.cmdsquestions.find({_id:{$in:uniqueCMDSQIds},type:"MCQ"}).forEach(function(cmdsdoc){
                    cmdsMCQIds.push(cmdsdoc._id.valueOf());
                })

                db.cmdsquestions.find({_id:{$in:uniqueCMDSQIds},type:"NUMERIC"}).forEach(function(cmdsdoc){
                    cmdsNumericIds.push(cmdsdoc._id.valueOf());
                })

                db.cmdsquestions.find({_id:{$in:uniqueCMDSQIds},type:"PARA"}).forEach(function(cmdsdoc){
                    cmdsParaIds.push(cmdsdoc._id.valueOf());
                })
                print("SCQ Array "+cmdsSCQIds.toString());
                print("MCQ Array "+cmdsMCQIds.toString());
                print("NUMERIC Array "+cmdsNumericIds.toString());
                print("PARA Array "+cmdsParaIds.toString());

                for(j=0;j<doc.metadata[i].details.length;j++){
                    print("Head SCQ");
                    if(doc.metadata[i].details[j].qusCount > 0){
                        print("Above SCQ");
                        if(doc.metadata[i].details[j].type == "SCQ"){
                            print("SCQ");
                            doc.metadata[i].details[j].qIds = cmdsSCQIds;
                        }
                        else if(doc.metadata[i].details[j].type == "MCQ"){
                            print("SCQ");
                            doc.metadata[i].details[j].qIds = cmdsMCQIds
                        }

                        else if(doc.metadata[i].details[j].type == "NUMERIC"){
                            print("SCQ");
                            doc.metadata[i].details[j].qIds = cmdsNumericIds
                        }

                        else if(doc.metadata[i].details[j].type =="PARA"){
                            print("SCQ");
                            doc.metadata[i].details[j].qIds = cmdsParaIds;
                        }
                    }
                }
                doc.metadata[i].qIds = unique_array;
            }
            print(doc.metadata.toString());
            return doc.metadata;
        }

    function inArray(needle,haystack)
    {
        var count=haystack.length;
        for(var i=0;i<count;i++)
        {
            if(haystack[i]===needle){
                return true;
            }
        }
        return false;
    }
    db.cmdstests.update({"_id" : doc._id}, {$unset: {"metadata" : ""}});
    db.cmdstests.update({"_id" : doc._id}, {$push: {"metadata" : {$each : getMetadata() } } });
    db.cmdstests.update({"_id" : doc._id}, {$set: {"completed" : true}});
    print(count);
});
