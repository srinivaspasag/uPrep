var qrTestDone=new(function($){
    var clickEvent="click",bodyClickEvent="click.qrTestDone",previewTestPage,globalTestId,
    testType="TEST";
    
    this.init=function(params){
        previewTestPage=$("#previewTestPage"); 
        testType=params.testType;
        previewTestPage.off(clickEvent)
        .on(clickEvent,".showSubPreview",showSubPreview)
        .on(clickEvent, ".viewTestStats", viewTestStats)
        .on(clickEvent,".getAllSchedules",getAllSchedules)
        .on(clickEvent,".regenerateAnalytics",regenerateAnalyticsDialog)
        .on(clickEvent,".instItemStgs",openStngs)
        
        
        $("body").off(bodyClickEvent)
        .on(bodyClickEvent,".cmdsRemSchedule",cmdsRemSchedule)    
        .on(bodyClickEvent,".cmdsGetNewKey",cmdsGetNewKey)   
        .on(bodyClickEvent,".cmdsGenerateAllKeys",cmdsGenerateAllKeys)   
        .on(bodyClickEvent,".cmdsAddDowntime",cmdsAddDowntime)
        .on(bodyClickEvent,".confirmDowntime",confirmDowntime)
        .on(bodyClickEvent,".cancelDowntime",cancelDowntime)
        .on(bodyClickEvent,".addToScheduleList",addToScheduleList)
        .on(bodyClickEvent,".confirmAddSchedule",confirmAddSchedule)
        .on(bodyClickEvent,".cancelAddSchedule",cancelAddSchedule)   
        .on(bodyClickEvent,".cmdsRemScheduleConfirm",cmdsRemScheduleConfirm)

        
        
        loadMJEqns(previewTestPage.get(0));
        condenseText(previewTestPage.find(".entityEditableDesc"),100);
        $("#previewTestPage .TPDSubDomain").off('click')
                          .on("click",TPDSubDomain);

    }; 
    var startLoader=showTopLoader;    
    var stopLoader=hideTopLoader;
    var closePopup=closecmdsPopup;
    var postReq=vReq.post;
    var getReq=vReq.get;
    
    
    var showSubPreview=function(){
        var $this=$(this);
        changeActiveClass($this,"gButtonActive");
        startLoader();
        var testId=$this.closest(".sec1002").data("testId");
        var params={testId:testId,assignmentId:testId,
            brdId:$this.data("courseId"),start:0,size:50,
            target:$this.data("targetView")};
        var successFn=function(data){
            stopLoader();
            var targetDiv=$this.closest(".sec1002").children(".LMHandlerDiv");
            targetDiv.html(data);            
            loadMJEqns(previewTestPage.get(0));
        };
        getReq("/qrtests/preview"+toCamelCase(testType)+"Quesns",params,successFn);        
    };
    var viewTestStats = function() {
        var targetHTML = $(this).closest(".testPageDetails").find(
                ".testPageMoreDet").children().clone(true);

        getcmdsPopupBody().html(targetHTML);
    };    
    //for add/get/remove shcdueles and their leys along with downtime addition
    var getAllSchedules=function(){
        var $this=$(this),testId=$this.data("globalTestId");
        startLoader();
        var successFn=function(data){
            stopLoader();
            getcmdsPopupBody(750).html(data);
            globalTestId=testId;
        }
        getReq("/qrtests/getAllSchedules",{testId:testId},successFn);          
    }
    var delScheduleId,delScheduleTr;
    var cmdsRemSchedule=function(){
        var tr=$(this).closest("tr");
        delScheduleTr=tr;
        delScheduleId=tr.data("scheduleId");
        var trs=tr.siblings("tr"),hasFutureSchedule=false;
        var currentTime=new Date().getTime();
        for(var p=0;p<trs.length;p++){
            var endTime=trs[p].data("endTime");
            if(currentTime<endTime){
                hasFutureSchedule=true;
                break;
            }
        }
        if(trs.length>0&&!hasFutureSchedule){
            fillcmdsPopup("cmdsRemScheduleConfirm","removeScheduleSample");
        }else{
            cmdsRemScheduleConfirm();
        }
    }
    var cmdsRemScheduleConfirm=function(){
        startLoader();
        var successFn=function(data){
            stopLoader();
            delScheduleTr.remove();
        }
        postReq("/qrtests/removeSchedule",{scheduleId:delScheduleId},successFn);                        
    }
    var cmdsGetNewKey=function(){
        var tr=$(this).closest("tr");
        startLoader();
        var successFn=function(data){
            stopLoader();
            putNewKeys(tr.closest("table"),data);
        }
        postReq("/qrtests/generateNewKeys",{scheduleId:tr.data("scheduleId"),testId:globalTestId},successFn);                                 
    }
    var cmdsGenerateAllKeys=function(){
        startLoader();
        var $this=$(this);
        var successFn=function(data){
            stopLoader();
            putNewKeys($this.closest("#cmdsPopup").find("table"),data);
        }
        postReq("/qrtests/generateNewKeys",{testId:globalTestId},successFn);                  
    }    
    var putNewKeys=function(table,data){
        var schedules=data.result.schedules;
        for(var p=0;p<schedules.length;p++){
            var k=schedules[p];
            table.find(".scheduleKey_"+k.scheduleId).html(k.key);
        }
    }
    var cmdsAddDowntime=function(){
        var $this=$(this),popup=$this.closest(".cmdsPopupBody");
        $this.addClass("nonner");
        $this.siblings(".cmdsDowntimeDiv").html(popup.find(".addDowntimeSample").html());        
    }
    var cancelDowntime=function(e,downtimeDiv){        
        downtimeDiv=downtimeDiv||$(this).closest(".cmdsDowntimeDiv");
        downtimeDiv.html("");
        downtimeDiv.siblings(".cmdsAddDowntime").removeClass("nonner");
    }
    var confirmDowntime=function(e){
        var $this=$(this),downtimeDiv=$this.closest(".cmdsDowntimeDiv");
        var duration=getHrsMinsSecs(downtimeDiv);   
        startLoader();
        $this.text("Adding..");
        var successFn=function(data){
            var extraTimeDiv=$this.closest("tr").find(".scheduleDowntimeTd");
            if(duration==0)extraTimeDiv.html("-");
            else{
                var timeObj=getTimeObj(duration);
                extraTimeDiv.html(timeObj.hr+":" + timeObj.min+":" + timeObj.sec);
            }
            stopLoader();
        }
        var completeFn=function(){            
            cancelDowntime(e,downtimeDiv);                
        }
        postReq("/qrtests/addDowntime",{scheduleId:downtimeDiv.closest("tr")
            .data("scheduleId"),extratime:duration},successFn,null,completeFn);                         
    }
    var addToScheduleList=function(){
        var popup=$(this).closest("#cmdsPopup");
        var entity=popup.find(".scheduleSample").html(); 
        popup.find(".addScheduleDiv").html(entity);        
    }

    var regenerateAnalyticsDialog = function(){
        var message = "Are you sure to regenerate ? <br><br>This will regenerate analytics for every student those who have attempted this test";
        showVYesNoBox(message, null, function(state) {
            if (state) {
                regenerateAnalytics();
            }
        });
        var regenerateAnalytics = function(){
            var entityId = $(".regenerateAnalytics").data("testId");
            var orgId = $(".regenerateAnalytics").data("orgId");
            var params = {
                id:entityId,
                entity:{
                    type:"TEST",
                    id:entityId
                },
                orgId:orgId
            };
            vReq.post("/qrtests/regenerateAnalytics",params,function(data){
                console.log(data);
                if(data.errorCode != ""){
                    console.log(data.errorCode);
                    return ;
                }
                if(data.result.success == true){
                    showMessage(""+data.result.message);
                }
            });
        }
    }
    var TPDSubDomain = function(){
        var $this = $(this);
        changeActiveClass($this, "gButtonActive");
        $this.closest(".cmdsPopupBody").find(".TPDSubDomainDiv").eq(
                $this.index()).removeClass("nonner").siblings(
                ".TPDSubDomainDiv").addClass("nonner");
    }
    var cancelAddSchedule=function(){
        $(this).closest(".addScheduleDiv").html("");
    }
    var openStngs = function(){
        $(this).siblings(".instItemStgsDropped").toggleClass("nonner");
    }
    var confirmAddSchedule=function(){
        var $this=$(this),addScheduleDiv=$this.closest(".addScheduleDiv"),
        popup=addScheduleDiv.closest("#cmdsPopup");        
        var entitiesResult=getScheduleEntities(addScheduleDiv);
        var schedule=entitiesResult.scheduleEntites[0];
        var errDiv=addScheduleDiv.find(".addScheduleErr");
        if(entitiesResult.hasError){
            errDiv.text(entitiesResult.errText);
            return;
        }else if(schedule.endTime<new Date().getTime()){
            errDiv.text("Add a valid schedule. End Time should be greater than the current time.");
            return;
        }
        var successFn=function(data){
            popup.find(".noScheduleTr").remove();
            addScheduleDiv.html("");
            var result=data.result,scheduleId=result.scheduleId;
            var scheduleEntity=makeHTMLTag("tr").data("scheduleId",scheduleId)
            .html('<td class="grayHead scheduleTimeTd"></td>\n\
            <td class="scheduleKeyTd scheduleKey_'+scheduleId+'">'+result.key+'</td>\n\
            <td class="scheduleDowntimeTd centry">-</td>\n\
            <td><a class="smally blocky cmdsGenerateKey">Generate New key</a>\n\
                <a class="smally blocky cmdsRemSchedule">Remove Schedule</a>\n\
                <a class="smally blocky cmdsAddDowntime">Add Downtime</a>\n\
                <div class="cmdsDowntimeDiv"></div></td>');
            scheduleEntity.find(".scheduleTimeTd").html(new Date(schedule.startTime).toLocaleString()+
            " to "+new Date(schedule.endTime).toLocaleString());
            var target=popup.find("tbody").find("tr").last();
            if(target.length>0){
                scheduleEntity.insertAfter(target);
            }else{
                popup.find("tbody").html(scheduleEntity);
            }            
        }
        postReq("/qrtests/addSchedule",{testId:globalTestId,schedule:schedule},
        successFn);                  
    }
}) (jQuery);
