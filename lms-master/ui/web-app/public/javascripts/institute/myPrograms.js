var myPrograms = new function(){
	var parDiv;
	var orgId,targetOrgMemberId,targetProfile;
	var CLICK = "click.myPrograms";
	var showLoadingDiv = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
	var mySectionsList = {};
	this.init = function(paymentInfo){
		parDiv = $("#myProgramsPage");
		parDiv.on(CLICK,".myEnrolledPrograms .siteProgramHolder",openEnrolledProgPopup)
			.on(CLICK,".showUserPaymentInfo",showUserPaymentInfoClicked);
		institute.init();
		getSections();
		var org = getMyOrgInfo();
		orgId = org.orgId;
		targetOrgMemberId = org.orgMemberId;
		targetProfile = org.userRole;
		$(document)
			.off(CLICK)
			.on(CLICK,".joinProgramForFree",joinNow)
			.on(CLICK,".signupPaidProgram",buyNow)

		parDiv.on(CLICK,".progTypeTab",tabChanged);
		postPayment(paymentInfo);
	};
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
			var popup = showVPopup(0.7);
			popup.html($("#myProgramInfoPopup").html());
			var itemName = $this.find(".sectionTitle").text();
			popup.find(".catProgramName").text(itemName);
			var desc = data.sectionInfo.desc;
			var myProgPaymentInfoBtn = popup.find(".myProgPaymentInfo");
			if(desc){
				popup.find(".catProgramDesc").text(desc);
			}else{
				popup.find(".catProgramDesc").text(i18nJS("TXT_NOT_AVAIALABLE")).addClass("italics");
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
				goToInstSectionLibrary($this,"","",progId,centerId,sectionId);
			});
			myProgPaymentInfoBtn.on(CLICK,function(){
				closeVPopup();
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
		putConsoleLogs(info);
		if(info){
			var popup = showVPopup(0.7);
			var params = makePayments.formatSKU(info.item_sku);
			putConsoleLogs(params);
            var prog_itemName = [];
			params.transactionId = info.transactionId;
			if(info.transactionStatus == "SUCCESS" || info.transactionStatus.toUpperCase() == "CREDIT"){
                		popup.html(showLoadingDiv);
                		$.post("/Security/addMemberToSection",params,function(data){
					popup.html($("#programPaymentConfirmPopup").html());
                    showItemName(params.itemName,prog_itemName,popup);
					popup.find(".paymentSuccess").removeClass("nonner");
                        		if(data){
                                		if(data.errorCode){
                                        		var error = "Error while joining : "+data.errorMessage;
                                        		showVMsgBox(error,null,"ERROR");
                                		}else if(data.result && data.result.done){
							popup.find(".partOfProgram").removeClass("nonner");
                                			getMySections();
                                		}
					}
				});
			}else{
				popup.html($("#programPaymentConfirmPopup").html());
				showItemName(params.itemName,prog_itemName,popup);
				if(info.transactionStatus == "CANCELLED"){
					popup.find(".paymentCancelled").removeClass("nonner");
				}else{
					popup.find(".paymentFailed").removeClass("nonner");
				}
			}
			replaceInstHistory(orgId,"myprograms");
		}
	};

     function showItemName(itemName,prog_itemName,popup){
        if(itemName.includes("+")){
            prog_itemName = itemName.split(",");
            for(i=0;i<prog_itemName.length;i++){
              prog_itemName[i] = prog_itemName[i].replace(/\+/g," ");
            }
            programName = prog_itemName[0];
            centerName = prog_itemName[1];
            centerName !== undefined ? prog_itemName[1]:"";
            sectionName = prog_itemName[2];
            sectionName !== undefined ? prog_itemName[2]:"";
            popup.find(".catProgramName").html("<div class='programName'>"+programName+"</div><div class='sectionCenterInfo big13 margTop5'>"+centerName+" | "+sectionName+"</div>");
        }
        else{
            popup.find(".catProgramName").text(itemName);
        }
    }

	var showUserPaymentInfoClicked =  function(e){
		var $this = $(this);
		var holder = $this.closest(".siteProgramHolder");
		var sectionId = holder.data("sectionId");
		var itemName = holder.find(".siteProgramName .sectionTitle").text();
		showUserPaymentInfo(sectionId,itemName);
	}
	var showUserPaymentInfo = function(sectionId,itemName){
		var popup = showVPopup(0.7);
                popup.html(showLoadingDiv);
		var params = {
			sectionId : sectionId,
			orgId : orgId,
			itemName : itemName
		};
		popup.load("/Institute/getSectionPayInfoPopup",params,function(data){
			popup.updateClientTime();
		})
		e.preventDefault();
		return false;
	};
	var buyNow = function(){
		var popup = $(".vpopupBody .singleProgramPopup");
                var sectionId = popup.find("#sectionId").val();
                var centerId = popup.find("#centerId").val();
                var programId = popup.find("#programId").val();
                var packageDays = popup.find(".packageSelect").val();
		var itemName = popup.find(".catProgramName").text();
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
			var callBackUrl = "/organization/"+orgId+"/postPaymentAddSection";
			showTopLoader();
			makePayments.init("SECTION",sectionId,itemName,packageOrgId,packageDays,callBackUrl,params,function(state){
				hideTopLoader();
				if(!state){
					showError(i18nJS("TRANSACTION_FAILED_TO_EXECUTE"));
				}
			});
		};
	};
	var joinNow = function(){
		var popup = $(".vpopupBody .singleProgramPopup");
                var sectionId = popup.find("#sectionId").val();
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
                popup.html(showLoadingDiv);
                $.post("/Security/addMemberToSection",params,function(data){
                        if(data){
				closeVPopup();
                                if(data.errorCode){
                                        var error = i18nJS("ERROR_WHILE_JOINING") +" : "+ data.errorMessage;
                                        showVMsgBox(error,null,"ERROR");
                                }else if(data.result && data.result.done){
                                        var message = i18nJS("SUCCESSFULLY_JOINED_PROGRAM");
                                        showVMsgBox(message,i18nJS("TXT_OK"),"SUCCESS");
                                	getMySections();
                                }else{
                                        var error = i18nJS("ERROR_WHILE_JOINING") + i18nJS("TRY_REFRESHING_THE_PAGE");
                                        showVMsgBox(error,null,"ERROR");
                                }
                        }
		});
           };
};
