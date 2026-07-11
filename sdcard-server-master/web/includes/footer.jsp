
<div id="cmdsBlackOut"></div>
<div id="cmdsPopup">
	<div class="rightText closePopupDiv">
		<a class="closecmdsPopup closePopup closePopupImg">x</a>
	</div>
	<div class="cmdsPopupBody"></div>
	<span class="cmdsPopupError nonner">Fields marked with * are
		compulsory</span>
	<div class="nonner cmdsPopupBtns rightText"></div>
</div>
<div id="topLoaderDiv" class="relative" style="z-index: 10000">
	<div id="topLoader" class="nonner">
		<span class="loadingSpanTop"></span>
	</div>
</div>
<style type="text/css">
.vpopupWrapper {
	left: 0;
	position: absolute;
	right: 0;
	top: 0;
	z-index: 1200;
	display: table;
	height: 100% !important;
	table-layout: fixed;
	width: 100%;
	outline: none;
	background-color: rgba(0, 0, 0, .06);
	opacity: 0;
}

.vpopupHolder {
	display: table-cell;
	text-align: center;
	vertical-align: middle;
	width: 100%;
}

.vpopupBodyHolder {
	text-align: left;
	direction: ltr;
	display: inline-block;
	padding: 20px;
	background-color: #fff;
	border: 1px solid #ccc;
	box-shadow: 1px 1px 4px -1px #ddd;
}

.disableMainBodyScroll {
	position: fixed !important;
	width: 100%;
}

.crossVPopup.stl {
	float: right;
	cursor: pointer;
	color: #666;
	background-image: url('/public/images/LMS/inst-sprite.png');
	background-repeat: no-repeat;
	font-size: 1px;
	padding: 7px;
	background-position: -723px -4px;
}

.vpopupBody {
	min-width: 100px;
	min-height: 50px;
}

.vpopupMsgResp {
	padding: 10px 15px;
	border: 1px solid #eee;
	background-color: #f4f4f4;
	color: #999;
	font-size: 13px;
}

body .vpopupWrapper.nonner {
	display: none;
}

.vpopupWrapper.fullVPopup .vpopupBodyHolder {
	padding: 0px;
}

.vpopupWrapper.fullVPopup .crossVPopup.stl {
	display: none;
}

#vMessagePopup {
	background-color: #FFFFFF;
	border: 1px solid #AAAAAA;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
	left: 0;
	margin: 0 auto;
	position: absolute;
	right: 0;
	top: 120px;
	width: 400px;
	z-index: 3000;
	display: none;
}

.vMsgBoxBody {
	padding: 10px 20px;
}

.vMsgBoxHead {
	padding: 10px 20px;
	font-size: 20px;
	color: #F1F1F1;
	background-color: #666;
}

.vMsgBoxHead.boxTypeERROR {
	background-color: #F66663;
}

.vMsgBoxHead.boxTypeSUCCESS {
	background-color: rgba(6, 162, 240, 0.7);
}

