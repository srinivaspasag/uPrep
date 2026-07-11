print("Original File Name,","Name,","Subject");
db.videos.find({}).forEach(function(video){
        var subject = "";
        for(var i=0; i < video.boardIds.length; i++){
            var objectId = new ObjectId(video.boardIds[i]);
            db.boards.find({"_id":objectId}).forEach(function(board){
                if(i != 0) {
                    subject+=" & ";
                }
                subject+=board.name;
            });
        }
        print(video.originalFileName+",",video.name+",",video.thumbnail+",",subject);
    }
);