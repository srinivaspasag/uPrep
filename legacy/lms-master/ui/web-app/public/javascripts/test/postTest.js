var testPageTargetUserId;
function initPostTest() {
    $("#postTest .icon").error(function() {
        $(this).addClass("nonner");
    });
    $(document).on("click", ".quesGridViewBtn", function(e) {
        var lastSelected = $(".userQuestionAttemptDetails").find(".activeBlackTab").removeClass("activeBlackTab");
        $(lastSelected).removeClass($(lastSelected).data("activeClass"));
        $(e.currentTarget).addClass("activeBlackTab").addClass($(e.currentTarget).data("activeClass"));
        $(".postTestQuesView").addClass("nonner");
        $(".postTestQuesGridView").removeClass("nonner");
    });
    $(document).on("click", ".quesListViewBtn", function(e) {
        var lastSelected = $(".userQuestionAttemptDetails").find(".activeBlackTab").removeClass("activeBlackTab");
        $(lastSelected).removeClass($(lastSelected).data("activeClass"));
        $(e.currentTarget).addClass("activeBlackTab").addClass($(e.currentTarget).data("activeClass"));
        $(".postTestQuesView").removeClass("nonner");
        $(".postTestQuesGridView").addClass("nonner");
        $(".resultPreviewChooseSubject").updateVSelectTag();
        loadTestMJEqns($(".postTestQuesView").get(0));
    });
    $("#postTest").on('click', '.viewAll,.viewResultBtn', showLeadersInPostTest)
            .on('click', '.showTestDetails', showTestDetailsPopup)
            .on('click', '.postTestMainTabs .depthTab', switchPostTestMainTab)
            .on('click', '.testQuesLoadMore', testQuesLoadMore)
            .on('click', '.postTestQuesShowSoln', getSolutionsPopup)
            .on('click','.submitRange',submitRange)
            .on('click','.studentQuesAnalytics',studentQuesAnalytics)
            .on('click','.change-question-status',changeQuestionStatus)
            .on('click','.submit-question-status',submitQuestionStatus)
            .on('click','.editStudentTest',editStudentTest)
            .on('click','.retakeTest',retakeStudentTest)
            .on('click','.resumeTest',resumeStudentTest)
            .on('click','.showAnswerInPopup',showAnswerInPopup)
            .on('click','.viewSolution',viewSolution)
            .on('click','.refreshStudentsAttemptsList',refreshStudentsAttemptsList)
            .on('change', ".testQuesAnalyticsSelectPaper .nDropDown,.testQuesAnalyticsSelectSubject .nDropDown,.testQuesAnalyticsSelectSort .nDropDown", function() {
        fetchTeacherTestQues(0);
    });
    var testId = $(".incommingTestId").val();
    if (!testId)
        testId = getURLParameter("testId");
    testPageTargetUserId = urlQueryHelper.read("targetUserId");
    vSelectFns.init();
    //MathJax.Hub.Queue(["Typeset",MathJax.Hub],$(".postTestQuesView").get(0));
    //loadTestMJEqns($(".postTestQuesView").get(0));

    var eachTopics = $(".subjectAnalytics .subjectList").find(".selectedSubject").find(".eachTopic");
    var pieData = [];
    $(eachTopics).each(function() {
        pieData.push({"itemName": $(this).data("topicName"), "itemValue": $(this).data("weightageValue")});
    });
    if ($("#postTest").find(".testOvrlAnalyticsQuesList").get(0)) {
        fetchTeacherTestQues(0);
    }
    //$(".testAnalyticsPieChartHolder").createGraph("PIE",{title:""},{width:160,height:150},pieData);
    $(".testTitle").elipsifyText();
    readUrlPostTest();
}
;
function testQuesLoadMore() {
    var $this = $(this);
    smallLoader($this);
    var start = parseInt($this.data("nextStart"), 10);
    fetchTeacherTestQues(start, function(data) {
        $this.remove();
    });
}
function fetchTeacherTestQues(start, cb) {
    var page = $("#postTest");
    var testId = page.find(".testQuesAnalyticsSelectPaper .nDropDown").data("value");
    var brdId = page.find(".testQuesAnalyticsSelectSubject .nDropDown").data("value");
    var orderBy = page.find(".testQuesAnalyticsSelectSort .nDropDown").data("value");
    var sortOrder = page.find(".testQuesAnalyticsSelectSort .nDropDown").data("itemValue");
    sortOrder = sortOrder ? sortOrder.order : "DESC";
    start = start ? start : 0;
    var params = {"entity": {"type": "TEST", "id": testId}, "orderBy": orderBy, "sortOrder": sortOrder, "size": 10, "start": start};
    if (brdId) {
        params["brdId"] = brdId;
    }
    var holder = page.find(".testOvrlAnalyticsQuesList");
    if (start == 0) {
        bigLoader(holder);
    }
    vReq.get("/Tests/drawTeacherTestQuestions", params, function(data) {
        if (cb) {
            try {
                cb(data);
            } catch (err) {
            }
        }
        if (start == 0) {
            holder.html(data);
        } else {
            holder.append(data);
        }
        loadTestMJEqns(holder.get(0));
    });
}
function resultPreviewSubjectChanged(value, targetElem, targetValue) {
    if (targetValue) {
        $(".testResultQuesTable").find(".tableRowResultQues").addClass("nonner");
        $(".testResultQuesTable").find(".cls-" + targetValue).removeClass("nonner");
    } else {
        $(".testResultQuesTable").find(".tableRowResultQues").removeClass("nonner");
    }
}
function postTestSubjectChanged(value, targetElem, targetValue) {
    $(".subjectAnalytics .subjectList").find(".selectedSubject").removeClass("selectedSubject");
    var eachTopics = $(".subjectAnalytics .subjectList").find("#sub-" + targetValue).addClass("selectedSubject").find(".eachTopic");
    var pieData = [];
    $(eachTopics).each(function() {
        pieData.push({"itemName": $(this).data("topicName"), "itemValue": $(this).data("weightageValue")});
    })
    //$(".testAnalyticsPieChartHolder").createGraph("PIE",{title:""},{width:160,height:150},pieData);
    //clickStream.record("QUESTION_VIEW_HOLDER","SUBJECT_SELECT","CHANGE",value);
}
function topicWeightageSubjectChanged(value, targetElem, targetValue) {
    $(".testTopicsWeightageTable").addClass("nonner");
    $(".testTopicsWeightageHolder").find(".cls-topicWeightage-" + targetValue).removeClass("nonner");
    //clickStream.record("TEST_DETAILS_CENTER_BODY","TOPIC_WEIGHTAGE_SELECT","CHANGE",value);
}
function childrenTestChanged(value, targetElem, targetValue) {
    $(targetElem).closest(".vselect").find(".vSelectText").text(value);
    var targetUserId = $(targetElem).data("targetUserId");
    var targetUserRole = $(targetElem).data("targetUserRole");
    try {
        goToInstTestPage("HISTORY", targetValue, targetUserId, targetUserRole);
    } catch (err) {
    }
    //clickStream.record("TEST_DETAILS_CENTER_BODY","VIEW_TEST_SELECT","CHANGE",value);
}
function showLeadersInPostTest(e) {
    /*var testId = $(".incommingTestId").val();
     if(!testId) testId = getURLParameter("testId"); 
     showLeaderBoardPopup(testId);*/
    $(".testResultSheetTab").trigger("click");
    if (e) {
        e.preventDefault();
    }
}
function fetchTestQuestions(testId, holder, targetUserId, testType, resultVisibility, resultVisibilityMessage) {
    if (testId) {
        if (resultVisibility === "VISIBLE") {
            bigLoader(holder);
            var params = {entity: {type: "TEST", id: testId}, targetUserId: targetUserId, testType: testType, resultVisibility: resultVisibility};
            vReq.get("/Tests/drawTestQuestions", params, function(data) {
                holder.html(data);
                vSelectFns.init();
                loadTestMJEqns(holder.get(0));
            });
        } else {
            showResultsVisibilityMessage(holder, resultVisibilityMessage);
        }
    }
}
function fetchTestMarkSheet(holder, testId, size, resultVisibility, resultVisibilityMessage,isDetailedResultSheet,minScore,maxScore) {
    if (resultVisibility !== "VISIBLE") {
        showResultsVisibilityMessage(holder, resultVisibilityMessage);
    } else {
        bigLoader(holder);
        var size = size ? size : 50;
        size = size <= 200 ? size : 200;
        var params = {entity: {type: "TEST", id: testId}, start: 0, size: size};
        if(isDetailedResultSheet){
            params.isDetailedResultSheet = isDetailedResultSheet;
            params.minScore = minScore;
            params.maxScore = maxScore;
        }
        vReq.get("/Tests/testResultSheet", params, function(data) {
            holder.html(data);
            $(".detailedAnalytics .refreshResultSheet").css("display","none");
            $(".detailedAnalytics .countStudents").removeClass("nonner");
            var LMHandlerDiv = holder.find(".LMHandlerDiv");
            if (LMHandlerDiv.length > 0) {
                LMHandlerDiv.data("urlStr", "/Tests/testResultSheetStudents")
                        .data("size", size).data("allParams", params)
                        .data("callback", "resetResultSheet");
            }
            marksSheetPage.init(null,holder);
        });
    }
}
var showResultsVisibilityMessage = function(holder, message) {
    var m = i18nJS("RESULTS_NOT_OUT_YET");
    if (message) {
        m = message;
    }
    holder.html('<div class="userMessage" style="padding: 150px 0px;\n\
     background-color: rgb(255, 255, 255); margin-top: 0px;">' + m + '</div>');
};
function readUrlPostTest() {
    var holder = $(".postTestMainTabs");
    var targetUserId = urlQueryHelper.read("targetUserId");
    if (testPageTargetUserId != targetUserId) {
        return false;
    }
    var tabId = urlQueryHelper.read("tabId");
    if (!tabId) {
        tabId = holder.data("defaultTab");
    }
    ;
    var target;
    holder.find(".depthTab").each(function() {
        var $this = $(this);
        if ($this.data("tabId") == tabId) {
            target = $this;
        }
    });
    if (!target && !target.get(0))
        return;
    onPostTestMainTabChange(tabId, target);
    return true;
}
function switchPostTestMainTab(e) {
    var target = $(e.currentTarget);
    var _id = target.data("tabId");
    onPostTestMainTabChange(_id, target);
    urlQueryHelper.push("tabId", _id);
}
function getSolutionsPopup(){
    var $this = $(this);
    var qId = $this.data("qid");
    if(!qId || $this.hasClass("disabled")){ return; }
    var params = {
        start : 0,
        //Changing size to only get latest solution
        size : 1,
        orderBy : "timeCreated",
        attempted : true,
        qId : qId
    };
    showTopLoader();
    vReq.get("/Questions/quesSolutions",params,function(data){
        var popup = showVPopup(0.6);
        data = "<div id='quesSolutionsPopup'>"+data+"</data>";
        popup.html(data);
        popup.find(".showQSComments,.LMHandlerDivLoadMore,.quesSolnVoteDiv").remove();
    });
}
function onPostTestMainTabChange(_id, target) {
    $(".postTestMainTabs").find(".activeDepthTab").removeClass("activeDepthTab");
    $(".userScoreBoard").addClass("nonner");
    $(".userQuestionAttemptDetails").addClass("nonner");
    $(".postTestDetails").addClass("nonner");
    $(".teacherTestDetails").addClass("nonner");
    $(".postTestResultSheet").addClass("nonner");
    $(".detailedAnalytics").addClass("nonner");
    $(".handleTest").addClass("nonner");
    $(".offlineMarkSheetsHolder").addClass("nonner");
    $(".showRatings").addClass("nonner");
    $(".gradeSubjectiveQues").addClass("nonner");
    $(".printResultSheet").addClass("invisibleButton");
    var resultVisibility = $(".postTestMainTabs").data("resultVisibility") || "VISIBLE";
    var resultVisibilityMessage = $(".postTestMainTabs").data("resultVisibilityMessage");
    var sideLeaderBoard = $(".testCreationLeftBar").find(".leaderBoard");
    sideLeaderBoard.removeClass("hideMe");
    timerObj.stop("refresh");
    switch (_id) {
        case "performance":
            var holder =  $(".userScoreBoard").removeClass("nonner");
            $(".showRatings").removeClass("nonner");
            if (resultVisibility == "HIDDEN") {
                showResultsVisibilityMessage(holder, resultVisibilityMessage);
            }
            break;
        case "overallPerformance":
            $(".teacherTestDetails").removeClass("nonner");
            break;
        case "yourAnswer":
            var holder = $(".userQuestionAttemptDetails").removeClass("nonner");
            $(".selectChildrenTest").updateVSelectTag();
            var testId = target.data("testId");
            var targetUserId = target.data("targetUserId");
            var testType = target.data("testType");
            fetchTestQuestions(testId, holder, targetUserId, testType, resultVisibility, resultVisibilityMessage);
            break;
        case "testDetails":
            $(".postTestDetails").removeClass("nonner");
            $(".topicWeightageChooseSub").updateVSelectTag();
            break;
        case "resultSheet":
            var holder = $(".postTestResultSheet").removeClass("nonner");
            var testId = target.data("testId");
            fetchTestMarkSheet(holder, testId, null, resultVisibility, resultVisibilityMessage);
            $(".printResultSheet").removeClass("invisibleButton");
            sideLeaderBoard.addClass("hideMe");
            break;
        case "detailedAnalytics":
            $(".detailedAnalytics").removeClass("nonner");
            $(".minNumber").focus();
            sideLeaderBoard.addClass("hideMe");
            $(".fullMarksSheetPage").addClass("nonner");
            $(".minNumber").val("");
            $(".maxNumber").val("");
            break;
        case "handleTest":
            sideLeaderBoard.addClass("hideMe");
            getTestStudentsAttemptsList();
            // var interval = setInterval(function(){
            //     if($(".activeDepthTab").data("tabId") === "handleTest"){
            //         getTestStudentsAttemptsList();
            //     }else{
            //         clearInterval(interval);
            //     }
            // },30000);
            break;
        case "markSheets":
            sideLeaderBoard.addClass("hideMe");
            $(".offlineMarkSheetsHolder").removeClass("nonner");
            if(!uploadStatusTimer){
                fetchScripts([{fname: "uicomWidgets/fileuploader.js",
                    cb: createOfflineTestUploader}]);
                $(".uploadTestsProgressDiv").html("");
            }
            break;
        case "gradeSubjectiveQuesTab":
            $(".gradeSubjectiveQues").removeClass("nonner");
            sideLeaderBoard.addClass("hideMe");
            getTestSubjectiveQuestions();
            break;
    }
    if (resultVisibility !== "VISIBLE") {
        sideLeaderBoard.addClass("hideMe");
    }
    target.addClass("activeDepthTab");
}

