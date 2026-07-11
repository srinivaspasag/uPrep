function getOrgId(userId) {
	print("userId :"+userId);
    var orgId = null;
    db.orgmembers.find({'userId': userId}).forEach(function(orgmember){
        orgId = orgmember.orgId;
    })
    print("orgId :"+ orgId);
    return orgId;
}

db.userentityanalytics.find({"entity.id":"5a7aba94e4b06be5ffe9de27"}).forEach(function(doc){
    print("_id :"+ doc._id);
    if(doc.orgId === null){
        db.userentityanalytics.update({"_id" : doc._id}, {$set: {"orgId": getOrgId(doc.userId)} });
    }
    print(" ");
})
