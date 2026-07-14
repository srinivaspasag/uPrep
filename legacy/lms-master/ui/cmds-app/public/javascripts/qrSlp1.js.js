var qrSlp = new function(){
	var parDiv,topicsHolder,sampleHolder;
	this.init = function(){
		parDiv = "#createSLP";
		topicsHolder = ".slpCreationContainer";
		sampleHolder = ".slpCreateSamples";
		parDiv = $(parDiv);
		topicsHolder = parDiv.find(topicsHolder);
		sampleHolder = parDiv.find(sampleHolder);
		fixContentSec();
		regFns();
	};
	var regFns = function(){
		parDiv
			.on("click",".addSlpTopicLink",addSlpTopicLink)	
			.on("click",".addSlpTopicSubmit",addSlpTopicSubmit)	
			.on("enterkey",".addSlpTopicInput .inputDiv",addSlpTopicSubmit)	
			/*.on("click",".addSlpEntityLink",addSlpEntityLink)
			.on("click",".addSlpEntitySubmit",addSlpEntitySubmit)
			.on("enterkey",".addSlpEntityInput .inputDiv",addSlpEntitySubmit)
			.on("focus",".addSlpEntityInput .inputDiv",addSlpEntityFocus)*/
			.on("focus",".addSlpTopicInput .inputDiv",addSlpTopicFocus)
			.on("click",".slpAddContentLink",slpAddContentLink)
			.on("click",".cancelSlpAddContentBtn",cancelSlpAddContentBtn)
			.on("click",".slpAddContentType",slpAddContentType)
			.on("click",".cmdsaPush",onClickEntity)
			.on("keyup",".slpNameInput",slpNameInputChanged)
			.on("click",".slpAddFromResource",slpAddFromResource);
		cloneHelper.init();
	};
	var slpTitleInputTimeout;
	var slpNameInputChanged = function(){
		var $this = $(this);
		if(slpTitleInputTimeout) clearTimeout(slpTitleInputTimeout);
		slpTitleInputTimeout = setTimeout(function(){
			parDiv.find(".slpModuleTitle").text(" - "+$this.val().trim());
		},100);
	};
	var entitySelected = function(entity,$this){
		if(entity){
			var par = $this.data("entity",entity);
			par.find(".slpEntity")
				.html(entity.linkHtml)
				.data("title",entity.name)
				.find(".cmdsaPush").removeAttr("title");
			var chooser = par.find(".chooseSlpEntityRequirement").removeClass("hider");
			if(window.webkitURL){
				chooser.addClass("webkitOffset");
			}
			var myTopicHolder = $this.closest(".topicCreationHold");
			var index = myTopicHolder.find(".subTopicCreationHold").index($this.closest(".subTopicCreationHold"));
			index += 1;
			var indexText = myTopicHolder.find(".topicCreationTitle").data("index");
			indexText += "."+index+" ";
			par.data("index",index).find(".slpEntityIndex").text(indexText);
			var createContent = $this.siblings(".subTopicCreationContent");
			createContent.find(".slpAddContentHold").animate({"max-height":"0px"},300,function(){
				$this.removeClass("activated");
				$(this).remove();
			});
				
			animScroll(par.find(".animScroll"),par.find(".slpEntityName"));
			cloneHelper.subTopic(myTopicHolder);
		}
	};
	var slpAddFromResource = function(contentType,contentName,refObj){
		resourcePopup.show(contentType,contentName,refObj);
	};
	/*var slpTipsGotIt = function(){
		$(this).closest(".slpTipsHold").animate({"max-height":'0px'},300,function(){
			$(this).addClass("nonner");
		});
	};*/
	var slpAddContentType = function(){
		var $this = $(this);
		var type = $this.data("type");
		var name = $this.find(".greyTextColor").text();
		var refObj = $this.closest(".subTopicCreationHold").find(".subTopicCreationTitle.activated");
		slpAddFromResource(type,name,refObj);
	};
	var slpAddContentLink = function(){
		var $this = $(this);
		var topicName = $this.closest(".topicCreationHold").find(".topicCreationTitle").data("name");
		if(!topicName){
			showError("Please provide us with a title to the Module topic");
			return;
		}
		$this.closest(".subTopicCreationTitle").addClass("activated");
		var div = cloneHelper.addContent($this.closest(".subTopicCreationHold").find(".subTopicCreationContent"));
		$(div).animate({"max-height":"130px"},300,function(){
			$this.addClass("nonner").siblings(".cancelSlpAddContentBtn").removeClass("nonner");
		});
	};
	var cancelSlpAddContentBtn = function(){
		var $this = $(this);
		$this.closest(".subTopicCreationHold").find(".slpAddContentHold").animate({"max-height":"0px"},300,function(){
			$this.closest(".subTopicCreationTitle").removeClass("activated");
			$this.addClass("nonner").siblings(".slpAddContentLink").removeClass("nonner");
			$(this).remove();
		});
	};
	var addTitleClicked = function($this,parId,mainTypeId){
		var holder = $this.closest(".animScroll");
		var par = holder.find(parId);
		animScroll(holder,par,null,function(){
			var name = $this.closest(mainTypeId).data("name");
			name = name ?  name : name;
			par.find(".inputDiv").focus().val(name);
		});
	};
	var addSlpTopicFocus = function(e){
		var $this = $(this);
		var slpNameInput = parDiv.find(".slpNameInput");
		var slpName = slpNameInput.val().trim();
		if(!slpName){
			showError("Please provide us with a title to the Module!",function(){
				slpNameInput.focus();
			});
			var pos = $this.closest(".animScrollItem").position()["top"] * 1;
			$this.closest(".animScroll").css({"top":pos+"px"});
			e.preventDefault();
			return false;
		}else{
			var pos = $this.closest(".animScrollItem").position()["top"] * -1;
			$this.closest(".animScroll").css({"top":pos+"px"});
		}
	};
	/*var addSlpEntityFocus = function(e){
		var $this = $(this);
		var topicName = $this.closest(".topicCreationHold").find(".topicCreationTitle").data("name");
		if(!topicName){
			showError("Please provide us with a title to the SLP topic");
			var pos = $this.closest(".animScrollItem").position()["top"] * 1;
			$this.closest(".animScroll").css({"top":pos+"px"});
			e.preventDefault();
			return false;
		}else{
			var pos = $this.closest(".animScrollItem").position()["top"] * -1;
			$this.closest(".animScroll").css({"top":pos+"px"});
		}
	};*/
	var addSlpTopicLink = function(){
		var $this = $(this);
		var slpNameInput = parDiv.find(".slpNameInput");
		var slpName = slpNameInput.val().trim();
		if(!slpName){
			showError("Please provide us with a title to the Module!",function(){
				slpNameInput.focus();
			});
		}else{
			addTitleClicked($this,".addSlpTopicInput",".topicCreationTitle");
		}
	};
	/*var addSlpEntityLink = function(){
		var $this = $(this);
		var topicName = $this.closest(".topicCreationHold").find(".topicCreationTitle").data("name");
		if(!topicName){
			showError("Please provide us with a title to the SLP topic");
		}else{
			addTitleClicked($this,".addSlpEntityInput",".subTopicCreationTitle");
		}
	};
	var addSlpEntitySubmit = function(){
		var $this = $(this);
		var val = $this.closest(".addSlpEntityInput").find(".inputDiv").val();
		if(val){
			var par = $this.closest(".subTopicCreationTitle").data("name",val);
			par.find(".slpEntityTitle").text(val);
			var myTopicHolder = $this.closest(".topicCreationHold");
			var index = myTopicHolder.find(".subTopicCreationHold").index($this.closest(".subTopicCreationHold"));
			index += 1;
			var indexText = myTopicHolder.find(".topicCreationTitle").data("index");
			indexText += "."+index+" ";
			par.data("index",index).find(".slpEntityIndex").text(indexText);
			animScroll(par.find(".animScroll"),par.find(".slpEntityName"));
			cloneHelper.subTopic(myTopicHolder);
		}else{
			showError("Please provide us with a title");
		}
	};*/
	var addSlpTopicSubmit = function(){
		var $this = $(this);
		var val = $this.closest(".addSlpTopicInput").find(".inputDiv").val();
		if(val){
			var par = $this.closest(".topicCreationTitle").data("name",val);
			par.find(".slpTopicTitle").text(val);
			var index = topicsHolder.find(".topicCreationHold").index($this.closest(".topicCreationHold"));
			index += 1;
			par.data("index",index).find(".slpTopicIndex").text(index+". ");
			animScroll(par.find(".animScroll"),par.find(".slpTopicName"));
			cloneHelper.topic();
		}else{
			showError("Please provide us with a title to the Module topic");
		}
	};
	var onClickEntity = function(e){
		var url = $(this).attr("href");
		var win=window.open(url, '_blank');
  		win.focus();
		e.preventDefault();
		e.stopPropagation();
		return false;	
	};
	var animScroll = function(holder,child,index,cbFn){
		holder = $(holder);
		child = child ? child : holder.find(".animScrollItem").get(index);
		child = $(child);
		var pos = child.position()["top"] * -1;
		holder.animate({"top":pos+"px"},250,function(){
			try{
				if(cbFn){ cbFn();}
			}catch(err){}
		});
	};
	var resourcePopup = new function(){
		var popup;
		var popupHolder;
		var mcWidget,mcParams;
		var resourceUrl = "/qrresources/popupResourcesTable";
		var tableHead,tableBody,refObj;
		this.show = function(contentType,name,_refObj){
			//popup = getcmdsPopupBody(550);
			popup = showVPopup(0.3);
			var res = sampleHolder.find(".slpAddResourcePopup").clone(false);
	        	$(popup).html(res);
			popupHolder = $(popup).find(".slpAddResourcePopup");
			popupHolder.find(".contentType").text(name);
			mcWidget = popup.find(".mcWidget");
        		mcParams = {orderBy: "timeCreated","includes":contentType};
        		initmcWidgetforCMDS(mcWidget, resourceUrl, mcParams, true, true, afterLoad);
			//REG FNs
			popupHolder.on("click",".cmdsaPush",onClickEntity)
				.on("click",".gCBox",entityChecked)
				.on("click",".selectSlpEntity",slpEntitySelected)
			refObj = _refObj;
		};
		var slpEntitySelected = function(){
			showTopLoader();
			var tr = tableBody.find(".gCBoxChecked").closest("tr");
			var type = tr.data("entityType");
			var entity = {
				name : tr.data("entityName"),
				id : tr.data("entityId"),
				type : type,
				entityType : type.replace("CMDS","")
			}
			$.get("/Widgets/getEntityLink",{"entity":entity},function(data){
				entity.linkHtml = data;
				reset();
				setTimeout(function(){
					entitySelected(entity,refObj);
					hideTopLoader();
					refObj = null;
				},250);
			});
		};
		var reset = function(){	
			closeVPopup();
			popup = popupHolder = mcWidget = mcParams = tableHead = tableBody = null;
		};
		var entityChecked = function(){
		    var $this = $(this);
		    setTimeout(function(){
			var isChecked = $this.hasClass("gCBoxChecked");
			var selectBtn = tableHead.find(".selectSlpEntity");
			if(isChecked){
				selectBtn.removeClass("hider");
				tableBody.find(".gCBox").not($this).addClass("hider");
			}else{
				selectBtn.addClass("hider");
				tableBody.find(".gCBox").removeClass("hider");
			}
		    },100);
		};
		var afterLoad = function(){
			tableHead = popupHolder.find(".headSecTable");
			tableBody = popupHolder.find(".cmdsTable");
        		calcTitleMaxWidth();
			popupHolder.find(".cmdsAllgCBox").replaceWith(popupHolder.find(".selectResoutceBtnHolder").html());
		};
    		var calcTitleMaxWidth = function() {
        		var entityNameDiv = tableBody.find(".entityNameDiv");
        		var entityNameWidth = tableHead.find(".itemTitleTH").width() - 10;
        		var entityNameMaxWidth = entityNameWidth - 30;
        		entityNameDiv.width(entityNameWidth)
                		.find(".singleLineText").css("max-width", entityNameMaxWidth);
    		};
	};
	var cloneHelper = new function(){
		var clnHolder;
		var topicDiv = ".topicCreationHold";
		var subTopicDiv = ".subTopicCreationHold";
		var addContentDiv = ".slpAddContentHold";
		var targetContainer = ".slpCreationContainer";
		this.init = function(){
			clnHolder = ".slpCreateSamples";
			clnHolder = parDiv.find(clnHolder);
			this.topic(targetContainer);
		};
		this.append = function(what,to){
			to = $(to);
			var cln = clnHolder.find(what).clone();
			to.append(cln);
			return cln;
		};
		this.html = function(what,to){
			var cln = clnHolder.find(what).clone();
			$(to).html(cln);
			return cln;
		};
		this.animate = function(div,cbFn){
			$(div).fadeTo(250,1,cbFn);
		}
		this.topic = function(to,append){
			to = to ? to : targetContainer;
			var cln = this.append(topicDiv,to);
			cloneHelper.animate(cln,function(){
				cloneHelper.animate(cln.find(subTopicDiv));
			});
		};
		this.subTopic = function(to){
			var cln = this.append(subTopicDiv,to);
			this.animate(cln);
		};
		this.addContent = function(to){
			var cln = this.append(addContentDiv,to);
			return cln;
		};
	};
};