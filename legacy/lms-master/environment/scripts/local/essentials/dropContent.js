 this is a mongo script which can be run as mongo DBNANE initContentSize.js
 this will insure that all contents are dropped completely use this only when you are 10000% sure  


var collections = ["cmdsfiles","files","cmdsmodules","modules","cmdsdocuments","documents","cmdsvideos","videos","cmdsquestions","questions","cmdstests","tests","cmdassignments","assignments","librarycontentlinks","cmdscontentlinks","comments"];
for( i=0;i<collections.length;i++){
					db.getCollection(collections[i]).drop();
				
}
