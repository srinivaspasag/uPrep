var instLibrary = new function(){
	var parDivId = "#instituteHome";
	var subBrdId = "";var topicBrdId = "";var subTopicBrdId="";
	var topicsData;
	var selTopic; var selSubTopic;var selSubSubTopic;
	var params = {'tabType':"MY_INSTITUTE",'year':new Date().getFullYear()};
	var instParams = params;
	var slideHolderDiv = ".instLibSlideHolder";
	var slideTime = 650;
	var hideTopicInfoTimeout;
	var dirArr = {"+":{left:'-=1002'},"-":{left:'+=1002'}};
	var animInProgress = false;var curSlideIndex = 0;
	var memberCenterInfo;
	var nextSlide = function(direction,postNavigation){
		if(animInProgress) return;
		var d = $(slideHolderDiv);
		if(!direction) direction = "+";
		animInProgress = true;
		d.animate(dirArr[direction],slideTime,function(){
			animInProgress = false;
			if(postNavigation){
				try{postNavigation(direction);}catch(err){}
			}
		});
	};
	var prevSlide = function(){
		nextSlide("-",postBarAnimation);
		delLastFromNavBar();
	};
	var subjectSelected = function(e){
		var p = cloneObject(params);
		var data = getDistinctData();
		p['programmeId'] = data["progId"];
		if(data["section"]){
			p['section'] = data["section"];
		}
		if(data["center"]){
			p['center'] = data["center"];
		}
		var subName;
		if(e){
			p['courseId'] = $(this).data("id");
			p['departmentId'] = $(this).data("deptId");
			subBrdId = $(this).data("brdId");
			subName = $(this).attr("title");
		}
		var div = $(".instLibSlideHolder").find(".instLibSlide").get(1);
		showTopLoader();
		$.get("/Institute/getLibTopics",p,function(data){
			$(div).html(data);
			hideTopLoader();
			nextSlide("+",postBarAnimation);
			try{
				topicsData = programmeTopics;programmeTopics=null;
			}catch(err){
				topicsData = [];
			}
			$(".instLibSelSubName").text(toCamelCase(subName));
		});
		instLibrary.myContents.refreshUi();
		fetchContent();
	};
	var fetchContent = function(){
		var brdIds = [];
		if(subTopicBrdId){
			brdIds.push(subTopicBrdId);
		}
		else if(topicBrdId){
			brdIds.push(topicBrdId);
		}
		else if(subBrdId){
			brdIds.push(subBrdId);
		}
		var data = getDistinctData();
		var shareType = {'id':data["progId"],'type':'PROGRAMME'};
		if(memberCenterInfo){
			if(memberCenterInfo.length==undefined){
				memberCenterInfo = new Array(memberCenterInfo);
			}
			shareType['centers'] = memberCenterInfo;
			/*var centers = cloneObject(memberCenterInfo);
			$(centers).each(function(){
				$(this).removeProp("sections");
			});*/
		}
		instLibrary.myContents.fetchDataOnChange(brdIds,shareType);
		$(".instLibViewTypes").removeClass("nonner");
	};
	var topicSelected = function(e){
		var topicIndex = $(this).data("index");
		topicBrdId = $(this).data("brdId");
		var topicName = $(this).attr("title");
		var topicData = selTopic = topicsData[topicIndex];
		var subTopics = topicData.subTopics;
	
		$(".instLibSelTopicName").text(toCamelCase(topicName));
		appendToNavBar(".instLibTopicName",topicName);
		$(parDivId).find(".instLibSubTopicTxt").removeClass("selectedInstLibTxt");
		drawTopicsInfoSec(topicData);
		$("#instLibSubTopicsInfoH").html($("#instLibTopicsInfoH").html());

		fetchContent();
		
		if(!subTopics || !subTopics.length || subTopics.length<1){
			$(".instLibSubTopicH1").addClass("nonner");
			$(".instLibNoSubTopics").removeClass("nonner");
			$(".nextPrevInstLibST").addClass("nonner");
			nextSlide("+",postBarAnimation);
			return;
		}else{
			$(".instLibNoSubTopics").addClass("nonner");
			$(".instLibSubTopicH1").removeClass("nonner");
		}
		var total = subTopics.length;
		var hTotal = Math.ceil(total/4);

		uiCloneHelper.set(parDivId+" .instLibrarySubTopics .instLibSubTopicH1",".instLibSubTopicH2",0);
		var hIndex;
		for(hIndex=0;hIndex<hTotal;hIndex++){
			var hDiv = uiCloneHelper.create(hIndex);
			$(hDiv).data("index",hIndex).css("left",(hIndex*$(hDiv).width())+"px");
			var st = $(hDiv).find(".instLibEachSubTopic").get(0);
			$(st).siblings().remove();
			var stIndex;var strt=hIndex*4;
			fillSubTopicData(st,subTopics[strt],strt);
			for(stIndex=strt+1;stIndex<total && stIndex<strt+4;stIndex++){
				var newSt = $(st).clone();
				$(hDiv).append(newSt);
				fillSubTopicData(newSt,subTopics[stIndex],stIndex);
			}
		}
		nextPrevReset(".instLibSubTopicH1",hIndex);
		if(hIndex>2){
			$(".nextPrevInstLibST").removeClass("nonner");
			$(".prevInstLibST").addClass("disPrevInstLib");
			$(".nextInstLibST").removeClass("disPrevInstLib");
		}else{
			$(".nextPrevInstLibST").addClass("nonner");
		}
		uiCloneHelper.removeFrom(hIndex);
		nextSlide("+",postBarAnimation);
	};
	var drawTopicsInfoSec = function(topicData){
		//console.log(topicData);
		var w = topicData.weightages;
		if(!w || w.length<1){
			$(".instTopicBrdWeightages").addClass("nonner");	
		}else{
			$(".instTopicBrdWeightages").removeClass("nonner");
			uiCloneHelper.set(parDivId+" .instTopicBrdWeightHold",".instTopicBrdWeight",0);var n=0;
			for(n=0;n<w.length;n++){
				var sp = uiCloneHelper.create(n);
				var wtg = w[n];
				$(sp).text(wtg.brdName+" - "+wtg.weightage+"%");
			}
			uiCloneHelper.removeFrom(n);
		}	
	};
	var topicMouseIn = function(){
		if(hideTopicInfoTimeout) clearTimeout(hideTopicInfoTimeout);
		$("#instLibTopicsInfoH").fadeIn(0);
		var topicIndex = $(this).data("index");
		var topicData = topicsData[topicIndex];
		drawTopicsInfoSec(topicData);
	};
	var topicMouseOut = function(){
		if(hideTopicInfoTimeout) clearTimeout(hideTopicInfoTimeout);
		hideTopicInfoTimeout = setTimeout(function(){
			$("#instLibTopicsInfoH").fadeOut(150);
		},200);
	};
	var fillSubTopicData = function(st,data,index){
		var name = "";
		if(data.name && typeof data.name == "string"){ name = data.name.toLowerCase();}
		$(st).find(".instLibSubTopicTxt")
			.data("brdId",data.brdId).data("index",index)
			.attr("title",name)
			.text(name);
	};
	var postBarAnimation = function(DIR){
		var showContent = true;
		if($(slideHolderDiv).position().left > -100){
			var width = $(".instLibPrevSlide").width();
			$(".instLibPrevSlide").animate({"left":"-"+width},100,function(){ $(this).addClass("nonner")});
			showContent = false;
		}else{
			$(".instLibPrevSlide").removeClass("nonner").animate({"left":"0"},100);	
		}
		if(DIR=="+"){
			curSlideIndex++;	
		}else{
			curSlideIndex--;
			try{onBackNav[curSlideIndex](curSlideIndex);}catch(err){}
		}
		showOrHideContents(showContent);
	};
	var onBackNav = [
		function(){//BACK FROM TOPICS TO SUBJECT
		},
		function(){//BACK FROM SUBTOPICS TO TOPIC
			hideSubSubTopics();
			$(".instLibTopicName").text("");
			$(".instLibSubTopicName").text("");
			topicBrdId = subTopicBrdId = undefined;
			fetchContent();
		}
	];
	var showOrHideContents = function(show){
		if(show){
			$(".instLibCenterInitial")
				.animate({"height":"0px"},slideTime,function(){
					$(this).addClass("nonner");
				});
			$(".instLibCenter").find(".pageHolderM").removeClass("nonner")
		}else{
			var d = $(".instLibCenter").find(".pageHolderM");
			$(".instLibCenterInitial").removeClass("nonner")
				.animate({"height":d.height()+"px"},slideTime,function(){
					d.addClass("nonner");
				});
		}
	};
	var appendToNavBar = function(hName,text){
		text = toCamelCase(text);
		if(!hName){
			$("#instLibNavBar").find(".otherVars").append(" / "+text);
		}else{
			$("#instLibNavBar").find(hName).text(" / "+text);
		}
	};
	var delLastFromNavBar = function(){
		var txt = $("#instLibNavBar").find(".otherVars").text();
		txt = txt.split("/");
		txt.pop();
		if(txt.length<=0){
			$("#instLibNavBar").find(".otherVars").text("");	
		}
		txt = txt.join(" / ");
		$("#instLibNavBar").find(".otherVars").text(txt);	
	};
	var prevTopics = function(){
		nextPrevHandler("+",".instLibTopicH1",".instLibTopicH2",".prevInstLibT",".nextInstLibT");
	};
	var nextTopics = function(){
		nextPrevHandler("-",".instLibTopicH1",".instLibTopicH2",".prevInstLibT",".nextInstLibT");	
	};
	var prevSubTopics = function(){
		nextPrevHandler("+",".instLibSubTopicH1",".instLibSubTopicH2",".prevInstLibST",".nextInstLibST");
	};
	var nextSubTopics = function(){
		nextPrevHandler("-",".instLibSubTopicH1",".instLibSubTopicH2",".prevInstLibST",".nextInstLibST");	
	};
	var nextPrevReset = function(holder,total){
		$holder = $(holder);
		if(total == undefined) total = $holder.data("total");
		$holder.data("total",total).data("cur",0).css("left","0px");
		return total;
	};
	var nextPrevHandler = function(direction,h1,h2,p,n){
		if(animInProgress) return;
		var h1 = $(h1);var h2 = $(h2);
		var width = h2.width();
		var total = h1.data("total");var cur = h1.data("cur");
		if( (direction=="-" && cur+2<total) || ( direction=="+" && cur>0) ){
			animInProgress = true;
			h1.animate({"left":direction+"="+width+"px"},200,function(){animInProgress=false;});
			if(direction=="-"){++cur;}else{--cur;};
			h1.data("cur",cur);
			if(cur<=0){
				$(p).addClass("disPrevInstLib");
				$(n).removeClass("disPrevInstLib");
			}else if(cur+2>=total){
				$(p).removeClass("disPrevInstLib");
				$(n).addClass("disPrevInstLib");
			}
		}
	};
	var subTopicSelected = function(){
		$(parDivId).find(".instLibSubTopicTxt").removeClass("selectedInstLibTxt");
		$(this).addClass("selectedInstLibTxt");
		var index = $(this).data("index");
		subTopicBrdId = $(this).data("brdId");
		fetchContent();
		selSubTopic = selTopic.subTopics[index];
		appendToNavBar(".instLibSubTopicName",selSubTopic.name);
		drawSubSubTopics();
	};
	var drawSubSubTopics = function(){
		uiCloneHelper.set(parDivId+" .instLibSubSubTopics",".instLibFacetSubSubTopic",0);
		var index;
		subSubTopics = selSubTopic.subSubTopics;
		if(!selSubTopic.subSubTopics || selSubTopic.subSubTopics.length<1){
			hideSubSubTopics();
			return;
		}
		$(".instLibSubSubTopics").removeClass("nonner");
		for(index=0;index<subSubTopics.length;index++){
			var hDiv = uiCloneHelper.create(index);
			var ssTopic = subSubTopics[index];
			$(hDiv).data("index",index).find(".SSTName").text(toCamelCase(ssTopic));
		}
		uiCloneHelper.removeFrom(index);
	};
	var hideSubSubTopics = function(){
		$(".instLibSubSubTopics").addClass("nonner");
	};
	var navBarSubClicked = function(){
		var d = $(slideHolderDiv);
		var hIndex = nextPrevReset(".instLibTopicH1");
		if(hIndex>2){
			$(".nextPrevInstLibT").removeClass("nonner");
			$(".prevInstLibT").addClass("disPrevInstLib");
			$(".nextInstLibT").removeClass("disPrevInstLib");
		}else{
			$(".nextPrevInstLibT").addClass("nonner");
		}
		d.animate({"left":"-1002px"},slideTime,function(){
			curSlideIndex=1;	
			hideSubSubTopics();
			$(".instLibTopicName").text("");
			$(".instLibSubTopicName").text("");
			topicBrdId = subTopicBrdId = undefined;
			fetchContent();
		});
	};
	var navBarTopicClicked = function(){
		hideSubSubTopics();
		$(".instLibSubTopicName").text("");
		$(".instLibEachSubTopic").find(".selectedInstLibTxt").removeClass("selectedInstLibTxt");
		var hIndex = nextPrevReset(".instLibSubTopicH1");
		if(hIndex>2){
			$(".nextPrevInstLibST").removeClass("nonner");
			$(".prevInstLibST").addClass("disPrevInstLib");
			$(".nextInstLibST").removeClass("disPrevInstLib");
		}else{
			$(".nextPrevInstLibST").addClass("nonner");
		}
		subTopicBrdId = undefined;
		fetchContent();
	};
	var navBarSubTopicClicked = function(){
		hideSubSubTopics();
	};
	var registerEvents = function(){
		$(parDivId).on("click",".instLibEachSub",subjectSelected)
			.on("click",".instLibEachTopic .instLibTopicTxt",topicSelected)
			.on("mouseenter",".instLibEachTopic .instLibTopicTxt",topicMouseIn)
			.on("mouseout",".instLibEachTopic .instLibTopicTxt",topicMouseOut)
			.on("click",".instLibEachSubTopic .instLibSubTopicTxt",subTopicSelected)
		
			.on("click",".instLibPrevSlide",prevSlide)
		
			.on("click",".prevInstLibT",prevTopics)
			.on("click",".nextInstLibT",nextTopics)
			.on("click",".prevInstLibST",prevSubTopics)
			.on("click",".nextInstLibST",nextSubTopics);
		$("#instLibNavBar").on("click",".instLibSelSubName",navBarSubClicked)
			.on("click",".instLibTopicName",navBarTopicClicked)
			.on("click",".instLibSubTopicName",navBarSubTopicClicked);
	};
	var getDistinctData = function(){
		var data = $(".instLibarySelBatch").data();
		if(!data){
			return {"progId":"","center":"","section":""};
		}
		return {"progId":data["value"],"center":data["center"],"section":data["section"]};
	};
	var getProgrammeInfo = function(){
		var p = cloneObject(params);
		var data = getDistinctData();
		p['programmeId'] = data["progId"];
		var div = $(".instLibSlideHolder").find(".instLibSlide").get(0);
		showTopLoader();
		smallLoader(div);
		$.get("/Institute/getLibProgrammeInfo",p,function(data){
			$(div).html(data);
			hideTopLoader();
		});
	};
	this.resetProgramme = function(){
		var d = $(slideHolderDiv);
		d.animate({"left":"0px"},slideTime,function(){
			try{postBarAnimation()}catch(err){}
		});
		getProgrammeInfo();
		hideSubSubTopics();
	};
	this.init = function(){
		institute.init();
		params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		try{
			memberCenterInfo = centerData;
		}catch(err){}
		registerEvents();
		getProgrammeInfo();
		this.myContents.init();
		vSelectFns.init();
	};
	this.myContents = new function(){
	   var params={"orderBy":'timeCreated',"shareBy":"ALL","query":"","resultType":"ALL","sortOrder":"DESC","start":0,'tabType':"MY_INSTITUTE",'year':new Date().getFullYear(),'parent':{'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')}};
	   var url={"questions":"/Institute/getLibQuestions","playlists":"/Institute/getLibPlaylists","documents":"/Institute/getLibDocuments","tests":"/Institute/getLibTests","videos":"/Institute/getLibVideos"};
	   var divUrl={"instLibQuestionslist":"/Institute/getLibQuestions","instLibPlaylist":"/Institute/getLibPlaylists","instLibDocumentslist":"/Institute/getLibDocuments","instLibTestslist":"/Institute/getLibTests","instLibVideoslist":"/Institute/getLibVideos"};
	   this.fetchSize=3;
	   var currentPaginatedDiv = "";
	   var timerObj;
	   var filterDivs = {"TESTS":".FilterForAttemptType","QUESTIONS":".FilterForAttemptType"};
	   var onTabChange = function(tabId){
			if(!tabId) return false;
			tabId = tabId.toUpperCase();
			if(filterDivs[tabId]){
				$(".instLibContentsControl").find(filterDivs[tabId]).removeClass("nonner");
				resetFilterDiv();
			}else{
				$(".instLibContentsControl").find(".FilterForAttemptType").addClass("nonner");
			}
	   };
	   var resetFilterDiv=function(){
		$(".FilterForAttemptType").find(".activeSSCTab").removeClass("activeSSCTab");
		$($(".FilterForAttemptType").find(".SSCTab").get(0)).addClass("activeSSCTab");
	   };
	   var resetParams=function(){
		$(".searchMyContents").val("");
		params.query = "";
		params.orderBy = "views";
		$(".instLibContentsControl").find(".SSCSelect").resetVSelectTag({'newVal':{'text':'Most Popular','val':'views'}});
	   };
	   var prepareParamsForDiscreteScroll = function(parId,fetchUrl,fetchSize,noInitialFetch){
		var maxLimit = $(parId+" .resultDataCount").val();
		maxLimit = maxLimit?parseInt(maxLimit):0;
		$(parId).find(".totCount").text($(parId+" .resultDataCount").data("realCount"));
		fetchSize = fetchSize!=undefined?fetchSize:instLibrary.myContents.fetchSize;
		var p = {leftArrowId:parId+" .instLibHeadLeftArrow",disableClassLeft:"leftArrowDisabled",rightArrowId:parId+" .instLibHeadRightArrow",disableClassRight:"rightArrowDisabled",start:0,size:fetchSize,max:maxLimit,itemsPerPage:fetchSize,scrollDir:'horizontal','fetchUrl':fetchUrl,otherParams:params,callBack:"","noInitialFetch":noInitialFetch};
		if(maxLimit){
			if(!currentPaginatedDiv){ $(parId).removeClass("nonner");}
		}else if(("."+currentPaginatedDiv)!=parId){
			$(parId).addClass("nonner");		
		}
		return p;
	   };
	   var initNextPrevNav=function(){
		currentPaginatedDiv = "";
		
		var p = prepareParamsForDiscreteScroll(".instLibPlaylist",url["playlist"],null,true);
		$(".instLibPlaylistsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibTestslist",url["tests"]);
		$(".instLibTestsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibDocumentslist",url["documents"],null,true);
		$(".instLibDocumentsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibVideoslist",url["videos"],null,true);
		$(".instLibVideosIconsHolder").setForDiscreteScrolling(p);
	   };
	   var hideAll=function(){
		$(".instLibEachContentList").addClass("nonner");
	   };
	   var showAll=function(changeTab){
		if(changeTab){
			$(".instLibContentsControl").find(".activeSSCTab").removeClass("activeSSCTab");
			$($(".instLibContentsControl").find(".SSCTab").get(0)).addClass("activeSSCTab");
		}
		$(parDivId).find(".instLibListsHolderPagination").removeClass("instLibListsHolderPagination");
		$(".instLibEachContentList").removeClass("nonner");
	   };
	   var filterTestData=function(e){
		if(!currentPaginatedDiv || !divUrl[currentPaginatedDiv]) return;
		$(".FilterForAttemptType").find(".activeSSCTab").removeClass("activeSSCTab");
		$(e.currentTarget).addClass("activeSSCTab");
		var id = $(e.currentTarget).data("tabId");
    		showLoader(".instLibTestsIconsHolder");
		var p = cloneObject(params);
		p["attemptType"] = id;
		p["target"] = p["tabType"];
		var dataHolder = $("."+currentPaginatedDiv).find(".instLibListsHolder");
    		showLoader(dataHolder);
		$.get(divUrl[currentPaginatedDiv],p,function(data){
			dataHolder.html(data);
			if(currentPaginatedDiv == "instLibQuestionslist"){
				var count = dataHolder.find(".totalQuesItemsCount").val();
				count = count?count:0;
				$(".instLibQuestionslist").find(".totCount").text(count);
				loadMJEqns(dataHolder.get(0));
			}else{
				decideAndInitNav(currentPaginatedDiv,0);
			}
		});
	   };
	   this.refreshUi = function(){
		showAll();
		initNextPrevNav();
		$(".instLibViewTypes").removeClass("nonner");
		$(".instLibSearchBox input").val("");
		$(".instLibIconView").addClass("selectedView").siblings().removeClass("selectedView");
		params["viewType"] = "icon";
		resetFacet(".rearrangeBy","orderBy","timeCreated");
		resetFacet(".showContentBy","shareBy","ALL");
	   };
	   var resetFacet = function(name,pType,to){
		var rr = $($(parDivId).find(name).find("div").get(1))
		rr.addClass("blueTextColor").siblings().removeClass("blueTextColor").find("input").attr("checked","");
		rr.find("input").attr("checked","checked");
		params[pType] = to;
	   }; 
	   var decideNavTab=function(e){
		$(parDivId).find(".instLibMainHolder").find(".activeSSCTab").removeClass("activeSSCTab");
		$(parDivId).find(".instLibListsHolderPagination").removeClass("instLibListsHolderPagination");
		var index = $(e.currentTarget).data("parTabIndex");
		var tabName = $(e.currentTarget).text();
		if(index){
			var sscTabs = $(".instLibContentsControl").find(".instLibMainHolder")
				.find(".SSCTabs").find(".SSCTab");
			$(sscTabs.get(index)).addClass("activeSSCTab");
			tabName = $(sscTabs.get(index)).text();
		}else{
			$(e.currentTarget).addClass("activeSSCTab");
			tabName = $(e.currentTarget).text();
		}
		var id = $(e.currentTarget).data("tabId");
		onTabChange(tabName);
		if(!id){
			showAll();
			initNextPrevNav();
		}
		else{
			hideAll();
			initPaginationOn(id,false);
		}
		if(id=="instLibQuestionslist"){
			$(".instLibViewTypes").addClass("nonner");
		}else{
			$(".instLibViewTypes").removeClass("nonner");
		}
	   };
	   var initPaginationOn=function(id,noInitialFetch){
		$("."+id).removeClass("nonner").find(".instLibListsHolder").addClass("instLibListsHolderPagination");
		currentPaginatedDiv = id;
		if(id=="instLibQuestionslist") return;
		var p = prepareParamsForDiscreteScroll("."+id,divUrl[id],12,noInitialFetch);
		$("."+id+" .instLibListsHolder").addClass("instLibListsHolderPagination").setForPagination(p);
	   };
	   var chooseContent=function(e){
		$(e.delegateTarget).find(".vChoosenTag").removeClass("vChoosenTag");
		$(e.currentTarget).addClass("vChoosenTag");
	   };
	   var putTimerOn=function(method,time,otherParams1,otherParams2){
		if(timerObj) clearTimeout(timerObj);
		timerObj = setTimeout(
			function(otherParams1,otherParams2){
				method(otherParams1,otherParams2);
			},time);
	   };
	   var showLoader=function(divId){
		if(!currentPaginatedDiv){
			$(divId).closest(".instLibEachContentList").removeClass("nonner");
		}
		smallLoader(divId);
	   };
	   var updateFetchSize=function(id,params){
		var size = instLibrary.myContents.fetchSize;
		if(id==currentPaginatedDiv) size = 12;
		params["size"] = size;
		return params;
	   };
	   this.fetchDataOnChange=function(brdIds,getShareType,centers){
		params.start = 0;
		resetFilterDiv();
    		showLoader(".instLibPlaylistsIconsHolder");
    		showLoader(".instLibTestsIconsHolder");
    		showLoader(".instLibDocumentsIconsHolder");
    		showLoader(".instLibVideosIconsHolder");
		if(getShareType!=undefined){ 
			params['with']=getShareType;
		}
		if(centers!=undefined){ 
			params['centers']=centers;
		}
		if(brdIds!=undefined){ 
			params['brdIds']=brdIds;
		}
		var p = cloneObject(params);
		$(".instLibPlaylistsIconsHolder").load(url.playlists,updateFetchSize("instLibPlaylist",p),function(){
			decideAndInitNav("instLibPlaylist",0);
			$(".instLibTestsIconsHolder").load(url.tests,updateFetchSize("instLibTestslist",p),function(){
				decideAndInitNav("instLibTestslist",0);
				$(".instLibDocumentsIconsHolder").load(url.documents,updateFetchSize("instLibDocumentslist",p)
				,function(){
					decideAndInitNav("instLibDocumentslist",0);
					$(".instLibVideosIconsHolder").load(url.videos,updateFetchSize("instLibVideoslist",p)
					,function(){
						decideAndInitNav("instLibVideoslist",0);
					});
				});
			});
		});
		loadQuestions(0);
	   };
	   var initDiscreteScrollOn=function(id){
		var p = prepareParamsForDiscreteScroll("."+id,divUrl[id]);
		$("."+id+" .instLibListsHolder").setForDiscreteScrolling(p);
	   };
	   var decideAndInitNav=function(id){
		if(currentPaginatedDiv == id){
			initPaginationOn(id,false);
		}else{
			initDiscreteScrollOn(id);
		}
	   };
	   var loadQuestions = function(start){
		var p = cloneObject(params);
		p["start"] = start?start:0;
		p["size"] = 10;
		p["target"] = p["tabType"];
		var dataHolder = $(".instLibQuestionsIconsHolder");
    		showLoader(dataHolder);
		dataHolder.load(url.questions,p,function(){
			var quesHolder = $(".instLibQuestionsIconsHolder");
			var count = quesHolder.find(".totalQuesItemsCount").val();
			count = count?count:0;
			$(".instLibQuestionslist").find(".totCount").text(count);
			loadMJEqns(quesHolder.get(0));
		});
	   };
	   var loadMoreQuestions = function(e){
		var start = $(this).data('start') + $(this).data('size');
		smallLoader($(this))
		if(typeof start == "number"){
			var p = cloneObject(params);
			p["start"] = start?start:0;
			p["size"] = 10;
			p["target"] = p["tabType"];
			$.get(url.questions,p,function(data){
				$(e.currentTarget).remove();
				var quesHolder = $(".instLibQuestionsIconsHolder");
				quesHolder.append(data);
				loadMJEqns(quesHolder.get(0));
			});
		}
	   };
	   var showContentBy = function(e){
		$(this).closest("div").addClass("blueTextColor").siblings().removeClass("blueTextColor");
		params["shareBy"] = $(this).data("shareBy");
		instLibrary.myContents.fetchDataOnChange();
	   };
	   var rearrangeContentBy = function(e){
		$(this).closest("div").addClass("blueTextColor").siblings().removeClass("blueTextColor");
		params["orderBy"] = $(this).data("orderBy");
		instLibrary.myContents.fetchDataOnChange();
	   };
	   var viewTypeChanged = function(e){
		$(this).addClass("selectedView").siblings().removeClass("selectedView");
		params["viewType"] = $(this).data("type");
		instLibrary.myContents.fetchDataOnChange();
	   }
	   this.init = function(){
		initNextPrevNav();
		registerFns();
	   };
	   var registerFns = function(){
		if($(parDivId).data("libInited")) return false;
		$(parDivId).on("click",".instLibMainHolder .SSCTab,.instLibItemsHead .headText",decideNavTab)
			   .on("click",".FilterForAttemptType .SSCTab",filterTestData)
			   .on("click",".LMHandlerDivLoadMore",loadMoreQuestions)
			   .on("click",".instLibFacetHolder .rearrangeBy input",rearrangeContentBy)
			   .on("click",".instLibFacetHolder .showContentBy input",showContentBy)
			   .on("click",".instLibViewTypes span",viewTypeChanged)
			   .on("keydown",".instLibSearchBox input",function(e){
				if(e.which == 13){
					params.query = $(e.currentTarget).val();
					instLibrary.myContents.fetchDataOnChange();
				}
			    });
		$(parDivId).data("libInited",true);
	   };
	};
};
function instLibraryBatchChanged(value,target,targetValue){
	instLibrary.resetProgramme();
}
