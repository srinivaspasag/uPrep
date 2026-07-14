var dryRun=false;
db.challengeleaderboard.dropIndex("userId_1_challengeId_1");
db.challengetakens.dropIndex("userId_1_challengeId_1");
db.challenges.find().forEach(function(e){
	var p = e.contentSrc;
	printjson(p);
	var challId=e._id.str;
	db.challengetakens.find({"challengeId" : challId}).forEach(function(ct){
		ct.parent=p;
			if(dryRun){
			printjson(ct);
			}else{
			db.challengetakens.save(ct);
			}
		});
	
	db.challengeleaderboard.find({"challengeId" : challId}).forEach(function(cl){
		cl.parent=p;
			if(dryRun){
			printjson(cl);
			}else{
			db.challengeleaderboard.save(cl);
			}
		});

	db.challengeuserinfos.find().forEach(function(m){
		var userId = m.userId;
		var count = db.orgmembers.count({"userId" : userId});
		if(count==1){
			m.parent={"id" : db.orgmembers.findOne({"userId" : userId}).orgId, "type" : "ORGANIZATION"}
				if(dryRun){
				printjson(m)
				}else{
				db.challengeuserinfos.save(m);
				}
			}
		});
	db.multiplierpowers.find({"src.id" : challId}).forEach(function(m){
		var userId = m.userId;
                    m.parent=p;
			if(dryRun){
			  printjson(m)
			}else{
	        	  db.multiplierpowers.save(m);
			}
               });
	}
);
