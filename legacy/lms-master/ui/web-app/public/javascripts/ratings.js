var ratings = new function(){
    this.init = function(){
        $(".entityRating .emoji").off("click")
                                .on("click",feedbackPopup);
        $(".entityRating .feedback").off("click")
                                    .on("click",feedbackPopup);
        loadData();
    }

    var loadData = function(){
        var ratingsDiv= $(".ratingsDiv").data("entityRatings");
        if(ratingsDiv.rating != null)
        {
            $("."+ratingsDiv.rating.toLowerCase()+"Rating img").addClass("active");
            $(".feedback").html("We got your feedback").addClass("greenColor");
        }
        if(ratingsDiv.feedback != null){
            $(".formTextarea").val(ratingsDiv.feedback);
        }
        else{
            $(".feedback").html("Give your feedback").addClass("redColor");
        }
    }

    var feedbackPopup = function(){
        if(!$(this).hasClass("feedback")){
            $(".emoji").removeClass("active");
            $(this).addClass("active");
        }
        var popup = showVPopup();
        popup.append($(".ratingsPopup").clone());
        popup.find($(".ratingsPopup")).removeClass("nonner");
        popup.find(($(".popupEmojiHolder"))).append($(".ratingIcons").clone());
        popup.off("click")
             .on("click",".emoji",emojiActive)
             .on("click",".submitFeedback",submitFeedback);

        function emojiActive(){
            $(".emoji").removeClass("active");
            $(this).addClass("active");
            var rating = $(".active").data("rating").toLowerCase();
            $(".entityRating .emoji").removeClass("active");
            $(".entityRating ."+rating+"Rating .emoji").addClass("active");
        }
    }

    function submitFeedback(){
        var rating = $(".ratingsPopup .active").data('rating');
        var entityType = $(".ratingIcons").data("entityType");
        var entityId = $(".ratingIcons").data("entityId");
        var orgId= $("#myInstitutePage").data("orgId");
        var feedback = $(".formTextarea").val().trim();
        var params = {
            rating:rating,
            entity:{
                id:entityId,
                type:entityType
            },
            orgId:orgId
        };
        if(feedback != ""){
            params.feedback = feedback;
        }
        if(rating == "" || rating == null){
           showError("Please choose rating");
           return ;
        }
        vReq.post("/MyContents/addRatingAndFeedback",params,function(data){
            if(data.erroCode!=""){
                showError(data.errorMessage);
            }
            if(data.result!=null){
                closeVPopup();
                if(data.result.feedback != null){
                    $(".feedback").html("We got your feedback").removeClass("redColor").addClass("greenColor");
                    $(".formTextarea").val(data.result.feedback);
                }
                else{
                    $(".feedback").html("Give your feedback").removeClass("greenColor").addClass("redColor");
                }
                showMessage("Rating submitted successfully");
            }
        });
    }
}