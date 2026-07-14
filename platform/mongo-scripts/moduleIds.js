db.librarycontentlinks.find({"source.type":"MODULE", "target.id":"5551f9a7e4b051e1254d915d", "recordState":"ACTIVE", "linkType":"ADDED"}, {_id:1}).foreach(function(doc) {
    print(doc["_id"].toString());
})