var videoControl = new function(){
	var parDiv = "#videoPage";
	this.init = function(){
		parDiv = $(parDiv);
		if(!parDiv.data("videoId")) return;
		initComments();
		getSugg();
		parDiv.find(".videoTitle").elipsifyText("big18");        
		parDiv.find(".videoDescription").elipsifyText();        
	};
	var getSugg = function(){
		var holder = parDiv.find(".videoSugg");
        if(holder.length == 0){
            return ;
        }
		smallLoader(holder);
		var videoId=parDiv.data("videoId");
		var params = {start:0,size:5,entity:{"id":videoId,"type":"VIDEO"}};
		$.get("/MyContents/videoSugg",params,function(data){
			holder.html(data);
		});
	};
	var initComments = function(){
    		var videoId=parDiv.data("videoId");
    		var commHolder=$(parDiv).find(".videoComments");
    		commHolder.removeClass('nonner');
		commHolder.html("");
        	var LMData={
			urlStr:"/widgets/commItems",
			size:10,start:0,
			parent:{type:"VIDEO",id:videoId},
			root:{id:videoId,type:"VIDEO"},
			orderBy:"timeCreated",
			target:'MY_INSTITUTE'
		};
        	var allParams={};
        	allParams.root = {"id":videoId,"type":"VIDEO"};
        	allParams.base = {"id":videoId,"type":"VIDEO"};
        	allParams.parent = {"id":videoId,"type":"VIDEO"};
        	allParams.scope = "ORG";
        	allParams.callBack="VIDEO";
        	allParams.targetPage="MY_INSTITUTE";
        	allParams.placeHolder="Write a Comment";
		bigLoader(commHolder);
        	initCommWidget(allParams,LMData,commHolder,function(holder,data){
			institute.animate($(holder).find(".commItem:first"),false);
		});
	};
};
$(function(){
	videoControl.init();
	try{
		if(institute){
			institute.init();
		}
	}catch(err){}
});
