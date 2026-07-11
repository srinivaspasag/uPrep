var createMsg = new function(){
	var paramsStructure = {
		sender:{"id":"","type":"USER"},
		receivers:[],
		content:"",
		subject:"",
		action:"SEND"
	};
	var hidePopupTimeout;
	var divId = ".msgCreatePopup";
	this.init = function(){
		$(divId).off("click","#msgSendBtn",send)
			.on("click","#msgSendBtn",send);
	};
	this.show = function(){
		show();
	};
	this.showWithUser = function(){
		var popup = show();
		var fullName = $(this).data("userFullName");
		var userId = $(this).data("userId");
		instMembrSugg.putUserExt(popup,{"fullName":fullName,"id":userId});
	};
	var show = function(user){
		$(".msgRTEHolder").find(".RTEArea").html("");
		if(hidePopupTimeout) clearTimeout(hidePopupTimeout);
		var popup = showVPopup();
		var innerDiv = $($("#createMsgFooterBlock").find(".msgCreatePopup").get(0)).clone(true).removeClass("nonner");
		assignRTEs(innerDiv.find(".msgRTEHolder"));
		innerDiv.find(".msgRTEHolder .RTEHolder").attr("data-page","MESSAGE");
		popup.html(innerDiv.get(0));
		innerDiv.find(".instTypeMembers input").focus();
		instMembrSugg.init(popup);
		createMsg.init();
		return popup;
	};
	var showResp = function(msg){
		showVPopupMsg(msg);
	};
	var send = function(){
		var $this = $(this);
		if($this.hasClass("btnDisabled")){
			return;
		}
		var params = cloneObject(paramsStructure);
		var popup = $this.closest(divId);
		var senderId = popup.data("senderId");
		params["sender"] = {"id":senderId,type:"USER"};
		var receivers = instMembrSugg.getUserList(popup.find(".instTypeMembers"));
		if(receivers.length==0){
			showError(i18nJS("USER_MSGS_PROVIDE_MEMBER"));
			return;
		}
		for(var r = 0;r<receivers.length;r++){
			params["receivers"].push({"id":receivers[r],type:"USER"});
		};
    		var getRTE=vRTE.getRTEContent;
		params["subject"] = popup.find(".msgSubjectInput").val().trim();
		var RTEHolder = popup.find(".msgRTEHolder").children(".RTEHolder");
		params["content"] = getRTE(RTEHolder);
		if(params["subject"].length==0 || vRTE.isRTEEmpty(RTEHolder)){
			showError(i18nJS("USER_MSGS_PROVIDE_SUBJECT_BODY"));
			return;
		}
		params = {"message":params};
		$this.addClass("btnDisabled");
		//console.log($.param(params));
		vReq.post("/UserMessages/postMessage",params,function(data){
			$this.removeClass("btnDisabled");
			if(data && data.result && data.result.isReceived){
				showResp(i18nJS("USER_MSGS_SENT_SUCCESSFULLY"));
			}
		},function(){
			$this.removeClass("btnDisabled");
		});
	};
};
$(function(){
	$(".instCreateMsg").live("click",createMsg.show);
	$(".userSendMsg").live("click",createMsg.showWithUser);
});
