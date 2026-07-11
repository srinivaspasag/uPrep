$(document).on('click',".showSharedInfo",function(){
   var $this=$(this),$data=$this.data();
   var shareInfoDiv=$(this).siblings(".sharedInfoDiv");
   addToggler(shareInfoDiv,$this);
   if(shareInfoDiv.html()==""){
       shareInfoDiv.html("Loading..");
       $.get("/widgets/getSharedWithInfo",{shareId:$data.shareId},function(data){
           var  people="",info=data.result;
           for(var k=0;k<info.length;k++){
               people+=getUserUrlAndClass(info[k]);
           }
           shareInfoDiv.html(people);
       });       
   }   
});

function instituteTickerCB(data){
	var count = 0;
	$(data).each(function(){
		if(this.count) count+=this.count;
	});
	if(count>0){
		$("#instTickerCount").text(count).data('count',count).removeClass("nonner");
	}
}
$(document).on("click","#myInstitutePage",function(){
	if($("#instTickerCount").data('count')){
	   tickers.flush("ORGANIZATION_ACTIVITY",function(data){
		if(data && data.result && data.result.flushSucessful){
			$("#instTickerCount").text("").data('count',0).addClass("nonner");	
		}	
	   });
	}	
});
function logoutService(data){
	window.top.location.reload();
}


//home page question feeds manager
var homePageManageContent=new(function($){
    var mcWidgetAttemptTabs=[{name:"All",mcSubTabParams:{"attemptType":"ALL"}},
    {name:"Attempted",mcSubTabParams:{"attemptType":"ATTEMPTED"}},
    {name:"Unattempted",mcSubTabParams:{"attemptType":"UNATTEMPTED"}}];    
    
    var sharedSubTabs=[{"name":"With Me",
                             mcSubTabParams:{"entityType":"QUESTION","sharedType":"WITH_ME",
                                 "with":{"type":"USER","id":USERID}}},{"name":"By Me",
                             mcSubTabParams:{"entityType":"QUESTION","sharedType":"BY_ME"}}];
                     
                             
    var addSubTabs=function(mcWidget,subTabs){
        var subTabsHTML=makeHTMLTag("div");
        if(subTabs){       
            for(var k=0;k<subTabs.length;k++){
                var subTab=subTabs[k],sep="/ ",classes="mcSubTab";
                if(k==0){
                    classes="mcSubTab activemcSubTab";
                    sep="";
                }
                var tab=makeHTMLTag("span",{"class":classes})
                .text(sep+subTab.name).data("params",subTab.mcSubTabParams);           
                subTabsHTML.append(tab);
            }       
        }   
        mcWidget.find(".mcSubTabs").html(subTabsHTML.children());             
    };   
    
    this.tabClick=function(mcWidget,mcTabsDiv){
        var mcWidgetData=mcWidget.data(),$params=mcWidgetData.params,manageContentVar=manageContent;
        
        var deleteList=["attemptType","sharedType","entityType","with","resultType"];
        manageContentVar.removemcWidgetParams($params,deleteList);
        
        var activeTab=mcTabsDiv.find(".activemcTab");        
        mcWidgetData.urlStr="/questions/quesItems";
       
        var tabType=activeTab.data("name");
        var subTabs=mcWidgetAttemptTabs;
        if(tabType=="SHARED_TAB"){
            subTabs=sharedSubTabs;
            mcWidgetData.urlStr="/widgets/sharedContent";
        }
        $.extend($params,activeTab.data("params"),subTabs[0].mcSubTabParams);
        addSubTabs(mcWidget,subTabs);
        manageContentVar.loadmcContent(mcWidget,mcTabsDiv);
    }
})(jQuery);
var homePagemcTabClick=function(mcWidget,mcTabsDiv){
    homePageManageContent.tabClick(mcWidget,mcTabsDiv);      
}