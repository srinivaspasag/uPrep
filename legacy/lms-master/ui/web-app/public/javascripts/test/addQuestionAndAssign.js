testCreation.addQuestions = {
	loadUrl:"/Tests/getAddQuestions",
	/*getQuestionsUrl:"/Tests/getFilteredQuestions",*/
	getQuestionsUrl:"/Questions/quesItems",
	addQuestionsUrl:"/Tests/addQuestionToList",
	removeQuestionsUrl:"/Tests/removeQuestionFromList",
	data:{sub:{name:'',brdId:''},topic:{name:'',brdId:''},subTopic:{name:'',brdId:''},exam:{name:'',brdId:''}},
	fillUi:function(){
		var testId = "";
		if(testCreation.testId && testCreation.testId.length>0){
			testId = testCreation.testId;
		}else{
			testId = getURLParameter("testId");
			testCreation.testId = testId;
		}
		$(".testCreationLeftBar .testDetails").addClass("nonner");
    		smallLoader(".testCreationLeftBar .testCreationAddQuestions");
		$("#ajaxDataReciever").load(this.loadUrl,{"testId":testId,'addQusInfo':true},function(response,stat){
			$(".testCreationLeftBar .testDetails").removeClass("nonner").html($(this).find('.testDetails').html());
			$(".testCreationLeftBar .testCreationAddQuestions").html($(this).find('.testCreationAddQuestions').html());
			$(".testCreationQuestionList .addQuestionSearchParams").html($(this).find('.addQuestionSearchParams').html());
			vSelectFns.init();
			testCreation.createTestJSON = JSON.parse(tempCreateTestJson);
			$(this).html("");
			if(entryPageIndex!=0){
				$(".testCreationAddQuestions").find(".back").addClass("nonner");
			}
			testCreation.addQuestions.data = {sub:{name:'',brdId:''},topic:{name:'',brdId:''},subTopic:{name:'',brdId:''},exam:{name:'',brdId:''}};
			testCreation.addQuestions.data.sub.brdId = $(".addQuestionSearchParams .selectSubject").data("subjectId");
			testCreation.addQuestions.data.qusType = $(".addQuestionSearchParams .selectQuesType").data("questionType");
			setTimeout(function(){testCreation.addQuestions.getQuestionsList();},1000);
			testCreation.addQuestions.disableAndEnableDoneButton();
			testCreation.createTestJSON.targetId = testCreation.createTestJSON.target.brdId;
		});
	},
	getQuestionsList:function(){
		var params = {"query":"","brdIds":[],start:0,size:-1,"allBrds":true,
                    "testId":testCreation.testId,'target':'TEST_ADD',
                orderBy:"timeCreated"};
		var url = testCreation.addQuestions.getQuestionsUrl;
		if(testCreation.addQuestions.data.subTopic.brdId)
			params.brdIds.push(testCreation.addQuestions.data.subTopic.brdId);
		/*else */if(testCreation.addQuestions.data.topic.brdId)
			params.brdIds.push(testCreation.addQuestions.data.topic.brdId);
		/*else */if(testCreation.addQuestions.data.sub.brdId)
			params.brdIds.push(testCreation.addQuestions.data.sub.brdId);
		if(testCreation.addQuestions.data.exam.brdId)
			params.brdIds.push(testCreation.addQuestions.data.exam.brdId);
		if(testCreation.addQuestions.data.qusType)
			params.type = testCreation.addQuestions.data.qusType;
		var query = $.trim($(".testCreationQuestionList .searchQuestions").val());
		if(query){
			params.query = query;
		}
    		smallLoader(".testCreationQuestionList .addQuestionsHolder");
		$(".testCreationQuestionList .addQuestionsHolder").load(url,params,function(response,stat){
			try{
				var totalHits = $(".totalQuesItemsCount").val();
				if(totalHits){
					testCreation.addQuestions.initResultPagination(totalHits,url,params);
				}
			}catch(err){
			}
			loadMJEqns(this);	
		});
	},
	paginationCb:function(){},
	initResultPagination:function(totalCount,url,params){
		totalCount=totalCount?totalCount:0;
		var params = {leftArrowId:".testAddQuestionPrev",disableClassLeft:"",rightArrowId:".testAddQuestionNext",disableClassRight:"",start:0,size:10,max:totalCount,itemsPerPage:10,scrollDir:'horizontal','fetchUrl':url,otherParams:params,callBack:testCreation.addQuestions.paginationCb};
		$(".testCreationQuestionList").find(".addQuestionsHolder").setForPagination(params);
	},
	addQuestionToTest:function(e){
		var t = testCreation.addQuestions;
		var params = new Object();
		params.testId = testCreation.testId;
		params.qid = $(e.currentTarget).closest(".ques").data("qid");
		params.forTest = true;
		$.get(t.addQuestionsUrl,params,function(response,stat){
			if(!response.result){
				testCreation.showErrorMessage(response.errorMessage,response.errorCode);
				return true;
			}
			testCreation.addQuestions.putQuestions(response.result,e.currentTarget);
		});
	},
	removeQuestionFromList:function(e){
		var t = testCreation.addQuestions;
		var params = new Object();
		params.testId = testCreation.testId;
		params.qid = $(e.currentTarget).data("qid");
		var curriculumDiv = $(e.currentTarget).closest('.eachCurriculum').find(".curriculumName");
		$.get(t.removeQuestionsUrl,params,function(response,stat){
		});
		$(e.currentTarget).closest(".quesAdded").remove();
		testCreation.addQuestions.calcCount(curriculumDiv,-1);
	},
	disableAndEnableDoneButton:function(){
		if(testCreation.addQuestions.validateQuestionCount()){
			$(".testCreationAddQuestions").find(".doneWithAdd").removeClass("disableBtn");
		}else{
			$(".testCreationAddQuestions").find(".doneWithAdd").addClass("disableBtn");
		}
	},
	validateQuestionCount:function(){
		var addedCount = 0;
		$(".testCreationAddQuestions").find(".subjectList").find(".addQuesEachSub").each(function(){
			addedCount += parseInt($(this).find('.subQuesCount').data('quesCount'));
		});
		if(testCreation.createTestJSON.qusCount != addedCount) return false;
		return true;
	},
	calcCount:function(curriculumDiv,addBy){
		var count = parseInt(curriculumDiv.data("quesCount"));
		count += addBy;
		if(!count) curriculumDiv.text(curriculumDiv.data().topicName);
		else curriculumDiv.text(curriculumDiv.data("topicName")+" ("+count+")");
		curriculumDiv.data("quesCount",count);
		var dropBtn = curriculumDiv.closest(".eachQType").find('.dropBtn');
		var t = dropBtn.find('.noOfQuestionEachType');
		count = parseInt(t.data("quesCount"));
		count += addBy;
		t.text(count+"/"+t.data("quesTotal")).data("quesCount",count);
		var eachSubCount = curriculumDiv.closest(".addQuesEachSub").find('.subQuesCount');
		count = parseInt(eachSubCount.data("quesCount"));
		count += addBy;
		eachSubCount.text(count+"/"+eachSubCount.data("quesTotal")).data("quesCount",count);
		setTimeout(function(){testCreation.addQuestions.disableAndEnableDoneButton();},2000);
	},
	removeQuestion:function(qid){
		var t = testCreation.addQuestions;
		var params = new Object();
		params.testId = testCreation.testId;
		params.qid = qid;
		$.get(t.removeQuestionsUrl,params,function(response,stat){
		});
	},
	putQuestions:function(response,elem){
		var qTypeHolder = $(".testCreationAddQuestions").find("#"+response.brdId).find("."+response.type);
		var curriculumDiv = qTypeHolder.find("#"+response.child.brdId);
		if(!curriculumDiv.get(0)){
			qTypeHolder.find(".curriculumList").append("<span class='leftAndRelative eachCurriculum'><span class='leftAndRelative big12 curriculumName' data-topic-name='"+response.child.name+"' data-ques-count=0 id='"+response.child.brdId+"'>"+response.child.name+"</span></span>");
			curriculumDiv = qTypeHolder.find("#"+response.child.brdId);
		}
		var holder = curriculumDiv.closest(".eachCurriculum");
		holder.append("<span class='leftAndRelative quesAdded'><span class='qt'>"+response.quid+"</span> <a class='removeQues' data-qid='"+response.qid+"'>remove</a></span>");
		$(elem).closest(".ques").remove();
		holder.closest(".curriculumList").removeClass("nonner").parent().find(".dropBtn").attr("showing","visible");
		testCreation.addQuestions.calcCount(curriculumDiv,1);
	},
	dropCurriculumList:function(e){
		var display = $(e.currentTarget).attr("showing");
		if(display == "visible"){
			$(e.currentTarget).attr("showing","hidden").parent().find(".curriculumList").addClass("nonner");
		}else{
			$(e.currentTarget).attr("showing","visible").parent().find(".curriculumList").removeClass("nonner");
		}
	},
	saveAndAddLater:function(e){	
		testCreationUi.exit(e);
	},
	registerFns:function(){
		$(".testCreationQuestionList").on("click",".loadPlaylistBtn",this.getQuestionsList);
		$(".testCreationAddQuestions")
			.on("click",".removeQues",this.removeQuestionFromList)
			.on("click",".dropBtn",this.dropCurriculumList)
			.on("click",".saveAndAddLater",testCreationUi.exit);
		$(".addQuestionsHolder").on("click",".questionAddToListBtn",this.addQuestionToTest);
	}
};
function addQuesSubChange(value,target){
	if(value!=testCreation.addQuestions.data.sub.name){
		$(".testCreationQuestionList .selectTopics").find(".vSelectText").val("All Topics");
		var subIndex = parseInt($(target).data("subjectIndex"));
		var list = testCreation.createTestJSON.metadata[subIndex].children;
	    uiCloneHelper.set(".testCreationQuestionList .selectTopics .vSelectList",".vSelectEach",0);var index=0;
	    if(list){
		for(index=0;index<list.length;index++){
			var sp = uiCloneHelper.create(index+1);
			$(sp).data("topicId",list[index].brdId).text(list[index].name);
		}
	    }
	    uiCloneHelper.removeFrom(index+1);
	    $(".testCreationQuestionList .selectTopics").updateVSelectTag();
	    list = testCreation.createTestJSON.metadata[subIndex].details;
	    if(list){
	    	$(".testCreationQuestionList .selectQuesType").find(".vSelectText").val(list[0].type);
		testCreation.addQuestions.data.qusType = list[0].type;
		uiCloneHelper.set(".testCreationQuestionList .selectQuesType .vSelectList",".vSelectEach",0);var index=0;
		for(index=1;index<list.length;index++){
			var sp = uiCloneHelper.create(index);
			$(sp).text(list[index].type);
		}
	     	uiCloneHelper.removeFrom(index);
		$(".testCreationQuestionList .selectQuesType").updateVSelectTag();
	     }
	}
	testCreation.addQuestions.data.sub.name = value;
	testCreation.addQuestions.data.sub.brdId = $(target).data("subjectId");
	testCreation.addQuestions.data.topic.name = "";
	testCreation.addQuestions.data.topic.brdId = "";
	//clickStream.record("ADD_QUES","SUBJECT_SELECT","CHANGE",value);
}
function addQuesTopicChange(value,target){
	testCreation.addQuestions.data.topic.name = value;
	testCreation.addQuestions.data.topic.brdId = $(target).data("topicId");
	//clickStream.record("ADD_QUES","TOPIC_SELECT","CHANGE",value);
}
function addQuesTypeChange(value,target){
	testCreation.addQuestions.data.qusType = value;
	//clickStream.record("ADD_QUES","Q_TYPE_SELECT","CHANGE",value);
}
function addQuesExamChange(value,target){
	testCreation.addQuestions.data.exam.name = value;
	testCreation.addQuestions.data.exam.brdId = $(target).data("targetexamId");
	//clickStream.record("ADD_QUES","TARGET_EXAM_SELECT","CHANGE",value);
}
testCreation.assign = {
	loadUrl:"/Tests/getTestDetailsForAssign",
	getSuggestionsUrl:"/Tests/getFriendsList",
	publishTestUrl:"/Tests/publishMyTest",
	fetchTimeoutObj:0,
	selectedFrndsIds:[],
	selectedFrndsNames:[],
	alphaKeysList:[8,50],
	data:{start:{mins:0,hrs:0,meridiem:0},end:{mins:0,hrs:0,meridiem:0}},
	fillUi:function(){
		var testId = "";
		if(testCreation.testId && testCreation.testId.length>0){
			testId = testCreation.testId;
		}else{
			testId = getURLParameter("testId");
			testCreation.testId = testId;
		}
    		smallLoader(".testCreationLeftBar .testDetails");
		$("#ajaxDataReciever").load(this.loadUrl,{"testId":testCreation.testId},function(response,stat){
			$(".testCreationLeftBar .testDetails").html($(this).find('.testDetails').html());
			$(".testCreationLeftBar .testFormatPreview").html($(this).find('.testFormatPreview').html());
			testCreation.createTestJSON = JSON.parse(tempCreateTestJson);
			$(this).html("");
			$(".testCreationAssign .vselect").updateVSelectTag({"scrollBarOptions":scrollBarOptions});
			testCreation.createTestJSON.targetId = testCreation.createTestJSON.target.brdId;
		});
		for(var i=48;i<=90;i++){
			this.alphaKeysList.push(i);
		}
		testCreation.assign.data = {start:{mins:0,hrs:0,meridiem:0},end:{mins:0,hrs:0,meridiem:0}};
		return true;
	},
	publicOrPrivate:function(e){
		var index = $(".testCreationAssign .publishDesc :radio").index(e.currentTarget);
		if(index){
			$(".testCreationAssign .publishDesc").find(".inputShareWithOnly").removeClass("nonner").find(".getName").focus();
		}else{
			$(".testCreationAssign .publishDesc").find(".inputShareWithOnly").addClass("nonner");
			testCreation.assign.hideSuggestion(null,true);
		}
	},
	scheduleOrNot:function(e){
		var checked = e.currentTarget.checked;
		if(checked){
			$(".testCreationAssign").find(".scheduleDetails").addClass("hider");
		}else{
			$(".testCreationAssign").find(".scheduleDetails").removeClass("hider");
		}
	},
	publishNow:function(e){
		var duration = testCreation.assign.getDuration();
		if(duration<=0){
			var retVal = confirm("Please confirm Duration of test : 'End Time need to be more than start time' \nReply NO/CANCEL to continue without duration");
			if(retVal) return false;
		}
		duration *= 60000;
		$.get(testCreation.assign.publishTestUrl,{"testId":testCreation.testId,'duration':duration},function(response,stat){
			if(response.result){
				testCreation.validateAndShowNextPage();
			}else{
				testCreation.showErrorMessage("Something went wrong , please try after some time",response.errorCode);
			}
		});
	},
	scheduleLater:function(e){
		testCreation.validateAndShowNextPage();
	},
	goForSuggestion:function(e){
		var key = e.which;
		var t = testCreation.assign;
		if(t.alphaKeysList.indexOf(key)>=0){
			try{clearTimeout(t.fetchTimeoutObj);}catch(err){}
			t.fetchTimeoutObj = setTimeout(function(){ testCreation.assign.getSuggestion(e);},500);
		}
	},
	getSuggestion:function(e){
		var queryText = $.trim($(e.currentTarget).val());
		var params = {"query":queryText};
		if(queryText.length>0){
    			smallLoader($(".testCreationAssign").find(".suggestionBox .inner"));
			$(".testCreationAssign").find(".suggestionBox .inner").load(this.getSuggestionsUrl,params,function(response,stat){
				$(this).html(response);
				testCreation.assign.showSuggestionBox(this);
			});
		}
	},
	showSuggestionBox:function(div){
		$(".testCreationAssign").find(".suggestionBox").removeClass("nonner").useScrollBar(scrollBarOptions);
		if($(div).attr("visibility") != "shown"){
			$(document).on("click",testCreation.assign.hideSuggestion);
			$(div).attr("visibility","shown");
			$(".testCreationAssign").find(".inputShareWithOnly .getName").focus();
		}
		var t = $(".testCreationAssign .suggestionBox").find(".eachSuggestion").get(0);
		$(t).addClass("selected");
	},
	hideSuggestion:function(e,forceIt){
		if(testCreation.assign.noHide && !forceIt){
			testCreation.assign.noHide = false;
			return;
		}
		try{clearTimeout(testCreation.assign.fetchTimeoutObj);}catch(err){}
		$(".testCreationAssign").find(".suggestionBox").addClass("nonner").find(".inner").html("").attr("visibility","none");
		$(document).off("click",testCreation.assign.hideSuggestion);
	},
	doNotHideBox:function(e){
		testCreation.assign.noHide = true;
	},
	onClickSuggestionTextBox:function(e){
		$(e.currentTarget).find("input").focus();
		testCreation.assign.doNotHideBox(e);
	},
	onKeyDown:function(e){
                var key = e.which;
                var newItem;
                var moveBy=0;
		var item = $(".testCreationAssign").find(".suggestionBox .inner").find(".selected");
		switch(key){
                        case 38:
                                newItem = $(item).prev().get(0);
                                moveBy = $(newItem).height();
                        break;
                        case 40:
                                newItem = $(item).next().get(0);
                                moveBy = -1*$(newItem).height();
                        break;
                        case 13:testCreation.assign.selectFrnd(item);
	                return;
			case 27:testCreation.assign.hideSuggestion(e,true);
			return;
			case 8:if($(".testCreationAssign").find(".inputShareWithOnly .getName").val().length<=0){
					testCreation.assign.removeLastFrnd();
					e.preventDefault();return false;
				}
			return;
                        default:return;
                };
		if(newItem){
			$(item).removeClass("selected");
			$(newItem).addClass("selected");
			$(".testCreationAssign").find(".suggestionBox").scrollBarExt("moveBy",moveBy);
			e.preventDefault();
			return false;
		}
		$(e.currentTarget).focus();
	},
	addFrnd:function(e){
		testCreation.assign.selectFrnd(e.currentTarget);
	},
	selectFrnd:function(item){
		var userId = $(item).data("userId");
		var name = $(item).find(".name").text();
		testCreation.assign.selectedFrndsIds.push(userId);
		testCreation.assign.selectedFrndsNames.push(name);
		testCreation.assign.hideSuggestion(null,true);
		var newUserDiv = $(".testCreationAssign .selectedNamesHolder").find(".selectedNames").clone();
		newUserDiv.data("userId",userId).find(".innerText").text(name);
		$(".testCreationAssign").find(".inputShareWithOnly .getName").before(newUserDiv).val("").focus();
		$(item).removeClass("selected");
	},
	removeLastFrnd:function(){
		var div = $(".inputShareWithOnly").find(".selectedNames").filter(":last");
		if(div.get(0)){
			testCreation.assign.removeFrnd(div);
		}
	},
	removeFrndWhenCrossed:function(e){
		testCreation.assign.removeFrnd($(e.currentTarget).parent());
	},
	removeFrnd:function(div){
		var userId = div.data("userId");
		var index = testCreation.assign.selectedFrndsIds.indexOf(userId);
		testCreation.assign.selectedFrndsIds.splice(index,1);
		testCreation.assign.selectedFrndsNames.splice(index,1);
		div.remove();
	},
	openDatePicker:function(e){
		$(e.currentTarget).parent().find(".inputDate").trigger('focus');
	},
	suggestionHover:function(e){
		$(".testCreationAssign").find(".suggestionBox .inner").find(".selected").removeClass("selected");
		$(e.currentTarget).addClass("selected");
	},
	getDuration:function(){
		var start = this.data.start.hrs*60+this.data.start.mins+this.data.start.meridiem*12*60;
		var end = this.data.end.hrs*60+this.data.end.mins+this.data.end.meridiem*12*60;
		return (end-start);
	},
	registerFns:function(){
		$(".testCreationAssign")
			.on("change",".publishDesc :radio",this.publicOrPrivate)
			.on("change",".noSchedule",this.scheduleOrNot)
			.on("click",".publishBtn",this.publishNow)
			.on("click",".scheduleLaterBtn",this.scheduleLater)
			.on("keyup",".inputShareWithOnly .getName",this.goForSuggestion)
			.on("click",".inputShareWithOnly",this.onClickSuggestionTextBox)
			.on("click",".suggestionBox .eachSuggestion",this.addFrnd)
			.on("click",".inputShareWithOnly .cross",this.removeFrndWhenCrossed)
			.on("keydown","..inputShareWithOnly .getName",this.onKeyDown)
			.on("click",".suggestionBox",this.doNotHideBox)
			.on("mouseenter mouseleave",".suggestionBox,.eachSuggestion",this.suggestionHover)
			.on("click",".calenderIcon",this.openDatePicker)
			;//.find(".inputDate").datepicker();
	}
};
function identifyStartOrEnd(target){
	return $(target).closest(".schedulePar").data("scheduleType");
}
function assignInputMeridiemChange(value,target){
	var loc = identifyStartOrEnd(target);
	testCreation.assign.data[loc].meridiem = parseInt($(target).data().value);
	//clickStream.record("ASSIGN","MERIDIEM_SELECT","CHANGE",value);
}
function assignInputMinsChange(value,target){
	var loc = identifyStartOrEnd(target);
	testCreation.assign.data[loc].mins = parseInt(value);
	//clickStream.record("ASSIGN","MINS_SELECT","CHANGE",value);
}
function assignInputHrsChange(value,target){
	var loc = identifyStartOrEnd(target);
	testCreation.assign.data[loc].hrs = parseInt(value);
	//clickStream.record("ASSIGN","HRS_SELECT","CHANGE",value);
}
