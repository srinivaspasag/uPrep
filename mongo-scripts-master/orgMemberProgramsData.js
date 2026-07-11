var orgId = "584e44e8c92e5dca741bae2a";

db.orgmembers.find({orgId:orgId,userId:"589855bcc92ec2a0710afafb"}).forEach(function(member){
    var status = false;
    if (member.interval.till!=-1) {
        status = true;
    }
    for(var i=0; i < member.mappings.length; i++){

          db.orgprograms.find({_id:new ObjectId(member.mappings[i].programId)}).forEach(function(program){
            programName =program.name;
          });

          db.orgcenters.find({_id:new ObjectId(member.mappings[i].centerId)}).forEach(function(center){
            centerName =center.name;
          });

          db.orgsections.find({_id:new ObjectId(member.mappings[i].sectionId)}).forEach(function(section){
            sectionName = section.name;
          });
          print("\""+member.firstName + " " + member.lastName +"\"" + ","+ member.memberId+","+new Date(member.timeCreated)+","+status+","+programName + ","+ centerName+","+sectionName+","+new Date(member.mappings[i].timeJoined)+","+(member.mappings[i].endTime == 0 ? "Unlimited" : new Date(member.mappings[i].endTime)));
    }
});