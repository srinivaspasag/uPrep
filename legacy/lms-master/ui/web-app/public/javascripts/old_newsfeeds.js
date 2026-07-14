    var newsFeedFns=new Object();
    var pointsFns=new Object();    
    var diviB="<div class='cleaner_with_divider postBorder'>&nbsp;</div>";

    //notification functions
var notiFeedFns={
    FOLLOWED:{
        init:function(feed){
            if(notiFeedFns.FOLLOWED[feed.src.type])
            return notiFeedFns.FOLLOWED[feed.src.type](feed);            
        },
        QUESTION:function(feed){
            return notiInsert(getUser(feed.actor)+" started following the "+getQuestion(feed.src)+".",feed);
        },
        USER:function(feed){
            return notiInsert(getUser(feed.actor)+" started following you.",feed);
        },
        TEST:function(feed){
            return notiInsert(getUser(feed.actor)+" started following the "+getTest(feed.src)+".",feed);
        },
        PLAYLIST:function(feed){
            return notiInsert(getUser(feed.actor)+" started following the "+getPlaylist(feed.src)+".",feed);
        }        
     },
     ADDED:{         
         init:function(feed){
            if(notiFeedFns.ADDED[feed.eType]){
		return notiFeedFns.ADDED[feed.eType](feed);
	    }else if(notiFeedFns.ADDED[feed.src.type]){
		return notiFeedFns.ADDED[feed.src.type](feed);
	    }else{
		return "";
	    }
         },
         INDEX_QUESTION:function(feed){
             return notiInsert(getUser(feed.actor)+" added a new "+getQuestion(feed.src)+".",feed);
         },
         ADD_SOLUTION:function(feed){
             return notiInsert(getUser(feed.actor)+" added a new solution to "+getQuestion(feed.src)+".",feed);
         },
	 REMARK:function(feed){
             return notiInsert(getUser(feed.actor)+" posted a new remark "+getRemark(feed.src,feed.srcOwner)+".",feed);
	 }
     },
     SHARED:{         
         init:function(feed){
            if(notiFeedFns.SHARED[feed.src.type])
            return notiFeedFns.SHARED[feed.src.type](feed);                              
         },
	 STATUSFEED:function(feed){
		return notiInsert(getUser(feed.actor)+" shared a feed : "+getStatusFeed(feed.src,feed.newsFeedId)+".",feed);
	 },
         QUESTION:function(feed){
            	return notiInsert(getUser(feed.actor)+" shared a new question : "+getQuestion(feed.src)+".",feed);
         },
     },
     ATTEMPTED:{
         init:function(feed){
             if(notiFeedFns.ATTEMPTED[feed.src.type])
             return notiFeedFns.ATTEMPTED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            return notiInsert(getUser(feed.actor)+" attempted the "+getQuestion(feed.src)+".",feed);
        },
        TEST:function(feed){
            return notiInsert(getUser(feed.actor)+" attempted the test "+getTest(feed.src)+".",feed);
        },
        CHALLENGE:function(feed){
            return notiInsert("The challenge "+getChallenge(feed.src,feed.info)+" you have taken has burst.",feed);
        }
     },
     VOTED:{
        init:function(feed){
             if(notiFeedFns.VOTED[feed.src.type])
             return notiFeedFns.VOTED[feed.src.type](feed);
        },
	STATUSFEED:function(feed){
		return notiInsert(getUser(feed.actor)+" upvoted "+getOwnerUser(feed.srcOwner)+" feed : "+getStatusFeed(feed.src,feed.newsFeedId)+".",feed);
	},
        QUESTION:function(feed){
            return notiInsert(getUser(feed.actor)+" upvoted "+getOwnerUser(feed.srcOwner)+" "+getQuestion(feed.src)+".",feed);
        },
        TEST:function(feed){
            return notiInsert(getUser(feed.actor)+" upvoted "+getOwnerUser(feed.srcOwner)+" test "+getTest(feed.src)+".",feed);
        },
        SOLUTION:function(feed){
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return notiInsert(getUser(feed.actor)+" upvoted on the solution you added on "+getQuestion(feed.src.pDoc)+".",feed);
            }            
        },
        COMMENT:function(feed){
	    return newsEntityProcessor.process("NOTIFICATIONS",feed,"VOTED");
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return notiInsert(getUser(feed.actor)+" upvoted on the discussion you added on "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return notiInsert(getUser(feed.actor)+" upvoted on the comment you added.",feed);
            }
        },
        PLAYLIST:function(feed){
            return notiInsert(getUser(feed.actor)+" upvoted "+getPlaylist(feed.src)+".",feed);
        }
     },
     VOTE_ENTITY : this.VOTED,
     COMMENTED:{
        init:function(feed){
             if(notiFeedFns.COMMENTED[feed.src.type])
             return notiFeedFns.COMMENTED[feed.src.type](feed);
        },
	STATUSFEED:function(feed){
		return notiInsert(getUser(feed.actor)+" commented on "+getOwnerUser(feed.srcOwner)+" feed "+getStatusFeed(feed.src,feed.newsFeedId)+".",feed);
	},
        QUESTION:function(feed){
            return notiInsert(getUser(feed.actor)+" added new discussion to "+getQuestion(feed.src)+".",feed);
        },
        SOLUTION:function(feed){
            if(feed.src.pDoc){
                return notiInsert(getUser(feed.actor)+" commented on the solution you added on "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return notiInsert(getUser(feed.actor)+" commented on the solution you added.",feed);
            }            
        },
        COMMENT:function(feed){
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return notiInsert(getUser(feed.actor)+" commented on the discussion you added on "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return notiInsert(getUser(feed.actor)+" replied on the comment you added.",feed);
            }
            
        }
     },
     ASKED:{
         init:function(feed){
             if(notiFeedFns.ASKED[feed.src.type])
             return notiFeedFns.ASKED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            var shareText=".",shareCount=feed.sharedWith;
            if(shareCount>0){
                shareText=" "+getUser(feed.sharedWith[0]);
                if(shareCount>1){
                    shareText+=" and "+(shareCount-1)+" others."
                }
            }
            return notiInsert(getUser(feed.actor)+" shared a "+getQuestion(feed.src)+shareText,feed);
        }
     },
     SHARED:{
         init:function(feed){
             if(notiFeedFns.SHARED[feed.src.type])
             return notiFeedFns.SHARED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            var shareText=".",shareCount=feed.sharedWith;
            if(shareCount>0){
                shareText=" "+getUser(feed.sharedWith[0]);
                if(shareCount>1){
                    shareText+=" and "+(shareCount-1)+" others."
                }
            }
            return notiInsert(getUser(feed.actor)+" shared a "+getQuestion(feed.src)+shareText,feed);        
        }        
     }
};


