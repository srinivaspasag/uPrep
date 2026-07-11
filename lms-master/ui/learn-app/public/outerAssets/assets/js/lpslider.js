var Microsite = new function(){
    var parDiv;
    var CLICK = "click.microsite";
    var CHANGE = "change.microsite";
    var MOUSEENTER = "mouseenter.microsite";
    var MOUSELEAVE = "mouseleave.microsite";
    this.init = function(){
        parDiv = $("#micro");
        $(document).off(CLICK)
        parDiv.off(CLICK)
            .on(MOUSEENTER,".siteFeatureBlock",mouseEnterFeature)
            .on(MOUSELEAVE,".siteFeatureBlock",mouseLeaveFeature)
        this.slides.start();
    };
    var mouseEnterFeature = function(){
        var $this = $(this);
        var title = $this.find(".siteFeatureName");
        var desc = $this.find(".siteFeatureDesc");
        title.animate({
            top:"-20px",
            opacity:0
        },250);
        desc.animate({
            top:"-20px",
            opacity:1
        },250);
    };
    var mouseLeaveFeature = function(){
        var $this = $(this);
        var title = $this.find(".siteFeatureName");
        var desc = $this.find(".siteFeatureDesc");
        title.animate({
            top:"0px",
            opacity:1
        },250);
        desc.animate({
            top:"0px",
            opacity:0
        },250);
    };
    this.slides = new function(){
        var slidesIntervalDur = 3000;
            var noOfSlides = 3;
            var curSlide = 0;
            var slidesIntervalObj;
        var textHolder,textSlides,tabletHolder,tabletSlides,mobileHolder,mobileSlides;
        var navHolder;
        var textWidth=0,mobileWidth=0,tabletWidth=0;
        var mouseleaveTimeoutObj;
        this.start = function(){
            parDiv = $("#main #usage")
            textHolder = parDiv.find(".siteSlideTextHolder");
            tabletHolder = parDiv.find("#slideTabletTable");
            mobileHolder = parDiv.find("#slideMobileTable");
                    textSlides = textHolder.find(".siteSlideTextTD");
            tabletSlides = tabletHolder.find(".siteSlideTabletTD");
            mobileSlides = mobileHolder.find(".siteSlideMobileTD");
                    navHolder = parDiv.find(".slide-dots");
            calcWidth();
            
                        startInterval();
                        parDiv.on("mouseenter",".slideImgsHolder,.siteSlideTextFrame",pauseSlideShow)
                            .on("mouseleave",".slideImgsHolder,.siteSlideTextFrame",function(){
                    if(mouseleaveTimeoutObj) clearTimeout(mouseleaveTimeoutObj);
                    mouseleaveTimeoutObj = setTimeout(function(){
                        startInterval(true);
                    },1000);
                })
                        .on(CLICK,'.slide-dot',dotClicked);
                    $(textSlides.get(curSlide)).fadeTo(200,1);
                    $(mobileSlides.get(curSlide)).fadeTo(200,1);
                    $(tabletSlides.get(curSlide)).fadeTo(200,1);
        };
        function calcWidth(){
            textWidth = $(textSlides.get(0)).width();
            mobileWidth = $(mobileSlides.get(0)).width();
            tabletWidth = $(tabletSlides.get(0)).width();
        };
        function pauseSlideShow(){
            if(mouseleaveTimeoutObj) clearTimeout(mouseleaveTimeoutObj);
                        if(slidesIntervalObj){ clearInterval(slidesIntervalObj); }
        };
        function startInterval(doSlide){
                        if(slidesIntervalObj){ clearInterval(slidesIntervalObj); }
            if(doSlide){
                            slideTo(curSlide+1);
            }
                        slidesIntervalObj = setInterval(function(){
                                slideTo(curSlide+1);
                        },slidesIntervalDur);
                };
        function slideTo(toSlide){
            var textLeft,mobileLeft,tabletLeft;
                        /*var textLeft = textHolder.position()["left"];
                        var mobileLeft = mobileHolder.position()["left"];
                        var tabletLeft = tabletHolder.position()["left"];*/
                        
            var currText = $(textSlides.get(curSlide));
                        var currMobile = $(mobileSlides.get(curSlide));
                        var currTablet = $(tabletSlides.get(curSlide));
            curSlide = toSlide;
                        if(curSlide == noOfSlides){
                                textLeft = mobileLeft = tabletLeft = 0;curSlide = 0;
                        }else{
                            textLeft = curSlide * textWidth * -1;
                            mobileLeft = curSlide * mobileWidth * -1;
                            tabletLeft = curSlide * tabletWidth * -1;
                        }
                        //$(loginSlides.get(curSlide)).fadeTo(0,1);
                        $(navHolder.find(".slide-dot").get(curSlide)).addClass("active").siblings().removeClass("active");       
                        textHolder.animate({"left":textLeft+"px"},400,function(){
                                /*currText.fadeTo(0,0);
                                $(textSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                        mobileHolder.animate({"left":mobileLeft+"px"},400,function(){
                                /*currMobile.fadeTo(0,0);
                                $(mobileSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                        tabletHolder.animate({"left":tabletLeft+"px"},400,function(){
                                /*currTablet.fadeTo(0,0);
                                $(tabletSlides.get(curSlide)).fadeTo(200,1);*/
                        });
                }
                var dotClicked = function(e){
                        var index = $(".slide-dots .slide-dot").index($(this));
                        slideTo(index);
                        startInterval();
                };
    };  
};
$(function(){
    Microsite.init();
});

