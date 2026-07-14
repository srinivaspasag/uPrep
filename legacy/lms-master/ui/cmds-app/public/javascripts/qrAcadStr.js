var qrAcadStr = new (function($) {
    var clickEvent = "click", bodyClickEvent = "click.qrAcadStr", acadStrTable, acadStrPage,
            targetTable, addEditMemberProfile;
    this.init = function(params) {
        acadStrTable = $("#acadStrTable");
        acadStrPage = $("#acadStrPage");
        targetTable = params.targetTable;
        acadStrContext = "EDIT_ACAD_STR";
        if (params.addEditMemberProfile) {
            addEditMemberProfile = params.addEditMemberProfile;
        }


        if (acadStrPage.length > 0) {
            acadStrPage.on(clickEvent, ".addCenter", addCenter).on(clickEvent,
                    ".editAcadStr", editAcadStr).on(clickEvent,
                    ".assignCoursesInAcadStr", assignCoursesInAcadStr)
        }

        acadStrTable.off(clickEvent).on(clickEvent, ".addDept", addDept).on(
                clickEvent, ".addProgram", addProgram).on(clickEvent,
                ".addSection", addSection).on(clickEvent,
                ".addProgramToCenter", addProgramToCenter).on(clickEvent,
                ".addCoursesToProgram", addCoursesToProgram).on(clickEvent,
                ".removeProgMapping", removeProgMapping).on(clickEvent,
                ".getDepts", getAcadEntities.DEPARTMENT).on(clickEvent,
                ".getProgramsOfDept", getProgramsOfDept).on(clickEvent,
                ".getCentersOfProgram", getCentersOfProgram).on(clickEvent,
                ".getCoursesOfProgram", getCoursesOfProgram).on(clickEvent,
                ".getSectionsOfCenter", getSectionsOfCenter).on(clickEvent,
                ".selectSectionForProgChange", selectSectionForProgChange).on(
                clickEvent, ".editASTItem", editASTItem).on(clickEvent,
                ".removeASTItem", removeASTItem).on(clickEvent,
                ".reactivateASTItem", reactivateASTItem)

                .off("keyup").off("focus").off("blur").on("keyup", ".ASTSearchInput",
                astSearchInput).on("focus", ".ASTSearchInput",
                astSearchInputFocus).on("blur", ".ASTSearchInput",
                astSearchInputBlur)

        $("#leftSection").off(clickEvent).on(clickEvent, ".editASTItem",
                editASTItem).on(clickEvent, ".removeASTItem", removeASTItem)
                .on(clickEvent, ".reactivateASTItem", reactivateASTItem)

        $("body").off(bodyClickEvent).on(bodyClickEvent, ".addDeptSubmit",
                addDeptSubmit).on(bodyClickEvent, ".addCenterSubmit",
                addCenterSubmit).on(bodyClickEvent, ".addProgramSubmit",
                addProgramSubmit).on(bodyClickEvent, ".addSectionSubmit",
                addSectionSubmit).on(bodyClickEvent, ".addProgToCenterSubmit",
                addProgToCenterSubmit).on(bodyClickEvent,
                ".addCoursesToProgSubmit", addCoursesToProgSubmit).on(
                bodyClickEvent, ".editASTItemSubmit", editASTItemSubmit).on(
                bodyClickEvent, ".removeReactivateSubmit",
                removeReactivateSubmit)
                .on(clickEvent,"#thumbnail",imageClick);

        // var tdWidth=(acadStrTable.width()-364)/2
        // acadStrTable.find(".ASTDeptsTd,.ASTProgramsTd,.ASTDeptsHeadTd,.ASTProgramsHeadTd")
        // .width(tdWidth);
        fixLeftSec();
    };

    function imageClick(){
      id = $(this).attr("id");
      if (window.qq) {
          createUploaderQuesAdd(id);
      } else {
          fetchScripts([{fname: "widgets/fileuploader.js", cb: createUploaderQuesAdd(id)}]);
      }
    };

    function createUploaderQuesAdd(id) {
        var params;
        var uploader = new qq.FileUploader({
          element: document.getElementById(id),
          action: '/Application/makeFile',
          debug: true,
          params: {
              uploadFileParamName: "imageFile",
          },
          onComplete:function(id, fileName, responseJSON){
            var obj = $.parseJSON(responseJSON);
            if(obj["success"] === true){
             $.post("/Application/uploadImage",{uploadFileParamName:"imageFile",myUserId:USERID,qqfile:fileName},function(data){
                if(data.result.uploaded){
                    var uuid = data.result.uuid;
                    var imagepath = $(data.result.imgHtml).attr('src');
                    $(".thumbnail .imageHolder img").attr("src", imagepath);
                    $(".thumbnail .imageHolder img").attr("uuid", uuid+".jpg");
                    console.log(uuid);
                }
                else{
                    showError("problem with image");
                }
            });
         }          
               else{
                showError("something went wrong");
                }
                
            }
        });
    }

    // var uploadIcon = function(){
    //     var parDiv = $(".thumbnail");
    //     animHolder = parDiv.find(".instChngPicAnim");
    //     btnHolder = parDiv.find(".instChngPicHold");
    //     if(!btnHolder.get(0)){ return;}
    //     parDiv.on("mouseenter",".instPrPicHolder",showOnHover)
    //     btnHolder.on("mouseenter",showOnHover);
    //     parDiv.on("mouseleave",".instPrPicHolder",hideOnHover)
    //     btnHolder.on("mouseleave",hideOnHover);
    //     onFileAvailable();
    // };
    //     var showOnHover = function(e){
    //         btnHolder.addClass("hovered");  
    //     };
    //     var hideOnHover = function(e){
    //         btnHolder.removeClass("hovered");   
    //     };
    //     var onFileAvailable = function(){
    //         qqVarForUpload= new qq.FileUploader({
    //                         element: btnHolder.get(0),
    //                         action: '/profile/uploadProfilePic',
    //                         debug: true,
    //                         sizeLimit : 5*1024*1024,
    //                         allowedExtensions : ["jpg", "gif", "png"],
    //                         multiple : false
    //                 });
    //                 qqVarForUpload.onUploadDone = onUploadDone;
    //                 qqVarForUpload.onUploadProgress = onUploadProgress;
    //                 btnHolder.find(".qq-upload-list").addClass("nonner");
    //                 var uploadButton=btnHolder.find(".qq-upload-button");
    //                 var uploadDropArea=btnHolder.find(".qq-upload-drop-area");
    //                 uploadButton.data("onFileChosen",onFileChosen);
    //                 uploadDropArea.data("onFileChosen",onFileDropped);
    //                 putMimeTypes(uploadButton);
    //                 uploadButton.addClass("uploadProfilePicBtn");
    //                 btnHolder.find(".qq-button-title").html("change");  
    //     };
    //     var putMimeTypes = function(holder){
    //                 var file = $(holder).find(":file");
    //                     $(file).attr("accept","image/*");
    //         };
    //     var onUploadDone = function(id,fileName,response,statusCode){
    //         if(response && response.errorCode == "" && response.result.done){
    //             var imgUrl = response.result.thumbnail+"?timestamp="+(new Date()).getTime();
    //             var imgTag = parDiv.find(".instPrPic").attr("src",imgUrl);
    //             parDiv.find(".instPrPicHolder").data("prevUrl",imgUrl);
    //             $(".myInstProfilePic").attr("src",imgUrl);
    //             imgTag.error(function(){
    //                 $(this).attr("src",response.result.thumbnail);
    //             });
    //         }else{
    //             showError(i18nJS("FAILED_TO_UPLOAD_PROFILE_PIC"));
    //         }
    //         reset();
    //     };
    //     var onUploadProgress = function(percentDecimal,progressText,id,fileName){
    //         var op = 1-percentDecimal;
    //         op = op > 0 ? op : 0.2;
    //         animHolder.css({"background-color":"rgba(255,255,255,"+op+")"});
    //     };
    //     var onFileChosen = function(input){
    //         onFileSelected(input,"_onInputChange",input);
    //     };
    //     var onFileDropped = function(input){
    //         onFileSelected(input,"_uploadFileList",input.files);
    //     };
    //     var onFileSelected = function(input,qqCbFn,qqCbFnParams){
    //         animHolder.removeClass("nonner");
    //         btnHolder.addClass("nonner");
    //         var img = parDiv.find(".instPrPic");
    //         var file = input.files[0];
    //         var reader = new FileReader();
    //         reader.readAsDataURL(file);
    //             reader.onload = function(e) {
    //                     img.attr("src", e.target.result);
    //             var targetUserId = parDiv.data("profileUserId");
    //             var targetOrgMemberId = parDiv.data("profileMemberId");
    //             var orgData = getOrgRequestInfo();
    //             var params = {
    //                 targetOrgMemberId: targetOrgMemberId,
    //                         targetUserId: targetUserId, 
    //                 uploadFileParamName: "inputFile"
    //             };
    //             $.extend(params,orgData);
    //             qqVarForUpload.setParams(params);
    //                     qqVarForUpload[qqCbFn](qqCbFnParams);
    //             };
    //         reader.onerror = function(){
    //             reset();
    //             showError(i18nJS("FAILED_TO_LOAD_IMAGE"));
    //         };  
    //     };
    //     var reset = function(){
    //         btnHolder.removeClass("nonner");
    //         animHolder.css({"background-color":"rgba(255,255,255,0.9)"}).addClass("nonner");
    //         var img = parDiv.find(".instPrPic");
    //         var prevImg = parDiv.find(".instPrPicHolder").data("prevUrl");
    //         if(img.attr("src")!=prevImg){
    //             img.attr("src",prevImg);
    //         }
    //     };
    var postReq = vReq.post;
    var getReq = vReq.get;
    var createASTItemTarget;
    var astDeptsTd = "ASTDeptsTd";
    var astProgramsTd = "ASTProgramsTd";
    var astCentersTd = "ASTCentersTd";
    var astSectionsTd = "ASTSectionsTd";
    var astCoursesTd = "ASTCoursesTd";
    var acadStrContext = "EDIT_ACAD_STR";

    var getProgCentersClass = "getCentersOfProgram";
    var getProgCoursesClass = "getCoursesOfProgram";
    var editAcadStr = function() {
        acadStrContext = "EDIT_ACAD_STR";
        editAcadStrAssignCoursesUtil($(this), "CENTER");
    };
    var assignCoursesInAcadStr = function() {
        acadStrContext = "ASSIGN_COURSES";
        editAcadStrAssignCoursesUtil($(this), "COURSE");
    };
    var editAcadStrAssignCoursesUtil = function($this, targetEntityType) {
        changeActiveClass($this, "blueTabActiveNew");
        var courseTds = acadStrTable.find(".assignCourseTd");
        var acadStrTds = acadStrTable.find(".editAcadStrTd");
        var programsTds = acadStrTable.find(".ASTProgramsTd .ASTItem");
        if (acadStrContext === "ASSIGN_COURSES") {
            acadStrTds.addClass("nonner");
            courseTds.removeClass("nonner");
            programsTds.addClass(getProgCoursesClass).removeClass(getProgCentersClass);
        } else {
            acadStrTds.removeClass("nonner");
            courseTds.addClass("nonner");
            programsTds.addClass(getProgCentersClass).removeClass(getProgCoursesClass);
        }
        resetASTResultsTd([astCentersTd, astCoursesTd, astSectionsTd]);
        var acadParams = getAcadStrTableParams();
        if (acadParams.programId) {
            getAcadEntities[targetEntityType]();
        }
    };

    var addDept = function() {
        fillcmdsPopup("addDeptSubmit", "addEditAcadEntitySample");
        createASTItemTarget = acadStrTable.find(".ASTDeptsTd .ASTResultsDiv");
    };
    var addDeptSubmit = function() {
        var cbfn = function(data, requestParams) {
            putASTItem(data, "department", "getProgramsOfDept", requestParams);
        };
        addEditDeptProgSecCenterUtil($(this), "addDepartment", cbfn);
    };
    var getProgramsOfDept = function() {
        changeActiveClass($(this), "ASTItemActive");
        getAcadEntities.PROGRAM();
    };

    var addProgram = function() {
        createASTItemTarget = acadStrTable
                .find(".ASTProgramsTd .ASTResultsDiv");
        fillcmdsPopup("addProgramSubmit", "addProgramSample");
    };
    var addProgramSubmit = function() {
        var cbfn = function(data, requestParams) {
            putASTItem(data, "program", "getCentersOfProgram", requestParams);
        };
        addEditDeptProgSecCenterUtil($(this), "addProgram", cbfn);
    };

    var addCenter = function() {
        var popup = fillcmdsPopup("addCenterSubmit", "addEditAcadEntitySample");
        popup.find(".cmdsPopupHead").text("Add Center");
    };
    var addCenterSubmit = function() {
        // var popup=getPopupDiv($(this));
        // var params=getFormValues(popup);
        // if(params.hasError){
        // return;
        // }
        // var zip=params.zipCode;
        // if(zip.length!=6||!checkIntNum(zip)){
        // showcmdsPopupError(popup,"Please enter a correct zip code.");
        // return;
        // }
        // startLoader();
        // var successFn=function(){
        // closePopup();
        // stopLoader();
        // refreshPage();
        // };
        // postReq("/qracadStr/addCenter",params,successFn);
        addEditDeptProgSecCenterUtil($(this), "addCenter", refreshPage);
    };
    var getCentersOfProgram = function() {
        changeActiveClass($(this), "ASTItemActive");
        getAcadEntities.CENTER();
    };
    var addProgramToCenter = function() {
        addAcadEntityToProgramUtil($(this), "addProgToCenterSubmit",
                "addProgramToCenter");
    };
    var addProgToCenterSubmit = function() {
        var acadEntityIdsPojo = addAcadEntityToProgramSubmitUtil($(this));
        var addCenterIds = acadEntityIdsPojo.addAcadEntityIds;
        var removeCenterIds = acadEntityIdsPojo.removeAcadEntityIds;
        if (addCenterIds.length == 0 && removeCenterIds.length == 0) {
            showcmdsPopupError(
                    null,
                    "You have not added/removed any \n\
            center. Click on cancel to exit");
            return;
        }
        startLoader();
        var successFn = function() {
            stopLoader();
            closePopup();
            getAcadEntities.CENTER();
        };
        var params = getAcadStrTableParams();
        if (addCenterIds.length > 0) {
            params.centerIds = addCenterIds;
            postReq("/qracadstr/addProgramCenters", params, successFn);
        }
        if (removeCenterIds.length > 0) {
            params.centerIds = removeCenterIds;
            postReq("/qracadstr/removeProgramCenters", params, successFn);
        }
    };
    var removeProgMapping = function(e) {
        e.stopPropagation();
        var $this = $(this);
        var astItem = $this.closest(".ASTItem"), $data = astItem.data(), entityType = $data.entityType;
        showVYesNoBox("Are you sure to remove ?", null, function(state) {
            if (state) {
                doRemove();
            }
        });
        var doRemove = function() {
            var params = getAcadStrTableParams();
            startLoader();
            var successFn = function() {
                stopLoader();
                astItem.remove();
                resetASTResultsTd([astSectionsTd]);
            };
            var ids = [$data[entityType.toLowerCase() + "Id"]];
            if (entityType === "CENTER") {
                params.centerIds = ids;
                postReq("/qracadstr/removeProgramCenters", params, successFn);
            } else {
                params.courseIds = ids;
                postReq("/qracadstr/removeProgramCourses", params, successFn);
            }
        };
    };

    var getSectionsOfCenter = function() {
        changeActiveClass($(this), "ASTItemActive");
        getAcadEntities.SECTION();
    };
    var addSection = function() {
        createASTItemTarget = acadStrTable
                .find(".ASTSectionsTd .ASTResultsDiv");
        var popup = fillcmdsPopup("addSectionSubmit", "addEditAcadEntitySample");
        var learnpediaId = popup.find("#learnpediaId").val();
        var orgId = popup.find("#orgId").val();
        popup.find(".cmdsPopupHead").text("Add Section");
        popup.find(".descHolder").removeClass("nonner");
        if(learnpediaId == orgId){
            popup.find(".sectionPoint1").removeClass("nonner");
            popup.find(".sectionPoint2").removeClass("nonner");
            popup.find(".sectionPoint3").removeClass("nonner");
            popup.find(".sectionPoint4").removeClass("nonner");
            popup.find(".thumbnail").removeClass("nonner");
        }
    };
    var addSectionSubmit = function() {
        var cbfn = function(data, requestParams) {
            putASTItem(data, "section", "", requestParams);
        };
        addEditDeptProgSecCenterUtil($(this), "addSection", cbfn);
    };
    var selectSectionForProgChange = function() {
        changeActiveClass($(this), "ASTItemActive");
        var secTd = $(this).closest("." + astSectionsTd);
        changeActiveClass(secTd, "ASTResultsTdClicked");
    };

    var getCoursesOfProgram = function() {
        changeActiveClass($(this), "ASTItemActive");
        getAcadEntities.COURSE();
    };
    var addCoursesToProgram = function() {
        addAcadEntityToProgramUtil($(this), "addCoursesToProgSubmit",
                "addCoursesToProgram", {
                    type: "COURSE"
                });
    };

    var addCoursesToProgSubmit = function() {
        var programId = getAcadStrTableParams().programId;
        var courseIdsPojo = addAcadEntityToProgramSubmitUtil($(this));
        var addCourseIds = courseIdsPojo.addAcadEntityIds;
        var removeCourseIds = courseIdsPojo.removeAcadEntityIds;
        var params = {
            programId: programId
        };
        var successFn = function() {
            closePopup();
            stopLoader();
            getAcadEntities.COURSE();
        };
        if (addCourseIds.length > 0) {
            params.courseIds = addCourseIds;
            postReq("/qracadstr/addProgramCourses", params, successFn);
        }
        if (removeCourseIds.length > 0) {
            params.courseIds = removeCourseIds;
            postReq("/qracadstr/removeProgramCourses", params, successFn);
        }
        if (addCourseIds.length === 0 && removeCourseIds.length === 0) {
            successFn();
        }
    };

    var addAcadEntityToProgramUtil = function($this, submitClass, urlStrip,
            extraParams) {
        startLoader();
        var successFn = function(data) {
            stopLoader();
            var acadEntities = $this.closest(".ASTResultsHolder").find(
                    ".ASTItem");
            var popup = getcmdsPopupBody(null, null, {
                submitClass: submitClass
            }).html(data);
            for (var k = 0; k < acadEntities.length; k++) {
                var $data = acadEntities.eq(k).data();
                var acadEntityId = $data.centerId || $data.courseId;
                var cBox = popup.find(".gCBoxAcadEntity_" + acadEntityId);
                if (urlStrip !== "addCoursesToProgram") {
                    cBox.addClass(
                            "acadEntitySelected gCBoxNoMoreTick").removeClass(
                            "gCBox");
                } else {
//                    cBox.addClass("gCBoxChecked");
                    cBox.closest(".mvcItem").remove();
                }
            }
            if (popup.find(".mvcItem").length === 0 && urlStrip === "addCoursesToProgram") {
                closePopup();
                showMessage("There are no courses to choose from");
            }
        };
        extraParams = extraParams || {};
        var params = $.extend({
            start: 0,
            size: 200
        }, extraParams);
        getReq("/qracadstr/" + urlStrip, params, successFn);
    };
    var addAcadEntityToProgramSubmitUtil = function($this) {
        var popup = getPopupDiv($this);
        var addAcadEntityIds = [], removeAcadEntityIds = [];
        var gCBoxChecked = "gCBoxChecked";
        var cBoxes = popup.find(".mvcItem .gCBox");
        var selectedClass = "acadEntitySelected";
        for (var k = 0; k < cBoxes.length; k++) {
            var cBox = cBoxes.eq(k), val = cBox.data("value");
            if (cBox.hasClass(selectedClass) && !cBox.hasClass(gCBoxChecked)) {
                removeAcadEntityIds.push(val);
            } else if (!cBox.hasClass(selectedClass)
                    && cBox.hasClass(gCBoxChecked)) {
                addAcadEntityIds.push(val);
            }
        }
        return {
            addAcadEntityIds: addAcadEntityIds,
            removeAcadEntityIds: removeAcadEntityIds
        };
    };

    var astSearchInput = function(e) {
        var $this = $(this), query = $this.val().trim();
        var entityType = $this.data("entityType");
        if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))
                && query) {
            getAcadEntities[entityType]({
                query: query
            });
        } else if (query.length == 0) {
            getAcadEntities[entityType]();
        }
    };
    var astSearchInputFocus = function() {
        getASTItemsUtil = searchastResultsTd;
    };
    var astSearchInputBlur = function() {
        getASTItemsUtil = getASTItemsUtilFn;
    };
    var searchastResultsTd = function(urlStrip, targetClass, queryParams) {
        var astItems = acadStrTable.find("." + targetClass).find(".ASTItem");
        var query;
        if (queryParams && queryParams.query) {
            query = queryParams.query.toLowerCase();
        }
        if (query) {
            for (var k = 0; k < astItems.length; k++) {
                var astItem = astItems.eq(k);
                var name = astItem.attr("title").toLowerCase();
                if (name.indexOf(query) > -1) {
                    astItem.show();
                } else {
                    astItem.hide();
                }
            }
        } else {
            astItems.show();
        }
    };
    var editRemoveASTItemEntityType, editRemoveASTItemEl;
    var editASTItem = function(e) {
        e.stopPropagation();
        var astItem = $(this).closest(".ASTItem"), astItemData = astItem.data();
        var entityType = astItemData.entityType.toLowerCase();
        editRemoveASTItemEntityType = entityType.toUpperCase();
        var name = astItemData[entityType + "Name"];
        var code = astItemData[entityType + "Code"];
        var desc = astItemData[entityType + "Desc"];
        var descriptionPoints = astItemData[entityType + "Descriptionpoints"];
        var thumbnail = astItemData[entityType + "Thumbnail"];
        var popup = fillcmdsPopup("editASTItemSubmit",
                "addEditAcadEntitySample").closest("#cmdsPopup");
        popup.find(".cmdsPopupHead").text("Edit " + entityType);
        popup.find(".formInput[name=name]").val(name);
        popup.find(".formInput[name=code]").val(code);
        if (entityType == "section") {
            popup.find(".descHolder").removeClass("nonner");
            popup.find(".sectionPoint4").removeClass("nonner");
            popup.find(".sectionPoint3").removeClass("nonner");
            popup.find(".sectionPoint2").removeClass("nonner");
            popup.find(".sectionPoint1").removeClass("nonner");
            popup.find(".thumbnail").removeClass("nonner");
            popup.find(".formInput[name=desc]").val(desc);
            popup.find(".formInput[name=point1]").val(descriptionPoints[0]);
            popup.find(".formInput[name=point2]").val(descriptionPoints[1]);
            popup.find(".formInput[name=point3]").val(descriptionPoints[2]);
            popup.find(".formInput[name=point4]").val(descriptionPoints[3]);
            popup.find(".thumbnail .imageHolder img").attr("src",thumbnail);
        }
        if (entityType == "program"){
            popup.find(".endDate").removeClass("nonner");
            var periodend = astItemData[entityType + "Periodend"];
            var date = new Date(periodend);
            setvChooseValue( popup.find('.datevChoose'), date.getDate() );
            setvChooseValue( popup.find('.monthvChoose'), date.getMonth() + 1 );
            setvChooseValue( popup.find('.yearvChoose'), date.getFullYear() );

            popup.find(".isOffline").removeClass("nonner");
            popup.find(".sharedProgram").removeClass("nonner");
            var isOffline = astItemData[entityType + "Isoffline"];
            isOffline ? popup.find("#isOfflineProgram").prop("checked",true) : popup.find("#isNotOfflineProgram").prop("checked",true);
            var sharedProgramAccessCheck = astItemData['sharedProgramAccess'];
            sharedProgramAccessCheck ? popup.find("#sharedProgramAccessCheck").prop("checked",true) : popup.find("#sharedProgramAccessCheck").prop("checked",false);
            // popup.find(".programCategory").removeClass("nonner");
            // var category = astItemData[entityType + "Category"];
            // $("#"+category).prop("checked", true);
        }
        editRemoveASTItemEl = astItem;
    };
    var editASTItemSubmit = function() {
        var params = getFormValues($(this).closest("#cmdsPopup"));
        var eType = editRemoveASTItemEntityType.toLowerCase();
        var cbfn = function() {
            editRemoveASTItemEl.data(eType + "Name", params.name);
            editRemoveASTItemEl.find(".ASTItemName").text(params.name);
            editRemoveASTItemEl.data(eType + "Code", params.code);
            editRemoveASTItemEl.data(eType + "Desc", params.desc);
        };
        var extraParams = {};
        extraParams[eType + "Id"] = editRemoveASTItemEl.data(eType + "Id");
        addEditDeptProgSecCenterUtil($(this), "edit"
                + editRemoveASTItemEntityType, cbfn, extraParams);
    };

    var removeASTItem = function(e) {
        removeReactivateUtil(e, $(this), "remove");
    };
    var reactivateASTItem = function(e) {
        removeReactivateUtil(e, $(this), "reactivate");
    };
    var removeReactivateUtil = function(e, $this, strip) {
        e.stopPropagation();
        var astItem = $this.closest(".ASTItem"), astItemData = astItem.data();
        var entityType = astItemData.entityType;
        editRemoveASTItemEntityType = entityType;
        var popup = fillcmdsPopup("removeReactivateSubmit",
                "removeReactivateAcadEntitySample").closest("#cmdsPopup");
        var head = popup.find(".cmdsPopupHead");
        head.find("span").text(entityType.toLowerCase());
        head.find("label").text(strip);
        editRemoveASTItemEl = astItem;
    };
    var removeReactivateSubmit = function() {
        var $this = $(this);
        var eType = editRemoveASTItemEntityType;
        var strip = $this.closest("#cmdsPopup").find(".cmdsPopupHead label")
                .text();
        var cbfn = function() {
            editRemoveASTItemEl.toggleClass("activeASTItem inactiveASTItem");
        };
        var extraParams = {};
        extraParams[eType.toLowerCase() + "Id"] = editRemoveASTItemEl
                .data(eType.toLowerCase() + "Id");
        addEditDeptProgSecCenterUtil($this,
                strip + editRemoveASTItemEntityType, cbfn, extraParams);
    };

    var getAcadEntities = {
        DEPARTMENT: function(extraParams) {
            resetASTResultsTd([astProgramsTd, astCentersTd, astSectionsTd,
                astCoursesTd]);
            getASTItemsUtil("getDepts", astDeptsTd, extraParams);
            getAcadEntities.changeTdStatus("ASTDeptsTd");
        },
        PROGRAM: function(extraParams, loadAlways) {
            extraParams = extraParams || {};
            extraParams.acadStrContext = acadStrContext;
            var strip = "department";
            if (targetTable == "ADD_PEOPLE_ACADEMICS" || targetTable == "CHANGE_PROGRAM") {
                if (addEditMemberProfile == "TEACHER") {
                    strip = "course";
                } else {
                    loadAlways = true;
                }
            }
            if (getAcadEntities.checkParams(strip, {}, loadAlways)
                    || loadAlways) {
                resetASTResultsTd([astCentersTd, astSectionsTd, astCoursesTd]);
                getASTItemsUtil("programsOfDeptOrCourse", astProgramsTd,
                        extraParams);
                getAcadEntities.changeTdStatus("ASTProgramsTd");
            }
        },
        CENTER: function(extraParams) {
            if (getAcadEntities.checkParams("program")) {
                resetASTResultsTd([astSectionsTd]);
                var cbfn;
                if (targetTable === "CHANGE_PROGRAM") {
                    cbfn = function() {
                        deactivateAcadEntites(astCentersTd, "CENTER",
                                "getSectionsOfCenter");
                        if (extraParams && extraParams.preselect) {
                            preselectAcadEntity(qrProgram.centerId, "SECTION");
                        }
                    };
                }
                getASTItemsUtil("centersOfProgram", astCentersTd, extraParams,
                        cbfn);
                getAcadEntities.changeTdStatus("ASTCentersTd");
            }
        },
        COURSE: function(extraParams) {
            if (getAcadEntities.checkParams("program")) {
                getASTItemsUtil("coursesOfProgram", astCoursesTd, extraParams);
                getAcadEntities.changeTdStatus("ASTCoursesTd");
            }
        },
        SECTION: function(extraParams) {
            if (getAcadEntities.checkParams("center")) {
                var cbfn;
                if (targetTable === "ADD_PEOPLE_ACADEMICS") {
                    cbfn = qrAddEditPeople.markAddedCourseSecs;
                } else if (targetTable === "CHANGE_PROGRAM") {
                    cbfn = function() {
                        deactivateAcadEntites(astSectionsTd, "SECTION", "");
                        if (extraParams && extraParams.preselect) {
                            preselectAcadEntity(qrProgram.sectionId);
                        }
                    };
                }
                getASTItemsUtil("sectionsOfProgCenter", astSectionsTd,
                        extraParams, cbfn);
                getAcadEntities.changeTdStatus("ASTSectionsTd");
            }
        },
        checkParams: function(entityType, extraParams, hideError) {
            extraParams = extraParams || {};
            var acadParams = $.extend(getAcadStrTableParams(), extraParams);
            if (!acadParams[entityType + "Id"] && !hideError) {
                showError("Please select a " + entityType + " first.");
                return false;
            } else
                return true;
        },
        changeTdStatus: function(targetLoadedTdClass) {
            var targetLoadedTd = acadStrTable.find("." + targetLoadedTdClass);
            changeActiveClass(targetLoadedTd, "ASTResultsTdLoaded");
            var prevClass = "ASTResultsTd";
            if (targetLoadedTdClass === "ASTCoursesTd") {
                prevClass = "ASTProgramsTd";
            }
            var prevDiv = targetLoadedTd.prev("." + prevClass);
            if (prevDiv.length > 0) {
                changeActiveClass(prevDiv, "ASTResultsTdClicked");
            }
            var activeClass = "ASTItemActive";
            targetLoadedTd.find("." + activeClass).removeClass(activeClass);
        }
    };
    this.getAcadEntities = getAcadEntities;
    var preselectAcadEntity = function(entityId, targetEntityNameToLoad) {
        var currentProgAstItem = $("#acadStrTable")
                .find(".ASTItem_" + entityId);
        if (currentProgAstItem.length > 0) {
            changeActiveClass(currentProgAstItem, "ASTItemActive");
            if (targetEntityNameToLoad) {
                getAcadEntities[targetEntityNameToLoad]({
                    preselect: true
                });
            }
        }
    };
    this.preselectAcadEntity = preselectAcadEntity;
    var deactivateAcadEntites = function(astClass, acadEntityName, removeClass) {
        var table = $("#acadStrTable");
        var astResultsTd = table.find("." + astClass);
        var astItems = astResultsTd.find(".ASTItem");
        var myAcads = getMyAcadEntities[acadEntityName]
                (getAcadStrTableParams());
        for (var k = 0; k < astItems.length; k++) {
            var astItem = astItems.eq(k);
            var entityId = astItem.data("entityId");
            if (myAcads.indexOf(entityId) === -1) {
                astItem.addClass("ASTItemDeactivated").removeClass(removeClass);
            }
        }
    };
    this.deactivateAcadEntites = deactivateAcadEntites;

    var getAcadStrTableParams = function() {
        var params = {};
        var activeItems = acadStrTable.find(".ASTItemActive");
        for (var k = 0; k < activeItems.length; k++) {
            $.extend(params, activeItems.eq(k).data());
        }
        ;
        if (targetTable === "ADD_PEOPLE_ACADEMICS" && addEditMemberProfile == "TEACHER") {
            var vChoose = acadStrTable.closest(".addMemStepBody").find(
                    ".vChoose");
            if (vChoose.length > 0 && vChoose.data("value") != "-1") {
                var courseId = vChoose.data("value");
                params.id = courseId;
                params.courseId = courseId;
                params.courseName = vChoose.data("optParams").courseName;
            }
        }

        params.targetTable = targetTable;
        return params;
    };
    this.getAcadStrTableParams = getAcadStrTableParams;
    var putASTItem = function(data, dataVar, astClass, requestParams) {
        var result = data.result, title = result.name || requestParams.name, entityId = result.id;
        var ASTItem = ASTItemSample.children().clone(true);
        var $data = {};
        $data[dataVar + "Id"] = entityId;
        $data[dataVar + "Name"] = title;
        $data["entityType"] = dataVar.toUpperCase();
        $data["entityId"] = entityId;
        ASTItem.addClass(astClass).attr("title", title).data($data);
        ASTItem.find(".ASTItemName").text(title);

        if (createASTItemTarget.find(".ASTItem").length === 0) {
            createASTItemTarget.html(ASTItem);
        } else {
            createASTItemTarget.append(ASTItem);
        }
    };

    var getASTItemsUtilFn = function(urlStrip, targetClass, extraParams, cbfn) {
        startLoader();
        var params = {
            start: 0,
            size: 200
        };
        $.extend(params, getAcadStrTableParams());
        if (extraParams)
            $.extend(params, extraParams);
        acadStrTable.find("." + targetClass + "Head").find("input").val("");
        var successFn = function(data) {
            stopLoader();
            acadStrTable.find("." + targetClass).html(data);
            if (cbfn) {
                cbfn();
            }
        };
        getReq("/qracadstr/" + urlStrip, params, successFn);
    };
    var getASTItemsUtil = getASTItemsUtilFn;
    var addEditDeptProgSecCenterUtil = function($this, urlStrip, cbfn,
            extraParams) {
        var popup = getPopupDiv($this);
        var params = getFormValues(popup);
        extraParams = extraParams || {};
        $.extend(params, getAcadStrTableParams(), extraParams);
        if (!params.code && params.name) {
            showcmdsPopupError(popup, getCodeFieldError(params.name));
            return;
        } else if (params.hasError) {
            return;
        } else if (urlStrip === "addProgram") {
            var dateDivs = popup.find(".datevChooseDiv");
            var dateFn = getDateMillisFromvChoose;
            var startDate = dateFn(dateDivs.eq(0));
            var endDate = dateFn(dateDivs.eq(1));
            if (startDate >= endDate || startDate === -1 || endDate === -1) {
                showcmdsPopupError(popup,
                        "Please enter a proper start and end dates.");
                return;
            }
            params.periodStart = startDate;
            params.periodEnd = endDate;
            params.isOffline = popup.find("#isOfflineProgram").prop("checked") ? 'true' : 'false';
            params.sharedProgramAccess = popup.find("#sharedProgramAccessCheck").prop("checked") ? 'true' : 'false';
            // var category = $('input[type=radio][name=ProgramCategory]:checked').attr('id');
            // console.log(category);

        }
        else if (urlStrip === "addSection") {
            var learnpediaId = popup.find("#learnpediaId").val();
            var orgId = popup.find("#orgId").val();
            if(learnpediaId == orgId){
                if(!params.point1 || !params.point2 || !params.point3 || !params.point4){
                console.log("error is problem");
                showcmdsPopupError(popup,"All Points are Mandatory");
                return;
            }
            var imagePath = $('.sectionImage').attr('src');
            console.log("imagePath" +imagePath);
            if(typeof imagePath === "undefined" || imagePath===""){
                console.log("error is came");
                showcmdsPopupError(popup,"please add image");
                return;
            }
            var descriptionPoints = [params.point1, params.point2, params.point3, params.point4];
            params.descriptionPoints = descriptionPoints;
            params.thumbnail= $('.sectionImage').attr('src');
            params.imageNameWithExtension = $('.sectionImage').attr('uuid');
            }
        }
        else if (urlStrip === "edit" + editRemoveASTItemEntityType) {
            if(editRemoveASTItemEntityType === "PROGRAM"){
            var dateDivs = popup.find(".datevChooseDiv");
            var dateFn = getDateMillisFromvChoose;
           
            var endDate = dateFn(dateDivs.eq(0));
            params.periodEnd = endDate;
            params.isOffline = popup.find("#isOfflineProgram").prop("checked") ? 'true' : 'false';
            // params.category = $('input[type=radio][name=ProgramCategory]:checked').attr('id');
            }
            else if(editRemoveASTItemEntityType === "SECTION"){
                var learnpediaId = popup.find("#learnpediaId").val();
                var orgId = popup.find("#orgId").val();
                if(learnpediaId == orgId){
                    if(!params.point1 || !params.point2 || !params.point3 || !params.point4){
                    console.log("error is came");
                    showcmdsPopupError(popup,"All Points are Mandatory");
                    return;
                }
                var imagePath = $('.sectionImage').attr('src');
                if(typeof imagePath === "undefined" || imagePath===""){
                    console.log("error is came");
                    showcmdsPopupError(popup,"please add image");
                    return;
                }
                var descriptionPoints = [params.point1, params.point2, params.point3, params.point4];
                params.descriptionPoints = descriptionPoints;
                params.thumbnail= $('.sectionImage').attr('src');
                params.imageNameWithExtension = $('.sectionImage').attr('uuid');
                }
            }
        }

        startLoader();
        var successFn = function(data) {
            closePopup();
            stopLoader();
            if (cbfn)
                cbfn(data, params);
        };
        postReq("/qracadStr/" + urlStrip, params, successFn);
    };
    var resetText = {
        PROGRAM: "Select a department to view its programs.",
        CENTER: "Select a department and program to view the centers it is run.",
        SECTION: "Select a program and center to view the sections.",
        COURSE: "Select a program to view the courses."
    };
    var resetASTResultsTd = function(classList) {
        for (var k = 0; k < classList.length; k++) {
            var td = acadStrTable.find("." + classList[k]);
            td.html(makeHTMLTag("div", {
                "class": "acadEntityDefaultText"
            }).html(resetText[td.data("entityType")]));
            var tdHead = acadStrTable.find("." + classList[k] + "Head");
            tdHead.find("input").val("");
        }
    };
    this.resetASTResultsTd = resetASTResultsTd;
})(jQuery);
