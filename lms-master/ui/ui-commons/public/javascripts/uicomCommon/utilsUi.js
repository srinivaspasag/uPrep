var divi = "<div class='cleaner_with_divider'>&nbsp;</div>";
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
$(".vImageUrl").live('click', function() {
    var $this = $(this);
    if (!$this.closest('.vpopupBody').get(0)) {
        showImagePreview($(this).get(0).outerHTML);
    }
});
var getTempDOMElem = function(type) {
    type = type ? type : "div";
    var elem = document.createElement(type);
    elem = $(elem);
    elem.addClass("tempDomElem");
    $("body").append(elem);
    setTimeout(function() {
        $(".tempDomElem").remove();
    }, 1000);
    return elem;
}
new function() {
    var timeoutObj;
    $(".autogrow").live('keyup cut', function(e) {
        var $this = $(this);
        var key = e.which;
        if (key == 8 || key == 46) {
            onDelete($this);
        } else {
            calcHeight($this);
        }
    });
    $(".autogrow").live('paste', function(e) {
        var $this = $(this);
        setTimeout(function() {
            calcHeight($this);
        }, 50);
    });
    function calcHeight($this) {
        var scrollHeight = $this.prop('scrollHeight');
        $this.css({"height": scrollHeight});
    }
    $(".autogrow").live('cut delete', function(e) {
        onDelete($(this));
    });
    function onDelete($this) {
        if (timeoutObj)
            clearTimeout(timeoutObj);
        timeoutObj = setTimeout(function() {
            $this.height(0);
            if (timeoutObj)
                clearTimeout(timeoutObj);
            timeoutObj = setTimeout(function() {
                var scrollHeight = $this.prop('scrollHeight');
                $this.height(scrollHeight);
            }, 10);
        }, 300);
    }
    ;
}(jQuery);
function stopAllHtml5Video() {
    var videos = $("video");
    videos.each(function(index, video) {
        try {
            //If videojs is loaded.
            if($(video).hasClass("vjs-tech")){
               videojs('html5VideoTag').src('');
               videojs('html5VideoTag').dispose();
               if(document.pictureInPictureElement){
                    document.exitPictureInPicture();
               }
            }
            else{
                video.pause();
                video.src = "";
            }
        } catch (err) {
        }
    });
    videos.remove();
    putConsoleLogs("All Video Play Stop called!");
}
//enterKey event reg
$(document).on('keyup', 'input,textarea', function(e) {
    if (e.which == 13 || e.keyCode == 13) {
        $(this).trigger("enterkey");
    }
});

$(".depthTab").live('click', function() {
    $(this).addClass("activeDepthTab").siblings(".depthTab").removeClass("activeDepthTab");
});

//Feedback
$("#feedBackButton").live('click', function() {
    var popup = getCommonPopupBody();
    popup.html("<div class='feedBackHead boldy'>Feedback</div>\n\
            <input type='text' id='feedBSub' placeHolder='Subject*' class='feedBackInput'/>\n\
            <textarea id='feedBDesc' placeHolder='Description' class='feedBackInput'></textarea>\n\
            <span id='feedBackError'></span><div class='margTop rightText'>\n\
            <span class='blueSubmitButton cancelCommonPopup' id='submitFeedBack'>Submit</span></div>");
});
$("#openGuideButton").live('click', function() {
    //openMyPage("/Application/guide",{tabType:"GUIDE"},null,null,null,$(this));
});
$("#submitFeedBack").live('click', function() {
    var sub = $("#feedBSub").val();
    var srcUrl = j[j.length - 1].tabType;
    if (sub == "") {
        $("#feedBackError").html("Subject field is compulsory");
        return;
    }
    vReq.post("/Profile/submitFeedback", {heading: sub, content: $("#feedBDesc").val(), userAgent: navigator.userAgent, srcUrl: srcUrl}, function(data) {

    });
});


//videoPlayer
var insertVideoPlayer = function(videoUrl, videoWidget, playerWidth, playerHeight) {
    if (videoUrl) {
        var urlStr = "http://api.embed.ly/1/oembed?url=" + videoUrl;
        $.ajax({
            url: urlStr,
            data: {width: playerWidth, height: playerHeight},
            jsonp: 'callback',
            dataType: 'jsonp',
            type: 'GET',
            success: function(data) {
                if (data.error_message) {
                    videoWidget.children().html(data.error_message);
                } else {
                    videoWidget.html(data.html);
                    var frame = videoWidget.children("iframe");
                    frame.attr("src", frame.attr("src") + "&showinfo=0");
                }
            }
        });
    }
    else
        videoWidget.html("<div class='dummyVideoPlayer'>\n\
        Sorry an error occured,<br> Try again Later\n\
        </div>");
};
var getDateMillisFromvChoose = function(datevChoose) {
    var date = datevChoose.find(".datevChoose").data("value");
    var month = datevChoose.find(".monthvChoose").data("value");
    var year = datevChoose.find(".yearvChoose").data("value");
    return validateAndGetMS(year, month, date);
};


