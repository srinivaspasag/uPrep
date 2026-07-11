var myPrograms = new function(){
    var parDiv;
    var endiv;
    var orgId,targetOrgMemberId,targetProfile;
    var CLICK = "click.myPrograms";
    // var showLoadingDiv = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
    var mySectionsList = {};
    this.init = function(paymentInfo){
        $(".myEnrolledPrograms").find(".read").css("display","none");
        parDiv = $("#myProgramsPage");
        parDiv.on(CLICK,".myEnrolledPrograms .siteProgramHolder",openEnrolledProgPopup)
        parDiv.on(CLICK,".myEnrolledPrograms .lpProgramHolder",openEnrolledProgPopup)
            .on(CLICK,".showUserPaymentInfo",showUserPaymentInfoClicked);
        parDiv.on("change","#subscription",subscription);
        getSections();
        var org = $("#myInstitutePage").data("orgObj");
        orgId = org.orgId;
        targetOrgMemberId = org.orgMemberId;
        targetProfile = org.userRole;
        $(document)
            .off(CLICK)
            .on(CLICK,".joinProgramForFree",joinNow)
            .on(CLICK,".signupPaidProgram",buyNow)
        // $("#categoryProgramsSection").on("click",".signupPaidProgram",buyNow);
        parDiv.on(CLICK,".progTypeTab",tabChanged);
        postPayment(paymentInfo);

        var url = (window.location).href;
        if(url.indexOf("sectionId")>-1 && url.indexOf("packageDays")>-1){
            showOverlay();
            showLoader();
            $(window).bind("load",function(){
                var sectionId = getParameterByName("sectionId",url);
                var packageDays = getParameterByName("packageDays",url);
                var popup = $("#allPrograms ."+sectionId);
                if(popup.length == 0){
                    var myProgramsTabSection = $("#myPrograms ."+sectionId);
                    if(myProgramsTabSection.length != 0){
                        swal({
                            title:"Already part of program",
                            type:"info",
                            timer:3000
                        });
                    }
                }
                else{
                    openTransactionPopup(sectionId,packageDays,popup);
                }
                hideOverlay();
                hideLoader();
            });
        }
        // hideLoader();
    };
    var showOverlay = function(){
        $("#overlayFull").removeClass("nonner");
    }
    var hideOverlay = function() {
        $("#overlayFull").addClass("nonner");
    }
    var getParameterByName = function(name, url){
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
      }

    var subscription = function(){
        $(this).addClass("added");
        $('.added').next().html($(this).find(':selected').data('price'));
        $(this).removeClass('added');
        // $('#price').html($(this).find(':selected').data('price'));
    }
    var getMySections = function(){
        var holder = parDiv.find(".myEnrolledPrograms .categoryProgramsContainer");
                var params = {
            myPrograms:true,
            start:0
        };
        var url = "/Institute/getMySections";
        categorySections.extGet(url,holder,params,function(){
            if(myProgramsList && myProgramsList.list && myProgramsList.list.length>0){
                var list = myProgramsList.list;
                for(var index = 0;index<list.length;index++){
                    var sec = myProgramsList.list[index];
                    mySectionsList[sec.sectionInfo.id] = sec;
                }
            }
        });
    };
    var openEnrolledProgPopup = function(){
        var $this = $(this);
                var sectionId = $(this).data("sectionId");
        var data = mySectionsList[sectionId];
        if(data){
            var progId = data.programInfo.id;
            var centerId = data.centerInfo.id;
            var sectionId = data.sectionInfo.id;
            swal({
                customClass:"openEnroll",
                html: $("#myProgramInfoPopup").html(),
                showConfirmButton:false
            }).catch(swal.noop);
            popup = $(".openEnroll");
            // popup.html($("#myProgramInfoPopup").html());
            var itemName = $this.find(".sectionTitle").text();
            popup.find(".catProgramName").text(itemName);
            var desc = data.sectionInfo.desc;
            var myProgPaymentInfoBtn = popup.find(".myProgPaymentInfo");
            if(desc){
                popup.find(".catProgramDesc").html("<pre class='preDesc'>"+desc+"</pre>");
            }else{
                popup.find(".catProgramDesc").text("Description not available").addClass("italics");
            }
            if(data.sectionInfo.revenueModel != "FREE"){
                popup.find(".catProgramPrice").text($this.find(".showUserPaymentInfo").data("price"));
            }else{
                popup.find(".catProgramPrice")
                    .text($this.find(".showUserPaymentInfo").text())
                    .addClass("freeProgram");
                myProgPaymentInfoBtn.addClass("nonner");
            }
            popup.find(".visitProgramLibrary").on(CLICK,function(){
                institute.openLibrary();
                swal.close();
            });
            myProgPaymentInfoBtn.on(CLICK,function(){
                setTimeout(function(){
                    showUserPaymentInfo(sectionId,itemName);
                },500);
            });
        }else{
            showError(i18nJS("COMMON_ERROR_MESSAGE"));
        }
        };
    var getSections = function(){
        try{
                        if(categorySections){
                                getMySections();
                categorySections.get(parDiv);
                        }
                }catch(err){
                        console.error("Failed to load category sections - "+err);
                }
    };
    var tabChanged = function(){
        var $this = $(this);
        var type= $this.data("type");
        switch(type){
            case "CATEGORY" :
                break;
            case "MY_PROGRAMS" :
                break;
        }
        $this.addClass("active").siblings().removeClass("active");
    };
    var postPayment = function(info){
        if(info){
            var params = makePayments.formatSKU(info.item_sku);
            params.transactionId = info.transactionId;
            if(info.transactionStatus == "SUCCESS" || info.transactionStatus.toUpperCase() == "CREDIT"){
                        $.post("/Security/addMemberToSection",params,function(data){
                                if(data){
                                        if(data.errorCode){
                                                var error = "Error while joining : "+data.errorMessage;
                                                swal({
                                                    html:error,
                                                    type:"error"
                                                });
                                        }else if(data.result && data.result.done){
                                            swal({
                                                html:"You have successfully joined the program",
                                                type:"success"
                                            });
                                            getMySections();
                                        }
                    }
                });
            }else{
                if(info.transactionStatus == "CANCELLED"){
                    swal({
                        type:"error",
                        html:"Payment was cancelled."
                    });
                }else{
                    swal({
                        type:"error",
                        html:"Payment failed to process."
                    });
                }
            }
            replaceInstHistory(orgId,"programs");
        }
    };
    function replaceInstHistory(orgId,append){
        append = append?"/"+append:"";
        var pushUrl = append;
        pushHistory(null , null,pushUrl,true);
        return pushUrl;
    }

    function pushHistory(state, title, pathWithSearhParams, doReplace) {
        var returnLocation = history.location || document.location;
        var currPath = returnLocation.pathname + returnLocation.search;
        state = state || {};
        state["prevUrl"] = currPath;
        if (pathWithSearhParams !== currPath) {
            if (doReplace) {
                history.replaceState(state, title, pathWithSearhParams);
            } else {
                history.pushState(state, title, pathWithSearhParams);
            }
        }
        history.lastPathName = returnLocation.pathname;
    }

    var showUserPaymentInfoClicked =  function(e){
        var $this = $(this);
        var holder = $this.closest(".siteProgramHolder");
        var sectionId = holder.data("sectionId");
        var itemName = holder.find(".siteProgramName .sectionTitle").text();
        showUserPaymentInfo(sectionId,itemName);
    }
    var showUserPaymentInfo = function(sectionId,itemName){
        var params = {
            sectionId : sectionId,
            orgId : orgId,
            itemName : itemName
        };
        $.get("/Institute/getSectionPayInfoPopup",params,function(data){
            swal({
                html:data,
                showConfirmButton:false,
                customClass:"getSection"
            });
            $(".getSection").updateClientTime();
        })
    };

    function openTransactionPopup(targetId,targetPackageDays,targetPop){
        var popup;
        var sectionId;
        var packageDays;
        popup = targetPop;
        sectionId = targetId;
        packageDays = targetPackageDays;
        var centerId = popup.find("#centerId").val();
        var programId = popup.find("#programId").val();
        var itemName = popup.find(".catProgramName").val();
        var packageOrgId = popup.find(".packageSelect").data("packageOrgId");
        var params = {
            orgId : orgId,
            "sectionIds[0]" : sectionId,
            centerId : centerId,
            programId : programId,
            targetOrgMemberId : targetOrgMemberId,
            targetProfile : targetProfile,
            packageDays : packageDays,
            itemName : itemName
        }
        if(makePayments){
            var callBackUrl = "/postPaymentAddSection";
            makePayments.init("SECTION",sectionId,itemName,packageOrgId,packageDays,callBackUrl,params,function(state){
                if(!state){
                }
            });
        };
    }

    var buyNow = function(){
        $this = $(this);
        $data = $this.data();
        sectionId = $data.sectionId;
        if (sectionId == null || sectionId=="" || sectionId ==undefined) {
            popup = $(".singleProgramPopup");
            sectionId = popup.find("#sectionId").val();
        }
        else{
            popup = $("."+sectionId);
        }
        packageDays = popup.find(".packageSelect").val();
        openTransactionPopup(sectionId,packageDays,popup);

    };
    var joinNow = function(){
        $this = $(this);
        $data = $this.data();
        var popup;
        var sectionId = $data.sectionId;
        console.log($data.sectionId);
        if (sectionId == null || sectionId=="" || sectionId ==undefined) {
            popup = $(".singleProgramPopup");
            sectionId = popup.find("#sectionId").val();
        }
        else{
            popup = $("."+sectionId);
        }
        var centerId = popup.find("#centerId").val();
        var programId = popup.find("#programId").val();
        var params = {
            orgId : orgId,
            "sectionIds[0]" : sectionId,
            centerId : centerId,
            programId : programId,
            targetOrgMemberId : targetOrgMemberId,
            targetProfile : targetProfile
        };
                $.post("/Security/addMemberToSection",params,function(data){
                        if(data){
                                if(data.errorCode){
                                        swal({
                                            title:"Error while joining the program",
                                            type:"error"
                                        });
                                }else if(data.result && data.result.done){
                                        swal({
                                            title:"You have successfully joined the program",
                                            type:"success"
                                        });
                                    getMySections();
                                }else{
                                        swal({
                                            title:"Something went wrong",
                                            type:"warning"
                                        });
                                }
                        }
        });
           };
};