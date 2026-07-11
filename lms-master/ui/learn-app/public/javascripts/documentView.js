var docView = new function(){
    var parDiv = "#resourceDocView";
    var frameLoadObj;
    var pdfViewerEmbed;
    var TIMEOUT_MILISEC = 60000;
    var errorMsg = "<div>Error Occured while loading the document</div>"
    this.init = function(){
        parDiv = $(parDiv);
        pdfViewerEmbed = parDiv.find(".pdfViewerEmbed");
        if(parDiv.data("inited") || !pdfViewerEmbed.get(0)) return;
        
        parDiv.on('click','.minimizeIcon',infoMinimize)
            .on('click','.maximizeIcon',infoMaximize);
        $(window).off('scroll',bodyScroll)
            .on('scroll',bodyScroll);
        showExtLoader();
        if(frameLoadTimeoutObj) clearTimeout(frameLoadTimeoutObj);
        frameLoadTimeoutObj = setTimeout(function(){
            frameLoad();
            hideExtLoader();
        },TIMEOUT_MILISEC);
        pdfViewerEmbed
            .height($(window).height())
            .load(frameLoad);
        parDiv.data("inited",true);
        parDiv.find(".docName").elipsifyText("boldy big20");
        parDiv.find("#docViewerNative").on("contextmenu",function(e){
            if(e)e.preventDefault();
            return false;
        });
    };
    var frameLoadTimeoutObj;
    var frameLoad = function(){
        //hideExtLoader();
        if(window.frames.length>0){
            var frameWindow = window.frames[0].window;
            frameWindow.onerror = onFrameError; 
        }
    };
    var supportedBrowsers = ["CHROME","FIREFOX"];
    var onFrameError = function(e){
        var browserInfo = vedantuClient.getInfo()[0];
        browserInfo = browserInfo.toUpperCase();
        if(supportedBrowsers.indexOf(browserInfo)<0){
            hideExtLoader();
            var error = errorMsg + "<div>Please try opening the document in other browser, like Google Chrome, Firefox.</div>";
            showError(error);
        }
        putConsoleError(e);
    };
    var hideExtLoader = function(){
        hideTopLoader();
        $("#loadingDocFrame").addClass("nonner");
    };
    var showExtLoader = function(){
        showTopLoader();
        $("#loadingDocFrame").removeClass("nonner");
    };
    var infoMinimize = function(){
        var $this = $(this);
                $(".docMetaInfo").animate({"max-height":"15px"},300,function(){
                    $this.addClass("nonner").siblings().removeClass("nonner");
                });
    };
    var infoMaximize = function(){
        var $this = $(this);
                $(".docMetaInfo").animate({"max-height":"400px"},300,function(){
                        $this.addClass("nonner").siblings().removeClass("nonner");
                }); 
    };
    var bodyScroll = function(e){
    };
    this.onScroll = function(e){
        var frame = parDiv.find(".docViewMain");
        var fTop = frame.position().top;
        var cTop = parDiv.position().top+$("#contentSectionHolder").position().top;
        fTop += cTop;
        var instHomeILE = $("#instituteHome");
        if(instHomeILE.get(0)){
            fTop += instHomeILE.position().top;
        }
        window.scrollTo(0,fTop);
    };
    this.updateStatus = function(state,val){
        switch(state){
            case "OPEN" : 
                break;
            case "INIT" : 
                showExtLoader();
                break;
            case "LOAD" : 
                if(frameLoadTimeoutObj) clearTimeout(frameLoadTimeoutObj);
                hideExtLoader();
                break;
            case "PROGRESS" : break;
            case "COMPLETE" : break;
            case "ERROR" :
                hideExtLoader();
                var error = "Error Occured while loading the document - "+val.message;
                var browserInfo = vedantuClient.getInfo()[0];
                browserInfo = browserInfo.toUpperCase();
                if(supportedBrowsers.indexOf(browserInfo)<0){
                    var error = errorMsg + "<div>Please try opening the document in other browser,";
                    error +=" like Google Chrome, Firefox.</div>";
                }
                // showError(error);
                $(".tryNativeViewer").click();
                putConsoleError(val);
                break;
            default : break;
        };
    };
};
