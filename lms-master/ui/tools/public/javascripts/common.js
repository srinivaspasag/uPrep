function validEmail(s) {
    if (s.length > 0) {
        var i = s.indexOf("@"), j = s.indexOf(".", i), k = s.indexOf(","), kk = s
        .indexOf(" "), jj = s.lastIndexOf(".") + 1, len = s.length;
        if ((i > 0) && (j > (i + 1)) && (k == -1) && (kk == -1)
            && (len - jj >= 2) && (len - jj <= 3)) {
            return true;
        } else {
            return false;
        }

    } else
        return false;
}
$(function(){
	$(".webSummitLogo").click(function(e){
	   try{
		var target = e.target ? e.target : e.toElement;
		if(target.nodeName != "A"){
			window.open($(this).data("href"), '_blank');
		}
	    }catch(err){}
	});
});
function refreshCaptcha(par){
	par = $(par);
        var d=new Date();
        par.find("img").attr("src",'/application/captcha?id='+par.find(".randomId").val()+'&ts='+d.getTime());
	par.find("input.code").val("");
}
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
                showError("Not a Valid Contact Number , no alphabets allowed and length should be minimum 8 characters!",function(){
                	target.addClass("hasError");
                	target.focus();
		});
        }
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
                showError("Not a Valid Email Domain , If your Email-Id is 'someone@gmail.com', then your email domain is 'gmail.com' ",function(){
                	target.addClass("hasError");
                	target.focus();
		});
        }
        target.focus();
});
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
var defineFixProp = function(obj,prop,val){
	Object.defineProperty(obj, prop, {
        	enumerable: false,
        	configurable: false,
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
                	case "mouseenter":
				vtooltip.show(e,cb(),null,offsetPlus,extraClassName);
				$(window).on("scroll.tooltip",hide);
                        	break;
                	case "mouseleave":hide();
                        	break;
        	};	
        	if(e) e.preventDefault();
	};
	var hide = function(){
		$(window).off("scroll.tooltip");
		clearToolTipXHR();
		toolTipXHR = undefined;
		vtooltip.hide();
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
function refreshCaptcha(par) {
    par = $(par);
    var d = new Date();
    par.find("img").attr("src", '/application/captcha?id=' + par.find(".randomId").val() + '&ts=' + d.getTime());
    par.find("input.code").val("");
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
