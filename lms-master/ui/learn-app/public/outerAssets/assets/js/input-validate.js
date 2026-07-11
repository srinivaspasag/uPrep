var countryData;
var countrycode;
    function validate()
    {
        var pn=document.getElementById("phone");
        var number=document.getElementById('phone').value;
        // countryData = $.fn.intlTelInput.getCountryData();
        // jQuery(document).ready(function(){
        countrycode = document.getElementById('phone').intlTelInput("getSelectedCountryData").dialCode;  
      // });
        

        if(countrycode=="91")
        {

        //alert(number);
        if(number.length ==0)
        {
            document.getElementById('null').innerHTML="Please enter a number";
            pn.style.borderColor = "#D92026";
            return false;
        }
        if(number.length<10 || number.length>10)
        {
            document.getElementById('null').innerHTML="Invalid phonenumber format";
            pn.style.borderColor = "#D92026";
            return false;
        }
        if(isNaN(number))
        {
            document.getElementById('null').innerHTML="Phone number must contain only digits.";
            pn.style.borderColor = "#D92026";
            return false;
        }

        document.getElementById('null').innerHTML="";
        pn.style.borderColor = "#66AFE9";
        $('#myModal').modal();
        return true;
    }
    else
    {
//         $("#phone").intlTelInput({
//   utilsScript: "assets/js/utils.js"
// });
    if ($.trim($("#phone").val())) 
  {
    if ($("#phone").intlTelInput("isValidNumber")) 
    {
      // console.log($("#phone").intlTelInput("getNumber"));
      // alert('Valid' );
      pn.style.borderColor = "#66AFE9";
        $('#myModal').modal();
        return true;
    }
    else 
    {
        document.getElementById('null').innerHTML="Invalid number";
            pn.style.borderColor = "#D92026";

      // console.log($("#phone").intlTelInput("getValidationError"));
      // alert('invalid');
    }
  }
}
    }
        //return false;
    function image1(){
        console.log("image1");
        var number;
        number=document.getElementById('phone').value;
        if(number=="")
        {
        window.location.href="https://qa.learnpedia.in/quicksignup?type="+ "JEE";
        }
        else
        {
            // countryData = $.fn.intlTelInput.getCountryData();
    // var addressDropdown = $("#address-country");
            countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
            
            // alert(initialCountry);
        // if(number=="")
        // {
        //     number=document.getElementById('footerphone').value;
        // }
        window.location.href="https://qa.learnpedia.in/quicksignup?contactnumber="+number +"&type="+ "JEE"+"&countrycode=" + countrycode;
    }

    }
    function image2(){
        console.log("image2");
        var number;
        number=document.getElementById('phone').value;
        if(number=="")
        {
        window.location.href="https://qa.learnpedia.in/quicksignup?type="+ "NEET";
        }
        else
        {
            // countryData = $.fn.intlTelInput.getCountryData();
    // var addressDropdown = $("#address-country");
            countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
        // if(number=="")
        // {
        //     number=document.getElementById('footerphone').value;
        // }
        window.location.href="https://qa.learnpedia.in/quicksignup?contactnumber="+number +"&type="+ "NEET" +"&countrycode=" + countrycode;;
}
    }

    function footerimage1(){
        // console.log("image1");
        var number;
        number=document.getElementById('footerphone').value;
        // countryData = $.fn.intlTelInput.getCountryData();
        countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
        // if(number=="")
        // {
        //     number=document.getElementById('footerphone').value;
        // }
        window.location.href="https://qa.learnpedia.in/quicksignup?contactnumber="+number +"&type="+ "JEE" +"&countrycode=" + countrycode;;

    }


    function footerimage2(){
        // console.log("image1");
        var number;
        number=document.getElementById('footerphone').value;
        // countryData = $.fn.intlTelInput.getCountryData();
        countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
        // if(number=="")
        // {
        //     number=document.getElementById('footerphone').value;
        // }
        window.location.href="https://qa.learnpedia.in/quicksignup?contactnumber="+number +"&type="+ "NEET" +"&countrycode=" + countrycode;


    }
    function validater(){
        var number=document.getElementById('footerphone');
        // countryData = $.fn.intlTelInput.getCountryData();
        countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
        if(countrycode=="91")
        {
        if(number.value=="" || number.value.length<10 || number.value.length>10 || isNaN(number.value))
        {
            console.log("hey");
            number.style.borderColor = "#D92026";
    }
    else{
        console.log("bye");
        number.style.borderColor = "#66AFE9";
        $('#footerModal').modal();
        return true;
    }
    }
    else
    {
        if ($.trim($("#footerphone").val())) 
  {
    if ($("#footerphone").intlTelInput("isValidNumber")) 
    {
      // console.log($("#phone").intlTelInput("getNumber"));
      // alert('Valid' );
      number.style.borderColor = "#66AFE9";
        $('#footerModal').modal();
        return true;
    }
    else 
    {
        // document.getElementById('null').innerHTML="Invalid number";
            number.style.borderColor = "#D92026";
            return false;

      // console.log($("#phone").intlTelInput("getValidationError"));
      // alert('invalid');
    }
  }
    }
}

$("#phone").intlTelInput({
       initialCountry: "in",
      separateDialCode: true,
      utilsScript: "utils.js"
    });  

$("#footerphone").intlTelInput({
      // allowDropdown: false,
      // autoHideDialCode: false,
      // autoPlaceholder: "off",
      // dropdownContainer: "body",
      // excludeCountries: ["us"],
      // formatOnDisplay: false,
       // geoIpLookup: function(callback) {
       //   $.get("http://ipinfo.io", function() {}, "jsonp").always(function(resp) {
       //     var countryCode = (resp && resp.country) ? resp.country : "";
       //   callback(countryCode);
       //   });
       // },
       initialCountry: "in",
       // nationalMode: true,
      // onlyCountries: ['us', 'gb', 'ch', 'ca', 'do'],
      // placeholderNumberType: "MOBILE",
      // preferredCountries: ['cn', 'jp'],
      separateDialCode: true,
      utilsScript: "utils.js"
    });

