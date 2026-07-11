var startTime = 1586284200000;
var endTime = startTime + 31 * 1000 * 86400;
var videoId = "";
var currentPage = "VIDEO";
var currentAction = "VIEW";
var videoPage = false;
var returnFun = true;
print("Line No"+","+"UserId"+","+"OrgId"+","+"Device Type"+","+"Device Id"+","+"Calling App"+","+"Page"+","+"Action"+","+"Entity Id"+","+"Time Created"+","+"Entity Name"+","+"Entity Duration(in secs)"+","+"LinkType"+","+"Entity size");
db.activityrecords.find({timeCreated:{$gte:startTime,$lte:endTime}}).forEach(function(ar){
    if(returnFun){
        if(ar.page === "VIDEO" && ar.action === "VIEW" && videoId !== ar.entity["id"]){
            videoId = ar.entity["id"];
            db.videos.find({_id:ObjectId(videoId)}).forEach(function(video){
                print("13"+","+ar.userId + ","+ ar.orgId +","+ ar.deviceType + "," + ar.deviceId +","+ar.callingApp + "," + ar.page + ","+ar.action+"," +ar.entity["id"]+","+new Date(ar.timeCreated).toString()+","+"\""+video.name+"\""+","+(video.duration/1000)+","+video.linkType+","+video.size.original);
                videoPage = true;
            })
        }
        if(videoPage === true && (ar.entity !== null && ar.entity != undefined) && ar.entity["id"]!== videoId){
            print("18"+","+ar.userId + ","+ ar.orgId +","+ ar.deviceType + "," + ar.deviceId +","+ar.callingApp + "," + ar.page + ","+ar.action+"," +ar.entity["id"]+","+new Date(ar.timeCreated).toString()+","+"N.A"+","+"N.A"+","+"N.A"+","+"N.A");
            returnFun = true;
            videoPage = false;
        }
        else if(videoPage === true && (ar.page !== currentPage || ar.action !== currentAction)){
            print("23"+","+ar.userId + ","+ ar.orgId +","+ ar.deviceType + "," + ar.deviceId +","+ar.callingApp + "," +ar.page + ","+ar.action+"," +"N.A"+","+new Date(ar.timeCreated).toString()+","+"N.A"+","+"N.A"+","+"N.A"+","+"N.A");
            returnFun = true;
            videoPage = false;
        }
    }
});