var activityFeedFns={
    FOLLOWED:{
        init:function(feed){
            if(activityFeedFns.FOLLOWED[feed.src.type])
            return activityFeedFns.FOLLOWED[feed.src.type](feed);
        },
        QUESTION:function(feed){
            return activityInsert(getUser(feed.actor)+" started following a "+getQuestion(feed.src)+".",feed);
        },
        USER:function(feed){
            return activityInsert(getUser(feed.actor)+" started following "+getUser(feed.src)+".",feed);
        },
        TEST:function(feed){
            return activityInsert(getUser(feed.actor)+" started following the "+getTest(feed.src)+".",feed);
        },
        PLAYLIST:function(feed){
            return activityInsert(getUser(feed.actor)+" started following the "+getPlaylist(feed.src)+".",feed);
        }        
     },
     ADDED:{
         init:function(feed){
            if(activityFeedFns.ADDED[feed.eType]){
		return activityFeedFns.ADDED[feed.eType](feed);
	    }else if(activityFeedFns.ADDED[feed.src.type]){
		return activityFeedFns.ADDED[feed.src.type](feed);
	    }else{
		return "";
	    }
         },
         INDEX_QUESTION:function(feed){
             return activityInsert(getUser(feed.actor)+" added a new "+getQuestion(feed.src)+".",feed);
         },
         ADD_SOLUTION:function(feed){
             return activityInsert(getUser(feed.actor)+" added a new solution to "+getQuestion(feed.src)+".",feed);
         },         
	 REMARK:function(feed){
             return activityInsert(getUser(feed.actor)+" posted a new remark "+getRemark(feed.src,feed.srcOwner)+".",feed);
	 }
     },
     SHARED:{         
         init:function(feed){
            if(activityFeedFns.SHARED[feed.src.type])
            return activityFeedFns.SHARED[feed.src.type](feed);                              
         },
	 STATUSFEED:function(feed){
		return activityInsert(getUser(feed.actor)+" shared a feed : "+getStatusFeed(feed.src,feed.newsFeedId)+".",feed);
	 },
         QUESTION:function(feed){
            var shareText=".",shareCount=feed.sharedWith;
            if(shareCount>0){
                shareText=" "+getUser(feed.sharedWith[0]);
                if(shareCount>1){
                    shareText+=" and "+(shareCount-1)+" others."
                }
            }
            return activityInsert(getUser(feed.actor)+" shared a "+getQuestion(feed.src)+shareText,feed);        
        }
     },
     ATTEMPTED:{
         init:function(feed){
             if(activityFeedFns.ATTEMPTED[feed.src.type])
             return activityFeedFns.ATTEMPTED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            return activityInsert(getUser(feed.actor)+" attempted a "+getQuestion(feed.src)+".",feed);
        },
        TEST:function(feed){
            return activityInsert(getUser(feed.actor)+" attempted the test "+getTest(feed.src)+".",feed);
        },
        CHALLENGE:function(feed){
            return activityInsert(getUser(feed.actor)+" attempted the challenge "+getChallenge(feed.src,feed.info)+".",feed);
        }
     },
     VOTED:{
         init:function(feed){
             if(activityFeedFns.VOTED[feed.src.type])
             return activityFeedFns.VOTED[feed.src.type](feed);
         },
	STATUSFEED:function(feed){
		return notiInsert(getUser(feed.actor)+" upvoted "+getOwnerUser(feed.srcOwner)+" feed : "+getStatusFeed(feed.src,feed.newsFeedId)+".",feed);
	},
        QUESTION:function(feed){
            return activityInsert(getUser(feed.actor)+" upvoted a "+getQuestion(feed.src)+".",feed);
        },
        TEST:function(feed){
            return activityInsert(getUser(feed.actor)+" upvoted the test "+getTest(feed.src)+".",feed);
        },
        SOLUTION:function(feed){
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return activityInsert(getUser(feed.actor)+" upvoted on the solution of a "+getQuestion(feed.src.pDoc)+".",feed);
            }            
        },
        COMMENT:function(feed){
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return activityInsert(getUser(feed.actor)+" upvoted on the discussion of a "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return activityInsert(getUser(feed.actor)+" upvoted on the comment.",feed);
            }            
        }
     },
     VOTE_ENTITY : this.VOTED,
     COMMENTED:{
         init:function(feed){
             if(activityFeedFns.COMMENTED[feed.src.type])
             return activityFeedFns.COMMENTED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            return activityInsert(getUser(feed.actor)+" added new discussion to the "+getQuestion(feed.src)+".",feed);
        },
        SOLUTION:function(feed){
            if(feed.src.pDoc){
                return activityInsert(getUser(feed.actor)+" commented on the solution of a "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return activityInsert(getUser(feed.actor)+" commented on the solution.",feed);
            }            
        },
        COMMENT:function(feed){
            if(feed.src.pDoc&&feed.src.pDoc.type=="QUESTION"){
                return activityInsert(getUser(feed.src.actor)+" commented on the discussion of a "+getQuestion(feed.src.pDoc)+".",feed);
            }
            else{
                return activityInsert(getUser(feed.actor)+" replied on the comment.",feed);
            }            
        }
     },
     PUBLISHED:{
         init:function(feed){
             if(activityFeedFns.PUBLISHED[feed.src.type])
             return activityFeedFns.PUBLISHED[feed.src.type](feed);
         },
         TEST:function(feed){
            return activityInsert(getUser(feed.actor)+" published the test "+getTest(feed.src)+".",feed);
         }
     },
     ASKED:{
         init:function(feed){
             if(activityFeedFns.ASKED[feed.src.type])
             return activityFeedFns.ASKED[feed.src.type](feed);
         },
        QUESTION:function(feed){
            var shareText=".",shareCount=feed.sharedWith;
            if(shareCount>0){
                shareText=" "+getUser(feed.sharedWith[0]);
                if(shareCount>1){
                    shareText+=" and "+(shareCount-1)+" others."
                }
            }
            return activityInsert(getUser(feed.actor)+" shared a "+getQuestion(feed.src)+shareText,feed);
        }
     },
}

