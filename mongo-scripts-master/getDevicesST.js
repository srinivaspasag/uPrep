var testId = "5c29afc1e0068c8ce72ed47f";

var users = [];

db.userentityattempts.find({"entity.id":testId}).forEach(function(attempt){
    users.push(attempt.userId);
});

var pages = ["TEST_ATTEMPT"];

var action = ["ATTEMPTED"];
db.activityrecords.find({userId:{$in:users},page:{$in:pages},action:{$in:action}}).forEach(function(record){
    db.orgmembers.find({userId:record.userId}).forEach(function(member){
        print("\""+member.firstName + " " + member.lastName +"\""+ ","+record.deviceType+","+record.deviceId+","+new Date(record.timeCreated)+","+record.page+","+record.action);
    });
});