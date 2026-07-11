var vPlaylist=new(function($){
    this.init=function(params){
        viewPageType="PL_VIEW";
        $("body").addClass("PL_DOC_VIEW");
        var DTPL;
        if(params.viewType=="PL_EDIT"){
            DTPL=$("#DTDiv_PL_EDIT");    
        }else{
            DTPL=$("#PLViewPage");            
        }
        initPlaylist(DTPL,params);            
    }
})(jQuery)
onWindowScroll["PL_VIEW"]=function(scrollType){
    playlistTOCVar.markActiveSec("PL_VIEW");
}
    
function initPlaylist(DTPL,params){
      var DTPL1002=DTPL.children(".DTPL1002");
      playlistTopBarVar.initPLTopBarFns(DTPL);
      playlistTOCVar.initPLTOCFns(DTPL1002.children(".DTPLLeft"));
      playlistCurationVar.initPLCurationFns(DTPL1002.children(".DTPLMiddle"));
      playlistViewVar.initPlaylistViewFns(DTPL);
      $(document).on('click',".confirmForcePLOpt",playlistCurationVar.confirmForcePLOptClick);
      $(document).on('click',".playPLVideo",playlistCurationVar.playPLVideoClick);
      $(document).on('click',".makePLPublic",playlistTopBarVar.makePLPublicClick);

      var PLMiddle=DTPL1002.children(".DTPLMiddle");
      if(params.topicCount){
          $.get("/playlists/PLTopic",{playlistId:params.playlistId,topicId:params.topicId,viewType:params.viewType},function(data){
                PLMiddle.html(data);
                loadMJEqns(PLMiddle.get(0));           
           });
      }
      else {
          PLMiddle.html("<div class='userMessage'>There are no contents this playlist</div>");
      }
      DTPL.find(".PLTOC .activePLTopic").find(".PLTOCSec").first().addClass("activeTOCTopic");
      if(params.viewType=="PL_VIEW"){
          var DTPLMiddle=DTPL1002.children(".DTPLMiddle");
          DTPLMiddle.find(".PLCompo").first().addClass("activePLCompo");
          var DTPLLeft=DTPL1002.children(".DTPLLeft");
          var topBars=$("#topBarHolder").outerHeight(true);
          var leftSecContents=DTPLLeft.children(".DTPLLeftBody").height()-DTPLLeft.find("#leftSecContent").height();
          var leftSecHeight=$(window).height()-(topBars+leftSecContents);
          DTPLLeft.find("#leftSecContent").height(leftSecHeight-10);//10 for margin at the bottom

          playlistTopBarVar.topicHeadHeight=DTPLMiddle.children(".PLTopicHead").height();
      }
}



var playlistTopBarVar={
     topBarHeight:$("#topBarHolder").height(),
     topicHeadHeight:0,

    //utilities for basic operations on playlist like creating and editing names of playlist and saving and publishing
    initPLTopBarFns:function(DTPL){
        DTPL.on('blur',".PLTopicName",this.topicNameChangeEvent);
        DTPL.on('blur',".PLNameChange",this.PLNameChangeEvent);
        DTPL.on('click',".addTagsToPL",this.addTagsToPLClick);        
        DTPL.on('click',".closePLAndView",this.closePLAndViewClick);
    },
    topicNameChangeEvent:function(){
           var $this=$(this),PLTopicHead=$this.closest(".PLTopicHead");
           var params={};
           params.heading=$this.val();params.playlistId=PLTopicHead.attr("lang");params.topicId=PLTopicHead.attr("rel");
           params.urlStr="/playlists/updateTopic";
           playlistTopBarVar.PLNameChange(params,$this,PLTopicHead.closest(".DTPL1002").find('.PLTOC .activePLTopic:first .PLTTName'));
    },
    PLNameChangeEvent:function(){
           var $this=$(this),params={};
           params.title=$this.val();params.playlistId=$this.closest(".PLTopBarHolder").attr("rel");
           params.urlStr="/playlists/updatePL";
           playlistTopBarVar.PLNameChange(params,$this,undefined);
    },
    PLNameChange:function(params,$this,tocEl){
       var h=params.heading||params.title;
       if(h!=""&&h!=$this.attr("rel")){
           $this.attr("rel",h);
            $.post(params.urlStr,params,function(data){
                if(checkJSONResponse(data)){
                    if(tocEl!=undefined)tocEl.text(h);
                }
            });
       }
       else if(h==""){
           showError("This field cannot be left empty");
           $this.val($this.attr("rel"));
       }
    },
    addTagsToPLClick:function(){
        var popup=getCommonPopupBody(650);
       popup.html($(this).closest(".PLTopBarHolder").siblings("#PLCurateAddTagsPopup").outerHTML());
       popup.children("#PLCurateAddTagsPopup").removeClass("nonner");
       popup.append("<div class='margTop20 centry'><span class='greenButton makePLPublic' \n\
        data-pl-id='"+$(this).data("plId")+"'>Submit</span></div>");
    },
    makePLPublicClick:function(){
        var $this=$(this),playlistId=$this.data("plId"),name=$this.data("title");
        var popup=$("#commonPopupHolder #PLCurateAddTagsPopup");
        var allTags=returnAllTagsAdded(popup.children("#addTagsMasterDiv"));
        if(allTags.subjectIds.length==0){
            showError("Please add atleast one subject");
            return;
        }
        $.post("/playlists/updatePL",{playlistId:playlistId,targetIds:allTags.targetIds,
            brdIds:allTags.brdIds,tags:allTags.normalTags},function(data){
            if(checkJSONResponse(data)){
                $.post("/playlists/updatePLStatus",{playlistId:playlistId});
                playlistTopBarVar.closeEditPLAndViewPL(playlistId,name);
            }
        });
    },
    closePLAndViewClick:function(){
        var $this=$(this),playlistId=$this.data("plId"),name=$this.data("title");
        playlistTopBarVar.closeEditPLAndViewPL(playlistId,name);       
    },
    closeEditPLAndViewPL:function(PLId,PLName){
        tabClose("PL_EDIT");
        openPLInViewer(PLId,PLName);
        pushHistory(null,null,"/playlist/"+PLId);
    }
}


