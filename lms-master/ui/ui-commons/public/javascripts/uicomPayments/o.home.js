var makePayments = new function(){
	var url = "/UIComPayments/startTransactionUser";
	var loadingDiv = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>"
	var CLICK = "click.payments";
	var makePaymentsOrgId = null;
	this.init = function(itemType,itemId,itemName,packageOrgId,packageDays,callBackUrl,customParams,cbFn){
		var sku = "";
		callBackUrl = location.origin + callBackUrl;
		if(customParams){
		   try{
			if(typeof customParams == "object"){
				sku = encodeSKUObj(customParams);
			}else if(typeof customParams == "string"){
				sku = encodeURIComponent(escape(customParams));
			}
		   }catch(err){ sku = "";}
		}
		var params = {
			"item.id": itemId,
			"item.type" : itemType,
			itemName : itemName,
			packageDays : packageDays,
			packageOrgId : packageOrgId,
			itemSKU : sku
		}
		$.post(url,params,function(data){
			if(data && data.errorCode=="" && data.result){
				params.orderId = data.result.orderId;
				$.get("/Institute/getWalletBalance",params,function(lpbalancedata){
					var lpBalance = lpbalancedata.result.maxRewardPointsToRedeem;
					showConfirmPopup(data.result,sku,callBackUrl,itemName,cbFn,lpBalance);
				});
			}else{
				if(cbFn){
					cbFn(false);
				}
			}
		});
	};

	this.setOrgId = function(orgId) {
		makePaymentsOrgId = orgId;
	};

	var showConfirmPopup = function(res,sku,callBackUrl,itemName,cbFn,lpBalance){
		var redirectUrl = res.redirectUrl;
		redirectUrl += "?item_sku="+sku+"&callbackUrl="+callBackUrl;
		redirectUrl += "&transactionId="+res.transactionId+"&userId="+res.userId;
		redirectUrl += "&paymentChannel="+res.paymentChannel;
		console.log(redirectUrl);
		closeVPopup();
		var popup = showVPopup(0.7,true,true);
		popup.html(loadingDiv);
		var userEmail = res.email?res.email:"";
		var userPhone = res.phone ? res.phone : "";
		var orderId = res.orderId;
		var paymentChannel = res.paymentChannel? res.paymentChannel : "EBS";
		var params = {
			itemName : itemName,
			orderId : orderId,
			orderTotal : res.orderTotal,
			userEmail : userEmail,
			userPhone : userPhone,
			lpCredits  : lpBalance
		}
		originalOrderTotal = res.orderTotal;
		$.get("/UIComPayments/showTransactionConfirmPopup",params,function(data){
			popup.html(data);
			popup.off(CLICK).on(CLICK,".redirectForTransaction",function(){
				$(this).attr("disabled","true");
				$(this).addClass("disabled");
				var emailInput = popup.find("#paymentContactEmail");
				var email = emailInput.val();

				var phoneInput = popup.find("#paymentContactPhone");
				var phone = phoneInput.val();
				var buyer_name = $("#fullName").val();
				var orgId = $("#loginOrgId").val();
				var pattern = new RegExp("^[6-9][0-9]{9}$");
				if(email && validateEmail(email)){
					if(phone && pattern.test(phone)){
						window.top.location = redirectUrl+"&email="+email+"&phone="+phone+"&buyer_name="+buyer_name+"&orgId="+orgId+"&orderId="+res.orderId;
					}
					else{
						showVMsgBox("Not a valid phone number, please re-enter!","OK","ERROR",function(){
							phoneInput.focus();
						});
					}
				}else{
				   showVMsgBox("Not a valid email-id, please re-enter!","OK","ERROR",function(){
					emailInput.focus();
				   });
				}
			});
			popup.on(CLICK, ".applyCouponButton", function() {
				var $this = $(this);
				if ($this.hasClass('btnDisabled')) {return;}
				var couponCode = popup.find("#couponCodeOrder").val();
				var couponParams = {
					couponCode : couponCode,
					orderId : orderId,
					userEmail : userEmail,
				}
				if (makePaymentsOrgId) {
					couponParams.orgId = makePaymentsOrgId;
				}
				validateAndApplyCoupon(couponParams, popup);
			});
			popup.on(CLICK, ".lpBalance", function() {
				if($(this).is(":checked")){
					$('#couponCodeOrder').attr("disabled","disabled");
					$('.applyCouponButton').addClass('btnDisabled');
					var walletParams = {
						lpCredits : lpBalance,
						orderId : orderId,
						userEmail : userEmail,
					}
					if (makePaymentsOrgId) {
						walletParams.orgId = makePaymentsOrgId;
					}
					validateAndApplyWalletBalance(walletParams, popup);
	            }
	            else if($(this).is(":not(:checked)")){
					$('#couponCodeOrder').removeAttr('disabled');
					$('.applyCouponButton').removeClass('btnDisabled');
					var walletParams = {
						lpCredits : lpBalance,
						orderId : orderId,
						userEmail : userEmail,
					}
					if (makePaymentsOrgId) {
						walletParams.orgId = makePaymentsOrgId;
					}
					validateAndRemoveWalletBalance(walletParams, popup);
	            }
			});
			if(cbFn){
				cbFn(true);
			}
		});
	};

	var validateAndRemoveWalletBalance =  function(params, popup) {
		$.post("/UIComPayments/validateAndRemoveWalletBalance", params, function(data){
			var messageSpan = popup.find("#lpCreditsMessage");
			var popupOrderTotal = popup.find("#popupOrderTotal");
			var popupBalance = popup.find("#balance");
			if (data.result.lpCredits == 0) {
				popupBalance.text(data.result.lpCredits);
				popupOrderTotal.text(data.result.discountedAmount);
			} else if(data && data.errorCode == "") {
				popupOrderTotal.text(data.result.discountedAmount);
			} else {
				var errorMessage = data.errorMessage;
				popupOrderTotal.text(originalOrderTotal);
			}
			messageSpan.removeClass("errorTextColor");
			messageSpan.removeClass("successTextColor");
			var messageRow = popup.find("#lpCreditsMessageRow");
			messageRow.hide();
		});
	};

	var validateAndApplyWalletBalance =  function(params, popup) {
		$.post("/UIComPayments/validateAndApplyWalletBalance", params, function(data){
			var messageSpan = popup.find("#lpCreditsMessage");
			var popupOrderTotal = popup.find("#popupOrderTotal");
			var popupBalance = popup.find("#balance");
			if (data.result.lpCredits == 0) {
				popupBalance.text(data.result.lpCredits);
				popupOrderTotal.text(data.result.discountedAmount);
				messageSpan.text("Sorry! You don't have enough balance to redeem.");
				messageSpan.removeClass("successTextColor");
				messageSpan.addClass("errorTextColor");
			} else if(data && data.errorCode == "") {
				popupBalance.text(data.result.lpCredits);
				popupOrderTotal.text(data.result.discountedAmount);
				messageSpan.text("Congratulations! Your LP Credits have been successfully applied. ");
				messageSpan.removeClass("errorTextColor");
				messageSpan.addClass("successTextColor");
			} else {
				var errorMessage = data.errorMessage;
				popupOrderTotal.text(originalOrderTotal);
				messageSpan.text(errorMessage);
				messageSpan.removeClass("successTextColor");
				messageSpan.addClass("errorTextColor");
			}
			var messageRow = popup.find("#lpCreditsMessageRow");
			messageRow.show();
		});
	};

	var validateAndApplyCoupon =  function(params, popup) {
		$.post("/UIComPayments/validateAndApplyCoupon", params, function(data){
			var messageSpan = popup.find("#couponMessage");
			var popupOrderTotal = popup.find("#popupOrderTotal");
			if(data && data.errorCode == "") {
				var discount = data.result.discount / 100;
				popupOrderTotal.text(data.result.discountedAmount);
				messageSpan.text("Congratulations! You've received discount of Rs. " + discount);
				messageSpan.removeClass("errorTextColor");
				messageSpan.addClass("successTextColor");
				document.getElementById("lpBalance").disabled = true;
			} else {
				var errorMessage = data.errorMessage;
				popupOrderTotal.text(originalOrderTotal);
				messageSpan.text("Sorry! " + errorMessage);
				messageSpan.removeClass("successTextColor");
				messageSpan.addClass("errorTextColor");
				document.getElementById("lpBalance").disabled = false;
			}
			var messageRow = popup.find("#couponMessageRow");
			messageRow.removeClass("nonner");
		});
	};

	var skuSplitChar = '#';
	var skuValSplitChar = '_';
	var encodeSKUObj = function(skuOBJ){
		var skuSTR = "";
		for(key in skuOBJ){
			if(skuSTR){
				skuSTR += skuSplitChar;
			}
			var each = key + skuValSplitChar +skuOBJ[key];
			skuSTR += each;
		}
		skuSTR = encodeURIComponent(skuSTR);
		return skuSTR;
	};
	var decodeSKUStr = function(skuSTR){
		skuSTR = decodeURIComponent(skuSTR);
		var skuOBJ = {};
		if(skuSTR && skuSTR.indexOf(skuSplitChar)>0){
			var skuArr1 = skuSTR.split(skuSplitChar);
			for(index in skuArr1){
				var each = skuArr1[index];
				var valSplit = each.split(skuValSplitChar);
				var key = valSplit[0];
				var val = valSplit[1];
				skuOBJ[key] = val;
			}
		}
		return skuOBJ;
	};
	this.formatSKU = decodeSKUStr;
	/*this.formatSKU = function(sku){
		if(sku){
			sku = decodeURIComponent(sku);
			try{
				sku = JSON.parse(sku);
			}catch(err){}
		}
		return sku;
	};*/
};

function validateEmail(s){
 if (s.length >0) { 
  var i=s.indexOf("@"),j=s.indexOf(".",i), k=s.indexOf(","), kk=s.indexOf(" "), jj=s.lastIndexOf(".")+1,
   len=s.length,plus=s.indexOf("+");
  if ((i>0) && (j>(i+1)) && (k==-1) && (kk==-1) && (plus==-1) &&(len-jj >=2) && (len-jj<=4)) {
            return true;
  }     
  else {
    return false;
  }

 }
 else return false;
 };