//adding normal tags
$(".addTagsClass").live('click', function() {
    $(this).children(".addTagsInput").focus();
});
$(".addTagsInput").live('keyup', function(e) {
    if ($(this).val().charAt($(this).val().length - 1) == ',') {
        addedTagsAppend($(this).val().substring(0, $(this).val().length - 1), $(this).closest(".addNormalTagsDiv"));
    }
    else if ($(this).val() != "" && ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))) {
        addedTagsAppend($(this).val(), $(this).closest(".addNormalTagsDiv"));
    }
    $(this).attr("size", ($(this).val().length + 1).toString());
});
$(".addTagsInput").live('keyup', function(e) {
    if (((e.which && e.which == 8) || (e.keyCode && e.keyCode == 8)) && $(this).val().length == 0) {
        var list = $(this).siblings(".addTagsList").find(".addedTag");
        list.last().remove();
        $(this).focus();
    }
});
$(".addTagsList label").live('click', function() {
    var pa = $(this).parent();
    pa.remove();
});
$(".addSuggTagsClass label").live('click', function() {
    addedTagsAppend($(this).html(), $(this).closest(".addNormalTagsDiv"));
});
function addedTagsAppend(text, addTagsDiv) {
    addTagsDiv.find(".addTagsList").append("<div class='addedTag'><span>" + text + "</span><label>x</label></div>");
    addTagsDiv.find(".addTagsInput").val('').attr("size", "1").focus();
}
function getNormalTags(target) {
    var normalTags = [];
    target.find(".addedTag span").each(function() {
        normalTags.push($(this).text());
    });
    var addTagsInput = target.find(".addTagsInput");
    if (addTagsInput.length > 0) {
        var trialTag = addTagsInput.val().trim();
        if (trialTag.length > 0) {
            normalTags.push(trialTag);
        }
    }
    return normalTags;
}
function putNormalTags(tags, addTagsList) {
    var tagsDiv = makeHTMLTag("div");
    for (var k = 0; k < tags.length; k++) {
        tagsDiv.append('<div class="addedTag"><span>' + tags[k] + '</span><label>x</label></div>');
    }
    addTagsList.html(tagsDiv.children());
}


$(document).on("focus", ".inputDiv input", function() {
    $(this).closest(".inputDiv").addClass("inputDivFocused");
});
$(document).on("blur", ".inputDiv input", function() {
    $(this).closest(".inputDiv").removeClass("inputDivFocused");
});

//cboxes and multiple select

//checkbox class:cBox,cBoxText
$(document).on("click", ".cBoxText", function() {
    var cbox = $(this).siblings(".cBox"), state = false;
    if (!cbox.prop("checked"))
        state = true;
    cbox.prop("checked", state);
});
$(document).on("click", ".gCBox", function() {
    gCBoxCheck($(this));
});
$(document).on("click", ".gCBoxText", function() {
    gCBoxCheck($(this).siblings(".gCBox"));
});
function gCBoxCheck(cBox) {
    var isChecked = true;
    if (cBox.hasClass("gCBoxChecked")) {
        cBox.removeClass("gCBoxChecked");
        isChecked = false;
    } else
        cBox.addClass("gCBoxChecked");
    var cbfn = cBox.data("callback");
    if (cbfn && window[cbfn]) {
        window[cbfn](cBox, isChecked);
    } else if (cbfn) {
        cbfn(cBox, isChecked);
    }
}
$(document).on("click", ".mvsAllItems", function() {
    var $this = $(this), mvs = $this.closest(".multipleVSelect");
    $this.find("input").prop("checked", true);
    $this.find(".gCBox").addClass("gCBoxChecked");
    mvs.find(".mvsItem input").prop("checked", false);
    mvs.find(".mvsItem .gCBox").removeClass("gCBoxChecked");
    mvs.data("values", []);
    var callback = mvs.data("onchange");
    if (callback) {
        window[callback](mvs, []);
    }
});
$(document).on("change", ".mvsItem input", function() {
    mvsItemCheckFn($(this));
});
$(document).on("click", ".mvsItemText,.mvsItem .gCBox", function() {
    mvsItemCheckFn($(this));
});
function mvsItemCheckFn($this) {
    var mvs = $this.closest(".multipleVSelect");
    var allItemsItem = mvs.find(".mvsAllItems").find("input,.gCBox"), state;
    if ((mvs.find("input").length > 0 && mvs.find("input:checked").length == 0)
            || (mvs.find(".gCBox").length > 0 && mvs.find(".gCBoxChecked").length == 0)) {
        state = true;
        allItemsItem.addClass("gCBoxChecked");
        mvs.data("values", []).data("names", []);
    } else {
        state = false;
        var cBoxes = mvs.find("input:checked,.gCBoxChecked"), vals = [], names = [];
        for (var k = 0; k < cBoxes.length; k++) {
            var p = cBoxes.eq(k).data("value");
            if (p)
                vals.push(p);
            names.push(cBoxes.eq(k).closest(".mvsItem").find(".mvsItemText").text());
        }
        allItemsItem.removeClass("gCBoxChecked");
        mvs.data("values", vals).data("names", names);
    }
    allItemsItem.prop("checked", state);
    var callback = mvs.data("onchange");
    if (callback) {
        window[callback](mvs, vals);
    }
}
var getmvsSample = function(mvsClass, mvsParamName, mvsOnchange, mvsAllItemName) {
    var mvsPa = mvsSample.clone(true);
    mvsPa.find(".multipleVSelect").addClass(mvsClass)
            .data({paramName: mvsParamName, onchange: mvsOnchange});
    mvsPa.find(".mvsAllItemsText").text(mvsAllItemName);
    return mvsPa.children();
}


