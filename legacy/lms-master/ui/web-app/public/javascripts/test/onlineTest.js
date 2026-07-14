var testFns = new function(){
	this.onFullScreen = function(){
		$("#topBarHolder").addClass("nonner");
		//$("#searchContentHeader").addClass("nonner");
		$("#chatDivsWrapper").addClass("nonner");
		$("#feedBackButHolder").addClass("nonner");
		$("#guideButHolder").addClass("nonner");
		$(".otherUserUseTopBar").addClass("nonner");
	};
	this.onFullScreenExit = function(){
		$("#topBarHolder").removeClass("nonner");
		//$("#searchContentHeader").removeClass("nonner");
		$("#chatDivsWrapper").removeClass("nonner");
		$("#feedBackButHolder").removeClass("nonner");
		$("#guideButHolder").removeClass("nonner");
		$(".otherUserUseTopBar").removeClass("nonner");
	};
	this.onTestEnd = function(testId,targetType,targetId){
		//openMyPage("/Tests/endTest",{'testId':testId,endTime:(new Date()).getTime()},null,null,true);
		var orgId = $("#myInstitutePage").data('orgId');
		bigLoader("#noTabSection");
		$.get("/Tests/endTest",{'testId':testId,endTime:(new Date()).getTime()},function(data){
			if(data && data.errorCode==""){
				if(orgId){
   					goToInstTestPage("END_TEST",testId,null,null,orgId,targetType,targetId);
				}else{
					openTestPage(testId);
				}
			}
			else if(data.errorCode == "NO_DATA_RECIEVED"){
				showError("Sorry the request could not be processed,contact admin");
				return ;
			}
			else if(data.errorCode == "ALREADY_ATTEMPTED"){
				goToInstTestPage("END_TEST",testId,null,null,null,targetType,targetId);
			}
			else{
				//showError(i18nJS("TEST_END_PROBLEM"));
				goToInstTestPage("END_TEST",testId,null,null,null,targetType,targetId);
			}
                        var testname=$("#current_test_name").val();
                        trackEventForGA("TEST","ATTEMPT",testname);
		});
	};
	this.urls = {
		"submitAnswerUrl":"/Tests/submitTestAnswer",
		"resetAttemptUrl":"/Tests/resetTestAnswer",
		"getQuestionsJson":"/Tests/getQuestionsJson"
	};
	this.reload = function(testId){
		goToInstPreTestPage("TEST",testId);
	};
	this.postTestPage = function(testId){
		goToInstTestPage("TEST",testId);
	};
	this.terminateProgressTest = function(paramsIn,cb){
        	$.post("/Tests/terminateTest",paramsIn,function(data){
			try{
				cb(data);
			}catch(err){};
		});
	};
	this.retryCurrentTest = function(){
		preTest.takeThisTestFromPreTest();
	};
};
var TEST_CREATION_ERR_CODE = {
	"TEST_NOT_FOUND":"Sorry, Test Not Found",
	"QUESTION_NOT_FOUND":"Retry Adding the question later",
	"BOARD_NOT_FOUND":"Error occured, Try , again later",
	"INVALID_QUESTION_TYPE":"Question Type not supported",
	"FILE_NOT_FOUND":"File Not Found",
	"PROCESS_FAILURE":"Please retry after sometime",
	"TYPE_NOT_SUPPORTED":"Not supported type",
	"METADATA_NOT_FOUND":"Error occured, Try , again later",
	"QUESTION_ALREADY_ADDED":"Selected Question , already added to Test",
	"INCOMPLETE_TEST":"Your Test is In-Complete",
	"ALREDY_PUBLISHED":"Your Test is already published",
	"USER_NOT_FOUND":"Please Log In Again",
	"QUESTION_MAX_COUNT_EXCEED":"Question count exceeded max limit for selected subject and question type",
	"NOT_PUBLISHED":"Your Test not published"
};
