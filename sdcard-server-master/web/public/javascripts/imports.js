var mountPointSample;
var dtappImports = new (function($) {
	var dtappContentDiv, bodyClickEvent = "click.qrProgram", clickEvent = "click", importingItemsHolder, importingItemSample, importedLibrarySample, importedLibrariesHolder;
	this.init = function(params) {
		dtappContentDiv = $("#dtappContentDiv");
		importingItemsHolder = $("#importingItems");
		importingItemSample = $("#importingItemSample");
		importedLibrarySample = $("#importedLibrarySample");
		mountPointSample = $("#mountPointSample");
		importedLibrariesHolder = $("#importedLibraries");
		dtappContentDiv
				.on(clickEvent, ".importLibraryBtn,.onlyImportLibraryBtn",
						openProgramsPopup)
				.on(clickEvent, ".startImportingLibrary", startImportingLibrary)
				.on(clickEvent, ".removeImportingLibrary",
						removeImportingLibrary).on(clickEvent,
						".importingItemCancelButton", importingItemCancel).on(
						clickEvent, ".syncNewContentForLibrary",
						syncNewContentForLibrary).on(clickEvent,
						".noSyncPossible", noSyncPossible).on(clickEvent,
						".sdcardGroup", opensdcardGroup).on(clickEvent,
						".sdcard", opensdcardFlashingSection).on(clickEvent,
						"#flashNow", flashNow)

		$("body").off(bodyClickEvent).on(bodyClickEvent,
				".submitSelectedSection", submitSelectedSection).on(
				bodyClickEvent, ".finallyStartFlashingNow",
				finallyStartFlashingNow)

		if (params && params.targetPage == "IMPORTS") {
			$.get("getImportedLibraries", {}, function(resp) {
				if (resp) {
					for ( var k = 0; k < resp.length; k++) {
						var importItem = resp[k];
						if (importItem.state == "DOWNLOADED") {
							renderImportedLibrary(importItem);
						} else {
							renderImportingItems(importItem);
						}
					}
					hideUnhideImportProgressDivs();
					hideUnhideImportedLibraries();
					setInterval(pollForCurrentStates, 5000);
					fetchSyncStatusForImportedLibraries();
				} else {
					showError(COMMON_ERROR_MESSAGE);
				}
			});
		}
	};
	var startLoader = showTopLoader;
	var stopLoader = hideTopLoader;
	var closePopup = closecmdsPopup;
	var postReq = vReq.post;
	var getReq = vReq.get;

	var fetchSyncStatusForImportedLibraries = function() {
		var importedLibraries = importedLibrariesHolder
				.children(".importedLibrary");
		if (importedLibraries.length > 0) {
			var importedLibrary = importedLibraries.eq(0);
			fetchSyncStatusForImportedLibrary(importedLibrary)
		}
	};
	var fetchSyncStatusForImportedLibrary = function(importedLibrary) {
		$.ajax({
			type : "GET",
			url : "resources",
			data : {
				remoteOnly : true,
				size : 1,
				addContent : true,
				targetType : "SECTION",
				targetId : importedLibrary.data("sectionId")
			}
		}).done(
				function(resp, textStatus, xhr) {
					var btnClass;
					if (resp.totalHits && resp.totalHits > 0) {
						btnClass = "syncNewContentForLibrary";
					} else {
						btnClass = "noSyncPossible";
						$.get("syncSDcardGroup", {
							targetType : "SECTION",
							targetId : importedLibrary.data("sectionId"),
							addedAfter : importedLibrary.data("lastSyncTime")
						}, function(data) {
							if (data.totalHits && data.totalHits > 0) {
								importedLibrary.find(".syncLibraryButton")
										.addClass("syncNewContentForLibrary")
										.removeClass("noSyncPossible");
							}
						});
					}
					importedLibrary.find(".syncLibraryButton").addClass(
							btnClass);
					var nextImportedLibraries = importedLibrary.next();
					if (nextImportedLibraries.length > 0) {
						fetchSyncStatusForImportedLibrary(nextImportedLibraries
								.eq(0));
					}
				});
	};
	var syncNewContentForLibrary = function(e) {
		e.stopPropagation();
		var importedLibrary = $(this).closest(".importedLibrary");
		var sectionId = importedLibrary.data("sectionId");
		var displayName = importedLibrary.find('.importedLibraryTitle').text();
		startLoader();
		$.get("resources", {
			addContent : true,
			targetType : "SECTION",
			targetId : sectionId
		}, function(resp) {
			stopLoader();
			$("#onlyImportButtonDiv").addClass("nonner");
			$("#importProgressDiv").removeClass("nonner");
			var importingItem = $("#importingItemSample").children()
					.clone(true);
			importingItem.data({
				sectionId : sectionId
			});
			importingItem.find(".importingItemTitle").html(displayName);
			importingItemsHolder.append(importingItem);
			importedLibrary.remove();
			hideUnhideImportedLibraries();
		});
		return false;
	};
	var noSyncPossible = function(e) {
		e.stopPropagation();
		showMessage("This Library content is up-to-date");
		return false;
	};

	var pollForCurrentStates = function() {
		var currentProgressingItems = importingItemsHolder
				.children(".importingItem_PREPARING,.importingItem_DOWNLOADING");
		if (currentProgressingItems.length === 0) {
			return;
		}
		$
				.get(
						"getImportedLibraries",
						{},
						function(resp) {
							if (resp) {
								var statesJson = {};
								for ( var k = 0; k < resp.length; k++) {
									statesJson[resp[k].id] = resp[k];
								}
								var progressingItems = importingItemsHolder
										.children(".importingItem_PREPARING,.importingItem_DOWNLOADING");
								for ( var i = 0; i < progressingItems.length; i++) {
									var importingItem = progressingItems.eq(i);
									var stateJson = statesJson[importingItem
											.data("sectionId")];
									if (stateJson
											&& stateJson.state === "DOWNLOADING") {
										importingItem
												.removeClass(
														"importingItem_PREPARING")
												.addClass(
														"importingItem_DOWNLOADING");
										populateStatusForDownloadingItems(
												importingItem, stateJson);
									} else if (stateJson
											&& stateJson.state === "DOWNLOADED") {
										importingItem.remove();
										renderImportedLibrary(stateJson);
										hideUnhideImportProgressDivs();
										hideUnhideImportedLibraries();
									} else if (stateJson
											&& (stateJson.state === "CANCELLED" || stateJson.state === "ABORTED")) {
										renderImportingItems(stateJson,
												importingItem);
										importingItem.remove();
										showError("Importing of "
												+ stateJson.name
												+ " library has been aborted");
									}
								}
							}
						});
	};
	var renderImportingItems = function(importItem, insertAfterThisItem) {
		var importingItem = importingItemSample.children().clone(true);
		var state = importItem.state;
		var sectionId = importItem.id;
		var sectionName = importItem.name;
		importingItem.data({
			sectionId : sectionId
		});
		importingItem.addClass("importingItem_" + state);
		importingItem.find(".importingItemTitle").html(sectionName);
		if (insertAfterThisItem) {
			importingItem.insertAfter(insertAfterThisItem);
		} else {
			importingItemsHolder.append(importingItem);
		}
		if (state === "DOWNLOADING" || state === "PREPARING") {
			importingItem.find(".startImportingButtonsDiv").addClass("nonner");
			importingItem.find(".importingItemDownloadStartedDiv").removeClass(
					"nonner");
			if (state === "DOWNLOADING") {
				populateStatusForDownloadingItems(importingItem, importItem);
			}
		} else if (state === "CANCELLED" || state === "ABORTED") {
			importingItem.find(".startImportingLibrary").text(
					"Restart Download");
		}
	};
	var populateStatusForDownloadingItems = function(importingItem,
			importItemJson) {
		importingItem.find(".importingItemPreparingText").addClass("nonner");
		importingItem.find(".importingItemStatusText").removeClass("nonner");
		var totalSize = bytesToSize(importItemJson.size);
		importingItem.find(".importingItemImportSize").text(totalSize + ")");
		if (importItemJson.downloadedSize) {
			var downloadedSize = bytesToSize(importItemJson.downloadedSize);
			importingItem.find(".importingItemImportedSize").text(
					"(" + downloadedSize);
			var p = Math
					.round((importItemJson.downloadedSize * 100 / importItemJson.size) * 100) / 100;
			importingItem.find(".importingItemImportPercent").text(p + "%");
			var totalWidth = importingItem.find(".importingItemProgressDiv")
					.width();
			importingItem.find(".importingItemGreenDiv").width(
					totalWidth * p / 100);
		}
	};
	var hideUnhideImportProgressDivs = function() {
		var importingItems = importingItemsHolder.children();
		$("#onlyImportButtonDiv,#importProgressDiv,#importsFetchingDiv")
				.addClass("nonner");
		if (importingItems.length > 0) {
			$("#importProgressDiv").removeClass("nonner");
		} else {
			$("#onlyImportButtonDiv").removeClass("nonner");
		}
	};
	var hideUnhideImportedLibraries = function() {
		var importedItems = importedLibrariesHolder.children();
		$("#importedLibrariesFetchingDiv,#importedLibraries")
				.addClass("nonner");
		if (importedItems.length > 0) {
			importedLibrariesHolder.removeClass("nonner");
		} else {
			$("#importedLibrariesFetchingDiv").removeClass("nonner").find(
					".centerText").text("No imported libraries found.");
		}
	}

	var renderImportedLibrary = function(importItem) {
		var importedLibraryItem = importedLibrarySample.children().clone(true);
		var sectionId = importItem.id;
		var sectionName = importItem.name;
		importedLibraryItem.data("sectionId", sectionId);
		importedLibraryItem.find(".importedLibraryTitle").text(sectionName);
		importedLibraryItem.attr("href", "sdCardGroups?targetId=" + sectionId
				+ "&targetType=SECTION&name=" + sectionName);
		importedLibraryItem.find(".importedLibraryContentCount").text(
				importItem.contentCount);
		importedLibraryItem.find(".importedLibrarySDCardGrpCount").text(
				importItem.noOfCards);
		importedLibraryItem.find(".importedLibraryFlashCount").text(
				importItem.flashCount);
		if (importItem.lastSynced > 0) {
			importedLibraryItem.find(".importedLibraryLastSyncTime").text(
					$.timeago(new Date(importItem.lastSynced)));
		} else {
			importedLibraryItem.find(".importedLibrarySyncDiv").css({
				visibility : "hidden"
			});
		}
		importedLibraryItem.data("lastSyncTime", importItem.lastSynced);
		importedLibrariesHolder.append(importedLibraryItem);
	};

	var openProgramsPopup = function() {
		if (!navigator.onLine) {
			showError(NO_INTERNET_CONNECTION);
			return;
		}
		startLoader();
		var successFn = function(data) {
			var popup = getcmdsPopupBody(700);
			popup.html(data);
			stopLoader();
		};
		getReq("programsPopup", {}, successFn);
	};
	var submitSelectedSection = function() {
		var tableParams = qrAcadStr.getAcadStrTableParams();
		var sectionId = tableParams.sectionId;
		var sectionName = tableParams.sectionName;
		var displayName = tableParams.programName + "/"
				+ tableParams.centerName + "/" + sectionName;

		if (!sectionId && !sectionName) {
			showError("Please choose a section");
			return;
		}

		// checking if the library is already imported/importing
		var allItems = $("#importingItems,#importedLibraries").children();
		for ( var k = 0; k < allItems.length; k++) {
			var itemSectionId = allItems.eq(k).data("sectionId");
			if (itemSectionId && sectionId == itemSectionId) {
				showError("The library you have chosen to import is either already imported or is being imported");
				return;
			}
		}

		if (!navigator.onLine) {
			showError(NO_INTERNET_CONNECTION);
			return;
		}
		closePopup();
		startLoader();
		$.get("recordImportLibrary", {
			name : displayName,
			type : "SECTION",
			id : sectionId,
			size : tableParams.size
		}, function(resp) {
			$.get("resources", {
				addContent : true,
				targetType : "SECTION",
				targetId : sectionId
			}, function(resp) {
				stopLoader();
				$("#onlyImportButtonDiv").addClass("nonner");
				$("#importProgressDiv").removeClass("nonner");
				var importingItem = $("#importingItemSample").children().clone(
						true);
				importingItem.data({
					sectionId : sectionId
				});
				importingItem.find(".importingItemTitle").html(displayName);
				importingItemsHolder.append(importingItem);
			});
		});
	};

	var sdcardsGrpIdsClone = [];
	var sdcardIdsClone = [];
	var totalSyncReqCount = 0;
	var syncingImportItem;
	var startImportingLibrary = function() {
		var importingItem = $(this).closest(".importingItem");
		syncingImportItem = importingItem;
		var sectionId = importingItem.data("sectionId");
		$(this).parent().addClass("nonner");
		importingItem.find(".importingItemDownloadNotStartedDiv").removeClass(
				"nonner");
		var params = {
			targetId : sectionId,
			targetType : "SECTION"
		};
		var globalCardIds = [];
		if (!navigator.onLine) {
			showError(NO_INTERNET_CONNECTION);
			return;
		}
		$.ajax({
			type : "GET",
			url : "syncSDcardGroup",
			data : params
		}).done(
				function(resp, textStatus, xhr) {
					if (resp && resp.list) {
						if (resp.list.length > 0) {
							var sdcardGrps = resp.list;
							var sdcardsGrpIds = [];
							var sdcardIds = [];
							for ( var k = 0; k < sdcardGrps.length; k++) {
								var sdcardGrp = sdcardGrps[k];
								var cardIds = sdcardGrp.cardIds;
								sdcardsGrpIds.push(sdcardGrp.id);
								for ( var i = 0; i < cardIds.length; i++) {
									sdcardIds.push(cardIds[i]);
								}
							}
							sdcardsGrpIdsClone = sdcardsGrpIds;
							sdcardIdsClone = sdcardIds;
							totalSyncReqCount = sdcardIdsClone.length
									+ sdcardsGrpIdsClone.length;
							syncCardGrpInfos();
						} else {
							startDownloadingLibrary();
							return;
						}
					} else {
						showError(COMMON_ERROR_MESSAGE);
					}
				}).fail(function(data, textStatus, xhr) {
			startDownloadingLibrary();
			return;
		});
	};
	var removeImportingLibrary = function() {
		var importingItem = $(this).closest(".importingItem");
		var sectionId = importingItem.data("sectionId");
		startLoader();
		$.post("updateLibrary", {
			id : sectionId,
			state : "DELETED"
		}, function(resp) {
			stopLoader();
			importingItem.remove();
			hideUnhideImportProgressDivs();
		});
	};
	var importingItemCancel = function() {
		var importingItem = $(this).closest(".importingItem");
		var sectionId = importingItem.data("sectionId");
		startLoader();
		$.post("updateLibrary", {
			id : sectionId,
			state : "CANCELLED"
		}, function(resp) {
			stopLoader();
			renderImportingItems({
				state : "CANCELLED",
				id : sectionId,
				name : importingItem.find(".importingItemTitle").text()
			}, importingItem);
			importingItem.remove();
		});
	};

	var syncCardGrpInfos = function() {
		updateSyncStatus();
		var sdcardGrpId = sdcardsGrpIdsClone.shift();
		if (!sdcardGrpId) {
			syncCardResources();
			return;
		}
		$.ajax({
			type : "GET",
			url : "syncSDCardGroupInfo",
			data : {
				groupId : sdcardGrpId
			}
		}).always(function(data, textStatus, xhr) {
			syncCardGrpInfos();
		});
	};
	var syncCardResources = function() {
		updateSyncStatus();
		var cardId = sdcardIdsClone.shift();
		if (!cardId) {
			startDownloadingLibrary();
			return;
		}
		$.ajax({
			type : "GET",
			url : "resources",
			data : {
				addContent : true,
				targetType : "SDCARD",
				targetId : cardId
			}
		}).always(function(data, textStatus, xhr) {
			syncCardResources();
		});
	};
	var updateSyncStatus = function() {
		if (syncingImportItem) {
			var doneCount = sdcardsGrpIdsClone.length + sdcardIdsClone.length;
			var percent = Math
					.round((100 - (doneCount * 100 / totalSyncReqCount)) * 100) / 100;
			syncingImportItem.find(".preparedDownloadPercent").text(
					percent + "% Complete");
		}
	};
	var startDownloadingLibrary = function() {
		if (syncingImportItem) {
			var sectionId = syncingImportItem.data("sectionId");
			syncingImportItem
					.find(
							".startImportingButtonsDiv,.importingItemDownloadNotStartedDiv")
					.addClass("nonner");
			syncingImportItem.find(".importingItemDownloadStartedDiv")
					.removeClass("nonner");
			syncingImportItem.removeClass("importingItem_INITIALIZED")
					.addClass("importingItem_PREPARING");
			$.post("startDownloadLibrary", {
				id : sectionId,
				type : "SECTION"
			}, function() {

			});
		} else {
			showError(COMMON_ERROR_MESSAGE)
		}
	};

	var opensdcardGroup = function() {
		resetsdcardsSection();
		resetFlashingSection();
		var globalCardIds = [];
		if (!navigator.onLine) {
			showError(NO_INTERNET_CONNECTION);
			return;
		}
		$(this).addClass("sdcardGroupActive").siblings().removeClass(
				"sdcardGroupActive").addClass("sdcardGroupInactive");
		startLoader();
		var groupId = $(this).data("cardGrpId");
		$
				.get(
						"sdCardGroupInfo",
						{
							groupId : groupId
						},
						function(resp) {
							stopLoader();
							var finalContent = "";
							if (resp && resp.cards && resp.cards.length > 0) {
								var holder = makeHTMLTag("div");
								var cards = resp.cards;
								var sdcardSample = $("#sdcardSample")
										.children();
								for ( var k = 0; k < cards.length; k++) {
									var card = cards[k];
									var name = card.name.trim();
									if (!name) {
										name = "<div class='color9 italica'>Untitled</div>";
									}
									var sdcardHTML = sdcardSample.clone(true);
									sdcardHTML.data("cardId", card.id);
									sdcardHTML.find(".sdcardTitle").html(name);
									sdcardHTML.find(".sdcardContentSize").html(
											bytesToSize(card.contentSize));
									var cardFillHeight = ((card.contentSize / card.size) * 43);
									sdcardHTML.find(".sdcardFillPercentDiv")
											.height(cardFillHeight);
									sdcardHTML.find(".sdcardContentCount")
											.html(card.count);
									holder.append(sdcardHTML);
								}
								finalContent = holder.children();
							} else {
								finalContent = '<div class="userMessage">No SD Cards found in this group.</div>';
							}
							$("#sdcardsSection").addClass(
									"sdcardsSectionActive").html(finalContent);
						});
	};
	var sdcardStateActive = "sdcardStateActive";
	var checksJSONClone = [];
	var opensdcardFlashingSection = function() {
		resetFlashingSection();
		$("#sdcardFlashingSection").addClass("sdcardFlashingSectionActive");
		$(this).addClass("sdcardActive").siblings().removeClass("sdcardActive");
		/*
		 * // checking sdcard insert using settimeout just to give it the effect
		 * of auto checking setTimeout(function() { var checksJSON = [ {
		 * targetHolderClass : "sdcardStatusSection", urlStrip :
		 * "checkSDCardInsert" }, { targetHolderClass : "sdcardStorageSection",
		 * urlStrip : "checkSDCardStorage" }, { targetHolderClass :
		 * "sdcardEncryptionSection", urlStrip : "checkSDCardEncryption" } ];
		 * checksJSONClone = checksJSON.slice(0); performFlashOperation(); },
		 * 500);
		 */

		// ===>>>new code
		$("#sdcardFlashingSection").find("#flashNow").removeClass("nonner");
	};
	var performFlashOperation = function() {
		var flashOpJson = checksJSONClone.shift();
		if (!flashOpJson) {
			$("#flashNow").removeClass("nonner");
			return;
		}
		var targetHolder = $("#" + flashOpJson.targetHolderClass);
		toggleSDCardStates(targetHolder, "sdcardFlashStepChecking",
				sdcardStateActive);
		$.get(
				flashOpJson.urlStrip,
				{},
				function(resp) {
					if (resp) {
						toggleSDCardStates(targetHolder,
								"sdcardFlashStepSuccess", sdcardStateActive);
						performFlashOperation();
					} else {
						toggleSDCardStates(targetHolder,
								"sdcardFlashStepFailed", sdcardStateActive);
					}
				}).fail(function(xhr, textStatus, errorThrown) {
			showError(COMMON_ERROR_MESSAGE);
		});
	};
	var toggleSDCardStates = function(targetHolder, targetClass, activeClass) {
		targetHolder.children("." + targetClass).addClass(activeClass)
				.siblings().removeClass(activeClass);
	};
	var currentFlashingJobId;
	var intervalForFlashStatusPolling;
	var flashNow = function() {
		fillcmdsPopup("finallyStartFlashingNow", "mountPointSample");
	};
	var finallyStartFlashingNow = function() {
		var popup = $(this).closest("#cmdsPopup");
		var mountPoint = popup.find("input").val().trim();
		if (mountPoint.length === 0) {
			showError("Please provide a mount point of the SD Card");
			return;
		}
		startLoader();
		$.post(
				"pack",
				{
					sdCardId : $(".sdcardActive").data("cardId"),
					groupId : $(".sdcardGroupActive").data("cardGrpId"),
					location : mountPoint
				},
				function(resp) {
					stopLoader();
					resetFlashingSection();
					if (resp.jobId) {
						currentFlashingJobId = resp.jobId;
						var popup = getcmdsPopupBody(550);
						popup.html($("#flashProgressSample").clone(true)
								.children());
						pollForFlashStatus();
						intervalForFlashStatusPolling = setInterval(
								pollForFlashStatus, 5000);
					} else {
						showError(COMMON_ERROR_MESSAGE);
					}

				}).fail(function(xhr, textStatus, errorThrown) {
			stopLoading();
			showError(COMMON_ERROR_MESSAGE);
		});
	};

	var pollForFlashStatus = function() {
		if (currentFlashingJobId) {
			$
					.get(
							"track",
							{
								jobId : currentFlashingJobId
							},
							function(resp) {
								if (resp && resp.steps && resp.completed) {
									var cmdsPopup = $("#cmdsPopup");
									var p = Math
											.round((resp.completed * 100 / resp.steps) * 100) / 100;
									cmdsPopup.find(".flashPercentText").text(
											p + "%");
									var totalWidth = cmdsPopup.find(
											".flashProgressDiv").width();
									cmdsPopup.find(".flashGreenDiv").width(
											totalWidth * p / 100);
									if (resp.steps === resp.completed) {
										showMessage("Successfully done");
										closecmdsPopup();
										if (intervalForFlashStatusPolling) {
											clearInterval(intervalForFlashStatusPolling);
										}
									} else if (resp.status === "FAILED") {
										showError("Some error Occured in flashing.Try again");
										closecmdsPopup();
										if (intervalForFlashStatusPolling) {
											clearInterval(intervalForFlashStatusPolling);
										}
									}
								}
							});
		}
	};

	// utils
	var resetFlashingSection = function() {
		var target = $("#sdcardFlashingSection");
		target.removeClass("sdcardFlashingSectionActive");
		var stepList = [ "sdcardStatusSection", "sdcardStorageSection",
				"sdcardEncryptionSection" ];
		for ( var k = 0; k < stepList.length; k++) {
			var holder = $("#" + stepList[k]);
			holder.children(".sdCardState").removeClass("sdcardStateActive")
					.first().addClass("sdcardStateActive");
			target.find("#flashNow").addClass("nonner");
		}
	};
	var resetsdcardsSection = function() {
		$("#sdcardsSection").removeClass("sdcardsSectionActive").html("");
	};
	function bytesToSize(bytes) {
		var k = 1024;
		var sizes = [ 'Bytes', 'KB', 'MB', 'GB', 'TB' ];
		if (bytes === 0)
			return '0 Bytes';
		var i = parseInt(Math.floor(Math.log(bytes) / Math.log(k)), 10);
		return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];

	}
})(jQuery);
