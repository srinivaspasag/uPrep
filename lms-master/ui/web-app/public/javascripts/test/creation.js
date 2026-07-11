var testCreationUi = {
	hideAll:function(){
		$(".testCreationHeader .stepsToCreateTest").find(".eachStepSelected").removeClass("eachStepSelected");

		/*Hidding left bar items*/
		$(".testCreationLeftBar .testFormat").addClass("nonner");
		$(".testCreationLeftBar .testFormatPreview").addClass("nonner");
		$(".testCreationLeftBar .testCreationAddQuestions").addClass("nonner");
		/* hidding all middle page items*/
		$(".testDetailsHolder").addClass("nonner");
		$(".testCreationFormatHolder").addClass("nonner");
		$(".testCreationQuestionList").addClass("nonner");
		$(".testCreationAssign").addClass("nonner");

		$(".testCreationLeftBar").removeClass("nonner");
	},
	changeNavigationTab:function(index){
		var currentTab = $(".testCreationHeader .stepsToCreateTest").find(".eachStep")[index];
		$(currentTab).addClass("eachStepSelected");
	},
	showDetailsUi:function(){
		testCreation.createTestJSON = {targetId:"",name:"",desc:"",metadata:[]};
		testCreationUi.hideAll();
		$(".testCreationLeftBar").addClass("nonner");
		$(".testDetailsHolder").removeClass("nonner");
		vSelectFns.init();
		return 0;
	},
	showFormatUi:function(){
		testCreationUi.hideAll();
		$(".testCreationLeftBar .testFormat").removeClass("nonner");
		$(".testCreationFormatHolder").removeClass("nonner");
		testCreation.format.fillUi();
		return 1;
	},
	showAddQuestionUi:function(){
		testCreationUi.hideAll();
		testCreation.addQuestions.fillUi();
		$(".testCreationLeftBar .testCreationAddQuestions").removeClass("nonner");
		$(".testCreationQuestionList").removeClass("nonner");
		return 2;
	},
	showAssignUi:function(){
		testCreationUi.hideAll();
		$(".testCreationLeftBar .testFormatPreview").removeClass("nonner");
		$(".testCreationAssign").removeClass("nonner");
		testCreation.assign.fillUi();
		return 3;
	},
	valdDetailUi:function(){
		var testName = $.trim($(".testDetailsHolder").find(".inputTestName").focus().attr("value"));var retValue=true;
		if(testName.length<=0){
			$(".testDetailsHolder").find(".testName .titleText").addClass("markReq");retValue=false;
		}else{
			$(".testDetailsHolder").find(".testName .titleText").removeClass("markReq");
		}
		if(testCreation.createTestJSON.targetId.length<1){
			$(".testDetailsHolder").find(".testTargetExam .titleText").addClass("markReq");retValue=false;
			$(".testDetailsHolder").find(".testTargetExam input").focus();
		}else{
			$(".testDetailsHolder").find(".testTargetExam .titleText").removeClass("markReq");
		}
		if(retValue==true){
			testCreation.createTestJSON.name = testName;
			testCreation.createTestJSON.desc = $.trim($(".testDetailsHolder").find(".inputTestDesc").attr("value"));
			//putConsoleLogs(testCreation.createTestJSON);
		}
		return retValue;
	},
	valdFormatUi:function(e){
		if(!testCreation.format.totalMarks){
			$(".testCreationFormatHolder .enterTotalQuestions").find(".blueTextColor").removeClass("blueTextColor").addClass("markReq");
			return false;
		}
		$(".testCreationFormatHolder .enterTotalQuestions").find(".markReq").addClass("blueTextColor").removeClass("markReq");
		$('.testCreationFormatHolder .choosenSubjects .eachSubject').find(".inputTotalQuestions").removeClass("markReq");
		var mData = testCreation.createTestJSON.metadata;var valid = true;
		for(var mIndex=0;mIndex<mData.length;mIndex++){
			var mTotalQues = mData[mIndex].qusCount;
			var dData = mData[mIndex].details;
			var dTotalCount = 0;
			if(dData){
				for(var dIndex=0;dIndex<dData.length;dIndex++){
					dTotalCount += dData[dIndex].qusCount;
				}
			}
			if(mTotalQues<=0 || dTotalCount<=0 || !dData){
				testCreation.showErrorMessage("Total Questions for "+mData[mIndex].name+" cannot be zero");
				var value = $($('.testCreationFormatHolder .choosenSubjects .eachSubject').get(mIndex)).find(".inputTotalQuestions").addClass('markReq');
				valid = false;
			}
			if(mTotalQues != dTotalCount){
				testCreation.showErrorMessage("Total Question of "+mData[mIndex].name+" does not match with total of its each question types count");
				var value = $($('.testCreationFormatHolder .choosenSubjects .eachSubject').get(mIndex)).find(".inputTotalQuestions").addClass('markReq');
				valid = false;
			}
		}
		if(valid){
			for(var mIndex=0;mIndex<mData.length;mIndex++){
				var dData = mData[mIndex].details;
				if(dData){
					for(var dIndex=0;dIndex<dData.length;dIndex++){
						if(dData[dIndex].qusCount<=0){ dData.splice(dIndex,1); dIndex--;}
					}
				}
			}
			e.currentTarget.disabled='disabled';
			testCreation.format.postDataToServer(testCreation.createTestJSON);
		}
		return false;
	},
	valdAddQuestionUi:function(){
		return testCreation.addQuestions.validateQuestionCount();
	},
	valdAssignUi:function(){
		return true;
	},
	submit:function(){
		tabClose("CREATE_TEST");
		testCreationUi.exit();
	},
	cancel:function(e){
		//tabClose("CREATE_TEST");
		//testCreationUi.exit(e);
	},
	exit:function(e){
		return;
		//goToHomePage();
		history.back();
		if(e){try{e.preventDefault();}catch(err){}}
	},
	init:function(){
		var wWidth = $(window).innerWidth();
		var wleft = (1001 - wWidth)/2;
		$(".testCreationHeader .background").css({left:wleft,width:wWidth});
		//testCreation.currentPageIndex = this.showDetailsUi();
		if(entryPageIndex == 3){
			vSelectFns.init();
		}
		testCreation.currentPageIndex = testCreation.showPageList[entryPageIndex]();
		testCreationUi.changeNavigationTab(testCreation.currentPageIndex);
	}
};
var testCreation = {
	showPageList:[testCreationUi.showDetailsUi,testCreationUi.showFormatUi,testCreationUi.showAddQuestionUi,testCreationUi.showAssignUi,testCreationUi.submit],
	validatePageList:[testCreationUi.valdDetailUi,testCreationUi.valdFormatUi,testCreationUi.valdAddQuestionUi,testCreationUi.valdAssignUi],
	currentPageIndex:0,
	//createTestJSON:{targetId:"",name:"",desc:"",metadata:[{brdId:'',name:'',qusCount:0,children:[],details:[{type:'SCQ',name:'Single Choice Questions',qusCount:0,marks:{positive:0,negative:0}}]}]},
	createTestJSON:{targetId:"",name:"",desc:"",metadata:[]},
	gradeId:"K12",
	testId:'',
	validateAndShowNextPage:function(e){
		var t = testCreation;
		var ret = t.validatePageList[t.currentPageIndex](e);
		if(ret === true){
			t.showNextPage();
		}
	},
	showNextPage:function(){
		var t = testCreation;
		window.scrollTo(0,0);
		t.currentPageIndex = t.showPageList[t.currentPageIndex+1]();
		testCreationUi.changeNavigationTab(t.currentPageIndex);
	},
	backPage:function(e){
		var t = testCreation;
		if(t.currentPageIndex <= 0){
			testCreationUi.exit(e);
			return;
		}
		t.currentPageIndex = t.showPageList[t.currentPageIndex-1]();
		testCreationUi.changeNavigationTab(t.currentPageIndex);
		if(e)e.preventDefault();
	},
	format:{
		totalMarks:0,curSelectedSubject:undefined,
		postUrl:"/Tests/createTest",
		qTypeListIdentifier:new Array(),
		qTypeList:[{type:"SCQ",name:"SCQ (Single Choice Questions)"},{type:"MCQ",name:"MCQ (Multiple Choice Questions)"},{type:"TEXT",name:"Subjective"},{type:"NUMERIC",name:"Numerical"}],
		fillUi:function(){
			$(".testCreationLeftBar").find(".testFormat").html("");
			$(".testCreationLeftBar").find(".testFormat").append($(".testDetailsBody").find(".choosenCurriculumPreview").clone());
			var testDetails = $(".testCreationLeftBar").find(".testDetails");
			var t = testCreation.format;
			var data = testCreation.createTestJSON;
			testDetails.find(".testName").text(data.name);
			testDetails.find(".testDescription").text(data.desc);
			testDetails.find(".testTargetExam .name").text(data.targetName);
			$(".testCreationFormatHolder .chooseQuesFormat").addClass("nonner");
			$(".testCreationFormatHolder .customInputTextBox").val(0);
			$(".testCreationFormatHolder .choosenSubjects").find(".selected").removeClass('selected');
			uiCloneHelper.set(".testCreationFormatHolder .choosenSubjects",".eachSubject",0);
			var subsData = testCreation.createTestJSON.metadata;var indx = 0;
			for(indx=0;indx<subsData.length;indx++){
				var div = uiCloneHelper.create(indx,{"data-subject-index":indx});
				$(div).removeClass("nonner").find(".subName").text(subsData[indx].name);
				subsData[indx].details = 
					[{'type':'SCQ','name':'SCQ',qusCount:0,marks:{positive:0,negative:0}},
					{'type':'MCQ','name':'MCQ',qusCount:0,marks:{positive:0,negative:0}},
					{'type':'NUMERIC','name':'NUMERIC',qusCount:0,marks:{positive:0,negative:0}}];	
			};
			if(indx==0){
				$(uiCloneHelper.findByIndex(0)).addClass("nonner");
			}
			uiCloneHelper.removeFrom(indx);
			for(var a=0;a<t.qTypeList.length;a++){
				t.qTypeListIdentifier[t.qTypeList[a].type]=a;
			}
			$(".testCreationFormatHolder").find(".nextBtn").removeAttr("disabled");
			t.curSelectedSubject = undefined;
		},
		onTotalMarksChange:function(e){
			var t = testCreation.format;
			t.totalMarks = parseNumericTextValue($.trim(($(e.currentTarget).val())));
			var subDivs = $(".testCreationFormatHolder .choosenSubjects").find(".eachSubject");
			var subsData = testCreation.createTestJSON.metadata;var perSubMarks = Math.floor(t.totalMarks/subsData.length);
			var extra = Math.floor(t.totalMarks%subsData.length);var indx=0;var lastOne;
			for(indx=0;indx<subsData.length;indx++){
				lastOne = $(subDivs.get(indx)).find(".inputTotalQuestions").attr("value",perSubMarks);
				subsData[indx].qusCount = perSubMarks;
			}
			if(lastOne)lastOne.attr("value",extra+perSubMarks);
			subsData[subsData.length-1].qusCount = extra+perSubMarks;
			if(t.totalMarks)
				$(".testCreationFormatHolder .enterTotalQuestions").find(".markReq").removeClass("markReq").addClass("blueTextColor");
			if(t.curSelectedSubject == undefined){
				testCreation.format.openSubjectTab($(".testCreationFormatHolder .choosenSubjects .eachSubject").get(0));	
			}
		},
		changeTotalMarks:function(e){
			var t = testCreation.format;t.totalMarks = 0;
			$(".testCreationFormatHolder .choosenSubjects").find(".eachSubject").each(function(){
				var value = parseNumericTextValue($(this).find(".inputTotalQuestions").val());
				testCreation.createTestJSON.metadata[$(this).data().subjectIndex].qusCount = value;
				t.totalMarks += value;
			});
			$(".testCreationFormatHolder").find(".enterTotalQuestions .inputTotalQuestions").attr("value",t.totalMarks);
			if(t.totalMarks>0)
				$(".testCreationFormatHolder .enterTotalQuestions").find(".markReq").removeClass("markReq").addClass("blueTextColor");
			if(t.curSelectedSubject == undefined){
				testCreation.format.openSubjectTab($(".testCreationFormatHolder .choosenSubjects .eachSubject").get(0));	
			}
		},
		subjectSelectedFromInput:function(e){
			var t=testCreation.format;
			if(!t.totalMarks){
				$(".testCreationFormatHolder .enterTotalQuestions").find(".blueTextColor").removeClass("blueTextColor").addClass("markReq");
				return true;
			}
			testCreation.format.openSubjectTab($(e.currentTarget).closest('.eachSubject').get(0));
		},
		subjectSelected:function(e){
			var t=testCreation.format;
			if(!t.totalMarks){
				$(".testCreationFormatHolder .enterTotalQuestions").find(".blueTextColor").removeClass("blueTextColor").addClass("markReq");
				return true;
			}
			testCreation.format.openSubjectTab(e.currentTarget);
		},
		openSubjectTab:function(target){
			if(!target) return;
			var t=testCreation.format;
			$(".testCreationFormatHolder .choosenSubjects").find(".selected").removeClass("selected");
			var subIndex = t.curSelectedSubject = $(target).addClass("selected").data("subjectIndex");
			$(".testCreationFormatHolder .chooseQuesFormat").removeClass("nonner");
			t.fillQuestionTypes(subIndex);
		},
		fillQuestionTypes:function(index){
			var t=testCreation.format;
			var subData = testCreation.createTestJSON.metadata[index];
			var index=0;
			var qTypeTable = $(".testCreationFormatHolder .quesTypesFormat");//.addClass("nonner");
			var tRows = qTypeTable.find(".tableRow");
			var qTypeListNeeded = [0,1,2,3];
			if(subData.details){
			for(index=0;index<subData.details.length;index++){
				qTypeTable.removeClass("nonner");var d = subData.details[index];
				//$(tRows[index]).removeClass("nonner").find(".cellWidthBig").text(d.name);
				$(tRows[index]).find(".inputNoOfQ").val(d.qusCount).attr("data-input-type","quesCount");
				$(tRows[index]).find(".inputMarksPerQ").val(d.marks.positive).attr("data-input-type","marksPositive");
				$(tRows[index]).find(".inputNegMarksPerQ").val(d.marks.negative).attr("data-input-type","marksNegative");
				//var a=t.qTypeListIdentifier[d.type];qTypeListNeeded[a]=null;
			}}
			/*for(;index<tRows.length;index++){
				$(tRows[index]).addClass("nonner");
			}
			uiCloneHelper.set(".testCreationFormatHolder .chooseAndAddTypes .vSelectList",".vSelectEach",0);
			$(".testCreationFormatHolder .chooseAndAddTypes").addClass("nonner").find(".vSelectText").val("choose a question type");
			index=0;
			for(var count=0;count<qTypeListNeeded.length;count++){
				var tIndex = qTypeListNeeded[count]; if(tIndex==null)continue;
				var sp = uiCloneHelper.create(index,{"q-type":t.qTypeList[tIndex].type});
				$(sp).text(t.qTypeList[tIndex].name);index++;
				$(".testCreationFormatHolder .chooseAndAddTypes").removeClass("nonner");
			}
			uiCloneHelper.removeFrom(index);
			$(".testCreationFormatHolder .chooseAndAddTypes").updateVSelectTag();*/
		},
		qTypeValueChanged:function(value,target){
			uiCloneHelper.set(".testCreationFormatHolder .quesTypesFormat table",".tableRow",0);
			var qType = $(target).attr("q-type");
			var subjectIndex = $(".testCreationFormatHolder .choosenSubjects").find(".selected").data().subjectIndex;subjectIndex=subjectIndex?subjectIndex:0;
			try{var index = testCreation.createTestJSON.metadata[subjectIndex].details.length;}catch(err){
				testCreation.createTestJSON.metadata[subjectIndex].details = [];
				index=0;
			}
			testCreation.createTestJSON.metadata[subjectIndex].details[index] = {type:qType,name:value,qusCount:0,marks:{positive:0,negative:0}};
			$(".testCreationFormatHolder .quesTypesFormat").removeClass("nonner");
			var tr = uiCloneHelper.create(index,{"q-type":qType,"q-type-index":index});
			$(tr).removeClass("nonner").find(".cellWidthBig").text(value);
			$(tr).find(".inputNoOfQ").val(0).attr("data-input-type","quesCount");
			$(tr).find(".inputMarksPerQ").val(0).attr("data-input-type","marksPositive");
			$(tr).find(".inputNegMarksPerQ").val(0).attr("data-input-type","marksNegative");
			uiCloneHelper.set(".testCreationFormatHolder .chooseAndAddTypes .vSelectList",".vSelectEach",0);
			if(index>=(testCreation.format.qTypeList.length-1)){
				$(".testCreationFormatHolder .chooseAndAddTypes").addClass("nonner");
			}else{
				uiCloneHelper.removeByObj(target);
				$(".testCreationFormatHolder .chooseAndAddTypes").updateVSelectTag();
				$(".testCreationFormatHolder .chooseAndAddTypes").find(".vSelectText").val("choose another question type");
			}
		},
		updateInputValues:function(e){
			var subjectIndex = $(".testCreationFormatHolder .choosenSubjects").find(".selected").data().subjectIndex;subjectIndex=subjectIndex?subjectIndex:0;
			var qTypeIndex = $(e.currentTarget).parent().parent().attr("q-type-index");
			var inputType = $(e.currentTarget).data().inputType;
			var value = $(e.currentTarget).val();
			var qTypeData = testCreation.createTestJSON.metadata[subjectIndex].details[qTypeIndex];
			if(!qTypeData){
				var name = testCreation.format.qTypeList[qTypeIndex].name;
				var code = testCreation.format.qTypeList[qTypeIndex].type;
				qTypeData = testCreation.createTestJSON.metadata[subjectIndex].details[qTypeIndex] = {'type':code,'name':name,qusCount:0,marks:{positive:0,negative:0}};
			}
			switch(inputType){
				case "quesCount":qTypeData.qusCount = parseNumericTextValue(value);
						break;
				case "marksPositive":qTypeData.marks.positive = parseMarksTextValue(value);
						break;
				case "marksNegative":qTypeData.marks.negative = parseMarksTextValue(value);
						break;
			}
		},
		copyFormatToOthers:function(){
			var subjectIndex = $(".testCreationFormatHolder .choosenSubjects").find(".selected").data().subjectIndex;subjectIndex=subjectIndex?subjectIndex:0;
			var subsData = testCreation.createTestJSON.metadata;
			var qTypeDetails = subsData[subjectIndex].details;
			for(var index=0;index<subsData.length;index++){
				subsData[index].details	= cloneObject(qTypeDetails);
			}
		},
		registerFns:function(){
			$(".testCreationFormatHolder")
				.on("change",".enterTotalQuestions .inputTotalQuestions",this.onTotalMarksChange)
				.on("change",".choosenSubjects .inputTotalQuestions",this.changeTotalMarks)
				.on("click",".choosenSubjects .inputTotalQuestions",this.subjectSelectedFromInput)
				.on("click",".choosenSubjects .inputTotalQuestions",false)
				.on("click",".choosenSubjects .eachSubject",this.subjectSelected)
				.on("click",".copyFormatBtn",this.copyFormatToOthers)
				.on("change",".quesTypesFormat table .customInputTextBox",this.updateInputValues);
		},
		postDataToServer:function(data){
			putConsoleLogs("create test data = "+JSON.stringify(data));
			$.post(testCreation.format.postUrl,data,function(response,stat){
				putConsoleLogs("status = "+stat+"; ; response = "+response);
				testCreation.testId = response?response.result:"";
				testCreation.showNextPage();
			});
		}
	},
	details:{
		getTopicsCount:0,gotTopicsCount:0,
		targetExamChanged:function(value,target){
			var targetExamId=$(target).data().targetexamId;
			testCreation.createTestJSON.targetId = targetExamId;
			testCreation.createTestJSON.targetName = value;
			var testDetailsHolder = $(".testDetailsHolder");
			testDetailsHolder.find(".eachExamTypesCurriculums").addClass("nonner");
			var count = testDetailsHolder.find("#"+targetExamId).removeClass("nonner").find('.curriculumCount').val();
			count = parseInt(count);
			if(count<=0){
				testDetailsHolder.find(".selectAllSubs").addClass("nonner");
				testDetailsHolder.find(".selectText").addClass("nonner");
				testDetailsHolder.find(".noCurriculumMsg").removeClass("nonner");
			}else{
				testDetailsHolder.find(".selectAllSubs").removeClass("nonner");
				testDetailsHolder.find(".selectText").removeClass("nonner");
				testDetailsHolder.find(".noCurriculumMsg").addClass("nonner");
			}
			testDetailsHolder.find(".topicCheckBox").attr("checked",false);
			testDetailsHolder.find(".subCheckBox").attr("checked",false);
			testDetailsHolder.find(".selectAllTopics").attr("checked",false);
			testDetailsHolder.find(".selectAllSubs").attr("checked",false);
			$(".choosenTargetExamPreview").removeClass("nonner").find(".testCreateDetailTargetExamName").text(value);
			testCreation.details.fillSubsSummary();
		},
		selectAllSubs:function(e){
			var checked = e.currentTarget.checked;
			var subList = $(".testDetailsHolder").find(".selectCurriculum #"+testCreation.createTestJSON.targetId+" .eachSub");
			var subListCheckBoxes = subList.find(".subCheckBox").attr("checked",checked);
			subListCheckBoxes.each(function(){
				testCreation.details.subChanged({currentTarget:this},true);
			});
			testCreation.details.fillSubsSummary();
		},
		selectAllTopics:function(e){
			var checked = e.currentTarget.checked;
			if(checked){
				$(e.currentTarget).parent().find(".topicCheckBox").attr("checked",true);
				$(e.currentTarget).parent().parent().find(".subCheckBox").attr("checked",true);
			}else{
				$(e.currentTarget).parent().find(".topicCheckBox").attr("checked",false);
			}
			testCreation.details.fillSubsSummary();
		},
		fillSubsSummary:function(){
			var subIndex = 0;
			var previewHolders = $(".testDetailsHolder .choosenCurriculumPreview").removeClass("nonner").find(".eachSub");
			testCreation.createTestJSON.metadata = [];
			var allSelectedSubs = $(".testDetailsHolder .selectCurriculum #"+testCreation.createTestJSON.targetId).find('.subCheckBox:checked').each(function(){
				var par = $(this).parent();
				var holder = previewHolders.get(subIndex);
				if(!holder){
					holder = $(previewHolders.get(0)).clone();
					previewHolders = $(".testDetailsHolder .choosenCurriculumPreview").append(holder).find(".eachSub");
				}
				var val =$(holder).find(".subName").text(par.find(".subName").text());
				var subTopicsList = $(holder).find(".subTopicsList").html("");
				var children = [];
				par.find(".topicCheckBox:checked").each(function(){
					subTopicsList.html(subTopicsList.html()+$(this).data().topicName+"<br/>");
					children.push({'brdId':$(this).data().topicId,'name':$(this).data().topicName});
				});
				$(holder).removeClass("nonner");
				subIndex++;
				var subjectObj = {brdId:par.data().subjectId,name:par.find(".subName").text(),children:children};
				testCreation.createTestJSON.metadata.push(subjectObj);
			});
			for(var indx=subIndex;indx<previewHolders.length;indx++){
				$(previewHolders.get(indx)).addClass("nonner");
			}
			if(subIndex===0)$(".testDetailsHolder .choosenCurriculumPreview").addClass("nonner");
		},
		postAllSubjectTopicsLoaded:function(){
			this.gotTopicsCount++;
			if(this.gotTopicsCount >= this.getTopicsCount){
				this.gotTopicsCount = this.getTopicsCount = 0;
				testCreation.details.fillSubsSummary();
			}
		},
		getTopics:function(e,exceptedVal,forceSelect){
			var par = $(e.currentTarget).parent();
			var subId = par.data().subjectId;var state = par.find(".selectTopics").attr("state");
			if(state && state == exceptedVal) return;
			if(state=="hiddenAndLoaded"){
				par.find(".selectTopics").removeClass("nonner").attr("state","visibleAndLoaded");
			}else if(state=="visibleAndLoaded"){
				par.find(".selectTopics").addClass("nonner").attr("state","hiddenAndLoaded");
			}else{
				testCreation.details.getTopicsCount++;
    				smallLoader(par.find(".selectTopics"));
				par.find(".selectTopics").load("/Tests/detailsGetTopics",{parentId:subId},function(response){
					$(this).removeClass("nonner");
					$(this).attr("state","visibleAndLoaded");
					$(this).find(":checkbox").attr("checked",forceSelect);
					testCreation.details.postAllSubjectTopicsLoaded();
				});
			}
		},
		subChanged:function(e,doNotFillSummary){
			var expectedVal4GetOptions;
			var checked = e.currentTarget.checked;
			if(checked == false){
				$(".selectCurriculum .selectAllSubs").attr("checked",false);
				expectedVal4GetOptions = "hiddenAndLoaded";
			}else{
				expectedVal4GetOptions = "visibleAndLoaded";
			}
			$(e.currentTarget).closest(".eachSub").find(":checkbox").attr("checked",checked);
			testCreation.details.getTopics(e,expectedVal4GetOptions,checked);
			if(!doNotFillSummary) testCreation.details.fillSubsSummary();
		},
		topicChanged:function(e){
			if(e.currentTarget.checked == false){
				$(e.currentTarget).parent().find(".selectAllTopics").attr("checked",false);
			}else{
				$(e.currentTarget).parent().parent().find(".subCheckBox").attr("checked",true);
			}
			testCreation.details.fillSubsSummary();
		},
		registerFns:function(){
			$(".testDetailsHolder .selectCurriculum")
				.on("change",".selectAllSubs",this.selectAllSubs)
				.on("change",".subCheckBox",this.subChanged)
				.on("change",".topicCheckBox",this.topicChanged)
				.on("change",".selectAllTopics",this.selectAllTopics)
				.on("click",".eachSub .subName",this.getTopics);
		}
	},
	registerBtns:function(){
		this.details.registerFns();
		this.format.registerFns();
		this.addQuestions.registerFns();
		this.assign.registerFns();

		$(".testDetailsHolder").on("click",".next",testCreation.validateAndShowNextPage)
				.on("click",".cancel",testCreationUi.cancel);
		$(".testCreationFormatHolder").on("click",".nextBtn",testCreation.validateAndShowNextPage)
						.on("click",".back",testCreation.backPage);
		$(".testCreationLeftBar").on("click",".testCreationAddQuestions .doneWithAdd",testCreation.validateAndShowNextPage)
				.on("click",".testCreationAddQuestions .back",testCreation.backPage);
		//$(".testCreationAssign").on("click",".publishBtn",testCreation.validateAndShowNextPage);
	},
	showErrorMessage:function(msg,errCode){
		if(errCode){
			msg = TEST_CREATION_ERR_CODE[errCode];
		}
		showError("Error : "+msg);
	},
	init:function(){
		testCreation.testId = incommingTestId;incommingTestId="";
		testCreationUi.init();
		testCreation.registerBtns();
		return true;
	}
};
function detailsOnTargetExamChange(value,target){
	$(".testDetailsHolder").find(".selectCurriculum").removeClass("nonner");
	testCreation.details.targetExamChanged(value,target);
	//clickStream.record("DETAILS","TARGET_EXAM_SELECT","CHANGE",value);
}
function formatOnQuesTypeChange(value,target){
	testCreation.format.qTypeValueChanged(value,target);
	//clickStream.record("FORMAT","QUES_TYPE_SELECT","CHANGE",value);
}

$(".cancelAndCloseTest").live('click',function(){
   goToHomePage();
   pushHistory(null,null,"/");
   tabClose("CREATE_TEST");
});
