var j=[{id:"#noTabSection",tabType:"HOME",scrollValue:0}];
function tabIndex(id){
    for(var i=0;i<j.length;i++){
            if(j[i].id==id)return i;
    }
    return -1;
}
var noSearchBarPages = ["MY_INSTITUTE","TEST_ATTEMPT","TEST","LAB_TEST","MESSAGES"];
function showNoTabSec(tabType){
    clearPopupsAndTimers();
//to remove the main element of an array and place it at the end
    j=[{id:"#noTabSection",tabType:"HOME",scrollValue:0}];
    j[j.length-1].scrollValue=$(window).scrollTop();
    j.splice(tabIndex("#noTabSection"),1);
    j.push({id:"#noTabSection",tabType:tabType,scrollValue:0});
    $(window).scrollTop(0);
    mainMenuActivate(tabType); 
    if(noSearchBarPages.indexOf(tabType)>=0){
	$('#searchContentHeader').addClass("nonner");
    }else{
	$('#searchContentHeader').removeClass("nonner");
    }
    
    try{
	onNoTabSecLoad(tabType);
    }catch(err){}
    $("body").removeClass("PL_DOC_VIEW");
}
function onNoTabSecLoad(tabType){
	switch(tabType){
		case "MY_INSTITUTE":break;
		default:break;
	};
	$("#searchContentTextBox").val("");
}
function tabNotFound(id,tabType){
//to add new element to the array and put a tab at the bottom
    j[j.length-1].scrollValue=$(window).scrollTop();
    j.push({id:"#DTDiv_"+id,tabType:tabType,scrollValue:0});
    $(window).scrollTop(0);
    activateFootAndDisplay(id,tabType);
}
function tabFound(id){
//if a tab at the bottom is clicked,then the that tab is displayed and it is removed from the array and placed at the end
    j[j.length-1].scrollValue=$(window).scrollTop();
    if(j[tabIndex("#DTDiv_"+id)]==undefined)return;
    var sv=j[tabIndex("#DTDiv_"+id)].scrollValue;
    var tabType= j[tabIndex("#DTDiv_"+id)].tabType;
    j.splice(tabIndex("#DTDiv_"+id),1);
    j.push({id:"#DTDiv_"+id,tabType:tabType,scrollValue:sv});
    activateFootAndDisplay(id,tabType);
    $(window).scrollTop(sv);
}
function tabMinimize(){
//removes the element from the end and places it at the starting
    j[j.length-1].scrollValue=$(window).scrollTop();
    var store =j.pop();
    j.unshift(store);
    closeAndMinimize(1);
    $(window).scrollTop(j[j.length-1].scrollValue);
}
function tabClose(id){
//when a tab at the bottom is closed, if the tab is not being shown then it is simply deleted
//or if it is shown then then the last but one or previous tab is shown after closing this tab
    if((tabIndex("#DTDiv_"+id)+1)==j.length){
        closeAndMinimize(2);
    }
    clearPopupsAndTimers();
    $("#DTDiv_"+id).empty().remove();
    $("#tab_"+id).empty().remove();
    if($("#footBar").html()=='')$("#footBarHolder").css("display","none");
    $(window).scrollTop(j[j.length-2].scrollValue);
    j.splice(tabIndex("#DTDiv_"+id),1);
}
function activateFootAndDisplay(id,tabType){
    clearPopupsAndTimers();
    $(".toBeClosed").css("display","none");
    $("#DTDiv_"+id).css("display","block");
    $("#footBar #tab_"+id).addClass("activeDTTab").siblings(".footTab").removeClass("activeDTTab");
    if(id=="UPLOAD")$("#uploadDoneBalloon").css("display","none").css("left","10px").css("top","-13px");
    if(tabType=="DOC_VIEW"||tabType=="PL_VIEW"){
        $("#topBarHolder").addClass("fixedTopBar");
    }
    else{
        $("#topBarHolder").removeClass("fixedTopBar");
    }
}
function closeAndMinimize(k){
    var tabType=j[j.length-k].tabType;
    if(j[j.length-k].id=="#noTabSection"){
        mainMenuActivate(tabType);
        if($("#noTabSection").html()==""){
            goToHomePage();
            pushHistory(null , null,"/");
        }
    }
    else activateFootAndDisplay(j[j.length-k].id.substring(7),tabType);
}
$(".footTab").live('click',function(){
  var id=$(this).attr("id").substring(4);
  tabFound(id);
});
$(".minimizeTab").live('click',function(){
   tabMinimize();
});
$(".footTab .closeTab").live('click',function(){
   var id=$(this).closest(".footTab").attr("id").substring(4);
   tabClose(id);
});
$(".closeTabFromTop").live('click',function(){
    var id=$(this).closest(".toBeClosed").attr("id").substring(6);
   tabClose(id);
});


//tab design
var footTab ={};
footTab["UPLOAD"]=function(){
        $("#footBar").append("<div class='footTab' id='tab_UPLOAD'><div class='UPLOAD_TabTile'><div class='UPTabBacker'></div><div class='uploadTabPercent'></div><img src='/public/images/uploadTabTile.png' /></div>\n\
                        "+xTab("Upload")+"<div id='uploadDoneBalloon'>100%</div></div>");
}
footTab["EDIT_DOC"]=function(docName){
        $("#footBar").append("<div class='footTab' id='tab_EDIT_DOC'><div class='EDIT_DOC_TabTile tabTile'><img src='/public/images/profile/editProfile.png' /><label>"+docName+"</label></div>\n\
                        "+xTab(docName)+"</div>");
}
footTab["PL_EDIT"]=function(plName){
        $("#footBar").append("<div class='footTab' id='tab_PL_EDIT'><div class='PL_EDIT_TabTile tabTile'>"+plName+"</div>\n\
                        "+xTab(plName)+"</div>");
}
footTab["CREATE_TEST"]=function(){
        $("#footBar").append("<div class='footTab' id='tab_CREATE_TEST'><div class='CREATE_TEST_TabTile tabTile'>Test Creation</div>\n\
                        "+xTab("Test Creation")+"</div>");
}
function xTab(nam){
    return "<div class='footBarFloaterHolder'><div class='footBarFloater'><img src='/public/images/tabPointer.png' alt='<'><span class='closeTab'></span><span class='footBarName'>"+nam+"</span></div></div>";
}
