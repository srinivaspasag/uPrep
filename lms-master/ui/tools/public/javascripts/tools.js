var tools = new (function() {
    this.init = function() {
        $(document)
//                .on("click", ".addNewLocation", addNewLocation)
//                .on("click", ".removeLocation", removeLocation)
                .on("click", ".approveOrg", approveOrg)
                .on("click", ".viewAppCredential", viewAppCredential)
                .on("click", ".restartAll", restartAll)
                .on("click", ".getDirectURL", getDirectURL)
//                .on("click", ".addOrgButton", function(e){
//			showMessage("Use website add org url to add new Organization in Vedantu.");
//			e.preventDefault();
//		})

                //org page
                .on("click", ".uploadOrgBoards", uploadOrgBoards)
                .on("click", ".uploadOrgPic", uploadOrgPic)

                //boards
                .on("submit", "form", formCheck)
                .on("click", ".getMyChildren", getMyChildren)
                .on("click", ".removeBoard", removeBoard)
                
		.on("click", ".showUpdateOrgSlug", showUpdateOrgSlug)
                .on("click", ".updateOrgSlug", updateOrgSlug)
//                .on("keyup", ".checkSlugAvailability input", checkSlugAvailability)
//                .on("blur", ".checkSlugAvailability input", doneSlugCheck)
                .on("click", ".editStatus", showUpdateOrgStatus)
                .on("click",".editSharedSubjectStatus",showSharedSubjectStatus)
                .on("click",".editClassroomConnectStatus",editClassroomConnectStatus)
                .on("click",".editDownloadStatus",editDownloadStatus)
                .on("click", ".updateOrgStatus", updateOrgStatus)
                .on("click",".updateSharedSubjectsStatus",updateSharedSubjectsStatus)
                .on("click",".updateClassroomConnectStatus",updateClassroomConnectStatus)
                .on("click",".updateDownloadStatus",updateDownloadStatus)
		.on("click", ".showUpdateOrgReferer", showUpdateOrgReferer)
		.on("click", ".removeOrgReferer", removeOrgReferer)
                .on("click", ".updateOrgReferer", updateOrgReferer)
                .on("change", ".checkRefererAvailability input", checkRefererAvailability)
                .on("blur", ".checkRefererAvailability input", doneRefererCheck)
                
//		.on("submit.ADD_ORG", "#addOrgForm", addOrgFormSubmit)

                //licensing
                .on("change", "input[name=peruser]", togglePerUser)
                .on("click", ".createPricingPlanSubmit", createPricingPlanFormSubmit)
                .on("click", ".showPlanDetails,.hidePlanDetails", togglePlanDetails)
                .on("change", ".changePlanState", changePlanState)
    };
    var togglePerUser = function() {
        if ($(this).val() === "peruser") {
            $(".perUserAmt").show();
            $(".lumpSumUsersAmt").hide();
        } else {
            $(".perUserAmt").hide();
            $(".lumpSumUsersAmt").show();
        }
    };
    var createPricingPlanFormSubmit = function() {
        var $this = $(this);
        if ($this.hasClass("disabledBtn")) {
            return;
        }
        var form = $("#createPricingPlanForm");
        var params = getFormValues(form);

        if (params.name.trim().length === 0) {
            alert("Enter a plan name.");
            return;
        }

        var perUser = form.find("input[name=peruser]").eq(0);

        if (perUser.prop("checked")) {
            params.peruser = true;
            var cost = form.find(".costPerUser").val();
            if (!checkFloatNum(cost)) {
                alert("Enter a valid cost for the plan.");
                return;
            } else {
                params.cost = cost;
            }
        } else {
            params.peruser = false;
            var cost = form.find(".lumpSumCost").val();
            if (!checkFloatNum(cost)) {
                alert("Enter a valid cost for the plan.");
                return;
            } else {
                params.cost = cost;
            }
            var usersCount = form.find(".usersCount").val();
            if (!checkIntNum(usersCount)) {
                alert("Enter a maximum number of users.");
                return;
            } else {
                params.users = usersCount;
            }
            var usersLimitExceedAmt = form.find(".usersLimitExceedAmt").val();
            if (!checkFloatNum(usersLimitExceedAmt)) {
                alert("Enter a valid cost if the users exceed the plan.");
                return;
            } else {
                params.additionalCost = usersLimitExceedAmt;
            }
        }
        $this.text("Submitting...").addClass("disabledBtn");
        $.post("/licensing/createPricingPlanSubmit", params, function(resp) {
            $this.text("Submit").removeClass("disabledBtn");
            if (resp.errorCode !== "" || !resp) {
                alert("There was some problem: " + resp.errorCode);
            } else {
                window.location = "/licensing";
            }
        });
    };
    var togglePlanDetails = function() {
        var $this = $(this);
        var details = $this.closest("tr").find(".planDetails");
        if ($this.hasClass("showPlanDetails")) {
            details.show();
            $this.text("Hide Details");
        } else {
            details.hide();
            $this.text("Show Details");
        }
        $this.toggleClass("hidePlanDetails showPlanDetails");
    };
    var changePlanState = function() {
        var $this = $(this);
        var planId = $this.data("planId");
        var state = $this.val();
        if (state === "") {
            state = "DRAFT";
        }
        $.post("/licensing/markState", {planId: planId, state: state}, function(resp) {
            if (!resp || resp.errorCode !== "") {
                showError("Error : " + resp.errorMessage);
            }
            if(state==="OBSOLETE"){
                $("<b>OBSOLETE</b>").insertAfter($this);
                $this.remove();
            }else if(state === "ACTIVE"){
		$this.find("option[value='DRAFT']").remove();
	    }
        });
    };
    var showUpdateOrgSlug = function() {
        $(this).closest("table").find(".checkSlugAvailability").removeClass("nonner");
        $(this).closest(".orgSlugId").addClass("nonner");
    };
    var updateOrgSlug = function() {
        var slugInputDiv = $(this).closest(".checkSlugAvailability");
        var slugDiv = $(this).closest("table").find(".orgSlugId");
        if (slugInputDiv.find(".slugCheckStatus").data("status") === "success") {
            var text = slugInputDiv.find(":text").val();
            var orgId = $(this).data("orgId");
            $.post("/organizations/updateSlug", {slug: text, orgId: orgId}, function(resp) {
                if (!resp.errorCode && resp.result) {
                    slugInputDiv.addClass("nonner");
                    slugDiv.removeClass("nonner").find(".slugText").text(resp.result.slug);
                } else {
                    showError("Error occurred, try again later");
                }
            });
        }
    };
    var showUpdateOrgStatus = function() {
        var popup = showVPopup(0.6);
        var type = $(this).data("type");
        var status = $(this).data("status");
        $(".orgProfileStatus").val(type);
        popup.html($(".changeStatus").html());
        popup.find(".changeStatus").removeClass("nonner");
        //Show checked value in popup.
        popup.find("#"+status).attr("checked","checked");
    };
    var showSharedSubjectStatus = function(){
        var popup = showVPopup(0.6);
        popup.html($(".changeSharedSubjectsStatus").html());
        popup.find(".changeSharedSubjectsStatus").removeClass("nonner");
    }

    var editClassroomConnectStatus = function(){
        var popup = showVPopup(0.6);
        popup.html($(".changeClassroomConnectStatus").html());
        popup.find(".changeClassroomConnectStatus").removeClass("nonner");
    }

    var editDownloadStatus = function(){
        var popup = showVPopup(0.6);
        popup.html($(".changeDownloadStatus").html());
        popup.find(".changeDownloadStatus").removeClass("nonner");
    }

    var updateOrgStatus = function() {
        var type = $(".orgProfileStatus").val();
        var status = $("input[name='status']:checked").val();
        var url = "/organizations/updateStatus";
        var orgId = $(this).data("orgId");
        $.post(url, {status: status, orgId: orgId , type:type}, function(resp) {
            if (!resp.errorCode && resp.result) {
                closeVPopup();
                window.location.reload();
            } else {
                showError("Error occurred, try again later");
            }
        });
    };
    var updateSharedSubjectsStatus = function() {
        var showStatus = $("input[name='showSharedSubjects']:checked").val();
        var showSharedSubjects = false;
        if(showStatus ==="YES"){
            showSharedSubjects = true;
        }
        var orgId = $(this).data("orgId");
        $.post("/organizations/updateOrganizationSharedSubjects", {showSharedSubjects: showSharedSubjects, orgId: orgId}, function(resp) {
            if (!resp.errorCode && resp.result) {
                closeVPopup();
                window.location.reload();
            } else {
                showError("Error occurred, try again later");
            }
        });
    };

    var updateClassroomConnectStatus = function() {
        var showStatus = $("input[name='showClassroomConnect']:checked").val();
        var showClassroomConnect = false;
        if(showStatus ==="YES"){
            showClassroomConnect = true;
        }
        var orgId = $(this).data("orgId");
        $.post("/organizations/updateOrganizationClassroomConnectStatus", {showClassroomConnect: showClassroomConnect, orgId: orgId}, function(resp) {
            if (!resp.errorCode && resp.result) {
                closeVPopup();
                window.location.reload();
            } else {
                showError("Error occurred, try again later");
            }
        });
    };

    var updateDownloadStatus = function() {
        var showStatus = $("input[name='disableDownload']:checked").val();
        var disableDownload = false;
        if(showStatus ==="YES"){
            disableDownload = true;
        }
        var orgId = $(this).data("orgId");
        $.post("/organizations/updateOrganizationDownloadStatus", {disableDownload: disableDownload, orgId: orgId}, function(resp) {
            if (!resp.errorCode && resp.result) {
                closeVPopup();
                window.location.reload();
            } else {
                showError("Error occurred, try again later");
            }
        });
    };


    var slugChkXHR;
    var doneSlugCheck = function() {
        var $this = $(this);
        var msgBox = $this.siblings(".slugCheckStatus");
        if (msgBox.data("status") == "success") {
            $this.removeClass("errorBorder");
            $this.removeClass("successBorder");
            msgBox.text("").addClass("nonner");
        }
    };
    var checkSlugAvailability = function() {
        var $this = $(this);
        var msgBox = $this.siblings(".slugCheckStatus").removeClass("nonner");
        setTimeout(function() {
            var text = $this.val();
            if (text.length > 1) {
                if (slugChkXHR) {
                    slugChkXHR.abort();
                    slugChkXHR = undefined;
                }
                slugChkXHR = $.get("/organizations/checkSlug", {slug: text}, function(resp) {
                    if (!resp.errorCode && resp.result) {
                        if (resp.result.available) {
                            showAvailable();
                        } else {
                            showErrorMsg("Un-Available");
                        }
                    } else {
                        showErrorMsg("Invalid Slug format TIP: neither space nor special characters, only alphanumeric value");
                    }
                });
                clearMsg();
            } else {
                showErrorMsg("Too Short");
            }
        }, 10);
        function clearMsg() {
            $this.removeClass("errorBorder");
            $this.removeClass("successBorder");
            msgBox.text("").data("status", "");
        }
        function showErrorMsg(msg) {
            $this.removeClass("successBorder").addClass("errorBorder");
            msgBox.text(msg).addClass("redTextColor").removeClass("greenTextColor").data("status", "error");
        }
        function showAvailable() {
            $this.removeClass("errorBorder").addClass("successBorder");
            msgBox.text("Available").removeClass("redTextColor").addClass("greenTextColor").data("status", "success");
        }
    };
    var showUpdateOrgReferer = function() {
        $(this).closest("table").find(".checkRefererAvailability").removeClass("nonner");
        $(this).closest(".orgRefererId").addClass("nonner");
    };
    var removeOrgReferer = function(){
	    var orgRefererAdded = $(this).closest(".orgRefererAdded");
            var orgId = $(this).data("orgId");
            $.post("/organizations/updateReferer", {remove: true, orgId: orgId}, function(resp) {
                if (!resp.errorCode && resp.result) {
		    orgRefererAdded.html("Referer removed, to add again , refresh the page!");
		}
	    });
    };
    var updateOrgReferer = function() {
        var refererInputDiv = $(this).closest(".checkRefererAvailability");
        var refererDiv = $(this).closest("table").find(".orgRefererId");
        if (refererInputDiv.find(".refererCheckStatus").data("status") === "success") {
            var text = refererInputDiv.find(":text").val();
            var orgId = $(this).data("orgId");
            $.post("/organizations/updateReferer", {referer: text, orgId: orgId}, function(resp) {
                if (!resp.errorCode && resp.result) {
                    refererInputDiv.addClass("nonner");
                    refererDiv.removeClass("nonner").find(".orgRefererAdded")
			.removeClass("nonner").find(".refererText").text(resp.result.referer);
		    refererDiv.find(".orgRefererAdd").remove();
                } else {
                    showError("Error occurred, try again later");
                }
            });
        }
    };
    var refererChkXHR;
    var doneRefererCheck = function() {
        var $this = $(this);
        var msgBox = $this.siblings(".refererCheckStatus");
        if (msgBox.data("status") == "success") {
            $this.removeClass("errorBorder");
            $this.removeClass("successBorder");
            msgBox.text("").addClass("nonner");
        }
    };
    var checkRefererAvailability = function() {
        var $this = $(this);
        var msgBox = $this.siblings(".refererCheckStatus").removeClass("nonner");
        setTimeout(function() {
            var text = $this.val();
            if (text.length > 1) {
                if (refererChkXHR) {
                    refererChkXHR.abort();
                    refererChkXHR = undefined;
                }
                refererChkXHR = $.get("/organizations/checkReferer", {referer: text}, function(resp) {
                    if (!resp.errorCode && resp.result) {
                        if (resp.result.available) {
                            showAvailable();
                        } else {
                            showErrorMsg("Un-Available");
                        }
                    } else {
                        showErrorMsg("Invalid referer format");
                    }
                });
                clearMsg();
            } else {
                showErrorMsg("Too Short");
            }
        }, 10);
        function clearMsg() {
            $this.removeClass("errorBorder");
            $this.removeClass("successBorder");
            msgBox.text("").data("status", "");
        }
        function showErrorMsg(msg) {
            $this.removeClass("successBorder").addClass("errorBorder");
            msgBox.text(msg).addClass("redTextColor").removeClass("greenTextColor").data("status", "error");
        }
        function showAvailable() {
            $this.removeClass("errorBorder").addClass("successBorder");
            msgBox.text("Available").removeClass("redTextColor").addClass("greenTextColor").data("status", "success");
        }
    };
    var addNewLocation = function() {
        var locDiv = $(this).closest(".locationDiv");
        var newLoc = locDiv.clone(true);
        newLoc.find(".addNewLocation").toggleClass("addNewLocation removeLocation").text("Remove");
        newLoc.find("input").val("");
        locDiv.parent().append(newLoc);
        resetLocationAttrNames(locDiv.parent());
    };
    var removeLocation = function() {
        var target = $(this).closest(".locationDiv");
        var locationsTd = target.parent();
        target.remove();
        resetLocationAttrNames(locationsTd);
    };
    var resetLocationAttrNames = function(locationsTd) {
        var locDivs = locationsTd.find(".locationDiv");
        for (var k = 0; k < locDivs.length; k++) {
            var inputEls = locDivs.eq(k).find("input");
            for (var i = 0; i < inputEls.length; i++) {
                var el = inputEls.eq(i);
                var newAttrName = "locations[" + k + "]." + el.attr("rel");
                el.attr("name", newAttrName);
            }
        }
    };
    var addOrgFormSubmit = function(e) {
        e.preventDefault();
        var $this = $(this);

        var domain = $(this).find("input[name=emailDomain]").val();
        if (!validDomain(domain)) {
            alert("Enter a email Domain of the format given.");
            return;
        }

        var orgWebsiteField = $this.find("#orgWebsite");
        var website = orgWebsiteField.val();
        $.get("/organizations/checkWebsite", {website: website}, function(data) {
            var availability = data.result.available;
            if (availability) {
                $(document).off("submit.ADD_ORG");
                $this.submit();
            } else {
                orgWebsiteField.focus();
                orgWebsiteField.siblings(".orgWebsiteError").show();
            }
        });
    };

    var viewAppCredential = function(){
        var $this = $(this);
	var params = {
		appId : $this.data("appId"),
		orgId : $this.data("orgId")
	};
	var popup = showVPopup(0.7);
	popup.html("Loading...");
	var html = "<table border='1' class='popupTable' style='width:500px;' cellspacing=0>%{trs}</table>";
	var trs = "<tr><th>%{item}</th><td>%{value}</td></tr>";
        vReq.post("/organizations/viewAppCredential", params, function(data) {
		if(data && data.errorCode=="" && data.result){
			var data = data.result;
			var trHtml = "";
			for(key in data){
				var tr = trs.replace("%{item}",i18nJS(key));
				tr = tr.replace("%{value}",data[key]);
				trHtml += tr;
			};
			html = html.replace("%{trs}",trHtml);
			popup.html(html);
		};
	});
    };
    var restartAll  = function(){
        vReq.post("/organizations/restartAll")
        var popup = showVPopup(0.7);
    popup.html("Event Bus Console is Restarted");
    }
    var approveOrg = function() {
        var $this = $(this);
        vReq.post("/organizations/approveOrg", {orgId: $this.data("orgId")}, function(data) {
            var pass = data.result.adminPassword;
            var t;
            if (pass.length > 0) {
                t = "<div class='userMessage'>Admin password : " + pass + "</div>";
            } else {
                var adminEmail = $this.closest("td").data("adminEmail");
                var adminEmailText = "";
                if (adminEmail) {
                    adminEmailText = " " + adminEmail;
                }
                t = "<span class='smallThinGray' style='color:#333333;'>You created the organization with an \n\
                    existing user" + adminEmailText + ".Use his/her credentials to login.</span>";
            }
            $(t).insertAfter($this);
            $this.remove();
        });
    };



    //org page  
    var uploadOrgBoards = function() {
        $(this).parent().siblings(".uploadDiv").removeClass("nonner");
    };
    var uploadOrgPic = function() {
        var $this = $(this).parent();
        $this.siblings(".uploadDiv").addClass("nonner");
        $this.siblings(".uploadProfilePicDiv").removeClass("nonner");
    };


    //boards
    var allowedFileTypes = ["xlsx", "xls"];
    var formCheck = function() {
        var isSizeOk = true;
        var isExtOk = true;
        var maxSizeAllowed = "";
        var uploadFileName = "";
        $(this).find('input[type=file][max-size]').each(function() {
            if (typeof this.files[0] !== 'undefined') {
                var maxSize = (parseInt($(this).attr('max-size'), 10) * 1000),
                        size = this.files[0].fileSize || this.files[0].size;
                var name = this.files[0].name;
                uploadFileName = name;
                var ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
                if (allowedFileTypes.indexOf(ext) === -1) {
                    isExtOk = false;
                    return false;
                }
                isSizeOk = maxSize > size;
                if (!isSizeOk) {
                    maxSizeAllowed = maxSize;
                    return false;
                }
            }
        });
        if (!isSizeOk) {
            alert("Max file size allowed is " + (maxSizeAllowed / (1000 * 1024)) + "MB");
            return false;
        } else if (!isExtOk) {
            alert("Only " + allowedFileTypes.join(",") + " files are allowed");
            return false;
        }


        var isCBoxOk = true;
        var cBoxErr = "";
        $(this).find(".checkBoxesDiv").each(function() {
            var checkedBoxes = $(this).find("input:checked");
            var minSelect = parseInt($(this).data("minSelect"));
            if (checkedBoxes.length < minSelect) {
                cBoxErr = $(this).data("error");
                isCBoxOk = false;
                return false;
            }
        });
        if (!isCBoxOk) {
            alert(cBoxErr);
            return false;
        }
        $(this).find(".uploadFileName").val(uploadFileName);
    };
    var getMyChildren = function() {
        var $this = $(this);
        var brdId = $this.data("brdId");
        var orgId = $this.data("orgId");
        var params = {treeRootIds: [brdId], depth: 10, uiBoardId : brdId};
        if (orgId) {
            params.orgId = orgId;
        }
        var context = $this.data("context");
        var target = $this.parent().siblings(".rootNodeChildrenDiv").html("Loading....");
        var head = "Children";
        if ($this.data("title")) {
            head = "Children of " + $this.data("title");
        }
        $.get(getUrlsForBoardsTypes[context], params, function(data) {
            target.html(data).prepend('<div class="normalHeading" style="margin-top:30px;">' + head + '</div>');
        });
    };
    var getUrlsForBoardsTypes = {
        GLOBAL: "/boards/getChildrenOfGlobalBoardNode",
        CONSUMER: "/boards/getChildrenOfConsumerBoardNode",
        ORG: "/boards/getChildrenOfOrgBoardNode"
    };
    var removeBoard = function(){
        var $this = $(this);
        var brdId = $this.data("brdId");
        var parentBrdId = $this.data("parentBrdId");
        var orgId = $this.data("orgId");
        var params = {brdId: brdId, orgId: orgId, parentBoardId : parentBrdId};
	var confirmtxt = "<div class='boldy'>Are you sure to remove? can not be un-done!</div>";
	confirmtxt += "<div class='redTextColor big12'>All the content attached/related to this board, need to edit & publish again via CMDS.</div>";
	showVYesNoBox(confirmtxt,null,function(state){
		if(state){
			doRemove();
		}
	});
	function doRemove(){
		$this.text("Working...");
		$.post("/boards/remove",params,function(data){
			$this.text("");
			if(data && data.errorCode=="" && data.result.done){
				$this.parent().find(".brdName").addClass("deletedItem");	
				$this.remove();	
			}else{
				showError("Failed...retry later! "+data.errorCode);
				console.error(data);
			}
		});
	}
    };

    var getDirectURL = function() {
        $.get("/organizations/getDirectURL", {targetUserId: $(this).data("userId"), orgId: currentToolsOrgId}, function(data) {
            var result = data.result;
            var username = result.username, pass = result.password;
            var urlsHTML = "";
            if ($(".chooseMemberType").val() !== "STUDENT") {
                var cmdsUrl = cmdsUrlPrefix + "/directaccess?u=" + username + "&p=" + pass;
                urlsHTML += "<div style='margin:25px 0;'><div class='margBot5 boldy'>CMDS Url</div>" + cmdsUrl + "</div>";
            }
            var ileUrl = ileUrlPrefix + "/directaccess?u=" + username + "&p=" + pass;
            urlsHTML += "<div style='margin:25px 0;'><div class='margBot5 boldy'>ILE Url</div>" + ileUrl + "</div>";
            getToolsPopupBody().html(urlsHTML);
        });
    };
});
tools.init();

