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
	targetType:"",
	targetId:"",
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
		this.targetType = $("#targetType").val();
		this.targetId = $("#targetId").val();
		try{
			this.data = $(".test_head").data("testData");
			this.prepareData();
			this.currentSubject = $(this.data.boards).get(this.currentSubjectIndex);
			this.startTimer();
			this.updateSubjectQType(this.currentSubject);
		}catch(err){
			//console.error(err);
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
				for(i=0;i<ques.length;i++){
					if(ques[i]["answerGiven"] != null){
						ques[i].state = "save_and_next";
						ques[i].answer = ques[i]["answerGiven"];
					}
				}
				boards[bIndex].typeLocator[type] = qType;
				boards[bIndex].typeLocator.length++;
				var m = pushIfAbsent(t.qTypes,type);
				questions = questions.concat(ques);
			}
			boards[bIndex].quesByTypes = boards[bIndex].questions;
			boards[bIndex].questions = boards[bIndex].allQuestions = questions;
			try{t.updateAnswerGiven(questions,bIndex);}catch(err){}
		}
	},
	updateSubjectQType:function(subData){
		//console.log(subData);
	    	if(subData && subData.typeLocator.length>1){
			$(".takeTestFilterQTypes").removeClass("nonner");
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
			$(".takeTestFilterQTypes").addClass("nonner");
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
		this.timeLeftDiv = $("#test_home .test_subjects .test_timer .total_time_left");
		this.tt = setTimeout(function(){
			takeTest.showErrorMsg(takeTest.messages.timeOver);
			endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
		},this.timeLeft*1000);//time left in milisec
		var updateCircleClockTime = (this.data.duration / 36);
		this.tCIntv = setInterval(function(){
			timer_circle.fill(timer_circle.currentAngle+10);
		},updateCircleClockTime);
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
		endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
	},
	changeSubject:function(e){
		$(e.delegateTarget).find(".selected").removeClass("selected");
		$(e.currentTarget).addClass("selected");
		takeTest.currentSubjectIndex = $(e.delegateTarget).find("span").index(e.currentTarget);
		takeTest.currentSubject = $(takeTest.data.boards).get(takeTest.currentSubjectIndex);
		takeTest.currentSubject.questions = takeTest.currentSubject.allQuestions;
		takeTest.updateSubjectQType(takeTest.currentSubject);
		$(".test_question_summary .show_only_marked input").attr("checked",false);
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
		//console.log(params);
		$.post(takeTest.submitAnswerUrl,params,function(data){
			var success = false;
			if(data){
			   if(data.errorCode!=""){
			   	try{
					takeTest.showStatusMessage(data.errorCode,true);
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
	showStatusMessage:function(message,showPopup){
		if(message  == "NULL_RESPONSE"){
			takeTest.onLine = false;
			if(showPopup){
				takeTest.showErrorMsg("Our servers are down,"+"<br/><b>Do not submit test, contact admin</b>");
			}
			return ;
		}
		if(message=="TEST_TIME_OVER"){
			takeTest.showErrorMsg(takeTest.messages.timeOver);
		}
		else if(message == "TEST_ENDED" || message == "TEST_PAUSED" || message == "TEST_PAUSED_RESUME_AGAIN"){
			if(message == "TEST_ENDED"){
				takeTest.showErrorMsg("Admin ended your test.Please contact admin");
			}
			else if(message == "TEST_PAUSED"){
				takeTest.showErrorMsg("Admin has paused your test.Please contact admin");
			}
			else if(message == "TEST_PAUSED_RESUME_AGAIN"){
				takeTest.showErrorMsg("Please resume your test");
			}
			try{takeTest.stopTimers();}catch(err){console.log("dsgvdf");console.log(err)}
			postTestUi();
			goToInstTestPage("END_TEST",takeTest.testId,null,null,null,takeTest.targetType,takeTest.targetId);
			return ;
		}
		else if(message == "ATTEMPT_NOT_FOUND"){
			takeTest.showErrorMsg("Admin has resetted your test.Please contact admin");
			try{takeTest.stopTimers();}catch(err){console.log("dsgvdf");console.log(err)}
			postTestUi();
			goToInstPreTestPage("",takeTest.testId,null,takeTest.targetType,takeTest.targetId);
			return;
		}
		// endCurrentExam(takeTest.testId);
		endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
	},
	onLine:true,
	internetConnectionCheck:function(){
		$.ajax({
			url:"/UIComTest/ping",
			cache:false,
			timeout:10000,
			type:"POST"
		}).done(function(data,textStatus,xhr){
			if(data.errorCode != ""){
				takeTest.showStatusMessage(data.errorCode,false);
			}
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
			testHead.find(".onlineIcon").removeClass("nonner").siblings().addClass("nonner");
			testHead.find(".testOfflineMsg").addClass("nonner");
		   }else{
			testHead.find(".offlineIcon").removeClass("nonner").siblings().addClass("nonner");
			testHead.find(".testOfflineMsg").removeClass("nonner");
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
		var question = $(takeTest.currentSubject.questions).get(takeTest.currentQuestionIndex);
		//takeTest.submitAnswerToServer("SKIP",question);
		takeTest.nextQuestion(e);
	},
	reset:function(e){
		$("#test_home .questionBody").find("input:checked").each(function(){
			this.checked = false;
		});
		$("#test_home").find(".questionBody").find(".numericAnsInputBox").val("").text("");
	},
	resetAttempt:function(e){
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
		var index = $(e.delegateTarget).find(".each_question").index(e.currentTarget);
		takeTest.drawQuestion(index);
	},
	drawQuestion:function(index,drawFreshly){
		this.reset();
		this.hideMessage();
		this.currentQuestionIndex = index;
		var question = $(this.currentSubject.questions).get(index);
		var quesBody = $("#test_home .test_questions .questionBody");
		if(question.type == "PARA"){
			quesBody.find(".paraText").html(question.paraContent);	
		}
		quesBody.find(".questionText").html(question.content);
		$(".test_questions .questionType").html(question.type);
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
			optionsParent.addClass("nonner");
			numAnsDiv.removeClass("nonner");
		  	if(answer){
				numAnsDiv.find(".numericAnsInputBox").val(answer).text("");
			}else{
				numAnsDiv.find(".numericAnsInputBox").val("").text("");
			}
		}else{
		  optionsParent.removeClass("nonner");
		  numAnsDiv.addClass("nonner").find(".numericAnsInputBox").val("").text("");
		  if(question.type == "SCQ") inputType = "radio";
		  else if(question.type == "MCQ" || question.type == "PARA") inputType = "checkbox";
		  for(var i=0;i<question.options.length;i++){
			var curDiv = $($(optionsDiv).get(i));
			curDiv.find(".optAlphaCount").html("("+allAlphabets.charAt(i)+")");
			curDiv.find(".eachOpt").html($(question.options).get(i));
			try{
				$(curDiv).removeClass("nonner").find("input").get(0).type = inputType;
			}catch(err){ $(curDiv).removeClass("nonner");};
		  };
		  for(var j=question.options.length;j<=5;j++){
			$(optionsParent.find(".eachOptHolder").get(j)).addClass("nonner");
		  }
		  $(answer).each(function(){
			try{
				$(optionsParent).find("input").get(this-1).checked = true;
			}catch(err){$(optionsParent).find("input").attr("checked",false);}
		  });
		  optionsParent.find("img").load(function(){
			if(takeTest.wdthTimeout) clearTimeout(takeTest.wdthTimeout);
			takeTest.wdthTimeout = setTimeout(function(){
				try{adjustTestMathJaxEqnsOptions($("#test_home .test_questions .questionBody"));}catch(err){}
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
			var parentElem = $("#test_home .test_question_summary .q_list");
			$(parentElem).html("");
			for(var i=1;i<=subject.questions.length;i++){
				var newElem = document.createElement("span");
				$(newElem).addClass("each_question").removeClass("nonner mark_for_review save_and_next").addClass(this.getLastClassName(i-1)).text("Q"+i);
				$(parentElem).append(newElem);
			}
			$(parentElem).append("<div class='cleaner_with_divider'>&nbsp;</div>");
		}
		var optionsDiv = $("#test_home .test_questions .questionNumber").text("Q"+(index+1));
		var list = $("#test_home .test_question_summary .q_list").find(".each_question");
		var lastIndex = $(list).index($("#test_home .test_question_summary .q_list .current"));
		$($(list).get(lastIndex)).removeClass("current mark_for_review save_and_next").addClass(this.getLastClassName(lastIndex));
		$($(list).get(index)).addClass("current");
	},
	showOnlyMarkedForReviewQuestions : function(e){
		if(e.currentTarget.checked == true){
			var list = $("#test_home .test_question_summary .q_list span");
			var all = $("#test_home .test_question_summary .q_list").find(".mark_for_review");
			var first1 = $(all).get(0);
			if(first1){
				$(list).not(".mark_for_review").addClass("nonner");
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
		var testQues = $("#test_home .test_questions");
		testQues.find(".questionNumber").addClass("nonner");
		testQues.find(".refreshQues").addClass("nonner");
		var quesBody = testQues.find(".questionBody");
		quesBody.find(".questionText").addClass("nonner");
		quesBody.find(".choices").addClass("nonner");
		quesBody.find(".testNumAns").addClass("nonner");
		quesBody.find(".message").removeClass("nonner");
		$("#test_home .test_questions .test_btns").addClass("nonner");
		var list = $("#test_home .test_question_summary .q_list").find(".each_question");
		var lastIndex = $(list).index($("#test_home .test_question_summary .q_list .current"));
		$($(list).get(lastIndex)).removeClass("current mark_for_review save_and_next").addClass(this.getLastClassName(lastIndex));
		$("#test_home .test_questions .question_number").text(" ");
		this.messageShown = true;
	},
	hideMessage:function(){
		if(!this.messageShown) return true;
		var testQues = $("#test_home .test_questions");
		testQues.find(".questionNumber").removeClass("nonner");
		testQues.find(".refreshQues").removeClass("nonner");
		var quesBody = testQues.find(".questionBody");
		quesBody.find(".questionText").removeClass("nonner");
		quesBody.find(".q").removeClass("nonner");
		quesBody.find(".choices").removeClass("nonner");
		quesBody.find(".message").addClass("nonner");
		$("#test_home .test_questions .test_btns").removeClass("nonner");
		this.messageShown = false;
	},
	getAnswers:function(){
		var quesBody = $("#test_home .test_questions .questionBody");
		var optionsParent = quesBody.find(".choices");
		var numAnsDiv = quesBody.find(".testNumAns");
		var qType = quesBody.data("qType");
		var answer = [];
		if(qType == "NUMERIC"){
			var val = numAnsDiv.find(".numericAnsInputBox").val();
			if(val)	answer.push(val);
		}else{
			optionsParent.find("input:checked").each(function(){
				answer.push((optionsParent.find("input").index($(this)))+1);
			});
		}
		return answer;
	},
	showErrorMsg:function(errorTxt){
		showTimedError(errorTxt);
	},
	registerFns:function(){
		var parDiv = $("#test_home");
		var btns = parDiv.find(".test_btns");
		btns.on("click",".mark_review_btn",this.markForReview)
			.on("click",".reset_btn",this.resetAttempt)
			.on("click",".skip_btn",this.skipQuestion)
			.on("click",".save_next_btn",this.saveAndNextQuestion);
		parDiv.find(".test_subjects .test_subjects_tabs").on("click","span",this.changeSubject);
		parDiv.on("click",".test_end_btn",this.endExam)
			.on("pre-refresh",".refreshQues",this.preRefresh)
			.on("post-refresh",".refreshQues",this.postRefresh);
		parDiv.find(".test_question_summary .q_list").on("click",".each_question",this.changeQuestion);
		parDiv.find(".test_question_summary .show_only_marked").on("change","input",this.showOnlyMarkedForReviewQuestions);
	}
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
}
$(document).ready(function(){
	takeTest.init();
});
