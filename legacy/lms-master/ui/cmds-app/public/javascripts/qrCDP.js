var qrCDP=new(function($){
    var cdpPage,topMostParentPlanId;
    var clickEvent="click",bodyClickEvent="click.qrCDP";
    this.init=function(params){
        cdpPage=$("#cdpPage");
        resetCmdsPages($("#cmdsCDPs"));
        topMostParentPlanId=cdpPage.data("cdpId");
        fixTopHeadWidths(cdpPage);
        
        cdpPage.off()
        .on(clickEvent,".getCDPLibraryContent",getCDPLibraryContent)
        .on(clickEvent,".remFrmCDPLibrary",remFrmCDPLibrary)
        .on(clickEvent,".toggleCDPContent",toggleCDPContent)
        .on(clickEvent,".collapsecdpContent,.expandcdpContent",viewHidecdpContent)
        .on(clickEvent,".createdNodeCDP",createdNodeCDP)
        .on(clickEvent,".removeCDPSubNode,.removeCDPTopicNode,.removeCDPContentNode",removeCDPNode)
        .on(clickEvent,".cdpTopHead",cdpTopHead)
        .on(clickEvent,".publishCDP",publishCDP)
//        .on("mousedown",".dragItem",dragItemMouseDown);

        $("body").off(bodyClickEvent)
        .on(bodyClickEvent,".submitCreatedNodeCDP",submitCreatedNodeCDP)
        .on(bodyClickEvent,".confirmRemoveCDPNode",confirmRemoveCDPNode)
        
        var activeSub=cdpPage.find(".cdpTopHead.gButtonActive");
        if(activeSub.length>0){
            subChanged(activeSub);
        }
        fetchScripts([{fname:"qrProducts.js"}]);
        fixContentSec();
        var fixedSec=$("#fixedSec");
        fixedSec.height(fixedSec.height()-32);        
    }
    var fixTopHeadWidths=function(){
        var heads=cdpPage.find(".cdpTopHeads");
        var w=(cdpPage.find(".cdpTopHeads").width()-125)/heads.find(".cdpTopHead").length;
        heads.find(".cdpTopHead").width(w);
    }
    
    var publishCDP=function(){
        var $this=$(this);
        startLoader();
        var successFn=function(){
            stopLoader();
            $this.toggleClass("gRedButton publishCDP boldy linBlk").text("Published");
        }
        vReq.post("/qrcdpplans/publishCDP",{cdpId:$this.data("cdpId")},successFn);
    }
    var getCDPLibraryContent=function(){
        var libArea=cdpPage.find(".cdpLibraryContent");
        libArea.removeClass("nonner");
        inlineLoader(libArea);
        $(this).toggleClass("toggleCDPContent getCDPLibraryContent").text("Back To Content");
        cdpPage.find(".cdpPageContent").addClass("nonner");
        var successFn=function(data){
            libArea.html(data);
            fixContentSec();
        }
        vReq.get("/qrcdpplans/folderContent",{cdpId:cdpPage.data("cdpId")},successFn);
    }
    var remFrmCDPLibrary=function(){
        var contents=remContentFrmPkgCDPUtil().contents;
        var contentTrs=remContentFrmPkgCDPUtil().contentTrs;
        var params={contents:contents,cdpId:cdpPage.data("cdpId"),fromLibrary:true};
        var successFn=function(data){
            if(data.result.success===true){
                contentTrs.remove();
                resetcmdsCBoxes();
            }else{
                showcmdsError("There was some error. Please try again.");
            }            
        }
        vReq.post("/qrcdpplans/removeCDPContent",params,successFn);            
    }
    var toggleCDPContent=function(){
        $(this).toggleClass("toggleCDPContent getCDPLibraryContent").text("Reference Content");
        cdpPage.find(".cdpPageContent,.cdpLibraryContent").toggleClass("nonner");
    }
    
    
    
    
    
    var cdpTopHead=function(){
        subChanged($(this));
    }
    var subChanged=function($this){
        changeActiveClass($this,"gButtonActive");
        startLoader();
        var successFn=function(data){
            stopLoader();
            cdpPage.find(".cdpHeadContent").html(data);
        }
        vReq.get("/qrcdpplans/subjectPlan",{cdpId:$this.data("cdpId"),cdpName:$this.attr("title")},
        successFn);        
    }
    
    var cdpcollapseClasses="collapsecdpContent expandcdpContent"
    var viewHidecdpContent=function(){
        var $this=$(this);
        $this.siblings(".cdpTopicBody").slideToggle("slow",function(){
            $this.toggleClass(cdpcollapseClasses);
        });
    }
    var createdNodeCDP=function(){
        var popup=fillcmdsPopup("submitCreatedNodeCDP","createCDPSample");
        popup.find(".cdpRemTr").remove();        
        popup.find(".cmdsPopupHead").text($(this).text());
        var parentId=$(this).data("parentId");
        popup.find(".cdpParentPlanId").val(parentId);
    }
    var submitCreatedNodeCDP=function(){
        createTSPackageCDPUtil($(this),"/qrcdpplans/createCDP");      
    }
    
    
    //removing nodes and content
    var deleteNodeType,deleteBtnEl,deleteParams;
    var removeCDPNode=function(){
        var $this=$(this);        
        deleteNodeType=$this.data("nodeType");
        deleteParams={cdpId:$this.data("cdpId"),url:"/qrcdpplans/removecdp"};
        var popup=fillcmdsPopup("confirmRemoveCDPNode","removeCDPSample");
        popup.find(".remCDPText").text($this.attr("title"));
        deleteBtnEl=$this;
    }
    var confirmRemoveCDPNode=function(){
        startLoader();
        var successFn=function(){
            if(deleteNodeType=="SUBJECT"){
                var oldBtn=cdpPage.find(".cdpTopHead.gButtonActive");
                var newBtn=oldBtn.siblings(".cdpTopHead");
                if(newBtn.length>0){
                    subChanged(newBtn);
                    oldBtn.remove();
                }else{
                    refreshPage();
                }
            }else if(deleteNodeType=="TOPIC"){
                deleteBtnEl.closest(".cdpTopicDiv").remove();
            }else if(deleteNodeType=="CONTENT"){
                deleteBtnEl.closest(".cdpContentDiv").remove();
            }
            stopLoader();
            closePopup();
        }
        vReq.post(deleteParams.url,deleteParams,successFn);        
    }
    
    //draganddrop rearranging
    var dragItemMouseDown=function(e){
        e.stopPropagation();
        dragUtil.init(e,$(this),dragCDPElcbFn,resetBorderColors);
    }
    var borderClass="cdpDivBordered";    
    this.dragging={
//        CONTENT_MOVE:function(targetEl){
//            resetBorderColors();
//            targetEl.addClass(borderClass);
//        },
//        CONTENT_UP:function(targetEl){
//            resetBorderColors();
//            targetEl.addClass(borderClass);
//        }   
        TOPIC_MOVE:function(targetEl){
            resetBorderColors();
            targetEl.addClass(borderClass);
        },    
    }
    var resetBorderColors=function(){
        cdpPage.find("."+borderClass).removeClass(borderClass);
    }    
})(jQuery);


