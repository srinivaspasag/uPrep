var myContents = {
	url:{"playlists":"/MyContents/getPlaylists","documents":"/MyContents/getDocuments","tests":"/MyContents/getTests","videos":"/MyContents/getVideos"},
	params:{"orderBy":'views',"query":"","resultType":"ALL","sortOrder":"DESC","start":0},
	divUrl:{"myContentsPlaylist":"/MyContents/getPlaylists","myContentsDocumentslist":"/MyContents/getDocuments","myContentsTestslist":"/MyContents/getTests","myContentsVideoslist":"/MyContents/getVideos"},
	divLinkage:{"PLAYLISTS":{id:"myContentsPlaylist",index:1},"DOCUMENTS":{id:"myContentsDocumentslist",index:2},"TESTS":{id:"myContentsTestslist",index:3},"VIDEOS":{id:"myContentsVideoslist",id:4}},
	isMyContents:true,
	fetchSize:3,
	resetVals:function(){
		this.params = {orderBy:'views',query:"",resultType:"FOLLOWING",sortOrder:"DESC"};
		this.isMyContents = this.targetUserId = this.isExploreContent = undefined;
	},
	shareWithInst:function(e){
		var data = $(this).closest('.myContentsEachItem').data();
		shareUi.open(data['entityId'],data['entityType'],undefined,data['allowPublic'],true);
 	},
	relativePath:'mycontent',
	init:function(){
		this.resetVals();
		this.isMyContents = isMyContents;
		this.targetUserId = targetUserId;
		this.isExploreContent = isExploreContent;
		if(targetUserId){ this.params.targetUserId = targetUserId;}
		if(isExploreContent){ 
			this.params.exploreContentPage = isExploreContent; this.params.resultType = "ALL";
			this.relativePath = "explorecontent";
		}
		//this.fetchSize = isMyContents?3:6;
		$(".contentsTabs").on("click",".SSCTab",this.decideNavTab);
		$(".FilterForTestOnly").on("click",".SSCTab",this.filterTestData);
		$(".filterMyContentsSearchBy").on("click",".depthTab",this.filterContentSearch);
		$(".chooseContents").on("click","span",this.chooseContent);
		$("#myContents").on("click",".myContentsShareIt",this.shareWithInst);
		$(".searchMyContents").keydown(function(e){
			if(e.which == 13){
				myContents.params.query = $(e.currentTarget).val();
				myContents.putTimerOn(myContents.fetchDataOnChange,500);
			}
		});
		this.initNextPrevNav();
		//this.initRecommendationNextPrev();
		vSelectFns.init();
		try{ this.openTabDirectly(openTabId);}catch(err){}
	},
	openTabDirectly:function(tabId){
		var divId = myContents.divLinkage[tabId];
		if(divId){
			$(".contentsTabs").find(".activeSSCTab").removeClass("activeSSCTab");
			$(".myContentsLHS").find(".myContentsListsHolderPagination").removeClass("myContentsListsHolderPagination");
			$($(".contentsTabs").find(".SSCTab").get(divId.index)).addClass("activeSSCTab");
			
			myContents.hideAll();
			myContents.initPaginationOn(divId.id,true);
		}
		myContents.onTabChange(tabId);
	},
	onTabChange:function(tabId){
		if(!tabId) return false;
		tabId = tabId.toUpperCase();
		if(tabId == "TESTS"){
			$(".topBarMyContents").find(".FilterForTestOnly").removeClass("nonner");
			myContents.resetTestFilter();
		}else{
			$(".topBarMyContents").find(".FilterForTestOnly").addClass("nonner");
		}
	},
	resetTestFilter:function(){
		$(".FilterForTestOnly").find(".activeSSCTab").removeClass("activeSSCTab");
		$($(".FilterForTestOnly").find(".SSCTab").get(0)).addClass("activeSSCTab");
	},
	resetParams:function(){
		$(".searchMyContents").val("");
		myContents.params.query = "";
		myContents.params.orderBy = "views";
		$(".contentsTabs").find(".SSCSelect").resetVSelectTag({'newVal':{'text':'Most Popular','val':'views'}});
	},
	prepareParamsForDiscreteScroll:function(parId,fetchUrl,fetchSize,noInitialFetch){
		var maxLimit = $(parId+" .resultDataCount").val();
		maxLimit = maxLimit?parseInt(maxLimit):0;
		fetchSize = fetchSize!=undefined?fetchSize:myContents.fetchSize;
		var params = {leftArrowId:parId+" .myContentsHeadLeftArrow",disableClassLeft:"leftArrowDisabled",rightArrowId:parId+" .myContentsHeadRightArrow",disableClassRight:"rightArrowDisabled",start:0,size:fetchSize,max:maxLimit,itemsPerPage:fetchSize,scrollDir:'horizontal','fetchUrl':fetchUrl,otherParams:myContents.params,callBack:"","noInitialFetch":noInitialFetch};
		if(maxLimit){
			if(!myContents.currentPaginatedDiv){ $(parId).removeClass("nonner");}
		}else if(("."+myContents.currentPaginatedDiv)!=parId){
			$(parId).addClass("nonner");		
		}
		return params;
	},
	initRecommendationNextPrev:function(){
		var maxLimit = $(".contentRecommendationList .resultDataCount").val();
		maxLimit = maxLimit?parseInt(maxLimit):0;
		var params = {leftArrowId:".recommendationLeftArrow",disableClassLeft:".recommendationDisableArrowLeft",rightArrowId:".recommendationRightArrow",disableClassRight:".recommendationDisableArrowRight",start:0,size:3,max:maxLimit,itemsPerPage:3,scrollDir:'horizontal','fetchUrl':"/MyContents/getRecommendations",otherParams:{"sortOrder":"DESC","resultType":"ALL"},callBack:""};
		$(".contentRecommendationList").setForDiscreteScrolling(params);
		var currCount = maxLimit<3?maxLimit:3;
		$(".recommendationCurrentCount").text(currCount+"/"+maxLimit);
	},
	initNextPrevNav:function(){
		myContents.currentPaginatedDiv = "";
		
		var params = myContents.prepareParamsForDiscreteScroll(".myContentsPlaylist","/MyContents/getPlaylists",null,true);
		$(".myContentsPlaylistsIconsHolder").setForDiscreteScrolling(params);

		params = myContents.prepareParamsForDiscreteScroll(".myContentsTestslist","/MyContents/getTests");
		$(".myContentsTestsIconsHolder").setForDiscreteScrolling(params);

		params = myContents.prepareParamsForDiscreteScroll(".myContentsDocumentslist","/MyContents/getDocuments",null,true);
		$(".myContentsDocumentsIconsHolder").setForDiscreteScrolling(params);

		params = myContents.prepareParamsForDiscreteScroll(".myContentsVideoslist","/MyContents/getVideos",null,true);
		$(".myContentsVideosIconsHolder").setForDiscreteScrolling(params);
	},
	hideAll:function(){
		$(".myContentsEachContentList").addClass("nonner");
	},
	showAll:function(changeTab){
		if(changeTab){
			$(".contentsTabs").find(".activeSSCTab").removeClass("activeSSCTab");
			$($(".contentsTabs").find(".SSCTab").get(0)).addClass("activeSSCTab");
		}
		$(".myContentsEachContentList").removeClass("nonner");
	},
	filterTestData:function(e){
		$(".FilterForTestOnly").find(".activeSSCTab").removeClass("activeSSCTab");
		$(e.currentTarget).addClass("activeSSCTab");
		var id = $(e.currentTarget).data("tabId");
    		myContents.showLoader(".myContentsTestsIconsHolder");
		myContents.params["attemptType"] = id;
		$(".myContentsTestsIconsHolder").load(myContents.url.tests,myContents.params,function(){
			myContents.decideAndInitNav("myContentsTestslist",0);
			$(myContents.params).removeProp("attemptType");
		});
	},
	decideNavTab:function(e){
		$(".contentsTabs").find(".activeSSCTab").removeClass("activeSSCTab");
		$(".myContentsLHS").find(".myContentsListsHolderPagination").removeClass("myContentsListsHolderPagination");
		$(e.currentTarget).addClass("activeSSCTab");
		var id = $(e.currentTarget).data("tabId");
		var tabName = $(e.currentTarget).text();
		myContents.onTabChange($(e.currentTarget).text());
		/*if(myContents.currentPaginatedDiv){
			//myContents.params.start = 0;myContents.params.size = myContents.fetchSize;
			//$("."+myContents.currentPaginatedDiv+" .myContentsListsHolder").load(myContents.divUrl[myContents.currentPaginatedDiv],myContents.params,function(){});
		}*/
		if(!id){
			myContents.showAll();myContents.initNextPrevNav();
    			pushHistory(null,null,"/"+myContents.relativePath,true);
		}
		else{
			myContents.hideAll();
			myContents.initPaginationOn(id,false);
    			pushHistory(null,null,"/"+myContents.relativePath+"/"+tabName.toLowerCase(),true);
		}
	},
	initPaginationOn:function(id,noInitialFetch){
		$("."+id).removeClass("nonner").find(".myContentsListsHolder").addClass("myContentsListsHolderPagination");
		myContents.currentPaginatedDiv = id;
		var params = myContents.prepareParamsForDiscreteScroll("."+id,myContents.divUrl[id],12,noInitialFetch);
		$("."+id+" .myContentsListsHolder").addClass("myContentsListsHolderPagination").setForPagination(params);
	},
	filterContentSearch:function(e){
		$(e.delegateTarget).find(".activeDepthTab").removeClass("activeDepthTab");
		$(e.currentTarget).addClass("activeDepthTab");
		myContents.resetParams();
		myContents.params.resultType = $(e.currentTarget).data("type");
		myContents.putTimerOn(myContents.fetchDataOnChange,50);
	},
	chooseContent:function(e){
		$(e.delegateTarget).find(".vChoosenTag").removeClass("vChoosenTag");
		$(e.currentTarget).addClass("vChoosenTag");
	},
	resulTypeChanged:function(e){
	},
	putTimerOn:function(method,time){
		if(myContents.timerObj) clearTimeout(myContents.timerObj);
		myContents.timerObj = setTimeout(method,time);
	},
	showLoader:function(divId){
		if(!myContents.currentPaginatedDiv){
			$(divId).closest(".myContentsEachContentList").removeClass("nonner");
		}
		smallLoader(divId);
	},
	updateFetchSize:function(id,params){
		var size = myContents.fetchSize;
		if(id==myContents.currentPaginatedDiv) size = 12;
		params["size"] = size;
		return params;
	},
	fetchDataOnChange:function(){
		myContents.params.start = 0;
		myContents.resetTestFilter();
		//myContents.params.size = myContents.fetchSize;
		//myContents.showAll(true);
    		myContents.showLoader(".myContentsPlaylistsIconsHolder");
    		myContents.showLoader(".myContentsTestsIconsHolder");
    		myContents.showLoader(".myContentsDocumentsIconsHolder");
    		myContents.showLoader(".myContentsVideosIconsHolder");
    		showTopLoader();
		var params = cloneObject(myContents.params);
		$(".myContentsPlaylistsIconsHolder").load(myContents.url.playlists,myContents.updateFetchSize("myContentsPlaylist",params),function(){
			myContents.decideAndInitNav("myContentsPlaylist",0);
			$(".myContentsTestsIconsHolder").load(myContents.url.tests,myContents.updateFetchSize("myContentsTestslist",params),function(){
				myContents.decideAndInitNav("myContentsTestslist",0);
				$(".myContentsDocumentsIconsHolder").load(myContents.url.documents,myContents.updateFetchSize("myContentsDocumentslist",params),function(){
					myContents.decideAndInitNav("myContentsDocumentslist",0);
					$(".myContentsVideosIconsHolder").load(myContents.url.videos,myContents.updateFetchSize("myContentsVideoslist",params),function(){
						myContents.decideAndInitNav("myContentsVideoslist",0);
					});
				});
			});
		});
	},
	initDiscreteScrollOn:function(id){
		var params = myContents.prepareParamsForDiscreteScroll("."+id,myContents.divUrl[id]);
		$("."+id+" .myContentsListsHolder").setForDiscreteScrolling(params);
	},
	decideAndInitNav:function(id,timerTime){
    		hideTopLoader();
		timerTime = timerTime?timerTime:0;
		if(myContents.currentPaginatedDiv == id){
			myContents.putTimerOn("myContents.initPaginationOn('"+id+"',false)",timerTime);
		}else{
			myContents.putTimerOn("myContents.initDiscreteScrollOn('"+id+"')",timerTime);
		}
	}
};
function myContentsTypeChange(value,target,data){
	myContents.params.orderBy = data;
	myContents.putTimerOn(myContents.fetchDataOnChange,500);
	//clickStream.record("CONTENT_HOLDER","FILTER_BY_SELECT","CHANGE",{'selected':value});
}
function removeFromMyContentCB(elem,response,stat){
        if(stat=="success" && response.result){
                $(elem).closest(".myContentsEachListItems").fadeTo(500,0,function(){
                	var eType = $(this).data("entityType");
			switch(eType){
				case "playlist":
    					myContents.showLoader(".myContentsPlaylistsIconsHolder");
					$(".myContentsPlaylistsIconsHolder").load(myContents.url.playlists,myContents.params,function(){
						myContents.decideAndInitNav("myContentsPlaylist");
					});
					break;
				case "document":
    					myContents.showLoader(".myContentsDocumentsIconsHolder");
					$(".myContentsDocumentsIconsHolder").load(myContents.url.documents,myContents.params,function(){
						myContents.decideAndInitNav("myContentsDocumentslist");
					});
					break;
				case "test":
    					myContents.showLoader(".myContentsTestsIconsHolder");
					$(".myContentsTestsIconsHolder").load(myContents.url.tests,myContents.params,function(){
						myContents.decideAndInitNav("myContentsTestslist");
					});
					break;
				case "video":
    					myContents.showLoader(".myContentsVideosIconsHolder");
					$(".myContentsVideosIconsHolder").load(myContents.url.videos,myContents.params,function(){
						myContents.decideAndInitNav("myContentsVideoslist");
					});
					break;
			}
		});
        }
};
$(function(){
	myContents.init();
});
