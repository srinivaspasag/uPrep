
var clickstream=new (function($){
    var currentxhr=null,timeDeltaInMiliSecs;
    var clickstreamUrl='/Application/updateClickStream';
    this.init=function(){
         //TODO CLICK STREAM DISABLED
	//$(document).on("change click dblclick keydown",".doCStream",domDefinedCStream);
         timeDeltaInMiliSecs=$("body").children("#serverTime").val()-new Date().getTime();
         if(!timeDeltaInMiliSecs || timeDeltaInMiliSecs<60000){
		timeDeltaInMiliSecs = 0;
	 }
    };
    var domDefinedCStream = function(e){
	var $this=$(this);
        var action=e.type.toUpperCase(); 
	doCStreamClick($this,$this.data(),action,e);
    };
    this.extElemRecord = function(elem,data,action){
	doCStreamClick(elem,data,action);
    };
    var doCStreamClick=function($this,data,action,e){// CS Element , data
	//TODO DISABLED
	return;
        var csParams={};
        var elname=$this.data("csName")||$this.data("name")||$this.attr("name");//DOM Attr name has been deprecated
        var widget=$this.closest(".CSWidget").data("widget");
        if(!elname||!widget)return;
        try{
            csParams.csElement=elname.toUpperCase();
            csParams.csWidget=widget.toUpperCase();
            csParams.csPage=j[j.length-1].tabType.toUpperCase(); 
        }catch(err){}
        if(!action && e){
        	action=e.type.toUpperCase(); 
	} 
	if(action== "KEYDOWN" && e){
            if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
                    action="ENTER";
            }else{
                    return;
            }
	}
        csParams.csAction=action; 
        csParams.csData=data;
        csParams.time=new Date().getTime();
        if(currentxhr&&!currentxhr.csParams){
            currentxhr.csParams=csParams;
        }else{
            record(csParams);
        }
    };
    
    this.beforeSend=function(xhr,settings){
        currentxhr=xhr;
    };
    this.complete=function(xhr){
        if(xhr.csParams){
            var csData=xhr.csParams.csData;
            try{
                csData.statusCode=xhr.status;
                csData.statusText=xhr.statusText;
                if(xhr.inputParams)$.extend(csData,xhr.inputParams);
                if(xhr.respErrorCode)csData.errorCode=xhr.respErrorCode;
            }catch(err){}
            record(xhr.csParams);
        }
        currentxhr=null;
    };
    this.record = function(widget,elem,action,data,time,csPage){
        var csParams={},$this=$(this);
        if(!time) csParams.time=new Date().getTime();
        csParams.csPage = csPage?csPage:j[j.length-1].tabType;
        try{
		csParams.csElement = elem.toUpperCase();
		csParams.csWidget = widget.toUpperCase();
		csParams.csAction = action.toUpperCase();
	}catch(err){}
        if(data) csParams["csData"] = data;
	record(csParams);
    }; 
    var record=function(csParams){
        csParams.time+=timeDeltaInMiliSecs;
        var domainName = window.location.hostname?window.location.hostname:window.location.host;     
        csParams.csProfile=domainName;       
        GA(csParams);
        $.post(clickstreamUrl,csParams,function(response,stat){});    
    };
})(jQuery);
function GA(csParams){
 // _gaq.push(['_trackEvent', category, action,label]);
 //try{_gaq.push(['_trackEvent', csParams.csWidget, csParams.csAction,csParams.csElement]);}catch(err){}
}
clickstream.init();
