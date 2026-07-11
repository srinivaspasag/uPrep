print("QId"+","+"Solns Field count"+","+"Soln Collection Record Count");
db.questions.find({}).forEach(function(ques){
    // if(ques.solutions === 0){
        var count = db.solutions.find({qId:ques._id.valueOf()}).count();
        if(ques.solutions == 0 && ques.solutions != count){
            // if(count == 0){
                // print(ques._id.valueOf()+","+ques.solutions+","+"No solutions"+","+new Date(ques.timeCreated));
            // }
            // else{
                print(ques._id.valueOf()+","+ques.solutions+","+count+","+new Date(ques.timeCreated));
            // }
        }
    // }
});