var notificationsEl=$("#notiHolder").find(".notiDiv");
function notiInsert(text,feed){
    var appendHTML = "<div class='notiFeed' data-feed-id="+feed.newsFeedId+">\n\
    <div class='postTitle'>"+text+"</div>"+getAgo(feed.time)+"</div>";
    notificationsEl.append(appendHTML);
    return appendHTML;
}
function activityInsert(text,feed){
    var appendHTML = "<div class='notiFeed' data-feed-id="+feed.newsActivityId+">\n\
    <div class='postTitle'>"+text+"</div>"+getAgo(feed.time)+"</div>";
    $("#activityFeedsDiv").append(appendHTML);
    return appendHTML;
}



function getUser(user){
	var name = user.firstName +" "+ user.lastName;
	if(user.id == USERID || (user.userId == USERID && user.orgId)){
		name = "You ";
	}
	var a = "<a class='openUserProfile' href='/user/"+user.id+"' data-user-id='"+user.id+"'>"+name+"</a>";
	var orgId = $("#myInstitutePage").data('orgId');
	if(orgId){
		a = "<a class='openInstProfile' href='/organization/"+orgId+"/profile/"+user.id+"' data-user-id='"+user.id+"'>\n\
        	"+name+"</a>";
	}
	return a;
}
function getOwnerUser(user){
	var a = "Your";
	if(user){
		var name = user.firstName +" "+ user.lastName;
		if(user.id == USERID || (user.userId == USERID && user.orgId)){
			a = "Your";
		}else{
			var name = user.firstName +" "+ user.lastName+"'s";
			a = "<a class='openUserProfile' href='/user/"+user.id+"' data-user-id='"+user.id+"'>"+name+"</a>";
			var orgId = $("#myInstitutePage").data('orgId');
			if(orgId){
				a = "<a class='openInstProfile' href='/organization/"+orgId+"/profile/"+user.id+"' data-user-id='"+user.id+"'>\n\
        			"+name+"</a>";
			}
		}
	}
	return a;
}
function getQuestion(ques){
    var subAndTopic="";
    if(ques.hasOwnProperty("subject")&&ques.hasOwnProperty("topic")){
        subAndTopic=" on <a class='boardSubject' href='/subject/"+ques.subject.brdId+"' data-brd-id='"+ques.subject.brdId+"'>"+ques.subject.name+"</a>:\n\
        <a class='boardTopic' href='/topic/"+ques.topic.brdId+"'  data-brd-id='"+ques.topic.brdId+"'>"+ques.topic.name+"</a>";
    }
    return("<a class='openQuesPage' data-qid='"+ques.id+"' href='/question/"+ques.id+"'>Question</a>"+subAndTopic);
}
function getChallenge(chal,info){
    if(info.qid&&info.title){
            return("<a class='openQuesPage' data-qid='"+info.qid+"' href='/question/"+info.qid+"'>"+info.title+"</a>");
        }
        else return "";    
}
function getTest(test){
    	var orgId = $("#myInstitutePage").data('orgId');
	var data = "";
	if(orgId){
		data = "<a class='openInstTest' data-test-id='"+test.id+"' href='/organization/"+orgId+"/test/"+test.id+"'>"+test.name+"</a>";
	}else{
		data = "<a class='openTestPage' data-test-id='"+test.id+"' href='/test/"+test.id+"'>"+test.name+"</a>";
	}
    	return data;
}
function getRemark(remark,owner){
    	var orgId = $("#myInstitutePage").data('orgId');
	var data = "<b>'"+remark.content+"'</b> in "+getOwnerUser(owner)+" profile";
    	return data;
}
function getStatusFeed(feed,newsFeedId){
	var orgId = $("#myInstitutePage").data('orgId');
	var msg = feed.statusMessage?feed.statusMessage:feed.sourceContent?feed.sourceContent.title:"Your Feed";
	var html = "<a class='openStatusFeed' data-feed-id='"+feed.id+"' href='/organization/"+orgId+"/feed/"+feed.id+"'>"+msg+"</a>";
    	return html;
}
function getPlaylist(pl){
    var title="playlist";
    if(pl.title)title=pl.title;
    return("<a class='openPLView' data-pl-id='"+pl.id+"' href='/playlist/"+pl.id+"'>"+title+"</a>");
}






