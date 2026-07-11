var uiCloneHelper={
	par:"",toClone:"",available:0,
	set:function(par,toClone,available){
		this.par = $(par);this.available = available!=undefined?available:this.available;
		this.toCloneStr = toClone;this.toClone = this.par.find(toClone);
	},
	findByIndex:function(index){
		return this.toClone.get(index);
	},
	create:function(index,setAttrs){
		var div = this.findByIndex(index);
		if(!div){
			div = $(this.toClone.get(this.available)).clone();
			this.par.append(div);this.toClone = this.par.find(this.toCloneStr);
		}if(typeof setAttrs == "object") $(div).attr(setAttrs);
		return div;
	},
	remove:function(index){
		var div = this.findByIndex(index);
		if(this.toClone.length<=1)return false;
		$(div).remove();
		this.toClone = this.par.find(this.toCloneStr);
	},
	removeFrom:function(index){
		if(index<=this.available)index=this.available+1;
		for(index;index<this.toClone.length;index++){
			var div = this.findByIndex(index);
			if(!div)break;
			$(div).remove();
		}
		this.toClone = this.par.find(this.toCloneStr);
	},
	removeByObj:function(obj){
		var index = this.toClone.index(obj);
		this.remove(index);
	}
};
function cloneObject(inObj){
	var outObj = JSON.stringify(inObj);
	outObj = JSON.parse(outObj);
	return outObj;
};
function getAllUrlParams(){
	var url = location.search;var ret = {};
	if(url){
		url = url.replace("?","");
		url = url.split("&");
		for(u in url){
			var t = url[u].split("=");
			ret[t[0]]=decodeURIComponent(t[1]);
		}
	}
	return ret;
}
function getURLParameter(name){
    var param="";
    try{
	var url = location.search;
	if(!url){ return param;}
	url = url.replace("?","");
	name += "=";
	var nameIndex = url.indexOf(name);
	if(nameIndex>=0){
		param = url.substr(nameIndex+name.length);
		if((eIndex = param.indexOf("&"))>0){
			param = param.substring(0,eIndex);
		}
		param = decodeURIComponent(param);
	}
    }
     catch(err){ param="";}
     return param;
}
new function($) {
  $.fn.setCursorPosition = function(pos) {
    if ($(this).get(0).setSelectionRange) {
      $(this).get(0).setSelectionRange(pos, pos);
    } else if ($(this).get(0).createTextRange) {
      var range = $(this).get(0).createTextRange();
      range.collapse(true);
      range.moveEnd('character', pos);
      range.moveStart('character', pos);
      range.select();
    }
  }
  $.fn.getCursorPosition = function() {
        var el = $(this).get(0);
        var pos = 0;
        if('selectionStart' in el) {
            pos = el.selectionStart;
        } else if('selection' in document) {
            el.focus();
            var Sel = document.selection.createRange();
            var SelLength = document.selection.createRange().text.length;
            Sel.moveStart('character', -el.value.length);
            pos = Sel.text.length - SelLength;
        }
        return pos;
   }
}(jQuery);


