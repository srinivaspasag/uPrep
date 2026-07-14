var vValidate=new(function($){
    this.init=function(params){

    }
})(jQuery);
$.fn.quickValidate = function() {
    var $form = this,
        $inputs = $form.find('input:text, input:password');
    var filters = {
        name: {
            regex: /^[A-Za-z0-9 ]{3,40}$/,
            error: 'Must be at least 3 characters long & max of 20.'
        },
        coll: {
            regex: /^[A-Za-z0-9 ]{3,20}$/,
            error: 'Must be at least 3 characters long & max of 20.'
        },
        url:{
            regex:  /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/,
            error: 'Not a valid url'
        },
        pass: {
            regex: /(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,}/,
            error: 'Must be at least 6 characters long, and contain at least one number, one uppercase and one lowercase letter.'
        },
        email: {
            regex: /^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/,
            error: 'Must be a valid e-mail address (user@gmail.com)'
        },
        phone: {
            regex: /^([\+]?)([\d]{8,14})$/,
            error: 'Must have 8-14 digits'
        }
    };
    var validate = function(klass, value) {
        var isValid = true,
            error = '';
        if (!value && /required/.test(klass)) {
            error = 'This field is required';
            isValid = false;
        } else {
            klass = klass.split(/\s/);
            $.each(klass, function(i, k){
                if (filters[k]) {
                    if (value && !filters[k].regex.test(value)) {
                        isValid = false;
                        error = filters[k].error;
                    }
                }
            });
        }
        return {
            isValid: isValid,
            error: error
        }
    };
    var printError = function($input) {
        var klass = $input.attr('class'),
            value = $input.val(),
            test = validate(klass, value),
            $error = $('<span class="error">' + test.error + '</span>'),
            $icon = $('<i class="error-icon"></i>');

        $input.removeClass('invalid').siblings('.error, .error-icon').remove();

        if (!test.isValid) {
            $input.addClass('invalid');
            $error.add($icon).insertAfter($input);
            $icon.hover(function() {
                $(this).siblings('.error').toggle();
            });
        }
    };
    $inputs.each(function() {
        if ($(this).is('.required')) {
            printError($(this));
        }
    });
    $inputs.live('keyup',function() {
        printError($(this));
    });
    return this;
};

