var statusMapper={};
statusMapper["AVAILABLE"]="CMBAvailable";
statusMapper["BUSY"]="CMBBusy";

var statusArray=["AVAILABLE","BUSY",'OFFLINE'];
var socket,windowActive=true,chatSound;
var chat_resp={},userGrpMapping={},shiftPress=false,myOnlineStatus=false,socketTimer,maskTime;
var chatSettingsSample,chatUserDivSample,chatInviteSample,chatReqPopupSample,chatDivSample,cdMyLineSample,cdOthersLineSample;
var chatDivsHolder,chatMainBlock,chatFrsDiv,chatFingDiv;
function chatFirstLoadFns(){
    chatMainBlock=$("#chatMainBlockHolder");
    chatDivsHolder=$("#chatDivsHolder");
    chatFrsDiv=$("#CMBFollowersDiv");
    chatFingDiv=$("#CMBFollowingDiv");
    chatSettingsSample=createCommonUIEl($("#chatSettingsSample"));
    chatUserDivSample=createCommonUIEl($("#chatUserDivSample"));
    chatInviteSample=createCommonUIEl($("#chatInviteSample"));
    chatReqPopupSample=createCommonUIEl($("#chatReqPopupSample"));
    chatDivSample=createCommonUIEl($("#chatDivSample"));
    cdMyLineSample=createCommonUIEl($("#CDMyLineSample"));    
    cdMyLineSample.find(".openMyProfile").removeClass("openMyProfile");
    cdOthersLineSample=createCommonUIEl($("#CDOthersLineSample"));
}

var vChat=new (function($){
    this.init=function(params){
        chatSound = document.getElementById('chatSound');
        chatSound.src = '/public/chatSound.wav'; 
        chatFirstLoadFns();
        chat_Connect.init();      
        chat_mainBlock.init();        
    }
})(jQuery);
var chat_Connect={
    init:function(){      
        this.makeConnection();
        chatMainBlock.on("click",".forceReconnect",this.forceReconnect);        
    },
    makeConnection:function(){
        socket= io.connect(CHAT_WEB_SERVICE_URL);
        socket.on('connect',function(){
            putConsoleLogs("=============> connected");
            chat_Connect.clearChatTimers();
            chat_Connect.registerUserAndFetchOnliners();
        });
        socket.on('error',function(){
             putConsoleLogs("=============> error in establishing connection");
              chat_Connect.clearChatTimers();
              chat_Connect.initiateReconnecting();
        });
        socket.on('disconnect',function(){
             putConsoleLogs("=============> disconnected connection");
              chat_mainBlock.onSignOut();
              chat_Connect.clearChatTimers();
              chat_Connect.initiateReconnecting();
        });
        socket.on('msg',function(data){
                chat_resp[data.action](data);
        });
        socket.on('connect_failed',function(){
             putConsoleLogs("=============> failed to connect");
        });
        socket.on('reconnect_failed',function(){
             putConsoleLogs("=============> failed to reconnect");
        });        
    },
    connectToChatServer:function(){
        socket.socket.reconnect();
    },
    initiateReconnecting:function(){
        var maskDiv=$("#CMBMask")
        $("#CMBMask").removeClass("nonner");
        var i=60;
        maskTime=setInterval(function(){
            $("#CMBMask .maskTime").text(i);
            maskDiv.find(".maskText").html("Unable to connect");
            maskDiv.find(".maskTimeDiv").removeClass("nonner");
            i--;
        },1000);
        socketTimer=setInterval(function(){
            maskDiv.find(".maskText").html("Reconnecting ...");
            i=60;
            chat_Connect.connectToChatServer();
        },60000);        
    },
    clearChatTimers:function(){
        clearInterval(socketTimer);
        clearInterval(maskTime);        
    },
    registerUserAndFetchOnliners:function(){
        socket.emit('register', {userId:USERID});
        socket.emit('msg',{from:USERID,action:"ONLINE_USERS",message:{userType:"BOTH",start:0,size:10}});        
    },
    forceReconnect:function(){
        chat_Connect.clearChatTimers();
        var maskDiv=$(this).closest("#CMBMask");
        chat_Connect.connectToChatServer();
        maskDiv.find(".maskText").html("Reconnecting ...");
        maskDiv.find(".maskTimeDiv").addClass("nonner");        
    }
}


