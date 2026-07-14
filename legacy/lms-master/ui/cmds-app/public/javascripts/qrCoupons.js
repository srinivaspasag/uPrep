var qrCoupons = new function(){
	var parDiv;
	var CLICK = "click.qrCoupons";
	var CHANGE = "change.qrCoupons";
	var SIZE = 20;
	var pageId;
	var urlList = {
		"active":{
			fetch : "/QrCoupons/fetchActive"
		},
		"all" : {
			fetch : "/QrCoupons/fetchAll"
		}
	};
	var fetchParams = {
		start : 0,
		size : SIZE,
		sortOrder: "DESC",
		orderBy : "timeCreated"
	};

	this.init = function(parDivId,pageIdIn){
		console.log("QrCoupons js loaded");
		parDiv = $(parDivId); pageId = pageIdIn;
		parDiv.off(CLICK).off(CHANGE)
			.on(CLICK,".showNewCouponPopup",addEditCouponPopup)
			.on(CLICK,".editCouponBtn", addEditCouponPopup)
			.on(CLICK,".deleteCouponBtn", deleteCoupon)
		resetFetchParams();
		fetch();
	};

	var resetFetchParams = function(){
		fetchParams = {
			start : 0,
			size : SIZE,
			sortOrder: "DESC",
			orderBy : "timeCreated"
		};
	};

	var loadMoreItems = function(){
		var $this = $(this);
		var nextStart = $this.data("nextStart");
		fetch(nextStart);
	};

	var fetch = function(start){
		var table = parDiv.find(".qrCouponsTable");
		var tr = table.find("tr:not(.qrCouponsTableHead)");
		showTopLoader();
		fetchParams.start = start = start ? start : 0;
		var url = urlList[pageId].fetch;
		vReq.get(url,fetchParams,function(data){
			if(start == 0){
				tr.remove();
			}else{
				table.find(".loadMoreDivTR").remove();
			}
			table.append(data);
			table.updateClientTime();
			condenseText($(".qrInvItemDesc"),90);
		});
	};

	var addEditCouponPopup = function() {
		var $this = $(this);
		var code = $this.data("code")
		console.log("Code is " + code);
		var params = {};
		if(code) {
			params = {code: $this.data("code") };
		}
		showTopLoader();
		vReq.get("/QrCoupons/addEditCouponPopup", params, function(data){
			var popup = showVPopup(0.7);
			popup.html(data);
			popup = popup.find(".newCouponPopup");
			showHideCouponFormFields(popup);
			popup.off(CLICK).off(CHANGE).off("keydown")
				.on(CLICK,".submitNewCoupon",submitAddUpdateCoupon)
				.on(CHANGE, "#couponTypeSelect",couponTypeChanged)
		});
	};

	var couponTypeChanged = function() {
		var $this = $(this);
		var popup = $this.closest(".newCouponPopup");
		showHideCouponFormFields(popup);
	};

	var showHideCouponFormFields = function(popup) {
		popup = $(popup);
		var couponType = popup.find("#couponTypeSelect").val();
		if (couponType === "PERCENTAGE") {
			popup.find("#discountValueRow").addClass("nonner");
			popup.find("#discountPercentageRow").removeClass("nonner");
			popup.find("#maxDiscountRow").removeClass("nonner");
		} else if (couponType === "FLAT") {
			popup.find("#discountPercentageRow").addClass("nonner");
			popup.find("#maxDiscountRow").addClass("nonner");
			popup.find("#discountValueRow").removeClass("nonner");
		}
	};

	var submitAddUpdateCoupon = function() {
		var $this = $(this);
		var action = $this.data("action");
		console.log("Action is: " + action);
		if($this.hasClass("disableBtn")){ return false; }
		$this.addClass("disableBtn");
		var popup = $this.closest(".newCouponPopup");
		var code = popup.find("#couponCodeInput").val();
		if (!code) {
			code = "";
		}
		console.log("Code:" +code);
		var couponType = popup.find("#couponTypeSelect").val();
		var params = {
			code: code,
			couponType: couponType,
		}
		if (couponType === "FLAT") {
			params.discountValue = popup.find("#discountValueInput").val() * 100;
		} else if (couponType === "PERCENTAGE") {
			params.discountPercentage = popup.find("#discountPercentageInput").val();
			if (popup.find("#maxDiscountInput").val() != "") {
				params.maxDiscount = popup.find("#maxDiscountInput").val() * 100;
			}
		}
		params.maxUsageCount = popup.find("#maxUsageCount").val();
		if (popup.find("#minOrderValueInput").val() != "") {
			params.minPurchaseValue = popup.find("#minOrderValueInput").val() * 100;
		}
		var datevChooseDiv = popup.find(".datevChooseDiv");
		params.expiryTime = getDateMillisFromvChoose(datevChooseDiv);
		console.log("Params:" + params);
		showTopLoader();
		var url = "/QrCoupons/postNewCoupon";
		if (action == "EDIT") {
			url = "/QrCoupons/updateCoupon";
		}
		vReq.post(url,params,function(data){
			$this.removeClass("disableBtn");
			closeVPopup();
			location.reload(true);
		},function(){
			$this.removeClass("disableBtn");
		});
	};

	var deleteCoupon = function() {
		var $this = $(this);
		var confirmtxt = "<div>Are you sure to <b>delete</b> this coupon? This action can not be un-done!</div>";
		showVYesNoBox(confirmtxt, null, function(state) {
			if (state) {
				doDelete();
			}
		});

		function doDelete() {
			var code = $this.data("code")
			var params = {code: $this.data("code") };
			showTopLoader();
			vReq.post("/QrCoupons/deleteCoupon", params, function(data){
				if(data && data.result) {
					var couponRow = $this.closest(".eachCouponInfo");
					couponRow.addClass("nonner");
					showMessage("Successfully deleted coupon: " + code);
				} else {
					showError("Failed to delete coupon: " + code);
				}
			}, function() {
				showError("Unable to delete the coupon");
			});
		}
	};

};
