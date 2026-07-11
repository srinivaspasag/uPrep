var institute = new function($){
	var parDivId = "#instituteHome";var subBrdId = "";var topicBrdId = "";var openedDiscuss;
	var params = {'tabType':"MY_INSTITUTE","year":new Date().getFullYear()};
	var batchParams = {};
	var memberSearchXHR;
	try{directDiscussion}catch(err){window[directDiscussion=false];}
	
	/* Institute Discussion Section Starts*/
	var askQuestion = function(){
		$(".instRTEHolder").find(".RTEArea").html("");
		var popup = getCommonPopupBody(725);
		var innerDiv = $($(".instAddQuesPopup").get(0)).clone(true).removeClass("nonner");
		popup.html(innerDiv.get(0));
	};
	var subChanged = function(e){
		var val = $(this).val();
	}
	var topicChanged = function(e){
		$(this).parent().find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
		$(this).addClass("instEachTrTopicSelect");
		//$("#instSearchQueries").append($($(".instSearchQuery").get(0)).clone(true).removeClass("nonner").find(".instTNm").text($(this).data("name")));
		$(".instSearchQuery").removeClass("nonner").find(".instTNm").text($(this).data("name"));
		var brdId = $(this).data("brdId");
		var brdIds=[];topicBrdId="";
		if(subBrdId){brdIds = [subBrdId];}
		var facet = false;
		if(brdId){
			brdIds.push(brdId);
			topicBrdId = brdId;
		}
		doSearch(brdIds,facet);
	};
	var subjectChanged = function(e){
		$(this).closest(".instSubFacetHold").siblings().find("span").removeClass("boldy");
		$(this).closest(".instSubFacetHold").find("span").addClass("boldy");
		$(".instSearchQuery").addClass("nonner");
		var brdId = $(this).val();var brdIds;
		var facetSubHolder = ".instTopicTree";
		topicBrdId = "";
		if(!brdId || brdId == 'all'){
			subBrdId = "";	
		}else{
			subBrdId = brdId;	
			brdIds = [brdId];
		}
		doSearch(brdIds,true,facetSubHolder);
	};
	var searchFiltered = function(e){
		$(this).parent().find(".simpleBlackActiveTab").removeClass("simpleBlackActiveTab");
		$(this).addClass("simpleBlackActiveTab");
		//doSearch();
	};
	var searchQueryChanged = function(e){
	};
	var topicCrossed = function(e){
		var length = $(this).closest(".instSearchQuery").addClass("nonner").siblings().length;
		$(".instTopicTree").find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
		if(length>0){
			$(this).closest(".instSearchQuery").remove();
		}
		var brdIds;
		if(subBrdId){brdIds = [subBrdId];}
		doSearch(brdIds,true);
	};
	this.qLoadCallBack = function(data,xhr,params){
		var facetDiv = $(".instIncommingFacets");
		if(params["facet"] == true){
			var facetHolder = params["facetHolder"];
			putFacets(facetDiv,facetHolder);
		}	
		$(facetDiv).remove();
		$("#instDiscusssionHolder").find(".unfollowEntity").removeClass("greenButton").text("Unfollow");
	};
	var doSearch = function(brdIds,fetchFacet,facetSubHolder){
		//alert("search");
		var mcWidget = $(parDivId).find(".instiQAndASection");
		if(!mcWidget.data("inited")){
			getDiscussions();
			return;
		}
        	//mcWidgetJSVar.reset["ALL"](mcWidget);
		closeDiscussion();
		//mcWidgetJSVar.remExtParams(mcWidget,['brdIds','facetHolder','resultType']);
		var params = {'facet':fetchFacet,'resultType':'all'};
		if(brdIds){params['brdIds']=brdIds;}
		if(facetSubHolder){params['facetHolder']=facetSubHolder;}
		mcWidgetJSVar.setExtParams(mcWidget,params,true);
		smallLoader($("#instDiscusssionHolder"));
		mcWidgetJSVar.loadmcContent(mcWidget,null,institute.qLoadCallBack);
	};
	var initMcWidget = function(mcWidget){
		var p = getInstParams();
		p["start"]=0;p["size"]=10;
		p["sortOrder"] = "DESC";
		p["facet"] = true;
		mcWidgetJSVar.init(mcWidget,"MY_INSTITUTE",[
                        [p,"/Institute/getDiscussions",10,20,[
                                {name:"Recent",mcSubTabParams:{"orderBy":"timeCreated","resultType":"all"}},
                                {name:"Popular",mcSubTabParams:{"orderBy":"mostPopular","resultType":"all"}},
                                {name:"Following",mcSubTabParams:{"resultType":"following","orderBy":"timeCreated"}}
				]
                        ]
                ]);	
		p["orderBy"] = "timeCreated";
		p["resultType"] = "all";
		return p;
	};
	var getDiscussions = function(){
		var divId = $("#instDiscusssionHolder");
		smallLoader(divId);
		$(".instTabHolder").find(".simpleBlackActiveTab").removeClass("simpleBlackActiveTab");
		$($(".instTabHolder").find(".simpleBlackTab").get(0)).addClass("simpleBlackActiveTab");
		$("#instSearchDiscussion input").val("");
                $(parDivId).removeClass("instiDiscOpened");
		var mcWidget = $(parDivId).find(".instiQAndASection");
        	if(mcWidget.length==0)return;
		var p = initMcWidget(mcWidget);
		p["orderBy"] = "timeCreated";
		p["resultType"] = "all";
		mcWidgetJSVar.loadmcContent(mcWidget,null,institute.qLoadCallBack);
		mcWidget.data('inited',true);
	};
	var putFacets = function(facetDiv,facetHolder){
                var facets;
		if(facetHolder){
			facets = $(facetDiv).find(facetHolder).html();
			$(parDivId).find(".instRightSec").find(facetHolder).html(facets);
		}else{
			facets = $(facetDiv).html();
			$(parDivId).find(".instRightSec").html(facets);
		}
		$(facetDiv).remove();
	};
	var getInstParams = function(){
		var p = cloneObject(params);
		$(p).removeProp('role');$(p).removeProp('start');$(p).removeProp('size');
		return p;
	};
	var postDiscussion = function(){
    		var quesParams = getInstParams();
    		//question,options,solution,answer
    		var QADiv=$(this).closest(".instAddQuesPopup");
    		var getRTE=vRTE.getRTEContent;
    		quesParams.title = getRTE(QADiv.find(".instRTEHolder").children(".RTEHolder"));
    		if(quesParams.title.length==0){
        		showError("Please enter Question");
        		return;
    		}
    		var tagsJson=returnAllTagsAdded(QADiv.find(".instAddQuesTags"));
    		if(tagsJson.subjectIds.length==0||tagsJson.topicIds.length==0){
        		showError("Please select atleast one Subject and Topic");
        		return;
		}
    		quesParams.brdIds=tagsJson.brdIds;
    		quesParams.targetIds=tagsJson.targetIds;
    		quesParams.desc = $(".instAddQuesDetails").find(".customTextarea").val();
		//putConsoleLogs(quesParams);
		$("#instPostQues").addClass("nonner");
    		showTopLoader();
    		$.post("/Institute/addDiscussions",quesParams,function(data){
        		hideTopLoader();
			$("#instPostQues").removeClass("nonner");
        		cancelAllCommonPopup();
			$($("#instDiscusssionHolder").find(".instEachDiscus").get(0)).before(data);
			var newInstDiss = $($("#instDiscusssionHolder").find(".instEachDiscus").get(0));
			MathJax.Hub.Queue(["Typeset",MathJax.Hub],newInstDiss.get(0));
    		});
	};
	var refreshPage = function(){
		var brdIds=[];var facet=true;var facetSubHolder;
		if(subBrdId){ 
			brdIds = [subBrdId];
			facetSubHolder = ".instTopicTree";
		}
		if(topicBrdId){
			brdIds.push(topicBrdId);
			facetSubHolder="";facet=false;	
		}
		doSearch(brdIds,facet,facetSubHolder);
	};
	this.commentAdded = function(){
		if(!openedDiscuss) return;
    		var commNoEl = openedDiscuss.find(".instCommentCount");
    		increaseCount(commNoEl);
	};
	var showCommentBox = function(commentInfo){
		commentInfo.addClass("instCommentSelected");
		$(".instCommentWidgetHolder").find(".commInputer").removeClass("nonner");
		commentInfo.data("opened",true);
	};
	var hideCommentBox = function(commentInfo){
		commentInfo.removeClass("instCommentSelected");
		$(".instCommentWidgetHolder").find(".commInputer").addClass("nonner");
		commentInfo.data("opened",false).find(".commInputArea").val("");
	};
	var commentClickedExt = function(e){
		if(e){
			var $this=$(e.currentTarget);
			var discuss = $this.closest(".instDiscusWrapper");
			if(discuss.data('target')=="EXTERNAL"){
				var p = cloneObject(params);p['dissId'] = discuss.data("dissId");
				pushInHistory("/discussion/"+p['dissId']); 
   				openMyPage("/Institute/openDiscussion",p,function(){
					var commentInfo = discuss.find(".instCommentInfo");
					setTimeout(function(){
					   var commentInfo = $(parDivId).find(".instEachDiscus")
						.find(".instCommentInfo");
					   commentInfo.addClass("instCommentSelected");
					   $(".instCommentWidgetHolder").find(".commInputer").removeClass("nonner");
					   commentInfo.data("opened",true);
					},2000);
				},discuss,null,$(this));
			}else{
			}
		}
	};
	var commentClicked = function(e){
		var discuss = openDiscussion(e,true);
		var commentInfo = discuss.find(".instCommentInfo");
		if(!commentInfo.data("opened")){
			showCommentBox(commentInfo);
		}else{
			hideCommentBox(commentInfo);
		}
		$(".instHideComments").removeClass("nonner");
	};
	var discussionHeadClicked = function(e){
		var discuss = openDiscussion(e);
		$(".instHideComments").removeClass("nonner");
	};
	this.openInstDiscussion = function(dissId){
		var divId = $("#instDiscusssionHolder");
		smallLoader(divId);
    		showTopLoader();
		$.get("/Institute/getMyDiscussion",{'dissId':dissId},function(data){
    			hideTopLoader();
			$(divId).html(data);
			openDiscussionDirect();	
		});
	};
	var openDiscussionDirect = function(){
		var e = {};
		e["currentTarget"] = $("#instDiscusssionHolder").find(".instEachDiscus").get(0);
		if(e.currentTarget){
			directDiscussion = true;
			openDiscussion(e,null,true);
		}
	};
	var openDiscussion = function(e,dontHideComInputer,donotPushHistory){
		var $this=$(e.currentTarget);
		var discuss = openedDiscuss = $this.closest(".instEachDiscus");
		discuss.siblings().addClass("nonner");
		discuss.find(".instDiscusWrapper").addClass("discussionOpened");
		discuss.find(".instDiscussDesc").addClass("big15");
    		discuss.css("border-bottom-width","0px").find(".instDiscussHead").addClass("discussionHeadOpened");
		$("#instDiscusssionHolder").addClass("commentOpened");
		$(parDivId).find(".instTabHolder").addClass("nonner")
		$(parDivId).find(".instPageFacets").addClass("nonner");
                $(parDivId).addClass("instiDiscOpened");
		if(!donotPushHistory){
			pushDiscussionHistory(discuss.data("discId")); 
		}                
		initComments(discuss,$this);
		if(!dontHideComInputer){
			hideCommentBox(discuss.find(".instCommentInfo"));
		}
		simQuesParams = cloneObject(params);
		simQuesParams["dissId"] = discuss.data("discId"); 
		simQuesParams["size"] = 4;
		discuss.find(".instDiscussHead").find(".RTEImageDiv").find('img').load(function(){
			$(this).closest(".instDiscussHead").useScrollBar(null,{vertical:false,horizontal:true});
		});
		MathJax.Hub.Queue(["Typeset",MathJax.Hub],discuss.get(0),function(){
			discuss.find(".instDiscussHead").useScrollBar(null,{vertical:false,horizontal:true});
		});
                
    		showTopLoader();
		$.get("/Institute/getSimilarQuestions",simQuesParams,function(data,stats){
    			hideTopLoader();
			var instRightSec = $(".instRightSec");
			instRightSec.find(".instSimilarQues").remove();
			instRightSec.find(".instRelatedTopics").remove();
			instRightSec.append(data);
			$(".instSimilarQues").find(".eachSimQues").each(function(){
				$this = $(this);
				if($this.height()>60){
					$this.addClass("eachSimQuesMaxHeight");
				}
			});
			$(".instSimilarQues").find("img").remove();
			MathJax.Hub.Queue(["Typeset",MathJax.Hub],$(".instSimilarQues").get(0));
			$(parDivId).find(".discussionOpened").on("click",".RTEImageDiv",rteImageOpen);
		});
		window.scrollTo(0,0);
		return discuss;
	};
	this.showPage = function(){
		if(openedDiscuss && !directDiscussion){
			var e = {"currentTarget":openedDiscuss};
			closeDiscussion(e);
		}else{
			/*closeDiscussion();
			getDiscussions();*/
			directDiscussion = false;
			goToMyInstitutePage("HISTORY");
		}
	};
	var backFromOpenedDiscussion = function(){
		if(openedDiscuss){
			var e = {"currentTarget":openedDiscuss};
			closeDiscussion(e);
			pushHistory(null , null, this.href);
			refreshPage();
			return false;
		}
	};
	var closeDiscussion = function(e){
		var $this=e?$(e.currentTarget):$(this);
		var discuss = $this.closest(".instEachDiscus");openedDiscuss=null;
		if(discuss.get(0)){
			discuss.siblings().removeClass("nonner");
			discuss.find(".instDiscusWrapper").removeClass("discussionOpened");
			discuss.find(".instDiscussDesc").removeClass("big15");
    			discuss.css("border-bottom-width","1px").find(".instDiscussHead").removeClass("discussionHeadOpened");
			discuss.find(".instCommentInfo").removeClass("instCommentSelected").data("opened",false);
			discuss.find(".instDiscussHead").unuseScrollBar();
			window.scrollTo(0,discuss.position()["top"]);
		}
		$("#instDiscusssionHolder").removeClass("commentOpened");
		$(".instRightSec").find(".instSimilarQues").remove();
		$(".instRightSec").find(".instRelatedTopics").remove();
                //
    		var commHolder=$(parDivId).find(".instCommentWidgetHolder");
    		commHolder.addClass('nonner').html("");
		$(parDivId).find(".instTabHolder").removeClass("nonner");
		$(parDivId).find(".instPageFacets").removeClass("nonner");
		if($(parDivId).hasClass("instiDiscOpened")){
                	$(parDivId).removeClass("instiDiscOpened");
			pushInHistory("");
		}
		$(".instCommentWidgetHolder").find(".commInputer").removeClass("nonner");
		$(parDivId).find(".discussionOpened").off("click",".RTEImageDiv",rteImageOpen);
	};
	var initComments = function(discuss,$this){
    		var commHolder=$(parDivId).find(".instCommentWidgetHolder");
    		commHolder.removeClass('nonner');
    		var discId=discuss.data("discId");
		commHolder.html("");
        	var LMData={urlStr:"/widgets/commItems",size:10,start:0,rootType:"DISCUSSION",rootId:discId,baseId:discId,baseType:"DISCUSSION",orderBy:"voteUp",target:'MY_INSTITUTE'};
        	var allParams={};
        	allParams.rootId=discId;allParams.rootType="DISCUSSION";
        	allParams.baseId=discId;allParams.baseType="DISCUSSION";
        	allParams.callBack="INST_DISCUSSION";
        	allParams.targetPage="MY_INSTITUTE";
                allParams.placeHolder="Add Answer here";
		smallLoader(commHolder);
        	initCommWidget(allParams,LMData,commHolder);
	};
	function pushDiscussionHistory(dissId){
		pushInHistory("discussion/"+dissId);
	};
	var seeMoreMembers = function(){
		var role = $(this).data('role');
		var total = $(this).data('total');
		var name = $(this).data('name');
		var p = cloneObject(params);p.role = role;
    		loadPeoplePopup("/Institute/moreMembers",name,total,p);
	};
	/* Institute Discussion Section ENDS*/

	var openCenterBody = function(){
		var parDiv = $(parDivId);
		parDiv.find(".instMiddleSec").addClass("nonner");
		parDiv.find(".instRightSec").addClass("nonner");
		smallLoader(parDiv.find(".instCenterBody").removeClass("nonner"));
	};
	var closeCenterBody = function(){
		var parDiv = $(parDivId);
		parDiv.find(".instMiddleSec").removeClass("nonner");
		parDiv.find(".instRightSec").removeClass("nonner");
		parDiv.find(".instCenterBody").addClass("nonner");
	};
	var openMembersPage = function(e){
		$(this).siblings().removeClass("instCountSelected");
		$(this).addClass("instCountSelected");
		var role = $(this).data('role');
		var name = $(this).data('name');
		var total = $(this).data('total');
		var p = cloneObject(params);
		p.role = role;p.totalMembers = total;p.name = name;
		openCenterBody();
    		showTopLoader();
		$.get("/Institute/membersPage",p,function(data){
    			hideTopLoader();
			$(parDivId).find(".instCenterBody").html(data);
			$(".instMemberSearch").find("input").val("");
			$(parDivId).find(".instCenterBody").find(".instMembersInfo").data("params",p);
			vSelectFns.init();
			fetchMembers(true);
		});
	};
	var fetchCenters = function(){
			
	};
	fetchMoreMembers = function(){
		smallLoader($(this));
		fetchMembers();
	};
	membersBySearchText = function(e){
		fetchMembers(true);
	}
	this.getSearchedMembers = function(){
		fetchMembers(true);
	};
	var fetchMembers = function(freshLoad){
		var membersDiv = $(parDivId).find(".instCenterBody").find(".instMembersInfo");
		var p = membersDiv.data("params");
		if(p.role == "PROFESSOR"){
			p["departmentId"] = $(".instMembersByDept").data("value");
			var courseId = membersDiv.find(".instMembersBySub").data("value");
			if(courseId){ p["courseId"] = courseId; }else{ $(p).removeProp("courseId");}
			p["programmeId"] = $(".instMembersByDept").data("progId");
		}else if(p.role == "MEMBER"){
			p["programmeId"] = $(".instMembersByDept").data("progId");
		}
		var center = membersDiv.find(".instMembersByCenter").data("value");
		var section = membersDiv.find(".instMembersBySec").data("value");
		var query = $(".instMemberSearch").find("input").val();
		if(center){ p["center"] = center; }else{ $(p).removeProp("center");}	
		if(section){ p["section"] = section; }else{ $(p).removeProp("section");}
		if(query){ p["query"] = query; }else{$(p).removeProp("query");}
		p["size"] = 8;
		p["searchAll"] = true;
		var membersHolder = $(".instMembersHolder"); 
		if(freshLoad){ 
			p["start"]=0;
			smallLoader(membersHolder);
		}else{ 
			p["start"]+=8;
		}
		membersDiv.data("params",p);
		if(memberSearchXHR){ memberSearchXHR.abort();}
    		showTopLoader();
		memberSearchXHR = $.get("/Institute/searchMembers",p,function(data){
    			hideTopLoader();
			if(freshLoad){
				membersHolder.html(data);
			}else{
				membersHolder.find(".instLoadMoreMembers").remove();
				membersHolder.append(data);
			}
			memberSearchXHR = undefined;
		});
	};
	var openInstSubPage = function(url,history,params,callBack){
		showTopLoader();
		params["newPageOpen"] = pushInHistory(history);
		$.get(url,params,function(data){
			hideTopLoader();
			$(parDivId).html(data);
			if(callBack){
				try{
					callBack(data);
				}catch(err){
					putConsoleError(err);
				}
			}
		});
	};	
	var openResultAnalytics = function(){
		/*showTopLoader();
		pushInHistory("resultanalytics");
		$.get("/Institute/testAnalytics",params,function(data){
			hideTopLoader();
			$(parDivId).html(data);		
		});*/
		openInstSubPage("/Institute/testAnalytics","resultanalytics",params);
	};
	var openMySchedule = function(e){
		/*pushInHistory("myschedule");
		$.get("/Institute/myschedule",params,function(data){
			hideTopLoader();
			$(parDivId).html(data);		
		});*/
		openInstSubPage("/Institute/myschedule","myschedule",params);
		if(e) e.preventDefault();
	};
	var openLibrary = function(){
		/*showTopLoader();
		pushInHistory("library");
		$.get("/Institute/library",params,function(data){
			hideTopLoader();
			$(parDivId).html(data);		
		});*/
		openInstSubPage("/Institute/library","library",params);
	};
	this.openTestPage = function(testId,extraParams){
		var url = "test/"+testId;
		var p = cloneObject(params);p["testId"] = testId;
		if(extraParams){
			url = url+"?";
			for(pt in extraParams){
				url+=pt+"="+extraParams[pt]+"&";	
			}
			p = $.extend(true,p,extraParams);	
		}
   		p["noInstHeader"] = "true";
		/*pushInHistory(url);
		$.get("/Tests/getOrgTest",p,function(data){
			hideTopLoader();
			$(parDivId).html(data);
			window.scrollTo(0,0);	
		});*/
		openInstSubPage("/Tests/getOrgTest",url,p,function(){
			window.scrollTo(0,0);	
		});
	};
	this.openTestLeaders = function(testId){
		/*showTopLoader();
   		pushInHistory("testleaders/"+testId);
		var p = cloneObject(params);p["testId"] = testId;
		$.get("/Institute/getTestLeaders",p,function(data){
			hideTopLoader();
			$(parDivId).html(data);		
		});*/
		var p = cloneObject(params);p["testId"] = testId;
		openInstSubPage("/Institute/getTestLeaders","testleaders/"+testId,p);
	};
	var showQA = function(e){
		instiOpenAgain();
		subBrdId = topicBrdId = undefined;
		refreshPage();
	};
	var headerTabClick = function(){
		$("#instPageNavigator").find(".clInstTab").removeClass("clInstTab");
		$(this).addClass("clInstTab");
	};
	var rteImageOpen = function(e){
		showDiagramPreview($(this).html(),"Image Preview");
		if(e) e.preventDefault();
		return false;
	};
	var pushInHistory = function(append){
		var orgId = $("#myInstitutePage").data("orgId");
		return pushInstHistory(orgId,append);
	};
	var getBatchParams = function(){
		var data = $(".instSelectBatch").data();
		batchParams = {"programmeId":data["value"],"centers":[{"name":data["center"],"sections":[data["section"]]}]};
		return batchParams;
	};
	this.initWidgets = function(){
		getBatchParams();
		getScheduleWidget();
		getMembersCount();
	};
	this.init = function(){
		putConsoleLogs($(".instiActivitySection").html());
		params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		if($(parDivId).data("inited")) return false;
		
		assignRTEs($(".instRTEHolder"));
		$(".instRightSec").on("click",".instEachTrTopic",topicChanged);
		$(".instRightSec").on("click",".instSubjectFacet",subjectChanged);
		$(".instTabHolder").on("click",".simpleBlackTab",searchFiltered);
		/*$("#instSearchDiscussion input").keydown(searchQueryChanged);*/
		$("#instSearchQueries").on("click",".crossIT",topicCrossed);
		$("#instPostQues").on("click",postDiscussion);
		$("#instDiscusssionHolder").on("click",".instEachDiscus .instDiscussHead",discussionHeadClicked)
				.on("click",".instEachDiscus .instCommentInfo",commentClicked);
		$(parDivId).on("click",".instMoreMembers",seeMoreMembers)
                	.on("click",".instAskQuesBtn",askQuestion)
                        .on("click",".instActivityTab",instiActivityTab)
                	.on("click",".instLoadMoreMembers",fetchMoreMembers)
			.on("click",".instHideComments",backFromOpenedDiscussion)
			.on("click",".instDiscussionTab",showQA)
                	.on("keyup",".instMemberSearch input",membersBySearchText)
			.on("click",".instCommentInfo",commentClickedExt)
                	.on("click",".instOpenMySchedule",openMySchedule)
			.on("click",".instCommentWidgetHolder .RTEImageDiv",rteImageOpen);
			
		$("#instPageNavigator").on("click",".instNavTab",headerTabClick);
		$("#instituteHeader")
			.on("click",".instOpenMembersPage",openMembersPage)
                	.on("click",".openInstPage,.instOpenAgain",goToMyInstitutePage)
                	.on("click",".instOpenAnalytics",openResultAnalytics)
                	.on("click",".instOpenMySchedule",openMySchedule)
                	.on("click",".instOpenLibraryPage",openLibrary)
			.on("click",".openInstiTestPage",instTestClicked)
			.on("click",".openInstiTestToppers",instTestToppersClicked);
		$(parDivId).data("inited",true);
		
		if(directDiscussion){
			initMcWidget($(parDivId).find(".instMiddleSec"));
			openDiscussionDirect();
		}else{
			//getDiscussions();
			instiActivityTab();
		}
		vSelectFns.init();
		setTimeout(function(){institute.initWidgets();},10);
	};
	setTimeout(function(){institute.init();},10);
       
	var getScheduleWidget = function(){
		var p = {};
		p["fromTime"] = (new Date()).setHours(0,0,0,0);
		p["tillTime"] = (new Date()).setHours(24*3-1,59,59,999);
		$.extend(true,p,batchParams);
		$.get("/Institute/scheduleWidget",p,function(data){
			$("#instScheduleWidget").html(data);
		});
	};
	var getMembersCount = function(){
		var data = $(".instSelectBatch").data();
		batchParams = {"programmeId":data["value"],"centers":[{"name":data["center"],"sections":[data["section"]]}]};
		var p = {
			"programmeId":data["value"],
			"center":data["center"],	
			"section":data["section"],
			"size":1,
			"year":new Date().getFullYear(),
			"searchAll":true
		};
		p["role"] = "MEMBER";
		$.get("/Institute/getMyBatchMembers",p,function(data){
			$($(".instStdCount").children()[0]).text(data.result.totalHits);
			p["role"] = "PROFESSOR";
			$.get("/Institute/getMyBatchMembers",p,function(data){
				$($(".instTeachersCount").children()[0]).text(data.result.totalHits);
			});	
		});	
	} 
        //by ajith
        var instiOpenAgain=function(){
            var qaSec=$(parDivId).find(".instiQAndASection");
            qaSec.removeClass("nonner");
            qaSec.siblings(".instiActivitySection").addClass("nonner")
            $(".instDiscussionTab").addClass("active").siblings().removeClass("active");
        }
        var instiActivityTab=function(){
            var actSec=$(parDivId).find(".instiActivitySection");
	    smallLoader(actSec);
            actSec.removeClass("nonner");
            $(parDivId).find(".instiQAndASection").addClass("nonner");            
            $(".instActivityTab").addClass("active").siblings().removeClass("active");
            showTopLoader();
            var urlStr="/application/activityFeeds";
            var entityType="ORGANIZATION",id=params.parent.id;
            actSec.data({"urlStr":urlStr,size:15,allParams:{feedType:"OLD",count:15,id:id,entityType:entityType}});
            $.get(urlStr,{feedType:"NEW",id:id,entityType:entityType,count:10},function(data){
                hideTopLoader();
                actSec.html(data);
		MathJax.Hub.Queue(["Typeset",MathJax.Hub],actSec.get(0));
            });
        }
}(jQuery);

