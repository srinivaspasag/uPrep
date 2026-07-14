var duplicates = new function(){
    var checkDuplicates = $("#checkDuplicates").data();
    var mcParams = {
        orgId:checkDuplicates.orgId,
        query:checkDuplicates.query,
        quesType:checkDuplicates.quesType,
        start:checkDuplicates.start,
        size:checkDuplicates.size,
        target:checkDuplicates.target
    };
    this.init = function(){
        var mcWidget = $(".mcWidget");
        var LMHandlerDiv = $(".LMHandlerDiv");
        $("#checkDuplicates").off("click")
                             .on("click",".deleteResources",deleteResources)
                             .on("click",".queryImage",queryImage)
                             .on("keyup",".queryInput",mcWidget,queryInput);
        initmcWidgetforCMDS(mcWidget, "/QrQuestions/checkDuplicates",
                    mcParams, false, true,afterLoad);
        highlight(mcParams.query);
        $(".rteTextDiv").addClass("rteTextDivStyled");
        loadMJEqns(LMHandlerDiv.get(0));
    };

    var queryInput = function(e){
        if(e.which == 13) {
            queryImage();
        }
    }

    var queryImage = function(){
        var query = $(".queryInput").val().trim();
        var oldquery = mcParams.query;
        if(query!="" && query!=undefined && query!= oldquery){
            mcParams.query = query;
            mcParams.firstTimeLoad = "true";
            vReq.post("/QrQuestions/checkDuplicates",mcParams,function(data){
                pushHistory(null, null, "/organization/"+cmdsOrgId+"/checkduplicates?includes=CMDSQUESTION&quesType=ALL&target="+"CHECK_DUPLICATES&firstTimeLoad=true&start=0&size=25&query="+query);
                $("#contentSectionHolder").html(data);
            });
        }
    }
    var deleteResources = function(){
        var checkStatus = checkedEntities.init();
        /*if (checkStatus.hasPublished) { ISSUE #749
         showError("You cannot delete published resources.<br>Uncheck them and try again.");
         return;
         }*/
        var confirmtxt = "<div>Are you sure to <b>delete</b> selected contents? can not be un-done!</div>";
        showVYesNoBox(confirmtxt, null, function(state) {
            if (state) {
                doDelete();
            }
        });
        function doDelete() {
            startLoader();
            var popup = checkStatus.prepareEntityClonesPopup("Delete Resources");
            cloneTableBody = popup.find(".entityCloneTable").children("tbody");
            cloneTableBody.children("tr.nonner").removeClass("nonner");
            var progressMsg = "deleting...";
            var entitiesById = [];
            for (var k = 0; k < checkStatus.entityIds.length; k++) {
                var entityId = checkStatus.entityIds[k];
                checkStatus.assignMsgInPopup(popup, entityId, progressMsg, "redTextColor big14");
                entitiesById[entityId] = checkStatus.entityList[k];
            }
            var params = {entities: checkStatus.srcEntities};
            var cbFn = function(data) {
                stopLoader();
                if (data && data.result && data.result.list.length > 0) {
                    var list = data.result.list;
                    for (var p = 0; p < list.length; p++) {
                        var item = list[p];
                        var entityId = item.content.id;
                        var msg = "deleted";
                        if (item.errorMessage && item.errorMessage != "null") {
                            msg = item.errorMessage;
                            $(entitiesById).removeProp(entityId);
                            entitiesById[entityId] = null;
                        } else {
                            $(entitiesById[entityId]).remove();
                        }
                        checkStatus.assignMsgInPopup(popup, entityId, msg, "redTextColor boldy");
                    }
                }
                if (entitiesById) {
                    var msg = "deleted";
                    for (entityId in entitiesById) {
                        if (!entitiesById[entityId])
                            continue;
                        checkStatus.assignMsgInPopup(popup, entityId, msg, "redTextColor boldy");
                        $(entitiesById[entityId]).remove();
                    }
                }
                resetcmdsCBoxes($(".mcWidget"));
                window.location.reload();
            };
            vReq.post("/qrresources/deleteResources", params, cbFn, cbFn);
        }
    }

    var afterLoad = function(){
        // highlight(mcParams.query);
    }

    var highlight = function(query){
        query = query + "";
        var queryArr = [];
        if(query!="" && query!=null){
            queryArr = query.split(" ");
            if(queryArr.length > 1){
                for(i=0;i<queryArr.length;i++){
                    $("#checkDuplicates .quesTextDivHolder").highlight(queryArr[i]);
                }
            }
            else{
                $("#checkDuplicates .quesTextDivHolder").highlight(query);
            }
        }
    }
}