$(document).on("change", ".mvcAllItems input", function() {
    var $this = $(this);
    mvcAllCheckFn($this, $this.prop("checked"));
});
var mvcAllCheckFn = function($this, isChecked) {
    var multiplevChoose = $this.closest(".multiplevChoose");
    var inputTargets = multiplevChoose.find(".mvcItem input");
    var cBoxes = multiplevChoose.find(".mvcItem .gCBox");
    if (isChecked) {
        inputTargets.prop("checked", true);
        cBoxes.addClass("gCBoxChecked");
    } else {
        inputTargets.prop("checked", false);
        cBoxes.removeClass("gCBoxChecked");
    }
    mvcItemCheckFn(multiplevChoose);
};
$(document).on("change", ".mvcItem input", function() {
    mvcItemCheckFn($(this).closest(".multiplevChoose"));
});
$(document).on("click", ".mvcItemText,.mvcItem .gCBox", function() {
    mvcItemCheckFn($(this).closest(".multiplevChoose"));
});
var mvcItemCheckFn = function(multiplevChoose) {
    var result = getmvcValues(multiplevChoose);
    multiplevChoose.data("values", result.values);
    multiplevChoose.data("names", result.names);
};
var getmvcValues = function(mvChoose) {
    var values = [], names = [], items = [];
    var cBoxes = mvChoose.children(".mvcItem").children(".gCBoxChecked,input:checked");
    for (var k = 0; k < cBoxes.length; k++) {
        var cBox = cBoxes.eq(k), p = cBox.data("value");
        items.push(cBox.parent());
        if (p)
            values.push(p);
        names.push(cBox.parent(".mvcItem").children(".mvcItemText").text());
    }
    return {values: values, names: names, mvcItems: items};
};

$(document).on("click", ".vRadio", function() {
    var $this = $(this), c = "vRadioChecked";
    var grp = $this.closest(".vRadioGrp");
    grp.find(".vRadio").removeClass(c);
    $this.addClass(c);
    grp.data("value", $this.data("value"));
    if (grp.data("callback")) {
        window[grp.data("callback")]($this);
    }
});


