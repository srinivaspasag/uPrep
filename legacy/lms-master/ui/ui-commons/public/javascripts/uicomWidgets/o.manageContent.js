var manageContent = new (function($) {
    this.init = function(mcWidget, mcWidgetData, loadContent, setLoadMoreOnly, cbAfterLoad) {
        var mcWidgetClick = "click.mcWidget", mcWidgetKeyup = "keyup.mcWidget";
        mcWidget.off(".mcWidget")
                .on(mcWidgetClick, ".mcTab", mcWidget, mcTab)
                .on(mcWidgetClick, ".mcSubTab", mcWidget, mcSubTab)
                .on(mcWidgetClick, ".mcSearchDivImg", mcWidget, mcSearchDivImg)
                .on(mcWidgetKeyup, ".mcSearchDivInput", mcWidget, mcSearchDivInput)
                .data("cbAfterLoad", cbAfterLoad);

        initmcWidgetParams(mcWidget, mcWidgetData);
        if (loadContent) {
            loadmcContent(mcWidget, undefined, cbAfterLoad, null, null, true);
        }
        if (setLoadMoreOnly) {
            setLoadMoreWidgets(mcWidget, mcWidgetData.urlStr, mcWidgetData.params,
                    (mcWidgetData.moreSize || mcWidgetData.initialSize));
        }
    };




    var mcTab = function(e) {
        mcTabUtil(e, $(this), "activemcTab");
        //this is mostly used in case of url change with tabs
        var mcWidget = e.data;
        var urlStr = $(this).data("urlStr");
        if (urlStr) {
            mcWidget.data("urlStr", urlStr);
        }
    };
    var mcSubTab = function(e) {
        mcTabUtil(e, $(this), "activemcSubTab");
    };
    var mcTabUtil = function(e, mcTabEl, activeClass) {
        activeClass = mcTabEl.parent().data("activeClass") || activeClass;
        mcTabEl.addClass(activeClass).siblings().removeClass(activeClass);
        var sourceForBeforemccall = mcTabEl;
        if (!mcTabEl.data("beforemccall"))
            sourceForBeforemccall = mcTabEl.parent();
        setParamsAndCheckCalls(e.data, mcTabEl.data().params, sourceForBeforemccall);
    };
    var mcSearchDivImg = function(e) {
        var mcWidget = e.data, $this = $(this);
        var query = mcWidget.find(".mcSearchDivInput").val().trim();
        if (query != "") {
            var params = {}, paramName = $this.data("paramName");
            var vChoose = $this.parent().siblings(".mcSearchDivvChoose");
            if (vChoose.length > 0) {
                paramName = vChoose.data("value");
            }
            params[paramName] = query;
            setParamsAndCheckCalls(mcWidget, params, $this);
        }
    };
    var mcSearchDivInput = function(e) {
        var mcWidget = e.data, $this = $(this), paramName = $this.data("paramName");
        var oldQuery = mcWidget.data("params")[paramName];
        var query = $this.val().trim(), makeCall = false;
        if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) && query) {
            makeCall = true;
        } else if (!query && oldQuery) {
            query = undefined;
            makeCall = true;
        }
        var vChoose = $this.parent().siblings(".mcSearchDivvChoose");
        if (vChoose.length > 0) {
            paramName = vChoose.data("value");
        }
        if (makeCall) {
            var params = {};
            params[paramName] = query;
            setParamsAndCheckCalls(mcWidget, params, $this);
        }
    };
    this.onmcSelectChange = function(value, target, dataValue) {
        var mcWidget = $(target).closest(".mcWidget");
        var vselect = $(target).closest(".vselect"), vselectData = vselect.data();
        var params = {}, val = vselectData.value;
        if (val == "-1")
            val = undefined;
        params[vselectData.paramName] = val;
        setParamsAndCheckCalls(mcWidget, params, vselect);
    };
    this.onmcvChooseChange = function(vChoose, value) {
        $("#fixedLeftSecPortion").removeClass("nonner");
        $(".questionFilter").addClass("nonner");
        $(".questionType").addClass("nonner");
        var mcWidget = vChoose.closest(".mcWidget");
        var params = {}, $data = vChoose.data();
        if (value == "-1") {
            value = undefined;
        }
        params[$data.paramName] = value;
        setParamsAndCheckCalls(mcWidget, params, vChoose);
    };
    this.onmcvRadioChange = function(vRadio) {
        var vRadioGrp = vRadio.closest(".vRadioGrp");
        var value = vRadioGrp.data("value");
        var mcWidget = vRadioGrp.closest(".mcWidget");
        var params = {}, $data = vRadioGrp.data();
        if (value == "-1") {
            value = undefined;
        }
        params[$data.paramName] = value;
        setParamsAndCheckCalls(mcWidget, params, vRadioGrp);
    };
    this.onmcMVSChange = function(mcMVS, values) {
        values = values || [];
        var mcWidget = mcMVS.closest(".mcWidget");
        if (values.length == 0) {
            values = undefined;
        }
        var params = {};
        params[mcMVS.data("paramName")] = values;
        setParamsAndCheckCalls(mcWidget, params, mcMVS);
    };


    var loadmcContent = function(mcWidget, sourceEl, callMeBack,
            onTheFlyIncludeParams, targetDivClass, doNotChangeUrl) {
        onTheFlyIncludeParams = onTheFlyIncludeParams || {};
        var mcWidgetData = mcWidget.data();
        var mcWidgetParams = mcWidgetData.params;
        if (mcWidgetParams.orderBy === "name.untouched" || mcWidgetParams.orderBy === "customOrder") {
            mcWidgetParams.sortOrder = "ASC";
        } else {
            delete mcWidgetParams.sortOrder;
        }


        var urlParams = $.extend({}, mcWidgetParams);

        $.extend(urlParams, onTheFlyIncludeParams);

        //set initial size and more size
        var urlStr = onTheFlyIncludeParams.urlStr || mcWidgetData.urlStr;
        urlParams.size = mcWidgetData.initialSize || 25;
        var moreSize = mcWidgetData.moreSize || urlParams.size;
        urlParams.start = urlParams.start || 0;
        showTopLoader();
        var successFn = function(data, status, xhr) {
            hideTopLoader();
            targetDivClass = targetDivClass || "LMHandlerDiv";
            var LMHandlerDiv = mcWidget.find("." + targetDivClass);
            LMHandlerDiv.html(data);
            setLoadMoreWidgets(mcWidget, urlStr, urlParams, moreSize, LMHandlerDiv);


            if (callMeBack) {
                callMeBack(data, xhr, urlParams, mcWidget);
            }
            if (sourceEl && sourceEl.data("aftermccall")) {
                window[sourceEl.data("aftermccall")](mcWidget, sourceEl);
            }

            if (!doNotChangeUrl) {
                var changeUrlAfterLoad = mcWidget.data("changeUrlAfterLoad");
                if (changeUrlAfterLoad) {
                    changeUrlAfterLoad(mcWidget);
                }
            }

            loadMJEqns(LMHandlerDiv.get(0));
        };
        vReq.get(urlStr, urlParams, successFn);
    };
    this.loadmcContent = function(mcWidget, sourceEl, callMeBack, onTheFlyIncludeParams, targetDivClass, doNotChangeUrl) {
        callMeBack = callMeBack ? callMeBack : mcWidget.data("cbAfterLoad");
        loadmcContent(mcWidget, sourceEl, callMeBack, onTheFlyIncludeParams, targetDivClass, doNotChangeUrl);
    };


    //utils
    var initmcWidgetParams = function(mcWidget, mcWidgetParams) {
        mcWidget.data("params", mcWidgetParams.params || {});
        mcWidget.data("urlStr", mcWidgetParams.urlStr);
        mcWidget.data("initialSize", mcWidgetParams.initialSize);
        mcWidget.data("moreSize", mcWidgetParams.moreSize);
    };
    var setmcWidgetParams = function(mcWidget, params) {
        var $data = mcWidget.data();
        params = params || {};
        $.each(params, function(key, val) {
            if (val !== undefined && (val.toString() === "false" || val)) {
                $data.params[key] = val;
            } else {
                delete $data.params[key];
            }
        });
    };
    var removemcWidgetParams = function(mcWidgetParams, paramList) {
        for (var k = 0; k < paramList.length; k++) {
            delete mcWidgetParams[paramList[k]];
        }
    };
    var checkmcBeforecalls = function(mcWidget, source) {
        var beforemccall = source.data("beforemccall");
        var callMeBack = mcWidget.data("cbAfterLoad");
        if (beforemccall) {
            window[beforemccall](mcWidget, source);
        } else
            loadmcContent(mcWidget, source, callMeBack);
    };
    var setParamsAndCheckCalls = function(mcWidget, params, source) {
        setmcWidgetParams(mcWidget, params);
        checkmcBeforecalls(mcWidget, source);
    };
    var setLoadMoreWidgets = function(mcWidget, urlStr, urlParams, moreSize, LMHandlerDiv) {
        urlParams.start = urlParams.start || 0;
        urlParams.size = urlParams.size || moreSize;        
        LMHandlerDiv = LMHandlerDiv || mcWidget.find(".LMHandlerDiv");
        var totalHitsHider = mcWidget.find(".totalHitsHider"), totalHits = 0;
        if (totalHitsHider.length > 0) {
            totalHits = parseInt(totalHitsHider.val());
        }
        totalHitsHider.remove();
        var $dataParams = {urlParams: urlParams, urlStr: urlStr,
            allParams: urlParams, moreSize: moreSize, totalHits: totalHits};
        LMHandlerDiv.data($dataParams);

        var arrowsDiv = mcWidget.find(".LMArrowsDiv");
        var pageSetterDiv = mcWidget.find(".pageSetterDiv");
        if (totalHits === 0) {
            arrowsDiv.addClass("hider");
            pageSetterDiv.addClass("nonner");
        } else {
            arrowsDiv.removeClass("hider");
            pageSetterDiv.removeClass("nonner");
        }
        if (arrowsDiv.length > 0) {
            arrowsDiv.find(".LMATotal").text(totalHits);
            arrowsDiv.data($dataParams);
            resetLMArrows(arrowsDiv, urlParams);
        } else if (pageSetterDiv.length > 0) {
            pageSetterDiv.data($dataParams);
            setLMPages(pageSetterDiv, urlParams.start, totalHits, urlParams.size);
        }
    };
    this.setLoadMoreWidgets = function(mcWidget, urlStr, urlParams, moreSize) {
        setLoadMoreWidgets(mcWidget, urlStr, urlParams, moreSize);
    };
    this.removemcWidgetParams = function(mcWidgetParams, paramList) {
        removemcWidgetParams(mcWidgetParams, paramList);
    };
    this.setmcWidgetParams = function(mcWidget, newParamsToAdd) {
        setmcWidgetParams(mcWidget, newParamsToAdd);
    };
})(jQuery);
var onmcSelectChange = function(value, target, dataValue) {
    manageContent.onmcSelectChange(value, target, dataValue);
};
var onmcvChooseChange = function(vChoose, value) {
    manageContent.onmcvChooseChange(vChoose, value);
};
var onmcvRadioChange = function(vRadio) {
    manageContent.onmcvRadioChange(vRadio);
};
var onmcMVSChange = function(mcMVS, values) {
    manageContent.onmcMVSChange(mcMVS, values);
};
