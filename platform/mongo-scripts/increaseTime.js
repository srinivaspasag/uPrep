db.orgmembers.find({"orgId":"5930ffeae4b085173199e08b"}).forEach(function(doc){

    if(typeof doc.mappings !== 'undefined' && typeof doc.mappings[0] !== 'undefined'&& doc.mappings[0] !== 'null'){
        if(doc.mappings.length > 0){
            print("id " + doc._id);
            for(var i=0; i < doc.mappings.length; i++){
                var endTime = doc.mappings[i].endTime;
                if(endTime > 0){
                    print("mappings programId " + doc.mappings[i].programId);
                    print("endTime before " + endTime);
                    endTime+=2678400000;
                    print("endTime after " + endTime);
                    var update = {}; 
                    update["mappings." + i + ".endTime"] = NumberLong(endTime);
                    db.orgmembers.update({"_id" : doc._id},{$set : update});
                }
            }
        }
    }
})