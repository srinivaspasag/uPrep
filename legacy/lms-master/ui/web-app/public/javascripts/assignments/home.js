var assignments = new function(){
	var userRole;
	var parDiv = "#assignments";
	var centerDiv = ".centerAsn";
	var targetUserId = "";
	var id;
	var alphabets=['(a)','(b)','(c)','(d)','(e)','(f)','(g)','(h)','(i)','(j)','(k)','(l)'];
	this.init = function(){
		parDiv = $(parDiv);
		centerDiv = parDiv.find(centerDiv);
		regFns();
		id = parDiv.data("id");
		userRole = parDiv.data("role");
		targetUserId = parDiv.data("targetUserId");
		if(userRole == "STUDENT"){
			getStudentAnalytics(targetUserId);	
		}else{
			getTeacherAnalytics();	
		}
	};
	var startTime = new Date().getTime();
	var regFns = function(){
		$(parDiv).on("click",".asnLeftTab",switchTab)
			.on("click",".attemptAsnQues",showQuesAttemptBox)
			.on("click",".quesCancelAns",hideQuesAttemptBox)
			.on("click",".quesAnsSubmit",quesAnsSubmit)
			.on("click",".asnQuesTable .loadMoreItems",loadMoreQuestions)
			.on("click",".asnUsersTable .loadMoreItems",loadMoreUsersAna)
			.on("click",".viewStAsnAttempts",teacherViewStudentAna)
			.on("change",".asnQuestionsSortBySubDrop .nDropDown",filterQuestionBySub);
	};
	var hideStudentMiniAna =  function(){
		parDiv.find(".asnStudentMiniAna").html("");
		parDiv.find(".asnLeftTab-ST").addClass("nonner").removeClass("selected");
	};
	var teacherViewStudentAna = function(){
		var $this = $(this);
		var targetUserId = $this.data("targetUserId");
		var targetUserFNm = $this.data("userFirstName");
		getStudentAnalytics(targetUserId);
		var d = parDiv.find(".asnLeftTab-ST").data("targetUserId",targetUserId)
			.removeClass("nonner").addClass("selected");
		d.find(".stFirstName").text(targetUserFNm);
		d.siblings().removeClass("selected");
	};
	var loadMoreQuestions = function(){
		var $this = $(this);
		var start = $this.data("nextStart");
		smallLoader($this);
		if(userRole == "STUDENT"){
			var targetUser = $this.data("targetUserId");
			getStudentQuestions(start,targetUser,$this);
		}else{
			getTeacherQuestions(start,$this);
		}
	};
	var filterQuestionBySub = function(){
		var userRole = $(this).closest(".asnQuestionsSortBySubDrop").data("userRole");
		if(userRole == "STUDENT"){
			//getStudentQuestions(0);
			var brdId = $(this).data("value");
			var table = parDiv.find(".asnQuesTable");
			if(brdId){
				table.find(".asnEachQuesTR").addClass("nonner");
				table.find(".asnQuesTRBrd-"+brdId).removeClass("nonner");
			}else{
				table.find(".asnEachQuesTR").removeClass("nonner");
			}
		}else{
			parDiv.find(".asnQuesTable").find(".asnEachQuesTR,.zeroLevelDivTR,.loadMoreDivTR").remove();
			getTeacherQuestions(0);
		}
	};
	var quesAnsSubmit = function(e){
		var ansBlock=$(this).closest(".quesAnsDiv").find(".addAnsBlock"),qType=ansBlock.data("type");
       		var ansStatus=checkQuesAnsUtils.quesAnsSubmit[qType](ansBlock);
		//console.log(ansStatus);
		if(ansStatus.result){
           		sendAnswer(ansBlock,ansStatus.ans,$(this));
       		}
       		else{
           		showError(ansStatus.error);
       		}
		if(e){
			e.stopPropagation();		
			e.preventDefault();
		}
		return false;
	};
	var getStudentMiniAnalytics = function(inTargetUserId){
		inTargetUserId = inTargetUserId ? inTargetUserId : targetUserId;
		var params = {id:id,targetUserId:inTargetUserId};
		var holder = parDiv.find(".asnStudentMiniAna");
		smallLoader(holder);
		vReq.get("/Assignments/studentMiniAnalytics",params,function(data){
			holder.html(data);
		});
	};
	var putQuestionAnalytics = function(data,ansBlock,answer){
		//console.log(data);
		var classNm = "";
		if(data.result){
			data = data.result;
			var tr = $(ansBlock).closest(".asnEachQuesTR");
			tr.find(".quesAnsDiv").remove();
			if(data.isUserAnswerCorrect == "CORRECT"){
				classNm = "correct";
			}
			else{
				classNm = "inCorrect";
			}
			var qType=ansBlock.data("type");
			var userAnswer = data.userAnswer;
			var correctAnswer = data.correctAnswer;
			userAnswer = userAnswer?userAnswer:answer;
			var respDiv = tr.find(".asnQuesAttempted").removeClass("nonner");
			if(qType && qType!="NUMERIC"){
				var ansList = userAnswer;
				userAnswer = "";
				$(ansList).each(function(index,value){
					userAnswer += alphabets[value-1] +",";
				});
				userAnswer = userAnswer.slice(0,userAnswer.lastIndexOf(","));
				if(data.isUserAnswerCorrect != "CORRECT"){
					ansList = correctAnswer;
					correctAnswer = "";	
					$(ansList).each(function(index,value){
						correctAnswer += alphabets[value-1] +",";
					});
					correctAnswer = correctAnswer.slice(0,correctAnswer.lastIndexOf(","));
				}
			}
			if(data.isUserAnswerCorrect != "CORRECT" && correctAnswer){
				respDiv.find(".correctAnsHold").text(correctAnswer);
			}else{
				respDiv.find(".correctAnsHold").closest("div").remove();
			}
			respDiv.find(".yourAnsHold").addClass(classNm).text(userAnswer);
			setTimeout(function(){
				getStudentMiniAnalytics();
			},1000);
		}
	};
	var sendAnswer = function(ansBlock,answer){
		var qid=ansBlock.data("qid");
        	ansBlock.closest(".quesAnsDiv").find(".quesAnsSubmit").text("Submitting..");
        	showTopLoader();
		var timeTaken = new Date().getTime() - startTime; 
		var params = {"assignmentId":id,"qId":qid,answerGiven:answer,timeTaken:timeTaken};
		vReq.post("/Assignments/submitAnswer",params,function(data){
			hideTopLoader();
			putQuestionAnalytics(data,ansBlock,answer);
		},function(){
        		ansBlock.closest(".quesAnsDiv").find(".quesAnsSubmit").text("Submit");
		});
	};
	var showQuesAttemptBox = function(){
		var tr = $(this).closest(".asnEachQuesTR");
		tr.find(".quesAnsDiv").show();
		$(this).addClass("nonner");
		startTime = new Date().getTime();
	};
	var hideQuesAttemptBox = function(e){
		var tr = $(this).closest(".asnEachQuesTR");
		tr.find(".quesAnsDiv").hide();
		tr.find(".attemptAsnQues").removeClass("nonner");
		if(e) e.preventDefault();
		return false;
	};
	var switchTab = function(){
		var type = $(this).data("type");
		if($(this).hasClass("selected")){ return; }
		hideStudentMiniAna();
		$(this).addClass("selected").siblings().removeClass("selected");
		switch(type){
			case "QUESTIONS" : getTeacherAnalytics();
					break;
			case "PERFORMANCE" : getStudentPerformance();
					break;
			case "ST_PERFORMANCE" : 
					var targetUserId = $(this).data("targetUserId");
					getStudentAnalytics(targetUserId);	
					break;
		}
	};
	var getStudentAnalytics = function(targetUserId){
		bigLoader(centerDiv);
		vReq.get("/Assignments/studentAnalytics",{id:id},function(data){
			centerDiv.html(data);
			getStudentQuestions(0,targetUserId);
		});
		getStudentMiniAnalytics(targetUserId);	
	};
	var getTeacherAnalytics = function(){
		bigLoader(centerDiv);
		vReq.get("/Assignments/teacherAnalytics",{id:id},function(data){
			centerDiv.html(data);
			getTeacherQuestions(0);
		});	
	};
	var getStudentPerformance = function(){
		bigLoader(centerDiv);
		vReq.get("/Assignments/studentsPerformance",{id:id},function(data){
			centerDiv.html(data);
			getStudentAttemptAnaltics(0);
		});	
	};
	var loadMoreUsersAna = function(){
		var $this = $(this);
		var start = $this.data("nextStart");
		smallLoader($this);
		getStudentAttemptAnaltics(start,$this);	
	};
	var getStudentAttemptAnaltics = function(start,loadMoreDiv){
		var pr = {"id":id,"start":start,"size":10};
		var holder = centerDiv.find(".asnUsersTable");
		if(start == 0){
			bigLoader(holder);
		}
		vReq.get("/Assignments/studentAttemptAnalytics",pr,function(data){
			if(start){
				$(loadMoreDiv).remove();
				holder.append(data);
			}else{
				holder.html(data);
			}
		});
	};
	var getStudentQuestions = function(start,targetUserId,loadMoreDiv){
		var pr = {"targetUserId":targetUserId,"id":id,"start":start,"size":10};
		/*var brdId = parDiv.find(".asnQuestionsSortBySubDrop .nDropDown").data("value");
		if(brdId){
			pr[brdId] = brdId;
		}*/
		var holder = centerDiv.find(".asnQuesTable");
		vReq.get("/Assignments/studentQuestions",pr,function(data){
			$(loadMoreDiv).remove();
			holder.append(data);
			loadTestMJEqns(holder.get(0));
		});
	};
	var getTeacherQuestions = function(start,loadMoreDiv){
		var pr = {"id":id,"start":start,"size":10,targetUserId:targetUserId};
		var brdId = parDiv.find(".asnQuestionsSortBySubDrop .nDropDown").data("value");
		if(brdId){
			pr["brdId"] = brdId;
		}
		var holder = centerDiv.find(".asnQuesTable");
		vReq.get("/Assignments/teacherQuestions",pr,function(data){
			$(loadMoreDiv).remove();
			holder.append(data);
			loadTestMJEqns(holder.get(0));
		});
	};
};
$(function(){
	try{institute.init();}catch(err){}
	assignments.init();
});
