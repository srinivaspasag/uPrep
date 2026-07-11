db.orgmembers.find({orgId:"558400c4e4b0f968650c7a13","profile":"STUDENT"}).sort({"timeCreated":1}).forEach(function(doc){
    var from = new Date(doc.interval.from);
    var till; 
    if(doc.interval.till != -1){
        var till = new Date(doc.interval.till);
    }
    var date = new Date(doc.timeCreated);
    try{    
        var programs = "";
        if(typeof doc.mappings[0] !== 'undefined' && doc.mappings[0] !== 'null'){
            if(doc.mappings.length > 0){
                for(var i=0; i < doc.mappings.length; i++){
                    var objectId = new ObjectId(doc.mappings[i].programId);
                    db.orgprograms.find({"_id":objectId}).forEach(function(program){
                        if(i != 0) {
                            programs+=" & ";
                        }
                        programs+=program.name;
                    });
                }
            }
        }else{
            programs+="NO programs"
        }
    }catch(ex){
        print(ex);        
    }

    if(doc.status){
        if(doc.status.length > 0){
            var loggedInTime = new Date(doc.status[0].loginTime);
            print(doc.firstName+",",date+",",programs+",",loggedInTime+",",from+",",till);
        }else{
            print(doc.firstName+",",date+",",programs+",","Logged in time not available"+",",from+",",till);
        }
    }else{
        print(doc.firstName+",",date+",",programs+",","Logged in time not available"+",",from+",",till);
    }
})
