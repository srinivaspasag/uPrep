var showErrors = function(errorTxt){
     $(".errorWrapperDiv").show();
	console.log($(".errorWrapperDiv"));
     var errorWrapper=$(".errorWrapper");
     errorWrapper.text(errorTxt);
}

var showErrorBox = function(err, cbFn) {
    showCmdsMsgBox(err, "OK", "ERROR", cbFn);
};
var showCmdsMsgBox = function(err, cancelText, typeOfBox, cbFn) {
    var popup = showVMsgBox(err, cancelText, typeOfBox, cbFn);
    return popup;
};

var hideError = function(){
     $(".serverErrorDiv").remove();
     $(".errorWrapperDiv").hide();
     var errorWrapper=$(".errorWrapper");
     errorWrapper.text("");
}
var showResponse = function(msg,doHide,className){
     if(msg){
     	var popup = showVPopup();
     	popup.html("<div class='loginVPopupError "+className+"'>"+msg+"</div>");
     }else if(doHide){
     	closeVPopup();
     }
}
var contactflag=0;
var emailflag=0;
//enterKey event reg
$(document).on('keyup', 'input,textarea', function(e) {
    if (e.which == 13 || e.keyCode == 13) {
        $(this).trigger("enterkey");
    }
});
$(function(){    
   function sendForgotPassReq(url,params){
	var errorMsg;
	if(!url){
		 return "ERROR : Server Error";
	}
	$.get("/UIComRegister/"+url, params, function(data){
  		if(data.errorCode != ""){
  			switch(data.errorCode){
				case "INVALID_ID" : 
				case "USER_NOT_FOUND":errorMsg = "Error : User Not Found";
					break;
				case "USER_NO_VERIFIED_EMAIL": errorMsg = "<div>You have not registered your Email ID,</div><div> contact your Institute's Admin to get new password </div>";
					break;
				default:errorMsg = "Error : Server failed";
					break;
			}
  		}
  		else{
    			errorMsg = "A Password reset link has been sent to your E-mail ID";
  		}
		showResponse(errorMsg,true);
	}).error(function(){
		errorMsg = "<div>You have not registered your Email ID,</div><div> contact your Institute's Admin to get new password </div>";
		showResponse(errorMsg,true,"redTextColor");
	});
	return errorMsg;
   } 
   $(document).on('click','#clickToSend',function(e){
	var holder = $(this).closest(".forgotPassHolder");
	var loginType = holder.data("loginType");
	var url = holder.data("url");
	var errorMsg = "";
        $('.errorWrapper').hide();
	if(!loginType || loginType == "EMAIL"){
      		var mail = holder.find('#forgotPassEmail').val();
      		if(!validateEmail(mail)){
			errorMsg = "Enter Valid EmailID";
		}else{
			errorMsg = sendForgotPassReq(url,{username: mail});
		}
      	}
      	else {
		var params = {};
		params.orgId = holder.find("#forgotPassOrgId").val();
		params.memberId = holder.find("#forgetPassMemberId").val();
		if(params.orgId && params.memberId){
			errorMsg = sendForgotPassReq(url,params);
		}else{
			errorMsg = "All fields are compulsory";
		}
	}
	showResponse(errorMsg,false,"redTextColor");
  }); 


});
function specialChar(s){
     var iChars = "!@#$%^&*()+=-[]\\\';,./{}|\":<>?";
     var spl=false;
for (var i = 0; i < s.length; i++) {
    if (iChars.indexOf(s.charAt(i))!= -1) {
            spl=true;
    }
  }
  return spl;
}
/*function filterText(input){
    var output;
    output = input.replace("'","&apos;","g");
    output = output.replace("\"","&quot;","g");
    output = output.replace("`","&acute;","g");
    output = output.replace(">","&gt;","g");
    output = output.replace("<","&lt;","g");
    output = output.replace("&","&amp;","g");
    output = output.replace(/\n/g,"<br>");
    return output;
}
function backFilterText(input){
    var output = input.replace("<br>",/\n/g);
    return output;
}*/


/*//for change password
$("#submitPassword").live('click',function(){
    if($("#newPassword").val()!=""&&$("#newPassword").val()==$("#confirmPassword").val()){
       $.post("/register/setPassword",{newPassword:$("#newPassword").val(),rePassword:$("#confirmPassword").val(),username:$("#emailPass").val(),verificationKey:$("#veriKey").val()},function(data){
            if(data.errorCode=="INVALID_VERIFICATION_KEY")$(".changePasswordHolder").html("It seems that you have taken a long time to reset your password.Request for resetting your password again <br><div id='forgotPassDiv'>"+forgotPassHTML+"</div>");
            else if(data.errorCode=="NOT_REGISTERED"||data.Code=="USER_NOT_FOUND")$(".changePasswordHolder").html("You are not a registered member of vedantu.com. Please click here to <a href='/register/index'>Sign up</a>");
            else if(data.errorCode!="")window.location="/errors/500.html";
            else $(".changePasswordHolder").html("Your password has been successfully reset.<br>Click here to <a href='/register/index'>login</a>")
        });
    }
});*/



