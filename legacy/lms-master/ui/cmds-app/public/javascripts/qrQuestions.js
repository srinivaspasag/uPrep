var qrQuestions = new (function($) {
    var clickEvent = "click", bodyClickEvent = "click.qrQuestions", statusPublished = "PUBLISHED";
    var curEditQuesParams=null;
    this.init = function(params) {
        params = params || {};
        //adding subtopics
        $("body").off(bodyClickEvent)

                //check opts
                .on(bodyClickEvent, ".addQuesnsToTest", addQuesnsToTest)
                .on(bodyClickEvent, ".submitAddQuesnsToTest", submitAddQuesnsToTest)
                .on(bodyClickEvent, ".postAsChallenge", postAsChallenge)
                .on(bodyClickEvent, ".postAsChallengeSubmit", postAsChallengeSubmit)
                .on(bodyClickEvent, ".editQuesTags", editQuesTags)
                .on(bodyClickEvent, ".submitEditQuesTags", submitEditQuesTags)
		.on(bodyClickEvent, ".cancelEditQues",cancelEditQues)
		.on(bodyClickEvent, ".QAEditSubmitConfirm",editSubmitConfirm)
        // var url=window.location.href;
        // if(url.indexOf("quesType")>-1){
        //     var questionType = getURLParameter("quesType");
        //     if(questionType == "PARA_QUES"){
        //         $("#fixedLeftSecPortion").addClass("nonner");
        //     }
        // }
        if (params.target === "QUESTIONSET") {
            loadMJEqns(cSecHolder.get(0));
            params = params || {};
            params.start = 0;
            params.size = 25;
            var urlParams = fetchUrlParams();
            $.extend(params, urlParams);
            var mcWidget = $("#questionSetPage");
            initmcWidgetforCMDS(mcWidget, "/qrquestions/getQuestionSetQuesns",
                    params, false, true);
            mcWidget.data("pageUrlParams", {start: 0, size: 50, questionSetName: urlParams.questionSetName});
            mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
        }


        fetchScripts([{fname: "uicomWidgets/tagging.js"}, {fname: "uicomWidgets/rte.js"}]);
        fixContentSec();
        questionOptionsDiv();
    };
    var startLoader = showTopLoader;
    var stopLoader = hideTopLoader;
    var closePopup = closecmdsPopup;
    var resetChecks = resetcmdsCBoxes;
    var getReq = vReq.get;
    var postReq = vReq.post;
    var checkedEntitiesClone;


    //utilities 
    var loadQuesnsPopup = function(data, cbfn) {
        var targetDiv = makeHTMLTag("div").html(checkedEntitiesClone.entityCloneList);
        var qMap = data.result, statusHTML = makeHTMLTag("div");
        for (var k = 0; k < qMap.length; k++) {
            var id = qMap[k].qid, status = qMap[k].errorCode, tr = makeHTMLTag("tr");
            if (status == "UPDATE_FAILED") {
                status = "Either this question is already added to the test,the question\n\
                 limit of the test exceeded or this question's type does not permit the addition."
            }
            if (status == "") {
                status = "<span class='greenColor boldy'>Done!!</span>";
            } else {
                status = "<span class='redColor boldy'>" + status + "</span>";
            }
            var ques = targetDiv.find(".ques_" + id);
//            if(status==statusPublished){
//                ques.find(".quesStatus").data("status",statusPublished)
//                .text(statusPublished);
//            }
            if (cbfn) {
                cbfn();
            }
            tr.html("<td>" + ques.find(".quesTextDivHolder").html() + "</td>");
            tr.append("<td>" + status + "</td>");
            statusHTML.append(tr);
        }
        var p = getcmdsPopupBody(800).html(entityClonesPopup.children().clone(true));
        p.find("tbody").html(statusHTML.children());
    };
    var changeQuesStatus = function(ques, finalStatus) {
        var statusEl = ques.find(".quesStatus");
        ques.data("status", finalStatus);
        statusEl.text(finalStatus).removeClass()
                .addClass("quesStatus quesStatus" + finalStatus);
        if (finalStatus === statusPublished) {
            ques.find(".editcmdsQues,.editQuesTags").remove();
        }
    };

    var questionOptionsDiv = function(){
        $(".cmdsTableSortDiv").find(".vChooseLevel").attr("data-param-name","includeDifficulty");
        var otherQuesType = $(".otherQuesType").data("otherQuesType");
        var qType = $(".quesTailTable").data("qType");
        if(otherQuesType === "PARA"){
            $(".questionFilter").removeClass("nonner");
            $(".questionType").removeClass("nonner");
            $("#fixedLeftSecPortion").removeClass("nonner");
        }else if(otherQuesType === "NOT_PARA"){
            $(".questionFilter").removeClass("nonner");
            $(".questionType").addClass("nonner");
            $("#fixedLeftSecPortion").removeClass("nonner");
        }
        else if(otherQuesType === "ALL" && (qType === "PARA_QUESTION" || qType === null)){
            $(".questionFilter").addClass("nonner");
            $("#fixedLeftSecPortion").addClass("nonner");
        }
    }


    //check box options
    var postAsChallenge = function() {
        checkedEntitiesClone = checkedEntities.init();
        var ques = checkedEntitiesClone.entityList[0];
        if (checkedEntitiesClone.hasPublished) {
            showcmdsError("You cannot add published question as a challenge.<br>\n\
            Uncheck the published question and try again");
            return;
        } else if (checkedEntitiesClone.entityIds.length > 1) {
            showcmdsError("Please select a single question. You can only post one\n\
             challenge at a time.");
            return;
        } else if (ques.data("type") === "PARA") {
            showcmdsError("You cannot post a paragraph question as challenge.");
            return;
        }

        var popup = fillcmdsPopup("postAsChallengeSubmit", "postChlgSample");
        startLoader();
        getReq("/qrchannels/channelsvChoose", {}, function(data) {
            stopLoader();
            popup.find(".channelsvChooseDiv").html(data);
        });
        popup.closest("#cmdsPopup").width(700);
        var hints = ques.find(".quesHints .chlgHint");
        if (hints.length > 0) {
            var targetTr = popup.find(".popupQuesHintsTr").removeClass("nonner");
            targetTr.find("td:last").html(hints.clone(true));
        }
    };
    var postAsChallengeSubmit = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
        var popup = $this.closest("#cmdsPopup");
        var params = getFormValues(popup);
        params.duration = getHrsMinsSecs(popup.find(".chlgDuration")) / 1000;
        params.lifeTime = getHrsMinsSecs(popup.find(".chlgLifeTime")) / 1000;
        params.hintsDeduction = [];
        popup.find(".chlgHintDeducVal").each(function() {
            params.hintsDeduction.push($(this).val());
        });
        if (params.hasError) {
            return;
        } else if (params.duration <= 0 || params.lifeTime <= 0 ||
                (params.hintsDeduction.length == 0 && popup.find(".chlgHintDeducVal").length > 0)) {
            showcmdsPopupError(popup);
            showError("Please select proper values for lifetime , duration and hint values(If applicable).");
            return;
        } else if (params.maxBid <= 0) {
            showError("Maximum Points can not be equal to or less than '0'");
            return;
        }
        if (checkedEntitiesClone.entityIds.length == 0) {
            showcmdsPopupError(popup, "Please select a question.");
            return;
        }
        params.questionId = checkedEntitiesClone.entityIds[0];
        params.entities = checkedEntitiesClone.srcEntities;
        startLoader();
        var successFn = function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            closePopup();
            resetChecks();
            trackEventForGA("CMDSCHALLENGE", "ADD_CONTENT", params.name);
            changeQuesStatus(checkedEntitiesClone.entityList[0], statusPublished);
        };
        $this.addClass("btnDisabled");
        postReq("/qrquestions/publishChallenge", params, successFn, function() {
            $this.removeClass("btnDisabled");
        });
    };
    var addQuesnsToTest = function() {
        checkedEntitiesClone = checkedEntities.init();
        var qids = checkedEntitiesClone.entityIds;
        if (qids.length > 1) {
            showError("You can only add one question at a time.");
            return;
        }
        var popup = getcmdsPopupBody(null, null, {submitClass: "submitAddQuesnsToTest"})
        inlineLoader(popup);
        var params = {start: 0, size: 25, published: false, resultType: "ALL"};
        var url = "/qrquestions/addQuesnsToTestList";
        var successFn = function(data) {
            popup.html(data);
            var listDiv = popup.find(".entityListDiv");
            if (listDiv.length > 0) {
                loadMoreEntities.init(listDiv, url, params, null, "HTML");
            }
        };
        getReq("/qrquestions/addQuesnsToTest", params, successFn);
    };
    var submitAddQuesnsToTest = function() {
        var popup = $(this).closest("#cmdsPopup");
        var params = getFormValues(popup);
        params.qId = checkedEntitiesClone.entityIds[0];
        startLoader();
        var successFn = function(data) {
            resetChecks();
            stopLoader();
            closePopup();
//            loadQuesnsPopup(data);
        };
        postReq("/qrTests/addQuesToTest", params, successFn);
    };




    //edit questions
    var questionForTagsEditing;
    var editQuesTags = function() {
        startLoader();
        questionForTagsEditing = $(this).closest(".ques");
        var successFn = function(data) {
            stopLoader();
            var popup = getcmdsPopupBody(800, null, {submitClass: "submitEditQuesTags"});
            popup.html(data);
            //Disable subject and topic dropdowns
            var publishStatus = questionForTagsEditing.data("status");
            if(publishStatus == "PUBLISHED"){
                popup.find(".ATSelectBoxHead").css("pointer-events","none");
                popup.find(".ASTHolder").css("pointer-events","none");
                popup.find(".ATTTRemoveTag").css("pointer-events","none");
            }
        };
        getReq("/qrquestions/editQuesTags", {id: $(this).data("qid")}, successFn);
    };
    var submitEditQuesTags = function() {
        var popup = getPopupDiv($(this));
        var tagsJson = returnAllTagsAdded(popup.find("#addTagsMasterDiv"));
        var qData = $("#editQuesTagsPage").data();
        var params = {questionId: qData.qid, type: qData.type,
            "entity.type": "CMDSQUESTION", "entity.id": qData.qid};
        if (tagsJson.subjectIds.length == 0 || tagsJson.topicIds.length == 0) {
            showError("Please select atleast one Subject and Topic");
            return;
        }
        params.targetIds = tagsJson.targetIds;
        params.tags = tagsJson.normalTags;
        params.brdIds = tagsJson.brdIds;
        var difficulty = popup.find(".QALevelSelect").data("value");
        if (difficulty == "-1") {
            showError("Please provide the difficulty");
            return;
        }
        params.difficulty = difficulty;
        params.updateList = ["targetIds", "tags", "brdIds", "difficulty"];
        startLoader();
        var successFn = function(data,text,xhr) {
            stopLoader();
	    var respType = xhr.getResponseHeader("Response-Type");
            if(respType == "html"){
                    var popup = showVPopup();
                    popup.html(data);
                    return;
            }
            closePopup();
            putQuesTagsAfterEditing(tagsJson, difficulty);
        };
	curEditQuesParams = params;
        postReq("/qrquestions/editquestion", params, successFn);
    };
    var cancelEditQues = function(){
            closePopup();
    };
    var editSubmitConfirm = function(){
	if(!curEditQuesParams) return;
	var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
	var params = curEditQuesParams;
        params.editEntities = [];
        $(".eachEditQuesAssociation").each(function(index,div){
                var $this = $(this);
                var editEntity = {entity:{id:$this.data("id"),type:$this.data("type")}};
                editEntity.editType = $(this).find(".check").val();
                params.editEntities.push(editEntity);
        });
	$this.addClass("btnDisabled");
        showTopLoader();
        vReq.post("/QrQuestions/editQuestion",params,function(data,nn,xhr){
		$this.removeClass("btnDisabled");
                hideTopLoader();
                var respType = xhr.getResponseHeader("Response-Type");
                if(respType == "html"){
                        var popup = showVPopup();
                        popup.html(data);
                        return;
		}else{
			closeVPopup();
                }
                if(data && data.result){
                        cancelEditQues();
                        showMessage("Tags for question update successfully!");
                }else{
                        showError("Failed to edit tags!");
                }
        },function(){
		$this.removeClass("btnDisabled");
                hideTopLoader();
	});
    };
    var putQuesTagsAfterEditing = function(tagsJson, difficulty) {
        var ques = questionForTagsEditing;
        try {
            ques.find(".quesCourse").text(tagsJson.subjects[0])
                    .data("brdId", tagsJson.subjectIds[0]);
            ques.find(".quesTopic").text(tagsJson.topics[0])
                    .data("brdId", tagsJson.topicIds[0]);
            var topicsAndSubTopics = $.merge(tagsJson.topics, tagsJson.subTopics);
            ques.find(".quesTopicsDiv").html(topicsAndSubTopics.join(","));
            ques.find(".quesNormalTagsDiv").html(tagsJson.normalTags.join(","));
            ques.find(".quesLevel").html(difficulty);
        } catch (err) {
        }
        ;
    };
})(jQuery);
var publishcmdsQuesns = function(vChoose, value) {
    qrQuestions.publishQuesns(vChoose, value);
};
var cmdsCheckOptsCbQUESTION = function(optsDiv) {
    var cBoxes = cmdsCBoxesChecked;
    var hasShare = true;
    cBoxes.each(function() {
        var ques = cBoxes.closest(".ques");
        var shareable = ques.children(".quesShareStatus").data("shareable");
        if (shareable == "false" || !shareable) {
            hasShare = false;
        }
    });
    var inacClass = "gButtonInactive", acClass = "gButton";
    var shareBtn = optsDiv.find(".sharecmdsQuesns");
    if (hasShare)
        shareBtn.removeClass(inacClass).addClass(acClass);
    else
        shareBtn.addClass(inacClass).removeClass(acClass);
};
var normalquesnsOptsCbfn = function() {

};




