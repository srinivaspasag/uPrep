
//entry suggestions
var entrySuggValStore="",esEntrySugg={},esDefaultPopulater={},esSubmit={};
$(".entrySuggDiv").live('click',function(){
   $(this).closest(".entrySuggHolder").find(".entrySuggInput").focus();   
});
$(".entrySuggInput").live('focus',function(e){
   var ESHolder=$(this).closest(".entrySuggHolder"),target=$(this).data("target");  
   assignShowEntryPH($(this));
   $(this).attr("placeholder","");
   addToggler(ESHolder.find(".ESListDiv"),ESHolder.find(".entrySuggDiv"));
   if($(this).val().length==0){
        if(esDefaultPopulater[target])esDefaultPopulater[target](ESHolder);
    }      
});
$(".entrySuggInput").live('blur',function(e){
      assignShowEntryPH($(this));
});
function assignShowEntryPH(entrySuggInput){
   var ESHolder=entrySuggInput.closest(".entrySuggHolder");
   if(ESHolder.find(".ESSelected").length>0||entrySuggInput.val().length>0){
       entrySuggInput.removeClass("showEntrySuggPH")
       .attr("placeholder","");       
   }else {
       entrySuggInput.addClass("showEntrySuggPH")
       .attr("placeholder","Search and Add or Select people from the list");     
   }
}
esDefaultPopulater["SHARE_FRNDS"]=function(ESHolder){
    
}
$(".entrySuggInput").live('keydown',function(e){
   if(((e.which && e.which == 8) || (e.keyCode && e.keyCode == 8))&&$(this).val().length==0){
       var list=$(this).siblings(".entrySuggAddedList").find(".ESSelected");
       list.last().remove();
       $(this).focus();
   }       
});
$(".entrySuggInput").live('keyup',function(e){
    var $this=$(this),ESHolder=$this.closest(".entrySuggHolder");
   $(this).attr("placeholder","");
   addToggler(ESHolder.find(".ESListDiv"),ESHolder.find(".entrySuggDiv")); 
   
   var ESPTiles=ESHolder.find(".ESPTile");
   if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
        finaliseESPTile(ESHolder);
   }
   else if((e.which && e.which == 40) || (e.keyCode && e.keyCode == 40)){
       //down arrow      
       upDownActiveTile("DOWN",ESPTiles,"ESPActiveTile");
   }
   else if((e.which && e.which == 38) || (e.keyCode && e.keyCode == 38)){
       //up arrow
       upDownActiveTile("UP",ESPTiles,"ESPActiveTile");
   }
   else{
         $this.attr("size",($this.val().length+1).toString());
         var searchVal=$this.val();
         if(searchVal.length==0){
             ESHolder.find(".ESListDiv").html("");
             esDefaultPopulater["SHARE_FRNDS"](ESHolder);
         }
         else if(searchVal.length==1) getEntrySuggResults($this,"MUST");
         else setTimeout(function(){entrySuggTimerFn($this)},300);
   }
});
$(".removeEntrySugg").live('click',function(e){
    $(this).closest(".ESSelected").remove();
});
$(".ESPTile").live('mouseover',function(e){
    $(this).addClass("ESPActiveTile").siblings().removeClass("ESPActiveTile");
});
$(".ESPTile").live('click',function(e){    
    finaliseESPTile($(this).closest(".entrySuggHolder"));
});
function getEntrySuggResults(inputObj,option){
    var searchVal=inputObj.val();
    var popup=inputObj.closest(".entrySuggHolder").find(".ESListDiv");
    //if(option=="MUST")popup.html("<div style='ESList'><div class='centry boldy margTop margBot10'>Loading..</div></div>");
    if((searchVal.length>0&&searchVal!=entrySuggValStore)||option=="MUST"){        
        var inputObjData=inputObj.data(),allParams=inputObjData.params;
        if(!allParams)allParams={};
        allParams[inputObjData.query]=searchVal;
        allParams.size=7;
        allParams.excludeIds=getESExcludeIds(inputObj);
        $.get(inputObjData.urlStr,allParams,function(data){
            var popupHTML=esEntrySugg[inputObjData.target](data);
            popup.html(makeHTMLTag("div",{"class":'ESList'}).html(popupHTML));
            popup.find(".ESPTile").first().addClass("ESPActiveTile");
        });
    }
    else if(searchVal.length==0){
        popup.html("");
    }
    entrySuggValStore=searchVal;
}
esEntrySugg["SHARE_FRNDS"]=function(data){
    var suggs=data.result,popupHTML=makeHTMLTag("div");
    for(var k=0;k<suggs.length;k++){
        var espTilePa=shareEntrySuggSample.clone(true);
        var s=suggs[k];
        espTilePa.find(".ESPTile").data("params",{userId:s.userId,entityId:s.userId});
        espTilePa.find("img").attr("src",s.profilePic);
        espTilePa.find(".entrySuggName").html(s.firstName+" "+s.lastName);
        popupHTML.append(espTilePa.children());
    }
    if(suggs.length==0)popupHTML.html("<div class='boldy margTop margBot10 centry'>No Matches found</div>");
    return popupHTML;
}
function entrySuggTimerFn(entrySuggInputObj){
    getEntrySuggResults(entrySuggInputObj);
}
esSubmit["SHARE_FRNDS"]=function(ESHolder){
    var activeTile=ESHolder.find(".ESListDiv .ESPActiveTile");
    var tile=makeHTMLTag("div",{"class":"ESSelected"}).data("params",activeTile.data("params"))
    .html(activeTile.find(".entrySuggName").text()+"<label class='removeEntrySugg'>x</label>");
    ESHolder.find(".entrySuggAddedList").append(tile);
}
function finaliseESPTile(ESHolder){
    var target=ESHolder.find(".entrySuggInput").data("target"); 
    if(esSubmit[target])esSubmit[target](ESHolder);
    entrySuggValStore="";
    ESHolder.find(".ESListDiv").html("");
    ESHolder.find(".entrySuggInput").attr("size","1").val("").focus();      
}

function getESExcludeIds($this){
    var ESHolder=$this.closest(".entrySuggHolder"),excludeIds=[];
    ESHolder.find(".ESSelected").each(function(){
        excludeIds.push($(this).data("params").entityId);
    });
    return excludeIds;
}
function upDownActiveTile(arrow,tiles,activeClass){    
       var newTile,firstLastTile,activeTile=tiles.siblings("."+activeClass);
       if(arrow=="DOWN"){
           newTile=activeTile.next();
           firstLastTile=tiles.first();
       }
       else{
           newTile=activeTile.prev();
           firstLastTile=tiles.last();
       }       
       if(activeTile.length>0){
           if(newTile.length>0){
               newTile.addClass(activeClass).siblings().removeClass(activeClass);
           }
           else {
              firstLastTile.addClass(activeClass).siblings().removeClass(activeClass);
           }
       }else{
           tiles.first().addClass(activeClass);
       }
}