function refreshStudentsAttemptsList(){
    getTestStudentsAttemptsList();
}

var createOfflineTestUploader = function() {
    var uploadDiv = $(".offlineMarkSheets").find(".uploadDiv");
    var u = new qq.FileUploader({
        element: uploadDiv.get(0),
        action: '/Tests/uploadMarkSheets',
        params: {uploadFileParamName: "resultFile",testId:$(".incommingTestId").val(),orgId:$("#myInstitutePage").data("orgId"),userId:USERID,"targetId":$(".offlineMarkSheets").data('targetId'),"targetType":$(".offlineMarkSheets").data('targetType')},
        debug: true,
        sizeLimit: 5 * 1024 * 1024,
        multiple:false,
        allowedExtensions: ["xls", "xlsx"]
    });
    u.onUploadDone = onUploadDone;
    u.onUploadProgress = onUploadProgress;
    var uploadButton = uploadDiv.find(".qq-upload-button");
    uploadButton.addClass("offlineMarkSheetButton");
    uploadButton.addClass("blueButton");
    uploadDiv.find(".qq-button-title").html("Choose File");
    return u;
};

var onUploadProgress = function(percentDecimal, progressText) {
    $(".offlineMarkSheets").find(".qq-upload-button").addClass("nonner");
    $(".offlineMarkSheets").find(".uploadTestsProgressDiv")
            .html("<span class='color8 smally'>Percentage Uploaded:</span>" + Math.round(percentDecimal * 100) + " %");
};
var uploadMarksSheetJobIds = [], uploadSheetProcessPercentDiv, uploadStatusTimer;
var progressBar;
var onUploadDone = function(id, fileName, data) {
        var err = data.errorCode;
        uploadMarksSheetJobIds = [];
        if (!data || err != "") {
            if (data && data.errorMessage != "")
                err = result.errorMessage;
            else if (!data) {
                err = "Refresh the page and try again.";
            }
            showError("<div class='boldy big18 margBot10 redColor'>Some error occured.</div>Error:" + err);
            afterUploadTestsAlways();
        } else {
            var errorCode = data.errorCode;
            if (errorCode) {
                showError("Some Error occured:" + errorCode);
                afterUploadTestsAlways();
            } else {
                var jobId = data.result.jobId;
                uploadMarksSheetJobIds = [jobId];
            }

            if (uploadMarksSheetJobIds.length === 0)
                return;
            uploadStatusUpdater();
            $(".offlineMarkSheets").find(".qq-upload-button").addClass("nonner");
            $(".offlineMarkSheets").find(".uploadTestsProgressDiv")
                    .html("<span class='color8 smally'>\n\
            Processing:</span> <div class='ldBar' data-preset='rainbow'></div>");
            // uploadSheetProcessPercentDiv = $(".offlineMarkSheets").find(".percentDiv");
            progressBar = new ldBar(".ldBar");
            uploadStatusTimer = setInterval(function() {
                uploadStatusUpdater();
            }, 5000);
        }
    };

