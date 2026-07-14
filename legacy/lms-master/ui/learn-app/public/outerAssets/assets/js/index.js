$(".preparation")
.on("click",".category-holder",openCategory);

function openCategory(){
	$this = $(this);
	if($this.data("name")==="all"){
		window.location.href="packages";	
	}
	else{
		window.location.href="/packages/"+$this.data("name");
	}
}
//Start function
//Remove after few days.

// setTimeout(function(){
// 	showVideoOnPageLoad();
// 	document.getElementById("myVideo").muted =true;
// },3000);

// function showVideoOnPageLoad(){
// 	swal({
// 		html:'<div class="popupVideoLoader row">'
// 		+'<div class="content col-md-8">'
// 		+'<h3 style="text-align:center;bottom:20px;position:relative;">Bathukamma (The History Behind)</h3>'
// 		+'<p>&nbsp;&nbsp;&nbsp;&nbsp;In the erstwhile kingdom of Vemulavaada which is currently known as Karimnagar, Rajarajeswara temple was very popular. Chola king of that time, learning that Rajarajeswara helped those in troubles turned as his devotee to weed off troubles while defending the attack from Raastrakoota kings. The Chola king who finally defeated Chalukya king destroyed the Rajeswara’s temple and took the Bruhat (huge) Siva linga as a gift for his father Raja Raja Chola, who built a temple to this huge Sivalinga (Brihadeswara) in Tanjavuru.</p>'
// +'<p>&nbsp;&nbsp;&nbsp;&nbsp;People of Vemulavaada in an attempt to console Parvathi devi (Bruhadamma) in the looted RajaRajeswara temple, and to show sorrowfulness to Cholas, started arranging flowers in the shape of Meru mountain and called it Bathukamma. At the top of the mountain like structure, Gouramma, made with turmeric is placed on top of the mountain like structure and is recounted with songs and dances for nine days. Bathukamma is formed using two words Bathuku & amma meaning life and mother respectively. At the end of nine days, the Bathukamma so created is dispatched into nearby rivers/lakes'
// +' Bathukamma festival is a social denouncing movement practiced from last 1000 years. Only songs are sung with mother Goddess Parvathi’s name with comforting, who is without Shiva.</p></div>'
// 		+'<div class="col-md-4 videoPlayer" style="padding:40px;">'
// 		+'	<video width="300" height="300" controls playsinline autoplay id="myVideo">'
// 		+'<source src="https://program-qa-learnpediaqa.s3.amazonaws.com/batukamma_video.mp4" type="video/mp4"></source>'
// 		+'	</video>'
// 		+'</div>'
// 		+'</div>',
// 		showConfirmButton: false,
// 		customClass:"videoPopupOnLoad",
// 		onClose:function(){
// 			$('.videoPlayer').remove();
// 			$("#videoPopupHolder").removeClass("nonner");
// 		}
// 	});
// 	// document.getElementById("myVideo").muted =false;
// }

// $("#videoPopupHolder").on("click",function(){
// 	showVideoOnPageLoad();
// })

//End function
/* For Phone Inputs */
$("#phone").intlTelInput({
	initialCountry: "in",
	separateDialCode: true,
	utilsScript: "/public/outerAssets/assets/js/utils.js"
});
$("#phone-footer").intlTelInput({
	initialCountry: "in",
	separateDialCode: true,
	utilsScript: "/public/outerAssets/assets/js/utils.js"
});
$(window).scroll(function() {    
	var scroll = $(window).scrollTop();
	if(scroll <= 400){
		$(".sticky-footer").removeClass("navbar-fixed-bottom");	
	}
	if (scroll >= 400) {
		$(".sticky-footer").addClass("navbar-fixed-bottom");
	}
	if(scroll + $(window).height() > $(document).height()-200) {
		$(".sticky-footer").addClass("hidden");
	}
	else{
		$(".sticky-footer").removeClass("hidden");	
	}
});
/* END For Phone Inputs */

/* For Otp Login and Signup */

