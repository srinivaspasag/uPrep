var showLoader = function(message){
    $(".overlay").show();
    $(".loader").css("display","block");
    if(message !== null || message !== undefined){
      $(".loadingText").html(""+message);
    }
  }

  var hideLoader = function(){
    $(".overlay").hide();
    $(".loader").css("display","none");
  }

  function getURLParameter(name){
    var param="";
    try{
      var url = location.search;
      if(!url){ return param;}
      url = url.replace("?","");
      name += "=";
      var nameIndex = url.indexOf(name);
      if(nameIndex>=0){
        param = url.substr(nameIndex+name.length);
        if((eIndex = param.indexOf("&"))>0){
          param = param.substring(0,eIndex);
        }
        param = decodeURIComponent(param);
      }
    }
    catch(err){ param="";}
    return param;
  };

  var checkConnection = function(){
    var online = true;
    $.ajax({
      url:"index.php/checkConnection",
      type:"POST",
      async:false,
      success:function(data){
        if(data.httpcode===0){
          alert("Please check your internet connection");
          online=false;
        }
        else if(data.httpcode===404){
          alert("Page not found,please try again later");
          online=false;
        }
        else if(data.httpcode===502){
          alert("Bad Gateway,please try again later");
          online=false;
        }
      }
    });
    return online;
  }