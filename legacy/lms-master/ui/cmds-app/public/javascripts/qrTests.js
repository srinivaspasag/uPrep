var qrTests = new (function($) {
    var clickEvent = "click", bodyClickEvent = "click.qrTests", testPage, testId, pageStr = "TEST_CREATION", testType;
    var addedQIds = [];
    var pageUrlParams = {
        orderBy: "timeCreated",
        includeTypes: "",
        courseId: "",
        query: "",
        start: 0,
        size: 25
    };
    this.init = function(params) {
        addedQIds = [];
        testType = params.testType;
        testPage = $("#testPage");
        this.testmetadata = params.metadata;
        testId = params.testId;
        this.testid = params.testId;

        if (history && history.state && history.state.prevUrl) {
            testPage.data("prevUrl", history.state.prevUrl);
        }

        qrTestsInitFormcWidget();

        // check for add color and publish status
        setInitPublishStatus();

        testPage.off(clickEvent)
                .on(clickEvent, ".viewTestStats", viewTestStats).on(clickEvent,
                ".previewTest", previewTest).on(clickEvent,
                ".saveTestState", saveTestState)
        // .on(bodyClickEvent,".publishTestOk",publishTestOk)
        // .on(bodyClickEvent,".publishAssignment",publishAssignment)

        $("body").off(bodyClickEvent).on(bodyClickEvent, ".removeQuesFromTest",
                removeQuesFromTest).on(bodyClickEvent, ".addQuesToTest",
                addQuesToTest)
                .on(bodyClickEvent, ".TPDSubDomain", TPDSubDomain)
                // .on(bodyClickEvent,".submitPublishTestOk",submitPublishTestOk)
                // .on(bodyClickEvent,".submitPublishAssignment",submitPublishAssignment)
                .on(bodyClickEvent, ".addScheduleToTest", addScheduleToTest)
                .on(bodyClickEvent, ".remScheduleEntity", remScheduleEntity)
                .on(bodyClickEvent, ".changeTestResultVisibility", changeTestResultVisibility)
                .on(bodyClickEvent, ".submitChangeTestResultVisibility", submitChangeTestResultVisibility)
                .on(clickEvent, ".addPdfPopup", addPdfPopup)
                .on(clickEvent, ".protectWithPassword", protectWithPassword)
                .on(clickEvent,".changeTestPassword",changeTestPassword)
        $("#testPage").on("click",".getTestQuesnsContent",getTestQuesnsContent)
                      .on("click",".getTestParaQuestions",getTestParaQuestions)
                      .on("click",".simplifySubjectNames",simplifySubjectNames)
                      .on("click",".checkBoxTestOptions",checkBoxTestOptions)
                      .on("click",".testSettingsToggle",testSettingsToggle)
        testPage.find(".testTitle").elipsifyText("big15 boldy");
        updateCheckBoxTestOptions();
        condenseText(testPage.find(".entityEditableDesc,.condenseText"), 100);
        testPage.find(".vChooseLevel").attr("data-param-name","includeDifficulty");
        if($("#testPage").hasClass("autoGenerate")){
            setTimeout(function(){
                previewTest();
                if($(".publishTest").text() == "Incomplete"){
                    showError("All questions were not found,please add remaining questions manually");
                }
            },1000);
        }
    };
    var startLoader = showTopLoader;
    var stopLoader = hideTopLoader;
    var closePopup = closecmdsPopup;
    var postReq = vReq.post;
    var getReq = vReq.get;

    var getTestQuesnsContent = function(){
        var quesType = $(this).data("questionType");
        var radioCourseId = $(".cmdsSearchSubsDiv .vRadioGrp").find(".vRadioChecked").data("value");
        var includeDifficulty = $(".vChooseLevel").find(".vChooseOptActive").data("value");
        if(quesType === "PARA"){
            $(".vChooseQuesSpecific").addClass("nonner");
            setvChooseValue($(".vChooseQuesSpecific"),-1);
        }
        else{
            $(".vChooseQuesSpecific").removeClass("nonner");
        }
        $("#fixedLeftSecPortion").removeClass("nonner");
        $(".vChooseLevel").removeClass("nonner");
        qrTestsInitFormcWidget(quesType,"",radioCourseId,includeDifficulty);
    }

    var getTestParaQuestions = function(){
        var quesType = $(this).data("type");
        if(quesType == "TEXT"){
            $("#fixedLeftSecPortion").addClass("nonner");
            $(".vChooseLevel").addClass("nonner");
            quesType = "PARA_QUES";
        }
        var paraId = $(this).data("qid");
        var radioCourseId = $(".cmdsSearchSubsDiv .vRadioGrp").find(".vRadioChecked").data("value");
        qrTestsInitFormcWidget(quesType,paraId,radioCourseId);
    }

    var simplifySubjectNames = function(){
        var testId = qrTests.testid;
        params = {
            cmdsTestId:testId
        }
        vReq.post("/QrTests/simplifyBoardNames", params , function(data){
            var popup = getcmdsPopupBody();
            popup.html(data);
            $(".getcmdsTestId").val(testId);
            popup.find(".simplifyBoardFormButtons").on("click",".submitSimplify",submitSimplifyBoard);
            popup.find(".ATTagTreeDiv").on("click",".ATTTRemoveSubjectTag",removeSimplifyBoard);
        });
    }

    var checkBoxTestOptions = function(){
        var enableMultipleOptions = [];
        var qTypes = [];
        var params = {
            testId:qrTests.testid,
            oneOrMoreMarksQTypes:{},
            enablePartialMarks:false,
            qTypes:{}
        };
        $("#enableMultipleOptions:checked").each(function(){
            enableMultipleOptions.push($(this).val());
        });
        $("#enablePartialMarks:checked").each(function(){
            qTypes.push($(this).val());
        });
        var valid = validateCheckBoxTestOptions(qTypes,enableMultipleOptions);
        if(valid == true){
            params.qTypes = qTypes;
            if(params.qTypes.length >0){
                params.enablePartialMarks = true;
            }
            params.oneOrMoreMarksQTypes = enableMultipleOptions;
            if(qTypes.length >0 || enableMultipleOptions.length > 0){
                vReq.post("/QrTests/enableOrDisablePartialMarks",params,function(data){
                     if(data.result.success === true){
                        showMessage("Successfully saved");
                     }
                }, function(data) {
                    $(".checkBoxTestOptionsHolder").html($(".checkBoxTestOptionsHolder").html());
                });;
            }
        }
    }

    var testSettingsToggle = function(e){
        $(".testOptionsSettings").finish();
        var disable = false;
        var subjectiveTest = $(".subjectiveTest").val();
        if(disable){
            return ;
        }
        openCloseGear();
        disable = true;
        function openCloseGear(){
           var valid = false;
           if($(".testSettingsToggle").hasClass("open")){
                $(".testSettingsToggle").removeClass("open");
                $(".testOptionsSettings").animate({opacity:0,"width":"0px","height":"0px"},"slow");
                $(".testOptionsSettings").addClass("nonner");
                $(".testSettingsToggle").addClass("close");
            }
            else if($(".testSettingsToggle").hasClass("close")){
                $(".testSettingsToggle").removeClass("close");
                $(".testOptionsSettings").removeClass("nonner");
                if(subjectiveTest === "true"){
                    $(".testOptionsSettings").animate({opacity:1,"width":"500px","height":"34px"},"slow");
                }else{
                    $(".testOptionsSettings").animate({opacity:1,"width":"625px","height":"34px"},"slow");
                }
                $(".testSettingsToggle").addClass("open");
            }
            valid = true;
            return valid;
        }
    }

    var validateCheckBoxTestOptions = function(qTypes,enableMultipleOptions){
        // console.log(enableMultipleOptions);
        // console.log(qTypes);
        var count =0;
        for(i=0;i<qTypes.length;i++){
            if(!enableMultipleOptions.includes(qTypes[i])){
                // console.log("Inside if");
                $("#enableMultipleOptions."+qTypes[i]).prop("checked","checked");
                enableMultipleOptions.push(qTypes[i]);
            }
        }
        // if(count > 0){
        //     showError("Please select more than one option, if partial marks is selected for question type");
        //     return false;
        // }
        return true;
    }

    var submitSimplifyBoard = function(){
        var simplifiedName = $("#simplifyBoardFormValue").val();
        var pattern = new RegExp("^[a-zA-Z0-9]*$");
        if(simplifiedName === ""){
            showError("Please enter subject name");
            return false;
        }
        else if(!pattern.test(simplifiedName)){
            showError("Please enter alphabets only");
            return false;
        }
        var brdIds =[];
        if($(".checkboxDiv input[type=checkbox]:checked").length === 0){
            showError("Please select a checkbox");
            return false;
        }
        $('.checkboxDiv input[type=checkbox]:checked').each(function () {
            brdIds.push($(this).data("brdId"));
        });
        var cmdsTestId = $(".getcmdsTestId").val();
        var params= {
            cmdsTestId:cmdsTestId,
            simplifiedBoards :{
                simplifiedName:simplifiedName,
                brdIds:brdIds
            }
        };
        vReq.post("/QrTests/addSimplifiedBoardNames",params,function(data){
            if(data.errorCode != ""){
                showError("Something went wrong");
                return false;
            }
            else{
                showMessage("Boards successfully simplified <br> Click again to simplify more boards");
                closecmdsPopup();
            }
        })
    }

    var removeSimplifyBoard = function(){
        $(this).closest(".ATTagTreeDiv").addClass("nonner");
        var cmdsTestId = $(".getcmdsTestId").val();
        var simplifiedName = $(this).data("simplifiedName");
        var brdInfo = $(this).data("brdInfo");
        var brdIds =[];
        for(i=0;i<brdInfo.length;i++){
            brdIds.push(brdInfo[i].boardId);
        }
        var params= {
            cmdsTestId:cmdsTestId,
            simplifiedBoards :{
                simplifiedName:simplifiedName,
                brdIds:brdIds
            }
        };
        vReq.post("/QrTests/removeSimplifiedBoardNames",params,function(data){
            if(data.errorCode != ""){
                showError("Something went wrong");
                return false;
            }
            else{
                showMessage("Simplified Board removed <br> Click again to remove more boards");
                closePopup();
            }
        })
    }


    var saveTestState = function() {
        // var postUrl = "/QrTests/saveAndAddLaterTest";
        // var pr = {};
        // if (testType == "ASSIGNMENT") {
        // postUrl = "/QrTests/saveAndAddLaterAssignment";
        // pr["assignmentId"] = testId;
        // } else {
        // pr["testId"] = testId;
        // }
        // $.post(postUrl, pr, function(data) {
        // });
        // var saveUrl = testPage.data("prevUrl");
        // if (!saveUrl) {
            saveUrl = $(this).data("backUpUrl");
        // }
        pushHistory(null, null, saveUrl);
        refreshPage();
    };
    var fillExistingQIds = function() {
        var qIds = [];
        var subs = qrTests.testmetadata;
        for (var k = 0; k < subs.length; k++) {
            var sub = subs[k];
            if (sub.qIds)
                $.merge(qIds, sub.qIds);
        }
        return qIds;
    };
    var setInitPublishStatus = function() {
        var testPageDetails = testPage.find(".testPageDetails");
        var testData = qrTests.testmetadata;
        for (var k = 0; k < testData.length; k++) {
            var courseId = testData[k].id;
            var currentSubEls = testPageDetails.find(".TPDSubCurrent_"
                    + courseId);
            addColorToCount(currentSubEls, testPageDetails
                    .find(".TPDSubTarget_" + courseId), "TPDSubTargetReached");

            var types = ["SCQ", "MCQ", "NUMERIC","PARA","MATRIX","SUBJECTIVE"];
            for (var p = 0; p < types.length; p++) {
                var qType = types[p];
                var currentQTypeEl = testPageDetails.find(".TPD" + qType
                        + "Current_" + courseId);
                addColorToCount(currentQTypeEl, testPageDetails.find(".TPD"
                        + qType + "Target_" + courseId),
                        "TPDQTypeTargetReached");
            }
        }
        checkPublishStatus(testPageDetails);
    };
    var qrTestsInitFormcWidget = function(quesType,paraId,courseId,includeDifficulty) {
        var urlParams = fetchUrlParams();
        var mcparams = {
            start: 0,
            size: 25,
            orderBy: "timeCreated",
            target: pageStr
                    /* ,excludeIds:fillExistingQIds() */
        };
        if(quesType === null || quesType === undefined || quesType === ""){
            mcparams.quesType = "NOT_PARA";
        }
        else{
            mcparams.quesType = quesType;
            pageUrlParams.quesType = quesType;
        }
        if(includeDifficulty != null && includeDifficulty != undefined && includeDifficulty != "" && includeDifficulty!=-1){
            mcparams.includeDifficulty = includeDifficulty;
        }
        if(addedQIds.length === 0){
            addedQIds = fillExistingQIds();
        }
        $.extend(mcparams, urlParams);
        startLoader();
        var mcWidget = testPage.children(".mcWidget");
        if(paraId === null || paraId === undefined || paraId === ""){
        }
        else{
            pageUrlParams.paraId = paraId;
            mcparams.paraId = paraId;
        }
        qrmcWidgetUtil.loadRadioSubs(mcWidget, pageStr);
        var vRadioGrp = mcWidget.find(".cmdsSearchSubsDiv .vRadioGrp");
        var vRadios = vRadioGrp.find(".vRadio");
        vRadios.eq(0).remove();
        if (vRadios.length > 1) {
            var paramCourseId = urlParams.courseId;
            if (paramCourseId) {
                var initialvRadio = vRadioGrp.find(".sub_" + paramCourseId);
                if (initialvRadio.length > 0) {
                    changeActiveClass(initialvRadio, "vRadioChecked");
                } else {
                    // alert("The course mentioned in the url is not part of
                    // this test.");
                    window.location = window.location.pathname;
                }
            } else {
                if(courseId === null || courseId === undefined || courseId === ""){
                    vRadios.eq(1).addClass("vRadioChecked");
                    mcparams.courseId = vRadios.eq(1).data("value");
                }
                else{
                    changeActiveClass(vRadioGrp.find(".sub_"+courseId),"vRadioChecked");
                    mcparams.courseId = courseId;
                }
            }
            vRadioGrp.data("value", mcparams.courseId);
            qrmcWidgetUtil.loadTopics(mcWidget, vRadioGrp, false);
            var brdIds = mcWidget.data("params").brdIds;
            if (brdIds.length === 0) {
                mcparams.brdIds = [mcparams.courseId];
            } else {
                if(courseId === null || courseId === undefined || courseId === ""){
                    mcparams.brdIds = brdIds;
                }
                else{
                    mcparams.courseId = courseId;
                    mcparams.brdIds = [mcparams.courseId];
                }
            }
        }
        initmcWidgetforCMDS(mcWidget, "/qrTests/getTestQuesns", mcparams, true,
                true, afterQuestionFetch);
        //Removing params from url
        pageUrlParams = {};
        mcWidget.data("pageUrlParams", pageUrlParams);
        mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
        updatemcWidgetParamHolders(mcWidget, urlParams);
        if (urlParams.includeTypes) {
            setvChooseValue(mcWidget.find(".vChooseQuesType"),
                    urlParams.includeTypes);
        }
    };
    var aftermcWidgetContentLoadedForQuesns = function(mcWidget) {
        var currentUrlParams = fetchUrlParams();
        var finalParams = getFinalUrlParamsForPage(mcWidget);
        if ($.isEmptyObject(currentUrlParams)) {
            var returnLocation = history.location || document.location;
            var path = returnLocation.pathname + "?" + $.param(finalParams);
            history.replaceState(null, null, path);
        } else {
            pushNewUrlParams(finalParams);
        }
        fixContentSec();
    };
    var afterQuestionFetch = function() {
        var holder = testPage.find(".cmdsQuesnsDiv");
        $(addedQIds).each(
                function(index, value) {
                    var addedQues = holder.find(".ques_" + value);
                    var r = addedQues.find(".addQuesToTest").removeClass(
                            "addQuesToTest").addClass("removeQuesFromTest")
                            .text("Remove");
                });
    };
    var updateCheckBoxTestOptions = function(){
        // console.log("Inside updateCheckBoxTestOptions");
        var partialMarksQTypes = $(".checkBoxTestOptionsHolder").data("partialMarksQTypes");
        var oneOrMoreMarksQTypes = $(".checkBoxTestOptionsHolder").data("oneOrMoreMarksQTypes");
        // console.log(partialMarksQTypes);
        if(partialMarksQTypes != undefined){
           for(i=0;i<partialMarksQTypes.length;i++){
                $("#enablePartialMarks."+partialMarksQTypes[i]).prop("checked","checked");
           }
        }

        if(oneOrMoreMarksQTypes != undefined){
            for(i=0;i<oneOrMoreMarksQTypes.length;i++){
                $("#enableMultipleOptions."+oneOrMoreMarksQTypes[i]).prop("checked","checked");
           }
        }
        else{
            $("#enableMultipleOptions.MCQ").prop("checked","checked");
        }
    };
    // adding quesitons
    var addQuesToTest = function() {
        addRemoveQuesToTest("INCREASE", $(this));
    }
    var removeQuesFromTest = function() {
        addRemoveQuesToTest("DECREASE", $(this));
    }
    var addRemoveQuesToTest = function(addRemType, $this) {
        var urlStrip = "removeQuesFrom" + toCamelCase(testType);
        var runText = "Removing..", removeText = "Remove", addText = "Add", beforeText = removeText, afterText = addText;
        if (addRemType === "INCREASE") {
            urlStrip = "addQuesTo" + toCamelCase(testType);
            runText = "Adding..";
            beforeText = addText;
            afterText = removeText;
        }
        var ques = $this.closest(".ques");
        startLoader();
        $this.text(runText);
        var qId = ques.data("qid");
        var params = {
            testId: testId,
            qId: qId,
            assignmentId: testId
        };
        var successFn = function(data) {
            stopLoader();
            $this.text(afterText).toggleClass(
                    "addQuesToTest removeQuesFromTest")
            var topicId = ques.find(".quesTopic").data("brdId");
            var courseId = ques.find(".quesCourse").data("brdId");
            var qType = ques.data("type");
            changeCount(courseId, topicId, qType, addRemType);
            var previewTestPage = $("#previewTestPage");
            if (previewTestPage.length > 0) {
                changeCount(courseId, topicId, qType, addRemType, pageStr
                        + "_PREVIEW");
            }
            addedQIds.push(qId);
            var mcWidget = testPage.children(".mcWidget");
            if (mcWidget.length > 0) {
                addedQIds.pop(qId);
                // setRemoveQIdInExcludeIds(qId,mcWidget.data("params").excludeIds,addRemType);
                setRemoveQIdInExcludeIds(qId, addedQIds, addRemType);
            }
        };
        var errorFn = function() {
            $this.text(beforeText);
        };
        postReq("/qrTests/" + urlStrip, params, successFn, errorFn);
    };
    var setRemoveQIdInExcludeIds = function(qId, excludeIds, addRemType) {
        if (addRemType === "INCREASE") {
            pushIfAbsent(excludeIds, qId);
        } else {
            var index = excludeIds.indexOf(qId);
            if (index > -1) {
                excludeIds.splice(index, 1);
            }
        }
    };
    var changeCount = function(courseId, topicId, qType, incDec, target) {
        var targetFn = increaseCount;
        if (incDec == "DECREASE") {
            targetFn = decreaseCount;
        }
        var testPageDetails = testPage.find(".testPageDetails");
        if (target === pageStr + "_PREVIEW") {
            testPageDetails = $("#previewTestPage")
                    .children(".testPageDetails");
        }

        // current sub total increase/decrease
        var currentSubEls = testPageDetails.find(".TPDSubCurrent_" + courseId);
        targetFn(currentSubEls.eq(0));
        targetFn(currentSubEls.eq(1));

        // earlier this was only for tests but now included for assignments
        addColorToCount(currentSubEls, testPageDetails.find(".TPDSubTarget_"
                + courseId), "TPDSubTargetReached");

        // topics
        targetFn(testPageDetails.find(".TPDTopicCurrent_" + topicId).eq(0));

        // scqs,mcqs and numeric
        var currentQTypeEl = testPageDetails.find(".TPD" + qType + "Current_"
                + courseId);
        targetFn(currentQTypeEl.eq(0));
        addColorToCount(currentQTypeEl, testPageDetails.find(".TPD" + qType
                + "Target_" + courseId), "TPDQTypeTargetReached");

        checkPublishStatus(testPageDetails);
    }
    var addColorToCount = function(currentEls, targetEl, activateClass) {
        var currentCount = parseInt(currentEls.eq(0).text());
        var targetCount = parseInt(targetEl.text()), colorClass = "";
        if (targetCount == currentCount)
            colorClass = "greenColor " + activateClass;
        else if (currentCount > targetCount)
            colorClass = "redColor";
        currentEls.removeClass("greenColor redColor " + activateClass)
                .addClass(colorClass);
    }
    var checkPublishStatus = function(testPageDetails) {
        // check for publishing
        var subTarget = false, qTypesTarget = false;
        var testmetadata = qrTests.testmetadata;
        var qTypeLength = $(".TPDQTypeTable tr").length / testmetadata.length;
        if ((testPageDetails.find(".TPDSubTargetReached").length / 2) == testmetadata.length) {
            subTarget = true;
        }
        if (testPageDetails.find(".TPDQTypeTargetReached").length == testmetadata.length * qTypeLength) {
            qTypesTarget = true;
        } else if (testType === "ASSIGNMENT") {
            qTypesTarget = true;
        }
        var publishClass = "publishTest", testBtn = testPageDetails.find("."
                + publishClass);
        if (subTarget && qTypesTarget) {
            testBtn.addClass(publishClass + "Ok").text("Complete");
        } else
            testBtn.removeClass(publishClass + "Ok").text("Incomplete");
    }

    // others
    var viewTestStats = function() {
        var targetHTML = $(this).closest(".testPageDetails").find(
                ".testPageMoreDet").children().clone(true);

        getcmdsPopupBody().html(targetHTML);
    };
    var previewTest = function() {
        startLoader();
        var successFn = function(data) {
            stopLoader();
            var popup = getcmdsPopupBody(1002).html(data);
            var closeEl = popup.closest("#cmdsPopup").find(".closecmdsPopup");
            closeEl.data("closecmdsPopupcbfn", testPreviewClosedCbfn);
        };
        var params = {
            testId: testId,
            brdId: qrTests.testmetadata[0].id,
            start: 0,
            size: 50,
            target: pageStr + "_PREVIEW",
            assignmentId: testId
        };
        getReq("/qrtests/preview" + toCamelCase(testType), params, successFn);
    };
    var TPDSubDomain = function() {
        var $this = $(this);
        changeActiveClass($this, "gButtonActive");
        $this.closest(".cmdsPopupBody").find(".TPDSubDomainDiv").eq(
                $this.index()).removeClass("nonner").siblings(
                ".TPDSubDomainDiv").addClass("nonner");
    };
    var publishTestOk = function() {
        var popup = getcmdsPopupBody(null, null, {
            submitClass: "submitPublishTestOk"
        });
        popup.html(publishTestOkSample.children().clone(true));
    };
    var addScheduleToTest = function() {
        var entity = $(this).siblings(".scheduleSample").html();
        $(
                "<tr class='scheduleEntityTr'><td colspan=2 class=pad10Top>"
                + entity + "\n\
        </td></tr>").insertBefore(
                $(this).closest("tr"));
    }
    var remScheduleEntity = function() {
        $(this).closest("tr").remove();
    }
})(jQuery);
var onTestTypeChange = function(vChoose, value) {
    var scheduleTr = vChoose.closest("table").find(".addScheduleTr");
    if (value == "ONLINE_TEST") {
        scheduleTr.addClass("nonner");
    } else if (value == "LAB_TEST") {
        scheduleTr.removeClass("nonner");
    }
};
var onTestScopeChange = function(vChoose, value) {
    var targetText = vChoose.siblings(".prdtScope");
    var addDiv = vChoose.closest("table").find(".addScheduleTr");
    if (value == "PUBLIC") {
        targetText.removeClass("nonner");
        addDiv.addClass("nonner");
    } else {
        targetText.addClass("nonner");
        addDiv.removeClass("nonner");
    }
};
var testPreviewClosedCbfn = function() {
    var mcWidget = $("#testPage").children(".mcWidget");
    if (mcWidget.length > 0) {
        manageContent.loadmcContent(mcWidget);
    }
};
// Protect Test with password

