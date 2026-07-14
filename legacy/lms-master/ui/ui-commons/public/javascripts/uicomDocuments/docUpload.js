var blueBtnClasses = "blueSubmitButton", uploadContentType = "DOC";
var docUpload = new (function($) {
	this.init = function(params) {
		if (params.domain == "CMDS_APP") {
			blueBtnClasses = "gBlueButton";
		}
		var contentType = params.contentType;
		uploadContentType = contentType;
		if (contentType == "VIDEO") {
			createVideoUploader();
		} else if (contentType == "DOC") {
			createUploader();
		}
	}
})(jQuery);
var uploadFillError = "Fields Marked with * are compulsory";
$(".uploadInstruct").live('click', function() {
	$(this).parent().children(".nonner").css("display", "block");
});
// for navigation between upload pages
$(".UPContentType").live(
		'click',
		function() {
			if ($(this).prop("checked")) {
				var contentTypeDiv = $(this).parent();
				contentTypeDiv.addClass("activeContentTypeDiv").siblings()
						.removeClass("activeContentTypeDiv");
			}
		});
$("#startUploadingFiles")
		.live(
				'click',
				function() {
					var uploadPage = $(this).closest("#uploadPageWrapper");
					var tagsJSON = returnAllTagsAdded($("#addTagsMasterDiv"));
					var docTitle = uploadPage.find(".UPFileTitle").val().trim();
					if (docTitle == '' || tagsJSON.subjectIds.length == 0) {
						showError(uploadFillError);
						return;
					} else {
						if (uploadQueue.length == 0) {
							showError("Please select some files for uploading");
							return;
						}

						var tags = tagsJSON.normalTags;
						var docScope = uploadPage.find(
								"#publicPrivateDoc .docScope:checked").val()
								|| "PUBLIC";
						$(window).scrollTop(0);
						uploadPage.find("#uploadDocs").addClass("nonner");
						var folderId = "", addContentPage = $(this).closest(
								"#addContentPage");
						if (addContentPage.length > 0) {
							folderId = addContentPage.data("folderId");
						}

						var params = {
							title : docTitle,
							folderId : folderId,
							tags : tags,
							scope : docScope,
							targetIds : tagsJSON.targetIds
						};

						if (uploadContentType == "VIDEO") {
							params.type = "VIDEO";
							var duration = getHrsMinsSecs(uploadPage
									.find(".timeDiv")) / 1000;
							if (duration <= 0) {
//								showError("Please Enter a proper duration.");
//								return;
							}else{
                                                            params.duration = duration;
                                                        }							
							var desc = uploadPage.find(".UPFileDesc").val();
							if (desc) {
								params.description = desc;
							}
							if (vedantuHub == "cmds-app") {
								var pp = uploadPage.find(".UPFilePP").val();
								if (pp
										&& (!hasNoSplChars(pp)
												|| pp.length < 30 || pp.length > 256)) {
									showError("The passphrase you have given does not match the instructions.");
									return;
								} else if (pp) {
									params.passphrase = pp;
								}
							}
						} else {
							params.type = uploadPage.find(
									".UPContentType:checked").val();
						}

						if (vedantuHub == "web-app") {
							params.brdIds = tagsJSON.brdIds;

						} else {
							params.courseId = tagsJSON.subjectIds;
							params.topicNames = tagsJSON.topicIds;
						}

						$(this).text("Uploading...");
						$.post("/uicomdocuments/createDoc", params, function(
								data) {
							var result = data.result;
							var docId = result._id || result.docId;
							uploadDoc.setParams({
								docId : docId
							});
							uploadPage.data("docId", docId);
							if (uploadQueue.length > 0)
								uploadPageUploadFns._onInputChange(uploadQueue
										.shift());
						});
					}
				});
$("#startEditingDoc").live(
		'click',
		function() {
			if (($("#uploadFileTitle_EditDoc").val() == '')
					|| $("#uploadFileCollege_EditDoc").val() == ''
					|| $("#uploadFileDept_EditDoc").val() == ''
					|| $("#uploadFileCourseName_EditDoc").val() == ''
					|| $("#uploadFileCourseCode_EditDoc").val() == "") {
				showError(uploadFillError);
				return;
			} else
				startEditingDoc();
		});
$(".addTocPage").live(
		'click',
		function() {
			var $this = $(this);
			var uploadPage = $(this).closest(".uploadPageHolder");
			showTopLoader();
			$.get("/uicomdocuments/toc", {
				docId : uploadPage.data("docId")
			}, function(data) {
				uploadPage.children(".UPTagFillDiv").toggleClass(
						"UPTagFillDiv UPTOCFillDiv").html(data).removeClass(
						"nonner");
				$this.parent().remove();
				showNextPrev(1, uploadPage.find(".UPTOCPreview"));
				hideTopLoader();
			});
		});