//load more 
var resetLMArrows = function(LMArrowsDiv, urlParams) {
    var total = LMArrowsDiv.data("totalHits");
    var start = parseInt(urlParams.start);
    var size = parseInt(urlParams.size);

    if (start >= total) {
        LMArrowsDiv.addClass("hider");
    } else {
        LMArrowsDiv.removeClass("hider");
    }

    //set prev arrow and its text
    var prevArrow = LMArrowsDiv.find(".LMPrevArrow");
    var activePrevClass = "LMPrevArrowActive";
    prevArrow.data("start", start - size);
    LMArrowsDiv.find(".LMAStart").text(start + 1);
    if (start == 0) {
        prevArrow.removeClass(activePrevClass);
    } else {
        prevArrow.addClass(activePrevClass);
    }

    //set next arrow and its text
    var nextArrow = LMArrowsDiv.find(".LMNextArrow");
    var activeNextClass = "LMNextArrowActive";
    nextArrow.data("start", start + size);

    if ((start + size) >= parseInt(total)) {
        LMArrowsDiv.find(".LMAStop").text(total);
        nextArrow.removeClass(activeNextClass);
    } else {
        LMArrowsDiv.find(".LMAStop").text(start + size);
        nextArrow.addClass(activeNextClass);
    }
}
var setLMPages = function(pageSetterDiv, start, totalHits, pageResultsSize) {
    var moreLibs = parseInt(totalHits / pageResultsSize);
    if (totalHits % pageResultsSize != 0)
        moreLibs++;
    var tagMaker = makeHTMLTag, finalHTML = tagMaker("div");
    var maxPageNosToShow = 5;
    if (moreLibs > 1) {
        var currentPage = parseInt(start / pageResultsSize) + 1, biggy = 1,
                prevPage = tagMaker("span", {"class": "acacac prevLibPage"}).text("Prev"),
                nextPage = tagMaker("span", {"class": "acacac nextLibPage"}).text("Next");
        if (currentPage > 1) {
            prevPage = tagMaker("a", {"class": "prevLibPage liner getLMPage"}).data("pageNo", (currentPage - 1));
        }
        if (currentPage < moreLibs) {
            prevPage = tagMaker("a", {"class": "nextLibPage liner getLMPage"}).data("pageNo", (currentPage + 1));
        }
        prevPage.text("Prev");
        nextPage.text("Next");
        var prevNext = tagMaker("div", {"class": 'myLibPrevNext'}).html(prevPage);
        prevNext.append(tagMaker("label", {"class": "feedPipe"}).text("|")).append(nextPage);

        if (currentPage % maxPageNosToShow != 0)
            biggy = parseInt(currentPage / maxPageNosToShow) + 1;
        else
            biggy = currentPage / maxPageNosToShow;
        var prevImgDiv = tagMaker("div", {"class": "quantImgDiv"}),
        nextImgDiv = tagMaker("div", {"class": "quantImgDiv"}),
        numHTML = tagMaker("div", {"class": "middleQuants"});
        for (var i = (biggy - 1) * maxPageNosToShow; i <= ((biggy * maxPageNosToShow) + 1); i++) {
            if (i != 0 && i == (biggy - 1) * maxPageNosToShow) {
                prevImgDiv.html(tagMaker("img", {"class": "getLMPage prevQuantImg",
                    "src": "/public/images/prevQuant.png"}).data("pageNo", i));
            }
            else if (i == (biggy * maxPageNosToShow) + 1 && i <= moreLibs) {
                nextImgDiv.html(tagMaker("img", {"class": "getLMPage nextQuantImg",
                    "src": "/public/images/nextQuant.png"}).data("pageNo", i));
            }
            else if (i == currentPage) {
                numHTML.append(tagMaker("span", {"class": 'currentLibPage getLMPage libPager'})
                        .data("pageNo", i).text(i));
            }
            else if (i <= moreLibs && i > 0) {
                numHTML.append(tagMaker("a", {"class": 'middleQuant getLMPage libPager'})
                        .data("pageNo", i).text(i));
            }
        }
        finalHTML.append(prevImgDiv).append(numHTML).append(nextImgDiv);
    }
    pageSetterDiv.html(finalHTML.children());
}
var initLoadMoreEls = new (function($) {
    var LMArrowsClicked = function() {
        var $this = $(this), lmArrowsDiv = $this.closest(".LMArrowsDiv");
        var mcWidget = lmArrowsDiv.closest(".mcWidget");
        var $data = lmArrowsDiv.data();
        var params = $data.urlParams, urlStr = $data.urlStr;
        params.start = $this.data("start");
        showTopLoader();
        $.get(urlStr, params, function(data) {
            hideTopLoader();
            var lmHandlerDiv = mcWidget.find(".LMHandlerDiv");
            lmHandlerDiv.html(data);
            loadMJEqns(lmHandlerDiv.get(0));
            resetLMArrows(lmArrowsDiv, params);
            try {
                fixContentSec();
            } catch (err) {
            }
            try {
                var callMeBack = mcWidget.data("cbAfterLoad");
                callMeBack(data);
            } catch (err) {
            }

            var changeUrlAfterLoad = mcWidget.data("changeUrlAfterLoad");
            if (changeUrlAfterLoad) {
                changeUrlAfterLoad(mcWidget);
            }
        });
    };
    var getLMPage = function() {
        var $this = $(this), pageSetterDiv = $this.closest(".pageSetterDiv");
        var $data = pageSetterDiv.data(), params = $data.urlParams, urlStr = $data.urlStr;
        params.start = (parseInt($this.data("pageNo")) - 1) * parseInt(params.size);
        showTopLoader();
        $.get(urlStr, params, function(data) {
            hideTopLoader();
            var lmHandlerDiv = pageSetterDiv.closest(".mcWidget").find(".LMHandlerDiv");
            lmHandlerDiv.html(data);
            loadMJEqns(lmHandlerDiv.get(0));
            setLMPages(pageSetterDiv, params.start, $data.totalHits, params.size);
        });
    }
    var clickEvent = "click.LMArrowsorLMPage";
    $(document).off(clickEvent)
            .on(clickEvent, ".LMPrevArrowActive", LMArrowsClicked)
            .on(clickEvent, ".LMNextArrowActive", LMArrowsClicked)
            .on(clickEvent, ".getLMPage", getLMPage)
})(jQuery);



