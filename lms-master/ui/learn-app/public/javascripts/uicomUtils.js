$(function(){
    if(!firstLoadFnsFired){
        firstLoadFns();
        firstLoadFnsFired=true;
    }
});
var createCommonUIEl = function(source) {
    var targetVar = makeHTMLTag('div');
    targetVar.html(source.html());
    source.remove();
    if (img20Sample.children(".img20Wrapper").length > 0) {
        targetVar.find(".img20Holder").html(img20Sample.children().clone(true));
    }
    if (img30Sample.children(".img30Wrapper").length > 0) {
        targetVar.find(".img30Holder").html(img30Sample.children().clone(true));
    }
    return targetVar;
};
function firstLoadFns(){
    var uiSamplesDiv=$("#uiSamplesDiv");

    //for profile pic and name of the user
    img30Sample= makeHTMLTag('div');
    img30Sample.html(uiSamplesDiv.children("#img30Sample").html());
    uiSamplesDiv.children("#img30Sample").remove();

    img20Sample= makeHTMLTag('div');
    img20Sample.html(uiSamplesDiv.children("#img20Sample").html());
    uiSamplesDiv.children("#img20Sample").remove();

    //for comment widget
    commWidgetSample=createCommonUIEl(uiSamplesDiv.children("#commWidgetSample"));

    //FOR COMMENT
    commItemSample=createCommonUIEl(uiSamplesDiv.children("#commItemSample"));

    //FOR INST PAGE COMMENT
    instCommItemSample=createCommonUIEl(uiSamplesDiv.children("#instCommItemSample"));

    //FOR REPLY
    replyItemSample=createCommonUIEl(uiSamplesDiv.children("#replyItemSample"));

    //FOR REPLY Widget DIV
    replyWidgetSample=createCommonUIEl(uiSamplesDiv.children("#replyWidgetSample"));    
}
new function($) {
    $.fn.live = function(eventType, cbFn) {
        if (cbFn) {
            $(document).on(eventType, this.selector, cbFn);
        }
        return $(this);
    };
}(jQuery);
var trackEventForGA = function(category, action, label, nonInteraction) {
    if (nonInteraction !== false) {
        nonInteraction = true;
    }
    label = label || "";
    if (!category || !action) {
        return;
    }
    try {
        _gaq.push(['_trackEvent', category, action, label, 0, nonInteraction]);
    } catch (err) {
    }
};
var trackPageView = function() {
    try {
        _gaq.push(['_trackPageview']);
    } catch (err) {
    }
};
$.ajaxSetup({
    timeout: 60000,
    cache: true
});
var getCurrentTime = function() {
    return new Date().getTime() + serverSystemTimeDelta;
};
$.ajaxSetup({
    "complete": function(xhr, state) {
        try {
            clickstream.complete(xhr);
            //delete currentRequests[xhr.requestUrl];
        } catch (err) {
        }
    },
    "success": function(data, tStatus, xhr) {
        try {
            clickstream.complete(xhr);
        } catch (err) {
        }
    },
    "beforeSend": function(jqXHR, settings) {
        try {
            clickstream.beforeSend(jqXHR, settings);
        } catch (err) {
        }
        try {
            if (window["customAjaxSetup"] && customAjaxSetup.beforeSend) {
                customAjaxSetup.beforeSend(jqXHR, settings);
            }
        } catch (err) {
            console.error("Ajax Custom - " + err);
        }
    }
});

//Mathjax
var adjustMathJaxEqnsOptions = function(targetEl) {
    $(targetEl).find(".quesOptions").each(function() {
        var makeBlock = false;
        $(this).children(".quesOption").children(".quesOptionBody").find(".MathJax_Display")
                .each(function() {
                    $(this).closest(".quesOption").addClass("mathJaxPa");
                    if ($(this).outerWidth(true) > $(this).closest(".quesOptionBody").outerWidth(true)) {
                        makeBlock = true;
                    }
                });
        if (makeBlock) {
            $(this).addClass("quesOptionsExceeded");
        }
    });
    $(targetEl).find(".rteTextDiv").addClass("rteTextDivStyled");
    $(targetEl).find(".MathJax_Display,.MathJax").find("span").addClass("MathJax_DisplaySpan");
}
function loadMJEqns(el, cbfn) {
    try {
        if (el)
            MathJax.Hub.Queue(["Typeset", MathJax.Hub, el]);
        else
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
        MathJax.Hub.Queue(function() {
            adjustMathJaxEqnsOptions(el);
            if (cbfn)
                cbfn();
        });
    } catch (err) {
        putConsoleLogs("Mathjax not Working :: " + err);
    }
}


