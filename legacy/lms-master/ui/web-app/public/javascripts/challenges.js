var challengeEvent = "click.challengeEvent";
var channelParams = {};
vChallenges = new (function($) {
    var challengePage, cpChallengesContent;
    this.init = function(params) {
        var lmhandlerParams = $.extend({status: "ACTIVE", orderBy: "timeCreated",
            resultType: "ALL", target: "ACTIVE_CHALLENGES"}, channelParams);

        challengePage = $("#challengePage");
        cpChallengesContent = $("#CPChallengesContent");

        challengePage
                .on("click", ".showActiveChallenges", showActiveChallenges)
                .on("click", ".showClosedChallenges", showClosedChallenges)
                .on("click", ".showAttemptedChallenges", showAttemptedChallenges)

        cpChallengesContent.children(".LMHandlerDiv").data("urlStr", "/challenges/challengeItems").data("size", 10)
                .data("allParams", lmhandlerParams);


        activateBurstTimers();

        populateLeaderBoard();
        populateStats();
        try {
            if (institute) {
                institute.init();
            }
        } catch (err) {
        }
    };

    var populateStats = function() {
        var rightSec = $("#CPRightSec");
        var multiplierEnums = {"SINGLE": 1, "DOUBLE": 2, "TRIPLE": 3};
        $.get("/challenges/challengeStats", channelParams, function(data) {
            var stats = data.result;
            rightSec.find(".CPRSStrikeRate").text(stats.strikeRate + "%");
            rightSec.find(".CPRSMultiplierCount").text(multiplierEnums[stats.multiplier]);
            rightSec.find(".CPRSMyPoints").text(stats.points);
            var hatDiv = rightSec.find(".CPRSHatDiv");
            if (stats.hintsCountMap) {
                $.each(stats.hintsCountMap, function(hintNum, count) {
                    hatDiv.eq(hintNum).find(".CPRSHatCount").text(count);
                });
            }
        });
    };
    var populateLeaderBoard = function() {
        var params = $.extend({target: "CHALLENGES_PAGE", start: 0, size: 5, rankType: "WEEKLY"}, channelParams);
        $.get("/challenges/globalLeaderBoard", params, function(data) {
            $("#CPRSGlobalLeaderBoard").html(data);
        });
    };


    var showActiveChallenges = function(e, activeChallengesTab) {
        var $this = activeChallengesTab || $(this);
        reOrderCPTabs($this, "activeChallenges");
    };
    var showClosedChallenges = function() {
        reOrderCPTabs($(this), "closedChallenges");
    };
    var showAttemptedChallenges = function() {
        reOrderCPTabs($(this), "attemptedChallenges");
    };

    var cpTabCbFns = {
        ACTIVE_CHALLENGES: function(params) {
            cpChallengesContent.children(".LMHandlerDiv").data("urlStr", "/challenges/challengeItems").data("size", 10)
                    .data("allParams", $.extend({status: "ACTIVE", resultType: "ALL", orderBy: "timeCreated",
                target: "ACTIVE_CHALLENGES"}, channelParams));
            var mainSec = $("#CPMainSec");
            updateChallengeCounts(mainSec, params.stats);
            activateBurstTimers();
        },
        CLOSED_CHALLENGES: function(params) {
            var LMHandlerDiv = cpChallengesContent.children(".LMHandlerDiv");
            LMHandlerDiv.data("urlStr", "/challenges/challengeItems").data("size", 10)
                    .data("allParams", $.extend({status: "ENDED", resultType: "ALL", orderBy: "timeCreated",
                target: "CLOSED_CHALLENGES"}, channelParams));
            var mainSec = $("#CPMainSec");
            updateChallengeCounts(mainSec, params.stats);
            closedChallengesLoaded(LMHandlerDiv);
        },
        ATTEMPTED_CHALLENGES: function(params) {
            var mainSec = $("#CPMainSec");
            var LMHandlerDiv = cpChallengesContent.children(".LMHandlerDiv");
            var defaults = [{subTarget: "ACTIVE_CHALLENGES", status: "ACTIVE"},
                {subTarget: "CLOSED_CHALLENGES", status: "ENDED"}];
            LMHandlerDiv.each(function(i) {
                LMHandlerDiv.eq(i).data("urlStr", "/challenges/challengeItems").data("size", 5)
                        .data("allParams", $.extend({status: defaults[i].status, orderBy: "timeCreated", target: "ATTEMPTED_CHALLENGES",
                    subTarget: defaults[i].subTarget, resultType: "ATTEMPTED"}, channelParams));
            });
            updateChallengeCounts(mainSec, params.stats);
            activateBurstTimers();
        }
    };
    this.cpTabCbFns = cpTabCbFns;

    var reOrderCPTabs = function($this, urlStrip) {
        var cp = $this.closest("#challengePage");
        cp.removeClass().addClass(urlStrip);
        resetCPSelects(cp.find(".CPSorters"));
        showTopLoader();
        var allParams = $.extend({start: 0, size: 10, orderBy: "timeCreated"}, channelParams);
        $.get("/challenges/" + urlStrip, allParams, function(data, s, xhr) {
            hideTopLoader();
            cp.find("#CPChallengesContent").html(data);
            $this.addClass("activeCPTab").siblings().removeClass("activeCPTab");
            //clickStream.recordElem($this,"CLICK",allParams,xhr);
        });
    };
    var resetCPSelects = function(cpSorters) {
        var defaults = [{val: "timeCreated", text: i18nJS("MOST_RECENT")}, {val: "-1", text: i18nJS("ALL_SUBJECTS")},
            {val: "-1", text: i18nJS("ALL_TYPES")}, {val: "-1", text: i18nJS("ALL_LEVELS")}]
        cpSorters.children(".CPSelect").each(function(i) {
            $(this).find(".vChooseHead").text(defaults[i].text);
            $(this).data("value", defaults[i].val);
        });
    };
    var updateChallengeCounts = function(mainSec, stats) {
        var cpTabs = mainSec.children(".CPTabs");
        cpTabs.find(".activeChallengesCount").text(stats.active);
        cpTabs.find(".closedChallengesCount").text(stats.closed);
        cpTabs.find(".attemptedChallengesCount").text(stats.attempted);
        var cpTotalCount = mainSec.children(".CPTotalCount");
        if (stats.nowActive) {
            cpTotalCount.find(".CPChallengeCount").text(stats.nowActive);
        }
        if (stats.pointsForGrabs) {
            cpTotalCount.find(".CPTotalPointsCount").text(stats.pointsForGrabs);
        }
    };
    var closedChallengesLoaded = function(LMHandlerDiv) {
        var orderBy = LMHandlerDiv.data("allParams").orderBy;
        if (orderBy === "timeCreated") {
            LMHandlerDiv.children(".challenge").each(function() {
                var dated = $(this).data("dated");
                if (LMHandlerDiv.children(".challenge_" + dated).length == 0) {
                    $(this).addClass("challenge_" + dated)
                            .prepend("<div class='boldy big20 margBot20'>" + dated + "</div>");
                }
            });
        }
    };
    this.closedChallengesLoaded=closedChallengesLoaded;



    this.onChannelChange = function(vChoose, value) {
        if (value != "-1") {
            channelParams.channelId = value;
        } else {
            channelParams.channelId = undefined;
        }
        var tab = challengePage.find(".showActiveChallenges");
        showActiveChallenges(null, tab);
        populateLeaderBoard();
        populateStats();
    };
})(jQuery);


