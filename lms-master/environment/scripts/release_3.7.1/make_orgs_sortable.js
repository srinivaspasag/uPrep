function replaceAll(find, replace, str) {
  return str.replace(new RegExp(find, 'g'), replace);
}
var replaceRegex="[^a-zA-Z0-9]";
db.orgdepartments.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex," ", value );
					printjson ( e) ;
				 db.orgdepartments.save(e)
				}
)

db.orgprograms.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex," ", value );
					printjson ( e) ;
				 db.orgprograms.save(e)
			}
)

db.orgcenters.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex,"", value );
					printjson ( e) ;
				        db.orgcenters.save(e);
				}
)

db.orgsections.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex,"", value );
					printjson ( e) ;
				       db.orgsections.save(e);
			}

)



db.organizations.find({}).forEach(function(e){
                                        var value = e.fullName.toLowerCase();
                                        e.cName = replaceAll( replaceRegex,"",value );
                                        printjson ( e) ;
					db.organizations.save(e);
                                }
)

