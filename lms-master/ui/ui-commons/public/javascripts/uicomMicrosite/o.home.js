var Microsite = new function(){
	var parDiv;
	var orgId;
	var uiSamples;
	var CLICK = "click.microsite";
	var CHANGE = "change.microsite";
	var MOUSEENTER = "mouseenter.microsite";
	var MOUSELEAVE = "mouseleave.microsite";
	var orgObj;
	var contactflag=0;
	var emailflag=0;
	var popupOpacityLevel = 0.7;
	var usernameAuthUrl = "/Security/authentify";
	this.init = function(){
		parDiv = $("#micrositePage");
		uiSamples = $("#siteUiSamples");
		orgId = parDiv.data("orgId");
		$(document).off(CLICK)
		parDiv.off(CLICK)
			.on(CLICK,".loginPopupLink",loginBtnClicked)
			.on(CLICK,"#orgContactUsLink",showContactUsPopup)
			.on(MOUSEENTER,".siteFeatureBlock",mouseEnterFeature)
			.on(MOUSELEAVE,".siteFeatureBlock",mouseLeaveFeature)
		$(document).on(CLICK,".forgotPass",showForgotPassPopup)
			.on(CLICK,".loginPopupBtn",loginBtnClicked)
			.on(CLICK,".showPasswordCheckbox",showPasswordCheckbox)
			.on(CLICK,".signupPopupButton,.joinPopupButton",showSignupPopup)
			
		orgObj = orgData;orgData = null;
		this.slides.start();
		checkServerValues();
		try{
			if(categorySections){
				categorySections.get(parDiv);
			}
		}catch(err){
			console.error("Failed to load category sections - "+err);
		}
        loadSignupOrLoginPopup();
	};
    var loadSignupOrLoginPopup = function(){
        var popup = $(".popup").val();
        if(popup != ""){
            if(popup === "login"){
                $(".loginPopupLink").trigger('click');
            }
            else if(popup == "signup"){
                $(".signupPopupButton").trigger('click');
            }
            // $(".vpopupWrapper").css("background","lightgrey");
        }
    }
	var mouseEnterFeature = function(){
		var $this = $(this);
		var title = $this.find(".siteFeatureName");
		var desc = $this.find(".siteFeatureDesc");
		title.animate({
			top:"-40px",
			opacity:0.1
		},250);
		desc.animate({
			top:"-40px",
			opacity:1
		},250);
	};
	var mouseLeaveFeature = function(){
		var $this = $(this);
		var title = $this.find(".siteFeatureName");
		var desc = $this.find(".siteFeatureDesc");
		title.animate({
			top:"0px",
			opacity:1
		},250);
		desc.animate({
			top:"0px",
			opacity:0.1
		},250);
	};
	var showForgotPassPopup = function(){
		if(!isInternalAuth()){
			showMessage("Contact your institute admin to change password!");
			return;
		}
		var $this = $(this);
		var enteredLoginId = $this.closest(".loginPopup").find("#userLoginId").val();
		enteredLoginId = enteredLoginId ? enteredLoginId : "";
		var popup = showVPopup(popupOpacityLevel);
		popup.html(uiSamples.find("#forgotPassPopup").html());
		var loginIdElem = popup.find("#userLoginId").val(enteredLoginId).focus();
		popup.find("#clickToGeneratePassword").on("click",function(){
			var loginId = $.trim(loginIdElem.val());
			var url = "memberForgotPass";
			var params = {
				orgId : orgId
			};
			if(validateEmail(loginId)){
				url = "userNameForgotPass";
				params.username = loginId;
			}else{
				params.memberId = loginId;
			}
			var errorDiv = popup.find(".loginErrorMsg").html("").addClass("nonner");	
			sendForgotPassReq(url,params,errorDiv,popup);
		});	
	};
   	function sendForgotPassReq(url,params,errorDiv,popup){
		var errorMsg;
		function showError(errorMesssage){
			errorDiv.html(errorMesssage).removeClass("nonner");	
		};
		if(!url){
			showError("ERROR : Server Error");
			return;
		}
		$.get("/UIComRegister/"+url, params, function(data){
  			if(data.errorCode != ""){
  			   switch(data.errorCode){
				case "INVALID_ID" : 
				case "USER_NOT_FOUND":errorMsg = "Error : User Not Found";
					break;
				case "USER_NO_VERIFIED_EMAIL": errorMsg = "<div>You have not registered your Email ID,</div>";
					errorMsg += "<div> contact your Institute's Admin to get new password </div>";
					break;
				default:errorMsg = "Error : Server failed";
					break;
			   }
			   showError(errorMsg);
  			}
  			else{
				closeVPopup();
    				showVMsgBox("A Password reset link has been sent to your E-mail ID",null,"SUCCESS");
  			}
		}).error(function(){
			errorMsg = "<div>You have not registered your Email ID,</div>";
			errorMsg += "<div> contact your Institute's Admin to get new password </div>";
			showError(errorMsg);
		});
		return errorMsg;
   	}; 
	var checkServerValues = function(){
		if(loginError){
			var popup = showLoginPopup(lastSectionId);
			var lastLoginId = lastUserName?lastUserName:(lastMemberId?lastMemberId:"");
			popup.find("#userLoginId").val(lastLoginId);
		}
		if(emailVerified == "done"){
			var popup = showVPopup(popupOpacityLevel);
	                popup.html($("#verifyEmailDone").html());
		};
		if(signupError){
			var error = "Error while signing-up : " + signupError;
    			showVMsgBox(error,null,"ERROR");
		}
	};
	var checkUsernameExist = function(){
		var $this = $(this);
		var val = $this.val();
		var resultDiv = $this.siblings(".signupErrorDiv");
        var isValidEmail = validateEmail(val);
        if(isValidEmail){
            $.get("/UIComRegister/checkUsernameExist",{email:val},function(data){
                if(data && data.errorCode=="" && data.result.doesEmailExists){
                    emailflag=1;
                    showMsg("Email already registered with us, please choose new!").removeClass("success");
                }else{
                    emailflag=0;
                    showMsg("Login-id available, go ahead!").addClass("success");
                }
            });
        }
        else{
            emailflag = 1;
            showMsg("Invalid Email").removeClass("success").addClass("redColor");
            return false;
        }
		function showMsg(text){
			return resultDiv.text(text).animate({height: "22px"}, 250);
		}
	};
    var validateMobileNumber = function(contactNumber,countrycode){
        var pattern = new RegExp("^[6-9][0-9]{9}$");
        var resp = true;
        if(countrycode == "91"){
            if(contactNumber.length != 10 || !pattern.test(contactNumber)){
                resp = false;
            }
        }
        else{
            if (!$("#contactNumber").intlTelInput("isValidNumber")) {
                resp = false;
            }
        }
        return resp;
    }
	var checkContactNumberExist=function(){
        var $this = $(this);
        var contact = $this.val();
        var resultDiv = $("#contactNumbererrorDiv");
        var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
        var isValidNumber = validateMobileNumber(contact,countrycode);
        if(isValidNumber){
            $.when($.ajax({url:"/UIComRegister/checkContactNumberExist",type:"GET",async:false,data:{contactNumber:contact,countryCode:countrycode}})).done(function(data){
                // error case -> show error popup
                // true -> contact number available
                //false -> not available
                 if(data && data.errorCode=="" && !data.result.doesContactNumberExists){
                    contactflag=1;
                    $(".otp-verification-section").addClass("nonner");
                    showMsg("ContactNumber already registered with us !!").removeClass("success");
                    return false;
                }else{
                    contactflag=0;
                    showMsg("ContactNumber available, go ahead!").addClass("success");
                    $(".otp-verification-section").removeClass("nonner").addClass("redColor");
                }
              });
        }
        else{
            contactflag = 1;
            $(".otp-verification-section").addClass("nonner");
            showMsg("Invalid contactNumber").removeClass("success").addClass("redColor");
            return false;
        }
        function showMsg(text){
            return resultDiv.text(text).animate({height: "22px",position:"relative"}, 250);
        }

    };

    function getOTPParams(){
        var countryCode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
        var contactNumber = $("#contactNumber").val();
        var orgId = $("#orgId").val();
        var fullName = $(".firstName").val();
        var params = {
            countryCode:countryCode,
            contactNumber:contactNumber,
            fullName:fullName,
            orgId:orgId
        }
        return params;
    }
     this.isValidOTP = false;
     this.isOTPSent = false;
     this.sendOTP = function(){
        var params = getOTPParams();
        params.respJSON = "true";
        $.post("/UIComRegister/sendOTP",params,function(data){
            if(data && data.errorCode == ""){
                $(".otp-verification-section").addClass("nonner");
                showMessage("An OTP has been sent to your registered mobile number");
                $(".otp-form-field-row").find(".resend-otp-section").removeClass("nonner");
                Microsite.isOTPSent = true;
            }
            else{
                showError("Something went wrong...Please try again");
                return false;
            }
        });
    }

	var isInternalAuth = function(){
		var retVal = true;
		if(orgObj && orgObj.authType){
			if(orgObj.authType == "EXT_AUTH_ORG"){
				retVal = false;
			}
		}
		return retVal;	
	};
	var showSignupPopup = function(){
		var popup = showVPopup(popupOpacityLevel,true);
		popup.html(uiSamples.find("#signupPopup").html());
		var sectionId = "";
		try{
			sectionId = $(this).data("sectionId");
		}catch(err){}
		setTimeout(function(){
			popup.load("/UIComRegister/signupForm",{orgId:orgId},function(){
				signupFormLoaded(popup.find(".signupPopup"),sectionId);
			});
		},500);
	};
	var showHideSignupError = function(popup,msg,field,e){
		var errorDiv = popup.find(".loginErrorMsg");
		popup.find(".showError").removeClass("showError");
		if(msg){
			errorDiv.html(msg).removeClass("nonner");
			if(field){ 
				field.focus();
				field.addClass("showError");
			}
			window.scrollTo(0,0);
			if(e) e.preventDefault();
		}else{
			errorDiv.html("").addClass("nonner");
		}
	};
	var signupFormLoaded = function(popup,sectionId){
		$("#contactNumber").intlTelInput({
        initialCountry:"in",
        formatOnDisplay:false,
        separateDialCode: false
      });
		var firstElem = popup.find(".loginInputField:first").focus();
		var signupForm = popup.find("#memberSignupForm");
		popup.find("[name='sectionId']").val(sectionId);
		popup.find(".loginPopupBtn").data("sectionId",sectionId);
		signupForm.submit(function(e){
			if(!checkForCookieEnabled()){
				e.preventDefault();
				return false;
			}
			var $this = $(this);
			showHideSignupError(popup);
			var pwdField = $this.find("[name='password']");
			var pwd = $this.find("[name='password']").val();
			var confirm_pwd = $this.find("[name='confirmpassword']").val();
			if(pwd.length<5){
				showHideSignupError(popup,"Password should be of minimum 5 characters!",pwdField,e);
				return false;
			}
			if(confirm_pwd != pwd){
				showHideSignupError(popup,"Password and confirm password should match!",pwdField,e);
				return false;
			}
			var hasErrorField = signupForm.find(".loginInputField.hasError").get(0);
			if(hasErrorField){
				showHideSignupError(popup,"Field has error, please fix before proceeding!",$(hasErrorField),e);
				return false;
			}
            var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
            document.getElementById("countryCode").value = countrycode;
            var contactNumber = document.getElementById("contactNumber").value;
            var pattern=new RegExp("^[6-9][0-9]{9}$");
            if (countrycode == "91") {
                if (contactNumber.length!=10 || isNaN(contactNumber)) {
                    showHideSignupError(popup,"Contact number must be exactly 10 digits");
                    return false;
                }
                else if(!pattern.test(contactNumber)){
                    showHideSignupError(popup,"Invalid Contact number");
                    return false;
                }
      } else {
          if ($.trim($("#contactNumber").val())) {
              if ($("#contactNumber").intlTelInput("isValidNumber")){
              }
              else {
                  showHideSignupError(popup,"Invalid Contact number");
                  return false;
              }
          }
      }
      var contactfield=document.getElementById("contactNumber");
      var referralCode=$("#referral").val();
      var validreferral=true;
      if(referralCode !="" && referralCode!=undefined)
      {
        $.when($.ajax({url:"/UIComRegister/isValidReferralCode",type:"POST",async: false,data:{referralCode:referralCode}})).done(function(data){
            if(data && data.errorCode=="" && !data.result.doesReferralCodeExists){
                validreferral=false;
            }
        });
    }
      var emailfield=document.getElementById("email");
      if(contactflag===1){
        contactfield.style.borderColor="red";
        return false;
    }else{
        contactfield.style.borderColor="#fff";
    }
    if(validreferral===false){
        showHideSignupError(popup,"Invalid Referral code");
        return false;
    }
      if(emailflag===1){
        emailfield.style.borderColor="red";
        return false;
    }else{
        emailfield.style.borderColor="#fff";
      }
    if($(".otp-verification-section").length > 0){
        var OTPField = $(".OTP");
        if(!Microsite.isOTPSent){
            showHideSignupError(popup,"Please click on send OTP",OTPField,e);
            return false;
        }
        var params = getOTPParams();
        params.userOTP = $(".OTP").val();
        $.when($.ajax({url:"/UIComRegister/validateOTP",type:"POST",async:false,data:params})).done(function(data){
            if(data && data.errorCode == ""){
                Microsite.isValidOTP = true;
            }
            else{
                showHideSignupError(popup,""+data.errorMessage,OTPField,e);
            }
        });
        if(!Microsite.isValidOTP){
            return false;
        }
    }
    $("#clickToSignUp").addClass("disable-button").val("Signing you up...");
		});
		var dobField = popup.find("#signUpDobField");
		var calanderHolder = popup.find("#dobCalenderHolder");
		popup.off(CLICK).off(CHANGE);
		popup.on(CHANGE,".emailUserName",checkUsernameExist);
		popup.on(CHANGE,".contactNumber",checkContactNumberExist);
        popup.on(CLICK,".sendOTP",Microsite.sendOTP);
		popup.on(CLICK,"#dobCalender,#signUpDobField",function(e){
			if(calanderHolder.data("visible")){
				calanderHolder.addClass("nonner");
				calanderHolder.data("visible",false);
			}else{
				calanderHolder.removeClass("nonner");
				calanderHolder.data("visible",true);
			}
			e.preventDefault();
		});
		$("#dobCalenderHolder").datepicker({
			minDate: "-100Y",
			maxDate: 0,
			changeMonth: true,
			dateFormat: "yy-mm-dd",
      			changeYear: true
		}).on("change",function(){
			calanderHolder.addClass("nonner");
			calanderHolder.data("visible",false);
			dobField.val($(this).val());	
		});
	};
	var showContactUsPopup = function(){
		var popup = showVPopup(popupOpacityLevel);
		popup.html(uiSamples.find("#contactUsPopup").html());
	};
	var loginBtnClicked = function(){
		var sectionId = $(this).data("sectionId")
		var popup = showLoginPopup(sectionId);
		popup.find(".loginErrorMsg").remove();
	};
	//show password
   	var showPasswordCheckbox=function(){
		var par = $(this).closest(".loginPopup");
        	if(par.find("#userPassword").data("shown") === "password"){
             		var val = par.find("#userPassword").addClass("nonner").data("shown","text").val();
             		par.find("#userPasswordText").removeClass("nonner").val(val);
        	}else{
             		var val = par.find("#userPasswordText").addClass("nonner").val();
             		par.find("#userPassword").removeClass("nonner").data("shown","password").val(val);
        	}       
   	};
	var showLoginPopup = function(sectionId){
		var popup = showVPopup(popupOpacityLevel);
		popup.html(uiSamples.find("#loginPopup").html());
		var loginIdElem = popup.find("#userLoginId").focus();
		var loginForm = popup.find("#memberLoginForm");
		try{
			sectionId = sectionId ? sectionId : "";
			popup.find("[name='sectionId']").val(sectionId);
		}catch(err){}
		loginForm.submit(function(e){
			if(!checkForCookieEnabled()){
				return false;
			}
			var loginId = loginIdElem.val();
			if(validateEmail(loginId) && isInternalAuth()){
				loginForm.attr("action",usernameAuthUrl);
				loginIdElem.attr("name","username");
			}else{
				if(isInternalAuth() && loginId){
					loginIdElem.val(loginId.toUpperCase());
				}
			};
        		var userPasswordEl= loginForm.find('#userPassword');
        		var userPasswordTextEl= loginForm.find('#userPasswordText');
        		if(userPasswordEl.data("shown")==="text"){
              			pass = userPasswordTextEl.val();
              			userPasswordEl.val(pass);
        		}
		});
		return popup;
	};
  	function checkForCookieEnabled(){
		if(navigator.cookieEnabled){
			return true;
		}else{
			try{
				var msg = "<span class='redTextColor big16'>";
				msg += "Your browser's cookie functionality is turned off. Please turn it on.?";
				msg += "</span>";
			  	setTimeout(function(){
    					showVMsgBox(msg,null,"ERROR");
				},100);
			}catch(err){}
			return false;
		}
  	}; 
	this.slides = new function(){
		var slidesIntervalDur = 5000;
        	var noOfSlides = 3;
        	var curSlide = 0;
        	var slidesIntervalObj;
		var textHolder,textSlides,tabletHolder,tabletSlides,mobileHolder,mobileSlides;
		var navHolder;
		var textWidth=0,mobileWidth=0,tabletWidth=0;
		var mouseleaveTimeoutObj;
		this.start = function(){
			textHolder = parDiv.find(".siteSlideTextHolder");
			tabletHolder = parDiv.find("#slideTabletTable");
			mobileHolder = parDiv.find("#slideMobileTable");
	                textSlides = textHolder.find(".siteSlideTextTD");
			tabletSlides = tabletHolder.find(".siteSlideTabletTD");
			mobileSlides = mobileHolder.find(".siteSlideMobileTD");
        	        navHolder = parDiv.find(".slide-dots");
			calcWidth();
			
                        startInterval();
                        parDiv.on("mouseenter",".slideImgsHolder,.siteSlideTextFrame",pauseSlideShow)
                        	.on("mouseleave",".slideImgsHolder,.siteSlideTextFrame",function(){
					if(mouseleaveTimeoutObj) clearTimeout(mouseleaveTimeoutObj);
					mouseleaveTimeoutObj = setTimeout(function(){
						startInterval(true);
					},1000);
				})
                		.on(CLICK,'.slide-dot',dotClicked);
                	$(textSlides.get(curSlide)).fadeTo(200,1);
                	$(mobileSlides.get(curSlide)).fadeTo(200,1);
                	$(tabletSlides.get(curSlide)).fadeTo(200,1);
		};
		function calcWidth(){
			textWidth = $(textSlides.get(0)).width();
			mobileWidth = $(mobileSlides.get(0)).width();
			tabletWidth = $(tabletSlides.get(0)).width();
		};
		function pauseSlideShow(){
			if(mouseleaveTimeoutObj) clearTimeout(mouseleaveTimeoutObj);
                        if(slidesIntervalObj){ clearInterval(slidesIntervalObj); }
		};
		function startInterval(doSlide){
                        if(slidesIntervalObj){ clearInterval(slidesIntervalObj); }
			if(doSlide){
                        	slideTo(curSlide+1);
			}
                        slidesIntervalObj = setInterval(function(){
                                slideTo(curSlide+1);
                        },slidesIntervalDur);
                };
		function slideTo(toSlide){
			var textLeft,mobileLeft,tabletLeft;
                        /*var textLeft = textHolder.position()["left"];
                        var mobileLeft = mobileHolder.position()["left"];
                        var tabletLeft = tabletHolder.position()["left"];*/
                        
			var currText = $(textSlides.get(curSlide));
                        var currMobile = $(mobileSlides.get(curSlide));
                        var currTablet = $(tabletSlides.get(curSlide));
			curSlide = toSlide;
                        if(curSlide == noOfSlides){
                                textLeft = mobileLeft = tabletLeft = 0;curSlide = 0;
                        }else{
                        	textLeft = curSlide * textWidth * -1;
                        	mobileLeft = curSlide * mobileWidth * -1;
                        	tabletLeft = curSlide * tabletWidth * -1;
                        }
                        //$(loginSlides.get(curSlide)).fadeTo(0,1);
                        $(navHolder.find(".slide-dot").get(curSlide)).addClass("active").siblings().removeClass("active");       
                        textHolder.animate({"left":textLeft+"px"},400,function(){
                                /*currText.fadeTo(0,0);
                                $(textSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                        mobileHolder.animate({"left":mobileLeft+"px"},400,function(){
                                /*currMobile.fadeTo(0,0);
                                $(mobileSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                        tabletHolder.animate({"left":tabletLeft+"px"},400,function(){
                                /*currTablet.fadeTo(0,0);
                                $(tabletSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                }
                var dotClicked = function(e){
                        var index = $(".slide-dots .slide-dot").index($(this));
                        slideTo(index);
                        startInterval();
                };
	};	
};
$(function(){
	Microsite.init();
});
function showError(errorText,cbFn){
	showVMsgBox(errorText,"OK","ERROR",cbFn);
}
function showMessage(errorText,cbFn){
	showVMsgBox(errorText,"OK","MESSAGE",cbFn);
}
