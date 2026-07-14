db.librarycontentlinks.find({linkType:"ADDED"}).sort({"timeCreated":1}).forEach(function(e){
				if( e.position != undefined && e.position!=0){
				  print("no need to run" );
				  return;
				}			
				var fieldName=e.target.type.toLowerCase()+"_"+e.target.id+"_"+e.linkType.toLowerCase()+"_position";
				var result =db.counters.findAndModify({ query :{ "collection":"librarycontentlinks","field":fieldName },update :{ "$inc" : { "value":1}},upsert: true,new:true });
				
				e.position= result.value;
				db.librarycontentlinks.save(e);
			//var foundResult=db.librarycontentlinks.find( { "target" :{ "type":e.target.type,"id":e.target.id},"source":{ "type":e.source.type,"id":e.source.id}});
			
				}
)