var protectWithPassword = function(){
    vReq.get("/QrTests/testPasswordPopup" , null, function(data) {
        var popup = getcmdsPopupBody();
        popup.html(data);
        popup.on("click",".setPassword",setPassword)
    });
};

var changeTestPassword = function(){
    vReq.get("/QrTests/testPasswordPopup" , null, function(data) {
        var popup = getcmdsPopupBody();
        popup.html(data);
        popup.on("click",".setPassword",setPassword)
        var testPasswordPopup = popup.find(".testPasswordPopup");
        var testPassword = $("#showTestPassword").data('id');
        var testResultPassword = $("#showTestResultPassword").data('id');
        testPasswordPopup.find("#passwordForTest").val(testPassword);
        testPasswordPopup.find("#passwordForTestResult").val(testResultPassword);
    });
};

var setPassword = function(){
    var $this = $(this);
    if($this.hasClass("disableBtn")){ return false; }
    $this.addClass("disableBtn");
    var popup = $this.closest(".testPasswordPopup");
    var testId = qrTests.testid;
    var password = popup.find("#passwordForTest").val().trim();
    var resultPassword = popup.find("#passwordForTestResult").val().trim();
    var params = {
        testId: testId,
        password: password,
        resultPassword: resultPassword
    }
    showTopLoader();
    vReq.post("/QrTests/savePasswordForTest",params,function(data){
        if(data.result.success === true){
            $this.removeClass("disableBtn");
            closePopup();
            refreshPage();
            showMessage("Successfully set password to this test");
        }
        else{
            showError("Something went wrong");
        }
    },function(){
        $this.removeClass("disableBtn");
    });
}