var playlistTOCVar={
     reqFiredForAddReq:false,
    //toc management
    initPLTOCFns:function(DTPLLeft){
        DTPLLeft.on('click',".PLTTNameDiv",this.PLTTNameDivClick);
        DTPLLeft.on('click',".activatePLTOCSec",this.PLTOCSecClick);
        DTPLLeft.on('click',".addNewPLTopic",this.addTopicClick);        
        DTPLLeft.on('blur',".newTopicInput",this.newTopicInputBlur);
        DTPLLeft.on('keyup',".newTopicInput",this.newTopicInputEnter);
        DTPLLeft.on('mousedown',".dragTopicSec",this.dragTopicSecEvent);
        DTPLLeft.on('click',".showPLContents",this.showPLContentsClick);
        DTPLLeft.on('click',".showPLInfo",this.showPLInfoClick);
        DTPLLeft.on('click',".showPLReviews",this.showPLReviewsClick);
    },
    PLTTNameDivClick:function(){
        playlistTOCVar.activateTopic($(this),"");
    },
    PLTOCSecClick:function(){
        var PLMiddle=$(this).closest(".DTPL1002").children(".DTPLMiddle");
        var PLTOCTopic=$(this).closest(".PLTOCTopic");
        if(PLTOCTopic.hasClass("activePLTopic"))playlistTOCVar.activateTOCSec(PLMiddle,$(this).attr("rel"));
        else playlistTOCVar.activateTopic($(this).siblings(".PLTTNameDiv"),$(this).attr("rel"));
    },
    addTopicClick:function(){
        playlistTOCVar.reqFiredForAddReq=false;
        var PLTOC=$(this).closest(".PLTOC");
        if(PLTOC.children(".PLTOCTopicAddBlock").length==0){
            var activeGrp=PLTOC.find(".PLTOCTopic").last();
            if(activeGrp.length==0)activeGrp=PLTOC.find(".PLTOCHeading");
            var num=parseInt(activeGrp.find(".PLTTNo").text()),newNum=num+1;
            var el=makeHTMLTag("div");
            el.html("<div class='PLTOCTopic PLTOCTopicAddBlock'><div class='boldy PLTTNameDivDummy' lang='PLTOCTOPIC'>\n\
            <label class='PLTTNo margRight5'>"+newNum+"</label>.<span class='PLTTName'>\n\
            <input type='text' placeHolder='Enter Topic Name' class='newTopicInput'/>\n\
            </span></div></div>");
            $(el.children()).insertAfter(activeGrp);
            PLTOC.find(".newTopicInput").focus();
        }
    },
    newTopicInputBlur:function(){
            var thisVar=$(this),PLTOCTopic=$(this).closest(".PLTOCTopic");
            if(thisVar.val().trim()==""){
                PLTOCTopic.remove();
            }
    },
    newTopicInputEnter:function(e){
            var thisVar=$(this);
            if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))&&thisVar.val()!=""){
                thisVar.closest(".PLTOCTopic").removeClass("PLTTNameDivDummy").addClass("PLTTNameDiv dragTopicSec");
                playlistTOCVar.addNewTopic(thisVar);
            }
    },
    showPLContentsClick:function(){
        playlistTOCVar.reorderPL_VIEW_Tabs($(this),"#DPLSTOC","#DPLSTOC");
    },
    showPLInfoClick:function(){
        var params={entityType:"PLAYLIST",entityId:$(this).closest(".DTPLLeftBody").data("plId"),start:0,size:5};
        playlistTOCVar.reorderPL_VIEW_Tabs($(this),"#DPLSInfo","#DPLSInfo .PLInfoPeoplesHolder","/playlists/getPLPeople",params);
    },
    showPLReviewsClick:function(){
      var params={rootId:$(this).closest(".DTPLLeftBody").data("plId"),rootType:"PLAYLIST",type:"REVIEW",
            start:0,size:10,orderBy:"timeCreated"};
        playlistTOCVar.reorderPL_VIEW_Tabs($(this),"#DPLSReviews","#DPLSReviews .LMHandlerDiv","/widgets/reviewItems",params);
    },
    reorderPL_VIEW_Tabs:function($this,nonnerDivName,targetDivName,urlStr,params){
       var a="leftSecActiveTab",DTPLLeftBody=$this.closest(".DTPLLeftBody");
        $this.addClass(a).siblings().removeClass(a);
        var nonnerDiv=DTPLLeftBody.find(nonnerDivName);
        var targetDiv=DTPLLeftBody.find(targetDivName);
        nonnerDiv.removeClass("nonner").siblings(".PLContentDiv").addClass("nonner");
        var allParams={};
        if(params)allParams=params;
        allParams.playlistId=DTPLLeftBody.data("plId");
        if(targetDiv.html()==""&&urlStr){
            $.get(urlStr,allParams,function(data){
                targetDiv.html(data);
                var LMHandlerDiv=targetDiv.find(".LMHandlerDiv");
                if(LMHandlerDiv.length>0){
                    LMHandlerDiv.data("urlStr",urlStr).data("size",15).data("allParams",params);
                }
            });
        }
    },


    activateTOCSec:function(PLMiddle,secRel){
        var scrollVal,plCompo=PLMiddle.findFirst("#sec_"+secRel);
        PLMiddle.closest(".DTPL1002").children(".DTPLMiddle");
        var topicHead=PLMiddle.children(".PLTopicHead");
        if(plCompo.attr("id")==PLMiddle.children(".PLCompo").not(".PLWrite").first().attr("id"))scrollVal=0;
        else scrollVal=plCompo.position().top;
        //PLTopScrollableHeight() assumed 0
        if(topicHead.position().top==0)scrollVal=scrollVal-topicHead.outerHeight(true);
        var secEl=PLMiddle.siblings(".DTPLLeft").find(".PLTOCSec[rel="+secRel+"]");
        PLMiddle.addClass("activatingSec");
        playlistTOCVar.assignActiveClass(secEl,plCompo);
        animateScrollBar(scrollVal,null,removeActivatingClass);
    },
    assignActiveClass:function(secEl,plCompoEl){
        secEl.addClass("activeTOCTopic").siblings().removeClass("activeTOCTopic");
        plCompoEl.addClass("activePLCompo").siblings().removeClass("activePLCompo");
    },
    addNewTopic:function(newTopicInput){
        var PLTOC=newTopicInput.closest(".PLTOC"),heading=newTopicInput.val();
        PLTOC.children(".PLTOCTopicAddBlock").removeClass("PLTOCTopicAddBlock");
        $.post("/playlists/addTopic",{playlistId:PLTOC.attr("rel"),heading:heading},function(data){
            if(checkJSONResponse(data)){
                var PLTTNameDiv=newTopicInput.closest(".PLTTNameDiv");
                PLTTNameDiv.attr("rel",data.result.topicId);
                newTopicInput.parent().html(heading);
                playlistTOCVar.activateTopic(PLTTNameDiv,"");
            }
        });
    },
    assignActiveTOC:function(PLTOC,el){
        PLTOC.find(".activeTOCTopic").removeClass("activeTOCTopic");
        el.addClass("activeTOCTopic");
    },
    activateTopic:function(PLTTNameDiv,secRel){
        PLTTNameDiv.parent().addClass("activePLTopic").siblings().removeClass("activePLTopic");
        var PLTOC=PLTTNameDiv.closest(".PLTOC"),PLMiddle=PLTOC.closest(".DTPL1002").find(".DTPLMiddle");
        PLTOC.find(".editingTOC").removeClass("editingTOC");
        var topicId=PLTTNameDiv.attr("rel");        
        playlistTOCVar.assignActiveTOC(PLTOC,PLTTNameDiv);
        var tabType=j[j.length-1].tabType;
        $.get("/playlists/PLTopic",{playlistId:PLTOC.attr("rel"),topicId:topicId,viewType:tabType},function(data){
            var tNo=PLTTNameDiv.find(".PLTTNo").text();
            PLMiddle.html(data);
            PLMiddle.find(".PLTopicHead .PLTopicNo").text(tNo);
            if(secRel!="")playlistTOCVar.activateTOCSec(PLMiddle,secRel);
            if(tabType=="PL_VIEW"){
                PLTTNameDiv.next(".PLTOCSec").addClass(".activeTOCTopic");
            }
            loadMJEqns(PLMiddle.get(0));           
        });
        $(window).scrollTop(0);
    },
    getSecTopicId:function(sec){
        return sec.closest(".PLTOCTopic").children(".PLTTNameDiv").attr("rel");
    },
    returnActiveTOC:function(PLMiddle,secId){
        var PLTOCTopic=PLMiddle.closest(".DTPL1002").findFirst(".DTPLLeft .activePLTopic");
        var PLTOCSec=PLTOCTopic.findFirst(".PLTOCSec[rel="+secId+"]");
        return {PLTOCTopic:PLTOCTopic,PLTOCSec:PLTOCSec};
    },
    markActiveSec:function (){
        var PLRel="",PLMiddle=$(".DTPLMiddle"),
        plTopScrollHeight=0;
        //PLTopScrollableHeight(); is zero
        if(PLMiddle.hasClass("activatingSec"))return;
        var PLCompos=PLMiddle.children(".PLCompo").not(".PLWrite");
        if($(window).scrollTop()>plTopScrollHeight){
            var middleLineMarker=(($(window).height()-(playlistTopBarVar.topicHeadHeight+playlistTopBarVar.topBarHeight))/3);
            var windowScrollPos=$(window).scrollTop()-plTopScrollHeight;
            var currentMarkerPos=middleLineMarker+windowScrollPos;           
            PLCompos.each(function(){
               var comp=$(this).position(),prevComp=$(this).prev(".PLCompo").not(".PLWrite");
               if(comp.top>currentMarkerPos&&prevComp.length>0){

                   PLRel=prevComp.attr("id").substring(4);
                   return false;
               }
            });
            if(PLRel==""&&PLCompos.last().length>0&&PLCompos.last().position().top<currentMarkerPos&&(PLCompos.last().position().top+PLCompos.last().outerHeight(true))>currentMarkerPos){
                PLRel=PLCompos.last().attr("id").substring(4);
            }
        }
        var PLTOCTopic=$(".DTPLLeft .activePLTopic");
        var secEl=PLTOCTopic.find(".PLTOCSec[rel="+PLRel+"]");
        var plCompoEl=PLMiddle.children("#sec_"+PLRel);
        if(secEl.length==0||plCompoEl.length==0){
            secEl=PLTOCTopic.findFirst(".PLTOCSec");
            plCompoEl=PLCompos.first();
        }
        playlistTOCVar.assignActiveClass(secEl,plCompoEl);
    },
    afterAddingSec:function(PLMiddle,params){
            var PLTOCTopic=playlistTOCVar.returnActiveTOC(PLMiddle,params.sectionId).PLTOCTopic;
            var secText="<div class='PLTOCSec dragTopicSec' rel='"+params.sectionId+"' lang='PLSEC'>\n\
            <span class='PLSecHeadIcon PLSecHeadIcon"+params.secType+"'>\n\
            </span><span class='headName'>"+params.heading+"</span></div>";
            var d=params.afterSectionId,sec=PLTOCTopic.find(".PLTOCSec[rel="+params.afterSectionId+"]");
            if(d==-1)$(secText).insertAfter(PLTOCTopic.find(".activePLTopic .PLTTNameDiv"));
            else if(d!=undefined)$(secText).insertAfter(sec);
            else sec.find(".headName").text(params.heading);
            playlistTOCVar.markActiveSec(PLMiddle.attr("rel"),"PL_EDIT");
    },

    //drag functions of topic and sections
    orgPosTop:0,
    orgPosLeft:0,
    dragTop:0,
    dragLeft:0,
    isDragged:false,
    dragTopicSecEvent:function(e){
            dragTop=$(this).offset().top;dragLeft=$(this).offset().left;
            var cloned=$(this).clone();
            cloned.attr("id","draggable");
            $("#dragID").html(cloned);
            $("#draggable").css("display","none");
            orgPosTop=e.pageY;
            orgPosLeft=e.pageX;
            playlistTOCVar.bindDocMouseMove($(this).attr("lang"),$(this));
            playlistTOCVar.bindDocMouseUp($(this).attr("lang"),$(this));
    },
    bindDocMouseMove:function(target,thisVar){
        $(document).bind('mousemove',function(e){
            if(e.pageY-$(window).scrollTop()<=100){
                $(window).scrollTop(e.pageY-100);
            }
            $("#draggable").css("display","block").css("top",dragTop+(e.pageY-orgPosTop)).css("left",dragLeft+(e.pageX-orgPosLeft)).addClass("dragging");
            playlistTOCVar.topicSecDrag[target](thisVar,e,"MOVE");
        });
    },
    bindDocMouseUp:function(target,thisVar){
        $(document).bind('mouseup',function(e){
            $(document).unbind('mousemove');
            $(document).unbind('mouseup');
            $("#dragID").html("");
            playlistTOCVar.topicSecDrag[target](thisVar,e,"UP");
        });
    },
    topicSecDrag:{
        PLTOCTOPIC:function(PLTTNameDiv,e,path){
            var PLTOC=PLTTNameDiv.closest(".PLTOC"),PLTOCTopic=PLTTNameDiv.closest(".PLTOCTopic");
            PLTOC.find(".PLTOCTopic,.PLTOCHeading").not(PLTOCTopic).each(function(){
               var offy=$(this).offset();
               if(offy!=null&&offy.left<=e.pageX&&(offy.left+$(this).width())>=e.pageX&&offy.top<=e.pageY&&(offy.top+$(this).height()+20)>=e.pageY){
                   playlistTOCVar.topicSecDrag["PLTOCTOPIC_"+path](PLTOC,PLTOCTopic,$(this));
                    return false;
               }
               else $(this).removeClass("topicBordered");
            });
        },
        PLTOCTOPIC_MOVE:function(PLTOC,PLTOCTopic,thisVar){
            PLTOC.find(".topicBordered").removeClass("topicBordered");
            thisVar.addClass("topicBordered");
        },
        PLTOCTOPIC_UP:function(PLTOC,PLTOCTopic,thisVar){
            var afterId=thisVar.children(".PLTTNameDiv").attr("rel");
            if(afterId==undefined)afterId=-1;
                $.post("/playlists/moveTopic",{playlistId:PLTOC.attr("rel"),topicId:PLTOCTopic.children(".PLTTNameDiv").attr("rel"),afterTopicId:afterId},function(data){
                    checkJSONResponse(data);
                });
                   var num=1;
                   $(PLTOCTopic.outerHTML()).insertAfter(thisVar);
                   PLTOCTopic.remove();
                   PLTOC.find(".PLTOCTopic").each(function(){
                       $(this).find(".PLTTNo").text(num);
                       num++;
                   });
                   PLTOC.find(".topicBordered").removeClass("topicBordered");
        },
        PLTOCSEC:function(PLTOCSec,e,path){
            var PLTOC=PLTOCSec.closest(".PLTOC");
            PLTOC.find(".PLTOCTopic").each(function(){
                var offy=$(this).offset(),topic=$(this);
               if(offy!=null&&offy.left<=e.pageX&&(offy.left+topic.width())>=e.pageX&&offy.top<=e.pageY&&(offy.top+topic.height()+20)>=e.pageY){
                   var bordy=false;
                   $(this).children().not(PLTOCSec).each(function(){
                       var sec=$(this),subOffy=sec.offset();
                       if(subOffy!=null&&subOffy.left<=e.pageX&&(subOffy.left+sec.width())>=e.pageX&&subOffy.top<=e.pageY&&(subOffy.top+sec.height())>=e.pageY){
                          playlistTOCVar.topicSecDrag["PLTOCSEC_"+path](PLTOC,PLTOCSec,sec);
                          bordy=true;
                          return false;
                       }
                   });
                   if(!bordy&&topic.find(".PLTTNameDiv").attr("rel")!=PLTOCSec.siblings(".PLTTNameDiv").attr("rel"))
                       playlistTOCVar.topicSecDrag["PLTOCSEC_"+path](PLTOC,PLTOCSec,topic.children().last());
                   return false;
               }
               else PLTOC.find(".secBordered").removeClass("secBordered");
            });
        },
        PLTOCSEC_MOVE:function(PLTOC,PLTOCSec,sec){
              PLTOC.find(".secBordered").removeClass("secBordered");
              sec.addClass("secBordered");
        },
        PLTOCSEC_UP:function(PLTOC,PLTOCSec,sec){
            var topicId=playlistTOCVar.getSecTopicId(PLTOCSec),afterSecId=sec.attr("rel");
            if(sec.hasClass("PLTTNameDiv"))afterSecId=-1;
                $.post("/playlists/moveSection",{playlistId:PLTOC.attr("rel"),topicId:topicId,
                sectionId:PLTOCSec.attr("rel"),afterSectionId:afterSecId,targetTopicId:playlistTOCVar.getSecTopicId(sec)},
                function(data){
                    checkJSONResponse(data);
                });
            PLTOC.find(".secBordered").removeClass("secBordered");
            $(PLTOCSec.outerHTML()).insertAfter(sec);
            PLTOCSec.remove();
        }
    }
}










