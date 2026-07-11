//Enter the userId of user to clear test analytics
var userId = "58cf8128a4442af18e0fe94b";
//Enter the testId of user to clear test analytics
var testId = "5a549693e0066abb2a6f4413";

db.entityuseractionmapping.find({userId:userId,"target.type":"TEST","target.id":testId,actionType:"ATTEMPTED"}).forEach(function(doc){
    print("entityuseractionmapping _id :"+ doc._id);
    db.entityuseractionmapping.remove({"_id" : doc._id});
})
db.userentityanalytics.find({userId:userId,"entity.type":"TEST","entity.id":testId}).forEach(function(doc){
    print("userentityanalytics _id :"+ doc._id);
    db.userentityanalytics.remove({"_id" : doc._id});
})
db.userentityattempts.find({userId:userId,"entity.type":"TEST","entity.id":testId}).forEach(function(doc){
    print("userentityattempts _id :"+ doc._id);
    db.userentityattempts.remove({"_id" : doc._id});
})
db.userquestionanalytics.find({userId:userId,"parentEntity.type":"TEST","parentEntity.id":testId}).forEach(function(doc){
    print("userquestionanalytics _id :"+ doc._id);
    db.userquestionanalytics.remove({"_id" : doc._id});
})
db.userquestionattempts.find({userId:userId,"parentEntity.type":"TEST","parentEntity.id":testId}).forEach(function(doc){
    print("userquestionattempts_id :"+ doc._id);
    db.userquestionattempts.remove({"_id" : doc._id});
})
