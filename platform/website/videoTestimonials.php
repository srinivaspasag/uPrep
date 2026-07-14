<div class="container" >
    <div class="row">
      <!-- <h2 class="heading c-white" style="margin-top: 40px;text-align: center;margin-bottom: 40px;">Watch Our Videos</h2> -->
    </div>
    <div class="row">
    <div id="student-video-testimonials" class="owl-carousel">
      <div class="item">
        <div class="video-holder">
          <div class="img-thumbnail-holder">
            <img class="video video-img img-responsive m-auto" id="vid-1" data-url="https://program-prod-uprep.s3.ap-south-1.amazonaws.com/veda_sruthi.mp4" onerror="this.onerror=null;this.src='img/t1_thumb.png';" src="img/t1_thumb.webp" alt="video" >
            <div class="play-button"><img src="img/play.png" width="70px" height="70px"/></div>
          </div>
          <div class="student-name">Veda Shruti<br/>Class 7</div>
        </div>
      </div>
      <div class="item">
        <div class="video-holder">
          <div class="img-thumbnail-holder">
            <img class="video video-img img-responsive m-auto" id="vid-2" data-url="https://program-prod-uprep.s3.ap-south-1.amazonaws.com/akshainie.mp4" onerror="this.onerror=null;this.src='img/t2_thumb.png';" src="img/t2_thumb.webp" alt="video">
            <div class="play-button"><img src="img/play.png" width="70px" height="70px"/></div>
          </div>
          <div class="student-name">Akshainie<br/>Class 8</div>
        </div>
      </div>
      <div class="item">
        <div class="video-holder">
          <div class="img-thumbnail-holder">
            <img class="video video-img img-responsive m-auto" id="vid-2" data-url="https://program-prod-uprep.s3.ap-south-1.amazonaws.com/nimarta.mp4" onerror="this.onerror=null;this.src='img/t3_thumb.png';" src="img/t3_thumb.webp" alt="video">
            <div class="play-button"><img src="img/play.png" width="70px" height="70px"/></div>
          </div>
          <div class="student-name">Nimarta<br/>Class 8</div>
        </div>
      </div>
      <div class="item">
        <div class="video-holder">
          <div class="img-thumbnail-holder">
            <img class="video video-img img-responsive m-auto" id="vid-3" data-url="https://program-prod-uprep.s3.ap-south-1.amazonaws.com/naisha.mp4" onerror="this.onerror=null;this.src='img/t4_thumb.png';" src="img/t4_thumb.webp" alt="video">
            <div class="play-button"><img src="img/play.png" width="70px" height="70px"/></div>
          </div>
          <div class="student-name">Naisha<br/>Class 7</div>
        </div>
      </div>
      <div class="item">
        <div class="video-holder">
          <div class="img-thumbnail-holder">
            <img class="video video-img img-responsive m-auto" id="vid-3" data-url="https://program-prod-uprep.s3.ap-south-1.amazonaws.com/unmana.mp4" onerror="this.onerror=null;this.src='img/t5_thumb.png';" src="img/t5_thumb.webp" alt="video">
            <div class="play-button"><img src="img/play.png" width="70px" height="70px"/></div>
          </div>
          <div class="student-name">Unmana Pathak<br/>Class 7</div>
        </div>
      </div>
    </div>
    </div>
  </div>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@9"></script>
  <script type="text/javascript">
    jQuery(document).ready(function($) {
                "use strict";
                //  TESTIMONIALS CAROUSEL HOOK
                $('#student-video-testimonials').owlCarousel({
                    loop: true,
                    center: true,
                    items: 3,
                    margin: 10,
                    autoplay: true,
                    dots:true,
                    nav:true,
                        navText: [
                      '<i class="fa fa-angle-left" aria-hidden="true"></i>',
                      '<i class="fa fa-angle-right" aria-hidden="true"></i>'
                  ],
                    autoplayTimeout: 8500,
                    smartSpeed: 450,
                    responsive: {
                      0: {
                        items: 1
                      },
                      768: {
                        items: 2
                      },
                      1170: {
                        items: 3
                      }
                    }
                });
            });

    $(".img-thumbnail-holder").off("click")
                                  .on("click",loadVideoInModel)

    function loadVideoInModel(){
        $this = $(this).find(".video");
        Swal.fire({
            width:"42rem",
            html:
            '<div class="embed-responsive embed-responsive-16by9 videoPlayer">'
            +'  <video width="100%" height="315" controls playsinline autoplay id="myVideo" muted>'
            +'<source src="'+$this.data("url")+'">'
            +'</source>'
            +'  </video>'
            +'</div>',
            showConfirmButton: false,
            showCloseButton: true,
            customClass: "videoPopup",
            onClose : function(){
                $('.videoPlayer').remove();
            }
        });
        document.getElementById("myVideo").muted =false;
    }
</script>