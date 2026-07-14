var pricing = new function(){
    this.init = function(){
        initializeValues();
        $(".userSelect").on("change",initializeValues);
        $(".pricingBasis").on("change",initializeValues);
        $(".tab").off("click",initializeValues)
                .on("click",initializeValues);

        $(".goToPack").off("click")
                      .on("click",goToPack);
        $(".modal").on("click",".submit-btn",validation);
          function validation(){
            var name = $("#full-name");
            var email = $("#email");
            var number = $("#number");
            var message = $("#message");
            var validate = true;
            if(name.val()=="" || name.val() == null){
              $("#name-group").addClass("has-error");
              $("#name-dialog").addClass("zmdi-alert-triangle");
              $("#name-dialog").css("display","inline-block");
              $("#name-help").html("Please Enter Name!");
              validate = false;
            }
            else{
              $("#name-group").removeClass("has-error");
              $("#name-dialog").removeClass("zmdi-alert-triangle");
              $("#name-dialog").css("display","none");
              $("#name-help").css("display","none");
            }
            if(email.val()=="" || email.val() == null || validateEmail(email.val())===false){
              $("#email-group").addClass("has-error");
              $("#email-dialog").addClass("zmdi-alert-triangle");
              $("#email-dialog").css("display","inline-block");
              $("#email-help").html("Please Enter Correct Email!");
              validate = false;
            }
            else
            {
              $("#email-group").removeClass("has-error");
              $("#email-dialog").removeClass("zmdi-alert-triangle");
              $("#email-dialog").css("display","none");
              $("#email-help").css("display","none");
            }

            if(number.val()=="" || number.val() == null){
              $("#number-group").addClass("has-error");
              $("#number-dialog").addClass("zmdi-alert-triangle");
              $("#number-dialog").css("display","inline-block");
              $("#number-help").html("Please Enter Contact Number!");
              validate = false;
            }
            else{
              $("#number-group").removeClass("has-error");
              $("#number-dialog").removeClass("zmdi-alert-triangle");
              $("#number-dialog").css("display","none");
              $("#number-help").css("display","none");
            }

            if(message.val()=="" || message.val() == null){
              $("#message-group").addClass("has-error");
              $("#message-dialog").addClass("zmdi-alert-triangle");
              $("#message-dialog").css("display","inline-block");
              $("#message-help").html("Please Enter Query!");
              validate = false;
            }
            else{
              $("#message-group").removeClass("has-error");
              $("#message-dialog").removeClass("zmdi-alert-triangle");
              $("#message-dialog").css("display","none");
              $("#message-help").css("display","none");
            }


            if(validate){
             var params = {
              "name" : name.val(),
              "email" : email.val(),
              "message" : message.val(),
              "number" : number.val(),
              "fromForm" : "PRICING"
            };
            $.post("Application/sendEmail", params,function(result){
              result = JSON.parse(result);
              if(result.result == null){
                swal({
                  title: "Something went wrong",
                  type: "error",
                  timer:5000
                });
                return;
              }
              if(result.result.success === true){
                swal({
                  title: "Message Sent",
                  type: "success",
                  timer:5000
                });
                $("#full-name").val("");
                $("#email").val("");
                $("#number").val("");
                $("#message").val("");
              }else{
                swal({
                  title: "Message Not Sent",
                  type: "error",
                  timer:5000
                });
              }
            });
            setTimeout(function(){
                $('#modalDefault .btn-link').click();
            },2000);
          }
        }
        function validateEmail(sEmail) {

          var filter = /^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;
          if (filter.test(sEmail)) {
            return true;
          }
          else {
            return false;
          }
        }
        $(".viewMore").on("click",function(){
            $('html, body').animate({
            scrollTop: $("#comparePlanTable").offset().top -100
          }, 1000)
        });
        var planFeatures = [
        {
            "Features": "ADMIN FEATURES",
            "rowClass":"colspan"
        },
        {
            "Features": "Teachers | Editors | Sales Person | Managers | Students | Admin",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Invite New Learners",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Access Learner Performance",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Add Learners to Programs",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        // {
        //     "Features": "Generate Learner Report",
        //     "Lite": "Yes",
        //     "Standard": "Yes",
        //     "Enterprise": "Yes",
        //     "rowClass":"default"
        // },
        {
            "Features": "Generate Orders Report",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Performance Of All Students - Test",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        // {
        //     "Features": "Generate Sales Report",
        //     "Lite": "-",
        //     "Standard": "Yes",
        //     "Enterprise": "Yes",
        //     "rowClass":"default"
        // },
        {
            "Features": "Performance Of All Students - Test(Downloadable)",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Customizable Signup Forms",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Partial Private Labeling",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Complete Private Labelling",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "OTHER ESSENTIAL FEATURES",
            "rowClass":"colspan"
        },
        {
            "Features": "Payment Gateway Integration",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Auto Upgrades & Maintenance",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Pricing",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Secure Offline Access(SD Card)",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Social Signup Support",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Free Business Email ID",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "TEST FEATURES",
            "rowClass":"colspan"
        },
        {
            "Features": "Leader Board",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Create Unlimited Tests",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "NTA Exam Template",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        // {
        //     "Features": "Randomising Test Questions (Android App)",
        //     "Lite": "Yes",
        //     "Standard": "Yes",
        //     "Enterprise": "Yes",
        //     "rowClass":"default"
        // },
        {
            "Features": "MCQ | SCQ | Numeric | Matrix | Paragraph - Question Types",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Section | Topic | Question wise Analysis",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "All Students Test Downloadable | Printable Reports",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Single Student Test Rank Card Downloadable",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Retake Test",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Upload Questions",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Test Scheduling",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Result Scheduling",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Answer Explanation with Videos",
            "Lite":"-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "COURSE FEATURES",
            "rowClass":"colspan"
        },
        {
            "Features": "Embed Youtube | Vimeo Videos",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Encrypted Courses",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Unlimited Learners",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Modules (Bundling Tests | Videos | EBooks)",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Upload EBooks",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Upload Videos ",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Embed Live Classes",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Discussion Forum",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Classroom Connect",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Newsfeed",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Content Dripping",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Content Offline Viewing",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "MARKETING FEATURES",
            "rowClass":"colspan"
        },
        {
            "Features": "Google Analytics Tracking",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Reviews & Testimonials",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Learners Analytics",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Push Messages",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Coupon Codes",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Orders Analytics",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Email Marketing",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Sales Analytics",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Signup Embeddables",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "SECURITY",
            "rowClass":"colspan"
        },
        {
            "Features": "Parallel Login Restriction",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Disable Learner Access",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Daily Data Backup",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "HTTPS",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Content Encryption",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Screen Capture Restriction (App)",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Download Video Protection",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Single Device Access (App)",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "LEARNPEDIA SUPPORT",
            "rowClass":"colspan"
        },
        {
            "Features": "Email Support",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Live Chat Support",
            "Lite": "Yes",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "Phone Support",
            "Lite": "-",
            "Standard": "Yes",
            "Enterprise": "Yes",
            "rowClass":"default"
        },
        {
            "Features": "One-on-one Online Support",
            "Lite": "-",
            "Standard": "-",
            "Enterprise": "Yes",
            "rowClass":"default"
        }
    ];
    for(i=0;i<planFeatures.length;i++){
        if(planFeatures[i].rowClass === "colspan"){
            $(".comparePlans").find("tbody").append("<tr><td colspan='5' class='f-700 text-center f-18' style='border:1px solid black'>"+planFeatures[i]['Features']+"</td></tr>")
        }
        else{
            $(".comparePlans").find("tbody").append("<tr><td>"+planFeatures[i]['Features']+"</td>"
                +"<td class='text-center'>"+(planFeatures[i]['Lite'] === "Yes" ? "<i class='zmdi zmdi-check c-green f-18'></i>":planFeatures[i]['Lite'])+"</td>"
                +"<td class='text-center'>"+(planFeatures[i]['Standard'] === "Yes" ? "<i class='zmdi zmdi-check c-green f-18'></i>":planFeatures[i]['Standard'])+"</td>"
                +"<td class='text-center'>"+(planFeatures[i]['Enterprise'] === "Yes" ? "<i class='zmdi zmdi-check c-green f-18'></i>":planFeatures[i]['Enterprise'])+"</td></tr>");
        }
    }
}

    var goToPack = function(){
        if($(this).hasClass("goToCOURSE")){
            $(".COURSE").trigger('click');
        }
        else if($(this).hasClass("goToTEST")){
            $(".TEST").trigger('click');
        }
        $('html, body').animate({
            scrollTop: $(".pricingCard").offset().top -115
          }, 1000);
    }

    var initializeValues = function(){
        var activeTab;
        var tabType;
        if($(this).hasClass("tab")){
            tabType = $(this).find("a").attr("aria-controls");
            $(".tab-pane").each(function(){
                if($(this).attr("id") == tabType){
                    activeTab = $(this);
                    return false;
                }
            });
        } 
        else{
            activeTab= $(".tab-pane.active");
            tabType = activeTab.attr("id");
        }
        if(tabType == "TECH"){
            $("#comparePlanTable").removeClass("nonner");
        }
        else{
            $("#comparePlanTable").addClass("nonner");
        }
        var priceType = $(".packOptions").find("input[name='pricingBasis']:checked").val();
        var studentCount = parseInt($(".packOptions").find(".userSelect").find("option:selected").val());
        calculatePrice(tabType,priceType,studentCount,activeTab);
    }

    var calculatePrice = function(tabType,priceType,studentCount,activeTab){
        var price = {lite:{cost:"",allowDiscount:true,monthly:true,yearly:true},standard:{cost:"",allowDiscount:true,monthly:true,yearly:true},enterprise:{cost:"",allowDiscount:true,monthly:true,yearly:true}};
        var discount = 0;
        price.app = 6000;
        if(tabType == "TECH"){
            if(studentCount <= 200){
                price.lite.cost = 30;
                price.standard.cost = 60;
                discount = 12;
            }
            else if(studentCount > 200 && studentCount <= 500){
                price.lite.cost = 23;
                price.standard.cost = 45;
                discount = 16;
            }
            else if(studentCount > 500 && studentCount <= 1000){
                price.lite.cost = 16;
                price.standard.cost = 32;
                discount = 20;
            }
            else if(studentCount > 1000 && studentCount <= 2000){
                price.lite.cost = 13;
                price.standard.cost = 26;
                discount = 25;
            }
            $(".packHead").find("h2").html("TECH ONLY PACK");
        }
        else if(tabType == "COURSE"){
            if(studentCount <= 200){
                price.lite.cost = 210;
                price.standard.cost = 240;
                price.enterprise.cost = 240;
                discount = 12;
            }
            else if(studentCount > 200 && studentCount <= 500){
                price.lite.cost = 160;
                price.standard.cost = 180;
                price.enterprise.cost = 180;
                discount = 16;
            }
            else if(studentCount > 500 && studentCount <= 1000){
                price.lite.cost = 125;
                price.standard.cost = 145;
                price.enterprise.cost = 145;
                discount = 20;
            }
            else if(studentCount > 1000 && studentCount <= 2000){
                price.lite.cost = 100;
                price.standard.cost = 116;
                price.enterprise.cost = 116;
                discount = 25;
            }
            $(".packHead").find("h2").html("JEE & NEET Course PACK");
        }
        else if(tabType == "TEST"){
            // price.standard.monthly = false;
            // price.standard.allowDiscount = false;
            price.enterprise.monthly = false;
            price.enterprise.allowDiscount = false;
            if(studentCount <= 200){
                price.lite.cost = 65;
                price.standard.cost = 75;
                price.enterprise.cost = 200;
                discount = 12;
            }
            else if(studentCount > 200 && studentCount <= 500){
                price.lite.cost = 49;
                price.standard.cost = 60;
                price.enterprise.cost = 150;
                discount = 16;
            }
            else if(studentCount > 500 && studentCount <= 1000){
                price.lite.cost = 39;
                price.standard.cost = 48;
                price.enterprise.cost = 120;
                discount = 20;
            }
            else if(studentCount > 1000 && studentCount <= 2000){
                price.lite.cost = 31;
                price.standard.cost = 39;
                price.enterprise.cost = 100;
                discount = 25;
            }
            $(".packHead").find("h2").html("JEE & NEET Test PACK");
        }
        if(priceType == "yearly"){
            price.lite.allowDiscount === true ? price.lite.cost = Math.round(price.lite.cost - ((discount / 100) * price.lite.cost)) : price.lite.cost;
            price.standard.allowDiscount === true ?  price.standard.cost = Math.round(price.standard.cost - ((discount / 100) * price.standard.cost)) : price.standard.cost;
            price.enterprise.allowDiscount === true ? price.enterprise.cost = Math.round(price.enterprise.cost - ((discount / 100) * price.enterprise.cost)) : price.enterprise.cost;
            price.app = Math.round(price.app - ((discount/100) * price.app));
        }
        $(".discount-message").html("("+ discount+"% off on your annual plan)");
        updatePackInfo(price,tabType,activeTab,priceType);
    }

    var updatePackInfo = function(price,tabType,activeTab,priceType){
        var PACK1 = activeTab.find(".PACK1");
        var PACK2 = activeTab.find(".PACK2");
        var PACK3  = activeTab.find(".PACK3");
        var appPriceTag = "<h4 class='c-white app-price-description'>INR "+price.app+" <small> pm</small> for Android App.</h4>";
        var disclaimerTag = "<p class='f-11'> * Additional Hardware costs apply.</p>"
        var monthPlanNotAvailableTag = "<p class='f-14'>Please choose yearly plan for this program.</p>"
        var packHeadLite = "<h2>"+(price.lite[""+priceType+""] === false ? ""+monthPlanNotAvailableTag+"":"<span class='f-32'>INR </span>"+price.lite.cost+"<small>  pm | user</small>")+"</h2>";
        var packHeadStandard = "<h2>"+(price.standard[""+priceType+""] === false ? ""+monthPlanNotAvailableTag+"":"<span class='f-32'>INR </span>"+price.standard.cost+defineSuperScript(price.standard))+"</h2>";
        var packHeadEnterprise = "<h2>"+(price.enterprise[""+priceType+""] === false ? ""+monthPlanNotAvailableTag+"":"<span class='f-32'>INR </span>"+price.enterprise.cost+defineSuperScript(price.enterprise))+"</h2>";
        var popularTag =  "<div class='tag c-black'>Popular</div>";
        if(tabType == "TECH"){
            PACK1.find(".pti-header").html(""+packHeadLite+""+"<div class='ptih-title'>Lite Pack</div>");
            PACK2.find(".pti-header").html(""+packHeadStandard+""+popularTag+""+"<div class='ptih-title'>Standard Pack</div>");
        }
        else if(tabType == "COURSE"){
            PACK1.find(".pti-header").html(""+packHeadLite+""+"<div class='ptih-title'>JEEPedia | NEETPedia Online</div>");
            PACK2.find(".pti-header").html(""+packHeadStandard+ disclaimerTag+"<div class='ptih-title'>JEEPedia | NEETPedia SD Card</div>");
            PACK3.find(".pti-header").html(""+packHeadEnterprise+ disclaimerTag+"<div class='ptih-title'>JEEPedia | NEETPedia Pendrive</div>");
        }
        else if(tabType == "TEST"){
            PACK1.find(".pti-header").html(""+packHeadLite+""+"<div class='ptih-title'>TESTPEDIA LITE</div>");
            PACK2.find(".pti-header").html(""+packHeadStandard+""+"<div class='ptih-title'>TESTPEDIA STANDARD</div>");
            PACK3.find(".pti-header").html(""+packHeadEnterprise+""+"<div class='ptih-title'>National Mock Test Series</div>");
        }
    }

    var defineSuperScript = function(price){
        return ("<small>"+(price["monthly"] === false ? " pa" : " pm")+" | user</small>");
    }
}