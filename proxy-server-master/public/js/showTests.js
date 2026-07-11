var showTests = new function(){
  var userId;
  this.init = function(){
    showLoader("Loading...");
    var url=window.location.href;
    if (url.indexOf("sectionId")>-1)
    {
      var sectionId=getURLParameter("sectionId");
      params={
        targetId:sectionId
      };
      $.post("index.php/showTests",params,function(data){
        hideLoader();
        var no_of_tests = data.result.length;
        var result = data.result;
        if(no_of_tests === 0){
          $("#programTitle").text("");
          $("#testData").html("<center><h2 id='heading'>No Tests to show (Please sync if not synced)</h2></center>");
        }
        else{
          $("#testData").html("<table id='testDataTable' class='table table-bordered'><tr><th>S.No</th><th>Tests</th><th></th></tr>");
          for (i = 0; i < no_of_tests; i++) {
            $("#testDataTable").append('<tr>'+
              '<td>' +(i+1)+ '</td>'+
              '<td>' +result[i].testName+ '</td>'+
              '<td>'+
              '<button class="btn btn-success syncRankList" data-target-id="' + result[i].testId +'">Get/Update Ranklist' + '</button></td>'+
              '</tr>'
              );
          }
        }
        $("#testDataTable").on("click",".syncRankList",syncRankList);
      });
    }
    else{
      $("#programTitle").text("");
      $("#testData").html("<center><h2 id='heading'>No Tests to show</h2></center>");
    }

    if(url.indexOf("sectionName")>-1){
      $("#programTitle").text("Tests in "+getURLParameter("sectionName"));
    }

    if(url.indexOf("userId")>-1){
      userId = getURLParameter("userId");
      console.log(userId);
    }
  }

  var syncRankList = function(){
    showLoader("Syncing Ranklist, please do not refresh");
    var connection = checkConnection();
    if(connection === false){
      hideLoader();
      return false;
    }
    $(".syncRankList").addClass("disabled");
    var targetId =$(this).data('targetId');
    params={
      userId:userId,
      testId:targetId
    };
    console.log(params);
    $.post('index.php/syncRankList',params,function(data){
      hideLoader();
      if(data.errorCode !=="" || data.result === "Success"){
        swal({
          title:"Ranklist updated successfully",
          type:"success"
        });
      }
      else{
        swal({
          title:"Some error has occured,please try again",
          type:"error"
        });
      }
      $(".syncRankList").removeClass("disabled");
    }).fail(function(response){
      hideLoader();
      console.log(response);
      alert("Something went wrong");
    });
  }
}