var playlistCurationVar={
    PLWriteHTML:"",
    PLAQHTML:"",
    PLATHTML:"",
    RTEHTML:"",
    insertAfterSec:undefined,
    plmiddleForConfirmCases:undefined,
    initPLCurationFns:function(PLMiddle){
        PLMiddle.on('click',".PLSecHead",this.PLSecHeadClick);
        PLMiddle.on('click',".addPLSectionBottom,.addPLSectionAfter,.addPLSectionTop,.editPLSection",this.insertPLWriteClick);
        //for view pl
        PLMiddle.on('click',".PLCommentButton",this.PLCommentButtonClick);
        $(document).on('click',".abandonPLSecAndEDIT",this.abandonPLSecAndEDITClick);
        $(document).on('click',".doNotAbandonPLSec",this.doNotAbandonPLSecClick);
        $(document).on('click',".abandonPLSecAndINSERT",this.abandonPLSecAndINSERTClick);

        var DTDIV=$("#DTDiv_PL_EDIT");
        var el=DTDIV.children(".PLWriteDummyHTML");
        this.PLWriteHTML=el.html();
        this.RTEHTML=el.find(".PLWriteBody").html();
        el.remove();

        var PLAQEl=DTDIV.children(".PLAQTDummyHTML");
        this.PLAQHTML=PLAQEl.html();
        PLAQEl.find(".PLAQSortQuesns").remove();
        PLAQEl.find(".LMHandlerDiv").addClass("PLAddTestsList").removeClass("PLAddQuesnsList");
        PLAQEl.find(".PLSearchQTInput").attr("placeholder","Search Tests");
        this.PLATHTML=PLAQEl.html();
        PLAQEl.remove();
    },




    PLSecHeadClick:function(){
        $(this).closest(".PLCompo").toggleClass("PLCompoOpened");
    },
    insertPLWriteClick:function(e){
        e.stopPropagation();
        var PLMiddle=$(this).closest(".DTPLMiddle"),afterSec=$(this).closest(".PLCompo"),task="INSERT";
        if($(this).hasClass("addPLSectionTop"))
          afterSec=PLMiddle.children(".PLTopicHead");
        else if($(this).hasClass("addPLSectionBottom"))
          afterSec=PLMiddle.children(".PLCompo").last();
        else if($(this).hasClass("editPLSection"))task="EDIT"
        var PLWrite=PLMiddle.children(".PLWrite");
        if(PLWrite.length>0&&(PLWrite.attr("id")!=""||PLWrite.find(".PLWriteHead").val()!="")){
            playlistCurationVar.plmiddleForConfirmCases=PLMiddle;playlistCurationVar.insertAfterSec=afterSec;
            playlistCurationVar.PLConfirmPopup("Do you want to abandon the "+PLWrite.find(".PLWriteHead").val()+" section\n\
             you are already composing ?","abandonPLSecAnd"+task,"doNotAbandonPLSec");
        }
        else if(task=="INSERT")playlistCurationVar.insertPLWrite(PLMiddle,afterSec);
        else if(task=="EDIT")playlistCurationVar.editPLSection(PLMiddle,afterSec);
        PLMiddle.children(".PLAddBtnBtmDiv").addClass("nonner");
    },
    focusPLWriteHead:function(PLWrite){
        $(window).scrollTop(PLWrite.offset().top-100);
        PLWrite.find(".PLWriteHead").focus();
    },
    doNotAbandonPLSecClick:function(){
        var PLMiddle=playlistCurationVar.plmiddleForConfirmCases;
        playlistCurationVar.focusPLWriteHead(PLMiddle.children(".PLWrite"));
        cancelCommonPopup();
    },
    abandonPLSecAndINSERTClick:function(){
        var PLMiddle=playlistCurationVar.plmiddleForConfirmCases,afterSec=playlistCurationVar.insertAfterSec;
        PLMiddle.children(".nonner").removeClass("nonner");
        playlistCurationVar.insertPLWrite(PLMiddle,afterSec);
        cancelCommonPopup();
    },
    abandonPLSecAndEDITClick:function(){
        var PLMiddle=playlistCurationVar.plmiddleForConfirmCases,afterSec=playlistCurationVar.insertAfterSec;
        PLMiddle.children(".nonner").removeClass("nonner");
        playlistCurationVar.editPLSection(PLMiddle,afterSec);
        cancelCommonPopup();
    },
    insertPLWrite:function(PLMiddle,afterSec){
        playlistCurationVar.PLWriteRemove(PLMiddle.find(".PLWrite"));
        $(playlistCurationVar.PLAddContentHTML(PLMiddle)).insertAfter(afterSec);
        var PLWrite=PLMiddle.find(".PLWrite");
        PLWrite.data("secType","TEXT");
        playlistCurationVar.initAddEditContentFns(PLWrite);
        $(window).scrollTop(PLWrite.offset().top-100);        
    },
    editPLSection:function(PLMiddle,afterSec){
        playlistCurationVar.insertPLWrite(PLMiddle,afterSec);
        var PLWrite=PLMiddle.findFirst(".PLWrite");
        var secType=afterSec.data("secType");
        playlistCurationVar.assignActivePLOpt(PLWrite,secType);
        PLWrite.find(".PLWriteOptions .PLOpt").remove();
        afterSec.addClass("nonner");
        var secId=afterSec.attr("id").substring(4);
        PLWrite.attr("id","updatePLSec_"+secId).addClass("PLWriteEDIT_MODE").data("secType",secType);
        playlistTOCVar.returnActiveTOC(PLMiddle,secId).PLTOCSec.addClass("editingTOC");

        //filling info
        PLWrite.find(".PLWriteHead").val(afterSec.find(".PLSecHeadText").text());
        playlistCurationVar.playlistEdit[secType](PLWrite,afterSec);
        playlistCurationVar.focusPLWriteHead(PLWrite);
    },
    playlistEdit:{
        TEXT:function(PLWrite,afterSec){
            PLWrite.find(".PLWriteBody").html(playlistCurationVar.RTEHTML);
            vRTE.init(PLWrite.find(".RTEHolder"));
            var content=afterSec.find(".PLSecContent").html();
            PLWrite.find(".RTEArea").html(content);
        },
        QUESTION:function(PLWrite,afterSec){
            var quesns=[],quesIds=[];
            afterSec.find(".PLSecBody .ques").each(function(){
                quesns.push($(this));
                quesIds.push($(this).data("qid"));
            });
            playlistCurationVar.initAddQuesPL(PLWrite,quesns,quesIds);
        },
        IMAGE:function(PLWrite,afterSec){
            playlistCurationVar.createUploaderPL(PLWrite);
            var PLTableBody=PLWrite.find(".PLUploadTable tbody");
            var imgHTML="";
            afterSec.find(".PLSecImg").each(function(){
                imgHTML+="<tr class='deletexPa PLSecImgTr' rel='"+$(this).attr("src")+"'><td><div class='PLSecImgTd'><img alt='' src='"+$(this).attr("src")+"' /></div></td><td><input type='text' class='PLImgCaptionInput'/></td><td class='deletex removePLUploadImage'>x</td></tr>";
            });
            if(imgHTML!=""){
                PLTableBody.append(imgHTML);
                PLTableBody.removeClass("nonner");
            }
        },
        LINK_VIDEO:function(PLWrite,afterSec){
            var content='';
            afterSec.find(".PLUrlBody").each(function(){
                var el=makeHTMLTag('div',{});
                el.html(playlistCurationVar.returnAddPLUrlDiv());
                el.children(".PLWriteUrlDiv").append("<span class='crossX removePLUrl'></span>"+$(this).outerHTML());
                el.find(".PLUrlInputDiv").remove();
                var descVar=el.find(".PLUrlDescNoEdit");
                if(descVar.length==1){
                    var urlDesc=descVar.text();
                    el.find(".PLUrlContent").append("<textarea class='PLUrlDesc'>"+urlDesc+"</textarea>");
                    descVar.remove();
                }
                content+=el.html();
            });
            PLWrite.find(".PLWriteBody").html(content);
            PLWrite.find(".PLWriteBody").append("<span class='yellowButton  margTop20 addMorePLUrls'>Add more Links/Videos</span>");
            playlistCurationVar.addInputForPLUrls(PLWrite.children(".PLWriteBody"));

        },
        TEST:function(PLWrite,afterSec){
            PLWrite.find(".PLWriteBody").html(afterSec.find(".PLSecBody").html());
        },
        DOCUMENT:function(PLWrite,afterSec){
            PLWrite.find(".PLWriteBody").html(afterSec.find(".PLSecBody").html());
        }
    },









    currentSecType:undefined,
    forceSecType:undefined,
    plwriteForConfirmCases:undefined,
    initAddEditContentFns:function(PLWrite){
        this.currentSecType="TEXT";

        PLWrite.on('click',".addEssayPL,.addImagePL,.addQuesPL,.addLinkVideoPL,.addTestPL,.addDocPL",this.PLOptClickCheck);



        //adding questions
        PLWrite.on('click',".removePLSecQues",this.removePLSecQuesClick);
        PLWrite.on('click',".addQuesToPLSec",this.addQuesToPLSecClick);
        PLWrite.on('click',".getPLAQT",this.getPLAQTClick);
        PLWrite.on('keyup',".PLSearchQTInput",this.PLSearchQTInputKeyup);
        PLWrite.on('click',".PLSearchQTInputImg",this.PLSearchQTInputImgClick);
        PLWrite.on('click',".PLAQAllQuesns,.PLAQTakenQuesns,.PLAQMyQuesns",this.PLAQQuesnsOwnerTypeClick);
        PLWrite.on('click',".resetPLAQT",this.resetPLAQTClick);

        //for rte initializing
        vRTE.init(PLWrite.find(".RTEHolder"));   

        //upload image
        PLWrite.on('click',".removePLUploadImage",this.removePLUploadImageClick);

        //link and video fetching
        PLWrite.on('paste',".PLUrlInput",this.PLUrlInputPaste);
        PLWrite.on('keyup',".PLUrlInput",this.PLUrlInputKeyup);

        PLWrite.on('click',".addMorePLUrls",this.addMorePLUrlsClick);
        PLWrite.on('click',".nextUrlImg",this.prevNextUrlImgClick);
        PLWrite.on('click',".prevUrlImg",this.prevNextUrlImgClick);
        PLWrite.on('click',".removePLUrl",this.removePLUrlClick);

        //tests
        PLWrite.on('click',".addTestToPLSec",this.addTestToPLSecClick);

        //submit,preview,delete,cancel sections
        PLWrite.on('click',".PLSecSubmit",this.PLSecSubmitPreviewClick);
        PLWrite.on('click',".PLSecPreview",this.PLSecSubmitPreviewClick);
        $(document).on('click',".closePLSecPreview",this.closePLSecPreviewClick);
        PLWrite.on('click',".PLSecCancel",this.PLSecCancelClick);
        $(document).on('click',".confirmPLSecCancel",this.confirmPLSecCancelClick);
        PLWrite.on('click',".PLSecDelete",this.PLSecDeleteClick);
        $(document).on('click',".confirmPLSecDelete",this.confirmPLSecDeleteClick);
    },

    PLOptClickCheck:function(){
        var secType=$(this).attr("rel");
        var PLWrite=$(this).closest(".PLWrite");
        var goAhead=playlistCurationVar.checkOtherSecs[playlistCurationVar.currentSecType](PLWrite);
        if(goAhead){
            playlistCurationVar.assignActivePLOpt(PLWrite,secType);
            playlistCurationVar.currentSecType=secType;
            playlistCurationVar.PLOptClickOked[secType](PLWrite);
        }
        else{
            playlistCurationVar.forceSecType=secType;
            playlistCurationVar.plwriteForConfirmCases=PLWrite;
            playlistCurationVar.PLConfirmPopup("Do you want to abandon the section you are composing?","confirmForcePLOpt","");
        }
    },
    confirmForcePLOptClick:function(){
            var secType=playlistCurationVar.forceSecType;
            var PLWrite=playlistCurationVar.plwriteForConfirmCases;
            playlistCurationVar.assignActivePLOpt(PLWrite,secType);
            playlistCurationVar.currentSecType=secType;
            playlistCurationVar.PLOptClickOked[secType](PLWrite);
            cancelCommonPopup();
    },
    PLOptClickOked:{
        TEXT:function(PLWrite){
            PLWrite.find(".PLWriteBody").html(playlistCurationVar.RTEHTML);
            vRTE.init(PLWrite.find(".RTEHolder"));
        },
        QUESTION:function(PLWrite){
            playlistCurationVar.initAddQuesPL(PLWrite,[],[]);
        },
        IMAGE:function(PLWrite){
            playlistCurationVar.createUploaderPL(PLWrite);
        },
        LINK_VIDEO:function(PLWrite){
            PLWrite.find(".PLWriteBody").html(playlistCurationVar.returnAddPLUrlDiv());
        },
        TEST:function(PLWrite){
            playlistCurationVar.initAddTestPL(PLWrite);
        },
        DOCUMENT:function(PLWrite){
            PLWrite.find(".PLWriteBody").html("<div class='margTop20 centry'><span class='blueSubmitButton'>\n\
            From Vedantu</span><span class='blueSubmitButton margLeft'>Upload</span></div>");
            PLWrite.find(".PLWriteHeadDiv").addClass("nonner");
        }
    },
    assignActivePLOpt:function(PLWrite,secType){
        var PLWriteOpt=PLWrite.find(".PLOpt[rel="+secType+"]");
        PLWrite.removeClass("PLWriteToolOnly PLWriteTEXT PLWriteIMAGE PLWriteLINK_VIDEO PLWriteQUESTION PLWriteTEST PLWriteDOCUMENT");
        PLWriteOpt.addClass("activePLOpt").siblings().removeClass("activePLOpt");
        PLWrite.addClass("PLWrite"+secType).data("secType",secType);
        PLWrite.find(".PLWriteBody").html("");
        PLWrite.find(".PLWriteHeadDiv").removeClass("nonner");
    },
    checkOtherSecs:{
        TEXT:function(PLWrite){
            var rteHTML=vRTE.getRTEContent(PLWrite.find(".RTEHolder"));
            if(rteHTML.length>100){
                return false;
            }
            else return true;
        },
        QUESTION:function(PLWrite){
            if(PLWrite.find(".addedPLQuesnsTable  tr").length>1){
                return false;
            }
            else return true;
        },
        IMAGE:function(PLWrite){
            if(PLWrite.find(".PLUploadTable  tr").length>1){
                return false;
            }
            else return true;
        },
        LINK_VIDEO:function(PLWrite){
            if(PLWrite.find(".PLUrlBody").length>0){
                return false;
            }
            else return true;
        },
        TEST:function(PLWrite){
            return true;
        },
        DOCUMENT:function(PLWrite){
            return true;
        }
    },


    //questions addition
    quesnsData:"",
    initAddQuesPL:function(PLWrite,quesnsObj,quesIds){
        PLWrite.find(".PLWriteBody").html(playlistCurationVar.PLAQHTML);
        vSelectFns.init();
        var LMHandlerDiv=PLWrite.find(".LMHandlerDiv"),quesnsData=playlistCurationVar.quesnsData;
        var allParams={start:0,size:15,target:"PLAYLIST_ADD",allBrds:true,excludeIds:quesIds};
        LMHandlerDiv.data("urlStr","/Questions/quesItems").data("size",15).data("allParams",allParams);
        if(quesnsData==""){
            $.get("/Questions/quesItems",allParams,function(data){
                quesnsData=data;
                LMHandlerDiv.html(data);
                loadMJEqns(LMHandlerDiv.get(0));           
            });
        }
        else LMHandlerDiv.html(quesnsData);
        var tbody=PLWrite.find(".addedPLQuesnsTable tbody");
        playlistCurationVar.populatePLQuesnsTable(tbody,quesnsObj);
    },
    removePLSecQuesClick:function(){
        var tbody=$(this).closest("tbody");
        $(this).closest("tr").remove();
        var q=1;
        tbody.children("tr.PLQuesnsTr").each(function(){
            $(this).find(".PLQuesnsTd").text("Question "+q);
            q++;
        });
       if($(this).closest("tbody").children("tr").length<2){
           $(this).closest("table").addClass("nonner");
       }
    },
    addQuesToPLSecClick:function(){
        var addedQuesnsTable=$(this).closest(".PLWriteBody").children(".addedPLQuesnsTable");
        var tbody=addedQuesnsTable.children("tbody"),ques=$(this).closest(".ques"),quesnsObj=[];
        $(this).removeClass(".addQuesToPLSec").text("Added");
        if(ques.length>0){
            quesnsObj.push(ques);
        }
        playlistCurationVar.populatePLQuesnsTable(tbody,quesnsObj);
        ques.remove();
    },
    populatePLQuesnsTable:function(tbody,quesnsObj){
        var l=tbody.find("tr").length;
        for(var k=0;k<quesnsObj.length;k++){
            var ques=quesnsObj[k];
            tbody.append("<tr class='crossXPa PLQuesnsTr'><td class='PLQuesnsTd'>Question "+(l+k)+"</td><td>\n\
            <a class='crossX removePLSecQues margLeft'></a></td></tr>");
            tbody.children("tr").last().children(".PLQuesnsTd").data("qid",ques.data("qid")).data("ques",ques.outerHTML());
        }
        if(quesnsObj.length>0)tbody.parent().removeClass("nonner");
    },



    getPLAQTClick:function(){
        var PLAQTHolder=$(this).closest(".PLAddQuesnsTestsHolder");
        playlistCurationVar.getPLAddQuesnsORTests(PLAQTHolder);
    },
    PLSearchQTInputKeyup:function(e){
        if((e.which&&e.which==13)||(e.keyCode&&e.keyCode==13)){
            var PLAQTHolder=$(this).closest(".PLAddQuesnsTestsHolder");
            playlistCurationVar.getPLAddQuesnsORTests(PLAQTHolder);
        }
    },
    PLSearchQTInputImgClick:function(){
        var PLAQTHolder=$(this).closest(".PLAddQuesnsTestsHolder");
        playlistCurationVar.getPLAddQuesnsORTests(PLAQTHolder);
    },
    PLAQQuesnsOwnerTypeClick:function(){
        var PLAQTHolder=$(this).closest(".PLAddQuesnsTestsHolder");
        var LMHandlerDiv=PLAQTHolder.find(".LMHandlerDiv");
        var allParams=LMHandlerDiv.data("allParams");
        $(this).addClass("activeBlackTab").siblings().removeClass("activeBlackTab");
        allParams.resultType=$(this).data("resultType");
        allParams.start=0;allParams.size=15;
        $.get(LMHandlerDiv.data("urlStr"),allParams,function(data){
            LMHandlerDiv.html(data);
        });
    },
    getPLAddQuesnsORTests:function(PLAQTHolder){
        var allParams={};
        var searchVal=PLAQTHolder.findFirst(".PLSearchQTInput").val();
        if(searchVal!="")allParams.query=searchVal;
        var brdIds=[];
        var exam=PLAQTHolder.find(".PLAQTSelectExam").data("value");
        if(exam!="-1")brdIds.push(exam);
        var sub=PLAQTHolder.find(".PLAQTSelectSub").data("value");
        if(sub!="-1")brdIds.push(sub);
        var topic=PLAQTHolder.find(".PLAQTSelectTopic").data("value");
        if(topic!="-1")brdIds.push(topic);
        var subTopic=PLAQTHolder.find(".PLAQTSelectSubTopic").data("value");
        if(subTopic!="-1")brdIds.push(subTopic);
        allParams.brdIds=brdIds;
        allParams.resultType="ALL";
        allParams.target="PLAYLIST_ADD";
        allParams.allBrds=true;
        var quesnsTable=PLAQTHolder.siblings(".addedPLQuesnsTable"),quesIds=[];
        if(quesnsTable.length>0){
            quesnsTable.find(".PLQuesnsTd").each(function(){
               quesIds.push($(this).data("qid"));
            });
        }
        if(PLAQTHolder.closest(".PLWrite").hasClass("PLWriteTEST"))allParams.eType="TEST";
        allParams.excludeIds=quesIds;
        var LMHandlerDiv=PLAQTHolder.find(".LMHandlerDiv");
        LMHandlerDiv.data("allParams",allParams);
        allParams.start=0;allParams.size=15;
        $.get(LMHandlerDiv.data("urlStr"),allParams,function(data){
            LMHandlerDiv.html(data);
            loadMJEqns(LMHandlerDiv.get(0));           
        });
    },
    resetPLAQTClick:function(){
        var PLAQTHolder=$(this).closest(".PLAddQuesnsTestsHolder");
        PLAQTHolder.find(".PLAQTSelectExam").data("value","-1").find(".vSelectText").val("ALL Exams");
        PLAQTHolder.find(".PLAQTSelectSub").data("value","-1").find(".vSelectText").val("ALL Subjects");
        PLAQTHolder.find(".PLAQTSelectTopic").data("value","-1").find(".vSelectText").val("ALL Topics");
        PLAQTHolder.find(".PLAQTSelectSubTopic").data("value","-1").find(".vSelectText").val("ALL Subtopics");
        PLAQTHolder.find(".blackTab").addClass("activeBlackTab").siblings().removeClass("activeBlackTab");
        PLAQTHolder.find(".PLSearchQTInput").val("");
        playlistCurationVar.getPLAddQuesnsORTests(PLAQTHolder);
    },

    //for image uploading
    uploadBtn:new Object(),
    filesQueue:[],
    uploadFns:new Object(),
    uploadParams:new Object(),
    createUploaderPL:function(PLWrite){
        var PLId=PLWrite.attr("lang"),uploadId="file-uploadImagePL_"+PLId;
        PLWrite.find(".PLWriteBody").html("<div class='uploadImagePLDiv margTop'><table class='PLUploadTable margBot20' width='85%'><tbody class='nonner'><tr class='boldy'>\n\
                                                <td>Images</td><td>Add Caption(optional)</td><td></td></tr></tbody></table>\n\
                                                <div id='file-uploadImagePL_"+PLId+"' class='file-uploadImagePL' rel='PL_EDIT'></div><div class='PLUploadCloud nonner'></div></div>");
        playlistCurationVar.filesQueue=[];
        playlistCurationVar.uploadBtn= new qq.FileUploader({
            element: document.getElementById(uploadId),
            action: "/playlists/uploadImage",
            debug: true,
            sizeLimit : 5*1024*1024,
            allowedExtensions : ["jpg","gif","png","jpeg","bmp"]
        });
        PLWrite.find(".qq-upload-button").addClass("blueSubmitButton");
        PLWrite.find(".qq-uploader").data("tabType","PL_EDIT");
    },
    queuePlaylistFiles:function(input,self,PLWrite){
        playlistCurationVar.uploadFns=self;
        var imgHTML="";
        for(var i=0; i<input.files.length; i++){
            var size=input.files[i].size;
            if(size==undefined)size=input.files[i].fileSize;
            size=self._formatSize(size);
            playlistCurationVar.filesQueue.push(input.files[i]);
            var num=(playlistCurationVar.filesQueue.length-1);
            imgHTML+="<tr class='deletexPa PLSecImgTr PLSecImgTrNaN'><td>"+input.files[i].name+"</td><td><input type='text' class='PLImgCaptionInput' rel='"+num+"'/></td><td class='deletex removePLUploadImage'>x</td></tr>";
        }
        PLWrite.find(".PLUploadTable tbody").removeClass("nonner").append(imgHTML);
    },
    PLStartUploading:function(PLMiddle){
         var PLWrite=PLMiddle.children(".PLWrite");
        PLWrite.find(".PLUploadCloud").removeClass("nonner").html("<img src='/public/images/loading.gif' alt='uploading...'/>");
        if(playlistCurationVar.filesQueue.length>0){
            playlistCurationVar.setPLUploadParams(PLWrite);
            playlistCurationVar.uploadFns._onInputChange(playlistCurationVar.filesQueue.shift());
        }
        else playlistCurationVar.PLUploadComplete(PLWrite);
    },
    setPLUploadParams:function(PLWrite){
            var PLId=PLWrite.attr("lang"),params=playlistCurationVar.uploadParams;
            var caption=PLWrite.find(".PLImgCaptionInput[rel="+(params.uploadNo-playlistCurationVar.filesQueue.length)+"]").val();
            if(caption=="")caption=undefined;
            playlistCurationVar.uploadBtn.setParams({
                playlistId:PLId,
                topicId:params.topicId,
                caption:encodeURIComponent(caption),
                sectionId:params.sectionId
            });
    },
    calculatePLUploadPercent:function(percent,toUpload,PLWrite){
        var t=PLWrite.find(".PLSecImgTrNaN").length,c=t-toUpload;
        var p=c*100/t+percent*100/t;
        PLWrite.find(".PLUploadCloud").html(Math.round(p)+"% uploaded");
    },
    PLUploadComplete:function(PLWrite){
        var PLMiddle=PLWrite.closest(".DTPLMiddle");
        var params=playlistCurationVar.uploadParams;
        var imgHTML="";
        PLWrite.find(".PLSecImgTr").each(function(){
           var i=$(this).attr("rel");
           if(i!=undefined&&i!="")imgHTML+="<img src='"+i+"' class='PLSecImg' />";
        });
        params.PLContent=imgHTML;
        playlistCurationVar.addToPlaylist(PLMiddle,params);
    },
    removePLUploadImageClick:function(){
            var tr=$(this).parent(),PLWrite=$(this).closest(".PLWrite"),
            PLSecImgTrNaN=$(this).closest(".PLUploadTable").find(".PLSecImgTrNaN");
            if(isNaN(parseInt(tr.attr("rel")))){
                $.post("/playlists/removeImage",{playlistId:PLWrite.attr("lang"),
                sectionId:PLWrite.attr("id").substring(12),image:tr.attr("rel")},function(data){
                    checkJSONResponse(data);
                });
            }
            else playlistCurationVar.filesQueue.splice(PLSecImgTrNaN.index(tr),1);
            tr.remove();
    },

    //for link and video fetching
    returnAddPLUrlDiv:function(){
        return "<div class='PLWriteUrlDiv crossXPa'><div class='PLUrlInputDiv'>Add URL :<input type='text' \n\
        class='PLUrlInput' placeHolder='Copy paste the URL here '/></div></div>";
    },
    PLUrlInputPaste:function(e){
        var $this=$(this);
        setTimeout(function(){
            playlistCurationVar.fetchInfoOnPaste($this);
        },10)
    },

    fetchInfoOnPaste:function($this){
       var url=$this.val();
       if(checkUrl(url)){
           var PLWriteUrlDiv=$this.closest(".PLWriteUrlDiv");
           playlistCurationVar.fetchUrlInfo(url,PLWriteUrlDiv);
       }
       else showError("Please enter a valid url");
    },
    PLUrlInputKeyup:function(e){
        var url=$(this).val();
        if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))){
           if(checkUrl(url)){
               var PLWriteUrlDiv=$(this).closest(".PLWriteUrlDiv");
               playlistCurationVar.fetchUrlInfo(url,PLWriteUrlDiv);
           }
           else showError("Please enter a valid url");
        }
    },

    fetchUrlInfo:function(url,PLWriteUrlDiv){
        if(url.indexOf("http")< 0)url="http://"+url;
        var domain=url.substring(0,url.indexOf("/",url.lastIndexOf(".")));
        PLWriteUrlDiv.append("<div class='smallLoader'></div>");
        var d=domain.toLowerCase();
       if(d=="http://www.youtube.com"||d=="http://www.vimeo.com"||d=="http://youtube.com"||d=="http://vimeo.com"){
           playlistCurationVar.fetchVideoInfo(url,domain,PLWriteUrlDiv);
           PLWriteUrlDiv.attr("rel","VIDEO");
       }
       else {
           if(url.charAt(url.length-1)!="/")url+="/";
           playlistCurationVar.fetchLinkInfo(url,domain,PLWriteUrlDiv);
           PLWriteUrlDiv.attr("rel","WEB_PAGE");
       }
    },
    fetchVideoInfo:function(url,domain,PLWriteUrlDiv){
        $.get("/widgets/fetchVideoInfo",{url:url},function(data){
            if(data.result!=undefined&&data.result.title!=""&&data.errorMessage==""){
                var d=data.result;
                PLWriteUrlDiv.children(".smallLoader").remove();
                PLWriteUrlDiv.append("<div class='PLVideoBody PLUrlBody' rel='VIDEO'>\n\
                <div class='PLUrlImgDiv'><a class='videoThumbWrapper playPLVideo' rel='"+url+"'>\n\
                <img src='"+d.image+"' alt='Image not available' />"+getDuration(d.duration)+"</a></div>\n\
                <div class='PLUrlContent'>"+playlistCurationVar.getPLUrlDetails(d,domain)+"</div>"+divi+"</div>");
            }
            else PLWriteUrlDiv.append("<div class='userMessage'>The Video you are trying to access is not available or the Video URL is not valid</div>");
            playlistCurationVar.freezePLUrl(url,PLWriteUrlDiv);
        });
    },
    fetchLinkInfo:function(url,domain,PLWriteUrlDiv){
        $.get("/widgets/fetchurl",{url:url,domain:domain},function(data){
            var prevNext="<div class='urlImagesPrevNext acacac nonner'><span class='prevUrlImgNaN'></span> <span class='nextUrlImgNaN'></span>\n\
                    <label class='urlImgActNo'>1</label> of <span class='urlImgCount'>1</span><span class='margLeft'>Choose a thumbnail</span></div>";
            PLWriteUrlDiv.children(".smallLoader").remove();
            if(!data.hasOwnProperty("title")||data.title==""){
                PLWriteUrlDiv.append("<div class='userMessage'>The URL information you are trying to access is not available or the URL is not valid</div>");
                return;
            }
            else{
                 PLWriteUrlDiv.append("<div class='PLLinkBody PLUrlBody' rel='WEB_PAGE'><div class='PLUrlImgDiv'></div>\n\
                    <div class='PLUrlContent'>"+playlistCurationVar.getPLUrlDetails(data,domain)+prevNext+"</div>"+divi+"</div>");
            }
            playlistCurationVar.freezePLUrl(url,PLWriteUrlDiv);
             var imageObj,PLUrlImages=PLWriteUrlDiv.find(".PLUrlImgDiv"),imgs=data.images;
              if(imgs.length>0){
                  PLWriteUrlDiv.find(".urlImagesPrevNext").removeClass("nonner");
                  for(var k=0;k<(imgs.length);k++){
                      imageObj=new Image();
                      imageObj.src=imgs[k];
                      imageObj.onload=playlistCurationVar.fillPLUrlImages(imgs[k],k,PLUrlImages);
                  }
              }
              else PLWriteUrlDiv.find(".urlInfoContent").removeClass("urlDetMarg");
        });
    },
    getPLUrlDetails:function(data,domain){
        return "<a class='boldy PLUrlTitle' target='_blank' href='"+data.url+"'>\n\
                "+data.title+"</a><a href='"+domain+"' target='_blank' class='PLUrlDomain blocky'>"+domain+"</a>\n\
                <textarea class='PLUrlDesc margTop'>"+data.description+"</textarea>";
    },
    freezePLUrl:function(url,PLWriteUrlDiv){
            PLWriteUrlDiv.find(".PLUrlInputDiv").html("Add URL: <span class='PLUrlFreezed'>"+url+"</span>");
            PLWriteUrlDiv.prepend("<span class='crossX removePLUrl'></span>");
            $("<span class='yellowButton  margTop20 addMorePLUrls'>Add more Links/Videos</span>").insertAfter(PLWriteUrlDiv);
    },
    addInputForPLUrls:function(PLWriteBody){
        if(PLWriteBody.children(".PLWriteUrlDiv").length==0){
            PLWriteBody.html(playlistCurationVar.returnAddPLUrlDiv());
            PLWriteBody.children(".addMorePLUrls").remove();
        }
    },
    removePLUrlClick:function(){
        var PLWriteBody=$(this).closest(".PLWriteBody");
        $(this).closest(".PLWriteUrlDiv").remove();
        playlistCurationVar.addInputForPLUrls(PLWriteBody);
    },
    playPLVideoClick:function(){
        $(this).closest(".PLVideoBody").addClass("playingPLVideo");
        insertVideoPlayer($(this).attr("rel"),$(this).parent(),600,350);
    },
    addMorePLUrlsClick:function(){
        var $this=$(this),PLWriteBody=$this.closest(".PLWriteBody");
        PLWriteBody.append(playlistCurationVar.returnAddPLUrlDiv());
        $this.remove();
    },
    fillPLUrlImages:function(imageSrc,imageIndex,PLUrlImages){
            var cla=(imageIndex==0)?'':"nonner";
            PLUrlImages.append("<img src='"+imageSrc+"' class='urlImg "+cla+"' rel='"+(imageIndex+1)+"'/>");
            PLUrlImages.closest(".PLLinkBody").find(".urlImgCount").text(PLUrlImages.find(".urlImg").length);
            playlistCurationVar.urlImagesPrevNext(PLUrlImages.closest(".PLLinkBody"));
    },
    urlImagesPrevNext:function(PLLinkBody){
        var currentImg=PLLinkBody.find(".urlImg").not(".nonner"),p=PLLinkBody.find(".prevUrlImgNaN"),n=PLLinkBody.find(".nextUrlImgNaN");
        if(currentImg.prev().length>0)p.addClass("prevUrlImg");
        else p.removeClass("prevUrlImg");
        if(currentImg.next().length>0)n.addClass("nextUrlImg");
        else n.removeClass("nextUrlImg");
    },
    prevNextUrlImgClick:function(){
        var PLLinkBody=$(this).closest(".PLLinkBody"),newImg;
        var imgSet=PLLinkBody.find(".urlImg").not(".nonner").addClass("nonner");
        if($(this).hasClass("nextUrlImg"))newImg=imgSet.next();
        else newImg=imgSet.prev();
        var imgNo=newImg.removeClass("nonner").attr("rel");
        PLLinkBody.find(".urlImgActNo").text(imgNo);
        playlistCurationVar.urlImagesPrevNext(PLLinkBody);
    },

    //tests addition
    testsData:"",
    initAddTestPL:function(PLWrite){
        PLWrite.find(".PLWriteBody").html(playlistCurationVar.PLATHTML);
        vSelectFns.init();
        var LMHandlerDiv=PLWrite.find(".LMHandlerDiv"),testsData=playlistCurationVar.testsData;
        LMHandlerDiv.data("urlStr","/Tests/testTileItems").data("size",15)
        .data("allParams",{target:"PLAYLIST_ADD",eType:"TEST"});
        if(testsData==""){
            $.get("/Tests/testTileItems",{start:0,size:15,target:"PLAYLIST_ADD",eType:"TEST"},function(data){
                testsData=data;
                LMHandlerDiv.html(data);
            });
        }
        else LMHandlerDiv.html(testsData);
        PLWrite.find(".PLWriteHeadDiv").addClass("nonner");
    },
    addTestToPLSecClick:function(){
        var testTile=$(this).closest(".testTile");
        var PLWrite=$(this).closest(".PLWrite"),afterSectionId;
        var PLMiddle=PLWrite.closest(".DTPLMiddle");
        if(PLWrite.prev(".PLCompo").attr("id")==undefined)afterSectionId=-1;
        else afterSectionId=PLWrite.prev(".PLCompo").attr("id").substring(4);
        var params={},sources=[];
        sources.push({type:"TEST",entityId:testTile.data("testId")});
        params.secType="TEST";
        params.sources=sources;
        params.afterSectionId=afterSectionId;
        params.topicId=PLWrite.attr("rel");
        params.playlistId=PLWrite.attr("lang");
        params.userId=USERID;
        params.heading=testTile.find(".TTTitle").text();
        $(this).remove();
        $.post("/playlists/addSection",params,function(data){
            if(checkJSONResponse(data)){
                params.sectionId=data.result.sectionId;
                var afterSec=PLWrite.prev();
                $("<div class='PLCompo' id='sec_"+params.sectionId+"' data-sec-type='TEST'><div class='PLSecHead'><span class='PLSecHeadIcon PLSecHeadIconTEST'>\n\
                </span><span class='editPLSection'>Edit</span><div class='PLSecHeadText'>"+params.heading+"</div></div>\n\
                <div class='PLSecBody'>"+testTile.outerHTML()+"</div></div>").insertAfter(afterSec);
                playlistCurationVar.PLWriteRemove(PLWrite);
                playlistTOCVar.afterAddingSec(PLMiddle,params);
            }
        });
    },




    //submit,preview,delete,cancel sections
    PLSecSubmitPreviewClick:function(){
        var PLWrite=$(this).closest(".PLWrite"),heading=PLWrite.find(".PLWriteHead").val(),afterSectionId;
        var PLMiddle=PLWrite.closest(".DTPLMiddle"),urlStr="addSection",sectionId=undefined;
        if(checkUndefined(PLWrite.attr("id"))!=""){
            urlStr="updateSection";
            sectionId=PLWrite.attr("id").substring(12);
            afterSectionId=undefined;
        }
        else if(PLWrite.prev(".PLCompo").attr("id")==undefined)afterSectionId=-1;
        else afterSectionId=PLWrite.prev(".PLCompo").attr("id").substring(4);
        var secType=PLWrite.data("secType");
        var submitParams=playlistCurationVar.submitParams[secType](PLWrite)
        var sources=submitParams.sources;
        if(heading==""){
            showError("Please add Heading");
            return;
        }
        else if(submitParams.isValid!=""){
            showError(submitParams.isValid);
            return;
        }
        var params={};
        params.secType=secType;
        params.sources=sources;
        params.heading=heading;
        params.afterSectionId=afterSectionId;
        params.topicId=PLWrite.attr("rel");
        params.playlistId=PLWrite.attr("lang");
        params.userId=USERID;
        params.sectionId=sectionId;
        if($(this).hasClass("PLSecSubmit")){
            $.post("/playlists/"+urlStr,params,function(data){
               if(checkJSONResponse(data)){
                if(params.sectionId==undefined)params.sectionId=data.result.sectionId;
                playlistCurationVar.playlistAdd[secType](PLMiddle,params);
                loadMJEqns(PLMiddle.get(0)); 
               }
            });
        }
        else {
            playlistCurationVar.playlistPreview[secType](PLMiddle,params);
            loadMJEqns($("#PLPreviewPopup").get(0)); 
        }
    },
    submitParams:{
        TEXT:function(writers){
            var sources=[],content=vRTE.getRTEContent(writers.find(".RTEHolder")),isValid="";
            if(content=="")isValid="Please write something";
            sources.push({content:content,type:"TEXT"});
            return {sources:sources,isValid:isValid};
        },
        QUESTION:function(writers){
            var sources=[],isValid="",trs=writers.find(".PLWriteBody .addedPLQuesnsTable tbody tr.PLQuesnsTr"),quesns=[];
            if(trs.length==0)isValid="Please Select some questions";
            else trs.each(function(){
                var td=$(this).children(".PLQuesnsTd");
                sources.push({type:"QUESTION",entityId:td.data("qid")});
                quesns.push(td.data("ques"));
            });
            writers.data("quesns",quesns);
            return {sources:sources,isValid:isValid};
        },
        IMAGE:function(writers){
            var sources=[],isValid="";
            sources.push({type:"IMAGE"});
            if(writers.find(".PLUploadTable tbody tr").length==1)isValid="Please Select some images to Upload";
            return {sources:sources,isValid:isValid};
        },
        LINK_VIDEO:function(writers){
            var sources=[],isValid="";
            writers.find(".PLWriteUrlDiv").each(function(){
                var $this=$(this),content=$this.find(".PLUrlDesc").val(),thumbnail;
                var type=$this.find(".PLUrlBody").attr("rel"),PLUrlTitle=$this.find(".PLUrlTitle"),duration;
                if(type=="WEB_PAGE"){
                    thumbnail=$this.find(".urlImg").not(".nonner").attr("src");
                }
                else if(type=="VIDEO"){
                    thumbnail=$this.find(".videoThumbWrapper img").attr("src");
                    duration=$this.find(".videoDuration").attr("rel");
                }
                var title=PLUrlTitle.text(),url=PLUrlTitle.attr("href");
                if(checkUndefined(title)!=""&&checkUndefined(url)!=""){
                    sources.push({type:type,content:content,title:title.trim(),url:url,thumbnail:thumbnail,duration:duration});
                }
            });
            if(sources.length==0)isValid="Please add URL and Submit";
            return {sources:sources,isValid:isValid};
        }
    },
    playlistAdd:{
        TEXT:function(PLMiddle,params){
            var sources=params.sources,PLContent="";
            for(var k=0;k<sources.length;k++){
                PLContent+="<div class='PLSecContent'>"+sources[k].content+"</div>";
            }
            params.PLContent=PLContent;
            playlistCurationVar.addToPlaylist(PLMiddle,params);
        },
        QUESTION:function(PLMiddle,params){
            var PLWrite=PLMiddle.find(".PLWrite"),quesns=PLWrite.data("quesns"),quesnsHTML="";
            if(quesns&&quesns.length>0){
                for(var k=0;k<quesns.length;k++){
                    quesnsHTML+=quesns[k];
                }
            }
            else {
               quesnsHTML="<div class='userMessage'>No questions are added to this section</div>";
            }
            params.PLContent= quesnsHTML;
            playlistCurationVar.addToPlaylist(PLMiddle,params);
        },
        IMAGE:function(PLMiddle,params){
            params.uploadNo=PLMiddle.find(".PLWrite .PLSecImgTrNaN").length;
            playlistCurationVar.uploadParams=params;
            playlistCurationVar.PLStartUploading(PLMiddle);
        },
        LINK_VIDEO:function(PLMiddle,params){
            var PLWrite=PLMiddle.find(".PLWrite");
            var sources=params.sources,PLContent="",k=0;
            PLWrite.find(".PLWriteUrlDiv").each(function(){
                if($(this).attr("rel")=="WEB_PAGE"){
                    $(this).find(".PLUrlImgDiv").html("<img src='"+sources[k].thumbnail+"'  class='urlImg'/>");
                    $(this).find(".urlImagesPrevNext").remove();
                }
                var descVar=$(this).find(".PLUrlDesc");
                if(descVar.length==1){
                    var desc=descVar.val();
                    descVar.remove();
                    $(this).find(".PLUrlContent").append("<div class='PLUrlDescNoEdit'>"+desc+"</div>");
                }
                k++;
                PLContent+=$(this).find(".PLUrlBody").outerHTML();
            });
            params.PLContent=PLContent;
            playlistCurationVar.addToPlaylist(PLMiddle,params);
        }
    },
    addToPlaylist:function(PLMiddle,params){
            PLMiddle.children(".PLCompo.nonner").remove();
            var PLWrite=PLMiddle.children(".PLWrite");
            var afterSec=PLWrite.prev();
            $("<div class='PLCompo PLCompo"+params.secType+"' id='sec_"+params.sectionId+"' \n\
            data-sec-type='"+params.secType+"'><div class='PLSecHead'>\n\
            <span class='PLSecHeadIcon PLSecHeadIcon"+params.sources[0].type+"'>\n\
            </span><span class='editPLSection'>Edit</span><div class='PLSecHeadText'>"+params.heading+"</div>\n\
            </div><div class='PLSecBody'>"+params.PLContent+"</div>\n\
            <div class='blueSubmitButton editPLSection'><img src='/public/images/playlist/editPL.png' alt='edit'/></div>\n\
            </div>").insertAfter(afterSec);
            playlistCurationVar.PLWriteRemove(PLWrite);
            playlistTOCVar.afterAddingSec(PLMiddle,params);
    },
    playlistPreview:{
        TEXT:function(PLMiddle,params){
            var sources=params.sources,PLContent="";
            for(var k=0;k<sources.length;k++){
                PLContent+="<div class='PLSecContent'>"+sources[k].content+"</div>";
            }
            params.PLContent=PLContent;
            playlistCurationVar.previewPlaylistSec(PLMiddle,params);
        },
        IMAGE:function(PLMiddle,params){
            var queue=$.extend([],playlistCurationVar.filesQueue);
            playlistCurationVar.previewPLSecImages(queue,PLMiddle,params);
        },
        LINK_VIDEO:function(PLMiddle,params){
            var PLWrite=PLMiddle.find(".PLWrite");
            var sources=params.sources,k=0;
            params.PLContent= PLWrite.children(".PLWriteBody").html();
            playlistCurationVar.previewPlaylistSec(PLMiddle,params);

            var previewCompo=$("#PLPreviewPopup .PLCompo");
            previewCompo.find(".PLWriteUrlDiv").each(function(){
                var $this=$(this);
                if($this.attr("rel")=="WEB_PAGE"){
                    $this.find(".PLUrlImgDiv").html("<img src='"+sources[k].thumbnail+"'  class='urlImg'/>");
                    $this.find(".urlImagesPrevNext").remove();
                }
                var descVar=$this.find(".PLUrlDesc");
                if(descVar.length==1){
                    var desc=descVar.val();
                    descVar.remove();
                    $this.find(".PLUrlContent").append("<div class='PLUrlDescNoEdit'>"+desc+"</div>");
                }
                $this.children(".PLUrlInputDiv,.removePLUrl").remove();
                k++;
            });
            previewCompo.find(".addMorePLUrls").remove();
        },
        QUESTION:function(PLMiddle,params){
            var PLWrite=PLMiddle.find(".PLWrite"),quesns=PLWrite.data("quesns"),quesnsHTML="";
            if(quesns&&quesns.length>0){
                for(var k=0;k<quesns.length;k++){
                    quesnsHTML+=quesns[k];
                }
            }
            else {
               quesnsHTML="<div class='userMessage'>No questions are added to this section</div>";
            }
            params.PLContent= quesnsHTML;
            playlistCurationVar.previewPlaylistSec(PLMiddle,params);
        }
    },
    previewPLSecImages:function(queue,PLMiddle,params){
        var imgHTML="",PLWrite=PLMiddle.findFirst(".PLWrite");
        PLWrite.find(".PLSecImgTr").each(function(){
           var i=$(this).attr("rel");
           if(i!=undefined&&i!="")imgHTML+="<img src='"+i+"' class='PLSecImg' />";
        });
           params.PLContent=imgHTML;
           playlistCurationVar.previewPlaylistSec(PLMiddle,params);
        var previewCompo=$("#PLPreviewPopup .PLCompo"),filesNo=queue.length;
        for(var i=0;i<filesNo;i++){
            var reader = new FileReader();
            reader.readAsDataURL(queue.shift());
            //previewCompo.findFirst(".PLSecBody").append("<img src='' class='PLSecImg PLSecImg_"+i+"' />");
            reader.onload = function (e) {
              //  previewCompo.find(".PLSecImg_"+i).attr("src",e.target.result);
               $("#PLPreviewPopup .PLSecBody").append("<img class='PLSecImg' src='"+e.target.result+"'/>");
            }
            reader.onerror = function(event){
                previewCompo.find(".PLSecImg_"+i).attr("alt","Some error Occured, image cannot be displayed");
            };
        }
    },
    previewPlaylistSec:function(PLMiddle,params){
            playlistCurationVar.plmiddleForConfirmCases=PLMiddle;
            var popup=getCommonPopupBody(750);
            popup.html("<div id='PLPreviewPopup'>\n\
            <div class='PLCompo PL"+params.secType+"'><div class='PLSecHead'>\n\
            <span class='PLSecHeadIcon PLSecHeadIcon"+params.sources[0].type+"'>\n\
            </span><div class='PLSecHeadText'>"+params.heading+"</div></div>\n\
            <div class='PLSecBody'>"+params.PLContent+"</div></div></div>");
    },
    closePLSecPreviewClick:function(){
       $("#PLPreviewPopupHolder").html("");
       var PLWrite=playlistCurationVar.plmiddleForConfirmCases.children(".PLWrite");
       playlistCurationVar.focusPLWriteHead(PLWrite);
    },
    PLSecCancelClick:function(){
       playlistCurationVar.plmiddleForConfirmCases=$(this).closest(".DTPLMiddle");
       playlistCurationVar.PLConfirmPopup("Do you want to cancel this section?","confirmPLSecCancel","");
    },
    confirmPLSecCancelClick:function(){
        var PLMiddle=playlistCurationVar.plmiddleForConfirmCases;
        playlistCurationVar.PLWriteRemove(PLMiddle.children(".PLWrite"));
        playlistCurationVar.checkPLCompoLength(PLMiddle);
        PLMiddle.children(".PLCompo.nonner").removeClass("nonner");
        cancelCommonPopup();
        playlistTOCVar.markActiveSec(PLMiddle.attr("rel"),"PL_EDIT");
    },
    PLSecDeleteClick:function(){
       playlistCurationVar.plmiddleForConfirmCases=$(this).closest(".DTPLMiddle");
       playlistCurationVar.PLConfirmPopup("Do you want to delete the section?","confirmPLSecDelete","");
    },
    confirmPLSecDeleteClick:function(){
        var PLMiddle=playlistCurationVar.plmiddleForConfirmCases,PLId=PLMiddle.attr("rel");
        var PLWrite=PLMiddle.children(".PLWrite"),topicId=PLWrite.attr("rel"),secId=PLWrite.attr("id").substring(12);
        if(secId!=""){
            $.post("/playlists/removeSection",{playlistId:PLId,topicId:topicId,sectionId:secId},function(data){
                checkJSONResponse(data);
            });
        }
        PLMiddle.children(".PLCompo.nonner").remove();
        playlistCurationVar.PLWriteRemove(PLWrite);
        playlistCurationVar.checkPLCompoLength(PLMiddle);
        cancelCommonPopup();
        playlistTOCVar.returnActiveTOC(PLMiddle,secId).PLTOCSec.remove();
        playlistTOCVar.markActiveSec(PLId,"PL_EDIT");
    },
    checkPLCompoLength:function(PLMiddle){
        if(PLMiddle.children(".PLCompo").length==0){
            $(playlistCurationVar.PLAddContentHTML(PLMiddle)).insertAfter(PLMiddle.children(".PLTopicHead"));
            var newPLWrite=PLMiddle.children(".PLWrite");
            playlistCurationVar.initAddEditContentFns(newPLWrite);
            newPLWrite.addClass("PLWriteToolOnly").data("secType","TEXT");
            newPLWrite.find(".activePLOpt").removeClass("activePLOpt");
            PLMiddle.children(".PLAddBtnBtmDiv").addClass("nonner");
        }
    },
    PLCommentButtonClick:function(){
        var plId=$(this).closest(".DTPLMiddle").data("plId");
        var PLCompo=$(this).closest(".PLCompo");
        var commWidgetHolder=PLCompo.find(".PLSecCommWidgetHolder");
        addToggler(commWidgetHolder,$(this));
        if(commWidgetHolder.html()==""){
            var LMData={urlStr:"/widgets/commItems",size:10,start:0,rootType:"SECTION",rootId:PLCompo.data("secId")};
            var allParams={};
            allParams.rootId=PLCompo.data("secId");allParams.rootType="SECTION";
            allParams.baseId=plId;allParams.baseType="PLAYLIST";
            allParams.callBack="PL_SEC";
            initCommWidget(allParams,LMData,commWidgetHolder);
        }
    },

    //utilities
    PLWriteRemove:function(PLWrite,option){
        var PLMiddle=PLWrite.closest(".DTPLMiddle");
        PLWrite.remove();
        var PLTOCTopic=playlistTOCVar.returnActiveTOC(PLMiddle,"").PLTOCTopic;
        PLTOCTopic.find(".editingTOC").removeClass("editingTOC");
        PLMiddle.children(".PLAddBtnBtmDiv").removeClass("nonner");
    },
    PLAddContentHTML:function(PLMiddle){
        var PLId=PLMiddle.attr("rel"),el=makeHTMLTag('div',{});
        if(playlistCurationVar.PLWriteHTML!=""){
            el.html(playlistCurationVar.PLWriteHTML);
            el.children(".PLWrite").attr("lang",PLId).attr("rel",PLMiddle.children(".PLTopicHead").attr("rel"));
        }
        return el.html();
    },
    PLConfirmPopup:function(message,yesClass,noClass){
        var popup=getCommonPopupBody(400);
        popup.html("<div class='boldy'>"+message+"\
        </div><div class='centry'><span class='blueSubmitButton margRight10 "+yesClass+"'>Yes</span>\n\
        <span class='blueSubmitButton "+noClass+"' onClick='cancelCommonPopup()'>No</span></div>");
    }
}



