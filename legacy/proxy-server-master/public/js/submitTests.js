var submitTest = new function() {
  this.init = function() {
    populateTestSubmissionsData();
    // $.post("index.php/totalTestsSubmissions", {}, function(data) {
    //   var no_of_tests = data.totalTests;
    //   var tests = data.tests;
    //   if (data.totalTests == 0) {
    //     $("#testData").html("<center><h2 id='heading'>All Tests Submitted</h2></center>");
    //   } else {
    //     $("#testData").html("<table id='testDataTable' class='table table-bordered'><tr><th>Tests</th><th>Submissions</th><th></th></tr>");
    //     for (i = 0; i < no_of_tests; i++) {
    //       $("#testDataTable").append("<tr><td><a href='showTestUsers.php?testId="+tests[i].testId+"'> "+tests[i].testName+ "</td></a>" + "<td>" + tests[i].count + "</td>" + "<td><button class='btn btn-success submitEachTest' data-test-id='" + tests[i].testId + "'>" +'Submit'+ "</button></td></tr>");
    //     }
    //     if(data.totalTests>1){
    //       $(".submitAllTests").css("display","block");
    //     }
    //   }
    // });
  };

  function populateTestSubmissionsData(){
        $("#submit-tests-table").DataTable({
        "createdRow": function( row, data, dataIndex ) {
          // console.log(data);
                $(row).find("td:nth-child(1)").html("<a href='showTestUsers.php?testId="+data.testId+"&testName="+data.testName+"'>"+data.testName+"</a>");
                $(row).attr("data-test-id",data.testId);
                $(row).find("td:nth-child(3)").html(new Date(parseInt(data.last_atttempt_time)).toLocaleString());
                // $(row).append("<td><button class='btn btn-success submitEachTest'>Submit</button></td>");
        },
        "processing": true,
        "serverSide": true,
        "ajax":{url:"index.php/totalTestsSubmissions",
        dataSrc:function(data){
            // console.log(data);
            return data.tests;
        }
    },
    "lengthMenu":[25,50,75,100],
    "order":[[2,"desc"]],
    "language": {
        searchPlaceholder: "Search for records..."
    },
    "columns": [
    {"data":"testName"},
    { "data": "count" },
    { "data" : "last_atttempt_time"}
    ],
});
    }

  
  // var submitEachTest = function(){
  //   var connection = checkConnection();
  //   if(connection === false){
  //     return false;
  //   }
  //   $(".submitEachTest").addClass("disabled");
  //   $(".submitAllTests").addClass("disabled");
  //   showLoader("Submitting Test...Please wait, do not refresh!!!");
  //   var testId = $(this).closest("tr").data("testId");
  //   $.post("index.php/submitTest/"+testId,{},function(data){
  //     hideLoader();
  //     if(data.errorCode!=="" || data.result.totalSubmits === 0){
  //       swal({
  //         title:"Something Went Wrong, Please Try Again",
  //         type:"warning"
  //       });
  //     }else if(data.errorCode!=="" || data.result.totalSubmits !== data.result.initialCount){
  //       swal({
  //         title:"Partial Tests Submitted, Please Try Again",
  //         type:"warning"
  //       });
  //     }
  //     else{
  //       swal({
  //         title:"Test successfully submitted",
  //         type:"success"
  //       });
  //     }
  //     setTimeout(function(){
  //       location.reload()
  //     },10000);
  //   });
  // }

  // var submitAllTests = function(){
  //   var connection = checkConnection();
  //   if(connection === false){
  //     return false;
  //   }
  //   $(".submitEachTest").addClass("disabled");
  //   $(".submitAllTests").addClass("disabled");
  //   showLoader("Submitting All Tests...Please wait, do not refresh!!!");
  //   $.post("index.php/submitAllTests",{},function(data){
  //     hideLoader();
  //     if(data.errorCode!="" || data.result.totalSubmits == 0){
  //       swal({
  //         title:"Something Went Wrong, Please Try Again",
  //         type:"warning"
  //       });
  //     }else if(data.errorCode!="" || data.result.totalSubmits != data.result.initialCount){
  //       swal({
  //         title:"Partial Tests Submitted, Please Try Again",
  //         type:"warning"
  //       });
  //     }
  //     else{
  //       swal({
  //         title:"Tests successfully submitted",
  //         type:"success"
  //       });
  //     }
  //     setTimeout(function(){
  //       location.reload()
  //     },2000);
  //   });
  // }
}
