var gradeSubjectiveQuestion = new function(){
    this.init = function(){
        $(".sbutton").off("click")
                    .on("click",selectOption);
        $(".saveButton").off("click")
                        .on("click",saveAnswer);
        $(".cancelButton").off("click")
                          .on("click",cancelButton);
        $(".editButton").off("click")
                        .on("click",editButton);

        $(".load-more-attempts").off("click")
                                .on("click",loadMoreAttempts);
    }

    function selectOption(){
        var isCorrect = $(this).data("isCorrect");
        $(this).closest(".mark-button-holder").find(".sbutton").removeClass("green").removeClass("red").removeClass("orange").removeClass("active");
        if(isCorrect === "CORRECT"){
            $(this).parent().find(".show-input").addClass("nonner");
            $(this).addClass("green").addClass("active");
        }
        else if(isCorrect === "INCORRECT"){
            $(this).parent().find(".show-input").addClass("nonner");
            $(this).addClass("red").addClass("active");
        }
        else{
            $(this).parent().find(".show-input").removeClass("nonner");
            $(this).addClass("orange").addClass("active");
        }
    }

    function loadMoreAttempts(){
        showTopLoader();
        var start = parseInt($(this).data("start"));
        var size = parseInt($(this).data("size"));
        var qId = $(".question-info-div").data("qId");
        var testId = $(".subjective-question-page").data("testId");
        var params ={
            start:start+size,
            size:size,
            id:qId,
            testId:testId,
            loadQuestionInfo:false
        };
        vReq.post("/Tests/getMoreSubjectiveQuestionAttempts",params,function(data){
            $(".load-more-attempts").remove();
            hideTopLoader();
            $(".user-question-attempt-holder").append(data);
        });
    }

    function editButton(){
        $(this).closest(".mark-save-holder").addClass("nonner");
        $(this).parent().parent().find(".mark-button-holder").removeClass("nonner");
        $(this).parent().parent().find(".mark-button-holder .cancelButton").removeClass("nonner");
    }

    function cancelButton(){
        $(this).parent().parent().find(".mark-save-holder").removeClass("nonner");
        $(this).closest(".mark-button-holder").addClass("nonner");
        $(this).addClass("nonner");
    }

    function saveAnswer(){
        var saveAnswerRef = $(this)
        showTopLoader();
        var isCorrect = $(this).parent().find(".active").data("isCorrect");
        if(isCorrect === null || isCorrect === undefined || isCorrect === ""){
            showError("Please choose an option");
            hideTopLoader();
            return false;
        }
        var qId = $(".question-info-div").data("qId");
        var testId = $(".subjective-question-page").data("testId");
        var studentUserId = $(this).parent().parent().find(".user-question-attempt").data("userId");
        var attemptId = $(this).parent().parent().find(".user-question-attempt").data("attemptId");
        var positive = $(".marks-holder").data("positive");
        var negative = $(".marks-holder").data("negative");
        if(negative >0){
            negative = -negative;
        }
        var params = {
            qId:qId,
            testId:testId,
            studentUserId:studentUserId,
            isCorrect:isCorrect,
            attemptId:attemptId
        }
        if(isCorrect === "CORRECT"){
            params.score = parseFloat(positive);

        }
        else if(isCorrect === "INCORRECT"){
            params.score = parseFloat(negative);
        }
        else if(isCorrect === "PARTIAL"){
            var inputValue = $(this).parent().find(".partial-marks").val();
            if(inputValue === ""){
                hideTopLoader();
                showError("Please enter partial marks in input field");
                return;
            }
            var partialMarks = parseFloat(inputValue).toFixed(1);
            if(partialMarks >= positive || partialMarks <= negative ){
                showError("Enter number between max and min marks");
                hideTopLoader();
                return;
            }
            params.score = partialMarks;
        }
        vReq.post("/Tests/gradeTestSubjectiveQuestion",params,function(data){
            hideTopLoader();
            if(data.result.success){
                saveAnswerRef.parent().find(".show-input").addClass("nonner");
                saveAnswerRef.parent().parent().find(".mark-save-holder").removeClass("nonner");
                saveAnswerRef.closest(".mark-button-holder").addClass("nonner");
                saveAnswerRef.parent().parent().find(".mark-save-holder .user-question-attempt-status").html(""+params.isCorrect);
                saveAnswerRef.parent().parent().find(".mark-save-holder .marks-received").html(""+params.score);
            }
            else{
                showError("Grade not saved");
            }
        });
    }
}