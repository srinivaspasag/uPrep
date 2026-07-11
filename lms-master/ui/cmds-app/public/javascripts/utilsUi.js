var opencmdsPage = function(url, params, targetTabEl, callBack) {
    cmdsBlackOut(false);
    showTopLoader();
    resetCmdsPages();
    params = params || {};
    $.extend(params, fetchUrlParams());
    params["newPageOpen"] = getNewPageUrl();
    var successFn = function(data) {
        hideTopLoader();
        cSecHolder.html(data);
        if (callBack)
            callBack();
        trackPageView();
    };
    vReq.get(url, params, successFn);
};
function getNewPageUrl(source) {
    var url = window.location.href;
    return url;
}
var resetCmdsPages = function() {
    closecmdsPopup();
    hideCmdsMsgBox();
    insideOutClick();
    $(window).scrollTop(0);
    try {
        stopAllHtml5Video();
    } catch (err) {
    }
};
$(document).on('mouseenter', ".hiddenMenuItemToggler", function() {
    floaterDiv = $(this).siblings(".hiddenMenuItems");
});


//programs
var getcmdsPrograms = function() {
    var progItem = $(this).siblings(".progMenuItems").children(".getProgramPage");
    if (progItem.length > 0) {
        goToProgramPage(progItem.data("programId"), "CONTENT");
        pushHistory(null, null, progItem.attr("href"));
    } else {
        if (cmdsProfile !== "MANAGER") {
            $(this).addClass("nonner");
            showError("Sorry, you are not part of any programs.<br>Ask Admin to assign programs to you.");
        } else {
            showError("Sorry, you are not part of any programs<br>\n\
            <a class='goToAcadStrPage cmdsaPush' href='/organization/" + cmdsOrgId + "/academicstructure'>Click Here </a>\n\
             to add a program.");
        }
    }
};
var getProgramPage = function() {
    goToProgramPage($(this).data("programId"), "content");
};
var goToProgramPage = function(programId, areaType) {
    opencmdsPage("/QrPrograms/program", {programId: programId, areaType: areaType}, $("#cmdsPrograms").parent());
};

//resources
var opencmdsResources = function() {
    opencmdsPage("/qrresources/resources", {}, $("#cmdsResources").parent());
};
var openCheckDuplicates = function() {
    opencmdsPage("/QrQuestions/checkDuplicates", {});
};
var opencmdsAddContent = function() {
    var cbfn = function() {
        var cSec = cSecHolder;
    };
    opencmdsPage("/qraddContent/addContent", {}, $("#cmdsAddContent"), cbfn);
};
var getTestPage = function() {
    goToTestPage($(this).data("entityId"));
};
var goToTestPage = function(testId) {
    opencmdsPage("/QrTests/testPage", {id: testId, testId: testId});
};
var goToReviewsPage = function(entityId) {
    opencmdsPage("/QrTests/getCMDSEntityReviews",{entityId:entityId});
};
var getAssignmentPage = function() {
    goToAssignmentPage($(this).data("entityId"));
};
var goToAssignmentPage = function(assignmentId) {
    opencmdsPage("/QrTests/assignmentPage", {id: assignmentId, assignmentId: assignmentId});
};
var getTSPage = function() {
    goToTestSeriesPage($(this).data("entityId"));
};
var goToTestSeriesPage = function(tsId) {
    opencmdsPage("/QrTests/testSeriesPage",
            {testId: tsId, testSeriesId: tsId, start: 0, size: 50, target: "TEST_SERIES"});
};
var getQuestionSetPage = function() {
    var $this = $(this);
    goToQuestionSetPage($this.data("entityId"), $this.data("entityName"));
};
var goToQuestionSetPage = function(questionSetId, questionSetName) {
    try {
        questionSetName = questionSetName ? questionSetName : getURLParameter("questionSetName");
    } catch (err) {
    }
    var params = {'questionSetId': questionSetId, start: 0, size: 25, needCBox: true,
        target: "QUESTION_SET", 'questionSetName': questionSetName};
    opencmdsPage("/QrQuestions/questionSetPage", params);
};
var getQuesPage = function() {
    goToQuesPage($(this).data("qid"));
};
var goToQuesPage = function(qid) {
    opencmdsPage("/QrQuestions/questionPage", {questionId: qid});
};
var editcmdsQues = function() {
    opencmdsPage("/qrquestions/editQuesPage", {id: $(this).data("qid")});
};

var addCmdsQues = function() {
    var params = {
        id:$(this).data("qid"),
        addPara:true
    }
    opencmdsPage("/qrquestions/editQuesPage", params);
};

var getChalPage = function() {
    goToChalPage($(this).data("entityId"));
}
var goToChalPage = function(entityId) {
    opencmdsPage("/QrQuestions/challengePage", {id: entityId});
}
var getFolderPage = function() {
    goToFolderPage($(this).data("entityId"));
};
var goToFolderPage = function(folderId) {
    opencmdsPage("/qrresources/folderPage", {folderId: folderId}, $("#cmdsResources"));
};
var getPkgPage = function() {
    goToPkgPage($(this).data("pkgId"));
}
var goToPkgPage = function(pkgId) {
    opencmdsPage("/qrproducts/packagePage", {packageId: pkgId});
}
var getCDPPage = function() {
    goToCDPPage($(this).data("entityId"));
}
var goToCDPPage = function(entityId) {
    opencmdsPage("/qrcdpplans/cdpPage", {cdpId: entityId});
}
var getDocPage = function() {
    goToDocPage($(this).data("entityId"));
}
var goToDocPage = function(docId) {
    opencmdsPage("/QrDocuments/docPage", {id: docId});
}
var getFilePage = function() {
    goToFilePage($(this).data("entityId"));
}
var goToFilePage = function(fileId) {
    opencmdsPage("/QrDocuments/filePage", {id: fileId});
}
var getVideoPage = function() {
    goToVideoPage($(this).data("entityId"));
}
var goToVideoPage = function(vidId) {
    opencmdsPage("/QrDocuments/videoPage", {id: vidId});
}
var getModulePage = function() {
    goToModulePage($(this).data("entityId"));
}
var goToModulePage = function(entityId) {
    opencmdsPage("/QrModules/home", {id: entityId}, $("#cmdsPrograms").parent());
};

var generateTestPage = function(){
    opencmdsPage("/QrTests/generateTest");
}


//institute settings
var opencmdsAcadStr = function() {
    opencmdsPage("/qracadstr/main", {start: 0, size: 200}, $("#cmdsAcadStr").parent());
};
var opencmdsInstituteInfo = function() {
    opencmdsPage("/qracadstr/instituteInfo", {}, $("#cmdsAcadStr").parent());
};
var editInstituteInfo = function() {
    var successFn = function() {
        //for profile pic
        uploadProfilePicUtil.init(cSecHolder.find(".memberPicDiv"),
                cSecHolder.find(".memberPicPreview"), '/qracadstr/uploadOrgPic');
    };
    opencmdsPage("/qracadstr/editInstituteInfo", {}, $("#cmdsAcadStr").parent(), successFn);
};
var opencmdsCourseMagnt = function() {
    opencmdsPage("/QrBoards/main", {start: 0, size: 200, type: "COURSE"}, $("#cmdsAcadStr").parent());
};
var opencmdsSellerDashboard = function(tabId) {
    tabId = tabId ? tabId : "";
    opencmdsPage("/QrInventory/home",{openPageId:tabId},$(this).parent());
};
var opencmdsCouponsDashboard = function(tabId) {
    tabId = tabId ? tabId : "";
    opencmdsPage("/QrCoupons/home",{openPageId:tabId},$(this).parent());
};

// sale details

var opencmdsSaleDetailsDashboard = function(tabId) {
    tabId = tabId ? tabId : "";
    opencmdsPage("/QrSaleDetails/home",{openPageId:tabId},$(this).parent());
};

// Billing Details
var opencmdsBillingDashboard = function(tabId) {
    tabId = tabId ? tabId : "";
    opencmdsPage("/QrBilling/home",{openPageId:tabId},$(this).parent());
};

//notification
var cmdsNotification = function() {
    opencmdsPage("/QrNotification/home", {},$("#cmdsNotification").parent())
}

var referralUsers = function(){
    opencmdsPage("/QrReferrals/referralUsers",{},$("#referralUsers").parent());
}

var shareQuestions = function(){
    opencmdsPage("/QrBoards/shareQuestions",{},$("#sharequestions").parent());
}
//Devices
var opencmdsDevices = function() {
    opencmdsPage("/QrDevices/home", {}, $("#cmdsDevices").parent());
};

//Exports
var opencmdsExports = function() {
    opencmdsPage("/QrExports/home", {}, $("#cmdsLibraryExport").parent());
};
var openSdCardGroup = function(){
    var $this = $(this);
    opencmdsSDCardGroup($this.data("id"));	
};
var opencmdsSDCardGroup = function(id) {
    opencmdsPage("/QrExports/sdCardGroup", {groupId:id}, $(this));
    pushHistory(null, null, "/organization/"+cmdsOrgId+"/sdcardgroup/"+id);
};
var opencmdsSDCardGroupsPage = function(id) {
    opencmdsPage("/QrExports/sdCardGroupsPage", {sectionId:id}, $(this));
    pushHistory(null, null, "/organization/"+cmdsOrgId+"/sdcardgroups/"+id);
};

//people
var opencmdsPeople = function(e, params) {
    params = params || {};
    params.start = 0;
    params.size = 50;
    opencmdsPage("/QrPeople/people", params);
};
var openMemberPage = function() {
    goToUserPage($(this).data("userId"), "MEMBER");
};
var openStudentPage = function() {
    goToUserPage($(this).data("userId"), "STUDENT");
};
var openOfflineUserPage = function() {
    goToUserPage($(this).data("userId"), "OFFLINE_USER");
};
var goToStudentPage = function(targetUserId) {
    goToUserPage(targetUserId, "STUDENT");
};
var goToOfflineUserPage = function(targetUserId) {
    goToUserPage(targetUserId, "OFFLINE_USER");
};
var goToMemberPage = function(targetUserId) {
    goToUserPage(targetUserId, "MEMBER");
};
var goToUserPage = function(targetUserId, profile) {
    var params = {targetUserId: targetUserId};
    var strip = "memberPage";
    if (profile === "STUDENT") {
        strip = "studentPage";
    }
    else if (profile === "OFFLINE_USER") {
        strip = "offlineUserPage";
    }
    opencmdsPage("/QrPeople/" + strip, params);
};
var addEditMemberPage = function(urlParams, action) {
    var targetProfile = urlParams.profile;
    url = "/qrpeople/addEditMember";
    var params = {};
    params.targetProfile = targetProfile;
    if (targetProfile && targetProfile.toUpperCase() === "STUDENT") {
        url = "/qrpeople/addEditStudent";
    }
    var targetUserId = urlParams.userid;
    if (action == "EDIT") {
        params.targetUserId = targetUserId;
    }

    var successFn = function() {
        uploadProfilePicUtil.init(cSecHolder.find(".memberPicDiv"),
                cSecHolder.find(".memberPicPreview"),
                '/qrpeople/uploadProfilePic');
    };
    opencmdsPage(url, params, null, successFn);
};

