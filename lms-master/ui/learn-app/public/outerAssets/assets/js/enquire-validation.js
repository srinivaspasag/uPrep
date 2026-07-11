jQuery(document).ready(function($) {
  "use strict"; 
  $('#send_enquiry').click(function(e){

            //Stop form submission & check the validation
            e.preventDefault();
            
            // Variable declaration
            var error = false;
            var name = $('#name').val();
            var email = $('#email').val();
            // var subject = $('#subject').val();
            // var message = $('#your_message').val();
            var number = $('#number').val();
            var state = $('#state').val();
            var city = $('#city').val();
            var premise = $('#premise').val();
            var investment = $('#investment').val();
            var pro_message = $("#pro_message").val();
            var que_message = $("#que_message").val();
            
            // Form field validation
            if(name.length == 0){
                var error = true;
                $('#name_error').fadeIn(500);
            }else{
                $('#name_error').fadeOut(500);
            }
            if(email.length == 0 || email.indexOf('@') == '-1'){
                var error = true;
                $('#email_error').fadeIn(500);
            }else{
                $('#email_error').fadeOut(500);
            }

            if(number.length == 0 || isNaN(number)){
                var error = true;
                $('#number_error').fadeIn(500);
            }else{
                $('#number_error').fadeOut(500);
            }
            // if(subject.length == 0){
            //     var error = true;
            //     $('#subject_error').fadeIn(500);
            // }else{
            //     $('#subject_error').fadeOut(500);
            // }
            // if(message.length == 0){
            //     var error = true;
            //     $('#message_error').fadeIn(500);
            // }else{
            //     $('#message_error').fadeOut(500);
            // }

            if(state.length == 0){
                var error = true;
                $('#state_error').fadeIn(500);
            }else{
                $('#state_error').fadeOut(500);
            }

            if(city.length == 0){
                var error = true;
                $('#city_error').fadeIn(500);
            }else{
                $('#city_error').fadeOut(500);
            }

            if(premise.length == 0){
                var error = true;
                $('#premises_error').fadeIn(500);
            }else{
                $('#premises_error').fadeOut(500);
            }

            if(investment.length == 0){
                var error = true;
                $('#investment_error').fadeIn(500);
            }else{
                $('#investment_error').fadeOut(500);
            }
            
            // If there is no validation error, next to process the mail function
            if(error == false){
               // Disable submit button just after the form processed 1st time successfully.
               $('#send_enquiry').attr({'disabled' : 'true', 'value' : 'Sending...' });
               var params = {
                    "name" : name,
                    "email" : email,
                    "number" : number,
                    "state" : state,
                    "city" : city,
                    "premise":premise,
                    "investment":investment,
                    "pro_message":pro_message,
                    "que_message":que_message,
                    "fromForm":"ENQUIRY"
                };
               /* Post Ajax function of jQuery to get all the data from the submission of the form as soon as the form sends the values to email.php*/
               $.post("Application/sendEmail",params,function(result){
                result = JSON.parse(result);
                    //Check the result set from enquire.php file.
                    if(result.result.success === true){
                        //If the email is sent successfully, remove the submit button
                        $('#name').remove();
                        $('#email').remove();
                        $("#number").remove();
                        $("#state").remove();
                        $("#city").remove();
                        $("#premise").remove();
                        $("#investment").remove();
                        $("#pro_message").remove();
                        $("#que_message").remove();
                        // $('#subject').remove();
                        // $('#your_message').remove();
                        $('#submit').remove();
                        //Display the success message
                        $('#mail_success').fadeIn(500);
                    }else{
                        //Display the error message
                        $('#mail_fail').fadeIn(500);
                        // Enable the submit button again
                        $('#send_enquiry').removeAttr('disabled').attr('value', 'Send The Message');
                    }
                });
           }
       });    
});