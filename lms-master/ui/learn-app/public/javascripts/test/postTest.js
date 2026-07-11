var postTest = new function(){
    this.init = function(){
        $('#boardSelect')
            .on("change","#board",boardChange);
        $('.q_list')
            .off('click')
            .on('click','#questionButton',changeQuestion);
        $('.questionBody')   
            .on('click', '.postTestQuesShowSoln', getSolutionsPopup);
        $('.videoSolution')
            .on("click",openVideoSolutionPopup);
        $('.postTestHead')
            .on("click",".retakeTest",retakeStudentTest);
    };

    var boardChange = function(){
        $this = $(this);
        boardId = $this.find(':selected').data("board-id");
        percentage = $this.find(':selected').data("correct-percentage")
        $('#gaugeDemo .gauge-arrow').trigger('updateGauge', percentage);
        if(boardId == "ALL"){
            $('.overallScoreDiv').removeClass('hidden');
            $('.boardAnalytics').children().addClass('hidden');
        }else{
            $('.overallScoreDiv').addClass('hidden');
            $('.boardAnalytics').children().addClass('hidden');
            $('#board_'+boardId).removeClass('hidden');
        }
    };
    var changeQuestion = function(){
        $this= $(this);
        var QId = $this.data("id");
        $('.questionBody').children().addClass('hidden');
        $('#questionBody_'+QId).removeClass('hidden');
        MathJax.Hub.Queue(["Rerender",MathJax.Hub]);
        $('#questionBody_'+QId).fadeIn(500);
        $('.solutionData').addClass('hidden');
    };



    function getSolutionsPopup(){
        var $this = $(this);
        var qId = $this.data("qid");
        if(!qId || $this.hasClass("disabled")){ return; }
        var params = {
            start : 0,
            size : $this.data("count"),
            orderBy : "timeCreated",
            attempted : true,
            qId : qId
        };
        $.get("/Tests/quesSolutions",params,function(data){
            data = "<div id='quesSolutionsPopup'>"+data+"</div>";
            $('.solutionData').html(data);
            $('.solutionData').removeClass('hidden');
            $('body').animate({
            scrollTop: $(".solutionData").offset().top
            }, 100);
        });
    };

    function openVideoSolutionPopup(){
        $this = $(this);
        var videoId = $this.data("id");
        params = {
            "id" : videoId
        }
        $.get("/MyContents/videoSolution",params,function(data){
            $('.smallVideoThumbnailHolder').addClass('hidden');
            $('.videoSolutionData').html(data);
            $('.videoSolutionData').removeClass('hidden');

        });
    };

    function retakeStudentTest(){
        swal({
          title: 'Are you sure?',
          text: "You won't be able to revert this!",
          type: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#3085d6',
          cancelButtonColor: '#d33',
          confirmButtonText: 'Yes, delete it!'
        }).then(
        function(result){
          if (result == true) {
            resetStudentTest();
          }
        });
    };

    function resetStudentTest(){
        var testId = $(".testName").data('testId');
        var userId = USERID;
        var orgId = $("#myInstitutePage").data("orgId");
        var params = {
            orgId:orgId,
            "entity.id":testId,
            "entity.type":"TEST",
            studentUserId:userId,
        }
        $.post("/Tests/resetStudentTest",params,function(data){
            if(data.errorCode != ""){
                swal({
                    title:"Something went wrong,please try again",
                    type:"warning"
                })
                return ;
            }
            institute.openPretestPage(testId);
            $(document).ready(function() {
                setTimeout(function() {
                    $("#takeTest .btn").trigger('click');
                },500);
            });
        });
    };
}