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
                <p>Manoj sir explains topics and points so well that my daughter, Kavya, understands them very clearly. He is very open to doubts, criticism and encourages the student to ask questions. He very patiently breaks down concepts into easy explanations which makes the topic very understandable to her. He never discourages any student even if they put forth their confusions repeatedly.
                <br/><br/>
                In this time of pandemic, for students in class 6-10, these are crucial years to form their base in the sciences and mathematics. In the absence of regular school, UPrep and its excellent teachers are a great blessing!</p>
              </div>
              <div class="testimonial-name">Divya Parmar<br/>
Mother of Kavya (Class 8)</div>
            </div>
            <!--END OF TESTIMONIAL 1 -->
            <!--TESTIMONIAL 2 -->
            <div class="item">
              <div class="shadow-effect">
                <p>It's been a pleasure to have my daughter in UPrep's Refresher Program. All the classes are very well organized and the faculty mingle very well with students. They are able to come down to the standard of the students and explain in detail. All the doubts are clearly explained and individual caring is observed in al classes. Special thanks to all teachers who are creating interest in improving learning ability and building good foundation to students.
                <br/><br/>
                My child is enjoying the classes and showing interest in attending and learning the subjects. Thanks again to Krishna sir and the UPrep team.
                </p>
              </div>
              <div class="testimonial-name">Jaya Vani <br/>Mother of Veda (Class 8)</div>
            </div>
            <!--END OF TESTIMONIAL 2 -->
                <!--TESTIMONIAL 3 -->
            <div class="item">
              <div class="shadow-effect">
                <p>Sakshath attended 45-day Refresher Course in Math and Science conducted by UPrep. This course was very useful in strengthening the concepts learnt in 7th grade and has improved his fundamentals. The course was handled by highly experienced and knowledgeable teachers and added to that, the online UPrep app ensured seamless learning. Thank you!
                </p>
              </div>
              <div class="testimonial-name">Dr. B.R. Shamanna<br/>Father of Sakshath (Class 8)</div>
            </div>
            <!--END OF TESTIMONIAL 3 -->
             <!--TESTIMONIAL 4 -->
            <div class="item">
              <div class="shadow-effect">
                <p>I am writing to you to express our appreciation to the team at UPrep for their efforts over the 45 days Refresher Program. We are so glad that we took this decision as we see our daughter grow in confidence.My daughter has really benefitted from the Refresher Program for Class 8. A platform, which offered choice to its students, encouraged students to think for themselves and looked at students holistically.
<br>The most noticeable thing about this program was that my daughter has grown immensely in self-confidence. She enjoyed attending the classes every day.I think your USP is to strengthen student’s foundation in Math and Science without mental stress! You have managed to achieve a perfect balance between concepts and activities.
<br>Keep teaching and keep growing! Wishing you all the very best for the future endeavors.
                </p>
              </div>
              <div class="testimonial-name">Vivekanand Raula<br/>Father of Akshainie (Class 8)</div>
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