//load js and css files
var jsFileLogger = [];
var asyncFnsStore=[];
var fetchScripts = function(list) {
    for (var k = 0; k < list.length; k++) {
        var file = list[k], f = file.fname, callback = file.cb;
        if (jsFileLogger.indexOf(f) === -1) {
            jsFileLogger.push(f);
            asyncFnsStore.push({targetFile: f, callback: callback});
            var getScriptXhr = $.getScript("/public/javascripts/" + f)
                    .always(function(data, statusText, xhr) {
                        var fileName = xhr.jsFileName;
                        if (statusText === "success") {
                            executeAsyncFns(fileName);
                        } else {
                            // putConsoleLogs("Error in downloading:::: " + fileName + " : " + statusText);
                            var index = jsFileLogger.indexOf(fileName);
                            if (index > -1) {
                                jsFileLogger.splice(index, 1);
                            }
                            //now downloading it because it might turn into an infinite loop
                            //fetchScripts([{fname:fileName,cb:xhr.jsFileCallback}]);
                        }
                    });
            getScriptXhr.jsFileName = f;
            getScriptXhr.jsFileCallback = callback;
        } else if (callback) {
            try {
                callback();
            } catch (err) {
                putConsoleLogs(f + " File is not downloaded yet,storing the callback till it gets dowbloaded");
                asyncFnsStore.push({targetFile: f, callback: callback});
            }
        }
    }
};
var executeAsyncFns = function(targetFile) {
    var asyncTempStore = [];
    while (asyncFnsStore.length > 0) {
        var s = asyncFnsStore.shift();
        if (s.targetFile == targetFile) {
            var cbfn = s.callback;
            if (cbfn) {
                cbfn();
            }
        } else {
            asyncTempStore.push(s);
        }
    }
    asyncFnsStore = asyncTempStore;
};


//these checks are made when I want to download the js files only when I need them.
//for eg files tagging.js,rte.js are loaded when the page loads but the files below are loaded only 
//when they are required.
var checkmcWidgetFile = function(cbfn) {
    if (window["manageContent"]) {
        cbfn();
    } else {
        var f = "uicomWidgets/manageContent.js";
        fetchScripts([{fname: f, cb: cbfn}]);
    }
};
var checkmoveItemFile = function(cbfn) {
    if (window["vMoveItem"]) {
        cbfn();
    } else {
        var f = "uicomWidgets/moveItem.js";
        fetchScripts([{fname: f, cb: cbfn}]);
    }
};

//getHandler,postReqHandler
var vReq = new (function($) {
    var respCheckFn;
    this.get = function(url, params, successFn, errorFn, completeFn, showErrMessage) {
        respCheckFn = checkHTMLResp;
        return ajaxReqHandler("GET", url, params, successFn, errorFn, completeFn, showErrMessage);
    }
    this.post = function(url, params, successFn, errorFn, completeFn, showErrMessage) {
        respCheckFn = checkHTMLResp;
        return ajaxReqHandler("POST", url, params, successFn, errorFn, completeFn, showErrMessage);
    }
    var ajaxReqHandler = function(reqType, url, params, successFn,
            errorFn, completeFn, showErrMessage) {
        return $.ajax({
            type: reqType,
            url: url,
            data: params
        }).done(function(data, textStatus, xhr) {
            var dTypes = xhr.respDataTypes;
            if (dTypes.indexOf("html") > -1) {
                respCheckFn = checkHTMLResp;
            } else if (dTypes.indexOf("json") > -1) {
                respCheckFn = checkJSONResp;
            }
            if (respCheckFn(xhr, params, showErrMessage)) {
                if (successFn)
                    successFn(data, textStatus, xhr);
            } else if (errorFn) {
                errorFn(data, textStatus, xhr);
            }
        }).fail(function(xhr, textStatus, errorThrown) {
            reqFailHandler(xhr, textStatus);
            if (errorFn) {
                errorFn(xhr, textStatus, errorThrown);
            }
        }).always(function(data, textStatus, xhr) {
            if (completeFn) {
                completeFn(data, textStatus, xhr);
            }
        });
    }
})(jQuery);
var currentRequests = {};
$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
    var reqUrl = originalOptions.url;
    jqXHR.requestUrl = reqUrl;
