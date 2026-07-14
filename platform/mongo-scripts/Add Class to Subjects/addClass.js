db.orgprograms.find({orgId:"5513a38de4b095b85aa162fd"}).forEach(function(doc){
    // print("_id :"+ doc._id);
    if(doc.courseIds != null){
        for (var i = 0;i <= doc.courseIds.length - 1; i++) {
            // print(doc.courseIds[i]);
            var objectId = new ObjectId(doc.courseIds[i]);
            db.boards.find({"_id" : objectId}).forEach(function(board){
                print("_id :"+ board.name);
                var index = board.name.indexOf('XI');
                if(index > -1){
                    var class1 = board.name.substr(index,3).trim();
                    db.boards.update({"_id" : board._id}, {$set: {"year": class1} });
                    print(class1); 
                }
            })
        }
    }
})