var chat_chatDiv={
    init:function(chatDivWrapper){
        chatDivWrapper
        .on("click",".chatDiv",this.chatDivClick)
        .on("click",".minimizeChatDiv,.maximizeChatDiv",this.minMaxChatDivClick)
        .on("click",".closeChatDiv",this.closeChatDivClick)
        .on("keydown",".CDTextarea",this.CDTextareaKeydown)        
        .on("keyup",".CDTextarea",this.CDTextareaKeyup)        
        .on("focus",".CDTextarea",this.CDTextareaFocus) 
        .on("blur",".CDTextarea",this.CDTextareaBlur) 
    },
    chatDivClick:function(){
        $(this).find(".CDTextarea").focus();
        chat_chatDiv.stopBlinking($(this));
    },
    stopBlinking:function($this){
        stopBlinking($this.closest(".chatDivWrapper").data("userId"));
    },    
    minMaxChatDivClick:function(){
        $(this).closest(".chatDiv").find(".CDNonner").toggleClass("nonner");
        $(this).toggleClass("maximizeChatDiv minimizeChatDiv");        
    },
    closeChatDivClick:function(){
        var chatDivWrapper=$(this).closest(".chatDivWrapper"),userId=chatDivWrapper.data("userId");
        userGrpMapping[userId].chatLines=chatDivWrapper.find(".CDChatLinesBody").html();
        chat_mainBlock.blueChatIconToggle(userId);
        chat_chatDiv.stopBlinking($(this));
        chatDivWrapper.remove();
      //for grp chat just close the window        
    },
    CDTextareaKeydown:function(e){
        var chatWrapper=$(this).closest(".chatDivWrapper");
        stopBlinking(chatWrapper);        
            if(((e.which&&e.which==16)||(e.keycode&&e.keycode==16))){
                shiftPress=true;
           }        
    },    
    CDTextareaKeyup:function(e){
       var message=$(this).val().trim();       
       if(((e.which&&e.which==13)||(e.keycode&&e.keycode==13))&&message!=""&&!shiftPress){
           var chatDivWrapper=$(this).closest(".chatDivWrapper");
           var userId=chatDivWrapper.data("userId"),ugmap=userGrpMapping[userId];
           var grpId=ugmap.grpId;
           if(ugmap.isOnline){
               chatDivWrapper.find(".CDChatLinesBody").append(chat_chatDiv.getCDMyLine(message));
               socket.emit('msg',{from:USERID,action:"MSG",type:"P2PChat",group:grpId,to:grpId,message:filterText(message)});
           }
           else{
               chatDivWrapper.find(".CDChatLinesBody").append(chat_chatDiv.getCDMyLine(message));
               showError("The user is offline");
               //in the offline msg func put the scrolltop thing also
           }
           //if not insert into chat lines and say that the other guy is offline adn cannot receive messages
           $(this).val("");
           chat_chatDiv.positionChatScroll(chatDivWrapper);
       }
       shiftPress=false;        
    },
    CDTextareaFocus:function(){
        var chatWrapper=$(this).closest(".chatDivWrapper");
        stopBlinking(chatWrapper);
        chatWrapper.addClass("activeChatDivWrapper")
        .siblings().removeClass("activeChatDivWrapper");
        $(this).addClass("isFocused");
    },
    CDTextareaBlur:function(){
        $(this).removeClass("isFocused");
    },    
    

    
    //utilities
    getChatDiv:function(userId){
        var userData=userGrpMapping[userId];   
        var wrapper=chatDivSample.children().clone(true);
        wrapper.find(".CDHeadUserImg").attr("src",userData.thumbnail).data("userId",userId);
        wrapper.find(".CDHeadUserName").text(userData.fullname).data("userId",userId);
        wrapper.attr("id","chatWrapper_"+userData.grpId).data("userId",userId);
        chat_chatDiv.init(wrapper);
        return wrapper;        
    },
    prepareChatDiv:function(userId,getLine){
        if(!getLine)getLine="";
        var ugmap=userGrpMapping[userId],chatLines="";
        if(ugmap&&ugmap.chatLines)chatLines=ugmap.chatLines;
        chat_chatDiv.cleanChatDiv();
        var chatWrapper=chat_chatDiv.getChatDiv(userId);
        chatWrapper.find(".CDChatLinesBody").append(chatLines).append(getLine);
        chatDivsHolder.append(chatWrapper);
        chat_mainBlock.blueChatIconToggle(userId);
        chat_chatDiv.positionChatScroll(chatWrapper);        
    },
    getOfflineMessage:function(){
        var el=makeHTMLTag('div',{});
        el.html("<div class='CDOfflineMessage'>User is offline and \n\
        will not be able to receive your messages</div>");
        return el.children();        
    },
    getCDMyLine:function(entry){
        var CDMyLine=cdMyLineSample.children().clone(true);
        CDMyLine.find(".CDCLText").html(urlify(filterText(entry)));
        return CDMyLine;        
    },
    getCDOthersLine:function(text,userId){
        var userData=userGrpMapping[userId];
        var CDOthersLine=cdOthersLineSample.children().clone(true);
        CDOthersLine.find(".CDLineUserImg").attr("src",userData.thumbnail).data("userId",userId);
        CDOthersLine.find(".CDCLText").html("<a class='boldy' data-user-id='"+userId+"'>\n\
        "+userData.fullname+" </a> :"+urlify(filterText(text)));
        return CDOthersLine;        
    },
    cleanChatDiv:function(){
        var wrappers=chatDivsHolder.children(".chatDivWrapper");
        if(wrappers.length>=4){
           var removableDiv=wrappers.first();
           if(removableDiv.hasClass("activeChatDivWrapper")){
               removableDiv=wrappers.eq(1);
           }
            var userId=removableDiv.data("userId");
            chat_mainBlock.blueChatIconToggle(userId);
            userGrpMapping[userId].chatLines=removableDiv.find(".CDChatLinesBody").html();
            removableDiv.remove();
        }
    },
    positionChatScroll:function(targetDiv){
        if(targetDiv.length>0){
            var linesDiv=targetDiv.find(".CDChatLines");
            linesDiv.scrollTop(linesDiv.children(".CDChatLinesBody").height());                            
        }        
    }    
}