var uploadStatusUpdater = function() {
        $.get("/Tests/uploadMarkSheetsStatus",
                {jobIds: uploadMarksSheetJobIds}, function(data) {
            var result = data.result.list;
            for (var k = 0; k < result.length; k++) {
                var entityResult = result[k];
                var percent = Math.round(entityResult.numCompletedSteps * 100 /
                        entityResult.numOfSteps);
                progressBar.set(percent);
                // uploadSheetProcessPercentDiv.html(percent);
                var removeJobId = false;
                if (entityResult.errorCode !== "") {
                    removeJobId = true;
                    setTimeout(function(){
                        showError("Some Error Occured: " +
                            entityResult.errorCode);
                    },1000);
                } else if (entityResult.numCompletedSteps === entityResult.numOfSteps) {
                    removeJobId = true;
                    var uploadMessage = "";
                    if (entityResult.message) {
                        uploadMessage = entityResult.message;
                    }
                    setTimeout(function(){
                        showMessage("Successfully Done<br>\n\
                    <div class='smallThinGray'>" + uploadMessage + "</div>");
                    },1000);
                }
                if (removeJobId) {
                    try {
                        clearInterval(uploadStatusTimer);
                    } catch (err) {
                        console.log("err in clearing interval")
                    }
                    ;
                    setTimeout(function(){
                        afterUploadTestsAlways();
                    },1000)
                }
            }
        });
    };

