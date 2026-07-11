var users = new function(){
    this.init = function(){
        $(".wrapper").off("click")
                     .on("click",".syncUserInfo",syncUserInfo)
                     .on("click",".resyncUser",resyncUser);
        populateUserTable();
    }

    function populateUserTable(){
        $("#user-table").DataTable({
        "createdRow": function( row, data, dataIndex ) {
                $(row).attr("data-user-id",data.userId);
                $(row).append("<td><button class='btn btn-success resyncUser'>Re-sync</button></td>");
        },
        "processing": true,
        "serverSide": true,
        "ajax":{url:"index.php/getUsers",
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
    { "data": "memberId" }
    ],
});
    }

    var syncUserInfo = function(){
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        syncUserFromCloud("Syncing Users..Do not refresh","");
    }

    var syncUserFromCloud = function(message,targetUserId){
        showLoader(message);
        var params = {};
        if(targetUserId != null && targetUserId != undefined && targetUserId != ""){
            params.targetUserId = targetUserId;
        }
        $.post("index.php/syncUsers",params,function(data){
            hideLoader();
            var count = data['usersSynced'];
            var error = data['error'];
            if(error !=""){
                swal({
                    title:"Something went wrong "+error,
                    type:"error"
                });
            }
            else{
                if(count >0){
                    swal({
                        title:"Users successfully synced. Added/Updated " + count + " new users ",
                        type:"success"
                    });
                }
                else if(count == 0){
                    swal({
                        title:"Already upto date",
                        type:"success"
                    });
                }
                else{
                    swal({
                        title:"Something went wrong",
                        type:"error"
                    });
                }
            }
            setTimeout(function(){
                location.reload()
            },2000);
        }).fail(function(response){
            hideLoader();
            console.log(response);
            alert("Something went wrong");
        });
    }

    var resyncUser = function(){
        var targetUserId = $(this).closest("tr").data("userId");
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        syncUserFromCloud("Re-syncing User..Do not refresh",targetUserId);
    }
}