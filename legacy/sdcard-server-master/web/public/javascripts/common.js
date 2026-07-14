var dtappHeader, dtappContentDiv;
$(function() {
    dtappHeader = $("#dtappHeader");
    dtappContentDiv = $("#dtappContentDiv");

    //popup
    $("body").on("click", ".cancelCommonPopup", cancelCommonPopup)
            .on("click", ".closecmdsPopupAlt", function() {
                closecmdsPopup($("#cmdsPopupAlt"));
            })
            .on("click", ".closecmdsMessagePopup", hidecmdsError)
            .on("click", ".closecmdsPopup,.closePopup", function() {
                closecmdsPopup();
            }).on("click", ".settingsImg", function() {
                if ($(this).hasClass("toggleClick")) {
                    insideOutClick();
                } else {
                    addToggler($(this).next(".settingsDiv"), $(this));
                }
            });
});


//popup
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
//$("body").on("click","#cmdsBlackOut",cancelCommonPopup);

var getcmdsPopupBody = function(width, height, btns, cmdsPopupClass) {
    var popup = $("#cmdsPopup");
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
    var popupBody = getCommonPopupBody(width, height, false, popup);
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
};
var hidecmdsPopupError = function(popup) {
    popup = popup || $("#cmdsPopup");
    popup.find(".cmdsPopupError").addClass("nonner").html("");
};
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
    if (popup.css("display") === "none") {
        cmdsBlackOut.hide();
    } else {
        cmdsBlackOut.css("z-index", popup.css("z-index") - 1);
    }
};
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



//vReq
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
                showError(d.children("#respErrorMessage").val());
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
                showError(resp.errorMessage || resp.errorCode);
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
        showError(COMMON_ERROR_MESSAGE);
    }
//   else if(xhr.status==0){
//        showError("Your internet connection is either too slow or lost");
//   }   
};
function checkJSONResponse(data) {
    var isClean = true;
    if (!data.hasOwnProperty("result")) {
        showError(COMMON_ERROR_MESSAGE);
        isClean = false;
    }
    else if (data.errorMessage != "") {
        showError(data.errorMessage);
        isClean = false;
    }
    return isClean;
}
function filterJS(input) {
    var output;
    output = input.replace("<script", "&lt;", "g");
    output = output.replace("</script", "&lt;", "g");
    return output;
}

//loaders
function showTopLoader() {
    $("#topLoader").removeClass("nonner");
}
function hideTopLoader() {
    $("#topLoader").addClass("nonner");
}


//make tags
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

var NO_INTERNET_CONNECTION="You are not connected to the internet. Connect and try again.";
var isUserOnline = function() {
    var isOnline = true;
    $.ajax({
        url: "/UIComTest/ping",
        cache: false,
        timeout: 10000,
        type: "HEAD"
    }).done(function(data, textStatus, xhr) {
        isOnline = true;
        onResponse();
        xhr.processed = true;
    }).fail(function(xhr, textStatus) {
        isOnline = false;
        onResponse();
        xhr.processed = true;
    }).always(function(data, stat, xhr) {
        if (xhr.processed)
            return;
        var pingHeaders;
        try {
            pingHeaders = xhr.getAllResponseHeaders();
        } catch (err) {
            pingHeaders = null;
        }
        if (pingHeaders) {
            isOnline = true;
        } else {
            isOnline = false;
        }
    });
    return navigator.onLine && isOnline;
};


//others
var changeActiveClass = function($this, activeClass) {
    $this.addClass(activeClass).siblings().removeClass(activeClass);
};


//open toggler
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
    if (openToggler != undefined) {
        openToggler.removeClass("openToggler");
    }
    $(window).unbind('click.openToggler');
    if (toggleClick != undefined) {
        toggleClick.removeClass("toggleClick");
    }
    openToggler = null;
    toggleClick = null;
}


