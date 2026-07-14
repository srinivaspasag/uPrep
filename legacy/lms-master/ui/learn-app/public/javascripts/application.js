$(document).ready(function() {
  $("#tab-2").keydown(function(event){
    if(event.keyCode == 13) {
      event.preventDefault();
      return false;
    }
  });
});
var loginProcess = new function(){
  this.init=function(){
    $(document).on("click",".showPasswordCheckbox",showPasswordCheckbox);
    $(".forgot-password").on("click",forgotPassword);
    $(".forgetPassword").on("click",hideLogin);
    $(".loginShow").on("click",loginShow);
  }

  var hideLogin = function(){
    $("#loginBox").css("display","none");
  }

  var loginShow = function(){
    $("#loginBox").addClass("animated fadeInUp");
    $("#loginBox").css("display","block");
  }

  this.validate = function(){
    var loginId = $("#userLoginId").val();
    var userPasswordEl= $('#userPasswordEMAIL_LOGIN');
    var userPasswordTextEl= $('#userPasswordTextEMAIL_LOGIN');
    var loginIdElem = $("#userLoginId");
    var loginForm = $("#memberLoginForm");
    var usernameAuthUrl = "/Security/authentify";
    if(userPasswordEl.data("shown")==="text"){
        pass = userPasswordTextEl.val();
        userPasswordEl.val(pass);
        userPasswordTextEl.focus();
    }
    if(loginId == ""){
      document.getElementById('checkemail').innerHTML = "Enter your Email Address or InstituteID";
      $(".emailValidation").addClass("has-warning has-feedback");
      $(".emailValidation").find(".zmdi-alert-triangle").removeClass("nonner");
      loginIdElem.focus();
      return false;
    }
    else{
      document.getElementById("checkemail").innerHTML = "";
      $(".emailValidation").removeClass("has-warning has-feedback");
      $(".emailValidation").find(".zmdi-alert-triangle").addClass("nonner");
    }
    if(userPasswordEl.val() == ""){
      document.getElementById('checkpassword').innerHTML = "Enter your password";
      $(".passwordValidation").addClass("has-warning has-feedback");
      $(".passwordValidation").find(".zmdi-alert-triangle").removeClass("nonner");
      userPasswordEl.focus();
      return false;
    }
    else{
      document.getElementById("checkpassword").innerHTML = "";
      $(".passwordValidation").removeClass("has-warning has-feedback");
      $(".passwordValidation").find(".zmdi-alert-triangle").addClass("nonner");
    }
    if(validateEmail(loginId)){
        loginForm.attr("action",usernameAuthUrl);
        loginIdElem.attr("name","username");
      }else{
        loginIdElem.val(loginId.toUpperCase());
        }
  };
  //show password
   var showPasswordCheckbox=function(){
        var loginType;
        loginType='EMAIL_LOGIN';
        if($("#userPassword"+loginType).data("shown") ==="password"){
             var val = $("#userPassword"+loginType).addClass("nonner").data("shown","text").val();
             $("#userPasswordText"+loginType).removeClass("nonner").val(val);
        }else{
             var val = $("#userPasswordText"+loginType).addClass("nonner").val();
             $("#userPassword"+loginType).removeClass("nonner").data("shown","password").val(val);
        }       
   };

   function validateEmail(s){
     if (s.length >0) {
      var i=s.indexOf("@"),j=s.indexOf(".",i), k=s.indexOf(","), kk=s.indexOf(" "), jj=s.lastIndexOf(".")+1,
       len=s.length;
      if ((i>0) && (j>(i+1)) && (k==-1) && (kk==-1) && (len-jj >=2) && (len-jj<=4)) {
                return true;
      }
      else {
        return false;
      }

     }
     else return false;
 }

 function showError(errorMesssage){
      errorDiv = $("#forgetError");
      errorDiv.html(errorMesssage);
};

   var forgotPassword = function(){
    var orgId;
    orgId=$(".orgId").val();
    var email = $("#passwordForget").val();
    if(email == ""){
      document.getElementById('forgetError').innerHTML = "Enter an email";
      return false;
    }
    else if (!validateEmail(email)){
      document.getElementById("forgetError").innerHTML = "Enter a valid email";
      return false;
    }
    else{
      document.getElementById("forgetError").innerHTML = "";
    }
    var params= {
      orgId:orgId,
      username:email
    };
    $.get("/Register/userNameForgotPass", params, function(data){
      if(data.errorCode != ""){
           switch(data.errorCode){
        case "INVALID_ID" :
        case "USER_NOT_FOUND":errorMsg = "Error : User Not Found";
          break;
        case "USER_NO_VERIFIED_EMAIL": errorMsg = "<div>You have not registered your Email ID,</div>";
          errorMsg += "";
          break;
        default:errorMsg = "Error : Server failed";
          break;
         }
         showError(errorMsg);
        }
        else{
          $("#forgetError").removeClass("redTextColor");
          $("#forgetError").html("A Password reset link has been sent to your E-mail ID").addClass("success");
        }
    });
  }
}