//doc and pl scrolling
//for window scrolling and page navigation
var onWindowScroll = {}, scrollNoter = 0, viewPageType;
$(window).bind('scroll.docPLView', function() {
    var scrollType = "down";
    if (viewPageType == 'DOC_VIEW' || viewPageType == 'PL_VIEW') {
        var w = $(window).scrollTop();
//        var viewLimit=PLTopScrollableHeight(tabType);
//        if(w>viewLimit){
//            $("#noTabSection").addClass("fixedView_"+tabType);
//        }
//        else  {
//            $("#noTabSection").removeClass("fixedView_"+tabType);
//        }
        if (scrollNoter <= w)
            scrollType = "down";
        else
            scrollType = "up";
        scrollNoter = w;
        if (onWindowScroll[viewPageType]) {
            onWindowScroll[viewPageType](scrollType);
        }
    }
});


//add video using youtube urls
vVideoAdd = new (function($) {
    this.init = function() {
        //link and video fetching
        $("body")
                .on('paste', ".vVideoUrlInput", videoUrlInputPaste)
                .on('keyup', ".vVideoUrlInput", videoUrlInputKeyup)
                .on('keyup', ".playvVideo", playvVideo)
    }

    var videoUrlInputKeyup = function(e) {
        if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))) {
            fetchVideoInfo($(this));
        }
    }
    var videoUrlInputPaste = function() {
        var $this = $(this);
        setTimeout(function() {
            fetchInfoOnPaste($this);
        }, 10)
    }
    var fetchInfoOnPaste = function($this) {
        fetchVideoInfo($this);
    }
    var fetchVideoInfo = function($this) {
        var url = $this.val();
        if (checkUrl(url)) {
            var vVideoHolder = $this.closest(".vVideoHolder");
            var targetDiv = vVideoHolder.find(".vVideoInfoBody");
            inlineLoader(targetDiv);
            $.get("/uicomwidgets/videoInfo", {url: url}, function(data) {
                targetDiv.html(data);
                var cb = $this.data("callback");
                if (cb && window[cb]) {
                    window[cb](vVideoHolder);
                }
            });
        }
        else
            showError("Please enter a valid url");
    }
    var playvVideo = function() {
        showError("under development");
    }
})(jQuery);
vVideoAdd.init();



//check ans submitted.
var checkQuesAnsUtils = new (function($) {
    this.quesAnsSubmit = {};
    this.quesAnsSubmit["SCQ"] = function(ansBlock) {
        var ans = ansBlock.find(".quesAnsRadio:checked").val(), answer = [];
        if (ans == undefined || ans == "") {
            return {error: "Please select an option", result: false};
        }
        else {
            answer.push(ans);
            return {result: true, ans: answer};
        }
    }
    this.quesAnsSubmit["MCQ"] = function(ansBlock) {
        var altAns = [], cBoxes = ansBlock.find(".quesAnsCheckbox:checked");
        if (cBoxes.length == 0) {
            return {error: "Please select an option", result: false}
        }
        else {
            cBoxes.each(function() {
                altAns.push($(this).val());
            });
            return {result: true, ans: altAns};
        }
    }
    this.quesAnsSubmit["PARAGRAPH"] = function(ansBlock) {
        var rteHolder = ansBlock.find(".RTEHolder");
        var ans = vRTE.getRTEContent(rteHolder);
        if (vRTE.isRTEEmpty(rteHolder)) {
            return {error: "Please write the answer and then submit", result: false}
        }
        else {
            return {result: true, ans: ans.trim()};
        }
    }
    this.quesAnsSubmit["TEXT"] = function(ansBlock) {
        var ans = ansBlock.find(".QATextNumInput").val();
        if (ans.trim() == "") {
            return {error: "Please write the answer and then submit", result: false}
        }
        else {
            return {result: true, ans: ans.trim()};
        }
    }
    this.quesAnsSubmit["NUMERIC"] = function(ansBlock) {
        var ans = ansBlock.find(".QATextNumInput").val().trim();
        if (!checkFloatNum(ans)) {
            return {error: "Please enter the answer(Numerical Values only)", result: false}
        }
        else if (ans.length > 11) {
            return {error: "Answer should not exceed 11 characters", result: false}
        }
        else {
            return {result: true, ans: parseFloat(ans)};
        }
    }
})(jQuery);




