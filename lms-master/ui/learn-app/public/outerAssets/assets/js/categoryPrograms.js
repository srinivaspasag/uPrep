$("#programs-section")
.on("click",".buyButton",buyProgram);

function buyProgram(){
	$("#signup-modal").modal();
	$this = $(this);
	// console.log($this.data());
	var packageDays = $this.data("packageDays");
	var sectionId = $this.data("sectionId");
	$("#signup-modal #sectionId").val(sectionId);
	$("#signup-modal #packageDays").val(packageDays);
	$("#contactNumber").intlTelInput({
		initialCountry: "in",
		separateDialCode: true,
		formatOnDisplay:false,
		utilsScript: "/public/outerAssets/assets/js/utils.js"
	});
	var BuyNowsignUpForm = $('#otpForm');
	BuyNowsignUpForm.off("click","#otpButton");
	$("#signup-modal").on("click",".closeBtn",function(){
		resetForm();
	});
	BuyNowsignUpForm.on("click","#otpButton",function(){
		var validate=true;
		var countrycode = $("#contactNumber").intlTelInput("getSelectedCountryData").dialCode;
		var contactNumber = $("#contactNumber").val();
		var username = $("#loginusername").val();
		var sectionId = $("#sectionId").val();
		var packageDays = $("#packageDays").val();

		validate = validateContactAndName(contactNumber,username);

		if(validate){
			var params = {
				fullName: username,
				contactNumber: contactNumber,
				countryCode: countrycode,
				progType: "All"
			};
			var data = sendOtp(params);

			$("#otpButton").hide();
				//IF USER HAS NOT VERIFIED THE PHONE NUMBER AND PHONE NUMBER EXISTS IN DB
				if(data.isVerifiedUser == false){
					resetForm();
					$("#otpErrorModal").modal();
					return false;
				}
				//FOR SIGNUP USING PHONE NUMBER
				else if(data.isNewPhone == true){
					$("#validateButton").removeClass("hidden");
					$("#validateButton").addClass("visible");
					$("#isOTPsignup").value=true;
					$("#isOTPlogin").value=false;
				}
				//FOR LOGIN USING PHONE NUMBER
				else{
					$("#loginButton").removeClass("hidden");
					$("#loginButton").addClass("visible");
				}
				$("#contactNumber").prop("readonly","true");
				$("#otpformgroup").removeClass("hidden");
				$("#otpformgroup").addClass("visible");
				$("#resendOTP").removeClass("hidden")
				$("#resendOTP").addClass("visible")

				$("#validateButton").on("click",function(){
					params.userOTP = $("#userotp").val();
					params.orgId = $("#orgId").val();
					params.firstName  = username;
					$("#isOTPsignup").val(true);
					if(validateOtp(params)){
						document.getElementById("otpForm").action="/Security/signup";
						document.getElementById("otpForm").submit();
						return true;
					}
				});
				$("#loginButton").on("click",function(){
					params.userOTP = $("#userotp").val();
					if(validateOtp(params)){
						delete params.progtype;
						delete params.isOTPsignup;
						$("#uname").val(contactNumber);
						$("#countryCode").val(countrycode);
						document.getElementById("otpForm").action="/Security/authentifyWithOTP";
						document.getElementById("otpForm").submit();
						return true;
					}
				});
				$("#resendOTP").on("click",function(){
					sendOtp(params);
					swal({
						text: 'OTP is resent',
						timer: 2000,
						type:"success"
					}).then(
					function () {},
                  // handling the promise rejection
                  function (dismiss) {
                  });
				});
			}

		});
}

function resetForm(){

	$("#otpButton").show();
	$("#signup-modal").modal('hide');
	$("#signup-modal").find("#contactNumber").val("");
	$("#contactNumber").removeAttr("readonly");
	$("#signup-modal").find("#loginusername").val("");
	$("#signup-modal").find("#otpformgroup").addClass("hidden");
	$("#signup-modal").find("#loginButton").addClass("hidden");
	$("#signup-modal").find("#validateButton").addClass("hidden");
	$("#signup-modal").find(".resendOTP").addClass("hidden");
}

function validateContactAndName(contactNumber,username){
	var validate = true;
	if(contactNumber.length == 0 || contactNumber.length !== 10 || isNaN(contactNumber)){
		$("#contactNumbergroup").addClass("has-error");
		$("#contactNumberDialog").addClass("zmdi-alert-triangle");
		$("#contactNumberDialog").css("display","inline-block");
		$("#contactNumberHelp").html("Invalid Mobile Number");
		validate = false;
	}
	else{
		$("#contactNumbergroup").removeClass("has-error");
		$("#contactNumberDialog").removeClass("zmdi-alert-triangle");
		$("#contactNumberDialog").css("display","none");
		$("#contactNumberHelp").css("display","none");
	}
	if(username=="" || username == null){
		$("#usernamegroup").addClass("has-error");
		$("#name-dialog").addClass("zmdi-alert-triangle");
		$("#name-dialog").css("display","inline-block");
		$("#name-help").html("Please Enter Something!");
		validate = false;
	}
	else{
		$("#usernamegroup").removeClass("has-error");
		$("#name-dialog").removeClass("zmdi-alert-triangle");
		$("#name-dialog").css("display","none");
		$("#name-help").css("display","none");
	}
	return validate;
}
function sendOtp(params){
	var otp;
	params.orgId = $("#orgId").val();
	$.when($.ajax({url:"/Register/sendOTP",type:"POST",async: false,data:params})).done(function(data){
		otp = data;
	});
	return otp;
}
function validateOtp(params){
	var valid;
	$.ajax({
		async:false,
		type:"POST",
		url:"/Register/validateOTP",
		data:params,
		success:function(data){
			if(data.errorCode!="" || data.errorMessage!=""){
				$("#otpcodeerror").removeClass("hidden");
				$("#otpcodeerror").addClass("visible");
				valid = false;
			}
			else{
				$("#otpcodeerror").removeClass("visible");
				$("#otpcodeerror").addClass("hidden");
				valid = true;
			}
		}
	});
	return valid;
}
$('#programs-section .pti-body .description').text(function(_, txt) {
	if(txt.length > 200){
		txt = txt.substr(0, 200) + "...";
	}
	$(this).html(txt)
});
$('#programs-section .pti-body').on("click",".readMore",function(){
	var orgId = $(this).data("orgId");
	var sectionId = $(this).data("sectionId");
	var params = {
		orgId : orgId,
		sectionId : sectionId
	};
	$.get("/singleProgramPopup",params,function(data){
		swal({
			html:data,
			showConfirmButton:false
		});
	});
});
$("#programs-section").off("change")
.on("change","#programs",function(){
	$this = $(this);
	var programName = $this.find(':selected').data('programName');
	window.location.href="/packages/"+programName;
});
$("#programs-section").on("click",".year", function(){
	$this = $(this);
	if($this.data("year")=="1year"){
		$(".2year").removeClass("active");
		$(".1year").addClass("active");
		$(".1yearpackage").removeClass("hidden");
		$(".2yearpackage").addClass("hidden");
	}
	else{
		$(".1year").removeClass("active");
		$(".2year").addClass("active");
		$(".1yearpackage").addClass("hidden");
		$(".2yearpackage").removeClass("hidden");
	}
});