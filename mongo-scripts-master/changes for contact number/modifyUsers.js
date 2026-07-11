db.users.find({}).forEach(function(doc){
    print("_id :"+ doc._id);
    db.users.update({"_id" : doc._id}, {$set: {"isSysGenPassword": false} });
    db.users.update({"_id" : doc._id}, {$set: {"isPhoneVerified": false} });
    print(" ");
})