var qrProgram = new (function($) {
    var programPage, bodyClickEvent = "click.qrProgram", clickEvent = "click",
            mainContentSection, programPageHeadDiv, currentAreaType = "content";
    this.programId, this.centerId, this.sectionId;
    this.contentUrlParams = {includes: "", orderBy: "timeCreated", query: "", centerId: "", sectionId: "", courseId: "", start: 0, size: 50};
    this.uploadMarkSheetsUrlParams = {orderBy: "timeCreated", query: "", centerId: "", sectionId: "", start: 0, size: 50};
    this.membersUrlParams = {targetProfile: "TEACHER", centerId: "", sectionId: "", query: "", start: 0, size: 50};
    this.studentsUrlParams = {centerId: "", sectionId: "", query: "", start: 0, size: 50};
    this.organizationsUrlParams = {orgsKeyValue: "", orderBy: "timeCreated", query: "", centerId: "", sectionId: "", courseId: "", start: 0, size: 50};
    this.classroomConnectUrlParams = {centerId:"",sectionId:""}
    this.init = function(params) {
        programPage = $("#programPage");
        putActiveMenuItem($("#cmdsPrograms").parent());
        mainContentSection = programPage.children("#mainSection");

        //load the appropriate content on the page
        var areaType = params.areaType || "CONTENT";
        setProgPageAcadIds(params);
        //changes in showing shared organizations
	decideSectionPage(qrProgram.sectionId);
        switch (areaType.toUpperCase()) {
            case "CONTENT":
                {
                    showProgContent(null, "DIRECT");
                    break;
                }
            case "MEMBERS":
                {
                    showProgMembers(null, "DIRECT");
                    break;
                }
            case "ORGANIZATIONS":
            {
                //showProgMembers(null, "DIRECT");
                showProgOrganizations(null, "DIRECT");
                break;
            }
            case "STUDENTS":
                {
                    showProgStudents(null, "DIRECT");
                    break;
                }
            case "UPLOADMARKSHEETS":
                {
                    uploadOfflineTests(null, "DIRECT");
                    break;
                }
            case "SCHEDULE":
                {
                    showClassroomConnect(null, "DIRECT");
                    break;
                }
        }


        // changes to show shared orgs
        programPage.off(clickEvent)
                .on(clickEvent, ".changeProgPageProgram", changeProgPageProgram)
                .on(clickEvent, "#showProgContent", showProgContent)
                .on(clickEvent, "#showProgMembers", showProgMembers)
                .on(clickEvent, "#showProgOrganizations", showProgOrganizations)
                .on(clickEvent, "#showProgStudents", showProgStudents)
                .on(clickEvent, "#uploadOfflineTests", uploadOfflineTests)
                .on(clickEvent,"#showClassroomConnect",showClassroomConnect)
                .on(clickEvent, ".removeFromProgramLibrary", removeFromLibrary)
                .on(clickEvent, "#switchSectionSdOnly .switchTab", changeSectionSdOnly)
                .on(clickEvent, ".removeSharingBtn" , removeProgramSharing)


        programPageHeadDiv = $("#programPageHeadDiv");
        $("body").off(bodyClickEvent)
                .on(bodyClickEvent, ".submitChangedProgram", submitChangedProgram)
                .on(bodyClickEvent, ".submitSectionForMovingStudents", submitSectionForMovingStudents);


        var fn = function() {
            putProgramInfoOnHead(params);
        };
        if (memberInfoPojo.loaded) {
            fn();
        } else {
            memberInfoPojocbfn = fn;
        }
    };
    var getReq = vReq.get;
    var postReq = vReq.post;

    var removeFromLibrary = function() {
        var $this = $(this);
        var confirmtxt = "<div>Are you sure to <b>remove</b> selected contents from library?<br>You can add the same, later from Resources.</div>";
        showVYesNoBox(confirmtxt, null, function(state) {
            if (state) {
                doDelete();
            }
        });
        function doDelete() {
            var checkCBoxes = checkedEntities.init();
            var params = {contents: checkCBoxes.srcEntities};
            var orgEntities = [];
            if (qrProgram.sectionId) {
                var orgEntity = {"id": qrProgram.sectionId, "type": "SECTION"};
                orgEntities.push(orgEntity);
            }
            else if (qrProgram.programId) {
                var orgEntity = {"id": qrProgram.programId, "type": "PROGRAM"};
                orgEntities.push(orgEntity);
                if (qrProgram.centerId) {
                    var orgEntity = {"id": qrProgram.centerId, "type": "CENTER"};
                    orgEntities[0].centers = [];
                    orgEntities[0].centers.push(orgEntity);
                    if (qrProgram.sectionId) {
                        var orgEntity = {"id": qrProgram.sectionId, "type": "SECTION"};
                        orgEntities[0].centers[0].sections = [];
                        orgEntities[0].centers[0].sections.push(orgEntity);
                    }
                }
            }
            params["orgEntities"] = orgEntities;
            vReq.post("/QrPrograms/removeFromLibrary", params, function(data) {
                if (data && data.errorCode == "" && data.result) {
                    if(data.result.cumulativeErrorCode != null && data.result.cumulativeErrorCode != ""){
                        showError(data.result.cumulativeErrorCode);
                        return;
                    }
                    for (var i = 0; i < checkCBoxes.entityList.length; i++) {
                        checkCBoxes.entityList[i].remove();
                    }
                    resetcmdsCBoxes();
                    showMessage("Completed!!");
                }
            });
        }
        ;
    };
    var afterContentLoaded = function(mcWidget) {
        var mcWidgetParams = mcWidget.data("params");
        var finalParams = getFinalUrlParamsForPage(mcWidget);
        var newUrl = getProgramPagePathName(mcWidgetParams.programId) + "?" + $.param(finalParams);
        pushHistory(null, null, newUrl);
        fixContentSec();
    };
    this.afterContentLoaded = afterContentLoaded;
    var changeProgPageProgram = function() {
        startLoader();
        var successFn = function(data) {
            var popup = getcmdsPopupBody(700);
            popup.html(data);
            $("#programPageSelectProgramSubmit").removeClass("submitSectionForMovingStudents")
                    .addClass("submitChangedProgram").data({});
            fetchScripts([{fname: "qrAcadStr.js",
                    cb: setAcadStrTable}]);
            stopLoader();
        };
        getReq("/qrprograms/changeProgPageProgram", {}, successFn);
    };
    var setAcadStrTable = function() {
        qrAcadStr.deactivateAcadEntites("ASTProgramsTd", "PROGRAM", "getCentersOfProgram");
        qrAcadStr.preselectAcadEntity(qrProgram.programId, "CENTER");
    };
	
    var changeSectionSdOnly = function(){
	var $this = $(this);
	if($this.hasClass("activeSwitch")){ return;};
	var par = $this.closest("#switchSectionSdOnly");
	var sectionData = par.data("sectionInfo");
	var sdOnly = $this.data("value") === "ON" ? true : false;
	var confirmMsg = i18nJS("SDCARD.ENABLE_SECTION_MSG");
	if(!sdOnly){
		confirmMsg = i18nJS("SDCARD.DISABLE_SECTION_MSG");
	}
	showVYesNoBox(confirmMsg,null,function(state){
		if(state){
			doChange();
		}
	});
	var doChange = function(){
		var params = {
			programId : qrProgram.programId,
			sectionId : sectionData.id,
			revenueModel : sectionData.revenueModel,
			accessScope : sectionData.accessScope,
			sdOnly : sdOnly,
			name : sectionData.name,
			code : sectionData.code,
			desc : sectionData.desc
		};
		$this.addClass("activeSwitch").siblings().removeClass("activeSwitch");
		showTopLoader();
		vReq.post("/QrAcadStr/editSection",params,function(data){
			if(!data.result.edited){
				showError(i18nJS("FAILED_TO_UPDATE"));
				return;
			}
	    		var sdCardsOptions = $("#sectionLibSDCardOptions");
			if(params.sdOnly){
	    			sdCardsOptions.removeClass("hider");
			}else{
	    			sdCardsOptions.addClass("hider");
			}
		});
	}
    };
    var submitChangedProgram = function() {
        var tableParams = qrAcadStr.getAcadStrTableParams();
        var programId = tableParams.programId;
        var programName = tableParams.programName;
        var centerId = tableParams.centerId;
        var centerName = tableParams.centerName;
        var sectionId = tableParams.sectionId;
        var sectionName = tableParams.sectionName;
        if (!programId && !programName) {
            showError("Please choose a program");
            return;
        }

        closePopup();

        setProgPageAcadIds(tableParams);

        //set acad names on head
        setAcadEntityInProgHead(programPageHeadDiv.find(".progPageProgram"),
                programName, programId);
        setAcadEntityInProgHead(programPageHeadDiv.find(".progPageCenter"),
                centerName, centerId);
        setAcadEntityInProgHead(programPageHeadDiv.find(".progPageSection"),
                sectionName, sectionId);

        var profilePicUploadBtn = programPage.find(".uploadPeopleProfilePics");
        if (profilePicUploadBtn.length > 0) {
            if (!sectionId) {
                profilePicUploadBtn.addClass("nonner");
            } else {
                profilePicUploadBtn.removeClass("nonner");
            }
        }

        //mcWidget
        var activeClass = $(".leftSecHeadActive").attr("id");
        if(activeClass == "showClassroomConnect"){
            showClassroomConnect();
            return;
        }
        var manageContentVar = manageContent;
        var mcWidgetParams = programPage.data("params");
        manageContentVar.removemcWidgetParams(mcWidgetParams,
                ["centerId", "sectionId", "programId"]);
        $.extend(mcWidgetParams, {programId: programId, centerId: centerId, sectionId: sectionId});
        var checkedOpts = programPage.find(".cmdsTableCheckedOpts").html("");
        checkedOpts.removeData("optsType");
	var vChooseAlpha = $(".vChooseAlpha");
	vChooseAlpha.find(".vChooseOptCustomOrder").remove();
        if (sectionId) {
            checkedOpts.data("sectionId", sectionId);
	    vChooseAlpha.find(".vChooseHead").text(i18nJS('TXT_CUSTOM'));
	    vChooseAlpha.data("value","customOrder")
	    mcWidgetParams.orderBy = "customOrder";
	    vChooseAlpha.find(".vChooseDropDown")
		.prepend("<div class='vChooseOpt vChooseOptCustomOrder' data-value='customOrder'>"+i18nJS('TXT_CUSTOM')+"</div>");
        } else {
            checkedOpts.removeAttr("data-section-id");
            checkedOpts.removeData("sectionId");
	    mcWidgetParams.orderBy = "timeCreated";
	    vChooseAlpha.data("value","timeCreated")
	    vChooseAlpha.find(".vChooseHead").text("Date Added");
        }
	decideSectionPage(sectionId);
        if (centerId) {
            checkedOpts.data("centerId", centerId);
        } else {
            checkedOpts.removeAttr("data-center-id");
            checkedOpts.removeData("centerId");
        }
        //Enable classroom connect option.
        if(programId != null && centerId != null && sectionId != null){
            $(".leftSecHeadsDiv").find(".showClassroomConnect").removeClass("nonner");
        }
        else{
            $(".leftSecHeadsDiv").find(".showClassroomConnect").addClass("nonner");
        }
        manageContentVar.loadmcContent(programPage);
        if (currentAreaType.toUpperCase() === "STUDENTS" && qrProgram.sectionId) {
            $("#moveOrAddStudents").removeClass("nonner");
        } else {
            $("#moveOrAddStudents").addClass("nonner");
        }
    };
    var putProgramInfoOnHead = function(params) {
        var programId = params.programId;
        var centerId = params.centerId;
        var sectionId = params.sectionId;
        if (programId) {
            var progs = memberInfoPojo.progList;
            var found = false;
            for (var p = 0; p < progs.length; p++) {
                var prog = progs[p];
                if (prog.id === programId) {
                    found = true;
                    setAcadEntityInProgHead(programPageHeadDiv.find(".progPageProgram"),
                            prog.name, prog.id);
                    programPageHeadDiv.find(".changeProgPageProgram").removeClass("nonner");
                    var centers = prog.centers;
                    for (var c = 0; c < centers.length; c++) {
                        var center = centers[c], sections = center.sections;
                        if (centerId === center.id) {
                            setAcadEntityInProgHead(programPageHeadDiv.find(".progPageCenter"),
                                    center.name, center.id);
                            for (var s = 0; s < sections.length; s++) {
                                var section = sections[s];
                                if (sectionId === section.id) {
                                    setAcadEntityInProgHead(programPageHeadDiv.find(".progPageSection"),
                                            section.name, section.id);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            if (!found) {
                closecmdsPopup();
                alert("You do not have sufficient rights to view this page.");
                opencmdsResources();
                pushHistory(null, null, vcmdsUrls.RESOURCES(), true);
            }
        }
    };
    var setProgPageAcadIds = function(params) {
        qrProgram.programId = params.programId;
        qrProgram.centerId = params.centerId;
        qrProgram.sectionId = params.sectionId;
    };
    var decideSectionPage = function(sectionId){
	var sectionOnlyElems = $(".sectionOnlyElem");
	if(sectionId){
	    sectionOnlyElems.removeClass("nonner");
	    var sdCardsOptions = $("#sectionLibSDCardOptions");
	    sdCardsOptions.addClass("hider");
	    $.get("/QrExports/getSectionInfo",{sectionId:sectionId},function(data){
		if(data && data.errorCode == ""){
			var secInfo = data.result.sectionInfo;
			var switchBtn = $("#switchSectionSdOnly").data("sectionInfo",secInfo);
			var activeSwitchBtn = ".switchTabOFF";
			if(secInfo.sdOnly){
	    			sdCardsOptions.removeClass("hider");
				activeSwitchBtn = ".switchTabON";
			}
			switchBtn.find(activeSwitchBtn).addClass("activeSwitch").siblings().removeClass("activeSwitch");
		}
	    });
	}else{
	    sectionOnlyElems.addClass("nonner");
	}	
    }
    var setAcadEntityInProgHead = function(acadEntityEl, name, id) {
        if (name && id) {
            acadEntityEl.text(name);
        } else {
            acadEntityEl.text(acadEntityEl.data("defaultText"));
        }
    };
    var problemInProgramPage = function() {
        alert("Encountered a conflict in the permissions to access this page.");
        window.location = "/";
    };

    var prepareProgramPagePopupForMovingStudents = function(vChoose, operation) {
        startLoader();
        var successFn = function(data) {
            var popup = getcmdsPopupBody(700);
            popup.html(data);
            $("#programPageSelectProgramSubmit").removeClass("submitChangedProgram")
                    .addClass("submitSectionForMovingStudents").data({operation: operation});
            fetchScripts([{fname: "qrAcadStr.js",
                    cb: setAcadStrTable}]);
            stopLoader();
        };
        getReq("/qrprograms/changeProgPageProgram", {}, successFn);
    };
    this.prepareProgramPagePopupForMovingStudents = prepareProgramPagePopupForMovingStudents;
    var submitSectionForMovingStudents = function() {
        var tableParams = qrAcadStr.getAcadStrTableParams();
        var toSectionId = tableParams.sectionId;
        if (!toSectionId) {
            showError("Please choose a section");
            return;
        }
        var operation = $(this).data("operation");
        var sectionId = qrProgram.sectionId;
        if (!sectionId) {
            showError(COMMON_ERROR_MESSAGE);
            return;
        }
        var params = {fromSectionId: sectionId,
            toSectionId: toSectionId, operationType: operation};
        postReq("/qrpeople/bulkUpdateStudentsInSection", params, function() {
            showMessage("Successfully done");
            closePopup();
            refreshPage();
        });
    };


    var showProgContent = function(e, accessType) {
        var successFn = function() {
            programPage.find('.mcSearchDivInput').attr('placeholder', 'Search Content');
        };
        currentAreaType = "content";
        progPageMenuToggler($("#showProgContent"), "/qrprograms/programContent",
                {}, successFn, true, accessType, qrProgram.contentUrlParams);
    };
    var showProgMembers = function(e, accessType) {
        var urlParams = fetchUrlParams();
        var targetProfile = urlParams.targetProfile || "TEACHER";
        var successFn = function() {
            changeAddPeopleEls(programPage.find(".peopleProfilevChoose"),
                    targetProfile);
            $("#moveOrAddStudents").addClass("nonner");
        };
        currentAreaType = "members";
        progPageMenuToggler($("#showProgMembers"), "/qrprograms/programMembers", {targetProfile: targetProfile,
            pageTarget: "PROGRAM_PAGE"}, successFn, false, accessType, qrProgram.membersUrlParams);
    };
    
    
    //Changes to show shared organizations    
    var showProgOrganizations = function(e, accessType) {
        var urlParams = fetchUrlParams();
        var targetProfile = urlParams.targetProfile || "ORGANIZATION";
        
        var successFn = function() {
        	changeAddPeopleEls(programPage.find(".peopleProfilevChoose"),targetProfile);
            if (qrProgram.sectionId) {
                $("#moveOrAddStudents").removeClass("nonner");
            } else {
                $("#moveOrAddStudents").addClass("nonner");
            }
        };
        currentAreaType = "organizations";
        progPageMenuToggler($("#showProgOrganizations"), "/qrprograms/programOrganizations", {targetProfile: targetProfile,
            pageTarget: "PROGRAM_PAGE"}, successFn, false, accessType, qrProgram.organizationsUrlParams);
    };
        
    
    var showProgStudents = function(e, accessType) {
        var successFn = function() {
            programPage.find('.mcSearchDivInput').attr('placeholder', 'Search Students');
            $("#addPeopleBtn").text("+ Add Students").data("targetProfile", "STUDENT");
            if (qrProgram.sectionId) {
                $("#moveOrAddStudents").removeClass("nonner");
            } else {
                $("#moveOrAddStudents").addClass("nonner");
            }
        };
        currentAreaType = "students";
        progPageMenuToggler($("#showProgStudents"), "/qrprograms/programStudents", {targetProfile: "STUDENT",
            pageTarget: "PROGRAM_PAGE"}, successFn, false, accessType, qrProgram.studentsUrlParams);
    };

    var showClassroomConnect = function(e,accessType){
        var successFn = function(){
        }
        currentAreaType = "schedule";
        $(".leftSecHeadsDiv").find(".showClassroomConnect").removeClass("nonner");
        progPageMenuToggler($("#showClassroomConnect"),"/qrSchedule/myClassroomConnect",{}
            ,successFn,false,accessType,qrProgram.classroomConnectUrlParams);
    }

    //upload tests
    var uploadOfflineTests = function(e, accessType) {
        var cbfn = function(params) {
            mainContentSection.html(uploadOfflineTestsSample.children().clone(true));
            fetchScripts([{fname: "uicomWidgets/fileuploader.js",
                    cb: createOfflineTestUploader}]);
            $.extend(params, {resultType: "ALL", includeModes: ["OFFLINE"], orderBy: "timeCreated"});
            getReq("/qrPrograms/offlineTests", params, function(data) {
                mainContentSection.find(".offlineTestsList").html(data);
                var urlParams = fetchUrlParams();
                $.extend(params, urlParams);
                initmcWidgetforCMDS(programPage,
                        "/qrprograms/offlineTestsTable", params, false, true);
                programPage.data("pageUrlParams", qrProgram.uploadMarkSheetsUrlParams);
                programPage.data("changeUrlAfterLoad", qrProgram.afterContentLoaded);
                updatemcWidgetParamHolders(programPage, urlParams);
                programPage.find('.mcSearchDivInput').attr('placeholder', 'Search Mark Sheets');
            });
        };
        currentAreaType = "uploadmarksheets";
        progPageMenuToggler($("#uploadOfflineTests"), null, {}, cbfn,
                false, accessType, qrProgram.uploadMarkSheetsUrlParams);
    };
    var createOfflineTestUploader = function() {
        var uploadDiv = programPage.find(".uploadDiv");
        var u = new qq.FileUploader({
            element: uploadDiv.get(0),
            action: '/qrprograms/uploadMarkSheets',
            params: {uploadFileParamName: "resultFile",
                programId: qrProgram.programId},
            debug: true,
            sizeLimit: 5 * 1024 * 1024,
            allowedExtensions: ["xls", "xlsx"]
        });
        u.onUploadDone = onUploadDone;
        u.onUploadProgress = onUploadProgress;
        var uploadButton = uploadDiv.find(".qq-upload-button");
        uploadButton.addClass("blueButton");
        uploadDiv.find(".qq-button-title").html("Choose File");
        return u;
    };
    var onUploadProgress = function(percentDecimal, progressText) {
        programPage.find(".uploadTestsProgressDiv")
                .html("<span class='color8 smally'>Percentage Uploaded:</span>" + Math.round(percentDecimal * 100) + " %");
    };

    var uploadMarksSheetJobIds = [], uploadSheetProcessPercentDiv, uploadStatusTimer;
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
            programPage.find(".uploadTestsProgressDiv")
                    .html("<span class='color8 smally'>\n\
            Processing:</span> <span class='percentDiv'>0</span>%");
            uploadSheetProcessPercentDiv = programPage.find(".percentDiv");
            uploadStatusTimer = setInterval(function() {
                uploadStatusUpdater();
            }, 5000);
        }
    };
    var afterUploadTestsAlways = function() {
        programPage.find(".uploadTestsProgressDiv").html("");
        createOfflineTestUploader();
    };
    var uploadStatusUpdater = function() {
        $.get("/qrprograms/uploadMarkSheetsStatus",
                {jobIds: uploadMarksSheetJobIds}, function(data) {
            var result = data.result.list;
            for (var k = 0; k < result.length; k++) {
                var entityResult = result[k];
                var percent = Math.round(entityResult.numCompletedSteps * 100 /
                        entityResult.numOfSteps);
                uploadSheetProcessPercentDiv.html(percent);
                var removeJobId = false;
                if (entityResult.errorCode !== "") {
                    removeJobId = true;
                    showError("Some Error Occured: " +
                            entityResult.errorCode);
                } else if (entityResult.numCompletedSteps === entityResult.numOfSteps) {
                    removeJobId = true;
                    var uploadMessage = "";
                    if (entityResult.message) {
                        uploadMessage = entityResult.message;
                    }
                    showMessage("Successfully Done<br>\n\
                    <div class='smallThinGray'>" + uploadMessage + "</div>");
                }
                if (removeJobId) {
                    try {
                        clearInterval(uploadStatusTimer);
                    } catch (err) {
                        console.log("err in clearing interval")
                    }
                    ;
                    afterUploadTestsAlways();
                }
            }
        });
    };

    var removeProgramSharing = function () {
        var $this = $(this);
        var programSharingDetails = $this.data();
        var confirmtxt = "<div>Are you sure you want to remove sharing ?</div>";
        showVYesNoBox(confirmtxt, null, function(state) {
            if (state) {
                doDelete();
            }
        });
        function doDelete() {
            var params = {
                subscriberOrgId : programSharingDetails.subscriberOrgId,
                providerOrgId : programSharingDetails.providerOrgId,
                programId : programSharingDetails.programId
            };
            vReq.post("/QrPrograms/removeProgramSharing", params, function() {
                location.reload(true);
                showMessage("Completed!!");
            });
        }
    };

    var progPageMenuToggler = function($this, url, extraParams,
            cbfn, showSubs, accessType, pageParams) {
        changeActiveClass($this, "leftSecHeadActive");
        var mcWidget = programPage;
        var params = {start: 0, size: 50};
        if (mcWidget.length > 0) {
            var mcWidgetParams = mcWidget.data("params") || {};
            var centerId = qrProgram.centerId;
            var sectionId = qrProgram.sectionId;
            if (centerId)
                params.centerId = centerId;
            if (sectionId)
                params.sectionId = sectionId;
            if (mcWidgetParams.query) {
                params.query = mcWidgetParams.query;
            }

            var currentUrlParams = fetchUrlParams();
            if (accessType === "DIRECT") {
                $.each(pageParams, function(key, val) {
                    if (currentUrlParams[key]) {
                        params[key] = currentUrlParams[key];
                    }
                });
            }
            delete mcWidgetParams.brdIds;
            delete mcWidgetParams.courseId;
        }
        var programId = qrProgram.programId;
        var finalUrl = getProgramPagePathName(programId) + "?" + $.param(params);
        params.programId = programId;

        if (url) {
            if (extraParams) {
                $.extend(params, extraParams);
            }
            if (accessType !== "DIRECT") {
                pushHistory(null, null, finalUrl);
            }
            startLoader();
            var successFn = function(data) {
                stopLoader();
                mainContentSection.html(data);
                if (cbfn) {
                    cbfn();
                }
            };
            getReq(url, params, successFn);
        } else if (cbfn) {
            if (accessType !== "DIRECT") {
                pushHistory(null, null, finalUrl);
            }
            cbfn(params);
        }
        //Show classroom connect option if program,center and section is selected.
        if(params.programId != null && params.centerId != null && params.sectionId != null){
            $(".leftSecHeadsDiv").find(".showClassroomConnect").removeClass("nonner");
        }
        if (showSubs) {
            $("#fixedLeftSecPortion").removeClass("hider");
        } else {
            $("#fixedLeftSecPortion").addClass("hider");
        }
    };
    var getProgramPagePathName = function(programId) {
        return "/organization/" + cmdsOrgId + "/program/" + programId + "/" + currentAreaType;
    };
})(jQuery);
var addContentFromProgram = function(vChoose, value) {
    startLoader();
    var cbfn = function() {
        stopLoader();
        vChooseVar.reset(vChoose, "-1", "+ Add Content");
        qrResources.addResource[value](value);
    };
    fetchScripts([{fname: "qrResources.js", cb: cbfn}]);
};
function onsdCardChooseChange(target,value){
	switch(value){
		case "CREATE" : 
			var params = {
				sectionId : qrProgram.sectionId
			};
			showTopLoader();
			vReq.get("/QrExports/createPopup",params,function(data){
				hideTopLoader();
				var popup = showVPopup(0.7,true,true);
				popup.html(data);
				popup.closest(".vpopupBodyHolder").find(".stl").show();
				popup.on("close.popup",function(){
					try{createSDCardGroupPopup.stopTimer();}catch(err){}
				});
			});
			break;
		case "VIEW" : opencmdsSDCardGroupsPage(qrProgram.sectionId);
			break;
	};
	target.find(".vChooseHead").html(target.find(".vChooseHeadBackup").html());
	target.data("value",-1);
}
