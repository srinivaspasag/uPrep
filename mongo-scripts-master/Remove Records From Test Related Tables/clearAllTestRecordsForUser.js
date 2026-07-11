db.entityuseractionmapping.find({userId:"584e488bc92e5dca741bae47","target.type":"TEST"}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.entityuseractionmapping.remove({"_id" : doc._id});
})
db.userentityanalytics.find({userId:"584e488bc92e5dca741bae47"}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.userentityanalytics.remove({"_id" : doc._id});
})
db.userentityattempts.find({userId:"584e488bc92e5dca741bae47"}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.userentityattempts.remove({"_id" : doc._id});
})
db.userquestionanalytics.find({userId:"584e488bc92e5dca741bae47"}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.userquestionanalytics.remove({"_id" : doc._id});
})
db.userquestionattempts.find({userId:"584e488bc92e5dca741bae47"}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.userquestionattempts.remove({"_id" : doc._id});
})