//ui samples
var qsSample,qsInputerSample,commItemSample,replyItemSample,replyWidgetSample,commWidgetSample,discItemSample;
var quesAddToStep1Sample,quesAddToPLSample,reviewItemSample,docPageCommItemSample,createPlaylistSample,img30Sample,img20Sample;
var myNameChangeList,profilePicChangeList,peoplePopupSample,feSample,shareEntrySuggSample;
var firstLoadFnsFired=false;
var ATTTSampleEXAM,ATTTSampleSUBJECT,ATTTSampleTOPIC,ATSTPSubTopicDivSample;
var ATTTSubTopicSample,ATTTSubSubTopicSample,addTagsBtnsSample;
var QAOptSample,shareWithSample;
$(function(){
    if(!firstLoadFnsFired){
        firstLoadFns();
        firstLoadFnsFired=true;
    }           
    myNameChangeList=[qsSample,commItemSample,replyItemSample,discItemSample,reviewItemSample,instCommItemSample];
    profilePicChangeList=[qsInputerSample,replyWidgetSample,commWidgetSample,img30Sample,img20Sample];
});
function firstLoadFns(){    
    var uiSamplesDiv=$("#uiSamplesDiv");

    //for profile pic and name of the user
    img30Sample= makeHTMLTag('div');    
    img30Sample.html(uiSamplesDiv.children("#img30Sample").html());
    uiSamplesDiv.children("#img30Sample").remove();

    img20Sample= makeHTMLTag('div');
    img20Sample.html(uiSamplesDiv.children("#img20Sample").html());
    uiSamplesDiv.children("#img20Sample").remove();

    //for people popup
    peoplePopupSample=createCommonUIEl(uiSamplesDiv.children("#peoplePopupSample"));


    //for add to pls ans tests
    quesAddToStep1Sample=createCommonUIEl(uiSamplesDiv.children("#quesAddToStep1Sample"));

    //for add to pls ans tests
    quesAddToPLSample=createCommonUIEl(uiSamplesDiv.children("#quesAddToPLSample"));


    //ques soln inputer
    qsInputerSample=createCommonUIEl(uiSamplesDiv.children("#QSInputerSample"));

    //ques soln sample
    qsSample=createCommonUIEl(uiSamplesDiv.children("#QSSample"));

    //for comment widget
    commWidgetSample=createCommonUIEl(uiSamplesDiv.children("#commWidgetSample"));

    //FOR COMMENT
    commItemSample=createCommonUIEl(uiSamplesDiv.children("#commItemSample"));

    //FOR INST PAGE COMMENT    	
    instCommItemSample=createCommonUIEl(uiSamplesDiv.children("#instCommItemSample"));

    //FOR REPLY
    replyItemSample=createCommonUIEl(uiSamplesDiv.children("#replyItemSample"));

    //FOR REPLY Widget DIV
    replyWidgetSample=createCommonUIEl(uiSamplesDiv.children("#replyWidgetSample"));

    //FOR DISCUSSION
    discItemSample=createCommonUIEl(uiSamplesDiv.children("#discItemSample"));


    //for docview page comments
    reviewItemSample=createCommonUIEl(uiSamplesDiv.children("#reviewItemSample"));
    docPageCommItemSample=createCommonUIEl(uiSamplesDiv.children("#docPageCommItemSample"));

    //create playlist
    createPlaylistSample=createCommonUIEl(uiSamplesDiv.children("#createPlaylistSample"));
    
    //rte
    rteSample=createCommonUIEl(uiSamplesDiv.children("#rteSample"));    
    
    //entry sugg sample
    shareEntrySuggSample=createCommonUIEl(uiSamplesDiv.children("#shareEntrySuggSample"));
    
    //tagging
    ATTTSampleEXAM=createCommonUIEl(uiSamplesDiv.children("#ATTTSampleEXAM"));
    ATTTSampleSUBJECT=createCommonUIEl(uiSamplesDiv.children("#ATTTSampleSUBJECT"));
    ATTTSampleTOPIC=createCommonUIEl(uiSamplesDiv.children("#ATTTSampleTOPIC"));
    ATSTPSubTopicDivSample=createCommonUIEl(uiSamplesDiv.children("#ATSTPSubTopicDivSample"));
    ATTTSubTopicSample=createCommonUIEl(uiSamplesDiv.children("#ATTTSubTopicSample"));
    ATTTSubSubTopicSample=createCommonUIEl(uiSamplesDiv.children("#ATTTSubSubTopicSample"));

    addTagsBtnsSample=createCommonUIEl(uiSamplesDiv.children("#addTagsBtnsSample"));       
    
    
    //ques opt sample
    QAOptSample=createCommonUIEl(uiSamplesDiv.children("#QAOptSample"));    
    
    //share with sample
    shareWithSample=createCommonUIEl(uiSamplesDiv.children("#shareWithSample"));    
}



//Top Bar of Home Page
function getNewPageUrl(source){
    var url = window.location.href;
    try{ // TO Identify new page open and its relative url #768
    	if(source && typeof source == "object"){
		if(source.jquery && source.get(0) && source.get(0).href){
    			url = source.get(0).href;
		}else if(source.href){
    			url = source.href;
		}
    	}
    }catch(err){
    	url = window.location.href;
    }
    return url;
}
var timerObj = new function(){
	var timersObj = {
	};
	this.set = function(key,fn,time){
	   if(key && fn){
		if(timersObj[key]){
			clearInterval(timersObj[key]);
			timersObj[key] = undefined;
		}
		timersObj[key] = setInterval(fn,time);
	   }
	};
	var stop = this.stop = function(key){
		if(timersObj[key]){
			putConsoleLogs("stop timer = "+timersObj[key]);
			clearInterval(timersObj[key]);
			timersObj[key] = undefined;
			$(timersObj).removeProp(key);
		}
	};
	this.stopAll = function(){
	   if(timersObj){
		for(key in timersObj){
			stop(key);
		}	
	   }
	}
};
function beforePageOpen(){
    try{
    	closeVPopup();
	hideVMsgBox();
    	cancelAllCommonPopup();
	stopAllHtml5Video();
	timerObj.stopAll();
    }catch(err){}
}
function openMyPage(url,params,callBack,cbParams,showDots,source){
    if(showDots){
	bigLoader("#noTabSection");
    }else{
    	showTopLoader();
    }
    beforePageOpen();
    params["newPageOpen"] = getNewPageUrl(source);
    window.scrollTo(0,0);
    vReq.get(url,params,function(data,textStatus,xhr){
        
        hideTopLoader();        
        $("#noTabSection").html(data);
	try{callBack(cbParams);}catch(err){}
        //if(source)clickStream.recordElem(source,"CLICK",params,xhr);
        if(params.tabType)showNoTabSec(params.tabType);
    });
}
$("#settingsImg").live('click',function(){
    addToggler($("#settingsOptions"),$(this));
});
$("#homePage,#homePageImg,.takeToHomePage").live('click',function(e){
    goToHomePage($(this));
    pushHistory(null , null, this.href );
    if(e) e.preventDefault();
    try{
	instHeader.hide();
    }catch(err){}
});
function goToHomePage(source){
	goToMyInstitutePage(source);
	return;
    openMyPage("/questions/homeContent",{start:0,size:10,orderBy:"timeCreated",tabType:"HOME",resultType:"ALL"},homePageCallback,null,null,source);
}
function homePageCallback(){
    try{vSelectFns.init();}catch(err){}    
    var mcWidget=$("#homeMiddleSec").find(".mcWidget");
    var mcWidgetData={
                urlStr:"/questions/quesItems",
                params:{resultType:"ALL",target:"HOME_PAGE_FEED",orderBy:"timeCreated"},
                moreSize:25,
                initialSize:15
    };
    manageContent.init(mcWidget,mcWidgetData,true);
    $("#homeRightSec").load("/questions/homeRightSec");           
    $.get("/Application/getNotificationsSummary",function(data){
            if(data.errorMessage==''&&data.result.newNotificationCount>0){                        
               $("#notiHolder").find(".notiNo").html(data.result.newNotificationCount).removeClass("notiNoNone");                       
            }
    });    
}
$("#profilePage,.openMyProfile").live('click',function(e){
    goToMyProfilePage($(this));
    pushHistory(null , null, this.href );
    if(e) e.preventDefault();
});
function goToMyProfilePage($this){
    openMyPage("/profile/profileView",{tabType:"MY_PROFILE"},null,null,null,$this);
}
$(function(){
   $("#myInstitutePage").on('click',function(e){
	var $this = $(this);
	if($this.data('count')>1){
		var orgSelectDrop = $("#orgSelectDrop").removeClass("nonner");
		if(!$(this).hasClass("activeMenuTab")){
			orgSelectDrop.find(".selectedOrg").removeClass("selectedOrg");
		}
		setTimeout(function(){
			$(document).on('click',hideOrgSelectDrop);
		},100);
	}else{
    		goToMyInstitutePage($(this));
	}
	if(e) e.preventDefault();
   });
   $("#orgSelectDrop").on('click','.eachOrgName',function(e){
	var $this = $(this);
	var orgId = $this.data('orgId');
	goToMyInstitutePage($this,orgId);
	hideOrgSelectDrop();
	if(e) e.preventDefault();
   });
});
function hideOrgSelectDrop(){
	$("#orgSelectDrop").addClass("nonner");
	$(document).off('click',hideOrgSelectDrop);	
}
function setOrgSessions(orgId,cb){
	vReq.post("/Institute/setSession",{'orgId':orgId},cb);
}
function updateOrgInfo(orgId,source){
	myInstitutePage = $("#myInstitutePage");
	orgId = orgId?orgId:myInstitutePage.data("orgId");
	if(!orgId) return false;
	
	var orgObj;
	var $source = $(source);
	if(source && $source.hasClass('eachOrgName')){
		$source.addClass("selectedOrg").siblings('.selectedOrg').removeClass("selectedOrg");
		orgObj = $source.data('orgObj');
	}else{
	   $("#orgSelectDrop").find('.eachOrgName').each(function(){
		var $this = $(this);
		if($this.data('orgId') == orgId){
			$this.addClass("selectedOrg").siblings('.selectedOrg').removeClass("selectedOrg");
			orgObj = $this.orgObj;
		}
	   });
	   if(!orgObj){
		orgObj = getMyOrgInfo();
	   }
	}
   	myInstitutePage.data("orgId",orgId).data('orgObj',orgObj)
		.addClass("activeMenuTab").siblings(".activeMenuTab").removeClass("activeMenuTab");
	return orgObj;
}

