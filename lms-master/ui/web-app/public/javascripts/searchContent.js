$(document).ready(function(){
	searchContent.init();
});

var searchContent = {
	entityType:"QUESTION",
	entityName:"Questions",
	suggestionVisibility:false,
	params:{"start":0,"size":10,"eType":"QUESTION","brdId":"",query:"","facet":false,"target":"SEARCH_CONTENT",brdIds:[]},
	enitityMap:{"QUESTION":"Questions","USER":"People","TEST":"Tests","PLAYLIST":"Playlists","DOCUMENT":"Documents"},
	init:function(){
		var wWidth = $(window).innerWidth();
		var wleft = (1001 - wWidth)/2;
		$(".topSearchContentBG").css({left:wleft,width:wWidth});
		this.registerFns();
		searchContent.updateETypeUi();
	},
	registerFns:function(){
		$("#searchContentTextBox")
			.click(this.onSearchQueryChange)
		//	.focusout(this.hideSuggestions)
			.keyup(this.onSearchQueryChange)
		$(".searchSuggestionParent")
			.on("click",".eachSearchContentSuggestions",this.suggestionClicked)
			.on("mouseenter mouseleave",".eachSearchContentSuggestions",this.suggestionHover);
		$(document).on("change",".searchFacetTargetCheckbox",this.brdsChanged);
	},
	showSuggestions:function(){
		$(document).off("click",this.hideSuggestions);
		$(document).on("click",this.hideSuggestions);
		$(".searchSuggestionParent").removeClass("nonner");
	},
	hideSuggestions:function(e,from){
		if((e && e.srcElement && e.srcElement.id=="searchContentTextBox")||
				(e&&$(e.target).closest(".searchContentSuggestionHolder").length>0)) return;
		$(".searchSuggestionParent").addClass("nonner");
		$(".searchContentSuggestionHolder").html("");
		searchContent.suggestionVisibility = false;	
		$(document).off("click",this.hideSuggestions);
	},
	onSearchQueryChange:function(e){
		var text = $(e.currentTarget).val();
		if(searchContent.timerObj) clearTimeout(searchContent.timerObj);
		switch(e.which){
			case 13: 
				var elem = $(".searchContentSuggestionHolder").find(".eachSearchContentSuggestionsSelected");
				var brdId = elem.data("brdId");
				var userId = elem.data("userId");
				if(userId){
					searchContent.hideSuggestions();
					searchContent.params.query = "";
					$("#searchContentTextBox").val("");
					openUserProfile(userId);
				}else{
					searchContent.doSearch(text,brdId);
				}
				break;
			case 38: searchContent.prevSuggestion();
				 break;
			case 40: searchContent.nextSuggestion();
				 break;
			default: searchContent.getSuggestions(text);
				 return;
		}
	},
	nextSuggestion:function(){
		if(!searchContent.suggestionVisibility) return;
		var lastTarget = $(".searchContentSuggestionHolder").find(".eachSearchContentSuggestionsSelected").removeClass("eachSearchContentSuggestionsSelected");
		if(!lastTarget.get(0)){
			lastTarget = $($(".eachSearchContentSuggestions").get(0)).addClass("eachSearchContentSuggestionsSelected");
		}else{
			lastTarget.next().addClass("eachSearchContentSuggestionsSelected");
		}
	},
	prevSuggestion:function(){
		if(!searchContent.suggestionVisibility) return;
		var lastTarget = $(".searchContentSuggestionHolder").find(".eachSearchContentSuggestionsSelected").removeClass("eachSearchContentSuggestionsSelected");
		var suggList = $(".searchContentSuggestionHolder").find(".eachSearchContentSuggestions");
		if(!lastTarget.get(0)){
			$(suggList.get(suggList.length-1)).addClass("eachSearchContentSuggestionsSelected");
		}else{
			lastTarget.prev().addClass("eachSearchContentSuggestionsSelected");
		}
	},
	suggestionHover:function(e){
		if(!searchContent.suggestionVisibility) return;
		var lastTarget = $(".searchContentSuggestionHolder").find(".eachSearchContentSuggestionsSelected").removeClass("eachSearchContentSuggestionsSelected");
		$(e.currentTarget).addClass("eachSearchContentSuggestionsSelected");
	},
	getSuggestions:function(text){
		searchContent.timerObj = setTimeout(function(){
			if(text.length>=1){
				var params = {"query":text,"eType":searchContent.entityType,"entityName":searchContent.entityName,"size":10};
				$(".searchContentSuggestionHolder").load("/SearchContent/getSuggestions",params,function(response,stat){
					searchContent.showSuggestions();	
					searchContent.suggestionVisibility = true;	
				});
			}else{
				//searchContent.hideSuggestions();
				searchContent.showSuggestions();	
				if(searchContent.entityType == "USER"){
					$(".searchContentSuggestionHolder")
					.html("<span class='searchContentSuggestionSummary big13 relative left'>Search <b>"+searchContent.entityName+"</b></span>");
				}else{
					$(".searchContentSuggestionHolder").html("<span class='searchContentSuggestionSummary big13 relative left'>Search <b>Exam , Subjects , Topics , Sub-Topics</b> in <b>"+searchContent.entityName+"</b></span>");
				}
			}
		},1);
	},
	suggestionClicked:function(e){
		var brdId = $(e.currentTarget).data("brdId");
		var userId = $(e.currentTarget).data("userId");
		if(userId){
			searchContent.params.query = "";
			$("#searchContentTextBox").val("");
			openUserProfile(userId);
			searchContent.hideSuggestions();
			pushHistory(null , null,"/user/"+userId);
		}else if(brdId){
			searchContent.doSearch(null,brdId);
		}		
	},
	doSearch:function(text,brdId,externalEntity,noHistoryPush){
		this.hideSuggestions();
		externalEntity = externalEntity?externalEntity:searchContent.entityType;
		var params = searchContent.params;
		params["start"]=0;params["size"]=10;params["eType"]=externalEntity;params["brdId"]="";params["query"]="";params["facet"]=true;
		params = $(params).removeProp("includeTypes[0]").get(0);
		params = $(params).removeProp("excludeTypes[0]").get(0);
		if(searchContent.includeTypes){
			params["includeTypes[0]"] = searchContent.includeTypes;
		}else if(searchContent.excludeTypes){
			params["excludeTypes[0]"] = searchContent.excludeTypes;
		}
		if(brdId){
			params.brdId = brdId;
		}else if(text){
			params.query = text;
		}else{
			return false;
		}
    		bigLoader($("#noTabSection"));
    		showTopLoader();
    		showNoTabSec("SEARCH_CONTENT");
		$("#noTabSection").load("/SearchContent/getSearchResult",params,function(){
			if(!noHistoryPush)
				pushSearchHistory(params);
    			hideTopLoader();
			params["brdIds"] = [];
			params["tags"] = [];
			params["targetIds"] = [];
			/*$("#searchContentPage").find(".searchContentResults").html($(this).find(".searchContentResults").html());
			$("#searchContentPage").find(".searchContentRHS").html($(this).find(".searchContentRHS").html());
				/*.find(":checkbox").each(function(){ var val = $(this).val();
					if(val && val!="select-all"){	
						params[$(this).data("brdTag")].push(val);
					}
				});*/
			searchContent.initResultPagination();
			searchContent.params["facet"]=false;
			searchContent.updateETypeUi();
		});
		return true;
	},
	updateETypeUi:function(){
	try{
    		var params = getAllUrlParams();
		if(params['query']){
			$("#searchContentTextBox").val(urlDecode(params['query']));
		}
		var eType = params['eType'];eType = eType.toUpperCase();var eName = searchContent.enitityMap[eType];
		$(".selectSearchContentType").resetVSelectTag({'newVal':{'text':eName,'val':eType}});
	}catch(err){
		$("#searchContentTextBox").val("");
		var eType = "QUESTION";var eName = searchContent.enitityMap[eType];
		$(".selectSearchContentType").resetVSelectTag({'newVal':{'text':eName,'val':eType}});
	}
	},
	onPageShift:function(obj){
		window.scrollTo(0,100);
	},
	initResultPagination:function(){
		var totalCount = $(".globalSearchHitCount").val();totalCount=totalCount?totalCount:0;
		var params = {leftArrowId:"#searchContentPage .myContentsHeadLeftArrow",disableClassLeft:"leftArrowDisabled",rightArrowId:"#searchContentPage .myContentsHeadRightArrow",disableClassRight:"rightArrowDisabled",start:0,size:10,max:totalCount,itemsPerPage:10,scrollDir:'horizontal','fetchUrl':"/SearchContent/getSearchBody",otherParams:searchContent.params,callBack:searchContent.onPageShift};
		$("#searchContentPage").find(".searchContentResults").setForPagination(params);
	},
	fetchNonFacetServerData:function(){
		searchContent.params["start"]=0;searchContent.params["size"]=10;
    		smallLoader($("#searchContentPage").find(".searchContentResults"));
		$("#searchContentPage").find(".searchContentResults").html("").load("/SearchContent/getSearchBody",searchContent.params,function(){
			searchContent.initResultPagination();
		});
	},
	brdsChanged:function(e){
		var val = $(e.currentTarget).val();var checked = e.currentTarget.checked;
		var brdTag = $(e.currentTarget).data("brdTag");
		var paramTag = searchContent.params[brdTag];
		if(val=="select-all"){
			var parTag = $(e.currentTarget).closest(".searchFacetsOptionsList");
			parTag.find(":checkbox").attr("checked",true);
		}else{
			if(checked){
				paramTag.push(val);
			}else{
				paramTag.splice(paramTag.indexOf(val),1);	
				$(e.currentTarget).closest(".searchFacetsOptionsList").find(".selectAllChkBox").attr("checked",false);
			}
		}
		if(searchContent.searchTimerObj) clearTimeout(searchContent.searchTimerObj);
		searchContent.searchTimerObj = setTimeout(function(){searchContent.fetchNonFacetServerData()},1000);
	}
}
function searchContentTypeChange(value,targetElem,targetValue){
	searchContent.entityType = targetValue;
	searchContent.entityName = value;
	searchContent.includeTypes = $(targetElem).data("includeTypes");
	searchContent.excludeTypes = $(targetElem).data("excludeTypes");
	//clickStream.record("SEARCH BAR","ENTITY TYPE","CLICK",{'searchEntityName':value});
}
function pushSearchHistory(params){
	var url = "/search?facet=true";
	for(p in params){
		if(params[p] && params[p].length>0){
			url += "&"+p+"="+params[p];
		}
	}
	pushHistory(null,null,url);
}
