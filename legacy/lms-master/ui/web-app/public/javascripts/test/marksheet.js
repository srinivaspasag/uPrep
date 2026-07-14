
//for full marks sheet of test
var marksSheetPage=new(function($){
    var fullMarksSheetPage;
    this.init=function(params,holder){
        fullMarksSheetPage=holder.find("#fullMarksSheetPage");
        fullMarksSheetPage
        .on("click",".showNextSubs",showNextSubs)
        .on("click",".showPrevSubs",showPrevSubs)
        .on("click",".showNextPapers",showNextPapers)
        .on("click",".showPrevPapers",showPrevPapers)
        .on("click",".showMeasuresOfTest",showMeasuresOfTest)
        .on("click",".refreshResultSheet",refreshResultSheet)
        assignShadowsForSubs();
        assignShadowsForPapers();
	startRefreshTimer();
    };
    var refreshTime = 10000; 
    var startRefreshTimer = function(){
	//timerObj.set("refresh",refreshResultSheet,refreshTime);
	var lastVal = fullMarksSheetPage.parent().data("lastVChooseValue");
	if(lastVal>=0){
    		var vChoose=fullMarksSheetPage.find(".paperChangevChoose");
    		this.onPaperChange(vChoose,lastVal);
	}
    };
    var refreshResultSheet = function(){
	putConsoleLogs("Going to refresh test leader board!");
	var holder = $(".postTestResultSheet");
	var testId = $(".testResultSheetTab").data("testId");
	var loadMoreDiv = holder.find(".marksSheetTable").find(".loadMoreItems");
	var size;
	if(loadMoreDiv.get(0)){
		size = parseInt(loadMoreDiv.data("start"));
	}
    	var resultVisibility = $(".postTestMainTabs").data("resultVisibility") || "VISIBLE";
    	var resultVisibilityMessage = $(".postTestMainTabs").data("resultVisibilityMessage");
	fetchTestMarkSheet(holder,testId,size,resultVisibility,resultVisibilityMessage);
    };
    var assignShadowsForSubs=function(){
        var tds=fullMarksSheetPage.find(".subject4");
        for(var k=0;k<tds.length;k++){
            var td=tds.eq(k).prev();
            td.addClass("nextShowTd");
            if(td.hasClass("testSubjectName")){
                addShowNextClass(td,"showNextSubs");
            }
        }            
    };
    var assignShadowsForPapers=function(){
        if(fullMarksSheetPage.find(".paper3").length>0){
            var els=fullMarksSheetPage.find(".paper2.totalMarks,.paper2.testNameTd");
            var target=els.addClass("nextShowPapersTd").first();
            addShowNextClass(target,"showNextPapers");
        }        
    };   
        
    var showNextSubs=function(){
        showPrevNext("NEXT",$(this));
    }; 
    var showPrevSubs=function(){
        showPrevNext("PREV",$(this));
    }; 
    var showNextPapers=function(){
        showPrevNext("NEXT",$(this),true);
    }; 
    var showPrevPapers=function(){
        showPrevNext("PREV",$(this),true);
    };     
    var showPrevNext=function(showType,$this,forPapers){
        //1 based index        
        var td=$this.closest("td");
        var indexes;
        var prevClass="showPrevSubs",nextClass="showNextSubs",showPrevClass="prevShowTd",
                showNextClass="nextShowTd";
        if(forPapers){
            indexes=removeShadowsForPapers($this);            
            prevClass="showPrevPapers";
            nextClass="showNextPapers";
            showNextClass="nextShowPapersTd";
            showPrevClass="prevShowPapersTd";            
        }else{
            indexes=removeShadowsForSubs($this);
        }
        
        
        var paperIndex=indexes.paperIndex;
        var subjectIndex=indexes.subjectIndex;
        var prevNextEls;
        var isPrevType=false;
        
        var classStrip=".paper"+paperIndex+".subject";
        var targetIndex=subjectIndex;
        var counter=2,maxElsInitials=3;
        var nonnerClass="nonnerForSubs";
        if(forPapers){
            classStrip=".paper";
            targetIndex=paperIndex;
            counter=1;
            maxElsInitials=2;
            nonnerClass="nonnerForPapers";
        }
        
        
        if(showType==="PREV"){
            isPrevType=true;
            prevNextEls=td.prevAll(classStrip);
        }else{
            prevNextEls=td.nextAll(classStrip);
        }
                
        var maxEls=maxElsInitials;
        if(prevNextEls.length<maxElsInitials){
            maxEls=prevNextEls.length;
        }
        
        var p=counter;
        
        for(var k=1;k<(maxEls+1);k++){
            var addClassElsIndex;
            if(isPrevType){
                addClassElsIndex=targetIndex-k;
            }else{
                addClassElsIndex=targetIndex+k;
            }
            
            var removeClassEls=fullMarksSheetPage.find(classStrip+addClassElsIndex);
            removeClassEls.removeClass(nonnerClass);
            
            
            
            var removeClassElsIndex;
            if(isPrevType){
                removeClassElsIndex=targetIndex+p;
            }else{
                removeClassElsIndex=targetIndex-p;
            }
            var addClassEls=fullMarksSheetPage.find(classStrip+removeClassElsIndex);
            addClassEls.addClass(nonnerClass);
            p--;
            
            if(k===maxEls){
                var nextShowEls,prevShowEls;
                if(isPrevType){
                    nextShowEls=fullMarksSheetPage.find(classStrip+(removeClassElsIndex-1));
                    if(removeClassEls.first().prev(classStrip).length>0){
                        prevShowEls=removeClassEls;
                    }                    
                }else{
                    if(removeClassEls.first().next(classStrip).length>0){
                        nextShowEls=removeClassEls;
                    }    
                    prevShowEls=fullMarksSheetPage.find(classStrip+(removeClassElsIndex+1));
                }
                
                if(nextShowEls){                    
                    if(forPapers){
                        nextShowEls=nextShowEls.filter(".testNameTd,.totalMarks");
                    }         
                    nextShowEls.addClass(showNextClass);
                    addShowNextClass(nextShowEls.first(),nextClass);
                }
                
                if(prevShowEls){
                    if(forPapers){
                        prevShowEls=prevShowEls.filter(".testNameTd,.subject1");
                    }
                    prevShowEls.addClass(showPrevClass);
                    addShowPrevClass(prevShowEls.first(),prevClass);
                }
            }
        }
    };     
    var addShowNextClass=function(target,className){
        if(target.find("."+className).length===0){        	
        	var el=$("<a class='"+className+"'></a>");
            target.append(el);
            var top=(target.height()-10)/2;
            el.css("top",top);
        }        
    };
    var addShowPrevClass=function(target,className){
        if(target.find("."+className).length===0){
        	var el=$("<a class='"+className+"'></a>");
            target.append(el);
            var top=(target.height()-10)/2;
            el.css("top",top);            
        }        
    };    
    var removeShadowsForSubs=function($this){
        var td=$this.closest("td");
        var $data=td.data();
        var subjectIndex=$data.subject;
        var paperIndex=$data.paper;
        var sisterEls=fullMarksSheetPage.find(".paper"+paperIndex+".subject"+subjectIndex);
        sisterEls.removeClass("nextShowTd prevShowTd")
                .find(".showNextSubs,.showPrevSubs").remove();                
        return {subjectIndex:subjectIndex,paperIndex:paperIndex};
    };  
    var removeShadowsForPapers=function($this,paperIndex){
        var td=$this.closest("td");
        var $data=td.data();
        if(!paperIndex){
            paperIndex=$data.paper;
        }        
        var sisterEls=fullMarksSheetPage.find(".paper"+paperIndex);        
        sisterEls.removeClass("nextShowPapersTd prevShowPapersTd")
                .find(".showNextPapers,.showPrevPapers").remove(); 
        return {paperIndex:paperIndex};
    };
    
    this.onPaperChange=function(vChoose,value){
        if(value!="-1"){
            var allTds=fullMarksSheetPage.find(".paper").addClass("nonner");
            var paperEls=allTds.filter(".paper"+value).removeClass("nonner nonnerForPapers");
            removeShadowsForPapers(vChoose,value);
            paperEls.find(".subjectMarks").addClass("nonner")
                    .siblings(".testSubjectStats").removeClass("nonner");
        }else{
            var els=fullMarksSheetPage.find(".paper").removeClass("nonner");
            els.find(".subjectMarks").removeClass("nonner")
                    .siblings(".testSubjectStats").addClass("nonner");
            fullMarksSheetPage.find(".paper").not(".paper1,.paper2").addClass("nonnerForPapers");
            assignShadowsForPapers();
        }
	try{
		fullMarksSheetPage.parent().data("lastVChooseValue",value);
	}catch(err){}
    };
    var showMeasuresOfTest=function(){
        marksSheetPage.onPaperChange($(this),"1");
    };
})(jQuery);
var onPaperChange=marksSheetPage.onPaperChange;
LMHandlerDivCallbackFns["resetResultSheet"]=function(LMHandlerDiv){
    var vChoose=LMHandlerDiv.closest("#fullMarksSheetPage").find(".paperChangevChoose");
    var value=vChoose.data("value");
    if(value!="-1"){
        marksSheetPage.onPaperChange(vChoose,"-1");
        marksSheetPage.onPaperChange(vChoose,value);
    }
};

