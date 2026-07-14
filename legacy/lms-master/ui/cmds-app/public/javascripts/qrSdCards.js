var qrSdCards = new function(){
	var parDiv;
	var CLICK = "click.qrSdCards";
	var currCardId;
	var noOfCards;
	this.init = function(noOfSdCards){
		parDiv = $("#sdCardGroupPage");
		noOfCards = noOfSdCards;
		parDiv.off(CLICK)
			.on(CLICK,".sdgCancel,.sdgSaveBtn",goBack)
			.on(CLICK,".sdgEachCard",selectSdCard)
			.on(CLICK,".sdgPrevArrow",prevSdCard)
			.on(CLICK,".sdgNextArrow",nextSdCard)
			.on(CLICK,".moveSdCardContent",moveSdCardContent)
		currCardId = parDiv.find(".sdgEachCard.active").data("id");
		fetchContents(currCardId);
	};
	var goBack = function(){
		history.back();
	};
	var afterLoad = function() {
        calcTitleMaxWidth();
    };
	var selectSdCard = function(){
		var $this = $(this);
		if($this.hasClass("active") || $this.hasClass("disable")){ return; }
		$this.addClass("active").siblings().removeClass("active");
		var cardId = $this.data("id");
		if(cardId){
			fetchContents(cardId);
		}
	};
	var prevSdCard = function(){
		var $this = $(this);
		moveNextPrevSdCards($this,"PREV");
	};
	var nextSdCard = function(){
		var $this = $(this);
		moveNextPrevSdCards($this,"NEXT");
	}
	var moveNextPrevSdCards = function($this,DIRECTION){
		if($this.hasClass("disable")) return;
		var sdgEachCards = parDiv.find(".sdgEachCard");
		var total = sdgEachCards.length;
		var width = sdgEachCards.outerWidth(true)+1;
		var holderWidth = parDiv.find(".sdgCards").width();
		var scroller = parDiv.find(".sdgCardsScroll");
		var scrollerWidth = total * width;
		var scrollerLeft = scroller.data("left");
		scrollerLeft = scrollerLeft ? scrollerLeft : 0;
		var diff = (scrollerLeft+scrollerWidth)-holderWidth;
		var moveLeftBy = scrollerLeft;
		if(diff>0 && DIRECTION == "NEXT"){
			moveLeftBy -= diff>width?width:diff;
		}else if(scrollerLeft<0 && DIRECTION == "PREV"){
			moveLeftBy = width>(-1*scrollerLeft)?0:moveLeftBy+width;
		}
		scroller.animate({left:moveLeftBy},250,function(){
			scroller.data("left",moveLeftBy);
		});
		if(((moveLeftBy+scrollerWidth)-holderWidth)<=0){
			parDiv.find(".sdgNextArrow").addClass("disable");
		}else{
			parDiv.find(".sdgNextArrow").removeClass("disable");
		}
		if(moveLeftBy>=0){
			parDiv.find(".sdgPrevArrow").addClass("disable");
		}else{
			parDiv.find(".sdgPrevArrow").removeClass("disable");
		}
	};
	var fetchContents = function(cardId){
		var holder = parDiv.find(".sdgContentsHolder");	
		smallLoader(holder);
		var params = {
			totalCards : noOfCards,
			id : cardId,
			start : 0,
			size : 100,
			orderBy : "customOrder",
			sortOrder : "ASC"
		};
		vReq.get("/QrExports/getSdCardContents",params,function(data){
			holder.html(data);
			holder.find(".pagin").removeClass("nonner")
			calcTitleMaxWidth();
			mcWidget = $("#sdCardContents");
			initmcWidgetforCMDS(mcWidget,"/QrExports/getSdCardContents",params, false, true, afterLoad);
		});
	};
	var moveSdCardContent = function(){
		var $this = $(this);
		var tr = $this.closest(".sectionContentEntity");
		var CLICK = "click.qrSdCardsMove";
		var entity = {
			id : tr.data("entityId"),
			type : tr.data("entityType")
		};
		var movingBottomDiv = $("#moveSdContentInfo");
		var itemNameDiv = movingBottomDiv.find(".reorderingItem");
		itemNameDiv.html(tr.find(".entityNameDiv").clone());
		itemNameDiv.find(".incompleteStateTag").remove();
		var blackout = $("#sdgContentBlackout");
		var sdCardsHolder = parDiv.find(".sdgCardsHolder");
		sdCardsHolder.addClass("moveContent");
		blackout.fadeTo(250,1,function(){
			sdCardsHolder.find(".sdgEachCard").addClass("disable");
			blackout.off(CLICK)
				.on(CLICK,".closeContentMove",closeMove)
			sdCardsHolder.off(CLICK);
			sdCardsHolder.on(CLICK,".sdgEachCard",onMove);
		});
		function closeMove(){
			blackout.off(CLICK);
			sdCardsHolder.off(CLICK);
			blackout.fadeTo(250,0,function(){
				sdCardsHolder.removeClass("moveContent");
                        	sdCardsHolder.find(".sdgEachCard").removeClass("disable");
				$(this).hide();
			});
		};
		function onMove(){
			var $this = $(this);
			if($this.hasClass("active")){ 
				showError(i18nJS("SD_MOVE_CONTENT_SRC_DEST_SAME"));
				return; 
			}
			var params = {
				content : entity,
				moveFromSDCardId : sdCardsHolder.find(".sdgEachCard.active").data("id"),
				moveToSDCardId : $this.data("id")
			};
			showTopLoader();
			vReq.get("/QrExports/sdCardContentMove",params,function(data){
				showMessage(i18nJS("TXT_SUCCESS"));
				closeMove();
				tr.remove();
				setTimeout(function(){
					refreshPage();
				},500);
			});
		};
	};
    var calcTitleMaxWidth = function(targetDiv) {
        if (!targetDiv) {
            targetDiv = parDiv;
        }
        var tableHead = targetDiv.find(".headSecTable");
        var tableBody = targetDiv.find(".cmdsTable");
        var entityNameDiv = tableBody.find(".entityNameDiv");
        var entityNameWidth = tableHead.find(".itemTitleTH").width() - 10;
        var entityNameMaxWidth = entityNameWidth - 30;
        entityNameDiv.width(entityNameWidth)
                .find(".singleLineText").css("max-width", entityNameMaxWidth);
    };
};
