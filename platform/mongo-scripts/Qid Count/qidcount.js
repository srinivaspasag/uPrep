function assignCmdsQuestionCount(cmdsQuestionsMap, cmdsQId) {

  typeof cmdsQuestionsMap[cmdsQId] === 'undefined' ? cmdsQuestionsMap[cmdsQId] = 1 : cmdsQuestionsMap[cmdsQId]++;
}

function assignGlobalAnswerId() {

}

var cmdsQuestionsMap = {};
db.questions.find({}).forEach(function(question){
        cmdsQId = question.cmdsQId;
        assignCmdsQuestionCount(cmdsQuestionsMap,cmdsQId);
})

var sortedArray = [];
for (contactNumberValue in cmdsQuestionsMap){
    sortedArray.push([contactNumberValue, cmdsQuestionsMap[contactNumberValue]])
}
sortedArray.sort(function(a,b){
    return b[1] - a[1]
})

dupCount = 0;
ansSetCount = 0;
db.questions.find({}).forEach(function(question){
        cmdsQId = question.cmdsQId;
        if(cmdsQuestionsMap[cmdsQId] > 1){
            dupCount++;
            if(typeof cmdsQId !== 'undefined'){
                var objectId = new ObjectId(cmdsQId);
                db.cmdsquestions.find({"_id" : objectId}).forEach(function(cmdsquestion){
                    var answerId = cmdsquestion.solutionInfo.globalAnsId;
                    db.questions.update({"_id":question._id},{$set: {"answerId": answerId}});
                    ansSetCount++;
                })
            }
        }
})
print("dupCount :"+dupCount);
print("ansSetCount :"+ansSetCount);