var onChannelChange = vChallenges.onChannelChange;








var eachminAngle = 360 / 1440;
var takeChallengeEachSecAngle;
var takeChallengeTimerDiv;
function startBurstTimers() {
    var challenges = $("#CPChallengesContent").children(".LMHandlerDiv").children(".challengeActive,.challengeAttemptedActive");
    challenges.each(function() {
        var timerDiv = $(this).find(".challengeTimer");
        var timeText = timerDiv.find(".challengeTimerTime");
        var timeLeft = (parseInt(timerDiv.data("burstTime"))
                - (getCurrentTime())) / 1000;
        var angle = 360 - ((eachminAngle * timeLeft) / 60);
        if (timeLeft && timeLeft > 0) {
            timer_circle.fill(angle, timerDiv.find(".timerCircle"));
            if (timeLeft <= 60) {
                timeText.html("<div class='boldy relativePos' \n\
            style='top:-10px;line-height:13px;'>"+i18nJS("FEW_SECONDS_LEFT")+"</div>");
            }
            else {
                var timeObj = getTimeObj(timeLeft * 1000);
                var timeStr = i18nJS("ERROR_BR_REFRESH_PAGE");
                if (!timeObj.hasError) {
                    timeStr = timeObj.hr + ":" + timeObj.min;
                }
                timeText.html("<div class='boldy big16'>" + timeStr + "</div> "+i18nJS("HRS_LEFT"));
            }
        }
        else if (timeLeft <= 0) {
            timeText.html("<div class='challengeClosedText' style='font-size:12px;'>"+i18nJS("RESULTS_PENDING")+"</div>");
            var takeButton = $(this).find(".takeChallenge");
            if (takeButton.length > 0) {
                takeButton.toggle("openQuesPage takeChallenge").attr("href", "/question/" + takeButton.data("qid"));
                takeButton.data("element", "OPEN_QUESTION").data("source", "CHALLENGES_PAGE").text(i18nJS("VIEW_CHALLENGE"));
            }
            $(this).find(".openQuesPage").removeClass("nonner");
            timer_circle.fill(360, timerDiv.find(".timerCircle"));
        }
    });
}
function activateBurstTimers() {
    startBurstTimers();
    burstTimer = setInterval(function() {
        startBurstTimers();
    }, 60000);
}