$(".notiHead").live('click',function(){
    var notiHolder=$(this).closest("#notiHolder");
    var notiDiv=notiHolder.children(".notiDiv");
    notiHolder.find(".notiNo").text(0).addClass("notiNoNone");
    addToggler(notiDiv,$(this));
    notiDiv.html("");
    getNotifications("/Application/getNotifications",{feedType:"NEW",size:5},"NOTI_POPUP");
});
$("#showFullNotis").live('click',function(){
    insideOutClick();
    $("#noTabSection").html("<div id='notificationsDiv' class='dummySecDiv'>\n\
    <div class='dummySecHead'>Notifications</div><div id='notiFeedsDiv'></div></div>");
    var params={feedType:"NEW",size:25}
    getNotifications("/Application/getNotifications",params);
});
$("#seeOlderNotis").live('click',function(){
    var params={feedType:"OLD",size:25,beforeNotificationId:$("#notiFeedsDiv").children(".notiFeed").last().data("feedId")}
    getNotifications("/Application/getNotifications",params,null,$(this));
});
function getNotifications(urlStr,params,target,loadMoreDiv){
    showTopLoader();
    if(target=="NOTI_POPUP")notificationsEl=$("#notiHolder .notiDiv");
    else notificationsEl=$("#notiFeedsDiv");
    $.get(urlStr,params,function(data){
        hideTopLoader();
        if(loadMoreDiv)loadMoreDiv.remove();
        if(data.result.newsFeedCount>0){
             $.each(data.result.newsFeeds,function(i,feed){
                 if(notiFeedFns[feed.info.actionType]){
                    notiFeedFns[feed.info.actionType].init(feed);
                 }                
            });
            if(target=="NOTI_POPUP"&&data.result.newsFeedCount==params.size)$("#notiHolder .notiDiv").append("<div class='centry seeAllNotisDiv'><a id='showFullNotis'>See all Notifications</a></div>");
            else if(data.result.newsFeedCount==params.size)$("#notiFeedsDiv").append("<div id='seeOlderNotis' class='loadMoreItems'>See older</div>");
        }
        else showError("No notifications to show");            
    });
}


