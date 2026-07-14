var qrResources = new function($) {
    var sourcePathDiv, clickEvent = "click", bodyClickEvent = "click.qrResources", folderId,
            folderPage, statusPublished = "PUBLISHED", mainResourcesPage = false,
            bodykeyup = "keyup.qrResources", changeEvent = "change.qrResources";
    var pageUrlParams = {includes: "", orderBy: "timeCreated", query: "", courseId: "", start: 0, size: 50};
    var isNTAPattern=false;
    this.init = function(params) {
        var mcWidget;
        putActiveMenuItem($("#cmdsResources").parent());
        if (params.mainResourcesPage) {
            mainResourcesPage = true;
        } else {
            mainResourcesPage = false;
        }

        folderPage = $("#folderPage");
        folderId = $("#folderPage").data("folderId");
        mcWidget = folderPage = $("#folderPage");
        var mcParams = {orderBy: "timeCreated", folderId: params.folderId};
        if (params.popupResources) {
            mcWidget = $("#popupResources");
        } else {
            resetCmdsPages($("#cmdsResources"));
        }


        var afterSubsLoadCbfn = null;
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
        //setting the url params and updating mcWidget params holders        
        var urlParams = fetchUrlParams();
        $.extend(mcParams, urlParams);
        //checking if the resources are for a popup table
        if (!params.popupResources) {
            initmcWidgetforCMDS(mcWidget, "/qrresources/resourcesTable",
                    mcParams, false, true, afterLoad);
            mcWidget.data("pageUrlParams", pageUrlParams);
            mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
            qrmcWidgetUtil.loadRadioSubs(mcWidget, null, null, afterSubsLoadCbfn);
            updatemcWidgetParamHolders(mcWidget, urlParams, "RESOURCES");

            folderPage.off(clickEvent)
                    .on(clickEvent, ".cmdsSourceInfo", cmdsSourceInfo)

                    //on options        
                    .on(clickEvent, ".deleteResources", deleteResources)
                    .on(clickEvent, ".publishDocVideos", publishDocVideos)

            //submit functions
            $("body").off(bodyClickEvent)

                    //creating and editing
                    .on(bodyClickEvent, ".submitEditedFolderDetails", submitEditedFolderDetails)
                    .on(bodyClickEvent, ".submitCreateFolder", submitCreateFolder)
                    .on(bodyClickEvent, ".goToCTFTestFormat", goToCTFTestFormat)
                    .on(bodyClickEvent, ".submitCreateTest", submitCreateTest)
                    .on(bodyClickEvent, ".submitCreateAssignment", submitCreateAssignment)
                    .on(bodyClickEvent, ".submitCreateTS", submitCreateTS)
                    .on(bodyClickEvent, ".submitCreatePackage", submitCreatePackage)
                    .on(bodyClickEvent, ".submitCreateCDP", submitCreateCDP)
                    .on(bodyClickEvent, ".CTFSub", ctfSub)
                    .on(bodyClickEvent, ".copyCTFToAllSubs", copyCTFToAllSubs)
                    .on(changeEvent, ".CTFQuesNo", ctfQuesNoTimed)
                    //moving resources
                    .on(bodyClickEvent, ".submitPublishDocVideos", submitPublishDocVideos)
            fixContentSec();
            calcTitleMaxWidth();
        } else {
            var popupParams = {};
            var afterLoadForPopupResources = function() {
                var trs = mcWidget.find("tr");
                for (var k = 1, l = trs.length; k < l; k++) {
                    var tr = trs.eq(k);
                    if (tr.data("entityType") === "FOLDER") {
                        tr.find(".gCBox").remove();
                    }
                }
                var cmdsPopup = $("#cmdsPopup");
                var top = 80 + $(window).scrollTop();
                cmdsPopup.css({top: top});
                //80 for bottom margin
                var popupHeight = $(window).height() - 160;
                cmdsPopup.height(popupHeight);
                var popOffset = cmdsPopup.offset();
                //70 for add selected button
                var heightOfNonFixedSec = ($('#fixedSec').offset().top - popOffset.top) + 70;
                var fixedSecHeight = popupHeight - heightOfNonFixedSec;
                $('#fixedSec').height(fixedSecHeight);
                calcTitleMaxWidth(cmdsPopup);
            };
	    popupParams = serializeJson.decode(popupParams);
            $.extend(mcParams, popupParams);
            afterLoadForPopupResources();
            initmcWidgetforCMDS(mcWidget, "/qrresources/popupResourcesTable",
                    mcParams, false, true, afterLoadForPopupResources);
        }
    };

    var startLoader = showTopLoader;
    var stopLoader = hideTopLoader;
    var closePopup = closecmdsPopup;
    var resetChecks = resetcmdsCBoxes;
    var getReq = vReq.get;
    var postReq = vReq.post;
    var checkedEnitiesClone;






    var afterLoad = function() {
        calcTitleMaxWidth();
    };
    var calcTitleMaxWidth = function(targetDiv) {
        if (!targetDiv) {
            targetDiv = folderPage;
        }
        var tableHead = targetDiv.find(".headSecTable");
        var tableBody = targetDiv.find(".cmdsTable");
        var entityNameDiv = tableBody.find(".entityNameDiv");
        var entityNameWidth = tableHead.find(".itemTitleTH").width() - 10;
        var entityNameMaxWidth = entityNameWidth - 30;
        entityNameDiv.width(entityNameWidth)
                .find(".singleLineText").css("max-width", entityNameMaxWidth);
    };
//    event handlers
    this.addResource = {
        FOLDER: function() {
            fillcmdsPopup("submitCreateFolder", "createFolderSample");
        },
        AUTO_GEN_TEST:function(){
            openGenerateTest(folderId);
        },
        MODULE: function() {
            openCreateMODULE(null, folderId);
        },
        DOCUMENT: function(value) {
            //this.addContentUtil(value);
            docAddResource.open();
        },
        FILE: function(value) {
            //this.addContentUtil(value);
            fileAddResource.open();
        },
        VIDEO: function(value) {
            videoResource.open();
        },
        QUESTION_SET: function(value) {
            this.addContentUtil(value);
        },
        TEST: function(value) {
            createTestAssignment(value);
        },
        ASSIGNMENT: function(value) {
            createTestAssignment(value);
        },
        TESTSERIES: function() {
            createTestSeries();
        },
        PACKAGE: function() {
            createPackage();
        },
        CDP: function() {
            createCDP();
        },
        addContentUtil: function(contentType) {
            var params = {addType: contentType, folderId: folderId};
            var cbfn = function() {
                var folderName = "";
                var titleDiv = folderPage.find(".entityEditableName");
                var cSec = cSecHolder;
                if (folderPage && titleDiv.length > 0) {
                    folderName = titleDiv.text().trim();
                    cSec.find(".chooseContentTarget").text("Choose Another Folder");
                    cSec.find(".uploadTargetDiv").html(getFolderTag(folderId, folderName));
                }
                pushHistory(null, null, vcmdsUrls.ADDCONTENT());
            };
            opencmdsPage("/qraddContent/addContent", params, $("#cmdsAddContent"), cbfn);
        }
    };
    var submitCreateFolder = function() {
        submitCreateEditFolder($(this), "CREATE");
    };
    var submitEditedFolderDetails = function() {
        submitCreateEditSource($(this), "EDIT");
    };
    var submitCreateEditFolder = function($this, actionType) {
        var popup = $this.closest("#cmdsPopup");
        var params = getFormValues(popup);
        if (params.hasError) {
            return;
        }
        params.parentFolderId = folderId;
        startLoader();
        var postUrlStr = "/qrresources/createFolder";
        if (actionType === "EDIT") {
            postUrlStr = "/qrSources/editSource";
        }
        var successFn = function(data) {
            closePopup();
            stopLoader();
            setTimeout(refreshPage, 1000);
            if (actionType === "CREATE") {
                trackEventForGA("FOLDER", "ADD_CONTENT", params.name);
            }
        };
        postReq(postUrlStr, params, successFn);
    };


    var createTestAssignment = function(testType) {
        console.log("inside createTestAssignment");
        var popup, sampleStr = "cmdsTestFormatSample";
        popup = fillcmdsPopup("goToCTFTestFormat", sampleStr);
        popup.children(".CTFTitleTaker").data("testType", testType);
        popup = popup.closest("#cmdsPopup");
        popup.on("dragstart", ".subjOrder", onSubjectEntryDragStart)
             .on("dragend", ".subjOrder", onSubjectEntryDragEnd)
             .on("dragover", ".subjOrder", onSubjectEntryDragOver)
             .on("dragleave", ".subjOrder", onSubjectEntryDragLeave)
             .on("drop", ".subjOrder", onSubjectEntryDragDrop)
        if (testType === "TEST") {
            popup.find(".goToCTFTestFormat").text("Next");
        } else {
            popup.find(".cmdsPopupHead").first().text("Create Assignment");
            popup.find(".testDurationTr").remove();
        }
        var targetSubs = popup.find(".mvcCTFSubsTd");
        inlineLoader(targetSubs);
        var cbfn = function(subs) {
            var newmvc = makeHTMLTag("div", {"class": "multiplevChoose mvcCTFSubs",
                "data-param-name": "brdIds"});
            for (var k = 0; k < subs.length; k++) {
                var item = mvcItemSample.children().clone(true), t = subs[k];
                item.find(".gCBox").attr("data-board-id",t.id);
                item.find(".gCBox").data({value: t.id, callback: "insertmvcTopics"});
                item.find(".gCBoxText").text(t.name);
                newmvc.append(item);
            }
            targetSubs.html(newmvc);
        };
        loadAndCacheCourses(cbfn);
        //loadRadioExams(popup.find(".mvcExamsTd"));
    };
    var subjectEntryToDrag;
    var subjectEntriesHolder = $(".selectedSubjectsList");
    var onSubjectEntryDragStart = function(e){
        var dataTransfer = e.originalEvent.dataTransfer;
        dataTransfer.setData('Text', null);
        subjectEntryToDrag = $(this);
    }

    var onSubjectEntryDragEnd = function(e){
        subjectEntryToDrag = null;
    }

    var onSubjectEntryDragOver = function(e){
        var event = e.originalEvent;
        if (event.pageY - $(window).scrollTop() <= 25) {
            $(window).scrollTop(event.pageY - 25);
        }
        var entry = $(this);
        entry.addClass("over");
        return false;
    }

    var onSubjectEntryDragLeave = function(e){
        $(this).removeClass("over");
    }

    var onSubjectEntryDragDrop = function(e){
        var event = e.originalEvent;
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        if (subjectEntryToDrag) {
            var targetEntry = $(this);
            targetEntry.removeClass("over");
            var oldPos = subjectEntryToDrag.index("." + "subjOrder");
            pos = targetEntry.index("." + "subjOrder");
            if (pos < oldPos) {
                subjectEntryToDrag.insertBefore(targetEntry);
            }
            else{
                subjectEntryToDrag.insertAfter(targetEntry);
            }
            subjectEntryToDrag = null;
        }
        return false;
    }

    var ctfParams;
    var setNTAOptions=function(popup){
        console.log(popup.find(".CTFTable").find(".CTFMaxQuestions"));
        popup.find(".maxQuestionsText").removeClass("nonner");
        popup.find(".CTFTable").find(".CTFMaxQuestions").each(function(){
            console.log($(this));
            $(this).removeClass("nonner");
        })
        isNTAPattern=true;

    }
    var goToCTFTestFormat = function() {
        console.log("inside goToCTFTestFormat");
        var popup = $(this).closest("#cmdsPopup"), params = getFormValues(popup);
        console.log(params);
        
        var mvcValues = getmvcValues(popup.find(".mvcCTFSubs"));
        var duration = getHrsMinsSecs(popup.find(".timeDiv"));
        if (params.hasError || mvcValues.values.length === 0) {
            showcmdsPopupError(popup);
            return;
        }
        var testType = popup.find(".CTFTitleTaker").data("testType");
        if (testType === "TEST") {
            if (duration <= 0) {
                showcmdsPopupError(popup, "Please Enter a proper duration.");
                return;
            }
            params.duration = duration;
            if(params.isNTAPattern === "true"){
            params.isNTAPattern=true;
            setNTAOptions(popup);
            }
            ctfParams = params;
            
            prepareCTF(popup, mvcValues);
        } else if (testType === "ASSIGNMENT") {
            ctfParams = params;
            prepareCTFForAssignment(popup, mvcValues);
        }
    };
    var submitCreateAssignment = function() {
        startLoader();
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }

        var metadata = ctfParams.metadata;
        var subTrs = getPopupDiv($(this)).find(".CTFTableForAssignment")
                .children("tbody").children("tr");
        for (var l = 0; l < metadata.length; l++) {
            var qNo = subTrs.eq(l).find("input").val();
            if (!(checkIntNum(qNo) && qNo)) {
                qNo = 0;
            }
            metadata[l].qusCount = qNo;
        }
        ;
        ctfParams.type = "ASSIGNMENT";
        createTestAssignmentSubmitUtil($this);
    };
    var submitCreateTest = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
        var r = populateMetadata($(this));
        ctfParams.subjectiveTest = false;
        if (r) {

            var metadata = ctfParams.metadata, err = "";
            console.log(metadata);
            for (var k = 0; k < metadata.length; k++) {
                var m = metadata[k];
                if (m.qusCount === 0) {
                    err += m.name + " has 0 questions.<br>";
                }
                var details = m.details;
                for (var i = 0; i < details.length; i++) {
                    var typeDetails = details[i], marks = typeDetails.marks;
                    if (marks.positive === 0 && typeDetails.qusCount > 0) {
                        err += "Provide marks for " + typeDetails.type + " questions of " + m.name + ".<br>";
                    }
                    if(typeDetails.qusCount < typeDetails.maxQuestionsTobeAttempted ){
                        err += "Max Questions to be attempted should be less than question count";
                    }
                    if(typeDetails.type === "SUBJECTIVE" && typeDetails.qusCount > 0){
                        ctfParams.subjectiveTest = true;
                    }
                }
            }
            if (err !== "") {
                showcmdsPopupError(null, err);
            } else {
                ctfParams.type = "TEST";
                createTestAssignmentSubmitUtil($this);

            }
        }
    };
    var createTestAssignmentSubmitUtil = function($this) {
        var resourceUrlFn = vcmdsUrls.ASSIGNMENT;
        var successFn = function(htmlContent) {
            $this.removeClass("btnDisabled");
            cSecHolder.html(htmlContent);
            closeStopCombo();
            var testId = cSecHolder.children("#testPage").data("entityId");
            trackEventForGA("CMDS" + ctfParams.type, "ADD_CONTENT", ctfParams.name);
            pushHistory(null, null, resourceUrlFn(testId));
            trackPageView();
        };
        var errorFn = function() {
            $this.removeClass("btnDisabled");
        };
        var url = "createAssignment";
        if (ctfParams.type === "TEST") {
            url = "createTest";
            resourceUrlFn = vcmdsUrls.TEST;
        }
        if (folderId) {
            ctfParams.folderId = folderId;
        }
        $this.addClass("btnDisabled");
        postReq("/qrtests/" + url, ctfParams, successFn, errorFn);
    };


    //create test/assignment utils
    var prepareCTF = function(popup, mvcValues) {
        hidecmdsPopupError(popup);
        popup.find(".CTFHolder").removeClass("nonner").siblings().addClass("nonner");
        var m = makeHTMLTag, divsHolder = m("div");
        var metadata = [];
        for (var k = 0; k < mvcValues.values.length; k++) {
            var subId = mvcValues.values[k], subName = mvcValues.names[k];
            var subjson = {id: subId, name: subName, qusCount: 0, details: [], children: []};
            var topicmvcValues = getmvcValues(mvcValues.mvcItems[k].find(".mvcTopics"));
            for (var p = 0; p < topicmvcValues.values.length; p++) {
                var topicId = topicmvcValues.values[p];
                var topicName = topicmvcValues.names[p];
                subjson.children.push({id: topicId, name: topicName});
            }
            metadata.push(subjson);
        }
        var finalMetadata = [];
        var subjectOrder = popup.find(".selectedSubjectsList .subjOrder");
        var boardSeqId = [];
        subjectOrder.each(function(){
            boardSeqId.push($(this).data("boardId"));
        });
        for (var k = 0; k < boardSeqId.length; k++) {
            for (var j = 0; j < metadata.length; j++) {
                if(metadata[j].id===boardSeqId[k]){
                    finalMetadata.push(metadata[j]);
                    var d = m("div", {"class": "gButton CTFSub"});
                    d.text(metadata[j].name);
                    divsHolder.append(d);
                }
            }
        }
        ctfParams.metadata = finalMetadata;
        var subs = divsHolder.children();
        subs.first().addClass("gButtonActive");
        popup.find(".CTFSubs").html(subs);
        if (subs.length === 1) {
            popup.find(".copyCTFToAllSubs").remove();
        }
        popup.find(".goToCTFTestFormat").toggleClass("goToCTFTestFormat submitCreateTest");
    };
    var ctfQuesNoTimed = function() {
        var $this = $(this), ctfHolder = $this.closest(".CTFHolder");
        setTimeout(function() {
            ctfQuesNo($this, ctfHolder);
        }, 100);
    };
    var ctfQuesNo = function($this, ctfHolder) {
        var boxes = ctfHolder.find(".CTFQuesNo");
        var totalQues = getTotalQuesns(boxes);
        ctfHolder.find(".CTFSubTotal").text(totalQues);
        ctfParams.metadata[activeCTFSubIndex].qusCount = 0;
        if (totalQues === "-") {
            ctfHolder.find(".CTFSubsTotal").text(totalQues);
        } else {
            putTotalCTFQuesns(ctfHolder, totalQues);
        }
    };
    var activeCTFSubIndex = 0;
    var ctfSub = function() {
        var $this = $(this);
        var r = populateMetadata($this);
        if (r) {
            activeCTFSubIndex = $this.index();
            changeActiveClass($this, "gButtonActive");
            var ctfHolder = $this.closest(".CTFHolder"), trs = ctfHolder.find("tr");
            checkorResetCTFNum(ctfHolder, "RESET");
            var metadata = ctfParams.metadata[activeCTFSubIndex], details = metadata.details;
            for (var k = 0; k < details.length; k++) {
                var tr = trs.eq(k + 1), d = details[k], m = d.marks;
                tr.find(".CTFQuesNo").val(d.qusCount);
                tr.find(".CTFPosMarks").val(m.positive);
                tr.find(".CTFNegMarks").val(m.negative);
                tr.find(".CTFMaxQuestions").val(d.maxQuestionsTobeAttempted);
            }
            ctfHolder.find(".CTFSubTotal").text(metadata.qusCount);
        }
    };
    var copyCTFToAllSubs = function() {
        var r = populateMetadata($(this), "COPY");
        if (r) {
            showcmdsPopupError(null, "<span class='greenColor'>Successfully copied!!</span>");
        }
    };
    var putTotalCTFQuesns = function(ctfHolder, currentTotal) {
        var metadata = ctfParams.metadata;
        var totalQues = getTotalQuesns(metadata);
        if (totalQues !== "-")
            totalQues += currentTotal;
        ctfHolder.find(".CTFSubsTotal").text(totalQues);
    };
    var getTotalQuesns = function(list) {
        var totalQues = 0;
        for (var k = 0; k < list.length; k++) {
            var val;
            try {
                val = list.eq(k).val();
            } catch (err) {
                val = list[k].qusCount;
            }
            if (checkIntNum(val)) {
                totalQues += parseInt(val);
            } else if (val === "") {
                totalQues += 0;
            } else {
                totalQues = "-";
                break;
            }
        }
        return totalQues;
    };
    var populateMetadata = function($this, populateType) {
        var metadata;
        if (populateType === "COPY") {
            metadata = ctfParams.metadata;
        } else {
            metadata = [ctfParams.metadata[activeCTFSubIndex]];
        }
        var ctfHolder = $this.closest("#cmdsPopup").find(".CTFHolder");
        var result = getCTFEntries(ctfHolder);
        if (result.hasError)
            return false;
        var totalQusCount = 0;
        for (var k = 0; k < metadata.length; k++) {
            metadata[k].details = result.details;
            metadata[k].qusCount = result.qusCount;
            totalQusCount += result.qusCount;
        }
        if (populateType === "COPY") {
            ctfHolder.find(".CTFSubsTotal").text(totalQusCount);
        }
        return true;
    };
    var getCTFEntries = function(ctfHolder) {
        var types = ["SCQ", "MCQ", "NUMERIC","PARA","MATRIX","SUBJECTIVE"];
        if (checkorResetCTFNum(ctfHolder)) {
            showcmdsPopupError(ctfHolder.closest("#cmdsPopup"), "Please enter numerical values only.");
            return {hasError: true};
        }
        var totalQues = 0, trs = ctfHolder.find("tr"), details = [];
        for (var k = 1; k < types.length+1; k++) {
            var tr = trs.eq(k), qusCount = checkEmptyVal(tr.find(".CTFQuesNo"));
            var positive = checkEmptyVal(tr.find(".CTFPosMarks"));
            var negative = checkEmptyVal(tr.find(".CTFNegMarks"));
            var maxQuestionsTobeAttempted=checkEmptyVal(tr.find(".CTFMaxQuestions"));
            if(maxQuestionsTobeAttempted <= 0){
                maxQuestionsTobeAttempted=qusCount;
            }
            if(isNTAPattern===true){
                console.log("isNTAPattern===true");
                details.push({qusCount: qusCount, type: types[k-1],maxQuestionsTobeAttempted:maxQuestionsTobeAttempted,
                marks: {positive: positive, negative: negative}});
            }
            else{
                console.log("isNTAPattern!=true");
                details.push({qusCount: qusCount, type: types[k-1],
                marks: {positive: positive, negative: negative}});
            }
            console.log(details);
            totalQues += qusCount;
        }
        console.log(details);
        return {hasError: false, details: details, qusCount: totalQues};
    };
    var checkorResetCTFNum = function(ctfHolder, type) {
        var CTFNums = ctfHolder.find(".CTFNum"), hasError = false;
        for (var i = 0; i < CTFNums.length; i++) {
            var val = CTFNums.eq(i).val();
            if (!checkIntNum(val) && val !== "") {
                hasError = true;
                break;
            }
            if (type === "RESET") {
                CTFNums.eq(i).val("0");
            }
        }
        return hasError;
    };
    var checkEmptyVal = function(input) {
        var val = input.val().trim();
        if (!val)
            return 0;
        else
            return parseInt(val);
    };

    var prepareCTFForAssignment = function(popup, mvcValues) {
        hidecmdsPopupError(popup);
        popup.find(".CTFQuesnsCountOnly").removeClass("nonner").siblings().addClass("nonner");
        var m = makeHTMLTag, divsHolder = m("div");
        var metadata = [];
        for (var k = 0; k < mvcValues.values.length; k++) {
            var subId = mvcValues.values[k], subName = mvcValues.names[k];
            // var d = m("tr", {"class": "CTFSubForAssignment"})
            //         .html("<td class=width100></td><td><input type=text /></td>");
            // d.find("td").eq(0).text(subName);
            var subjson = {id: subId, name: subName, qusCount: 0, details: [], children: []};
            var topicmvcValues = getmvcValues(mvcValues.mvcItems[k].find(".mvcTopics"));
            for (var p = 0; p < topicmvcValues.values.length; p++) {
                var topicId = topicmvcValues.values[p];
                var topicName = topicmvcValues.names[p];
                subjson.children.push({id: topicId, name: topicName});
            }
            // divsHolder.append(d);
            metadata.push(subjson);
        }
        var finalMetadata = [];
        var subjectOrder = popup.find(".selectedSubjectsList .subjOrder");
        var boardSeqId = [];
        subjectOrder.each(function(){
            boardSeqId.push($(this).data("boardId"));
        });
        for (var k = 0; k < boardSeqId.length; k++) {
            for (var j = 0; j < metadata.length; j++) {
                if(metadata[j].id===boardSeqId[k]){
                    finalMetadata.push(metadata[j]);
                    var d = m("tr", {"class": "CTFSubForAssignment"})
                    .html("<td class=width100></td><td><input type=text /></td>");
                    d.find("td").eq(0).text(metadata[j].name);
                    divsHolder.append(d);
                }
            }
        }
        ctfParams.metadata = finalMetadata;
        var subsTrs = divsHolder.children();
        popup.find(".CTFTableForAssignment").children("tbody").html(subsTrs);
        popup.find(".goToCTFTestFormat")
                .toggleClass("goToCTFTestFormat submitCreateAssignment");
    };

    //CREATE TEST SERIES
    var createTestSeries = function() {
        var popup = fillcmdsPopup("submitCreateTS", "createTSSample");
        loadRadioExams(popup.find(".mvcExamsTd"));
    };
    var submitCreateTS = function() {
        createTSPackageCDPUtil($(this), "/qrtests/createTestSeries");
    };


    //create study package
    var createPackage = function() {
        fillcmdsPopup("submitCreatePackage", "createPackageSample");
    };
    var submitCreatePackage = function() {
        createTSPackageCDPUtil($(this), "/qrproducts/createPackage");
    };
    var createCDP = function() {
        var popup = fillcmdsPopup("submitCreateCDP", "createCDPSample");
        loadmvChooseExams(popup.find(".createCDPExams"), {paramName: "targetIds"}, "formmvc");
    };
    var submitCreateCDP = function() {
        createTSPackageCDPUtil($(this), "/qrcdpplans/createCDP");
    };






















    var cmdsSourceInfo = function() {
        startLoader();
        var successFn = function(data) {
            var popup = getcmdsPopupBody(800);
            popup.html(data);
            stopLoader();
        };
        getReq("/qrsources/addedToInfo", {sourceId: $(this).data("folderId"), start: 0, size: 50}, successFn);
    };




    //delete resources
    var deleteResources = function() {
        var checkStatus = checkedEntities.init();
        /*if (checkStatus.hasPublished) { ISSUE #749
         showError("You cannot delete published resources.<br>Uncheck them and try again.");
         return;
         }*/
        var confirmtxt = "<div>Are you sure to <b>delete</b> selected contents? can not be un-done!</div>";
        showVYesNoBox(confirmtxt, null, function(state) {
            if (state) {
                doDelete();
            }
        });
        function doDelete() {
            if(checkStatus.srcEntities[0].type == "CMDSQUESTION"){
                var checkScope = checkQuesScope(checkStatus);
                if(checkScope === true){
                    return ;
                }
            }
            startLoader();
            var popup = checkStatus.prepareEntityClonesPopup("Delete Resources");
            cloneTableBody = popup.find(".entityCloneTable").children("tbody");
            cloneTableBody.children("tr.nonner").removeClass("nonner");
            var progressMsg = "deleting...";
            var entitiesById = [];
            for (var k = 0; k < checkStatus.entityIds.length; k++) {
                var entityId = checkStatus.entityIds[k];
                checkStatus.assignMsgInPopup(popup, entityId, progressMsg, "redTextColor big14");
                entitiesById[entityId] = checkStatus.entityList[k];
            }
            var params = {entities: checkStatus.srcEntities};
            var cbFn = function(data) {
                stopLoader();
                if (data && data.result && data.result.list.length > 0) {
                    var list = data.result.list;
                    for (var p = 0; p < list.length; p++) {
                        var item = list[p];
                        var entityId = item.content.id;
                        var msg = "deleted";
                        if (item.errorMessage && item.errorMessage != "null") {
                            msg = item.errorMessage;
                            $(entitiesById).removeProp(entityId);
                            entitiesById[entityId] = null;
                        } else {
                            $(entitiesById[entityId]).remove();
                        }
                        checkStatus.assignMsgInPopup(popup, entityId, msg, "redTextColor boldy");
                    }
                }
                if (entitiesById) {
                    var msg = "deleted";
                    for (entityId in entitiesById) {
                        if (!entitiesById[entityId])
                            continue;
                        checkStatus.assignMsgInPopup(popup, entityId, msg, "redTextColor boldy");
                        $(entitiesById[entityId]).remove();
                    }
                }
                resetChecks();
                resetcmdsCBoxes($(".mcWidget"));
            };
            postReq("/qrresources/deleteResources", params, cbFn, cbFn);
        }
    };

    var checkQuesScope = function(checkStatus){
        var scopeFlag = false;
        if(checkStatus.srcEntities.length > 0){
            for(i=0;i<checkStatus.srcEntities.length;i++){
                if($(".ques_"+checkStatus.srcEntities[i].id).data("scope") === "PRIVATE"){
                    scopeFlag = true;
                    break;
                }
            }
            if(scopeFlag === true){
                showError("Please uncheck learnpedia questions");
            }
        }
        return scopeFlag;
    }

    //publishing
    var publishDocVideos = function() {
        var checkStatus = checkedEntities.init();
        if (checkStatus.hasPublished) {
            showError("You selection contains published items.You cannot re-publish content.");
            return;
        }
        fillcmdsPopup("submitPublishDocVideos", "publishEntitySample");
    };
    var submitPublishDocVideos = function() {
        startLoader();
        var scope = $(this).closest("#cmdsPopup").find(".vChoose").data("value");
        var successFn = function(data) {
            stopLoader();
            closePopup();
            var fn = function(tr) {
                tr.data("status", statusPublished).text(statusPublished);
            };
            checkResultList(data.result.documents, $("#fixedSec").find(".cmdsTable tbody"), fn);
            resetChecks();
        };
        postReq("/qrDocuments/publishDocVideos", {docIds: checkedEnitiesClone.entityIds, scope: scope}, successFn);
    };
    this.addToOptForResources = {
        PACKAGE: function() {
            checkedEnitiesClone = checkedEntities.init();
            qrPackageCDPUtil.addToPackage();
        },
        TEST_SERIES: function() {
            cmdsAddToTS();
        },
        CDP: function() {
            checkedEnitiesClone = checkedEntities.init();
            if (checkedEnitiesClone.entityIds.length > 1) {
                showMessage("You can only add one resource to a CDP at one time.");
                return;
            }
            qrPackageCDPUtil.addToCDP();
        }
    };


    var cmdsAddToTS = function() {
        var popup = getcmdsPopupBody(null, null, {submitClass: "submitAddTestsToTS"})
        inlineLoader(popup);
        var params = {start: 0, size: 25};
        var url = "/qrproducts/addTestsToTSList";
        var successFn = function(data) {
            popup.html(data);
            var listDiv = popup.find(".entityListDiv");
            if (listDiv.length > 0) {
                loadMoreEntities.init(listDiv, url, params, null, "HTML");
            }
        }
        getReq("/qrproducts/addTestsToTS", params, successFn);
    };
    var submitAddTestsToTS = function() {
        var popup = $(this).closest("#cmdsPopup");
        var params = getFormValues(popup);
        var testIds = [];
        cmdsCBoxesChecked.each(function() {
            var p = $(this).closest("tr").data("entityId");
            if (p)
                testIds.push(p);
        });
        params.testIds = testIds;
        startLoader();
        var successFn = function(data) {
            stopLoader();
            closePopup();
            resetChecks();
        }
        postReq("/qrProducts/addTestsToTSSubmit", params, successFn);
    };