//institute challenge channels
var openChannels = function() {
    opencmdsPage("/QrChannels/channels", {}, $("#cmdsChannels").parent());
};
//institute plans
var openOrgPlans = function() {
    opencmdsPage("/QrPlans/orgPlans", {}, $("#cmdsOrgPlans").parent());
};
//institute invoices
var openOrgInvoices = function() {
    opencmdsPage("/QrInvoices/home", {}, $("#cmdsOrgInvoices").parent());
};
//institute ext sign up
var openExtSignupPage = function() {
    opencmdsPage("/QrExtSignup/home", {}, $("#cmdsSingUpMGMT").parent());
};
var openCustomizeSignupPage = function() {
    opencmdsPage("/QrExtSignup/customizeSignup", {}, $("#cmdsSingupMGMTPage"));
};


var changePublicSettings = function() {
    insideOutClick();
    startLoader();
    var popup = getcmdsPopupBody(null, null, null, "cmdsSettingsPopup");
    popup.siblings(".closePopupDiv").addClass("nonner");
    var params = {"closePopupClass": "closecmdsPopup closePopupImg"};
    vReq.get("/qrpeople/changePublicSettings", params, function(data) {
        stopLoader();
        popup.html(data);
        var fn = function() {
            userSettings.init(popup, cmdsOrgId);
        };
        fetchScripts([{fname: "uicomSettings.js", cb: fn}]);
    });
};



//initing some handlers
var startLoader = showTopLoader;
var stopLoader = hideTopLoader;
var closePopup = closecmdsPopup;
var closeStopCombo = function() {
    stopLoader();
    closePopup();
}
var closeStopRefreshCombo = function() {
    stopLoader();
    closePopup();
    refreshPage();
}
var initiateSomeHandlers = new (function() {
    var $cmdsBody = $("body"), clickEvent;
    var cmdsQuesnInfo = function() {
        startLoader();
        $.get("/qrQuestions/addedToInfo", {questionId: $(this).closest(".ques").data("qid"),
            start: 0, size: 50}, function(data) {
            var popup = getcmdsPopupBody(800);
            popup.html(data);
            stopLoader();
        });
    }


    clickEvent = "click.topBar";
    $cmdsBody
            //programs
            .on(clickEvent, ".getProgramPage", getProgramPage)
            .on(clickEvent, "#cmdsPrograms", getcmdsPrograms)

            //resources
            .on(clickEvent, "#cmdsResources,.goToResources,.goToHomePage", opencmdsResources)
            .on(clickEvent, "#cmdsAddContent", opencmdsAddContent)
            .on(clickEvent, ".getFolderPage", getFolderPage)
            .on(clickEvent, ".getPkgPage", getPkgPage)
            .on(clickEvent, ".getCDPPage", getCDPPage)
            .on(clickEvent, ".getQuestionSetPage", getQuestionSetPage)
            .on(clickEvent, ".getquesPage", getQuesPage)
            .on(clickEvent, ".editcmdsQues", editcmdsQues)
            .on(clickEvent,".addCmdsQues",addCmdsQues)
            .on(clickEvent, ".cmdsQuesnInfo", cmdsQuesnInfo)
            .on(clickEvent, ".getTestPage", getTestPage)
            .on(clickEvent, ".getAssignmentPage", getAssignmentPage)
            .on(clickEvent, ".getTSPage", getTSPage)
            .on(clickEvent, ".openStudentPage", openStudentPage)
            .on(clickEvent, ".openOfflineUserPage", openOfflineUserPage)
            .on(clickEvent, ".openMemberPage", openMemberPage)
            .on(clickEvent, ".getVideoPage", getVideoPage)
            .on(clickEvent, ".getFilePage", getFilePage)
            .on(clickEvent, ".getDocPage", getDocPage)
            .on(clickEvent, ".getChalPage", getChalPage)
            .on(clickEvent, ".getModulePage", getModulePage)


            //institute settings
            .on(clickEvent, "#cmdsAcadStr,.goToAcadStrPage", opencmdsAcadStr)
            .on(clickEvent, "#cmdsInstituteInfo", opencmdsInstituteInfo)
            .on(clickEvent, ".editInstituteInfo", editInstituteInfo)
            .on(clickEvent, "#courseMagnt", opencmdsCourseMagnt)

            //people
            .on(clickEvent, ".cmdsPeople", opencmdsPeople)
            //tablets
            .on(clickEvent, "#cmdsDevices", opencmdsDevices)

            //EXPORT
            .on(clickEvent, "#cmdsLibraryExport", opencmdsExports)
            .on(clickEvent, ".openSdCardGroup", openSdCardGroup)

            //CHANNELS
            .on(clickEvent, "#cmdsChannels,.goToChannelsPage", openChannels)

            //ORG PLANS 
            .on(clickEvent, "#cmdsOrgPlans", openOrgPlans)

            //ORG INVOICES 
            .on(clickEvent, "#cmdsOrgInvoices", openOrgInvoices)

            //ORG EXT SIGN UP
            .on(clickEvent, "#cmdsSingUpMGMT", openExtSignupPage)

            //for user drop down box
            .on(clickEvent, ".showTopBarDropDown,.showSettingsDropDown", function() {
                addToggler($(this).next(".topBarBoxDropDown"), $(this));
            })

            .on(clickEvent, "#cmdsSellerDashboard", function(e){opencmdsSellerDashboard()})

            .on(clickEvent, "#cmdsCouponsDashboard", function(e){opencmdsCouponsDashboard()})
            .on(clickEvent, "#cmdsSaleDetailsDashboard", function(e){opencmdsSaleDetailsDashboard()})
            .on(clickEvent, "#cmdsBillingDashboard", function(e){opencmdsBillingDashboard()})
            //for notification gcm
            .on(clickEvent, "#cmdsNotification", cmdsNotification)
            .on(clickEvent,"#referralUsers",referralUsers)
            .on(clickEvent,"#sharequestions",shareQuestions)

            //public profile
            .on(clickEvent, ".changePublicSettings", changePublicSettings)
            

    //commons ui/global needs
    var fetchQuesSolAttachments = function(){
	var $this = $(this);
	var qId = $this.data("id");
	var params = {
		qId : qId
	};
	showTopLoader();
	vReq.get("/QrQuestions/getSolnAttachments",params,function(data){
		hideTopLoader();
		$this.closest(".quesSolnAttachmentsHolder").html(data);
	});
    };
    var showQuesTail = function() {
        var $this = $(this);
        var className = "qrQuesActive";
        var ques = $this.closest(".ques");
        if (!ques.hasClass(className)) {
            ques.addClass(className);
            $this.html("Hide Details");
        } else {
            ques.removeClass(className);
            $this.html("Show Details");
        }
    };
    var qrQuesGlobalEvent = "click.qrQuestionsEvent";
    $cmdsBody.off(qrQuesGlobalEvent)
            .on(qrQuesGlobalEvent, ".showQuesTail", showQuesTail)
            .on(qrQuesGlobalEvent, ".fetchQuesSolAttachments", fetchQuesSolAttachments)


    //for test series page
    var publishTestSeries = function() {
        startLoader();
        var $this = $(this);
        var successFn = function(data) {
            stopLoader();
            closePopup();
            $this.parent().html('<div class="boldy linBlk">Published</div>');
        }
        vReq.post("/qrtests/publishTest", {testId: $(this).data("tsId"), duration: 0,
            scope: "PUBLIC"}, successFn);
    }
    var qrTSEvent = "click.qrTSEvent";
    $cmdsBody.off(qrTSEvent)
            .on(qrTSEvent, ".publishTestSeries", publishTestSeries)

})();

var getOptsType = function(optsDiv) {
    var optsType = optsDiv.data("optsType").toLowerCase();
    if (optsType == "library") {
        var sectionId = optsDiv.data("sectionId");
        var centerId = optsDiv.data("centerId");
        if (sectionId) {
            optsType = "section" + optsType;
        } else if (centerId) {
            optsType = "center" + optsType;
        }
    }
    return optsType;
}

var cmdsTablegCBoxCheck = function(cBox, isChecked) {
    var mcWidget = cBox.closest(".mcWidget");
    var tableSec = cBox.closest("#tableSection");
    if (mcWidget.length === 0) {
        //very wrong way of doing, will correct it later
        mcWidget = cBox.closest("#cmdsPopup,#cmdsPopupAlt");
    }
    if (tableSec.length === 0) {
        tableSec = mcWidget;
    }
    var optsDiv = mcWidget.find(".cmdsTableCheckedOpts");
    if (optsDiv.length > 0) {
        var optsType = getOptsType(optsDiv);
        var optsStr = optsType + "OptsSample";
        var cbfnStr = optsType + "OptsCbfn";
    }
    cmdsCBoxesChecked = tableSec.find(".gCBoxChecked").not(".cmdsAllgCBox");
    if (optsDiv.length > 0) {
        if (cmdsCBoxesChecked.length > 0) {
            optsDiv.html(window[optsStr].children().clone(true));
            if (window[cbfnStr]) {
                window[cbfnStr](optsDiv);
            }
        } else {
            optsDiv.html("").data("optsType", undefined);
        }
    }
    if (!isChecked) {
        tableSec.find(".cmdsAllgCBox").removeClass("gCBoxChecked");
    }
};
var cmdsTableAllCheck = function(cBox, isChecked) {
    var mcWidget = cBox.closest(".mcWidget");
    var tableSec = cBox.closest("#tableSection");
    if (mcWidget.length === 0) {
        //very wrong way of doing, will correct it later
        mcWidget = cBox.closest("#cmdsPopup,#cmdsPopupAlt");
    }
    if (tableSec.length === 0) {
        tableSec = mcWidget;
    }
    var optsDiv = mcWidget.find(".cmdsTableCheckedOpts");
    if (optsDiv.length > 0) {
        var optsType = getOptsType(optsDiv);
        var optsStr = optsType + "OptsSample";
        var cbfnStr = optsType + "OptsCbfn";
    }

    if (isChecked) {
        tableSec.find(".gCBox").addClass("gCBoxChecked");
        var allgCBox = tableSec.find(".cmdsAllgCBox");
        cmdsCBoxesChecked = tableSec.find(".gCBoxChecked").not(".cmdsAllgCBox");
        if (cmdsCBoxesChecked.length === 0) {
            allgCBox.remove();
            return;
        }
        if (optsDiv.length > 0) {
            optsDiv.html(window[optsStr].children().clone(true));
            if (window[cbfnStr]) {
                window[cbfnStr](optsDiv);
            }
        }
    } else {
        tableSec.find(".gCBox").removeClass("gCBoxChecked");
        cmdsCBoxesChecked = [];
        if (optsDiv.length > 0) {
            optsDiv.html("").data("optsType", undefined);
        }
    }
};
var getEntityTypesChecked = function() {
    var entityTypesSelected = [];
    for (var k = 0; k < cmdsCBoxesChecked.length; k++) {
        var entityType = cmdsCBoxesChecked.eq(k).closest("tr").data("entityType");
        pushIfAbsent(entityTypesSelected, entityType);
    }
    return entityTypesSelected;
};
var resetcmdsCBoxes = function(mcWidget) {
    try {
        if (!mcWidget) {
            var cmdsPopup = $("#cmdsPopup");
            var mcWidget = cmdsCBoxesChecked.eq(0).closest(".mcWidget");
            if (mcWidget.length === 0) {
                mcWidget = cmdsPopup;
            }
            if (mcWidget.is(cmdsPopup) && cmdsPopup.css("display") === "none") {
                mcWidget = $(".mcWidget");
            }
        }
        cmdsCBoxesChecked.removeClass("gCBoxChecked");
        mcWidget.find(".cmdsAllgCBox").removeClass("gCBoxChecked");
        mcWidget.find(".cmdsTableCheckedOpts").html("").data("optsType", undefined);
    } catch (err) {
    }
};