.vMessageBody {
	padding: 15px 0 30px;
	color: #666;
	font-size: 15px;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>
<div class="vpopupWrapper nonner">
	<div class="vpopupHolder">
		<div class="vpopupBodyHolder">
			<span class="crossVPopup stl" title="close popup"></span> <span
				class="closeVPopup nonner"></span>
			<div class="vpopupBody"></div>
		</div>
	</div>
</div>
<div id="vMessagePopup">
	<div class="vMsgBoxHead boxTypeERROR">
		<span class="">Error</span>
	</div>
	<div class="vMsgBoxHead boxTypeSUCCESS">
		<span class="">Message</span>
	</div>
	<div class="vMsgBoxBody">
		<div class="vMessageBody"></div>
		<div class="margTop rightText vMsgBoxButtons">
			<div class="smallGreenButton closeVMessagePopup">Ok</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	var showVPopup = function(opacity, noExtHiding, fullPopup) {
		if (hideVPopupTimeout)
			clearTimeout(hideVPopupTimeout);
		var sTop = $(window).scrollTop();
		opacity = !opacity || opacity > 1 || opacity < 0.1 ? 0.2 : opacity;
		var popup = $(".vpopupWrapper").css("background-color",
				"rgba(0, 0, 0, " + opacity + ")");
		var fullWidth = $(window).innerWidth();
		fullWidth = fullWidth < window.screen.width ? window.screen.width
				: fullWidth;
		fullWidth -= 10;
		popup.removeClass("nonner").css("min-width", fullWidth + "px").fadeTo(
				300, 1, function() {
					$(this).css("display", "table");
				});
		$("#contentSectionHolder").addClass("disableMainBodyScroll").css({
			'top' : "-" + sTop + 'px'
		}).data('scrollTop', sTop);
		if (noExtHiding) {
			popup.off("click", decHidePopup);
		} else {
			popup.off("click", decHidePopup);
			popup.on("click", decHidePopup);
		}
		if (fullPopup) {
			popup.addClass("fullVPopup");
		} else {
			popup.removeClass("fullVPopup");
		}
		$(document).off("keyup", keyupVPopup).on("keyup", keyupVPopup);
		var popupBody = popup.find(".vpopupBody");
		popupBody.off("close.popup");
		return popupBody;
	};
	var keyupVPopup = function(e) {
		var key = e.which;
		if (key == 27) {
			hideVPopup();
		}
	}
	var hideVPopup = function() {
		if (hideVPopupTimeout)
			clearTimeout(hideVPopupTimeout);
		$(".vpopupWrapper").fadeTo(
				50,
				0,
				function() {
					var popupBody = $(this).addClass("nonner").hide().find(
							".vpopupBody").html("");
					closeVPopupCB(popupBody);
				});
		var sTop = $("#contentSectionHolder").removeClass(
				"disableMainBodyScroll").css({
			'top' : ''
		}).data('scrollTop');
		$(document).off("keyup", keyupVPopup);
		window.scrollTo(0, sTop);
	};
	var closeVPopup = function() {
		if (hideVPopupTimeout)
			clearTimeout(hideVPopupTimeout);
		var popup = $(".vpopupWrapper");
		if (!popup.hasClass("nonner")) {
			var sTop = $("#contentSectionHolder").removeClass(
					"disableMainBodyScroll").css({
				'top' : ''
			}).data('scrollTop');
			$(document).off("keyup", keyupVPopup);
			window.scrollTo(0, sTop);
			var popupBody = popup.fadeTo(0, 0).addClass("nonner").hide().find(
					".vpopupBody").html("");
			closeVPopupCB(popupBody);
		}
	};
	var closeVPopupCB = function(popupBody) {
		popupBody = popupBody ? popupBody : $(".vpopupWrapper").find(
				".vpopupBody");
		popupBody.trigger("close.popup");
		popupBody.off("close.popup");
	};
	var decHidePopup = function(e) {
		var $this = $(e.srcElement || e.target);
		var has = $(".vpopupBodyHolder").has($this);
		if (!has.get(0) && !$this.hasClass("vpopupBodyHolder")) {
			hideVPopup();
		}
	};
	var hideVPopupTimeout;
	var showVPopupMsg = function(msg, closeTime) {
		var popup = showVPopup(null, true);
		var htmlData = "<div class='vpopupMsgResp'>" + msg + "</div>";
		popup.html(htmlData);
		if (hideVPopupTimeout)
			clearTimeout(hideVPopupTimeout);
		closeTime = closeTime ? closeTime : 5000;
		if (closeTime >= 0) {
			hidePopupTimeout = setTimeout(function() {
				hideVPopup();
			}, closeTime);
		}
	};
	var cbFn = function() {
	};
	var showVYesNoBox = function(text, extClasses, cbFunction) {
		var popup = showVMsgBox(text, "No", "SUCCESS", cbFunction);
		var buttons = "<div class='smallGreenButton closeVMessagePopup "+extClasses+"' data-val='true'>Yes</div>";
		buttons += "<div class='smallRedButton closeVMessagePopup' style='margin-left:10px;' data-val='false'>No</div>";
		popup.find(".vMsgBoxButtons").html(buttons);
	};
	var showVMsgBox = function(err, cancelText, typeOfBox, cbFunction) {
		cancelText = cancelText || "Ok";
		typeOfBox = typeOfBox ? typeOfBox : "ERROR";
		err = err || COMMON_ERROR_MESSAGE;
		var popup = $("#vMessagePopup").show().css("top",
				(150 + $(window).scrollTop()));
		;
		popup.find(".vMsgBoxHead").addClass("nonner");
		popup.find(".boxType" + typeOfBox).removeClass("nonner");
		var blackOutDiv = getBlackOut().show();
		blackOutDiv.css("z-index", popup.css("z-index") - 1);
		popup.find(".vMessageBody").html(err);
		popup.find(".vMsgBoxButtons").html(
				"<div class='smallGreenButton closeVMessagePopup'>Ok</div>");
		popup.find(".closeVMessagePopup").text(cancelText);
		$(document).off("keyup.cancelMsgBox", keyupVMsgBox).on(
				"keyup.cancelMsgBox", keyupVMsgBox);
		cbFn = cbFunction ? cbFunction : function() {
		};
		return popup;
	};
	var hideVError = function() {
		hideVMsgBox();
		if (cbFn) {
			var $this = $(this);
			var arg = $this.data("val");
			arg = arg == "true" ? true : arg;
			try {
				cbFn(arg);
			} catch (err) {
				putConsoleError(err);
			}
			cbFn = function() {
			};
		}
	};
	var hideVMsgBox = function(dontHideBlackOut) {
		$("#vMessagePopup").hide();
		$(document).off("keyup.cancelMsgBox", keyupVMsgBox);
		if (!dontHideBlackOut) {
			var blackOutDiv = getBlackOut().hide();
		}
	};
	var keyupVMsgBox = function(e) {
		var key = e.which;
		if (key == 13 || key == 27) {
			hideVMsgBox();
		}
	}
	var getBlackOut = function() {
		var blackOutDiv = $("#errorPopupBlackOut");
		if (!blackOutDiv.get(0)) {
			blackOutDiv = $("#cmdsBlackOut");
		}
		return blackOutDiv;
	};
	$(function() {
		$(".vpopupWrapper").on("click", decHidePopup);
		$(document).on("click", ".crossVPopup", hideVPopup).on("click",
				".closeVPopup", closeVPopup).on("click", ".closeVMessagePopup",
				hideVError);
	});
</script>

