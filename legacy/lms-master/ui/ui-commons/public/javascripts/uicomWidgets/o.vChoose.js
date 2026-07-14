$(document).on("click",".vChooseHead,.vChooseDownIcon",function(){
    var vChoose=$(this).closest(".vChoose");
    openvChoose(vChoose);
});
var openvChoose=function(vChoose){
   var inited=vChoose.data("inited");
   if(inited&&inited==true){       
       vChooseVar.off(vChoose);
   }else{       
       vChooseVar.on(vChoose);
   }     
};
var vChooseVar=new function($){  
    var activeClass="vChooseOptActive";
    this.on=function(vChoose){
        vChoose.data("inited",true); 
        vChoose.addClass("vChooseActive");
        vChoose.children(".vChooseDropDown").css({top:vChoose.height()})
        $(window).bind('click.showvChoose',function(e) {
            var t=$(e.target);
            if(!(t.is(vChoose))&&t.closest(vChoose).length==0){              
                vChoose.removeClass("vChooseActive");
                vChooseOff(vChoose,t);
            }
        });      
        vChoose.find(".vChooseOpt").each(function(){
            var $this=$(this);
            if(!$this.hasClass("invalidvChooseOpt")&&vChoose.data("value")===$this.data("value")){  
                $this.addClass(tickerClass).siblings().removeClass(tickerClass);
                return false;
            }
        });   
        vChoose.find(".dateDropDown").useScrollBar();
        $(document).on("keydown.vChoose",vChoose,vChooseKeydown);
        vChoose.on("mouseover",".vChooseOpt",vChoose,vChooseMouseover)
        .on("click",".vChooseOpt",vChoose,vChooseOptClick);
    };
    var vChooseKeydown=function(e){
       cancelEvent(e);
       var vChoose=e.data,tiles=vChoose.find(".vChooseOpt");
       if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
            vChooseSelected(vChoose);
       }
       else if((e.which && e.which == 40) || (e.keyCode && e.keyCode == 40)){
           upDownActiveTile("DOWN",tiles,activeClass);
       }
       else if((e.which && e.which == 38) || (e.keyCode && e.keyCode == 38)){
           upDownActiveTile("UP",tiles,activeClass);
       }
    }
    var tickerClass="vChooseOptTicked";
    var vChooseSelected=function(vChoose){
        var opt=vChoose.find(".vChooseOptActive");
        var $optData=opt.data(),finalValue=$optData.value;
        var isValidOpt=true;
        if(opt.hasClass("invalidvChooseOpt")){
            isValidOpt=false;
        }
        if(opt.length>0&&isValidOpt){
            opt.addClass(tickerClass).siblings().removeClass(tickerClass);
            vChoose.find(".vChooseHead").html(opt.html());
            var $vChooseData=vChoose.data();
            delete $vChooseData["optParams"];
            $vChooseData.value=finalValue;
            $vChooseData.optParams=$optData.optParams;
        }        
        var cbfnsStr=vChoose.data("onchange");
        var cbfns=[];
        if(cbfnsStr){
            cbfns=cbfnsStr.trim().split(" ");
        }                
        if(cbfns.length>0&&isValidOpt){
            for(var k=0;k<cbfns.length;k++){
                window[cbfns[k]](vChoose,finalValue);
            }            
        }
        vChooseOff(vChoose);
        try{
            clickstream.extElemRecord(vChoose,{"value":finalValue},"CHANGE");
        }catch(err){}
    };
    var vChooseMouseover=function(){
        $(this).addClass(activeClass).siblings().removeClass(activeClass);
    };
    var vChooseOptClick=function(e){
        vChooseSelected(e.data);
    };


    this.off=function(vChoose){        
        vChooseOff(vChoose);
    };  
    var vChooseOff=function(vChoose,newvChooseEl){
        vChoose.data("inited",false).removeClass("vChooseActive").off();        
        $(document).off(".vChoose");
//        if(newvChooseEl&&(newvChooseEl.hasClass("vChoose")||
//                newvChooseEl.closest(".vChoose").length>0)){
//            $(window).unbind('click.showvChoose');        
//        }        
    };
    this.reset=function(vChoose,value,htmlContent,newvChooseOpts){
        var vChooseOpt=vChoose.find(".vChooseOpt").first();
        value=value||vChooseOpt.data("value");
        htmlContent=htmlContent||vChooseOpt.html();
        vChoose.data("value",value).find(".vChooseHead").html(htmlContent);
        if(newvChooseOpts){
            vChoose.find(".vChooseDropDown").html(newvChooseOpts);
        }        
    };
    this.set=function(vChoose,value){
        setvChooseValue(vChoose,value);
    };
}(jQuery);