function goToTestListingPage(){
	openMyPage("/Tests/listUserTest");
}
function putConsoleLogs(msg){
	try{console.log(msg);}catch(err){}
}
function putConsoleError(msg){
	try{console.error(msg);}catch(err){}
}
$(document).on("change",".websiteTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	val = $.trim(val);
	if(val=="") return;
	var urlRegExp = new RegExp("^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$");
	try{
        	val = urlRegExp.test(val);
        }catch(err){}
	target.removeClass("hasError");
	if(!val){
		showError("Not a Valid Website Url , Use http:// https:// start of the url",function(){
			target.focus();
		});
		target.addClass("hasError");
	}
	target.focus();
});
$(document).on("change",".emailDomainTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	val = $.trim(val);
	if(val=="") return;
	var urlRegExp = new RegExp('((?:[a-z][a-z0-9_]*))'+'(\\.)'+'((?:[a-z][a-z]+))');
	try{
        	val = urlRegExp.test(val);
        }catch(err){}
	target.removeClass("hasError");
	if(!val){
		showError("Not a Valid Email Domain , If your Email-Id is 'someone@gmail.com', then your email domain is 'gmail.com' ");
		target.addClass("hasError");
	}
	target.focus();
});
$(document).on("change",".emailTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	val = $.trim(val);
	if(val=="") return;
	var urlRegExp = new RegExp('^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$');
	try{
        	val = urlRegExp.test(val);
        }catch(err){}
	target.removeClass("hasError");
	if(!val){
		showError("Not a Valid Email Id , Example - Email-Id is 'someone@gmail.com'");
		target.addClass("hasError");
	}
	target.focus();
});
$(document).on("change",".dateTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	val = $.trim(val);
	if(val=="") return;
	var validate = false;
	var urlRegExp = new RegExp('(\\d)(\\d)(\\d)(\\d)(-)(\\d)(\\d)(-)(\\d)(\\d)',["i"]);
	try{
        	validate = urlRegExp.test(val);
		if(validate){
			var split = val.split("-");
			if(split[1]>12) validate = false;
			if(split[2]>31) validate = false;
		}
        }catch(err){}
	target.removeClass("hasError");
	if(!validate){
		showError("Not a Valid Date , Format - 'yyyy-mm-dd', Example - '2014-01-31'");
		target.addClass("hasError");
	}
	target.focus();
});
$(document).on("change",".contactNumberTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	val = $.trim(val);
	if(val=="") return;
	var result = true;
	var contactNumRegExp = new RegExp(/^((\+[1-9]{1,4}[ \-]*)|(\([0-9]{2,3}\)[ \-]*)|([0-9]{2,4})[ \-]*)*?[0-9]{3,4}?[ \-]*[0-9]{3,4}?$/);
	try{
		result = contactNumRegExp.test(val);
	}catch(err){
		result = false;
	}
	target.removeClass("hasError");
	if(val.length<8 || !result){
		showError("Not a Valid Contact Number , no alphabets allowed and length should be minimum 8 characters!");
		target.addClass("hasError");
		target.focus();
	}
});
$(document).on("change",".numberTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	var numVal = 0;
	try{
		numVal = parseNumericTextValue(val);
	}catch(err){
		numVal = 0;
	}
	target.removeClass("hasError");
	if(val.length>19){
		showError("Not a Valid Number , maximum 19 digits allowed!");
		target.addClass("hasError");
		target.focus();
	}else{
		target.val(numVal);
	}
});
$(document).on("change",".markTextBox,.decimalTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	var numVal = parseMarksTextValue(val);
	target.val(numVal);
});
$(document).on("change",".priceTextBox",function(e){
	var target = $(e.currentTarget);
	var val = target.val();
	var numVal = parseMarksTextValue(val);
	if(numVal){numVal = numVal.toFixed(2);};
	target.val(numVal);
});
function parseMarksTextValue(val){
	var numVal = 0;
	try{
		numVal = parseFloat(val);
	}catch(err){
		numVal = parseNumericTextValue(val);
	}
	if(isNaN(numVal)){
		numVal = 0;
	}
	return numVal;
}
function parseNumericTextValue(val){
	var t = val.match(/(\d+)/gi);var numVal = 0;
	try{
		var numVal = parseInt(t.join(""));
	}catch(err){ numVal=0; }
	return numVal;
}
new function($){
	//leftArrowId,disableClassLeft,rightArrowId,disableClassRight,start,size,max,fetchUrli,otherParams,callBack,noInitialFetch
	$.fn.setForDiscreteScrolling = function(options){
		$(this).each(function(){
			var obj = options; obj.container = this; obj.pagination = false;
			initDiscreteScrolling(obj);
	     	});
	};
	$.fn.setForPagination = function(options){
		$(this).each(function(){
			var obj = options; obj.container = this; obj.pagination = true;
			var lastSize = $(obj.rightArrowId).data('currentCount');
			initDiscreteScrolling(obj);
			if(lastSize && lastSize != (obj.size+obj.start) && !obj.noInitialFetch){
				fetchData(obj,obj.start);
			}else{
				putPaginationDiv(obj);
			}
		});
	};
	var initDiscreteScrolling = function(obj){
		/*$(this).attr('data-fetched-left',obj.start);
		$(this).attr('data-fetched-right',obj.size);*/
		$(obj.leftArrowId).data('currentCount',obj.start).off('click',fillPrev);
		$(obj.rightArrowId).data('currentCount',obj.size).off('click',fillNext);
		if(obj.start<=0){
			$(obj.leftArrowId).addClass(obj.disableClassLeft).on('click',obj,fillPrev);
		}else{
			$(obj.leftArrowId).removeClass(obj.disableClassLeft).on('click',obj,fillPrev);
		}
		if(obj.max<=obj.itemsPerPage){
			$(obj.rightArrowId).addClass(obj.disableClassRight).on('click',obj,fillNext);
		}else{
			$(obj.rightArrowId).removeClass(obj.disableClassRight).on('click',obj,fillNext);
		}
		$(obj.container).find(".commonPaginationDiv").remove();
		obj.paginationDiv = undefined;obj.pageId=0;
		if(obj.pagination){
			$(document).off("click",".commonPaginationEachItem",shiftOnPaginationClick);
			$(document).on("click",".commonPaginationEachItem",obj,shiftOnPaginationClick);
		}
		obj.pageIndex = 0;
	};
	var fillNext = function(e){
		var obj = e.data;var target = e.currentTarget;
		var currCount = $(target).data('currentCount');
		if(currCount < obj.max){
			obj.pageIndex++;
			shiftPageIndexOnNextPrev(obj,obj.pageId+1);
			fetchData(obj,currCount,"next");
		}
	};
	var disableOrEnableArrows = function(direction,obj,startIndex,endIndex){
		if(!direction) return;
		try{
			$(obj.leftArrowId).data('currentCount',startIndex).removeClass(obj.disableClassLeft);
			$(obj.rightArrowId).data('currentCount',endIndex).removeClass(obj.disableClassRight);
		}catch(err){}
		if(endIndex >= obj.max){
			$(obj.rightArrowId).addClass(obj.disableClassRight);
		}else if(startIndex <= 0){
			$(obj.leftArrowId).addClass(obj.disableClassLeft);
		}
	};
	var fillPrev = function(e){
		var obj = e.data;var target = e.currentTarget;
		var currCount = $(target).data('currentCount');
		var start = currCount - obj.size;start = start < 0 ? 0 : start;
		if(currCount > 0){
			obj.pageIndex--;
			shiftPageIndexOnNextPrev(obj,obj.pageId-1);
			fetchData(obj,start,"prev");
		}
	};
	var shiftPageIndexOnNextPrev = function(obj,newPageId){
		if(!obj.paginationDiv || !obj.pagination) return;
		var commonPaginationDiv = $(obj.container).find(".commonPaginationDiv");
		commonPaginationDiv.find(".commonPaginationEachItemSelected").removeClass("commonPaginationEachItemSelected");
		var pageId = $(commonPaginationDiv.find(".commonPaginationEachItem").get(newPageId)).addClass("commonPaginationEachItemSelected").data("pageId");
		obj.pageId = pageId = pageId?parseInt(pageId,10):0;
		obj.paginationDiv = "<div class='left relative commonPaginationDiv'>"+commonPaginationDiv.html()+"</div>";
	};
	var shiftOnPaginationClick = function(e){
		var obj = e.data;
		var commonPaginationDiv = $(obj.container).find(".commonPaginationDiv");
		commonPaginationDiv.find(".commonPaginationEachItemSelected").removeClass("commonPaginationEachItemSelected");
		var pageId = $(e.currentTarget).addClass("commonPaginationEachItemSelected").data("pageId");
		obj.pageId = pageId = pageId?parseInt(pageId,10):0;
		obj.paginationDiv = "<div class='left relative commonPaginationDiv'>"+commonPaginationDiv.html()+"</div>";
		var start = pageId*obj.size;
		obj.pageIndex = obj.pageId;
		fetchData(obj,start,"next");
	};
	var putPaginationDiv = function(obj){
		if(obj.paginationDiv){
			$(obj.container).append(obj.paginationDiv);
			return;
		}
		var count = 0;
		var div = "<div class='left relative commonPaginationDiv'><center>";
		var eachSpan = "<span class='relative commonPaginationEachItem commonPaginationEachItemSelected big14 boldy' data-page-id='0'>1</span>";
		for(var ind=obj.start,count=1;ind<obj.max;ind=count*obj.size,count++){
			div += eachSpan;
			eachSpan = "<span class='relative commonPaginationEachItem big14 boldy' data-page-id='"+count+"'>"+(count+1)+"</span>";
		}
		div += "</center></div>";
		if(count>2){
			obj.paginationDiv = div;
			$(obj.container).append(div);
		}
	};
	var fetchData = function(obj,startIndex,direction){
		if(obj.fetchUrl){
			var params = obj.otherParams?obj.otherParams:new Object();
			params.start = startIndex ; params.size = obj.size;
    			smallLoader(obj.container);
			vReq.get(obj.fetchUrl,params,function(response,stats){
				var endIndex = startIndex+obj.size;
				$(obj.container).html(response);
				if(obj.getMaxLimitFn){
					obj.max = obj.getMaxLimitFn(obj.container);
				}
				if(obj.pagination) putPaginationDiv(obj);
				disableOrEnableArrows(direction,obj,startIndex,endIndex);
				try{ if(obj.callBack) obj.callBack(obj);}catch(err){}
			});
		}
	};
}(jQuery);

