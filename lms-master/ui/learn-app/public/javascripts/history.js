function isEmpty(obj) {
    for(var key in obj) {
        if(obj.hasOwnProperty(key))
            return false;
    }
    return true;
}
var vHistory=new(function(){
    var urlPathStrips,urlParams,urlMapper={},orgUrlMapper={};
    var activities="ACTIVITIES",discussions="DISCUSSIONS",analytics="ANALYTICS",library="LIBRARY",programs="PROGRAMS"
    ,conversation="CONVERSATION",profile="PROFILE",referral="REFERRAL",notifications="NOTIFICATIONS"
    ,program="PROGRAM",subject="SUBJECT",chapter="CHAPTER",test="TEST",discussion="DISCUSSION", document="DOCUMENT",video="VIDEO",pretest="PERTEST"
    ,attempt="ATTEMPT",aiims="AIIMS",neet="NEET",jeeadvanced="JEEADVANCED",jeemain="JEEMAIN",bitsat="BITSAT";
    //mycontent and explorecontent can be both sigle and double
    var singlePathList = [notifications,activities,discussions,analytics,library,programs,conversation,profile,referral];
    var digitalLibraryList = [subject,program,chapter,test,attempt,document,video];
    var pageNotFound=function(holder){
    holder = holder!=undefined?holder:$("#noTabSection");
        $.get("/Widgets/pageNotFound",function(data){
           holder.html(data);
        });
    };
    this.init=function(urlStrips,paramUrl){
        urlPathStrips=urlStrips;
        urlParams = paramUrl?paramUrl:{};
        var category=urlPathStrips[0].toUpperCase();
        swal.close();
        if(category==""){
            urlMapper["HOME"]();
        }
        else if(urlMapper[category]) {
            if((singlePathList.indexOf(category)>=0 && urlPathStrips.length!=1)
             || (digitalLibraryList.indexOf(category)>=0)){
                category=urlPathStrips[urlPathStrips.length-2].toUpperCase();
                if(digitalLibraryList.indexOf(category)>=0){
                    urlMapper[category]();
                }else{
                    pageNotFound();
                }
            }else{
                urlMapper[category]();
            }
        }
        else{
            pageNotFound();
        }
    }
    urlMapper[program] = function(){
        institute.openProgramPage(urlPathStrips[2]);
    }

    urlMapper[aiims] = function(){
        institute.aiims();
    }
    urlMapper[jeemain] = function(){
        institute.jeemain();
    }
    urlMapper[jeeadvanced] = function(){
        institute.jeeadvanced();
    }
    urlMapper[neet] = function(){
        institute.neet();
    }
    urlMapper[bitsat] = function(){
        institute.bitsat();
    }
    urlMapper[subject] = function(){
        institute.openSubjectPage(urlPathStrips[2],urlPathStrips[4]);
    }
    urlMapper[chapter] = function(){
        institute.openChapterPage(urlPathStrips[2],urlPathStrips[4],urlPathStrips[6]);
    }
    urlMapper[test] = function(){
        institute.openTestPage(urlPathStrips[1]);
    }
    urlMapper[discussion] = function(){
        institute.openDiscussionPage(urlPathStrips[1]);
    }
    urlMapper[attempt] = function(){
        institute.openLibraryWithRefresh();
    }
    urlMapper[pretest] = function(){
        institute.openPretestPage(urlPathStrips[1]);
    }
    urlMapper[document] = function(){
        institute.openDocumentPage(urlPathStrips[1]);
    }
    urlMapper[video] = function(){
        if(isEmpty(urlParams)){
            institute.openVideoPage(urlPathStrips[1]);
        }else{
            institute.openVideoPageWithModule(urlPathStrips[1],urlParams.moduleId);
        }
    }
    urlMapper[referral] = function() {
        institute.getRecentPage();
    }
    urlMapper[activities] = function(){
        institute.activities.open();
    }
    urlMapper[discussions] = function(){
        institute.doubts.open();
    }
    urlMapper[library] = function(){
        institute.openLibraryWithRefresh();
    }
    urlMapper[analytics] = function(){
        institute.openResultAnalytics();
    }
    urlMapper[programs] = function(){
        institute.openInstAvailPrograms();
    }
    urlMapper[conversation] = function(){
        instHeader.openConversation();
    }
    urlMapper[notifications] = function(){
        institute.openAllNotifications();
    }
    urlMapper[profile] = function(){
        institute.instProfilePage();
    }
})(jQuery);