var dragUtil=new(function($){
    var droppables=[],dragTop,dragLeft,orgPosTop,orgPosLeft,matchCbfn,dragItem
    ,draggablePreview,dragPreviewHolder,draggleElsHolder,resetDragFn;
    this.init=function(e,elBeingDragged,cbfn,resetCbfn){        
        matchCbfn=cbfn;
        resetDragFn=resetCbfn;
        dragItem=elBeingDragged;
        dragPreviewHolder=$("#dragID");
        draggleElsHolder=elBeingDragged.closest(".draggleElsHolder");
        var dragPa=elBeingDragged,countLimiter=0;
        while(dragPa&&!dragPa.is(draggleElsHolder)){
            var sibs=dragPa.siblings(".dragItem");
            for(var p=0;p<sibs.length;p++){
                droppables.push(sibs.eq(p));
            }
            dragPa=dragPa.parents(".dragItem").first();
            
            //just to make sure inifinite loop does not run.
            countLimiter++;
            if (countLimiter>10){
                break;
            }
        }
        droppables.push(draggleElsHolder.eq(0));
        
        dragTop=dragItem.offset().top;dragLeft=dragItem.offset().left;
        var cloned=dragItem.clone();
        cloned.attr("id","draggable");
        $("#dragID").html(cloned);
        draggablePreview=$("#draggable").css("display","none");
        orgPosTop=e.pageY;
        orgPosLeft=e.pageX;
        e.preventDefault();
        bindMouseMove();
        bindMouseUp();        
    }
    var bindMouseMove=function(){
        $(document).bind('mousemove',function(e){
            if(e.pageY-$(window).scrollTop()<=100){
                $(window).scrollTop(e.pageY-100);
            }
            draggablePreview.css({display:"block",top:dragTop+(e.pageY-orgPosTop),
                left:dragLeft+(e.pageX-orgPosLeft)}).addClass("dragging");
            matchElsWithPtr(e,"MOVE");
        });        
    }
    var bindMouseUp=function(){
        $(document).bind('mouseup',function(e){
            $(document).unbind('mousemove');
            $(document).unbind('mouseup');
            dragPreviewHolder.html("");
            matchElsWithPtr(e,"UP");
            resetDragFn();
        });        
    }
    var matchElsWithPtr=function(e,mouseStatus){
        var elsLength=droppables.length;
        for(var k=0;k<elsLength;k++){
            var targetEl=droppables[k];
            var offy=targetEl.offset();
            if(offy!=null&&offy.left<=e.pageX&&(offy.left+targetEl.width())>=e.pageX&&
                    offy.top<=e.pageY&&(offy.top+targetEl.height()+20)>=e.pageY){
//                console.log(targetEl);
                 if(matchCbfn){
                     matchCbfn(targetEl,mouseStatus);
                 }
                 return false;
            }
        }
    }
})(jQuery);

var dragCDPElcbFn=function(targetEl,mouseStatus){
    var cbfn=qrCDP.dragging[targetEl.data("dragId")+"_"+mouseStatus];
    if(cbfn){       
        cbfn(targetEl);
    }    
}