var liveShareUrl;
var picUrl = "http://cdn.apk-cloud.com/detail/image/com.learnpedia.android-w250.png";
var customOutMessage = "Learnpedia is an edu-tech company that promises to make learning a fun and fascinating experience for students preparing to write various competitive exams. Its flagship product ScoreJEE is already redefining preparation for IIT-JEE.";
var header;
var caption = "Enjoy Learning";
$("#fbShareButton").click(function(){
	var app_id = 1656748771289533;
	var href = "https://www.facebook.com/dialog/feed?app_id="+app_id+"&caption="+caption+"&name="+header+"&link="+liveShareUrl+"&picture="+picUrl+"&description="+customOutMessage;
	FB.AppEvents.logEvent('share');
    window.open(href, "facebook", "left=600,top=200,height=200,width=200");
});

var instHeader = new function(){
	var open = function(e){
		var holder = e.data.holder;
		var parDivId = e.data.parDivId;
		var div = $(parDivId).find(holder);
		if(div.hasClass("blockNow")){
			instHeader.messages.hideMe(e);
			return;
		}
		instHeader.hideAll(e);
		$(parDivId).addClass("clicked");
		div.fadeTo(200,1);
		div.addClass("blockNow");
		setTimeout(function(){
			$(document).on("click",instHeader.hideAll);
		},1);
		var cbFn = e.data.cbFn;
		if(cbFn){
			try{ 
				cbFn(e.data);
			}catch(err){
				putConsoleError(err);
			}
		}
	};
	var hideMe = function(e,parDivId,holderClass,chkInnerElem){
		var $this = $(e.srcElement||e.target);
		var div = $(parDivId).find("."+holderClass);
		if(chkInnerElem){
                	var has = $(div).has($this);
                	if(!has.get(0) && !$this.hasClass(holderClass)){
				div.hide().removeClass("blockNow");
				$(parDivId).removeClass("clicked");
				return 0;
                	}
		}else{
			div.hide().removeClass("blockNow");
			$(parDivId).removeClass("clicked");
		}
		return 1;
	};
	this.messages = new function(){
		var parDivId = "#instMsgNotiHolder";
		var hideNow = function(e){
			$(document).off("click",instHeader.hideAll);
			instHeader.messages.hideMe(e);
		};
		this.hideMe =function(e,chkInnerElem){
			return hideMe(e,parDivId,"instMsgNotiDiv",chkInnerElem);
		};
		var fetchConversations = function(){
			var holder = $(parDivId).find(".msgsNotifications");
			smallLoader(holder);
			$.get("/UserMessages/notifyConversations",{start:0,size:5},function(data){
				holder.html(data);
			});
		};
		this.openConversation = function(e){
			var $this = $(this);
			var src = e.srcElement||e.target;
			console.log(src);
			if(e.originalEvent.returnNow || (src.nodeName=="A" && !$(src).hasClass("openMsgConversation"))){
				return;
			}
			setTimeout(function(){
				var convId = $this.data("conversationId");
				var userConvId = $this.data("userConversationId");
				var params = {"userConversationId":userConvId,"conversationId":convId,"tabType":"MESSAGES"};
				var holder = $("#msgsHeader").find("#msgsHome");
				if(holder.get(0)){
					showTopLoader();
					vReq.get("/UserMessages/conversation",params,function(data){
						hideTopLoader();
						holder.html(data);
						instHeader.getCount();	
					});
				}else{
					openMyPage("/UserMessages/openConversation",params,function(){
						instHeader.getCount();	
					},null,null,$this);	
				}
				instHeader.messages.hideMe(e);
   				pushInstHistory($("#myInstitutePage").data('orgId'),"messages/conversation/"+userConvId);
			},100);
			if(e){
				e.preventDefault();
			}
		};
		this.updateCount = function(data){	
        		if(data && data.unreadConversationCount>0){                        
    	       		$("#instMsgNotiHolder").addClass("notify")
				.find("#instMsgCount")
				.text(data.unreadConversationCount);
        		}else{
    	       		$("#instMsgNotiHolder").removeClass("notify")
				.find("#instMsgCount").text("");
			}
		};
		this.init = function(){
			$(parDivId).on("click",".instMsgNotiHead",{
				"parDivId":parDivId,
				"holder":".instMsgNotiDiv",
				"cbFn":fetchConversations
			},open)
				.on("click",".instCreateMsg,.instMsgSeeAll",hideNow)
				.on("click",".openMsgConversation",this.openConversation)
			$(document).on("click",".openMsgConversation",this.openConversation);
		}
	};

	this.getCount = function(){
    	   $.get("/Institute/getNotificationsSummary",{},function(data){
	   	instHeader.noti.updateCount(data.others);		
	   	instHeader.messages.updateCount(data.messages);		
	   }); 
	};
	this.noti = new function(){
		var parDivId = "#instNotiHolder";
		var noNotisFoundDiv = "<div class='centerText greyTextColor'>"+i18nJS("NO_RECENT_NOTIFICATION")+"</div>";
		this.hideMe =function(e,chkInnerElem){
			return hideMe(e,parDivId,"instNotiDiv",chkInnerElem);
		};
		this.updateCount = function(data){
            		if(data && data.totalHits>0){                        
        	       		$("#instNotiHolder").addClass("notify")
					.find("#instTickerCount")
					.text(data.totalHits);
            		}
		};
		var getRecent = function(){
			var instNotiHolder = $("#instNotiHolder");
			var holder = instNotiHolder.find(".instNotificationsH");
        	       	instNotiHolder.removeClass("notify");
			smallLoader(holder);
			var feedType = "NEW";
			var clustered = true;
			var params = {"feedType":feedType,size:5,"needClustered":clustered};
    			$.get("/Institute/getNotifications",params,function(data){
			     try{
				var htmlData = "";
        			if(data && data.errorMessage=='' && data.result.list.length>0){
             			   $.each(data.result.list,function(i,feed){
					var aType = feed.info && feed.info.actionType ? feed.info.actionType : feed.eType;
                 			if(newsEntityProcessor.isSupported(aType)){
                    				htmlData += newsEntityProcessor.process("NOTIFICATIONS",feed,aType,clustered);
                 			}                
            			   });
				   holder.html(htmlData);
				   MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
				}else{
				   holder.html(noNotisFoundDiv);
				}
			     }catch(err){
				   holder.html(noNotisFoundDiv);
			     }
			}); 
		};
		var hideNotis = function(e){
			return hideMe(e,parDivId,"instNotiDiv",false);
		};
		this.init = function(){
			$(parDivId).on("click",".instNotiHead",{
				"parDivId":parDivId,
				"holder":".instNotiDiv",
				"cbFn":getRecent
			},open)
			.on("click",".notiFeed,.instNotiSeeAll",hideNotis);
		}
	};
	this.refer = new function(){
		var parDivId = "#instReferHolder";
		
		this.hideMe =function(e,chkInnerElem){
			return hideMe(e,parDivId,"instNotiDiv",chkInnerElem);
		};
		var getRecent = function(){
			var instNotiHolder = $("#instReferHolder");
			var holder1 = instNotiHolder.find(".instNotificationsH");
			var holder2 = instNotiHolder.find(".instNotiTop");
        	instNotiHolder.removeClass("notify");
			smallLoader(holder1);
			smallLoader(holder2);
			var params = {"campaignType":"REFERRAL"};
			var credits;
			var referralcode;
			$.get("/Institute/getReferralData",params,function(data){
				credits = data.result.existingRewardPoints;
			    referralcode = data.result.referralCode;
			    liveShareUrl = data.baseUrl+"signup?referralcode="+referralcode;
			    // if (liveShareUrl == "http://localhost:19001/")
			    // 	liveShareUrl = "https://learn.learnpedia.in/org/learnpedia?referralcode="+referralcode;
       //          else
       //              liveShareUrl = liveShareUrl+"signup?referralcode="+referralcode;

		    	if (data.result.message != null) {
                    var currentMsg = "<h2 class='centerText'>INVITE TO LEARNPEDIA</h2><h4 class='centerText greyTextColor'>You get Rs."+data.result.referrerRewards+" and your friend gets Rs."+data.result.friendRewards+" too</h4><h3 class='centerText'>Referralcode: "+referralcode+"</h3>";
			    	var creditMsg = "<span class='big16'>Learnpedia Credits: Rs."+credits+"</span>";
			    	header = "Signup with my Referral link, You get Rs."+data.result.friendRewards;
					holder1.html(currentMsg);
					holder2.html(creditMsg);
		    	}else{
                    var currentMsg = "<h2 class='centerText'>Our Introductory Refer & Earn Is Now Over.</h2><h4 class='centerText greyTextColor'>You can still invite your friends and also use your credits</h4><h3 class='centerText'>Referralcode: "+referralcode+"</h3>";
			    	var creditMsg = "<span class='big16'>Learnpedia Credits: Rs."+credits+"</span>";
			    	header = "Welcome To Learnpedia";
					holder1.html(currentMsg);
					holder2.html(creditMsg);
		    	}
			});
		};
		var hideNotis = function(e){
			return hideMe(e,parDivId,"instNotiDiv",false);
		};
		this.init = function(){
			$(parDivId).on("click",".instReferHead",{
				"parDivId":parDivId,
				"holder":".instNotiDiv",
				"cbFn":getRecent
			},open)
            .on("click",".terms",hideNotis);
		}
	};
	this.settings = new function(){
		var parDivId = "#instUserDetails";
		this.hideMe =function(e,chkInnerElem){
			return hideMe(e,parDivId,"instSettingsOptions",chkInnerElem);
		};
		this.init = function(){
			$(parDivId).on("click",".instOpenSettingsDrop",{
				"parDivId":parDivId,
				"holder":".instSettingsOptions",
				"cbFn":null
			},open)
			.on("click","a",close)
			.on("click",".openInstAvailPrograms,.openMyOrders",close)
			.on("click",".openInstSettings",openPopup);
		};
		var close = function(e){
			setTimeout(function(){
				instHeader.settings.hideMe(e,false);
			},100);
		}
		var openPopup = function(e){
			instHeader.settings.hideMe(e,false);
			openAccPopup();
			if(e) e.preventDefault();
			return false;
		};
		this.accSettingsOpen = function(){
			openAccPopup();
		};
		this.regEmailPopupOpen = function(){
			openAccPopup({"useExtClassName":"showOnlyEmailOpt"});
		};
		var openAccPopup = function(extParams){
			var popup = showVPopup(0.5,false,true);
			bigLoader(popup);
			var orgId = $("#myInstitutePage").data("orgId");
			var pr = {"orgId":orgId,"closePopupClass":"crossVPopup"};
			if(extParams && typeof extParams == "object"){
				pr = $.extend(pr,extParams);
			}
			$.get("/UserSettings/openSettings",pr,function(data){
				popup.html(data);
				userSettings.init(popup,orgId);
			});
		};
	};
	this.hideAll = function(e){
		var rets = new Array();
		rets.push(instHeader.messages.hideMe(e,true));
		rets.push(instHeader.noti.hideMe(e,true));
		rets.push(instHeader.settings.hideMe(e,true));
		rets.push(instHeader.refer.hideMe(e,true));
		var ret = 0;	
		$(rets).each(function(){
			ret += this;
		});
		if(!ret){
			$(document).off("click",instHeader.hideAll);
		}
	}
	var clickEvent = "click", keyupEvent = "keyup", pasteEvent = "paste";
	this.init = function(){
		regInstFns();
		this.messages.init();
		this.noti.init();
		this.settings.init();
		this.refer.init();
		this.getCount();
		// getUserPushNotifications();
		// promoPopup();
	};
	var regInstFns = function(){
		$(".viewMemberMappings").live('click',viewMemberMappings);		
                $(".viewProfileExtraInfo").live('click',viewProfileExtraInfo);		
		// $("#contentSectionHolder")
		// 	.off("mouseenter mouseleave","a.openInstProfile",toolTipMember)
		// 	.on("mouseenter mouseleave","a.openInstProfile",toolTipMember);
	};
	var mouseEventType;
	var memberToolTipTimeoutObj;
	var memberToolTipXHR;
	this.clearTimer = function(){
		if(memberToolTipTimeoutObj){clearTimeout(memberToolTipTimeoutObj);}
		clearToolTipXHR();
		if(memberToolTipXHR){memberToolTipXHR.abort();}
	};
	var toolTipMember = function(e){
		var fetchAndShow = function(){
			var pr = {"targetUserId":targetUserId};
			memberToolTipXHR = $.get("/Institute/getMemberToolTip",pr,function(html){
				var eType = getEType();
				if(eType == "mouseenter"){
					vtooltip.show(e,html,onShow,offsetPlus,"orgMemberToolTip");
				}
				memberDataCache.push(targetUserId,html);
			});
		};
		//putConsoleLogs("tool tip member ======= "+e.timeStamp);
		var $this = $(this);
		if(memberToolTipTimeoutObj){clearTimeout(memberToolTipTimeoutObj);}
		var offsetPlus = 0;
		var targetUserId = $this.data("userId");
		if(!targetUserId){ return;}
		toolTipFn(e,function(){
			clearToolTipXHR();
			if(memberToolTipTimeoutObj){clearTimeout(memberToolTipTimeoutObj);}
			var cacheData = memberDataCache.get(targetUserId);
			if(cacheData){
				memberToolTipTimeoutObj = setTimeout(function(){
					vtooltip.show(e,cacheData,onShow,offsetPlus,"orgMemberToolTip");
				},1);
				return "Loading..";
			}
			memberToolTipTimeoutObj = setTimeout(function(){
				fetchAndShow();
			},500);
			return "Loading..";
		},offsetPlus,"orgMemberToolTip");
		var onShow = function(){
			$("#tooltipContainer").find(".instProfilePic").load(function(){
				$(this).fadeTo(120,1);
				$(this).closest(".instProfilePicContainer").addClass("imgLoaded");
			});
		};
		mouseEventType = e.type;
		var getEType = function(){
			return mouseEventType;
		};
	};
	var memberDataCache = new function(){
		var cacheData = {};
		var expiry = 60000;
		var has = function(userId){
			if(cacheData[userId]){
				return true;
			}
			return false;
		};
		this.get = function(userId){
			if(has(userId)){
				var data = cacheData[userId];
				var expiryTime = data.expiryTime;
				var curTime = (new Date()).getTime();
				if(expiryTime>curTime){
					return data.member;
				}else{
					cacheData[userId] = undefined;
				}
			}
		};
		this.push = function(userId,memberData){
			var expiryTimeStamp = (new Date()).getTime()+expiry;
			var data = {"expiryTime":expiryTimeStamp,"member":memberData};
			//console.log(data);
			cacheData[userId] = data;
		};
	};
	var viewMemberMappings = function(){
		var $this = $(this);
		var targetUserId = $this.data("userId");
		var targetUserName = $this.data("userName");
		targetUserName = targetUserName ? targetUserName : "User";
		var popup = showVPopup(0.5);
		bigLoader(popup);
		var pr = {
			"targetUserId":targetUserId,
			"targetUserName":targetUserName
		};
		vReq.get("/Institute/getMemberMappings",pr,function(data){
			popup.html(data);
		});
	};
        var viewProfileExtraInfo = function(){
		var $this = $(this);
		var targetUserId = $this.data("userId");
		var targetUserName = $this.data("userName");
		targetUserName = targetUserName ? targetUserName : "User";
		var popup = showVPopup(0.5);
		bigLoader(popup);
		var pr = {
			"targetUserId":targetUserId,
			"targetUserName":targetUserName
		};
		vReq.get("/Institute/getProfileExtraInfo",pr,function(data){
			popup.html(data);
		});
	};
	var getUserPushNotifications = function(){
		$.get("/UserSettings/getUserPushNotifications",{},function(data){
			$("#userPushNotifyPopup").html(data);
		});
	};

	var promoPopup = function(){
		$.get("/UserSettings/showWelcomeMessage",{},function(data){
			$("#userPromoPopup").html(data);
		});
	};
	$("#instTopBar").on("click",".orgHomePg",function(e){
		goToMyInstitutePage($(this),$(this).data("orgId"));
		e.preventDefault();
	});
	this.show = function(orgId){
		var instTopBar = $("#instTopBar");
		if(instTopBar.data("shown")) return;
		instTopBar.find(".orgHomePg").addClass("nonner");
		instTopBar.data("shown",true);
		$("#topBar").fadeTo(150,0,function(){
			$(this).addClass("nonner").hide();
			$(instTopBar.find(".topBrOrgId_"+orgId).get(0)).removeClass("nonner");
			instTopBar.fadeTo(150,1,function(){
				$(this).removeClass("nonner");
			});
		});
	};
	this.hide = function(){
		$("#instTopBar").fadeTo(150,0,function(){
			$(this).addClass("nonner").hide();
			$("#topBar").fadeTo(150,1,function(){
				$(this).removeClass("nonner");
			});
		}).data("shown",false);
	};
}
$(function(){
	instHeader.init();
	nDropDown.init();
});