var uploadDoc;
function createUploader() {
	// if(uploadQueue.length>0)uploadQueue.splice(0,uploadQueue.length);
	uploadQueue = [];
	var uploadEl = $("#uploadDocs");
	uploadDoc = new qq.FileUploader({
		element : uploadEl.get(0),
		action : "/uicomdocuments/upload",
		debug : true,
		allowedExtensions : [ "pdf", "doc", "docx", "xls", "jpeg", "jpg",
				"png", "gif", "tiff", "bmp", "ppt", "odt", "xlsx", "pptx" ],
		sizeLimit : 50 * 1024 * 1024
	});
	var uploadType = "DOC_UPLOAD";
	uploadDoc.onUploadDone = uploadUtils.onUploadDone;
	uploadDoc.onUploadProgress = uploadUtils.onUploadProgress;
	uploadDoc.uploadType = uploadType;
	var uploadBtn = uploadEl.find(".qq-upload-button");
	uploadBtn.addClass(blueBtnClasses).data("uploadType", uploadType);
	uploadEl.find(".qq-button-title").html("Choose File");
}
var createVideoUploader = function() {
	uploadQueue = [];
	var uploadEl = $("#uploadDocs");
	uploadDoc = new qq.FileUploader({
		element : uploadEl.get(0),
		action : '/uicomdocuments/uploadVideo',
		debug : true,
		allowedExtensions : [ "webm", "ogg", "mp4", "swf" ],
                sizeLimit : 25 * 1024 * 1024
	// ["webm","ogg","mp4","avi","flv","m4v"]
	});
	uploadDoc.onUploadDone = uploadUtils.onUploadDone;
	uploadDoc.onUploadProgress = uploadUtils.onUploadProgress;
	uploadEl.find(".qq-upload-button").addClass(blueBtnClasses).data(
			"uploadType", "VIDEO_UPLOAD").css("max-width", "200px");
	uploadEl.find(".qq-button-title").html("Choose Video to Upload");
}
function displayUploadFillPage() {
	var uploadDocs = $("#uploadDocs"), UPListDiv = $("#uploadPageWrapper")
			.children(".UPListDiv");
	UPListDiv.html($("#selectFilesBodySample").html());
	UPListDiv.children(".selectedFilesBody").append(uploadDocs);
	uploadDocs.find(".qq-upload-button").data("uploadType", "DOC_UPLOAD");
	uploadDocs.find(".qq-button-title").text("Add more to create notebook");
	UPListDiv.siblings(".UPTagFillDiv,.UPProgressDiv").removeClass("nonner");
}
function calculatePercentUpload(percent) {
	var uploadPage = $("#uploadPageWrapper");
	var uploadFileList = uploadPage.find("#uploadFileList");
	var c = uploadFileList.find(".uploadCompleted").length;
	var t = uploadFileList.find(".UPFileQueue").length;
	var p = c * 100 / t + percent * 100 / t;
	var maxWidth = uploadPage.width();
	// 10 is the margin-right for prograess div and 10 is some extra just like
	// that
	var w = p * maxWidth / 100;
	var uploadProgressDiv = uploadPage.children(".UPProgressDiv");
	uploadProgressDiv.removeClass("nonner");
	uploadProgressDiv.find("#uploadProgressBar").width(w);
	uploadProgressDiv.find("#uploadProgressPercent").text(
			Math.round(p) + " % uploaded");
	var tabP = Math.round(p) * 18 / 100 + 6;
	$("#tab_UPLOAD  .uploadTabPercent").height(tabP);
}
function uploadFinished() {
	var uploadPage = $("#uploadPageWrapper");
	uploadPage.find("#startUploadingFiles").html("Uploading done !!");
	uploadPage.find("#uploadDocs").remove();
	$("#uploadDoneBalloon").stop().animate({
		top : '-13',
		left : '27'
	}, 1).css("display", "block");
	var finishHTML, secondBtn = "", firstBtn;
	if (uploadContentType == "DOC") {
		if (vedantuHub == "web-app") {
			firstBtn = '<a class="blueSubmitButton skipTocPage" href="/mycontent">Skip</a>';
		} else {
			firstBtn = '<a class="gBlueButton goToResources cmdsaPush" href="/sources">Skip</a>';
		}
		secondBtn = "<span class='" + blueBtnClasses
				+ " addTocPage margLeft'>Add TOCs</span>";
	} else {
		if (vedantuHub == "web-app") {
			firstBtn = '<a class="blueSubmitButton skipTocPage" href="/mycontent">Go To MyContent</a>';
		} else {
			firstBtn = '<a class="gBlueButton goToResources cmdsaPush" href="/sources">Go To Sources</a>';
		}
	}
	finishHTML = firstBtn + secondBtn;
	uploadPage.children(".UPProgressDiv").html(
			"<div class='margTop centry'>\n\
        " + finishHTML + "</div>");
	uploadPage.find(".UPTagFillDiv").addClass("nonner");
}

