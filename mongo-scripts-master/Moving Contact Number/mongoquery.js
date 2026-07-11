db.orgmembers.find({'extraInfo' :{$ne : null}}).forEach(function(orgmember){
	if (orgmember.extraInfo.length != 0){	
	    contactNumber = orgmember.extraInfo[0].value;
	    print("Name" + orgmember.firstName +"Contact Number :"+contactNumber);
	    db.orgmembers.update({"_id" : orgmember._id}, {$set: {"contactNumber": contactNumber }});
	}
})