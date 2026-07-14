var pretest = new function(){
    var parentDiv = "#preTest";
    this.init = function(){
        library.init();
        var testId = $(".incommingTestId").val();
        var testName = $(".incommingTestName").val();
        var pdfId = $(".incommingPdfId").val();
        if(!testId) testId = getURLParameter("testId");
        params = {'testId':testId,'pdfId':pdfId,'testName':testName,tabType:"TEST_ATTEMPT"};
        window['serverTimeDelta'] = $(".serverPreTestLoadTime").val()-new Date().getTime();
        if(!window['serverTimeDelta'] || window['serverTimeDelta']<60000){
            window['serverTimeDelta'] = 0;
        }
        $(".subjectWeightage")
            .on("change","#subjectInTest",subjectChangeWeightage);
        $("#takeTest")
            .on("click",check);
        $("#takeTestSmall")
            .on("click",check);
        $('.view-details')
            .on("click",viewSubjectDetails);
    };

    var check = function(){
        if(isMobileDevice()){
            swal({
              title: 'This feature is not available in mobile browser.',
              text: "Please use desktop or our mobile application",
              type: 'warning',
              showCancelButton: true,
              confirmButtonColor: '#3085d6',
              cancelButtonColor: '#d33',
              confirmButtonText: 'Yes, Take me to Playstore!'
            }).then(function(result){
              if (result == true) {
                window.top.location.href = "https://play.google.com/store/apps/details?id=com.learnpedia.android&hl=en_IN";
              }
            });
        }
        else{
            var passwordValue = $(".incommingPasswordForTest").val();
            if(passwordValue !== "null"){
                swal(
                    {
                    title: 'Please Enter Password',
                    input: 'password',
                    showCancelButton: true,
                    confirmButtonText: 'Submit',
                    showLoaderOnConfirm: true,
                    preConfirm: function (password) {
                        return new Promise(function (resolve, reject) {
                            setTimeout(function() {
                                if (password != passwordValue) {
                                    reject('Please Enter Correct Password!!')
                                } else {
                                    resolve()
                                }
                            }, 500) })
                    }, allowOutsideClick: false }).then(function (password)
                    {
                        guidelinesPopup();
                    })
                }
                else
                {
                    guidelinesPopup();
                }
            }

    };

    var isMobileDevice = function(){
        var valid = false;
        var isMobile = {
            Android: function() {
                return navigator.userAgent.match(/Android/i);
            },
            BlackBerry: function() {
                return navigator.userAgent.match(/BlackBerry/i);
            },
            iOS: function() {
                return navigator.userAgent.match(/iPhone|iPad|iPod/i);
            },
            Opera: function() {
                return navigator.userAgent.match(/Opera Mini/i);
            },
            Windows: function() {
                return navigator.userAgent.match(/IEMobile/i);
            },
            any: function() {
                return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Opera() || isMobile.Windows());
            }
        };
        if( isMobile.any() ){
            valid = true;
        }
        return valid;
    };

    var subjectChangeWeightage = function(){
        $this = $(this);
        var subjectId = $this.find(':selected').data('subjectId');
        $(".testTopicsWeightageTable").addClass("hidden");
        $(".testTopicsWeightageHolder").find(".cls-topicWeightage-"+subjectId).removeClass("hidden");
    };

    var guidelinesPopup = function(){
        swal({
            html:$("#preTestTakeTestBtnHold").find(".testGuidelinesHolder").parent().html(),
            showConfirmButton:false,
            customClass:"testGuidelines",
            showCloseButton: true
        });
        initGuidelinesHolder($(".testGuidelines"));
        $(".testGuidelines").find(".testGuidelinesHolder").on("click","#proceedTest",proceedToTest);
    };

    var proceedToTest = function(){
        params['startTime']=(new Date()).getTime()+window['serverTimeDelta'];
        params["entity.id"]=params.testId;
        params["entity.type"]="TEST";
        swal.close();
        history.replaceState(null,null,"/");
        if(prepareUiForTest()){
            openInstSubPage("/Tests/attempt","test/attempt/"+params.testId,params);
        }else{
            swal({
                html:"Browser dependency error!"
            });
        }
    };

    var viewSubjectDetails = function(){
        $this = $(this);
        var _id = "_testDetailsPop_"+$this.data("id");
        swal({
            html:$("#subjectDetailsHolder").find(".testDetailsPopupHolder").find("#"+_id).removeClass("hidden").parent().html()
        }).then(
              function () {},
                  $("#subjectDetailsHolder").find(".testDetailsPopupHolder").find("#"+_id).addClass("hidden")
            );

    };
};