var instLibrary = new function(){
	var parDivId = "#instituteHome";
	var subBrdId = "";var topicBrdId = "";var subTopicBrdId="";
	var subName="";
	var topicsData;
	var selTopic; var selSubTopic;var selSubSubTopic;
	var params = {'tabType':"MY_INSTITUTE",'year':new Date().getFullYear()};
	var instParams = params;
	var slideHolderDiv = ".instLibSlideHolder";
	var slideTime = 650;
	var hideTopicInfoTimeout;
	var dirArr = {"+":{left:'-=100%'},"-":{left:'+=100%'}};
	var animInProgress = false;var curSlideIndex = 0;
	var item_per_col = 3;
	var col_per_page = 3;
	var DEFAULT_OPEN_TAB = "MODULES";
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
		p['programId'] = data["progId"];
		if(data["section"]){
			p['sectionId'] = data["section"];
		}
		if(data["center"]){
			p['centerId'] = data["center"];
		}
		if(e){
			p['parentId'] = $(this).data("id");
			subBrdId = $(this).data("brdId");
			subName = $(this).attr("title");
		}
		var div = $(".instLibSlideHolder").find(".instLibSlide").get(1);
		showTopLoader();
		vReq.get("/Institute/getLibTopics",p,function(data){
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
		urlQueryHelper.push("subject",subBrdId);
		//instLibrary.myContents.refreshUi();
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
		instLibrary.myContents.fetchDataOnChange(brdIds);
		$(".instLibViewTypes").removeClass("nonner");
	};
	var topicSelected = function(e){
		topicBrdId = $(this).data("brdId");
		var p = cloneObject(params);
		var data = getDistinctData();
		p['programId'] = data["progId"];
		if(data["section"]){
			p['section'] = data["section"];
		}
		if(data["center"]){
			p['center'] = data["center"];
		}
		var topicName;
		if(e){
			p['parentId'] = topicBrdId;
			topicName = $(this).attr("title");
		}
		var div = $(".instLibSlideHolder").find(".instLibSlide").get(2);
		showTopLoader();
		vReq.get("/Institute/getLibSubTopics",p,function(data){
			$(div).html(data);
			hideTopLoader();
			nextSlide("+",postBarAnimation);
			$(".instLibSelSubName").text(toCamelCase(subName));
			$(".instLibSelTopicName").text(toCamelCase(topicName));
		});
		urlQueryHelper.push("topic",topicBrdId);
		//instLibrary.myContents.refreshUi();
		fetchContent();
	};
	/*var topicSelected = function(e){
		var topicIndex = $(this).data("index");
		topicBrdId = $(this).data("brdId");
		var topicName = $(this).attr("title");
		var topicData = selTopic = topicsData[topicIndex];
		var subTopics = topicData.subTopics;
	
		$(".instLibSelTopicName").text(toCamelCase(topicName));
		appendToNavBar(".instLibTopicName",topicName);
		$(parDivId).find(".instLibSubTopicTxt").removeClass("selectedInstLibTxt");
		drawTopicsInfoSec(topicData);
		//$("#instLibSubTopicsInfoH").html($("#instLibTopicsInfoH").html());

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
		var hTotal = Math.ceil(total/item_per_col);

		uiCloneHelper.set(parDivId+" .instLibrarySubTopics .instLibSubTopicH1",".instLibSubTopicH2",0);
		var hIndex;
		for(hIndex=0;hIndex<hTotal;hIndex++){
			var hDiv = uiCloneHelper.create(hIndex);
			$(hDiv).data("index",hIndex).css("left",(hIndex*$(hDiv).width())+"px");
			var st = $(hDiv).find(".instLibEachSubTopic").get(0);
			$(st).siblings().remove();
			var stIndex;var strt=hIndex*item_per_col;
			fillSubTopicData(st,subTopics[strt],strt);
			for(stIndex=strt+1;stIndex<total && stIndex<strt+item_per_col;stIndex++){
				var newSt = $(st).clone();
				$(hDiv).append(newSt);
				fillSubTopicData(newSt,subTopics[stIndex],stIndex);
			}
		}
		nextPrevReset(".instLibSubTopicH1",hIndex);
		if(hIndex>col_per_page){
			$(".nextPrevInstLibST").removeClass("nonner");
			$(".prevInstLibST").addClass("disPrevInstLib");
			$(".nextInstLibST").removeClass("disPrevInstLib");
		}else{
			$(".nextPrevInstLibST").addClass("nonner");
		}
		uiCloneHelper.removeFrom(hIndex);
		nextSlide("+",postBarAnimation);
	};*/
	var drawTopicsInfoSec = function(topicData){
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
		}else if(DIR=="-"){
			curSlideIndex = curSlideIndex < 0 ? 0 : curSlideIndex-1;
			try{onBackNav[curSlideIndex](curSlideIndex);}catch(err){}
		}
		//showOrHideContents(showContent);
	};
	var onBackNav = [
		function(){//BACK FROM TOPICS TO SUBJECT
			subBrdId = undefined;
			fetchContent();
		},
		function(){//BACK FROM SUBTOPICS TO TOPIC
			hideSubSubTopics();
			$(".instLibTopicName").text("");
			$(".instLibSubTopicName").text("");
			topicBrdId = subTopicBrdId = undefined;
			fetchContent();
		}
	];
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
		var total = parseInt(h1.data("total"),10) - 1;
		var cur = h1.data("cur");
		if( (direction=="-" && cur+col_per_page<total) || ( direction=="+" && cur>0) ){
			animInProgress = true;
			h1.animate({"left":direction+"="+width+"px"},200,function(){animInProgress=false;});
			if(direction=="-"){++cur;}else{--cur;};
			h1.data("cur",cur);
			if(cur<=0){
				$(p).addClass("disPrevInstLib");
				$(n).removeClass("disPrevInstLib");
			}else if(cur+col_per_page>=total){
				$(p).removeClass("disPrevInstLib");
				$(n).addClass("disPrevInstLib");
			}else{
				$(p).removeClass("disPrevInstLib");
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
	/*var navBarSubClicked = function(){
		var d = $(slideHolderDiv);
		var hIndex = nextPrevReset(".instLibTopicH1");
		if(hIndex>col_per_page){
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
		if(hIndex>col_per_page){
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
	};*/
	var programChanged = function(){
		setTimeout(function(){
			instLibrary.resetProgramme();
		},100);
		var progId = $(this).data("value");
		urlQueryHelper.push("program",progId);
	};
	var centerChanged = function(){
		setTimeout(function(){
			instLibrary.resetProgramme();
		},100);
		var centerId = $(this).data("value");
		urlQueryHelper.push("center",centerId);	
	};
	var sectionChanged = function(){
		instLibrary.resetProgramme();
		var sectionId = $(this).data("value");
		urlQueryHelper.push("section",sectionId);	
	};
	var registerEvents = function(){
		$(parDivId).off("click",".instLibEachSub")
					.on("click",".instLibEachSub",subjectSelected)
			.off("click",".instLibEachTopic .instLibTopicTxt")
			.on("click",".instLibEachTopic .instLibTopicTxt",topicSelected)
			/*.on("mouseenter",".instLibEachTopic .instLibTopicTxt",topicMouseIn)
			.on("mouseout",".instLibEachTopic .instLibTopicTxt",topicMouseOut)*/
			.off("click",".instLibEachSubTopic .instLibSubTopicTxt")
			.on("click",".instLibEachSubTopic .instLibSubTopicTxt",subTopicSelected)
		
			.on("click",".instLibPrevSlide",prevSlide)
			.on("click",".instLibSelSubName",goToSubjectPage)
			
			// .on("change",".instLibarySelMyProg .nDropDown",programChanged)
			// .on("change",".instLibarySelMyCenter .nDropDown",centerChanged)
			// .on("change",".instLibarySelMySection .nDropDown",sectionChanged)
		
			.on("click",".prevInstLibT",prevTopics)
			.on("click",".nextInstLibT",nextTopics)
			.on("click",".prevInstLibST",prevSubTopics)
			.on("click",".nextInstLibST",nextSubTopics);
		$(".instLibarySelMyProg .nDropDown").off("change")
											.on("change",programChanged);

		$(".instLibarySelMyCenter .nDropDown").off("change")
											.on("change",centerChanged);

		$(".instLibarySelMySection .nDropDown").off("change")
											.on("change",sectionChanged);

		/*$("#instLibNavBar").on("click",".instLibSelSubName",navBarSubClicked)
			.on("click",".instLibTopicName",navBarTopicClicked)
			.on("click",".instLibSubTopicName",navBarSubTopicClicked);*/
	};
	var getDistinctData = function(){
		var progId = $(".instLibarySelMyProg").find(".nDropDown").data("value");
		var centerId = $(".instLibarySelMyCenter").find(".nDropDown").data("value");
		var sectionId = $(".instLibarySelMySection").find(".nDropDown").data("value");
		if(progId){
			return {"progId":progId,"center":centerId,"section":sectionId};
		}
		return {"progId":"","center":"","section":""};
	};
	var getProgrammeInfo = function(){
		var p = cloneObject(params);
		var data = getDistinctData();
		p['programId'] = data["progId"];
		var div = $(".instLibSlideHolder").find(".instLibSlide").get(0);
		showTopLoader();
		vReq.get("/Institute/getLibProgrammeInfo",p,function(data){
			$(div).html(data);
			hideTopLoader();
			showScrollBarOnSubjectList();
			institute.animate($($(div).find(".instLibEachSub").get(0)));
		});
	};
	var showScrollBarOnSubjectList = function(){
		var height = $(".instSubHolder").height();
		var instLibraryControlsHeight = $(".instLibraryControls").height();
		if(height > instLibraryControlsHeight /2){
			$(".instSubHolder").css("overflow-y","auto");
			$(".instSubHolder").css("max-height",instLibraryControlsHeight /2);
		}
		else{
			$(".instSubHolder").css("overflow-y","hidden");
		}
	}
	var goToSubjectPage = function(){
		try{
			$('#instLibPrevSlide').click();
		}catch(err){
			putConsoleError(err);
		}
	};
	this.resetProgramme = function(){
		var d = $(slideHolderDiv);
		d.animate({"left":"0px"},slideTime,function(){
			try{postBarAnimation()}catch(err){}
		});
		getProgrammeInfo();
		hideSubSubTopics();
		subBrdId = topicBrdId = subTopicBrdId = undefined;
		urlQueryHelper.replace("subject","");
		urlQueryHelper.replace("topic","");
		fetchContent();
	};
	var resetAllData = function(){
		subBrdId = topicBrdId = subTopicBrdId = undefined;
		selTopic = selSubTopic = selSubSubTopic = undefined;
	};
	this.onBack = function(tabId){
		resetAllData();
		var progInfo = getDistinctData();
		progInfo = institute.readUrlForProgInfo();
		getProgrammeInfo();
		fetchContent();
		try{
			this.myContents.openTabDirectly(tabId.toUpperCase());
		}catch(err){
			putConsoleError(err);
			this.myContents.openTabDirectly(DEFAULT_OPEN_TAB);
		}
		return true;
	};
	this.init = function(){
		this.subjectFlag = false;
		this.loadReqonTabClick = false;
		resetAllData();
		var orgId = $("#myInstitutePage").data('orgId');
		var tabIdFromUrl = location.pathname.split("/")[4];
		if(!tabIdFromUrl){
			replaceInstHistory(orgId,"library/"+DEFAULT_OPEN_TAB.toLowerCase()+urlQueryHelper.getQueryString());
		}                
		try{
			institute.init();                        
			var progInfo = getDistinctData();
			progInfo = institute.readUrlForProgInfo();
			var counter1 =0;
			var url = window.location.href;
			if (url.indexOf("subject") > -1){
				instLibrary.subjectFlag = true;
				var topicSelection1 = setInterval(function(){
					counter1++;
					var subjectId = urlQueryHelper.read("subject");
					var subId = 'SUBJECT'+subjectId.toString();
					try{
						$('#'+subId).click();
					}catch(err){
						putConsoleError(err);
					}
					if(counter1>=1){
						clearInterval(topicSelection1);
					}
				}, 500);
			}
			var counter =0;
			var topicSelection = setInterval(function(){
				counter++;
				if (url.indexOf("topic") > -1){
					var topicId = urlQueryHelper.read("topic");
					var topId = 'TOPIC'+topicId.toString();
					try{
						$('#'+topId).trigger('click');
					}catch(err){
						putConsoleError(err);
					}
				}
				if(counter>=1){
					clearInterval(topicSelection);
				}
			}, 2000);
		}catch(err){}  
                /*by ajith: giving 50px as leeway, and 10px as side padding for
                 *  each tab.'All' tab is excluded when width is calculated */
                var tabs=$('.tabFilterType');
                //subtract margin 16px on both side.
                var availableWidth=($(".instLibraryHolder").width()-82)/(tabs.length-1);
                tabs.css('width',(availableWidth-10));

		//params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		params['contentSrc'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		registerEvents();
		getProgrammeInfo();
		this.myContents.init();
		if(!instLibrary.subjectFlag){
			fetchContent();
		}
		try{
			if(libraryOpenTabId){
				libraryOpenTabId = libraryOpenTabId.toUpperCase();
				this.myContents.openTabDirectly(libraryOpenTabId);
			}else{
				this.myContents.openTabDirectly(DEFAULT_OPEN_TAB);
			}
		}catch(err){
			this.myContents.openTabDirectly(DEFAULT_OPEN_TAB);
		}
	};
	this.myContents = new function(){
	   var params={"orderBy":'timeCreated',"orderByOrg":"timeCreated","shareBy":"ALL","query":"","resultType":"ALL","sortOrder":"DESC","start":0,'tabType':"MY_INSTITUTE",'year':new Date().getFullYear(),'contentSrc':{'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')}};
	   var url={"questions":"/Institute/getLibQuestions","playlists":"/Institute/getLibPlaylists","documents":"/Institute/getLibDocuments","tests":"/Institute/getLibTests","videos":"/Institute/getLibVideos","assignments":"/Institute/getLibAssignments","files":"/Institute/getLibFiles","modules":"/Institute/getLibModules"};
	   var divUrl={"instLibQuestionslist":"/Institute/getLibQuestions","instLibPlaylist":"/Institute/getLibPlaylists","instLibDocumentslist":"/Institute/getLibDocuments","instLibTestslist":"/Institute/getLibTests","instLibAssignmentslist":"/Institute/getLibAssignments","instLibVideoslist":"/Institute/getLibVideos","instLibFileslist":"/Institute/getLibFiles","instLibModuleslist":"/Institute/getLibModules"};
	   var tabParams = {};
	   this.fetchSize=10;
	   var currentPaginatedDiv = "";
	   var timerObj;
	   var libDivId = ".instLibraryHolder";
	   var filterDivs = {"TESTS":".FilterForAttemptType","QUESTIONS":".FilterForAttemptType","ASSIGNMENTS":".FilterForAttemptType"};
	   var onTabChange = function(tabId,tabDivId){
			if(!tabId) return false;
			tabId = tabId.toUpperCase();
			tabId = $.trim(tabId);
			if(filterDivs[tabId]){
				$(tabDivId).find(filterDivs[tabId]).removeClass("nonner");
				//resetFilterDiv();
			}else{
				$(libDivId).find(".FilterForAttemptType").addClass("nonner");
			}
	   };
	   var resetFilterDiv=function(){
		$($(".FilterForAttemptType").find(".SSCTab").get(0)).addClass("activeSSCTab").siblings().removeClass("activeSSCTab");
		nDropDown.reset($(libDivId).find(".instLibContentSortFilter"));
	   };
	   var getMaxLimit = function(parId){
		var countDiv = $(parId).find(".resultDataCount");
		var maxLimit = countDiv.val();
		maxLimit = maxLimit?parseInt(maxLimit):0;
		putTotCount(parId,countDiv.data("realCount"));
		return maxLimit;	
	   };
	   var prepareParamsForDiscreteScroll = function(parId,fetchUrl,fetchSize,noInitialFetch,putParams){
		putParams = putParams ? putParams : params;
		var maxLimit = getMaxLimit(parId);
		fetchSize = fetchSize!=undefined?fetchSize:instLibrary.myContents.fetchSize;
		var p = {leftArrowId:parId+" .instLibHeadLeftArrow",disableClassLeft:"leftArrowDisabled",rightArrowId:parId+" .instLibHeadRightArrow",disableClassRight:"rightArrowDisabled",start:0,size:fetchSize,max:maxLimit,itemsPerPage:fetchSize,scrollDir:'horizontal','fetchUrl':fetchUrl,otherParams:putParams,callBack:"","noInitialFetch":noInitialFetch,"getMaxLimitFn":getMaxLimit};
		if(maxLimit){
			if(!currentPaginatedDiv){ $(parId).removeClass("nonner");}
		}else if(("."+currentPaginatedDiv)!=parId){
			$(parId).addClass("nonner");		
		}
		return p;
	   };
	   var initNextPrevNav=function(){
	   /*
		currentPaginatedDiv = "";
		var p;
		p = prepareParamsForDiscreteScroll(".instLibTestslist",url["tests"],null,true);
		$(".instLibTestsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibAssignmentslist",url["assignments"],null,true);
		$(".instLibAssignmentsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibDocumentslist",url["documents"],null,true);
		$(".instLibDocumentsIconsHolder").setForDiscreteScrolling(p);

		p = prepareParamsForDiscreteScroll(".instLibVideoslist",url["videos"],null,true);
		$(".instLibVideosIconsHolder").setForDiscreteScrolling(p);
		
		p = prepareParamsForDiscreteScroll(".instLibFileslist",url["files"],null,true);
		$(".instLibFilesIconsHolder").setForDiscreteScrolling(p);
		
                p = prepareParamsForDiscreteScroll(".instLibModuleslist",url["modules"],null,true);
		$(".instLibModulesIconsHolder").setForDiscreteScrolling(p);
		
                
		p = prepareParamsForDiscreteScroll(".instLibPlaylist",url["playlist"],null,true);
		$(".instLibPlaylistsIconsHolder").setForDiscreteScrolling(p);
		*/
	   };
	   var hideAll=function(){
		$(".instLibEachContentList").addClass("nonner");
	   };
	   var showAll=function(changeTab){
		if(changeTab){
			$(".instLibContentsControl").find(".activeLibTab").removeClass("activeLibTab");
			$($(".instLibContentsControl").find(".instLibFltrTab").get(0)).addClass("activeLibTab");
		}
		$(parDivId).find(".instLibListsHolderPagination").removeClass("instLibListsHolderPagination");
		$(parDivId).find(".instLibPagination").removeClass("instLibPagination");
		$(".instLibEachContentList").removeClass("nonner");
	   };
	   var filterDataByAttempts = function(e){
		if(!currentPaginatedDiv || !divUrl[currentPaginatedDiv]) return;
		//$(".FilterForAttemptType").find(".activeSSCTab").removeClass("activeSSCTab");
		var $this = $(this);
		$(this).addClass("activeSSCTab").siblings().removeClass("activeSSCTab");
		var tabId = $this.data("tabId");
		var p = updateFetchSize(currentPaginatedDiv,tabParams[currentPaginatedDiv]);
		p["resultType"] = tabId ? tabId : "ALL";
		filterSingleContentType(p,$this);
	   };
	   var sortContentData = function(e){
		var p = updateFetchSize(currentPaginatedDiv,tabParams[currentPaginatedDiv]);
		p["orderBy"] = $(this).data("value");
		p["orderBy"] = p["orderBy"] ? p["orderBy"] : params["orderByOrg"];
		p["sortOrder"] = $(this).data("itemValue").order;
		filterSingleContentType(p,$(this));
	   };
	   var filterSingleContentType = function(p,$this){
		p["target"] = p["tabType"];
		var dataHolder = $this.closest(".instLibEachContentList").find(".instLibListsHolder");
    		showLoader(dataHolder);
		vReq.get(divUrl[currentPaginatedDiv],p,function(data){
			dataHolder.html(data);
			if(currentPaginatedDiv == "instLibQuestionslist"){
				var count = dataHolder.find(".totalQuesItemsCount").val();
				count = count?count:0;
				putTotCount(".instLibQuestionslist",count);
				loadMJEqns(dataHolder.get(0));
			}else{
				decideAndInitNav(currentPaginatedDiv,p);
			}
		});
	   };
	   this.refreshUi = function(){
		showAll(true);
		initNextPrevNav();
		$(".instLibViewTypes").removeClass("nonner");
		$(".instLibSearchBox input").val("");
		$(".instLibIconView").addClass("selectedView").siblings().removeClass("selectedView");
		params["viewType"] = "icon";
		//resetFacet(".rearrangeBy","orderBy","timeCreated");
		//resetFacet(".showContentBy","shareBy","ALL");
	   };
	   /*var resetFacet = function(name,pType,to){
		var rr = $($(parDivId).find(name).find("div").get(1))
		rr.addClass("blueTextColor").siblings().removeClass("blueTextColor").find("input").attr("checked","");
		rr.find("input").attr("checked","checked");
		params[pType] = to;
	   };*/
	   var divLinkage = {
		/*"PLAYLISTS":{id:"myContentsPlaylist",index:1},*/
                "MODULES":{id:"instLibModuleslist",index:1,reqRun:false},
                "TESTS":{id:"instLibTestslist",index:2,reqRun:false},
                "DOCUMENTS":{id:"instLibDocumentslist",index:3,reqRun:false},
                "VIDEOS":{id:"instLibVideoslist",index:4,reqRun:false},
                "ASSIGNMENTS":{id:"instLibAssignmentslist",index:5,reqRun:false},
                "FILES":{id:"instLibFileslist",index:6,reqRun:false},
                "QUESTIONS":{id:"instLibQuestionslist",index:7,reqRun:false}
	   };
	   this.openTabDirectly = function(tabId){
		var divId = divLinkage[tabId];
		divId = divId ? divId : divLinkage[DEFAULT_OPEN_TAB];
		setTimeout(function(){
			$($(".instLibContentsControl").find(".instLibFltrTab").get(divId.index)).trigger("click");
		},10);
		/*if(divId){
			$(".instLibContents").find(".instLibListsHolderPagination").removeClass("instLibListsHolderPagination");
			$($(".contentsTabs").find(".instLibFltrTab").removeClass("activeLibTab").get(divId.index)).addClass("activeLibTab");
			
			hideAll();
			initPaginationOn(divId.id,true);
		}
		myContents.onTabChange(tabId);*/
	   };
	   var decideNavTab=function(e){
		$(parDivId).find(".instLibMainHolder").find(".activeLibTab").removeClass("activeLibTab");
		$(parDivId).find(".instLibListsHolderPagination").removeClass("instLibListsHolderPagination");
		$(parDivId).find(".instLibPagination").removeClass("instLibPagination");
		var index = $(e.currentTarget).data("parTabIndex");
		var tabName = $(e.currentTarget).find(".tabFilterType").text();
		if(index){
			var tabs = $(".instLibContentsControl").find(".instLibMainHolder")
				.find(".instLibFltrTab");
			$(tabs.get(index)).addClass("activeLibTab");
			tabName = $(tabs.get(index)).find(".tabFilterType").text();
		}else{
			$(e.currentTarget).addClass("activeLibTab");
			tabName = $(e.currentTarget).find(".tabFilterType").text();
		}
		pushInstHistory($("#myInstitutePage").data('orgId'),"library/"+tabName.toLowerCase()+urlQueryHelper.getQueryString());
		var id = $(e.currentTarget).data("tabId");
		onTabChange(tabName,$("."+id));
		if(!id){
			showAll();
			initNextPrevNav();
		}
		else{
			hideAll();
			initPaginationOn(id,true);
			if(instLibrary.loadReqonTabClick !== false){
				divLinkage[tabName.toUpperCase()].reqRun === false ? decideLibraryRequests(cloneObject(params),id,tabName.toUpperCase()):"";
			}
		}
		if(id=="instLibQuestionslist"){
			$(".instLibViewTypes").addClass("nonner");
		}else{
			$(".instLibViewTypes").removeClass("nonner");
		}
	   };
	   var initPaginationOn=function(id,noInitialFetch,putParams){
		$("."+id).removeClass("nonner").addClass("instLibPagination")
			.find(".instLibListsHolder").addClass("instLibListsHolderPagination");
		currentPaginatedDiv = id;
		if(id=="instLibQuestionslist") return;
		var p = prepareParamsForDiscreteScroll("."+id,divUrl[id],10,noInitialFetch,putParams);
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
		bigLoader(divId);
	   };
	   var updateFetchSize=function(id,params){
		var size = instLibrary.myContents.fetchSize;
		if(id==currentPaginatedDiv) size = 10;
		params["size"] = size;
		return params;
	   };
	   this.resetReqRun = function(){
		Object.keys(divLinkage).forEach(function(key) {
			divLinkage[key].reqRun = false;
		});
	   }
	   this.fetchDataOnChange=function(brdIds){
		if(params.query != null && params.query != "" && params.query != undefined){
			$(".searchInputBox").val(params.query);
		}
		params.start = 0;
		resetFilterDiv();
    		/*TODO showLoader(".instLibPlaylistsIconsHolder");
    		showLoader(".instLibDocumentsIconsHolder");*/
    		showLoader(".instLibVideosIconsHolder");
    		showLoader(".instLibFilesIconsHolder");
                showLoader(".instLibModulesIconsHolder");
    		showLoader(".instLibTestsIconsHolder");
    		showLoader(".instLibAssignmentsIconsHolder");
		if(brdIds!=undefined){ 
			params['brdIds']=brdIds;
		}
		var data = getDistinctData();
		params['programId'] = data["progId"];
		if(data["section"]){
			params['sectionId'] = data["section"];
		}else{
			$(params).removeProp('sectionId');
		}
		if(data["center"]){
			params['centerId'] = data["center"];
		}else{
			$(params).removeProp('centerId');
		}
		var p = cloneObject(params);
		var tabId = $(".instLibMainHolder").find(".activeLibTab").data("tabId");
		var tabName = $(".activeLibTab").find(".tabFilterType").text();
        decideLibraryRequests(p,tabId,tabName.toUpperCase());
        instLibrary.myContents.resetReqRun();
        instLibrary.loadReqonTabClick = true;
		/*$(".instLibPlaylistsIconsHolder").load(url.playlists,updateFetchSize("instLibPlaylist",p),function(){
			decideAndInitNav("instLibPlaylist",0);*/
			/*TODO vReq.get(url.tests,updateFetchSize("instLibTestslist",p),function(htmlData){
				$(".instLibTestsIconsHolder").html(htmlData);
				decideAndInitNav("instLibTestslist",0);
				vReq.get(url.documents,updateFetchSize("instLibDocumentslist",p)
				,function(htmlData){
					$(".instLibDocumentsIconsHolder").html(htmlData);
					decideAndInitNav("instLibDocumentslist",0);
					vReq.get(url.videos,updateFetchSize("instLibVideoslist",p)
					,function(htmlData){
						$(".instLibVideosIconsHolder").html(htmlData);
						decideAndInitNav("instLibVideoslist",0);
					});
				});
			});*/
		//});
	   };

	   var decideLibraryRequests = function(p,tabId,tabName){
			showTopLoader();
			if(tabId === ""){
				return false;
			}
			switch(tabId){
			case "instLibAssignmentslist":
				vReq.get(url.assignments,updateFetchSize("instLibAssignmentslist",p),function(htmlData){
			        $(".instLibAssignmentsIconsHolder").html(htmlData);
			        decideAndInitNav("instLibAssignmentslist",p);
			    });
			break;
			case "instLibVideoslist":
			vReq.get(url.videos,updateFetchSize("instLibVideoslist",p),function(htmlData){
			        $(".instLibVideosIconsHolder").html(htmlData);
			        decideAndInitNav("instLibVideoslist",p);
			    });
			break;
			case "instLibDocumentslist":
			vReq.get(url.documents,updateFetchSize("instLibDocumentslist",p),function(htmlData){
			        $(".instLibDocumentsIconsHolder").html(htmlData);
			        decideAndInitNav("instLibDocumentslist",p);
			    });
			break;
			case "instLibTestslist":
			vReq.get(url.tests,updateFetchSize("instLibTestslist",p),function(htmlData){
			        $(".instLibTestsIconsHolder").html(htmlData);
			        decideAndInitNav("instLibTestslist",p);
			    });
			break;
			case "instLibFileslist":
			vReq.get(url.files,updateFetchSize("instLibFileslist",p),function(htmlData){
			        $(".instLibFilesIconsHolder").html(htmlData);
			        decideAndInitNav("instLibFileslist",p);
			    });
			break;
			case "instLibQuestionslist":
			loadQuestions(0);
			break;
			case "instLibModuleslist":
			vReq.get(url.modules,updateFetchSize("instLibModuleslist",p),function(htmlData){
			        $(".instLibModulesIconsHolder").html(htmlData);
			        decideAndInitNav("instLibModuleslist",p);
			});
			break;
		}
			hideTopLoader();
			divLinkage[tabName].reqRun = true;
	}
	   var initDiscreteScrollOn=function(id,putParams){
		var p = prepareParamsForDiscreteScroll("."+id,divUrl[id],undefined,undefined,putParams);
		$("."+id+" .instLibListsHolder").setForDiscreteScrolling(p);
	   };
	   var decideAndInitNav=function(id,putParams){
		tabParams[id] = cloneObject(putParams);
		if(currentPaginatedDiv == id){
			initPaginationOn(id,false,putParams);
		}else{
			initDiscreteScrollOn(id,putParams);
		}
	   };
	   var loadQuestions = function(start){
		var p = cloneObject(params);
		p["start"] = start?start:0;
		p["size"] = 10;
		p["target"] = p["tabType"];
		tabParams["instLibQuestionslist"] = p;
		var dataHolder = $(".instLibQuestionsIconsHolder");
    		showLoader(dataHolder);
		vReq.get(url.questions,p,function(data){
			dataHolder.html(data);
			var quesHolder = $(".instLibQuestionsIconsHolder");
			var count = quesHolder.find(".totalQuesItemsCount").val();
			count = count?count:0;
			putTotCount(".instLibQuestionslist",count);
			loadMJEqns(quesHolder.get(0));
		});
	   };
	   var putTotCount = function(holder,count){
		count = parseInt(count,10);
		count = count?count:0;
		if(count>3 || (holder == ".instLibQuestionslist" && count>0)){
			$(holder).find(".viewAllCont").removeClass("opcHide");
		}else{
			$(holder).find(".viewAllCont").addClass("opcHide");
		}
		$(holder).find(".totCount").text(count);
	   };
	   var loadMoreQuestions = function(e){
		var start = $(this).data('start') + $(this).data('size');
		smallLoader($(this))
		if(typeof start == "number"){
			var p = tabParams["instLibQuestionslist"];
			p["start"] = start?start:0;
			p["size"] = 10;
			p["target"] = p["tabType"];
			vReq.get(url.questions,p,function(data){
				$(e.currentTarget).remove();
				var quesHolder = $(".instLibQuestionsIconsHolder");
				quesHolder.append(data);
				loadMJEqns(quesHolder.get(0));
			});
		}
	   };
	   /*var showContentBy = function(e){
		$(this).closest("div").addClass("blueTextColor").siblings().removeClass("blueTextColor");
		params["shareBy"] = $(this).data("shareBy");
		instLibrary.myContents.fetchDataOnChange();
	   };*/
	   var rearrangeContentBy = function(e){
		params["orderBy"] = $(this).data("value");
		params["orderByOrg"] = $(this).data("value");
		instLibrary.myContents.fetchDataOnChange();
	   };
	   var viewTypeChanged = function(e){
		$(this).addClass("selectedView").siblings().removeClass("selectedView");
		params["viewType"] = $(this).data("type");
		if(params["viewType"] == "list"){
			$(parDivId).find(".instLibContents").addClass("libContentListView");
		}else{
			$(parDivId).find(".instLibContents").removeClass("libContentListView");
		}
		instLibrary.myContents.fetchDataOnChange();
	   }
	   this.init = function(){
		libDivId = $(".instLibraryHolder");
		if($(libDivId).find(".instLibContents").data("inited")) return;
		prepareUi();
		initNextPrevNav();
		registerFns();
		$(libDivId).find(".instLibContents").data("inited",true);
	   };
	   var prepareUi = function(){
		var attemptFilterDiv = $(libDivId).find(".FilterForAttemptType").get(0);
		$(libDivId).find(".instLibContentAttemptFilter").html("").html(attemptFilterDiv);
		var filterDrop = $(libDivId).find(".instLibContentSortFilter .nDropDown");
		$(libDivId).find(".instLibContentSortFilter").html(filterDrop);
	   };
	   var hideShowMainCntrls = function(){
		var cntrls = $(libDivId).find(".instLibMainControls").toggleClass("hide50");
		$(this).toggleClass("showCntr");
	   };
	   var registerFns = function(){
		if($(parDivId).data("libInited")) return false;
		$(parDivId).on("click",".instLibMainHolder .instLibFltrTab,.instLibItemsHead .headText",decideNavTab)
			   .on("click",".FilterForAttemptType .SSCTab",filterDataByAttempts)
			   .on("change",".instLibContentSortFilter .nDropDown",sortContentData)
			   .on("click",".instLibraryHolder .LMHandlerDivLoadMore",loadMoreQuestions)
			   .on("change",".libAllContentFilterDrop .nDropDown",rearrangeContentBy)
			   /*.on("click",".instLibFacetHolder .showContentBy input",showContentBy)*/
			   .on("click",".instLibViewTypes span",viewTypeChanged)
			   .on("click",".hideShowInstLibCntrls",hideShowMainCntrls)
			   .on("keydown",".instLibSearchBox input",function(e){
				var $this = $(this);
				if(e.which == 13){
					params.query = $(e.currentTarget).val();
					instLibrary.myContents.fetchDataOnChange();
				}
				setTimeout(function(){
				   if(!$this.val() && params.query){
					$(params).removeProp("query");
					instLibrary.myContents.fetchDataOnChange();
				   }
				});
			    });
		$(parDivId).data("libInited",true);
	   };
	};
};

