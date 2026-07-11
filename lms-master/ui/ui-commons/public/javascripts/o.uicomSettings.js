var userSettings = new function(){
	var orgId;
	var userJson;
	this.init = function(popup,orgId){
		var $popup = $(popup);
		var $popup = $popup.find(".instSettingsPopup")
			.on("click",".instStngsChngPassBtn",showChngPwd)
			.on("click",".instStngsChngPassDoneBtn",submitChngPwd)
			.on("click",".instStngsRegEmail",showAddEmail)
			.on("click",".instStngsRegEmailBtn",submitNewEmail)
			.on("click",".instStngsRegUpdateBtn",submitNewEmailAndPassword)
			.on("click",".resendEmailVerifyLink",resendEmailLink)
			.on("click",".instStngsUseEmailBtn",useEmailBtn)
			.on("click",".subscribeBtn",subscribeBtn)
			.on("click",".unsubscribeBtn",unsubscribeBtn)
			.on("change",".emailUserName",checkUsernameExist)
			.on("click",".instStngsUseEmailBtn2",useEmailBtnPost);

		if($popup.hasClass("showOnlyEmailOpt")){
			$popup.find(".instStngsRegEmail").trigger("click");
		}
		userJson = cloneObject(window["usrJsonData"]);
		window["usrJsonData"] = null;
	};
	var subscribeBtn = function(){
		var holder = $(this).closest(".subscriptionEmail");
		vReq.get("/UIComUserSettings/subscribeEmail",{"mailCategory":"NOTIFICATION"},function(data){
			if(data && data.errorCode==""){
				holder.text(holder.data("textSubscribed"));
			}
		});
	};
	var unsubscribeBtn = function(){
		var holder = $(this).closest(".subscriptionEmail");
		vReq.get("/UIComUserSettings/unsubscribeEmail",{"mailCategory":"NOTIFICATION"},function(data){
			if(data && data.errorCode==""){
				holder.text(holder.data("textUnsubscribed"));
			}
		});
	};
	var useEmailBtn = function(){
		$(this).addClass("nonner")
			.closest(".regEmailVerified").find(".myStngsVerifyOldPwdHold").removeClass("nonner");
	};
	var useEmailBtnPost = function(){
		var $this = $(this);
		var email = $(this).closest(".regEmailVerified").find(".userEmailId").data("emailId");
		var password = $(this).closest(".myStngsVerifyOldPwdHold").find(".myStngsVerifyOldPwd").val();
		var pr = {
			"newUsername":email,
			password:password,
			loginType:$(this).data("loginType"),
			username:$(this).data("username"),
			orgId:orgId
		};
		var stgnPopup = $this.closest(".instSettingsPopup");
		$.post("/uicomusersettings/updateUsername",pr,function(data){
			if(data){
				if(data.errorCode===""){
					stgnPopup.find(".myStngsLoginId")
						.data("username",email).text(email).css({"color":"green"});
					stgnPopup.find(".settingsLoginMsg1").remove();
					stgnPopup.find(".settingsLoginMsg2").removeClass("nonner");
					$this.closest(".instStngsUseEmailHold").find(".instStngsUseEmailMsg").removeClass("nonner");
					$this.closest(".myStngsVerifyOldPwdHold").remove();
				}else if(data.errorCode==="CURRENT_WRONG_PASSWORD"){
					showError("Old Password did not match.");
				}else if(data.errorCode==="USER_ALREADY_EXISTS"){
					showError("The above Email-Id is already in use as LOGIN-ID by someone else.");
				}else{
					showError("Some Error Occurred, Please try again.");
				}
			}else{
				showError("Some Error Occurred, Please try again.");
			}
		});	
	};
	var showAddEmail = function(){
		var holder = $(this).closest(".instStngEmail");
		holder.find(".regEmailEachDiv").addClass("nonner");
		holder.find(".regEmailHolder").removeClass("nonner").find("input").focus();
	};
	var submitNewEmail = function(){
		var holder = $(this).closest(".instStngEmail");
		var email = holder.find(".myStngsChngEmail").val();
		var existingEmail = holder.data("emailId");
		if(validateEmail(email)){
			if(existingEmail == email){
				showError("You have chosen the Existing 'Email-Id' as your NEW 'Email-Id', please provide us with another 'Email-Id'");
				return;
			}
			var member = userJson;
			//member.dob = "2000-01-01";
			var pr = {
				dob:member.dob,
				gender:member.gender,
				lastName:member.lastName,
				firstName:member.firstName,
				email:email
			}
			$.post("/uicomUserSettings/updateUser",pr,function(data){
				if(!data || data.errorCode){
					showError("Add Email Failed");
				}else{
					$.post("/uicomUserSettings/updateUserEmailInOrgMember",pr,function(dat){
						if(!dat || dat.errorCode){
							showError(dat.errorMessage);
						}else{
							holder.find(".regEmailEachDiv").addClass("nonner");
							holder.find(".regEmailNotVerified").removeClass("nonner").find(".userEmailId").text(email);
						}
					});
				}
			});
		}else{
			showError("Enter Valid Email Address");
		}
	};

	var submitNewEmailAndPassword =function(){
		var holder = $(this).closest(".instStngEmail");
		var email = holder.find(".myStngsChngEmail").val();
		var password=holder.find("#newpassword").val();
		var confirmpassword=holder.find("#confirmpassword").val();
		if(!validateEmail(email)){
			showError("Invalid Email Id format");
			return;
		}
		if(password!=confirmpassword){
			showError("Password and confirm password do not match");
			return;
		}
		if(password==""){
			showError("Password cannot be empty");
			return;
		}
		var member=userJson;
		var params={
			firstName:member.firstName,
			gender:member.gender,
			dob:member.dob,
			password:password,
			email:email
		}
		$.post("/uicomUserSettings/updateUser",params,function(data){
			if(!data || data.errorCode){
				showError(data.errorMessage);
			}else{
				$.post("/uicomUserSettings/updateUserEmailInOrgMember",params,function(dat){
					if(!dat || dat.errorCode){
						showError(dat.errorMessage);
					}else{
						window.location.reload();
					}
				});
			}
		});
	};
	var resendEmailLink = function(){
		var $this = $(this);
		$.get("/UIComRegister/resendVerifyLink",function(data){
			if(data && data.errorCode==""){
				$this.addClass("nonner");
				$this.siblings(".emailVerifyLinkSent").removeClass("nonner");
			}
		});
	};

	var checkUsernameExist = function(){
      var $this = $(this);
      var val = $this.val();
      var resultDiv = $this.siblings(".signupErrorDiv");
      $.get("/UIComRegister/checkUsernameExist",{email:val},function(data){
        if(data && data.errorCode=="" && data.result.doesEmailExists){
          emailflag=1;
          $(".signupErrorDiv").removeClass("success");
          showMsg("Email already registered with us !!").addClass("nosuccess");
        }else{
          emailflag=0;
          $(".signupErrorDiv").removeClass("nosuccess");
          showMsg("Login-id available, go ahead!").addClass("success");
        }
      });
      function showMsg(text){
        return resultDiv.text(text).animate({height: "22px"}, 250);
      }
    };


	var showChngPwd = function(){
		var holder = $(this).closest(".instStngLogin").addClass("showChngPwd");
		showChngPwdMsg(holder);
	};
	var showChngPwdMsg = function(holder,msgCls){
		var msgHolder = holder.find(".chngPwdMsgs");
		msgHolder.find("div").addClass("nonner");
		if(msgCls){
			msgHolder.removeClass("nonner");
			msgHolder.find(msgCls).removeClass("nonner");	
		}else{
			msgHolder.addClass("nonner");
		}
	}
	var submitChngPwd = function(){
		var holder = $(this).closest(".instStngLogin");
		showChngPwdMsg(holder);
		var newPass = $.trim(holder.find(".myStngsChngNewPwd").val());
		var reNewPass = $.trim(holder.find(".myStngsChngReNewPwd").val());
		var currentPass = $.trim(holder.find(".myStngsChngOldPwd").val());
		if(currentPass.length<5 || newPass.length<5){
			showChngPwdMsg(holder,".pwdLengthError");
			return;
		}
		if(newPass === reNewPass){
		   var pr = {
			loginType:holder.find(".myStngsLoginId").data("loginType"),
			username:holder.find(".myStngsLoginId").data("username"),
			currentPass:currentPass,
			newPassword:newPass,
			orgId:orgId
		   };
		   $.post("/uicomUserSettings/changePassword",pr,function(data){
			if(data){
			   if(data.errorCode){
				switch(data.errorCode){
				case "CURRENT_WRONG_PASSWORD" : showChngPwdMsg(holder,".curPwdNotMatch");
					break;
				default : showChngPwdMsg(holder,".unknownError");
					break;
				}
			   }else{
		   		holder.removeClass("showChngPwd");
				holder.find(".myStngsChngPwd input").val("");
				showChngPwdMsg(holder,".pwdChngSuccess");
			   }
			}else{
				showChngPwdMsg(holder,".unknownError");
			}
		   });
		}else{
			showChngPwdMsg(holder,".pwdNotMatch");
		}
	};
};
