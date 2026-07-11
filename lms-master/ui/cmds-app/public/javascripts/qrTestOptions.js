var qrTestOptions = new function(){
    this.init = function(){
        $("#testPage").on("change",".enablePartialMarks",enablePartialMarks)
                      .on("change",".enableSectionLocking",enableSectionLocking)
                      .on("change",".enableAutoResumeTest",enableAutoResumeTest)
        $("#previewTestPage").on("change",".enableSectionLocking",enableSectionLocking)
                            .on("change",".enableAutoResumeTest",enableAutoResumeTest)
        //Setting overflow Holder width dynamically.
        var width = $("#contentSectionHolder").width();
        $(".overFlowHolder").css("max-width",width - 750);
    }

    var enablePartialMarks = function(){
        enableOrDisableTestOptions("enablePartialMarks","partialMarksOptionTable","Partial Marks are");
    }

    var enableSectionLocking = function(){
        console.log("Inside qrTestOptions");
        enableOrDisableTestOptions("enableSectionLocking","sectionLockingOptionDiv","Section Locking is");
    }

    var enableAutoResumeTest = function(){
        console.log("Inside qrTestOptions");
        enableOrDisableTestOptions("enableAutoResumeTest","enableAutoResumeTestDiv","Auto Resume option is");
    }

    var enableOrDisableTestOptions = function(testOption,testOptionDiv,text){
        var radioValue = $("input[name="+testOption+"]:checked").val();
        var testId = $(".testPageDetails").data("testId");
        var params = {
            testId:testId
        };
        var testOptionUrl = "";
        if(testOption == "enablePartialMarks"){
            params.enablePartialMarks = radioValue === "true" ? true : false;
            params.oneOrMoreMarksQTypes = ["MCQ","PARA","MATRIX"];
            if(radioValue === "true"){
                params.qTypes = ["MCQ","PARA","MATRIX"];
            }
            else{
                params.qTypes = [];
            }
            testOptionUrl = "enableOrDisablePartialMarks";
        }
        else if(testOption == "enableSectionLocking"){
            params.enableSectionLocking = radioValue === "true" ? true : false;
            testOptionUrl = "enableOrDisableSectionLocking";
        }
        else if(testOption == "enableAutoResumeTest"){
            params.enableAutoResumeTest = radioValue === "true" ? true : false;
            testOptionUrl = "enableAutoResumeTest";
        }
        vReq.post("/QrTests/"+testOptionUrl,params,function(data){
            if(data.result.success === true){
                if(params[""+testOption] === true){
                    $("."+testOptionDiv).find(".radioEnableText").removeClass("redColor").addClass("greenColor").text(""+text+" enabled for this test");
                }
                else{
                    $("."+testOptionDiv).find(".radioEnableText").removeClass("greenColor").addClass("redColor").text(""+text+" not enabled for this test");
                }
            }
        }, function(data) {
            $("."+testOptionDiv).find(".radioEnableText").removeClass("greenColor").addClass("redColor").text("Something went wrong");
            $("."+testOptionDiv).html($("."+testOptionDiv).html());
        });
    }
}