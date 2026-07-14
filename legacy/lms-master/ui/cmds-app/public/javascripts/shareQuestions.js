var shareQuestionsBoard = new function() {
    var publishTimer;
    this.init = function(){
        $("#questionSharing").on("click",".collapsable",accordionOpenClose);
        $(".addMapping").on("click",addMapping);
        $(".addMoreMapping").on("click",addMoreMapping);
        $(".deleteMapping").on("click",deleteMapping);
        $(".shareMapping").on("click",shareMapping);
        $(".visibleCheckbox").on("change",visibleCheckboxChange);
        openFirstAccordion("");
    }

    var addMapping = function(){
        var parentOrgId = $("#learnpediaOrgId").val();
        var targetOrgId = $(".orgSelect option:selected").data('orgId');
        var orgName = $(".orgSelect option:selected").data('orgName');
        if(targetOrgId == undefined || targetOrgId == null){
            showError("Please choose an organization");
            return ;
        }
        params = {
            parentOrgId:parentOrgId,
            targetOrgId:targetOrgId,
            orgName:orgName
        };
        addMappingsPopup(params);
    }

    var addMoreMapping = function(){
        var parentOrgId = $("#learnpediaOrgId").val();
        var targetOrgId = $(this).data('orgId');
        var orgName = $(this).data('orgName');
        params = {
            parentOrgId:parentOrgId,
            targetOrgId:targetOrgId,
            orgName:orgName
        };
        addMappingsPopup(params);

    }

    var deleteMapping = function(){
        var params = getParams($(this));
        showVYesNoBox("Are you sure to delete", null, function(state) {
            if (state) {
                vReq.post("/QrBoards/deleteMapping",params,function(data){
                    if(data.errorCode != null){
                        showError(data.errorMessage);
                    }
                    if(data.result.saved){
                        shareQuestions();
                    }
                });
            }
        });
    }

    var shareMapping = function(){
        var params = getParams($(this));
        params.reSync = $(this).data("resync");
        showVYesNoBox("Are you sure to share", null, function(state) {
            if (state) {
                if (publishTimer) {
                    clearInterval(publishTimer);
                }
                var popup = getcmdsPopupBody();
                popup.html($("#questionSharing").find(".shareMappingProgress").html());
                $.ajax({
                    url:"/QrBoards/shareMapping",
                    data:params,
                    timeout:600000,
                    type:"POST",
                    success:function(data){
                        var jobIdsType = {
                            "SCQ" : "",
                            "MCQ" : "",
                            "NUMERIC" : "",
                            "TEXT" : "",
                            "MATRIX":""
                        }
                        var jobIds = [];
                        if(data.errorCode != ""){
                            showError(data.errorCode);
                            closecmdsPopup();
                            return ;
                        }
                        var jobIdsOfEntities = data.result;
                        if(jobIdsOfEntities.length == 0){
                            showError("No questions to share");
                            closecmdsPopup();
                            return;
                        }
                        for(var i =0 ;i<jobIdsOfEntities.length;i++){
                            if(jobIdsOfEntities[i].QType == "SCQ"){
                                jobIdsType.SCQ = jobIdsOfEntities[i].jobId;
                                $(".SCQProgress").removeClass("nonner");
                            }
                            if(jobIdsOfEntities[i].QType == "MCQ"){
                                jobIdsType.MCQ = jobIdsOfEntities[i].jobId;
                                $(".MCQProgress").removeClass("nonner");
                            }
                            if(jobIdsOfEntities[i].QType == "TEXT"){
                                jobIdsType.TEXT = jobIdsOfEntities[i].jobId;
                                $(".TEXTProgress").removeClass("nonner");
                            }
                            if(jobIdsOfEntities[i].QType == "NUMERIC"){
                                jobIdsType.NUMERIC = jobIdsOfEntities[i].jobId;
                                $(".NUMERICProgress").removeClass("nonner");
                            }
                            if(jobIdsOfEntities[i].QType == "MATRIX"){
                                jobIdsType.MATRIX = jobIdsOfEntities[i].jobId;
                                $(".MATRIXProgress").removeClass("nonner");
                            }
                            jobIds.push(jobIdsOfEntities[i].jobId);
                        }
                        if (jobIds.length > 0) {
                            publishStatusUpdater(jobIds,jobIdsType);
                            publishTimer = setInterval(function() {
                                publishStatusUpdater(jobIds,jobIdsType);
                            }, 5000);
                        }
                    },
                    error:function(xhr,textStatus){
                        showError("Something went wrong");
                    }
                });
                // vReq.post("/QrBoards/shareMapping",params,function(data){
                //     var jobIdsType = {
                //         "SCQ" : "",
                //         "MCQ" : "",
                //         "NUMERIC" : "",
                //         "TEXT" : "",
                //         "MATRIX":""
                //     }
                //     var jobIds = [];
                //     var jobIdsOfEntities = data.result;
                //     if(jobIdsOfEntities.length == 0){
                //         showError("No questions to share");
                //         closecmdsPopup();
                //         return;
                //     }
                //     for(var i =0 ;i<jobIdsOfEntities.length;i++){
                //         if(jobIdsOfEntities[i].QType == "SCQ"){
                //             jobIdsType.SCQ = jobIdsOfEntities[i].jobId;
                //             $(".SCQProgress").removeClass("nonner");
                //         }
                //         if(jobIdsOfEntities[i].QType == "MCQ"){
                //             jobIdsType.MCQ = jobIdsOfEntities[i].jobId;
                //             $(".MCQProgress").removeClass("nonner");
                //         }
                //         if(jobIdsOfEntities[i].QType == "TEXT"){
                //             jobIdsType.TEXT = jobIdsOfEntities[i].jobId;
                //             $(".TEXTProgress").removeClass("nonner");
                //         }
                //         if(jobIdsOfEntities[i].QType == "NUMERIC"){
                //             jobIdsType.NUMERIC = jobIdsOfEntities[i].jobId;
                //             $(".NUMERICProgress").removeClass("nonner");
                //         }
                //         if(jobIdsOfEntities[i].QType == "MATRIX"){
                //             jobIdsType.MATRIX = jobIdsOfEntities[i].jobId;
                //             $(".MATRIXProgress").removeClass("nonner");
                //         }
                //         jobIds.push(jobIdsOfEntities[i].jobId);
                //     }
                //     if (jobIds.length > 0) {
                //         publishStatusUpdater(jobIds,jobIdsType);
                //         publishTimer = setInterval(function() {
                //             publishStatusUpdater(jobIds,jobIdsType);
                //         }, 5000);
                //     }

                // });
            }
        });
    }

    var publishStatusUpdater = function(jobIds,jobIdsType) {
        $.get("/qrprograms/getPublishStatus", {jobIds: jobIds}, function(data) {
            var result = data.result.list;
            for (var k = 0; k < result.length; k++) {
                var entityResult = result[k];
                var entityJobId = entityResult.jobId;
                var percent = 0;
                var maxWidth = 0;
                switch(entityJobId){
                    case jobIdsType.SCQ :
                    percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                    $(".SCQProgress").find(".publishPercent").html(percent + "%");
                    $(".SCQProgress").find(".publishCount").html(entityResult.numCompletedSteps + "/" + entityResult.numOfSteps);
                    maxWidth = $(".SCQProgress").find(".publishGrayBar").width();
                    $(".SCQProgress").find(".publishGreenBar").width(percent * maxWidth / 100);
                    break;
                    case jobIdsType.MCQ :
                    percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                    $(".MCQProgress").find(".publishPercent").html(percent + "%");
                    $(".MCQProgress").find(".publishCount").html(entityResult.numCompletedSteps + "/" + entityResult.numOfSteps);
                    maxWidth = $(".MCQProgress").find(".publishGrayBar").width();
                    $(".MCQProgress").find(".publishGreenBar").width(percent * maxWidth / 100);
                    break;
                    case jobIdsType.NUMERIC :
                    percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                    $(".NUMERICProgress").find(".publishPercent").html(percent + "%");
                    $(".NUMERICProgress").find(".publishCount").html(entityResult.numCompletedSteps + "/" + entityResult.numOfSteps);
                    maxWidth = $(".NUMERICProgress").find(".publishGrayBar").width();
                    $(".NUMERICProgress").find(".publishGreenBar").width(percent * maxWidth / 100);
                    break;
                    case jobIdsType.TEXT :
                    percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                    $(".TEXTProgress").find(".publishCount").html(entityResult.numCompletedSteps + "/" + entityResult.numOfSteps);
                    $(".TEXTProgress").find(".publishPercent").html(percent + "%");
                    maxWidth = $(".TEXTProgress").find(".publishGrayBar").width();
                    $(".TEXTProgress").find(".publishGreenBar").width(percent * maxWidth / 100);
                    break;
                    case jobIdsType.MATRIX :
                    percent = Math.round(entityResult.numCompletedSteps * 100 / entityResult.numOfSteps);
                    $(".MATRIXProgress").find(".publishCount").html(entityResult.numCompletedSteps + "/" + entityResult.numOfSteps);
                    $(".MATRIXProgress").find(".publishPercent").html(percent + "%");
                    maxWidth = $(".MATRIXProgress").find(".publishGrayBar").width();
                    $(".MATRIXProgress").find(".publishGreenBar").width(percent * maxWidth / 100);
                }
                var removeJobId = false;
                if (entityResult.errorCode !== "") {
                    var errorMsg = entityResult.errorMessage ? entityResult.errorMessage : entityResult.errorCode;
                    showError("Something went wrong, Please try again");
                    removeJobId = true;
                }
                else if (entityResult.numCompletedSteps >= entityResult.numOfSteps) {
                    removeJobId = true;
                }
                if (removeJobId) {
                    var index = jobIds.indexOf(entityJobId);
                    if (index !== -1) {
                        jobIds.splice(index, 1);
                    }
                }
            }
            if (jobIds.length === 0) {
                try {
                    clearInterval(publishTimer);
                    console.log("Shared Successfully");
                    shareQuestions();
                } catch (err) {
                    console.error("err in clearing interval")
                }
            }
        });
    }

    var visibleCheckboxChange =function(){
        var parentOrgId = $("#learnpediaOrgId").val();
        var sharedToOrgId = $(this).data('sharedToOrgId');
        var params = {
            "parentOrgId" : parentOrgId,
            "sharedToOrgId" : sharedToOrgId
        }
        params.visible = $(this)[0].checked
        vReq.post("/QrBoards/visibleMapping",params,function(data){
            if(data.errorCode != null){
                showError(data.errorMessage);
            }
            if(data.result.saved){
                var message = "";
                if(params.visible){
                    message = "Successfully Published";
                }
                else{
                    message = "Successfully Unpublished";
                }
                showMessage(message);
                // shareQuestions();
            }
        });
    }

    var addMappingsPopup = function(params){
        vReq.post("/QrBoards/addMappings",params,function(data){
            var popup = getcmdsPopupBody();
            popup.html(data);
            popup.find("#targetBoardName").html(params.orgName+" Boards");
            popup.find("#targetBoardName").attr("data-target-org-id",params.targetOrgId);
            popup.find("#parentBoardName").attr("data-parent-org-id",params.parentOrgId);
        })
    }

    var getParams = function(target){
        var parentOrgId = $("#learnpediaOrgId").val();
        var sharedToOrgId = target.closest("tr").data("sharedToOrgId");
        var parentBoardId = target.closest("tr").data("parentBoardId");
        var sharedToBoardId = target.closest("tr").data("sharedToBoardId");
        var params = {
            "parentOrgId":parentOrgId,
            "sharedToOrgId":sharedToOrgId,
            "parentBoardId":parentBoardId,
            "sharedToBoardId":sharedToBoardId
        }
        return params;
    }
    var accordionOpenClose = function(){
        if($(this).find(".openClose").text() == "  +  ")
            $(this).find(".openClose").text("  -  ");
        else
            $(this).find(".openClose").text("  +  ");

        $(this).nextUntil(".collapsable").toggleClass("collapse");
    }

    var openFirstAccordion = function(tableName){
        tableName = "sharedOrgBoardTable";
        setTimeout(function(){
           $("."+tableName+" tr.collapsable:first .openClose").text("  -  ");
           $("."+tableName+" tr.collapsable:first").nextUntil(".collapsable").toggleClass("collapse");
       },1000);
    }
}