function saveBtnCallbackFn(elem,response,stat){
	if(stat=="success" && response.result){
		$(elem).fadeTo(500,0,function(){
			$(this).addClass("nonner");
		});
		$(elem).val("Saved").text("Saved");
	}
}

new function($){
	var cbFn = function(){};
	var state = false;
	$.goFullScreen = function(cbFnIn){
		cbFn = cbFnIn?cbFnIn:function(){};
		setCallbacks();
		var docElm = document.documentElement;
		if (docElm.requestFullscreen) {
    			docElm.requestFullscreen();
		}
		else if (docElm.mozRequestFullScreen) {
    			docElm.mozRequestFullScreen();
		}
		else if (docElm.webkitRequestFullScreen) {
    			docElm.webkitRequestFullScreen();
		}else{
			return false;
		}
		return true;
	};
	$.exitFullScreen = function(){
		unsetCallbacks();
		if (document.exitFullscreen) {
    			document.exitFullscreen();
		}
		else if (document.mozCancelFullScreen) {
    			document.mozCancelFullScreen();
		}
		else if (document.webkitCancelFullScreen) {
    			document.webkitCancelFullScreen();
		}else{
			return false;
		}
		return true;
	};
	function unsetCallbacks(){
		document.removeEventListener("fullscreenchange",fullscreenCb, false);
		document.removeEventListener("mozfullscreenchange",fullscreenCb, false);
		document.removeEventListener("webkitfullscreenchange",fullscreenCb, false);
	};
	function setCallbacks(){
		unsetCallbacks();
		document.addEventListener("fullscreenchange",fullscreenCb, false);
		document.addEventListener("mozfullscreenchange",fullscreenCb, false);
		document.addEventListener("webkitfullscreenchange",fullscreenCb, false);
	};
	function fullscreenCb(e){
		state = (document.fullscreen | document.mozFullScreen | document.webkitIsFullScreen)?true:false;
		try{cbFn(e,state);}catch(err){}
	}
}(jQuery);

