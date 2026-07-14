var corsUploader = new function(){
	var myParams = {
		type : '',
		cbFns : {
			'onFileChoosen':function(){},
			'onUrlSigned':function(){},
			'onProgress':function(){},
			'onComplete':function(){},
			'onCancel':function(){},
			'onReUpload':function(){},
			'beforeSend':function(){}
		},
		fileTypes : [],
		mediaType : '',
		maxFileSize : 1024*1024,
		btnText : 'Choose File',
		btnClass : "blueButton",
		allowMimeTypes : []
	};
	var uploadDiv;
	var parDiv;
	var corsData;
	var qqVarForUpload;
	var verificationRequired;
	var LOCAL_UPLOAD_URL = "/QrAddContent/uploadLocalFile";
	var isUploadInProgress = false;
	this.init = function(parentDiv,uploaderDiv,pr){
		myParams['type'] = pr["type"];
		myParams['cbFns'] = pr["cbFns"]?pr["cbFns"]:myParams[cbFns];
		myParams['fileTypes'] = pr["fileTypes"];
		myParams['mediaType'] = pr["mediaType"];
		myParams['maxFileSize'] = pr["maxFileSize"]?pr["maxFileSize"]:myParams['maxFileSize'];
		myParams['btnText'] = pr["btnText"];
		myParams['btnClass'] = pr["btnClass"]?pr["btnClass"]:myParams['btnClass'];
		myParams['allowMimeTypes'] = pr["allowMimeTypes"]?pr["allowMimeTypes"]:myParams['allowMimeTypes'];
		parDiv = parentDiv;
		uploadDiv = uploaderDiv;
		regFns();
		isUploadInProgress = false;
        	fetchScripts([{fname:"uicomWidgets/fileuploader.js",cb:uploaderJsLoaded}]);
	};
	this.resetParams = function(parentDiv,uploaderDiv,pr){ // reset params other than call-back fns
		if(!pr) return false;
		parDiv = parentDiv;
		uploadDiv = uploaderDiv;
		$(pr).removeProp("cbFns");
		var fileTypes = myParams["fileTypes"];
		myParams = $.extend(myParams,pr);
		myParams["fileTypes"] = pr["fileTypes"]?pr["fileTypes"]:fileTypes;
		//console.log(myParams);
		return myParams;
	};
	var regFns = function(){
		$(parDiv).on("click",".cancelCorsUpload",cancelUpload)
			.on("click",".retryCorsUpload",reUpload);
	};
	var uploaderJsLoaded = function(){
        	qqVarForUpload= new qq.FileUploader({
            		element: uploadDiv.get(0),
            		action: '',
            		debug: true,
            		sizeLimit : myParams['maxFileSize'],
            		allowedExtensions : myParams['fileTypes'],
			params:{uploadFileParamName:"file"},
			multiple : false
        	});
        	qqVarForUpload.onUploadDone = onUploadDone;
        	qqVarForUpload.onUploadProgress = onUploadProgress; 
        	qqVarForUpload.onUploadBeforeSend = onUploadBeforeSend; 
        	uploadDiv.find(".qq-upload-list").addClass("nonner");
        	var uploadButton=uploadDiv.find(".qq-upload-button");
        	var uploadDropArea=uploadDiv.find(".qq-upload-drop-area");
		uploadButton.data("onFileChosen",onFileChosen);
		uploadDropArea.data("onFileChosen",onFileDropped);
		putMimeTypes(uploadButton);
        	uploadButton.addClass(myParams['btnClass']);
        	uploadDiv.find(".qq-button-title").html(myParams['btnText']);
	};
	var putMimeTypes = function(holder){
		var file = $(holder).find(":file");
		if(myParams.allowMimeTypes && typeof myParams.allowMimeTypes == "object" &&myParams.allowMimeTypes.length > 0){
			var types = myParams.allowMimeTypes.join(", ");
			$(file).attr("accept",types);
		}else if(typeof myParams.allowMimeTypes == "string" && myParams.allowMimeTypes.length > 0){
			$(file).attr("accept",myParams.allowMimeTypes);
		}else{
			$(file).removeAttr("accept");
		}
	};
	var onFileDropped = function(dataTransfer){
		onFileSelected(dataTransfer,"_uploadFileList",dataTransfer.files);
	};
	var onFileChosen = function(input){
		onFileSelected(input,"_onInputChange",input);
	};
	var onFileSelected = function(input,qqCbFn,qqCbFnParam){
		isUploadInProgress = false;
		var file = input.files[0];
		try{
			if(myParams.cbFns && myParams.cbFns.onFileChoosen){
				myParams.cbFns.onFileChoosen(file);
			}
		}catch(err){
			putConsoleError(err);
		}
		getUrlSigned(input,qqCbFn,qqCbFnParam);
		var div = showProgressDiv(file.name);
		uploadDiv.addClass("nonner");
		div.find(".progressBarHolder").startProgressBar();
	};
	var getUrlSigned = function(input,qqCbFn,qqCbFnParam){
		var params = {"fileName":input.files[0].name,"type":myParams['type'],"mediaType":myParams["mediaType"]};
		params["url"] = LOCAL_UPLOAD_URL;
		corsData = {};
		$.get("/QrAddContent/getUploadSigned",params,function(data){
			if(data && data.errorCode=="" && data.result){
				corsData = data.result;
				verificationRequired = data.result.verificationRequired;
				//corsData = $.extend(true,corsData,corsData.requestParams);
				corsData.fileName = params.fileName;
				//qqVarForUpload._onInputChange(input);
				qqVarForUpload[qqCbFn](qqCbFnParam);
				try{
					if(myParams.cbFns && myParams.cbFns.onUrlSigned){
						myParams.cbFns.onUrlSigned(input.files[0],params,cloneObject(corsData));
					}
				}catch(err){
					putConsoleError(err);
				}
			}else{
				corsData = undefined;
				var div = parDiv.find(".corsUploadProgressDiv");
				div.find(".corsUploadFileName").text(params.fileName);
				div.find(".progressBarTd").addClass("nonner");
				var respDiv = div.find(".progressFinishedTd").removeClass("nonner");
				respDiv.find(".successRespUpload,.errorRespUpload").addClass("nonner");
				respDiv.find(".errorRespUpload").removeClass("nonner");
				div.find(".cancelCorsUpload").addClass("nonner");
				showError("Something went wrong. Please try again later.");
			}
		});
	};
	var onUploadBeforeSend = function(xhr,name,params,file){
		try{
			if(myParams.cbFns && myParams.cbFns.beforeSend){
				myParams.cbFns.beforeSend(xhr,name,params,file);
			}
		}catch(err){
			putConsoleError(err);
		}
		isUploadInProgress = true;
		if(LOCAL_UPLOAD_URL == corsData.url){
			params['qqfile'] = name;
			params = $.extend(true,params,corsData.requestParams);
                	var queryString = qq.obj2url(params, corsData.url);
                	xhr.open("POST", queryString, true);
                	xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
                	xhr.setRequestHeader("X-File-Name", encodeURIComponent(name));
                	xhr.setRequestHeader("Content-Type", "application/octet-stream");
			return file;
		}else{
			var formData = new FormData();
			var pr = corsData.requestParams;
			for(keyIndex in pr){
				formData.append(keyIndex,pr[keyIndex]);
			}
			/*formData.append("key",corsData.key);
			formData.append("acl",corsData.acl);
			formData.append("AWSAccessKeyId",corsData.AWSAccessKeyId);
			formData.append("Policy",corsData.policy);
			formData.append("Signature",corsData.signature);
			formData.append("Content-type",corsData.contentType);*/
			formData.append("file",file);
			xhr.open("POST",corsData.url,true);
			return formData;
		}
	};
	var checkUploadSuccess = function(response,statusCode){
		if(statusCode>=200 && statusCode<300){
			if(!verificationRequired){
				return true;
			}else{
				if(response && response.success != false && response.errorCode!=undefined && response.errorCode==""){
					return true;
				}
			}
		}
		return false;
	};
	var onUploadDone = function(id,fileName,response,statusCode){
		isUploadInProgress = false;
		var div = parDiv.find(".corsUploadProgressDiv");
		div.find(".corsUploadFileName").text(fileName);
		div.find(".progressBarTd").addClass("nonner");
		var respDiv = div.find(".progressFinishedTd").removeClass("nonner");
		respDiv.find(".successRespUpload,.errorRespUpload").addClass("nonner");
		var success = false;
		if(checkUploadSuccess(response,statusCode)){
			respDiv.find(".successRespUpload").removeClass("nonner");
			success = true;
		}else{
			respDiv.find(".errorRespUpload").removeClass("nonner");
			div.find(".cancelCorsUpload").addClass("nonner");
			success = false;
		}
		try{
			if(myParams.cbFns && myParams.cbFns.onComplete){
				myParams.cbFns.onComplete(id,fileName,success,response,statusCode);
			}
		}catch(err){
			putConsoleError(err);
		}
	};
	var showProgressDiv = function(fileName){
		var div = parDiv.find(".corsUploadProgressDiv").removeClass("nonner");
		div.find(".corsUploadFileName").text(fileName);
		div.find(".progressFinishedTd").addClass("nonner");
		div.find(".progressBarTd").removeClass("nonner");
		div.find(".cancelCorsUpload").removeClass("nonner");
		return div;
	};
	var onUploadProgress = function(percentDecimal,progressText,id,fileName){
		isUploadInProgress = true;
		var progress = Math.round(percentDecimal*100);
		var div = showProgressDiv(fileName);
		div.find(".progressBarHolder").updateProgressBar(progress);
		try{
			if(myParams.cbFns && myParams.cbFns.onProgress){
				myParams.cbFns.onProgress(percentDecimal,progressText,id,fileName);
			}
		}catch(err){
			putConsoleError(err);
		}
	};
	var isProgress = this.isProgress = function(){
		return isUploadInProgress;
	};
	this.cancel = function(){
		if(!isProgress()){
			return false;
		}
		uploadDiv.removeClass("nonner").find(".qq-upload-list").addClass("nonner");
		parDiv.find(".corsUploadProgressDiv").addClass("nonner");
		try{ 
			qqVarForUpload._handler.cancelAll();
		}catch(err){}
		try{
			if(myParams.cbFns && myParams.cbFns.onCancel){
				myParams.cbFns.onCancel(uploadDiv);
			}
		}catch(err){
			putConsoleError(err);
		}
		isUploadInProgress = false;
		return true;
	};
	var cancelUpload = function(){
		var r = confirm("Are you sure to cancel uploading?");
		if(r){
			reUpload();
		}
		try{
			if(myParams.cbFns && myParams.cbFns.onCancel){
				myParams.cbFns.onCancel(uploadDiv);
			}
		}catch(err){
			putConsoleError(err);
		}
	};
	var reUpload = function(){
		isUploadInProgress = false;
		uploadDiv.removeClass("nonner").find(".qq-upload-list").addClass("nonner");
		parDiv.find(".corsUploadProgressDiv").addClass("nonner");
		try{ 
			qqVarForUpload._handler.cancelAll();
		}catch(err){}
		/*vReq.post("/QrAddContent/cancelVideoUpload",{},function(data){
			},function(data){
			}
		);*/
		try{
			if(myParams.cbFns && myParams.cbFns.onReUpload){
				myParams.cbFns.onReUpload(uploadDiv);
			}
		}catch(err){
			putConsoleError(err);
		}
	};
};