function successPopup(message,timeOut,timeOutTime){
    addToggler("#successPopup",this,true);
    $("#successPopup").html("<div class='successPopupBody'><div class='justBold'>"+message+"</div><div class='successButtonsHolder margTop20'><input class='blueSubmitButton SUSmallBtn cancelPopup' type='submit' value='ok'/>\n\
                             </div></div>");

    if(timeOut)setTimeout('insideOutClick()',timeOutTime);
}
$(document).on('click',".cancelPopup",function(){
    insideOutClick();
});
$(document).on('click',"#resendEmailVerificationLink",function(){
	var userId = $(this).data("userId");
    var params = {"userId":userId,"callingUserId":userId};
    $.post("/uicomregister/resendVerifyLink",params,function(data){
        if(data.errorCode=="")$(".userMessage").html("A new verification link has been sent to your email id.<br> Follow the link to verify your account");
        else $(".userMessage").html("There is some error in resending the link.Please us at <a href='mailto:helpdesk@vedantu.com'>helpdesk@vedantu.com</a>");
     });
});
$(document).on("click",".unsubscribeEmail",function(){
  var reason = $('#unsubscribeReason').val();
  var userId = $('#userId').val();
  var mailCategory = $('#mailCategory').val();
  $('.serverErrorMsg,.errorWrapperDiv').hide();
  $('.errorWrapper span').hide();
  if(reason == ""){
    $('.errorWrapperDiv').show();
    $('.errorWrapper span').hide();
    $('.blankError').show();
    $('.commonMsg').show();
  }else{
	var params = {reason:reason,userId:userId,targetUserId:userId,mailCategory:mailCategory,external:true};
        $.post("/uicomregister/unsubscribeFromEmail",params,function(data){
            var finalMessage="";
            if(!data||!data.result.done||data.errorCode!=""){
		if( data.errorCode =="ALREADY_UNSUBSCRIBED") {
    			finalMessage="<div class='big18'>You are already unsubscribed.</div>"
		} 
		else {
    			finalMessage="<div class='redTextColor big18'>There was some problem in unsubscribing. Refresh the page and try again.</div>"
		} 
	    }else{
                finalMessage="<div class='greenTextColor big18'>You have been successfully unsubscribed for emails from us.</div>";
	    }
            $(".unsubscribeContent").html(finalMessage);
	});
  }
});
$(document).on("click",'.changeButton',function(){
  var pass = $('#newPassword').val();
  var vPass = $('#confirmPassword').val();
  $('.serverErrorMsg span,.errorWrapper span').hide();
  $('.serverErrorMsg,.errorWrapper').hide();
  if(pass =="" || vPass == ''){
    $('.errorWrapper').show();
    $('.errorWrapper span').hide();
    $('.blankError').show();
    $('.commonMsg').show();
  }
  else if(pass.length<5){
    $('.errorWrapper').show();
    $('.errorWrapper span').hide();
    $('.minCharError').show();
    $('.commonMsg').show();
  }else if(pass!=vPass){
    $('.errorWrapper').show();
    $('.errorWrapper span').hide();
    $('.blankError').hide();
    $('.commonMsg').show();
    $('.mismatchError').show();
  }
  else{
	var orgId = $("#organizationId").val();
	var params = {newPassword:pass,rePassword:vPass,username:$("#emailPass").val(),code:$("#veriKey").val(),userId:$("#passUserId").val()};
        $.post("/uicomregister/setPassword",params,function(data){
            if(data.errorCode != ""){
			$(".updatePasswordWrapper").find(".serverErrorMsg").show().find("."+data.errorCode).show();
		}
            else{
			$(".updatePasswordWrapper").find(".passwordContent")
				.html("<div class='center' style='font-size:16px;'>Password reset was successful, redirecting to login ...</div>");
			setTimeout(function(){
				if(orgId){
					window.location.href = "/login/organization/"+orgId;
				}else{
					window.location.href = "/login";
				}
			},5000);
		}
        });
   }
});

