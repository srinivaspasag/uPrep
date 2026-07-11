var entityReview = new function(){
    var entityReview = $("#entityReviews").data();
    var mcWidget;
    var params = {
        entityType:entityReview.entityType,
        entityId:entityReview.entityId,
        start:entityReview.start,
        size:entityReview.size,
        orgId:entityReview.orgId
    };
    this.init = function(){
        var reviewType = $(".active").data("reviewType").toLowerCase();
        $(".pagin").addClass("hidden");
        mcWidget = $("."+reviewType+"Review");
        $("#entityReviews").off("click")
                           .on("click",".entityReviewType",entityReviewType);
        params.ratingType = $(".active").data("reviewType").toUpperCase();
        mcWidget.find(".pagin").removeClass("hidden");
        initmcWidgetforCMDS(mcWidget,"/QrTests/getCMDSEntityReviews",params, false, true);
    }
    var entityReviewType = function(){
        $(".pagin").addClass("hidden");
        params.start =0;
        $(".reviewDiv").addClass("nonner");
        $(".entityReviewType").removeClass("active");
        var reviewType = $(this).data("reviewType").toLowerCase();
        params.ratingType = reviewType.toUpperCase();
        $(this).addClass("active");
        $("."+reviewType+"Review").removeClass("nonner");
        mcWidget = $("."+reviewType+"Review");
        vReq.post("/QrTests/getCMDSEntityReviews",params,function(data){
            mcWidget.html(data);
            mcWidget.find(".pagin").removeClass("hidden");
            initmcWidgetforCMDS(mcWidget,"/QrTests/getCMDSEntityReviews",params, false, true);
        });
    }
}