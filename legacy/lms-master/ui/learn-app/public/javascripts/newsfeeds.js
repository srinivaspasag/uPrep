var newsEntityProcessor = new function(){
    var _ACTOR = "%{ACTOR}";
    var _ENTITY = "%{ENTITY}";
    var _ENTITY_NON_CLICK = "%{ENTITY_NON_CLICK}";
    var _REASON = "%{REASON}";
    var _ENTITY_OWNER = "%{ENTITY_OWNER}";
    var _ENTITY_OWNER_PLURAL = "%{ENTITY_OWNER_PLURAL}";
    var _ROOT_OWNER = "%{ROOT_OWNER}";
    var _ROOT_ENTITY = "%{ROOT_ENTITY}";
    var _AORAN = "%{A/AN}";
    var VOWELS = ['A','E','I','O','U'];
    var SUPPORTED_TYPES = ["VOTED","ADDED","COMMENTED","ATTEMPTED","FOLLOWED","SHARED","MADE_VISIBLE","ENDED"];
    this.process = function(feedLocation,feed,actionType,clustered){
        var feeds = [feed];
        if(clustered){
            var lastFeedId = feed.lastNewsFeedId;
            feeds = feed.clusteredNews;
            feed = feed.clusteredNews[0];
            feed.newsFeedId = lastFeedId;
        }
        var src = feed.src;
        var eventType = feed.eType;
        var textHtml = getActionTypeText(actionType,src.type,eventType,feed);
        var root = src.rootDetails;
        var actor = feed.actor;
        var reason = feed.why;
        if(root){
            textHtml = textHtml.ROOT;
            if(reason == "ROOT_OWNER"){
                textHtml = textHtml.replace(_ROOT_OWNER,i18nJS("TXT_YOUR"));
            }else{
                textHtml = textHtml.replace(_ROOT_OWNER,"");
            }
            textHtml = textHtml.replace(_ROOT_ENTITY,getSrcEntity(root,feed,true));
        }else{
            textHtml = textHtml.SRC;
        }
        if(src){
            var user = getOwnerUser_Singular(feed.srcOwner);
            textHtml = textHtml.replace(_ENTITY_OWNER,user);
            if(textHtml.contains(_ENTITY_OWNER_PLURAL)){
                var user_Plural = getOwnerUser_Plural(feed.srcOwner);
                textHtml = textHtml.replace(_ENTITY_OWNER_PLURAL,user_Plural);
            }
            var entity = getSrcEntity(src,feed,true);
            textHtml = textHtml.replace(_ENTITY,entity);
            if(textHtml.contains(_ENTITY_NON_CLICK)){
                var entityNonClick = getSrcEntity(src,feed,false);
                textHtml = textHtml.replace(_ENTITY_NON_CLICK,entityNonClick);
            }
        }
        if(actor){
            var actorHTML = i18nJS("TXT_A_MEMBER");
            if(feeds && feeds.length>0){
                actorHTML = getActor(actor);
                for(var a=0;a<feeds.length;a++){
                    var actor_n = feeds[a].actor;
                    if(actor_n.id != actor.id){
                        actorHTML += ", "+getActor(actor_n);
                    }   
                }
                actorHTML = actorHTML.replace(/,([^,]*)$/,' & $1');
            }else{
                actorHTML = getActor(actor);
            }
            textHtml = textHtml.replace(_ACTOR,actorHTML);
        }
        if(textHtml.contains(_REASON)){
            var reasonText = "";
            var me = getMyOrgInfo();
            var me = {"id":USERID,"profile":me.userRole};
            var you = getActor(me);
            switch(reason){
                case "ATTEMPTED" : reasonText = i18nJS("ATTEMPTED_BY")+" "+you;
                    break;
                case "ROOT_OWNER" : 
                case "OWNER" : reasonText = i18nJS("ADDED_BY")+" "+you;
                    break;
                case "SHARED_WITH" : reasonText = i18nJS("SHARED_BY")+" "+you;
                    break;
                case "COMMENTED" : reasonText = i18nJS("COMMENTED_BY")+" "+you;
                    break;
                case "FOLLOWING_SOURCE" : reasonText = i18nJS("FOLLOWED_BY")+" "+you;
                    break;
                case "ADDED_SOLUTION" : reasonText = i18nJS("SOLUTION_ADDED_BY")+" "+you;
                    break;
            }
            textHtml = textHtml.replace(_REASON,reasonText);
        }
        if(textHtml.contains(_AORAN)){
            var A = "a";
            var text = $.trim(textHtml);
            var nextAlphabet = text.substr(text.indexOf(_AORAN) + _AORAN.length + 1,1);
            if(nextAlphabet){
                nextAlphabet = nextAlphabet.toUpperCase();
                if(VOWELS.indexOf(nextAlphabet)>=0){
                    A = "an";
                }
            }
            textHtml = textHtml.replace(_AORAN,A);
        }
        switch(feedLocation){
            case "NOTIFICATIONS" : html = notiInsert(textHtml,feed,actor);
                    break;
            case "ACTIVITIES" : html = activityInsert(textHtml,feed);
                    break;
        };
        return html;
    };
    this.isSupported = function(actionType){
        if(SUPPORTED_TYPES.indexOf(actionType)>=0){
            return true;
        }else{
            return false;
        }
    };
    var getSrcEntity = function(src,feed,clickable){
        switch(src.type){
            case "DISCUSSION" : return getDoubt(src,clickable);
            case "COMMENT" : return getComment(src,clickable);
            case "TEST" : return getTest(src,clickable);
            case "ASSIGNMENT" : return getAssignment(src,clickable);
            case "QUESTION" : return getQuestion(src,clickable); 
            case "CHALLENGE" : return getChallenge(src,clickable);
            case "STATUSFEED" : return getStatusFeed(src,feed.newsFeedId,clickable);
            case "REMARK" : return getRemark(src,feed.srcOwner,clickable);
            case "SOLUTION" : return getSolution(src,clickable); 
            case "VIDEO" : return getVideo(src,clickable); 
            case "DOCUMENT" : return getDoc(src,clickable); 
            case "FILE" : return getFile(src,clickable); 
                        case "MODULE" : return getModule(src,clickable); 
            //case "PLAYLIST" : return getPlaylist(src);
            default : return "Entity";
        }
    }
    var getActionTypeText = function(actionType,srcType,eType,feed){
        var langString_ROOT;
        var langString_SRC;
        switch(actionType){
            case "VOTE_ENTITY" : 
            case "VOTED" : 
                langString_ROOT = _ACTOR+" "+i18nJS("NEWS_FEED_UPVOTED")+" "+_ENTITY+" "+i18nJS("NEWS_FEED_ADDED_BY")+" "+_ENTITY_OWNER+", "+i18nJS("NEWS_FEED_ON")+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_UPVOTED")+" "+_ENTITY+" "+i18nJS("NEWS_FEED_ADDED_BY")+" "+_ENTITY_OWNER;
                break;
            case "FOLLOWED" : 
                langString_ROOT = _ACTOR+" "+i18nJS("NEWS_FEED_FOLLOWED")+" "+_ENTITY_OWNER+"'s "+_ENTITY+" "+i18nJS("NEWS_FEED_ON")+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_FOLLOWING")+" "+_ENTITY+" "+i18nJS("NEWS_FEED_ADDED_BY")+" "+_ENTITY_OWNER;
                break;
            case "ADDED" : 
                if(srcType == "CHALLENGE"){
                    langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_ADDED")+" "+_AORAN+" "+i18nJS("NEWS_FEED_NEW")+" "+_ENTITY_NON_CLICK;
                }else{
                   switch(eType){
                    case "ADD_SOLUTION" : 
                        langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_ADDED")+" "+_AORAN+" "+i18nJS("NEWS_FEED_NEW_SOLUTION_TO")+" "+_ENTITY+" "+_REASON;
                        break;
                    default : langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_ADDED_A_NEW")+" "+_ENTITY;
                                        break;
                   }
                }
                langString_ROOT = langString_SRC;
                break;
            case "MADE_VISIBLE" :
                if(feed.actor.id == feed.srcOwner.id){
                    langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_ADDED")+" "+_AORAN+" "+_ENTITY+" "+i18nJS("NEWS_FEED_ADDED_TO_YOUR_LIB")+".";
                }else{
                    langString_SRC = _ACTOR+" "+i18nJS("NEWS_FEED_ADDED_TO_YOUR_LIB")+" "+_AORAN+" "+_ENTITY+", "+i18nJS("NEWS_FEED_CREATED_BY")+" "+_ENTITY_OWNER;
                }
                langString_ROOT = langString_SRC;
                break;
            case "SHARED" :
                langString_SRC = langString_ROOT = _ACTOR+" "+i18nJS("NEWS_FEED_SHARED")+" "+_AORAN+" "+_ENTITY;
                break;
            case "ATTEMPTED" : 
                langString_SRC = langString_ROOT = _ACTOR+" "+i18nJS("NEWS_FEED_ATTEMPTED")+" "+_ENTITY+" "+i18nJS("NEWS_FEED_ADDED_BY")+" "+_ENTITY_OWNER;
                break;
            case "COMMENTED" :
                var clang =  i18nJS("NEWS_FEED_COMMENTED_ON");
                clang = srcSpecificLang(clang,srcType,actionType);
                clang = " "+clang+" ";
                langString_ROOT = _ACTOR + clang +_ENTITY+" "+i18nJS("ADDED_BY")+" "+_ENTITY_OWNER+" "+i18nJS("TXT_ON")+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR + clang + _ENTITY+" "+i18nJS("ADDED_BY")+" "+_ENTITY_OWNER;
                break;
            case "ASKED" : 
                langString_SRC = langString_ROOT = _ACTOR+" "+i18nJS("NEWS_FEED_ASKED")+" "+_AORAN+" "+_ENTITY;
                break;
            case "ENDED" : 
                langString_SRC = langString_ROOT = i18nJS("NEWS_FEED_RESULT_OF")+" "+_ENTITY+", "+_REASON+", "+i18nJS("NEWS_FEED_RESULTS_OUT_CHECK_OUT");
                break;
        };
        langString_SRC += ".";
        langString_ROOT += ".";
        var lang = {"ROOT":langString_ROOT,"SRC":langString_SRC};
        return lang;
    };
    var srcSpecificLang = function(lang,srcType,action){
        switch(action){
            case "COMMENTED" : 
                if(srcType == "DISCUSSION"){
                    lang = i18nJS("NEWS_FEED_ANSWERED");
                }else if(srcType == "QUESTION"){
                    lang = i18nJS("NEWS_FEED_PROVIDED_SOULTION");
                }
                break;
        }
        return lang;
    }
    var notiInsert = function(textHtml,feed,actor){
            var appendHTML = "<div class='notiFeed list-group-item media' data-feed-id="+feed.newsFeedId+">\n\
                <div class='pull-left'><img src='"+actor.thumbnail + "' class='avatar-img' alt='Image not available'>\n</div><div class='postTitle media-body'>"+textHtml+"</div>"+getAgo(feed.time)+"</div>";
            return appendHTML;
    };
    var activityInsert = function(textHtml,feed){
            var appendHTML = "<div class='notiFeed list-group-item media' data-feed-id="+feed.newsActivityId+">\n\
                    <div class='pull-left'><img src='"+feed.actor.thumbnail + "' class='lgi-img' alt='Image not available'>\n</div><div class='postTitle media-body'>"+textHtml+"</div>"+getAgo(feed.time)+"</div>";
            return appendHTML;
    }
    var getOrgUrl = function(){
        var orgId = $("#myInstitutePage").data('orgId');
        if(orgId){
            return "/organization/"+orgId+"/";
        }else{
            return "/";
        }
    };
    var getComment = function(src){
        var text = src.content;
        var preText = i18nJS("TXT_COMMENT");
        var root = src.rootDetails;
        if(root){
            changePreText(root.type);
        }else{
            changePreText(src.type);
        }
        function changePreText(type){
            switch(type){
                case "QUESTION" : preText = i18nJS("TXT_SOLUTION");
                    break;
                case "DISCUSSION" : preText = i18nJS("TXT_ANSWER");
                    break;
            };
        }
        var html = preText;
        /*var html = preText+"- \"<span class='greenTextColor singleLineText toolTipTitle displayInBlock' data-title='"+text+"' \n\
                style='max-width:200px;' >"+text+"</span>\"";*/
        return html;
    };
    var getDoubt = function(src,clickable){
        var openUrl = getOrgUrl() + "discussion/"+src.id;
        var name = src.name?src.name:src.title;
        var aTag = i18nJS("ENTITY_DOUBT")+" - \"<a class='openInstDoubtExt doCStream' data-diss-id='"+src.id+"'>"+name+"</a>\"";
        return aTag;
    };
    var getTest = function(test,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = i18nJS("ENTITY_TEST")+" - ";
        if(orgId){
            var className = "openInstTest";
            var userRole = getMyOrgInfo()["userRole"]; 
            if((test.attempted == false || test.attempted == "false") && userRole == "STUDENT"){
                className = "openInstPreTest";
            }
            data += "<a class='"+className+"' data-test-id='"+test.id+"'>"+test.name+"</a>";
        }else{
            data += "<a class='openTestPage' data-test-id='"+test.id+"'>"+test.name+"</a>";
        }
            return data;
    };
    var getAssignment = function(test,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var userRole = getMyOrgInfo()["userRole"]; 
        var data = i18nJS("ENTITY_ASSIGNMENT")+" - ";
        if(orgId){
            data += "<a class='openInstAssignment' data-test-id='"+test.id+"' data-user-role='"+userRole+"'>"+test.name+"</a>";
        }
            return data;
    };
    var getQuestion = function(ques,clickable){
        return getQuestionLink(ques,i18nJS("ENTITY_QUES"));
    }
    var getSolution = function(ques,clickable){
        ques.id = ques.qId;
        var link = getQuestionLink(ques,i18nJS("ENTITY_QUES"));
        link = "Solution of the "+link;
        return link;
    }
    var getQuestionLink = function(ques,title,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = "";
        if(orgId){
            data += "<a class='openQuesPage' data-qid='"+ques.id+"' data-org-id='"+orgId+"'>"+title+"</a>";
        }else{
            data += "<a class='openQuesPage' data-qid='"+ques.id+"'>"+title+"</a>";
        }
            return data;
    };
    var getChallenge = function(chal,clickable){
        var name = i18nJS("ENTITY_CHALLENGE")+" - \""+chal.name+"\"";
            if(clickable && chal.id && chal.name){
            chal.id = chal.qId;
            return getQuestionLink(chal,name);
            }else if(chal.name){
            return name;
        }
            else return i18nJS("ENTITY_CHALLENGE");
    };
    var getStatusFeed = function(feed,newsFeedId,clickable){
        var orgId = $("#myInstitutePage").data('orgId');
        var msg = i18nJS("NEWS_FEED_NEW_STATUS_FEED");
        if(feed.statusMessage){
            msg = i18nJS("ENTITY_FEED")+" - \""+feed.statusMessage+"\"";
        }else if(feed.sourceContent){
            if(feed.sourceContent.title){
                msg = i18nJS("ENTITY_FEED")+" - \""+feed.sourceContent.title+"\"";
            }else if(feed.sourceContent.type){
                switch(feed.sourceContent.type){
                    case "IMAGE" : msg = i18nJS("NEWS_FEED_IMAGE_IN_FEED");
                            break;
                    case "LINK_VIDEO" : msg = i18nJS("NEWS_FEED_VIDEO_IN_FEED");
                            break;
                    case "WEB_PAGE" : msg = i18nJS("NEWS_FEED_WEB_PAGE_IN_FEED");
                            break;
                }
            }
        }
        var tempDiv = getTempDOMElem();
        tempDiv.html(msg);
        msg = tempDiv.text();
        tempDiv.remove();
        var html = "<a class='openStatusFeed' data-feed-id='"+feed.id+"'>"+msg+"</a>";
            return html;
    };
    var getRemark = function(remark,owner,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var profileOwner = getOwnerUser_Plural(owner);
        var data = i18nJS("ENTITY_REMARK")+" - \"<b>'"+remark.content+"'</b>\" in "+profileOwner+" profile";
            return data;
    };
    var getVideo = function(video,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = i18nJS("ENTITY_VIDEO")+" - ";
        if(orgId){
            data += "<a class='openInstVideo' data-id='"+video.id+"'>"+video.name+"</a>";
        }else{
            data += "<a class='openVideoPage' data-id='"+video.id+"'>"+video.name+"</a>";
        }
            return data;
    };
    var getDoc = function(doc,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = i18nJS("ENTITY_DOCUMENT")+" - ";
        if(orgId){
            data += "<a class='openInstDoc' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }else{
            data += "<a class='openDocPage' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }
            return data;
    };
    var getFile = function(doc,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = i18nJS("ENTITY_FILE")+" - ";
        if(orgId){
            data += "<a class='openInstFile' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }else{
            data += "<a class='openFilePage' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }
            return data;
    };
        var getModule = function(doc,clickable){
            var orgId = $("#myInstitutePage").data('orgId');
        var data = i18nJS("ENTITY_MODULE")+" - ";
        if(orgId){
            data += "<a class='openInstModule' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }else{
            data += "<a class='openModulePage' data-id='"+doc.id+"'>"+doc.name+"</a>";
        }
            return data;
    };        
    var getUserLink = function(user,name){
        var a = "";
        if(user.isOrgUser){
            var extraClass = "";
            if(user.profile && user.profile.length>0 && user.profile!="STUDENT"){
                extraClass = "greenLink";
                if(user.profile == "TEACHER"){
                    name += "("+i18nJS("PROFILE_TEACHER")+")";
                }else{
                    name += "("+i18nJS("PROFILE_MANAGER")+")";
                }
            }
            a = "<a class='openInstProfile "+extraClass+"' \n\
                data-user-id='"+user.userId+"'>"+name+"</a>";
        }else{
            a = "<a class='openUserProfile' data-user-id='"+user.userId+"'>"+name+"</a>";
        }
        return a;
    };
    function getActor(user){
        user = getUserObjForUI(user); 
        var name = user.name;
        if(user.userId == USERID){
            name = i18nJS("TXT_YOU")+" ";
        }
        var a = getUserLink(user,name);
        return a;
    }
    function getOwnerUser_Singular(user){
        return getActor(user);
    }
    function getOwnerUser_Plural(user){
        var a = i18nJS("TXT_YOUR");
        if(user){
            user = getUserObjForUI(user);
            var name = a;
            if(user.userId != USERID){
                name = user.name+"'s";
            }
            a = getUserLink(user,name);
        }
        return a;
    }
    String.prototype.contains = function(it) { 
        return (this.indexOf(it) != -1) ? true : false; 
    };
    var getUserObjForUI = function(user){
    var usr = {name:"",userId:"",isOrgUser:false,orgId:"",profile:user.profile};
    usr.name = user.firstName + " " + user.lastName;
    usr.userId = user.id;
    var orgId = $("#myInstitutePage").data('orgId');
    if(user.userId && user.orgId){
        usr.userId = user.userId;
        if(user.orgId == orgId){
            usr.isOrgUser = true;
            usr.orgId = user.orgId;
        }
    }else{
        usr.isOrgUser = true;
        usr.orgId = orgId;
    }
    return usr;
}
    var getTempDOMElem = function(type) {
        type = type ? type : "div";
        var elem = document.createElement(type);
        elem = $(elem);
        elem.addClass("tempDomElem");
        $("body").append(elem);
        setTimeout(function() {
            $(".tempDomElem").remove();
        }, 1000);
        return elem;
    }
};
new function($){
    if(intervalObj){ clearInterval(intervalObj);}
    var intervalObj = setInterval(function(){
        try{
            if(myAssociateOrg){
                if(intervalObj){clearInterval(intervalObj);}
                defineFixProp(self,"myOrg",cloneObject(myAssociateOrg));
                defineFixProp(window,"getMyOrgInfo",function(){
                    return myOrg;
                });
                myAssociateOrg = undefined;
            }
        }catch(err){}
    },100);
    window["getMyOrgInfo"] = function(){
        return myAssociateOrg;
    };
}(jQuery);
