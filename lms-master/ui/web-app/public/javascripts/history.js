var vHistory=new(function(){
    var urlPathStrips,urlParams,urlMapper={},orgUrlMapper={};
    
    var home="HOME",myprofile='MYPROFILE',mycontent='MYCONTENT',upload='UPLOAD',
    explorecontent='EXPLORECONTENT',challenges='CHALLENGES',search='SEARCH',user='USER',
    question='QUESTION',addquestion='ADDQUESTION',playlist='PLAYLIST',testSeries = "TESTSERIES",
    test='TEST',testleaders="TESTLEADERS",offlinetest='OFFLINETEST',document='DOCUMENT',video='VIDEO',editdoc='EDITDOC',
    curateplaylist='CURATEPLAYLIST',createtest='CREATETEST',exam='EXAM',subject='SUBJECT',
    topic='TOPIC',subtopic='SUBTOPIC',organization='ORGANIZATION',discussion='DISCUSSION',module="MODULE",myorders="MYORDERS";
    
    
    var siglePathList=[myprofile,upload,challenges,addquestion,editdoc,curateplaylist,createtest,
                       search,home,myorders];
    var doublePathList=[user,playlist,test,document,video,
                        offlinetest,exam,subject,topic,subtopic,question,module];
    //mycontent and explorecontent can be both sigle and double
    var pageNotFound=function(holder){
	holder = holder!=undefined?holder:$("#noTabSection");
        $.get("/uicomwidgets/pageNotFound",function(data){
           holder.html(data); 
        });
    };
    this.init=function(urlStrips,paramUrl){
        urlPathStrips=urlStrips;
        urlParams = paramUrl?paramUrl:{};
        var category=urlPathStrips[0].toUpperCase();
        if(category==""){
            urlMapper["HOME"]();
        }
        else if(urlMapper[category]) {
            if((siglePathList.indexOf(category)>=0 && urlPathStrips.length!=1)
                ||(doublePathList.indexOf(category)>=0 && urlPathStrips.length!=2)){
                pageNotFound(); 
            }else{
               	urlMapper[category]();
            }
        }else pageNotFound();
	//history.lastPathName = location.pathname; 
    }
    urlMapper[home]=function(){
        goToHomePage();
    }
    urlMapper[myprofile]=function(){
        goToMyProfilePage();
    }
    urlMapper[mycontent]=function(){
	var cTypes = urlPathStrips[1];
	cTypes = cTypes && typeof cTypes=="string"?cTypes.toLowerCase():"";
        goToMyContentPage(null,cTypes);
    }
    urlMapper[upload]=function(){
        goToUploadPage();
    } 
    urlMapper[explorecontent]=function(){
	var cTypes = urlPathStrips[1];
	cTypes = cTypes && typeof cTypes=="string"?cTypes.toLowerCase():"";
        goToExploreContentPage(null,cTypes);
    }
    urlMapper[challenges]=function(){
        goToChallengesPage();
    }
    urlMapper[search]=function(){
        var ret = goToSearchPage();
        if(!ret)pageNotFound();
    }
    urlMapper[user]=function(){
        showOthersProfile(urlPathStrips[1]);
    }
    urlMapper[question]=function(){
        openQuesPage(urlPathStrips[1])
    }
    urlMapper[addquestion]=function(){
        goToAddQuestionPage();
    }
    urlMapper[playlist]=function(){
        openPLInViewer(urlPathStrips[1]);
    }
    urlMapper[document]=function(){
        openDocInViewer(urlPathStrips[1]);
    }
    urlMapper[testSeries]=function(){
	var testSeriesId = urlPathStrips[1];
	if(testSeriesId){
        	goToTestSeriesPage(urlPathStrips[1],"HISTORY");
	}else{
        	pageNotFound();
	}
    } 
    urlMapper[test]=function(){
        openTestPage(urlPathStrips[1]);
    }
    urlMapper[testleaders]=function(){
        goToTestToppersPage("HISTORY",urlPathStrips[1]);
    }
    urlMapper[offlinetest]=function(){
        openOfflineTestPage(urlPathStrips[1]);
    }
    urlMapper[video]=function(){
        openDocInViewer(urlPathStrips[1]);
    }
    urlMapper[editdoc]=function(){
        goToMyContentPage(null,"documents");
        pushHistory(null,null,"/mycontent/documents",true);
    }
    urlMapper[curateplaylist]=function(){
        goToMyContentPage(null,"playlists");
        pushHistory(null,null,"/mycontent/playlists",true);
    }
    urlMapper[createtest]=function(){
        goToMyContentPage(null,"tests");
        pushHistory(null,null,"/mycontent/tests",true);
    }
    urlMapper[exam]=function(){
        openExamPage(urlPathStrips[1]);
    }
    urlMapper[subject]=function(){
        openSubjectPage(urlPathStrips[1]);
    }
    urlMapper[topic]=function(){
        openTopicPage(urlPathStrips[1]);
    }
    urlMapper[subtopic]=function(){
        openSubTopicPage(urlPathStrips[1]);
    }
    
    //organization
    function callOrgMapper(subPageName,orgId,subPageId){
    	try{
    		orgUrlMapper[subPageName](orgId,subPageId,urlPathStrips);
    	}catch(err){
    		putConsoleError("ERROR OPENING INSTITUTE SUB_PAGE = "+err);
    		goToMyInstitutePage(null,orgId);
    	}
    }
    urlMapper[organization]=function(){
        var source = "HISTORY";
        if(urlPathStrips.length == 2){
	    var orgId = urlPathStrips[1];
            try{
           	goToMyInstitutePage(source,orgId);
            }catch(err){
                goToMyInstitutePage(null,orgId);
            }
        }else if(urlPathStrips.length > 2){
            var subPageName = urlPathStrips[2].toUpperCase();
	    var orgId = urlPathStrips[1];
	    var subPageId = urlPathStrips[3];
	    if(history.lastPathName == location.pathname && onPageHistoryBack[subPageName]){
		var ret = onPageHistoryBack[subPageName](urlPathStrips);
		if(!ret){
			callOrgMapper(subPageName,orgId,subPageId);
		}
            }else if(orgUrlMapper[subPageName]){
		callOrgMapper(subPageName,orgId,subPageId);
            }else{
		 pageNotFound();
	    }
        }else{
		pageNotFound();
	}
    }
    var source="HISTORY";
    orgUrlMapper["RESULTANALYTICS"]=function(orgId){
    	goToInstResultAnalytics(source,orgId);
    }
    orgUrlMapper["LIBRARY"]=function(orgId,tabId){
	if($(".instLibraryHolder").get(0) && instLibrary.onBack){
		instLibrary.onBack(tabId);	
	}else{
        	goToInstLibrary(source,orgId,tabId);
	}
    }
    orgUrlMapper["MYSCHEDULE"]=function(orgId){
        goToInstMySchedule(source,orgId);
    }
    orgUrlMapper["CHALLENGES"]=function(orgId){
        goToInstChlg(source,orgId);
    }
    orgUrlMapper["MYORDERS"]=function(orgId){
        openMyOrders(source,orgId);
    }    
    orgUrlMapper["TESTLEADERS"]=function(orgId,testId){
        if(testId && testId.length>0){
        	goToInstTestToppersPage(source,testId);
        }else{ 
		goToMyInstitutePage(null,orgId);
	}
    }
    orgUrlMapper["TEST"]=function(orgId,testId){
        if(urlPathStrips.length > 0 && urlPathStrips.length == 6){
            var subName = urlPathStrips[urlPathStrips.length - 2];
            var subId = urlPathStrips[urlPathStrips.length -1];
            if(subName === "subjectivequestion"){
                openInstSubjectiveQuestionPage(source,testId,subId,orgId);
            }
        }
        else{
            var targetUserId=urlParams.targetUserId;
            var targetUserRole=urlParams.targetUserRole;
            if(testId && testId.length>0){
                goToInstTestPage(source,testId,targetUserId,targetUserRole);
            }else{
                goToMyInstitutePage(null,orgId);
        }
	}
    }
    orgUrlMapper["PRETEST"]=function(orgId,testId){
        if(testId && testId.length>0){
        	goToInstPreTestPage(source,testId,orgId);
        }else{ 
		goToMyInstitutePage(null,orgId); 
	}
    }
    orgUrlMapper["DISCUSSIONS"]=function(orgId){
        goToAllDiscussionsPage(source,orgId);
    }
    orgUrlMapper["ACTIVITIES"]=function(orgId){
        goToRecentActivityPage(source,orgId);
    }
    orgUrlMapper["DISCUSSION"]=function(orgId,dissId){
        goToDiscussionPage(source,dissId,orgId);
    }
    orgUrlMapper["PEOPLE"]=function(orgId,dissId){
        goToOrgMembersPage(source,orgId);
    }
    orgUrlMapper["PROFILE"]=function(orgId,userId){
	openInstProfile(source,userId,orgId);
    }
    orgUrlMapper[addquestion]=function(orgId){
	goToInstAddQuestionPage(source,userId,orgId);
    }
    orgUrlMapper["SCHEDULE"] = function(orgId){
        goToInstClassroomConnect(source,orgId);
    }
    orgUrlMapper["MESSAGES"]=function(orgId,subPageId,strip){
	if(!subPageId || subPageId == "inbox"){
       		openMessagesInbox(); 
	}else if(subPageId == "conversation"){
		openMessagesConversation(strip[4]);	
	}else{
       		openMessagesInbox(); 
	}
    }
    orgUrlMapper["ASSIGNMENT"]=function(orgId,subPageId,strip){
	if(subPageId && strip[4]){
		goToInstAsgnPage(source,strip[4],subPageId,orgId);
	}else{
       		pageNotFound(); 
	}
    }
    orgUrlMapper["FEED"]=function(orgId,feedId){
        openInstFeed(source,feedId,orgId);
    }
    orgUrlMapper["QUESTION"]=function(orgId,qId){
        openInstQuesPage(source,qId,orgId);
    }
    orgUrlMapper["VIDEO"]=function(orgId,vidId){
        openInstVideo(source,vidId,orgId);
    }
    orgUrlMapper["DOCUMENT"]=function(orgId,docId){
        openInstDoc(source,docId,orgId);
    }
    orgUrlMapper["NOTIFICATIONS"]=function(orgId){
        openAllNotifications(source,orgId);
    }
    orgUrlMapper["FILE"]=function(orgId,fileId){
        openInstFile(source,fileId,orgId);
    }
    orgUrlMapper["MODULE"]=function(orgId,moduleId){
        var sectionId = urlParams.sectionId;
        openInstModule(source,moduleId,orgId,sectionId);
    }
    orgUrlMapper["MYPROGRAMS"]=function(orgId){
        goToInstProgramsPage(source,orgId);
    }
    urlMapper[discussion]=function(){
        goToDiscussionPage(source,urlPathStrips[1]);
    } 
})(jQuery);


