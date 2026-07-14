var vAddQuestion=new (function($){    
    var qaOptsSample,quesAddQType="SCQ",webAppDomain="WEB_APP",domain=webAppDomain,
    clickEvent="click.vAddQuestion",docClickEvent="click.vAddQuestion",putrteContent,
    getrte,QADiv,editQuestionId; 
    var curEditQuesParams = null;
    this.init=function(params){
        QADiv=$("#quesAddContentSec");        
        var optsDiv=QADiv.find(".quesAddOptsDiv");
        qaOptsSample=optsDiv.clone(true);     
	domain=params.domain;
        
        
        if(domain===webAppDomain){       
            //sharing
	    if(orgId){
            	shareUi.openInst(orgId,orgName,"QUESTION",QADiv.find(".shareWithHolder"),false); 
	    }else{
            	shareUi.open("","QUESTION",QADiv.find(".shareWithHolder"),true);
	    }
        }
        
        QADiv.off(clickEvent)
        //For adding options and rte functions
        .on("change",".quesAddOptChoose input",quesAddOptChooseChange)
        .on(clickEvent,".quesAddOptCross",quesAddOptCrossClick)
        .on(clickEvent,".quesAddOptAdd",quesAddOptAddClick)

        .on("focus",".quesAddOptRTEDiv .RTEArea",rteAreaFocus)
        .on("focus",".QAQuesTextRTEDiv .RTEArea",quesAreaFocus)
        .on("blur",".quesAddOptRTEDiv .RTEArea",rteAreaBlur)
        .on(clickEvent,".QAGiveSoln",giveSoln)
        .on(clickEvent,".cancelQASoln",cancelSoln)
        .on(clickEvent,".QAAddHint",QAAddHint)
        .on(clickEvent,".removeQAHint",removeQAHint)
        .on(clickEvent,".QASolnAttachBtn",showVideoResourcePopup)
        .on(clickEvent,".removeVideoSolution",removeVideoSolution)
        
        $(document).off(docClickEvent)
        .on(docClickEvent,".QASubmit",qaSubmitClick)
        .on(clickEvent,".QARefresh",qaRefresh)
        .on(docClickEvent,".QACreateNewCopy",qaCreateNewCopy)
        .on(docClickEvent,".cancelEditQues",cancelEditQues)
        .on(docClickEvent,".QAEditSubmitConfirm",editSubmitConfirm)
        .on(docClickEvent,".getPopupResourcesFolderPage",showVideoResourcePopup)
        
        //download rte.js and assign rtes
        var divList=QADiv.find(".QARTEDiv");    
        if(params.isEditQuestion){
            editQuestionId=params.qid;
            addPara = params.addPara;
        }else{
            $(".quesAddMainDivWrapper").prepend("<div style='text-align:center;'><span style='color:#CC6666;font-weight:bold;'> NOTE : </span><span>Once the question is <strong>published</strong>,you cannot change <strong>Subjects,Topics,Question Type</strong> and <strong>Key</strong></span></div>");
            editQuestionId=null;
        }
        
        var cbfn=function(){
            assignRTEs(divList);
            putrteContent=vRTE.putRTEContent;
            getrte=vRTE.getRTEContent;              
            divList.each(function(){
                $(this).find(".RTEHolder").attr("data-page","CMDS-QUESTION");
            });
            if(params.isEditQuestion){
                if(params.addPara){
                    setUpEditQuestion(params,divList,addPara);
                }
                else{
                    setUpEditQuestion(params,divList);
                }
            };
        };        
        fetchScripts([{fname:"uicomWidgets/rte.js",cb:cbfn}]);
        
    };
    
    var setUpEditQuestion=function(params,rteDivList,addPara){
        var quesPojo=params.ques;
        quesAddQType=quesPojo.type;
        var publishStatus = quesPojo.published;
        setTimeout(function(){
            //disableEditQuesOptions();
            disableEditQuesOptions(publishStatus);
        },100);
        if(quesAddQType === "TEXT"){
            $(".QAQTypeSelect").find(".vChooseHead").text("Paragraph");
            $(".QAQTypeSelect").find(".vChooseDropDown").html("");
            if(addPara === "true"){
                $(".showParagraph").removeClass("nonner");
                $(".showParagraph").attr("id",editQuestionId);
                $(".showParagraph").attr("data-para-id",editQuestionId);
                $(".showParagraph").html(quesPojo.questionBody.newText);
                vChoose = $(".QALevelDiv").find(".vChoose");
                vAddQuestion.onQAQTypeSelectChange(vChoose,"PARAQUESTION");
                QADiv.find(".QAQTypeSelect").attr("data-value","PARA");
                $(".QALevelSelect .vChooseOpt").not(".vChooseOptActive").fadeOut();
                return ;
            }
        }
        else if(quesAddQType === "SUBJECTIVE"){
            $(".questionHeading").html("Enter subjective question here <span id='info-on-hover' title='Subjective type questions needs to be manually graded.'><img src='/public/images/info30.png' class='displayInBlock' width='15px'></span>");
            $(".quesAddOptsDiv").html("");
            $(".QAQTypeSelect").css("pointer-events","none");
        }
        else{
            $(".QAQTypeSelect .vChooseDropDown").find(".vChooseOpt:last").fadeOut();
        }
        var quesTextContent=quesPojo.questionBody.newText;
        putrteContent(rteDivList.eq(0).find(".RTEArea"),quesTextContent);
        var solutionInfo=quesPojo.solutionInfo;
        if(solutionInfo){
            var options=solutionInfo.optionBody.newOptions;
            if(options.length>0){
                for(var k=1;k<rteDivList.length;k++){
                    putrteContent(rteDivList.eq(k).find(".RTEArea"),options[k-1]);
                }                
            }            
            var solutions=solutionInfo.solutions;
            if(solutions.length>0){
                var soln=solutions[0];
                var target=QADiv.find(".QASolnDiv");
                target.addClass("QASolnDivActive");
                putrteContent(target.find(".RTEArea"),soln.newText);
		var attachments = soln.attachmentsInfo;
		if(attachments && attachments.length>0){
			for(var v=0;v<attachments.length;v++){
				var entity = attachments[v].entity;
				var info = attachments[v].info;
				entity.title = info ? info.name : i18nJS("TXT_NOT_AVAIALABLE");
				addVideoSoultion(attachments[v].entity);	
			};
		}
            }
            var opts=QADiv.find(".quesAddOpt");
            var answers=solutionInfo.answer;
            if(answers){
                if(quesAddQType==="SCQ"){
                    try{
                        var target=opts.eq(parseInt(answers)-1);
                        target.prop("checked",true);
                        var row=target.closest("tr");
                        row.addClass("QAOCorrectRow").siblings()
                                .removeClass("QAOCorrectRow");
                    }catch(err){}                    
                }else if(quesAddQType ==="MCQ" || quesAddQType ==="MATRIX"){
                    for(var k=0;k<answers.length;k++){
                        opts.eq(parseInt(answers[k])-1)
                                .prop("checked",true);
                    }
                }else if(quesAddQType==="NUMERIC"){
                    prepareNumericAndTextAnsArea(opts.closest(".quesAddOptsDiv"));
                    QADiv.find(".QATextNumInput").val(answers);
                }                
            }
            else{
                if(quesAddQType === "TEXT"){
                    $(".quesAddOptsDiv").html("");
                    $(".QASolnDiv").addClass("nonner");
                }
            }
        }
        var hintsPojo=quesPojo.hints;        
        if(hintsPojo){
          var  hints=hintsPojo.hints;
          if(hints.length>0){
            for(var k=0;k<hints.length;k++){
                var hint=hints[k];
                var targetRTEDiv=QAAddHint();
                putrteContent(targetRTEDiv.find(".RTEArea"),hint.newText);
            }              
          }
        }
    };

    var disableEditQuesOptions = function(publishStatus){
        $(".QACreateNewCopy").removeClass("nonner");
         if(publishStatus == true){
            // $(".QAQuesTextRTEDiv .RTEArea").attr("contentEditable",false);
            // $(".quesAddOptRTEDiv .RTEArea").attr("contentEditable",false);
            // $(".quesAddOptsDiv .quesAddOpt").attr("disabled","disabled");
            // $(".QAQuesTextRTEDiv .RTEArea, .quesAddOptRTEDiv .RTEArea, .QAQTypeSelect, .ATTHolder, .ATSelectSubjectOpt").css("cursor","not-allowed");
            $(".quesAddMainDivWrapper").prepend("<div style='text-align:center;'><span style='color:#CC6666;font-weight:bold;'> NOTE : </span>You can only edit <strong>Question Level</strong>,<strong>Questions Text, Options Text , Solution Text and Hints.</strong></div>");
            $(".QATextNumInput").attr("disabled","disabled");
            // $(".RTEToolBar").addClass("nonner");
            $(".QAQTypeSelect").css("pointer-events","none");
            $(".ATSelectBoxHead").css("pointer-events","none");
            $(".ATTTRemoveTag").css("pointer-events","none");
            $(".instSelMySubject").css("pointer-events","none");
            $(".ATSelectSubjectOpt").attr("disabled","disabled");
            $(".ATTHolder").find(".ATSelectOptList .ATCRBox").attr("disabled","disabled");
            $(".ATTagTreeDiv").find(".ATRemImg").removeClass("ATTTRemoveTopic ATTTRemoveTag");
         }
    }
    //qType Change
    var prepareNumericAndTextAnsArea=function(optsDiv){
        optsDiv.addClass("nonner");
        $(".QASolnDiv").prepend("<div class='numericDiv'>Answer is: <input type='text' class='QATextNumInput'/>\n\
        <div class='smally color8'>Provide only numerical answers. For eg : 100 (correct),\n\
         10.5 (correct), 2/3 (wrong), 10g (wrong)</div></div>");
    };
    this.onQAQTypeSelectChange=function(vChoosevSelect,value){
        $(".numericDiv").addClass("nonner");
        var qType=value;
        var paraId = $(".showParagraph").attr("id");
        if((paraId != null || paraId !=undefined) && qType!="PARAQUESTION"){
            if(paraId!=""){
               showMessage("Subjects and Topics are cleared");
               qaRefresh();
            }
        }
        if(value === "PARAQUESTION"){
            $(".ATSelectSubjectOpt").attr("disabled","disabled");
            $(".ATTHolder").find(".ATSelectOptList .ATCRBox").attr("disabled","disabled");
            $(".ATTagTreeDiv").find(".ATRemImg").removeClass("ATTTRemoveTopic ATTTRemoveTag");
            qType = "MCQ";
        }
        var  opts=qaOptsSample.children().clone(true);
        quesAddQType=qType;
        var QADiv=$(vChoosevSelect).closest("#quesAddContentSec"),
                optsDiv=QADiv.find(".quesAddOptsDiv");
        optsDiv.find("tr").removeClass("QAOCorrectRow");
        if(qType=="NUMERIC"){
            prepareNumericAndTextAnsArea(optsDiv);
        }
        else if(qType=="SCQ"){
            // optsDiv.html(opts);
            $(".numericDiv").addClass("nonner");
            optsDiv.find(".quesAddOptChoose").each(function(){
                $(this).html("<input type='radio' name='quesAddOpt' class='quesAddOpt'/>");
            });
        }
        else if(qType=="MCQ" || qType=="MATRIX"){
            $(".numericDiv").addClass("nonner");
            // optsDiv.html(opts);
            optsDiv.find(".quesAddOptChoose").each(function(){
                $(this).html("<input type='checkbox' name='quesAddOpt' class='quesAddOpt'/>");
            });
        }
        if(qType === "PARA"){
            // optsDiv.html("");
            optsDiv.addClass("nonner");
            $(".questionHeading").text("Enter a Paragraph");
            $(".QASolnDiv").addClass("nonner");
            $(".QAHintsDiv").addClass("nonner");
        }
        else if(qType === "NUMERIC"){
            $(".questionHeading").text("Enter question here");
            $(".QASolnDiv").removeClass("nonner");
            $(".QAHintsDiv").removeClass("nonner");
        }
        else if(qType === "SUBJECTIVE"){
            $(".questionHeading").html("Enter subjective question here <span id='info-on-hover' title='Subjective type questions needs to be manually graded.'><img src='/public/images/info30.png' class='displayInBlock' width='15px'></span>");
            optsDiv.addClass("nonner");
            $(".QASolnDiv").removeClass("nonner");
            $(".QAHintsDiv").removeClass("nonner");
        }
        else{
            optsDiv.removeClass("nonner");
            $(".questionHeading").text("Enter question here");
            $(".QASolnDiv").removeClass("nonner");
            $(".QAHintsDiv").removeClass("nonner");
        }
        // assignRTEs(QADiv.find(".quesAddOptRTEDiv"));
    }    
    
    
    //For adding options and rte functions
    var quesAddOptChooseChange=function(){
        var row=$(this).closest("tr");
        var paraId = $(".showParagraph").data("paraId");
        if(quesAddQType=="SCQ"){
            row.addClass("QAOCorrectRow").siblings().removeClass("QAOCorrectRow");
        }
        else if(quesAddQType=="MCQ" || paraId!="" || quesAddQType=="MATRIX"){
            if($(this).prop("checked"))row.addClass("QAOCorrectRow");
            else row.removeClass("QAOCorrectRow");
        }                
    }
    var quesAddOptCrossClick=function(){
        var tBody=$(this).closest(".quesAddOptsTable").children("tbody");
        $(this).closest("tr").remove();
        resetQuesAddOptNum(tBody);            
    }    
    var quesAddOptAddClick=function(){
        var paraId = $(".showParagraph").data("paraId");
        var tBody=$(this).closest(".quesAddOptsDiv").findFirst("table tbody");
        var tr=QAOptSample.children("table").children("tbody").children().clone(true);
        if(quesAddQType=="MCQ" || quesAddQType=="MATRIX" || paraId!=""){
            tr.find(".quesAddOptChoose").html('<input type="checkbox" class="quesAddOpt" name="quesAddOpt">');
        }
        tBody.append(tr);
        resetQuesAddOptNum(tBody);
        assignRTEs(tr.find(".quesAddOptRTEDiv"));        
        tr.find(".quesAddOptRTEDiv .RTEHolder").attr("data-page","CMDS-QUESTION");
    }
    var resetQuesAddOptNum=function(tBody){
        var i=1;
        tBody.children("tr").children("td.quesAddOptHead").each(function(){
            $(this).text("Option "+i);
            i++;
        });
    }


    var rteAreaFocus=function(){
        $(this).closest(".quesAddOptRTEDiv").addClass("activeQuesAddOptRTEDiv");
    }
    var quesAreaFocus = function(){
        //Commenting for time being
        // $(".checkDuplicates").removeClass("nonner");
    }
    var rteAreaBlur=function(){
        $(this).closest(".quesAddOptRTEDiv").removeClass("activeQuesAddOptRTEDiv");
    };
    var addVideoSoultion = function(video){
	//console.log(video);
    	var sampleVideo = "<div class='eachVideoSol' data-video-id='%{entityId}'> \n\
		<span class='resourceIconVIDEO'></span> \n\
		<a class='getVideoPage cmdsaPush' title='%{entityName}' data-entity-id='%{entityId}' \n\
   		href='/organization/%{orgId}/video/%{entityId}'> \n\
    		<span class='singleLineText displayInBlock'>%{entityName}</span> </a> \n\
		<span class='removeVideoSolution'>X</span></div>";
	var title = video.title || video.name;
	var videoHTML = sampleVideo.replaceAll("%{entityName}",title);
	videoHTML = videoHTML.replaceAll("%{entityId}",video.id);
	videoHTML = videoHTML.replaceAll("%{orgId}",orgId);
	$(".QASolnAttachBtn").addClass("nonner");
	var holder = $(".QASolnAttachHolder").removeClass("nonner");
	holder.append(videoHTML);
    };
    var showVideoResourcePopup = function(){
    var folderId = $(this).data("entityId");
	showResourcesPopup("CMDSVIDEO",false,folderId,null,function(selectedVideos){
        var check = checkResourceType(selectedVideos);
        if(check == false){
            return ;
        }
		for(var v=0;v<selectedVideos.length;v++){
			addVideoSoultion(selectedVideos[v]);	
		};
	});	
    };
    var removeVideoSolution = function(){
	var $this = $(this);
	var vid = $this.closest(".eachVideoSol");
	vid.remove();
	if(QADiv.find(".eachVideoSol").length == 0){
		$(".QASolnAttachBtn").removeClass("nonner");
	        $(".QASolnAttachHolder").addClass("nonner");
	}
    };
    var checkResourceType = function(video){
        if(video[0].type != "CMDSVIDEO"){
            showError("Please add only videos here");
            return false;
        }
    }
    var getSolutionObj = function(){
        var getRTE=vRTE.getRTEContent;
	var RTEHolder = QADiv.find(".QASolnRTEDiv").children(".RTEHolder");
	var videoSol = QADiv.find(".eachVideoSol");
        if(!vRTE.isRTEEmpty(RTEHolder) || videoSol.length>0){
           var solnText = getRTE(RTEHolder);
	   var vidEntities = [];
	   videoSol.each(function(){
		var entity = {entity:{
			type:"CMDSVIDEO",
			id:$(this).data("videoId")}
		};
		vidEntities.push(entity);
	   });
	   var sol = {
		content : solnText,
		attachments : vidEntities
	   };
	   return sol;
	}else{
		return;
	}
    };
    var giveSoln=function(){
        $(this).closest(".QASolnDiv").addClass("QASolnDivActive");
        $(this).closest(".QASolnDiv").find(".QASolnHead").removeClass("nonner");
    };
    var cancelSoln=function(){
        var target=$(this).closest(".QASolnDiv");
        target.find(".QASolnHead").addClass("nonner");
        target.removeClass("QASolnDivActive");
        putrteContent(target.find(".RTEArea"),"");
    };    
    var QAAddHint=function(){
        var addHintEl=QADiv.find(".QAAddHint");
        var hintDiv=addHintEl.siblings(".QAHintCloneDiv").clone(true);
        hintDiv.removeClass("nonner QAHintCloneDiv").insertBefore(addHintEl);
        var rteDiv=hintDiv.children(".QAHintRTEDiv");
        assignRTEs(rteDiv);     
        rteDiv.find(".RTEHolder").attr("data-page","CMDS-QUESTION");
        return rteDiv;
    };
    var removeQAHint=function(){
        $(this).closest(".QAHintDiv").remove();
    };

    var qaCreateNewCopy = function(){
        $(this).addClass("clicked");
        qaSubmitClick();
    }

    var qaSubmitClick=function(){
        $(".QACreateNewCopy").addClass("btnDisabled");
        var qaSubmitEl=$(".QASubmit");
        var urlStr="submitQuestion",quesParams={},$this=$(this);
        var target="PUBLISH_QUESTION";
        var isEditQuestion=false;
        if($(".QACreateNewCopy").hasClass("clicked")){
            var target = "PUBLISH_QUESTION";
            isEditQuestion = false;
            urlStr = "submitQuestion";
        }
        else if(editQuestionId && addPara!="true"){
            var target="EDIT_QUESTION";
            isEditQuestion=true;
            urlStr="editQuestion";
        }
        else{
            var target = "PUBLISH_QUESTION";
            isEditQuestion = false;
            urlStr = "submitQuestion";
        }
        quesParams.target=target;
        
        if($this.hasClass("QASubmitPreview")){
            urlStr="previewQuestion";             
        }        

        var QADiv=$(this).closest("#quesAddContentSec");
        if(QADiv.length===0){
            QADiv=$("#quesAddContentSec");
        }
        
        var qType=QADiv.find(".QAQTypeSelect").data('value');
        
        //question,options,solution,answer
        var getRTE=vRTE.getRTEContent;
	    var contentRTE = QADiv.find(".QAQuesTextRTEDiv").children(".RTEHolder");
        var tagsJson=returnAllTagsAdded(QADiv.find("#addTagsMasterDiv"));
        if(tagsJson.subjectIds.length==0||tagsJson.topicIds.length==0){
            showError("Please select atleast one Subject and Topic");
            return;
        }
        var difficulty=QADiv.find(".QALevelSelect").data("value");
        if(difficulty=="-1"){
            showError("Please provide the difficulty");
            return;
        }
        if(vRTE.isRTEEmpty(contentRTE)){
            showError("Please enter Question");
            return;
        }
        quesParams.content=getRTE(contentRTE);

        if(quesParams.content.length>25000){
            showError("Question text length is too long.");
            return;            
        }

        var options=[],answer=[];
        var paraId = $(".showParagraph").data("paraId");
        if((qType=="MCQ"||qType=="SCQ"||qType=="MATRIX")|| (qType ==="PARA" && paraId!="")){
            var trs=QADiv.find(".quesAddOptsTable tbody").find("tr");
            for(var i=0;i<trs.length;i++){
                var opt=getRTE(trs.eq(i).find(".RTEHolder"));
                if(opt.length>0){
                    options.push(opt);
                    if(trs.eq(i).find(".quesAddOpt").prop("checked"))answer.push(i+1);
                }
            }
        }
        else if(qType=="NUMERIC"){
            var ans=QADiv.find(".QATextNumInput").val();
            if(ans.trim().length>0)answer.push(ans);
        }

        if((qType === "MCQ" || qType ==="SCQ"||qType === "MATRIX" || (qType ==="PARA" && paraId!="")) &&(options.length<2||options.length>5)){
            showError("Please enter minimum two options and a maximum of five options");
            return;
        }
        quesParams.options=options;
        // console.log(qType);
        if((qType !=="PARA" || paraId !="") && qType !="SUBJECTIVE" && qType!="TEXT" && answer.length==0){
            showError("Please give an answer to the question");
            return;        
        }else if(qType=="NUMERIC"){
            var numericAns=answer[0];
            if(numericAns.length>11){
                showError("Numeric Answer should not exceed 11 characters");
                return;   
            }        
            else if(!checkFloatNum(numericAns)){
                showError("Please enter a numeric answer");
                return;               
            }    
            else {
                answer[0]=parseFloat(numericAns);
            }
        }
        quesParams.answers=answer;  
        var soln = getSolutionObj();
        if(soln){
            quesParams.solution=soln;
        }
        var controller="questions";
        quesParams.difficulty=difficulty;        
        var refNo=QADiv.find(".origRefNo").val();
        if(refNo){
            quesParams.source=refNo;
        }        
        //tags selected
        if(qType === "PARA"){
            if($(".showParagraph").data("paraId") === ""){
                quesParams.type = "TEXT";
            }
            else{
                quesParams.paraId = $(".showParagraph").attr("id");
                quesParams.type = qType;
            }
        }
        else{
            quesParams.type=qType;
        }
        quesParams.targetIds=tagsJson.targetIds;
	if(tagsJson.normalTags){
        	quesParams.tags=tagsJson.normalTags;
	}
        quesParams.brdIds=tagsJson.brdIds;       
        if(editQuestionId && addPara!="true" && !$(".QACreateNewCopy").hasClass("clicked")){
	    /*var updateList = [];
            for (key in quesParams) {
            	updateList.push(key);
            }
	    quesParams.updateList = updateList; NOT REQ BY BACKEND */
            cancelCommonPopup(); // HIDE PREVIEW POPUP ON SAVE
            quesParams.questionId=editQuestionId;
            quesParams["entity.id"]=editQuestionId;
            quesParams["entity.type"]="CMDSQUESTION";
        }
        if(urlStr==="previewQuestion"){
            quesParams.exam=tagsJson.exams;
            quesParams.subject=tagsJson.subjects[0];
            quesParams.topics=tagsJson.topics;
            quesParams.subTopics=tagsJson.subTopics;
        }    
        
        if(domain==webAppDomain){            
            var shareParams;
            try{
                    shareParams=shareUi.getShareParams(QADiv.find(".shareWithHolder"));
                    quesParams.scope=shareParams.scope;
                    quesParams.shareWith=shareParams["with"];
            }catch(err){}                        
		if(orgId){
			quesParams.scope = "ORG";
			quesParams.contentSrc = {"id":orgId,"type":"ORGANIZATION"};
		}
        }else{
            controller="QrQuestions";
            var hintsDivs=QADiv.find(".QAHintRTEDiv").not(".QAHintCloneDiv .QAHintRTEDiv");
            var hints=[];
            for(var p=0;p<hintsDivs.length;p++){
                var hintRTE= getRTE(hintsDivs.eq(p).find(".RTEHolder"));
                if(hintRTE.length>0&&hintRTE!="<br>"){
                    hints.push(hintRTE);
                }
            }
            quesParams.hints=hints;
            var folderId=QADiv.closest("#addContentPage").data("folderId");
            if(folderId != "null" && folderId != undefined && folderId != null){
                quesParams.folderId=folderId;
            }
        }

        showTopLoader();
        $this.text($this.data("ingText"));
        qaSubmitEl.css("opacity","0.5").removeClass("QASubmit");
        var completeFn=function(){
            qaSubmitEl.addClass("QASubmit").css("opacity","1");
        };
	curEditQuesParams = quesParams;
        vReq.post("/"+controller+"/"+urlStr,quesParams,function(data,text,xhr){
            $(".QACreateNewCopy").removeClass("btnDisabled");
            if(data.result!= null || data.result != undefined){
                if(quesParams.type === "TEXT"){
                    $(".showParagraph").removeClass("nonner");
                    $(".showParagraph").attr("id",data.result.paraId);
                    $(".showParagraph").data("paraId",data.result.paraId);
                    $(".showParagraph").html(quesParams.content);
                }
            }
            hideTopLoader();
            completeFn();
            $this.text($this.data("normalText"));
            if(urlStr =="previewQuestion"){
                getCommonPopupBody().html(data);
                loadMJEqns($("#commonPopupHolder").get(0));
                if(quesParams.type === "PARA" && $(".showParagraph").data("paraId") !=""){
                    $("#previewQuestionDiv").find($(".showParagraphPreview")).removeClass("nonner");
                    $(".showParagraphPreview").html($(".showParagraph").html());
                    $(".showParagraphPreview").css("margin","20px");
                }
            }
            else{   
		var respType = xhr.getResponseHeader("Response-Type");
		if(respType == "html"){
            var popup = showVPopup();
            popup.html(data);
            return;
		}
                if(domain==webAppDomain){
                    if(orgId){
				history.back();
			}else{
				goToHomePage();
                   		pushHistory(null,null,"/");                           
			}    
                }else{
                    $(window).scrollTop(0);
                    cancelCommonPopup();
                    var message;
                    if(quesParams.type === "TEXT"){
                        if(isEditQuestion === true){
                            message = "Paragraph saved successfully";
                        }
                        else{
                            message = "Paragraph added successfully <br>Now start adding questions to paragraph";
                        }
                    }
                    else{
                        message ="Question added successfully";
                    }
                    try{
                        var tabFlag = $(".addQuestion").val();
                        if(tabFlag === "addMultiQuestion"){
                            if(target === "EDIT_QUESTION"){
                                refreshPage();
                            }
                            else if($(".QACreateNewCopy").hasClass("clicked")){
                                if(data.result!= null || data.result != undefined){
                                    pushHistory(null, null, "/organization/" + orgId + "/question/"+ data.result.id);
                                    goToQuesPage(data.result.id);
                                }
                            }
                            emptyQuesAddDiv(qType);
                        }
                        else{
                            qrAddContent.loadQuesAddArea();
                        }
                    }catch(err){
                        console.log(err);
                        refreshPage();
                        message="Question saved successfully";                    
                    }
                    trackEventForGA("CMDSQUESTION","ADD_CONTENT","");
                    showMessage(message);  
                }                           
            }        
        },null,completeFn);        
    };
    var qaRefresh = function(){
        var refresh = 'multiple';
        qrAddContent.loadQuesAddArea(refresh);
    }
    var emptyQuesAddDiv = function(qType){
        $(".RTEArea").each(function(){
            $(this).html("");
        })
        $(".QATextNumInput").val("");
        vChoose = $(".QALevelDiv").find(".vChoose");
        if(qType === "TEXT" || qType === "PARA"){
            $(".QALevelSelect .vChooseOpt").not(".vChooseOptActive").fadeOut();
        }
        else{
            vChooseVar.reset(vChoose,"-1","Choose")
        }
        $(".QALevelSelect").find(".vChooseOpt").removeClass("vChooseOptActive");
        $(".QALevelSelect").find(".vChooseOpt").removeClass("vChooseOptTicked");
        if(qType === "TEXT" || qType === "PARA"){
            vAddQuestion.onQAQTypeSelectChange(vChoose,"PARAQUESTION");
        }
        else{
            vAddQuestion.onQAQTypeSelectChange(vChoose,qType);
        }
        $(".eachVideoSol").remove();
        if(QADiv.find(".eachVideoSol").length == 0){
            $(".QASolnAttachBtn").removeClass("nonner");
            $(".QASolnAttachHolder").addClass("nonner");
        }
    }
    var cancelEditQues = function(){
	$(window).scrollTop(0);
        cancelCommonPopup();
        try{
            qrAddContent.loadQuesAddArea();
        }catch(err){
            refreshPage();
        }
    };
    var editSubmitConfirm = function(){
	if(!curEditQuesParams) return;
	var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
	var params = curEditQuesParams;
	params.editEntities = [];
    var checkBoxcount = 0;
	$(".eachEditQuesAssociation").each(function(index,div){
		var $this = $(this);
        if($(this).find(".check").is(":checked") === true){
            checkBoxcount++;
            var editEntity = {entity:{id:$this.data("id"),type:$this.data("type")}};
            editEntity.editType = $(this).find(".check").val();
            params.editEntities.push(editEntity);
        }
        // console.log($(".eachEditQuesAssociation input:checked").val());
	});
    if(checkBoxcount == 0){
        showError("Please select atleast one test");
        return ;
    }
	$this.addClass("btnDisabled");
	showTopLoader();
	vReq.post("/QrQuestions/editQuestion",params,function(data,nn,xhr){
		$this.removeClass("btnDisabled");
		hideTopLoader();
		var respType = xhr.getResponseHeader("Response-Type");
		if(respType == "html"){
			var popup = showVPopup();
			popup.html(data);
			return;
		}else{
			closeVPopup();
		}
		if(data && data.result){
			cancelEditQues();
			showMessage("Question saved successfully");
		}else{
			showError("Failed to edit the question!");
		}
	},function(){
		$this.removeClass("btnDisabled");
		hideTopLoader();
	}); 
    };
})(jQuery);
var onQAQTypeChange=function(vChoose,value){
    vAddQuestion.onQAQTypeSelectChange(vChoose,value);
};