function putWrapperForPopup(divId,popupId,wrapperColor){
	closeWrapperForPopup();
	openedPopupId = popupId;
	$(divId).append("<div class='left absolute fullWidth fullHeight' style='background-color:"+wrapperColor+";' id='popupWrapper'></div>");
}
function closeWrapperForPopup(){
	openedPopupId = "";
	$("#popupWrapper").remove();
}
$(document).on("click","#popupWrapper",closePopup_wrapper);
var openedPopupId = "";
function closePopup_wrapper(){
	$(openedPopupId).addClass("nonner");
	closeWrapperForPopup();
}
//function getDiffFromPresent(time){
//	var diff = (new Date()).getTime() - time;
//	var hours = (diff/3600000);
//	if(hours>24){
//		return (hours/24)+" days";
//	}else if(hours<0){
//		return (diff/60000)+" mins";
//	}else{
//		return hours+" hrs";
//	}
//}
var pushIfAbsent = function(arr,val){
	try{
		var index = arr.indexOf(val);
		if(index<0){
			return arr.push(val);
		}else{
			return index;
		}
	}catch(err){return -1;}
}
var removeItemFromArr = function(arr,val){
	try{
		var index = arr.indexOf(val);
		if(index>=0){
			return arr.splice(index,1);
		}else{
			return arr;
		}
	}catch(err){return arr;}
}
new function($){
	$.fn.positionCenter = function(){
		var div = $(this).removeClass("nonner");
		var maxHeight = $(window).innerHeight() - 100;var maxWidth = $(window).innerWidth() - 100;
		div.css({'max-height':maxHeight+"px",'max-width':maxWidth+"px"});
		var left = (maxWidth-div.width())/2;
		left = left<50 ? 50 : left;
		var ttop = (maxHeight-div.height())/2;
		ttop = ttop<50 ? 50 : ttop;
		div.css({'left':left+"px",'top':ttop+"px"});
		return this;
	};
}(jQuery);
String.prototype.replaceAll = String.prototype.replaceAll || function(search,replaceWith){
	var myString = this;
	try{
		var regex = new RegExp(search, 'g');
		myString =  myString.replace(regex,replaceWith);
	}catch(err){}
	return myString;
}
function digitPercisionString(digit,point){
	digit = digit.toString();
	if(digit.length < point){
		var pre = "";
		for(i = digit.length;i<point;i++){
			pre += "0";
		}
		digit = pre + digit;
	}else if(digit.length > point){
		digit = digit.toPrecision(point);
	}
	return digit;
}
new function($){
	Date.locale = {
    	   en: {
       		month_names: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September',
			 'October', 'November', 'December'],
       		month_names_short: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    	   }
	};
	$.fn.formatDate = function(format,lang){
	    lang = lang?lang:"en";
	    $(this).each(function(){
		var dateTime = parseInt(this,10);
		if(!dateTime || dateTime<0){
			format = "";
			return;
		}
		var date = new Date(dateTime);
		format = format.replace("dd",date.getDate());
		format = format.replace("mmm",Date.locale[lang].month_names[date.getMonth()]);
		format = format.replace("mm",digitPercisionString(date.getMonth()+1,2));
		format = format.replace("yyyy",date.getFullYear());
		format = format.replace("yy",date.getYear());
		format = format.replace("hrs",date.getHours());
		format = format.replace("min",digitPercisionString(date.getMinutes(),2));
		format = format.replace("sec",digitPercisionString(date.getSeconds(),2));
	    });
	    return format;
	};
	$.fn.updateClientTime = function(className,format,dt){
		className = className ? className : ".updateClientTime";
		$(this).each(function(){
			$(this).find(className).each(function(){
				var $this = $(this);
				var frm = $this.data("format");
				frm = frm ? frm : (format ? format : "dd/mm/yyyy hrs:min:sec");
				var dtHtml = $([$this.data("time")]).formatDate(frm);
				$this.replaceWith(dtHtml);
			});
		});
	};
	$.fn.dateWiseTime = function(){
		var dt = this;
		try{
			if(this.get(0)){
				dt = this.get(0);
			}
		}catch(er){}
		dt.setHours(0);
        	dt.setMinutes(0);
       		dt.setSeconds(0);
       		dt.setMilliseconds(0);
		return dt;	
	};
}(jQuery);