/* FETCH ORG BY NAME */
$(function(){
	var orgLogin = new function(){
		var xhr;
		var lastText = "";
		var suggHolder;
		this.init = function(){
			$(".signInWrapper").on("click","#clickToMemberLogin",login);
			$(document)
				.on("keyup",".suggOrgName",keyUp)
				.on("keydown",".suggOrgName",keyDown)
				.on("mouseleave",".regInstSugg",mouseLeave)
				.on("mouseenter",".regInstSugg",mouseEnter)
				.on("click",".regInstSugg",clicked);
		};
		var login = function(){
			$("#memberLoginForm").submit();	
		};
		var clicked = function(){
			var $this = $(this);
			selected($this);
		};
		var selected = function($this){
			var holder = $this.closest(".regInputBoxHolder");
			var orgId = $this.data("orgId");
			if(orgId){
				holder.find(".suggOrgId").val(orgId);
				holder.find(".suggOrgName").val($.trim($this.text()));
				close(holder);
			}
			hideOrgToolTip();
			if(isInternalAuth(orgId)){
				$("#loginMemberId,#forgetPassMemberId").css({"text-transform":"uppercase"});
			}else{
				$("#loginMemberId,#forgetPassMemberId").css({"text-transform":"none"});
			}
		};
		var close = function(holder){
			$(holder).find(".regInstSuggHolder").addClass("nonner").html("");
			$(document).off("click",closeAll);
			hideOrgToolTip();
		};
		var closeAll = function(holder){
			$(".regInstSuggHolder").addClass("nonner").html("");
			$(document).off("click",closeAll);
			hideOrgToolTip();
		};
		var preventKeys = [38,40,13];
		var keyUp = function(e){
			if(preventKeys.indexOf(e.which)>=0){ return; }
			var holder = $(this).closest(".regInputBoxHolder");
			var val = $.trim($(this).val());
			if(val.length>0){
				fetchOrg(val,holder);	
			}else{
				close(holder);
			}
		};
		var keyDown = function(e){
			var key = e.which;
			switch(key){
				case 38:navigateUpDown("PREV",e);
                                	return false;
                        	case 40:navigateUpDown("NEXT",e);
                                	return false;
                        	case 13:
					var cur = $(this).closest(".regInputBoxHolder").find(".hovered");
					selected(cur);
		    			if(e){ e.preventDefault();}
                                	return false;
			}
		};
		var navigateUpDown = function(DIR,e){
		    if(e) e.preventDefault();
		    suggHolder = $(".regInstSuggHolder");
                    var cur = suggHolder.find(".hovered").removeClass("hovered");
		    var item = cur;
                    switch(DIR){
                        case "NEXT":if(cur.next(".regInstSugg").get(0)){
                                        item = cur.next().addClass("hovered");
                                    }else{
                                        item = suggHolder.find(".regInstSugg:first").addClass("hovered");
                                    }
                                break;
                        case "PREV":if(cur.prev(".regInstSugg").get(0)){
                                        item = cur.prev().addClass("hovered");
                                    }else{
                                        item = suggHolder.find(".regInstSugg:last").addClass("hovered");
                                    }
                                break;
               	    };
		    var orgId = $(item).data("orgId");
		    showOrgToolTip(orgId);
        	};
		var mouseLeave = function(){
			hideOrgToolTip();
		};
		var mouseEnter = function(){
			var $this = $(this);
          		$this.addClass("hovered").siblings().removeClass("hovered");
			var orgId = $this.data("orgId");
			showOrgToolTip(orgId);
        	};
		var showOrgToolTip = function(orgId){
			var holder = $(".orgInfoToolTip");
			if(orgId && orgSuggListById[orgId]){
				var org = orgSuggListById[orgId];
				holder.find(".orgInfoPic").attr("src",org.orgThumbnail);
				holder.find(".orgInfoSmallName").text(org.name);
				holder.find(".orgInfoFullName").text(org.fullName);
				holder.removeClass("hider");
				if(app_type == "LEARN"){
					var height = holder.height();
					holder.css("top",(height*-1)+"px");
				}
			}
		};
		var hideOrgToolTip = function(){
			$(".orgInfoToolTip").addClass("hider");
		};
		var fetchOrg = function(text,holder){
			if(text === lastText) return;
			if(xhr){xhr.abort();}
			hideOrgToolTip();
			var params = {"query":text,"start":0,"size":5};
			xhr = $.get("/uicomregister/getOrgsList",params,function(data){
				$(holder).find(".regInstSuggHolder").removeClass("nonner").html(data);
				$(document).on("click",closeAll);
				lastText = text;
			});
		}
		var isInternalAuth = this.isInternalAuth = function(orgId){
			var retVal = false;
			var orgIdSelected = orgId || $("#loginOrgId").val();
			if(orgIdSelected && orgSuggListById[orgIdSelected]){
				var org = orgSuggListById[orgIdSelected];
				if(org.authType != "EXT_AUTH_ORG"){
					retVal = true;
				}
			}
			return retVal;	
		};
		this.init();
	};
	defineFixProp(window,"orgLogin",orgLogin);	
});