var signupProcess = new function(){
    this.init=function(){
        var url=window.location.href;
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
        signUpForm.on("click",".referralCodeEnable",referralCodeEnable);
    }

    var referralCodeEnable = function(){
      $("#referrerEnableLink").addClass("nonner");
      $("#referrergroup").removeClass("nonner");
      $("#referral").focus();
    }
    this.validate = function() {
      var valid=true;
      var password = $("#password").val();
      var url = window.location.href;
      if (url.indexOf("type") > -1) {
          var type = getURLParameter("type");
          document.getElementById('progType').value=type;
      } else {
          // type = "All";
          type = $("input[name=programType]:checked").val();
          document.getElementById('progType').value=type;
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
          $.when($.ajax({url:"/Register/isValidReferralCode",type:"POST",async: false,data:{referralCode:referralCode}})).done(function(data){
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

    function getURLParameter(name){
      var param="";
      try{
      var url = location.search;
      if(!url){ return param;}
      url = url.replace("?","");
      name += "=";
      var nameIndex = url.indexOf(name);
      if(nameIndex>=0){
          param = url.substr(nameIndex+name.length);
          if((eIndex = param.indexOf("&"))>0){
              param = param.substring(0,eIndex);
          }
          param = decodeURIComponent(param);
      }
      }
      catch(err){ param="";}
      return param;
    }

    var checkUsernameExist = function(){
      var $this = $(this);
      var val = $this.val();
      // var resultDiv = $this.siblings(".signupErrorDiv");
      $.get("/Register/checkUsernameExist",{email:val},function(data){
        if(data && data.errorCode=="" && data.result.doesEmailExists){
          emailflag=1;
          $("#emailgroup").addClass("has-error");
          $("#emailDialog").addClass("zmdi-alert-triangle");
          $("#emailDialog").css("display","inline-block");
          $("#emailHelp").html("Email already registered with us !!");
          // showMsg("Email already registered with us !!").removeClass("success");
        }else{
          emailflag=0;
          $("#emailgroup").removeClass("has-error");
          $("#emailDialog").removeClass("zmdi-alert-triangle");
          $("#emailgroup").addClass("has-success");
          $("#emailDialog").addClass("zmdi-check");
          $("#emailDialog").css("display","inline-block");
          $("#emailHelp").html("Login-id available, go ahead!");
          // $("#emailDialog").css("display","none");
          // showMsg("Login-id available, go ahead!").addClass("success");
        }
      });
      // function showMsg(text){
      //   return resultDiv.text(text).animate({height: "22px"}, 250);
      // }
    };

    var checkContactNumberExist=function(){
      var $this = $(this);
      var contact = $this.val();
      var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      // var resultDiv = $("#contactNumbererrorDiv");
      $.get("/Register/checkContactNumberExist",{contactNumber:contact,countryCode:countrycode},function(data){
        if(data && data.errorCode=="" && !data.result.doesContactNumberExists){
          contactflag=1;
          $("#contactNumbergroup").addClass("has-error");
          $("#contactNumberDialog").addClass("zmdi-alert-triangle");
          $("#contactNumberDialog").css("display","inline-block");
          $("#contactNumberHelp").html("ContactNumber already registered with us !!");
          // showMsg("ContactNumber already registered with us !!").removeClass("success");
        }else{
          contactflag=0;
          $("#contactNumbergroup").removeClass("has-error");
          $("#contactNumberDialog").removeClass("zmdi-alert-triangle");
          $("#contactNumbergroup").addClass("has-success");
          $("#contactNumberDialog").addClass("zmdi-check");
          $("#contactNumberDialog").css("display","inline-block");
          $("#contactNumberHelp").html("ContactNumber available, go ahead!");
          // showMsg("ContactNumber available, go ahead!").addClass("success");
        }
      });
        // function showMsg(text){
        //   return resultDiv.text(text).animate({height: "22px",position:"relative"}, 250);
        // }

    };
};

var otpProcess = new function(){
  this.orgId = $("input[name=orgId]").val();
  this.otpcheck = function(){
      var url = window.location.href;
      var valid = true;
      if (url.indexOf("type") > -1) {
          var type = getURLParameter("type");
          document.getElementById('progType').value=type;
      } else {
        type = $("input[name=programType]:checked").val();
        document.getElementById('progType').value=type;
      }
      var countrycode = $(".contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var username = document.getElementById("username").value;
      document.getElementById("flagCode").value = countrycode;
      var otp = document.getElementById("userOTP").value;
      var contactNumber = $(".contactNumber").val();
      var contactfield=document.getElementById("contactNumber");
      var countrycode = $(".contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      document.getElementById("flagCode").value = countrycode;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
        if (contactNumber.length!=10) {
          $("#contactlength").text("Contact number must be exactly 10 digits");
          return false;
        }
        else if(!pattern.test(contactNumber)){
          $("#contactlength").text("Invalid Number");
          return false;
        }
      }else {
        if ($.trim($(".contactNumber").val())) {
          if ($(".contactNumber").intlTelInput("isValidNumber")) {
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
      $.when($.ajax({url:"/Register/validateOTP",type:"POST",async: false,data:params})).done(function(data){
            if(data.errorCode!="" || data.errorMessage!=""){
                $("#otperror").html("OTP is invalid");
                valid = false;
            }
          });

      $("#contactlength").text("");
      return valid;
        }

      this.otpLogin = function(){
      var valid = true;
      var countrycode = $(".contactNumber").intlTelInput("getSelectedCountryData").dialCode;
      var username = document.getElementById("otpusername").value;
      // document.getElementById("countrycode").value = countrycode;
      var otp = document.getElementById("userloginOTP").value;
      var contactNumber = document.getElementsByClassName("contactNumber")[1].value;
      var contactfield=document.getElementById("contactNumber");
      var countrycode = $("#phoneNumber").intlTelInput("getSelectedCountryData").dialCode;
      document.getElementById("countrycode").value = countrycode;
      var pattern=new RegExp("^[6-9][0-9]{9}$");
      if (countrycode == "91") {
        if (contactNumber.length!=10) {
          $("#logincontactlength").text("Contact number must be exactly 10 digits");
          return false;
        }
        else if(!pattern.test(contactNumber)){
          $("#logincontactlength").text("Invalid Number");
          return false;
        }
      }else {
        if ($.trim($(".contactNumber").val())) {
          if ($(".contactNumber").intlTelInput("isValidNumber")) {
          }else {
            $("#logincontactlength").text("Invalid Number");
            return false;
          }
        }
      }
      if($("#contactName").length){
        document.getElementById("contactName").value = contactNumber;
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
      $.when($.ajax({url:"/Register/validateOTP",type:"POST",async: false,data:params})).done(function(data){
            if(data.errorCode!="" || data.errorMessage!=""){
                $("#otploginerror").text("OTP is invalid");
                valid = false;
            }
            else{
              $("#otploginerror").text("");
            }
          });

      $("#logincontactlength").text("");
      return valid;
        }

        this.resendOTP = function(){
          var contactNumber;
          var countryCode;
          if($("#memberSignupForm").hasClass("formactive")){
            contactNumber = $("#otpsignupContact").val();
            countryCode = $("#otpsignupContact").intlTelInput("getSelectedCountryData").dialCode;
          }
          else if($("#memberLoginForm").hasClass("formactive")){
            contactNumber = document.getElementsByClassName("contactNumber")[1].value;
            countryCode = $("#phoneNumber").intlTelInput("getSelectedCountryData").dialCode;
          }
          else if($("#form").hasClass("formactive")){
            contactNumber = $("#contactNumber").val();
            countryCode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
          }
          var params = {
            contactNumber: contactNumber,
            countryCode: countryCode,
            orgId:otpProcess.orgId
          };
          $.post("/Register/sendOTP", params, function(data) {
            if(data.errorMessage!=""){
              swal({
                  text: 'OTP is resent',
                  timer: 2000,
                  type:"success"
                  }).then(
                  function () {},
                  // handling the promise rejection
                  function (dismiss) {
                  });
            }
            else
            {
              swal({
                  text: 'Something went wrong',
                  timer: 2000,
                  type:"warning"
                  }).then(
                  function () {},
                  // handling the promise rejection
                  function (dismiss) {
                  });
            }
            // else if(data.errorMessage!="" && data.isNewPhone == false){
            //   $("#otploginmessage").html("OTP is sent");
            // }
          });
        }

      this.sendOTP = function(){
        var valid = true;
        var type="All";
        var username = document.getElementById("loginusername").value;
        var contactNumber = $("#contactNumber").val();
        var contactfield=document.getElementById("contactNumber");
        var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
        document.getElementById("countryCode").value = countrycode;
        var pattern=new RegExp("^[6-9][0-9]{9}$");
        if (countrycode == "91") {
          if (contactNumber.length!=10) {
            $("#numberlength").text("Contact number must be exactly 10 digits");
            return false;
          }
          else if(!pattern.test(contactNumber)){
            $("#numberlength").text("Invalid Number");
            return false;          }
        }else {
          if ($.trim($("#contactNumber").val())) {
            if ($("#contactNumber").intlTelInput("isValidNumber")) {
            }else {
              $("#numberlength").text("Invalid Number");
              return false;
            }
          }
      };
      $("#numberlength").text("");
      if(username == ""){
        $("#unamelength").text("Please enter a username");
        return false;
      }
      $("#unamelength").text("");
      if($("#uname").length){
        document.getElementById("uname").value = contactNumber;
      }
      $("#contactNumber").prop("readonly","true");
        var params = {
            fullName: username,
            contactNumber: contactNumber,
            countryCode: countrycode,
            progType: type,
            orgId:otpProcess.orgId
        };
        $.when($.ajax({url:"/Register/sendOTP",type:"POST",async: false,data:params})).done(function(data){
                $(".resendOTP").css("display","block");
                $("#form").addClass("formactive");
                $(".resendOTP").on("click",function(){
                  swal({
                  title: 'OTP is resent',
                  timer: 2000,
                  type:"success"
                  }).then(
                  function () {},
                  // handling the promise rejection
                  function (dismiss) {
                  });
                });
            // if(data.isVerifiedUser ==true && data.isNewPhone==false){
                if(data.isVerifiedUser==true && data.isNewPhone == true){
                  if(document.getElementById("form").name =="productForm"){
                    document.getElementById("radiogroup").style.display="block";  
                  }
                  else{
                    document.getElementById("radiogroup").style.display="table";
                  }
                  var progtype;
                  progtype = $("input[name=programType]:checked").val();
                  document.getElementById('type').value=progtype;
                  document.getElementById("isOTPsignup").value=true;
                  document.getElementById("isOTPlogin").value=false;
                  document.getElementById("validateButton").innerHTML="Signup";
                }
                else if(data.isVerifiedUser==false){
                  $("#otpErrorModal").modal();
                  return false;
                }
                document.getElementById("loginusername").readonly = true;
                if(document.getElementById("form").name =="productForm"){
                  document.getElementById("otpformgroup").style.display="block";
                }
                else{
                  document.getElementById("otpformgroup").style.display="table";
                }
                $("#otpButton").hide();
                document.getElementById("validateButton").style.display="inline-block";
                $("#validateButton").on("click",function(){
                  params.userOTP = $("#userotp").val();
                  $.ajax({
                    async:false,
                    type:"POST",
                    url:"/Register/validateOTP",
                    data:params,
                    success:function(data1){
                      if(data1.errorCode!="" || data1.errorMessage!=""){
                        $("#otpcodeerror").html("OTP is invalid");
                        valid = false;
                      }
                      else{
                        $("#validateButton").hide();
                        document.getElementById("loginButton").style.display="inline-block";
                        if(data.isVerifiedUser==true && data.isNewPhone == true){
                          document.getElementById("form").action="/Security/signup";
                          document.getElementById("form").submit();
                        }
                        else if(data.isVerifiedUser == true && data.isNewPhone == false ){
                          document.getElementById("form").action="/Security/authentifyWithOTP";
                          document.getElementById("form").submit(); 
                        }
                      }
                    }
                  })
                })
          });
        return valid;
      }
    }
$('.serverErrorMsg,.errorWrapperDiv').hide();
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
        $.post("/Register/setPassword",params,function(data){
            if(data.errorCode != ""){
              $(".updatePasswordWrapper").find(".serverErrorMsg").show().find("."+data.errorCode).show();
            }
            else{
              $(".updatePasswordWrapper").find(".passwordContent").html("<div class='text-center' style='f-16'>Password reset was successful, redirecting to login ...</div>");
              setTimeout(function(){
        // if(orgId){
        //   window.location.href = "/login/organization/"+orgId;
        // }else{
          window.location.href = "/login";
        // }
      },5000);
            }
          });
      }
});