//sorting
function onCPSelectChange(vChoose, value) {
    loadSortedChallenges(vChoose.closest(".CPSorters"), vChoose);
}
function loadSortedChallenges(CPSorters, sourceEl) {
    var params = {};
    var LMHandlerDiv = CPSorters.siblings("#CPChallengesContent").children(".LMHandlerDiv");
    LMHandlerDiv.each(function(i) {
        var targetLMHandlerDiv = LMHandlerDiv.eq(i);
        if (targetLMHandlerDiv.data("allParams"))
            params = targetLMHandlerDiv.data("allParams");
        $.extend(params, channelParams);
        CPSorters.children(".CPSelect").each(function() {
            if ($(this).data("value") != "-1")
                params[$(this).data("sortType")] = $(this).data("value");
            else {
                delete params[$(this).data("sortType")];
            }
        });
        targetLMHandlerDiv.data("urlStr", "/challenges/challengeItems").data("size", 10).data("allParams", params);
        params.start = 0;
        params.size = 10;
        showTopLoader();
        $.get(targetLMHandlerDiv.data("urlStr"), params, function(data, s, xhr) {
            hideTopLoader();
            targetLMHandlerDiv.html(data);
            var callback = targetLMHandlerDiv.data("callback");
            if (LMHandlerDivCallbackFns[callback]) {
                try {
                    LMHandlerDivCallbackFns[callback](LMHandlerDiv);
                } catch (err) {
                }
            }
            //clickStream.recordElem(sourceEl,"CLICK",params,xhr);
        });
    });
}
LMHandlerDivCallbackFns["ACTIVE_CHALLENGES"] = function() {
    activateBurstTimers();
};
LMHandlerDivCallbackFns["CLOSED_CHALLENGES"] = function(LMHandlerDiv) {
    vChallenges.closedChallengesLoaded(LMHandlerDiv);
};







var currentChallengeBurstsIn = 0;
var currentChallenge;

var takeChallenge = function() {
    //var popup = getCommonPopupBody(725);
    var popup = showVPopup();
    var msg = vedantuClient.verifyClientForTest("challenge");
    if (msg) {
        popup.html("<div class='redTextColor big16 centerText' style='padding:20px;'>" + msg + "</div>");
        return;
    }
    popup.html($("#challengePageHolder")
            .find(".challengeGuidelinesHolder").parent().html());
    popup.find("#approveChallenge").data($(this).data());
    currentChallenge = $(this);
    takeChallengeChalId = $(this).data("challengeId");
    popup.off("click.approveChallenge").on("click.approveChallenge", "#approveChallenge", startChallengeFn);
    initGuidelinesHolder(popup);
};




