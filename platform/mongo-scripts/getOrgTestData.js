var startDay = 1586370600000;
var endDay = 1586457000000;

var orgs = [];

var count = [];

db.userentityattempts.find({"entity.type":"TEST",timeCreated:{$gte:startDay,$lte:endDay}}).forEach(function(attempt){
    var index = orgs.indexOf(attempt.orgId);
    if(index === -1){
        orgs.push(attempt.orgId);
        count.push(1);
    }
    else{
        var newCount = count[index]+1;
        count[index] = newCount;
    }
});

for(i=0;i<orgs.length;i++){
    db.organizations.find({_id:new ObjectId(orgs[i])}).forEach(function(org){
        print(org.name+","+count[i]);
    });
}