var afterUploadTestsAlways = function() {
        $(".offlineMarkSheets").find(".uploadTestsProgressDiv").html("");
        $(".offlineMarkSheets").find(".qq-upload-button").removeClass("nonner");
        createOfflineTestUploader();
};


function getTestStudentsAttemptsList(){
    var testId = $(".incommingTestId").val();
    var orgId = $("#myInstitutePage").data("orgId");
    var holder = $(".handleTest").removeClass("nonner");
    bigLoader(holder);
    var params = {
        orgId:orgId,
        entity:{
            type:"TEST",
            id:testId
        },
        start:0,
        size:50
    };
    vReq.get("/Tests/testStudentsAttemptsList",params,function(data){
        holder.html(data);
    });
}

function getTestSubjectiveQuestions(){
    var testId = $(".incommingTestId").val();
    var orgId = $("#myInstitutePage").data("orgId");
    var holder = $(".gradeSubjectiveQues").removeClass("nonner");
    bigLoader(holder);
    var params = {
        orgId:orgId,
        id:testId,
        entity:{
            type:"TEST",
            id:testId
        }
    };
    vReq.get("/Tests/drawTestSubjectiveQuestions",params,function(data){
        holder.html(data);
    });
}

function showLoader() {
    $("#testStatusLoader").removeClass("nonner");
}
function hideLoader() {
    $("#testStatusLoader").addClass("nonner");
}