$(".topScreen").on("click",".get-started-submit-btn",getStarted);
$(".sticky-footer").on("click",".get-started-submit-btn",getStarted);
$("#signup-modal").on("click",".sign-up-modal-btn",{ modalFlag: "true" },validateModalFields);
$("#signup-modal").on("click",".resend-otp-modal-btn",{ modalFlag: "true" },resendOtpModal);
$("#login-modal").on("click",".login-modal-btn",{ modalFlag: "false" },validateModalFields);
$("#login-modal").on("click",".resend-otp-modal-btn",{ modalFlag: "false" },resendOtpModal);

function getStarted(){
	$this = $(this);
	if($this.data("type")==="top")
	{
		var phone = $("#phone"); 
		var number = phone.val();
		$("#phone-footer").val(number);
	}
	else{
		var phone = $("#phone-footer"); 
		var number = phone.val();	
		$("#phone").val(number);
	}
	countryData = $.fn.intlTelInput.getCountryData();
	countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
	if (countrycode == "91") {
		if(number.length == 0 || number.length !== 10 || isNaN(number)){
			$("#phone-footer").attr("placeholder", "Invalid Number");
			$("#phone-footer").val("");
			$("#phone").attr("placeholder", "Invalid Number");
			$("#phone").val("");
			$(".getStartedInput").css("border","2px solid rgb(255, 0, 0)");
			return false;
		}
		$(".getStartedInput").css("border","1px solid green");
	} else {
		if ($.trim(number)) {
			if (!phone.intlTelInput("isValidNumber")) {
				$("#phone-footer").attr("placeholder", "Invalid Number");
				$("#phone-footer").val("");
				$("#phone").attr("placeholder", "Invalid Number");
				$("#phone").val("");
				$(".getStartedInput").css("border","2px solid rgb(255, 0, 0)");
				return false;
			}
			$(".getStartedInput").css("border","1px solid green");
		}
	}
	var orgId = $("input[name=orgId]").val();
	var params = {
		contactNumber: number,	
		countryCode: countrycode,
		orgId:orgId
	};

	$.post("/Register/sendOTP", params, function(data) {
		if (data.isVerifiedUser == true && data.isNewPhone == true) {
			$("#signup-modal").modal();
			$("#contact-number").val(number);
			$("#flagCode").val(countrycode);
		}
		else if (data.isVerifiedUser == true && data.isNewPhone == false) {
			$("#login-modal").modal();
			$("#login-modal #contact-number").val(number);
			$("#login-modal #flagCode").val(countrycode);
		}
		else {
			swal({
				html : "Your Contact Number is Not Verified, Please Login Through Your Email/InstituteID and Verify Your Contact Number",
				type : "warning"
			});
		}
	});
}


function validateModalFields(event){
	var id;
	if(event.data.modalFlag==="true"){
		id="#signup-modal";
		var name = $(id+" #full-name");
		var otp = $(id+" #otp");
		var contactNumber = $(id+" #contact-number").val();
		var countrycode = $(id+" #flagCode").val();
		var orgId = $(id+" #orgId").val();
	}
	else
	{
		id="#login-modal";
		var name = $(id+" #full-name");
		var otp = $(id+" #otp");
		var contactNumber = $(id+" #contact-number").val();
		var countrycode = $(id+" #flagCode").val();
		var orgId = $(id+" #orgId").val();
	}
	var validate = true;
	if(name.val()=="" || name.val() == null){
		$(id+" #name-group").addClass("has-error");
		$(id+" #name-dialog").addClass("zmdi-alert-triangle");
		$(id+" #name-dialog").css("display","inline-block");
		$(id+" #name-help").html("Please Enter Something!");
		validate = false;
	}
	else{
		$(id+" #name-group").removeClass("has-error");
		$(id+" #name-dialog").removeClass("zmdi-alert-triangle");
		$(id+" #name-dialog").css("display","none");
		$(id+" #name-help").css("display","none");
	}
	if(otp.val()=="" || otp.val() == null || isNaN(otp.val()) || otp.val().length !== 4){
		$(id+" #otp-group").addClass("has-error");
		$(id+" #otp-dialog").addClass("zmdi-alert-triangle");
		$(id+" #otp-dialog").css("display","inline-block");
		$(id+" #otp-help").html("Please Enter Correct Otp!");
		validate = false;
	}
	else
	{
		$(id+" #otp-group").removeClass("has-error");
		$(id+" #otp-dialog").removeClass("zmdi-alert-triangle");
		$(id+" #otp-dialog").css("display","none");
		$(id+" #otp-help").css("display","none");
	}
	var progtype = $("input[name=programType]:checked").val();
	var params = {
		"userOTP":otp.val(),
		username:contactNumber,
		contactNumber:contactNumber,
		countryCode:countrycode
	}

	$.when($.ajax({url:"/Register/validateOTP",type:"POST",async: false,data:params})).done(function(data){
		if(data.errorCode!="" || data.errorMessage!=""){
			swal({
				title : "Incorrect OTP",
				type:"error"
			});
			validate = false;
		}
	});
	if(validate){
		params.firstName =  name.val();
		params.progType= progtype;
		params.orgId = orgId;
		params.isOTPsignup = true;
		if(event.data.modalFlag==="true"){
			$.when($.ajax({url:"/Security/signup",type:"POST",async: false,data:params})).done(function(data){
				
			});
			window.location.reload();

		}
		else{
			delete params.progtype;
			delete params.isOTPsignup;
			$.when($.ajax({url:"/Security/authentifyWithOTP",type:"POST",async: false,data:params})).done(function(data){
				
			});
			window.location.reload();
		}
	}
}

