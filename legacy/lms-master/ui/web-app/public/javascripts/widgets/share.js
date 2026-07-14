$(document).on('click',".showSharedInfo",function(){
   var $this=$(this),$data=$this.data();
   var shareInfoDiv=$(this).siblings(".sharedInfoDiv");
   addToggler(shareInfoDiv,$this);
   if(shareInfoDiv.html()==""){
       shareInfoDiv.html("Loading..");
       $.get("/widgets/getSharedWithInfo",{shareId:$data.shareId},function(data){
           var  people="",info=data.result;
           for(var k=0;k<info.length;k++){
               people+=getUserUrlAndClass(info[k]);
           }
           shareInfoDiv.html(people);
       });       
   }   
});
$(document).on('click',".shareEntity",function(){
   var $data=$(this).data();
   shareUi.open($data.entityId,$data.entityType);
});
var shareUi = new function(){
	var postParams;
	this.open = function(entityId,entityType,popup,allowPublic){
		postParams = {"with":[],"entity":{"type":entityType,"id":entityId}};
		var params = {'entityType':entityType.toUpperCase(),'entityId':entityId};
		if(!popup){
    			popup=getCommonPopupBody(450,80);
		}else{
			params["embed"] = "true";	
		}
		smallLoader(popup);
		if(allowPublic){
			params["allowPublic"] = postParams['allowPublic'] = "true";
		}
		$.get("/Widgets/shareWith",params,function(data){
    			popup.html(data);
			registerFns(popup);
			vSelectFns.init();
			var shareWithDiv = $(popup).find(".shareWithDiv");
			var target = shareWithDiv.find(".shareSelect");
			var shareType = shareWithDiv.data("shareType");
			shareUi.getShareTypeUi(shareType,target);
		});
	};
	var hideSections = function(e){
		var $this = $(e.srcElement);
		var has = $(".instShareSelSection").has($this);
		if(!has.get(0)){
			$(".instShareSelSection").addClass("nonner");
			$(".shareWithDiv").off("click",hideSections);
		}
	};
	var centerClicked = function(e){
		var center = $(this).closest(".instShareEachCenter");
		center.find(".instShareCenterCHK").attr("checked","checked");
		center.find(".instShareSelSection").removeClass("nonner");
		$(".shareWithDiv").on("click",hideSections);
	};
	var sectionSelected = function(e){
		var holder = $(this).closest(".instShareSelSection");
		var list = [];
		holder.find(".instShrSec").each(function(){
			var chkBox = $(this).find("input:checkbox");
			var checked = chkBox.is(":checked");
			if(checked){
				list.push($(this).data("value"));
			}
		});
		holder.data("list",list);
	};
	var sectionAddedDone = function(e){
		var list = $(this).closest(".instShareSelSection").addClass("nonner").data("list");
		$(".shareWithDiv").off("click",hideSections);
		var holder = $(this).closest(".instShareEachCenter");
		if(list){
			holder.find(".instShareCenterSummary").removeClass("nonner").find(".blueTextColor")
				.text(list.toString());
		}else{
			holder.find(".instShareCenterSummary").addClass("nonner").find(".blueTextColor")
				.text("");
		}
	};
	var centerChecked = function(){
		if($(this).is(":checked")){
			$(this).closest(".instShareEachCenter").find(".instShareSelSection").removeClass("nonner");
		}
	};
	var cancelCenter = function(){
		$(".instShareCentersHolder").html("");
	};
	var doneCenter = function(){
		$(".instShareCentersHolder").addClass("nonner");	
		$(".instShareBatches").addClass("nonner");	
		$(".instSharedDetails").removeClass("nonner");
		var inrHtml = "";var data = {"id":'','type':"PROGRAMME","centers":[]};
		var batchHolder = $(".instSelectShareBatch");
		inrHtml += "<span class='big14 boldy'>"+batchHolder.attr("title")+" : </span>";
		data["id"] = batchHolder.data("value");
		$(".instShareEachCenter").each(function(){
			var checked = $(this).find(".instShareCenterCHK").is(":checked");
			if(checked){
				var name = $(this).find(".instShrCenterName").text();
				inrHtml +="<span class='boldy'>"+name+"</span>";
				var sections = $(this).find(".instShareSelSection").data("list");
				var centers = {'name':name,'sections':sections};
				if(sections){
					centers.sections = sections;
					inrHtml +=" / "+sections.toString();	
				}
				inrHtml += " ; ";	
				data["centers"].push(centers);
			}
		});
		cancelCenter();
		batchHolder.resetVSelectTag({'newVal':{'text':'none'}});
		var txtHolder = $($(".instEachSharedCenter").get(0)).clone();
		//postParams['with'].push(data);
		txtHolder.removeClass("nonner").data("obj",data).find(".txt").html(inrHtml);
		$(".instEachSharedHolder").append(txtHolder);
		enableShareBtn();
	};
	var enableShareBtn = function(){
		$(".shareWithDiv").find(".submitShare").removeClass("submitShareDisabled").addClass("cancelCommonPopup");
	};
	var disableShareBtn = function(){
		$(".shareWithDiv").find(".submitShare").addClass("submitShareDisabled").removeClass("cancelCommonPopup");
	};
	var crossSelected = function(){
		var center = $(this).closest(".instEachSharedCenter");
		center.remove();
	};
	var addMoreToShare = function(){
		$(".instShareCentersHolder").removeClass("nonner");	
		$(".instShareBatches").removeClass("nonner");	
	};
	var shareClicked = function(){
		if($(this).hasClass('submitShareDisabled')) return;
   		var shareDiv=$(this).closest(".shareWithDiv");
		var type = shareDiv.data("shareType");
		switch(type){
			case "INSTITUTE":shareToInst();
					break;	
			case "INDIVIDUALS":shareToIndv(shareDiv);
					break;	
			case "PUBLIC":shareToPublic(shareDiv);
					break;	
		}
		text = {"INSTITUTE":"with selected batch of the Institute","INDIVIDUALS":"with these selected users","PUBLIC":"Publicaly"};
		console.log(postParams);
		$.post("/Widgets/shareEntity",postParams,function(data){
			console.log(data);	
       			if(data.errorCode=="ALREADY_SHARED"){
           			showTopMessage("You have already shared "+text[type]);
			}
			else if(data.errorCode.length>0){
          			showTopMessage("Sharing Failed");
			}
			else if(data.result.alreadySharedWith && data.result.alreadySharedWith.length>0){
				var shared=data.result.alreadySharedWith;
           			var people="";
           			for(var k=0;k<shared.length;k++){
               				people+=shared[k].firstName+" "+shared[k].lastName+", ";
           			}
           			showTopMessage("You have already shared with "+people);
			}else{
				showTopMessage("Shared");
			}
		});
	};
	var shareToPublic = function(shareDiv){
		postParams['with'] = [];
    		postParams.scope="PUBLIC";
	};
	var shareToIndv = function(shareDiv){
		postParams['with'] = [];
    		postParams.scope="PUBLIC";
        	var shareWith=[];
        	var ESHolder=shareDiv.find(".entrySuggHolder");
        	ESHolder.find(".ESSelected").each(function(){
            		shareWith.push({type:"USER",id:$(this).data("params").userId});
            		postParams.scope="PRIVATE";
        	});
      		postParams["with"]=shareWith;        
    		var area=shareDiv.find(".shareCommentText");
    		if(area.length>0&&area.val()!=""){
       			postParams.content=area.val();
    		}
		return postParams;
	};
	this.getIndvProp = shareToIndv;
	var shareToInst = function(){
		var orgId = $(".shareWithDiv").find(".shareSelect").data("id");
		postParams['with'] = [{'id':orgId,"type":"ORGANIZATION"}];
		var summaryDivs = $(".instEachSharedHolder").find(".instEachSharedCenter");
		if(summaryDivs.length>0){
			postParams['with'] = [];
		}
		summaryDivs.each(function(){
			var wData = $(this).data("obj");
			if(wData){ postParams['with'].push(wData);}
		});
		return postParams;
	};
	var registerFns = function(popup){
		$(popup).find(".shareWithDiv").on("click",".instSelectShareCenter",centerClicked)
				.on("click",".instShrSec input",sectionSelected)
				.on("click",".instShrSecDone",sectionAddedDone)
				.on("click",".instShareCenterCNCL",cancelCenter)
				.on("click",".instShareCenterBtn",doneCenter)
				.on("click",".instShareCenterCHK",centerChecked)
				.on("click",".instShareAddMore",addMoreToShare)
				.on("click",".instEachSharedCenter .crossIt",crossSelected)
				.on("click",".submitShare",shareClicked)
				disableShareBtn();
	};
	this.getShareTypeUi = function(type,target){
		disableShareBtn();
		switch(type){
   			case "INDIVIDUALS":getIndvUi(target);
					break;
			case "INSTITUTE": getInstUi(target);
					break;
			case "PUBLIC": getPublicUi(target);
					break;
    		}
	};
	var getIndvUi = function(target){
    		var holder=$(target).closest(".shareWithDiv").find(".shareTypeHolder");
		var entityType = $('.shareWithDiv').data('entityType');
		var params = {'entityType':entityType};
		smallLoader(holder);
		$.get("/Widgets/getShareIndvUi",params,function(data){
			holder.html(data);
        		holder.find(".entrySuggInput").focus();
			enableShareBtn();
		});
	};
	var getPublicUi = function(target){
    		var holder=$(target).closest(".shareWithDiv").find(".shareTypeHolder");
		var entityType = $('.shareWithDiv').data('entityType');
		var params = {'entityType':entityType};
		smallLoader(holder);
		$.get("/Widgets/getSharePublicUi",params,function(data){
			holder.html(data);
        		holder.find(".entrySuggInput").focus();
			enableShareBtn();
		});
	};
	var getInstUi = function(target){
		var instId = $(target).data("id");
		$(target).closest(".shareSelect").data("id",instId);	
		var entityType = $('.shareWithDiv').data('entityType');
		var params = {'entityType':entityType,'orgId':instId};
    		var holder = $(target).closest(".shareWithDiv").find(".shareTypeHolder");
		smallLoader(holder);
		$.get("/Institute/getSharableBatches",params,function(data){
			holder.html(data);
			vSelectFns.init();
			postParams['orgId'] = instId;
		});
	};
	this.getBatchDetails = function(target,programmeId){
		var batchIndex = $(target).data("index");
		var params = {'entityType':$('.shareWithDiv').data('entityType'),'batchIndex':batchIndex,'programmeId':programmeId,'orgId':$(".shareWithDiv").find(".shareSelect").data("id")};
		var holder = $(".instShareCentersHolder");
		smallLoader(holder);
		$.get("/Institute/getSharableCenters",params,function(data){
			holder.html(data);
		});
	};
};
function shareSelectChange(text,target,value){
    	shareUi.getShareTypeUi(value,target);
    	$(target).closest(".shareWithDiv").data("shareType",value);
}
function instShareBatchChanged(value,target,targetValue){
	shareUi.getBatchDetails(target,targetValue);
	$(".instSelectShareBatch").attr("title",value);
}