var getFormValues = function(targetDiv) {
    var params = {};
    var hasError = false;
    var fieldReq = false;
    var errorText;
    targetDiv.find(".formInput,.formmvc,.formSelect,.formvChoose,\n\
    .formTextarea,.formNormalTags,.formvRadioGrp").each(function() {
        var $this = $(this);
        if ($this.data("req")) {
            fieldReq = true;
        } else {
            fieldReq = false;
        }
        if ($this.hasClass("hasError")) {
            $this.focus();
            errorText = "Form Has some error , Please verify and proceed !";
            hasError = true;
        }
        else if ($this.hasClass("formvChoose") || $this.hasClass("formvRadioGrp")
                || $this.hasClass("formmvc")) {
            var values = $this.data("value") || $this.data("values");
            var names = $this.data("names");
            var validVal = !(values == "-1" || !values || values.length == 0);
            if (validVal) {
                params[$this.data("paramName")] = values;
                params[$this.data("paramName") + "Names"] = names;
            }
            if (!validVal && fieldReq) {
                hasError = true;
            }
        }
        else if ($this.hasClass("formNormalTags")) {
            var tags = getNormalTags($this);
            var validVal = !(!tags || tags.length == 0);
            if (validVal) {
                params[$this.data("paramName")] = tags;
            }
            if (!validVal && fieldReq) {
                hasError = true;
            }
        } else if ($this.attr("type") == "checkbox") {
            var itemVal = $this.is(":checked");
            params[$this.attr("name")] = itemVal;
        } else if ($this.attr("type") == "radio") {
            if ($this.is(":checked")) {
                params[$this.attr("name")] = $this.val().trim();
            }
        } else {
            var itemVal = $this.val().trim();
            if (itemVal === undefined || itemVal === null) {
                itemVal = "";
            }
            params[$this.attr("name")] = itemVal;
            if (!itemVal && fieldReq) {
                hasError = true;
            } else if ($this.hasClass("formInput")) {
                var resp = userInputs.check($this, $this.data("patternType"));
                if (!resp.result) {
                    hasError = true;
                    errorText = resp.error;
                }
            }
        }
    });
    if (hasError) {
        showcmdsPopupError(null, errorText);
    }
    params.hasError = hasError;
    params.errorText = errorText;
    return params;
};
var userInputs = {
    resp: {result: true},
    check: function(inputEl, patternType) {
        this.resp = {result: true};
        if (this[patternType]) {
            this[patternType](inputEl);
        }
        return this.resp;
    },
//    NORMAL_INPUT:function(inputEl){
//        var val=inputEl.val();
//        if(val.length>256){
//            this.resp={result:false,error:inputEl.attr("name")+" should not be more \n\
//             than 256 characters in length"};
//        }
//    }, 
    ACAD_ENTITY_CODE: function(inputEl) {
        var val = inputEl.val();
        var regPattern = /[^0-9A-z-]/g;
        if (regPattern.test(val) || val.length > 6) {
            this.resp = {result: false, error: "Code should not be more than 6 characters.\n\
            <br>Allowed characters for Code: 0-9,A-Z,a-z,-"};
        }
    }
};

var uploadProfilePicUtil = new (function() {
    var uploadDiv, previewDiv;
    this.picFile;
    this.qqUploadVar;
    this.cbfn;
    this.completeFn;
    var actionUrl;
    this.init = function(uploadDivEl, previewDivEl, url) {
        uploadDiv = uploadDivEl;
        previewDiv = previewDivEl;
        actionUrl = url;
        this.picFile = null;
        this.qqUploadVar = null;
        this.cbfn = null;
        this.completeFn = null;
        fetchScripts([{fname: "uicomWidgets/fileuploader.js", cb: createPicUploader}]);
    };
    //upload profile pic
    var createPicUploader = function() {
        var u = new qq.FileUploader({
            element: uploadDiv.get(0),
            action: actionUrl,
            debug: true,
            sizeLimit: 5 * 1024 * 1024,
            allowedExtensions: ["jpg", "gif", "png"]
        });
        u.onUploadDone = onUploadDone;
        var uploadButton = uploadDiv.find(".qq-upload-button");
        uploadButton.addClass("aTagClass liner");
        uploadButton.data("onFileChosen", onProfilePicChosen);
        var uploadDropArea = uploadDiv.find(".qq-upload-drop-area");
        uploadDropArea.data("onFileChosen", onFileDropped);
        uploadDiv.find(".qq-button-title").html("Add Picture");
        uploadDiv.find(".qq-upload-list").hide();
        uploadProfilePicUtil.qqUploadVar = u;
    };
    var onFileDropped = function(dataTransfer) {
        onProfilePicSelected(dataTransfer);
    };
    var onProfilePicChosen = function(input) {
        onProfilePicSelected(input);
    };
    var onProfilePicSelected = function(input) {
        var picTarget = previewDiv;
        var imgEl = makeHTMLTag("img", {"class": "img50"});
        previewImagesUtil(input, picTarget, imgEl);
        uploadProfilePicUtil.picFile = input.files[0];
    };
    var onUploadDone = function(id, fileName, result) {
        uploadDiv.find(".qq-upload-list").html("");
        if (uploadProfilePicUtil.completeFn) {
            uploadProfilePicUtil.completeFn(result);
        }
        if (!result || result.errorCode != "") {
            var error = "";
            if (uploadProfilePicUtil.uploadError) {
                error = uploadProfilePicUtil.uploadError;
            } else {
                error = "There was an error in uploading the profile pic.<br>" + result.errorMessage
                        + "<br>Please try uploading the picture again.";
            }
            showError(error);
        } else {
            if (uploadProfilePicUtil.cbfn) {
                uploadProfilePicUtil.cbfn(result);
            }
        }
    };
});

