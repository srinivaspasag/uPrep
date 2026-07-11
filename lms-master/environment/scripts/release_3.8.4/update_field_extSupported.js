db.orgsections.find({}).forEach(function(e){

	if( e.sdOnly != undefined	){
	e.extSupperted=e.sdOnly;
	e.sdOnly=undefined; 
	}
	printjson(e);
//	db.orgsections.save(e);

})
