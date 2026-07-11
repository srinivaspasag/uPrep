var preTest = new function(){
	var params = {};
        var showGuidelines = function(){
            // check();
            var popup = showVPopup();
            popup.html($("#preTestTakeTestBtnHold").find(".testGuidelinesHolder").parent().html());
            $(popup).find(".testGuidelinesHolder").on('click','#proceedToTakeTest',proceedToTakeTest);
            initGuidelinesHolder(popup);
	   };
       var check = function(){
        var platform = checkPlatform();
        if(!platform){
            return false;
        }
        var password = $(".incommingPasswordForTest").val();
            if(password !== "null"){
                vReq.get("/Tests/checkPasswordForTestPopup" , null, function(data) {
                    var popup = showVPopup(0.7);
                    popup.html(data);
                    popup.on("click",".checkPassword",checkPassword)
                    popup.on("change",".toggle-password",togglePassword)
                });
                var togglePassword = function(){
                    if($("#passwordForTest").attr('type') == 'text'){
                        $('#passwordForTest').attr('type','password');
                    }
                    else{
                        $('#passwordForTest').attr('type','text');
                    }
                    $('#passwordForTest').focus();
                }
                var checkPassword = function(){
                    var $this = $(this);
                    if($this.hasClass("disableBtn")){ return false; }
                    $this.addClass("disableBtn");
                    var popup = $this.closest(".checkPasswordForTestPopup");
                    var passwordValue = popup.find("#passwordForTest").val().trim();
                    if (passwordValue != password){
                        popup.find(".signupErrorDiv").removeClass("nonner");
                        $this.removeClass("disableBtn");
                    return;
                    }else{
                        closeVPopup();
                        var counter = 0;
                        var showGuideLines = setInterval(function(){
                            counter++;
                            if(counter>=1){
                                clearInterval(showGuideLines);
                            }
                            showGuidelines();
                        }, 500);
                    }
                };
            }else{
                showGuidelines();
            }
       }

        var proceedToTakeTest = this.takeThisTestFromPreTest = function(){
            $("#userPushNotifyPopup").addClass("nonner");
            $(".popover-demo").addClass("nonner");
        	closeVPopup();
            var urlParams=fetchUrlParams();
            var subjectiveTest = $(".subjectiveTest").val() === "true" ? true:false;
            if(subjectiveTest){
                $("#topBarHolder").addClass("nonner");
            }
            if(!urlParams["targetId"] || !urlParams["targetType"] ||
                urlParams["targetId"]==undefined || urlParams["targetType"]==undefined ||
                urlParams["targetId"]=="undefined" || urlParams["targetType"]=="undefined"
                )
            {
                showError("You have accessed a direct url.");
                return ;
            }
            params["target.id"] = urlParams["targetId"];
            params["target.type"] = urlParams["targetType"];
            if (urlParams["sectionId"] !== undefined) {
                params["sectionId"] = urlParams["sectionId"];
            }
            else if(urlParams["targetType"] == "MODULE" && urlParams["sectionId"] === undefined){
                showError("You have accessed a direct url.");
                return ;
            }
		history.replaceState(null,null,"/");
        handleTestBackTrigger();
		if(subjectiveTest || prepareUiForTest()){
			params['startTime']=(new Date()).getTime()+window['serverTimeDelta'];                        
                        if(urlParams["context.id"]){
                            params["context.id"]=urlParams["context.id"];
                            params["context.type"]=urlParams["context.type"];
                            params["entity.id"]=params.testId;
                            params["pdfId"]=params.pdfId;
                            params["orgId"]=$("#myInstitutePage").data("orgId");
                            params["entity.type"]="TEST";                            
                        }
			openMyPage("/Tests/attempt",params,null,null,true);
		}else{
			showError(i18nJS("TEST_BROWSER_SPECIFIC_SUPPORT_DEPENDENCY"));	
		}
	};
	var showLeadersInPreTest = function(e){
		var testId = $(".incommingTestId").val();
		if(!testId) testId = getURLParameter("testId"); 
		showLeaderBoardPopup(testId);
		if(e){ e.preventDefault();}
	};
	var showTestGuidelinesPopup = function(){
	};
	this.init = function(){
		var msg = vedantuClient.verifyClientForTest();
        	if(msg) {
                	$("#preTestTakeTestBtnHold").html("<div class='redTextColor big14' style='padding:20px;background-color:#fff;border-radius:5px;margin-top:20px;'>"+msg+"</div>");
                return;
        	}
		var testId = $(".incommingTestId").val();
		var testName = $(".incommingTestName").val();
		var pdfId = $(".incommingPdfId").val();
		if(!testId) testId = getURLParameter("testId"); 
		params = {'testId':testId,'pdfId':pdfId,'testName':testName,tabType:"TEST_ATTEMPT"};
		
		window['serverTimeDelta'] = $(".serverPreTestLoadTime").val()-new Date().getTime();
		if(!window['serverTimeDelta'] || window['serverTimeDelta']<60000){
			window['serverTimeDelta'] = 0;
		}
		
		$("#preTest .icon").error(function(){
			$(this).addClass("nonner");
		});
		$("#preTest").on('click','.takeTestBtn',check)
			.on('click','.viewTestGuidelines',showTestGuidelinesPopup)
			.on('click','.showTestDetails',showTestDetailsPopup)
			.on('click','.viewAll',showLeadersInPreTest);
		vSelectFns.init();
		$(".testTitle").elipsifyText();
	}
}
function topicWeightageSubjectChanged(value,targetElem,targetValue){
	$(".testTopicsWeightageTable").addClass("nonner");
	$(".testTopicsWeightageHolder").find(".cls-topicWeightage-"+targetValue).removeClass("nonner");
	//clickStream.record("TEST_DETAILS_CENTER_BODY","TOPIC_WEIGHTAGE_SELECT","CHANGE",value);
}