//uploading diagram
var rteHolderForAddDiagram;
$(".RTEAddDiagram").live("click", function() {
    var popup = getCommonPopupBody(550, null, true);
    smallLoader(popup);
    var params = {};
    vReq.get("/UIComWidgets/addDiagrams", params, function(data) {
        popup.html(data);
        popup.find(".LMHandlerDiv").data("urlStr", searchQAADUrl)
                .data("size", 10).data("allParams", params);
        if (window.qq) {
            createUploaderQuesAdd();
        } else {
            fetchScripts([{fname: "uicomWidgets/fileuploader.js", cb: createUploaderQuesAdd}]);
        }
    });
    rteHolderForAddDiagram = $(this).closest(".RTEHolder");
});
$(".addDiagramMainTabs .depthTab").live("click", function(e) {
    $(this).addClass("activeDepthTab").siblings().removeClass("activeDepthTab");
    $(this).closest(".addDiagramHolder").find("." + $(this).data("tabId")).removeClass("nonner")
            .siblings(".QAADContentDiv").addClass("nonner");
});
$(".QAADChooseDivTabs .blackTab").live("click", function(e) {
    $(this).addClass("activeBlackTab").siblings().removeClass("activeBlackTab");
    loadQADiagrams($(this));
});
$(".addDiagPreview").live("click", function(e) {
    showDiagramPreview($(this).data("imghtml"), $(this).data("imgTitle"), "Add");
});
$(".searchQADiagram").live("keyup", function(e) {
    if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
        loadQADiagrams($(this));
    }
});
var searchQAADUrl = "/questions/searchDiagram";
function loadQADiagrams($this) {
    var chooseDiv = $this.closest(".QAADChooseDiv");
    var LMHandlerDiv = chooseDiv.find(".LMHandlerDiv");
    var query = [], val = chooseDiv.find(".searchQADiagram").val().trim();
    if (val != "")
        query.push(val);
    query.push(chooseDiv.find(".activeBlackTab").data("query"));
    var params = {query: query.toString(), start: 0, size: 8};
    LMHandlerDiv.data("size", 10).data("urlStr", searchQAADUrl).data("allParams", params);
    vReq.get(searchQAADUrl, params, function(data) {
        LMHandlerDiv.html(data);
    });
}

