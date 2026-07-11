
print("MemberId"+","+"Student Name"+","+"Login Activity"+","+"UserId");

function calculateLoginData(logins){
    var loginData = "";
    if(logins.length == 0){
        loginData = "Not Available";
    }
    for(j=0;j<logins.length;j++){
        loginData += logins[j]+(j<logins.length-1 ? " AND ":"");
    }
    return loginData;
}


db.orgmembers.find({orgId:"5eddc270e4b088bd96b255cc",profile:"STUDENT"}).forEach(function(member){
    var logins = [];
    db.activityrecords.find({userId:member.userId}).sort({timeCreated:-1}).limit(2).forEach(function(ar){
        logins.push(new Date(ar.timeCreated));
    });
    print(member.memberId + "," + "\""+member.firstName + " "+ member.lastName +"\""+","+ calculateLoginData(logins)+","+member.userId);
});