//code error maker
var getCodeFieldError = function(name) {
    name = name.trim().toUpperCase();
    name = name.replace(" ", "-");
    var code = "Please enter a code eg." + name;
    return code;
};
//caches
var loadAndCacheExams = function(cbfn) {
    try {
        cbfn(orgExams);
    } catch (err) {
        $.get("/widgets/getExams", function(data) {
            orgExams = data.result;
            cbfn(orgExams);
        });
    }
}
var loadAndCacheCourses = function(cbfn) {
    try {
        cbfn(orgCourses);
    } catch (err) {
        $.get("/uicomboards/getOrgBoards", {type: "COURSE"}, function(data) {
            orgCourses = data.result.list;
            cbfn(orgCourses);
        });
    }
}
var loadAndCacheSources = function(cbfn) {
    try {
        cbfn(orgSources);
    } catch (err) {
        $.get("/qrsources/getSources", {start: 0, size: 10, version: "v1"}, function(data) {
            orgSources = data.result.sources;
            cbfn(orgSources);
        });
    }
}
var loadAndCacheTests = function(cbfn) {
    try {
        cbfn(orgTests);
    } catch (err) {
        $.get("/qrproducts/getProducts", {start: 0, size: 10, productType: "TEST"}, function(data) {
            orgTests = data.result.products;
            cbfn(orgTests);
        });
    }
}
var orgTopics = {};
var loadAndCacheTopics = function(courseId, cbfn, params) {
    try {
        cbfn(orgTopics[courseId]);
    } catch (err) {
        $.get("/uicomboards/getOrgBoards", {parentId: courseId,
            type: "TOPIC"}, function(data) {
            orgTopics[courseId] = data.result.list;
            cbfn(orgTopics[courseId]);
        });
    }
};




var loadvChooseSubs = function(target, dataParams, extraClasses) {
    inlineLoader(target);
    var cbfn = function(subs) {
        var newvChoose = vChooseSample.children().clone(true);
        newvChoose.data(dataParams);
        newvChoose.addClass(extraClasses);
        newvChoose.find(".vChooseHead").width(150);
        var targetOpts = newvChoose.find(".vChooseDropDown");
        for (var k = 0; k < subs.length; k++) {
            var s = subs[k], item = makeHTMLTag("div", {"class": "vChooseOpt"})
                    .data({value: s.courseId}).text(s.name);
            targetOpts.append(item);
        }
        target.html(newvChoose);
    };
    loadAndCacheCourses(cbfn);
};



var loadRadioCenters = function(target) {
    inlineLoader(target);
    $.get("/widgets/getCenters", {start: 0, size: 50}, function(data) {
        var centers = data.result.centers;
        loadRadioUtil(target, {paramName: "newCenter"}, "formvRadioGrp", centers);
    });
};
var loadRadioExams = function(target) {
    inlineLoader(target);
    var cbfn = function(exams) {
        loadRadioUtil(target, {paramName: "targetId"}, "formvRadioGrp", exams);
    }
    loadAndCacheExams(cbfn);
}
var loadRadioUtil = function(targetDiv, dataParams, extraClasses, radioList) {
    var newRadioGrp = makeHTMLTag("div", {"class": "vRadioGrp"});
    if (dataParams)
        newRadioGrp.data(dataParams);
    if (extraClasses)
        newRadioGrp.addClass(extraClasses);
    for (var k = 0; k < radioList.length; k++) {
        var item = makeHTMLTag("div", {"class": "vRadio"}), radio = radioList[k];
        var itemId = radio.courseId || radio.name || radio;
        var itemName = radio.name || radio;
        item.data({value: itemId}).text(itemName);
        newRadioGrp.append(makeHTMLTag("div", {"class": "vRadioItem"}).html(item));
    }
    targetDiv.html(newRadioGrp);
}




var loadmvChooseExams = function(targetDiv, dataParams, extraClasses) {
    var cbfn = function(exams) {
        var newmvc = makeHTMLTag("div", {"class": "multiplevChoose mvcExams"});
        if (extraClasses)
            newmvc.addClass(extraClasses);
        if (dataParams)
            newmvc.data(dataParams);
        for (var k = 0; k < exams.length; k++) {
            var item = mvcItemSample.children().clone(true), exam = exams[k];
            item.find(".gCBox").data({value: exam});
            item.find(".gCBoxText").text(exam);
            newmvc.append(item);
        }
        targetDiv.html(newmvc);
    };
    loadAndCacheExams(cbfn);
};
var insertmvcTopicsFromRadio = function($this) {
    //just imitating mvcItem and using its callback  insertmvcTopics
    insertmvcTopics($this, true);
}
var insertmvcTopics = function(cBox, isChecked) {
    var mvcItem = cBox.parent(), mvcItemBody = mvcItem.children(".mvcItemBody");
    //Develop sequence drag drop
    $(".sequenceSubj").removeClass("nonner");
    $(".sequenceSubj .selectedSubjectsList").html("");
    $(".mvcCTFSubs > .mvcItem > .gCBoxChecked").each(function(){
        $(".sequenceSubj .selectedSubjectsList").append("<li class='subjOrder' data-board-id='"+$(this).data("boardId")+"' style='cursor:move' draggable='true'>"+$(this).closest(".mvcItem").children(".gCBoxText").text()+"</li>");
    });
    if (isChecked) {
        if (mvcItemBody.html() == "") {
            var cbfn = function(topics) {
                var newmvc = makeHTMLTag("div", {"class": "multiplevChoose mvcTopics"});
                for (var k = 0; k < topics.length; k++) {
                    var item = mvcItemSample.children().clone(true), t = topics[k];
                    item.find(".gCBox").data("value", t.id);
                    item.find(".gCBoxText").text(t.name);
                    newmvc.append(item);
                }
                mvcItemBody.html(newmvc);
            };
            loadAndCacheTopics(cBox.data("value"), cbfn);
        }
        mvcItemBody.removeClass("nonner").find(".gCBox").removeClass("gCBoxChecked");
    } else {
        mvcItemBody.addClass("nonner");
    }
};

//util for checkboxes checking
var checkedEntities = new (function() {
    var statusPublished = "PUBLISHED";
    var visible = "VISIBLE";
    var invisible = "INVISIBLE";
    this.init = function(analysisType) {
        var needVisibiltyReport = false;
        if (analysisType === "CHECK_VISIBILITY") {
            needVisibiltyReport = true;
        }
        var entityIds = [], hasPublished = false, entityTrList = [],
                entityGlobalIds = [], contents = [], entityCloneList = [], visibleContents = [], invisibleContents = [],
                unpublishedContents = [];
        for (var k = 0; k < cmdsCBoxesChecked.length; k++) {
            var cBox = cmdsCBoxesChecked.eq(k);
            var entityTr = cBox.closest("tr");
            if (entityTr.length === 0) {
                entityTr = cBox.closest(".ques");
            }
            var status = entityTr.data("status");

            if (status === statusPublished) {
                hasPublished = true;
            }
            var id = entityTr.data("entityId");
            entityIds.push(id);
            entityTrList.push(entityTr);
            entityGlobalIds.push(entityTr.data("globalEntityId"));
            entityCloneList.push(entityTr.clone(true));
            var entityType = entityTr.data("entityType");
            var srcEntity = {type: entityType, id: id,
                title: entityTr.data("entityName")};
            contents.push(srcEntity);
            if (needVisibiltyReport) {
                var visibilityStatus = entityTr.data("visibilityStatus");
                if (visibilityStatus === visible) {
                    visibleContents.push(srcEntity);
                } else if (visibilityStatus === invisible && status === statusPublished) {
                    invisibleContents.push(srcEntity);
                } else {
                    unpublishedContents.push(srcEntity);
                }
            }
        }

        this.entityIds = entityIds;
        this.entityGlobalIds = entityGlobalIds;
        this.entityList = entityTrList;
        this.entityCloneList = entityCloneList;
        this.srcEntities = contents;
        this.hasPublished = hasPublished;
        this.visibleContents = visibleContents;
        this.invisibleContents = invisibleContents;
        this.unpublishedContents = unpublishedContents;
        qrPackageCDPUtil.contentsChecked = contents;
        return this;
    };
    var prepareEntityClonesPopup = function(title, entityCloneList, srcEntities) {
        if (!entityCloneList) {
            entityCloneList = checkedEntities.entityCloneList;
        }
        if (!srcEntities) {
            srcEntities = checkedEntities.srcEntities;
        }
        var statusHTML = makeHTMLTag("div");
        for (var k = 0; k < entityCloneList.length; k++) {
            var entity = entityCloneList[k];
            var srcEntity = srcEntities[k];
            var tr = makeHTMLTag("tr").addClass("nonner popupEntityTr_" + srcEntity.id);

            var entityTitleEl = entity.find(".entityTitleTd");
            if (entityTitleEl.length === 0) {
                entityTitleEl = entity.find(".quesTextDivHolder");
            }

            tr.html("<td>" + entityTitleEl.html() + "</td>");
            tr.addClass("popupEntity_" + srcEntity.id + " popupEntity").data("srcEntity", srcEntity);
            tr.append("<td class='statusTd width170 maxWidth170'><span class='msgPlace'></span></td>");
            tr.find(".entityNameDiv").width("initial");
            statusHTML.append(tr);
        }
        var p = getcmdsPopupBody(800, null, null, "Visibility Status").html(entityClonesPopup.children().clone(true));
        p.find("tbody").html(statusHTML.children());
        if (title) {
            p.find(".cmdsPopupHead").text(title);
        }
        return p;
    };
    this.assignMsgInPopup = function(popup, entityId, msg, extraClass) {
        var tr = popup.find(".popupEntity_" + entityId);
        var innerHtml = "<div class='" + extraClass + "'>" + msg + "</div>";
        var statusTd = tr.find(".statusTd").html(innerHtml);
    };
    this.prepareEntityClonesPopup = prepareEntityClonesPopup;
})(jQuery);


