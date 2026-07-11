jQuery(document).ready(function($) {
  "use strict"; 
  $('#send_number').click(function(e){

            //Stop form submission & check the validation
            e.preventDefault();
            
            // Variable declaration
            var error = false;
            var number = $('#number').val();
            
            // Form field validation
            
            if(number.length == 0 || isNaN(number) || number.length<10){
                var error = true;
                $('#number_error').fadeIn(500);
            }else{
                $('#number_error').fadeOut(500);
            }
            
            // If there is no validation error, next to process the mail function
            if(error == false){
               // Disable submit button just after the form processed 1st time successfully.
               $('#send_number').attr({'disabled' : 'true', 'value' : 'Sending...' });
               var params = {
                "number":number,
                "fromForm":"PROGRAMS"
               }
               /* Post Ajax function of jQuery to get all the data from the submission of the form as soon as the form sends the values to email.php*/
               $.post("Application/sendEmail", params,function(result){
                result = JSON.parse(result);
                    // //Check the result set from enquire.php file.
                    if(result.result.success === true){
                        //If the email is sent successfully, remove the submit button
                        $("#number").remove();
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