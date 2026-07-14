db.cmdscontentlinks.find({linkType:"ADDED"}).sort({"timeCreated":1}).forEach(function(e){
				if( e.position != undefined ){
				  print("no need to run" );
				  return;
				}			
				var fieldName=e.target.type.toLowerCase()+"_"+e.target.id+"_"+e.linkType.toLowerCase()+"_position";
				var result =db.counters.findAndModify({ query :{ "collection":"cmdscontentlinks","field":fieldName },update :{ "$inc" : { "value":1}},upsert: true,new:true });
				
				e.position= result.value;
				db.cmdscontentlinks.save(e);
				if( e.globalLinkId != undefined){
				printjson(e.globalLinkId+" "+e.target.type+" " + e.target.id+ " " + e.source.type+ " " +e.source.id);
			//var foundResult=db.librarycontentlinks.find( { "target" :{ "type":e.target.type,"id":e.target.id},"source":{ "type":e.source.type,"id":e.source.id}});
			
		var foundResult=db.librarycontentlinks.find( { "_id":ObjectId(e.globalLinkId) });
			if( foundResult != undefined ){
			foundResult.forEach( function(ile){
				printjson( ile);
				ile.position=e.position;
				printjson( ile);
				db.librarycontentlinks.save(ile);
			})
					}
				}
			
				}
)

