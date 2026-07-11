var qrPeople = new (function($) {
    var peoplePage, peopleTargetProfile, bodyClickEvent = "click.qrPeople", clickEvent = "click", peoplePageUserId, peoplePageOrgUserId, pageTarget;
    var pageUrlParams = {targetProfile: "TEACHER", programId: "", centerId: "", sectionId: "",
        courseId: "", query: "", start: 0, size: 50};
    var myProgramId;
    this.init = function(params) {
    	myProgramId = params.programId;
        peoplePage = $("#peoplePage");
        peoplePage.off(clickEvent)
                .on(clickEvent, ".editOrgMember", editOrgMember)
                .on(clickEvent, ".deactivateOrgMember", showDeactivationPopup)
                .on(clickEvent, ".editOrgStudent", editOrgStudent)
                .on(clickEvent, ".editOrgOfflineUser", editOrgOfflineUser)
                .on(clickEvent, ".addOrgPeople", addOrgPeople)
                .on(clickEvent, ".offlineStudentsData",offlineStudentsData)
                .on(clickEvent, ".studentsSignupData",getStudentsData)
                .on(clickEvent, ".showUserMappings", showUserMappings)
                // username,password,email changes
                .on(clickEvent, ".unsetMemberEmail", unsetMemberEmail)
                .on(clickEvent, ".changeMemberPass", changeMemberPass)
                .on(clickEvent, ".resetMemberUsername", resetMemberUsername)

                // upload profile pics
                .on(clickEvent, ".uploadPeopleProfilePics",
                        uploadPeopleProfilePics)

                .on(clickEvent, ".viewStudentProgramPaymentInfo", viewStudentProgramPaymentInfo)
                //send emails
                .on(clickEvent, ".sendEmails", sendEmails)
                .on(clickEvent, ".submitSendEmail", submitSendEmail)

        $("body").off(bodyClickEvent)
                .on(bodyClickEvent, ".changeMemberPassSubmit", changeMemberPassSubmit)
                .on(bodyClickEvent, ".resetMemberUsernameSubmit",
                        resetMemberUsernameSubmit)
                .on(bodyClickEvent, ".uploadStudentsFile", uploadStudentsFile)
                .on(bodyClickEvent,".downloadOfflineStudentsData",downloadOfflineStudentsData)
                .on(bodyClickEvent, ".addOrgSingleStudent", addOrgSingleStudent)
                .on(bodyClickEvent, ".checkIfProgramChosen", checkIfProgramChosen)
                .on(bodyClickEvent, ".viewProgMappingDetails", viewHideProgMappingDetails)
                .on(bodyClickEvent, ".hideProgMappingDetails", viewHideProgMappingDetails)
                .on(bodyClickEvent, ".deactivateMemberNow", deactivateMemberNow)
                .on(bodyClickEvent, ".scheduleDeactivation", showScheduleDeactivation)
                .on(bodyClickEvent, ".scheduleDeactivationNow", scheduleDeactivationNow)

        peopleTargetProfile = params.targetProfile;
        pageTarget = params.pageTarget || "PEOPLE_PAGE";
        if (peopleTargetProfile === "TEACHER") {
            peoplePage.find(".coursevChoose").removeClass("nonner");
        } else {
            peoplePage.find(".coursevChoose").addClass("nonner");
        }
        var mcWidget;
        var urlParams = fetchUrlParams();
        $.extend(params, urlParams);
        if (pageTarget === "PEOPLE_PAGE") {
            mcWidget = peoplePage.addClass("mcWidget");
            var filterWidget = peoplePage.find(".filterProgCenterSecDiv");
            filterAcadEntWidget.loadWidget(filterWidget, params);
            mcWidget.data("pageUrlParams", pageUrlParams);
            mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
            var $data = mcWidget.data();
        } else {
            peoplePage.removeClass("mcWidget");
            mcWidget = peoplePage.closest("#programPage");
            var programPageUrlParams;
            if (peopleTargetProfile === "STUDENT") {
                programPageUrlParams = qrProgram.studentsUrlParams;
            } else {
                programPageUrlParams = qrProgram.membersUrlParams;
            }
            mcWidget.data("pageUrlParams", programPageUrlParams);
            mcWidget.data("changeUrlAfterLoad", qrProgram.afterContentLoaded);
        }
        initmcWidgetforCMDS(mcWidget, "/qrpeople/peopleTable", params,
                false, true);
        updatemcWidgetParamHolders(mcWidget, urlParams);
        fixContentSec();
        if (params.targetUserId) {
            peoplePageUserId = params.targetUserId;
        }
        if (params.targetOrgMemberId) {
            peoplePageOrgUserId = params.targetOrgMemberId;
        }
    };
    var getReq = vReq.get;
    var postReq = vReq.post;

    var offlineStudentsData=function() {
        var popup = getcmdsPopupBody();
        popup.html(downloadTestUserData.children().clone(true));
    }

    var getStudentsData = function(){
        var popup = getcmdsPopupBody();
        popup.html(downloadTestUserData.children().clone(true));
        popup.find(".cmdsPopupHead").html("Download Students Data");
        popup.find(".downloadData").attr("action","/qrPeople/getStudentsData");
    }

    var downloadOfflineStudentsData = function(){
      var popup = getcmdsPopupBody();
      var dateDivs = popup.find(".datevChooseDiv");
      var dateFn = getDateMillisFromvChoose;
      var startDate = dateFn(dateDivs.eq(0));
      var endDate = dateFn(dateDivs.eq(1));
      if(startDate == 0){
        showcmdsError("Please enter proper start date for specific month");
        return false;
      }
      if(endDate == 0){
        showcmdsError("Please enter proper end date for specific month");
        return false;
      }
      var aDay = 86399000;
      //To get endDate of particular day till last second
      endDate = endDate + aDay;
      var timeDiff = endDate - startDate;
      //At max 3 months data can be retrieved.
      var timeLimit = aDay * 93;
      if(timeLimit <= timeDiff){
        showError("At one point of time, you can only get 3 months data");
        return false;
      }
      if (startDate >= endDate || startDate === -1 || endDate === -1) {
        showcmdsError("Please enter proper start and end dates.", "OK");
        return false;
      }
      $('#startDate').val(startDate);
      $('#endDate').val(endDate);
    }
    var addOrgPeople = function() {
    	console.log("qwe");
        var targetProfile = $(this).data("targetProfile");
        if (targetProfile === "STUDENT") {
            var popup = getcmdsPopupBody();
            popup.html(addOrgStudentSample.children().clone(true));
        }else if (targetProfile.split("|").length>1) {
        	shareProgToOrg(targetProfile.split("|")[0]);
        } else if (targetProfile === "OFFLINE_USER") {
            addOrgOfflineUser();
        } else {
            addEditPeopleUtil({
                targetProfile: targetProfile
            }, null, "ADD");
        }
    };
    
    var shareProgToOrg = function(subscriberOrgId) {
    	startLoader();
        var $this = $(this);
        var pa = $this.parent();
        var successFn = function(data) {
           stopLoader();
           cSecHolder.html(data);
        };
       console.log("prog"+myProgramId);
        vReq.get("/qrpeople/shareProgToOrg", {
            programId:myProgramId,
            providerOrgId:"",
            subscriberOrgId:subscriberOrgId
        }, successFn);      
        
    };
    
    var showDeactivationPopup = function() {
        showTopLoader();
        var targetUserId = $(this).data("userId");
        var params = {
            targetUserId: targetUserId,
            userState: $(this).data("userState"),
            activateFrom: $(this).data("activeFrom"),
            activateTill: $(this).data("activeTill"),
        };
        vReq.get("/QrPeople/showDeactivationPopup", params, function(data) {
            var popup = showVPopup();
            popup.html(data);
            popup.find(".datePickCalendar").each(function() {
                var $this = $(this);
                var date = $this.data("prevValue");
                if (date == -1) {
                    $this.val("");
                } else if (date > 0) {
                    var val = $(date).formatDate("mm/dd/yyyy");
                    $this.val(val);
                    $this.data("prevValue", $(new Date(date)).dateWiseTime().getTime());
                }
            });
            popup.updateClientTime();
        });
    };
    var showScheduleDeactivation = function() {
        var $this = $(this);
        $this.closest(".deactivationOptions").addClass("nonner")
                .siblings(".scheduleDeactivationHold").removeClass("nonner")
                .find(".datePickCalendar").datepicker({minDate: 0});
    };
    var deactivateMemberNow = function() {
        var $this = $(this);
        var type = $this.data("type");
        type = type ? true : false;
        var targetUserId = $this.closest("#deactivationPopup").data("userId");
        var from = -1, to = -1;
        //var dt = new Date();//$(new Date()).dateWiseTime();
        if (type) {
            //SERVER NEEDS -2 FOR CURRENT TIME
	    //to = dt.getTime() + serverSystemTimeDelta;
            to = -2;
        } else {
            //SERVER NEEDS -2 FOR CURRENT TIME
            //from = dt.getTime() + serverSystemTimeDelta;
            from = -2;
        }
        callServerForDeactivation(targetUserId, from, to, function(state, data) {
            if (state) {
                var msg = "Success";
                if (type) {
                    msg = "Successfully Deactivated";
                } else {
                    msg = "Successfully Activated";
                }
                closeVPopup();
                showMessage(msg, function() {
                    refreshPage();
                });
            } else if (data.errorCode == "") {
                showError("Operation Failed");
            }
        });
    };
    var scheduleDeactivationNow = function() {
        var $this = $(this);
        var parDiv = $this.closest(".scheduleDeactivationHold");
        var fromInput = $("#deactivateFromInput");
        var from = fromInput.datepicker('getDate');
        var toInput = $("#deactivateToInput");
        var to = toInput.datepicker('getDate');
        if (!from || !to) {
            showError("Please select 'From' and 'To' date!");
            return;
        }
        from = from.getTime();
        to = to.getTime();
        if (to <= from) {
            showError("'To' date should be more than 'From' date!");
            return;
        }
        if (from == parseInt(fromInput.data("prevValue"), 10)) {
            from = -1;
        }
        if (to == parseInt(toInput.data("prevValue"), 10)) {
            to = -1;
        }
        var dt = $(new Date()).dateWiseTime();
        if (from == dt.getTime()) {
            //SERVER NEEDS -2 FOR CURRENT TIME
            //from = new Date().getTime() + serverSystemTimeDelta;
            from = -2;
        }
        if (to > 0) {
            var temp = new Date(to);
            temp.setDate(temp.getDate() + 1);
            to = temp.getTime() - 1;
        }
        var targetUserId = $this.closest("#deactivationPopup").data("userId");
        callServerForDeactivation(targetUserId, from, to, function(state, data) {
            if (state) {
                closeVPopup();
                showMessage("Successfully Scheduled!", function() {
                    refreshPage();
                });
            } else if (data.errorCode == "") {
                showError("Operation Failed");
            }
        });
    };
    var callServerForDeactivation = function(targetUserId, from, to, cbFn) {
        var prms = {targetUserId: targetUserId, activateFrom: from, activateTill: to};
        vReq.post("/QrPeople/scheduleDeactivateMember", prms, function(data) {
            var state = false;
            if (data && data.errorCode == "" && data.result.done) {
                state = true;
            }
            try {
                if (cbFn) {
                    cbFn(state, data);
                }
            } catch (err) {
                putConsoleError(err);
            }
        });
    };
    var editOrgMember = function() {
        var $data = $(this).data();
        addEditPeopleUtil({
            targetUserId: $data.userId, targetProfile: $data.userProfile
        }, null, "EDIT");
    };
    var addEditPeopleUtil = function(params, url, action) {
        startLoader();
        params = params || {};
        if (!url) {
            url = "/qrpeople/addEditMember";
        }

        if (action === "EDIT") {
            pushHistory(null, null, vcmdsUrls.EDITMEMBER(params.targetProfile, params.targetUserId));
        } else {
            pushHistory(null, null, vcmdsUrls.ADDMEMBER(params.targetProfile));
        }



        var successFn = function(data) {
            stopLoader();
            closePopup();
            cSecHolder.html(data);
            uploadProfilePicUtil.init(cSecHolder.find(".memberPicDiv"),
                    cSecHolder.find(".memberPicPreview"),
                    '/qrpeople/uploadProfilePic');
        };
        getReq(url, params, successFn);
    };
    var uploadStudentsFile = function() {
        var popup = $(this).closest(".cmdsPopupBody");
        popup.html(uploadStudentsFileSample.children().clone(true));

        // adding programs
        var tagger = makeHTMLTag;
        var entityOptsHolder = tagger("div");
        var progs = getMemberInfoPojo().progList;
        for (var c = 0; c < progs.length; c++) {
            var entity = progs[c], entityId = entity.id, entityName = entity.name;
            var opt = tagger("div", {
                "class": "vChooseOpt"
            }).data("value", entityId).text(entityName);
            entityOptsHolder.append(opt);
        }
        popup.find(".vChooseDropDown").html(entityOptsHolder.children());

        fetchScripts([{
                fname: "uicomWidgets/fileuploader.js",
                cb: createStudentsFileUploader
            }]);
    };
    var qqVarForUpload;
    var createStudentsFileUploader = function() {
        var uploadDiv = $("#cmdsPopup").find(".uploadDiv");
        qqVarForUpload = new qq.FileUploader({
            element: uploadDiv.get(0),
            action: '/qrpeople/uploadStudentsFile',
            debug: true,
            sizeLimit: 5 * 1024 * 1024,
            allowedExtensions: ["xls", "xlsx"]
        });
        qqVarForUpload.onUploadDone = onUploadDone;
        qqVarForUpload.onUploadProgress = onUploadProgress;
        var uploadButton = uploadDiv.find(".qq-upload-button");
        uploadButton.addClass("blueButton checkIfProgramChosen");
        uploadDiv.find(".qq-button-title").html("Choose File");
    };
    var checkIfProgramChosen = function(e) {
        var popup = getPopupDiv($(this));
        var progId = popup.find(".vChoose").data("value");
        if (progId == "-1") {
            showError("Choose a program first.");
            e.preventDefault();
        } else {
            qqVarForUpload.setParams({
                programId: progId,
                uploadFileParamName: "inputFile",
                merge: true
            })
        }
    };
    var onUploadDone = function(id, fileName, result) {
        var err = result.errorMessage;
        var progId = $("#cmdsPopup").find(".vChoose").data("value");
        if (err != "") {
            showError(err);
            $("#cmdsPopup").find(".uploadStudentsProgressDiv").html("");
            createStudentsFileUploader();
        } else {
            showMessage("Successfully uploaded.");
            setTimeout(function() {
                var params = {targetProfile: "STUDENT", programId: progId, start: 0, size: 50};
                opencmdsPeople(null, params);
                pushHistory(null, null, vcmdsUrls.PEOPLE(params));
                closePopup();
            }, 1000);
        }
    };
    var onUploadProgress = function(percentDecimal, progressText) {
        $("#cmdsPopup").find(".uploadStudentsProgressDiv").html(
                "<span class='color8 smally'>Percentage Uploaded:</span>"
                + Math.round(percentDecimal * 100) + " %");
    };

    var addOrgSingleStudent = function() {
        addEditPeopleUtil({
            targetProfile: "STUDENT"
        }, "/qrpeople/addEditStudent", "ADD");
    };
    var editOrgStudent = function() {
        addEditPeopleUtil({
            targetUserId: $(this).data("userId"),
            targetProfile: "STUDENT"
        }, "/qrpeople/addEditStudent", "EDIT");
    };
    var addOrgOfflineUser = function() {
        addEditPeopleUtil({
            targetProfile: "OFFLINE_USER"
        }, "/qrpeople/addEditOfflineUser", "ADD");
    };
    var editOrgOfflineUser = function() {
        addEditPeopleUtil({
            targetUserId: $(this).data("userId"),
            targetProfile: "OFFLINE_USER"
        }, "/qrpeople/addEditOfflineUser", "EDIT");
    };

    var showUserMappings = function() {
        startLoader();
        var successFn = function(data) {
            stopLoader();
            var popup = getcmdsPopupBody().html(data);
            popup.children(".cmdsPopupHead").text("Institute Info");
        };
        getReq("/qrpeople/showUserMappings", {
            targetUserId: $(this).data("userId")
        }, successFn);
    };
    var viewHideProgMappingDetails = function() {
        var $this = $(this), tr = $this.closest("tr"), index = tr
                .data("trIndex");
        var siblings = tr.siblings(".progMapTr" + index);
        var siblingsLength = siblings.length;
        var viewClass = "viewProgMappingDetails", hideClass = "hideProgMappingDetails";
        var td = tr.children("td");
        if ($this.hasClass(viewClass)) {
            $this.removeClass(viewClass).addClass(hideClass);
            siblings.removeClass("nonner");
            $this.text("Hide Details");
            td.removeAttr("colspan").attr("rowspan", siblingsLength + 1);
        } else {
            $this.addClass(viewClass).removeClass(hideClass);
            $this.text("View Details");
            siblings.addClass("nonner");
            td.removeAttr("rowspan").attr("colspan", 3);
        }
    };

    var uploadPeopleProfilePics = function() {
        var popup = getcmdsPopupBody();
        popup.html(uploadPeopleProfilePicsSample.children().clone(true));
        fetchScripts([{
                fname: "uicomWidgets/fileuploader.js",
                cb: createProfilePicsFileUploader
            }]);
    };
    var createProfilePicsFileUploader = function() {
        var uploadDiv = $("#cmdsPopup").find(".uploadDiv");
        var u = new qq.FileUploader({
            element: uploadDiv.get(0),
            action: '/qrpeople/uploadPeopleProfilePics',
            debug: true,
            params: {
                uploadFileParamName: "inputFile"
            },
            sizeLimit: 25 * 1024 * 1024,
            allowedExtensions: ["zip"]
        });
        u.onUploadDone = onUploadProfilePicsDone;
        u.onUploadProgress = onUploadProfilePicsProgress;
        var uploadButton = uploadDiv.find(".qq-upload-button");
        uploadButton.addClass("blueButton");
        uploadDiv.find(".qq-button-title").html("Choose File");
    };
    var onUploadProfilePicsDone = function(id, fileName, result) {
        var err = result.errorMessage;
        if (err != "") {
            showError(err);
            $("#cmdsPopup").find(".uploadStudentsProgressDiv").html("");
            createStudentsFileUploader();
        } else {
            processUploadedProfilePicsResponse(result);
        }
    };
    var onUploadProfilePicsProgress = function(percentDecimal, progressText) {
        $("#cmdsPopup").find(".uploadProgressDiv").html(
                "<span class='color8 smally'>Percentage Uploaded:</span>"
                + Math.round(percentDecimal * 100) + " %");
    };
    var processUploadedProfilePicsResponse = function(data) {
        var popup = getcmdsPopupBody();
        popup.html(uploadedProfilePicsSample.children().clone(true));
        var imagesMap = data.result.status;
        var holder = makeHTMLTag("tbody");
        $.each(imagesMap, function(memberId, statusResult) {
            var thumbnail = "";
            if (statusResult.done) {
                thumbnail = "<img class='img30' src='" + statusResult.thumbnail
                        + "'/>";
            }
            var trHTML = "<tr><td>" + memberId + "</td>\n\
            <td>"
                    + thumbnail + "</td></tr>";
            holder.append(trHTML);
        });
        popup.find("tbody").html(holder.children());
    };

    // email,username,password
    var unsetMemberEmail = function() {
        startLoader();
        var $this = $(this);
        var pa = $this.parent();
        pa.html("Unsetting..");
        var successFn = function(data) {
            stopLoader();
            if (data && data.result && data.result.done) {
                pa.html("Email unset done.");
                $(".memberEmailId").text("");
            }
        };
        postReq("/qrpeople/unsetMemberEmail", {
            targetUserId: $this.data("userId"),
            targetOrgMemberId: $this.data("orgMemberId")
        }, successFn);
    };
    var changeMemberPass = function() {
        var popup = fillcmdsPopup("changeMemberPassSubmit",
                "changePasswordSample");
        popup.find(".successText").html("Password changed successfully.");
        memberCredentialChangeUtil(popup);
    };
    var changeMemberPassSubmit = function() {
        changeUsernamePasswordUtil($(this), "changeMemberPassword");
    };
    var resetMemberUsername = function() {
        var popup = fillcmdsPopup(
                "resetMemberUsernameSubmit",
                "changePasswordSample",
                "Reset Username<div class=smallThinGray>(Provide the password to be used \n\
        after resetting the username)</div>");
        popup.find(".successText").html("Username is successfully reset.");
        memberCredentialChangeUtil(popup);
    };
    var resetMemberUsernameSubmit = function() {
        changeUsernamePasswordUtil($(this), "resetUsername", "RESET_USERNAME");
    };
    var memberCredentialChangeUtil = function(popup) {
        var tr = popup.find(".currentPassTr");
        tr.addClass("nonner").find("input").val("currentPassword");
    };

    var changeUsernamePasswordUtil = function($this, urlStrip, target) {
        var popup = $this.closest("#cmdsPopup");
        var params = checkPasswords(popup);
        if (!params) {
            return;
        }
        if (peoplePageUserId) {
            params.targetUserId = peoplePageUserId;
        }
        if (peoplePageOrgUserId) {
            params.targetOrgMemberId = peoplePageOrgUserId;
        }
        $this.text("Submitting..");
        var successFn = function(data) {
            var extraMessage = "", timer = 2;
            if (data.isOrgLogin) {
                extraMessage = ",.extraMessage";
                timer = 5;
            }
            if (target === "RESET_USERNAME") {
                popup
                        .find(".extraMessage")
                        .html(
                                "Now MemberId and the password given can be used to login.");
                timer = 5;
            }
            popup.find(".successText" + extraMessage).removeClass("nonner");
            setTimeout(function() {
                cancelCommonPopup();
            }, timer * 1000);
        };
        var completeFn = function() {
            $this.text("Submit");
        };
        postReq("/qrpeople/" + urlStrip, params, successFn, null, completeFn);
    };
    var checkPasswords = function(popup) {
        var params = getFormValues(popup);
        var newPass = params.newPassword, conPass = params.confirmPassword;

        var err = "";
        if (params.hasError) {
            err = "Fields marked with * are compulsory";
        } else if (newPass != conPass) {
            err = "Passwords do not match.";
        } else if (newPass.length < 6) {
            err = "Password length should be atleast 6 characters";
        }
        if (err != "") {
            showcmdsPopupError(popup, err);
            return false;
        } else {
            return params;
        }
    };


    var viewStudentProgramPaymentInfo = function() {
        vReq.get("/qrprograms/studentPaymentInfo",
                {sectionId: $(this).data("sectionId"), targetUserId: $(this).data("targetUserId")},
        function(data) {
            getcmdsPopupBody().html(data);
        });
    };
})(jQuery);
var setCoursesForPeoplePage = function(mcWidget, vChoose) {
    var profile = vChoose.data("value");
    var filterWidget = mcWidget.find(".filterProgCenterSecDiv");
    var sendEmailsClass = mcWidget.find(".sendEmails");
    var downloadOfflineUserDataClass=mcWidget.find(".offlineStudentsData");
    var exportStudentsDataClass = mcWidget.find(".studentsSignupData");
    var btnHolderText = mcWidget.find(".btnHolderText");
    var coursevChoose = filterWidget.children(".coursevChoose");
    var mcWidgetParams = mcWidget.data("params");
    var orgId= downloadOfflineUserDataClass.data("orgId");
    if (profile === "TEACHER") {
        coursevChoose.removeClass("nonner");
        var programId = mcWidgetParams.programId;
        filterAcadEntWidget.setCourses(programId, filterWidget);
    } else {
        delete mcWidgetParams.courseId;
        coursevChoose.addClass("nonner");
    }
    //Show this option only to specific orgId(In this case MIIT.In Future we will change to Lalit)
    if(profile == "STUDENT" && orgId == "55f6b821e4b06863a03b2fcc"){
        downloadOfflineUserDataClass.removeClass("nonner");
        btnHolderText.removeClass("nonner");
        exportStudentsDataClass.removeClass("nonner");
    }
    if (profile === "STUDENT") {
        sendEmailsClass.removeClass("nonner");
        exportStudentsDataClass.removeClass("nonner");

    } else{
        exportStudentsDataClass.addClass("nonner");
        btnHolderText.addClass("nonner");
        downloadOfflineUserDataClass.addClass("nonner");
        sendEmailsClass.addClass("nonner");
    }
    manageContent.loadmcContent(mcWidget, vChoose);
};
var moveOrAddStudentsChange = function(vChoose, finalValue) {
    if (finalValue !== "-1") {
        qrProgram.prepareProgramPagePopupForMovingStudents(vChoose, finalValue);
        setvChooseValue(vChoose, "-1");
    }
};
var sendEmails = function(){
    var urlParams = fetchUrlParams();
    if(urlParams.programId===undefined || urlParams.centerId===undefined || urlParams.sectionId===undefined){
        showError("Please select the Program, Center & Section then click on send email's button");
        return;
    }

    var progList = getMemberInfoPojo().progList;
    var programForMail = $.grep(progList, function(e){ return e.id == urlParams.programId; });
    var programName = programForMail[0].name;

    var centersList = programForMail[0].centers;
    var centerForMail = $.grep(centersList, function(e){ return e.id == urlParams.centerId; });
    var centerName = centerForMail[0].name;

    var sectionList = centerForMail[0].sections;
    var sectionForMail = $.grep(sectionList, function(e){ return e.id == urlParams.sectionId; });
    var sectionName = sectionForMail[0].name;

    urlParams.programName = programName;
    urlParams.sectionName = sectionName;
    urlParams.centerName = centerName;

    vReq.get("/QrPeople/sendEmailsPopup" , urlParams, function(data) {
        var popup = showVPopup(0.7);
        popup.html(data);
        popup.on("click",".submitSendEmail",submitSendEmail)
    });
};

var submitSendEmail = function() {
    var $this = $(this);
    if($this.hasClass("disableBtn")){ return false; }
    $this.addClass("disableBtn");
    var popup = $this.closest(".sendEmailPopup");
    var emailPopupData = popup.find(".emailPopupData");
    var $data = emailPopupData.data();
    var subject = popup.find("#subjectForEmailSection").val().trim();
    if (!subject) {
        subject = "";
    }
    var message = popup.find("#messageForEmailSection").val().trim();
    var params = {
        programId : $data.programId,
        centerId : $data.centerId,
        sectionId : $data.sectionId,
        subject: subject,
        message: message
    }
    showTopLoader();
    vReq.post("/QrPeople/sendEmail",params,function(data){
        $this.removeClass("disableBtn");
        closeVPopup();
        showMessage("Successfully sent emails to the students");
    },function(){
        $this.removeClass("disableBtn");
    });
};
