var qrExtSignup = new function() {
    var parDiv;
    var parDivCustom;
    var click = "click.qrExtSignUp";
    var enterkey = "enterkey.qrExtSignUp";
    var change = "change.qrExtSignUp";
    var table;
    var customizeTable;
    var sampleCatTd = "<td class='centerText'><div><input type='checkbox' class='chooseSecForCategory'/></div></td>";
    var noOfDefaultCols = 0;
    var sectionCategoryMap = {};
    this.init = function() {
        parDiv = $("#cmdsSingupMGMTPage");
        table = parDiv.find(".cmdsSignupTable");
        parDiv.off(click).off(enterkey).off(change);
        parDiv
                .on(click, ".openProgramSections", fetchSubjectSections)
                .on(click, ".addCategoryBtn", addCategoryBtn)
                .on(click, ".removeCategory", removeCategory)
                .on(click, ".cancelSignupSubmit", cancelExtSignup)
                .on(click, ".submitExtSignup", submitExtSignup)
                .on(click, ".customizeSignupBtn", showCustomizeSignup)
                .on(click, ".customizeCategoriespBtn", showcustomizeCategories)
                .on(click, ".manageDigLibraryBtn", showCustomizeDigitalLibrary)
                .on(click, ".chooseSecCurrency", changeSecCurrency)
                .on(click, ".managePackageInfo", managePackageInfo)
                .on(change, ".checkForPayment", checkForPaymentChanged)
                .on(change, ".chooseSecForCategory", chooseSecForCategoryChanged)
                .on(change, ".chooseSecOpenness", chooseSecOpennessChanged)
                .on(enterkey, ".addCategoryInput", addCategoryCol)

        noOfDefaultCols = 4;
        fetchCategories();
    };
    var cancelExtSignup = function() {
        sectionCategoryMap = {};
        refreshPage();
    };
    this.initCustomize = function() {
        parDivCustom = $("#cmdsCustomizeSignupPage");
        customizeTable = parDivCustom.find(".cmdsCustSignupTable");
        parDivCustom.off(click).off(enterkey).off(change);
        parDivCustom
                .on(change, ".reqFieldInSignup", reqFieldInSignupChanged)
                .on(change, ".addFieldInSignup", addFieldInSignupChanged)
                .on(click, ".addNewFieldBtn", addNewFieldBtn)
                .on(click, ".submitThisNewFieldForSignup", addFieldRow)
                .on(click, ".cancelSignupSubmit", refreshPage)
                .on(click, ".removeField", removeField)
                .on(click, ".submitCustomizeSignup", submitCustomizeSignup)
                .on(click, ".addDropDownFieldsForSignup", addDropDownFieldsForSignup)

        var bodyClickEvent = "click.qrExtSignup";
        $(document)
                .off(bodyClickEvent)
                .on(bodyClickEvent, ".addNewDropDownInputForSignup", addNewDropDownInputForSignup)
                .on(bodyClickEvent, ".removeDropDownInputForSignup", removeDropDownInputForSignup)
                .on(bodyClickEvent, ".submitDropDownInputFields", submitDropDownInputFields)

        noOfDefaultCols = 3;
    };
    this.initCustomizeDigitalLibrary = function() {
        parDivCustomizeDigitalLibrary = $("#cmdsCustomizeDigitalLibraryPage");
        customizeDigitalLibraryTable = parDivCustomizeDigitalLibrary.find(".cmdsCustDigitalLibraryTable");
        parDivCustomizeDigitalLibrary.off(click).off(enterkey).off(change);
        parDivCustomizeDigitalLibrary
                .on(change, ".hideFieldInDigitalLibrary", hideFieldInDigitalLibrary)
                .on(click, ".cancelDigitalLibrarySubmit", refreshPage)
                .on(click, ".submitDigitalLibraryFields", submitDigitalLibraryFields)
        var bodyClickEvent = "click.qrExtSignup";
    };
    this.initCustomizeCategories = function() {
        parDivCustomizeCategories = $("#cmdsCustomizeCategoriesPage");
        customizeCustomizeCategoriesTable = parDivCustomizeCategories.find(".cmdsCustomizeCategoriesTable");
        customizeCustomizeCategoriesTable.off(click).on(click,".submitCustomizeCategories",submitCustomizeCategories)
                                         .on(click,".iconPath .iconUpload",addiconPath)
                                         .on(click,".bannerPath .bannerUpload",addbannerPath);
    };
    var id="";
    var addiconPath = function(){
        id = $(this).attr("id");
        type="icon";
        if (window.qq) {
          createUploaderQuesAdd(id,type);
      } else {
          fetchScripts([{fname: "widgets/fileuploader.js", cb: createUploaderQuesAdd(id,type)}]);
      }
    };
    var createUploaderQuesAdd = function(id,type){
        var params;
        var uploader = new qq.FileUploader({
          element: document.getElementById(id),
          action: '/Application/makeFile',
          debug: true,
          params: {
              uploadFileParamName: "imageFile",
              id: id,
              type:type
          },
          onComplete:function(id, fileName, responseJSON){
            var obj = $.parseJSON(responseJSON);
            if(obj["success"] === true){
               $.post("/Application/uploadImage",{uploadFileParamName:"imageFile",myUserId:USERID,qqfile:fileName},function(data){
                    if(data.result.uploaded){
                        $id = obj["id"];
                        var $imageHolder = $("#"+$id);
                        var $item = $imageHolder.closest('td');
                        var imagepath = $(data.result.imgHtml).attr('src');
                        if(obj["type"]==="icon"){
                            var iconUUID = data.result.uuid;
                            $item.find(".iconHolder img").attr("src", imagepath);
                            $item.find(".iconHolder img").attr("uuid", iconUUID);
                            var imagesrc = $item.find(".iconHolder img").attr("src");
                            var thumbuuid = $item.find(".iconHolder img").attr("uuid");
                        }
                        else{
                            var bannerUUID = data.result.uuid;
                            $item.find(".bannerHolder img").attr("src", imagepath);
                            $item.find(".bannerHolder img").attr("uuid", bannerUUID);
                            var imagesrc = $item.find(".bannerHolder img").attr("src");
                            var banuuid = $item.find(".bannerHolder img").attr("uuid");
                        }    
                    }
                    else{
                        showError("try uploading again!!");
                    }
                });
            }          
               else{
                showError("something went wrong");
                }               
            }
        });
    };
    var addbannerPath = function(){
        id = $(this).attr("id");
        type = "banner";
        if (window.qq) {
          createUploaderQuesAdd(id,type);
      } else {
          fetchScripts([{fname: "widgets/fileuploader.js", cb: createUploaderQuesAdd(id,type)}]);
      }
  };
    var submitCustomizeCategories = function(){
            $this = $(this);
            var categoryId = $this.data("id");
            var params = {
                id : categoryId
            };
            var $item = $(this).closest("tr");
            var priority = $item.find(".priority").val().trim();
            var description = $item.find(".description").val().trim();
            var shortDescription = $item.find(".shortDescription").val().trim();
            var iconPath = $item.find(".iconPath .iconHolder img").attr("src");
            var bannerPath = $item.find(".bannerPath .bannerHolder img").attr("src");
            var bannerUUID = $item.find(".bannerPath .bannerHolder img").attr("uuid");
            var iconUUID = $item.find(".iconPath .iconHolder img").attr("uuid");
            if(typeof iconPath === "undefined" || iconPath===""){
                console.log("error is came");
                showError("please upload a icon");
                return;
            }
            if(typeof bannerPath === "undefined" || bannerPath===""){
                console.log("error is came");
                showError("please upload a banner");
                return;
            }
            if(parseInt(priority) > 3 || parseInt(priority)<=0){
                showError("should be less than 3 and greater than 0");
                return;
            }
            params.priority = priority;
            params.description = description;
            params.shortDescription = shortDescription;
            params.thumbnail = iconPath;
            params.banner = bannerPath;
            params.bannerUUID = bannerUUID+".jpg";
            params.iconUUID = iconUUID+".jpg";
            showTopLoader();
            vReq.get("/QrExtSignup/customizeCategory", params, function(data) {
                if (data && data.result.success ) {
                    showMessage("Successfully Updated");
                    hideTopLoader();
                }
                else{
                    showError("priority cant be same");
                    hideTopLoader();
                }
        });
    };
    var fetchCategories = function() {
        showTopLoader();
        vReq.get("/QrExtSignup/getCategories", {}, function(data) {
            if (data && data.errorCode == "" &&
                    data.result.list && data.result.list.length > 0) {
                var list = data.result.list;
                for (var i = 0; i < list.length; i++) {
                    var cat = list[i];
                    var index = appendNewCol(cat.name, cat.id);
                    if (cat.sectionIds) {
                        $(cat.sectionIds).each(function() {
                            var sectionId = this;
                            if (!sectionCategoryMap[sectionId]) {
                                sectionCategoryMap[sectionId] = [];
                            }
                            sectionCategoryMap[sectionId].push({id: cat.id, index: index});
                        });
                    }
                    ;
                }
                ;
            }
            ;
            putConsoleLogs(sectionCategoryMap);
        });
    };
    var fetchSubjectSections = function() {
        var $this = $(this);
        var programId = $this.data("progId");
        var tr = $this.closest("tr");
        // if(tr.hasClass("nonEditRow")){
        //     return;
        // }
        var noOfCols = table.find("th").length;
        var params = {
            programId: programId,
            noOfCols: noOfCols
        };
        showTopLoader();
        vReq.get("/QrExtSignup/getProgramSections", params, function(data) {
            tr.after(data);
            var nextTr = tr.next();
            tr.remove();
            do {
                if (nextTr.hasClass("sectionTr")) {
                    var sectionId = nextTr.data("sectionId");
                    var cats = sectionCategoryMap[sectionId];
                    if (!cats) {
                        nextTr = nextTr.next();
                        continue;
                    }
                    $(cats).each(function(index, cat) {
                        var catId = cat.id;
                        var catIndex = cat.index;
                        if (catIndex > 0 && $(table.find("th").get(catIndex)).data("id") == catId) {
                            var td = $(nextTr.find("td").get(catIndex));
                            td.find(".chooseSecForCategory").prop("checked", true);
                        }
                    });
                } else {
                    break;
                }
                nextTr = nextTr.next();
            } while (nextTr.get(0));
        });
    };
    var chooseSecOpennessChanged = function() {
        var $this = $(this);
        var value = $this.val();
        if ("CLOSED" == value) {
            var tr = $this.closest("tr");
            tr.find(".checkForPayment").prop("checked", false);
//            tr.find(".pricePerSec").val("").prop("disabled", true);
            tr.find(".managePackageInfo").addClass("btnDisabled");
        } else {
            tr.find(".managePackageInfo").removeClass("btnDisabled");
        }
    };
    var changeSecCurrency = function() {
        var $this = $(this);
        var div = parDiv.find("#chooseCurrencySample").html();
        var popup = showVPopup(0.6);
        popup.html(div);
        var holder = popup.find(".chooseCurrencyPopup");
        var select = holder.find(".chooseCurrencySignup");
        select.val($this.data("code") + "#" + $this.data("symbol"));
        holder.on(click, ".selectCurrencyBtn", function() {
            var val = select.val();
            val = val.split("#");
            var code = val[0], symbol = val[1];
            $this.data("code", code);
            $this.data("symbol", symbol);
            $this.text(code + "(" + symbol + ")");
            closeVPopup();
        });
    };
    var addCategoryBtn = function() {
        var $this = $(this);
        $this.addClass("nonner");
        $this.siblings(".addCategoryDiv").removeClass("nonner").find(".addCategoryInput").val("").focus();
    };
    var hideAddCategoryBtn = function() {
        parDiv.find(".addCategoryBtn").removeClass("nonner");
        parDiv.find(".addCategoryDiv").addClass("nonner").find(".addCategoryInput").val("");
    };
    var appendNewCol = function(name, id) {
        var secTrs = table.find(".sectionTr");
        secTrs.each(function() {
            var secTr = $(this);
            secTr.find(".addCatTd").before(sampleCatTd);
        });
        var catHTML = "<th class='categoryName' data-id='" + id + "'>";
        catHTML += "<span class='removeCategory'></span><span>" + name + "</span></th>";
        var addNewCatTh = table.find(".addNewCategory").before(catHTML);
        var index = table.find("th").index(addNewCatTh) - 1;
        var progTrs = table.find(".progTr");
        progTrs.each(function() {
            var progTr = $(this);
            var tmp = progTr.find(".fetchRowByCol");
            var colCount = parseInt(tmp.attr("colspan"));
            tmp.attr("colspan", ++colCount);
        });
        return index;
    };
    var addCategoryCol = function() {
        var name = $.trim($(this).val());
        if (name.length == 0) {
            return;
        }
        hideAddCategoryBtn();
        var params = {
            name: name
        };
        showTopLoader();
        vReq.post("/QrExtSignup/addCategory", params, function(data) {
            if (data && data.result && data.result.id) {
                appendNewCol(name, data.result.id);
            }
        });
    };
    var canAllowToAddPayment = function(tr) {
        var chooseSecOpenness = tr.find(".chooseSecOpenness");
        if ("CLOSED" != chooseSecOpenness.val()) {
            return true;
        } else {
            return false;
        }
    };
    var checkForPaymentChanged = function() {
        var $this = $(this);
        var tr = $this.closest("tr");
        if ($this.is(":checked")) {
            var canAllow = canAllowToAddPayment(tr);
            if (canAllow) {
                tr.find(".managePackageInfo").removeClass("btnDisabled");
            } else {
                showError("Can not add payment-info if the class is a 'closed' one!");
                $this.prop("checked", false);
                tr.find(".managePackageInfo").addClass("btnDisabled");
            }
        } else {
            tr.find(".managePackageInfo").addClass("btnDisabled");
        }
    };
    var removeCategory = function() {
        var $this = $(this);
        var params = {
            id: $this.closest(".categoryName").data("id")
        };
        showTopLoader();
        vReq.post("/QrExtSignup/removeCategory", params, function(data) {
            if (data && data.result && data.result.deleted) {
                var tr = $this.closest("tr");
                var th = $this.closest("th");
                var index = tr.find("th").index(th);
                var secTrs = table.find(".sectionTr");
                secTrs.each(function() {
                    var secTr = $(this);
                    $(secTr.find("td").get(index)).remove();
                });
                th.remove();
            }
        });
    };
    var chooseSecForCategoryChanged = function() {
        $(this).data("changed", true);
    };
    var submitCategories = function(cbFn) {
        var categories = table.find(".categoryName");
        var secTrs = table.find(".sectionTr");
        var allCols = table.find("th");
        if (categories.length <= 0 || secTrs.length <= 0) {
            try {
                cbFn();
            } catch (err) {
            }
            return;
        }
        var list = [];
        categories.each(function() {
            var item = $(this);
            var id = item.data("id");
            var index = allCols.index(item);
            var addedSecList = [];
            var removedSecList = [];
            secTrs.each(function() {
                var secTr = $(this);
                var td = $(secTr.find("td").get(index));
                var chooseSecForCategory = td.find(".chooseSecForCategory");
                if (chooseSecForCategory.data("changed")) {
                    var isChecked = chooseSecForCategory.is(":checked");
                    if (isChecked) {
                        addedSecList.push(secTr.data("sectionId"));
                    } else {
                        removedSecList.push(secTr.data("sectionId"));
                    }
                }
            });
            if (addedSecList.length > 0 || removedSecList.length > 0) {
                var obj = {
                    id: id,
                    addedSectionIds: addedSecList,
                    removedSectionIds: removedSecList
                }
                list.push(obj);
            }
        });
        if (list.length > 0) {
            var params = {
                categoryList: list
            };
            vReq.post("/QrExtSignup/editCategories", params, function(data) {
                if (data && data.result.edited && cbFn) {
                    try {
                        cbFn(data);
                    } catch (err) {
                    }
                }
            });
        } else {
            try {
                cbFn();
            } catch (err) {
            }
        }
    };
    var submitSections = function(cbFn) {
        var secTrs = table.find(".sectionTr");
        if (secTrs.length <= 0) {
            try {
                cbFn();
            } catch (err) {
            }
            return;
        }
        var list = [];
        secTrs.each(function() {
            var section = $(this);
            var obj = {
                id: section.data("sectionId"),
                accessScope: "CLOSED",
                revenueModel: "FREE",
                costRate: {
                    value: 0,
                    currencyCode: 'INR'
                }
            };
            obj.accessScope = section.find(".chooseSecOpenness").val();
            var isPaid = section.find(".checkForPayment").is(":checked");
            obj.revenueModel = isPaid ? "PAID" : "FREE";
//            obj.costRate.value = section.find(".pricePerSec").val().trim();
//            obj.costRate.value *= 100; //IN PAISE
//            obj.costRate.currencyCode = section.find(".chooseSecCurrency").data("code");
            list.push(obj);
        });
        if (list.length > 0) {
            var params = {
                sectionAccessInfos: list
            };
            vReq.post("/QrExtSignup/updateSectionAccess", params, function(data) {
                if (data && data.result.edited && cbFn) {
                    try {
                        cbFn(data);
                    } catch (err) {
                    }
                }
            });
        } else {
            try {
                cbFn();
            } catch (err) {
            }
        }
    };
    var submitExtSignup = function() {
        // CATEGORY SUBMIT
        submitCategories(function(data) {
            // SECTION SUBMIT
            submitSections(function(data2) {
                showMessage("Successfully saved the changes!");
            });
        });
    };

    //CUSTOMIZE SINGUP FORM
    var showCustomizeSignup = function() {
        opencmdsPage("/QrExtSignup/customizeSignup", {}, $("#cmdsSingupMGMTPage"));
    };
    var showcustomizeCategories = function() {
        console.log("came");
        opencmdsPage("/QrExtSignup/customizeCategories", {}, $("#cmdsSingupMGMTPage"));
    };
    var reqFieldInSignupChanged = function() {
        var $this = $(this);
        if ($this.is(":checked")) {
            var tr = $this.closest("tr");
            tr.find(".addFieldInSignup").prop("checked", true);
        }
    };
    var addFieldInSignupChanged = function() {
        var $this = $(this);
        if (!$this.is(":checked")) {
            var tr = $this.closest("tr");
            tr.find(".reqFieldInSignup").prop("checked", false);
        }
    };
    var addNewFieldBtn = function() {
        var $this = $(this);
        $this.addClass("nonner");
        var addNewFieldDiv = $this.siblings(".addNewFieldDiv").removeClass("nonner")
	addNewFieldDiv.find(".addNewFieldType").val("TEXT");
	addNewFieldDiv.find(".addNewFieldInput").val("").focus();
    };
    var addFieldRow = function() {
        var inputField = $(this).siblings("input");
        var name = $.trim(inputField.val());
        var fieldType = $(this).siblings("select").val();
        if (!name) {
            return;
        }
        var addTr = inputField.closest("tr");
        var newTr = $("#sampleSignupField").find("tr").clone();
        addTr.before(newTr);
        newTr.find(".custSignupFieldName:last")
                .text(name)
                .after("<span class='removeField'></span>");
        if (addFieldRowCustomize[fieldType]) {
            addFieldRowCustomize[fieldType](newTr);
        }else if(fieldType){
	    newTr.data("fieldType", fieldType);
            newTr.find(".singupFieldType").text(i18nJS("SIGNUP.FIELD_TYPE_"+fieldType));
	}
        parDivCustom.find(".addNewFieldBtn").removeClass("nonner");
        inputField.closest(".addNewFieldDiv").addClass("nonner")
        inputField.val("");
    };
    var addFieldRowCustomize = {
        DROP_DOWN: function(newTr) {
            newTr.find(".singupFieldValues").html('<a class="addDropDownFieldsForSignup">'+i18nJS("SIGNUP.ADD_EDIT")+'</a>\n\
            <select class="dropDownForSignup"><option value="">'+i18nJS("SIGNUP.SELECT_DEFAULT_VALUE")+'</option></select>');
            newTr.data("fieldType", "TEXT");
            newTr.find(".singupFieldType").text(i18nJS("SIGNUP.FIELD_TYPE_DROP_DOWN"));
            newTr.find(".addPlaceHolderInSignup").replaceWith("<span class='greyTextColor italics smally'>"+i18nJS('TXT_NOT_APPLICABLE')+"</span>");
        },
	REGEXP: function(newTr){
            newTr.find(".singupFieldValues").html('<input class="inputRegexpForSignup" placeholder="Enter Regular Expression"/>');
            newTr.data("fieldType", "TEXT");
            newTr.find(".singupFieldType").text(i18nJS("SIGNUP.FIELD_TYPE_REGEXP"));
	}
    };
    var removeField = function() {
        var tr = $(this).closest("tr");
        tr.remove();
    };
    var submitCustomizeSignup = function() {
        var params = {
            targetOrgMemberProfile: "STUDENT"
        };
        var fields = [];
        customizeTable.find(".signupOptionalField").each(function() {
            var $this = $(this);
            var include = $this.find(".addFieldInSignup").is(":checked");
            if (include) {
                var name = $this.find(".custSignupFieldName").text().trim();
                var req = $this.find(".reqFieldInSignup").is(":checked");
                var rangeDropDown = $this.find(".dropDownForSignup");
                var rangeOpts = rangeDropDown.find("option");
                var valueSet = [];
		var defaultValue;
                if (rangeOpts.length > 0) {
                    for (var k = 0, l = rangeOpts.length; k < l; k++) {
                        var v = rangeOpts.eq(k).val().trim();
                        if (v) {
                            valueSet.push(v);
                        }
                    }
		    defaultValue = rangeDropDown.val();
                }
		var validationType = "VALUE";
                if (valueSet.length > 0) {
                    validationType = "LIST";
                }
		var placeHolder = $this.find(".addPlaceHolderInSignup").val();
		placeHolder = placeHolder?placeHolder:"";
		var regExpElem = $this.find(".inputRegexpForSignup");
		var regexpInput = $.trim(regExpElem.val());
		if(regexpInput){
			try{
				var regexpObj = new RegExp(regexpInput);
				if(!regexpObj){
                			showError(i18nJS("SIGNUP.ERROR_NOT_PROPER_REGEXP"),function(){
						regExpElem.focus();	
					});
					return;	
				}
			}catch(err){
                		showError(i18nJS("SIGNUP.ERROR_NOT_PROPER_REGEXP"),function(){
					regExpElem.focus();	
				});
				return;	
			}
			validationType = "REGEX";
			valueSet.push(regexpInput);
		}	
                var obj = {
			required: req, 
			name: name,
			fieldType: $this.data("fieldType") || "TEXT", 
			valueSet: valueSet,
			placeHolder : placeHolder,
			validationType : validationType
		};
		if(defaultValue){
			obj.defaultValue = defaultValue;
		}
                fields.push(obj);
            }
        });
        params.fields = fields;
        vReq.post("/QrExtSignup/submitCustomSignup", params, function(data) {
            if (data && data.result) {
                showMessage("Successfully altered the Sign-Up form fields!");
            }
        });
    };
    var currentSelectForSignUp;
    var addDropDownFieldsForSignup = function() {
        var popup = fillcmdsPopup("submitDropDownInputFields", "entryForDropDownSample");
        $('<div class="cmdsPopupHead">Add Fields</div>').insertBefore(popup.find(".entryForDropDown"));
        currentSelectForSignUp = $(this).siblings("select");
        var values = currentSelectForSignUp.data("options") || [];
        var defaultValues = makeHTMLTag("div");
        for (var k = 0, l = values.length; k < l; k++) {
            var newField = entryForDropDownSample.children().clone(true);
            newField.find("input").val(values[k]);
            defaultValues.append(newField);
        }
        if (values.length > 0) {
            var entry = popup.find(".entryForDropDown");
            defaultValues.children().insertAfter(entry);
            entry.remove();
        }
    };
    var addNewDropDownInputForSignup = function() {
        var newField = entryForDropDownSample.children().clone(true);
        newField.insertAfter($(this).closest(".entryForDropDown"));
    };
    var removeDropDownInputForSignup = function() {
        $(this).closest(".entryForDropDown").remove();
    };
    var submitDropDownInputFields = function() {
        var fields = $(this).closest("#cmdsPopup").find(".entryForDropDown");
        var dropDownValuesOptions = makeHTMLTag("div");
        var dropDownValues = [];
        for (var k = 0, l = fields.length; k < l; k++) {
            var v = fields.eq(k).find(".dropDownValueForSignup").val().trim();
            if (v) {
                dropDownValuesOptions.append("<option value='" + v + "'>" + v + "</option>");
                dropDownValues.push(v);
            }
        }
        if (dropDownValues.length === 0) {
            showError("Please add a value for drop down");
        } else {
            closePopup();
            if (currentSelectForSignUp) {
                currentSelectForSignUp.html(dropDownValuesOptions.children());
                currentSelectForSignUp.data("options", dropDownValues);
            }
        }
    };
    var managePackageInfo = function() {
        var $this = $(this);
        if ($this.hasClass("btnDisabled")) {
            showError("Can not add payment-info if the class is a 'closed' or 'free'!");
            return;
        }
        var tr = $this.closest("tr");
        if (!tr.find(".checkForPayment").is(":checked")) {
            showError("Can not add payment-info if the class is 'free'!");
            return;
        }
        var programId = $this.data("progId");
        var sectionId = $this.data("sectionId");
        var noOfCols = table.find("th").length;
        var params = {
            programId: programId,
            sectionId: sectionId,
            noOfCols: noOfCols
        };
        showTopLoader();
        vReq.get("/QrExtSignup/getPackageInfo", params, function(data) {
            var popup = showVPopup(0.7);
            popup.html(data);
            popup = popup.find(".packageInfoPopup");
            popup.off(click)
                 .on(click, ".addNewPackage", addNewPackage)
                 .on(click, ".editPackageBtn", editPackage)
                 .on(click, ".deletePackageBtn", deletePackage)
                 .on(click, ".submitPackages", submitPackages)
                 .on(click, ".updateMaxDiscount", updateMaxDiscount)
        });
    };
    var addNewPackage = function() {
        var $this = $(this);
        var popup = $this.closest(".packageInfoPopup");
        var packageTable = popup.find(".packageInfoTable");
        var packageInputTr = "<tr class=\"packageTr\"><td><input type=\"number\" class=\"numDaysInput\" placeholder=\"Days\"> </td>";
        packageInputTr += "<td><input type=\"number\" class=\"costRateInput\" placeholder=\"Price\"> </td></tr>";
        popup.find(".noPackagesText").addClass("nonner");
        packageTable.append(packageInputTr);
    };
    var editPackage = function(e) {
        var $this = $(this);
        var packageTr = $this.closest("tr");
        var numDaysVal = packageTr.find(".numDaysCol").html();
        var costRateVal = packageTr.find(".costRateCol").html();
        var numDaysTd = "<td><input type=\"number\" class=\"numDaysInput\" value=" + numDaysVal +"> </td>";
        var costRateTd = "<td><input type=\"number\" class=\"costRateInput\" value="+ costRateVal +"> </td>";
        var newTr = "<tr class=\"packageTr\">" + numDaysTd + costRateTd + "</tr>";
        $this.closest("tr").replaceWith(newTr);
        return false;
    };
    var deletePackage = function() {
        var $this = $(this);
        alert("The package is marked for deletion. Press Submit button to complete deletion.");
        $this.closest("tr").remove();
        return false;
    };
    var submitPackages = function() {
        var $this = $(this);
        // if ($this.hasClass("btnDisabled")) { return; }
        // $this.addClass("btnDisabled");
        var popup = $this.closest(".packageInfoPopup");
        var sectionId = $this.data("sectionId");
        var packageTable = popup.find(".packageInfoTable");
        var packagesList = [];
        console.log("length" + packageTable.find(".packageTr").length);
        packageTable.find(".packageTr").each(function() {
            var packageTr = $(this);
            var numDaysVal = 0;
            var costRateVal = 0;
            console.log($this[0]);
            if (packageTr.hasClass("packageDisplayTr")) {
                numDaysVal = packageTr.find(".numDaysCol").html();
                costRateVal = packageTr.find(".costRateCol").html();
            } else {
                numDaysVal = packageTr.find(".numDaysInput").val();
                costRateVal = packageTr.find(".costRateInput").val().trim();
            }
            if (numDaysVal <= 0 || costRateVal <= 0) {
                alert("Number of days OR Cost should be valid numbers!")
                return;
            }
            var package = {
                numDays : numDaysVal,
                costRate : {
                    value: costRateVal * 100,
                    currencyCode: 'INR'
                }
            }
            packagesList.push(package);
        });

        var params = {
            sectionId : sectionId,
            packagesList : packagesList
        };
        console.log(params);
        showTopLoader();
        vReq.post("/QrExtSignup/updatePackageInfo", params, function(data) {
            if (data && data.result.edited) {
                showMessage("Successfully saved the changes!");
            } else {
                console.log("Error in update of packages");
            }
            closeVPopup();
        });
    };
    var updateMaxDiscount = function() {
        console.log("Update max discount");
        var $this = $(this);
        var popup = $this.closest(".packageInfoPopup");
        var sectionId = $this.data("sectionId");
        var maxDiscount = popup.find(".maxDiscountInput").val();
        var params = {
            sectionId : sectionId,
            maxDiscount : maxDiscount
        }
        console.log(params);
        vReq.post("/QrExtSignup/updateMaxDiscount", params, function(data) {
            if (data && data.result.edited) {
                showMessage("Successfully updated the Max discount for this section!");
            }
        });
    }
};
// Customize Digital submitDigitalLibraryFields
    var showCustomizeDigitalLibrary = function() {
        opencmdsPage("/QrExtSignup/manageDigitalLibrary", {}, $("#cmdsSingupMGMTPage"));
    };

    var hideFieldInDigitalLibrary = function() {
        var $this = $(this);
        if ($this.is(":checked")) {
            var tr = $this.closest("tr");
            tr.find(".hideFieldInDigitalLibrary").prop("checked", true);
        }
    };

    var submitDigitalLibraryFields = function() {
        var params = {
            orgId: "orgId",
            userId : "userId"
        };
        var fields = [];
        customizeDigitalLibraryTable.find(".digitalLibraryField").each(function() {
            var $this = $(this);
            var include = $this.find(".hideFieldInDigitalLibrary").is(":checked");
            if (include) {
                var name = $this.find(".digitalLibraryFieldName").text().trim();
                fields.push(name);
            }
        });
        var obj = {
            fields : fields
        };
        params.fields = fields;
        vReq.post("/QrExtSignup/submitDigitalLibraryFields", params, function(data) {
            if (data && data.result) {
                showMessage("Successfully altered the Digital Library fields!");
            }
        });
    };
