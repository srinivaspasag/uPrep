db.organizations.find({}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.organizations.update({"_id" : doc._id}, {$set: {"doubtsForumMode": "PUBLIC" }});
})