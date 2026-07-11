var qrSdGroups = new function(){
	var parDiv;
	var CLICK = "click.qrSdGroups";
	this.init = function(){
		parDiv = $("#sdCardGroupsPage");
		parDiv.off(CLICK)
			.on(CLICK,".markSdGroupOpen",markSdGroupChanged)
			.on(CLICK,".showSdCardCostPopup",showSdCardCostPopupClicked)
			.on(CLICK,".deleteSdCardGroupButton", deleteSdCardGroupButton)
	};

	var deleteSdCardGroupButton = function () {
		var $this = $(this);
		var params = {
			"sdCardGroupId" : $this.data("id")
		};
		showVYesNoBox("Are you sure to remove ?", null, function(state) {
            if (state) {
 				deleteSdCardGroup();
            }
        });
     	var deleteSdCardGroup = function(){
			vReq.post("/QrExports/deleteSdCardGroup",params,function(data){
				if(data.errorCode != ""){
				   showcmdsError(data.errorCode);
				}else{
					refreshPage();
					showMessage("Successfully deleted");
				}
			});
     	}   
	};

	var markSdGroupChanged = function(){
		var $this = $(this);
		if($this.is(":checked")){
			showSdCardCostPopup($this);
		}else{
			markSdGroup($this.closest(".sdgEachGroup"),"CLOSED");
		}
	};
	var showSdCardCostPopupClicked = function(){
		var $this = $(this);
		var chkbox = $this.siblings(".markSdGroupOpen");
		chkbox.prop("checked",true);
		var popup = showSdCardCostPopup($this);
		if($this.data("price") && $this.data("currency")){
			popup.find(".priceSdCardGroup").val($this.data("price"));
			popup.find(".chooseCurrencySignup").val($this.data("currency")+"#"+$this.data("currencySymbol"));
		}
	};
	var showSdCardCostPopup = function($this){
		var popup = showVPopup();
		popup.html(parDiv.find("#sdCardGroupUiSamples").html());
		popup.off(CLICK);
		popup.on(CLICK,".submitSdCardPrice",function(){
			var currency = popup.find(".chooseCurrencySignup").val();
			currency = currency.split("#");
			var price = popup.find(".priceSdCardGroup").val();
			var costRate = {
				value : price * 100,
				currencyCode : currency[0],
				currencySymbol : currency[1]
			};
			var sdGroupDiv = $this.closest(".sdgEachGroup");
			markSdGroup(sdGroupDiv,"OPEN",costRate);
		});
		return popup;
	};
	var markSdGroup = function(div,state,costRate){
		showTopLoader();
		var params = {
			state : state,
			groupId : div.data("id")
		};
		if(costRate){
			params.costRate = costRate;
		}
		vReq.post("/QrExports/markSdGroup",params,function(data){
			if(data && data.errorCode == ""){
				closeVPopup();
				var showSdCardCostPopup = div.find(".showSdCardCostPopup");
				if(costRate && state == "OPEN"){
					showSdCardCostPopup.text(costRate.currencyCode+"("+costRate.currencySymbol+") "+costRate.value/100)
						.data("price",(costRate.value/100))
						.data("currency",costRate.currencyCode)
						.data("currencySymbol",costRate.currencySymbol)
				}else{
					showSdCardCostPopup
						.text(i18nJS("SDCARD_SPECIFY_PRICE"))
						.data("price","")
						.data("currency","")
						.data("currencySymbol","")
				}
			}
		});
	};
};
