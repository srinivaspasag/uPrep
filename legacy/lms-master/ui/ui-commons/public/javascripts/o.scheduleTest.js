var scheduleTest = new function(){
    this.init = function(){
        disableInactiveTests();
        scheduleTest.startTimer();
    }

    var disableInactiveTests = function(){
        $(".showTimer").each(function(){
            if($(this).hasClass("timer-active")){
                $(this).closest(".moduleEntryContentType").addClass("disable-module-test-tab");
            }
        })
    }

    this.startTimer = function(){
        var timeHolder = $(".scheduleTimer");
        timeHolder.each(function(key){
            var remainingTime = $(this).data("timeBlockIn")/1000;
            var timer = $(this);
            remainingTime = parseInt(remainingTime,10);
            timerObj.set("scheduleTestTimer"+key,function(key){
                if (remainingTime < 60) {
                    clearInterval(timerObj[key]);
                    timer.closest(".moduleEntryContentType").removeClass("disable-module-test-tab");
                    timer.closest(".entityRemainingTimeBlock").remove();
                    return ;
                }
                remainingTime = remainingTime - 60;
                var hours = Math.floor(remainingTime/3600);
                var preHours = hours;
                if(hours < 10){
                    preHours = "0" + hours;
                }
                var minutes = ("0"+Math.floor((remainingTime-hours*3600)/60)).slice(-2);
                timer.text(preHours+":"+minutes+ " hrs");
            },60000);
        });
    }
}

$(function(){
    scheduleTest.init();
});