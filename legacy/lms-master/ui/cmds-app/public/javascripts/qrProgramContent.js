var qrProgramContent = new (function($) {
    var programContent, clickEvent = "click", bodyClickEvent = "click.qrProgramContent", pageHeadDiv;
    this.init = function(params) {
        programContent = $("#programContent");
        pageHeadDiv = $("#pageHeadDiv");
        programContent.off(clickEvent)
                .on(clickEvent, ".makeVisible", makeVisible)
                .on(clickEvent, ".orderContentSectionLibrary", reorderContent.execute)
                .on(clickEvent,".addToClassroomConnect",addToClassroomConnect)
                .on(clickEvent,".scheduleTestButton",scheduleTestButton)


        $("body").off(bodyClickEvent)
                .on(bodyClickEvent, ".showToManySections", showToManySections)
                .on(bodyClickEvent, ".resumeMakeVisibleOfContents", resumeMakeVisibleOfContents);


        var mcWidget = programContent.closest("#programPage");
        var afterSubsLoadCbfn = null;
        params = params || {};
        params.target = "LIBRARY_RESOURCES";
        var mcParams = $.extend({orderBy: "timeCreated"}, params);
        if (params.courseId) {
            var courseId = params.courseId;
            mcParams.brdIds = [courseId];
            mcParams.courseId = courseId;
            afterSubsLoadCbfn = function() {
                var vRadioGrp = mcWidget.find(".cmdsSearchSubsDiv").children(".vRadioGrp");
                vRadioGrp.data("value", courseId);
                changeActiveClass(vRadioGrp.find(".sub_" + courseId), "vRadioChecked");
                qrmcWidgetUtil.loadTopics(mcWidget, vRadioGrp, false);
            };
        }
        var urlParams = fetchUrlParams();
        $.extend(mcParams, urlParams);
        initmcWidgetforCMDS(mcWidget, "/qrprograms/libraryTable",
                mcParams, false, true, afterLoad);
        mcWidget.data("pageUrlParams", qrProgram.contentUrlParams);
        mcWidget.data("changeUrlAfterLoad", qrProgram.afterContentLoaded);
        updatemcWidgetParamHolders(mcWidget, urlParams, "LIBRARY");


        var targetDivForSubs = mcWidget.find(".cmdsSearchSubsDiv");
        inlineLoader(targetDivForSubs);
        $.get("/qracadstr/getProgramCourses", {programId: params.programId}, function(data) {
            var subs = data.result.list;
            qrmcWidgetUtil.prepareRadioSubsHTML(subs, targetDivForSubs, afterSubsLoadCbfn);
        });
        fixContentSec();
        calcTitleMaxWidth();
    };

    var getReq = vReq.get;
    var postReq = vReq.post;
    var resetChecks = resetcmdsCBoxes;
    var orgEntitiesForMakeVisible = [];
    var publishTimer;
    var publishJobIdList = [];
    var cloneTableBody;
    var publishedVar = "PUBLISHED";
    var makeVisibleContentsSuccessFn;
    var makeVisibleContentsOpType;
    var downloadableEntities = [];
    var moduleCheckedList = 0;
    var moduleContentsList = 0;

    var afterLoad = function() {
        calcTitleMaxWidth();
        reorderContent.postMCLoad();
    };
    var calcTitleMaxWidth = function() {
        var tableHead = programContent.find(".headSecTable");
        var tableBody = programContent.find(".cmdsTable");
        var entityNameWidth = tableHead.find(".itemTitleTH").width() - 10;
        tableBody.find(".entityNameDiv").width(entityNameWidth)
                .find(".singleLineText").css("max-width", entityNameWidth - 30);
        //Clearing timers on page Load.
        clearAllTimers();
        setTimerForScheduleTest();
    };
    var setTimerForScheduleTest = function(){
        if($(".headSecTableDiv").hasClass("sectionLibraryTable")){
            var timeHolder = $(".scheduleTimer");
            timeHolder.each(function(){
                var remainingTime = $(this).data("timeBlockIn")/1000;
                var timer = $(this);
                remainingTime = parseInt(remainingTime,10);
                var x = setInterval(function(){
                    if (remainingTime < 60) {
                        clearInterval(x);
                        timer.closest(".entityRemainingTimeBlock").remove();
                        return ;
                    }
                    remainingTime = remainingTime - 60;
                    var hours = Math.floor(remainingTime/3600);
                    var preHours = hours;
                    if(hours < 10){
                        preHours = "0" + hours;
                    }
                    var minutes = ("0"+Math.floor((remainingTime-hours*3600)/60)).slice(-2);
                    timer.text(preHours+":"+minutes+ " hrs");
                },60*1000);
            });
        }
    };

    var addToClassroomConnect = function(){
        var checkCBoxes = $.extend(true, {}, checkedEntities.init("CHECK_VISIBILITY"));
        var unpublishedContents = checkCBoxes.unpublishedContents;
        var invisibleContents = checkCBoxes.invisibleContents;
        var visibleContents = checkCBoxes.visibleContents;
        if(unpublishedContents.length != 0){
            showError("Please publish content before adding to classroom connect");
            return ;
        }
        if(invisibleContents.length != 0){
            showError("Please visible content before adding to classroom connect");
            return ;
        }
        for(i=0;i<visibleContents.length;i++){
            if(visibleContents[i].type === "CMDSASSIGNMENT" || visibleContents[i] === "CMDSFILE"){
                showError("You can only add TEST,DOCUMENT,VIDEO and MODULE to classroom connect");
                return;
            }
        }
        var params = {
            sectionId:qrProgram.sectionId,
            programId:qrProgram.programId,
            centerId:qrProgram.centerId,
            entityList:visibleContents
        };
        var popup = getcmdsPopupBody();
        popup.html(addToClassroomConnectPopupSample.children().clone(true));
        $.get("/qracadstr/getProgramCourses", {programId: params.programId}, function(data) {
            $(".subjectsHolder").html("");
            var subjects = data.result.list;
            $(".subjectsHolder").append("<option value='selected' disabled selected hidden>Subjects</option>");
            for(i=0;i<subjects.length;i++){
                $(".subjectsHolder").append("<option value='"+subjects[i].name+"' data-subject-name='"+subjects[i].name+"' data-subject-id='"+subjects[i].id+"'>"+subjects[i].name+"</option>");
            }
        });
        cmdsPopup = popup.closest("#cmdsPopup");
        var today = new Date();
        today.setDate(today.getDate());
        popup.find(".calendar").click(function(){
            $(".datePickCalendar").focus();
        });
        popup.find(".datePickCalendar").datepicker({
            onClose:function(){

            },dateFormat:"dd/mm/yy",
            option:{"showAnim":"clip"}
        });
        popup.find(".addContentToClassroomConnect").on("click",addContentToClassroomConnect);
        cmdsPopup.find(".closecmdsPopup").off("click")
                                        .on("click", onClosePopup);

        function addContentToClassroomConnect(){
            var fromDate = $("#fromDate").datepicker('getDate');
            if(fromDate == null){
                showError("Please enter date");
                return ;
            }
            var firstDay = new Date(fromDate.getFullYear(), fromDate.getMonth(), 1);
            var day = fromDate.getTime();
            var month = firstDay.getTime();
            params.day = day;
            params.month = month;
            var boardName = $( ".subjectsHolder option:selected" ).data("subjectName");
            var boardId = $( ".subjectsHolder option:selected" ).data("subjectId");
            if(boardId == null || boardName == null){
                showError("Please choose board");
                return ;
            }
            params.boardName = boardName;
            params.boardId = boardId;
            console.log(params);
            vReq.post("/qrSchedule/addSchedule",params,function(data){
                if(data.errorCode != null){
                    showError(data.errorMessage);
                }
                if(data.result.saved){
                    showMessage("Content successfully added to classroom connect");
                }
            });
            setTimeout(function(){
                    closecmdsPopup();
                    resetcmdsCBoxes();
            },1000);
        }

        function onClosePopup(){
            resetcmdsCBoxes();
        }
    }
    var clearAllTimers = function(){
        var interval = window.setTimeout(function() {}, 0);
        while (interval--) {
            window.clearTimeout(interval); // will do nothing if no timeout with interval is present
        }
    }
    var prepareOrgEntities = function(srcEntities, type, value) {
        var ret = [];
        $(srcEntities).each(function(index, section) {
            $(section).removeProp("title");
            var i = {"orgEntity": section};
            if (type === "downloadable") {
                type = 'downloadble';
            }
            i[type] = value;
            ret.push(i);
        });
        return ret;
    };
    var verifyPreDownloadable = function(entityList) {
        var goAhead = true;
        var non_downloadable = [];
        $(entityList).each(function(index, section) {
            section = $(section);
            if (!section.find(".entityDownloadable").get(0)
                    || !section.find(".entityVisible").get(0)) {
                non_downloadable.push(section);
                section.find("td").addClass("focusedTD");
                goAhead = false;
            }
        });
        if (!goAhead && non_downloadable.length > 0) {
            showVYesNoBox("<p>The contents highlighted are either not allowed to be downloaded or not-visible in selected sections! </br>\n\
			Please redo the process after reviewing. </p>\n\
			<b>do you want to un-check not-applicable ones ?</b>",
                    "", function(stat) {
                        if (stat == true) {
                            var temp = $(non_downloadable[0]);
                            $(non_downloadable).each(function(index, section) {
                                section.find(".gCBoxChecked").removeClass("gCBoxChecked");
                                section.find("td").removeClass("focusedTD");
                            });
                            var mcWidget = temp.closest(".mcWidget");
                            mcWidget.find(".cmdsAllgCBox").removeClass("gCBoxChecked");
                            mcWidget.find(".cmdsTableCheckedOpts").html("").data("optsType", undefined);
                            mcWidget.find(".gCBoxChecked:first").trigger("click").trigger("click");
                        }
                        non_downloadable = [];
                    });
        }
        return goAhead;
    };
    var showToManySections = function() {
        if (publishTimer) {
            clearInterval(publishTimer);
        }
        var popup = $("#cmdsPopup");
        popup.find(".statusTd").html("");
        makeVisibleContentsSuccessFn = showToManySectionsSuccessFn;
        cloneTableBody = popup.find(".entityCloneTable tbody").removeClass("hider");
        var popupEntity = popup.find(".popupEntity");
        var status = popupEntity.data("status");
        var srcEntities = [popupEntity.data("srcEntity")];
        var checkedResult = checkedEntities.init();
        var operationType = $(this).data("type");
        var operationTypeText = $(this).data("typeText");
        operationTypeText = operationTypeText ? operationTypeText : operationType;
        var operationValue = $(this).data("value");
        operationValue = typeof operationValue == "string" ? (operationValue == "true" ? true : false) : operationValue;
        var verifyPreDownloadableResult;
	makeVisibleContentsOpType = operationType;
        if (operationType === "downloadable" && operationValue && srcEntities[0].type !== "CMDSMODULE" &&
                !verifyPreDownloadable(checkedResult.entityList)) {
            return;
        }
        popup.find(".focusedTD").removeClass("focusedTD");
        orgEntitiesForMakeVisible = prepareOrgEntities(checkedResult.srcEntities, operationType, operationValue);
        downloadableEntities = [];
        fnToCallAfterChoosingDownloadableItems = function() {
            if (status === publishedVar) {
                if(srcEntities[0].type === "CMDSMODULE" && orgEntitiesForMakeVisible[0].visible == true){
                    callPublishContents(operationType, operationValue, srcEntities);
                }
                else
                {
                    makeVisibleContents(srcEntities, "Making " + operationTypeText);
                }
            } else {
                callPublishContents(operationType, operationValue, srcEntities);
            }
        };
        if (srcEntities[0].type === "CMDSMODULE" && operationType === "downloadable") {
            var goaHead=true;
            $(checkedResult.entityList).each(function(i,section){
                if(section.find(".visibilityStatusDiv").text().trim()!=="Visible"){
                    goaHead=false;
                    return false;
                }
            });
            if(!goaHead){
                showError("The module is not visible in some of the sections selected.\n\
                <br>Make it visible in those sections and try again");
                return;
            }
            startLoader();
            for (var k = 0, l = orgEntitiesForMakeVisible.length; k < l; k++) {
                orgEntitiesForMakeVisible[k].downloadble = false;
            }
            var successFn = function(data) {
                stopLoader();
                getcmdsPopupBodyAlt(800, null, {submitClass: "resumeMakeVisibleOfContents",
                    cancelClass: "closecmdsPopupAlt"}).html(data);
                $('<div class="gBlueButton margRight5 resumeMakeVisibleOfContents">Submit without choosing content</div>')
                        .insertAfter($("#cmdsPopupAlt").find(".resumeMakeVisibleOfContents"));
                //Hide download status
                $(".downloadState").addClass("nonner");
                $(".entityDownloadState").addClass("nonner");
            };
            getReq("/qrmodules/modulecontents", {id: srcEntities[0].id}, successFn);
        } else {
            fnToCallAfterChoosingDownloadableItems();
        }
    };
    var showToManySectionsSuccessFn = function(resultList) {
        var popup = $("#cmdsPopup");
        for (var k = 0; k < resultList.length; k++) {
            var resultListEntity = resultList[k];
            var sectionId = resultListEntity.sectionInfo.id;
            var errorCode = resultListEntity.errorCode;
            var errorMsg = resultListEntity.errorMessage ? resultListEntity.errorMessage : errorCode;
            var section = popup.find(".secEntity_" + sectionId);
            var target = section.find(".visibilityStatusDiv");
            var downloadableTarget = section.find(".downloadableStatusDiv");
            if ((errorCode === "" || errorCode === "ALREADY_ADDED") && resultListEntity.visibility != "PRIVATE") {
                target.html("<div class='boldy greenColor entityVisible'>Visible</div>");
            } else if(makeVisibleContentsOpType == "visible") {
                if (errorMsg) {
                    target.html("<span class='redColor'><b>Not Visible</b>(" + errorMsg + ")</span>");
                } else {
                    target.html("<span class='redColor boldy'>Not Visible</span>");
                }
            }
            if (resultListEntity.downloadable === true) {
                downloadableTarget.html("<div class='boldy greenColor entityDownloadable'>Enabled</div>");
            } else if(makeVisibleContentsOpType == "downloadable" && errorMsg){
                downloadableTarget.html("<div class='boldy redColor entityDownloadable'>Disabled (" + errorMsg + ")</div>");
            }else{
                downloadableTarget.html("<div class='boldy redColor entityDownloadable'>Disabled</div>");
	    }
        }
        cloneTableBody.addClass("hider");
        resetChecks();
        showMessage("Completed!!");
    };
    var validateReorderContent = function(srcEntities){
	var showResourseType = $(".vChooseContentTypes").data("value");
	var orderType = $(".vChooseAlpha").data("value");
	if(srcEntities.length>1 || showResourseType != -1 || orderType != "customOrder"){
		showMessage(i18nJS('TO_REORDER_CONTENT_MSG'));
		return false;
	}
	return true;
    };
    var scheduleTestButton = function(){
        $(".gCBox").removeClass("gCBoxChecked");
        // $(this).closest("tr").addClass("scheduleTestRow");
        var entityRow = $(this).closest("tr");
        entityRow.attr("data-schedule-test","yes");
        $(this).closest("tr").find(".gCBox").addClass("gCBoxChecked").trigger("click").trigger("click");
        var popup = getcmdsPopupBody();
        popup.closest("#cmdsPopup").css("width","700px");
        popup.html(scheduleTestSample.children().clone(true));
        cmdsPopup = popup.closest("#cmdsPopup");
        var testName = $(this).closest("tr").find(".singleLineText").text();
        popup.find("#scheduleTestName").html("<h2>Scheduling Test for "+"<span style='text-transform:capitalize;'>"+testName+"</span></h2><hr/>");
        var today = new Date();
        today.setDate(today.getDate());
        popup.find(".calendar").click(function(){
            var id = $(this).data("id");
            $(".datePickCalendar:eq("+id+")").focus();
        });
        popup.find(".datePickCalendar").datepicker({
            onSelect:function(){
                var datePickerId = $(this).closest(".datePickCalendar").attr("id");
                if(datePickerId === "End"){
                    $(".closeDateRow").removeClass("disableRow");
                }
            },
            onClose:function(){

            },minDate:today,
            dateFormat:"dd/mm/yy"

        });
        popup.find(".scheduleTest").on("click",scheduleTest);
        cmdsPopup.find(".closecmdsPopup").off("click")
                                        .on("click", onClosePopup);
        function scheduleTest(){
            $(this).addClass("settingSchedule");
            var fromDate = $("#Start").datepicker('getDate');
            var endDate = $("#End").datepicker('getDate');
            var closeDate = $("#Close").datepicker('getDate');
            if(fromDate == null && endDate == null && closeDate == null){
                showError("Please enter any of the dates");
                return ;
            }
            if(fromDate != null){
                var startduration = getHrsMinsSecs(popup.find(".Start .timeDiv"));
                var startTime = fromDate.getTime() + startduration ;
                $(this).attr("data-start-time",new Date(startTime).toString());
                if(today > startTime){
                    showError("Please select valid start time");
                    return ;
                }
            }
            if(endDate != null){
                var endDuration = getHrsMinsSecs(popup.find(".End .timeDiv"));
                var endTime = endDate.getTime() + endDuration;
                $(this).attr("data-end-time",new Date(endTime).toString());
                if(today > endTime){
                    showError("Please select valid end time");
                    return ;
                }
            }
            if(closeDate != null){
                var closeDuration = getHrsMinsSecs(popup.find(".Close .timeDiv"));
                var closeTime = closeDate.getTime() + closeDuration;
                $(this).attr("data-close-time",new Date(closeTime).toString());
                if(today > closeTime){
                    showError("Please select valid close time");
                    return ;
                }
                if(endDate == null){
                    showError("Please enter end date");
                    return ;
                }
            }
            if(fromDate != null && endDate != null ){
                if(endTime < startTime){
                    showError("End time cannot be less than Start time");
                    return;
                }
            }
            if(fromDate != null && closeDate != null ){
                if(closeTime < startTime){
                    showError("Close time cannot be less than Start time");
                    return;
                }
            }
            if(endDate !=null && closeDate != null){
                if(closeTime < endTime){
                    showError("Close time cannot be less than End time");
                    return;
                }
            }
            makeVisible();
        }

        function onClosePopup(){
            entityRow.attr("data-schedule-test","no");
            resetcmdsCBoxes();
        }
    }


    var reorderContent = new function(){
	var CLICK = "click.reordercontent";
	var MOUSEENTER = "mouseenter.reordercontent";
	var MOUSELEAVE = "mouseleave.reordercontent";
	var moveHereTr = $(document);
	var srcEntity,srcElem,orgSrcElem;
	var retainReorder = false;
	var waitInMiliSec = 100;
	this.execute = function(){
		var checkCBoxes = checkedEntities.init();
		if(validateReorderContent(checkCBoxes.srcEntities)){
                	resetcmdsCBoxes();
			srcEntity = checkCBoxes.srcEntities[0];
			orgSrcElem = checkCBoxes.entityList[0];
			srcElem = orgSrcElem.clone();
			orgSrcElem.addClass("disable");
			/*$("#cmdsBlackOut").show();
			$("#mainSection").addClass("incrZIndex");*/
			showBottomDiv();
			setTimeout(function(){
				setReg();
			},waitInMiliSec);
		};
	};
	function showBottomDiv(){
		moveHereTr = $("#reorderContentMoveHereTR");
		var movingBottomDiv = $("#reorderContentInfo");
		var itemNameDiv = movingBottomDiv.find(".reorderingItem");
		itemNameDiv.html(srcElem.find(".entityNameDiv").clone());
		itemNameDiv.find(".incompleteStateTag").remove();
		movingBottomDiv.removeClass("nonner");
	};
	function setReg(){
		unsetReg();
		$(document)
			.on(CLICK,".gCBox",cancelReorder)
			.on(CLICK,".LMArrowsDiv",holdMove)
			.on(CLICK,".reorderContentMoveHere",reorderContentHere);
		var table = $("#programContent .cmdsTable");
		table.on(MOUSEENTER,".sectionContentEntity",mouseEnter)
			.on(MOUSELEAVE,mouseLeave)
		table.addClass("orderingInProgress");
	}
	function unsetReg(){
		$(document).off(CLICK);
		var table = $("#programContent .cmdsTable");
		table.off(MOUSEENTER).off(MOUSELEAVE);
		table.removeClass("orderingInProgress");
	};
	function holdMove(){
		retainReorder = true;
		return false;	
	};
	this.postMCLoad = function(){
		if(!retainReorder){
			cancelReorder();
			return;
		}
		showBottomDiv();
		setTimeout(function(){
			setReg();
		},waitInMiliSec);
		retainReorder = false;
	};
	function mouseEnter(){
		var $this = $(this);
		var reorderDiv = moveHereTr.find(".reorderContentMoveHere");
		var moveIndex = $this.data("moveIndex");
		var lastMoveIndex = reorderDiv.data("moveIndex");
		var orgMoveIndex = orgSrcElem.data("moveIndex");
		if(moveIndex != lastMoveIndex){
			reorderDiv.height(0);
		}
		if(orgMoveIndex>moveIndex){
			moveIndex++;
		}else if(orgMoveIndex==moveIndex){
			return;
		}
		reorderDiv.data("moveIndex",moveIndex);
		reorderDiv.data("serverPosition",$this.data("serverPosition"));
		reorderDiv.find(".reorderPositionDiv").removeClass("nonner")
			.find(".reorderPosition").text(moveIndex);
		$this.before(moveHereTr);
		moveHereTr.removeClass("nonner");
		reorderDiv.animate({height:'14px'},150);
	};
	function mouseLeave(){
		moveHereTr.addClass("nonner");
		var reorderDiv = moveHereTr.find(".reorderContentMoveHere");
		reorderDiv.height(0);
	};
	function cancelReorder(){
		var movingBottomDiv = $("#reorderContentInfo");
		movingBottomDiv.find(".reorderingItem").html("");
		movingBottomDiv.addClass("nonner");
		moveHereTr.find(".reorderPositionDiv").addClass("nonner");
		moveHereTr.addClass("nonner");
		moveHereTr.find("span.moveText").removeClass("nonner");
		moveHereTr.find("span.movingText").addClass("nonner");
		if(orgSrcElem){
			orgSrcElem.removeClass("disable");
		}
		/*$("#cmdsBlackOut").hide();
		$("#mainSection").removeClass("incrZIndex");*/
		unsetReg();
	};
	function reorderContentHere(){
		$this = $(this);
		var moveIndex = $this.data("serverPosition");
		var params = {
			target : {
				type : "SECTION",
				id : qrProgram.sectionId	
			},
			entity : {
				type : srcEntity.type,
				id : srcEntity.id
			},
			moveFrom : orgSrcElem.data("serverPosition"),
			moveTo : moveIndex
		};
		showTopLoader();
		$this.find("span.moveText").addClass("nonner");
		$this.find("span.movingText").removeClass("nonner");
		unsetReg();
            	postReq("/qrprograms/reorderContent", params, function() {
			if(orgSrcElem){
				orgSrcElem.remove();
			}
			if(srcElem){
				moveHereTr.after(srcElem);
			}
			cancelReorder();
			hideTopLoader();
			setTimeout(function(){
				refreshPage();
			},500);
		},function(){
			cancelReorder();
			hideTopLoader();
		});
		return;
	};
    };

    var makeVisible = function() {
        var checkCBoxes;
        if (publishTimer) {
            clearInterval(publishTimer);
        }
        makeVisibleContentsSuccessFn = showToSingleSecSuccessFn;
        programContent.find(".focusedTD").removeClass("focusedTD");
        //orgEntitiesForMakeVisible=[{id:qrProgram.sectionId,type:"SECTION"}];       
        var operationType = $(this).data("type");
        var operationTypeText = $(this).data("typeText");
        operationTypeText = operationTypeText ? operationTypeText : operationType;
        var operationValue = $(this).data("value");
        //If makeVisible function is called from schedule test function.
        if($(".scheduleTest").hasClass("settingSchedule")){
            operationType = $(".settingSchedule").data("type");
            operationValue = $(".settingSchedule").data("value");
            operationTypeText = $(".settingSchedule").data("typeText");
        }
        operationValue = typeof operationValue == "string" ? (operationValue == "true" ? true : false) : operationValue;
        var entityList = [];
        var probaleEmptyErrorTxt = "Resources Not Selected.";
	makeVisibleContentsOpType = operationType;
        switch (operationType) {
            case "visible" :
                checkCBoxes = $.extend(true, {}, checkedEntities.init("CHECK_VISIBILITY"));
                if (operationValue) {
                    entityList = checkCBoxes.invisibleContents;
                    probaleEmptyErrorTxt = "Entities already visible";
                } else {
                    entityList = checkCBoxes.visibleContents;
                    probaleEmptyErrorTxt = "Entities already in-visible";
                }
                break;
            case "downloadable" :
                checkCBoxes = checkedEntities.init();
                entityList = checkCBoxes.srcEntities;
                probaleEmptyErrorTxt = "Unable to make selected entities downloadable!";
                break;
        }
        /* VALIDATION */
        if (operationType === "downloadable" && !verifyPreDownloadable(checkCBoxes.entityList)) {
            return;
        }
        orgEntitiesForMakeVisible = prepareOrgEntities([{id: qrProgram.sectionId, type: "SECTION"}], operationType, operationValue);
        //If makeVisible function is called from schedule test function.
        if($(".scheduleTest").hasClass("settingSchedule")){
            if(orgEntitiesForMakeVisible.length == 1){
                orgEntitiesForMakeVisible[0].schedule = {};
                if($(".settingSchedule").data("startTime") != undefined){
                    orgEntitiesForMakeVisible[0].schedule.startTime = $(".settingSchedule").data("startTime");
                }
                if($(".settingSchedule").data("endTime") != undefined){
                    orgEntitiesForMakeVisible[0].schedule.endTime = $(".settingSchedule").data("endTime");
                }
                if($(".settingSchedule").data("closeTime") != undefined){
                    orgEntitiesForMakeVisible[0].schedule.closeTime = $(".settingSchedule").data("closeTime");
                }
            }
            else{
                showError("Something went wrong..please try again");
                return ;
            }
        }

        //orgEntitiesForMakeVisible = prepareOrgEntities([{id:qrProgram.sectionId,type:"SECTION"}],"visible",true);       

        downloadableEntities = [];
        fnToCallAfterChoosingDownloadableItems = function() {
            var popup = checkCBoxes.prepareEntityClonesPopup(null,
                    checkCBoxes.entityCloneList, checkCBoxes.srcEntities);
            cloneTableBody = popup.find(".entityCloneTable").children("tbody");
            //Remove scheduleTest and entityRemainingTime Block in clone popup.
            popup.find(".entityCloneTable .scheduleTestButton").addClass("nonner");
            popup.find(".entityCloneTable .entityRemainingTimeBlock").addClass("nonner");
            cloneTableBody.children("tr.nonner").removeClass("nonner");
            var progressMsg = "Making " + operationTypeText;
            for (var k = 0; k < entityList.length; k++) {
                var entityId = entityList[k].id;
                assignMadeVisibleMsg(entityId, progressMsg);
            }
            cloneTableBody.find(".msgPlace").text("N.A.").addClass("redTextColor");
            checkForMultiPublishingEnd();
            var ret = callPublishContents(operationType, operationValue, checkCBoxes.unpublishedContents);
            if (entityList.length > 0) {
                makeVisibleContents(entityList, progressMsg);
            } else if (!ret) {
                closecmdsPopup();
                showError(probaleEmptyErrorTxt);
            }
        };

        if (operationType === "downloadable") {
            var hasModule = false;
            if (checkCBoxes.unpublishedContents.length === 0 && entityList.length === 0) {
                showError(probaleEmptyErrorTxt);
                return;
            }
            for (var k = 0; k < checkCBoxes.srcEntities.length; k++) {
                if (checkCBoxes.srcEntities[k].type === "CMDSMODULE") {
                    hasModule = true;
                }
            }

            if (!hasModule) {
                fnToCallAfterChoosingDownloadableItems();
            } else if (hasModule && checkCBoxes.srcEntities.length > 1) {
                showError("A module should always be made visible separately.\n\
                            Either your selection contains more than one module or one module \n\
                            with other contents.");
                return;
            }
            else if (hasModule && checkCBoxes.srcEntities.length === 1) {
                if (operationValue) {
                    startLoader();
                    orgEntitiesForMakeVisible[0].downloadble = false;
                    var successFn = function(data) {
                        stopLoader();
                        getcmdsPopupBodyAlt(800, null, {submitClass: "resumeMakeVisibleOfContents",
                            cancelClass: "closecmdsPopupAlt"}).html(data);
                        $('<div class="gBlueButton margRight5 resumeMakeVisibleOfContents">Submit without choosing content</div>')
                                .insertAfter($("#cmdsPopupAlt").find(".resumeMakeVisibleOfContents"));
                    };
                    getReq("/qrmodules/modulecontents", {id: checkCBoxes.srcEntities[0].id,sectionId:qrProgram.sectionId}, successFn);
                } else {
                    showMessage("Manage the Module Contents download property\n\
                         using 'Enable Download on device' option");
                }
            }
        } else {
            fnToCallAfterChoosingDownloadableItems();
        }
    };

    //popup for choosing downloadable entities for module
    var fnToCallAfterChoosingDownloadableItems;
    var resumeMakeVisibleOfContents = function() {
        moduleCheckedList = $(".moduleTable").find(".gCBoxChecked").length;
        moduleContentsList = $(".moduleTable").find(".gCBox").length;
        var popup = $("#cmdsPopupAlt");
        var cBoxes = popup.find("#fixedSec .gCBox");
        downloadableEntities = [];
        for (var k = 0, l = cBoxes.length; k < l; k++) {
            if (cBoxes.eq(k).hasClass("gCBoxChecked")) {
                var trData = cBoxes.eq(k).closest("tr").data();
                downloadableEntities.push({type: trData.entityType, id: trData.entityId});
            }
        }
        closecmdsPopup(popup);
        if (fnToCallAfterChoosingDownloadableItems) {
            fnToCallAfterChoosingDownloadableItems();
        } else {
            showError(COMMON_ERROR_MESSAGE);
        }
    };

    var callPublishContents = function(operationType, operationValue, unpublishedContents) {
        if (operationType === "visible" && operationValue) {
            publishJobIdList = [];
            return publishContents(unpublishedContents);
        }
    };
    var showToSingleSecSuccessFn = function(resultList) {
        for (var k = 0; k < resultList.length; k++) {
            var entity = resultList[k];
            var entityId = entity.content.id;
            var entityType = entity.content.type;
            var errorCode = entity.errorCode;
            var visible = false;
            var downloadable = entity.downloadable ? true : false;
            var errorMsg = "";
            if ((errorCode == "" || errorCode == "ALREADY_ADDED") && entity.visibility != "PRIVATE") {
                visible = true;
            } else {
                errorMsg = entity.errorMessage ? entity.errorMessage : errorCode;
            }
            if(entityType == "CMDSTEST"){
                if(visible == true){
                    removeScheduleOption(entityId,visible,errorMsg);
                }
                else{
                    addScheduleOption(entityId,visible,errorMsg);
                }
            }
            if(visible == true && entityType == 'CMDSMODULE'){
                if(moduleCheckedList >0 && moduleCheckedList < moduleContentsList ){
                    downloadable = 'Partial';
                }
                else if(moduleContentsList > 0 && moduleCheckedList >0 && moduleCheckedList == moduleContentsList ){
                    downloadable = 'Enabled';
                }
                else if(moduleCheckedList == 0){
                    downloadable = "Disabled";
                }
            }
            assignMadeVisibleStatus(entityId, visible, downloadable, errorMsg);
        }
        checkForMultiPublishingEnd();
    };

    var assignMadeVisibleMsg = function(entityId, msg) {
        var tr = cloneTableBody.children(".popupEntity_" + entityId);
        var entity = programContent.find(".entity_" + entityId);
        var visibleTarget = entity.find(".visibilityStatusDiv");
        var statusTd = tr.find(".statusTd").text(msg);
    }

    var addScheduleOption = function(entityId,visible,errorMsg){
        var entity = programContent.find(".entity_"+entityId);
        // console.log(entity);
        if(!errorMsg){
            entity.find(".entityRemainingTimeBlock").addClass("nonner");
            entity.find(".scheduleTestButton").html("");
            entity.find(".getTestPage").after("<span class='scheduleTestButton'>"+
            "<img src='/public/images/alarm-white.png' style='position: relative;top:2px;'>"+ 
            "<span style='bottom: 1px;position: relative;'> Schedule Test</span>"+
        "</span>");
        }
    }

    var removeScheduleOption = function(entityId,visible,errorMsg){
        var entity = programContent.find(".entity_"+entityId);
        var schedule = entity.data("scheduleTest");
        if(schedule === "yes"){
            setTimeout(function(){
                refreshPage();
            },2000);
            return;
        }
        if(!errorMsg){
            // console.log(entity.find(".scheduleTestButton"));
            entity.find(".scheduleTestButton").addClass("nonner");
        }
    }
    var assignMadeVisibleStatus = function(entityId, visible, downloadable, errorMsg) {
        //console.log(arguments);
        var tr = cloneTableBody.children(".popupEntity_" + entityId);
        var entity = programContent.find(".entity_" + entityId);
        var visibleTarget = entity.find(".visibilityStatusDiv");
        var statusTd = tr.find(".statusTd").html("");
        if (errorMsg) {
            tr.addClass("madeVisibleError");
            statusTd.append("<span class='redColor boldy'>" + errorMsg + "</span>");
	    if(makeVisibleContentsOpType == "visible"){
            	visibleTarget.html("<span class='redColor'><b>Not Visible</b>(" + errorMsg + ")</span>");
	    }
        } else if (visible) {
            tr.addClass("madeVisible");
            statusTd.append("<span class='greenColor boldy'>Visible!!</span>");
            entity.data("visibilityStatus", "VISIBLE");
            visibleTarget.html("<span class='greenColor boldy entityVisible'>Visible</span>");
        } else {
            tr.addClass("madeVisibleError");
            statusTd.append("<span class='redColor boldy'>Not Visible</span>");
            entity.data("visibilityStatus", "INVISIBLE");
            visibleTarget.html("<span class='redColor boldy'>Not Visible</span>");
        }
        var downloadableTarget = entity.find(".downloadableStatusDiv");
        if (downloadable == true) {
            statusTd.append("<span class='greenColor boldy'> & Download enabled</span>");
            downloadableTarget.html("<div class='boldy greenColor entityDownloadable'>Enabled</div>");
        } else if (entity.find(".entityDownloadable").get(0)) {
            if(downloadable == 'Disabled' || downloadable == false){
                statusTd.append("<span class='redColor boldy'> & Download Disabled</span>");
                downloadableTarget.html("<div class='boldy redColor entityDownloadable'>Disabled</div>");
            }
            else if(downloadable == 'Enabled'){
                statusTd.append("<span class='greenColor boldy'> & Download Enabled</span>");
                downloadableTarget.html("<div class='boldy greenColor entityDownloadable'>Enabled</div>");
            }
            else if(downloadable == 'Partial'){
                statusTd.append("<span class='yellowColor boldy'> & Download Partial</span>");
                downloadableTarget.html("<div class='boldy yellowColor entityDownloadable'>Partial</div>");
            }
        }
        // switch(downloadable){
        //     case true:
        //     case "Enabled":
        //         statusTd.append("<span class='greenColor boldy'> & Download enabled</span>");
        //         downloadableTarget.html("<div class='boldy greenColor entityDownloadable'>Enabled</div>");
        //         break;
        //     case "Disabled":
        //         statusTd.append("<span class='redColor boldy'> & Download Disabled</span>");
        //         downloadableTarget.html("<div class='boldy redColor entityDownloadable'>Disabled</div>");
        //         break;
        //     case "Partial":
        //         statusTd.append("<span class='yellowColor boldy'> & Download Partial</span>");
        //         downloadableTarget.html("<div class='boldy yellowColor entityDownloadable'>Partial</div>");
        //         break;
        //     case entity.find(".entityDownloadable").get(0):
        //         statusTd.append("<span class='redColor boldy'> & Download disabled</span>");
        //         downloadableTarget.html("<div class='boldy redColor entityDownloadable'>Disabled</div>");
        //         break;
        // }
    };
    var checkForMultiPublishingEnd = function() {
        resetChecks();
        if (cloneTableBody.children(".madeVisible,.publishingError,.madeVisibleError").length
                === cloneTableBody.children().length) {
            showMessage("Completed!!");
        }
    };





    var makeVisibleContents = function(invisibleContents, msgText) {
        msgText = msgText ? msgText : "Making Visible..";
        if (invisibleContents.length > 0) {
            for (var k = 0; k < invisibleContents.length; k++) {
                var entityId = invisibleContents[k].id;
                cloneTableBody.children(".popupEntity_" + entityId)
                        .find(".statusTd").html(msgText);
            }

            if (downloadableEntities && downloadableEntities.length > 0) {
                for (var p = 0, l = orgEntitiesForMakeVisible.length; p < l; p++) {
                    orgEntitiesForMakeVisible[p]["downloadableEntities"] = downloadableEntities;
                }
            }

            var params = {options: orgEntitiesForMakeVisible, contents: invisibleContents};
            var successFn = function(data) {
                var resultList = data.result.list;
                if (makeVisibleContentsSuccessFn) {
                    makeVisibleContentsSuccessFn(resultList);
                }
            };
            postReq("/qrprograms/makevisible", params, successFn, function() {
                //cloneTableBody.children(".popupEntity_"+entityId).find(".statusTd").html("");
                cloneTableBody.find(".statusTd")
                        .addClass("madeVisibleError")
                        .html("<span class='redColor boldy'>Error Occured</span>");
            });
        }
    };
    var publishContents = function(unpublishedContents) {
        if (unpublishedContents.length > 0) {
            var params = {entities: unpublishedContents};
            var successFn = function(data) {
                var jobIdsOfEntities = data.result.info;
                var progressBar = publishBarSample.children().clone();

                $.each(jobIdsOfEntities, function(entityId, entityResp) {
                    var tr = cloneTableBody.children(".popupEntity_" + entityId)
                            .addClass("pollingTd");
                    var errorCode = entityResp.errorCode;
                    var errorMsg = entityResp.errorMessage ? entityResp.errorMessage : errorCode;
                    if (errorCode) {
                        addPublishingError(tr, errorMsg);
                    }
                    else if(entityResp.jobId === "PUBLISH_IN_PROGRESS"){
                        addPublishingError(tr,"In Progress...Please wait");
                    }
                    else {
                        var jobId = entityResp.jobId;
                        tr.data("jobId", jobId);
                        publishJobIdList.push(jobId);
                        tr.find(".statusTd").html(progressBar.clone(true));
                    }
                });
                if (publishJobIdList.length > 0) {
                    publishStatusUpdater();
                    publishTimer = setInterval(function() {
                        publishStatusUpdater();
                    }, 5000);
                }
            };
            postReq("/qrprograms/publish", params, successFn);
            return true;
        }
    };
    var publishStatusUpdater = function() {
        var inVisibleContents = [];
        $.get("/qrprograms/getPublishStatus", {jobIds: publishJobIdList}, function(data) {
            var result = data.result.list;
            for (var k = 0; k < result.length; k++) {
                var entityResult = result[k];
                var entityId = entityResult.id;
                var tr = cloneTableBody.children(".popupEntity_" + entityId);
                var percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                tr.find(".publishPercent").html(percent + "%");
                var maxWidth = tr.find(".publishGrayBar").width();
                tr.find(".publishGreenBar").width(percent * maxWidth / 100);
                var removeJobId = false;
                if (entityResult.errorCode !== "") {
                    var errorMsg = entityResult.errorMessage ? entityResult.errorMessage : entityResult.errorCode;
                    addPublishingError(tr, errorMsg);
                    removeJobId = true;
                } else if (entityResult.numCompletedSteps === entityResult.numOfSteps) {
                    removeJobId = true;
                    inVisibleContents.push(tr.data("srcEntity"));
                    var entity = programContent.find(".entity_" + entityId);
                    entity.data("status", publishedVar);
                    tr.data("status", publishedVar);

                    //only for questions
                    var quesStatus = entity.find(".quesStatus");
                    if (quesStatus.length > 0) {
                        quesStatus.removeClass().addClass("quesStatus quesStatus" + publishedVar);
                        quesStatus.text(publishedVar);
                        entity.find(".editcmdsQues,.editQuesTags").remove();
                    }
                }
                if (removeJobId) {
                    var index = publishJobIdList.indexOf(tr.data("jobId"));
                    if (index !== -1) {
                        publishJobIdList.splice(index, 1);
                    }
                    tr.removeClass("pollingTd");
                }
            }
            if (publishJobIdList.length === 0) {
                try {
                    clearInterval(publishTimer);
                } catch (err) {
                    console.error("err in clearing interval")
                }
            }
            makeVisibleContents(inVisibleContents);
        });
    };
    var addPublishingError = function(tr, errorCode) {
        switch (errorCode) {
            case "NOT_CONVERTED" :
            case "NOT_CONVERTED" :
                errorCode = "Resource under process";
                break;
            default :
                break;
        }
        ;
        tr.addClass("publishingError")
                .find(".statusTd").html("<span class='redColor boldy big14'>\n\
        " + errorCode + "</span>");
    };
})(jQuery);
function onVisiblityMoreChooseChange() {
}
var downloadtomanysectionsOptsCbfn = function(optsDiv) {
    var popupEntity = optsDiv.closest("#cmdsPopup").find(".popupEntity");
    var entity = popupEntity.data("srcEntity");
    if (entity.type === "CMDSMODULE") {
        optsDiv.find(".showToManySections").eq(1).text("Manage Contents Download in Module");
        optsDiv.find(".vChooseOpt").eq(1).remove();
    }
};
//var sectionlibraryOptsCbfn = function(optsDiv) {
//    var popupEntity = optsDiv.closest("#cmdsPopup").find(".popupEntity");
//    var entity = popupEntity.data("srcEntity");
//    if (entity.type === "CMDSMODULE") {
//        optsDiv.find(".showToManySections").eq(1).text("Manage Contents Download in Module");
//        optsDiv.find(".vChooseOpt").eq(1).remove();
//    }
//};
