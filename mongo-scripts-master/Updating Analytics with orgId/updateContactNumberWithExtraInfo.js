var j = 0;

db.orgmembers.find({contactNumber:"",orgId:"5930ffeae4b085173199e08b"}).forEach(function(doc){
    if(doc.extraInfo != null && doc.extraInfo.length > 0){
        for(i=0;i < doc.extraInfo.length;i++){
            if(doc.extraInfo[i].name === "Contact Number" && doc.extraInfo[i].value != ""){
                contactNumber = doc.extraInfo[i].value;
                print("Name" + doc.firstName +"Contact Number :"+contactNumber);
                j=j+1;
                break;
                //db.orgmembers.update({"_id" : doc._id}, {$set: {"contactNumber": contactNumber }});
            }
        }
    }
});
print("Total count is: "+ j);