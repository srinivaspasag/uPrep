
db.cmdsfiles.find({}).forEach(function(e){
					  if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.cmdsfiles.save(e)
				}
)

db.cmdsvideos.find({}).forEach(function(e){
		                        if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.cmdsvideos.save(e)
			}
)

db.cmdsquestions.find({}).forEach(function(e){
					 if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.cmdsquestions.save(e);
				}
)

db.cmdsquestionsets.find({}).forEach(function(e){
					 if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.cmdsquestionsets.save(e);
			}

)



db.cmdsdocuments.find({}).forEach(function(e){
                                         if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.cmdsdocuments.save(e);
                                }
)

db.cmdassignments.find({}).forEach(function(e){
                                        var totalQuestionCount =0;
                                        for( var i=0;i<e.metadata.length;i++){
						if( e.metadata[i].qIds != undefined ){
                                                	totalQuestionCount+=e.metadata[i].qIds.length;
                                                }
                                        }
                                        print( "totalQuestions"+ totalQuestionCount+" " + e.qusCount);

                                        if(e.recordState == "ACTIVE" && e.qusCount == totalQuestionCount ){
                                           e.completed = true;
                                        }
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.cmdassignments.save(e);
                                }
)

db.cmdstests.find({}).forEach(function(e){
                                         var totalQuestionCount=0;
                                         for( var i=0;i<e.metadata.length;i++){
                                         	if( e.metadata[i].qIds != undefined ){
                                                	totalQuestionCount+=e.metadata[i].qIds.length;
                                                }
                                       }
                                         print( "totalQuestions"+ totalQuestionCount+" "+ e.qusCount);

                                         if(e.recordState == "ACTIVE" && e.qusCount == totalQuestionCount ){
                                           e.completed = true;
                                         }
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.cmdstests.save(e);
                                }
)


////////////////////////////////////////////////////////////////////////////////////////////


db.files.find({}).forEach(function(e){
					  if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.files.save(e)
				}
)

db.videos.find({}).forEach(function(e){
		                        if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.videos.save(e)
			}
)

db.questions.find({}).forEach(function(e){
					 if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.questions.save(e);
				}
)

db.questionsets.find({}).forEach(function(e){
					 if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
					printjson (e) ;
				        db.questionsets.save(e);
			}

)



db.documents.find({}).forEach(function(e){
                                         if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.documents.save(e);
                                }
)

db.assignments.find({}).forEach(function(e){
                                         if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.assignments.save(e);
                                }
)

db.tests.find({}).forEach(function(e){
                                         if(e.recordState == "ACTIVE"){
					   e.completed = true; 
					}
					else{
                                           e.completed = false;
					}
                                        printjson (e) ;
					db.tests.save(e);
                                }
)


