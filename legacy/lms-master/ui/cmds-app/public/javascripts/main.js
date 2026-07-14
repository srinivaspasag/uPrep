//overridden methods to enable the js in web-app to be used here in cmds
var getCommonPopupBody = function(width, height, allowToBeChild, popup) {
    if (!popup) {
        popup = $("#cmdsPopup");
    }
    popup.removeAttr("style").removeClass();
    popup.show().css("min-height", height + "px").width(width);
    var cmdsBlackOut = popup.siblings("#cmdsBlackOut").show();
    cmdsBlackOut.css("z-index", popup.css("z-index") - 1);
    var top = (120 + $(window).scrollTop()) + "px";
    popup.css("top", top);
    popup.find(".cmdsPopupBtns").addClass("nonner");
    return popup.children(".cmdsPopupBody");
};
var cancelCommonPopup = function() {
    closecmdsPopup();
};
$("body").on("click", ".cancelCommonPopup", cancelCommonPopup);
$("body").on("click", ".closecmdsPopupAlt", function() {
    closecmdsPopup($("#cmdsPopupAlt"));
});
//$("body").on("click","#cmdsBlackOut",cancelCommonPopup);

var getcmdsPopupBody = function(width, height, btns, cmdsPopupClass) {
    return makecmdsPopupBody($("#cmdsPopup"), width, height, btns, cmdsPopupClass);
};
var getcmdsPopupBodyAlt = function(width, height, btns, cmdsPopupClass) {
    return makecmdsPopupBody($("#cmdsPopupAlt"), width, height, btns, cmdsPopupClass);
};
var makecmdsPopupBody = function(popup, width, height, btns, cmdsPopupClass) {
    popup.height(height);
    var btnsDiv = popup.find(".cmdsPopupBtns");
    if (btns) {
        var cancelClass = btns.cancelClass || "closecmdsPopup";
        var btnsHTML = "";
        if (btns.submitClass)
            btnsHTML = '<div class="gBlueButton margRight5 ' + btns.submitClass + '">Submit</div>';
        btnsHTML += '<div class="gButton ' + cancelClass + '">Cancel</div>';
        btnsDiv.removeClass("nonner").html(btnsHTML);
    } else
        btnsDiv.addClass("nonner").html("");
    var popupBody = getCommonPopupBody(width, height,false,popup);
    popup.find(".cmdsPopupBtns,.closecmdsPopup,.closePopupDiv").removeClass("nonner");
    if (cmdsPopupClass) {
        popup.addClass(cmdsPopupClass);
    }
    return popupBody;
};
var cmdsBlackOut = function(show) {
    if (show) {
        $("#cmdsBlackOut").show();
    } else {
        $("#cmdsBlackOut").hide();
    }
}
var closecmdsPopup = function(popup) {
    if (!popup) {
        popup = $("#cmdsPopup");
    }
    popup.hide();
    var closeEl = popup.find(".closecmdsPopup");
    popup.find(".cmdsPopupBtns").addClass("nonner").html("");
    popup.find(".cmdsPopupError").addClass("nonner");
    popup.siblings("#cmdsBlackOut").hide();
    popup.removeClass("cmdsErrorPopup");
    popup.find(".cmdsPopupBody").html("");
    var cbfn = closeEl.data("closecmdsPopupcbfn");
    if (cbfn) {
        cbfn();
        delete closeEl.data().closecmdsPopupcbfn;
    }
};
var showcmdsPopupError = function(popup, err) {
    popup = popup || $("#cmdsPopup");
    err = err || "Fields maked with * are compulsory";
    popup.find(".cmdsPopupError").removeClass("nonner").html(err);
}
var hidecmdsPopupError = function(popup) {
    popup = popup || $("#cmdsPopup");
    popup.find(".cmdsPopupError").addClass("nonner").html("");
}
var showcmdsError = function(err, cancelText, cbFn) {
    showCmdsMsgBox(err, cancelText, "ERROR", cbFn);
};
var showCmdsMsgBox = function(err, cancelText, typeOfBox, cbFn) {
    var popup = showVMsgBox(err, cancelText, typeOfBox, cbFn);
    return popup;
};
var hidecmdsError = function() {
    hideCmdsMsgBox();
};
var hideCmdsMsgBox = function() {
    hideVMsgBox(true);
    var popup = $("#cmdsPopup");
    var cmdsBlackOut = popup.siblings("#cmdsBlackOut");
    if (popup.css("display") == "none") {
        cmdsBlackOut.hide();
    } else {
        cmdsBlackOut.css("z-index", popup.css("z-index") - 1);
    }
};
$("body").on("click", ".closecmdsMessagePopup", hidecmdsError);
//overriding uicommons fn
var showError = function(err, cbFn) {
    showCmdsMsgBox(err, "OK", "ERROR", cbFn);
};
var showMessage = function(message, cbFn) {
    showCmdsMsgBox(message, "OK", "SUCCESS", cbFn);
};



