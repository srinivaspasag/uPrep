var comment = new function(){
    this.init = function(){
        parDivId = $(".instCommentWidgetHolder");
        $(".submitCommInput").off("click")
                             .on("click",submitCommInput);

        $(".submitReplyInput").off("click")
                              .on("click",submitReplyInput);
        parDivId.off("click")
                .on("click",".replyToCommItem",replyToCommItem)
                .on("click",".commWidgetHideReplies",commWidgetHideReplies)
                .on("click",".LMHandlerDivLoadMore",LMHandlerDivLoadMore);
        $(".note-editable").html("");
    }

    function initCommWidget(allParams,LMDataParams,targetDiv,callBack){
        var commWidget=commWidgetSample.children().clone(true);
        targetDiv.html(commWidget);
        assignRTEs(commWidget.find(".inputerRTEDiv"),allParams.placeHolder);
        delete allParams.placeHolder;
        commWidget.data("allParams",allParams);
        var LMHandlerDiv=commWidget.children(".LMHandlerDiv");
        LMHandlerDiv.data("urlStr","/widgets/commItems")
        .data("size",25).data("allParams",LMDataParams);
        showTopLoader();
        vReq.get("/widgets/commItems",LMDataParams,function(data,s,xhr){
            hideTopLoader();
            LMHandlerDiv.html(data);
            loadMJEqns(LMHandlerDiv.get(0));
        if(callBack){
            callBack(LMHandlerDiv,data,LMDataParams);
            LMHandlerDiv.data("cbFn",callBack);
        }
    });
}
    var uiSamplesDiv = $("#uiSamplesDiv");
    var makeHTMLTag = function(tag, attrs) {
        var el = document.createElement(tag);
        if (attrs) {
            for (var k in attrs)
                el.setAttribute(k, attrs[k]);
        }
        return $(el);
    };

    var createCommonUIEl = function(source) {
        var targetVar = makeHTMLTag('div');
        targetVar.html(source.html());
        source.remove();
        // if (img20Sample.children(".img20Wrapper").length > 0) {
        //     targetVar.find(".img20Holder").html(img20Sample.children().clone(true));
        // }
        // if (img30Sample.children(".img30Wrapper").length > 0) {
        //     targetVar.find(".img30Holder").html(img30Sample.children().clone(true));
        // }
        return targetVar;
    };
    rteSample=createCommonUIEl(uiSamplesDiv.children("#rteSample"));
    replyWidgetSample=createCommonUIEl(uiSamplesDiv.children("#replyWidgetSample"));

    //for rtes
    $(".inputerRTE .RTEArea").on('focus',function(){
       $(this).closest(".inputerRTE").addClass("activeInputerRTE");
    });
    $(".inputerArea").on('focus',function(){
       $(this).closest(".inputer").addClass("activeInputer");
    });
    $(".cancelInputerRTE").on('click',function(){
       var inputer=$(this).closest(".inputerRTE");
       var rteArea=inputer.find(".RTEArea");
       cleanRTE(rteArea);
       inputer.removeClass("activeInputerRTE");
    });
    $(".cancelInputer").on('click',function(){
       var inputer=$(this).closest(".inputer");
       inputer.find(".inputerArea").val("");
       inputer.removeClass("activeInputer");
    });
    instCommItemSample=createCommonUIEl(uiSamplesDiv.children("#instCommItemSample"));
    function submitCommInput(){
        var $this=$(this)
        var comment = $(".commInputer").find(".note-editable").html();
        var commWidget=$this.closest(".commWidget"),commentParams=commWidget.data("allParams");
        var allParams = {};
        showLoader();
        allParams["root.id"] = commentParams.root.id;
        allParams["root.type"] = commentParams.root.type;
        allParams["parent.id"] = commentParams.parent.id;
        allParams["parent.type"] = commentParams.parent.type;
        allParams["base.id"] = commentParams.base.id;
        allParams["base.type"] = commentParams.base.type;
        allParams.scope = commentParams.scope;
        allParams.callBack = commentParams.callBack;
        allParams.targetPage = commentParams.targetPage;
        commItem=instCommItemSample.children().clone(true);
        if(comment==""){
            swal({
                title:"Please enter a comment",
                type:"warning"
            });
            return ;
        }
        allParams.content = comment;
        allParams.orgId = $("#myInstitutePage").data("orgId");
        vReq.post("/Widgets/postComment",allParams,function(data){
            hideLoader();
            if(data.errorMessage!=""){
                swal({
                    title:"Answer could not be posted,please try again",
                    type:"warning",
                    timer:3000
                });
                return false;
            }
            else
            {
                swal({
                    title:"Answer posted successfully",
                    type:"success",
                    timer:"3000"
                });
                var LMHandlerDiv=commWidget.children(".LMHandlerDiv");
                LMHandlerDiv.prepend(commItem);
                commItem.find(".commItemText").html(allParams.content);
                var comment=LMHandlerDiv.children(".commItem").first();
                comment.data("commId",data.result.id);
                comment.find(".upVoteItem").data("entityId",data.result.id);
                comment.find(".instUpvoteHolder").data("entityId",data.result.id);
                if(postCommCallBackFn[allParams.callBack]){
                    postCommCallBackFn[allParams.callBack](commWidget);
                }
                loadMJEqns(LMHandlerDiv.get(0));
                $(".note-editable").html("");
            }
        });
    };

    var postCommCallBackFn={};
    postCommCallBackFn["QUES_SOLUTION"]=function(commWidget){
        var commNoEl=commWidget.closest(".quesSoln").find(".hideQSComments label");
        increaseCount(commNoEl);
    }
    postCommCallBackFn["QUES_DISCUSSION"]=function(commWidget){
        var commNoEl=commWidget.closest(".quesSolnsDiscsHolder").find(".quesDiscsTabNo");
        increaseCount(commNoEl);
    }
    postCommCallBackFn["INST_DISCUSSION"]=function(commWidget){
        var commNoEl=commWidget.closest(".instDoubtOpen").find(".ansInstFeedCount").find(".commCount");
        increaseCount(commNoEl);
        MathJax.Hub.Queue(["Typeset",MathJax.Hub,commWidget.get(0)]);
        var firstItem = commWidget.find(".commItem:first");
        firstItem.insertAfter(commWidget.find(".commItem:last"));
        firstItem.fadeTo(200,1);
    }
    postCommCallBackFn["VIDEO"]=function(commWidget){
        MathJax.Hub.Queue(["Typeset",MathJax.Hub,commWidget.get(0)]);
        commWidget.find(".commItem:first").fadeTo(200,1);
    }

    function replyToCommItem(){
        var $this = $(this);
        var commItem=$this.closest(".commItem");
        commItem.find(".replyToCommItem").removeClass("replyToCommItem").addClass("commWidgetHideReplies");
        if($this.hasClass("showCommentReplies")){
        $this.removeClass("showCommentReplies").addClass("hideCommentReplies");
        }
        showCommReplies(commItem);
    }

    function showCommReplies(commItem){
        var replyWidgetHolder=commItem.find(".replyWidgetHolder");
        replyWidgetHolder.removeClass("nonner");
        if(replyWidgetHolder.html()==""){
            replyWidgetHolder.html(replyWidgetSample.children().clone(true));
            // assignRTEs(replyWidgetHolder.find(".inputerRTEDiv"),"Add a Reply");
            replyWidgetHolder.addClass("p-t-20")
            var LMHandlerDiv=replyWidgetHolder.find(".LMHandlerDiv");
            var allParams={};
            allParams["parent.id"] = commItem.data("commId");
            allParams["parent.type"] = "COMMENT";
            allParams.orderBy = "timeCreated";
            LMHandlerDiv.data("urlStr","/Widgets/replyItems").data("size",25).data("allParams",allParams);
            allParams.start=0;allParams.size=10;
            // showLoader();
            vReq.get("/widgets/replyItems",allParams,function(data){
                // hideLoader();
                LMHandlerDiv.html(data);
                loadMJEqns(LMHandlerDiv.get(0));
            });
        }
    };
    var commWidgetHideReplies = function(){
        var $this = $(this);
        var commItem = $this.closest(".commItem");
        var replyWidgetHolder = commItem.find(".replyWidgetHolder");
        replyWidgetHolder.addClass("nonner");
        commItem.find(".commWidgetHideReplies").addClass("replyToCommItem").removeClass("commWidgetHideReplies");
        if($this.hasClass("hideCommentReplies")){
        $this.removeClass("hideCommentReplies").addClass("showCommentReplies");
        }
    }

    function LMHandlerDivLoadMore(){
    var $this=$(this);
    if(!$this.hasClass("loadingLM")){
        $this.addClass("loadingLM");
        var LMHandlerDiv=$(this).closest(".LMHandlerDiv");
        var LMData=LMHandlerDiv.data();
        var allParams={};
        if(LMData.allParams)allParams=LMData.allParams;
        allParams.size=LMData.size||10;
        allParams.start=$this.data("start")||LMHandlerDiv.children().not($this).length -1 ;
        $this.html("<span class='loadingSpan'></span>");
        vReq.get(LMData.urlStr,allParams,function(data,textStatus,xhr){
            $this.remove();
            LMHandlerDiv.append(data);
            var callback=LMHandlerDiv.data("callback");
        if(callback && LMHandlerDivCallbackFns[callback]){
        callback = LMHandlerDivCallbackFns[callback];
        }else{
        callback = LMHandlerDiv.data("cbFn");
        }
            if(callback){
                try{
            callback(LMHandlerDiv,data,allParams);
        }catch(err){}
            }
            loadMJEqns(LMHandlerDiv.get(0));
        });
    }
};
    var LMHandlerDivCallbackFns={};


    function submitReplyInput(){
        var $this=$(this),replyWidget=$this.closest(".replyWidget");
        var replyParams=replyWidget.closest(".commWidget").data("allParams");
        var reply=$(".replyInputer").find(".note-editable").html().trim();
        var allParams = {};
        showLoader();
        allParams.orgId = $("#myInstitutePage").data("orgId");
        allParams["root.id"] = replyParams.root.id;
        allParams["root.type"] = replyParams.root.type;
        allParams["base.id"] = replyParams.base.id;
        allParams["base.type"] = replyParams.base.type;
        allParams.content=reply;
        allParams.depth=1;allParams.scope="PUBLIC";allParams.type="REPLY";
        allParams["parent.type"] = "COMMENT";
        allParams["parent.id"] = replyWidget.closest(".commItem").data("commId");
        if(reply==""){
            swal({
                title:"Please enter a comment",
                type:"warning"
            });
            return ;
        }
        $this.closest(".inputerRTE").removeClass("activeInputerRTE");
        var replyItem=replyItemSample.children().clone(true);
        replyItem.find(".replyItemText").html(reply);
        vReq.post("/Widgets/postReply",allParams,function(data){
            hideLoader();
            if(data.errorMessage!=""){
                swal({
                    title:"Comment could not be posted,please try again",
                    type:"warning",
                    timer:3000
                });
                return false;
            }
            else{
                swal({
                    title:"Comment posted successfully",
                    type:"success",
                    timer:3000
                });
                var LMHandlerDiv=replyWidget.children(".LMHandlerDiv");
                LMHandlerDiv.prepend(replyItem);
                LMHandlerDiv.children(".userMessage").remove();
                increaseCount(replyWidget.closest(".commItem").find(".commRepliesNum label"));
                LMHandlerDiv.children(".replyItem").first().data("replyId",data.result.id); 
                loadMJEqns(LMHandlerDiv.get(0));
                $(".note-editable").html("");
            }
        });
    };
};