$(document).on("focus", "input", function() {
    $(this).addClass("inputDivFocused");
});
$(document).on("blur", "input", function() {
    $(this).removeClass("inputDivFocused");
});


var getCommonPopupBody = function(width, height) {
    var popup = $("#toolsPopup");
    popup.show().css("min-height", height + "px").width(width);
    var top = (120 + $(window).scrollTop()) + "px";
    popup.css("top", top);
    return popup.children(".toolsPopupBody");
};
var getToolsPopupBody = function(width, height) {
    var popup = $("#toolsPopup");
    popup.height(height);
    var popupBody = getCommonPopupBody(width, height);
    return popupBody;
};
$(document).on("click", ".closetoolsPopup", function() {
    console.log($("#toolsPopup"))
    $("#toolsPopup").hide();
});


//bbNYMIRt
//http://localhost:19003/application/addorganization?name=Laskhya+Institute&fullName=Laskhya+Institute&website=http%3A%2F%2FlaskhyaInstitute.com&emailDomain=gmail.com&contactNumber=9997778246&type=COLLEGE&locations[0].city=Patiala&locations[0].state=Punjab&locations[0].country=India&address=fllore+2&description=&scope=PUBLIC&representative.firstName=ajith&representative.lastName=&representative.email=ajith%40vedantu.com