var fillcmdsPopup = function(submitClass, cloneVar, heading) {
    var popup = getcmdsPopupBody(null, null, {submitClass: submitClass});
    popup.html(window[cloneVar].children().clone(true));
    popup.find("input[type=text]").first().focus();
    if (heading) {
        popup.find(".cmdsPopupHead").html(heading);
    }
    return popup;
};
var getPopupDiv = function($this) {
    return $this.closest("#cmdsPopup");
};
var inlineLoader = function(target) {
    target.html("<img src='/public/images/loading.gif' alt='loading..' />");
};
$("body").on("click", ".closecmdsPopup,.closePopup", function(){
    closecmdsPopup();
});


var fixContentSec = function() {
    var fixedSec = $('#fixedSec');
    var windowHeight = $(window).height();
    if (fixedSec.length > 0) {
        $("body").css("overflow", "auto");
        if (fixedSec.length > 0) {
            var h = windowHeight - fixedSec.offset().top;
            fixedSec.height(h);
        }
    }
    var fixedLeftSecPortion = $("#fixedLeftSecPortion");
    if (fixedLeftSecPortion.length > 0) {
        fixedLeftSecPortion.height(windowHeight - fixedLeftSecPortion.offset().top);
    }
};
var fixLeftSec = function() {
    var windowHeight = $(window).height();
    var fixedLeftSecPortion = $("#leftSection");
    if (fixedLeftSecPortion.length > 0) {
        fixedLeftSecPortion.height(windowHeight - fixedLeftSecPortion.offset().top);
    }
};
var cmdsCBoxesChecked = [];
var changeActiveClass = function($this, activeClass) {
    $this.addClass(activeClass).siblings().removeClass(activeClass);
};
$("body").on('click', "a.cmdsaPush", function() {
    pushHistory(null, null, this.href);
    return false;
});
var initmcWidgetforCMDS = function(mcWidget, urlStr, params, loadContent, setLoadMoreOnly, cbAfterLoad) {
    var size = params.size || 50;
    var fn = function() {
        try {
            //adjustment to put a callback always to remove cmdsOptions
            var cbFormcWidget = function(data, xhr, urlParams, mcWidget) {
                resetcmdsCBoxes(mcWidget);
                if (cbAfterLoad) {
                    cbAfterLoad();
                }
            };
            manageContent.init(mcWidget, {params: params, urlStr: urlStr, initialSize: size},
            loadContent, setLoadMoreOnly, cbFormcWidget);
            //[params,urlstr,initialsize,moresize]moresize made equal to initial size
        } catch (err) {
            putConsoleLogs(err);
        }
    };
    checkmcWidgetFile(fn);
};
var setvChooseValue = function(vChoose, value) {
    var vChooseOpts = vChoose.find(".vChooseOpt");
    var found = false, htmlContent = "";
    for (var k = 0, l = vChooseOpts.length; k < l; k++) {
        var thisOpt = vChooseOpts.eq(k);
        if (value === thisOpt.data("value")) {
            htmlContent = thisOpt.html();
            found = true;
            break;
        }
    }
    if (found) {
        vChoose.data("value", value).find(".vChooseHead").html(htmlContent);
    }
};
var updatemcWidgetParamHolders = function(mcWidget, urlParams, target) {
    var cmdsTableSortDiv = mcWidget.find(".cmdsTableSortDiv");
    $.each(urlParams, function(key, val) {
        //courseid is updated in the above code.
        switch (key) {
            case "orderBy":
                {
                    setvChooseValue(cmdsTableSortDiv.children(".vChooseAlpha"), val);
                    break;
                }
            case "includes":
                {
                    setvChooseValue(cmdsTableSortDiv.children(".vChooseContentTypes"), val);
                    if (val === "CMDSQUESTION") {
                        var optsType;
                        if (target === "RESOURCES") {
                            optsType = "normalQuesns";
                        } else if (target === "LIBRARY") {
                            optsType = "library";
                        }
                        mcWidget.find(".cmdsTableCheckedOpts").data("optsType", optsType);
                    }
                    break;
                }
            case "query":
                {
                    mcWidget.find(".mcSearchDivInput").val(val);
                    break;
                }
            case "targetProfile":
                {
                    var peoplevChoose = mcWidget.find(".peopleProfilevChoose");
                    setvChooseValue(peoplevChoose, val);
                    changeAddPeopleEls(peoplevChoose, val);
                    break;
                }
        }
    });
};
var aftermcWidgetContentLoaded = function(mcWidget) {
    pushNewUrlParams(getFinalUrlParamsForPage(mcWidget));
    fixContentSec();
};
var getFinalUrlParamsForPage = function(mcWidget) {
    var pageUrlParams = mcWidget.data("pageUrlParams");
    var mcWidgetParams = mcWidget.data().params;
    var start = mcWidget.find(".LMArrowsDiv").data("urlParams").start || 0;
    var finalUrlParams = {};
    $.each(pageUrlParams, function(key, val) {
        if (mcWidgetParams[key] && key !== "start") {
            finalUrlParams[key] = mcWidgetParams[key];
        } else if (key === "start") {
            finalUrlParams[key] = start;
        } else if (pageUrlParams[key] !== "") {
            finalUrlParams[key] = pageUrlParams[key];
        }
    });
    return finalUrlParams;
};
var changeAddPeopleEls = function(vChoose, profile) {
    var profileText = vChoose.find(".vChooseOptActive").data("text");
    profileText = profileText ? profileText : toCamelCase(profile);
    profileText = profileText == "Offline_user" ? "Tablet Profile" : profileText;
    var ph = "Search " + profileText + "s";
    var btnText = "+ Add " + profileText + "s";
    $("#mcWidgetInput").attr("placeholder", ph);
    $("#addPeopleBtn").text(btnText).data("targetProfile", profile);
};
window["toCamelCase"] = function(str) {
    if (!str)
        return "";
    var strOut = str.charAt(0).toUpperCase();
    var strOut2 = str.substr(1).toLowerCase();
    strOut2 = strOut2 ? strOut2.toLowerCase() : "";
    strOut = strOut + strOut2;
    return strOut;
};



