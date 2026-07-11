function replaceAll(find, replace, str) {
  return str.replace(new RegExp(find, 'g'), replace);
}
var replaceRegex="[^a-zA-Z0-9]";
db.boards.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex,"", value );
					printjson ( e) ;
					if( e.cAliases != undefined ){
					   test=[];
						for( var i in e.cAliases ){
							var op = replaceAll( replaceRegex,"", e.cAliases[i]);	
							test.push( op )
						}

						e.cAliases = test;	
					}
				  printjson( e ) 
				 db.boards.save(e)
				}
)

