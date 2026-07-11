db.solutions.find({}).forEach(function(doc){
    var objectId = new ObjectId(doc.qId);
    db.questions.find({"_id" : objectId}).forEach(function(question){
        if(question.solutions == 0){
            print(question.solutions);
            var solution = "1";
            var sol = new NumberLong(solution);
            db.questions.update({"_id" : question._id}, {$set: {"solutions": sol} });
        }
    })
})