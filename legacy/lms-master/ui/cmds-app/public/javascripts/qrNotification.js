var qrNotification = new function(){
	var parDiv;
	var CLICK = "click.qrNotification";
	var CHANGE = "change.qrNotification";
	var SIZE = 20;
	var message;
	var programNames = [];
	var checkboxes = [];
	var programsData = {};
	this.init = function(){
		putConsoleLogs("in  init");
		parDiv = $("#qrOrgNotification");

		parDiv.off(CLICK).off(CHANGE)
		.on(CLICK,".showNewNotificationPopup",newNotificationPopup)
		.on(CHANGE,"#programCheckbox",programCheckboxChange)
		.on(CHANGE,"#resourceCheckbox",resourceCheckbox)
		.on(CLICK,".sendNotification",sendNotification)
		.on(CHANGE,".programSelector",programChageSelector)
		.on(CHANGE,".centerSelector",centerChageSelector)

		$.get("/Widgets/programListOfMemberJSON", function(data) {
			programsData = data.programs;
			putProgramsData();
		});
	};

	var newNotificationPopup = function(){
		[].forEach.call(document.querySelectorAll('input[type=checkbox]:checked'), function(checkboxe) {
		  // do whatever
		  programNames.push(checkboxe.value);
		  console.log("ProgramNames : "+checkboxe.value);
		});
		var params = {
			message : document.getElementById("message").value,
			programNames
		};
		//putConsoleLogs("params : "+ params.programNames[0].value);
		vReq.get("/QrNotification/sendToGCMDirect",params,function(data){
			if(data.result.success=="success")
				window.alert("Success : "+data.result.totalHits+"\n"+"Failure : "+data.result.failure);
			else
				window.alert("No such reg id present");
			window.location.reload(true);
		});
	};

	function putProgramsData(){
		setTimeout(function() {
			for(i=0;i<programsData.length;i++){
				$(".programSelector").append("<option data-program-id = '"+programsData[i].id+"' value='"+programsData[i].name+"'>"+programsData[i].name+"</option>");
			}
		},500);
	}

	var programChageSelector =function(){
		var programId = $(this).find(":selected").data("programId");
		for(i=0;i<programsData.length;i++){
			if(programsData[i].id == programId){
				resetSelectBox(".centerSelector","Select Center");
				resetSelectBox(".sectionSelector","Select Section");
				for(j=0;j<programsData[i].centers.length;j++){
					var center = programsData[i].centers[j];
					$(".centerSelector").append("<option data-sections = '"+JSON.stringify(center.sections)+"' data-center-id = '"+center.id+"' value='"+center.name+"'>"+center.name+"</option>");
				}
				break;
			}
		}
	}

	var centerChageSelector =function(){
		var centerId = $(this).find(":selected").data("centerId");
		var sections = $(this).find(":selected").data("sections");
		resetSelectBox(".sectionSelector","Select Section");
		for(var i =0 ;i < sections.length; i++){
			$(".sectionSelector").append("<option data-section-id = '"+sections[i].id+"' value='"+sections[i].name+"'>"+sections[i].name+"</option>");
		}
	}

	var resetSelectBox = function(target, defaultOption){
		$(target).html("");
		$(target).append("<option disabled = 'true' selected>"+defaultOption+"</option>")
	}

	var sendNotification = function(){
		var params = {

		}
		var notificationTitle = $.trim($(".notificationTable #notificationTitle").val());
		var notificationMessage = $.trim($(".notificationTable #notificationMessage").val());
		var notificationSummary = $.trim($(".notificationTable #notificationSummary").val());
		var notificationBigImage = $.trim($(".notificationTable #notificationBigImage").val());
		var programId = "";
		var resourceId = "";
		var resourseType = "";

		if(!notificationTitle || notificationTitle == ''){
			showError("Please enter Title");
			$("#notificationTitle").val("");
			return;
		}
		if(!notificationMessage || notificationMessage ==''){
			showError("Please enter Message");
			$("#notificationMessage").val("");
			return;
		}
		params.notificationTitle = notificationTitle;
		params.notificationMessage = notificationMessage;
		if($("#programCheckbox").is(':checked')){
			programId = $(".programSelector option:selected").data("programId");
			centerId = $(".centerSelector option:selected").data("centerId");
			sectionId = $(".sectionSelector option:selected").data("sectionId");
			if(programId === undefined || programId === null){
				showError("Please select a Program");
				return;
			}
			if(centerId === undefined || centerId === null){
				showError("Please select a Center");
				return;
			}
			if(sectionId === undefined || sectionId === null){
				showError("Please select a section");
				return;
			}
			params.sectionId = sectionId;
			if($("#resourceCheckbox").is(":checked")){
				resourceId = $.trim($(".notificationTable #resourceId").val());
				resourseType = $('.notificationTable input[name=resourseType]:checked').val();
				if(!resourseType || resourseType == undefined){
					showError("Please select Resource type");
					return;
				}
				if(!resourceId || resourceId == ''){
					showError("Please enter Id");
					return;
				}
				params.entityId = resourceId;
				params.entityType = resourseType;
			}
		}
		if(notificationSummary !=''){
			params.notificationSummary = notificationSummary
		}
		if(notificationBigImage !=''){
			params.notificationBigImage = notificationBigImage
			// validateImageURL(notificationBigImage);
		}
		console.log(params);
		vReq.post("/QrNotification/sendFirebaseNotification",params,function(data){
			if(data.erroCode!=""){
				showError(data.errorMessage);
			}
			if(data.result){
				showMessage("Notification Sent successfully!");
				resetInputFields();
			}
		});
	}


	var resetInputFields = function(){
		$("input[type=text]").val("");
	}

	function imageExists(url, callback) {
		var img = new Image();
		img.onload = function() { callback(true); };
		img.onerror = function() { callback(false); };
		img.src = url;
	}

	function validateImageURL(url)
	{
		var imageUrl = url;
		imageExists(imageUrl, function(exists) {
        //Show the result
        if(!exists){
			showError("Invalid Image Url");
        }
    });

	}


	var programCheckboxChange = function(){
		var programSelect = $('.programSelect');
		var resourseCheck = $('.resourseCheck');
		if($(this).is(':checked'))
		{
			programSelect.removeClass("disableRow");
			resourseCheck.removeClass("disableRow");
			programSelect.find('.selectPicker').prop("disabled",false);
			resourseCheck.find('input').prop("disabled",false);
			resourceCheckbox();
		}
		else{
			programSelect.addClass("disableRow");
			resourseCheck.addClass("disableRow");
			programSelect.find('.selectPicker').prop("disabled",true);
			resourseCheck.find('input').prop("disabled",true);
			resourseCheck.find('input').prop("checked",false);
			resourceCheckbox();
		}
	}

	var resourceCheckbox = function(){
		var Typerow = $('.resourseType');
		var IdRow = $('.resourseId');
		if($(this).is(':checked'))
		{
			Typerow.removeClass("disableRow");
			IdRow.removeClass("disableRow");
			Typerow.find('input').prop("disabled",false);
			IdRow.find('input').prop("disabled",false);
		}
		else{
			Typerow.addClass("disableRow");
			IdRow.addClass("disableRow");
			Typerow.find('input').prop("disabled",true);
			IdRow.find('input').prop("disabled",true);
		}
	}
};