var qrAddEditPeople = new (function($) {
    var bodyClickEvent = "click.qrAddEditPeople", addEditPeoplePage, memProfile,
            targetUserId, addMemAddedAcademics, peoplePage, targetOrgMemberId, targetPage, subTargetPage,
            courseSectionMappingCache, clickEvent = "click", keyupEvent = "keyup", pasteEvent = "paste";
    var changeEvent = "change";
    var uploadProfilePicTakingPlace = false;
    var paidSectionIds = [];
    var MAX_ROUNDOFF = 10000;
    this.init = function(params) {
        params = params || {};
        addEditPeoplePage = $("#addEditPeoplePage");
        addMemAddedAcademics = $("#addMemAddedAcademics");
        addEditPeoplePage.off(clickEvent).off(keyupEvent).off(pasteEvent)
                .on(clickEvent, ".selectMemProfile", selectMemProfile)
                .on(clickEvent, ".addMemProfileSubmit", addMemProfileSubmit)


                .on(clickEvent, ".addMemStepHead[rel='ACADEMICS']", accessMemAcademics)
                .on(clickEvent, ".removeProgCenterSecs", removeProgCenterSecs)
                .on(clickEvent, ".openCoursevChoose", openCoursevChoose)
                .on(clickEvent, ".addMemAcademicsSubmit", addMemAcademicsSubmit)


                .on(clickEvent, ".addMemStepHead[rel='ROLE']", accessMemRole)
                .on(clickEvent, ".addMemRoleSubmit", addMemRoleSubmit)
                .on(clickEvent, ".addMemStepHead", addMemStepHead)
                .on(clickEvent, ".editSubcriptionDate",editEndDateOfProgramSubscription)
                .on(clickEvent, ".editSaleDetails", editSaleDetailsOfSubscription)

                .on(clickEvent, ".finaliseSecsForMem", finaliseSecsForMem)
                .on(clickEvent, ".addEditStudentSubmit", addEditStudentSubmit)
                .on(clickEvent, ".addEditOfflineUserSubmit", addEditOfflineUserSubmit)
                .on(keyupEvent, ".memberIdInput", memberIdInputted)
                .on(pasteEvent, ".memberIdInput", memberIdInputPasted)

        $(document).off(bodyClickEvent).off(changeEvent)
                .on(bodyClickEvent, ".addStudentToPaidProgram", addStudentToPaidProgram)
                .on(bodyClickEvent, ".addPaymentItem", addPaymentItem)
                .on(bodyClickEvent, ".paymentEntryDeleteNotation", deletePaymentItem)
                .on(bodyClickEvent, ".updateEndDateOfSubscription",updateEndDateOfSubscription)
                .on(bodyClickEvent, ".updateSaleDetails", updateSaleDetails)
                .on(changeEvent, ".paymentTypeSelect", changePaymentType)
                .on(changeEvent, ".packageSelect", changeSelectedPackage)
                .on(changeEvent, "#discountPercentage", changeDiscountPercentage)
                .on(changeEvent, "#roundoffInput", changeRoundoffInput)
        courseSectionMappingCache = {};
        paidSectionIds = [];
        targetPage = params.target;
        subTargetPage = params.subTarget;
        if (params.targetUserId) {
            targetUserId = params.targetUserId;
            targetOrgMemberId = params.targetOrgMemberId;
            memProfile = params.profile;
            prepareOldMappings();
            if (params.target === "ADD_EDIT_MEMBER") {
                selectMemProfileUtil(memProfile);
            }
        }


        if (targetPage === "ADD_EDIT_STUDENT") {
            memProfile = "STUDENT";
            addEditPeoplePage.find(".addMemStepBody").first().removeClass("nonner");
            addEditPeoplePage.addClass("add" + memProfile + "Page");
        }
        if (targetPage === "ADD_EDIT_OFFLINE_USER") {
            memProfile = "OFFLINE_USER";
            addEditPeoplePage.find(".addMemStepBody").first().removeClass("nonner");
            addEditPeoplePage.addClass("add" + memProfile + "Page");
        }

        if (subTargetPage === "ADD_MEMBER" && params.targetProfile) {
            selectMemProfile(null, addEditPeoplePage.find("[rel='" + params.targetProfile + "']"));
        }


        //for profile pic
        uploadProfilePicUtil.init(addEditPeoplePage.find(".memberPicDiv"),
                addEditPeoplePage.find(".memberPicPreview"), '/qrpeople/uploadMemberProfilePic');
    };
    var postReq = vReq.post;
    var getReq = vReq.get;




    var selectMemProfile = function(e, $this) {
        if (!$this) {
            $this = $(this);
        }
        selectMemProfileUtil($this.attr("rel"));
        $this.removeClass("blueTabInactiveNew")
                .siblings(".blueTabNew").addClass("blueTabInactiveNew");
        changeActiveClass($this, "blueTabActiveNew");
    };
    var selectMemProfileUtil = function(profile) {
        memProfile = profile;
        addEditPeoplePage.find(".addMemStepBody").first().removeClass("nonner");
        addEditPeoplePage.addClass("add" + profile + "Page");
        if (memProfile !== "TEACHER") {
            addEditPeoplePage.find(".addMemStepBodyHead2").eq(1).text("Assign Sections");
        }
    };



    var addMemProfileSubmit = function() {
        var url = "/QrPeople/addMemberSubmit";
        if (subTargetPage === "EDIT_MEMBER") {
            url = "/QrPeople/editMemberSubmit";
        }
        addEditPeopleSubmitUtil($(this), url);
    };
    var addEditStudentSubmit = function() {
        var url = "/QrPeople/addStudentSubmit";
        if (subTargetPage === "EDIT_STUDENT") {
            url = "/QrPeople/editStudentSubmit";
        }
        addEditPeopleSubmitUtil($(this), url);
    };
    var addEditOfflineUserSubmit = function() {
        var url = "/QrPeople/addOfflineUserSubmit";
        if (subTargetPage === "EDIT_OFFLINE_USER") {
            url = "/QrPeople/editOfflineUserSubmit";
        }
        addEditPeopleSubmitUtil($(this), url);
    };
    var addEditPeopleSubmitUtil = function($this, url) {
        var stepBody = $this.closest(".addMemStepBody");
        //var dob = getDateInDOBFormat(getDateMillisFromvChoose(stepBody.find(".datevChooseDiv")));
        //var oldDobDiv = stepBody.find(".dobVChooseTD");
        //var oldDob = oldDobDiv.data("value");
        //if (oldDob && oldDob != dob) {
        //   var warnMsg = oldDobDiv.data("warnMsg");
        //    showVYesNoBox(warnMsg, "", function(state) {
        //        if (state) {
        //            addEditPeopleSubmitFinal($this, url, stepBody, dob);
        //        }
        //    });
        //} else {
        //    addEditPeopleSubmitFinal($this, url, stepBody, dob);
        //}
        addEditPeopleSubmitFinal($this, url, stepBody, null);
    }
    var addEditPeopleSubmitFinal = function($this, url, stepBody, dob) {
        var params = getFormValues(stepBody);
        var parentEmail = params.parentEmail;
        var contactNumber = params.contactNumber;
        var errorEl = stepBody.find(".cmdsPopupError");
        if (params.hasError) {
            showcmdsPopupError(stepBody);
            return;
        //} else if (dob === "") {
        //    showcmdsPopupError(stepBody, "Please enter a valid date of birth");
        //    return;
        } else if (parentEmail && !validEmail(parentEmail)) {
            showcmdsPopupError(stepBody, "Please enter a valid parent email.");
            return;
        } else if (!contactNumber) {
            showcmdsPopupError(stepBody, "Please enter a valid contact number.");
            return;
        }
        else if (!params.email && !params.targetMemberId) {
            showError("Please provide with either Email-Id or Institute Id. This is used to generate login credentials for the member.");
            return;
        }
        else if (!params.isEdit && !params.email && !params.targetMemberId) {
            showcmdsPopupError(stepBody, "Please provide with either Email-Id or Institute Id. This is used to generate login credentials for the member.");
            return;
        } else {
            errorEl.addClass("nonner");
        }
        params.dob = dob;
        params.profile = memProfile;
        params.updateList = [];
        for (key in params) {
            if (!key.contains("extraInfo")) {
                params.updateList.push(key);
            }
        }
        params.updateList.push("extraInfo");
        removeItemFromArr(params.updateList, "hasError");
        removeItemFromArr(params.updateList, "errorText");
        removeItemFromArr(params.updateList, "updateList");
        removeItemFromArr(params.updateList, "genderNames");
        removeItemFromArr(params.updateList, "targetMemberId");
        removeItemFromArr(params.updateList, "isEdit");

        startLoader();
        cmdsBlackOut(true);
        var isAddMember = true;
        if (subTargetPage === "EDIT_STUDENT" || subTargetPage === "EDIT_MEMBER" || subTargetPage === "EDIT_OFFLINE_USER") {
            params.targetUserId = targetUserId;
            params.targetOrgMemberId = targetOrgMemberId;
            isAddMember = false;
        }
        var successFn = function(data) {
            var selectedProfileDiv = addEditPeoplePage.find(".selectedMemProfileDiv")
                    .removeClass("nonner");
            selectedProfileDiv.find(".memProfileName").text(memProfile);
            selectedProfileDiv.siblings(".selectMemProfileDiv").remove();

            targetUserId = data.result.userId;
            targetOrgMemberId = data.result.id;
            var fullname = "", lastName = "";
            if (params.lastName) {
                lastName = " " + params.lastName;
            }
            fullname = params.firstName + lastName;
            if (targetUserId === cmdsUserId) {

                topBar.find(".topBarUsername")
                        .text(fullname);
            }
            if (isAddMember) {
                trackEventForGA(memProfile, "ADD_MEMBER", fullname);
            }
            uploadProfilePic(stepBody);
        };
        postReq(url, params, successFn);
    };

    var accessMemAcademics = function(e, memStepHead) {
        if (uploadProfilePicTakingPlace) {
            alert("Please wait till the profile picture is uploaded");
            return;
        }
        memStepHead = memStepHead || $(this);
        var stepBody = memStepHead.next(".addMemStepBody");
        var targetDiv = stepBody.find(".coursesvChooseDiv");
        if (memProfile === "TEACHER") {
            if (targetDiv.children(".vChoose").length === 0) {
                var successFn = function(data) {
                    targetDiv.html(data);
                };
                getReq("/qrboards/coursesvChoose", {}, successFn);
            }
            stepBody.find(".openCoursevChoose").removeClass("nonner");
        } else {
            targetDiv.html("");
            qrAcadStr.getAcadEntities.PROGRAM(undefined, true);
        }
    };
    var prepareOldMappings = function() {
        var mappings = addMemAddedAcademics.children(".addedMapping");
        courseSectionMappingCache = {};
        for (var p = 0; p < mappings.length; p++) {
            var mapping = mappings.eq(p), $data = mapping.data();
            $data.secsStr = $data.sectionName;
            $data.sectionIds = [$data.sectionId];
            setCourseSecCache($data.courseId, [$data.sectionId]);
            appendCourseSecMappingUtil($data, "LOAD_OLD_MAPPINGS");
            mapping.remove();
        }
        if (memProfile === "STUDENT") {
            paidSectionIds = [];
            var paidMappings = addMemAddedAcademics.children(".addMappingPaid");
            for (var p = 0; p < paidMappings.length; p++) {
                var mapping = paidMappings.eq(p);
                var $data = mapping.data();
                addPaidEntry($data);
                mapping.remove();
            }
        }
    };
    var addPaidEntry = function($data) {
        var addMemProgCenter = makeHTMLTag("div", {"class": "addMemProgCenter"});
        var endDate = $data.endTime;
        var date = new Date(endDate);
        var dateStr = date.getDate();
        var monthStr = date.getMonth() + 1;
        var yearStr = date.getFullYear();
        var endTimeStr;
        if ($data.endTime ==0){
            endTimeStr = "Unlimited";
        }else{
            endTimeStr = dateStr +'/'+ monthStr +'/'+ yearStr;
        }
        addMemProgCenter.html(getProgCenterHTML($data.programName,
                $data.centerName, $data.sectionName, endTimeStr , $data.endTime, $data.sectionId));
        paidSectionIds.push($data.sectionId);
        addMemProgCenter.find(".removeProgCenterSecs").remove();
        $("#memPaidPrograms").append(addMemProgCenter);
    };
    this.getProgsOfCourses = function(vChoose, value) {
        qrAcadStr.getAcadEntities.PROGRAM();
    };
    var markAddedCourseSecs = function() {
        var courseId = addEditPeoplePage.find(".coursesvChooseDiv .vChoose").data("value");
        if (!courseId) {
            courseId = "NO_COURSE";
        }
        var cBoxes = $("#acadStrTable").find(".gCBox");
        for (var k = 0; k < cBoxes.length; k++) {
            var cBox = cBoxes.eq(k);
            var sectionId = cBox.data("value");
            var isPaid = cBox.closest(".ASTItem").data("paid");
            if (isPaid&&memProfile==="STUDENT") {
                if (paidSectionIds.indexOf(sectionId) > -1) {
                    cBox.toggleClass("gCBox gCBoxNoMoreTick");
                }
            } else {
                var cache = courseSectionMappingCache[courseId];
                if (cache && cache.indexOf(sectionId) !== -1) {
                    cBox.addClass("gCBoxChecked secSelected");
                }
            }
        }
    };
    this.markAddedCourseSecs = markAddedCourseSecs;
    var selectedPaidSectionParams;
    var finaliseSecsForMem = function() {
        var sectionsResult = analyseSectionsState($(this).closest(".ASTResultsHolder"));
        var alreadyAddedPaidSectionNames = sectionsResult.alreadyAddedPaidSectionNames;
        var paidSectionsToAddIds = sectionsResult.paidSectionsToAddIds;
        var paidSectionsToAddNames = sectionsResult.paidSectionsToAddNames;
        var tableParams = qrAcadStr.getAcadStrTableParams();

        var removeFromSections = sectionsResult.removeFromSections;
        var addToSections = sectionsResult.addToSections;
        if (memProfile === "STUDENT") {
            if ((addToSections.length > 0 && paidSectionsToAddIds.length > 0)
                    || paidSectionsToAddIds.length > 1) {
                showError("You can only add one paid program at a time and\n\
             do not add a paid program in combination with free programs");
                return;
            } else if (paidSectionsToAddIds.length === 1) {
                startLoader();
                getReq("/qrpeople/addSaleDetails", {sectionId: paidSectionsToAddIds[0]}, function(data) {
                    stopLoader();
                    var popup = getcmdsPopupBody(800);
                    popup.html(data);
                });
                selectedPaidSectionParams = $.extend({sectionIds: paidSectionsToAddIds,
                    sectionId: paidSectionsToAddIds[0], sectionName: paidSectionsToAddNames[0]}, tableParams);
                return;
            }
        }

        tableParams.secsStr = sectionsResult.secsStr;
        tableParams.sectionIds = sectionsResult.sectionIds;

        var courseId = tableParams.courseId || null;
        var successFn = function() {
            setCourseSecCache(courseId, addToSections, removeFromSections);
            appendCourseSecMappingUtil(tableParams);
            qrAcadStr.resetASTResultsTd(["ASTSectionsTd"]);
            if (cmdsUserId === targetUserId) {
                //to reset the programs list on menu and memberinfopojo of the current user
                getMemberInfoPojo(true);
            }
        };
        if (addToSections.length === 0 && removeFromSections.length === 0) {
            qrAcadStr.resetASTResultsTd(["ASTSectionsTd"]);
            return;
        }
        var params = tableParams;
        var pr = {removeFromSections: removeFromSections, addToSections: addToSections};
        if (courseId) {
            pr["courseIds"] = courseId;
        }
        $.extend(params, pr);
        updateInstructorCourseSecUtil(params, successFn);
    };
    var removeProgCenterSecs = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            return;
        }
        var progCenter = $this.closest(".addMemProgCenter"), $data = progCenter.data();
        var eachCourseDiv = progCenter.closest(".addMemEachCourse");
        var courseIds = [];
        if (memProfile === "TEACHER") {
            courseIds.push(eachCourseDiv.data("courseId"));
        } else {
            courseIds = null;
        }
        var params = $.extend({}, $data, {removeFromSections: $data.sectionIds,
            addToSections: [], courseIds: courseIds});
        var removeProgCenter = function() {
            $this.removeClass("btnDisabled");
            progCenter.remove();
            if (eachCourseDiv.children(".addMemProgCenter").length === 0) {
                eachCourseDiv.remove();
            }
            if (cmdsUserId === targetUserId) {
                //to reset the programs list on menu and memberinfopojo of the current user
                getMemberInfoPojo(true);
            }
        };
        $this.addClass("btnDisabled");
        updateInstructorCourseSecUtil(params, removeProgCenter);
    };
    var addStudentToPaidProgram = function() {
        $this = $(this);
        $cmdsPopup = $this.closest("#cmdsPopup")
        if (memProfile !== "STUDENT" || !selectedPaidSectionParams) {
            return;
        }
        var params = $.extend({targetUserId: targetUserId, targetOrgMemberId: targetOrgMemberId,
            targetProfile: memProfile, createTransaction: true}, selectedPaidSectionParams);
        var formValues = getFormValues($cmdsPopup);
        if (formValues.hasError) {
            showcmdsPopupError();
            return;
        }

        var selectedPackageDays = $cmdsPopup.find(".packageSelect").val();
        var selectedPackage = $cmdsPopup.find(".packageSelect").find(":selected");
        var hasPaymentFormError = false;
        var saleDetailsInfo = {};
        var paymentItems = [];
        paymentItems = parsePaymentItems($cmdsPopup);
        if (paymentItems == null) {
            showcmdsPopupError($cmdsPopup, "Payment fields can't be empty!");
            return;
        }

        saleDetailsInfo["origSaleAmount"] = selectedPackage.data("cost");
        saleDetailsInfo["paymentItems"] = paymentItems;
        saleDetailsInfo["discountPercentage"] = $("#discountPercentage").val();
        saleDetailsInfo["roundOff"] = $("#roundoffInput").val()*100;
        var successFn = function(data) {
            closecmdsPopup();
            stopLoader();
            var newPaidSectionMapping = data.result.newlyAddedMapping;
            var newPaidSectionEntry = $.extend({"endTime": newPaidSectionMapping.endTime}, selectedPaidSectionParams);
            addPaidEntry(newPaidSectionEntry);
            selectedPaidSectionParams = null;
            qrAcadStr.resetASTResultsTd(["ASTSectionsTd"]);
        };
        startLoader();
        $.extend(params, formValues);
        $.extend(params, {"packageDays" : selectedPackageDays,
            "returnNewlyAddedMapping" : true, "saleDetailsInfo" : saleDetailsInfo });
        postReq("/QrPeople/addMemToAcadStr", params, successFn);
    };
    var parsePaymentItems = function(cmdsPopup) {
        var paymentItems = [];
        var paymentTrs = cmdsPopup.find(".paymentItemsTable").find(".paymentItemTr");
        var hasPaymentFormError = false;
        paymentTrs.each( function() {
            var paymentItem = {};
            var tr = $(this);
            var paymentType = tr.find('.paymentTypeSelect').val();
            paymentItem["paymentType"] = paymentType;
            tr.find('.paymentInput').each(
                 function() {
                     var itemVal = $(this).val().trim();
                     if (itemVal === undefined || itemVal === null) {
                         itemVal = "";
                     }
                     if (itemVal === "") {
                         hasPaymentFormError = true;
                         return false;
                     }
                     if ($(this).attr("name") === "amount") {
                         paymentItem[$(this).attr("name")] = itemVal * 100;
                     }  else if($(this).attr("name") === "payableDate") {
                         var pDate = new Date(this.value);
                         console.log(pDate);
                         console.log(pDate.getTime());
                         if(pDate != null) {
                             paymentItem[$(this).attr("name")] = pDate.getTime();
                         }
                     } else {
                         paymentItem[$(this).attr("name")] = itemVal;
                     }
            });
            paymentItems.push(paymentItem);
        });
        if (hasPaymentFormError) return null;
        return paymentItems;
    };
    var addPaymentItem = function() {
        var $this = $(this);
        var paymentItemsTable = $(".paymentItemsTable");
        var paymentItemTr ="<tr class=\"paymentItemTr\"><td><select class=\"paymentTypeSelect\">";
        paymentItemTr += "<option value=\"CASH\"> CASH </option>";
        paymentItemTr += "<option value=\"CHEQUE\"> CHEQUE </option>";
        paymentItemTr += "<option value=\"PAYTM\"> PAYTM </option>";
        paymentItemTr += "<option value=\"ETRANSFER\"> ETRANSFER </option>";
        paymentItemTr += "</select></td>";
        paymentItemTr += "<td><input type=\"number\" name=\"amount\" class=\"paymentInput\" placeholder=\"Amount\"></td>";
        paymentItemTr += "<td class=\"paymentEntryDeleteTd\"><span class=\"paymentEntryDeleteNotation\"></span></td>";
        paymentItemTr += "</tr>";
        paymentItemsTable.append(paymentItemTr);
    };
    var deletePaymentItem = function() {
        var $this = $(this);
        $this.closest("tr").remove();
        return false;
    }
    var changePaymentType = function() {
        var $this = $(this);
        var paymentItemTr = $this.closest("tr");
        var selectedType = $this.val();
        paymentItemTr.find("td:gt(1)").remove();
        switch (selectedType) {
            case "CASH" :
                break;
            case "CHEQUE" :
                paymentItemTr.append("<td><input name=\"bankName\" class=\"paymentInput\" placeholder=\"Bank Name\"></td>");
                paymentItemTr.append("<td><input type=\"number\" class=\"paymentInput\" name=\"chequeNumber\" placeholder=\"Cheque Number\"></td>");
                paymentItemTr.append("<td><input type=\"date\" class=\"paymentInput\" name=\"payableDate\" placeholder=\"Payable Date\"></td>");
                break;
            case "PAYTM" :
                paymentItemTr.append("<td><input name=\"reference\" class=\"paymentInput\" placeholder=\"Reference\"></td>");
                break;
            case "ETRANSFER" :
                paymentItemTr.append("<td><input name=\"reference\" class=\"paymentInput\" placeholder=\"Reference\"></td>");
                break;
        }
        paymentItemTr.append("<td class=\"paymentEntryDeleteTd\"><span class=\"paymentEntryDeleteNotation\"></span></td>");
    };
    var calculateTotalAmount = function() {
        var selectedPackage = $(".packageSelect").find(":selected");
        var maxDiscount = $(".packageSelect").data("maxDiscount");
        var selectedPackageCost = selectedPackage.data("cost");
        var discountPercentage = $("#discountPercentage").val();
        var roundoff = $("#roundoffInput").val()*100;
        if (discountPercentage > maxDiscount || discountPercentage < 0 ) {
            showError("Max discount exceeds the limit");
            $("#discountPercentage").val("0.0");
            return;
        }
        if (roundoff > MAX_ROUNDOFF) {
            showError("Roundoff discount exceeds the limit");
            $("#roundoffInput").val("0.0");
            return;
        }
        var totalAmount = selectedPackageCost - (discountPercentage * selectedPackageCost / 100.0) - roundoff;
        totalAmount = Math.round(totalAmount / 100);
        $("#totalAmountDisplay").text(totalAmount);
    }
    var changeSelectedPackage = function() {
        calculateTotalAmount();
    }
    var changeDiscountPercentage = function() {
        calculateTotalAmount();
    }
    var changeRoundoffInput = function() {
        calculateTotalAmount();
    }
    var openCoursevChoose = function() {
        var vChoose = $(this).closest(".addMemStepBodyMain").find(".vChoose");
        vChooseVar.reset(vChoose, "-1", "Select a Course");
        qrAcadStr.resetASTResultsTd(["ASTProgramsTd", "ASTCentersTd", "ASTSectionsTd"]);
        $(window).scrollTop(vChoose.offset().top - 100);
    };
    var updateInstructorCourseSecUtil = function(reqParams, successFn) {
        var params = $.extend({targetUserId: targetUserId, targetOrgMemberId: targetOrgMemberId,
            targetProfile: memProfile}, reqParams);
        var removeFromSections = params.removeFromSections;

        var addUpdateSectionIdsPojo = getAddUpdateSectionIdsPojo(params);
        var newAddSectionIds = addUpdateSectionIdsPojo.newSectionIds;
        var updateAddSectionIds = addUpdateSectionIdsPojo.updateSectionIds;
        if (newAddSectionIds.length > 0) {
            params.sectionIds = newAddSectionIds;
            postReq("/QrPeople/addMemToAcadStr", params, successFn);
        }
        if (updateAddSectionIds.length > 0) {
            params.sectionIds = updateAddSectionIds;
            postReq("/QrPeople/addCourseToMemAcadStr", params, successFn);
        }

        if (removeFromSections.length > 0) {
            params.sectionIds = removeFromSections;
            var url = "removeMemFromAcadStr";
            if (params.courseIds) {
                params.removeCourseIds = params.courseIds;
                url = "removeCourseFromMemAcadStr";
            }
            postReq("/QrPeople/" + url, params, successFn);
        }
    };
    var getAddUpdateSectionIdsPojo = function(params) {
        var addToSections = params.addToSections;
        var progCenterEls = addEditPeoplePage
                .find("." + getCenterProgClassName(params));
        return getNewUpdateSectionIdsPojo(progCenterEls, addToSections);
    };
    var getRemoveUpdateSectionIdsPojo = function() {

    };
    var getNewUpdateSectionIdsPojo = function(progCenterEls, sectionIdsToCheck) {
        var newSectionIds = [], updateSectionIds = [];
        if (progCenterEls.length > 0) {
            for (var p = 0; p < sectionIdsToCheck.length; p++) {
                var found = false;
                var secId = sectionIdsToCheck[p];
                for (var k = 0; k < progCenterEls.length; k++) {
                    var sectionIdsOfProgCenter = progCenterEls.eq(k).data("sectionIds");
                    if (sectionIdsOfProgCenter.indexOf(secId) > -1) {
                        found = true;
                        updateSectionIds.push(secId);
                        break;
                    }
                }
                if (!found) {
                    newSectionIds.push(secId);
                }
            }
        } else {
            newSectionIds = sectionIdsToCheck;
        }
        return {newSectionIds: newSectionIds, updateSectionIds: updateSectionIds};
    };
    var analyseSectionsState = function(astResultsHolder) {
        var astItems = astResultsHolder.find(".ASTItem");
        var sectionsSelected = [];
        var addToSections = [];
        var removeFromSections = [];
        var sectionIds = [];
        var alreadyAddedPaidSectionNames = [];
        var paidSectionsToAddIds = [];
        var paidSectionsToAddNames = [];
        for (var k = 0; k < astItems.length; k++) {
            var astItem = astItems.eq(k);
            var sectionId = astItem.data("sectionId");
            var isPaidSection = astItem.data("paid");
            var sectionName = astItem.data("sectionName");

            if (isPaidSection && memProfile === "STUDENT") {
                if (astItem.find(".gCBoxNoMoreTick").length > 0) {
                    alreadyAddedPaidSectionNames.push(sectionName);
                } else if (astItem.find(".gCBoxChecked").length > 0) {
                    paidSectionsToAddIds.push(sectionId);
                    paidSectionsToAddNames.push(sectionName);
                }
            } else {
                var isSelectedPreviously = (astItem.find(".secSelected").length > 0);
                var isChecked = (astItem.find(".gCBoxChecked").length > 0);
                if (isChecked) {
                    sectionsSelected.push(sectionName);
                    sectionIds.push(sectionId);
                }
                if (!isSelectedPreviously && isChecked) {
                    addToSections.push(sectionId);
                } else if (isSelectedPreviously && !isChecked) {
                    removeFromSections.push(sectionId);
                }
            }
        }
        return {secsStr: sectionsSelected.join(","), addToSections: addToSections,
            removeFromSections: removeFromSections, sectionIds: sectionIds,
            alreadyAddedPaidSectionNames: alreadyAddedPaidSectionNames,
            paidSectionsToAddIds: paidSectionsToAddIds,
            paidSectionsToAddNames: paidSectionsToAddNames
        };
    };
    var createCourseDiv = function(courseId, courseName) {
        courseName = courseName || '';
        addMemAddedAcademics.append("<div class='addMemEachCourse addMemEachCourse_" + courseId + "' \n\
            data-course-id='" + courseId + "'>\n\
            <div class='addMemCourseHead'>" + courseName + "</div></div>");
        return addMemAddedAcademics.children(".addMemEachCourse").last();
    };
    var createProgCenter = function(courseDiv, tableParams, secsStr, endTime) {
        var date = new Date(endTime);
        var dateStr = date.getDate();
        var monthStr = date.getMonth() + 1;
        var yearStr = date.getFullYear();
        var endTimeStr;
        if (endTime ==0){
            endTimeStr = "Unlimited";
        }else{
            endTimeStr = dateStr +'/'+ monthStr +'/'+ yearStr;
        }
        var progName = tableParams.programName;
        var centerName = tableParams.centerName;
        var sectionId = tableParams.sectionId;
        var addMemProgCenter = makeHTMLTag("div",
                {"class": "addMemProgCenter " + getCenterProgClassName(tableParams)});
        addMemProgCenter.html(getProgCenterHTML(progName, centerName, secsStr, endTimeStr,endTime,sectionId));
        courseDiv.append(addMemProgCenter);
        return addMemProgCenter;
    };
    var getProgCenterHTML = function(progName, centerName, secsStr, endTimeStr,endTime,sectionId) {
        return "<span class='amsdProg'>" + progName + "</span>\n\
                                <span class='addMemSecSep'>></span>\n\
                                <span class='amsdProg'>" + centerName + "</span>\n\
                                <span class='addMemSecSep'>></span>\n\
                                <span class='amsdSecs'>" + secsStr + "</span>\n\
                                <span class='exipresOn'>| Expires on : </span>\n\
                                <span class='endTime'>" + endTimeStr + "</span>\n\
                                <a class='floatRight smally removeProgCenterSecs'>Remove</a>\n\
        <a class='floatRight smally editSubcriptionDate' sectionId='"+sectionId+"'endTime='"+endTime+"' style='margin-right: 1%;'>Edit Subcription Date</a>\n\
        <a class='floatRight smally editSaleDetails' sectionId='"+sectionId+"'style='margin-right: 1%;'>Edit Sale Details</a>";
    };
    var setCourseSecCache = function(courseId, addSectionIds, removeSectionIds) {
        courseId = courseId || "NO_COURSE";
        var courseSecMapper = courseSectionMappingCache[courseId];
        if (!courseSecMapper) {
            courseSectionMappingCache[courseId] = [];
        }
        courseSecMapper = courseSectionMappingCache[courseId];
        if (addSectionIds) {
            for (var k = 0; k < addSectionIds.length; k++) {
                courseSecMapper.push(addSectionIds[k]);
            }
        }
        if (removeSectionIds) {
            for (var k = 0; k < removeSectionIds.length; k++) {
                var index = courseSecMapper.indexOf(removeSectionIds[k]);
                courseSecMapper.splice(index, 1);
            }
        }
    };
    var appendCourseSecMappingUtil = function(params, type) {
        var courseId = params.courseId || "NO_COURSE";
        var courseDiv = addMemAddedAcademics.children(".addMemEachCourse_" + courseId);
        if (courseDiv.length === 0) {
            courseDiv = createCourseDiv(courseId, params.courseName);
        }
        var progCenter = courseDiv.find("." + getCenterProgClassName(params));
        var sectionIds = [];
        if (progCenter.length === 0) {
            progCenter = createProgCenter(courseDiv, params, params.secsStr, params.endTime);
            sectionIds = params.sectionIds;
        } else {
            var secsStr;
            if (type === "LOAD_OLD_MAPPINGS") {
                sectionIds = progCenter.data("sectionIds");
                sectionIds.push(params.sectionId);
                secsStr = progCenter.find(".amsdSecs").text().trim().split(",");
                secsStr.push(params.sectionName);
                secsStr = secsStr.join(",");
            } else {
                sectionIds = params.sectionIds;
                secsStr = params.secsStr;
            }
            progCenter.find(".amsdSecs").html(secsStr);
            if (sectionIds.length === 0) {
                progCenter.remove();
            }
        }
        var $programData = progCenter.data();
        $.extend($programData, params);
        progCenter.data("sectionIds", sectionIds);
    };
    var getCenterProgClassName = function(params) {
        return "addMemProgCenter_" + params.programId + "_" + params.centerId;
    };
    var addMemAcademicsSubmit = function() {
        var stepBody = $(this).closest(".addMemStepBody");
        collapseAddMemStepBody(stepBody);
        accessMemRole(stepBody.next('.addMemStepHead'));
        takeToBackPage();
    };
    var takeToBackPage = function() {
        var backUpUrl = vcmdsUrls.MEMBER(targetUserId);
        if (memProfile === "STUDENT") {
            backUpUrl = vcmdsUrls.STUDENT(targetUserId);
        } else if (memProfile === "OFFLINE_USER") {
            backUpUrl = vcmdsUrls.OFFLINE_USER(targetUserId);
        }
        goToBackPage(backUpUrl);
    };



    var accessMemRole = function(e, memStepHead) {
        memStepHead = memStepHead || $(this);
    };
    var addMemRoleSubmit = function() {
        var stepBody = $(this).closest(".addMemStepBody");
        var role = stepBody.find(".vChoose").data("value");
        if (role == "-1") {
            showError("Please choose a role.");
            return;
        }
        var successFn = function() {
            takeToBackPage();
            closePopup();
        };
        postReq("/QrPeople/setroleofmember", {role: role}, successFn);
    };

    //utils
    var collapseAddMemStepBody = function(stepBody) {
        stepBody.addClass("nonner").prev('.addMemStepHead').removeClass("nonner");
        var nextStepBody = stepBody.nextAll().eq(1);
        nextStepBody.removeClass("nonner");
        nextStepBody.prev('.addMemStepHead').addClass("nonner");
    };
    var addMemStepHead = function() {
        var $this = $(this);
        $this.siblings(".addMemStepHead").removeClass("nonner");
        $this.siblings(".addMemStepBody").addClass("nonner");
        $this.addClass("nonner").next('.addMemStepBody').removeClass("nonner");
    };




    var uploadProfilePic = function(stepBody) {
        var u = uploadProfilePicUtil;
        u.qqUploadVar.setParams({targetOrgMemberId: targetOrgMemberId,
            targetUserId: targetUserId, uploadFileParamName: "inputFile"});
        if (u.qqUploadVar && u.picFile) {
            u.qqUploadVar._onInputChange(u.picFile);
            uploadProfilePicTakingPlace = true;
            startLoader();
            var fn = function(data) {
                if (targetUserId === cmdsUserId) {
                    $("#topBar").find(".topBarUserImg")
                            .attr("src", data.result.thumbnail);
                }
                collapseAddMemStepBody(stepBody);
                accessMemAcademics(null, stepBody.next('.addMemStepHead'));
            };
            var completeFn = function() {
                uploadProfilePicTakingPlace = false;
                stopLoader();
                cmdsBlackOut(false);
                closePopup();
            };
            u.cbfn = fn;
            u.completeFn = completeFn;
        } else {
            stopLoader();
            cmdsBlackOut(false);
            closePopup();
            collapseAddMemStepBody(stepBody);
            accessMemAcademics(null, stepBody.next('.addMemStepHead'));
        }
    };
    //making caps of memberid
    var memberIdInputted = function(e) {
        memberIdCapsUtil($(this));
    };
    var memberIdInputPasted = function(e) {
        var $this = $(this);
        setTimeout(function() {
            memberIdCapsUtil($this);
        }, 10);
    };
    var memberIdCapsUtil = function($this) {
        var inputText = $this.val();
        if (inputText) {
            $this.val(inputText.toUpperCase());
        }
    };



    var editEndDateOfProgramSubscription = function(){
        var endDateVal = $(this).attr('endTime');
        var sectionIdVal = $(this).attr('sectionId');
        var params = {
            "sectionId":sectionIdVal,
            "targetUserId":targetUserId,
            "targetOrgMemberId":targetOrgMemberId
        }
        var endDate = parseInt(endDateVal);
        var popup = getcmdsPopupBody(480);
        smallLoader(popup);
        vReq.get("/QrPeople/editEndDatePopup", params , function(data) {
            popup.html(data);
            var date = new Date(endDate);
            setvChooseValue( $(popup).find('.datevChoose'), date.getDate() );
            setvChooseValue( $(popup).find('.monthvChoose'), date.getMonth() + 1 );
            setvChooseValue( $(popup).find('.yearvChoose'), date.getFullYear() );
        });

    };

    var editSaleDetailsOfSubscription = function(){
        var sectionIdVal = $(this).attr('sectionId');
        console.log("Section ID: " + sectionIdVal);
        var params = {
                "sectionId":sectionIdVal,
                "targetUserId":targetUserId
            }
        var popup = getcmdsPopupBody(800);
        smallLoader(popup);
        vReq.get("/QrPeople/editSaleDetails", params, function(data) {
            popup.html(data);
        });
    };

    var updateEndDateOfSubscription = function(){
        var popup = getPopupDiv($(this));
        var dateDivs = popup.find(".datevChooseDiv");
        var dateFn = getDateMillisFromvChoose;
        var endDate = dateFn(dateDivs.eq(0));
        var userDetails = popup.find(".detailsOfUser");
        var $data = userDetails.data();
        var params = $data;
        params.endTime=endDate;
        var successFn = function() {
            closePopup();
            showMessage("Successfully edited end time",refreshPage);
        };
        postReq("/QrPeople/updateEndDate", params, successFn);
    };

    var updateSaleDetails = function(){
        console.log("Update sale details function");
        $this = $(this);
        $cmdsPopup = $this.closest("#cmdsPopup")

        var paymentItems = [];
        paymentItems = parsePaymentItems($cmdsPopup);
        if (paymentItems == null) {
            showcmdsPopupError($cmdsPopup, "Payment fields can't be empty!");
            return;
        }

        var saleDetailsId = $this.data('saleDetailsId');
        var params = {
                "saleDetailsId" : saleDetailsId,
                "targetUserId":targetUserId,
                "targetOrgMemberId":targetOrgMemberId,
                "paymentItems": paymentItems
        };
        var successFn = function(data) {
            closecmdsPopup();
            stopLoader();
        }
        startLoader();
        postReq("/QrPeople/updateSaleDetails", params, successFn);
        console.log(params);
    };

})(jQuery);

var getProgsOfCourses = function(vChoose, value) {
    qrAddEditPeople.getProgsOfCourses(vChoose, value);
};

