//
var ENTITY = "TESTS";
//
var startDay = 1585679400000;
var endDay = 0;
//
var loop = 30;
var count = 0;
//
var orgId = "584e44e8c92e5dca741bae2a";

function getMonthlyTestData(){
    while(loop >count){
        if(count >0){
            startDay = endDay;
        }
        endDay = startDay + 86400000;
        print(new Date(startDay).toString()+","+new Date(endDay).toString()+","+db.userentityattempts.find({timeCreated:{$gte:startDay,$lte:endDay},"entity.type":"TEST",orgId:orgId}).count());
        count++;
    }
}

function getMonthlyVideoData(){
    while(loop >count){
        if(count >0){
            startDay = endDay;
        }
        endDay = startDay + 86400000;
        print(new Date(startDay).toString()+","+new Date(endDay).toString()+","+db.activityrecords.find({timeCreated:{$gte:startDay,$lte:endDay},page:"VIDEO",action:"OPEN",entity:{$exists:true},orgId:orgId}).count());
        count++;
    }
}

function getMonthlyDocsData(){
    while(loop >count){
        if(count >0){
            startDay = endDay;
        }
        endDay = startDay + 86400000;
        print(new Date(startDay).toString()+","+new Date(endDay).toString()+","+db.activityrecords.find({timeCreated:{$gte:startDay,$lte:endDay},page:"DOCUMENT",action:"OPEN",entity:{$exists:true},orgId:orgId}).count());
        count++;
    }
}

switch(ENTITY) {
    case "TESTS":
        getMonthlyTestData();
    break;
    case "VIDEOS":
        getMonthlyVideoData();
    break;
    case "DOCUMENTS":
        getMonthlyDocsData();
    break;
}