var takeChallengeChalId;
var startChallengeFn = function() {
    var sourceEl = $(currentChallenge), mainSec = $(currentChallenge).closest("#CPMainSec"), chal = sourceEl.closest(".challenge");
    //cancelAllCommonPopup();
    closeVPopup();
    if (prepareChallengeFullScreen()) {
        var holder = $("#takeChallengeHolder");
        var chalId = $(this).data("challengeId");
        var params = {id: takeChallengeChalId};
        $.get("/challenges/takeChallenge", params, function(data) {
            holder.html(data);
            $.get("/challenges/startChallenge", params, function(quesData, s, xhr) {
                holder.find(".challengeQues").html(quesData)
                        .on("click", "img", function() {
                    showImagePreview($(this).get(0).outerHTML);
                });
                loadMJEqns(holder.get(0));
                sourceEl.toggle("openQuesPage takeChallenge").attr("href", "/question/" + sourceEl.data("qid"));
                sourceEl.data("element", "OPEN_QUESTION").data("source", "CHALLENGES_PAGE").text("View Challenge");
                sourceEl.addClass("nonner");
                try {
                    var timerDiv = chal.find(".challengeTimer");
                    currentChallengeBurstsIn = timerDiv.data("burstTime");
                } catch (err) {
                }
                //clickStream.recordElem(sourceEl,"CLICK",params,xhr);
            });
        });
    }
    else {
        showError(i18nJS("BROWSER_SPECIFIC_SUPPORT_DEPENDENCY"));
    }
};
function prepareChallengeTimer(holder) {
    var timerDiv = holder.find(".TCPTimerDiv");
    var timerData = timerDiv.data();
    console.log(getCurrentTime() + " started at:");
    var timeLeft = parseInt(timerData.timeLimit)
            - (getCurrentTime() - parseInt(timerData.startTime));
    if (timeLeft <= 0)
        return false;
    var timeToBurst = timerData.burstTime - (getCurrentTime());
    if (timeToBurst <= 0)
        return false;
    else if (timeToBurst < timeLeft) {
        timeLeft = timeToBurst;
    }
    timeLeft = timeLeft / 1000;
    timerDiv.data("actualTimeLeft", timeLeft++);
    takeChallengeEachSecAngle = 360 / timeLeft;
    takeChallengeTimerDiv = $("#takeChallengeHolder").find(".TCPTimerDiv");
    startChallengeTimer();
    return true;
}
function startChallengeTimer() {
    takeChallengeTimerUpdate();
    challengeTimer = setInterval(function() {
        takeChallengeTimerUpdate();
    }, 1000);
}
function takeChallengeTimerUpdate() {
    var timeLeftNow = takeChallengeTimerDiv.data("actualTimeLeft");
    //timeLeftNow--;
    takeChallengeTimerDiv.data("actualTimeLeft", --timeLeftNow);
    var el = makeHTMLTag('div');
    el.html(getDuration(timeLeftNow));
    var angle = 360 - (takeChallengeEachSecAngle * timeLeftNow);
    timer_circle.fill(angle, takeChallengeTimerDiv.find(".timerCircle"));
    takeChallengeTimerDiv.find(".TCPTimerText").text(el.text());
    if (timeLeftNow <= 0) {
        putConsoleLogs("exiting");
        removeChallengeFullScreen();
        showError("Sorry, Time alloted for the challenge has expired");
    }
}
function prepareChallengeFullScreen() {
    var ret = $.goFullScreen(challengeFullScreenExit);
    if (ret) {
        $("#topBarHolder,#contentSectionHolder,#searchContentHeader,#chatDivsWrapper,.otherUserUseTopBar").addClass("nonner");
        try {
            $(document).bind("contextmenu", disableRightClick);
            if (playRunMode == "PROD") {
                $(document).bind("contextmenu", disableRightClick);
//			$(window).bind("blur",clngWindowOutFocused);
            }
        } catch (err) {
        }
    }
    return true;
}
function disableRightClick(e) {
    if (e)
        e.preventDefault();
    return false;
}
function challengeFullScreenExit(e, onFullScreen) {
    if (onFullScreen == false) {
        removeChallengeFullScreen();
    }
}
function clngWindowOutFocused() {
    if ($("#takeChallengeHolder").get(0)) {
        removeChallengeFullScreen();
    } else {
        $(window).unbind("blur", clngWindowOutFocused);
    }
}
function removeChallengeFullScreen() {
    $.exitFullScreen();
    try {
        clearInterval(challengeTimer);
    } catch (err) {
    }
    // handle escape button case with a popup,also stay or leave paeg
    $("#topBarHolder,#contentSectionHolder,#searchContentHeader,#chatDivsWrapper,.otherUserUseTopBar").removeClass("nonner");
    $(document).unbind("contextmenu", disableRightClick);
    $(window).unbind("blur", clngWindowOutFocused);
    $(window).unbind("blur", clngWindowOutFocused);
    $("#takeChallengeHolder").html("");
}

