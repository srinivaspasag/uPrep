var modules = new (function($) {
    var modulePage, moduleEntriesHolder, topicDotsLiner;
    this.init = function(params) {
        modulePage = $("#modulePage");
        moduleEntriesHolder = $("#moduleEntriesHolder");
        topicDotsLiner = $("#topicDotsLiner");

        modulePage.on("click", ".moduleContentNoAccess", moduleContentNoAccess)
        if (params.userRole === "STUDENT") {
            setUpProgressBar(params);
        }
        putCountForContent();
        putCountForTopics();        
        // var maxWidth=$('.moduleEntryLeftSec').width()-60;
        // $('.moduleEntryLeftSec .singleLineText').css({'max-width':maxWidth,display:"inline-block"});        
    };

    //events
    var moduleContentNoAccess = function(e) {
        showError("Please access the content in sequential order");
        e.stopPropagation();
        return false;
    };

    var setUpProgressBar = function(params) {
        if (params.compulsoryContentCount > 0) {
            var percent = parseInt(params.completedCompulsoryContentCount * 100 / params.compulsoryContentCount);
            var maxWidth = modulePage.find("#moduleProgressBlackBar").width();
            modulePage.find("#moduleProgressGreenBar").width(maxWidth * percent / 100);
            modulePage.find("#moduleProgressText").text("Progress " + percent + "%");
        }else{
            $("#moduleProgressText").parent().html("");
        }
    };


    //utils
    var putCountForContent = function() {
        var allContents = moduleEntriesHolder.children(".moduleEntryContentType");
        for (var k = 0, l = allContents.length; k < l; k++) {
            var entry = allContents.eq(k);
            entry.find(".moduleContentNo").text((k + 1) + ".");
        }
    };
    var putCountForTopics = function() {
        var allTopics = moduleEntriesHolder.children(".moduleEntryTopicType");
        for (var k = 0, l = allTopics.length; k < l; k++) {
            var entry = allTopics.eq(k);
            entry.find(".moduleEntryTopicCircle").text(indexToChar(k).toUpperCase());
        }
        if (modulePage.offset().left === 0) {
            //TODO bad fix, need to be looked into
            setTimeout(putLinerForTopics, 1000);
        } else {
            putLinerForTopics();
        }
    };
    var getChar=function(count){
        if(count>26){
            var firstChar=a[(parseInt(count-1/26))-1];
            var secondChar=a[(count-1)%26];
            return (firstChar+secondChar).toUpperCase();
        }else{
            return a[count-1].toUpperCase();
        }
    };
    var putLinerForTopics = function() {
        var m = modulePage.offset();
        var topics = moduleEntriesHolder.children(".moduleEntryTopicType");
        var topTopicDot = topics.first().find(".moduleEntryTopicCircle");
        var topTopicDotOffset = topTopicDot.offset();
        if (!topTopicDotOffset) {
            return;
        }
        var x1 = topTopicDotOffset.left - m.left;
        var y1 = topTopicDotOffset.top - m.top;
        var finalTopDimen = y1 + topTopicDot.outerHeight();
        topicDotsLiner.css({left: x1 + (topTopicDot.outerWidth() / 2) - 2, top: finalTopDimen});
        var bottomTarget = $("#moduleProgressBlackBar");
        if (bottomTarget.length === 0) {
            var lastTopic = topics.last();
            var firstContentAfterLastTopic = lastTopic.nextAll(".moduleEntryContentType");
            if (firstContentAfterLastTopic.length > 0) {
                bottomTarget = firstContentAfterLastTopic;
            } else {
                bottomTarget = lastTopic;
            }
        }
        var bottomTargetOffset = bottomTarget.offset();
        var y2 = bottomTargetOffset.top - m.top;
        topicDotsLiner.height(y2 - finalTopDimen);
    };
})(jQuery);

