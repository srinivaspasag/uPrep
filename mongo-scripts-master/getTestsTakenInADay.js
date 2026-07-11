var orgId = "5dedf6d2e4b0897459b023a2";

var startDay = 1586370600000;
var endDay = 1586457000000;

var tests = [];
var count = [];

db.userentityattempts.find({"entity.type":"TEST",orgId:orgId,timeCreated:{$gte:startDay,$lte:endDay}}).forEach(function(attempt){
    var index = tests.indexOf(attempt.entity["id"]);
    if(index === -1){
        tests.push(attempt.entity["id"]);
        count.push(1);
    }
    else{
        var newCount = count[index]+1;
        count[index] = newCount;
    }
});


for(i=0;i<tests.length;i++){
    db.tests.find({_id:new ObjectId(tests[i])}).forEach(function(test){
        print("\""+test.name+"\""+","+test._id.valueOf()+","+count[i]);
    });
}