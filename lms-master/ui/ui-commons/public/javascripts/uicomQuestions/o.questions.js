//home page quesns tabs
var vQuestions=new (function($){
    var quesAttemptTimeStore={};
    this.init=function(){
        $(document)
        //event handlers on question entity  

        .on("click",".attemptQues",attemptQuesClick)
        .on("click",".quesAnsSubmit",quesAnsSubmitClick)
        .on("click",".quesCancelAns",quesCancelAnsClick)
        .on("click",".quesAnsDiv",quesAnsDivClick)


        //others
        .on("click",".SSCSearchDivImg",sscSearchDivImgClick)          
    }        
    
    //event handler functions on question entity  
    var attemptQuesClick=function(e){
       e.stopPropagation();
       var ques=$(this).closest(".ques");
       ques.addClass("quesAddAnsState");
        setTimeOfAttempt(ques.data("qid"));        
    };
    var setTimeOfAttempt=function(qId){
        var ques=quesAttemptTimeStore[qId];
        if(!ques){
            ques=quesAttemptTimeStore[qId]={};            
        }
        ques.startTime=new Date().getTime();
    };
    var getTimeOfAttempt=function(qId){
        var ques=quesAttemptTimeStore[qId];
        if(!ques&&!ques.startTime){
            //if for some unknown reasons, the start time is compromised
            return 5000;
        }else{
            return (new Date().getTime()-ques.startTime);
        }
    };
    var quesAnsSubmitClick=function(e){
       e.stopPropagation();
       var ansBlock=$(this).closest(".quesAnsDiv").find(".addAnsBlock"),qType=ansBlock.data("type");
       var ansStatus=checkQuesAnsUtils.quesAnsSubmit[qType](ansBlock);
       if(ansStatus.result){
           sendAnswer(ansBlock,ansStatus.ans,$(this));
       }
       else{
           showError(ansStatus.error);
       }            
    }
    var quesCancelAnsClick=function(e){
       e.stopPropagation();
       var ques=$(this).closest(".ques");
       ques.removeClass("quesAddAnsState");        
    }
    var quesAnsDivClick=function(e){
        e.stopPropagation();
    }    
    
    //others
    var sscSearchDivImgClick=function(){
       $(this).parent().toggleClass("openedSSCSearchDiv");
       $(this).siblings("input").focus();        
    }
    
    
    //utilities
    this.showQuestionAnalytics=function(urlStr,params){
        showTopLoader();
        params.endListAtIndex=5;
        params.start=0;
        params.size=10;
        $.get(urlStr,params,function(data){
            hideTopLoader();
            if(data.trim().length>0){                            
                var popup=getCommonPopupBody(575);
                popup.html(data);
            }
        });        
    };
    var sendAnswer=function(ansBlock,answer){
        var qid=ansBlock.data("qid");
        ansBlock.closest(".quesAnsDiv").find(".quesAnsSubmit").text("Submitting..");
        var showGraph=false,qType=ansBlock.data("type"),submitPlace="";
        if(qType==="SCQ"||qType==="MCQ"||qType==="NUMERIC")showGraph=true;
        if(ansBlock.closest(".ques").hasClass("QPQues"))submitPlace="QUESTION_PAGE";
        showTopLoader();
        var quesHref;
        try{
            quesHref=ansBlock.closest(".ques").find(".quesTextDiv").attr("href");
        }catch (err){
            
        }
        vReq.post("/questions/submitAnswer",{qId:qid,answerGiven:answer
            ,timeTaken:getTimeOfAttempt(qid),entityId:qid,entityType:"QUESTION",status:"COMPLETE",
            submitPlace:submitPlace,type:qType},function(data){
            hideTopLoader();
            trackEventForGA("QUESTION","ATTEMPT",qid);
               if(submitPlace!=="QUESTION_PAGE"){               
                   if(quesHref){
                        pushHistory(null , null,quesHref);
                   }                   
                   $("#noTabSection").html(data);
               }
               else{
                    var ques=ansBlock.closest(".ques");
                    ques.removeClass("quesAddAnsState");
                    increaseCount(ques.find(".QPAttemptCount"));
                    ques.find(".attemptQues").removeClass("attemptQues").html(ques.find(".QPAttemptCountDiv").html());
                    ques.find(".QPSubmitAnsHelper").html(data);
                    vQuestionPage.removeQuesAttemptWarns();
               }
               if(showGraph){
                   vQuestions.showQuestionAnalytics("/questions/quesGraph",
                   {qId:qid,entityType:"QUESTION",entityId:qid,
                       accessType:"INDIRECT",type:qType});
               }
        });
    }  
    
    
    
this.createQAGraph=function(qStats){
        var QAPHolder=$("#quesAnalyticsPopup");
        var options=qStats.answerGivenCounts;
        if(options.length<4){
            options=getMoreOptions(options);
        }
        var correctAnsStr=qStats.correctAnswerGivenCount.answerGiven.join(",");
        var myAnsStr="";
        if(qStats.myAnswer){
            myAnsStr=qStats.userAnswerGivenCount.answerGiven.join(",");
        }
        var barHTML="<div class='BGBarDiv'>\n\
                <div class='BGBarText'></div>\n\
                <div class='BGBar'></div>\n\
            </div>";
        var barHTMLSample=makeHTMLTag('div');
        barHTMLSample.html(barHTML);
        var totalCount=qStats.measures.attempts;
        for(var n=0;n<options.length;n++){
            var opt=options[n];
            var bar=barHTMLSample.children().clone(true);
            var percent=parseInt(opt.count*100/totalCount);
            bar.find(".BGBarText").text(percent+"%");
            var left=((n*45)+((n+1)*20))+"px";
            bar.css("left",left);
            bar.find(".BGBar").css("height",(percent*210)/100);
            QAPHolder.find(".BGBarsHolder").append(bar);
	    var answerGiven = opt.answerGiven;
	    if(qStats.type != "NUMERIC"){
		$(answerGiven).each(function(index,value){
			answerGiven[index] = allAlphabets.charAt(value-1);
		});
	    }
            var optClass="",statusText="",optText=answerGiven.join(",");
            if(correctAnsStr===optText){
                statusText="Correct Answer";
                optClass=" BGXAxisItemCorrect";
            }
            else {
                statusText="";
                optClass="";
            }
            if(myAnsStr===optText){
                statusText="Your Answer";
                optClass+=" BGXAxisItemMyAns";
            }
            QAPHolder.find(".BGXAxisDiv").append("<div class='BGXAxisItem"+optClass+"' style='left:"+left+"'>\n\
            <span class='BGXAxisItemOptText'>"+optText+"</span><div class='BGXAxisItemStatus'>"+statusText+"</div></div>");
        }
       QAPHolder.find(".barGraphArea").css("width",(70*options.length));
       addToggler(QAPHolder,$(".showQuesAnalytics"));
    };   
    var getMoreOptions=function(options){
        var answersList=[];
        for(var p=0;p<options.length;p++){
            var answer=options[p].answerGiven;
            answersList.push(answer.join(","));
        }
        for(var k=1;k<5;k++){
            if(answersList.indexOf(k.toString())===-1){
                options.push({count:0,answerGiven:[k.toString()]});
            }
            if(options.length>=4){
                break;
            }
        }
        return options;
    };
})(jQuery);
vQuestions.init();