var chat_mainBlock={
    init:function(){
        chatMainBlock
        //starting a chat with user
        .on("click",".CMBUserDiv",this.CMBUserDivClick)
        .on("click",".sendChatReq",this.sendChatReqClick)
        .on("click",".chatReqAccept",this.chatReqAcceptClick)
        .on("click",".chatReqDecline",this.chatReqDeclineClick)
        .on("click","#CMBIHWrapper",this.CMBIHWrapperClick)
        
        //user settings        
        .on("click",".CMBSettings",this.CMBSettingsClick)
        .on("click",".CMBStatusSet",this.CMBStatusSetClick)        
        .on("click",".CMBMyStatus",this.CMBMyStatusClick)
        .on("click",".CMBSetStatusMessage",this.CMBSetStatusMessageClick)
        .on("keyup",".CMBStatusInput",this.CMBStatusInputKeyup)
        .on("blur",".CMBStatusInput",this.CMBStatusInputBlur)
        
        //sign in and out of chat
        .on("click",".signOutOfChat",this.signOutOfChatClick)
        .on("click",".signInToChat",this.signInToChatClick)
        
        //tail
        .on("click","#CMBTail",this.CMBTailClick)  
    },
    
    //starting a chat with a user
    CMBUserDivClick:function(){
        var targetUserId=$(this).data("userId"),ugmap=userGrpMapping[targetUserId];
        if(!myOnlineStatus){
            showError("Please sign to chat");
            return;
        }
        if(ugmap&&ugmap.grpId){
            var grpId=ugmap.grpId;
            var chatDiv=chatDivsHolder.children("#chatWrapper_"+grpId);
            if(chatDiv.length==0){
                 chat_chatDiv.prepareChatDiv(targetUserId);
            }
            else{
                chatDiv.find(".CDTextarea").focus();
            }
        }
        else{
            if(ugmap&&ugmap.isOnline){
                var chatPopup=chatReqPopupSample.children().clone(true);
                var popupHolder=$("#chatReqPopupHolder");
                popupHolder.html(chatPopup);
                popupHolder.find("input").focus();
                popupHolder.children("#chatReqPopup").data("targetUserId",targetUserId);
                popupHolder.find(".chatReqUserImg").attr("src",$(this).find(".CMBUserImg").attr("src"));
                addToggler(chatPopup,$(this));
            }
            else showError("You cannot chat with an offline user");
        }        
    },
    sendChatReqClick:function(){
       var popup=$(this).closest("#chatReqPopup");
       var targetUserId=popup.data("targetUserId");
       socket.emit('msg',{from:USERID,action:"START",type:"P2PChat",
           to:targetUserId,message:popup.find(".chatReqPurposeInput").val()});
       insideOutClick();        
    },
    chatReqAcceptClick:function(){
        var inviteData=$(this).closest("#CMBInviteHolder").data("inviteData");
        socket.emit('msg',{from:USERID,action:"JOINED",type:"P2PChat",group:inviteData.group,
            to:inviteData.group,message:socket.socket.sessionid});
        chat_mainBlock.createChatDivAfterGrpJoin(inviteData.from,inviteData.group);
        $("#CMBIHWrapper").html("");        
    },
    chatReqDeclineClick:function(){
        var inviteData=$(this).closest("#CMBInviteHolder").data("inviteData");
        socket.emit('msg',{from:USERID,action:"DECLINE",type:"P2PChat",group:inviteData.group,to:inviteData.group});
        $("#CMBIHWrapper").html("");        
    },
    CMBIHWrapperClick:function(){
        stopBlinking("INVITE");  
    },
    

    //user settings
    CMBSettingsClick:function(){
        addToggler($(this).siblings(".CMBStatusUpdateDiv"),$(this));        
    },
    CMBStatusSetClick:function(){
        var status=statusArray[$(this).index()];
        socket.emit('msg',{from:USERID,action:"UPDATE_STATUS",message:{userId:USERID,
                status:status},type:"P2PChat"});
        chat_mainBlock.statusUpdate(status);
        insideOutClick();        
    },   
    CMBMyStatusClick:function(){
        chat_mainBlock.prepareStatusArea($(this),$(this).text());
    },
    CMBSetStatusMessageClick:function(){
        chat_mainBlock.prepareStatusArea($(this),"");
    },    
    prepareStatusArea:function($this,txt){
        $this.siblings(".CMBStatusInput")
        .removeClass("nonner").val(txt).focus();        
    },
    CMBStatusInputKeyup:function(e){
       if(((e.which&&e.which==13)||(e.keycode&&e.keycode==13))){
           chat_mainBlock.updateStatusMessage($(this));
       }        
    },
    CMBStatusInputBlur:function(){
         chat_mainBlock.updateStatusMessage($(this));
    },
    updateStatusMessage:function($this){
        var statusMessage=$this.val().trim();
        socket.emit('msg',{from:USERID,action:"UPDATE_STATUS",
            message:{userId:USERID,statusMessage:statusMessage},type:"P2PChat"});
        this.statusUpdate(null,statusMessage);
        $this.addClass("nonner");        
    },
    statusUpdate:function(status,statusMessage){
        var cmbhead=chatMainBlock.find("#CMBHead");
        if(status)cmbhead.find(".CMBBubble").removeClass().addClass("CMBBubble "+statusMapper[status]).data("status",status);
        var statusDiv=cmbhead.find(".CMBStatus").removeClass();
        if(statusMessage!=undefined){            
            if(statusMessage!=""){
                statusDiv.addClass("CMBStatus CMBMyStatus").text(statusMessage);
            }else {
                statusDiv.addClass("CMBStatus CMBSetStatusMessage").text("Set status here");
            }                    
        }
        else if(status=="OFFLINE"){
            statusDiv.addClass("CMBStatus signInToChat").text("Sign in");
        }
    },    
    othersStatusUpdate:function(target,status,statusMessage){
        target.find(".CMBBubble").removeClass().addClass("CMBBubble "+statusMapper[status])
        .data("status",status);
        if(statusMessage!=undefined)target.find(".CMBStatus").text(statusMessage);         
    },
    
    //sign in and out of chat
    signOutOfChatClick:function(){
        socket.emit('msg',{from:USERID,action:"USER_LEFT"});
        chat_mainBlock.onSignOut();        
    },    
    signInToChatClick:function(e){
        e.stopPropagation();
        $(this).html("Signing ..");
        chat_Connect.registerUserAndFetchOnliners();        
    },
    onSignOut:function(){
        var CMBHead=$("#CMBHead");
        myOnlineStatus=false;
        //on cmb head
        CMBHead.find(".CMBSettingsDiv").html("");
        chat_mainBlock.statusUpdate("OFFLINE");
        CMBHead.find(".CMBStatus").html("<a class='signInToChat liner smally'>\n\
            Sign in to chat</a>").removeClass("CMBMyStatus");
        CMBHead.find(".CMBSettingsDiv").addClass("nonner");

        //on frnds and their chat divs
        chatDivsHolder.children(".chatDivWrapper").each(function(){
            var userId=$(this).data("userId");
            userGrpMapping[userId].chatLines=$(this).find(".CDChatLinesBody").html();
            chat_chatDiv.stopBlinking($(this));
            $(this).remove();        
        });
        chatMainBlock.find(".CMBUserDiv").each(function(){
           chat_mainBlock.othersStatusUpdate($(this),"OFFLINE");
        });
        $.each(userGrpMapping,function(userId,obj){
            obj.grpId=undefined;
        });

        //cmbtail
        setOnlineUsersCount();
        insideOutClick();        
    },
    
    //tail 
    CMBTailClick:function(){
        var CMB=$(this).closest("#chatMainBlockHolder");
        CMB.find("#CMBNonner").toggleClass("nonner");
        $(this).toggleClass("CMBTailBordered");        
    },
    
    
    //utilities
    populateCMBUsers:function(users,targetDiv){
        targetDiv.children(".userMessage").remove();
        if(users.length>0){
            for(var k=0;k<users.length;k++){
                var user=users[k],name=user.firstName+" "+user.lastName;
                var CMBUserDiv=chatUserDivSample.children().clone(true),ugmap=userGrpMapping[user.userId];
                CMBUserDiv.data("userId",user.userId).attr("rel",user.userId);                
                if(!ugmap){
                    userGrpMapping[user.userId]={fullname:name,thumbnail:user.thumbnail,isOnline:true};
                }
                CMBUserDiv.find(".CMBUserImg").attr("src",user.thumbnail);
                CMBUserDiv.find(".CMBName").text(name);
                chat_mainBlock.othersStatusUpdate(CMBUserDiv,user.status,user.statusMessage);                               
                targetDiv.append(CMBUserDiv);
            }
        }
        else{
            targetDiv.append("<div class='userMessage'>No Users Online</div>");
        }
    },
    createChatDivAfterGrpJoin:function(targetUserId,grpId){
        var user= chatMainBlock.find(".CMBUserDiv[rel='"+targetUserId+"']");
        if(user.length>0){
            userGrpMapping[targetUserId].grpId=grpId;
            var userId=user.data("userId");
            chat_chatDiv.cleanChatDiv();
            chat_mainBlock.blueChatIconToggle(userId);
            var chatDiv=chat_chatDiv.getChatDiv(userId);
            chatDivsHolder.append(chatDiv);            
        }
        else showError("There is some error in initiating the chat");
    },
    blueChatIconToggle:function(userId){
        var CMBUserDiv=chatMainBlock.find(".CMBUserDiv[rel='"+userId+"']");
        CMBUserDiv.find(".CMBChatDivStatus").toggleClass("CMBChatDivActive CMBChatDivSendReq");
    },
    userMoved:function(grpId,offlineMsg){
        var chatDiv=chatDivsHolder.find("#chatWrapper_"+grpId);        
        if(grpId&&chatDiv.length>0){
            chatDiv.find(".CDOfflineMessageDiv").html(offlineMsg);
        }
        setOnlineUsersCount();        
    }
}




