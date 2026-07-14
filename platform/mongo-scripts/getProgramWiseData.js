var programIdsList = ["584e47d5c92e5dca741bae39"];


var orgId = "584e44e8c92e5dca741bae2a";

function displayUserPrograms(programs){
    var programString = "";
    var startDate;
    var endDate;
    for(i=0;i<programs.length;i++){
        startDate = new Date(programs[i].startDate).toString();
        endDate = programs[i].endDate == 0 ? "Unlimited" : new Date(programs[i].endDate).toString();
        programString += "\""+programs[i].progName +"\""+ ","+ "\""+programs[i].centerName +"\""+ "," + "\""+programs[i].sectionName +"\""+","+startDate+","+endDate +","+",";
    }
    if(programString === ""){
        programString = "No Programs From the List";
    }
    return programString;
}
print("Name"+","+"MemberId"+","+"UserId"+","+"Program"+","+"Center"+","+"Section"+","+"Time Joined"+","+"End Time");
db.orgmembers.find({orgId:orgId,profile:"STUDENT"}).forEach(function(doc){
    var programs = [];
    if(typeof doc.mappings !== 'undefined'  && doc.mappings[0] !== 'undefined' && doc.mappings[0] !== 'null'){
        if(doc.mappings.length > 0){
            for(var i=0; i < doc.mappings.length; i++){
                var eachProgram = {};
                var programIndex = programIdsList.indexOf(doc.mappings[i].programId);
                if(programIndex !== -1){
                    eachProgram.startDate = doc.mappings[i].timeJoined;
                    eachProgram.endDate = doc.mappings[i].endTime;
                    db.orgprograms.find({"_id":new ObjectId(doc.mappings[i].programId)}).forEach(function(program){
                        eachProgram.progName = program.name;
                    });

                    db.orgcenters.find({"_id":new ObjectId(doc.mappings[i].centerId)}).forEach(function(center){
                        eachProgram.centerName = center.name;
                    });

                    db.orgsections.find({"_id":new ObjectId(doc.mappings[i].sectionId)}).forEach(function(section){
                        eachProgram.sectionName = section.name;
                    });
                    programs.push(eachProgram);
                }
            }
        }
    }

    if(typeof doc.expiredMappings !== 'undefined'  && doc.expiredMappings[0] !== 'undefined' && doc.expiredMappings[0] !== 'null'){
        if(doc.expiredMappings.length > 0){
            for(var j=0; j < doc.expiredMappings.length; j++){
                var expiredProgram = {};
                var expiredProgramIndex = programIdsList.indexOf(doc.expiredMappings[j].programId);
                if(expiredProgramIndex !== -1){
                    expiredProgram.startDate = doc.expiredMappings[j].timeJoined;
                    expiredProgram.endDate = doc.expiredMappings[j].endTime;
                    db.orgprograms.find({"_id":new ObjectId(doc.expiredMappings[j].programId)}).forEach(function(program){
                        expiredProgram.progName = program.name;
                    });

                    db.orgcenters.find({"_id":new ObjectId(doc.expiredMappings[j].centerId)}).forEach(function(center){
                        expiredProgram.centerName = center.name;
                    });

                    db.orgsections.find({"_id":new ObjectId(doc.expiredMappings[j].sectionId)}).forEach(function(section){
                        expiredProgram.sectionName = section.name;
                    });
                    programs.push(expiredProgram);
                }
            }
        }
    }
    // var LastLoginDate = 0;
    // var tests = db.userentityattempts.find({userId:doc.userId,"entity.type":"TEST"}).count();
    // var videos = db.activityrecords.find({userId:doc.userId,page:"VIDEO",action:"OPEN",entity:{$exists:true}}).count();
    // var docs = db.activityrecords.find({userId:doc.userId,page:"DOCUMENT",action:"OPEN",entity:{$exists:true}}).count() ;
    // var doubts = db.activityrecords.find({userId:doc.userId,page:"DOUBTS",action:"ADD",entity:{$exists:true}}).count();

    // db.activityrecords.find({userId:doc.userId}).sort({timeCreated:-1}).limit(1).forEach(function(ar){
    //     LastLoginDate = new Date(ar.timeCreated);
    // });

    // LastLoginDate  = (LastLoginDate === 0) ? "Not Logged In" : LastLoginDate;
    var programsResp = displayUserPrograms(programs);
    if(programsResp !== "No Programs From the List"){
        print(doc.firstName+","+doc.memberId+","+doc.userId+","+programsResp);
    }
});