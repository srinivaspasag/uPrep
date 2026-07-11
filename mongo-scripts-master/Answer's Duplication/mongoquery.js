function unique(arr) {
	print("Array :"+arr);
    var hash = {}, result = [];
    for ( var i = 0, l = arr.length; i < l; ++i ) {
        if ( !hash.hasOwnProperty(arr[i]) ) { 
            hash[ arr[i] ] = true;
            result.push(arr[i]);
        }
    }
	print("Result :"+result);
    return result;
}

db.answers.find({'answer.1': {$exists: true}}).forEach(function(doc){
	print("Id :"+doc._id);
    db.answers.update({"_id" : doc._id}, {$set: {"answer": unique(doc.answer)} });
})