//commons for moving
var moveUtil = new (function($) {
    this.init = function() {
        $("body")
                .on("click", ".moveResources", moveResources)
                .on("click", ".moveToFoldersSubmit", moveToFoldersSubmit)
    };

    var moveResources = function() {
        checkedEntities.init();
        preparemoveItemList("Move Resources To", "moveToFoldersSubmit");
    };
    var moveToFoldersSubmit = function() {
        var params = {entities: checkedEntities.srcEntities};
        moveToFoldersUtil("/qrresources/moveToFoldersSubmit",
                params, checkedEntities.entityList, $(this));
    };

    var moveToFoldersUtil = function(urlStr, params, itemList, $this) {
        showTopLoader();
        var popup = $this.closest("#cmdsPopup");
        var targetFolderId = popup.find(".moveWidget").data("finalValue");
        if (!targetFolderId) {
            showcmdsError("Please select a target Folder");
            return;
        }

        params.targetFolderId = targetFolderId;
        var successFn = function(data) {
            hideTopLoader();
            resetcmdsCBoxes();
            closecmdsPopup();
            for (var p = 0; p < itemList.length; p++) {
                itemList[p].remove();
            }
        };
        vReq.post(urlStr, params, successFn);
    };
})(jQuery);
moveUtil.init();



var preparemoveItemList = function(heading, submitClass) {
    var popup = getcmdsPopupBody(null, null, {submitClass: submitClass});
    popup.html('<div class="cmdsPopupHead">' + heading + '</div>\n\
    <div class="moveWidget"></div>');
    var urlParams = {start: 0, size: 1000, orderBy: "name.untouched"};
    var cbfn = function() {
        vMoveItem.init(popup.find(".moveWidget"), {urlStr: "/qrresources/moveToFolders",
            parentIdName: "folderId", urlParams: urlParams}, true);
    };
    checkmoveItemFile(cbfn);
};






//test creation and test preview
var getScheduleEntities = function(scheduleList, duration) {
    var hasError = false, scheduleEntites = [], errText = "Please Enter a proper Schedule";
    for (var k = 0; k < scheduleList.length; k++) {
        var entities = scheduleList.eq(k).find(".scheduleEntity");
        var fromDiv = entities.eq(0), toDiv = entities.eq(1);

        var dateMillisFrom = getDateMillisFromvChoose(fromDiv);
        var dateMillisTo = getDateMillisFromvChoose(toDiv);
        if (dateMillisFrom == -1 || dateMillisTo == -1) {
            hasError = true;
            break;
        }
        var timeSecsFrom = getHrsMinsSecs(fromDiv);
        var timeSecsTo = getHrsMinsSecs(toDiv);
        if (timeSecsFrom <= 0 || timeSecsTo <= 0) {
            hasError = true;
            break;
        }
        var fromTime = dateMillisFrom + timeSecsFrom;
        var toTime = dateMillisTo + timeSecsTo;
        if (duration && (toTime - fromTime) < duration) {
            errText = "Test Duration should be greater than the schedule duration";
            hasError = true;
            break;
        }
        var quesnsState = "ORIGINAL";
        quesnsState = scheduleList.eq(k).find(".vChooseQuesnsState").data("value");

        scheduleEntites.push({qOrderType: quesnsState, startTime: fromTime,
            endTime: toTime});
//            qids list misssing
    }
    return {hasError: hasError, scheduleEntites: scheduleEntites, errText: errText};
}


//package specific
var qrPackageCDPUtil = new (function() {
    var clickEvent = "click.qrPackageCDPUtil";
    this.contentsChecked = [];
    this.init = function() {
        $("body").off(clickEvent)
                .on(clickEvent, ".addToPackageSubmit", addToPackageSubmit)
                .on(clickEvent, ".addToCDPSubmit", addToCDPSubmit)
                .on(clickEvent, ".viewPkgHistory", viewPkgHistory)
    }

    this.addToCDP = function() {
        var url = "/qrcdpplans/addToCDP";
        var listUrl = url + "List";
        var addTocbfn = function(popup) {
            var urlParams = {start: 0, size: 25};
            var title = qrPackageCDPUtil.contentsChecked[0].title;
            popup.find(".cdpContentItemTitle").val(title);
            var cbfn = function() {
                vMoveItem.init(popup.find(".moveWidget"), {urlStr: listUrl,
                    parentIdName: "cdpId", urlParams: urlParams, beforecbfn: addToCDPBeforecbfn,
                    markItemcbfn: markItemcbfn}, false);
            }
            checkmoveItemFile(cbfn);
        }
        addToPackageCDPUtil(url, listUrl, "addToCDPSubmit", addTocbfn);
    }
    var addToCDPBeforecbfn = function(moveWidget, moveItem) {
        if (moveItem.hasClass("moveItemLevel3")) {
            return false;
        } else {
            return true;
        }
    }
    var markItemcbfn = function(moveWidget, moveItem) {
        var target = moveWidget.closest("#cmdsPopup").find(".cdpContentTitleTable");
        if (moveItem.data("level") == "3") {
            target.removeClass("nonner");
        } else {
            target.addClass("nonner");
        }
    }
    this.addToPackage = function() {
        var url = "/qrproducts/addToPackage";
        var listUrl = url + "List";
        addToPackageCDPUtil(url, listUrl, "addToPackageSubmit");
    }
    var addToPackageCDPUtil = function(addUrl, addListUrl, submitClass, addTocbfn) {
        var popup = getcmdsPopupBody(null, null, {submitClass: submitClass})
        inlineLoader(popup);
        var params = {start: 0, size: 25};
        var url = addListUrl;
        var successFn = function(data) {
            popup.html(data);
            if (addTocbfn) {
                addTocbfn(popup);
            }
            var listDiv = popup.find(".entityListDiv");
            if (listDiv.length > 0) {
                loadMoreEntities.init(listDiv, url, params, null, "HTML");
            }
        }
        vReq.get(addUrl, params, successFn);
    }
    var addToPackageSubmit = function() {
        addToPkgCDPSubmitUtil($(this), "/qrProducts/addToPackageSubmit");
    }
    var addToCDPSubmit = function() {
        addToPkgCDPSubmitUtil($(this), "/qrcdpplans/addToCDPSubmit", "CDP");
    }
    var addToPkgCDPSubmitUtil = function($this, submitUrl, utilType) {
        var popup = $this.closest("#cmdsPopup");
        var params = getFormValues(popup);
        params.contents = qrPackageCDPUtil.contentsChecked;
        if (utilType == "CDP") {
            var moveWidgetData = popup.find(".moveWidget").data();
            var level = moveWidgetData.itemParams.level;
            if (level == "3") {
                params.parentPlanId = moveWidgetData.finalValue;
                params.srcEntity = qrPackageCDPUtil.contentsChecked[0];
            } else if (level == "LIBRARY") {
                submitUrl = "/qrcdpplans/addToCDPLibrarySubmit";
                params.cdpId = moveWidgetData.finalValue;
            } else {
                showcmdsPopupError(popup, "Please read the instructions and add content");
            }
        } else {
            params.folderId = popup.find(".vRadioChecked").data("folderId");
        }
        startLoader();
        var successFn = function(data) {
            stopLoader();
            closePopup();
            resetcmdsCBoxes();
        }
        vReq.post(submitUrl, params, successFn);
    }
    var viewPkgHistory = function() {
        var histHTML = $(this).siblings(".packageHistoryDiv").html();
        showMessage(histHTML);
        $("#cmdsPopup").width(800);
    }
})(jQuery);
qrPackageCDPUtil.init();

var remContentFrmPkgCDPUtil = function() {
    var contents = [], contentList = makeHTMLTag("div");
    for (var k = 0; k < cmdsCBoxesChecked.length; k++) {
        var contentTr = cmdsCBoxesChecked.eq(k).closest("tr");
        var id = contentTr.data("entityId");
        var uiEntityType = contentTr.data("entityType");
        contents.push({type: entityTypeFramer.getEntityType(uiEntityType),
            id: id});
        contentList.append(contentTr);
    }
    return {contents: contents, contentTrs: contentList};
}
var createTSPackageCDPUtil = function($this, url) {
    var popup = $this.closest("#cmdsPopup");
    var params = getFormValues(popup);
    if (params.name == "") {
        showcmdsPopupError(popup);
        return;
    }
    //this check works for cdp creation
//        if(popup.find(".cdpFromDiv").length>0){
//            var startTime=getDayMonthYearMillis(popup.find(".cdpFromDiv"));
//            var endTime=getDayMonthYearMillis(popup.find(".cdpToDiv"));
//            if(startTime==-1||endTime==-1||(startTime-endTime)>0){
//                showcmdsPopupError(popup,"Please enter a proper duration.");
//                return;
//            }               
//            params.startTime=startTime;
//            params.endTime=endTime;
//        }            
    startLoader();
    var successFn = function(data) {
        closeStopRefreshCombo();
        //take to test series page or package page
    }
    vReq.post(url, params, successFn);
};


