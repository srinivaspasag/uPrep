db.duplicate_files.find().forEach( function(e) {
	if (e.value != null) {
		print(" NEW CONTENT UPDATE");
		cmdsQuery = {};
		cmdsQuery._id = new ObjectId(e._id);
		cmdsContent = db.cmdsfiles.findOne(cmdsQuery);
		contentQuery = {};
		contentQuery.cmdsFileId = cmdsContent._id.valueOf();
		
		latestContentQuery = {};
		latestContentQuery._id = new ObjectId(
				cmdsContent.globalFileId);
		latestContent = db.files.findOne(latestContentQuery);
		print( " Latest ID : "+ latestContent._id.valueOf() )
		db.files.find(contentQuery).forEach( function(ileContent) {
			if (ileContent._id.valueOf() != latestContent._id.valueOf()) {
				print( " CURRENT ID : "+ ileContent._id.valueOf() )
				voteDiff = 0
				upvoteQuery = {
					"actionType":"VOTED",
					"target.id": ileContent._id.valueOf(),
					"target.type":"FILE"
					
				}
				printjson(upvoteQuery);
				db.entityuseractionmapping.find(upvoteQuery).forEach( function(actionMap) {
					printjson(actionMap);
					upvoteQuery = {
									"userId":actionMap.userId,
									"actionType":"VOTED",
									"target.id": latestContent._id.valueOf(),
									"target.type":"FILE"
					}
					votedResult = db.entityuseractionmapping.findOne(upvoteQuery);
					print("check for null object")
					if (votedResult == null) {
						voteDiff++;
						actionMap.target.id = latestContent._id.valueOf();
						print("New voteaction");
					    // printjson(actionMap);
					    db.entityuseractionmapping.save(actionMap);
					}
				})
				upvoteQuery = {
					"actionType":"VOTED",
					"target.id": ileContent._id
					.valueOf(),
					"target.type":"FILE"
				}
			// db.entityuseractionmapping.remove(upvoteQuery);
				commentActionFindQuery = {
					"actionType":"COMMENTED",
					"target.id": ileContent._id.valueOf(),
					"target.type":"FILE"
				}
				db.entityuseractionmapping.find(commentActionFindQuery).forEach(function(commentAction) {
					printjson(commentAction);
					commentAction.target.id = latestContent._id.valueOf();
					print("New commentAction");
					printjson(commentAction);
					db.entityuseractionmapping.save(commentAction);
				})
				viewActionFindQuery = {
					"actionType":"VIEWED",
					"target.id": ileContent._id.valueOf(),
					"target.type":"FILE"
				}
				db.entityuseractionmapping.find(viewActionFindQuery).forEach(function(viewAction) {
					printjson(viewAction);
					viewAction.target.id = latestContent._id.valueOf();
					print("New viewAction ");
					printjson(viewAction);
					db.entityuseractionmapping.save(viewAction);
				})
					commentFindQuery = {
					 "$or":[ { 
						 "base.id":ileContent._id.valueOf(),  
						 "base.type":"FILE"
					 },{
						 "root.id":ileContent._id.valueOf(),
						 "root.type":"FILE"
					 },{
						 "parent.id":ileContent._id.valueOf(),
						 "parent.type":"FILE"
					 }
					        ],
				}
				printjson( commentFindQuery );
				db.comments.find(commentFindQuery).forEach( function(comment) {
					print("old  comment");
					printjson(comment);
					if( comment.base != null && comment.base.id ==ileContent._id.valueOf() ){
						comment.base.id = latestContent._id.valueOf();
					}
					if( comment.parent != null && comment.parent.id ==ileContent._id.valueOf() ){
						comment.parent.id = latestContent._id.valueOf();
					}
					
					if( comment.root != null && comment.root.id ==ileContent._id.valueOf() ){
						comment.root.id = latestContent._id.valueOf();
					}
					print("New  comment");
					printjson(comment);
					db.comments.save(comment);
				})
				print("vote difference was "+ voteDiff);
				latestContent.upVotes = latestContent.upVotes+ voteDiff;
				latestContent.views = latestContent.views+ ileContent.views;
				latestContent.comments = latestContent.comments+ ileContent.comments;
				print("Updating library links ");
				librayLinkQuery = {
					 "source.id":ileContent._id.valueOf(),
					 "source.type":"FILE"
				}
				printjson( librayLinkQuery );
				db.librarycontentlinks.find(librayLinkQuery	).forEach( function(libraryLink) {
					print("old  libraryLink");
					printjson(libraryLink);
					libraryLink.source.id = latestContent._id.valueOf();
					print("New libraryLink");
					printjson(libraryLink);
					db.librarycontentlinks.save(libraryLink);
				})
				if( ileContent.timeCreated < latestContent.timeCreated ){
									latestContent.timeCreated= ileContent.timeCreated;
				} 
				db.files.remove( ileContent);
				
			}
		});
		db.files.save( latestContent);
	}
})
