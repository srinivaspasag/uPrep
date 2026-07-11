var instProfile = new function(){
	var parDivId = "#instProfilePage";
	var parDiv = $(document);
	this.init = function(){
		parDiv = $(parDivId);
		if(!parDiv.get(0) || parDiv.data("inited")) return;
		parDiv.on("click",".instGetOlderActivityFeeds",getMoreFeeds)
			  .on("click",".instPrDbtsContainer .loadMoreItems",moreDbtsAsked)                      
			  .on("click",".questionAttemptedQues",showQuesPopup)
              .on("click",".registerContactNumber",registerContactNumber)
              .on("click",".verifyEmail",function(){
                $.get("/UIComRegister/resendVerifyLink",function(data){
                        if(data && data.errorCode==""){
                            showVMsgBox("Email Verification Link sent, check your mail account and revisit here.","Ok","SUCCESS");
                        }
                });
               })
              .on("click",".registerEmail",function(){
               if(instHeader && instHeader.settings){
                instHeader.settings.regEmailPopupOpen();
              }
        })
        var orgId = $("#myInstitutePage").data("orgId");
        var userRole = $("#myInstitutePage").data("orgObj").userRole;
        //Disable recent activity in profile for students of Trividyaa organization.
        if(orgId !== null && orgId === "5bacb325e4b0ff57ab848e07" && userRole !== null && userRole === "STUDENT"){
           $(".instPrActivityContainer").closest(".instPrBottomTD").addClass("nonner");
        }
        else{
            getActivityFeeds({feedType:"NEW"});
        }
		getDbtsAsked();	
		this.remarks.init();
		parDiv.data("inited",true);
		this.profilePic.init();
	};
	var showQuesPopup = function(){
		showVMsgBox($(this).data("msg"),"Okay","SUCCESS");	
	};

    var registerContactNumber=function(){
        var popup = showVPopup(0.7);
        var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>";
        popup.html(div);
        var orgId = $(this).data("orgId");
        popup.load("/UIComRegister/contactNumberPopup",function(data){
            $(".signupPopup").find("#orgId").val(orgId);
        });
    }
	var moreDbtsAsked = function(){
		var cont = $(".instPrDbtsContainer");
		var curCount = cont.find(".instPrEachDbtAsked").length;
		var total = parseInt($(".instDiscussionCount").val(),10);
		var remains = total - curCount;
		if(remains>0){
			cont.find(".loadMoreItems").remove();
			getDbtsAsked(curCount);
		}
	};     
	var getDbtsAsked = function(start){
		start = start?start:0;
		//console.log(parDiv.data("profileUserId"));
		var holder = parDiv.find(".instPrDbtsContainer");
		var targetUserId = parDiv.data("profileUserId");
		var params = {"sortOrder":"DESC","start":start,"size":5,"targetUserId":targetUserId};
		//params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
		vReq.get("/Institute/getUserDoubtAsked",params,function(data){
			holder.append(data);		
		});
	};
	var getMoreFeeds = function(){
		var holder = parDiv.find(".instPrActivityContainer");
        	var lastId = holder.find(".notiFeed:last").data("feedId");
		$(this).remove();
		var params = {feedType:"OLD",beforeNewsActivityId:lastId};
		console.log(params);
		getActivityFeeds(params);
	};
	var getActivityFeeds = function(extParams){
		var userId = parDiv.data("profileUserId");
		var holder = parDiv.find(".instPrActivityContainer");
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
				var moreTxt = i18nJS("TXT_MORE");
				appendHTML += "<div><a class='instGetOlderActivityFeeds' data-user-id='"+userId+"'>"+moreTxt+"</a></div>";
			   }
			   holder.append(appendHTML);
			   MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
        		}
        		else{
				holder.append("<div class='userMessage'>"+i18nJS("NO_FEEDS_AVAILABLE")+"</div>");
			}
    		});
	};
	this.remarks = new function(){
		this.init = function(){
			if(!parDiv.get(0) || !parDiv.find(".instPrRemarksContainer").get(0)) return;
			parDiv.off("click",".showUserRemarkPopup",show)
                                .off("click",".instPrRemarksContainer .loadMoreItems",moreRemarks)
                                .on("click",".instPrRemarksContainer .loadMoreItems",moreRemarks)
				.on("click",".showUserRemarkPopup",show)
                        
                        getRemarks();     
		};
		var show = function(){
			var popup = showVPopup();
			var innerDiv = parDiv.find("#profileRemarkPopupId");
			popup.html(innerDiv.html());
			popup.find(".postUserRemark").off("click").on("click",post);
			popup.find(".profilePostRemarkBox").focus();
		}; 
		var post = function(){
			var $this = $(this);
			if($this.hasClass("btnDisabled")){
				return;
			}
			var targetUserId = parDiv.data("profileUserId");
			var content = $.trim($(this).closest(".profileRemarkPopup").find(".profilePostRemarkBox").val());
			if(!content.length){
				showError(i18nJS("BLANK_CONTANT_NOT_ALLOWED"));return;
			}
			var pr = {"targetUserId":targetUserId,"content":content};
			closeVPopup();
			$this.addClass("btnDisabled");
			vReq.post("/Remark/addRemark",pr,function(data){
				$this.removeClass("btnDisabled");
				putPostRemarkResp(data);
			},function(){
				$this.removeClass("btnDisabled");
				var data = "<div class='centerText boldy redTextColor' style='padding-top:30px;'>";
				data += i18nJS("POST_REMARK_FAILED")+"</div>";
				putPostRemarkResp(data);
			});
			function putPostRemarkResp(data){
				var div = parDiv.find(".instPrRemarksContainer").find(".instPrRemark:first");
				if(!div.get(0)){
					parDiv.find(".instPrRemarksContainer").html(data);
				}else{
					div.before(data);
				}
			}
		};
                var getRemarks=function(start){
                    start = start?start:0;
                    var holder = parDiv.find(".instPrRemarksContainer");
                    var targetUserId = parDiv.data("profileUserId");
                    var params = {"sortOrder":"DESC","start":start,"size":5};
		    var targetUserRole = parDiv.data("profileUserRole");
		    if(targetUserRole == "STUDENT"){
			params["targetUserId"] = targetUserId;
		    }else if(targetUserRole == "TEACHER"){
			params["providerId"] = targetUserId;
		    }else{
			return;
		    }
                    //params['parent'] = {'type':'ORGANIZATION','id':$("#myInstitutePage").data('orgId')};
                    vReq.get("/remark/remarks",params,function(data){
                            holder.append(data);		
                    },function(){
		    	holder.html("<div class='centerText boldy' style='padding-top:30px;'>"+i18nJS("ERROR_OCCURRED")+"</div>");
		    }); 
                };
                var moreRemarks = function(){
                        var cont = $(".instPrRemarksContainer");
                        var curCount = cont.find(".instPrRemark").length;
                        var total = parseInt($(".instPrRemarksCount").val(),10);
                        var remains = total - curCount;
                        if(remains>0){
                                cont.find(".loadMoreItems").remove();
                                getRemarks(curCount);
                        }
                };                   
	};
	this.profilePic = new function(){
		var qqVarForUpload;
		var btnHolder;
		var animHolder;
		this.init = function(){
			animHolder = parDiv.find(".instChngPicAnim");
			btnHolder = parDiv.find(".instChngPicHold");
			if(!btnHolder.get(0)){ return;}
			parDiv.on("mouseenter",".instPrPicHolder",showOnHover)
			btnHolder.on("mouseenter",showOnHover);
			parDiv.on("mouseleave",".instPrPicHolder",hideOnHover)
			btnHolder.on("mouseleave",hideOnHover);
			onFileAvailable();
		};
		var showOnHover = function(e){
			btnHolder.addClass("hovered");	
		};
		var hideOnHover = function(e){
			btnHolder.removeClass("hovered");	
		};
		var onFileAvailable = function(){
			qqVarForUpload= new qq.FileUploader({
                        	element: btnHolder.get(0),
                        	action: '/profile/uploadProfilePic',
                        	debug: true,
                        	sizeLimit : 5*1024*1024,
                        	allowedExtensions : ["jpg", "gif", "png"],
                        	multiple : false
                	});
                	qqVarForUpload.onUploadDone = onUploadDone;
                	qqVarForUpload.onUploadProgress = onUploadProgress;
                	btnHolder.find(".qq-upload-list").addClass("nonner");
                	var uploadButton=btnHolder.find(".qq-upload-button");
                	var uploadDropArea=btnHolder.find(".qq-upload-drop-area");
                	uploadButton.data("onFileChosen",onFileChosen);
                	uploadDropArea.data("onFileChosen",onFileDropped);
                	putMimeTypes(uploadButton);
                	uploadButton.addClass("uploadProfilePicBtn");
                	btnHolder.find(".qq-button-title").html("change");	
		};
		var putMimeTypes = function(holder){
                	var file = $(holder).find(":file");
                        $(file).attr("accept","image/*");
        	};
		var onUploadDone = function(id,fileName,response,statusCode){
			if(response && response.errorCode == "" && response.result.done){
				var imgUrl = response.result.thumbnail+"?timestamp="+(new Date()).getTime();
				var imgTag = parDiv.find(".instPrPic").attr("src",imgUrl);
				parDiv.find(".instPrPicHolder").data("prevUrl",imgUrl);
				$(".myInstProfilePic").attr("src",imgUrl);
				imgTag.error(function(){
					$(this).attr("src",response.result.thumbnail);
				});
			}else{
				showError(i18nJS("FAILED_TO_UPLOAD_PROFILE_PIC"));
			}
			reset();
		};
		var onUploadProgress = function(percentDecimal,progressText,id,fileName){
			var op = 1-percentDecimal;
			op = op > 0 ? op : 0.2;
			animHolder.css({"background-color":"rgba(255,255,255,"+op+")"});
		};
		var onFileChosen = function(input){
			onFileSelected(input,"_onInputChange",input);
		};
		var onFileDropped = function(input){
			onFileSelected(input,"_uploadFileList",input.files);
		};
		var onFileSelected = function(input,qqCbFn,qqCbFnParams){
			animHolder.removeClass("nonner");
			btnHolder.addClass("nonner");
			var img = parDiv.find(".instPrPic");
			var file = input.files[0];
			var reader = new FileReader();
			reader.readAsDataURL(file);
        		reader.onload = function(e) {
            			img.attr("src", e.target.result);
				var targetUserId = parDiv.data("profileUserId");
				var targetOrgMemberId = parDiv.data("profileMemberId");
				var orgData = getOrgRequestInfo();
				var params = {
					targetOrgMemberId: targetOrgMemberId,
            				targetUserId: targetUserId, 
					uploadFileParamName: "inputFile"
				};
				$.extend(params,orgData);
				qqVarForUpload.setParams(params);
                		qqVarForUpload[qqCbFn](qqCbFnParams);
        		};
			reader.onerror = function(){
				reset();
				showError(i18nJS("FAILED_TO_LOAD_IMAGE"));
			};	
		};
		var reset = function(){
			btnHolder.removeClass("nonner");
			animHolder.css({"background-color":"rgba(255,255,255,0.9)"}).addClass("nonner");
			var img = parDiv.find(".instPrPic");
			var prevImg = parDiv.find(".instPrPicHolder").data("prevUrl");
			if(img.attr("src")!=prevImg){
				img.attr("src",prevImg);
			}
		};
	};
};
$(function(){
	instProfile.init();
});