//for uploading images for add question
var uploadQuesAdd, quesAddUploadFns, quesAddImagesQueue = [];
function createUploaderQuesAdd() {
    var page = rteHolderForAddDiagram.data("page");
    var action = "";
    if(page === "DISCUSSION" || page === "COMMENT"){
        action = "/UIComWidgets/uploadImageInContent";
    }else if(page === "MESSAGE"){
        action = "/UIComWidgets/uploadImageInComm";
    }
    else if(page === "CMDS-QUESTION"){
        action = "/UIComWidgets/uploadImageInCmds";
    }
    else{
        console.log("Unhandled page case");
        action = "/UIComWidgets/uploadImageInContent";
    }
    uploadQuesAdd = new qq.FileUploader({
        element: document.getElementById("file-uploadImageQues"),
        action: action,
        debug: true,
        multiple: false,
        sizeLimit: 5 * 1024 * 1024,
        allowedExtensions: ["jpg", "gif", "png", "jpeg"],
        params: {
            uploadFileParamName: "imageFile",
            myUserId: USERID
        }
    });
    uploadQuesAdd.onUploadDone = diagramUploadDone;
    uploadQuesAdd.onUploadProgress = diagramUploadProgress;
    var uploadDiv = $(uploadQuesAdd._element).closest(".QAADUploadDiv");
    var button = uploadDiv.find(".qq-upload-button").addClass("blueSubmitButton");
    button.find(":file").attr("accept", "image/*");
    uploadDiv.find(".qq-upload-list").addClass("nonner");
    uploadDiv.find(".qq-button-title").html("Choose Image File");
}
var diagramUploadProgress = function(percentDecimal, progressText, id, fileName) {
    var uploadBtnHolder = $(uploadQuesAdd._element);
    var uploadDiv = uploadBtnHolder.closest(".QAADUploadDiv");
    var input = uploadDiv.find("#addDiagUploadTextBox");
    input.val(fileName);
    uploadBtnHolder.hide();
    uploadDiv.find(".QAADStartUpload").addClass("QAADUploadNow");
    var percent = Math.round(percentDecimal * 100);
    uploadDiv.find(".QAADUploadingPercent").removeClass("nonner").text(percent + "%");
    uploadDiv.find(".QAADUploading").removeClass("nonner");
}
var diagramUploadDone = function(id, fileName, resp) {
    if (resp && resp.result && resp.result.uploaded) {
        //giveImgToRTE(result.imgHtml,false);
        $(".QAADImgPreview").html(resp.result.imgHtml);
    } else {
        console.error(result);
        showError("Uploading Failed. Please try again");
        closeQAADPopup();
    }
    $(".QAADUploadingPercent,.QAADUploading").addClass("nonner").text("");
}
$(".QAADAddImg").live("click", function() {
    if (!$(".QAADImgPreview").find("img").get(0)) {
        showError("No Image uploaded or Uploading is in progress!");
        return;
    }
    giveImgToRTE($(".QAADImgPreview").html(), false);
    closeQAADPopup();
});
function closeQAADPopup() {
    cancelCommonPopup();
    $("#diagramPreview").addClass("nonner");
    $("#errorPopupBlackOut").hide();
}
/*function prepareQAImgUpload(file){ 
 var uploadBtnHolder=$(uploadQuesAdd._element);
 var uploadDiv=uploadBtnHolder.closest(".QAADUploadDiv");
 var input=uploadDiv.find("#addDiagUploadTextBox");
 input.val(file.name);
 uploadBtnHolder.hide();       
 uploadDiv.find(".QAADStartUpload").addClass("QAADUploadNow");       
 }
 $(".QAADUploadNow").live("click",function(){
 var uploadDiv=$(uploadQuesAdd._element).closest(".QAADUploadDiv");
 var fileTitle=uploadDiv.find("#addDiagUploadTextBox").val().trim();
 var params={
 entityType:"DIAGRAM",
 fileType:"IMAGE"};
 if(fileTitle!="")params.fileTitle=fileTitle;       
 uploadQuesAdd.setParams({
 params:decodeURIComponent($.param(params))
 }); 
 uploadDiv.find(".QAADUploading").removeClass("nonner");    
 if(quesAddImagesQueue.length>0)quesAddUploadFns._onInputChange(quesAddImagesQueue.shift());
 });*/
function giveImgToRTE(src, addTag) {
    var rteHolder = rteHolderForAddDiagram;
    vRTE.insertImageInRTE(rteHolder, src, addTag);
    closeQAADPopup();
}
$(".addDiagAdd").live("click", function() {
    giveImgToRTE($(this).data("imghtml"), false);
});

//remove browse as user
$(document).on("click", ".removeBrowseAsUser", function() {
    vReq.post("/security/removeBrowseAsUser", {}, function(data) {
        window.location.reload();
    });
});
$(document).on("click", ".browseAsUser", function() {
    var $data = $(this).data();
    vReq.post("/security/browseAsUser", {targetUserId: $data.userId}, function(data) {
        if (data.errorCode) {
            showError("You do not have sufficient rights for this.");
        } else {
            window.location.reload();
        }
    });
});


//show more text
var condenseText = function(textDivs, maxLength) {
    textDivs.each(function(){
	var textDiv = $(this);
    	if (textDiv.length === 0) {
        	return;
    	}
    	var t = textDiv.text().trim();
    	textDiv.data("allText",t);
    	if (t.length > maxLength) {
        	var shownText = t.substring(0, maxLength);
        	var hiddenText = t.substring(maxLength);
        	var finalContent = shownText + "<label class='showMoreLessTextLabel'>...</label>\n\
        	<a class='seeMoreText'>See More</a><div class='nonner showMoreLessTextHidden'>" + hiddenText + "</div>";
        	textDiv.html(finalContent);
    	}
    });
};
$(document).on('click', '.seeMoreText', function() {
    $(this).removeClass("seeMoreText").addClass("hideMoreText").text("See Less");
    var hiddenText = $(this).siblings(".showMoreLessTextHidden").text();
    $(this).siblings(".showMoreLessTextLabel").text(hiddenText);
});
$(document).on('click', '.hideMoreText', function() {
    $(this).removeClass("hideMoreText").addClass("seeMoreText").text("See More");
    $(this).siblings(".showMoreLessTextLabel").text("...");
});
