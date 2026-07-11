var takeTest = {
	data:"",
	boards:"",
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
	quotaExpired:false,
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
			this.testBoardsLength = this.data.boards.length;
			this.prepareData();
			this.currentSubject = $(this.data.boards).get(this.currentSubjectIndex);
			if(this.data.enableSectionLocking === true){
                this.startTimerForSectionLocking();
                this.disableTestTabs();
            }
            else{
                this.startTimer();
            }
			this.drawQuestion(0,true);
			var height = screen.height - 240;
            //For subjective test,screen is small,so we reduce the height accordingly
            if(this.data.subjectiveTest){
                height = "60vh";
            }
			$(".mainContent").css("height",height);
			$(".questionsBlock").css("height",height);
			$(".questionsStateMode").css("height",height*0.3);
			$(".test_question_summary").css("height",height*0.7);

		}catch(err){
			console.error(err);
			var url = testFns.urls["getQuestionsJson"];
			vReq.get(url,{"id":testId,"qTypeDistribution":true},function(data){
				var t = takeTest;
				t.data = data;
				t.prepareData();
				t.currentSubject = $(data.boards).get(t.currentSubjectIndex);
				t.startTimer();
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
		var boardsTypeQuestion = [];
		var start = 0;
		for(var bIndex = 0;bIndex<boards.length;bIndex++){
			var section = 0;
			var questionTypes = boards[bIndex].questions;
			var questions = [];
			for(qType in questionTypes){
				var ques = questionTypes[qType].questions;
				if(!ques || ques.length<1) continue;
				section = section + 1;
				var type = questionTypes[qType].type;
				var totalParagraphs = questionTypes[qType].totalParagraphs;
				for(i=0;i<ques.length;i++){
					if(ques[i]["answerGiven"] != null){
						ques[i].state = "save_and_next";
						ques[i].answer = ques[i]["answerGiven"];
						console.log(ques[i].answer);
					}
				}
				boardsTypeQuestion[start] = ques;
				boardsTypeQuestion[start].name = boards[bIndex].name;
				boardsTypeQuestion[start].aliasName = boards[bIndex].name;
				boardsTypeQuestion[start].type = type;
				boardsTypeQuestion[start].totalParagraphs = totalParagraphs;
				boardsTypeQuestion[start].section = "Sec"+section;
				boardsTypeQuestion[start].tabIndex = boards[bIndex].id;
				start = start + 1;
				questions = questions.concat(ques);
			}
			try{
				t.updateAnswerGiven(questions,bIndex);
			}
			catch(err){
				console.log(err);
				showError("something went wrong");
				return;
			}
		}
		this.data.boards = boardsTypeQuestion;
        this.jumbleQuestions(this.data.boards);
	},
    jumbleQuestions:function(data){
        for(i = 0; i<data.length;i++){
            var boardsWise = data[i];
            this.shuffle(boardsWise);
        }
    },
    shuffle:function(array){
        array.sort(function(){
            return Math.random() - 0.5;
        });
    },
	startTimer:function(){
	try{
		this.data.duration = parseInt(this.data.duration,10);
		if(!this.data.duration || this.data.duration <= 0) {console.log("Time left is zero");return false;}
		// try{
		// 	this.timeLeft = testDownTime.updateTimer(this.data.duration);
		// }catch(err){
			this.timeLeft = this.data.duration/1000;
		// }
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
		//To generate random ping intervals
		// var random = Math.floor(Math.random() * (60 - 30 + 1)) + 30;
		var pingTime = 5;
		this.internet = setInterval(function(){
			takeTest.internetConnectionCheck();
		},pingTime*1000);
	}catch(err){}
	},
	//Implement function to start timers for section locking.
    startTimerForSectionLocking:function(){
        this.sectionTimeInterval = 0;
        this.data.totalTestTime = this.data.totalTestTime/1000;
        this.data.duration = this.data.duration/1000;
        this.data.duration = parseInt(this.data.duration,10);
        this.leftOver = this.data.duration;
        this.data.totalTestTime = parseInt(this.data.totalTestTime,10);
        this.sectionTimeInterval = this.data.totalTestTime/takeTest.testBoardsLength;
        this.timeLeft = this.data.duration;
        this.tt = setTimeout(function(){
            takeTest.showErrorMsg(takeTest.messages.timeOver);
            endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
        },this.timeLeft*1000);
        this.timeLeftDivSection = $("#test_home .test_subjects .section_timer .section_time_left");
        this.timeLeftDiv = $("#test_home .test_subjects .test_timer .total_time_left");
        this.timersForSection = {};
        for(i=takeTest.testBoardsLength,j=0;i>0,j<takeTest.testBoardsLength;i--,j++){
            takeTest.timersForSection[j] = this.leftOver - (this.sectionTimeInterval) * (i-1);
            if(takeTest.timersForSection[j] < 0){
                takeTest.timersForSection[j] = 0;
            }
            this.leftOver = this.leftOver - takeTest.timersForSection[j];
            // console.log(takeTest.timersForSection[j]);
        }
        takeTest.startTimerForEachSection(0);
        takeTest.timeLeft = parseInt(takeTest.timeLeft,10);
        this.tIntv = setInterval(function(){
            // console.log("setting Interval "+takeTest.timeLeft);
            takeTest.timeLeft--;
            var hrs = Math.floor(takeTest.timeLeft/3600);
            var mins = ("0"+Math.floor((takeTest.timeLeft-hrs*3600)/60)).slice(-2);
            var secs = ("0"+(takeTest.timeLeft - (mins*60 + hrs*3600))).slice(-2);
            $(takeTest.timeLeftDiv).text(hrs+":"+mins+":"+secs);
            if(takeTest.timeLeft < 60){
                $(takeTest.timeLeftDiv).addClass("warning");
            }
            else{
                $(takeTest.timeLeftDiv).removeClass("warning");
            }
        },1000);
        //To generate random ping intervals
        // var random = Math.floor(Math.random() * (60 - 30 + 1)) + 30;
        var pingTime = 5;
        this.internet = setInterval(function(){
            takeTest.internetConnectionCheck();
        },pingTime*1000);
    },

    startTimerForEachSection:function(i){
        this.setTimerForSection = setInterval(function(){
            if(i >= takeTest.testBoardsLength){
                return;
            }
            takeTest.timersForSection[i] = parseInt(takeTest.timersForSection[i],10);
            // console.log("i value is "+takeTest.timersForSection[i]);
            if(takeTest.timersForSection[i] > 0){
                takeTest.timersForSection[i]--;
            }
            var hrs = Math.floor(takeTest.timersForSection[i]/3600);
            var mins = ("0"+Math.floor((takeTest.timersForSection[i]-hrs*3600)/60)).slice(-2);
            var secs = ("0"+(takeTest.timersForSection[i] - (mins*60 + hrs*3600))).slice(-2);
            $(takeTest.timeLeftDivSection).text(hrs+":"+mins+":"+secs);
            if(takeTest.timersForSection[i] < 60){
                $(takeTest.timeLeftDivSection).addClass("warning");
            }
            else{
                $(takeTest.timeLeftDivSection).removeClass("warning");
            }
            if(takeTest.timersForSection[i] <= 0){
                clearInterval(this.setTimerForSection);
                // console.log("Clearing interval"+takeTest.timersForSection[i]);
                // console.log("clearing interval of"+i);

                var currentSubject = $(".test_subjects_tabs").find(".selected");
                var currentSubjectIndex = takeTest.currentSubject.tabIndex;
                if(currentSubject.next('.testBoard').length>0){
                    currentSubject.removeClass("selected");
                    var testBoard = $(".testBoard");
                    for(j=currentSubject.index(0);j<testBoard.length;j++){
                        var nextBoard = testBoard[j];
                        if($(nextBoard).data("subjectId") != currentSubjectIndex){
                            $(nextBoard).removeClass("disabled").addClass("selected");
                            break;
                        }
                    }
                    takeTest.changeSubject();
                    takeTest.disableTestTabs();
                    i++;
                    // takeTest.startTimerForEachSection(i);
                }
            }
        },1000);
    },
    //Implement function to disableTestTabs for section locking.
    disableTestTabs:function(){
        // console.log("Inside disableTestTabs");
        var currentSubjectIndex = takeTest.currentSubject.tabIndex;
        // console.log(currentSubjectIndex);
        $(".testBoard").each(function(){
            if($(this).data("subjectId") != currentSubjectIndex){
                $(this).addClass("disabled");
            }
            else{
                $(this).removeClass("disabled");
            }
        })
    },
	stopTimers:function(){
		try{
		if(this.tIntv) clearInterval(this.tIntv);
		if(this.tCIntv) clearInterval(this.tCIntv);
		if(this.tt) clearInterval(this.tt);
		if(this.internet) clearInterval(this.internet);
		if(takeTest.data.enableSectionLocking === true){
            if(this.setTimerForSection){
                clearInterval(this.setTimerForSection);
            }
        }
		}catch(err){}
	},
	pauseIntvTimer:function(){
		if(this.tIntv){
			clearInterval(this.tIntv);
			delete this.tIntv;
		}
		if(this.tt){
			clearInterval(this.tt);
			delete this.tt;
		}
		if(takeTest.data.enableSectionLocking === true){
            // console.log("Inside enableSectionLocking");
            if(this.setTimerForSection){
                // console.log("Inside setTimerForSection");
                clearInterval(this.setTimerForSection);
                delete this.setTimerForSection;
            }
        }
	},
	resumeIntvTimer:function(){
		if(!this.tIntv){
			this.tIntv = setInterval(function(){
				takeTest.timeLeft--;
				var hrs = Math.floor(takeTest.timeLeft/3600);
				var mins = ("0"+Math.floor((takeTest.timeLeft-hrs*3600)/60)).slice(-2);
				var secs = ("0"+(takeTest.timeLeft - (mins*60 + hrs*3600))).slice(-2);
				$(takeTest.timeLeftDiv).text(hrs+":"+mins+":"+secs);
			},1000);
		}
		if(!this.tt){
			this.tt = setTimeout(function(){
				takeTest.showErrorMsg(takeTest.messages.timeOver);
				// endCurrentExam(takeTest.testId);
				endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
			},this.timeLeft*1000);
		}
		if(takeTest.data.enableSectionLocking === true){
            if(!this.setTimerForSection){
                $(".testBoard").removeClass("selected");
                $(".testBoard").each(function(i){
                    if(i==0){
                        $(this).addClass("selected");
                        return false;
                    }
                });

                takeTest.startTimerForEachSection(0);
            }
        }
	},
	endExam:function(e){
		var boards = takeTest.data.boards;
		var eachSectionQuestionStatus;
		var TestQuestionAnalyticsTableHolder = $("#test_home").find(".TestQuestionSummary").find("tbody");
		TestQuestionAnalyticsTableHolder.html("");
		for (var i = 0; i < boards.length; i++) {
			eachSectionQuestionStatus = takeTest.getQuestionSetState(i);
			TestQuestionAnalyticsTableHolder.append("<tr>"+
														"<td>"+eachSectionQuestionStatus.aliasName +" "+ eachSectionQuestionStatus.section+"</td>"+
														"<td>"+eachSectionQuestionStatus.numOfQuestion+"</td>"+
														"<td>"+eachSectionQuestionStatus.save_and_next+"</td>"+
														"<td>"+eachSectionQuestionStatus.visited+"</td>"+
														"<td>"+eachSectionQuestionStatus.mark_for_review+"</td>"+
														"<td>"+eachSectionQuestionStatus.saved_and_marked+"</td>"+
														"<td>"+eachSectionQuestionStatus.undefined+"</td>"+
													"</tr>");
		}
		var status = takeTest.getQuestionSetState(0);
		var popup = showVPopup();
		popup.html($("#test_home").find(".TestQuestionSummary").parent().html());
		$(".TestQuestionSummary").on("click",".revertBack",takeTest.closeTablePopup);
		$(".TestQuestionSummary").on("click",".submitTestFinal",takeTest.submitTheTest);
		// showVYesNoBox("Are you sure to submit ?", null, function(state) {
  //           if (state) {
  //               if(!takeTest.ifInternetAvailable()) return;
		// 		endCurrentExam(takeTest.testId);
  //           }
  //       });
	},
	getQuestionSetState:function(index){
		var board = this.data.boards[index];
		var questionListStatusOutput = {
			numOfQuestion : board.length,
			name : board.name,
			aliasName : board.aliasName,
			section : board.section,
			visited : 0,
			undefined : 0,
			save_and_next : 0,
			mark_for_review : 0,
			saved_and_marked : 0
		};
		for(var bIndex = 0;bIndex<board.length;bIndex++){
			questionListStatusOutput[board[bIndex].state]++;
		}
		console.log(questionListStatusOutput);
		return questionListStatusOutput;
	},
	changeSubjectClick:function(e){
		$(e.delegateTarget).find(".selected").removeClass("selected");
		$(e.currentTarget).addClass("selected");
		takeTest.changeSubject();
	},
	showSectionWiseQuestionStatus:function(){
		var questionSectionSetStatus = takeTest.getQuestionSetState($(this).data("section"));
		// console.log(questionSectionSetStatus);
	},
	changeSubject:function(){
		takeTest.currentSubjectIndex = $(".test_subjects_tabs").find(".selected").index(0);
		takeTest.currentSubject = $(takeTest.data.boards).get(takeTest.currentSubjectIndex);
		var totalParagraphs = takeTest.currentSubject.totalParagraphs;
		$(".test_question_summary .show_only_marked input").attr("checked",false);
		takeTest.drawQuestion(0,true, totalParagraphs);
	},
	showTypeQuestion:function(e){
		$(e.delegateTarget).find(".selected").removeClass("selected");
		$(e.currentTarget).addClass("selected");
		var typeId = $(e.currentTarget).data("typeId");
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
		var question = $(takeTest.currentSubject).get(takeTest.currentQuestionIndex);
		takeTest.drawQuestion(takeTest.currentQuestionIndex,true);
	},
	saveAndNextQuestion:function(e){
		if(!takeTest.ifInternetAvailable()) return;
		var question = $(takeTest.currentSubject).get(takeTest.currentQuestionIndex);
		var index = takeTest.currentQuestionIndex;
		question.answer = takeTest.getAnswers();
		if(question.answer.length<=0){
			takeTest.nextQuestion(e);
			return true;
		}
		var oldState = question.state;
		if($(e.target).hasClass("mark_review_btn")){
			question.state = "saved_and_marked";
		}
		else{
			question.state = "save_and_next";
		}
		takeTest.submitAnswerToServer("COMPLETE",question,function(success,data){
			// console.log("inside saveAndNextQuestion ");
			if(!success){
				question.state = oldState;
				question.answer = undefined;
				if(quotaExpired === true){
					takeTest.showStatusMessage("MAX_LIIMIT_REACHED",true);
					// console.log("question.state : "+question.state);
				}
			}
			takeTest.nextQuestion(e);
		});

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
			// console.log("Inside submitAnswerToServer");
			// console.log(" quotaExpired :  "+data.result.quotaExpired);
			if(data){
				if(data.errorCode!=""){
					try{
						takeTest.showStatusMessage(data.errorCode,true);
					}
					catch(err){
					}
				}
				else if(data.result.quotaExpired === true){
					success = false;
					quotaExpired=true;
					// console.log("inside else of submitAnswerToServer");
				}
				else{
					success = true;
                    if(question.type == "SUBJECTIVE"){
                        question.answer = data.result.userAnswer;
                    }
				}
			}
			if(cbFn){
				try{ cbFn(success,data);}catch(err){}
			}
		});
	},
	onLine:true,
	showStatusMessage:function(message,showPopup){
		// console.log("quotaExpired");
		// console.log("message : "+message);
		// console.log("showPopup : "+showPopup);
		if(message  == "NULL_RESPONSE"){
			takeTest.pauseIntvTimer();
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
				takeTest.showErrorMsg("Test has ended");
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
        else if(message == "STORAGE_EXCEPTION"){
            if(showPopup){
                showError("Something went wrong while uploading answer");
                return;
            }
        }
        else if(message == "MAX_LIIMIT_REACHED"){
        	// console.log("MAX_LIIMIT_REACHED");
        	if(showPopup){
        		// console.log("inside show");
                showError("Reached Maximum! Please clear response of already attempted questions in Same Section to answer");
                // var question = $(takeTest.currentSubject).get(takeTest.currentQuestionIndex);
                // question.state=visited;
                return;
            }
        }
		// endCurrentExam(takeTest.testId);
		endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
	},
	internetConnectionCheck:function(){
		$.ajax({
			url:"/UIComTest/ping",
			cache:false,
			data:{testId:takeTest.testId,userId:USERID,timeLeft:takeTest.timeLeft*1000},
			timeout:10000,
			type:"POST"
		}).done(function(data,textStatus,xhr){
			if(data.errorCode != ""){
				takeTest.showStatusMessage(data.errorCode,false);
			}
			else{
				takeTest.resumeIntvTimer();
				takeTest.onLine = true;
			}
			onResponse();
			xhr.processed = true;
		}).fail(function(xhr,textStatus){
			takeTest.pauseIntvTimer();
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
		var firstSubject = $(".test_subjects_tabs .testBoard").eq(0);
		var question = $(takeTest.currentSubject).get(takeTest.currentQuestionIndex);
		// console.log("question.state in nextQuestion : "+question.state);

		if(this.markedForReviewArray){
			nextIndex = this.markedForReviewArray[this.markedForReviewArray.indexOf(this.currentQuestionIndex)+1];
		}
		if(nextIndex < takeTest.currentSubject.length && nextIndex >= 0){
			this.drawQuestion(nextIndex);
		}
		else{
            if(takeTest.data.enableSectionLocking != true ){
                var currentSubject = $(".test_subjects_tabs").find(".selected");
                currentSubject.removeClass("selected");
                if(currentSubject.next('.testBoard').length>0){
                    currentSubject.next().addClass("selected");
                    takeTest.changeSubject();
                }
                else{
                    showMessage("You Reached end of the test! Please Review your test and Submit");
                    currentSubject = firstSubject;
                    currentSubject.addClass("selected");
                    takeTest.changeSubject();
                }
            }
            else{
                var currentSubjectIndex = takeTest.currentSubject.tabIndex;
                var currentSubject = $(".test_subjects_tabs").find(".selected");
                currentSubject.removeClass("selected");
                if(currentSubject.next().data("subjectId") == currentSubjectIndex){
                    currentSubject.next().addClass("selected");
                    takeTest.changeSubject();
                }
                else{
                    currentSubject.addClass("selected");
                    showMessage("You Reached end of the section!");
                }
            }
        }
        // console.log("question.state in nextQuestion1 : "+question.state);
	},
	markForReview:function(e){
		var question = $(takeTest.currentSubject).get(takeTest.currentQuestionIndex);
		question.answer = takeTest.getAnswers();
		var oldState = question.state;
		if(question.answer.length > 0){
			takeTest.saveAndNextQuestion(e);
			return;
		}
		else{
			question.state = "mark_for_review";
		}
		//takeTest.submitAnswerToServer("REVIEW",question);
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
		var question = $(takeTest.currentSubject).get(index);
		if(!takeTest.resetAttemptUrl || !question) return;
		takeTest.reset();
		if(!takeTest.ifInternetAvailable()) return;
		var params = {
			"type":question.type,
			"qId":question.id,
			"testId":takeTest.testId,
			"entityId":takeTest.testId,
			"entityType":"TEST"
		};
		$.get(takeTest.resetAttemptUrl,params,function(data){
			if(data && data.errorCode=="" && data.result.success){
                if(question.type == "SUBJECTIVE"){
                    var subjTextAns = $("#test_home .questionBody").find(".subjTextAns");
                    vRTE.putRTEContent(subjTextAns.find(".RTEArea"),"");
                }
			}else if(question.answer && question.state == "save_and_next"){
				takeTest.showErrorMsg("Reset Question failed, due to some un-expected error!");
			}
			$(question).removeProp("answer");
			$(question).removeProp("state");
			$(question).prop("state","visited");
			takeTest.updateQuestionSummary(index);
		});
	},
	changeQuestion:function(e){
		var index = $(e.delegateTarget).find(".each_question").index(e.currentTarget);
		takeTest.drawQuestion(index);
	},
	drawQuestionInstr: function(question,questionBoard,totalParagraphs){
        // console.log("Inside drawQuestionInstr");
		var quesBody = $("#test_home .test_questions .questionBody");
		quesBody.find(".changingInstructionList").children().addClass("nonner");
		quesBody.find(".changingInstructionList").children().removeClass("visible");
        if(question.type == "NUMERIC"){
            quesBody.find(".markingScheme").find(".skippedText").html(" If the question is unanswered");
        }
        else{
            quesBody.find(".markingScheme").find(".skippedText").html(" If none of the options is chosen.(i.e the question is unanswered)");
        }
		if(question.type == "NUMERIC"){
			var numericInstr = quesBody.find(".sectionWiseInstruction").find(".numericInstr");
			numericInstr.addClass("visible");
			numericInstr.find(".questionsCount").html(questionBoard.length);
			quesBody.find(".markingScheme").find(".correctText").html(" If the correct numerical value is typed in the provided space");
			numericInstr.find(".maxQuestionsToBeAttemptedForSection").html(question.maxQuestionsToBeAttemptedForSection);
		}
		else if(question.type == "MCQ" || question.type == "MATRIX"){
			var mcqInstr = quesBody.find(".sectionWiseInstruction").find(".mcqInstr");
            mcqInstr.addClass("visible");
            takeTest.updateInstr(question.type,mcqInstr,quesBody);
			mcqInstr.find(".questionsCount").html(questionBoard.length);
			mcqInstr.find(".optionCount").html(question.options.length);
			mcqInstr.find(".maxQuestionsToBeAttemptedForSection").html(question.maxQuestionsToBeAttemptedForSection);
			// quesBody.find(".markingScheme").find(".correctText").html(correctInstrText);
		}
		else if(question.type == "PARA"){
			var paraInstr = quesBody.find(".sectionWiseInstruction").find(".paraInstr");
			paraInstr.addClass("visible");
            takeTest.updateInstr(question.type,paraInstr,quesBody);
			paraInstr.find(".paraCount").html(totalParagraphs);
			paraInstr.find(".paraQuestionCount").html(question.paraQuestionsCount);
			paraInstr.find(".optionCount").html(question.options.length);
			paraInstr.find(".maxQuestionsToBeAttemptedForSection").html(question.maxQuestionsToBeAttemptedForSection);
			// quesBody.find(".markingScheme").find(".correctText").html(correctInstrText);
		}
        else if(question.type == "SUBJECTIVE"){
            var subjInstr = quesBody.find(".sectionWiseInstruction").find(".subjInstr");
            subjInstr.addClass("visible");
            takeTest.updateInstr(question.type,subjInstr,quesBody);
            subjInstr.find(".questionsCount").html(questionBoard.length);
            subjInstr.find(".maxQuestionsToBeAttemptedForSection").html(question.maxQuestionsToBeAttemptedForSection);
        }
		else{
			var scqInstr = quesBody.find(".sectionWiseInstruction").find(".scqInstr");
			scqInstr.addClass("visible");
			scqInstr.find(".questionsCount").html(questionBoard.length);
			scqInstr.find(".optionCount").html(question.options.length);
			scqInstr.find(".maxQuestionsToBeAttemptedForSection").html(question.maxQuestionsToBeAttemptedForSection);
			quesBody.find(".markingScheme").find(".correctText").html(" If only the alphabet corresponding to the correct option is selected");
		}
		quesBody.find(".markingScheme").find(".correctMarks").html("<span class='marksDiv'>+"+"<strong>"+question.marks.positive+"</strong></span>");
        if(takeTest.data.partialMarksQTypes !=null && takeTest.data.partialMarksQTypes.length >0 && takeTest.data.partialMarksQTypes.includes(question.type) && takeTest.data.enablePartialMarks === true){
            // console.log("Partial marks"+takeTest.data.enablePartialMarks);
            // console.log("partialMarksQTypes"+takeTest.data.partialMarksQTypes);
            takeTest.createPartialMarkInstructions(question,question.options.length);
            quesBody.find(".partialMarksList").removeClass("nonner");
        }
        else{
            quesBody.find(".partialMarksList").addClass("nonner");
        }
		if(question.marks.negative > 0){
			quesBody.find(".markingScheme").find(".inCorrectMarksList").removeClass("nonner");
			quesBody.find(".markingScheme").find(".inCorrectMarks").html("<span class='marksDiv'>-"+"<strong>"+question.marks.negative+"</strong></span>");
		}
		else{
			quesBody.find(".markingScheme").find(".inCorrectMarksList").addClass("nonner");
		}
	},
    updateInstr:function(qType,instr,quesBody){
        // console.log("Inside updateInstr "+qType);
        var correctInstrText = "";
        if(takeTest.data.oneOrMoreMarksQTypes !=null && takeTest.data.oneOrMoreMarksQTypes.includes(qType)){
            // console.log("Inside if of updateInstr"+takeTest.data.oneOrMoreMarksQTypes);
            instr.find(".qTypeText").html("ONE OR MORE THAN ONE");
            instr.find(".qTypeAllText").html("all");
            correctInstrText = " If only the alphabets corresponding to all the correct option(s) is(are) selected";
        }
        else{
            // console.log("Inside else of updateInstr"+takeTest.data.oneOrMoreMarksQTypes);
            instr.find(".qTypeText").html(",ONLY ONE");
            instr.find(".qTypeAllText").html("");
            correctInstrText = " If only the alphabet corresponding to the correct option is selected";
        }
        quesBody.find(".markingScheme").find(".correctText").html(correctInstrText);
    },
    createPartialMarkInstructions:function(question,options){
        var partialMark = 0;
        var partialMarksListDiv = "<div class='partialMarksInstrDiv'>";
        var countOptions = options;
        for(count=1;count<options;count++){
            partialMark = Math.round(parseFloat((question.marks.positive/options) * (options - count)) * 100)/100;
            partialMarksListDiv += "<div><span class='marksTitle'>Partial Marks</span> :"+"<span class='marksDiv'>+"+"<strong>"+partialMark+"</strong></span>"+"<span class='partialMarksText'> &nbsp;If "+countOptions+ (countOptions === options ? "":" or more ")+" options are correct but ONLY "+(options-count)+" are chosen</span></div>";
            countOptions--;
        }
        partialMarksListDiv += "</div>";
        $("#test_home .test_questions .questionBody").find(".markingScheme").find(".partialMarksList").html(partialMarksListDiv);
    },
	drawQuestion:function(index,drawFreshly, totalParagraphs){
		this.reset();
		this.currentQuestionIndex = index;
		var question = $(this.currentSubject).get(index);
		var questionBoard = $(this.currentSubject);
		var quesBody = $("#test_home .test_questions .questionBody");
		var oldState = question.state;
		if(oldState == undefined){
			question.state = "visited";
		}
		quesBody.find(".questionText").html(question.content);

		takeTest.drawQuestionInstr(question,questionBoard,totalParagraphs);
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
        var subjAnsDiv = quesBody.find(".subjTextAns");
		quesBody.data("qType",question.type);
		if(question.type == "NUMERIC"){
			optionsParent.addClass("nonner");
            subjAnsDiv.addClass("nonner");
			numAnsDiv.removeClass("nonner");
		  	if(answer){
				numAnsDiv.find(".numericAnsInputBox").val(answer).text("");
			}else{
				numAnsDiv.find(".numericAnsInputBox").val("").text("");
			}
		}
        else if(question.type == "SUBJECTIVE"){
            optionsParent.addClass("nonner");
            numAnsDiv.addClass("nonner");
            subjAnsDiv.removeClass("nonner");
            assignRTEs(subjAnsDiv.find(".instSubjRTEHolder"));
            if(answer){
                console.log(answer);
                vRTE.putRTEContent(subjAnsDiv.find(".RTEArea"),answer[0]);
            }
            else{
                console.log("Inside else");
                vRTE.putRTEContent(subjAnsDiv.find(".RTEArea"),"");
            }
        }
        else{
		  optionsParent.removeClass("nonner");
          subjAnsDiv.addClass("nonner");
		  numAnsDiv.addClass("nonner").find(".numericAnsInputBox").val("").text("");
		  if(question.type == "SCQ") inputType = "radio";
          else if(takeTest.data.oneOrMoreMarksQTypes !=null && takeTest.data.oneOrMoreMarksQTypes.includes(question.type)){
            inputType = "checkbox";
          }
          else if(question.type == "NUMERIC"){
          }
          else{
            inputType = "radio";
          }
		  // else if(question.type == "MCQ" || question.type == "PARA" || question.type == "MATRIX") inputType = "checkbox";
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
			var deservedClassName = $(this.currentSubject).get(index).state;
			return deservedClassName?deservedClassName:"";
	},
	updateQuestionSummary:function(index,drawFreshly){
		if(drawFreshly){
			var subject = this.currentSubject;
			var i=0;
			var parentElem = $("#test_home .test_question_summary .q_list");
			$(parentElem).html("");
			$(".sectionWiseInstruction").find(".sectionName").text(subject.aliasName+" - "+subject.section);
			$(".test_question_summary").find(".sub_name").text(subject.aliasName+" - "+subject.section);
			for(var i=1;i<=subject.length;i++){
				var newElem = document.createElement("span");
				$(newElem).addClass("each_question").removeClass("nonner mark_for_review save_and_next saved_and_marked").addClass(this.getLastClassName(i-1)).text("Q"+i);
				$(parentElem).append(newElem);
			}
			$(parentElem).append("<div class='cleaner_with_divider'>&nbsp;</div>");
		}
		var optionsDiv = $("#test_home .test_questions .questionNumber").text("Question "+(index+1)+":");
		var list = $("#test_home .test_question_summary .q_list").find(".each_question");
		var lastIndex = $(list).index($("#test_home .test_question_summary .q_list .current"));
		$($(list).get(lastIndex)).removeClass("current mark_for_review save_and_next saved_and_marked").addClass(this.getLastClassName(lastIndex));
		$($(list).get(index)).addClass("current");
	},
	showOnlyMarkedForReviewQuestions : function(e){
		if(e.currentTarget.checked == true){
			var list = $("#test_home .test_question_summary .q_list span");
			var all = $("#test_home .test_question_summary .q_list").find(".mark_for_review,.saved_and_marked");
			// var saved_and_marked = $("#test_home .test_question_summary .q_list").find(".saved_and_marked");
			// var currentIndex = all.length;
			// for(var i = 0; i < saved_and_marked.length; i++) {
			// 	all[currentIndex++] = saved_and_marked[i];
			// 	all.length++;
			// }
			// console.log(saved_and_marked);
			var first1 = $(all).get(0);
			if(first1){
				$(list).not(".mark_for_review,.saved_and_marked").addClass("hidden");
				// $(list).not(".saved_and_marked").addClass("hidden");
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
		}
	},
	openInstruction : function(){
		var popup = showVPopup();
		popup.html($("#test_home").find(".testGuidelinesHolder").parent().html());
	},
	openQuestionPaper : function(){
		var popup = showVPopup();
		popup.html($("#test_home").find(".questionPaperHolder").parent().html());
        loadTestMJEqns(popup.find(".questionPaperHolder").get(0));
	},
	closeTablePopup : function(){
		closeVPopup();
	},
	submitTheTest : function(){
		if(!takeTest.ifInternetAvailable()) return;
		endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
		closeVPopup();
	},
	getAnswers:function(){
		var quesBody = $("#test_home .test_questions .questionBody");
		var optionsParent = quesBody.find(".choices");
		var numAnsDiv = quesBody.find(".testNumAns");
        var subjAnsDiv = quesBody.find(".subjTextAns");
		var qType = quesBody.data("qType");
		var answer = [];
		if(qType == "NUMERIC"){
			var val = numAnsDiv.find(".numericAnsInputBox").val();
			if(val)	answer.push(val);
		}
        else if(qType == "SUBJECTIVE"){
            console.log("Inside getAnswers subjective");
            var rteHolder = subjAnsDiv.find(".instSubjRTEHolder").children(".RTEHolder");
            var val = vRTE.getRTEContent(rteHolder);
            if(val) answer.push(val);
        }
        else{
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
		var btns = parDiv.find(".allbtnholder");
		btns.on("click",".mark_review_btn",this.markForReview)
			.on("click",".reset_btn",this.resetAttempt)
			// .on("click",".skip_btn",this.skipQuestion)
			.on("click",".save_next_btn",this.saveAndNextQuestion);
		parDiv.find(".test_subjects .test_subjects_tabs").on("click","span",this.changeSubjectClick);
		parDiv.find(".test_subjects .test_subjects_tabs").on("mouseenter","img",this.showSectionWiseQuestionStatus);
		parDiv.find(".test_subjects .test_type_tabs").on("click","span",this.showTypeQuestion);
		parDiv.on("click",".test_end_btn",this.endExam)
			.on("click",".refreshQuesTest",this.postRefresh);
		parDiv.find(".test_question_summary .q_list").on("click",".each_question",this.changeQuestion);
		parDiv.find(".test_question_summary .show_only_marked").on("change","input",this.showOnlyMarkedForReviewQuestions);
		parDiv.find(".qpandinstr").on("click",".read_instr",this.openInstruction);
		parDiv.find(".qpandinstr").on("click",".questionPaper",this.openQuestionPaper);
	}
};
$(document).ready(function(){
	takeTest.init();
});