var nDropDown = new function(){
	var parDivId = document;
	this.init = function(){
		$(parDivId).on("click",".nDropDown .dropDownHead",openOrClose);
		$(parDivId).on("click",".nDropDown .eachDropedElement",selected);
	};
	var closeAll = function(){
		$(parDivId).off("click",closeAll);
		$(parDivId).find(".nDropDown").each(function(){
			$(this).find(".dropDownContainer").addClass("nonner");
		});
	};
	var show = function($this){
		$this.find(".dropDownContainer").removeClass("nonner");
		$(parDivId).on("click",closeAll);
	};
	var hide = function($this){
		$(parDivId).off("click",closeAll);
		$this.find(".dropDownContainer").addClass("nonner");
	};
	var openOrClose = function(){
		var head = $(this);
		var $this = $(this).closest(".nDropDown");
		if(!$this.find(".dropDownContainer").hasClass("nonner")){
			hide($this);
		}else{
			show($this);
		}
	};
	var selected = function(){
		var $this= $(this);
		var par = $this.closest(".nDropDown");
		var value = $this.data("value");
		value = value ? value : "";
		par.data("value",value);
		par.data("itemValue",{});
		par.data("itemValue",$this.data("itemValue"));
		var text = $.trim($this.data("text"));
		text = text?text:$this.text().trim();
		var appendText = $this.closest(".eachDroperElemHolder").data("appendText");
		appendText = appendText?$.trim(appendText)+" ":"";
		text = appendText+text;
		par.find(".textCont").text(text);
		hide(par);	
		par.trigger("change");
	};
	var newElement = function(container,text,value,item){
		var el = document.createElement("div");
		$(el).addClass("eachDropedElement")
			.data("value",value)
			.data("itemValue",item)
			.text(text);
		$(container).append(el);
		return $(el);
	};
	var clearAll = function(container){
		container.find(".dropElemPackage,.eachDropedElement").remove();
	}
	var reset = this.reset = function(holders){
		var drop;
		$(holders).each(function(index,holder){
			drop = $(holder).find(".nDropDown");
			var container = drop.find(".dropDownContainer");
			var item = container.find(".eachDropedElement:first");
			var val = item.data("value") ? item.data("value") : "";
			drop.data("value",val)
				.data("itemValue",item.data("itemValue"));
			var text = item.data("text");
			text = text?text:item.text();
			drop.find(".textCont").text(text);
		});
		return drop;
	};
	this.redraw = function(holder,allowSelectAll,list,listType,append){
		var drop = $(holder).find(".nDropDown");
		var container = drop.find(".dropDownContainer");
		if(!append){
			clearAll(container);
		}
		if(allowSelectAll){
			newElement(container,"All").data("text","All "+listType);	
		}
		if(list && list.length>0){
			$(list).each(function(index,item){
				newElement(container,item.name,item.id,item);
			});
		}
		reset(holder);	
		return true;
	};
	var searchByVal = function(holder,val){
		var drop = $(holder).find(".nDropDown");
		var container = drop.find(".dropDownContainer");
		var retObj;
		container.find(".eachDropedElement").each(function(index){
			var $this = $(this);
			var v = $this.data("value");
			if(v == val){
				retObj = {index:index,text:$this.text(),value:v,data:cloneObject($this.data())};
			}
		});
		return retObj;
	};
	this.searchAndUpdate = function(holder,val,doTrigger){
		var obj = searchByVal(holder,val);
		if(obj){
			var drop = $(holder).find(".nDropDown");
			for(d in obj.data){
				drop.data(d,obj.data[d]);
			}
			drop.data("value",obj.value);
			drop.find(".dropDownHead .textCont").text(obj.text);
			if(doTrigger){
				drop.trigger("change");
			}
		}
		return obj;
	};
};
var instMembrSugg = new function(){
	var parDivId = ".instTypeMembers";
	var suggXHR;
	var suggTimeout;
	var lastSearchedTxt = "";
	var params = {"searchAll":true,"size":5,"start":0,"year":new Date().getFullYear()};
	var suggHolder;
	var suggHolderNm = ".instTypMembrsSugg", suggContainerId = ".instTypMembrsSuggContainer";
	var uiSample = "<span class='instTpSrchMembr' data-user-id='%userId'>%fullName <label class='instTpSrchMembrCross'>X</label></span>";
	var currUserList = [];
	this.init = function(widHolder){
		//params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		params['contentSrc'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		suggHolder = $(widHolder).find(parDivId).find(suggHolderNm);
		$(suggHolder).off("mouseenter",".instTypEachMembrSugg",mouseEnter)
			.off("click",".instTypEachMembrSugg",selectUser)
			.off("mouseleave",".instTypEachMembrSugg",mouseLeave)
			.on("mouseenter",".instTypEachMembrSugg",mouseEnter)
			.on("mouseleave",".instTypEachMembrSugg",mouseLeave)
			.on("click",".instTypEachMembrSugg",selectUser)
			.on("click",".loadMoreMemberSugg",loadMore);
		$(widHolder).find(parDivId)
			.off("keydown",".instTpSrchMbrsInput",onKeyPress)
			.on("keydown",".instTpSrchMbrsInput",onKeyPress)
			.off("blur",".instTpSrchMbrsInput",close)
			.on("blur",".instTpSrchMbrsInput",close)
			.off("click",".instTpSrchMembrCross",crossMember)
			.on("click",focusInput)
			.on("click",".instTpSrchMembrCross",crossMember);
		currUserList = [];
	};
	var focusInput = function(){
		$(this).find(".instTpSrchMbrsInput").focus();
	};
	this.putUserExt = function(widHolder,user){
	    if(user && user.fullName && user.id){
		var d = uiSample.replace("%fullName",user.fullName);
		d = d.replace("%userId",user.id);
		var parDiv = $(widHolder).find(parDivId);
		parDiv.find(".instTpSrchMembers").append(d);
		parDiv.find(".instTpSrchMbrsInput").val("");
	    }
	};
	this.getUserList = function(divId){
		var arr = new Array();
		$(divId).find(".instTpSrchMembers .instTpSrchMembr").each(function(){
			arr.push($(this).data("userId"));
		});
		return arr;
	};
	var clearAll = function(){
		suggHolder.html("").closest(suggContainerId).addClass("nonner").scrollTop(0);
	};
	var removeMember = function(memberDiv){
		var userId = $(memberDiv).data("userId");
		removeItemFromArr(currUserList,userId);
		$(memberDiv).remove();
	};
	var crossMember = function(){
		removeMember($(this).closest(".instTpSrchMembr"));
		return false;	
	};
	var delLastSugg = function(holder){
		removeMember($(holder).find(".instTpSrchMembers").find(".instTpSrchMembr:last"));
	};
	var scrollDir = function(dir){
		var cur = suggHolder.find(".selectInstMember");
		var holder = suggHolder.closest(suggContainerId);
		var ttop = cur.position()["top"];
		ttop = ttop && !isNaN(ttop) ? ttop : 0;
		holder.scrollTop(ttop);
	};
	var onKeyPress = function(e){
		var key = e.which;
		var $this = $(this);
		var text = $.trim($this.val());
		suggHolder = $this.closest(parDivId).find(suggHolderNm);
		switch(key){
			case 8 :if(text.length<=0){
					delLastSugg(suggHolder.closest(".instTypeMembers"));
				}
				break;
			//UP
			case 38:navigateUpDown("PREV");
				scrollDir("PREV");
				e.preventDefault();
				return;
			case 40:navigateUpDown("NEXT");
				scrollDir("NEXT");
				e.preventDefault();
				return;
			case 13:selectUser($this);
				e.preventDefault();
				return;
		}
		if(suggTimeout) clearTimeout(suggTimeout);
		suggTimeout = setTimeout(function(){
			text = $.trim($this.val());
			//console.log("key == "+key+" , val == "+text);
			if(text){
				search(text);
			}else{
				clearAll();
			}
		},1);
	};
	var navigateUpDown = function(DIR){
		var cur = suggHolder.find(".selectInstMember").removeClass("selectInstMember");
		switch(DIR){
			case "NEXT":if(cur.next(".instTypEachMembrSugg").get(0)){
					cur.next(".instTypEachMembrSugg").addClass("selectInstMember");
				    }else{
					suggHolder.find(".instTypEachMembrSugg:first").addClass("selectInstMember");
				    }
				break;
			case "PREV":if(cur.prev(".instTypEachMembrSugg").get(0)){
					cur.prev(".instTypEachMembrSugg").addClass("selectInstMember");
				    }else{
					suggHolder.find(".instTypEachMembrSugg:last").addClass("selectInstMember");
				    }
				break;
		};
	};
	var mouseEnter = function(){
		var cur = suggHolder.find(".selectInstMember").removeClass("selectInstMember");
		$(this).addClass("selectInstMember");
		$(this).closest(parDivId)
			.off("blur",".instTpSrchMbrsInput",close);
	};
	var mouseLeave = function(){
		$(this).closest(parDivId)
			.on("blur",".instTpSrchMbrsInput",close);
	};
	var selectUser = function(){
		var cur = suggHolder.find(".selectInstMember");
		var name = cur.data("fullName");
		var userId = cur.data("userId");
		if(!name || !userId){
			if(cur.hasClass("loadMoreMemberSugg")){
				var start = cur.data("nextStart");
				search(params["query"],start);
			}
			return; 
		}
		var d = uiSample.replace("%fullName",name);
		d = d.replace("%userId",userId);
		suggHolder.closest(parDivId).find(".instTpSrchMembers").append(d);
		pushIfAbsent(currUserList,userId);
		$(parDivId).find(".instTpSrchMbrsInput").val("");
		setTimeout(function(){close();},100);
	};
	var close = function(){
		clearAll();
	};
	var loadMore = function(e){
		e.preventDefault();
		return false;
	};
	var search = function(text,start){
		params["query"] = text;
		pushIfAbsent(currUserList,USERID);
		params["excludes"] = currUserList;
		//params.size = 2;
		if(start){
			params.start = start;
		}else{
			params.start = 0;
		}
		if(suggXHR) suggXHR.abort();
		var loadMoreDiv = suggHolder.find(".loadMoreMemberSugg").removeClass("loadMoreMemberSugg instTypEachMembrSugg");
		smallLoader(loadMoreDiv);
		suggXHR = $.get("/Institute/suggMembers",params,function(data){
		   loadMoreDiv.remove();
		   if(start){
			var lastItem = suggHolder.find(".instTypEachMembrSugg:last");
			suggHolder.append(data);
			var next = lastItem.next(".instTypEachMembrSugg");
			if(next.get(0)){
				next.addClass("selectInstMember");
			}else{
				lastItem.addClass("selectInstMember");
			}
		   }else{
			if(!data || $.trim(data).length<1){
				clearAll();
			}else{
				suggHolder.closest(suggContainerId).removeClass("nonner").scrollTop(0);
				suggHolder.html(data);
				suggHolder.find(".instTypEachMembrSugg:first").addClass("selectInstMember");
			}
		   }
		});
	};
}