//    if ( currentRequests[ reqUrl] ) {
//         currentRequests[ reqUrl ].abort();
//    }
//    currentRequests[ reqUrl ] = jqXHR;
    jqXHR.respDataTypes = options.dataTypes;
});
var checkHTMLResp = function(xhr, params, showErrMessage) {
    try {
        hideTopLoader();
        if (params)
            xhr.inputParams = params;
        var d = makeHTMLTag('div');
        d.html(filterJS(xhr.responseText));
        if (d.children("#respErrorCode").length > 0) {
            xhr.respErrorCode = d.children("#respErrorCode").val();
            if (!(showErrMessage === false)) {
                // showError(d.children("#respErrorMessage").val());
            }
            makeHTMLTag("div").html(xhr.responseText);
            return false;
        } else
            return true;
    } catch (err) {
        console.error(err);
    }
    return true;
}
var checkJSONResp = function(xhr, params, showErrMessage) {
    hideTopLoader();
    var finalResult = true;
    if (params)
        xhr.inputParams = params;
    try {
        var resp = $.parseJSON(xhr.responseText);
        if (resp.errorCode != "") {
            xhr.respErrorCode = resp.errorCode;
            if (!(showErrMessage === false)) {
                // showError(resp.errorMessage || resp.errorCode);
            }
            finalResult = false;
        } else {
            finalResult = true;
        }
    } catch (err) {
        finalResult = false;
    }
    return finalResult;
}
var COMMON_ERROR_MESSAGE = "Something went wrong.Refresh the Page and try again.";
var reqFailHandler = function(xhr, textStatus) {
    hideTopLoader();
    if (xhr.status === 404 || xhr.status === 500 || textStatus === "error") {
        // showError(COMMON_ERROR_MESSAGE);
    }
//   else if(xhr.status==0){
//        showError("Your internet connection is either too slow or lost");
//   }
};
function checkJSONResponse(data) {
    var isClean = true;
    if (!data.hasOwnProperty("result")) {
        // showError(COMMON_ERROR_MESSAGE);
        isClean = false;
    }
    else if (data.errorMessage != "") {
        // showError(data.errorMessage);
        isClean = false;
    }
    return isClean;
}

var previewImagesUtil = function(input, picTargetDivs, sampleImgEl) {
    var files = input.files, filesNo = files.length;
    var reader = new FileReader();
    sampleImgEl = sampleImgEl || makeHTMLTag("img");
    for (var i = 0; i < filesNo; i++) {
        var picTarget = picTargetDivs.eq(i);
        var imgEl = sampleImgEl.clone(true);
        reader.readAsDataURL(files[i]);
        reader.onload = function(e) {
            imgEl.attr("src", e.target.result);
            picTarget.html(imgEl);
        };
        reader.onerror = function(event) {
            imgEl.attr("src", "Some error Occured, Image cannot be displayed");
            picTarget.html(imgEl);
        };
    }
};



//loaders
function showTopLoader() {
    $("#topLoader").removeClass("nonner");
}
function hideTopLoader() {
    $("#topLoader").addClass("nonner");
}
function loadingImg(id) {
    $(id).html("<div class='dummyLoader'><img src='/public/images/loading.gif' alt='loading..' /></div>");
}
function loadingSmallImg(id) {
    $(id).html("<div class='smallLoader centry'><img src='/public/images/loading.gif' alt='loading..' /></div>");
}
function smallLoader(el) {
    $(el).html("<div class='smallLoader'></div>");
}
function bigLoader(el) {
    $(el).html("<div class='bigLoader'></div>");
}
function loader(el, minHeight) {
    if (!minHeight)
        minHeight = 0;
    $(el).html("<div class='loader'></div>").css("min-height", minHeight);
}


function increaseCount(countEl) {
    var count = parseInt(countEl.text());
    if (!isNaN(count))
        countEl.text(++count);
}
function decreaseCount(countEl) {
    var count = parseInt(countEl.text());
    if (!isNaN(count))
        --count;
    if (count >= 0)
        countEl.text(count);
}

//caches
var topicToSubTopic = {};

