var timeTaken = db.userentityanalytics.aggregate([{$match:{$and:[{"acadDim.type":"OVERALL"},
    {"entity.id":{$nin:["OVERALL",""]}}]}},
    {$group:{_id:"$userId",totalTime:{$sum:"$measures.timeTaken"}}}]);

timeTaken.forEach(function(obj){
    //var obj = timeTaken.next();
    print("userId is : "+obj._id);
    print("totalTime is : "+obj.totalTime);
    db.userentityanalytics.update({"userId" : obj._id,"acadDim.type":"OVERALL","entity.id":"OVERALL"}, {$set: {"measures.timeTaken": obj.totalTime} });
}); 
    