function getActivityFeeds(userId){
    var targetDiv=$("#activityFeedsDiv");
    $.get("/application/getActivityFeeds",{feedType:"NEW",id:userId,entityType:"USER",size:5},function(data){
        if(data.result.newsFeedCount>0){
            $.each(data.result.newsFeeds,function(i,feed){
                if(activityFeedFns[feed.info.actionType])
                    activityFeedFns[feed.info.actionType].init(feed);
            });
        }
        else targetDiv.append("<div class='userMessage'>No feeds available</div>");
        if(data.result.newsFeedCount==5)targetDiv.append("<div><a class='getOlderActivityFeeds' liner'\n\
         data-user-id='"+userId+"'>more</a></div>");
    });
}
$(".getOlderActivityFeeds").live('click',function(){
       var  activityFeedsEl=$(this).closest("#activityFeedsDiv");
        var $this=$(this),userId=$this.data("userId");
        var beforeId=activityFeedsEl.children(".notiFeed").last().data("feedId")
        $(this).append("<img src='/public/images/loading.gif' alt='loading..'>");
        $.get("/application/getActivityFeeds",{feedType:"OLD",id:userId,entityType:"USER",size:10,
            beforeNewsActivityId:beforeId},function(data){
            $this.parent().remove();
            if(data.result.newsFeedCount>0){
                $.each(data.result.newsFeeds,function(i,feed){
                    if(activityFeedFns[feed.info.actionType])
                        activityFeedFns[feed.info.actionType].init(feed);
                });
            }
            else activityFeedsEl.append("<div class='userMessage'>No more feeds available</div>");
            if(data.result.newsFeedCount==10)
                activityFeedsEl.append("<div><a class='getOlderActivityFeeds' liner'\n\
                data-user-id='"+userId+"'>more</a></div>");
        });
});
function getNewNewsFeeds(){
    setTimeout(function(){
        if(j[tabIndex("#contentMain")].tabType=="home"){
            $.get("/Application/getNewNewsFeeds",function(data){
                if(data.result.newsFeedCount>0){
                    $.each(data.result.newsFeeds,function(i,feed){
                        newsFeedFns[feed.eType](feed,"NEWS","#newNewsFeeds");
                    });
                   if(!$("#newFeedsNo").hasClass("gotSomeNews")){
                       $("#newFeedsNo").addClass("gotSomeNews");
                       $("#newFeedsNo label").html(data.result.newsFeedCount);
                   }
                   else $("#newFeedsNo label").html(parseInt($("#newFeedsNo label").html())+data.result.newsFeedCount);
                }
            });
        }
//            getNewNewsFeeds();
    },300000);
}