var showFloater = false, floaterDiv;
$(document).on('mouseenter', ".floaterSrc,.floaterDiv", function() {
    if (floaterDiv) {
        showFloater = true;
        floaterDiv.removeClass("nonner");
    }
});
$(document).on('mouseleave', ".floaterSrc", function(e) {
    showFloater = false;
    setTimeout(function() {
        if (!showFloater && floaterDiv)
            floaterDiv.addClass("nonner");
    }, 200);
});
$(document).on('mouseleave', ".floaterDiv", function(e) {
    showFloater = false;
    if (floaterDiv)
        floaterDiv.addClass("nonner");
});
$(document).on('focus', ".floaterDiv select,.floaterDiv input", function() {
    if (floaterDiv)
        floaterDiv.addClass("floaterDivDisplay");
}).on('blur', ".floaterDiv select,.floaterDiv input", function() {
    if (floaterDiv)
        floaterDiv.removeClass("floaterDivDisplay");
});


/*/commonPopup
 var getCommonPopupBody;
 var cancelAllCommonPopup;
 var cancelCommonPopup;
 new function($){
 var commonPopupArr = [];
 getCommonPopupBody = function(holderWidth,holderHeight,allowToBeChild){
 var commonPopupHolder=$("#commonPopupHolder");
 var commonPopup=commonPopupHolder.find(".commonPopup");
 if(commonPopupArr.length > 0){
 if(!allowToBeChild){
 cancelAllCommonPopup();
 commonPopup=$(".commonPopup");
 }else{
 commonPopup = commonPopup.clone();
 commonPopupHolder.append(commonPopup);
 $(commonPopupArr[commonPopupArr.length-1]).addClass("nonner");
 }
 }
 if(holderWidth) commonPopup.width(holderWidth);
 if(holderHeight) commonPopup.find('.commonPopupBody').css('min-height',holderHeight+'px');
 commonPopup.removeClass("nonner");
 commonPopup.data("popupIndex",commonPopupArr.length);
 commonPopup.find(".commonPopupBody").html("");
 commonPopupArr.push(commonPopup);
 return commonPopup.find(".commonPopupBody");
 };
 $(".cancelCommonPopup").live('click',function(e){
 var target = $(this).closest(".commonPopup");
 cancelCommonPopup(target);
 });
 cancelAllCommonPopup = function(){
 $($(".commonPopup").get(0)).siblings().remove();
 commonPopupArr = [];
 $(".commonPopup").addClass("nonner")
 .find(".commonPopupBody").html("");
 };
 cancelCommonPopup = function(target){
 if(target){
 var popupIndex = $(target).data("popupIndex");
 commonPopupArr.splice(popupIndex,1);
 $(target).addClass("nonner").find(".commonPopupBody").html("");
 }else{
 target = commonPopupArr.pop();
 $(target).addClass("nonner").find(".commonPopupBody").html("");
 }
 if($(target).data("popupIndex")>0){
 $(target).remove();
 $(commonPopupArr[commonPopupArr.length-1]).removeClass("nonner");
 }
 };
 }(jQuery);
 function clearPopupsAndTimers(){
 cancelCommonPopup();
 insideOutClick();
 //    if(challengeTimer)clearInterval(challengeTimer);
 //    if(burstTimer)clearInterval(burstTimer);
 }
 */


var openToggler, toggleClick, openTogglerCallback = {};
function addToggler(target, $this) {
    insideOutClick();
    openToggler = target;
    toggleClick = $this;
    if (openToggler)
        openToggler.addClass("openToggler");
    if (toggleClick)
        toggleClick.addClass("toggleClick");
    $(window).bind('click.openToggler', function(e) {
        var t = $(e.target);
        if (!(t.is(openToggler)) && t.closest(openToggler).length == 0 && !(t.is(toggleClick)) && t.closest(toggleClick).length == 0) {
            insideOutClick();
        }
    });
}
function insideOutClick() {
    if (openToggler && openToggler.data("callback")) {
        openTogglerCallback[openToggler.data("callback")](openToggler);
    }
    if (openToggler != undefined)
        openToggler.removeClass("openToggler");
    $(window).unbind('click.openToggler');
    if (toggleClick != undefined)
        toggleClick.removeClass("toggleClick");
    openToggler = null;
    toggleClick = null;
}

