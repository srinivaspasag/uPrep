$(document).ready(function() {
  $("#productsModal").keydown(function(event){
    if(event.keyCode == 13) {
      event.preventDefault();
      return false;
    }
  });
});
function searchKeyPress(e) {
    e = e || window.event;
    if (e.keyCode == 13) {
        document.getElementById('btnSearch').click();
        return false;
    }
    return true;
}

function footersearchKeyPress(e) {
    e = e || window.event;
    if (e.keyCode == 13) {
        document.getElementById('footerbutton').click();
        return false;
    }
    return true;
}
var countryData;
var countrycode;

function validate() {
    var pn = document.getElementById("phone");
    var number = document.getElementById('phone').value;
    countryData = $.fn.intlTelInput.getCountryData();
    countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
    if (countrycode == "91") {
        if (number.length == 0) {
            document.getElementById('null').innerHTML = "Please enter a number";
            pn.style.borderColor = "#D92026";
            return false;
        }
        if (number.length < 10 || number.length > 10) {
            document.getElementById('null').innerHTML = "Invalid phonenumber format";
            pn.style.borderColor = "#D92026";
            return false;
        }
        if (isNaN(number)) {
            document.getElementById('null').innerHTML = "Phone number must contain only digits.";
            pn.style.borderColor = "#D92026";
            return false;
        }
        document.getElementById('null').innerHTML = "";
        pn.style.borderColor = "#66AFE9";
        // return true;
    } else {
        if ($.trim($("#phone").val())) {
            if ($("#phone").intlTelInput("isValidNumber")) {
                pn.style.borderColor = "#66AFE9";
                // $('#otploginModal').modal();
                // return true;
            } else {
                document.getElementById('null').innerHTML = "Invalid number";
                pn.style.borderColor = "#D92026";
                return false;
            }
        }
    }
    var orgId = $("input[name=orgId]").val();
    var params = {
        contactNumber: number,
        countryCode: countrycode,
        orgId:orgId
    };
    $.post("/Register/sendOTP", params, function(data) {
        var defaultCountryCode = "in";
        for (i = 0; i < countryData.length; i++) {
            if (params.countryCode == countryData[i].dialCode) {
                defaultCountryCode = countryData[i].iso2;
            }
        }
        $(".contactNumber").intlTelInput("setCountry", defaultCountryCode);
        if (data.isVerifiedUser == true && data.isNewPhone == true) {
            document.getElementsByClassName("contactNumber")[0].value = params.contactNumber;
            $("#memberLoginForm").removeClass("formactive");
            $("#memberSignupForm").addClass("formactive");
            $("#otpsignupModal").modal();
        } else if (data.isVerifiedUser == true && data.isNewPhone == false) {
            document.getElementsByClassName("contactNumber")[1].value = params.contactNumber;
            $("#memberSignupForm").removeClass("formactive");
            $("#memberLoginForm").addClass("formactive");
            $('#otploginModal').modal();
        } else {
            $("#otpErrorModal").modal();
        }
    });
}

function image1() {
    var number;
    number = document.getElementById('phone').value;
    if (number == "") {
        window.location.href = "https://learn.learnpedia.in/quicksignup?type=" + "JEE";
    } else {
        countryData = $.fn.intlTelInput.getCountryData();
        countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
        window.location.href = "https://learn.learnpedia.in/quicksignup?contactnumber=" + number + "&type=" + "JEE" + "&countrycode=" + countrycode;
    }
}

function image2() {
    var number;
    number = document.getElementById('phone').value;
    if (number == "") {
        window.location.href = "https://learn.learnpedia.in/quicksignup?type=" + "NEET";
    } else {
        countryData = $.fn.intlTelInput.getCountryData();
        countrycode = $("#phone").intlTelInput("getSelectedCountryData").dialCode;
        window.location.href = "https://learn.learnpedia.in/quicksignup?contactnumber=" + number + "&type=" + "NEET" + "&countrycode=" + countrycode;
    }
}

function footerimage1() {
    var number;
    number = document.getElementById('footerphone').value;
    countryData = $.fn.intlTelInput.getCountryData();
    countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
    window.location.href = "https://learn.learnpedia.in/quicksignup?contactnumber=" + number + "&type=" + "JEE" + "&countrycode=" + countrycode;
}

function footerimage2() {
    var number;
    number = document.getElementById('footerphone').value;
    countryData = $.fn.intlTelInput.getCountryData();
    countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
    window.location.href = "https://learn.learnpedia.in/quicksignup?contactnumber=" + number + "&type=" + "NEET" + "&countrycode=" + countrycode;
}

function validater() {
    var number = document.getElementById('footerphone');
    var phonenumber = number.value;
    countryData = $.fn.intlTelInput.getCountryData();
    countrycode = $("#footerphone").intlTelInput("getSelectedCountryData").dialCode;
    if (countrycode == "91") {
        if (number.value == "" || number.value.length < 10 || number.value.length > 10 || isNaN(number.value)) {
            number.style.borderColor = "#D92026";
            return false;
        } else {
            number.style.borderColor = "#66AFE9";
            // $('#footerModal').modal();
            // return true;
        }
    } else {
        if ($.trim($("#footerphone").val())) {
            if ($("#footerphone").intlTelInput("isValidNumber")) {
                number.style.borderColor = "#66AFE9";
                $('#footerModal').modal();
                // return true;
            } else {
                number.style.borderColor = "#D92026";
                return false;
            }
        }
    }
    var orgId = $("input[name=orgId]").val();
    var params = {
        contactNumber: phonenumber,
        countryCode: countrycode,
        orgId:orgId
    };
    $.post("/Register/sendOTP", params, function(data) {
        var defaultCountryCode = "in";
        for (i = 0; i < countryData.length; i++) {
            if (params.countryCode == countryData[i].dialCode) {
                defaultCountryCode = countryData[i].iso2;
            }
        }
        $(".contactNumber").intlTelInput("setCountry", defaultCountryCode);
        if (data.isVerifiedUser == true && data.isNewPhone == true) {
            document.getElementsByClassName("contactNumber")[0].value = params.contactNumber;
            $("#memberLoginForm").removeClass("formactive");
            $("#memberSignupForm").addClass("formactive");
            $("#otpsignupModal").modal();
        } else if (data.isVerifiedUser == true && data.isNewPhone == false) {
            document.getElementsByClassName("contactNumber")[1].value = params.contactNumber;
            $("#memberSignupForm").removeClass("formactive");
            $("#memberLoginForm").addClass("formactive");
            $('#otploginModal').modal();
        } else {
            $("#otpErrorModal").modal();
        }
    });
}
var type;
$(document).ready(function() {
    $(document).on("click", ".contactModalDialog", function() {
        type = $(this).data('id');
        window.location.href = "https://learn.learnpedia.in/quicksignup?type=" + type;
    });
});