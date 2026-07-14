
db.activityrecords.find({"entity.id":"5e9ae9a0e4b087876c866118",page:"TEST_ATTEMPT",action:"ATTEMPTED"}).forEach(function(record){
    db.orgmembers.find({userId:record.userId}).forEach(function(member){
        print("\""+member.firstName + " " + member.lastName +"\"" + ","+member.memberId + ","+record.deviceType+","+record.deviceId+","+record.userId+","+new Date(record.timeCreated).toLocaleString());
    });
});