var signupProcess = new function(){
    this.init=function(){
        var url=window.location.href;
        console.log(url);
        if (url.indexOf("referralcode")>-1)
        {
            var referralcode=getURLParameter("referralcode");
            if($("#referral").length){
              document.getElementById("referral").value=referralcode;
              document.getElementById("referral").readonly=true;
            }
        }else{
          if($("#referral").length){
            document.getElementById("referral").value = "";
            document.getElementById("referral").disabled=false;
          }
        }
        $("#contactNumber").intlTelInput({
          initialCountry:"in",
          formatOnDisplay:false,
          separateDialCode: false
        });
        // var $this = $(this);
        var signUpForm = $('#memberSignupForm');
        signUpForm.off("click").off("change");
		    signUpForm.on("change",".emailUserName",checkUsernameExist);
        signUpForm.on("change",".contactNumber",checkContactNumberExist);
    }
    this.validate = function() {
      var valid=true;
      var password = $("#password").val();
      var confirmPassword = $("#confirmpassword").val();
      var url = window.location.href;
      if (url.indexOf("type") > -1) {
          var type = getURLParameter("type");
          document.getElementById('progType').value=type;
      } else {
          // type = "All";
          type = $("input[name=programType]:checked").val();
          document.getElementById('progType').value=type;
      }
      if (password != confirmPassword) {
          $("#match").text("Password and confirm password should match!");
          valid=false;
      } else {
          $("#match").text("");
      }
      if (password.length < 5) {
          $("#passwordlength").text("Password must be min 5 characters");
          valid=false;
      } else {
          $("#passwordlength").text("");
      }
      var contactNumber = $("#contactNumber").val();
      var contactfield=document.getElementById("contactNumber");
      var emailfield=document.getElementById("email");
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      document.getElementById("countryCode").value = countrycode;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
        if (contactNumber.length!=10) {
          $("#numberlength").text("Contact number must be exactly 10 digits");
          valid=false;
        }
        else if(!pattern.test(contactNumber)){
          $("#numberlength").text("Invalid Number");
          valid=false;
        }
      }else {
        if ($.trim($("#contactNumber").val())) {
          if ($("#contactNumber").intlTelInput("isValidNumber")) {
          }else {
            $("#numberlength").text("Invalid Number");
            valid=false;
          }
        }
      };
      if(contactflag===1)
      {
        contactfield.style.borderColor="red";
        valid=false;
      }else{
        contactfield.style.borderColor="#fff";
      }
      if(emailflag===1)
      {
        emailfield.style.borderColor="red";
        valid=false;
      }else{
        emailfield.style.borderColor="#fff";
      }
      if($("#referral").length){
        var referralCode=$("#referral").val();
        if(referralCode!=""){
          $.when($.ajax({url:"/UIComRegister/isValidReferralCode",type:"POST",async: false,data:{referralCode:referralCode}})).done(function(data){
            if(data && data.errorCode=="" && !data.result.doesReferralCodeExists){
                $("#referralErrorDiv").text("Invalid referral code");
                valid=false;
            }
          });
        }
      }
      var campaignCode;
      if($("#campaignCode").length){
        campaignCode=$("#campaignCode").val();
        if(campaignCode!=""){
          $.when($.ajax({url:"/UIComRegister/isValidPromoCode",type:"POST",async: false,data:{campaignCode:campaignCode}})).done(function(data){
            if(data && !data.result.campaignCodeExists){
                $("#campaignErrorDiv").text("Invalid Promo Code");
                valid=false;
            }
          });
        }
      }
      $("#contactlength").text("");
      return valid;
    }

    var checkUsernameExist = function(){
      var $this = $(this);
      var val = $this.val();
      var resultDiv = $this.siblings(".signupErrorDiv");
      $.get("/UIComRegister/checkUsernameExist",{email:val},function(data){
        if(data && data.errorCode=="" && data.result.doesEmailExists){
          emailflag=1;
          showMsg("Email already registered with us !!").removeClass("success");
        }else{
          emailflag=0;
          showMsg("Login-id available, go ahead!").addClass("success");
        }
      });
      function showMsg(text){
        return resultDiv.text(text).animate({height: "22px"}, 250);
      }
    };

    var checkContactNumberExist=function(){
      var $this = $(this);
      var contact = $this.val();
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var resultDiv = $("#contactNumbererrorDiv");
      $.get("/UIComRegister/checkContactNumberExist",{contactNumber:contact,countryCode:countrycode},function(data){
        if(data && data.errorCode=="" && !data.result.doesContactNumberExists){
          contactflag=1;
          showMsg("ContactNumber already registered with us !!").removeClass("success");
        }else{
          contactflag=0;
          showMsg("ContactNumber available, go ahead!").addClass("success");
        }
      });
        function showMsg(text){
          return resultDiv.text(text).animate({height: "22px",position:"relative"}, 250);
        }

    };
};
//Get phonenumber for OTP
var otpProcess = new function(){
    this.init=function(){
      var url=window.location.href;
      // Fill name
      if (url.indexOf("FirstName")>-1) {
          var name=getURLParameter("FirstName");
          document.getElementById("username").value=name;
      }
      // Fill Contact Number
      if (url.indexOf("contactnumber")>-1) {
        if($("#contactNumber").length){
          var contactnumber=getURLParameter("contactnumber");
          document.getElementById("contactNumber").value=contactnumber;
        }
      } else if(url.indexOf("Phone")>-1) {
          var contactnumber=getURLParameter("Phone");
          document.getElementById("contactNumber").value=contactnumber;
      } else {
        if($("#contactNumber").length){
          document.getElementById("contactNumber").value = "";
          document.getElementById("contactNumber").disabled=false;
        }
      }
      var defaultCountryCode="in";
      if(url.indexOf("countrycode")>-1) {
        var countrycode=getURLParameter("countrycode");
        var countryData = $.fn.intlTelInput.getCountryData();
        for(i=0;i<countryData.length;i++) {
          if(countrycode==countryData[i].dialCode) {
            defaultCountryCode=countryData[i].iso2;
          }
        }
      }
      $("#contactNumber").intlTelInput({
        initialCountry:defaultCountryCode,
        formatOnDisplay:false,
        separateDialCode: false
      });
      $("#contactNumber").keyup(function(event){
        if(event.keyCode == 13){
          $("#clickToSignUp").click();
        }
      });
    }
    this.otpsend=function() {
      var username = document.getElementById("username").value;
      var valid=true;
      if(username==""){
        $("#usernamelength").text("Please enter a username.");
        return false;
      }
      else{
        $("#usernamelength").text("");
      }
      var url = window.location.href;
      if (url.indexOf("type") > -1) {
          var type = getURLParameter("type");
      } else {
          type = $("input[name=programType]:checked").val();
          document.getElementById('progType').value=type;
      }
      var campaignCode;
      if($("#campaignCode").length){
        campaignCode=$("#campaignCode").val();
        if(campaignCode!=""){
          $.when($.ajax({url:"/UIComRegister/isValidPromoCode",type:"POST",async: false,data:{campaignCode:campaignCode}})).done(function(data){
            if(data && !data.result.campaignCodeExists){
                $("#campaignErrorDiv").text("Invalid Promo Code.");
                valid=false;
            }
          });
        }
      }
      if (url.indexOf("countrycode") > -1) {
          var countrycode = getURLParameter("countrycode");
      }
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var contactNumber = document.getElementById("contactNumber").value;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
          if (contactNumber.length !=10) {
              $("#contactlength").text("Contact Number must be of 10 digits.");
              valid=false;
          }
          else if(!pattern.test(contactNumber)){
            $("#contactlength").text("Invalid Number.");
            valid=false;
          }
      } else {
          if ($.trim($("#contactNumber").val())) {
              if ($("#contactNumber").intlTelInput("isValidNumber")){
                  $("#contactlength").text("");
              }
              else {
                  $("#contactlength").text("Invalid Number.");
                  valid=false;
              }
          }
      }

      if(valid == true){
        $("#contactlength").text("");
        var popup = showVPopup(0.7);
        var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
        popup.html(div);
        var sectionId = $(this).data("sectionId");
        var orgId = $("input[name=orgId]").val();
        var params = {
            fullName: username,
            orgId:orgId,
            contactNumber: contactNumber,
            countryCode: countrycode,
            progType: type
        };
        if(campaignCode && campaignCode.length > 0 && campaignCode!=""){
          params.campaignCode = campaignCode;
        }
        $.post("/UIComRegister/verifyContactNumber",params,function(data){
          if(data.errorCode!="" || data.errorMessage!=""){
            closeVPopup();
            showErrorBox(""+data.errorMessage);
            return false;
          }
          else
            popup.load("/UIComRegister/sendOTP", params);
        });
    }
}

  this.otpValidate=function() {
        // var userId = $(this).data("userId");
        var otp = document.getElementById("otp").value;
        var orgId=$("#orgId").val();
        var $data =$("#result").data();
        var campaignCode;
        var params = {
          "userOTP": otp,
          firstName:$data.fullName,
          username:$data.username,
          contactNumber:$data.contactNumber,
          countryCode:$data.countryCode
        };
        $.post("/uicomregister/validateOTP", params, function(data) {
          if (data.errorCode != "" || data.errorMessage!="") {
                $( "#otperror" ).addClass( "loginErrorMsg" );
                $("#otperror").html("OTP is invalid");
              }
              else {
                closeVPopup();
                var popup = showVPopup(0.7);
                var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
                popup.html(div);
                if($("#campaignCode").val()!=""){
                  campaignCode = $("#campaignCode").val();
                  params.campaignCode = campaignCode;
                }
                delete params.userOTP;
                params.progType=$data.progType;
                params.orgId= orgId;
                params.isNewUser = data.result.isNewUser;
                if(data.result.isUserLoggedIn==true){
                  closeVPopup();
                  $.post("/UIComRegister/authoriseContactNumber",params,function(data){
                    if(data.errorCode==""){
                      window.location.reload();
                    }else{
                      showErrorBox("Something went wrong, please try again later");
                    }
                  });
                }
                else{
                  if(data.result.isNewUser == true) {
                    closeVPopup();
                    var pop = showVPopup(0.7);
                    var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
                    pop.html(div);
                    params.isOTPsignup = true;
                    popup.load("/Security/signup",params,function(data){closeVPopup();location.reload();});
                  }else{
                    closeVPopup();
                    var pop = showVPopup(0.7);
                    var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
                    pop.html(div);
                    params.isOTPlogin = true;
                    popup.load("/Security/authentifyWithOTP",params,function(data){closeVPopup();location.reload();});
                  }
                }
              }
            });
    }
    this.otpcheck = function(){
      var url = window.location.href;
      var valid = true;
      if (url.indexOf("type") > -1) {
          var type = getURLParameter("type");
          document.getElementById('progType').value=type;
      } else {
          type = "All";
          document.getElementById('progType').value=type;
      }
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var username = document.getElementById("username").value;
      document.getElementById("countryCode").value = countrycode;
      var otp = document.getElementById("userOTP").value;
      var contactNumber = $("#contactNumber").val();
      console.log(contactNumber);
      var contactfield=document.getElementById("contactNumber");
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      document.getElementById("countryCode").value = countrycode;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
        if (contactNumber.length!=10) {
          $("#contactlength").text("Contact number must be exactly 10 digits.");
          return false;
        }
        else if(!pattern.test(contactNumber)){
          $("#contactlength").text("Invalid Number");
          return false;
        }
      }else {
        if ($.trim($("#contactNumber").val())) {
          if ($("#contactNumber").intlTelInput("isValidNumber")) {
          }else {
            $("#contactlength").text("Invalid Number");
            return false;
          }
        }
      }
      if($("#contactname").length){
        document.getElementById("contactname").value = contactNumber;
      }
      var campaignCode;
      if($("#campaignCode").length){
        campaignCode=$("#campaignCode").val();
        if(campaignCode!=""){
          $.when($.ajax({url:"/UIComRegister/isValidPromoCode",type:"POST",async: false,data:{campaignCode:campaignCode}})).done(function(data){
            if(data && !data.result.campaignCodeExists){
                $("#campaignErrorDiv").text("Invalid Promo Code");
                valid=false;
            }
          });
        }
      }
      var params = {
        "userOTP":otp,
        firstName:username,
        username:contactNumber,
        contactNumber:contactNumber,
        countryCode:countrycode
      }
      $.when($.ajax({url:"/UIComRegister/validateOTP",type:"POST",async: false,data:params})).done(function(data){
            if(data.errorCode!="" || data.errorMessage!=""){
                $("#otperror").html("OTP is invalid");
                valid = false;
            }
          });

      $("#contactlength").text("");
      return valid;
        }
      }