//RTEs
function assignRTEs(divList, placeHolder) {
    for (var k = 0; k < divList.length; k++) {
        var div = divList.eq(k);
        div.html(rteSample.children().clone(true));
        var rteHolder = div.find(".RTEHolder");
        vRTE.init(rteHolder);
        if (placeHolder && placeHolder != undefined) {
            rteHolder.find(".RTEArea").addClass("RTEHasPH")
                    .data("ph", placeHolder).html(placeHolder);
        }
    }
}
function cleanRTE(rteArea) {
    rteArea.html("");
    if (rteArea.data("ph")) {
        rteArea.html(rteArea.data("ph")).addClass("RTEHasPH");
    }
}

//errors and response checks
function showTimedError(errorTxt) {
    setTimeout(function() {
        // showError(errorTxt);
    }, 1000);
}
function showMessage(content, btnText, cbFn) {
    btnText = btnText || "OK";
    showVMsgBox(content, btnText, "SUCCESS", cbFn);
}
$(".cancelErrorPopup").live('click', function() {
    cancelErrorPopup();
});
function cancelErrorPopup() {
    hideVMsgBox();
}

//text string urls emails validation
function morify(str, charLimit) {
    if (!charLimit)
        charLimit = 140;
    var finalStr = "";
    if (str.length > charLimit) {
        var i = str.indexOf(' ', charLimit), l = "";
        if (i != -1) {
            l = "<span class='hided' style='display:none'>" + str.substring(i) + "</span><a class='moreInfo'> ..more</a>";
        }
        else {
            i = charLimit;
        }
        finalStr = str.substring(0, i) + l;
    }
    return finalStr;
}
function pluralize(count, pluralText, singularText) {
    var returnText = "";
    if (!pluralText)
        pluralText = "s";
    var num = parseInt(count);
    if (!isNaN(num)) {
        if (num == 1 && singularText)
            returnText = singularText;
        else if (num != 1)
            returnText = pluralText;
    }
    return returnText;
}
function validEmail(s) {
    if (s.length > 0) {
        var i = s.indexOf("@"), j = s.indexOf(".", i), k = s.indexOf(","), kk = s.indexOf(" "), jj = s.lastIndexOf(".") + 1,
                len = s.length;
        if ((i > 0) && (j > (i + 1)) && (k == -1) && (kk == -1) && (len - jj >= 2) && (len - jj <= 4)) {
            return true;
        }
        else {
            return false;
        }

    }
    else
        return false;
}
function validDomain(s) {
    if (s.length > 0) {
        var j = s.indexOf("."), k = s.indexOf(","), kk = s.indexOf(" "), jj = s.lastIndexOf(".") + 1,
                len = s.length;
        if (j !== 0 && (k == -1) && (kk == -1) && (len - jj >= 2) && (len - jj <= 4)) {
            return true;
        }
        else {
            return false;
        }

    }
    else
        return false;
}
function urlify(text) {

    // http://, https://, ftp://
    var urlPattern = /\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|]/gim;

    // www. sans http:// or https://
    var pseudoUrlPattern = /(^|[^\/])(www\.[\S]+(\b|$))/gim;

    // Email addresses *** here I've changed the expression ***
    var emailAddressPattern = /(([a-zA-Z0-9_\-\.]+)@[a-zA-Z_]+?(?:\.[a-zA-Z]{2,6}))+/gim;

    return text
            .replace(urlPattern, '<a target="_blank" class="vUrl" href="$&">$&</a>')
            .replace(pseudoUrlPattern, '$1<a target="_blank" class="vUrl" href="http://$2">$2</a>')
            .replace(emailAddressPattern, '<a target="_blank" class="vMailto" href="mailto:$1">$1</a>');
}
function filterText(input) {
    var output;
    output = input.replace("'", "&apos;", "g");
    output = output.replace("\"", "&quot;", "g");
    output = output.replace("`", "&acute;", "g");
    output = output.replace(">", "&gt;", "g");
    output = output.replace("<", "&lt;", "g");
    output = output.replace(/\n/g, "<br>");
    return output;
}
function filterJS(input) {
    var output;
    output = input.replace("<script", "&lt;", "g");
    output = output.replace("</script", "&lt;", "g");
    return output;
}
function urlDecode(str) {
    return decodeURIComponent((str + '').replace(/\+/g, '%20'));
}
function checkUrl(url) {
    var pos = url.indexOf("."), count = 0;
    while (pos != -1) {
        count++;
        pos = url.indexOf(".", pos + 1);
    }
    if (count >= 1 && count <= 4)
        return true;
    else
        return false;
}
var getUrlDomain = function(url) {
    try {
        url = url.replace("http://", "", "gi");
        url = url.replace("www.", "", "gi");
        url = "http://www." + url.substring(0, url.indexOf('/'));
        return url;
    } catch (err) {
        return null;
    }
}
var commonElsOfArrays = function(arr1, arr2) {
    var commonElsArray = [];
    for (var k = 0; k < arr1.length; k++) {
        var el = arr1[k];
        if (arr2.indexOf(el) > -1) {
            commonElsArray.push(el);
        }
    }
    return commonElsArray;
};


