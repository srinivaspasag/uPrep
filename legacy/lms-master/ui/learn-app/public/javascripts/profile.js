var instProfile = new function(){
    var parDivId = "#instProfilePage";
    var parDiv = $(document);
    this.init = function(){
        parDiv = $(parDivId);
        if(!parDiv.get(0) || parDiv.data("inited")) return;
        parDiv.on("click",".instGetOlderActivityFeeds",getMoreFeeds)
              .on("click",".registerContactNumber",registerContactNumber)
              .on("click",".registerEmail",registerEmail)
              .on("click","#submitNumber",submitNumber)
              .on("click","#postOtp",postOtp)
        parDiv.on("click",".verifyEmail",function(){
                $.get("/Register/resendVerifyLink",function(data){
                        if(data && data.errorCode==""){
                            swal({
                                title:"Email Verification Link sent, check your mail account and revisit here.",
                                type:"success"
                                });
                        }
                        else{
                            swal({
                                title:"Something went wrong,Please try again later",
                                type:"warning"
                            });
                        }
                    });
            });
        $(".viewMemberMappings").live('click',viewMemberMappings);      
        $(".viewProfileExtraInfo").live('click',viewProfileExtraInfo);
        getActivityFeeds({feedType:"NEW"});
        getDbtsAsked();
        this.profilePic.init();
    };
    this.profilePic = new function(){
        this.init = function(){
            btnHolder = parDiv.find(".instChngPicHold");
            if(!btnHolder.get(0)){ return;}
            onFileAvailable();
        }
        var onFileAvailable = function(){
            var uploader = new qq.FileUploader({
              element: btnHolder.get(0),
              action: '/Application/makeFile',
              debug: true,
              params: {
                  uploadFileParamName: "inputFile",
                  myUserId: USERID
              },
              onComplete:function(id, fileName, responseJSON){
                $(".instPrPic").attr("src","/public/images/common/horizontal-loader.gif");
               var imageParams = {
                    uploadFileParamName:"inputFile",
                    myUserId:USERID,
                    qqfile:responseJSON.fileName,
                    userRole:$("#myInstitutePage").data("orgObj").userRole,
                    targetUserId:$("#instProfilePage").data("profileUserId"),
                    targetOrgMemberId:$("#myInstitutePage").data("orgObj").orgMemberId
                }
                if(responseJSON.success==true){
                    $.post("/Application/uploadProfilePic",imageParams,function(response){
                        if(response && response.errorCode == "" && response.result.done){
                            var imgUrl = response.result.thumbnail+"?timestamp="+(new Date()).getTime();
                            var imgTag = parDiv.find(".instPrPic").attr("src",imgUrl);
                            parDiv.find(".instPrPicHolder").data("prevUrl",imgUrl);
                            $(".myInstProfilePic").attr("src",imgUrl);
                        }
                  });
                }
                else{
                  swal("something went wrong");
                }
            }
        });
    }
}

    var getActivityFeeds = function(extParams){
        var userId = parDiv.data("profileUserId");
        var holder = parDiv.find(".activityContainer");
        var clustered = true;
        var params = {feedType:"NEW",eId:userId,eType:"USER",size:5,"needClustered":clustered};
        params = $.extend(params,extParams);
        params = transformParams(params);
        $.get("/Institute/getActivityJSON",params,function(data){
        if(data.errorCode=="" && data.result.list.length>0){
            var appendHTML = "";
            $.each(data.result.list,function(i,feed){
            var aType = feed.info && feed.info.actionType ? feed.info.actionType : feed.eType;
            if(newsEntityProcessor.isSupported(aType)){
                        //appendHTML += activityFeedFns[aType].init(feed);
                        appendHTML += newsEntityProcessor.process("ACTIVITIES",feed,aType,clustered);
                    }
                });
            if(data.result.list.length==5){
                var moreTxt = i18nJS("TXT_MORE")+" Feeds";
                appendHTML += "<div><a class='instGetOlderActivityFeeds view-more' data-user-id='"+userId+"'>"+moreTxt+"</a></div>";
            }
            holder.append(appendHTML);
            MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
        }
        else{
            $("#activityHeader").css("display","none");
            holder.append("<div class='text-center f-20'>No feeds available!</div>");
        }
    });
};
    var registerEmail = function(){
        institute.openPopup();
    }
    var registerContactNumber = function(){
        $(".mobileNumberDiv").addClass("hidden");
        $(".numberHolderDiv").removeClass("hidden");
    }

    var submitNumber = function(){
        var contactNumber = $("#registerNumber").val();
        var countrycode = $("#registerNumber").intlTelInput("getSelectedCountryData").dialCode;
        $("#countryCode").val(countrycode);
        var pattern=new RegExp("^[6-9][0-9]{9}$");
        if (countrycode == "91") {
          if (contactNumber.length !=10) {
              swal({
                title:"Contact Number must be of 10 digits",
                type:"warning",
                timer:3000
              });
              return ;
          }
          else if(!pattern.test(contactNumber)){
            swal({
                title:"Invalid number",
                type:"warning",
                timer:3000
              });
              return ;
          }
      } else {
          if ($.trim(contactNumber)) {
              if ($("#registerNumber").intlTelInput("isValidNumber")){
              }
              else {
                swal({
                    title:"Invalid number",
                    type:"warning",
                    timer:3000
                });
                return ;
              }
          }
      }
      var params = {
          contactNumber: contactNumber,
          countryCode: countrycode
      };
      $.post("/Register/verifyContactNumber",params,function(data){
        if(data.errorCode !== "" || data.result.isNewPhone === true){
            $("#otpHolderDiv").removeClass("hidden");
            $("#submitNumber").addClass("hidden");
            $("#postOtp").removeClass("hidden");
            $("#registerNumber").prop("readonly",true);
            params.internalVerificationRequest = "true";
            params.orgId = $("#myInstitutePage").data("orgId");
            $.post("/Register/sendOTP",params,function(data){
                if(data.errorMessage==""){
                    swal({
                        title:"Something went wrong",
                        type:"warning"
                    });
                }
            });
        }
        else
         {
          swal({
            title:"Contact number is not available,please enter a new one",
            type:"error",
            timer:"5000"
          });
        }
    });
}
    var postOtp = function(){
        var contactNumber = $("#registerNumber").val();
        var countrycode = $("#registerNumber").intlTelInput("getSelectedCountryData").dialCode;
        var otp = $("#otpField").val();
        var params = {
          contactNumber: contactNumber,
          countryCode: countrycode,
          "userOTP":otp
      };
      $.post("/Register/validateOTP",params,function(data){
        if (data.errorCode != "" || data.errorMessage!="") {
            swal({
                title:"OTP is Invalid",
                type:"error",
                timer:"5000"
            });
        }
        else{
            delete params.userOTP;
            params.orgId = $("#myInstitutePage").data("orgId");
            $.post("/Register/authoriseContactNumber",params,function(data){
                if(data.errorCode==""){
                  institute.instProfilePage();
                }else{
                  swal({
                    title:"Something went wrong, please try again later",
                    type:"warning"
                  });
                }
            })
        };
    });
}
    var getMoreFeeds = function(){
        var holder = parDiv.find(".activityContainer");
            var lastId = holder.find(".notiFeed:last").data("feedId");
        $(this).remove();
        var params = {feedType:"OLD",beforeNewsActivityId:lastId};
        console.log(params);
        getActivityFeeds(params);
    };
    var getDbtsAsked = function(start){
        start = start?start:0;
        //console.log(parDiv.data("profileUserId"));
        var holder = parDiv.find(".doubtsContainer");
        var targetUserId = parDiv.data("profileUserId");
        var params = {"sortOrder":"DESC","start":start,"size":5,"targetUserId":targetUserId};
        params.orgId = $("#myInstitutePage").data("orgId");
        params.userRole = $("#myInstitutePage").data("orgObj").userRole;
        //params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
        vReq.get("/Institute/getUserDoubtAsked",params,function(data){
            holder.append("<div class='f-20 p-b-20'>Doubts Asked</div>");
            holder.append(data);
        });
    };
    var viewMemberMappings = function(){
        var $this = $(this);
        var targetUserId = $this.data("userId");
        var targetUserName = $this.data("userName");
        targetUserName = targetUserName ? targetUserName : "User";
        var pr = {
            "targetUserId":targetUserId,
            "targetUserName":targetUserName
        };
        vReq.get("/Institute/getMemberMappings",pr,function(data){
            swal({
                html:data,
            });
        });
    };
        var viewProfileExtraInfo = function(){
        var $this = $(this);
        var targetUserId = $this.data("userId");
        var targetUserName = $this.data("userName");
        targetUserName = targetUserName ? targetUserName : "User";
        var pr = {
            "targetUserId":targetUserId,
            "targetUserName":targetUserName
        };
        vReq.get("/Institute/getProfileExtraInfo",pr,function(data){
            swal({
                html:data,
            });
        });
    };
}