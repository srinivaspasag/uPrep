function prepareUiForTest(){
	var ret = $.goFullScreen(fullScreenHandler);
	if(ret){
		$(document).bind("contextmenu",disableRightClick);
		$(document).on("keydown",disableRightClick);
		//$(window).bind("resize",windowResizeOnTest);
		testFns.onFullScreen();
	}
	return ret;
}
function disableRightClick(e){
	if(e)e.preventDefault();
	return false;
}
function fullScreenHandler(e,stat){
	if(stat==false){
		closeVPopup();
		showVYesNoBox("Are you sure to submit ?", null, function(state) {
			if(!state){
				$.goFullScreen(fullScreenHandler);
			}
			else {
				if(!takeTest.ifInternetAvailable()) return;
				endRunningTest();
			}
		});
	}
}
function windowOutFocused(){
	if($("#test_home").get(0)){
        	endRunningTest();
        }else{
		$(window).unbind("blur",windowOutFocused);
	}
}
function onPopState(e){
    if(!window.clearTestPopstate){
        try{
        takeTest.stopTimers();
        postTestUi();
        window.clearTestPopstate = true;
        }catch(err){
            console.log(err);
        }
    }
}
var checkPlatform = function(){
    try{
        if(window.screen.width >= 800 && window.screen.height >=600){
            return true;
        }
        else{
            showError("Your screen resolution is not optimal to take this test."+"<br><br>Min Screen Resolution : 800 x 600"+"<br><br>Please reattempt this test from a web browser on a laptop/desktop or from our Android app.");
            return false;
        }
    }catch(err){
        console.log(err);
    }
    return false;
}
function handleTestBackTrigger(){
    try{
        setTimeout(function(){window.onpopstate = onPopState;},1000);
        window.clearTestPopstate = false;
    }catch(err){
        console.log(err);
    }
}
function endRunningTest(){
	// showTimedError("Oops! You either pressed 'Esc' or 'Alt+Tab'. Test has ended and your progress is submitted.");
	try{
		if(takeTest){
			endCurrentExam(takeTest.testId,takeTest.targetType,takeTest.targetId);
		}
	}catch(err){console.log(err)}
}
function endCurrentExam(testId,targetType,targetId){
	try{takeTest.stopTimers();}catch(err){console.log("dsgvdf");console.log(err)}
	postTestUi();
	testFns.onTestEnd(testId,targetType,targetId);
}
function postTestUi(){
	//$(window).unbind("resize",windowResizeOnTest);
	$.exitFullScreen();
        //$(window).unbind("blur.takeTest");
	$(window).unbind("blur",windowOutFocused);
	$(document).unbind("contextmenu",disableRightClick);
	$(document).off("keydown",disableRightClick);
	testFns.onFullScreenExit();
}
function showTestDetailsPopup(e){
	putWrapperForPopup("#noTabSection",".testDetailsPopupHolder");
	var _id = "_testDetailsPop-"+$(e.currentTarget).data("subjectId");
	$(".testDetailsPopupHolder").find(".chooseQuesFormat").addClass("nonner");
	$(".testDetailsPopupHolder").removeClass("nonner").find("#"+_id).removeClass("nonner");
	$(".testDetailsPopupHolder").css("left",(($(window).innerWidth()-$(".testDetailsPopupHolder").width())/2)+"px");
	if(e) e.preventDefault();
}
$(document).on("click",".closeTestDetailsPopup",function(e){
	$(e.currentTarget).closest(".testDetailsPopupHolder").addClass("nonner");
	closeWrapperForPopup();	
});
//Mathjax
var adjustTestMathJaxEqnsOptions=function(targetEl){
    $(targetEl).find(".quesOptionsExceeded").removeClass("quesOptionsExceeded");
    $(targetEl).find(".choices").each(function(){
        var makeBlock=false;
        //$(this).find(".eachOpt").find(".MathJax_Display").each(function(){
        $(this).find(".eachOpt").each(function(){
            //$(this).closest(".eachOpt").addClass("mathJaxPa");
            if($(this).outerWidth(true)>$(this).closest(".eachOptHolder").outerWidth(true)){
                makeBlock=true;
            }
            else if($(this).find('.MathJax_Display').outerWidth(true)>$(this).closest(".eachOptHolder").outerWidth(true)){
                makeBlock=true;
            }
        });
        if(makeBlock){
            $(this).children(".eachOptHolder").addClass("quesOptionsExceeded");
	}
    });
    $(targetEl).find(".rteTextDiv").addClass("rteTextDivStyled");
    $(targetEl).find(".MathJax_Display").find("span").addClass("MathJax_DisplaySpan");
}
function loadTestMJEqns(el,cbfn){
    try{
       if(el)MathJax.Hub.Queue(["Typeset",MathJax.Hub,el]);
       else MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
       MathJax.Hub.Queue(function(){
           adjustTestMathJaxEqnsOptions(el);
           if(cbfn)cbfn();
       });
    }catch(err){
	console.error("Mathjax not Working :: "+err);
    }
}
var testDownTime = new function($){
	var downTime = 0;
	var startTime = 0;
	this.set = function(data){
		try{
			if(!data || !data.result || !data.result.downTime) return false;
			if(!data.result.startTime) return false;
			downTime = data.result.downTime?parseInt(data.result.downTime,10):0;
			startTime = parseInt(data.result.startTime);
			return true;
		}catch(err){
			downTime = 0;
			startTime = 0;
		}
		return false;
	},
        //made for test-app exclusively
        this.setStartTime=function(startTimeGiven){
		try{
                if(!startTimeGiven) return false;
			startTime = parseInt(startTimeGiven);
			return true;
		}catch(err){
			startTime = 0;
		}            
                return false;
        }
	this.updateTimer = function(duration){
		if(!startTime) return duration/1000;                
		var curTime = new Date().getTime() + window['serverTimeDelta'];
		var timeLeft = (startTime+downTime+duration) - curTime;
		timeLeft = timeLeft>duration?duration:timeLeft;
		var angle = (1 - timeLeft/duration) * 360;
		setTimeout(function(){timer_circle.fill(angle);},10);
		timeLeft = timeLeft/1000;
		if(timeLeft<0){
			showTimedError("Out of Time! Test has ended and your progress is submitted.");
		}
		return timeLeft;
	}
};
