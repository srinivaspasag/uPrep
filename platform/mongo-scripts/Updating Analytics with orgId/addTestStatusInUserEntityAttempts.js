db.userentityattempts.find({"entity.type":"TEST"}).forEach(function(doc){
    if(doc.finished == true){
        db.userentityattempts.update({"_id" : doc._id}, {$set: {"testStatus": "FINISHED"} });
    }
    else{
        db.userentityattempts.update({"_id" : doc._id}, {$set: {"testStatus": "ONGOING"} });
    }
});