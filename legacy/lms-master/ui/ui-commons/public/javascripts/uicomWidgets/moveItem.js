var vMoveItem=new(function($){
    var clickEvent="click.vMoveItem",dblclickEvent="dblclick.vMoveItem";
    this.init=function(moveWidget,params,loadMoveItems){
        moveWidget.off(clickEvent).off(dblclickEvent)
        .on(clickEvent,".moveItemOpen",moveWidget,moveItemOpen)
        .on(clickEvent,".moveItemClose",moveWidget,moveItemClose)
        .on(clickEvent,".moveItemHead",moveWidget,moveItemHead)
        .on(dblclickEvent,".moveItemHead",moveWidget,moveItemHeadDblclick)
        
        //params={urlStr,urlParams,beforecbfn,aftercbfn,parentIdName}
        moveWidget.data(params);
        //for initial loading of move items
        if(loadMoveItems){
            loadNewChildren(moveWidget,moveWidget,moveWidget);
        }        
    }
    
    //event handlers
    var moveItemOpenClass="moveItemOpen",moveItemCloseClass="moveItemClose";
    var moveItemOpen=function(e){
        loadMoveItemChildren(e.data,$(this).closest(".moveItem")); 
        $(this).removeClass(moveItemOpenClass).addClass(moveItemCloseClass);
        e.stopPropagation();
    };
    var moveItemClose=function(e){
        var target=$(this).closest(".moveItemHead").siblings(".moveItemChildren");
        target.addClass("nonner");
        $(this).addClass(moveItemOpenClass).removeClass(moveItemCloseClass);
        e.stopPropagation();
    }; 
    var moveItemHead=function(e){
        markActiveItem(e.data,$(this).closest(".moveItem"));
    };
    var moveItemHeadDblclick=function(e){
        markActiveItem(e.data,$(this).closest(".moveItem"));
        loadMoveItemChildren(e.data,$(this).closest(".moveItem"));
    };
    var markActiveItem=function(moveWidget,moveItem){
        var activeClass="activeMoveItem";        
        moveWidget.find(".moveItem").removeClass(activeClass);
        moveItem.addClass(activeClass);        
        var $data=moveWidget.data();
        $data.finalValue=moveItem.data("entityId");
        $data.finalName=moveItem.find(".moveItemName").text();
        $data.itemParams=moveItem.data();
        if($data.markItemcbfn){
            $data.markItemcbfn(moveWidget,moveItem);
        }
    }
    var loadMoveItemChildren=function(moveWidget,moveItem){
        var target=moveItem.children(".moveItemChildren");
        target.removeClass("nonner");
        if(target.html()==""){            
            loadNewChildren(target,moveItem,moveWidget);
        }          
    }
    var loadNewChildren=function(target,moveItem,moveWidget){        
        var moveWidgetData=moveWidget.data();        
        var goAhead=true;
        if(moveWidgetData.beforecbfn){
            goAhead=moveWidgetData.beforecbfn(moveWidget,moveItem);
        }            
        if(!goAhead){
            return;
        }
        target.html("<div class='time'>Loading..</div>");
        var params=moveWidgetData.urlParams||{};
        var parentIdName=moveWidgetData.parentIdName;
        if(!parentIdName)parentIdName="parentId";
        
        var entityId=moveItem.data("entityId");
        if(entityId){
            params[parentIdName]=entityId;
        }        
        if(moveItem.hasClass("moveItem")){
            params.multiplier=moveItem.parents(".moveItem").length+1;
        }
        $.get(moveWidgetData.urlStr,params,function(data){
            target.html(data);
            if(moveWidgetData.aftercbfn)window[moveWidgetData.aftercbfn](moveWidget);
        });         
    }    
})(jQuery);