var contactNumberPopup = new function(){
  this.init=function(){
    $("#contactNumber").intlTelInput({
        initialCountry:"in",
        formatOnDisplay:false,
        separateDialCode: false
      });
    $("#contactNumber").keyup(function(event){
        if(event.keyCode == 13){
          $("#clickToSignUp").click();
        }
      });
  }
  this.validate=function(){
    var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var contactNumber = document.getElementById("contactNumber").value;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
          if (contactNumber.length !=10) {
              $("#contactlength").text("Contact Number must be of 10 digits");
              return false;
          }
          else if(!pattern.test(contactNumber)){
            $("#contactlength").text("Invalid Number");
            return false;
          }
      } else {
          if ($.trim($("#contactNumber").val())) {
              if ($("#contactNumber").intlTelInput("isValidNumber")){
              }
              else {
                  $("#contactlength").text("Invalid Number");
                  return false;
              }
          }
      }
      $("#contactlength").text("");
      var orgId = $("#orgId").val();
      var params = {
          contactNumber: contactNumber,
          countryCode: countrycode,
          orgId:orgId
      };
      $.post("/UIComRegister/verifyContactNumber",params,function(data){
        console.log(data);
        if(data.errorCode!="" || data.result.isNewPhone==true){
          // closeVPopup();
          var popup = showVPopup(0.7);
          popup.load("/UIComRegister/sendOTP", params);
        }
        else
         {
          closeVPopup();
          showErrorBox("Contact Number is not available, please enter a new one");
         }
      });
  }
}

