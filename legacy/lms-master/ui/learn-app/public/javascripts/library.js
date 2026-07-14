var libraryDivId = "#digitalLibrary";
function showLoader() {
    $("#topLoader").removeClass("nonner");
}
function hideLoader() {
    $("#topLoader").addClass("nonner");
}
function openInstSubPage(url,history,params,callBack,childHolderId){
    showLoader();
    // beforePageOpen();
    params["newPageOpen"] = pushInHistory(history);
    // params["newPageOpen"] = "/library";
    $.get(url,params,function(data){
        hideLoader();
        if(childHolderId){
            $(libraryDivId).find(childHolderId).html(data);
        }else{
            $(libraryDivId).html(data);
        }
        if(callBack){
            try{
                callBack(data);
            }catch(err){
                putConsoleError(err);
            }
        }
    });
    window.scrollTo(0,0);
};

function getInstPushUrl(orgId,append){
    append = append?"/"+append:"";
    var pushUrl = append;
    pushUrl = encodeURI(pushUrl);
    return pushUrl;
};

function pushInHistory(append){
    var orgId = $("#myInstitutePage").data("orgId");
    return pushInstHistory(orgId,append);
};

function pushInstHistory(orgId,append){
    var pushUrl = getInstPushUrl(orgId,append);
    pushHistory(null , null,pushUrl);
    return pushUrl;
};

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
};

