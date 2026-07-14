var subjects=new(function($){
    this.init=function(params){
        populateSelectOpts("SUBJECT");
        $(".instAddSubTags").off("change")
                            .on("change","#subjectPicker",selectDoubtSubject);

        $(".instAddTopicTags").off("change")
                              .on("change","#topicPicker",selectDoubtTopic);
    };
})(jQuery);

function selectDoubtTopic(){
    $.each($("#topicPicker"),function(){
      $("#topicPicker .ATCRBox").removeClass("selectTopicActive");
    });
    $.each($("#topicPicker option:selected"),function(){
      $(this).addClass("selectTopicActive");
    });
}

function returnAllTagsAdded(addTagsMasterDiv){
    var brdIds=[];
    addTagsMasterDiv.find("option:selected").each(function(){
       brdIds.push($(this).data("brdId"));
    });
    return {brdIds:brdIds};
  }

function selectDoubtSubject(){
    var brdId;
    $.each($("#subjectPicker"),function(){
      $("#subjectPicker .ATCRBox").removeClass("selectSubActive");
    });
    $("#subjectPicker option:selected").addClass("selectSubActive");
    brdId = $("#subjectPicker .selectSubActive").data("brdId");
    var params={};
    params.start=0;params.size=25;
    params.type="TOPIC";
    params.parentId=brdId;
    $(".doubtTopicHolder").removeClass("nonner");
    showSubLoader(".showTopicLoader");
    $.get("/Register/getOrgBoards",params,function(data){
        $(".ATSelectTopicOpt").remove();
        hideSubLoader(".showSubLoader");
        $(".instAddTopicTags").removeClass("nonner");
        topics = data.result.list;
        $(".instAddTopicTags").html("<select class='ATSelectTopicOpt selectpicker' id='topicPicker' multiple data-placeholder='Choose a topic'+\
            id='topicPicker' title='Nothing selected'>");
        for(var k=0;k<topics.length;k++){
            var brdId=topics[k].id,name=topics[k].name;
            $(".ATSelectTopicOpt").append("<option name='ATCRBox' class='ATCRBox doCStream' id='ATCRBox_"+brdId+"' data-brd-id='"+brdId+"' data-name='"+name+"'>"+name+"</option>"+
              +"</select>");
         }
         $(".selectpicker").selectpicker('refresh');
    });
}
var searchQAADUrl = "/questions/searchDiagram";
function imageClick(){
  id = $(this).attr("id");
  if (window.qq) {
      createUploaderQuesAdd(id);
  } else {
      fetchScripts([{fname: "widgets/fileuploader.js", cb: createUploaderQuesAdd(id)}]);
  }
}

function formulaEditor(){
  $.get("/Widgets/fe", function(data) {
    $("#FEWrapperHolder").html(data);
    $("#feModal").modal();
  });
}
//for uploading images for add question

function createUploaderQuesAdd(id) {
    var uploader = new qq.FileUploader({
      element: document.getElementById(id),
      action: '/Application/makeFile',
      debug: true,
      params: {
          uploadFileParamName: "imageFile",
          myUserId: USERID
      },
      onComplete:function(id, fileName, responseJSON){
        if(responseJSON.success==true){
            $.post("/Application/uploadImg",{uploadFileParamName:"imageFile",myUserId:USERID,qqfile:responseJSON.fileName},function(data){
            $(".note-editable").append("<div class='RTEImageDiv'>" + data.result.imgHtml + "</div>");
          });
        }
        else{
          swal("something went wrong");
        }
      }
  });
}


function showSubLoader(divHolder){
}
function hideSubLoader(divHolder){
}
function populateSelectOpts(reqTagsType,parentId){
    var params={};
    params.start=0;params.size=25;
    params.type="COURSE";
    $.get("/Register/getOrgBoards",params,function(data){
          var tags=data.result.list;
          $(".instAddSubTags").append(
                "<select class='ATSelectOpt selectpicker' id='subjectPicker' title='Nothing selected'>");
          for(var k=0;k<tags.length;k++){
              var brdId=tags[k].id,name=tags[k].name;
          $(".ATSelectOpt").append("<option name='ATCRBox' class='ATCRBox doCStream' id='ATCRBox_"+brdId+"' data-brd-id='"+brdId+"' data-name='"+name+"'>"+name+"</option>"+
                +"</select>");
         }
         $(".selectpicker").selectpicker('refresh');
    });
  }