function editStudentTest(){
    var testStatus = $(this).data("testStatus");
    var target = $(this);
    if($(this).hasClass("disabled")){
        return ;
    }
    var completed = parseInt($(".testCompletedCount").val());
    var inprogress = parseInt($(".testProgressCount").val());
    var paused = parseInt($(".testPausedCount").val());
    var resumed = parseInt($(".testResumedCount").val());
    var userName = target.closest("tr").find(".openInstProfile").text().trim()+"("+target.closest("tr").find(".studentId").text().trim()+")";
    if(testStatus === "COMPLETED"){
        message = "Are you sure to reset the test for  "+"<b>"+userName+"</b><br>This will erase student analytics for this test ?";
    }
    else if(testStatus == "IN-PROGRESS"){
        message = "Are you sure to end the test for  "+"<b>"+userName+"</b>";
    }
    else if(testStatus == "PAUSED"){
        message = "Are you sure to pause the test for  "+"<b>"+userName+"</b>";
    }
    else if(testStatus == "RESUMED"){
        message = "Are you sure to resume the test for  "+"<b>"+userName+"</b>";
    }
    else if(testStatus == "REGENERATE_ANALYTICS"){
        message = "Are you sure to regenerate analytics for  "+"<b>"+userName+"</b>";
    }
    showVYesNoBox(message, null, function(state) {
        if (state) {
            doEditTest();
        }
    });
    var doEditTest = function(){
        showLoader();
        var testId = $(".incommingTestId").val();
        var userId = target.closest("tr").find(".openInstProfile").data("userId");
        var orgId = $("#myInstitutePage").data("orgId");
        var params = {
            orgId:orgId,
            entity:{
                id:testId,
                type:"TEST"
            },
            studentUserId:userId
        }
        if(testStatus === "COMPLETED"){
            url = "/Tests/resetStudentTest";
        }
        if(testStatus === "REGENERATE_ANALYTICS"){
            url = "/Tests/regenerateStudentTestAnalytics";
        }
        else if(testStatus == "PAUSED"){
            url = "/Tests/pauseStudentTest";
            params.testId = params.entity.id;
            params.entityType = params.entity.type;
            delete params.entity.id;
            delete params.entity.type;
        }
        else if(testStatus == "RESUMED"){
            url = "/Tests/resumeStudentTest";
            params.testId = params.entity.id;
            params.entityType = params.entity.type;
            delete params.entity.id;
            delete params.entity.type;
        }
        else if(testStatus == "IN-PROGRESS"){
            url = "/Tests/endStudentTest";
            params.testId = params.entity.id;
            params.entityType = params.entity.type;
            delete params.entity.id;
            delete params.entity.type;
        }
        $.post(url,params,function(data){
            hideLoader();
            if(data.errorCode == "TEST_ENDED"){
                showError("Student already ended the test, please refresh");
                return ;
            }
            else if(data.errorCode == "ANALYTICS_GENERATION_UNDER_PROCESS"){
                showError("Analytics are still being generated, Kindly wait");
                return ;
            }
            else if(data.errorCode != ""){
                showError("Something went wrong. Please try again");
                return ;
            }
            var existingTestStatus = target.closest("tr").find(".status").text().trim();
            if(testStatus == "COMPLETED"){
                // target.closest("tr").remove();
                getTestStudentsAttemptsList();
            }
            else if(testStatus == "PAUSED"){
                paused = paused+1;
                $(".testPausedCount").val(paused);
                var ob = target.closest("tr").find(".buttonHolder");
                target.closest("tr").find(".status").html("");
                target.closest("tr").find(".status").html('<span style="color: #ead300;font-weight: bold;"><center>PAUSED</center></span>');
                ob.html('<span class="bigBlueButton editStudentTest" data-test-status="RESUMED" style="margin-bottom:10px;">Resume Test</span><span class="bigBlueButton editStudentTest" data-test-status="IN-PROGRESS" style="background: #d62c2c;">End Test</span>');
            }
            else if(testStatus == "RESUMED"){
                resumed = resumed+1;
                $(".testResumedCount").val(resumed);
                var ob = target.closest("tr").find(".buttonHolder");
                target.closest("tr").find(".status").html("");
                target.closest("tr").find(".status").html('<span style="color:#27BCEC;font-weight: bold;"><center>RESUMED</center></span>');
                ob.html('<span class="bigBlueButton editStudentTest" data-test-status="PAUSED" style="margin-bottom:10px;background: #ead300">Pause Test</span><span class="bigBlueButton editStudentTest" data-test-status="IN-PROGRESS" style="background: #d62c2c;">End Test</span>');
            }
            else if(testStatus == "IN-PROGRESS"){
                completed = completed+1;
                $(".testCompletedCount").val(completed);
                target.closest("tr").find(".status").html("");
                target.closest("tr").find(".status").html('<span style="color: #31d331;font-weight: bold;"><center>COMPLETED</center></span>');
                var ob = target.closest("tr").find(".buttonHolder");
                ob.html(' <span class="bigBlueButton editStudentTest" data-test-status="COMPLETED" style="background:#65c765">Reset Test</span>');
            }
            if(existingTestStatus == "IN-PROGRESS" && inprogress >0){
                inprogress = inprogress -1;
                $(".testProgressCount").val(inprogress);
            }
            else if(existingTestStatus == "RESUMED" && resumed > 0){
                resumed = resumed -1;
                $(".testResumedCount").val(resumed);
            }
            else if(existingTestStatus == "PAUSED" && paused > 0){
                paused = paused -1;
                $(".testPausedCount").val(paused);
            }
            else if(existingTestStatus == "COMPLETED" && completed >0){
                completed = completed -1;
                $(".testCompletedCount").val(completed);
            }
            updateTestStatusCount(completed,inprogress,paused,resumed);
        });
    }
}