var uploadQueue = [], uploadPageUploadFns;
function queueUploadFiles(input, uploadFns) {
	uploadPageUploadFns = uploadFns;
	if (uploadQueue.length == 0) {
		displayUploadFillPage();
	}
	for ( var i = 0; i < input.files.length; i++) {
		var size = input.files[i].size;
		if (size == undefined)
			size = input.files[i].fileSize;
		size = uploadPageUploadFns._formatSize(size);
		$("#uploadFileList").append(
				"<div class='UPFileQueue' id='uploadFile" + uploadQueue.length
						+ "'>\n\
            <span class='UPFileName'>"
						+ input.files[i].name
						+ "</span><span class='UPFileSize'>" + size
						+ "</span></div>");
		uploadQueue.push(input.files[i]);
	}
	if (uploadContentType == "VIDEO") {
		$("#addContentPageMain").find(".vVideoHolder,.cmdsVidSep,#uploadDocs")
				.remove();
	}
	if (uploadQueue.length > 1)
		$("#uploadFileListHolder .UPFileListDummy").removeClass("nonner");
}

var uploadUtils = new (function($) {
	this.onUploadProgress = function(percentDecimal, progressText, id) {
		$("#uploadFileList #uploadFile" + id + " .UPFileSize").text(
				progressText);
		calculatePercentUpload(percentDecimal);
	}
	this.onUploadDone = function(id, fileName, result) {
		if (result.success) {
			$("#uploadFile" + id).addClass("uploadCompleted");
			$("#uploadFile" + id + " .UPFileSize").text("Done !");
		} else {
			$("#uploadFile" + id + " .UPFileSize").text("Failed");
		}
		calculatePercentUpload(0);
		if (uploadQueue.length > 0)
			uploadPageUploadFns._onInputChange(uploadQueue.shift());
		else
			uploadFinished();
	}
})(jQuery);
function returnAsyncRespDocUpload(fileName) {
	var response;
	$.ajax({
		url : "/uicomdocuments/createDoc",
		data : {
			title : fileName
		},
		async : false,
		type : "POST",
		success : function(data) {
			response = data.result;
		}
	});
	return response;
}

// add toc page fucntions
$(".inputToc").live(
		'keyup',
		function(e) {
			var uploadPage = $(this).closest(".uploadPageHolder");
			var tocDiv = uploadPage.find(".tocDiv");
			var levelCount = 0, maxHl = parseInt(tocDiv.find(".maxHl").val());
			var tocText = $(this).val(), $this = $(this);
			var pageId = uploadPage.find(".UPTOCPreview .currentPageView")
					.attr("rel");
			if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))
					&& tocText != '') {
				var headingLevel = uploadPage.find(".headingLevel").text()
						.substring(8);
				$.post("/uicomdocuments/addToc", {
					docId : uploadPage.data("docId"),
					tocText : tocText,
					headingLevel : headingLevel,
					pageIdAssociatedTo : pageId,
					pageIdAssociatedTO : pageId
				}, function(data) {
					var tocTable = tocDiv.find(".tocTable");
					tocTable.append(returnTocTr(tocText, '', tocDiv));
					tocTable.find("tr").each(
							function() {
								levelCount = parseInt($(this).children(
										"td.tocText").attr("rel"));
								if (levelCount >= maxHl)
									tocDiv.find(".maxHl").val(levelCount + 1);
							});
					$this.val("");
				});
			}
		});
$(".removeToc").live('click', function() {
	var remT = $(this).parent("td").parent("tr");
	if (remT.children(".tocText").hasClass("tocChild")) {
		remT.remove();
	} else {
		tocRemove(remT, $(this).closest(".tocDiv"));
	}
});
$(".hlne")
		.live(
				'click',
				function() {
					var pa = $(this).parent();
					var hl = parseInt(pa.children(".headingLevel").text()
							.substring(8)), maxHl = parseInt(pa.children(
							".maxHl").val());
					hl++;
					if (hl <= maxHl) {
						pa.children(".headingLevel").html("Indent: " + hl);
					} else {
						showError("Please fill the TOC with heading Level "
								+ maxHl + " first");
						return;
					}
					if (hl > 1)
						pa.children(".hlpr").attr("src",
								"/public/images/uicomDocuments/hlprActive.png")
								.css("cursor", "pointer");
					else
						pa
								.children(".hlpr")
								.attr("src",
										"/public/images/uicomDocuments/hlprInactive.png")
								.css("cursor", "default");
				});