var library = new function(){

    this.init = function(){
        $(".subject-header")
            .on("click",".individualSubject",individualSubject);
        $(".library-header")
            .on("change","#program",programChange);
        $(".subjectChapter-header")
            .on("change","#subject",subjectChange)
            .on("change","#program",programChangeInOtherPages);
        $(".chaptersTable")
            .on("click", ".individualChapter", individualChapter);
        $(".subjectContent-header")
            .on("change","#chapter",chapterChange)
            .on("change","#program",programChangeInOtherPages)
            .on("change","#subject",subjectChangeInOtherPages);
        $("#module-content-body tr")
            .on("click",openModule);
        $("#document-content-body")
            .off("click")
            .on("click",".card-body",openDocument);
        $(".testClass")
            .off("click")
            .on("click",openTest);
        $("#moduleEntriesHolder")
            .off("click")
            .on("click","#moduleHolder",openModuleOfType);
        $("#videoEntriesHolder")
            .off("click")
            .on("click","#videoHolder",openVideoNext);
        $(".postComment")
            .on('click','.submitComment',postComment);
    };

    var programChange = function(){
        $this = $(this);
        var programId = $this.find(':selected').data('programId');
        params = {
            programId : programId
        };
        openInstSubPage("/MyContents/changeProgram","library/program/"+programId,
            params,null,".subject-header");
    };

    var programChangeInOtherPages = function(){
        $this = $(this);
        var programId = $this.find(':selected').data('programId');
        params = {
            programId : programId
        };
        openInstSubPage("/Institute/library","library/program/"+programId,
            params,refreshSelectPicker);
    };

    var subjectChangeInOtherPages = function(){
        $this = $(this);
        var parentId = $this.find(':selected').data('subjectId');
        var programId = $this.find(':selected').data('programId');
        params = {
            parentId : parentId,
            programId: programId
        };
        openInstSubPage("/MyContents/subject","library/program/"+programId+"/subject/"+parentId,
            params,refreshSelectPicker);
    };

    var individualSubject = function(){
        $this = $(this);
        $data = $this.data();
        var programId = $(".library-header #program").find(':selected').data('programId');
        params = {
            programId : $data.programId,
            brdIds : [$data.subjectId],
            parentId : $data.subjectId
        };
        openInstSubPage("/MyContents/subject","library/program/"+programId+"/subject/"+$data.subjectId,
            params,refreshSelectPicker);
    };

    var subjectChange = function(){
        $this = $(this);
        var parentId = $this.find(':selected').data('subjectId');
        var programId = $this.find(':selected').data('programId');
        var subjectName = $this.find(':selected').text();
        var subjectSplit = subjectName.split(" ");
        const colors = {
            "Botany" : "#CDDC39",
            "Physics" : "#F44336",
            "Maths" : "#4CAF50",
            "Chemistry" : "#FF9800",
            "Zoology" : "#009688",
            "Mathematics" : "#4CAF50"
        }
        $(".subjectChapter-header").css('background-color', colors[subjectSplit[0]]);
        params = {
            parentId : parentId,
            programId: programId
        };
        openInstSubPage("/MyContents/changeSubject","library/program/"+programId+"/subject/"+parentId,
            params, refreshSelectPicker, ".chaptersTable");
    };

    var individualChapter = function(){
        $this = $(this);
        $data = $this.data();
        params = {
            programId : $data.programId,
            brdIds : [$data.chapterId],
            parentId: $data.subjectId,
            chapterId: $data.chapterId
        };
        openInstSubPage("/MyContents/chapter","library/program/"+$data.programId+"/subject/"+$data.subjectId+"/chapter/"+$data.chapterId,
            params,refreshSelectPicker);
    };

    var chapterChange = function(){
        $this = $(this);
        var parentId = $this.find(':selected').data('subjectId');
        var programId = $this.find(':selected').data('programId');
        var chapterId = $this.find(':selected').data('chapterId');
        params = {
            parentId : parentId,
            programId: programId,
            brdIds : [chapterId],
            chapterId: chapterId
        };
        openInstSubPage("/MyContents/chapter","library/program/"+programId+"/subject/"+parentId+"/chapter/"+chapterId,
            params, refreshSelectPicker);
    };

    var openModule = function(){
        $this = $(this);
        moduleId = $this.data("id");
        params = {
            id : moduleId
        };
        openInstSubPage("/MyContents/modulePage","module/"+moduleId,params);
    };

    var openTest = function(event,testId, attempted){
        var orgObj = $('#myInstitutePage').data("orgObj");
        var userRole = orgObj.userRole;
        $this = $(this);
        if(testId){
            testId=testId;
        }else{
            testId = $this.data("id");
        }
        if(attempted){
            attempted=attempted;
        }else{
            attempted = $this.data("attempted");
        }
        params = {
            id : testId,
            targetUserRole : userRole
        };

        if(userRole == "MANAGER" || userRole == "TEACHER" || attempted){
            openInstSubPage("/MyContents/testPage","test/"+testId,params,refreshSelectPicker);
        }else{
            openInstSubPage("/MyContents/preTestPage","pretest/"+testId,params,refreshSelectPicker);
        }
    };


    this.browser = function() {
        if (this.browser.prototype._cachedResult)
            return this.browser.prototype._cachedResult;

        // Opera 8.0+
        var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;

        // Firefox 1.0+
        var isFirefox = typeof InstallTrigger !== 'undefined';

        // Safari 3.0+ "[object HTMLElementConstructor]"
        var isSafari = /constructor/i.test(window.HTMLElement) || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || safari.pushNotification);

        // Internet Explorer 6-11
        var isIE = /*@cc_on!@*/false || !!document.documentMode;

        // Edge 20+
        var isEdge = !isIE && !!window.StyleMedia;

        // Chrome 1+
        var isChrome = !!window.chrome && !!window.chrome.webstore;

        // Blink engine detection
        var isBlink = (isChrome || isOpera) && !!window.CSS;

        return this.browser.prototype._cachedResult =
            isOpera ? 'Opera' :
            isFirefox ? 'Firefox' :
            isSafari ? 'Safari' :
            isChrome ? 'Chrome' :
            isIE ? 'IE' :
            isEdge ? 'Edge' :
            "Don't know";
    };

    var openDocument = function(event,documentId,moduleId){
        $this = $(this);
        if(documentId){
            documentId=documentId;
        }
        else{
        documentId = $this.data("id");
        }
        params = {
            id : documentId,
            moduleId : moduleId
        };
        openInstSubPage("/MyContents/documentPage","document/"+documentId,params);
    };

    var openVideo = function(event,videoId,moduleId){
        $this = $(this);
        if(videoId)
        {
            videoId= videoId;
        }
        else
        {
        videoId = $this.data("id");
    }
        params = {
            id : videoId,
            moduleId : moduleId
        };
        openInstSubPage("/MyContents/videoPage","video/"+videoId+"?moduleId="+moduleId,params);
    };

    var refreshSelectPicker = function(){
        $(".selectpicker").selectpicker({});
        return;
    };

    var openModuleOfType= function(){
        $this = $(this);
        id= $this.data("entityId");
        moduleId= $this.data("moduleId");
        type= $this.data("entityType");
        attempted= $this.data("entityAttempted");
        if(type=='TEST')
        {
           openTest(null,id,attempted);
        }
        else if(type=='DOCUMENT')
        {
            openDocument(null,id,moduleId);
        }
        else if(type=='VIDEO')
        {
            openVideo(null,id,moduleId);
        }
    };

    var openVideoNext = function(){
        $this = $(this);
        id= $this.data("entityId");
        moduleId= $this.data("moduleId");
        openVideo(null,id,moduleId);
    };

    var postComment = function(){
        var holder = ".wi-comments .list-group";
        var text = $.trim($('.commentText').val());
        $('.commentText').val('');
        if(text.length == 0){
            text = $.trim($('.commentTextLargeScreen').val());
            $('.commentTextLargeScreen').val('');
            if(text.length == 0){
                return;
            }
        }
        $this = $(this);
        var videoId = $this.data("id");
        var params = {
            "parent.type" : "VIDEO",
            "base.type" : "VIDEO",
            "root.type" : "VIDEO",
            "root.id" : videoId,
            "base.id" : videoId,
            "parent.id" : videoId,
            "content" : text
        };
        $.post("/Widgets/postComment",params,function(data){
            if(data && data.errorCode=="" && data.result){
                var p = {
                    content:text,
                    id:data.result.id,
                    timeCreated:new Date().getTime()
                };
                vReq.get("/Institute/singleComment",p,function(data){
                    $(holder).append(data); 
                });
            }
            $this.trigger("delete");
        });
    };
};