//numbers and dates
function getRandomNumber(range)
{
    return Math.floor(Math.random() * range);
}

function getRandomChar()
{
    var chars = "0123456789abcdefghijklmnopqurstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ";
    return chars.substr(getRandomNumber(62), 1);
}

function randomId(size)
{
    var str = "";
    for (var i = 0; i < size; i++)
    {
        str += getRandomChar();
    }
    return str;
}
function hasNoSplChars(str) {
    return !/[~`!#$%\^&*+=\-\[\]\\';,/{}|\\":<>\?]/g.test(str);
}
//to allow all chars and numbers expect q and r :
//var re = /[^A-PS-Z0-9]+/i;
//document.write(re.test(myString));


function indexToChar(n) {
    var s = "";
    while (n >= 0) {
        s = String.fromCharCode(n % 26 + 97) + s;
        n = Math.floor(n / 26) - 1;
    }
    return s;
}
function validateAndGetMS(y, m, d) {
    y = y.toString();
    d = d.toString();
    m = m.toString();
    if (m.charAt(0) == '0')
        m = m.charAt(1);
    if (d.charAt(0) == '0')
        d = d.charAt(1);
    if (!checkIntNum(m) || !checkIntNum(y) || !checkIntNum(d)) {
        return 0;
    }
    if (m > 12 || m < 1 || (y.length != 4) || d > getMonthMaxDays(new Date(y, m - 1, 1)) || d < 1) {
        return 0;
    } else {
        return (new Date(y, m - 1, d).getTime());
    }
}
function FebMax(y) {
    var d = 4, feb = 28;
    if (y % 100 == 0)
        d = 400;
    if (y % d == 0)
        feb = 29;
    return feb;
}
function getMonthMaxDays(dateObj) {
    var monthNormal = dateObj.getMonth() + 1;
    var maxD = 30, m31 = [1, 3, 5, 7, 8, 10, 12];
    if (m31.indexOf(monthNormal) != -1)
        maxD = 31;
    else if (monthNormal == 2)
        maxD = FebMax(dateObj.getFullYear());
    return maxD;
}
function getDayHrsMins(dateObj) {
    var hrs = dateObj.getHours();
    var mins = dateObj.getMinutes();
    var str = "AM";
    if (hrs > 12) {
        hrs -= 12;
        str = "PM";
    }
    if (hrs < 10)
        hrs = "0" + hrs;
    if (mins < 10)
        mins = "0" + mins;
    return (hrs + ":" + mins + str);
}
function getOrdinalDate(date) {
    var lastDigit = date.toString().substring(date.length - 1), suffix = "th";
    var digits = ["1", "2", "3"], suffixes = ["st", "nd", "rd"];
    if (digits.indexOf(lastDigit) != -1)
        suffix = suffixes[digits.indexOf(lastDigit)];
    return date + suffix;
}
function getAgo(time) {
    return("<span class='time'>" + $.timeago(new Date(time)) + "</span>");
}
function getTimeObj(millis) {
    var secs = millis / 1000;
    if (secs != 0 && secs != undefined) {
        var hr = parseInt(Math.floor(secs / 3600));
        var min = parseInt(Math.floor((secs - (hr * 3600)) / 60));
        var sec = parseInt(secs - (hr * 3600) - (min * 60));

        if (hr < 10) {
            hr = "0" + hr;
        }
        if (min < 10) {
            min = "0" + min;
        }
        if (sec < 10) {
            sec = "0" + sec;
        }
        return {hr: hr, min: min, sec: sec, hasError: false}
    } else
        return {hasError: true}
}
function getDuration(secs) {
    var timeStr = "";
    var timeObj = getTimeObj(secs * 1000);
    if (!timeObj.hasError) {
        timeStr = "<div class='videoDuration' rel='" + secs + "'>" + timeObj.hr + ":" + timeObj.min + ":" + timeObj.sec + "</div>";
    }
    return timeStr;
}
function getHrsMinsSecs(targetDiv) {
    var select = '';
    if (targetDiv.find(".timeSelectHrs").length > 0) {
        select = "Select";
    }
    var hrs = parseInt(targetDiv.find(".time" + select + "Hrs").val(), 10);
    var mins = parseInt(targetDiv.find(".time" + select + "Mins").val(), 10);
    var secs = parseInt(targetDiv.find(".time" + select + "Secs").val(), 10);
    if (checkIntNum(hrs) && checkIntNum(mins) && checkIntNum(secs) &&
            hrs <= 24 && mins <= 60 && secs <= 60) {
        secs += mins * 60 + hrs * 3600;
        return (secs * 1000);
    } else {
        return -1;
    }
}
var getDateInDOBFormat = function(timeInMillis) {
    var str = "";
    if (timeInMillis == 0 || timeInMillis == -1) {
        return str;
    }
    try {
        var d = new Date(timeInMillis);
        var month = (d.getMonth() + 1);
        var date = d.getDate();
        if (month < 10) {
            month = "0" + month;
        }
        if (date < 10) {
            date = "0" + date;
        }
        str = d.getFullYear() + "-" + month + "-" + date;
    } catch (e) {

    }
    return str;
};
function checkIntNum(num) {
    var intRegex = /^\d+$/;
    return intRegex.test(num);
}
function checkFloatNum(num) {
    num = num.toString();
    return (!isNaN(num) && parseFloat(parseFloat(num).toString()) === parseFloat(num));
}


//misc
function checkUndefined(c) {
    if (c == undefined)
        c = "";
    return c;
}
function makeHTMLTag(tag, attrs) {
    var el = document.createElement(tag);
    if (attrs) {
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
    }
    return $(el);
}
function cancelEvent(e) {
    e = e || window.event;
    if (e.preventDefault && e.stopPropagation)
    {
        e.preventDefault();
        e.stopPropagation()
    }
    return false;
}
var getUserSelectionRange = function() {
    var userSelection;
    if (window.getSelection) {
        userSelection = window.getSelection();
    }
    else if (document.selection) { // should come last; Opera!
        userSelection = document.selection.createRange();
    }
    return getRangeObject(userSelection);
};
function getRangeObject(selectionObject) {
    if (selectionObject.getRangeAt)
        return selectionObject.getRangeAt(0);
    else { // Safari!
        var range = document.createRange();
        range.setStart(selectionObject.anchorNode, selectionObject.anchorOffset);
        range.setEnd(selectionObject.focusNode, selectionObject.focusOffset);
        return range;
    }
}
$(".vUrl").live('click', function(e) {
    e.stopPropagation();
});
$.fn.outerHTML = function() {
    return $('<div />').append(this.eq(0).clone()).html();
};
$.fn.findFirst = function(elementSelector) {
    return $(this).find(elementSelector).first();
};
function animateScrollBar(scrollValue, timeInMS, callback) {
    if (!timeInMS)
        timeInMS = 1000;
    $('html,body').animate({scrollTop: scrollValue + "px"}, timeInMS, callback);
}
$.fn.appendText = function(text) {
    this.each(function() {
        var textNode = document.createTextNode(text);
        $(this).append(textNode);
    });
};


//history api fns
$(window).bind('load', function() {
    setTimeout(function() {
        bindPopstateEvent();
    }, 0);
});
function bindPopstateEvent() {
    $(window).off("popstate");
    $(window).on("popstate", function(e) {
        // var currentState = e.originalEvent.state;//not using stateobj for now
        refreshPage();
    });
}
var refreshPage = function() {
    var urlPathStrips = window.location.pathname.substr(1).split("/");
    var urlParams = {};
    try {
        urlParams = getAllUrlParams();
    } catch (err) {
    }
    vHistory.init(urlPathStrips, urlParams);
};

function getAllUrlParams(){
    var url = location.search;var ret = {};
    if(url){
        url = url.replace("?","");
        url = url.split("&");
        for(u in url){
            var t = url[u].split("=");
            ret[t[0]]=decodeURIComponent(t[1]);
        }
    }
    return ret;
}
var goToBackPage = function(backUpUrl) {
    if (history && history.length > 1) {
        history.back();
    } else if (backUpUrl) {
        pushHistory(null, null, backUpUrl);
        refreshPage();
    }
}
//$("a").live('click',function() {
//    pushHistory(null , null, this.href );
//    return false;
//});{
function pushHistory(state, title, pathWithSearhParams, doReplace) {
    var returnLocation = history.location || document.location;
    var currPath = returnLocation.pathname + returnLocation.search;
    state = state || {};
    state["prevUrl"] = currPath;
    if (pathWithSearhParams !== currPath) {
        if (doReplace) {
            history.replaceState(state, title, pathWithSearhParams);
        } else {
            history.pushState(state, title, pathWithSearhParams);
        }
    }
    history.lastPathName = returnLocation.pathname;
}
var pushNewUrlParams = function(urlParams) {
    var returnLocation = history.location || document.location;
    var currPath = returnLocation.pathname;
    if (urlParams && !$.isEmptyObject(urlParams)) {
        currPath += "?" + $.param(urlParams);
    }
    pushHistory(null, null, currPath);
};
var fetchUrlParams = function() {
    return decodeURIComponent(window.location.search.substr(1))
            .split('&')
            .reduce(function _reduce(/*Object*/ a, /*String*/ b) {
                b = b.split('=');
                var key = b[0];
                if (key) {
                    if (key.charAt(key.length - 2) == "[" && key.charAt(key.length - 1) == "]") {
                        if (a[key] == undefined) {
                            a[key] = [];
                        }
                        a[key].push(b[1]);
                    } else {
                        a[b[0]] = b[1];
                    }
                }
                return a;
            }, {});
};


var urlParamsToObj = function(params) {
    params = params.replace(/&/g, "\",\"").replace(/=/g, "\":\"");
    params = decodeURI(params);
    params = JSON.parse('{"' + params + '"}');
    return params;
};
var transformParams = function(params) {
    //var arr=decodeURIComponent($.customParam(params)).split("&")
    if (!params) {
        return {};
    }
    params = (typeof params == "string") ? urlParamsToObj(params) : params;
    var customParam = $.customParam(params);
    var newParams = {};
    if (customParam) {
        var arr = customParam.split("&");
        for (var k = 0; k < arr.length; k++) {
            var p = arr[k].split("=");
            var key = decodeURIComponent(p[0]);
            var value = decodeURIComponent(p[1]);
            newParams[key] = value;
        }
    }
    return newParams;
};

(function($) {
    // copy from jquery.js
    var r20 = /%20/g,
            rbracket = /\[\]$/;

    $.extend({
        customParam: function(a) {
            var s = [],
                    add = function(key, value) {
                        // If value is a function, invoke it and return its value
                        value = jQuery.isFunction(value) ? value() : value;
                        s[ s.length ] = encodeURIComponent(key) + "=" + encodeURIComponent(value);
                    };

            // If an array was passed in, assume that it is an array of form elements.
            if (jQuery.isArray(a) || (a.jquery && !jQuery.isPlainObject(a))) {
                // Serialize the form elements
                jQuery.each(a, function() {
                    add(this.name, this.value);
                });

            } else {
                for (var prefix in a) {
                    buildParams(prefix, a[ prefix ], add);
                }
            }

            // Return the resulting serialization
            return s.join("&").replace(r20, " ");
        }
    });

    /* private method*/
    function buildParams(prefix, obj, add) {
        if (jQuery.isArray(obj)) {
            // Serialize array item.
            jQuery.each(obj, function(i, v) {
                if (rbracket.test(prefix)) {
                    // Treat each array item as a scalar.
                    add(prefix, v);

                } else {
                    //buildParams( prefix + "[" + ( typeof v === "object" || jQuery.isArray(v) ? i : "" ) + "]", v, add );
                    buildParams(prefix + "[" + i + "]", v, add);
                }
            });

        } else if (obj != null && typeof obj === "object") {
            // Serialize object item.
            for (var name in obj) {
                buildParams(prefix + "." + name, obj[ name ], add);
            }

        } else {
            // Serialize scalar item.
            add(prefix, obj);
        }
    }
    ;
})(jQuery);