//    utils


    var folderVar = "FOLDER", questionSetVar = "CMDSQUESTIONSET", cdpVar = "CMDSCDP", pacakgeVar = "CMDSPACAKGE",
            testVar = "CMDSTEST", assignmentVar = "CMDSASSIGNMENT", testSeriesVar = "CMDSTESTSERIES",
            docVar = "CMDSDOCUMENT", videoVar = "CMDSVIDEO";


    var addToLibraryOutCasts = [folderVar, questionSetVar, cdpVar, pacakgeVar];
    var addToCDPOutCasts = [folderVar, questionSetVar, pacakgeVar, testSeriesVar, cdpVar];
    var addToPackageOutCasts = [folderVar, questionSetVar, pacakgeVar, testSeriesVar];
    this.cBoxChecked = function(optsDiv) {
        var typesChecked = getEntityTypesChecked();
        var resourceOpts = optsDiv.find(".resourceOpt");
        var commonTypes = [];

        //for add to program Library
        commonTypes = commonElsOfArrays(typesChecked, addToLibraryOutCasts);
        hideUnhideOpt(commonTypes, resourceOpts.eq(2));

        //for add to cdp
        commonTypes = commonElsOfArrays(typesChecked, addToCDPOutCasts);
        hideUnhideOpt(commonTypes, resourceOpts.eq(4));

        //for add to package
        commonTypes = commonElsOfArrays(typesChecked, addToPackageOutCasts);
        hideUnhideOpt(commonTypes, resourceOpts.eq(3));

        //for addtotestseries
        if (typesChecked.indexOf(testVar) > -1 && typesChecked.length === 1) {
            resourceOpts.eq(5).removeClass("nonner");
        } else {
            resourceOpts.eq(5).addClass("nonner");
        }

        var vChoose = optsDiv.children(".vChoose");
        if (vChoose.find(".nonner").length === 1) {
            vChoose.addClass("nonner");
        } else {
            vChoose.removeClass("nonner");
        }
    };
    var hideUnhideOpt = function(commonTypes, opt) {
        if (commonTypes.length > 0) {
            opt.addClass("nonner");
        } else {
            opt.removeClass("nonner");
        }
    };
}(jQuery);
var addToOptForResources = function(vChoose, value) {
    vChooseVar.reset(vChoose, "-1", "Add To");
    qrResources.addToOptForResources[value]();
};
var onAddResource = function(vChoose, value) {
    vChooseVar.reset(vChoose, "-1", "+ Add Content");
    qrResources.addResource[value](value);
};
var resourcesOptsCbfn = qrResources.cBoxChecked;

