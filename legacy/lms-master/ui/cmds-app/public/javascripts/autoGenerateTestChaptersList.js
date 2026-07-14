var autoGenerateTestChaptersList = new function() {
	this.init = function(){
		showLoader();
		prepareChildrenData();
		// var pageTable3 = $(".pageTable3")
		openFirstAccordion("pageTable3");
		setTimeout(function(){
			hideLoader();
		},1000);
		$(".CTFTitleTaker").on("click",".collapsable",accordionOpenClose);
		$(".CTFTitleTaker").find(".goToCTFTestFormatQuesType").on("click",goToCTFTestFormatQuesType);
		$(".CTFTitleTaker").on("click",".skipTopicWiseFilter",skipTopicWiseFilter)
		$(".CTFPage4").find(".goToCTFGenerateTest").on("click",generateTestSubmit);
		$(".CTFPage4").on("click",".skipDifficultyWiseFilter",skipDifficultyWiseFilter)
	}

	var showLoader = function(){
		document.getElementById("overlay").style.display = "block";
	}

	var hideLoader = function(){
		document.getElementById("overlay").style.display = "none";
		$(".CTFTitleTaker").removeClass("nonner");
	}

	var skipTopicWiseFilter = function(){
		message = "Are you sure you want to skip filters and generate test?";
		showVYesNoBox(message, null, function(state) {
			if (state) {
				//console.log(metadata_copy);
				for(i=0;i<metadata_copy.length;i++){
					// if(metadata_copy[i].children == null){
						delete(metadata_copy[i].children);
					// }
				}
				//console.log(metadata_copy);
			    autoGenerateTest(metadata_copy);
			}
		});
	}

	var skipDifficultyWiseFilter = function(){
		message = "Are you sure you want to skip filters and generate test?";
		showVYesNoBox(message, null, function(state) {
	        if (state) {
	            autoGenerateTest(metadata);
	        }
	    });
	}
	var autoGenerateTest = function(metadata){
		//console.log(metadata);
		var testId = $("#testPage").data("entityId");
		var params = {
			testId:testId,
			metadata:metadata
		}
		var successFn = function(htmlContent) {
			hideTestLoader();
			resourceUrlFn = vcmdsUrls.TEST;
            hideTopLoader();
            $(this).removeClass("btnDisabled");
            cSecHolder.html(htmlContent);
            cSecHolder.find("#testPage").addClass("autoGenerate");
            closeStopCombo();
            var testId = cSecHolder.children("#testPage").data("entityId");
            //console.log(testId);
            //trackEventForGA("CMDS" + ctfParams.type, "ADD_CONTENT", ctfParams.name);
            history.replaceState(null, null, resourceUrlFn(testId));
            trackPageView();
        };
        var errorFn = function() {
			hideTestLoader();
            $(this).removeClass("btnDisabled");
        };
        showTestLoader();
		vReq.post("/qrTests/createTestAuto",params,successFn,errorFn);
	}

	var showTestLoader = function(){
		$("#autoGenerateTestPage").addClass("nonner");
		$("#testLoaderOverlay").removeClass("nonner");
	}

	var hideTestLoader = function(){
		$("#testLoaderOverlay").addClass("nonner");
		$("#autoGenerateTestPage").removeClass("nonner");
	}

	var prepareChildrenData = function(){
		var topicSize = $(".getTopics").size();
		var courseIds = [];
		var datas= [];
		var courseTopics={};
		if(topicSize > 0){
			$(".getTopics").each(function(){
				courseIds.push($(this).data('courseId'));
			});

			populateChildrenInDom(courseIds)
		}
	}

	var populateChildrenInDom = function(courseIds){
		for(j=0;j<courseIds.length;j++){
			$.get("/uicomboards/getOrgBoards", {parentId: courseIds[j],type: "TOPIC"}, function(data){
				var topic = data.result.list;
				for(i=0;i<topic.length;i++){
					$(".chapter_"+topic[i].parentIds[0]).before(
						'<tr class="collapse topicList topic_'+topic[i].id+' '+topic[i].parentIds[0]+'" data-topic-id='+topic[i].id+'>'+
						'<td class="topicName">'+
						topic[i].name+
						'</td>'+
						'<td class="CTFScq"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'<td class="CTFMcq"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'<td class="CTFNumeric"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'<td class="CTFPara"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'<td class="CTFMatrix"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'<td class="CTFSubjective"><input type="text" class="CTFNum numberTextBox" maxlength="3" size="3" value="0" /></td>'+
						'</tr>');
				}
			}).success(function(data){
				addChildrenToMetaData(data);
			});
		}
	}

	var addChildrenToMetaData = function(data){
		var topic = data.result.list;
		$(".chapter_"+topic[0].parentIds[0]).remove();
		for(i=0;i<metadata.length;i++){
			if(metadata[i].id == topic[0].parentIds[0]){
				if(metadata[i].children == null){
					metadata[i].children = [];
				}
				for(j=0;j<topic.length;j++){
					metadata[i].children.push(topic[j]);
				}
			}
		}
	}

	var goToCTFTestFormatQuesType = function(){
		var chaptersListTable = $(".CTFTable");
		var boards = [];
		
		chaptersListTable.find("tbody").each(function(){
			var board = {
				id:"",
				childrens:[],
				published:""
			}
			var childrens = [];
			board.id = $(this).data("subjectId");
			$(this).find("tr").each(function(){
				if($(this).hasClass("questionStatus")){
					board.published = $(this).find("input[type='radio']:checked").val();
				}
				else if($(this).hasClass("topicList")){
					var children = {
						name : "",
						id: "",
						qusCount: 0,
						totalMarks:"",
						details:[]
					};
					$(this).find("td").each(function(){
						var detail = {
							type:"",
							qusCount:0
						}
						if($(this).hasClass("topicName"))
						{
							children.name = $(this)[0].innerHTML;
						}
						else if($(this).hasClass("CTFScq")){
							detail.type = "SCQ";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
						else if($(this).hasClass("CTFMcq")){
							detail.type = "MCQ";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
						else if($(this).hasClass("CTFNumeric")){
							detail.type = "NUMERIC";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
						else if($(this).hasClass("CTFPara")){
							detail.type = "PARA";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
						else if($(this).hasClass("CTFMatrix")){
							detail.type = "MATRIX";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
						else if($(this).hasClass("CTFSubjective")){
							detail.type = "SUBJECTIVE";
							detail.qusCount = parseInt($(this).find("input").val());
							children.details.push(detail);	
						}
					});
					children.id = $(this).data("topicId");
					childrens.push(children);
					board.childrens = childrens;
				}
			});
			boards.push(board);
		});
		if(validateUserInputMetadata(boards)){
			addUserInputMetadata(boards);
			goToTestPageFour(metadata);
		}
		else{
			//console.log("not validated");
		}
	}

	var validateUserInputMetadata = function(boards){
		var validate = true;
		for(i=0;i<metadata.length;i++){
			var actualBoard = metadata[i];
			var totalQuestionsBoard = metadata[i].qusCount;
			var totalScqQuestionCount = metadata[i].details[0].qusCount;
			var totalMcqQuestionCount = metadata[i].details[1].qusCount;
			var totalNumericQuestionCount = metadata[i].details[2].qusCount;
			var totalParaQuestionCount = metadata[i].details[3].qusCount;
			var totalMatrixQuestionCount = metadata[i].details[4].qusCount;
			var totalSubjectiveQuestionCount = metadata[i].details[5].qusCount;
			for(j=0;j<boards.length;j++){
				if(actualBoard.id == boards[j].id){
					var currentScqCount = 0;
					var currentMcqCount = 0;
					var currentNumericCount = 0;
					var currentParaCount = 0;
					var currentMatrixCount = 0;
					var currentSubjectiveCount = 0;
					for (var q = 0; q < boards[j].childrens.length; q++){
						var currentChildren = boards[j].childrens[q];
						currentScqCount+=currentChildren.details[0].qusCount;
						currentMcqCount+=currentChildren.details[1].qusCount;
						currentNumericCount+=currentChildren.details[2].qusCount;
						currentParaCount+=currentChildren.details[3].qusCount;
						currentMatrixCount+=currentChildren.details[4].qusCount;
						currentSubjectiveCount+=currentChildren.details[5].qusCount;
					}
					if(totalScqQuestionCount < currentScqCount || totalMcqQuestionCount < currentMcqCount || totalNumericQuestionCount < currentNumericCount || totalParaQuestionCount < currentParaCount || totalMatrixQuestionCount < currentMatrixCount || totalSubjectiveQuestionCount < currentSubjectiveCount){
						validate = false;
						showError("You have selected More questions in "+actualBoard.name+" please check");
						return validate;
					}
					else if(totalScqQuestionCount > currentScqCount || totalMcqQuestionCount > currentMcqCount || totalNumericQuestionCount > currentNumericCount || totalParaQuestionCount > currentParaCount || totalMatrixQuestionCount > currentMatrixCount || totalSubjectiveQuestionCount > currentSubjectiveCount){
						validate = false;
						showError("You have selected Less questions in "+actualBoard.name+" please check");
						return validate;
					}
				}
			}
		}
		return validate;
	}

	var addUserInputMetadata = function(boards){
		for(i=0;i<metadata.length;i++){
			var actualBoard = metadata[i];
			for(j=0;j<boards.length;j++){
				if(actualBoard.id == boards[j].id){
					metadata[i].published = boards[j].published;
					metadata[i].children = [];
					for (var q = 0; q < boards[j].childrens.length; q++){
						var currentChildren = boards[j].childrens[q];
						if(currentChildren.details[0].qusCount == 0 && currentChildren.details[1].qusCount == 0 & currentChildren.details[2].qusCount== 0 && currentChildren.details[3].qusCount==0 && currentChildren.details[4].qusCount==0 && currentChildren.details[5].qusCount==0)
						{
							continue;
						}
						//console.log(currentChildren);
						var totalQuesChapter = currentChildren.details[0].qusCount+currentChildren.details[1].qusCount+currentChildren.details[2].qusCount+currentChildren.details[3].qusCount+currentChildren.details[4].qusCount+currentChildren.details[5].qusCount;
						currentChildren.qusCount = totalQuesChapter;
						metadata[i].children.push(currentChildren);
					}

				}
			}
		}
		//console.log(metadata);
	}

	var goToTestPageFour = function(){
		$(".CTFPage4").on("click",".collapsable",accordionOpenClose);
		$(".CTFTitleTaker").addClass("nonner");
		$(".CTFPage4").removeClass("nonner");

		for(i=0;i<metadata.length;i++){
			var currentBoard = metadata[i];
            // console.log(currentBoard);
            generateTbody(currentBoard)

            // break;
        }
		$("tr").on("click",".selector",function(e){
			var count = $(this).data('qusCount');
			if(count ==0){
				$(this).prop("disabled",true);
				return ;
			}
			//console.log($(this).data('qusCount'));
			$(e.delegateTarget).find(".selected").removeClass("selected");
			$(e.currentTarget).addClass("selected");
			$(this).parent().parent().find(".SCQselector").addClass("nonner");
			$(this).parent().parent().find(".MCQselector").addClass("nonner");
			$(this).parent().parent().find(".Numericselector").addClass("nonner");
			$(this).parent().parent().find(".PARAselector").addClass("nonner");
			$(this).parent().parent().find(".Matrixselector").addClass("nonner");
			$(this).parent().parent().find(".Subjectiveselector").addClass("nonner");
			$(this).parent().parent().find("."+$(this).data("selector")+"selector").removeClass("nonner");
		});

		$("tr .selector").hover(function(){
			var count = $(this).data('qusCount');
			if(count ==0){
				$(this).css("cursor","not-allowed");
				return ;
			}
		});
		openFirstAccordion("pageTable4");
		//$(".goToCTFGenerateTest").on("click",validateUserInputPage4Metadata);
    }

    var accordionOpenClose = function(){
		console.log("Inside accordionOpenClose");
		if($(this).find(".openClose").text() == "  +  ")
			$(this).find(".openClose").text("  -  ");
		else
			$(this).find(".openClose").text("  +  ");

		$(this).nextUntil(".collapsable").toggleClass("collapse");
	}

	var openFirstAccordion = function(tableName){
	    setTimeout(function(){
	     $("."+tableName+" tr.collapsable:first .openClose").text("  -  ");
	     $("."+tableName+" tr.collapsable:first").nextUntil(".collapsable").toggleClass("collapse");
	    },1000);
	}

    var generateTbody = function(currentBoard){
	var className = ["SCQselector","MCQselector","Numericselector","PARAselector","Matrixselector","Subjectiveselector"];
		$(".pageTable4").append("<tbody class='"+currentBoard.id+"' data-subject-id='"+currentBoard.id+"'><tr class='collapsable'" +
		" data-subject-id="+currentBoard.id+"><td class='tableHead' colspan=7><span class='openClose'>  +  </span>"+currentBoard.name + "-"+currentBoard.qusCount+"</tr>"+
		"<tr class='tableHead1 collapse'><td class='tableHead3'>TopicName</td><td class='selected selector' data-selector='SCQ' data-qus-count='"+currentBoard.details[0].qusCount+"'>SCQ- "+currentBoard.details[0].qusCount+"</td><td class='selector' data-selector='MCQ' data-qus-count='"+currentBoard.details[1].qusCount+"'>MCQ- "+currentBoard.details[1].qusCount+"</td><td class='selector' data-selector='Numeric' data-qus-count='"+currentBoard.details[2].qusCount+"'>NUMERIC- "+currentBoard.details[2].qusCount+"</td><td class='selector' data-selector='PARA' data-qus-count='"+currentBoard.details[3].qusCount+"'>PARA- "+currentBoard.details[3].qusCount+"</td><td class='selector' data-selector='Matrix' data-qus-count='"+currentBoard.details[4].qusCount+"'>MATRIX- "+currentBoard.details[4].qusCount+"</td><td class='selector' data-selector='Subjective' data-qus-count='"+currentBoard.details[5].qusCount+"'>SUBJECTIVE- "+currentBoard.details[5].qusCount+"</td></tr>"+
		"<tr class='tableHead2 collapse'><td class='tableHead3'>Difficulty Level</td><td colspan='2'>Total</td><td>Easy</td><td>Moderate</td><td>Tough</td></tr></tbody>");
		var children = currentBoard.children;
		for(j=0;j<children.length;j++){
		var first=0;
			var show = "nonner";
			for(k=0;k<children[j].details.length;k++){
				if(first == 0){
					show = "visible";
					first++;
				}
				else{
					show = "nonner";
				}
				if(children[j].details[k].qusCount == 0){
					continue;
				}
				$("."+currentBoard.id).append("<tr  data-topic-id='"+children[j].id+"' class='collapse "+show+ " topicList "+className[k]+"'>"+
					"<td class='topicName'>"+children[j].name+"</td><td colspan='2'><input type='text' class='CTFNum totalQusCount numberTextBox' maxlength='3' size='3' disabled value='"+children[j].details[k].qusCount+"'</td>"+
					"<td><input type='text' class='CTFNum easy numberTextBox' maxlength='3' size='3' value='0'</td>"+
					"<td><input type='text' class='CTFNum moderate numberTextBox' maxlength='3' size='3' value='0'</td>"+
					"<td><input type='text' class='CTFNum tough numberTextBox' maxlength='3' size='3' value='0'</td>"+
					"</tr>");
			}
		}
		$(".pageTable4").append("</tbody>");
		// if(validateUserInputPage4Metadata(boards)){
		// 	addUserInputPage4Metadata(boards);
		// 	//goToTestPageFour(metadata);
		// }
		// else{
		// 	console.log("not validated");
		// }
	}

	var generateTestSubmit = function(){
		// console.log("Inside generateTestSubmit");
		if(validateUserInputPage4Metadata()){
			var easyCount = 0;
		var moderateCount = 0;
		var toughCount = 0;
		$(".pageTable4").find("tbody").each(function(){
			var subject = $(this);
			var subjectId = $(this).data('subjectId');
			for(i=0;i<metadata.length;i++){
				var actualBoard = metadata[i];
				if(actualBoard.id == subjectId){
					subject.find("tr").each(function(){
						var chapter = $(this);
						for(j=0;j<actualBoard.children.length;j++){
							if($(this).data('topicId') == actualBoard.children[j].id){
									easyCount =parseInt(chapter.find("td .easy").val());
									moderateCount = parseInt(chapter.find("td .moderate").val());
									toughCount = parseInt(chapter.find("td .tough").val());
								if(chapter.hasClass("SCQselector")){
									metadata[i].children[j].details[0].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								else if(chapter.hasClass("MCQselector")){
									metadata[i].children[j].details[1].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								else if(chapter.hasClass("Numericselector")){
									metadata[i].children[j].details[2].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								else if(chapter.hasClass("PARAselector")){
									metadata[i].children[j].details[3].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								else if(chapter.hasClass("Matrixselector")){
									metadata[i].children[j].details[4].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								else if(chapter.hasClass("Subjectiveselector")){
									metadata[i].children[j].details[5].difficulty = {
										easy:easyCount,
										moderate:moderateCount,
										tough:toughCount
									};
								}
								// console.log(details);
							}
						}
					})
				}
			}
		});
		//console.log(metadata);
		autoGenerateTest(metadata);
		}
	}

	var validateUserInputPage4Metadata = function(){
		var validate = true;
		$(".pageTable4").find("tbody").each(function(){
			var subject = $(this);
			subject.find("tr").each(function(){
				if($(this).hasClass("topicList")){
					var totalQusCount = parseInt($(this).find(".totalQusCount").val());
					var easyCount =parseInt($(this).find("td .easy").val());
					var moderateCount = parseInt($(this).find("td .moderate").val());
					var toughCount = parseInt($(this).find("td .tough").val());
					if(totalQusCount != easyCount + moderateCount + toughCount ){
						//console.log($(this).find(".topicName")[0].innerHTML);
						validate = false;
						showError("Difference in question count in topic <br> "+$(this).find(".topicName")[0].innerHTML);
						return validate;
					}
				}
			});
			return validate;
		});
		return validate;
	}
};