var tcpHintFn = function() {
    var TCP = $(this).closest("#takeChallengePage"), $this = $(this), hintBtn = $this.closest(".TCPHintDiv");
    var count = $(this).closest(".TCPHintDiv").index();
    var targetDiv = TCP.find("#TCPHintSec").children(".TCPHint").eq(count);
    targetDiv.removeClass("nonner");
    if (targetDiv.find(".TCPHintContent").html() == "") {
        var sourceEl = $(this);
        var params = {token: TCP.find(".challengeQuesContent").data("token")};
        $.get("/challenges/challengeHint", params, function(data, s, xhr) {
            targetDiv.find(".TCPHintContent").html(data.result.hint);
            hintBtn.toggleClass("TCPHintStriked TCPHintActive");
            hintBtn.next().addClass("TCPHintActive");
            TCP.find(".TCPHats .TCPHat").eq(count).html("<img src='/public/images/challenges/cross.png'>");
            var points = TCP.find(".TCPPointsCount").data("points");
            var pointsDeductible = parseInt($this.data("points"));
            if (pointsDeductible)
                points = points - pointsDeductible;
            TCP.find(".TCPPointsCount").text(points).data("points", points);
            TCP.find(".TCPHintsStatusPage").text((count + 1) + " hints taken");
            //clickStream.recordElem(sourceEl,"CLICK",params,xhr);
            loadMJEqns(targetDiv.get(0));
        });
    }
};


var submitChallengeAns = function(e) {
    var target = $(this).closest(".challengeQuesContent");
    var ansStatus = checkQuesAnsUtils.quesAnsSubmit[target.data("type")](target);
    if (ansStatus.result) {
        var params = {token: target.data("token"), answer: ansStatus.ans};
        removeChallengeFullScreen();
        console.log(new Date().getTime());
        $.post("/challenges/submitAnswer", params, function(data, s, xhr) {
            var timeLeftText = "";
            try {
                var timeLeft = getDuration((parseInt(currentChallengeBurstsIn)
                        - getCurrentTime()) / 1000);
                var el = makeHTMLTag('div');
                el.html(timeLeft);
                var t = el.text();
                if (t.length == 5)
                    t = "00:" + t;
                if (timeLeft)
                    timeLeftText = t + " hrs.";
            } catch (err) {
            }
            var error = "<div class='boldy greenColor'>"+i18nJS("TXT_SUBMITTED")+"</div>\n\
                <div class='margHalfTop'>" + i18nJS("CHAL_RESULT_TIME") + timeLeftText + "</div>";
            if (timeLeftText == "") {
                error = "<div class='boldy greenColor centry'>"+i18nJS("ANSWER_SUBMITTED_SUCCESSFULLY")+"</div>";
                showMessage(error);
            } else {
                showMessage(error);
            }
            if (data.errorCode != "") {
                if (data.errorCode == "CHALLENGE_ENDED") {
                    error = i18nJS("CHAL_TIME_ELAPSED");
                } else {
                    error = i18nJS("CHAL_ANS_SUBMIT_FAILED");
                }
                showError(error);
            }
            var challengeTitle=$("#challengeTitle").val();
            trackEventForGA("CHALLENGE","ATTEMPT",challengeTitle);
        });
    }
    else {
        showError(ansStatus.error);
    }
};
var cancelChallenge = function() {
    showError(i18nJS("CHAL_FORFEIT_ASK"), "confirmCancelChallenge");
};
var confirmCancelChallenge = function() {
    removeChallengeFullScreen();
};


$(document).on("click", ".thisWeekCLB", function() {
    changeCLBTabs($(this), "WEEKLY");
});
$(document).on("click", ".thisMonthCLB", function() {
    changeCLBTabs($(this), "MONTHLY");
});
$(document).on("click", ".overallCLB", function() {
    changeCLBTabs($(this), "OVERALL");
});
function changeCLBTabs($this, rankType) {
    var c = "activeSSCSubTab";
    $this.addClass(c).siblings().removeClass(c);
    $.get("/challenges/globalLeaderBoardItems", {target: "HOME_PAGE", start: 0, size: 5, rankType: rankType}, function(data) {
        $this.closest(".CLBHolder").find(".CLBListDiv").html(data);
    });
}


//popups for ?

var cprsHelp = function() {
    var popup = getCommonPopupBody($(this).attr("rel"));
    popup.html($(this).closest(".CPRSHelpHolder").find(".CPRSHelpDiv").html());
};

$(document).off(challengeEvent)
        .on(challengeEvent, ".takeChallenge", takeChallenge)
        .on(challengeEvent, ".TCPHintActive .TCPHintBtn", tcpHintFn)
        .on(challengeEvent, ".submitChallengeAns", submitChallengeAns)
        .on(challengeEvent, ".cancelChallenge", cancelChallenge)
        .on(challengeEvent, ".confirmCancelChallenge", confirmCancelChallenge)
        .on(challengeEvent, ".CPRSHelp", cprsHelp)