var allAlphabets = "abcdefghijklmnopqrstuvwxyz";
var timer_circle = {
	currentAngle:0,
	fill:function(angle,timerCircle){
            if(!timerCircle)timerCircle=$("#timer_circle");
		if(angle<=180){
			timerCircle.find(".half_fill_circle_2").removeClass("visible");
			timerCircle.find(".half_fill_circle_1").removeClass("overHalf");
		}else{
			timerCircle.find(".half_fill_circle_2").addClass("visible");
			timerCircle.find(".half_fill_circle_1").addClass("overHalf");
		}
		this.rotate(timerCircle.find(".rotating_half_circle"),angle);
		this.currentAngle = angle;
	},
	rotate:function(div,degree){
		div.css({'-webkit-transform': 'rotate(' + degree + 'deg)',
                        '-moz-transform': 'rotate(' + degree + 'deg)',
                        '-ms-transform': 'rotate(' + degree + 'deg)',
                        '-o-transform': 'rotate(' + degree + 'deg)',
                        'transform': 'rotate(' + degree + 'deg)'});
	}
};
window["toCamelCase"] = function(str){
	if(!str) return "";
	var strOut = str.charAt(0).toUpperCase();
	var strOut2 = str.substr(1).toLowerCase();
	strOut2 = strOut2?strOut2.toLowerCase():"";
	strOut = strOut +strOut2;
	return strOut;
};
//var doLimitedScroll=function(){
//	return;
//	var wTop = $(window).scrollTop();
//	var wHeight = $(window).innerHeight();
//	$(".limitedScroll").each(function(){
//		var iTop = $(this).offset()['top'];
//		var initialTop = $(this).data("initialTop");
//		var newTop = wTop;
//		if(!initialTop){
//			$(this).data("initialTop",iTop);
//			initialTop = iTop;
//		}
//		var iHeight = $(this).height();
//		var hDiff = iHeight - wHeight;
//		if(hDiff>0){
//			newTop -= hDiff;  
//		}
//		if(wTop>initialTop){
//			newTop -= initialTop;  
//			$(this).animate({'top':newTop},50);
//		}else{
//			$(this).css({'top':0});
//		}
//	});
//}
//$(window).scroll(function(){
//	doLimitedScroll();
//});
//$(window).resize(function(){
//	doLimitedScroll();
//});
function showDiagramPreview(imghtml,imgTitle,buttonText){
	showTopLoader();
	var previewPopup = $("#diagramPreview");
	previewPopup.find(".imgPreviewTitle").text(imgTitle);
	if(buttonText){
        	previewPopup.find(".addDiagAdd").removeClass("nonner").data("imghtml",imghtml).text(buttonText);
	}else{
        	previewPopup.find(".addDiagAdd").addClass("nonner");
	}
        var previewSpan=previewPopup.find(".diagramPreviewImg");
	$("#errorPopupBlackOut").show().css("z-index",previewPopup.css("z-index")-1);
	
	previewSpan.html(imghtml);
        previewSpan.find("img").load(function(){
		previewPopup.positionCenter();
		hideTopLoader();
	}).error(function(){
		$(this).closest("#diagramPreview").addClass("nonner");
		$("#errorPopupBlackOut").hide();
		hideTopLoader();
	});
	return previewPopup;
}
function showImagePreview(imghtml,imgTitle){
	var previewPopup = $("#imagePreview");
	if(imgTitle){
		previewPopup.find(".imgPreviddewTitle").text(imgTitle);
	}
	var popup = showVPopup(0.3);
	popup.html(previewPopup.html());
        var previewSpan=popup.find(".imagePreviewImg").fadeTo(0,0);
	previewSpan.html(imghtml);
	var loader = popup.find(".imagePreviewLoading div");
	bigLoader(loader);
        previewSpan.find("img").load(function(){
		loader.parent().remove();
		var maxWidth = window.innerWidth-140;
		var maxHeight = window.innerHeight-140;
		var $this = $(this);
		var imgHeight = $this.outerHeight(true);
		var imgWidth = $this.outerWidth(true);
		$this.data("height",imgHeight).data("width",imgWidth);
		var minWidth = imgWidth<maxWidth?imgWidth:maxWidth;
		minWidth = minWidth>300?minWidth:300;
		var minHeight = imgHeight<maxHeight?imgHeight:maxHeight;
		previewSpan.css({
			"max-width":maxWidth+'px',
			"max-height":maxHeight+'px',
			"min-width":minWidth+'px',
			"min-height":minHeight+'px'
		}).fadeTo(200,1);
	}).error(function(){
		loader.text("Failed to load Image!").addClass("redTextColor");
		hideTopLoader();
	});
	popup.on('change','.imgPreviewZoomSel',function(){
		var val = $(this).val();
		var img = previewSpan.find("img");
		var width = img.data("width")*val/100;
		var height = img.data("height")*val/100;
		img.css({"width":width+'px',"height":height+'px'});
	});
	return previewPopup;
}
$(document).on("click",".closeDiagPreview",function(e){
	$(this).closest("#diagramPreview").addClass("nonner");
	$("#errorPopupBlackOut").hide();
});
$(document).on("click",".goBackInHistory",function(){
	window.history.back();
});
new function($){
	$(document).on("mousedown",".virNumericKeyboard .VNKeys",function(){
		$(this).addClass("downKey");
	}).on("mouseup",".virNumericKeyboard .VNKeys",function(){
		$(this).removeClass("downKey");
	}).on("click",".virNumericKeyboard .VNKeys",function(){
		var holder = $(this).closest(".virNumericKeyboard");
		var triggerClass = holder.data("inputBoxClass");
		var val=$(this).data("val");
		if(val=="<"){
			var extVal = $(triggerClass).val()||$(triggerClass).text();
			extVal = extVal.toString();
			var cPos = $(triggerClass).getCursorPosition();
			var newVal = extVal.slice(0,cPos-1)+extVal.slice(cPos);
			$(triggerClass).text(newVal).val(newVal);
		}else if(val!=undefined){
			var extVal = $(triggerClass).val()||$(triggerClass).text();
			extVal = extVal.toString();
			if(val=="." && extVal.indexOf(".")>=0){
				return;
			}
			var newVal = extVal+val.toString();
			if(val == "-" && val!=newVal){
				return;
			}
			$(triggerClass).text(newVal).val(newVal);
		}else{
			$(triggerClass).text("").val("");
		}
	});
}(jQuery);
var vedantuClient = new function(window){
	this.getInfo = function(){
  		var N= navigator.appName, ua= navigator.userAgent, tem;
  		var M= ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
  		if(M && (tem= ua.match(/version\/([\.\d]+)/i))!= null){
		 	M[2]= tem[1];
		}
  		M= M? [M[1], M[2]]: [N, navigator.appVersion,'-?'];
		return M;
	};
	var getReqInfo = function(){
		var M = vedantuClient.getInfo();
		M[0] = M[0].toUpperCase();
		try{
			M[1] = parseInt(M[1]);
		}catch(err){
			M[1] = 0;
		}
		M[2] = M[1];
  		return M;
	};
	var getBrName = function(brNm){
		brNm = brNm.toUpperCase();
		brNm = brNm == "MSIE" ? "Internet Explorer" : toCamelCase(brNm);
		return brNm;
	};
	var testSupportedBrowsers = {
		"CHROME":22,
		"FIREFOX":17,
		"SAFARI":5,
		"OPERA":10,
		"MSIE":10
	};
	var bestSupportedBrowsers = {
		"CHROME":22,
		"FIREFOX":17,
		"SAFARI":5,
		"OPERA":10,
		"MSIE":10
	};
	var minSupportedBrowsers = {
		"CHROME":18,
		"FIREFOX":15,
		"SAFARI":4,
		"OPERA":8,
		"MSIE":9
	};
	var dMsg = "You are using %1 Browser version = %2";
	var recBrowserList="";
	for(br in bestSupportedBrowsers){
		recBrowserList += getBrName(br)+" version : "+bestSupportedBrowsers[br]+"+ ,";
	}
	var recBrowser = "We recommend to use the latest/updated version of "+recBrowserList;
	var recBrowserPara = "<p>"+recBrowser+"</p>";
	var getChromeLink = "<a target='_blank' href='http://www.google.com/chrome'>Get Google Chrome Web-Browser here</a>";
	var warnMsg = "<p class='boldy big14'>It seems that you are using an older version of a browser</p>"+recBrowserPara+getChromeLink;
	var errorMsg = "<p class='boldy big14 redTextColor'>Sorry, it seems that you are using an older version of a browser,which we does not support anymore.</p>"+recBrowserPara+getChromeLink;
	var testMsg = "Sorry, it seems that you are using an older version of a browser,which we does not support for taking %s ."+recBrowser;
	
	this.verifyClient = function(div){
		var m = getReqInfo();
		var ver = m[1];
		var bestVer = bestSupportedBrowsers[m[0]];
		var minVer = minSupportedBrowsers[m[0]];
		if(ver >= bestVer){
			$(div).hide();
			return "";
		}else if(ver >= minVer){
			$(div).show().html(warnMsg);
			return warnMsg;
		}else{
			$(div).show().html(errorMsg);
			return errorMsg;
		}
	};
	this.detectedClientMsg = function(){
		var m = this.getInfo();
		var msg = dMsg.replace("%1",getBrName(m[0]));
		msg = msg.replace("%2",m[1]);
		return msg;
	}
	this.verifyClientForTest = function(type){
		type = type?type:"test";
		var m = getReqInfo();
		var ver = m[1];
		var curVer = testSupportedBrowsers[m[0]];
		if(ver >= curVer){
			return "";
		}else{
			return testMsg.replace("%s",type);	
		}
	};
};
var vtooltip = new function(){
	var timeoutObj;
	var extClasses;
	this.show = function(e,innerHtml,cbFn,offSetPlus,classes){
		if(timeoutObj) clearTimeout(timeoutObj);
		timeoutObj = setTimeout(function(){
			show(e,innerHtml,offSetPlus,classes);
			if(cbFn){
				try{cbFn(true);}catch(err){}
			}
		},250);
	}; 
	var show = function(e,innerHtml,offSetPlus,classes){
		var $this = $(e.currentTarget);
		var offset = $this.offset();
		var x = offset.left - $(window).scrollLeft();
		var y = offset.top - $(window).scrollTop();
		var div = $("#tooltipContainer");
		innerHtml = innerHtml && $.trim(innerHtml).length>0? innerHtml : "<div>NA</div>";
		var body = div.find(".tooltipBody").html(innerHtml);
		if(classes){
			extClasses = classes;
			div.addClass(extClasses);
		}
		div.fadeTo(120,1);
		var width = div.width();
		var height = div.height();
		var thisWidth = $this.width();
		offSetPlus = offSetPlus?offSetPlus:0;
		var left = (x + 2 + offSetPlus + (thisWidth/2)) - (width/2);
		left = left<0?0:left;
		//var poniterLeft = (x+offSetPlus) - left - 3;
		var poniterLeft = width/2 - 5;
		poniterLeft = poniterLeft<0?left-x:poniterLeft;
		var ttop = y - height - 8;
		div.find(".tooltipPointer").addClass("nonner").css("margin-left",(poniterLeft)+"px");
		if(ttop<0){
			ttop = y + $(e.currentTarget).height() + 8;
			div.find(".tooltipPointerUp").removeClass("nonner");
		}else{
			div.find(".tooltipPointerDown").removeClass("nonner");
		}
		div.css({left:left,top:ttop});
	};
	this.hide = function(){
		var div = $("#tooltipContainer");
		var body = div.fadeTo(10,0).hide().find(".tooltipBody").html("");
		if(extClasses){
			div.removeClass(extClasses);
			extClasses = undefined;
		}
		if(timeoutObj) clearTimeout(timeoutObj);
	};
}
function validateEmail(s){
 if (s.length >0) {
  var i=s.indexOf("@"),j=s.indexOf(".",i), k=s.indexOf(","), kk=s.indexOf(" "), jj=s.lastIndexOf(".")+1,
   len=s.length;
  if ((i>0) && (j>(i+1)) && (k==-1) && (kk==-1) && (len-jj >=2) && (len-jj<=4)) {
            return true;
  }
  else {
    return false;
  }

 }
 else return false;
 }