var loginProcess=new(function(){
    var loginType,memberLogin="MEMBER_LOGIN",emailLogin="EMAIL_LOGIN",accessLogin="ACCESS_CODE",OTPLogin="OTP_LOGIN";
   this.init=function(params){
       params=params||{};
       if(params.loginType){
           loginType=params.loginType;
       }
       $(document)
       .on("click","#loginWithMemberId",loginWithMemberId)
       .on("click","#loginWithEmailId",loginWithEmailId)
       .on("click","#showAccessCode",showAccessCode)
       .on("click","#loginWithAccessCode",loginWithAccessCode)
       .on("click","#loginWithOTP",loginWithOTP)
       .on("enterkey",".loginCodeInput",loginWithAccessCode)
       .on("click",".backToLoginStart",backToLoginStart)
       .on("click",".showPasswordCheckbox",showPasswordCheckbox)
       .on("click","#clickToLogin",loginFormSubmit)
       .on("click","#clickToAccessLogin",accessLoginFormSubmit)
       .on("click",'.loginContent .forgotPass',forgotPass)
       .on("click",'.howToLoginBtn',howToLogin)
   };
   var howToLogin = function(){
	var popup = showVPopup(0.5);
	var d = "<div><img src='/public/images/loginSlides/how-to-login.jpg' alt='How to login' class='nonner' style='opacity:0;'/>";
	d += "<div class='boldy big16 blueTextColor'>Loading...</div></div>";
	popup.html(d);
	popup.find("img").load(function(){
		$(this).removeClass("nonner").fadeTo(300,1).siblings().remove();
	});
   };
   var showAccessCode=function(){
	$(".loginCodeForm").removeClass("nonner").find("input").val("").focus();
	$(this).addClass("nonner");
   };
   var loginWithAccessCode=function(){
       	var $this = $("#loginWithAccessCode");
	var loginCode = $(".loginCodeInput").val();
	if(!loginCode){
		showResponse("Access code is missing :(",true,"redTextColor");
		return;
	}
	var params = {"accessCode":loginCode,"getOrgKey":false};
	$.get("/UIComSecurity/authAccessCode",params,function(resp){
		if(resp && resp.errorCode=="" && resp.result){
        		loginWithUtil($this);
			$("#loginAccessCode").val(loginCode);
		}else{
			showResponse("Access code entered is not matching :(",true,"redTextColor");	
		}
	});
   };   
   var loginWithMemberId=function(){
        loginWithUtil($(this));
   };      
   var loginWithEmailId=function(){
        loginWithUtil($(this));
   };
   var loginWithOTP=function(){
        loginWithUtil($(this));
   };   
   var backToLoginStart=function(){
       loginType=null;
       loginFormChildShowUtil('0');
       $(".loginCodeForm").addClass("nonner").find("input").val("");
       $("#showAccessCode").removeClass("nonner");
   };
   var loginWithUtil=function($this){
       loginType=$this.data("loginType");
       var id="emailLoginDiv";
       if(loginType===memberLogin){
           id='memberLoginDiv';
       }else if(loginType===accessLogin){
           id='accessCodeLoginDiv';
       }else if(loginType===OTPLogin){
          id="OTPLoginDiv";
       }
       $("#loginFormDiv").children(".loginDiv").addClass("nonner");
       $("#"+id).removeClass("nonner");
       loginFormChildShowUtil('-440',function(){
		$("#loginUsername").focus();
		$("#loginMemberId").focus();
		$("#accessFirstName").focus();
		var instInput = $("#loginOrgId");
		if(instInput.get(0) && instInput.val().length<=0){
			$("#loginOrgName").val("").focus();
		}
	});
   };
   var loginFormChildShowUtil=function(left,cbFn){
	hideError();
        $(".loginPageInput").val("");
        $("#signInWrapper").stop().animate({
                left : left
        }, 500,function(){
		if(cbFn){
			try{cbFn();}catch(err){}
		}	
	});
   };
   
   
   
   //show password
   var showPasswordCheckbox=function(){
        if($("#userPassword"+loginType).data("shown") ==="password"){
             var val = $("#userPassword"+loginType).addClass("nonner").data("shown","text").val();
             $("#userPasswordText"+loginType).removeClass("nonner").val(val);
        }else{
             var val = $("#userPasswordText"+loginType).addClass("nonner").val();
             $("#userPassword"+loginType).removeClass("nonner").data("shown","password").val(val);
        }       
   };   
   
  function checkForCookieEnabled(){
	if(navigator.cookieEnabled){
		return true;
	}else{
		try{
			var msg = "<span class='redTextColor big16'>";
			msg += "Your browser's cookie functionality is turned off. Please turn it on.?";
			msg += "</span>";
			showVPopupMsg(msg,-1);
		}catch(err){}
		return false;
	}
  }; 
  var accessLoginFormSubmit = function(e){
        e.preventDefault();
	if(!checkForCookieEnabled()){
		return false;
	}
        var loginUserName = $.trim($("#loginAccessEmail").val());
        var loginFirstName = $.trim($("#accessFirstName").val());
	if(!loginFirstName){
		showErrors("Please enter your name!");
		loginFirstName.focus();
	}else if(!loginUserName){
		showErrors("Please enter your Email-ID!");
		loginUserName.focus();
	}else{
            $('#accessLoginForm').submit();
	}
  };
   //submit form
  var loginFormSubmit=function(e){
        e.preventDefault();
	if(!checkForCookieEnabled()){
		return false;
	}
        var loginUserName = $("#loginUsername");
        var loginMemberId= $("#loginMemberId");
        var userPasswordEl= $('#userPassword'+loginType);
        var userPasswordTextEl= $('#userPasswordText'+loginType);
        $(".errorWrapperDiv").show();
        var errorWrapper=$(".errorWrapper");
        var user;
        if(loginType===memberLogin){
            	user=loginMemberId.val();
		if($("#loginOrgId").val().trim().length==0){
            		errorWrapper.text(loginErrors.missingOrg);
			return;
		}
        }else{
             user=loginUserName.val();
        }
            
        var pass = $('#userPassword'+loginType).val();
        if(userPasswordEl.data("shown")==="text"){
              pass = userPasswordTextEl.val();
              userPasswordEl.val(pass);
        }
        
        if(user =='' || pass == ''){    
            errorWrapper.text(loginErrors.blankError);                            
        }
        else if(loginType===emailLogin&&!validateEmail(loginUserName.val())){
            errorWrapper.text(loginErrors.emailError);
        }
        else {
          errorWrapper.text("");
          errorWrapper.parent().hide();
          if(loginType===memberLogin){
            $('#memberLoginForm').submit();
          }else{
            $('#loginForm').submit();
          }
        }
  };   
  var forgotPass=function(){
        var holder=$(this).closest(".loginDiv").find('.forgotPassWrap');
      	var popup = showVPopup();
	popup.html(holder.html());

	var loginType = $(this).data("loginType");
        if(loginType=="MEMBER"){
		var org = {};
		org.name = $("#loginOrgName").val();	
		org.id = $("#loginOrgId").val();
		if(org.id){
			popup.find("#forgotPassOrgName").val(org.name);
			popup.find("#forgotPassOrgId").val(org.id);
		}
		var memberId=$("#loginMemberId").val();
        	if(memberId){
            		popup.find("#forgetPassMemberId").val(memberId);
        	}
	}else{
		var email=$("#loginUsername").val();
        	if(email!=""){
            		popup.find("#forgotPassEmail").val(email);
        	}
	}       
  };
});
var showWrongPassPopup = function(){
	var popup = showVPopup();
	$(popup).on("close.popup",function(){
		$("#userPasswordMEMBER_LOGIN").focus();
		$("#userPasswordEMAIL_LOGIN").focus();
	});
	popup.html($(".wrongPassWrap").html());
};

