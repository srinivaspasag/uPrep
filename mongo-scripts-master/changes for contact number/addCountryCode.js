db.orgmembers.find({}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.orgmembers.update({"_id" : doc._id}, {$set: {"countryCode": "91"} });
    print(" ");
})