
var list=db.getCollectionNames()
for( var i=0;i< list.length;i++){

var result = db.getCollection( list[i]).validate(true);
        var errorExist = false;
	if( result.errors != undefined && result.errors.length > 0){
		print("Error in "+ list[i]+" ");
		print("Errors: "+ result.errors);

	}
	if( !errorExist ){
		printjson( result)
		print ( " No Error found for" + list[i]);
	}
		
}


