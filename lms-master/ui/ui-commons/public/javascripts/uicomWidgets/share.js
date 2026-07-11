$(document).on('click',".shareEntity",function(){
   var $data=$(this).data();
   shareUi.open($data.entityId,$data.entityType,undefined,$data.allowPublic,true,null,true,true);
});
var shareUi = new function(){
	var postParams;
	var popupH;
	var callBackFn;
	this.open = function(entityId,entityType,popup,allowPublic,showShareBtn,
        cbFn,allowIndv,allowInst,afterShareUIOpenFn){		
                var params={};
                if(entityType&&entityId){
                    entityType = entityType.toUpperCase();
                    postParams = {"with":[],"entity":{"type":entityType,"id":entityId}};
                    params={'entityType':entityType.toUpperCase(),'entityId':entityId};
                }else {
                    postParams={};
                }
                
		if(!popup){
    			popup=getCommonPopupBody(450,80);
		}
		params["embed"] = "false";
		if(!showShareBtn){
			params["embed"] = "true";	
		}
		popupH = popup;
		smallLoader(popup);
		params["allowIndv"] = postParams['allowIndv'] = "true";
		params["allowInst"] = postParams['allowInst'] = "true";
		if(allowPublic){
			params["allowPublic"] = postParams['allowPublic'] = "true";
		}
		if(allowIndv == false){
			params["allowIndv"] = postParams['allowIndv'] = "false";
		}
		if(allowInst == false){
			params["allowInst"] = postParams['allowInst'] = "false";
		}
		vReq.get("/UIComShare/shareWith",params,function(data){
    			popup.html(data);
			registerFns(popup);
			var shareWithDiv = $(popup).find(".shareWithDiv");
			var target = shareWithDiv.find(".shareSelect");
			var shareType = shareWithDiv.data("shareType");
                        if(afterShareUIOpenFn){
                            afterShareUIOpenFn();
                        }
			shareUi.getShareTypeUi(target,shareType);
		});
		callBackFn = undefined;
		if(cbFn){
			callBackFn = cbFn;
		}
	};
	this.openInst = function(orgId,orgName,entityType,popup,showShareBtn,cbFn){
		entityType = entityType.toUpperCase();
		postParams = {"with":[],"entity":{"type":entityType}};
		var params = {'entityType':entityType.toUpperCase()};
		if(!popup){
    			popup=getCommonPopupBody(450,80);
		}
		params["embed"] = "false";
		if(!showShareBtn){
			params["embed"] = "true";	
		}
		popupH = popup;
		smallLoader(popup);
		params["allowIndv"] = postParams['allowIndv'] = "false";
		params["allowInst"] = postParams['allowInst'] = "true";
		params["allowPublic"] = postParams['allowPublic'] = "false";
		params["orgId"] = postParams['orgId'] = orgId;
		params["orgName"] = postParams['orgName'] = orgName;
		vReq.get("/UIComShare/shareWithInst",params,function(data){
    			popup.html(data);
			registerFns(popup);
			var shareWithDiv = $(popup).find(".shareWithDiv").data("noSection",true);
			shareWithDiv.data("allowAllShare",true);
			var vChoose = $(popup).find(".shareSelect");
			vChoose.data("optParams",{"instId":orgId});
			shareUi.getShareTypeUi(vChoose,"INSTITUTE");
		});
		callBackFn = undefined;
		if(cbFn){
			callBackFn = cbFn;
		}
	};
	var retryShare = function(){
   		var $data=$(this).data();
   		shareUi.open($data.entityId,$data.entityType,popupH,$data.allowPublic,true,null,$data.allowIndv,$data.allowInst);
	};
	var openSectionPopup = function(center){
		if($(center).closest(".shareWithDiv").data("noSection")) return true;
		center.find(".instShareSelSection").removeClass("nonner")
			.find(".instShareSections").useScrollBar();
		$(".shareTypeHolder").on("click",hideSections);
	};
	var hideSections = function(e){
		var $this = $(e.srcElement||e.target);
		var has = $(".instShareSelSection").has($this);
		if(!has.get(0) && !$this.hasClass("instShareSelSection")){
			$(".instShareSelSection").addClass("nonner");
			$(".shareTypeHolder").off("click",hideSections);
			e.preventDefault();
			return false;
		}
	};
	var centerClicked = function(e){
		var center = $(this).closest(".instShareEachCenter");
		center.find(".instShareCenterCHK").prop("checked",true);
		openSectionPopup(center);
	};
	var getSectionSelected = function(holder){
		var list = [],sectionSrcEntities=[];
		holder.find(".instShrSec").each(function(){
			var chkBox = $(this).find("input:checkbox");
			var checked = chkBox.is(":checked");
			if(checked){
				list.push($(this).data("name"));
                                sectionSrcEntities.push({type:"SECTION",id:$(this).data("value")})
			}
		});
		holder.data("sectionSrcEntities",sectionSrcEntities).data("names",list);
		return list;
	};
	var sectionAddedDone = function(e){
                    var shareSection = $(this).closest(".instShareSelSection");
		var list = getSectionSelected(shareSection);
		shareSection.addClass("nonner");
		$(".shareTypeHolder").off("click",hideSections);
		var holder = $(this).closest(".instShareEachCenter");
		if(list && list.length>0){
			holder.find(".instShareCenterSummary").removeClass("nonner").find(".blueTextColor")
				.text(list.toString());
		}else{
			holder.find(".instShareCenterSummary").addClass("nonner").find(".blueTextColor")
				.text("");
		}
	};
	var allCentersChecked = function(){
		var holder = $(this).closest(".instShareCentersHolder");
		if($(this).is(":checked")){
			holder.find(".instShareEachCenter").each(function(){
				$(this).find(".instShareCenterCHK").prop("checked",true);	
			});
		}else{
			holder.find(".instShareEachCenter").each(function(){
				var $this = $(this);
				$this.find(".instShareCenterCHK").removeAttr("checked");	
				$this.find(".instShareCenterSummary").addClass("nonner").find(".blueTextColor").text("");
				$this.find(".instShareSelSection").data("sectionSrcEntities",null).find("input").removeAttr("checked");
			});
		}
	};
	var centerChecked = function(){
		var holder = $(this).closest(".instShareEachCenter");
		if($(this).is(":checked")){
			openSectionPopup(holder);
		}else{
			holder.find(".instShareCenterSummary").addClass("nonner").find(".blueTextColor").text("");
			holder.find(".instShareSelSection").data("sectionSrcEntities",null).find("input").removeAttr("checked");
			holder.closest(".instShareCentersHolder").find(".instShrAllCentersCHK").removeAttr("checked");
		}
	};
	var cancelCenter = function(){
		$(".instShareCentersHolder").html("");
		var choose = popupH.find(".instSelectShareBatch");
                vChooseVar.reset(choose,choose.data("defaultVal"),choose.data("defaultText"));
	};
	var doneCenter = function(){
		var inrHtml = "";var data = {"id":'','type':"PROGRAM","centers":[]};
		var batchHolder = $(".instSelectShareBatch");
		inrHtml += "<span class='big14 boldy'>"+batchHolder.attr("title")+" : </span>";
		data["id"] = batchHolder.data("value");
		var checkedCount = 0;
		$(".instShareEachCenter").each(function(){
			var checked = $(this).find(".instShareCenterCHK").is(":checked");
			if(checked){
				checkedCount++;
                                var centerNameDiv=$(this).find(".instShrCenterName");
				var name = centerNameDiv.text();
                                var centerId=centerNameDiv.data("centerId");
                                
				inrHtml +="<span class='boldy'>"+name+"</span>";
                                var instShareSelSectionData= $(this).find(".instShareSelSection").data();
                                var sectionNames=instShareSelSectionData.names;
				var sections =instShareSelSectionData.sectionSrcEntities;
				var centers = {'name':name,'sections':null,
                                    type:"CENTER",id:centerId};
				if(sections && sections.length>0){
					centers.sections = sections;
					inrHtml +=" / "+sectionNames.join(",");	
				}else{
					$(centers).removeProp("sections");
				}
				inrHtml += " ; ";	
				data["centers"].push(centers);
			}
		});
		if(checkedCount <= 0){
			showError("Please select atleast one center.");
			return;
		}
		cancelCenter();
		$(".instShareCentersHolder").addClass("nonner");	
		$(".instShareBatches").addClass("nonner");	
		$(".instSharedDetails").removeClass("nonner");
		var txtHolder = $($(".instEachSharedCenter").get(0)).clone();
		//postParams['with'].push(data);
		txtHolder.removeClass("nonner").data("obj",data).find(".txt").html(inrHtml);
		$(".instEachSharedHolder").append(txtHolder);
		enableShareBtn();
	};
	var enableShareBtn = function(){
		$(".shareWithDiv").find(".submitShare").removeClass("submitShareDisabled");
	};
	var disableShareBtn = function(){
		$(".shareWithDiv").find(".submitShare").addClass("submitShareDisabled");
	};
	var crossSelected = function(){
		var $this = $(this);
		var instEachSharedHolder = $this.closest(".instEachSharedHolder");
		var center = $this.closest(".instEachSharedCenter");
		center.remove();
		var allSelCenters = instEachSharedHolder.find(".instEachSharedCenter");
		if(allSelCenters.length<=1){
			$(".instSharedDetails").addClass("nonner");	
			addMoreToShare();
			if(popupH.find(".instSelectShareBatch").data("defaultVal") != "ORG"){
				disableShareBtn();
			}
		}
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
			case "INSTITUTE":shareToInst(shareDiv);
					break;	
			case "INDIVIDUALS":shareToIndv(shareDiv);
					break;	
			case "PUBLIC":shareToPublic(shareDiv);
					break;	
		}
		text = {"INSTITUTE":"with selected batch of the Institute","INDIVIDUALS":"with these selected users","PUBLIC":"Publicaly"};
		putConsoleLogs(postParams);
		smallLoader(popupH);
		var queryData = {
			url:"/UIComShare/shareEntity",
			params:postParams
		};
		if(callBackFn){
			queryData = callBackFn(queryData);
			if(!queryData || !queryData.url || $.trim(queryData.url).length==0) return;
		}
		$.post(queryData.url,queryData.params,function(data){
			putConsoleLogs(data);
			var msg = "";
			var successSpan = "<span>";
			var errorSpan = "<span class='redTextColor'>";
			var successButton = "<span class='greenButton cancelCommonPopup'>Okay</span>";
			var allowPublic = postParams.allowPublic?postParams.allowPublic:"";
			var allowIndv = postParams.allowIndv?postParams.allowIndv:"";
			var allowInst = postParams.allowInst?postParams.allowInst:"";
			var retryButton = "<span class='blueSubmitButton shareEntityRetry' data-entity-id='"+postParams.entity.id+"' data-entity-type='"+postParams.entity.type+"' data-allow-public='"+allowPublic+"' data-allow-indv='"+allowIndv+"' data-allow-inst='"+allowInst+"' style='margin-left:10px;padding:4px 12px;'>Retry</span>";
       			if(data.errorCode=="ALREADY_SHARED"){
           			msg += errorSpan+"You have already shared "+text[type];
				successButton += retryButton;
			}
			else if(data.errorCode.length>0){
          			msg += errorSpan+"Sharing Failed";
				successButton += retryButton;
			}
			else if(data.result.alreadySharedWith && data.result.alreadySharedWith.length>0){
				var shared=data.result.alreadySharedWith;
           			var people="";
           			for(var k=0;k<shared.length;k++){
               				people+=shared[k].firstName+" "+shared[k].lastName+", ";
           			}
           			msg += errorSpan+"You have already shared with "+people;
				successButton += retryButton;
			}else{
				msg += successSpan+"Shared";
			}
			msg +="</span>";
			var htmlMSG = "<div style='padding:20px;' class='big14 centerText greyTextColor'>"+msg+"</div>";
			htmlMSG += "<div class='centerText'>"+successButton+"</div>";
			$(popupH).html(htmlMSG); 
		});
	};
	this.getShareParams = function(holder){
   		var shareDiv=$(holder).find(".shareWithDiv");
		var type = shareDiv.data("shareType");
		var retP;
		switch(type){
			case "INSTITUTE":retP = shareToInst(shareDiv);
					break;	
			case "INDIVIDUALS":retp = shareToIndv(shareDiv);
					break;	
			case "PUBLIC":retP = shareToPublic(shareDiv);
					break;	
		}
		return retP;
	};
	var shareToPublic = function(shareDiv){
		postParams['with'] = [];
    		postParams.scope="PUBLIC";
		return postParams;
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
	var shareToInst = function(shareDiv){
		shareDiv = shareDiv.get(0)?shareDiv:$(".shareWithDiv");
		var orgId = shareDiv.find(".shareSelect").data("id");
		postParams['with'] = [{'id':orgId,"type":"ORGANIZATION"}];
		var summaryDivs = shareDiv.find(".instEachSharedHolder").find(".instEachSharedCenter");
		if(summaryDivs.length>1){
			postParams['with'] = [];
		}
		summaryDivs.each(function(){
			var wData = $(this).data("obj");
                    if(wData){ postParams['with'].push(wData);}
		});
		return postParams;
	};
	var sectionSelected = function(){
		/*$(this).prop("checked",true);
		return false;*/
	};
	var registerFns = function(popup){
		$(popup).find(".shareWithDiv").on("click",".instSelectShareCenter",centerClicked)
				.on("change","input:checkbox",sectionSelected)//#592
				.on("click",".instShrSecDone",sectionAddedDone)
				.on("click",".instShareCenterCNCL",cancelCenter)
				.on("click",".instShareCenterBtn",doneCenter)
				.on("click",".instShareCenterCHK",centerChecked)
				.on("click",".instShrAllCentersCHK",allCentersChecked)
				.on("click",".instShareAddMore",addMoreToShare)
				.on("click",".instEachSharedCenter .crossIt",crossSelected)
				.on("click.submitShare",".submitShare",shareClicked);

		$(popup).off("click",".shareEntityRetry",retryShare).on("click",".shareEntityRetry",retryShare);
		//disableShareBtn();
	};
	this.getShareTypeUi = function(vChoose,type){
		disableShareBtn();
		switch(type){
   			case "INDIVIDUALS":getIndvUi(vChoose);
					break;
			case "INSTITUTE": getInstUi(vChoose);
					break;
			case "PUBLIC": getPublicUi(vChoose);
					break;
    		}
	};
	var getIndvUi = function(vChoose){
		var shareDiv = vChoose.closest(".shareWithDiv");
    		var holder=shareDiv.find(".shareTypeHolder");
		var entityType = shareDiv.data('entityType');
		var params = {'entityType':entityType};
		smallLoader(holder);
		vReq.get("/UIComShare/getShareIndvUi",params,function(data){
			holder.html(data).find(".entrySuggInput").focus();
			enableShareBtn();
		});
	};
	var getPublicUi = function(vChoose){
		var shareDiv = vChoose.closest(".shareWithDiv");
    		var holder=shareDiv.find(".shareTypeHolder");
		var entityType = shareDiv.data('entityType');
		var params = {'entityType':entityType};
		smallLoader(holder);
		vReq.get("/UIComShare/getSharePublicUi",params,function(data){
			holder.html(data).find(".entrySuggInput").focus();
			enableShareBtn();
		});
	};
	var getInstUi = function(vChoose){
		var shareDiv = vChoose.closest(".shareWithDiv");
		var instId = vChoose.data("optParams").instId;
		vChoose.closest(".shareSelect").data("id",instId);	
		var entityType = shareDiv.data('entityType');
		var params = {'entityType':entityType,'orgId':instId};
		if(popupH.find(".shareWithDiv").data("allowAllShare")){
			params["allowAllShare"] = true;
			enableShareBtn();
		}
    		var holder = shareDiv.find(".shareTypeHolder");
		smallLoader(holder);
		vReq.get("/UIComShare/shareableBatches",params,function(data){
			holder.html(data);
			postParams['orgId'] = instId;
		});
	};
	this.getBatchDetails = function(vChoose,programmeId,url){
		var shareDiv = vChoose.closest(".shareWithDiv");
		if(programmeId == "ORG"){
			shareDiv.find(".instShareCentersHolder").html("");
			enableShareBtn();
			return;
		}else{
			disableShareBtn();
		}
		var batchIndex = vChoose.data("optParams").batchIndex;
                var orgId=shareDiv.find(".shareSelect").data("id");
                var entityType=shareDiv.data('entityType');
		var params = {};
                if(entityType)params.entityType=entityType;
                if(orgId)params.orgId=orgId;
		var params = {'batchIndex':batchIndex,programId:programmeId};
		var holder = shareDiv.find(".instShareCentersHolder");
		smallLoader(holder);
                url=url||"/UIComShare/shareableCenters";
		vReq.get(url,params,function(data){
			holder.html(data);
		});
	};
};
function shareSelectChange(vChoose,value){
    	vChoose.closest(".shareWithDiv").data("shareType",value);
    	shareUi.getShareTypeUi(vChoose,value);
}
function instShareBatchChanged(vChoose,value){
	shareUi.getBatchDetails(vChoose,value);
	var title = vChoose.find('.vChooseOptTicked').text();
	vChoose.attr("title",title);
}
