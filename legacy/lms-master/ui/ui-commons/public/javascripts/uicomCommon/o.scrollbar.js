(function($){
	$.fn.useScrollBar = function(options,scrollTypes){
	scrollTypes = scrollTypes?scrollTypes:{};
	options = options?options:scrollBar.defaultOptions;
    	this.each(function(){
        	var index;
        	if($(this).attr("scrollBarAppend")!="done"){
			index = scrollBarArr.length;
			scrollBarArr[index] = new scrollBarObj(this,index,options,scrollTypes);
		}else{
			index = $(this).attr("scrollBarIndex");
			scrollBarArr[index].resetPosition();
			scrollBarArr[index].decide(this);
		}
		var id = "#"+scrollBarArr[index].id;
		$(id).find('.scrollBar').width(options.width);
		$(id).find('.scrollBarHorizon').height(options.width);
	    	$(id).find('.scrollRail,.scrollRailHorizon').css(options.cssTrack);
	    	$(id).find('.scrollThumb,.scrollThumbHorizon').css(options.cssThumb);
	    	if(options.imgThumb){
		    $(id+" .thumbImg").attr("src",options.imgThumb);	
		    $(id+" .thumbImg").show();
	    	}  
	    	if(options.imgTrack){
		    $(id+" .railImg").attr("src",options.imgTrack);
		    $(id+" .railImg").show();
	    	}
		if(options.noKeyNav){
			scrollBarArr[index].keydown = function(){};
		}
    	});
	return this;
    	};
	$.fn.unuseScrollBar = function(){
    		this.each(function(){
			$this=$(this);
			$this.removeAttr("scrollBarAppend").removeAttr("scrollBarIndex");
			$this.html($this.find("textBody").html());
		});
		return this;
	};
    $.fn.scrollBarExt = function(method,param){
	this.each(function(){
		var index = parseInt($(this).attr("scrollBarIndex"));
		var Obj = $(scrollBarArr).get(index);
		if(!Obj) return false;
		//if(!Obj.on) return false;
		switch(method){
		case "update" :	Obj.decide(this);break;
		case "pageDown" :if(!Obj.on) return false; 
				return Obj.pageDown(param);
		case "moveBy" : if(!Obj.on) return false;
				return Obj.moveBy(param);
		}
	    });
	    return this;
	};
	var scrollBarArr = new Array();
    var scrollBarObj = function(caller,index,options,scrollTypes){
        this.index = index;
	this.me;
	this.vert = scrollTypes.vertical!=undefined?scrollTypes.vertical:true;
	this.horizon = scrollTypes.horizontal!=undefined?scrollTypes.horizontal:false;
        this.fadeValue = options.disableAlphaValue!=undefined?options.disableAlphaValue:1;
	    this.focused = false;
	    this.on = false;
	    this.div = {
		    scrollBar : 0,
		    container : 0,
		    textBody : 0,
		    thumb : 0,
		    track : 0,
		    my : 0
	    };
	    this.height = {total : 0,text : 0,thumb:0};
	    this.width = {total : 0,text : 0,thumb:0};
	    this.position = {thumb:0,text:0},
	    this.positionHorizon = {thumb:0,text:0},
	    this.drag = {startPosY : 0,maxTop : 0,startPosX: 0,maxLeft: 0};
        /*for(item in scrollBar){
                this[item] = scrollBar[item];
        };*/
		$.extend(this,scrollBar);
        this.id = this.init(caller,options);
        return this;
    };
    var scrollBar = {
        mouseupEvent:"mouseup.scrollBar",
        mousedownEvent:"mousedown.scrollBar",
        mousemoveEvent:"mousemove.scrollBar",
        keydownEvent:"keydown.scrollBar",
	defaultOptions:{
                        width:8,
        		imgTrack:"", 
	                imgThumb:"/public/images/common/test-scroll.png",
                        cssTrack:"",//{"background-color":"#D8EEFD"},
                        cssThumb:"",//{"background-color":"#003D5D","border":"2px solid #BDBDBD","border-radius":"10px"},
                        disableAlphaValue:0.5,
                        noKeyNav:false
        },
	init : function(caller,options){
        	var scrollParent = $('#scrollParent').clone();
        	var id = "scrollParent" + this.index;
        	$(scrollParent).attr("id",id);
		this.me = $(scrollParent);
		
		this.div.scrollBar = this.me.find('.scrollBar');
		this.div.thumb = this.div.scrollBar.find('.scrollThumb');
		this.div.track = this.div.scrollBar.find('.scrollRail');
		
		this.div.scrollBarHorizon = this.me.find('.scrollBarHorizon');
		this.div.thumbHorizon = this.div.scrollBarHorizon.find('.scrollThumbHorizon');
		this.div.trackHorizon = this.div.scrollBarHorizon.find('.scrollRailHorizon');
		
		this.div.container = this.me.find('.container');
		this.div.textBody = this.div.container.find('.textBody');
		
		$(this.div.textBody).append($(caller).html());
		$(caller).html("");
		$(caller).append($(scrollParent));
		$("#"+id).show();
		$(caller).attr("scrollBarAppend","done");
		$(caller).attr("scrollBarIndex",this.index);
		//$(window).resize(this.decide);
		
		this.decide(caller,options);
        return id;
   	},
	fadeTimeoutObj : undefined,
	disableThumb : function(){
		if(this.donotDisableThumb) return;
        	$(this.div.track).fadeTo(200,this.fadeValue);
        	$(this.div.trackHorizon).fadeTo(200,this.fadeValue);
	},
	enableThumb : function(d){
		if(this.fadeTimeoutObj) clearTimeout(this.fadeTimeoutObj);
		this.donotDisableThumb = d; 
        	$(this.div.track).fadeTo(0,1);
        	$(this.div.trackHorizon).fadeTo(0,1);
	},
	timedEnabledThumb : function(){
		this.enableThumb(true);
        	var myObj = this;
		this.fadeTimeoutObj = setTimeout(function(){
                myObj.donotDisableThumb = false;
                myObj.disableThumb();
        },1000);
	},
	decide : function(caller,options){
		$(this.me).height($(caller).height());//.width($(caller).width());
		if($(this.div.textBody).height()<=20){
			$(this.div.textBody).height($(this.div.textBody).children().height());
		}
		if($(this.div.textBody).width()<=20){
			$(this.div.textBody).width($(this.div.textBody).children().width());
		}
		this.vert = this.heightCalc() && this.vert;
		this.horizon = this.widthCalc() && this.horizon;
		if(this.vert||this.horizon){
			this.on = true;
			if(this.vert){
				$(this.div.scrollBar).show();
				$(this.div.track).fadeTo(0,this.fadeValue);
				this.thumbCalcVert();
			}
			if(this.horizon){
				$(this.div.scrollBarHorizon).show();
				$(this.div.trackHorizon).fadeTo(0,this.fadeValue);
				this.thumbCalcHorizon();
				$(this.div.container).height(this.height.total);
				$(this.me).height(this.height.total+options.width);
			}
			//$(this.div.container).css({"width":"98%","right":"2%"});
			if(!this.registrationDone){
				if(this.vert){
					$(this.me).on('mousewheel DOMMouseScroll',this,this.handleWheel);
				}
				$(this.me).on("mouseenter",this,function(e){
               				var myObj = e.data;
			   		myObj.enableThumb();
			   		//myObj.focused = true;
				});
				$(document).on("click","#scrollParent"+this.index+" .container",this,function(e){
               				var myObj = e.data;
			   		//myObj.focused = true;
			   		$(document).on(scrollBar.keydownEvent,myObj,myObj.keydown);
			   		return true;
				}).on("click",this,function(e){
					var myObj = e.data;
			   		$(document).off(scrollBar.keydownEvent,myObj,myObj.keydown);
			   		return true;
				});
				$(this.me).on("mouseleave",this,function(e){
                			var myObj = e.data;
                                    myObj.disableThumb();
            			});
				$(this.div.track).on("click",this,this.handleClickVert);
				$(this.div.thumb).on(scrollBar.mousedownEvent,this,this.startDragVert);
				$(this.div.trackHorizon).on("click",this,this.handleClickHorizon);
				$(this.div.thumbHorizon).on(scrollBar.mousedownEvent,this,this.startDragHorizon);
				this.decideBrowser();
				this.registrationDone = true;
			}
			return true;
		}else{
			$(this.div.scrollBar).hide();
			$(this.div.scrollBarHorizon).hide();
			//$(this.div.container).css({"width":"100%","right":"0%"});
			$(this.me).height($(this.div.textBody).children().height());//.width($(caller).width());
			return false;
		}
	},
	decideBrowser : function(){
			var browserName = "";
			try{
				browserName = vedantuClient.getInfo()[0];
				browserName = browserName.toUpperCase();
			}catch(err){
				browserName = "";
			}
			if(browserName === "FIREFOX"){
				this.getDelta = this.getDeltaMozilla;
			}else if(browserName === "MSIE"){
				this.getDelta = this.getDeltaIE;
			}else{
				this.getDelta = this.getDeltaOthers;
			}
	},
	thumbCalcVert : function(){
		var thumbHeight = ((this.height.total/this.height.text)*100)+"%";
		$(this.div.thumb).height(thumbHeight);
		this.height.thumb = $(this.div.thumb).height();
		this.drag.maxTop = this.height.total - this.height.thumb;
	},
	thumbCalcHorizon : function(){
		var thumbWidth = ((this.width.total/this.width.text)*100)+"%";
		$(this.div.thumbHorizon).width(thumbWidth);
		this.width.thumb = $(this.div.thumbHorizon).width();
		this.drag.maxLeft = this.width.total - this.width.thumb;
	},
	heightCalc : function(){
		this.height.total = $(this.div.container).height();
		this.height.text = $(this.div.textBody).height();
		this.height.thumb = $(this.div.thumb).height();
		//var scrollBarHeight = $(this.div.scrollBar).height();
		return (this.height.text > this.height.total);
		//console.log("total = "+this.height.total+",text = "+this.height.text+",scrollbar = "+scrollBarHeight);
	},
	widthCalc : function(){
		this.width.total = $(this.div.container).width();
		this.width.text = $(this.div.textBody).width();
		this.width.thumb = $(this.div.thumbHorizon).width();
		//var scrollBarWidth = $(this.div.scrollBarHorizon).width();
		return (this.width.text > this.width.total);
		//console.log("total = "+this.height.total+",text = "+this.height.text+",scrollbar = "+scrollBarHeight);
	},
	getDelta : function(){},
	getDeltaMozilla : function(e){
		return (e.detail*-12);
	},
	getDeltaOthers : function(e){
		return Math.floor(parseInt(e.wheelDeltaY/3));
	},
	getDeltaIE : function(e){
		return Math.floor(parseInt(e.wheelDelta/3));
	},
	handleClickVert : function(e){
        	var myObj = e.data;
		var thumbStartPos = $(myObj.div.thumb).position().top;
		var thubmEndPos = thumbStartPos + myObj.height.thumb;
		var diff = 0;
		var y = e.offsetY?e.offsetY:e.originalEvent.layerY;
		if(y > thubmEndPos){
			diff = y - thubmEndPos;	
		}else if(y < thumbStartPos){
			diff = y - thumbStartPos;
		}else{
			return true;
		}
		var nTop = thumbStartPos + diff;
		$(myObj.div.thumb).css("top",nTop);
		myObj.positionText("top",nTop);
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	handleClickHorizon : function(e){
        	var myObj = e.data;
		var thumbStartPos = $(myObj.div.thumbHorizon).position().left;
		var thubmEndPos = thumbStartPos + myObj.width.thumb;
		var diff = 0;
		var x = e.offsetX?e.offsetX:e.originalEvent.layerX;
		if(x > thubmEndPos){
			diff = x - thubmEndPos;	
		}else if(x < thumbStartPos){
			diff = x - thumbStartPos;
		}else{
			return true;
		}
		var left = thumbStartPos + diff;
		$(myObj.div.thumb).css("left",left);
		myObj.positionText("left",left);
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	handleWheel : function(e){
        	var myObj = e.data;
		var deltaY = myObj.getDelta(e.originalEvent);
		var ret = myObj.moveText("top",deltaY);
		if(ret & e){e.preventDefault();}
		return !ret;
	},
	moveText : function(direction,delta){
		var fn = {
		  "top":function(myObj,deltaY){
			//console.log("Scroll Is Happening");
			var retVal = true;
			var lastVal = $(myObj.div.textBody).position();
			lastVal = lastVal.top;
			var minTop = myObj.height.total - myObj.height.text;
			var nTop = lastVal+deltaY;
			//console.log(lastVal+" + "+deltaY);
			if(nTop > 0){
				nTop = 0;
				retVal = false;
			}
			if(nTop < minTop){
				nTop = minTop;
				retVal = false;
			}	
			$(myObj.div.textBody).css("top",nTop);
			myObj.position.text = nTop;
			myObj.positionThumb("top",nTop);
			return retVal;
		    },
		    "left" : function(myObj,deltaX){
			//console.log("Scroll Is Happening");
			var retVal = true;
			var lastVal = $(myObj.div.textBody).position();
			lastVal = lastVal.left;
			var minLeft = myObj.width.total - myObj.width.text;
			var nLeft = lastVal+deltaX;
			//console.log(lastVal+" + "+deltaY);
			if(nLeft > 0){
				nLeft = 0;
				retVal = false;
			}
			if(nLeft < minLeft){
				nLeft = minLeft;
				retVal = false;
			}	
			$(myObj.div.textBody).css("left",nLeft);
			myObj.position.text = nLeft;
			myObj.positionThumb("left",nLeft);
			return retVal;
		     }
		};
		return fn[direction](this,delta);
	},
	positionThumb : function(direction,val){
	   var fn = {
		"top": function(myObj,nTop){
			var thumbTop = ((nTop*-1) / myObj.height.text)*100;
			$(myObj.div.thumb).css("top",(thumbTop+"%"));
		},
		"left": function(myObj,left){
			var thumbLeft = ((left*-1) / myObj.width.text)*100;
			$(myObj.div.thumbHorizon).css("left",(thumbLeft+"%"));
		}
	    };
	    fn[direction](this,val);
	},
	positionText : function(direction,val){
	    var fn = {	
		"top" : function(myObj,nTop){
			var textTop = (nTop / myObj.height.total) * myObj.height.text;
			textTop *= -1;	
			$(myObj.div.textBody).css("top",textTop);
			myObj.position.text = textTop;
		},
		"left" : function(myObj,left){
			var textLeft = (left / myObj.width.total) * myObj.width.text;
			textLeft *= -1;	
			$(myObj.div.textBody).css("left",textLeft);
			myObj.positionHorizon.text = textLeft;
		}
	    };
	    fn[direction](this,val);
	},
	startDragVert : function(e){
        	var myObj = e.data;
		myObj.drag.clickPosY = e.pageY; 
		myObj.drag.startPosY = ($(myObj.div.thumb).position()).top;
		$(window.document).on(scrollBar.mousemoveEvent,myObj,myObj.doDragVert);
		$(window.document).on(scrollBar.mouseupEvent,myObj,myObj.endDrag);
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	doDragVert : function(e){
        	var myObj = e.data;
		var diff = e.pageY - myObj.drag.clickPosY;
		var nTop = myObj.drag.startPosY + diff;
		//console.log("Top = "+nTop+", diff = "+diff);
		if(nTop < 0){
			nTop = 0;
		}else if(nTop > myObj.drag.maxTop){
			nTop = myObj.drag.maxTop;
		}
		$(myObj.div.thumb).css("top",nTop);
		myObj.positionText("top",nTop);
		myObj.timedEnabledThumb();
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	startDragHorizon : function(e){
        	var myObj = e.data;
		myObj.drag.clickPosX = e.pageX; 
		myObj.drag.startPosX = ($(myObj.div.thumb).position()).left;
		$(window.document).on(scrollBar.mousemoveEvent,myObj,myObj.doDragHorizon);
		$(window.document).on(scrollBar.mouseupEvent,myObj,myObj.endDrag);
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	doDragHorizon : function(e){
        	var myObj = e.data;
		var diff = e.pageX - myObj.drag.clickPosX;
		var left = myObj.drag.startPosX + diff;
		//console.log("Top = "+nTop+", diff = "+diff);
		if(left < 0){
			left = 0;
		}else if(left > myObj.drag.maxLeft){
			left = myObj.drag.maxLeft;
		}
		$(myObj.div.thumbHorizon).css("left",left);
		myObj.positionText("left",left);
		myObj.timedEnabledThumb();
		try{ e.preventDefault();}catch(err){}
		return false;
	},
	endDrag : function(e){
		var myObj = e.data;
		$(window.document).off(scrollBar.mousemoveEvent,myObj.doDragVert);
		$(window.document).off(scrollBar.mousemoveEvent,myObj.doDragHorizon);
		$(window.document).off(scrollBar.mouseupEvent,myObj.endDrag);
	},
	pageDown:function(){
		var val = -1 * this.height.total;
		return this.moveText("top",val);
	},
	pageUp:function(){
		var val = this.height.total;
		return this.moveText("top",val);
	},
	moveBy:function(value){
		var ret = this.moveText("top",value);
		if(ret){this.timedEnabledThumb();}
		return ret;
	},
	keydown : function(e){
        var myObj = e.data;
		var keycode = e.which;
		var val = 0;
		var direction = "top";
		switch(keycode){
			case 33: val = myObj.height.total;
			break;
			case 38 : val = 20;
			break;
			case 34 : val = -1 * myObj.height.total;
			break;
			case 40 : val = -20;
			break;
			case 37 : val = 20;
				direction = "left";
			break;
			case 39 : val = -20;
				direction = "left";
			break;
			default: return true;
		}
		var ret = myObj.moveText(direction,val);
		if(ret && e){
			myObj.timedEnabledThumb();
			e.preventDefault();
		}
	},
	resetPosition : function(){
		$(this.div.thumb).css("top","0px");
		$(this.div.textBody).css("top","0px");
		}
    };
})(jQuery);
