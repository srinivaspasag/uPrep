var testId = "5a685af2e006c1f7f09a5da1";
var attemptId = "EMPTY";

db.entityuseractionmapping.find({"target.type":"TEST","target.id":testId,actionType:"ATTEMPTED"}).forEach(function(doc){
    // print("userId: "+ doc.userId);
    var count = db.userentityanalytics.find({userId:doc.userId,"entity.type":"TEST","entity.id":testId}).count();
    if(count == 0){
        db.userquestionattempts.find({userId:doc.userId,"parentEntity.type":"TEST","parentEntity.id":testId}).forEach(function(que){
            attemptId = que.attemptId;
            return ;
        });
        print("userId:" + doc.userId +","+attemptId);
    }
})