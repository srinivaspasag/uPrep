var takeTest = {
    data:"",
    submitAnswerUrl:testFns.urls["submitAnswerUrl"],
    resetAttemptUrl:testFns.urls["resetAttemptUrl"],
    currentSubject:"",
    currentSubjectIndex:0,
    currentQuestionIndex:0,
    markedForReviewArray:null,
    messageShown:false,
    testId:"",
    startTime:new Date().getTime(),
    qTypes:[],
    wdthTimeout:'',
    messages:{
        timeOver : "Out of Time! Test has ended and your progress is submitted.",
        answerReq : "Please choose an answer to save and proceed"
    },
    init:function(){
        this.stopTimers();
        var testId = $(".incommingTestId").val();
        if(!testId) testId = getURLParameter("testId");
        if(!testId) return;
        this.registerFns();
        this.testId = testId;
        try{
            this.data = takeTestData;
            this.prepareData();
            this.currentSubject = $(this.data.boards).get(this.currentSubjectIndex);
            this.startTimer();
            this.updateSubjectQType(this.currentSubject);
        }catch(err){
            console.error(err);
            var url = testFns.urls["getQuestionsJson"];
            vReq.get(url,{"id":testId,"qTypeDistribution":true},function(data){
                var t = takeTest;
                t.data = data;
                t.prepareData();
                t.currentSubject = $(data.boards).get(t.currentSubjectIndex);
                t.startTimer();
                t.updateSubjectQType(t.currentSubject);
            });
        }
        this.startTime = new Date().getTime();
    },
    updateAnswerGiven:function(ques,subIndex){
        var t = takeTest;
        var allAns = t.data.answersGiven;
        if(!allAns || allAns.length<0 || !ques) return;
        $(ques).each(function(){
            if(allAns[this.id]){
                this.answer = allAns[this.id];
                this.state = "save_and_next";
            }
        });
        if(subIndex == t.currentSubjectIndex){
            setTimeout(function(){t.drawQuestion(0,true)},10);
        }
    },
    prepareData:function(){
        var t = takeTest;
        var boards = this.data.boards;
        for(var bIndex = 0;bIndex<boards.length;bIndex++){
            boards[bIndex].typeLocator = [];
            var questionTypes = boards[bIndex].questions;
            var questions = [];
            for(qType in questionTypes){
                var ques = questionTypes[qType].questions;
                if(!ques || ques.length<1) continue;
                var type = questionTypes[qType].type;
                boards[bIndex].typeLocator[type] = qType;
                boards[bIndex].typeLocator.length++;
                var m = pushIfAbsent(t.qTypes,type);
                questions = questions.concat(ques);
            }
            boards[bIndex].quesByTypes = boards[bIndex].questions;
            boards[bIndex].questions = boards[bIndex].allQuestions = questions;
            try{t.updateAnswerGiven(questions,bIndex);}catch(err){console.log(err);}
        }
    },
    updateSubjectQType:function(subData){
            if(subData && subData.typeLocator.length>1){
            $(".takeTestFilterQTypes").removeClass("hidden");
            vSelectFns.init();
                uiCloneHelper.set(".selectTestFilterQType .vSelectList",".vSelectEach",0);var index=0;
                uiCloneHelper.removeFrom(1);
            for(type in subData.typeLocator){
                var sp = uiCloneHelper.create(++index);
                $(sp).text("Only "+type+"'s");
                $(sp).data("value",type);
            }
            $(".selectTestFilterQType").resetVSelectTag({newVal:{'text':'All Types','val':''}});
            }else{
            $(".takeTestFilterQTypes").addClass("hidden");
        }
        vSelectFns.init();
    },
    startTimer:function(){
    try{
        this.data.duration = parseInt(this.data.duration,10);
        if(!this.data.duration || this.data.duration <= 0) return false;
        try{
            this.timeLeft = testDownTime.updateTimer(this.data.duration);
        }catch(err){
            this.timeLeft = this.data.duration/1000;
        }
        this.timeLeftDiv = $(".test_home .test_subjects .test_timer .total_time_left");
        this.tt = setTimeout(function(){
            takeTest.showErrorMsg(takeTest.messages.timeOver);
            endCurrentExam(takeTest.testId);
        },this.timeLeft*1000);//time left in milisec
        takeTest.timeLeft = parseInt(takeTest.timeLeft,10);
        this.tIntv = setInterval(function(){
            takeTest.timeLeft--;
            var hrs = Math.floor(takeTest.timeLeft/3600);
            var mins = ("0"+Math.floor((takeTest.timeLeft-hrs*3600)/60)).slice(-2);
            var secs = ("0"+(takeTest.timeLeft - (mins*60 + hrs*3600))).slice(-2);
            $(takeTest.timeLeftDiv).text(hrs+":"+mins+":"+secs);
        },1000);
        this.internet = setInterval(function(){
            takeTest.internetConnectionCheck();
        },5000);
    }catch(err){}
    },
    stopTimers:function(){
        try{
        if(this.tIntv) clearInterval(this.tIntv);
        if(this.tCIntv) clearInterval(this.tCIntv);
        if(this.tt) clearInterval(this.tt);
        if(this.internet) clearInterval(this.internet);
        }catch(err){}
    },
    endExam:function(e){
        if(!takeTest.ifInternetAvailable()) return;
        endCurrentExam(takeTest.testId);
    },
    changeSubject:function(e){
        $('.errorMsg').addClass('hidden');
        takeTest.updateSubjectQType(takeTest.currentSubject);
        $(e.delegateTarget).find(".selected").removeClass("selected");
        $(e.currentTarget).addClass("selected");
        takeTest.currentSubjectIndex = $(e.delegateTarget).find("span").index(e.currentTarget);
        takeTest.currentSubject = $(takeTest.data.boards).get(takeTest.currentSubjectIndex);
        takeTest.currentSubject.questions = takeTest.currentSubject.allQuestions;
        $(".show_only_marked input").attr("checked",false);
        takeTest.drawQuestion(0,true);
        $(".test_question_summary").find(".sub_name").text(takeTest.currentSubject.name);
    },
    changeSubjectDropDown:function(e){
        $('.errorMsg').addClass('hidden');
        takeTest.updateSubjectQType(takeTest.currentSubject);
        $(e.delegateTarget).find(".selected").removeClass("selected");
        $(e.currentTarget).addClass("selected");
        takeTest.currentSubjectIndex = $('option:selected',this).index();
        takeTest.currentSubject = $(takeTest.data.boards).get(takeTest.currentSubjectIndex);
        takeTest.currentSubject.questions = takeTest.currentSubject.allQuestions;
        $(".show_only_marked input").attr("checked",false);
        takeTest.drawQuestion(0,true);
        $(".test_question_summary").find(".sub_name").text(takeTest.currentSubject.name);
    },
    preRefresh:function(e){
        if(!takeTest.ifInternetAvailable()) return;
        var $this = $(this);
        var quesBody = $this.closest($this.data("closest"));
        quesBody.data("answer",takeTest.getAnswers());
    },
    postRefresh:function(e){
        var $this = $(this);
        var quesBody = $this.closest($this.data("closest"));
        var answers = quesBody.data("answer");
        var question = $(takeTest.currentSubject.questions).get(takeTest.currentQuestionIndex);
        takeTest.drawAnswer(quesBody,question,answers);
        quesBody.find(".quesOptionsExceeded").removeClass("quesOptionsExceeded");   
        loadTestMJEqns(quesBody.get(0));    
    },
    saveAndNextQuestion:function(e){
        if(!takeTest.ifInternetAvailable()) return;
        var question = $(takeTest.currentSubject.questions).get(takeTest.currentQuestionIndex);
        question.answer = takeTest.getAnswers();
        if(question.answer.length<=0){
            showError(takeTest.messages.answerReq);
            return true;
        }
        $('.errorMsg').addClass('hidden');
        var oldState = question.state;
        question.state = "save_and_next";
        takeTest.submitAnswerToServer("COMPLETE",question,function(success,data){
            if(!success){
                question.state = oldState;
                question.answer = undefined;
            }
        });
        takeTest.nextQuestion(e);
    },
    submitAnswerToServer:function(state,question,cbFn){
        if(!takeTest.ifInternetAvailable()) return;
        var endTime = new Date().getTime();
        var timeTaken = endTime - takeTest.startTime;
        var params = {
            "type":question.type,
            "timeTaken":timeTaken,
            "answerGiven":question.answer,
            "qId":question.id,
            "testId":takeTest.testId,
            "status":state
        };
        $.post(takeTest.submitAnswerUrl,params,function(data){
            var success = false;
            if(data){
               if(data.errorCode!=""){
                try{
                    takeTest.showErrorMsg(data.errorMessage?data.errorMessage:takeTest.messages.timeOver);
                    if(data.errorCode=="TEST_TIME_OVER"){
                        endCurrentExam(takeTest.testId);
                    }
                }catch(err){    
                }
               }else{
                success = true;
               }
            }
            if(cbFn){
                try{ cbFn(success,data);}catch(err){}
            }
        });
    },
    onLine:true,
    internetConnectionCheck:function(){
        $.ajax({
            url:"/Tests/ping",
            cache:false,
            timeout:10000,
            type:"HEAD"
        }).done(function(data,textStatus,xhr){
            takeTest.onLine = true;
            onResponse();
            xhr.processed = true;
        }).fail(function(xhr,textStatus){
            takeTest.onLine = false;
            onResponse();
            xhr.processed = true;
        }).always(function(data,stat,xhr){
            if(xhr.processed) return;
            var pingHeaders;
            try{
                pingHeaders = xhr.getAllResponseHeaders();
            }catch(err){ pingHeaders = null;}
            if(pingHeaders){
                takeTest.onLine = true;
            }else{
                takeTest.onLine = false;
            }
            onResponse();
        });
        function onResponse(){
           var testHead = $(".test_head");
           if(navigator.onLine && takeTest.onLine){
            testHead.find(".onlineIcon").removeClass("hidden");
            testHead.find(".offlineIcon").addClass("hidden");
            $(".testOfflineMsg").addClass("hidden");
            $(".errorMsg").addClass('hidden');
           }else{
            testHead.find(".offlineIcon").removeClass("hidden");
            testHead.find(".onlineIcon").addClass("hidden");;
            $(".testOfflineMsg").removeClass("hidden");
           }
        }
        onResponse();
        return navigator.onLine && takeTest.onLine;
    },
    ifInternetAvailable:function(){
        if(!(navigator.onLine && takeTest.onLine)){
            showError("Please try after your device Internet Connection is available again!");
            return false;
        }
        return true;
    },
    nextQuestion:function(e){
        $('.errorMsg').addClass('hidden');
        var nextIndex = this.currentQuestionIndex + 1;
        if(this.markedForReviewArray){
            nextIndex = this.markedForReviewArray[this.markedForReviewArray.indexOf(this.currentQuestionIndex)+1];
        }
        if(nextIndex < takeTest.currentSubject.questions.length && nextIndex >= 0){
            this.drawQuestion(nextIndex);
        }else{
            this.showMessage();
        }
    },
    markForReview:function(e){
        $('.errorMsg').addClass('hidden');
        var question = $(takeTest.currentSubject.questions).get(takeTest.currentQuestionIndex);
        question.answer = takeTest.getAnswers();
        /*if(question.answer<0){
            alert("Please choose an answer and proceed");
            return true;
        }*/
        question.state = "mark_for_review";
        //takeTest.submitAnswerToServer("REVIEW",question);
        takeTest.nextQuestion(e);
    },
    skipQuestion:function(e){
        $('.errorMsg').addClass('hidden');
        var question = $(takeTest.currentSubject.questions).get(takeTest.currentQuestionIndex);
        //takeTest.submitAnswerToServer("SKIP",question);
        takeTest.nextQuestion(e);
    },
    reset:function(e){
        $('.errorMsg').addClass('hidden');
        $(".test_home .questionBody").find("input:checked").each(function(){
            this.checked = false;
        });
        $(".test_home").find(".questionBody").find(".numericAnsInputBox").val("").text("");
    },
    resetAttempt:function(e){
        $('.errorMsg').addClass('hidden');
        var index = takeTest.currentQuestionIndex;
        var question = $(takeTest.currentSubject.questions).get(index);
        if(!takeTest.resetAttemptUrl || !question) return;
        takeTest.reset();
        if(!takeTest.ifInternetAvailable()) return;
        var params = {
            "type":question.type,
            "qId":question.id,
            "testId":takeTest.testId
        };
        $.get(takeTest.resetAttemptUrl,params,function(data){
            if(data && data.errorCode=="" && data.result.success){
            }else if(question.answer && question.state == "save_and_next"){
                takeTest.showErrorMsg("Reset Question failed, due to some un-expected error!");
            }
            $(question).removeProp("answer");
            $(question).removeProp("state");
            takeTest.updateQuestionSummary(index);
        });
    },
    changeQuestion:function(e){
        $('.errorMsg').addClass('hidden');
        var index = $(e.delegateTarget).find(".each_question").index(e.currentTarget);
        takeTest.drawQuestion(index);
    },
    drawQuestion:function(index,drawFreshly){
        this.reset();
        this.hideMessage();
        this.currentQuestionIndex = index;
        var question = $(this.currentSubject.questions).get(index);
        var quesBody = $(".test_home .test_questions .questionBody");
        quesBody.find(".questionText").html(question.content);
        this.drawAnswer(quesBody,question,question.answer);
        this.updateQuestionSummary(index,drawFreshly);
        //MathJax.Hub.Queue(["Typeset",MathJax.Hub],$(".test_questions .questionBody").get(0));
        loadTestMJEqns(quesBody.get(0));    
        this.startTime = new Date().getTime();
    },
    drawAnswer:function(quesBody,question,answer){
        var optionsParent = quesBody.find(".choices");
        var optionsDiv = optionsParent.find(".eachOptHolder");
        var numAnsDiv = quesBody.find(".testNumAns");
        quesBody.data("qType",question.type);
        if(question.type == "NUMERIC"){
            optionsParent.addClass("hidden");
            numAnsDiv.removeClass("hidden");
            if(answer){
                numAnsDiv.find(".numericAnsInputBox").val(answer).text("");
            }else{
                numAnsDiv.find(".numericAnsInputBox").val("").text("");
            }
        }else{
          optionsParent.removeClass("hidden");
          numAnsDiv.addClass("hidden").find(".numericAnsInputBox").val("").text("");
          if(question.type == "SCQ") inputType = "radio";
          else if(question.type == "MCQ") inputType = "checkbox";
          for(var i=0;i<question.options.length;i++){
            var curDiv = $($(optionsDiv).get(i));
            var allAlphabets = "abcdefghijklmnopqrstuvwxyz";
            curDiv.find(".optAlphaCount").html("("+allAlphabets.charAt(i)+")");
            curDiv.find(".eachOpt").html($(question.options).get(i));
            try{
                $(curDiv).removeClass("hidden").find("input").get(0).type = inputType;
            }catch(err){ $(curDiv).removeClass("hidden");};
          };
          for(var j=question.options.length;j<=5;j++){
            $(optionsParent.find(".eachOptHolder").get(j)).addClass("hidden");
          }
          $(answer).each(function(){
            try{
                $(optionsParent).find("input").get(this-1).checked = true;
            }catch(err){$(optionsParent).find("input").attr("checked",false);}
          });
          optionsParent.find("img").load(function(){
            if(takeTest.wdthTimeout) clearTimeout(takeTest.wdthTimeout);
            takeTest.wdthTimeout = setTimeout(function(){
                try{adjustTestMathJaxEqnsOptions($(".test_home .test_questions .questionBody"));}catch(err){}
            },1000);
          });
        }
    },
    getLastClassName:function(index){
            var deservedClassName = $(this.currentSubject.questions).get(index).state;
            return deservedClassName?deservedClassName:"";
    },
    updateQuestionSummary:function(index,drawFreshly){
        if(drawFreshly){
            var subject = this.currentSubject;
            var i=0;
            var parentElem = $(".test_home .test_question_summary .q_list");
            $(parentElem).html("");
            for(var i=1;i<=subject.questions.length;i++){
                var newElem = document.createElement("span");
                $(newElem).addClass("each_question").removeClass("hidden mark_for_review save_and_next").addClass(this.getLastClassName(i-1)).text("Q"+i);
                $(parentElem).append(newElem);
            }
            $(parentElem).append("<div class='cleaner_with_divider'>&nbsp;</div>");
        }
        var optionsDiv = $(".test_home .test_questions .questionNumber").text("Q"+(index+1));
        var list = $(".test_home .test_question_summary .q_list").find(".each_question");
        var lastIndex = $(list).index($(".test_home .test_question_summary .q_list .current"));
        $($(list).get(lastIndex)).removeClass("current mark_for_review save_and_next").addClass(this.getLastClassName(lastIndex));
        $($(list).get(index)).addClass("current");
    },
    showOnlyMarkedForReviewQuestions : function(e){
        $('.errorMsg').addClass('hidden');
        if(e.currentTarget.checked == true){
            var list = $(".test_home .test_question_summary .q_list span");
            var all = $(".test_home .test_question_summary .q_list").find(".mark_for_review");
            var first1 = $(all).get(0);
            if(first1){
                $(list).not(".mark_for_review").addClass("hidden");
                var index = $(list).index(first1);
                takeTest.drawQuestion(index);
                takeTest.markedForReviewArray=[];
                $(all).each(function(){
                    takeTest.markedForReviewArray.push($(list).index(this));
                });
            }else{
                e.currentTarget.checked = false;
            }
        }
        else{
            takeTest.markedForReviewArray = null;
            takeTest.updateQuestionSummary(takeTest.currentQuestionIndex,true);
            takeTest.hideMessage();
        }
    },
    showMessage:function(e){
        var testQues = $(".test_home .test_questions");
        testQues.find(".questionNumber").addClass("hidden");
        testQues.find(".refreshQues").addClass("hidden");
        var quesBody = testQues.find(".questionBody");
        quesBody.find(".questionText").addClass("hidden");
        quesBody.find(".choices").addClass("hidden");
        quesBody.find(".testNumAns").addClass("hidden");
        quesBody.find(".message").removeClass("hidden");
        $(".test_home .test_questions .test_btns").addClass("hidden");
        var list = $(".test_home .test_question_summary .q_list").find(".each_question");
        var lastIndex = $(list).index($(".test_home .test_question_summary .q_list .current"));
        $($(list).get(lastIndex)).removeClass("current mark_for_review save_and_next").addClass(this.getLastClassName(lastIndex));
        $(".test_home .test_questions .question_number").text(" ");
        this.messageShown = true;
    },
    hideMessage:function(){
        if(!this.messageShown) return true;
        var testQues = $(".test_home .test_questions");
        testQues.find(".questionNumber").removeClass("hidden");
        testQues.find(".refreshQues").removeClass("hidden");
        var quesBody = testQues.find(".questionBody");
        quesBody.find(".questionText").removeClass("hidden");
        quesBody.find(".q").removeClass("hidden");
        quesBody.find(".choices").removeClass("hidden");
        quesBody.find(".message").addClass("hidden");
        $(".test_home .test_questions .test_btns").removeClass("hidden");
        this.messageShown = false;
    },
    getAnswers:function(){
        var quesBody = $(".test_home .test_questions .questionBody");
        var optionsParent = quesBody.find(".choices");
        var numAnsDiv = quesBody.find(".testNumAns");
        var qType = quesBody.data("qType");
        var answer = [];
        if(qType == "NUMERIC"){
            var val = numAnsDiv.find(".numericAnsInputBox").val();
            if(val) answer.push(val);
        }else{
            optionsParent.find("input:checked").each(function(){
                answer.push((optionsParent.find("input").index($(this)))+1);
            });
        }
        return answer;
    },
    showErrorMsg:function(errorTxt){
        swal({
            html:errorTxt,
        });
    },
    registerFns:function(){
        var parDiv = $(".test_home");
        var btns = parDiv.find(".test_btns");
        btns.on("click",".mark_review_btn",this.markForReview)
            .on("click",".reset_btn",this.resetAttempt)
            .on("click",".skip_btn",this.skipQuestion)
            .on("click",".save_next_btn",this.saveAndNextQuestion);
        parDiv.find(".test_subjects .test_subjects_tabs").on("click","span",this.changeSubject);
        parDiv.on("click",".test_finished_btn",function(){
                $(".test_end_btn").toggleClass("visible hidden");
            })
            .on("click",".test_end_btn",this.endExam)
            .on("pre-refresh",".refreshQues",this.preRefresh)
            .on("post-refresh",".refreshQues",this.postRefresh);
        parDiv.find(".test_question_summary .q_list").on("click",".each_question",this.changeQuestion);
        $(".show_only_marked").on("change","input",this.showOnlyMarkedForReviewQuestions);
        $(".test_subjectsSmall")
        .on("change","#subjectTest",this.changeSubjectDropDown);
    }
};
new function($){
    $(document).on("mousedown",".virNumericKeyboard .VNKeys",function(){
        $(this).addClass("downKey");
    }).on("mouseup",".virNumericKeyboard .VNKeys",function(){
        $(this).removeClass("downKey");
    }).on("click",".virNumericKeyboard .VNKeys",function(){
        var holder = $(this).closest(".virNumericKeyboard");
        var triggerClass = holder.data("inputBoxClass");
        var val=$(this).data("val");
        if(val=="<"){
            var extVal = $(triggerClass).val()||$(triggerClass).text();
            extVal = extVal.toString();
            var cPos = $(triggerClass).getCursorPosition();
            var newVal = extVal.slice(0,cPos-1)+extVal.slice(cPos);
            $(triggerClass).text(newVal).val(newVal);
        }else if(val!=undefined){
            var extVal = $(triggerClass).val()||$(triggerClass).text();
            extVal = extVal.toString();
            if(val=="." && extVal.indexOf(".")>=0){
                return;
            }
            var newVal = extVal+val.toString();
            $(triggerClass).text(newVal).val(newVal);
        }else{
            $(triggerClass).text("").val("");
        }
    });
}(jQuery);
new function($) {
  $.fn.setCursorPosition = function(pos) {
    if ($(this).get(0).setSelectionRange) {
      $(this).get(0).setSelectionRange(pos, pos);
    } else if ($(this).get(0).createTextRange) {
      var range = $(this).get(0).createTextRange();
      range.collapse(true);
      range.moveEnd('character', pos);
      range.moveStart('character', pos);
      range.select();
    }
  }
  $.fn.getCursorPosition = function() {
        var el = $(this).get(0);
        var pos = 0;
        if('selectionStart' in el) {
            pos = el.selectionStart;
        } else if('selection' in document) {
            el.focus();
            var Sel = document.selection.createRange();
            var SelLength = document.selection.createRange().text.length;
            Sel.moveStart('character', -el.value.length);
            pos = Sel.text.length - SelLength;
        }
        return pos;
   }
}(jQuery);
function pushIfAbsent(arr,val){
    try{
        var index = arr.indexOf(val);
        if(index<0){
            return arr.push(val);
        }else{
            return index;
        }
    }catch(err){return -1;}
};
function testFilterQType(value,target,tValue){
    takeTest.currentSubject = $(takeTest.data.boards).get(takeTest.currentSubjectIndex);
    var index = takeTest.currentSubject.typeLocator[tValue];
    if(index){
        takeTest.currentSubject.questions = takeTest.currentSubject.quesByTypes[index].questions;
        $(".test_question_summary").find(".sub_name").text(takeTest.currentSubject.name+" - "+value);
    }else{
        takeTest.currentSubject.questions = takeTest.currentSubject.allQuestions;
        $(".test_question_summary").find(".sub_name").text(takeTest.currentSubject.name);
    }   
    takeTest.drawQuestion(0,true);
};

var showError = function(err, cbFn) {
    $('.errorMsg').removeClass('hidden').html(err);
};
var showMessage = function(message, cbFn) {
    swal(message, "OK", "SUCCESS", cbFn);
};

$(document).ready(function(){
    takeTest.init();
});