var uploadResourcePopup = new function() {
    var popup;
    var clickEvent = "click.ResourcePopupClose";
    var beforeCloseCB;
    var afterCloseCB;
    var TEXT = "There is a resource upload on-progress , do you want to cancel it and exit?";
    var defFn = function() {
        return true;
    };
    beforeCloseCB = afterCloseCB = defFn;
    this.register = function(popupBody, beforeCloseCBFn, afterCloseCBFn) {
        popup = popupBody.closest("#cmdsPopup");
        popup.find(".closecmdsPopup")
                .off(clickEvent, onAddResourcePopupClose)
                .on(clickEvent, onAddResourcePopupClose);
        beforeCloseCB = beforeCloseCBFn ? beforeCloseCBFn : defFn;
        afterCloseCB = afterCloseCBFn ? afterCloseCBFn : defFn;
    };
    var onDone = this.deRegister = function() {
        popup.find(".closecmdsPopup").off(clickEvent, onAddResourcePopupClose);
        beforeCloseCB = afterCloseCB = defFn;
    };
    var onAddResourcePopupClose = function(e) {
        var canClose = true;
        try {
            if (beforeCloseCB) {
                canClose = beforeCloseCB();
            }
        } catch (err) {
            canClose = true;
        }
        if (!canClose) {
            var ret = confirm(TEXT);
            if (!ret) {
                e.preventDefault();
                return false;
            }
        }
        try {
            afterCloseCB();
        } catch (err) {
        }
        onDone();
    };
};
var videoResource = new function() {
    var popup;
    var parDiv;
    var corsData = {};
    var contentAddType = "";
    this.open = function() {
        popup = getcmdsPopupBody(550);
        smallLoader(popup);
        vReq.post("/QrAddContent/addVideo", {}, function(data) {
            popup.html(data);
            uploadResourcePopup.register(popup, checkIsUploadProgress, cancelUploadInProgress);
            regFns();
        });
    };
    var regFns = function() {
        parDiv = popup.find("#addVideoContainer");
        parDiv.on("click", ".addVideoByTypeBtn", showAddVideoForm)
                .on("paste", ".addVideoPasteInput", fetchVideoByUrl)
                .on("click", "#doneAddVideo", commitVideo);
    };
    var checkIsUploadProgress = function() {
        var ret = corsUploader.isProgress();
        return !ret;
    };
    var cancelUploadInProgress = function() {
        corsUploader.cancel();
    };
    var showAddVideoForm = function() {
        var $this = $(this);
        var type = $this.data("type");
        parDiv.find(".selectVideoAddType").addClass("nonner");
        parDiv.find("#addVideoDiv").data("uploadedVidInfo", null).removeClass("nonner");
        corsData = {};
        switch (type) {
            case "URL" :
                showVideoAddByUrl();
                break;
            case "UPLOAD" :
                showVideoAddByUpload();
                break;
        }
        ;
    };
    var showVideoAddByUrl = function() {
        getVideoInfo = getAddedInfo;
        contentAddType = "ADDED";
        parDiv.find("#addVideoDiv").addClass("addVideoByUrl");
    };
    var showVideoAddByUpload = function() {
        getVideoInfo = getUploadedInfo;
        contentAddType = "UPLOAD";
        parDiv.find("#addVideoDiv").addClass("addVideoByUpload");
        //fetchScripts([{fname:"uicomWidgets/fileuploader.js",cb:uploaderJsLoaded}]);
        fetchScripts([{fname: "corsUploader.js", cb: uploaderJsLoaded}]);
    };
    var uploaderJsLoaded = function() {
        var pr = {
            type: "CMDSVIDEO",
            mediaType: "VIDEO",
            cbFns: {
                'onUrlSigned': function(file, params, data) {
                    corsData = data;
                },
                'onProgress': function() {
                },
                'onComplete': function(id, fileName, success, response, statusCode) {
                    if (success) {
                        parDiv.find("#addVideoDiv").data("uploadedVidInfo", corsData);
                    }
                },
                'onCancel': function() {
                },
                'onReUpload': function() {
                    parDiv.find("#addVideoDiv").data("uploadedVidInfo", undefined);
                    corsData = {};
                },
                'beforeSend': function() {
                }
            },
            fileTypes: ["mp4", "webm", "mpeg4", "wmv"],
            maxFileSize: 200 * 1024 * 1024, /* 200 MB */
            btnText: "Choose Video File",
            btnClass: "blueButton",
            allowMimeTypes: ["video/*"]
        };
        corsUploader.init(parDiv, popup.find(".uploadVidDiv"), pr);
    };
    var fetchVideoByUrl = function() {
        var $this = $(this);
        setTimeout(function() {
            var holder = parDiv.find(".addedVideoPreview");
            smallLoader(holder);
            var url = $this.val();
            vReq.get("/uicomwidgets/videoInfo", {url: url}, function(data) {
                holder.html(data).addClass("appended");
            });
        }, 100);
    };
    var commitVideo = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
        var videoParams = getVideoInfo();
        if (!videoParams) {
            if (contentAddType == "UPLOAD") {
                showError("Unable to add video, the required file is not uploaded yet!");
            } else if (contentAddType == "ADDED") {
                showError("Unable to add video, the required video is not fetched yet!");
            } else {
                showError("Unable to add video!");
            }
            return;
        }
        if (!videoParams.name) {
            showError("Please provide with a title to the video");
            return;
        }
        var folderId = $("#folderPage").data("folderId");
        folderId = folderId ? folderId : "";
        var params = {"folderId": folderId};
        params = $.extend(true, params, videoParams);
        var tagsJson = returnAllTagsAdded(parDiv.find(".addTagsForVideo"));
        if (tagsJson.subjectIds.length == 0 || tagsJson.topicIds.length == 0) {
            showError("Please select atleast one Subject and Topic");
            return;
        }
        params.brdIds = tagsJson.brdIds;
        $this.addClass("btnDisabled");
        startLoader();
        vReq.post("/QrAddContent/commitVideo", params, function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            closePopup();
            uploadResourcePopup.deRegister();
            trackEventForGA("CMDSVIDEO", "ADD_CONTENT", params.name);
            setTimeout(function() {
                refreshPage();
            }, 1200);
        }, function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            var errorCode = data.errorCode;
            var errorMessage = data.errorMessage ? data.errorMessage : data.errorCode;
            showError("Unable to add video Error :: " + errorMessage);
        });
    };
    var getVideoInfo = function() {
    };
    var getUploadedInfo = function() {
        var holder = parDiv.find("#addVideoDiv");
        var data = holder.data("uploadedVidInfo");
        if (!data || !data.id) {
            return;
        }
        var info = {
            name: holder.find('.addVideoTitleInput').val(),
            type: "UPLOADED",
            description: holder.find(".addVideoDesc").val(),
            videoId: data.id,
            originalFileName: data.fileName,
            uploadedFileName: data.requestParams.key,
            uuid: data.uuid,
            usage: document.getElementById('sdcard').checked ? 'sdcard' : 'web'
        };
        return info;
    };
    var getAddedInfo = function() {
        var holder = parDiv.find(".vVideoContent");
        var params = getFormValues(popup);
        if (params.hasError) {
            return;
        }
        var info = {
            name: params.title,
            thumbnail: params.thumbnail,
            url: params.videoUrl,
            type: "ADDED",
            description: holder.find(".vVideoDesc").html(),
            duration: params.duration * 1000,
            linkInfo: {
                id: params.videoId,
                originalURL: params.requestUrl,
                domainName: params.domainName,
                domainURL: params.domainUrl,
                embedURL: params.videoEmbedUrl
            }
        };
        return info;
    };
    this.cParams = {
        "commitUrl": "/QrAddContent/commitVideo",
        "parDiv": "addVidContainer",
        "type": "CMDSVIDEO",
        "typeInText": "Video",
        "mediaType": "VIDEO",
        "fileTypes": ["mp4", "webm", "mpeg4", "wmv"],
        "maxFileSize": 200 * 1024 * 1024,/* 200 MB */
        "btnText": "Choose Video File",
        "btnClass": "blueButton",
        "allowMimeTypes": ["video/*"],
        "fileCommitId": "videoId"
    };
    this.uiParams = {
        "title": "Add Video",
        "supportedText": "NOTE : Currently we support only 'webm' and 'MP4' files, please provide us with the same."
    };
};
var popupAddResource = function(inParams) {
    var popup;
    var parDiv;
    var corsData = {};
    var myParams = {
        "cbFns": inParams["cbFns"],
        "openUrl": inParams["openUrl"] ? inParams["openUrl"] : "/QrAddContent/addResourcePopup",
        "openParams": inParams["openParams"] ? inParams["openParams"] : {},
        "commitUrl": inParams["commitUrl"],
        "parDiv": inParams["parDiv"],
        "type": inParams["type"],
        "typeInText": inParams["typeInText"],
        "mediaType": inParams["mediaType"],
        "fileTypes": inParams["fileTypes"] ? inParams["fileTypes"] : [],
        "maxFileSize": inParams["maxFileSize"] ? inParams["maxFileSize"] : 100 * 1024 * 1024,
        "btnText": inParams["btnText"] ? inParams["btnText"] : "Choose File",
        "btnClass": inParams["btnClass"] ? inParams["btnClass"] : "blueButton",
        "allowMimeTypes": inParams["allowMimeTypes"] ? inParams["allowMimeTypes"] : [],
        "fileCommitId": inParams["fileCommitId"] ? inParams["fileCommitId"] : "id"
    };
    myParams["openParams"] = $.extend(true, myParams["openParams"], {"popupTypeMsg": myParams["mediaType"]});
    this.open = function() {
        popup = getcmdsPopupBody(480);
        smallLoader(popup);
        try {
            if (myParams.cbFns && myParams.cbFns.onPopupOpen) {
                myParams.cbFns.onPopupOpen();
            }
        } catch (err) {
        }
        vReq.get(myParams["openUrl"], myParams["openParams"], function(data) {
            popup.html(data);
            uploadResourcePopup.register(popup, checkIsUploadProgress, cancelUploadInProgress);
            regFns();
            fetchScripts([{fname: "corsUploader.js", cb: uploaderJsLoaded}]);
        });
    };
    var regFns = function() {
        parDiv = popup.find("#addResourcePopupContainer");
        parDiv.on("click", "#doneAddResourcePopup", commitResourcePopup);
    };
    var checkIsUploadProgress = function() {
        var ret = corsUploader.isProgress();
        return !ret;
    };
    var cancelUploadInProgress = function() {
        corsUploader.cancel();
    };
    this.resetParams = function(rParams) {
        $(rParams).removeProp("cbFns");
        var fileTypes = myParams["fileTypes"];
        myParams = $.extend(myParams, rParams);
        myParams["fileTypes"] = rParams["fileTypes"] ? rParams["fileTypes"] : fileTypes;
        var pr = {
            type: myParams["type"],
            mediaType: myParams["mediaType"],
            fileTypes: myParams["fileTypes"],
            maxFileSize: myParams["maxFileSize"],
            allowMimeTypes: myParams["allowMimeTypes"]
        };
        corsUploader.resetParams(parDiv, popup.find(".uploadResourcePopupDiv"), pr);
    };
    this.resetUi = function(p) {
        popup.find("#uploadResourceTitle").text(p["title"]);
        popup.find("#uploadResourceSupportedText").text(p["supportedText"]);
    };
    var uploaderJsLoaded = function() {
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
        var folderId = $("#folderPage").data("folderId");
        folderId = folderId ? folderId : "";
        var params = {"folderId": folderId};
        params = $.extend(true, params, videoParams);
        var tagsJson = returnAllTagsAdded(parDiv.find(".addTagsForResourcePopup"));
        if (tagsJson.subjectIds.length == 0 || tagsJson.topicIds.length == 0) {
            showError("Please select atleast one Subject and Topic");
            return;
        }
        params.brdIds = tagsJson.brdIds;
        startLoader();
        $this.addClass("btnDisabled");
        vReq.post(myParams["commitUrl"], params, function(data) {
            stopLoader();
            $this.removeClass("btnDisabled");
            closePopup();
            trackEventForGA(myParams.type, "ADD_CONTENT", params.name);
            uploadResourcePopup.deRegister();
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
            name: holder.find('.addResourcePopupTitleInput').val(),
            type: "UPLOADED",
            description: holder.find(".addResourcePopupDesc").val(),
            originalFileName: data.fileName,
            uploadedFileName: data.requestParams.key,
            uuid: data.uuid
        };
        info[idd] = data.id;
        return info;
    };
};
var docAddResource = new function() {
    var myParams = {
        "commitUrl": "/QrAddContent/commitDoc",
        "parDiv": "addDocContainer",
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
    this.params = myParams;
    this.uiParams = {
        "title": "Add Document",
        "supportedText": "NOTE : Currently we support only 'ppt','pptx','pdf','doc','docx' files, please provide us with the same."
    };
    var fwkObj = new popupAddResource(myParams);
    $.extend(this, fwkObj);
};
var fileAddResource = new function() {
    var TYPE_VIDEO = "VIDEO";
    var TYPE_DOC = "DOCUMENT";
    var typeDef = {};
    typeDef[TYPE_VIDEO] = videoResource.cParams;
    typeDef[TYPE_DOC] = docAddResource.params;
    var typeDefUi = {};
    typeDefUi[TYPE_VIDEO] = videoResource.uiParams;
    typeDefUi[TYPE_DOC] = docAddResource.uiParams;
    var uiParams = {
        "title": "Add File",
        "supportedText": "NOTE : Can upload any type of file, whoose size is less than 100 MB."
    };
    var knownTypes = {
        "application/msword": TYPE_DOC,
        "application/pdf": TYPE_DOC,
        "application/vnd.ms-powerpoint": TYPE_DOC,
        "application/pdf2": TYPE_DOC,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document": TYPE_DOC,
        "video/mp4": TYPE_VIDEO,
        "video/webm": TYPE_VIDEO,
        "video/wmv": TYPE_VIDEO,
        "video/mpeg4": TYPE_VIDEO
    };
    var onFileChoosen = function(file) {
        var fileType = file.type;
        var entityType = knownTypes[fileType];
        if (entityType && file.size <= typeDef[entityType].maxFileSize) {
            var ret = confirm("The file is getting uploaded, can also be used as '" + entityType + "', to use it as '" + entityType + "' press 'OK' or 'Yes' ! or treat as a download-able file only , use 'Cancel' or 'No'");
            if (ret) {
                fwkObj.resetParams(cloneObject(typeDef[entityType]));
                fwkObj.resetUi(typeDefUi[entityType]);
            }
        }
    };
    var onReload = function() {
        fwkObj.resetParams(cloneObject(myParams));
        fwkObj.resetUi(uiParams);
    };
    var onPopupOpen = onReload;
    var myParams = {
        "commitUrl": "/QrAddContent/commitFile",
        "parDiv": "addFileContainer",
        "type": "CMDSFILE",
        "typeInText": "File",
        "mediaType": "FILE",
        "fileTypes": [],
        /*"maxFileSize": 100 * 1024 * 1024, /* 100 MB */
        "maxFileSize": 1024 * 1024 * 1024, /* 1 GB */
        "allowMimeTypes": [],
        "btnText": "Choose File",
        "btnClass": "blueButton",
        "fileCommitId": "fileId"
    };
    var sendParams = cloneObject(myParams);
    sendParams["cbFns"] = {
        'onFileChoosen': onFileChoosen,
        'onReload': onReload,
        'onPopupOpen': onPopupOpen
    };
    var fwkObj = new popupAddResource(sendParams);
    $.extend(this, fwkObj);
};
function openCreateMODULE(id, folderId) {
    id = id ? id : "";
    var folderIdUrlStrip = folderId ? ("?folderId=" + folderId) : "";
    pushHistory(null, null, "/organization/" + cmdsOrgId + "/module/" + id + folderIdUrlStrip);
    opencmdsPage("/QrModules/home", {id: id}, $("#cmdsPrograms").parent());
};
function openGenerateTest(folderId) {
    var folderIdUrlStrip = folderId ? ("?folderId=" + folderId) : "";
    pushHistory(null, null, "/organization/" + cmdsOrgId + "/generatetest"+ folderIdUrlStrip);
    opencmdsPage("/QrTests/generateTest");
};
