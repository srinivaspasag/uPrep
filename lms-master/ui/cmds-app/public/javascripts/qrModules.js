var qrModules = new function() {
    var documentClickEvent = "click.qrModules", clickEvent = "click";
    var modulePage, moduleId, moduleTitle, moduleEntriesHolder;
    var moduleEntryFinalisedClass = "moduleEntryFinalised";
    var mainSection, topicDotsLiner, moduleAddContentButton;
    this.init = function(params) {
        modulePage = $("#modulePage");
        moduleId = modulePage.data("moduleId");
        moduleTitle = modulePage.data("moduleTitle");
        moduleEntriesHolder = $("#moduleEntriesHolder");
        mainSection = $("#mainSection");
        topicDotsLiner = mainSection.children("#topicDotsLiner");
        moduleAddContentButton = mainSection.find("#moduleAddContentButton");
        modulePage
                .on("keyup", "#moduleTitleInput", moduleTitleEnterPressed)
                .on("blur", "#moduleTitleInput", moduleTitleBlurred)
                .on("keyup", ".moduleEntryTopicInput", moduleTopicTitleEnterPressed)
                .on("blur", ".moduleEntryTopicInput", moduleTopicTitleBlurred)
                .on("focus", ".moduleEntryTopicInput", moduleTopicTitleFocused)
        if (!params.published) {
            createSamples();
            modulePage
                    .on(clickEvent, ".moduleDeleteContentType", moduleDeleteEntry)
                    .on(clickEvent, ".moduleDeleteTopicType", moduleDeleteEntry)
                    .on(clickEvent, ".moduleEntryAddNewTopicType", moduleEntryAddNewTopicType)
                    .on(clickEvent, ".moduleEntryTopicTypeAddBox", moduleEntryTopicTypeAddBox)
                    .on(clickEvent, ".addContentToModule", moduleAddContent)
                    .on(clickEvent, '.addModuleTags', addModuleTags)
                    .on(clickEvent, ".addContentToTopic", addContentToTopic)
                    .on(clickEvent, "#saveModule", saveModule)
                    .on(clickEvent, ".scheduleTestInModule",scheduleTestInModule)
                    .on(clickEvent, ".deleteSchedule",deleteSchedule)
                    //for draggable source
                    .on("dragstart", ".moduleEntryContentType", onModuleEntryDragStart)
                    .on("dragend", ".moduleEntryContentType", onModuleEntryDragEnd)
                    //for target dropping area
                    .on("dragover", ".moduleEntryFinalised,.moduleEntryTopicTypeWithAddCircle", onModuleEntryDragOver)
                    .on("dragleave", ".moduleEntryFinalised,.moduleEntryTopicTypeWithAddCircle", onModuleEntryDragLeave)
                    .on("drop", ".moduleEntryFinalised,.moduleEntryTopicTypeWithAddCircle", onModuleEntryDragDrop)

            $(document).off(documentClickEvent)
                    .on(documentClickEvent, "#addFinalModuleContent", addFinalModuleContent)
                    .on(documentClickEvent, ".getPopupResourcesFolderPage", getPopupResourcesFolderPage)
                    .on(documentClickEvent, ".addFinalModuleTags", addFinalModuleTags)
            loadPreRequisitesModuleIds();
            resetUpModuleEntriesSection();
        } else {
            putCountForTopics();
            putCountForContent();
        }
    };

    var getReq = vReq.get;
    var postReq = vReq.post;

    var moduleEntryToDrag;
    var onModuleEntryDragStart = function(e) {
        var dataTransfer = e.originalEvent.dataTransfer;
        dataTransfer.setData('Text', null);
        moduleEntryToDrag = $(this);
    };
    var onModuleEntryDragEnd = function(e) {
        moduleEntryToDrag = null;
    };
    var onModuleEntryDragOver = function(e) {
        var event = e.originalEvent;
        if (event.pageY - $(window).scrollTop() <= 25) {
            $(window).scrollTop(event.pageY - 25);
        }
        var entry = $(this);
        var cssVal = "visible";
        if (entry.hasClass("moduleEntryTopicTypeWithAddCircle") &&
                entry.is(moduleEntriesHolder.children(".moduleEntry").last())) {
            var cssVal = "hidden";
        }
        $(this).find(".moduleEntryMoveDisplayer").css({visibility: cssVal});
        return false;
    };
    var onModuleEntryDragLeave = function(e) {
        $(this).find(".moduleEntryMoveDisplayer").css({visibility: "hidden"});
    };
    var onModuleEntryDragDrop = function(e) {
        var event = e.originalEvent;
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        $(this).find(".moduleEntryMoveDisplayer").css({visibility: "hidden"});
        if (moduleEntryToDrag) {
            var targetEntry = $(this);
            var oldPos = moduleEntryToDrag.index("." + moduleEntryFinalisedClass);
            var pos = -1;
            if (targetEntry.hasClass("moduleEntryTopicTypeWithAddCircle")) {
                var entries = moduleEntriesHolder.children(".moduleEntry");
                if (targetEntry.is(entries.last())) {
                    pos = -1;
                } else if (targetEntry.is(entries.first())) {
                    pos = 0;
                }
            } else {
                pos = targetEntry.index("." + moduleEntryFinalisedClass);
                if (pos < oldPos) {
                    pos++;
                }
            }
            if (pos !== -1 && oldPos !== pos) {
                moduleEntryToDrag.insertAfter(targetEntry);
                resetUpModuleEntriesSection();
                postReq("/qrmodules/moveModuleEntry", {moduleId: moduleId, pos: pos, oldPos: oldPos});
            }
            moduleEntryToDrag = null;
        }
        return false;
    };

    var loadPreRequisitesModuleIds = function() {
//        getReq("/qrmodules/getAllModules", {}, function() {
//            var modules = [];
//            var 
//            for (var k = 0, l = modules.length; k < l; k++) {
//                var 
//            }
//        });
    };
    var createSamples = function() {
        var sampleChildren = $("#modulePageSamples").children();
        for (var k = 0; k < sampleChildren.length; k++) {
            var el = sampleChildren.eq(k);
            var s = el.attr("rel");
            qrModules[s] = el.clone(true);
            el.remove();
        }
    };

    //title of module/module
    var doBlurActionForModuleTitle = true;
    var moduleTitleBlurred = function() {
        if (doBlurActionForModuleTitle) {
            var val = $(this).val().trim();
            if (val !== "") {
                changeModuleTitle(val);
            } else if (moduleId && moduleTitle) {
                $(this).val(moduleTitle);
            }
        }
        doBlurActionForModuleTitle = true;
    };
    var moduleTitleEnterPressed = function(e) {
        var val = $(this).val().trim();
        if (((e.which && e.which === 13) || (e.keyCode && e.keyCode === 13))) {
            doBlurActionForModuleTitle = false;
            $(this).blur();
            if (val !== "") {
                changeModuleTitle(val);
            } else {
                alert("Please enter a valid title for Module");
            }
        }
    };
    var changeModuleTitle = function(val) {
        if (!moduleId) {
            var params = {name: val};
            var urlParams = fetchUrlParams();
            if (urlParams && urlParams.folderId) {
                params.folderId = urlParams.folderId;
            }
            postReq("/qrmodules/createmodule", params, function(data) {
                var result = data.result;
                moduleId = result.id;
                moduleTitle = val;
                modulePage.data("moduleId", moduleId);
                modulePage.data("moduleTitle", moduleTitle);
                pushHistory(null, null, vcmdsUrls.MODULE(moduleId), true);
            });
        } else {
            postReq("/qrmodules/updatemodule", {name: val, id: moduleId, updateList: ["name"]}, function(data) {
                moduleTitle = val;
                modulePage.data("moduleTitle", moduleTitle);
            });
        }
    };


    //add Tags
    var addModuleTags = function() {
        if (!moduleId) {
            showError("Name the module first");
            return;
        }
//        popup.html(qrModules.addModuleTagsSample.clone(true));
//        var targetSubs = popup.find(".mvcCTFSubsTd");
//        inlineLoader(targetSubs);
//        var tagsSpans = modulePage.find(".moduleTagsSpans").children();
//        var addedTags = [];
//        for (var p = 0, l = tagsSpans.length; p < l; p++) {
//            addedTags.push(tagsSpans.eq(p).data("brdId"));
//        }
//        var cbfn = function(subs) {
//            var newmvc = makeHTMLTag("div", {"class": "multiplevChoose mvcCTFSubs",
//                "data-param-name": "brdIds"});
//            for (var k = 0; k < subs.length; k++) {
//                var item = mvcItemSample.children().clone(true), t = subs[k];
//                item.find(".gCBox").data({value: t.id, callback: "insertmvcTopics"});
//                if (addedTags.indexOf(t.id) > -1) {
//                    item.find(".gCBox").addClass("gCBoxChecked");
//                }
//                item.find(".gCBoxText").text(t.name);
//                newmvc.append(item);
//            }
//            targetSubs.html(newmvc);
//        };
//        loadAndCacheCourses(cbfn);
        startLoader();
        vReq.get("/qrmodules/editModuleTags", {id: moduleId}, function(data) {
            stopLoader();
            var popup = getcmdsPopupBody(500, null, {submitClass: "addFinalModuleTags"});
            popup.html(data);
        });
    };


    var addFinalModuleTags = function() {
        var popup = getPopupDiv($(this));
        var tagsJson = returnAllTagsAdded(popup.find("#addTagsMasterDiv"));
        var params = {id: moduleId, updateList: ["boardIds"]};
        params.brdIds = tagsJson.brdIds;
        var tagNames = $.merge(tagsJson.subjects, tagsJson.topics);
        startLoader();
        var successFn = function(data, text, xhr) {
            stopLoader();
            closePopup();
            var tagsDiv = modulePage.find(".moduleTagsDiv");
            var spansDiv = modulePage.find(".moduleTagsSpans");
            if (tagNames.length > 0) {
                tagsDiv.removeClass("nonner");
                var holder = makeHTMLTag("div");
                for (var k = 0, l = tagNames.length; k < l; k++) {
                    var name = tagNames[k];
                    if (k < l - 1) {
                        name += ", ";
                    }
                    holder.append("<span>" + name + "</span>");
                }
                spansDiv.html(holder.children());
            } else {
                tagsDiv.addClass("nonner");
                spansDiv.html("");
            }
        };
        postReq("/qrmodules/updateModule", params, successFn);
    };


    //adding the topics
    var moduleEntryAddNewTopicType = function() {
        var entriesForTopicInput = moduleEntriesHolder.children(".moduleEntryTopicTypeWithInput");
        for (var k = 0, l = entriesForTopicInput.length; k < l; k++) {
            var topicEntry = entriesForTopicInput.eq(k);
            var val = topicEntry.find("input").val().trim();
            if (!val && !topicEntry.hasClass(moduleEntryFinalisedClass)) {
                topicEntry.remove();
            }
        }
        var entry = $(this).closest(".moduleEntryContentType");
        $(qrModules.moduleEntryTopicTypeWithInput.clone(true)).insertBefore(entry);
        resetUpModuleEntriesSection();
    };
    var moduleEntryTopicTypeAddBox = function() {
        var entry = $(this).closest(".moduleEntry");
        $(qrModules.moduleEntryTopicTypeWithInput.clone(true)).insertBefore(entry);
        entry.remove();
        putCountForTopics();
    };
    var doBlurActionForTopicTitle = true;
    var moduleTopicTitleFocused = function() {
        if (!moduleId) {
            showError("Name the module first");
            doBlurActionForTopicTitle = false;
            $(this).blur();
            return;
        }
    };
    var moduleTopicTitleBlurred = function() {
        if (doBlurActionForTopicTitle) {
            var val = $(this).val().trim();
            var entry = $(this).closest(".moduleEntry");
            var oldTitle = entry.data("entryTitle");
            if (val !== "") {
                changeTopicTitle(val, entry);
            } else if (oldTitle) {
                $(this).val(oldTitle);
            }
        }
        doBlurActionForTopicTitle = true;
    };

    var moduleTopicTitleEnterPressed = function(e) {
        var val = $(this).val().trim();
        if (((e.which && e.which === 13) || (e.keyCode && e.keyCode === 13))) {
            doBlurActionForTopicTitle = false;
            var entry = $(this).closest(".moduleEntry");
            $(this).blur();
            if (val !== "") {
                changeTopicTitle(val, entry);
            } else {
                alert("Please enter a valid title for topic");
                entry.find(".inputDiv").addClass("moduleEntryTopicInputDivGrayed");
            }
        }
    };
    var changeTopicTitle = function(val, entry) {
        var oldEntryTitle = entry.data("entryTitle");
        var params = {moduleId: moduleId};
        var urlStrip = "addModuleEntries";
        if (oldEntryTitle) {
            urlStrip = "updateModuleEntries";
            params.name = val;
            params.pos = entry.index("." + moduleEntryFinalisedClass);
        } else {
            params.children = [{name: val}];
            params.pos = entry.prevAll("." + moduleEntryFinalisedClass).first().index("." + moduleEntryFinalisedClass) + 1;
        }
        postReq("/qrmodules/" + urlStrip, params, function() {
            entry.data("entryTitle", val);
            entry.addClass(moduleEntryFinalisedClass);
            resetUpModuleEntriesSection();
        });
        entry.find(".inputDiv").removeClass("moduleEntryTopicInputDivGrayed");
    };


    //add content
    var targetForAddContent;
    var targetPosForAddContent;
    var moduleAddContent = function() {
        if (!moduleId) {
            showError("Name the module first");
            return;
        }
        var popup = getcmdsPopupBody(700);
        fetchResources(popup);
        targetForAddContent = moduleEntriesHolder.children("." + moduleEntryFinalisedClass).last();
        targetPosForAddContent = null;
    };
    var addContentToTopic = function() {
        var topicEntry = $(this).closest(".moduleEntry");
        if (topicEntry.hasClass(moduleEntryFinalisedClass)) {
            var lastProbableEntry = topicEntry.nextUntil('.moduleEntryTopicType', '.moduleEntryContentType').last();
            if (lastProbableEntry.length > 0) {
                targetForAddContent = lastProbableEntry;
            } else {
                targetForAddContent = topicEntry;
            }
            targetPosForAddContent = targetForAddContent.index("." + moduleEntryFinalisedClass) + 1;
            var popup = getcmdsPopupBody(700);
            fetchResources(popup);
        }
    };
    var addFinalModuleContent = function() {
        var checkStatus = checkedEntities.init();
        var trs = checkStatus.entityList;
        var selectedIds = checkStatus.entityIds;
        var srcEntities = checkStatus.srcEntities;
        if (srcEntities.length > 0) {
            var addedContentEntries = moduleEntriesHolder.children(".moduleEntryContentType");
            var hasError = false;
            for (var n = 0, l = addedContentEntries.length; n < l; n++) {
                var entityId = addedContentEntries.eq(n).data("entityId");
                if (entityId && selectedIds.indexOf(entityId) > -1) {
                    showError("Some of the content selected is already added.<br>\n\
                    Remove them and try again");
                    hasError = true;
                    break;
                }
            }
            if (hasError) {
                return;
            }
            var children = [];
            for (var p = 0, l = srcEntities.length; p < l; p++) {
                children.push({entity: srcEntities[p]});
            }
            var params = {children: children, moduleId: moduleId};
            if (targetPosForAddContent) {
                params.pos = targetPosForAddContent;
            } else {
                params.pos = -1;
            }
            closecmdsPopup();
            startLoader();
            postReq("/qrmodules/addModuleEntries", params, function(data) {
                stopLoader();
                if (data.result && data.result.success) {
                    var holder = $(document.createElement("div"));
                    for (var k = 0, l = srcEntities.length; k < l; k++) {
                        var srcEntity = srcEntities[k];
                        var entry = qrModules["moduleEntryContentType"].clone(true);
                        entry.addClass(moduleEntryFinalisedClass);
                        var nameDivs = trs[k].clone(true).find(".entityTitleTd").children();
                        nameDivs.insertBefore(entry.find(".moduleContentStat"));
                        entry.data({entityType: srcEntity.type, entityId: srcEntity.id});
                        var vChoose = entry.find(".vChoose");
                        var type = srcEntity.type;
                        if (type === "CMDSFILE") {
                            vChoose.find(".vChooseOpt").last().remove();
                        } else {
                            vChoose.find(".vChooseOpt").last().text("Required");
                        }
                        holder.append(entry);
                    }
                    if (targetForAddContent.length > 0) {
                        holder.children().insertAfter(targetForAddContent);
                        resetUpModuleEntriesSection();
                    } else if (targetForAddContent.length === 0) {
                        moduleEntriesHolder.html(holder.children());
                        resetUpModuleEntriesSection();
                    } else {
                        showError("Some error occured. Try again.");
                    }
                } else {
                    showError("Either the content is already added or some error occured");
                }
            });
        } else {
            showError("Select Content to add");
        }
    };
    var getPopupResourcesFolderPage = function() {
        fetchResources($(this).closest(".cmdsPopupBody"), $(this));
    };
    var fetchResources = function(popup, $this) {
        popup.html("<div class='userMessage'>Fetching Content...</div>");
        var params = {};
        if ($this) {
            params.folderId = $this.data("entityId");
        }
        startLoader();
        getReq("/qrresources/popupResources", params, function(data) {
            stopLoader();
            popup.html(data);
	    popup.find(".addFromPopupResource").attr("id","addFinalModuleContent");
        });
    };


    //delete entities
    var moduleDeleteEntry = function() {
        var entry = $(this).closest(".moduleEntry");
        if (entry.hasClass(moduleEntryFinalisedClass)) {
            var pos = entry.index("." + moduleEntryFinalisedClass);
            var params = {moduleId: moduleId, pos: pos};
            postReq("/qrmodules/deleteModuleEntry", params);
        }
        if (entry.hasClass("moduleEntryTopicTypeWithInput")) {
            var entries = moduleEntriesHolder.children(".moduleEntry");
            if (entry.is(entries.first()) ||
                    entry.is(entries.last())) {
                qrModules.moduleEntryTopicTypeWithAddCircle.clone(true).insertBefore(entry);
            }
        }
        entry.remove();
        resetUpModuleEntriesSection();
    };

    var saveModule = function() {
        var backUpUrl = $(this).data("backUpUrl");
        goToBackPage(backUpUrl);
    };

    var getScheduleParams = function(holder){
        //Entity is CMDSTEST.
        var entityId = holder.closest(".moduleEntry").data("entityId");
        //Source is CMDSMODULE.
        var sourceId = modulePage.data("moduleId");
        //Target is SECTION.
        var targetId = modulePage.data("targetId");
        var scheduleParams = {
            target:{
                id:targetId,
                type:"SECTION"
            },
            entity:{
                id:entityId,
                type:"CMDSTEST"
            },
            source:{
                id:sourceId,
                type:"CMDSMODULE"
            }
        };
        return scheduleParams;
    }

    var scheduleTestInModule = function(){
        var holder = $(this);
        var popup = getcmdsPopupBody(700);
        popup.html(scheduleTestSample.children().clone(true));
        var testName = $(this).closest(".moduleEntryContentTypeMainSec").find(".singleLineText").text();
        popup.find("#scheduleTestName").html("<h2>Scheduling Test for "+"<span style='text-transform:capitalize;'>"+testName+"</span></h2><hr/>");
        popup.find(".publish-message").addClass("nonner");
        popup.find(".module-info-message").removeClass("nonner").html("<span class='redColor padRight10'>*</span>Uncheck the Make Sequential option to  make Schedule Test option work as expected.<br><span class='redColor padRight10'>*</span>Invisible the Module and Visible again to reflect the Changes at User End.");
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

        function scheduleTest(){
            var dates = {
                start:{
                    date:$("#Start").datepicker('getDate')
                },
                end:{
                    date:$("#End").datepicker('getDate')
                },
                close:{
                    date:$("#Close").datepicker('getDate')
                }
            }
            var initialTime = 0;
            var params = getScheduleParams(holder);
            params.schedule = {};
            for(var key in dates){
                if(dates[key]['date'] != null){
                    var cKey = key.charAt(0).toUpperCase() + key.slice(1);
                    var sDuration = getHrsMinsSecs(popup.find("."+cKey+" .timeDiv"));
                    var sTime = dates[key]['date'].getTime() + sDuration;
                    if((today > sTime) || (sTime < initialTime)){
                        showError("Please select valid "+key+" time ");
                        return;
                    }
                    dates[key]['time'] = sTime;
                    initialTime = dates[key]['time'];
                    params['schedule'][key+'Time'] = new Date(sTime).toString();
                }
            }
            vReq.post("/QrModules/setSchedule",params,function(data){
                closecmdsPopup();
                showMessage("Schedule set successfully");
                setTimeout(function(){
                    refreshPage();
                },1000);
            });
        }
    }

    var deleteSchedule = function(){
        var holder = $(this);
        var params = getScheduleParams(holder);
        showVYesNoBox("Are you sure to delete schedule for the test", null, function(state) {
        if (state) {
             vReq.post("/QrModules/deleteSchedule",params,function(data){
                showMessage("Schedule removed successfully");
                setTimeout(function(){
                    refreshPage();
                },1000);
            });
        }
    });
    }

    //utils
    var resetUpModuleEntriesSection = function() {
        moduleEntriesHolder.children(".moduleEntryAddContentButtonDiv").remove();
        var firstEntry = moduleEntriesHolder.children(".moduleEntry").eq(0);
        var secondEntry = moduleEntriesHolder.children(".moduleEntry").eq(1);
        moduleAddContentButton.removeClass("nonner");

        if (firstEntry.length === 0) {
            moduleEntriesHolder.html(qrModules.moduleEntryTopicTypeWithInput.clone(true));
        } else if (firstEntry.hasClass("moduleEntryContentType")) {
            qrModules.moduleEntryTopicTypeWithAddCircle.clone(true).insertBefore(firstEntry);
        } else if (firstEntry.hasClass("moduleEntryTopicTypeWithAddCircle") &&
                secondEntry.hasClass("moduleEntryTopicType")) {
            firstEntry.remove();
        } else if (firstEntry.hasClass("moduleEntryTopicTypeWithInput")
                && !firstEntry.hasClass(moduleEntryFinalisedClass)
                && moduleEntriesHolder.children("." + moduleEntryFinalisedClass).length > 0) {
            qrModules.moduleEntryTopicTypeWithAddCircle.clone(true).insertBefore(firstEntry);
            firstEntry.remove();
        }

        var lastEntry = moduleEntriesHolder.children(".moduleEntry").last();
        if (lastEntry.hasClass("moduleEntryContentType")) {
            qrModules.moduleEntryTopicTypeWithInput.clone(true).insertAfter(lastEntry);
        }

        lastEntry = moduleEntriesHolder.children(".moduleEntry").last();
        if (lastEntry.hasClass("moduleEntryTopicType")
                && !lastEntry.hasClass(moduleEntryFinalisedClass)
                && moduleEntriesHolder.children("." + moduleEntryFinalisedClass).length > 0) {
            if (lastEntry.prev(".moduleEntryTopicType").length > 0) {
                lastEntry.remove();
            } else {
                moduleAddContentButton.addClass("nonner");
                qrModules.moduleEntryAddContentButtonDiv.clone(true).insertBefore(lastEntry);
            }
        }
        putCountForTopics();
        putCountForContent();
    };
    var putCountForContent = function() {
        var allContents = moduleEntriesHolder.children(".moduleEntryContentType");
        for (var k = 0, l = allContents.length; k < l; k++) {
            var entry = allContents.eq(k);
            entry.find(".moduleContentNo").text((k + 1) + ".");
        }
    };
    var putCountForTopics = function() {
        var allTopics = moduleEntriesHolder.children(".moduleEntryTopicType")
                .not(".moduleEntryTopicTypeWithAddCircle");
        for (var k = 0, l = allTopics.length; k < l; k++) {
            var entry = allTopics.eq(k);
            entry.find(".moduleEntryTopicTitleCount").text(indexToChar(k).toUpperCase());
            if (entry.find("input").length > 0 && !entry.find("input").val().trim()) {
                entry.find(".inputDiv").addClass("moduleEntryTopicInputDivGrayed");
            }
        }
        putLinerForTopics();
    };
    var putLinerForTopics = function() {
        var m = mainSection.offset();
        var topics = moduleEntriesHolder.children(".moduleEntryTopicType");
        var topTopicDot = topics.first().find(".moduleEntryTopicTitleCount,.moduleEntryTopicTypeAddBox");
        var topTopicDotOffset = topTopicDot.offset();
        if (!topTopicDotOffset) {
            return;
        }
        var x1 = topTopicDotOffset.left - m.left;
        var y1 = topTopicDotOffset.top - m.top;
        var finalTopDimen = y1 + topTopicDot.outerHeight();
        topicDotsLiner.css({left: x1 + (topTopicDot.outerWidth() / 2) - 1, top: finalTopDimen});
        var bottomTopicDot = topics.last().find(".moduleEntryTopicTitleCount,.moduleEntryTopicTypeAddBox");
        var bottomTopicDotOffset = bottomTopicDot.offset();
        var y2 = bottomTopicDotOffset.top - m.top;
        topicDotsLiner.height(y2 - finalTopDimen);
    };
};
var toggleModuleSequential = function(cBox, isChecked) {
    var moduleData = cBox.closest("#modulePage").data();
    var moduleId = moduleData.moduleId;
    if (!moduleId) {
        showError("Name the module first");
        return;
    }
    var params = {id: moduleId, updateList: ["moduleRun"]};
    if (isChecked) {
        params.moduleRun = "SEQUENTIAL";
    } else {
        params.moduleRun = "NON_SEQUENTIAL";
    }
    vReq.post("/qrmodules/updatemodule", params);
};
var updateModuleEntryCompletionReq = function(vChoose, value) {
    var entry = vChoose.closest(".moduleEntry");
    var moduleId = entry.closest("#modulePage").data("moduleId");
    var params = {moduleId: moduleId};
    var ruleType = "NONE";
    if (value != -1) {
        ruleType = "VIEW";
    }
    params["completionRule.type"] = ruleType;
    params.pos = entry.index(".moduleEntryFinalised");
    vReq.post("/qrmodules/updateModuleEntries", params);
};
