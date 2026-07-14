db.orders.find().forEach(function(e){
        var item = e.items[0].item;
	var saveToDB=false;
	if(item.type=="SECTION" && item.id){
       	    printjson(item);
  	    var orgSection =  db.orgsections.findOne({_id:ObjectId(item.id)});
	    e.items[0].seller= {"type" : "ORGANIZATION", "id" : orgSection.orgId};
            saveToDB=true; 
	   //printjson(e);
	}else if(item.type=="PLAN" && item.id){
	    e.items[0].seller= {"type" : "ORGANIZATION", "id" : "VEDANTU"};	    
            saveToDB=true; 
	}

	if(saveToDB){
		db.orders.save(e);
	}
        //printjson(e.items);
})
