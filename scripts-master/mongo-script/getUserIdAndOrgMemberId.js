// Put List of memberIDs here
var memberIds = [""];

db.orgmembers.find({memberId:{$in:memberIds},orgId:"584e44e8c92e5dca741bae2a",profile:"STUDENT"}).forEach(function(member){
    print(member.userId + ","+member._id.valueOf());
});