// var MODULE_ID = "57a9bd57e4b091ab3581211d"
db.modules.find({},{"children.entity":1}).
	forEach(function(module){
		module.children.forEach(function(child){
			if(child.entity instanceof Object){
				if(child.entity.type  === "VIDEO"){
					db.videos.find({"_id" : ObjectId(child.entity.id)},{"uuid":1}).
					forEach(function(video){
						print(video.uuid);
					})
				}
			}
		})
	}
);