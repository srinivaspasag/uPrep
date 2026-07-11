var startTime = 1585074600000;
var nextTime = startTime + 3600000;
var endTime = 1585161000000;
while(nextTime <= endTime){
    var distinctUsers = db.activityrecords.distinct("userId",{timeCreated:{$gte:startTime,$lte:nextTime}});
    print(new Date(startTime).toLocaleString()+","+distinctUsers.length);
    startTime = nextTime;
    nextTime +=3600000;
}