$(".hlpr")
		.live(
				'click',
				function() {
					var pa = $(this).parent();
					var hl = parseInt(pa.children(".headingLevel").text()
							.substring(8));
					if (hl > 1)
						hl--;
					pa.children(".headingLevel").text("Indent: " + hl);
					if (hl > 1)
						pa.children(".hlpr").attr("src",
								"/public/images/uicomDocuments/hlprActive.png")
								.css("cursor", "pointer");
					else
						pa
								.children(".hlpr")
								.attr("src",
										"/public/images/uicomDocuments/hlprInactive.png")
								.css("cursor", "default");
				});
$(".docPageNumInputUP").live(
		'keyup',
		function(e) {
			var pa = $(this).parent(), num = $(this).val();
			if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
				if (isNaN(parseInt(num)) || num == '')
					showError("please enter a number");
				else if (parseInt(num) < 1
						|| parseInt(num) > parseInt(pa.children(
								".docTotalPagesUP").text()))
					showError("The page number you entered is unavailable");
				else
					pageChangeFunc(parseInt(num), $(this).closest(
							".UPTOCPreview"));
			}
		});
$(".filePreviewNext").live(
		'click',
		function() {
			var UPTOCPreview = $(this).closest(".UPTOCPreview");
			pageChangeFunc(parseInt(UPTOCPreview.find(".currentPageView").attr(
					"id").substring(6)) + 1, UPTOCPreview);
		});
$(".filePreviewPrevious").live(
		'click',
		function() {
			var UPTOCPreview = $(this).closest(".UPTOCPreview");
			pageChangeFunc(parseInt(UPTOCPreview.find(".currentPageView").attr(
					"id").substring(6)) - 1, UPTOCPreview);
		});
$(".tocTable tr").live('mouseover', function() {
	$(this).children(".tocText").addClass("HLTocText");
	$(this).children(".addEditToc").css("opacity", "1");
});
$(".tocTable tr").live('mouseout', function() {
	$(this).children(".tocText").removeClass("HLTocText");
	$(this).children(".addEditToc").css("opacity", "0");
});
$(".tocText").live(
		'click',
		function() {
			var tocText = $(this).html();
			if ($(this).children("input").val() == undefined)
				$(this).html("<input type='text' value='" + tocText + "' />")
						.addClass("tocRepair");
			$(this).children("input").focus();
		});
$(".tocText input").live('blur', function() {
	var tocDiv = $(this).closest(".tocDiv");
	var remT = $(this).parent("td").parent("tr"), tText = $(this).val();
	tocEdit(remT, tText, tocDiv);
});
$(".tocText input").live('keyup', function(e) {
	var tocDiv = $(this).closest(".tocDiv");
	if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
		var remT = $(this).parent("td").parent("tr"), tText = $(this).val();
		tocEdit(remT, tText, tocDiv);
	}
});
$(".addEditToc").live(
		'click',
		function() {
			var remT = $(this).parent("tr");
			$(
					returnTocTr("<input type='text' value='' />",
							"tocRepair tocChild", $(this).closest(".tocDiv")))
					.insertAfter(remT);
			$(".tocChild").children("input").focus();
		});

