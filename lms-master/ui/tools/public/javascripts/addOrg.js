var addOrg = new function(){
	var parDiv;
	this.init = function(){
		parDiv = $("#addOrgForm");
		parDiv
			.on("change",".orgShortName,.orgFullName",fillDataList)
			.on("change",".websiteInputField",websiteChanged)
			.on("keyup paste change", ".checkSlugAvailability input", checkSlugAvailability)
	                .on("blur", ".checkSlugAvailability input", doneSlugCheck)
			.on("click", ".addNewLocation", addNewLocation)
	                .on("click", ".removeLocation", removeLocation)
	                .on("click", ".showTermsConditions", showTermsConditions)
	                .on("click", ".whatIsThisField", showFieldHelp)
    			.on('click',".refreshCaptcha",function(){
            			refreshCaptcha($(this).closest(".captchaDiv"));
        		})
			.on("focus",".captchaDiv .code",function(){
				$(this).removeClass("errorBorder");
			})
			.on("submit.ADD_ORG",addOrgFormSubmit);
            	refreshCaptcha(parDiv.find(".captchaDiv"));
		var typeDiv = parDiv.find("select[name='type']");
		var type = typeDiv.data("value");
		if(type && typeDiv.get(0)){
			typeDiv.find("option[value='"+type+"']").prop("selected",true);
		}
	};
	var fillDataList = function(){
		var $this = $(this);
		var name = $this.val();
		var slug;
		var splitted = name.split(" ");
		var datalist = parDiv.find("#suggSlugs");
		if(splitted.length>1){
			slug = splitted.join("");
			push(datalist,slug);
		}
		slug = splitted[0];
		push(datalist,slug);
		function push(datalist,val){
			val = val ? val.toLowerCase():"";
			if(!datalist.find("option[value='"+val+"']").get(0)){
				datalist.append("<option value='"+val+"'>"+val+"</option>");
			}
		}
		if($this.hasClass("orgShortName")){
			var target = parDiv.find("input[name='slug']");
			checkSlugWithServer(target,slug,"EXT",function(stat){
				if(stat){
					target.val($.trim(slug).toLowerCase());
				}
			},true);
		}
	};
	var showFieldHelp = function(){
		var $this = $(this);
		var text = $this.data("text");
		if(text){
			showMessage(text,function(){
				$this.closest("td").siblings().find("input:first").focus();
			});
		}
	};
	var termsFrame;
	/*var onFrameClick = function(){
		$("#vPopup .termsAndCondition").find(".backToTerms")
			.removeClass("nonner")
			.off("click")
			.on("click",function(){
				onFrameLoad($("#termsAndConditionHold"));
			});
	}*/
	function onFrameLoad(div){
		var termsDiv = $("#vPopup").find(".termsAndCondition");
		var version = div.data("version"); 
		var name = div.data("name"); 
		var frame = "<iframe rel='nofollow' src='/terms/get/"+name+"?version="+version+"'></iframe>";
		termsDiv.find(".trmsCondBody").html(frame);
		var termsDiv = $("#vPopup .termsAndCondition");
		termsDiv.find("iframe").load(function(){
			termsFrame = window.frames[0].window;
			var head = termsFrame.document.getElementsByTagName('head')[0];
			var s = document.createElement('style');
    			s.setAttribute('type', 'text/css');
			var css = "body.c14{ padding:0px;}";
    			if (s.styleSheet) {   // IE
        			s.styleSheet.cssText = css;
    			} else {                // the world
        			s.appendChild(document.createTextNode(css));
    			}
			/*$(termsFrame.document).find("a")
				.off("click")
				.on("click",onFrameClick);*/
			termsDiv.find(".backToTerms").addClass("nonner");
    			head.appendChild(s);
		});
	};
	var showTermsConditions = function(){
		var div = $("#termsAndConditionHold");
		showPopup(div.html());
		onFrameLoad(div);
	};
    	var slugChkXHR;
	var slugChkTimer;
	var slugDoneTimer;
    	var doneSlugCheck = function(e) {
           	var $this = $(this);
	   	if(slugDoneTimer) clearTimeout(slugDoneTimer);
		var otherSlugVal = parDiv.find(".slugVal").text();
		if($this.val()!=otherSlugVal){
			checkSlugWithServer($this,null,e.type);
		}else{
	   		slugDoneTimer = setTimeout(function(){
        			var msgBox = $this.closest(".checkSlugAvailability").find(".slugCheckStatus");
        			if (msgBox.data("status") == "success") {
            				$this.removeClass("errorBorder");
            				$this.removeClass("successBorder");
            				msgBox.text("").addClass("nonner");
        			}
	   		},100);
		}
    	};
    	var checkSlugAvailability = function(e) {
        	var $this = $(this);
		var eType = e.type;
		checkSlugWithServer($this,null,eType);
	};
	var slugOnBlur = function(e){
		var $this = $(this);
	};
	var checkSlugWithServer = function($this,text,eType,cbFn,fetchOnly){
        	var msgBox = $this.closest(".checkSlugAvailability").find(".slugCheckStatus").removeClass("nonner");
	   	if(slugChkTimer) clearTimeout(slugChkTimer);
        	slugChkTimer = setTimeout(function() {
            		text = text || $this.val();
			text = $.trim(text).toLowerCase();
            		if (text.length > 1) {
                		if (slugChkXHR) {
                    			slugChkXHR.abort();
                    			slugChkXHR = undefined;
                		}
                		slugChkXHR = $.get("/Organizations/checkSlug", {slug: text}, function(resp) {
                    			if (!resp.errorCode && resp.result) {
                        			if (resp.result.available) {
                            				showAvailable();
							callBackCaller(true);
                        			} else {
                            				showErrorMsg("Un-Available");
							callBackCaller(false);
                        			}
                    			} else {
                        			showErrorMsg("Invalid format! TIP: neither space nor special characters, only alphanumeric value");
						callBackCaller(false);
                    			}
                		});
                		clearMsg();
            		} else {
                		showErrorMsg("Too Short");
            		}
        	}, 100);
		function callBackCaller(stat){
			if(cbFn){
				try{ cbFn(stat);}catch(err){}
			}
		}
        	function clearMsg() {
            		$this.removeClass("errorBorder");
            		$this.removeClass("successBorder");
            		msgBox.text("").data("status", "");
        	}
        	function showErrorMsg(msg) {
			if(fetchOnly) return;
            		$this.removeClass("successBorder").addClass("hasError");
            		msgBox.text(msg).addClass("redTextColor").removeClass("greenTextColor").data("status", "error");
			parDiv.find(".slugVal").text("");
        	}
        	function showAvailable() {
            		$this.removeClass("hasError").addClass("successBorder");
			//$this.val(text);
            		msgBox.text("Available").removeClass("redTextColor").addClass("greenTextColor").data("status", "success");
			parDiv.find(".slugVal").text(text);
	    		if(eType == "change" || fetchOnly){
				msgBox.text("");
	    		}
        	}
    	};
    var addNewLocation = function() {
        var locDiv = $(this).closest(".locationDiv");
        var newLoc = locDiv.clone(true);
        newLoc.find(".addNewLocation").toggleClass("addNewLocation removeLocation").text("Remove");
        newLoc.find("input").val("");
        locDiv.parent().append(newLoc);
        resetLocationAttrNames(locDiv.parent());
    };
    var removeLocation = function() {
        var target = $(this).closest(".locationDiv");
        var locationsTd = target.parent();
        target.remove();
        resetLocationAttrNames(locationsTd);
    };
    var resetLocationAttrNames = function(locationsTd) {
        var locDivs = locationsTd.find(".locationDiv");
        for (var k = 0; k < locDivs.length; k++) {
            var inputEls = locDivs.eq(k).find("input");
            for (var i = 0; i < inputEls.length; i++) {
                var el = inputEls.eq(i);
                var newAttrName = "locations[" + k + "]." + el.attr("rel");
                el.attr("name", newAttrName);
            }
        }
    };
	var websiteChanged = function(){
		var $this = $(this);
        	var website = $this.val();
        	$.get("/Organizations/checkWebsite", {website: website}, function(data) {
            		var availability = data.result?data.result.available:false;
            		if(data.errorCode == "INVALID_WEBSITE"){
				showError("Please enter proper website url. Should start with http:// or https://",function(){
	                		$this.addClass("hasError").focus();
				});
            		}else if (availability) {
	        		$this.removeClass("hasError");
            		}else if(availability == false){
				showError("This website is already registered with us.",function(){
	                		$this.addClass("hasError").focus();
				});
			}else{
				showError("Please re-check the website url entered by you.",function(){
	                		$this.addClass("hasError").focus();
				});
			}
        	});
	};
    	var addOrgFormSubmit = function(e) {
        	e.preventDefault();
        	var $this = $(this);

		if($this.find(".hasError").get(0)){
	   		setTimeout(function(){
				showError("Please fix the error in the page , before proceeding.",function(){
					$this.find(".hasError:first").focus();
				});
	   		},100);
	   		return;
		};
                parDiv.off("submit.ADD_ORG");
		var planName = $this.find("input[name='planId']:checked").data("planName");
		$this.append("<input type='hidden' name='planName' value='"+planName+"' />");
                $this.submit();
    	};
	function validDomain(s) {
    		if (s.length > 0) {
        		var j = s.indexOf("."), k = s.indexOf(","), kk = s.indexOf(" "), jj = s.lastIndexOf(".") + 1,
                	len = s.length;
        		if (j !== 0 && (k == -1) && (kk == -1) && (len - jj >= 2) && (len - jj <= 4)) {
            			return true;
        		}
    		}
        	return false;
	}
};
$(function(){
	addOrg.init();
});