//Add PDF to Tests

var addPdfPopup = function() {
    var parDiv;
    var myParams = {
        "commitUrl": "/QrTests/addPdfToTest",
        "parDiv": "addResourcePopupContainer",
        "type": "CMDSDOCUMENT",
        "typeInText": "Document",
        "mediaType": "DOC",
        "fileTypes": ["doc", "docx", "pdf", "ppt", "pptx"],
        "maxFileSize": 50 * 1024 * 1024, /*50 MB*/
        "btnText": "Choose Document File",
        "btnClass": "blueButton",
        "allowMimeTypes": [],
        //"application/pdf","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation"],    
        "fileCommitId": "docId"
    };
    var popup = getcmdsPopupBody(480);
        smallLoader(popup);
        vReq.get("/QrTests/addPdfToTestPopup", {}, function(data) {
            popup.html(data);
            try {
                if (myParams.cbFns && myParams.cbFns.onPopupOpen) {
                    myParams.cbFns.onPopupOpen();
                }
            } catch (err) {
            }
            regFns();
            fetchScripts([{fname: "corsUploader.js", cb: uploaderJsLoaded}]);
        });
    var regFns = function() {
        parDiv = popup.find("#addResourcePopupContainer");
        parDiv.on("click", "#doneAddResourcePopup", commitResourcePopup);
    };
    var uploaderJsLoaded = function() {
        parDiv = popup.find("#addResourcePopupContainer");
        var pr = {
            type: myParams["type"],
            mediaType: myParams["mediaType"],
            cbFns: {
                'onFileChoosen': function(file) {
                    try {
                        if (myParams.cbFns && myParams.cbFns.onFileChoosen) {
                            myParams.cbFns.onFileChoosen(file);
                        }
                    } catch (err) {
                    }
                },
                'onUrlSigned': function(file, params, data) {
                    corsData = data;
                },
                'onProgress': function() {
                },
                'onComplete': function(id, fileName, success, response, statusCode) {
                    if (success) {
                        parDiv.find("#addResourcePopupDiv").data("uploadedResourcePopupInfo", corsData);
                    }
                },
                'onCancel': function() {
                },
                'onReUpload': function() {
                    parDiv.find("#addResourcePopupDiv").data("uploadedResourcePopupInfo", undefined);
                    corsData = {};
                    try {
                        if (myParams.cbFns && myParams.cbFns.onReload) {
                            myParams.cbFns.onReload();
                        }
                    } catch (err) {
                    }
                },
                'beforeSend': function() {
                }
            },
            fileTypes: myParams["fileTypes"],
            maxFileSize: myParams["maxFileSize"],
            btnText: myParams["btnText"],
            btnClass: myParams["btnClass"],
            allowMimeTypes: myParams["allowMimeTypes"]
        };
        corsUploader.init(parDiv, popup.find(".uploadResourcePopupDiv"), pr);
    };
    var commitResourcePopup = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
        var videoParams = getResourcePopupInfo();
        if (!videoParams) {
            showError("Unable to add " + myParams["typeInText"] + ", the required file is not uploaded yet!");
            return;
        }
        if (!videoParams.name) {
            showError("Please provide with a title to the " + myParams["typeInText"]);
            return;
        }
        var testId = qrTests.testid;
        var params = {"testId": testId};
        params = $.extend(true, params, videoParams);
        //params.brdIds = [qrTests.testmetadata[0].id , qrTests.testmetadata[0].children[0].id]
        startLoader();
        $this.addClass("btnDisabled");
        vReq.post(myParams["commitUrl"], params, function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            closePopup();
            setTimeout(function() {
                refreshPage();
            }, 1200);
        }, function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            var errorCode = data.errorCode;
            var errorMessage = data.errorMessage ? data.errorMessage : data.errorCode;
            showError("Unable to add " + myParams["typeInText"] + " Error :: " + errorMessage);
        });
    };
    var getResourcePopupInfo = function() {
        var holder = parDiv.find("#addResourcePopupDiv");
        var data = holder.data("uploadedResourcePopupInfo");
        if (!data || !data.id) {
            return;
        }
        var idd = myParams["fileCommitId"];
        var info = {
            name: data.fileName,
            type: "UPLOADED",
            description: data.fileName,
            originalFileName: data.fileName,
            uploadedFileName: data.requestParams.key,
            uuid: data.uuid
        };
        info[idd] = data.id;
        return info;
    };
};