//share entity
var addToProgramLibraryUtil = new (function($) {
    this.init = function() {
        $("body").on("click", ".addToLibrarySubmit", addToLibrarySubmit)
                .on("click", ".addToProgramLibrary", addToProgramLibrary)
    };
    var addToProgramLibrary = function() {
        checkedEntities.init();
        sharecmdsEntities("addToLibrarySubmit");
    };
    var sharecmdsEntities = function(submitClass) {
        //(entityId,entityType,popup,allowPublic,showShareBtn,
        //        cbFn,allowIndv,allowInst,extraParams,afterShareUIOpenFn)
        var popup = getCommonPopupBody(450, 80);
        var afterShareUIOpenFn = function() {
            var holder = popup.find(".shareTypeHolder");
            smallLoader(holder);
            popup.find(".topSec").html("Choose a program/centers/sections").addClass("big15 boldy");
            popup.find(".submitShare").addClass(submitClass).text("Submit");
            popup.find(".shareWithDiv").off("click.submitShare");
            $.get("/Widgets/programListOfMember", function(data) {
                holder.html(data);
                holder.find(".instSelectShareBatch").data("onchange", "addToLibraryProgSelected");
            });
        };
        shareUi.open(null, null, popup, false, true, null, false, false, afterShareUIOpenFn);
    };
    var addToLibrarySubmit = function() {
        var popup = getPopupDiv($(this));
        var summaryDivs = popup.find(".instEachSharedHolder").find(".instEachSharedCenter");
        var orgEntities = [];
        //according to the ui, there is no chance to select just a programs
        //when a program is selected,automatically all the centers are selected.
        summaryDivs.each(function() {
            var wData = $(this).data("obj");
            if (wData) {
                var centers = wData.centers;
                var progOrgEntity = {type: "PROGRAM", id: wData.id};
                var centersPojo = [];
                for (var c = 0; c < centers.length; c++) {
                    var center = centers[c];
                    if (center.sections) {
                        $.merge(orgEntities, center.sections);
                    } else {
                        centersPojo.push(center);
                    }
                }
                if (centersPojo.length > 0) {
                    progOrgEntity.centers = centersPojo;
                    $.merge(orgEntities, [progOrgEntity]);
                }
            }
        });
        if (summaryDivs.length === 0) {
            showError("Please select a programme,center or section.");
            return;
        }
        var params = {contents: checkedEntities.srcEntities, orgEntities: orgEntities};
        startLoader();
        var successFn = function(data) {
            stopLoader();
            resetcmdsCBoxes();
            closePopup();
            addToLibrarySuccessFn(data);
        };
        vReq.post("/qrresources/addToLibrary", params, successFn);
    };
    var addToLibrarySuccessFn = function(data) {
        var list = data.result.list;
        if (list.length === 0) {
            showMessage("Successfully Done");
        } else {
            var quesnsToOrgEntitiesMap = prepareQuesnsAndOrgEntitiesPojo(list);
            var popup = checkedEntities.prepareEntityClonesPopup();

            var cloneTableBody = popup.find(".entityCloneTable").children("tbody");
            $.each(quesnsToOrgEntitiesMap, function(srcEntityId, errorsDiv) {
                var tr = cloneTableBody.children(".popupEntityTr_" + srcEntityId)
                        .removeClass("nonner");
                tr.find(".statusTd").html(errorsDiv);
            });
        }
    };
    var prepareQuesnsAndOrgEntitiesPojo = function(list) {
        var quesnsToOrgEntitiesMap = {};
        for (var k = 0; k < list.length > 0; k++) {
            var entity = list[k], srcEntityId = entity.content.id;
            var errorCode = entity.errorCode;
            if (errorCode === "") {
                continue;
            }
            if (!quesnsToOrgEntitiesMap[srcEntityId]) {
                quesnsToOrgEntitiesMap[srcEntityId] = makeHTMLTag("div");
            }
            quesnsToOrgEntitiesMap[srcEntityId].append(prepareErrHTML(entity));
        }
        return quesnsToOrgEntitiesMap;
    };
    var prepareErrHTML = function(entity) {
        var errorCode = entity.errorCode;
        var orgEntity = entity.orgEntity;
        var errDiv = makeHTMLTag("div", {"class": "margHalfTop boldy smally"});
        errDiv.html(orgEntity.name + ": " + "<span class='redColor boldy'>" + errorCode + "</span>");
        return errDiv;
    };
})(jQuery);
var addToLibraryProgSelected = function(vChoose, value) {
    shareUi.getBatchDetails(vChoose, value, "/widgets/centersOfProgramOfMember");
    var title = vChoose.find('.vChooseOptTicked').text();
    vChoose.attr("title", title);
};
addToProgramLibraryUtil.init();




//CHECK result
var checkResultList = function(resultList, targetTableBody, entityLevelFn) {
    var statusHTML = makeHTMLTag("div");
    for (var k = 0; k < resultList.length; k++) {
        var entity = resultList[k];
        var id = entity.entityId, status = entity.errorCode, tr = makeHTMLTag("tr");
        var entityTr = targetTableBody.children(".cmdsTr_" + id);
        var entityName = entityTr.find(".entityNameDiv").html();
        if (status == "") {
            status = "<span class='greenColor boldy'>Done!!</span>";
            if (entityLevelFn) {
                entityLevelFn(entityTr);
            }
        } else {
            status = "<span class='redColor boldy'>" + status + "</span>";
        }
        tr.html("<td>" + entityName + "</td>");
        tr.append("<td>" + status + "</td>");
        statusHTML.append(tr);
    }
    var popup = getcmdsPopupBody(800).html(checkResultSample.children().clone(true));
    popup.find("tbody").html(statusHTML.children());
};


//publish doc
var publishDocVideosUtil = function() {
    var docIds = [$(this).data("docId")];
    showTopLoader();
    var scope = $(this).closest("#cmdsPopup").find(".vChoose").data("value");
    var successFn = function(data) {
        hideTopLoader();
        closecmdsPopup();
        publishDocVideoEl.removeClass("gRedButton").addClass("boldy").text("Published");
    }
    vReq.post("/qrDocuments/publishDocVideos", {docIds: docIds, scope: scope}, successFn);
}
$("body").on("click", ".publishDocVideosUtil", publishDocVideosUtil);
var publishDocVideoEl;
$("body").on("click", ".publishcmdsDoc,.publishcmdsVideo", function() {
    var popup = fillcmdsPopup("publishDocVideosUtil", "publishEntitySample");
    var docId = $(this).data("docId");
    popup.closest("#cmdsPopup").find(".publishDocVideosUtil").data("docId", docId);
    publishDocVideoEl = $(this);
});


//tags of entities
var getFolderTag = function(entityId, entityName) {
    return '<span class="resourceIconFOLDER"></span>\n\
    <a class="getFolderPage cmdsaPush"  title="' + entityName + '" data-entity-id="' + entityId + '"\n\
    href="' + vcmdsUrls.FOLDER(entityId) + '">' + entityName + '</a>';
}


//load more for entities like tests
var loadMoreEntities = new (function($) {
    this.init = function(entityListDiv, url, params, cbfn, respType, loadContent) {
        var totalEl = entityListDiv.find(".entityListTotal"), totalHits = 0;
        if (totalEl.length > 0) {
            totalHits = totalEl.val();
            totalEl.remove();
        }
        entityListDiv.data({url: url, params: params, cbfn: cbfn, respType: respType,
            totalHits: totalHits});
        if (loadContent) {
            loadMoreUtil(entityListDiv, 0);
        } else {
            putLoadMore(entityListDiv, params.size);
        }

        entityListDiv.off()
                .on("click", ".loadMoreEntities", loadMoreEntities.loadMore);
    }
    this.loadMore = function() {
        var $this = $(this), listDiv = $this.closest(".entityListDiv");
        var start = $this.data("start");
        $this.remove();
        loadMoreUtil(listDiv, start);
    }
    var loadMoreUtil = function(listDiv, start) {
        var listDivData = listDiv.data(), params = listDivData.params;
        params.start = start;
        var successFn = function(data) {
            if (listDivData.respType === "HTML") {
                listDiv.find(".entityList").append(data);
            }
            if (listDivData.cbfn) {
                window[listDivData.cbfn](data, listDiv);
            }
            putLoadMore(listDiv, start + params.size);
        }
        vReq.get(listDivData.url, params, successFn);
    }
    var putLoadMore = function(listDiv, newStart) {
        var total = listDiv.data("totalHits");
        if (newStart >= total) {
            return;
        }
        listDiv.append("<div class='margHalfTop'>\n\
        <a class='loadMoreEntities smally'>Load More..</a>\n\
        </div>");
        listDiv.find(".loadMoreEntities").data("start", newStart);
    }
})(jQuery);

var publishUtil = new (function($) {
    var pollUrl, progressBarParent, jobId, successFn, errorFn, publishTimer;
    this.init = function(url, jId, progressBarPa, sFn, errFn) {
        pollUrl = url;
        jobId = jId;
        successFn = sFn;
        errorFn = errFn;
        if (!progressBarPa) {
            progressBarParent = getcmdsPopupBody().html(publishBarSample.children().clone());
            progressBarParent.closest("#cmdsPopup").find(".closecmdsPopup,.cmdsPopupBtns")
                    .addClass("nonner");
        } else {
            progressBarParent = progressBarPa;
        }
        publishStatusUpdater();
        publishTimer = setInterval(function() {
            publishStatusUpdater();
        }, 5000);
    }

    var publishStatusUpdater = function() {
        $.get(pollUrl, {jobId: jobId}, function(data) {
            var result = data.result;
            var percent = result.percentCompleted;
            progressBarParent.find(".publishPercent").html(percent + "%");
            var maxWidth = progressBarParent.find(".publishGrayBar").width();
            progressBarParent.find(".publishGreenBar").width(percent * maxWidth / 100);
            if (result.isComplete) {
                try {
                    clearInterval(publishTimer);
                } catch (err) {
                    console.log("err in clearing interval")
                }
                if (result.isSuccess && successFn) {
                    successFn();
                } else {
                    showcmdsError("Some error occured. Please try again");
                    //print the question here;
                    if (errorFn) {
                        errorFn();
                    }
                }
            }
        });
    }

})(jQuery);