function updateTestStatusCount(completed,inprogress,paused,resumed){
    var testProgressCount = inprogress;
    var testCompletedCount = completed;
    var testPausedCount = paused;
    var testResumedCount = resumed;
    if(testProgressCount > 0){
        $(".showTestProgressCount").removeClass("nonner").html("Test In Progress: "+testProgressCount);
    }else{
        $(".showTestProgressCount").addClass("nonner");
    }
    if(testCompletedCount > 0){
        $(".showTestCompletedCount").removeClass("nonner").html("Test Completed: "+testCompletedCount);
    }else{
        $(".showTestCompletedCount").addClass("nonner");
    }
    if(testPausedCount > 0) {
        $(".showTestPausedCount").removeClass("nonner").html("Test Paused: "+testPausedCount);
    }else{
        $(".showTestPausedCount").addClass("nonner");
    }
    if(testResumedCount > 0){
        $(".showTestResumedCount").removeClass("nonner").html("Test Resumed: "+testResumedCount);
    }else{
        $(".showTestResumedCount").addClass("nonner");
    }
}

function retakeStudentTest(){
    if($(this).hasClass("disabled")){
        return ;
    }
    showVYesNoBox("Are you sure you want to retake test?<br><b>This will erase your previous attempt</b>", null, function(state) {
        if (state) {
            doResetTest();
        }
    });
    var doResetTest = function(){
        var urlParams=fetchUrlParams();
        if(!urlParams["targetId"] || !urlParams["targetType"] || 
            urlParams["targetId"]==undefined || urlParams["targetType"]==undefined ||
            urlParams["targetId"]=="undefined" || urlParams["targetType"]=="undefined"
            ){
            showError("You have accessed a direct url.");
            return ;
        }
        var testId = $(".incommingTestId").val();
        var userId = USERID;
        var orgId = $("#myInstitutePage").data("orgId");
        var params = {
            orgId:orgId,
            entity:{
                id:testId,
                type:"TEST"
            },
            studentUserId:userId
        }
        $.post("/Tests/resetStudentTest",params,function(data){
            if(data.errorCode == "ANALYTICS_GENERATION_UNDER_PROCESS"){
                showError("Analytics are still being generated, Kindly wait");
                return ;
            }
            else if(data.errorCode != ""){
                showError("Something went wrong. Please try again");
                return ;
            }
            var orgId = $("#myInstitutePage").data("orgId");
            if(urlParams["targetType"] === "MODULE"){
                showMessage("Test has been resetted.You are now being taken back to library");
                setTimeout(function(){
                    goToInstLibrary("",orgId,"modules");
                },2000);
            }
            else{
                goToInstPreTestPage("",testId,orgId,urlParams["targetType"],urlParams["targetId"]);
            }
            $(document).ready(function() {
                setTimeout(function() {
                    $("#preTest .takeTestBtn").trigger('click');
                },500);
            });
        });
    }
}

