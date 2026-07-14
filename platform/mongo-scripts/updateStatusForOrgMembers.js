var count= 0;

var orgId = "5ba9d12ae4b0ff57ab844dab";

db.orgmembers.find({orgId:orgId,profile:"STUDENT",status:{$exists:true}}).forEach(function(doc){
    doc.status = [];
    db.orgmembers.update({"_id" :doc._id}, {$set: {"status": doc.status} });
    count++;
});
print(count);