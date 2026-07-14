db.accesscodes.find().forEach(
	function(e){ 
                if(e.orgId==null){
		   var orgmember=db.orgmembers.findOne({"userId": e.creatorId}); 
		   if(orgmember) { 
                        e.orgId = orgmember.orgId;
                        printjson (e) ;
                        db.accesscodes.save(e)
		   } 
                }           
	}) 