function showNextPrev(pageNum, UPTOCPreview) {
	var prev = UPTOCPreview.find(".filePreviewPrevious");
	var next = UPTOCPreview.find(".filePreviewNext");
	if (parseInt(pageNum) > 1)
		prev.removeClass("nonner");
	else
		prev.addClass("nonner");
	if (parseInt(pageNum) < parseInt(UPTOCPreview.find(".docTotalPagesUP")
			.text()))
		next.removeClass("nonner");
	else
		next.addClass("nonner");
}
function pageChangeFunc(pageNum, UPTOCPreview) {
	var pageVar = UPTOCPreview.find("#UPPage" + pageNum);
	if (pageVar.attr("id") != undefined) {
		UPPageChange(pageNum, UPTOCPreview);
		pageVar.addClass("currentPageView");
		showNextPrev(pageNum, UPTOCPreview);
	} else
		$
				.get(
						"/uicomdocuments/getPage",
						{
							docId : UPTOCPreview.data("docId"),
							pageNumber : pageNum
						},
						function(data) {
							UPPageChange(pageNum, UPTOCPreview);
							UPTOCPreview
									.find(".filePagesView")
									.append(
											"<img id='UPPage"
													+ pageNum
													+ "' class='currentPageView UPPage'\n\
             rel='"
													+ data.result.pageId
													+ "' src='"
													+ data.result.image
													+ "' alt=''/>");
							showNextPrev(pageNum, UPTOCPreview);
						});
}
function UPPageChange(pageNum, UPTOCPreview) {
	UPTOCPreview.find(".currentPageView").removeClass("currentPageView");
	UPTOCPreview.find(".docPageNumInputUP").val(pageNum);
	UPTOCPreview.closest(".uploadPageHolder").find(".uploadPageNo").html(
			"Pg" + pageNum);
}
function returnTocTr(tocText, className, tocDiv) {
	var HL = tocDiv.find(".headingLevel").text().substring(8);
	var trClass = 'deletexPa';
	var uploadPage = tocDiv.closest(".uploadPageHolder");
	var currentPageNo = uploadPage.find(".currentPageView").attr("rel");
	if (HL == 1)
		trClass = 'deletexPa boldy';
	return "<tr class='"
			+ trClass
			+ "'><td class='deletex'><img class='removeToc' src='/public/images/rem.png' alt='X'/>\n\
                </td><td class='tocText "
			+ className
			+ "' rel='"
			+ HL
			+ "' style='padding-left:"
			+ 10
			* (HL - 1)
			+ "px'>"
			+ tocText
			+ "</td>\n\
                <td class='pageNum' rel='"
			+ currentPageNo
			+ "'>"
			+ uploadPage.find(".uploadPageNo").html().substring(2)
			+ "</td>\n\
                <td class='addEditToc'><a>add next item</a></td></tr>";
}
function pageIndex(pNo, tText, tocDiv) {
	var j = 0;
	tocDiv.find(".tocTable tr").each(
			function() {
				if ($(this).children(".pageNum").html() == pNo) {
					j++;
					if ($(this).children(".tocText").text() == tText
							|| $(this).children(".tocText").children("input")
									.val() != undefined) {
						return false;
					}
				}
			});
	return j;
}
function tocEdit(remT, tText, tocDiv) {
	var uploadPage = tocDiv.closest(".uploadPageHolder");
	var pNo = remT.children("td.pageNum").html();
	var pageId = remT.children("td.pageNum").attr("rel");
	if (remT.children(".tocText").hasClass("tocChild")) {
		if (tText == '')
			remT.remove();
		else {
			$.post("/uicomdocuments/addToc", {
				docId : uploadPage.data("docId"),
				tocText : tText,
				headingLevel : uploadPage.find(".headingLevel").text()
						.substring(8),
				pageIdAssociatedTo : pageId,
				pageIdAssociatedTO : pageId
			}, function(data) {
				remT.children(".tocText").removeClass("tocChild").removeClass(
						"tocRepair").html(tText);
			});
		}
	} else {
		if (tText == '')
			tocRemove(remT, tocDiv);
		else
			$.post("/uicomdocuments/editToc", {
				docId : uploadPage.data("docId"),
				tocText : tText,
				indexOnPage : pageIndex(pNo, '', tocDiv),
				index : pNo,
				pageIdAssociatedTO : pageId,
				pageIdAssociatedTo : pageId
			}, function(data) {
				remT.children(".tocText").removeClass("tocRepair").html(tText);
			});
	}
}
function tocRemove(remT, tocDiv) {
	var pageId = remT.children("td.pageNum").attr("rel");
	var pNo = remT.children("td.pageNum").html(), tText = remT.children(
			"td.tocText").text();
	$.post("/uicomdocuments/removeToc", {
		docId : tocDiv.closest(".fillingTOCSection").data("docId"),
		indexOnPage : pageIndex(pNo, tText, tocDiv),
		index : remT.children("td.pageNum").html(),
		pageIdAssociatedTO : pageId,
		pageIdAssociatedTo : pageId
	}, function(data) {
		remT.remove();
	});
}

// pass phrase
$(document).on("click", ".showPPBlock", function() {
	var $this = $(this);
	$this.addClass("nonner");
	$this.closest("tr").siblings(".ppBlockTr").removeClass("nonner");
});
$(document).on("click", ".hidePPBlock", function() {
	var tr = $(this).closest(".ppBlockTr");
	tr.find("textarea").val("");
	tr.addClass("nonner");
	tr.siblings(".showPPBlockTr").find(".showPPBlock").removeClass("nonner");
});
$(document).on("click", ".generatePP", function() {
	var tr = $(this).closest(".ppBlockTr");
	tr.find("textarea").val(randomId(256));
});