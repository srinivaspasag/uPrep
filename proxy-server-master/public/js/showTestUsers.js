var showTestUsers = new function(){
    this.init = function(){
        populateTestUsersTable();
        $(".submitEachTest").on("click",submitEachTest);
    }

    function populateTestUsersTable(){
        var url=window.location.href;
        var count = 0;
        if (url.indexOf("testId")>-1)
        {
            var testId = getURLParameter("testId");
        }
        if(url.indexOf("testName")> -1){
            $("#testName").html(getURLParameter("testName"));
        }
        $("#test-user-table").DataTable({
        "createdRow": function( row, data, dataIndex ) {
            // console.log(data);
                $(".submitEachTest").attr("data-test-id",data.testId);
                if(data.synced == 0){
                    $(row).find("td:nth-child(4)").html("No").addClass("redColor");
                    count++;
                }
                else{
                    $(row).find("td:nth-child(4)").html("Yes").addClass("greenColor");
                }
                if(count > 0){
                    // console.log("Inside if");
                    $(".submitTestBtn").removeClass("nonner");
                }
                else{
                    // console.log("Inside else");
                    $(".submitTestBtn").addClass("nonner");
                }
                // console.log(count);
                // $(row).append("<td><button class='btn btn-success resyncUser'>Re-sync</button></td>");
        },
        "processing": true,
        "serverSide": true,
        "ajax":{url:"index.php/getTestUsers",
        data:{testId:testId},
        dataSrc:function(data){
            return data.data;
        }
    },
    "lengthMenu":[25,50,75,100],
    "language": {
        searchPlaceholder: "Search for records..."
    },
    "columns": [
    {"data":"studentname"},
    { "data": "memberId" },
    { "data" : "deviceId" },
    { "data": "synced" }
    ],
});
    }

    var submitEachTest = function(){
    var connection = checkConnection();
    if(connection === false){
      return false;
    }
    $(".submitEachTest").addClass("disabled");
    $(".submitAllTests").addClass("disabled");
    showLoader("Submitting Test...Please wait, do not refresh!!!");

    var testId = $(this).data("testId");
    $.post("index.php/submitTest/"+testId,{},function(data){
      hideLoader();
      if(data.errorCode!=="" || data.result.totalSubmits === 0){
        swal({
          title:"Something Went Wrong, Please Try Again",
          type:"warning"
        });
      }else if(data.errorCode!=="" || data.result.totalSubmits !== data.result.initialCount){
        swal({
          title:"Partial users Synced, Please Try Again",
          type:"warning"
        });
      }
      else{
        swal({
          title:"Total users synced: "+data.result.totalSubmits,
          type:"success"
        });
      }
      setTimeout(function(){
        location.reload()
      },5000);
    });
  }
}