var filterAcadEntWidget = new (function($) {
    var tagger = makeHTMLTag;
    this.progChange = function(mcWidget, vChoose) {
        var mcWidgetVar = manageContent;
        var mcWidgetParams = mcWidget.data("params");

        mcWidgetVar.removemcWidgetParams(mcWidgetParams, ["centerId", "sectionId", "courseId", "brdIds"]);
        resetCenters(vChoose);
        resetSections(vChoose);
        var programId = vChoose.data("value");
        var filterWidget = vChoose.closest(".filterProgCenterSecDiv");

        //loadcourses
        setCourses(programId, filterWidget);

        mcWidgetVar.loadmcContent(mcWidget, vChoose);

        ///load centers

        setProgramAndResetCenters(programId, filterWidget);
    };


    this.centerChange = function(mcWidget, vChoose) {
        var mcWidgetVar = manageContent;
        var mcWidgetParams = mcWidget.data("params");
        mcWidgetVar.removemcWidgetParams(mcWidgetParams, ["sectionId"]);
        resetSections(vChoose);
        mcWidgetVar.loadmcContent(mcWidget, vChoose);

        ///load centers
        var centerId = vChoose.data("value");
        var programId = vChoose.siblings(".progvChoose").data("value");
        if (centerId != "-1") {
            var key = programId + "_" + centerId;
            var sections = getOpts(key, "sections");
            resetSections(vChoose, sections);
        }
    };
    this.loadWidget = function(filterWidget, params) {
        var fn = function() {
            populatePrograms(filterWidget);
            var programId = params.programId, centerId = params.centerId, sectionId = params.sectionId;
            setProgramAndResetCenters(programId, filterWidget);
            setCenterAndResetSections(programId, centerId, filterWidget);
            setSection(sectionId, filterWidget);
            setCourses(programId, filterWidget);
        };
        if (memberInfoPojo.loaded) {
            fn();
        } else {
            memberInfoPojocbfn = fn;
        }
    };


    var populatePrograms = function(filterWidget) {
        var progList = getMemberInfoPojo().progList;
        progList = [{id: "-1", name: "All Programs"}].concat(progList);
        var opts = makeOptsFromList(progList);
        filterWidget.find(".progvChoose").find(".vChooseDropDown").html(opts);
    };
    var setProgramAndResetCenters = function(programId, filterWidget) {
        var memberInfo = getMemberInfoPojo().data;
        var progvChoose = filterWidget.find(".progvChoose");
        if (programId != "-1" && programId) {
            vChooseVar.reset(progvChoose, programId, memberInfo[programId].name);
            var centersOpts = getOpts(programId, "centers");
            resetCenters(progvChoose, centersOpts);
        }
    };
    var setCenterAndResetSections = function(programId, centerId, filterWidget) {
        var memberInfo = getMemberInfoPojo().data;
        var key = programId + "_" + centerId;
        var centervChoose = filterWidget.find(".centervChoose");
        if (centerId != "-1" && centerId) {
            vChooseVar.reset(centervChoose, centerId, memberInfo[key].name);
            var sectionOpts = getOpts(key, "sections");
            resetSections(centervChoose, sectionOpts);
        }
    };
    var setSection = function(sectionId, filterWidget) {
        var memberInfo = getMemberInfoPojo().data;
        var sectionvChoose = filterWidget.find(".sectionvChoose");
        if (sectionId != "-1" && sectionId) {
            vChooseVar.reset(sectionvChoose, sectionId, memberInfo[sectionId].name);
        }
    };
    var setCourses = function(programId, filterWidget) {
        var opts;
        var isProgPage = false;
        var loadSubs;
        var progPage = filterWidget.closest("#programPage");
        if (progPage.length > 0) {
            isProgPage = true;
            loadSubs = qrmcWidgetUtil.loadRadioSubs;
        }
        if (programId != "-1" && programId) {
            $.get("/qracadstr/getProgramCourses", {programId: programId}, function(data) {
                var courses = data.result.list;
                if (isProgPage) {
                    loadSubs(progPage, "", courses);
                } else {
                    opts = makeOptsFromList(courses);
                    resetCourses(filterWidget, opts);
                }
            });
        } else {
            if (isProgPage) {
                loadSubs(progPage, "", []);
            } else {
                opts = makeOptsFromList();
                resetCourses(filterWidget, opts);
            }
        }
    };
    this.setCourses = setCourses;


    var getOpts = function(entityIdOrKey, reqChildntityName) {
        var memberInfo = getMemberInfoPojo().data;
        var entities = memberInfo[entityIdOrKey][reqChildntityName];
        return makeOptsFromList(entities);
    };
    var makeOptsFromList = function(entities) {
        var entityOptsHolder = tagger("div");
        entities = entities || [];
        for (var c = 0; c < entities.length; c++) {
            var entity = entities[c], entityId = entity.id, entityName = entity.name;
            var opt = tagger("div", {"class": "vChooseOpt"})
                    .data("value", entityId).text(entityName);
            entityOptsHolder.append(opt);
        }
        if (entities.length === 0) {
            entityOptsHolder.html(tagger("div", {"class": "vChooseOpt invalidvChooseOpt"})
                    .data("value", "-1").text(""));
        }
        return entityOptsHolder.children();
    };
    var resetCenters = function(vChoose, opts) {
        resetUtil(vChoose, opts, "centervChoose", "Select a Center",
                "Select a Program first", "All Centers");
    };
    var resetSections = function(vChoose, opts) {
        resetUtil(vChoose, opts, "sectionvChoose", "Select a Section",
                "Select a Center first", "All Sections");
    };
    var resetCourses = function(vChoose, opts) {
        resetUtil(vChoose, opts, "coursevChoose", "Select a Course",
                "Select a Program first", "All Courses");
    };
    var resetUtil = function(vChoose, opts, vChooseClass, resetHeadText,
            defaultOptText, allOptsText) {
        var filterWidget = vChoose.closest(".filterProgCenterSecDiv");
        var targetvChoose = filterWidget.find("." + vChooseClass);
        var holder = tagger("div");
        if (!opts) {
            opts = tagger("div", {"class": "vChooseOpt invalidvChooseOpt"})
                    .data("value", "-1").text(defaultOptText);
            holder.html(opts);
        } else if (opts && allOptsText) {
            var allOpt = tagger("div", {"class": "vChooseOpt"}).data("value", "-1")
                    .text(allOptsText);
            holder.html(opts).prepend(allOpt);
        }
        vChooseVar.reset(targetvChoose, "-1", resetHeadText, holder.children());
    };
})(jQuery);
var onFilterWidgetProgChange = function(mcWidget, vChoose) {
    filterAcadEntWidget.progChange(mcWidget, vChoose);
};
var onFilterWidgetCenterChange = function(mcWidget, vChoose) {
    filterAcadEntWidget.centerChange(mcWidget, vChoose);
};





var qrmcWidgetUtil = new function($) {
    var testCreationVar = "TEST_CREATION";
    var makeTag = makeHTMLTag;
    var loadRadioSubs = function(mcWidget, qrTarget, subs, afterSubsLoadCbfn) {
        var targetDiv = mcWidget.find(".cmdsSearchSubsDiv");
        inlineLoader(targetDiv);
        var cbfn = function(subs) {
            prepareRadioSubsHTML(subs, targetDiv, afterSubsLoadCbfn);
        };
        if (subs) {
            cbfn(subs);
        } else if (qrTarget === testCreationVar) {
            cbfn(qrTests.testmetadata);
        } else {
            loadAndCacheCourses(cbfn);
        }
    };
    var prepareRadioSubsHTML = function(subs, targetDiv, afterSubsLoadCbfn) {
        var newmvc = makeTag("div", {"class": "vRadioGrp"}).data({paramName: "courseId",
            callback: "onmcvRadioChange", value: "-1", beforemccall: "setGetTopicTree"});
        for (var k = 0; k < subs.length; k++) {
            var s = subs[k], courseId = s.id;
            var item = makeTag("div", {"class": "vRadio sub_" + courseId});
            item.data({value: courseId}).text(s.name);
            newmvc.append(item);
        }
        newmvc.prepend(makeTag("div", {"class": "vRadio vRadioChecked boldy"})
                .data("value", "-1").text("All Subjects"));
        targetDiv.html(newmvc);
        var targetPa = targetDiv.parent();
        var topicsPa = targetDiv.siblings(".cmdsTableTopicTree")
                .addClass("nonner");
        topicsPa.children(".cmdsSearchTopicsDiv").html("");
        if (subs.length === 0) {
            targetPa.addClass("nonner");
        } else {
            targetPa.removeClass("nonner");
        }
        if (afterSubsLoadCbfn) {
            afterSubsLoadCbfn();
        }
    };
    this.prepareRadioSubsHTML = prepareRadioSubsHTML;
    this.loadRadioSubs = loadRadioSubs;
//    this.loadSources=function(mcWidget){
//        var mvs=getmvsSample("mcMultipleVSelect","listItemIds","onmcMVSChange","All Sources");        
//        var target=mcWidget.find(".cmdsSearchSourcesDiv");
//        var cbfn=mvsUtil(mvs,target); 
//        loadAndCacheSources(cbfn);
//    }
    this.loadTests = function(mcWidget) {
        var mvs = getmvsSample("mcMultipleVSelect", "destinationIds", "onmcMVSChange", "All Tests");
        var target = mcWidget.find(".cmdsSearchTestsDiv");
        var cbfn = mvsUtil(mvs, target);
        loadAndCacheTests(cbfn);
    };
    this.loadTopics = function(mcWidget, vRadioGrp, loadmcContent) {
        var mvs = getmvsSample("mcMultipleVSelect", "brdIds", "onmcMVSChange", "All Topics");
        //to attach the courseId when a topic changes,because 
        //a topic change replaces entire brdIds
        mvs.find(".mvsAllItems").addClass("boldy");
        mvs.data("beforemccall", "setCourseIdForTopics");
        delete mvs.data("captureAllTopics");


        var sub = vRadioGrp.find(".vRadioChecked");
        if (!mcWidget.data("params")) {
            mcWidget.data("params", {});
        }
        var mcParams = mcWidget.data("params");
        mcParams.brdIds = [];
        var target = mcWidget.find(".cmdsSearchTopicsDiv");
        var courseId = sub.data("value");
        var pa = target.parent();
        var topicIds = [], hasTopics = false;
        var cbfn = mvsUtil(mvs, target);

        var isTestCreationPage = false;
        if (mcWidget.closest("#testPage").length > 0) {
            isTestCreationPage = true;
        }
        if (!isTestCreationPage && courseId != "-1") {
            loadAndCacheTopics(courseId, cbfn);
        } else if (courseId != "-1" && isTestCreationPage) {
            qrTests.currentSubData = qrTests.testmetadata[sub.index()];
            var topics = qrTests.currentSubData.children;
            if (topics && topics.length > 0) {
                for (var p = 0; p < topics.length; p++) {
                    var t = topics[p];
                    topicIds.push(t.id);
                }
                pa.removeClass("nonner");
                mcParams.brdIds = topicIds;
                cbfn(topics);
                hasTopics = true;
                mvs.data("captureAllTopics", true);
            } else {
                pa.addClass("nonner");
                loadAndCacheTopics(courseId, cbfn);
            }
        } else {
            pa.addClass("nonner");
        }



        if (loadmcContent) {
            if (courseId != "-1") {
                $.merge(mcParams.brdIds, [courseId]);
            }
            manageContent.loadmcContent(mcWidget, vRadioGrp);
        }
    };
    var mvsUtil = function(mvs, target) {
        var mvsItem = mvs.find(".mvsItem");
        var cbfn = function(list) {
            for (var k = 0; k < list.length; k++) {
                var listItem = list[k], name = listItem.name;
                var item = mvsItem.clone(true);
                item.find(".mvsItemText").text(name).attr("title", name);
                item.find(".gCBox").data("value", listItem.id);
                mvs.append(item);
            }
            mvsItem.remove();
            target.html(mvs);
            var pa = target.parent();
            if (list.length === 0) {
                pa.addClass("nonner");
            } else {
                pa.removeClass("nonner");
            }
        };
        return cbfn;
    };
}(jQuery);
var setGetTopicTree = function(mcWidget, vRadioGrp) {
    qrmcWidgetUtil.loadTopics(mcWidget, vRadioGrp, true);
};
var setCourseIdForTopics = function(mcWidget, mvs) {
    var params = mcWidget.data("params");
    var courseId = params.courseId;
    if (!params.brdIds) {
        params.brdIds = [];
    }

    if (mvs.data("captureAllTopics") && params.brdIds.length === 0) {
        //==>test creation and all topics selected for a subject which has topics metadata chosen
        var cboxes = mvs.find(".mvsItem .gCBox");
        for (var k = 0; k < cboxes.length; k++) {
            params.brdIds.push(cboxes.eq(k).data("value"));
        }
    }
    if (courseId != "-1" && params.brdIds.length === 0) {
        params.brdIds = [courseId];
    }
    manageContent.loadmcContent(mcWidget, mvs);
};