/*var tickers = new function(){
	var sufix = "/Feeds";
	var fetchUrl = sufix+"/getTicker";
	var flushUrl = sufix+"/flushTicker";
	var intervalInMS = 30000;//30 sec interval
	this.timerObj;
	var types = ["ORGANIZATION_ACTIVITY","SHARED_WITH_ME"];
	var cbFns = {"ORGANIZATION_ACTIVITY":undefined};
	this.start = function(){
		return;
		cbFns = {"ORGANIZATION_ACTIVITY":instituteTickerCB};
		if(this.timerObj) clearInterval(this.timerObj);
		this.timerObj = setInterval(fetch,intervalInMS);
		fetch();
	};
	var xhrObj;
	var fetch = function(){
		if(xhrObj) xhrObj.abort();
		var params = {'tickerType':'ORGANIZATION_ACTIVITY','callerUserId':USERID};
	   	xhrObj = $.get(fetchUrl,params,function(data,stat){
			if(data && data.result && data.result.tickers){
				var tickers = data.result.tickers;
				for(type in tickers){
					try{
						cbFns[type](tickers[type]);
					}catch(err){
						putConsoleError("Tickers Fetch : check 'newsfeed.js' = "+err);
					}
				}
			}else{
				chkLogout(data);
			}
			putConsoleLogs(data);
		}).error(function(err){
			error(err);		
		});
	};
	this.flush = function(type,cbFn){
		$.get(flushUrl,{'tickerType':type,'callerUserId':USERID},function(data,stat){
			try{ cbFn(data);}catch(err){
				putConsoleError("Tickers Flush : check 'newsfeed.js' = "+err);
			}
			chkLogout(data);
			putConsoleLogs(data);
		}).error(function(err){
			error(err);		
		});
	};
	var chkLogout = function(data){
		try{
			if(data.errorCode == "LOGOUT"){
				logoutService(data);
			}
		}catch(err){
			putConsoleError("Tickers Logout : check file newsfeed.js = "+err);
		}
	};
	var error = function(err){
		putConsoleError("Tickers ERROR : check file newsfeed.js = "+err);
	};
	setTimeout(function(){
		tickers.start();
	},1000);
}*/

