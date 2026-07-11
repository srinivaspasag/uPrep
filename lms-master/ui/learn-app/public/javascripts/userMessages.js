var userMessages = new function(){
    var msgNavigator = "#msgNavigator";
    var parDivId = "#msgsHome";
    var navTabClicked = function(){
        $(this).addClass("clMsgTab").siblings().removeClass("clMsgTab");
    };
    this.init = function(){
        if($(parDivId).data("inited")) return;
        $("#msgNavigator").on("click",".msgNavTab",navTabClicked)
            .on("click",".openMsgInbox",this.inbox.open);
        $(parDivId).on("mouseenter mouseleave",".msgOtherRecieversList",recieversToolTip);
        $(parDivId).data("inited",true);
    };
    var animate = this.animate = function(msg,thisOnly){
        $(msg).animate({"left":"0px"},200,"linear",function(){
            if(!thisOnly){
                animate($(this).next());
            }
        });
    };
    var recieversToolTip = function(e){
        var count = parseInt($(this).data("count"),10);
        if(count<=0){ return;}
        var conv = $(this).closest(".openMsgConversation");
        var convId = conv.data("conversationId");
        var senderId = conv.data("senderId");
        toolTipFn(e,function(){
            clearToolTipXHR();
            var pr = {"conversationId":convId,"start":0,"size":10,"senderId":senderId};
            pr["excludeUserIds"] = [senderId,USERID];
            var xhr = vReq.get("/UserMessages/getTooltipUsers",pr,function(html){
                if(checkToolTipXHR()){
                    vtooltip.show(e,html,function(){
                        //$(".toolTipUser-"+senderId).remove();
                        //$(".toolTipUser-"+USERID).remove();
                    });
                }
            });
            setToolTipXHR(xhr);
            return "Loading..";
        },0);
    };
    var preventAndReturn = function(e){
        e.originalEvent.returnNow = true;
    };
    this.inbox = new function(){
        var parDivId = "#msgsInbox";
        var start = 0;
        var size = 10;
        var totalHits = 0;
        var curCount = 0;
        this.open = function(e){
            var holder = $("#msgsHeader").find("#msgsHome");
            showTopLoader();
            $.get("/UserMessages/inbox",function(data){
                hideTopLoader();
                holder.html(data);
                instHeader.getCount();
            });
            pushInstHistory($("#myInstitutePage").data('orgId'),"messages/inbox/");
            if(e) e.preventDefault();
        };
        this.init = function(){
            userMessages.init();
            msgNavigator = $(msgNavigator);
            msgNavigator.find(".clMsgTab").removeClass("clMsgTab");
                    msgNavigator.find(".msgNavTab.openMsgInbox").addClass("clMsgTab");
            $(parDivId)
                .on("click",".msgSelectionCHK",convChecked)
                .on("click",".msgSelectionCHK,.openInstProfile",preventAndReturn)
                .on("click",".msgInboxRightArrow,.msgInboxLeftArrow",fetchMore)
                .on("click",".msgConvDeleteButton",deleteConvSelected);
            this.onBack();
        };
        this.onBack = function(){
            start = urlQueryHelper.read("start");
            start = start ? parseInt(start) : 0;
            fetch();
            return true;
        };

        var deleteConvSelected = function(){
            var parDiv = $(parDivId);
            var list = parDiv.find(".msgSelectionCHK:checked");
            var success = false;
            parDiv.find(".msgConvDeleteButton").fadeTo(150,0);
            list.each(function(){
                var conv = $(this).closest(".msgEachOne");
                var pr = {"userConversationId":conv.find(".openMsgConversation").data("userConversationId")};
                $(conv).animate({"left":"100%"},300,"linear",function(){
                    $(this).remove();
                });
                vReq.post("/UserMessages/deleteConversation",pr,function(data){
                    var par = $(parDivId).find(".msgInboxNavig");
                    curCount--;
                    totalHits--;
                    if(!totalHits){
                        par.find(".msgInboxCountDiv").fadeTo(0,0);
                        return;
                    }
                    par.find(".msgInboxCount").text((start+1)+"-"+(curCount+start));
                    par.find(".msgInboxTotal").text(totalHits);
                });
            });
        };
        var convChecked = function(e){
            var parDiv = $(parDivId);
            var $this = $(this);
            var all = parDiv.find(".msgSelectionCHK").is(":checked");
            if(all){
                parDiv.find(".msgConvDeleteButton").fadeTo(250,1);
            }else{
                parDiv.find(".msgConvDeleteButton").fadeTo(150,0);
            }
            if($(this).is(":checked")){
                $this.closest(".msgEachOne").addClass("convChked");
            }else{
                $this.closest(".msgEachOne").removeClass("convChked");
            }
            var isChecked = $this.is(":checked");
            setTimeout(function(){
                $this.prop("checked",isChecked);
            },10);
            return false;
        };
        var fetch = function(){
            var holder = $(parDivId).find(".msgInboxContainer");
            bigLoader(holder);
            var params = {start:start,size:size};
            $(parDivId).find(".msgInboxLeftArrow").addClass("leftArrowDisabled");
            vReq.get("/UserMessages/getInboxConversations",params,function(data){
                holder.html(data);
                updateCount(holder);
                animate(holder.find(".msgEachOne:first"));
            });
        };
        var fetchMore = function(){
            var $this = $(this);
            var future = $this.data("future");
            var holder = $(parDivId).find(".msgInboxContainer");
            var timestamp;
            if(future){
                if($(this).hasClass("leftArrowDisabled")) return;
                start -= size;
                timestamp = holder.find(".openMsgConversation:first").data("timestamp");
            }else{
                if($(this).hasClass("rightArrowDisabled")) return;
                start += size;
                $(this).siblings().removeClass("leftArrowDisabled");
                timestamp = holder.find(".openMsgConversation:last").data("timestamp");
                future = false;
            }
            fetchNext(holder,future,timestamp);
        };
        var fetchNext = function(holder,future,timestamp){
            start = start < 0 ? 0 : start;
            var params = {start:start,size:size,"timestamp":timestamp};
            bigLoader(holder);
            vReq.get("/UserMessages/getMoreInboxConversations",params,function(data){
                holder.html(data);
                updateCount(holder);
                animate(holder.find(".msgEachOne:first"));
            });
        };
        var updateCount = function(holder){
            var par = $(parDivId).find(".msgInboxNavig");
            curCount = parseInt(holder.find("#msgInboxCountInput").remove().val());
            totalHits = parseInt(holder.find("#msgInboxTotalHits").remove().val());
            if(!totalHits){
                par.find(".msgInboxCountDiv").fadeTo(0,0);
                return;
            }
            par.find(".msgInboxCount").text((start+1)+"-"+(curCount+start));
            par.find(".msgInboxTotal").text(totalHits);
            if(start+curCount<totalHits){
                par.find(".msgInboxRightArrow").removeClass("rightArrowDisabled");  
            }else{
                par.find(".msgInboxRightArrow").addClass("rightArrowDisabled"); 
            }
            if(start>0){
                par.find(".msgInboxLeftArrow").removeClass("leftArrowDisabled");
            }else{
                par.find(".msgInboxLeftArrow").addClass("leftArrowDisabled");
            }
            urlQueryHelper.push("start",start);
        };
    };
    this.conversation = new function(){
        var parDivId = "#msgConversation";
        this.init = function(){
            userMessages.init();
                    $(msgNavigator).find(".msgNavTab.openMsgInbox").addClass("clMsgTab");
            var parDiv = $(parDivId);
            parDiv.on("click",".msgConvPostReply.RTEClosed",openReplyBox)
                .on("click",".cancelMsgConvReply",closeReplyBox)
                .on("click",".convLoadOlderMsgs",getMsgsBefore)
                .on("click",".msgSelectionCHK,.openInstProfile",preventAndReturn)
                .on("click",".RTEImageDiv",rteImageOpen)
                .on("click",".submitMsgConvReply",reply);
            assignRTEs(parDiv.find(".msgConvPostRTE"));
            getFirstMsg();
        };
        var rteImageOpen = function(e){
            showImagePreview($(this).html());
            if(e) e.preventDefault();
            return false;
        };
        var openReplyBox = function(){
            $(this).removeClass("RTEClosed").find(".RTEHolder").addClass("RTEActive");
            $(this).find(".RTEArea").focus();
        };
        var closeReplyBox = function(){
            $(parDivId).find(".msgConvPostReply").addClass("RTEClosed");
        };
        var doMathJax = function(div){
            div = div ? $(div) : $(parDivId);
            try{
                if(MathJax && MathJax.Hub){
                    MathJax.Hub.Queue(["Typeset",MathJax.Hub],div.get(0));
                }
            }catch(err){}
        };
        var getFirstMsg = function(){
            var parDiv = $(parDivId);
            var holder = parDiv.find(".msgConvMsgBody");
            doMathJax(holder);
            getMsgs();
        };
        var getMsgs = function(){
            var parDiv = $(parDivId);
            var convId = parDiv.data("conversationId");
            var holder = parDiv.find(".msgConvPostsContainer");
            var total =  parseInt(parDiv.find(".convCount-"+convId).text())-1;
            var params = {"curMsgs":0,size:3,"conversationId":convId,"totalMsgs":total};
            params["ignoreMsgId"]=parDiv.find(".msgConvMsgBody").data("msgId");
            vReq.get("/UserMessages/getMessages",params,function(htmlData){
                holder.html(htmlData);
                animate(holder.find(".msgConvEachPost:first"));
                doMathJax(holder);
            });
        };
        var getMsgsBefore = function(){
            var $this = $(this);
            smallLoader($this);
            var parDiv = $(parDivId);
            var convId = parDiv.data("conversationId");
            var firstConv = parDiv.find(".msgConvPostsContainer").find(".msgConvEachPost:first");
            var userMsgId = firstConv.data("userMsgId");
            var total = parseInt(parDiv.find(".convCount-"+convId).text())-1;
            var params = {
                "curMsgs":parDiv.find(".msgConvPostsContainer").find(".msgConvEachPost").length,
                size:3,
                "conversationId":convId,
                "userMessageId":userMsgId,
                "totalMsgs":total
            };
            params["ignoreMsgId"]=parDiv.find(".msgConvMsgBody").data("msgId");
            vReq.get("/UserMessages/getMessagesBefore",params,function(htmlData){
                $this.remove();
                firstConv.before(htmlData);
                animate(parDiv.find(".msgConvEachPost:first"));
                doMathJax(firstConv.parent());
            });
        };
        var reply = function(){
            var params = {};
                var getRTE=vRTE.getRTEContent;
            var parDiv = $(parDivId);
            var rteHolder = parDiv.find(".msgConvPostRTE").children(".RTEHolder");
            if(vRTE.isRTEEmpty(rteHolder)){
                showError(i18nJS("USER_MSGS_REPLY_CONTENT_MISSING"));
                return;
            };
                params.content = getRTE(rteHolder);
            params.conversationId = parDiv.data("conversationId");      
            params.parentMessageId = parDiv.find(".msgConvMsgBody").data("msgId");
            params.sender = {"type":"USER","id":parDiv.find(".msgConvPostReply").data("senderId")};
            params.action = "REPLY";
            params = {"message":params};
            closeReplyBox();
            showTopLoader();
            vReq.post("/UserMessages/postMessage",params,function(data){
                if(data && data.result && data.result.isReceived){
                    var parDiv = $(parDivId);
                    var holder = parDiv.find(".msgConvPostsContainer");
                        cleanRTE(rteHolder.find(".RTEArea"));
                    var convId = parDiv.data("conversationId");
                    parDiv.find(".convCountPerConv").removeClass("nonner");
                    var countDiv = parDiv.find(".convCount-"+convId);
                    countDiv.text(parseInt(countDiv.text(),10)+1); 
                    var pr = {"message":JSON.stringify(data.result.message)};
                    vReq.get("/UserMessages/postMessageUi",pr,function(htmlData){
                        hideTopLoader();
                        holder.append(htmlData);
                        animate(holder.find(".msgConvEachPost:last"),true);
                        doMathJax(holder);
                    },function(){
                        hideTopLoader();
                    });
                }else{
                    hideTopLoader();
                    $(parDivId).find(".msgConvPostReply").removeClass("RTEClosed");
                }
            },function(){
                hideTopLoader();
                $(parDivId).find(".msgConvPostReply").removeClass("RTEClosed");
            });
        };
    };
};