//channels
var qrChannels = new (function() {
    var channelsPage, bodyClickEvent = "click.qrChannels";
    this.init = function() {
        channelsPage = $("#channelsPage");



        $("body").off(bodyClickEvent)
                .on("click", ".createChannel", createChannel)
                .on("click", ".openChannelPage", openChannelPage)
                .on(bodyClickEvent, ".submitCreateChannel", submitCreateChannel)
    };

    var createChannel = function() {
        fillcmdsPopup("submitCreateChannel", "createChannelSample");
    };
    var submitCreateChannel = function() {
        var popup = getPopupDiv($(this));
        var params = getFormValues(popup);
        startLoader();
        var successFn = function() {
            closePopup();
            stopLoader();
            refreshPage();
        };
        vReq.post("/qrchannels/createchannel", params, successFn);
    };

    var openChannelPage = function() {

    };
})(jQuery);
qrChannels.init();


var filterContentUtil = new (function($) {
    var clickEvent = "click.filterContentUtil";
    this.init = function() {
        $("body").off(clickEvent)
                .on(clickEvent, ".getQuesnsContent", getQuesnsContent)
                .on(clickEvent,".getParaQuestions",getParaQuestions)
                .on(clickEvent,".getDuplicateParaQuestions",getDuplicateParaQuestions)
                .on(clickEvent,".checkDuplicates",checkDuplicates)
    };
    var getQuesnsContent = function() {
        var quesType = $(this).data("questionType");
        var mcWidget = $(this).closest(".mcWidget");
        var filtervChoose = mcWidget.find(".vChooseContentTypes");
        if(quesType == "PARA"){
            delete mcWidget.data("params").includeTypes;
            vChooseVar.reset($(".vChooseQuesType"),-1,"All Types");
        }
        delete mcWidget.data("pageUrlParams").paraId;
        mcWidget.data("params").includes = "CMDSQUESTION";
        mcWidget.data("params").quesType = quesType;
        mcWidget.data("pageUrlParams").quesType = $(this).data("questionType");
        vChooseVar.reset(filtervChoose, "CMDSQUESTION", "Questions");
        filterContent(mcWidget, filtervChoose);
        // $("#fixedLeftSecPortion").removeClass("nonner");
    };

    var checkDuplicates = function(){
        $(".RTEArea").removeClass("MATHJAX");
        var query ="";
        var query1 = $(this).closest(".quesBody").find(".quesTextDiv .big15");
        var query2 = $(".QAQuesTextRTEDiv .RTEArea");
        if(query1.text() == "" || query1.text() == undefined || query1.text() == null){
            query = query2;
        }
        else{
            query = query1;
        }
        query.has(".RTELatex").addClass("MATHJAX");
        if(query.hasClass("MATHJAX")){
            showError("Cannot check duplicates for a question in mathjax format");
            return ;
        }
        query = query.text().trim();
        if(query =="" || query == undefined || query == null){
            showError("Cannot check duplicates for this question");
            return ;
        }
        var quesType = "ALL";
        var target = "CHECK_DUPLICATES";
        var start = 0;
        var size =25;
        var includes = "CMDSQUESTION";
        var firstTimeLoad = "true";
        window.open("/organization/"+cmdsOrgId+"/checkduplicates?query="+query+"&quesType="+quesType+"&target="+target+"&start="+start+"&size="+size+"&includes="+includes+"&firstTimeLoad="+firstTimeLoad);
    }

    var getParaQuestions = function() {
        var mcWidget = $(this).closest(".mcWidget");
        var paraId = $(this).data("qid");
        var paragraph = $(".ques_"+paraId).find(".quesTextDivHolder").html();
        var filtervChoose = mcWidget.find(".vChooseContentTypes");
        var quesType = $(this).data("type");
        if(quesType == "TEXT"){
            quesType = "PARA_QUES";
            delete mcWidget.data("params").includeTypes;
            delete mcWidget.data("params").includeDifficulty;
            // $("#fixedLeftSecPortion").addClass("nonner");
            vChooseVar.reset($(".vChooseLevel"), -1);
        }
        mcWidget.data("params").includes = "CMDSQUESTION";
        mcWidget.data("params").paraId = paraId;
        mcWidget.data("params").quesType = quesType
        mcWidget.data("pageUrlParams").quesType = quesType;
        mcWidget.data("pageUrlParams").paraId = paraId;
        vChooseVar.reset(filtervChoose, "CMDSQUESTION", "Questions");
        filterContent(mcWidget, filtervChoose);
    };

    var getDuplicateParaQuestions = function(){
        var mcWidget = $(this).closest(".mcWidget");
        var paraId = $(this).data("qid");
        var filtervChoose = mcWidget.find(".vChooseContentTypes");
        var quesType ="PARA_QUES";
        mcWidget.data("params").paraId = paraId;
        mcWidget.data("params").quesType = quesType;
        mcWidget.data("params").target = "CHECK_DUPLICATES";
        vChooseVar.reset(filtervChoose, "CMDSQUESTION", "Questions");
        pushHistory(null, null, "/organization/"+cmdsOrgId+"/checkduplicates?includes=CMDSQUESTION&paraId="+paraId+"&quesType="+quesType+"&target="+"CHECK_DUPLICATES");
        opencmdsPage("/qrQuestions/checkDuplicates",mcWidget.data("params"));
    }
    var filterContent = function(mcWidget, vChoose) {
        var target = vChoose.data("target");
        var isLibraryResources = false;
        if (target === "LIBRARY_RESOURCES") {
            isLibraryResources = true;
        }

        var entityType = vChoose.data("value");
        var mcWidgetVar = manageContent;
        var mcWidgetData = mcWidget.data();
        var mcWidgetParams = mcWidgetData.params;
        mcWidgetParams.target = target;
        var quesnsOptsStr = "normalQuesns";
        var otherOptsStr = "resources";
        if (isLibraryResources) {
            quesnsOptsStr = "library";
            otherOptsStr = "library";
        }

        if (entityType === "CMDSQUESTION") {
            setOptsType(mcWidget, quesnsOptsStr);
        } else {
            setOptsType(mcWidget, otherOptsStr);
        }
        mcWidgetVar.loadmcContent(mcWidget, vChoose);
    };
    this.filterContent = filterContent;

    var setOptsType = function(mcWidget, optsType) {
        mcWidget.find(".cmdsTableCheckedOpts").data("optsType", optsType);
    };

})(jQuery);
filterContentUtil.init();
var filterContent = filterContentUtil.filterContent;



var visibilityStatusUtil = function() {
    var $this = $(this);
    var completed = $this.data("completedError");
    var converted = $this.data("convertedError");
    if (completed) {
        showError(completed);
        return;
    } else if (converted) {
        showError(converted);
        return;
    }
    var tr = $this.closest("tr");
    if (tr.length === 0) {
        tr = $this.closest(".ques");
    }
    var target = "";
    if ($this.closest("#programPage").length > 0) {
        target = "LIBRARY_RESOURCES";
    }
    var $data = tr.data();
    var entityType = $data.entityType;
    var entityName = $data.entityName ? $data.entityName : "";
    var allowDownload = $this.data("allowDownload");
    var content = {id: $data.entityId, type: entityType};
    startLoader();
    var params = {content: content, entityName: entityName, start: 0, size: 100, target: target, status: $data.status, allowDownload: allowDownload};
    var successFn = function(data) {
        stopLoader();
        getcmdsPopupBody(800).html(data);
    };
    vReq.get("/Widgets/visibilityStatus", params, successFn);
};
$("body")
        .on("click", ".getVisibilityStatus", visibilityStatusUtil)



var editInstituteInfoUtil = new (function($) {
    this.init = function() {
        $(document)
                .on("click", ".addNewLocation", addNewLocation)
                .on("click", ".removeLocation", removeLocation)
                .on("click", ".submitEditInstituteInfo", submitEditInstituteInfo)
                .on("click", ".cancelEditInstituteInfo", refreshPage)
                .on("change", ".radioAuthType", radioAuthTypeChanged)
                .on("change",".disableSignup",disableSignup)
                .on("change",".smsgateway-select",selectSMSGateway)
                .on("change",".enableOTPInput",enableOTPInput)
    };

    var radioAuthTypeChanged = function() {
        var $this = $(this);
        var val = $this.val();
        var internalTr = $(".vedantuAuthTr");
        var externalTr = $(".extAuthTr");
        if (val == "VEDANTU") {
            internalTr.removeClass("nonner");
            externalTr.addClass("nonner");
            $(externalTr).find("[name='endPoint.authEndpoint']").data("req", false);
        } else {
            externalTr.removeClass("nonner");
            internalTr.addClass("nonner");
            $(externalTr).find("[name='endPoint.authEndpoint']").data("req", true);
        }
    };

    var enableOTPInput = function(){
        var checked = $(this).is(":checked");
        if(checked){
            $(".sms-gateway-section").removeClass("disableOption");
        }
        else{
            $(".sms-gateway-section").addClass("disableOption");
        }
    }

    var disableSignup = function(){
        var disableSignup = $("input[type=radio][name=disableSignup]:checked").val();
        if(disableSignup === "true"){
            $(".disableSignupMessageHolder").removeClass("nonner");
        }
        else{
            $(".disableSignupMessageHolder").addClass("nonner");
        }
    }

    var selectSMSGateway = function(){
        var gateway = $(this).val();
        if(gateway === "SMSCOUNTRY"){
            $(".sms-country-options").removeClass("nonner");
            $(".sms-grid-options").addClass("nonner");
        }else{
            $(".sms-country-options").addClass("nonner");
            $(".sms-grid-options").removeClass("nonner");
        }
    }

    var addNewLocation = function() {
        var locDiv = $(this).closest(".locationDiv");
        var newLoc = locDiv.clone(true);
        newLoc.find(".addNewLocation")
                .toggleClass("addNewLocation removeLocation").text("Remove");
        locDiv.parent().append(newLoc);
        newLoc.find("input").val("");
    };
    var removeLocation = function() {
        var target = $(this).closest(".locationDiv");
        target.remove();
    };
    var submitEditInstituteInfo = function() {
        var targetDiv = cSecHolder;
        var params = getFormValues(targetDiv);
        var locations = [];
        var smsGateway = {};
        //get locations
        var locDivs = targetDiv.find(".locationDiv");
        for (var k = 0; k < locDivs.length; k++) {
            var locDiv = locDivs.eq(k), inputDivs = locDiv.find("input");
            var city = inputDivs.eq(0).val();
            var state = inputDivs.eq(1).val();
            var country = inputDivs.eq(2).val();
            if (city && state && country) {
                locations.push({city: city, state: state, country: country});
            }
        }
        var err, errText = "Fields marked with * are compulsory.";
        if (params.hasError) {
            err = params.errorText ? params.errorText : errText;
        } else if (locations.length === 0) {
            err = errText;
        }

        var errorDiv = targetDiv.find(".editInstituteErrDiv");
        if (err) {
            errorDiv.removeClass("nonner").text(err);
            return;
        } else {
            errorDiv.addClass("nonner");
        }
        params.locations = locations;
        params.doubtsForumMode = $('input[type=radio][name=DoubtsForumMode]:checked').attr('id');
        params.disableSignup = $("input[type=radio][name=disableSignup]:checked").val() === "true" ? true:false;
        var enableOTP = $(".enableOTPInput").is(":checked");
        params.enableOTP = enableOTP;
        smsGateway = getSmsGatewayFormResponse();
        if(enableOTP === true && !smsGateway.validateForm){
            return false;
        }
        if(smsGateway.validateForm && enableOTP){
            params.smsGateway = smsGateway;
            params.enableOTP = true;
        }
        if(!params.disableSignup){
            params.disableSignupMessage = "";
        }
        else{
            params.disableSignupMessage = $("#disableSignupMessage").val();
        }
        params.updateList = [];
        for (key in params) {
            if (!key.contains("socialMedia") 
		&& !key.contains("endPoint")
		&& !key.contains("appInfos")) {
                params.updateList.push(key);
            }
        }
        params.updateList.push("endPoint");
        params.updateList.push("socialMedia");
        params.updateList.push("appInfos");
        params.updateList.push("encLevel");
        removeItemFromArr(params.updateList, "hasError");
        removeItemFromArr(params.updateList, "errorText");
        removeItemFromArr(params.updateList, "updateList");
        removeItemFromArr(params.updateList, "typeNames");
        removeItemFromArr(params.updateList, "isEnc");
        startLoader();
        cmdsBlackOut(true);
        vReq.post('/qracadstr/submitEditInstituteInfo', params, uploadProfilePic);
    };

    var getSmsGatewayFormResponse = function(){
        var smsGateway = {};
        var host = $(".smsgateway-select").val();
        smsGateway.host = host;
        var parentDiv = "";
        var validateForm = true;
        if(host === "SMSCOUNTRY"){
            parentDiv = "sms-country-options";
        }else{
            parentDiv = "sms-grid-options";
        }
        $("."+parentDiv+" .smsGatewayFormInput").each(function(){
            var field = $(this);
            var fieldName = field.attr("name");
            var fieldValue = field.val();
            if(fieldValue === "" || fieldValue === undefined || field.hasClass("hasError")){
                field.closest(".inputDiv").addClass("border-red");
                validateForm = false;
            }else{
                smsGateway[""+fieldName] = fieldValue;
            }
        });
        validateForm === true ? $(".sms-gateway-message").removeClass("redColor").addClass("greenColor").html("SMS Gateway Details stored successfully") : $(".sms-gateway-message").removeClass("greenColor").addClass("redColor").html("SMS Gateway Details not stored...Please fix all fields.");
        smsGateway.validateForm = validateForm;
        if(smsGateway.validateForm){
            smsGateway.postData = constructPostData(smsGateway,host);
        }
        return smsGateway;
    }

    var constructPostData = function(smsGateway,host){
        var postData = "";
        if(host === "SMSCOUNTRY"){
            postData += "User="+encodeURI(smsGateway.User)+"&passwd="+smsGateway.passwd+"&sid="+smsGateway.sid+"&mtype=N"+"&DR=Y";
        }
        else{
            postData += "user="+(smsGateway.user)+"&apikey="+smsGateway.apikey+"&sender_id="+smsGateway.sender_id+"&route=4";
        }
        return postData;
    }
    var uploadProfilePic = function() {
        var u = uploadProfilePicUtil;
        var cbfn = function(data) {
            stopLoader();
            cmdsBlackOut();
            refreshPage();
            $("#topBar").find(".topBarInstiLogo img")
                    .attr("src", data.result.thumbnail);
        };
        u.cbfn = cbfn;
        u.qqUploadVar.setParams({uploadFileParamName: "inputFile"});
        if (u.qqUploadVar && u.picFile) {
            startLoader();
            u.qqUploadVar._onInputChange(u.picFile);
        } else {
            stopLoader();
            refreshPage();
        }
    };
})(jQuery);
editInstituteInfoUtil.init();


