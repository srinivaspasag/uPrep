var loadModel = new function(){
    this.init = function(){
        // loadSwalModel();
    }

    var loadSwalModel = function(){
        setTimeout(function(){
            Swal.fire({
                width:"64rem",
                showConfirmButton:false,
                showCloseButton:true,
                allowOutsideClick:false,
                allowEscapeKey:false,
                allowEnterKey:false,
                focusConfirm:false,
                html: "<div class='show-banner'><a target='_blank' href='https://forms.gle/MdhKTmf8FadBrwue9'><img src='https://program-prod-uprep.s3.ap-south-1.amazonaws.com/webinar_banner.png' alt='uprep-banner-image'/></a></div>",
            });
            // Swal.fire({
            //     html:"<iframe class='google-form nonner' src='https://docs.google.com/forms/d/e/1FAIpQLSeIYsUa-PIieap_TNqM1ktlJBUJ-ArfLY4CMbNWQ0IhFhiXwg/viewform?embedded=true' width='100%' height='520' frameborder='0' marginheight='0' marginwidth='0' onload='loadModel.iframeLoad()'>Loading…</iframe>",
            //     showConfirmButton:false,
            //     width:"60rem",
            //     onBeforeOpen: function(el){
            //        loadModel.showLoader(el);
            //     },
            //     showCloseButton: true,
            //     allowOutsideClick:false,
            //     allowEscapeKey:false,
            //     allowEnterKey:false,
            //     focusConfirm:false
            // });
        },5000);
    }

    // this.iframeLoad = function(){
    //     this.hideLoader();
    //     $(".google-form").removeClass("nonner");
    // }

    // this.showLoader = function(el){
    //     $(el).append("<div class='showLoader' style='font-size:1.5rem;text-align: center;padding: 2rem 0rem;color:#333;'><div class='content'>Kindly Submit your details here <span class='dots'>...</span></div></div>")
    // }

    // this.hideLoader = function(){
    //     $(".showLoader").addClass("nonner");
    // }
}



$(document).ready(function(){
    loadModel.init();
});