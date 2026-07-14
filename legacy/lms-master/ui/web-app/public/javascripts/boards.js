var vBoards=new function($){          
    //eventhandler functions
    var bepSubjectClick=function(){
        $(this).addClass("activeBEPSubject").siblings().removeClass("activeBEPSubject");
        var BSContents=$(this).closest("#BEPMiddleDiv").children("#BSContents");
        showTopLoader();
        var params={brdId:$(this).data("brdId")};
        $.get("/boards/getBSContents",params,function(data,s,xhr){
            hideTopLoader();
            BSContents.html(data);
       });        
    }
    var bepTopicClick=function(){
        var boardPage=$(this).closest("#boardPage");
        showTopLoader();
        var params={brdId:$(this).data("brdId"),examCode:boardPage.data("examCode"),
            examId:boardPage.data("examId")};
        $.get("/boards/boardTopic",params,function(data,s,xhr){
             hideTopLoader();
             boardPage.closest("#noTabSection").html(data);
             loadBTPContent(boardPage);
       });
       pushHistory(null,null,this.href);
       return false;        
    }
    var btpAllExamCheck=function(){
        var examList=$(this).closest(".BTPExamList");
        examList.find(".BTPExamCheck").prop("checked",false);
        var BTPNavdiv=examList.closest("#boardPage").find("#BTPNavDiv");
        BTPNavdiv.find(".boardExam").remove();
        BTPNavdiv.prepend("<a class='ptrTagHovered'>All Exams</a> /");
        loadBTPContent(BTPNavdiv.closest("#boardPage"));        
    }
    var btpExamCheck=function(){
        var examList=$(this).closest(".BTPExamList");
        var allExamCheck=examList.find(".BTPAllExamCheck");
        var BTPNavdiv=examList.closest("#boardPage").find("#BTPNavDiv");
        BTPNavdiv.find(".boardExam").remove();
        var examsHTML="";
        if(examList.find(".BTPExamCheck:checked").length==0){
            allExamCheck.prop("checked",true);
            examsHTML="<a class='ptrTagHovered'>All Exams</a> /";
        }
        else {
            allExamCheck.prop("checked",false);
            examList.find(".BTPExamCheck:checked").each(function(){
                if($(this).prop("checked")){
                    examsHTML+="<a class='boardExam ptrTagHovered' \n\
                    data-brd-id='"+$(this).val()+"'>"+$(this).attr("name")+"</a>, ";
                }
            });
            examsHTML=examsHTML.substring(0,examsHTML.length-1)+" /";
        }
        BTPNavdiv.prepend(examsHTML);
        loadBTPContent(BTPNavdiv.closest("#boardPage"),$(this));        
    }
    var btpLeftSecItemClick=function(){
       var $this=$(this);
       $this.addClass("activeBTPLeftSecItem").siblings().removeClass("activeBTPLeftSecItem");
       var boardPage=$this.closest("#boardPage");
       var BTPNavDiv=boardPage.find("#BTPNavDiv");
       BTPNavDiv.find(".BTPNavActiveSubTopic").remove();
       if(!$this.hasClass("BTPLeftSecTopic")){
           BTPNavDiv.append("<a class='BTPNavActiveSubTopic ptrTagHovered' \n\
            data-brd-id='"+$this.data("brdId")+"'>/ "+$this.text()+"</a>");
       }
       loadBTPContent(boardPage);        
    }
    var btpNavActiveTopicClick=function(){
        var BTPNavDiv=$(this).closest("#BTPNavDiv");
        var BTPLeftSecTopic=BTPNavDiv.closest("#boardPage").find(".BTPLeftSecTopic");
        if(BTPNavDiv.find(".BTPNavActiveSubTopic").length>0){
            BTPNavDiv.find(".BTPNavActiveSubTopic").remove();
            BTPLeftSecTopic.addClass("activeBTPLeftSecItem").siblings().removeClass("activeBTPLeftSecItem");
            loadBTPContent($(this).closest("#boardPage"));
        }        
    }
    var btpShowNavTopicsClick=function(){
        var BTPNavTopicsHolder=$(this).closest(".BTPNavTopicsHolder");
        var subId=BTPNavTopicsHolder.siblings(".boardSubject").data("brdId");
        var targetDiv=BTPNavTopicsHolder.find(".BTPNavTopicsDiv");
        if(targetDiv.html()==""&&subId){
            var topicsHTML="";
            $.get("/uicomboards/getConsumerBoards",{parentId:subId},function(data){
                var topics=data.result.boards;
                for(var k=0;k<topics.length;k++){
                    topicsHTML+="<a class='BTPNavTopic ptrTag doCStream' title='"+topics[k].name+"'\n\
		    data-cs-name='DROPPED_DOWN_BOARD'\n\
                    data-brd-id='"+topics[k].brdId+"'>"+topics[k].name+"</a>";
                }
                targetDiv.html(topicsHTML);
            });
        }
        addToggler(BTPNavTopicsHolder,$(this));        
    }
    var btpNavTopicClick=function(){
        var $this=$(this),BTPNavDiv=$this.closest("#BTPNavDiv");
        var BTPLeftSecItems=BTPNavDiv.closest("#boardPage").find(".BTPLeftSecItems");
        BTPNavDiv.find(".BTPNavActiveSubTopic").remove();
        BTPNavDiv.find(".BTPNavActiveTopic").text($this.attr("title")).data("brdId",$this.data("brdId"));
        var leftSecHTML="<div class='BTPLeftSecTopic activeBTPLeftSecItem' \n\
        data-brd-id='"+$this.data("brdId")+"' >"+$this.attr("title")+"</div>";
        $.get("/UICOMboards/getConsumerBoards",{type:"SUBTOPIC",parentId:$this.data("brdId")},function(data){
           var subTopics=data.result.boards;
           for(var k=0;k<subTopics.length;k++){
               leftSecHTML+="<div class='BTPLeftSecSubTopic' data-brd-id='"+subTopics[k].brdId+"'\n\
                         rel='"+subTopics[k].brdId+"'>"+subTopics[k].name+"</div>";
           }
           BTPLeftSecItems.html(leftSecHTML);
        });
        insideOutClick();
        loadBTPContent($(this).closest("#boardPage"));        
    }
    
    
    var loadBTPContent=function(boardPage){
            if(!boardPage)boardPage=$("#boardPage");
            var brdIds=[],navDiv=boardPage.find("#BTPNavDiv");
            var allParams={};

            //brdIds
            navDiv.find("a.boardExam").each(function(){
                brdIds.push($(this).data("brdId"));
            });
            brdIds.push(navDiv.find("a.BTPNavActiveTopic").data("brdId"));
            if(navDiv.find("a.BTPNavActiveSubTopic").length>0){
                brdIds.push(navDiv.find("a.BTPNavActiveSubTopic").data("brdId"));
            }
            allParams.brdIds=brdIds;
            allParams.allBrds=true;

            var mcWidget=boardPage.find(".mcWidget");        
            manageContent.setmcWidgetParams(mcWidget,allParams);               
            manageContent.loadmcContent(mcWidget);
    }
    
    this.init=function(params){
        var targetPage=params.targetPage||'';
        if(targetPage=="BOARD_TOPIC"){
            var leftSec=$("#BTPLeftSec");
            if(params.examCode!=""&&params.examId!=""){
                leftSec.find(".BTPExamCheck[rel='"+params.examId+"']").prop("checked",true);
                leftSec.find(".BTPAllExamCheck").prop("checked",false);
            }
            if(params.pageType=="BOARD_SUB_TOPIC"){
             leftSec.find(".BTPLeftSecSubTopic[rel='"+params.subTopicId+"']")
             .addClass("activeBTPLeftSecItem").siblings().removeClass("activeBTPLeftSecItem");
            }    
            manageContent.init($("#BTPContent").find(".mcWidget"),{urlStr:"/questions/quesItems",
                params:{target:"BOARD_PAGE",orderBy:"mostPopular"},moreSize:25,initialSize:15});
            loadBTPContent();                
        }
        //event handlers
        $("#boardPage").on("click",".BEPSubject",bepSubjectClick)
        .on("click",".BEPTopic",bepTopicClick)
        .on("click",".BTPAllExamCheck",btpAllExamCheck)
        .on("click",".BTPExamCheck",btpExamCheck)
        .on("click",".BTPLeftSecSubTopic,.BTPLeftSecTopic",btpLeftSecItemClick)
        .on("click",".BTPNavActiveTopic",btpNavActiveTopicClick)
        .on("click",".BTPShowNavTopics",btpShowNavTopicsClick)       
        .on("click",".BTPNavTopic",btpNavTopicClick)               
    }          
}(jQuery);
var boardPageContentType=function(mcWidget,mcTabsDiv){
    var activeTab=mcTabsDiv.find(".activemcTab");        
    mcWidget.data("urlStr",activeTab.data("urlStr"));
    manageContent.loadmcContent(mcWidget,mcTabsDiv);
}