var markContentCompleted=function(){
    var urlParams=fetchUrlParams();
    if(urlParams["context.id"]&&urlParams["entity.id"]){
        var entityId = urlParams['entity.id'];
        if(entityId.indexOf('?') != -1){
            entityId = entityId.split('?')[0];
        }
        $.post("/widgets/markContentCompleted",{"context.id":urlParams["context.id"],
        "context.type":urlParams["context.type"],
        "entity.type":urlParams["entity.type"],
        "entity.id":entityId});
    }
};
new function($){
	if(intervalObj){ clearInterval(intervalObj);}
	var intervalObj = setInterval(function(){
		try{
			if(myAssociateOrg){
				if(intervalObj){clearInterval(intervalObj);}
				defineFixProp(self,"myOrg",cloneObject(myAssociateOrg));
				defineFixProp(window,"getMyOrgInfo",function(){
					return myOrg;
				});
				myAssociateOrg = undefined;
			}
		}catch(err){}	
	},100);
	window["getMyOrgInfo"] = function(){
		return myAssociateOrg;
	};
}(jQuery);
function getOrgRequestInfo(){
	var div = $("#myInstitutePage");
	var orgObj = getMyOrgInfo();
	var orgId = div.data("orgId");
	if(!orgObj || !$("#instTopBar").data("shown")) return {"orgId":orgId};
	var data = {
		"orgId":orgId,
		"userRole":orgObj["userRole"],
		"memberId":orgObj["memberId"]
	};
	return data;
}
function openOrganizationPage(pr,orgId,append,source,pushUrl){
	orgObj = updateOrgInfo(orgId,source);
	if(!orgObj) return false;
	
	try{instHeader.clearTimer();}catch(err){}
	var orgId = orgObj.orgId;
	var url = pr['url'],cb = pr['cb'],cbParams = pr['cbParams'],
	fetchParams = pr['params']?pr['params']:{},
	showDots = pr['showDots'];
   	fetchParams['tabType']="MY_INSTITUTE";
	fetchParams['parent'] = {'type':'ORGANIZATION','id':orgId};
	fetchParams['orgId'] = orgId;
	fetchParams['userRole'] = orgObj["userRole"];
	fetchParams['memberId'] = orgObj["memberId"];
	/*fetchParams['instFullName'] = orgObj["fullName"];
	fetchParams['instLogo'] = orgObj["thumbnail"];*/
	if(append){
   		if(source != "HISTORY"){ /* Back Button And Fwd Button IMPL */
			pushInstHistory(orgId,append);
		}else{
			history.lastPathName = getInstPushUrl(orgId,append);
		}
	}else if(pushUrl){
		pushHistory(null , null, pushUrl);	
		history.lastPathName = location.pathname;
        	trackPageView();
	}
	try{
		if(institute && pr.directPgUrl){
			institute.goToUrl(pr.directPgUrl,null,fetchParams);	
		}else{
			openMyPage(url,fetchParams,cb,cbParams,showDots,source);
		}
	}catch(err){
		openMyPage(url,fetchParams,cb,cbParams,showDots,source);
	}
	return orgId;
}
function getInstPushUrl(orgId,append){
	append = append?"/"+append:"";
	var pushUrl = "/organization/"+orgId+append;
	pushUrl = encodeURI(pushUrl); 
	return pushUrl;
}
function pushInstHistory(orgId,append){
	var pushUrl = getInstPushUrl(orgId,append);
	pushHistory(null , null,pushUrl);
        trackPageView();
	return pushUrl;
}
function replaceInstHistory(orgId,append){
	append = append?"/"+append:"";
	var pushUrl = "/organization/"+orgId+append;
	pushHistory(null , null,pushUrl,true);
	return pushUrl;
}
function goToMyInstitutePage(source,orgId){
	/*try{
		if(institute){
			institute.goToHome();
			return;
		}	
	}catch(err){}*/
	var orgParams = {'url':"/Institute/home"};
	//var loadingSpan = $("#topLoader").find(".loadingSpanTop").addClass("nonner");
	openOrganizationPage(orgParams,orgId,"",source);
	/*loadingSpan.removeClass("nonner");
	hideTopLoader();*/
}
function goToInstResultAnalytics(source,orgId){
	var orgParams = {'url':"/Institute/resultAnalytics"};
	openOrganizationPage(orgParams,orgId,"resultanalytics",source);
}
function goToInstMySchedule(source,orgId){
	var orgParams = {'url':"/Institute/mySchedulePage"};
	openOrganizationPage(orgParams,orgId,"myschedule",source);
}
function goToInstClassroomConnect(source,orgId){
    var orgParams = {'url':"/Institute/myClassroomConnectPage"};
    openOrganizationPage(orgParams,orgId,"schedule",source);
}
function goToInstLibrary(source,orgId,tabId){
	var orgParams = {'url':"/Institute/libraryHome",'params':{'tabId':tabId}};
	openOrganizationPage(orgParams,orgId,"library",source);
}
function goToInstSectionLibrary(source,orgId,tabId,programId,centerId,sectionId){
	var orgParams = {'url':"/Institute/libraryHome",'params':{'tabId':tabId}};
	var pushUrl = "library";
	if(programId){
		pushUrl += "?program="+programId;
		if(centerId){
			pushUrl += "&center="+centerId;
			if(sectionId){
				pushUrl += "&section="+sectionId;
			}
		}
	}
	openOrganizationPage(orgParams,orgId,pushUrl,source);
}
function goToInstChlg(source,orgId){
	var orgParams = {'url':"/Institute/challengesHome"};
	openOrganizationPage(orgParams,orgId,"challenges",source);
}
function goToDiscussionPage(source,dissId,orgId){
	var orgParams = {'url':"/Institute/homeSingleDoubt",'params':{'id':dissId}};
	openOrganizationPage(orgParams,orgId,"discussion/"+dissId,source);
}
function goToOrgMembersPage(source,orgId){
	var orgParams = {'url':"/Institute/homeMembersUi",'params':{}};
	openOrganizationPage(orgParams,orgId,"people",source);
}
function goToAllDiscussionsPage(source,orgId){
	var orgParams = {'url':"/Institute/homeDoubts",'params':{}};
	openOrganizationPage(orgParams,orgId,"discussions",source);
}
function goToRecentActivityPage(source,orgId){
    var orgParams = {'url':"/Institute/activities",'params':{}};
    openOrganizationPage(orgParams,orgId,"activities",source);
}
function goToInstTestToppersPage(source,testId,orgId){
	var orgParams = {'url':"/Institute/getTestLeadersPage",'params':{'testId':testId}};
	openOrganizationPage(orgParams,orgId,"testleaders/"+testId,source);
}
function openInstProfile(source,userId,orgId){
	if(!userId) return false;
	var orgParams = {'url':"/Institute/openProfile",'params':{'targetUserId':userId}};
	openOrganizationPage(orgParams,orgId,"profile/"+userId,source);
}
function openMyOrders(source,orgId){
	var orgParams = {'url':"/Profile/myorders",
            'params':{start:0,size:50,"customer.type":"USER"}};
	openOrganizationPage(orgParams,orgId,"myorders",source);
}
function openInstFeed(source,feedId,orgId){
	var orgParams = {'url':"/Institute/feedUi",'params':{'feedId':feedId}};
	openOrganizationPage(orgParams,orgId,"feed/"+feedId,source);
}
function openAllNotifications(source,orgId){
	var orgParams = {'url':"/Institute/allNotifications"};
	openOrganizationPage(orgParams,orgId,"notifications/",source);
}
function openReferralPage(source,orgId){
    var orgParams= {'url':"/Institute/referralTerms"};
    openOrganizationPage(orgParams,orgId,"referralTerms",source);
}
function openInstVideo(source,vidId,orgId,moduleId){
    var pushUrl;
    var orgParams;
    if(moduleId != null && moduleId !="" && moduleId != undefined){
        pushUrl = "video/"+vidId+"?moduleId="+moduleId;
        orgParams = {'url':"/MyContents/videoPage",'params':{'id':vidId,'moduleId':moduleId}};
    }
    else{
        pushUrl = "video/"+vidId;
        orgParams = {'url':"/MyContents/videoPage",'params':{'id':vidId}};
    }
	var pushUrlObj = decideOnPushUrl(source,pushUrl);
	openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function openInstDoc(source,docId,orgId){
	var orgParams = {'url':"/MyContents/docPage",'params':{'id':docId}};
	var pushUrl = "document/"+docId;
	var pushUrlObj = decideOnPushUrl(source,pushUrl);
	openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function openInstFile(source,fileId,orgId){
	var orgParams = {'url':"/MyContents/filePage",'params':{'id':fileId}};
	var pushUrl = "file/"+fileId;
	var pushUrlObj = decideOnPushUrl(source,pushUrl);
	openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function openInstModule(source,moduleId,orgId,sectionId){
    var orgParams = {'url':"/MyContents/modulePage",'params':{'id':moduleId}};
    var pushUrl = "module/"+moduleId;
    if(sectionId !== undefined && sectionId !== null && sectionId !== "null"){
        pushUrl+= "?sectionId="+sectionId;
        orgParams['params']['sectionId'] = sectionId;
    }
    openOrganizationPage(orgParams,orgId,pushUrl,source);
}
function goToInstProgramsPage(source,orgId){
	var orgParams = {'url':"/Institute/myProgramsPage"};
	openOrganizationPage(orgParams,orgId,"myprograms",source);
}
function openInstQuesPage(source,qId,orgId){
	var params={id:qId};
    	if(source && typeof source == "object" && source.data("source")){
        	params.source=source.data("source");
    	}
	var orgParams = {'url':"/Institute/questionPage",params:params};
	openOrganizationPage(orgParams,orgId,"question/"+qId,source);
};
function openInstSubjectiveQuestionPage(source,testId,qId,orgId){
    var params = {id:qId,testId:testId};
    var orgParams = {'url':"/Tests/subjectiveQuestionPage",params:params};
    openOrganizationPage(orgParams,orgId,"test/"+testId+"/subjectivequestion/"+qId,source);
}
function goToInstAddQuestionPage(source,orgId){
	var orgParams = {'url':"/Institute/addQuestion"};
	openOrganizationPage(orgParams,orgId,"addquestion",source);
}
function goToInstTestPage(source,testId,targetUserId,targetUserRole,orgId, targetType,targetId){
   	var params = {'id':testId};
	var pushUrl = "test/"+testId;
   	if(targetUserId){
		params["targetUserId"] = targetUserId;
   		pushUrl += "?targetUserId="+targetUserId;
		if(targetUserRole){
			params["targetUserRole"] = targetUserRole;
			pushUrl += "&targetUserRole="+targetUserRole;
		}
        if(targetType != undefined && targetType!= "" && targetId != "" && targetId != undefined){
            pushUrl += "&targetType="+targetType+"&targetId="+targetId;
        }
	}else{
        if(targetType != undefined && targetType!= "" && targetId != "" && targetId != undefined){
            pushUrl += "?targetType="+targetType+"&targetId="+targetId;
            params['targetType'] = targetType;
            params['targetId'] = targetId;
        }
	}
	var orgParams = {'url':"/Tests/getOrgTest",'params':params};
    var pushUrlObj = decideOnPushUrl(source,pushUrl);
    openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function goToInstAsgnPage(source,asgnId,userRole,orgId){
   	var params = {'id':asgnId};
	var orgParams = {'url':"/Assignments/teacherPage",'params':params};
	var pushUrl = "assignment/teacher/"+asgnId;
	if(userRole.toUpperCase() == "STUDENT"){
		orgParams['url'] = "/Assignments/studentPage";
		pushUrl = "assignment/student/"+asgnId;
	}
	var pushUrlObj = decideOnPushUrl(source,pushUrl);
	openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function goToInstPreTestPage(source,testId,orgId,targetType,targetId){
   	var params = {'id':testId};        
	var pushUrl = "pretest/"+testId+"?targetType="+targetType+"&targetId="+targetId;
	var orgParams = {'url':"/Tests/getOrgPreTest",'params':params};
    var pushUrlObj = decideOnPushUrl(source,pushUrl);
	openOrganizationPage(orgParams,orgId,pushUrlObj.append,source,pushUrlObj.pushUrl);
}
function decideOnPushUrl(source,append){
	var obj = {
		'append':append,
		'pushUrl':''
	}
	try{
		if(source && typeof source == "object" && source.prop("href")){
			obj.pushUrl = source.prop("href");
			obj.append = '';
		}
	}catch(err){}
	return obj;
}
$(".askQuesInst").live('click',function(e){
   goToInstAddQuestionPage($(this));
   if(e) e.preventDefault();
   return;   
});
// $(".showAllMyNotifications").live('click',function(e){
//    openAllNotifications($(this));
//    if(e) e.preventDefault();
//    return;
// });

$(".terms").live('click',function(e){
    openReferralPage($(this));
    if(e) e.preventDefault();
    return;
});
$(".openInstVideo").live('click',function(e){
   var vidId = $(this).data("id");
   var orgId = $(this).data("orgId");
   var moduleId = $(this).data("moduleId");
   openInstVideo($(this),vidId,orgId,moduleId);
   if(e) e.preventDefault();
});
$(".openInstDoc").live('click',function(e){
   var docId = $(this).data("id");
   openInstDoc($(this),docId);
   if(e) e.preventDefault();
});
$(".openInstFile").live('click',function(e){
   var fileId = $(this).data("id");
   openInstFile($(this),fileId);
   if(e) e.preventDefault();
});
$(document).on("click",".openInstModule",function(e){
   var moduleId = $(this).data("id");
   var sectionId = $(this).data("sectionId");
   var orgId = $(this).data("orgId");
   openInstModule($(this),moduleId,orgId,sectionId);
   if(e) e.preventDefault();    
});
// $(".openInstProfile").live('click',function(e){
//    var userId=$(this).data("userId");
//    openInstProfile($(this),userId);
//    if(e) e.preventDefault();
// });
$(".openMyOrders").live('click',function(e){
   openMyOrders($(this),$(this).data("orgId"));
   if(e) e.preventDefault();
});
$(".openStatusFeed").live('click',function(e){
   var feedId=$(this).data("feedId");
   openInstFeed($(this),feedId);
   if(e) e.preventDefault();
});
//Discussions global fns
$(".openInstDoubtExt").live('click',function(e){
   var dissId = $(this).data("dissId");
   goToDiscussionPage($(this),dissId);
   if(e) e.preventDefault();   
});

$(document).on("click",".instMsgSeeAll",function(e){
   pushInstHistory($("#myInstitutePage").data('orgId'),"messages/inbox");
   openMessagesInbox($(this));
});
var openMessagesInbox = function(source){
   openMyPage("/UserMessages/openInbox",{tabType:"MESSAGES"},null,null,null,source);
}
var openMessagesConversation = function(convId){
   var params = {tabType:"MESSAGES","userConversationId":convId};
   openMyPage("/UserMessages/openConversation",params,null,null,null,null);
}

$("#myContentPage,.skipTocPage").live('click',function(e){
    goToMyContentPage($(this));
    pushHistory(null,null,this.href);
    if(e) e.preventDefault();
    return;
});
function goToMyContentPage($this,pathStrip){
   var params = {tabType:"MY_CONTENT"};
   if(pathStrip && pathStrip.length>0){
   	params["tabId"] = pathStrip;
   }
   openMyPage("/MyContents/index",params,null,null,null,$this);
    if($this&&$this.hasClass("skipTocPage")){
        if($this.closest(".uploadPageHolder").data('tabType')=="UPLOAD")tabClose("UPLOAD");
       else if($this.closest(".uploadPageHolder").data("tabType")=='EDIT_DOC')tabClose("EDIT_DOC");
    }
}

$(document).on("click","#exploreContent,.goToExploreContent",function(e){
    goToExploreContentPage($(this));
    pushHistory(null , null, this.href );
    if(e) e.preventDefault();
    return;
});
function goToExploreContentPage(source,pathStrip){
    var params = {"exploreContentPage":"true",tabType:"EXPLORE_CONTENT"};
    if(pathStrip && pathStrip.length>0){
   	params["tabId"] = pathStrip;
    }
    openMyPage("/MyContents/index",params,null,null,null,source);
}


$(document).on("click",".goToChallengesPage",function(e){
    goToChallengesPage($(this));
    pushHistory(null , null, this.href );
    if(e) e.preventDefault();
    return;
});
function goToChallengesPage(source){
    openMyPage("/Challenges/challenges",{tabType:"CHALLENGES_PAGE"},null,null,null,source);
}

$(document).on("click",".goToTestSeriesPage",function(e){
    var testSeriesId = $(this).data("testSeriesId");
    goToTestSeriesPage(testSeriesId,$(this));
    pushHistory(null , null, this.href );
    if(e) e.preventDefault();
    return;
});
function goToTestSeriesPage(testSeriesId,$this){
	if(testSeriesId){
    		openMyPage("/Subscription/testSeriesPage",{'testSeriesId':testSeriesId},null,null,null,$this);
		return true;
	}else{
    		var popup=getCommonPopupBody(635);
    		popup.html($("#homePageTSWidget").find(".testSeriesSuggestion").html());
	}
}


//search
function goToSearchPage(){
    var allParams = getAllUrlParams();
    return searchContent.doSearch(allParams["query"],allParams["brdId"],allParams["eType"],true);
}



//upload
$(".uploadContent").live('click',function(){
    goToUploadPage();
    pushHistory(null , null, this.href );
    return false;
});
function goToUploadPage(){
     if(tabIndex("#DTDiv_UPLOAD")!=-1){
        tabFound("UPLOAD");
     }
    else{
        $("<div id='DTDiv_UPLOAD' class='toBeClosed DTUPLOAD'></div>").insertBefore($("#contentSectionHolder .DBTabPositioner"));
        footTab["UPLOAD"]();
        tabNotFound("UPLOAD","UPLOAD");
        showTopLoader();
        $.get("/UIComDocuments/uploadStart",function(data) {
          hideTopLoader();
          $("#DTDiv_UPLOAD").html(data);
        });
    }
    clearMenu();
}
function clearMenu(){
    $("#menuBar a").removeClass("activeMenuTab");
    $("#exploreContent").removeClass("activeMenuTab");
}

var activateMenu=new Object();
activateMenu["MY_CONTENT"]="#myContentPage";
activateMenu["MY_PROFILE"]="#profilePage";
activateMenu["MY_INSTITUTE"]="#myInstitutePage";
activateMenu["EXPLORE_CONTENT"]="#exploreContent";
function mainMenuActivate(tabType){
    clearMenu();
    if(tabType=="DOC_VIEW"||tabType=="PL_VIEW"){
        $("body").addClass("PL_DOC_VIEW");
    }
    else{
        $("#noTabSection").removeClass("fixedView_PL_VIEW fixedView_DOC_VIEW");
        $("body").removeClass("PL_DOC_VIEW");
    }
    //activate top menu item
    if(activateMenu[tabType]){
        $(activateMenu[tabType]).addClass("activeMenuTab");
    }
    $(".toBeClosed").css("display","none");
    $("#noTabSection").css("display","block");
    $("#footBar  .footTab").removeClass("activeDTTab");
  }

//documents global fns
$(".openDocPage").live('click',function(e){
   var docId=$(this).data("docId");
   openDocInViewer(docId);
   pushHistory(null , null, this.href );
   if(e) e.preventDefault();
   return;
});
function openDocInViewer(docId){    
   openMyPage("/Doc/docVideoPage",{docId:docId,tabType:"DOC_VIEW"});
}
$(".editMyDoc").live('click',function(e){
   var docId=$(this).data("docId");
   editingMyDoc(docId);
   pushHistory(null , null,"/editDoc");
   if(e) e.preventDefault();   
});
function editingMyDoc(docId,docName){
    //chnage the id to using _ .
     if(tabIndex("#DTDiv_EDIT_DOC")!=-1){
         tabFound("EDIT_DOC");
         if(docId!=$("#DTDiv_EDIT_DOC").attr("rel"))showError("You can only edit one document at a time");
     }
        else{
        $("<div id='DTDiv_EDIT_DOC' class='toBeClosed' rel='"+docId+"'></div>").insertBefore($("#contentSectionHolder .DBTabPositioner"));
        footTab["EDIT_DOC"](docName);
        tabNotFound("EDIT_DOC","EDIT_DOC");
        showTopLoader();
        vReq.get("/library/libraryedit",{docId:docId},function(data) {
            hideTopLoader();
            $("#DTDiv_EDIT_DOC").html(data);
        });
    }
    clearMenu();
}



//playlists global fns
$(".openPLView,.viewPlaylist").live('click',function(e){
   var plId=$(this).data("plId");
   openPLInViewer(plId);
   pushHistory(null , null, this.href );
   if(e) e.preventDefault();   
});
$(".editPlaylist").live('click',function(e){
   var plId=$(this).data("plId");
   curatePL(plId,$(this).data("title"));
   pushHistory(null , null,"/curateplaylist");
   if(e) e.preventDefault();   
});
function openPLInViewer(plId){
   openMyPage("/playlists/PLView",{playlistId:plId,tabType:"PL_VIEW"});
}
var playlistCurationCase={};
function curatePL(PLId,PLName){
         if(tabIndex("#DTDiv_PL_EDIT")>-1){
             tabFound("PL_EDIT");
             if(PLId!=$("#DTDiv_PL_EDIT").attr("rel")){
                 playlistCurationCase["PLId"]=PLId;
                 playlistCurationCase["PLName"]=PLName;
                 showError("Do you want to abandon the Playlist you are already making?","abandonPLCuration");
             }
         }
         else{
             var id="PL_EDIT";
              $("<div id='DTDiv_PL_EDIT' class='toBeClosed DTPLs' rel='"+PLId+"'></div>").insertBefore($("#contentSectionHolder .DBTabPositioner"));
              footTab["PL_EDIT"](PLName);
              tabNotFound(id,"PL_EDIT");
              showTopLoader();
              vReq.get("/playlists/PLCurate",{playlistId:PLId},function(data){
                    hideTopLoader();
                    $("#DTDiv_"+id).html(data);
               });
         }
        clearMenu();
}
$(".abandonPLCuration").live('click',function(){
    tabClose("PL_EDIT");
    var p=playlistCurationCase;
    curatePL(p.PLId,p.PLName);
});
$(".createPlaylist").live('click',function(){
    var popup=getCommonPopupBody(350);
    popup.html(createPlaylistSample.children().clone(true));
});
$(".confirmCreatePL").live('click',function(){
    var popup=$(this).closest(".commonPopup"),PLName=popup.find(".createPLPUNameInput").val();
    if(PLName!=""){
        clearPopupsAndTimers();
        vReq.post("/playlists/createPL",{title:PLName,defaultTopicHeading:"Topic 1"},function(data){
            if(checkJSONResponse(data)){
                var PLId=data.result.playlistId;
                curatePL(PLId,PLName);
            }
        });
    }
});


//tests global fns
$(".openTestPage").live('click',function(e){
   var testId=$(this).data("testId");
   var orgId=$(this).data("orgId");
   if(orgId){
   	var targetUserId=$(this).data("targetUserId");
   	var targetUserRole=$(this).data("targetUserRole");
   	goToInstTestPage($(this),testId,targetUserId,targetUserRole,orgId);
   	if(e) e.preventDefault();   
	return;
   }
   openTestPage(testId);
   pushHistory(null , null, "/test/"+testId);
   if(e) e.preventDefault();   
});
$(".openInstTest").live('click',function(e){
   var testId=$(this).data("testId");
   var targetUserId=$(this).data("targetUserId");
   var targetUserRole=$(this).data("targetUserRole");
   var targetType = $(this).data("targetType");
   var targetId = $(this).data("targetId");
   var orgId = $(this).data("orgId");
   if(targetId != undefined && targetId != "" && targetType != undefined && targetType != "" ){
        goToInstTestPage($(this),testId,targetUserId,targetUserRole,orgId,targetType,targetId);
   }
   else{
        goToInstTestPage($(this),testId,targetUserId,targetUserRole,orgId);
   }
   if(e) e.preventDefault();   
});
$(".openInstAssignment").live('click',function(e){
   var asgnId=$(this).data("testId");
   var userRole=$(this).data("userRole");
   if(asgnId && userRole){
   	goToInstAsgnPage($(this),asgnId,userRole);
   }
   if(e) e.preventDefault();   
});
$(".openInstPreTest").live('click',function(e){
	if($(this).parent().hasClass("disabled") || $(this).parent().parent().hasClass("disabled")){
		console.log("blocked");
		e.preventDefault();
		return;
	}
   var testId=$(this).data("testId");
   var targetType = $(this).data("targetType");
   var targetId = $(this).data("targetId");
   var orgId = $(this).data("orgId");
   goToInstPreTestPage($(this),testId,orgId,targetType,targetId);
   if(e) e.preventDefault();   
});
$(".openOfflineTestPage").live('click',function(e){
   var testId=$(this).data("testId");
   openOfflineTestPage(testId);
   pushHistory(null , null, this.href );
   if(e) e.preventDefault();   
});
$(".editTestPage").live('click',function(){
   var testId=$(this).data("testId");
   createTest({'entryPage':'addQuestion','testId':testId,createType:"EDIT"});
});
// $(".openInstAvailPrograms").live('click',function(e){
//    	goToInstProgramsPage($(this));
//     	if(e) e.preventDefault();
// });

// $(".instOpenAnalytics").live('click',function(e){
//     goToInstResultAnalytics($(this));
//         if(e) e.preventDefault();
// });


// $(".instOpenLibraryPage").live('click',function(e){
//     goToInstLibrary($(this));
//         if(e) e.preventDefault();
// });


// $(".instDoubtsForum").live('click',function(e){
//     goToAllDiscussionsPage($(this));
//         if(e) e.preventDefault();
// });

// $(".instRATab").live('click',function(e){
//     goToRecentActivityPage($(this));
//         if(e) e.preventDefault();
// });

function openTestPage(testId){
    openMyPage('/Tests/viewTest',{'testId':testId,tabType:"TEST"});
}
function openOfflineTestPage(testId){
    openMyPage('/Tests/getOfflineTest',{'testId':testId,tabType:"TEST"});
}
$(".openTestToppersPage").live('click',function(e){
   var testId=$(this).data("testId");
   var orgId=$(this).data("orgId");
   if(orgId){
   	goToInstTestToppersPage($(this),testId,orgId);
   }else{
   	goToTestToppersPage($(this),testId);
   }
   if(e) e.preventDefault();
   return;
});
function goToTestToppersPage(source,testId){
   	openMyPage("/Tests/getTestLeadersPage",{'tabType':"TEST",'testId':testId},null,null,null,source);
   	if(source == "HISTORY") return;
	pushHistory(null,null,"/testleaders/"+testId);
}
$("#addQuestionPage").live("click",function(){
    openMyPage('/Questions/addQuestion',{tabType:"ADD_QUESTION"});
});

$(".createTest").live('click',function(e){
   createTest({createType:"NEW"});
   pushHistory(null , null,"/createtest" );
   if(e) e.preventDefault();
   return;
});

var testCreationCase={};
function createTest(params){
     if(tabIndex("#DTDiv_CREATE_TEST")>-1){
         tabFound("CREATE_TEST");
         if(params.createType=="NEW"||(params.createType=="EDIT"&&params.testId!=$("#DTDiv_CREATE_TEST").attr("rel"))){
             testCreationCase=params;
             showError("Do you want to abandon the test you are already making?","abandonTestTab");
         }
     }
     else{
         var id="CREATE_TEST";
         var rel="";
         if(params.testId)rel=params.testId;
          $("<div id='DTDiv_CREATE_TEST' class='toBeClosed' rel='"+rel+"'></div>").insertBefore($("#contentSectionHolder .DBTabPositioner"));
          footTab["CREATE_TEST"]();
          tabNotFound(id,"CREATE_TEST");
	  showTopLoader();
	  $(params).removeProp("createType");
            vReq.get("/tests/creation",params,function(data){
	  	hideTopLoader();
                $("#DTDiv_CREATE_TEST").html(data);
                //testCreation.init();
            });
     }
    clearMenu();
}
$(".abandonTestTab").live('click',function(){
    tabClose("CREATE_TEST");
    createTest(testCreationCase);
    testCreationCase={};
});


//questions global fns
$(".openQuesPage").live('click',function(e){
   var orgId = $(this).data("orgId");
   var qId = $(this).data("qid");
   if(orgId){
   	openInstQuesPage($(this),qId,orgId);
   }else{
   	openQuesPage(qId,$(this));
    	pushHistory(null , null, this.href );
   }
   if(e) e.preventDefault();   
});
$(".openInstSubjectiveQuestion").live("click",function(e){
    var orgId = $(this).data("orgId");
    var testId = $(this).data("testId");
    var qId = $(this).data("qId");
    if(!testId){
        return;
    }
    if(orgId){
        openInstSubjectiveQuestionPage($(this),testId,qId,orgId);
    }
    if(e) e.preventDefault();
});
$(".quesUnavailable").live('click',function(e){
	closeVPopup();
	showError("This question is currently un-available");
	return false;
});

function openQuesPage(qid,$this){
    var params={qId:qid,id:qid,solStart:0,solSize:15,tabType:"QUESTION_PAGE"};
    if($this&&$this.data("source")){
        params.source=$this.data("source");
    }
    openMyPage("/questions/questionPage",params,null,null,null,$this);
}
$(".saveQues").live('click',function(e){
   var qid=$(this).data("qid"),$this=$(this);
   $this.removeClass("saveQues").addClass("unsaveQues");
   $this.children("span").text("Unsave");
   var params={entity:{type:"QUESTION",id:qid}};
   vReq.post("/Widgets/followEntity",params,function(data,textStatus,xhr){
        //clickStream.recordElem($this,"CLICK",params,xhr);
   })
});
$(".unsaveQues").live('click',function(e){
   var qid=$(this).data("qid"),$this=$(this);
   $this.addClass("saveQues").removeClass("unsaveQues");
   $this.children("span").text("Save");
   var params={entity:{type:"QUESTION",id:qid}};
   vReq.post("/Widgets/unfollowEntity",params,function(data,textStatus,xhr){
        //clickStream.recordElem($this,"CLICK",params,xhr);
   })
});
$(".quesAddToPL").live('click',function(){
   var qId=$(this).data("qId");
   //post req
});
$(".quesAddToTest").live('click',function(){
   var qId=$(this).data("qId");
   //post req
});

$(".askQues").live('click',function(e){
   goToAddQuestionPage($(this));
   pushHistory(null , null, this.href );
   if(e) e.preventDefault();
   return;   
});
function goToAddQuestionPage($this){
    openMyPage("/questions/addquestion",{tabType:"ADD_QUESTION"},null,null,null,$this);
}


//boards global fns
$(".boardExam").live('click',function(e){
   /*var brdId=$(this).data("brdId");
   openExamPage(brdId,$(this));
   pushHistory(null , null, this.href );*/
   if(e) e.preventDefault();
   return;
});
function openExamPage(brdId,$this){
   openMyPage("/boards/boardExam",{brdId:brdId,tabType:"BOARD_EXAM"},$this);
}
$(".boardSubject").live('click',function(e){
   /*openSubjectPage($(this).data("brdId"))
   pushHistory(null , null, this.href );*/
   if(e) e.preventDefault();
   return;
});
function openSubjectPage(brdId){
   openMyPage("/boards/boardSubject",{brdId:brdId,tabType:"BOARD_SUBJECT"});
}
$(".boardTopic,.BSPTopic").live('click',function(e){
   /*openTopicPage($(this).data("brdId"))
   pushHistory(null , null, this.href );*/
   if(e) e.preventDefault();
});
function openTopicPage(brdId){  
   openMyPage("/boards/boardTopic",{brdId:brdId,tabType:"BOARD_TOPIC"});
}
$(".boardSubTopic").live('click',function(e){
   /*openSubTopicPage($(this).data("brdId"))
   pushHistory(null , null, this.href );*/
   if(e) e.preventDefault();
});
function openSubTopicPage(brdId){
   vReq.get("/boards/getBoardInfo",{brdId:brdId},function(data){
       var subTopic=data.result,topicId=subTopic.parent.brdId;
       openMyPage("/boards/boardSubTopic",{brdId:topicId,subTopicName:subTopic.name,
           subTopicId:subTopic.brdId,tabType:"BOARD_SUB_TOPIC"});
   });
}



//users global fns
$(".openUserProfile").live('click',function(e){
   var userId=$(this).data("userId");
   openUserProfile(userId,$(this));
   if(e) e.preventDefault();
});
function openUserProfile(userId,source){
   if(userId==USERID){
    pushHistory(null , null,"/myprofile");
    goToMyProfilePage(source);
   }
   else{
    showOthersProfile(userId,source);
    if(source)pushHistory(null , null,source.attr("href"));
   }
}
function showOthersProfile(userId,source){
    openMyPage("/Profile/profileOthers",{targetUserId: userId,tabType:"OTHERS_PROFILE"},null,null,null,source);
}
function getUserUrlAndClass(user){
    return "<a data-user-id='"+user.id+"' data-element='USER_FULLNAME' \n\
            class='openUserProfile ptrTagHovered blocky' href='/user/"+user.id+"'>\n\
             "+user.firstName+" "+user.lastName+"</a>";
}

//vooteup widget
$(".upVoteItem").live('click',function(e){
    var $this=$(this),$thisData=$this.data();
    increaseCount($this.siblings(".upVoteItemCount"));
    $this.removeClass("upVoteItem").addClass("upVotedItem");
    vReq.post("/widgets/upVoteItem",{entity:{type:$thisData.entityType,id:$thisData.entityId}},function(data,s,xhr){
        $this.siblings(".upVoteItemCount").text(data.result.upVotes);
    });
});

//follow unfollow entity
var followEntity={},unfollowEntity={};
followEntity["POPULAR_PLAYLIST"]=function($this){
    getNewPopularPL($this.closest(".PLItem"));
}
followEntity["POPULAR_DOC"]=function($this){
    if($this.closest(".docItem").length>0)
    getNewPopularDoc($this.closest(".docItem"));
}
followEntity["POPULAR_TEST"]=function($this){
    getNewPopularTest($this.closest(".testItem"));
}
followEntity["USER"]=function($this){
    $this.text("Unfollow");
}
followEntity["FRND_SUGG"]=function($this){
    getNewFrndSugg($this.closest(".frndSugg"));
}
followEntity["FOLLOW_DISCUSSION"]=function($this){
    $this.removeClass("greenButton").text("Unfollow");
}
$(".followEntity").live('click',function(e){
   var $this=$(this),d=$this.data();
   $this.removeClass("followEntity").addClass("unfollowEntity");
   $this.text("Unsave");
   if(followEntity[d.callback])followEntity[d.callback]($(this));
   var params={entity:{type:d.entityType,id:d.entityId}};
   vReq.post("/Widgets/followEntity",params,function(data,s,xhr){
        //clickStream.recordElem($this,"CLICK",params,xhr);
   })
});

$(".unfollowEntity").live('click',function(e){
   var $this=$(this),d=$this.data();
   $this.removeClass("unfollowEntity").addClass("followEntity");
   $this.text("Save");
   if(unfollowEntity[d.callback])unfollowEntity[d.callback]($(this));
   var params={entity:{type:d.entityType,id:d.entityId}};
   vReq.post("/Widgets/unfollowEntity",params,function(data,s,xhr){
        //clickStream.recordElem($this,"CLICK",params,xhr);
   })
});
unfollowEntity["USER"]=function($this){
    $this.text("Follow");
}
unfollowEntity["FOLLOW_DISCUSSION"]=function($this){
    $this.addClass("greenButton").text("Follow");
}



//Widgets on questions home page
var popularPLsData=[],popularTestsData=[],popularDocsData=[],frndSuggsData=[],popularPLsCrossed=[],
popularTestsCrossed=[],popularDocsCrossed=[],frndSuggsCrossed=[];
$(".popularPLCross").live('click',function(){
    var popularPL=$(this).closest(".PLItem");
    getNewPopularPL(popularPL);
});
function getNewPopularPL(popularPL){
    popularPLsCrossed.push(popularPL.find(".widgetItemName").data("plId"));
    if(popularPLsData.length>0){
        popularPL.html(popularPLsData.pop());
    }
    else{
        vReq.get("/Widgets/popularPLItems",{excludeIds:popularPLsCrossed,size:6,target:"HOME_PAGE_RECOS",orderBy:"avgRating"},function(data){
                var dummyDiv=$(document.createElement("div"));
                dummyDiv.html(data);
                dummyDiv.children(".PLItem").each(function(){
                    popularPLsData.push($(this).html());
                });
                if(popularPLsData.length>0){
                    popularPL.html(popularPLsData.pop());
                }
                else popularPL.remove();
        });
    }
}


$(".popularTestCross").live('click',function(){
    var popularTest=$(this).closest(".testItem");
    getNewPopularTest(popularTest,$(this));
});
function getNewPopularTest(popularTest,source){
    popularTestsCrossed.push(popularTest.find(".widgetItemName").data("testId"));
    if(popularTestsData.length>0){
        popularTest.html(popularTestsData.pop());
        //if(source)clickStream.recordElem(source,"CLICK",null,xhr);
    }
    else{
        var params={excludeIds:popularTestsCrossed,size:6,
            target:"HOME_PAGE_RECOS",orderBy:"avgRating"};
        vReq.get("/Widgets/popularTestItems",params,function(data,s,xhr){
                var dummyDiv=$(document.createElement("div"));
                dummyDiv.html(data);
                dummyDiv.children(".testItem").each(function(){
                    popularTestsData.push($(this).html());
                });
                if(popularTestsData.length>0){
                    popularTest.html(popularTestsData.pop());
                }
                else popularTest.remove();
                //if(source)clickStream.recordElem(source,"CLICK",params,xhr);
        })
    }
}





$(".popularDocCross").live('click',function(){
    var popularDoc=$(this).closest(".docItem");
    getNewPopularDoc(popularDoc)
});
function getNewPopularDoc(popularDoc){
    popularDocsCrossed.push(popularDoc.find(".widgetItemName").data("docId"));
    if(popularDocsData.length>0){
        popularDoc.html(popularDocsData.pop());
    }
    else{
        vReq.get("/Widgets/popularDocItems",{excludeIds:popularDocsCrossed,size:6,
            target:"HOME_PAGE_RECOS",orderBy:"avgRating",excludeTypes:["video"]},function(data){
                var dummyDiv=$(document.createElement("div"));
                dummyDiv.html(data);
                dummyDiv.children(".docItem").each(function(){
                    popularDocsData.push($(this).html());
                });
                if(popularDocsData.length>0){
                    popularDoc.html(popularDocsData.pop());
                }
                else popularDoc.remove();
        });
    }
}


$(".frndSuggCross").live('click',function(){
    var frndSugg=$(this).closest(".frndSugg");
    getNewFrndSugg(frndSugg);
});
function getNewFrndSugg(frndSugg){
    frndSugg.find(".blueSubmitButton").text("Unfollow");
    frndSuggsCrossed.push(frndSugg.find(".widgetItemName").data("userId"));
    if(frndSuggsData.length>0){
        frndSugg.html(frndSuggsData.pop());
    }
    else{
        vReq.get("/Widgets/frndSuggItems",{excludeIds:frndSuggsCrossed,size:6},function(data){
                var dummyDiv=$(document.createElement("div"));
                dummyDiv.html(data);
                dummyDiv.children(".frndSugg").each(function(){
                    frndSuggsData.push($(this).html());
                });
                if(frndSuggsData.length>0){
                    frndSugg.html(frndSuggsData.pop());
                }
                else frndSugg.remove();
        });
    }
}




//rating and review widget
$(".ratingWidget img").live('mouseover',function(){
  var ratingWidget=$(this).closest(".ratingWidget");
    ratingWidget.find("img").attr("src","/public/images/rateWhite.png");
    for (var i=1;i<=parseInt($(this).attr("class").substring(8));i++)
        ratingWidget.find(".rateStar"+i).attr("src","/public/images/rateYellow.png");
});
$(".ratingWidget").live('mouseout',function(){
    ratingFill($(this));
});
$(".ratingWidget img").live('click',function(){
   var $this=$(this);
   var ratingWidget=$(this).closest(".ratingWidget");
   var ratingGiven=$(this).attr("class").substring(8);
  if(ratingWidget.closest("#docPLReviewBox").length==0){
     vReq.post("/Widgets/rateEntity",{entity:{id:ratingWidget.data("entityId"),rating:ratingGiven,
         type:ratingWidget.data("entityType")}},function(data){
            if(data.errorMessage==''){
                updateReviewStats(data.result,$this);
            }
            else showError(COMMON_ERROR_MESSAGE);
        });
    }
    else{
        var isNewRating=false;
        if(parseInt(ratingWidget.data("myRating"))!=ratingGiven)isNewRating=true;
        ratingWidget.data("myRating",ratingGiven);
        ratingWidget.data("isNewRating",isNewRating);
        ratingFill(ratingWidget);
    }
});
function updateReviewStats(resp,$this){
    var rateAndReviewBox=$this.closest(".PLContentDiv,.docContentDiv");
    var r=resp.avgRating.toString().substr(0,4);
    rateAndReviewBox.find(".rateCircle").html(r);
    var ratingWidget=rateAndReviewBox.find(".ratingWidget");
    ratingWidget.data("myRating",resp.ratingByMe);
    ratingFill(ratingWidget);
    var details=resp.ratingDetails,votes=resp.totalVote;
    var reviewStars=rateAndReviewBox.find(".reviewStar")
    for(var k=0;k<5;k++){
        var pixels=(details[k+1]*65)/votes;
        reviewStars.eq(k).find(".starTop").width(pixels);
    }
}
var docPLReviewRef;
$(".addDocPLReview").live('click',function(){
    var reviewBox=$(this).closest("#leftSecContent").find("#DPLSImgHider").children().clone(true);
    var popup=getCommonPopupBody(450);
    popup.html(reviewBox);
    docPLReviewRef=$(this);
});
$(".submitDocPLReview").live('click',function(){
   var reviewBox=$(this).closest("#docPLReviewBox");
   var d=reviewBox.find(".ratingWidget").data();
   var rating=d.myRating;
   var isNewRating=false,i=d.isNewRating;
   if(i!=undefined||i==true)isNewRating=true;
    var type=d.entityType,id=d.entityId;
    var textContent=reviewBox.find("textarea").val();
    var reviewItem=reviewItemSample.children().clone(true);
    reviewItem.find(".reviewItemText").text(textContent);   
   if(!isNaN(rating)&&rating!=0&&textContent!=""){
      var LMHandlerDiv=$("#DPLSReviews .LMHandlerDiv");
    if(LMHandlerDiv.children(".userMessage").length>0){
        LMHandlerDiv.html(reviewItem);
    }else{
        LMHandlerDiv.prepend(reviewItem);       
    }
     vReq.post("/Widgets/postReview",{baseId:id,rating:rating,rootId:id,
         baseType:type,rootType:type,isNewRating:isNewRating,textContent:textContent},function(data){
            if(data.errorMessage==''){
               if(isNewRating){
                   updateReviewStats(data.result.ratingInfo,docPLReviewRef);
               }               
               reviewItem.find(".commReviewVoteDiv").data("commentId",data.result.commentId);
               reviewItem.find(".ratingStars").width(rating*11);
               reviewItem.find(".upVoteItem").data("entityId",data.result.commentId);
            }
            else showError(COMMON_ERROR_MESSAGE);
            clearPopupsAndTimers();
        });
   }
   else if(!isNaN(rating)||rating==undefined||rating==0){
        showError("You have to rate before reviewing");
   }   
});
function ratingFill(ratingWidget){
      var userRating=parseInt(ratingWidget.data("myRating"));
        ratingWidget.find("img").attr("src","/public/images/rateWhite.png");
        for (var i=1;i<=userRating;i++)
            ratingWidget.find(".rateStar"+i).attr("src","/public/images/rateYellow.png");
}
function getRatingStars(myRating){
    var rateImg='';
    if(myRating!=undefined){
        for(var i=1;i<=5;i++){
            if(i<=myRating)
            rateImg=rateImg+"<div class='floatLeft'><img class='rateStar"+i+"' src='/public/images/rateYellow.png' alt=''/></div>";
            else rateImg=rateImg+"<div class='floatLeft'><img class='rateStar"+i+"' src='/public/images/rateWhite.png' alt=''/></div>";
        }
    }
    return rateImg;
}



$(".commMoreTags").live('click',function(){
  var tagsParent=$(this).parent();
  if(tagsParent.children("li").html()==undefined)tagsParent.children("span").css("display","inline").addClass("showMoreClass");
  else tagsParent.children("li").css("display","list-item");
  tagsParent.children("a").html("..less").removeClass("commMoreTags").addClass("commLessTags");
});
$(".commLessTags").live('click',function(){
   var tagsParent=$(this).parent();
   var count=$(this).attr("rel");
  showMoreTags(tagsParent,count);
  tagsParent.children(".commLessTags").remove();
});
function showMoreTags(tagsParent,count){
               var moreTags=1;
           tagsParent.children("span,li").each(function(){
               if(moreTags>count)$(this).css("display","none").removeClass("showMoreClass");
               moreTags++;
           });
           moreTags--;
           if(moreTags>count){
               tagsParent.append("<a class='commMoreTags' rel='"+count+"'>..more</a>");
           }
}


$(".moreInfo").live('click',function(){
   var moreParent=$(this).parent();
       moreParent.children(".hided").css("display","inline");
        moreParent.children(".moreInfo").removeClass("moreInfo").addClass("lessInfo").html(" less");
        //take care of scrollbar
});
$(".lessInfo").live('click',function(){
   var moreParent=$(this).parent();
        moreParent.children(".hided").css("display","none");
        moreParent.children(".lessInfo").removeClass("lessInfo").addClass("moreInfo").html(" ..more");
        //take care of scrollbar
});
$(".deletex,.deletexShow").live('mouseover',function(){
    $(this).children("img").attr("src","/public/images/remAc.png");
});
$(".deletex,.deletexShow").live('mouseout',function(){
    $(this).children("img").attr("src","/public/images/rem.png");
});
$(".commentLikeImg,.feedCommentLikeImg").live('mouseover',function(){
    $(this).attr("src","/public/images/uicomDocuments/upActive.png");
});
$(".commentLikeImg,.feedCommentLikeImg").live('mouseout',function(){
    $(this).attr("src","/public/images/uicomDocuments/up.png");
});
$(document).on('click',".playRTEVideo",function(){
    if($(this).closest(".RTEArea").length==0){
        var videoDiv=$(this).closest(".RTEVideoDiv");
        videoDiv.addClass("playingRTEVideo");
        var w=videoDiv.width()*0.95;
        insertVideoPlayer(videoDiv.find(".RTEVideoTitle").attr("href"),$(this).parent(),w,350);              
    }
});



//loadmore
$(".LMHandlerDivLoadMore").live('click',function(){
    var $this=$(this);
    if(!$this.hasClass("loadingLM")){
        $this.addClass("loadingLM");
        var LMHandlerDiv=$(this).closest(".LMHandlerDiv");
        var LMData=LMHandlerDiv.data();
        var allParams={};
        if(LMData.allParams)allParams=LMData.allParams;
        allParams.size=LMData.size||10;
        allParams.start=$this.data("start")||LMHandlerDiv.children().not($this).length;
        $this.html("<span class='loadingSpan'></span>");
        vReq.get(LMData.urlStr,allParams,function(data,textStatus,xhr){
            $this.remove();
            LMHandlerDiv.append(data);
            var callback=LMHandlerDiv.data("callback");
	    if(callback && LMHandlerDivCallbackFns[callback]){
		callback = LMHandlerDivCallbackFns[callback];
	    }else{
		callback = LMHandlerDiv.data("cbFn");	
	    }
            if(callback){
                try{
			callback(LMHandlerDiv,data,allParams);
		}catch(err){}
            }
            loadMJEqns(LMHandlerDiv.get(0));
            //clickStream.recordElem($this,"CLICK",allParams,xhr);
        });
    }
});
var LMHandlerDivCallbackFns={};



function loadPeoplePopup(urlStr,heading,total,params){
    var popup=getCommonPopupBody(400,null,true);
    popup.html(peoplePopupSample.children().clone(true));
    popup.find(".poeplePopupHeadName").text(heading);
    popup.find(".peoplePopupCount").text(total);
    var LMHandlerDiv=popup.find(".LMHandlerDiv");
    var allParams={};
    if(params)allParams=params;
    allParams.start=0;allParams.size=15;
    LMHandlerDiv.data("urlStr",urlStr).data("size",15).data("allParams",allParams)
    vReq.get(urlStr,params,function(data){
        LMHandlerDiv.html(data);
    });
}
$(".refreshQues").live('click',function(){
	var $this = $(this);
	var holder = $this.closest($this.data("closest"));
	$this.trigger("pre-refresh");
	if(!holder.get(0)){
		holder = $this.parent();
	}
	holder.find("img").each(function(index,img){
		$(img).attr("src",img.src);
	});
	var type = $this.data("type");
	holder.find(".MathJax_Display").each(function(index,val){
		var $this = $(this);
		var eqElem = $this.siblings("script:first");
		var val = eqElem.html();
		eqElem.remove();
		$this.siblings(".MathJax_Preview:first").remove();
		$this.replaceWith("$$"+val+"$$");
	});
	var tt = holder.html();
	//holder.html("");
	var temp = $("#tempRenderDiv").html(tt);
	var postFn = function(){
		holder.html(temp.html());
		temp.html("");
		holder.find(".refreshQues").trigger("post-refresh");
	};
	var loadMathJaxFn = loadMJEqns;
	if(type && type.toUpperCase() == "TEST"){
		loadMathJaxFn = loadTestMJEqns;	
	}
	try{
        	loadMathJaxFn(temp.get(0),postFn);
	}catch(err){}
});

//promotional
$(".WCWDocVideoMoreInfo").live('click',function(){
        showTopLoader();
        var sourceEl=$(this);
        $.get("/widgets/moreDocVideoInfo",function(data,s,xhr){
            hideTopLoader();
            var popup=getCommonPopupBody(800);
            popup.html(data);
            //clickStream.recordElem(sourceEl,"CLICK",null,xhr);
        });
});


$(".logoutMe").live("click",function(){
	window.location.href = "/security/logout?orgId="+$("#myInstitutePage").data('orgId');
});
$(".removeBrowseAsOtherUser").live("click",function(){
	var orgUserLastHistory = window.localStorage.orgUserLastHistory;
        if(!orgUserLastHistory){
                orgUserLastHistory = "/";
        }
	vReq.post("/security/removeBrowseAsUser",{},function(data){
       	    window.localStorage.orgUserLastHistory = "";
	    pushHistory({},"Vedantu","/",true);
            window.location.href = orgUserLastHistory;
	});
});
$(".useMemberProfile").live("click",function(){
	var $data = $(this).data();
	var params = {"targetUserId":$data.userId};
        vReq.post("/security/browseAsUser",params,function(data){
            if(data.errorCode){
                showError(i18nJS("SUFFICIENT_RIGHT_MISSING"));
            }else{
            	window.localStorage.orgUserLastHistory = window.location.href;
                //window.history.go(-(window.history.length-1));
                window.location.href = "/";
	    }
        });
});
var getUserObjForUI = function(user){
	var usr = {name:"",userId:"",isOrgUser:false,orgId:"",profile:user.profile};
	usr.name = user.firstName + " " + user.lastName;
	usr.userId = user.id;
	var orgId = $("#myInstitutePage").data('orgId');
	if(user.userId && user.orgId){
		usr.userId = user.userId;
		if(user.orgId == orgId){
			usr.isOrgUser = true;
			usr.orgId = user.orgId;
		}
	}else{
		usr.isOrgUser = true;
		usr.orgId = orgId;
	}
	return usr;
}
var customAjaxSetup = new function(){
	this.beforeSend = function(xhr,settings){
	};
};
$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
	var orgData = getOrgRequestInfo();
	var data = originalOptions.data;
	data = transformParams(originalOptions.data);
	data["clientTimeOffset"] = new Date().getTimezoneOffset() * 60000;
	data["myUserId"] = USERID;
	data["clientServerTimeDelta"] = function(){
		var ret = 0;
		ret = $("body").data("clientServerTimeDelta");
		if(!ret){
			$("body").data("clientServerTimeDelta",clientServerTimeDelta);
			ret = clientServerTimeDelta;
		}
		return ret;
	}();
	if(!orgData){
		options.data = $.param(data);	
	}else{
   		options.data = $.param($.extend({},orgData, data));
	}
	/*
		options.data = data + "&" + $.param(orgData);
	*/
});
var onPageHistoryBack = {
	"TEST" : function(){
		return readUrlPostTest();	
	},
	"MESSAGES" : function(urlPathStrips){
		if(urlPathStrips[3]=="inbox"){
			return userMessages.inbox.onBack();
		}
		return false;
	},
	"LIBRARY" : function(urlPathStrips){
		return instLibrary.onBack(urlPathStrips[3]);	
	},
	"DISCUSSIONS" : function(){
		return institute.doubts.onBack();	
	},	
	"PEOPLE" : function(){
		return institute.people.onBack();	
	},	
};
var urlQueryHelper = new function(){
	var url;
	var readAll = this.readAll = function(){
		var params = getAllUrlParams();
		return params;
	};
	this.getQueryString = function(){
		var string = location.href;
		string = string.split("?")[1];
		string = string ? "?"+string : "";
		return string;
	};
	this.read = function(name){
		return getURLParameter(name);
	};
	this.push = function(name,val){
		writeUrl(name,val);	
	};
	this.replace = function(name,val){
		writeUrl(name,val,true);	
	};
	var writeUrl = function(name,val,isReplace){
		val = val?encodeURIComponent(val):val;
		var loc = location.pathname + location.search;
		var newLoc = loc;
		if(loc.contains(name)){
			newLoc = loc.substring(0,loc.indexOf("?"));
			var params = readAll();
			if(val){
				params[name] = val;
			}else{
				$(params).removeProp(name);
			}
			newLoc += "?";
			for(p in params){
				newLoc += p+"="+params[p]+"&";
			}
			newLoc = newLoc.substr(0,newLoc.length-1);
		}else if(val){
			var joinChar = newLoc.contains("?") ? "&" : "?";
			newLoc += joinChar + name + "=" + val;
		}
		if(isReplace){
			pushHistory(null,null,newLoc,true);
		}else{
			pushHistory(null,null,newLoc);
		}
	};
};
$(document).on('click', '.openInstAboutApp', function(e){
        var popup = showVPopup(0.6);
	bigLoader(popup);
	vReq.get("/Application/getAboutAppPopup",{},function(data){
		popup.html(data);
	});
	instHeader.settings.hideMe(e,false);
});
$(document).on('click', '.openMyAccessCodePopup', function(e){
        var popup = showVPopup(0.6);
	bigLoader(popup);
	var params = {
		orderBy : "timeCreated",
		sortOrder : "DESC"
	};
	vReq.get("/Profile/getMyAccessCodes",params,function(data){
		popup.html(data);
	});
	instHeader.settings.hideMe(e,false);
});