var playlistViewVar={
    initPlaylistViewFns:function(DTPL){
        DTPL.on('click',".fullScreen_PL_VIEW_TOCHead",this.fullScreenClick);
        DTPL.on('click',".PLTOCHeading",this.fullScreenTOCHeadClick);
    },
    fullScreenClick:function(){
        $(this).closest(".DTPLLeft").addClass("fullScreen_PL_VIEW_ShowTOCs");
    },
    fullScreenTOCHeadClick:function(){
        $(this).closest(".DTPLLeft").removeClass("fullScreen_PL_VIEW_ShowTOCs");
    }
}





//for questionslist widget
//this div needs to be initialised when it is loaded, data.urlStr and data.size,data.allParams should be provided, the load more class is always
//LMHandlerDivLoadMore
//also when any of the top search or query parameters change data.allparams  should be changed




function PLAQTExamChange(value,target,dataValue){
    var vSelect=$(target).closest(".vselect"),examId=dataValue;
    var targetVar=vSelect.siblings(".PLAQTSelectSub");
    PLAQTSelectChangeFn("/uicomboards/getOrgBoards",{type:"COURSE",size:7,targetId:examId},targetVar);
}
function PLAQTSubChange(value,target,dataValue){
    var vSelect=$(target).closest(".vselect"),subId=dataValue;
    var targetVar=vSelect.siblings(".PLAQTSelectTopic");
    PLAQTSelectChangeFn("/uicomboards/getOrgBoards",{type:"TOPIC",size:7,parentId:subId},targetVar);
}
function PLAQTTopicChange(value,target,dataValue){
    var vSelect=$(target).closest(".vselect"),topicId=dataValue;
    var targetVar=vSelect.siblings(".PLAQTSelectSubTopic");
    PLAQTSelectChangeFn("/uicomboards/getOrgBoards",{type:"SUBTOPIC",size:7,parentId:topicId},targetVar);
}
function PLAQTSelectChangeFn(urlStr,allParams,target){
    $.get(urlStr,allParams,function(data){
       if(checkJSONResponse(data)){
            var boards=data.result.boards,opts="<span class='vSelectEach' data-value='-1'>All</span>";
            for(var k=0;k<boards.length;k++){
                opts+="<span class='vSelectEach' data-value='"+boards[k].brdId+"'>"+boards[k].name+"</span>";
            }
            target.find(".vSelectList").html(opts);
            target.updateVSelectTag();
       }
    });
}
function removeActivatingClass(){
    $("#PLViewPage").children(".DTPL1002").children(".DTPLMiddle").removeClass("activatingSec");
}
