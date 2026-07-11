var upvoteComm = new function(){
    var parDivId = "#instituteHome";
    var defaultSize = 10;
    this.init = function(){
        var parDiv = $(parDivId);
        if(parDiv.data("upVoteCommInited")){
            return;
        }
        parDiv.off("click",".upvoteInstItem",upvote)
            .on("click",".upvoteInstItem",upvote)
            .off("click",".commentsInstFeed,.commentsInstFeedCount",showComment)
            .on("click",".commentsInstFeed,.commentsInstFeedCount",showComment)
            .off("click",".instLoadMoreFeedComments",showMoreComments)
            .on("click",".instLoadMoreFeedComments",showMoreComments)
        // /*Tooltip*/
        //     .off("mouseenter mouseleave",".upvotesInstFeedCount",upvoteToolTip)
        //     .on("mouseenter mouseleave",".upvotesInstFeedCount",upvoteToolTip)
        //     .off("mouseenter mouseleave",".followersInstFeedCount",followerToolTip)
        //     .on("mouseenter mouseleave",".followersInstFeedCount",followerToolTip)
        // /*Popup*/
        //     .off("click",".upvotesInstFeedCount",upvotersPopup)
        //     .off("click",".followersInstFeedCount",followersPopup)
        //     .on("click",".upvotesInstFeedCount",upvotersPopup)
        //     .on("click",".followersInstFeedCount",followersPopup)
        /*Comments*/
            .off("keypress",".instFeedCommentInput",postComment)
            .on("keypress",".instFeedCommentInput",postComment);
            
        // $(document)
        //     .off("click",".loadMorePopupUsers",morePopupUsers)
        //     .on("click",".loadMorePopupUsers",morePopupUsers);
        parDiv.data("upVoteCommInited",true);
    };
    // var upvotersPopup = function(e){
    //     popupUsers(e,$(this),"/widgets/popupEntityUpVoters","Upvoted by Users");
    // };
    // var followersPopup = function(e){
    //     popupUsers(e,$(this),"/widgets/popupEntityFollowers","Followers");
    // };
    // var popupUsers = function(e,$this,url,popupTitle){
    //     var entityCount = parseInt($this.data("entityCount"),10);
    //     if(entityCount<=0){ return;}
    //     var ent = $this.closest(".instUpvoteHolder");
    //     var entityType = $this.data("entityType");
    //     entityType = entityType?entityType:ent.data("entityType");
    //     var entityId = $this.data("entityId");
    //     entityId = entityId?entityId:ent.data("entityId");  
    //     if(!entityType || !entityId){ return;}
    //     // clearToolTipXHR();
    //     var pr = {"entity":{"id":entityId,"type":entityType},"start":0,"size":defaultSize};
    //     var popup = showVPopup();
    //     popup.html($("#instUsersPopupCont").html());
    //     popup.find(".usersPopupTitle").text(popupTitle);
    //     vReq.get(url,pr,function(html){
    //         popup.find(".popupUsersList").html(html);
    //         popup.find(".instUsersPopup").data("fetchUrl",url).data("fetchParams",pr);
    //         showImgs(popup,".instUserPic");
    //     });
    // };
    // var showImgs = function(parDivId,picId){
    //     var pics = $(parDivId).find(picId)
    //         .load(function(){
    //             $(this).fadeTo(120,1);
    //             $(this).closest(".instProfilePicContainer").addClass("imgLoaded");
    //         })
    //         .error(function(){
    //             this.error = true;
    //         });
    //     setTimeout(function(){
    //         pics.each(function(){
    //             if(this.complete && !this.error){
    //                 $(this).load();
    //             }
    //         });
    //     },1000);
    // };
    // var morePopupUsers = function(){
    //     var $this = $(this);
    //     var popup = $this.closest(".instUsersPopup");
    //     var url = popup.data("fetchUrl");
    //     var pr = popup.data("fetchParams");
    //     pr["start"] = pr["start"]+pr["size"];
    //     $this.text("loading...").addClass("greyTextColor");
    //     vReq.get(url,pr,function(data){
    //         $this.remove();
    //         popup.find(".popupUsersList").append(data);
    //         popup.find(".instUsersPopup").data("fetchParams",pr);
    //         showImgs(popup,".instUserPic");
    //     });
    // };
    // var upvoteToolTip = function(e){
    //     toolTipUsers(e,$(this),"/widgets/tooltipEntityUpVoters",3);
    // };
    // var followerToolTip = function(e){
    //     toolTipUsers(e,$(this),"/widgets/tooltipEntityFollowers",3);
    // };
    // var toolTipUsers = function(e,$this,url,offsetPlus){
    //     var entityCount = parseInt($this.data("entityCount"),10);
    //     if(entityCount<=0){ return;}
    //     var ent = $this.closest(".instUpvoteHolder");
    //     var entityType = $this.data("entityType");
    //     entityType = entityType?entityType:ent.data("entityType");
    //     var entityId = $this.data("entityId");
    //     entityId = entityId?entityId:ent.data("entityId");  
    //     if(!entityType || !entityId){ return;}
    //     toolTipFn(e,function(){
    //         clearToolTipXHR();
    //         var pr = {"entity":{"id":entityId,"type":entityType},"start":0,"size":8};
    //         var xhr = $.get(url,pr,function(html){
    //             if(checkToolTipXHR()){
    //                 vtooltip.show(e,html,null,offsetPlus);
    //             }
    //         });
    //         setToolTipXHR(xhr);
    //         return i18nJS("LOADING");
    //     },offsetPlus);
    // };
    var upvote = function(){
        var $this = $(this);
        var ent = $this.closest(".instUpvoteHolder");
        $this.addClass("nonner");
        ent.find(".upvotedInstItem").removeClass("nonner");
        ent.find(".upvotesInstFeedCount .instImgs").addClass("voted");
        var cntDiv = ent.find(".upvotesCount");
        var newCount = parseInt(cntDiv.text(),10)+1;
        cntDiv.text(newCount).closest(".upvotesInstFeedCount").data("entityCount",newCount);
        var params={};
        params["entity.type"] = ent.data("entityType");
        params["entity.id"] = ent.data("entityId");
        vReq.post("/widgets/upVoteItem",params,function(data){
            $this.remove();
        },function(error){
            ent.find(".upvotedInstItem").addClass("nonner");
            ent.find(".upvotesInstFeedCount .instImgs").removeClass("voted");
            $this.removeClass("nonner");
            var newCount = parseInt(cntDiv.text(),10)-1;
            cntDiv.text(newCount).data("entityCount",newCount);
        }); 
    };
    var showComment = function(e){
        var $this = $(this);
        var curTarget = $(e.currentTarget); 
        var feed = $this.closest(".eachInstFeedHolder");
        var holder = feed.find(".instFeedCommContainer");
        if(holder.data("opened")){
            holder.find(".instFeedsComments").fadeTo(0,0);
            holder.data("opened",false).addClass("nonner");
            return;
        }
        holder.removeClass("nonner");
        var ent = $this.closest(".instUpvoteHolder");
        var params = {
            "rootType":ent.data("entityType"),
            "rootId":ent.data("entityId"),
            "start":0,
            "size":defaultSize,
            "urlStr":"/Institute/activityComments",
            "orderBy":"timeCreated",
            "sortOrder":"ASC"
        };
        params["parent.type"] = ent.data("entityType");
        params["parent.id"]  = ent.data("entityId");
        vReq.get("/Institute/activityComments",params,function(data){
            holder.html(data).data("opened",true);
            institute.animate(holder.find(".instFeedsComments"),true);
            if(curTarget.hasClass("commentsInstFeed")){
                holder.find(".instFeedCommentInput").focus();
            }
            institute.activities.showComImgs(holder);
        });
    };
    var showMoreComments = function(){
        var $this = $(this);
        var nextStart = parseInt($this.data("nextStart"),10);
        console.log("load more comments start === "+nextStart);
        var params = {
            "rootType":$this.data("entityType"),
            "rootId":$this.data("entityId"),
            "start":nextStart,
            "size":defaultSize,
            "urlStr":"/Institute/moreActivityComments",
            "orderBy":"timeCreated",
            "sortOrder":"ASC"
        };
        params["parent.type"] = $this.data("entityType");
        params["parent.id"] = $this.data("entityId");
        var holder = $this.closest(".instFeedComContainer");
        smallLoader($this);
        $.get("/Institute/moreActivityComments",params,function(data){
            $this.remove();
            holder.append(data);
            institute.animate(holder.find(".instFeedsComments"),true);
            institute.activities.showComImgs(holder);
        });
    };
    var postComment = function(e){
        var $this = $(this);
        if(e.which!=13) return;
        var text = $.trim($this.val());
        if(text.length==0){
            e.preventDefault();
            return false;
        }
        $this.blur();
        var ent = $this.closest(".instFeedsComments");
        console.log($(".instFeedsComments").data("rootType"));
        var holder = ent.find(".instFeedComContainer");
        var params = {
            "content":text
        };
        params["parent.type"]=ent.data("rootType");
        params["parent.id"] = ent.data("rootId");
        params["root.type"] = ent.data("rootType");
        params["root.id"] = ent.data("rootId");
        vReq.post("/Widgets/postComment",params,function(data){
            if(data && data.errorCode=="" && data.result){
                var countDiv = ent.closest(".eachInstFeedHolder").find(".commentsInstFeedCount")
                    .find(".commentsCount");
                countDiv.text(parseInt(countDiv.text(),10)+1);
                $this.val("");
                var p = {
                    content:text,
                    id:data.result.id,
                    timeCreated:new Date().getTime()
                };
                vReq.get("/Institute/singleComment",p,function(data){
                    $(holder).append(data); 
                    institute.activities.showComImgs(holder);
                });
            }
            $this.trigger("delete");
        });
        return;
    };
};
$(function(){
    upvoteComm.init();
});
