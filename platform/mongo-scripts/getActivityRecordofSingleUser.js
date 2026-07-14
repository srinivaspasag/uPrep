var orgId = "561f82e7e4b04529cf8dcdb2";

// var date = new Date();
// var endDate = date.getTime();
// var startDate = date.setDate(date.getDate()-730);

// var pages = ["DOCUMENT","VIDEO","TEST"];

var studentsList = [];

db.orgmembers.find({orgId:orgId,recordState:"ACTIVE",profile:"STUDENT",userId:"5d3fce87e4b043314fdc6aa3"}).forEach(function(member){
    var studentData  = {
    };
    studentData.userName = member.firstName + " " + member.lastName;
    studentData.userId = member.userId;
    studentData.memberId = member.memberId;
    studentData.contactNumber = member.contactNumber;
    studentData.email = member.email;
    studentData.timeJoined = new Date(member.timeCreated);
    studentsList.push(studentData);
});
print("MemberId, Student Name, Student Email, Contact Number, Time Joined , Tests Taken, Documents Read, Unique Documents Read, Videos Watched,Unique Videos Watched,Doubts Posted");
// for(j=0;j<pages.length;j++){
    for(i=0;i<studentsList.length;i++){
        var finalOutput = studentsList[i].memberId +"," +
                            "\""+studentsList[i].userName+"\"" +"," +
                            "\""+studentsList[i].email +"\""+ "," +
                            "\""+studentsList[i].contactNumber+"\"" +"," +
                            studentsList[i].timeJoined +"," +
                            db.userentityattempts.find({userId:studentsList[i].userId,"entity.type":"TEST"}).count()+"," +
                            db.activityrecords.find({page:"DOCUMENT",entity:{$exists:true},userId:studentsList[i].userId}).count()+"," +
                            db.activityrecords.distinct("entity.id",{page:"DOCUMENT",entity:{$exists:true},userId:studentsList[i].userId}).length+"," +
                            db.activityrecords.find({page:"VIDEO",entity:{$exists:true},userId:studentsList[i].userId}).count()+"," +
                            db.activityrecords.distinct("entity.id",{page:"VIDEO",entity:{$exists:true},userId:studentsList[i].userId}).length+"," +
                            db.activityrecords.find({page:"DOUBTS",action:"ADD",entity:{$exists:true},userId:studentsList[i].userId}).count();
      print(finalOutput);
        // print(studentsList[i].memberId +"," + studentsList[i].userName + ","+ studentsList[i].email + ","+ studentsList[i].contactNumber +","+db.activityrecords.find({page:"DOCUMENT",entity:{$exists:true},userId:studentsList[i].userId}).count()+","+db.activityrecords.find({page:"VIDEO",entity:{$exists:true},userId:studentsList[i].userId}).count()+","+db.activityrecords.find({page:"TEST",entity:{$exists:true},userId:studentsList[i].userId}).count());
    }
    // print();
    // print();
// }