function initCommWidget(allParams,LMDataParams,targetDiv,callBack){
    var commWidget=commWidgetSample.children().clone(true);
    targetDiv.html(commWidget);
    assignRTEs(commWidget.find(".inputerRTEDiv"),allParams.placeHolder);
    commWidget.find(".inputerRTEDiv .RTEHolder").attr("data-page","COMMENT");
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

//for rtes
$(".inputerRTE .RTEArea").live('focus',function(){
   $(this).closest(".inputerRTE").addClass("activeInputerRTE");
});
$(".inputerArea").live('focus',function(){
   $(this).closest(".inputer").addClass("activeInputer");
});
$(".cancelInputerRTE").live('click',function(){
   var inputer=$(this).closest(".inputerRTE");
   var rteArea=inputer.find(".RTEArea");
   cleanRTE(rteArea);
   inputer.removeClass("activeInputerRTE");
});
$(".cancelInputer").live('click',function(){
   var inputer=$(this).closest(".inputer");
   inputer.find(".inputerArea").val("");
   inputer.removeClass("activeInputer");
});
$(".submitCommInput").live('click',function(){
    var $this=$(this),comment=getInputerVal($this);
    var commWidget=$this.closest(".commWidget"),allParams=commWidget.data("allParams");
    allParams.content=comment;
    if(comment!=""){       
        $this.closest(".inputerRTE").removeClass("activeInputerRTE");
        var commItem;
        if(allParams["targetPage"]=="MY_INSTITUTE"){
		commItem=instCommItemSample.children().clone(true);
	}else{
		commItem=commItemSample.children().clone(true);
	}
	var parent = allParams.parent||{};
        if(parent.type=="SOLUTION"){
            commItem.find(".commReviewVoteDiv,.commItemRepliesSec").remove();
        }
        commItem.find(".commItemText").html(comment);
        var LMHandlerDiv=commWidget.children(".LMHandlerDiv");
        LMHandlerDiv.prepend(commItem);
    	commItem.fadeTo(200,1);
        LMHandlerDiv.children(".userMessage").remove();
        vReq.post("/Widgets/postComment",allParams,function(data){
            var comment=LMHandlerDiv.children(".commItem").first();
            comment.data("commId",data.result.id);
            comment.find(".upVoteItem").data("entityId",data.result.id);
            comment.find(".instUpvoteHolder").data("entityId",data.result.id);
            if(postCommCallBackFn[allParams.callBack]){
                postCommCallBackFn[allParams.callBack](commWidget);
            }            
            loadMJEqns(LMHandlerDiv.get(0));                       
        });
    }else{
	showError("There is nothing in the comment input box to submit!");	
    }    
});
function getInputerVal($this){
    var content="",inputer=$this.closest(".inputer");
    if(inputer.hasClass("inputerRTE")){
        var rteHolder=$this.closest(".inputerRTE").find(".RTEHolder");
	if(!vRTE.isRTEEmpty(rteHolder)){
        	content=vRTE.getRTEContent(rteHolder); 
        	cleanRTE(rteHolder.find(".RTEArea"));        
	}
    }else{
        content=inputer.find(".inputerArea").val().trim();
    }
    return content;
}
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
$(".replyToCommItem,.commRepliesNum,.showCommentReplies").live('click',function(){
    var $this = $(this);
    var commItem=$this.closest(".commItem");
    commItem.find(".replyToCommItem").text("Hide Replies").removeClass("replyToCommItem").addClass("commWidgetHideReplies");
    if($this.hasClass("showCommentReplies")){
	$this.removeClass("showCommentReplies").addClass("hideCommentReplies");
    }
    showCommReplies(commItem);
});
function showCommReplies(commItem){
    var replyWidgetHolder=commItem.find(".replyWidgetHolder");
    replyWidgetHolder.removeClass("nonner");
    if(replyWidgetHolder.html()==""){
        replyWidgetHolder.html(replyWidgetSample.children().clone(true));
        assignRTEs(replyWidgetHolder.find(".inputerRTEDiv"),"Add a Reply");
        replyWidgetHolder.find(".inputerRTEDiv .RTEHolder").attr("data-page","COMMENT");
        var LMHandlerDiv=replyWidgetHolder.find(".LMHandlerDiv");
        var allParams={};
        allParams.parent = {"id":commItem.data("commId"),"type":"COMMENT"};
	allParams.orderBy = "timeCreated";
        LMHandlerDiv.data("urlStr","/Widgets/replyItems").data("size",25).data("allParams",allParams);
        allParams.start=0;allParams.size=10;
        showTopLoader();
        vReq.get("/widgets/replyItems",allParams,function(data){
            hideTopLoader();
            LMHandlerDiv.html(data);
            loadMJEqns(LMHandlerDiv.get(0));                       
        });
    }
};
$(".commWidgetHideReplies,.hideCommentReplies").live('click',function(){
    var $this = $(this);
    var commItem = $this.closest(".commItem");
    var replyWidgetHolder = commItem.find(".replyWidgetHolder");
    replyWidgetHolder.addClass("nonner");
    commItem.find(".commWidgetHideReplies").text("Reply").addClass("replyToCommItem").removeClass("commWidgetHideReplies");
    if($this.hasClass("hideCommentReplies")){
	$this.removeClass("hideCommentReplies").addClass("showCommentReplies");
    }
});


$(".submitReplyInput").live('click',function(){
    var $this=$(this),replyWidget=$this.closest(".replyWidget"),
    allParams=replyWidget.closest(".commWidget").data("allParams");
    var reply=getInputerVal($this);
    allParams.content=reply;
    allParams.depth=1;allParams.scope="PUBLIC";allParams.type="REPLY";
    allParams.parent = {"type":"COMMENT","id":replyWidget.closest(".commItem").data("commId")};
    if(reply!=""){
        $this.closest(".inputerRTE").removeClass("activeInputerRTE");
        var replyItem=replyItemSample.children().clone(true);
        replyItem.find(".replyItemText").html(reply);
        var LMHandlerDiv=replyWidget.children(".LMHandlerDiv");
        LMHandlerDiv.prepend(replyItem);
        LMHandlerDiv.children(".userMessage").remove();
        increaseCount(replyWidget.closest(".commItem").find(".commRepliesNum label"));
        vReq.post("/Widgets/postReply",allParams,function(data){
            LMHandlerDiv.children(".replyItem").first().data("replyId",data.result.id); 
            loadMJEqns(LMHandlerDiv.get(0));                       
        });
    }else{
	showError("There is nothing in the reply input box to submit!");	
    }
});