function resendOtpModal(event){
	if(event.data.modalFlag==="true"){
		var countrycode = $("#signup-modal #flagCode").val();
		var contactNumber = $("#signup-modal #contact-number").val();
	}
	else{
		var countrycode = $("#login-modal #flagCode").val();
		var contactNumber = $("#login-modal #contact-number").val();
	}
	var orgId = $("input[name=orgId]").val();
	var params = {
		contactNumber: contactNumber,
		countryCode: countrycode,
		orgId:orgId
	};
	$.post("/Register/sendOTP", params, function(data) {
		if(data.errorMessage!=""){
			swal({
				text: 'OTP is resent',
				timer: 2000,
				type:"success"
			});
		}
		else
		{
			swal({
				text: 'Something went wrong',
				timer: 2000,
				type:"warning"
			});
		}

	});
}
/*END For Otp Login and Signup */

/* For Video PopUp */

$('#videos')
.off('click')
.on('click','.video',function(){
	$this = $(this);
	swal({
		html:	
		'<div class="embed-responsive embed-responsive-16by9 videoPlayer">'
		+'	<iframe width="560" height="315" src="'+$this.data("url")+'" frameborder="0" allowfullscreen>'
		+'	</iframe>'
		+'</div>',
		showConfirmButton: false,
		customClass: "videoPopup",
		onClose : function(){
			$('.videoPlayer').remove();
		}

	})
});

/* END For Video PopUp */

/* For Testimonials */
$(".testimonials")
.on("click",".container .testimonial-left",previousTestimonial)
.on("click",".container .testimonial-right",nextTestimonial);

function nextTestimonial(){
	var testimonial = $(".testimonials .active");
	$(".testimonials .testimonial-left").removeClass("hidden");
	var number = testimonial.data("number");
	if(number===2){
		$(".testimonials .testimonial-right").addClass("hidden");
	}
	if(number>=3){
		return;
	}
	testimonial.removeClass("active");
	testimonial.addClass("hidden");
	var next = $(".testimonials .testimonial-holder").children()[number+1];
	$(".testimonials .testimonial-holder .testimonial").fadeIn(500);
	next.className += " active";
	next.classList.remove("hidden");

};

function previousTestimonial(){
	var testimonial = $(".testimonials .active");
	var number = testimonial.data("number");
	$(".testimonials .testimonial-right").removeClass("hidden");
	if(number===1){
		$(".testimonials .testimonial-left").addClass("hidden");
	}
	if(number<=0){
		return;
	}
	testimonial.removeClass("active");
	testimonial.addClass("hidden");
	var previous = $(".testimonials .testimonial-holder").children()[number-1];
	$(".testimonials .testimonial-holder .testimonial").fadeIn(500);
	previous.className += " active";
	previous.classList.remove("hidden");
}
/*END For Testimonials */


$('.category-holder .description').text(function(_, txt) {
	if(txt.length > 100){
		txt = txt.substr(0, 100) + "...";
	}
	$(this).html(txt)
});