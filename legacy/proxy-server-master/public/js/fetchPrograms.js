var fetchPrograms = new function(){
    this.init = function(){
        showLoader("Loading...please wait");
        $.post("index.php/authenticateAdmin", {}, function(data) {
            hideLoader();
            try{
                var userId = data.result.id;
                $("#userId").val(userId);
                var no_of_programs = data.result.orgProfile.info.mappings.programs.length;
                var programs = data.result.orgProfile.info.mappings.programs;
            }
            catch(e){
                console.log(e);
                swal({
                    title:"Error",
                    type:"error"
                });
                return false;
            }
            if(no_of_programs === 0){
                $("#programData").html("<center><h2 id='heading'>No programs to show</h2></center>");
            }
            else {
                $("#programData").html("<table id='programDataTable' class='table table-bordered'><tr><th>Program Name</th><th></th><th></th></tr>");
                for(i=0;i<programs.length;i++){
                    program  = programs[i];
                    for(j=0;j<program.centers.length;j++){
                        center = program.centers[j];
                        for(k=0;k<center.sections.length;k++){
                            section = center.sections[k];
                            $("#programDataTable").append('<tr>' + '<td><a href="showTests.php?sectionId=' + section.id + '&sectionName=' + programs[i].name + '&userId=' + userId + '">' + programs[i].name  +','+ section.name+ '</a></td>' +
                                '<td><button class="btn btn-success syncProgram" data-target-id="' + section.id + '" data-program-name="' + programs[i].name + '" data-target-type="SECTION">'
                                +"Sync Program"+ '</button></td>'+'<td><button class="btn btn-warning syncTest" data-target-id="' + section.id + '">Sync Tests</button></td></tr>');
                        }
                    }
                }
                if(no_of_programs > 1){
                    $(".syncAllPrograms").css("display","block");
                }
            }
        }).fail(function(response){
            hideLoader();
            console.log(response);
            alert("Something went wrong");
        });
        $(".wrapper").on("click",".syncOrgInfo",syncOrgInfo);
        $(".tableContainer").on("click", ".syncProgram",syncProgram);
        $(".tableContainer").on("click", ".syncAllPrograms",syncAllPrograms);
        $(".tableContainer").on("click", ".syncTest",syncTest);
    }

    var syncProgram = function(){
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        $(".syncProgram").addClass("disabled");
        $(".syncAllPrograms").addClass("disabled");
        var programName = $(this).data("programName");
        showLoader("Syncing Program "+programName+", do not refresh");
        var userId = $("#userId").val();
        var targetId =$(this).data('targetId');
        params={
            userId:userId,
            targetId:targetId
        };
        $.post('index.php/syncProgram',params,function(data){
            hideLoader();
            var count = data['newContentSynced'];
            var failed = data['failed'];
            if(count === 0){
                swal({
                    title:"Already upto date",
                    type:"success"
                });
            }
            else if(count > 0){
                swal({
                    title:"Data successfully synced. Added " + count + " new items " + failed + " failed items",
                    type:"success"
                });
            }
            if(count === null || count === undefined) {
                swal({
                    title:"Something went wrong",
                    type:"error"
                });
                return false;
            }
        }).fail(function(response){
            hideLoader();
            console.log(response);
            alert("Something went wrong");
        });
        $(".syncProgram").removeClass("disabled");
        $(".syncAllPrograms").removeClass("disabled");
    };

    var syncOrgInfo = function(){
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        showLoader("Syncing org info...please wait");
        $.post("index.php/syncOrgInfo",{},function(data){
            hideLoader();
            if(data.errorCode !==""){
                swal({
                    title:"Something went wrong, please try again",
                    type:"error"
                });
                return false;
            }
            else{
                swal({
                    title:"Successfully synced",
                    type:"success"
                });
            }
        }).fail(function(response){
            hideLoader();
            console.log(response);
            alert("Something went wrong");
        });
    }

    var syncAllPrograms = function(){
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        $(".syncProgram").addClass("disabled");
        $(".syncAllPrograms").addClass("disabled");
        showLoader("Syncing All Programs...Please wait, do not refresh!!!");
        var userId = $("#userId").val();
        var targetIds=[];
        var count=0;
        $(".syncProgram").each(function(){
            targetIds[count] = $(this).data('targetId');
            count = count+1;
        });
        var params = {
            userId:userId,
            targetId:""
        };
        var promises = [];
        for(targetId in targetIds){
            params['targetId'] = targetIds[targetId];
            var request = $.ajax({
                url:"index.php/syncProgram",
                data:params,
                method:'post',
                success:function(data){
                }
            });
            promises.push(request);
        }
        $.when.apply(null, promises).done(function() {
            var count =0;
            var failed = 0;
            $.each(arguments, function(index, responseData){
                count += responseData[0].newContentSynced;
                failed += responseData[0].failed;
            });
            hideLoader();
            if(count === 0){
                swal({
                    title:"Already upto date",
                    type:"success"
                });
            }
            else if(count > 0){
                swal({
                    title:"Data successfully synced. Added " + count + " new items "  + failed + " failed items",
                    type:"success"
                });
            }
            if(count === null || count === undefined) {
                swal({
                    title:"Something went wrong, please try again",
                    type:"error"
                });
                return false;
            }
        }).fail(function(response){
            hideLoader();
            console.log(response);
            alert("Something went wrong");
        });
        $(".syncProgram").removeClass("disabled");
        $(".syncAllPrograms").removeClass("disabled");
    }

    var syncTest = function(){
        var connection = checkConnection();
        if(connection === false){
            return false;
        }
        $(".syncTest").addClass("disabled");
        showLoader("Syncing Test...Please wait, do not refresh!!!");
        var targetId = $(this).data("targetId");
        var userId = $("#userId").val();
        params = {
            userId:userId,
            targetId:targetId
        };
        $.post("index.php/syncTests",params,function(data){
            hideLoader();
            if(data.errorCode !==""){
                console.log(data);
                swal({
                    title:"Something went wrong, please try again",
                    type:"error"
                });
                return false;
            }
            else if(data.result === null || data.result === undefined){
                console.log(data);
                swal({
                    title:"Something went wrong",
                    type:"error"
                });
                return false;
            }
            else{
                var totalTests = data.result.totalTests;
                var successfullTests = data.result.successfullTests;
                if(totalTests === 0){
                    swal({
                        title:"No Tests Available"+"\r\n"+"Please Sync Your Program, If not synced",
                        type:"warning"
                    });
                }
                if(successfullTests === 0 && totalTests>0){
                    swal({
                        title:"Already upto date",
                        type:"success"
                    });
                }
                else if(successfullTests > 0){
                    swal({
                        title:"Tests Successfully Synced. Added " + successfullTests + " tests "+"\r\n"+"Total tests available "+totalTests,
                        type:"success"
                    });
                }
                $(".syncTest").removeClass("disabled");
            }
        });
    }
}