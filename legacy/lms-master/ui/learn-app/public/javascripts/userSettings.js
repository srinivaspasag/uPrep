var userSettings = new function(){
    var orgId;
    var userJson;
    this.init = function(popup,orgId){
        // var $popup = $(popup);
        var $popup = $("#settingsPopupH")
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
        userJson = $("#user").data("userDetails");
    };
    var subscribeBtn = function(){
        var holder = $(this).closest(".subscriptionEmail");
        vReq.get("/UserSettings/subscribeEmail",{"mailCategory":"NOTIFICATION"},function(data){
            if(data && data.errorCode==""){
                holder.text(holder.data("textSubscribed"));
            }
        });
    };
    var unsubscribeBtn = function(){
        var holder = $(this).closest(".subscriptionEmail");
        vReq.get("/UserSettings/unsubscribeEmail",{"mailCategory":"NOTIFICATION"},function(data){
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
        orgId = $("#myInstitutePage").data("orgId");
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
        $.post("/UserSettings/updateUsername",pr,function(data){
            if(data){
                if(data.errorCode===""){
                    stgnPopup.find(".myStngsLoginId")
                        .data("username",email).text(email).css({"color":"green"});
                    stgnPopup.find(".settingsLoginMsg1").remove();
                    stgnPopup.find(".settingsLoginMsg2").removeClass("nonner");
                    $this.closest(".instStngsUseEmailHold").find(".instStngsUseEmailMsg").removeClass("nonner");
                    $this.closest(".myStngsVerifyOldPwdHold").remove();
                }else if(data.errorCode==="CURRENT_WRONG_PASSWORD"){
                    swal("Old Password did not match.");
                }else if(data.errorCode==="USER_ALREADY_EXISTS"){
                    swal("The above Email-Id is already in use as LOGIN-ID by someone else.");
                }else{
                    swal("Some Error Occurred, Please try again.");
                }
            }else{
                swal("Some Error Occurred, Please try again.");
            }
        }); 
    };
    var showAddEmail = function(){
        var holder = $(this).closest(".instStngEmail");
        holder.find(".regEmailEachDiv").addClass("nonner");
        holder.find(".regEmailHolder").removeClass("nonner").find("input").focus();
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
    var submitNewEmail = function(){
        var holder = $(this).closest(".instStngEmail");
        var email = holder.find(".myStngsChngEmail").val();
        var existingEmail = holder.data("emailId");
        orgId = $("#myInstitutePage").data("orgId");
        if(validateEmail(email)){
            if(existingEmail == email){
                swal("You have chosen the Existing 'Email-Id' as your NEW 'Email-Id', please provide us with another 'Email-Id'");
                return;
            }
            var member = userJson;
            //member.dob = "2000-01-01";
            var pr = {
                dob:member.dob,
                gender:member.gender,
                lastName:member.lastName,
                firstName:member.firstName,
                email:email,
                orgId:orgId
            }
            $.post("/UserSettings/updateUser",pr,function(data){
                if(!data || data.errorCode){
                    swal("Add Email Failed");
                }else{
                    $.post("/UserSettings/updateUserEmailInOrgMember",pr,function(dat){
                        if(!dat || dat.errorCode){
                            swal(dat.errorMessage);
                        }else{
                            holder.find(".regEmailEachDiv").addClass("nonner");
                            holder.find(".regEmailNotVerified").removeClass("nonner").find(".userEmailId").text(email);
                        }
                    });
                }
            });
        }else{
            swal("Enter Valid Email Address");
        }
    };

    var submitNewEmailAndPassword =function(){
        var holder = $(this).closest(".instStngEmail");
        var email = holder.find(".myStngsChngEmail").val();
        var password=holder.find("#newpassword").val();
        var confirmpassword=holder.find("#confirmpassword").val();
        orgId = $("#myInstitutePage").data("orgId");
        if(!validateEmail(email)){
            swal("Invalid Email Id format");
            return;
        }
        if(password!=confirmpassword){
            swal("Password and confirm password do not match");
            return;
        }
        if(password==""){
            swal("Password cannot be empty");
            return;
        }
        var member=userJson;
        var params={
            firstName:member.firstName,
            gender:member.gender,
            dob:member.dob,
            password:password,
            email:email,
            orgId:orgId
        }
        $.post("/UserSettings/updateUser",params,function(data){
            if(!data || data.errorCode){
                swal(data.errorMessage);
            }else{
                $.post("/UserSettings/updateUserEmailInOrgMember",params,function(dat){
                    if(!dat || dat.errorCode){
                        swal(dat.errorMessage);
                    }else{
                        window.location.reload();
                    }
                });
            }
        });
    };
    var resendEmailLink = function(){
        var $this = $(this);
        $.get("/Register/resendVerifyLink",function(data){
            if(data && data.errorCode==""){
                $this.addClass("nonner");
                $this.siblings(".emailVerifyLinkSent").removeClass("nonner");
            }
        });
    };

    var checkUsernameExist = function(){
      var $this = $(this);
      var val = $this.val();
      var resultDiv = $(".signupErrorDiv");
      $.get("/Register/checkUsernameExist",{email:val},function(data){
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
        if(!$(".pwdChngSuccess").hasClass("nonner")){
            $(".pwdChngSuccess").css("display","none");
        }
        $(".myStngsPwd").css("display","none");
        $(".myStngsChngPwd").css("display","block");
        $(".showChngPwdBtnHold").css("display","none");
        $(".showChngPwdDoneBtnHold").css("display","block");
        // var holder = $(this).closest(".instStngLogin").addClass("showChngPwd");
        // showChngPwdMsg(holder);
    };

    var showChngPwdMsg = function(holder,msgCls){
        holder=$("#settingsContainer");
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
        orgId = $("#myInstitutePage").data("orgId");
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
           $.post("/UserSettings/changePassword",pr,function(data){
            if(data){
               if(data.errorCode){
                switch(data.errorCode){
                case "CURRENT_WRONG_PASSWORD" : showChngPwdMsg(holder,".curPwdNotMatch");
                    break;
                default : showChngPwdMsg(holder,".unknownError");
                    break;
                }
               }else{
                $(".myStngsPwd").css("display","block");
                $(".myStngsChngPwd").css("display","none");
                $(".showChngPwdDoneBtnHold").css("display","none");
                $(".showChngPwdBtnHold").css("display","block");
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