//chat response handling
chat_resp["connected"]=function(data){
  //  putConsoleLogs(data);
}
chat_resp["LIVE_GROUPS_FETCHED"]=function(data){
    var liveGrps=data.message;
    if(liveGrps.length>0){
        for(var k=0;k<liveGrps.length;k++){
            var grp=liveGrps[k], members=grp.members.split(","),ugmap=userGrpMapping[members[0]];
            if(members.length==1&&ugmap){
                ugmap.grpId=grp.room;
                socket.emit('msg',{from:USERID,action:"JOINED",type:"P2PChat",group:grp.room,to:grp.room,message:"LOGIN_JOIN"});
            }
            else {
                //for grp chat ,assume grp as user and do the mapping
            }
        }
    }
}
chat_resp["ONLINE_USERS"]=function(data){
    var d=data.message,userInfo=data.message.userInfo;
    var CMBHead=chatMainBlock.find("#CMBHead");
    chatMainBlock.find("#CMBMask").addClass("nonner");
    chat_Connect.clearChatTimers();
    CMBHead.find(".CMBSettingsDiv").html(chatSettingsSample.children().clone(true)).removeClass("nonner");
    myOnlineStatus=true;
    chatFingDiv.html("");
    chatFrsDiv.html("");
    chat_mainBlock.populateCMBUsers(d.followerUsers,chatFrsDiv);
    chat_mainBlock.populateCMBUsers(d.followingUsers,chatFingDiv);
    setOnlineUsersCount();
    chat_mainBlock.statusUpdate(userInfo.status,userInfo.statusMessage);
    socket.emit('msg',{from:USERID,action:"GET_LIVE_GROUPS"});
}
function setOnlineUsersCount(){
    var c=chatMainBlock;
    var following=chatFingDiv.find(".CMBBusy,.CMBAvailable").length;
    var followers=chatFrsDiv.find(".CMBBusy,.CMBAvailable").length;
    c.find(".CMBTailFollowersNo").text(followers);
    c.find(".CMBTailFollowingNo").text(following);
}
chat_resp["UPDATE_STATUS"]=function(data){
    var user=data.message,userDiv=chatMainBlock.find(".CMBUserDiv[rel="+user.userId+"]");
    chat_mainBlock.othersStatusUpdate(userDiv,user.status,user.statusMessage);
}
chat_resp["JOINED"]=function(data){
    if(data.from!=USERID&&data.message!="LOGIN_JOIN"){
        chat_mainBlock.createChatDivAfterGrpJoin(data.from,data.group);
        if(!windowActive)chatSound.play();
        startBlinking(userGrpMapping[data.from].fullname+" accepted your a request",data.from);        
    }
    else if(data.from==USERID&&data.message!=socket.socket.sessionid){
        var c=chatMainBlock;
        var inviteData=c.find("#CMBInviteHolder").data("inviteData");
        chat_mainBlock.createChatDivAfterGrpJoin(inviteData.from,inviteData.group);
        c.find("#CMBIHWrapper").html("");
    }
}
chat_resp["INVITE"]=function(data){
    var fromUserId=data.from,c=chatMainBlock;
    var user=c.find(".CMBUserDiv[rel='"+fromUserId+"']");
    if(user.length>0){
        var CMBIH=chatInviteSample.children().clone(true);
        var fullname=user.find(".CMBName").text();
        CMBIH.data("inviteData",data);
        CMBIH.find(".CMBIHUserImg").attr("src",user.find(".CMBUserImg").attr("src"));
        CMBIH.find(".CMBIHUserName").text(fullname);
        if(data.message!=""){
            CMBIH.find(".CMBIHPurposeDiv").html("<span class='boldy'>Purpose: </span> "+data.message);
        }
        c.find("#CMBIHWrapper").html(CMBIH);
        if(!windowActive)chatSound.play();
        startBlinking(fullname+" sent you a request","INVITE");
    }
}
chat_resp["DECLINE"]=function(data){
    var name=userGrpMapping[data.from].fullname;
    if(name){
        showError(name+" declined your chat request");
    }
    else showError("Your chat request is declined");

}
chat_resp["MSG"]=function(data){
    var chatWrapper=chatDivsHolder.children("#chatWrapper_"+data.group);
    var getLine="";
    if(data.from!=USERID){
        getLine=chat_chatDiv.getCDOthersLine(data.message,data.from);
    }
    if(chatWrapper.length>0){
        chatWrapper.find(".CDChatLinesBody").append(getLine);
        chatWrapper.find(".CDNonner").removeClass("nonner");
        chat_chatDiv.positionChatScroll(chatWrapper);
    }
    else{
        chat_chatDiv.prepareChatDiv(data.from,getLine);                     
        startBlinking(userGrpMapping[data.from].fullname+" says...",data.from); 
    }
    if(data.from!=USERID){
        var name=name=userGrpMapping[data.from].fullname;
        if(!windowActive){
            chatSound.play();
            startBlinking(name+" says...",data.from);
        }
        else if(!chatWrapper.find(".CDTextarea").hasClass("isFocused")){
            startBlinking(name+" says...",data.from);
        }          
    }  
}
chat_resp["USER_LEFT"]=function(data){
    if(data.from!=USERID){
        var c=chatDivsHolder,ugmap=userGrpMapping[data.from];
        ugmap.isOnline=false;
        var CMBUserDiv=c.find(".CMBUserDiv[rel='"+data.from+"']");
        chat_mainBlock.othersStatusUpdate(CMBUserDiv,"OFFLINE");
        var grpId=ugmap.grpId;
        chat_mainBlock.userMoved(grpId,chat_chatDiv.getOfflineMessage());
    }
    else if(data.from==USERID){
        socket.emit('unregister', {userId:USERID,from:USERID});
        chat_mainBlock.onSignOut();
    }
}
chat_resp["USER_ONLINE"]=function(data){
    var c=chatDivsHolder;
    if(data.from!=USERID){
        var CMBUserDiv=c.find(".CMBUserDiv[rel='"+data.from+"']");
        var user=data.message;
        if(CMBUserDiv.length==0){
            var targetDiv=chatFrsDiv;
            if(data.relationToTarget=="FOLLOWING")targetDiv=chatFingDiv;
          chat_mainBlock.populateCMBUsers([user],targetDiv);
        }
        CMBUserDiv=c.find(".CMBUserDiv[rel='"+data.from+"']");
        var ugmap=userGrpMapping[data.from];
        ugmap.isOnline=true;
        chat_mainBlock.othersStatusUpdate(CMBUserDiv,user.status,user.statusMessage);
        var grpId=ugmap.grpId;
        chat_mainBlock.userMoved(grpId,"");
    }
}

