// this is a mongo script which can be run as mongo DBNANE initContentSize.js
// this will insure that all contents are zeroed 


var collections = ["cmdsfiles","files","cmdsmodules","modules","cmdsdocuments","documents","cmdsvideos","videos","cmdsquestions","questions","cmdstests","tests","cmdassignments","assignments"];
for( i=0;i<collections.length;i++){
        printjson(db.getCollection(collections[i]).count()); 
        db.getCollection(collections[i]).find( {recordState:"ACTIVE"}).forEach( function(e) {
                                        e.size={}       
                                      e.size.initialized=false;
                                      e.size.original=0;
                                      e.size.thumbnail=0;
                                      e.size.encrypted=0;
                                      e.size.converted=0;
                                      e.size.totalSize=0;
                                      e.size.finalized=false;
	                              printjson(e);   

					db.getCollection(collections[i]).save(e);
				
                                      })
}
//db.cmdsquestions.find({}).forEach(function(e){
//                                      e.size={}       
//                                      e.size.initialized=false;
//                                      e.size.original=0;
//                                      e.size.thumbnail=0;
//                                      e.size.encrypted=0;
//                                      e.size.converted=0;
//                                      e.size.totalSize=0;
//                                      e.size.finalized=false;
//                              printjson(e);   
//                                      //db.cmdsquestions.save(e);
//
//    