function appendCSSStyle(css){
	var style = document.createElement("style");
	style.type = "text/css";
	$(style).html(css);
	$("head").append(style);
}
/* PAGE FULL WIDTH */
$(function(){
	function setFullWidth(){
		var fullWidth = $(window).width();
		fullWidth = fullWidth < 1200 ? 1200 : fullWidth;
		$(".pageFullWidth").width(fullWidth);
		appendCSSStyle(".pageFullWidth{ width:"+fullWidth+"px;}");
	}
	$(window).resize(function(){
		setFullWidth();
	});
	setFullWidth();
});
/*PAGE FULL WIDTH END*/
new function($){
	$.fn.startProgressBar = function(){
	    $(this).each(function(){	   
		var $this = $(this);
		$this.data("progressPercent",0);
		var bar = $this.find('.progressBar');
		var thumb = bar.find('.progressDone').addClass('nonner').width(0);
		var pre = bar.find('.preProgress').removeClass('nonner');
		var count = $this.find('.progressCount').text("Initializing");
		var barWidth = bar.width();
		var preWidth = pre.width();
		var intMiliSec = barWidth / preWidth * 250;
		function animatePre(me,maxLeft){
			if(!me.hasClass('nonner')){
				me.animate({"margin-left":maxLeft+'px'},intMiliSec,function(){
					me.css("margin-left",(0-me.width())+"px");
					animatePre(me,maxLeft);
				});
			}
		};
		animatePre(pre,barWidth);
	    });
	};
	$.fn.updateProgressBar = function(percent){
	    $(this).each(function(){
		var $this = $(this);
		$this.data("progressPercent",percent);
		var bar = $this.find('.progressBar');
		var thumb = bar.find('.progressDone').removeClass('nonner').width(percent+"%");
		var pre = bar.find('.preProgress').addClass('nonner');
		var count = $this.find('.progressCount').text(percent+"%");
	    });
	};
}(jQuery);

