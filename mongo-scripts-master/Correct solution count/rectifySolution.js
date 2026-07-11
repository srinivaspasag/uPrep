db.questions.find({}).forEach(function(doc){
    print(doc._id.str);
    var count = db.solutions.find({"qId":doc._id.str}).count();
    print(count);
    var sol = NumberLong(count);
    print(sol);
    try{
        db.questions.update({"_id" : doc._id}, {$unset: {"solutions": ""} });
        db.questions.update({"_id" : doc._id}, {$set: {"solutions": sol} });
    }catch(err){
        print(err);
    }
    print(doc.solutions); 
})