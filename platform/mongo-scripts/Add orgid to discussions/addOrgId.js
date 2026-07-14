function getOrgId(userId) {
	print("userId :"+userId);
    var orgId = null;
    db.orgmembers.find({'userId': userId}).forEach(function(orgmember){
        orgId = orgmember.orgId;
    })
    print("orgId :"+ orgId);
    return orgId;
}

db.discussions.find({}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.discussions.update({"_id" : doc._id}, {$set: {"orgId": getOrgId(doc.userId)} });
    print(" ");
})