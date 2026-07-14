<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta title="UPrep Live Online Courses App | Foundation Courses, Math, Science, JEE, NTSE">
    <meta name="description" content="Uprep Live Online Courses For Classes 6 to 10,Foundation Course, Math, Science, Olympiads, JEE Foundation, NTSE, Online Program and India First Integrated Platform For Students. Students Easy Learning App">
    <!-- <meta name="keywords" content="Foundation Courses, online tuition, online teacher, online tutor, online tutoring, online teaching, Online Coaching Courses and Classes 6 to 10, Live Online Classes, IIT JEE coaching,NTSE, Stress free Environment, Easy Learning APP, Olympiads Classes,IIT, JEE,Online Math Classes, Online Science Classes"> -->
    <meta name="Keywords" content="national science olympiad, nso olympiad, ijso, math olympiad, science olympiad foundation, international mathematical olympiad, math olympiad 2019, olympiad exam 2019, international math olympiad, pre regional mathematics olympiad, science olympiad 2019, science olympiad, crest olympiads, green olympiad, olympiad 2019, regional mathematics olympiad 2019, olympiad, international science olympiad, imo olympiad, physics olympiad, hbcse olympiad, international math olympiad 2019, international math olympiad 2020, national olympiad, nso exam, national engineering olympiad, mathematics olympiad, international physics olympiad, olympiad exam, english olympiad, imo exam">
     <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="css/reset.css">
    <link rel="stylesheet" href="css/utilities.css">
    <link rel="stylesheet" href="css/aos.css">
    <link rel="stylesheet" href="css/main.css">
     <link rel="stylesheet" href="css/form.css">
     <link rel="stylesheet" href="css/index-style.css">
     <link rel="stylesheet" href="css/exams.css">


    <link rel="icon" type="image/png" sizes="32x32" href="assets/images/favicons/72.png">
    <link rel="icon" type="image/png" sizes="16x16" href="assets/images/favicons/72.png">

    <script src="js/s.js"></script>
    <style type="text/css">
    @media only screen and (min-width: 800px) {
     header>div {
        padding: 1rem 1rem;
    }
}

    </style>
    <title>UPrep Live Online Courses App | Foundation Courses, Math, Science, JEE, NTSE</title>
    <!-- Global site tag (gtag.js) - Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=UA-171772510-1"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', 'UA-171772510-1');
</script>
<script src='//cdn.freshmarketer.com/665538/1716619.js'></script>
<!-- Facebook Pixel Code --><script>!function(f,b,e,v,n,t,s){if(f.fbq)return;n=f.fbq=function(){n.callMethod?n.callMethod.apply(n,arguments):n.queue.push(arguments)};if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';n.queue=[];t=b.createElement(e);t.async=!0;t.src=v;s=b.getElementsByTagName(e)[0];s.parentNode.insertBefore(t,s)}(window, document,'script','https://connect.facebook.net/en_US/fbevents.js');fbq('init', '1567573663463265');fbq('track', 'PageView');</script><noscript><img height="1" width="1" style="display:none"src="https://www.facebook.com/tr?id=1567573663463265&ev=PageView&noscript=1"/></noscript><!-- End Facebook Pixel Code -->
</head>
<body>
    <div class="inner-body">
        <?php require_once('header.php')?>

       
                        
                    </div>
           
  <div class="jee" id="jee">
  
<?php include_once('exams/jee.php')?>
</div>    

<div class="neet" id="neet">
  
<?php include_once('exams/neet.php')?>
</div>


<div class="imo" id="imo">
  
<?php include_once('exams/imo.php')?>
</div> 

<div class="kypy" id="kypy">
  
<?php include_once('exams/kypy.php')?>

</div>   

<div class="nso" id="nso">
  
<?php include_once('exams/nso.php')?>
</div>

<div class="nstse" id="nstse">
  
<?php include_once('exams/nstse.php')?>
</div>
  <div class="ntse" id="ntse">


<?php include_once('exams/ntse.php')?>
  
</div>  

        <?php include_once('footer.php')?>
    </div>
    <script src="js/app.js"></script>
    <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script type="text/javascript" src="js/loadModel.js"></script>

    <script type="text/javascript">
        $(".nav-inner").find(".nav-list .exams").addClass("active");
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
  <!-- This site is converting visitors into subscribers and customers with OptinMonster - https://optinmonster.com-->
<script type="text/javascript" src="https://a.omappapi.com/app/js/api.min.js" data-account="90623" data-user="80589" async></script>
<!-- / OptinMonster -->
</body>
</html>

<script type="text/javascript">
  $(".read-more-link").on('click',function(){
    console.log($(this));
    var readMore = $(this).data("readMore");
    console.log(readMore);
    if(readMore === "yes"){
      $(this).parent().find(".show-more-text").css("display","block");
      $(this).data("readMore","no");
      $(this).text("Read Less");
    }
    else{
      $(this).data("readMore","yes");
      $(this).parent().find(".show-more-text").css("display","none");
      $(this).text("Read More");
    }
  });
</script>