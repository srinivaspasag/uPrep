var qrInventory = new function(){
	var parDiv;
	var CLICK = "click.qrInventory";
	var CHANGE = "change.qrInventory";
	var SIZE = 20;
	var pageId;
	var urlList = {
		"accessCodes":{
			fetch : "/QrInventory/fetchInventories"
		},
		"shipments" : {
			fetch : "/QrInventory/fetchShipments"
		}
	};
	var fetchParams = {
		start : 0,
		size : SIZE,
		sortOrder: "DESC",
		orderBy : "timeCreated"
	};
	this.init = function(parDivId,pageIdIn){
		parDiv = $(parDivId); pageId = pageIdIn;
		parDiv.off(CLICK).off(CHANGE)
			.on(CLICK,".showNewInventoryPopup",newInventoryPopup)
			.on(CLICK,".createBulkAccessCodesPopup",createBulkAccessCodesPopup)
			.on(CLICK,".searchInventoryTable",searchInventoryShow)
			.on(CLICK,".searchInventoryNow",searchInventoryNow)
			.on(CLICK,".viewShipAddress",viewShipAddressPopup)
			.on(CLICK,".resendAccessCodeEmail",resendAccessCodeEmail)
			.on(CLICK,".allowedDevicesPopup",showAllowedDevicesPopup)
			.on(CLICK,".loadMoreItems",loadMoreItems)
			.on(CHANGE,".invShipmentState",invShipmentStateChanged)
		resetFetchParams();
		fetch();
	};
	var showAllowedDevicesPopup = function(){
		var $this = $(this);
		var data = $this.data("devices");
		var HTML = "<div class='allowedDevicesPopup'>%content</div>";
		var title = i18nJS("ALLOWED_DEVICE_ID");
		HTML = HTML.replace("%content","<div class='big16 boldy centerText'>"+title+"</div>%content");
		var content = "<div style='padding:12px 5px;'><div class='allowedDevicesHolder'>";
		for(var i=0;i<data.length;i++){
			var div = "<div class='eachDeviceId' data-value='"+data[i]+"'>"+data[i];
			div+="<a class='right removeDeviceId'>"+i18nJS("TXT_REMOVE")+"</a></div>";
			content += div;
		}
		content += "</div>";
		content += "<div class='eachDeviceId'><input type='text' class='addDeviceIdInput'/>"
		content += "<a class='addDeviceId right'>"+i18nJS("TXT_ADD")+"</a></div>";
		content += "</div>";
		content += "<div class='centerText'><span class='smallBlueButton closeVPopup'>"+i18nJS("TXT_DONE")+"</span></div>";
		HTML = HTML.replace("%content",content);
		var popup = showVPopup(0.7);
		popup.html(HTML);
		popup = popup.find(".allowedDevicesPopup");
		popup.on(CLICK,".addDeviceId",addNewDeviceId)
			.on(CLICK,".removeDeviceId",removeDeviceId);
		var tr = $this.closest(".eachShipmentInfo");
		popup.data("accessCodeId",tr.data("id"));
		popup.data("tr",tr);
	};
	var addNewDeviceId = function(){
		var $this = $(this);
		var input = $this.siblings(".addDeviceIdInput");
		var val = $.trim(input.val());
		if(!val){
                        showError(i18nJS("BLANK_CONTANT_NOT_ALLOWED"),function(){
                                input.focus();
                        });
                        return;
		}
		var regExp = new RegExp("^[a-zA-Z0-9:]*$");
		if(!regExp.test(val)){
                        showError(i18nJS("ALPHANUMERIC_WRONG_FORMAT"),function(){
                                input.focus();
                        });
                        return;
                }
		var devices = $this.closest(".allowedDevicesPopup").data("tr").find(".allowedDevicesPopup").data("devices");
		if(devices.indexOf(val)>=0){
                        showError(i18nJS("DUPLICATE_VALUE_FOUND"),function(){
                                input.focus();
                        });
                        return;
		}
		addRemoveDeviceId($this,val,"ADD",function(data,popup){
			var div = "<div class='eachDeviceId'>"+val;
			div+="<a class='right removeDeviceId'>"+i18nJS("TXT_REMOVE")+"</a></div>";
			popup.find(".allowedDevicesHolder").append(div);
			input.val("");
			var tr = popup.data("tr");
			var deviceIdsCountElem = tr.find(".deviceIdsCount");
			increaseCount(deviceIdsCountElem);
			var allowedDevicesPopup = tr.find(".allowedDevicesPopup");
			var devices = allowedDevicesPopup.data("devices");
			devices.push(val);
		});
	};
	var removeDeviceId = function(){
		var $this = $(this);
		var holder = $this.closest(".eachDeviceId");
		var val = holder.data("value");
		showVYesNoBox(i18nJS("CONFIRM_DELETE_POPUP_TXT"),null,function(state){
			if(!state){ return }
			addRemoveDeviceId($this,val,"REMOVE",function(data,popup){
				holder.remove();
				var tr = popup.data("tr");
				var deviceIdsCountElem = tr.find(".deviceIdsCount");
				decreaseCount(deviceIdsCountElem);
				var allowedDevicesPopup = tr.find(".allowedDevicesPopup");
				var devices = allowedDevicesPopup.data("devices");
				removeItemFromArr(devices,val);
			});
		});
	};
	var addRemoveDeviceId = function($this,val,optType,cbFn){
		var popup = $this.closest(".allowedDevicesPopup");
		showTopLoader();
		var params = {
			deviceIds : [val],
			accessCodeId : popup.data("accessCodeId"),
			operation : optType
		};
		vReq.post("/QrInventory/addRemoveDeviceId",params,function(data){
			cbFn(data,popup);
		});
	};
	var searchInventoryNow = function(){
		var tr = $(this).closest("tr");
		tr.find(".searchInventoryField").each(function(){
			var $this = $(this);
			var val = $.trim($this.val());
			if(val){
				fetchParams[$this.data("field")] = val;
				fetchParams["customSearch"] = true;
			}else{
				$(fetchParams).removeProp($this.data("field"));
			}
		});
		fetch();
	};
	var searchInventoryShow = function(){
		var tr = parDiv.find(".qrInventorySearchTR").toggleClass("nonner");
		tr.find("input,select").val("");
		if(tr.hasClass("nonner") && fetchParams["customSearch"]){
			resetFetchParams();
			fetch();
		}
	};
	var resendAccessCodeEmail = function(){
		var $this = $(this);
		var par = $this.closest(".eachShipmentInfo");
		var email = $this.data("email");
		showTopLoader();
		var params = {
			accessCodeId : par.data("id")
		}
		vReq.post("/QrInventory/resendAccessCodeEmail",params,function(data){
			showMessage(i18nJS("ACCESS_CODE_EMAIL_RESEND",email));
		});
	};
	var invShipmentStateChanged = function(){
		var $this = $(this);
		var val = $this.val();
		var oldVal = $this.data("oldVal");
		showTopLoader();
		var params = {
			accessCodeId : $this.closest(".eachShipmentInfo").data("id"),
			shipmentStatus : val
		};
		vReq.post("/QrInventory/updateShipmentStatus",params,function(data){
			$this.removeClass("stateNOT_DISPATCHED stateDISPATCHED stateRECEIVED");
			$this.addClass("state"+val);
			$this.data("oldVal",oldVal);
		},function(){
			$this.val(oldVal);
		});
	};
	var viewShipAddressPopup = function(){
		var $this = $(this);
		var dataHTML = $this.siblings(".shipmentAddressValue");
		var popup = showVPopup(0.7);
		popup.html(dataHTML.clone());
		popup.find(".shipmentAddressValue").removeClass("nonner");
	};
	var loadMoreItems = function(){
		var $this = $(this);
		var nextStart = $this.data("nextStart");
		fetch(nextStart);
	};
	var resetFetchParams = function(){
		fetchParams = {
			start : 0,
			size : SIZE,
			sortOrder: "DESC",
			orderBy : "timeCreated"
		};
	};
	var fetch = function(start){
		var table = parDiv.find(".qrInventoryTable");
		var tr = table.find("tr:not(.qrInventoryTableHead)");
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
	var newInventoryPopup = function(){
		showTopLoader();
		vReq.get("/QrInventory/createInventoryPopup",{},function(data){
			var popup = showVPopup(0.7);
			popup.html(data);
			popup = popup.find(".newInventoryPopup");
			popup.off(CLICK).off(CHANGE).off("keydown")
				.on(CLICK,".submitNewInventory",submitNewInventory)
				.on(CLICK,".shipToSameChkBox",shipToSameChanged)
				.on(CHANGE,".chooseSellableEntityType",sellableEntityTypeChanged)
				.on("keydown",".chooseSellableEntityId",chooseSellableEntityId)
				.on("blur",".chooseSellableEntityId",hideDataList)
				.on(CLICK,".cmdsEntityListItem",datalistSelected)
				.on(CLICK,".addNewInvItem",addNewInvItem)
		});
	};

	var createBulkAccessCodesPopup = function(){
		showTopLoader();
		vReq.get("/QrInventory/createBulkAccessCodesPopup",{},function(data){
			var popup = showVPopup(0.7);
			popup.html(data);
			popup = popup.find(".createBulkAccessCodesPopup");
			popup.off(CLICK).off(CHANGE).off("keydown")
				.on(CLICK,".submitBulkAccessCodes",submitBulkAccessCodes)
				.on(CHANGE,".chooseSellableEntityType",sellableEntityTypeChanged)
				.on("keydown",".chooseSellableEntityId",chooseSellableEntityId)
				.on("blur",".chooseSellableEntityId",hideDataList)
				.on(CLICK,".cmdsEntityListItem",datalistSelected)
				.on(CLICK,".addNewInvItem",addNewInvItem)
		});
	};

	var addNewInvItem = function(){
		var $this = $(this);
		var item = $this.closest(".chooseSellableItem");
		var newItem = item.clone(true);
		item.after(newItem);
		newItem.find("input").val("").removeData();
		setTimeout(function(){
			$this.hide();
		},100);
	};
	var hideDataListTimeout;
	var datalistSelected = function(){
		var $this = $(this);
		var id = $this.data("id");
		var name = $this.text();
		var input = $this.closest(".cmdsEntityList").siblings(".chooseSellableEntityId");
		input.data("id",id).val(name);
		$this.addClass("selected").siblings().removeClass("selected");
		return hideDataList();
	};
	var hideDataList = function(){
		if(hideDataListTimeout) clearTimeout(hideDataListTimeout);
		hideDataListTimeout = setTimeout(function(){
			$(".cmdsEntityList").addClass("nonner");
		},500);
		return false;
	};
	var searchTimeoutObj;	
	var searchXHR;
	var cmdsEntityListItem = "<div class='cmdsEntityListItem' data-id='$ID'>$TEXT</div>";
	var chooseSellableEntityId = function(){
		var $this = $(this);
		if(searchTimeoutObj) clearTimeout(searchTimeoutObj);
		searchTimeoutObj = setTimeout(function(){
			var val = $this.val();
			if(val.length>1){
				var type = $this.siblings(".chooseSellableEntityType").val();
				var datalist = $this.siblings(".cmdsEntityList");
				var params = {
					type : type,
					name : val,
					start : 0,
					size : 5,
					accessScope : "OPEN"
				};
				if(searchXHR){searchXHR.abort(); searchXHR = null;}
				searchXHR = $.get("/QrInventory/searchSellableEntity",params,function(data){
					if(data && data.errorCode == "" && data.result.sellableItems
						&& data.result.sellableItems.list.length>0){
						var datalistItems = "";
						var list = data.result.sellableItems.list;
						for(var i=0;i<list.length;i++){
							var item = list[i].info;
							var itemElem = cmdsEntityListItem.replace("$TEXT",item.name);
							itemElem = itemElem.replace("$ID",item.id);
							datalistItems += itemElem;
						}
						datalist.html(datalistItems).removeClass("nonner");
					}
					searchXHR = null;
				});
			};
		},250);
	};
	var sellableEntityTypeChanged = function(){
		var $this = $(this);
		var popup = $this.closest(".newInventoryPopup");
		var val = $this.val();
		var opt = $this.find("option[value='"+val+"']");
		var isShipable = opt.data("shipable");
		$this.data("shipable",isShipable);
		
		popup.find(".chooseSellableEntityType").each(function(){
			var each = $(this);
			isShipable = isShipable || each.data("shipable");
		});
		var tr = popup.find(".inventoryStateTR");
		var choose = tr.find(".invShipmentState");
		if(!isShipable){
			tr.addClass("nonner");
			choose.data("ignore",true);
		}else{
			tr.removeClass("nonner");
			choose.data("ignore",false);
		};
		if(searchXHR){searchXHR.abort(); searchXHR = null;}
		$this.siblings(".chooseSellableEntityId").val("");
	};
	var shipToSameChanged = function(){
		var $this = $(this);
		var popup = $this.closest(".newInventoryPopup");
		var container = popup.find(".inventoryShipTo");
		if($this.is(":checked")){
			container.html("");
		}else{
			container.html(popup.find(".inventoryBillTo").html());
		};
	};
	var getAddressTo = function(holder){
		var obj = {
			name : holder.find(".billToName").val(),
			contactNo : holder.find(".billToPhone").val(),
			email : holder.find(".billToEmail").val(),
			address : holder.find(".billToAddress").val(),
			pinCode : holder.find(".billToPinCode").val(),
			location : {
				city : holder.find(".billToCity").val(),
				country : holder.find(".billToCountry").val()
			}
		};
		return obj;
	};
	var submitNewInventory = function(){
		var $this = $(this);
		if($this.hasClass("disableBtn")){ return false; }
		var popup = $this.closest(".newInventoryPopup");
		var emailInput = popup.find(".invEmailInput");
		var email = emailInput.val();
		// if(!validEmail(email)){
		// 	showError(i18nJS("NOT_A_VALID_EMAIL_ID"),function(){
		// 		emailInput.focus();
		// 	});
		// 	return;
		// };
		var entities = [];
		var entityInputHolder = popup.find(".chooseSellableItem");
		for(var index=0;index<entityInputHolder.length;index++){
			var each = $(entityInputHolder.get(index));
			var entityIdInput = each.find(".chooseSellableEntityId");
			var entityId = entityIdInput.data("id");
			if(!entityId){
				showError(i18nJS("CHOOSE_SEARCH_ENTITY"),function(){
					entityIdInput.focus();
				});
				return;
			}
			var entity = {
				type : each.find(".chooseSellableEntityType").val(),
				id : entityId
			};
			entities.push(entity);
		};
		var sellingPoint = popup.find(".invSellPoint").val();
		var sellingRef = popup.find(".invSellRefNo").val();
		if(!sellingPoint || !sellingRef){
			showError(i18nJS("PLEASE_PROVIDE_SELLING_POINT"));
			return;
		}
		var chooseShipState = popup.find(".invShipmentState");
		var billingAddress = getAddressTo(popup.find(".inventoryBillTo"));
		var shippingAddress = "";
		if(popup.find(".shipToSameChkBox").is(":checked")){
			shippingAddress = billingAddress;
		}else{
			shippingAddress = getAddressTo(popup.find(".inventoryShipTo"));
		}
		$this.addClass("disableBtn");
		var params = {
			buyerContactDetails : {
				email : email ? email :"" ,
				billingAddress : billingAddress, 
				shipmentAddress : shippingAddress
			},
			entities : entities,
			sellerInfo : {
				pointOfSale : sellingPoint,
				sellerReferenceNo : sellingRef,
			},
		};
		if(!chooseShipState.data("ignore")){
			params.shipmentStatus = chooseShipState.val();
		};
		var table = parDiv.find(".qrInventoryTable");
		showTopLoader();
		vReq.post("/QrInventory/postNewEntry",params,function(data){
			$this.removeClass("disableBtn");
			closeVPopup();
			table.find(".noRecordTR").remove();
			table.find(".qrInventoryTableHead:last").after(data);
			table.updateClientTime();
			showMessage(i18nJS("ACCESS_CODE_EMAIL_RESEND",email));
		},function(){
			$this.removeClass("disableBtn");
		});
	};
	var submitBulkAccessCodes=function(){
		var $this = $(this);
		if($this.hasClass("disableBtn")){ return false; }
		var popup = $this.closest(".createBulkAccessCodesPopup");
		var entities = [];
		var entityInputHolder = popup.find(".chooseSellableItem");
		for(var index=0;index<entityInputHolder.length;index++){
			var each = $(entityInputHolder.get(index));
			var entityIdInput = each.find(".chooseSellableEntityId");
			var entityId = entityIdInput.data("id");
			if(!entityId){
				showError(i18nJS("CHOOSE_SEARCH_ENTITY"),function(){
					entityIdInput.focus();
				});
				return;
			}
			var entity = {
				type : each.find(".chooseSellableEntityType").val(),
				id : entityId
			};
			entities.push(entity);
		};
		var sellingPoint = popup.find(".invSellPoint").val();
		var sellingRef = popup.find(".invSellRefNo").val();
		var bulkCount = popup.find(".bulkCount").val();
		if(bulkCount > 500){
			showError(i18nJS("Sorry, The Limit of the Bulk Access Codes is 500"));
			return;
		}
		if(!sellingPoint || !sellingRef){
			showError(i18nJS("PLEASE_PROVIDE_SELLING_POINT"));
			return;
		}
		var addressobject = {
			name : '',
			contactNo : '',
			email : '',
			address : '',
			pinCode : '',
			location : {
				city : '',
				country : ''
			}
		};
		var billingAddress = addressobject;
		var shippingAddress = billingAddress;
		$this.addClass("disableBtn");
		var params = {
			buyerContactDetails : {
				email : '',
				billingAddress : billingAddress,
				shipmentAddress : shippingAddress
			},
			entities : entities,
			sellerInfo : {
				pointOfSale : sellingPoint,
				sellerReferenceNo : sellingRef,
			},
			count : bulkCount,
			shipmentStatus : "NOT_DISPATCHED",
		};
		var table = parDiv.find(".qrInventoryTable");
		showTopLoader();
		var successFn = function() {
            closeVPopup();
            showMessage("Successfully Created",refreshPage);
        };
		vReq.post("/QrInventory/postBulkAccessCodeReq",params,successFn);
	};
};
