var myClassroomConnect= new function(){
    var domain = vedantuHub;
    var requestUrl;
    if(domain == "web-app"){
        requestUrl = "Institute";
    }
    else if(domain == "cmds-app"){
        requestUrl = "QrSchedule";
    }
    this.init = function(){
        if(domain!=null && domain!= undefined){
            if(domain == "web-app"){
                var progInfo = getDistinctData();
                progInfo = institute.readUrlForProgInfo();
                $(".instLibarySelMyProg .nDropDown").off("change")
                                                    .on("change",programChanged);
                $(".instLibarySelMyCenter .nDropDown").off("change")
                                                .on("change",centerChanged);
                $(".instLibarySelMySection .nDropDown").off("change")
                                                    .on("change",sectionChanged);
            }
            myClassroomConnect.resetProgramme();
        }
    }

    var programChanged = function(){
        setTimeout(function(){
            myClassroomConnect.resetProgramme();
        },100);
        var progId = $(this).data("value");
        urlQueryHelper.push("program",progId);
    };
    var centerChanged = function(){
        setTimeout(function(){
            myClassroomConnect.resetProgramme();
        },100);
        var centerId = $(this).data("value");
        urlQueryHelper.push("center",centerId);
    };
    var sectionChanged = function(){
        setTimeout(function(){
            myClassroomConnect.resetProgramme();
        },100);
        var sectionId = $(this).data("value");
        urlQueryHelper.push("section",sectionId);
    };

    var getDistinctData = function(){
        var progId = $(".instLibarySelMyProg").find(".nDropDown").data("value");
        var centerId = $(".instLibarySelMyCenter").find(".nDropDown").data("value");
        var sectionId = $(".instLibarySelMySection").find(".nDropDown").data("value");
        if(progId){
            return {"progId":progId,"center":centerId,"section":sectionId};
        }
        return {"progId":"","center":"","section":""};
    }

    this.resetProgramme = function(){
        var params = {};
        if(domain == "web-app"){
            var data = getDistinctData();
            params['programId'] = data["progId"];
            if(data["section"]){
                params['sectionId'] = data["section"];
            }else{
                $(params).removeProp('sectionId');
            }
            if(data["center"]){
                params['centerId'] = data["center"];
            }else{
                $(params).removeProp('centerId');
            }
            if(params['centerId'] == null){
                $("#errorHandler").html("Please choose center");
            }
            if(params['sectionId'] == null){
                $("#errorHandler").html("Please choose section");
            }

            if(params['sectionId'] == null && params['centerId'] == null){
                $("#errorHandler").html("Please choose center and section");
            }
        }

        if(domain == "cmds-app"){
            params['programId'] = qrProgram.programId;
            params['sectionId'] = qrProgram.sectionId;
            params['centerId'] = qrProgram.centerId;
            if(params['programId'] == null || params['programId'] == "" || params['programId'] == undefined){
                showError("Please choose program");
                return;
            }
            if(params['centerId'] == null || params['centerId'] == "" || params['centerId'] == undefined){
                showError("Please choose center");
                return;
            }
            if(params['sectionId'] == null || params['sectionId'] == "" || params['sectionId'] == undefined){
                showError("Please choose section");
                return;
            }
        }
        $("#calendar").html("");
        var fromDate = new Date();
        var firstDay = new Date(fromDate.getFullYear(), fromDate.getMonth(), 1);
        if(params['programId'] != null && params['sectionId'] != null && params['centerId'] !=null){
            $("#errorHandler").html("");

            initializeCalendar(params);
        }
    }

    var initializeCalendar = function(params){
        var calendarEl = document.getElementById("calendar");
        var calendar = new FullCalendar.Calendar(calendarEl, {
        plugins: [ 'interaction', 'dayGrid' ],
        eventColor: '#4CAF50',
        lazyFetching:true,
        eventLimit:2,
        datesRender : function (info) {
            params.month = info.view.currentStart.getTime();
            if(domain == "web-app"){
                params.orgId = $("#myInstitutePage").data("orgId");
            }
            if(domain == "cmds-app"){
                params.orgId = cmdsOrgId;
            }
            showTopLoader();
            vReq.get("/"+requestUrl+"/getSchedule",params,function(data){
                if(data.errorCode != ""){
                    showError("Something went wrong");
                    return ;
                }
                var calendarDays = data.result.days;
                var events = [];
                for(i=0;i<calendarDays.length;i++){
                    var actualDay = calendarDays[i];
                    events = constructEventJson(actualDay,events);
                }
                calendar.getEventSources().forEach(eventSource => {
                    eventSource.remove()
                });
                calendar.addEventSource(events);
                hideTopLoader();
            });
        },
        eventRender: function (info) {
            if ("htmlTitle" in info.event.extendedProps){
                info.el.firstChild.innerHTML = info.event.extendedProps.htmlTitle;
            }
            var today = calendar.getNow().getTime();
            var eventDate = info.event.start.getTime() + 86399000 ;;
            if(eventDate < today){
                $(info.el).addClass("past-event");
            }
            else if(eventDate >= today && eventDate - today < 86399000){
                $(info.el).addClass("current-event");
            }
        },
        eventClick:function(info){
            showTopLoader();
            var day = info.event.start;
            var programId = params.programId;
            var sectionId = params.sectionId;
            var centerId = params.centerId;
            var orgId = params.orgId;
            var month = params.month;
            var infoParams = {
                orgId:orgId,
                centerId:centerId,
                programId:programId,
                sectionId:sectionId,
                centerId:centerId,
                day:day.getTime(),
                month:month
            };
            vReq.get("/"+requestUrl+"/getScheduleDayInfo",infoParams,function(data){
                hideTopLoader();
                var presentDate = calendar.formatDate(day,{
                    month:'long',
                    year:'numeric',
                    day:'numeric',
                    weekday:'long'
                });
                var popup;
                if(domain == "web-app"){
                    popup = showVPopup(0.6);
                }
                else if(domain == "cmds-app"){
                    popup = getcmdsPopupBody();
                }
                popup.html(data);
                if(domain == "web-app"){
                    popup.closest(".vpopupHolder").addClass("scheduleInfoPopup");
                }
                else if(domain == "cmds-app"){
                    popup.closest("#cmdsPopup").addClass("qrScheduleInfoPopup");
                    $(".qrScheduleInfoPopup").find(".closePopupDiv").addClass("nonner");
                    popup.on("click",".closeCmdsPopupWIndow",function(){
                        closecmdsPopup();
                    });
                }
                $("#todayDate").html(presentDate);
                popup.find(".boardData").off("click")
                                        .on("click",changeBoardTab);
                popup.find(".removeDaySchedule").off("click")
                                                .on("click",removeDaySchedule);
                popup.find(".removeEntityFromSchedule").off("click")
                                                .on("click",removeEntityFromSchedule);

                function changeBoardTab(){
                    var activeBoardIndex = $(".boardActive").data("index");
                    var boardIndex = $(this).data("index");
                    if(activeBoardIndex != boardIndex){
                        $(".boardData").each(function(){
                            if($(this).data('index') == boardIndex){
                                $(this).addClass("boardActive");
                            }
                            else{
                                $(this).removeClass("boardActive");
                            }
                        });
                        $(".boardInfo").each(function(){
                            $(this).removeClass("active");
                            if($(this).data('index') == boardIndex){
                                $(this).removeClass("nonner").addClass("active");
                            }
                            else{
                                $(this).addClass("nonner");
                            }
                        });
                    }
                }

                function removeDaySchedule(){
                    showVYesNoBox("Are you sure you want to remove this day schedule",null,function(state){
                        if(state){
                            vReq.post("/"+requestUrl+"/removeDaySchedule",infoParams,function(data){
                                if(data.errorCode != ""){
                                    showError("Something went wrong");
                                    return ;
                                }
                                showMessage("Successfully removed schedule");
                                setTimeout(function(){
                                    refreshPage();
                                },2000);
                            });
                        }
                    });
                }

                function removeEntityFromSchedule(){
                    var entity = $(this).closest(".content").find(".entityLink");
                    var boardId = $(this).closest(".contentInfo").data("boardId");
                    var entityCmdsId = entity.data("cmdsId");
                    var entityId = entity.data("webId");
                    var entityType = entity.data("entityType");
                    var removeParams = infoParams;
                    removeParams.entityId = entityId;
                    removeParams.entityType = entityType;
                    removeParams.entityCmdsId = entityCmdsId;
                    removeParams.boardId = boardId;
                    showVYesNoBox("Are you sure you want to remove this entity from the day schedule",null,function(state){
                        if(state){
                            vReq.post("/"+requestUrl+"/removeSchedule",removeParams,function(data){
                                if(data.errorCode != ""){
                                    showError("Something went wrong");
                                    return ;
                                }
                                showMessage("Successfully removed entity");
                                setTimeout(function(){
                                    refreshPage();
                                },2000);
                            });
                        }
                    });
                }
            })
        }
    });
        calendar.render();
    }

    var constructEventJson = function(actualDay,events){
        for(j=0;j<actualDay.metadata.length;j++){
            var event = {
                title:"",
                start:"",
            }
            var metadata = actualDay.metadata[j];
            var count = calculateCount(metadata);
            event.htmlTitle = constructEventHtml(metadata,count);
            event.start = new Date(actualDay.date);
            events.push(event);
        }
        return events;
    }

    var calculateCount = function(metadata){
        var testCount =0;
        var videoCount =0;
        var docCount = 0;
        var moduleCount = 0;
        var count = 0;
        for(k=0;k<metadata.details.length;k++){
            var details = metadata.details[k];
            if(details.type === 'TEST'){
                testCount = details.count;
            }
            else if(details.type === 'MODULE'){
                moduleCount = details.count;
            }
            else if(details.type === 'DOCUMENT'){
                docCount = details.count;
            }
            else if(details.type === 'VIDEO'){
                videoCount = details.count;
            }
            count = videoCount+ docCount + moduleCount + testCount;
        }
        return {"testCount":testCount,"moduleCount":moduleCount,"docCount":docCount,"videoCount":videoCount,"count":count};
    }

    var constructEventHtml = function(metadata,count){
        var eventHtml;
        eventHtml ="<div class='eventDataHolder'>";
        eventHtml += "<div class='eventHolder' data-board-id='"+metadata.id+"' data-board-name='"+metadata.name+"' title='"+metadata.name+"'>"+metadata.name+"</div>";
        eventHtml+="<div class='contentHolder'>";
        if(count.testCount !=0 ){
            eventHtml+= "<span class='imgContentHolder testContent'><img src='/public/images/icons/test.png' title='Test'>"+"<span class='count'>" +count.testCount+"</span></span>";
        }
        if(count.moduleCount !=0){
            eventHtml+= "<span class='imgContentHolder moduleContent'><img src='/public/images/icons/module.png' title='Module'>"+ "<span class='count'>" +count.moduleCount+"</span></span>";
        }
        if(count.docCount !=0){
            eventHtml+= "<span class='imgContentHolder docContent'><img src='/public/images/icons/document.png' title='Document'>"+"<span class='count'>" +count.docCount+"</span></span>";
        }
        if(count.videoCount !=0){
            eventHtml+= "<span class='imgContentHolder videoContent'><img src='/public/images/icons/video.jpg' title='Video'>" + "<span class='count' >" + count.videoCount+"</span></span>";
        }
        eventHtml+="</div>";
        eventHtml+="</div>";
        return eventHtml;
    }
}