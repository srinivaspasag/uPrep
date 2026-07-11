var generateTest = new function() {
    this.init = function(){
        var urlParams = fetchUrlParams();
        if (urlParams && urlParams.folderId) {
            folderId = urlParams.folderId;
        }
        var paramsFinal = {};
        var popup = $("#cmdsTestFormatSample");
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
        $("#generateTestHome").on("click",".goToCTFTestFormat",goToCTFTestFormat)
                            .on("click", ".CTFSubAuto", ctfSub)
                            .on("click", ".copyCTFToAllSubsAuto", copyCTFToAllSubs)
                            .on("change", ".CTFQuesAuto", ctfQuesNoTimed)
                            .on("click",".backButton",backButton)
                            .on("click", ".submitCreateTestAuto", submitCreateTest)
                            .on("dragstart", ".subjOrder", onSubjectEntryDragStart)
                             .on("dragend", ".subjOrder", onSubjectEntryDragEnd)
                             .on("dragover", ".subjOrder", onSubjectEntryDragOver)
                             .on("dragleave", ".subjOrder", onSubjectEntryDragLeave)
                             .on("drop", ".subjOrder", onSubjectEntryDragDrop);
    }
    var ctfParams;
    var goToCTFTestFormat = function() {
        var popup = $("#generateTestHome"), params = getFormValues(popup);
        var mvcValues = getmvcValues(popup.find(".mvcCTFSubs"));
        var duration = getHrsMinsSecs(popup.find(".timeDiv"));
        if (params.hasError || mvcValues.values.length === 0) {
            $(".CTFTitleTakerError").html("* All fields are compulsory")
            return;
        }
        if (duration <= 0) {
            $(".CTFTitleTakerError").html("Please enter proper duration");
            return;
        }
        $(".CTFTitleTakerError").html("");
        params.duration = duration;
        ctfParams = params;
        prepareCTF(popup, mvcValues);
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

    //create test/assignment utils
    var prepareCTF = function(popup, mvcValues) {
        popup.find(".CTFHolder").removeClass("nonner").siblings().addClass("nonner");
        var m = makeHTMLTag, divsHolder = m("div");
        var metadata = [];
        for (var k = 0; k < mvcValues.values.length; k++) {
            var subId = mvcValues.values[k], subName = mvcValues.names[k];
            // var d = m("div", {"class": "gButton CTFSubAuto"});
            // d.text(subName);
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
                    var d = m("div", {"class": "gButton CTFSubAuto"});
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
            popup.find(".copyCTFToAllSubsAuto").addClass("nonner");
        }
        else{
            popup.find(".copyCTFToAllSubsAuto").removeClass("nonner");
        }
    };
    var ctfQuesNoTimed = function() {
        var $this = $(this), ctfHolder = $this.closest(".CTFHolder");
        setTimeout(function() {
            ctfQuesNo($this, ctfHolder);
        }, 100);
    };
    var ctfQuesNo = function($this, ctfHolder) {
        var boxes = ctfHolder.find(".CTFQuesAuto");
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
                tr.find(".CTFQuesAuto").val(d.qusCount);
                tr.find(".CTFPosMarks").val(m.positive);
                tr.find(".CTFNegMarks").val(m.negative);
            }
            ctfHolder.find(".CTFSubTotal").text(metadata.qusCount);
        }
    };
    var copyCTFToAllSubs = function() {
        var r = populateMetadata($(this), "COPY");
        if (r) {
            $(".showStatus").html("<span class='greenColor boldy'>Successfully copied!!</span>");
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
        var ctfHolder = $("#generateTestHome").find(".CTFHolder");
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
            $(".showStatus").html("<span class='redColor'>Please enter numerical values only.</span>");
            return {hasError: true};
        }
        var totalQues = 0, trs = ctfHolder.find("tr"), details = [];
        for (var k = 1; k < types.length+1; k++) {
            var tr = trs.eq(k), qusCount = checkEmptyVal(tr.find(".CTFQuesAuto"));
            var positive = checkEmptyVal(tr.find(".CTFPosMarks"));
            var negative = checkEmptyVal(tr.find(".CTFNegMarks"));
            details.push({qusCount: qusCount, type: types[k-1],
                marks: {positive: positive, negative: negative}});
            totalQues += qusCount;
        }
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
        var val = input.val();
        if (!val)
            return 0;
        else
            return parseInt(val);
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
                    if(typeDetails.type === "SUBJECTIVE" && typeDetails.qusCount > 0){
                        ctfParams.subjectiveTest = true;
                    }
                }
            }
            if (err !== "") {
                $(".showStatus").html("<span class='redColor'>"+err+"</span>");
            } else {
                ctfParams.type = "TEST";
                createTestAssignmentSubmitUtil($this);
            }
        }
    };
    var createTestAssignmentSubmitUtil = function($this) {
        var resourceUrlFn = vcmdsUrls.ASSIGNMENT;
        var successFn = function(htmlContent) {
            hideTopLoader();
            $this.removeClass("btnDisabled");
            cSecHolder.html(htmlContent);
            closeStopCombo();
            var testId = cSecHolder.find("#testPage").data("entityId");
            trackEventForGA("CMDS" + ctfParams.type, "ADD_CONTENT", ctfParams.name);
            history.replaceState(null, null, resourceUrlFn(testId));
            trackPageView();
        };
        var errorFn = function() {
            hideTopLoader();
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
        ctfParams.autoGenerateFlag = true;
        if(ctfParams.autoGenerateFlag == true){
            resourceUrlFn = vcmdsUrls.GENERATETEST;
        }
        $this.addClass("btnDisabled");
        // console.log(ctfParams);
        showTopLoader();
        vReq.post("/qrtests/" + url, ctfParams, successFn, errorFn);
    };

    var backButton = function(){
        $(".CTFHolder").addClass("nonner");
        $(".CTFTitleTaker").removeClass("nonner");
    }
}