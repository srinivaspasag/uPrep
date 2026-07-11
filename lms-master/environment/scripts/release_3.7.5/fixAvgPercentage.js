//NOTE: execute this script as follows
// mongo databsename < fixAvgPercentage.js > fixAnalyticsResult.txt
db.userentityanalytics.find({"entity.type" : "TEST", "entity.id" : "OVERALL", "acadDim.id" : "OVERALL"}).forEach(
	function(e){
		var totalPercentage = 0;
		var totalAttempt = 0;
		db.userentityanalytics.find({"userId" : e.userId, "entity.type" : "TEST", "acadDim.type" : "OVERALL"}).forEach(
			function(f){
				totalPercentage+=f.percentage;	
				totalAttempt++;
		});
		var newPercentage = totalPercentage/totalAttempt;
		print("avgPercentage for userId: "+e.userId+" = "+newPercentage + ", totalAttempt="+totalAttempt);
		e.percentage=newPercentage;
		db.userentityanalytics.save(e);
	}
);