new function($){
	var temp = document.createElement("span");
	temp = $(temp);
	$.fn.elipsifyText = function(classes){
	   var ret = false;
	   $(this).each(function(){
		var $this = $(this);
		var parWidth = $this.parent().width();
		temp.html($this.html());
		if(classes){temp.addClass(classes);}
		temp.appendTo("body");
		if(temp.width()>parWidth){
			$this.addClass("singleLineText elipsifiedText");
			$this.after("<span class='openCloseElipsified'>Show More</span>");
			ret = true;
		}
		if(classes){temp.removeClass(classes);}
		temp.remove();
	    });
	    return ret;
	};
	$(document).on("click",".openCloseElipsified",function(){
		var $this = $(this);
		var div = $this.siblings(".elipsifiedText");
		div.toggleClass("singleLineText");
		if(div.hasClass("singleLineText")){
			$this.text("Show More");
		}else{
			$this.text("Collapse");
		}
	});
	String.prototype.contains = function(it) { 
		return (this.indexOf(it) != -1) ? true : false; 
	};
}(jQuery);
var defineFixProp = function(obj,prop,val){
	Object.defineProperty(obj, prop, {
        	enumerable: false,
            configurable: true,
        	writable: false,
        	value : val
        });
}
new function($){
	var titleToolTip = function(e){
        	var title = $(this).data("title");
        	toolTipFn(e,function(){
                	return title;
        	},0);
        	e.preventDefault();
	};
	var toolTipXHR;
	var toolTipFn = function(e,cb,offsetPlus,extraClassName){
        	switch(e.type){
                	case "mouseenter":vtooltip.show(e,cb(),null,offsetPlus,extraClassName);
                        	break;
                	case "mouseleave":clearToolTipXHR();
                                          toolTipXHR = undefined;
                                          vtooltip.hide();
                        	break;
        	};	
        	if(e) e.preventDefault();
	};
	var clearToolTipXHR = function(){
		if(toolTipXHR){
                        toolTipXHR.abort();
                        toolTipXHR = undefined;
                }
	};
	defineFixProp(window,"toolTipFn",toolTipFn);
	defineFixProp(window,"clearToolTipXHR",clearToolTipXHR);
	defineFixProp(window,"setToolTipXHR",function(xhr){
		toolTipXHR = xhr;
	});
	defineFixProp(window,"checkToolTipXHR",function(){
		if(toolTipXHR) return true;
	});
	$(document).on("mouseenter mouseleave",".toolTipTitle",titleToolTip);
}(jQuery);
function putParamsInUrl(url,params){
	if(typeof params == "object"){
		for(var p in params){
			var joinChar = url.contains("?") ? "&" : "?";
                        url += joinChar + p + "=" + encodeURIComponent(params[p]);
		};
	}
	return url;
}
var serializeJson = new function(){
	this.encode = function(obj){
		var retStr = "";
		if(typeof obj == "object"){
			try{
				retStr = escape(JSON.stringify(obj));
			}catch(err){}
		}
		return retStr;
	};
	this.decode = function(str){
		var obj = {};
		if(typeof str == "string"){
		   try{
			obj = JSON.parse(unescape(str));
		   }catch(err){}
		}
		return obj;
	};
};
