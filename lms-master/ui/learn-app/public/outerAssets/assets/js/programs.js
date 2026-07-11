
    $(document).ready(function() {
        var selectedAccess="";
        var program = {
            "iconPath" : '',
            "comboOption" : '',
            "id" : 0,
            "variant" : []
        };

        const JEEACCESS = [{
                "iconPath" : '/public/outerAssets/images/programs/pendriveIcon.png',
                "comboOption" : '1',
                "id" : 9194637329,
                "variant" : [33360694673,33360694545]
            }, {

                "iconPath" : '/public/outerAssets/images/programs/onlineIcon.png',
                "comboOption" : '0',
                "id" : 9194456209,
                "variant" : [33358586257, 33358586193]
            }, {
            
                "iconPath" : '/public/outerAssets/images/programs/sdCardIcon.png',
                "comboOption" : '2',
                "id" : 9194536657,
                "variant" : [33359719761, 33359719697]
            }];


         const NEETACCESS = [{
                "iconPath" : '/public/outerAssets/images/programs/pendriveIcon.png',
                "comboOption" : '1',
                "id" : 9194869841,
                "variant" : [33362896849,33362809553]
            }, {

                "iconPath" : '/public/outerAssets/images/programs/onlineIcon.png',
                "comboOption" : '0',
                "id" : 9194733905,
                "variant" : [33361589009, 33361588881]
            }, {
            
                "iconPath" : '/public/outerAssets/images/programs/sdCardIcon.png',
                "comboOption" : '2',
                "id" : 9194812369,
                "variant" : [33362235729, 33362235601]
            }];   

        $("#programs").hide();
        $("#notready").hide();
        $("#finalDecision").hide();
        $("#comboBoxes").hide();
        $("#modify").hide();
        $("#pricing").hide();
        $("#responsiveJEE").hide();
        $("#responsiveNEET").hide();

        var url = (window.location).href;
        var id = url.substring(url.lastIndexOf('#') + 1);
        if(id=="ScoreJEE"){
            ScoreJeebtnClick();
        }
        else if(id=="ScoreNEET"){
            ScoreNeetbtnClick();
        }

        $('#seeMore').click(function() {
            $("#seeMoreIcon").toggleClass('fa fa-arrow-circle-o-up fa fa-arrow-circle-o-down')
            $(this).text(function(i, old) {
                return old == 'See less' ? 'See more' : 'See less';
            });
        });


        $('#pendrive').click(function() {
            $("#comboBoxes").hide();
            $("#pendrive").addClass('shadowAccess');
            $("#online").removeClass('shadowAccess');
            $("#sdCard").removeClass('shadowAccess');
            $("#modify").show();

            selectedAccess = 'pendrive';
            program = JEEACCESS[0];
            changeProgram(program);
            return;
        });




        $('#online').click(function() {
            $("#comboBoxes").hide();
            $("#online").addClass('shadowAccess');
            $("#pendrive").removeClass('shadowAccess');
            $("#sdCard").removeClass('shadowAccess');
            $("#modify").show();
            selectedAccess = 'online';
            program = JEEACCESS[1];
            changeProgram(program);
            return;
        });


         $('#sdCard').click(function() {
            $("#sdCard").addClass('shadowAccess');
            $("#pendrive").removeClass('shadowAccess');
            $("#online").removeClass('shadowAccess');
            $("#modify").show();
            $("#comboBoxes").hide();
            selectedAccess = 'sdCard';
            program = JEEACCESS[2];
            changeProgram(program);
            return;
        });

         function changeProgram(program) {
            $("#programs").show();
            $("#finalDecision").show();
            $("#accessIcon").attr('src',program.iconPath);
            $("#modify").show();
            $('#accessComboBox option').eq(program.comboOption).prop('selected',true);
            
            $('#buyNow').empty();
            $("#buyNow").show();
            $("#pricing").show();

            if (document.getElementById('12Months').checked) {
                changeDuration(0);
                
            } else if (document.getElementById('24Months').checked) {
                changeDuration(1);

            }
         };

         function changeDuration(duration) {
            if(duration == 0) {
            document.getElementById("durationAgain").innerHTML="12 Months";
            document.getElementById("durationexplain").innerHTML = "You have selected ScoreJEE 12 Months program, this gives you access till JEE 2018";
            }
            else if(duration == 1) {
            document.getElementById("durationAgain").innerHTML="24 Months";
            document.getElementById("durationexplain").innerHTML = "You have selected ScoreJEE 24 Months program, this gives you access till JEE 2019";
            }
            $('#durationComboBox option').eq(duration).prop('selected',true);
            $('#buyNow').empty();
            $("#buyNow").show();
            if(program.id > 0) {
                buyButton(program.id, program.variant[duration]);
            }
         };

         $('#durationComboBox').change(function() {
            var duration = document.getElementById("durationComboBox");
            if(this.value=="12 Months")
            {
                changeDuration(0);
                $('#12Months').prop("checked", true);
            }
            else if(this.value=="24 Months"){
                changeDuration(1);
                $('#24Months').prop("checked", true);
            }
         });

           $('#accessComboBox').change(function() {
                $("#sdCard").removeClass('shadowAccess');
                $("#pendrive").removeClass('shadowAccess');
                $("#online").removeClass('shadowAccess');
                $("#sdCardContent").removeClass('active');
                $("#pendriveContent").removeClass('active');
                $("#onlineContent").removeClass('active');
            if(this.value=='Pendrive'){
                selectedAccess = 'pendrive';
                $("#pendrive").addClass('shadowAccess');
                $("#pendriveContent").addClass('active');
                program = JEEACCESS[0];
            changeProgram(program);
            }
            else if(this.value=='Online'){
                selectedAccess = 'online';
                $("#online").addClass('shadowAccess');
                $("#onlineContent").addClass('active');
                program = JEEACCESS[1];
            changeProgram(program);

            }
            else if(this.value=='SD Card'){
                program = JEEACCESS[2];
                $("#sdCard").addClass('shadowAccess');
                $("#sdCardContent").addClass('active');
            changeProgram(program);

            }
         });


        // Checking for duration radio button
        $('input[type=radio][name=duration]').change(function() {
            if (this.value == '12 Months') {
                // document.getElementById("durationexplain").innerHTML = "You have selected ScoreJEE 12 Months program, this gives you access till JEE 2018";
                // $('html, body').animate({ scrollTop: $("#durationexplain").offset().top }, 100);
                // $('#durationComboBox option').eq(0).prop('selected',true);
                // document.getElementById("durationAgain").innerHTML="12 Months";
                changeDuration(0);
            } else if (this.value == '24 Months') {
                // document.getElementById("durationexplain").innerHTML = "You have selected ScoreJEE 24 Month program, this gives you access till JEE 2019";
                // $('html, body').animate({ scrollTop: $("#durationexplain").offset().top }, 100);
                // $('#durationComboBox option').eq(1).prop('selected',true);
                // document.getElementById("durationAgain").innerHTML="24 Months";
                changeDuration(1);
            }
        });

        $("#modifydisplay").click(function()
        {
            $("#comboBoxes").toggle();
        });


        function hideTagsJEE()
        {
            $("#ScoreJeebtn").addClass('shadow');   
            $("#ScoreNeetbtn").removeClass('shadow');
            $("#programs").hide();
            $("#finalDecisionNEET").hide();
            $("#comboBoxesNEET").hide();
            $("#modifyNEET").hide();
            $("#buyNow").empty();
            $("#pricing").hide();
            $("#programs").fadeIn("slow");
        }
        
        

        // Checking for click handler for JEE button
        $("#ScoreJeebtn")
                .on("click",ScoreJeebtnClick);
        $("#ScoreJEEMenu")
                .on("click",ScoreJeebtnClick);                

        function ScoreJeebtnClick(){
            hideTagsJEE();
            $("#responsiveJEE").show();
            $("#responsiveNEET").hide();
            $("#notready").show();
            if ($("#ScoreNEET").hasClass('showdisplay')) {
                $("#ScoreNEET").removeClass('showdisplay');
                $("#ScoreNEET").addClass('nodisplay');
            }
            $("#ScoreJEE").addClass('showdisplay');
            $.scrollTo($("#ScoreJEE"), {
                duration: 0
            });
        };


        // Checking for click handler for NEET button
        $("#ScoreNeetbtn")
                .on("click",ScoreNeetbtnClick);
        $("#ScoreNEETMenu")
                .on("click",ScoreNeetbtnClick);

        function ScoreNeetbtnClick(){
            hideTagsNEET();
            $("#notready").show();
             $("#responsiveNEET").show();
            $("#responsiveJEE").hide();
            if ($("#ScoreJEE").hasClass('showdisplay')) {
                $("#ScoreJEE").removeClass('showdisplay');
                $("#ScoreJEE").addClass('nodisplay');
            }
            $("#ScoreNEET").addClass('showdisplay');

            $.scrollTo($("#ScoreNEET"), {
                duration: 0
            });
        }

        

        // FOR NEET


        $("#finalDecisionNEET").hide();
        $("#comboBoxesNEET").hide();
        $("#modifyNEET").hide();
        

        $('#seeMoreNEET').click(function() {
            $("#seeMoreIconNEET").toggleClass('fa fa-arrow-circle-o-up fa fa-arrow-circle-o-down')
            $(this).text(function(i, old) {
                return old == 'See more' ? 'See less' : 'See more';
            });
        });


         $('input[type=radio][name=durationNEET]').change(function() {
            if (this.value == '12 Months') {
                changeDurationNEET(0);
            } else if (this.value == '24 Months') {
                changeDurationNEET(1);
            }
        });


         function changeProgramNEET(program) {
            $("#programs").show();
            $("#finalDecisionNEET").show();
            $("#accessIconNEET").attr('src',program.iconPath);
            // $("#comboBoxes").show();
            $("#modifyNEET").show();
            $('#accessComboBoxNEET option').eq(program.comboOption).prop('selected',true);
            
            $('#buyNow').empty();
            $("#buyNow").show();
            $("#pricing").show();

            if (document.getElementById('12MonthsNEET').checked) {
                changeDurationNEET(0);
                
            } else if (document.getElementById('24MonthsNEET').checked) {
                changeDurationNEET(1);

            }
         };

         function changeDurationNEET(duration) {
            if(duration == 0) {
            document.getElementById("durationAgainNEET").innerHTML="12 Months";
            document.getElementById("durationexplainNEET").innerHTML = "You have selected ScoreNEET 12 Months program, this gives you access till NEET 2018";
            }
            else if(duration == 1) {
            document.getElementById("durationAgainNEET").innerHTML="24 Months";
            document.getElementById("durationexplainNEET").innerHTML = "You have selected ScoreNEET 24 Month program, this gives you access till NEET 2019";
            }
            $('#durationComboBoxNEET option').eq(duration).prop('selected',true);
            $('#buyNow').empty();
            $("#buyNow").show();
            if(program.id > 0) {
                buyButton(program.id, program.variant[duration]);
            }
         };

         $('#pendriveNEET').click(function() {
            $("#pendriveNEET").addClass('shadowAccess');
            $("#onlineNEET").removeClass('shadowAccess');
            $("#sdCardNEET").removeClass('shadowAccess');
            $("#modifyNEET").show();
            $("#comboBoxesNEET").hide();
            selectedAccess = 'pendrive';
            program = NEETACCESS[0];
            changeProgramNEET(program);
            return;
        });




        $('#onlineNEET').click(function() {
            $("#onlineNEET").addClass('shadowAccess');
            $("#pendriveNEET").removeClass('shadowAccess');
            $("#sdCardNEET").removeClass('shadowAccess');
            $("#modifyNEET").show();

            $("#comboBoxesNEET").hide();
            selectedAccess = 'online';
            program = NEETACCESS[1];
            changeProgramNEET(program);
            return;
        });


         $('#sdCardNEET').click(function() {
            $("#sdCardNEET").addClass('shadowAccess');
            $("#pendriveNEET").removeClass('shadowAccess');
            $("#onlineNEET").removeClass('shadowAccess');
            $("#modifyNEET").show();

            $("#comboBoxesNEET").hide();
            selectedAccess = 'sdCard';
            program = NEETACCESS[2];
            changeProgramNEET(program);
            return;
        });

        $("#modifydisplayNEET").click(function()
        {
            $("#comboBoxesNEET").toggle();
        });


        $('#durationComboBoxNEET').change(function() {
            var duration = document.getElementById("durationComboBoxNEET");
            if(this.value=="12 Months")
            {
                changeDurationNEET(0);
                document.getElementById("durationexplainNEET").innerHTML = "You have selected ScoreNEET 12 Months program, this gives you access till NEET 2018";
                document.getElementById("durationAgainNEET").innerHTML="12 Months";
                $('#12MonthsNEET').prop("checked", true);
            }
            else if(this.value=="24 Months"){
                changeDurationNEET(1);
                document.getElementById("durationexplainNEET").innerHTML = "You have selected ScoreNEET 24 Month program, this gives you access till NEET 2019";
                document.getElementById("durationAgainNEET").innerHTML="24 Months";
                $('#24MonthsNEET').prop("checked", true);
            }
         });

           $('#accessComboBoxNEET').change(function() {
                $("#sdCardNEET").removeClass('shadowAccess');
                $("#pendriveNEET").removeClass('shadowAccess');
                $("#onlineNEET").removeClass('shadowAccess');
                $("#sdCardNEETContent").removeClass('active');
                $("#pendriveNEETContent").removeClass('active');
                $("#onlineNEETContent").removeClass('active');
            if(this.value=='Pendrive'){
                selectedAccess = 'pendrive';
                $("#pendriveNEET").addClass('shadowAccess');
                $("#pendriveNEETContent").addClass('active');
                program = NEETACCESS[0];
            changeProgramNEET(program);
            }
            else if(this.value=='Online'){
                selectedAccess = 'online';
                $("#onlineNEET").addClass('shadowAccess');
                $("#onlineNEETContent").addClass('active');
                program = NEETACCESS[1];
            changeProgramNEET(program);

            }
            else if(this.value=='SD Card'){
                $("#sdCardNEET").addClass('shadowAccess');
                $("#sdCardNEETContent").addClass('active');
                program = NEETACCESS[2];
            changeProgramNEET(program);

            }
         });


        function hideTagsNEET()
        {
            $("#ScoreNeetbtn").addClass('shadow');   
            $("#ScoreJeebtn").removeClass('shadow'); 
            $("#programs").hide();
            $("#finalDecision").hide();
            $("#comboBoxes").hide();
            $("#modify").hide();
            $("#buyNow").empty();
            $("#pricing").hide();
            $("#programs").fadeIn("slow");
        }


         // var delay = 5000000; 
         // setTimeout(function(){ 
         //    window.location = "#notready"; 
         //    $("#notready").show();}, delay);

    });

    
