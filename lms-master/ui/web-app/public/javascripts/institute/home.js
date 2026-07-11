var institute = new function($){
	var parDivId = "#instituteHome";var openedDiscuss;
	var params = {'tabType':"MY_INSTITUTE","year":new Date().getFullYear()};
	var memberSearchXHR;
	
	var getInstParams = function(){
		var p = cloneObject(params);
		$(p).removeProp('role');$(p).removeProp('start');$(p).removeProp('size');
		return p;
	};
	this.goToHome = function(){
		openInstSubPage("/Institute/homeFromInside","",{});
	}
	this.goToUrl = function(url,append,params){
		openInstSubPage(url,append,params);
	}
	var openInstSubPage = function(url,history,params,callBack,childHolderId){
		showTopLoader();
		beforePageOpen();
		params["newPageOpen"] = pushInHistory(history);
		vReq.get(url,params,function(data){
			hideTopLoader();
			if(childHolderId){
				$(parDivId).find(childHolderId).html(data);
			}else{
				$(parDivId).html(data);
			}
			if(callBack){
				try{
					callBack(data);
				}catch(err){
					putConsoleError(err);
				}
			}
			instHeader.getCount();
		});
		vtooltip.hide();
		window.scrollTo(0,0);
	};	
	var openInstHomeSubPage = function(url,history,params,callBack){
		openInstSubPage(url,history,params,callBack,".instMainCol");
	};	
	var openResultAnalytics = function(){
		openInstSubPage("/Institute/testAnalytics","resultanalytics",params);
	};
	var openAllNotifications = function(){
		$("#instPageNavigator").find(".clInstTab").removeClass("clInstTab");
		openInstSubPage("/Institute/openAllNotifications","notifications",params);
	};
	var openInstProfile = function(){
		$("#instPageNavigator").find(".clInstTab").removeClass("clInstTab");
		var userId=$(this).data("userId");
		if(!userId) return false;
		params.targetUserId=userId;
		openInstSubPage("/Institute/profile","profile/"+userId,params);
	}
	var openInstAvailPrograms = function(){
		openInstSubPage("/Institute/openInstAvailPrograms","myprograms",params);
	};
	var openChallenges = function(e){
		var p = cloneObject(params);
		p["tabType"]="CHALLENGES_PAGE";
		p["contentSrc"]=params["contentSrc"];
		openInstSubPage("/Challenges/challenges","challenges",p);
		if(e) e.preventDefault();
	};
	var openMySchedule = function(e){
		//openInstSubPage("/Institute/myschedule","myschedule/",params);
		institute.schedules.open();
		if(e) e.preventDefault();
	};
	this.openLibrary = function(){
		openInstSubPage("/Institute/library","library",params);
	};
	this.openClassroomConnect = function(){
		openInstSubPage("/Institute/myClassroomConnect","schedule",params);
	}
	this.openTestPage = function(testId,extraParams){
		var url = "test/"+testId;
		var p = cloneObject(params);p["id"] = testId;
		if(extraParams){
			url = url+"?";
			for(pt in extraParams){
				url+=pt+"="+extraParams[pt]+"&";	
			}
			p = $.extend(true,p,extraParams);	
		}
   		p["noInstHeader"] = "true";
		openInstSubPage("/Tests/getOrgTest",url,p,function(){
			window.scrollTo(0,0);	
		});
	};
	this.openTestLeaders = function(testId){
		var p = cloneObject(params);p["testId"] = testId;
		openInstSubPage("/Institute/getTestLeaders","testleaders/"+testId,p);
	};
	var rteImageOpen = function(e){
		showImagePreview($(this).html());
		if(e) e.preventDefault();
		return false;
	};
	pushInHistory = function(append){
		var orgId = $("#myInstitutePage").data("orgId");
		return pushInstHistory(orgId,append);
	};
	this.showWidgets = function(clsNm,show){
		var widgetPar = $("#instHomeWidgetsContainer");
		if(show){
			widgetPar.find(clsNm).fadeTo(150,1);
		}else{
			widgetPar.find(clsNm).fadeTo(10,0).hide();
		}
	};
	this.initWidgets = function(pageId){
		institute.showWidgets(".instHomeLftWidget");
		switch(pageId){
		    case "SCHEDULE":
			institute.showWidgets(".instSchedulePgWidget",true);
			break;
		    case "ACTIVITY":
			institute.activities.widgets();
			//getScheduleWidget();
			break;
		}
	};
	var headerTabClick = function(){
		$("#instPageNavigator").find(".clInstTab").removeClass("clInstTab");
		$(this).addClass("clInstTab");
	};
	var sideTabClicked = function(){
		$(this).addClass("active").siblings().removeClass("active");
	};
	var attachThumbPrev = function(){
		moveThumbNextPrev($(this),-1);
	};
	var attachThumbNext = function(){
		moveThumbNextPrev($(this),1);
	};
	var moveThumbNextPrev = function($this,addBy){
		if(!$this.hasClass("active")) return;
		var holder = $this.closest(".instThumbNextPrevHold");
		var par = $this.closest(".instLinkPreview");
		var index = holder.data("count")+addBy;
		var total = holder.data("total");
		if(index < total-1){
			holder.find(".instAttachLinkNext").addClass("active");		
		}else{
			holder.find(".instAttachLinkNext").removeClass("active");	
		}
		if(index>0){
			holder.find(".instAttachLinkPrev").addClass("active");		
		}else{
			holder.find(".instAttachLinkPrev").removeClass("active");		
		}
		var width = par.find(".instAttachLinkImgContainer").width()+3;
		var left = index *(-1)*width;
		par.find(".instAttachLinkAllImgs").animate({"left":left+"px"});
		holder.data("count",index);
		holder.find(".instAttachThumbCount").find("#curCount").text(index+1);
		holder.closest(".thumbnailCell").data("index",index);
	};
	var homeImgLoaded = function(){
		$(this).fadeTo(120,1);
		$(this).closest(".instProfilePicContainer").addClass("imgLoaded");
	};
	var homeImgError = function(){
		this.error = true;
	};
	var showHomeImgs =  function(picId){
		showImgsFwk(picId,homeImgLoaded,homeImgError);
	};
	var showImgsFwk = function(picId,successFn,errorFn){
		var pics = $(parDivId).find(picId)
			.load(successFn)
			.error(errorFn);
		setTimeout(function(){
			pics.each(function(){
				if(this.complete && !this.error){
					$(this).load();
				}
			});
		},1000);
	};
	this.readUrlForProgInfo = function(progInfo){
		var data = urlQueryHelper.readAll();
		progInfo = $.extend({"progId":"","center":"","section":""},progInfo);
		if(!data.program){
			nDropDown.reset($(".instSelMyProgram"));
		}else{
		   var obj = nDropDown.searchAndUpdate($(".instSelMyProgram"),data.program,true);
		   if(obj){
			progInfo["progId"] = obj.value;
		   }
		}
		if(!data.center){
			nDropDown.reset($(".instSelMyCenter"));
		}else{
		   var obj = nDropDown.searchAndUpdate($(".instSelMyCenter"),data.center,true);
		   if(obj){
			progInfo["center"] = obj.value;
		   }
		}
		if(!data.section){
			nDropDown.reset($(".instSelMySection"));
		}else{
		   var obj = nDropDown.searchAndUpdate($(".instSelMySection"),data.section,true);
		   if(obj){
			progInfo["section"] = obj.value;
		   }
		}
		return progInfo;
	};
	var programChanged = function(){
		var $this = $(this);
		var progId = $this.data("value");
		var item = $this.data("itemValue");
		var allowSelectAll = $this.closest(".instSelMyBatch").data("allowSelectAll");
		if(item && item.centers && item.centers.length>0){
			var holder = $(parDivId).find(".instSelMyCenter");
			allowSelectAll = holder.data("allowSelectAll");
			var sectionHolder = $(parDivId).find(".instSelMySection");
			/*var centers = "{data:"+JSON.stringify(item.centers)+"}";
			var pr = {"list":centers,"listType":"Centers","allowSelectAll":allowSelectAll};
			holder.html("<div>Loading...</div>");
			$.post("/Institute/drawLibraryDropList",pr,function(data){
				holder.html(data);
				allowSelectAll = sectionHolder.data("allowSelectAll");
				var sectionItem = holder.find(".eachDropedElement:first");
				sectionDropDownUpdate(allowSelectAll,sectionItem.data("itemValue"));
			});*/
			nDropDown.redraw(holder,allowSelectAll,item.centers,"Centers",false);	
			allowSelectAll = sectionHolder.data("allowSelectAll");
			var sectionItem = holder.find(".eachDropedElement:first");
			sectionDropDownUpdate(allowSelectAll,sectionItem.data("itemValue"));
		};
	};
	var sectionDropDownUpdate = function(allowSelectAll,item){
		var sectionHolder = $(parDivId).find(".instSelMySection");
		var list = item?item.sections:undefined;
		nDropDown.redraw(sectionHolder,allowSelectAll,list,"Sections",false);	
		/*if(item && item.sections && item.sections.length>0){
			var sections = "{data:"+JSON.stringify(item.sections)+"}";
			var pr = {"list":sections,"listType":"Sections","allowSelectAll":allowSelectAll};
			sectionHolder.html("<div>Loading...</div>");
			$.post("/Institute/drawLibraryDropList",pr,function(data){
				sectionHolder.html(data);
			});
		}*/
	};
	var centerChanged = function(){
		var $this = $(this);
		var centerId = $this.data("value");
		var item = $this.data("itemValue");
		var allowSelectAll = $this.closest(".instSelMyBatch").data("allowSelectAll");
		sectionDropDownUpdate(allowSelectAll,item);
	};
	this.init = function(homePgVal){
		//params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		params['contentSrc'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		if($(parDivId).data("inited")) return false;
		$("#instTopBar")
				 .off('click', '.instNotiSeeAll')
		         .on("click",".instNotiSeeAll",openAllNotifications);
		$("#instTopBar")
		         .off('click', '.openInstProfile')
		         .on("click",".openInstProfile",openInstProfile);
		$("#instPageNavigator").on("click",".instNavTab",headerTabClick);
		$("#instituteHeader")
                	.on("click",".openInstPage,.instOpenAgain",goToMyInstitutePage)
                 .on("click",".instOpenAnalytics",openResultAnalytics)
                 .on("click",".instOpenMySchedule",openMySchedule)
                 .on("click",".instOpenLibraryPage",this.openLibrary)
                 .on("click",".instDoubtsForum",this.doubts.open)
                 .on("click",".instRATab",this.activities.open)
                 .on("click",".openClassroomConnectTab",this.openClassroomConnect)
                 .on("click",".openInstAvailPrograms",openInstAvailPrograms)
            .on("click",".instOpenChallenges",openChallenges)
			.on("click",".openInstiTestPage",instTestClicked)
			.on("click",".openInstiTestToppers",instTestToppersClicked);
		$(".instHomeLinksHold")
			.on("click",".instDbtTab",this.doubts.open)
			.on("click",".instRATab",this.activities.open)
			.on("click",".instSchTab",this.schedules.open)
			.on("click",".instPplTab",this.people.open)
			.on("click",".instHomeTab",sideTabClicked);
		$(parDivId).on("click",".instAttachLinkPrev",attachThumbPrev)
			.on("click",".instAttachLinkNext",attachThumbNext)
			.on("click",".openInstDoubt",this.oneDbt.open)
			.on("click",".goToInstChallengesPage",openChallenges)
			.on("change",".instSelMyProgram .nDropDown",programChanged)
			.on("change",".instSelMyCenter .nDropDown",centerChanged)
			.on("click",".instItemStgs",openStngs)
			.on("mouseleave",".instEachDbt,.eachInstFeedHolder",closeStngs)
			.data("inited",true);
		//this.activities.open();
		var orgId = $("#myInstitutePage").data('orgId');
		//Remove this If condition if you want QR Code to be available for all organizations
		// if(orgId == LEARNPEDIA_ID ){
			var qrData= "";
			var url = [location.protocol, '//', location.host, location.pathname].join('')
			var urlSplitString = url.split("/");
			var entityId = urlSplitString[urlSplitString.length-1];
			var entityType = urlSplitString[urlSplitString.length -2].toUpperCase();
			qrData = getQrHTML(orgId,entityType,entityId);
			if($("#instituteHome").find("#mainMsgHolder").length == 0){
				switch(entityType){
					case "DOCUMENT":
					case "VIDEO":
						$("#instituteHeader").find("#instituteHome").prepend(qrData);
						break;
					case "MODULE":
						$("#instituteHeader").find("#modulePage #modulePageHeader").prepend(qrData);
						break;
					case "PRETEST":
						entityType = "TEST";
						qrData = getQrHTML(orgId,entityType,entityId);
						$("#instituteHeader").find("#instituteHome").prepend(qrData);
						break;
					case "TEST":
						$("#instituteHeader").find("#postTest .postTestDetails").prepend(qrData);
						break;
				}
				$("#instituteHome").on("click",".scanQrimagePreview",scanQrimagePreview);
			}
		// }
	};

	var getQrHTML = function(orgId,entityType,entityId){
		var qrData = "<div class='qrCodeScan' title='Scan and Learn' style='cursor:pointer;position:absolute'>"
					+"<img class='scanQrimagePreview' "
					+"src='https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="+baseUrl+"downloadapp/"+orgId+"/"+entityType+"/"+entityId+"' "
					+"alt='Qr Code' width='60' height='60'>"
					+"</div>";
		return qrData;
	}

	var scanQrimagePreview = function(){
		var popup = showVPopup();
		popup.append("<h3>Scan to watch on our app</h3>");
		popup.append($(".scanQrimagePreview").clone().prop("width","200").prop("height","200"));
	}
	var openStngs = function(){
		$(this).siblings(".instItemStgsDropped").toggleClass("nonner");
	};
	var closeStngs = function(){
		$(this).find(".instItemStgsDropped").addClass("nonner");
	};
	var closeAllStg = function(){
		$(parDivId).find(".instItemStgsDropped").addClass("nonner");
	};
	
	var deleteItem = function(parentDiv,url,$this,cbFn){
		var ret = confirm(i18nJS("CONFIRM_DELETE_POPUP_TXT"));
		if(!ret){
			closeAllStg();
			return;
		};
		var pr = {id:$this.data("id")};
		var parentFeed = $this.closest(parentDiv);
		vReq.get(url,pr,function(data){
			var deleted = false;
			if(data && data.errorCode=="" && data.result.deleted == true){
				parentFeed.remove();
				deleted = true;
			}else{
				closeAllStg();	
			}	
			try{
				if(cbFn){
					cbFn(deleted,data);
				}
			}catch(err){console.error(err);}
		},function(){
			closeAllStg();	
		});
	};
	var deleteDoubt = function(){
		deleteItem(".instEachDbt","/Institute/deleteDoubt",$(this));
	};
	var getScheduleWidget = function(){
		var p = {};
		p["fromTime"] = (new Date()).setHours(0,0,0,0);
		p["tillTime"] = (new Date()).setHours(24*3-1,59,59,999);
		$.get("/Institute/scheduleWidget",p,function(data){
			$("#instHomeWidgetsContainer")
				.find(".instUpcomingsWidget").html(data);
			institute.showWidgets(".instUpcomingsWidget",true);
		});
	};
	var animate = this.animate = function(dbt,thisOnly){
		dbt.fadeTo(180,1,function(){
			if(!thisOnly){
				animate($(this).next());
			}
		});
	};
	var toJSON = function(data){
		if(typeof data=="string"){
			return JSON.parse(data);
		}else{
			data = cloneObject(data);
		}
		return data;
	};
	this.notifications = new function(){
		var parDiv;
		var noNotisFoundDiv = "<div class='centerText greyTextColor noMoreNotis'>No Recent Notifications.</div>";
		this.init = function(){
			institute.init();
			parDiv = $("#userAllNotifcations");
			parDiv.on("click",".instGetOlderNotis",getMore);
			get(0);	
		};
		var getMore = function(){
			get($(this).data("nextStart"),"OLD");
		};
		var get = function(start,feedType){
			var holder = parDiv.find(".notificationsHolder");
			feedType = feedType?feedType:"NEW";
			start = start ? start : 0;
			var size = 10;
			var clustered = true;
			var params = {"feedType":feedType,size:size,start:start,"needClustered":clustered};
			if(feedType == "OLD"){
				params["beforeNotificationId"] = holder.find(".notiFeed:last").data("feedId");
			}
			holder.find(".instGetOlderNotisHolder").remove();
    			$.get("/Institute/getNotifications",params,function(data){
			     try{
				var htmlData = "";
        			if(data && data.errorMessage=='' && data.result.list.length>0){
             			   $.each(data.result.list,function(i,feed){
					var aType = feed.info && feed.info.actionType ? feed.info.actionType : feed.eType;
                 			if(newsEntityProcessor.isSupported(aType)){
                    				//htmlData += notiFeedFns[aType].init(feed);
                    				htmlData += newsEntityProcessor.process("NOTIFICATIONS",feed,aType,clustered);
                 			}                
            			   });
        		   	   if(data.result.list.length>=size){
					htmlData += "<div class='instGetOlderNotisHolder'> \n\
						<a class='instGetOlderNotis big16' data-next-start='"+(start+size)+"'>"+i18nJS("SHOW_MORE")+"</a> \n\
						</div>";
			   	   }
				   holder.append(htmlData);
				   MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
				}else{
				   holder.find(".noMoreNotis").remove();
				   holder.append(noNotisFoundDiv);
				}
				parDiv.find(".notisCurCount").text((start+data.result.list.length));
			     }catch(err){
				   holder.find(".noMoreNotis").remove();
				   holder.append(noNotisFoundDiv);
			     }
			}); 
		};
	};
	this.activities = new function(){
		var actParams;
		var feedParams;
		var activityDiv;
		//var videoEmbed = "<embed width='420' height='345' src='%url%' type='application/x-shockwave-flash'></embed>";
		var videoEmbed = "<iframe width='510' height='287' src='%playUrl%' frameborder='0' mozallowfullscreen webkitallowfullscreen allowfullscreen style='background:#000;'></iframe>";
		this.open = function(){
			openInstSubPage("/Institute/activityUi","activities",params);
		};
		var showImgs = function(holder){
			showHomeImgs($(holder).find(".instFeedProfilePic"));
			showImgsFwk($(holder).find(".instActFeedImg"),function(){
				$(this).fadeTo(120,1);
				$(this).closest(".instActFeedImgHolder").addClass("imgLoaded");
			   },
			   function(){
				this.error = true;
			   }
			);
		}
		this.showImgs = showImgs;
		this.showComImgs = function(divId){
			showHomeImgs($(divId).find(".instFeedCommentPic"));
		};
		this.initSingle = function(){
			institute.init();
			actParams = cloneObject(params);
			feedParams = cloneObject(actParams);
			activityDiv = $(parDivId).find(".instShareFeedsHold");
			$(activityDiv)
				.on("click",".instFeedsContainer .instImgPreviewCont",uploadedImageOpen)
				.on("click",".instFeedsContainer .playInstVideo",previewVideo)
				.on("click",".instFeedsContainer .delInstFeed",deleteFeed)
				.on("click",".crossVidPreview",closeVidPreview);
			showImgs($(activityDiv).find(".instFeedsContainer"));
			var feed = $(activityDiv).find(".eachInstFeedHolder:first");
			animate(feed,true);
			setTimeout(function(){
				feed.find(".commentsInstFeedCount").trigger("click");
			},1000);
		};
		this.init = function(){
			institute.init();
			actParams = cloneObject(params);
			feedParams = cloneObject(actParams);
			activityDiv = $(parDivId).find(".instShareFeedsHold");
			if($(activityDiv).data("activityInited")) return false;
			$(".instHomeLinksHold").find(".instRATab").addClass("active").siblings().removeClass("active");
			showHomeImgs($(activityDiv).find(".instCreateShare").find(".instUserPic"));
			$(activityDiv)
				.on("change",".instActivitySelMyBatch",batchChanged)
				.on("click",".instFeedPostContainer .instImgPreviewCont,.instFeedsContainer .instImgPreviewCont",uploadedImageOpen)
				.on("click",".instFeedPostContainer .playInstVideo,.instFeedsContainer .playInstVideo",previewVideo)
				.on("click",".instFeedsContainer .delInstFeed",deleteFeed)
				.on("click",".crossVidPreview",closeVidPreview)
				.on("click",".loadMoreOrgFeeds",loadMore);
			getFeeds(0);
			institute.initWidgets("ACTIVITY");
			$(activityDiv).data("activityInited",true);
		};
		var deleteFeed = function(){
			deleteItem(".eachInstFeedHolder","/Institute/deleteActivityFeed",$(this));
		};
		var uploadedImageOpen = function(e){
			showImagePreview($(this).html());
			if(e) e.preventDefault();
			return false;
		};
		var batchChanged = function(){
			getFeeds(0);
		}
		this.widgets = function(){
			chlgWidget();	
		};
		var previewVideo = function(){
			var $this=$(this);
			var url = $this.attr("rel");
			var playUrl = $this.data("playUrl");
			var previewDiv = $this.closest(".instVideoPreview").find(".videoPlayPreview");
			var embed = videoEmbed.replace("%playUrl%",playUrl);
			previewDiv.find(".vidPreviewEmbedHolder").html(embed);
			previewDiv.removeClass("nonner");
			$this.closest("table").addClass("nonner");
		};
		var closeVidPreview = function(){
			var $this=$(this);
			var url = $this.attr("rel");
			$this.closest(".instVideoPreview").find("table").removeClass("nonner");
			$this.closest(".videoPlayPreview").addClass("nonner").find(".vidPreviewEmbedHolder").html("");
		};
		var chlgWidget = function(){
			var p = {"start":0,"size":5,rankType:"WEEKLY"};
			var chlgWidget = $(parDivId).find(".instChlgBoardWidget");
			$.get("/Institute/challengeLeaderBoard",p,function(data){
				chlgWidget.html(data);
			});
			chlgWidget.on("change",".selectInstChlgTimeSc",chlgRankChng);	
		};
		var chlgRankChng = function(){
			var rankType = $(this).val();
			var p = {"start":0,"size":5,"rankType":rankType};
			var chlgWidget = $(parDivId).find(".instChlgBoardWidget");
			var holderDiv = chlgWidget.find(".instAllChlgLeaders");
			smallLoader(holderDiv);
			$.get("/Institute/challengeLeaderBoardItems",p,function(data){
				holderDiv.html(data);
				chlgWidget.find(".instYourChlgBrdHolder").html("").append(holderDiv.find(".instYourChlgBrd"));
				chlgWidget.find(".instYourChlgBrdHolder").find(".instYourChlgBrd").removeClass("nonner");
			});
		};
		this.addNewFeed =  function(htmlData){
			var ret = true;
			var holder = $(activityDiv).find(".instFeedPostContainer").html(htmlData).addClass("nonner");
			var feed;
			try{ 
				ret = institute.activities.postSuccess;
				feed = institute.activities.postData;
				feed = toJSON(feed);
			}catch(err){ 
				ret=false;
				holder.html("");
			}
			var select = $(".instActivitySelMyBatch .nDropDown").data("itemValue");
			var setTimedFn = function(){};
			function putOutside(){
				holder.removeClass("nonner");
				setTimedFn = function(){
                                	holder.fadeTo(0,250,function(){
                                        	$(this).html("");
                                	});
				};
			}
			function putInside(){
				holder.removeClass("nonner").find(".eachInstFeedHolder").remove();
				holder = $(activityDiv).find(".instFeedsContainer");
				var firstOne = holder.find(".eachInstFeedHolder:first");
				if(firstOne.length>0){
					holder.find(".eachInstFeedHolder:first").before(htmlData);
				}else{
					holder.html(htmlData);
				}
				holder.find(".eachInstFeedHolder:first").addClass("feedJustAdded");
                                holder.find(".feedPostSuccessMsg").remove();
				setTimedFn = function(){
                                	holder.find(".feedJustAdded").removeClass("feedJustAdded");
					$(activityDiv).find(".instFeedPostContainer").html("");
				};
			}
			if(ret && !select){
				putInside();
			}
			else if(ret && feed){
				var shrdWith = feed.sharedWith;
				if(shrdWith.length>0 && shrdWith[0].id && shrdWith[0].type=="PROGRAMME"){
					var id = shrdWith[0].id;
					var selectArr = select.split("_");
					if(id == selectArr[1]){
						putInside();	
					}else{
						putOutside();
					} 
				}else{
					putOutside();
				}
			}else{
				putOutside();
			}
			animate(holder.find(".eachInstFeedHolder:first"),true);
			showImgs(holder);
			setTimeout(setTimedFn,10000);
			return ret;
		};
		var loadMore = function(){
			smallLoader($(this));
			getFeeds(feedParams["start"]+feedParams["size"]);	
		};
		var getFeeds = function(start){
			var holder = $(activityDiv).find(".instFeedsContainer");
			var select = $(".instActivitySelMyBatch").find(".nDropDown").data("itemValue");
			$(activityDiv).find(".instFeedPostContainer").html("");
			if(select){
				select = toJSON(select);
				feedParams["eType"] = select.type;
				feedParams["eId"] = select.id;
			}else{
				feedParams["eType"] = "ORGANIZATION";	
				feedParams["eId"] = $("#myInstitutePage").data("orgId");
			}
			/*feedParams["filter"] = [
				{
					type:"eventType",
					value:"SHARE_ENTITY"
				}
			];*/
			feedParams["size"] = 10;
			feedParams["needClustered"] = true;
			var lastAvail;
			var url = "/Institute/getActivityFeeds";
			if(start){
				feedParams["start"]=start;
				lastAvail = holder.find(".eachInstFeedHolder:last");
				url = "/Institute/getMoreActivityFeeds";
				//feedParams["beforeNewsActivityId"]=lastAvail.data("newsFeedId");
				feedParams["beforeNewsActivityId"]=lastAvail.data("lastNewsFeedId");
			}else{
				feedParams["start"]=0;
				bigLoader(holder);
				$(feedParams).removeProp("beforeNewsActivityId");
			}
			vReq.get(url,feedParams,function(data){
				holder.find(".loadMoreOrgFeeds").remove();
				if(feedParams["start"]>1){
					holder.append(data);
				}else{
					holder.html(data);
				}
				if(!lastAvail){
					lastAvail = holder.find(".eachInstFeedHolder:first");
				}else{
					lastAvail = lastAvail.next();
				}
				animate(lastAvail,false);
				showImgs(holder);
			});	
		};
	};
	this.people = new function(){
		var membersDiv;
		var pParams;
		this.open = function(){
			openInstHomeSubPage("/Institute/membersUi","people",params);
		};
		var showImgs = function(){
			//membersDiv.find(".instProfilePic").load(homeImgLoaded);
			showHomeImgs(membersDiv.find(".instProfilePic"));
		};
		var clearSearchBox = function(forceClear){
			var query = urlQueryHelper.read("query");
			if(forceClear || !query){
				query = "";
			}
			pParams["query"] = query;
			$(".instMemberSearch").find(".searchInputBox").val(query);
			return query;
		};
		this.onBack = function(){
			var tabType = urlQueryHelper.read("tabType");
			clearSearchBox();	
			var progId = urlQueryHelper.read("program");
			if(!progId){
				nDropDown.reset($(".instMembersByDept"));
			} 
			var obj = nDropDown.searchAndUpdate($(".instMembersByDept"),progId);
			if(obj){
				pParams["programId"] = obj.value;
			}
			var courseId = urlQueryHelper.read("course");
			if(!courseId){
				nDropDown.reset($(".instMembersBySub"));
			}
			obj = nDropDown.searchAndUpdate($(".instMembersBySub"),courseId);
			tabType = tabType ? tabType : membersDiv.find(".instMembersType:first").data("type");
			if(tabType){
				membersDiv.find(".instMembersType").each(function(){
					var $this = $(this);
					if($this.data("type") == tabType){
						$this.trigger("click.back");
					}
				});
			}else{
				membersDiv.find(".instMembersType:first").addClass("selected").siblings().removeClass("selected");
				fetchMembers(true);
			}
			return true;
		};
		this.init = function(){
			institute.init();
			vSelectFns.init();
			membersDiv = $(parDivId).find(".instMembersInfo");
                	membersDiv.on("click",".instLoadMoreMembers",fetchMoreMembers)
                		.on("keyup",".instMemberSearch .searchInputBox",membersBySearchText)
                		.on("change",".instMembersByDept .nDropDown,.instMembersBySub .nDropDown",this.getMembers)
                		.on("click.back click",".instMembersType",toogleMemberRoles);
			pParams = cloneObject(params);
			$(".instHomeLinksHold").find(".instPplTab").addClass("active").siblings().removeClass("active");
			this.onBack();
			institute.initWidgets("PEOPLE");
		};
		var toogleMemberRoles = function(e){
			var $this = $(this);
			var role = $this.data("type");
			if(!e.isTrigger || e.namespace != "back"){
				urlQueryHelper.push("tabType",role);	
			}
			var name = $this.data("name");
			$this.closest(".chooseMembersTabs").data("role",role).data("name",name);
			$this.addClass("selected").siblings().removeClass("selected");
			if(role == "STUDENT"){
				membersDiv.find(".instMembersBySub").fadeTo(100,0,function(){
					$(this).css("display","none");
				});
			}else{
				membersDiv.find(".instMembersBySub").css("display","inline-block").fadeTo(200,1);
			}
			clearSearchBox();	
			fetchMembers(true);
		};
		var fetchMoreMembers = function(){
			smallLoader($(this));
			fetchMembers();
		};
		var membersBySearchText = function(e){
			fetchMembers(true);
		};
		this.getMembers = function(){
			clearSearchBox(true);	
			fetchMembers(true);
			urlQueryHelper.push("program",pParams["programId"]);	
		};
		var memberSearchXHR;
		var fetchMembers = function(freshLoad){
			pParams["targetProfile"] = membersDiv.find(".chooseMembersTabs").data("role");
			pParams["name"] = membersDiv.find(".chooseMembersTabs").data("name");
			if(pParams["targetProfile"] == "TEACHER"){
				var courseId = membersDiv.find(".instMembersBySub").find(".nDropDown").data("value");
				if(courseId){ pParams["courseId"] = courseId; }else{ $(pParams).removeProp("courseId");}
				urlQueryHelper.push("course",courseId);	
			}else{
				$(pParams).removeProp("courseId");
				urlQueryHelper.push("course",null);	
			}
			pParams["programId"] = $(".instMembersByDept").find(".nDropDown").data("value");
			var query = $(".instMemberSearch").find(".searchInputBox").val();
			pParams["query"] = query?query:"";
			urlQueryHelper.replace("query",pParams["query"]);
			pParams["size"] = 20;
			pParams["searchAll"] = true;
			var membersHolder = $(".instMembersHolder"); 
			if(freshLoad){ 
				pParams["start"]=0;
				bigLoader(membersHolder);
			}else{ 
				pParams["start"]+=20;
			}
			if(memberSearchXHR){ memberSearchXHR.abort();}
			memberSearchXHR = vReq.get("/Institute/searchMembers",pParams,function(data){
				if(freshLoad){
					membersHolder.html(data);
					animate(membersHolder.find(".instEachMember:first"));
				}else{
					membersHolder.find(".instLoadMoreMembers").remove();
					var dd = membersHolder.find(".instEachMember:last");
					membersHolder.append(data);
					animate(dd.next());
				}
				showImgs();
				memberSearchXHR = undefined;
			});
		};
	};
	this.schedules = new function(){
		this.open = function(){
			openInstHomeSubPage("/Institute/mySchedule","myschedule",params);
		};
		this.init = function(){
			institute.init();
			$(".instHomeLinksHold").find(".instSchTab").addClass("active").siblings().removeClass("active");
			institute.initWidgets("SCHEDULE");
		};
	};
	this.oneDbt = new function(){
		var dParams;
		var dbtParId;
		var getRightSec = function(){
			if(dbtParId.find(".instDoubtRightSec").data("fetched"))return;	
			dParams["entity"] = {"id":dbtParId.find('.instEachDbt').data("dissId"),"type":"DISCUSSION"}; 
    			showTopLoader();
			$.get("/Institute/getSimilarDoubts",dParams,function(data,stats){
    				hideTopLoader();
				var instRightSec = dbtParId.find(".instDoubtRightSec");
				instRightSec.html(data);
				$(".instSimilarQues").find(".eachSimQues").each(function(){
					$this = $(this);
					if($this.height()>60){
						$this.addClass("eachSimQuesMaxHeight");
					}
				});
			});
			dbtParId.find(".instDoubtRightSec").data("fetched",true);	
		};
		this.open = function(e){
			dbtParId = $(".instDoubtOpen");
			dParams = cloneObject(params);
			var dissId = $(this).data("dissId");	
			dParams["id"] = dissId;	
			openInstSubPage("/Institute/openDoubt","discussion/"+dissId,{'id':dissId});
			e.preventDefault();
		};
		var searchByTopic = function(e){
			var $this = $(this);
			var brdId = $this.data("brdId");
			var brdName = $this.text().trim();
			institute.doubts.open(e,function(){
				setTimeout(function(){
					institute.doubts.extLoad(brdId,brdName);
				},10);
			},{isExtLoad:true});
			e.preventDefault();
		};
		var followUnfollowDoubt = function(){
			var $this = $(this);
			var doubt = $this.closest(".instEachDbt");
			var countDiv = doubt.find(".followersCount .count");
			if($this.hasClass("followEntity")){
				increaseCount(countDiv);
			}
			else{
				decreaseCount(countDiv);
			}
		}; 
		this.init = function(){
			institute.init();
			dbtParId = $(".instDoubtOpen"); 
			dParams = cloneObject(params); 
			var dissId = dbtParId.find('.instEachDbt').data("dissId");	
			dParams["id"] = dissId;
			dbtParId.on("click",".instTrTopicText",searchByTopic)
				.on("click",".delInstDbt",deleteOneDoubt)
				.on("click",".followEntity,.unfollowEntity",followUnfollowDoubt)
				.on("change",".instCommentFilterDiv .nDropDown",sortDoubtOnComment)
				.on("click",".openInstDoubtAns",function(e){
					initComments();
					e.preventDefault();
				});
			dbtParId.find(".instEachDbt").addClass("fadeAnim");
			dbtParId.find(".instDbtProfilePic").load(homeImgLoaded);
			dbtParId.on("click",".RTEImageDiv",rteImageOpen);
			getRightSec();
			initComments();
			institute.initWidgets("DOUBT");
			showHomeImgs(dbtParId.find(".instDbtProfilePic"));
		};
		var deleteOneDoubt = function(e){
			deleteItem(".instEachDbt","/Institute/deleteDoubt",$(this),function(deleted){
				if(deleted){
					institute.doubts.open(e,function(){
						setTimeout(function(){
							institute.doubts.extLoad();
						},10);
					},{isExtLoad:true});
				}
			});
		};
		var sortDoubtOnComment = function(){
			var $this = $(this);
			var val = $this.data("value");
			var sortOrder = $this.data("itemValue").order;
			dbtParId.find('.instCommentFilterDivHolder').html($this.closest(".instCommentFilterDiv"));
			initComments(val,sortOrder);	
		};
		var initComments = function(orderBy,sortOrder){
			var discuss = dbtParId.find('.instEachDbt');
    			var commHolder=$(parDivId).find(".instCommentWidgetContainer");
    			commHolder.removeClass('nonner');
    			var discId=discuss.data("dissId");
    			commHolder=commHolder.find(".instCommentWidgetHolder");
			commHolder.html("");
			orderBy = orderBy ? orderBy : "timeCreated";	
			sortOrder = sortOrder ? sortOrder : "ASC";	
        		var LMData={urlStr:"/widgets/commItems",size:10,start:0,
					parent:{type:"DISCUSSION",id:discId},
					root:{id:discId,type:"DISCUSSION"},
					orderBy:orderBy,sortOrder:sortOrder,
					target:'MY_INSTITUTE'
			};
        		var allParams={};
        		allParams.root = {"id":discId,"type":"DISCUSSION"};
        		allParams.base = {"id":discId,"type":"DISCUSSION"};
        		allParams.parent = {"id":discId,"type":"DISCUSSION"};
        		allParams.scope = "ORG";
        		allParams.callBack="INST_DISCUSSION";
        		allParams.targetPage="MY_INSTITUTE";
                	allParams.placeHolder=i18nJS("ADD_ANSWER");
			bigLoader(commHolder);
        		initCommWidget(allParams,LMData,commHolder,function(holder,data,params){
				var firstIndex = params ? params.start ? params.start : 0 : 0;
				var firstItem = $(holder).find(".commItem").get(firstIndex);
				if(firstItem){
					animate($(firstItem),false);
				}
			});
			var LMHandlerDiv = commHolder.find(".LMHandlerDiv");
			var inputBox = commHolder.find(".inputerRTE");
			inputBox.insertAfter(LMHandlerDiv);
		};
	};
	this.doubts = new function(){
		var dParams;
		var dbtParId;
		var dbtFacetId;
		var subBrdId;
		this.facetsData;
		this.open = function(e,cbFn,extParams){
			var pr = {};
			$.extend(pr,params,extParams);
			openInstSubPage("/Institute/doubtUi","discussions",pr,cbFn);
		};
		var manageTabs = function(){
			var $this = $(this);
			$this.addClass("simpleBlackActiveTab")
				.siblings().removeClass("simpleBlackActiveTab");
			dParams["orderBy"] = $(this).data("orderBy");
			dParams["resultType"] = $(this).data("resultType");
			urlQueryHelper.push("tabIndex",$this.index());	
			resetBrdParams();		
			browse(true);
		};
		var subChanged = function(){
			resetBrdParams(true);		
			var $this = $(this);
			if($this.is(":checked")){
				$this.closest(".dbtFacetSub").addClass("selected")
				.closest(".dbtFacetSubject").siblings().find(".dbtFacetSub").removeClass("selected")
				.find(".dbtFacetSubChk").removeAttr("checked");
				var brdId = $this.val();
				var facetHolder;
				if(brdId){
					dParams["brdIds"] = [brdId];
					subBrdId = brdId;
					facetHolder = ".dbtFacetSubId_"+brdId+" .dbtFacetSubjTopicsHolder";
				}else{
					$(dParams).removeProp("brdIds");
				}
				browse(true,facetHolder);
			}else{
				$this.closest(".dbtFacetSub").removeClass("selected");
				dbtFacetId.find(".dbtFacetAllSub").addClass("selected");
				$(dParams).removeProp("brdIds");
				browse(true);
			}
		};
		var searchDivFocus = function(e){
			switch(e.type){
				case "focusin":
					if($(this).find(".placeHolder").get(0)){
						$(this).html("");
					}
					vtooltip.show(e,i18nJS("TYPE_AND_ENTER_TO_SEARCH"));
					$(this).on("mouseleave",function(){
						vtooltip.hide();
						$(this).off("mouseleave");
					});
					break;
				case "focusout":
					if($(this).text().trim().length == 0){
						$(this).removeClass("txtFilled");
						$(this).html("<span class='placeHolder'>"+i18nJS("TXT_SEARCH")+"</span>");
					}else if(!$(this).find(".placeHolder").get(0)){
						$(this).addClass("txtFilled");
					}
					vtooltip.hide();
					break;
			}
		};
		var crossSelTopic = function(){
			var $this = $(this).closest(".instDbtTopicSeld");
			var brdId = $this.data("brdId");
			if(dParams.brdIds && dParams.brdIds.length > 0){
				dParams.brdIds.splice(dParams.brdIds.indexOf(brdId),1);
			}
			if(subBrdId){
				dParams.brdIds.push(subBrdId);
			}
			$(parDivId).find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
			$this.remove();
			browse(false);
		};
		var putBoardInSearchBox = function(brdId,brdName){
			var html = "<span class='instDbtTopicSeld' data-brd-id='"+brdId+"' contenteditable=false>"+brdName+" <span class='closeDbtTopic'>x</span></span>";
			dbtParId.find(".instDoubtSearchedTopics")
                                .html(html)
				.addClass("txtFilled");
		};
		var resetBrdParams = function(paramsOnly){
			subBrdId = undefined;
			topicBrdId = undefined;
			dbtParId.find(".instDoubtSearchedTopics").html("").removeClass("txtFilled");
			$(dParams).removeProp("brdIds");
			if(!paramsOnly){
			   dbtParId.find(".instDoubtSearchBox .searchDivBox")
				.html("<span class='placeHolder'>Search</span>").removeClass("txtFilled");
			}
		};
		var putTopicBrdIdInParam = function(){
			if(topicBrdId){
				dParams["brdIds"] = [topicBrdId];
			}else{
				$(dParams).removeProp("brdIds");
			}
		};
		var searchTimeoutObj;
		var searchDivKeys = function(e){
			var $this = $(this);
			switch(e.which){
				case 13:
					var cln = $this.clone();
					$(cln).find("span").remove();
					var val = $(cln).text().trim();
					if(val.length>0){
						putTopicBrdIdInParam();
						browse(true,null,val);
					}else if($this.text().trim().length==0){
						$(parDivId).find(".instEachTrTopicSelect")
							.removeClass("instEachTrTopicSelect");
						resetBrdParams(true);		
						browse(true);
					}else{
						putTopicBrdIdInParam();
						browse(false);
					}
					e.preventDefault();
					break;
				case 8:
					break;
			}
			if(searchTimeoutObj){ clearTimeout(searchTimeoutObj);}
			searchTimeoutObj = setTimeout(function(){
			   var cln = $this.clone();
			   $(cln).find("span").remove();
			   var val = $(cln).text().trim();
			   if(!val && dParams.query){
				resetBrdParams();
				browse(true,null,"");
			   }
			},100);
		};
		var topicBrdId;
		var topicChanged = function(){
			var $this = $(this);
			if($this.hasClass("instEachTrTopicSelect")) return;

			$(parDivId).find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
			$this.addClass("instEachTrTopicSelect");
			var brdId = $this.data("brdId");
			dParams["brdIds"] = [brdId];
			topicBrdId = brdId;
			putBoardInSearchBox(brdId,$this.text());
			browse(false);
		};
		var showImgs = function(){
			//dbtParId.find(".instDbtProfilePic").load(homeImgLoaded);
			showHomeImgs(dbtParId.find(".instDbtProfilePic"));
		};
		var followUnfollowDoubt = function(){
			var $this = $(this);
			var doubt = $this.closest(".instEachDbt");
			var countDiv = doubt.find(".followersCount .count");
			if($this.hasClass("followEntity")){
				increaseCount(countDiv);
			}
			else{
				decreaseCount(countDiv);
			}
		}; 
		this.onBack = function(){
			var tabIndex = urlQueryHelper.read("tabIndex");
			if(tabIndex){
				initDParams();	
				$(dbtParId.find(".mangeDoubtsTab").get(tabIndex)).trigger("click");
			}else{
				getDoubts();
			}
			return true;
		};
		this.init = function(isExtLoad){
			institute.init();
			dbtParId = $(".instDoubtsHolder");
			dbtFacetId = $(".instDoubtRightSec");
			if(dbtParId.data("inited")) return false;
			$(".instHomeLinksHold").find(".instDbtTab").addClass("active").siblings().removeClass("active");
			dParams = cloneObject(params);
			dbtParId.on("click",".mangeDoubtsTab",manageTabs)
				.on("mouseenter mouseleave",".instEachDbt .openInstDoubt",doubtHover)
				.on("click",".askDoubtBtn",askDoubt)
				.on("focusin blur",".instDoubtSearchBox .searchDivBox",searchDivFocus)
				.on("keydown",".instDoubtSearchBox .searchDivBox",searchDivKeys)
				.on("click",".loadMoreDoubts",loadMore)
				.on("click",".followEntity,.unfollowEntity",followUnfollowDoubt)
				.on("click",".openInstDoubtAns",institute.oneDbt.open)
				.on("click",".closeDbtTopic",crossSelTopic)
				.on("click",".searchDbtsBtn",toggleSearchDiv)
				.on("click",".delInstDbt",deleteDoubt)
				.on("click",".instTrTopicText",topicChanged);
			$("#instPostDoubt").on("click",postDoubt);
			dbtFacetId.on("change",".dbtFacetSubChk",subChanged)
				.on("click",".instTrTopicText",topicChanged);
			assignRTEs($(".instRTEHolder"));
			if(!isExtLoad){
				this.onBack();
			}
			dbtParId.data("inited",true);
			institute.initWidgets("DOUBTS");
		};
		var toggleSearchDiv = function(){
			var $this = $(this);
			var searchDiv = $(parDivId).find(".instDoubtSearchBox");
			var searchBox = searchDiv.find(".searchDivBox"); 
			if($this.data("opened")){
				searchDiv.addClass("nonner");
				if(searchBox.hasClass("txtFilled")){
					searchBox.html("<span class='placeHolder'>"+i18nJS("TXT_SEARCH")+"</span>").removeClass("txtFilled");
					browse(true);
				}
				$this.data("opened",false);
			}else{
				searchDiv.removeClass("nonner");
				$this.data("opened",true);
				searchBox.focus();	
			}
		};
		var getQueryText = function(){
			var searchDiv = $(parDivId).find(".instDoubtSearchBox");
			var searchBox = searchDiv.find(".searchDivBox");
			var queryTxt = "";
			if(searchBox.hasClass("txtFilled") && !searchBox.find(".placeHolder").get(0)){
				queryTxt = searchBox.text();
			}
			return queryTxt;
		};
		var askDoubt =function(){
			$(".instRTEHolder").find(".RTEArea").html("");
			$(".instRTEHolder").find(".RTEHolder").attr("data-page","DISCUSSION");
			var popup = showVPopup();
			var innerDiv = $($(".instAddDoubtPopup").get(0)).clone(true).removeClass("nonner");
			popup.html(innerDiv.get(0));
			innerDiv.find(".instAddDoubtTitle input").focus();
		};
		var postDoubt = function(){
			var quesParams = cloneObject(params);
    			var QADiv=$(this).closest(".instAddDoubtPopup");
    			var getRTE=vRTE.getRTEContent;
    			quesParams.name = QADiv.find(".instAddDoubtTitle input").val();
    			if(quesParams.name.length==0){
        			showError(i18nJS("DOUBT_NEED_A_TITLE"));
        			return;
    			}
			var rteHolder = QADiv.find(".instRTEHolder").children(".RTEHolder");
    			if(vRTE.isRTEEmpty(rteHolder)){
        			showError(i18nJS("DOUBT_EXPLAIN_FURTHER"));
        			return;
    			}
    			quesParams.content = getRTE(rteHolder);
    			var tagsJson=returnAllTagsAdded(QADiv.find(".instAddDoubtTags"));
			if(tagsJson.subjectIds.length==0||tagsJson.topicIds.length==0){
        			showError(i18nJS("ENTER_ATLEAST_SUBJECT_TOPIC"));
        			return;
			}
			quesParams.brdIds=tagsJson.brdIds;
    			//quesParams.brdIds=["22dd","23dd"];
    			quesParams.scope="PUBLIC";
			//putConsoleLogs(quesParams);
			QADiv.find("#instPostQues").addClass("nonner");
			closeVPopup();
    			showTopLoader();
			vReq.post("/Institute/addNewDoubt",quesParams,function(data){
        			hideTopLoader();
				var dHolder = $(".instDoubtsLists");
				var fDbt = dHolder.find(".instEachDbt").get(0);
				if(fDbt){
        				$(fDbt).before(data);
				}else{
					dHolder.html(data);
				}
				var newInstDiss = $(dHolder.find(".instEachDbt").get(0));
				animate(newInstDiss,true);
				showImgs();
				//MathJax.Hub.Queue(["Typeset",MathJax.Hub,newInstDiss.get(0)]);
    			});
		}
		var doubtHover = function(e){
			toolTipFn(e,function(){return i18nJS("DOUBT_CLICK_TO_OPEN");});
		};
		var initDParams = function(brdIds,query){
			dParams["start"]=0;
			dParams["size"]=10;
			dParams["sortOrder"] = "DESC";
			dParams["facet"] = true;
			dParams["orderBy"] = "timeCreated";
			dParams["resultType"] = "ALL";
			dParams["query"] = query?query:"";
			if(brdIds){
				dParams["brdIds"] = brdIds;
			}else{
				resetBrdParams(true);
				$(dParams).removeProp("brdIds");
			}
		};
		var getDoubts = function(brdIds,query){
			$(".instFilterDoubts").find(".simpleBlackActiveTab").removeClass("simpleBlackActiveTab");
			$($(".instFilterDoubts").find(".simpleBlackTab").get(0)).addClass("simpleBlackActiveTab");
			initDParams(brdIds,query);	
			browse(true);
		};
		var loadMore = function(){
			smallLoader($(this));
			browse(false,null,null,dParams["start"]+dParams["size"]);	
		}
		this.extLoad = function(brdId,brdName,query){
			var brdIds;
			if(brdId){
				putBoardInSearchBox(brdId,brdName);
				brdIds = [brdId];
				topicBrdId = brdId;
			}
			getDoubts(brdIds,query);
			//browse(true,null,query);
		};
		var browseXHR;
		var browse = function(facet,subFacetHolder,query,start){
			dParams["facet"] = facet;
			dParams["subFacetHolder"] = subFacetHolder?subFacetHolder:"";
			dParams["query"] = query?query:getQueryText();
			var divId = $(parDivId).find(".instDoubtsLists");
			var lastAvailDbt;
			if(start){
				dParams["start"]=start;
				lastAvailDbt = divId.find(".instEachDbt:last");
			}else{
				dParams["start"]=0;
				bigLoader(divId);
			}
			if(browseXHR){ browseXHR.abort(); }
			browseXHR = vReq.get("/Institute/getDoubts",dParams,function(data){
				divId.find(".loadMoreDoubts").remove();
				if(dParams["start"]>1){
					divId.append(data);
				}else{
					divId.html(data);
				}
				if(!lastAvailDbt){
					lastAvailDbt = divId.find(".instEachDbt:first");
				}else{
					lastAvailDbt = lastAvailDbt.next();
				}
				animate(lastAvailDbt,false);
				if(facet){
					putFacets(divId.find(".instIncomingDbtFacets"),subFacetHolder);
				}
				if(dParams["brdIds"] && dParams["brdIds"][0]){
					var inBrdId = dParams["brdIds"][0];
					$(".doubtBrd-"+inBrdId).addClass("instEachTrTopicSelect");
				}
				showImgs();
				try{
					if(MathJax && MathJax.Hub){
						MathJax.Hub.Queue(["Typeset",MathJax.Hub],divId.get(0));
					}
				}catch(err){}
			});
		};
		var putFacets = function(facetDiv,facetHolder){
			var rightSec = $(parDivId).find(".instDoubtRightSec");
			rightSec.find(".dbtFacetSubjTopicsHolder").html("").removeClass("anim").height(0);
			rightSec.find(".dbtFacetSubject .instImgs").addClass("nonner");
                	var facets = $(facetDiv).html();
			facets = facets ? facets : "";
			if(facetHolder){
				facetHolder = rightSec.find(facetHolder);
				facetHolder.html(facets).addClass("anim");
				facetHolder.height(facetHolder.children().height());//Anim purpose
				facetHolder.closest(".dbtFacetSubject").find(".instImgs").removeClass("nonner");
				rightSec.find(".instDbtPopularTags").remove();
			}else{
				rightSec.html(facets);
			}
			$(facetDiv).remove();
		};
	};
}(jQuery);
/*$(function(){
	setTimeout(function(){institute.init();},1);
});*/
//result anlaytics
function instTestClicked(e){
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
		showError(i18nJS("CANNOT_OPEN_TEST"));
	}
    if(e){
	e.preventDefault();
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
		showError(i18nJS("CANNOT_OPEN_TEST"));
	}
    if(e){
	e.preventDefault();
    }
};