function resumeStudentTest(){
    var platform = checkPlatform();
    if(!platform){
        return false;
    }
    var urlParams=fetchUrlParams();
    var subjectiveTest = $(".subjectiveTest").val() === "true" ? true:false;
    if(subjectiveTest){
        $("#topBarHolder").addClass("nonner");
    }
    if(!urlParams["targetId"] || !urlParams["targetType"] || 
        urlParams["targetId"]==undefined || urlParams["targetType"]==undefined ||
        urlParams["targetId"]=="undefined" || urlParams["targetType"]=="undefined")
    {
        showError("You have accessed a direct url.");
        return ;
    }
    if(urlParams["targetType"] === "MODULE" && urlParams["sectionId"] === undefined){
        showError("You have accessed a direct url.");
        return ;
    }
    history.replaceState(null,null,"/");
    handleTestBackTrigger();
    if(subjectiveTest || prepareUiForTest()){
        var testId = $(".incommingTestId").val();
        var orgId = $("#myInstitutePage").data("orgId");
        params = {
            testId:testId,
            orgId:orgId
        }
        params["target.id"] = urlParams["targetId"];
        params["target.type"] = urlParams["targetType"];
        params["context.id"]=urlParams["context.id"];
        params["context.type"]=urlParams["context.type"];
        params["pdfId"]=$(".incommingPdfId").val();
        params['startTime']=(new Date()).getTime();
        if (urlParams["targetType"] === "MODULE" && urlParams["sectionId"] !== undefined) {
          params["sectionId"] = urlParams["sectionId"];
        }
        openMyPage("/Tests/resumeAttempt",params,null,null,true);
    }else{
        showError(i18nJS("TEST_BROWSER_SPECIFIC_SUPPORT_DEPENDENCY"));
    }
}

function viewSolution(){
    $(this).parent().parent().find(".postTestQuesShowSoln").click();
}

function showAnswerInPopup(){
    var popup = showVPopup(0.6);
    popup.html($(this).parent().find(".showSubjectiveAnswer").clone());
    popup.find(".showSubjectiveAnswer").removeClass("nonner");
}

