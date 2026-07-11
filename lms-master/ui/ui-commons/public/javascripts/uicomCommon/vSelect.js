(function($){
$.fn.useVSelect = function(params){
	$(this).addClass("vselectHide");
	$(this).html($("#vSelect_parent").html());
	var value = vSelectFns.setNewId(this);
	var obj = {id:value.id,index:value.index,params:params};
	var sBarOptions = params?params.scrollBarOptions:"";
	vSelectFns.fill(value.id,params);
	var eachItems = $(this).find(".vSelectList .vSelectEach");
	vSelectFns.applyScrollBar($(this).find(".vSelectListHolder"),eachItems,sBarOptions);
	vSelectFns.registerEventCB(value.id,obj,params.selected);
};
$.fn.updateVSelectTag = function(params){
	var sBarOptions = params?params.scrollBarOptions:"";
	this.each(function(){
		var eachItems = $(this).find(".vSelectList .vSelectEach");
		var holder = $(this).find(".vSelectListHolder").addClass("vSelectListShow");
		vSelectFns.applyScrollBar(holder,eachItems,sBarOptions);
		holder.removeClass("vSelectListShow");
	});
}
/* Options Example
	* {'newVal':{'text':'All Sections','val':'v'},
	*  'optionsList':"<span class='vSelectEach' data-value=''>All Sections</span>"}
*/
$.fn.resetVSelectTag = function(params){
	if(!params.newVal || !params.newVal.text){
		params.newVal = {'text':'','val':''};
	}
	if(!params.newVal.val){params.newVal.val="";}
	this.each(function(){
		$this = $(this);
		$this.data('value',params.newVal.val).find(".vSelectText").val("").val($.trim(params.newVal.text));
		$this.find(".vSelectListHolder").data("selected",$.trim(params.newVal.text));
		$this.find(".vSelectList").data("");
		if(typeof params.optionsList=="string"){	
			$this.find(".vSelectList").html(params.optionsList);
		}
	});
	this.updateVSelectTag();
	return this;
}
var vSelectFns = {
	onchangecb:null,
	indexCount:0,
	scrollBarOptions:"",
	setNewId:function(caller){
		var newIndex = parseInt(vSelectFns.indexCount,10);
		vSelectFns.indexCount++;
		var newId = "vSelect_"+newIndex;
		$(caller).attr("id",newId);
		newId = "#"+newId;
		return {id:newId,index:newIndex};
	},
	registerEventCB:function(newId,obj,selected){
		$(newId).on("click",".vSelectHead",obj,vSelectFns.onClick)
		.on("click",".vSelectList .vSelectEach",obj,vSelectFns.select)
		.find(".vSelectHead .vSelectText").val("").val($.trim(selected))
		.keydown(obj,vSelectFns.onkeydown);
		/*$(newId).find(".vSelectText").focusin(obj,vSelectFns.focusIn)
			.focusout(obj,vSelectFns.focusOut);*/
	},
	applyScrollBar:function(onMe,eachItems,sBarOptions){
		var height = eachItems.length * $($(eachItems).get(0)).height() + eachItems.length*3;
		$(onMe).css("height",height+"px")
			.useScrollBar(sBarOptions);
	},
	init:function(){
		$(document)
			.off("click",".vselect",function(){return false;})
			.off("click",vSelectFns.hideList)
			.on("click",".vselect",function(){return false;})
			.on("click",vSelectFns.hideList);
		$(".vselect").each(function(){
			var eachItems = $(this).find(".vSelectList .vSelectEach");
			var doneOrNot = $(this).attr("initiated");
			if(eachItems.length>=1 && $(this).attr("id")!="vSelect_parent" && doneOrNot!="done"){
				var selected = $(this).removeClass("vselectHide").find(".vSelectText").width(function(index,width){ return (width-12)+"px";}).val();
				var holder = $(this).find(".vSelectListHolder").data("selected",selected).addClass("vSelectListShow");
				var value = vSelectFns.setNewId(this);
				var obj = {id:value.id,index:value.index};
				vSelectFns.registerEventCB(value.id,obj,$(this).find(".vSelectHead .vSelectText").val());
				$(this).attr("initiated","done");
				vSelectFns.applyScrollBar(holder,eachItems,vSelectFns.scrollBarOptions);
			}
		});
		vSelectFns.hideList();
	},
	fill:function(id,params){
		var items;
		if(typeof params.items == "string"){
			items = params.items.split(",");
		}else{
			items = params.items;
		}
		if(!items) return;
		var uiItems = $(id+" .vSelectList").find(".vSelectEach");
		for(var index=0;index<items.length;index++){
			var item = uiItems.get(index);
			if(!item){
				item = vSelectFns.createNew($(id+" .vSelectList"),uiItems.get(0));
				uiItems[uiItems.length] = item;
			}
			$(item).text(items[index]).attr("val",items[index]);
		};
	},
	createNew:function(id,cloneMe){
		var newItem = $(cloneMe).clone();
		$(id).append(newItem);
		return newItem;
	},
	focusOut:function(e){
		var myObj = e.data;
		if($(myObj.id).find(".vSelectListHolder").data("opened")){
			vSelectFns.hideThis(myObj);
			return;
		}
	},
	focusIn:function(e){
		var myObj = e.data;
		vSelectFns.hideList();
		$(myObj.id).find(".vSelectListHolder").addClass("vSelectListShow").on("mouseenter",".vSelectEach",myObj,vSelectFns.onHover).data("opened",true);
		vSelectFns.curSelectedText = $(myObj.id+" .vSelectHead .vSelectText").val();
		$(myObj.id+" .vSelectList .selected").removeClass("selected");
		$($(myObj.id+" .vSelectList span:contains('"+vSelectFns.curSelectedText+"')").get(0)).addClass("selected");
		myObj.opened = true;
	},
	onClick:function(e){
		var myObj = e.data;
		if($(myObj.id).find(".vSelectListHolder").data("opened")){
			vSelectFns.hideThis(myObj);
			return;
		}
		vSelectFns.hideList();
		$(myObj.id).find(".vSelectListHolder").addClass("vSelectListShow").on("mouseenter",".vSelectEach",myObj,vSelectFns.onHover).data("opened",true);
		vSelectFns.curSelectedText = $(myObj.id+" .vSelectHead .vSelectText").focus().val();
		$(myObj.id+" .vSelectList .selected").removeClass("selected");
		$($(myObj.id+" .vSelectList span:contains('"+vSelectFns.curSelectedText+"')").get(0)).addClass("selected");
		myObj.opened = true;
	},
	onHover:function(e){
		var myObj = e.data;
		$(myObj.id+" .vSelectList .selected").removeClass("selected");
		$(this).addClass("selected");
	},
	reset:function(tag){
		var selected = $(tag).find(".vSelectListHolder").data("selected");
		if(selected!=undefined){$(tag).find(".vSelectText").val("").val($.trim(selected));}
	},
	hideList:function(e){
		$(".vselect").find(".vSelectListShow").each(function(){
			$(this).removeClass("vSelectListShow").data("opened",false);
			vSelectFns.reset($(this).closest(".vselect"));
		});
	},
	hideThis:function(myObj,dontReset){
		$(myObj.id).find(".vSelectListHolder").removeClass("vSelectListShow").data("opened",false);
		$(myObj.id+" .vSelectHead .vSelectText").focus();
		if(!dontReset){ vSelectFns.reset($(myObj.id));}
	},
	select:function(e){
		var myObj = e.data;
		var value = $(this).text();
		//$(myObj.id).find(".vSelectHead .vSelectText").attr("value",value);
		vSelectFns.changeText(myObj.id,value,this,true);
		vSelectFns.hideThis(myObj,true);
	},
	onkeydown:function(e){
		var myObj = e.data;
		var key = e.which;
		var newItem;
		var moveBy=0;
		var item = $(myObj.id+" .vSelectList .selected").get(0);
		switch(key){
			case 38:
				newItem = $(item).prev().get(0);
				moveBy = $(newItem).height();
			break;
			case 40:
				newItem = $(item).next().get(0);
				moveBy = -1*$(newItem).height();
			break;
			case 13:vSelectFns.hideThis(myObj,true);
				vSelectFns.changeText(myObj.id,$(item).text(),item,true);
			return;
			case 27:vSelectFns.hideThis(myObj);
			return;
			default:return;
		};
		if(newItem){
			$(newItem).addClass("selected");
			$(item).removeClass("selected");
			$(myObj.id).find(".vSelectListHolder").scrollBarExt("moveBy",moveBy);
			//$(myObj.id).find(".vSelectHead .vSelectText").focus().attr("value",$(newItem).text());
			vSelectFns.changeText(myObj.id,$(newItem).text(),newItem,false);
			e.preventDefault();
			return false;
		};
	},
	onVSelectTextChange:function(id,innerValue,target){
		var fn_name = $(id).attr("onstatechange");
		var vSelect = $(id);
		try{
			var value = $(target).data("value");
			vSelect.data("value",value);
                        $(target).closest(".vSelectListHolder").data("selected",innerValue);
		}catch(err){}
		try{
			if(fn_name){ window[fn_name](innerValue,target,value);}
		}catch(err){putConsoleLogs(err);}
		try{
			clickstream.extElemRecord(vSelect,{"value":innerValue,"target_data_val":value},"CHANGE");
		}catch(err){};
	},
	changeText:function(id,value,item,doCallback){
		value = $.trim(value);
		$(id).find(".vSelectHead .vSelectText").focus().val("").val(value);
		if(doCallback){
			try{this.onchangecb(id,value,item);}catch(err){}
		}
	}
};
$(document).ready(function(){
	vSelectFns.onchangecb = vSelectFns.onVSelectTextChange;
	vSelectFns.init();
});
window["vSelectFns"] = {init:vSelectFns.init};
})(jQuery);