postCommCallBackFn["INST_DISCUSSION"]=function(commWidget){
	institute.commentAdded();
};
function instUpdateSectionTag(sections){
	uiCloneHelper.set(".instMembersBySec .vSelectList",".vSelectEach",0);var index=0;
	for(i in sections){
		var sp = uiCloneHelper.create(++index);
		var sec = sections[i];
		$(sp).text(sec);
		$(sp).data("value",sec);
		$(sp).attr("title","Section : "+sec);
		$(sp).attr("name","SELECT_SECTION");
	}
	uiCloneHelper.removeFrom(index+1);
	$(".instMembersBySec").resetVSelectTag({'newVal':{'text':'All Sections'}});
}
function instMembersByCenter(value,target,targetVal){
	$(target).closest(".vselect").attr("title","Center : "+value);
	var sections = $(target).data("sections");
	instUpdateSectionTag(sections);	
	institute.getSearchedMembers();
}
function instMembersBySubject(value,target,targetVal){
	$(target).closest(".vselect").attr("title","Subject : "+value);
	institute.getSearchedMembers();
}
function instMembersByDept(value,target,targetVal){
	$(target).closest(".vselect").attr("title","Programme : "+value);
	var centerTag = $(".instMembersByCenter").resetVSelectTag(
		{'newVal':{'text':'All Centers'},
		 'optionsList':"<span class='vSelectEach' data-value=''>All Centers</span>"}
		);
	var sectionTag = $(".instMembersBySec").resetVSelectTag(
		{'newVal':{'text':'All Sections'},
		 'optionsList':"<span class='vSelectEach' data-value=''>All Sections</span>"
		});
	var progId = $(target).data("progId");
	$(".instMembersByDept").data("progId",progId);
	institute.getSearchedMembers();
	
	$.get("/Institute/getCentersByProgramme",{"programmeId":progId},function(data){
			$(".instMembersByCenter").resetVSelectTag(
				{'newVal':{'text':'All Centers'},
		 		'optionsList':data}
			);
		});
}
function instMembersBySection(value,target,targetVal){
	$(target).closest(".vselect").attr("title","Section : "+value);
	institute.getSearchedMembers();
}
function instBatchChanged(value,target,targetValue){
    $(target).closest(".vselect").attr("title","Batch : "+value);
    $(".instSelectBatchText").text(value);
    var courses = $(target).data("courses");
    if(courses && courses.length>0){
	$(".instMyCourses").removeClass("nonner");
	uiCloneHelper.set(".instMyCourses",".instEachCourse",0);
	var index=0;
	uiCloneHelper.removeFrom(1);
	for(i in courses){
		var sp = uiCloneHelper.create(index++);
		var course = courses[i];
		$(sp).text(course.name);
		$(sp).data("id",course._id);
	}
     }else{
	$(".instMyCourses").addClass("nonner");
     }
     setTimeout(function(){institute.initWidgets();},10);
}

//result anlaytics
function instTestClicked(){
    var testId = $(this).data("testId");
    var testGroupId = $(this).data("testGroup");
    var targetUserId = $(this).data("targetUserId");
    var otherParams;
	if(targetUserId){
		otherParams = {"targetUserId":targetUserId};
	}
	if(testGroupId){
    		institute.openTestPage(testGroupId,otherParams);
	}else if(testId){
		institute.openTestPage(testId,otherParams);
	}else{
		showError("Can not open the selected test");
	}
};
function instTestToppersClicked(){
    var testId = $(this).data("testId");
    var testGroupId = $(this).data("testGroup");
	if(testGroupId){
		institute.openTestLeaders(testGroupId);
	}else if(testId){
		institute.openTestLeaders(testId);
	}else{
		showError("Can not open the selected test");
	}
};
