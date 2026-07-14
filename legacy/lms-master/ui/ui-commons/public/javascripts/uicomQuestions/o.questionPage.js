var vQuestionPage=new (function($){
    var questionPageAttemptedQues=false,globalClickEvent="click.vQuestionPage";        
    this.init=function(params){
        var rightSec=$("#QPRightSec"),middleSec=$("#QPMiddleSec"),qid=params.qid;
        //console.log(params);
        if(params.isAttempted=="true"||params.attemptable=="false"){
            showQuesSolns();
        } 
        
        questionPageAttemptedQues=params.isAttempted;
        var brdIds=[];
        var boardsJson=params.boardsJSON;
        $.each(boardsJson,function(name,brdId){
           brdIds.push(brdId);
        });

        getSimilarTags(brdIds,rightSec)
        getRelatedQuestions(qid,rightSec);
        getRelatedTests(qid,rightSec);
	
        if(params.challengeId!=''){
            vReq.get("/challenges/challengeLeaderBoard",{start:0,size:5,target:"QUESTION_PAGE",
                id:params.challengeId},function(data){
                rightSec.find(".QPChallengeLeaderBoard").html(data);
            });
        }
        if(params.challengeId!=''&&params.source=="CHALLENGES_PAGE"){
            vReq.get("/challenges/userChallengeInfo",{id:params.challengeId},function(data){
                middleSec.find(".QPChallengeStatsDiv").html(data);
            });
        }        
        loadMJEqns(middleSec.get(0));        
                        
        //event handlers
        initEventHanlders($("#questionPage"),rightSec);                
    }
    var initEventHanlders=function(questionPage,rightSec){
        questionPage
        //for solutions and discussions
        .on("click",".submitQSInput",submitQSInputClick)
        .on("click",".showQSComments",showQSCommentsClick)
        .on("click",".hideQSComments",hideQSCommentsClick)
        
        //warn section
        .on("click",".showQuesAnalyticsWarn",showQuesAnalyticsWarnClick)
        .on("click",".quesSolnsTabWarn",quesSolnsTabWarnClick)
        .on("click",".quesDiscsTabWarn",quesDiscsTabWarnClick)
        .on("click",".showChallengeAnalyticsWarn",showChallengeAnalyticsWarnClick)
          
        
        
        $(document).off(globalClickEvent)
        .on(globalClickEvent,".removeQuesAttemptWarns",removeQuesAttemptWarnsClick)      
        .on(globalClickEvent,".quesSolnsTab",showQuesSolns)
        .on(globalClickEvent,".quesDiscsTab",showQuesDiscs)        
        //question analytics
        .on(globalClickEvent,".showQuesAnalytics",showQuesAnalyticsClick)
        .on(globalClickEvent,".showChallengeAnalytics",showChallengeAnalyticsClick)
        //right section
        rightSec
        .on(globalClickEvent,".QPSQNext,.QPSQPrev",qpSQNextPrevClick)
        .on(globalClickEvent,".QPSimilarQuesnsTab",qpSimilarQuesnsTabClick)
        .on(globalClickEvent,".QPRelatedTestsTab",qpRelatedTestsTabClick)
        .on(globalClickEvent,".QPRelatedPLsTab",qpRelatedPLsTabClick)
        .on(globalClickEvent,".QPRelatedDocsTab",qpRelatedDocsTabClick)
        .on(globalClickEvent,".QPRelatedVideosTab",qpRelatedVideosTabClick)           
    };
    var getRelatedQuestions=function(qid,rightSec){
        vReq.get("/questions/similarQuesns",{start:0,size:2,entity:{id:qid,type:"QUESTION"},
            resultEntityType:"QUESTION"},function(data){
            var target=rightSec.find(".QPSimilarQuesns");
            target.html(data);
	    var hidden = target.find("#similarQuesCount");
	    var total = parseInt(hidden.data("totalHits"));
	    var cur = parseInt(hidden.val());
	    if(total > cur){
		rightSec.find(".QPSimilarQuesnsDiv").find(".QPSQNextNaN").addClass("QPSQNext");
	    }
	    target.data("totalHits",total);
            loadMJEqns(target.get(0));
        });    
    }
    var getRelatedTests=function(qid,rightSec){
        /* TODO */
	return;
        vReq.get("/widgets/relatedEntities",{start:0,size:3,target:"QUESTION_PAGE",
            entity:{id:qid,type:"QUESTION"},resultEntityType:"TEST"},function(data){
                rightSec.find(".QPRelatedTests").html(data);
        });        
    }  
    var getSimilarTags=function(brdIds,rightSec){
        /* TODO */
	return;
	var relatedTags=rightSec.children(".QPRelatedTags");
        vReq.get("/questions/similarTags",{brdIds:brdIds,start:0,size:25},function(data){
           var tags=data.result,tagsHTML="",k=0;
           $.each(tags,function(name,value){
               tagsHTML+="<label class='quesTag'>"+name+"</label>("+value+") ";
               k++;
           });
           if(k>0)tagsHTML="<div class='boldy'>Related tags</div>"+tagsHTML;
           relatedTags.html(tagsHTML);
        });        
    }


    
    //comments and discussions event handler functions
    var showQuesSolns=function(){
        var solnsDiv=$("#QPMiddleSec .quesSolnsDiv");
        toggleQPTabs(solnsDiv,"quesSolns","quesDiscs");
        solnsDiv.html(qsInputerSample.children().clone(true));
        assignRTEs(solnsDiv.find(".inputerRTEDiv"),"Add Solution here");
        solnsDiv.find(".inputerRTEDiv .RTEHolder").attr("data-page","COMMENT");
        var LMHandlerDiv=solnsDiv.children(".LMHandlerDiv");
        var qid=solnsDiv.closest(".quesSolnsDiscsHolder").data("qid");
        LMHandlerDiv.data("urlStr","/questions/quesSolutions").data("size",25).data("allParams",{qId:qid,orderBy:"timeCreated"});
        showTopLoader();
        var params={start:0,size:5,qId:qid,orderBy:"timeCreated",attempted:questionPageAttemptedQues};
        vReq.get("/questions/quesSolutions",params,function(data){
            hideTopLoader();
            LMHandlerDiv.html(data);
            loadMJEqns(LMHandlerDiv.get(0));               
        });
    }
    var showQuesDiscs=function(){
        var discsDiv=$("#QPMiddleSec .quesDiscsDiv");
        toggleQPTabs(discsDiv,"quesDiscs","quesSolns");
        if(discsDiv.html()==""){
            var qid=discsDiv.closest(".quesSolnsDiscsHolder").data("qid");
            var LMData={urlStr:"/widgets/commItems",size:10,start:0,parent:{id:qid,type:"QUESTION"},orderBy:"timeCreated"};
            var allParams={};
            allParams.parent = {id:qid,type:"QUESTION"};allParams.baseId=qid;allParams.baseType="QUESTION";
            allParams.callBack="QUES_DISCUSSION";allParams.placeHolder="Add discussion here";
            allParams.attempted=questionPageAttemptedQues;
            initCommWidget(allParams,LMData,discsDiv);
        }        
    }   
    var toggleQPTabs=function(activeDiv,active,victim){
        activeDiv.removeClass("nonner").siblings("."+victim+"Div").addClass("nonner");
        activeDiv.closest(".quesSolnsDiscsHolder").find("."+active+"Tab").addClass("activeTextTab")
        .siblings("."+victim+"Tab").removeClass("activeTextTab");            
    }
    var submitQSInputClick=function(){
        var $this=$(this),soln=getInputerVal($this);
        var solnsDiv=$this.closest(".quesSolnsDiv");
        if(soln!="" && soln!="Add Solution here"){
            $(this).closest(".inputerRTE").removeClass("activeInputerRTE");
            var quesSoln=qsSample.children().clone(true);
            quesSoln.find(".quesSolnText").html(soln);
            var LMHandlerDiv=solnsDiv.children(".LMHandlerDiv");
            LMHandlerDiv.prepend(quesSoln);
            LMHandlerDiv.children(".userMessage").remove();
            var commNoEl=$this.closest(".quesSolnsDiscsHolder").find(".quesSolnsTabNo");
            increaseCount(commNoEl);
               var params={qId:solnsDiv.closest(".quesSolnsDiscsHolder").data("qid"),content:soln};        
            $.post("/questions/addSolution",params,function(data){
                var newSoln=solnsDiv.children(".LMHandlerDiv").children(".quesSoln").first();
                newSoln.data("solId",data.result.id);
                newSoln.find(".upVoteItem").data("entityId",data.result.id);
                loadMJEqns(newSoln.get(0));
            });
        }else{
		showError("Please enter a solution!");
	}        
    }    
    var showQSCommentsClick=function(){
        var commHolder=$(this).closest(".quesSoln").findFirst(".QSCommentsHolder");
        var solId=$(this).closest(".quesSoln").data("solId");
        if(commHolder.html()==""){
            var LMData={urlStr:"/widgets/commItems",size:10,start:0,parent:{type:"SOLUTION",id:solId},orderBy:"timeCreated"};
            var allParams={};
            allParams.parent = {id:solId,type:"SOLUTION"};
            allParams.root={id:$(this).closest(".quesSolnsDiscsHolder").data("qid"),type:"QUESTION"};
            allParams.base=allParams.root;
            allParams.callBack="QUES_SOLUTION";allParams.placeHolder="Add comment here";
            initCommWidget(allParams,LMData,commHolder);
        }
        $(this).toggleClass("showQSComments hideQSComments");
        commHolder.removeClass("nonner");
        $(this).children("span").text("Hide Comments (");        
    }    
    var hideQSCommentsClick=function(){
        var commHolder=$(this).closest(".quesSoln").findFirst(".QSCommentsHolder");
        $(this).toggleClass("showQSComments hideQSComments");
        commHolder.addClass("nonner");
        $(this).children("span").text("Show Comments (");        
    }    
    
    
    //question analytics
    var showQuesAnalyticsClick=function(){
        var ques=$("#QPMiddleSec").children(".ques");
        var sourceEl=ques.find(".showQuesAnalytics");
        var qid=ques.data("qid");
        vQuestions.showQuestionAnalytics("/questions/quesGraph",{qId:qid,entityType:"QUESTION",
            entityId:qid,accessType:"DIRECT",type:ques.data("type")},sourceEl);
    };
    var showChallengeAnalyticsClick=function(){
        var $this=$("#QPRightSec").find(".showChallengeAnalytics,.showChallengeAnalyticsWarn"); 
        var $thisData=$this.data();
        vQuestions.showQuestionAnalytics("/questions/challengeGraph",{qId:$thisData.qid,
            accessType:"CHALLENGE_ANALYTICS",type:$thisData.type,
            entityId:$thisData.challengeId,entityType:"CHALLENGE"},$this);    
    };
    
    
    

    //warn section
    var qpWarnStr="Your attempt won't be considered in Analytics. Do you want to go ahead ?";
    var showQuesAnalyticsWarnClick=function(){
        showError(qpWarnStr,"showQuesAnalytics removeQuesAttemptWarns");
    }
    var quesSolnsTabWarnClick=function(){
        showError(qpWarnStr,"quesSolnsTab removeQuesAttemptWarns");
    }
    var quesDiscsTabWarnClick=function(){
        showError(qpWarnStr,"quesDiscsTab removeQuesAttemptWarns");
    }
    var showChallengeAnalyticsWarnClick=function(){
        showError(qpWarnStr,"showChallengeAnalytics removeQuesAttemptWarns");
    }
    var removeQuesAttemptWarnsClick=function(){
        removeQuesAttemptWarns(); 
    }
    var removeQuesAttemptWarns=function(){
    	var middleSec=$("#QPMiddleSec");        
        middleSec.find(".quesDiscsTabWarn").toggleClass("quesDiscsTabWarn quesDiscsTab");
        middleSec.find(".quesSolnsTabWarn").toggleClass("quesSolnsTabWarn quesSolnsTab");
        middleSec.find(".showQuesAnalyticsWarn").toggleClass("showQuesAnalyticsWarn showQuesAnalytics");
        $("#QPRightSec").find(".showChallengeAnalyticsWarn").toggleClass("showChallengeAnalyticsWarn showChallengeAnalytics");
    }    
    this.removeQuesAttemptWarns=removeQuesAttemptWarns;
    
    //right section
    var qpSQNextPrevClick=function(){
       	var $this=$(this);
	var QPS = $this.closest(".QPSimilarQuesnsDiv");
	var QPSQ = QPS.find(".QPSimilarQuesns");
	var total = QPSQ.data('totalHits');
	var cur = parseInt(QPSQ.find("#similarQuesCount").val());
	var addBy = parseInt($this.data("addBy"));
	var size = 2;
	var start = cur + addBy - size;
	if(!start && start!=0) return;
	if(start >= total){
		QPS.find(".QPSQNextNaN").removeClass("QPSQNext");
	}else{
		QPS.find(".QPSQNextNaN").addClass("QPSQNext");
	}
	if(start <= 0){
		start = 0;
		QPS.find(".QPSQPrevNaN").removeClass("QPSQPrev");
	}else{
		QPS.find(".QPSQPrevNaN").addClass("QPSQPrev");
	}
       	QPSQ.html("<div class='smallLoader'></div>");
       	var params={start:start,size:2};
       	params["entity"] = {"id":$this.closest("#QPRightSec").data("qid"),"type":"QUESTION"};
       	vReq.get("/questions/similarQuesns",params,function(data){
              QPSQ.html(data);
              loadMJEqns(QPSQ.get(0));
        });
    }        
    var qpSimilarQuesnsTabClick=function(){
        var $this=$(this);
        QPContentToggle($this,"QPSimilarQuesnsDiv","QPSimilarQuesns");
    }        
    var qpRelatedTestsTabClick=function(){
        var $this=$(this);
        QPContentToggle($this,"QPRelatedContent","QPRelatedTests");        
    }       
    var qpRelatedPLsTabClick=function(){
        var $this=$(this);
        QPContentToggle($this,"QPRelatedContent","QPRelatedPLs",{resultEntityType:"PLAYLIST"});        
    } 
    var qpRelatedDocsTabClick=function(){
        var $this=$(this);
        QPContentToggle($this,"QPRelatedContent","QPRelatedDocs",{resultEntityType:"DOCUMENT",excludeTypes:["video"]});            
    } 
    var qpRelatedVideosTabClick=function(){
        var $this=$(this);
        QPContentToggle($this,"QPRelatedContent","QPRelatedVideos",{resultEntityType:"DOCUMENT",includeTypes:["video"]});            
    } 
    var QPContentToggle=function($this,parentClass,targetClass,params){
        $this.addClass("activeTextTab").siblings(".textTab").removeClass("activeTextTab");
        var parent=$this.closest("."+parentClass);
        var target=parent.find("."+targetClass);
        target.removeClass("nonner").siblings().addClass("nonner");
        if(params&&target.html()==""){
            params.entity = {type:"QUESTION",id:$this.closest("#QPRightSec").data("qid")};
            params.start=0;
            params.size=3;params.target="QUESTION_PAGE";
            target.load("/widgets/relatedEntities",params);
        }
    }       
})(jQuery)