function changeQuestionStatus(){
    var editQuestionStatus = $(this).closest(".question-status-container").find(".edit-question-status");
    if(editQuestionStatus.hasClass("nonner")){
        editQuestionStatus.removeClass("nonner");
    }
    else{
        editQuestionStatus.addClass("nonner");
    }
}

function submitQuestionStatus(){
    var editQuestionStatus = $(this).closest(".edit-question-status");
    var selectStatus = editQuestionStatus.find(".select-status");
    var status = selectStatus.val();
    var quesStatus = $(this).parent().parent().find(".ques-status");
    if(status === null || status === undefined){
        showError("Please choose an option");
        return;
    }
    var testId = $(".incommingTestId").val();
    var questionId = editQuestionStatus.data("questionId");
    var params={
        testId:testId,
        questionId:questionId,
        status:status
    }
    var statusColor = "greyTextColor";
    if(status === "CANCELLED"){
        statusColor = "redColor";
    }
    else if(status === "BONUS"){
        statusColor = "greenColor";
    }
    vReq.post("/Tests/updateQuestionMarkStatus",params,function(data){
        if(data.result.success){
            quesStatus.html(status).removeClass("greyTextColor redColor greenColor").addClass(statusColor);
            editQuestionStatus.addClass("nonner");
            showMessage("Question status updated successfully..please regenerate analytics to reflect the changes");

        }
        else{
            showError("Something went wrong. Please try again");
        }
    });
}

function submitRange(){
    $(".marksOverflowError").text("");
    var min = $(".minNumber").val();
    var max = $(".maxNumber").val();
    if(min==""){
        $(".minError").removeClass("nonner").text("Please enter a minimum number").css("color","red");
        $(".minNumber").focus();
        return false;
    }
    $(".minError").addClass("nonner");
    if(max==""){
        $(".maxError").removeClass("nonner").text("Please enter a maximum number").css("color","red");
        $(".maxNumber").focus();
        return false;
    }
    $(".maxError").addClass("nonner");
    if (isNaN(min)) {
        $(".minError").removeClass("nonner").text("Please enter numeric values only").css("color","red");
        $(".minNumber").focus();
        return false;
    }
    $(".minError").addClass("nonner");
    if (isNaN(max)) {
        $(".maxError").removeClass("nonner").text("Please enter numeric values only").css("color","red");
        $(".maxNumber").focus();
        return false;
    }
    $(".maxError").addClass("nonner");
    if(parseInt(max)<parseInt(min)){
        $(".maxError").removeClass("nonner").text("Max should be more than Min").css("color","red");
        $(".maxNumber").focus();
        return false;
    }
    var totalMarks = $("#examTotalMarks").val();
    if(parseInt(min)>totalMarks || parseInt(max)>totalMarks){
        $(".marksOverflowError").removeClass("nonner").text("Marks must be less than totalMarks: "+ totalMarks).css("color","red");
        return false;
    }
    $(".marksOverflowError").addClass("nonner");
    max = Math.round(max);
    min = Math.round(min);
    var holder = $(".fullMarksSheetPage").removeClass("nonner");
    var testId = $(".detailedAnalyticsTab").data("testId");
    resultVisibility = "VISIBLE";
    var isdetailedResultSheet = true;
    fetchTestMarkSheet(holder, testId, null, resultVisibility,"",isdetailedResultSheet,min,max);
}

function studentQuesAnalytics(event){
    var clicked = event.target;
    var testId = $(".incommingTestId").val();
    var isCorrect=$(clicked).data('isCorrect');
    var quesId = $(clicked).data('quesId');
    var orgId = $("#myInstitutePage").data("orgId");
    if(isCorrect === "CORRECT"){
        isCorrect = "CORRECT";
    }
    else if(isCorrect === "PARTIAL"){
        isCorrect = "PARTIAL";
    }
    else{
        isCorrect = "INCORRECT";
    }
    var params= {isCorrect:isCorrect,qId:quesId,orgId:orgId};
    params["parentEntity"] = {type:"TEST",id:testId};
    vReq.get("/Tests/drawEachQuestionStudentsCorrectWrongList",params,function(data){
     var popup = showVPopup(0.6);
     popup.html(data);
     if(isCorrect == "CORRECT"){
        $("#popupHeading").html("Students that answered the question correct");
     }
     else if(isCorrect == "PARTIAL"){
        $("#popupHeading").html("Students that answered the question partially correct");
     }
     else{
        $("#popupHeading").html("Students that answered the question incorrect");
     }
    });
}