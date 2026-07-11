db.accesscodes.find({"buyerContactDetails.email":""}).forEach(function(doc){

    if(doc.userId){
        db.orgmembers.find({"userId":doc.userId}).forEach(function(orgm){
        db.accesscodes.update({"_id" : doc._id}, {$set: {"buyerContactDetails.email":orgm.email} });
            print("_id :"+ doc._id);
            print(orgm.email);
        })
    }
})