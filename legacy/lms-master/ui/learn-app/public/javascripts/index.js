$(document).ready(function() {
    /*----------- wow animation with support of wow.js and animation.css ----------------*/
    var wow = new WOW({
        boxClass: 'wow', // animated element css class (default is wow)
        animateClass: 'animated', // animation css class (default is animated)
        offset: 0, // distance to the element when triggering the animation (default is 0)
        mobile: false // trigger animations on mobile devices (true is default)
    });
    wow.init();
    /*----------- Google Map - with support of gmaps.js ----------------*/
    function isMobile() {
        return ('ontouchstart' in document.documentElement);
    }

    function init_gmap() {
        if (typeof google == 'undefined') return;
        var options = {
            center: [17.41548, 78.45223],
            zoom: 15,
            mapTypeControl: true,
            mapTypeControlOptions: {
                style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
            },
            navigationControl: true,
            scrollwheel: false,
            streetViewControl: true
        }
        if (isMobile()) {
            options.draggable = false;
        }
        $('#googleMaps').gmap3({
            map: {
                options: options
            },
            marker: {
                latLng: [17.41548, 78.45223],
                options: {
                    icon: 'assets/images/mapicon.png'
                }
            }
        });
    }
    init_gmap();
    jQuery(document).ready(function($) {
        "use strict";
        /*---------------------- Current Menu Item -------------------------*/
        $('#main-menu #headernavigation').onePageNav({
            currentClass: 'active',
            changeHash: false,
            scrollSpeed: 750,
            scrollThreshold: 0.5,
            scrollOffset: 160,
            filter: ':not(.sub-menu a, .not-in-home)',
            easing: 'swing'
        });
        /*------------------- Testimonials Client Slider ----------------------*/
        var image_array = new Array();
        image_array = [{
                image: '/public/outerAssets/images/testimonials-client/7.jpg'
            },
            // image for the first layer, goes with the text from id="sw0"
            {
                image: '/public/outerAssets/images/testimonials-client/2.jpg'
            },
            // image for the second layer, goes with the text from id="sw1"
            {
                image: '/public/outerAssets/images/testimonials-client/3.jpg'
            },
            // image for the third layer, goes with the text from id="sw2"
            {
                image: '/public/outerAssets/images/testimonials-client/4.jpg'
            },
            // ...
            {
                image: '/public/outerAssets/images/testimonials-client/utham.png'
            }, {
                image: '/public/outerAssets/images/testimonials-client/6.jpg'
            }, {
                image: '/public/outerAssets/images/testimonials-client/vaibhav.jpg'
            }
        ];
        $('#testimonial-slider').content_slider({ // bind plugin to div id="slider1"
            map: image_array, // pointer to the image map
            max_shown_items: 7, // number of visible circles
            hv_switch: 0, // 0 = horizontal slider, 1 = vertical
            active_item: 0, // layer that will be shown at start, 0=first, 1=second...
            wrapper_text_max_height: 450, // height of widget, displayed in pixels
            middle_click: 1, // when main circle is clicked: 1 = slider will go to the previous layer/circle, 2 = to the next
            under_600_max_height: 1200, // if resolution is below 600 px, set max height of content
            border_radius: -1, // -1 = circle, 0 and other = radius
            automatic_height_resize: 1,
            border_on_off: 0,
            allow_shadow: 0
        });
    });
    (function($) {
        $.fn.countTo = function(options) {
            // merge the default plugin settings with the custom options
            options = $.extend({}, $.fn.countTo.defaults, options || {});
            // how many times to update the value, and how much to increment the value on each update
            var loops = Math.ceil(options.speed / options.refreshInterval),
                increment = (options.to - options.from) / loops;
            return $(this).each(function() {
                var _this = this,
                    loopCount = 0,
                    value = options.from,
                    interval = setInterval(updateTimer, options.refreshInterval);

                function updateTimer() {
                    value += increment;
                    loopCount++;
                    $(_this).html(value.toFixed(options.decimals));
                    if (typeof(options.onUpdate) == 'function') {
                        options.onUpdate.call(_this, value);
                    }
                    if (loopCount >= loops) {
                        clearInterval(interval);
                        value = options.to;
                        if (typeof(options.onComplete) == 'function') {
                            options.onComplete.call(_this, value);
                        }
                    }
                }
            });
        };
        $.fn.countTo.defaults = {
            from: 0, // the number the element should start at
            to: 100, // the number the element should end at
            speed: 1000, // how long it should take to count between the target numbers
            refreshInterval: 100, // how often the element should be updated
            decimals: 1, // the number of decimal places to show
            onUpdate: null, // callback method for every time the element is updated,
            onComplete: null, // callback method for when the element finishes updating
        };
    })(jQuery);
    jQuery(function($) {
        $('.timer').countTo({
            from: 0,
            to: 1.2,
            speed: 3500,
            refreshInterval: 50,
            onComplete: function(value) {
                console.debug(this);
            }
        });
    });
    $('#signup').on('click', function() {
        ga('send', 'event', 'button', 'click', 'Signup');
    });
});
$(document).ready(function() {
    var id = '#dialog';
    //Get the screen height and width
    var maskHeight = $(document).height();
    var maskWidth = $(window).width();
    //Set heigth and width to mask to fill up the whole screen
    $('#mask').css({
        'width': maskWidth,
        'height': maskHeight
    });
    //transition effect
    $('#mask').fadeIn(1000);
    $('#mask').fadeTo("slow", 0.8);
    //Get the window height and width
    var winH = $(window).height();
    var winW = $(window).width();
    //Set the popup window to center
    $(id).css('top', winH / 2 - $(id).height() / 2);
    $(id).css('left', winW / 2 - $(id).width() / 2);
    //transition effect
    $(id).fadeIn(2000);
    //if close button is clicked
    $('.window .close').click(function(e) {
        //Cancel the link behavior
        e.preventDefault();
        $('#mask').hide();
        $('.window').hide();
    });
    //if mask is clicked
    $('#mask').click(function() {
        $(this).hide();
        $('.window').hide();
    });
});