var editNameDescUtil = new (function($) {
    var clickEvent = "click.editNameDescUtil";
    var nameEl, descEl;
    var popupWindow;
    var entityType;
    var entityId;
    var urlClass;
    this.init = function() {
        $("body").off(clickEvent)
                .on(clickEvent, ".submitEditedEntityDetails", submitEditedEntityDetails)
                .on(clickEvent, ".editEntityDetails", editEntityDetails);
    };
    var editEntityDetails = function(details) {
        var details = {};
        details.nameEl = $(".entityEditableName");
        details.descEl = $(".entityEditableDesc");
        details.name = details.nameEl.text().trim();
        var desc = details.descEl.text().trim();
        if (details.descEl.data("allText")) {
            desc = details.descEl.data("allText");
        }
        details.desc = desc;
        entityType = $(this).data("entityType");
        entityId = $(this).data("entityId");
        urlClass = $(this).data("urlClass");
        showEditPopup(details);
    };
    var showEditPopup = function(details) {
        var name = details.name;
        var desc = details.desc;
        nameEl = details.nameEl;
        descEl = details.descEl;
        if (!name && !desc) {
            return;
        }
        popupWindow = fillcmdsPopup("submitEditedEntityDetails", "editNameDescSample");
        if (nameEl.length === 0) {
            popupWindow.find(".nameTr").remove();
        } else {
            popupWindow.find(".nameTr input").val(name);
        }
        if (descEl.length === 0) {
            popupWindow.find(".descTr").remove();
        } else {
            popupWindow.find(".descTr textarea").val(desc);
        }
    };
    this.showEditPopup = showEditPopup;
    var submitEditedEntityDetails = function() {
        var params = getFormValues(popupWindow);
        if ($(this).hasClass("btnDisabled")) {
            return;
        }
        if (!params.hasError) {
            $(this).addClass("btnDisabled");
            params.entity = {type: entityType, id: entityId};
            params.updateList = ["name"];
            if (descEl && descEl.length > 0) {
                params.updateList.push("description");
                if (!params.description) {
                    params.description = "";
                }
            }
            params.urlClass = urlClass;
            vReq.post("/qrresources/updateTitleAndDesc", params, function() {
                closecmdsPopup();
                if (nameEl && nameEl.length > 0) {
                    nameEl.text(params.name);
                }
                var descHead = $(".entityEditableDescHead");
                var desc = params.description;
                if (descHead.length > 0) {
                    if (desc.length > 0) {
                        descHead.removeClass("nonner");
                    } else {
                        descHead.addClass("nonner");
                    }
                }
                if (descEl && descEl.length > 0) {
                    descEl.text(desc);
                    condenseText(descEl, 100);
                }
            });
        }
    };
})(jQuery);
editNameDescUtil.init();

var resultVisibilityStatusChange = function(vChoose, finalValue) {
    var messageDiv = vChoose.siblings('.resultVisibilityMessageDiv');
    messageDiv.find("textarea").val("");
    if (finalValue === "VISIBLE") {
        messageDiv.addClass("nonner");
    } else {
        messageDiv.removeClass("nonner");
    }
};

var changeTestResultVisibility = function() {
    var popup = fillcmdsPopup("submitChangeTestResultVisibility", "changeResultTestVisibilitySample");
    var status = $(this).data("resultVisibility");
    var statusMessage = $(this).data("resultVisibilityMessage");
    if (status === "HIDDEN") {
        setvChooseValue(popup.find(".vChoose"), status);
        popup.find("textarea").val(statusMessage);
        popup.find(".resultVisibilityMessageDiv").removeClass("nonner");
    }
};
var submitChangeTestResultVisibility = function() {
    var popup = $(this).closest("#cmdsPopup");
    var params = getFormValues(popup);
    var testId = $(".testPageDetails").data("testId");
    params['entity.id'] = testId;
    params['entity.type'] = "CMDSTEST";
    vReq.post("/qrTests/changeTestResultVisibility",
            params, function(result) {
                var displayText = "SHOW";
                if (params.resultVisibility === "HIDDEN") {
                    displayText = "HIDE";
                }
                // $(".testResultShowText").text(displayText);
                $(".changeTestResultVisibility").data("resultVisibility", params.resultVisibility);
                $(".changeTestResultVisibility").data("resultVisibilityMessage", params.resultVisibilityMessage);
                if(result.result.resultVisibility === 'VISIBLE'){
                    $(".testResultShowText").text(displayText).removeClass("redColor").addClass("greenColor");
                }
                else{
                    $(".testResultShowText").text(displayText).removeClass("greenColor").addClass("redColor");
                }
                closecmdsPopup();
            });
};

var testResultVisibilityEvent = "click.resultVisibility";
$("body").off(testResultVisibilityEvent)
        .on(testResultVisibilityEvent, ".changeTestResultVisibility", changeTestResultVisibility)
        .on(testResultVisibilityEvent, ".submitChangeTestResultVisibility", submitChangeTestResultVisibility)

var showResourcesPopup = function(resourceType,selectMultiple,folderId,selectBtnId,cbFn) {
        var popup = getcmdsPopupBody(700);
        popup.addClass("videoResourcePopup");
        popup.html("<div class='userMessage'>Fetching Content...</div>");
	resourceType = resourceType ? resourceType : "ALL";
	selectMultiple = selectMultiple ? true : false;
        var params = {
		selectMultiple : selectMultiple,
		// resourceType : resourceType,
		// includes : resourceType,
		// "includes[0]" : resourceType
	};
	// var mcExtParams = {
	// 	includes : resourceType,
	// 	"includes[0]" : resourceType
	// };
	// params.mcExtParams = serializeJson.encode(mcExtParams);
	if(folderId){
		params.folderId = folderId;
	}
        startLoader();
	var CLICK = "click.popupResources";
        vReq.get("/qrresources/popupResources", params, function(data) {
            stopLoader();
            popup.html(data);
	    popup = popup.find("#popupResources");
	    popup.off(CLICK);
	    var btn = popup.find(".addFromPopupResource");
	    btn.on(CLICK,function(){
		onSelect();
	    });
	    if(selectBtnId){
		btn.attr("id",selectBtnId);
	    }
	    if(!selectMultiple){
		doSingleSelect();
	    }
        });
	function doSingleSelect(){
		popup.find(".cmdsAllgCBox").addClass("hider");
		popup.on(CLICK,".gCBox",function(){
			var $this = $(this);
			setTimeout(function(){
			   var popCheckBoxes = popup.find(".gCBox:not(.gCBoxChecked,.cmdsAllgCBox)");
			   if($this.hasClass("gCBoxChecked")){
				popCheckBoxes.addClass("hider");
			   }else{
				popCheckBoxes.removeClass("hider");
			   }
			},100);
		});
	};
	function onSelect(){
        	var checkStatus = checkedEntities.init();
		var entities = checkStatus.srcEntities;
		if(cbFn && entities && entities.length>0){
			try{
				cbFn(entities,checkStatus.entityIds);
			}catch(err){
				putConsoleError(err);
			}
		}
		popup = undefined;
		closecmdsPopup();
	};
};
$(document).on('click', '.openInstAboutApp', function(e){
        var popup = showVPopup(0.6);
	bigLoader(popup);
	vReq.get("/Application/getAboutAppPopup",{},function(data){
		popup.html(data);
	});
    	insideOutClick();
});