var memberInfoPojo = {lastLoadedTime: 0, data: {}, progList: [], loaded: false};
var prepareMemberInfoPojo = function(memberInfo) {
    var data = {}, progList = [];
    var progs = memberInfo.mappings.programs;
    var tagger = makeHTMLTag;
    var progsHolder = tagger("div");
    for (var k = 0; k < progs.length; k++) {
        var prog = progs[k], centers = prog.centers;
        var progId = prog.id;
        data[progId] = prog;
        for (var c = 0; c < centers.length; c++) {
            var center = centers[c], sections = center.sections;
            data[progId + "_" + center.id] = center;
            for (var s = 0; s < sections.length; s++) {
                var section = sections[s];
                data[section.id] = section;
            }
        }
        progList.push(prog);
        progsHolder.append(tagger("a", {"class": "cmdsaPush hiddenMenuItem getProgramPage",
            href: vcmdsUrls.PROGRAM(progId)}).data("programId", progId).text(prog.name));
    }
    //preparing menuitems
    var progMenuItems = $("#topBar").find(".progMenuItems");
    progMenuItems.html(progsHolder.children());
    var targetDiv = progMenuItems.closest(".cmdsHiddenMenuItemDiv");
    if (progs.length === 0 && cmdsProfile !== "MANAGER") {
        targetDiv.addClass("nonner");
    } else {
        targetDiv.removeClass("nonner");
    }
    memberInfoPojo = {lastLoadedTime: new Date().getTime(), data: data, progList: progList};
};
var getMemberInfoPojo = function(reloadNow) {
    var time = memberInfoPojo.lastLoadedTime;
    var reqSent = memberInfoPojo.reqSent;
    var nowTime = new Date().getTime();
    //put a buffer for 1/2hr
    if ((nowTime - time > 1800000 || reloadNow) && !reqSent) {
        memberInfoPojo.reqSent = true;
        $.get("/qrpeople/getMyMemberInfo", function(data) {
            prepareMemberInfoPojo(data.result.info);
            delete memberInfoPojo.reqSent;
            memberInfoPojo.loaded = true;
            if (memberInfoPojocbfn) {
                memberInfoPojocbfn();
                memberInfoPojocbfn = null;
            }
        });
    }
    return memberInfoPojo;
};
var memberInfoPojocbfn;
var setUserOrgs = function() {
    $.get("/widgets/setuserorgs", {orgId: cmdsOrgId}, function(data) {
        $(data).insertBefore($("#cmdsLogout"));
    });
};


var getMyAcadEntities = {
    PROGRAM: function() {
        var progList = memberInfoPojo.progList;
        var arr = [];
        for (var k = 0; k < progList.length; k++) {
            arr.push(progList[k].id);
        }
        return arr;
    },
    CENTER: function(params) {
        var arr = [];
        try {
            var programId = params.programId;
            var centers = memberInfoPojo.data[programId].centers;
            for (var k = 0; k < centers.length; k++) {
                arr.push(centers[k].id);
            }
        } catch (err) {
        }
        return arr;
    },
    SECTION: function(params) {
        var arr = [];
        try {
            var programId = params.programId;
            var centerId = params.centerId;
            var sections = memberInfoPojo.data[programId + "_" + centerId].sections;
            for (var k = 0; k < sections.length; k++) {
                arr.push(sections[k].id);
            }
        } catch (err) {
        }
        return arr;
    }
};





var putActiveMenuItem = function(targetTabEl) {
    var activeTabClass = "cmdsActiveMenuItem";
    $("#topBar").find(".cmdsActiveMenuItem").removeClass(activeTabClass);
    if (targetTabEl) {
        targetTabEl.addClass(activeTabClass);
    }
};
