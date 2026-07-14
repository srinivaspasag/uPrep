function replaceAll(find, replace, str) {
  return str.replace(new RegExp(find, 'g'), replace);
}
var replaceRegex="[^a-zA-Z0-9]";
db.cmdsfolders.find({}).forEach(function(e){
					var value = e.name.toLowerCase();
					e.cName = replaceAll( replaceRegex,"", value );
					printjson ( e) ;
				 db.cmdsfolders.save(e)
				}
)