chat_resp["FAILURE"]=function(data){
    return;
}




//user isOnline is put when action ONLINE_USERS is there and USER_ONLINE(ie, user came online)
//user to grpId mapping done when user when THe LIVE_GROUPS_FETCHED and JOINED takes place.
//When User_LEFT takes place, the isOnline is set to false
// NOTE the groupId is never removed from mapping




//for grp chat
//1)whena person upgrades a normal chat to grp chat,
        //in the usermap make grpId undefined, then create a usermap for grpId with profile pic,fullname undefined,grpId set to grpId itslef
        //and set the chatdiv.data('grpId') to grpId and the chatdiv.data('type') to grpChat
//2) same for grp with a name where fullname=name and rest of all same





$(window).on("focus",function(){
    windowActive=true;
}).on("blur",function(){
    windowActive=false;
});
var storeDocTitle=document.title,blinkCount,newBlinkTitle,blinkTimer,blinkSource="";
function startBlinking(msg,source){
    setBlink(msg);
    clearTimeout(blinkTimer);
    blinkTitle();    
    if(source)blinkSource=source;
    else blinkSource="";
}
function stopBlinking(source){     
    if(source==blinkSource)clearTimeout(blinkTimer);
    document.title=storeDocTitle;
}
function setBlink(newTitle){
    storeDocTitle=document.title;
    blinkCount=0;
    newBlinkTitle=newTitle;
}
function blinkTitle(){    
    blinkTimer=setTimeout(function(){
        blinkCount++;
        if(blinkCount%2==0)document.title=storeDocTitle;
        else document.title=newBlinkTitle;         
        blinkTitle();
    },1000)    
}



