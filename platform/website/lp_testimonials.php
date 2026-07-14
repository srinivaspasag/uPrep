<style type="text/css">
  .float{
  position:fixed;
  width:60px;
  height:60px;
  bottom:40px;
  right:40px;
  background-color:#25d366;
  color:#FFF;
  border-radius:50px;
  text-align:center;
  font-size:30px;
  box-shadow: 2px 2px 3px #999;
  z-index:100;
}

.my-float{
  margin-top:16px;
}
</style>
<!--  <script>
  function initFreshChat() {
    window.fcWidget.init({
      token: "58a5790b-8932-4f7d-8938-b588c0afbf2c",
      host: "https://wchat.freshchat.com"
    });
  }
  function initialize(i,t){var e;i.getElementById(t)?initFreshChat():((e=i.createElement("script")).id=t,e.async=!0,e.src="https://wchat.freshchat.com/js/widget.js",e.onload=initFreshChat,i.head.appendChild(e))}function initiateCall(){initialize(document,"freshchat-js-sdk")}window.addEventListener?window.addEventListener("load",initiateCall,!1):window.attachEvent("load",initiateCall,!1);
</script> -->
<!-- TESTIMONIALS -->
<!-- <section class="testimonials"> -->
    <div class="container">

      <div class="row">
        <div class="col-sm-12">
          <div id="customers-testimonials" class="owl-carousel">

            <!--TESTIMONIAL 1 -->
            <div class="item">
              <div class="shadow-effect">
                <div class="icon m-a">
                                <img src='img/lp/newIndex/testimonials/2.jpg' alt="icon">
                            </div>
                <p>I had difficulty in retaining information while preparing for the JEE Main exam, that's when I was introduced to UPrep's ScoreJEE. It has helped me immensely to perform to my fullest and clear the JEE exams with ease. Its interactive feature makes learning fun. Time management is another factor that scares us - ScoreJEE's tests also improved my time management skills</p>
              </div>
              <div class="testimonial-name">DARSHAN KARRA</div>
            </div>
            <!--END OF TESTIMONIAL 1 -->
            <!--TESTIMONIAL 2 -->
            <div class="item">
              <div class="shadow-effect">
                 <div class="icon m-a">
                                <img src='img/lp/newIndex/testimonials/7.jpg' alt="icon">
                            </div>
                <p>I personally feel that the videos offered by UPrep help students in learning concepts faster than light. This not only helps for the exams such as IIT-JEE but also for SRM,VITEE,GITAM,etc
                </p>
              </div>
              <div class="testimonial-name">Aravind<br/>KMIIT Student, Hyderabad</div>
            </div>
            <!--END OF TESTIMONIAL 2 -->
                <!--TESTIMONIAL 3 -->
            <div class="item">
              <div class="shadow-effect">
                  <div class="icon m-a">
                                <img src='img/lp/newIndex/testimonials/3.jpg' alt="icon">
                            </div>
                <p>With ScoreJEE, one will not find it difficult to follow the lectures which one has missed. Its video lectures with classroom atmosphere can be accessed from the comfort of our homes. It has given me the confidence and clarity in many concepts.
                </p>
              </div>
              <div class="testimonial-name">RAAJ BORA</div>
            </div>
            <!--END OF TESTIMONIAL 3 -->
             <!--TESTIMONIAL 4 -->
            <div class="item">
              <div class="shadow-effect">
                  <div class="icon m-a">
                                <img src='img/lp/newIndex/testimonials/4.jpg' alt="icon">
                            </div>
                <p>UPrep's ScoreJEE is unique and innovative in its approach to identify the gaps for JEE preparations. Apart from that, the segregation of chapters into separate concepts helps us as students to understand the basics at large. Simple and clear explanation of concepts helped me score well in JEE Main examination.
                </p>
              </div>
              <div class="testimonial-name">DEEPAK BHARAMBE</div>
            </div>
            <!--END OF TESTIMONIAL 4 -->
          </div>
        </div>
      </div>
      </div>
    <!-- </section> -->
    <!-- END OF TESTIMONIALS -->

    <a href="https://api.whatsapp.com/send?phone=919160106323&text=Hey%20UPrep,%20I%20want%20to%20know%20more%20about%20the%20Foundation%20Pro%20program." class="float nonner" target="_blank">
<i class="fa fa-whatsapp my-float"></i>
</a>

<script type="text/javascript">
    jQuery(document).ready(function($) {
                "use strict";
                //  TESTIMONIALS CAROUSEL HOOK
                $('#customers-testimonials').owlCarousel({
                    loop: true,
                    center: true,
                    items: 4,
                    margin: 0,
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
                        items: 1
                      },
                      1170: {
                        items: 1
                      }
                    }
                });
            });
</script>
<!-- Start FB SDK -->
    <!-- Load Facebook SDK for JavaScript -->
      <div id="fb-root"></div>
      <script>
        window.fbAsyncInit = function() {
          FB.init({
            xfbml            : true,
            version          : 'v8.0'
          });
        };

        (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s); js.id = id;
        js.src = 'https://connect.facebook.net/en_US/sdk/xfbml.customerchat.js';
        fjs.parentNode.insertBefore(js, fjs);
      }(document, 'script', 'facebook-jssdk'));
  </script>

  <!-- Your Chat Plugin code -->
      <div class="fb-customerchat" attribution=setup_tool page_id="107898660930733" theme_color="#008080" logged_in_greeting="Hi! How can we help you?" logged_out_greeting="Hi! How can we help you?"></div>
<!-- End FB SDK -->