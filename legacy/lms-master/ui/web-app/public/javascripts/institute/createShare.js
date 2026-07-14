var instShareWid = new function($){
	var divId = ".instCreateShare";
	//var urlregex = new RegExp("^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$");
	var urlMap = {"Link":{url:"fetchExtUrl",type:"link"},"Vid":{url:"fetchExtVideo",type:"video"}};
	var attachClicked = function(){
		var $this = $(this);
		$(".instShareInput").find(".extAttachInput").addClass("nonner");
		if($this.hasClass("selected")){
			$this.removeClass("selected");
		}else{
			$this.addClass("selected").siblings().removeClass("selected");
			$(".instShareInput").find(".instShare"+$this.data("inputType")+"Input").removeClass("nonner").val("");
			if($this.hasClass("instShrPicIco")){
				var instShareFileInput = $($("iframe.instShareFileInput").get(0).contentDocument);
				var button = instShareFileInput.find(".blueButton");
				button.text(button.data("defaultText"));
			}
		}
		$(divId).find(".instAttachPreviewHold").html("").data("fetchUrl","");
		hideAllMsgNow();
	};
	var instShrTimeout;
	var attachInserted = function(e){
		$("#instCreateShrTable").find(".instAttachPreviewHold").find(".instItemPreview").data("src",null);
		var $this = $(this);
		if(instShrTimeout) clearTimeout(instShrTimeout);
		instShrTimeout = setTimeout(function(){
			var val = $.trim($this.val());
			var type = $this.data("inputType");
			fetchUrl(val,type);
		},100);
	};
	var verifyImgShare = function(type){
		if(type == "Link"){
			var div = $(divId);
			var imgCountDiv = div.find(".instThumbNextPrevHold");
			var imgCount = imgCountDiv.data("total");
			var orgCount = imgCount;
			if(imgCount<=0) return;
			var imgHolder = $("#testShareLoadedImg");
			div.find(".instAttachLinkImg").each(function(index,img){
				var newImg = document.createElement("img");
				newImg = $(newImg);
				newImg.attr("src",img.src).load(function(){
					imgHolder.html(newImg);
					if(newImg.width()<20 || newImg.height()<20){
						imgCount--;
						img.remove();
						//console.error("Error in size : "+index+", width = "+newImg.width()+", height = "+newImg.height());
						var now = index == orgCount-1?true:false;
						setUpdateTimer(now);	
					}else{
						$(img).data("loaded",true);
					}
				}).error(function(){
					imgCount--;
					img.remove();
					var now = index >= orgCount-1 ? true : false;
					setUpdateTimer(now);	
					//console.error("Error in Load : "+index);
				});
			});
			var updateTimerObj;
			var setUpdateTimer = function(now){
				if(updateTimerObj){clearTimout(updateTimerObj);}
				if(now){
					update();
				}else{
					setTimeout(function(){
						update();
					},1000);
				}
			};
			var update = function(){
				imgHolder.html("");
				if(imgCount <= 0){
					imgCountDiv.remove();
				}
				else if(orgCount>imgCount){
					imgCountDiv.data("total",imgCount).find("#curTotal").text(imgCount);	
					if(imgCount<=1){
						imgCountDiv.find(".instAttachLinkNext,.instAttachLinkPrev").removeClass("active");
					}
					if(imgCountDiv.data("count")>=imgCount){
						imgCountDiv.data("count",imgCount-1).find("#curCount").text(imgCount);
						var width = div.find(".instAttachLinkImgContainer").width()+3;
						var left = (imgCount-1) *(-1)*width;
						div.find(".instAttachLinkAllImgs").animate({"left":left+"px"});
						imgCountDiv.find(".instAttachLinkNext").removeClass("active");
					}
				}
			}
		}
	};
	var fetchUrlXHR;
	var fetchUrl = function(url,type){
		if(url==""){
			$(divId).find(".instAttachPreviewHold").html("").data("fetchUrl","");
			hideAllMsgNow();
			return;
		}
		else if(url == $(divId).find(".instAttachPreviewHold").data("fetchUrl")){
			return;
		}
		var testUrl = true;
		/*try{
			testUrl = urlregex.test(url);
		}catch(err){}*/
  		if(testUrl){
			hideAllMsgNow();
			var holder = $(divId).find(".instAttachPreviewHold");
			smallLoader(holder);
			var fetchUrl = "/Institute/"+urlMap[type].url;
			if(fetchUrlXHR){ fetchUrlXHR.abort(); }
			fetchUrlXHR = $.get(fetchUrl,{'url':url,'type':urlMap[type].type},function(data){
				holder.html(data);
				verifyImgShare(type);
			}).error(function(){
				holder.html("");
				showMsgInTimeout(type);
			});	
			holder.data("fetchUrl",url);
		}else{
			$(divId).find(".instAttachPreviewHold").html("").data("fetchUrl","");
			showMsgInTimeout(type);
		}
	};
	var refineTheMessage = function(par){
		par = $(par);
		par.find("script,input,textarea,img").remove();
		par.find("a").each(function(){
			var $this = $(this);
			$this.replaceWith("<a class='blueTextColor' target='_blank' href='"+$this.prop("href")+"'>"+$this.html()+"</a>");
		});
		return par.html();
	};
	var instShareBoxChanged = function(){
		var $this = $(this);
		setTimeout(function(){
			$this.find("script,input,textarea,img").remove();
		},100);
	}
	var attachDiscard = function(){
		$(this).closest(".instItemPreview").remove();
		divId.find(".extAttachInput").val("");
	};
	var typesList = {
	};
	var toJSON = function(data){
		try{
			if(typeof data=="string"){
				return JSON.parse(data);
			}else{
				data = cloneObject(data);
			}
		}catch(err){
			data = undefined;
		}
		return data;
	};
	var getShareEntity = function($this,text,orgText){
		var type = $this.closest(".instShrOptionsCol").find(".selected").data("inputType");
		var item = $this.closest(".instCreateShare").find(".instAttachPreviewHold")
			.find(".instItemPreview");
		var dataSrc = item.data("src");
		//console.log(dataSrc);
		var src;
		switch(type){
		    case "Link":
				src = toJSON(dataSrc);
				var thumbnails = src.thumbnail;
				var tIndex = item.find(".thumbnailCell").data("index");
				if(typeof thumbnails == "object" && thumbnails.length>0){
					try{
						var imgs = item.find(".instAttachLinkImg");
						if(imgs.get(tIndex) && $(imgs.get(tIndex)).data("loaded")){
							src.thumbnail = imgs.get(tIndex).src;
						}else if($(imgs.get(0)).data("loaded")){
							src.thumbnail = imgs.get(0).src;
						}
					}catch(err){
						src.thumbnail = thumbnails[tIndex]?thumbnails[tIndex]:thumbnails[0];
					};
				}
				src.image = src.thumbnail;
				src.type = "WEB_PAGE";
				src.linkType = "ADDED";
				var domainUrl = src.url;
                		domainUrl = domainUrl.substring(0,domainUrl.indexOf('/',domainUrl.lastIndexOf('.')));
				src.linkInfo = {
					originalURL : src.url,
					domainName : "",
					domainURL : domainUrl,
				};
				break;
		    case "File":
				src = toJSON(dataSrc);
				src.type = "IMAGE";
				src.linkType = "UPLOADED";
				src.image = src.url;
				break;
		    case "Vid":	
				src = toJSON(dataSrc);
				src.content = $.trim(src.content);
				$(src).removeProp("description");
				src.type = "LINK_VIDEO";
				src.linkType = "ADDED";
				src.linkInfo = {
					id : src.videoId,
					originalURL : src.url,
					domainName : src.site_name,
					domainURL : src.domainUrl,
				};
				break;
		    default : if(!orgText || orgText.length==0){
					showError(i18nJS("PLEASE_ENTER_TEXT"));
				}
				break;
		};
		return src;
	};
	var hideAll = function(){
		divId.find(".instShrOptionsCol").fadeTo(100,0,function(){
				$(this).hide();
			})
			.find(".selected").removeClass("selected");
		divId.find(".instItemPreview").remove();
		divId.find(".extAttachInput").val("").addClass("nonner");
		var div = $("#instCreateShrTable").find(".instShareInputBox")
			.removeClass("focusBox")
			.data("focused",false)
			.height("initial");
		div.html("<span class='placeholder'>"+div.data("placeholder")+"</span>");
	};
	var shareWithClicked = function(){
		var $this = $(this);
		if($this.hasClass("btnDisabled")) return true;
		var shareTextBox = $this.closest("#instCreateShrTable").find(".instShareInputBox");
		var text = refineTheMessage(shareTextBox);
		var orgText = shareTextBox.text().trim();
		var src = getShareEntity($this,text,orgText);
		var params = {};
		if(src || orgText){
		   if(src){
			params.source = src;
		   }
		   params.statusMessage = text;
		   var popup = showVPopup(0.7,true);
		   var org = $("#myInstitutePage").data("orgObj"); 
		   hideAllMsgNow();
		   shareUi.openInst(org.orgId,org.instFullName,"STATUSFEED",popup,true,function(data){
			params["with"] = data.params["with"];
			closeVPopup();
			showTopLoader();
			divId.find(".shrWithBtn").addClass("btnDisabled");
			vReq.post("/Institute/addActivityFeed",params,function(data){
				hideTopLoader();
				divId.find(".shrWithBtn").removeClass("btnDisabled");
				try{
					var hidePost = institute.activities.addNewFeed(data);
					if(hidePost){
						hideAll();
					}else{
						showMsgInTimeout("AddError");
					}
				}catch(err){}	
			}).error(function(){
				hideTopLoader();
				divId.find(".shrWithBtn").removeClass("btnDisabled");
				showMsgInTimeout("AddError");
			});
			return;
		   });
		}
	};
	var hideMsgTimeout;
	var hideAllMsgNow = function(){
		if(hideMsgTimeout) clearTimeout(hideMsgTimeout);
		$(divId).find(".instNotValidUrlTxt").children().addClass("nonner");
	}
	var showMsgInTimeout = function(type){
		hideAllMsgNow();
		$(divId).find(".instNotValidUrlTxt").find("#"+type).removeClass("nonner");
		hideMsgTimeout = setTimeout(function(){
			$(divId).find(".instNotValidUrlTxt").children().addClass("nonner");
		},155000);
	}
	this.imageUploaded = function(data,html){
		data = toJSON(data);	
		var holder = $(divId).find(".instAttachPreviewHold");
		if(data.errorCode==""){
			holder.html(html);
		}else{
			holder.html("");
			showMsgInTimeout("File");
		}
	};
	var instShareFocused = function(){
		var $this = $(this);
		if($this.data("focused")) return;
		$this.animate({height:44},250,function(){
			$this.data("focused",true);
			$this.addClass("focusBox").html("");
			divId.find(".instShrOptionsCol").fadeTo(350,1);
		});
	};
	this.init = function(){
		divId = $(divId);
		divId.on("click",".instShrLinkIco,.instShrPicIco,.instShrVidIco",attachClicked)
			.off("paste keydown",".instShareLinkInput,.instShareVidInput",attachInserted)
			.on("paste keydown",".instShareLinkInput,.instShareVidInput",attachInserted)
			.on("click",".instAttachDiscard",attachDiscard)
			.on("click",".shrWithBtn",shareWithClicked)
			.on("change paste",".instShareInputBox",instShareBoxChanged)
			.on("click",".instShareInputBox",instShareFocused);
	};
}(jQuery);
window.top.onIframeImgLoad = function(data,html){
	html = $.trim(html);
	instShareWid.imageUploaded(data,html);
}
$(function(){
                instShareWid.init();
});