var urlQueryHelper = new function(){
    var url;
    var readAll = this.readAll = function(){
        var params = getAllUrlParams();
        return params;
    };
    function getURLParameter(name){
    var param="";
    try{
    var url = location.search;
    if(!url){ return param;}
    url = url.replace("?","");
    name += "=";
    var nameIndex = url.indexOf(name);
    if(nameIndex>=0){
        param = url.substr(nameIndex+name.length);
        if((eIndex = param.indexOf("&"))>0){
            param = param.substring(0,eIndex);
        }
        param = decodeURIComponent(param);
    }
    }
     catch(err){ param="";}
     return param;
}
    this.getQueryString = function(){
        var string = location.href;
        string = string.split("?")[1];
        string = string ? "?"+string : "";
        return string;
    };
    this.read = function(name){
        return getURLParameter(name);
    };
    this.push = function(name,val){
        writeUrl(name,val);
    };
    this.replace = function(name,val){
        writeUrl(name,val,true);
    };
    var writeUrl = function(name,val,isReplace){
        val = val?encodeURIComponent(val):val;
        var loc = location.pathname + location.search;
        var newLoc = loc;
        if(loc.contains(name)){
            newLoc = loc.substring(0,loc.indexOf("?"));
            var params = readAll();
            if(val){
                params[name] = val;
            }else{
                $(params).removeProp(name);
            }
            newLoc += "?";
            for(p in params){
                newLoc += p+"="+params[p]+"&";
            }
            newLoc = newLoc.substr(0,newLoc.length-1);
        }else if(val){
            var joinChar = newLoc.contains("?") ? "&" : "?";
            newLoc += joinChar + name + "=" + val;
        }
        if(isReplace){
            pushHistory(null,null,newLoc,true);
        }else{
            pushHistory(null,null,newLoc);
        }
    };
};